using System;
using Dev.Testing.Terrain;
using UnityEngine;
using Universe.Data.Chunk;
using Random = System.Random;

namespace Universe.Data.Generation {

	/// <summary>
	///     Generates voxel terrain deterministically from seeds.
	///     Each chunk gets a unique seed based on world coordinates and can regenerate identical data.
	/// </summary>
	public static class SeedBasedTerrainGenerator {

		/// <summary>
		///     Generate a unique seed for a chunk based on its coordinates and world seed
		/// </summary>
		public static long GetChunkSeed(int chunkX, int chunkY, int chunkZ, long worldSeed) {
			// Use a robust hash combination to ensure unique seeds
			long hash = worldSeed;
			hash = hash * 31 + chunkX;
			hash = hash * 31 + chunkY;
			hash = hash * 31 + chunkZ;

			// Additional mixing to improve distribution
			hash ^= hash >> 16;
			hash *= 0x85ebca6b;
			hash ^= hash >> 13;
			hash *= 0xc2b2ae35;
			hash ^= hash >> 16;

			return hash;
		}

		/// <summary>
		///     Generate chunk data from seed for asteroid-type terrain
		/// </summary>
		public static void GenerateAsteroidChunk(
			IChunkData chunkData,
			int chunkX, int chunkY, int chunkZ,
			long chunkSeed,
			AsteroidGenerationSettings settings) {

			// Initialize random with chunk seed
			Random random = new Random((int)(chunkSeed & 0x7FFFFFFF));

			// Calculate world position offsets for this chunk
			int worldOffsetX = chunkX * IChunkData.ChunkSize;
			int worldOffsetY = chunkY * IChunkData.ChunkSize;
			int worldOffsetZ = chunkZ * IChunkData.ChunkSize;

			// Generate blocks for this chunk
			for(int x = 0; x < IChunkData.ChunkSize; x++) {
				for(int y = 0; y < IChunkData.ChunkSize; y++) {
					for(int z = 0; z < IChunkData.ChunkSize; z++) {

						// World coordinates
						int worldX = worldOffsetX + x;
						int worldY = worldOffsetY + y;
						int worldZ = worldOffsetZ + z;

						// Block index within chunk
						int blockIndex = chunkData.GetBlockIndex(new Vector3(x, y, z));

						// Generate block type using asteroid generation logic
						short blockType = GenerateAsteroidBlock(
							worldX,
							worldY,
							worldZ,
							chunkSeed, // Use chunk seed for consistency
							settings
						);

						chunkData.SetBlockType(blockIndex, blockType);
					}
				}
			}
		}

		/// <summary>
		///     Generate a single block for asteroid terrain using the same logic as TerrainTest
		/// </summary>
		static short GenerateAsteroidBlock(
			int worldX, int worldY, int worldZ,
			long seed,
			AsteroidGenerationSettings settings) {

			// Calculate distance from asteroid center
			float distanceFromCenter = Mathf.Sqrt(
				Mathf.Pow(worldX - settings.centerX, 2) +
				Mathf.Pow(worldY - settings.centerY, 2) +
				Mathf.Pow(worldZ - settings.centerZ, 2)
			);

			// Add noise-based distortion to create irregular asteroid shape
			double distortion = Perlin.Noise(
				                    worldX * settings.noiseFrequency,
				                    worldY * settings.noiseFrequency,
				                    worldZ * settings.noiseFrequency
			                    ) *
			                    settings.shapeDistortion;

			float distortedRadius = settings.radius + (float)distortion;

			// Determine if this block should be solid
			if (distanceFromCenter <= distortedRadius) {
				// Near surface - add some variation
				if (distanceFromCenter > distortedRadius - settings.surfaceThickness) {
					double surfaceNoise = Perlin.Noise(
						worldX * settings.noiseFrequency * 2,
						worldY * settings.noiseFrequency * 2,
						worldZ * settings.noiseFrequency * 2
					);

					return distanceFromCenter <= distortedRadius - surfaceNoise * 5 ? (short)1 : (short)0;
				}
				// Interior - solid
				return 1;
			}
			// Outside asteroid - air
			return 0;
		}

