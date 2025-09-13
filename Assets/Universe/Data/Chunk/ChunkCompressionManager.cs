using System;
using System.Threading.Tasks;
using UnityEngine;
using UnityEngine.Rendering;
using EngineSettings = Settings.EngineSettings;

namespace Universe.Data.Chunk {

	/**
	* Manages GPU-based compression and decompression of chunk data using compute shaders.
	* `	Uses AsyncGPUReadback for efficient data transfer between CPU and GPU.
	*/
	public class ChunkCompressionManager : MonoBehaviour {

		static readonly int CompressedInput = Shader.PropertyToID("CompressedInput");
		static readonly int ChunkOutput = Shader.PropertyToID("ChunkOutput");
		static readonly int ChunkMetadata = Shader.PropertyToID("ChunkMetadata");
		static readonly int ChunkInput = Shader.PropertyToID("ChunkInput");
		static readonly int ChunkCompressedOutput = Shader.PropertyToID("ChunkCompressedOutput");

		// Reference to the memory manager and its GPU resources
		ChunkMemoryManager MemoryManager {
			get => ChunkMemoryManager.Instance;
		}

		ComputeShader CompressionShader {
			get => MemoryManager.compressionShader;
		}

		ComputeBuffer GpuInputBuffer {
			get => MemoryManager.GPUInputBuffer;
		}

		ComputeBuffer GpuOutputBuffer {
			get => MemoryManager.GPUOutputBuffer;
		}

		ComputeBuffer GpuMetadataBuffer {
			get => MemoryManager.GPUMetadataBuffer;
		}

		float GPUReadbackTimeoutSeconds {
			get => EngineSettings.Instance.MaxGPUReadbackTimeout.Value;
		}

		public static ChunkCompressionManager Instance => FindFirstObjectByType<ChunkCompressionManager>();

