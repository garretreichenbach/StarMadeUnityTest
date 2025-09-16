using System;
using System.Threading.Tasks;
using Unity.VisualScripting.Dependencies.Sqlite;
using UnityEditor;
using UnityEngine;
using Universe.Data.Chunk;
using Universe.Data.Common;
using Universe.World;

namespace Universe.Data.GameEntity {

	[CustomEditor(typeof(GameEntity), true)]
	public class EntityEditor : Editor {
		public override void OnInspectorGUI() {
			DrawDefaultInspector();
			if(GUILayout.Button("Rebuild Mesh")) {
				(target as GameEntity)?.RequestMeshRebuild();
			}
			if(GUILayout.Button("Load Chunk Data")) {
				EntityDatabaseManager.Instance.LoadEntity(((GameEntity)target).Uid, true);
			}
			if(GUILayout.Button("Unload Chunk Data")) {
				EntityDatabaseManager.Instance.UnloadEntity(((GameEntity)target).Uid, true);
			}
			if(GUILayout.Button("Unload From Scene")) {
				EntityDatabaseManager.Instance.UnloadEntity(((GameEntity)target).Uid);
			}
		}
	}

	public class GameEntity : MonoBehaviour {

		public static int IDCounter;

		GameState _gameState;

		[Header("Entity Info")]
		public string Uid => data.Uid;

		public int EntityID => data.EntityID;

		public GameEntityType Type => data.EntityType;

		public string Name {
			get => data.EntityName;
			set => data.EntityName = value;
		}

		public int FactionID {
			get => data.FactionID;
			set => data.FactionID = value;
		}

		public int SectorID {
			get => data.SectorID;
			set => data.SectorID = value;
		}

		public bool Loaded {
			get => data.ChunkLoaded;
			set => data.ChunkLoaded = value;
		}

		public int ChunkCount {
			get => data.ChunkCount;
			set => data.ChunkCount = value;
		}

		public Vector3Int ChunkDimensions {
			get => new Vector3Int(data.ChunkDimensions[0], data.ChunkDimensions[1], data.ChunkDimensions[2]);
			set => data.ChunkDimensions = new[] { value.x, value.y, value.z };
		}

		public Vector3Int Sector {
			get {
				return Galaxy.Instance.GetSectorCoordsFromID(SectorID);
			}
			set {
				SectorID = Galaxy.Instance.GetSectorIDFromCoords(value);
			}
		}

		string ChunkDataPath => EntityDatabaseManager.Instance.GetChunkDataPath(Uid);

		public bool DrawDebugInfo { get; set; } = true;

		[Header("Draw Stats")]
		public int blockCount;
		public int triangleCount;
		public int vertexCount;

		public GameEntityData data;
		public ChunkData[] Chunks = Array.Empty<ChunkData>();

		bool _initialized;
		MeshFilter _meshFilter;
		MeshRenderer _meshRenderer;
		MeshCollider _meshCollider;

		void OnGUI() {
			if(!DrawDebugInfo) return;
			string debugText;
			if(Loaded) {
				debugText = $"{Name} (ID: {EntityID})\nType: {Type}\nChunks: {ChunkCount} ({ChunkDimensions.x}x{ChunkDimensions.y}x{ChunkDimensions.z})\nBlocks: {blockCount}\nTriangles: {triangleCount}\nVertices: {vertexCount}\nSector: {Sector}\nFaction: {FactionID}";
			} else {
				debugText = $"{Name} (ID: {EntityID})\nType: {Type}\nChunks: Not Loaded\nSector: {Sector}\nFaction: {FactionID}";
			}
			Handles.Label(transform.position + Vector3.up * 2, debugText);
		}

		public async Task<bool> LoadChunkData() {
			if(Loaded) {
				Debug.LogWarning($"Entity {Uid} is already loaded. Cannot load chunk data.");
				return false;
			}
			byte[] rawCompressedData = await EntityDatabaseManager.Instance.ReadChunkData(ChunkDataPath);
			if(rawCompressedData == null || rawCompressedData.Length == 0) {
				Debug.LogWarning($"No chunk data found for entity {Uid} at path {ChunkDataPath}.");
				return false;
			}
			_ = await _gameState.ChunkMemoryManager.DecompressEntity(this, rawCompressedData);
			Loaded = true;
			RequestMeshRebuild();
			return true;
		}

		public async Task<bool> WriteChunkData() {
			if(!Loaded) {
				Debug.LogWarning($"Entity {Name} is not loaded. Cannot write chunk data.");
				return false;
			}
			var sw = System.Diagnostics.Stopwatch.StartNew();
			byte[] compressedData = await _gameState.ChunkMemoryManager.CompressEntity(this);
			bool result = await EntityDatabaseManager.Instance.WriteChunkData(ChunkDataPath, compressedData);
			sw.Stop();
			if(sw.ElapsedMilliseconds > 5000) {
				Debug.LogWarning($"[GameEntity] WriteChunkData for entity {Uid} took {sw.ElapsedMilliseconds} ms - consider optimizing storage or chunk size.");
			}
			return result;
		}

		public IChunkData GetChunkData(long index) {
			return Chunks[index];
		}

