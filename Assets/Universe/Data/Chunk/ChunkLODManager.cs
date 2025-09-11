using System;
using System.Collections.Generic;
using UnityEngine;
using Universe.Data.GameEntity;
using Universe.Data.Chunk;

namespace Universe.Data.LOD {

	/// <summary>
	/// Manages LOD for all chunks in the scene, updating mesh detail based on camera distance
	/// </summary>
	public class ChunkLODManager : MonoBehaviour {

		[Header("LOD Settings")]
		[Tooltip("Distance multiplier for LOD calculations")]
		public float lodDistanceScale = 64f;

		[Tooltip("Minimum face size at near distance (higher = less detail)")]
		[Range(1, 8)]
		public int minFaceSizeNear = 1;

		[Tooltip("Maximum face size at far distance (higher = less detail)")]
		[Range(1, 32)]
		public int maxFaceSizeFar = 8;

		[Tooltip("Maximum distance for LOD calculations")]
		public float maxLODDistance = 500f;

		[Tooltip("How often to update LOD (in seconds)")]
		public float updateInterval = 0.5f;

		[Header("Performance")]
		[Tooltip("Maximum chunks to rebuild per frame")]
		public int maxRebuildsPerFrame = 2;

		[Tooltip("Enable debug visualization")]
		public bool debugVisualization = false;

		// LOD levels definition
		private readonly LODLevel[] lodLevels = {
			new LODLevel { distance = 0f, faceSize = 1, name = "High Detail" },
			new LODLevel { distance = 64f, faceSize = 2, name = "Medium Detail" },
			new LODLevel { distance = 128f, faceSize = 4, name = "Low Detail" },
			new LODLevel { distance = 256f, faceSize = 8, name = "Very Low Detail" },
			new LODLevel { distance = 512f, faceSize = 16, name = "Ultra Low Detail" }
		};

		private Camera mainCamera;
		private float lastUpdateTime;
		private readonly Dictionary<GameEntity.GameEntity, ChunkLODData> trackedEntities = new();
		private readonly Queue<ChunkRebuildRequest> rebuildQueue = new();
		private ChunkGenerationQueue chunkGenQueue;

		[System.Serializable]
		private struct LODLevel {
			public float distance;
			public int faceSize;
			public string name;
		}

		private struct ChunkLODData {
			public int currentLODLevel;
			public float lastDistance;
			public int currentFaceSize;
			public bool needsRebuild;
		}

		private struct ChunkRebuildRequest {
			public GameEntity.GameEntity entity;
			public int newFaceSize;
			public int lodLevel;
		}

		void Start() {
			mainCamera = Camera.main;
			if (mainCamera == null) {
				Debug.LogWarning("ChunkLODManager: No main camera found!");
			}

			chunkGenQueue = FindObjectOfType<ChunkGenerationQueue>();
			if (chunkGenQueue == null) {
				Debug.LogWarning("ChunkLODManager: No ChunkGenerationQueue found!");
			}

			// Update ChunkBuilder LOD settings
			UpdateChunkBuilderSettings();
		}

		void Update() {
			// Update camera reference in case it changes (e.g., switching between game/scene view)
			var currentCamera = GetActiveCamera();
			if (currentCamera != mainCamera) {
				mainCamera = currentCamera;
				if (debugVisualization) {
					Debug.Log($"ChunkLODManager: Switched to camera {mainCamera?.name ?? "null"}");
				}
			}

			if (mainCamera == null) return;

			// Update LOD levels periodically
			if (Time.time - lastUpdateTime >= updateInterval) {
				UpdateLODLevels();
				lastUpdateTime = Time.time;
			}

			// Process rebuild queue
			ProcessRebuildQueue();
		}

		/// <summary>
		/// Get the currently active camera (Scene camera in editor, or main camera in play mode)
		/// </summary>
		private Camera GetActiveCamera() {
#if UNITY_EDITOR
			// In editor, prefer Scene view camera when not playing
			if (!Application.isPlaying) {
				var sceneView = UnityEditor.SceneView.lastActiveSceneView;
				if (sceneView != null && sceneView.camera != null) {
					return sceneView.camera;
				}
			}

			// If playing in editor, try Scene view camera first, then main camera
			if (Application.isPlaying) {
				var sceneView = UnityEditor.SceneView.lastActiveSceneView;
				if (sceneView != null &&
				    sceneView.camera != null &&
				    UnityEditor.SceneView.currentDrawingSceneView == sceneView) {
					return sceneView.camera;
				}
			}
#endif
			// Fall back to main camera
			return Camera.main;
		}

