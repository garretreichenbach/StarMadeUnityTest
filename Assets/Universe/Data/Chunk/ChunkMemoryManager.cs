using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Threading;
using System.Threading.Tasks;
using Dev;
using Settings;
using Unity.Burst;
using Unity.Collections;
using Unity.Jobs;
using UnityEngine;
using UnityEngine.Rendering;
using Universe.Data.Common;
using Debug = UnityEngine.Debug;

namespace Universe.Data.Chunk {
	[Serializable]
	public class ChunkMemoryManager : StatsDisplay.IStatsDisplayReporter {
		// Semaphore to serialize access to GPU compression/decompression resources
		SemaphoreSlim _gpuMutex;

		float ChunkOperationTimeout {
			get => EngineSettings.Instance.MaxChunkOperationWaitTime.Value;
		}

		// Waits for a chunk's GPU operation (compression or decompression) to complete.
		// Returns true if the operation completed successfully, false otherwise.
		void WaitForChunkOperation(long chunkID) {
			if(!_activeOperations.TryGetValue(chunkID, out CompressionOperation operation)) {
				return;
			}
			// Wait for the TaskCompletionSource to complete (blocking)
			try {
				operation.CompletionSource.Task.Wait(TimeSpan.FromSeconds(ChunkOperationTimeout));
			} catch(Exception ex) {
				Debug.LogError($"Error waiting for chunk operation: {ex.Message}");
			}
		}

		#region Diagnostic Tests

		/**
		 * Diagnostic: Run a compression -> decompression round-trip on a single chunk and verify data integrity.
		 * Returns true if round-trip preserves the chunk's raw data.
		 */
		public async Task<bool> TestCompressionRoundTrip(long chunkID) {
			if(!Allocations.TryGetValue(chunkID, out ChunkAllocation alloc)) {
				Debug.LogError($"[TestCompressionRoundTrip] Chunk {chunkID} not allocated");
				return false;
			}

			if(alloc.State != ChunkState.Uncompressed) {
				Debug.LogError($"[TestCompressionRoundTrip] Chunk {chunkID} must be uncompressed to run test (state={alloc.State})");
				return false;
			}

			// Capture original data
			int[] original = GetRawDataArray(chunkID);

			// Compress
			bool compOk = await CompressChunk(chunkID);
			if(!compOk) {
				Debug.LogError($"[TestCompressionRoundTrip] Compression failed for chunk {chunkID}");
				return false;
			}

			// Ensure state is compressed
			if(!Allocations.TryGetValue(chunkID, out alloc) || alloc.State != ChunkState.Compressed) {
				Debug.LogError($"[TestCompressionRoundTrip] Chunk {chunkID} not in compressed state after compression (state={(Allocations.TryGetValue(chunkID, out alloc) ? alloc.State.ToString() : "missing")})");
				return false;
			}

			// Decompress
			bool decompOk = await DecompressChunk(chunkID);
			if(!decompOk) {
				Debug.LogError($"[TestCompressionRoundTrip] Decompression failed for chunk {chunkID}");
				return false;
			}

			// Readback decompressed data
			int[] restored = GetRawDataArray(chunkID);

			// Compare
			if(restored == null || original == null || restored.Length != original.Length) {
				Debug.LogError($"[TestCompressionRoundTrip] Data length mismatch for chunk {chunkID}: original={original?.Length ?? -1}, restored={restored?.Length ?? -1}");
				return false;
			}

			int mismatches = 0;
			for(int i = 0; i < original.Length; i++) {
				if(original[i] != restored[i]) {
					mismatches++;
					if(mismatches <= 10) Debug.LogError($"[TestCompressionRoundTrip] Mismatch at index {i}: original={original[i]}, restored={restored[i]}");
				}
			}

			if(mismatches == 0) {
				Debug.Log($"[TestCompressionRoundTrip] Chunk {chunkID} compression/decompression round-trip successful");
				return true;
			}
			Debug.LogError($"[TestCompressionRoundTrip] Chunk {chunkID} round-trip failed with {mismatches} mismatches");
			return false;
		}

