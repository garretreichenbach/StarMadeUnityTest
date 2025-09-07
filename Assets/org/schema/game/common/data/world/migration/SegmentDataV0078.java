package org.schema.game.common.data.world.migration;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.octree.Octree;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.network.StateInterface;

public class SegmentDataV0078 {
	public static final int occlusionIndex = 0;
	public static final int lightBlockSize = 12;
	public static final int typeIndex = 0;

	// 0 - 5 Ambient
	// 6 - 11 Ambient
	// 12 - 14 Gather
	// 15 - 16 = type
	// 17	   = vis
	public static final int hitpointsIndex = 2;
	public static final int orientationVisLevel = 4;
	public static final int blockSize = 6;
	public static final int subLevelStart = 0; //64 possibilities
	public static final int subLevelEnd = 6;
	public static final int subVisStart = 6; //64 possibilities
	public static final int subVisEnd = 12;
	public static final int subOrientationStart = 12; //16 possibilities
	public static final int subOrientationEnd = 16;
	public static final int BLOCK_COUNT = SegmentData.SEG * SegmentData.SEG * SegmentData.SEG;
	public static final int TOTAL_SIZE = BLOCK_COUNT * blockSize;
	public static final int TOTAL_SIZE_LIGHT = BLOCK_COUNT * lightBlockSize;
	public static final int SEG_TIMES_SEG_TIMES_SEG = 16 * 16 * 16;
	public static final int SEG_TIMES_SEG = 16 * 16;
	public static final int SEG = 16;
	public static final int PIECE_ADDED = 0;
	public static final int PIECE_REMOVED = 1;
	public static final int PIECE_CHANGED = 2;
	public static final int PIECE_UNCHANGED = 3;
	private static final int MASK = 0xff;
	Vector3b helperPos = new Vector3b();
	private Segment segment;
	private byte[] data;
	private byte[] lightData;
	private int size;
	private Vector3b min = new Vector3b();
	private Vector3b max = new Vector3b();
	private Octree octree;
	private boolean needsRevalidate = false;
	private boolean softReset;
	private boolean revalidating;

	public SegmentDataV0078(boolean withLighting) {
		octree = new Octree(2, withLighting);
		//		System.err.println("ARRAYSIZE = "+arraySize+" from "+this.dim);
		data = new byte[TOTAL_SIZE];
		if (withLighting) {
			lightData = new byte[TOTAL_SIZE_LIGHT];
		}
		resetBB();

	}

	public static int getInfoIndex(byte x, byte y, byte z) {
		assert (valid(x, y, z)) : x + ", " + y + ", " + z + ": DIM " + SegmentData.SEG + ";";

		int i = blockSize * ((z * SEG_TIMES_SEG)
				+ (y * SEG) + x);

		return i;
	}

	public static int getInfoIndex(Vector3b pos) {
		return getInfoIndex(pos.x, pos.y, pos.z);
	}

	public static int getLightInfoIndex(byte x, byte y, byte z) {
		assert (valid(x, y, z)) : x + ", " + y + ", " + z + ": DIM " + SegmentData.SEG + ";";
		int i = lightBlockSize * ((z * SEG_TIMES_SEG)
				+ (y * SEG) + x);

		return i;
	}

	public static int getLightInfoIndex(Vector3b pos) {
		return getLightInfoIndex(pos.x, pos.y, pos.z);
	}

	public static int getLightInfoIndexFromIndex(int dataIndex) {
		return (dataIndex / blockSize) * lightBlockSize;
	}

	public static boolean valid(byte x, byte y, byte z) {
		boolean b = (x < SEG && y < SEG && z < SEG);
		b = b && (x >= 0 && y >= 0 && z >= 0);
		return b;
	}

	/**
	 * convert byte array (of size 4) to float
	 *
	 * @param test
	 * @return
	 */
	public static float byteArrayToFloat(byte test[]) {
		int bits = 0;
		int i = 0;
		for (int shifter = 3; shifter >= 0; shifter--) {
			bits |= (test[i] & MASK) << (shifter * 8);
			i++;
		}

		return Float.intBitsToFloat(bits);
	}

