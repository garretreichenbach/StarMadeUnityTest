using System;
using System.Collections.Generic;
using Unity.Collections;
using Unity.Jobs;
using UnityEngine;
using System.Threading.Tasks;
using UnityEngine.Rendering;

namespace Universe.Data.Chunk {

    public class ChunkMemoryManager : MonoBehaviour {

        #region Memory Pool Configuration

        // Memory pool sizes (configurable in inspector)
        [Header("Memory Configuration")]
        [SerializeField]
        int maxUncompressedChunks = 1024;  // ~32MB for 1024 chunks
        [SerializeField]
        int maxCompressedChunks = 4096;    // Variable size pool
        [SerializeField]
        int compressionBatchSize = 16;     // GPU batch size

        // Each uncompressed chunk: 32³ blocks × 4 bytes = 131,072 bytes (128KB)
        const int UncompressedChunkSize = 32 * 32 * 32 * sizeof(int);
        const int BlocksPerChunk = 32 * 32 * 32;

        #endregion

        #region Core Memory Pools

        // Main uncompressed data pool - sequential 128KB chunks
        NativeArray<int> _uncompressedPool;

        // Compressed data pool - variable sized allocations
        NativeArray<byte> _compressedPool;
        int _compressedPoolHead; // Next allocation offset

        // Chunk metadata and allocation tracking
        NativeHashMap<long, ChunkAllocation> _allocations;    // ChunkID -> Allocation info
        NativeHashMap<long, ChunkHeader> _headers;            // ChunkID -> Header info

        // Free slot management
        Queue<int> _freeUncompressedSlots;
        Queue<CompressedSlot> _freeCompressedSlots; // Size-based free slots

        #endregion

        #region GPU Resources

        [Header("GPU Compression")]
        [SerializeField]
        ComputeShader compressionShader;
        [SerializeField]
        ComputeShader decompressionShader;

        // GPU buffers for compression operations
        ComputeBuffer _gpuInputBuffer;      // Input data for compression
        ComputeBuffer _gpuOutputBuffer;     // Output data from compression
        ComputeBuffer _gpuMetadataBuffer;   // Compression metadata

        // Async operation tracking
        Dictionary<long, CompressionOperation> _activeOperations;

        #endregion

        #region Data Structures

        public struct ChunkAllocation {
            public long ChunkID;
            public int PoolIndex;           // Index in uncompressed pool (-1 if compressed)
            public int CompressedOffset;    // Offset in compressed pool (-1 if uncompressed)
            public int CompressedSize;      // Size in compressed pool (0 if uncompressed)
            public ChunkState State;
            public float LastAccessTime;
            public int EntityID;            // Which entity owns this chunk
            public int ChunkIndexInEntity;  // Index within the entity's chunk array
        }

        public struct ChunkHeader {
            public long ChunkID;
            public ChunkState State;
            public float LastAccessTime;
            public float LastModifiedTime;
            public bool IsDirty;
            public byte CompressionLevel;   // 0 = uncompressed, 1-9 = compression levels
            public int OriginalSize;        // Always UNCOMPRESSED_CHUNK_SIZE
            public int CompressedSize;      // Size when compressed
            public uint Checksum;           // Data integrity check
        }

        public struct CompressedSlot {
            public int Offset;
            public int Size;
        }

        public enum ChunkState {
            Unallocated,
            Uncompressed,
            Compressed,
            GPU_Compressing,
            GPU_Decompressing,
            Error
        }

        class CompressionOperation {
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
            for (int i = 0; i < maxUncompressedChunks; i++) {
                _freeUncompressedSlots.Enqueue(i);
            }

            _freeCompressedSlots = new Queue<CompressedSlot>();
            _compressedPoolHead = 0;
            _activeOperations = new Dictionary<long, CompressionOperation>();

            Debug.Log($"GlobalChunkMemoryManager initialized: {totalUncompressedInts * sizeof(int) / 1024 / 1024}MB uncompressed, {estimatedCompressedSize / 1024 / 1024}MB compressed");
        }

        void InitializeGPUResources() {
            if (compressionShader == null || decompressionShader == null) {
                Debug.LogError("Compression shaders not assigned! GPU compression disabled.");
                return;
            }

            // Create GPU buffers for batch compression operations
            _gpuInputBuffer = new ComputeBuffer(compressionBatchSize * BlocksPerChunk, sizeof(int));
            _gpuOutputBuffer = new ComputeBuffer(compressionBatchSize * BlocksPerChunk, sizeof(int)); // Max size
            _gpuMetadataBuffer = new ComputeBuffer(compressionBatchSize, sizeof(int) * 4); // Per-chunk metadata
        }

        #endregion

        #region Core Memory Operations

