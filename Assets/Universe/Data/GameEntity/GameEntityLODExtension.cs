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

				// Calculate chunk coordinates within the entity
				int cx = i % entity.chunkDimensions.x;
				int cy = (i / entity.chunkDimensions.x) % entity.chunkDimensions.y;
				int cz = i / (entity.chunkDimensions.x * entity.chunkDimensions.y);

				IChunkData[] neighbors = new IChunkData[6]; // +X, -X, +Y, -Y, +Z, -Z

				// Helper to get chunk data at a relative coordinate
				System.Func<int, int, int, IChunkData> getChunkDataAtRelativeCoord = (relX, relY, relZ) => {
					int absX = cx + relX;
					int absY = cy + relY;
					int absZ = cz + relZ;

					if (absX >= 0 &&
					    absX < entity.chunkDimensions.x &&
					    absY >= 0 &&
					    absY < entity.chunkDimensions.y &&
					    absZ >= 0 &&
					    absZ < entity.chunkDimensions.z) {
						int neighborIndex = absX + absY * entity.chunkDimensions.x + absZ * entity.chunkDimensions.x * entity.chunkDimensions.y;
						return entity.Chunks[neighborIndex].Data;
					}
					return null; // Outside entity bounds, treat as air
				};

				neighbors[0] = getChunkDataAtRelativeCoord(1, 0, 0); // +X
				neighbors[1] = getChunkDataAtRelativeCoord(-1, 0, 0); // -X
				neighbors[2] = getChunkDataAtRelativeCoord(0, 1, 0); // +Y
				neighbors[3] = getChunkDataAtRelativeCoord(0, -1, 0); // -Y
				neighbors[4] = getChunkDataAtRelativeCoord(0, 0, 1); // +Z
				neighbors[5] = getChunkDataAtRelativeCoord(0, 0, -1); // -Z

				// Use ChunkBuilder with forced LOD level and neighbors
				var result = ChunkBuilder.BuildChunkAtLOD(chunk.Data, chunkPos, neighbors, lodLevel);

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

			// Destroy the old mesh to prevent memory leaks and visual artifacts
			if (meshFilter.sharedMesh != null) {
#if UNITY_EDITOR
				if (!Application.isPlaying) {
					// In editor, use DestroyImmediate for immediate cleanup
					Object.DestroyImmediate(meshFilter.sharedMesh);
				} else {
					// In play mode, use Destroy
					Object.Destroy(meshFilter.sharedMesh);
				}
#else
				// In builds, always use Destroy
				UnityEngine.Object.Destroy(meshFilter.sharedMesh);
#endif
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