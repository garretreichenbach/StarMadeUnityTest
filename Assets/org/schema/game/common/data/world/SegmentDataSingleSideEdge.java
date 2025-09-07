package org.schema.game.common.data.world;

import org.schema.common.util.ByteUtil;
import org.schema.common.util.MemoryManager;
import org.schema.game.common.data.IcosahedronHelper;
import org.schema.game.common.data.physics.octree.ArrayOctree;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Created by Jordan on 07/03/2017.
 */
public class SegmentDataSingleSideEdge extends SegmentData {
	private int filledType;

	public SegmentDataSingleSideEdge(boolean onClient) {
		super(onClient);
	}

	public SegmentDataSingleSideEdge(boolean onClient, int filledType) {
		super(onClient);
		this.filledType = filledType;
		setOcree();
	}

	public void setOcree() {
		octree = new ArrayOctree(onServer);
	}

	@Override
	public void translateModBlocks() throws SegmentDataWriteException {
	}

	@Override
	public short getType(int index) {
		return isInSide(index) ? (short) (SegmentData4Byte.typeMask & filledType) : 0;
	}

	@Override
	public short getHitpointsByte(int index) {
		return isInSide(index) ? (short) ((SegmentData4Byte.hpMask & filledType) >> SegmentData4Byte.hpIndexStart) : 0;
	}

	@Override
	public boolean isActive(int index) {
		return isInSide(index) && (SegmentData4Byte.activationMask & filledType) > 0;
	}

	@Override
	public byte getOrientation(int index) {
		return isInSide(index) ? (byte) ((SegmentData4Byte.orientationMask & filledType) >> SegmentData4Byte.orientationIndexStart) : 0;
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

	private boolean isInSide(int index) {
		float x = (index & SEG_MINUS_ONE) - SEG_HALF + segment.pos.x;
		float y = (index >> Segment.DIM_BITS & SEG_MINUS_ONE) - SEG_HALF + segment.pos.y;
		float z = (index >> Segment.DIM_BITS_X2 & SEG_MINUS_ONE) - SEG_HALF + segment.pos.z;
		return IcosahedronHelper.isPointInSide(x, y, z);
	}

	@Override
	protected ArrayOctree getOctreeInstance(boolean onServer) {
		return null;
	}

	@Override
	public boolean containsFast(int index) {
		return isInSide(index);
	}

	@Override
	public void deserialize(DataInput inputStream, long time) throws IOException, SegmentDataWriteException {
		throw new SegmentDataWriteException(this);
	}

	@Override
	public void getBytes(int dataIndex, byte[] bytes) {
		assert (bytes.length >= 4);
		if(isInSide(dataIndex)) {
			int value = filledType;
			bytes[0] = (byte) (value & 0xFF);
			bytes[1] = (byte) ((value >> 8) & 0xFF);
			bytes[2] = (byte) ((value >> 16) & 0xFF);
			bytes[3] = (byte) ((value >> 24) & 0xFF);
		} else {
			Arrays.fill(bytes, 0, 4, (byte) 0);
		}
	}

	@Override
	public void serialize(DataOutput outputStream) throws IOException {
		for(int i = 0; i < TOTAL_SIZE; i++) {
			if(isInSide(i)) {
				int value = filledType;
				outputStream.writeByte((byte) (value & 0xFF));
				outputStream.writeByte((byte) ((value >> 8) & 0xFF));
				outputStream.writeByte((byte) ((value >> 16) & 0xFF));
				outputStream.writeByte((byte) ((value >> 24) & 0xFF));
			} else {
				outputStream.writeByte(0);
				outputStream.writeByte(0);
				outputStream.writeByte(0);
				outputStream.writeByte(0);
			}
		}
	}

	@Override
	public int getDataAt(int infoIndex) {
		return isInSide(infoIndex) ? filledType : 0;
	}

	@Override
	public byte[] getAsByteBuffer(byte[] out) {
		for(int i = 0; i < BLOCK_COUNT; i++) {
			if(isInSide(i)) {
				int dIndex = i * 4;
				int value = filledType;
				out[dIndex] = (byte) (value);
				out[dIndex + 1] = (byte) (value >> 8);
				out[dIndex + 2] = (byte) (value >> 16);
				out[dIndex + 3] = (byte) (value >> 24);
			}
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
		return SegmentDataType.SINGLE_SIDE_EDGE;
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
		for(int i = 0; i < BLOCK_COUNT; i++) {
			if(isInSide(i)) {
				out[i] = filledType;
			}
		}
		return out;
	}

	@Override
	public MemoryManager.MemIntArray getAsIntBuffer(MemoryManager.MemIntArray out) {
		for(int i = 0; i < BLOCK_COUNT; i++) {
			if(isInSide(i)) {
				out.put(i, filledType);
			}
		}
		return out;
	}

	@Override
	protected void copyTo(ByteBuffer buffer) {
		for(int i = 0; i < BLOCK_COUNT; i++) {
			if(isInSide(i)) {
				buffer.putInt(isInSide(i) ? filledType : 0);
			}
		}
	}
}
