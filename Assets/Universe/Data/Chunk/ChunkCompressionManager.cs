using System;
using System.Buffers;
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
		static readonly int ChunkBaseBytesID = Shader.PropertyToID("ChunkBaseBytes");
		static readonly int GlobalCompressedOutputID = Shader.PropertyToID("GlobalCompressedOutput");

		// Reference to the memory manager and its GPU resources
		ChunkMemoryManager MemoryManager { get => ChunkMemoryManager.Instance; }

		ComputeShader CompressionShader { get => MemoryManager.compressionShader; }
		ComputeBuffer GpuInputBuffer { get => MemoryManager.GPUInputBuffer; }
		ComputeBuffer GpuOutputBuffer { get => MemoryManager.GPUOutputBuffer; }
		ComputeBuffer GpuMetadataBuffer { get => MemoryManager.GPUMetadataBuffer; }

		public static ChunkCompressionManager Instance => FindFirstObjectByType<ChunkCompressionManager>();

		// Simple reusable compute buffer pool to reduce per-batch allocations
		readonly object _bufferPoolLock = new object();
		System.Collections.Generic.List<ComputeBuffer> _bufferPool = new System.Collections.Generic.List<ComputeBuffer>();

		ComputeBuffer RentComputeBuffer(int count, int stride, ComputeBufferType type = ComputeBufferType.Default) {
			lock(_bufferPoolLock) {
				for(int i = 0; i < _bufferPool.Count; ++i) {
					var b = _bufferPool[i];
					if(b != null && b.count == count && b.stride == stride) {
						_bufferPool.RemoveAt(i);
						return b;
					}
				}
			}
			// Not found -> allocate
			return new ComputeBuffer(count, stride, type);
		}

		void ReturnComputeBuffer(ComputeBuffer buf) {
			if(buf == null) return;
			lock(_bufferPoolLock) {
				_bufferPool.Add(buf);
			}
		}

		void OnDestroy() {
			// Dispose pooled buffers
			lock(_bufferPoolLock) {
				foreach(var b in _bufferPool) b.Dispose();
				_bufferPool.Clear();
			}
		}

		/**
		* Compress a single chunk (wrapper around CompressChunk internal flow).
		*/
		public async Task<ChunkMemoryManager.CompressedChunk> CompressChunk(long chunkID) {
			// Use two-pass shader: CSComputeSizes -> CSWritePayloads
			int intsPerChunk = 32 * 32 * 32;
			int[] rawData = MemoryManager.GetRawDataArray(chunkID);
			if(rawData == null || rawData.Length != intsPerChunk) throw new Exception($"Invalid chunk data for {chunkID}");

			// Upload input (single chunk occupies first slot)
			GpuInputBuffer.SetData(rawData, 0, 0, intsPerChunk);

			// Zero metadata before sizes pass
			int[] zeroMeta = new int[2];
			GpuMetadataBuffer.SetData(zeroMeta);

			// Dispatch sizes kernel
			int kernelSizes;
			try {
				kernelSizes = CompressionShader.FindKernel("CSComputeSizes");
			} catch(Exception ex) {
				throw new Exception($"Compression shader missing kernel 'CSComputeSizes'. Ensure the assigned compute shader is the compressor (ChunkCompressor.compute). Shader: {CompressionShader?.name}", ex);
			}
			CompressionShader.SetBuffer(kernelSizes, ChunkInput, GpuInputBuffer);
			CompressionShader.SetBuffer(kernelSizes, ChunkMetadata, GpuMetadataBuffer);
			CompressionShader.Dispatch(kernelSizes, 1, 1, 1);

			// Read back metadata (payload size, original size)
			var metaTcs = new TaskCompletionSource<int[]>();
			AsyncGPUReadback.Request(GpuMetadataBuffer,
				req => {
					if(req.hasError) metaTcs.TrySetException(new Exception("GPU metadata readback error"));
					else metaTcs.TrySetResult(req.GetData<int>().ToArray());
				});
			int[] meta = await metaTcs.Task;
			if(meta == null || meta.Length < 1) throw new Exception("GPU metadata readback returned no data");
			int payloadSize = meta[0];
			int offsetTableBytes = 32 * 32 * 4;
			if(payloadSize < 0) throw new Exception($"Invalid compressed payload size from GPU: {payloadSize}");
			int totalCompressedBytes = checked(payloadSize + offsetTableBytes);

			// Compute chunk base (single chunk -> base = 0) and upload ChunkBaseBytes buffer
			ComputeBuffer chunkBaseBuf = new ComputeBuffer(1, sizeof(uint));
			try {
				uint[] bases = new uint[] { 0 };
				chunkBaseBuf.SetData(bases);

				// Ensure output buffer is zeroed for safety (only needed region)
				int intsNeeded = (totalCompressedBytes + 3) / 4;
				int[] zeroOut = ArrayPool<int>.Shared.Rent(intsNeeded);
				try {
					Array.Clear(zeroOut, 0, intsNeeded);
					GpuOutputBuffer.SetData(zeroOut, 0, 0, intsNeeded);
				} finally {
					ArrayPool<int>.Shared.Return(zeroOut);
				}

				// Dispatch write kernel
				int kernelWrite;
				try {
					kernelWrite = CompressionShader.FindKernel("CSWritePayloads");
				} catch(Exception ex) {
					throw new Exception($"Compression shader missing kernel 'CSWritePayloads'. Ensure the assigned compute shader is the compressor (ChunkCompressor.compute). Shader: {CompressionShader?.name}", ex);
				}
				CompressionShader.SetBuffer(kernelWrite, ChunkInput, GpuInputBuffer);
				CompressionShader.SetBuffer(kernelWrite, ChunkBaseBytesID, chunkBaseBuf);
				// Global output is the same GPUOutputBuffer (ByteAddressBuffer)
				CompressionShader.SetBuffer(kernelWrite, GlobalCompressedOutputID, GpuOutputBuffer);
				CompressionShader.SetBuffer(kernelWrite, ChunkMetadata, GpuMetadataBuffer);
				CompressionShader.Dispatch(kernelWrite, 1, 1, 1);

				// Read back the compacted output buffer
				var dataTcs = new TaskCompletionSource<int[]>();
				AsyncGPUReadback.Request(GpuOutputBuffer,
					req => {
						if(req.hasError) dataTcs.TrySetException(new Exception("GPU data readback error"));
						else dataTcs.TrySetResult(req.GetData<int>().ToArray());
					});

				int[] compressedInts = await dataTcs.Task;
				if(compressedInts == null) throw new Exception("GPU output readback returned no data");

				int availableBytes = compressedInts.Length * sizeof(int);
				if(totalCompressedBytes > availableBytes) throw new Exception($"Readback returned fewer bytes ({availableBytes}) than expected ({totalCompressedBytes})");

				// Copy into rented byte buffer, then into compressed pool
				var bytePool = ArrayPool<byte>.Shared;
				byte[] compressedData = bytePool.Rent(totalCompressedBytes);
				try {
					Buffer.BlockCopy(compressedInts, 0, compressedData, 0, totalCompressedBytes);
				} catch {
					bytePool.Return(compressedData);
					throw;
				}

				// Reserve space in compressed pool atomically and copy to NativeArray
				int reservedOffset = Interlocked.Add(ref MemoryManager._compressedPoolHead, totalCompressedBytes) - totalCompressedBytes;
				if(reservedOffset < 0) {
					Interlocked.Exchange(ref MemoryManager._compressedPoolHead, 0);
					bytePool.Return(compressedData);
					throw new Exception("Compressed pool negative head after reservation");
				}
				if(reservedOffset + totalCompressedBytes > MemoryManager._compressedPool.Length) {
					Interlocked.Add(ref MemoryManager._compressedPoolHead, -totalCompressedBytes);
					bytePool.Return(compressedData);
					throw new Exception("Compressed pool out of memory");
				}
				try {
					for(int i = 0; i < totalCompressedBytes; ++i) MemoryManager._compressedPool[reservedOffset + i] = compressedData[i];
				} catch(Exception ex) {
					Interlocked.Add(ref MemoryManager._compressedPoolHead, -totalCompressedBytes);
					ArrayPool<byte>.Shared.Return(compressedData);
					throw new Exception($"Failed copying compressed data into pool: {ex.Message}");
				} finally {
					ArrayPool<byte>.Shared.Return(compressedData);
				}

				// Update allocation and header (same as previous behavior)
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
			} finally {
				chunkBaseBuf.Dispose();
			}
		}

		/**
		* Compress multiple chunks as a single GPU batch. The compressed blobs are enqueued
		* back to the ChunkMemoryManager for throttled, main-thread commit. This method
		* returns when the GPU readbacks have completed and the compressed blobs have been
		* enqueued (not when they are committed into the compressed pool).
		*/
		public async Task CompressChunksAsync(long[] chunkIDs) {
			if(chunkIDs == null || chunkIDs.Length == 0) return;
			int batchLen = chunkIDs.Length;
			int maxPerBatch = MemoryManager.CompressionBatchSize;
			if(batchLen > maxPerBatch) throw new ArgumentException($"Batch length {batchLen} exceeds configured batch size {maxPerBatch}");

			int intsPerChunk = 32 * 32 * 32;
			int totalInts = intsPerChunk * batchLen;
			int[] upload = ArrayPool<int>.Shared.Rent(totalInts);
			try {
				for(int i = 0; i < batchLen; ++i) {
					long id = chunkIDs[i];
					int[] raw = MemoryManager.GetRawDataArray(id);
					if(raw == null || raw.Length != intsPerChunk) {
						try {
							MemoryManager.CompleteCompressionOperation(id, false, new Exception("Invalid chunk data for batch compression"));
						} catch { }
						Array.Clear(upload, i * intsPerChunk, intsPerChunk);
						continue;
					}
					Buffer.BlockCopy(raw, 0, upload, i * intsPerChunk * sizeof(int), intsPerChunk * sizeof(int));
				}

				// Upload input to GPU (batched)
				GpuInputBuffer.SetData(upload, 0, 0, totalInts);

				// Zero metadata before sizes pass
				int[] zeroMeta = ArrayPool<int>.Shared.Rent(batchLen * 2);
				try {
					Array.Clear(zeroMeta, 0, batchLen * 2);
					GpuMetadataBuffer.SetData(zeroMeta, 0, 0, batchLen * 2);
				} finally {
					ArrayPool<int>.Shared.Return(zeroMeta);
				}

				// Dispatch sizes kernel for the batch
				int kernelSizes;
				try {
					kernelSizes = CompressionShader.FindKernel("CSComputeSizes");
				} catch(Exception ex) {
					throw new Exception($"Compression shader missing kernel 'CSComputeSizes' (batch). Ensure the assigned compute shader is the compressor (ChunkCompressor.compute). Shader: {CompressionShader?.name}", ex);
				}
				CompressionShader.SetBuffer(kernelSizes, ChunkInput, GpuInputBuffer);
				CompressionShader.SetBuffer(kernelSizes, ChunkMetadata, GpuMetadataBuffer);
				CompressionShader.Dispatch(kernelSizes, batchLen, 1, 1);

				// Read back metadata (per-chunk payload sizes)
				var metaTcs = new TaskCompletionSource<int[]>();
				AsyncGPUReadback.Request(GpuMetadataBuffer,
					req => {
						if(req.hasError) metaTcs.TrySetException(new Exception("GPU metadata readback error (batch)"));
						else metaTcs.TrySetResult(req.GetData<int>().ToArray());
					});

				int[] meta = await metaTcs.Task;
				if(meta == null) throw new Exception("GPU metadata readback returned no data (batch)");

				// Compute per-chunk base offsets (in bytes) and total size
				int offsetTableBytes = 32 * 32 * 4;
				uint[] bases = new uint[batchLen];
				intCursor:
				{ }
				int totalBytes = 0;
				for(int i = 0; i < batchLen; ++i) {
					int payload = 0;
					int metaIndex = i * 2;
					if(metaIndex < meta.Length) payload = meta[metaIndex];
					int chunkBytes = checked(payload + offsetTableBytes);
					bases[i] = (uint)totalBytes;
					totalBytes += chunkBytes;
				}

				// Upload chunk base buffer
				ComputeBuffer chunkBaseBuf = new ComputeBuffer(batchLen, sizeof(uint));
				try {
					chunkBaseBuf.SetData(bases);

					// Ensure output buffer region has zeros for safety (only needed region)
					int intsNeeded = (totalBytes + 3) / 4;
					int[] zeroOut = ArrayPool<int>.Shared.Rent(intsNeeded);
					try {
						Array.Clear(zeroOut, 0, intsNeeded);
						GpuOutputBuffer.SetData(zeroOut, 0, 0, intsNeeded);
					} finally {
						ArrayPool<int>.Shared.Return(zeroOut);
					}

					// Dispatch write kernel to compact outputs into GpuOutputBuffer at bases
					int kernelWrite;
					try {
						kernelWrite = CompressionShader.FindKernel("CSWritePayloads");
					} catch(Exception ex) {
						throw new Exception($"Compression shader missing kernel 'CSWritePayloads' (batch). Ensure the assigned compute shader is the compressor (ChunkCompressor.compute). Shader: {CompressionShader?.name}", ex);
					}
					CompressionShader.SetBuffer(kernelWrite, ChunkInput, GpuInputBuffer);
					CompressionShader.SetBuffer(kernelWrite, ChunkBaseBytesID, chunkBaseBuf);
					CompressionShader.SetBuffer(kernelWrite, ChunkMetadata, GpuMetadataBuffer);
					CompressionShader.SetBuffer(kernelWrite, GlobalCompressedOutputID, GpuOutputBuffer);
					CompressionShader.Dispatch(kernelWrite, batchLen, 1, 1);

					// Read back the compacted output buffer
					var dataTcs = new TaskCompletionSource<int[]>();
					AsyncGPUReadback.Request(GpuOutputBuffer,
						req => {
							if(req.hasError) dataTcs.TrySetException(new Exception("GPU data readback error (batch)"));
							else dataTcs.TrySetResult(req.GetData<int>().ToArray());
						});

					int[] compressedInts = await dataTcs.Task;
					if(compressedInts == null) throw new Exception("GPU output readback returned no data (batch)");

					// Slice per-chunk and enqueue compressed writes
					int intCursor = 0; // pointer into compressedInts (in ints)
					var bytePool = ArrayPool<byte>.Shared;
					for(int i = 0; i < batchLen; ++i) {
						long id = chunkIDs[i];
						int payload = 0;
						int metaIndex = i * 2;
						if(metaIndex < meta.Length) payload = meta[metaIndex];
						int totalB = checked(payload + offsetTableBytes);
						int intsNeededChunk = (totalB + 3) / 4;
						if(intCursor + intsNeededChunk > compressedInts.Length) {
							try {
								MemoryManager.CompleteCompressionOperation(id, false, new Exception("Compressed readback shorter than expected (batch)"));
							} catch { }
							break;
						}
						byte[] rented = bytePool.Rent(totalB);
						try {
							Buffer.BlockCopy(compressedInts, intCursor * sizeof(int), rented, 0, totalB);
							MemoryManager.EnqueueCompressedWrite(id, rented, true);
						} catch(Exception ex) {
							bytePool.Return(rented);
							try {
								MemoryManager.CompleteCompressionOperation(id, false, ex);
							} catch { }
						}
						intCursor += intsNeededChunk;
					}
				} finally {
					chunkBaseBuf.Dispose();
				}
			} finally {
				ArrayPool<int>.Shared.Return(upload);
			}
		}

		/**
		* Decompress a chunk (single chunk path). Validates and writes back to uncompressed pool.
		*/
		public Task<ChunkMemoryManager.ChunkHeader> DecompressChunk(long chunkID, int poolIndex) {
			// validate allocation & header
			if(!MemoryManager._allocations.TryGetValue(chunkID, out ChunkMemoryManager.ChunkAllocation alloc)) throw new Exception($"Chunk allocation not found for {chunkID}");
			if(!MemoryManager._headers.TryGetValue(chunkID, out ChunkMemoryManager.ChunkHeader header)) throw new Exception($"Chunk header not found for {chunkID}");
			if(alloc.State != ChunkMemoryManager.ChunkState.Compressed) throw new Exception($"Chunk {chunkID} is not in compressed state");

			int compressedOffset = alloc.CompressedOffset;
			int compressedSize = alloc.CompressedSize;
			int originalSize = header.OriginalSize;

			if(compressedSize <= 0) throw new Exception("Invalid compressedSize in allocation");

			// copy compressed bytes into rented buffer to avoid allocating
			var compressedPool = ArrayPool<byte>.Shared;
			byte[] compressedData = compressedPool.Rent(compressedSize);
			for(int i = 0; i < compressedSize; ++i) compressedData[i] = MemoryManager._compressedPool[compressedOffset + i];

			// Create int[] buffer rented from pool for upload
			var intPool = ArrayPool<int>.Shared;
			int[] compressedInts = intPool.Rent((compressedSize + 3) / 4);
			Buffer.BlockCopy(compressedData, 0, compressedInts, 0, compressedSize);
			ComputeBuffer gpuCompressedBuffer = new ComputeBuffer((compressedSize + 3) / 4, sizeof(int), ComputeBufferType.Raw);
			gpuCompressedBuffer.SetData(compressedInts);

			ComputeBuffer gpuOutputBuffer = new ComputeBuffer(32 * 32 * 32, sizeof(int));
			ComputeBuffer gpuMetadataBuffer = new ComputeBuffer(2, sizeof(uint));
			int offsetTableBytes = 32 * 32 * 4;
			if(compressedSize < offsetTableBytes) {
				// return reserved slot and throw
				MemoryManager._freeUncompressedSlots.Enqueue(poolIndex);
				gpuCompressedBuffer.Dispose();
				gpuOutputBuffer.Dispose();
				gpuMetadataBuffer.Dispose();
				intPool.Return(compressedInts);
				compressedPool.Return(compressedData);
				throw new Exception("Invalid compressedSize in pool");
			}
			int payloadSize = compressedSize - offsetTableBytes;
			uint[] meta = { (uint)payloadSize, (uint)originalSize };
			gpuMetadataBuffer.SetData(meta);

			ComputeShader decompressionShader = MemoryManager.decompressionShader;
			int kernel;
			try {
				kernel = decompressionShader.FindKernel("CSMain");
			} catch(Exception ex) {
				throw new Exception($"Decompression shader missing kernel 'CSMain'. Ensure the assigned compute shader is the decompressor (ChunkDecompressor.compute). Shader: {decompressionShader?.name}", ex);
			}
			decompressionShader.SetBuffer(kernel, CompressedInput, gpuCompressedBuffer);
			decompressionShader.SetBuffer(kernel, ChunkOutput, gpuOutputBuffer);
			decompressionShader.SetBuffer(kernel, ChunkMetadata, gpuMetadataBuffer);
			decompressionShader.Dispatch(kernel, 1, 1, 1);

			// Async readback using callback: capture data into a rented int[] and enqueue for main-thread commit.
			AsyncGPUReadback.Request(gpuOutputBuffer,
				req => {
					if(req.hasError) {
						// Return rented resources and reserved slot and notify failure
						MemoryManager._freeUncompressedSlots.Enqueue(poolIndex);
						intPool.Return(compressedInts);
						compressedPool.Return(compressedData);
						try {
							MemoryManager.CompleteCompressionOperation(chunkID, false, new Exception("GPU readback error (decompression)"));
						} catch { }
					} else {
						try {
							var native = req.GetData<int>();
							int len = native.Length;
							int[] rentedInts = ArrayPool<int>.Shared.Rent(len);
							native.CopyTo(rentedInts);
							if(len != 32 * 32 * 32) {
								ArrayPool<int>.Shared.Return(rentedInts);
								MemoryManager._freeUncompressedSlots.Enqueue(poolIndex);
								try {
									MemoryManager.CompleteCompressionOperation(chunkID, false, new Exception($"Decompressed data size mismatch: {len}"));
								} catch { }
							} else {
								// Enqueue for main-thread commit (throttled)
								MemoryManager.EnqueueDecompressedWrite(chunkID, poolIndex, rentedInts, true);
							}
						} catch(Exception ex) {
							MemoryManager._freeUncompressedSlots.Enqueue(poolIndex);
							intPool.Return(compressedInts);
							compressedPool.Return(compressedData);
							try {
								MemoryManager.CompleteCompressionOperation(chunkID, false, ex);
							} catch { }
						}
					}
					// Dispose GPU buffers used for decompression
					gpuOutputBuffer.Dispose();
					gpuCompressedBuffer.Dispose();
					gpuMetadataBuffer.Dispose();
					// Return temporary buffers used for upload
					intPool.Return(compressedInts);
					compressedPool.Return(compressedData);
				});

			// Return header immediately; commit happens later.
			return Task.FromResult(header);
		}

		System.Collections.IEnumerator Start() {
			// Wait briefly for ChunkMemoryManager to initialize (avoid race where MemoryManager.Instance is null)
			float start = Time.realtimeSinceStartup;
			while(MemoryManager == null && Time.realtimeSinceStartup - start < 5f) {
				yield return null;
			}
			var mm = MemoryManager;
			if(mm == null) {
				Debug.LogError("ChunkCompressionManager: MemoryManager instance not found after wait");
				yield break;
			}
			// If shaders aren't assigned in inspector, try to auto-load from Resources/Shader/Compute
			if(mm.compressionShader == null) {
				var tryComp = Resources.Load<ComputeShader>("Shader/Compute/ChunkCompressor");
				if(tryComp != null) {
					mm.compressionShader = tryComp;
					Debug.Log("ChunkCompressionManager: auto-assigned compression shader from Resources/Shader/Compute/ChunkCompressor");
				} else {
					Debug.LogWarning("ChunkCompressionManager: compressionShader not assigned on MemoryManager and auto-load failed");
				}
			}
			if(mm.decompressionShader == null) {
				var tryDecomp = Resources.Load<ComputeShader>("Shader/Compute/ChunkDecompressor");
				if(tryDecomp != null) {
					mm.decompressionShader = tryDecomp;
					Debug.Log("ChunkCompressionManager: auto-assigned decompression shader from Resources/Shader/Compute/ChunkDecompressor");
				} else {
					Debug.LogWarning("ChunkCompressionManager: decompressionShader not assigned on MemoryManager and auto-load failed");
				}
			}

			// Validate compute shader kernel presence early to give clearer diagnostics
			if(CompressionShader == null) {
				Debug.LogError("ChunkCompressionManager: compressionShader not assigned on GameObject");
			} else {
				try {
					CompressionShader.FindKernel("CSComputeSizes");
					CompressionShader.FindKernel("CSWritePayloads");
				} catch(Exception ex) {
					Debug.LogError($"ChunkCompressionManager: Compression shader missing expected kernels (CSComputeSizes/CSWritePayloads): {CompressionShader?.name}. Error: {ex.Message}");
				}
			}

			if(mm.decompressionShader == null) {
				Debug.LogError("ChunkCompressionManager: decompressionShader not assigned on MemoryManager");
			} else {
				try {
					mm.decompressionShader.FindKernel("CSMain");
				} catch(Exception ex) {
					Debug.LogError($"ChunkCompressionManager: Decompression shader missing expected kernel 'CSMain': {mm.decompressionShader?.name}. Error: {ex.Message}");
				}
			}
		}
	}
}