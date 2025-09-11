using System;
using System.Collections.Generic;
using Dev;
using Unity.Burst;
using Unity.Collections;
using Unity.Jobs;
using Unity.Mathematics;
using Unity.VisualScripting;
using UnityEngine;
using UnityEngine.Rendering;
using static Universe.Data.Chunk.Chunk;

namespace Universe.Data.Chunk {
	public struct ChunkBuildResult {
		public Mesh mesh;
		public int vertexCount;
		public int triangleCount;
		public int blockCount;
		public int faceCount;
		public int lodLevel;
		public float buildTime;
	}

	public struct LODInfo {
		public int faceSize;
		public int lodLevel;
		public float distance;
		public string description;
	}

	public class ChunkBuilder : MonoBehaviour {
		// Optional resolver to query blocks outside the local chunk bounds.
		public static Func<IChunkData, int, int, int, short> ExternalBlockResolver;

		// Enhanced LOD control with more granular settings
		[Header("LOD Settings")]
		[InspectorLabel("LOD Enabled")]
		[Tooltip("Enable dynamic LOD based on camera distance")]
		public static bool LODEnabled = true;

		[InspectorLabel("LOD Distance Scale")]
		[Tooltip("Distance multiplier for LOD calculations - higher = more aggressive LOD")]
		public static float LODDistanceScale = 64f;

		[InspectorLabel("LOD Levels")]
		[Tooltip("Number of discrete LOD levels (2-8)")]
		[Range(2, 8)]
		public static int LODLevelCount = 8;

		[InspectorLabel("Min Face Size")]
		[Tooltip("Minimum face size for highest detail LOD")]
		[Range(1, 4)]
		public static int LODMinFaceAtNear = 1;

		[InspectorLabel("Max Face Size")]
		[Tooltip("Maximum face size for lowest detail LOD")]
		[Range(4, 32)]
		public static int LODMaxFaceAtFar = 32;

		[Header("Performance")]
		[InspectorLabel("Enable Multithreading")]
		[Tooltip("Use Unity Job System for mesh building")]
		public static bool EnableMultithreading = true;

		[InspectorLabel("Batch Size")]
		[Tooltip("Number of faces to process per job batch")]
		public static int JobBatchSize = 64;

		// Performance tracking
		private static Dictionary<int, float> lodBuildTimes = new Dictionary<int, float>();
		private static int totalBuilds = 0;

