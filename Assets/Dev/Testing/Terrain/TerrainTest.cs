using UnityEngine;
using Universe.Data.Chunk;
using Universe.Data.GameEntity;

namespace Dev.Testing.Terrain {
	public class TerrainTest : MonoBehaviour {
		[Header("Generation Settings")]
		public Vector3Int chunkDimensions = new(3, 3, 3);
		public float noiseFrequency = 0.035f;
		public float surfaceThickness = 10f;
		public float shapeDistortion = 10f;

		void Start() {
			GameEntity entity = new GameObject("TestAsteroid").AddComponent<Asteroid>();
			entity.LoadDataFromDB(new GameEntity.GameEntityData {
				Type = GameEntityType.Asteroid,
				FactionID = 0,
				SectorID = 0,
				Name = "TestAsteroid",
			});
			entity.gameObject.transform.position = new Vector3(0, 0, 0);
			entity.gameObject.transform.rotation = Quaternion.identity;
			entity.gameObject.SetActive(true);

			entity.chunkDimensions = chunkDimensions;

			var chunksTotal = chunkDimensions.x * chunkDimensions.y * chunkDimensions.z;
			entity.TotalChunks = chunksTotal;
			entity.LoadInSector(0);

			// Provide cross-chunk block resolver so faces between chunks are culled
			ChunkBuilder.ExternalBlockResolver = (currentChunk, lx, ly, lz) => {
				int s = Chunk.ChunkSize;
				var cd = (ChunkDataV8)currentChunk;
				int ci = (int)cd.Index;
				int cx = ci % chunkDimensions.x;
				int cy = (ci / chunkDimensions.x) % chunkDimensions.y;
				int cz = ci / (chunkDimensions.x * chunkDimensions.y);
				// Adjust coordinates and move to neighbor chunk if needed
				if (lx < 0) {
					cx--;
					lx = s - 1;
				}
				else if (lx >= s) {
					cx++;
					lx = 0;
				}
				if (ly < 0) {
					cy--;
					ly = s - 1;
				}
				else if (ly >= s) {
					cy++;
					ly = 0;
				}
				if (lz < 0) {
					cz--;
					lz = s - 1;
				}
				else if (lz >= s) {
					cz++;
					lz = 0;
				}
				// If outside world bounds, treat as air
				if (cx < 0 || cy < 0 || cz < 0 || cx >= chunkDimensions.x || cy >= chunkDimensions.y || cz >= chunkDimensions.z)
					return 0;
				int neighborIndex = cx + cy * chunkDimensions.x + cz * chunkDimensions.x * chunkDimensions.y;
				var neighborChunk = entity.Chunks[neighborIndex].Data;
				long bi = neighborChunk.GetBlockIndex(new Vector3(lx, ly, lz));
				return neighborChunk.GetBlockType(bi);
			};

			// First pass: create chunks and assign fully populated data, but do NOT rebuild yet.
			for(var i = 0; i < chunksTotal; i++) {
				var chunkX = i % chunkDimensions.x;
				var chunkY = (i / chunkDimensions.x) % chunkDimensions.y;
				var chunkZ = i / (chunkDimensions.x * chunkDimensions.y);

				var chunkPos = new Vector3(chunkX * Chunk.ChunkSize, chunkY * Chunk.ChunkSize, chunkZ * Chunk.ChunkSize);

				IChunkData chunkData;
				unsafe {
					chunkData = new ChunkDataV8(i, ChunkAllocator.Allocate(Chunk.ChunkSize));
					var radius = (Mathf.Min(chunkDimensions.x, Mathf.Min(chunkDimensions.y, chunkDimensions.z)) * Chunk.ChunkSize) / 2 - 10;
					var centerX = (chunkDimensions.x * Chunk.ChunkSize) / 2;
					var centerY = (chunkDimensions.y * Chunk.ChunkSize) / 2;
					var centerZ = (chunkDimensions.z * Chunk.ChunkSize) / 2;
					for(var x = 0; x < Chunk.ChunkSize; x++) {
						for(var y = 0; y < Chunk.ChunkSize; y++) {
							for(var z = 0; z < Chunk.ChunkSize; z++) {
								var blockIndex = x + y * Chunk.ChunkSize + z * Chunk.ChunkSize * Chunk.ChunkSize;
								var worldX = chunkX * Chunk.ChunkSize + x;
								var worldY = chunkY * Chunk.ChunkSize + y;
								var worldZ = chunkZ * Chunk.ChunkSize + z;

								var distortion = Perlin.Noise(worldX * noiseFrequency, worldY * noiseFrequency, worldZ * noiseFrequency) * shapeDistortion;
								var distortedRadius = radius + distortion;

								var distance = Mathf.Sqrt(Mathf.Pow(worldX - centerX, 2) + Mathf.Pow(worldY - centerY, 2) + Mathf.Pow(worldZ - centerZ, 2));
								if (distance <= distortedRadius) {
									if (distance > distortedRadius - surfaceThickness) {
										var noise = Perlin.Noise(worldX * noiseFrequency * 2, worldY * noiseFrequency * 2, worldZ * noiseFrequency * 2);
										chunkData.SetBlockType(blockIndex, distance <= distortedRadius - noise * 5 ? (short)1 : (short)0);
									}
									else {
										chunkData.SetBlockType(blockIndex, 1);
									}
								}
								else {
									chunkData.SetBlockType(blockIndex, 0);
								}
							}
						}
					}
				}

				entity.Chunks[i] = new Chunk { Data = chunkData };
				// Defer rebuild until all neighbor chunk data is assigned, to ensure cross-chunk culling works.
			}

			// Second pass: rebuild all chunks now that neighbor data is available.
			var chunkGenQueue = FindObjectOfType<ChunkGenerationQueue>();
			chunkGenQueue.RequestMeshRebuild(entity);
			for(var i = 0; i < chunksTotal; i++) {
				unsafe {
					ChunkAllocator.Free(((ChunkDataV8)entity.Chunks[i].Data).Data, Chunk.ChunkSize);
				}
			}
		}
	}
}