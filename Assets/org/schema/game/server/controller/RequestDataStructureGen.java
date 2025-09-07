package org.schema.game.server.controller;

/**
 * Base request data for generation using structures
 */
public class RequestDataStructureGen extends RequestData {

	public TerrainChunkCacheElement[] rawChunks = new TerrainChunkCacheElement[27];
	public TerrainChunkCacheElement currentChunkCache = new TerrainChunkCacheElement();

	@Override
	public void reset() {
		if(currentChunkCache != null){
			currentChunkCache.clear();
		}
	}
}