	/**
	 * convert float to byte array (of size 4)
	 *
	 * @param f
	 * @return
	 */
	public static byte[] floatToByteArray(float f) {
		int i = Float.floatToRawIntBits(f);
		return intToByteArray(i);
	}

	/**
	 * convert int to byte array (of size 4)
	 *
	 * @param param
	 * @return
	 */
	public static byte[] intToByteArray(int param) {
		byte[] result = new byte[4];
		for (int i = 0; i < 4; i++) {
			int offset = (result.length - 1 - i) * 8;
			result[i] = (byte) ((param >>> offset) & MASK);
		}
		return result;
	}

	/**
	 * be cautious using that method, because it will NOT update any adding
	 * or removing of segments. that has to be done extra!
	 *
	 * @param pos
	 * @param pieceData
	 * @return true if an element was added or removed
	 */
	public int applySegmentData(Vector3b pos, byte[] pieceData) {
		synchronized (this) {
			int index = getInfoIndex(pos);
			int c = 0;
			short oldType = getType(index);
			short oldHP = getHitpoints(index);
			byte vis = getVis(index);
			for (int i = index; i < index + blockSize; i++) {
				data[i] = pieceData[c++];
			}
			//restore vis because server sends always 0... FIXME: give no vis for server at all
			setVis(index, vis);
			short newType = getType(index);
			short newHP = getHitpoints(index);

			if (newType != oldType) {
				System.err.println("APPLY SEGMENT DATA " + oldType + " - " + newType);

				if (oldType == Element.TYPE_NONE && newType != Element.TYPE_NONE) {
					onAddingElement(index, pos.x, pos.y, pos.z, newType);
					return PIECE_ADDED;
				}
				if (oldType != Element.TYPE_NONE && newType == Element.TYPE_NONE) {
					onRemovingElement(index, pos.x, pos.y, pos.z, oldType);
					return PIECE_REMOVED;
				}
				return PIECE_CHANGED;
			} else {
				if (oldHP != newHP) {
					return PIECE_CHANGED;
				} else {
					return PIECE_UNCHANGED;
				}
			}

		}
	}

	public int arraySize() {
		return data.length;
	}

	public boolean contains(byte x, byte y, byte z) {
		if (valid(x, y, z)) {
			return containsUnsave(x, y, z);
		}
		return false;
	}

	public boolean contains(int index) {
		return getType(index) != Element.TYPE_NONE;
	}
	//	public Vector3b[] getAllPositions(){
	//		Vector3b[] poses = new Vector3b[getSize()];
	//		int c = 0;
	//		for(int i = 0; i< data.length; i++){
	//			if(data[i] != null){
	//				poses[c++] = new Vector3b(data[i].x(), data[i].y(), data[i].z());
	//			}
	//		}
	//		return poses;
	//	}

	/**
	 * @param pos
	 * @return true, if that element is currently in the PlanetSurface
	 */
	public boolean contains(Vector3b pos) {
		return contains(pos.x, pos.y, pos.z);
	}

	public boolean containsUnblended(byte x, byte y, byte z) {
		return valid(x, y, z) && getType(x, y, z) > Element.TYPE_NONE;
	}

	public boolean containsUnblended(Vector3b pos) {
		return containsUnblended(pos.x, pos.y, pos.z);
	}

	public boolean containsUnsave(byte x, byte y, byte z) {
		return getType(x, y, z) != Element.TYPE_NONE;
	}

	public boolean containsUnsave(int index) {
		return getType(index) != Element.TYPE_NONE;
	}

	public void createFromByteBuffer(byte[] bytes, StateInterface state) {
		ByteBuffer wrap = ByteBuffer.wrap(bytes);
		synchronized (this) {
			if (data == null) {
				data = new byte[BLOCK_COUNT * blockSize];
			}
			for (int i = 0; i < data.length; i++) {
				data[i] = wrap.get();
			}
		}
	}

