package org.schema.game.common.data.world.migration;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.GameResourceLoader;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.common.controller.io.*;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.*;
import org.schema.game.common.util.FolderZipper.ZipCallback;
import org.schema.schine.resource.FileExt;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Chunk32Migration {

	private static final ByteBuffer dataByteBuffer = MemoryUtil.memAlloc(1024*1024);
	private final String UID;
	private File dir;
	private String dirName;
	static{
		
		if(ElementKeyMap.keySet.isEmpty()){
			ElementKeyMap.initializeData(GameResourceLoader.getConfigInputFile());
		}
		try {
			BlockShapeAlgorithm.initialize();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public Chunk32Migration(String currentPrefix, String dirName, File dir) {
		this.UID = currentPrefix.substring(0, currentPrefix.length()-1);
		this.dir = dir;
		this.dirName = dirName;
		
	}
	public static void test(String UID, String dirName) throws DeserializationException, IOException{
		
		
		Vector3i s = new Vector3i(0,0,64);
		RemoteSegment check = new RemoteSegment(null);
		SegmentData ll = new SegmentData4Byte();
		ll.assignData(check);
		System.err.println(" ---- --- CHECKING: "+s);
		int r = SegmentDataIONew.requestStatic(s.x, s.y, s.z, 
				UID, dataByteBuffer, dirName, check);
		
		if(r == SegmentDataFileUtils.READ_DATA){
			System.err.println("CHECK OK "+s+" :: "+check.pos);
		
			assert(s.equals(check.pos)):"; POS "+s+"; "+check.pos;
		}
	}
	public static void main(String[] mamm) throws Exception{
		processFolderRecursive("./blueprints-default/", true, null, true);
		
		
//		for(int i = 0; i < 1000; i++){
//			testRandom();
//		}
		
//		File dir = new FileExt("./blueprints/Omega Destroyer 08/DATA/");
//		assert(FileUtils.contentEquals(
//				new FileExt(dir, "ENTITY_SHIP_Omega Destroyer 08_1469409223641.0.0.0.smd3"), 
//				new FileExt(dir, "ENTITY_SHIP_Omega Test Save_1466009427542.0.0.0.smd3")));
//		
//		{
//			String UID = "ENTITY_SHIP_Omega Test Save_1466009427542";
//			String dirName = "./blueprints/Omega Destroyer 08/DATA/";
//			test(UID, dirName);
//		}
//		{
//			String UID = "ENTITY_SHIP_Omega Destroyer 08_1469409223641";
//			String dirName = "./blueprints/Omega Destroyer 08/DATA/";
//			test(UID, dirName);
//		}
//		
//		{
//			String UID = "ENTITY_SHIP_Omega Destroyer 08_1469409223641";
//			String dirName = "./server-database/lala/DATA/";
//			test(UID, dirName);
//		}
	}
	private static void processFolderRecursive(String folder, boolean delete,
			ZipCallback zipCallback, boolean doCheck) {
		File dir = new FileExt(folder);
		if(dir.exists() && dir.isDirectory()){
			for( String m : dir.list()){
				if(m.equals("DATA")){
					processFolder(folder+m+"/", delete, zipCallback, doCheck);
				}else{
					processFolderRecursive(folder+m+"/", delete, zipCallback, doCheck);
				}
			}
		}
	}
	public static void testRandom(){
		try{
		Random r = new Random();
		
		RemoteSegment check = new RemoteSegment(null);
		SegmentData ll = new SegmentData4Byte();
		ll.assignData(check);
		System.err.println("WRITING RANDOM REGION FILE");
		int start = -8;
		SegmentRegionFileNew rf = null;
		for(int z = start; z < (start+16); z++ ){
			for(int y = start; y < (start+16); y++ ){
				for(int x = start; x < (start+16); x++ ){
					
					
					check.pos.set(x * 32, y * 32, z * 32);
					
					ll.resetFast();
					for(int i = 0; i < SegmentData.SEG_TIMES_SEG_TIMES_SEG; i++){
						
						short type = (short) r.nextInt(ElementKeyMap.highestType+1);
						while(type != 0 && !ElementKeyMap.isValidType(type)){
							type = (short) r.nextInt(ElementKeyMap.highestType+1);
						}
						ll.setType(i, type);
						ll.setHitpointsByte(i, r.nextInt(256));
						ll.setOrientation(i, (byte)r.nextInt(16));
						ll.setActive(i, r.nextBoolean());
						ll.incSize();
					}
					File tmp = new FileExt("segTestTmp.smd3");
					try {
						rf = SegmentDataIONew.write(check, System.currentTimeMillis(), tmp, rf);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		if(rf != null){
			try {
				System.err.println("REGION FILE :::: "+rf.getFile().length());
			
				rf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			File tmp = new FileExt("segTestTmp.smd3");
			tmp.delete();
		}
		} catch (SegmentDataWriteException e) {
			e.printStackTrace();
			throw new RuntimeException("this should be never be thrown as migration should always be to"
					+ "a normal segment data", e);
		}
	}
	public static void processFolder(String folder, boolean delete, ZipCallback zipCallback, boolean doCheck){
		File dir = new FileExt(folder);
		if(dir.exists() && dir.isDirectory()){
			String[] list = dir.list();
			
			Arrays.sort(list);
			
			ObjectArrayList<String> col = new ObjectArrayList<String>();
			
			
			String currentPrefix = null;
			for(String s : list){
				if(s.endsWith(SegmentDataIO16.BLOCK_FILE_EXT)){
					if(zipCallback != null){
						zipCallback.update(null);
					}
					if(currentPrefix == null || !s.startsWith(currentPrefix)){
						if(!col.isEmpty()){
							try {
								if(currentPrefix == null){
									throw new NullPointerException();
								}
								processObject(currentPrefix, folder, dir, col, doCheck);
							} catch (Exception e) {
								e.printStackTrace();
							} 
							col.clear();
						}
						currentPrefix = s.substring(0, s.indexOf(".")+1);
					}
					
					col.add(s);
				}
			}
			if(!col.isEmpty()){
				try {
					if(currentPrefix == null){
						throw new NullPointerException();
					}
					processObject(currentPrefix, folder, dir, col, doCheck);
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
			if(delete){
				for(String s : list){
					if(s.endsWith(SegmentDataIO16.BLOCK_FILE_EXT)){
						File f = new FileExt(dir, s);
						f.delete();
					}
				}
			}
		}
	}




	

	private static void processObject(String currentPrefix, String dirName, File dir, ObjectArrayList<String> col, boolean doCheck) throws DeserializationException, IOException {
		
		Chunk32Migration c = new Chunk32Migration(currentPrefix, dirName, dir);
		System.err.println("PROCESSING CHUNK16 FILES: "+currentPrefix+"; File count: "+col.size());
		for(String s : col){
			
			c.add(s);
		}
		
		try {
			c.buildChunk32();
		} catch (SegmentDataWriteException e) {
			throw new RuntimeException("Should not happen at this point", e);
		}
		
		c.writeChunk32(doCheck);
		
		
	}
	
	private void writeChunk32(boolean doCheck) throws IOException {
		Map<String, SegmentRegionFileNew> l = new Object2ObjectOpenHashMap<String, SegmentRegionFileNew>();
		Set<String> names = new ObjectOpenHashSet<String>();
		System.err.println("WRITING NEW CHUNK32 FORMAT. Count: "+map32.size());
		for(Chunk32FirstVersionRM sD : map32.values()){
			
			RemoteSegment s = sD.s;
			
		
			s.setSize(sD.migrated.getSize());
			String fileName = SegmentDataFileUtils.getSegFile(s.pos.x, s.pos.y, s.pos.z, 
					UID, 
					null, null, dirName);
			File f = new FileExt(fileName);
			if(!names.contains(fileName)){
				//remove previous file
				names.add(fileName);
				f.delete();
			}
			
			SegmentRegionFileNew write = SegmentDataIONew.write(s, time, f, l.get(fileName));
			if(write != null){
				l.put(fileName, write);
			}
		}
		System.err.println("FLUSHING ALL WRITTEN FILES TO DISK. File count: "+l.size());
		for(SegmentRegionFileNew f : l.values()){
//			f.flush(true);
			f.close();
		}
		
//		if(doCheck){
//			for(RemoteSegment s : map32.values()){
//				RemoteSegment check = new RemoteSegment(null);
//				SegmentData ll = new SegmentData();
//				ll.assignData(check);
//				System.err.println(" ---- --- CHECKING: "+s.pos);
//				int r = SegmentDataIONew.requestStatic(s.pos.x, s.pos.y, s.pos.z, 
//						UID, dataByteBuffer, dirName, check);
//				
//				if(r == SegmentDataFileUtils.READ_DATA){
//					System.err.println("CHECK OK "+s.pos+" :: "+check.pos);
//				
//					assert(s.pos.equals(check.pos)):"SIZE "+s.getSize()+" / "+check.getSize()+"; POS "+s.pos+"; "+check.pos;
//				}
//			}
//		}
	}




	private Long2ObjectOpenHashMap<Chunk16SegmentData> map = new Long2ObjectOpenHashMap<Chunk16SegmentData>();
	private Long2ObjectOpenHashMap<Chunk32FirstVersionRM> map32 = new Long2ObjectOpenHashMap<Chunk32FirstVersionRM>();
	private final Vector3i min = new Vector3i(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE); 
	private final Vector3i max = new Vector3i(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE); 
	long time = System.currentTimeMillis();
	
	private class Chunk32FirstVersionRM{
		public SegmentData3Byte dataOld;
		public final SegmentData migrated = new SegmentData4Byte();
		public RemoteSegment s = new RemoteSegment(null);
		
		public Chunk32FirstVersionRM(){
			migrated.assignData(s);
		}
	}

	private void buildChunk32() throws SegmentDataWriteException {
		System.err.println("BUILDING CHUNK32 IN RANGE: ["+min+", "+max+"]");
		
		for(int z = min.z; z < max.z; z++){
			for(int y = min.y; y < max.y; y++){
				for(int x = min.x; x < max.x; x++){
					int xx = ByteUtil.divU16(x)*16;
					int yy = ByteUtil.divU16(y)*16;
					int zz = ByteUtil.divU16(z)*16;
					long index16 = ElementCollection.getIndex(xx, yy, zz);
					
					
					
					int cX = x+8;
					int cY = y+8;
					int cZ = z+8;
					int xTar = ByteUtil.divU32(cX)*32;
					int yTar = ByteUtil.divU32(cY)*32;
					int zTar = ByteUtil.divU32(cZ)*32;
					
					long index32 = ElementCollection.getIndex(xTar, yTar, zTar);
					
					
					
					Chunk16SegmentData chunk16Data = map.get(index16);
					
					if(chunk16Data != null){
						int local16 = Chunk16SegmentData.getInfoIndex(ByteUtil.modU16(x), ByteUtil.modU16(y), ByteUtil.modU16(z));
						
						boolean hasType = chunk16Data.getType(local16) > 0;
							
						if(hasType){
							int local32 = SegmentData3Byte.getInfoIndex(ByteUtil.modU32(cX), ByteUtil.modU32(cY), ByteUtil.modU32(cZ));
							
							Chunk32FirstVersionRM chunk32 = map32.get(index32);
							if(chunk32 == null){
								chunk32 = new Chunk32FirstVersionRM();
								chunk32.s.setPos(xTar, yTar, zTar);
								chunk32.dataOld = new SegmentData3Byte();
								map32.put(index32, chunk32);
							}
//							chunk32.getSegmentData().getAsIntBuffer()[local32] = SegmentData.convertIntValueDirect(chunk16Data, local16);
							
							chunk32.dataOld.getAsOldByteBuffer()[local32] = chunk16Data.getAsOldByteBuffer()[local16];
							chunk32.dataOld.getAsOldByteBuffer()[local32+1] = chunk16Data.getAsOldByteBuffer()[local16+1];
							chunk32.dataOld.getAsOldByteBuffer()[local32+2] = chunk16Data.getAsOldByteBuffer()[local16+2];
							
							assert(chunk16Data.getType(local16) == chunk32.dataOld.getType(local32));
							if(hasType){
								chunk32.dataOld.incSize();
							}
						}
					}
					
				}
			}
		}
		SegmentSerializationBuffersGZIP bm = SegmentSerializationBuffersGZIP.get();
		try {
			for(Chunk32FirstVersionRM rm : map32.values()){
				int currentVersion = Chunk16SegmentData.CHUNK16VERSION;
				SegmentDataInterface dummy = rm.dataOld;
				while(currentVersion < SegmentDataIONew.VERSION-1){
					
					SegmentDataInterface dummyNext = bm.dummies.get(currentVersion+1);
					dummyNext.resetFast();
					dummy.migrateTo(currentVersion, dummyNext);
					dummyNext.setSize(dummy.getSize());
					dummy.resetFast();
					dummy = dummyNext;
					currentVersion++;
				}
				if(currentVersion == SegmentDataIONew.VERSION-1){
					dummy.migrateTo(currentVersion, rm.migrated);
					dummy.resetFast();
					
					for(int i = 0; i < SegmentData.TOTAL_SIZE; i++){
						short type = rm.migrated.getType(i);
						if(type != 0){
							rm.migrated.incSize();
						}
					}
				}
			}
		}finally {
			SegmentSerializationBuffersGZIP.free(bm);
		}
	}

	
	public void add(String fileName) throws DeserializationException, IOException{
		File f = new FileExt(this.dir, fileName);
		SegmentDataIO16.request(f, map, min, max, dataByteBuffer, time);
	}
		
	
}
