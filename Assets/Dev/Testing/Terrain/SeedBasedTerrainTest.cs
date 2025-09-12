using System.Collections;
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
		public Vector3Int chunkDimensions = new(3, 3, 3);

		[Header("Asteroid Settings")]
		public AsteroidGenerationSettings asteroidSettings;

		[Header("Testing")]
		[Tooltip("If true, generates the same asteroid twice to verify determinism")]
		public bool testDeterminism = false;

		void Start() {
			if (ChunkMemoryManager.Instance == null) {
				Debug.LogError("GlobalChunkMemoryManager not found! Make sure it's in the scene.");
				return;
			}

			if (asteroidSettings == null) {
				asteroidSettings = AsteroidGenerationSettings.CreateDefault(chunkDimensions);
			}

			GenerateTestAsteroid();

			if (testDeterminism) {
				StartCoroutine(TestDeterminismCoroutine());
			}
		}

		void GenerateTestAsteroid() {
			Debug.Log("=== Generating Test Asteroid with Global Memory System ===");

			GameEntity entity = new GameObject("GlobalMemoryTestAsteroid").AddComponent<Asteroid>();
			entity.LoadDataFromDB(new GameEntity.GameEntityData {
				Type = GameEntityType.Asteroid,
				FactionID = 0,
				SectorID = 0,
				Name = "GlobalMemoryTestAsteroid",
			});

			entity.gameObject.transform.position = new Vector3(0, 0, 0);
			entity.gameObject.transform.rotation = Quaternion.identity;
			entity.gameObject.SetActive(true);
			entity.chunkDimensions = chunkDimensions;

			var chunksTotal = chunkDimensions.x * chunkDimensions.y * chunkDimensions.z;
			entity.AllocateChunks(chunksTotal);
			entity.LoadInSector(0);

			SetupCrossChunkResolver(entity);

			// Generate chunks using the new global memory system
			GenerateChunksWithGlobalMemory(entity);

			// Build the mesh
			var chunkGenQueue = FindObjectOfType<ChunkGenerationQueue>();
			if (chunkGenQueue != null) {
				chunkGenQueue.RequestMeshRebuild(entity);
			}
			else {
				Debug.LogWarning("ChunkGenerationQueue not found - triggering immediate mesh rebuild");
				entity.RebuildMesh();
			}

			PrintMemoryStatistics("After generating first asteroid");
		}

		void GenerateChunksWithGlobalMemory(GameEntity entity) {
			var chunksTotal = entity.chunkCount;
			Debug.Log($"Generating {chunksTotal} chunks using GlobalChunkMemoryManager");

			// Generate all chunks using the global memory system
			for(var i = 0; i < chunksTotal; i++) {
				var chunkX = i % chunkDimensions.x;
				var chunkY = (i / chunkDimensions.x) % chunkDimensions.y;
				var chunkZ = i / (chunkDimensions.x * chunkDimensions.y);

				// Generate a unique chunk ID for this entity and chunk index
				long chunkID = GenerateChunkID(entity.id, i);

				// Allocate chunk in global memory system
				if (!ChunkMemoryManager.Instance.AllocateChunk(chunkID, entity.id, i)) {
					Debug.LogError($"Failed to allocate chunk {chunkID} in global memory!");
					continue;
				}

				// Create ChunkMemorySlice that references the global memory
				var chunkSlice = new ChunkData(chunkID);

				// Generate terrain data directly into global memory
				SeedBasedTerrainGenerator.GenerateChunk(
					chunkSlice,
					chunkX,
					chunkY,
					chunkZ,
					worldSeed,
					asteroidSettings
				);

				// Log generation info
				Debug.Log($"Generated chunk [{chunkX},{chunkY},{chunkZ}] with ID: {chunkID}");
			}

			Debug.Log($"Successfully generated {chunksTotal} chunks in global memory system");
		}

		long GenerateChunkID(int entityID, int chunkIndex) {
			// Generate a unique chunk ID based on entity ID and chunk index
			// Use a simple but effective combination that avoids collisions
			return ((long)entityID << 32) | (uint)chunkIndex;
		}

		void SetupCrossChunkResolver(GameEntity entity) {
			Debug.Log("Setting up cross-chunk resolver for global memory system");

			// Updated cross-chunk resolver that works with ChunkMemorySlice
			ChunkBuilder.ExternalBlockResolver = (currentChunk, lx, ly, lz) => {
				int s = IChunkData.ChunkSize;

				var currentSlice = (ChunkData)currentChunk;

				// Find which chunk this slice belongs to by searching entity chunks
				int currentChunkIndex = -1;
				for(int i = 0; i < entity.Chunks.Length; i++) {
					if (entity.Chunks[i]._chunkID == currentSlice._chunkID) {
						currentChunkIndex = i;
						break;
					}
				}

				if (currentChunkIndex == -1) {
					Debug.LogError($"Could not find chunk index for chunk ID {currentSlice._chunkID}");
					return 0;
				}

				// Calculate current chunk coordinates
				int cx = currentChunkIndex % chunkDimensions.x;
				int cy = (currentChunkIndex / chunkDimensions.x) % chunkDimensions.y;
				int cz = currentChunkIndex / (chunkDimensions.x * chunkDimensions.y);

				// Adjust coordinates and move to neighbor chunk if needed
				if (lx < 0) {
					cx--;
					lx = s - 1;
				}
				else if (lx >= s) {
					cx++;
					lx = 0;
				}
				if (ly < 0) {
					cy--;
					ly = s - 1;
				}
				else if (ly >= s) {
					cy++;
					ly = 0;
				}
				if (lz < 0) {
					cz--;
					lz = s - 1;
				}
				else if (lz >= s) {
					cz++;
					lz = 0;
				}

				// If outside world bounds, treat as air
				if (cx < 0 || cy < 0 || cz < 0 || cx >= chunkDimensions.x || cy >= chunkDimensions.y || cz >= chunkDimensions.z)
					return 0;

				int neighborIndex = cx + cy * chunkDimensions.x + cz * chunkDimensions.x * chunkDimensions.y;
				var neighborChunk = entity.Chunks[neighborIndex];
				int bi = neighborChunk.GetBlockIndex(new Vector3(lx, ly, lz));
				return neighborChunk.GetBlockType(bi);
			};
		}

		/// <summary>
		/// Test that the same seed produces identical results using global memory system
		/// </summary>
		IEnumerator TestDeterminismCoroutine() {
			yield return new WaitForSeconds(2f); // Wait for first asteroid to generate

			Debug.Log("=== Testing Determinism with Global Memory System ===");

			GameEntity entity2 = new GameObject("DeterminismTest").AddComponent<Asteroid>();
			entity2.LoadDataFromDB(new GameEntity.GameEntityData {
				Type = GameEntityType.Asteroid,
				FactionID = 0,
				SectorID = 0,
				Name = "DeterminismTest",
			});

			entity2.gameObject.transform.position = new Vector3(200, 0, 0); // Offset position
			entity2.gameObject.transform.rotation = Quaternion.identity;
			entity2.gameObject.SetActive(true);
			entity2.chunkDimensions = chunkDimensions;
			entity2.chunkCount = chunkDimensions.x * chunkDimensions.y * chunkDimensions.z;
			entity2.LoadInSector(0);

			// Generate using same seed but different entity ID
			GenerateChunksWithGlobalMemory(entity2);

			// Build mesh
			var chunkGenQueue = FindObjectOfType<ChunkGenerationQueue>();
			if (chunkGenQueue != null) {
				chunkGenQueue.RequestMeshRebuild(entity2);
			}
			else {
				entity2.RebuildMesh();
			}

			PrintMemoryStatistics("After generating second asteroid for determinism test");
			Debug.Log("Determinism test complete - check that both asteroids look identical!");
		}

		/// <summary>
		/// Simulate what would happen in a networked scenario with global memory
		/// </summary>
		[ContextMenu("Simulate Network Generation")]
		void SimulateNetworkGeneration() {
			Debug.Log("=== Simulating Network Generation with Global Memory ===");

			// Simulate what a client would receive from server
			var networkChunkData = new[] {
				// Natural chunks - just send seed and generate locally
				new NetworkChunkData {
					chunkX = 0, chunkY = 0, chunkZ = 0,
					seed = worldSeed,
					isModified = false,
					estimatedSize = IChunkData.ChunkSize * IChunkData.ChunkSize * IChunkData.ChunkSize * 4
				},
				new NetworkChunkData {
					chunkX = 1, chunkY = 0, chunkZ = 0,
					seed = worldSeed,
					isModified = false,
					estimatedSize = IChunkData.ChunkSize * IChunkData.ChunkSize * IChunkData.ChunkSize * 4
				},

				// Modified chunk - would receive compressed data from server
				new NetworkChunkData {
					chunkX = 2, chunkY = 0, chunkZ = 0,
					seed = -1,
					isModified = true,
					voxelData = new byte[32768], // Simulated compressed data
					estimatedSize = 32768
				}
			};

			foreach (var chunkData in networkChunkData) {
				if (chunkData.isModified) {
					Debug.Log($"Chunk [{chunkData.chunkX},{chunkData.chunkY},{chunkData.chunkZ}] is modified");
					Debug.Log($"  - Would receive {chunkData.voxelData.Length} bytes of compressed data");
					Debug.Log($"  - Would allocate chunk ID in global memory");
					Debug.Log($"  - Would decompress directly into global memory pool");
					Debug.Log($"  - Network bandwidth: {chunkData.estimatedSize} bytes");
				}
				else {
					long calculatedSeed = SeedBasedTerrainGenerator.GetChunkSeed(chunkData.chunkX, chunkData.chunkY, chunkData.chunkZ, chunkData.seed);
					Debug.Log($"Chunk [{chunkData.chunkX},{chunkData.chunkY},{chunkData.chunkZ}] - seed: {calculatedSeed}");
					Debug.Log($"  - Would generate locally using global memory system");
					Debug.Log($"  - Network bandwidth: 8 bytes (just the seed)");
					Debug.Log($"  - Local generation into global memory pool");
				}
			}
		}

		void PrintMemoryStatistics(string context) {
			if (ChunkMemoryManager.Instance != null) {
				ChunkMemoryManager.Instance.GetMemoryStatistics(out int uncompressed, out int compressed, out long totalMemory);
				Debug.Log($"[{context}] Memory Stats - Uncompressed: {uncompressed}, Compressed: {compressed}, Total: {totalMemory / 1024 / 1024}MB");
			}
		}

		void OnDestroy() {
			// Clean up any chunks we allocated
			var asteroids = FindObjectsOfType<GameEntity>();
			foreach (var entity in asteroids) {
				if (entity.Chunks != null) {
					for(int i = 0; i < entity.Chunks.Length; i++) {
						ChunkMemoryManager.Instance?.DeallocateChunk(entity.Chunks[i]._chunkID);
					}
				}
			}
		}
	}

	/// <summary>
	/// Network data structure for simulating chunk transmission
	/// </summary>
	public struct NetworkChunkData {
		public int chunkX, chunkY, chunkZ;
		public long seed;
		public bool isModified;
		public byte[] voxelData;
		public int estimatedSize;
	}
}