	public void deserialize(DataInput inputStream) throws IOException {
		synchronized (this) {
			reset();

			if (!softReset) {
				inputStream.readFully(data);
				needsRevalidate = true;
			} else {
				byte[] buf = new byte[blockSize];
				int i = 0;
				//				while(i < data.length){
				inputStream.readFully(buf);
				//				}
			}
		}
		//		for(int i = 0; i < data.length; i++){
		//			if(data[i] != 0){
		//				System.err.println("DATADATA: "+i/blockSize+" / "+i%blockSize+": "+data[i]+": "+getType((i/blockSize) * blockSize)+"; "+getHitpoints((i/blockSize) * blockSize)+": "+getPosition(new Vector3b(), (i/blockSize) * blockSize)+", "+getType(getPosition(new Vector3b(), (i/blockSize) * blockSize)));
		//			}
		//		}

	}

	// public ByteBuffer getByteBuffer(){
	// //The byte array for output
	// byte[ ] byteArray = new byte[floatArray.length*4];
	// //Create a FloatBuffer 'view' on the ByteBuffer that wraps the byte array
	// FloatBuffer outFloatBuffer = ByteBuffer.wrap(byteArray).asfloatBuffer( );
	// //Write the array of floats into the byte array. FloatBuffer does this
	// efficiently
	// outFloatBuffer.put(floatArray, 0, floatArray.length);
	// //And write the length then the byte array
	// dataOut.writeInt(floatArray.length);
	// dataOut.write(byteArray, 0, floatArray.length*4);
	// dataOut.flush( );
	// dataOut.close( );
	// }
	public byte[] getAsBuffer() {
		//Create a FloatBuffer 'view' on the ByteBuffer that wraps the byte array

		//Write the array of floats into the byte array. FloatBuffer does this
		//efficiently

		//append size

		return data;
	}

	public short getHitpoints(int dataIndex) {
		return ByteUtil.shortReadByteArray(data, dataIndex + hitpointsIndex);
	}

	public Vector3b getMax() {
		return max;
	}

	public Vector3b getMin() {
		return min;
	}

	public byte getOcclusion(int dataIndex, int subIndex) {
		return ByteUtil.getHex(lightData, dataIndex, subIndex);
	}

	//
	// As far as I can tell, Derby will only store BLOBs inline with the other
	// database data, so you end up with the BLOB split up over a ton of
	// separate DB page files. This BLOB storage mechanism is good for ACID, and
	// good for smaller BLOBs (say, image thumbnails), but breaks down with
	// larger objects. According to the Derby docs, turning autocommit off when
	// manipulating BLOBs may also improve performance, but this will only go so
	// far.
	//
	// I strongly suggest you migrate to H2 or another DBMS if good performance
	// on large BLOBs is important, and the BLOBs must stay within the DB. You
	// can use the SQuirrel SQL client and its DBCopy plugin to directly migrate
	// between DBMSes (you just need to point it to the Derby/JavaDB JDBC driver
	// and the H2 driver). I'd be glad to help with this part, since I just did
	// it myself, and haven'transformationArray been happier.
	//
	// Failing this, you can move the BLOBs out of the database and into the
	// filesystem. To do this, you would replace the BLOB column in the database
	// with a BLOB size (if desired) and location (a URI or platform-dependent
	// file string). When creating a new blob, you create a corresponding file
	// in the filesystem. The location could be based off of a given directory,
	// with the primary key appended. For example, your DB is in
	// "DBFolder/DBName" and your blobs go in "DBFolder/DBName/Blob" and have
	// filename "BLOB_PRIMARYKEY.bin" or somesuch. To edit or read the BLOBs,
	// you query the DB for the location, and then do read/write to the file
	// directly. Then you log the new file size to the DB if it changed.

	public Octree getOctree() {
		return octree;
	}

	public void setOctree(Octree octree) {
		this.octree = octree;
	}

