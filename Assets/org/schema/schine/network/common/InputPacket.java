package org.schema.schine.network.common;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;

public class InputPacket {
	
	private FastByteArrayInputStream in;
	public DataInputStream payload;
	public long time;
	public long socketId = -1;
	private int readLen;
	public final byte[] lenInt = new byte[4];
	public final FastByteArrayInputStream st = new FastByteArrayInputStream( lenInt);
	public final DataInputStream dt = new DataInputStream( st);
	private int bLen;
	private int packetRead;

	public InputPacket(int initialSize) {
		super();
		this.in = new FastByteArrayInputStream(new byte[initialSize]);
		this.payload = new DataInputStream(in);
	}

	public void readFully(DataInput from, int size) throws IOException {
		ensureSize(size, false);
		in.length = size;
		
		//transfer data to packet
		from.readFully(in.array, 0, size);
	}

	public void reset() {
		in.reset();
		st.reset();
		st.position(0);
		Arrays.fill(lenInt, (byte)0);
		st.length = 4;
		st.offset = 0;
		readLen = 0;
		packetRead = 0;
		socketId = -1;
	}


	public int size() {
		return (int)in.length();
	}
	private void ensureSize(int size, boolean copy) {
		if(size > in.array.length) {
			int cSize = in.array.length;
			while(cSize < size) {
				cSize *= 2;
			}
//			System.err.println("increased size: "+cSize+"; "+size+"; from "+in.length);
			byte[] newCop = new byte[cSize];
			if(copy) {
				System.arraycopy(in.array, 0, newCop, 0, in.array.length);
			}
			in = new FastByteArrayInputStream(newCop);
			this.payload = new DataInputStream(in);
		}
	}
	public void readFully(final SocketChannel chan, final ByteBuffer buf, final int len) throws IOException {
		assert(len > 0):len;
		ensureSize(len, false);
		DataProcessorChannel.ensure (buf, len, chan);
		assert(len < in.array.length):"OOB: "+len+" / "+in.array.length;
		buf.get (in.array, 0, len);
		in.length = len;
		in.offset = 0;
		in.position(0);
	}
	public void readMessageLen(ByteBuffer buf) throws IOException {
		assert(buf.hasRemaining()):"Empty buffer";
		int toAdd = Math.min(4-readLen, buf.remaining());
		buf.get (st.array, readLen, 4-readLen);
		readLen += toAdd;
		assert(readLen <= 4):readLen;
		if(readLen == 4) {
			this.bLen = dt.readInt();
			ensureSize(this.bLen, false);
		}else {
		}
	}
	public boolean isReadLen() {
		return readLen == 4;
	}
	public int getLenFromBuffer() {
		assert(isReadLen());
		return bLen;
	}
	public boolean readProcedural(ByteBuffer buf, int len) {
		
		int readUpTo = Math.min(buf.remaining(), len-packetRead);
//		System.err.println("READ:: "+packetRead+"; lR: "+readUpTo+"; Rem "+buf.remaining()+"; "+in.array.length);
		buf.get (in.array, packetRead, readUpTo);
		
		
		packetRead += readUpTo;
		
		if(packetRead == len) {
			in.length = len;
			in.offset = 0;
			in.position(0);
//			System.err.println("COMPLETED READ "+len);
			return true;
		}
//		System.err.println("PARTIAL READ "+packetRead);
		assert(packetRead < len);
		return false;
	}
	
	public void writeToMessage(ByteBuffer buf, int len) {
		assert(len < in.array.length):"OOB: "+len+" / "+in.array.length;
		buf.get (in.array, 0, len);
		in.length = len;
		in.offset = 0;
		in.position(0);
	}

}