		/// <summary>
		/// Register a GameEntity to be tracked for LOD updates
		/// </summary>
		public void RegisterEntity(GameEntity.GameEntity entity) {
			if (!trackedEntities.ContainsKey(entity)) {
				var lodData = new ChunkLODData {
					currentLODLevel = 0,
					lastDistance = float.MaxValue,
					currentFaceSize = minFaceSizeNear,
					needsRebuild = false
				};
				trackedEntities[entity] = lodData;

				if (debugVisualization) {
					Debug.Log($"ChunkLODManager: Registered entity {entity.name}");
				}
			}
		}

		/// <summary>
		/// Unregister a GameEntity from LOD tracking
		/// </summary>
		public void UnregisterEntity(GameEntity.GameEntity entity) {
			if (trackedEntities.Remove(entity)) {
				if (debugVisualization) {
					Debug.Log($"ChunkLODManager: Unregistered entity {entity.name}");
				}
			}
		}

		/// <summary>
		/// Get the appropriate face size for a given distance
		/// </summary>
		public int GetFaceSizeForDistance(float distance) {
			// Clamp distance to max LOD distance
			distance = Mathf.Min(distance, maxLODDistance);

			// Find appropriate LOD level
			for(int i = lodLevels.Length - 1; i >= 0; i--) {
				if (distance >= lodLevels[i].distance) {
					return Mathf.Clamp(lodLevels[i].faceSize, minFaceSizeNear, maxFaceSizeFar);
				}
			}

			return minFaceSizeNear;
		}

		/// <summary>
		/// Get LOD level index for a given distance
		/// </summary>
		public int GetLODLevelForDistance(float distance) {
			for(int i = lodLevels.Length - 1; i >= 0; i--) {
				if (distance >= lodLevels[i].distance) {
					return i;
				}
			}
			return 0;
		}

		private void UpdateLODLevels() {
			if (mainCamera == null) return;

			Vector3 cameraPosition = mainCamera.transform.position;
			var entitiesToUpdate = new List<GameEntity.GameEntity>();

			foreach (var kvp in trackedEntities) {
				var entity = kvp.Key;
				var lodData = kvp.Value;

				if (entity == null) continue;

				// Calculate distance to entity
				float distance = Vector3.Distance(cameraPosition, entity.transform.position);

				// Get appropriate LOD level and face size
				int newLODLevel = GetLODLevelForDistance(distance);
				int newFaceSize = GetFaceSizeForDistance(distance);

				// Check if LOD level changed significantly
				bool shouldUpdate = false;

				// Update if LOD level changed
				if (newLODLevel != lodData.currentLODLevel) {
					shouldUpdate = true;
				}

				// Update if distance changed significantly (more than 10%)
				else if (Mathf.Abs(distance - lodData.lastDistance) / lodData.lastDistance > 0.1f) {
					shouldUpdate = true;
				}

				if (shouldUpdate && newFaceSize != lodData.currentFaceSize) {
					// Queue for rebuild
					var request = new ChunkRebuildRequest {
						entity = entity,
						newFaceSize = newFaceSize,
						lodLevel = newLODLevel
					};
					rebuildQueue.Enqueue(request);

					// Update tracking data
					lodData.currentLODLevel = newLODLevel;
					lodData.currentFaceSize = newFaceSize;
					lodData.lastDistance = distance;
					lodData.needsRebuild = true;

					trackedEntities[entity] = lodData;

					if (debugVisualization) {
						Debug.Log($"ChunkLODManager: Queued {entity.name} for LOD {newLODLevel} rebuild (face size: {newFaceSize}, distance: {distance:F1})");
					}
				}
			}
		}

		private void ProcessRebuildQueue() {
			int rebuildsThisFrame = 0;

			while (rebuildQueue.Count > 0 && rebuildsThisFrame < maxRebuildsPerFrame) {
				var request = rebuildQueue.Dequeue();

				if (request.entity == null) continue;

				// Update ChunkBuilder settings for this rebuild
				var previousSettings = SaveChunkBuilderSettings();
				ApplyLODSettings(request.newFaceSize);

				try {
					// Trigger mesh rebuild
					if (chunkGenQueue != null) {
						chunkGenQueue.RequestMeshRebuild(request.entity);
					}
					else {
						request.entity.RebuildMesh();
					}

					// Update tracking
					if (trackedEntities.TryGetValue(request.entity, out var lodData)) {
						lodData.needsRebuild = false;
						trackedEntities[request.entity] = lodData;
					}

					rebuildsThisFrame++;

					if (debugVisualization) {
						Debug.Log($"ChunkLODManager: Rebuilt {request.entity.name} with face size {request.newFaceSize}");
					}
				}
				finally {
					// Restore previous settings
					RestoreChunkBuilderSettings(previousSettings);
				}
			}
		}

