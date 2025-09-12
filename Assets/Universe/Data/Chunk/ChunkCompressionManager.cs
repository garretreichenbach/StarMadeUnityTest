using System;
using System.Threading.Tasks;
using UnityEngine;
using UnityEngine.Rendering;

namespace Universe.Data.Chunk {

    /**
	 * Manages GPU-based compression and decompression of chunk data using compute shaders.
	 * Utilizes AsyncGPUReadback for efficient data transfer between CPU and GPU.
	 */
    public class ChunkCompressionManager {
		// Reference to the memory manager and its GPU resources
		ChunkMemoryManager MemoryManager {
			get => ChunkMemoryManager.Instance;
		}

		ComputeShader CompressionShader {
			get => MemoryManager.compressionShader;
		}

		ComputeBuffer GpuInputBuffer {
			get => MemoryManager._gpuInputBuffer;
		}

		ComputeBuffer GpuOutputBuffer {
			get => MemoryManager._gpuOutputBuffer;
		}

		ComputeBuffer GpuMetadataBuffer {
			get => MemoryManager._gpuMetadataBuffer;
		}

		public async Task<ChunkMemoryManager.CompressedChunk> CompressChunkAsync(long chunkID) {
			// Get raw chunk data
			int[] rawData = MemoryManager.GetRawDataArray(chunkID);
			if(rawData == null || rawData.Length != 32 * 32 * 32)
				throw new Exception($"Invalid chunk data for {chunkID}");

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
			CompressionShader.SetBuffer(kernel, "ChunkInput", GpuInputBuffer);
			CompressionShader.SetBuffer(kernel, "ChunkCompressedOutput", GpuOutputBuffer);
			CompressionShader.SetBuffer(kernel, "ChunkMetadata", GpuMetadataBuffer);
			CompressionShader.Dispatch(kernel, 32, 32, 1);

			// Read back compressed data and metadata
			AsyncGPUReadbackRequest metaRequest = AsyncGPUReadback.Request(GpuMetadataBuffer);
			while(!metaRequest.done) await Task.Yield();
			if(metaRequest.hasError) throw new Exception("GPU readback error (metadata)");
			uint[] meta = metaRequest.GetData<uint>().ToArray();
			int compressedSize = (int)meta[0];
			if(compressedSize <= 0 || compressedSize > rawData.Length * sizeof(int) * 2)
				throw new Exception($"Invalid compressed size: {compressedSize}");

			// Compute element count for int buffer
			int elementCount = (compressedSize + 3) / 4; // Round up to cover all bytes
			AsyncGPUReadbackRequest dataRequest = AsyncGPUReadback.Request(GpuOutputBuffer, 0, elementCount);
			while(!dataRequest.done) await Task.Yield();
			if(dataRequest.hasError) throw new Exception("GPU readback error (data)");
			int[] compressedInts = dataRequest.GetData<int>().ToArray();
			// Convert int[] to byte[]
			byte[] compressedData = new byte[compressedSize];
			Buffer.BlockCopy(compressedInts, 0, compressedData, 0, compressedSize);

			// Allocate space in compressed pool and update memory manager
			int offset = MemoryManager._compressedPoolHead;
			if(offset + compressedSize > MemoryManager._compressedPool.Length)
				throw new Exception("Compressed pool out of memory");
			// Copy to compressed pool
			var pool = MemoryManager._compressedPool;
			for(int i = 0; i < compressedSize; i++) {
				pool[offset + i] = compressedData[i];
			}
			MemoryManager._compressedPoolHead += compressedSize;

			// Update allocation and header
			if(!MemoryManager._allocations.TryGetValue(chunkID, out ChunkMemoryManager.ChunkAllocation alloc))
				throw new Exception($"Chunk allocation not found for {chunkID}");
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

		public async Task<bool> DecompressChunkAsync(long chunkID) {
			// Get allocation and header
			if(!MemoryManager._allocations.TryGetValue(chunkID, out ChunkMemoryManager.ChunkAllocation alloc))
				throw new Exception($"Chunk allocation not found for {chunkID}");
			if(!MemoryManager._headers.TryGetValue(chunkID, out ChunkMemoryManager.ChunkHeader header))
				throw new Exception($"Chunk header not found for {chunkID}");
			if(alloc.State != ChunkMemoryManager.ChunkState.Compressed)
				throw new Exception($"Chunk {chunkID} is not in compressed state");

			int compressedOffset = alloc.CompressedOffset;
			int compressedSize = alloc.CompressedSize;
			int originalSize = header.OriginalSize;

			// Allocate uncompressed slot
			if(MemoryManager._freeUncompressedSlots.Count == 0)
				throw new Exception("No free uncompressed slots available for decompression");
			int poolIndex = MemoryManager._freeUncompressedSlots.Dequeue();

			// Prepare compressed input buffer (with offset table at start)
			// For now, assume the offset table is already present in the compressed data
			// (If not, it needs to be built here)
			byte[] compressedData = new byte[compressedSize];
			var pool = MemoryManager._compressedPool;
			for(int i = 0; i < compressedSize; i++) {
				compressedData[i] = pool[compressedOffset + i];
			}
			// Upload to GPU
			using(ComputeBuffer gpuCompressedBuffer = new ComputeBuffer((compressedSize + 3) / 4, sizeof(int))) {
				int[] compressedInts = new int[(compressedSize + 3) / 4];
				Buffer.BlockCopy(compressedData, 0, compressedInts, 0, compressedSize);
				gpuCompressedBuffer.SetData(compressedInts);

				// Prepare output buffer (for decompressed ints)
				using(ComputeBuffer gpuOutputBuffer = new ComputeBuffer(32 * 32 * 32, sizeof(int))) {
					// Prepare metadata buffer
					using(ComputeBuffer gpuMetadataBuffer = new ComputeBuffer(2, sizeof(uint))) {
						uint[] meta = new uint[2] { (uint)compressedSize, (uint)originalSize };
						gpuMetadataBuffer.SetData(meta);

						// Dispatch decompression compute shader
						ComputeShader decompressionShader = MemoryManager.decompressionShader;
						int kernel = decompressionShader.FindKernel("CSMain");
						decompressionShader.SetBuffer(kernel, "CompressedInput", gpuCompressedBuffer);
						decompressionShader.SetBuffer(kernel, "ChunkOutput", gpuOutputBuffer);
						decompressionShader.SetBuffer(kernel, "ChunkMetadata", gpuMetadataBuffer);
						decompressionShader.Dispatch(kernel, 32, 32, 1);

						//n Read back decompressed data
						AsyncGPUReadbackRequest readback = AsyncGPUReadback.Request(gpuOutputBuffer);
						while(!readback.done) await Task.Yield();
						if(readback.hasError) throw new Exception("GPU readback error (decompression)");
						int[] decompressedInts = readback.GetData<int>().ToArray();
						if(decompressedInts.Length != 32 * 32 * 32)
							throw new Exception($"Decompressed data size mismatch: {decompressedInts.Length}");

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
					}
				}
			}

			// Free compressed slot
			// Memory Manager should handle this automatically (I think)
			return true;
		}
	}
}