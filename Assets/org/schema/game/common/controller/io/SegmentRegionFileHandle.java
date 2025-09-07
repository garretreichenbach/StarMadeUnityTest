package org.schema.game.common.controller.io;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.schema.schine.resource.FileExt;

public abstract class SegmentRegionFileHandle {
	public final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	protected File f;
	protected final String name;
	protected final String UID;
	protected final boolean onServer;
	public final DataOutputStream dro;
	public final SegmentOutputStream segmentOutputStream;
	public boolean needsHeaderWriting;
	private long lastAccess;
	private final IOFileManager ioFileManager;
	private boolean dirty;

	protected RandomAccessFile file;
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return ((SegmentRegionFileHandle) o).name.equals(name);
	}
	
	public SegmentRegionFileHandle(int size, String UID, boolean onServer, File f, String name, IOFileManager ioFileManager) throws IOException {

		this.UID = UID;
		this.onServer = onServer;
		this.name = name;
		this.f = f;
		lastAccess = System.currentTimeMillis();
		this.ioFileManager = ioFileManager;

		segmentOutputStream = new SegmentOutputStream(size);
		dro = new DataOutputStream(segmentOutputStream);

		if(ioFileManager != null){
			ioFileManager.registerExists(name);
		}

	}
	public RandomAccessFile getFile() {
		return file;
	}

	public String getDesc() {
		return onServer ? "SERVER[" : "CLIENT[" + UID + "]";
	}
	public void close() throws IOException {
		file.getChannel().close();
		file.close();
	}
	
	public void reopen() throws IOException {
		if (file != null) {
			try {
				file.getChannel().close();
				file.close();
				//				ro.close();
				//				buffRo.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		file = new RandomAccessFile(new FileExt(name), "rw");
	}
	public void updateLastAccess() {
		lastAccess = System.currentTimeMillis();
	}

	/**
	 * @return the ioFileManager
	 */
	public IOFileManager getIoFileManager() {
		return ioFileManager;
	}

	public long getLastAccessTime() {
		return lastAccess;
	}

	public String getName() {
		return name;
	}

	/**
	 * @return the f
	 */
	public File getNormalFile() {
		return f;
	}
	protected abstract void createHeader() throws IOException;

	public void delete() throws IOException {
		close();
		f.delete();
	}
	public abstract void writeHeader() throws IOException;

	public void writeSegment(long position, SegmentOutputStream so) throws IOException {
		so.writeToFile(file, position, name, f);
		this.dirty = true;
	}

	public void flush(boolean force) throws IOException {
		if (dirty || force) {
			file.getChannel().force(false);
			dirty = false;
		}
	}

}
