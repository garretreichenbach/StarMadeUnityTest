using System;
using System.Buffers;
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
		async Task<bool> WaitForChunkOperationAsync(long chunkID) {
			if(!_activeOperations.TryGetValue(chunkID, out CompressionOperation operation)) {
				return false;
			}
			try {
				var completed = await Task.WhenAny(operation.CompletionSource.Task, Task.Delay(TimeSpan.FromSeconds(ChunkOperationTimeout)));
				if(completed == operation.CompletionSource.Task) {
					// Propagate result/exception
					try {
						return await operation.CompletionSource.Task;
					} catch {
						return false;
					}
				} else {
					Debug.LogError($"Timeout waiting for chunk operation {chunkID}");
					return false;
				}
			} catch(Exception ex) {
				Debug.LogError($"Error waiting for chunk operation: {ex.Message}");
				return false;
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
		// Pending compressed chunk writes enqueued by the compression manager's GPU readback callbacks.
		Queue<PendingCompressedWrite> _pendingCompressedWrites;
		object _pendingCompressedWritesLock;
		// Pending decompressed chunk writes (int arrays) queued by decompression readbacks.
		Queue<PendingDecompressedWrite> _pendingDecompressedWrites;
		object _pendingDecompressedWritesLock;
		// Adaptive throttling: maximum milliseconds per frame to spend committing compressed writes.
		[Header("Compression Commit Throttling")]
		[SerializeField] float maxCommitMsPerFrame = 2.0f; // ms budget per frame for commits. 0 = unlimited (use count fallback)
		[SerializeField] int maxCompressedWritesPerFrame = 8; // fallback limit when maxCommitMsPerFrame <= 0
		// Backpressure: max queued compressed blobs before we start logging warnings or delaying further dispatches
		[SerializeField] int pendingWriteBackpressureThreshold = 256;
		// TODO: Implement stronger backpressure: pause dispatching new GPU batches when pending queues grow too large.
		// For now we warn when the queue exceeds pendingWriteBackpressureThreshold

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
			public int ReservedPoolIndex; // for decompression: reserved uncompressed pool slot
			public float StartTime;
		}

		public struct PendingCompressedWrite {
			public long ChunkID;
			public byte[] Data;
			public bool ReturnToPool; // if true, Commit will return Data to ArrayPool<byte>
		}

		public struct PendingDecompressedWrite {
			public long ChunkID;
			public int PoolIndex;
			public int[] Data;
			public bool ReturnToPool; // if true, Commit will return Data to ArrayPool<int>
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
			_pendingCompressedWrites = new Queue<PendingCompressedWrite>();
			_pendingCompressedWritesLock = new object();
			_pendingDecompressedWrites = new Queue<PendingDecompressedWrite>();
			_pendingDecompressedWritesLock = new object();
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
			// NOTE: Clearing allocated memory synchronously here can cause a noticeable spike when many chunks
			// are allocated at once (e.g. entity load). The previous implementation performed a job and called
			// .Complete(), which blocks the main thread. To avoid brief editor freezes we defer clearing. The
			// chunk memory will be overwritten by generation/decompression operations in typical flows, so explicit
			// clearing is not required. If we need zeroed memory for some code paths, implement a lazy clear or
			// schedule the clear job without blocking and ensure consumers handle uninitialized data.
			// TODO: Implement a background clearing queue that spreads memzero work across frames if needed.
			// ClearChunkMemory(poolIndex);

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
			ChunkAllocation allocation = _allocations[chunkID];
			UpdateLastAccessTime(chunkID);
			int globalIndex = allocation.PoolIndex * BlocksPerChunk + blockIndex;
			return _uncompressedPool[globalIndex];
		}

		public void SetRawData(long chunkID, int blockIndex, int rawData) {
			ChunkAllocation allocation = _allocations[chunkID];
			UpdateLastAccessTime(chunkID);
			MarkChunkDirty(chunkID);
			int globalIndex = allocation.PoolIndex * BlocksPerChunk + blockIndex;
			_uncompressedPool[globalIndex] = rawData;
		}

		public int[] GetRawDataArray(long chunkID) {
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

			ChunkAllocation allocation = _allocations[chunkID];
			UpdateLastAccessTime(chunkID);
			MarkChunkDirty(chunkID);

			int startIndex = allocation.PoolIndex * BlocksPerChunk;
			var slice = _uncompressedPool.GetSubArray(startIndex, BlocksPerChunk);
			slice.CopyFrom(data);
		}

		#endregion

		#region Compression Management
		
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
			// Debug.Log($"[DecompressChunk] Chunk {chunkID} calling compressionManager.DecompressChunk");
			CompressionOperation operation = new CompressionOperation {
				ChunkID = chunkID,
				TargetState = ChunkState.Uncompressed,
				CompletionSource = new TaskCompletionSource<bool>(),
				StartTime = Time.time,
				ReservedPoolIndex = -1,
			};
			_activeOperations[chunkID] = operation;
			ChunkCompressionManager compressionManager = CompressionManager;
			if(compressionManager != null) {
				try {
					// Serialize GPU access to avoid concurrent buffer reuse
					await _gpuMutex.WaitAsync();
					try {
						// Reserve uncompressed slot here and store it on the operation so the compression manager and later commit can use it.
						if(_freeUncompressedSlots.Count == 0) {
							// No slot available; fail
							operation.CompletionSource.SetException(new Exception("No free uncompressed slots available for decompression"));
							_activeOperations[chunkID] = operation;
							_gpuMutex.Release();
							return false;
						}
						int poolIndex = _freeUncompressedSlots.Dequeue();
						operation.ReservedPoolIndex = poolIndex;
						_activeOperations[chunkID] = operation;
						// Ask compression manager to start decompression and arrange readback; it will attach the readback request to our operation via SetOperationReadbackRequest.
						await compressionManager.DecompressChunk(chunkID, poolIndex);
						// Return control to caller; final completion happens when the main-thread commit executes.
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
			var pendingCompressOpTasks = new List<Task>();
			var decompressAfterCommit = new List<long>();
			var pendingIndexMap = new Dictionary<long, int>();
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

				// Enqueue compressed blob for throttled commit on main thread.
				// Create an active CompressionOperation so callers can await the commit.
				CompressionOperation op = new CompressionOperation {
					ChunkID = chunkID,
					TargetState = ChunkState.Compressed,
					CompletionSource = new TaskCompletionSource<bool>(),
					StartTime = Time.time,
				};
				_activeOperations[chunkID] = op;
				// Enqueue the compressed data (the actual copy into the compressed pool will be throttled in Update)
				EnqueueCompressedWrite(chunkID, compressedData, false);
				// Collect the per-chunk task so we can await all commits after dispatch
				var t = GetChunkOperationTask(chunkID);
				if(t != null) pendingCompressOpTasks.Add(t);
				// After commit completes, we need to decompress this chunk
				decompressAfterCommit.Add(chunkID);
				// Track mapping for post-processing on main thread
				if(!pendingIndexMap.ContainsKey(chunkID)) pendingIndexMap.Add(chunkID, chunkIndex);
			}

			// Wait for all compressed commits to complete (commits are throttled and happen in Update)
			if(pendingCompressOpTasks.Count > 0) {
				try {
					await Task.WhenAll(pendingCompressOpTasks);
				} catch { }
			}

			// Now that compressed blobs are committed (allocations updated), start decompression for those chunks
			var decompressionTasks = new List<Task<bool>>();
			foreach(var id in decompressAfterCommit) {
				decompressionTasks.Add(DecompressChunk(id));
			}
			if(decompressionTasks.Count > 0) {
				try {
					await Task.WhenAll(decompressionTasks);
				} catch { }
			}

			// Post-process assignments on main thread (safe to access NativeArray)
			foreach(var kv in pendingIndexMap) {
				long id = kv.Key;
				int idx = kv.Value;
				if(_allocations.TryGetValue(id, out var alloc) && alloc.PoolIndex >= 0 && idx >= 0 && idx < gameEntity.Chunks.Length) {
					gameEntity.Chunks[idx] = new ChunkData(id, alloc.PoolIndex);
					// Log preview (first 8 ints)
					try {
						int[] blockData = _uncompressedPool.GetSubArray(alloc.PoolIndex * BlocksPerChunk, Math.Min(8, BlocksPerChunk)).ToArray();
						string blockPreview = string.Join(", ", blockData);
						// Debug.Log($"[DecompressEntity] Assigned chunkID={id} to index={idx}, first 8 block types: [{blockPreview}]");
					} catch(Exception) { }
				} else {
					// If allocation is missing, count as failed
					chunksFailed++;
				}
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

			var pendingOpTasks = new List<Task>();
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
									// Dispatch the batch synchronously while holding GPU mutex. CompressChunksAsync returns a Task that will
									// complete when per-chunk CompletionSources are resolved. We don't await here to allow dispatching multiple batches quickly.
									var batchTask = compressionManager.CompressChunksAsync(batchIds);
									// Collect per-chunk operation tasks so we can await them after dispatching all batches.
									foreach(var id in batchIds) {
										var t = GetChunkOperationTask(id);
										if(t != null) pendingOpTasks.Add(t);
									}
									// We don't await batchTask here; it is safe to let it run and we will await _pendingOpTasks later.
								} catch(Exception exBatch) {
									Debug.LogError($"Batch GPU compression failed for batch starting at {start}: {exBatch.Message}");
									// Reset states for this batch so caller can retry individual chunks
									for(int bi = 0; bi < batchIds.Length; ++bi) {
										long id = batchIds[bi];
										if(_allocations.TryGetValue(id, out var a)) {
											a.State = ChunkState.Uncompressed;
											_allocations[id] = a;
										}
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
							if(_allocations.TryGetValue(id, out var a)) {
								a.State = ChunkState.Uncompressed;
								_allocations[id] = a;
							}
						}
					}
				} else {
					Debug.LogError("No ChunkCompressionManager available for batch compression");
					// Reset states
					foreach(var id in toCompress) {
						if(_allocations.TryGetValue(id, out var a)) {
							a.State = ChunkState.Uncompressed;
							_allocations[id] = a;
						}
					}
				}
			}

			// After dispatching all batches, await all per-chunk completion tasks (avoids blocking the main thread during callbacks).
			if(pendingOpTasks.Count > 0) {
				try {
					await Task.WhenAll(pendingOpTasks);
				} catch {
					// individual operation exceptions are handled per chunk via CompleteCompressionOperation
				}
			}

			// Now, collect compressed data for all chunks
			for(int i = 0; i < entity.Chunks.Length; i++) {
				var chunk = entity.Chunks[i];
				if(!_allocations.TryGetValue(chunk._chunkID, out ChunkAllocation allocation)) {
					Debug.LogWarning($"Chunk {chunk._chunkID} not allocated, skipping compression");
					continue;
				}

				// At this point all requested compress operations were awaited above; no per-chunk blocking expected here.
				if(allocation.State == ChunkState.GPUCompressing || allocation.State == ChunkState.GPUDecompressing) {
					Debug.LogWarning($"Chunk {chunk._chunkID} still in processing state after waiting: {allocation.State}");
					// best-effort: try one final await
					await WaitForChunkOperationAsync(chunk._chunkID);
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
			// If the operation contains a GPU readback request that is done, move its data into the pending queues for main-thread commit.
			try {
				if(operation.ReadbackRequest.HasValue && operation.ReadbackRequest.Value.done) {
					// If this operation was a decompression (TargetState == Uncompressed), extract ints and enqueue decompressed write
					if(operation.TargetState == ChunkState.Uncompressed) {
						try {
							int[] decompressed = operation.ReadbackRequest.Value.GetData<int>().ToArray();
							// Enqueue into pending decompressed writes using the reserved pool index
							PendingDecompressedWrite pdw = new PendingDecompressedWrite { ChunkID = operation.ChunkID, PoolIndex = operation.ReservedPoolIndex, Data = decompressed };
							lock(_pendingDecompressedWritesLock) {
								_pendingDecompressedWrites.Enqueue(pdw);
							}
							// Do NOT complete the operation here; completion will be signaled when the main-thread commit finishes.
							return;
						} catch(Exception ex) {
							Debug.LogError($"Failed processing decompression readback for chunk {operation.ChunkID}: {ex.Message}");
							try {
								CompleteCompressionOperation(operation.ChunkID, false, ex);
							} catch { }
							return;
						}
					}
					// For other operation types we can simply mark as completed; compressed-path completions are handled elsewhere.
					try {
						operation.CompletionSource.SetResult(true);
					} catch { }
					return;
				}
			} catch(Exception ex) {
				Debug.LogError($"Error in ProcessCompletedOperation for chunk {operation.ChunkID}: {ex.Message}");
				try {
					operation.CompletionSource.SetException(ex);
				} catch { }
			}
		}

		// Allow ChunkCompressionManager to attach a readback request and reserved pool index to an active operation.
		public void SetOperationReadbackRequest(long chunkID, AsyncGPUReadbackRequest request, int reservedPoolIndex) {
			if(!_activeOperations.TryGetValue(chunkID, out CompressionOperation op)) return;
			op.ReadbackRequest = request;
			op.ReservedPoolIndex = reservedPoolIndex;
			_activeOperations[chunkID] = op;
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

			// Commit pending compressed writes using an adaptive time-budget to avoid hitches.
			// Process both decompressed and compressed pending writes within the time budget. Decompressed writes are prioritized
			// because they are required for entity load/usage. Both queues are serviced under the same budget to avoid hitches.
			bool hasPendingAny = false;
			lock(_pendingCompressedWritesLock) {
				if(_pendingCompressedWrites != null && _pendingCompressedWrites.Count > 0) hasPendingAny = true;
			}
			lock(_pendingDecompressedWritesLock) {
				if(_pendingDecompressedWrites != null && _pendingDecompressedWrites.Count > 0) hasPendingAny = true;
			}
			if(hasPendingAny) {
				float start = Time.realtimeSinceStartup * 1000f; // ms
				int processed = 0;
				while(true) {
					// Prioritize decompressed writes
					PendingDecompressedWrite dwrite;
					bool hadDecomp = false;
					lock(_pendingDecompressedWritesLock) {
						if(_pendingDecompressedWrites.Count > 0) {
							dwrite = _pendingDecompressedWrites.Dequeue();
							hadDecomp = true;
						} else dwrite = default;
					}
					if(hadDecomp) {
						CommitPendingDecompressedWrite(dwrite);
						processed++;
					} else {
						// No decompressed work, do compressed
						PendingCompressedWrite cwrite;
						bool hadComp = false;
						lock(_pendingCompressedWritesLock) {
							if(_pendingCompressedWrites.Count > 0) {
								cwrite = _pendingCompressedWrites.Dequeue();
								hadComp = true;
							} else cwrite = default;
						}
						if(hadComp) {
							CommitPendingCompressedWrite(cwrite);
							processed++;
						} else {
							// Nothing to do
							break;
						}
					}
					// Backpressure logging (sample counts under lock)
					int compRem = 0, decompRem = 0;
					lock(_pendingCompressedWritesLock) {
						compRem = _pendingCompressedWrites.Count;
					}
					lock(_pendingDecompressedWritesLock) {
						decompRem = _pendingDecompressedWrites.Count;
					}
					int totalRem = compRem + decompRem;
					if(totalRem > pendingWriteBackpressureThreshold) {
						Debug.LogWarning($"Pending writes queue large: compressed={compRem} decompressed={decompRem} total={totalRem}");
					}
					// Check time budget
					if(maxCommitMsPerFrame > 0f) {
						float now = Time.realtimeSinceStartup * 1000f;
						if(now - start >= maxCommitMsPerFrame) break;
					} else {
						if(processed >= maxCompressedWritesPerFrame) break;
					}
				}
			}
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
				Debug.LogError($"[TestCompressionRoundTrip] Chunk {chunkID} not in compressed state after compression (state={(_allocations.TryGetValue(chunkID, out alloc) ? alloc.State.ToString() : "missing")})");
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
			var keys = new List<long>();
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

		// Return the Task representing the active compression/decompression operation for a chunk, or null if none.
		public Task GetChunkOperationTask(long chunkID) {
			if(_activeOperations != null && _activeOperations.TryGetValue(chunkID, out CompressionOperation op)) return op.CompletionSource?.Task;
			return null;
		}
		// Called by GPU readback callback to enqueue a compressed blob for main-thread commit.
		public void EnqueueCompressedWrite(long chunkID, byte[] data) {
			EnqueueCompressedWrite(chunkID, data, false);
		}

		public void EnqueueCompressedWrite(long chunkID, byte[] data, bool returnToPool) {
			if(data == null) throw new ArgumentNullException(nameof(data));
			lock(_pendingCompressedWritesLock) {
				_pendingCompressedWrites.Enqueue(new PendingCompressedWrite { ChunkID = chunkID, Data = data, ReturnToPool = returnToPool });
			}
		}

		// Called by decompression readbacks (on main thread) to enqueue decompressed int arrays for main-thread commit.
		public void EnqueueDecompressedWrite(long chunkID, int poolIndex, int[] data) {
			EnqueueDecompressedWrite(chunkID, poolIndex, data, false);
		}

		public void EnqueueDecompressedWrite(long chunkID, int poolIndex, int[] data, bool returnToPool) {
			if(data == null) throw new ArgumentNullException(nameof(data));
			lock(_pendingDecompressedWritesLock) {
				_pendingDecompressedWrites.Enqueue(new PendingDecompressedWrite { ChunkID = chunkID, PoolIndex = poolIndex, Data = data, ReturnToPool = returnToPool });
			}
		}

		void CommitPendingCompressedWrite(PendingCompressedWrite write) {
			long chunkID = write.ChunkID;
			byte[] compressedBytes = write.Data;
			int totalBytes = compressedBytes.Length;
			if(totalBytes <= 0) {
				try {
					CompleteCompressionOperation(chunkID, false, new Exception("Empty compressed blob"));
				} catch { }
				return;
			}
			// Reserve space in compressed pool atomically
			int newHead = Interlocked.Add(ref _compressedPoolHead, totalBytes);
			int poolOffset = newHead - totalBytes;
			if(poolOffset + totalBytes > _compressedPool.Length) {
				Interlocked.Add(ref _compressedPoolHead, -totalBytes);
				try {
					CompleteCompressionOperation(chunkID, false, new Exception("Compressed pool out of memory (commit)"));
				} catch { }
				return;
			}
			// Copy bytes into NativeArray on main thread using bulk CopyFrom
			var destBytes = _compressedPool.GetSubArray(poolOffset, totalBytes);
			destBytes.CopyFrom(compressedBytes);

			// Update allocation and header
			if(!_allocations.TryGetValue(chunkID, out ChunkAllocation alloc)) {
				Interlocked.Add(ref _compressedPoolHead, -totalBytes);
				try {
					CompleteCompressionOperation(chunkID, false, new Exception("Chunk allocation not found (commit)"));
				} catch { }
				return;
			}
			int oldPoolIndex = alloc.PoolIndex;
			alloc.CompressedOffset = poolOffset;
			alloc.CompressedSize = totalBytes;
			alloc.State = ChunkState.Compressed;
			alloc.PoolIndex = -1;
			_allocations[chunkID] = alloc;

			if(_headers.TryGetValue(chunkID, out ChunkHeader header)) {
				header.State = ChunkState.Compressed;
				header.CompressedSize = totalBytes;
				header.IsDirty = false;
				_headers[chunkID] = header;
			} else {
				Interlocked.Add(ref _compressedPoolHead, -totalBytes);
				try {
					CompleteCompressionOperation(chunkID, false, new Exception("Chunk header not found (commit)"));
				} catch { }
				return;
			}

			if(oldPoolIndex >= 0) _freeUncompressedSlots.Enqueue(oldPoolIndex);
			try {
				CompleteCompressionOperation(chunkID, true, null);
			} catch { }
			// Return pooled buffer if caller indicated so
			if(write.ReturnToPool) {
				try {
					ArrayPool<byte>.Shared.Return(write.Data);
				} catch { }
			}
		}

		void CommitPendingDecompressedWrite(PendingDecompressedWrite write) {
			long chunkID = write.ChunkID;
			int poolIndex = write.PoolIndex;
			int[] decompressedInts = write.Data;
			if(decompressedInts == null || decompressedInts.Length != BlocksPerChunk) {
				try {
					CompleteCompressionOperation(chunkID, false, new Exception("Invalid decompressed blob"));
				} catch { }
				// Return the reserved pool slot if present
				if(poolIndex >= 0) _freeUncompressedSlots.Enqueue(poolIndex);
				return;
			}

			// Copy into uncompressed pool (main thread) using bulk CopyFrom
			int startIndex = poolIndex * BlocksPerChunk;
			var dest = _uncompressedPool.GetSubArray(startIndex, BlocksPerChunk);
			dest.CopyFrom(decompressedInts);

			// Update allocation and header
			if(!_allocations.TryGetValue(chunkID, out ChunkAllocation alloc)) {
				// allocation missing, free slot and fail
				_freeUncompressedSlots.Enqueue(poolIndex);
				try {
					CompleteCompressionOperation(chunkID, false, new Exception("Chunk allocation not found (decompress commit)"));
				} catch { }
				return;
			}
			alloc.PoolIndex = poolIndex;
			alloc.CompressedOffset = -1;
			alloc.CompressedSize = 0;
			alloc.State = ChunkState.Uncompressed;
			_allocations[chunkID] = alloc;

			if(_headers.TryGetValue(chunkID, out ChunkHeader header)) {
				header.State = ChunkState.Uncompressed;
				header.CompressedSize = 0;
				header.IsDirty = false;
				_headers[chunkID] = header;
			} else {
				// Should not happen, but rollback
				alloc.PoolIndex = -1;
				_allocations[chunkID] = alloc;
				_freeUncompressedSlots.Enqueue(poolIndex);
				try {
					CompleteCompressionOperation(chunkID, false, new Exception("Chunk header not found (decompress commit)"));
				} catch { }
				return;
			}

			// Signal completion to any waiter
			try {
				CompleteCompressionOperation(chunkID, true, null);
			} catch { }
			// Return rented int[] to pool if requested
			if(write.ReturnToPool) {
				try {
					ArrayPool<int>.Shared.Return(decompressedInts);
				} catch { }
			}
		}

		// Finalize a compression/decompression operation: set result/exception and remove active operation.
		// This is called from main-thread commit paths and GPU callbacks to signal completion to awaiters.
		public void CompleteCompressionOperation(long chunkID, bool success, Exception error) {
			if(!_activeOperations.TryGetValue(chunkID, out CompressionOperation op)) {
				return; // nothing to complete
			}
			try {
				if(success) {
					try {
						op.CompletionSource?.SetResult(true);
					} catch { }
				} else {
					if(error != null) {
						try {
							op.CompletionSource?.SetException(error);
						} catch { }
					} else {
						try {
							op.CompletionSource?.SetResult(false);
						} catch { }
					}
				}
			} finally {
				// Remove active operation record
				_activeOperations.Remove(chunkID);
			}
		}
	}
}