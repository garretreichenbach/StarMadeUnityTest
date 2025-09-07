package org.schema.game.client.view.shader;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.schema.game.client.view.GameResourceLoader;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.Shaderable;

public class JumpOverlayShader implements Shaderable {

	public Shader s = null;//ShaderLibrary.shieldShader;
	//	private Vector3f[] dirs = new Vector3f[MAX_NUM];

	public float minAlpha = 0.0f;
	public float m_time = 0.0f;

	public JumpOverlayShader() {
	}

	@Override
	public void onExit() {
		GlUtil.glActiveTexture(GL13.GL_TEXTURE2);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	@Override
	public void updateShader(DrawableScene scene) {

	}

	@Override
	public void updateShaderParameters(Shader shader) {

		if (shader == null) {
			throw new NullPointerException("Jump Shader NULL; Jump Drawing enabled: " + EngineSettings.G_DRAW_SHIELDS.isOn());
		}
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, Controller.getResLoader().getSprite("shield_tex").getMaterial().getTexture().getTextureId());
		GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, GameResourceLoader.effectTextures[0].getTextureId());
		GlUtil.glActiveTexture(GL13.GL_TEXTURE2);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, GameResourceLoader.effectTextures[1].getTextureId());
		if (shader.recompiled) {

			GlUtil.updateShaderInt(shader, "m_ShieldTex", 0);

			GlUtil.updateShaderInt(shader, "m_Distortion", 1);

			GlUtil.updateShaderInt(shader, "m_Noise", 2);

			GlUtil.updateShaderCubeNormalsBiNormalsAndTangentsBoolean(shader);
			
			FloatBuffer fb = GlUtil.getDynamicByteBuffer(6 * 3 * 4, 0)
					.asFloatBuffer();
			fb.rewind();
			for(int i = 0; i < CubeMeshQuadsShader13.quadPosMark.length; i++){
				fb.put(CubeMeshQuadsShader13.quadPosMark[i].x);
				fb.put(CubeMeshQuadsShader13.quadPosMark[i].y);
				fb.put(CubeMeshQuadsShader13.quadPosMark[i].z);
			}

			fb.rewind();
			GlUtil.updateShaderFloats3(shader, "quadPosMark", fb);
		}

		GlUtil.updateShaderFloat(shader, "m_MinAlpha", minAlpha);

		GlUtil.updateShaderFloat(shader, "m_Time", m_time);
		
		shader.recompiled = false;

	}

}
