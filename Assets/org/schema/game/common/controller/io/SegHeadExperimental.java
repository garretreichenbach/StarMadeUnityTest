package org.schema.game.common.controller.io;


	import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.controller.SegmentBufferManager;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;



	/**
	 * HEADER DEF:
	 * 
	 * VERSION : byte
	 * |<x 4096>
	 * |rawsize : unsigned short (2 bytes)
	 * |offset : short (2 bytes)
	 * 
	 */
	public class SegHeadExperimental{
		
		private static final byte VERSION = SegmentDataIONew.VERSION;
		private short[] offset = new short[SegmentDataIONew.DIMxDIMxDIM];
		private int[] sizes = new int[SegmentDataIONew.DIMxDIMxDIM];
		
		public static final int SEGMENT_MAX_SECTOR = 96*1024;
		public static final int SEGMENT_2 = 32*1024;
		public static final int SEGMENT_1 = 8*1024;
		public static final int SEGMENT_0 = 4*1024;
		
		private int start1Offset = -1;
		private int start2Offset = -1;
		private int startMaxOffset = -1;
		
		public byte version = -1;
		private int lastSector;
		public static final short NO_DATA = 0;
		
		public static final int HEADER_DATA_POS = 4;
		public static final int HEADER_SIZE = 
				ByteUtil.SIZEOF_INT + //some leeway if there is something to flag for files
				(SegmentDataIONew.DIMxDIMxDIM * ByteUtil.SIZEOF_SHORT) + //offests
				(SegmentDataIONew.DIMxDIMxDIM * ByteUtil.SIZEOF_SHORT); //sizes
			
		
		private static final byte[] NO_DATA_HEADER = new byte[HEADER_SIZE];
		
		private static final ObjectArrayFIFOQueue<byte[]> queue = new ObjectArrayFIFOQueue<byte[]>(100);
		public static final short OFFSET_SHIFT = 1;
		static{
			for(int i = 0; i < 100; i++){
				queue.enqueue(new byte[HEADER_SIZE]);
			}
		}
		
		public static int getSegCoord(int x){
			//region file dim is 16
			return ByteUtil.modU16(ByteUtil.divUSeg(x) + SegmentBufferManager.DIMENSION_HALF);
		} 
		private int getSegIndex(int segX, int segY, int segZ){
			return getLocalIndex(getSegCoord(segX), getSegCoord(segY), getSegCoord(segZ));
		}
		private int getLocalIndex(int x, int y, int z){
			return (z * SegmentDataIONew.DIMxDIM) + (y * SegmentDataIONew.DIM) + x;
		}
		
		public int getLocalSize(int x, int y, int z){
			return sizes[getLocalIndex(x, y, z)];
		}
		public int getLocalOffset(int x, int y, int z){
			return offset[getLocalIndex(x, y, z)];
		}
		public int getSize(int segX, int segY, int segZ){
			return sizes[getSegIndex(segX, segY, segZ)];
		}
		public short getOffset(int segX, int segY, int segZ){
			return offset[getSegIndex(segX, segY, segZ)];
		}
		
		static {
			DataOutputStream out = new DataOutputStream(new FastByteArrayOutputStream(NO_DATA_HEADER)); 
			try {
				out.writeByte(VERSION);
				out.writeInt(0);
				out.writeByte(0);
				out.writeShort((short)-1);
				out.writeShort((short)-1);
				out.writeShort((short)-1);
				for(int i = 0; i < SegmentDataIONew.DIMxDIMxDIM; i++){
					out.writeShort(0);
					out.writeShort(0);
				}
			
			} catch (IOException e) { e.printStackTrace();
			}
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		public void write(DataOutputStream out) throws IOException{
			out.writeByte(VERSION);
			out.writeInt(0);
			out.writeByte(lastSector);
			out.writeShort((short)start1Offset);
			out.writeShort((short)start2Offset);
			out.writeShort((short)startMaxOffset);
			
			
			for(int i = 0; i < SegmentDataIONew.DIMxDIMxDIM; i++){
				out.writeShort(offset[i]);
				out.writeShort((short)sizes[i]);
			}
		}
		public void read(DataInputStream in) throws IOException{
			version = in.readByte();
			in.readInt();
			lastSector = in.readByte() & 0xFF;
			start1Offset = in.readShort();
			start2Offset = in.readShort();
			startMaxOffset = in.readShort();
			
			for(int i = 0; i < SegmentDataIONew.DIMxDIMxDIM; i++){
				offset[i] = in.readShort(); //max is 4096
				sizes[i] = in.readShort() & 0xFFFF; //unsigned short
			}
		}
		
		
		private static void freeHeaderTmp(byte[] c){
			Arrays.fill(c, (byte)0);
			synchronized(queue){
				queue.enqueue(c);
				queue.notify();
			}
		}
		private static byte[] getHeaderTmp(){
			synchronized(queue){
				while(queue.isEmpty()){
					try {
						queue.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				return queue.dequeue();
			}
		}
		void read(RandomAccessFile file) throws IOException{
			byte[] headerTmp = getHeaderTmp();
			file.read(headerTmp, 0, headerTmp.length);
			DataInputStream in = new DataInputStream(new FastByteArrayInputStream(headerTmp));
			read(in);
			in.close();
			freeHeaderTmp(headerTmp);
		}
		
		public static void writeEmptyHeader(DataOutputStream out) throws IOException {
			out.write(VERSION);
		}

		public static void writeEmptyHeader(File f) throws IOException {
			FileOutputStream fileOutputStream = new FileOutputStream(f);
			BufferedOutputStream out = new BufferedOutputStream(fileOutputStream, NO_DATA_HEADER.length);
			out.write(NO_DATA_HEADER);
			out.close();
			fileOutputStream.close();
		}

		public void resetToEmpty() {
			version = VERSION;
			Arrays.fill(offset, (short)0);
			Arrays.fill(sizes, 0);
			
		}

		public void writeTo(RandomAccessFile file) throws IOException {
			byte[] headerTmp = getHeaderTmp();
			DataOutputStream d = new DataOutputStream(new FastByteArrayOutputStream(headerTmp));
			write(d);
			file.seek(0);
			file.write(headerTmp);
			d.close();
			freeHeaderTmp(headerTmp);
		}

		public void writeEmptyDirectly(int x, int y, int z, RandomFileOutputStream ro) throws IOException {
			
			@SuppressWarnings("resource") //doesn't have to be closed since it runs on the region file
			DataOutputStream dro = new DataOutputStream(ro);
			
			int segIndex = getSegIndex(x, y, z);
			
			ro.setFilePointer(HEADER_DATA_POS + ((segIndex * 2) * ByteUtil.SIZEOF_SHORT));
			
			//0 means no data, offset never changes once set
			dro.writeShort(offset[segIndex]);
			//also set length to to indicate a empty segment
			dro.writeShort(0);
		}

		
		
		
		public boolean isEmpty(int x, int y, int z) {
			return getSize(x, y, z) == 0;
		}
		public boolean isNoData(int x, int y, int z) {
			return getOffset(x, y, z) == NO_DATA;
		}
		public boolean isEmptyOrNoData(int x, int y, int z) {
			int index = getSegIndex(x, y, z);
			return isEmptyOrNoData(index);
		}
		
		public boolean isEmptyOrNoData(int localIndex) {
			int os = offset[localIndex];
			int size = sizes[localIndex];
			return os == NO_DATA || size == 0;
		}
		
		public boolean hasOffsetOrHasNoData(int x, int y, int z) {
			int index = getSegIndex(x, y, z);
			int os = offset[index];
			int size = sizes[index];
			return os == NO_DATA && size != 0;
		}
		
		public long getTimeStamp(int x, int y, int z, SegmentRegionFileNew rf) throws IOException {
			if(isEmptyOrNoData(x, y, z)){
				//no data. non empty segment
				return -1;
			}
			
			short dataOffset = convertToDataOffset(getOffset(x, y, z)) ;
			
			rf.file.seek(getAbsoluteFilePos(dataOffset) + ByteUtil.SIZEOF_BYTE);
			
			return rf.file.readLong();
		}
		public static short convertToDataOffset(short o) {
			return (short) (o-SegmentHeader.OFFSET_SHIFT);
		}
		
		public short createNewDataOffsetOnFileEnd(SegmentRegionFileNew rf) throws IOException {
			
			short offset = (short)lastSector;
			lastSector++;
			
			rf.getFile().setLength(rf.getFile().length() + getCurrentSectorSize());
			
			return offset;
		}
		
		private long getDynAbsPos(short dataOffset){
			
			int rest = dataOffset;
			if(start1Offset >= 0){
				long off = 0;
				if(dataOffset > start1Offset){
					off += start1Offset * SEGMENT_0;
					rest -= start1Offset;
					
					if(start2Offset >= 0){
						if(dataOffset > start2Offset){
							int diff2 = (start2Offset - start1Offset);
							off += diff2 * SEGMENT_1;
							rest -= diff2;
							
							if(startMaxOffset >= 0){
								int diffMax = startMaxOffset - start2Offset - start1Offset;
								off += diffMax * SEGMENT_2;
								rest -= diffMax;
								
								return off + (rest * SEGMENT_MAX_SECTOR);
								
							}else{
								return off + (rest * SEGMENT_2);
							}
							
						}else{
							return off + (rest * SEGMENT_1);
						}
					}else{
						return off + rest * SEGMENT_1;
					}
					
				}else{
					return rest * SEGMENT_0;
				}
			}else if(start2Offset >= 0){
				long off = 0;
				if(dataOffset > start2Offset){
					int diff2 = (start2Offset);
					off += diff2 * SEGMENT_0;
					rest -= diff2;
					
					if(startMaxOffset >= 0){
						int diffMax = startMaxOffset - start2Offset;
						off += diffMax * SEGMENT_2;
						rest -= diffMax;
						
						return off + (rest * SEGMENT_MAX_SECTOR);
						
					}else{
						return off + (rest * SEGMENT_2);
					}
					
				}else{
					return off + (rest * SEGMENT_0);
				}
			}else if(startMaxOffset >= 0){
				
				if(dataOffset < startMaxOffset){
					return dataOffset * SEGMENT_0;
				}else{
					long off = 0;
					off += startMaxOffset * SEGMENT_0;
					rest -= startMaxOffset;
					
					return off + rest * SEGMENT_MAX_SECTOR;
				}
			}else{
				//smallest
				return rest * SEGMENT_0;
			}
		}
		
		public long getAbsoluteFilePos(short dataOffset) {
				
			
			return HEADER_SIZE + getDynAbsPos(dataOffset);
		}
		
		public void updateAndWrite(int x, int y, int z, short dataOffset, int length, SegmentRegionFileNew rf) throws IOException {
			
			rf.file.seek(ByteUtil.SIZEOF_BYTE + ByteUtil.SIZEOF_INT);
			rf.file.writeByte(lastSector);
			rf.file.writeShort((short)start1Offset);
			rf.file.writeShort((short)start2Offset);
			rf.file.writeShort((short)startMaxOffset);
			
			int headerOffset = dataOffset + SegmentHeader.OFFSET_SHIFT;
			assert(headerOffset <= Short.MAX_VALUE);
			
			int segIndex = getSegIndex(x, y, z);
			
			offset[segIndex] = (short) headerOffset;
			sizes[segIndex] = length;
			
			assert(length > 0);
			
			rf.file.seek(HEADER_DATA_POS + ((segIndex * 2) * ByteUtil.SIZEOF_SHORT));
			rf.file.writeShort(headerOffset);
			rf.file.writeShort((short)length);
			
		}
		public int getCurrentSectorSize(){
			if(startMaxOffset >= 0){
				return SEGMENT_MAX_SECTOR;
			}else if(start2Offset >= 0){
				return SEGMENT_2;
			}else if(start1Offset >= 0){
				return SEGMENT_1;
			}else{
				return SEGMENT_0;
			}
		}
		public int getMaxSectorSize() {
			return SEGMENT_MAX_SECTOR;
		}
		public void expandSectorOnDataOffset(SegmentRegionFileNew rf, short dataOffset, int length) throws IOException {
			if(length < SEGMENT_1){
				start1Offset = dataOffset;
				rf.getFile().setLength(rf.getFile().length() + SEGMENT_1);
			}else if(length < SEGMENT_2){
				start2Offset = dataOffset;
				rf.getFile().setLength(rf.getFile().length() + SEGMENT_2);
			}else if(length < SEGMENT_MAX_SECTOR){
				startMaxOffset = dataOffset;
				rf.getFile().setLength(rf.getFile().length() + SEGMENT_MAX_SECTOR);
			}else{
				throw new IllegalArgumentException();
			}
		}
		

		
	}