		public static ChunkBuildResult BuildChunk(IChunkData chunk, Vector3 chunkPosition, IChunkData[] neighbors, int? forcedLOD = null) {
			var startTime = Time.realtimeSinceStartup;

			// Set up the external block resolver for this build operation
			ExternalBlockResolver = (c, x, y, z) => {
				// Determine which neighbor chunk to query based on x, y, z
				// This logic needs to be robust to handle all 6 directions
				// For simplicity, let's assume a fixed order for neighbors: +X, -X, +Y, -Y, +Z, -Z
				// This will need to be properly implemented based on how neighbors are passed

				int neighborIndex = -1; // Placeholder
				int localX = x, localY = y, localZ = z;
				int s = ChunkSize;

				if (x >= s) {
					neighborIndex = 0;
					localX = x - s;
				} // +X
				else if (x < 0) {
					neighborIndex = 1;
					localX = x + s;
				} // -X
				else if (y >= s) {
					neighborIndex = 2;
					localY = y - s;
				} // +Y
				else if (y < 0) {
					neighborIndex = 3;
					localY = y + s;
				} // -Y
				else if (z >= s) {
					neighborIndex = 4;
					localZ = z - s;
				} // +Z
				else if (z < 0) {
					neighborIndex = 5;
					localZ = z + s;
				} // -Z

				if (neighborIndex != -1 && neighbors != null && neighbors.Length > neighborIndex && neighbors[neighborIndex] != null) {
					return neighbors[neighborIndex].GetBlockType(neighbors[neighborIndex].GetBlockIndex(new Vector3(localX, localY, localZ)));
				}
				return 0; // Default to air if no neighbor or invalid access
			};

			var facePositions = new List<float3>();
			var faceDirs = new List<byte>();
			var faceSizes = new List<int2>();

			// Determine LOD level and face size
			LODInfo lodInfo = ComputeLODInfo(chunkPosition, forcedLOD);

			// Apply voxel downsampling if LOD level is greater than 1
			IChunkData processedChunk = chunk;
			int currentChunkSize = chunk.GetSize(); // Get original chunk size

			if (lodInfo.lodLevel > 1) { // Or a configurable threshold
				processedChunk = DownsampleChunk(chunk, lodInfo.lodLevel, neighbors);
				currentChunkSize = processedChunk.GetSize(); // Update chunk size for downsampled chunk
			}

			// Generate faces using greedy meshing with LOD-appropriate face size
			int blockCount = GenerateGreedyFaces(processedChunk, facePositions, faceDirs, faceSizes, lodInfo.faceSize, currentChunkSize);

			int totalFaces = facePositions.Count;
			if (totalFaces == 0) {
				var empty = new Mesh();
				empty.SetVertices(new List<Vector3>(0));
				empty.SetIndices(Array.Empty<int>(), MeshTopology.Triangles, 0, false);
				empty.RecalculateBounds();

				var buildTime = Time.realtimeSinceStartup - startTime;
				return new ChunkBuildResult {
					mesh = empty,
					vertexCount = 0,
					triangleCount = 0,
					blockCount = 0,
					faceCount = 0,
					lodLevel = lodInfo.lodLevel,
					buildTime = buildTime
				};
			}

			int totalVertexCount = totalFaces * 4;
			int totalIndexCount = totalFaces * 6;

			Mesh newMesh;

			if (EnableMultithreading && totalFaces > JobBatchSize) {
				newMesh = BuildMeshWithJobs(facePositions, faceDirs, faceSizes, totalVertexCount, totalIndexCount);
			}
			else {
				newMesh = BuildMeshDirect(facePositions, faceDirs, faceSizes, totalVertexCount, totalIndexCount);
			}

			var finalBuildTime = Time.realtimeSinceStartup - startTime;

			// Track performance
			UpdatePerformanceStats(lodInfo.lodLevel, finalBuildTime);

			return new ChunkBuildResult {
				mesh = newMesh,
				vertexCount = totalVertexCount,
				triangleCount = totalIndexCount / 3,
				blockCount = blockCount,
				faceCount = totalFaces,
				lodLevel = lodInfo.lodLevel,
				buildTime = finalBuildTime
			};
		}

		static LODInfo ComputeLODInfo(Vector3 chunkWorldPos, int? forcedLOD = null) {
			if (!LODEnabled) {
				return new LODInfo {
					faceSize = LODMinFaceAtNear,
					lodLevel = 0,
					distance = 0f,
					description = "LOD Disabled"
				};
			}

			var cam = GetActiveCamera();
			if (cam == null) {
				return new LODInfo {
					faceSize = LODMaxFaceAtFar,
					lodLevel = LODLevelCount - 1,
					distance = float.MaxValue,
					description = "No Camera"
				};
			}

			float distance = Vector3.Distance(cam.transform.position, chunkWorldPos);

			// Use forced LOD if provided
			if (forcedLOD.HasValue) {
				int clampedLOD = Mathf.Clamp(forcedLOD.Value, 0, LODLevelCount - 1);
				int forcedFaceSize = GetFaceSizeForLODLevel(clampedLOD);
				return new LODInfo {
					faceSize = forcedFaceSize,
					lodLevel = clampedLOD,
					distance = distance,
					description = $"Forced LOD {clampedLOD}"
				};
			}

			// Calculate LOD level based on distance
			float lodFloat = distance / LODDistanceScale;
			int lodLevel = Mathf.Clamp((int)lodFloat, 0, LODLevelCount - 1);
			int faceSize = GetFaceSizeForLODLevel(lodLevel);

			return new LODInfo {
				faceSize = faceSize,
				lodLevel = lodLevel,
				distance = distance,
				description = $"Auto LOD {lodLevel} (dist: {distance:F1}, cam: {cam.name})"
			};
		}

