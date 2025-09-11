using UnityEngine;
using Universe.Data.Chunk;
using Universe.Data.GameEntity;
using Universe.Data.LOD;

namespace Graphics.LOD {
	/// <summary>
	/// Auto-registers GameEntities with the ChunkLODManager when they are created
	/// Add this component to any GameEntity that should use LOD
	/// </summary>
	[RequireComponent(typeof(Universe.Data.GameEntity.GameEntity))]
	public class LODAutoRegister : MonoBehaviour {

		[Header("LOD Settings")]
		[Tooltip("Override automatic LOD registration")]
		public bool manualControl = false;

		[Tooltip("Force a specific LOD level (if manual control is enabled)")]
		[Range(0, 7)]
		public int forcedLODLevel = 0;

		[Tooltip("Enable debug visualization for this entity")]
		public bool showDebugInfo = false;

		private ChunkLODManager lodManager;
		private GameEntity gameEntity;
		private bool isRegistered = false;

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
				}
				else {
					// Let LOD manager handle it
					gameEntity.RebuildMesh();
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
			}
			else {
				RegisterWithLODManager();
			}
		}

#if UNITY_EDITOR
		void OnDrawGizmos() {
			if (!showDebugInfo) return;

			// Draw LOD info above the entity
			Vector3 pos = transform.position + Vector3.up * 10f;

			if (manualControl) {
				UnityEditor.Handles.Label(pos, $"Manual LOD: {forcedLODLevel}");
			}
			else if (lodManager != null && gameEntity != null) {
				float distance = Vector3.Distance(Camera.main?.transform.position ?? Vector3.zero, transform.position);
				int lodLevel = lodManager.GetLODLevelForDistance(distance);
				int faceSize = lodManager.GetFaceSizeForDistance(distance);
				UnityEditor.Handles.Label(pos, $"Auto LOD: {lodLevel} (Face: {faceSize}, Dist: {distance:F1})");
			}
		}
#endif
	}
}