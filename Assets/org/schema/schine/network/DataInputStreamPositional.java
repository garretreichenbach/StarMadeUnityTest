package org.schema.schine.network;

import java.io.DataInputStream;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;

public class DataInputStreamPositional extends DataInputStream {

	public DataInputStreamPositional(FastByteArrayInputStream arg0) {
		super(arg0);
	}

	/**
	 * @return
	 * @see it.unimi.dsi.fastutil.io.FastByteArrayInputStream#position()
	 */
	public long position() {
		return ((FastByteArrayInputStream) in).position();
	}

	/**
	 * @param newPosition
	 * @see it.unimi.dsi.fastutil.io.FastByteArrayInputStream#position(long)
	 */
	public void position(long newPosition) {
		((FastByteArrayInputStream) in).position(newPosition);
	}

}
