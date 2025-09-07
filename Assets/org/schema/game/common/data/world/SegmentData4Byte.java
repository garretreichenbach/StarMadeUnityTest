package org.schema.game.common.data.world;

import api.DebugFile;
import api.utils.game.BlueprintModMappings;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.MemoryManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.octree.ArrayOctree;
import org.schema.game.common.data.world.nat.terra.server.MemoryArea;
import org.schema.game.server.data.blueprint.SegmentControllerOutline;
import org.schema.game.server.data.blueprintnw.BlueprintEntry;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class SegmentData4Byte extends SegmentData {

	public static final boolean USE_COMPRESSION_CHECK = true;
	public static final int BYTES_USED = 4;
	public static final int BITS_USED = BYTES_USED * 8;
	//Block Type [13 bits]
	public static final int typeBits = 13;
	public static final int typeIndexStart = 0;
	public static final int typeMask = ((~0) >>> (BITS_USED - typeBits)) << typeIndexStart;
	public static final int typeMaskNot = ~typeMask;
	//HP [7 bits]
	public static final int hpBits = 7;
	public static final int hpIndexStart = typeIndexStart + typeBits;
	public static final int hpMask = ((~0) >>> (BITS_USED - hpBits)) << hpIndexStart;
	public static final int hpMaskNot = ~hpMask;
	public static final int activationIndexStart = hpIndexStart + hpBits;
	//Activation [1 bit]
	public static final int activationBits = 1;
	public static final int activationMask = ((~0) >>> (BITS_USED - activationBits)) << activationIndexStart;
	public static final int activationMaskNot = ~activationMask;
	public static final int orientationIndexStart = activationIndexStart + activationBits;
	//Orientation [5 bits]
	public static final int orientationBits = 5;
	public static final int orientationMask = ((~0) >>> (BITS_USED - orientationBits)) << orientationIndexStart;
	public static final int orientationMaskNot = ~orientationMask;
	public static final int extraIndexStart = orientationIndexStart + orientationBits;
	//Extra [6 bits]
	//These are unused bits that can be used for future purposes
	public static final int extraBits = 6;
	public static final int extraMask = ((~0) >>> (BITS_USED - extraBits)) << extraIndexStart;
	public static final int extraMaskNot = ~extraMask;
	private static final ObjectArrayList<IntOpenHashSet> typeMapPool = new ObjectArrayList<>();
	private final MemoryArea memoryArea;
	public NativeMemoryManager memoryManager = NativeMemoryManager.segmentDataManager;
	boolean needsBitmapCompressionCheck;

	public static final int TOTAL_SIZE_BYTES = TOTAL_SIZE * BYTES_USED;

	public SegmentData4Byte() {
		memoryArea = memoryManager.getMemoryArea();
	}

	public SegmentData4Byte(boolean onClient) {
		super(onClient);
		memoryArea = memoryManager.getMemoryArea();
	}

	/**
	 * Migrates from 3-byte data format to 4-byte data.
	 * @param bytes The 3-byte data to migrate.
	 * @param segmentPiece The segment piece to set the migrated data into.
	 */
	public static void migrateFrom3Byte(byte[] bytes, SegmentPiece segmentPiece) {
		assert bytes.length == 3 : new IllegalArgumentException("Expected 3 bytes for migration, got " + bytes.length);
		//Combine the three bytes into an int
		int oldData = convert3ByteToIntValue(bytes[0], bytes[1], bytes[2]);
		//Extract the values using the old masks
		short type = (short) (SegmentData.typeMask & oldData);
		byte orientation = (byte) ((orientMask & oldData) >> orientationStart);
		boolean isActive = (oldData & activeMask) != 0;
		short hp = (short) ((SegmentData.hpMask & oldData) >> hitpointsIndexStart);
		//Set the values in the segment piece
		segmentPiece.setType(type);
		segmentPiece.setOrientation(orientation);
		segmentPiece.setActive(isActive);
		segmentPiece.setHitpointsByte(hp);
	}

	public static int makeDataInt(short type, byte orientation, boolean isActive, byte hitpointsByte) {
		int data = 0;
		data = putType(data, type);
		data = putOrientationInt(data, orientation);
		data = putActivation(data, isActive);
		data = putHitpoints(data, hitpointsByte);
		return data;
	}

	public static int putType(int from, short value) {
		return (from & typeMaskNot) | value;
	}

	public static int putHitpoints(int from, byte value) {
		return (from & hpMaskNot) | (value << hpIndexStart);
	}

	public static int putActivation(int from, boolean value) {
		return value ? (from | activationMask) : (from & activationMaskNot);
	}

	public static int putOrientationInt(int from, byte value) {
		return (from & orientationMaskNot) | (value << orientationIndexStart);
	}

	public SegmentData4Byte(SegmentData segmentData) {
		super(segmentData);
		memoryArea = memoryManager.getMemoryArea();

		try {
			segmentData.checkWritable();
			segmentData.copyTo(this);
		} catch(SegmentDataWriteException exception) {
			exception.printStackTrace();
		}
	}

	public static short getTypeFromIntData(int data) {
		return (short) (typeMask & data);
	}

	public static void migrateFrom6(SegmentData4Byte segmentData, SegmentDataIntArray oldData) {
		for(int i = 0; i < TOTAL_SIZE; i++) {
			try {
				segmentData.setType(i, oldData.getType(i));
				segmentData.setHitpointsByte(i, oldData.getHitpointsByte(i));
				segmentData.setActive(i, oldData.isActive(i));
				segmentData.setOrientation(i, oldData.getOrientation(i));
			} catch(Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	public static void freeStaticTypeMap(IntOpenHashSet map) {
		map.clear();
		synchronized(typeMapPool) {
			typeMapPool.add(map);
		}
	}

	public static IntOpenHashSet getStaticTypeMap() {
		synchronized(typeMapPool) {
			if(typeMapPool.isEmpty()) return new IntOpenHashSet(16);
			else return typeMapPool.remove(typeMapPool.size() - 1);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		memoryManager.freeArea(memoryArea);
	}

	@Override
	public void translateModBlocks() throws SegmentDataWriteException {
		Segment segment = this.segment;
		if(segment == null) {
			System.err.println("Outdated blueprint, not translating mod blocks");
			return;
		}

		String uid = segment.getSegmentController().getUniqueIdentifier();
		for(SegmentControllerOutline<?> outline : SegmentControllerOutline.shipUidsToConvert) {
			if(outline.uniqueIdentifier.equals(uid)) {
				SegmentControllerOutline.shipUidsToConvert.remove(outline);
				assert outline.en instanceof BlueprintEntry : "Tried to check old blueprint";
				BlueprintEntry entry = (BlueprintEntry) outline.en;
				for(int i = 0; i < TOTAL_SIZE; i++) {
					short oldId = getType(i);
					if(oldId != 0) {
						short newId = entry.getModMappings().translateId(oldId);
						if(newId != oldId) System.err.println("TRANSLATE: " + oldId + " to " + newId);
						setType(i, newId);
					}
				}
				System.err.println("MM: " + entry.getModMappings());
				System.err.println("MMF: " + entry.getModMappingsFile());
				System.err.println("Name: " + entry.getName());
				entry.getModMappings().printState();
				DebugFile.info("> Current State: ");
				BlueprintModMappings.getCurrent().printState();
				break;
			}
		}
	}

	@Override
	public void migrateTo(int fromVersion, SegmentDataInterface segmentData) {
		if(fromVersion == 6) migrateFrom6(this, (SegmentDataIntArray) segmentData);
		else throw new UnsupportedOperationException("Migrate from version " + fromVersion + " to " + getClass().getSimpleName() + " is not supported");
	}

	@Override
	public void setType(int index, short newType) throws SegmentDataWriteException {
		memoryArea.writeIntIndex(index, (memoryArea.readIntIndex(index) & typeMaskNot) | newType);
	}

	@Override
	public short getType(int index) {
		return (short) (typeMask & memoryArea.readIntIndex(index));
	}

	@Override
	public void setHitpointsByte(int index, int value) throws SegmentDataWriteException {
		value = (short) Math.min(127, value);
		memoryArea.writeIntIndex(index, (memoryArea.readIntIndex(index) & hpMaskNot) | value << hpIndexStart);
	}

	@Override
	public short getHitpointsByte(int index) {
		return (short) ((hpMask & memoryArea.readIntIndex(index)) >> hpIndexStart);
	}

	@Override
	public void setActive(int index, boolean active) throws SegmentDataWriteException {
		memoryArea.writeIntIndex(index, (memoryArea.readIntIndex(index) & activationMaskNot) | (active ? 1 : 0) << activationIndexStart);
	}

	@Override
	public boolean isActive(int index) {
		return (memoryArea.readIntIndex(index) & activationMask) != 0;
	}

	@Override
	public void setOrientation(int index, byte orientation) throws SegmentDataWriteException {
		memoryArea.writeIntIndex(index, (memoryArea.readIntIndex(index) & orientationMaskNot) | orientation << orientationIndexStart);
	}

	@Override
	public byte getOrientation(int index) {
		return (byte) ((orientationMask & memoryArea.readIntIndex(index)) >> orientationIndexStart);
	}

	@Override
	public void setExtra(int index, byte extra) {
		memoryArea.writeIntIndex(index, (memoryArea.readIntIndex(index) & extraMaskNot) | extra << extraIndexStart);
	}

	@Override
	public int getExtra(int index) {
		return (extraMask & memoryArea.readIntIndex(index)) >> extraIndexStart;
	}

	@Override
	public void resetFast() throws SegmentDataWriteException {
		super.resetFast();
		memoryArea.fillFully((byte) 0);
	}

	@Override
	public boolean containsFast(int index) {
		return (memoryArea.readIntIndex(index) & typeMask) != 0;
	}

	@Override
	public void deserialize(DataInput inputStream, long time) throws IOException, SegmentDataWriteException {
		rwl.writeLock().lock();
		try {
			reset(time);
			for(int i = 0; i < TOTAL_SIZE; i++) {
				int data = inputStream.readInt();
				memoryArea.writeIntIndex(i, data);
			}
			setNeedsRevalidate(true);
		} finally {
			rwl.writeLock().unlock();
		}
	}

	@Override
	public void serialize(DataOutput outputStream) throws IOException {
		try {
			for(int i = 0; i < TOTAL_SIZE; i++) outputStream.writeInt(memoryArea.readIntIndex(i));
		} finally {
			rwl.readLock().unlock();
		}
	}

	@Override
	protected ArrayOctree getOctreeInstance(boolean onServer) {
		return new ArrayOctree(onServer);
	}

	/**
	 * This method will NOT update any adding or removing of segments. You must do that yourself!
	 */
	@Override
	public int applySegmentData(byte x, byte y, byte z, int pieceData, int offset, final boolean synched, long absIndex, boolean updateRemoveBB, boolean updateSegmentBB, long time) throws SegmentDataWriteException {
		return applySegmentData(x, y, z, pieceData, offset, synched, absIndex, updateRemoveBB, updateSegmentBB, time, false);
	}

	@Override
	public int applySegmentData(byte x, byte y, byte z, int pieceData, int offset, final boolean synched, long absIndex, boolean updateRemoveBB, boolean updateSegmentBB, long time, boolean preserveControl) throws SegmentDataWriteException {
		if(synched) {
			rwl.writeLock().lock();
		}
		try {
			int index = getInfoIndex(x, y, z);
			boolean oldActive = isActive(index);
			short oldType = getType(index);
			byte oldOrientation = getOrientation(index);
			memoryArea.writeIntIndex(index + offset, pieceData);

			short newType = getType(index);
			short newHP = getHitpointsByte(index);

			if(newType == Element.TYPE_NONE && oldType == Element.TYPE_NONE) return PIECE_UNCHANGED;
			if(newType != oldType) {
				if(oldType == Element.TYPE_NONE) {
					onAddingElement(index, x, y, z, newType, updateSegmentBB, synched, absIndex, time);
					return PIECE_ADDED;
				}
				if(newType == Element.TYPE_NONE) {
					onRemovingElement(index, x, y, z, oldType, updateRemoveBB, updateSegmentBB, oldOrientation, oldActive, synched, time, preserveControl);
					return PIECE_REMOVED;
				}
				onRemovingElement(index, x, y, z, oldType, updateRemoveBB, updateSegmentBB, oldOrientation, oldActive, synched, time, preserveControl);
				onAddingElement(index, x, y, z, newType, updateSegmentBB, synched, absIndex, time);
				return PIECE_CHANGED;
			} else {
				short oldHP = getHitpointsByte(index);
				if(oldHP != newHP) return PIECE_CHANGED;
				else {
					boolean nowActive = isActive(index);
					if(oldActive != nowActive) {
						if(ElementKeyMap.isDoor(newType)) {
							if(nowActive) {
								assert (newType != ElementKeyMap.PICKUP_RAIL);
								octree.insert(x, y, z, index);
							} else octree.delete(x, y, z, index, newType);
						}
						return PIECE_ACTIVE_CHANGED;
					} else if(oldOrientation != getOrientation(index)) return PIECE_CHANGED;
					return PIECE_UNCHANGED;
				}
			}
		} finally {
			if(synched) rwl.writeLock().unlock();
		}
	}

	@Override
	public byte[] getAsByteBuffer(byte[] out) {
		memoryArea.readAllBytes(0, out);
		return out;
	}

	@Override
	public void setInfoElementForcedAddUnsynched(int dataIndex, int newData) throws SegmentDataWriteException {
		memoryArea.writeIntIndex(dataIndex, newData);
		setBlockAddedForced(true);
	}

	@Override
	public int getDataAt(int infoIndex) {
		return memoryArea.readIntIndex(infoIndex);
	}

	/**
	 * This method is used to inflate the given byte array using the provided Inflater.
	 * The byte array is expected to contain the compressed data of the segment.
	 * The method will throw a SegmentInflaterException if the inflated size does not match the expected size (TOTAL_SIZE * BYTES_USED).
	 * It will also throw a DataFormatException if the provided data is in an incorrect format.
	 * After inflating the data, it is written into the memory area of the segment.
	 * If there are any mod blocks present in the data, they are translated using the translateModBlocks method.
	 *
	 * @param inflater The Inflater to use for inflating the data.
	 * @param in       The byte array containing the compressed data.
	 * @return The number of bytes produced by the inflation.
	 * @throws SegmentInflaterException  If the inflated size does not match the expected size.
	 * @throws DataFormatException       If the provided data is in an incorrect format.
	 * @throws SegmentDataWriteException If an error occurs while writing the inflated data into the memory area.
	 */
	@Override
	public int inflate(Inflater inflater, byte[] in) throws SegmentInflaterException, DataFormatException, SegmentDataWriteException {
		int inflate = inflater.inflate(in);
		if(inflate != TOTAL_SIZE * BYTES_USED) throw new SegmentInflaterException(inflate, TOTAL_SIZE * BYTES_USED);
		int dIndex = 0;
		for(int i = 0; i < BLOCK_COUNT; i++) {
			memoryArea.writeIntIndex(i, (in[dIndex] & 0xFF) | (in[dIndex + 1] & 0xFF) << 8 | (in[dIndex + 2] & 0xFF) << 16 | (in[dIndex + 3] & 0xFF) << 24);
			dIndex += BYTES_USED;
		}
		try {
			translateModBlocks();
		} catch(SegmentDataWriteException exception) {
			exception.printStackTrace();
		}
		return inflate;
	}

	@Override
	public int readFrom(ByteBuffer buffer) throws SegmentDataWriteException {
		memoryArea.putAll(buffer);
		return TOTAL_SIZE;
	}

	@Override
	public void getBytes(int dataIndex, byte[] bytes) {
		int data = memoryArea.readIntIndex(dataIndex);
		bytes[0] = (byte) (data & 0xFF);
		bytes[1] = (byte) ((data >>> 8) & 0xFF);
		bytes[2] = (byte) ((data >>> 16) & 0xFF);
		bytes[3] = (byte) ((data >>> 24) & 0xFF);
	}

	@Override
	public void repairAll() {
		try {
			for(int i = 0; i < TOTAL_SIZE; i++) {
				short type = getType(i);
				if(ElementKeyMap.isValidType(type)) setHitpointsByte(i, ElementKeyMap.MAX_HITPOINTS);
			}
		} catch(SegmentDataWriteException exception) {
			exception.printStackTrace();
		}
	}

	@Override
	public SegmentDataType getDataType() {
		return SegmentDataType.FOUR_BYTE;
	}

	@Override
	public int[] getAsIntBuffer(int[] out) {
		for(int i = 0; i < out.length; i++) out[i] = memoryArea.readIntIndex(i);
		return out;
	}

	@Override
	public MemoryManager.MemIntArray getAsIntBuffer(MemoryManager.MemIntArray out) {
		out.copyFrom(memoryArea);
		out.clear();
		return out;
	}

	@Override
	public SegmentData doBitmapCompressionCheck(RemoteSegment seg) {
		if(USE_COMPRESSION_CHECK && needsBitmapCompressionCheck) {
			needsBitmapCompressionCheck = false;
			IntOpenHashSet typeMap = getStaticTypeMap();
			for(int i = 0; i < TOTAL_SIZE && typeMap.size() <= 16; i++) typeMap.add(getType(i));
			if(typeMap.size() <= 16) {
				int[] blockTypeData = new int[typeMap.size()];
				int i = 0;
				for(int dat : typeMap) {
					blockTypeData[i] = dat;
					i++;
				}
				return new SegmentDataBitMap(!onServer, blockTypeData, this);
			}
			freeStaticTypeMap(typeMap);
		}
		return this;
	}

	@Override
	public void setDataAt(int i, int data) throws SegmentDataWriteException {
		memoryArea.writeIntIndex(i, data);
		setBlockAddedForced(true);
	}

	@Override
	protected void copyTo(ByteBuffer buffer) {
		memoryArea.copyTo(buffer);
	}
}
