package org.schema.game.common.data.world;

import api.DebugFile;
import api.utils.game.BlueprintModMappings;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.MemoryManager.MemIntArray;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;
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


public class SegmentDataIntArray extends SegmentData {


	private static final ObjectArrayList<IntOpenHashSet> typeMapPool = new ObjectArrayList<>();
	private static final boolean USE_COMPRESSION_CHECK = true;

	private final MemoryArea data;
	public NativeMemoryManager nManager = NativeMemoryManager.segmentDataManager;
	boolean needsBitmapCompressionCheck;

	public SegmentDataIntArray() {
		data = nManager.getMemoryArea();
	}

	public SegmentDataIntArray(boolean onClient) {
		super(onClient);
		data = nManager.getMemoryArea();
	}

	public SegmentDataIntArray(SegmentData segmentData) {
		super(segmentData);
		data = nManager.getMemoryArea();

		try {
			segmentData.checkWritable();
			segmentData.copyTo(this);
		} catch(SegmentDataWriteException e) {
			e.printStackTrace();
			throw new RuntimeException("needs to be implemented for other versions " + segmentData.getClass(), e);
		}
	}

	public static void freeStaticTypeMap(IntOpenHashSet m) {
		m.clear();
		synchronized(typeMapPool) {
			typeMapPool.add(m);
		}
	}

	public static IntOpenHashSet getStaticTypeMap() {
		synchronized(typeMapPool) {
			if(typeMapPool.isEmpty()) {
				return new IntOpenHashSet(16);
			} else {
				return typeMapPool.remove(typeMapPool.size() - 1);
			}
		}
	}

	@Override
	protected void finalize() throws Throwable {
		nManager.freeArea(data);
	}