		public Vector3 GetChunkPosition(int index) {
			int chunkX = index % ChunkDimensions.x;
			int chunkY = index / ChunkDimensions.x % ChunkDimensions.y;
			int chunkZ = index / (ChunkDimensions.x * ChunkDimensions.y);
			return new Vector3(chunkX * IChunkData.ChunkSize, chunkY * IChunkData.ChunkSize, chunkZ * IChunkData.ChunkSize);
		}

		void Initialize(GameState gameState) {
			_gameState = gameState;
			_meshFilter = gameObject.GetComponent<MeshFilter>();
			if(_meshFilter == null) {
				_meshFilter = gameObject.AddComponent<MeshFilter>();
			}
			_meshRenderer = gameObject.GetComponent<MeshRenderer>();
			if(_meshRenderer == null) {
				_meshRenderer = gameObject.AddComponent<MeshRenderer>();
			}
			_meshCollider = gameObject.GetComponent<MeshCollider>();
			if(_meshCollider == null) {
				_meshCollider = gameObject.AddComponent<MeshCollider>();
			}
			_initialized = true;
		}

		public void RebuildMesh() {
			blockCount = 0;
			triangleCount = 0;
			vertexCount = 0;
			if(!Loaded) {
				_meshFilter.mesh.Clear();
				_meshFilter.mesh.RecalculateBounds();
				_meshCollider.sharedMesh = null;
				return;
			}
			if(Chunks == null || Chunks.Length == 0) {
				AllocateChunks(ChunkDimensions);
			}
			if(data.ChunkCount == 0 || Chunks == null || Chunks.Length == 0) {
				Debug.LogError($"[GameEntity] RebuildMesh: Entity {Uid} is empty!");
				return;
			}
			var combine = new CombineInstance[data.ChunkCount];
			for(int i = 0; i < data.ChunkCount; i++) {
				var chunk = GetChunkData(i);
				if(chunk == null) {
					Debug.LogError($"[GameEntity] RebuildMesh: Chunk {i} is null!");
					continue;
				}
				if(chunk is ChunkData cd && !cd.IsValid) {
					Debug.LogError($"[GameEntity] RebuildMesh: Chunk {i} is invalid (chunkID={cd._chunkID})!");
					continue;
				}
				Vector3 chunkPos = GetChunkPosition(i);
				ChunkBuildResult result = ChunkBuilder.BuildChunk(chunk);
				combine[i].mesh = result.mesh;
				combine[i].transform = Matrix4x4.TRS(chunkPos, Quaternion.identity, Vector3.one);
				blockCount += result.blockCount;
				triangleCount += result.triangleCount;
				vertexCount += result.vertexCount;
			}

			_meshRenderer.material = Resources.Load<Material>("ChunkMaterial");
			Mesh mesh = new Mesh();
			mesh.CombineMeshes(combine, true);
			_meshFilter.mesh.RecalculateBounds();
			_meshFilter.mesh = mesh;
			_meshCollider.sharedMesh = mesh;
		}

		public void RequestMeshRebuild() {
			ChunkGenerationQueue chunkGenQueue = FindFirstObjectByType<ChunkGenerationQueue>();
			if(chunkGenQueue != null) {
				chunkGenQueue.RequestMeshRebuild(this);
			}
		}

		public void AllocateChunks(Vector3Int chunkDimensions) {
			ChunkDimensions = chunkDimensions;
			int chunksTotal = chunkDimensions.x * chunkDimensions.y * chunkDimensions.z;
			Chunks = new ChunkData[chunksTotal];
			ChunkCount = chunksTotal;
			Loaded = true;

			for(int i = 0; i < chunksTotal; i++) {
				long chunkID = ((long)EntityID << 32) | (uint)i;
				ChunkMemoryManager memoryManager = _gameState.ChunkMemoryManager;
				if(!memoryManager.Allocations.ContainsKey(chunkID)) {
					memoryManager.AllocateChunk(chunkID, EntityID, i);
					Chunks[i] = new ChunkData(chunkID, memoryManager.Allocations[chunkID].PoolIndex);
				}
			}
		}

		[Serializable]
		public struct GameEntityData {
			[PrimaryKey]
			public string Uid { get; set; }

			public GameEntityType EntityType { get; set; }

			public int EntityID { get; set; }

			public string EntityName { get; set; }

			public int FactionID { get; set; }

			public int SectorID { get; set; }

			public bool ChunkLoaded { get; set; }

			public int ChunkCount { get; set; }

			public int ChunkDimensionsX { get; set; }

			public int ChunkDimensionsY { get; set; }

			public int ChunkDimensionsZ { get; set; }

			[Ignore]
			public int[] ChunkDimensions {
				get => new[] { ChunkDimensionsX, ChunkDimensionsY, ChunkDimensionsZ };
				set => (ChunkDimensionsX, ChunkDimensionsY, ChunkDimensionsZ) = (value[0], value[1], value[2]);
			}
		}
	}

	[Serializable]
	public enum GameEntityType {
		Ship,
		Station,
		Asteroid,
		Planet,
		Player,
		Character,
	}
}