		/// <summary>
		/// Get the currently active camera (Scene camera in editor, or main camera in play mode)
		/// </summary>
		static Camera GetActiveCamera() {
#if UNITY_EDITOR
			// In editor, prefer Scene view camera when not playing
			if (!Application.isPlaying) {
				var sceneView = UnityEditor.SceneView.lastActiveSceneView;
				if (sceneView != null && sceneView.camera != null) {
					return sceneView.camera;
				}
			}

			// If playing in editor, try Scene view camera first, then main camera
			if (Application.isPlaying) {
				var sceneView = UnityEditor.SceneView.lastActiveSceneView;
				if (sceneView != null &&
				    sceneView.camera != null &&
				    UnityEditor.SceneView.currentDrawingSceneView == sceneView) {
					return sceneView.camera;
				}
			}
#endif
			// Fall back to main camera
			return Camera.main;
		}

		static int GetFaceSizeForLODLevel(int lodLevel) {
			if (LODLevelCount <= 1) return LODMinFaceAtNear;

			float t = (float)lodLevel / (LODLevelCount - 1);
			// Apply non-linear scaling for LOD aggressiveness
			int faceSize = Mathf.RoundToInt(Mathf.Lerp(LODMinFaceAtNear, LODMaxFaceAtFar, t));

			faceSize = Mathf.NextPowerOfTwo(faceSize);

			return Mathf.Clamp(faceSize, LODMinFaceAtNear, Math.Min(LODMaxFaceAtFar, ChunkSize));
		}

		static Mesh BuildMeshWithJobs(List<float3> facePositions, List<byte> faceDirs, List<int2> faceSizes,
			int totalVertexCount, int totalIndexCount) {

			var buildJob = new BuildFacesJob {
				FacePositions = new NativeArray<float3>(facePositions.ToArray(), Allocator.TempJob),
				FaceDirs = new NativeArray<byte>(faceDirs.ToArray(), Allocator.TempJob),
				FaceSizes = new NativeArray<int2>(faceSizes.ToArray(), Allocator.TempJob)
			};

			var outputMeshData = Mesh.AllocateWritableMeshData(1);
			buildJob.OutputMesh = outputMeshData[0];
			buildJob.OutputMesh.SetIndexBufferParams(totalIndexCount, IndexFormat.UInt32);
			buildJob.OutputMesh.SetVertexBufferParams(
				totalVertexCount,
				new VertexAttributeDescriptor(VertexAttribute.Position, stream: 0),
				new VertexAttributeDescriptor(VertexAttribute.Normal, stream: 1),
				new VertexAttributeDescriptor(VertexAttribute.TexCoord0, dimension: 2, stream: 2)
			);

			var handle = buildJob.Schedule(facePositions.Count, JobBatchSize);
			var newMesh = new Mesh();
			var sm = new SubMeshDescriptor(0, totalIndexCount) { firstVertex = 0, vertexCount = totalVertexCount };

			handle.Complete();

			buildJob.OutputMesh.subMeshCount = 1;
			buildJob.OutputMesh.SetSubMesh(0, sm);
			Mesh.ApplyAndDisposeWritableMeshData(outputMeshData, new[] { newMesh });

			buildJob.FacePositions.Dispose();
			buildJob.FaceDirs.Dispose();
			buildJob.FaceSizes.Dispose();

			newMesh.RecalculateBounds();
			return newMesh;
		}

		static Mesh BuildMeshDirect(List<float3> facePositions, List<byte> faceDirs, List<int2> faceSizes,
			int totalVertexCount, int totalIndexCount) {

			var vertices = new Vector3[totalVertexCount];
			var normals = new Vector3[totalVertexCount];
			var uvs = new Vector2[totalVertexCount];
			var triangles = new int[totalIndexCount];

			for(int i = 0; i < facePositions.Count; i++) {
				int vStart = i * 4;
				int tStart = i * 6;

				BuildFace(facePositions[i],
					faceDirs[i],
					faceSizes[i],
					vertices,
					normals,
					uvs,
					triangles,
					vStart,
					tStart);
			}

			var mesh = new Mesh();
			mesh.SetVertices(vertices);
			mesh.SetNormals(normals);
			mesh.SetUVs(0, uvs);
			mesh.SetTriangles(triangles, 0);
			mesh.RecalculateBounds();

			return mesh;
		}