	public byte getOrientation(int dataIndex) {
		return (byte) ByteUtil.extractShort(ByteUtil.shortReadByteArray(data, dataIndex + orientationVisLevel), subOrientationStart, subOrientationEnd, this);
	}

	public Vector3b getPosition(Vector3b out, int index) {
		index /= blockSize;
		// re-engineer coordinates from index
		int z = (index / (SegmentData.SEG * SegmentData.SEG)) % SegmentData.SEG;
		int y = ((index % (SegmentData.SEG * SegmentData.SEG)) / SegmentData.SEG) % SegmentData.SEG;
		int x = ((index % (SegmentData.SEG * SegmentData.SEG)) % SegmentData.SEG);
		out.set((byte) x, (byte) y, (byte) z);
		return out;
	}

	/**
	 * @return the segment
	 */
	public Segment getSegment() {
		return segment;
	}

	/**
	 * @param segment the segment to set
	 */
	public void setSegment(Segment segment) {
		this.segment = segment;
	}

	/**
	 * @return the segment
	 */
	public SegmentController getSegmentController() {
		return segment.getSegmentController();
	}

	public byte[] getSegmentPieceData(int infoIndex, byte[] pieceDataOut) {
		int c = 0;
		for (int i = infoIndex; i < infoIndex + blockSize; i++) {
			pieceDataOut[c++] = data[i];
		}
		return pieceDataOut;

	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
		if (segment != null) {
			segment.setSize(size);
		}
		assert (size >= 0 && size <= BLOCK_COUNT) : "arraySize: " + BLOCK_COUNT;
	}

	public short getType(byte x, byte y, byte z) {
		int index = getInfoIndex(x, y, z);
		return getType(index);
	}

	public short getType(int index) {
		return ByteUtil.shortReadByteArray(data, index + typeIndex);
	}

	public short getType(Vector3b pos) {
		return getType(pos.x, pos.y, pos.z);
	}

	public byte getVis(byte x, byte y, byte z) {
		int index = getInfoIndex(x, y, z);
		return getVis(index);
	}

	public byte getVis(int index) {
		return (byte) ByteUtil.extractShort(ByteUtil.shortReadByteArray(data, index + orientationVisLevel), subVisStart, subVisEnd, this);
	}

	public byte getVis(Vector3b pos) {
		return getVis(pos.x, pos.y, pos.z);
	}

	/**
	 * @return the revalidating
	 */
	public boolean isRevalidating() {
		return revalidating;
	}

	public boolean needsRevalidate() {
		return needsRevalidate;
	}

	public boolean neighbors(byte x, byte y, byte z) {

		if (contains((byte) (x - 1), y, z)) {
			return true;
		}
		if (contains((byte) (x + 1), y, z)) {
			return true;
		}
		if (contains(x, (byte) (y - 1), z)) {
			return true;
		}
		if (contains(x, (byte) (y + 1), z)) {
			return true;
		}
		if (contains(x, y, (byte) (z - 1))) {
			return true;
		}
		if (contains(x, y, (byte) (z + 1))) {
			return true;
		}
		return false;
	}

	public void onAddingElement(int index, byte x, byte y, byte z, short newType) {
		synchronized (this) {
			int oldSize = size;
			// new element
			setSize(size + 1);
			octree.insert(x, y, z);
			helperPos.set(x, y, z);
			getSegmentController().onAddedElementSynched(newType, getOrientation(index), x, y, z, this.segment, true, segment.getAbsoluteIndex(x, y, z), 0, false);
			if (!revalidating) {
				segment.dataChanged(true);
			} else {
				//				System.err.println("Still revalidating");
			}
			updateBB(x, y, z);
		}
	}

	public void onRemovingElement(int index, byte x, byte y, byte z, short oldType) {
		synchronized (this) {
			int oldSize = size;
			// remove element
			setSize(size - 1);
			octree.delete(x, y, z);

			getSegmentController().onRemovedElementSynched(oldType, oldSize, x, y, z, (byte) 0, this.segment, false, 0);
			if (!revalidating) {
				segment.dataChanged(true);
			} else {
				//				System.err.println("Still revalidating");
			}
			updateBB(x, y, z);
		}
	}

