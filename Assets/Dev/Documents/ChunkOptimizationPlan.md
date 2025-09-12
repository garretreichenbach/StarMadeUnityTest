# Global Chunk Memory System Implementation Plan

## Overview
Transform the current per-chunk `NativeArray<int>` storage into a unified global memory system with GPU-based compression for optimal performance and memory usage.

## Phase 1: Core Global Memory Manager

### 1.1 Global Memory Architecture
```csharp
public class GlobalChunkMemoryManager : MonoBehaviour
{
    // Raw chunk data storage
    private NativeArray<int> _globalChunkData;
    private NativeArray<ChunkHeader> _chunkHeaders;
    
    // Memory allocation tracking
    private NativeHashMap<long, ChunkAllocation> _chunkAllocations;
    private NativeQueue<int> _freeChunkSlots;
    
    // GPU compression resources
    private ComputeShader _compressionShader;
    private ComputeShader _decompressionShader;
    private ComputeBuffer _compressionBuffer;
    private ComputeBuffer _decompressionBuffer;
}
```

### 1.2 Chunk Memory Layout
- **Uncompressed chunks**: Sequential 32KB blocks (32³ × 4 bytes)
- **Compressed chunks**: Variable-size blocks with header indicating compressed size
- **Memory pools**: Separate regions for compressed/uncompressed data

## Phase 2: Data Structures & Interfaces

### 2.1 Core Data Structures
```csharp
public struct ChunkHeader
{
    public long ChunkID;
    public int DataOffset;      // Offset in global memory
    public int DataSize;        // Size in bytes
    public ChunkState State;    // Uncompressed/Compressed/GPU_Processing
    public float LastAccessTime;
    public byte CompressionLevel;
}

public struct ChunkAllocation
{
    public int GlobalOffset;
    public int AllocatedSize;
    public bool IsCompressed;
    public int EntityID;
    public int ChunkIndex;
}

public enum ChunkState
{
    Uncompressed,
    Compressed,
    GPU_Compressing,
    GPU_Decompressing,
    Unloaded
}
```

### 2.2 Memory Slicing Interface
```csharp
public struct ChunkMemorySlice : IChunkData
{
    private GlobalChunkMemoryManager _manager;
    private long _chunkID;
    private int _globalOffset;
    
    // Implement IChunkData interface with bounds checking
    public short GetBlockType(int index) => _manager.GetBlockType(_chunkID, index);
    public void SetBlockType(int index, short type) => _manager.SetBlockType(_chunkID, index, type);
    // ... other IChunkData methods
}
```

## Phase 3: GPU Compression System

### 3.1 Compute Shader Architecture

#### Compression Shader (`ChunkCompression.compute`)
- **Input**: Raw chunk data (32KB)
- **Algorithm**: LZ4-style compression optimized for GPU
- **Output**: Compressed data + compression metadata
- **Thread groups**: 32×32×1 (one thread per block column)

#### Decompression Shader (`ChunkDecompression.compute`)
- **Input**: Compressed chunk data
- **Algorithm**: Parallel LZ4 decompression
- **Output**: Reconstructed raw chunk data
- **Optimization**: Wavefront-aware parallel decompression

### 3.2 Compression Strategy
```csharp
public class GPUCompressionManager
{
    public async Task<CompressedChunk> CompressChunkAsync(long chunkID)
    {
        // 1. Upload chunk data to GPU
        // 2. Dispatch compression compute shader
        // 3. Read back compressed data
        // 4. Update global memory allocation
        // 5. Free uncompressed slot
    }
    
    public async Task<bool> DecompressChunkAsync(long chunkID)
    {
        // 1. Allocate uncompressed slot
        // 2. Upload compressed data to GPU
        // 3. Dispatch decompression compute shader
        // 4. Update chunk header state
        // 5. Free compressed slot
    }
}
```

## Phase 4: Migration & Integration

### 4.1 ChunkDataV8 Refactor
```csharp
public struct ChunkDataV8 : IComponentData, IChunkData
{
    public long ChunkID { get; private set; }
    private GlobalChunkMemoryManager _globalManager;
    
    // Remove: public NativeArray<int> Data;
    // Add: Global memory access through manager
    
    public short GetBlockType(int index)
    {
        return _globalManager.GetBlockType(ChunkID, index);
    }
    
    public void SetBlockType(int index, short type)
    {
        _globalManager.SetBlockType(ChunkID, index, type);
        _globalManager.MarkChunkDirty(ChunkID);
    }
}
```

