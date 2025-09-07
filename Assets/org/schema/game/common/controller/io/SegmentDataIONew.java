package org.schema.game.common.controller.io;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.SerializationException;
import org.schema.common.ByteBufferInputStream;
import org.schema.game.client.controller.GameClientController;
import org.schema.game.client.data.ClientStatics;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.TransientSegmentController;
import org.schema.game.common.data.world.DeserializationException;
import org.schema.game.common.data.world.RemoteSegment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentData.SegmentDataType;
import org.schema.game.common.data.world.Universe;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.FileExt;

import com.googlecode.javaewah.EWAHCompressedBitmap;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;



public class SegmentDataIONew implements SegmentDataIOInterface{
	public static final int DIM = 16;
	public static final int DIMxDIM = DIM*DIM;
	public static final int DIMxDIMxDIM = DIM*DIM*DIM;
	public static final byte VERSION = SegmentData.VERSION;
	
	private final Long2ObjectOpenHashMap<String> fileNameCacheRead = new Long2ObjectOpenHashMap<String>();
	private final Long2ObjectOpenHashMap<String> fileNameCacheWrite = new Long2ObjectOpenHashMap<String>();
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final IOFileManager manager;
	private final UniqueIdentifierInterface segmentController;
	private final boolean onServer;
	private final String segmentDataPath;
	
