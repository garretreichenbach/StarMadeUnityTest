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

        [Tooltip("Force immediate mesh rebuilds (bypass queue)")]
        public bool immediateRebuilds = true;

        [Header("Manual Testing")]
        [Range(0, 7)]
        public int manualLODLevel = 0;

        [Tooltip("Force rebuild with manual LOD level")]
        public bool forceManualRebuild = false;

        [Header("Camera Testing")]
        [Tooltip("Test camera distance detection")]
        public bool testCameraDetection = true;

        [Tooltip("Move camera for LOD testing")]
        public bool enableCameraMovement = false;

        private ChunkLODManager lodManager;
        private GameEntity testEntity;
        private float lastUpdateTime;
        private LODPerformanceData[] performanceData;
        private Camera activeCamera;

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

            // Enable debug visualization
            lodManager.debugVisualization = true;

            // Find test entity (should be created by SeedBasedTerrainTest)
            testEntity = FindObjectOfType<GameEntity>();
            if (testEntity == null) {
                Debug.LogWarning("No GameEntity found for LOD testing. Make sure SeedBasedTerrainTest has run.");
                enabled = false;
                return;
            }

            // Ensure LOD auto-register component exists and is configured
            var lodAutoRegister = testEntity.GetComponent<LODAutoRegister>();
            if (lodAutoRegister == null) {
                lodAutoRegister = testEntity.gameObject.AddComponent<LODAutoRegister>();
            }
            lodAutoRegister.showDebugInfo = true;
            lodAutoRegister.autoRebuildOnLODChange = true;

            // Initialize performance data array
            performanceData = new LODPerformanceData[8];

            // Get active camera
            activeCamera = GetActiveCamera();
            Debug.Log($"Active camera: {activeCamera?.name ?? "None"}");

            // Run initial tests
            if (testAllLODLevels) {
                StartCoroutine(TestAllLODLevels());
            }

            // Test camera detection immediately
            if (testCameraDetection) {
                TestCameraDetection();
            }
        }

        void Update() {
            // Update active camera reference
            var currentCamera = GetActiveCamera();
            if (currentCamera != activeCamera) {
                activeCamera = currentCamera;
                Debug.Log($"Camera switched to: {activeCamera?.name ?? "None"}");
            }

            // Handle manual rebuild
            if (forceManualRebuild) {
                forceManualRebuild = false;
                TestSpecificLODLevel(manualLODLevel);
            }

            // Automatic testing
            if (enableLODTesting && Time.time - lastUpdateTime >= updateInterval) {
                lastUpdateTime = Time.time;

                if (testEntity != null && activeCamera != null) {
                    // Test current distance-based LOD
                    var buildInfo = testEntity.GetBuildInfo();
                    float distance = Vector3.Distance(activeCamera.transform.position, testEntity.transform.position);
                    int autoLOD = lodManager.GetLODLevelForDistance(distance);
                    int faceSize = lodManager.GetFaceSizeForDistance(distance);

                    Debug.Log($"Auto LOD Test: Camera={activeCamera.name}, Distance={distance:F1}, LOD={autoLOD}, Face Size={faceSize}, Blocks={buildInfo.totalBlocks}");
                }
            }

            // Simple camera movement for testing
            if (enableCameraMovement && activeCamera != null) {
                float moveSpeed = 50f * Time.deltaTime;
                if (Input.GetKey(KeyCode.W)) activeCamera.transform.Translate(Vector3.forward * moveSpeed);
                if (Input.GetKey(KeyCode.S)) activeCamera.transform.Translate(Vector3.back * moveSpeed);
                if (Input.GetKey(KeyCode.A)) activeCamera.transform.Translate(Vector3.left * moveSpeed);
                if (Input.GetKey(KeyCode.D)) activeCamera.transform.Translate(Vector3.right * moveSpeed);
                if (Input.GetKey(KeyCode.Q)) activeCamera.transform.Translate(Vector3.down * moveSpeed);
                if (Input.GetKey(KeyCode.E)) activeCamera.transform.Translate(Vector3.up * moveSpeed);
            }
        }

        void TestCameraDetection() {
            var camera = GetActiveCamera();
            if (camera != null && testEntity != null) {
                float distance = Vector3.Distance(camera.transform.position, testEntity.transform.position);
                Debug.Log($"=== Camera Detection Test ===");
                Debug.Log($"Camera: {camera.name}");
                Debug.Log($"Camera Position: {camera.transform.position}");
                Debug.Log($"Entity Position: {testEntity.transform.position}");
                Debug.Log($"Distance: {distance:F2}");
                Debug.Log($"LOD Level: {lodManager.GetLODLevelForDistance(distance)}");
                Debug.Log($"Face Size: {lodManager.GetFaceSizeForDistance(distance)}");
            }
            else {
                Debug.LogWarning("Camera detection failed!");
                Debug.LogWarning($"Camera: {camera?.name ?? "null"}");
                Debug.LogWarning($"Entity: {testEntity?.name ?? "null"}");
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
                    reductionRatio = lodLevel > 0 && performanceData[0].vertexCount > 0 ?
                        1f - ((float)result.vertexCount / performanceData[0].vertexCount) : 0f
                };

                Debug.Log($"LOD {lodLevel} Test: {result.buildTime * 1000:F2}ms, {result.vertexCount} verts, {result.triangleCount} tris, {result.faceCount} faces, {performanceData[lodLevel].reductionRatio:P1} reduction");
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

            // Store original stats for comparison
            int originalVertices = testEntity.vertexCount;
            int originalTriangles = testEntity.triangleCount;

            // Rebuild with specific LOD
            var startTime = Time.realtimeSinceStartup;

            if (immediateRebuilds) {
                // Bypass queue and rebuild immediately
                testEntity.RebuildMeshAtLOD(lodLevel);
            } else {
                // Use queue system
                var chunkGenQueue = FindObjectOfType<ChunkGenerationQueue>();
                if (chunkGenQueue != null) {
                    chunkGenQueue.RequestMeshRebuild(testEntity);
                } else {
                    testEntity.RebuildMeshAtLOD(lodLevel);
                }
            }

            var buildTime = Time.realtimeSinceStartup - startTime;

            // Log the actual changes
            Debug.Log($"LOD {lodLevel} Results: Vertices {originalVertices} -> {testEntity.vertexCount}, Triangles {originalTriangles} -> {testEntity.triangleCount}");

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

        [ContextMenu("Test Camera Detection")]
        public void ManualTestCameraDetection() {
            TestCameraDetection();
        }

        [ContextMenu("Test Current LOD")]
        public void TestCurrentLOD() {
            if (testEntity != null && lodManager != null && activeCamera != null) {
                float distance = Vector3.Distance(activeCamera.transform.position, testEntity.transform.position);
                int currentLOD = lodManager.GetLODLevelForDistance(distance);
                Debug.Log($"Testing current LOD {currentLOD} at distance {distance:F1}");
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
                Debug.Log($"Current Stats: {testEntity.vertexCount} vertices, {testEntity.triangleCount} triangles");
            }
        }

        [ContextMenu("Force Rebuild Current")]
        public void ForceRebuildCurrent() {
            if (testEntity != null) {
                testEntity.RebuildMesh();
                Debug.Log("Forced mesh rebuild using automatic LOD");
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
            if (testEntity != null && activeCamera != null) {
                StartCoroutine(CameraMovementTest());
            }
        }

        System.Collections.IEnumerator CameraMovementTest() {
            var camera = activeCamera;
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

                // Force rebuild at this distance
                testEntity.RebuildMesh();
                yield return new WaitForSeconds(0.5f);
            }

            // Restore original camera position
            camera.transform.position = originalPos;
            Debug.Log("=== Camera Movement LOD Test Complete ===");
        }

        void OnGUI() {
            if (!showPerformanceComparison || testEntity == null) return;

            GUILayout.BeginArea(new Rect(Screen.width - 300, 10, 290, 500));
            GUILayout.BeginVertical("LOD Test Info", GUI.skin.window);

            // Current entity info
            var buildInfo = testEntity.GetBuildInfo();
            GUILayout.Label($"Entity: {testEntity.name}");
            GUILayout.Label($"Chunks: {buildInfo.totalChunks}");
            GUILayout.Label($"Blocks: {buildInfo.totalBlocks}");
            GUILayout.Label($"Vertices: {testEntity.vertexCount}");
            GUILayout.Label($"Triangles: {testEntity.triangleCount}");

            GUILayout.Space(10);

            // Camera and LOD info
            if (activeCamera != null) {
                float distance = Vector3.Distance(activeCamera.transform.position, testEntity.transform.position);
                int currentLOD = lodManager != null ? lodManager.GetLODLevelForDistance(distance) : 0;
                int faceSize = lodManager != null ? lodManager.GetFaceSizeForDistance(distance) : 1;

                GUILayout.Label($"Camera: {activeCamera.name}");
                GUILayout.Label($"Distance: {distance:F1}");
                GUILayout.Label($"Current LOD: {currentLOD}");
                GUILayout.Label($"Face Size: {faceSize}");
            }
            else {
                GUILayout.Label("Camera: None");
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

            if (GUILayout.Button("Force Rebuild")) {
                testEntity.RebuildMesh();
            }

            if (GUILayout.Button("Test Camera Detection")) {
                TestCameraDetection();
            }

            GUILayout.Space(10);

            // Settings
            GUILayout.Label("Settings:");
            immediateRebuilds = GUILayout.Toggle(immediateRebuilds, "Immediate Rebuilds");
            enableCameraMovement = GUILayout.Toggle(enableCameraMovement, "Camera Movement (WASD+QE)");

            GUILayout.EndVertical();
            GUILayout.EndArea();
        }

#if UNITY_EDITOR
        void OnDrawGizmos() {
            if (!enableLODTesting || testEntity == null) return;

            var camera = GetActiveCamera();
            if (camera == null) return;

            // Draw distance rings for LOD levels (if LOD manager exists)
            if (lodManager != null) {
                Vector3 entityPos = testEntity.transform.position;

                // Draw LOD distance rings
                for(int i = 0; i < 8; i++) {
                    float distance = i * lodManager.lodDistanceScale;
                    Gizmos.color = Color.Lerp(Color.green, Color.red, i / 7f);
                    Gizmos.DrawWireSphere(entityPos, distance);
                }

                // Draw line from camera to entity
                Gizmos.color = Color.yellow;
                Gizmos.DrawLine(camera.transform.position, entityPos);

                // Draw camera info
                Vector3 labelPos = camera.transform.position + Vector3.up * 5f;
                float dist = Vector3.Distance(camera.transform.position, entityPos);
                int lodLevel = lodManager.GetLODLevelForDistance(dist);
                UnityEditor.Handles.Label(labelPos, $"{camera.name}\nDist: {dist:F1}\nLOD: {lodLevel}");
            }
        }
#endif
    }
}