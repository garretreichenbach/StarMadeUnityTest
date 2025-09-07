package org.schema.game.common.controller.io;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import org.schema.schine.resource.FileExt;

/**
 * A positionable file output stream.
 * <p/>
 * Threading Design : [x] Single Threaded  [ ] Threadsafe  [ ] Immutable  [ ] Isolated
 */

public class RandomFileOutputStream
		extends OutputStream {

	// *****************************************************************************
	// INSTANCE PROPERTIES
	// *****************************************************************************

	protected RandomAccessFile randomFile;                             // the random file to write to
	protected boolean sync;                                   // whether to synchronize every write

	// *****************************************************************************
	// INSTANCE CONSTRUCTION/INITIALIZATON/FINALIZATION, OPEN/CLOSE
	// *****************************************************************************

	public RandomFileOutputStream(File fil) throws IOException {
		this(fil, false);
	}

	public RandomFileOutputStream(RandomAccessFile fil, boolean syn) throws IOException {
		super();

		randomFile = fil;
		sync = syn;
	}

	public RandomFileOutputStream(File fil, boolean syn) throws IOException {
		super();

		File par;                                    // parent file

		fil = fil.getAbsoluteFile();
		if ((par = fil.getParentFile()) != null) {
			fil.mkdirs();
		}
		randomFile = new RandomAccessFile(fil, "rw");
		sync = syn;
	}

	public RandomFileOutputStream(IOFileManager manager, String fileName,
	                              boolean syn) throws IOException {
		//		randomFile = manager.getFile(fileName).getFile();
		//		randomFile=new RandomAccessFile(fil,"rw");
		sync = syn;
	}

	public RandomFileOutputStream(String fnm) throws IOException {
		this(fnm, false);
	}

	// *****************************************************************************
	// INSTANCE METHODS - OUTPUT STREAM IMPLEMENTATION
	// *****************************************************************************

	public RandomFileOutputStream(String fnm, boolean syn) throws IOException {
		this(new FileExt(fnm), syn);
	}

	public FileDescriptor getFD() throws IOException {
		return randomFile.getFD();
	}

	public long getFilePointer() throws IOException {
		return randomFile.getFilePointer();
	}

	public void setFilePointer(long pos) throws IOException {
		randomFile.seek(pos);
	}

	public long getFileSize() throws IOException {
		return randomFile.length();
	}

	public void setFileSize(long len) throws IOException {
		randomFile.setLength(len);
	}

	// *****************************************************************************
	// INSTANCE METHODS - RANDOM ACCESS EXTENSIONS
	// *****************************************************************************

	@Override
	public void write(int val) throws IOException {
		randomFile.write(val);
		if (sync) {
			randomFile.getFD().sync();
		}
	}

	@Override
	public void write(byte[] val) throws IOException {
		randomFile.write(val);
		if (sync) {
			randomFile.getFD().sync();
		}
	}

	@Override
	public void write(byte[] val, int off, int len) throws IOException {
		randomFile.write(val, off, len);
		if (sync) {
			randomFile.getFD().sync();
		}
	}

	@Override
	public void flush() throws IOException {
		if (sync) {
			randomFile.getFD().sync();
		}
	}

	@Override
	public void close() throws IOException {
		randomFile.close();
	}

} // END PUBLIC CLASS
