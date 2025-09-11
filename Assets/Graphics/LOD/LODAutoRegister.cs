using UnityEngine;
using Universe.Data.GameEntity;
using Universe.Data.LOD;

namespace Graphics.LOD {
	/// <summary>
	/// Auto-registers GameEntities with the ChunkLODManager when they are created
	/// Add this component to any GameEntity that should use LOD
	/// </summary>
	[RequireComponent(typeof(GameEntity))]
	public class LODAutoRegister : MonoBehaviour {

		[Header("LOD Settings")]
		[Tooltip("Override automatic LOD registration")]
		public bool manualControl = false;

		[Tooltip("Force a specific LOD level (if manual control is enabled)")]
		[Range(0, 7)]
		public int forcedLODLevel = 0;

		[Tooltip("Enable debug visualization for this entity")]
		public bool showDebugInfo = true;

		[Tooltip("Auto-rebuild mesh when LOD changes")]
		public bool autoRebuildOnLODChange = true;

		private ChunkLODManager lodManager;
		private GameEntity gameEntity;
		private bool isRegistered = false;
		private int lastLODLevel = -1;
		private float lastDistance = -1f;
		private float nextUpdateTime = 0f;
		private float updateInterval = 0.5f; // Update every 0.5 seconds

		void Awake() {
			gameEntity = GetComponent<Universe.Data.GameEntity.GameEntity>();
			if (gameEntity == null) {
				Debug.LogError("LODAutoRegister requires a GameEntity component!");
				enabled = false;
				return;
			}
		}

		void Start() {
			// Find or create LOD manager
			lodManager = FindObjectOfType<ChunkLODManager>();
			if (lodManager == null) {
				Debug.LogWarning($"No ChunkLODManager found in scene. Creating one for {gameObject.name}");
				var lodManagerGO = new GameObject("ChunkLODManager");
				lodManager = lodManagerGO.AddComponent<ChunkLODManager>();
			}

			RegisterWithLODManager();
		}

		void Update() {
			// Manual LOD update if auto-rebuild is enabled
			if (autoRebuildOnLODChange && !manualControl && Time.time >= nextUpdateTime) {
				CheckForLODChanges();
				nextUpdateTime = Time.time + updateInterval;
			}
		}

		void CheckForLODChanges() {
			if (lodManager == null || gameEntity == null) return;

			var camera = GetActiveCamera();
			if (camera == null) return;

			float distance = Vector3.Distance(camera.transform.position, transform.position);
			int currentLOD = lodManager.GetLODLevelForDistance(distance);
			int faceSize = lodManager.GetFaceSizeForDistance(distance);

			// Check if LOD level changed significantly
			bool shouldRebuild = false;

			if (currentLOD != lastLODLevel) {
				shouldRebuild = true;
				if (showDebugInfo) {
					Debug.Log($"LOD changed for {gameObject.name}: {lastLODLevel} -> {currentLOD} (distance: {distance:F1})");
				}
			}
			else if (lastDistance > 0 && Mathf.Abs(distance - lastDistance) / lastDistance > 0.2f) {
				// Distance changed by more than 20%
				shouldRebuild = true;
				if (showDebugInfo) {
					Debug.Log($"Distance changed significantly for {gameObject.name}: {lastDistance:F1} -> {distance:F1}");
				}
			}

			if (shouldRebuild) {
				gameEntity.RebuildMesh();
				lastLODLevel = currentLOD;
				lastDistance = distance;
			}
		}

		Camera GetActiveCamera() {
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
				if (sceneView != null && sceneView.camera != null) {
					return sceneView.camera;
				}
			}
#endif
			// Fall back to main camera
			return Camera.main;
		}

		void OnEnable() {
			if (lodManager != null && gameEntity != null && !isRegistered) {
				RegisterWithLODManager();
			}
		}

		void OnDisable() {
			UnregisterFromLODManager();
		}

		void OnDestroy() {
			UnregisterFromLODManager();
		}

		void RegisterWithLODManager() {
			if (!manualControl && lodManager != null && gameEntity != null) {
				lodManager.RegisterEntity(gameEntity);
				isRegistered = true;

				if (showDebugInfo) {
					Debug.Log($"Registered {gameObject.name} with ChunkLODManager");
				}
			}
		}

		void UnregisterFromLODManager() {
			if (isRegistered && lodManager != null && gameEntity != null) {
				lodManager.UnregisterEntity(gameEntity);
				isRegistered = false;

				if (showDebugInfo) {
					Debug.Log($"Unregistered {gameObject.name} from ChunkLODManager");
				}
			}
		}

		/// <summary>
		/// Manually trigger a mesh rebuild with specific LOD level
		/// </summary>
		[ContextMenu("Force Rebuild")]
		public void ForceRebuild() {
			if (gameEntity != null) {
				if (manualControl) {
					// Use ChunkBuilder directly with forced LOD
					gameEntity.RebuildMeshAtLOD(forcedLODLevel);
					if (showDebugInfo) {
						Debug.Log($"Manual rebuild of {gameObject.name} at LOD {forcedLODLevel}");
					}
				}
				else {
					// Let LOD manager handle it
					gameEntity.RebuildMesh();
					if (showDebugInfo) {
						var camera = GetActiveCamera();
						if (camera != null) {
							float distance = Vector3.Distance(camera.transform.position, transform.position);
							int lodLevel = lodManager.GetLODLevelForDistance(distance);
							Debug.Log($"Auto rebuild of {gameObject.name} at distance {distance:F1}, LOD {lodLevel}");
						}
					}
				}
			}
		}

		/// <summary>
		/// Toggle between automatic and manual LOD control
		/// </summary>
		[ContextMenu("Toggle Manual Control")]
		public void ToggleManualControl() {
			manualControl = !manualControl;

			if (manualControl) {
				UnregisterFromLODManager();
				if (showDebugInfo) {
					Debug.Log($"Switched {gameObject.name} to manual LOD control");
				}
			}
			else {
				RegisterWithLODManager();
				if (showDebugInfo) {
					Debug.Log($"Switched {gameObject.name} to automatic LOD control");
				}
			}
		}

		/// <summary>
		/// Force immediate LOD check and rebuild if needed
		/// </summary>
		[ContextMenu("Check LOD Now")]
		public void CheckLODNow() {
			CheckForLODChanges();
		}

#if UNITY_EDITOR
		void OnDrawGizmos() {
			if (!showDebugInfo) return;

			// Draw LOD info above the entity
			Vector3 pos = transform.position + Vector3.up * 10f;
			var camera = GetActiveCamera();

			if (manualControl) {
				UnityEditor.Handles.Label(pos, $"Manual LOD: {forcedLODLevel}");
			}
			else if (lodManager != null && gameEntity != null && camera != null) {
				float distance = Vector3.Distance(camera.transform.position, transform.position);
				int lodLevel = lodManager.GetLODLevelForDistance(distance);
				int faceSize = lodManager.GetFaceSizeForDistance(distance);

				UnityEditor.Handles.Label(pos, $"Auto LOD: {lodLevel}\nFace: {faceSize}\nDist: {distance:F1}\nCam: {camera.name}");

				// Draw line to camera
				Gizmos.color = Color.yellow;
				Gizmos.DrawLine(transform.position, camera.transform.position);
			}
			else {
				UnityEditor.Handles.Label(pos, "LOD: No Camera");
			}
		}
#endif
	}
}