		/**
		* Compresses a chunk and returns the compressed data.
		* Note: This should only be called by ChunkMemoryManager. If you need to compress a chunk, use MemoryManager.CompressChunk() instead.
		*/
		public async Task<ChunkMemoryManager.CompressedChunk> CompressChunk(long chunkID) {
			// Get raw chunk data
			int[] rawData = MemoryManager.GetRawDataArray(chunkID);
			if(rawData == null || rawData.Length != 32 * 32 * 32) {
				throw new Exception($"Invalid chunk data for {chunkID}");
			}

			// Upload chunk data to GPU
			GpuInputBuffer.SetData(rawData);
			// Clear output and metadata buffers
			int maxOutputInts = 32 * 32 * 32 * 2; // Worst case: no compression
			int[] zeroOutput = new int[maxOutputInts];
			GpuOutputBuffer.SetData(zeroOutput);
			uint[] zeroMeta = new uint[4];
			GpuMetadataBuffer.SetData(zeroMeta);

			// Dispatch compression compute shader
			int kernel = CompressionShader.FindKernel("CSMain");
			CompressionShader.SetBuffer(kernel, ChunkInput, GpuInputBuffer);
			CompressionShader.SetBuffer(kernel, ChunkCompressedOutput, GpuOutputBuffer);
			CompressionShader.SetBuffer(kernel, ChunkMetadata, GpuMetadataBuffer);
			CompressionShader.Dispatch(kernel, 32, 32, 1);

			// Read back compressed data and metadata
			AsyncGPUReadbackRequest metaRequest = AsyncGPUReadback.Request(GpuMetadataBuffer);
			while(!metaRequest.done) {
				if(metaRequest.hasError) {
					throw new Exception("GPU readback error (metadata)");
				}
				await Task.Yield();
			}
			if(metaRequest.hasError) {
				throw new Exception("GPU readback error (metadata)");
			}
			int[] meta = metaRequest.GetData<int>().ToArray();
			// Only the first 4 values are valid for a single chunk
			// Debug.Log($"[CompressChunk] chunkID={chunkID} meta array (first 4): [{meta[0]}, {meta[1]}, {meta[2]}, {meta[3]}]");
			int compressedSize = meta[0];
			// Log for debugging
			// Debug.Log($"[CompressChunk] chunkID={chunkID} compressedSize={compressedSize} rawData.Length={rawData.Length}");
			if(compressedSize <= 0 || compressedSize > rawData.Length * sizeof(int) * 2) {
				Debug.LogError($"[CompressChunk] Invalid compressed size: {compressedSize} (chunkID={chunkID})");
				throw new Exception($"Invalid compressed size: {compressedSize}");
			}
			// Hard cap: compressedSize must not exceed output buffer size in bytes
			int outputBufferBytes = GpuOutputBuffer.count * sizeof(int);
			if (compressedSize > outputBufferBytes) {
				Debug.LogError($"[CompressChunk] compressedSize {compressedSize} exceeds output buffer size {outputBufferBytes} bytes (chunkID={chunkID})");
				throw new Exception($"Compressed size exceeds output buffer size");
			}
			// Check alignment (optional, for debugging)
			if (compressedSize % 4 != 0) {
				Debug.LogWarning($"[CompressChunk] compressedSize {compressedSize} is not a multiple of 4 (chunkID={chunkID})");
			}

			// Compute element count for int buffer
			int elementCount = (compressedSize + 3) / 4; // Round up to cover all bytes
			// Debug.Log($"[CompressChunk] chunkID={chunkID} elementCount={elementCount} GpuOutputBuffer.count={GpuOutputBuffer.count}");
			if(elementCount <= 0) {
				Debug.LogError($"[CompressChunk] Invalid element count for GPU readback: {elementCount} (compressedSize={compressedSize}, chunkID={chunkID})");
				throw new Exception($"Invalid element count for GPU readback: {elementCount} (compressedSize={compressedSize})");
			}
			if(elementCount > GpuOutputBuffer.count) {
				Debug.LogError($"[CompressChunk] Element count {elementCount} exceeds GpuOutputBuffer.count {GpuOutputBuffer.count} (chunkID={chunkID})");
				throw new Exception($"Element count {elementCount} exceeds GpuOutputBuffer.count {GpuOutputBuffer.count}");
			}
			// Defensive logging before readback
			// Debug.Log($"[CompressChunk] About to request GPU readback: offset=0, elementCount={elementCount}, bufferCount={GpuOutputBuffer.count}");
			if(elementCount > GpuOutputBuffer.count) {
				Debug.LogError($"[CompressChunk] elementCount exceeds buffer count before readback! chunkID={chunkID}");
				throw new Exception("elementCount exceeds buffer count");
			}

			// Use the simpler overload to avoid Unity's offset/size bug
			AsyncGPUReadbackRequest dataRequest = AsyncGPUReadback.Request(GpuOutputBuffer);
			// Debug.Log($"[CompressChunk] Requested GPU readback (simple overload): done={dataRequest.done}, hasError={dataRequest.hasError}, layerCount={dataRequest.layerCount}, width={dataRequest.width}, height={dataRequest.height}");
			while(!dataRequest.done) {
				if(dataRequest.hasError) {
					throw new Exception("GPU readback error (data)");
				}
				await Task.Yield();
			}
			if(dataRequest.hasError) {
				throw new Exception("GPU readback error (data)");
			}
			int[] compressedInts = dataRequest.GetData<int>().ToArray();
			if (compressedInts.Length < elementCount) {
				throw new Exception($"Readback returned fewer elements than expected: {compressedInts.Length} < {elementCount}");
			}
			// Only use the first elementCount elements
			if (compressedInts.Length > elementCount) {
				Array.Resize(ref compressedInts, elementCount);
			}
			// Log first 16 bytes of output buffer for debugging
			string firstBytes = string.Join(", ", compressedInts.Length > 4 ? new ArraySegment<int>(compressedInts, 0, 4) : compressedInts);
			// Debug.Log($"[CompressChunk] chunkID={chunkID} first 16 bytes of output buffer: [{firstBytes}]");
			// Convert int[] to byte[]
			byte[] compressedData = new byte[compressedSize];
			Buffer.BlockCopy(compressedInts, 0, compressedData, 0, compressedSize);

			// Allocate space in compressed pool and update memory manager
			int offset = MemoryManager._compressedPoolHead;
			if(offset + compressedSize > MemoryManager._compressedPool.Length) {
				throw new Exception("Compressed pool out of memory");
			}
			// Copy to compressed pool
			var pool = MemoryManager._compressedPool;
			for(int i = 0; i < compressedSize; i++) {
				pool[offset + i] = compressedData[i];
			}
			MemoryManager._compressedPoolHead += compressedSize;

			// Update allocation and header
			if(!MemoryManager._allocations.TryGetValue(chunkID, out ChunkMemoryManager.ChunkAllocation alloc)) {
				throw new Exception($"Chunk allocation not found for {chunkID}");
			}
			alloc.CompressedOffset = offset;
			alloc.CompressedSize = compressedSize;
			alloc.State = ChunkMemoryManager.ChunkState.Compressed;
			int oldPoolIndex = alloc.PoolIndex;
			alloc.PoolIndex = -1;
			MemoryManager._allocations[chunkID] = alloc;

			if(MemoryManager._headers.TryGetValue(chunkID, out ChunkMemoryManager.ChunkHeader header)) {
				header.State = ChunkMemoryManager.ChunkState.Compressed;
				header.CompressedSize = compressedSize;
				header.IsDirty = false;
				MemoryManager._headers[chunkID] = header;
			}

			// Free uncompressed slot
			if(oldPoolIndex >= 0)
				MemoryManager._freeUncompressedSlots.Enqueue(oldPoolIndex);

			// Return compressed chunk info
			return new ChunkMemoryManager.CompressedChunk {
				ChunkID = chunkID,
				Offset = offset,
				Size = compressedSize,
			};
		}

