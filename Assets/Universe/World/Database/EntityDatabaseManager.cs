using System.Collections.Generic;
using System.Threading.Tasks;
using LiteDB;
using Unity.VisualScripting;
using UnityEngine;
using Universe.Data.GameEntity;

namespace Universe.World.Database {

	/**
	* Manages the saving and loading of game entities to and from the database using LiteDB.
	*/
	public class EntityDatabaseManager : MonoBehaviour {

		[Header("Database Stats")]
		[InspectorLabel("Current Total Entities")]
		[Tooltip("The total number of entities active in the scene.")]
		public int CurrentActiveEntityCount => _activeEntities.Count;

		[InspectorLabel("Current Loaded Entities")]
		[Tooltip("The number of entities currently chunk loaded in memory.")]
		public int CurrentLoadedEntityCount {
			get {
				int count = 0;
				foreach(var entity in _activeEntities.Values) {
					if(entity.ChunkLoaded) {
						count++;
					}
				}
				return count;
			}
		}

		[InspectorLabel("Current Unloaded Entities")]
		[Tooltip("The number of entities that are active in the scene, but not chunk loaded].")]
		public int CurrentUnloadedEntityCount {
			get {
				int count = 0;
				foreach(var entity in _activeEntities.Values) {
					if(!entity.ChunkLoaded) {
						count++;
					}
				}
				return count;
			}
		}

		//Todo: More stats like amount of entities currently being processed for saving/loading, etc.

		public static EntityDatabaseManager Instance { get; private set; }

		static Dictionary<int, GameEntity.GameEntityData> _activeEntities;

		const string UniverseName = "world0"; //Todo: Make this based on the current universe being played

		LiteDatabase _db;

		void Awake() {
			if(Instance != null && Instance != this) {
				Destroy(this);
			} else {
				Instance = this;
				_activeEntities = new Dictionary<int, GameEntity.GameEntityData>();
				InitDB();
			}
		}

		/**
		* Initializes the LiteDB database connection and sets up necessary collections and indexes.
		*/
		void InitDB() {
			var folderPath = System.IO.Path.Combine(Application.persistentDataPath, "Database");
			if(!System.IO.Directory.Exists(folderPath)) {
				Debug.Log($"Creating database folder at {folderPath}");
				System.IO.Directory.CreateDirectory(folderPath);
			}
			var filePath = System.IO.Path.Combine(folderPath, $"{UniverseName}.db");
			_db = new LiteDatabase($"Filename={filePath};Connection=shared;");
			_db.GetCollection<GameEntity.GameEntityData>("entities");
		}

		/**
		* Loads the entity data from the database, and creates an empty game object in the scene.
		* Note: This does not load the entity's block data, just the entity data itself
		*/
		public Task<GameEntity.GameEntityData> LoadEntity(long databaseID) {
			var col = _db.GetCollection<GameEntity.GameEntityData>("entities");
			GameEntity.GameEntityData data = col.FindById(databaseID);
			GameObject entityObj = new GameObject($"Entity_{data.ID}");
			GameEntity entityComp = null;
			switch(data.Type) {
				case GameEntityType.Asteroid:
					entityComp = entityObj.AddComponent<Asteroid>();
					break;
			}
			_activeEntities.Add(data.ID, data);
			entityComp.Data = data;
			return Task.FromResult(data);
		}

		/**
		 * Unloads the entity from the scene and database.
		 */
		public Task UnloadEntity(int entityID) {
			if(_activeEntities.ContainsKey(entityID)) {
				GameEntity.GameEntityData data = _activeEntities[entityID];
				if(data.ChunkLoaded) {
					GameEntity entityComp = GetLoadedEntityFromID(entityID);
					if(entityComp != null) {
						_ = entityComp.WriteChunkData();
						data.ChunkLoaded = false;
						Destroy(entityComp.gameObject);
					} else {
						Debug.LogWarning($"Entity ID {entityID} is marked as loaded but could not find the component in the scene.");
					}
				}
				_activeEntities.Remove(entityID);
			} else {
				Debug.LogWarning($"Entity ID {entityID} not found in active entities.");
			}
			return Task.CompletedTask;
		}

		public GameEntity GetLoadedEntityFromID(int entityID) {
			if(_activeEntities.ContainsKey(entityID)) {
				var entityObj = GameObject.Find($"Entity_{entityID}");
				if(entityObj != null) {
					var entityComp = entityObj.GetComponent<GameEntity>();
					if(entityComp != null && entityComp.ChunkLoaded) {
						return entityComp;
					}
				}
			}
			Debug.LogWarning($"Entity ID {entityID} not found in active entities.");
			return null;
		}


		/**
		* Saves the entity data to the database, including all block data (if the entity is loaded)
		*/
		public Task SaveEntityAsync(GameEntity entity) {
			var col = _db.GetCollection<GameEntity.GameEntityData>("entities");
			if(entity.ChunkLoaded) {
				//If the entity is loaded, we need to save all chunk data as well
				var completed = entity.WriteChunkData();
				if(!completed.IsCompletedSuccessfully) {
					Debug.LogWarning($"Entity {entity.Name} chunk data write not completed yet. Save will proceed without chunk data.");
				}
			}
			col.Upsert(entity.Data);
			return Task.CompletedTask;
		}

		public string GetChunkDataPath(long databaseID) {
			return System.IO.Path.Combine(Application.persistentDataPath, "Database/Entities", $"{databaseID}.ent");
		}

		public async Task<bool> WriteChunkData(string chunkDataPath, byte[] compressedData) {
			var folderPath = System.IO.Path.GetDirectoryName(chunkDataPath);
			if(!System.IO.Directory.Exists(folderPath)) {
				System.IO.Directory.CreateDirectory(folderPath);
			}
			try {
				await System.IO.File.WriteAllBytesAsync(chunkDataPath, compressedData);
				return true;
			} catch(System.Exception ex) {
				Debug.LogError($"Failed to write chunk data to {chunkDataPath}: {ex}");
				return false;
			}
		}

		/**
		 * Adds a new entity to the database if it doesn't already exist.'
		 */
		public void CreateEntityData(GameEntity.GameEntityData data) {
			var col = _db.GetCollection<GameEntity.GameEntityData>("entities");
			col.Insert(data);
		}
	}
}