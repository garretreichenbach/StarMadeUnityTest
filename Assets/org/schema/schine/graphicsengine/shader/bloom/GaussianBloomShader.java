package org.schema.schine.graphicsengine.shader.bloom;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.schema.schine.graphicsengine.OculusVrHelper;
import org.schema.schine.graphicsengine.core.FrameBufferObjects;
import org.schema.schine.graphicsengine.core.GLException;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.GraphicsContext;
import org.schema.schine.graphicsengine.core.ScreenChangeCallback;

public class GaussianBloomShader extends AbstractGaussianBloomShader implements ScreenChangeCallback{

	private BloomPass2 bloomPass2;
	private BloomPass3 bloomPass3;
	private BloomPass4 bloomPass4;
	private boolean screenChanged;
	public GaussianBloomShader(FrameBufferObjects fbo) {
		super();
		this.fbo = fbo;

		try {
			createFrameBuffers();
		} catch (GLException e) {
			e.printStackTrace();
		}
		GraphicsContext.current.registerScreenChangeCallback(this);
	}

	public void applyWeightMult(float mult) {
		this.weightMult = mult;
		bloomPass3.applyWeights();
		bloomPass4.applyWeights();
	}

	private void createFrameBuffers() throws GLException {
		if (this.firstBlur != null) {
			this.firstBlur.cleanUp();
		}
		if (this.secondBlur != null) {
			this.secondBlur.cleanUp();
		}
		this.firstBlur = new FrameBufferObjects("GaussFirst", GLFrame.getWidth() / 2, GLFrame.getHeight() / 2);
//		this.firstBlur.setReuseDepthBuffer(depthRenderBufferId);
		this.firstBlur.initialize();
		this.secondBlur = new FrameBufferObjects("GaussSecond", GLFrame.getWidth() / 2, GLFrame.getHeight() / 2);
//		this.secondBlur.setReuseDepthBuffer(depthRenderBufferId);
		this.secondBlur.initialize();
		bloomPass2 = new BloomPass2(fbo, firstBlur, this);
		bloomPass3 = new BloomPass3(fbo, firstBlur, secondBlur, this);
		bloomPass4 = new BloomPass4(fbo, firstBlur, secondBlur, this);
	}
	public void initialize(int depthRenderBufferId) {
	}
	public void draw(int ocMode) {
		
		if (screenChanged) {
			try {
				createFrameBuffers();
			} catch (GLException e) {
				e.printStackTrace();
				GLFrame.processErrorDialogException(e, null);
			}
			screenChanged = false;
		}
		calculateMatrices();
		bloomPass2.draw(ocMode);
		bloomPass3.draw(ocMode);
		bloomPass4.draw(ocMode);

		GL20.glUseProgram(0);
	}

	public void draw(FrameBufferObjects fbo, int ocMode) {
		calculateMatrices();
		bloomPass2.draw(OculusVrHelper.OCCULUS_NONE);
		bloomPass3.draw(OculusVrHelper.OCCULUS_NONE);
		fbo.enable();
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		bloomPass4.draw(ocMode);
//		bloomPass2.drawFbo(ocMode);
		GlUtil.glDisable(GL11.GL_BLEND);
		fbo.disable();
	}

	public void cleanUp() {
		if(firstBlur != null){
			firstBlur.cleanUp();
		}
		if(secondBlur != null){
			secondBlur.cleanUp();
		}
		GraphicsContext.current.unregisterScreenChangeCallback(this);
	}

	@Override
	public void onWindowSizeChanged(int width, int height) {
		screenChanged = true;
	}

	

}
