using Networking.Entity;
using UnityEngine;
using Universe.Data.Chunk;
using Universe.Data.GameEntity;
using Universe.Data.Generation;

namespace Dev.Testing.Terrain {

	/// <summary>
	/// Updated TerrainTest that uses seed-based generation instead of hardcoded generation
	/// </summary>
	public class SeedBasedTerrainTest : MonoBehaviour {

		[Header("World Settings")]
		[Tooltip("Seed for deterministic world generation")]
		public long worldSeed = 12345;

		[Header("Generation Settings")]
		public Vector3Int chunkDimensions = new(3, 3, 3);

		[Header("Asteroid Settings")]
		public AsteroidGenerationSettings asteroidSettings;

		[Header("Testing")]
		[Tooltip("If true, generates the same asteroid twice to verify determinism")]
		public bool testDeterminism = true;

		void Start() {
			// Initialize asteroid settings to match your current TerrainTest defaults
			if (asteroidSettings == null) {
				asteroidSettings = AsteroidGenerationSettings.CreateDefault(chunkDimensions);
			}

			GenerateTestAsteroid();

			// Test determinism by generating the same thing twice
			if (testDeterminism) {
				StartCoroutine(TestDeterminismCoroutine());
			}
		}

		void GenerateTestAsteroid() {
			// Create the game entity (same as your original TerrainTest)
			GameEntity entity = new GameObject("SeedBasedTestAsteroid").AddComponent<Asteroid>();
			entity.LoadDataFromDB(new GameEntity.GameEntityData {
				Type = GameEntityType.Asteroid,
				FactionID = 0,
				SectorID = 0,
				Name = "SeedBasedTestAsteroid",
			});

			//Add LODAutoRegister
			entity.gameObject.AddComponent<Graphics.LOD.LODAutoRegister>();

			entity.gameObject.transform.position = new Vector3(0, 0, 0);
			entity.gameObject.transform.rotation = Quaternion.identity;
			entity.gameObject.SetActive(true);
			entity.chunkDimensions = chunkDimensions;

			var chunksTotal = chunkDimensions.x * chunkDimensions.y * chunkDimensions.z;
			entity.TotalChunks = chunksTotal;
			entity.LoadInSector(0);

			// Set up cross-chunk block resolver (same as original)
			SetupCrossChunkResolver(entity);

			// Generate chunks using seed-based generation
			GenerateChunksFromSeeds(entity);

			// Build the mesh
			var chunkGenQueue = FindObjectOfType<ChunkGenerationQueue>();
			chunkGenQueue.RequestMeshRebuild(entity);
		}

		void GenerateChunksFromSeeds(GameEntity entity) {
			var chunksTotal = entity.TotalChunks;

			// Generate all chunks from seeds
			for(var i = 0; i < chunksTotal; i++) {
				var chunkX = i % chunkDimensions.x;
				var chunkY = (i / chunkDimensions.x) % chunkDimensions.y;
				var chunkZ = i / (chunkDimensions.x * chunkDimensions.y);

				// Create chunk data
				unsafe {
					var chunkData = new ChunkDataV8(i);

					// Generate using seed-based generation
					SeedBasedTerrainGenerator.GenerateChunk(
						chunkData,
						chunkX,
						chunkY,
						chunkZ,
						worldSeed,
						asteroidSettings
					);

					entity.Chunks[i] = new Chunk { Data = chunkData };
				}

				// Log the seed for this chunk for debugging
				long chunkSeed = SeedBasedTerrainGenerator.GetChunkSeed(chunkX, chunkY, chunkZ, worldSeed);
				Debug.Log($"Generated chunk [{chunkX},{chunkY},{chunkZ}] with seed: {chunkSeed}");
			}
		}

		void SetupCrossChunkResolver(GameEntity entity) {
			// Same cross-chunk resolver logic as original TerrainTest
			ChunkBuilder.ExternalBlockResolver = (currentChunk, lx, ly, lz) => {
				int s = Chunk.ChunkSize;
				var cd = (ChunkDataV8)currentChunk;
				int ci = (int)cd.Index;
				int cx = ci % chunkDimensions.x;
				int cy = (ci / chunkDimensions.x) % chunkDimensions.y;
				int cz = ci / (chunkDimensions.x * chunkDimensions.y);

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
				var neighborChunk = entity.Chunks[neighborIndex].Data;
				int bi = neighborChunk.GetBlockIndex(new Vector3(lx, ly, lz));
				return neighborChunk.GetBlockType(bi);
			};
		}

		/// <summary>
		/// Test that the same seed produces identical results
		/// </summary>
		System.Collections.IEnumerator TestDeterminismCoroutine() {
			yield return new WaitForSeconds(2f); // Wait for first asteroid to generate

			Debug.Log("=== Testing Determinism ===");

			// Generate second asteroid at different position with same seed
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
			entity2.TotalChunks = chunkDimensions.x * chunkDimensions.y * chunkDimensions.z;
			entity2.LoadInSector(0);

			// Generate using same seed
			GenerateChunksFromSeeds(entity2);

			// Build mesh
			var chunkGenQueue = FindObjectOfType<ChunkGenerationQueue>();
			chunkGenQueue.RequestMeshRebuild(entity2);

			Debug.Log("Determinism test complete - check that both asteroids look identical!");
		}

		/// <summary>
		/// Simulate what would happen in a networked scenario
		/// </summary>
		[ContextMenu("Simulate Network Generation")]
		void SimulateNetworkGeneration() {
			Debug.Log("=== Simulating Network Generation ===");

			// Simulate what a client would receive from server
			var networkChunkData = new[] {
				// Natural chunks - just send seed
				new NetworkChunkData { chunkX = 0, chunkY = 0, chunkZ = 0, seed = worldSeed, isModified = false },
				new NetworkChunkData { chunkX = 1, chunkY = 0, chunkZ = 0, seed = worldSeed, isModified = false },

				// Modified chunk - send full data (simulated)
				new NetworkChunkData { chunkX = 2, chunkY = 0, chunkZ = 0, seed = -1, isModified = true, voxelData = new byte[0] }
			};

			foreach (var chunkData in networkChunkData) {
				if (chunkData.isModified) {
					Debug.Log($"Chunk [{chunkData.chunkX},{chunkData.chunkY},{chunkData.chunkZ}] is modified - would receive {chunkData.voxelData.Length} bytes of data");
				}
				else {
					long calculatedSeed = SeedBasedTerrainGenerator.GetChunkSeed(chunkData.chunkX, chunkData.chunkY, chunkData.chunkZ, chunkData.seed);
					Debug.Log($"Chunk [{chunkData.chunkX},{chunkData.chunkY},{chunkData.chunkZ}] - seed: {calculatedSeed} (bandwidth: 8 bytes)");
				}
			}
		}
	}
}