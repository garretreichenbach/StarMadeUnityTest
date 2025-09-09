using System;
using Unity.Entities;
using Unity.VisualScripting;
using UnityEngine;
using Universe.Data.Chunk;
using Universe.Data.GameEntity;

namespace Dev.Testing.ECS {
	/**
	 * ECS system test for spawning a large number of objects in parallel.
	 */
	public class ParallelSpawner : MonoBehaviour {
		
		private EntityManager _entityManager;
		
		[InspectorLabel("Objects spawned")]
		public int count;
		
		[InspectorLabel("Object to spawn")]
		public GameEntity Prefab;

		[InspectorLabel("Area within which to spawn objects")]
		public Vector3 spawnArea = new(32, 32, 32);

		void Start() {
			/*_entityManager = World.DefaultGameObjectInjectionWorld.EntityManager;
			count = (int)(spawnArea.x * spawnArea.y * spawnArea.z);
			long indexCounter = 0;
			for(var x = 0; x < spawnArea.x; x++) {
				for(var y = 0; y < spawnArea.y; y++) {
					for(var z = 0; z < spawnArea.z; z++) {
						var instance = _entityManager.Instantiate(Prefab);
						_entityManager.SetComponentData(instance, new ChunkDataV8 { Index = indexCounter++ });
					}
				}
			}*/
			GameEntity entity = new GameObject("TestShip").AddComponent<Ship>();
			entity.LoadDataFromDB(new GameEntity.GameEntityData {
				Type = GameEntityType.Ship,
				UID = Guid.NewGuid(),
				Name = "Test Ship",
				FactionID = 0,
				SectorID = -1
			});
			ChunkBuffer buffer = entity.gameObject.AddComponent<ChunkBuffer>().Create(1);
			IChunkData chunkData = new ChunkDataV8();
			for(var i = 0; i < Chunk.ChunkSize * Chunk.ChunkSize * Chunk.ChunkSize; i++) {
				chunkData.SetBlockType(i, 1);
			}
			buffer.SetChunkData(0, gameObject.AddComponent<Chunk>());
			ChunkBaker baker = new();
			baker.Bake(buffer.GetChunkData(0));
		}
	}
}