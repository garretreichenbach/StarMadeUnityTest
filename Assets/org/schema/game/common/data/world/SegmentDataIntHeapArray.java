package org.schema.game.common.data.world;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.MemoryManager.MemIntArray;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.octree.ArrayOctree;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;


public class SegmentDataIntHeapArray extends SegmentData{
	
	public NativeMemoryManager nManager = NativeMemoryManager.segmentDataManager;
	
	private final int[] data;
	boolean needsBitmapCompressionCheck;
	public SegmentDataIntHeapArray() {
		super();
		data = new int[TOTAL_SIZE];
	}
	public SegmentDataIntHeapArray(boolean onClient) {
		super(onClient);
		data = new int[TOTAL_SIZE];
	}
	public SegmentDataIntHeapArray(SegmentData segmentData) {
		super(segmentData);
		data = new int[TOTAL_SIZE];
		
		
		segmentData.copyTo(this);
	}
	
	@Override
	public void setType(int index, short newType)  throws SegmentDataWriteException{
		data[index] = (data[index] & typeMaskNot) | newType;
	}
	@Override
	public void setHitpointsByte(int index, int value)  throws SegmentDataWriteException{
		assert (value >= 0 && value < 128) : value;
		value = (short) Math.min(127, value);
		data[index] = (data[index] & hpMaskNot) | (value << hitpointsIndexStart);
	}
	@Override
	public void setActive(int index, boolean active)  throws SegmentDataWriteException{
		data[index] = active ? (data[index] | activeMask) : (data[index] & activeMaskNot) ;
		assert (active == isActive(index)) : active + "; " + isActive(index);
	}
	@Override
	public void setOrientation(int index, byte value)  throws SegmentDataWriteException{
		assert (value >= 0 && value < 32) : "NOT A SIDE INDEX "+value;
		data[index] = (data[index] & orientMaskNot) | (value << orientationStart);
		assert (value == getOrientation(index)) : "failed orientation coding: " + value + " != result " + getOrientation(index);
	}
	@Override
	public short getType(int index) {
		return (short) (typeMask & data[index]);
	}

	@Override
	public short getHitpointsByte(int index) {
		return (short) ((hpMask & data[index]) >> hitpointsIndexStart);
	}

	@Override
	public boolean isActive(int index) {
		return (activeMask & data[index]) > 0;
	}
	@Override
	public void resetFast()  throws SegmentDataWriteException{
		super.resetFast();
		Arrays.fill(data, (byte) 0);
	}
	@Override
	public boolean containsFast(int index) {
		return (data[index] & typeMask) != 0;
	}
	@Override
	public byte getOrientation(int index) {
		return (byte) ((orientMask & data[index]) >> orientationStart);
	}

	@Override
	public void setExtra(int index, byte extra) throws SegmentDataWriteException {

	}

	@Override
	public int getExtra(int index) {
		return 0;
	}

