using System;
using UnityEngine;
using Universe.Data.Chunk;
using Universe.Data.LOD;

namespace Universe.Data.GameEntity {

    public abstract class GameEntity : MonoBehaviour {

        static int _idCounter = 0;

        public struct GameEntityData {
            public GameEntityType Type;
            public int ID;
            public string Name;
            public int FactionID;
            public int SectorID;

            public GameEntityData(GameEntityType type, string name = "", int factionID = 0, int sectorID = 0) : this() {
                Type = type;
                ID = _idCounter++;
                Name = name;
                FactionID = factionID;
                SectorID = sectorID;
            }
        }

        public readonly GameEntityType Type;
        public int ID;
        public bool loaded;
        protected int sectorID;
        private int _totalChunks;
        private Chunk.Chunk[] _chunks;

        [Header("Stats")]
        public int blockCount;
        public int chunkCount;
        public int triangleCount;
        public int vertexCount;

        private ChunkLODManager _lodManager;

        public Chunk.Chunk[] Chunks {
            get => _chunks;
        }

        public int TotalChunks {
            get => _totalChunks;
            set => _totalChunks = value;
        }

        public Vector3Int chunkDimensions;

        public Vector3 GetChunkPosition(int index) {
            var chunkX = index % chunkDimensions.x;
            var chunkY = (index / chunkDimensions.x) % chunkDimensions.y;
            var chunkZ = index / (chunkDimensions.x * chunkDimensions.y);
            return new Vector3(chunkX * Chunk.Chunk.ChunkSize, chunkY * Chunk.Chunk.ChunkSize, chunkZ * Chunk.Chunk.ChunkSize);
        }

        protected GameEntity(GameEntityType type) {
            Type = type;
        }

        protected abstract void Initialize(GameEntityData data);

        /**
         * Loads the entity data from the database, and creates an empty game object in the scene.
         * Note: This does not load the entity's block data, just the entity data itself.
         */
        public GameEntity LoadDataFromDB(GameEntityData data) {
            //Todo: Implement loading from DB
            //For now, just create a new entity entirely for testing
            Asteroid asteroid = gameObject.AddComponent<Asteroid>();
            asteroid.Initialize(data);
            return asteroid;
        }

        public void LoadInSector(int sectorID) {
            try {
                this.sectorID = sectorID;
                _chunks = new Chunk.Chunk[_totalChunks];
            } catch(Exception e) {
                Debug.LogError("Failed to load entity in sector: " + e.Message);
                loaded = false;
            }
        }

        public Chunk.Chunk GetChunk() {
            return _chunks[0];
        }

        public bool isDirty = false;

        /// <summary>
        /// Rebuild mesh using automatic LOD based on camera distance
        /// </summary>
        public void RebuildMesh() {
            // Find LOD manager if not cached
            if (_lodManager == null) {
                _lodManager = FindObjectOfType<ChunkLODManager>();
            }

            if (_lodManager != null) {
                // Calculate distance to active camera
                var camera = GetActiveCamera();
                if (camera != null) {
                    float distance = Vector3.Distance(camera.transform.position, transform.position);
                    int lodLevel = _lodManager.GetLODLevelForDistance(distance);

                    Debug.Log($"RebuildMesh: Distance={distance:F1}, LOD={lodLevel}, Camera={camera.name}");
                    this.RebuildMeshAtLOD(lodLevel);
                    return;
                }
            }

            // Fallback to high detail if no LOD manager
            Debug.Log("RebuildMesh: Fallback to LOD 0 (no LOD manager or camera)");
            this.RebuildMeshAtLOD(0);
        }

        /// <summary>
        /// Get the currently active camera (Scene camera in editor, or main camera in play mode)
        /// </summary>
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
    }

    public enum GameEntityType {
        Ship,
        Station,
        Asteroid,
        Planet,
        Player,
        Character,
    }
}