package org.schema.game.client.view.cubes;

public class CubeMeshBufferContainerOld {

//	ByteBuffer indexBufferBytes;
//	ShortBuffer indexBuffer;
//	FloatBuffer attibuteBuffer;
//	int[] blendedElementBuffer;
//	int blendedCount;
//	byte[] lightData;
//	byte[] visData;
//
//	public static CubeMeshBufferContainerOld getInstance() {
//		CubeMeshBufferContainerOld c = new CubeMeshBufferContainerOld();
//		c.generate();
//		return c;
//	}
//
//	public static int getLightInfoIndex(byte x, byte y, byte z) {
//		int i = SegmentData.lightBlockSize * ((z * SegmentData.SEG_TIMES_SEG)
//				+ (y * SegmentData.SEG) + x);
//
//		return i;
//	}
//
//	public static int getLightInfoIndex(Vector3b pos) {
//		return getLightInfoIndex(pos.x, pos.y, pos.z);
//	}
//
//	public void generate() {
//		blendedElementBuffer = new int[CubeInfo.CUBE_COUNT_PER_SEGMENT];
//		lightData = new byte[SegmentData.TOTAL_SIZE_LIGHT];
//		visData = new byte[SegmentData.BLOCK_COUNT];
//		// half the size, because it's the maximum vertices visible
//		indexBufferBytes = MemoryUtil.memAlloc(CubeInfo.INDEX_BUFFER_SIZE
//				* ByteUtil.SIZEOF_SHORT);
//
//		indexBuffer = indexBufferBytes.order(ByteOrder.nativeOrder()).asShortBuffer();
//
//		attibuteBuffer = MemoryUtil.memAllocFloat((CubeInfo.CUBE_VERTICES_FLOAT_COUNT / 3));
//		//		System.err.println("FLOAT BUFFER IS "+((CubeInfo.CUBE_VERTICES_FLOAT_COUNT / 3) *4)/1024+" kb");
//		attibuteBuffer.rewind();
//		indexBuffer.rewind();
//	}
//
//	public byte getOcclusion(int dataIndex, int subIndex) {
//		return ByteUtil.getHex(lightData, dataIndex, subIndex);
//	}
//
//	public byte getVis(byte x, byte y, byte z) {
//		int index = x + y * SegmentData.SEG + z * SegmentData.SEG_TIMES_SEG;
//		return getVis(index);
//	}
//
//	public byte getVis(int index) {
//		return visData[index];
//	}
//
//	public byte getVis(Vector3b pos) {
//		return getVis(pos.x, pos.y, pos.z);
//	}
//
//	public byte getVisFromDataIndex(int index) {
//		return visData[index / SegmentData.blockSize];
//	}
//
//	public void resetLight() {
//		Arrays.fill(lightData, (byte) 0);
//		Arrays.fill(visData, (byte) 0);
//
//	}
//
//	public void setOcclusion(byte x, byte y, byte z, byte[] set) {
//		int infoIndex = getLightInfoIndex(x, y, z);
//		setOcclusion(infoIndex, set);
//	}
//
//	public void setOcclusion(int index, byte value, int subIndex) {
//		ByteUtil.writeHex(lightData, index, subIndex, value);
//		//		lightData[index + occlusionIndex + subIndex] = value;
//	}
//
//	public void setOcclusion(int index, byte[] set) {
//		for (int i = 0; i < 24; i++) {
//			setOcclusion(index, set[i], i);
//		}
//	}
//
//	public void setOcclusionZero(int index) {
//		Arrays.fill(lightData, index, SegmentData.lightBlockSize - 1, (byte) 0);
//
//	}
//
//	public void setVis(byte x, byte y, byte z, byte vis) {
//		int index = x + y * SegmentData.SEG + z * SegmentData.SEG_TIMES_SEG;
//		setVis(index, vis);
//
//		assert (getVis(x, y, z) == vis);
//	}
//
//	public void setVis(int index, byte vis) {
//		visData[index] = vis;
//	}
//
//	public void setVis(Vector3b pos, byte vis) {
//		setVis(pos.x, pos.y, pos.z, vis);
//	}
}