		static void BuildFace(float3 position, byte direction, int2 size,
			Vector3[] vertices, Vector3[] normals, Vector2[] uvs, int[] triangles,
			int vStart, int tStart) {

			float3 o = position;
			int su = size.x;
			int sv = size.y;

			Vector3 v0, v1, v2, v3;
			switch (direction) {
				case 0: // +X
					v0 = new Vector3(o.x + 0.5f, o.y - 0.5f, o.z - 0.5f);
					v1 = new Vector3(o.x + 0.5f, o.y - 0.5f, o.z + su - 0.5f);
					v2 = new Vector3(o.x + 0.5f, o.y + sv - 0.5f, o.z + su - 0.5f);
					v3 = new Vector3(o.x + 0.5f, o.y + sv - 0.5f, o.z - 0.5f);
					break;

				case 1: // -X
					v0 = new Vector3(o.x - 0.5f, o.y - 0.5f, o.z + su - 0.5f);
					v1 = new Vector3(o.x - 0.5f, o.y - 0.5f, o.z - 0.5f);
					v2 = new Vector3(o.x - 0.5f, o.y + sv - 0.5f, o.z - 0.5f);
					v3 = new Vector3(o.x - 0.5f, o.y + sv - 0.5f, o.z + su - 0.5f);
					break;

				case 2: // +Y
					v0 = new Vector3(o.x - 0.5f, o.y + 0.5f, o.z - 0.5f);
					v1 = new Vector3(o.x + su - 0.5f, o.y + 0.5f, o.z - 0.5f);
					v2 = new Vector3(o.x + su - 0.5f, o.y + 0.5f, o.z + sv - 0.5f);
					v3 = new Vector3(o.x - 0.5f, o.y + 0.5f, o.z + sv - 0.5f);
					break;

				case 3: // -Y
					v0 = new Vector3(o.x - 0.5f, o.y - 0.5f, o.z + sv - 0.5f);
					v1 = new Vector3(o.x + su - 0.5f, o.y - 0.5f, o.z + sv - 0.5f);
					v2 = new Vector3(o.x + su - 0.5f, o.y - 0.5f, o.z - 0.5f);
					v3 = new Vector3(o.x - 0.5f, o.y - 0.5f, o.z - 0.5f);
					break;

				case 4: // +Z
					v0 = new Vector3(o.x - 0.5f, o.y - 0.5f, o.z + 0.5f);
					v1 = new Vector3(o.x - 0.5f, o.y - 0.5f + sv, o.z + 0.5f);
					v2 = new Vector3(o.x - 0.5f + su, o.y - 0.5f + sv, o.z + 0.5f);
					v3 = new Vector3(o.x - 0.5f + su, o.y - 0.5f, o.z + 0.5f);
					break;

				default: // 5: -Z
					v0 = new Vector3(o.x - 0.5f + su, o.y - 0.5f, o.z - 0.5f);
					v1 = new Vector3(o.x - 0.5f + su, o.y - 0.5f + sv, o.z - 0.5f);
					v2 = new Vector3(o.x - 0.5f, o.y - 0.5f + sv, o.z - 0.5f);
					v3 = new Vector3(o.x - 0.5f, o.y - 0.5f, o.z - 0.5f);
					break;
			}

			vertices[vStart + 0] = v0;
			vertices[vStart + 1] = v1;
			vertices[vStart + 2] = v2;
			vertices[vStart + 3] = v3;

			var normal = GetFaceNormal(direction);
			normals[vStart + 0] = normal;
			normals[vStart + 1] = normal;
			normals[vStart + 2] = normal;
			normals[vStart + 3] = normal;

			uvs[vStart + 0] = new Vector2(0, 0);
			uvs[vStart + 1] = new Vector2(1, 0);
			uvs[vStart + 2] = new Vector2(1, 1);
			uvs[vStart + 3] = new Vector2(0, 1);

			triangles[tStart + 0] = vStart + 0;
			triangles[tStart + 1] = vStart + 2;
			triangles[tStart + 2] = vStart + 1;
			triangles[tStart + 3] = vStart + 0;
			triangles[tStart + 4] = vStart + 3;
			triangles[tStart + 5] = vStart + 2;
		}

