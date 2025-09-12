using System.Collections.Generic;
using UnityEngine;

namespace Universe.Data.Chunk {
	public class ChunkGenerationQueue : MonoBehaviour {
		readonly Queue<GameEntity.GameEntity> _queue = new Queue<GameEntity.GameEntity>();

		void Update() {
			if(_queue.Count > 0) {
				GameEntity.GameEntity entity = _queue.Dequeue();
				entity.RebuildMesh();
			}
		}

		public void RequestMeshRebuild(GameEntity.GameEntity entity) {
			if(!_queue.Contains(entity)) {
				_queue.Enqueue(entity);
			}
		}
	}
}