using System;
using UnityEngine;
using Universe.Data.Chunk;

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
            var combine = new CombineInstance[_chunks.Length];
            blockCount = 0;
            chunkCount = _chunks.Length;
            triangleCount = 0;
            vertexCount = 0;

            for (var i = 0; i < _chunks.Length; i++) {
                var chunk = _chunks[i];
                var chunkPos = GetChunkPosition(i);
                var result = Chunk.ChunkBuilder.BuildChunk(chunk.Data, chunkPos);
                combine[i].mesh = result.mesh;
                combine[i].transform = Matrix4x4.TRS(chunkPos, Quaternion.identity, Vector3.one);
                blockCount += result.blockCount;
                triangleCount += result.triangleCount;
                vertexCount += result.vertexCount;
            }

            var meshFilter = GetComponent<MeshFilter>();
            if (meshFilter == null) {
                meshFilter = gameObject.AddComponent<MeshFilter>();
            }

            var meshRenderer = GetComponent<MeshRenderer>();
            if (meshRenderer == null) {
                meshRenderer = gameObject.AddComponent<MeshRenderer>();
                meshRenderer.material = Resources.Load<Material>("ChunkMaterial");
            }

            var mesh = new Mesh();
            mesh.CombineMeshes(combine, true);
            meshFilter.mesh = mesh;
            isDirty = false;
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
