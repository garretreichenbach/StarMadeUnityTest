package org.schema.game.client.view.cubes;

import org.schema.common.util.MemoryManager.ManagedMemoryChunk;


public interface CubeBuffer {
	
	public void makeStructured(int[][] opaqueRanges, int[][] blendedRanges);
	
	public void make();
	
	public void rewindBuffers();
	
	public void flipBuffers();
	
	
	public void clearBuffers();
	
	public int limitBuffers();

	public int totalPosition();

	public void createOpaqueSizes(int[][] opaqueRanges);

	public void createBlendedRanges(int[][] opaqueRanges, int[][] blendedRanges);
	
	public ManagedMemoryChunk getTotalBuffer();
}
