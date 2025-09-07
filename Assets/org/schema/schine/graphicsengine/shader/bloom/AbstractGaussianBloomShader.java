package org.schema.schine.graphicsengine.shader.bloom;

import java.nio.FloatBuffer;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;

import org.lwjgl.system.MemoryUtil;
import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.FrameBufferObjects;

public class AbstractGaussianBloomShader {

	protected static FloatBuffer normalBuffer = MemoryUtil.memAllocFloat(9);
	static FloatBuffer mvpBuffer = MemoryUtil.memAllocFloat(16);
	protected FrameBufferObjects firstBlur;
	protected FrameBufferObjects secondBlur;
	protected FrameBufferObjects fbo;
	protected int silhouetteTextureId;
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
	protected float weightMult;

	public static float[] calculateGaussianWeights(int weightCount) {
		float weights[] = new float[weightCount], sum, sigma2 = 64.0f;
		// Compute and sum the weights
		weights[0] = gauss(0, sigma2, 0); // The 1-D Gaussian function
		sum = weights[0];
		for (int i = 1; i < weightCount; i++) {
			weights[i] = gauss(i, sigma2, 0);
			sum += 2 * weights[i];
		}

		float one = 0;
		// Normalize the weights
		for (int i = 0; i < weightCount; i++) {
			weights[i] = weights[i] / sum;
			System.err.println("WEIGHT: " + weights[i]);
			one += weights[i];
		}
		System.err.println("SUM: (should be 1) " + one);

		return weights;
	}

	public static float[] calculateGaussianWeightsNew(int radius, double radiusModifier) {
		float[] kernel = new float[radius * 2 + 1];
		double twoRadiusSquaredRecip = 1.0 / (2.0 * radius * radius);
		double sqrtTwoPiTimesRadiusRecip = 1.0 / (Math.sqrt(2.0 * Math.PI) * radius);

		int r = -radius;
		double sum = 0;
		for (int i = 0; i < kernel.length; i++) {
			double x = r * radiusModifier;
			x *= x;
			kernel[i] =
					(float) (sqrtTwoPiTimesRadiusRecip * Math.exp(-x * twoRadiusSquaredRecip));
			r++;
			sum += kernel[i];
		}

		double div = sum;
		float hight = 0;
		for (int i = 0; i < kernel.length; i++) {
			kernel[i] /= div;
			hight = Math.max(hight, kernel[i]);
		}
		
//		for (int i = 0; i < kernel.length; i++) {
//			kernel[i] = kernel[i] / hight;
//		}
		
		return kernel;
	}

	private static float gauss(float i, float sigma2, float center) {
		float r = center - i;
		return (float) Math.exp(-0.5 * (r * r) / sigma2);
	}

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

	public void setSilhouetteTexture(int texture) {
		this.silhouetteTextureId = texture;

	}
}
