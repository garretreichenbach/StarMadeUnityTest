using System.Collections.Generic;
using Settings;
using UnityEngine;

namespace Universe.Data.Chunk {
	public class ChunkGenerationQueue : MonoBehaviour {
		readonly Queue<GameEntity.GameEntity> _queue = new Queue<GameEntity.GameEntity>();

		int MaxRebuildsPerFrame => EngineSettings.Instance.MaxEntityRebuildsPerFrame.Value;

		void Update() {
			int rebuilt = 0;
			int toProcess = Mathf.Min(MaxRebuildsPerFrame, _queue.Count); // Limit to 5 per frame to avoid hitches
			for(int i = 0; i < toProcess; i++) {
				GameEntity.GameEntity entity = _queue.Dequeue();
				if(entity != null) {
					entity.RebuildMesh();
					rebuilt++;
				}
			}
			if(rebuilt > 0) {
				Debug.Log($"Rebuilt {rebuilt} entities this frame.");
			}
		}

		public void RequestMeshRebuild(GameEntity.GameEntity entity) {
			if(!_queue.Contains(entity)) {
				_queue.Enqueue(entity);
			}
		}
	}
}