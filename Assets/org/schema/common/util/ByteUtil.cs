package org.schema.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.schema.common.FastMath;

public class ByteUtil {
	
	public static final boolean Chunk32 = true;
	
	public static final int SIZEOF_BYTE = 1;
	public static final int SIZEOF_SHORT = 2;
	public static final int SIZEOF_CHAR = 2;
	public static final int SIZEOF_INT = 4;
	public static final int SIZEOF_FLOAT = 4;
	public static final int SIZEOF_LONG = 8;
	public static final int SIZEOF_DOUBLE = 8;
	
	public final static int t = 1 << 10;
	public final static int t2 = 1 << 5;
	public static final int asd = 0x11;
	public static final int sd = 0x7F;
	/**
	 * <code>
	 * 4096
	 * | /2
	 * 2048
	 * | * 24 (vertices)
	 * 49152
	 * | * 8 (2 floats)     | * 12 (3 floats)
	 * 393216               589824
	 * (2000 meshes: 750MB) (2000 meshes: 1125MB)
	 * </code>
	 */
	static final float a = 4294967296f;
	static final float b = 16777216f;
	private static final int MASK = 0xff;

	/**
	 * convert boolean to byte array (of size 4)
	 *
	 * @param f
	 * @return
	 */
	public static byte[] booleanToByteArray(boolean f) {
		//FIXME putting booleans ins bytes is not exactly efficient
		byte[] b = new byte[ByteUtil.SIZEOF_BYTE];
		ByteBuffer.wrap(b).put(f ? (byte) 1 : (byte) 0);
		return b;

	}

	public static byte[] booleanToByteArray(boolean f, byte[] b) {
		//FIXME putting booleans ins bytes is not exactly efficient
		ByteBuffer.wrap(b).put(f ? (byte) 1 : (byte) 0);
		return b;
	}

	/**
	 * Convert the byte array to an int starting from the given offset.
	 *
	 * @param b      The byte array
	 * @param offset The array offset
	 * @return The integer
	 */
	public static int byteArrayToInt(byte[] b) {
		int result = 0;

		for (int i = 0; i < 4; i++) {
			result <<= 8;
			result ^= b[i] & 0xff;
		}

		return result;
	}

	public static long byteArrayToLong(byte[] bytes) {
		long l = 0;
		for (int i = 0; i < 8; i++) {
			l <<= 8;
			l ^= (long) bytes[i] & 0xff;
		}
		return l;
	}

	public static final int circularMod(int x, int m) {
		int r = x % m;
		return r < 0 ? r + m : r;
	}

	public static byte combineHexToByte(byte first, byte second) {
		byte a = 0;
		a |= second;
		a <<= 4;
		a |= first;
		return a;
	}

