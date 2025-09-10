using Unity.Collections;
using UnityEngine;

namespace Universe.Data.Chunk {
	public class ChunkAllocator {
		public static unsafe int* Allocate(int chunkSize) {
			return (int*) Unity.Collections.LowLevel.Unsafe.UnsafeUtility.Malloc(chunkSize * chunkSize * chunkSize * sizeof(int), 4, Allocator.Persistent);
		}
		
		public static unsafe void Free(int* chunkData) {
			Unity.Collections.LowLevel.Unsafe.UnsafeUtility.Free(chunkData, Allocator.Persistent);
		}
	}
}