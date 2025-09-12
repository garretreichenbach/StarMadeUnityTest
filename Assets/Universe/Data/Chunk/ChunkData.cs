using System;
using Unity.Collections;
using Unity.Entities;
using UnityEngine;

namespace Universe.Data.Chunk {

	public struct ChunkHeader {
		public long ChunkID;
		public int DataOffset; // Offset in global memory
		public int DataSize; // Size in bytes
		public ChunkState State; // Uncompressed/Compressed/GPU_Processing
		public float LastAccessTime;
		public byte CompressionLevel;
	}

	public struct ChunkAllocation {
		public int GlobalOffset;
		public int AllocatedSize;
		public bool IsCompressed;
		public int EntityID;
		public int ChunkIndex;
	}

	public enum ChunkState {
		Uncompressed,
		Compressed,
		GPUCompressing,
		GPUDecompressing,
		Unloaded,
	}

	/**
	 * Chunk Data V8 is an unsafe implementation of chunk data storage that uses 4 bytes (32 bits) per block.
	 * This version is designed to be faster than previous versions.
	 * It mostly keeps the same structure as V7 but combines the activation bits with the data bits, and
	 * increases the size of the data bits by 1.
	 */
	public struct ChunkData : IChunkData {
		public static readonly int TotalBits = 32; //4 Bytes per block

		public static readonly int TypeBits = 13; // Number of bits for block type (up to 8192 types)
		public static readonly int TypeBitsStart = 0; // Starting bit position for block type
		public static readonly int TypeMask = (1 << TypeBits) - 1; // Mask to extract block type
		public static readonly int TypeMaskInverted = ~TypeMask; // Inverted mask for block type

		public static readonly int HPBits = 7; // Number of bits for block HP (up to 128 HP)
		public static readonly int HPBitsStart = TypeBitsStart + TypeBits; // Starting bit position for block HP
		public static readonly int HPMask = (1 << HPBits) - 1; // Mask to extract block HP
		public static readonly int HPMaskInverted = ~HPMask; // Inverted mask for block HP

		public static readonly int OrientationBits = 5; // Number of bits for block orientation (up to 32 orientations)
		public static readonly int OrientationBitsStart = HPBitsStart + HPBits; // Starting bit position
		public static readonly int OrientationMask = (1 << OrientationBits) - 1; // Mask to extract block orientation
		public static readonly int OrientationMaskInverted = ~OrientationMask; // Inverted mask for block orientation

		public static readonly int DataBits = 7; // Number of bits for block-specific data (up to 128 values)
		public static readonly int DataBitsStart = OrientationBitsStart + OrientationBits; // Starting bit position
		public static readonly int DataMask = (1 << DataBits) - 1; // Mask
		public static readonly int DataMaskInverted = ~DataMask; // Inverted mask for block-specific data

		byte Version => 8; // Version of the chunk data structure

		public long _chunkID;
		public long _seed;

		public ChunkData(long chunkID = 0, long seed = 0) {
			_chunkID = chunkID;
			_seed = seed;
		}

		public IChunkData MigrateVersion() {
			throw new NotImplementedException("V8 is the latest version, cannot migrate!");
		}

		public int GetBlockData(int index) {
			return ChunkMemoryManager.Instance.GetRawData(_chunkID, index);
		}

		public void SetBlockData(int index, int value) {
			ChunkMemoryManager.Instance.SetRawData(_chunkID, index, value);
		}

		public short GetBlockType(int index) {
			return (short) (GetBlockData(index) & TypeMask);
		}

		public void SetBlockType(int index, short type) {
			SetBlockData(index, (GetBlockData(index) & TypeMaskInverted) | (type & TypeMask));
		}

		public short GetBlockHP(int index) {
			return (short)((GetBlockData(index) >> HPBitsStart) & HPMask);
		}

		public void SetBlockHP(int index, short hp) {
			SetBlockData(index, (GetBlockData(index) & HPMaskInverted) | ((hp & HPMask) << HPBitsStart));
		}

		public byte GetBlockOrientation(int index) {
			return (byte)(GetBlockData(index) >> OrientationBitsStart & OrientationMask);
		}

		public void SetBlockOrientation(int index, byte orientation) {
			SetBlockData(index, (GetBlockData(index) & OrientationMaskInverted) | ((orientation & OrientationMask) << OrientationBitsStart));
		}

		public Vector3 GetBlockPosition(int index) {
			var size = IChunkData.ChunkSize;
			var x = index % size;
			var y = (index / size) % size;
			var z = index / (size * size);
			return new Vector3(x, y, z);
		}
		public int GetBlockIndex(Vector3 position) {
			var size = IChunkData.ChunkSize;
			int x = (int)position.x;
			int y = (int)position.y;
			int z = (int)position.z;
			return x + y * size + z * size * size;
		}

		public bool GetBlockActivation(int index) {
			if (IsActivatable()) {
				//Todo: Extract activation from data bits using an external block info lookup
			}
			return false;
		}

		public bool IsActivatable() {
			//Todo: Check if the block info for this type has activation
			return true;
		}
		public static void Initialize(ChunkMemoryManager chunkMemoryManager) {
			throw new NotImplementedException();
		}
	}

	public interface IChunkData {
		public static readonly int ChunkSize = 32;

		IChunkData MigrateVersion();

		short GetBlockType(int index);

		void SetBlockType(int index, short type);

		short GetBlockHP(int index);

		void SetBlockHP(int index, short hp);

		byte GetBlockOrientation(int index);

		void SetBlockOrientation(int index, byte orientation);

		int GetBlockData(int index);

		void SetBlockData(int index, int data);

		Vector3 GetBlockPosition(int index);

		int GetBlockIndex(Vector3 position);
	}
}