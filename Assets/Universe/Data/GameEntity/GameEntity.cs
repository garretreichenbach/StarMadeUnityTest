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

        public void RebuildMesh() {
            this.RebuildMeshAtLOD(4);
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
