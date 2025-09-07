package org.schema.game.common.data;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.resource.DiskWritable;
import org.schema.schine.resource.FileExt;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class EntityFileTools {
	public static void write(final HashMap<String, Object> locks, final DiskWritable ts, final String pathRaw, final String fileName) throws IOException {
		
		String path = pathRaw;
		
		if(!path.endsWith("/")){
			path +="/";
		}
//		System.err.println("[SERVER] WRITING ENTITY TAG: " + ts + " to " + fileName);
//		if(ts instanceof PlayerState){
//			System.err.println("[SERVER] Additional writing info for player state: "+((PlayerState)ts).toDetailedString());
//		}
		Object lock = null;
		synchronized (locks) {
			lock = locks.get(fileName);
			if (lock == null) {
				lock = new Object();
				locks.put(fileName, lock);
			}
		}
		long t0 = System.currentTimeMillis();
		long tagCreateTime = 0;
		long tagWriteTime = 0;
		long delTime = 0;
		long renameTime = 0;
		synchronized (lock) {
			File tmpFile = new FileExt(path+"tmp/" + fileName + ".tmp");
			
			if(tmpFile.exists()){
				System.err.println("Exception: tmpFile of "+ts.getUniqueIdentifier()+" still exists");
				tmpFile.delete();
			}
			
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(tmpFile), 4096);
			DataOutputStream os = new DataOutputStream(bufferedOutputStream);

			t0 = System.currentTimeMillis();
			Tag tagStructure = ts.toTagStructure();
			try {
				if (ts instanceof PlayerState || ts instanceof Ship) {
					if (tagStructure.getType() != Type.STRUCT || tagStructure.getValue() == null || ((Tag[]) tagStructure.getValue()).length < 1) {
						os.close();
						throw new IllegalArgumentException("serialization of " + ts + " failed, and will not be written because it could lead to corruption. Please send in a report!");
					}
				}

				tagCreateTime = System.currentTimeMillis() - t0;

				t0 = System.currentTimeMillis();
				tagStructure.writeTo(os, true);
				os.close();
				tagWriteTime = System.currentTimeMillis() - t0;
			} catch (RuntimeException e) {
				System.err.println("Exception during write of: " + ts);
				try {
					os.close();
				} catch (Exception es) {
					es.printStackTrace();
				}
				throw e;
			}

			File over = new FileExt(path+fileName);
			File old = new FileExt(path+fileName + ".old");

			if (over.exists()) {
				if (old.exists()) {
					System.err.println("Exception: tried parallel write off OLD: " + old.getName());
					t0 = System.currentTimeMillis();
					old.delete();
					delTime += System.currentTimeMillis() - t0;
				}
				t0 = System.currentTimeMillis();
//				over.renameTo(old);
				FileUtil.copyFile(over, old);
				renameTime += System.currentTimeMillis() - t0;

				t0 = System.currentTimeMillis();
				boolean deleted = over.delete();
				System.err.println("[SERVER] DELETING ORIGINALFILE TO REPLACE WITH NEW ONE: "+deleted+"; "+over.getAbsolutePath());
				delTime += System.currentTimeMillis() - t0;
			}
			
			t0 = System.currentTimeMillis();
			FileUtil.copyFile(tmpFile, over);
			
			tmpFile.delete();
			renameTime += System.currentTimeMillis() - t0;
			if (old.exists()) {
				t0 = System.currentTimeMillis();
				old.delete();
				delTime += System.currentTimeMillis() - t0;
			}
			
			if(ts instanceof PlayerState){
				System.err.println("[SERVER] CHECKING IF FILE EXISTS "+over.exists());
				if(!over.exists()){
					throw new FileNotFoundException(over.getAbsolutePath());
				}
			}
		}
		if(ts instanceof PlayerState){
			System.err.println("[SERVER] PlayerState writing done: "+((PlayerState)ts).toDetailedString());
		}
		
		File check = new FileExt(path+fileName);
		if(!check.exists()){
			throw new FileNotFoundException("ERROR WRITING FILE: "+check.getAbsolutePath());
		}
	}
}