		/**
		* Decompresses a chunk and returns the uncompressed data.
		* Note: This should only be called by ChunkMemoryManager. If you need to decompress a chunk, use MemoryManager.DecompressChunk() instead.
		*/
		public async Task<ChunkMemoryManager.ChunkHeader> DecompressChunk(long chunkID) {
			// Get allocation and header
			if(!MemoryManager._allocations.TryGetValue(chunkID, out ChunkMemoryManager.ChunkAllocation alloc)) {
				throw new Exception($"Chunk allocation not found for {chunkID}");
			}
			if(!MemoryManager._headers.TryGetValue(chunkID, out ChunkMemoryManager.ChunkHeader header)) {
				throw new Exception($"Chunk header not found for {chunkID}");
			}
			if(alloc.State != ChunkMemoryManager.ChunkState.Compressed) {
				throw new Exception($"Chunk {chunkID} is not in compressed state");
			}

			int compressedOffset = alloc.CompressedOffset;
			int compressedSize = alloc.CompressedSize;
			int originalSize = header.OriginalSize;

			// Allocate uncompressed slot
			if(MemoryManager._freeUncompressedSlots.Count == 0)
				throw new Exception("No free uncompressed slots available for decompression");
			int poolIndex = MemoryManager._freeUncompressedSlots.Dequeue();

			// Prepare compressed input buffer (with offset table at start)
			byte[] compressedData = new byte[compressedSize];
			var pool = MemoryManager._compressedPool;
			for(int i = 0; i < compressedSize; i++) {
				compressedData[i] = pool[compressedOffset + i];
			}
			// Upload to GPU
			using ComputeBuffer gpuCompressedBuffer = new ComputeBuffer((compressedSize + 3) / 4, sizeof(int));
			int[] compressedInts = new int[(compressedSize + 3) / 4];
			Buffer.BlockCopy(compressedData, 0, compressedInts, 0, compressedSize);
			gpuCompressedBuffer.SetData(compressedInts);

			// Prepare output buffer (for decompressed ints)
			using ComputeBuffer gpuOutputBuffer = new ComputeBuffer(32 * 32 * 32, sizeof(int));
			// Prepare metadata buffer
			using ComputeBuffer gpuMetadataBuffer = new ComputeBuffer(2, sizeof(uint));
			uint[] meta = { (uint)compressedSize, (uint)originalSize };
			gpuMetadataBuffer.SetData(meta);

			// Dispatch decompression compute shader
			ComputeShader decompressionShader = MemoryManager.decompressionShader;
			int kernel = decompressionShader.FindKernel("CSMain");
			decompressionShader.SetBuffer(kernel, CompressedInput, gpuCompressedBuffer);
			decompressionShader.SetBuffer(kernel, ChunkOutput, gpuOutputBuffer);
			decompressionShader.SetBuffer(kernel, ChunkMetadata, gpuMetadataBuffer);
			decompressionShader.Dispatch(kernel, 32, 32, 1);

			// Read back decompressed data
			AsyncGPUReadbackRequest readback = AsyncGPUReadback.Request(gpuOutputBuffer);
			while(!readback.done) {
				if(readback.hasError) {
					throw new Exception("GPU readback error (decompression)");
				}
				await Task.Yield();
			}
			if(readback.hasError) {
				throw new Exception("GPU readback error (decompression)");
			}
			int[] decompressedInts = readback.GetData<int>().ToArray();
			if(decompressedInts.Length != 32 * 32 * 32) {
				throw new Exception($"Decompressed data size mismatch: {decompressedInts.Length}");
			}

			// Write to uncompressed pool
			int startIndex = poolIndex * 32 * 32 * 32;
			var uncompressedPool = MemoryManager._uncompressedPool;
			for(int i = 0; i < decompressedInts.Length; i++) {
				uncompressedPool[startIndex + i] = decompressedInts[i];
			}

			// Update allocation and header
			alloc.PoolIndex = poolIndex;
			alloc.CompressedOffset = -1;
			alloc.CompressedSize = 0;
			alloc.State = ChunkMemoryManager.ChunkState.Uncompressed;
			MemoryManager._allocations[chunkID] = alloc;

			header.State = ChunkMemoryManager.ChunkState.Uncompressed;
			header.CompressedSize = 0;
			header.IsDirty = false;
			MemoryManager._headers[chunkID] = header;

			// Free compressed slot (handled by MemoryManager if needed)
			return header;
		}
	}
}