		private struct ChunkBuilderSettings {
			public float lodDistanceScale;
			public int lodMinFaceAtNear;
			public int lodMaxFaceAtFar;
		}

		private ChunkBuilderSettings SaveChunkBuilderSettings() {
			return new ChunkBuilderSettings {
				lodDistanceScale = ChunkBuilder.LODDistanceScale,
				lodMinFaceAtNear = ChunkBuilder.LODMinFaceAtNear,
				lodMaxFaceAtFar = ChunkBuilder.LODMaxFaceAtFar
			};
		}

		private void RestoreChunkBuilderSettings(ChunkBuilderSettings settings) {
			ChunkBuilder.LODDistanceScale = settings.lodDistanceScale;
			ChunkBuilder.LODMinFaceAtNear = settings.lodMinFaceAtNear;
			ChunkBuilder.LODMaxFaceAtFar = settings.lodMaxFaceAtFar;
		}

		private void ApplyLODSettings(int faceSize) {
			// Override ChunkBuilder settings for this specific rebuild
			ChunkBuilder.LODDistanceScale = lodDistanceScale;
			ChunkBuilder.LODMinFaceAtNear = faceSize;
			ChunkBuilder.LODMaxFaceAtFar = faceSize;
		}

		private void UpdateChunkBuilderSettings() {
			ChunkBuilder.LODDistanceScale = lodDistanceScale;
			ChunkBuilder.LODMinFaceAtNear = minFaceSizeNear;
			ChunkBuilder.LODMaxFaceAtFar = maxFaceSizeFar;
		}

		/// <summary>
		/// Force update all tracked entities (useful for debugging)
		/// </summary>
		[ContextMenu("Force Update All LOD")]
		public void ForceUpdateAllLOD() {
			foreach (var entity in trackedEntities.Keys) {
				var request = new ChunkRebuildRequest {
					entity = entity,
					newFaceSize = minFaceSizeNear,
					lodLevel = 0
				};
				rebuildQueue.Enqueue(request);
			}
		}

		/// <summary>
		/// Get debug information about current LOD state
		/// </summary>
		public string GetDebugInfo() {
			if (mainCamera == null) return "No camera";

			var info = new System.Text.StringBuilder();
			info.AppendLine($"ChunkLODManager Status:");
			info.AppendLine($"- Tracked entities: {trackedEntities.Count}");
			info.AppendLine($"- Rebuild queue: {rebuildQueue.Count}");
			info.AppendLine($"- Camera position: {mainCamera.transform.position}");

			foreach (var kvp in trackedEntities) {
				var entity = kvp.Key;
				var lodData = kvp.Value;
				if (entity != null) {
					float distance = Vector3.Distance(mainCamera.transform.position, entity.transform.position);
					info.AppendLine($"- {entity.name}: LOD {lodData.currentLODLevel}, Face Size {lodData.currentFaceSize}, Distance {distance:F1}");
				}
			}

			return info.ToString();
		}

		void OnDrawGizmos() {
			if (!debugVisualization || mainCamera == null) return;

			// Draw LOD distance rings
			Vector3 cameraPos = mainCamera.transform.position;

			for(int i = 0; i < lodLevels.Length; i++) {
				Gizmos.color = Color.Lerp(Color.green, Color.red, i / (float)(lodLevels.Length - 1));
				Gizmos.DrawWireSphere(cameraPos, lodLevels[i].distance);
			}

			// Draw entity LOD info
			foreach (var kvp in trackedEntities) {
				var entity = kvp.Key;
				var lodData = kvp.Value;

				if (entity != null) {
					Gizmos.color = Color.Lerp(Color.green, Color.red, lodData.currentLODLevel / (float)(lodLevels.Length - 1));
					Gizmos.DrawWireCube(entity.transform.position, Vector3.one * 5f);
				}
			}
		}

#if UNITY_EDITOR
		void OnGUI() {
			if (!debugVisualization) return;

			GUILayout.BeginArea(new Rect(10, 100, 400, 300));
			GUILayout.Label(GetDebugInfo());
			GUILayout.EndArea();
		}
#endif
	}
}