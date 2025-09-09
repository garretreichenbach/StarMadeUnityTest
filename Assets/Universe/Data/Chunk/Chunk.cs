using System;
using Unity.Entities;
using UnityEngine;
using static Universe.Data.Chunk.Chunk;

namespace Universe.Data.Chunk {
	
	public class Chunk : MonoBehaviour {
		
		public static readonly int ChunkSize = 32;

		public IChunkData Data;

		public Chunk(IChunkData data) {
			Data = data;
		}
	}

	public class ChunkBuffer : MonoBehaviour {
		
		private Chunk[] _chunkData;
		
		public ChunkBuffer Create(int chunksTotal) {
			_chunkData = new Chunk[chunksTotal];
			for(var i = 0; i < chunksTotal; i++) {
				_chunkData[i] = gameObject.AddComponent<Chunk>();
				_chunkData[i].Data = new ChunkDataV8(i);
			}
			return this;
		}

		public Chunk GetChunkData(int index) {
			if(index < 0 || index >= _chunkData.Length) {
				throw new IndexOutOfRangeException("Chunk index out of range: " + index);
			}
			return _chunkData[index];
		}
		
		public void SetChunkData(int index, Chunk data) {
			if(index < 0 || index >= _chunkData.Length) {
				throw new IndexOutOfRangeException("Chunk index out of range: " + index);
			}
			_chunkData[index] = data;
		}

		public int GetTotalChunks() {
			return _chunkData.Length;
		}

		public Chunk[] GetAllChunkData() {
			return _chunkData;
		}
	}

	public class ChunkBaker : Baker<Chunk> {
		public override void Bake(Chunk chunk) {
			var entity = GetEntity(TransformUsageFlags.Dynamic);
			switch(chunk.Data) {
				case ChunkDataV8 v8:
					AddComponent(entity, v8);
					break;
				default:
					throw new NotImplementedException("Unsupported chunk data version!");
			}
			DependsOn(chunk);
		}
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
		
		public static readonly int HPBits = 7;    // Number of bits for block HP (up to 128 HP)
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
		
		private byte Version => 8; // Version of the chunk data structure
		public long Index;
		private unsafe int* _data; // Array to hold chunk data
		
		public unsafe ChunkDataV8(long index = 0) {
			Index = index;
			_data = ChunkAllocator.NormalAllocator.Allocate(ChunkSize);
		}

		public IChunkData MigrateVersion() {
			throw new System.NotImplementedException("V8 is the latest version, cannot migrate!");
		}
		
		public int[] GetRawDataArray() {
			throw new NotImplementedException("Direct array access not supported in unsafe chunk data, use GetRawDataPointer instead.");
		}
		
		public unsafe int* GetRawDataPointer() {
			return _data;
		}
		
		public void SetRawDataArray(int[] data) {
			throw new NotImplementedException("Direct array access not supported in unsafe chunk data, use SetRawDataPointer instead.");
		}
		
		public unsafe void SetRawDataPointer(int* data, int length) {
			for(var i = 0; i < length; i++) {
				_data[i] = data[i];
			}
		}
		
		public unsafe int GetRawData(long index) {
			return _data[index];
		}
		
		public unsafe void SetRawData(long index, int value) {
			_data[index] = value;
		}

		public unsafe short GetBlockType(long index) {
			return (short)(_data[index] & TypeMask);
		}
		
		public unsafe void SetBlockType(long index, short type) {
			_data[index] = (_data[index] & TypeMaskInverted) | (type & TypeMask);
		}
		
		public unsafe short GetBlockHP(long index) {
			return (short)((_data[index] >> HPBitsStart) & HPMask);
		}
		
		public unsafe void SetBlockHP(long index, short hp) {
			_data[index] = (_data[index] & HPMaskInverted) | ((hp & HPMask) << HPBitsStart);
		}
		
		public unsafe byte GetBlockOrientation(long index) {
			return (byte)((_data[index] >> OrientationBitsStart) & OrientationMask);
		}
		
		public unsafe void SetBlockOrientation(long index, byte orientation) {
			_data[index] = (_data[index] & OrientationMaskInverted) | ((orientation & OrientationMask) << OrientationBitsStart);
		}
		
		public unsafe int GetBlockData(long index) {
			return (_data[index] >> DataBitsStart) & DataMask;
		}
		
		public unsafe void SetBlockData(long index, int data) {
			_data[index] = (_data[index] & DataMaskInverted) | ((data & DataMask) << DataBitsStart);
		}
		
		public unsafe bool GetBlockActivation(long index) {
			if(IsActivatable()) {
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
		
		Vector3 GetBlockPosition(long index) => new(index % ChunkSize, index / ChunkSize % ChunkSize, index / (ChunkSize * ChunkSize));
		
		long GetBlockIndex(Vector3 position) => (long)(position.x + position.y * ChunkSize + position.z * ChunkSize * ChunkSize);
	}
}