		static Vector3 GetFaceNormal(byte dir) {
			return dir switch {
				0 => new Vector3(1, 0, 0),
				1 => new Vector3(-1, 0, 0),
				2 => new Vector3(0, 1, 0),
				3 => new Vector3(0, -1, 0),
				4 => new Vector3(0, 0, 1),
				_ => new Vector3(0, 0, -1)
			};
		}

		static int GenerateGreedyFaces(IChunkData chunk, List<float3> positions, List<byte> dirs, List<int2> sizes,
			int maxFaceSize, int currentChunkSize) {
			int s = currentChunkSize;
			int blockCount = 0;

			// Count blocks for stats
			int totalBlocks = s * s * s;
			for(int i = 0; i < totalBlocks; i++) {
				if (chunk.GetBlockType(i) != 0) blockCount++;
			}

			// Temporary mask arrays for greedy meshing
			short[][] mask = new short[s][];
			for(int index = 0; index < s; index++) {
				mask[index] = new short[s];
			}

			ushort GetTypeClamped(int x, int y, int z) {
				if (x < 0 || y < 0 || z < 0 || x >= currentChunkSize || y >= currentChunkSize || z >= currentChunkSize) {
					if (ExternalBlockResolver != null) {
						return (ushort)ExternalBlockResolver(chunk, x, y, z);
					}
					return 0; // Always return air block outside chunk for now
				}
				return (ushort)chunk.GetBlockType(chunk.GetBlockIndex(new Vector3(x, y, z)));
			}

			void GreedySlice(int axis, int sgn) {
				int W, H;
				for(int i = 0; i < s; i++) {
					// Build mask for slice i on given axis/sign
					if (axis == 0) {
						W = s;
						H = s; // u=z, v=y at fixed x=i
						for(int v = 0; v < H; v++)
						for(int u = 0; u < W; u++) {
							int x = i, y = v, z = u;
							ushort a = GetTypeClamped(x, y, z);
							ushort b = GetTypeClamped(x + sgn, y, z);
							mask[u][v] = (short)(a != 0 && (b == 0 || b != a) ? a : 0);
						}
					}
					else if (axis == 1) {
						W = s;
						H = s; // u=x, v=z at fixed y=i
						for(int v = 0; v < H; v++)
						for(int u = 0; u < W; u++) {
							int x = u, y = i, z = v;
							ushort a = GetTypeClamped(x, y, z);
							ushort b = GetTypeClamped(x, y + sgn, z);
							mask[u][v] = (short)(a != 0 && (b == 0 || b != a) ? a : 0);
						}
					}
					else {
						W = s;
						H = s; // axis==2, u=x, v=y at fixed z=i
						for(int v = 0; v < H; v++)
						for(int u = 0; u < W; u++) {
							int x = u, y = v, z = i;
							ushort a = GetTypeClamped(x, y, z);
							ushort b = GetTypeClamped(x, y, z + sgn);
							mask[u][v] = (short)(a != 0 && (b == 0 || b != a) ? a : 0);
						}
					}

					// Greedy merge rectangles with LOD-aware max face size
					for(int v = 0; v < s; v++) {
						for(int u = 0; u < s; u++) {
							short type = mask[u][v];
							if (type == 0) continue;

							// Find width with LOD constraint
							int w = 1;
							while (w < maxFaceSize && u + w < s && mask[u + w][v] == type) w++;

							// Find height with LOD constraint
							int h = 1;
							bool stop = false;
							while (h < maxFaceSize && v + h < s && !stop) {
								for(int k = 0; k < w; k++)
									if (mask[u + k][v + h] != type) {
										stop = true;
										break;
									}
								if (!stop) h++;
							}

							// Clear processed area from mask
							for(int dv = 0; dv < h; dv++)
							for(int du = 0; du < w; du++)
								mask[u + du][v + dv] = 0;

							// Emit face with proper orientation
							if (axis == 0) {
								positions.Add(new float3(i, v, u));
								dirs.Add((byte)(sgn > 0 ? 0 : 1));
								sizes.Add(new int2(w, h));
							}
							else if (axis == 1) {
								positions.Add(new float3(u, i, v));
								dirs.Add((byte)(sgn > 0 ? 2 : 3));
								sizes.Add(new int2(w, h));
							}
							else {
								positions.Add(new float3(u, v, i));
								dirs.Add((byte)(sgn > 0 ? 4 : 5));
								sizes.Add(new int2(w, h));
							}
						}
					}
				}
			}

			// Process all 6 directions
			GreedySlice(0, +1); // +X
			GreedySlice(0, -1); // -X
			GreedySlice(1, +1); // +Y
			GreedySlice(1, -1); // -Y
			GreedySlice(2, +1); // +Z
			GreedySlice(2, -1); // -Z

			return blockCount;
		}

