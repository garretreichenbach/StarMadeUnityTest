using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using Dev;
using Settings;
using Unity.Burst;
using Unity.Collections;
using Unity.Jobs;
using UnityEngine;
using UnityEngine.Rendering;

namespace Universe.Data.Chunk {
	public class ChunkMemoryManager : MonoBehaviour, StatsDisplay.IStatsDisplayReporter {
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
		[SerializeField] int compressionBatchSize = 16; // GPU batch size
		// Public accessor for tools/other managers to read batch capacity
		public int CompressionBatchSize => compressionBatchSize;

		// Each uncompressed chunk: 32³ blocks × 4 bytes = 131,072 bytes (128KB)
		const int UncompressedChunkSize = 32 * 32 * 32 * sizeof(int);
		const int BlocksPerChunk = 32 * 32 * 32;

		#endregion

		#region Core Memory Pools

		// Main uncompressed data pool - sequential 128KB chunks
		public NativeArray<int> _uncompressedPool;

		// Compressed data pool - variable sized allocations
		public NativeArray<byte> _compressedPool;
		public int _compressedPoolHead; // Next allocation offset

		// Chunk metadata and allocation tracking
		public NativeHashMap<long, ChunkAllocation> _allocations; // ChunkID -> Allocation info
		public NativeHashMap<long, ChunkHeader> _headers; // ChunkID -> Header info

		// Free slot management
		public Queue<int> _freeUncompressedSlots;
		Queue<CompressedSlot> _freeCompressedSlots; // Size-based free slots

		#endregion

		#region GPU Resources

		public ChunkCompressionManager CompressionManager => ChunkCompressionManager.Instance;

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
			public float LastAccessTime;
			public float LastModifiedTime;
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

		public static ChunkMemoryManager Instance { get; private set; }

		void Awake() {
			if(Instance != null) {
				Debug.LogError("Multiple ChunkMemoryManager instances found!");
				Destroy(gameObject);
				return;
			}
			Instance = this;
			DontDestroyOnLoad(gameObject);
			InitializeMemoryPools();
			InitializeGPUResources();
			// Initialize GPU mutex
			_gpuMutex = new SemaphoreSlim(1, 1);
		}

		void InitializeMemoryPools() {
			// Allocate main uncompressed pool
			int totalUncompressedInts = maxUncompressedChunks * BlocksPerChunk;
			_uncompressedPool = new NativeArray<int>(totalUncompressedInts, Allocator.Persistent);

			// Allocate compressed pool (estimate 4:1 compression ratio)
			int estimatedCompressedSize = maxCompressedChunks * UncompressedChunkSize / 4;
			_compressedPool = new NativeArray<byte>(estimatedCompressedSize, Allocator.Persistent);

			// Initialize tracking structures
			_allocations = new NativeHashMap<long, ChunkAllocation>(maxUncompressedChunks + maxCompressedChunks, Allocator.Persistent);
			_headers = new NativeHashMap<long, ChunkHeader>(maxUncompressedChunks + maxCompressedChunks, Allocator.Persistent);

			// Initialize free slot queues
			_freeUncompressedSlots = new Queue<int>();
			for(int i = 0; i < maxUncompressedChunks; i++) {
				_freeUncompressedSlots.Enqueue(i);
			}

			_freeCompressedSlots = new Queue<CompressedSlot>();
			_compressedPoolHead = 0;
			_activeOperations = new Dictionary<long, CompressionOperation>();

			Debug.Log($"GlobalChunkMemoryManager initialized: {totalUncompressedInts * sizeof(int) / 1024 / 1024}MB uncompressed, {estimatedCompressedSize / 1024 / 1024}MB compressed");
		}

