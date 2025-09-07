package org.schema.schine.graphicsengine.shader.bloom;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.FrameBufferObjects;
import org.schema.schine.graphicsengine.core.GLException;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;

public class BloomPass2 implements Shaderable {

	private FrameBufferObjects fbo;
	private FrameBufferObjects firstBlur;
	private GaussianBloomShader gShader;

	public BloomPass2(FrameBufferObjects fbo, FrameBufferObjects firstBlur, GaussianBloomShader gShader) throws GLException {
		super();
		this.fbo = fbo;

		this.firstBlur = firstBlur;
		this.gShader = gShader;
	}

	public void draw(int ocMode) {

		Shader shader = ShaderLibrary.bloomShaderPass2;
		shader.setShaderInterface(this);

		firstBlur.enable();
		GL11.glClearColor(0,0,0,0);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		fbo.draw(shader, ocMode);
		firstBlur.disable();

	}

	@Override
	public void onExit() {
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
	}

	@Override
	public void updateShader(DrawableScene scene) {

	}

	@Override
	public void updateShaderParameters(Shader shader) {

		gShader.calculateMatrices();

		GlUtil.updateShaderMat4(shader, "MVP", AbstractGaussianBloomShader.mvpBuffer, false);

		GlUtil.updateShaderFloat(shader, "LumThresh", 1.0f - EngineSettings.F_BLOOM_INTENSITY.getFloat());

		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, fbo.getTexture());
		GlUtil.updateShaderInt(shader, "RenderTex", 0);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, gShader.silhouetteTextureId);
		GlUtil.updateShaderInt(shader, "SilhouetteTex", 1);
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);

	}

	public void drawFbo(int ocMode) {
		firstBlur.draw(ocMode);
	}

	public void cleanUp() {
	}

}