	/**
	 * @param posTobe
	 * @return the element at this position null, if there is no element in that
	 * position
	 */
	public void removeInfoElement(byte x, byte y, byte z) {
		synchronized (this) {
			int i = getInfoIndex(x, y, z);
			data[i + typeIndex] = Element.TYPE_NONE;
		}
	}

	/**
	 * @param pos
	 * @return the element at this position null, if there is no element in that
	 * position
	 */
	public void removeInfoElement(Vector3b pos) {
		removeInfoElement(pos.x, pos.y, pos.z);
	}

	public void reset() {
		synchronized (this) {
			Arrays.fill(data, (byte) 0);
			if (lightData != null) {
				Arrays.fill(lightData, (byte) 0);
			}

			setSize(0);
			octree.reset();
			resetBB();
		}
	}

	public void resetBB() {
		max.set(Byte.MIN_VALUE, Byte.MIN_VALUE, Byte.MIN_VALUE);
		min.set(Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MAX_VALUE);

	}

	public void resetLight() {
		Arrays.fill(lightData, (byte) 0);
	}

	private void revalidate(byte x, byte y, byte z) {
		int index = getInfoIndex(x, y, z);
		short type = getType(index);
		if (type != Element.TYPE_NONE) {
			//			System.err.println("[SEGDATA] ADDED ELEMENT: "+x+", "+y+"; "+z+"; "+type);
			onAddingElement(index, x, y, z, type);
		}
	}

	public void revalidateData() {
		synchronized (this) {
			revalidating = true;
			octree.reset();
			if (size > 0) {
				System.err.println("[WARNING][SEGMENTDATA] segment not empty on revalidate. size was " + size + " in " + segment.pos + " -> " + getSegmentController());
				reset();
			}
			//			assert(getSize() == 0):" size is "+getSize()+" in "+getSegment().pos+" -> "+getSegmentController();
			for (byte z = 0; z < SegmentData.SEG; z++) {
				for (byte y = 0; y < SegmentData.SEG; y++) {
					for (byte x = 0; x < SegmentData.SEG; x++) {
						revalidate(x, y, z);
					}
				}
			}
			//			Vector3b p = new Vector3b();
			//			for(byte z = 0; z < SegmentData.SEG; z++){
			//				for(byte y = 0; y < SegmentData.SEG; y++){
			//					for(byte x = 0; x < SegmentData.SEG; x++){
			//						p.set(x,y,z);
			//						setVis(p,getSegment().getVisablility(p));
			//					}
			//				}
			//			}
			needsRevalidate = false;
			revalidating = false;
			segment.dataChanged(true);
		}

	}

	public void serialize(DataOutputStream outputStream) throws IOException {
		outputStream.write(data);
		//		int debug = 0;
		//		for(int i = 0; i < data.length; i++){
		//			if(data[i] != 0){
		////				System.err.println("DATADATA: "+i/blockSize+" / "+i%blockSize+": "+data[i]+": "+getType((i/blockSize) * blockSize)+"; "+getHitpoints((i/blockSize) * blockSize));
		//				debug ++;
		//			}
		//		}
		//		System.err.println("[SEGMENTDATA][SERIALIZE] written bytes != 0: "+debug);

	}

	public void setHitpoints(int index, short value) {
		ByteUtil.shortWriteByteArray((short) Math.max(0, value), data, index + hitpointsIndex);
	}

	public void setInfoElement(byte x, byte y, byte z, short newType) {
		synchronized (this) {
			int index = getInfoIndex(x, y, z);
			short oldType = ByteUtil.shortReadByteArray(data, index + typeIndex);

			ByteUtil.shortWriteByteArray(newType, data, index + typeIndex);

			if (newType == Element.TYPE_NONE) {
				// an existing element was set to NULL
				if (oldType != Element.TYPE_NONE) {
					onRemovingElement(index, x, y, z, oldType);
				}

			} else {
				// a NULL element was set to existing
				if (oldType == Element.TYPE_NONE) {
					onAddingElement(index, x, y, z, newType);

					//set hitpoints
					setHitpoints(index, ElementKeyMap.MAX_HITPOINTS);

				}

			}
		}

	}

