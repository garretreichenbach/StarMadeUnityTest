using System;
using System.Threading;
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
			const int MAX_COMPRESS_RETRIES = 3;
			int attempt = 0;
			int compressedPayloadSize = 0;
			int totalCompressedBytes = 0;
			int[] compressedInts = null;
			byte[] compressedData = null;
			bool valid = false;
			while(attempt < MAX_COMPRESS_RETRIES && !valid) {
				attempt++;
				int kernel = CompressionShader.FindKernel("CSMain");
				CompressionShader.SetBuffer(kernel, ChunkInput, GpuInputBuffer);
				CompressionShader.SetBuffer(kernel, ChunkCompressedOutput, GpuOutputBuffer);
				CompressionShader.SetBuffer(kernel, ChunkMetadata, GpuMetadataBuffer);
				CompressionShader.Dispatch(kernel, 1, 1, 1);

				// Read back metadata
				AsyncGPUReadbackRequest metaRequest = AsyncGPUReadback.Request(GpuMetadataBuffer);
				while(!metaRequest.done) {
					if(metaRequest.hasError) break;
					await Task.Yield();
				}
				if(metaRequest.hasError) {
					if(attempt >= MAX_COMPRESS_RETRIES) throw new Exception("GPU readback error (metadata)");
					continue;
				}
				int[] meta = metaRequest.GetData<int>().ToArray();
				// Defensive: validate metadata length
				if(meta == null || meta.Length == 0) {
					if(attempt >= MAX_COMPRESS_RETRIES) throw new Exception("GPU metadata readback returned empty data");
					continue;
				}
				compressedPayloadSize = meta[0];
				int offsetTableBytes = 32 * 32 * 4;
				// Validate payload size
				if(compressedPayloadSize < 0) {
					Debug.LogWarning($"[CompressChunk] Invalid compressedPayloadSize={compressedPayloadSize} on attempt {attempt}, retrying");
					if(attempt >= MAX_COMPRESS_RETRIES) throw new Exception($"Invalid compressed payload size from GPU: {compressedPayloadSize}");
					continue;
				}
				totalCompressedBytes = checked(compressedPayloadSize + offsetTableBytes);

				// Read back output buffer
				AsyncGPUReadbackRequest dataRequest = AsyncGPUReadback.Request(GpuOutputBuffer);
				while(!dataRequest.done) {
					if(dataRequest.hasError) break;
					await Task.Yield();
				}
				if(dataRequest.hasError) {
					if(attempt >= MAX_COMPRESS_RETRIES) throw new Exception("GPU readback error (data)");
					continue;
				}
				compressedInts = dataRequest.GetData<int>().ToArray();
				// Defensive: ensure we have enough bytes in the readback to cover totalCompressedBytes
				int availableBytes = compressedInts.Length * sizeof(int);
				if(totalCompressedBytes > availableBytes) {
					Debug.LogWarning($"[CompressChunk] Readback availableBytes={availableBytes} less than expected totalCompressedBytes={totalCompressedBytes} on attempt {attempt}");
					if(attempt >= MAX_COMPRESS_RETRIES) throw new Exception($"Readback returned fewer bytes ({availableBytes}) than expected ({totalCompressedBytes})");
					continue;
				}
				if (compressedInts.Length > (totalCompressedBytes + 3) / 4) Array.Resize(ref compressedInts, (totalCompressedBytes + 3) / 4);
				compressedData = new byte[totalCompressedBytes];
				Buffer.BlockCopy(compressedInts, 0, compressedData, 0, totalCompressedBytes);

				// Validate offset table inside compressedData
				try {
					int columnCount = 32 * 32;
					int payloadSizeCheck = compressedData.Length - offsetTableBytes;
					uint prev = 0;
					for(int i = 0; i < columnCount; ++i) {
						uint off = BitConverter.ToUInt32(compressedData, i * 4);
						if(off > payloadSizeCheck) throw new Exception($"Invalid offset[{i}]={off} > payloadSize={payloadSizeCheck}");
						if(i > 0 && off < prev) throw new Exception($"Offset table not monotonic at i={i}: prev={prev} cur={off}");
						prev = off;
					}
					valid = true;
				} catch(Exception ex) {
					Debug.LogWarning($"[CompressChunk] Validation failed on attempt {attempt}: {ex.Message}");
					valid = false;
					// loop will retry
				}
			}
			if(!valid) {
				throw new Exception($"Compression produced invalid offset table after {MAX_COMPRESS_RETRIES} attempts");
			}

			// Allocate space in compressed pool and update memory manager
			// Reserve space atomically in the compressed pool (safe even if accessed elsewhere)
			int offset;
			int newHead = Interlocked.Add(ref MemoryManager._compressedPoolHead, totalCompressedBytes);
			offset = newHead - totalCompressedBytes;
			// Defensive checks
			if(offset < 0) {
				Debug.LogWarning($"[CompressChunk] Compressed pool head negative ({MemoryManager._compressedPoolHead - totalCompressedBytes}), clamping to 0");
				offset = 0;
				Interlocked.Exchange(ref MemoryManager._compressedPoolHead, totalCompressedBytes);
			}
			if(offset + totalCompressedBytes > MemoryManager._compressedPool.Length) {
				// Roll back reservation
				Interlocked.Add(ref MemoryManager._compressedPoolHead, -totalCompressedBytes);
				throw new Exception("Compressed pool out of memory");
			}
			// Copy to compressed pool with try/catch to provide clearer errors
			var pool = MemoryManager._compressedPool;
			try {
				for(int i = 0; i < totalCompressedBytes; i++) {
					pool[offset + i] = compressedData[i];
				}
			} catch(Exception ex) {
				// Roll back reservation on failure
				Interlocked.Add(ref MemoryManager._compressedPoolHead, -totalCompressedBytes);
				Debug.LogError($"[CompressChunk] Failed copying compressed data into pool at offset={offset}, len={totalCompressedBytes}: {ex.Message}");
				throw;
			}

			// Update allocation and header
			if(!MemoryManager._allocations.TryGetValue(chunkID, out ChunkMemoryManager.ChunkAllocation alloc)) {
				throw new Exception($"Chunk allocation not found for {chunkID}");
			}
			alloc.CompressedOffset = offset;
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

			// Free uncompressed slot
			if(oldPoolIndex >= 0)
				MemoryManager._freeUncompressedSlots.Enqueue(oldPoolIndex);

			// Return compressed chunk info
			return new ChunkMemoryManager.CompressedChunk {
				ChunkID = chunkID,
				Offset = offset,
				Size = totalCompressedBytes,
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
			// Use a raw (byte-addressable) buffer because the decompressor expects a ByteAddressBuffer
			using ComputeBuffer gpuCompressedBuffer = new ComputeBuffer((compressedSize + 3) / 4, sizeof(int), ComputeBufferType.Raw);
			int[] compressedInts = new int[(compressedSize + 3) / 4];
			Buffer.BlockCopy(compressedData, 0, compressedInts, 0, compressedSize);
			gpuCompressedBuffer.SetData(compressedInts);

			// Prepare output buffer (for decompressed ints)
			using ComputeBuffer gpuOutputBuffer = new ComputeBuffer(32 * 32 * 32, sizeof(int));
			// Prepare metadata buffer
			using ComputeBuffer gpuMetadataBuffer = new ComputeBuffer(2, sizeof(uint));
			int offsetTableBytes = 32 * 32 * 4; // COLUMN_COUNT * 4
			if(compressedSize < offsetTableBytes) throw new Exception("Invalid compressedSize in pool");
			int payloadSize = (compressedSize - offsetTableBytes);
			// ChunkMetadata[0] expects the payload size (bytes after offset table)
			uint[] meta = { (uint)payloadSize, (uint)originalSize };
			gpuMetadataBuffer.SetData(meta);

			// Dispatch decompression compute shader
			ComputeShader decompressionShader = MemoryManager.decompressionShader;
			int kernel = decompressionShader.FindKernel("CSMain");
			decompressionShader.SetBuffer(kernel, CompressedInput, gpuCompressedBuffer);
			decompressionShader.SetBuffer(kernel, ChunkOutput, gpuOutputBuffer);
			decompressionShader.SetBuffer(kernel, ChunkMetadata, gpuMetadataBuffer);
			// See note above: use a single group so the shader runs 32x32 threads as intended.
			decompressionShader.Dispatch(kernel, 1, 1, 1);

			// Read back decompressed data with timeout
			AsyncGPUReadbackRequest readback = AsyncGPUReadback.Request(gpuOutputBuffer);
			float startTime = Time.realtimeSinceStartup;
			float timeout = 5.0f; // 5 seconds max for GPU readback
			while(!readback.done) {
				if(readback.hasError) {
					Debug.LogError($"[DecompressChunk] GPU readback error (decompression) for chunk {chunkID}");
					throw new Exception("GPU readback error (decompression)");
				}
				if(Time.realtimeSinceStartup - startTime > timeout) {
					Debug.LogError($"[DecompressChunk] GPU readback timed out for chunk {chunkID}");
					throw new Exception("GPU readback timed out");
				}
				await Task.Yield();
			}
			if(readback.hasError) {
				Debug.LogError($"[DecompressChunk] GPU readback error (decompression) for chunk {chunkID} (after done)");
				throw new Exception("GPU readback error (decompression)");
			}
			int[] decompressedInts = readback.GetData<int>().ToArray();
			if(decompressedInts.Length != 32 * 32 * 32) {
				Debug.LogError($"[DecompressChunk] Decompressed data size mismatch: {decompressedInts.Length} for chunk {chunkID}");
				throw new Exception($"Decompressed data size mismatch: {decompressedInts.Length}");
			}

			// CPU-side validation: decompress compressedData on CPU and compare to GPU result
			int offsetTableBytes_check = 32 * 32 * 4;
			Debug.Log($"[DecompressChunk] Debug: compressedSize={compressedSize}, compressedData.Length={compressedData.Length}, offsetTableBytes={offsetTableBytes_check}");
			if(compressedData.Length < offsetTableBytes_check) {
				Debug.LogError($"[DecompressChunk] Compressed data is smaller than offset table: {compressedData.Length} < {offsetTableBytes_check}");
				throw new Exception("Compressed data too small for offset table");
			}
			// Validate offset table entries first
			int columnCount = 32 * 32;
			payloadSize = compressedData.Length - offsetTableBytes_check;
			uint[] offsets = new uint[columnCount];
			for(int i = 0; i < columnCount; ++i) {
				int idx = i * 4;
				if(idx + 4 <= compressedData.Length) offsets[i] = BitConverter.ToUInt32(compressedData, idx);
				else offsets[i] = (uint)payloadSize; // defensive
			}
			// Check monotonicity and bounds; be tolerant and clamp small errors instead of failing hard
			for(int i = 0; i < columnCount; ++i) {
				uint o = offsets[i];
				if(o > payloadSize) {
					// If offset is only slightly past the payload (e.g. by alignment), clamp and warn
					Debug.LogWarning($"[DecompressChunk] Offset[{i}]={o} > payloadSize={payloadSize}, clamping to payloadSize");
					offsets[i] = (uint)payloadSize;
					o = offsets[i];
				}
				if(i > 0 && offsets[i] < offsets[i-1]) {
					// Fix minor non-monotonicity by forcing non-decreasing offsets; log for diagnosis
					Debug.LogWarning($"[DecompressChunk] Offset table not monotonic at i={i}: prev={offsets[i-1]} cur={offsets[i]}, fixing by setting to prev");
					offsets[i] = offsets[i-1];
				}
			}
			// Dump a small sample of offsets for debugging
			string offsDump = "";
			for(int i = 0; i < Math.Min(16, columnCount); ++i) offsDump += offsets[i].ToString() + " ";
			Debug.Log($"[DecompressChunk] offsetTable sample (first 16): {offsDump}");

			try {
				int[] cpuDecompressed = CPUDecompress(compressedData);
				if(cpuDecompressed.Length != decompressedInts.Length) {
					Debug.LogError($"[DecompressChunk] CPU decompressed length mismatch: {cpuDecompressed.Length} vs GPU {decompressedInts.Length}");
				} else {
					for(int i = 0; i < decompressedInts.Length; i++) {
						if(decompressedInts[i] != cpuDecompressed[i]) {
							int idx = i;
							int columnIndex = idx / 32;
							int z = idx % 32;
							int x = columnIndex % 32;
							int y = columnIndex / 32;
							Debug.LogError($"[DecompressChunk] GPU vs CPU mismatch at index {idx} (x={x},y={y},z={z}): gpu={decompressedInts[i]}, cpu={cpuDecompressed[i]}");
							// Dump offsets for this column
							offsetTableBytes = 32 * 32 * 4;
							uint colOffset = BitConverter.ToUInt32(compressedData, columnIndex * 4);
							uint nextOffset = (columnIndex < 32 * 32 - 1) ? BitConverter.ToUInt32(compressedData, (columnIndex + 1) * 4) : (uint)(compressedData.Length - offsetTableBytes);
							Debug.LogError($"[DecompressChunk] columnIndex={columnIndex} colOffset={colOffset} nextOffset={nextOffset} payloadSize={(compressedData.Length - offsetTableBytes)} totalCompressed={compressedData.Length}");
							throw new Exception("GPU vs CPU decompressed mismatch detected (see logs)");
						}
					}
				}
			} catch(Exception ex) {
				Debug.LogError($"[DecompressChunk] CPU validation exception: {ex.Message}");
				throw;
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

		int[] CPUDecompress(byte[] compressedStream) {
			int COLUMN_COUNT = 32 * 32;
			int CHUNK_SIZE_Z = 32;
			int offsetTableBytes = COLUMN_COUNT * 4;
			int payloadSize = compressedStream.Length - offsetTableBytes;
			int[] outArr = new int[COLUMN_COUNT * CHUNK_SIZE_Z];
			for(int col = 0; col < COLUMN_COUNT; col++) {
				int offsetIndex = col * 4;
				if(offsetIndex + 4 > compressedStream.Length) {
					throw new IndexOutOfRangeException($"Offset table read out of range: offsetIndex={offsetIndex}, compressedStream.Length={compressedStream.Length}");
				}
				uint colOffset = BitConverter.ToUInt32(compressedStream, offsetIndex);
				uint nextOffset = (col < COLUMN_COUNT - 1) ? BitConverter.ToUInt32(compressedStream, (col + 1) * 4) : (uint)payloadSize;
				int readPtr = offsetTableBytes + (int)colOffset;
				int endPtr = offsetTableBytes + (int)nextOffset;
				int outBase = col * CHUNK_SIZE_Z;
				int outIdx = 0;
				while(readPtr + 8 <= endPtr && outIdx < CHUNK_SIZE_Z) {
					if(readPtr + 8 > compressedStream.Length) {
						throw new IndexOutOfRangeException($"Compressed payload read out of range: readPtr={readPtr}, needed=8, length={compressedStream.Length}");
					}
					int value = BitConverter.ToInt32(compressedStream, readPtr);
					uint runLength = BitConverter.ToUInt32(compressedStream, readPtr + 4);
					readPtr += 8;
					for(uint r = 0; r < runLength && outIdx < CHUNK_SIZE_Z; ++r) {
						outArr[outBase + outIdx] = value;
						outIdx++;
					}
				}
				for(; outIdx < CHUNK_SIZE_Z; ++outIdx) outArr[outBase + outIdx] = 0;
			}
			return outArr;
		}
	}
}
