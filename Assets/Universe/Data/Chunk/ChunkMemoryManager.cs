using System;
using System.Collections.Generic;
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

		/// <summary>
		///     Represents the metadata and status information for a chunk of memory in the ChunkMemoryManager system.
		/// </summary>
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

		/// <summary>
		///     Represents a section of allocated space within the compressed memory pool, used to manage compressed chunks of
		///     data.
		/// </summary>
		public struct CompressedSlot {
			public int Offset;
			public int Size;
		}

		/// <summary>
		///     Represents a compressed chunk of memory, including its unique identifier, offset, and size information.
		/// </summary>
		public struct CompressedChunk {
			public long ChunkID;
			public int Offset;
			public int Size;
		}

		/// <summary>
		///     Represents the various states of a chunk in the ChunkMemoryManager system,
		///     indicating its current allocation and processing status.
		/// </summary>
		public enum ChunkState {
			Unallocated,
			Uncompressed,
			Compressed,
			GPUCompressing,
			GPUDecompressing,
			Error,
		}

		/// <summary>
		///     Represents an operation to transition a chunk's data to a target state, such as compression or decompression.
		/// </summary>
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
			GPUOutputBuffer = new ComputeBuffer(compressionBatchSize * BlocksPerChunk, sizeof(int)); // Max size
			GPUMetadataBuffer = new ComputeBuffer(compressionBatchSize, sizeof(int) * 4); // Per-chunk metadata
		}

		#endregion

		#region Core Memory Operations

		public bool AllocateChunk(long chunkID, int entityID = -1, int chunkIndex = -1) {
			if(_allocations.ContainsKey(chunkID)) {
				Debug.LogWarning($"Chunk {chunkID} already allocated!");
				return false;
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
				return;
			}

			// Cancel any active operations on this chunk
			if(_activeOperations.ContainsKey(chunkID)) {
				_activeOperations[chunkID].CompletionSource?.SetCanceled();
				_activeOperations.Remove(chunkID);
			}

			// Free the appropriate slot
			if(allocation.State == ChunkState.Uncompressed || allocation.State == ChunkState.GPUCompressing) {
				if(allocation.PoolIndex >= 0) {
					_freeUncompressedSlots.Enqueue(allocation.PoolIndex);
				}
			}

			if(allocation.State == ChunkState.Compressed || allocation.State == ChunkState.GPUDecompressing) {
				if(allocation.CompressedOffset >= 0 && allocation.CompressedSize > 0) {
					_freeCompressedSlots.Enqueue(new CompressedSlot {
						Offset = allocation.CompressedOffset,
						Size = allocation.CompressedSize,
					});
				}
			}

			_allocations.Remove(chunkID);
			_headers.Remove(chunkID);
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
				if (_activeOperations.TryGetValue(chunkID, out CompressionOperation op)) {
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
					await compressionManager.CompressChunk(chunkID);
					operation.CompletionSource.SetResult(true);
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

			if(allocation.State != ChunkState.Compressed) {
				return false; // Can only decompress compressed chunks
			}

			// Mark as being decompressed
			allocation.State = ChunkState.GPUDecompressing;
			_allocations[chunkID] = allocation;

			CompressionOperation operation = new CompressionOperation {
				ChunkID = chunkID,
				TargetState = ChunkState.Uncompressed,
				CompletionSource = new TaskCompletionSource<bool>(),
				StartTime = Time.time,
			};

			_activeOperations[chunkID] = operation;

			// Use GPUCompressionManager for actual decompression
			ChunkCompressionManager compressionManager = CompressionManager;
			if(compressionManager != null) {
				try {
					var header = await compressionManager.DecompressChunk(chunkID);
					bool result = header.State == ChunkState.Uncompressed;
					operation.CompletionSource.SetResult(result);
				} catch(Exception ex) {
					Debug.LogError($"GPU decompression failed: {ex.Message}");
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
				if(_allocations.TryGetValue(kvp.Key, out ChunkAllocation allocation) &&
					allocation.State == ChunkState.Uncompressed &&
					header.LastAccessTime < oldestTime && Time.time - header.LastAccessTime > 30.0f) { // 30 second threshold
					oldestTime = header.LastAccessTime;
					oldestChunkID = kvp.Key;
				}
			}

			if(oldestChunkID != -1) {
				// Start async compression of oldest chunk
				CompressChunk(oldestChunkID);
				return true;
			}

			return false;
		}

		/**
		 * Compresses all chunks belonging to the specified entity and returns the compressed data as a byte array so it can be written to disk.
		 */
		public async Task<byte[]> CompressEntity(GameEntity.GameEntity entity) {
			var entityData = new System.IO.MemoryStream();
			var compressionTasks = new List<Task<bool>>();
			var chunkIDsToCompress = new List<long>();

			// First, start compression tasks for all uncompressed chunks
			foreach(var chunk in entity.Chunks) {
				if(!_allocations.TryGetValue(chunk._chunkID, out ChunkAllocation allocation)) {
					Debug.LogWarning($"Chunk {chunk._chunkID} not allocated, skipping compression");
					continue;
				}

				if(allocation.State == ChunkState.Uncompressed) {
					compressionTasks.Add(CompressChunk(chunk._chunkID));
					chunkIDsToCompress.Add(chunk._chunkID);
				}
			}

			// Await all compression tasks
			if(compressionTasks.Count > 0) {
				await Task.WhenAll(compressionTasks);
			}

			// Now, collect compressed data for all chunks
			foreach(var chunk in entity.Chunks) {
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
				for(int i = 0; i < size; i++) {
					compressedData[i] = _compressedPool[offset + i];
				}

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
				if(Time.time - operation.StartTime > ChunkOperationTimeout) {
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

		public void GetMemoryStatistics(out int uncompressedChunks, out int compressedChunks, out long totalMemoryUsed) {
			uncompressedChunks = 0;
			compressedChunks = 0;

			foreach(var allocation in _allocations) {
				if(allocation.Value.State == ChunkState.Uncompressed) {
					uncompressedChunks++;
				} else if(allocation.Value.State == ChunkState.Compressed) {
					compressedChunks++;
				}
			}

			totalMemoryUsed = (long)_uncompressedPool.Length * sizeof(int) + _compressedPool.Length;
		}

		public string Report(StatsDisplay.DisplayMode displayMode, float deltaTime) {
			if(!displayMode.HasFlag(StatsDisplay.DisplayMode.MemoryStats)) return "";
			GetMemoryStatistics(out int uncompressed, out int compressed, out long totalMemory);
			return $"Chunk Memory - Uncompressed: {uncompressed}, Compressed: {compressed}, Total Memory: {totalMemory / 1024 / 1024}MB";
		}

		public void ClearLastReport() { }

		#endregion

	}
}