		#endregion

		// Public helpers for tools
		public long[] GetAllChunkIDs() {
			var keys = new List<long>();
			foreach(var kvp in Allocations) {
				keys.Add(kvp.Key);
			}
			return keys.ToArray();
		}

		public bool TryGetHeader(long chunkID, out ChunkHeader header) {
			if(Headers.TryGetValue(chunkID, out header)) return true;
			header = default;
			return false;
		}

		#region Jobs for Performance

		[BurstCompile]
		struct ClearMemoryJob : IJobParallelFor {
			public NativeArray<int> Data;

			public void Execute(int index) { Data[index] = 0; }
		}

		#endregion

		#region Memory Pool Configuration

		// Memory pool sizes (configurable in inspector)
		[Header("Memory Configuration")] [SerializeField]
		int maxUncompressedChunks = 1024; // ~32MB for 1024 chunks

		[SerializeField] int maxCompressedChunks = 4096; // Variable size pool

		// Each uncompressed chunk: 32³ blocks × 4 bytes = 131,072 bytes (128KB)
		const int UncompressedChunkSize = 32 * 32 * 32 * sizeof(int);
		const int BlocksPerChunk = 32 * 32 * 32;

		#endregion

		#region Core Memory Pools

		// Main uncompressed data pool - sequential 128KB chunks
		public NativeArray<int> UncompressedPool;

		// Compressed data pool - variable sized allocations
		public NativeArray<byte> CompressedPool;
		public int compressedPoolHead; // Next allocation offset

		// Chunk metadata and allocation tracking
		public NativeHashMap<long, ChunkAllocation> Allocations; // ChunkID -> Allocation info
		public NativeHashMap<long, ChunkHeader> Headers; // ChunkID -> Header info

		// Free slot management
		public Queue<int> FreeUncompressedSlots;
		Queue<CompressedSlot> _freeCompressedSlots; // Size-based free slots

		#endregion

		#region GPU Resources

		public ChunkCompressionManager CompressionManager;

		[Header("GPU Compression")] [SerializeField]
		public ComputeShader compressionShader;

		[SerializeField] public ComputeShader decompressionShader;

		// GPU buffers for compression operations
		public ComputeBuffer GPUInputBuffer; // Input data for compression
		public ComputeBuffer GPUOutputBuffer; // Output data from compression
		public ComputeBuffer GPUMetadataBuffer; // Compression metadata

		// Async operation tracking
		Dictionary<long, CompressionOperation> _activeOperations;

		#endregion

		#region Data Structures

		public struct ChunkAllocation {
			public long ChunkID;
			public int PoolIndex; // Index in uncompressed pool (-1 if compressed)
			public int CompressedOffset; // Offset in compressed pool (-1 if uncompressed)
			public int CompressedSize; // Size in compressed pool (0 if uncompressed)
			public ChunkState State;
			public float LastAccessTime;
			public int EntityID; // Which entity owns this chunk
			public int ChunkIndexInEntity; // Index within the entity's chunk array
		}

		/**
		* Metadata header for each chunk, tracking state, timestamps, and integrity info.
		*/
		public struct ChunkHeader {
			public long ChunkID;
			public ChunkState State;
			public bool IsDirty;
			public byte CompressionLevel; // 0 = uncompressed, 1-9 = compression levels
			public int OriginalSize; // Always UNCOMPRESSED_CHUNK_SIZE
			public int CompressedSize; // Size when compressed
			public uint Checksum; // Data integrity check
		}

		/**
		* Represents a free slot in the compressed memory pool.
		*/
		public struct CompressedSlot {
			public int Offset;
			public int Size;
		}

		/**
		* Represents a compressed chunk's metadata within the compressed memory pool.
		*/
		public struct CompressedChunk {
			public long ChunkID;
			public int Offset;
			public int Size;
		}

