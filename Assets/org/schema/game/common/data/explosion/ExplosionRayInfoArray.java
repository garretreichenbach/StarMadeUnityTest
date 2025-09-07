package org.schema.game.common.data.explosion;

import java.io.DataInputStream;
import java.io.IOException;

import it.unimi.dsi.fastutil.shorts.ShortCollection;

public class ExplosionRayInfoArray {

	
	private int indices[];
	private short sizes[];
	private short values[];
	public static ExplosionRayInfoArray read(int sizeRayInfo, int totalSize, ExplosionDataHandler h, DataInputStream sIn) throws IOException {
		
		ExplosionRayInfoArray r = new ExplosionRayInfoArray();
		r.readIn(sizeRayInfo, totalSize, h, sIn);
//		rayInfo = new ExplosionRayInfo[sizeRayInfo*sizeRayInfo*sizeRayInfo];
//		for (int z = 0; z < sizeRayInfo; z++) {
//			for (int y = 0; y < sizeRayInfo; y++) {
//				for (int x = 0; x < sizeRayInfo; x++) {
//					rayInfo[getIndexRay(x,y,z)] = ExplosionRayInfo.read(sIn);
//				}
//			}
//		}
		return r;
	}
	private void readIn(int sizeRayInfo, int totalSize, ExplosionDataHandler h, DataInputStream sIn) throws IOException{
		
		long t = System.nanoTime();
		indices = new int[sizeRayInfo*sizeRayInfo*sizeRayInfo];
		sizes = new short[sizeRayInfo*sizeRayInfo*sizeRayInfo];
		values = new short[totalSize];
		
		long initTook = (System.nanoTime() - t) / 1000000;
//		System.err.println("[EXPLOSION] Array init took "+initTook+" ms");
		
		t = System.nanoTime();
		
		int index = 0;
		for (int z = 0; z < sizeRayInfo; z++) {
			for (int y = 0; y < sizeRayInfo; y++) {
				for (int x = 0; x < sizeRayInfo; x++) {
					int arrayIndex = h.getIndexRay(x,y,z);
					
					final short size = sIn.readShort();
					
					indices[arrayIndex] = index;
					sizes[arrayIndex] = size;
					
					for(int i = 0; i < size; i++){
						values[index] = sIn.readShort();
						index++;
					}
					
					
//					indices[arrayIndex] = ExplosionRayInfo.read(sIn);
				}
			}
		}
		long readTook = (System.nanoTime() - t) / 1000000;
//		System.err.println("[EXPLOSION] 'Ray' File read took "+readTook+ "ms");
	}
	public boolean hasAny(int indexRay) {
		return getSize(indexRay) > 0;
	}
	public int getSize(int indexRay){
		return sizes[indexRay];
	}
	public void addAllTo(int indexRay, ShortCollection closedList) {
		final int size = getSize(indexRay);
		int index = indices[indexRay];
		for(int i = 0; i < size; i++){
			closedList.add(values[index]);
			index++;
		}
	}
}
