using System.Collections.Generic;
using System.Threading.Tasks;
using Unity.VisualScripting;
using Unity.VisualScripting.Dependencies.Sqlite;
using UnityEngine;
using Universe.Data.GameEntity;

namespace Universe.World {

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

		static Dictionary<string, GameEntity.GameEntityData> _activeEntities;

		const string UniverseName = "world0"; //Todo: Make this based on the current universe being played

		SQLiteConnection _db;

		void Awake() {
			if(Instance != null && Instance != this) {
				Destroy(this);
			} else {
				Instance = this;
				_activeEntities = new Dictionary<string, GameEntity.GameEntityData>();
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
			_db = new SQLiteConnection(filePath);

			_db.CreateTable<GameEntity.GameEntityData>();
		}

		/**
		* Loads the entity data from the database, and creates an empty game object in the scene.
		* Note: This does not load the entity's block data, just the entity data itself
		*/
		public Task<GameEntity.GameEntityData> LoadEntity(string UID, bool loadPhysical = false) {
			GameEntity.GameEntityData data = _db.Find<GameEntity.GameEntityData>(e => e.UID == UID);
			//Check if entity already exists in scene
			GameObject entityObj = GameObject.Find(data.UID);
			if(entityObj == null) {
				entityObj = new GameObject(data.UID);
			}
			GameEntity entityComp = null;
			switch(data.EntityType) {
				case GameEntityType.Asteroid:
					entityComp = entityObj.AddComponent<Asteroid>();
					break;
			}
			if(entityComp == null) {
				Debug.LogError($"Failed to create entity of type {data.EntityType} for entity ID {data.UID}");
				throw new System.Exception($"Failed to create entity of type {data.EntityType} for entity ID {data.UID}");
			}
			entityComp.Data = data;
			// entityObj.SetActive(true);
			_activeEntities[data.UID] = data;
			entityComp.Data = data;
			if(loadPhysical) {
				_ = entityComp.LoadChunkData();
			}
			Debug.Log($"Loaded entity {data.EntityName} with DB ID {data.UID} into scene.");
			return Task.FromResult(data);
		}

		/**
		* Unloads the entity from the scene.
		*/
		public Task UnloadEntity(string entityUID, bool unloadPhysicalOnly = false) {
			if(_activeEntities.ContainsKey(entityUID)) {
				GameEntity.GameEntityData data = _activeEntities[entityUID];
				if(data.ChunkLoaded) {
					GameEntity entityComp = GetLoadedEntityFromUID(entityUID);
					if(entityComp != null) {
						_ = entityComp.WriteChunkData();
						data.ChunkLoaded = false;
					} else {
						Debug.LogWarning($"Entity {entityUID} is marked as loaded but could not find the component in the scene.");
					}
					if(unloadPhysicalOnly) {
						entityComp.RebuildMesh();
						// entityComp.gameObject.SetActive(false);
					} else {
						Destroy(entityComp.gameObject);
						_activeEntities.Remove(entityUID);
					}
				}
				_db.Update(data);
				_db.Commit();
			} else {
				Debug.LogWarning($"Entity ID {entityUID} not found in active entities.");
			}
			Debug.Log($"Unloaded entity {entityUID}");
			return Task.CompletedTask;
		}

		public GameEntity GetLoadedEntityFromUID(string entityUID) {
			if(_activeEntities.ContainsKey(entityUID)) {
				var entityObj = GameObject.Find(entityUID);
				if(entityObj != null) {
					var entityComp = entityObj.GetComponent<GameEntity>();
					if(entityComp != null && entityComp.ChunkLoaded) {
						return entityComp;
					}
				}
			}
			Debug.LogWarning($"Entity ID {entityUID} not found in active entities.");
			return null;
		}

		public string GetChunkDataPath(string UID) {
			return System.IO.Path.Combine(Application.persistentDataPath, "Database/Entities", $"{UID}.ent");
		}

		/**
		* Writes entity chunk data to disk.
		*/
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
		* Reads entity chunk data from disk.
		*/
		public async Task<byte[]> ReadChunkData(string chunkDataPath) {
			if(System.IO.File.Exists(chunkDataPath)) {
				try {
					return await System.IO.File.ReadAllBytesAsync(chunkDataPath);
				} catch(System.Exception ex) {
					Debug.LogError($"Failed to read chunk data from {chunkDataPath}: {ex}");
					return null;
				}
			}
			Debug.LogWarning($"Chunk data file not found at {chunkDataPath}");
			return null;
		}

		/**
		* Adds a new entity to the database if it doesn't already exist.
		*/
		public void InsertEntity(GameEntity.GameEntityData data) {
			_db.Insert(data);
			_db.Commit();
			_activeEntities.Add(data.UID, data);
			Debug.Log($"Inserted entity {data.EntityName} with UID {data.UID} into database.");
		}

		void OnDestroy() {
			_db?.Dispose();
			_db = null;
			Instance = null;
			_activeEntities.Clear();
			_activeEntities = null;
		}
	}
}