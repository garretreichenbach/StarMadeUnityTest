using System;
using UnityEngine;
using UnityEngine.Serialization;
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
        public int id;
        public bool loaded;
        protected int sectorID;
        int _totalChunks;
        public ChunkData[] Chunks = Array.Empty<ChunkData>();

        [Header("Stats")]
        public int blockCount;
        public int chunkCount;
        public int triangleCount;
        public int vertexCount;

        public GameEntity(int sectorID) {
            id = _idCounter++;
            this.sectorID = sectorID;
        }

        public IChunkData GetChunkData(long index) {
            return Chunks[index];
        }

        public Vector3Int chunkDimensions;

        public Vector3 GetChunkPosition(int index) {
            var chunkX = index % chunkDimensions.x;
            var chunkY = (index / chunkDimensions.x) % chunkDimensions.y;
            var chunkZ = index / (chunkDimensions.x * chunkDimensions.y);
            return new Vector3(chunkX * IChunkData.ChunkSize, chunkY * IChunkData.ChunkSize, chunkZ * IChunkData.ChunkSize);
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
                //Todo: Load chunk IDs from DB based on entity ID and sector ID
            } catch(Exception e) {
                Debug.LogError("Failed to load entity in sector: " + e.Message);
                loaded = false;
            }
        }

        public bool isDirty = false;

        public void RebuildMesh() {
            var combine = new CombineInstance[chunkCount];
            blockCount = 0;
            triangleCount = 0;
            vertexCount = 0;

            for (var i = 0; i < chunkCount; i++) {
                var chunkPos = GetChunkPosition(i);
                var result = ChunkBuilder.BuildChunk(GetChunkData(i), chunkPos);
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

        public void AllocateChunks(int chunksTotal) {
            Chunks = new ChunkData[chunksTotal];
            chunkCount = chunksTotal;
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
