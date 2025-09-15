using System;
using System.Collections.Concurrent;
using System.Threading;
using System.Threading.Tasks;
using Settings;
using Unity.VisualScripting.Dependencies.Sqlite;
using UnityEngine;
using Universe.Data.Client.Player;
using Universe.Data.GameEntity;
using Universe.Data.Inventory;

namespace Universe.World {
	public enum DataType {
		GameEntity,
		PlayerData,
		InventoryData,
	}

	public class DatabaseManager {

		SQLiteConnection _db;

		float CommitInterval => ServerConfig.Instance.DatabaseAutoCommitInterval.Value;

		bool _needsCommit;
		float _commitTimer;
		bool _initialized;

		struct WriteRequest {
			public string Path;
			public byte[] Data;
			public TaskCompletionSource<bool> Completion;
		}

		readonly ConcurrentQueue<WriteRequest> _writeQueue = new ConcurrentQueue<WriteRequest>();
		readonly CancellationTokenSource _writeCts = new CancellationTokenSource();
		Task _writeTask;

		public void Initialize(string dbPath) {
			if(_initialized) return;
			_db = new SQLiteConnection("Data Source=" + dbPath + ";Version=3;");
			_db.CreateTable<GameEntity.GameEntityData>();
			_writeTask = Task.Run(ProcessWriteQueue, _writeCts.Token);
			_initialized = true;
		}

		public void Load(DataType type, string uid, out object o) {
			o = type switch {
				DataType.GameEntity => _db.Find<GameEntity.GameEntityData>(x => x.Uid == uid) is var data ? o = data : o = null,
				DataType.PlayerData => _db.Find<PlayerData>(x => x.Uid == uid) is var pdata ? o = pdata : o = null,
				_ => null,
			};
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
				if(_writeQueue.TryDequeue(out var req)) {
					var sw = System.Diagnostics.Stopwatch.StartNew();
					try {
						string folderPath = System.IO.Path.GetDirectoryName(req.Path);
						if(!System.IO.Directory.Exists(folderPath)) {
							System.IO.Directory.CreateDirectory(folderPath);
						}
						await System.IO.File.WriteAllBytesAsync(req.Path, req.Data);
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
	}
}