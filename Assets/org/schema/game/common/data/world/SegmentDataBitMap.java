package org.schema.game.common.data.world;

import org.schema.common.util.ByteUtil;
import org.schema.common.util.MemoryManager;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.octree.ArrayOctree;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class SegmentDataBitMap extends SegmentData {
	private int indexBitShift;
	private int indexBitShiftInv;
	private int indexBitMask;
	private int bitMask;
	private int[] bitMap;
	private int[] blockTypeData;
	private final int shift = 5;

	public SegmentDataBitMap(boolean onClient, int[] blockTypeData, SegmentData convert) {
		super(onClient);
		assert (blockTypeData.length <= 16) : blockTypeData.length;
		this.blockTypeData = blockTypeData;
		indexBitShift = shift;
		while(blockTypeData.length > (1 << (32 >> indexBitShift))) {
			indexBitShift--;
		}
		//TODO: optimization1: method to fetch multiple types (as many as possible per int). if done from lookup table we get up to 32 types/orientations/etc per int, which would save a lot of bit operations.
		//TODO: optimization2: possible count map if few enough types
		indexBitShiftInv = shift - indexBitShift;
		bitMask = (1 << (32 >> indexBitShift)) - 1;
		indexBitMask = (1 << indexBitShift) - 1;
		bitMap = new int[TOTAL_SIZE >> indexBitShift];
		convert(convert);
		setOctree();
	}

	protected void convert(SegmentData convert) {
		for(int i = 0; i < SegmentData.BLOCK_COUNT; i++) {
			int typeIndex = calcBitCode(convert, i);
			bitMap[i >> indexBitShift] |= typeIndex << ((i & indexBitMask) << indexBitShiftInv);
		}
	}

	private void setOctree() {
		octree = new ArrayOctree(onServer);
	}

	private int calcBitCode(SegmentData convert, int index) {
		int data = convert.getDataAt(index);
		for(int i = 0; i < blockTypeData.length; i++) {
			if(data == blockTypeData[i])
				return i;
		}
		assert (false) : "This block type not registered: " + convert.getSegment() + "; DATA " + data + " Type: " + (ElementKeyMap.toString(SegmentData4Byte.getTypeFromIntData(data))) +
				" Registered types: " + printBlocks(blockTypeData);
		return 0;
	}

	private String printBlocks(int[] blockIndexList) {
		StringBuilder s = new StringBuilder();
		for(int i = 0; i < blockTypeData.length; i++) {
			s.append(ElementKeyMap.toString(SegmentData4Byte.getTypeFromIntData(blockIndexList[i]))).append(", ");
		}
		return s.toString();
	}

	public SegmentDataBitMap(boolean onClient) {
		super(onClient);
	}

	public SegmentDataBitMap(SegmentData segmentData) throws SegmentDataWriteException {
		throw new SegmentDataWriteException(this);
	}

	@Override
	protected ArrayOctree getOctreeInstance(boolean onServer) {
		return new ArrayOctree(onServer);
	}

	@Override
	public boolean containsFast(int index) {
		return getType(index) != 0;
	}

	@Override
	public void deserialize(DataInput inputStream, long time) throws IOException, SegmentDataWriteException {
		throw new SegmentDataWriteException(this);
	}

	@Override
	public void getBytes(int dataIndex, byte[] bytes) {
		int value = getDataAt(dataIndex);
		bytes[0] = (byte) (value & 0xFF);
		bytes[1] = (byte) ((value >> 8) & 0xFF);
		bytes[2] = (byte) ((value >> 16) & 0xFF);
		bytes[3] = (byte) ((value >> 24) & 0xFF);
	}

	@Override
	public void serialize(DataOutput outputStream) throws IOException {
		for(int i = 0; i < TOTAL_SIZE; i++) {
			int value = getDataAt(i);
			outputStream.writeByte((byte) (value & 0xFF));
			outputStream.writeByte((byte) ((value >> 8) & 0xFF));
			outputStream.writeByte((byte) ((value >> 16) & 0xFF));
			outputStream.writeByte((byte) ((value >> 24) & 0xFF));
		}
	}

	@Override
	public int getDataAt(int index) {
		return blockTypeData[getBlockTypeIndex(index)];
	}

	@Override
	public byte[] getAsByteBuffer(byte[] out) {
		for(int i = 0; i < BLOCK_COUNT; i++) {
			int dIndex = i * 4;
			int value = getDataAt(i);
			out[dIndex] = (byte) (value);
			out[dIndex + 1] = (byte) (value >> 8);
			out[dIndex + 2] = (byte) (value >> 16);
			out[dIndex + 3] = (byte) (value >> 24);
		}
		return out;
	}

	@Override
	public int inflate(Inflater inflater, byte[] in)
			throws SegmentInflaterException, DataFormatException, SegmentDataWriteException {
		throw new SegmentDataWriteException(this);
	}

	@Override
	public void checkWritable() throws SegmentDataWriteException {
		throw new SegmentDataWriteException(this);
	}

	@Override
	public SegmentDataType getDataType() {
		return SegmentDataType.BITMAP;
	}

	@Override
	public int serializeRemoteSegment(DataOutput stream) throws IOException {
		stream.writeInt(indexBitShift);
		stream.writeInt(blockTypeData.length);
		for(int block : blockTypeData) {
			stream.writeInt(block);
		}
		for(int data : bitMap) {
			stream.writeInt(data);
		}
		int size = ByteUtil.SIZEOF_INT * (blockTypeData.length + bitMap.length + 2);
		assert (size < 32768) : "BlockTypeSize: " + blockTypeData.length + " Bitmap Size: " + bitMap.length + " Total Bytes: " + size;
		return size;
	}

	@Override
	public void deserializeRemoteSegment(DataInput stream) throws IOException {
		indexBitShift = stream.readInt();
		int blockTypeLength = stream.readInt();
		indexBitShiftInv = shift - indexBitShift;
		bitMask = (1 << (32 >> indexBitShift)) - 1;
		indexBitMask = (1 << indexBitShift) - 1;
		blockTypeData = new int[blockTypeLength];
		for(int i = 0; i < blockTypeData.length; i++) {
			blockTypeData[i] = stream.readInt();
		}
		bitMap = new int[TOTAL_SIZE >> indexBitShift];
		for(int i = 0; i < bitMap.length; i++) {
			bitMap[i] = stream.readInt();
		}
		setOctree();
	}

	@Override
	public int[] getAsIntBuffer(int[] out) {
		for(int i = 0; i < TOTAL_SIZE; i++) {
			out[i] = getDataAt(i);
		}
		return out;
	}

	@Override
	public MemoryManager.MemIntArray getAsIntBuffer(MemoryManager.MemIntArray out) {
		for(int i = 0; i < TOTAL_SIZE; i++) {
			out.put(i, getDataAt(i));
		}
		return out;
	}

	@Override
	protected void copyTo(ByteBuffer buffer) {
		for(int i = 0; i < TOTAL_SIZE; i++) {
			buffer.putInt(getDataAt(i));
		}
	}

	public int getBlockTypeIndex(int index) {
		return (bitMap[index >> indexBitShift] >> ((index & indexBitMask) << indexBitShiftInv)) & bitMask;
	}

	@Override
	public void translateModBlocks() throws SegmentDataWriteException {
	}

	@Override
	public short getType(int index) {
		return (short) (blockTypeData[getBlockTypeIndex(index)] & SegmentData4Byte.typeMask);
//		return (short) (blockTypeData[getBlockTypeIndex(index)] & typeMask); //Todo: This needs to be converted to the new system
	}

	@Override
	public short getHitpointsByte(int index) {
		return (short) ((blockTypeData[getBlockTypeIndex(index)] & SegmentData4Byte.hpMask) >> SegmentData4Byte.hpIndexStart);
//		return (short) ((blockTypeData[getBlockTypeIndex(index)] & hpMask) >> hitpointsIndexStart);
	}

	@Override
	public boolean isActive(int index) {
		return (blockTypeData[getBlockTypeIndex(index)] & SegmentData4Byte.activationMask) > 0;
//		return (blockTypeData[getBlockTypeIndex(index)] & activeMask) > 0;
	}

	@Override
	public byte getOrientation(int index) {
		return (byte) ((blockTypeData[getBlockTypeIndex(index)] & SegmentData4Byte.orientationMask) >> SegmentData4Byte.orientationIndexStart);
//		return (byte) ((blockTypeData[getBlockTypeIndex(index)] & orientMask) >> orientationStart);
	}

	@Override
	public void setExtra(int index, byte extra) throws SegmentDataWriteException {
	}

	@Override
	public int getExtra(int index) {
		return 0;
	}

	@Override
	public SegmentData doBitmapCompressionCheck(RemoteSegment seg) {
		return this;
	}

	@Override
	public void setDataAt(int i, int data) throws SegmentDataWriteException {
		throw new SegmentDataWriteException(this);
	}
}
