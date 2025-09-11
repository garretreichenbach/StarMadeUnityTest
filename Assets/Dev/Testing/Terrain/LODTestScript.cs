using Graphics.LOD;
using UnityEngine;
using Universe.Data.GameEntity;
using Universe.Data.LOD;
using Universe.Data.Chunk;

namespace Dev.Testing {
    /// <summary>
    /// Test script for demonstrating LOD functionality
    /// Add this to your SeedBasedTerrainTest scene to test LOD
    /// </summary>
    public class LODTestScript : MonoBehaviour {

        [Header("LOD Test Settings")]
        [Tooltip("Enable automatic LOD testing")]
        public bool enableLODTesting = true;

        [Tooltip("Test all LOD levels on startup")]
        public bool testAllLODLevels = true;

        [Tooltip("Show performance comparison")]
        public bool showPerformanceComparison = true;

        [Tooltip("Update interval for automatic testing")]
        public float updateInterval = 2f;

        [Header("Manual Testing")]
        [Range(0, 7)]
        public int manualLODLevel = 0;

        [Tooltip("Force rebuild with manual LOD level")]
        public bool forceManualRebuild = false;

        private ChunkLODManager lodManager;
        private GameEntity testEntity;
        private float lastUpdateTime;
        private LODPerformanceData[] performanceData;

        [System.Serializable]
        public struct LODPerformanceData {
            public int lodLevel;
            public float buildTime;
            public int vertexCount;
            public int triangleCount;
            public int faceCount;
            public float reductionRatio;
        }

        void Start() {
            // Find or create LOD manager
            lodManager = FindObjectOfType<ChunkLODManager>();
            if (lodManager == null) {
                var lodManagerGO = new GameObject("ChunkLODManager");
                lodManager = lodManagerGO.AddComponent<ChunkLODManager>();
                Debug.Log("Created ChunkLODManager for LOD testing");
            }

            // Find test entity (should be created by SeedBasedTerrainTest)
            testEntity = FindObjectOfType<GameEntity>();
            if (testEntity == null) {
                Debug.LogWarning("No GameEntity found for LOD testing. Make sure SeedBasedTerrainTest has run.");
                enabled = false;
                return;
            }

            // Add LOD auto-register component if it doesn't exist
            var lodAutoRegister = testEntity.GetComponent<LODAutoRegister>();
            if (lodAutoRegister == null) {
                lodAutoRegister = testEntity.gameObject.AddComponent<LODAutoRegister>();
                lodAutoRegister.showDebugInfo = true;
            }

            // Initialize performance data array
            performanceData = new LODPerformanceData[8];

            // Run initial tests
            if (testAllLODLevels) {
                StartCoroutine(TestAllLODLevels());
            }
        }

        void Update() {
            // Handle manual rebuild
            if (forceManualRebuild) {
                forceManualRebuild = false;
                TestSpecificLODLevel(manualLODLevel);
            }

            // Automatic testing
            if (enableLODTesting && Time.time - lastUpdateTime >= updateInterval) {
                lastUpdateTime = Time.time;

                if (testEntity != null) {
                    // Test current distance-based LOD
                    var buildInfo = testEntity.GetBuildInfo();
                    float distance = Vector3.Distance(Camera.main?.transform.position ?? Vector3.zero, testEntity.transform.position);
                    int autoLOD = lodManager.GetLODLevelForDistance(distance);

                    Debug.Log($"Auto LOD Test: Distance={distance:F1}, LOD={autoLOD}, Blocks={buildInfo.totalBlocks}");
                }
            }
        }

