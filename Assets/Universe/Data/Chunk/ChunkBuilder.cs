using System;
using System.Collections.Generic;
using Dev;
using Element;
using Unity.Burst;
using Unity.Collections;
using Unity.Jobs;
using Unity.Mathematics;
using UnityEngine;
using UnityEngine.Rendering;
using static Universe.Data.Chunk.Chunk;

namespace Universe.Data.Chunk {
	public class ChunkBuilder : MonoBehaviour, StatsDisplay.IStatsDisplayReporter {

		static int _chunkCount;
		static int _blockCount;
		static int _vertexCount;
		static int _triangleCount;

		public static void BuildChunk(IChunkData chunk, MeshFilter meshFilter, MeshRenderer renderer) {

			var facePositions = new List<float3>();
			var faceDirs = new List<byte>();

			int totalBlocks = ChunkSize * ChunkSize * ChunkSize;
			for (int i = 0; i < totalBlocks; i++) {
				var type = chunk.GetBlockType(i);
				if (type == 0) continue;
				_blockCount ++;
				var posV3 = chunk.GetBlockPosition(i);
				int x = (int)posV3.x; int y = (int)posV3.y; int z = (int)posV3.z;
				// Check 6 neighbors
				// +X neighbor
				if (x + 1 >= ChunkSize || chunk.GetBlockType(chunk.GetBlockIndex(new Vector3(x + 1, y, z))) == 0) {
					facePositions.Add(new float3(x,y,z)); faceDirs.Add(0);
				}
				// -X
				if (x - 1 < 0 || chunk.GetBlockType(chunk.GetBlockIndex(new Vector3(x - 1, y, z))) == 0) {
					facePositions.Add(new float3(x,y,z)); faceDirs.Add(1);
				}
				// +Y
				if (y + 1 >= ChunkSize || chunk.GetBlockType(chunk.GetBlockIndex(new Vector3(x, y + 1, z))) == 0) {
					facePositions.Add(new float3(x,y,z)); faceDirs.Add(2);
				}
				// -Y
				if (y - 1 < 0 || chunk.GetBlockType(chunk.GetBlockIndex(new Vector3(x, y - 1, z))) == 0) {
					facePositions.Add(new float3(x,y,z)); faceDirs.Add(3);
				}
				// +Z
				if (z + 1 >= ChunkSize || chunk.GetBlockType(chunk.GetBlockIndex(new Vector3(x, y, z + 1))) == 0) {
					facePositions.Add(new float3(x,y,z)); faceDirs.Add(4);
				}
				// -Z
				if (z - 1 < 0 || chunk.GetBlockType(chunk.GetBlockIndex(new Vector3(x, y, z - 1))) == 0) {
					facePositions.Add(new float3(x,y,z)); faceDirs.Add(5);
				}
			}

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
				FaceDirs = new NativeArray<byte>(faceDirs.Count, Allocator.TempJob, NativeArrayOptions.UninitializedMemory)
			};
			for (int i = 0; i < facePositions.Count; i++) {
				buildJob.FacePositions[i] = facePositions[i];
				buildJob.FaceDirs[i] = faceDirs[i];
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
			newMesh.RecalculateBounds();
			meshFilter.mesh = newMesh;
			renderer.material = Resources.Load<Material>("ChunkMaterial");

			// Update simple stats
			_chunkCount++;
			_vertexCount += totalVertexCount;
			_triangleCount += totalIndexCount / 3;
		}

		// [BurstCompile]
		struct BuildFacesJob : IJobParallelFor {
			public Mesh.MeshData OutputMesh;
			[ReadOnly] public NativeArray<float3> FacePositions;
			[ReadOnly] public NativeArray<byte> FaceDirs;
			
