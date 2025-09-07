package org.schema.game.common.util;

public class FileStreamSegment {

	public final byte[] buffer;
	public short length = 0;
	public boolean last = false;

	public FileStreamSegment(int size) {
		super();
		buffer = new byte[size];
	}
}
