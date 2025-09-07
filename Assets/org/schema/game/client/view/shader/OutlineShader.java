package org.schema.game.client.view.shader;

import java.nio.FloatBuffer;

import javax.vecmath.Vector2f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.system.MemoryUtil;
import org.schema.schine.graphicsengine.OculusVrHelper;
import org.schema.schine.graphicsengine.core.FrameBufferObjects;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;

public class OutlineShader {
	private static FloatBuffer fb = MemoryUtil.memAllocFloat(9*2);
	public static float offsetMult = 0.0007f;
	private static Vector2f[] offsets = new Vector2f[]{
		new Vector2f(1,1),
		new Vector2f(0,1),
		new Vector2f(-1,1),
		new Vector2f(1,0),
		new Vector2f(0,0),
		new Vector2f(-1,0),
		new Vector2f(1,-1),
		new Vector2f(0,-1),
		new Vector2f(-1,-1),
	};
	public void draw(FrameBufferObjects foregroundFbo, float meshForce){
		Shader outlineShader = ShaderLibrary.outlineShader;
		outlineShader.loadWithoutUpdate();
		
		GlUtil.updateShaderFloat(outlineShader, "bgl_MeshForce", meshForce);
		
		GlUtil.glActiveTexture(GL13.GL_TEXTURE4);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, foregroundFbo.getTextureID());
		GlUtil.updateShaderInt(outlineShader, "bgl_RenderedTexture", 4);
		GlUtil.glActiveTexture(GL13.GL_TEXTURE5);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, foregroundFbo.getDepthTextureID());
		GlUtil.updateShaderInt(outlineShader, "bgl_DepthTexture", 5);
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		fb.clear();
		for(int i = 0; i < offsets.length; i++){
			fb.put(offsets[i].x*offsetMult);
			fb.put(offsets[i].y*offsetMult);
		}
		fb.flip();
		GlUtil.updateShaderFloats2(outlineShader, "bgl_TextureCoordinateOffset", fb);
		
		foregroundFbo.draw(OculusVrHelper.OCCULUS_NONE);
		
		GlUtil.glActiveTexture(GL13.GL_TEXTURE4);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GlUtil.glActiveTexture(GL13.GL_TEXTURE5);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GlUtil.updateShaderInt(outlineShader, "bgl_DepthTexture", 5);
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		outlineShader.unloadWithoutExit();
		
		GlUtil.glDisable(GL11.GL_BLEND);
		
		
		
		
	}
}