### 4.2 GameEntity Integration
```csharp
public abstract class GameEntity : MonoBehaviour
{
    // Replace: private Chunk.Chunk[] _chunks;
    private long[] _chunkIDs;
    private GlobalChunkMemoryManager _chunkManager;
    
    public ChunkMemorySlice GetChunkData(int chunkIndex)
    {
        return _chunkManager.GetChunkSlice(_chunkIDs[chunkIndex]);
    }
    
    public void PreloadChunksForRendering()
    {
        // Ensure all chunks needed for mesh building are decompressed
        foreach (long chunkID in _chunkIDs)
        {
            _chunkManager.EnsureDecompressed(chunkID);
        }
    }
}
```

## Phase 5: Performance Optimizations

### 5.1 Intelligent Compression Strategy
- **Distance-based**: Compress chunks far from camera
- **Usage-based**: Compress rarely accessed chunks
- **Memory pressure**: Aggressive compression when memory is low
- **Batch processing**: Group compression/decompression operations

### 5.2 Streaming & Preloading
```csharp
public class ChunkStreamingManager
{
    public void UpdateChunkStates(Vector3 cameraPosition)
    {
        foreach (var chunk in _trackedChunks)
        {
            float distance = Vector3.Distance(cameraPosition, chunk.WorldPosition);
            
            if (distance < IMMEDIATE_DISTANCE)
                EnsureUncompressed(chunk.ID);
            else if (distance < PRELOAD_DISTANCE)
                ScheduleDecompression(chunk.ID);
            else if (distance > COMPRESS_DISTANCE)
                ScheduleCompression(chunk.ID);
        }
    }
}
```

### 5.3 Memory Pool Management
- **Fixed-size pools**: For uncompressed chunks (32KB each)
- **Variable-size pools**: For compressed chunks (multiple size classes)
- **Defragmentation**: Background process to compact compressed memory
- **Memory budgets**: Configurable limits for each pool

## Phase 6: Implementation Timeline

### Week 1-2: Foundation
- [ ] Implement `GlobalChunkMemoryManager` basic structure
- [ ] Create `ChunkMemorySlice` with `IChunkData` interface
- [ ] Set up memory pools and allocation tracking
- [ ] Basic unit tests for memory management

### Week 3-4: GPU Compression
- [ ] Implement compression/decompression compute shaders
- [ ] Create `GPUCompressionManager` with async operations
- [ ] Test compression ratios and performance
- [ ] Handle GPU memory management

### Week 5-6: Integration & Replacement
- [ ] **Complete ChunkDataV8 removal** - delete the old struct entirely
- [ ] Update `Chunk` struct to use `ChunkMemorySlice` directly
- [ ] Verify all existing code works unchanged (terrain generation, chunk building, etc.)
- [ ] Update any remaining references to ChunkDataV8 in comments/docs

### Week 7-8: Optimization & Polish
- [ ] Implement intelligent compression strategies
- [ ] Add chunk streaming based on camera distance
- [ ] Performance profiling and optimization
- [ ] Memory pressure handling and fallbacks

## Phase 7: Expected Benefits

### Performance Improvements
- **Memory efficiency**: 60-80% reduction in chunk memory usage
- **Cache performance**: Sequential memory access patterns
- **GPU utilization**: Parallel compression/decompression
- **Reduced GC pressure**: Single large allocation vs many small ones

### Scalability Benefits
- **Larger worlds**: More chunks fit in memory
- **Network efficiency**: Send compressed chunks over network
- **Streaming**: Seamless loading/unloading of distant chunks
- **Memory predictability**: Fixed memory budget for chunk system

## Phase 8: Risk Mitigation

### Technical Risks
- **GPU compatibility**: Fallback to CPU compression for older GPUs
- **Memory fragmentation**: Implement defragmentation strategies
- **Async complexity**: Robust state management for async operations
- **Data corruption**: Checksums and validation for compressed data

### Migration Strategy
- **Gradual rollout**: Implement alongside existing system initially
- **A/B testing**: Compare performance between old and new systems
- **Rollback plan**: Keep old system as fallback during transition
- **Data migration**: Tools to convert existing save files

## Conclusion

This implementation will transform your chunk system into a high-performance, memory-efficient solution that scales well with world size. The GPU-based compression provides excellent throughput while the global memory management ensures optimal cache performance and reduces fragmentation.

The key to success will be careful implementation of the async compression system and thorough testing of edge cases, especially around chunk state transitions and memory pressure scenarios.