	public SegmentDataIONew(UniqueIdentifierInterface segmentController, boolean onServer) {
		this.segmentController = segmentController;
		this.onServer = onServer;
		this.manager = new IOFileManager(segmentController.getUniqueIdentifier(), segmentController.isOnServer());
		if (onServer) {
			this.segmentDataPath = GameServerState.SEGMENT_DATA_DATABASE_PATH;
		} else {
			this.segmentDataPath = ClientStatics.SEGMENT_DATA_DATABASE_PATH;
		}
	}
	@Override
	public void releaseFileHandles() throws IOException {
		long t = System.currentTimeMillis();
		manager.closeAll();
		long took = System.currentTimeMillis() - t;
		if (took > 10) {
			System.err.println("[SEGMENT-IO] WARNING: File Handle Release for " + segmentController + " on server(" + segmentController.isOnServer() + ") took " + took + " ms");
		}
	}
	public static int requestStatic(int x, int y, int z, String UID, ByteBuffer dataByteBuffer, String path, RemoteSegment seg)
			throws IOException, DeserializationException {
			String s = SegmentDataFileUtils.getSegFile(x, y, z, 
					UID, 
					null, null, path);
			
			File f = new FileExt(s);
			
			//checking the manager first when handle exists saves time (no need to check in the filesystem file.exists())
			if ( !f.exists()) {
				System.err.println("NO FILE: "+f.getAbsolutePath());
				return SegmentDataFileUtils.READ_NO_DATA;
			} else {
				SegmentRegionFileNew rf = null;
				try{
					rf = (SegmentRegionFileNew) IOFileManager.getFromFile(f);
					BufferedInputStream byteArrayInputStream = null;
					DataInputStream in = null;
						
						
					assert(rf != null);
					
					if (rf.getHeader().isEmpty(x, y, z)) {
						seg.timestampTmp = -1;
						return SegmentDataFileUtils.READ_EMPTY;
					} if(rf.getHeader().isNoData(x, y, z)){
						return SegmentDataFileUtils.READ_NO_DATA;
					}else {
						
						int length = rf.getHeader().getSize(x, y, z) ;
						
						short offset = SegmentHeader.convertToDataOffset(rf.getHeader().getOffset(x, y, z));
						
						long dataPosition = SegmentHeader.getAbsoluteFilePos(offset);
						
						
						
						dataByteBuffer.rewind();
						dataByteBuffer.limit(length);
						
						rf.getFile().getChannel().read(dataByteBuffer, dataPosition);
						dataByteBuffer.rewind();
						
						byteArrayInputStream = new BufferedInputStream(new ByteBufferInputStream(
								dataByteBuffer));
						in = new DataInputStream(
								byteArrayInputStream);
						
						System.err.println("::: READING: "+x+", "+y+", "+z+"; off: "+offset+" -> "+dataPosition+"; size "+length+"; "+s);
						seg.pos.set(x, y, z);
//						System.err.println(s+" ::: READING: "+x+", "+y+", "+z+"; off: "+offset+" -> "+dataPosition+"; size "+length);
						seg.deserialize(in, length, false, false, 0);
						// do not reset only when the segmentdata is the debug data
						boolean needsResaveOnNextSave = false;
						
						if (needsResaveOnNextSave) {
							seg.timestampTmp = System.currentTimeMillis();
						} else {
							seg.timestampTmp = seg.lastChangedDeserialized;
						}
						
						
						return SegmentDataFileUtils.READ_DATA;
					}
				}finally{
					rf.close();
				}
		}
	}
	@Override
	public int request(int x, int y, int z, RemoteSegment seg)
			throws IOException, DeserializationException {
		rwl.readLock().lock();
		try {
			String s = SegmentDataFileUtils.getSegFile(x, y, z, 
					segmentController.getReadUniqueIdentifier(), 
					segmentController.getObfuscationString(), fileNameCacheRead, getSegmentDataPath(true));

			File f = new FileExt(s);

			//checking the manager first when handle exists saves time (no need to check in the filesystem file.exists())
			if (!manager.existsFile(s) && !f.exists()) {
				return SegmentDataFileUtils.READ_NO_DATA;
			} else {
				SegmentRegionFileNew rf = null;
				rf = (SegmentRegionFileNew) manager.getFileAndLockIt(s, true);
				ByteBuffer dataByteBuffer = null;
				ByteBufferInputStream byteArrayInputStream = null;
				DataInputStream in = null;
				try {
					
					
					assert(rf != null);

					if (rf.getHeader().isEmpty(x, y, z)) {
						seg.timestampTmp = -1;
						return SegmentDataFileUtils.READ_EMPTY;
					}else if(rf.getHeader().isNoData(x, y, z)){
						return SegmentDataFileUtils.READ_NO_DATA;
					}else {

						int length = rf.getHeader().getSize(x, y, z) ;
						
						short offset = SegmentHeader.convertToDataOffset(rf.getHeader().getOffset(x, y, z));

						long dataPosition = SegmentHeader.getAbsoluteFilePos(offset);
						
						dataByteBuffer = segmentController.getDataByteBuffer();
						dataByteBuffer.rewind();
						dataByteBuffer.limit(length);

						rf.getFile().getChannel().read(dataByteBuffer, dataPosition);
						dataByteBuffer.rewind();

						byteArrayInputStream = new ByteBufferInputStream(dataByteBuffer);
						in = new DataInputStream(byteArrayInputStream);

						//time stamp is the first 8 bytes
						
						// do not reset only when the segmentdata is the debug data
						boolean needsResaveOnNextSave = false;
						try {
//							System.err.println(s+" ::: READING: "+x+", "+y+", "+z+"; off: "+offset+" -> "+dataPosition+"; size "+length);
							needsResaveOnNextSave = seg.deserialize(in, length, false, true, segmentController.getUpdateTime());
						} catch (IOException e) {
							System.err.println("Exception: IOException " + e + " happened on " + segmentController + " -> file: " + s);
							throw e;
						}

						if (needsResaveOnNextSave) {
							seg.timestampTmp = System.currentTimeMillis();
						} else {
							
							seg.timestampTmp = seg.lastChangedDeserialized;
						}

						return SegmentDataFileUtils.READ_DATA;
					}
				} finally {
					if (dataByteBuffer != null) {
						segmentController.releaseDataByteBuffer(dataByteBuffer);
					}
					if (byteArrayInputStream != null) {
						byteArrayInputStream.close();
					}
					if (in != null) {
						in.close();
					}
					assert(rf != null);
					assert(rf.rwl != null);
					assert(rf.rwl.readLock() != null);
					rf.rwl.readLock().unlock();
				}
			}
		} finally {
			rwl.readLock().unlock();
		}
	}
	