		static void UpdatePerformanceStats(int lodLevel, float buildTime) {
			totalBuilds++;

			if (!lodBuildTimes.ContainsKey(lodLevel)) {
				lodBuildTimes[lodLevel] = buildTime;
			}
			else {
				// Running average
				lodBuildTimes[lodLevel] = (lodBuildTimes[lodLevel] + buildTime) * 0.5f;
			}
		}

		/// <summary>
		/// Get performance statistics for debugging
		/// </summary>
		public static string GetPerformanceStats() {
			var stats = new System.Text.StringBuilder();
			stats.AppendLine($"ChunkBuilder Performance Stats:");
			stats.AppendLine($"Total Builds: {totalBuilds}");
			stats.AppendLine($"Multithreading: {EnableMultithreading}");
			stats.AppendLine($"LOD Enabled: {LODEnabled}");

			foreach (var kvp in lodBuildTimes) {
				stats.AppendLine($"LOD {kvp.Key} Avg Build Time: {kvp.Value * 1000:F2}ms");
			}

			return stats.ToString();
		}

		/// <summary>
		/// Clear performance statistics
		/// </summary>
		[ContextMenu("Clear Performance Stats")]
		public static void ClearPerformanceStats() {
			lodBuildTimes.Clear();
			totalBuilds = 0;
		}

		/// <summary>
		/// Build chunk with specific LOD level (for debugging)
		/// </summary>
		public static ChunkBuildResult BuildChunkAtLOD(IChunkData chunk, Vector3 chunkPosition, IChunkData[] neighbors, int lodLevel) {
			return BuildChunk(chunk, chunkPosition, neighbors, lodLevel);
		}

		/// <summary>
		/// Get estimated face count for LOD level (for performance prediction)
		/// </summary>
		public static int EstimateFaceCountForLOD(int lodLevel, int blockCount) {
			int faceSize = GetFaceSizeForLODLevel(lodLevel);
			float reductionFactor = 1f / (faceSize * faceSize);
			return Mathf.RoundToInt(blockCount * 6 * reductionFactor); // 6 faces per block max
		}