		/**
		* Represents the current state of a chunk in memory, including whether it is unallocated, uncompressed, etc.
		*/
		public enum ChunkState {
			Unallocated,
			Uncompressed,
			Compressed,
			GPUCompressing,
			GPUDecompressing,
			Error,
		}

		/**
		* Tracks an ongoing compression or decompression operation for a chunk.
		*/
		public struct CompressionOperation {
			public long ChunkID;
			public ChunkState TargetState;
			public TaskCompletionSource<bool> CompletionSource;
			public AsyncGPUReadbackRequest? ReadbackRequest;
			public float StartTime;
		}

		#endregion

		#region Initialization

		GameState _state;

		public ChunkMemoryManager(GameState state) {
			_state = state;
			FreeUncompressedSlots = new Queue<int>();
			CompressionManager = new ChunkCompressionManager(_state);
			InitializeMemoryPools();
			InitializeGPUResources();
			// Initialize GPU mutex for parallelism (match buffer pool size)
			_gpuMutex = new SemaphoreSlim(EngineSettings.Instance.GPUCompressionBufferPoolSize.Value);
		}

		void InitializeMemoryPools() {
			// Allocate main uncompressed pool
			int totalUncompressedInts = maxUncompressedChunks * BlocksPerChunk;
			UncompressedPool = new NativeArray<int>(totalUncompressedInts, Allocator.Persistent);

			// Allocate compressed pool (estimate 4:1 compression ratio)
			int estimatedCompressedSize = maxCompressedChunks * UncompressedChunkSize / 4;
			CompressedPool = new NativeArray<byte>(estimatedCompressedSize, Allocator.Persistent);

			// Initialize tracking structures
			Allocations = new NativeHashMap<long, ChunkAllocation>(maxUncompressedChunks + maxCompressedChunks, Allocator.Persistent);
			Headers = new NativeHashMap<long, ChunkHeader>(maxUncompressedChunks + maxCompressedChunks, Allocator.Persistent);

			// Initialize free slot queues
			FreeUncompressedSlots = new Queue<int>();
			for(int i = 0; i < maxUncompressedChunks; i++) {
				FreeUncompressedSlots.Enqueue(i);
			}

			_freeCompressedSlots = new Queue<CompressedSlot>();
			compressedPoolHead = 0;
			_activeOperations = new Dictionary<long, CompressionOperation>();

			Debug.Log($"GlobalChunkMemoryManager initialized: {totalUncompressedInts * sizeof(int) / 1024 / 1024}MB uncompressed, {estimatedCompressedSize / 1024 / 1024}MB compressed");
		}

		void InitializeGPUResources() {
			if(compressionShader == null || decompressionShader == null) {
				Debug.LogError("Compression shaders not assigned! GPU compression disabled.");
				return;
			}
			int batchSize = EngineSettings.Instance.GPUCompressionBatchSize.Value;
			// Create GPU buffers for batch compression operations
			GPUInputBuffer = new ComputeBuffer(batchSize * BlocksPerChunk, sizeof(int));
			// Output buffer is used as a ByteAddressBuffer in the compute shader, so create it as a raw buffer
			GPUOutputBuffer = new ComputeBuffer(batchSize * BlocksPerChunk, sizeof(int), ComputeBufferType.Raw); // Max size (byte-addressable)
			GPUMetadataBuffer = new ComputeBuffer(batchSize, sizeof(int) * 4); // Per-chunk metadata (structured)
		}

		#endregion

		#region Core Memory Operations