	@Override
	public IOFileManager getManager() {
		return manager;
	}
	public static SegmentRegionFileNew write(RemoteSegment s, final long t, File f, SegmentRegionFileNew segmentRegionFileNew) throws IOException {
		
		
		SegmentRegionFileNew rf = null;
		if(segmentRegionFileNew != null){
			rf = segmentRegionFileNew;
		}else{
			rf = (SegmentRegionFileNew) IOFileManager.getFromFile(f);
		}
		if (s.isEmpty()) {
			// write timestamp only without data
			onEmptySegment(s.pos.x, s.pos.y, s.pos.z, t, rf);
			// unlock done in finally block
			return null;
		}


		//timestamp will be -1 for empty or no data
		//so a switch from empty->non-empty will always indicate a write

		SegmentOutputStream so = rf.segmentOutputStream;
		DataOutputStream dro = rf.dro;

		
		
		short offset = rf.getHeader().getOffset(s.pos.x, s.pos.y, s.pos.z);
		short dataOffset = 0;
		
		
		if (offset == SegmentHeader.NO_DATA) {
			dataOffset = rf.getHeader().createNewDataOffsetOnFileEnd(rf);
		} else {
			dataOffset = SegmentHeader.convertToDataOffset(offset);
		}
		
		long absFilePos = SegmentHeader.getAbsoluteFilePos(dataOffset);

		int length = s.serialize(dro, true, System.currentTimeMillis(), VERSION);
		
		rf.writeSegment(absFilePos, so);
		
		assert (length < SegmentHeader.SEGMENT_SECTOR) : length + "/" + SegmentHeader.SEGMENT_SECTOR;
		
		if (length >= SegmentHeader.SEGMENT_SECTOR) {
			throw new IOException("Critical error. segment size exceeded file-sector-size: " + length + "; " + SegmentHeader.HEADER_SIZE);
		}

		rf.getHeader().updateAndWrite(s.pos.x, s.pos.y, s.pos.z, dataOffset, length, rf);
		return rf;
	}
	@Override
	public boolean write(RemoteSegment s, long lastChanged, boolean writeHeader, boolean debug) throws IOException {
		if(!canWrite(s, debug)){
			return false;
		}
		try {
			debug = true;
			
			rwl.writeLock().lock();

			String fileName = SegmentDataFileUtils.getSegFile(s.pos.x, s.pos.y, s.pos.z, segmentController.getWriteUniqueIdentifier(), 
					segmentController.getObfuscationString(), fileNameCacheWrite, getSegmentDataPath(false));
			File f = new FileExt(fileName);
			
			if(debug){
				System.err.println("[SEGMENTIO] "+segmentController+" WRITING SEGMENT "+s.pos+" TO "+fileName+"; UID: "+segmentController.getWriteUniqueIdentifier());
			}
			SegmentRegionFileNew rf = null;
			final SegmentData segmentData = s.getSegmentData();
			try {

				rf = (SegmentRegionFileNew) manager.getFileAndLockIt(fileName, false);
				

				
				if(segmentData != null){
					segmentData.rwl.readLock().lock();;
				}
				
//				if(segmentData != null && (segmentData.isRevalidating() || segmentData.needsRevalidate())){
//					if(s.getSegmentController() != null){
//						System.err.println("[IO] "+s.getSegmentController().getState()+" "+s.getSegmentController()+" NOT WRITING SEGMENT WHILE REVALIDATING; is: "+segmentData.isRevalidating()+"; needs: "+segmentData.needsRevalidate()+"; pos: "+s.pos);
//					}
//					return false;
//				}
				
				//Segment not empty!
				
				long t = System.currentTimeMillis();

				// check if s was changed at all (it has to be at least once at creation)
				if (lastChanged <= 0) {
					s.setLastChanged(t);
				}
				
				
				//we can seek here, because file is write locked
				long dbTimeStamp = rf.getHeader().getTimeStamp(s.pos.x, s.pos.y, s.pos.z, rf);
				//timestamp will be -1 for empty or no data
				//so a switch from empty->non-empty will always indicate a write
				if (dbTimeStamp <= t && dbTimeStamp >= lastChanged) {
					
					
					if(debug) {
						System.err.println("[IO] OLD: TS: "+dbTimeStamp+"; "+lastChanged+"; "+System.currentTimeMillis());
					}
					return false;
				}else if(dbTimeStamp > t){
					System.err.println("["+(segmentController.isOnServer() ? "[SERVER]" : "[CLIENT]")+"[IO] WARNING: segment last changed in the future: "+segmentController+": "+s.pos+"; DB TS: "+(new Date(dbTimeStamp))+"; LAST CHANGED: "+(new Date(lastChanged)));
				}
				if (s.isEmpty()) {
					if(ServerConfig.DEBUG_EMPTY_CHUNK_WRITES.isOn()){
						System.err.println("[SERVER][DEBUG] WRITE EMPTY: "+s.getSegmentController()+": "+s.pos+"; "+lastChanged);
					}
					if(debug) {
						System.err.println("[IO] EMPTY");
					}
					// write timestamp only without data
					onEmptySegment(s.pos.x, s.pos.y, s.pos.z, lastChanged, rf);
					// unlock done in finally block
					return false;
				}
				SegmentOutputStream so = rf.segmentOutputStream;
				DataOutputStream dro = rf.dro;

				
				
				short offset = rf.getHeader().getOffset(s.pos.x, s.pos.y, s.pos.z);
				short dataOffset = 0;
				
				
				if (offset == SegmentHeader.NO_DATA) {
					dataOffset = rf.getHeader().createNewDataOffsetOnFileEnd(rf);
				} else {
					dataOffset = SegmentHeader.convertToDataOffset(offset);
				}
				

				int length = s.serialize(dro, true, lastChanged, VERSION);
				
				
				long absFilePos = SegmentHeader.getAbsoluteFilePos(dataOffset);
				
				rf.writeSegment(absFilePos, so);
				
				assert (length < SegmentHeader.SEGMENT_SECTOR) : length + "/" + SegmentHeader.SEGMENT_SECTOR;
				
				if (length >= SegmentHeader.SEGMENT_SECTOR) {
					throw new IOException("Critical error. segment size exceeded file-sector-size: " + length + "; " + SegmentHeader.HEADER_SIZE);
				}
				long timeForserialize = System.currentTimeMillis() - t;
				t = System.currentTimeMillis();

				rf.getHeader().updateAndWrite(s.pos.x, s.pos.y, s.pos.z, dataOffset, length, rf);
				

				if (segmentController.isOnServer() && ServerConfig.FORCE_DISK_WRITE_COMPLETION.isOn()) {
					rf.dro.flush();
				}

				if (segmentController.isOnServer() && ServerConfig.DEBUG_SEGMENT_WRITING.isOn()) {
					rf.dro.flush();
					RemoteSegment seg = new RemoteSegment((SegmentController) segmentController);
					//					debugSegmentData.assignData(seg);
					seg.pos.set(s.pos);
					try {
						request(seg.pos.x, seg.pos.y, seg.pos.z, seg);
//						boolean equals = Arrays.equals(s.getSegmentData().getAsIntBuffer(), seg.getSegmentData().getAsIntBuffer());
//						if (!equals) {
//							try {
//								throw new IOException("Exception !!!!!!!!!! ERROR in segment data writing " + seg.pos + " on " + segmentController);
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
//						} else {
//							System.err.println("SEGMENT READ IS OK!");
//						}

					} catch (DeserializationException e) {
						e.printStackTrace();
					}

				}
			} finally {
				if(segmentData != null){
					segmentData.rwl.readLock().unlock();
				}
				rf.rwl.writeLock().unlock();
			}
		} catch (IOException e) {
			System.err.println("Exception HAPPENED ON SEGMENT " + s + "; controller: " + s.getSegmentController() + "; size: " + s.getSize() + "; data " + s.getSegmentData());
			e.printStackTrace();
			if (segmentController.isOnServer()) {
				((GameServerState) ((SegmentController)segmentController).getState())
						.getController().broadcastMessage(Lng.astr("ERROR while saving data!\nPlease tell an admin to\nrestart the server and to\nsend in a report\nDATA MAY BE LOST IF CONTINUED"), ServerMessage.MESSAGE_TYPE_ERROR);
			}
			throw e;
		} finally {
			
			rwl.writeLock().unlock();
		}

		return true;
	}
	public final String getSegmentDataPath(boolean read) {
		if(read && onServer && segmentController.isLoadByBlueprint()){
			return segmentController.getBlueprintSegmentDataPath();
		}
		return segmentDataPath;
	}

