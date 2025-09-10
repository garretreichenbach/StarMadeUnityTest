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
        public readonly int ID;
        public bool loaded;
        protected int sectorID;
        private int _totalChunks;
        
        private ChunkBuffer ChunkBuffer { get; set; }

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
                ChunkBuffer = gameObject.AddComponent<ChunkBuffer>().Create(_totalChunks);
            } catch(Exception e) {
                Debug.LogError("Failed to load entity in sector: " + e.Message);
                loaded = false;
            }
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