        public bool AllocateChunk(long chunkID, int entityID = -1, int chunkIndex = -1) {
            if (_allocations.ContainsKey(chunkID)) {
                Debug.LogWarning($"Chunk {chunkID} already allocated!");
                return false;
            }

            if (_freeUncompressedSlots.Count == 0) {
                // Try to compress some chunks to free up space
                if (!TryFreeUncompressedSlot()) {
                    Debug.LogError("No free uncompressed slots available!");
                    return false;
                }
            }

            int poolIndex = _freeUncompressedSlots.Dequeue();

            var allocation = new ChunkAllocation {
                ChunkID = chunkID,
                PoolIndex = poolIndex,
                CompressedOffset = -1,
                CompressedSize = 0,
                State = ChunkState.Uncompressed,
                LastAccessTime = Time.time,
                EntityID = entityID,
                ChunkIndexInEntity = chunkIndex
            };

            var header = new ChunkHeader {
                ChunkID = chunkID,
                State = ChunkState.Uncompressed,
                LastAccessTime = Time.time,
                LastModifiedTime = Time.time,
                IsDirty = false,
                CompressionLevel = 0,
                OriginalSize = UncompressedChunkSize,
                CompressedSize = 0,
                Checksum = 0
            };

            _allocations.Add(chunkID, allocation);
            _headers.Add(chunkID, header);

            // Zero out the allocated memory
            ClearChunkMemory(poolIndex);

            return true;
        }

