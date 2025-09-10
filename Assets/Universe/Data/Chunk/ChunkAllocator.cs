using Unity.Collections;
using UnityEngine;

namespace Universe.Data.Chunk {
	public abstract class ChunkAllocator {
		public static long TotalAllocatedMemory = 0;

		public static unsafe int* Allocate(int chunkSize) {
			long size = (long)chunkSize * chunkSize * chunkSize * sizeof(int);
			TotalAllocatedMemory += size;
			return (int*) Unity.Collections.LowLevel.Unsafe.UnsafeUtility.Malloc(size, 4, Allocator.Persistent);
		}
		
		public static unsafe void Free(int* chunkData, int chunkSize) {
			long size = (long)chunkSize * chunkSize * chunkSize * sizeof(int);
			TotalAllocatedMemory -= size;
			Unity.Collections.LowLevel.Unsafe.UnsafeUtility.Free(chunkData, Allocator.Persistent);
		}
	}
}