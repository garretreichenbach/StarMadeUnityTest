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
public class SegmentHeader{
	
	private static final byte VERSION = SegmentDataIONew.VERSION;
	private short[] offset = new short[SegmentDataIONew.DIMxDIMxDIM];
	private int[] sizes = new int[SegmentDataIONew.DIMxDIMxDIM];
	
	public static final int SEGMENT_SECTOR = 48*1024;
	
	public byte version = -1;
	public static final short NO_DATA = 0;
	
	public static final int HEADER_DATA_POS = 4;
	public static final int HEADER_SIZE = 
			ByteUtil.SIZEOF_INT + //some leeway if there is something to flag for files
			(SegmentDataIONew.DIMxDIMxDIM * ByteUtil.SIZEOF_SHORT) + //offests
			(SegmentDataIONew.DIMxDIMxDIM * ByteUtil.SIZEOF_SHORT); //sizes
		
	public static final int H_1 = HEADER_SIZE+SEGMENT_SECTOR;
	
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
			out.writeByte(0);
			out.writeByte(0);
			out.writeByte(0);
			for(int i = 0; i < SegmentDataIONew.DIMxDIMxDIM; i++){
				out.writeShort(0);
				out.writeShort(0);
			}
		
		} catch (IOException e) { e.printStackTrace();
		}finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	public void write(DataOutputStream out) throws IOException{
		out.writeByte(VERSION);
		out.writeByte(0);
		out.writeByte(0);
		out.writeByte(0);
		for(int i = 0; i < SegmentDataIONew.DIMxDIMxDIM; i++){
			out.writeShort(offset[i]);
			out.writeShort((short)sizes[i]);
		}
	}
	public void read(DataInputStream in) throws IOException{
		version = in.readByte();
		in.readByte();
		in.readByte();
		in.readByte();
		
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
		
		int offset = convertToDataOffset(getOffset(x, y, z)) ;
		
		rf.file.seek(HEADER_SIZE + offset*SEGMENT_SECTOR + ByteUtil.SIZEOF_BYTE);
		
		return rf.file.readLong();
	}
	
	public static short convertToDataOffset(short o) {
		return (short) (o-SegmentHeader.OFFSET_SHIFT);
	}
	
	public short createNewDataOffsetOnFileEnd(SegmentRegionFileNew rf) throws IOException {
		long previousLength = rf.getFile().length();
		
		long oo = Math.max(0, ((previousLength - HEADER_SIZE) / SEGMENT_SECTOR));
		
		assert(oo <= Short.MAX_VALUE);
		
		short offset = (short)oo; 
		
		rf.getFile().setLength(rf.getFile().length() + SEGMENT_SECTOR);
		
		return offset;
	}
	
	public static long getAbsoluteFilePos(short dataOffset) {
		return HEADER_SIZE + ((long)dataOffset * (long)SEGMENT_SECTOR);
	}
	
	public void updateAndWrite(int x, int y, int z, short dataOffset, int length, SegmentRegionFileNew rf) throws IOException {
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
	

	
}
