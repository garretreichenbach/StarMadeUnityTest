package org.schema.game.client.view.cubes.cubedyn;

import java.nio.ByteOrder;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.schema.common.util.ByteUtil;
import org.schema.game.client.view.SegmentDrawer;
import org.schema.game.client.view.cubes.CubeMeshBufferContainer;
import org.schema.game.client.view.cubes.CubeMeshInterface;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.GraphicsContext;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.ShaderLibrary.CubeShaderType;

public class CubeMeshManagerBulkOptimized extends VBOManagerBulkBase{


	public CubeMeshManagerBulkOptimized() {
		super();
		System.err.println("[GRAPHICS] TRY USING VBO MAPPED BUFFER: " + EngineSettings.G_USE_VBO_MAP.isOn());
	}


	

	public CubeMeshInterface getInstance() {
		CubeMeshInterface f = new CubeMeshDynOpt(this);
		return f;
	}

	public void drawMulti(final boolean clearMaked, Shader shader) {
		int segSize = vboSegs.size();
		final boolean multiDraw = EngineSettings.USE_GL_MULTI_DRAWARRAYS.isOn();
		
		
		Shader currentShader = shader;
		Shader original = shader;
		Shader virtual = null;
		if(shader != null && original.optionBits >= 0){
			virtual = ShaderLibrary.getCubeShader(original.optionBits | CubeShaderType.VIRTUAL.bit);
			virtual.setShaderInterface(original.getShaderInterface());
		}
		
		for (int j = 0; j < segSize; j++) {
			VBOSeg seg = vboSegs.get(j);

			boolean glBindBuffer = GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, seg.bufferId);
			if (glBindBuffer) {
				if(GraphicsContext.INTEGER_VERTICES){
					GlUtil.glVertexAttribIPointer(ShaderLibrary.CUBE_SHADER_VERT_INDEX, CubeMeshBufferContainer.vertexComponents, GL11.GL_INT, 0, 0);
				}else{
					GL11.glVertexPointer(CubeMeshBufferContainer.vertexComponents, GL11.GL_FLOAT, 0, 0);
				}
			}
			int markerSize = seg.markers.size();
			for (int i = 0; i < markerSize; i++) {
				DrawMarker mark = seg.markers.get(i);
				if (mark.start.size() > 0) {
					GlUtil.glPushMatrix();

					GlUtil.glMultMatrix(mark.t);
					
					if(virtual != null){
						if((mark.optionBits & DrawMarker.VIRTUAL) == DrawMarker.VIRTUAL){
							if(currentShader != virtual){
								original.unload();
								virtual.load();
								currentShader = virtual;
							}
						}else{
							if(currentShader != original){
								virtual.unload();
								original.load();
								currentShader = original;
							}
						}
					}
					
					if (multiDraw) {
						long t = System.currentTimeMillis();
						IntBuffer multiBufferStarts = GlUtil.getDynamicByteBuffer(mark.start.size() * ByteUtil.SIZEOF_INT, 0).order(ByteOrder.nativeOrder()).asIntBuffer();
						multiBufferStarts.put(mark.start.elements(), 0, mark.start.size());
						multiBufferStarts.flip();

						IntBuffer multiBufferCounts = GlUtil.getDynamicByteBuffer(mark.count.size() * ByteUtil.SIZEOF_INT, 1).order(ByteOrder.nativeOrder()).asIntBuffer();
						multiBufferCounts.put(mark.count.elements(), 0, mark.count.size());
						multiBufferCounts.flip();

						long taken = System.currentTimeMillis() - t;
						if(taken > SegmentDrawer.WARNING_MARGIN) {
							System.err.println("WARNING: MultiDraw Buffer Time: "+taken+"ms");
						}
						
						t = System.currentTimeMillis();
						GL14.glMultiDrawArrays(GL11.GL_TRIANGLES, multiBufferStarts, multiBufferCounts);
						taken = System.currentTimeMillis() - t;
						if(taken > SegmentDrawer.WARNING_MARGIN) {
							System.err.println("WARNING: MultiDraw Draw Time: "+multiBufferStarts+"; "+multiBufferCounts+" "+taken+"ms");
						}
					} else {
						int size = mark.start.size();
						for (int k = 0; k < size; k++) {
							int start = mark.start.getInt(k);
							int count = mark.count.getInt(k);
							GL11.glDrawArrays(GL11.GL_TRIANGLES, start, count);
						}
					}

					GlUtil.glPopMatrix();
				}
				if (clearMaked) {
					releaseMarker(mark);
				}
			}
			if (clearMaked) {
				seg.markers.clear();
			}

		}

	}

}
