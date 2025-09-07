package org.schema.game.client.view.shader;

import java.nio.FloatBuffer;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.schema.common.TimeStatistics;
import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.game.client.view.GameResourceLoader;
import org.schema.game.client.view.MainGameGraphics;
import org.schema.game.client.view.cubes.CubeMeshBufferContainer;
import org.schema.game.client.view.effects.DepthBufferScene;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.Shaderable;
import org.schema.schine.graphicsengine.shader.ShadowParams;

public class CubeMeshQuadsShader13 implements Shaderable {
	private static final float animationDelay = 0.5f;
	public CubeTexQuality quality = CubeTexQuality.SELECTED;
	int i = 0;
	private int animationFrame;
	private float time;
	private boolean allLight;
	private boolean bound = false;
	public ShadowParams shadowParams;
	private float uTime;

	public static void unbindTextures() {
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE2);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE3);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE4);
		GlUtil.glDisable(GL12.GL_TEXTURE_3D);
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
	}

	public static void uploadMVP(Shader shader) {
		FloatBuffer fbMat = GlUtil.getDynamicByteBuffer(16 * 4, 0)
				.asFloatBuffer();
		Matrix4f m = new Matrix4f();
		Matrix4fTools.mul(Controller.modelviewMatrix,
				Controller.projectionMatrix, m);
		// m.invert();
		fbMat.rewind();
		Matrix4fTools.store(m, fbMat);
		fbMat.rewind();

		GlUtil.updateShaderMat4(shader, "v_inv", fbMat, false);
	}
	public static String[] nTexStrings = new String[32];
	public static String[] nNormalStrings = new String[32];
	static{
		for(int i = 0; i < nTexStrings.length; i++){
			nTexStrings[i] = "mainTex"+i;
			nNormalStrings[i] = "normalTex"+i;
		}
	}
	public static void bindtextures(boolean wasRecompiled, Shader shader, CubeTexQuality quality) {
		if (wasRecompiled) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);

		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D,
				GameResourceLoader.getOverlayTextures(quality));
		GlUtil.updateShaderInt(shader, "overlayTex", 0);
		if (wasRecompiled) {
			GlUtil.printGlErrorCritical();
		}
		int texIndex = 1;
		for (int i = 0; i < 8; i++) {
			if (GameResourceLoader.cubeTextures[i] != null) {
				GlUtil.glActiveTexture(GL13.GL_TEXTURE0 + texIndex);
				GlUtil.glBindTexture(GL11.GL_TEXTURE_2D,
						GameResourceLoader.getCubeTexture(i, quality));
				GlUtil.updateShaderInt(shader, nTexStrings[i], texIndex);
				texIndex++;
			}
		}

		if (wasRecompiled) {
			GlUtil.printGlErrorCritical();
		}
		if (EngineSettings.G_NORMAL_MAPPING.isOn()) {
			for (int i = 0; i < 8; i++) {
				if (GameResourceLoader.cubeNormalTextures != null && GameResourceLoader.cubeNormalTextures[i] != null) {
					GlUtil.glActiveTexture(GL13.GL_TEXTURE0 + texIndex);
					GlUtil.glBindTexture(GL11.GL_TEXTURE_2D,
							GameResourceLoader.getCubeNormalTexture(i, quality));
					GlUtil.updateShaderInt(shader, nNormalStrings[i], texIndex);
					texIndex++;
				}
			}
		}
		if (wasRecompiled) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		if (wasRecompiled) {
			GlUtil.printGlErrorCritical();
		}
	}

	@Override
	public void onExit() {

		unbindTextures();

	}

	@Override
	public void updateShader(DrawableScene scene) {

	}
	public static final Vector3f[] quadPosMark = new Vector3f[]{
		new Vector3f(2,4,0),
		new Vector3f(2,4,0),
		new Vector3f(4,0,2),
		new Vector3f(4,0,2),
		new Vector3f(0,4,2),
		new Vector3f(0,4,2),
	};
	@Override
	public void updateShaderParameters(Shader shader) {
		if (!bound) {
			TimeStatistics.reset("Shader load " + i);

			TimeStatistics.set("Shader load " + i);
			i = (i + 1) % 2;
			bound = true;
		}
//		uploadMVP(shader);
		// GlUtil.updateShaderBoolean(shader, "debugNormalMap",
		// !Keyboard.isKeyDown(GLFW.GLFW_KEY_SPACE));
		boolean wasRecompiled = false;
		// ShadowShader.putDepthMatrix(shader, true);
		if (shader.recompiled) {
			wasRecompiled = true;
			GlUtil.printGlErrorCritical();
			assert(CubeMeshBufferContainer.vertexComponents > 2);
			GlUtil.updateShaderCubeNormalsBiNormalsAndTangentsBoolean(shader);
			
			GlUtil.printGlErrorCritical();
			FloatBuffer fb = GlUtil.getDynamicByteBuffer(6 * 3 * 4, 0)
					.asFloatBuffer();
			fb.rewind();
			for(int i = 0; i < quadPosMark.length; i++){
				fb.put(quadPosMark[i].x);
				fb.put(quadPosMark[i].y);
				fb.put(quadPosMark[i].z);
			}
			

			fb.rewind();
			GlUtil.updateShaderFloats3(shader, "quadPosMark", fb);
			GlUtil.printGlErrorCritical();

			// putTexOrder(shader);

			shader.recompiled = false;
			GlUtil.printGlErrorCritical();

		}

		GlUtil.updateShaderFloat(shader, "zNear", DepthBufferScene.getNearPlane());
		GlUtil.updateShaderFloat(shader, "zFar", DepthBufferScene.getFarPlane());
		
		GlUtil.updateShaderVector3f(shader, "viewPos", Controller.getCamera().getPos());
		GlUtil.updateShaderVector3f(shader, "lightPos", MainGameGraphics.mainLight.getPos());
		
		GlUtil.updateShaderInt(shader, "animationTime", animationFrame);

		GlUtil.updateShaderFloat(shader, "lodThreshold", EngineSettings.LOD_DISTANCE_IN_THRESHOLD.getFloat());
		
		GlUtil.updateShaderFloat(shader, "uTime", uTime);

		GlUtil.updateShaderInt(shader, "spotCount", MainGameGraphics.spotLights.size());

		if (wasRecompiled) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.updateShaderFloat(shader, "extraAlpha", 1);
		GlUtil.updateShaderInt(shader, "allLight", allLight ? 1 : 0);

		// GlUtil.updateShaderFloat(shader, "density",
		// 1.5f/Controller.vis.getVisLen());

		if (wasRecompiled) {
			GlUtil.printGlErrorCritical();
		}
		bindtextures(wasRecompiled, shader, quality);

		if (shadowParams != null) {
			shadowParams.execute(shader);
		}
	}

	public void update(Timer timer) {
		uTime += timer.getDelta();
		time += timer.getDelta();
		if (time > animationDelay) {
			time -= animationDelay;
			animationFrame = (animationFrame + 1) % 4;
		}
	}

	public void setShadow(ShadowParams shadowParams) {
		this.shadowParams = shadowParams;
	}

	/**
	 * @return the allLight
	 */
	public boolean isAllLight() {
		return allLight;
	}

	/**
	 * @param allLight the allLight to set
	 */
	public void setAllLight(boolean allLight) {
		this.allLight = allLight;
	}

	public enum CubeTexQuality {
		LOW,
		SELECTED,
	}

}
