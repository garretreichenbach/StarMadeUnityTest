using System.Collections.Generic;
using System.Collections.Concurrent;
using System.Threading.Tasks;
using System.Threading;
using Settings;
using Unity.VisualScripting;
using Unity.VisualScripting.Dependencies.Sqlite;
using UnityEngine;
using Universe.Data.GameEntity;

namespace Universe.World {

	/**
	* Manages the saving and loading of game entities to and from the database using LiteDB.
	*/
	public class EntityDatabaseManager : MonoBehaviour {
		
		[Header("Database Settings")]
		[InspectorLabel("Instant Commit")]
		[Tooltip("If true, changes to the database will be committed immediately. If false, changes will be batched and committed periodically.")]
		public bool InstantCommit => ServerSettings.Instance.InstantCommit.Value;

		[InspectorLabel("Auto Commit Interval (seconds)")]
		[Tooltip("If Instant Commit is false, this is the interval in seconds at which changes will be committed to the database.")]
		public float CommitInterval => ServerSettings.Instance.DatabaseAutoCommitInterval.Value;

		bool _needsCommit = false;
		float _commitTimer = 0f;

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
					if(entity.Loaded) {
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
					if(!entity.Loaded) {
						count++;
					}
				}
				return count;
			}
		}

		public bool DrawDebugInfo {
			get {
				if(_activeEntities.Count == 0) {
					return false;
				}
				foreach(var entity in _activeEntities.Values) {
					if(entity != null && entity.DrawDebugInfo) {
						return true;
					}
				}
				return false;
			}
			set {
				if(DrawDebugInfo) {
					foreach(var entity in _activeEntities.Values) {
						if(entity != null) {
							entity.DrawDebugInfo = true;
						}
					}
				} else {
					foreach(var entity in _activeEntities.Values) {
						if(entity != null) {
							entity.DrawDebugInfo = false;
						}
					}
				}
			}
		}

		//Todo: More stats like amount of entities currently being processed for saving/loading, etc.

		public static EntityDatabaseManager Instance { get; private set; }

		static Dictionary<string, GameEntity> _activeEntities;

		const string UniverseName = "world0"; //Todo: Make this based on the current universe being played

		SQLiteConnection _db;

		// Write queue for chunk data
		class WriteRequest {
			public string Path;
			public byte[] Data;
			public TaskCompletionSource<bool> Completion;
		}

		ConcurrentQueue<WriteRequest> _writeQueue = new ConcurrentQueue<WriteRequest>();
		CancellationTokenSource _writeCts = new CancellationTokenSource();
		Task _writeTask;

		void Awake() {
			if(Instance != null && Instance != this) {
				Destroy(this);
			} else {
				Instance = this;
				_activeEntities = new Dictionary<string, GameEntity>();
				InitDB();
			}
		}

		void Start() {
			_writeTask = Task.Run(ProcessWriteQueue);
		}

		void Update() {
			if(!InstantCommit && _needsCommit) {
				_commitTimer += Time.deltaTime;
				if(_commitTimer >= CommitInterval) {
					_db.Commit();
					_commitTimer = 0f;
					_needsCommit = false;
					Debug.Log("Committed changes to the entity database.");
				}
			}
		}

		async Task ProcessWriteQueue() {
			while (!_writeCts.Token.IsCancellationRequested) {
				if (_writeQueue.TryDequeue(out var req)) {
					var sw = System.Diagnostics.Stopwatch.StartNew();
					try {
						var folderPath = System.IO.Path.GetDirectoryName(req.Path);
						if (!System.IO.Directory.Exists(folderPath)) {
							System.IO.Directory.CreateDirectory(folderPath);
						}
						await System.IO.File.WriteAllBytesAsync(req.Path, req.Data);
						req.Completion.SetResult(true);
					} catch (System.Exception ex) {
						Debug.LogError($"[WriteQueue] Failed to write chunk data to {req.Path}: {ex}");
						req.Completion.SetResult(false);
					}
				} else {
					await Task.Delay(10); // Avoid busy-wait
				}
			}
		}

		/**
		* Initializes the database connection and sets up necessary collections and indexes.
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
			GameObject entityObj = GameObject.Find(data.UID); //Todo: Change this to a more efficient lookup
			if(entityObj == null) {
				entityObj = new GameObject(data.UID);
			}
			GameEntity entityComp = null;
			switch(data.EntityType) {
				case GameEntityType.Asteroid:
					entityComp = entityObj.GetComponent<Asteroid>(); //Todo: These method calls are expensive, replace with something better
					break;
			}
			if(entityComp == null) {
				Debug.LogError($"Failed to load entity of type {data.EntityType} for entity ID {data.UID}");
				throw new System.Exception($"Failed to create entity of type {data.EntityType} for entity ID {data.UID}");
			}
			entityComp.Data = data;
			_activeEntities[data.UID].Data = data;
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
				GameEntity entity = _activeEntities[entityUID];
				if(entity.Loaded) {
					if(entity != null) {
						_ = entity.WriteChunkData();
						entity.Loaded = false;
					} else {
						Debug.LogWarning($"Entity {entityUID} is marked as loaded but could not find the component in the scene.");
					}
					if(unloadPhysicalOnly) {
						entity.RequestMeshRebuild();
					} else {
						Destroy(entity.gameObject);
						_activeEntities.Remove(entityUID);
					}
				}
				_db.Update(entity.Data);
				if(InstantCommit) {
					_db.Commit();
				} else {
					_needsCommit = true;
				}
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
					if(entityComp != null && entityComp.Loaded) {
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
			var req = new WriteRequest {
				Path = chunkDataPath,
				Data = compressedData,
				Completion = new TaskCompletionSource<bool>()
			};
			_writeQueue.Enqueue(req);
			return await req.Completion.Task;
		}

		public async Task FlushChunkDataWrites() {
			while (!_writeQueue.IsEmpty) {
				await Task.Delay(10);
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
		public void InsertEntity(GameEntity entity) {
			_db.Insert(entity.Data);
			if(InstantCommit) {
				_db.Commit();
			} else {
				_needsCommit = true;
			}
			_activeEntities.Add(entity.UID, entity);
			Debug.Log($"Inserted entity {entity.Name} with UID {entity.UID} into database.");
		}

		public async Task<bool> RemoveEntity(string uid) {
			if(_activeEntities.ContainsKey(uid)) {
				await UnloadEntity(uid);
				_activeEntities.Remove(uid);
				_db.Delete<GameEntity.GameEntityData>(uid);
				if(InstantCommit) {
					_db.Commit();
				} else {
					_needsCommit = true;
				}
				Debug.Log($"Removed entity {uid} from database.");
				return true;
			}
			Debug.LogWarning($"Attempted to remove entity {uid} but it was not found in active entities.");
			return false;
		}

		void OnDestroy() {
			FlushChunkDataWrites().Wait();
			 _writeCts.Cancel();
			_writeTask?.Wait(1000);
			_writeTask = null;
			_writeCts.Dispose();
			_db?.Dispose();
			_db = null;
			Instance = null;
			_activeEntities.Clear();
			_activeEntities = null;
		}
	}
}