        System.Collections.IEnumerator TestAllLODLevels() {
            Debug.Log("=== Starting LOD Performance Test ===");

            for(int lodLevel = 0; lodLevel < 8; lodLevel++) {
                yield return new WaitForSeconds(0.5f); // Small delay between tests

                var result = TestSpecificLODLevel(lodLevel);
                performanceData[lodLevel] = new LODPerformanceData {
                    lodLevel = lodLevel,
                    buildTime = result.buildTime,
                    vertexCount = result.vertexCount,
                    triangleCount = result.triangleCount,
                    faceCount = result.faceCount,
                    reductionRatio = lodLevel > 0 ? 1f - ((float)result.vertexCount / performanceData[0].vertexCount) : 0f
                };

                Debug.Log($"LOD {lodLevel} Test: {result.buildTime * 1000:F2}ms, {result.vertexCount} verts, {result.triangleCount} tris, {result.faceCount} faces");
            }

            if (showPerformanceComparison) {
                PrintPerformanceComparison();
            }

            Debug.Log("=== LOD Performance Test Complete ===");
        }

        ChunkBuildResult TestSpecificLODLevel(int lodLevel) {
            if (testEntity == null) {
                return new ChunkBuildResult();
            }

            Debug.Log($"Testing LOD Level {lodLevel}...");

            // Get performance estimate
            var estimate = testEntity.GetLODEstimate(lodLevel);
            Debug.Log($"LOD {lodLevel} Estimate: {estimate.estimatedFaces} faces, {estimate.reductionRatio:P1} reduction");

            // Rebuild with specific LOD
            var startTime = Time.realtimeSinceStartup;
            testEntity.RebuildMeshAtLOD(lodLevel);
            var buildTime = Time.realtimeSinceStartup - startTime;

            return new ChunkBuildResult {
                buildTime = buildTime,
                vertexCount = testEntity.vertexCount,
                triangleCount = testEntity.triangleCount,
                blockCount = testEntity.blockCount,
                faceCount = estimate.estimatedFaces,
                lodLevel = lodLevel
            };
        }

        void PrintPerformanceComparison() {
            Debug.Log("=== LOD Performance Comparison ===");
            Debug.Log("LOD | Build Time | Vertices | Triangles | Faces | Reduction");
            Debug.Log("----+------------+----------+-----------+-------+----------");

            for(int i = 0; i < performanceData.Length; i++) {
                var data = performanceData[i];
                if (data.buildTime > 0) {
                    Debug.Log($" {data.lodLevel}  | {data.buildTime * 1000:F1}ms     | {data.vertexCount,8} | {data.triangleCount,9} | {data.faceCount,5} | {data.reductionRatio:P1}");
                }
            }
        }

        [ContextMenu("Test Current LOD")]
        public void TestCurrentLOD() {
            if (testEntity != null && lodManager != null) {
                float distance = Vector3.Distance(Camera.main?.transform.position ?? Vector3.zero, testEntity.transform.position);
                int currentLOD = lodManager.GetLODLevelForDistance(distance);
                TestSpecificLODLevel(currentLOD);
            }
        }

        [ContextMenu("Test All LOD Levels")]
        public void ManualTestAllLODLevels() {
            if (testEntity != null) {
                StartCoroutine(TestAllLODLevels());
            }
        }

        [ContextMenu("Show Build Info")]
        public void ShowBuildInfo() {
            if (testEntity != null) {
                var buildInfo = testEntity.GetBuildInfo();
                Debug.Log($"Build Info: {buildInfo.totalChunks} chunks, {buildInfo.totalBlocks} blocks, ~{buildInfo.estimatedVertices} vertices");
            }
        }

        [ContextMenu("Show Performance Stats")]
        public void ShowPerformanceStats() {
            Debug.Log(ChunkBuilder.GetPerformanceStats());
            if (lodManager != null) {
                Debug.Log(lodManager.GetDebugInfo());
            }
        }

        [ContextMenu("Clear Performance Stats")]
        public void ClearPerformanceStats() {
            ChunkBuilder.ClearPerformanceStats();
            performanceData = new LODPerformanceData[8];
        }

        /// <summary>
        /// Simulate moving the camera to test LOD transitions
        /// </summary>
        [ContextMenu("Simulate Camera Movement")]
        public void SimulateCameraMovement() {
            if (testEntity != null && Camera.main != null) {
                StartCoroutine(CameraMovementTest());
            }
        }

