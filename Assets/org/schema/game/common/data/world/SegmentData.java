package org.schema.game.common.data.world;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.MemoryManager.MemFloatArray;
import org.schema.common.util.MemoryManager.MemIntArray;
import org.schema.common.util.MemoryManager.MemShortArray;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.cubes.CubeMeshBufferContainer;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;
import org.schema.game.common.controller.EditableSendableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SegmentDataMetaData;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.octree.ArrayOctree;

import javax.vecmath.Vector3f;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public abstract class SegmentData implements SegmentDataInterface {

	public static final int typeBitCount = 11; //11 bits -> 2048
	public static final int hitpointsBitCount = 7; //7 bits -> 128
	public static final int activeBitCount = 1; //1 bit -> 1
	public static final int orientationBitCount = 5; //5 bit -> 16

	public static final int typeIndexStart = 0;
	public static final int hitpointsIndexStart = typeIndexStart + typeBitCount;
	public static final int activeIndexStart = hitpointsIndexStart + hitpointsBitCount;
	public static final int orientationStart = activeIndexStart + activeBitCount;

	public static final int typeMask = ((~0) >>> (32 - typeBitCount)) << typeIndexStart; //Integer.parseInt("000000000000011111111111", 2);
	public static final int typeMaskNot = ~typeMask;
	public static final int hpMask = ((~0) >>> (32 - hitpointsBitCount)) << hitpointsIndexStart; //Integer.parseInt("000000111111100000000000", 2);
	public static final int hpMaskNot = ~hpMask;
	public static final int activeMask = ((~0) >>> (32 - activeBitCount)) << activeIndexStart; //Integer.parseInt("000001000000000000000000", 2);
	public static final int activeMaskNot = ~activeMask;
	public static final int orientMask = ((~0) >>> (32 - orientationBitCount)) << orientationStart; //Integer.parseInt("111110000000000000000000", 2);
	public static final int orientMaskNot = ~orientMask;
	public static final int SEG = Segment.DIM;
	public static final float SEGf = SEG;
	public static final byte ANTI_BYTE = -(SEG); // -16 -> 0xf0 -> 11110000;
	public static final int SEG_MINUS_ONE = SEG - 1;
	public static final int SEG_TIMES_SEG = SEG * SEG;
	public static final int SEG_TIMES_SEG_TIMES_SEG = SEG * SEG * SEG;
	public static final int BLOCK_COUNT = SEG_TIMES_SEG_TIMES_SEG;
	public static final int TOTAL_SIZE = BLOCK_COUNT;
	public static final int PIECE_ADDED = 0;
	public static final int PIECE_REMOVED = 1;
	public static final int PIECE_CHANGED = 2;
	public static final int PIECE_UNCHANGED = 3;
	public static final int PIECE_ACTIVE_CHANGED = 4;
	public static final int SEG_HALF = Segment.HALF_DIM;
	public static final int VERSION = 7;
	public static final short MAX_TYPE_ID = 8191;
	private static final long yDelim = (Short.MAX_VALUE + 1) * 2;
	private static final long zDelim = yDelim * yDelim;

	public final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
	protected final Vector3b min = new Vector3b();
	protected final Vector3b max = new Vector3b();
	protected ArrayOctree octree;
	protected final boolean onServer;
	protected Segment segment;

	private int size;
	private boolean preserveControl;
	private boolean needsRevalidate = false;
	private boolean revalidating;
	private boolean blockAddedForced;

	private IntArrayList lodShapes;
	public boolean revalidatedOnce;
	private StringWriter errors;
	private StringWriter errors2;
	private static boolean DEBUG = false;

	public enum SegmentDataType {
		//nt id 2 is reserved for empty seg
		FOUR_BYTE(SegmentData4Byte.class, RemoteSegment.DATA_AVAILABLE_BYTE),
		SINGLE(SegmentDataSingle.class, (byte) 3),
		BITMAP(SegmentDataBitMap.class, (byte) 4),
		SINGLE_SIDE_EDGE(SegmentDataSingleSideEdge.class, (byte) 5),
		;
		final Class<? extends SegmentData> associatedClass;
		public final byte networkTypeId;
		public int received;

		public SegmentData instantiate(boolean onClient) {
			SegmentData doInstantiate = doInstantiate(onClient);
			assert (doInstantiate.getClass() == associatedClass) : "Type mismatch: " + doInstantiate.getClass() + "; " + associatedClass;
			return doInstantiate;
		}

		private SegmentData doInstantiate(boolean onClient) {
			return switch(this) {
				case FOUR_BYTE -> new SegmentData4Byte(onClient);
				case SINGLE -> new SegmentDataSingle(onClient);
				case BITMAP -> new SegmentDataBitMap(onClient);
				case SINGLE_SIDE_EDGE -> new SegmentDataSingleSideEdge(onClient);
			};
		}

		public static SegmentDataType getByNetworkId(byte ntId) {
			for(SegmentDataType t : values()) {
				if(t.networkTypeId == ntId) {
					return t;
				}
			}
			throw new IllegalArgumentException("network type id not found: " + ntId);
		}

		private SegmentDataType(Class<? extends SegmentData> associatedClass, byte networkTypeId) {
			this.associatedClass = associatedClass;
			this.networkTypeId = networkTypeId;
		}

		private static boolean checkNTIds() {
			for(SegmentDataType t : values()) {
				if(t.networkTypeId == RemoteSegment.DATA_EMPTY_BYTE) {
					System.err.println("INVALID NT ID (EMPTY) " + t);
					return false;
				}
				for(SegmentDataType s : values()) {
					if(s != t && s.networkTypeId == t.networkTypeId) {
						System.err.println("EQUAL NT ID " + t + " :: " + s);
						return false;
					}
				}
			}
			return true;
		}
	}

	static {
		assert (SegmentDataType.checkNTIds());
	}

	public SegmentData() {
		octree = null;
		this.onServer = true;
	}

	public SegmentData(boolean onClient) {
		this.onServer = !onClient;
		octree = getOctreeInstance(this.onServer);

		resetBB();
	}

	/**
	 * creates a copy with just the data copied (needs revalidation to be usable)
	 *
	 * @param segmentData
	 */
	public SegmentData(SegmentData segmentData) {
		this.onServer = segmentData.onServer;
		octree = getOctreeInstance(onServer);

		resetBB();
	}

	protected abstract ArrayOctree getOctreeInstance(boolean onServer);

	public static byte getPosXFromIndex(int index) {
		int x = (index & 0x1F);
		return (byte) x;
	}

	public static byte getPosYFromIndex(int index) {
		int y = ((index >> 5) & 0x1F);
		return (byte) y;
	}

	public static byte getPosZFromIndex(int index) {
		int z = ((index >> 10) & 0x1F);
		return (byte) z;
	}

	public static Vector3b getPositionFromIndex(int index, Vector3b out) {
		int z = ((index >> 10) & 0x1F);
		int y = ((index >> 5) & 0x1F);
		int x = (index & 0x1F);
		assert (checkIndex(x, y, z, index)) : x + ", " + y + ", " + z;
		assert (valid(x, y, z)) : x + ", " + y + ", " + z + "; " + index;
		out.set((byte) x, (byte) y, (byte) z);
		return out;
	}

	public static long getLongIndex(int index, int xAdd, int yAdd, int zAdd) {
		int z = ((index >> 10) & 0x1F);
		int y = ((index >> 5) & 0x1F);
		int x = (index & 0x1F);

		return ElementCollection.getIndex(x + xAdd, y + yAdd, z + zAdd);
	}

	public static Vector3f getPositionFromIndexWithShift(int index, Vector3i segPos, Vector3f out) {
		int z = ((index >> 10) & 0x1F);
		int y = ((index >> 5) & 0x1F);
		int x = (index & 0x1F);

		assert (checkIndex(x, y, z, index)) : x + ", " + y + ", " + z;

		out.set(segPos.x + x - SegmentData.SEG_HALF, segPos.y + y - SegmentData.SEG_HALF, segPos.z + z - SegmentData.SEG_HALF);
		return out;
	}

	protected static boolean checkIndex(int xx, int yy, int zz, int index) {
		int z = index / SEG_TIMES_SEG;
		index -= z * SEG_TIMES_SEG;
		int y = index / SEG;
		index -= y * SEG;
		int x = index;

		return xx == x && yy == y && zz == z;
	}

	public static boolean valid(final byte x, final byte y, final byte z) {
		return (((x | y | z) & ANTI_BYTE) == 0);
	}

	public static boolean valid(final int x, final int y, final int z) {
		return (((x | y | z) & ANTI_BYTE) == 0);
	}

	public static boolean allNeighborsInside(final byte x, final byte y, final byte z) {
		return x < SEG_MINUS_ONE && y < SEG_MINUS_ONE && z < SEG_MINUS_ONE && x >= 1 && y >= 1 && z >= 1;
	}

	public static boolean allNeighborsInside(final int x, final int y, final int z) {
		return x < SEG_MINUS_ONE && y < SEG_MINUS_ONE && z < SEG_MINUS_ONE && x >= 1 && y >= 1 && z >= 1;
	}

	public static int getInfoIndex(byte x, byte y, byte z) {
		int i = ((z * SEG_TIMES_SEG) + (y * SEG) + x);
		return i;
	}

	public static int getInfoIndex(int x, int y, int z) {
		int i = ((z * SEG_TIMES_SEG) + (y * SEG) + x);
		return i;
	}

	public static int getInfoIndex(Vector3b pos) {
		return getInfoIndex(pos.x, pos.y, pos.z);
	}

	public static int getInfoIndex(Vector3i pos) {
		return getInfoIndex(pos.x, pos.y, pos.z);
	}

	/**
	 * be cautious using that method, because it will NOT update any adding
	 * or removing of segments. that has to be done extra!
	 *
	 * @return true if an element was added or removed
	 */
	public int applySegmentData(SegmentPiece block, long currentTime) throws SegmentDataWriteException {
		return applySegmentData(block.x, block.y, block.z, block.getData(), 0, false, block.getAbsoluteIndex(), true, true, currentTime);
	}

	/**
	 * be cautious using that method, because it will NOT update any adding
	 * or removing of segments. that has to be done extra!
	 *
	 * @return true if an element was added or removed
	 */
	public int applySegmentData(byte x, byte y, byte z, int pieceData, int offset, final boolean synched, long absIndex, boolean updateRemoveBB, boolean updateSegmentBB, long time) throws SegmentDataWriteException {
		throw new SegmentDataWriteException(this);
	}

	public int applySegmentData(byte x, byte y, byte z, int pieceData, int offset, final boolean synched, long absIndex, boolean updateRemoveBB, boolean updateSegmentBB, long time, boolean preserveControl) throws SegmentDataWriteException {
		throw new SegmentDataWriteException(this);
	}
//	public int arraySize() {
//		return data.length;
//	}

	public void assignData(Segment segment) {
		segment.setSegmentData(this);
		this.segment = segment;
		resetBB();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */

	public boolean contains(byte x, byte y, byte z) {
		return valid(x, y, z) && containsUnsave(x, y, z);
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
		return segment.pos.equals(((SegmentData) obj).segment.pos);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "(DATA: " + (segment == null ? "NullSeg" : segment) + ")";
	}

	public abstract boolean containsFast(int index);

	/**
	 * @param index
	 * @return returns 1 of contains, 0 if not
	 */
	public int containsFastCode(int index) {
		return containsFast(index) ? 1 : 0;
	}

	public boolean containsFast(Vector3b pos) {
		return containsFast(getInfoIndex(pos));
	}

	public boolean containsFast(Vector3i pos) {
		return containsFast(getInfoIndex(pos));
	}

	public boolean containsUnblended(byte x, byte y, byte z) {
		short type;
		return valid(x, y, z) && ((type = getType(x, y, z)) != Element.TYPE_NONE && !ElementKeyMap.getInfo(type).isBlended());

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

//	public float damage(float damageInitial, Vector3i oPos, float armorEfficiency, float armorHarden, float radius, SegmentDamageCallback cb) throws SegmentDataWriteException {
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
//						if ((type = getType(i)) != Element.TYPE_NONE && (hitpoints = getHitpoints(i)) > 0) {
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
//										if (center && x == SegmentData.SEG_HALF && y == SegmentData.SEG_HALF && z == SegmentData.SEG_HALF) {
//											System.err.println("[HIT-SEGMENTDATA] Core Destroyed " + getSegment());
//										} else {
//											cb.registerRemoval(this, x, y, z);
//											//											setInfoElementUnsynched(x,y,z, Element.TYPE_NONE, false);
//										}
//									} else {
//										setHitpoints(i, (short) Math.min(255, Math.max(0, (hitpoints - damage))));
//										assert (getHitpoints(i) > 0);
//									}
//								}
//							}
//						}
//						i++;
//					}
//				}
//			}
//			return totalDamage;
//		} finally {
//			rwl.writeLock().unlock();
//		}
//	}

	public abstract void deserialize(DataInput inputStream, long time) throws IOException, SegmentDataWriteException;

	public static void convertFrom3ByteToIntArray(byte[] from, int[] to) {
		for(int i = 0; i < TOTAL_SIZE; i++) {
			to[i] = convert3ByteToIntValue(from[i * 3], from[i * 3 + 1], from[i * 3 + 2]);
		}
	}

	@Override
	public byte[] getAsOldByteBuffer() {
		throw new IllegalArgumentException("Incompatible SegmentData Version");
	}

	@Override
	public void migrateTo(int fromVersion, SegmentDataInterface segmentData) {
		assert (false);
	}

	@Override
	public void setType(int index, short newType) throws SegmentDataWriteException {
		throw new SegmentDataWriteException(this);
	}

	public static int putType(int from, short value) {
//		return (from & typeMaskNot) | value;
		return SegmentData4Byte.putType(from, value);
	}

	@Override
	public void setHitpointsByte(int index, int value) throws SegmentDataWriteException {
		throw new SegmentDataWriteException(this);
	}

	public static int putHitpoints(int from, byte value) {
//		return (from & hpMaskNot) | (value << hitpointsIndexStart);
		return SegmentData4Byte.putHitpoints(from, value);
	}

	@Override
	public void setActive(int index, boolean active) throws SegmentDataWriteException {
		throw new SegmentDataWriteException(this);
	}

	public static int putActivation(int from, boolean value) {
//		return value ? (from | activeMask) : (from & activeMaskNot);
		return SegmentData4Byte.putActivation(from, value);
	}

	@Override
	public void setOrientation(int index, byte value) throws SegmentDataWriteException {
		throw new SegmentDataWriteException(this);
	}

	public static int putOrientationInt(int from, byte value) {
//		return (from & orientMaskNot) | (value << orientationStart);
		return SegmentData4Byte.putOrientationInt(from, value);
	}

	public abstract void getBytes(int dataIndex, byte[] bytes);

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
	public void resetFast() throws SegmentDataWriteException {
		revalidatedOnce = false;
		setSize(0);
		setSize(0);
		if(octree != null) {
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

	@Override
	public int getSize() {
		return size;
	}

	@Override
	public void setSize(int size) {
		assert ((size >= 0 && size <= BLOCK_COUNT)) : "Exception WARNING: SEGMENT SIZE WRONG " + size + " " + (segment != null ? (segment.getSegmentController().getState() + ": " + segment.getSegmentController() + " " + segment) : "");

		this.size = size;
		if(segment != null) {
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

	public static short getTypeFromIntData(int data) {
//		return (short) (typeMask & data);
		return SegmentData4Byte.getTypeFromIntData(data);
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
		if(!allNeighborsInside(x, y, z)) {
			if(contains((byte) (x - 1), y, z)) {
				return true;
			}
			if(contains((byte) (x + 1), y, z)) {
				return true;
			}
			if(contains(x, (byte) (y - 1), z)) {
				return true;
			}
			if(contains(x, (byte) (y + 1), z)) {
				return true;
			}
			if(contains(x, y, (byte) (z - 1))) {
				return true;
			}
			if(contains(x, y, (byte) (z + 1))) {
				return true;
			}
		} else {
			if(containsUnsave((byte) (x - 1), y, z)) {
				return true;
			}
			if(containsUnsave((byte) (x + 1), y, z)) {
				return true;
			}
			if(containsUnsave(x, (byte) (y - 1), z)) {
				return true;
			}
			if(containsUnsave(x, (byte) (y + 1), z)) {
				return true;
			}
			if(containsUnsave(x, y, (byte) (z - 1))) {
				return true;
			}
			if(containsUnsave(x, y, (byte) (z + 1))) {
				return true;
			}
		}
		return false;
	}

	protected void onAddingElement(int index, byte x, byte y, byte z, short newType, boolean updateSegmentBB, final boolean synched, long absIndex, long time) {
		if(synched) {
			rwl.writeLock().lock();
		}
		try {
			onAddingElementUnsynched(index, x, y, z, newType, updateSegmentBB, true, absIndex, time);
		} finally {
			if(synched) {
				rwl.writeLock().unlock();
			}
		}

	}

	/**
	 * only save to use if data is a block
	 *
	 * @param data
	 * @return if block is in octree
	 */
	protected static boolean inOctree(int data) {
		short type = getTypeFromIntData(data);
		ElementInformation info;
		return ElementKeyMap.isValidType(type) && (!((info = ElementKeyMap.infoArray[type]).isDoor() && !isActiveFromIntData(data))) && (info.isInOctree());
	}

	protected boolean inOctree(int type, int index) {
		ElementInformation info = ElementKeyMap.infoArray[type];
		return info.isInOctree() && !(info.isDoor() && !isActive(index));
	}

	public static boolean isActiveFromIntData(int data) {
		return (activeMask & data) > 0;
	}

	private void onAddingElementUnsynched(final int index, final byte x, final byte y, final byte z, final short newType, final boolean updateSegmentBB, final boolean segBufferUpdate, final long absIndex, final long time) {
		int oldSize = this.size;

		incSize();

		if(inOctree(newType, index)) {
			octree.insert(x, y, z, index);
		}
		if(ElementKeyMap.lodShapeArray[newType]) {
			addLodShape(index, x, y, z);
		}
		byte orientation = getOrientation(index);
		getSegmentController().onAddedElementSynched(newType, orientation, x, y, z, this.segment, false, absIndex, time, revalidating);
		if(segBufferUpdate && !revalidating) {
			getSegmentController().getSegmentBuffer().onAddedElement(newType, oldSize, x, y, z, segment, time, orientation);
		}
		if(getSegmentController().isOnServer() && !revalidating) {
			((EditableSendableSegmentController) getSegmentController()).doDimExtensionIfNecessary(segment, x, y, z);
		}
		if(!revalidating) {
			segment.dataChanged(true);
		}

		updateBBAdd(x, y, z, index);

		if(updateSegmentBB) {
			getSegmentController().getSegmentBuffer().updateBB(segment);
		}
	}

	private void removeLodShape(int index, byte x, byte y, byte z) {
		int i = lodShapes.indexOf(index);
		if(i >= 0) {
			lodShapes.remove(i);
		} else {
			assert (false) : index;
		}
	}

	private void addLodShape(int index, byte x, byte y, byte z) {
		if(lodShapes == null) {
			lodShapes = new IntArrayList();
		}

		lodShapes.add(index);

	}

	public void replace(SegmentData oldData) {
		setSize(oldData.size);
		min.set(oldData.min);
		max.set(oldData.max);
		octree = oldData.octree;
		lodShapes = oldData.lodShapes;
		lodData = oldData.lodData;
		lodTypeAndOrientcubeIndex = oldData.lodTypeAndOrientcubeIndex;

		needsRevalidate = oldData.needsRevalidate();
		segment = oldData.segment;
		segment.setSegmentData(this);
	}

	public void onRemovingElement(int index, byte x, byte y, byte z, short oldType, boolean updateBBGeneral, boolean updateSegmentBB, byte oldOrientation, boolean wasActive, final boolean synched, long time, boolean preserveControl) {
		if(synched) {
			rwl.writeLock().lock();
		}
		try {
			int oldSize = size;
			// remove element
			setSize(oldSize - 1);
			if(inOctree(oldType, index)) {
				octree.delete(x, y, z, index, oldType);
			}
			if(ElementKeyMap.isLodShape(oldType)) {
				removeLodShape(index, x, y, z);
			}
			getSegmentController().onRemovedElementSynched(oldType, oldSize, x, y, z, oldOrientation, this.segment, this.preserveControl || preserveControl, time);
			if(!revalidating) {
				segment.dataChanged(true);
			} else {
				//				System.err.println("Still revalidating");
			}
			if(updateBBGeneral) {
				updateBBRemove(x, y, z, updateSegmentBB, index);
			}
		} finally {
			if(synched) {
				rwl.writeLock().unlock();
			}
		}
	}

	/**
	 * @return the element at this position null, if there is no element in that
	 * position
	 */
	public void removeInfoElement(byte x, byte y, byte z) throws SegmentDataWriteException {
		setType(getInfoIndex(x, y, z), Element.TYPE_NONE);
	}

	/**
	 * @param pos
	 * @return the element at this position null, if there is no element in that
	 * position
	 */
	public void removeInfoElement(Vector3b pos) throws SegmentDataWriteException {
		removeInfoElement(pos.x, pos.y, pos.z);
	}

	public void reset(long time) throws SegmentDataWriteException {
		rwl.writeLock().lock();
		try {
			this.preserveControl = true;
			if(segment != null) {
				long xAbsStart = (segment.pos.x & 0xFFFF);
				long yAbsStart = ((long) (segment.pos.y & 0xFFFF) << 16);
				long zAbsStart = ((long) (segment.pos.z & 0xFFFF) << 32);
				long xAbs = xAbsStart;
				long yAbs = yAbsStart;
				long zAbs = zAbsStart;

				for(byte z = 0; z < SegmentData.SEG; z++) {
					for(byte y = 0; y < SegmentData.SEG; y++) {
						for(byte x = 0; x < SegmentData.SEG; x++) {

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

	public void restructBB(Vector3b min, Vector3b max) {
		max.set(Byte.MIN_VALUE, Byte.MIN_VALUE, Byte.MIN_VALUE);
		min.set(Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MAX_VALUE);
		int i = 0;
		for(byte zC = 0; zC < SegmentData.SEG; zC++) {
			for(byte yC = 0; yC < SegmentData.SEG; yC++) {
				for(byte xC = 0; xC < SegmentData.SEG; xC++) {

					if(containsFast(i)) {
						max.x = (byte) Math.max(xC + 1, max.x);
						max.y = (byte) Math.max(yC + 1, max.y);
						max.z = (byte) Math.max(zC + 1, max.z);

						min.x = (byte) Math.min(xC, min.x);
						min.y = (byte) Math.min(yC, min.y);
						min.z = (byte) Math.min(zC, min.z);

					}

					i++;
				}
			}
		}
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

		for(byte zC = 0; zC < SegmentData.SEG; zC++) {
			for(byte yC = 0; yC < SegmentData.SEG; yC++) {
				for(byte xC = 0; xC < SegmentData.SEG; xC++) {

					if(containsFast(i)) {
						//						try{
						//						throw new NullPointerException();
						//						}catch(NullPointerException e){
						//							e.printStackTrace();
						//						}
						//						System.err.println("CONTAINS "+xC+"; "+yC+"; "+zC);
						updateBBAdd(xC, yC, zC, i);

					}

					i++;
				}
			}
		}
		if(updateSegmentBB) {
			if(minBeforeX != min.x || minBeforeY != min.y || minBeforeZ != min.z ||
					maxBeforeX != max.x || maxBeforeY != max.y || maxBeforeZ != max.z)

				getSegmentController().getSegmentBuffer().restructBBFast(this);
		}
	}

	private void unvalidate(byte x, byte y, byte z, int index, long time) {

		short type = getType(index);
		byte o = getOrientation(index);
		if(type != Element.TYPE_NONE) {
			onRemovingElement(index, x, y, z, type, false, false, o, isActive(index), false, time, false);
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
			for(byte z = 0; z < SegmentData.SEG; z++) {
				for(byte y = 0; y < SegmentData.SEG; y++) {
					for(byte x = 0; x < SegmentData.SEG; x++) {
						unvalidate(x, y, z, i, time);
						i++;
					}
				}
			}

			getSegmentController().getSegmentBuffer().updateBB(segment);

		} finally {
			revalidating = false;
			segment.dataChanged(true);
			rwl.writeLock().unlock();
		}

	}

	private static final byte[] coordX = new byte[BLOCK_COUNT];
	private static final byte[] coordY = new byte[BLOCK_COUNT];
	private static final byte[] coordZ = new byte[BLOCK_COUNT];
	private static final long[] absCoordX = new long[BLOCK_COUNT];
	private static final long[] absCoordY = new long[BLOCK_COUNT];
	private static final long[] absCoordZ = new long[BLOCK_COUNT];

	static {
		createRevalidationLookupTables();
	}

	private static void createRevalidationLookupTables() {
		long xAbs = 0;
		long yAbs = 0;
		long zAbs = 0;
		int index = 0;
		for(byte z = 0; z < SegmentData.SEG; z++) {
			for(byte y = 0; y < SegmentData.SEG; y++) {
				for(byte x = 0; x < SegmentData.SEG; x++) {
					coordX[index] = x;
					coordY[index] = y;
					coordZ[index] = z;
					absCoordX[index] = xAbs;
					absCoordY[index] = yAbs;
					absCoordZ[index] = zAbs;
					index++;
					xAbs++;
				}
				xAbs = 0;
				yAbs += yDelim;
			}
			xAbs = 0;
			yAbs = 0;
			zAbs += zDelim;
		}
	}

	public void revalidateDataMeta(long time, boolean staticElement, boolean prevalidatedTypes, SegmentDataMetaData metaData) {
		assert (!this.revalidatedOnce) : errors + "; " + errors2 + "; " + getSegmentController();
		this.revalidatedOnce = true;

		metaData.reset(staticElement);
		assert (segment != null);
		metaData.segPos.set(segment.pos);
		try {
			revalidating = true;
			assert (size == 0) : " size is " + size + " in " + segment.pos + " -> " + getSegmentController();

			long xAbsStart = (segment.pos.x & 0xFFFF);
			long yAbsStart = ((long) (segment.pos.y & 0xFFFF) << 16);
			long zAbsStart = ((long) (segment.pos.z & 0xFFFF) << 32);

			if(prevalidatedTypes) {
				for(int index = 0; index < BLOCK_COUNT; index++) {
					//inline
					int type = getType(index);//getType(index);//((data[index + 2] & 0xff) + lookUpSecType[data[index + 1] & 0xff]);
					if(type != Element.TYPE_NONE) {
						//also inline and fast route
						byte x = coordX[index];
						byte y = coordY[index];
						byte z = coordZ[index];
						long xAbs = xAbsStart + absCoordX[index];
						long yAbs = yAbsStart + absCoordY[index];
						long zAbs = zAbsStart + absCoordZ[index];

						long absIndex = xAbs + yAbs + zAbs;

						size++;
						if(inOctree(type, index)) {
							octree.insert(x, y, z, index);
						}
						if(ElementKeyMap.lodShapeArray[type]) {
							addLodShape(index, x, y, z);
						}
						metaData.onAddedElementSynched((short) type, x, y, z, this, index, absIndex);
						min.x = (byte) Math.min(min.x, x);
						min.y = (byte) Math.min(min.y, y);
						min.z = (byte) Math.min(min.z, z);
						max.x = (byte) Math.max(max.x, x + 1);
						max.y = (byte) Math.max(max.y, y + 1);
						max.z = (byte) Math.max(max.z, z + 1);
						octree.insertAABB16(x, y, z, index);
					}
				}
			} else {
				final int highestType = ElementKeyMap.highestType;

				for(int index = 0; index < BLOCK_COUNT; index++) {
					//inline
					int type = getType(index);//getType(index);//((data[index + 2] & 0xff) + lookUpSecType[data[index + 1] & 0xff]);
					if(type != Element.TYPE_NONE) {

						//also inline and fast route
						if(type <= highestType && ElementKeyMap.validArray[type]) {
							//was checking hp > maxHp here. we can deal with (hp > maxHp) on damage
							//as this is called for every single block
							//						int hp = getHitpoints(index);
							//						int maxHp = ElementKeyMap.infoArray[type].getMaxHitPoints();
							//						if (getHitpoints(index) > maxHp) {
							//							setHitpoints(index, Math.min(maxHp, hp));
							//						}
							byte x = coordX[index];
							byte y = coordY[index];
							byte z = coordZ[index];
							long xAbs = xAbsStart + absCoordX[index];
							long yAbs = yAbsStart + absCoordY[index];
							long zAbs = zAbsStart + absCoordZ[index];

							long absIndex = xAbs + yAbs + zAbs;

							size++;
							if(inOctree(type, index)) {
								octree.insert(x, y, z, index);
							}
							if(ElementKeyMap.lodShapeArray[type]) {
								addLodShape(index, x, y, z);
							}
							metaData.onAddedElementSynched((short) type, x, y, z, this, index, absIndex);
							min.x = (byte) Math.min(min.x, x);
							min.y = (byte) Math.min(min.y, y);
							min.z = (byte) Math.min(min.z, z);
							max.x = (byte) Math.max(max.x, x + 1);
							max.y = (byte) Math.max(max.y, y + 1);
							max.z = (byte) Math.max(max.z, z + 1);
							octree.insertAABB16(x, y, z, index);

						} else {
							setType(index, Element.TYPE_NONE);
						}
					}
				}
			}
			Segment s = segment;
			if(s != null) {
				s.setSize(size);
			}
			assert ((size >= 0 && size <= BLOCK_COUNT)) : "Exception WARNING: SEGMENT SIZE WRONG " + size + " " + (segment != null ? (segment.getSegmentController().getState() + ": " + segment.getSegmentController() + " " + segment) : "");
			assert (isBBValid()) : this.getClass().getSimpleName() + ": Pos: " + getSegmentPos() + "; size " + size + "; minmax " + min + ", " + max;

		} catch(SegmentDataWriteException e) {
			throw new RuntimeException("Should never happen here", e);
		} finally {
			revalidating = false;
			needsRevalidate = false;
			segment.dataChanged(true);
		}
	}

	public void revalidateData(long time, boolean staticElement) {
		assert (!this.revalidatedOnce) : errors + "; " + errors2 + "; " + getSegmentController();

		this.revalidatedOnce = true;

		if(DEBUG) {
			try {
				throw new Exception("BUILT REVALIDATE " + segment.pos);
			} catch(Exception r) {
				errors = new StringWriter();
				r.printStackTrace(new PrintWriter(errors));
			}
		}
		rwl.writeLock().lock();
		try {
			revalidating = true;
			assert (size == 0) : " size is " + size + " in " + segment.pos + " -> " + getSegmentController();

			long xAbsStart = (segment.pos.x & 0xFFFF);
			long yAbsStart = ((long) (segment.pos.y & 0xFFFF) << 16);
			long zAbsStart = ((long) (segment.pos.z & 0xFFFF) << 32);

			final int highestType = ElementKeyMap.highestType;

			for(int index = 0; index < BLOCK_COUNT; index++) {
				//inline
				int type = getType(index);//getType(index);//((data[index + 2] & 0xff) + lookUpSecType[data[index + 1] & 0xff]);
				if(type != Element.TYPE_NONE) {
					//also inline and fast route
					if(type <= highestType && ElementKeyMap.validArray[type]) {
						//was checking hp > maxHp here. we can deal with (hp > maxHp) on damage
						//as this is called for every single block
//						int hp = getHitpoints(index);
//						int maxHp = ElementKeyMap.infoArray[type].getMaxHitPoints();
//						if (getHitpoints(index) > maxHp) {
//							setHitpoints(index, Math.min(maxHp, hp));
//						}
						byte x = coordX[index];
						byte y = coordY[index];
						byte z = coordZ[index];
						long xAbs = xAbsStart + absCoordX[index];
						long yAbs = yAbsStart + absCoordY[index];
						long zAbs = zAbsStart + absCoordZ[index];

						onAddingElementUnsynched(index, x, y, z, (short) type, false, true, xAbs + yAbs + zAbs, time);
						assert ((size >= 0 && size <= BLOCK_COUNT)) : "Exception WARNING: SEGMENT SIZE WRONG " + size + " " + (segment != null ? (segment.getSegmentController().getState() + ": " + segment.getSegmentController() + " " + segment) : "");

					} else {
						setType(index, Element.TYPE_NONE);
					}
				}
			}

			assert (isBBValid()) : this.getClass().getSimpleName() + ": Pos: " + getSegmentPos() + "; size " + size + "; minmax " + min + ", " + max;

			revalidating = false;

			segment.dataChanged(true);
		} catch(SegmentDataWriteException e) {
			throw new RuntimeException("Should never happen here", e);
		} finally {
			rwl.writeLock().unlock();
		}
	}

	public void revalidateMeta(SegmentDataMetaData buildMetaData) {
		if(DEBUG) {
			try {
				throw new Exception("BUILT META " + segment.pos);
			} catch(Exception r) {
				errors2 = new StringWriter();
				r.printStackTrace(new PrintWriter(errors2));
			}
		}
		rwl.writeLock().lock();
		try {
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

		} finally {
			needsRevalidate = false;
			revalidating = false;
			rwl.writeLock().unlock();
		}
	}

	public boolean revalidateSuccess() {
		for(byte z = 0; z < SegmentData.SEG; z++) {
			for(byte y = 0; y < SegmentData.SEG; y++) {
				for(byte x = 0; x < SegmentData.SEG; x++) {
					short type = getType(x, y, z);
					if(type != Element.TYPE_NONE && !ElementKeyMap.exists(type)) {
						System.err.println("FAILED: " + type + "; " + x + ", " + y + ", " + z);
						return false;
					}
				}
			}
		}
		return true;
	}

	public abstract void serialize(DataOutput outputStream) throws IOException;

	protected void writeAs3Byte(DataOutput outputStream, int[] data) throws IOException {
		for(int i = 0; i < data.length; i++) {
			int value = data[i];
			outputStream.writeByte((byte) (value & 0xFF));
			outputStream.writeByte((byte) ((value >> 8) & 0xFF));
			outputStream.writeByte((byte) ((value >> 16) & 0xFF));
		}
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
	public void setInfoElement(byte x, byte y, byte z, short newType, byte orientation, byte activation, short hitpoints, boolean updateSegmentBB, long absIndex, long time) throws SegmentDataWriteException {
		rwl.writeLock().lock();
		try {
			setInfoElementUnsynched(x, y, z, newType, orientation, activation, hitpoints, updateSegmentBB, absIndex, time);
		} finally {
			rwl.writeLock().unlock();
		}

	}

	public void setInfoElement(byte x, byte y, byte z, short type, boolean updateSegmentBB, long absIndex, long time) throws SegmentDataWriteException {
		setInfoElement(x, y, z, type, (byte) -1, (byte) -1, type == 0 ? 0 : ElementKeyMap.MAX_HITPOINTS, updateSegmentBB, absIndex, time);
	}

	public void setInfoElement(Vector3b pos, short type, boolean updateSegmentBB, long absIndex, long time) throws SegmentDataWriteException {

		//		System.err.println("setting info at "+pos+", Element: "+e.x+", "+e.y+", "+e.z+", index: "+getInfoIndex(pos.x, pos.y, pos.z, Element.HALF_SIZE));
		setInfoElement(pos.x, pos.y, pos.z, type, updateSegmentBB, absIndex, time);
	}

	public void setInfoElement(Vector3b pos, short type,
	                           byte elementOrientation, byte activation, boolean updateSegmentBB, long absIndex, long time) throws SegmentDataWriteException {
		setInfoElement(pos.x, pos.y, pos.z, type, elementOrientation, activation, type == 0 ? 0 : ElementKeyMap.MAX_HITPOINTS, updateSegmentBB, absIndex, time);

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
	public void setInfoElementForcedAddUnsynched(byte x, byte y, byte z, short newType, boolean updateSegmentBB) throws SegmentDataWriteException {
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
	public void setInfoElementForcedAddUnsynched(byte x, byte y, byte z, short newType, byte orientation, byte activation, boolean updateSegmentBB) throws SegmentDataWriteException {
		int index = getInfoIndex(x, y, z);

		if(newType == Element.TYPE_NONE) {
			setType(index, Element.TYPE_NONE);
			setOrientation(index, (byte) 0);
			setActive(index, false);
			setHitpointsByte(index, 0);
			blockAddedForced = true;
			return;
		}

		setType(index, newType);

		if(orientation > -1) {
			setOrientation(index, orientation);
		}
		if(activation > -1) {
			setActive(index, activation != 0);
		}

		this.blockAddedForced = true;

		setHitpointsByte(index, ElementKeyMap.MAX_HITPOINTS);
	}

	public void setInfoElementForcedAddUnsynched(int dataIndex, int newData) throws SegmentDataWriteException {
		throw new SegmentDataWriteException(this);
	}

	public void setInfoElementUnsynched(byte x, byte y, byte z, short newType, boolean updateSegmentBB, long absIndex, long time) throws SegmentDataWriteException {
		setInfoElementUnsynched(x, y, z, newType, (byte) -1, (byte) -1, updateSegmentBB, absIndex, time);
	}

	public void setInfoElementUnsynched(byte x, byte y, byte z, short newType, byte orientation, byte activation, boolean updateSegmentBB, long absIndex, long time) throws SegmentDataWriteException {

		setInfoElementUnsynched(x, y, z, newType, orientation, activation,
				(ElementKeyMap.isValidType(newType)) ? ElementKeyMap.MAX_HITPOINTS : 0, updateSegmentBB, absIndex, time);
	}

	public void setInfoElementUnsynched(byte x, byte y, byte z, short newType, byte orientation, byte activation, short hitpoints, boolean updateSegmentBB, long absIndex, long time) throws SegmentDataWriteException {
		int index = getInfoIndex(x, y, z);
		short oldType = getType(index);
		byte oldOrientation = getOrientation(index);

		setType(index, newType);
		if(orientation > -1) {
			setOrientation(index, orientation);
		}
		boolean wasActive = isActive(index);
		if(activation > -1) {
			setActive(index, activation != 0);
		}
		assert (this.segment != null && this.segment.getSegmentData() == this);

		if(newType == Element.TYPE_NONE) {
			setActive(index, false);
			setOrientation(index, (byte) 0);
			// an existing element was set to NULL
			if(oldType != Element.TYPE_NONE) {
				onRemovingElement(index, x, y, z, oldType, true, updateSegmentBB, oldOrientation, wasActive, false, time, false);
			}

		} else {
			// a NULL element was set to existing
			if(oldType == Element.TYPE_NONE) {
				onAddingElementUnsynched(index, x, y, z, newType, updateSegmentBB, true, absIndex, time);

				//set hitpoints
				setHitpointsByte(index, hitpoints);
			} else {
				// an element has been changed (e.g. active)
				ElementInformation.updatePhysical(newType, oldType, wasActive, x, y, z, octree, this, index);
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
	public void setInfoElementUnsynched(Vector3b pos, short type, boolean updateSegmentBB, long absIndex, long time) throws SegmentDataWriteException {
		setInfoElementUnsynched(pos.x, pos.y, pos.z, type, updateSegmentBB, absIndex, time);
	}

	public void setNeedsRevalidate(boolean needsRevalidate) {
		this.needsRevalidate = needsRevalidate;
	}

	public void incSize() {
		this.size++;
		assert ((size >= 0 && size <= BLOCK_COUNT)) : "Exception WARNING: SEGMENT SIZE WRONG " + size + " " + (segment != null ? (segment.getSegmentController().getState() + ": " + segment.getSegmentController() + " " + segment) : "");

		if(segment != null) {
			segment.setSize(this.size);
		}
	}

	private void updateBBAdd(byte x, byte y, byte z, int baseIndex) {
		min.x = (byte) Math.min(min.x, x);
		min.y = (byte) Math.min(min.y, y);
		min.z = (byte) Math.min(min.z, z);
		max.x = (byte) Math.max(max.x, x + 1);
		max.y = (byte) Math.max(max.y, y + 1);
		max.z = (byte) Math.max(max.z, z + 1);
//		if (x >= max.x) {
//			max.x = (byte) (x + 1);
//		}
//		if (y >= max.y) {
//			max.y = (byte) (y + 1);
//		}
//		if (z >= max.z) {
//			max.z = (byte) (z + 1);
//		}
//
//		if (x < min.x) {
//			min.x = x;
//		}
//		if (y < min.y) {
//			min.y = y;
//		}
//		if (z < min.z) {
//			min.z = z;
//		}
		octree.insertAABB16(x, y, z, baseIndex);

	}

	private void updateBBRemove(byte x, byte y, byte z, boolean updateSegmentBB, int baseIndex) {
		if(size == 0) {
			if(updateSegmentBB) {
				max.set(Byte.MIN_VALUE, Byte.MIN_VALUE, Byte.MIN_VALUE);
				min.set(Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MAX_VALUE);
				octree.resetAABB16();
				restructBB(updateSegmentBB);
				getSegmentController().getSegmentBuffer().restructBBFast(this);
			}
		} else {
			if(x + 1 < max.x && y + 1 < max.y && z + 1 < max.z &&
					x > min.x && y > min.y && z > min.z) {
				//removed from middle -> nothing to do
				//					System.err.println("UPDATED FROM MIDDLE: NOTHING TO DO "+x+", "+y+", "+z+"; BB ("+min+"; "+max+")");
			} else {
				if(updateSegmentBB) {
					restructBB(updateSegmentBB);
				}
			}
		}
	}

	public void writeSingle(int index, DataOutput stream) throws IOException {
//		stream.write(data, index, 3);
//		//		stream.writeByte(data[index]);
//		//		stream.writeByte(data[index+1]);
//		//		stream.writeByte(data[index+2]);
	}

	public void readSingle(int index, DataInput in) throws IOException {
//		in.read(data, index, 3);
//		//		data[index] = in.readByte();
//		//		data[index+1] = in.readByte();
//		//		data[index+2] = in.readByte();
	}

	/**
	 * @return the blockAddedForced
	 */
	public boolean isBlockAddedForced() {
		return blockAddedForced;
	}

	/**
	 * Set this in a generator, if the chunk is non empty and
	 * needs revalidation
	 *
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
	public static final int lodDataSize = (4 + 3) * lodLightNum; //4 for color, 3 for dir, times amount of lights
	public static final byte FULL_ORIENT = 24;
	public static final short HP_MAX = 128;
	public static final int BYTES_USED = 3;
	public static final int TOTAL_SIZE_BYTES = TOTAL_SIZE * ByteUtil.SIZEOF_INT;

	public float[] getLodData() {
		return lodData;
	}

	public void calculateLodLight(int lodIndex, int infoIndex, MemFloatArray sideData, MemFloatArray lodData, MemShortArray lodTypeAndOrientcubeIndex) {
		int startIndex = (lodIndex * 6 * 4);

		int tAndOStartIndex = lodIndex * 2;
		byte localAlgoIndex = BlockShapeAlgorithm.getLocalAlgoIndex(6, getOrientation(infoIndex));
		short type = getType(infoIndex);
		lodTypeAndOrientcubeIndex.put(tAndOStartIndex + 0, type);
		lodTypeAndOrientcubeIndex.put(tAndOStartIndex + 1, localAlgoIndex);

		Oriencube orientcube = (Oriencube) BlockShapeAlgorithm.algorithms[5][localAlgoIndex];
		if(type != 0 && ElementKeyMap.getInfoFast(type).getId() == 104) {
			int o = localAlgoIndex % 6;
			orientcube = Oriencube.getOrientcube(
					o, o > 1 ? Element.FRONT : Element.TOP);
		}
		byte prim = orientcube.getOrientCubePrimaryOrientation();
		byte primOpp = (byte) Element.getOpposite(prim);

		int lightStartIndexTot = lodIndex * lodDataSize;

		int index = 0;
		for(int i = 0; i < 6; i++) {
			if(i != prim && i != primOpp) {

				int lightStartIndex = lightStartIndexTot + index * (4 + 3);
				int posIndex = lightStartIndex + 4;

				lodData.put(lightStartIndex + 0, 0);
				lodData.put(lightStartIndex + 1, 0);
				lodData.put(lightStartIndex + 2, 0);
				lodData.put(lightStartIndex + 3, 0);

				float coloring = 0;

				lodData.put(posIndex + 0, Element.DIRECTIONSf[prim].x + Element.DIRECTIONSf[i].x);
				lodData.put(posIndex + 1, Element.DIRECTIONSf[prim].y + Element.DIRECTIONSf[i].y);
				lodData.put(posIndex + 2, Element.DIRECTIONSf[prim].z + Element.DIRECTIONSf[i].z);

				int sideDataStartIndex = startIndex + i * 4;

				if(sideData.get(sideDataStartIndex) >= 0) {
					lodData.put(lightStartIndex + 0, sideData.get(sideDataStartIndex + 0));
					lodData.put(lightStartIndex + 1, sideData.get(sideDataStartIndex + 1));
					lodData.put(lightStartIndex + 2, sideData.get(sideDataStartIndex + 2));
					lodData.put(lightStartIndex + 3, sideData.get(sideDataStartIndex + 3));
					coloring++;
				}

				int sideDataStartIndexPrim = startIndex + prim * 4;

				float primFac = 0.01f;
				if(sideData.get(sideDataStartIndexPrim) >= 0) {
					lodData.add(lightStartIndex + 0, sideData.get(sideDataStartIndexPrim + 0) * primFac);
					lodData.add(lightStartIndex + 1, sideData.get(sideDataStartIndexPrim + 1) * primFac);
					lodData.add(lightStartIndex + 2, sideData.get(sideDataStartIndexPrim + 2) * primFac);
					lodData.add(lightStartIndex + 3, sideData.get(sideDataStartIndexPrim + 3) * primFac);
					coloring += primFac;
				}
				if(coloring > 0f) {
					float f = 1f / coloring;
					lodData.scale(lightStartIndex + 0, f);
					lodData.scale(lightStartIndex + 1, f);
					lodData.scale(lightStartIndex + 2, f);
					lodData.scale(lightStartIndex + 3, f);
				}
				index++;

//				System.err.println("LIGHT COL OF "+lightPos+" ---> "+lightCol);
			}
		}
		assert (index == 4);
	}

	public short[] getLodTypeAndOrientcubeIndex() {
		return lodTypeAndOrientcubeIndex;
	}

	public IntArrayList drawingLodShapes;

	public void loadLodFromContainer(
			CubeMeshBufferContainer currentBufferContainer) {

		if(currentBufferContainer.lodShapes.size() > 0) {
			if(drawingLodShapes == null) {
				drawingLodShapes = new IntArrayList();
			}
			int wantedSize = currentBufferContainer.lodShapes.size() * lodDataSize;
			int wantedTnOSize = currentBufferContainer.lodShapes.size() * 2;

			if(lodData == null || lodData.length != wantedSize) {
				lodData = currentBufferContainer.lodData.toArray(wantedSize);
				lodTypeAndOrientcubeIndex = currentBufferContainer.lodTypeAndOrientcubeIndex.toArray(wantedTnOSize);

			} else {
				for(int i = 0; i < wantedSize; i++) {
					lodData[i] = currentBufferContainer.lodData.get(i);
				}
				for(int i = 0; i < wantedTnOSize; i++) {
					lodTypeAndOrientcubeIndex[i] = currentBufferContainer.lodTypeAndOrientcubeIndex.get(i);
				}
			}
			drawingLodShapes.clear();
			drawingLodShapes.addAll(currentBufferContainer.lodShapes);
		} else {
			drawingLodShapes = null;
			lodData = null;
		}
	}

	@Override
	public Vector3i getSegmentPos() {
		return segment.pos;
	}

	public abstract int getDataAt(int infoIndex);

	public static int convertIntValueDirect(Chunk16SegmentData f, int index) {
		int dataIndex = index;
		short type = f.getType(dataIndex);
		byte orientation = f.getOrientation(dataIndex);
		boolean active = f.isActive(dataIndex);
		short hitpoints = f.getHitpointsByte(dataIndex);

		int r = 0;

		r = putType(r, type);
		r = putHitpoints(r, (byte) hitpoints);
		r = putActivation(r, active);
		r = putOrientationInt(r, (byte) SegmentData3Byte.convertOrient(type, orientation, active));

		return r;
	}

	public static int convertIntValue(Chunk16SegmentData f, int index) {
		int dataIndex = index * 3;
		short type = f.getType(dataIndex);
		byte orientation = f.getOrientation(dataIndex);
		boolean active = f.isActive(dataIndex);
		short hitpoints = f.getHitpointsByte(dataIndex);

		int r = 0;

		r = putType(r, type);
		r = putHitpoints(r, (byte) hitpoints);
		r = putActivation(r, active);
		r = putOrientationInt(r, (byte) SegmentData3Byte.convertOrient(type, orientation, active));

		return r;
	}

	public static int makeDataInt(short type, byte orientation, boolean isActive, byte hitpointsByte) {
//		int data = 0;
//		data = putType(data, type);
//		data = putOrientationInt(data, orientation);
//		data = putActivation(data, isActive);
//		data = putHitpoints(data, hitpointsByte);
//
//		return data;
		return SegmentData4Byte.makeDataInt(type, orientation, isActive, hitpointsByte);
	}

	public static int makeDataInt(short type, byte orientation, boolean isActive) {
		return makeDataInt(type, orientation, isActive, ElementKeyMap.MAX_HITPOINTS);
	}

	public static int makeDataInt(short type, byte orientation) {
		return makeDataInt(type, orientation, ElementKeyMap.getInfo(type).activateOnPlacement());
	}

	public static int makeDataInt(short type, boolean isActive) {
		return makeDataInt(type, (byte) 0, isActive);
	}

	public static int makeDataInt(short type) {
		if(type == Element.TYPE_NONE) return makeDataInt(Element.TYPE_NONE, (byte) 0, false, (byte) 0);
		else return makeDataInt(type, (byte) 0, ElementKeyMap.getInfo(type).activateOnPlacement());
	}

	public static int convert3ByteToIntValue(byte a, byte b, byte c) {

//		if(( (((int)a) | ((b) << 8) | ((c) << 16)) != ( ((int)a) + ((b) << 8) + ((c) << 16)))){
//			System.err.println("A: "+a+": "+Integer.toBinaryString((int)a));
//			System.err.println("B: "+b+" << 8: "+Integer.toBinaryString(((b) << 8)));
//			System.err.println("C: "+c+" << 16: "+Integer.toBinaryString(((c) << 16)));
//		
//		}
//		
//		assert( ((a) | ((b) << 8) | ((c) << 16)) == ( (a) + ((b) << 8) + ((c) << 16)));

		return (a & 0xFF) | ((b & 0xFF) << 8) | ((c & 0xFF) << 16);
	}

	public static byte[] convertIntTo3Byte(int value, byte[] out) {
		out[0] = (byte) (value);
		out[1] = (byte) (value >> 8);
		out[2] = (byte) (value >> 16);
		return out;
	}

	@Override
	public boolean isIntDataArray() {
		return true;
	}

	public abstract byte[] getAsByteBuffer(byte[] out);

	@Override
	public int inflate(Inflater inflater, byte[] in)
			throws SegmentInflaterException, DataFormatException, SegmentDataWriteException {
		throw new SegmentDataWriteException(this);
	}

	public void checkWritable() throws SegmentDataWriteException {
	}

	public abstract SegmentDataType getDataType();

	public int serializeRemoteSegment(DataOutput stream) throws IOException {
		throw new IllegalArgumentException("Only special types of segment data implement this");
	}

	public void deserializeRemoteSegment(DataInput dataInputStream) throws IOException {
		throw new IllegalArgumentException("Only special types of segment data implement this");
	}

	/**
	 * since for a normal array the function is going to return the array itself.
	 * make sure not to write to the returned value
	 *
	 * @param out
	 * @return
	 */
	public abstract int[] getAsIntBuffer(int[] out);

	public abstract MemIntArray getAsIntBuffer(MemIntArray out);

	public int countBruteForce() {
		int count = 0;
		for(int i = 0; i < TOTAL_SIZE; i++) {
			if(containsFast(i)) {
				count++;
			}
		}
		return count;

	}

	public void repairAll() {
	}

	public void copyTo(SegmentDataInterface to) {
		try {
			for(int i = 0; i < TOTAL_SIZE; i++) {
				to.setDataAt(i, getDataAt(i));
			}
		} catch(SegmentDataWriteException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	protected abstract void copyTo(ByteBuffer buffer);

	@Override
	public int readFrom(ByteBuffer uncompressed) throws SegmentDataWriteException {
		throw new SegmentDataWriteException(this);
	}

	public void fill(LongArrayList cache, int x, int y, int z) {
		for(int i = 0; i < SegmentData.BLOCK_COUNT; i++) {
			if(contains(i)) {
				cache.add(getLongIndex(i, x, y, z));
			}
		}
	}

}
