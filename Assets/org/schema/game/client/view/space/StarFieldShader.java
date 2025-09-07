package org.schema.game.client.view.space;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.Shaderable;
import org.schema.schine.graphicsengine.util.timer.SinusTimerUtil;

public class StarFieldShader implements Shaderable {

	static final int[] texels = new int[]{
			255, 253, 250, 246, 241, 220, 210, 200,
			180, 170, 160, 135, 120, 110, 90, 85,
			70, 56, 43, 32, 24, 18, 14, 11,
			8, 6, 4, 3, 2, 1, 0, 0
	};
	private static FloatBuffer projBuffer = MemoryUtil.memAllocFloat(16);
	private static FloatBuffer projBufferLast = MemoryUtil.memAllocFloat(16);
	private static FloatBuffer modelBuffer = MemoryUtil.memAllocFloat(16);
	private static FloatBuffer modelBufferLast = MemoryUtil.memAllocFloat(16);
	int frames = 0;
	int framesMax = 30;
	private SinusTimerUtil timerUtil = new SinusTimerUtil(20);

	public StarFieldShader() {
		createAntiAliasingTexture();
	}

	private void createAntiAliasingTexture() {

		ByteBuffer b = MemoryUtil.memAlloc(texels.length * ByteUtil.SIZEOF_INT);

		for (int i = 0; i < texels.length; i++) {
			b.putInt(texels[i]);
		}
		b.flip();
		//		texture = TextureLoader.get1DTexture(texels.length, b);
	}

	@Override
	public void onExit() {
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

	}

	@Override
	public void updateShader(DrawableScene scene) {

	}

	@Override
	public void updateShaderParameters(Shader shader) {
		float size = 6.8f + timerUtil.getTime() * 0.6f;
		if (EngineSettings.F_BLOOM.isOn()) {
			size *= 2;
		}
		GlUtil.updateShaderFloat(shader, "Size2", size);

		if (frames <= 0) {
			modelBufferLast.rewind();
			modelBuffer.rewind();
			modelBufferLast.put(modelBuffer);
			modelBufferLast.rewind();
		}
		frames++;
		if (frames >= framesMax) {
			frames = 0;
		}

		projBufferLast.rewind();
		projBuffer.rewind();
		projBufferLast.put(projBuffer);
		projBufferLast.rewind();

		projBuffer.rewind();
		Matrix4fTools.store(Controller.projectionMatrix, projBuffer);
		projBuffer.rewind();

		modelBuffer.rewind();
		Matrix4fTools.store(Controller.modelviewMatrix, modelBuffer);
		modelBuffer.rewind();

		GlUtil.updateShaderMat4(shader, "ProjectionMatrix", projBuffer, false);
		GlUtil.updateShaderMat4(shader, "ModelViewMatrix", modelBuffer, false);

		//		location = GL20.glGetUniformLocation(shader, "LastModelViewMatrix");
		//		if( location >= 0 )
		//		{
		//			GL20.glUniformMatrix4(location, false, modelBufferLast);
		//		}else{
		//			System.err.println("LastModelViewMatrix"+"! handler not found ");
		//		}
		Vector3f cPos = Controller.getCamera().getPos();

		GlUtil.updateShaderVector3f(shader, "CamPosition", cPos);

		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, Controller.getResLoader().getSprite("starSprite").getMaterial().getTexture().getTextureId());
		GlUtil.updateShaderInt(shader, "starTex", 0);
	}

	public void update(Timer timer) {
		timerUtil.update(timer);
	}

}
