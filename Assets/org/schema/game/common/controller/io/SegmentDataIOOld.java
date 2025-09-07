package org.schema.game.common.controller.io;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.ClientStatics;
import org.schema.game.common.controller.SegmentBufferIteratorEmptyInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.DeserializationException;
import org.schema.game.common.data.world.RemoteSegment;
import org.schema.game.common.data.world.Segment;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.resource.FileExt;

public class SegmentDataIOOld {

	public static final int READ_DATA = 0;
	public static final int READ_EMPTY = 1;
	public static final int READ_NO_DATA = 2;
	
	private static final Vector3i maxSegementsPerFile = new Vector3i(16, 16, 16);
	static final int size = maxSegementsPerFile.x * maxSegementsPerFile.y * maxSegementsPerFile.z;
	static final int headerTotalSize = size * 8; //32kb
	static final int timestampTotalSize = size * 8; //32kb
	private final static int sectorInKb = 5 * 1024; //real size = (16*16*16*3)/1024 = 12
	private static byte[] emptyHeader = new byte[headerTotalSize + timestampTotalSize];
	private static byte[] minusOne = ByteUtil.intToByteArray(-1);

	static {
		int i = 0;
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
		assert (i == emptyHeader.length / 2) : i + "/" + emptyHeader.length / 2;

	}

	//	private int dataChunkSize = SegmentData.SEG * SegmentData.SEG+ SegmentData.SEG * 32; // == 128 kb

	private final String segmentDataPath;
	int[] headerData = new int[2];
	private SegmentController segmentController;
	private byte[] headerArray = new byte[8]; //data: pos + length
	private byte[] timestampArray = new byte[8];
	private boolean onServer;
	private long timeStampSeekDebug;

	public SegmentDataIOOld(SegmentController segmentController, boolean onServer) {
		this.segmentController = segmentController;
		this.onServer = onServer;
		if (onServer) {
			this.segmentDataPath = GameServerState.SEGMENT_DATA_DATABASE_PATH;
		} else {
			this.segmentDataPath = ClientStatics.SEGMENT_DATA_DATABASE_PATH;
		}
	}

	public static int getOffsetSeekIndex(int x, int y, int z) {
		/*
		 *
		 * bit operation has a huge speed advatage
		 * int xSeg = Math.abs( x  / SegmentData.SEG ) % maxSegementsPerFile.x;
		 * int ySeg = Math.abs( y  / SegmentData.SEG ) % maxSegementsPerFile.y;
		 * int zSeg = Math.abs( z  / SegmentData.SEG ) % maxSegementsPerFile.z;
		 *
		 * (proved to equal results for all integers)
		 */

		int xSeg = (Math.abs(x) >> 4) & 0xF;
		int ySeg = (Math.abs(y) >> 4) & 0xF;
		int zSeg = (Math.abs(z) >> 4) & 0xF;

		int offIndex = ((zSeg * maxSegementsPerFile.y * maxSegementsPerFile.x)
				+ (ySeg * maxSegementsPerFile.x) + xSeg);

		assert (offIndex < size) : offIndex + "/" + size + ": " + x + ", " + y + ", " + z + " ---> " + xSeg + ", " + ySeg + ", " + zSeg + " ";
		return offIndex;
	}

	public static void main(String[] sa) {

		//		System.err.println(Arrays.toString(ByteUtil.intToByteArray(-1, new byte[4])));
		//		System.err.println(ByteUtil.encodeBytesToInt(-1, -1, -1, -1));
	}

	private void createSegmentControllerData(int x, int y, int z, File f) throws IOException {
		long t = System.currentTimeMillis();
		FileOutputStream fileOutputStream = new FileOutputStream(f);
		BufferedOutputStream out = new BufferedOutputStream(fileOutputStream);
		DataOutputStream dataOutputStream = new DataOutputStream(out);

		writeEmptyHeader(dataOutputStream);
		dataOutputStream.close();
		fileOutputStream.close();
		out.close();
		fileOutputStream.close();
		System.err.println("Wrote Empty Header " + emptyHeader.length + ": " + f.getName() + " - " + segmentController.getState() + "; " + (System.currentTimeMillis() - t) + "ms");
	}