		void InitializeGPUResources() {
			if(compressionShader == null || decompressionShader == null) {
				Debug.LogError("Compression shaders not assigned! GPU compression disabled.");
				return;
			}

			// Create GPU buffers for batch compression operations
			GPUInputBuffer = new ComputeBuffer(compressionBatchSize * BlocksPerChunk, sizeof(int));
			// Output buffer is used as a ByteAddressBuffer in the compute shader, so create it as a raw buffer
			// PER_CHUNK_OUTPUT_BYTES (in bytes) defined in shader: 262144
			int perChunkOutputInts = 262144 / 4; // 65536 ints per chunk
			GPUOutputBuffer = new ComputeBuffer(compressionBatchSize * perChunkOutputInts, sizeof(int), ComputeBufferType.Raw);
			// Metadata: two uints per chunk (payloadSize, originalSize)
			GPUMetadataBuffer = new ComputeBuffer(compressionBatchSize * 2, sizeof(int));

		}

		#endregion

		#region Core Memory Operations

		public bool AllocateChunk(long chunkID, int entityID = -1, int chunkIndex = -1) {
			// If chunk already exists, forcibly deallocate it first
			if(_allocations.ContainsKey(chunkID)) {
				Debug.LogWarning($"[AllocateChunk] Chunk {chunkID} already allocated. Forcibly deallocating before re-allocation.");
				DeallocateChunk(chunkID);
			}

			if(_freeUncompressedSlots.Count == 0) {
				// Try to compress some chunks to free up space
				if(!TryFreeUncompressedSlot()) {
					Debug.LogError("No free uncompressed slots available!");
					return false;
				}
			}

			int poolIndex = _freeUncompressedSlots.Dequeue();

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
				LastAccessTime = Time.time,
				LastModifiedTime = Time.time,
				IsDirty = false,
				CompressionLevel = 0,
				OriginalSize = UncompressedChunkSize,
				CompressedSize = 0,
				Checksum = 0,
			};

			_allocations.Add(chunkID, allocation);
			_headers.Add(chunkID, header);

			// Zero out the allocated memory
			ClearChunkMemory(poolIndex);

			return true;
		}

