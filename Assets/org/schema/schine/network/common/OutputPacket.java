package org.schema.schine.network.common;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;

public class OutputPacket {
	
	
	
	private final FastByteArrayOutputStream out;
	public final DataOutputStream payload;
	public long time;

	public String debug;
	public long socketId = -1;
	
	public OutputPacket(FastByteArrayOutputStream out, int delay) {
		super();
		if (delay > 0) {
			this.time = System.currentTimeMillis();
		}
		this.out = out;
		this.payload = new DataOutputStream(out);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
	}

	public void reset() {
		out.reset();
		debug = null;
		socketId = -1;
	}

	public void writeTo(DataOutputStream to) throws IOException {
		assert (size() > 0);
		if(debug != null) {
			System.err.println("[PACKETDEBUGSEND] "+debug+"; Size: "+size());
		}
		to.writeInt(size()); // size
		to.write(out.array, 0, size());		
	}

	public int size() {
		return (int) out.position();
	}

	public void writeTo(SocketChannel chan, ByteBuffer bufOut) throws IOException {
		assert (size() > 0):size();
		bufOut.clear();
		putintoFrom(bufOut, 0);
		bufOut.flip();
		chan.write(bufOut);
	}

	public void putintoFrom(ByteBuffer byteBuffer, int from) {
		byteBuffer.putInt(size());
		byteBuffer.put(out.array, from, size()-from);
	}

}
