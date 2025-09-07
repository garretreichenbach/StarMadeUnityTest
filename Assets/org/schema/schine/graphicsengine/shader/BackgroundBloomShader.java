package org.schema.schine.graphicsengine.shader;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.FrameBufferObjects;
import org.schema.schine.graphicsengine.core.GlUtil;

public class BackgroundBloomShader implements Shaderable {

	private FrameBufferObjects fbo;
	
	public BackgroundBloomShader(FrameBufferObjects fbo) {
		super();
		this.fbo = fbo;
	}

	@Override
	public void onExit() {
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	@Override
	public void updateShader(DrawableScene scene) {

	}

	@Override
	public void updateShaderParameters(Shader shader) {
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		//		System.err.println("base "+baseMapId);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, fbo.getTexture());

		GlUtil.updateShaderInt(shader, "bgl_RenderedTexture", 0);
		
		//		Vector4f l = AbstractScene.mainLight.getPosition().getVector4f();
		//for an awesome effect of speed, put light position always in the middle

		//		System.err.println(project);

	}

}