	/**
	 * @param pos (provide real position)
	 * @return the element at this position null, if there is no element in that
	 * position
	 */
	//	public Element getInfoElement(Vector3b pos) {
	//		int i = getInfoIndex(pos) ;
	////		System.err.println("index for "+pos+": "+i);
	//		if(i >= 0 && i < data.length){
	//			return data[i];
	//		}else{
	//			System.err.println("referencing index out of bounds: "+pos+ " -> #"+i);
	//			return null;
	//		}
	//	}
	public void setInfoElement(Vector3b pos, short type) {

		//		System.err.println("setting info at "+pos+", Element: "+e.x+", "+e.y+", "+e.z+", index: "+getInfoIndex(pos.x, pos.y, pos.z, Element.HALF_SIZE));
		setInfoElement(pos.x, pos.y, pos.z, type);
	}

	public void setNeedsRevalidate(boolean needsRevalidate) {
		this.needsRevalidate = needsRevalidate;
	}

	public void setOcclusion(byte x, byte y, byte z, byte[] set) {
		int infoIndex = getLightInfoIndex(x, y, z);
		setOcclusion(infoIndex, set);
	}

	public void setOcclusion(int index, byte value, int subIndex) {
		ByteUtil.writeHex(lightData, index, subIndex, value);
		//		lightData[index + occlusionIndex + subIndex] = value;
	}

	public void setOcclusion(int index, byte[] set) {
		for (int i = 0; i < 24; i++) {
			setOcclusion(index, set[i], i);
		}
	}

	public void setOcclusionZero(int index) {
		Arrays.fill(lightData, index, lightBlockSize - 1, (byte) 0);

	}

	public void setOrientation(int index, byte value) {
		value = (byte) Math.max(0, Math.min(5, value));
		assert (value >= 0 && value < 6) : "NOT A SIDE INDEX";
		value = Element.orientationMapping[value];
		ByteUtil.putRangedBitsOntoShort(data, value, subOrientationStart, subOrientationEnd, index + orientationVisLevel, this);

		assert (value == getOrientation(index)) : "failed orientation coding: " + value + " != result " + getOrientation(index);
	}

	public void setSoftReset(boolean b) {
		this.softReset = b;

	}

	public void setTypeForcedWithoutUpdate(int index, short newType) {
		ByteUtil.shortWriteByteArray(newType, data, index + typeIndex);
	}

	public void setVis(byte x, byte y, byte z, byte vis) {
		int index = getInfoIndex(x, y, z);
		setVis(index, vis);
	}

	public void setVis(int index, byte vis) {
		//		byte oldVis = getVis(index);
		ByteUtil.putRangedBitsOntoShort(data, vis, subVisStart, subVisEnd, index + orientationVisLevel, this);
		assert (getVis(index) == vis);
		//		if(Segment.lastSegmentAdd != null && this == Segment.lastSegmentAdd.getSegmentData() && getInfoIndex(Segment.lastPosAdd) == index){
		//			System.err.println("VIS CHANGE !!! "+oldVis+" -> "+getVis(index)+"; "+this);
		//		}
	}

	public void setVis(Vector3b pos, byte vis) {
		setVis(pos.x, pos.y, pos.z, vis);
	}

	private void updateBB(byte x, byte y, byte z) {
		if (x > max.x) {
			max.x = x;
		}
		if (y > max.y) {
			max.y = y;
		}
		if (z > max.z) {
			max.z = z;
		}

		if (x < min.x) {
			min.x = x;
		}
		if (y < min.y) {
			min.y = y;
		}
		if (z < min.z) {
			min.z = z;
		}
		getSegmentController().getSegmentBuffer().updateBB(segment);
	}

}
