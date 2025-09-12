using UnityEngine;
using Universe.Data.Chunk;
using Universe.Data.GameEntity;
using Universe.Data.Generation;

namespace Dev.Testing.Terrain {
	public class GlobalMemoryTerrainTest : MonoBehaviour {

		[Header("World Settings")]
		[Tooltip("Seed for deterministic world generation")]
		public long worldSeed = 12345;

		[Header("Generation Settings")]
		public Vector3Int chunkDimensions = new Vector3Int(3, 3, 3);

		[Header("Asteroid Settings")]
		public AsteroidGenerationSettings asteroidSettings;

		void Start() {
			if(ChunkMemoryManager.Instance == null) {
				Debug.LogError("GlobalChunkMemoryManager not found! Make sure it's in the scene.");
				return;
			}

			if(asteroidSettings == null) {
				asteroidSettings = AsteroidGenerationSettings.CreateDefault(chunkDimensions);
			}

			GenerateTestAsteroid();
		}

		void OnDestroy() {
			// Clean up any chunks we allocated
			var asteroids = FindObjectsOfType<GameEntity>();
			foreach(GameEntity entity in asteroids) {
				if(entity.Chunks != null) {
					for(int i = 0; i < entity.Chunks.Length; i++) {
						ChunkMemoryManager.Instance?.DeallocateChunk(entity.Chunks[i]._chunkID);
					}
				}
			}
		}

		void GenerateTestAsteroid() {
			Debug.Log("=== Generating Test Asteroid with Global Memory System ===");

			GameEntity entity = new GameObject("TestAsteroid").AddComponent<Asteroid>();

			entity.gameObject.transform.position = new Vector3(0, 0, 0);
			entity.gameObject.transform.rotation = Quaternion.identity;
			entity.gameObject.SetActive(true);
			entity.chunkDimensions = chunkDimensions;

			int chunksTotal = chunkDimensions.x * chunkDimensions.y * chunkDimensions.z;
			entity.AllocateChunks(chunksTotal);

			SetupCrossChunkResolver(entity);

			// Generate chunks using the new global memory system
			GenerateChunksWithGlobalMemory(entity);

			// Build the mesh
			ChunkGenerationQueue chunkGenQueue = FindObjectOfType<ChunkGenerationQueue>();
			if(chunkGenQueue != null) {
				chunkGenQueue.RequestMeshRebuild(entity);
			} else {
				Debug.LogWarning("ChunkGenerationQueue not found - triggering immediate mesh rebuild");
				entity.RebuildMesh();
			}

			PrintMemoryStatistics("After generating first asteroid");
		}

		void GenerateChunksWithGlobalMemory(GameEntity entity) {
			int chunksTotal = entity.chunkCount;
			Debug.Log($"Generating {chunksTotal} chunks using GlobalChunkMemoryManager");

			// Generate all chunks using the global memory system
			for(int i = 0; i < chunksTotal; i++) {
				int chunkX = i % chunkDimensions.x;
				int chunkY = i / chunkDimensions.x % chunkDimensions.y;
				int chunkZ = i / (chunkDimensions.x * chunkDimensions.y);

				// Generate a unique chunk ID - include entity ID to avoid collisions
				long chunkID = GenerateChunkID(entity.ID, i);

				// Allocate chunk in global memory system
				if(!ChunkMemoryManager.Instance.AllocateChunk(chunkID, entity.ID, i)) {
					Debug.LogError($"Failed to allocate chunk {chunkID} in global memory!");
					continue;
				}

				// Create ChunkData that references the global memory
				ChunkData chunkData = new ChunkData(chunkID);

				// IMPORTANT: Set the chunk in the entity's array
				entity.Chunks[i] = chunkData;

				// Generate terrain data directly into global memory
				SeedBasedTerrainGenerator.GenerateChunk(chunkData, chunkX, chunkY, chunkZ, worldSeed, asteroidSettings);

				// Log generation info
				Debug.Log($"Generated chunk [{chunkX},{chunkY},{chunkZ}] with ID: {chunkID}");
			}

			Debug.Log($"Successfully generated {chunksTotal} chunks in global memory system");
		}

		long GenerateChunkID(int entityID, int chunkIndex) {
			// Generate a unique chunk ID based on entity ID and chunk index
			// Use a better approach that ensures uniqueness across all entities
			long baseID = (long)entityID << 32 | (uint)chunkIndex;

			// Add world seed to ensure uniqueness across different worlds/sessions
			long hashedID = baseID ^ worldSeed >> 16;

			return hashedID;
		}

		void SetupCrossChunkResolver(GameEntity entity) {
			Debug.Log("Setting up cross-chunk resolver for global memory system");

			// Updated cross-chunk resolver that works with ChunkData
			ChunkBuilder.ExternalBlockResolver = (currentChunk, lx, ly, lz) => {
				int s = IChunkData.ChunkSize;

				ChunkData currentData = (ChunkData)currentChunk;

				// Find which chunk this corresponds to by searching entity chunks
				int currentChunkIndex = -1;
				for(int i = 0; i < entity.Chunks.Length; i++) {
					if(entity.Chunks[i]._chunkID == currentData._chunkID) {
						currentChunkIndex = i;
						break;
					}
				}

				if(currentChunkIndex == -1) {
					Debug.LogError($"Could not find chunk index for chunk ID {currentData._chunkID}");
					return 0;
				}

				// Calculate current chunk coordinates
				int cx = currentChunkIndex % chunkDimensions.x;
				int cy = currentChunkIndex / chunkDimensions.x % chunkDimensions.y;
				int cz = currentChunkIndex / (chunkDimensions.x * chunkDimensions.y);

				// Adjust coordinates and move to neighbor chunk if needed
				if(lx < 0) {
					cx--;
					lx = s - 1;
				} else if(lx >= s) {
					cx++;
					lx = 0;
				}
				if(ly < 0) {
					cy--;
					ly = s - 1;
				} else if(ly >= s) {
					cy++;
					ly = 0;
				}
				if(lz < 0) {
					cz--;
					lz = s - 1;
				} else if(lz >= s) {
					cz++;
					lz = 0;
				}

				// If outside world bounds, treat as air
				if(cx < 0 || cy < 0 || cz < 0 || cx >= chunkDimensions.x || cy >= chunkDimensions.y || cz >= chunkDimensions.z)
					return 0;

				int neighborIndex = cx + cy * chunkDimensions.x + cz * chunkDimensions.x * chunkDimensions.y;

				// Make sure the neighbor chunk exists
				if(neighborIndex >= entity.Chunks.Length) {
					return 0;
				}

				ChunkData neighborChunk = entity.Chunks[neighborIndex];
				int bi = neighborChunk.GetBlockIndex(new Vector3(lx, ly, lz));
				return neighborChunk.GetBlockType(bi);
			};
		}

		void PrintMemoryStatistics(string context) {
			if(ChunkMemoryManager.Instance != null) {
				ChunkMemoryManager.Instance.GetMemoryStatistics(out int uncompressed, out int compressed, out long totalMemory);
				Debug.Log($"[{context}] Memory Stats - Uncompressed: {uncompressed}, Compressed: {compressed}, Total: {totalMemory / 1024 / 1024}MB");
			}
		}
	}
}