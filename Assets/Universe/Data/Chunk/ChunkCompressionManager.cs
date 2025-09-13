using System;
using System.Threading;
using System.Threading.Tasks;
using UnityEngine;
using UnityEngine.Rendering;

namespace Universe.Data.Chunk {

	/**
	* Manages GPU-based compression and decompression of chunk data using compute shaders.
	* Uses AsyncGPUReadback for efficient data transfer between CPU and GPU.
	*/
	public class ChunkCompressionManager : MonoBehaviour {

		static readonly int CompressedInput = Shader.PropertyToID("CompressedInput");
		static readonly int ChunkOutput = Shader.PropertyToID("ChunkOutput");
		static readonly int ChunkMetadata = Shader.PropertyToID("ChunkMetadata");
		static readonly int ChunkInput = Shader.PropertyToID("ChunkInput");
		static readonly int ChunkCompressedOutput = Shader.PropertyToID("ChunkCompressedOutput");

		// Reference to the memory manager and its GPU resources
		ChunkMemoryManager MemoryManager { get => ChunkMemoryManager.Instance; }

		ComputeShader CompressionShader { get => MemoryManager.compressionShader; }
		ComputeBuffer GpuInputBuffer { get => MemoryManager.GPUInputBuffer; }
		ComputeBuffer GpuOutputBuffer { get => MemoryManager.GPUOutputBuffer; }
		ComputeBuffer GpuMetadataBuffer { get => MemoryManager.GPUMetadataBuffer; }

		public static ChunkCompressionManager Instance => FindFirstObjectByType<ChunkCompressionManager>();

		/**
		 * Compress a single chunk (wrapper around CompressChunk internal flow).
		 */
		public async Task<ChunkMemoryManager.CompressedChunk> CompressChunk(long chunkID) {
			// Use the single-chunk path
			// Get raw chunk data
			int[] rawData = MemoryManager.GetRawDataArray(chunkID);
			if(rawData == null || rawData.Length != 32 * 32 * 32) {
				throw new Exception($"Invalid chunk data for {chunkID}");
			}

			// Upload input
			GpuInputBuffer.SetData(rawData);

			// Zero out metadata and output before dispatch
			int maxOutputInts = 32 * 32 * 32 * 2; // conservative
			int[] zeroOutput = new int[maxOutputInts];
			GpuOutputBuffer.SetData(zeroOutput);
			uint[] zeroMeta = new uint[4];
			GpuMetadataBuffer.SetData(zeroMeta);

			// Dispatch compression shader (single group)
			int kernel = CompressionShader.FindKernel("CSMain");
			CompressionShader.SetBuffer(kernel, ChunkInput, GpuInputBuffer);
			CompressionShader.SetBuffer(kernel, ChunkCompressedOutput, GpuOutputBuffer);
			CompressionShader.SetBuffer(kernel, ChunkMetadata, GpuMetadataBuffer);
			CompressionShader.Dispatch(kernel, 1, 1, 1);

			// Read back metadata
			AsyncGPUReadbackRequest metaReq = AsyncGPUReadback.Request(GpuMetadataBuffer);
			while(!metaReq.done) {
				if(metaReq.hasError) throw new Exception("GPU metadata readback error");
				await Task.Yield();
			}
			int[] meta = metaReq.GetData<int>().ToArray();
			if(meta == null || meta.Length == 0) throw new Exception("GPU metadata readback returned no data");
			int compressedPayloadSize = meta[0];
			int offsetTableBytes = 32 * 32 * 4;
			if(compressedPayloadSize < 0) throw new Exception($"Invalid compressed payload size from GPU: {compressedPayloadSize}");
			int totalCompressedBytes = checked(compressedPayloadSize + offsetTableBytes);

			// Read back output buffer
			AsyncGPUReadbackRequest dataReq = AsyncGPUReadback.Request(GpuOutputBuffer);
			while(!dataReq.done) {
				if(dataReq.hasError) throw new Exception("GPU data readback error");
				await Task.Yield();
			}
			int[] compressedInts = dataReq.GetData<int>().ToArray();
			int availableBytes = compressedInts.Length * sizeof(int);
			if(totalCompressedBytes > availableBytes) throw new Exception($"Readback returned fewer bytes ({availableBytes}) than expected ({totalCompressedBytes})");
			if(compressedInts.Length > (totalCompressedBytes + 3) / 4) Array.Resize(ref compressedInts, (totalCompressedBytes + 3) / 4);
			byte[] compressedData = new byte[totalCompressedBytes];
			Buffer.BlockCopy(compressedInts, 0, compressedData, 0, totalCompressedBytes);

			// Basic validation of offset table
			int columnCount = 32 * 32;
			int payloadSize = compressedData.Length - offsetTableBytes;
			for(int i = 0; i < columnCount; ++i) {
				int idx = i * 4;
				if(idx + 4 > compressedData.Length) throw new Exception("Offset table truncated");
				uint off = BitConverter.ToUInt32(compressedData, idx);
				if(off > payloadSize) throw new Exception($"Invalid offset[{i}]={off} > payloadSize={payloadSize}");
				if(i > 0) {
					uint prev = BitConverter.ToUInt32(compressedData, (i - 1) * 4);
					if(off < prev) throw new Exception($"Offset table not monotonic at i={i}: prev={prev} cur={off}");
				}
			}

			// Reserve space in compressed pool atomically
			int reservedOffset = Interlocked.Add(ref MemoryManager._compressedPoolHead, totalCompressedBytes) - totalCompressedBytes;
			if(reservedOffset < 0) {
				// should not happen, but protect
				Interlocked.Exchange(ref MemoryManager._compressedPoolHead, 0);
				throw new Exception("Compressed pool negative head after reservation");
			}
			if(reservedOffset + totalCompressedBytes > MemoryManager._compressedPool.Length) {
				// rollback
				Interlocked.Add(ref MemoryManager._compressedPoolHead, -totalCompressedBytes);
				throw new Exception("Compressed pool out of memory");
			}
			// Copy into pool
			try {
				for(int i = 0; i < totalCompressedBytes; ++i) MemoryManager._compressedPool[reservedOffset + i] = compressedData[i];
			} catch(Exception ex) {
				// rollback
				Interlocked.Add(ref MemoryManager._compressedPoolHead, -totalCompressedBytes);
				throw new Exception($"Failed copying compressed data into pool: {ex.Message}");
			}

			// Update allocation and header
			if(!MemoryManager._allocations.TryGetValue(chunkID, out ChunkMemoryManager.ChunkAllocation alloc)) throw new Exception($"Chunk allocation not found for {chunkID}");
			alloc.CompressedOffset = reservedOffset;
			alloc.CompressedSize = totalCompressedBytes;
			alloc.State = ChunkMemoryManager.ChunkState.Compressed;
			int oldPoolIndex = alloc.PoolIndex;
			alloc.PoolIndex = -1;
			MemoryManager._allocations[chunkID] = alloc;

			if(MemoryManager._headers.TryGetValue(chunkID, out ChunkMemoryManager.ChunkHeader header)) {
				header.State = ChunkMemoryManager.ChunkState.Compressed;
				header.CompressedSize = totalCompressedBytes;
				header.IsDirty = false;
				MemoryManager._headers[chunkID] = header;
			} else {
				throw new Exception($"Chunk header not found for {chunkID}");
			}

			if(oldPoolIndex >= 0) MemoryManager._freeUncompressedSlots.Enqueue(oldPoolIndex);

			return new ChunkMemoryManager.CompressedChunk { ChunkID = chunkID, Offset = reservedOffset, Size = totalCompressedBytes };
		}

