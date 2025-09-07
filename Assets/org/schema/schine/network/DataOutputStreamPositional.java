package org.schema.schine.network;

import java.io.DataOutputStream;
import java.io.IOException;

import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;

public class DataOutputStreamPositional extends DataOutputStream {

	private FastByteArrayOutputStream c;

	public DataOutputStreamPositional(FastByteArrayOutputStream out) {
		super(out);
		this.c = out;
	}

	public byte[] getArray() {
		return c.array;
	}

	/**
	 * @return
	 * @throws IOException
	 * @see it.unimi.dsi.fastutil.io.FastBufferedOutputStream#position()
	 */
	public long position() throws IOException {
		return ((FastByteArrayOutputStream) out).position();
	}

	/**
	 * @param newPosition
	 * @throws IOException
	 * @see it.unimi.dsi.fastutil.io.FastBufferedOutputStream#position(long)
	 */
	public void position(long newPosition) throws IOException {
		((FastByteArrayOutputStream) out).position(newPosition);
	}

}
