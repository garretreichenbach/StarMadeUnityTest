using System;
using System.Threading.Tasks;
using Unity.VisualScripting.Dependencies.Sqlite;
using UnityEditor;
using UnityEngine;
using Universe.Data.Chunk;
using Universe.World;

namespace Universe.Data.GameEntity {

	[CustomEditor(typeof(GameEntity), true)]
	public class EntityEditor : Editor {
		public override void OnInspectorGUI() {
			DrawDefaultInspector();
			if(GUILayout.Button("Rebuild Mesh")) {
				(target as GameEntity)?.RebuildMesh();
			}
			if(GUILayout.Button("Load Chunk Data")) {
				EntityDatabaseManager.Instance.LoadEntity(((GameEntity)target).UID, true);
			}
			if(GUILayout.Button("Unload Chunk Data")) {
				EntityDatabaseManager.Instance.UnloadEntity(((GameEntity)target).UID, true);
			}
			if(GUILayout.Button("Unload From Scene")) {
				EntityDatabaseManager.Instance.UnloadEntity(((GameEntity)target).UID);
			}
		}
	}

	public class GameEntity : MonoBehaviour {

		public static int IDCounter;

		[Header("Entity Info")]
		public string UID => Data.UID;

		public int EntityID => Data.EntityID;

		public GameEntityType Type => Data.EntityType;

		public string Name {
			get => Data.EntityName;
			set => Data.EntityName = value;
		}

		public int FactionID {
			get => Data.FactionID;
			set => Data.FactionID = value;
		}

		public int SectorID {
			get => Data.SectorID;
			set => Data.SectorID = value;
		}

		public bool Loaded {
			get => Data.ChunkLoaded;
			set => Data.ChunkLoaded = value;
		}

		public int ChunkCount {
			get => Data.ChunkCount;
			set => Data.ChunkCount = value;
		}

		public Vector3Int ChunkDimensions {
			get => new Vector3Int(Data.ChunkDimensions[0], Data.ChunkDimensions[1], Data.ChunkDimensions[2]);
			set => Data.ChunkDimensions = new[] { value.x, value.y, value.z };
		}

		public Vector3Int Sector {
			get {
				return Galaxy.Instance.GetSectorCoordsFromID(SectorID);
			}
			set {
				SectorID = Galaxy.Instance.GetSectorIDFromCoords(value);
			}
		}

		string ChunkDataPath => EntityDatabaseManager.Instance.GetChunkDataPath(UID);

		public bool DrawDebugInfo { get; set; } = true;

		[Header("Draw Stats")]
		public int blockCount;
		public int triangleCount;
		public int vertexCount;

		public GameEntityData Data;
		public ChunkData[] Chunks = Array.Empty<ChunkData>();

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
				Debug.LogWarning($"Entity {Name} is already loaded. Cannot load chunk data.");
				return false;
			}
			byte[] rawCompressedData = await EntityDatabaseManager.Instance.ReadChunkData(ChunkDataPath);
			if(rawCompressedData == null || rawCompressedData.Length == 0) {
				Debug.LogWarning($"No chunk data found for entity {Name} at path {ChunkDataPath}.");
				return false;
			}
			_ = await ChunkMemoryManager.Instance.DecompressEntity(this, rawCompressedData);
			Loaded = true;
			RebuildMesh();
			return true;
		}

		public async Task<bool> WriteChunkData() {
			if(!Loaded) {
				Debug.LogWarning($"Entity {Name} is not loaded. Cannot write chunk data.");
				return false;
			}
			byte[] compressedData = await ChunkMemoryManager.Instance.CompressEntity(this);
			_ = EntityDatabaseManager.Instance.WriteChunkData(ChunkDataPath, compressedData);
			return true;
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

		public void RebuildMesh() {
			MeshRenderer meshRenderer = GetComponent<MeshRenderer>();
			MeshFilter meshFilter = GetComponent<MeshFilter>();
			blockCount = 0;
			triangleCount = 0;
			vertexCount = 0;
			if(!Loaded) {
				meshFilter.mesh.Clear();
				meshFilter.mesh.RecalculateBounds();
				return;
			}

			var combine = new CombineInstance[Chunks.Length];
			for(int i = 0; i < Chunks.Length; i++) {
				Vector3 chunkPos = GetChunkPosition(i);
				ChunkBuildResult result = ChunkBuilder.BuildChunk(GetChunkData(i));
				combine[i].mesh = result.mesh;
				combine[i].transform = Matrix4x4.TRS(chunkPos, Quaternion.identity, Vector3.one);
				blockCount += result.blockCount;
				triangleCount += result.triangleCount;
				vertexCount += result.vertexCount;
			}

			if(meshFilter == null) {
				meshFilter = gameObject.AddComponent<MeshFilter>();
			}

			if(meshRenderer == null) {
				meshRenderer = gameObject.AddComponent<MeshRenderer>();
				meshRenderer.material = Resources.Load<Material>("ChunkMaterial");
			}

			Mesh mesh = new Mesh();
			mesh.CombineMeshes(combine, true);
			meshFilter.mesh = mesh;
			RequestMeshRebuild();
		}

		public void RequestMeshRebuild() {
			ChunkGenerationQueue chunkGenQueue = FindFirstObjectByType<ChunkGenerationQueue>();
			if(chunkGenQueue != null) {
				chunkGenQueue.RequestMeshRebuild(this);
			} else {
				Debug.LogWarning("ChunkGenerationQueue not found - triggering immediate mesh rebuild");
				RebuildMesh();
			}
		}

		public void AllocateChunks(Vector3Int chunkDimensions) {
			ChunkDimensions = chunkDimensions;
			int chunksTotal = chunkDimensions.x * chunkDimensions.y * chunkDimensions.z;
			Chunks = new ChunkData[chunksTotal];
			ChunkCount = chunksTotal;
			Loaded = true;
		}

		[Serializable]
		public struct GameEntityData {
			[PrimaryKey]
			public string UID { get; set; }
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
				get => new int[] { ChunkDimensionsX, ChunkDimensionsY, ChunkDimensionsZ };
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