		/**
		* Decompress a chunk (single chunk path). Validates and writes back to uncompressed pool.
		*/
		public async Task<ChunkMemoryManager.ChunkHeader> DecompressChunk(long chunkID) {
			// validate allocation & header
			if(!MemoryManager._allocations.TryGetValue(chunkID, out ChunkMemoryManager.ChunkAllocation alloc)) throw new Exception($"Chunk allocation not found for {chunkID}");
			if(!MemoryManager._headers.TryGetValue(chunkID, out ChunkMemoryManager.ChunkHeader header)) throw new Exception($"Chunk header not found for {chunkID}");
			if(alloc.State != ChunkMemoryManager.ChunkState.Compressed) throw new Exception($"Chunk {chunkID} is not in compressed state");

			int compressedOffset = alloc.CompressedOffset;
			int compressedSize = alloc.CompressedSize;
			int originalSize = header.OriginalSize;

			if(compressedSize <= 0) throw new Exception("Invalid compressedSize in allocation");

			// allocate uncompressed slot
			if(MemoryManager._freeUncompressedSlots.Count == 0) throw new Exception("No free uncompressed slots available for decompression");
			int poolIndex = MemoryManager._freeUncompressedSlots.Dequeue();

			// copy compressed bytes into local array
			byte[] compressedData = new byte[compressedSize];
			for(int i = 0; i < compressedSize; ++i) compressedData[i] = MemoryManager._compressedPool[compressedOffset + i];

			// upload to GPU
			using(ComputeBuffer gpuCompressedBuffer = new ComputeBuffer((compressedSize + 3) / 4, sizeof(int), ComputeBufferType.Raw)) {
				int[] compressedInts = new int[(compressedSize + 3) / 4];
				Buffer.BlockCopy(compressedData, 0, compressedInts, 0, compressedSize);
				gpuCompressedBuffer.SetData(compressedInts);

				using(ComputeBuffer gpuOutputBuffer = new ComputeBuffer(32 * 32 * 32, sizeof(int))) {
					using(ComputeBuffer gpuMetadataBuffer = new ComputeBuffer(2, sizeof(uint))) {
						int offsetTableBytes = 32 * 32 * 4;
						if(compressedSize < offsetTableBytes) throw new Exception("Invalid compressedSize in pool");
						int payloadSize = compressedSize - offsetTableBytes;
						uint[] meta = { (uint)payloadSize, (uint)originalSize };
						gpuMetadataBuffer.SetData(meta);

						ComputeShader decompressionShader = MemoryManager.decompressionShader;
						int kernel = decompressionShader.FindKernel("CSMain");
						decompressionShader.SetBuffer(kernel, CompressedInput, gpuCompressedBuffer);
						decompressionShader.SetBuffer(kernel, ChunkOutput, gpuOutputBuffer);
						decompressionShader.SetBuffer(kernel, ChunkMetadata, gpuMetadataBuffer);
						decompressionShader.Dispatch(kernel, 1, 1, 1);

						// read back
						AsyncGPUReadbackRequest readback = AsyncGPUReadback.Request(gpuOutputBuffer);
						float startTime = Time.realtimeSinceStartup;
						float timeout = 5.0f;
						while(!readback.done) {
							if(readback.hasError) throw new Exception("GPU readback error (decompression)");
							if(Time.realtimeSinceStartup - startTime > timeout) throw new Exception("GPU readback timed out");
							await Task.Yield();
						}
						if(readback.hasError) throw new Exception("GPU readback error (decompression)");
						int[] decompressedInts = readback.GetData<int>().ToArray();
						if(decompressedInts.Length != 32 * 32 * 32) throw new Exception($"Decompressed data size mismatch: {decompressedInts.Length}");

						// optional CPU validation omitted here for speed
						int startIndex = poolIndex * 32 * 32 * 32;
						var uncompressedPool = MemoryManager._uncompressedPool;
						for(int i = 0; i < decompressedInts.Length; ++i) uncompressedPool[startIndex + i] = decompressedInts[i];

						// update allocation and header
						alloc.PoolIndex = poolIndex;
						alloc.CompressedOffset = -1;
						alloc.CompressedSize = 0;
						alloc.State = ChunkMemoryManager.ChunkState.Uncompressed;
						MemoryManager._allocations[chunkID] = alloc;

						header.State = ChunkMemoryManager.ChunkState.Uncompressed;
						header.CompressedSize = 0;
						header.IsDirty = false;
						MemoryManager._headers[chunkID] = header;

						return header;
					}
				}
			}
		}

