package org.schema.schine.graphicsengine.shader.bloom;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.system.MemoryUtil;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.FrameBufferObjects;
import org.schema.schine.graphicsengine.core.GLException;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;

public class BloomPass4 implements Shaderable {

	private FrameBufferObjects fbo;
	private FrameBufferObjects firstBlur;
	private FrameBufferObjects secondBlur;
	private float[] gaussianWeights;
	private AbstractGaussianBloomShader gShader;
	private FloatBuffer gaussBuffer = MemoryUtil.memAllocFloat(10);

	public BloomPass4(FrameBufferObjects fbo, FrameBufferObjects firstBlur, FrameBufferObjects secondBlur, AbstractGaussianBloomShader gaussianBloomShader) throws GLException {
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

	public void cleanUp() {

	}

	public void draw(int ocMode) {

		Shader shader = ShaderLibrary.bloomShaderPass4;
		shader.setShaderInterface(this);
		fbo.draw(shader, ocMode);
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

		//		uniform mat4 ModelViewMatrix;
		//
		//		uniform mat3 NormalMatrix;
		//
		//		uniform mat4 ProjectionMatrix;
		//
		//		uniform mat4 MVP;

		//		GlUtil.updateShaderMat4(shader, "ModelViewMatrix", modelBuffer, false);
		//		GlUtil.updateShaderMat4(shader, "ProjectionMatrix", projBuffer, false);
		GlUtil.updateShaderFloat(shader, "gammaInv", 1.0f/(EngineSettings.G_GAMMA.getFloat()));
		GlUtil.updateShaderMat4(shader, "MVP", AbstractGaussianBloomShader.mvpBuffer, false);
		//		GlUtil.updateShaderMat3(shader, "NormalMatrix", normalBuffer, false);

		//		uniform sampler2D RenderTex;
		//		uniform sampler2D BlurTex;
		//		uniform int Width;
		//		uniform int Height;
		//		uniform float LumThresh; // Luminance threshold

		GlUtil.updateShaderFloat(shader, "Width", firstBlur.getWidth());
		//		GlUtil.updateShaderFloat(shader, "Width", GLFrame.getWidth());

		//		GlUtil.updateShaderFloat(shader, "LumThresh", 1);

		gaussBuffer.rewind();
		GlUtil.updateShaderFloats1(shader, "Weight", gaussBuffer);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, fbo.getTexture());
		GlUtil.updateShaderInt(shader, "RenderTex", 0);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, secondBlur.getTexture());
		GlUtil.updateShaderInt(shader, "BlurTex", 1);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);

		//		GlUtil.glActiveTexture(GL13.GL_TEXTURE2);
		//		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, gShader.silhouetteTextureId);
		//		GlUtil.updateShaderInt(shader, "SilhouetteTex", 2);
		//		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);

	}
	//	private static FloatBuffer modelViewBuffer = MemoryUtil.memAllocFloat(16);
	//	private static FloatBuffer projectionBuffer = MemoryUtil.memAllocFloat(16);
	//	private static IntBuffer viewPortBuffer = MemoryUtil.memAllocInt(16);
	//    public Vector3f project( float x, float y, float z)
	//    {
	//
	//    	modelViewBuffer.rewind();
	//    	projectionBuffer.rewind();
	//    	viewPortBuffer.rewind();
	//
	//
	//
	//		GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewPortBuffer);
	//
	////		int[] viewport = viewPortBuffer.array();
	////		double modelview[] = modelViewBuffer.array();
	////		double projection[] = projectionBuffer.array();
	//
	//		viewPortBuffer.put(2, (fbo.getWidth()));
	//		viewPortBuffer.put(3, (fbo.getHeight()));
	//		FloatBuffer coord = MemoryUtil.memAllocFloat(3);
	//        // lwjgl 2.0 altered params for GLU funcs
	//        GLU.gluProject( x, y, z, modelViewBuffer, projectionBuffer, viewPortBuffer, coord);
	//        return new Vector3f(coord.get(0)/viewPortBuffer.get(2), coord.get(1)/viewPortBuffer.get(3), coord.get(2));
	//    }

}
