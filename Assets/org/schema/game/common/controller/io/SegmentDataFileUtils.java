package org.schema.game.common.controller.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.ClientStatics;
import org.schema.game.common.controller.SegmentBufferManager;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.resource.FileExt;
import org.schema.schine.resource.tag.Tag;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class SegmentDataFileUtils {
	private static final String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_-";
	public static final String BLOCK_FILE_EXT = ".smd3";
	public static final int READ_DATA = 0;
	public static final int READ_EMPTY = 1;
	public static final int READ_NO_DATA = 2;
	
	public static List<String> getAllFiles(Vector3i segConMin, Vector3i segConMax, String uid, UniqueIdentifierInterface uidM){
		
		List<String> allFiles = new ObjectArrayList<String>();
		Vector3i minPos = new Vector3i(segConMin);
		minPos.scale(16);
		minPos.sub(SegmentDataIONew.DIM*16, SegmentDataIONew.DIM*16, SegmentDataIONew.DIM*16);
		
		
		Vector3i maxPos = new Vector3i(segConMax);
		maxPos.scale(16);
		maxPos.add(SegmentDataIONew.DIM*16, SegmentDataIONew.DIM*16, SegmentDataIONew.DIM*16);
		
		String segmentDataPath;
		if (uidM == null || uidM.isOnServer()) {
			segmentDataPath = GameServerState.SEGMENT_DATA_DATABASE_PATH;
		} else {
			segmentDataPath = ClientStatics.SEGMENT_DATA_DATABASE_PATH;
		}
		
		for(int z = minPos.z; z <= maxPos.z; z+= SegmentDataIONew.DIM*8){
			for(int y = minPos.y; y <= maxPos.y; y+= SegmentDataIONew.DIM*8){
				for(int x = minPos.x; x <= maxPos.x; x+= SegmentDataIONew.DIM*8){
					
					String segFile = getSegFile(x, y, z, uid, uidM != null ? uidM.getObfuscationString() : null, null, segmentDataPath);
					allFiles.add(segFile);
				}
			}
		}
		return allFiles;
	}
	public static String getSegFile(int x, int y, int z, String uid, String obfuscate, Long2ObjectOpenHashMap<String> fileNameCache, String segmentPathData) {
		long segmentBufferIndex = SegmentBufferManager.getBufferIndexFromAbsolute(x, y, z);
		if(fileNameCache != null){
			/*
			 * fileNameChache needs to be synched
			 * since this function can be accessed by
			 * threads
			 */
			synchronized (fileNameCache) {
				String g;
				if ((g = fileNameCache.get(segmentBufferIndex)) != null) {
					//found cache
					return g;
				}
			}
		}
		
		
		int xS = SegmentBufferManager.getBufferCoordAbsolute(x);
		int yS = SegmentBufferManager.getBufferCoordAbsolute(y);
		int zS = SegmentBufferManager.getBufferCoordAbsolute(z);
		
		StringBuilder sb = new StringBuilder();
		sb.append(segmentPathData);
		
		if (obfuscate != null) {
			convertUID(uid, obfuscate, sb);

			sb.append(".");
			sb.append(xS);
			sb.append(".");
			sb.append(yS);
			sb.append(".");
			sb.append(zS);
			//			sb.append(segmentController.getUniqueIdentifier());
		} else {
			sb.append(uid);
			sb.append(".");
			sb.append(xS);
			sb.append(".");
			sb.append(yS);
			sb.append(".");
			sb.append(zS);
		}

		sb.append(BLOCK_FILE_EXT);
		
		if(fileNameCache != null){
			synchronized (fileNameCache) {
				fileNameCache.put(segmentBufferIndex, sb.toString());
			}
		}

		return sb.toString();
	}

	public static void convertUID(String uid, String clientHostName, StringBuilder out) {
		StringBuilder sbUni = new StringBuilder();
		sbUni.append(uid);

		String host = clientHostName;
		//this should be collision free
		long hash = Math.abs(uid.hashCode() + host.hashCode()) % 128;
		//ffs, prove is needed....
		//all possible value are >0 and <256
		//hashcode is integer, so no way, a long is going to overflow
		//hashcode MAY collide, but will resolve in different coding
		//-> no way 2 different filenames are going to be encoded the same
		for (int i = 0; i < sbUni.length(); i++) {
			out.append(alphabet.charAt(((int) ((sbUni.charAt(i) * (i + hash)) % alphabet.length()))));
		}
	}
	public static void deleteEntitiyFileAndAllData(String UID) throws Exception{
		Vector3i minPos = new Vector3i();
		Vector3i maxPos = new Vector3i();
		getMinMax(UID, minPos, maxPos);
		List<String> allFiles = SegmentDataFileUtils.getAllFiles(minPos, maxPos, UID, null);
		int found = 0;
		for(String sA : allFiles){
			File f = new FileExt(sA);
			if(f.exists()){
				found++;
				System.err.println("[SEGMENT][REMOVE] removing raw block data file: " + f.getName()+" (exists: "+f.exists()+")");
				f.delete();
			}
		}
		
		File entFile = new FileExt(GameServerState.ENTITY_DATABASE_PATH+UID+".ent");
		
		if(entFile.exists()){
			entFile.delete();
			System.err.println("[SEGMENT][REMOVE] removed entity file: "+entFile.getAbsolutePath());
		}
	}
	public static void getMinMax(String UID, Vector3i min, Vector3i max) throws Exception{
		File entFile = new FileExt(GameServerState.ENTITY_DATABASE_PATH+UID+".ent");
		
		if(entFile.exists()){
				Tag readFrom = Tag.readFrom(new BufferedInputStream(new FileInputStream(entFile)), true, false);
				Tag entTag = null;
				
				if("s3".equals(readFrom.getName())){
					entTag = readFrom;
				}else{
				
					Tag[] sub = (Tag[])readFrom.getValue();
					
					for(Tag t : sub){
						if("s3".equals(t.getName())){
							entTag = t;
							break;
						}
					}
				}
				
				if(entTag != null){
					Tag[] ts = (Tag[])entTag.getValue();
					min.set((Vector3i)ts[1].getValue());
					max.set((Vector3i)ts[2].getValue());
					return;
				}
		}
		throw new IllegalArgumentException("NO BB FOUND IN "+UID);
	}
}