		/**
		* For testing: force a chunk to be re-compressed on next access.
		* This does not immediately free any memory, it just marks the chunk as dirty.
		*/
		public void InvalidateChunk(long chunkID) {
			if(MemoryManager._headers.TryGetValue(chunkID, out ChunkMemoryManager.ChunkHeader header)) {
				header.IsDirty = true;
				MemoryManager._headers[chunkID] = header;
			}
		}

		/**
		* Compress multiple chunks in a single dispatch (batch).
		* Each chunk occupies a fixed PER_CHUNK_OUTPUT_BYTES region in the GPU output buffer.
		*/
		public async Task<ChunkMemoryManager.CompressedChunk[]> CompressChunks(long[] chunkIDs) {
			if(chunkIDs == null || chunkIDs.Length == 0) throw new ArgumentException("chunkIDs empty");
			int count = chunkIDs.Length;
			int batchCapacity = MemoryManager.CompressionBatchSize;
			if(count > batchCapacity) throw new ArgumentException($"Batch size {count} exceeds capacity {batchCapacity}");

			const int CHUNK_SIZE_X = 32, CHUNK_SIZE_Y = 32, CHUNK_SIZE_Z = 32;
			int COLUMN_COUNT = CHUNK_SIZE_X * CHUNK_SIZE_Y;
			int BLOCKS_PER_CHUNK = CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z;
			int offsetTableBytes = COLUMN_COUNT * 4;
			int perChunkOutputBytes = 262144; // must match shader PER_CHUNK_OUTPUT_BYTES
			int perChunkOutputInts = perChunkOutputBytes / 4;

			// Build combined input array (chunks concatenated)
			int[] combined = new int[count * BLOCKS_PER_CHUNK];
			for(int i = 0; i < count; ++i) {
				int[] raw = MemoryManager.GetRawDataArray(chunkIDs[i]);
				if(raw == null || raw.Length != BLOCKS_PER_CHUNK) throw new Exception($"Invalid raw data for chunk {chunkIDs[i]}");
				Array.Copy(raw, 0, combined, i * BLOCKS_PER_CHUNK, BLOCKS_PER_CHUNK);
			}

			// Upload input and clear output/metadata
			GpuInputBuffer.SetData(combined);
			int totalOutputInts = perChunkOutputInts * batchCapacity; // buffer was created with this size
			int[] zeroOut = new int[totalOutputInts];
			GpuOutputBuffer.SetData(zeroOut);
			uint[] zeroMeta = new uint[batchCapacity * 2];
			GpuMetadataBuffer.SetData(zeroMeta);

			// Dispatch groups = count
			int kernel = CompressionShader.FindKernel("CSMain");
			CompressionShader.SetBuffer(kernel, ChunkInput, GpuInputBuffer);
			CompressionShader.SetBuffer(kernel, ChunkCompressedOutput, GpuOutputBuffer);
			CompressionShader.SetBuffer(kernel, ChunkMetadata, GpuMetadataBuffer);
			CompressionShader.Dispatch(kernel, count, 1, 1);

			// Read back metadata and output
			AsyncGPUReadbackRequest metaReq = AsyncGPUReadback.Request(GpuMetadataBuffer);
			while(!metaReq.done) { if(metaReq.hasError) throw new Exception("GPU metadata readback error"); await Task.Yield(); }
			uint[] meta = metaReq.GetData<uint>().ToArray();

			AsyncGPUReadbackRequest dataReq = AsyncGPUReadback.Request(GpuOutputBuffer);
			while(!dataReq.done) { if(dataReq.hasError) throw new Exception("GPU data readback error"); await Task.Yield(); }
			int[] outputInts = dataReq.GetData<int>().ToArray();

			var results = new ChunkMemoryManager.CompressedChunk[count];
			for(int i = 0; i < count; ++i) {
				int payloadSize = (int)meta[i * 2 + 0];
				int totalBytes = offsetTableBytes + payloadSize;
				if(totalBytes <= 0) throw new Exception($"Invalid total size for chunk index {i}");
				int chunkBaseInt = i * perChunkOutputInts;
				int byteOffset = chunkBaseInt * 4;
				int availableBytes = outputInts.Length * 4 - byteOffset;
				if(totalBytes > availableBytes) throw new Exception($"Insufficient readback bytes for chunk {i}: needed={totalBytes} available={availableBytes}");
				byte[] compressedBytes = new byte[totalBytes];
				Buffer.BlockCopy(outputInts, byteOffset, compressedBytes, 0, totalBytes);

				// Reserve space in compressed pool
				int newHead = Interlocked.Add(ref MemoryManager._compressedPoolHead, totalBytes);
				int poolOffset = newHead - totalBytes;
				if(poolOffset + totalBytes > MemoryManager._compressedPool.Length) {
					Interlocked.Add(ref MemoryManager._compressedPoolHead, -totalBytes);
					throw new Exception("Compressed pool out of memory (batch)");
				}
				for(int b = 0; b < totalBytes; ++b) MemoryManager._compressedPool[poolOffset + b] = compressedBytes[b];

				long chunkID = chunkIDs[i];
				if(!MemoryManager._allocations.TryGetValue(chunkID, out ChunkMemoryManager.ChunkAllocation alloc)) throw new Exception($"Chunk allocation not found for {chunkID}");
				int oldPoolIndex = alloc.PoolIndex;
				alloc.CompressedOffset = poolOffset;
				alloc.CompressedSize = totalBytes;
				alloc.State = ChunkMemoryManager.ChunkState.Compressed;
				alloc.PoolIndex = -1;
				MemoryManager._allocations[chunkID] = alloc;

				if(MemoryManager._headers.TryGetValue(chunkID, out ChunkMemoryManager.ChunkHeader header)) {
					header.State = ChunkMemoryManager.ChunkState.Compressed;
					header.CompressedSize = totalBytes;
					header.IsDirty = false;
					MemoryManager._headers[chunkID] = header;
				} else {
					throw new Exception($"Chunk header not found for {chunkID}");
				}

				if(oldPoolIndex >= 0) MemoryManager._freeUncompressedSlots.Enqueue(oldPoolIndex);

				results[i] = new ChunkMemoryManager.CompressedChunk { ChunkID = chunkID, Offset = poolOffset, Size = totalBytes };
			}

			// Notify ChunkMemoryManager that these chunks have been compressed successfully
			for(int i = 0; i < count; ++i) {
				long chunkID = chunkIDs[i];
				try {
					MemoryManager.CompleteCompressionOperation(chunkID, true, null);
				} catch {
					// Non-fatal: best effort notification
				}
			}

			return results;
		}
	}
}
