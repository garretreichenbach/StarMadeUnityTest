package org.schema.game.common.data.world;

import org.schema.common.util.ByteUtil;
import org.schema.common.util.MemoryManager;
import org.schema.game.common.data.physics.octree.ArayOctreeTools;
import org.schema.game.common.data.physics.octree.ArrayOctree;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class SegmentDataSingle extends SegmentData {
	private int filledType;

	public SegmentDataSingle(int filledType) {
		this.filledType = filledType;
	}

	public SegmentDataSingle(boolean onClient) {
		super(onClient);
	}

	public SegmentDataSingle(boolean onClient, int filledType) {
		super(onClient);
		this.filledType = filledType;
		setOcree();
	}

	public void setOcree() {
		if(inOctree(filledType)) {
			if(onServer) {
				octree = ArayOctreeTools.fullTreeServer();
			} else {
				octree = ArayOctreeTools.fullTreeClient();
			}
		} else {
			if(onServer) {
				octree = ArayOctreeTools.emptyTreeServer();
			} else {
				octree = ArayOctreeTools.emptyTreeClient();
			}
		}
	}

	public SegmentDataSingle(SegmentData segmentData) throws SegmentDataWriteException {
		throw new SegmentDataWriteException(this);
	}

	@Override
	public void translateModBlocks() throws SegmentDataWriteException {
	}

	@Override
	public short getType(int index) {
		return (short) (SegmentData4Byte.typeMask & filledType);
	}

	@Override
	public short getHitpointsByte(int index) {
		return (short) ((SegmentData4Byte.hpMask & filledType) >> SegmentData4Byte.hpIndexStart);
	}

	@Override
	public boolean isActive(int index) {
		return (SegmentData4Byte.activationMask & filledType) > 0;
	}

	@Override
	public byte getOrientation(int index) {
		return (byte) ((SegmentData4Byte.orientationMask & filledType) >> SegmentData4Byte.orientationIndexStart);
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

	@Override
	protected ArrayOctree getOctreeInstance(boolean onServer) {
		return null;
	}

	@Override
	public boolean containsFast(int index) {
		return true;
	}

	@Override
	public void deserialize(DataInput inputStream, long time) throws IOException, SegmentDataWriteException {
		throw new SegmentDataWriteException(this);
	}

	@Override
	public void getBytes(int dataIndex, byte[] bytes) {
		assert (bytes.length >= 3);
		int value = filledType;
		bytes[0] = (byte) (value & 0xFF);
		bytes[1] = (byte) ((value >> 8) & 0xFF);
		bytes[2] = (byte) ((value >> 16) & 0xFF);
		bytes[3] = (byte) ((value >> 24) & 0xFF);
	}

	@Override
	public void serialize(DataOutput outputStream) throws IOException {
		for(int i = 0; i < TOTAL_SIZE; i++) {
			int value = filledType;
			outputStream.writeByte((byte) (value & 0xFF));
			outputStream.writeByte((byte) ((value >> 8) & 0xFF));
			outputStream.writeByte((byte) ((value >> 16) & 0xFF));
			outputStream.writeByte((byte) ((value >> 24) & 0xFF));
		}
	}

	@Override
	public int getDataAt(int infoIndex) {
		return filledType;
	}

	@Override
	public byte[] getAsByteBuffer(byte[] out) {
		for(int i = 0; i < BLOCK_COUNT; i++) {
			int dIndex = i * 4;
			int value = filledType;
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
	public SegmentDataType getDataType() {
		return SegmentDataType.SINGLE;
	}

	@Override
	public int serializeRemoteSegment(DataOutput stream) throws IOException {
		stream.writeInt(filledType);
		return ByteUtil.SIZEOF_INT;
	}

	@Override
	public void deserializeRemoteSegment(DataInput stream) throws IOException {
		filledType = stream.readInt();
		setOcree();
	}

	@Override
	public int[] getAsIntBuffer(int[] out) {
		Arrays.fill(out, filledType);
		return out;
	}

	@Override
	public MemoryManager.MemIntArray getAsIntBuffer(MemoryManager.MemIntArray out) {
		out.fill(filledType);
		return out;
	}

	@Override
	protected void copyTo(ByteBuffer buffer) {
		for(int i = 0; i < TOTAL_SIZE; i++) {
			buffer.putInt(filledType);
		}
	}
}
