package org.schema.game.common.data.world.migration;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.GameResourceLoader;
import org.schema.game.common.controller.io.SegmentDataIO16;
import org.schema.game.common.controller.io.SegmentDataIOOld;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.DeserializationException;
import org.schema.game.common.data.world.RemoteSegment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentData4Byte;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.resource.FileExt;

import java.io.*;

public class Migration00898 {

	
	private final static int sectorInKb = 5 * 1024; //real size = (16*16*16*3)/1024 = 12
	private static final Vector3i maxSegementsPerFile = new Vector3i(16, 16, 16);
	static final int size = maxSegementsPerFile.x * maxSegementsPerFile.y * maxSegementsPerFile.z;
	static final int headerTotalSize = size * 8; //32kb
	static final int timestampTotalSize = size * 8; //32kb
	static long lastChanged;
	private static byte[] headerArray = new byte[8];
	private static byte[] timestampArray = new byte[8];

	private static void getHeader(int offIndex, int[] header, RandomAccessFile rf, int[] headerData) throws IOException {
		rf.seek(offIndex * headerArray.length);
		header[0] = rf.readInt();
		header[1] = rf.readInt();
	}

	public static void main(String[] args) {
		File dir = new FileExt("./server-database/DATA");
		try {
			migrate(dir);
			FileUtil.deleteDir(new FileExt("./client-database"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	//	public static void migrate(SegmentDataV0078 from, SegmentData to){
	//		for(byte x = 0; x < 16; x++){
	//			for(byte y = 0; y < 16; y++){
	//				for(byte z = 0; z < 16; z++){
	//
	//					int fromIndex = from.getInfoIndex(x, y, z);
	//					int toIndex = to.getInfoIndex(x,y,z);
	//
	//
	//					short type = from.getType(fromIndex);
	//
	//					if(type != Element.TYPE_NONE){
	//						to.setType(toIndex, (short) Math.abs(type));
	//						try{
	//							short maxHitPoints = ElementKeyMap.getInfo(type).getMaxHitPoints();
	//							to.setHitpoints(toIndex, maxHitPoints);
	//						}catch(Exception e){
	//							e.printStackTrace();
	//							System.err.println("type "+type);
	//						}
	//					}else{
	//					}
	//				}
	//			}
	//		}
	//	}

	public static void migrate(File dir) throws IOException {
		byte[] exbuffer = new byte[1024 * 1024];
		ElementKeyMap.initializeData(GameResourceLoader.getConfigInputFile());
		File[] listFiles = dir.listFiles();
		byte[] buffer = new byte[3 * 1024 * 1024];
		int[] headerData = new int[2];
		SegmentData data = new SegmentData4Byte(true);

		for (File f : listFiles) {

			try {
				System.err.println("migrating " + f.getAbsolutePath() + " "
						+ f.exists());
				if (!f.getName().endsWith(".smd")) {
					continue;
				}
				System.err.println("TRYING TO MIGRATE: " + f.getAbsolutePath());
				RandomAccessFile rf = new RandomAccessFile(
						f.getAbsolutePath(), "r");// new
				// RandomAccessFile(f,
				// "r");
				for (int z = 0; z < 16 * 16; z += 16) {
					for (int y = 0; y < 16 * 16; y += 16) {
						for (int x = 0; x < 16 * 16; x += 16) {

							int offIndex = SegmentDataIOOld.getOffsetSeekIndex(x,
									y, z);

							getHeader(offIndex, headerData, rf, headerData);
							int offset = headerData[0];
							int length = headerData[1];

							if (offset < 0) {
								continue;
							} else {

								long timeStampSeekPosition = headerTotalSize
										+ offIndex * timestampArray.length;
								assert (timeStampSeekPosition < rf.length()) : " "
										+ timeStampSeekPosition + " / "
										+ rf.length() + " on " + " (" + x + ", "
										+ y + ", " + z + ") on " + f.getName()
										+ " " + " offest(" + offset
										+ "); offsetIndex(" + offIndex + ")";
								rf.seek(timeStampSeekPosition);
								long timestamp = rf.readLong();

								assert (length > 0 && length < sectorInKb) : " len: "
										+ length
										+ " / "
										+ sectorInKb
										+ " ON "
										+ f.getName()
										+ " ("
										+ x
										+ ", "
										+ y
										+ ", "
										+ z + ")";

								long dataPosition = (headerTotalSize + timestampTotalSize)
										+ offset * sectorInKb;
								rf.seek(dataPosition);

								RemoteSegment seg = new RemoteSegment(null);
								data.reset(0);
								seg.setSegmentData(data);

								rf.readFully(exbuffer, 0, length);

								ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
										exbuffer, 0, length);
								DataInputStream in = new DataInputStream(
										byteArrayInputStream);

								try {
									seg.deserialize(in, length, true, true, 0);
								} catch (DeserializationException e) {
									e.printStackTrace();
								}
								//set size > 0 so it is written
								seg.setSize(1);

								System.err.println("MIGRATING: " + x + ", " + y + ", " + z + " -> " + seg.pos);

								// seg.setLastChanged(timestamp);
								// set last changed to now since it has to be saved
								// in the new version no matter what
								seg.setLastChanged(System.currentTimeMillis());
								byteArrayInputStream.close();

								in.close();

								// ###########################WRITE

								SegmentDataIO16.writeStatic(seg, headerData, SegmentDataIO16.VERSION, f);
							}
						}
					}
				}
				rf.close();
				f.deleteOnExit();
				boolean delete = f.delete();
				System.err.println("DELETING: " + f.getAbsolutePath() + ": " + delete);

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

}
