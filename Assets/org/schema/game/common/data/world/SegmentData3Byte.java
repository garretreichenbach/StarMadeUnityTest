package org.schema.game.common.data.world;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;
import org.schema.game.common.controller.EditableSendableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SegmentDataMetaData;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.VoidUniqueSegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.octree.ArrayOctree;

import javax.vecmath.Vector3f;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class SegmentData3Byte implements SegmentDataInterface {

	public static final int lightBlockSize = 39;
	public static final int typeIndexStart = 0;

	// 0 - 5 Ambient
	// 6 - 11 Ambient
	// 12 - 14 Gather
	// 15 - 16 = type
	// 17	   = vis
	public static final int typeIndexEnd = 11; //11 bits -> 2048 (0 incl)
	public static final int hitpointsIndexStart = 11; //8 bits -> 256
	public static final int hitpointsIndexEnd = 19;
	public static final int activeIndexStart = 19; //1 bit -> 1
	public static final int activeIndexEnd = 20;
	public static final int orientationStart = 20; //4 bit -> 16
	public static final int orientationEnd = 24;
	public static final int blockSize = 3;
	public static final byte ACTIVE_BIT = 8;
	public static final byte ACTIVE_BIT_HP = 16;
	public static final byte[] typeMap = new byte[(4096 * 2) * 3];
	public static final int maxHp = 256;
	public static final int maxHpHalf = 128;
	public static final byte[] hpMap = new byte[maxHp * 3];
	public static final byte[] hpMapHalf = new byte[maxHpHalf * 3];
	public static final int MAX_ORIENT = 16;
	public static final int FULL_ORIENT = 24;
	public static final byte[] orientMap = new byte[MAX_ORIENT * 3];
	public static final int SEG = Segment.DIM;
	public static final float SEGf = SEG;
	public static final byte ANTI_BYTE = -(SEG); // -16 -> 0xf0 -> 11110000;
	public static final int SEG_MINUS_ONE = SEG - 1;
	public static final int SEG_TIMES_SEG = SEG * SEG;
	public static final int SEG_TIMES_SEG_TIMES_SEG = SEG * SEG * SEG;
	public static final int BLOCK_COUNT = SEG_TIMES_SEG_TIMES_SEG;
	public static final int TOTAL_SIZE = BLOCK_COUNT * blockSize;
	public static final int TOTAL_SIZE_LIGHT = BLOCK_COUNT * lightBlockSize;
	public static final int t = 255;
	public static final int PIECE_ADDED = 0;
	public static final int PIECE_REMOVED = 1;
	public static final int PIECE_CHANGED = 2;
	public static final int PIECE_UNCHANGED = 3;
	public static final int PIECE_ACTIVE_CHANGED = 4;
	public static final int SEG_HALF = Segment.HALF_DIM;
	static final long yDelim = (Short.MAX_VALUE + 1) * 2;
	static final long zDelim = yDelim * yDelim;
	private static final int MASK = 0xff;
	private static final int TYPE_SECOND_MASK = 0x7;
	public static final int[] lookUpSecType = createLookup();

	static {
		int types = typeMap.length / 3;
		for (short s = 0; s < types; s++) {
			int index = s * 3;
			int intRead3ByteArray = ByteUtil.intRead3ByteArray(typeMap, index);
			int putRangedBitsOntoInt = ByteUtil.putRangedBitsOntoInt(intRead3ByteArray, s, typeIndexStart, typeIndexEnd, null);
			ByteUtil.intWrite3ByteArray(putRangedBitsOntoInt, typeMap, index, null);
		}
	}

	static {
		int hp = maxHp;
		for (short s = 0; s < hp; s++) {
			int index = s * 3;
			int intRead3ByteArray = ByteUtil.intRead3ByteArray(hpMap, index);
			int putRangedBitsOntoInt = ByteUtil.putRangedBitsOntoInt(intRead3ByteArray, s, hitpointsIndexStart, hitpointsIndexEnd, null);
			ByteUtil.intWrite3ByteArray(putRangedBitsOntoInt, hpMap, index, null);
		}
	}
	static{
		int hp = maxHpHalf;
		for (short s = 0; s < hp; s++) {
			int index = s * 3;
			int intRead3ByteArray = ByteUtil.intRead3ByteArray(hpMapHalf, index);
			int putRangedBitsOntoInt = ByteUtil.putRangedBitsOntoInt(intRead3ByteArray, s, hitpointsIndexStart, hitpointsIndexEnd-1, null);
			ByteUtil.intWrite3ByteArray(putRangedBitsOntoInt, hpMapHalf, index, null);
		}
	}

	static {
		int orient = MAX_ORIENT;
		for (short s = 0; s < orient; s++) {
			int index = s * 3;
			int intRead3ByteArray = ByteUtil.intRead3ByteArray(orientMap, index);
			int putRangedBitsOntoInt = ByteUtil.putRangedBitsOntoInt(intRead3ByteArray, s, orientationStart, orientationEnd, null);
			ByteUtil.intWrite3ByteArray(putRangedBitsOntoInt, orientMap, index, null);
		}
	}

	public final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Vector3b min = new Vector3b();
	private final Vector3b max = new Vector3b();
	private final ArrayOctree octree;
	private final boolean onServer;
	private Segment segment;
	private byte[] data;
	private int size;
	private boolean preserveControl;
	private boolean revalidating;
	private boolean blockAddedForced;
	
	private IntArrayList lodShapes;
	public boolean revalidatedOnce;
	private StringWriter errors;
	private StringWriter errors2;
	private boolean needsRevalidate;
	private static boolean DEBUG = false; 
	
	public SegmentData3Byte() {
		data = new byte[TOTAL_SIZE];
		octree = null;
		this.onServer = true;
	}
	public SegmentData3Byte(boolean onClient) {
		this.onServer = !onClient;
		octree = new ArrayOctree(this.onServer);
		//		System.err.println("ARRAYSIZE = "+arraySize+" from "+this.dim);
		data = new byte[TOTAL_SIZE];

		resetBB();
	}
	/**
	 * creates a copy with just the data copied (needs revalidation to be usable)
	 * @param segmentData
	 */
	public SegmentData3Byte(SegmentData3Byte segmentData) {
		this.onServer = segmentData.onServer;
		octree = new ArrayOctree(this.onServer);
		data = new byte[TOTAL_SIZE];
		System.arraycopy(segmentData.data, 0, data, 0, TOTAL_SIZE);
		resetBB();
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

	public static Vector3b getPositionFromIndex(int index, Vector3b out) {
		index /= blockSize;
		// re-engineer coordinates from index
		//		int z = ( index / (SEG_TIMES_SEG))% SEG;
		//		int y = ((index % (SEG_TIMES_SEG)) / SEG)%SEG;
		//		int x = ((index % (SEG_TIMES_SEG)) % SEG);

		int z = index / SEG_TIMES_SEG;
		index -= z * SEG_TIMES_SEG;
		int y = index / SEG;
		index -= y * SEG;
		int x = index;
		assert(valid(x, y, z)):x+", "+y+", "+z+"; "+index;
		out.set((byte) x, (byte) y, (byte) z);
		return out;
	}
	public static Vector3f getPositionFromIndexWithShift(int index, Vector3i segPos, Vector3f out) {
		index /= blockSize;
		// re-engineer coordinates from index
		//		int z = ( index / (SEG_TIMES_SEG))% SEG;
		//		int y = ((index % (SEG_TIMES_SEG)) / SEG)%SEG;
		//		int x = ((index % (SEG_TIMES_SEG)) % SEG);
		
		int z =  ((index >> 10) & 0x1F);
		int y =  ((index >> 5) & 0x1F);
		int x =  (index & 0x1F);
		
		assert(checkIndex(x,y,z,index)):x+", "+y+", "+z;
		
		out.set(segPos.x + x - SegmentData3Byte.SEG_HALF, segPos.y + y - SegmentData3Byte.SEG_HALF,  segPos.z + z - SegmentData3Byte.SEG_HALF);
		return out;
	}

	private static boolean checkIndex(int xx, int yy, int zz, int index) {
		int z = index / SEG_TIMES_SEG;
		index -= z * SEG_TIMES_SEG;
		int y = index / SEG;
		index -= y * SEG;
		int x = index;
		
		return xx == x && yy == y && zz == z;
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

	public static void main(String[] args) {
		byte[] data = new byte[3];

		//		ByteUtil.putRangedBits3OntoInt(data, 1, activeIndexStart, activeIndexEnd, 0, null);

		//		System.err.println(Arrays.toString(data));
		boolean active = true;
		if (!active) {
			data[0] &= (byte) (~ACTIVE_BIT);
		} else {
			data[0] |= ACTIVE_BIT;
		}
		assert (!active == ((data[0] & ACTIVE_BIT) == 0)) : data[0] + "; " + active;
		active = false;
		if (!active) {
			data[0] &= (byte) (~ACTIVE_BIT);
		} else {
			data[0] |= ACTIVE_BIT;
		}
		assert (!active == ((data[0] & ACTIVE_BIT) == 0));

		active = true;
		if (!active) {
			data[0] &= (byte) (~(byte) 16);
		} else {
			data[0] |= ACTIVE_BIT;
		}
		assert (!active == ((data[0] & ACTIVE_BIT) == 0));
		//		data[1] = Byte.MIN_VALUE;

		//		int mask = ((1 << (end - begin)) - 1);
		//		return ((value >> begin) & mask);
		Random r = new Random();
		for (short i = 0; i < maxHp; i++) {

			short newType = i;
			int index = 0;

			data[index] = (byte) r.nextInt(256);
			data[index + 1] = (byte) r.nextInt(256);
			data[index + 2] = (byte) r.nextInt(256);

			int tIndex = newType * 3;
			data[index] &= ~hpMap[hpMap.length - 3];
			data[index + 1] &= ~hpMap[hpMap.length - 2];

			data[index] |= hpMap[tIndex];
			data[index + 1] |= hpMap[tIndex + 1];

			int first = data[index + 2] & 0xff;
			int snd = data[index + 1] & 0xff;
			int third = data[index] & 0xff;
			short typeFast = (short)
					((
							((snd & hpMap[hpMap.length - 2]) >> 3)
									+
									(((third & hpMap[hpMap.length - 3]) * 32))
					));

			short t = (short) ByteUtil.extractInt(ByteUtil.intRead3ByteArray(data, 0), hitpointsIndexStart, hitpointsIndexEnd, new Object());
			System.err.println(newType + " -> " + typeFast + "; " + "; " + t);
			assert (t == newType);
			assert (t == typeFast);
		}

		for (short i = 0; i < MAX_ORIENT; i++) {

			short newType = i;
			int index = 0;

			data[index] = (byte) r.nextInt(256);
			data[index + 1] = (byte) r.nextInt(256);
			data[index + 2] = (byte) r.nextInt(256);

			int tIndex = newType * 3;
			data[index] &= ~orientMap[orientMap.length - 3];
			data[index + 1] &= ~orientMap[orientMap.length - 2];

			data[index] |= orientMap[tIndex];
			data[index + 1] |= orientMap[tIndex + 1];

			int first = data[index + 2] & 0xff;
			int snd = data[index + 1] & 0xff;
			int third = data[index] & 0xff;
			short typeFast = (short)
					((

							(((third & orientMap[orientMap.length - 3]) >> 4))
					));

			short t = (short) ByteUtil.extractInt(ByteUtil.intRead3ByteArray(data, 0), orientationStart, orientationEnd, new Object());
			System.err.println(newType + " -> " + typeFast + "; " + "; " + t);
			assert (t == newType);
			assert (t == typeFast);
		}

		{
			//		short newType = 1000;
			//		int index = 0;
			//		int tIndex = newType * 3;
			//		data[index+2] = ByteUtil.typeMap[tIndex+2];
			//		data[index+1] -= (data[index+1] & 7);
			//		data[index+1] |= ByteUtil.typeMap[tIndex+1];
			//
			//		int first = data[index+2] & 0xff;
			//		int snd = data[index+1] & 0xff;
			//		short typeFast = (short) (first + ((snd & 7) * 256) );
			//
			//
			//		short t = (short) ByteUtil.extractInt(ByteUtil.intRead3ByteArray(data, 0), 0, 11, new Object());
			//		System.err.println(newType+" -> "+typeFast+"; "+((snd) & 7)+"; "+t);
		}
		//		{
		//			short newType = 0;
		//			int index = 0;
		//			int tIndex = newType * 3;
		//			data[index+2] = ByteUtil.hpMap[tIndex+2];
		//			data[index+1] -= (data[index+1] & 7);
		//			data[index+1] |= ByteUtil.hpMap[tIndex+1];
		//
		//			int first = data[index+2] & 0xff;
		//			int snd = data[index+1] & 0xff;
		//			short typeFast = (short) (first + ((snd & 7) * 256) );
		//
		//
		//			short t = (short) ByteUtil.extractInt(ByteUtil.intRead3ByteArray(data, 0), 0, 11, new Object());
		//			System.err.println(newType+" -> "+typeFast+"; "+((snd) & 7)+"; "+t);
		//		}
		//

	}

	public static void setActive(int index, boolean active, byte[] data) {

		//IMPORTANT active is 0 flag, while inactive is ACTIVE_BIT

		if (!active) {
			data[index] |= ACTIVE_BIT;
		} else {
			data[index] &= (byte) (~ACTIVE_BIT);
		}
		//		ByteUtil.putRangedBits3OntoInt(data, active ? 0 : 1, activeIndexStart, activeIndexEnd, index, this);
	}
	public static void setActiveByHp(int index, boolean active, byte[] data) {
		
		//IMPORTANT active is 0 flag, while inactive is ACTIVE_BIT
		
		if (!active) {
			data[index] |= ACTIVE_BIT_HP;
		} else {
			data[index] &= (byte) (~ACTIVE_BIT_HP);
		}
		//		ByteUtil.putRangedBits3OntoInt(data, active ? 0 : 1, activeIndexStart, activeIndexEnd, index, this);
	}

	public static void setHitpoints(int index, short value, short type, byte[] data) {
		assert (value >= 0 && value < 512);
			int tIndex = value * 3;
			data[index] &= ~hpMap[hpMap.length - 3];
			data[index + 1] &= ~hpMap[hpMap.length - 2];
	
			data[index] |= hpMap[tIndex];
			data[index + 1] |= hpMap[tIndex + 1];

	}

	public static void setOrientation(int index, byte value, byte[] data) {
		assert (value >= 0 && value < MAX_ORIENT) : "NOT A SIDE INDEX";
		//		value = Element.orientationMapping[value];

		//			ByteUtil.putRangedBits3OntoInt(data, value, orientationStart, orientationEnd, index, this);
		data[index] &= ~orientMap[orientMap.length - 3];
		data[index] |= orientMap[value * 3];

	}

	public static void setType(int index, short newType, byte[] data) {
		if (newType < 2048) {
			int tIndex = newType * 3;
			data[index + 2] = typeMap[tIndex + 2];
			data[index + 1] -= (data[index + 1] & 7); //substract first 3 bytes
			data[index + 1] |= typeMap[tIndex + 1]; //put on new value
		} else {
			System.err.println("ERROR: Type is invalied. must be < 2048 but was: " + newType);
		}
		//		ByteUtil.putRangedBits3OntoInt(data, newType, typeIndexStart, typeIndexEnd, index, this);
	}

	public static boolean valid(final byte x, final byte y, final byte z) {
		return (((x | y | z) & ANTI_BYTE) == 0);
		//		return ((x & ANTI_BYTE) == 0) && ((y & ANTI_BYTE) == 0) && ((z & ANTI_BYTE) == 0);
		//		return x < SEG && y < SEG && z < SEG && x >= 0 && y >= 0 && z >= 0;
	}
	public static boolean valid(final int x, final int y, final int z) {
		return (((x | y | z) & ANTI_BYTE) == 0);
		//		return ((x & ANTI_BYTE) == 0) && ((y & ANTI_BYTE) == 0) && ((z & ANTI_BYTE) == 0);
		//		return x < SEG && y < SEG && z < SEG && x >= 0 && y >= 0 && z >= 0;
	}

	public static boolean allNeighborsInside(final byte x, final byte y, final byte z) {
		return x < SEG_MINUS_ONE && y < SEG_MINUS_ONE && z < SEG_MINUS_ONE && x >= 1 && y >= 1 && z >= 1;
	}

	public static boolean allNeighborsInside(final int x, final int y, final int z) {
		return x < SEG_MINUS_ONE && y < SEG_MINUS_ONE && z < SEG_MINUS_ONE && x >= 1 && y >= 1 && z >= 1;
	}

	public static int getInfoIndex(byte x, byte y, byte z) {
		int i = blockSize * ((z * SEG_TIMES_SEG) + (y * SEG) + x);
		return i;
	}

	public static int getInfoIndex(int x, int y, int z) {
		int i = blockSize * ((z * SEG_TIMES_SEG) + (y * SEG) + x);
		return i;
	}

	public static int getInfoIndex(Vector3b pos) {
		return getInfoIndex(pos.x, pos.y, pos.z);
	}

	public static int getInfoIndex(Vector3i pos) {
		return getInfoIndex(pos.x, pos.y, pos.z);
	}

	public static int[] createLookup() {
		int[] lookUpSecType = new int[256];
		for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
			int index = i & 0xff;
			lookUpSecType[index] = ((index & TYPE_SECOND_MASK) * 256);
		}

		return lookUpSecType;
	}


	/**
	 * be cautious using that method, because it will NOT update any adding
	 * or removing of segments. that has to be done extra!
	 *
	 * @return true if an element was added or removed
	 */
	public int applySegmentData(SegmentPiece block, long currentTime) {
		return applySegmentData(block.x, block.y, block.z, block.getData(), 0, false, block.getAbsoluteIndex(), true, true, currentTime);
	}

	/**
	 * be cautious using that method, because it will NOT update any adding
	 * or removing of segments. that has to be done extra!
	 *
	 * @param idServer
	 * @param pieceData
	 * @param l
	 * @return true if an element was added or removed
	 */
	public int applySegmentData(byte x, byte y, byte z, int pieceData, int offset, final boolean synched, long absIndex, boolean updateRemoveBB, boolean updateSegmentBB, long time) {
		//		assert(onServer || rwl.getWriteHoldCount() == 0);
		//		rwl.writeLock().t
		if (synched) {
			rwl.writeLock().lock();
		}
		try {

			int index = getInfoIndex(x, y, z);
//			int c = 0;
			boolean oldActive = isActive(index);
			short oldType = getType(index);
			byte oldOrientation = getOrientation(index);
//			final int sizeOffset = index+blockSize;

			System.arraycopy(pieceData, 0, data, index + offset, blockSize);

//			for(int i = index; i < sizeOffset; i++){
//				assert(data[i] == pieceData[offset+(c++)]);
//			}
			short newType = getType(index);
			short newHP = getHitpointsByte(index);

			if (newType == Element.TYPE_NONE && oldType == Element.TYPE_NONE) {
				//happens regularily as a confirmation from the server if
				//this client was the one removing
				return PIECE_UNCHANGED;
			}
			if (newType != oldType) {
				if (oldType == Element.TYPE_NONE && newType != Element.TYPE_NONE) {
					onAddingElement(index, x, y, z, newType, updateSegmentBB, synched, absIndex, time);
					return PIECE_ADDED;
				}
				if (oldType != Element.TYPE_NONE && newType == Element.TYPE_NONE) {
					onRemovingElement(index, x, y, z, oldType, updateRemoveBB, updateSegmentBB, oldOrientation, oldActive, synched, time);
					return PIECE_REMOVED;
				}
				assert (newType != Element.TYPE_NONE);
				return PIECE_CHANGED;
			} else {
				short oldHP = getHitpointsByte(index);
				if (oldHP != newHP) {
					return PIECE_CHANGED;
				} else {
					boolean nowActive = isActive(index);
					if (oldActive != nowActive) {

						if (ElementKeyMap.isDoor(newType)) {
							if (nowActive && !oldActive) {
								assert (onServer || rwl.getWriteHoldCount() == 0);
								assert(newType != ElementKeyMap.PICKUP_RAIL);
								//door became active
								octree.insert(x, y, z, index);
							} else if (!nowActive && oldActive) {
								//doore became inactive
								assert (onServer || rwl.getWriteHoldCount() == 0);
								octree.delete(x, y, z, index, newType);
							}
						}
						return PIECE_ACTIVE_CHANGED;
					} else if (oldOrientation != getOrientation(index)) {
						return PIECE_CHANGED;
					}

					return PIECE_UNCHANGED;
				}

			}
		} finally {
			if (synched) {
				rwl.writeLock().unlock();
			}
		}
	}

	public int arraySize() {
		return data.length;
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

	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */

	public boolean checkEmpty() {
		for (int i = 0; i < data.length; i += blockSize) {
			if (getType(i) != Element.TYPE_NONE) {
				return false;
			}
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */

	public boolean contains(byte x, byte y, byte z) {
		if (valid(x, y, z)) {
			return containsUnsave(x, y, z);
		}
		return false;
	}

	public boolean contains(int index) {
		return getType(index) != Element.TYPE_NONE;
	}

	/**
	 * @param pos
	 * @return true, if that element is currently in the PlanetSurface
	 */
	public boolean contains(Vector3b pos) {
		return contains(pos.x, pos.y, pos.z);
	}

	/**
	 * used in explosionOrder only
	 */
	@Override
	public int hashCode() {
		return segment.hashCode();
	}

	/**
	 * used in explosionOrder only
	 */
	@Override
	public boolean equals(Object obj) {
		return segment.pos.equals(((SegmentData3Byte) obj).segment.pos);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "(DATA: " + segment + ")";
	}

	public boolean containsFast(int index) {
		return data[index + 2] != 0 || (((data[index + 1] & 0xff) & 7) != 0);
	}

	public boolean containsFast(Vector3b pos) {
		return containsFast(getInfoIndex(pos));
	}

	public boolean containsFast(Vector3i pos) {
		return containsFast(getInfoIndex(pos));
	}

	public boolean containsUnblended(byte x, byte y, byte z) {
		if (valid(x, y, z)) {
			short type = getType(x, y, z);
			return type != Element.TYPE_NONE && !ElementKeyMap.getInfo(type).isBlended();

		} else {
			return false;
		}
	}

	public boolean containsUnblended(Vector3b pos) {
		return containsUnblended(pos.x, pos.y, pos.z);
	}

	public boolean containsUnsave(byte x, byte y, byte z) {
		return containsFast(getInfoIndex(x, y, z));
	}

	public boolean containsUnsave(int x, int y, int z) {
		return containsFast(getInfoIndex(x, y, z));
	}

	public boolean containsUnsave(int index) {
		return getType(index) != Element.TYPE_NONE;
	}

//	public float damage(float damageInitial, Vector3i oPos, float armorEfficiency, float armorHarden, float radius, SegmentDamageCallback cb) {
//		float totalDamage = 0;
//		rwl.writeLock().lock();
//		try {
//			if (radius == 0) {
//				System.err.println("[SERVER] WARNING: EXPLOSION RADIUS WAS ZERO");
//				return 0;
//			}
//
//			int i = 0;
//			float radiusSquared = radius * radius;
//			float radSquInv = 1f / radiusSquared;
//
//			boolean center = getSegment().pos.equals(0, 0, 0) && getSegmentController() instanceof Ship;
//			float hitpoints;
//			for (byte z = 0; z < SEG; z++) {
//				for (byte y = 0; y < SEG; y++) {
//					for (byte x = 0; x < SEG; x++) {
//						//						i = getInfoIndex(x, y, z);
//						short type;
//						if ((type = getType(i)) != Element.TYPE_NONE && (hitpoints = getHitpointsByte(i)) > 0) {
//
//							float xP = oPos.x - x;
//							float yP = oPos.y - y;
//							float zP = oPos.z - z;
//
//							float d = (xP * xP) + (yP * yP) + (zP * zP);
//
//							if (d < radiusSquared) {
//								float armourPercent = ElementKeyMap.getInfo(type).getArmourPercent();
//
//								//								float armor = (armourPercent*1f+armorHarden);
//								//								int actualDamage = (int) Math.max(0, damage - Math.ceil(damage * (armor + armor*armorEfficiency)));
//
//								float damage = (1.0f - Math.min(1f, d * radSquInv)) * damageInitial;
//
////								float armor = Math.min(0.99f, (elementInformation.getArmourPercent()+(getBlockEffectManager().status.armorHarden)));
////								int actualDamage = (int) Math.max(0, damage - Math.ceil(damage * (armor + armor*armorEfficiency)));
//
////								armourPercent = Math.min(0.99f, (armourPercent+(armorHarden)));//armourPercent * (1f+armorHarden);
////								armourPercent = Math.min(0.99f, (armourPercent+(armorHarden)));//armourPercent + (armorEfficiency * armourPercent);
//
////								float armour = FastMath.ceil(damage * armourPercent);
//
//								float armour = Math.min(0.99f, (armourPercent + (armorHarden)));
//
////								damage = Math.max(0, damage - armour);
//								damage = (float) Math.max(0, damage - Math.ceil(damage * (armour + armour * armorEfficiency)));
//								if (damage > 0) {
//
//									cb.damage(x, y, z, getSegment(), (int) damage, type);
//
//									float damageDone = Math.min(hitpoints + (hitpoints * armourPercent + (hitpoints * ElementKeyMap.getInfo(type).getExplosionAbsorbtion())), damage + (damage * armourPercent + (damage * ElementKeyMap.getInfo(type).getExplosionAbsorbtion())));
//
//									//									System.err.println("Damage DONE: "+x+", "+y+"; "+z+"; "+damageDone);
//
//									totalDamage += damageDone;
//
//									if ((short) (Math.max(0, hitpoints - damage)) <= 0) {
//										setHitpoints(i, (short) 0);
//										if (center && x == SegmentData3Byte.SEG_HALF && y == SegmentData3Byte.SEG_HALF && z == SegmentData3Byte.SEG_HALF) {
//											System.err.println("[HIT-SEGMENTDATA] Core Destroyed " + getSegment());
//										} else {
//										}
//									} else {
//										setHitpoints(i, (short) Math.min(255, Math.max(0, (hitpoints - damage))));
//										assert (getHitpoints(i) > 0);
//									}
//								}
//							}
//						}
//						i += blockSize;
//					}
//				}
//			}
//			return totalDamage;
//		} finally {
//			rwl.writeLock().unlock();
//		}
//	}

	public void deserialize(DataInput inputStream, long time) throws IOException {
		rwl.writeLock().lock();
		try {
			reset(time);
			inputStream.readFully(data);
			needsRevalidate = true;
		} finally {
			rwl.writeLock().unlock();
		}

		//		for(int i = 0; i < data.length; i++){
		//			if(data[i] != 0){
		//				System.err.println("DATADATA: "+i/blockSize+" / "+i%blockSize+": "+data[i]+": "+getType((i/blockSize) * blockSize)+"; "+getHitpoints((i/blockSize) * blockSize)+": "+getPosition(new Vector3b(), (i/blockSize) * blockSize)+", "+getType(getPosition(new Vector3b(), (i/blockSize) * blockSize)));
		//			}
		//		}

	}

	@Override
	public void translateModBlocks() throws SegmentDataWriteException {
		throw new RuntimeException("SegmentData3Byte does not support mod blocks");
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
	@Override
	public byte[] getAsOldByteBuffer() {
		return data;
	}

	@Override
	public void migrateTo(int fromVersion, SegmentDataInterface segmentData) {
		try{
			for(int i = 0; i < BLOCK_COUNT; i++){
				
				short oldType = getType(i*blockSize);
				int oldOrient = getOrientation(i*blockSize);
				boolean oldAct = isActive(i*blockSize);
				int oldHP = Math.min(127, getHitpointsByte(i*blockSize));
				
				oldOrient = convertOrient(oldType, oldOrient, oldAct);
				
				segmentData.setType			(i, oldType);
				segmentData.setOrientation	(i, (byte)oldOrient);
				segmentData.setActive		(i, oldAct);
				segmentData.setHitpointsByte(i, oldHP);
				
				assert(segmentData.getOrientation(i) == oldOrient);
				assert(segmentData.getType(i) == getType(i*blockSize) );
				
			}
		} catch (SegmentDataWriteException e) {
			e.printStackTrace();
			throw new RuntimeException("this should be never be thrown as migration should always be to"
					+ "a normal segment data", e);
		}
	}
	
	public static final int convertOrient(short oldType, int oldOrient, boolean oldAct){
		if(ElementKeyMap.isValidType(oldType)){
			ElementInformation info = ElementKeyMap.getInfoFast(oldType);
			if(info.blockStyle != BlockStyle.NORMAL && info.getBlockStyle() != BlockStyle.SPRITE && info.getBlockStyle() != BlockStyle.WEDGE){
				oldOrient = (oldOrient + (oldAct ? 0 : MAX_ORIENT)) % (BlockShapeAlgorithm.algorithms[info.blockStyle.id-1].length-1);
			}
		}
		return oldOrient;
	}
	
	private static SegmentData3Byte tt = new SegmentData3Byte();
	public static void migrateTo(byte a, byte b, byte c, SegmentPiece segmentData) {
		tt.data[0] = a;
		tt.data[1] = b;
		tt.data[2] = c;
			
		short oldType = tt.getType(0);
		int oldOrient = tt.getOrientation(0);
		boolean oldAct = tt.isActive(0);
		int oldHP = Math.min(127, tt.getHitpointsByte(0));
		
		oldOrient = convertOrient(oldType, oldOrient, oldAct);
		
		segmentData.setType			(oldType);
		segmentData.setOrientation	((byte)oldOrient);
		segmentData.setActive		(oldAct);
		segmentData.setHitpointsByte(oldHP);
			
	}
	public static void migratePiece(VoidUniqueSegmentPiece sp) {
		int oldOrient = sp.getOrientation();
		boolean oldAct = sp.isActive();
		if(ElementKeyMap.isValidType(sp.getType())){
			ElementInformation info = ElementKeyMap.getInfoFast(sp.getType());
			if(info.blockStyle != BlockStyle.NORMAL){
				oldOrient = (oldOrient + (oldAct ? 0 : MAX_ORIENT)) % (BlockShapeAlgorithm.algorithms[info.blockStyle.id-1].length-1);
			}
		}
		
		sp.setOrientation((byte)oldOrient);
	}
@Override
	public void setType(int index, short newType) {
		setType(index, newType, data);
	}

	@Override
	public void setHitpointsByte(int index, int value) {
		assert (value >= 0 && value < 256) : value;

		short s = getType(index);
		
		
		int tIndex = value * 3;
		data[index] &= ~hpMap[hpMap.length - 3];
		data[index + 1] &= ~hpMap[hpMap.length - 2];

		data[index] |= hpMap[tIndex];
		data[index + 1] |= hpMap[tIndex + 1];

		assert (getHitpointsByte(index) == value);
	}

	/**
	 * 1 = inactive; 0 = active (because active should be default)
	 *
	 * @param index
	 * @param active
	 */
	@Override
	public void setActive(int index, boolean active) {

		//IMPORTANT active is 0 flag, while inactive is ACTIVE_BIT

		if (!active) {
			data[index] |= ACTIVE_BIT;
		} else {
			data[index] &= (byte) (~ACTIVE_BIT);
		}
		assert (active == isActive(index)) : active + "; " + isActive(index);
		//		ByteUtil.putRangedBits3OntoInt(data, active ? 0 : 1, activeIndexStart, activeIndexEnd, index, this);
	}
	public void setActiveByHp(int index, boolean active) {
		
		//IMPORTANT active is 0 flag, while inactive is ACTIVE_BIT
		
		if (!active) {
			data[index] |= ACTIVE_BIT_HP;
		} else {
			data[index] &= (byte) (~ACTIVE_BIT_HP);
		}
		assert (active == isActiveByHp(index)) : active + "; " + isActiveByHp(index);
		//		ByteUtil.putRangedBits3OntoInt(data, active ? 0 : 1, activeIndexStart, activeIndexEnd, index, this);
	}

	//	public  void setOctree(ArrayOctree octree) {
	//		this.octree = octree;
	//	}
	@Override
	public void setOrientation(int index, byte value) {
		assert (value >= 0 && value < MAX_ORIENT) : "NOT A SIDE INDEX";
		//		value = Element.orientationMapping[value];

		//			ByteUtil.putRangedBits3OntoInt(data, value, orientationStart, orientationEnd, index, this);
		data[index] &= ~orientMap[orientMap.length - 3];
		data[index] |= orientMap[value * 3];

		assert (value == getOrientation(index)) : "failed orientation coding: " + value + " != result " + getOrientation(index);
	}

	@Override
	public short getType(int index) {
		return (short) ((data[index + 2] & 0xff) + lookUpSecType[data[index + 1] & 0xff]);
	}

	@Override
	public short getHitpointsByte(int dataIndex) {

		//		return  (short) ByteUtil.extractInt(ByteUtil.intRead3ByteArray(data, dataIndex), hitpointsIndexStart, hitpointsIndexEnd, this);

		int snd = data[dataIndex + 1] & 0xff;
		int third = data[dataIndex] & 0xff;

		return (short)
				((
						((snd & hpMap[hpMap.length - 2]) >> 3)
								+
								(((third & hpMap[hpMap.length - 3]) * 32))
				));
	}

	/**
	 * 1 = inactive; 0 = active (because active should be default)
	 *
	 * @param currentIndex
	 * @param activeProhibiter
	 */
	@Override
	public boolean isActive(int dataIndex) {
		return (data[dataIndex] & ACTIVE_BIT) == 0;
	}
	public boolean isActiveByHp(int dataIndex) {
		return (data[dataIndex] & ACTIVE_BIT_HP) == 0;
	}

	@Override
	public byte getOrientation(int dataIndex) {
		return (byte) (((data[dataIndex] & 0xff) & orientMap[orientMap.length - 3]) >> 4);
	}

	@Override
	public void setExtra(int index, byte extra) throws SegmentDataWriteException {

	}

	@Override
	public int getExtra(int index) {
		return 0;
	}

	public void getBytes(int dataIndex, byte[] bytes){
		assert(bytes.length >= 3);
		bytes[0] = data[dataIndex];
		bytes[1] = data[dataIndex+1];
		bytes[2] = data[dataIndex+2];
	}

	/**
	 * @return the segment
	 */
	@Override
	public Segment getSegment() {
		return segment;
	}

	/**
	 * @return the segment
	 */
	@Override
	public SegmentController getSegmentController() {
		return segment.getSegmentController();
	}

	@Override
	public void resetFast() {
		revalidatedOnce = false;
		setSize(0);
		Arrays.fill(data, (byte) 0);
		setSize(0);
		if(octree != null){
			octree.reset();
		}
		resetBB();
		setSize(0);
	}

	/**
	 * @param segment the segment to set
	 */
	public void setSegment(Segment segment) {
		this.segment = segment;
	}

	public Vector3b getMax() {
		return max;
	}

	public Vector3b getMin() {
		return min;
	}

	public ArrayOctree getOctree() {
		return octree;
	}

	public byte[] getSegmentPieceData(int infoIndex, byte[] pieceDataOut) {
		int c = 0;
		for (int i = infoIndex; i < infoIndex + blockSize; i++) {
			pieceDataOut[c++] = data[i];
		}
		return pieceDataOut;

	}

	@Override
	public int getSize() {
		return size;
	}

	@Override
	public void setSize(int size) {
		assert ((size >= 0 && size <= BLOCK_COUNT)) : "Exception WARNING: SEGMENT SIZE WRONG " + size + " " + (segment != null ? (segment.getSegmentController().getState() + ": " + segment.getSegmentController() + " " + segment) : "");

		this.size = size;
		if (segment != null) {
			segment.setSize(this.size);
		}
	}

	@Override
	public short getType(byte x, byte y, byte z) {
		int index = getInfoIndex(x, y, z);
		return getType(index);
	}

	public short getType(Vector3b pos) {
		return getType(pos.x, pos.y, pos.z);
	}
	public byte getOrientation(Vector3b pos) {
		return getOrientation(pos.x, pos.y, pos.z);
	}

	public byte getOrientation(byte x, byte y, byte z) {
		return getOrientation(getInfoIndex(x, y, z));
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
		if (!allNeighborsInside(x, y, z)) {
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
		} else {
			if (containsUnsave((byte) (x - 1), y, z)) {
				return true;
			}
			if (containsUnsave((byte) (x + 1), y, z)) {
				return true;
			}
			if (containsUnsave(x, (byte) (y - 1), z)) {
				return true;
			}
			if (containsUnsave(x, (byte) (y + 1), z)) {
				return true;
			}
			if (containsUnsave(x, y, (byte) (z - 1))) {
				return true;
			}
			if (containsUnsave(x, y, (byte) (z + 1))) {
				return true;
			}
		}
		return false;
	}

	private void onAddingElement(int index, byte x, byte y, byte z, short newType, boolean updateSegmentBB, final boolean synched, long absIndex, long time) {
		if (synched) {
			rwl.writeLock().lock();
		}
		try {
			onAddingElementUnsynched(index, x, y, z, newType, updateSegmentBB, true, absIndex, null, time);
		} finally {
			if (synched) {
				rwl.writeLock().unlock();
			}
		}

	}

	private boolean inOctree(short type, int index){
		ElementInformation info = ElementKeyMap.infoArray[type];
		return (!(info.isDoor() && !((data[index] & ACTIVE_BIT) == 0))) && (info.isInOctree());
	}
	private void onAddingElementUnsynched(int index, byte x, byte y, byte z, short newType, boolean updateSegmentBB, boolean segBufferUpdate, long absIndex, SegmentDataMetaData metaData, long time) {
		int oldSize = this.size;

		incSize();

		if (inOctree(newType, index)) {
			assert(newType != ElementKeyMap.PICKUP_RAIL);
			assert(newType != ElementKeyMap.PICKUP_AREA);
			assert(newType != ElementKeyMap.EXIT_SHOOT_RAIL);
			octree.insert(x, y, z, index);
		}else{
			assert(newType != ElementKeyMap.SHIPYARD_CORE_POSITION);
		}
		if(ElementKeyMap.isLodShape(newType)){
			addLodShape(index, x,y,z);
		}
		if(metaData != null){
			metaData.onAddedElementSynched(newType, 
					x, y, z, 
					this, index, 
					absIndex);
		}else{
			byte orientation = getOrientation(index);
			getSegmentController().onAddedElementSynched(newType, orientation, x, y, z, this.segment, false, absIndex, time, revalidating);
			if(segBufferUpdate && !revalidating){
				
				getSegmentController().getSegmentBuffer().onAddedElement(newType, oldSize, x, y, z, segment, time, orientation);
			}
			if (getSegmentController().isOnServer() && !revalidating) {
				((EditableSendableSegmentController)getSegmentController()).doDimExtensionIfNecessary(segment, x, y, z);
			}
			if (!revalidating) {
				segment.dataChanged(true);
				// new element
	
			}
		}
		updateBB(x, y, z, true, updateSegmentBB, index/blockSize);
	}
	
	private void removeLodShape(int index, byte x, byte y, byte z) {
		int i = lodShapes.indexOf(index);
		if(i >= 0){
			lodShapes.remove(i);
		}else{
			assert(false):index;
		}
		
	}
	private void addLodShape(int index, byte x, byte y, byte z) {
		if(lodShapes == null){
			lodShapes = new IntArrayList();
		}
		
		lodShapes.add(index);
		
	}

	public void onRemovingElement(int index, byte x, byte y, byte z, short oldType, boolean updateBBGeneral, boolean updateSegmentBB, byte oldOrientation, boolean wasActive, final boolean synched, long time) {
		if (synched) {
			rwl.writeLock().lock();
		}
		try {
			int oldSize = size;
			// remove element
			setSize(oldSize - 1);
			if (inOctree(oldType, index)) {
				octree.delete(x, y, z, index, oldType);
			}
			if(ElementKeyMap.isLodShape(oldType)){
				removeLodShape(index, x,y,z);
			}
			getSegmentController().onRemovedElementSynched(oldType, oldSize, x, y, z, oldOrientation, this.segment, preserveControl, time);
			if (!revalidating) {
				segment.dataChanged(true);
			} else {
				//				System.err.println("Still revalidating");
			}
			if(updateBBGeneral){
				updateBB(x, y, z, updateSegmentBB, false, index/blockSize);
			}
		} finally {
			if (synched) {
				rwl.writeLock().unlock();
			}
		}
	}
	

	/**
	 * @param posTobe
	 * @return the element at this position null, if there is no element in that
	 * position
	 */
	public void removeInfoElement(byte x, byte y, byte z) {
		setType(getInfoIndex(x, y, z), Element.TYPE_NONE, data);
	}

	/**
	 * @param pos
	 * @return the element at this position null, if there is no element in that
	 * position
	 */
	public void removeInfoElement(Vector3b pos) {
		removeInfoElement(pos.x, pos.y, pos.z);
	}

	public void reset(long time) {
		rwl.writeLock().lock();
		try {
			this.preserveControl = true;
			if (segment != null) {
				long xAbsStart = (segment.pos.x & 0xFFFF);
				long yAbsStart = ((long) (segment.pos.y & 0xFFFF) << 16);
				long zAbsStart = ((long) (segment.pos.z & 0xFFFF) << 32);
				long xAbs = xAbsStart;
				long yAbs = yAbsStart;
				long zAbs = zAbsStart;

				for (byte z = 0; z < SEG; z++) {
					for (byte y = 0; y < SEG; y++) {
						for (byte x = 0; x < SEG; x++) {

						}
					}
				}

				for (byte z = 0; z < SegmentData3Byte.SEG; z++) {
					for (byte y = 0; y < SegmentData3Byte.SEG; y++) {
						for (byte x = 0; x < SegmentData3Byte.SEG; x++) {

							//tested in elementCollection to be correct
							long absIndex = xAbs + yAbs + zAbs;
							setInfoElementUnsynched(x, y, z, Element.TYPE_NONE, false, absIndex, time);
							xAbs++;
						}
						xAbs = xAbsStart;
						yAbs += yDelim;
					}
					xAbs = xAbsStart;
					yAbs = yAbsStart;
					zAbs += zDelim;
				}

				segment.setSize(0);
				//				SegmentController segmentController = getSegmentController();
				//				if(segmentController != null){
				//					segmentController.dec(this);
				//				}
			}
			this.preserveControl = false;
			setSize(0);
			octree.reset();
			resetBB();
		} finally {
			rwl.writeLock().unlock();
		}

	}

	public void resetBB() {
		assert (size == 0);
		max.set(Byte.MIN_VALUE, Byte.MIN_VALUE, Byte.MIN_VALUE);
		min.set(Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MAX_VALUE);

	}

	public void restructBB(boolean updateSegmentBB) {

		int minBeforeX = min.x;
		int minBeforeY = min.y;
		int minBeforeZ = min.z;

		int maxBeforeX = max.x;
		int maxBeforeY = max.y;
		int maxBeforeZ = max.z;

		max.set(Byte.MIN_VALUE, Byte.MIN_VALUE, Byte.MIN_VALUE);
		min.set(Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MAX_VALUE);
		octree.resetAABB16();
		
		int i = 0;
		int index = 0;
		for (byte zC = 0; zC < SegmentData3Byte.SEG; zC++) {
			for (byte yC = 0; yC < SegmentData3Byte.SEG; yC++) {
				for (byte xC = 0; xC < SegmentData3Byte.SEG; xC++) {

					if (containsFast(i)) {
						//						try{
						//						throw new NullPointerException();
						//						}catch(NullPointerException e){
						//							e.printStackTrace();
						//						}
						//						System.err.println("CONTAINS "+xC+"; "+yC+"; "+zC);
						updateBB(xC, yC, zC, false, true, index);
						
					}

					i += blockSize;
					index++;
				}
			}
		}
	}


	private void unvalidate(byte x, byte y, byte z, int index, long time) {

		short type = getType(index);
		byte o = getOrientation(index);
		if (type != Element.TYPE_NONE) {
			onRemovingElement(index, x, y, z, type, false, false, o, isActive(index), false, time);
		}

	}

	public void unvalidateData(long time) {
		rwl.writeLock().lock();
		try {
			revalidating = true;
			//			octree.reset();
			//			if(getSize() > 0){
			//				System.err.println("[WARNING][SEGMENTDATA] segment not empty on revalidate. size was "+getSize()+" in "+getSegment().pos+" -> "+getSegmentController());
			//				reset();
			//			}
			int i = 0;
			for (byte z = 0; z < SegmentData3Byte.SEG; z++) {
				for (byte y = 0; y < SegmentData3Byte.SEG; y++) {
					for (byte x = 0; x < SegmentData3Byte.SEG; x++) {
						unvalidate(x, y, z, i, time);
						i += blockSize;
					}
				}
			}

			getSegmentController().getSegmentBuffer().updateBB(segment);
			revalidating = false;
			segment.dataChanged(true);
		} finally {
			rwl.writeLock().unlock();
		}

	}

	public void revalidateData(long time, SegmentDataMetaData metaData) {
		assert(!this.revalidatedOnce):errors+"; "+errors2+"; "+getSegmentController();
		
		this.revalidatedOnce = true;
		
		if(DEBUG){
			try{
				throw new Exception("BUILT REVALIDATE "+segment.pos);
			}catch(Exception r){
				errors = new StringWriter();
				r.printStackTrace(new PrintWriter(errors));
			}
		}
		if(metaData == null){
			rwl.writeLock().lock();
		}
		try {
			if(metaData != null){
				metaData.reset(false);
				metaData.segPos.set(segment.pos);
			}
			revalidating = true;
			assert (size == 0) : " size is " + size + " in " + segment.pos + " -> " + getSegmentController();
			int index = 0;

			long xAbsStart = (segment.pos.x & 0xFFFF);
			long yAbsStart = ((long) (segment.pos.y & 0xFFFF) << 16);
			long zAbsStart = ((long) (segment.pos.z & 0xFFFF) << 32);
			long xAbs = xAbsStart;
			long yAbs = yAbsStart;
			long zAbs = zAbsStart;

			long t = System.currentTimeMillis();
			for (byte z = 0; z < SegmentData3Byte.SEG; z++) {
				for (byte y = 0; y < SegmentData3Byte.SEG; y++) {
					for (byte x = 0; x < SegmentData3Byte.SEG; x++) {

						//inline
						int type = ((data[index + 2] & 0xff) + lookUpSecType[data[index + 1] & 0xff]);
						if (type != Element.TYPE_NONE) {
							//also inline and fast route
							if (type <= ElementKeyMap.highestType && ElementKeyMap.validArray[type]) {
//								int hp = getHitpointsByte(index);
//								int maxHp = ElementKeyMap.infoArray[type].getMaxHitPoints();
//
//								if (getHitpoints(index) > maxHp) {
//									setHitpoints(index, Math.min(maxHp, hp));
//								}
								onAddingElementUnsynched(index, x, y, z, (short)type, false, true, xAbs+yAbs+zAbs, metaData, time);
								assert ((size >= 0 && size <= BLOCK_COUNT)) : "Exception WARNING: SEGMENT SIZE WRONG " + size + " " + (segment != null ? (segment.getSegmentController().getState() + ": " + segment.getSegmentController() + " " + segment) : "");

							} else {
								setType(index, Element.TYPE_NONE, data);
								assert (getType(x, y, z) == Element.TYPE_NONE) : "FAILED: " + type + "; " + x + ", " + y + ", " + z;
							}
						}

						index += blockSize;
						xAbs++;
					}
					xAbs = xAbsStart;
					yAbs += yDelim;
				}
				xAbs = xAbsStart;
				yAbs = yAbsStart;
				zAbs += zDelim;
			}
			assert (isBBValid()):getSegmentPos()+"; size "+size+"; minmax "+min+", "+max;

			
			long tookRe = System.currentTimeMillis() - t;
			if (tookRe > 50 && !getSegmentController().isOnServer()) {
				System.err.println("[CLIENT] " + segment + " WARNING: Revalidating took " + tookRe + "ms (without locks)");
			}
			if(metaData == null){
				//extend dimention if needed
				if (getSegmentController() instanceof EditableSendableSegmentController) {
					((EditableSendableSegmentController) getSegmentController())
					.doDimExtensionIfNecessary(segment,
							min.x, min.y, min.z);
					((EditableSendableSegmentController) getSegmentController())
					.doDimExtensionIfNecessary(segment,
							(byte) (max.x - 1), (byte) (max.y - 1), (byte) (max.z - 1));
				}
			
				getSegmentController().getSegmentBuffer().updateBB(segment);
				needsRevalidate = false;
			}
			revalidating = false;
			
			segment.dataChanged(true);
		} finally {
			if(metaData == null){
				rwl.writeLock().unlock();
			}
		}

	}
	public void revalidateMeta(SegmentDataMetaData buildMetaData) {
		if(DEBUG){
			try{
				throw new Exception("BUILT META "+segment.pos);
			}catch(Exception r){
				errors2 = new StringWriter();
				r.printStackTrace(new PrintWriter(errors2));
			}
		}
		rwl.writeLock().lock();
		try{
			revalidating = true;
			
			buildMetaData.apply(segment, getSegmentController());
			
			getSegmentController().getSegmentBuffer().updateBB(segment);
			//extend dimention if needed
			((EditableSendableSegmentController) getSegmentController())
			.doDimExtensionIfNecessary(segment,
					min.x, min.y, min.z);
			((EditableSendableSegmentController) getSegmentController())
			.doDimExtensionIfNecessary(segment,
					(byte) (max.x - 1), (byte) (max.y - 1), (byte) (max.z - 1));
			
			needsRevalidate = false;
			revalidating = false;
		}finally{
			rwl.writeLock().unlock();
		}
	}

	public boolean revalidateSuccess() {
		for (byte z = 0; z < SegmentData3Byte.SEG; z++) {
			for (byte y = 0; y < SegmentData3Byte.SEG; y++) {
				for (byte x = 0; x < SegmentData3Byte.SEG; x++) {
					short type = getType(x, y, z);
					if (type != Element.TYPE_NONE && !ElementKeyMap.exists(type)) {
						System.err.println("FAILED: " + type + "; " + x + ", " + y + ", " + z);
						return false;
					}
				}
			}
		}
		return true;
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

//	public void setHitpoints(int index, int value) {
//		assert (value >= 0 && value < 256) : value;
//
//		int tIndex = value * 3;
//		data[index] &= ~hpMap[hpMap.length - 3];
//		data[index + 1] &= ~hpMap[hpMap.length - 2];
//
//		data[index] |= hpMap[tIndex];
//		data[index + 1] |= hpMap[tIndex + 1];
//
//		assert (getHitpoints(index) == value);
//
//	}

	//	public void setInfoElement(byte x, byte y, byte z, short newType, boolean updateSegmentBB){
	//		sdsdsynchronized(this){
	//			setInfoElementUnsynched(x, y, z, newType, (byte)-1, (byte)-1, updateSegmentBB);
	//		}
	//
	//	}
	//	public void setInfoElement(byte x, byte y, byte z, short newType, byte orientation, boolean updateSegmentBB){
	//		sdsdsynchronized(this){
	//			setInfoElementUnsynched(x, y, z, newType, orientation, (byte)-1, updateSegmentBB);
	//		}
	//
	//	}
	public void setInfoElement(byte x, byte y, byte z, short newType, byte orientation, byte activation, short hitpoints, boolean updateSegmentBB, long absIndex, long time) {
		rwl.writeLock().lock();
		try {
			setInfoElementUnsynched(x, y, z, newType, orientation, activation, hitpoints, updateSegmentBB, absIndex, time);
		} finally {
			rwl.writeLock().unlock();
		}

	}

	public void setInfoElement(byte x, byte y, byte z, short type, boolean updateSegmentBB, long absIndex, long time) {
		setInfoElement(x, y, z, type, (byte) -1, (byte) -1, type == 0 ? 0 : ElementKeyMap.getInfo(type).getMaxHitPointsByte(), updateSegmentBB, absIndex, time);
	}

	public void setInfoElement(Vector3b pos, short type, boolean updateSegmentBB, long absIndex, long time) {

		//		System.err.println("setting info at "+pos+", Element: "+e.x+", "+e.y+", "+e.z+", index: "+getInfoIndex(pos.x, pos.y, pos.z, Element.HALF_SIZE));
		setInfoElement(pos.x, pos.y, pos.z, type, updateSegmentBB, absIndex, time);
	}

	public void setInfoElement(Vector3b pos, short type,
	                           byte elementOrientation, byte activation, boolean updateSegmentBB, long absIndex, long time) {
		setInfoElement(pos.x, pos.y, pos.z, type, elementOrientation, activation, 
				type == 0 ? 0 : ElementKeyMap.getInfo(type).getMaxHitPointsByte(), updateSegmentBB, absIndex, time);

	}

	/**
	 * WARNING: this method is only safe if it's called before the segment is
	 * in the segment buffer
	 * It is only called during generataion
	 *
	 * @param x
	 * @param y
	 * @param z
	 * @param newType
	 * @param updateSegmentBB
	 */
	@Override
	public void setInfoElementForcedAddUnsynched(byte x, byte y, byte z, short newType, boolean updateSegmentBB) {
		setInfoElementForcedAddUnsynched(x, y, z, newType, (byte) -1, ElementInformation.activateOnPlacement(newType), updateSegmentBB);
	}

	/**
	 * WARNING: this method is only safe if it's called before the segment is
	 * in the segment buffer
	 * It is only called during generataion
	 *
	 * @param x
	 * @param y
	 * @param z
	 * @param newType
	 * @param orientation
	 * @param activation
	 * @param updateSegmentBB
	 */
	@Override
	public void setInfoElementForcedAddUnsynched(byte x, byte y, byte z, short newType, byte orientation, byte activation, boolean updateSegmentBB) {
		if (newType == Element.TYPE_NONE) {
			//			System.err.println("WARNING: Tried to set None Element on Forced Add");
			return;
		}

		int index = getInfoIndex(x, y, z);

		setType(index, newType);
		
		if (orientation > -1) {
			setOrientation(index, orientation);
		}
		if (activation > -1) {
			setActive(index, activation != 0);
		}

		this.blockAddedForced = true;
		
		setHitpointsByte(index, ElementKeyMap.MAX_HITPOINTS);
	}
	
	public void setInfoElementForcedAddUnsynched(int dataIndex, byte byte0, byte byte1, byte byte2){
		data[dataIndex] = byte0;
		data[dataIndex + 1] = byte1;
		data[dataIndex + 2] = byte2;
		
		this.blockAddedForced = true;
	}

	public void setInfoElementUnsynched(byte x, byte y, byte z, short newType, boolean updateSegmentBB, long absIndex, long time) {
		setInfoElementUnsynched(x, y, z, newType, (byte) -1, (byte) -1, updateSegmentBB, absIndex, time);
	}

	public void setInfoElementUnsynched(byte x, byte y, byte z, short newType, byte orientation, byte activation, boolean updateSegmentBB, long absIndex, long time) {		
		
		setInfoElementUnsynched(x,y,z,newType,orientation,activation,
				(newType > 0 && ElementKeyMap.isValidType(newType)) ? ElementKeyMap.MAX_HITPOINTS : 0,updateSegmentBB,absIndex,time);
	}
	
	public void setInfoElementUnsynched(byte x, byte y, byte z, short newType, byte orientation, byte activation, short hitpoints, boolean updateSegmentBB, long absIndex, long time) {
		int index = getInfoIndex(x, y, z);
		short oldType = getType(index);
		byte oldOrientation = getOrientation(index);

		setType(index, newType, data);
		if (orientation > -1) {
			setOrientation(index, orientation);
		}
		boolean wasActive = isActive(index);
		if (activation > -1) {
			setActive(index, activation != 0);
		}

		if (newType == Element.TYPE_NONE) {
			setActive(index, false);
			setOrientation(index, (byte) 0);
			// an existing element was set to NULL
			if (oldType != Element.TYPE_NONE) {
				onRemovingElement(index, x, y, z, oldType, true, updateSegmentBB, oldOrientation, wasActive, false, time);
			}

		} else {
			// a NULL element was set to existing
			if (oldType == Element.TYPE_NONE) {
				onAddingElementUnsynched(index, x, y, z, newType, updateSegmentBB, true, absIndex, null, time);

				//set hitpoints
				setHitpointsByte(index, hitpoints);
			} else {
				// an element has been changed (e.g. active)
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
	public void setInfoElementUnsynched(Vector3b pos, short type, boolean updateSegmentBB, long absIndex, long time) {
		setInfoElementUnsynched(pos.x, pos.y, pos.z, type, updateSegmentBB, absIndex, time);
	}

	public void setNeedsRevalidate(boolean needsRevalidate) {
		this.needsRevalidate = needsRevalidate;
	}

	public void incSize() {
		this.size++;
		assert ((size >= 0 && size <= BLOCK_COUNT)) : "Exception WARNING: SEGMENT SIZE WRONG " + size + " " + (segment != null ? (segment.getSegmentController().getState() + ": " + segment.getSegmentController() + " " + segment) : "");

		if (segment != null) {
			segment.setSize(this.size);
		}
	}

	private void updateBB(byte x, byte y, byte z, boolean updateSegmentBB, boolean add, int index) {

		if (add) {
			if (x >= max.x) {
				max.x = (byte) (x + 1);
			}
			if (y >= max.y) {
				max.y = (byte) (y + 1);
			}
			if (z >= max.z) {
				max.z = (byte) (z + 1);
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
			octree.insertAABB16(x, y, z, index);
			if (updateSegmentBB) {
				getSegmentController().getSegmentBuffer().updateBB(segment);
			}
			
		} else {
			
			if (size == 0) {
				if (updateSegmentBB) {
					max.set(Byte.MIN_VALUE, Byte.MIN_VALUE, Byte.MIN_VALUE);
					min.set(Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MAX_VALUE);
					octree.resetAABB16();
					restructBB(updateSegmentBB);
				}
			} else {
				if (x + 1 < max.x && y + 1 < max.y && z + 1 < max.z &&
						x > min.x && y > min.y && z > min.z) {
					//removed from middle -> nothing to do
					//					System.err.println("UPDATED FROM MIDDLE: NOTHING TO DO "+x+", "+y+", "+z+"; BB ("+min+"; "+max+")");
				} else {
					if (updateSegmentBB) {
						restructBB(updateSegmentBB);
					}
				}
			}
		}

		//		assert(min.x >= 0 && min.y >= 0 && min.z >= 0);
		//		assert(max.x < SegmentData.SEG && max.y < SegmentData.SEG && max.z < SegmentData.SEG);

	}

	public void writeSingle(int index, DataOutputStream stream) throws IOException {
		stream.write(data, index, 3);
		//		stream.writeByte(data[index]);
		//		stream.writeByte(data[index+1]);
		//		stream.writeByte(data[index+2]);
	}

	public void readSingle(int index, DataInputStream in) throws IOException {
		in.read(data, index, 3);
		//		data[index] = in.readByte();
		//		data[index+1] = in.readByte();
		//		data[index+2] = in.readByte();
	}

	/**
	 * @return the blockAddedForced
	 */
	public boolean isBlockAddedForced() {
		return blockAddedForced;
	}

	/**
	 * @param blockAddedForced the blockAddedForced to set
	 */
	public void setBlockAddedForced(boolean blockAddedForced) {
		this.blockAddedForced = blockAddedForced;
	}

	public boolean isBBValid() {
		return (min.x <= max.x)
				&& (min.y <= max.y)
				&& (min.z <= max.z);
	}


	

	public IntArrayList getLodShapes() {
		return lodShapes;
	}

	private float[] lodData;
	private short[] lodTypeAndOrientcubeIndex;
	private static int lodLightNum = 4;
	public static final int lodDataSize = (4 + 3)*lodLightNum; //4 for color, 3 for dir, times amount of lights
	

	public float[] getLodData() {
		return lodData;
	}

	
	public void calculateLodLight(int lodIndex, int infoIndex, float[] sideData, float[] lodData, short[] lodTypeAndOrientcubeIndex) {
		int startIndex = (lodIndex * 6 * 4);

		int tAndOStartIndex = lodIndex * 2;
		byte localAlgoIndex = BlockShapeAlgorithm.getLocalAlgoIndex(6, getOrientation(infoIndex));
		short type = getType(infoIndex);
		lodTypeAndOrientcubeIndex[tAndOStartIndex+0] = type;
		lodTypeAndOrientcubeIndex[tAndOStartIndex+1] = localAlgoIndex;
		
		
		Oriencube orientcube = (Oriencube) BlockShapeAlgorithm.algorithms[5][localAlgoIndex];
		if(type != 0 && ElementKeyMap.getInfoFast(type).getId() == 104 ){
			int o = localAlgoIndex%6;
			orientcube = Oriencube.getOrientcube(
					o, o > 1 ? Element.FRONT : Element.TOP);
		}
		byte prim = orientcube.getOrientCubePrimaryOrientation();
		byte primOpp = (byte) Element.getOpposite(prim);
		
		int lightStartIndexTot = lodIndex * lodDataSize;
		
		
		
		
		int index = 0;
		for(int i = 0; i < 6; i++){
			if(i != prim && i != primOpp){
				
				int lightStartIndex = lightStartIndexTot + index * (4+3);
				int posIndex = lightStartIndex + 4;
				
				lodData[lightStartIndex+0] = 0;
				lodData[lightStartIndex+1] = 0;
				lodData[lightStartIndex+2] = 0;
				lodData[lightStartIndex+3] = 0;
				
				
				float coloring = 0;
				
				lodData[posIndex+0] = Element.DIRECTIONSf[prim].x + Element.DIRECTIONSf[i].x;
				lodData[posIndex+1] = Element.DIRECTIONSf[prim].y + Element.DIRECTIONSf[i].y;
				lodData[posIndex+2] = Element.DIRECTIONSf[prim].z + Element.DIRECTIONSf[i].z;
				
				
				int sideDataStartIndex = startIndex + i * 4;
				
				if(sideData[sideDataStartIndex] >= 0){
					lodData[lightStartIndex+0] = sideData[sideDataStartIndex+0];
					lodData[lightStartIndex+1] = sideData[sideDataStartIndex+1];
					lodData[lightStartIndex+2] = sideData[sideDataStartIndex+2];
					lodData[lightStartIndex+3] = sideData[sideDataStartIndex+3];
					coloring++;
				}
				
				int sideDataStartIndexPrim = startIndex + prim * 4;
				
				float primFac = 0.01f;
				if(sideData[sideDataStartIndexPrim] >= 0){
					lodData[lightStartIndex+0] += sideData[sideDataStartIndexPrim+0] * primFac;
					lodData[lightStartIndex+1] += sideData[sideDataStartIndexPrim+1] * primFac;
					lodData[lightStartIndex+2] += sideData[sideDataStartIndexPrim+2] * primFac;
					lodData[lightStartIndex+3] += sideData[sideDataStartIndexPrim+3] * primFac;
					coloring+= primFac;
				}
				if(coloring > 0f){
					float f = 1f/coloring;
					lodData[lightStartIndex+0] *= f;
					lodData[lightStartIndex+1] *= f;
					lodData[lightStartIndex+2] *= f;
					lodData[lightStartIndex+3] *= f;
				}
				index++;
				
//				System.err.println("LIGHT COL OF "+lightPos+" ---> "+lightCol);
			}
		}
		assert(index == 4);
	}

	public short[] getLodTypeAndOrientcubeIndex() {
		return lodTypeAndOrientcubeIndex;
	}
	public IntArrayList drawingLodShapes;

	@Override
	public Vector3i getSegmentPos() {
		return segment.pos;
	}
	@Override
	public boolean isIntDataArray() {
				return false;
	}
	@Override
	public int inflate(Inflater inflater, byte[] byteFormatBuffer) throws SegmentInflaterException, DataFormatException {
		int inflate = inflater.inflate(data);
		if (inflate != data.length) {
			throw new SegmentInflaterException(inflate, data.length);
		}
		return inflate;
	}
	@Override
	public SegmentData doBitmapCompressionCheck(RemoteSegment seg) {
		return null;
	}
	@Override
	public void setDataAt(int i, int data) throws SegmentDataWriteException {
		throw new SegmentDataWriteException(null);
	}
	@Override
	public int readFrom(ByteBuffer uncompressed) throws SegmentDataWriteException {
		throw new UnsupportedOperationException("only top version chunks support this");
	}
	




}
