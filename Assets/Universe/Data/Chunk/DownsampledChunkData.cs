using System;
using UnityEngine;

namespace Universe.Data.Chunk {
	public struct DownsampledChunkData : IChunkData {
		int[] _blocks;
		int _size;

		public DownsampledChunkData(int[] blocks, int size) {
			_blocks = blocks;
			_size = size;
		}

		public IChunkData MigrateVersion() { throw new NotImplementedException(); }
		public int[] GetRawDataArray() { return _blocks; }
		public void SetRawDataArray(int[] data) { _blocks = data; }
		public int GetRawData(int index) { return _blocks[index]; }
		public void SetRawData(int index, int value) { _blocks[index] = value; }
		public short GetBlockType(int index) { return (short)_blocks[index]; } // Assuming block type is directly stored
		public void SetBlockType(int index, short type) { _blocks[index] = type; }
		public short GetBlockHP(int index) { return 0; } // Not implemented for downsampled
		public void SetBlockHP(int index, short hp) { } // Not implemented for downsampled
		public byte GetBlockOrientation(int index) { return 0; } // Not implemented for downsampled
		public void SetBlockOrientation(int index, byte orientation) { } // Not implemented for downsampled
		public int GetBlockData(int index) { return 0; } // Not implemented for downsampled
		public void SetBlockData(int index, int data) { } // Not implemented for downsampled

		public Vector3 GetBlockPosition(int index) {
			var x = (int)(index % _size);
			var y = (int)((index / _size) % _size);
			var z = (int)(index / (_size * _size));
			return new Vector3(x, y, z);
		}

		public int GetBlockIndex(Vector3 position) {
			int x = (int)position.x;
			int y = (int)position.y;
			int z = (int)position.z;
			return x + y * _size + z * _size * _size;
		}

		public int GetSize() { return _size; } // New method
	}
}