	void getHeader(int x, int y, int z, int[] header) throws IOException {
		String s = getSegFile(x, y, z, segmentController);
		//		File f = new FileExt(s);
		//		new RandomAccessFile(f, "r");

		RandomAccessFile rf = new RandomAccessFile(s, "r");
		synchronized (rf) {
			getHeader(x, y, z, header, rf);
		}
		//		rf.close();
	}

	private void getHeader(int x, int y, int z, int[] header, RandomAccessFile rf) throws IOException {

		int offIndex = getOffsetSeekIndex(x, y, z);
		getHeader(offIndex, header, rf);
	}

	private void getHeader(int offIndex, int[] header, RandomAccessFile rf) throws IOException {
		rf.seek(offIndex * headerArray.length);
		header[0] = rf.readInt();
		header[1] = rf.readInt();
	}

	String getSegFile(int x, int y, int z, SegmentController segmentController) {
		int xS = ByteUtil.divSeg(x) / maxSegementsPerFile.x - (x < 0 ? 1 : 0);
		int yS = ByteUtil.divSeg(y) / maxSegementsPerFile.y - (y < 0 ? 1 : 0);
		int zS = ByteUtil.divSeg(z) / maxSegementsPerFile.z - (z < 0 ? 1 : 0);
		return segmentDataPath + segmentController.getUniqueIdentifier() + "." + xS + "." + yS + "." + zS + ".smd";
	}

	public final String getSegmentDataPath() {
		return segmentDataPath;
	}

