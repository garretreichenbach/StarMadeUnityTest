using Unity.Collections;
using UnityEngine;

namespace Universe.Data.Chunk {
	public class ChunkAllocator : MonoBehaviour {
		
		public static int HighPriority = 1;
		public static int NormalPriority = 0;
		public static int LowPriority = -1;
		
		public static ChunkAllocator LowAllocator = new(LowPriority);
		public static ChunkAllocator NormalAllocator = new(NormalPriority);
		public static ChunkAllocator HighAllocator = new(HighPriority);
		
		private int _priority;
		
		private ChunkAllocator(int priority) {
			_priority = priority;
		}

		public unsafe int* Allocate(int chunkSize) {
			return (int*) Unity.Collections.LowLevel.Unsafe.UnsafeUtility.Malloc(chunkSize * chunkSize * chunkSize * sizeof(int), 4, Allocator.Persistent);
		}
		
		public unsafe void Free(int* chunkData) {
			Unity.Collections.LowLevel.Unsafe.UnsafeUtility.Free(chunkData, Allocator.Persistent);
		}
	}
}