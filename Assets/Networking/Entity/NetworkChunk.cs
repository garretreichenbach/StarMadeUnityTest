using System;

namespace Networking.Entity {

	[Serializable]
	public struct NetworkChunkData {
		public int chunkX, chunkY, chunkZ;
		public long seed; // -1 if modified
		public bool isModified;
		public byte[] voxelData; // Only used if isModified = true
	}
}