			public void Execute(int index) {
				int vStart = index * 4;
				int tStart = index * 6;
				float3 basePos = FacePositions[index];
				byte dir = FaceDirs[index];
				
				var outputVerts = OutputMesh.GetVertexData<Vector3>();
				var outputNormals = OutputMesh.GetVertexData<Vector3>(stream: 1);
				var outputUVs = OutputMesh.GetVertexData<Vector2>(stream: 2);
				var outputTris = OutputMesh.GetIndexData<int>();
				
				// Write quad vertices based on face dir
				for (int corner = 0; corner < 4; corner++) {
					float3 offset = GetFaceVertex(dir, corner);
					float3 v = basePos + offset;
					outputVerts[vStart + corner] = new Vector3(v.x, v.y, v.z);
					float3 n = GetFaceNormal(dir);
					outputNormals[vStart + corner] = new Vector3(n.x, n.y, n.z);
					outputUVs[vStart + corner] = GetFaceUV(corner);
				}
				// Triangles (0,2,1, 0,3,2) with base vStart
				outputTris[tStart + 0] = vStart + 0;
				outputTris[tStart + 1] = vStart + 2;
				outputTris[tStart + 2] = vStart + 1;
				outputTris[tStart + 3] = vStart + 0;
				outputTris[tStart + 4] = vStart + 3;
				outputTris[tStart + 5] = vStart + 2;
			}
			
			static float3 GetFaceNormal(byte dir) {
				switch (dir) {
					case 0: return new float3(1,0,0);
					case 1: return new float3(-1,0,0);
					case 2: return new float3(0,1,0);
					case 3: return new float3(0,-1,0);
					case 4: return new float3(0,0,1);
					default: return new float3(0,0,-1);
				}
			}
			
			static Vector2 GetFaceUV(int corner) {
				switch (corner) {
					case 0: return new Vector2(0,0);
					case 1: return new Vector2(1,0);
					case 2: return new Vector2(1,1);
					default: return new Vector2(0,1);
				}
			}
			
			static float3 GetFaceVertex(byte dir, int corner) {
				// Returns local-space vertex offset for the given face and corner
				// Matching ElementInfo layout
				switch (dir) {
					case 0: // +X
						return corner switch {
							0 => new float3(0.5f,-0.5f,-0.5f),
							1 => new float3(0.5f,-0.5f,0.5f),
							2 => new float3(0.5f,0.5f,0.5f),
							_ => new float3(0.5f,0.5f,-0.5f)
						};
					case 1: // -X
						return corner switch {
							0 => new float3(-0.5f,-0.5f,0.5f),
							1 => new float3(-0.5f,-0.5f,-0.5f),
							2 => new float3(-0.5f,0.5f,-0.5f),
							_ => new float3(-0.5f,0.5f,0.5f)
						};
					case 2: // +Y
						return corner switch {
							0 => new float3(-0.5f,0.5f,-0.5f),
							1 => new float3(0.5f,0.5f,-0.5f),
							2 => new float3(0.5f,0.5f,0.5f),
							_ => new float3(-0.5f,0.5f,0.5f)
						};
					case 3: // -Y
						return corner switch {
							0 => new float3(-0.5f,-0.5f,0.5f),
							1 => new float3(0.5f,-0.5f,0.5f),
							2 => new float3(0.5f,-0.5f,-0.5f),
							_ => new float3(-0.5f,-0.5f,-0.5f)
						};
					case 4: // +Z
						return corner switch {
							0 => new float3(-0.5f,-0.5f,0.5f),
							1 => new float3(-0.5f,0.5f,0.5f),
							2 => new float3(0.5f,0.5f,0.5f),
							_ => new float3(0.5f,-0.5f,0.5f)
						};
					default: // 5: -Z
						return corner switch {
							0 => new float3(0.5f,-0.5f,-0.5f),
							1 => new float3(0.5f,0.5f,-0.5f),
							2 => new float3(-0.5f,0.5f,-0.5f),
							_ => new float3(-0.5f,-0.5f,-0.5f)
						};
				}
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