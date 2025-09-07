package org.schema.game.common.data.world.migration;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.GameResourceLoader;
import org.schema.game.common.controller.io.RandomFileOutputStream;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.*;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.resource.FileExt;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Migration0061 {
	public static final Vector3i maxSegementsPerFile = new Vector3i(16, 16, 16);
	static final int size = maxSegementsPerFile.x * maxSegementsPerFile.y * maxSegementsPerFile.z;
	static final int headerTotalSize = size * 8;
	static final int timestampTotalSize = size * 8; //32kb
	static final int sectorInKb = 5 * 1024;
	static byte[] headerArray = new byte[8]; //data: pos + length
	static byte[] timestampArray = new byte[8];
	;
	static int x, y, z;
	static long lastChanged;

	public static void main(String[] args) {
		File dir = new FileExt("./server-database/DATA");
		try {
			migrate0061to0062(dir);
			FileUtil.deleteDir(new FileExt("./client-database"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void migrate(SegmentDataV0061 from, SegmentData to) {
		for (byte x = 0; x < 16; x++) {
			for (byte y = 0; y < 16; y++) {
				for (byte z = 0; z < 16; z++) {

					int fromIndex = from.getInfoIndex(x, y, z);
					int toIndex = SegmentData.getInfoIndex(x, y, z);

					short type = from.getType(fromIndex);

					if (type != Element.TYPE_NONE) {
						try{
							to.setType(toIndex, (short) Math.abs(type));
						} catch (SegmentDataWriteException e) {
							e.printStackTrace();
							throw new RuntimeException("this should be never be thrown as migration should always be to"
									+ "a normal segment data", e);
						}
						try {
							to.setHitpointsByte(toIndex, ElementKeyMap.MAX_HITPOINTS);
						} catch (Exception e) {
							e.printStackTrace();
							System.err.println("type " + type);
						}
					} else {
					}
				}
			}
		}
	}

	public static void migrate0061to0062(File dir) throws IOException {
		ElementKeyMap.initializeData(GameResourceLoader.getConfigInputFile());
		File[] listFiles = dir.listFiles();
		byte[] buffer = new byte[3 * 1024 * 1024];
		for (File f : listFiles) {

			System.err.println("migrating " + f.getAbsolutePath() + " " + f.exists());
			if (!f.getName().endsWith(".smd")) {
				continue;
			}

			long offsetIndex = 0;
			RandomAccessFile rf = new RandomAccessFile(f, "rw");
			while (offsetIndex < headerTotalSize) {
				rf.seek(offsetIndex);

				int offset = rf.readInt();
				int length = rf.readInt();

				try {
					if (offset >= 0) {

						SegmentDataV0061 read = read0061(rf, offset, offsetIndex / headerArray.length, length, buffer);

						SegmentData updated = new SegmentData4Byte(false);

						migrate(read, updated);

						write(f, offset, offsetIndex / headerArray.length, updated);

						System.err.println("DONE MIGRATED: " + f.getAbsolutePath() + " (" + offset + ")");
					} else {
						assert (length <= 0) : length;
					}

				} catch (DeserializationException e) {
					System.err.println("SEEK POS: " + offsetIndex);
					e.printStackTrace();
				} catch (IOException e) {
					System.err.println("SEEK POS: " + offsetIndex);
					e.printStackTrace();
				}
				offsetIndex += headerArray.length;

			}

			rf.close();
		}

	}

	public static SegmentDataV0061 read0061(DataInput dataInputStream) throws IOException {
		DataInputStream zip;
		if (size < 0) {
			zip = new DataInputStream(new GZIPInputStream((InputStream)dataInputStream));
		} else {
			zip = new DataInputStream(new GZIPInputStream((InputStream)dataInputStream, size));
		}
		//		System.err.println("READING "+getSegmentController()+"; "+getSegmentController().getUniqueIdentifier()+"; "+pos);
		lastChanged = zip.readLong();
		x = zip.readInt();
		y = zip.readInt();
		z = zip.readInt();
		byte dataByte = zip.readByte();
		SegmentDataV0061 v = new SegmentDataV0061(false);
		v.deserialize(zip);
		return v;
	}

	private static SegmentDataV0061 read0061(RandomAccessFile rf,
	                                         int offset, long offsetIndex, int length, byte[] buffer) throws IOException, DeserializationException {

		long tsIndex = headerTotalSize + offsetIndex * timestampArray.length;

		long dataPosition = (headerTotalSize + timestampTotalSize)
				+ offset * sectorInKb;

		System.err.println("LEN " + rf.length() + " -> " + tsIndex + "; offest: " + offset + "; headerSize: " + headerTotalSize + "; tsArray " + timestampArray.length + "; read lenth: " + length + " -- data position: " + dataPosition);

		rf.seek(tsIndex);
		long timestamp = rf.readLong();

		//		System.err.println("TIMESTAMP: "+timestamp+"; datapos: "+dataPosition);
		rf.seek(dataPosition);

		assert (length > 0 && length < sectorInKb && length <= buffer.length) : "OHOH: " + length + " / " + buffer.length;

		rf.readFully(buffer, 0, length);

		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer,
				0, length);

		DataInputStream in = new DataInputStream(
				byteArrayInputStream);

		SegmentDataV0061 s = read0061(in);
		byteArrayInputStream.close();

		in.close();
		return s;

	}

	private static int searializeData(DataOutputStream outputStream, int x, int y, int z, long lastChanged, short version, SegmentData data) throws IOException {
		DataOutputStream wrap = new DataOutputStream(outputStream);
		assert (wrap.size() == 0);
		GZIPOutputStream zz;
		DataOutputStream zip = new DataOutputStream(zz = new GZIPOutputStream(wrap));

		System.err.println("SERIALIZING: " + x + ", " + y + ", " + z + "; change: " + lastChanged);

		zip.writeLong(lastChanged);

		zip.writeInt(x);
		zip.writeInt(y);
		zip.writeInt(z);

		zip.writeByte(RemoteSegment.DATA_AVAILABLE_BYTE);

		data.serialize(zip);
		//		} else {
		//			assert (isEmpty());
		//			zip.writeByte(DATA_EMPTY_BYTE);
		////				 System.err.println(getSegmentController().getState()+" data EMPTY for "+pos);
		//
		//		}

		zz.finish();
		zz.flush();


		/*
		 * the size of the actual (comrpressed) written bytes.
		 * zip.size wouldnt give the right size,
		 * since it counts all incoming bytes and not that written to
		 * the underlying zip stream
		 */
		return wrap.size();
	}

	private static void write(File f, int offset, long offsetIndex, SegmentData data) throws IOException {

		RandomFileOutputStream ro = new RandomFileOutputStream(f, false);
		DataOutputStream dro = new DataOutputStream(ro);

		ro.setFilePointer(headerTotalSize + timestampTotalSize + offset * sectorInKb);

		int length = searializeData(dro, x, y, z, lastChanged, (short) 1, data);

		ro.setFilePointer(offsetIndex * headerArray.length);

		System.err.println("WRITING AT OFFSET: " + offset + " with length " + length);

		dro.writeInt(offset);
		dro.writeInt(length);

		ro.setFilePointer(headerTotalSize + offsetIndex * timestampArray.length);
		dro.writeLong(lastChanged);

		dro.flush();
		ro.flush();
		ro.close();
	}

}
