package org.schema.schine.graphicsengine.shader.bloom;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.system.MemoryUtil;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.FrameBufferObjects;
import org.schema.schine.graphicsengine.core.GLException;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;

public class BloomPass3 implements Shaderable {

	private FrameBufferObjects fbo;
	private FrameBufferObjects firstBlur;
	private FrameBufferObjects secondBlur;
	private float[] gaussianWeights;
	private AbstractGaussianBloomShader gShader;
	private FloatBuffer gaussBuffer = MemoryUtil.memAllocFloat(10);

	public BloomPass3(FrameBufferObjects fbo, FrameBufferObjects firstBlur, FrameBufferObjects secondBlur, AbstractGaussianBloomShader gaussianBloomShader) throws GLException {
		super();
		this.fbo = fbo;

		this.firstBlur = firstBlur;
		this.secondBlur = secondBlur;
		this.gaussianWeights = AbstractGaussianBloomShader.calculateGaussianWeightsNew(9,1.0);
		gaussBuffer.rewind();
		for (int i = 9; i >= 0; i--) {
			gaussBuffer.put(gaussianWeights[i]);
		}
		gaussBuffer.rewind();
		this.gShader = gaussianBloomShader;
	}

	public void applyWeights() {
		for (int i = 0; i < gaussBuffer.limit(); i++) {
			gaussBuffer.put(i, gaussBuffer.get(i) * gShader.weightMult);
		}
	}

	public void draw(int ocMode) {

		Shader shader = ShaderLibrary.bloomShaderPass3;
		shader.setShaderInterface(this);

		secondBlur.enable();
		GL11.glClearColor(0,0,0,0);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		fbo.draw(shader, ocMode);
		secondBlur.disable();

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

		
		GlUtil.updateShaderMat4(shader, "MVP", AbstractGaussianBloomShader.mvpBuffer, false);
		
		GlUtil.updateShaderFloat(shader, "Height", firstBlur.getHeight());
		gaussBuffer.rewind();
		GlUtil.updateShaderFloats1(shader, "Weight", gaussBuffer);

		//		GlUtil.updateShaderFloat(shader, "LumThresh", 1);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, firstBlur.getTexture());
		GlUtil.updateShaderInt(shader, "BlurTex", 1);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);

		//		GlUtil.glActiveTexture(GL13.GL_TEXTURE2);
		//		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, gShader.silhouetteTextureId);
		//		GlUtil.updateShaderInt(shader, "SilhouetteTex", 2);
		//		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);

	}
	

}
