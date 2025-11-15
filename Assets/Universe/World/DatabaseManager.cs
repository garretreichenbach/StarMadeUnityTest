using System;
using System.Collections.Concurrent;
using System.Diagnostics;
using System.IO;
using System.Threading;
using System.Threading.Tasks;
using Settings;
using Unity.VisualScripting.Dependencies.Sqlite;
using UnityEngine;
using Universe.Data.Client.Player;
using Universe.Data.GameEntity;
using Universe.Data.Inventory;
using Debug = UnityEngine.Debug;

namespace Universe.World {
	public enum DataType {
		GameEntity,
		PlayerData,
		InventoryData,
	}

	public class DatabaseManager {
		readonly CancellationTokenSource _writeCts = new CancellationTokenSource();

		readonly ConcurrentQueue<WriteRequest> _writeQueue = new ConcurrentQueue<WriteRequest>();
		readonly Task _writeTask;
		float _commitTimer;

		SQLiteConnection _db;
		bool _initialized;

		bool _needsCommit;

		public DatabaseManager(string worldName) {
			if(_initialized) {
				return;
			}

			try {
				string worldPath = Path.Combine(Application.persistentDataPath, "Database", worldName, worldName + ".db");
				Debug.Log($"[DatabaseManager] Initializing database at {worldPath}");

				// Ensure the directory exists
				string directoryPath = Path.GetDirectoryName(worldPath);
				if (string.IsNullOrEmpty(directoryPath)) {
					throw new InvalidOperationException("Unable to determine directory path for database");
				}

				if (!Directory.Exists(directoryPath)) {
					Debug.Log($"[DatabaseManager] Creating directory: {directoryPath}");
					Directory.CreateDirectory(directoryPath);
				}

				// Create the SQLite connection (SQLite will create the file if it doesn't exist)
				Debug.Log($"[DatabaseManager] Opening database connection to: {worldPath}");

				_db = new SQLiteConnection(worldPath);
				_db.CreateTable<GameEntity.GameEntityData>();
				_writeTask = Task.Run(ProcessWriteQueue, _writeCts.Token);
				_initialized = true;

				Debug.Log("[DatabaseManager] Database initialized successfully");
			} catch (Exception ex) {
				Debug.LogError($"[DatabaseManager] Failed to initialize database: {ex.Message}");
				Debug.LogError($"[DatabaseManager] Stack trace: {ex.StackTrace}");
				throw;
			}
		}

		float CommitInterval {
			get => ServerConfig.Instance.DatabaseAutoCommitInterval.Value;
		}

		public object Load(DataType type, string uid) {
			object entity = type switch {
				DataType.GameEntity => _db.Find<GameEntity.GameEntityData>(x => x.Uid == uid),
				DataType.PlayerData => _db.Find<PlayerData>(x => x.Uid == uid),
				_ => null,
			};
			Debug.Log($"Loaded entity {uid} of type {type} into scene");
			return entity;
		}

		public void Write(DataType type, object o) {
			switch(type) {
				case DataType.GameEntity:
					if(o is GameEntity.GameEntityData entityData) {
						_db.InsertOrReplace(entityData);
						_needsCommit = true;
					}
					break;

				case DataType.PlayerData:
					if(o is PlayerData playerData) {
						_db.InsertOrReplace(playerData);
						_needsCommit = true;
					}
					break;

				case DataType.InventoryData:
					if(o is Inventory inventory) {
						_db.InsertOrReplace(inventory);
						_needsCommit = true;
					}
					break;

				default:
					throw new ArgumentOutOfRangeException(nameof(type), type, null);
			}
		}

		async Task Update() {
			if(_needsCommit) {
				_commitTimer += Time.deltaTime;
				if(_commitTimer >= CommitInterval) {
					_db.Commit();
					_commitTimer = 0f;
					_needsCommit = false;
				}
			}
			await Task.Yield();
		}

		async Task ProcessWriteQueue() {
			while(!_writeCts.Token.IsCancellationRequested) {
				if(_writeQueue.TryDequeue(out WriteRequest req)) {
					Stopwatch sw = Stopwatch.StartNew();
					try {
						string folderPath = Path.GetDirectoryName(req.Path);
						if(!Directory.Exists(folderPath)) {
							Directory.CreateDirectory(folderPath);
						}
						await File.WriteAllBytesAsync(req.Path, req.Data);
						req.Completion.SetResult(true);
					} catch(Exception ex) {
						Debug.LogError($"[WriteQueue] Failed to data to {req.Path}: {ex}");
						req.Completion.SetResult(false);
					}
				} else {
					await Task.Delay(10); // Avoid busy-wait
				}
			}
		}

		public void Shutdown() {
			_db.Commit();
			_needsCommit = false;
			_writeCts.Cancel();
			_writeTask.Wait();
			_db?.Close();
			_db = null;
			_initialized = false;
		}

		struct WriteRequest {
			public string Path;
			public byte[] Data;
			public TaskCompletionSource<bool> Completion;
		}
	}
}