		public bool AllocateChunk(long chunkID, int entityID = -1, int chunkIndex = -1) {
			// If chunk already exists, forcibly deallocate it first
			if(Allocations.ContainsKey(chunkID)) {
				Debug.LogWarning($"[AllocateChunk] Chunk {chunkID} already allocated. Forcibly deallocating before re-allocation.");
				DeallocateChunk(chunkID);
			}

			if(FreeUncompressedSlots.Count == 0) {
				// Try to compress some chunks to free up space
				if(!TryFreeUncompressedSlot()) {
					Debug.LogError("No free uncompressed slots available!");
					return false;
				}
			}

			int poolIndex = FreeUncompressedSlots.Dequeue();

			ChunkAllocation allocation = new ChunkAllocation {
				ChunkID = chunkID,
				PoolIndex = poolIndex,
				CompressedOffset = -1,
				CompressedSize = 0,
				State = ChunkState.Uncompressed,
				LastAccessTime = Time.time,
				EntityID = entityID,
				ChunkIndexInEntity = chunkIndex,
			};

			ChunkHeader header = new ChunkHeader {
				ChunkID = chunkID,
				State = ChunkState.Uncompressed,
				IsDirty = false,
				CompressionLevel = 0,
				OriginalSize = UncompressedChunkSize,
				CompressedSize = 0,
				Checksum = 0,
			};

			Allocations.Add(chunkID, allocation);
			Headers.Add(chunkID, header);

			// Zero out the allocated memory
			ClearChunkMemory(poolIndex);

			return true;
		}

		public void DeallocateChunk(long chunkID) {
			if(!Allocations.TryGetValue(chunkID, out ChunkAllocation allocation)) {
				Debug.Log($"[DeallocateChunk] Chunk {chunkID} not found in allocations.");
				return;
			}

			// Cancel any active operations on this chunk
			if(_activeOperations.ContainsKey(chunkID)) {
				Debug.Log($"[DeallocateChunk] Cancelling active operation for chunk {chunkID} (state: {allocation.State})");
				_activeOperations[chunkID].CompletionSource?.SetCanceled();
				_activeOperations.Remove(chunkID);
			}

			// If chunk is in GPUDecompressing or GPUCompressing, forcibly reset state
			if(allocation.State == ChunkState.GPUDecompressing || allocation.State == ChunkState.GPUCompressing) {
				Debug.LogWarning($"[DeallocateChunk] Chunk {chunkID} was in {allocation.State} state. Forcibly resetting to Uncompressed before removal.");
				allocation.State = ChunkState.Uncompressed;
				Allocations[chunkID] = allocation; // <-- Ensure dictionary is updated
			}

			// Free the appropriate slot
			if(allocation.State == ChunkState.Uncompressed) {
				if(allocation.PoolIndex >= 0) {
					FreeUncompressedSlots.Enqueue(allocation.PoolIndex);
				}
			}

			if(allocation.State == ChunkState.Compressed) {
				if(allocation.CompressedOffset >= 0 && allocation.CompressedSize > 0) {
					_freeCompressedSlots.Enqueue(new CompressedSlot {
						Offset = allocation.CompressedOffset,
						Size = allocation.CompressedSize,
					});
				}
			}

			Allocations.Remove(chunkID);
			Headers.Remove(chunkID);
			Debug.Log($"[DeallocateChunk] Chunk {chunkID} fully deallocated.");
		}

		void ClearChunkMemory(int poolIndex) {
			int startIndex = poolIndex * BlocksPerChunk;
			var slice = UncompressedPool.GetSubArray(startIndex, BlocksPerChunk);

			// Use a job to clear memory efficiently
			ClearMemoryJob clearJob = new ClearMemoryJob {
				Data = slice,
			};
			clearJob.Schedule(BlocksPerChunk, 1024).Complete();
		}

		#endregion

		#region Block Access Methods

		public int GetRawData(long chunkID, int blockIndex) {
			if(!Allocations.TryGetValue(chunkID, out ChunkAllocation allocation)) {
				throw new KeyNotFoundException($"Chunk {chunkID} not allocated");
			}
			if(allocation.State != ChunkState.Uncompressed) {
				// Caller should ensure chunk is uncompressed. Return 0 (air) as fallback.
				return 0;
			}
			int globalIndex = allocation.PoolIndex * BlocksPerChunk + blockIndex;
			return UncompressedPool[globalIndex];
		}