        public void DeallocateChunk(long chunkID) {
            if (!_allocations.TryGetValue(chunkID, out var allocation)) {
                return;
            }

            // Cancel any active operations on this chunk
            if (_activeOperations.ContainsKey(chunkID)) {
                _activeOperations[chunkID].CompletionSource?.SetCanceled();
                _activeOperations.Remove(chunkID);
            }

            // Free the appropriate slot
            if (allocation.State == ChunkState.Uncompressed || allocation.State == ChunkState.GPU_Compressing) {
                if (allocation.PoolIndex >= 0) {
                    _freeUncompressedSlots.Enqueue(allocation.PoolIndex);
                }
            }

            if (allocation.State == ChunkState.Compressed || allocation.State == ChunkState.GPU_Decompressing) {
                if (allocation.CompressedOffset >= 0 && allocation.CompressedSize > 0) {
                    _freeCompressedSlots.Enqueue(new CompressedSlot {
                        Offset = allocation.CompressedOffset,
                        Size = allocation.CompressedSize
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
            var clearJob = new ClearMemoryJob {
                data = slice
            };
            clearJob.Schedule(BlocksPerChunk, 1024).Complete();
        }

        #endregion

        #region Block Access Methods (called by ChunkMemorySlice)

        public int GetRawData(long chunkID, int blockIndex) {
            if (!EnsureChunkAccessible(chunkID)) {
                Debug.LogError($"Cannot access chunk {chunkID} for reading");
                return 0;
            }
            var allocation = _allocations[chunkID];
            UpdateLastAccessTime(chunkID);
            int globalIndex = allocation.PoolIndex * BlocksPerChunk + blockIndex;
            return _uncompressedPool[globalIndex];
        }

        public void SetRawData(long chunkID, int blockIndex, int rawData) {
            if (!EnsureChunkAccessible(chunkID)) {
                Debug.LogError($"Cannot access chunk {chunkID} for writing");
                return;
            }
            var allocation = _allocations[chunkID];
            UpdateLastAccessTime(chunkID);
            MarkChunkDirty(chunkID);
            int globalIndex = allocation.PoolIndex * BlocksPerChunk + blockIndex;
            _uncompressedPool[globalIndex] = rawData;
        }

        public short GetBlockType(long chunkID, int blockIndex) {
            if (!EnsureChunkAccessible(chunkID)) {
                Debug.LogError($"Cannot access chunk {chunkID} for reading");
                return 0;
            }

            var allocation = _allocations[chunkID];
            UpdateLastAccessTime(chunkID);

            int globalIndex = allocation.PoolIndex * BlocksPerChunk + blockIndex;
            int rawData = _uncompressedPool[globalIndex];

            return (short)(rawData & ChunkData.TypeMask);
        }

        public void SetBlockType(long chunkID, int blockIndex, short blockType) {
            if (!EnsureChunkAccessible(chunkID)) {
                Debug.LogError($"Cannot access chunk {chunkID} for writing");
                return;
            }

            var allocation = _allocations[chunkID];
            var header = _headers[chunkID];

            UpdateLastAccessTime(chunkID);
            MarkChunkDirty(chunkID);

            int globalIndex = allocation.PoolIndex * BlocksPerChunk + blockIndex;
            int rawData = _uncompressedPool[globalIndex];

            // Clear type bits and set new type
            rawData = (rawData & ChunkData.TypeMaskInverted) | (blockType & ChunkData.TypeMask);
            _uncompressedPool[globalIndex] = rawData;
        }

        // Similar methods for HP, Orientation, Data, etc.
        public short GetBlockHP(long chunkID, int blockIndex) {
            if (!EnsureChunkAccessible(chunkID)) return 0;

            var allocation = _allocations[chunkID];
            UpdateLastAccessTime(chunkID);

            int globalIndex = allocation.PoolIndex * BlocksPerChunk + blockIndex;
            int rawData = _uncompressedPool[globalIndex];

            return (short)((rawData >> ChunkData.HPBitsStart) & ChunkData.HPMask);
        }

        public void SetBlockHP(long chunkID, int blockIndex, short hp) {
            if (!EnsureChunkAccessible(chunkID)) return;

            var allocation = _allocations[chunkID];
            UpdateLastAccessTime(chunkID);
            MarkChunkDirty(chunkID);

            int globalIndex = allocation.PoolIndex * BlocksPerChunk + blockIndex;
            int rawData = _uncompressedPool[globalIndex];

            rawData = (rawData & ChunkData.HPMaskInverted) | ((hp & ChunkData.HPMask) << ChunkData.HPBitsStart);
            _uncompressedPool[globalIndex] = rawData;
        }

        public int[] GetRawDataArray(long chunkID) {
            if (!EnsureChunkAccessible(chunkID)) {
                return new int[BlocksPerChunk]; // Return empty array
            }

            var allocation = _allocations[chunkID];
            UpdateLastAccessTime(chunkID);

            int startIndex = allocation.PoolIndex * BlocksPerChunk;
            var slice = _uncompressedPool.GetSubArray(startIndex, BlocksPerChunk);

            return slice.ToArray();
        }

        public void SetRawDataArray(long chunkID, int[] data) {
            if (data.Length != BlocksPerChunk) {
                Debug.LogError($"Invalid data array size: {data.Length}, expected {BlocksPerChunk}");
                return;
            }

            if (!EnsureChunkAccessible(chunkID)) return;

            var allocation = _allocations[chunkID];
            UpdateLastAccessTime(chunkID);
            MarkChunkDirty(chunkID);

            int startIndex = allocation.PoolIndex * BlocksPerChunk;
            var slice = _uncompressedPool.GetSubArray(startIndex, BlocksPerChunk);

            slice.CopyFrom(data);
        }

        #endregion

        #region Compression Management

        bool EnsureChunkAccessible(long chunkID) {
            if (!_allocations.TryGetValue(chunkID, out var allocation)) {
                Debug.LogError($"Chunk {chunkID} not allocated!");
                return false;
            }

            // If already uncompressed, we're good
            if (allocation.State == ChunkState.Uncompressed) {
                return true;
            }

            // If compressed, we need to decompress synchronously (blocking operation)
            if (allocation.State == ChunkState.Compressed) {
                return DecompressChunkBlocking(chunkID);
            }

            // If currently being processed, we need to wait or fail
            if (allocation.State == ChunkState.GPU_Compressing || allocation.State == ChunkState.GPU_Decompressing) {
                Debug.LogWarning($"Chunk {chunkID} is being processed, blocking access");
                return WaitForChunkOperation(chunkID);
            }

            return false;
        }

        private bool DecompressChunkBlocking(long chunkID) {
            // This is a fallback for immediate access needs
            // In practice, we'd want to avoid this and use async decompression
            Debug.LogWarning($"Blocking decompression of chunk {chunkID} - consider async preloading");

            // TODO: Implement CPU-based decompression fallback
            // For now, return false to indicate failure
            return false;
        }

        private bool WaitForChunkOperation(long chunkID) {
            if (_activeOperations.TryGetValue(chunkID, out var operation)) {
                // Wait for the operation to complete (with timeout)
                var timeoutTime = Time.time + 5.0f; // 5 second timeout
                while (!operation.CompletionSource.Task.IsCompleted && Time.time < timeoutTime) {
                    // Process pending GPU readbacks
                    UpdateCompressionOperations();
                    System.Threading.Thread.Sleep(1);
                }

                return operation.CompletionSource.Task.IsCompletedSuccessfully;
            }

            return false;
        }

        public async Task<bool> CompressChunkAsync(long chunkID) {
            if (!_allocations.TryGetValue(chunkID, out var allocation)) {
                return false;
            }

            if (allocation.State != ChunkState.Uncompressed) {
                return false; // Can only compress uncompressed chunks
            }

            // Mark as being compressed
            allocation.State = ChunkState.GPU_Compressing;
            _allocations[chunkID] = allocation;

            var operation = new CompressionOperation {
                ChunkID = chunkID,
                TargetState = ChunkState.Compressed,
                CompletionSource = new TaskCompletionSource<bool>(),
                StartTime = Time.time
            };

            _activeOperations[chunkID] = operation;

            // TODO: Implement GPU compression dispatch
            // For now, simulate async operation
            await Task.Delay(10); // Simulate GPU work

            // TODO: Move data from uncompressed to compressed pool
            // TODO: Update allocation structure

            operation.CompletionSource.SetResult(true);
            return await operation.CompletionSource.Task;
        }

        #endregion

        #region Utility Methods

        void UpdateLastAccessTime(long chunkID) {
            if (_headers.TryGetValue(chunkID, out var header)) {
                header.LastAccessTime = Time.time;
                _headers[chunkID] = header;
            }
        }

        void MarkChunkDirty(long chunkID) {
            if (_headers.TryGetValue(chunkID, out var header)) {
                header.IsDirty = true;
                header.LastModifiedTime = Time.time;
                _headers[chunkID] = header;
            }
        }

        bool TryFreeUncompressedSlot() {
            // Find oldest uncompressed chunk that's not recently accessed
            long oldestChunkID = -1;
            float oldestTime = float.MaxValue;

            foreach (var kvp in _headers) {
                var header = kvp.Value;
                if (_allocations.TryGetValue(kvp.Key, out var allocation) &&
                    allocation.State == ChunkState.Uncompressed &&
                    header.LastAccessTime < oldestTime &&
                    Time.time - header.LastAccessTime > 30.0f) { // 30 second threshold

                    oldestTime = header.LastAccessTime;
                    oldestChunkID = kvp.Key;
                }
            }

            if (oldestChunkID != -1) {
                // Start async compression of oldest chunk
                _ = CompressChunkAsync(oldestChunkID);
                return true;
            }

            return false;
        }

        void UpdateCompressionOperations() {
            var completedOperations = new List<long>();

            foreach (var kvp in _activeOperations) {
                var operation = kvp.Value;

                // Check if GPU readback is complete
                if (operation.ReadbackRequest.HasValue && operation.ReadbackRequest.Value.done) {
                    // Process completed compression/decompression
                    ProcessCompletedOperation(operation);
                    completedOperations.Add(kvp.Key);
                }

                // Timeout check
                if (Time.time - operation.StartTime > 30.0f) {
                    Debug.LogError($"Compression operation for chunk {operation.ChunkID} timed out");
                    operation.CompletionSource.SetException(new TimeoutException());
                    completedOperations.Add(kvp.Key);
                }
            }

            // Remove completed operations
            foreach (var chunkID in completedOperations) {
                _activeOperations.Remove(chunkID);
            }
        }

        private void ProcessCompletedOperation(CompressionOperation operation) {
            // TODO: Implement based on operation type
            operation.CompletionSource.SetResult(true);
        }

        #endregion

        #region Jobs for Performance

        [Unity.Burst.BurstCompile]
        private struct ClearMemoryJob : IJobParallelFor {
            public NativeArray<int> data;

            public void Execute(int index) {
                data[index] = 0;
            }
        }

        #endregion

        #region Cleanup

        void OnDestroy() {
            // Dispose all native collections
            if (_uncompressedPool.IsCreated) _uncompressedPool.Dispose();
            if (_compressedPool.IsCreated) _compressedPool.Dispose();
            if (_allocations.IsCreated) _allocations.Dispose();
            if (_headers.IsCreated) _headers.Dispose();

            // Dispose GPU buffers
            _gpuInputBuffer?.Dispose();
            _gpuOutputBuffer?.Dispose();
            _gpuMetadataBuffer?.Dispose();
        }

        void Update() {
            // Process pending compression operations
            UpdateCompressionOperations();
        }

        #endregion

        #region Debug and Statistics

        public void GetMemoryStatistics(out int uncompressedChunks, out int compressedChunks, out long totalMemoryUsed) {
            uncompressedChunks = 0;
            compressedChunks = 0;

            foreach (var allocation in _allocations) {
                if (allocation.Value.State == ChunkState.Uncompressed) {
                    uncompressedChunks++;
                } else if (allocation.Value.State == ChunkState.Compressed) {
                    compressedChunks++;
                }
            }
            totalMemoryUsed = (long)_uncompressedPool.Length * sizeof(int) + _compressedPool.Length;
        }

        [ContextMenu("Print Memory Statistics")]
        void PrintMemoryStatistics() {
            GetMemoryStatistics(out int uncompressed, out int compressed, out long totalMemory);
            Debug.Log($"Memory Stats - Uncompressed: {uncompressed}, Compressed: {compressed}, Total Memory: {totalMemory / 1024 / 1024}MB");
        }

        #endregion
    }
}