	public long getTimeStamp(int x, int y, int z) throws IOException {
		synchronized (this) {
			String s = getSegFile(x, y, z, segmentController);
			File f = new FileExt(s);
			if (!f.exists()) {
				//			System.err.println("FILE DOES NOT EXIST IN DB FOR SEGMENT !"+x+","+y+","+z);
				return -1;
			}
			RandomAccessFile rf = new RandomAccessFile(s, "r");//new RandomAccessFile(f, "r");
			synchronized (rf) {

				int offIndex = getOffsetSeekIndex(x, y, z);

				rf.seek(headerTotalSize + offIndex * timestampArray.length);
				try {
					long timestamp = rf.readLong();

					//			rf.close();
					return timestamp;
				} catch (Exception e) {
					e.printStackTrace();
					//			rf.close();
					return -1;
				} finally {
					//			rf.close();
					rf.close();
				}
			}

		}
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

	private void onEmptySegment(RemoteSegment s) throws IOException {
		File f = new FileExt(getSegFile(s.pos.x, s.pos.y, s.pos.z, segmentController));

		RandomFileOutputStream ro = new RandomFileOutputStream(f, false);
		DataOutputStream dro = new DataOutputStream(ro);

		//write new timestamp
		int offset = getOffsetSeekIndex(s.pos.x, s.pos.y, s.pos.z);

		ro.setFilePointer(offset * headerArray.length);

		//set offset to 0 so we know there is no data
		dro.writeInt(-1);
		//also set length to to indicate a empty segment
		dro.writeInt(-1);

		ro.setFilePointer(headerTotalSize + offset * timestampArray.length);
		//		System.err.println("SETTING LAST CHANGED OF EMPTY SEGMENT: "+s.getLastChanged());
		dro.writeLong(s.getLastChanged());

		dro.flush();
		ro.flush();

		ro.close();
		dro.close();
	}

	public void purge() {
		synchronized (this) {
			segmentController.getSegmentBuffer().iterateOverEveryElement(new SegmentBufferIteratorEmptyInterface() {

				@Override
				public boolean handle(Segment s, long lastChanged) {
					File f = new FileExt(getSegFile(s.pos.x, s.pos.y, s.pos.z, segmentController));
					if (f.exists()) {
						f.delete();
					}
					return true;
				}

				@Override
				public boolean handleEmpty(int posX, int posY, int posZ, long lastChanged) {
					File f = new FileExt(getSegFile(posX, posY, posZ, segmentController));
					if (f.exists()) {
						f.delete();
					}
					return true;
				}
			}, true);

		}
	}

	public void purge(RemoteSegment s) throws IOException {
		File f = new FileExt(getSegFile(s.pos.x, s.pos.y, s.pos.z, segmentController));
		RandomFileOutputStream ro = new RandomFileOutputStream(f, false);
		DataOutputStream dro = new DataOutputStream(ro);
		//write new timestamp
		int offset = getOffsetSeekIndex(s.pos.x, s.pos.y, s.pos.z);

		ro.setFilePointer(headerTotalSize + offset * timestampArray.length);

		dro.writeLong(0);

		dro.flush();
		ro.flush();

		ro.close();
		dro.close();
	}

	public int request(int x, int y, int z, RemoteSegment seg)
			throws IOException, DeserializationException {
		synchronized (this) {

			String s = getSegFile(x, y, z, segmentController);

			File f = new FileExt(s);

			if (!f.exists()) {
				createSegmentControllerData(x, y, z, f);
				return READ_NO_DATA;
			} else {
				RandomAccessFile rf = new RandomAccessFile(s, "r");//new RandomAccessFile(f, "r");
				synchronized (rf) {
					int offIndex = getOffsetSeekIndex(x, y, z);

					getHeader(offIndex, headerData, rf);
					int offset = headerData[0];
					int length = headerData[1];

					if (offset < 0) {

						if (length < 0) {
							try {
								rf.seek(headerTotalSize + offIndex
										* timestampArray.length);
								long timestamp = rf.readLong();
								seg.setLastChanged(System.currentTimeMillis());
							} catch (IOException e) {
								System.err.println("COULD NOT READ TIMESTAMP FOR "
										+ seg + " ... " + e.getMessage());
								seg.setLastChanged(System.currentTimeMillis());
							}
							return READ_EMPTY;
						} else {
							return READ_NO_DATA;
						}
					} else {
						long timeStampSeekPosition = headerTotalSize + offIndex * timestampArray.length;
						assert (timeStampSeekPosition < rf.length()) : " " + timeStampSeekPosition + " / " + rf.length() + " on " + " (" + x + ", " + y + ", " + z + ") on " + f.getName() + " " + seg.getSegmentController() + " offest(" + offset + "); offsetIndex(" + offIndex + ")";
						rf.seek(timeStampSeekPosition);
						this.timeStampSeekDebug = timeStampSeekPosition;
						long timestamp = rf.readLong();

						assert (length > 0 && length < sectorInKb) : " len: " + length + " / " + sectorInKb + " ON " + f.getName() + " (" + x + ", " + y + ", " + z + ")";

						long dataPosition = (headerTotalSize + timestampTotalSize)
								+ offset * sectorInKb;
						rf.seek(dataPosition);

						synchronized (segmentController.getState().getDataBuffer()) {

							rf.readFully(segmentController.getState()
									.getDataBuffer(), 0, length);

							ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
									segmentController.getState().getDataBuffer(),
									0, length);
							DataInputStream in = new DataInputStream(
									byteArrayInputStream);

							seg.deserialize(in, length, false, true, segmentController.getState().getUpdateTime());

							//							seg.setLastChanged(timestamp);
							//set last changed to now since it has to be saved in the new version no matter what
							seg.setLastChanged(System.currentTimeMillis());
							byteArrayInputStream.close();

							in.close();

							//						rf.close();
						}
						return READ_DATA;
					}

				}
			}
		}
	}

