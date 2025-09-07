package org.schema.game.common.controller.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.schema.schine.resource.FileExt;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class IOFileManager {

	public static final ReentrantReadWriteLock rwlServer = new ReentrantReadWriteLock();
	public static final ReentrantReadWriteLock rwlClient = new ReentrantReadWriteLock();
	private static final int MAX_OPEN_FILES = 500;
	private static final Object2ObjectOpenHashMap<String, SegmentRegionFileHandle> staticFileMapServer = new Object2ObjectOpenHashMap<String, SegmentRegionFileHandle>();
	private static final Object2ObjectOpenHashMap<String, SegmentRegionFileHandle> staticFileMapClient = new Object2ObjectOpenHashMap<String, SegmentRegionFileHandle>();
	private static final Object removeLock = new Object();
	private final Object2ObjectOpenHashMap<String, SegmentRegionFileHandle> fileMap = new Object2ObjectOpenHashMap<String, SegmentRegionFileHandle>();
	private final boolean onServer;
	private final String UID;
	private Object2BooleanOpenHashMap<String> existMap = new Object2BooleanOpenHashMap<String>();

	public IOFileManager(String UID, boolean onServer) {
		this.UID = UID;
		this.onServer = onServer;
	}

	/**
	 * Removes the file handle that was accessed
	 * the most time ago
	 *
	 * @param ioFileManager
	 * @throws IOException
	 */
	private static void removeOldestHandle(IOFileManager ioFileManager, String forRequested) throws IOException {

		//write lock is already engaged!

		String fileName = null;
		long currentTimeMillis = System.currentTimeMillis();
		SegmentRegionFileHandle oldestHandle = null;

		long oldest = -1;

		for (SegmentRegionFileHandle f : ioFileManager.getStaticFileMap().values()) {
			if (oldest < 0 || f.getLastAccessTime() < oldest) {
				oldestHandle = f;
				oldest = oldestHandle.getLastAccessTime();
			}
		}
		oldestHandle.rwl.writeLock().lock();

		fileName = oldestHandle.getName();
		assert (oldestHandle != null);

		if (oldestHandle.needsHeaderWriting) {
			oldestHandle.writeHeader();
			oldestHandle.needsHeaderWriting = false;
		}
		oldestHandle.flush(false);
		oldestHandle.close();

		ioFileManager.getStaticFileMap().remove(oldestHandle.getName());
		oldestHandle.getIoFileManager().fileMap.remove(oldestHandle.getName());

		oldestHandle.rwl.writeLock().unlock();
		//		System.err.println("[IOFileManager] removing oldest handle DONE took "+(System.currentTimeMillis() - currentTimeMillis)+" ms ("+fileName+") (initiated for request of: "+forRequested+")");
	}

	/**
	 * Removes the file handle that was accessed
	 * the most time ago
	 *
	 * @param ioFileManager
	 * @throws IOException
	 */
	public static void writeAllHeaders(IOFileManager ioFileManager) throws IOException {
		try {
			ioFileManager.getStaticFileLock().readLock().lock();
			//		System.err.println("[IOFileManager] removing oldest handle start");
			String fileName = null;
			long currentTimeMillis = System.currentTimeMillis();
			for (SegmentRegionFileHandle f : ioFileManager.getStaticFileMap().values()) {
				if (f.needsHeaderWriting) {
					f.writeHeader();
					f.needsHeaderWriting = false;
				}
			}
		} finally {
			ioFileManager.getStaticFileLock().readLock().unlock();
		}
	}

	public static void writeAllOpenFiles(IOFileManager ioFileManager) {
		try {
			ioFileManager.getStaticFileLock().readLock().lock();

			for (SegmentRegionFileHandle f : ioFileManager.getStaticFileMap().values()) {
				f.rwl.writeLock().lock();
				try {
					f.flush(false);
				} catch (IOException e) {
					e.printStackTrace();
				}
				f.rwl.writeLock().unlock();
			}
		} finally {
			ioFileManager.getStaticFileLock().readLock().unlock();
		}
	}

	public void closeAll() throws IOException {
		try {
			getStaticFileLock().writeLock().lock();
			for (Entry<String, SegmentRegionFileHandle> f : fileMap.entrySet()) {
				f.getValue().rwl.writeLock().lock();
				getStaticFileMap().remove(f.getKey());
				f.getValue().close();
				f.getValue().rwl.writeLock().unlock();
			}
			fileMap.clear();
		} finally {
			getStaticFileLock().writeLock().unlock();
		}
	}
	public boolean checkEmptyExists(String filename){
		try {
			getStaticFileLock().readLock().lock();
			if(!existMap.containsKey(filename)){
				existMap.put(filename, (new FileExt(filename)).exists());
			}
			return existMap.getBoolean(filename);
		} finally {
			getStaticFileLock().readLock().unlock();
		}
	}
	public void registerExists(String name) {
		try {
			getStaticFileLock().readLock().lock();
			existMap.put(name, true);
		} finally {
			getStaticFileLock().readLock().unlock();
		}		
	}
	public boolean existsFile(String fileName) {
		try {
			getStaticFileLock().readLock().lock();
			return fileMap.containsKey(fileName);
		} finally {
			getStaticFileLock().readLock().unlock();
		}
	}

	public SegmentRegionFileHandle getFileNormal(String fileName) throws IOException {
		SegmentRegionFileHandle segmentRegionFileHandle;
		try {
			getStaticFileLock().readLock().lock();
			segmentRegionFileHandle = fileMap.get(fileName);
		} finally {
			getStaticFileLock().readLock().unlock();
		}
		if (segmentRegionFileHandle == null) {
			try {
				getStaticFileLock().writeLock().lock();
				// file is not in cache: check if we need to remove oldest to keep limit
				if (getStaticFileMap().size() >= MAX_OPEN_FILES) {
					removeOldestHandle(this, fileName);
				}

				File file = new FileExt(fileName);
				//				System.err.println("0 EXISTS FILE "+fileName+"; "+file.exists());
				segmentRegionFileHandle = fileName.endsWith(SegmentDataIO16.BLOCK_FILE_EXT) ? 
						new SegmentRegionFileOld(UID, onServer, file, fileName, this) : 
							new SegmentRegionFileNew(UID, onServer, file, fileName, this);
				//				System.err.println("1 EXISTS FILE "+fileName+"; "+file.exists());
				if (!segmentRegionFileHandle.getFile().getChannel().isOpen()) {
					System.err.println("[IO] Exception: initial opening of " + fileName + " failed!");
				}
				//				System.err.println("2 EXISTS FILE "+fileName+"; "+file.exists());
				fileMap.put(segmentRegionFileHandle.getName(), segmentRegionFileHandle);
				getStaticFileMap().put(segmentRegionFileHandle.getName(), segmentRegionFileHandle);
			} finally {
				getStaticFileLock().writeLock().unlock();
			}
		}

		if (!segmentRegionFileHandle.getFile().getChannel().isOpen()) {
			try {
				throw new FileNotFoundException("Having to reopen closed file " + fileName);
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.err.println("[IFFileManager] WARNING: the file " + segmentRegionFileHandle.getName() + " was closed for some reason. Reopening!");
			segmentRegionFileHandle.reopen();
		}
		segmentRegionFileHandle.updateLastAccess();
		
		return segmentRegionFileHandle;

	}
	public static SegmentRegionFileHandle getFromFile(File file) throws IOException{

		String fileName = file.getName();
		SegmentRegionFileHandle segmentRegionFileHandle = 
				fileName.endsWith(SegmentDataIO16.BLOCK_FILE_EXT) ? 
				new SegmentRegionFileOld(fileName, true, file, fileName, null) : 
					new SegmentRegionFileNew(fileName, true, file, fileName, null);
		if (!segmentRegionFileHandle.getFile().getChannel().isOpen()) {
			throw new IOException("[IO] Exception: initial opening of " + fileName + " failed!");
		}
		segmentRegionFileHandle.updateLastAccess();
		
		return segmentRegionFileHandle;
	}
	public SegmentRegionFileHandle getFileAndLockIt(String fileName, boolean read) throws IOException {
		SegmentRegionFileHandle segmentRegionFileHandle;
		try {
			getStaticFileLock().readLock().lock();
			segmentRegionFileHandle = fileMap.get(fileName);
			
			if (segmentRegionFileHandle != null) {
				if (!segmentRegionFileHandle.getFile().getChannel().isOpen()) {
					try {
						throw new FileNotFoundException("Having to reopen closed file " + fileName);
					} catch (Exception e) {
						e.printStackTrace();
					}
					System.err.println("[IFFileManager] WARNING: the file " + segmentRegionFileHandle.getName() + " was closed for some reason. Reopening!");
					segmentRegionFileHandle.reopen();
				}
				segmentRegionFileHandle.updateLastAccess();
				if (read) {
					segmentRegionFileHandle.rwl.readLock().lock();
				} else {
					segmentRegionFileHandle.rwl.writeLock().lock();
				}
				return segmentRegionFileHandle;
			}
		} finally {
			getStaticFileLock().readLock().unlock();
		}
		if (segmentRegionFileHandle == null) {
//			System.err.println("NEW ::: "+fileName);
			try {
				getStaticFileLock().writeLock().lock();
				// file is not in cache: check if we need to remove oldest to keep limit
				if (getStaticFileMap().size() >= MAX_OPEN_FILES) {
					removeOldestHandle(this, fileName);
				}

				File file = new FileExt(fileName);
				//				System.err.println("0 EXISTS FILE "+fileName+"; "+file.exists());
				segmentRegionFileHandle = fileName.endsWith(SegmentDataIO16.BLOCK_FILE_EXT) ? 
						new SegmentRegionFileOld(UID, onServer, file, fileName, this) : 
							new SegmentRegionFileNew(UID, onServer, file, fileName, this);
				//				System.err.println("1 EXISTS FILE "+fileName+"; "+file.exists());
				if (!segmentRegionFileHandle.getFile().getChannel().isOpen()) {
					System.err.println("[IO] Exception: initial opening of " + fileName + " failed!");
				}
				//				System.err.println("2 EXISTS FILE "+fileName+"; "+file.exists());
				fileMap.put(segmentRegionFileHandle.getName(), segmentRegionFileHandle);
				getStaticFileMap().put(segmentRegionFileHandle.getName(), segmentRegionFileHandle);

				if (!segmentRegionFileHandle.getFile().getChannel().isOpen()) {
					try {
						throw new FileNotFoundException("Having to reopen closed file " + fileName);
					} catch (Exception e) {
						e.printStackTrace();
					}
					System.err.println("[IFFileManager] WARNING: the file " + segmentRegionFileHandle.getName() + " was closed for some reason. Reopening!");
					segmentRegionFileHandle.reopen();
				}
				segmentRegionFileHandle.updateLastAccess();
				if (read) {
					segmentRegionFileHandle.rwl.readLock().lock();
				} else {
					segmentRegionFileHandle.rwl.writeLock().lock();
				}
			} finally {
				getStaticFileLock().writeLock().unlock();
			}
		}

		return segmentRegionFileHandle;

	}

	public final Object2ObjectOpenHashMap<String, SegmentRegionFileHandle> getStaticFileMap() {
		if (onServer) {
			return staticFileMapServer;
		} else {
			return staticFileMapClient;
		}
	}

	public final ReentrantReadWriteLock getStaticFileLock() {
		if (onServer) {
			return rwlServer;
		} else {
			return rwlClient;
		}
	}

	public void removeFileFromMap(String fileName) throws IOException {
		getStaticFileLock().writeLock().lock();
		try {
			SegmentRegionFileHandle segmentRegionFileHandle = fileMap.get(fileName);

			if (segmentRegionFileHandle != null) {
				fileMap.remove(segmentRegionFileHandle.getName());
				getStaticFileMap().remove(segmentRegionFileHandle.getName());
			}
		} finally {
			getStaticFileLock().writeLock().unlock();
		}
	}

	public void removeFile(String fileName) throws IOException {
		getStaticFileLock().writeLock().lock();
		try {
			SegmentRegionFileHandle segmentRegionFileHandle = fileMap.get(fileName);

			if (segmentRegionFileHandle != null) {
				segmentRegionFileHandle.delete();
				fileMap.remove(segmentRegionFileHandle.getName());
				getStaticFileMap().remove(segmentRegionFileHandle.getName());

			}
		} finally {
			getStaticFileLock().writeLock().unlock();
		}
	}
	public static void cleanUp(boolean server){
		if(server){
			rwlServer.writeLock().lock();
			for(SegmentRegionFileHandle a : staticFileMapServer.values()){
				try {
					a.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			staticFileMapServer.clear();
			rwlServer.writeLock().unlock();
		}else{
			rwlClient.writeLock().lock();
			for(SegmentRegionFileHandle a : staticFileMapClient.values()){
				try {
					a.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			staticFileMapClient.clear();
			rwlClient.writeLock().unlock();
		}
	}

	
}