	private static void onEmptySegment(int x, int y, int z, long lastChanged, SegmentRegionFileNew rf) throws IOException {
		if(!rf.getHeader().isEmpty(x, y, z)){
			//write as empty, if segment was previously not empty or had no data before
			RandomFileOutputStream ro = new RandomFileOutputStream(rf.getFile(), false);
			rf.getHeader().writeEmptyDirectly(x, y, z, ro);
		}
	}
	@Override
	public int getSize(int x, int y, int z) throws IOException {
		//		synchronized(this)ads{
		String s = SegmentDataFileUtils.getSegFile(x, y, z, 
				segmentController.getReadUniqueIdentifier(), 
				segmentController.getObfuscationString(), fileNameCacheRead, getSegmentDataPath(true));
		//			t = System.nanoTime();
		if (!GameClientController.exists(s)) {
			return -1;
		} else {
		}
		SegmentRegionFileNew rf = null;
		try {

			//aquire lock synchronized
			//so file is not closed by another thread
			rf = (SegmentRegionFileNew) manager.getFileAndLockIt(s, true);


			return rf.getHeader().getSize(x, y, z);
		} catch (Exception e) {
			e.printStackTrace();
			manager.removeFile(s);
			return -1;
		} finally {
			if (rf != null) {
				rf.rwl.readLock().unlock();
			}
		}

	}
	@Override
	public long getTimeStamp(int x, int y, int z) throws IOException {
		String s = SegmentDataFileUtils.getSegFile(x, y, z, 
				segmentController.getReadUniqueIdentifier(), 
				segmentController.getObfuscationString(), fileNameCacheRead, getSegmentDataPath(true));
		//			t = System.nanoTime();
		if (!GameClientController.exists(s)) {
			return -1;
		} else {
		}
		SegmentRegionFileNew rf = null;
		try {

			//aquire lock synchronized
			//so file is not closed by another thread
			rf = (SegmentRegionFileNew) manager.getFileAndLockIt(s, true);
			return rf.getHeader().getTimeStamp(x, y, z, rf);
		} catch (Exception e) {
			e.printStackTrace();
			manager.removeFile(s);
			return -1;
		} finally {
			if (rf != null) {
				rf.rwl.readLock().unlock();
			}
		}

	}
	public boolean isOnServer() {
		return onServer;
	}
	
