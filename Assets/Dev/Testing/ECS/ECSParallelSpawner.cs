using UnityEngine;
using Universe.Data.Chunk;
using Universe.Data.GameEntity;

namespace Dev.Testing.ECS {
	/**
	 * ECS system test for spawning a large number of objects in parallel.
	 */
	public class ParallelSpawner : MonoBehaviour {

		void Start() {
			GameEntity entity = new GameObject("TestShip").AddComponent<Ship>();
			entity.LoadDataFromDB(new GameEntity.GameEntityData {
				Type = GameEntityType.Ship,
				FactionID = 0,
				SectorID = 0,
				Name = "TestShip",
			});
			entity.gameObject.transform.position = new Vector3(0, 0, 0);
			entity.gameObject.transform.rotation = Quaternion.identity;
			entity.gameObject.SetActive(true);

			ChunkBuffer buffer = entity.gameObject.AddComponent<ChunkBuffer>().Create(1);
			IChunkData chunkData;
			unsafe {
				chunkData = new ChunkDataV8(index: 0, data: ChunkAllocator.Allocate(Chunk.ChunkSize));
				for(var i = 0; i < Chunk.ChunkSize * Chunk.ChunkSize * Chunk.ChunkSize; i++) {
					chunkData.SetBlockType(i, 1);
				}
			}
			Chunk chunk = gameObject.AddComponent<Chunk>();
			chunk.Data = chunkData;
			buffer.SetChunkData(0, chunk);
			chunk.Rebuild();
			unsafe {
				ChunkAllocator.Free(((ChunkDataV8)chunkData).Data);
			}
		}
	}
}