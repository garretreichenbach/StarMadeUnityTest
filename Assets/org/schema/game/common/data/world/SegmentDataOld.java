package org.schema.game.common.data.world;

import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.client.view.cubes.shapes.spike.SpikeIcon;
import org.schema.game.client.view.cubes.shapes.spike.topbottom.*;
import org.schema.game.client.view.cubes.shapes.sprite.SpriteBottom;
import org.schema.game.client.view.cubes.shapes.wedge.*;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.octree.ArrayOctree;
import org.schema.schine.network.StateInterface;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class SegmentDataOld implements SegmentDataInterface {
	public static final byte ANTI_BYTE = -16; // -> 0xf0 -> 11110000;
	public static final int lightBlockSize = 39;

	// 0 - 5 Ambient
	// 6 - 11 Ambient
	// 12 - 14 Gather
	// 15 - 16 = type
	// 17	   = vis
	public static final int typeIndexStart = 0;
	public static final int typeIndexEnd = 11; //11 bits -> 2048 (0 incl)
	public static final int hitpointsIndexStart = 11; //9 bits -> 512
	public static final int hitpointsIndexEnd = 20;
	public static final int activeIndexStart = 20; //1 bit -> 1
	public static final int activeIndexEnd = 21;
	public static final int orientationStart = 21; //3 bit -> 8
	public static final int orientationEnd = 24;
	public static final int blockSize = 3;
	public static final byte ACTIVE_BIT = 16;
	public static final byte[] typeMap = new byte[(4096 * 2) * 3];
	public static final int maxHp = 512;
	public static final byte[] hpMap = new byte[maxHp * 3];
	public static final int maxOrient = 8;
	public static final byte[] orientMap = new byte[maxOrient * 3];
	public static final int vis = 12;
	public static final int SEG = 16;
	public static final int BLOCK_COUNT = SEG * SEG * SEG;
	public static final int TOTAL_SIZE = BLOCK_COUNT * blockSize;
	public static final int TOTAL_SIZE_LIGHT = BLOCK_COUNT * lightBlockSize;
	public static final int SEG_TIMES_SEG_TIMES_SEG = 16 * 16 * 16;
	public static final int SEG_TIMES_SEG = 16 * 16;
	
	public static final int SEG_MINUS_ONE = SEG - 1;
	public static final int t = 255;
	public static final int PIECE_ADDED = 0;
	public static final int PIECE_REMOVED = 1;
	public static final int PIECE_CHANGED = 2;
	public static final int PIECE_UNCHANGED = 3;
	public static final int PIECE_ACTIVE_CHANGED = 4;
	private static final int MASK = 0xff;
	public static BlockShapeAlgorithm[][] algorithmsOld = new
			BlockShapeAlgorithm[][]{
			{

					new WedgeTopFront(), new WedgeTopRight(), new WedgeTopBack(), new WedgeTopLeft(),
					new WedgeBottomFront(), new WedgeBottomRight(), new WedgeBottomBack(), new WedgeBottomLeft(),
					new WedgeLeftFront(), new WedgeLeftRight(), new WedgeLeftBack(), new WedgeLeftLeft(),

					new WedgeLeftFront(), new WedgeLeftRight(), new WedgeLeftBack(), new WedgeLeftLeft(),
					new WedgeTopFront(), new WedgeTopRight(), new WedgeTopBack(), new WedgeTopLeft(),
					new WedgeBottomFront(), new WedgeBottomRight(), new WedgeBottomBack(), new WedgeBottomLeft(),

					new WedgeIcon()
			},
			{
					new SpikeTopFrontRight(), new SpikeTopBackRight(), new SpikeTopBackLeft(), new SpikeTopFrontLeft(),
					new SpikeBottomFrontRight(), new SpikeBottomBackRight(), new SpikeBottomBackLeft(), new SpikeBottomFrontLeft(),

					new SpikeTopFrontRight(), new SpikeTopBackRight(), new SpikeTopBackLeft(), new SpikeTopFrontLeft(),
					new SpikeBottomFrontRight(), new SpikeBottomBackRight(), new SpikeBottomBackLeft(), new SpikeBottomFrontLeft(),

					new SpikeTopFrontRight(), new SpikeTopBackRight(), new SpikeTopBackLeft(), new SpikeTopFrontLeft(),
					new SpikeBottomFrontRight(), new SpikeBottomBackRight(), new SpikeBottomBackLeft(), new SpikeBottomFrontLeft(),

					//			new SpikeRightFrontRight(), new SpikeRightBackRight(), new SpikeRightBackLeft(), new SpikeRightFrontLeft(),
					//			new SpikeLeftFrontRight(), new SpikeLeftBackRight(), new SpikeLeftBackLeft(), new SpikeLeftFrontLeft(),

					new SpikeIcon(),
			},

			{new SpriteBottom(), new SpriteBottom(), new SpriteBottom(), new SpriteBottom(),
					new SpriteBottom(), new SpriteBottom(), new SpriteBottom(), new SpriteBottom(),
					new SpriteBottom(), new SpriteBottom(), new SpriteBottom(), new SpriteBottom(),
					new SpriteBottom(), new SpriteBottom(), new SpriteBottom(), new SpriteBottom(),
					new SpriteBottom(), new SpriteBottom(), new SpriteBottom(), new SpriteBottom(),
					new SpriteBottom(), new SpriteBottom(), new SpriteBottom(), new SpriteBottom(),
					new SpriteBottom()
			}

	};

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

	static {
		int orient = maxOrient;
		for (short s = 0; s < orient; s++) {
			int index = s * 3;
			int intRead3ByteArray = ByteUtil.intRead3ByteArray(orientMap, index);
			int putRangedBitsOntoInt = ByteUtil.putRangedBitsOntoInt(intRead3ByteArray, s, orientationStart, orientationEnd, null);
			ByteUtil.intWrite3ByteArray(putRangedBitsOntoInt, orientMap, index, null);
		}
	}

	private final Vector3b min = new Vector3b();
	private final Vector3b max = new Vector3b();
	private final ArrayOctree octree;
	private final Vector3b helperPos = new Vector3b();
	private Segment segment;
	private byte[] data;
	private int size;
	private boolean preserveControl;
	private boolean revalidating;
	public SegmentDataOld(boolean onClient) {
		octree = new ArrayOctree(!onClient);
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

	public static int getLightInfoIndexFromIndex(int dataIndex) {
		return (dataIndex / blockSize) * 3 * 24;
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
			data[0] &= (byte) (~(byte) 16);
		} else {
			data[0] |= ACTIVE_BIT;
		}
		assert (!active == ((data[0] & ACTIVE_BIT) == 0)) : data[0] + "; " + active;
		active = false;
		if (!active) {
			data[0] &= (byte) (~(byte) 16);
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
		for (short i = 0; i < 512; i++) {

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
			data[index] &= (byte) (~(byte) 16);
		}
		//		ByteUtil.putRangedBits3OntoInt(data, active ? 0 : 1, activeIndexStart, activeIndexEnd, index, this);
	}

	public static void setHitpoints(int index, short value, byte[] data) {
		assert (value >= 0 && value < 512);
		//		ByteUtil.putRangedBits3OntoInt(data, value, hitpointsIndexStart, hitpointsIndexEnd, index, this);

		int tIndex = value * 3;
		data[index] &= ~hpMap[hpMap.length - 3];
		data[index + 1] &= ~hpMap[hpMap.length - 2];

		data[index] |= hpMap[tIndex];
		data[index + 1] |= hpMap[tIndex + 1];

	}

	public static void setOrientation(int index, byte value, byte[] data) {
		assert (value >= 0 && value < 8) : "NOT A SIDE INDEX";
		value = Element.orientationMapping[value];

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
			System.err.println("ERROR: Type is invalied. must be < 4096 but was: " + newType);
		}
		//		ByteUtil.putRangedBits3OntoInt(data, newType, typeIndexStart, typeIndexEnd, index, this);
	}

	public static boolean valid(final byte x, final byte y, final byte z) {
		return (((x | y | z) & ANTI_BYTE) == 0);
		//		return ((x & ANTI_BYTE) == 0) && ((y & ANTI_BYTE) == 0) && ((z & ANTI_BYTE) == 0);
		//		return x < SEG && y < SEG && z < SEG && x >= 0 && y >= 0 && z >= 0;
	}

	public static boolean allNeighborsInside(final byte x, final byte y, final byte z) {
		return x < SEG_MINUS_ONE && y < SEG_MINUS_ONE && z < SEG_MINUS_ONE && x >= 1 && y >= 1 && z >= 1;
	}

	public static int getInfoIndex(byte x, byte y, byte z) {
		//		assert(valid(x, y, z)): x+", "+y+", "+z+": "+Segment.getDim()+";";

		int i = blockSize * ((z * SEG_TIMES_SEG) + (y * SEG) + x);

		return i;
	}

	public static int getInfoIndex(Vector3b pos) {
		return getInfoIndex(pos.x, pos.y, pos.z);
	}

	public static BlockShapeAlgorithm getAlgo(int blockStyle, byte orientationA, boolean active) {
		return algorithmsOld[blockStyle - 1][orientationA + (active ? 0 : 8)];
	}

	/**
	 * be cautious using that method, because it will NOT update any adding
	 * or removing of segments. that has to be done extra!
	 *
	 * @param pos
	 * @param pieceData
	 * @return true if an element was added or removed
	 */
	public int applySegmentData(Vector3b pos, byte[] pieceData, long time) {
		return applySegmentData(pos.x, pos.y, pos.z, pieceData, time);
	}

	/**
	 * be cautious using that method, because it will NOT update any adding
	 * or removing of segments. that has to be done extra!
	 *
	 * @param idServer
	 * @param pieceData
	 * @return true if an element was added or removed
	 */
	public int applySegmentData(byte x, byte y, byte z, byte[] pieceData, long time) {
		//FIXME warning: experimentially took out synchronized
		synchronized (this) {
			int index = getInfoIndex(x, y, z);
			int c = 0;
			boolean oldActive = isActive(index);
			short oldType = getType(index);
			byte oldOrientation = getOrientation(index);

			for (int i = index; i < index + blockSize; i++) {
				data[i] = pieceData[c++];
			}
			short newType = getType(index);
			short newHP = getHitpointsByte(index);

			if (newType != oldType) {

				if (oldType == Element.TYPE_NONE && newType != Element.TYPE_NONE) {
					onAddingElement(index, x, y, z, newType, true, time);
					return PIECE_ADDED;
				}
				if (oldType != Element.TYPE_NONE && newType == Element.TYPE_NONE) {
					onRemovingElement(index, x, y, z, oldOrientation, oldType, true, time);
					return PIECE_REMOVED;
				}
				return PIECE_CHANGED;
			} else {
				short oldHP = getHitpointsByte(index);
				if (oldHP != newHP) {
					return PIECE_CHANGED;
				} else {
					if (oldActive != isActive(index)) {
						return PIECE_ACTIVE_CHANGED;
					}
					return PIECE_UNCHANGED;
				}

			}

		}
	}

	public int arraySize() {
		return data.length;
	}

	public boolean checkEmpty() {
		for (int i = 0; i < data.length; i += blockSize) {
			if (getType(i) != Element.TYPE_NONE) {
				return false;
			}
		}

		return true;
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

	public boolean containsFast(int index) {
		int first = data[index + 2] & 0xff;
		int snd = data[index + 1] & 0xff;
		return first != 0 || ((snd & 7) != 0);
	}

	public boolean containsFast(Vector3b pos) {
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

	

	public void deserialize(DataInput inputStream, long time) throws IOException {
		synchronized (this) {
			reset(time);
			inputStream.readFully(data);
			setNeedsRevalidate(true);
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

		for (int i = 0; i < BLOCK_COUNT; i++) {
			int index = i * blockSize;

			short type = getType(index);
			if (type == Element.TYPE_NONE) {
				continue;
			}
			short hp = getHitpointsByte(index);
			boolean active = isActive(index);
			byte orientation = getOrientation(index);
			if (!ElementKeyMap.isValidType(type)) {
				System.err.println("ERROR: cannot migrate single block with type: " + type + ": block type is unknown in this blockConfig");
				continue;
			}
			ElementInformation info = ElementKeyMap.getInfo(type);
			boolean found = false;

			if (!info.isOrientatable() && info.getBlockStyle() == BlockStyle.NORMAL) {
				orientation = (byte) info.getDefaultOrientation();
				found = true;
			}

			if (info.getId() == ElementKeyMap.GRAVITY_ID) {
				orientation = Element.BOTTOM;
				found = true;
			}
			if (info.getBlockStyle() != BlockStyle.NORMAL) {

				if (info.getBlockStyle() == BlockStyle.SPRITE) {
					//replace all sprites with top orientation
					orientation = Element.TOP;
					active = true;
					found = true;
				} else {
					try{
						BlockShapeAlgorithm algo = getAlgo(info.getBlockStyle().id, orientation, active);
	
						//find first algortithm to eliminate duplicates to be able reorder
						for (int j = 0; j < algorithmsOld[info.getBlockStyle().id - 1].length; j++) {
							BlockShapeAlgorithm foundBlAlgo = algorithmsOld[info.getBlockStyle().id - 1][j];
							if (foundBlAlgo.getClass().equals(algo.getClass())) {
								found = true;
								orientation = (byte) j;
								break;
							}
						}
						assert (orientation < 24);
	
						if (orientation > 15) {
							//set active bit
							active = false;
							orientation -= 16;
	
						} else {
							active = true;
						}
					}catch(Exception r){
						r.printStackTrace();
					}

				}
				assert (found);
			}

			try {
				segmentData.setType(index, type);
			
				segmentData.setHitpointsByte(index, (short) Math.min(255, hp));
				segmentData.setActive(index, active);
				if (found) {
					//orientation has been normalized
					segmentData.setOrientation(index, orientation);
				} else {
					segmentData.setOrientation(index, orientation);
				}
			} catch (SegmentDataWriteException e) {
				e.printStackTrace();
				throw new RuntimeException("this should be never be thrown as migration should always be to"
						+ "a normal segment data", e);
			}
			assert (type == segmentData.getType(index));
			assert (segmentData.getHitpointsByte(index) == (hp > 255 ? 255 : hp));
			assert (active == segmentData.isActive(index));

			assert (info.getBlockStyle() == BlockStyle.NORMAL || orientation + (active ? 0 : 16) < BlockShapeAlgorithm.algorithms[info.getBlockStyle().id - 1].length) : "BlockStyle " + info.getBlockStyle() + ": orient " + orientation + " -> " + (orientation + (active ? 0 : 16)) + "; act: " + active + ": " + info.getName();

		}
		if (segmentData.getSegment() != null && ((RemoteSegment) segmentData.getSegment()).getSegmentController() != null) {
			((RemoteSegment) segmentData.getSegment()).setLastChanged(System.currentTimeMillis());
		}
	}

	@Override
	public void setType(int index, short newType) {
		setType(index, newType, data);
	}

	@Override
	public void setHitpointsByte(int index, int value) {
		assert (value >= 0 && value < 512);

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
			data[index] &= (byte) (~(byte) 16);
		}
		assert (active == isActive(index)) : active + "; " + isActive(index);
		//		ByteUtil.putRangedBits3OntoInt(data, active ? 0 : 1, activeIndexStart, activeIndexEnd, index, this);
	}

	//	public  void setOctree(ArrayOctree octree) {
	//		this.octree = octree;
	//	}
	@Override
	public void setOrientation(int index, byte value) {
		assert (value >= 0 && value < 8) : "NOT A SIDE INDEX";
		value = Element.orientationMapping[value];

		//			ByteUtil.putRangedBits3OntoInt(data, value, orientationStart, orientationEnd, index, this);
		data[index] &= ~orientMap[orientMap.length - 3];
		data[index] |= orientMap[value * 3];

		assert (value == getOrientation(index)) : "failed orientation coding: " + value + " != result " + getOrientation(index);
	}

	@Override
	public short getType(int index) {
		int first = data[index + 2] & 0xff;
		int snd = data[index + 1] & 0xff;
		short typeFast = (short) (first + ((snd & 7) * 256));
		return typeFast;
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
		boolean active = (data[dataIndex] & ACTIVE_BIT) == 0;
		//		assert(active == (ByteUtil.extractInt(ByteUtil.intRead3ByteArray(data, dataIndex), activeIndexStart, activeIndexEnd, this) == 0)):active+" ; "+(ByteUtil.extractInt(ByteUtil.intRead3ByteArray(data, dataIndex), activeIndexStart, activeIndexEnd, this) == 0);
		return active;
	}

	@Override
	public byte getOrientation(int dataIndex) {

		//		return  (byte) ByteUtil.extractInt(ByteUtil.intRead3ByteArray(data, dataIndex), orientationStart, orientationEnd, this);
		return (byte) (((data[dataIndex] & 0xff) >> 5) & 7);
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
		setSize(0);
		Arrays.fill(data, (byte) 0);
		setSize(0);
		octree.reset();
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
		if (!(size >= 0 && size <= BLOCK_COUNT)) {
			System.err.println("Exception WARNING: SEGMENT SIZE WRONG " + size + " " + (segment != null ? (segment.getSegmentController().getState() + ": " + segment.getSegmentController() + " " + segment) : ""));
			try {
				throw new IllegalArgumentException();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		size = (Math.max(0, Math.min(size, BLOCK_COUNT)));
		this.size = size;
		if (segment != null) {
			segment.setSize(this.size);
		}
		assert (size >= 0 && size <= BLOCK_COUNT) : "arraySize: " + size + " / " + BLOCK_COUNT;
	}

	@Override
	public short getType(byte x, byte y, byte z) {
		int index = getInfoIndex(x, y, z);
		return getType(index);
	}

	public short getType(Vector3b pos) {
		return getType(pos.x, pos.y, pos.z);
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

	public void onAddingElement(int index, byte x, byte y, byte z, short newType, boolean updateSegmentBB, long time) {
		synchronized (this) {
			onAddingElementUnsynched(index, x, y, z, newType, updateSegmentBB, true, segment.getAbsoluteIndex(x, y, z), time);
		}
	}

	public void onAddingElementUnsynched(int index, byte x, byte y, byte z, short newType, boolean updateSegmentBB, boolean segBufferUpdate, long absIndex, long time) {
		int oldSize = size;
		// new element
		setSize(size + 1);
		octree.insert(x, y, z, index);
		//			helperPos.set(x, y, z);
		getSegmentController().onAddedElementSynched(newType, getOrientation(index), x, y, z, this.segment, segBufferUpdate, absIndex, time, false);
		if (!revalidating) {
			segment.dataChanged(true);
		} else {
			//				System.err.println("Still revalidating");
		}
		updateBB(x, y, z, updateSegmentBB, true);
	}

	public void onRemovingElement(int index, byte x, byte y, byte z, byte oldOrientation, short oldType, boolean updateSegmentBB, long time) {
		synchronized (this) {
			int oldSize = size;
			// remove element
			setSize(oldSize - 1);
			octree.delete(x, y, z, index, oldType);

			getSegmentController().onRemovedElementSynched(oldType, oldSize, x, y, z, oldOrientation, this.segment, preserveControl, time);
			if (!revalidating) {
				segment.dataChanged(true);
			} else {
				//				System.err.println("Still revalidating");
			}
			updateBB(x, y, z, updateSegmentBB, false);
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
		synchronized (this) {
			this.preserveControl = true;
			if (segment != null) {
				for (byte z = 0; z < SEG; z++) {
					for (byte y = 0; y < SEG; y++) {
						for (byte x = 0; x < SEG; x++) {
							setInfoElementUnsynched(x, y, z, Element.TYPE_NONE, false, time);
						}
					}
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
		}
	}

	public void resetBB() {
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

		resetBB();

		int i = 0;

		for (byte zC = 0; zC < SegmentData.SEG; zC++) {
			for (byte yC = 0; yC < SegmentData.SEG; yC++) {
				for (byte xC = 0; xC < SegmentData.SEG; xC++) {

					if (containsFast(i)) {
						//						try{
						//						throw new NullPointerException();
						//						}catch(NullPointerException e){
						//							e.printStackTrace();
						//						}
						//						System.err.println("CONTAINS "+xC+"; "+yC+"; "+zC);
						updateBB(xC, yC, zC, false, true);
					}

					i += blockSize;
				}
			}
		}
	}

	private void revalidate(byte x, byte y, byte z, int index, long time) {
		short type = getType(index);
		if (type != Element.TYPE_NONE) {
			//			System.err.println("[SEGDATA] ADDED EL'EMENT: "+x+", "+y+"; "+z+"; "+type);
			synchronized (this) {

				if (ElementKeyMap.exists(type)) {
					onAddingElementUnsynched(index, x, y, z, type, false, true, segment.getAbsoluteIndex(x, y, z), time);
				} else {
					//				System.err.println("DETECTED CUSTOM BLOCK: "+type+"; "+x+", "+y+", "+z);
					setType(index, Element.TYPE_NONE, data);

					assert (getType(x, y, z) == Element.TYPE_NONE) : "FAILED: " + type + "; " + x + ", " + y + ", " + z;
				}
			}
		}
	}

	public void revalidateData(long time) {
		synchronized (this) {
			revalidating = true;
			//			octree.reset();
			//			if(getSize() > 0){
			//				System.err.println("[WARNING][SEGMENTDATA] segment not empty on revalidate. size was "+getSize()+" in "+getSegment().pos+" -> "+getSegmentController());
			//				reset();
			//			}
			assert (size == 0) : " size is " + size + " in " + segment.pos + " -> " + getSegmentController();
			int i = 0;
			for (byte z = 0; z < SegmentData.SEG; z++) {
				for (byte y = 0; y < SegmentData.SEG; y++) {
					for (byte x = 0; x < SegmentData.SEG; x++) {
						revalidate(x, y, z, i, time);
						i += blockSize;
					}
				}
			}
			getSegmentController().getSegmentBuffer().updateBB(segment);
			//			Vector3b p = new Vector3b();
			//			for(byte z = 0; z < SegmentData.SEG; z++){
			//				for(byte y = 0; y < SegmentData.SEG; y++){
			//					for(byte x = 0; x < SegmentData.SEG; x++){
			//						p.set(x,y,z);
			//						setVis(p,getSegment().getVisablility(p));
			//					}
			//				}
			//			}
			setNeedsRevalidate(false);
			revalidating = false;
			segment.dataChanged(true);
		}

	}

	public boolean revalidateSuccess() {
		for (byte z = 0; z < SegmentData.SEG; z++) {
			for (byte y = 0; y < SegmentData.SEG; y++) {
				for (byte x = 0; x < SegmentData.SEG; x++) {
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

	public void setInfoElement(byte x, byte y, byte z, short newType, boolean updateSegmentBB, long time) {
		synchronized (this) {
			setInfoElementUnsynched(x, y, z, newType, (byte) -1, (byte) -1, updateSegmentBB, time);
		}

	}

	public void setInfoElement(byte x, byte y, byte z, short newType, byte orientation, byte activation, boolean updateSegmentBB, long time) {
		synchronized (this) {
			setInfoElementUnsynched(x, y, z, newType, orientation, activation, updateSegmentBB, time);
		}

	}

	public void setInfoElement(Vector3b pos, short type, boolean updateSegmentBB, long time) {

		//		System.err.println("setting info at "+pos+", Element: "+e.x+", "+e.y+", "+e.z+", index: "+getInfoIndex(pos.x, pos.y, pos.z, Element.HALF_SIZE));
		setInfoElement(pos.x, pos.y, pos.z, type, (byte) -1, (byte) -1, updateSegmentBB, time);
	}

	public void setInfoElement(Vector3b pos, short type,
	                           byte elementOrientation, byte activation, boolean updateSegmentBB, long time) {
		setInfoElement(pos.x, pos.y, pos.z, type, elementOrientation, activation, updateSegmentBB, time);

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
	public void setInfoElementForcedAddUnsynched(byte x, byte y, byte z, short newType, boolean updateSegmentBB, long time) {
		setInfoElementForcedAddUnsynched(x, y, z, newType, (byte) -1, (byte) -1, updateSegmentBB, time);
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
	public void setInfoElementForcedAddUnsynched(byte x, byte y, byte z, short newType, byte orientation, byte activation, boolean updateSegmentBB, long time) {
		if (newType == Element.TYPE_NONE) {
			//			System.err.println("WARNING: Tried to set None Element on Forced Add");
			return;
		}

		int index = getInfoIndex(x, y, z);

		setType(index, newType, data);
		if (orientation > -1) {
			setOrientation(index, orientation);
		}
		if (activation > -1) {
			setActive(index, activation != 0);
		}

		onAddingElementUnsynched(index, x, y, z, newType, updateSegmentBB, false, segment.getAbsoluteIndex(x, y, z), time);
		setHitpointsByte(index, ElementKeyMap.MAX_HITPOINTS);
	}

	public void setInfoElementUnsynched(byte x, byte y, byte z, short newType, boolean updateSegmentBB, long time) {
		setInfoElementUnsynched(x, y, z, newType, (byte) -1, (byte) -1, updateSegmentBB, time);
	}

	public void setInfoElementUnsynched(byte x, byte y, byte z, short newType, byte orientation, byte activation, boolean updateSegmentBB, long time) {
		int index = getInfoIndex(x, y, z);
		short oldType = getType(index);
		byte oldOrientation = getOrientation(index);

		setType(index, newType, data);
		if (orientation > -1) {
			setOrientation(index, orientation);
		}
		if (activation > -1) {
			setActive(index, activation != 0);
			//			System.err.println("NOWWWW ACTIVE: "+isActive(index)+"; orientation: "+getOrientation(index));
		}

		if (newType == Element.TYPE_NONE) {
			setActive(index, false);
			setOrientation(index, (byte) 0);
			// an existing element was set to NULL
			if (oldType != Element.TYPE_NONE) {
				onRemovingElement(index, x, y, z, oldOrientation, oldType, updateSegmentBB, time);
			}

		} else {
			// a NULL element was set to existing
			if (oldType == Element.TYPE_NONE) {
				onAddingElementUnsynched(index, x, y, z, newType, updateSegmentBB, true, segment.getAbsoluteIndex(x, y, z), time);

				//set hitpoints
				setHitpointsByte(index, ElementKeyMap.MAX_HITPOINTS);

				//				if(newType == ElementKeyMap.CORE_ID){
				//					System.err.println("SET CORE: "+getHitpoints(index)+" / "+ElementKeyMap.informationMap.get(ElementKeyMap.get(newType)).getMaxHitPoints());
				//				}

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
	public void setInfoElementUnsynched(Vector3b pos, short type, boolean updateSegmentBB, long time) {
		setInfoElementUnsynched(pos.x, pos.y, pos.z, type, updateSegmentBB, time);
	}

	public void setNeedsRevalidate(boolean needsRevalidate) {
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
			if (updateSegmentBB) {
				getSegmentController().getSegmentBuffer().updateBB(segment);
			}
		} else {
			if (size == 0) {
				if (updateSegmentBB) {
					restructBB(updateSegmentBB);
				}
			} else {
				if (x + 1 < max.x && y + 1 < max.y && z + 1 < max.z &&
						x > min.x && y > min.y && z > min.z) {
					//removed from middle -> nothing to do
					//					System.err.println("UPDATED FROM MIDDLE: NOTHING TO DO "+x+", "+y+", "+z+"; BB ("+getMin()+"; "+getMax()+")");
				} else {
					if (updateSegmentBB) {
						restructBB(updateSegmentBB);
					}
				}
			}
		}

		//		assert(getMin().x >= 0 && getMin().y >= 0 && getMin().z >= 0);
		//		assert(getMax().x < 16 && getMax().y < 16 && getMax().z < 16);

	}

	@Override
	public void setInfoElementForcedAddUnsynched(byte x, byte y, byte z,
			short type, boolean updateSegmentBB) {
				
	}

	@Override
	public void setInfoElementForcedAddUnsynched(byte x, byte y, byte z,
			short newType, byte orientation, byte activation,
			boolean updateSegmentBB) {
				
	}

	@Override
	public Vector3i getSegmentPos() {
				return null;
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
