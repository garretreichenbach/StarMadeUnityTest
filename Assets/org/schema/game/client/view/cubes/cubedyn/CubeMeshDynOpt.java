package org.schema.game.client.view.cubes.cubedyn;

import com.bulletphysics.linearmath.Transform;
import org.lwjgl.opengl.ARBMapBufferRange;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.MemoryManager.ManagedMemoryChunk;
import org.schema.game.client.view.cubes.*;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.world.DrawableRemoteSegment;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.GraphicsContext;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;

import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class  CubeMeshDynOpt implements CubeMeshInterface {

	private boolean initialized;
	private VBOCell currentVBOCell;
	private CubeMeshManagerBulkOptimized man;

	public CubeMeshDynOpt(CubeMeshManagerBulkOptimized man) {
		this.man = man;
	}
	
	public void mark(Transform t, int id, int optionBits, boolean markBlended, int vismask) {
		if(vismask == -1) return;
		if (currentVBOCell != null) {
			if(vismask == Element.VIS_ALL){ 
				int start;
				int count;

				if (!markBlended) {
					start = (currentVBOCell.startPositionByte / ByteUtil.SIZEOF_FLOAT) / CubeMeshBufferContainer.vertexComponents;
					count = (currentVBOCell.blendedFloatStartPos) / CubeMeshBufferContainer.vertexComponents;
				} else {
					start = (currentVBOCell.startPositionByte / ByteUtil.SIZEOF_FLOAT) / CubeMeshBufferContainer.vertexComponents + (currentVBOCell.blendedFloatStartPos) / CubeMeshBufferContainer.vertexComponents;
					count = (currentVBOCell.lengthInBytes / ByteUtil.SIZEOF_FLOAT - currentVBOCell.blendedFloatStartPos) / CubeMeshBufferContainer.vertexComponents;
				}
//				System.err.println("BLEND: "+markBlended+" START "+start+"; COUNT: "+count);
				if (count > 0) {

					DrawMarker drawMarker;
					boolean exists = !currentVBOCell.vboSeg.markers.isEmpty() && currentVBOCell.vboSeg.markers.get(currentVBOCell.vboSeg.markers.size() - 1).id == id;

					if (exists) {
						drawMarker = currentVBOCell.vboSeg.markers.get(currentVBOCell.vboSeg.markers.size() - 1);
					} else {
						drawMarker = CubeMeshManagerBulkOptimized.getMarker();
						drawMarker.t.set(t);
						drawMarker.id = id;
						drawMarker.optionBits = optionBits;
						currentVBOCell.vboSeg.markers.add(drawMarker);
					}

					drawMarker.start.add(start);
					drawMarker.count.add(count);
				}
			}else{
				for(int i = 0; i < 1; i++){
					int flag = i == 6 ? 0 : Element.SIDE_FLAG[i]; // seventh side always drawn (all angled sides)
					if((vismask & flag) == flag){
						int start;
						int count;
						int sPos = (currentVBOCell.startPositionByte / ByteUtil.SIZEOF_FLOAT) / CubeMeshBufferContainer.vertexComponents;
						if (!markBlended) {
							//OPAQUE
							int startOfCell = sPos;
							int offset = currentVBOCell.opaqueRanges[i][0] / CubeMeshBufferContainer.vertexComponents;
							start =  startOfCell+offset;
							count = currentVBOCell.opaqueRanges[i][1] / CubeMeshBufferContainer.vertexComponents;
						} else {
							int startOfBlendedCell = 
									sPos + (currentVBOCell.blendedFloatStartPos) / CubeMeshBufferContainer.vertexComponents; 
							int offset = currentVBOCell.blendedRanges[i][0] / CubeMeshBufferContainer.vertexComponents;
							start = startOfBlendedCell+offset;
							count = currentVBOCell.blendedRanges[i][1] / CubeMeshBufferContainer.vertexComponents;
						}
						
						
						if (count > 0) {
							
							DrawMarker drawMarker;
							boolean exists = !currentVBOCell.vboSeg.markers.isEmpty() && currentVBOCell.vboSeg.markers.get(currentVBOCell.vboSeg.markers.size() - 1).id == id;
		
							if (exists) {
								drawMarker = currentVBOCell.vboSeg.markers.get(currentVBOCell.vboSeg.markers.size() - 1);
							} else {
								drawMarker = CubeMeshManagerBulkOptimized.getMarker();
								drawMarker.t.set(t);
								drawMarker.id = id;
								drawMarker.optionBits = optionBits;
								currentVBOCell.vboSeg.markers.add(drawMarker);
							}
		
							drawMarker.start.add(start);
							drawMarker.count.add(count);
						}
					}
				}
				
			}
		}

	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void contextSwitch(CubeMeshBufferContainer container, int[][] opaqueRanges, int[][] blendedRanges, int blendBufferPos, int totalDrawnBlockCount, DrawableRemoteSegment segment) {
		if (CubeMeshManagerBulkOptimized. DEBUG) {
			GlUtil.printGlErrorCritical();
		}
		if (!CubeMeshNormal.checkedForRangeMap) {
			CubeMeshNormal.setGl_ARB_map_buffer_range(GraphicsContext.current.getCapabilities().GL_ARB_map_buffer_range);
			System.err.println("USE BUFFER RANGE: " + CubeMeshNormal.isGl_ARB_map_buffer_range());
			CubeMeshNormal.checkedForRangeMap = true;
		}
		boolean init = false;

		container.dataBuffer.makeStructured(opaqueRanges, blendedRanges);
		container.dataBuffer.getTotalBuffer().flip();
//		System.err.println("BUFFER "+container.dataBuffer.getTotalBuffer().position()+"; "+container.dataBuffer.getTotalBuffer().limit()+" / "+container.dataBuffer.getTotalBuffer().length());
		if (!initialized) {
			prepare(null);
			init = true;
		}

		if (container.dataBuffer.getTotalBuffer().limit() != 0) {

			long tPut = 0;
			long tOrder = 0;
			long tUnmap = 0;

			int sizeNeededInBytes = container.dataBuffer.getTotalBuffer().limit() * ByteUtil.SIZEOF_FLOAT;
			if (currentVBOCell != null) {
				currentVBOCell.free = true;
			}
			if (currentVBOCell != null && currentVBOCell.stillFitsInTaken(sizeNeededInBytes)) {
				// nothing to do, we can use the old one
			} else {
				currentVBOCell = man.getFreeSegment(sizeNeededInBytes, segment);
			}
			assert (currentVBOCell  != null):segment+"; "+segment.getSegmentController();
			assert (currentVBOCell.getBufferId() != 0);

			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, currentVBOCell.getBufferId()); // Bind
			if (CubeMeshManagerBulkOptimized.DEBUG) {
				GlUtil.printGlErrorCritical();
			}
			long tStart = System.nanoTime();
			boolean debugUseMapBuffer = false;
			if (debugUseMapBuffer && CubeMeshNormal.USE_MAP_BUFFER) {
				if (CubeMeshNormal.isGl_ARB_map_buffer_range()) {
					assert (currentVBOCell.startPositionByte + currentVBOCell.lengthInBytes <= currentVBOCell.vboSeg.maxBytes);
					if (!(currentVBOCell.startPositionByte + currentVBOCell.lengthInBytes <= currentVBOCell.vboSeg.maxBytes)) {
						throw new RuntimeException("OPENGL: Buffer invalid: " + currentVBOCell.startPositionByte + "; " + currentVBOCell.endPositionByte + "; LENGTH: " + currentVBOCell.lengthInBytes + "; MAX: " + currentVBOCell.vboSeg.maxBytes);
					}
					CubeMeshNormal.oldHelpBuffer = ARBMapBufferRange.glMapBufferRange(
							GL15.GL_ARRAY_BUFFER,
							currentVBOCell.startPositionByte,
							currentVBOCell.lengthInBytes,
							CubeMeshNormal.mappingByte,
							CubeMeshNormal.oldHelpBuffer == null ? null : CubeMeshNormal.oldHelpBuffer);

					assert ((currentVBOCell.startPositionByte + container.dataBuffer.getTotalBuffer().limit() * ByteUtil.SIZEOF_FLOAT) < currentVBOCell.vboSeg.maxBytes);
					if (CubeMeshManagerBulkOptimized.DEBUG) {
						GlUtil.printGlErrorCritical("STARTPOS: " + currentVBOCell.startPositionByte + "; len: " + container.dataBuffer.getTotalBuffer().limit() * ByteUtil.SIZEOF_FLOAT + "; TOTAL END: " + (currentVBOCell.startPositionByte + container.dataBuffer.getTotalBuffer().limit() * ByteUtil.SIZEOF_FLOAT) + " / " + currentVBOCell.vboSeg.maxBytes);
					}
//					System.err.println("STARTPOS: "+currentVBOCell.startPositionByte+"; len: "+helpBuffer.limit() * ByteUtil.SIZEOF_FLOAT+"; TOTAL END: "+(currentVBOCell.startPositionByte + helpBuffer.limit() * ByteUtil.SIZEOF_FLOAT)+" / "+MAX_BYTES);
				} else {

					CubeMeshNormal.oldHelpBuffer = GL15.glMapBuffer(
							GL15.GL_ARRAY_BUFFER,
							GL15.GL_WRITE_ONLY,
							CubeMeshNormal.oldHelpBuffer == null ? null : CubeMeshNormal.oldHelpBuffer);
					if (CubeMeshManagerBulkOptimized.DEBUG) {
						GlUtil.printGlErrorCritical();
					}
				}
				if (CubeMeshNormal.oldHelpBuffer == null && CubeMeshNormal.USE_MAP_BUFFER) {
					EngineSettings.G_USE_VBO_MAP.setOn(false);
					CubeMeshNormal.USE_MAP_BUFFER = false;
					System.err.println("[Exception]WARNING: MAPPED BUFFER HAS BEEN TURNED OFF " + GlUtil.getGlError());
				}
			}
			long tBindAD = System.nanoTime() - tStart;
			boolean glUnmapBuffer = true;
			if (debugUseMapBuffer && CubeMeshNormal.USE_MAP_BUFFER) {
				long t = System.nanoTime();
				
				;

				t = System.nanoTime();
				if (CubeMeshNormal.isGl_ARB_map_buffer_range()) {
					if(GraphicsContext.INTEGER_VERTICES){
						IntBuffer mapBuffer = CubeMeshNormal.oldHelpBuffer.order(ByteOrder.nativeOrder()).asIntBuffer();
						tOrder = (System.nanoTime() - t) / 1000000;
						mapBuffer.put(((CubeBufferInt)container.dataBuffer).totalBuffer.getBackingToCurrent());
					}else{
						FloatBuffer mapBuffer = CubeMeshNormal.oldHelpBuffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
						tOrder = (System.nanoTime() - t) / 1000000;
						mapBuffer.put(((CubeBufferFloat)container.dataBuffer).totalBuffer.getBackingToCurrent());
					}
				} else {
					
					if(GraphicsContext.INTEGER_VERTICES){
						IntBuffer mapBuffer = CubeMeshNormal.oldHelpBuffer.order(ByteOrder.nativeOrder()).asIntBuffer();
						mapBuffer.position(currentVBOCell.startPositionByte / ByteUtil.SIZEOF_FLOAT);
						tOrder = (System.nanoTime() - t) / 1000000;
						mapBuffer.put(((CubeBufferInt)container.dataBuffer).totalBuffer.getBackingToCurrent());
					}else{
						FloatBuffer mapBuffer = CubeMeshNormal.oldHelpBuffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
						mapBuffer.position(currentVBOCell.startPositionByte / ByteUtil.SIZEOF_FLOAT);
						tOrder = (System.nanoTime() - t) / 1000000;
						mapBuffer.put(((CubeBufferFloat)container.dataBuffer).totalBuffer.getBackingToCurrent());
					}
				}
				tPut = (System.nanoTime() - t) / 1000000;

				t = System.nanoTime();
				glUnmapBuffer = GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);
				tUnmap = (System.nanoTime() - t) / 1000000;
				if (CubeMeshManagerBulkOptimized.DEBUG) {
					GlUtil.printGlErrorCritical();
				}
			} else {
				//DO BUFFER SUB DATA UPDATE
				if(GraphicsContext.INTEGER_VERTICES){
					IntBuffer b = ((CubeBufferInt)container.dataBuffer).totalBuffer.getBackingToCurrent();
//					System.err.println("SUBDATA: "+b.position()+" "+b.limit());
					GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, currentVBOCell.startPositionByte, b);// Load The Data
				}else{
					GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, currentVBOCell.startPositionByte, ((CubeBufferFloat)container.dataBuffer).totalBuffer.getBackingToCurrent());// Load The Data
				}
				if (CubeMeshManagerBulkOptimized.DEBUG) {
					GlUtil.printGlErrorCritical();
				}
			}

			//		System.err.println("DDDD !!!!!!!!!! iB: "+container.indexBuffer.position()+" / "+container.indexBuffer.limit()+"; aB: "+container.attibuteBuffer.position()+" / "+container.attibuteBuffer.limit()+";buID "+getIndexBufferId(0)+"; atID: "+getAttributeBufferId(0));
			CubeMeshNormal.bufferContextSwitchTime = System.nanoTime() - tStart;

			CubeMeshNormal.bufferContextSwitchTime /= 1000000;
			if (CubeMeshNormal.bufferContextSwitchTime > 10) {
				System.err.println("[CUBE] WARNING: context switch time: " + CubeMeshNormal.bufferContextSwitchTime + " ms : " + tBindAD / 1000000 + "ms: O " + tOrder + "; P " + tPut + "; U " + tUnmap + "::; map " + CubeMeshNormal.USE_MAP_BUFFER + "; range " + CubeMeshNormal.isGl_ARB_map_buffer_range() + "; init " + init + "  unmap " + glUnmapBuffer);
			}

			assert (container.dataBuffer.getTotalBuffer().limit() / CubeMeshBufferContainer.vertexComponents == (currentVBOCell.lengthInBytes / ByteUtil.SIZEOF_FLOAT) / CubeMeshBufferContainer.vertexComponents) : container.dataBuffer.getTotalBuffer().limit() / CubeMeshBufferContainer.vertexComponents + "; " + (currentVBOCell.lengthInBytes / ByteUtil.SIZEOF_FLOAT) / CubeMeshBufferContainer.vertexComponents;
			if (CubeMeshManagerBulkOptimized.DEBUG) {
				GlUtil.printGlErrorCritical();
			}
			currentVBOCell.blendedFloatStartPos = blendBufferPos;
			for(int i = 0; i < 7; i++){
				currentVBOCell.blendedRanges[i][0] = blendedRanges[i][0];
				currentVBOCell.blendedRanges[i][1] = blendedRanges[i][1];
				
				currentVBOCell.opaqueRanges[i][0] = opaqueRanges[i][0];
				currentVBOCell.opaqueRanges[i][1] = opaqueRanges[i][1];
			}
		} else {
			//			System.err.println("[CUBEMESH] WARNING: tried to switch to empty mesh");
			if (currentVBOCell != null) {
				currentVBOCell.free = true;
				currentVBOCell = null;
			}
		}

	}

	@Override
	public void draw(int blended, int vis) {
		drawMesh(blended, vis);
	}

	@Override
	public void drawMesh(int blended, int vis) {

		if (currentVBOCell == null) {
			//				System.err.println("SEGMENT NULL");
			return;
		}

		assert (currentVBOCell.getBufferId() != 0);
		
		boolean glBindBuffer = GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, currentVBOCell.getBufferId());
		if (glBindBuffer) {
			
			if(GraphicsContext.INTEGER_VERTICES){
				GlUtil.glVertexAttribIPointer(ShaderLibrary.CUBE_SHADER_VERT_INDEX, CubeMeshBufferContainer.vertexComponents, GL11.GL_INT, 0, 0);	
//				GL20.glEnableVertexAttribArray(ShaderLibrary.CUBE_SHADER_VERT_INDEX);
			}else{
				GL11.glVertexPointer(CubeMeshBufferContainer.vertexComponents, GL11.GL_FLOAT, 0, 0);
			}
		}
		
		if (CubeMeshBufferContainer.isTriangle()) {
			//				System.err.println("DRAW TRIANGLE");
			
			if(vis == Element.VIS_ALL){ 
				if (blended == BOTH) {
					GL11.glDrawArrays(GL11.GL_TRIANGLES,
							(currentVBOCell.startPositionByte / ByteUtil.SIZEOF_FLOAT) / CubeMeshBufferContainer.vertexComponents,
							(currentVBOCell.lengthInBytes / ByteUtil.SIZEOF_FLOAT) / CubeMeshBufferContainer.vertexComponents);
				} else if (blended == OPAQUE) {
					GL11.glDrawArrays(GL11.GL_TRIANGLES,
							(currentVBOCell.startPositionByte / ByteUtil.SIZEOF_FLOAT) / CubeMeshBufferContainer.vertexComponents,
							(currentVBOCell.blendedFloatStartPos) / CubeMeshBufferContainer.vertexComponents);
				} else if (blended == BLENDED) {

					GL11.glDrawArrays(GL11.GL_TRIANGLES,
							(currentVBOCell.startPositionByte / ByteUtil.SIZEOF_FLOAT) / CubeMeshBufferContainer.vertexComponents + (currentVBOCell.blendedFloatStartPos) / CubeMeshBufferContainer.vertexComponents,
							(currentVBOCell.lengthInBytes / ByteUtil.SIZEOF_FLOAT - currentVBOCell.blendedFloatStartPos) / CubeMeshBufferContainer.vertexComponents
					);
				}
			}else{
				if (blended == BOTH) {
					GL11.glDrawArrays(GL11.GL_TRIANGLES,
							(currentVBOCell.startPositionByte / ByteUtil.SIZEOF_FLOAT) / CubeMeshBufferContainer.vertexComponents,
							(currentVBOCell.lengthInBytes / ByteUtil.SIZEOF_FLOAT) / CubeMeshBufferContainer.vertexComponents);
				} else if (blended == OPAQUE) {
					for(int i = 0; i < 7; i++){
						int flag = i == 6 ? 0 : Element.SIDE_FLAG[i]; // seventh side always drawn (all angled sides)
						if((vis & flag) == flag){
							int start;
							int count;
							int sPos = (currentVBOCell.startPositionByte / ByteUtil.SIZEOF_FLOAT) / CubeMeshBufferContainer.vertexComponents;
							//OPAQUE
							int startOfCell = sPos;
							int offset = currentVBOCell.opaqueRanges[i][0] / CubeMeshBufferContainer.vertexComponents;
							start =  startOfCell+offset;
							count = currentVBOCell.opaqueRanges[i][1] / CubeMeshBufferContainer.vertexComponents;
							
							GL11.glDrawArrays(GL11.GL_TRIANGLES,
									start,
									count);
						}
					
					}
				} else if (blended == BLENDED) {
					for(int i = 0; i < 7; i++){
						int flag = i == 6 ? 0 : Element.SIDE_FLAG[i]; // seventh side always drawn (all angled sides)
						if((vis & flag) == flag){
							int start;
							int count;
							int sPos = (currentVBOCell.startPositionByte / ByteUtil.SIZEOF_FLOAT) / CubeMeshBufferContainer.vertexComponents;
							int startOfBlendedCell = 
									sPos + (currentVBOCell.blendedFloatStartPos) / CubeMeshBufferContainer.vertexComponents; 
							int offset = currentVBOCell.blendedRanges[i][0] / CubeMeshBufferContainer.vertexComponents;
							start = startOfBlendedCell+offset;
							count = currentVBOCell.blendedRanges[i][1] / CubeMeshBufferContainer.vertexComponents;
							GL11.glDrawArrays(GL11.GL_TRIANGLES,
									start,
									count);
						}
					}
				}
			}

		} else {
			if (blended == BOTH) {
				GL11.glDrawArrays(GL11.GL_QUADS,
						(currentVBOCell.startPositionByte / ByteUtil.SIZEOF_FLOAT) / CubeMeshBufferContainer.vertexComponents,
						(currentVBOCell.lengthInBytes / ByteUtil.SIZEOF_FLOAT) / CubeMeshBufferContainer.vertexComponents);

			} else if (blended == OPAQUE) {
				GL11.glDrawArrays(GL11.GL_QUADS,
						(currentVBOCell.startPositionByte / ByteUtil.SIZEOF_FLOAT) / CubeMeshBufferContainer.vertexComponents,
						(currentVBOCell.blendedFloatStartPos) / CubeMeshBufferContainer.vertexComponents);
			} else if (blended == BLENDED) {
				GL11.glDrawArrays(GL11.GL_QUADS,
						(currentVBOCell.startPositionByte / ByteUtil.SIZEOF_FLOAT) / CubeMeshBufferContainer.vertexComponents + (currentVBOCell.blendedFloatStartPos) / CubeMeshBufferContainer.vertexComponents,
						(currentVBOCell.lengthInBytes / ByteUtil.SIZEOF_FLOAT - currentVBOCell.blendedFloatStartPos) / CubeMeshBufferContainer.vertexComponents
				);
			}
		}

	}

	//	private void drawSeperateOpaque(){
	//
	//		GL11.glVertexPointer(CubeMeshBufferContainer.vertexComponents, GL11.GL_FLOAT, 0, 0);
	//		GL11.glDrawArrays(GL11.GL_QUADS, 0, blendBufferPos/CubeMeshBufferContainer.vertexComponents);
	//
	//	}
	//	private void drawSeperateBlend(){
	//
	//		GL11.glVertexPointer(CubeMeshBufferContainer.vertexComponents, GL11.GL_FLOAT, 0, blendBufferPos * 4);
	//		GL11.glDrawArrays(GL11.GL_QUADS, 0, (endBufferPos-blendBufferPos)/CubeMeshBufferContainer.vertexComponents);
	//	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	@Override
	public void prepare(ManagedMemoryChunk buffer) {

		initialized = true;
	}

	@Override
	public void released() {
		if (currentVBOCell != null) {
			currentVBOCell.released();
			currentVBOCell = null;
		}
	}

}
