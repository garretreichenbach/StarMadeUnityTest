using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using UnityEngine;
using UnityEngine.Rendering;
using EngineSettings = Settings.EngineSettings;

namespace Universe.Data.Chunk {

	/**
	* Manages GPU-based compression and decompression of chunk data using compute shaders.
	* Uses AsyncGPUReadback for efficient data transfer between CPU and GPU.
	 * Todo: Implement processing for multiple chunks per batch so we can better utilize the GPU.
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

		class BufferSet {
			public ComputeBuffer Input;
			public ComputeBuffer CompressionOutput;
			public ComputeBuffer DecompressionOutput;
			public ComputeBuffer Metadata;
		}

		BufferSet[] _bufferPool;
		Queue<int> _availableBuffers;
		object _bufferPoolLock = new object();

		void Start() {
			int bufferPoolSize = EngineSettings.Instance.GPUCompressionBufferPoolSize.Value;
			_bufferPool = new BufferSet[bufferPoolSize];
			_availableBuffers = new Queue<int>(bufferPoolSize);

			for(int i = 0; i < bufferPoolSize; i++) {
				_bufferPool[i] = new BufferSet {
					Input = new ComputeBuffer(32 * 32 * 32, sizeof(int)),
					CompressionOutput = new ComputeBuffer(32 * 32 * 32 * 2, sizeof(int)),
					DecompressionOutput = new ComputeBuffer(32 * 32 * 32, sizeof(int)),
					Metadata = new ComputeBuffer(4, sizeof(uint)),
				};
				_availableBuffers.Enqueue(i);
			}
		}

		int AcquireBufferSet() {
			lock(_bufferPoolLock) {
				while(_availableBuffers.Count == 0) Monitor.Wait(_bufferPoolLock);
				return _availableBuffers.Dequeue();
			}
		}
		void ReleaseBufferSet(int idx) {
			lock(_bufferPoolLock) {
				_availableBuffers.Enqueue(idx);
				Monitor.Pulse(_bufferPoolLock);
			}
		}

		// Fallback CPU compression method for headless servers without GPU support
		void CompressChunkCPU(long chunkID) {
			// Todo: Implement a CPU-based fallback compression algorithm
			throw new NotImplementedException();
		}

		// Fallback CPU decompression method for headless servers without GPU support
		void DecompressChunkCPU(long chunkID) {
			// Todo: Implement a CPU-based fallback decompression algorithm
			throw new NotImplementedException();
		}

		/**
		* Compresses a chunk and returns the compressed data.
		* Note: This should only be called by ChunkMemoryManager. If you need to compress a chunk, use MemoryManager.CompressChunk() instead.
		*/
		public async Task CompressChunk(long chunkID) {
			int bufferIdx = AcquireBufferSet();
			BufferSet buffers = _bufferPool[bufferIdx];
			try {
				// Get raw chunk data
				int[] rawData = MemoryManager.GetRawDataArray(chunkID);
				if(rawData is not { Length: 32 * 32 * 32 }) {
					throw new Exception($"Invalid chunk data for {chunkID}");
				}

				// Upload chunk data to GPU
				buffers.Input.SetData(rawData);
				// Clear output and metadata buffers
				const int maxOutputInts = 32 * 32 * 32 * 2; // Worst case: no compression
				int[] zeroOutput = new int[maxOutputInts];
				buffers.CompressionOutput.SetData(zeroOutput);
				uint[] zeroMeta = new uint[4];
				buffers.Metadata.SetData(zeroMeta);

				// Dispatch compression compute shader
				const int MAX_COMPRESS_RETRIES = 3;
				int attempt = 0;
				int totalCompressedBytes = 0;
				byte[] compressedData = null;
				bool valid = false;
				while(attempt < MAX_COMPRESS_RETRIES && !valid) {
					attempt++;
					int kernel = CompressionShader.FindKernel("CSMain");
					CompressionShader.SetBuffer(kernel, ChunkInput, buffers.Input);
					CompressionShader.SetBuffer(kernel, ChunkCompressedOutput, buffers.CompressionOutput);
					CompressionShader.SetBuffer(kernel, ChunkMetadata, buffers.Metadata);
					CompressionShader.Dispatch(kernel, 1, 1, 1);

					// Read back metadata
					AsyncGPUReadbackRequest metaRequest = AsyncGPUReadback.Request(buffers.Metadata);
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
					int compressedPayloadSize = meta[0];
					int offsetTableBytes = 32 * 32 * 4;
					// Validate payload size
					if(compressedPayloadSize < 0) {
						Debug.LogWarning($"[CompressChunk] Invalid compressedPayloadSize={compressedPayloadSize} on attempt {attempt}, retrying");
						if(attempt >= MAX_COMPRESS_RETRIES) throw new Exception($"Invalid compressed payload size from GPU: {compressedPayloadSize}");
						continue;
					}
					totalCompressedBytes = checked(compressedPayloadSize + offsetTableBytes);

					// Read back output buffer
					AsyncGPUReadbackRequest dataRequest = AsyncGPUReadback.Request(buffers.CompressionOutput);
					while(!dataRequest.done) {
						if(dataRequest.hasError) break;
						await Task.Yield();
					}
					if(dataRequest.hasError) {
						if(attempt >= MAX_COMPRESS_RETRIES) throw new Exception("GPU readback error (data)");
						continue;
					}
					int[] compressedInts = dataRequest.GetData<int>().ToArray();
					// Defensive: ensure we have enough bytes in the readback to cover totalCompressedBytes
					int availableBytes = compressedInts.Length * sizeof(int);
					if(totalCompressedBytes > availableBytes) {
						Debug.LogWarning($"[CompressChunk] Readback availableBytes={availableBytes} less than expected totalCompressedBytes={totalCompressedBytes} on attempt {attempt}");
						if(attempt >= MAX_COMPRESS_RETRIES) throw new Exception($"Readback returned fewer bytes ({availableBytes}) than expected ({totalCompressedBytes})");
						continue;
					}
					if(compressedInts.Length > (totalCompressedBytes + 3) / 4) Array.Resize(ref compressedInts, (totalCompressedBytes + 3) / 4);
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
				return;
			} finally {
				ReleaseBufferSet(bufferIdx);
			}
		}

		/**
		* Decompresses a chunk and returns the uncompressed data.
		* Note: This should only be called by ChunkMemoryManager. If you need to decompress a chunk, use MemoryManager.DecompressChunk() instead.
		*/
		public async Task DecompressChunk(long chunkID) {
			var sw = System.Diagnostics.Stopwatch.StartNew();
			int bufferIdx = AcquireBufferSet();
			var buffers = _bufferPool[bufferIdx];
			try {
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
				int[] compressedInts = new int[(compressedSize + 3) / 4];
				Buffer.BlockCopy(compressedData, 0, compressedInts, 0, compressedSize);
				buffers.Input.SetData(compressedInts);

				// Prepare output buffer (for decompressed ints)
				// (already allocated in buffer pool)
				// Prepare metadata buffer
				int offsetTableBytes = 32 * 32 * 4;
				if(compressedSize < offsetTableBytes) throw new Exception("Invalid compressedSize in pool");
				int payloadSize = (compressedSize - offsetTableBytes);
				uint[] meta = { (uint)payloadSize, (uint)originalSize };
				buffers.Metadata.SetData(meta);

				// Dispatch decompression compute shader
				ComputeShader decompressionShader = MemoryManager.decompressionShader;
				int kernel = decompressionShader.FindKernel("CSMain");
				decompressionShader.SetBuffer(kernel, CompressedInput, buffers.Input);
				decompressionShader.SetBuffer(kernel, ChunkOutput, buffers.DecompressionOutput);
				decompressionShader.SetBuffer(kernel, ChunkMetadata, buffers.Metadata);
				decompressionShader.Dispatch(kernel, 1, 1, 1);

				// Read back decompressed data with timeout
				AsyncGPUReadbackRequest readback = AsyncGPUReadback.Request(buffers.DecompressionOutput);
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

				int offsetTableBytesCheck = 32 * 32 * 4;
				if(compressedData.Length < offsetTableBytesCheck) {
					Debug.LogError($"[DecompressChunk] Compressed data is smaller than offset table: {compressedData.Length} < {offsetTableBytesCheck}");
					throw new Exception("Compressed data too small for offset table");
				}
				int columnCount = 32 * 32;
				payloadSize = compressedData.Length - offsetTableBytesCheck;
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
					if(i > 0 && offsets[i] < offsets[i - 1]) {
						// Fix minor non-monotonicity by forcing non-decreasing offsets; log for diagnosis
						Debug.LogWarning($"[DecompressChunk] Offset table not monotonic at i={i}: prev={offsets[i - 1]} cur={offsets[i]}, fixing by setting to prev");
						offsets[i] = offsets[i - 1];
					}
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

				sw.Stop();
				if(sw.ElapsedMilliseconds > 100) {
					Debug.LogWarning($"[DecompressChunk] chunkID={chunkID} took {sw.ElapsedMilliseconds} ms");
				}
			} finally {
				ReleaseBufferSet(bufferIdx);
			}
		}
	}
}