		public void SetRawData(long chunkID, int blockIndex, int rawData) {
			ChunkAllocation allocation = Allocations[chunkID];
			MarkChunkDirty(chunkID);
			int globalIndex = allocation.PoolIndex * BlocksPerChunk + blockIndex;
			UncompressedPool[globalIndex] = rawData;
		}

		public int[] GetRawDataArray(long chunkID) {
			if(!Allocations.TryGetValue(chunkID, out ChunkAllocation allocation)) {
				throw new KeyNotFoundException($"Chunk {chunkID} not allocated");
			}
			if(allocation.State != ChunkState.Uncompressed) {
				return Array.Empty<int>();
			}
			int startIndex = allocation.PoolIndex * BlocksPerChunk;
			var slice = UncompressedPool.GetSubArray(startIndex, BlocksPerChunk);
			return slice.ToArray();
		}

		public void SetRawDataArray(long chunkID, int[] data) {
			if(data.Length != BlocksPerChunk) {
				throw new ArgumentException($"Data array length {data.Length} does not match chunk size {BlocksPerChunk}");
			}
			if(!Allocations.TryGetValue(chunkID, out ChunkAllocation allocation)) {
				throw new KeyNotFoundException($"Chunk {chunkID} not allocated");
			}
			if(allocation.State != ChunkState.Uncompressed) {
				throw new InvalidOperationException($"Chunk {chunkID} is not uncompressed");
			}
			MarkChunkDirty(chunkID);
			int startIndex = allocation.PoolIndex * BlocksPerChunk;
			var slice = UncompressedPool.GetSubArray(startIndex, BlocksPerChunk);
			slice.CopyFrom(data);
		}

		#endregion

		#region Compression Management

		public async Task<bool> CompressChunk(long chunkID) {
			if(!Allocations.TryGetValue(chunkID, out ChunkAllocation allocation)) {
				return false;
			}

			if(allocation.State != ChunkState.Uncompressed) {
				return false;
			}

			allocation.State = ChunkState.GPUCompressing;
			Allocations[chunkID] = allocation;

			CompressionOperation operation = new CompressionOperation {
				ChunkID = chunkID,
				TargetState = ChunkState.Compressed,
				CompletionSource = new TaskCompletionSource<bool>(),
				StartTime = Time.time,
			};

			_activeOperations[chunkID] = operation;

			ChunkCompressionManager compressionManager = CompressionManager;
			if(compressionManager != null) {
				try {
					await _gpuMutex.WaitAsync();
					Stopwatch sw = Stopwatch.StartNew();
					try {
						await compressionManager.CompressChunk(chunkID);
						sw.Stop();
						operation.CompletionSource.SetResult(true);
					} finally {
						_gpuMutex.Release();
					}
				} catch(Exception ex) {
					Debug.LogError($"GPU compression failed: {ex.Message}");
					operation.CompletionSource.SetException(ex);
				}
			} else {
				Debug.LogError("No GPUCompressionManager instance for compression");
				operation.CompletionSource.SetResult(false);
			}
			return await operation.CompletionSource.Task;
		}

