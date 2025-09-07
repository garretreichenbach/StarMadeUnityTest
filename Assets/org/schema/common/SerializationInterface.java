package org.schema.common;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;

public interface SerializationInterface {
	public void serialize(DataOutput b, boolean isOnServer) throws IOException;

	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException;
	
	public static void visualizeLong(String name, long l) {
		try {
			byte[] p = new byte[8];
			FastByteArrayOutputStream f = new FastByteArrayOutputStream(p);
			DataOutputStream m = new DataOutputStream(f);
			m.writeLong(l);
			long val = 
					(((long)p[0] << 56) + 
					((long)(p[1]&255) << 48) + 
					((long)(p[2]&255) << 40) + 
					((long)(p[3]&255) << 32) + 
					((long)(p[4]&255) << 24) + 
					((p[5]&255) << 16) + 
					((p[6]&255) << 8) + 
					((p[7]&255) << 0));
//			long val = 
//					(((long)p[0] << 56) +
//	                ((long)(p[1] & 255) << 48) +
//	                ((long)(p[2] & 255) << 40) +
//	                ((long)(p[3] & 255) << 32) +
//	                ((long)(p[4] & 255) << 24) +
//	                ((p[5] & 255) << 16) +
//	                ((p[6] & 255) <<  8) +
//	                ((p[7] & 255) <<  0));
			
			System.err.println(name+" BYTES: "+(p[0]&0xFF)+", "+(p[1]&0xFF) + ", " + (p[2]&0xFF) + ", " + (p[3]&0xFF) + ", " + (p[4]&0xFF) + ", " + (p[5]&0xFF) + ", " + (p[6]&0xFF) + ", " + (p[7]&0xFF) + " => "+l+"; ; FROM ** "+val);
			
			
			m.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static void set(SerializationInterface from, SerializationInterface to) throws IOException {
		FastByteArrayOutputStream outStream = new FastByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(outStream);
		from.serialize(out, false);
		FastByteArrayInputStream inStream = new FastByteArrayInputStream(outStream.array, 0, outStream.length);
		DataInputStream in = new DataInputStream(inStream);
		to.deserialize(in, 0, false);
	}
}