	@Override
	public void deserialize(DataInput inputStream, long time) throws IOException, SegmentDataWriteException{
		rwl.writeLock().lock();
		try {
			reset(time);
			
			byte[] b = new byte[TOTAL_SIZE * 3];
			inputStream.readFully(b);

//			convertFrom3ByteToIntArray(b, data);
			setNeedsRevalidate(true);
		} finally {
			rwl.writeLock().unlock();
		}

	}
	@Override
	protected ArrayOctree getOctreeInstance(boolean onServer){
		return new ArrayOctree(onServer);
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
	@Override
	public int applySegmentData(byte x, byte y, byte z, int pieceData, int offset, final boolean synched, long absIndex, boolean updateRemoveBB, boolean updateSegmentBB, long time)  throws SegmentDataWriteException{
		return applySegmentData(x, y, z, pieceData, offset, synched, absIndex, updateRemoveBB, updateSegmentBB, time, false);
	}
	@Override
	public int applySegmentData(byte x, byte y, byte z, int pieceData, int offset, final boolean synched, long absIndex, boolean updateRemoveBB, boolean updateSegmentBB, long time, boolean preserveControl)  throws SegmentDataWriteException{
		//		assert(onServer || rwl.getWriteHoldCount() == 0);
		//		rwl.writeLock().t
		if (synched) {
			rwl.writeLock().lock();
		}
		try {

			int index = getInfoIndex(x, y, z);
			boolean oldActive = isActive(index);
			short oldType = getType(index);
			byte oldOrientation = getOrientation(index);

			data[index+offset] = pieceData;

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
					onRemovingElement(index, x, y, z, oldType, updateRemoveBB, updateSegmentBB, oldOrientation, oldActive, synched, time, preserveControl);
					return PIECE_REMOVED;
				}
				onRemovingElement(index, x, y, z, oldType, updateRemoveBB, updateSegmentBB, oldOrientation, oldActive, synched, time, preserveControl);
				onAddingElement(index, x, y, z, newType, updateSegmentBB, synched, absIndex, time);
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
								assert(newType != ElementKeyMap.PICKUP_RAIL);
								//door became active
								octree.insert(x, y, z, index);
							} else if (!nowActive && oldActive) {
								//doore became inactive
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
	@Override
	public byte[] getAsByteBuffer(byte[] out) {
		for(int i = 0; i < BLOCK_COUNT; i++){
			final int dIndex = i * 3;
			final int value = data[i];
			out[dIndex] = (byte) (value);
			out[dIndex+1] = (byte) (value >> 8);
			out[dIndex+2] = (byte) (value >> 16);
			
		}
		return out;
	}
	@Override
	public void setInfoElementForcedAddUnsynched(int dataIndex, int newData) throws SegmentDataWriteException{
		data[dataIndex] = newData;
		this.setBlockAddedForced(true);
	}
	@Override
	public int getDataAt(int infoIndex) {
		return data[infoIndex];
	}
	@Override
	public int inflate(Inflater inflater, byte[] in)
			throws SegmentInflaterException, DataFormatException, SegmentDataWriteException{
		
		int inflate = inflater.inflate(in);
		if (inflate != data.length*BYTES_USED) {
			throw new SegmentInflaterException(inflate, data.length*BYTES_USED);
		}
		
		int dIndex = 0;
		for(int i = 0; i < BLOCK_COUNT; i++){
			data[i] = in[dIndex] & 0xFF | (in[dIndex+1] & 0xFF) << 8 | (in[dIndex+2] & 0xFF) << 16 | (in[dIndex+3] & 0xFF) << 24;
			dIndex += BYTES_USED;
		}
		
		return inflate;
	}
	@Override
	public int readFrom(ByteBuffer uncompressed) throws SegmentDataWriteException {
		uncompressed.asIntBuffer().get(data);
		return TOTAL_SIZE;
	}
	@Override
	public void serialize(DataOutput outputStream) throws IOException {
		writeAs3Byte(outputStream, data);
	}
	@Override
	public void getBytes(int dataIndex, byte[] bytes){
		assert(bytes.length >= 3);
		
		int value = data[dataIndex];
		
		bytes[0] = (byte) (value & 0xFF);
		bytes[1] = (byte) ((value >> 8) & 0xFF);
		bytes[2] = (byte) ((value >> 16) & 0xFF);
	}

	@Override
	public void translateModBlocks() throws SegmentDataWriteException {

	}

	@Override
	public void migrateTo(int fromVersion, SegmentDataInterface segmentData) {
//		try {
//			throw new Exception(onServer+" MIGRATE: "+fromVersion);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		if(fromVersion == 6){
			//to lz4 compression. nothing to do
			System.arraycopy(data, 0, ((SegmentDataIntHeapArray)segmentData).data, 0, data.length);
		}else if(fromVersion == 5){
			//just move the data over
			System.arraycopy(data, 0, ((SegmentDataIntHeapArray)segmentData).data, 0, data.length);
			((SegmentDataIntHeapArray)segmentData).needsBitmapCompressionCheck = true;
		}else if(fromVersion == 3){
			//just move the data over
			System.arraycopy(data, 0, ((SegmentDataIntHeapArray)segmentData).data, 0, data.length);
			((SegmentDataIntHeapArray)segmentData).needsBitmapCompressionCheck = true;
		} else if(fromVersion == 4){
			
			System.arraycopy(data, 0, ((SegmentDataIntHeapArray)segmentData).data, 0, data.length);
			((SegmentDataIntHeapArray)segmentData).repairAll();
			((SegmentDataIntHeapArray)segmentData).needsBitmapCompressionCheck = true;
		} else {
			assert(false):fromVersion;
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
					float pp = (float)hp / (float)info.getHpOldByte();
					short newHp = ElementKeyMap.MAX_HITPOINTS;//(short) Math.max(0, Math.max(ElementKeyMap.MAX_HITPOINTS, FastMath.round(pp*(float)ElementKeyMap.MAX_HITPOINTS)));
					setHitpointsByte(i, Math.min(127, newHp));
//						assert(getHitpointsByte(i) == ElementKeyMap.MAX_HITPOINTS):hp+" ; "+getHitpointsByte(i);
					
						
				}
				
			}
		} catch (SegmentDataWriteException e) {
			e.printStackTrace();
		}		
	}
	@Override
	public SegmentDataType getDataType() {
		return SegmentDataType.FOUR_BYTE;
	}
	@Override
	public int[] getAsIntBuffer(int[] out) {
		return data;
	}
	@Override
	public MemIntArray getAsIntBuffer(MemIntArray out) {
		for(int i = 0; i < TOTAL_SIZE; i++){
			out.put(i, getDataAt(i));
		}
		return out;
	}
	
	private static final ObjectArrayList<IntOpenHashSet> typeMapPool = new ObjectArrayList<IntOpenHashSet>();
	private static final boolean USE_COMPRESSION_CHECK = true;
	public static void freeStaticTypeMap(IntOpenHashSet m){
		m.clear();
		synchronized(typeMapPool){
			typeMapPool.add(m);
		}
	}
	public static IntOpenHashSet getStaticTypeMap(){
		synchronized(typeMapPool){
			if(typeMapPool.isEmpty()){
				return new IntOpenHashSet(16);
			}else{
				return typeMapPool.remove(typeMapPool.size()-1);
			}
		}
	}
	@Override
	public SegmentData doBitmapCompressionCheck(RemoteSegment seg) {
		if(USE_COMPRESSION_CHECK && needsBitmapCompressionCheck){
			needsBitmapCompressionCheck = false;
			IntOpenHashSet typeMap = getStaticTypeMap();
			for(int i = 0; i < TOTAL_SIZE && typeMap.size() <= 16; i++){
				typeMap.add(data[i]);
			}
			if(typeMap.size() <= 16){
				//we can optimize this into a bitmap
				int[] blockTypeData = new int[typeMap.size()];
				
				int i = 0;
				for(int dat : typeMap){
					blockTypeData[i] = dat;
					i++;
				}
				SegmentDataBitMap map = new SegmentDataBitMap(!onServer, blockTypeData, this);
				return map;
				
			}else{
				//nothing to do. we need a full int array
			}
			
			
			freeStaticTypeMap(typeMap);
		}
		return this;
	}
	@Override
	public void setDataAt(int i, int data) throws SegmentDataWriteException {
		this.data[i] = data;
		this.setBlockAddedForced(true);
	}
	@Override
	protected void copyTo(ByteBuffer buffer) {
		buffer.asIntBuffer().put(this.data);
	}
}
