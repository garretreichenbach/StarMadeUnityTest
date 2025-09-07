package org.schema.schine.resource.tag;

import java.io.DataInputStream;
import java.io.IOException;

public class ProfiledDataInputStream extends java.io.FilterInputStream implements java.io.DataInput {
	private final DataInputStream d;

	private long read = 0;

	public ProfiledDataInputStream(DataInputStream d) {
		super(d);
		this.d = d;
	}

	/**
	 * @param b
	 * @throws IOException
	 * @see java.io.DataInputStream#readFully(byte[])
	 */
	@Override
	public final void readFully(byte[] b) throws IOException {
		read += b.length;
		d.readFully(b);
	}

	/**
	 * @param b
	 * @param off
	 * @param len
	 * @throws IOException
	 * @see java.io.DataInputStream#readFully(byte[], int, int)
	 */
	@Override
	public final void readFully(byte[] b, int off, int len) throws IOException {
		read += Math.min(b.length, len);
		d.readFully(b, off, len);
	}

	@Override
	public int skipBytes(int n) throws IOException {
		read += n;
		return d.skipBytes(n);
	}

	/**
	 * @return
	 * @throws IOException
	 * @see java.io.DataInputStream#readBoolean()
	 */
	@Override
	public final boolean readBoolean() throws IOException {
		read += 1;
		return d.readBoolean();
	}

	/**
	 * @return
	 * @throws IOException
	 * @see java.io.DataInputStream#readByte()
	 */
	@Override
	public final byte readByte() throws IOException {
		read += 1;
		return d.readByte();
	}

	/**
	 * @return
	 * @throws IOException
	 * @see java.io.DataInputStream#readUnsignedByte()
	 */
	@Override
	public final int readUnsignedByte() throws IOException {
		read += 1;
		return d.readUnsignedByte();
	}

	/**
	 * @return
	 * @throws IOException
	 * @see java.io.DataInputStream#readShort()
	 */
	@Override
	public final short readShort() throws IOException {
		read += 2;
		return d.readShort();
	}

	/**
	 * @return
	 * @throws IOException
	 * @see java.io.DataInputStream#readUnsignedShort()
	 */
	@Override
	public final int readUnsignedShort() throws IOException {
		read += 2;
		return d.readUnsignedShort();
	}

	/**
	 * @return
	 * @throws IOException
	 * @see java.io.DataInputStream#readChar()
	 */
	@Override
	public final char readChar() throws IOException {
		read += 1;
		return d.readChar();
	}

	/**
	 * @return
	 * @throws IOException
	 * @see java.io.DataInputStream#readInt()
	 */
	@Override
	public final int readInt() throws IOException {
		read += 4;
		return d.readInt();
	}

	/**
	 * @return
	 * @throws IOException
	 * @see java.io.DataInputStream#readLong()
	 */
	@Override
	public final long readLong() throws IOException {
		read += 8;
		return d.readLong();
	}

	/**
	 * @return
	 * @throws IOException
	 * @see java.io.DataInputStream#readFloat()
	 */
	@Override
	public final float readFloat() throws IOException {
		read += 4;
		return d.readFloat();
	}

	/**
	 * @return
	 * @throws IOException
	 * @see java.io.DataInputStream#readDouble()
	 */
	@Override
	public final double readDouble() throws IOException {
		read += 8;
		return d.readDouble();
	}

	@Override
	public String readLine() throws IOException {
		return d.readLine();
	}

	/**
	 * @return
	 * @throws IOException
	 * @see java.io.DataInputStream#readUTF()
	 */
	@Override
	public final String readUTF() throws IOException {
		String readUTF = d.readUTF();
		int size = readUTF.length() * 4 + 4;
		read += size;
		if ("null".equals(readUTF)) {
			System.err.println("READING OF " + readUTF + " HAS " + size + "; bytes");
			throw new NullPointerException();
		}
		return readUTF;
	}

	/**
	 * @return the read
	 */
	public long getReadSize() {
		return read;
	}

}