	public void write(RemoteSegment s) throws IOException {
		//		try{
		//		throw new NullPointerException();
		//		}catch(NullPointerException e){
		//			e.printStackTrace();
		//		}
		synchronized (this) {
			File f = new FileExt(getSegFile(s.pos.x, s.pos.y, s.pos.z, segmentController));

			if (!f.exists()) {
				createSegmentControllerData(s.pos.x, s.pos.y, s.pos.z, f);
			}

			if (s.isEmpty()) {
				// write timestamp only without data
				onEmptySegment(s);
				return;
			}

			long t = System.currentTimeMillis();
			long dbTimeStamp = getTimeStamp(s.pos.x, s.pos.y, s.pos.z);

			// check if s was changed at all (it has to be at least once at creation)
			if (s.getLastChanged() <= 0) {
				s.setLastChanged(System.currentTimeMillis());
			}
			if (dbTimeStamp >= s.getLastChanged()) {
				if (!onServer) {
					//					System.err.println("[SEGMENTIO] Skipping writingToDiskLock segment "+s.pos+"... db version is equal or newer; DB: "+dbTimeStamp+" - lastChangedState: "+s.getLastChanged() );
				}
				return;
			}

			long timeForTimestamp = System.currentTimeMillis() - t;
			t = System.currentTimeMillis();

			getHeader(s.pos.x, s.pos.y, s.pos.z, headerData);

			long timeForOffsetRetr = System.currentTimeMillis() - t;
			t = System.currentTimeMillis();

			RandomFileOutputStream ro = new RandomFileOutputStream(f, false);
			DataOutputStream dro = new DataOutputStream(ro);

			int offset = headerData[0];
			int dataOffset = 0;
			//		System.err.println("HeaderSize: "+(headerTotalSize+timestampTotalSize));
			if (offset < 0) {
				long previousLength = ro.getFileSize();

				dataOffset = Math.max(0, (int) ((previousLength - headerTotalSize - timestampTotalSize) / sectorInKb));
				int sizeOfOneSegment = sectorInKb;
				//			System.err.println("data doesent exist yet. new  offset at "+dataOffset+": lenOfFile: "+(ro.getFileSize())+": "+dataOffset*sectorInKb);
				ro.setFileSize(ro.getFileSize() + sizeOfOneSegment);
				ro.setFilePointer(previousLength);
			} else {
				ro.setFilePointer(headerTotalSize + timestampTotalSize + offset * sectorInKb);
				dataOffset = offset;
			}

			long dataPosition = ro.getFilePointer();

			int length = s.serialize(dro, true, System.currentTimeMillis(), SegmentDataIO16.VERSION);

			assert (length < sectorInKb) : length + "/" + sectorInKb;
			long timeForserialize = System.currentTimeMillis() - t;
			long fileSize = ro.getFileSize();
			t = System.currentTimeMillis();
			//		System.err.println("Writing length: "+length+" at "+ro.getFilePointer());

			//write new offset and timestamp
			int offsetIndex = getOffsetSeekIndex(s.pos.x, s.pos.y, s.pos.z);

			long timeForOffset = System.currentTimeMillis() - t;

			t = System.currentTimeMillis();

			ro.setFilePointer(offsetIndex * headerArray.length);

			assert (length < sectorInKb);
			dro.writeInt(dataOffset);
			dro.writeInt(length);

			//		System.err.println("WRITING OFFSET: "+dataOffset);
			ro.setFilePointer(headerTotalSize + offsetIndex * timestampArray.length);
			dro.writeLong(s.getLastChanged());

			long timeForHeaderWrite = System.currentTimeMillis() - t;
			t = System.currentTimeMillis();
			//			ro.getFD().sync();
			long timeForSynch = System.currentTimeMillis() - t;

			t = System.currentTimeMillis();
			ro.flush();
			dro.flush();
			dro.close();
			ro.close();

			long timeForClose = System.currentTimeMillis() - t;

			//				System.err.println(onServer+"[SEGMENTIO] stats for "+s.pos+"; POS: "+dataPosition+" / "+fileSize+": length: "+length+"; Offset: "+offset+"; TIMESTAMP:"+s.getLastChanged()+"; SC: "+s.getSegmentController()+"; SYNCH: "+timeForSynch+"; TS "+timeForTimestamp+"; FOff1 "+timeForOffsetRetr+"; TSer: "+timeForserialize+"; Toff2 "+timeForOffset+"; THead "+timeForHeaderWrite+"; TCl "+timeForClose+"; sector "+sectorInKb);
			//			}
		}
	}

	private void writeEmptyHeader(DataOutputStream out) throws IOException {

		out.write(emptyHeader);
		out.flush();
	}

}