	public boolean canWrite(RemoteSegment s, boolean debug){
		if (s.getSegmentController().isOnServer() && s.getSegmentController() != null && !(s.getSegmentController() instanceof TransientSegmentController)) {
			Universe universe = ((GameServerState) s.getSegmentController().getState()).getUniverse();
			synchronized (universe.writeMap) {
				try {

					long thiswrite = System.currentTimeMillis();
					long lastWrite = s.getSegmentController().getLastWrite();
					assert (s.getSegmentController().getWriteUniqueIdentifier() != null);
					long lastWriteUID = universe.writeMap.getLong(s.getSegmentController().getWriteUniqueIdentifier());

					if (lastWriteUID > 0) {
						if (lastWriteUID != lastWrite) {
							throw new SerializationException(s.getSegmentController() + "sec[" + s.getSegmentController().getSectorId() + "]; Tried to write old version over new (rollbackbug) entity-UID LAST WRITTEN " + lastWriteUID + "; entity-instance LAST WRITTEN " + lastWrite + ";  UID: " + s.getSegmentController().getUniqueIdentifier());
						}
					}

					universe.writeMap.put(s.getSegmentController().getWriteUniqueIdentifier(), thiswrite);
					s.getSegmentController().setLastWrite(thiswrite);
					assert (s.getSegmentController().getLastWrite() == thiswrite);
					//					System.err.println("LAST SAVED: "+s.getSegmentController()+": "+thiswrite+"; "+s.getSegmentController().getUniqueIdentifier());
				} catch (SerializationException e) {
					e.printStackTrace();
					((GameServerState) s.getSegmentController().getState()).getController().broadcastMessageAdmin(
							Lng.astr("SerialException on Server! \nPlease send in report from server\ncaused by: %s\n%s",  s.getSegmentController(), ((GameServerState) s.getSegmentController().getState()).getUniverse().getSector(s.getSegmentController().getSectorId())), ServerMessage.MESSAGE_TYPE_ERROR);
					return false;
				}
			}
		}
		if (s.getSegmentController() instanceof TransientSegmentController && !((TransientSegmentController) s.getSegmentController()).isTouched()) {
			//			System.err.println("[SEGMENTIO] not writing transient object "+s.getSegmentController());
			return false;
		}
		if (s.getSegmentController().isOnServer() && (s.isDeserializing() || s.isRevalidating())) {
			//do not cache unvalidated data on clients
			return false;
		}
		if(segmentController.isLoadByBlueprint()){
			if(debug){
				System.err.println("[SEGMENTIO] "+segmentController+" NOT WRITING AS IT'S LOADED BY BLUEPRINT UID: "+segmentController.getWriteUniqueIdentifier());
			}
			return false;
		}
		return true;
	}
	
	
	@Override
	public EWAHCompressedBitmap requestSignature(int x, int y, int z)
			throws IOException, DeserializationException {
		rwl.readLock().lock();
		try {
			
			String s = SegmentDataFileUtils.getSegFile(x, y, z, 
					segmentController.getReadUniqueIdentifier(), 
					segmentController.getObfuscationString(), fileNameCacheRead, getSegmentDataPath(true));

			File f = new FileExt(s);

			//checking the manager first when handle exists saves time (no need to check in the filesystem file.exists())
			if (!manager.existsFile(s) && !f.exists()) {
				return null;
			} else {
				EWAHCompressedBitmap bm = new EWAHCompressedBitmap((DIMxDIMxDIM) / 64);
				SegmentRegionFileNew rf = null;
				try {
					rf = (SegmentRegionFileNew) manager.getFileAndLockIt(s, true);
					for (int i = 0; i < DIMxDIMxDIM; i++) {
						if(rf.getHeader().isEmptyOrNoData(i)){
							bm.set(i);
						}
						i++;
					}
				} finally {
					rf.rwl.readLock().unlock();
	
				}
				return bm;
			}
			
			
			
		} finally {
			rwl.readLock().unlock();
		}
	}
	
