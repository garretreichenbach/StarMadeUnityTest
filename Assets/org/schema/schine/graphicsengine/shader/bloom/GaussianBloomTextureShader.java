package org.schema.schine.graphicsengine.shader.bloom;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;

import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.FrameBufferObjects;
import org.schema.schine.graphicsengine.core.GLException;
import org.schema.schine.graphicsengine.core.GLFrame;

public class GaussianBloomTextureShader extends AbstractGaussianBloomShader {

	//	private static float gauss(float i, float sigma2, float center){
	//		float r = center - i;
	//		return (float) Math.exp(-0.5 * (r * r) / sigma2);
	//	}
	//	private static float[] makeGaussKernel1d(double sigma) {
	//		// make 1D Gauss filter kernel large enough
	//		// to avoid truncation effects (too small in ImageJ!)
	//		int center = (int) (3.0 * sigma);
	//		float[] kernel = new float[2 * center + 1]; // odd size
	//		double sigma2 = sigma * sigma;
	//
	//		for (int i = 0; i < kernel.length; i++) {
	//			double r = center - i;
	//			kernel[i] = (float) Math.exp( (r * r) / sigma2);
	//		}
	//
	//		return kernel;
	//	}
	private BloomPass2 bloomPass2;
	private BloomPass3 bloomPass3;

	private BloomPass4 bloomPass4;

	public GaussianBloomTextureShader(FrameBufferObjects fbo, FrameBufferObjects firstBlur) throws GLException {
		super();
		this.fbo = fbo;

		this.firstBlur = new FrameBufferObjects("GaussTexFirst", GLFrame.getWidth(), GLFrame.getHeight());
		this.firstBlur.initialize();
		this.secondBlur = new FrameBufferObjects("GaussTexSecond", GLFrame.getWidth(), GLFrame.getHeight());
		this.secondBlur.initialize();

		bloomPass3 = new BloomPass3(fbo, firstBlur, secondBlur, this);
		bloomPass4 = new BloomPass4(fbo, firstBlur, secondBlur, this);
	}

	@Override
	void calculateMatrices() {

		Matrix4f pmat = new Matrix4f(Controller.projectionMatrix);
		Matrix4f mmat = new Matrix4f(Controller.modelviewMatrix);

		Matrix4f mvp = new Matrix4f();

		Matrix4fTools.mul(pmat, mmat, mvp);
		mvpBuffer.rewind();
		Matrix4fTools.store(mvp, mvpBuffer);
		mvpBuffer.rewind();

		Matrix3f nMat = new Matrix3f();
		nMat.m00 = mmat.m00;
		nMat.m10 = mmat.m10;
		nMat.m20 = mmat.m20;
		nMat.m01 = mmat.m01;
		nMat.m11 = mmat.m11;
		nMat.m21 = mmat.m21;
		nMat.m02 = mmat.m02;
		nMat.m12 = mmat.m12;
		nMat.m22 = mmat.m22;
		nMat.invert();

		normalBuffer.rewind();
		Matrix4fTools.store(nMat, normalBuffer);
		normalBuffer.rewind();
	}

	public void draw(int ocMode) {
		calculateMatrices();
		bloomPass2.draw(ocMode);
		bloomPass3.draw(ocMode);
		bloomPass4.draw(ocMode);

	}

}
