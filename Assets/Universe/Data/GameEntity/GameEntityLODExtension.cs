using UnityEngine;
using Universe.Data.Chunk;

namespace Universe.Data.GameEntity {
	/// <summary>
	/// Extension methods for GameEntity to support LOD functionality
	/// </summary>
	public static class GameEntityLODExtension {

		/// <summary>
		/// Rebuild mesh with a specific LOD level
		/// </summary>
		public static void RebuildMeshAtLOD(this GameEntity entity, int lodLevel) {
			if (entity.Chunks == null) return;

			var combine = new CombineInstance[entity.Chunks.Length];
			entity.blockCount = 0;
			entity.chunkCount = entity.Chunks.Length;
			entity.triangleCount = 0;
			entity.vertexCount = 0;

			for(var i = 0; i < entity.Chunks.Length; i++) {
				var chunk = entity.Chunks[i];
				var chunkPos = entity.GetChunkPosition(i);

				// Use ChunkBuilder with forced LOD level
				var result = ChunkBuilder.BuildChunkAtLOD(chunk.Data, chunkPos, lodLevel);

				combine[i].mesh = result.mesh;
				combine[i].transform = Matrix4x4.TRS(chunkPos, Quaternion.identity, Vector3.one);
				entity.blockCount += result.blockCount;
				entity.triangleCount += result.triangleCount;
				entity.vertexCount += result.vertexCount;
			}

			ApplyMeshToEntity(entity, combine);
		}

		/// <summary>
		/// Get detailed build information for the entity
		/// </summary>
		public static ChunkBuildInfo GetBuildInfo(this GameEntity entity) {
			if (entity.Chunks == null) {
				return new ChunkBuildInfo {
					totalChunks = 0,
					totalBlocks = 0,
					estimatedVertices = 0,
					estimatedTriangles = 0
				};
			}

			int totalBlocks = 0;
			for(int i = 0; i < entity.Chunks.Length; i++) {
				var chunk = entity.Chunks[i];
				if (chunk.Data != null) {
					// Count non-air blocks
					int chunkSize = Universe.Data.Chunk.Chunk.ChunkSize;
					int totalVoxels = chunkSize * chunkSize * chunkSize;

					for(int j = 0; j < totalVoxels; j++) {
						if (chunk.Data.GetBlockType(j) != 0) {
							totalBlocks++;
						}
					}
				}
			}

			return new ChunkBuildInfo {
				totalChunks = entity.Chunks.Length,
				totalBlocks = totalBlocks,
				estimatedVertices = totalBlocks * 24, // 6 faces * 4 vertices max
				estimatedTriangles = totalBlocks * 12 // 6 faces * 2 triangles max
			};
		}

		/// <summary>
		/// Get LOD performance estimate for different LOD levels
		/// </summary>
		public static LODPerformanceEstimate GetLODEstimate(this GameEntity entity, int lodLevel) {
			var buildInfo = entity.GetBuildInfo();
			int estimatedFaces = ChunkBuilder.EstimateFaceCountForLOD(lodLevel, buildInfo.totalBlocks);

			return new LODPerformanceEstimate {
				lodLevel = lodLevel,
				estimatedFaces = estimatedFaces,
				estimatedVertices = estimatedFaces * 4,
				estimatedTriangles = estimatedFaces * 2,
				reductionRatio = buildInfo.totalBlocks > 0 ? (float)estimatedFaces / (buildInfo.totalBlocks * 6) : 0f
			};
		}

		/// <summary>
		/// Rebuild mesh with performance monitoring
		/// </summary>
		public static ChunkBuildResult RebuildMeshWithStats(this GameEntity entity) {
			var startTime = Time.realtimeSinceStartup;

			if (entity.Chunks == null) {
				return new ChunkBuildResult {
					mesh = new Mesh(),
					vertexCount = 0,
					triangleCount = 0,
					blockCount = 0,
					faceCount = 0,
					lodLevel = 0,
					buildTime = 0f
				};
			}

			var combine = new CombineInstance[entity.Chunks.Length];
			entity.blockCount = 0;
			entity.chunkCount = entity.Chunks.Length;
			entity.triangleCount = 0;
			entity.vertexCount = 0;
			int totalFaces = 0;

			for(var i = 0; i < entity.Chunks.Length; i++) {
				var chunk = entity.Chunks[i];
				var chunkPos = entity.GetChunkPosition(i);
				var result = ChunkBuilder.BuildChunk(chunk.Data, chunkPos);

				combine[i].mesh = result.mesh;
				combine[i].transform = Matrix4x4.TRS(chunkPos, Quaternion.identity, Vector3.one);
				entity.blockCount += result.blockCount;
				entity.triangleCount += result.triangleCount;
				entity.vertexCount += result.vertexCount;
				totalFaces += result.faceCount;
			}

			ApplyMeshToEntity(entity, combine);
			entity.isDirty = false;

			var buildTime = Time.realtimeSinceStartup - startTime;

			return new ChunkBuildResult {
				mesh = entity.GetComponent<MeshFilter>()?.sharedMesh,
				vertexCount = entity.vertexCount,
				triangleCount = entity.triangleCount,
				blockCount = entity.blockCount,
				faceCount = totalFaces,
				lodLevel = -1, // Combined result from multiple chunks
				buildTime = buildTime
			};
		}

		private static void ApplyMeshToEntity(GameEntity entity, CombineInstance[] combine) {
			var meshFilter = entity.GetComponent<MeshFilter>();
			if (meshFilter == null) {
				meshFilter = entity.gameObject.AddComponent<MeshFilter>();
			}

			var meshRenderer = entity.GetComponent<MeshRenderer>();
			if (meshRenderer == null) {
				meshRenderer = entity.gameObject.AddComponent<MeshRenderer>();
				meshRenderer.material = Resources.Load<Material>("ChunkMaterial");
			}

			var mesh = new Mesh();
			mesh.CombineMeshes(combine, true);
			meshFilter.mesh = mesh;
		}
	}

	/// <summary>
	/// Information about chunk build statistics
	/// </summary>
	public struct ChunkBuildInfo {
		public int totalChunks;
		public int totalBlocks;
		public int estimatedVertices;
		public int estimatedTriangles;
	}

	/// <summary>
	/// Performance estimate for a specific LOD level
	/// </summary>
	public struct LODPerformanceEstimate {
		public int lodLevel;
		public int estimatedFaces;
		public int estimatedVertices;
		public int estimatedTriangles;
		public float reductionRatio; // 0-1, how much geometry is reduced
	}
}