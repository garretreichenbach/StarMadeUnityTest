using UnityEngine;
using Universe.Data.Chunk;
using Universe.Data.GameEntity;

namespace Dev.Testing.Terrain {
	public class TerrainTest : MonoBehaviour {
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

			var chunkDimensionX = 50;
			var chunkDimensionY = 1;
			var chunkDimensionZ = 50;
			var chunksTotal = chunkDimensionX * chunkDimensionY * chunkDimensionZ;
			ChunkBuffer buffer = entity.gameObject.AddComponent<ChunkBuffer>().Create(chunksTotal);

			for (var i = 0; i < chunksTotal; i++) {
				var chunkX = i % chunkDimensionX;
				var chunkY = (i / chunkDimensionX) % chunkDimensionY;
				var chunkZ = i / (chunkDimensionX * chunkDimensionY);
				
				var chunkPos = new Vector3(chunkX * Chunk.ChunkSize, chunkY * Chunk.ChunkSize, chunkZ * Chunk.ChunkSize);
				
				IChunkData chunkData;
				unsafe {
					chunkData = new ChunkDataV8(i, ChunkAllocator.Allocate(Chunk.ChunkSize));
					for (var x = 0; x < Chunk.ChunkSize; x++) {
						for (var y = 0; y < Chunk.ChunkSize; y++) {
							for (var z = 0; z < Chunk.ChunkSize; z++) {
								var blockIndex = x + y * Chunk.ChunkSize + z * Chunk.ChunkSize * Chunk.ChunkSize;
								chunkData.SetBlockType(blockIndex, 1);
							}
						}
					}
				}

				var chunkGo = new GameObject("Chunk" + i);
				chunkGo.transform.parent = entity.transform;
				chunkGo.transform.position = chunkPos;
				var chunk = chunkGo.AddComponent<Chunk>();
				chunk.Data = chunkData;
				buffer.SetChunkData(i, chunk);
				chunk.Rebuild();
				unsafe {
					ChunkAllocator.Free(((ChunkDataV8)chunkData).Data);
				}
			}
		}
	}
}