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

	public class ChunkBuilder : MonoBehaviour, StatsDisplay.IStatsDisplayReporter {
		// Optional resolver to query blocks outside the local chunk bounds.
		// If set, it will be used to fetch neighbor chunk block types so faces between chunks can be culled.
		public static System.Func<IChunkData, int, int, int, short> ExternalBlockResolver;
		// LOD control: defines how aggressively we merge faces based on camera distance.
		[InspectorLabel("LOD Distance Scale")]
		[Tooltip("Higher = require more distance before increasing merge size.")]
		public static float LODDistanceScale = 2f; // higher = require more distance before increasing merge size
		[InspectorLabel("LOD Min Face At Near")]
		[Tooltip("Minimum face size when the chunk is near the camera (lower = more detail).")]
		public static int LODMinFaceAtNear = ChunkSize; // near chunks keep faces small
		[InspectorLabel("LOD Max Face At Far")]
		[Tooltip("Maximum face size when the chunk is far from the camera (higher = more detail).")]
		public static int LODMaxFaceAtFar = 256; // far chunks can merge up to full chunk dimension
		//Todo: Faces between chunks arent actually being combined yet, so this setting doesnt do anything!!!
		static int _chunkCount;
		static int _blockCount;
		static int _vertexCount;
		static int _triangleCount;

		public static void BuildChunk(IChunkData chunk, MeshFilter meshFilter, MeshRenderer renderer) {

			var facePositions = new List<float3>();
			var faceDirs = new List<byte>();
			var faceSizes = new List<int2>();

			// Compute a distance-based LOD for greedy meshing. Near = smaller max face, Far = larger max face.
			int maxFace = ComputeMaxFaceSize(meshFilter.transform.position);
			// Generate faces using greedy meshing per direction with a max face size (LOD-like behavior)
			GenerateGreedyFaces(chunk, facePositions, faceDirs, faceSizes, maxFace);

			int totalFaces = facePositions.Count;
			if (totalFaces == 0) {
				var empty = new Mesh();
				empty.SetVertices(new List<Vector3>(0));
				empty.SetIndices(Array.Empty<int>(), MeshTopology.Triangles, 0, false);
				empty.RecalculateBounds();
				meshFilter.mesh = empty;
				renderer.material = Resources.Load<Material>("ChunkMaterial");
				return;
			}

			int totalVertexCount = totalFaces * 4;
			int totalIndexCount = totalFaces * 6;

			var buildJob = new BuildFacesJob {
				FacePositions = new NativeArray<float3>(facePositions.Count, Allocator.TempJob, NativeArrayOptions.UninitializedMemory),
				FaceDirs = new NativeArray<byte>(faceDirs.Count, Allocator.TempJob, NativeArrayOptions.UninitializedMemory),
				FaceSizes = new NativeArray<int2>(faceSizes.Count, Allocator.TempJob, NativeArrayOptions.UninitializedMemory)
			};
			for(int i = 0; i < facePositions.Count; i++) {
				buildJob.FacePositions[i] = facePositions[i];
				buildJob.FaceDirs[i] = faceDirs[i];
				buildJob.FaceSizes[i] = faceSizes[i];
			}

			var outputMeshData = Mesh.AllocateWritableMeshData(1);
			buildJob.OutputMesh = outputMeshData[0];
			buildJob.OutputMesh.SetIndexBufferParams(totalIndexCount, IndexFormat.UInt32);
			buildJob.OutputMesh.SetVertexBufferParams(
				totalVertexCount,
				new VertexAttributeDescriptor(VertexAttribute.Position, stream: 0),
				new VertexAttributeDescriptor(VertexAttribute.Normal, stream: 1),
				new VertexAttributeDescriptor(VertexAttribute.TexCoord0, dimension: 2, stream: 2)
			);

			var handle = buildJob.Schedule(totalFaces, 32);
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
			meshFilter.mesh = newMesh;
			renderer.material = Resources.Load<Material>("ChunkMaterial");

			// Update simple stats
			_chunkCount++;
			_vertexCount += totalVertexCount;
			_triangleCount += totalIndexCount / 3;
		}

		static int ComputeMaxFaceSize(Vector3 chunkWorldPos) {
			var cam = Camera.main;
			//Todo: Update this every time the camera moves between chunks
			if (cam == null) return math.clamp(LODMaxFaceAtFar, 1, ChunkSize);
			float dist = Vector3.Distance(cam.transform.position, chunkWorldPos);
			float unit = ChunkSize * LODDistanceScale;
			int lod = (int)math.floor(dist / math.max(1f, unit));
			lod = math.clamp(lod, 0, 10);
			int size = 1 << lod; // 1,2,4,8,...
			size = math.clamp(size, LODMinFaceAtNear, math.min(LODMaxFaceAtFar, ChunkSize));
			return size;
		}

		static void GenerateGreedyFaces(IChunkData chunk, List<float3> positions, List<byte> dirs, List<int2> sizes, int maxFaceSize) {
			int s = ChunkSize;
			// Count blocks for stats
			int totalBlocks = s * s * s;
			for(int i = 0; i < totalBlocks; i++)
				if (chunk.GetBlockType(i) != 0)
					_blockCount++;

			// Temporary mask arrays (short block type)
			short[][] mask = new short[s][];
			for(int index = 0; index < s; index++) {
				mask[index] = new short[s];
			}

			ushort getTypeClamped(int x, int y, int z) {
				if (x < 0 || y < 0 || z < 0 || x >= s || y >= s || z >= s) {
					// Ask external resolver (neighbor chunks) if available; otherwise treat as air.
					if (ExternalBlockResolver != null) {
						return (ushort)ExternalBlockResolver(chunk, x, y, z);
					}
					return 0;
				}
				return (ushort)chunk.GetBlockType(chunk.GetBlockIndex(new Vector3(x, y, z)));
			}

			void greedySlice(int axis, int sgn) {
				int W, H; // dimensions of the mask (u=W, v=H)
				for(int i = 0; i < s; i++) {
					// Build mask for slice i on given axis/sign
					if (axis == 0) {
						W = s;
						H = s; // u=z, v=y at fixed x=i
						for(int v = 0; v < H; v++)
						for(int u = 0; u < W; u++) {
							int x = i;
							int y = v;
							int z = u;
							ushort a = getTypeClamped(x, y, z);
							ushort b = getTypeClamped(x + sgn, y, z);
							mask[u][v] = (short)(a != 0 && (b == 0 || b != a) ? a : 0);
						}
					}
					else if (axis == 1) {
						W = s;
						H = s; // u=x, v=z at fixed y=i
						for(int v = 0; v < H; v++)
						for(int u = 0; u < W; u++) {
							int x = u, y = i, z = v;
							ushort a = getTypeClamped(x, y, z);
							ushort b = getTypeClamped(x, y + sgn, z);
							mask[u][v] = (short)(a != 0 && (b == 0 || b != a) ? a : 0);
						}
					}
					else {
						W = s;
						H = s; // axis==2, u=x, v=y at fixed z=i
						for(int v = 0; v < H; v++)
						for(int u = 0; u < W; u++) {
							int x = u, y = v, z = i;
							ushort a = getTypeClamped(x, y, z);
							ushort b = getTypeClamped(x, y, z + sgn);
							mask[u][v] = (short)(a != 0 && (b == 0 || b != a) ? a : 0);
						}
					}

					// Greedy merge rectangles of same type
					for(int v = 0; v < s; v++) {
						for(int u = 0; u < s; u++) {
							short type = mask[u][v];
							if (type == 0) continue;
							int w = 1;
							while (w < maxFaceSize && u + w < s && mask[u + w][v] == type) w++;
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
							// Clear mask
							for(int dv = 0; dv < h; dv++)
							for(int du = 0; du < w; du++)
								mask[u + du][v + dv] = 0;

							// Emit face
							if (axis == 0) {
								// x fixed = i, u=z, v=y
								positions.Add(new float3(i, v, u));
								dirs.Add((byte)(sgn > 0 ? 0 : 1));
								sizes.Add(new int2(w, h));
							}
							else if (axis == 1) {
								// y fixed = i, u=x, v=z
								positions.Add(new float3(u, i, v));
								dirs.Add((byte)(sgn > 0 ? 2 : 3));
								sizes.Add(new int2(w, h));
							}
							else {
								// z fixed = i, u=x, v=y
								positions.Add(new float3(u, v, i));
								dirs.Add((byte)(sgn > 0 ? 4 : 5));
								sizes.Add(new int2(w, h));
							}
						}
					}
				}
			}

			// X axis
			greedySlice(0, +1);
			greedySlice(0, -1);
			// Y axis
			greedySlice(1, +1);
			greedySlice(1, -1);
			// Z axis
			greedySlice(2, +1);
			greedySlice(2, -1);
		}

		[BurstCompile] //Disable this if you need to debug stuff
		struct BuildFacesJob : IJobParallelFor {
			public Mesh.MeshData OutputMesh;
			[ReadOnly] public NativeArray<float3> FacePositions; // origin cell (x,y,z)
			[ReadOnly] public NativeArray<byte> FaceDirs; // 0..5 as before
			[ReadOnly] public NativeArray<int2> FaceSizes; // size along the two axes on the face (u = first, v = second)

			public void Execute(int index) {
				int vStart = index * 4;
				int tStart = index * 6;
				float3 o = FacePositions[index];
				byte dir = FaceDirs[index];
				int2 size = FaceSizes[index];
				int su = size.x; // width along the first axis of the face
				int sv = size.y; // height along the second axis

				var outputVerts = OutputMesh.GetVertexData<Vector3>();
				var outputNormals = OutputMesh.GetVertexData<Vector3>(stream: 1);
				var outputUVs = OutputMesh.GetVertexData<Vector2>(stream: 2);
				var outputTris = OutputMesh.GetIndexData<int>();

				Vector3 v0, v1, v2, v3;
				switch (dir) {
					case 0: // +X, plane at x + 0.5, u=z, v=y
						v0 = new Vector3(o.x + 0.5f, o.y - 0.5f, o.z - 0.5f);
						v1 = new Vector3(o.x + 0.5f, o.y - 0.5f, o.z + su - 0.5f);
						v2 = new Vector3(o.x + 0.5f, o.y + sv - 0.5f, o.z + su - 0.5f);
						v3 = new Vector3(o.x + 0.5f, o.y + sv - 0.5f, o.z - 0.5f);
						break;

					case 1: // -X, plane at x - 0.5, u=z, v=y
						v0 = new Vector3(o.x - 0.5f, o.y - 0.5f, o.z + su - 0.5f);
						v1 = new Vector3(o.x - 0.5f, o.y - 0.5f, o.z - 0.5f);
						v2 = new Vector3(o.x - 0.5f, o.y + sv - 0.5f, o.z - 0.5f);
						v3 = new Vector3(o.x - 0.5f, o.y + sv - 0.5f, o.z + su - 0.5f);
						break;

					case 2: // +Y, plane at y + 0.5, u=x, v=z
						v0 = new Vector3(o.x - 0.5f, o.y + 0.5f, o.z - 0.5f);
						v1 = new Vector3(o.x + su - 0.5f, o.y + 0.5f, o.z - 0.5f);
						v2 = new Vector3(o.x + su - 0.5f, o.y + 0.5f, o.z + sv - 0.5f);
						v3 = new Vector3(o.x - 0.5f, o.y + 0.5f, o.z + sv - 0.5f);
						break;

					case 3: // -Y, plane at y - 0.5, u=x, v=z
						v0 = new Vector3(o.x - 0.5f, o.y - 0.5f, o.z + sv - 0.5f);
						v1 = new Vector3(o.x + su - 0.5f, o.y - 0.5f, o.z + sv - 0.5f);
						v2 = new Vector3(o.x + su - 0.5f, o.y - 0.5f, o.z - 0.5f);
						v3 = new Vector3(o.x - 0.5f, o.y - 0.5f, o.z - 0.5f);
						break;

					case 4: // +Z, plane at z + 0.5, u=x, v=y
						// Match ElementInfo winding so faces point outward
						v0 = new Vector3(o.x - 0.5f, o.y - 0.5f, o.z + 0.5f); // bottom-left
						v1 = new Vector3(o.x - 0.5f, o.y - 0.5f + sv, o.z + 0.5f); // top-left
						v2 = new Vector3(o.x - 0.5f + su, o.y - 0.5f + sv, o.z + 0.5f); // top-right
						v3 = new Vector3(o.x - 0.5f + su, o.y - 0.5f, o.z + 0.5f); // bottom-right
						break;

					default: // 5: -Z, plane at z - 0.5, u=x, v=y
						// Match ElementInfo winding so faces point outward
						v0 = new Vector3(o.x - 0.5f + su, o.y - 0.5f, o.z - 0.5f); // bottom-right
						v1 = new Vector3(o.x - 0.5f + su, o.y - 0.5f + sv, o.z - 0.5f); // top-right
						v2 = new Vector3(o.x - 0.5f, o.y - 0.5f + sv, o.z - 0.5f); // top-left
						v3 = new Vector3(o.x - 0.5f, o.y - 0.5f, o.z - 0.5f); // bottom-left
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
				// Stretch UVs 0..1 across the merged face
				outputUVs[vStart + 0] = new Vector2(0, 0);
				outputUVs[vStart + 1] = new Vector2(1, 0);
				outputUVs[vStart + 2] = new Vector2(1, 1);
				outputUVs[vStart + 3] = new Vector2(0, 1);
				// Triangles
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
					_ => new float3(0, 0, -1)
				};
			}

			static Vector2 GetFaceUV(int corner) {
				return corner switch {
					0 => new Vector2(0, 0),
					1 => new Vector2(1, 0),
					2 => new Vector2(1, 1),
					_ => new Vector2(0, 1)
				};
			}
		}

		public string Report(StatsDisplay.DisplayMode displayMode, float deltaTime) {
			return !displayMode.HasFlag(StatsDisplay.DisplayMode.RenderStats) ? "" : $"ChunkBuilder:\n\t{_chunkCount} chunks\n\t{_blockCount} blocks\n\t{_vertexCount} vertices\n\t{_triangleCount} triangles\n";
		}

		public void ClearLastReport() {
			_chunkCount = 0;
			_vertexCount = 0;
			_triangleCount = 0;
			_blockCount = 0;
		}

		void Start() {
			FindFirstObjectByType<StatsDisplay>()?.Reporters.Add(this);
		}
	}
}