	//INSERTED CODE
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
				for(int i = 0; i < SegmentData.TOTAL_SIZE; i++) {
					short oldId = getType(i);
					if(oldId != 0) {
						short newId = entry.getModMappings().translateId(oldId);
						if(newId != oldId) {
							System.err.println("TRANSLATE: " + oldId + " to " + newId);
						}
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
	public void setType(int index, short newType) throws SegmentDataWriteException {
		data.writeIntIndex(index, (data.readIntIndex(index) & typeMaskNot) | newType);
	}

	@Override
	public void setHitpointsByte(int index, int value) throws SegmentDataWriteException {
		assert (value >= 0 && value < 128) : value;
		value = (short) Math.min(127, value);
		data.writeIntIndex(index, (data.readIntIndex(index) & hpMaskNot) | (value << hitpointsIndexStart));
	}

	@Override
	public void setActive(int index, boolean active) throws SegmentDataWriteException {
		data.writeIntIndex(index, active ? (data.readIntIndex(index) | activeMask) : (data.readIntIndex(index) & activeMaskNot));
		assert (active == isActive(index)) : active + "; " + isActive(index);
	}

	@Override
	public void setOrientation(int index, byte value) throws SegmentDataWriteException {
		assert (value >= 0 && value < 32) : "NOT A SIDE INDEX " + value;
		data.writeIntIndex(index, (data.readIntIndex(index) & orientMaskNot) | (value << orientationStart));
		assert (value == getOrientation(index)) : "failed orientation coding: " + value + " != result " + getOrientation(index);
	}

	@Override
	public short getType(int index) {
		return (short) (typeMask & data.readIntIndex(index));
	}

	@Override
	public short getHitpointsByte(int index) {
		return (short) ((hpMask & data.readIntIndex(index)) >> hitpointsIndexStart);
	}

	@Override
	public boolean isActive(int index) {
		return (activeMask & data.readIntIndex(index)) > 0;
	}

	@Override
	public void resetFast() throws SegmentDataWriteException {
		super.resetFast();
		data.fillFully((byte) 0);
	}

	@Override
	public boolean containsFast(int index) {
		return (data.readIntIndex(index) & typeMask) != 0;
	}

	@Override
	public byte getOrientation(int index) {
		return (byte) ((orientMask & data.readIntIndex(index)) >> orientationStart);
	}

	@Override
	public void setExtra(int index, byte extra) throws SegmentDataWriteException {
		throw new UnsupportedOperationException("Extra bits are not present in version 6!");
	}

	@Override
	public int getExtra(int index) {
		throw new UnsupportedOperationException("Extra bits are not present in version 6!");
	}

	@Override
	public void deserialize(DataInput inputStream, long time) throws IOException, SegmentDataWriteException {
		rwl.writeLock().lock();
		try {
			reset(time);

			for(int i = 0; i < TOTAL_SIZE * 3; i++) {
				data.writeIntIndex(i, convert3ByteToIntValue(inputStream.readByte(), inputStream.readByte(), inputStream.readByte()));
			}

			setNeedsRevalidate(true);
		} finally {
			rwl.writeLock().unlock();
		}

	}

	@Override
	protected ArrayOctree getOctreeInstance(boolean onServer) {
		return new ArrayOctree(onServer);
	}

	/**
	 * be cautious using that method, because it will NOT update any adding
	 * or removing of segments. that has to be done extra!
	 *
	 * @param pieceData
	 * @return true if an element was added or removed
	 */
	@Override
	public int applySegmentData(byte x, byte y, byte z, int pieceData, int offset, final boolean synched, long absIndex, boolean updateRemoveBB, boolean updateSegmentBB, long time) throws SegmentDataWriteException {
		return applySegmentData(x, y, z, pieceData, offset, synched, absIndex, updateRemoveBB, updateSegmentBB, time, false);
	}

	@Override
	public int applySegmentData(byte x, byte y, byte z, int pieceData, int offset, final boolean synched, long absIndex, boolean updateRemoveBB, boolean updateSegmentBB, long time, boolean preserveControl) throws SegmentDataWriteException {
		//		assert(onServer || rwl.getWriteHoldCount() == 0);
		//		rwl.writeLock().t
		if(synched) {
			rwl.writeLock().lock();
		}
		try {

			int index = getInfoIndex(x, y, z);
			boolean oldActive = isActive(index);
			short oldType = getType(index);
			byte oldOrientation = getOrientation(index);

			data.writeIntIndex(index + offset, pieceData);

			short newType = getType(index);
			short newHP = getHitpointsByte(index);

			if(newType == Element.TYPE_NONE && oldType == Element.TYPE_NONE) {
				//happens regularily as a confirmation from the server if
				//this client was the one removing
				return PIECE_UNCHANGED;
			}
			if(newType != oldType) {
				if(oldType == Element.TYPE_NONE && newType != Element.TYPE_NONE) {
					onAddingElement(index, x, y, z, newType, updateSegmentBB, synched, absIndex, time);
					return PIECE_ADDED;
				}
				if(oldType != Element.TYPE_NONE && newType == Element.TYPE_NONE) {
					onRemovingElement(index, x, y, z, oldType, updateRemoveBB, updateSegmentBB, oldOrientation, oldActive, synched, time, preserveControl);
					return PIECE_REMOVED;
				}
				onRemovingElement(index, x, y, z, oldType, updateRemoveBB, updateSegmentBB, oldOrientation, oldActive, synched, time, preserveControl);
				onAddingElement(index, x, y, z, newType, updateSegmentBB, synched, absIndex, time);
				assert (newType != Element.TYPE_NONE);
				return PIECE_CHANGED;
			} else {
				short oldHP = getHitpointsByte(index);
				if(oldHP != newHP) {
					return PIECE_CHANGED;
				} else {
					boolean nowActive = isActive(index);
					if(oldActive != nowActive) {

						if(ElementKeyMap.isDoor(newType)) {
							if(nowActive && !oldActive) {
								assert (newType != ElementKeyMap.PICKUP_RAIL);
								//door became active
								octree.insert(x, y, z, index);
							} else if(!nowActive && oldActive) {
								//doore became inactive
								octree.delete(x, y, z, index, newType);
							}
						}
						return PIECE_ACTIVE_CHANGED;
					} else if(oldOrientation != getOrientation(index)) {
						return PIECE_CHANGED;
					}

					return PIECE_UNCHANGED;
				}

			}
		} finally {
			if(synched) {
				rwl.writeLock().unlock();
			}
		}
	}

	@Override
	public byte[] getAsByteBuffer(byte[] out) {

		data.readAllBytes(0, out);

		return out;
	}

	@Override
	public void setInfoElementForcedAddUnsynched(int dataIndex, int newData) throws SegmentDataWriteException {
		data.writeIntIndex(dataIndex, newData);
		this.setBlockAddedForced(true);
	}

	@Override
	public int getDataAt(int infoIndex) {
		return data.readIntIndex(infoIndex);
	}

	@Override
	public int inflate(Inflater inflater, byte[] in) throws SegmentInflaterException, DataFormatException, SegmentDataWriteException {

		int inflate = inflater.inflate(in);
		if(inflate != TOTAL_SIZE * BYTES_USED) {
			throw new SegmentInflaterException(inflate, TOTAL_SIZE * BYTES_USED);
		}

		int dIndex = 0;
		for(int i = 0; i < BLOCK_COUNT; i++) {
			data.writeIntIndex(i, convert3ByteToIntValue(in[dIndex], in[dIndex + 1], in[dIndex + 2]));
			dIndex += BYTES_USED;
		}

		//INSERTED CODE
		try {
			translateModBlocks();
		} catch(SegmentDataWriteException e) {
			e.printStackTrace();
		}
		///

		return inflate;
	}

	@Override
	public int readFrom(ByteBuffer b) throws SegmentDataWriteException {
		data.putAll(b);
		return TOTAL_SIZE;
	}

	@Override
	public void serialize(DataOutput outputStream) throws IOException {
		for(int i = 0; i < TOTAL_SIZE; i++) {
			int value = data.readIntIndex(i);
			outputStream.writeByte((byte) (value & 0xFF));
			outputStream.writeByte((byte) ((value >> 8) & 0xFF));
			outputStream.writeByte((byte) ((value >> 16) & 0xFF));
		}

//		writeAs3Byte(outputStream, data);
	}

	@Override
	public void getBytes(int dataIndex, byte[] bytes) {
		assert (bytes.length >= 3);

		int value = data.readIntIndex(dataIndex);

		bytes[0] = (byte) (value & 0xFF);
		bytes[1] = (byte) ((value >> 8) & 0xFF);
		bytes[2] = (byte) ((value >> 16) & 0xFF);
	}

	@Override
	public void migrateTo(int fromVersion, SegmentDataInterface segmentData) {
//		try {
//			throw new Exception(onServer+" MIGRATE: "+fromVersion);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		if(fromVersion == 6) {
			SegmentData4Byte.migrateFrom6((SegmentData4Byte) segmentData, this);
		} else if(fromVersion == 5) {
			//just move the data over
			data.copyTo(((SegmentDataIntArray) segmentData).data);
			((SegmentDataIntArray) segmentData).needsBitmapCompressionCheck = true;
		} else if(fromVersion == 3) {
			//just move the data over
			data.copyTo(((SegmentDataIntArray) segmentData).data);
			((SegmentDataIntArray) segmentData).needsBitmapCompressionCheck = true;
		} else if(fromVersion == 4) {


			data.copyTo(((SegmentDataIntArray) segmentData).data);
			((SegmentDataIntArray) segmentData).repairAll();
			((SegmentDataIntArray) segmentData).needsBitmapCompressionCheck = true;
		} else {
			assert (false) : fromVersion;
		}
	}

	@Override
	public void repairAll() {
		try {
			for(int i = 0; i < SegmentData.TOTAL_SIZE; i++) {
				final short type = getType(i);

				if(ElementKeyMap.isValidType(type)) {
					short hp = getHitpointsByte(i);
					ElementInformation info = ElementKeyMap.getInfoFast(type);
//					if(type == 608) {
//						assert(false):hp+"; "+info.getHpOldByte();
//					}
					float pp = (float) hp / (float) info.getHpOldByte();
					short newHp = ElementKeyMap.MAX_HITPOINTS;//(short) Math.max(0, Math.max(ElementKeyMap.MAX_HITPOINTS, FastMath.round(pp*(float)ElementKeyMap.MAX_HITPOINTS)));
					setHitpointsByte(i, Math.min(127, newHp));
//						assert(getHitpointsByte(i) == ElementKeyMap.MAX_HITPOINTS):hp+" ; "+getHitpointsByte(i);


				}

			}
		} catch(SegmentDataWriteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public SegmentDataType getDataType() {
		return SegmentDataType.FOUR_BYTE;
	}

	@Override
	public int[] getAsIntBuffer(int[] out) {
		for(int i = 0; i < out.length; i++) {
			out[i] = data.readIntIndex(i);
		}
		return out;
	}

	@Override
	public MemIntArray getAsIntBuffer(MemIntArray out) {
		out.copyFrom(data);
		out.clear(); //reset position/limit
		return out;
	}

	@Override
	public SegmentData doBitmapCompressionCheck(RemoteSegment seg) {
		if(USE_COMPRESSION_CHECK && needsBitmapCompressionCheck) {
			needsBitmapCompressionCheck = false;
			IntOpenHashSet typeMap = getStaticTypeMap();
			for(int i = 0; i < TOTAL_SIZE && typeMap.size() <= 16; i++) {
				typeMap.add(data.readIntIndex(i));
			}
			if(typeMap.size() <= 16) {
				//we can optimize this into a bitmap
				int[] blockTypeData = new int[typeMap.size()];

				int i = 0;
				for(int dat : typeMap) {
					blockTypeData[i] = dat;
					i++;
				}
				SegmentDataBitMap map = new SegmentDataBitMap(!onServer, blockTypeData, this);
				return map;

			} else {
				//nothing to do. we need a full int array
			}


			freeStaticTypeMap(typeMap);
		}
		return this;
	}

	@Override
	public void setDataAt(int i, int data) throws SegmentDataWriteException {
		this.data.writeIntIndex(i, data);
		this.setBlockAddedForced(true);
	}

	@Override
	protected void copyTo(ByteBuffer buffer) {
		this.data.copyTo(buffer);
	}
}