	@Override
	public void writeEmpty(int x, int y, int z, SegmentController c, long lastChanged, boolean writeHeader) throws IOException {
		if (c instanceof TransientSegmentController && !((TransientSegmentController) c).isTouched()) {
			return;
		}
		if(c.isLoadByBlueprint()){
			return;
		}
		try {

			rwl.writeLock().lock();

			
			
			String fileName = SegmentDataFileUtils.getSegFile(x, y, z, 
					segmentController.getWriteUniqueIdentifier(),
					segmentController.getObfuscationString(), fileNameCacheWrite, getSegmentDataPath(false));

			SegmentRegionFileNew rf = null;
			if (!manager.existsFile(fileName) && !(new FileExt(fileName)).exists()) {
				return;
			} else {
				try {
				
					rf = (SegmentRegionFileNew) manager.getFileAndLockIt(fileName, false);
					// write timestamp only without data
					onEmptySegment(x, y, z, lastChanged, rf);
					// unlock done in finally block
					return;
	
				} finally {
					try {
						if (rf != null && rf.rwl != null && rf.rwl.writeLock() != null) {
							rf.rwl.writeLock().unlock();
						} else {
							System.err.println("Exception LOCK NULL: " + rf);
							if (rf != null) {
								System.err.println("Exception LOCK RWL NULL: " + rf.rwl);
	
								if (rf.rwl != null) {
									System.err.println("Exception LOCK RWL NULL: " + rf.rwl.writeLock());
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			System.err.println("Exception HAPPENED ON EMPTY SEGMENT " + x + ", " + y + ", " + z + "; controller: " + c + ";");
			e.printStackTrace();
			if (segmentController.isOnServer()) {
				((GameServerState) ((SegmentController)segmentController).getState())
						.getController().broadcastMessage(Lng.astr("ERROR while saving data!\nPlease tell an admin to\nrestart the server and to\nsend in a report\nDATA MAY BE LOST IF CONTINUED"), ServerMessage.MESSAGE_TYPE_ERROR);
			}
			throw e;
		} finally {
			rwl.writeLock().unlock();
		}
	}
	public static int checkVersionSkip(int version, SegmentDataType segmentDataType) {
		//skip version 5 (to fix optimized segments not being migrated)
		if(version == 5 && segmentDataType == SegmentDataType.FOUR_BYTE) {
			return 6;
		}
		return version;
	}
}
