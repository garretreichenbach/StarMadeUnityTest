package org.schema.schine.graphicsengine.shader;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.FrameBufferObjects;
import org.schema.schine.graphicsengine.core.GLU;
import org.schema.schine.graphicsengine.core.GlUtil;

public class BloomShader implements Shaderable {

	private static FloatBuffer modelViewBuffer = MemoryUtil.memAllocFloat(16);
	private static FloatBuffer projectionBuffer = MemoryUtil.memAllocFloat(16);
	private static IntBuffer viewPortBuffer = MemoryUtil.memAllocInt(16);
	private FrameBufferObjects fbo;
	private float exposure = 0.0044f;
	private float decay = 1.0f;
	private float density = 0.84f;
	private float weight = 6.65f;

	public BloomShader(FrameBufferObjects fbo) {
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

		GlUtil.updateShaderInt(shader, "firstPass", 0);

		//		Vector4f l = AbstractScene.mainLight.getPosition().getVector4f();
		Vector4f l = new Vector4f(0, 0, 0, 0);
		Vector3f project = project(l.x, l.y, l.z);
		//for an awesome effect of speed, put light position always in the middle

		//		System.err.println(project);
		GlUtil.updateShaderVector2f(shader, "lightPositionOnScreen", project.x, project.y);
		GlUtil.updateShaderFloat(shader, "exposure", exposure);
		GlUtil.updateShaderFloat(shader, "weight", weight);
		GlUtil.updateShaderFloat(shader, "density", density);
		GlUtil.updateShaderFloat(shader, "decay", decay);

	}

	public Vector3f project(float x, float y, float z) {

		modelViewBuffer.rewind();
		projectionBuffer.rewind();
		viewPortBuffer.rewind();

		Matrix4fTools.store(Controller.modelviewMatrix, modelViewBuffer);
		Matrix4fTools.store(Controller.projectionMatrix, projectionBuffer);

		GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewPortBuffer);

		//		int[] viewport = viewPortBuffer.array();
		//		double modelview[] = modelViewBuffer.array();
		//		double projection[] = projectionBuffer.array();

		viewPortBuffer.put(2, (fbo.getWidth()));
		viewPortBuffer.put(3, (fbo.getHeight()));
		FloatBuffer coord = MemoryUtil.memAllocFloat(3);
		// lwjgl 2.0 altered params for GLU funcs
		GLU.gluProject(x, y, z, modelViewBuffer, projectionBuffer, viewPortBuffer, coord);
		return new Vector3f(coord.get(0) / viewPortBuffer.get(2), coord.get(1) / viewPortBuffer.get(3), coord.get(2));
	}

}
