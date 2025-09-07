package org.schema.schine.graphicsengine.shader.targetBloom;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.system.MemoryUtil;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.bloom.AbstractGaussianBloomShader;
import org.schema.schine.input.Keyboard;

import java.nio.FloatBuffer;

public class Blurrer implements ScreenChangeCallback {
	private FrameBufferObjects inputScene;

	private final FrameBufferObjects toBloom;

	private FrameBufferObjects vertical;

	private FrameBufferObjects output;

	private BloomLevel vertLevel;

	private BloomLevel horLevel;

	private int ocMode;

	private int verticalRadius = 19;
	private int horizontalRadius = 29;

	private int maxRadius;

	private FrameBufferObjects[] downsamplers;

	private int outputReuseDepthBuffer;

	private boolean screenChanged;

	private static FloatBuffer gaussBuffer;

	public Blurrer(FrameBufferObjects toBloom, int ocMode) {
		this.toBloom = toBloom;
		this.ocMode = ocMode;

		this.vertLevel = new BloomLevel(verticalRadius);
		this.horLevel = new BloomLevel(horizontalRadius);

		this.maxRadius = Math.max(verticalRadius, horizontalRadius);
		if(gaussBuffer == null || gaussBuffer.capacity() != maxRadius) {
			if(gaussBuffer != null) {
				GlUtil.destroyDirectByteBuffer(gaussBuffer);
			}
			gaussBuffer = MemoryUtil.memAllocFloat(maxRadius * 2 + 1);
		}
		GraphicsContext.current.registerScreenChangeCallback(this);
	}

	public void initialize() {
		try {
			createFrameBuffers();
		} catch(GLException e) {
			e.printStackTrace();
		}
	}