		public async Task<bool> DecompressChunk(long chunkID) {
			if(!Allocations.TryGetValue(chunkID, out ChunkAllocation allocation)) {
				return false;
			}
			// Prevent re-entrancy: don't decompress if already decompressing
			if(allocation.State == ChunkState.GPUDecompressing) {
				Debug.LogWarning($"Attempted to decompress chunk {chunkID} but it is already in GPUDecompressing state.");
				return false;
			}
			if(allocation.State != ChunkState.Compressed) {
				Debug.LogError($"[DecompressChunk] Chunk {chunkID} is not in Compressed state, but {allocation.State}");
				return false; // Can only decompress compressed chunks
			}
			// Do NOT set state to GPUDecompressing here!
			CompressionOperation operation = new CompressionOperation {
				ChunkID = chunkID,
				TargetState = ChunkState.Uncompressed,
				CompletionSource = new TaskCompletionSource<bool>(),
				StartTime = Time.time,
			};
			_activeOperations[chunkID] = operation;
			ChunkCompressionManager compressionManager = CompressionManager;
			if(compressionManager != null) {
				try {
					// Serialize GPU access to avoid concurrent buffer reuse
					await _gpuMutex.WaitAsync();
					try {
						await compressionManager.DecompressChunk(chunkID);
						// After decompression, verify state
						allocation = Allocations[chunkID];
						if(allocation.State == ChunkState.Uncompressed) {
							operation.CompletionSource.SetResult(true);
						} else {
							Debug.LogError($"Chunk {chunkID} state invalid after decompression: {allocation.State}");
							allocation.State = ChunkState.Compressed;
							Allocations[chunkID] = allocation;
							operation.CompletionSource.SetException(new Exception("Invalid chunk state after decompression"));
						}
					} finally {
						_gpuMutex.Release();
					}
				} catch(Exception ex) {
					Debug.LogError($"GPU decompression failed: {ex.Message}, ChunkState: {allocation.State}");
					// Reset state so chunk can be retried
					if(Allocations.TryGetValue(chunkID, out ChunkAllocation failedAlloc)) {
						failedAlloc.State = ChunkState.Compressed;
						Allocations[chunkID] = failedAlloc;
						Debug.LogWarning($"[DecompressChunk] Chunk {chunkID} state forcibly reset to Compressed after failure.");
					}
					operation.CompletionSource.SetException(ex);
				}
			} else {
				Debug.LogError("No GPUCompressionManager instance for decompression");
				operation.CompletionSource.SetResult(false);
			}
			return await operation.CompletionSource.Task;
		}

		#endregion

		#region Utility Methods

		void MarkChunkDirty(long chunkID) {
			if(Headers.TryGetValue(chunkID, out ChunkHeader header)) {
				header.IsDirty = true;
				Headers[chunkID] = header;
			}
		}

		bool TryFreeUncompressedSlot() {
			//Todo: Unity bitches about time.time in async methods, so we had to refactor this to not use it
			long oldestChunkID = -1;

			foreach(var kvp in Headers) {
				if(Allocations.TryGetValue(kvp.Key, out ChunkAllocation allocation) && allocation.State == ChunkState.Uncompressed) {
					oldestChunkID = kvp.Key;
					break; // Just pick the first uncompressed chunk we find
				}
			}

			if(oldestChunkID != -1) {
				// Start async compression of oldest chunk
				_ = CompressChunk(oldestChunkID);
				return true;
			}

			return false;
		}

