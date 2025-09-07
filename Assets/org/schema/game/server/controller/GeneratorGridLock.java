package org.schema.game.server.controller;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

public class GeneratorGridLock{
	
	
	private final Long2ObjectOpenHashMap<TerrainChunkCacheElement> cache = new Long2ObjectOpenHashMap<TerrainChunkCacheElement>();
	
	private final LongOpenHashSet locked = new LongOpenHashSet(); 
	
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return array index of the index and TerrainChunkCacheArray array
	 */
	public static int getGridCoordinateLocal(int x, int y, int z){
		return z*9+y*3+x;
	}
	
	
	/**
	 * only use when chunk is locked
	 * @param index
	 * @param e
	 */
	public void putInCache(long index, TerrainChunkCacheElement e){
		synchronized(cache){
			cache.put(index, e);
		}
	}
	/**
	 * only use when chunk is locked
	 * @param index
	 * @param e
	 */
	public TerrainChunkCacheElement getFromCache(long index){
		synchronized(cache){
			return cache.get(index);
		}
	}
	
	
	public void unlockGrid(long[] indices) {
		for(long l : indices){
			locked.remove(l);
		}
	}

	public void lockGrid(long[] indices) {
		for(long l : indices){
			locked.add(l);
		}
	}

	public boolean isGridLocked(long[] indices) {
		for(long l : indices){
			if(locked.contains(l)){
				return true;
			}
		}
		return false;
	}
}
