package org.schema.game.common.controller.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.SerializationException;
import org.schema.common.ByteBufferInputStream;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.GameClientController;
import org.schema.game.client.data.ClientStatics;
import org.schema.game.common.controller.SegmentBufferManager;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.TransientSegmentController;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.world.Chunk16SegmentData;
import org.schema.game.common.data.world.DeserializationException;
import org.schema.game.common.data.world.RemoteSegment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.Universe;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.FileExt;

import com.googlecode.javaewah.EWAHCompressedBitmap;

import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class SegmentDataIO16 implements SegmentDataIOInterface{

	public static final Vector3i maxSegementsPerFile = new Vector3i(16, 16, 16);
	public static final byte VERSION = 2;
	public static final byte[] versionBytes = ByteUtil.intToByteArray(VERSION);
	public static final int size = maxSegementsPerFile.x * maxSegementsPerFile.y * maxSegementsPerFile.z;
	public static final int headerTotalSize = size * 8; //32kb
	public static final int timestampTotalSize = size * 8; //32kb
	public static final int segmentShift = 8;
	public static final int segmentShiftBlocks = segmentShift * SegmentData.SEG;
	
	public static final String BLOCK_FILE_EXT = ".smd2";
	private final static int sectorInKb = 5 * 1024; //real size = (16*16*16*3)/1024 = 12
	private static final long SHORT_MAX2 = Short.MAX_VALUE * 2;
	private static final long SHORT_MAX2x2 = SHORT_MAX2 * SHORT_MAX2;
	static byte[] emptyHeader = new byte[versionBytes.length + headerTotalSize + timestampTotalSize];
	private static byte[] minusOne = ByteUtil.intToByteArray(-1);

	static {
		int i = 0;
		for (int m = 0; m < versionBytes.length; m++) {
			emptyHeader[i++] = versionBytes[m];//offset
		}

		for (int z = 0; z < maxSegementsPerFile.z; z++) {
			for (int y = 0; y < maxSegementsPerFile.y; y++) {
				for (int x = 0; x < maxSegementsPerFile.x; x++) {

					for (int m = 0; m < minusOne.length; m++) {
						emptyHeader[i++] = minusOne[m];//offset
					}
					i += 4;
				}
			}
		}
		assert (i == ((emptyHeader.length - 4) / 2 + 4)) : i + "/" + ((emptyHeader.length - 4) / 2 + 4);

	}

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final UniqueIdentifierInterface segmentController;
	private final String segmentDataPath;
	private final Long2ObjectOpenHashMap<String> fileNameCacheRead = new Long2ObjectOpenHashMap<String>();
	private final Long2ObjectOpenHashMap<String> fileNameCacheWrite = new Long2ObjectOpenHashMap<String>();
	private byte[] headerArray = new byte[8]; //data: pos + length
	private byte[] timestampArray = new byte[8];
	private final boolean onServer;
	private long timeStampSeekDebug;
	private IOFileManager  manager;

	public SegmentDataIO16(UniqueIdentifierInterface segmentController, boolean onServer) {

		this.segmentController = segmentController;
		this.onServer = onServer;
		this.manager = new IOFileManager(segmentController.getUniqueIdentifier(), segmentController.isOnServer());
		if (onServer) {
			this.segmentDataPath = GameServerState.SEGMENT_DATA_DATABASE_PATH;
		} else {
			this.segmentDataPath = ClientStatics.SEGMENT_DATA_DATABASE_PATH;
		}

		//		this.oldSegmentIO = new SegmentDataIOOld(segmentController, onServer);
	}

	public static void main(String[] sdf) throws IOException {
		long t = 10;
		byte[] b = new byte[8];
		FastByteArrayOutputStream fastByteArrayOutputStream = new FastByteArrayOutputStream(b);

		DataOutputStream sb = new DataOutputStream(fastByteArrayOutputStream);
		//		for(long l = 0; l < Long.MAX_VALUE; l++){
		long l = Long.MAX_VALUE + 2;
		sb.writeLong(l);
		//			assert(b[0] >= 0);
		//			if(l%10000000 == 0){
		//				System.err.println("AT; "+l+" of "+Long.MAX_VALUE);
		//			}
		for (int i = 0; i < b.length; i++) {
			System.err.println(i + ": " + b[i]);
		}
		fastByteArrayOutputStream.reset();
		//		}
		//
		//		ByteArrayInputStream rm = new ByteArrayInputStream(b);
		//		DataInputStream sd = new DataInputStream(rm);
		//		System.err.println(sd.readByte());
		//		System.err.println(sd.readByte());
		//		System.err.println(sd.readByte());
		//		System.err.println(sd.readByte());
		sb.close();
	}

	public static int getHeaderOffset(int x, int y, int z) {
		/*
		 *
		 * bitwise op for
		 * int xSeg = Math.abs( x  / SegmentData.SEG ) % maxSegementsPerFile.x;
		 * int ySeg = Math.abs( y  / SegmentData.SEG ) % maxSegementsPerFile.y;
		 * int zSeg = Math.abs( z  / SegmentData.SEG ) % maxSegementsPerFile.z;
		 *
		 * (proven to equal results for all integers)
		 */

		int xSeg = (Math.abs(x + segmentShiftBlocks) >> 4) & 0xF;
		int ySeg = (Math.abs(y + segmentShiftBlocks) >> 4) & 0xF;
		int zSeg = (Math.abs(z + segmentShiftBlocks) >> 4) & 0xF;

		int offIndex = ((zSeg * maxSegementsPerFile.y * maxSegementsPerFile.x)
				+ (ySeg * maxSegementsPerFile.x) + xSeg);

		assert (offIndex < size) : offIndex + "/" + size + ": " + x + ", " + y + ", " + z + " ---> " + xSeg + ", " + ySeg + ", " + zSeg + " ";
		return offIndex;
	}

//	public static void convertUID(String uid, UniqueIdentifierInterface uidInt, StringBuilder out) {
//		StringBuilder sbUni = new StringBuilder();
//		sbUni.append(uid);
//
//		String host = uidInt.getObfuscationString();
//		//this should be collision free
//		long hash = Math.abs(uid.hashCode() + host.hashCode()) % 128;
//		//ffs, prove is needed....
//		//all possible value are >0 and <256
//		//hashcode is integer, so no way, a long is going to overflow
//		//hashcode MAY collide, but will resolve in different coding
//		//-> no way 2 different filenames are going to be encoded the same
//		//		long posCodec = getIndex(xS, yS, zS);
//		for (int i = 0; i < sbUni.length(); i++) {
//			//			System.err.println(posCodec+"; sss "+hash);
//			out.append(alphabet.charAt(((int) ((sbUni.charAt(i) * (i + hash)) % alphabet.length()))));
//		}
//	}

	public static final long getIndex(int vx, int vy, int vz) {

		long x = vx + Short.MAX_VALUE;
		long y = vy + Short.MAX_VALUE;
		long z = vz + Short.MAX_VALUE;

		long index = z * SHORT_MAX2x2 + y * SHORT_MAX2 + x;

		if (index < 0) {
			throw new IllegalArgumentException("ElementCollection Index out of bounds: " + x + ", " + y + ", " + z + " -> " + index);
		}

		return index;

	}

	private static void writeEmptyHeader(DataOutputStream out) throws IOException {
		out.write(emptyHeader);
	}

	public static void writeEmptyHeader(File f) throws IOException {
		long t = System.currentTimeMillis();
		FileOutputStream fileOutputStream = new FileOutputStream(f);
		BufferedOutputStream out = new BufferedOutputStream(fileOutputStream, emptyHeader.length);
		long fileStreamOpen = System.currentTimeMillis() - t;
		out.write(emptyHeader);

		long t0 = System.currentTimeMillis();

		out.close();
		fileOutputStream.close();

		long fileStreamClose = System.currentTimeMillis() - t0;
		if ((System.currentTimeMillis() - t) > 50) {
			System.err.println("[IO] WARNING Wrote Empty Header " + emptyHeader.length + ": " + f.getName() + "; TIME: " + (System.currentTimeMillis() - t) + "ms; open " + fileStreamOpen + "; close: " + fileStreamClose);
		}
	}

	public static void writeStatic(RemoteSegment s, int[] headerData, byte version, File old) throws IOException {

		String uIdent = old.getName().substring(0, old.getName().indexOf("."));
		int xS = ByteUtil.div16(s.pos.x + segmentShiftBlocks) / maxSegementsPerFile.x - (s.pos.x + segmentShiftBlocks < 0 ? 1 : 0);
		int yS = ByteUtil.div16(s.pos.y + segmentShiftBlocks) / maxSegementsPerFile.y - (s.pos.y + segmentShiftBlocks < 0 ? 1 : 0);
		int zS = ByteUtil.div16(s.pos.z + segmentShiftBlocks) / maxSegementsPerFile.z - (s.pos.z + segmentShiftBlocks < 0 ? 1 : 0);

		File f = new FileExt(old.getParentFile().getAbsolutePath() + "/" + uIdent + "." + xS + "." + yS + "." + zS + BLOCK_FILE_EXT);
		if (!f.exists()) {
			f.createNewFile();

			long t = System.currentTimeMillis();
			FileOutputStream fileOutputStream = new FileOutputStream(f);
			BufferedOutputStream out = new BufferedOutputStream(fileOutputStream);
			DataOutputStream dataOutputStream = new DataOutputStream(out);

			writeEmptyHeader(dataOutputStream);
			dataOutputStream.close();
			fileOutputStream.close();
			out.close();
			fileOutputStream.close();
			if (s.getSegmentController() != null && !s.getSegmentController().isOnServer()) {
				System.err.println("Wrote Empty Header " + emptyHeader.length + ": " + f.getName() + "; " + (System.currentTimeMillis() - t) + "ms; for " + s.pos);
			}
		}

		RandomAccessFile rf = new RandomAccessFile(f, "r");
		int offIndex = getHeaderOffset(s.pos.x, s.pos.y, s.pos.z);
		rf.seek(versionBytes.length + offIndex * 8/*headerArray.length*/);
		headerData[0] = rf.readInt();
		headerData[1] = rf.readInt();
		rf.close();

		RandomFileOutputStream ro = new RandomFileOutputStream(f, false);
		DataOutputStream dro = new DataOutputStream(ro);

		int offset = headerData[0];
		int dataOffset = 0;
		//		System.err.println("HeaderSize: "+(headerTotalSize+timestampTotalSize));
		if (offset < 0) {
			long previousLength = ro.getFileSize();

			dataOffset = Math.max(0, (int) ((previousLength - headerTotalSize - timestampTotalSize - versionBytes.length) / sectorInKb));

			int sizeOfOneSegment = sectorInKb;
			//			System.err.println("data doesent exist yet. new  offset at "+dataOffset+": lenOfFile: "+(ro.getFileSize())+": "+dataOffset*sectorInKb);
			ro.setFileSize(ro.getFileSize() + sizeOfOneSegment);
			ro.setFilePointer(previousLength);
		} else {

			ro.setFilePointer(versionBytes.length + headerTotalSize + timestampTotalSize + offset * sectorInKb);
			dataOffset = offset;
			//			throw new NullPointerException();
		}

		long dataPosition = ro.getFilePointer();

		int length = s.serialize(dro, true, System.currentTimeMillis(), version);

		assert (length < sectorInKb) : length + "/" + sectorInKb;
		long fileSize = ro.getFileSize();
		//		System.err.println("Writing length: "+length+" at "+ro.getFilePointer());

		//write new offset and timestamp
		int offsetIndex = getHeaderOffset(s.pos.x, s.pos.y, s.pos.z);

		ro.setFilePointer(versionBytes.length + offsetIndex * 8/*headerArray.length*/);

		assert (length < sectorInKb);
		dro.writeInt(dataOffset);
		dro.writeInt(length);

		ro.setFilePointer(versionBytes.length + headerTotalSize + offsetIndex * 8/*timestampArray.length*/);
		dro.writeLong(System.currentTimeMillis());

		ro.flush();
		dro.flush();
		dro.close();
		ro.close();

	}

	/**
	 * @return the manager
	 */
	@Override
	public IOFileManager getManager() {
		return manager;
	}

	
	
	public static String getSegFile(int x, int y, int z, String uid, UniqueIdentifierInterface uidM, Long2ObjectOpenHashMap<String> fileNameCache, String segmentPathData) {

		int xS = ByteUtil.div16(x + segmentShiftBlocks) / maxSegementsPerFile.x - (x + segmentShiftBlocks < 0 ? 1 : 0);
		int yS = ByteUtil.div16(y + segmentShiftBlocks) / maxSegementsPerFile.y - (y + segmentShiftBlocks < 0 ? 1 : 0);
		int zS = ByteUtil.div16(z + segmentShiftBlocks) / maxSegementsPerFile.z - (z + segmentShiftBlocks < 0 ? 1 : 0);

		long index = ElementCollection.getIndex(xS, yS, zS);

		
		if(fileNameCache != null){
			/*
			 * fileNameChache needs to be synched
			 * since this function can be accessed by
			 * threads
			 */
			synchronized (fileNameCache) {
				String g;
				if ((g = fileNameCache.get(index)) != null) {
					//found cache
					return g;
				}
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append(segmentPathData);

		if (uidM != null && !uidM.isOnServer()) {
			SegmentDataFileUtils.convertUID(uid, uidM.getObfuscationString(), sb);

			sb.append(".");
			sb.append(xS);
			sb.append(".");
			sb.append(yS);
			sb.append(".");
			sb.append(zS);
			//			sb.append(segmentController.getUniqueIdentifier());
		} else {
			sb.append(uid);
			sb.append(".");
			sb.append(xS);
			sb.append(".");
			sb.append(yS);
			sb.append(".");
			sb.append(zS);
		}

		sb.append(BLOCK_FILE_EXT);
		
		if(fileNameCache != null){
			synchronized (fileNameCache) {
				fileNameCache.put(index, sb.toString());
			}
		}

		return sb.toString();
	}

	public final String getSegmentDataPath(boolean read) {
		if(read && onServer && segmentController.isLoadByBlueprint()){
			return segmentController.getBlueprintSegmentDataPath();
		}
		return segmentDataPath;
	}

	@Override
	public long getTimeStamp(int x, int y, int z) throws IOException {
		String s = getSegFile(x, y, z, segmentController.getReadUniqueIdentifier(), segmentController, fileNameCacheRead, getSegmentDataPath(true));
		if (!GameClientController.exists(s)) {
			return -1;
		} else {
		}
		SegmentRegionFileOld rf = null;
		try {

			//aquire lock synchronized
			//so file is not closed by another thread
			rf = (SegmentRegionFileOld)manager.getFileAndLockIt(s, true);

			int offIndex = getHeaderOffset(x, y, z);
			long timeStamp = getTimeStamp(offIndex, rf);

			return timeStamp;
		} catch (NoHeaderException e) {
			e.printStackTrace();
			manager.removeFile(s);
			return -1;
		} finally {
			if (rf != null) {
				rf.rwl.readLock().unlock();
			}
		}

	}

	@Override
	public int getSize(int x, int y, int z) throws IOException {
		//		synchronized(this)ads{
		long t = System.nanoTime();
		String s = getSegFile(x, y, z, segmentController.getReadUniqueIdentifier(), segmentController, fileNameCacheRead, getSegmentDataPath(true));
		long segFile = System.nanoTime() - t;
		//			t = System.nanoTime();
		if (!GameClientController.exists(s)) {
			return -1;
		} else {
		}
		SegmentRegionFileOld rf = null;
		try {

			//aquire lock synchronized
			//so file is not closed by another thread
			rf = (SegmentRegionFileOld) manager.getFileAndLockIt(s, true);

			int offIndex = getHeaderOffset(x, y, z);
			int size = getSize(offIndex, rf);

			return size;
		} catch (NoHeaderException e) {
			e.printStackTrace();
			manager.removeFile(s);
			return -1;
		} finally {
			if (rf != null) {
				rf.rwl.readLock().unlock();
			}
		}

	}

	private static int getSize(int offIndex, SegmentRegionFileOld rf) throws IOException {
		rf.rwl.readLock().lock();
		int timestamp = rf.getSize(offIndex);
		rf.rwl.readLock().unlock();
		return timestamp;
	}

	private static long getTimeStamp(int offIndex, SegmentRegionFileOld rf) throws IOException {
		rf.rwl.readLock().lock();
		long timestamp = rf.getTimestamp(offIndex);
		rf.rwl.readLock().unlock();
		return timestamp;
	}

	private static int getVersion(SegmentRegionFileOld rf) throws IOException {
		rf.rwl.readLock().lock();
		int version = rf.getVersion();
		rf.rwl.readLock().unlock();
		return version;
	}

	/**
	 * @return the timeStampSeekDebug
	 */
	public long getTimeStampSeekDebug() {
		return timeStampSeekDebug;
	}

	/**
	 * @param timeStampSeekDebug the timeStampSeekDebug to set
	 */
	public void setTimeStampSeekDebug(long timeStampSeekDebug) {
		this.timeStampSeekDebug = timeStampSeekDebug;
	}

	private void onEmptySegment(int x, int y, int z, long lastChanged, SegmentRegionFileOld rf) throws IOException {
		int offset = getHeaderOffset(x, y, z);

		RandomFileOutputStream ro = new RandomFileOutputStream(rf.getFile(), false);
		@SuppressWarnings("resource") //doesn't have to be closed since it runs on the region file
		DataOutputStream dro = new DataOutputStream(ro);

		ro.setFilePointer(versionBytes.length + offset * headerArray.length);

		//set offset to 0 so we know there is no data
		dro.writeInt(-1);
		//also set length to to indicate a empty segment
		dro.writeInt(-1);

		ro.setFilePointer(versionBytes.length + headerTotalSize + offset * timestampArray.length);
		dro.writeLong(lastChanged);

		rf.setHeader(offset, -1, -1, lastChanged);
	}

	@Override
	public void releaseFileHandles() throws IOException {
		long t = System.currentTimeMillis();
		manager.closeAll();
		long took = System.currentTimeMillis() - t;
		if (took > 10) {
			System.err.println("[SEGMENT-IO] WARNING: File Handle Release for " + segmentController + " on server(" + segmentController.isOnServer() + ") took " + took + " ms");
		}
	}

	@Override
	public int request(int x, int y, int z, RemoteSegment seg)
			throws IOException, DeserializationException {
		rwl.readLock().lock();
		try {
			String s = getSegFile(x, y, z, segmentController.getReadUniqueIdentifier(), segmentController, fileNameCacheRead, getSegmentDataPath(true));

			File f = new FileExt(s);

			//checking the manager first when handle exists saves time (no need to check in the filesystem file.exists())
			if (!manager.existsFile(s) && !f.exists()) {
				return SegmentDataFileUtils.READ_NO_DATA;
			} else {
				SegmentRegionFileOld rf = null;
				ByteBuffer dataByteBuffer = null;
				BufferedInputStream byteArrayInputStream = null;
				DataInputStream in = null;
				try {
					rf = (SegmentRegionFileOld) manager.getFileAndLockIt(s, true);

					int headerOffset = getHeaderOffset(x, y, z);

					int offset = rf.getBlockDataOffset(headerOffset);//headerData[0];
					int length = rf.getSize(headerOffset);//headerData[1];

					if (offset < 0) {

						if (length < 0) {
							try {
								long timestamp = getTimeStamp(headerOffset, rf);
								seg.timestampTmp = timestamp;
								/*
								HERE IS THE DEADLOCK. WE ARE PUTING IN STUFF INTO THE BUFFER WHILE BEING LOADING IN THREADS. MAKE TEMP TIMESTEP TO FILL IN LATER WHEN ADDING
								seg.setLastChanged(timestamp);
								*/
							} catch (IOException e) {
								System.err.println("COULD NOT READ TIMESTAMP FOR "
										+ seg + " ... " + e.getMessage());
								seg.setLastChanged(System.currentTimeMillis());
								seg.timestampTmp = System.currentTimeMillis();
							}
							//						rf.close();
							return SegmentDataFileUtils.READ_EMPTY;
						} else {
							//						rf.close();
							return SegmentDataFileUtils.READ_NO_DATA;
						}
					} else {

						int version = getVersion(rf);

						long timestamp = getTimeStamp(headerOffset, rf);// channel.map(MapMode.READ_ONLY, timeStampSeekPosition, 8).getLong();

						assert (length > 0 && length < sectorInKb) : " len: " + length + " / " + sectorInKb + " ON " + f.getName() + " (" + x + ", " + y + ", " + z + ")";

						long dataPosition = (versionBytes.length + headerTotalSize + timestampTotalSize)
								+ offset * sectorInKb;
						dataByteBuffer = segmentController.getDataByteBuffer();
						dataByteBuffer.rewind();
						dataByteBuffer.limit(length);

						rf.getFile().getChannel().read(dataByteBuffer, dataPosition);
						dataByteBuffer.rewind();

						byteArrayInputStream = new BufferedInputStream(new ByteBufferInputStream(
								dataByteBuffer));
						in = new DataInputStream(
								byteArrayInputStream);

						// do not reset only when the segmentdata is the debug data
						boolean needsResaveOnNextSave = false;
						try {
							needsResaveOnNextSave = seg.deserialize(in, length, false, true, segmentController.getUpdateTime());
						} catch (IOException e) {
							System.err.println("Exception: IOException " + e + " happened on " + segmentController + " -> file: " + s);
							throw e;
						}

						if (needsResaveOnNextSave) {
							//in case the segments needs to be saved (e.g. loaded from old version)
//							seg.setLastChanged(System.currentTimeMillis());
							seg.timestampTmp = System.currentTimeMillis();
						} else {
							seg.timestampTmp = timestamp;
//							seg.setLastChanged(timestamp);
						}

						//						rf.close();
						return SegmentDataFileUtils.READ_DATA;
					}
				} finally {
					if (dataByteBuffer != null) {
						segmentController.releaseDataByteBuffer(dataByteBuffer);
					}
					if (byteArrayInputStream != null) {
						byteArrayInputStream.close();
					}
					if (in != null) {
						in.close();
					}
					rf.rwl.readLock().unlock();
				}
			}
		} finally {
			rwl.readLock().unlock();
		}
	}

	
	
	public static void request(File f, Long2ObjectOpenHashMap<Chunk16SegmentData> segs, Vector3i min, Vector3i max, ByteBuffer dataByteBuffer, long time)
			throws IOException, DeserializationException {
		
		
		if(!f.exists()){
			return;
		}
		SegmentRegionFileOld rf = null;
		BufferedInputStream byteArrayInputStream = null;
		DataInputStream in = null;
		rf = (SegmentRegionFileOld) IOFileManager.getFromFile(f);

		for (int z = 0; z < 256; z += 16) {
			for (int y = 0; y < 256; y += 16) {
				for (int x = 0; x < 256; x += 16) {
					
						
					int headerOffset = getHeaderOffset(x, y, z);
		
					int offset = rf.getBlockDataOffset(headerOffset);//headerData[0];
					int length = rf.getSize(headerOffset);//headerData[1];

					if (offset < 0 || length < 0) {
		
						
					} else {
						Chunk16SegmentData seg = new Chunk16SegmentData();
						
						int version = getVersion(rf);
		
						long timestamp = getTimeStamp(headerOffset, rf);// channel.map(MapMode.READ_ONLY, timeStampSeekPosition, 8).getLong();
		
						assert (length > 0 && length < sectorInKb) : " len: " + length + " / " + sectorInKb + " ON " + f.getName() + " (" + x + ", " + y + ", " + z + ")";
		
						long dataPosition = (versionBytes.length + headerTotalSize + timestampTotalSize)
								+ offset * sectorInKb;
						dataByteBuffer.rewind();
						dataByteBuffer.limit(length);
		
						rf.getFile().getChannel().read(dataByteBuffer, dataPosition);
						dataByteBuffer.rewind();
		
						byteArrayInputStream = new BufferedInputStream(new ByteBufferInputStream(
								dataByteBuffer));
						in = new DataInputStream(
								byteArrayInputStream);
		
						seg.deserialize(in, length, true, false, time);
		
		
						long ld = ElementCollection.getIndex(seg.getSegmentPos());
						segs.put(ld, seg);
						
						min.min(seg.getSegmentPos().x, seg.getSegmentPos().y, seg.getSegmentPos().z);
						max.max(seg.getSegmentPos().x+16, seg.getSegmentPos().y+16, seg.getSegmentPos().z+16);
					}
				}
			}
		}
		rf.close();
	}
	
	private boolean exists(int x, int y, int z) {
		String s = getSegFile(x, y, z, segmentController.getReadUniqueIdentifier(), segmentController, fileNameCacheRead, getSegmentDataPath(true));

		File f = new FileExt(s);

		//checking the manager first when handle exists saves time (no need to check in the filesystem file.exists())
		if (!manager.existsFile(s) && !f.exists()) {
			return false;
		}
		return true;
	}

	@Override
	public EWAHCompressedBitmap requestSignature(int xs, int ys, int zs)
			throws IOException, DeserializationException {
		rwl.readLock().lock();
		try {
			
			int ox = ByteUtil.divU16(ByteUtil.divU16(xs) + SegmentBufferManager.DIMENSION_HALF) * SegmentBufferManager.DIMENSION;
			int oy = ByteUtil.divU16(ByteUtil.divU16(ys) + SegmentBufferManager.DIMENSION_HALF) * SegmentBufferManager.DIMENSION;
			int oz = ByteUtil.divU16(ByteUtil.divU16(zs) + SegmentBufferManager.DIMENSION_HALF) * SegmentBufferManager.DIMENSION;

			final Vector3i start = new Vector3i(ox - SegmentBufferManager.DIMENSION_HALF, oy - SegmentBufferManager.DIMENSION_HALF, oz - SegmentBufferManager.DIMENSION_HALF);
			final Vector3i end = new Vector3i(ox - SegmentBufferManager.DIMENSION_HALF, oy - SegmentBufferManager.DIMENSION_HALF, oz - SegmentBufferManager.DIMENSION_HALF);
			end.add(SegmentBufferManager.DIMENSION, SegmentBufferManager.DIMENSION, SegmentBufferManager.DIMENSION);
			start.scale(16);
			end.scale(16);

			boolean exists = false;

			exists = exists || exists(start.x, start.y, start.z);
			exists = exists || exists(start.x, start.y, end.z);
			exists = exists || exists(end.x, start.y, start.z);
			exists = exists || exists(start.x, end.y, start.z);
			exists = exists || exists(start.x, end.y, end.z);
			exists = exists || exists(end.x, start.y, end.z);
			exists = exists || exists(end.x, end.y, start.z);
			exists = exists || exists(end.x, end.y, end.z);
			if (!exists) {
				return null;
			}

			EWAHCompressedBitmap bm = new EWAHCompressedBitmap((16 * 16 * 16) / 64);
			int i = 0;
			for (int z = start.z; z < end.z; z += 16) {
				for (int y = start.y; y < end.y; y += 16) {
					for (int x = start.x; x < end.x; x += 16) {

						String s = getSegFile(x, y, z, segmentController.getReadUniqueIdentifier(), segmentController, fileNameCacheRead, getSegmentDataPath(true));

						File f;

						//checking the manager first when handle exists saves time (no need to check in the filesystem file.exists())
						if (!manager.existsFile(s) && !((f = new FileExt(s)).exists())) {
							//no file present
						} else {
							SegmentRegionFileOld rf = null;
							try {
								rf = (SegmentRegionFileOld) manager.getFileAndLockIt(s, true);

								int offIndex = getHeaderOffset(x, y, z);

								int offset = rf.getBlockDataOffset(offIndex);
								int length = rf.getSize(offIndex);

								if (offset < 0) {
									if (length < 0) {

										bm.set(i);
									} else {
										//										return READ_NO_DATA;
									}
								} else {

								}

							} finally {
								rf.rwl.readLock().unlock();

							}
						}
						i++;
					}
				}
			}
			return bm;
		} finally {
			rwl.readLock().unlock();
		}
	}

	public void writeEmpty(Vector3i pos, SegmentController c, long lastChanged, boolean writeHeader) throws IOException {

	}

	@Override
	public void writeEmpty(int x, int y, int z, SegmentController c, long lastChanged, boolean writeHeader) throws IOException {
		if (c instanceof TransientSegmentController && !((TransientSegmentController) c).isTouched()) {
			return;
		}
		if(c.isLoadByBlueprint()){
			return;
		}
		try {

			rwl.writeLock().lock();

			
			
			String fileName = getSegFile(x, y, z, segmentController.getWriteUniqueIdentifier(), segmentController, fileNameCacheWrite, getSegmentDataPath(false));
//			File f = new FileExt(fileName);

			SegmentRegionFileOld rf = null;
			try {

				rf = (SegmentRegionFileOld) manager.getFileAndLockIt(fileName, false);
				// write timestamp only without data
				onEmptySegment(x, y, z, lastChanged, rf);
				// unlock done in finally block
				return;

			} finally {
				try {
					if (rf != null && rf.rwl != null && rf.rwl.writeLock() != null) {
						rf.rwl.writeLock().unlock();
					} else {
						System.err.println("Exception LOCK NULL: " + rf);
						if (rf != null) {
							System.err.println("Exception LOCK RWL NULL: " + rf.rwl);

							if (rf.rwl != null) {
								System.err.println("Exception LOCK RWL NULL: " + rf.rwl.writeLock());
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			System.err.println("Exception HAPPENED ON EMPTY SEGMENT " + x + ", " + y + ", " + z + "; controller: " + c + ";");
			e.printStackTrace();
			if (segmentController.isOnServer()) {
				((GameServerState) ((SegmentController)segmentController).getState())
						.getController().broadcastMessage(Lng.astr("ERROR while saving data!\nPlease tell an admin to\nrestart the server and to\nsend in a report\nDATA MAY BE LOST IF CONTINUED"), ServerMessage.MESSAGE_TYPE_ERROR);
			}
			throw e;
		} finally {
			rwl.writeLock().unlock();
		}
	}

	@Override
	public boolean write(RemoteSegment s, long lastChanged, boolean writeHeader, boolean debug) throws IOException {
		if (s.getSegmentController().isOnServer() && !(s.getSegmentController() instanceof TransientSegmentController)) {
			Universe universe = ((GameServerState) s.getSegmentController().getState()).getUniverse();
			synchronized (universe.writeMap) {
				try {

					long thiswrite = System.currentTimeMillis();
					long lastWrite = s.getSegmentController().getLastWrite();
					assert (s.getSegmentController().getWriteUniqueIdentifier() != null);
					long lastWriteUID = universe.writeMap.getLong(s.getSegmentController().getWriteUniqueIdentifier());

					if (lastWriteUID > 0) {
						if (lastWriteUID != lastWrite) {
							throw new SerializationException(s.getSegmentController() + "sec[" + s.getSegmentController().getSectorId() + "]; Tried to write old version over new (rollbackbug) entity-UID LAST WRITTEN " + lastWriteUID + "; entity-instance LAST WRITTEN " + lastWrite + ";  UID: " + s.getSegmentController().getUniqueIdentifier());
						}
					}

					universe.writeMap.put(s.getSegmentController().getWriteUniqueIdentifier(), thiswrite);
					s.getSegmentController().setLastWrite(thiswrite);
					assert (s.getSegmentController().getLastWrite() == thiswrite);
					//					System.err.println("LAST SAVED: "+s.getSegmentController()+": "+thiswrite+"; "+s.getSegmentController().getUniqueIdentifier());
				} catch (SerializationException e) {
					e.printStackTrace();
					((GameServerState) s.getSegmentController().getState()).getController().broadcastMessageAdmin(
							Lng.astr("SerialException on Server! \nPlease send in report from server\ncaused by: %s\n%s",  s.getSegmentController(), ((GameServerState) s.getSegmentController().getState()).getUniverse().getSector(s.getSegmentController().getSectorId())), ServerMessage.MESSAGE_TYPE_ERROR);
					return false;
				}
			}
		}
		if (s.getSegmentController() instanceof TransientSegmentController && !((TransientSegmentController) s.getSegmentController()).isTouched()) {
			//			System.err.println("[SEGMENTIO] not writing transient object "+s.getSegmentController());
			return false;
		}
		if (s.getSegmentController().isOnServer() && (s.isDeserializing() || s.isRevalidating())) {
			//do not cache unvalidated data on clients
			return false;
		}
		if(segmentController.isLoadByBlueprint()){
			if(debug){
				System.err.println("[SEGMENTIO] "+segmentController+" NOT WRITING AS IT'S LOADED BY BLUEPRINT UID: "+segmentController.getWriteUniqueIdentifier());
			}
			return false;
		}
		try {
			
			rwl.writeLock().lock();

			String fileName = getSegFile(s.pos.x, s.pos.y, s.pos.z, segmentController.getWriteUniqueIdentifier(), segmentController, fileNameCacheWrite, getSegmentDataPath(false));
			File f = new FileExt(fileName);
			
			if(debug){
				System.err.println("[SEGMENTIO] "+segmentController+" WRITING SEGMENT "+s.pos+" TO "+fileName+"; UID: "+segmentController.getWriteUniqueIdentifier());
			}
			SegmentRegionFileOld rf = null;
			try {

				rf = (SegmentRegionFileOld) manager.getFileAndLockIt(fileName, false);
				if (s.isEmpty()) {
					// write timestamp only without data
					onEmptySegment(s.pos.x, s.pos.y, s.pos.z, lastChanged, rf);
					// unlock done in finally block
					return false;
				}

				long t = System.currentTimeMillis();
				int seekindex = getHeaderOffset(s.pos.x, s.pos.y, s.pos.z);
				long dbTimeStamp = getTimeStamp(seekindex, rf);

				// check if s was changed at all (it has to be at least once at creation)
				if (lastChanged <= 0) {
					s.setLastChanged(System.currentTimeMillis());
				}
				if (dbTimeStamp >= lastChanged) {
					//					if(onServer){
					//						System.err.println("[SEGMENTIO] Skipping writingToDiskLock "+segmentController+" segment "+s.pos+"... db version is equal or newer; DB: "+dbTimeStamp+" - lastChangedState: "+lastChanged );
					//					}
					return false;
				}

				long timeForTimestamp = System.currentTimeMillis() - t;
				t = System.currentTimeMillis();
				//			int[] headerData = new int[2];
				//			getHeader(seekindex, headerData, rf);

				long timeForOffsetRetr = System.currentTimeMillis() - t;
				t = System.currentTimeMillis();

				SegmentOutputStream so = rf.segmentOutputStream;//new SegmentOutputStream();
				DataOutputStream dro = rf.dro;//new DataOutputStream(so);

				//			RandomFileOutputStream ro = rf.ro;//new RandomFileOutputStream(f, false);
				//			BufferedOutputStream buffRo = rf.buffRo;//new BufferedOutputStream(ro, 4096*2);
				//			DataOutputStream dro = rf.dro;//new DataOutputStream(buffRo);
				//			RandomFileOutputStream ro = new RandomFileOutputStream(f, false);
				//			BufferedOutputStream buffRo = new BufferedOutputStream(ro, 4096*2);
				//			DataOutputStream dro = new DataOutputStream(buffRo);
				boolean needsClose = true;

				//			RandomFileOutputStream ro = new RandomFileOutputStream(manager, fileName,false);
				//			DataOutputStream dro = new DataOutputStream(ro);

				int offset = rf.getBlockDataOffset(seekindex);//headerData[0];
				int dataOffset = 0;
				//		System.err.println("HeaderSize: "+(headerTotalSize+timestampTotalSize));
				if (offset < 0) {
					long previousLength = rf.getFile().length();

					dataOffset = Math.max(0, (int) ((previousLength - headerTotalSize - timestampTotalSize - versionBytes.length) / sectorInKb));
					int sizeOfOneSegment = sectorInKb;
					//			System.err.println("data doesent exist yet. new  offset at "+dataOffset+": lenOfFile: "+(ro.getFileSize())+": "+dataOffset*sectorInKb);
					rf.getFile().setLength(rf.getFile().length() + sizeOfOneSegment);
					//				ro.setFilePointer(previousLength);
				} else {

					//				ro.setFilePointer(versionBytes.length + headerTotalSize + timestampTotalSize + offset * sectorInKb);
					dataOffset = offset;
				}

				//			long dataPosition = ro.getFilePointer();

				int length = s.serialize(dro, true, lastChanged, VERSION);

				//			buffRo.flush();
				//			dro.flush();
				rf.writeSegment(versionBytes.length + headerTotalSize + timestampTotalSize + dataOffset * sectorInKb, so);

				assert (length < sectorInKb) : length + "/" + sectorInKb;
				if (length >= sectorInKb) {
					throw new IOException("Critical error. segment size exceeded file-sector-size: " + length + "; " + sectorInKb);
				}
				long timeForserialize = System.currentTimeMillis() - t;
				//			long fileSize = ro.getFileSize();
				t = System.currentTimeMillis();
				//		System.err.println("Writing length: "+length+" at "+ro.getFilePointer());

				int offsetIndex = getHeaderOffset(s.pos.x, s.pos.y, s.pos.z);
				//			if(writeHeader){
				//write new offset and timestamp
				long timeForOffset = System.currentTimeMillis() - t;

				t = System.currentTimeMillis();

				rf.setVersion(VERSION);
				rf.getFile().seek(0);
				rf.getFile().writeInt(VERSION);

				rf.getFile().seek(versionBytes.length + (offsetIndex * headerArray.length));

				assert (length < sectorInKb);
				rf.getFile().writeInt(dataOffset);
				rf.getFile().writeInt(length);

				rf.getFile().seek(versionBytes.length + headerTotalSize + (offsetIndex * timestampArray.length));
				rf.getFile().writeLong(lastChanged);

				//			}else{
				//				rf.needsHeaderWriting = true;
				//			}

				long timeForHeaderWrite = System.currentTimeMillis() - t;
				t = System.currentTimeMillis();
				//			ro.getFD().sync();
				long timeForSynch = System.currentTimeMillis() - t;

				t = System.currentTimeMillis();
				//			if(needsClose){
				//				ro.close();
				//				dro.close();
				//				buffRo.close();
				//			}

				long timeForClose = System.currentTimeMillis() - t;

				/*
				 * Update the header
				 */
				rf.setHeader(offsetIndex, dataOffset, length, lastChanged);
				//			System.err.println(onServer+"[SEGMENTIO] stats for "+s.pos+"; POS: "+dataPosition+" / "+fileSize+": length: "+length+"; Offset: "+offset+"; TIMESTAMP:"+lastChanged+"; SC: "+s.getSegmentController()+"; SYNCH: "+timeForSynch+"; TS "+timeForTimestamp+"; FOff1 "+timeForOffsetRetr+"; TSer: "+timeForserialize+"; Toff2 "+timeForOffset+"; THead "+timeForHeaderWrite+"; TCl "+timeForClose+"; sector "+sectorInKb);

				if (segmentController.isOnServer() && ServerConfig.FORCE_DISK_WRITE_COMPLETION.isOn()) {
					rf.dro.flush();
				}

//				if (segmentController.isOnServer() && ServerConfig.DEBUG_SEGMENT_WRITING.isOn()) {
//					rf.dro.flush();
//					RemoteSegment seg = new RemoteSegment((SegmentController) segmentController);
//					//					debugSegmentData.assignData(seg);
//					seg.pos.set(s.pos);
//					try {
//						request(seg.pos.x, seg.pos.y, seg.pos.z, seg);
//						boolean equals = Arrays.equals(s.getSegmentData().getAsBuffer(), seg.getSegmentData().getAsBuffer());
//						if (!equals) {
//							try {
//								throw new IOException("Exception !!!!!!!!!! ERROR in segment data writing " + seg.pos + " on " + segmentController);
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
//						} else {
//							System.err.println("SEGMENT READ IS OK!");
//						}
//
//					} catch (DeserializationException e) {
//						e.printStackTrace();
//					}
//
//				}
			} finally {
				rf.rwl.writeLock().unlock();
			}
		} catch (IOException e) {
			System.err.println("Exception HAPPENED ON SEGMENT " + s + "; controller: " + s.getSegmentController() + "; size: " + s.getSize() + "; data " + s.getSegmentData());
			e.printStackTrace();
			if (segmentController.isOnServer()) {
				((GameServerState) ((SegmentController)segmentController).getState())
						.getController().broadcastMessage(Lng.astr("ERROR while saving data!\nPlease tell an admin to\nrestart the server and to\nsend in a report\nDATA MAY BE LOST IF CONTINUED"), ServerMessage.MESSAGE_TYPE_ERROR);
			}
			throw e;
		} finally {
			rwl.writeLock().unlock();
		}

		return true;
	}


	public boolean isOnServer() {
		return onServer;
	}

}