		/// <summary>
		/// Downsamples a chunk for LOD, reducing the visual block count.
		/// </summary>
		/// <param name="originalChunk">The original chunk data.</param>
		/// <param name="lodLevel">The current LOD level.</param>
		/// <returns>A new IChunkData representing the downsampled chunk.</returns>
		public static IChunkData DownsampleChunk(IChunkData originalChunk, int lodLevel, IChunkData[] neighbors) {
			if (lodLevel <= 1) { // No downsampling for LOD 0 and 1
				return originalChunk;
			}

			int originalSize = originalChunk.GetSize();
			int downsampleFactor = 1 << (lodLevel - 1); // 2^(lodLevel-1) for LOD 1, 2, 4, etc.
			if (downsampleFactor >= originalSize) { // Prevent downsampling beyond chunk size
				downsampleFactor = originalSize;
			}

			int newSize = originalSize / downsampleFactor;
			int[] downsampledBlocks = new int[newSize * newSize * newSize];

			// Temporarily set up the external block resolver for this downsampling operation
			// This assumes the neighbors array is correctly ordered (+X, -X, +Y, -Y, +Z, -Z)
			Func<IChunkData, int, int, int, short> currentExternalBlockResolver = (c, x, y, z) => {
				int neighborIndex = -1;
				int localX = x, localY = y, localZ = z;
				int s = originalSize; // Use originalSize for neighbor lookup

				if (x >= s) {
					neighborIndex = 0;
					localX = x - s;
				} // +X
				else if (x < 0) {
					neighborIndex = 1;
					localX = x + s;
				} // -X
				else if (y >= s) {
					neighborIndex = 2;
					localY = y - s;
				} // +Y
				else if (y < 0) {
					neighborIndex = 3;
					localY = y + s;
				} // -Y
				else if (z >= s) {
					neighborIndex = 4;
					localZ = z - s;
				} // +Z
				else if (z < 0) {
					neighborIndex = 5;
					localZ = z + s;
				} // -Z

				if (neighborIndex != -1 && neighbors != null && neighbors.Length > neighborIndex && neighbors[neighborIndex] != null) {
					return neighbors[neighborIndex].GetBlockType(neighbors[neighborIndex].GetBlockIndex(new Vector3(localX, localY, localZ)));
				}
				return 0; // Default to air if no neighbor or invalid access
			};


			for(int z = 0; z < newSize; z++) {
				for(int y = 0; y < newSize; y++) {
					for(int x = 0; x < newSize; x++) {
						bool isSolid = false;
						for(int dz = 0; dz < downsampleFactor; dz++) {
							for(int dy = 0; dy < downsampleFactor; dy++) {
								for(int dx = 0; dx < downsampleFactor; dx++) {
									int originalX = x * downsampleFactor + dx;
									int originalY = y * downsampleFactor + dy;
									int originalZ = z * downsampleFactor + dz;

									ushort blockType;
									if (originalX < 0 ||
									    originalX >= originalSize ||
									    originalY < 0 ||
									    originalY >= originalSize ||
									    originalZ < 0 ||
									    originalZ >= originalSize) {
										// Query from neighbor if out of bounds
										blockType = (ushort)currentExternalBlockResolver(originalChunk, originalX, originalY, originalZ);
									}
									else {
										blockType = (ushort)originalChunk.GetBlockType(originalChunk.GetBlockIndex(new Vector3(originalX, originalY, originalZ)));
									}

									if (blockType != 0) {
										isSolid = true;
										break;
									}
								}
								if (isSolid) break;
							}
							if (isSolid) break;
						}
						if (isSolid) {
							// Set a default solid block type (e.g., 1)
							downsampledBlocks[x + y * newSize + z * newSize * newSize] = 1;
						}
						else {
							downsampledBlocks[x + y * newSize + z * newSize * newSize] = 0; // Air
						}
					}
				}
			}

			return new DownsampledChunkData(downsampledBlocks, newSize);
		}

		[BurstCompile]
		struct BuildFacesJob : IJobParallelFor {
			public Mesh.MeshData OutputMesh;
			[ReadOnly] public NativeArray<float3> FacePositions;
			[ReadOnly] public NativeArray<byte> FaceDirs;
			[ReadOnly] public NativeArray<int2> FaceSizes;

