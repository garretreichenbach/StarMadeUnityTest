package org.schema.game.common.controller.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.schema.game.common.data.world.Chunk16SegmentData;
import org.schema.game.common.data.world.SegmentData;

import com.googlecode.javaewah.EWAHCompressedBitmap;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;

public class SegmentRegionFileOld extends SegmentRegionFileHandle{
	private static final int DATA_ROW = 16;

	private final int[] offsets = new int[SegmentDataIO16.size];
	private final int[] sizes = new int[SegmentDataIO16.size];
	private final long[] timestamps = new long[SegmentDataIO16.size];
	private int version;
	
	

	public SegmentRegionFileOld(String UID, boolean onServer, File f, String name, IOFileManager ioFileManager) throws IOException {
		super(Chunk16SegmentData.TOTAL_SIZE, UID, onServer, f, name, ioFileManager);
		


		createHeader();

	}

	public static byte[] createTimestampHeader(long time) throws IOException {
		byte[] buffer = new byte[SegmentDataIO16.size * 8];
		DataOutputStream b = new DataOutputStream(new FastByteArrayOutputStream(buffer));
		for (int i = 0; i < SegmentDataIO16.size; i++) {
			b.writeLong(time);
		}
		b.close();
		return buffer;
	}

	public static void writeLastChanged(File f, byte[] buffer) throws IOException {
		RandomAccessFile rf = new RandomAccessFile(f, "rw");
		assert (buffer.length == SegmentDataIO16.size * 8);

		//seek begining of timeStamps
		rf.seek(4 + SegmentDataIO16.size * 4 + SegmentDataIO16.size * 4);

		rf.write(buffer);
		rf.close();
	}



	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	
	