		/// <summary>
		///     Generate chunk data for any terrain type based on settings
		/// </summary>
		public static void GenerateChunk(
			IChunkData chunkData,
			int chunkX, int chunkY, int chunkZ,
			long worldSeed,
			ITerrainGenerationSettings settings) {

			long chunkSeed = GetChunkSeed(chunkX, chunkY, chunkZ, worldSeed);

			switch (settings.TerrainType) {
				case TerrainType.Asteroid:
					GenerateAsteroidChunk(chunkData, chunkX, chunkY, chunkZ, chunkSeed, (AsteroidGenerationSettings)settings);
					break;

				case TerrainType.Planet:
					// TODO: Implement planet generation
					GeneratePlanetChunk(chunkData, chunkX, chunkY, chunkZ, chunkSeed, (PlanetGenerationSettings)settings);
					break;

				case TerrainType.Empty:
				default:
					// Generate empty chunk (all air blocks)
					GenerateEmptyChunk(chunkData);
					break;
			}
		}

		static void GenerateEmptyChunk(IChunkData chunkData) {
			// Set all blocks to air (type 0)
			int totalBlocks = IChunkData.ChunkSize * IChunkData.ChunkSize * IChunkData.ChunkSize;
			for(int i = 0; i < totalBlocks; i++) {
				chunkData.SetBlockType(i, 0);
			}
		}

		static void GeneratePlanetChunk(
			IChunkData chunkData,
			int chunkX, int chunkY, int chunkZ,
			long chunkSeed,
			PlanetGenerationSettings settings) {

			// TODO: Implement planet terrain generation
			// For now, generate empty chunks
			GenerateEmptyChunk(chunkData);
		}
	}

	/// <summary>
	///     Base interface for terrain generation settings
	/// </summary>
	public interface ITerrainGenerationSettings {
		TerrainType TerrainType { get; }
	}

	/// <summary>
	///     Settings for asteroid generation - matches your current TerrainTest parameters
	/// </summary>
	[Serializable]
	public class AsteroidGenerationSettings : ITerrainGenerationSettings {

		[Header("Asteroid Shape")]
		public float radius = 40f;
		public float centerX = 48f; // Center of 3x3x3 chunks (96/2)
		public float centerY = 48f;
		public float centerZ = 48f;

		[Header("Surface Generation")]
		public float noiseFrequency = 0.035f;
		public float surfaceThickness = 10f;
		public float shapeDistortion = 10f;

		public TerrainType TerrainType {
			get => TerrainType.Asteroid;
		}

		/// <summary>
		///     Create settings that match your current TerrainTest configuration
		/// </summary>
		public static AsteroidGenerationSettings CreateDefault(Vector3Int chunkDimensions) {
			return new AsteroidGenerationSettings {
				radius = Mathf.Min(chunkDimensions.x, Mathf.Min(chunkDimensions.y, chunkDimensions.z)) * IChunkData.ChunkSize / 2f - 10f,
				centerX = chunkDimensions.x * IChunkData.ChunkSize / 2f,
				centerY = chunkDimensions.y * IChunkData.ChunkSize / 2f,
				centerZ = chunkDimensions.z * IChunkData.ChunkSize / 2f,
				noiseFrequency = 0.035f,
				surfaceThickness = 10f,
				shapeDistortion = 10f,
			};
		}
	}

	/// <summary>
	///     Settings for planet generation (placeholder for future implementation)
	/// </summary>
	[Serializable]
	public class PlanetGenerationSettings : ITerrainGenerationSettings {

		// TODO: Add planet-specific generation parameters
		public float planetRadius = 1000f;
		public float surfaceLevel;
		public float noiseScale = 0.01f;

		public TerrainType TerrainType {
			get => TerrainType.Planet;
		}
	}

	public enum TerrainType {
		Empty,
		Asteroid,
		Planet,
		Custom,
	}
}