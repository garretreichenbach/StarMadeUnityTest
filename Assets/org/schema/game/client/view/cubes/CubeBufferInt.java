package org.schema.game.client.view.cubes;

import org.schema.common.util.MemoryManager;
import org.schema.common.util.MemoryManager.MemIntArray;

public class CubeBufferInt implements CubeBuffer{
	public final MemIntArray frontBuffer; 
	public final MemIntArray backBuffer; 
	public final MemIntArray bottomBuffer; 
	public final MemIntArray topBuffer; 
	public final MemIntArray rightBuffer; 
	public final MemIntArray leftBuffer;
	public final MemIntArray angledBuffer;
	public final MemIntArray[] buffers;
	public final MemIntArray totalBuffer;
	
	
	
	public CubeBufferInt(MemoryManager man){
		int sixth = (CubeInfo.INDEX_BUFFER_SIZE * CubeMeshBufferContainer.vertexComponents) / 6;
		
		
		
		frontBuffer = man.intArray(sixth);
		backBuffer = man.intArray(sixth);
		bottomBuffer = man.intArray(sixth);
		topBuffer = man.intArray(sixth);
		rightBuffer = man.intArray(sixth);
		leftBuffer = man.intArray(sixth);
		angledBuffer = man.intArray(sixth*4); //max 4 angled shapes per block (sprites)
		
		buffers = new MemIntArray[]{
			frontBuffer,
			backBuffer,
			bottomBuffer,
			topBuffer,
			rightBuffer,
			leftBuffer,
			angledBuffer,
		};
		
		totalBuffer = man.intArray(CubeInfo.INDEX_BUFFER_SIZE * CubeMeshBufferContainer.vertexComponents);
		
		
	}
	
	@Override
	public void makeStructured(int[][] opaqueRanges, int[][] blendedRanges){
		totalBuffer.clear();
		flipBuffers();
		
		for(int i = 0; i < buffers.length; i++){
			MemIntArray b = buffers[i];
			
			
			int start = 0;
			int lim = start+opaqueRanges[i][1];
			if(start < lim) {
			
				b.limit = lim;
				b.position = start;
				
				totalBuffer.put(b);
			}
			
		}
		
		for(int i = 0; i < buffers.length; i++){
			MemIntArray b = buffers[i];
			
			int start = opaqueRanges[i][1];
			int lim = start+blendedRanges[i][1];
			if(start < lim) {
				b.limit = lim;
				b.position = start;
				
				totalBuffer.put(b);
			}
			
		}
		
	}
	@Override
	public void make(){
		totalBuffer.clear();
		flipBuffers();
		for(int i = 0; i < buffers.length; i++){
			MemIntArray b = buffers[i];
			
			totalBuffer.put(b);
		}
	}
	@Override
	public void rewindBuffers(){
		for(MemIntArray b : buffers){
			b.rewind();
		}
	}
	@Override
	public void flipBuffers(){
		for(MemIntArray b : buffers){
			b.flip();
		}
	}
	@Override
	public void clearBuffers(){
		for(int i = 0; i < buffers.length; i++){
			MemIntArray b = buffers[i];
			b.clear();
		}
	}

	@Override
	public int limitBuffers() {
		int l = 0;
		for(MemIntArray b : buffers){
			l += b.limit;
		}
		return l;
	}

	@Override
	public int totalPosition() {
		int l = 0;
		for(MemIntArray b : buffers){
			l += b.position;
		}
		return l;
	}

	@Override
	public void createOpaqueSizes(int[][] opaqueRanges) {
		int sPos = 0;
		for(int i = 0; i < buffers.length; i++){
			MemIntArray b = buffers[i];
			int pos = b.position;
			opaqueRanges[i][0] = sPos;
			opaqueRanges[i][1] = pos;
			sPos += pos;
		}
	}

	@Override
	public void createBlendedRanges(int[][] opaqueRanges, int[][] blendedRanges) {
		int sPos = 0;
		for(int i = 0; i < buffers.length; i++){
			MemIntArray b = buffers[i];
			int pos = b.position;
			
			int opaqueSize = opaqueRanges[i][1];
			int blendedSize = pos - opaqueSize;
			
			blendedRanges[i][0] = sPos;
			blendedRanges[i][1] = blendedSize;
			
			sPos += blendedSize;
			
		}
	}

	@Override
	public MemIntArray getTotalBuffer() {
		return totalBuffer;
	}
}
