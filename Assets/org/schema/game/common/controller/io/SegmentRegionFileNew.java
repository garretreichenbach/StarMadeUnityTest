package org.schema.game.common.controller.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.schema.game.common.data.world.SegmentData;


public class SegmentRegionFileNew extends SegmentRegionFileHandle{

	private final SegmentHeader header;

	public SegmentRegionFileNew(String UID, boolean onServer, File f, String name, IOFileManager ioFileManager) throws IOException {
		super(SegmentData.TOTAL_SIZE, UID, onServer, f, name, ioFileManager);
		header = new SegmentHeader();

		
		createHeader();

	}




	public int getVersion() {
		return header.version;
	}


	
	@Override
	protected void createHeader() throws IOException {
		assert(header != null);
		long t = System.currentTimeMillis();
		boolean existed = f.exists();
		long length = 0;
		//no need to force. force is done when removing oldest handle
		if (existed) {
			this.file = new RandomAccessFile(f, "rw");
			length = f.length();
		}

		if (length >= SegmentHeader.HEADER_SIZE) {
			header.read(file);
		} else {
			if (existed) {
				try {
					throw new NoHeaderException("No Header in file " + getDesc() + " -> " + f.getName() + "; read: " + length + "; is now: " + file.length() + " / " + f.length() + " (must be min: " + SegmentHeader.HEADER_SIZE+ ")" + "; REWRITING HEADER; fExisted: " + f.exists());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			SegmentHeader.writeEmptyHeader(f);

			if (!existed) {
				this.file = new RandomAccessFile(f, "rw");
			}
			header.resetToEmpty();
			
		}
	}

	@Override
	public void writeHeader() throws IOException {
		System.err.println("[IO] Writing Header of " + getName());
		header.writeTo(file);
	}
	public void writeSegmentWithoutMap(long position, SegmentOutputStream so) throws IOException {
		so.writeToFileWithoutMap(file, position, getName(), f);
	}

	public SegmentHeader getHeader() {
		return header;
	}
	

}