	public FrameBufferObjects blur(FrameBufferObjects inputScene) {
		this.inputScene = inputScene;
		if(screenChanged) {
			try {
				createFrameBuffers();
			} catch(GLException e) {
				e.printStackTrace();
				GLFrame.processErrorDialogException(e, null);
			}
			screenChanged = false;
		}
		GlUtil.glDisable(GL11.GL_BLEND);
		try {
			FrameBufferObjects downSampledInput = downsample(toBloom);
			if(downSampledInput != null) downSampledInput = toBloom;
			if(downSampledInput == null) return output;
			verticalPass(downSampledInput);
			horizontalPass();
			if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F10)) return downSampledInput;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return output;
	}

	private FrameBufferObjects downsample(FrameBufferObjects inputScene) {

		FrameBufferObjects from = inputScene;
		if(downsamplers == null) return null;
		for(int i = 0; i < downsamplers.length; i++) {
			downsamplers[i].enable();
			Shader shader;
			if(from == inputScene) {
				shader = ShaderLibrary.downsampler;
			} else {
				shader = ShaderLibrary.downsamplerFirst;
			}
			shader.loadWithoutUpdate();

			GlUtil.glActiveTexture(GL13.GL_TEXTURE3);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, from.getTexture());
			GlUtil.updateShaderInt(shader, "inputTexture", 3);

			if(from == inputScene) {
				GlUtil.glActiveTexture(GL13.GL_TEXTURE4);
				GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, from.getDepthTextureID());
				GlUtil.updateShaderInt(shader, "depthTexture", 4);
			}
			GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
			GlUtil.updateShaderFloat(shader, "inputTextureWidth", from.getWidth());
			GlUtil.updateShaderFloat(shader, "inputTextureHeight", from.getHeight());

			GL11.glClearColor(0, 0, 0, 0);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			from.draw(ocMode);

			shader.unloadWithoutExit();

			downsamplers[i].disable();
			from = downsamplers[i];

			GlUtil.glActiveTexture(GL13.GL_TEXTURE3);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GlUtil.glActiveTexture(GL13.GL_TEXTURE4);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		}

		return from;
	}

	private void verticalPass(FrameBufferObjects inputScene) {
		Shader shader = ShaderLibrary.bloomShader1Vert;
		BloomLevel lvl = vertLevel;

		shader.loadWithoutUpdate();

		GlUtil.updateShaderFloat(shader, "Height", inputScene.getHeight());

		gaussBuffer.clear();

		int weightM = lvl.weights.length / 2 - 1;
		for(int i = weightM; i >= 0; i--) {
			gaussBuffer.put(lvl.weights[i]);
		}

		gaussBuffer.rewind();
		GlUtil.updateShaderFloats1(shader, "Weight", gaussBuffer);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE3);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, inputScene.getTexture());
		GlUtil.updateShaderInt(shader, "BlurTex", 3);
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);

		vertical.enable();

		GL11.glClearColor(0, 0, 0, 0);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		inputScene.draw(shader, ocMode);

		vertical.disable();

		shader.unloadWithoutExit();
	}

	private void horizontalPass() {
		Shader shader = ShaderLibrary.bloomShader2Hor;
		BloomLevel lvl = horLevel;
		FrameBufferObjects inputBlur = vertical;

		shader.loadWithoutUpdate();
		GlUtil.updateShaderFloat(shader, "gammaInv", 1.0f / (EngineSettings.G_GAMMA.getFloat()));

		GlUtil.updateShaderFloat(shader, "Width", vertical.getWidth());

		gaussBuffer.clear();

		int weightM = lvl.weights.length / 2 - 1;
		for(int i = weightM; i >= 0; i--) {
			gaussBuffer.put(lvl.weights[i]);
		}

		gaussBuffer.rewind();
		GlUtil.updateShaderFloats1(shader, "Weight", gaussBuffer);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, inputScene.getTexture());
		GlUtil.updateShaderInt(shader, "RenderTex", 0);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, inputBlur.getTexture());
		GlUtil.updateShaderInt(shader, "BlurTex", 1);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);

		output.enable();

		GL11.glClearColor(0, 0, 0, 0);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		inputScene.draw(shader, ocMode);

		output.disable();

		shader.unloadWithoutExit();
	}

	private class BloomLevel {
		private float[] weights;

		public BloomLevel(int radius) {
			this.weights = AbstractGaussianBloomShader.calculateGaussianWeightsNew(radius, 0.1);
			float hight = 0;

			for(int i = 0; i < weights.length; i++) {
				hight = Math.max(hight, weights[i]);
			}
			for(int i = 0; i < weights.length; i++) {
				weights[i] = weights[i] / (hight * 0.1f);
				weights[i] = weights[i] * 2.0f;
			}
		}
	}

	public void cleanUp() {
		if(this.output != null) {
			this.output.cleanUp();
		}
		if(this.vertical != null) {
			this.vertical.cleanUp();
		}
		if(downsamplers != null) {
			for(int i = 0; i < downsamplers.length; i++) {
				if(downsamplers[i] != null) {
					downsamplers[i].cleanUp();
					downsamplers[i] = null;
				}
			}
		}
		GraphicsContext.current.unregisterScreenChangeCallback(this);
	}

	public void createFrameBuffers() throws GLException {
		cleanUp();
		this.output = new FrameBufferObjects("BlurrerOutput", GLFrame.getWidth(), GLFrame.getHeight());
		this.output.setReuseDepthBuffer(outputReuseDepthBuffer);
		this.output.initialize();

		int downsample = 16;

		int fboNeeded = 0;
		int x = 2;
		while(x < downsample) {
			x *= 2;
			fboNeeded++;
		}

		downsamplers = new FrameBufferObjects[fboNeeded];

		x = 2;
		int i = 0;
		while(x < downsample) {
			downsamplers[i] = new FrameBufferObjects("BlurrerDownsample" + i, GLFrame.getWidth() / x, GLFrame.getHeight() / x);
			downsamplers[i].setWithDepthAttachment(false);
			downsamplers[i].initialize();
			x *= 2;
			i++;
		}
		this.vertical = new FrameBufferObjects("BlurrerVertical", GLFrame.getWidth() / downsample, GLFrame.getHeight() / downsample);
		this.vertical.setWithDepthAttachment(false);
		this.vertical.initialize();

	}

	public FrameBufferObjects getInputScene() {
		return inputScene;
	}

	public void setInputScene(FrameBufferObjects inputScene) {
		this.inputScene = inputScene;
	}

	public void setReuseRenderDepthBuffer(int depthBufferID) {
		this.outputReuseDepthBuffer = depthBufferID;
	}

	@Override
	public void onWindowSizeChanged(int width, int height) {
		this.screenChanged = true;
	}

}
