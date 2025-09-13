using System;
using System.Threading.Tasks;
using UnityEditor;
using UnityEngine;
using Universe.Data.Chunk;
using Universe.World.Database;

namespace Universe.Data.GameEntity {

	[CustomEditor(typeof(GameEntity), true)]
	public class EntityEditor : Editor {
		public override void OnInspectorGUI() {
			DrawDefaultInspector();
			if(GUILayout.Button("Rebuild Mesh")) {
				(target as GameEntity)?.RebuildMesh();
			}
			if(GUILayout.Button("Load Chunk Data") && !((GameEntity) target).ChunkLoaded) {
				EntityDatabaseManager.Instance.LoadEntity(((GameEntity) target).ID);
			}
			if(GUILayout.Button("Unload Chunk Data") && ((GameEntity) target).ChunkLoaded) {
				EntityDatabaseManager.Instance.UnloadEntity(((GameEntity) target).ID);
			}
		}
	}

	public abstract class GameEntity : MonoBehaviour {

		static int _idCounter;

		[Header("Entity Info")]
		public string UID => Data.UID;
		public int ID => Data.ID;
		public long DatabaseID => Data.DatabaseID;
		public GameEntityType Type => Data.Type;
		public string Name {
			get => Data.Name;
			set => Data.Name = value;
		}

		public int FactionID {
			get => Data.FactionID;
			set => Data.FactionID = value;
		}

		public int SectorID {
			get => Data.SectorID;
			set => Data.SectorID = value;
		}

		public bool ChunkLoaded {
			get => Data.ChunkLoaded;
			set => Data.ChunkLoaded = value;
		}

		string ChunkDataPath => EntityDatabaseManager.Instance.GetChunkDataPath(DatabaseID);

		[Header("Debug Stats")]
		public int blockCount;
		public int chunkCount;
		public int triangleCount;
		public int vertexCount;

		public Vector3Int chunkDimensions;
		public GameEntityData Data;
		public ChunkData[] Chunks = Array.Empty<ChunkData>();

		protected GameEntity(GameEntityData data) {
			Data = data;
			EntityDatabaseManager.Instance.CreateEntityData(data);
		}

		public async Task<bool> WriteChunkData() {
			if(!ChunkLoaded) {
				Debug.LogWarning($"Entity {Name} is not loaded. Cannot write chunk data.");
				return false;
			}
			//Compress chunk data
			byte[] compressedData = await ChunkMemoryManager.Instance.CompressEntity(this);
			//Write to disk
			_ = EntityDatabaseManager.Instance.WriteChunkData(ChunkDataPath, compressedData);
			return true;
		}

		public IChunkData GetChunkData(long index) {
			return Chunks[index];
		}

		public Vector3 GetChunkPosition(int index) {
			int chunkX = index % chunkDimensions.x;
			int chunkY = index / chunkDimensions.x % chunkDimensions.y;
			int chunkZ = index / (chunkDimensions.x * chunkDimensions.y);
			return new Vector3(chunkX * IChunkData.ChunkSize, chunkY * IChunkData.ChunkSize, chunkZ * IChunkData.ChunkSize);
		}

		public void RebuildMesh() {
			var combine = new CombineInstance[chunkCount];
			blockCount = 0;
			triangleCount = 0;
			vertexCount = 0;

			for(int i = 0; i < chunkCount; i++) {
				Vector3 chunkPos = GetChunkPosition(i);
				ChunkBuildResult result = ChunkBuilder.BuildChunk(GetChunkData(i));
				combine[i].mesh = result.mesh;
				combine[i].transform = Matrix4x4.TRS(chunkPos, Quaternion.identity, Vector3.one);
				blockCount += result.blockCount;
				triangleCount += result.triangleCount;
				vertexCount += result.vertexCount;
			}

			MeshFilter meshFilter = GetComponent<MeshFilter>();
			if(meshFilter == null) {
				meshFilter = gameObject.AddComponent<MeshFilter>();
			}

			MeshRenderer meshRenderer = GetComponent<MeshRenderer>();
			if(meshRenderer == null) {
				meshRenderer = gameObject.AddComponent<MeshRenderer>();
				meshRenderer.material = Resources.Load<Material>("ChunkMaterial");
			}

			Mesh mesh = new Mesh();
			mesh.CombineMeshes(combine, true);
			meshFilter.mesh = mesh;
		}

		public void AllocateChunks(int chunksTotal) {
			Chunks = new ChunkData[chunksTotal];
			chunkCount = chunksTotal;
			ChunkLoaded = true;
		}

		public struct GameEntityData {
			public string UID;
			public GameEntityType Type;
			public int ID;
			public long DatabaseID;
			public string Name;
			public int FactionID;
			public int SectorID;
			public bool ChunkLoaded;

			public GameEntityData(GameEntityType type, long databaseID, string name = "", int factionID = 0, int sectorID = 0) : this() {
				UID = $"Entity_{ID}";
				Type = type;
				ID = _idCounter++;
				DatabaseID = databaseID;
				Name = name;
				FactionID = factionID;
				SectorID = sectorID;
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