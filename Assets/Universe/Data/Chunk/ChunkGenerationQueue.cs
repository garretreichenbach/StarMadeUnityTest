using System.Collections.Generic;
using Settings;
using UnityEngine;

namespace Universe.Data.Chunk {
	public class ChunkGenerationQueue : MonoBehaviour {
		readonly Queue<GameEntity.GameEntity> _rebuildQueue = new Queue<GameEntity.GameEntity>();
		readonly Queue<GameEntity.GameEntity> _teardownQueue = new Queue<GameEntity.GameEntity>();

		int MaxRebuildsPerFrame => EngineSettings.Instance.MaxEntityRebuildsPerFrame.Value;

		void Update() {
			int rebuilt = 0;
			int toProcess = Mathf.Min(MaxRebuildsPerFrame, _rebuildQueue.Count); // Limit to 5 per frame to avoid hitches
			for(int i = 0; i < toProcess; i++) {
				GameEntity.GameEntity entity = _rebuildQueue.Dequeue();
				if(entity != null) {
					entity.RebuildMesh();
					rebuilt++;
				}
			}
			if(rebuilt > 0) {
				Debug.Log($"Rebuilt {rebuilt} entities this frame.");
			}

			int tornDown = 0;
			toProcess = _teardownQueue.Count; // Process all teardowns each frame
			for(int i = 0; i < toProcess; i++) {
				GameEntity.GameEntity entity = _teardownQueue.Dequeue();
				if(entity != null) {
					entity.Teardown();
					tornDown++;
				}
			}
			if(tornDown > 0) {
				Debug.Log($"Tore down {tornDown} entities this frame.");
			}
		}

		public void RequestMeshRebuild(GameEntity.GameEntity entity) {
			if(!_rebuildQueue.Contains(entity)) {
				_rebuildQueue.Enqueue(entity);
			}
		}

		public void RequestTeardown(GameEntity.GameEntity entity) {
			if(!_teardownQueue.Contains(entity)) {
				_teardownQueue.Enqueue(entity);
			}
		}
	}
}