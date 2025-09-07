package org.schema.game.client.view.cubes;

import org.schema.common.util.MemoryManager;
import org.schema.common.util.MemoryManager.MemFloatArray;

public class CubeBufferFloat implements CubeBuffer{
	public final MemFloatArray frontBuffer; 
	public final MemFloatArray backBuffer; 
	public final MemFloatArray bottomBuffer; 
	public final MemFloatArray topBuffer; 
	public final MemFloatArray rightBuffer; 
	public final MemFloatArray leftBuffer;
	public final MemFloatArray angledBuffer;
	public final MemFloatArray[] buffers;
	public final MemFloatArray totalBuffer;
	
	
	
	public CubeBufferFloat(MemoryManager man){
		int sixth = (CubeInfo.INDEX_BUFFER_SIZE * CubeMeshBufferContainer.vertexComponents) / 6;
		
		
		
		frontBuffer = man.floatArray(sixth);
		backBuffer = man.floatArray(sixth);
		bottomBuffer = man.floatArray(sixth);
		topBuffer = man.floatArray(sixth);
		rightBuffer = man.floatArray(sixth);
		leftBuffer = man.floatArray(sixth);
		angledBuffer = man.floatArray(sixth*4); //max 4 angled shapes per block (sprites)
		
		buffers = new MemFloatArray[]{
			frontBuffer,
			backBuffer,
			bottomBuffer,
			topBuffer,
			rightBuffer,
			leftBuffer,
			angledBuffer,
		};
		
		totalBuffer = man.floatArray(CubeInfo.INDEX_BUFFER_SIZE * CubeMeshBufferContainer.vertexComponents);
	}
	
	@Override
	public void makeStructured(int[][] opaqueRanges, int[][] blendedRanges){
		totalBuffer.clear();
		flipBuffers();
		
		int tSize = 0;
		for(int i = 0; i < buffers.length; i++){
			MemFloatArray b = buffers[i];
			
			
			int p = 0;
			int size = opaqueRanges[i][1];
			int lim = p+size;
			
			
			
			b.limit = (lim);
			b.position = (p);
			
//			System.err.println("OPA: "+i+": "+p+" -> "+lim+"; rem "+b.remaining()+"; totpos: "+totalBuffer.position());
			if(p < lim) {
				totalBuffer.put(b);
			}
			
			tSize += size;
			
			
		}
		
		for(int i = 0; i < buffers.length; i++){
			MemFloatArray b = buffers[i];
			
			int p = opaqueRanges[i][1];
			int lim = p+blendedRanges[i][1];
			
			b.limit = (lim);
			b.position = (p);
			
			if(p < lim) {
				totalBuffer.put(b);
			}
			
		}
		
	}
	@Override
	public void make(){
		totalBuffer.clear();
		flipBuffers();
		for(int i = 0; i < buffers.length; i++){
			MemFloatArray b = buffers[i];
			
			totalBuffer.put(b);
		}
	}
	@Override
	public void rewindBuffers(){
		for(MemFloatArray b : buffers){
			b.rewind();
		}
	}
	@Override
	public void flipBuffers(){
		for(MemFloatArray b : buffers){
			b.flip();
		}
	}
	@Override
	public void clearBuffers(){
		for(int i = 0; i < buffers.length; i++){
			MemFloatArray b = buffers[i];
			b.clear();
		}
	}

	@Override
	public int limitBuffers() {
		int l = 0;
		for(MemFloatArray b : buffers){
			l += b.limit();
		}
		return l;
	}

	@Override
	public int totalPosition() {
		int l = 0;
		for(MemFloatArray b : buffers){
			l += b.position;
		}
		return l;
	}

	@Override
	public void createOpaqueSizes(int[][] opaqueRanges) {
		int sPos = 0;
		for(int i = 0; i < buffers.length; i++){
			MemFloatArray b = buffers[i];
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
			MemFloatArray b = buffers[i];
			int pos = b.position;
			
			int opaqueSize = opaqueRanges[i][1];
			int blendedSize = pos - opaqueSize;
			
			blendedRanges[i][0] = sPos;
			blendedRanges[i][1] = blendedSize;
			
			sPos += blendedSize;
			
//			System.err.println("SIDE: "+i+": "+opaqueRanges[i][0]+", "+opaqueRanges[i][1]+"; bl: "+blendedRanges[i][0]+", "+blendedRanges[i][1]);
		}
	}

	@Override
	public MemFloatArray getTotalBuffer() {
		return totalBuffer;
	}
}