			public void Execute(int index) {
				int vStart = index * 4;
				int tStart = index * 6;
				float3 o = FacePositions[index];
				byte dir = FaceDirs[index];
				int2 size = FaceSizes[index];
				int su = size.x;
				int sv = size.y;

				var outputVerts = OutputMesh.GetVertexData<Vector3>();
				var outputNormals = OutputMesh.GetVertexData<Vector3>(stream: 1);
				var outputUVs = OutputMesh.GetVertexData<Vector2>(stream: 2);
				var outputTris = OutputMesh.GetIndexData<int>();

				Vector3 v0, v1, v2, v3;
				switch (dir) {
					case 0: // +X
						v0 = new Vector3(o.x + 0.5f, o.y - 0.5f, o.z - 0.5f);
						v1 = new Vector3(o.x + 0.5f, o.y - 0.5f, o.z + su - 0.5f);
						v2 = new Vector3(o.x + 0.5f, o.y + sv - 0.5f, o.z + su - 0.5f);
						v3 = new Vector3(o.x + 0.5f, o.y + sv - 0.5f, o.z - 0.5f);
						break;

					case 1: // -X
						v0 = new Vector3(o.x - 0.5f, o.y - 0.5f, o.z + su - 0.5f);
						v1 = new Vector3(o.x - 0.5f, o.y - 0.5f, o.z - 0.5f);
						v2 = new Vector3(o.x - 0.5f, o.y + sv - 0.5f, o.z - 0.5f);
						v3 = new Vector3(o.x - 0.5f, o.y + sv - 0.5f, o.z + su - 0.5f);
						break;

					case 2: // +Y
						v0 = new Vector3(o.x - 0.5f, o.y + 0.5f, o.z - 0.5f);
						v1 = new Vector3(o.x + su - 0.5f, o.y + 0.5f, o.z - 0.5f);
						v2 = new Vector3(o.x + su - 0.5f, o.y + 0.5f, o.z + sv - 0.5f);
						v3 = new Vector3(o.x - 0.5f, o.y + 0.5f, o.z + sv - 0.5f);
						break;

					case 3: // -Y
						v0 = new Vector3(o.x - 0.5f, o.y - 0.5f, o.z + sv - 0.5f);
						v1 = new Vector3(o.x + su - 0.5f, o.y - 0.5f, o.z + sv - 0.5f);
						v2 = new Vector3(o.x + su - 0.5f, o.y - 0.5f, o.z - 0.5f);
						v3 = new Vector3(o.x - 0.5f, o.y - 0.5f, o.z - 0.5f);
						break;

					case 4: // +Z
						v0 = new Vector3(o.x - 0.5f, o.y - 0.5f, o.z + 0.5f);
						v1 = new Vector3(o.x - 0.5f, o.y - 0.5f + sv, o.z + 0.5f);
						v2 = new Vector3(o.x - 0.5f + su, o.y - 0.5f + sv, o.z + 0.5f);
						v3 = new Vector3(o.x - 0.5f + su, o.y - 0.5f, o.z + 0.5f);
						break;

					default: // 5: -Z
						v0 = new Vector3(o.x - 0.5f + su, o.y - 0.5f, o.z - 0.5f);
						v1 = new Vector3(o.x - 0.5f + su, o.y - 0.5f + sv, o.z - 0.5f);
						v2 = new Vector3(o.x - 0.5f, o.y - 0.5f + sv, o.z - 0.5f);
						v3 = new Vector3(o.x - 0.5f, o.y - 0.5f, o.z - 0.5f);
						break;
				}

				outputVerts[vStart + 0] = v0;
				outputVerts[vStart + 1] = v1;
				outputVerts[vStart + 2] = v2;
				outputVerts[vStart + 3] = v3;

				var n = GetFaceNormal(dir);
				outputNormals[vStart + 0] = new Vector3(n.x, n.y, n.z);
				outputNormals[vStart + 1] = new Vector3(n.x, n.y, n.z);
				outputNormals[vStart + 2] = new Vector3(n.x, n.y, n.z);
				outputNormals[vStart + 3] = new Vector3(n.x, n.y, n.z);

				outputUVs[vStart + 0] = new Vector2(0, 0);
				outputUVs[vStart + 1] = new Vector2(1, 0);
				outputUVs[vStart + 2] = new Vector2(1, 1);
				outputUVs[vStart + 3] = new Vector2(0, 1);

				outputTris[tStart + 0] = vStart + 0;
				outputTris[tStart + 1] = vStart + 2;
				outputTris[tStart + 2] = vStart + 1;
				outputTris[tStart + 3] = vStart + 0;
				outputTris[tStart + 4] = vStart + 3;
				outputTris[tStart + 5] = vStart + 2;
			}

			static float3 GetFaceNormal(byte dir) {
				return dir switch {
					0 => new float3(1, 0, 0),
					1 => new float3(-1, 0, 0),
					2 => new float3(0, 1, 0),
					3 => new float3(0, -1, 0),
					4 => new float3(0, 0, 1),
					_ => new float3(0, 0, -1),
				};
			}
		}
	}
}