	public static byte[] concat(byte[] first, byte[] second) {
		byte[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	public static <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	public static long convertFourShortsToLong1(short b1, short b2, short b3, short b4) {
		return (long) (b4 & 0xFFFF) << 48 | (long) (b3 & 0xFFFF) << 32 | (long) (b2 & 0xFFFF) << 16 | (long) b1 & 0xFFFF;
	}
	public static final int divSeg(int i) {
		return Chunk32 ? div32(i) : div16(i);
	}
	public static final int divUSeg(int i) {
		return Chunk32 ? divU32(i) : divU16(i);
	}
	public static final int modUSeg(int i) {
		return Chunk32 ? modU32(i) : modU16(i);
	}
	
	public static final int div16(int i) {
		return i >= 0 ? (i >> 4) : -(-i >> 4);
	}
	public static final int divU16(int i) {
		return (i >> 4);
	}
	public static final int modU16(int i) {
		return (i & 0xF);
	}
	
	
	public static final int div32(int i) {
		return i >= 0 ? (i >> 5) : -(-i >> 5);
	}
	public static final int divU32(int i) {
		return (i >> 5);
	}
	public static final int modU32(int i) {
		return (i & 0x1F);
	}
	
	
	
	public static final int divU64(int i) {
		return (i >> 6);
	}

	public static final int divU128(int i) {
		return (i >> 7);
	}

	public static final int divU256(int i) {
		return (i >> 8);
	}

	

	public static final int modU128(int i) {
		return (i & 0x7F);
	}

	public static final int modU256(int i) {
		return (i & 0xFF);
	}

	public static final int modU18(int i) {
		//		return (int) ((Math.abs(i) % 18));
		return circularMod(i, 18);
	}

	public static final int modU8(int i) {
		return (i & 0x7);
	}

	public static final int divU8(int i) {
		return (i >> 3);
	}

	public static int encodeBytesToInt(int x, int y, int z, int a) {
		int vertexCoded = 0;
		vertexCoded = ((vertexCoded) | x);
		vertexCoded = ((vertexCoded << 8) | y);
		vertexCoded = ((vertexCoded << 8) | z);
		vertexCoded = ((vertexCoded << 8) | a);
		return vertexCoded;
	}
	public static long encodeToLong(int x, int y){
		long l = (((long)x) << 32) | (y & 0xffffffffL);
		return l;
	}
	public static int extractInt(int value, int begin, int end, Object lock) {
		int mask = ((1 << (end - begin)) - 1);
		return ((value >> begin) & mask);
	}

	public static short extractShort(short value, int begin, int end, Object lock) {
		short mask = (short) (((short) 1 << (end - begin)) - (short) 1);
		return (short) ((value >> begin) & mask);
	}

	/**
	 * convert float to byte array (of size 4)
	 *
	 * @param f
	 * @return
	 */
	public static byte[] floatToByteArray(float f) {
		byte[] b = new byte[ByteUtil.SIZEOF_FLOAT];
		ByteBuffer.wrap(b).putFloat(f);
		return b;
	}

	public static byte[] floatToByteArray(float f, byte[] b) {
		ByteBuffer.wrap(b).putFloat(f);
		return b;
	}

	public static int[] getBytesFromEncodedInt(int from, int dest[]) {
		dest[0] = ((from) & 0xFF);
		dest[1] = ((from >> 8) & 0xFF);
		dest[2] = ((from >> 16) & 0xFF);
		dest[3] = ((from >> 24) & 0xFF);
		return dest;
	}

	public static float getCodeS(int normalMode, int overlay, byte occlusion) {
		return getCodeS(normalMode, overlay, occlusion, 0, 0);
	}
	public static float getCodeF(byte sideId, byte layer, short type, byte hitPoints, byte animated, byte tex, boolean onlyInBuildMode) {
		return getCodeF(sideId, layer, type, hitPoints, animated, tex, (byte)0, onlyInBuildMode);
	}
	public static float getCodeS(int normalMode, int overlay, byte occlusion, int elevat, int elevatV) {
		assert(elevat >= 0 && elevat < 4);
		assert(elevatV >= 0 && elevatV < 4);
		
		
		float normalModeSAC = normalMode;  // 9 bit (512 max)
		float overlayC = overlay << 9;  // 6 bit (64 max)
		float occC = occlusion << 15;  // 4 bit (16 max)
		float elevSAC = elevat << 20;  // 2 bit (4 max)
		float elevVSAC = elevatV << 22;  // 2 bit (4 max)
		return normalModeSAC + overlayC + occC + elevSAC + elevVSAC;
	}
	public static int getCodeSI(int normalMode, int overlay, byte occlusion, byte lDirX, byte lDirY, byte lDirZ, int elevat, int elevatV) {
		
		int normalModeSAC = normalMode;  // 9 bit (512 max)
		int overlayC = overlay << 9;  // 6 bit (64 max)
		int occC = occlusion << 15;  // 4 bit (16 max)
		
//		int lDirXC = lDirX << 20;
//		int lDirYC = lDirY << 24;
//		int lDirZC = lDirZ << 28;
		int elevSAC = elevat << 20;  // 2 bit (4 max)
		int elevVSAC = elevatV << 22;  // 2 bit (4 max)
		return normalModeSAC + overlayC + occC + elevSAC + elevVSAC;
	}

	
	public static int getCodeIndexI(int index, byte lightR, byte lightG, byte lightB) {
		
		int indexCode = index; //max Index = 32k -> 16 bit
		int lightRCode = (lightR) << 16;  //32 -> 5 bit
		int lightGCode = (lightG) << 21;  //32 -> 5 bit
		int lightBCode = (lightB) << 26;  //32 -> 5 bit
		
		
		int code = indexCode + lightRCode + lightGCode + lightBCode; //total of 24 bit encoded
		return code;
	}

	public static float getCodeF(byte sideId, byte layer, short type, byte hitPoints, byte animated, byte tex, byte xyManip, boolean onlyInBuildMode) {
		
		//2 BITS FREEE HERE!!!!!
		float sideCodeC = sideId << 2;  // 3 bit -> 4, 8
		float layerC = layer << 5;  // 3 bit -> 32, 64, 128, 256,
		float xyManipC = xyManip << 8;  // 1 bit
		float typeC = type << 9;  // 8 bit -> 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536,
		float hitPointsC = hitPoints << 17; // 3 bit -> 131072, 262144, 524288,
		float animatedC = animated << 20; // 1 bit -> 1048576,
		float texC = tex << 21; // 1 bit -> 2097152, 4194304, 8388608
		float onlyInBuildModeE = onlyInBuildMode ? (1 << 23) : 0; // 1 bit -> 2097152, 4194304, 8388608
//		float mirrorC 		= mirror    		<< 22; // 3 bit -> 2097152, 4194304, 8388608
		//, 16777216
		//total of 24 bit encoded
		float com = sideCodeC + layerC + typeC + hitPointsC + animatedC + texC + xyManipC + onlyInBuildModeE;
		return com;
	}
	public static int getCodeI(byte sideId, byte layer, short type, byte hitPoints, byte animated, byte tex, byte xyManip, boolean onlyInBuildMode, boolean extendedBlockTexture) {
		
		//2 BITS FREEE HERE!!!!!
		int sideCodeC = sideId << 2;  // 3 bit -> 4, 8
		int layerC = layer << 5;  // 3 bit -> 32, 64, 128, 256,
		int xyManipC = xyManip << 8;  // 1 bit
		int typeC = type << 9;  // 8 bit -> 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536,
		int hitPointsC = hitPoints << 17; // 3 bit -> 131072, 262144, 524288,
		int animatedC = animated << 20; // 1 bit -> 1048576,
		int texC = tex << 21; // 1 bit -> 2097152, 4194304, 8388608
		int onlyInBuildModeE = onlyInBuildMode ? (1 << 23) : 0; // 1 bit -> 2097152, 4194304, 8388608
		int extendedBlockTextureE = extendedBlockTexture ? (1 << 24) : 0;
//		int mirrorC 		= mirror    		<< 22; // 3 bit -> 2097152, 4194304, 8388608
		//, 16777216
		//total of 24 bit encoded
		int com = sideCodeC + layerC + typeC + hitPointsC + animatedC + texC + xyManipC + onlyInBuildModeE + extendedBlockTextureE;
		return com;
	}

	public static float getCodeF2() {
		return 0;
	}

	public static float getCodeIndexF(float index, byte lightR, byte lightG, byte lightB) {
		
		if(Chunk32){
			float indexCode = index; //max Index = 32k -> 16 bit
			float lightRCode = (lightR/8) << 16;  //4 -> 2 bit
			float lightGCode = (lightG/8) << 18;  //4 -> 2 bit
			float lightBCode = (lightB/8) << 20;  //4 -> 2 bit
			float code = indexCode + lightRCode + lightGCode + lightBCode; //total of 24 bit encoded
			return code;
		}else{
			throw new IllegalArgumentException();
		}
	}
	public static byte getHex(byte[] lightData, int index, int subIndex) {
		byte value = retrieveHexFromByte(subIndex % 2 == 0, lightData[index + subIndex / 2]);
		assert (value >= 0 && value <= 16);
		return value;
	}

	public static short[] getShortsFromEncodedLong(long from, short dest[]) {
		dest[0] = (short) ((from) & 0xFFFF);
		dest[1] = (short) ((from >> 16) & 0xFFFF);
		dest[2] = (short) ((from >> 32) & 0xFFFF);
		dest[3] = (short) ((from >> 48) & 0xFFFF);
		return dest;
	}

	public static int intRead3ByteArray(byte[] to, int offsetIndex) {
		int result = 0;
		for (int i = 0; i < 3; i++) {
			result <<= 8;
			result ^= to[offsetIndex + i] & 0xff;
		}

		return result;
	}
	public static int intRead2ByteArray(byte[] to, int offsetIndex) {
		int result = 0;
		for (int i = 0; i < 2; i++) {
			result <<= 8;
			result ^= to[offsetIndex + i] & 0xff;
		}
		
		return result;
	}

	public static int intReadByteArray(byte[] to, int offsetIndex) {
		int result = 0;

		for (int i = 0; i < 4; i++) {
			result <<= 8;
			result ^= to[offsetIndex + i] & 0xff;
		}

		return result;
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

	public static byte[] intToByteArray(int param, byte[] result) {
		for (int i = 0; i < 4; i++) {
			int offset = (result.length - 1 - i) * 8;
			result[i] = (byte) ((param >>> offset) & MASK);
		}
		return result;
	}

	public static void intWrite3ByteArray(int value, byte[] to, int offsetIndex, Object lock) {
		to[offsetIndex + 0] = (byte) (value >>> 16);
		to[offsetIndex + 1] = (byte) (value >>> 8);
		to[offsetIndex + 2] = (byte) (value);
	}
	public static void intWrite2ByteArray(int value, byte[] to, int offsetIndex, Object lock) {
		to[offsetIndex + 0] = (byte) (value >>> 8);
		to[offsetIndex + 1] = (byte) (value);
	}

	public static void intWriteByteArray(int value, byte[] to, int offsetIndex, Object lock) {
		to[offsetIndex + 0] = (byte) (value >>> 24);
		to[offsetIndex + 1] = (byte) (value >>> 16);
		to[offsetIndex + 2] = (byte) (value >>> 8);
		to[offsetIndex + 3] = (byte) (value);
	}

	/**
	 * convert long to byte array (of size 4)
	 *
	 * @param f
	 * @return
	 */
	public static byte[] longToByteArray(long f) {
		byte[] b = new byte[ByteUtil.SIZEOF_LONG];
		ByteBuffer.wrap(b).putLong(f);
		//		System.err.println("writing "+Arrays.toString(b)+" "+f);
		return b;
	}

	public static byte[] longToByteArray(long f, byte[] b) {
		ByteBuffer.wrap(b).putLong(f);
		return b;
	}

	public static void main(String[] srgs) {
		int a = 10;
		
		float x = 2f / a;
		
		System.err.println(x);
	}

	public static void putRangedBits3OntoInt(byte[] to, int value, int begin, int end, int offsetIndex, Object lock) {
		intWrite3ByteArray(putRangedBitsOntoInt(intRead3ByteArray(to, offsetIndex), value, begin, end, lock), to, offsetIndex, lock);
	}

	public static void putRangedBitsOntoInt(byte[] to, int value, int begin, int end, int offsetIndex, Object lock) {
		intWriteByteArray(putRangedBitsOntoInt(intReadByteArray(to, offsetIndex), value, begin, end, lock), to, offsetIndex, lock);
	}

	public static int putRangedBitsOntoInt(int input, int value, int begin, int end, Object lock) {

		int mask = ((1 << (end - begin)) - 1) << begin;
		int r = (input & ~mask) | ((value << begin) & mask);
		return r;

	}

	public static void putRangedBitsOntoShort(byte[] to, byte value, int begin, int end, int offsetIndex, Object lock) {
		shortWriteByteArray(putRangedBitsOntoShort(shortReadByteArray(to, offsetIndex), value, begin, end, lock), to, offsetIndex);
	}

	public static short putRangedBitsOntoShort(short value, byte b, int begin, int end, Object lock) {
		//first subtract old value, or the biswise or will
		//screw up the bits
		synchronized (lock) {
			short extract = extractShort(value, begin, end, lock);
			short extractedShort = putRangedBitsOntoShort2((short) 0, (byte) extract, begin, end);
			value -= extractedShort;

			short n = (short) (b & MASK);
			assert (n < (short) (((short) 1 << (end - begin)))) : n + " out of range for " + (end - begin) + "bits";
			n = (short) (n << begin);

			return (short) (value | n);
		}
	}

	public static short putRangedBitsOntoShort2(short value, byte b, int begin, int end) {

		//		System.err.println("3AFTER  "+Integer.toBinaryString(value));

		short n = (short) (b & MASK);
		assert (n < (short) (((short) 1 << (end - begin)))) : n + " out of range for " + (end - begin) + "bits";
		n = (short) (n << begin);

		return (short) (value | n);

	}

	public static int readInt(InputStream inputStream) throws IOException {
		int result = 0;

		for (int i = 0; i < 4; i++) {
			result <<= 8;
			int b = 0;
			while ((b = inputStream.read()) == -1) {
				//        		System.err.println("NO INPUT");
				//        		throw new IOException("END OF STREAM");
			}
			result ^= b & 0xff;
		}

		return result;
	}

	public static long readLong(InputStream s) throws IOException {

		long l = 0;
		for (int i = 0; i < 8; i++) {
			l <<= 8;
			int b = 0;
			while ((b = s.read()) == -1) {
			}
			l ^= (long) b & 0xff;
		}
		return l;
	}

	public static short readShort(InputStream inputStream) throws IOException {
		short result = 0;

		for (int i = 0; i < 2; i++) {
			result <<= 8;
			int b = 0;
			while ((b = inputStream.read()) == -1) {
				//         		System.err.println("NO INPUT");
				//         		throw new IOException("END OF STREAM");
			}
			result ^= b & 0xff;
		}

		return result;
	}

	public static byte readShortByteArray6First(byte[] to, int offsetIndex, int shift) {
		short result = shortReadByteArray(to, offsetIndex);
		byte dH = (byte) ((result >> shift) & 0xbf);
		return dH;
	}

	public static byte retrieveHexFromByte(boolean first, byte val) {
		if (!first) {
			val >>= 4;
		}
		return (byte) (val & 0xf);
	}

	public static short shortReadByteArray(byte[] to, int offsetIndex) {
		short result = 0;

		for (int i = 0; i < 2; i++) {
			result <<= 8;
			result ^= to[offsetIndex + i] & 0xff;
		}

		return result;
	}

	/**
	 * convert int to byte array (of size 4)
	 *
	 * @param param
	 * @return
	 */
	public static byte[] shortToByteArray(short param) {
		byte[] result = new byte[2];
		for (int i = 0; i < 2; i++) {
			int offset = (result.length - 1 - i) * 8;
			result[i] = (byte) ((param >>> offset) & MASK);
		}
		return result;
	}

	public static byte[] shortToByteArray(short param, byte[] result) {
		for (int i = 0; i < 2; i++) {
			int offset = (result.length - 1 - i) * 8;
			result[i] = (byte) ((param >>> offset) & MASK);
		}
		return result;
	}

	public static void shortWriteByteArray(short param, byte[] to, int offsetIndex) {

		for (int i = 0; i < 2; i++) {
			int offset = (1 - i) * 8;
			to[offsetIndex + i] = (byte) ((param >>> offset) & MASK);
		}
	}

	/**
	 * convert String to byte array (of size 4)
	 *
	 * @param f
	 * @return
	 */
	public static byte[] stringToByteArray(String f) {
		return f.getBytes();
	}

	public static void testCubeCode() {
		// texCode(2 bit) + light(4 bit)
		//+ layer(4 bit) + type(6 bit)
		//+ orientation (3 bit) +  hit points (4 bit)
		//+ animated (1 bit)
		// = 24 bit = 3 byte ;)

		byte texCode = 2;
		byte light = 15;
		byte layer = 14;
		byte type = 63;
		byte hitPoints = 13;
		byte animated = 1;

		float f = Float.MAX_VALUE;
		int i = Integer.MAX_VALUE;
		float texCodeC = texCode; // 1, 2,
		float lightC = light * 4;//8, 16, 32, 64
		float layerC = layer * 128;//256,512,1024,2048
		float typeC = type * 4096;//8192, 16384, 32768, 65536, 131072, 262144
		float hitPointsC = hitPoints * 524288; //1048576, 2097152, 4194304, 8388608
		float animatedC = animated * 16777216;

		float com = texCodeC + lightC + layerC + typeC + hitPointsC + animatedC;

		System.err.println("COM " + com + " / " + Float.MAX_VALUE + " --- " + (com < Float.MAX_VALUE));
		//				 -1.56502208E8
		//				 -1.56502208E8

		byte animatedE = (byte) Math.floor(com / 16777216);
		com -= animatedE * 16777216;

		byte hitPointsE = (byte) Math.floor(com / 524288);
		com -= hitPointsE * 524288;

		byte typeE = (byte) FastMath.fastFloor(com / 4096);
		com -= typeE * 4096;

		byte layerE = (byte) Math.floor(com / 128);
		com -= layerE * 128;
		System.err.println("COM " + com);
		byte lightE = (byte) (Math.floor(com / 4));
		com -= lightE * 4;

		byte texCodeE = (byte) Math.floor(com);

		System.err.println("texCodeE: " + texCodeE + "/" + texCode +
				"\nlightE: " + lightE + "/" + light +
				"\nlayerE: " + layerE + "/" + layer +
				"\ntypeE: " + typeE + "/" + type +
				"\nhitPointsE: " + hitPointsE + "/" + hitPoints +
				"\nanimatedE: " + animatedE + "/" + animated);
	}

	public static void testGetNeighboringSegment() {

		for (int inOut = Integer.MIN_VALUE; inOut < Integer.MAX_VALUE; inOut++) {

			int x = (inOut < 0 ? -1 : 0);
			x += inOut / 16;

			int out = (x * 16);
			x = FastMath.cyclicModulo(inOut, 16);

			int b = ByteUtil.divUSeg((inOut < 0 ? inOut - 1 : inOut));
			int c = (b * 16);
			b = (byte) ByteUtil.modUSeg(x);

			assert (x == b && out == c) : inOut + ": " + x + " --> " + b + "     " + out + " --> " + c;
		}
	}

	public static void testHexToByte() {
		for (int x = 0; x < 16; x++) {
			for (int y = 0; y < 16; y++) {
				byte b = combineHexToByte((byte) x, (byte) y);

				System.err.println("1: " + retrieveHexFromByte(true, b));
				System.err.println("2: " + retrieveHexFromByte(false, b));
				assert (retrieveHexFromByte(true, b) == x);
				assert (retrieveHexFromByte(false, b) == y);
			}
		}
	}

	public static void testIntByteCode() {
	    /*
         * 2048 = 11 bit -> types
		 * 1024 = 10 bit -> hp
		 *    8 =  3 bit -> orientation
		 * -------------------------------
		 *        24 bit = 3 byte
		 */
		byte[] b = new byte[3];
		int testType = 2047;
		int testHp = 1023;
		int testOr = 5;
		intWrite3ByteArray(20138, b, 0, new Object());
		System.err.println("TEST: " + (20138 == intRead3ByteArray(b, 0)) + ": " + intRead3ByteArray(b, 0));
		byte[] b4 = new byte[4];
		putRangedBitsOntoInt(b4, testType, 0, 11, 0, new Object());
		putRangedBitsOntoInt(b4, testHp, 11, 21, 0, new Object());
		putRangedBitsOntoInt(b4, testOr, 21, 24, 0, new Object());
		System.err.println("4B IS NOW: " + Arrays.toString(b4) + ";");

		putRangedBits3OntoInt(b, testType, 0, 11, 0, new Object());
		putRangedBits3OntoInt(b, testHp, 11, 21, 0, new Object());
		putRangedBits3OntoInt(b, testOr, 21, 24, 0, new Object());

		//		intWriteByteArray(testType, b, 0, new Object());

		System.err.println("ORIG BITS: " + Integer.toBinaryString(testType));
		//		for(int i = 0; i < b.length; i++){

		//		}
		System.err.println("B IS NOW: " + Arrays.toString(b) + ";");
		System.err.println("byte array: " + intRead3ByteArray(b, 0));
		System.err.println("TYPE: " + testType + " -> " + extractInt(intRead3ByteArray(b, 0), 0, 11, new Object()));
		System.err.println("HP  : " + testHp + " -> " + extractInt(intRead3ByteArray(b, 0), 11, 21, new Object()));
		System.err.println("ORIE: " + testOr + " -> " + extractInt(intRead3ByteArray(b, 0), 21, 24, new Object()));
		byte tb[] = new byte[3 * 2048 * 1024 * 8];
		int i = 0;
		Object o = new Object();
		for (int type = 0; type < 2048; type++) {
			for (int hp = 0; hp < 1024; hp++) {
				for (int orientation = 0; orientation < 6; orientation++) {
					putRangedBits3OntoInt(b, type, 0, 11, 0, o);
					putRangedBits3OntoInt(b, hp, 11, 21, 0, o);
					putRangedBits3OntoInt(b, orientation, 21, 24, 0, o);

					assert (extractInt(intRead3ByteArray(b, 0), 0, 11, o) == type) : extractInt(intRead3ByteArray(b, 0), 0, 11, o) + " != " + type;
					assert (extractInt(intRead3ByteArray(b, 0), 11, 21, o) == hp) : extractInt(intRead3ByteArray(b, 0), 11, 21, o) + " != " + hp;
					assert (extractInt(intRead3ByteArray(b, 0), 21, 24, o) == orientation);

					putRangedBits3OntoInt(b, orientation, 21, 24, 0, o);
					putRangedBits3OntoInt(b, hp, 11, 21, 0, o);
					putRangedBits3OntoInt(b, type, 0, 11, 0, o);

					assert (extractInt(intRead3ByteArray(b, 0), 0, 11, o) == type) : extractInt(intRead3ByteArray(b, 0), 0, 11, o) + " != " + type;
					assert (extractInt(intRead3ByteArray(b, 0), 11, 21, o) == hp) : extractInt(intRead3ByteArray(b, 0), 11, 21, o) + " != " + hp;
					assert (extractInt(intRead3ByteArray(b, 0), 21, 24, o) == orientation);

					//					for(int j = 0; j < 4; j++){
					//						tb[j] = 3;
					//					}
					i++;
				}
			}
		}
	}

	public static void writeFloat(Float float1, OutputStream buffer) throws IOException {
		buffer.write(floatToByteArray(float1));

	}

	public static void writeHex(byte[] lightData, int index, int subIndex,
	                            byte value) {
		byte b = lightData[index + subIndex / 2];
		boolean first = subIndex % 2 == 0;
		byte otherVal = retrieveHexFromByte(!first, b);

		assert (value >= 0 && value <= 16);
		if (first) {
			lightData[index + subIndex / 2] = combineHexToByte(value, otherVal);
		} else {
			lightData[index + subIndex / 2] = combineHexToByte(otherVal, value);
		}
	}

	public static void writeInt(int param, OutputStream outputStream) throws IOException {
		for (int i = 0; i < 4; i++) {
			int offset = (ByteUtil.SIZEOF_INT - 1 - i) * 8;
			outputStream.write((byte) ((param >>> offset) & MASK));
		}
	}

	public static void writeLong(long l, OutputStream outputStream) throws IOException {
		outputStream.write(longToByteArray(l));
	}
}