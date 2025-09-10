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
		static int _vertexCount;
		static int _triangleCount;

		public static void BuildChunk(IChunkData chunk, MeshFilter meshFilter, MeshRenderer renderer) {
			var buildJob = new ProcessMeshDataJob();

			var inputMeshes = new List<Mesh>();
			var vertexStarts = new List<int>();
			var triStarts = new List<int>();
			var positions = new List<float3>();
			int totalVertexCount = 0;
			int totalIndexCount = 0;

			// Gather meshes for all non-empty blocks and build compact start-offset arrays
			int totalBlocks = ChunkSize * ChunkSize * ChunkSize;
			for (int i = 0; i < totalBlocks; i++) {
				// A block is considered present if its TYPE is non-zero
				var blockType = chunk.GetBlockType(i);
				if (blockType == 0) continue;
				var mesh = ElementInfo.GetMesh(blockType);
				inputMeshes.Add(mesh);
				vertexStarts.Add(totalVertexCount);
				triStarts.Add(totalIndexCount);
				var pos = chunk.GetBlockPosition(i);
				positions.Add(new float3(pos.x, pos.y, pos.z));
				totalVertexCount += mesh.vertexCount;
				totalIndexCount += mesh.GetSubMesh(0).indexCount;
			}

			// Early-out: no geometry
			if (inputMeshes.Count == 0) {
				var empty = new Mesh();
				empty.SetVertices(new List<Vector3>(0));
				empty.SetIndices(Array.Empty<int>(), MeshTopology.Triangles, 0, false);
				empty.RecalculateBounds();
				meshFilter.mesh = empty;
				renderer.material = Resources.Load<Material>("ChunkMaterial");
				return;
			}

			buildJob.VertexStart = new NativeArray<int>(vertexStarts.Count, Allocator.TempJob, NativeArrayOptions.UninitializedMemory);
			buildJob.TriStart = new NativeArray<int>(triStarts.Count, Allocator.TempJob, NativeArrayOptions.UninitializedMemory);
			buildJob.Positions = new NativeArray<float3>(positions.Count, Allocator.TempJob, NativeArrayOptions.UninitializedMemory);
			for (int i = 0; i < vertexStarts.Count; i++) {
				buildJob.VertexStart[i] = vertexStarts[i];
				buildJob.TriStart[i] = triStarts[i];
				buildJob.Positions[i] = positions[i];
			}

			buildJob.MeshData = Mesh.AcquireReadOnlyMeshData(inputMeshes);
			var outputMeshData = Mesh.AllocateWritableMeshData(1);
			buildJob.OutputMesh = outputMeshData[0];
			buildJob.OutputMesh.SetIndexBufferParams(totalIndexCount, IndexFormat.UInt32);
			buildJob.OutputMesh.SetVertexBufferParams(
				totalVertexCount,
				new VertexAttributeDescriptor(VertexAttribute.Position, stream: 0),
				new VertexAttributeDescriptor(VertexAttribute.Normal, stream: 1),
				new VertexAttributeDescriptor(VertexAttribute.TexCoord0, dimension: 2, stream: 2)
			);

			var handle = buildJob.Schedule(inputMeshes.Count, 4);
			var newMesh = new Mesh();
			var sm = new SubMeshDescriptor(0, totalIndexCount) { firstVertex = 0, vertexCount = totalVertexCount };
			handle.Complete();

			buildJob.OutputMesh.subMeshCount = 1;
			buildJob.OutputMesh.SetSubMesh(0, sm);
			Mesh.ApplyAndDisposeWritableMeshData(outputMeshData, new[] { newMesh });
			buildJob.MeshData.Dispose();
			buildJob.VertexStart.Dispose();
			buildJob.TriStart.Dispose();
			buildJob.Positions.Dispose();
			newMesh.RecalculateBounds();
			meshFilter.mesh = newMesh;
			renderer.material = Resources.Load<Material>("ChunkMaterial");

			// Update simple stats
			_chunkCount++;
			_vertexCount += totalVertexCount;
			_triangleCount += totalIndexCount / 3;
		}

		// [BurstCompile]
		struct ProcessMeshDataJob : IJobParallelFor {
			[ReadOnly]
			public Mesh.MeshDataArray MeshData;
			public Mesh.MeshData OutputMesh;
			public NativeArray<int> VertexStart;
			public NativeArray<int> TriStart;
			public NativeArray<float3> Positions;
			
			public void Execute(int index) {
				var data = MeshData[index];
				var vCount = data.vertexCount;
				var vStart = VertexStart[index];
				var offset = Positions[index];
				
				var verts = new NativeArray<float3>(vCount, Allocator.Temp, NativeArrayOptions.UninitializedMemory);
				data.GetVertices(verts.Reinterpret<Vector3>());
				
				// Normals may be missing on some meshes
				bool hasNormals = data.HasVertexAttribute(VertexAttribute.Normal);
				NativeArray<float3> normals = default;
				if (hasNormals) {
					normals = new NativeArray<float3>(vCount, Allocator.Temp, NativeArrayOptions.UninitializedMemory);
					data.GetNormals(normals.Reinterpret<Vector3>());
				}
				
				// UV0 may be missing on some meshes
				bool hasUV0 = data.HasVertexAttribute(VertexAttribute.TexCoord0);
				NativeArray<float2> uvs = default;
				if (hasUV0) {
					uvs = new NativeArray<float2>(vCount, Allocator.Temp, NativeArrayOptions.UninitializedMemory);
					data.GetUVs(0, uvs.Reinterpret<Vector2>());
				}
				
				var outputVerts = OutputMesh.GetVertexData<Vector3>();
				var outputNormals = OutputMesh.GetVertexData<Vector3>(stream: 1);
				var outputUVs = OutputMesh.GetVertexData<Vector2>(stream: 2);
				
				for(int i = 0; i < vCount; i++) {
					var v = verts[i] + offset;
					outputVerts[i + vStart] = new Vector3(v.x, v.y, v.z);
					outputNormals[i + vStart] = hasNormals ? normals[i] : Vector3.up;
					outputUVs[i + vStart] = hasUV0 ? uvs[i] : Vector2.zero;
				}
				
				verts.Dispose();
				if (normals.IsCreated) normals.Dispose();
				if (uvs.IsCreated) uvs.Dispose();
				
				var tStart = TriStart[index];
				var subMesh = data.GetSubMesh(0);
				var tCount = subMesh.indexCount;
				var inputIndexStart = subMesh.indexStart;
				var baseVertex = subMesh.baseVertex;
				var outputTris = OutputMesh.GetIndexData<int>();
				if (data.indexFormat == IndexFormat.UInt16) {
					var tris = data.GetIndexData<ushort>();
					for(int i = 0; i < tCount; ++i) {
						int idx = tris[i + inputIndexStart] + baseVertex;
						outputTris[i + tStart] = vStart + idx;
					}
				}
				else {
					var tris = data.GetIndexData<int>();
					for(int i = 0; i < tCount; ++i) {
						int idx = tris[i + inputIndexStart] + baseVertex;
						outputTris[i + tStart] = vStart + idx;
					}
				}
			}
		}

		public string Report(StatsDisplay.DisplayMode displayMode, float deltaTime) {
			return !displayMode.HasFlag(StatsDisplay.DisplayMode.RenderStats) ? "" : $"ChunkBuilder:\n\t{_chunkCount} chunks\n\t{_vertexCount} vertices\n\t{_triangleCount} triangles\n";
		}

		public void ClearLastReport() {
			_chunkCount = 0;
			_vertexCount = 0;
			_triangleCount = 0;
		}

		void Start() {
			FindFirstObjectByType<StatsDisplay>()?.Reporters.Add(this);
		}
	}
}