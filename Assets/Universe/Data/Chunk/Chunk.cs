using System;
using Unity.Collections;
using Unity.Entities;
using UnityEngine;

namespace Universe.Data.Chunk {

	public struct Chunk {
		//How many blocks each chunk holds on each axis (32x32x32 = 32768 blocks)
		public static readonly int ChunkSize = 32;
		public IChunkData Data;
	}

	/**
	 * Chunk Data V8 is an unsafe implementation of chunk data storage that uses 4 bytes (32 bits) per block.
	 * This version is designed to be faster than previous versions.
	 * It mostly keeps the same structure as V7 but combines the activation bits with the data bits, and
	 * increases the size of the data bits by 1.
	 */
	public struct ChunkDataV8 : IComponentData, IChunkData {

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

		public long Index;
		public readonly unsafe int* Data; // Array to hold chunk data

		public unsafe ChunkDataV8(long index = 0) {
			Index = index;
			Data = ChunkAllocator.Allocate(Chunk.ChunkSize);
		}
		public unsafe ChunkDataV8(long index, int* data) {
			Index = index;
			Data = data;
		}

		public IChunkData MigrateVersion() {
			throw new System.NotImplementedException("V8 is the latest version, cannot migrate!");
		}

		public int[] GetRawDataArray() {
			int[] arr = new int[Chunk.ChunkSize * Chunk.ChunkSize * Chunk.ChunkSize];
			unsafe {
				for(int i = 0; i < arr.Length; i++) {
					arr[i] = Data[i];
				}
			}
			return arr;
		}

		public unsafe int* GetRawDataPointer() {
			return Data;
		}

		public void SetRawDataArray(int[] data) {
			throw new NotImplementedException("Direct array access not supported in unsafe chunk data, use SetRawDataPointer instead.");
		}

		public unsafe void SetRawDataPointer(int* data, int length) {
			for(var i = 0; i < length; i++) {
				Data[i] = data[i];
			}
		}

		public unsafe int GetRawData(long index) {
			return Data[index];
		}

		public unsafe void SetRawData(long index, int value) {
			Data[index] = value;
		}

		public unsafe short GetBlockType(long index) {
			return (short)(Data[index] & TypeMask);
		}

		public unsafe void SetBlockType(long index, short type) {
			Data[index] = (Data[index] & TypeMaskInverted) | (type & TypeMask);
		}

		public unsafe short GetBlockHP(long index) {
			return (short)((Data[index] >> HPBitsStart) & HPMask);
		}

		public unsafe void SetBlockHP(long index, short hp) {
			Data[index] = (Data[index] & HPMaskInverted) | ((hp & HPMask) << HPBitsStart);
		}

		public unsafe byte GetBlockOrientation(long index) {
			return (byte)((Data[index] >> OrientationBitsStart) & OrientationMask);
		}

		public unsafe void SetBlockOrientation(long index, byte orientation) {
			Data[index] = (Data[index] & OrientationMaskInverted) | ((orientation & OrientationMask) << OrientationBitsStart);
		}

		public unsafe int GetBlockData(long index) {
			return (Data[index] >> DataBitsStart) & DataMask;
		}

		public unsafe void SetBlockData(long index, int data) {
			Data[index] = (Data[index] & DataMaskInverted) | ((data & DataMask) << DataBitsStart);
		}
		public Vector3 GetBlockPosition(long index) {
			var size = Chunk.ChunkSize;
			var x = (int)(index % size);
			var y = (int)((index / size) % size);
			var z = (int)(index / (size * size));
			return new Vector3(x, y, z);
		}
		public long GetBlockIndex(Vector3 position) {
			var size = Chunk.ChunkSize;
			int x = (int)position.x;
			int y = (int)position.y;
			int z = (int)position.z;
			return (long)x + (long)y * size + (long)z * size * size;
		}

		public unsafe bool GetBlockActivation(long index) {
			if (IsActivatable()) {
				//Todo: Extract activation from data bits using an external block info lookup
			}
			return false;
		}

		public unsafe bool IsActivatable() {
			//Todo: Check if the block info for this type has activation
			return true;
		}
	}

	public interface IChunkData {

		IChunkData MigrateVersion();

		int[] GetRawDataArray();

		void SetRawDataArray(int[] data);

		int GetRawData(long index);

		void SetRawData(long index, int value);

		short GetBlockType(long index);

		void SetBlockType(long index, short type);

		short GetBlockHP(long index);

		void SetBlockHP(long index, short hp);

		byte GetBlockOrientation(long index);

		void SetBlockOrientation(long index, byte orientation);

		int GetBlockData(long index);

		void SetBlockData(long index, int data);

		Vector3 GetBlockPosition(long index);

		long GetBlockIndex(Vector3 position);
	}
}