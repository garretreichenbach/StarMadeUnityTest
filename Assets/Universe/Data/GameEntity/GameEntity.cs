using System;
using UnityEngine;
using Universe.Data.Chunk;

namespace Universe.Data.GameEntity {

	public abstract class GameEntity : MonoBehaviour {

		static int _idCounter;

		[Header("Entity Info")]
		public int ID => _data.ID;
		public long DatabaseID => _data.DatabaseID;
		public GameEntityType Type => _data.Type;
		public string Name => _data.Name;
		public int FactionID => _data.FactionID;
		public int SectorID => _data.SectorID;
		public bool Loaded => _data.Loaded;

		[Header("Debug Stats")]
		public int blockCount;
		public int chunkCount;
		public int triangleCount;
		public int vertexCount;

		public Vector3Int chunkDimensions;
		int _totalChunks;
		public bool isDirty;

		GameEntityData _data;

		public ChunkData[] Chunks = Array.Empty<ChunkData>();

		public GameEntity(GameEntityData data) {
			_data = data;
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
				ChunkBuildResult result = ChunkBuilder.BuildChunk(GetChunkData(i), chunkPos);
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
			isDirty = false;
		}

		public void AllocateChunks(int chunksTotal) {
			Chunks = new ChunkData[chunksTotal];
			chunkCount = chunksTotal;
		}

		public struct GameEntityData {
			public GameEntityType Type;
			public int ID;
			public long DatabaseID;
			public string Name;
			public int FactionID;
			public int SectorID;
			public bool Loaded;

			public GameEntityData(GameEntityType type, long databaseID, string name = "", int factionID = 0, int sectorID = 0) : this() {
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