		/**
		* Decompresses an entity's chunk data from a byte array.
		*/
		public async Task<bool> DecompressEntity(GameEntity.GameEntity gameEntity, byte[] rawCompressedData) {
			MemoryStream entityData = new MemoryStream(rawCompressedData);
			var decompressionTasks = new List<Task<bool>>();
			int chunksTotal = gameEntity.ChunkCount;
			int chunksRead = 0;
			int chunksFailed = 0;

			// Ensure the Chunks array is allocated and initialized to invalid
			if(gameEntity.Chunks == null || gameEntity.Chunks.Length != gameEntity.ChunkCount) {
				gameEntity.Chunks = new ChunkData[gameEntity.ChunkCount];
				for(int i = 0; i < gameEntity.Chunks.Length; i++) {
					gameEntity.Chunks[i] = new ChunkData(ChunkData.InvalidChunkID);
				}
			}

			while(entityData.Position < entityData.Length && chunksRead < chunksTotal) {
				// Read chunk index (new: 4 bytes)
				byte[] chunkIndexBytes = new byte[sizeof(int)];
				entityData.Read(chunkIndexBytes, 0, sizeof(int));
				int chunkIndex = BitConverter.ToInt32(chunkIndexBytes, 0);

				// Read chunk ID
				byte[] chunkIDBytes = new byte[sizeof(long)];
				entityData.Read(chunkIDBytes, 0, sizeof(long));
				long chunkID = BitConverter.ToInt64(chunkIDBytes, 0);

				// Read compressed size
				byte[] sizeBytes = new byte[sizeof(int)];
				entityData.Read(sizeBytes, 0, sizeof(int));
				int compressedSize = BitConverter.ToInt32(sizeBytes, 0);

				// Read compressed data
				byte[] compressedData = new byte[compressedSize];
				entityData.Read(compressedData, 0, compressedSize);

				chunksRead++;

				// Allocate chunk if not already allocated
				if(!Allocations.ContainsKey(chunkID)) {
					if(!AllocateChunk(chunkID, gameEntity.EntityID, chunkIndex)) {
						Debug.LogError($"Failed to allocate chunk {chunkID} for entity {gameEntity.Name}");
						chunksFailed++;
						continue;
					}
				}

				if(!Allocations.TryGetValue(chunkID, out ChunkAllocation allocation)) {
					Debug.LogError($"Chunk {chunkID} allocation missing after allocation");
					chunksFailed++;
					continue;
				}

				if(allocation.State == ChunkState.Uncompressed) {
					// Already uncompressed, skip
					if(!gameEntity.Chunks[chunkIndex].IsValid) {
						gameEntity.Chunks[chunkIndex] = new ChunkData(chunkID, allocation.PoolIndex);
					}
					continue;
				}

				// Decompress chunk (add task to list, do not await here)
				var decompressTask = DecompressChunk(chunkID).ContinueWith(task => {
					bool decompressed = task.Status == TaskStatus.RanToCompletion && task.Result;
					if(decompressed) {
						if(Allocations.TryGetValue(chunkID, out ChunkAllocation alloc) && alloc.PoolIndex >= 0 && chunkIndex >= 0 && chunkIndex < gameEntity.Chunks.Length) {
							gameEntity.Chunks[chunkIndex] = new ChunkData(chunkID, alloc.PoolIndex);
						}
					} else {
						Debug.LogError($"Failed to decompress chunk {chunkID} for entity {gameEntity.Name}");
						chunksFailed++;
					}
					return decompressed;
				});
				decompressionTasks.Add(decompressTask);
			}

			// Wait for all decompression tasks to complete after starting them all
			if(decompressionTasks.Count > 0) {
				await Task.WhenAll(decompressionTasks);
			}

			if(chunksFailed > 0) {
				Debug.LogError($"Decompression completed with {chunksFailed} failed chunks for entity {gameEntity.Name}");
			}

			// Return true if all chunks were read and decompressed successfully
			return chunksRead == chunksTotal && chunksFailed == 0;
		}

		/**
		* Compresses all chunks belonging to the specified entity and returns the compressed data as a byte array so it can be written to disk.
		*/
		public async Task<byte[]> CompressEntity(GameEntity.GameEntity entity) {
			MemoryStream entityData = new MemoryStream();
			var compressionTasks = new List<Task<bool>>();

			// First, start compression tasks for all uncompressed chunks
			for(int i = 0; i < entity.Chunks.Length; i++) {
				ChunkData chunk = entity.Chunks[i];
				if(!Allocations.TryGetValue(chunk._chunkID, out ChunkAllocation allocation)) {
					continue;
				}

				if(allocation.State == ChunkState.Uncompressed) {
					compressionTasks.Add(CompressChunk(chunk._chunkID));
				}
			}

			// Await all compression tasks
			if(compressionTasks.Count > 0) {
				await Task.WhenAll(compressionTasks);
			}

			// Now, collect compressed data for all chunks
			for(int i = 0; i < entity.Chunks.Length; i++) {
				ChunkData chunk = entity.Chunks[i];
				if(!Allocations.TryGetValue(chunk._chunkID, out ChunkAllocation allocation)) {
					continue;
				}

				if(allocation.State == ChunkState.GPUCompressing || allocation.State == ChunkState.GPUDecompressing) {
					WaitForChunkOperation(chunk._chunkID);
				}

				// Now the chunk should be compressed
				if(!Allocations.TryGetValue(chunk._chunkID, out allocation) || allocation.State != ChunkState.Compressed) {
					continue;
				}

				// Read compressed data from pool
				int offset = allocation.CompressedOffset;
				int size = allocation.CompressedSize;
				byte[] compressedData = new byte[size];
				for(int j = 0; j < size; j++) {
					compressedData[j] = CompressedPool[offset + j];
				}

				// Write chunk index (new: 4 bytes)
				entityData.Write(BitConverter.GetBytes(i), 0, sizeof(int));
				// Append to entity data
				entityData.Write(BitConverter.GetBytes(chunk._chunkID), 0, sizeof(long));
				entityData.Write(BitConverter.GetBytes(size), 0, sizeof(int));
				entityData.Write(compressedData, 0, size);
			}
			return entityData.ToArray();
		}

