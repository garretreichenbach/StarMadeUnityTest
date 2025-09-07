package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RemoteLongGZipPackage extends RemoteField<long[]> {

	public RemoteLongGZipPackage() {
		super(null, null);
	}

	@Override
	public int byteLength() {
				return 0;
	}

	@Override
	@Deprecated
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {

		//		int lengthCompressed = stream.getInt();
		//		int lengthUncompressed = stream.getInt();
		//		byte[] compressed = new byte[lengthCompressed];
		//
		//		stream.get(compressed); //read compressed bytes from buffer
		//
		//		ByteArrayInputStream bin = new ByteArrayInputStream(compressed);
		//		ByteArrayOutputStream bon = new ByteArrayOutputStream(lengthUncompressed * ByteUtil.SIZEOF_LONG);
		//
		//		try {
		//			GZIPInputStream in = new GZIPInputStream(bin);
		//			byte[] buffer = new byte[4096];
		//			int bytes_read;
		//			while ((bytes_read = in.read(buffer)) > 0) {
		////				System.err.println("bytes read: "+bytes_read+" of "+buffer.length);
		//				bon.write(buffer, 0, bytes_read);
		//			}
		//
		//			in.close();
		//		} catch (IOException e) {
		//			e.printStackTrace();
		//		}
		//		byte[] uncompressed = bon.toByteArray();
		//		ByteBuffer b = ByteBuffer.wrap(uncompressed);
		//		long[] v = new long[lengthUncompressed / ByteUtil.SIZEOF_LONG];
		//		for(int i = 0; i < v.length; i++){
		//			v[i] = b.getLong();
		//		}
		//
		//		System.err.println("RECEIVED COMPRESSED ARRAY: "+Arrays.toString(v));
		//		set(v);

	}

	@Override
	@Deprecated
	public int toByteStream(DataOutputStream buffer) throws IOException {
		assert (false) : "deprecated";
		//		byte[] uncompressed = new byte[get().length * ByteUtil.SIZEOF_LONG];
		//		System.out.println("uncompressed "+uncompressed.length);
		//		ByteBuffer b = ByteBuffer.wrap(uncompressed);
		//		for(int i = 0; i < get().length; i++){
		//			b.putLong(get()[i]);
		//		}
		//		b.rewind();
		//
		//		ByteArrayOutputStream compressedOutputStream = new ByteArrayOutputStream();
		//
		//		try {
		//			GZIPOutputStream gZipOut = new GZIPOutputStream(compressedOutputStream);
		//			gZipOut.write(uncompressed);
		//			gZipOut.close();
		//			compressedOutputStream.close();
		//		} catch (IOException e) {
		//			e.printStackTrace();
		//		}
		//		byte[] compressed = compressedOutputStream.toByteArray();
		//		System.out.println("compressed "+compressed.length);
		//
		//		byte[] lenCompressed = ByteUtil.intToByteArray(compressed.length);
		//
		//		byte[] lenUncompressed = ByteUtil.intToByteArray(uncompressed.length);
		//
		//		byte[] lengthes = ByteUtil.concat(lenCompressed, lenUncompressed);
		//
		//		byte[] out = ByteUtil.concat(lengthes, compressed);
		//		System.out.println("out compressed "+out.length);
		//		buffer.put(out);
		return -1;
	}

}