	@Override
	protected void createHeader() throws IOException {

		long t = System.currentTimeMillis();
		boolean existed = f.exists();
		long length = 0;
		//no need to force. force is done when removing oldest handle
		if (existed) {
			this.file = new RandomAccessFile(f, "rw");
			length = f.length();
		}

		long synch = System.currentTimeMillis() - t;
		//		assert(length == f.length());
		if (length >= SegmentDataIO16.headerTotalSize) {
			//			file.seek(0);
			byte[] headerTmp = new byte[SegmentDataIO16.size * DATA_ROW + 4]; // 65540 bytes -> 64kb -> ok
			file.read(headerTmp, 0, headerTmp.length);

			DataInputStream in = new DataInputStream(new FastByteArrayInputStream(headerTmp));
			//			DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(f), SegmentDataIO.size*16+4));

			this.version = in.readInt();
			for (int i = 0; i < SegmentDataIO16.size; i++) {
				offsets[i] = in.readInt();
				sizes[i] = in.readInt();
			}
			for (int i = 0; i < SegmentDataIO16.size; i++) {
				timestamps[i] = in.readLong();
			}
			in.close();
		} else {
			if (existed) {
				try {
					throw new NoHeaderException("No Header in file " + getDesc() + " -> " + f.getName() + "; read: " + length + "; is now: " + file.length() + " / " + f.length() + " (must be min: " + SegmentDataIO16.headerTotalSize + ")" + "; REWRITING HEADER; fExisted: " + f.exists());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			SegmentDataIO16.writeEmptyHeader(f);

			if (!existed) {
				this.file = new RandomAccessFile(f, "rw");
			}

			DataInputStream in = new DataInputStream(new FastByteArrayInputStream(SegmentDataIO16.emptyHeader));

			this.version = in.readInt();
			for (int i = 0; i < SegmentDataIO16.size; i++) {
				offsets[i] = in.readInt();
				sizes[i] = in.readInt();
			}
			for (int i = 0; i < SegmentDataIO16.size; i++) {
				timestamps[i] = in.readLong();
			}
			in.close();
		}
		long took = System.currentTimeMillis() - t;
		if (took > 60) {
			System.err.println("[IO] WARNING: " + getDesc() + " Header creation for " + getName() + " took " + took + "ms; [synch: " + synch + "ms] existed: " + existed);
		}
	}

	

	public int getBlockDataOffset(int offset) {
		return offsets[offset];
	}

	

	public int getSize(int offset) {
		return sizes[offset];
	}

	public long getTimestamp(int offIndex) {
		return timestamps[offIndex];
	}

	



	public void setHeader(int offset, int blockDataOffset, int size, long timestamp) {
		offsets[offset] = blockDataOffset;
		sizes[offset] = size;
		timestamps[offset] = timestamp;
		//		needsHeaderWriting = true;
	}

	@Override
	public void writeHeader() throws IOException {
		System.err.println("[IO] Writing Header of " + getName());
		file.writeInt(version);
		for (int i = 0; i < offsets.length; i++) {
			file.writeInt(offsets[i]);
			file.writeInt(sizes[i]);
		}
		for (int i = 0; i < timestamps.length; i++) {
			file.writeLong(timestamps[i]);
		}
	}
	public void writeSegmentWithoutMap(long position, SegmentOutputStream so) throws IOException {
		so.writeToFileWithoutMap(file, position, getName(), f);
	}

	private EWAHCompressedBitmap calculateSignature(int segX, int segY, int segZ) {

		//		int ox = ByteUtil.divU16(ByteUtil.divU16(segX)+ SegmentBufferManager.DIMENSION_HALF) * SegmentBufferManager.DIMENSION ;
		//		int oy = ByteUtil.divU16(ByteUtil.divU16(segY)+ SegmentBufferManager.DIMENSION_HALF) * SegmentBufferManager.DIMENSION ;
		//		int oz = ByteUtil.divU16(ByteUtil.divU16(segZ)+ SegmentBufferManager.DIMENSION_HALF) * SegmentBufferManager.DIMENSION ;
		//
		//
		//
		//		final Vector3i start = new Vector3i(ox- SegmentBufferManager.DIMENSION_HALF,oy- SegmentBufferManager.DIMENSION_HALF,oz- SegmentBufferManager.DIMENSION_HALF);
		//		final Vector3i end = new Vector3i(ox- SegmentBufferManager.DIMENSION_HALF,oy- SegmentBufferManager.DIMENSION_HALF,oz- SegmentBufferManager.DIMENSION_HALF);
		//		end.add(SegmentBufferManager.DIMENSION, SegmentBufferManager.DIMENSION, SegmentBufferManager.DIMENSION);
		//
		//		for(int z = 0; z < SegmentBufferManager.DIMENSION; z++){
		//			for(int y = 0; y < SegmentBufferManager.DIMENSION; y++){
		//				for(int x = 0; x < SegmentBufferManager.DIMENSION; x++){
		//
		//					int xx = x*16+start.x*16;
		//					int yy = y*16+start.y*16;
		//					int zz = z*16+start.z*16;
		//
		//					String segFile = controller.getSegmentProvider().getSegmentDataIO().getSegFile(xx, yy, zz, controller);
		//					assert(segFile.contains(f.getName())):segFile+"; "+f.getName()+" -> "+xx+", "+yy+", "+zz+" from "+segX+", "+segY+", "+segZ+"; start "+start;
		//				}
		//			}
		//		}

		int nX = 0;
		int nY = 0;
		int nZ = 0;

		int index = 0;

		EWAHCompressedBitmap s = new EWAHCompressedBitmap((SegmentData.SEG_TIMES_SEG_TIMES_SEG) / 64);
		//		System.err.println("READING ES: ; "+f.getName());
		for (int z = nZ; z < nZ + SegmentDataIO16.maxSegementsPerFile.z; z++) {
			for (int y = nY; y < nY + SegmentDataIO16.maxSegementsPerFile.y; y++) {
				for (int x = nX; x < nX + SegmentDataIO16.maxSegementsPerFile.x; x++) {

					//					int offsetSeekIndex = SegmentDataIO.getOffsetSeekIndex(x*16,y*16,z*16);

					int xO = x * SegmentData.SEG;
					int yO = y * SegmentData.SEG;
					int zO = z * SegmentData.SEG;
					int offIndex = SegmentDataIO16.getHeaderOffset(
							xO - SegmentDataIO16.segmentShiftBlocks,
							yO - SegmentDataIO16.segmentShiftBlocks,
							zO - SegmentDataIO16.segmentShiftBlocks);
					int offset = getBlockDataOffset(offIndex);//headerData[0];
					int length = getSize(offIndex);//headerData[1];

					byte sig = 2;

					if (offset < 0) {
						if (length < 0) {
							//							if(f.getName().contains("ENTITY_SPACESTATION_schema_1389787308606.0.0.0")){
							//								Segment.debugDraw(new Vector3i(xO-128, yO-128,  zO-128), controller.getWorldTransform(), -3, 0f, 1f, 0f, 1f, 500000f);
							//							}
							sig = 1; //empty
							s.set(index);
						} else {
							//							if(f.getName().contains("ENTITY_SPACESTATION_schema_1389787308606.0.0.0")){
							//							Segment.debugDraw(new Vector3i(xO-128, yO-128,  zO-128), controller.getWorldTransform(), -3, 0f, 1f, 1f, 1f, 500000f);
							//							}
							sig = 0; // no data
						}
					} else {
						//						if(f.getName().contains("ENTITY_SPACESTATION_schema_1389787308606.0.0.0")){
						//							Segment.debugDraw(new Vector3i(xO-128, yO-128,  zO-128), controller.getWorldTransform(), -3, 0f, 0f, 1f, 1f, 500000f);
						//						}
						assert (length > 0);
						//						System.err.println("EXISTS: "+offIndex+"; "+f.getName()+"; "+xO+", "+yO+", "+zO);
					}

					index++;
				}
			}
		}

		//		IntIteratorOverIteratingRLW it = new IntIteratorOverIteratingRLW(s.getIteratingRLW());
		//		while(it.hasNext()){
		//			addIndex(it.next());
		//		}

		return s;

	}

	public EWAHCompressedBitmap getSignature(int x, int y, int z) {
		//		return null;
		return calculateSignature(x, y, z);
	}

}