		void UpdateCompressionOperations() {
			var completedOperations = new List<long>();

			foreach(var kvp in _activeOperations) {
				CompressionOperation operation = kvp.Value;

				// Check if GPU readback is complete
				if(operation.ReadbackRequest.HasValue && operation.ReadbackRequest.Value.done) {
					// Process completed compression/decompression
					ProcessCompletedOperation(operation);
					completedOperations.Add(kvp.Key);
				}

				// Timeout check
				if(Time.time - operation.StartTime > ChunkOperationTimeout && !operation.CompletionSource.Task.IsCompleted) {
					Debug.LogError($"Compression operation for chunk {operation.ChunkID} timed out");
					operation.CompletionSource.SetException(new TimeoutException());
					completedOperations.Add(kvp.Key);
				}
			}

			// Remove completed operations
			foreach(long chunkID in completedOperations) {
				_activeOperations.Remove(chunkID);
			}
		}

		void ProcessCompletedOperation(CompressionOperation operation) {
			// TODO: Implement based on operation type
			operation.CompletionSource.SetResult(true);
		}

		#endregion

		#region Cleanup

		void OnDestroy() {
			// Dispose all native collections
			if(UncompressedPool.IsCreated) UncompressedPool.Dispose();
			if(CompressedPool.IsCreated) CompressedPool.Dispose();
			if(Allocations.IsCreated) Allocations.Dispose();
			if(Headers.IsCreated) Headers.Dispose();

			// Dispose GPU buffers
			GPUInputBuffer?.Dispose();
			GPUOutputBuffer?.Dispose();
			GPUMetadataBuffer?.Dispose();
		}

		void Update() {
			// Process pending compression operations
			UpdateCompressionOperations();
		}

		#endregion

		#region Debug and Statistics

		public void GetMemoryStatistics(out int uncompressedMemory, out int compressedMemory, out long totalMemoryUsed) {
			uncompressedMemory = 0;
			compressedMemory = 0;
			foreach(var allocation in Allocations) {
				if(allocation.Value.State == ChunkState.Uncompressed) {
					uncompressedMemory += UncompressedChunkSize;
				} else if(allocation.Value.State == ChunkState.Compressed) {
					compressedMemory += allocation.Value.CompressedSize;
				}
			}
			uncompressedMemory = uncompressedMemory / 1024 / 1024;
			compressedMemory = compressedMemory / 1024 / 1024;
			totalMemoryUsed = (long)(UncompressedPool.Length * sizeof(int) + CompressedPool.Length) / 1024 / 1024;
		}

		public string Report(StatsDisplay.DisplayMode displayMode, float deltaTime) {
			if(!displayMode.HasFlag(StatsDisplay.DisplayMode.MemoryStats)) return "";
			GetMemoryStatistics(out int uncompressed, out int compressed, out long totalMemory);
			return $"Chunk Memory:\n\tUncompressed: {uncompressed}MB\n\tCompressed: {compressed}MB\n\tTotal Memory: {totalMemory}MB";
		}

		public void ClearLastReport() { }

		#endregion

	}
}