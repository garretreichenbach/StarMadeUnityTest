package org.schema.game.common.data.world;

import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.element.world.FastValidationContainer;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.io.SegmentSerializationBuffersGZIP;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.server.data.GameServerState;

import javax.vecmath.Vector3f;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

public class Chunk16SegmentData implements SegmentDataInterface {

	
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
	public static final int SEG = 16;
	public static final int SEG_HALF = 8;
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
	public long lastChanged;
	static final long yDelim = (Short.MAX_VALUE + 1) * 2;
	static final long zDelim = yDelim * yDelim;
	private static final int MASK = 0xff;
	private static final int TYPE_SECOND_MASK = 0x7;
	private static final int[] lookUpSecType = createLookup();
	public static final int SHIFT_ = 8;
	public static final Vector3i SHIFT = new Vector3i(SHIFT_,SHIFT_,SHIFT_);
	
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
	private byte[] data;
	private int size;
	private boolean revalidating;
	private boolean blockAddedForced;
	private FastValidationContainer fastValidationIdex;
	
	public final Vector3i segmentPos = new Vector3i();
	public Chunk16SegmentData() {
		//		System.err.println("ARRAYSIZE = "+arraySize+" from "+this.dim);
		data = new byte[TOTAL_SIZE];

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
		out.set((byte) x, (byte) y, (byte) z);
		return out;
	}
	public static Vector3f getPositionFromIndexWithShift(int index, Vector3i segPos, Vector3f out) {
		index /= blockSize;
		// re-engineer coordinates from index
		//		int z = ( index / (SEG_TIMES_SEG))% SEG;
		//		int y = ((index % (SEG_TIMES_SEG)) / SEG)%SEG;
		//		int x = ((index % (SEG_TIMES_SEG)) % SEG);
		
		int z =  ((index >> 8) & 0xF);
		int y =  ((index >> 4) & 0xF);
		int x =  (index & 0xF);
		
		assert(checkIndex(x,y,z,index)):x+", "+y+", "+z;
		
		out.set(segPos.x + x - SEG_HALF, segPos.y + y - SEG_HALF,  segPos.z + z - SEG_HALF);
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
		//		ByteUtil.putRangedBits3OntoInt(data, value, hitpointsIndexStart, hitpointsIndexEnd, index, this);
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
			data[index + 2] = 
					typeMap[tIndex + 2];
			data[index + 1] -= 
					(data[index + 1] & 7); //substract first 3 bytes
			data[index + 1] |= 
					typeMap[tIndex + 1]; //put on new value
		} else {
			System.err.println("ERROR: Type is invalied. must be < 4096 but was: " + newType);
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

	

	public int arraySize() {
		return data.length;
	}
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



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "(DATA: CHUNK16)";
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

	

	public void deserialize(DataInput inputStream, long time) throws IOException {
		rwl.writeLock().lock();
		try {
			reset(time);
			inputStream.readFully(data);
			setNeedsRevalidate(true);
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
		assert (false);
	}

	@Override
	public void setType(int index, short newType) {
		setType(index, newType, data);
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

		//		return  (byte) ByteUtil.extractInt(ByteUtil.intRead3ByteArray(data, dataIndex), orientationStart, orientationEnd, this);

		return (byte) (((data[dataIndex] & 0xff) & orientMap[orientMap.length - 3]) >> 4);
	}

	@Override
	public void setExtra(int index, byte extra) throws SegmentDataWriteException {

	}

	@Override
	public int getExtra(int index) {
		return 0;
	}

	/**
	 * @return the segment
	 */
	@Override
	public Segment getSegment() {
		return null;
	}

	/**
	 * @return the segment
	 */
	@Override
	public SegmentController getSegmentController() {
		return null;
	}

	@Override
	public void resetFast() {
		size = 0;
		Arrays.fill(data, (byte) 0);
		size = 0;
		resetBB();
		size = 0;
	}


	public Vector3b getMax() {
		return max;
	}

	public Vector3b getMin() {
		return min;
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
		this.size = size;
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

	private void onAddingElementUnsynched(int index, byte x, byte y, byte z, short newType, boolean updateSegmentBB, boolean segBufferUpdate, long absIndex, long time) {
		int oldSize = this.size;

		incSize();

		updateBB(x, y, z, updateSegmentBB, true);
	}
	

	public void onRemovingElement(int index, byte x, byte y, byte z, short oldType, boolean updateSegmentBB, byte oldOrientation, boolean wasActive, final boolean synched, long time) {
		if (synched) {
			rwl.writeLock().lock();
		}
		try {
			int oldSize = size;
			// remove element
			size = oldSize - 1;
			updateBB(x, y, z, updateSegmentBB, false);
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
			Arrays.fill(data, (byte)0);
			size = 0;
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

	

	private void unvalidate(byte x, byte y, byte z, int index, long time) {

		short type = getType(index);
		byte o = getOrientation(index);
		if (type != Element.TYPE_NONE) {
			onRemovingElement(index, x, y, z, type, false, o, isActive(index), false, time);
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
			for (byte z = 0; z < SEG; z++) {
				for (byte y = 0; y < SEG; y++) {
					for (byte x = 0; x < SEG; x++) {
						unvalidate(x, y, z, i, time);
						i += blockSize;
					}
				}
			}

			revalidating = false;
		} finally {
			rwl.writeLock().unlock();
		}

	}

	public boolean revalidateSuccess() {
		for (byte z = 0; z < SEG; z++) {
			for (byte y = 0; y < SEG; y++) {
				for (byte x = 0; x < SEG; x++) {
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
	public void setInfoElement(byte x, byte y, byte z, short newType, byte orientation, byte activation, boolean updateSegmentBB, long absIndex, long time) {
		rwl.writeLock().lock();
		try {
			setInfoElementUnsynched(x, y, z, newType, orientation, activation, updateSegmentBB, absIndex, time);
		} finally {
			rwl.writeLock().unlock();
		}

	}

	public void setInfoElement(byte x, byte y, byte z, short type, boolean updateSegmentBB, long absIndex, long time) {
		setInfoElement(x, y, z, type, (byte) -1, (byte) -1, updateSegmentBB, absIndex, time);
	}

	public void setInfoElement(Vector3b pos, short type, boolean updateSegmentBB, long absIndex, long time) {

		//		System.err.println("setting info at "+pos+", Element: "+e.x+", "+e.y+", "+e.z+", index: "+getInfoIndex(pos.x, pos.y, pos.z, Element.HALF_SIZE));
		setInfoElement(pos.x, pos.y, pos.z, type, updateSegmentBB, absIndex, time);
	}

	public void setInfoElement(Vector3b pos, short type,
	                           byte elementOrientation, byte activation, boolean updateSegmentBB, long absIndex, long time) {
		setInfoElement(pos.x, pos.y, pos.z, type, elementOrientation, activation, updateSegmentBB, absIndex, time);

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
		
		setHitpointsByte(index, ElementKeyMap.getInfoFast(newType).getMaxHitPointsByte());
	}

	public void setInfoElementUnsynched(byte x, byte y, byte z, short newType, boolean updateSegmentBB, long absIndex, long time) {
		setInfoElementUnsynched(x, y, z, newType, (byte) -1, (byte) -1, updateSegmentBB, absIndex, time);
	}

	public void setInfoElementUnsynched(byte x, byte y, byte z, short newType, byte orientation, byte activation, boolean updateSegmentBB, long absIndex, long time) {
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
				onRemovingElement(index, x, y, z, oldType, updateSegmentBB, oldOrientation, wasActive, false, time);
			}

		} else {
			// a NULL element was set to existing
			if (oldType == Element.TYPE_NONE) {
				onAddingElementUnsynched(index, x, y, z, newType, updateSegmentBB, true, absIndex, time);

				//set hitpoints
				setHitpointsByte(index, ElementKeyMap.MAX_HITPOINTS);
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
	}

	public void incSize() {
		this.size++;

	}

	private void updateBB(byte x, byte y, byte z, boolean updateSegmentBB, boolean add) {

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
		} else {
		}

		//		assert(min.x >= 0 && min.y >= 0 && min.z >= 0);
		//		assert(max.x < SEG && max.y < SEG && max.z < SEG);

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

	public void resetFastValidationIndex() {
		if(this.fastValidationIdex != null){
			this.fastValidationIdex.clear();
			
		}
		this.fastValidationIdex = null;
	}

	public void createFastValidationIndex(FastValidationContainer a) {
		return;
//		this.fastValidationIdex = a;
//		int index = 0;
//
//		long xAbsStart = (segment.pos.x & 0xFFFF);
//		long yAbsStart = ((long) (segment.pos.y & 0xFFFF) << 16);
//		long zAbsStart = ((long) (segment.pos.z & 0xFFFF) << 32);
//		long xAbs = xAbsStart;
//		long yAbs = yAbsStart;
//		long zAbs = zAbsStart;
//
//		long t = System.currentTimeMillis();
//		for (byte z = 0; z < SEG; z++) {
//			for (byte y = 0; y < SEG; y++) {
//				for (byte x = 0; x < SEG; x++) {
//
//					//tested in elementCollection to be correct
////					revalidate(x, y, z, index, xAbs+yAbs+zAbs, time);
////					short type = getType(index);
//
//					//inline
//					int type = ((data[index + 2] & 0xff) + lookUpSecType[data[index + 1] & 0xff]);
//					if (type != Element.TYPE_NONE) {
//						//also inline and fast route
//						if (type <= ElementKeyMap.highestType && ElementKeyMap.validArray[type]) {
//							int hp = getHitpoints(index);
//							int maxHp = ElementKeyMap.infoArray[type].getMaxHitPoints();
//
//							if (getHitpoints(index) > maxHp) {
//								setHitpoints(index, Math.min(maxHp, hp));
//							}
//							this.size++;
//							segment.setSize(this.size);
//
//							if (!(ElementKeyMap.infoArray[type].isDoor() && !((data[index] & ACTIVE_BIT) == 0))) {
//								getOctree().insert(x, y, z, index);
//							}
//
//							updateBB(x, y, z, false, true);
//							
//							a.a.add((short)x);
//							a.a.add((short)y);
//							a.a.add((short)z);
//							a.a.add((short)type);
//							a.l.add(xAbs + yAbs + zAbs);
//							
//
//						} else {
//							setType(index, Element.TYPE_NONE, data);
//							assert (getType(x, y, z) == Element.TYPE_NONE) : "FAILED: " + type + "; " + x + ", " + y + ", " + z;
//						}
//					}
//
//					index += blockSize;
//					xAbs++;
//				}
//				xAbs = xAbsStart;
//				yAbs += yDelim;
//			}
//			xAbs = xAbsStart;
//			yAbs = yAbsStart;
//			zAbs += zDelim;
//		}
		
	}


	


	
	

	@Override
	public Vector3i getSegmentPos() {
		return this.segmentPos;
	}
	public static final byte CHUNK16VERSION = 2;
	public boolean deserialize(DataInputStream dataInputStream, int size, boolean ignoreName, boolean reset, long time) throws IOException, DeserializationException {
		//		System.err.println("READING "+getSegmentController()+"; "+getSegmentController().getUniqueIdentifier()+"; "+pos);
		int version = 0;
		PushbackInputStream pb = new PushbackInputStream(dataInputStream, 2);
		byte[] signature = new byte[2];
		pb.read(signature); // read the signature
		pb.unread(signature); // push back the signature to the stream

		DataInputStream pbDataInputStream = new DataInputStream(pb);

		if (signature[0] == (byte) 0x1f && signature[1] == (byte) 0x8b) {
			/*
			 * check if matches standard gzip maguc number
			 */
			System.err.println("[SEGMENT] WARNING: Reading an old format segment! " + getSegmentController() + "; " + segmentPos);
			oldDeserialize(pbDataInputStream, size, ignoreName, time);
			return true;
		}
		if (signature[0] < 0) {
			//negative, that means the segment is versioned
			version = -pbDataInputStream.readByte();
		}
		dataInputStream = pbDataInputStream;

		long lastChanged = dataInputStream.readLong();

		int x = dataInputStream.readInt();
		int y = dataInputStream.readInt();
		int z = dataInputStream.readInt();

		if (ignoreName) {
			segmentPos.set(x, y, z);
		}

		assert (ignoreName || segmentPos.x == x && segmentPos.y == y && segmentPos.z == z) : " deserialized " + x + ", " + y + ", " + z + "; toSerialize " + segmentPos + " on " + getSegmentController();

		byte dataByte = dataInputStream.readByte();

		if (dataByte == RemoteSegment.DATA_AVAILABLE_BYTE) {
			
			
			final SegmentSerializationBuffersGZIP bm = SegmentSerializationBuffersGZIP.get();
			try {
				Inflater inflater = bm.inflater;
				byte[] buffer = bm.SEGMENT_BUFFER;
				byte[] byteFormatBuffer = bm.SEGMENT_BYTE_FORMAT_BUFFER;
				SegmentDataInterface dummy;
				boolean preExistingSegData = false;
				
	
	
				int inflatedSize = dataInputStream.readInt();
	
				if (getSegmentController() == null || getSegmentController().isOnServer()) {
					GameServerState.dataReceived += inflatedSize;
				} else {
					GameClientState.dataReceived += inflatedSize;
				}
				assert (inflatedSize <= buffer.length) : inflatedSize + "/" + buffer.length;
				int read = dataInputStream.read(buffer, 0, inflatedSize);

				//572; 583;
				if (read != inflatedSize) {
					throw new DeserializationException(read + "; " + inflatedSize + "; " + buffer.length + "; " + this + "; ");
				}
				inflater.reset();

				inflater.setInput(buffer, 0, inflatedSize);


				try {
					if (preExistingSegData && reset) {
						reset(time);
					}
					int inflate;
					if (version < CHUNK16VERSION) {
						dummy = bm.dummies.get(version);
						if(dummy == null){
							System.err.println("[INFLATER] no dummy for version "+version);
						}
						try{
							inflate = dummy.inflate(inflater, byteFormatBuffer);
						}catch(SegmentInflaterException e){
							if(getSegmentController() != null){
								System.err.println("[INFLATER] Exception: " + getSegmentController().getState() + " size received: " + inflatedSize + ": " + e.inflate + "/" + e.shouldBeInflate+ " for " + getSegmentController() + " pos " + segmentPos);
							}
							e.printStackTrace();
							inflate = 0;
						} catch (SegmentDataWriteException e) {
							e.printStackTrace();
							throw new RuntimeException("this should be never be thrown as migration should always be to"
									+ "a normal segment data", e);
						}
							
						while (version < CHUNK16VERSION) {

							//migrate to new format
							if (version < CHUNK16VERSION - 1) {
								SegmentDataInterface dummyNext = bm.dummies.get(version + 1);
//								System.err.println("MIGRATE from: "+dummy.getClass().getSimpleName()+" -> "+dummyNext.getClass().getSimpleName());
								dummy.migrateTo(version, dummyNext);
								try {
									dummy.resetFast();
								} catch (SegmentDataWriteException e) {
									pbDataInputStream.close();
									throw new RuntimeException(e);
								}
								dummy = dummyNext;
							} else {
								assert (version == CHUNK16VERSION - 1);
//								System.err.println("MIGRATE FINAL from: "+dummy.getClass().getSimpleName()+" -> "+this.getClass().getSimpleName());
								dummy.migrateTo(version, this);
								try{
									dummy.resetFast();
								} catch (SegmentDataWriteException e) {
									pbDataInputStream.close();
									throw new RuntimeException(e);
								}

							}

							version++;

						}
					} else {
						//normal: right version
						try {
							inflate = inflate(inflater, byteFormatBuffer);
						} catch (SegmentInflaterException e) {
							System.err.println("[INFLATER] Exception: " + getSegmentController().getState() + " size received: " + inflatedSize + ": " + e.inflate + "/" + e.shouldBeInflate+ " for " + getSegmentController() + " pos " + segmentPos);
							e.printStackTrace();
							inflate = 0;
						}

					}

					if (inflate == 0) {
						System.err.println("WARNING: INFLATED BYTES 0: " + inflater.needsInput() + " " + inflater.needsDictionary());
					}

				} catch (DataFormatException e) {
					e.printStackTrace();
				} 

			}finally {
				SegmentSerializationBuffersGZIP.free(bm);
			}

		} else {
			assert (dataByte == RemoteSegment.DATA_EMPTY_BYTE) : dataByte + "/" + RemoteSegment.DATA_EMPTY_BYTE + ": " + x + ", " + y + ", " + z + "; byte size: " + size;
			//Received empty segment
		}
		this.lastChanged = (lastChanged);

		return version < CHUNK16VERSION;

	}
	public void oldDeserialize(DataInputStream dataInputStream, int size, boolean ignoreName, long time) throws IOException, DeserializationException {
		DataInputStream zip;
		if (size < 0) {
			zip = new DataInputStream(new GZIPInputStream(dataInputStream));
		} else {
			zip = new DataInputStream(new GZIPInputStream(dataInputStream, size));
		}
		//				System.err.println("READING "+getSegmentController()+"; "+getSegmentController().getUniqueIdentifier()+"; "+pos);
		long lastChanged = zip.readLong();

		int x = zip.readInt();
		int y = zip.readInt();
		int z = zip.readInt();

		if (ignoreName) {
			segmentPos.set(x, y, z);
		}

		assert (ignoreName || segmentPos.x == x && segmentPos.y == y && segmentPos.z == z) : " deserialized " + x + ", " + y + ", " + z + "; toSerialize " + segmentPos;

		byte dataByte = zip.readByte();

		boolean needsReset = false;
		if (dataByte == RemoteSegment.DATA_AVAILABLE_BYTE) {
			deserialize(zip, time);

		} else {
			assert (dataByte == RemoteSegment.DATA_EMPTY_BYTE);
			//Received empty segment
		}
		this.lastChanged = (lastChanged);

		try {
			int read = zip.read();
			if (read != -1) {
				throw new DeserializationException("EoF not reached: " + read + " - size given: " + size);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new DeserializationException("[WARNING][DESERIALIZE] " + getSegmentController().getState() + ": " + getSegmentController() + ": " + segmentPos + ": " + e.getMessage());
		}

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
		
	}

	@Override
	public int readFrom(ByteBuffer uncompressed) {
		throw new UnsupportedOperationException("only top version chunks support this");
	}
}