		public void DeallocateChunk(long chunkID) {
			if(!_allocations.TryGetValue(chunkID, out ChunkAllocation allocation)) {
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
				_allocations[chunkID] = allocation; // <-- Ensure dictionary is updated
			}

			// Free the appropriate slot
			if(allocation.State == ChunkState.Uncompressed) {
				if(allocation.PoolIndex >= 0) {
					_freeUncompressedSlots.Enqueue(allocation.PoolIndex);
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

			_allocations.Remove(chunkID);
			_headers.Remove(chunkID);
			Debug.Log($"[DeallocateChunk] Chunk {chunkID} fully deallocated.");
		}

		void ClearChunkMemory(int poolIndex) {
			int startIndex = poolIndex * BlocksPerChunk;
			var slice = _uncompressedPool.GetSubArray(startIndex, BlocksPerChunk);

			// Use a job to clear memory efficiently
			ClearMemoryJob clearJob = new ClearMemoryJob {
				Data = slice,
			};
			clearJob.Schedule(BlocksPerChunk, 1024).Complete();
		}

		#endregion

		#region Block Access Methods

		public int GetRawData(long chunkID, int blockIndex) {
			/*if(!EnsureChunkAccessible(chunkID).Result) {
				Debug.LogError($"Cannot access chunk {chunkID} for reading");
				return 0;
			}*/
			ChunkAllocation allocation = _allocations[chunkID];
			UpdateLastAccessTime(chunkID);
			int globalIndex = allocation.PoolIndex * BlocksPerChunk + blockIndex;
			return _uncompressedPool[globalIndex];
		}

		public void SetRawData(long chunkID, int blockIndex, int rawData) {
			/*if(!EnsureChunkAccessible(chunkID).Result) {
				Debug.LogError($"Cannot access chunk {chunkID} for reading");
				return;
			}*/
			ChunkAllocation allocation = _allocations[chunkID];
			UpdateLastAccessTime(chunkID);
			MarkChunkDirty(chunkID);
			int globalIndex = allocation.PoolIndex * BlocksPerChunk + blockIndex;
			_uncompressedPool[globalIndex] = rawData;
		}

		public int[] GetRawDataArray(long chunkID) {
			/*if(!EnsureChunkAccessible(chunkID).Result) {
				Debug.LogError($"Cannot access chunk {chunkID} for reading");
				return null;
			}*/
			ChunkAllocation allocation = _allocations[chunkID];
			UpdateLastAccessTime(chunkID);
			int startIndex = allocation.PoolIndex * BlocksPerChunk;
			var slice = _uncompressedPool.GetSubArray(startIndex, BlocksPerChunk);
			return slice.ToArray();
		}

		public void SetRawDataArray(long chunkID, int[] data) {
			if(data.Length != BlocksPerChunk) {
				Debug.LogError($"Invalid data array size: {data.Length}, expected {BlocksPerChunk}");
				return;
			}

			/*if(!EnsureChunkAccessibleAsync(chunkID).Result) {
				Debug.LogError($"Cannot access chunk {chunkID} for reading");
				return;
			}*/

			ChunkAllocation allocation = _allocations[chunkID];
			UpdateLastAccessTime(chunkID);
			MarkChunkDirty(chunkID);

			int startIndex = allocation.PoolIndex * BlocksPerChunk;
			var slice = _uncompressedPool.GetSubArray(startIndex, BlocksPerChunk);

			slice.CopyFrom(data);
		}

		#endregion

		#region Compression Management

		/**
		* Ensures the specified chunk is accessible for reading/writing. If the chunk is compressed, it will be decompressed asynchronously.
		*/
		public async Task<bool> EnsureChunkAccessible(long chunkID) {
			if(!_allocations.TryGetValue(chunkID, out ChunkAllocation allocation)) {
				Debug.LogError($"Chunk {chunkID} not allocated!");
				return false;
			}

			// If already uncompressed, we're good
			if(allocation.State == ChunkState.Uncompressed) {
				return true;
			}

			// If compressed, decompress asynchronously
			if(allocation.State == ChunkState.Compressed) {
				/*if (CompressionManager != null) {
					return await DecompressChunk(chunkID);
				}*/
				return false;
			}

			// If currently being processed, wait for operation
			if(allocation.State == ChunkState.GPUCompressing || allocation.State == ChunkState.GPUDecompressing) {
				Debug.LogWarning($"Chunk {chunkID} is being processed, waiting for access (async)");
				// Wait asynchronously for the operation to complete
				if(_activeOperations.TryGetValue(chunkID, out CompressionOperation op)) {
					try {
						return await op.CompletionSource.Task;
					} catch {
						return false;
					}
				}
			}
			return false;
		}

		public async Task<bool> CompressChunk(long chunkID) {
			if(!_allocations.TryGetValue(chunkID, out ChunkAllocation allocation)) {
				return false;
			}

			if(allocation.State != ChunkState.Uncompressed) {
				return false;
			}

			// Mark as being compressed
			allocation.State = ChunkState.GPUCompressing;
			_allocations[chunkID] = allocation;

			CompressionOperation operation = new CompressionOperation {
				ChunkID = chunkID,
				TargetState = ChunkState.Compressed,
				CompletionSource = new TaskCompletionSource<bool>(),
				StartTime = Time.time,
			};

			_activeOperations[chunkID] = operation;

			// Use GPUCompressionManager for actual compression
			ChunkCompressionManager compressionManager = CompressionManager;
			if(compressionManager != null) {
				try {
					// Ensure only one GPU compression/decompression runs at a time
					await _gpuMutex.WaitAsync();
					try {
						await compressionManager.CompressChunk(chunkID);
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
			if(!_allocations.TryGetValue(chunkID, out ChunkAllocation allocation)) {
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
			Debug.Log($"[DecompressChunk] Chunk {chunkID} calling compressionManager.DecompressChunk");
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
						allocation = _allocations[chunkID];
						if(allocation.State == ChunkState.Uncompressed) {
							Debug.Log($"[DecompressChunk] Chunk {chunkID} successfully decompressed and state set to Uncompressed");
							operation.CompletionSource.SetResult(true);
						} else {
							Debug.LogError($"Chunk {chunkID} state invalid after decompression: {allocation.State}");
							allocation.State = ChunkState.Compressed;
							_allocations[chunkID] = allocation;
							operation.CompletionSource.SetException(new Exception("Invalid chunk state after decompression"));
						}
					} finally {
						_gpuMutex.Release();
					}
				} catch(Exception ex) {
					Debug.LogError($"GPU decompression failed: {ex.Message}, ChunkState: {allocation.State}");
					// Reset state so chunk can be retried
					if(_allocations.TryGetValue(chunkID, out var failedAlloc)) {
						failedAlloc.State = ChunkState.Compressed;
						_allocations[chunkID] = failedAlloc;
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

		void UpdateLastAccessTime(long chunkID) {
			if(_headers.TryGetValue(chunkID, out ChunkHeader header)) {
				header.LastAccessTime = Time.time;
				_headers[chunkID] = header;
			}
		}

		void MarkChunkDirty(long chunkID) {
			if(_headers.TryGetValue(chunkID, out ChunkHeader header)) {
				header.IsDirty = true;
				header.LastModifiedTime = Time.time;
				_headers[chunkID] = header;
			}
		}

		bool TryFreeUncompressedSlot() {
			// Find oldest uncompressed chunk that's not recently accessed
			long oldestChunkID = -1;
			float oldestTime = float.MaxValue;

			foreach(var kvp in _headers) {
				ChunkHeader header = kvp.Value;
				if(_allocations.TryGetValue(kvp.Key, out ChunkAllocation allocation) && allocation.State == ChunkState.Uncompressed && header.LastAccessTime < oldestTime && Time.time - header.LastAccessTime > 30.0f) // 30 second threshold
				{
					oldestTime = header.LastAccessTime;
					oldestChunkID = kvp.Key;
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
			var entityData = new System.IO.MemoryStream(rawCompressedData);
			var decompressionTasks = new List<Task<bool>>();
			int chunksTotal = gameEntity.ChunkCount;
			int chunksRead = 0;
			int chunksFailed = 0;

			// Ensure the Chunks array is allocated and initialized to invalid
			if(gameEntity.Chunks == null || gameEntity.Chunks.Length != gameEntity.ChunkCount) {
				gameEntity.Chunks = new ChunkData[gameEntity.ChunkCount];
				for(int i = 0; i < gameEntity.Chunks.Length; i++) {
					gameEntity.Chunks[i] = new ChunkData(ChunkData.InvalidChunkID, 0);
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
				if(!_allocations.ContainsKey(chunkID)) {
					if(!AllocateChunk(chunkID, gameEntity.EntityID, chunkIndex)) {
						Debug.LogError($"Failed to allocate chunk {chunkID} for entity {gameEntity.Name}");
						chunksFailed++;
						continue;
					}
				}

				if(!_allocations.TryGetValue(chunkID, out ChunkAllocation allocation)) {
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
						if(_allocations.TryGetValue(chunkID, out var alloc) && alloc.PoolIndex >= 0 && chunkIndex >= 0 && chunkIndex < gameEntity.Chunks.Length) {
							gameEntity.Chunks[chunkIndex] = new ChunkData(chunkID, alloc.PoolIndex);
							// Log chunk assignment and first 8 block types
							int[] blockData = _uncompressedPool.GetSubArray(alloc.PoolIndex * 32 * 32 * 32, 8).ToArray();
							string blockPreview = string.Join(", ", blockData);
							Debug.Log($"[DecompressEntity] Assigned chunkID={chunkID} to index={chunkIndex}, first 8 block types: [{blockPreview}]");
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
			var entityData = new System.IO.MemoryStream();
			// Use batched compression: collect all uncompressed chunk IDs and dispatch in one GPU batch
			var toCompress = new List<long>();
			for(int i = 0; i < entity.Chunks.Length; i++) {
				var chunk = entity.Chunks[i];
				if(!_allocations.TryGetValue(chunk._chunkID, out ChunkAllocation allocation)) {
					Debug.LogWarning($"Chunk {chunk._chunkID} not allocated, skipping compression");
					continue;
				}
				if(allocation.State == ChunkState.Uncompressed) {
					// mark as compressing to reserve slot and avoid races
					allocation.State = ChunkState.GPUCompressing;
					_allocations[chunk._chunkID] = allocation;
					toCompress.Add(chunk._chunkID);
					// Create an active CompressionOperation so callers can wait for this chunk
					CompressionOperation op = new CompressionOperation {
						ChunkID = chunk._chunkID,
						TargetState = ChunkState.Compressed,
						CompletionSource = new TaskCompletionSource<bool>(),
						StartTime = Time.time,
						ReadbackRequest = null
					};
					_activeOperations[chunk._chunkID] = op;
				}
			}

			if(toCompress.Count > 0) {
				// Process in batches to respect GPU batch capacity
				ChunkCompressionManager compressionManager = CompressionManager;
				if(compressionManager != null) {
					int batchCap = this.CompressionBatchSize;
					try {
						await _gpuMutex.WaitAsync();
						try {
							for(int start = 0; start < toCompress.Count; start += batchCap) {
								int len = Math.Min(batchCap, toCompress.Count - start);
								long[] batchIds = new long[len];
								toCompress.CopyTo(start, batchIds, 0, len);
								try {
									await compressionManager.CompressChunks(batchIds);
								} catch(Exception exBatch) {
									Debug.LogError($"Batch GPU compression failed for batch starting at {start}: {exBatch.Message}");
									// Reset states for this batch so caller can retry individual chunks
									for(int bi = 0; bi < batchIds.Length; ++bi) {
										long id = batchIds[bi];
										if(_allocations.TryGetValue(id, out var a)) { a.State = ChunkState.Uncompressed; _allocations[id] = a; }
									}
									// continue with next batch
								}
							}
						} finally {
							_gpuMutex.Release();
						}
					} catch(Exception ex) {
						Debug.LogError($"Batch GPU compression outer failure: {ex.Message}");
						// Reset any remaining toCompress entries to Uncompressed
						foreach(var id in toCompress) {
							if(_allocations.TryGetValue(id, out var a)) { a.State = ChunkState.Uncompressed; _allocations[id] = a; }
						}
					}
				} else {
					Debug.LogError("No ChunkCompressionManager available for batch compression");
					// Reset states
					foreach(var id in toCompress) {
						if(_allocations.TryGetValue(id, out var a)) { a.State = ChunkState.Uncompressed; _allocations[id] = a; }
					}
				}
			}

			// Now, collect compressed data for all chunks
			for(int i = 0; i < entity.Chunks.Length; i++) {
				var chunk = entity.Chunks[i];
				if(!_allocations.TryGetValue(chunk._chunkID, out ChunkAllocation allocation)) {
					Debug.LogWarning($"Chunk {chunk._chunkID} not allocated, skipping compression");
					continue;
				}

				if(allocation.State == ChunkState.GPUCompressing || allocation.State == ChunkState.GPUDecompressing) {
					Debug.LogWarning($"Chunk {chunk._chunkID} is being processed, waiting for compression");
					WaitForChunkOperation(chunk._chunkID);
				}

				// Now the chunk should be compressed
				if(!_allocations.TryGetValue(chunk._chunkID, out allocation) || allocation.State != ChunkState.Compressed) {
					Debug.LogError($"Chunk {chunk._chunkID} is not compressed after operation");
					continue;
				}

				// Read compressed data from pool
				int offset = allocation.CompressedOffset;
				int size = allocation.CompressedSize;
				byte[] compressedData = new byte[size];
				for(int j = 0; j < size; j++) {
					compressedData[j] = _compressedPool[offset + j];
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

		// Allow external callers (e.g. ChunkCompressionManager) to complete per-chunk compression operations
		public void CompleteCompressionOperation(long chunkID, bool success, Exception ex = null) {
			if(!_activeOperations.TryGetValue(chunkID, out CompressionOperation op)) return;
			if(success) {
				try { op.CompletionSource.SetResult(true); } catch { }
			} else {
				try { op.CompletionSource.SetException(ex ?? new Exception("Compression failed (batch)")); } catch { }
			}
			_activeOperations.Remove(chunkID);
		}

		#endregion

		#region Cleanup

		void OnDestroy() {
			// Dispose all native collections
			if(_uncompressedPool.IsCreated) _uncompressedPool.Dispose();
			if(_compressedPool.IsCreated) _compressedPool.Dispose();
			if(_allocations.IsCreated) _allocations.Dispose();
			if(_headers.IsCreated) _headers.Dispose();

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

		void Start() {
			StatsDisplay statsDisplay = FindFirstObjectByType<StatsDisplay>();
			if(statsDisplay != null) {
				statsDisplay.Reporters.Add(this);
			}
		}

		public void GetMemoryStatistics(out int uncompressedMemory, out int compressedMemory, out long totalMemoryUsed) {
			uncompressedMemory = 0;
			compressedMemory = 0;
			foreach(var allocation in _allocations) {
				if(allocation.Value.State == ChunkState.Uncompressed) {
					uncompressedMemory += UncompressedChunkSize;
				} else if(allocation.Value.State == ChunkState.Compressed) {
					compressedMemory += allocation.Value.CompressedSize;
				}
			}
			uncompressedMemory = uncompressedMemory / 1024 / 1024;
			compressedMemory = compressedMemory / 1024 / 1024;
			totalMemoryUsed = (long)(_uncompressedPool.Length * sizeof(int) + _compressedPool.Length) / 1024 / 1024;
		}

		public string Report(StatsDisplay.DisplayMode displayMode, float deltaTime) {
			if(!displayMode.HasFlag(StatsDisplay.DisplayMode.MemoryStats)) return "";
			GetMemoryStatistics(out int uncompressed, out int compressed, out long totalMemory);
			return $"Chunk Memory:\n\tUncompressed: {uncompressed}MB\n\tCompressed: {compressed}MB\n\tTotal Memory: {totalMemory}MB";
		}

		public void ClearLastReport() { }

		#endregion

		#region Diagnostic Tests

		/**
		* Diagnostic: Run a compression -> decompression round-trip on a single chunk and verify data integrity.
		* Returns true if round-trip preserves the chunk's raw data.
		*/
		public async Task<bool> TestCompressionRoundTrip(long chunkID) {
			if(!_allocations.TryGetValue(chunkID, out ChunkAllocation alloc)) {
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
			if(!_allocations.TryGetValue(chunkID, out alloc) || alloc.State != ChunkState.Compressed) {
				Debug.LogError($"[TestCompressionRoundTrip] Chunk {chunkID} not in compressed state after compression (state={( _allocations.TryGetValue(chunkID, out alloc) ? alloc.State.ToString() : "missing")})");
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
			} else {
				Debug.LogError($"[TestCompressionRoundTrip] Chunk {chunkID} round-trip failed with {mismatches} mismatches");
				return false;
			}
		}

		#endregion

		// Public helpers for tools
		public long[] GetAllChunkIDs() {
			var keys = new System.Collections.Generic.List<long>();
			foreach(var kvp in _allocations) {
				keys.Add(kvp.Key);
			}
			return keys.ToArray();
		}

		public bool TryGetHeader(long chunkID, out ChunkHeader header) {
			if(_headers.TryGetValue(chunkID, out header)) return true;
			header = default;
			return false;
		}
	}
}
