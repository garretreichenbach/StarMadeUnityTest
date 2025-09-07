package org.schema.game.common.controller.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;

public class SegmentOutputStream extends ByteArrayOutputStream {

	//	private ByteBuffer buffer;

	public SegmentOutputStream(int segmentBlockSize) {
		super(segmentBlockSize);
		//		buffer = MemoryUtil.memAlloc(SIZE);
	}

	public int count() {
		return count;
	}

	public void writeToFile(RandomAccessFile file, long offset, String name, File fileF)
			throws IOException {

		//		MappedByteBuffer map = file.getChannel().map(MapMode.READ_WRITE, offset, count);
		//		map.put(buf, 0, count);

		//fast writing method. however this does not guarate that it is physical on disk. The physical writing is done
		//after all segments are either cached or written to minimize IO interruption
		file.getChannel().transferFrom(Channels.newChannel(new FastByteArrayInputStream(buf, 0, count)), offset, count);

		//this reads
		//		file.getChannel().transferTo(offset, count, Channels.newChannel((new FastByteArrayOutputStream(buf))));
		this.reset();
	}

	public void writeToFileWithoutMap(RandomAccessFile file, long offset,
	                                  String name, File fileF) throws IOException {
		file.seek(offset);
		file.write(this.buf, 0, this.count);
		this.reset();
	}

}