        System.Collections.IEnumerator CameraMovementTest() {
            var camera = Camera.main;
            var originalPos = camera.transform.position;
            var targetPos = testEntity.transform.position;

            Debug.Log("=== Starting Camera Movement LOD Test ===");

            // Test different distances
            float[] testDistances = { 10f, 50f, 100f, 200f, 400f, 800f };

            foreach (float distance in testDistances) {
                // Move camera to test distance
                Vector3 direction = (originalPos - targetPos).normalized;
                camera.transform.position = targetPos + direction * distance;

                yield return new WaitForSeconds(1f);

                // Log LOD info at this distance
                int lodLevel = lodManager.GetLODLevelForDistance(distance);
                int faceSize = lodManager.GetFaceSizeForDistance(distance);

                Debug.Log($"Distance: {distance:F1} -> LOD: {lodLevel}, Face Size: {faceSize}");
            }

            // Restore original camera position
            camera.transform.position = originalPos;
            Debug.Log("=== Camera Movement LOD Test Complete ===");
        }

        void OnGUI() {
            if (!showPerformanceComparison || testEntity == null) return;

            GUILayout.BeginArea(new Rect(Screen.width - 300, 10, 290, 400));
            GUILayout.BeginVertical("LOD Test Info", GUI.skin.window);

            // Current entity info
            var buildInfo = testEntity.GetBuildInfo();
            GUILayout.Label($"Entity: {testEntity.name}");
            GUILayout.Label($"Chunks: {buildInfo.totalChunks}");
            GUILayout.Label($"Blocks: {buildInfo.totalBlocks}");
            GUILayout.Label($"Vertices: {testEntity.vertexCount}");
            GUILayout.Label($"Triangles: {testEntity.triangleCount}");

            GUILayout.Space(10);

            // Current LOD info
            if (lodManager != null && Camera.main != null) {
                float distance = Vector3.Distance(Camera.main.transform.position, testEntity.transform.position);
                int currentLOD = lodManager.GetLODLevelForDistance(distance);
                int faceSize = lodManager.GetFaceSizeForDistance(distance);

                GUILayout.Label($"Distance: {distance:F1}");
                GUILayout.Label($"Current LOD: {currentLOD}");
                GUILayout.Label($"Face Size: {faceSize}");
            }

            GUILayout.Space(10);

            // Manual controls
            GUILayout.Label("Manual LOD Test:");
            manualLODLevel = (int)GUILayout.HorizontalSlider(manualLODLevel, 0, 7);
            GUILayout.Label($"LOD Level: {manualLODLevel}");

            if (GUILayout.Button("Test This LOD")) {
                TestSpecificLODLevel(manualLODLevel);
            }

            if (GUILayout.Button("Test All LODs")) {
                StartCoroutine(TestAllLODLevels());
            }

            GUILayout.EndVertical();
            GUILayout.EndArea();
        }

#if UNITY_EDITOR
        void OnDrawGizmos() {
            if (!enableLODTesting || testEntity == null) return;

            var camera = GetActiveCamera();
            if (camera == null) return;

            // Draw distance rings for LOD levels
            Vector3 entityPos = testEntity.transform.position;

            for(int i = 0; i < 8; i++) {
                float distance = i * lodManager.lodDistanceScale;
                Gizmos.color = Color.Lerp(Color.green, Color.red, i / 7f);
                Gizmos.DrawWireSphere(entityPos, distance);
            }

            // Draw line from camera to entity
            Gizmos.color = Color.yellow;
            Gizmos.DrawLine(camera.transform.position, entityPos);

            // Draw camera name
            UnityEditor.Handles.Label(camera.transform.position + Vector3.up * 2f, camera.name);
        }
        Camera GetActiveCamera() {
            var sceneView = UnityEditor.SceneView.lastActiveSceneView;
            if (sceneView != null) return sceneView.camera;
            if (Camera.main != null) return Camera.main;
            return null;
        }
#endif
    }
}