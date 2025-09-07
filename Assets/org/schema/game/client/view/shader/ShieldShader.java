package org.schema.game.client.view.shader;

import java.nio.FloatBuffer;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.system.MemoryUtil;
import org.schema.game.client.view.GameResourceLoader;
import org.schema.game.client.view.effects.ShieldDrawerManager;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.Shaderable;

public class ShieldShader implements Shaderable {

	public static final int MAX_NUM = 8;
	private final static FloatBuffer fbAlphas = MemoryUtil.memAllocFloat(MAX_NUM);
	private final static FloatBuffer fbDmg = MemoryUtil.memAllocFloat(MAX_NUM);
	private final static FloatBuffer fbPerc = MemoryUtil.memAllocFloat(MAX_NUM);
	private final static FloatBuffer fb = MemoryUtil.memAllocFloat(MAX_NUM * 3);
	private final static Vector3f[] tmpP = new Vector3f[16];
	//	private Vector3f[] dirs = new Vector3f[MAX_NUM];

	static {
		for (int i = 0; i < tmpP.length; i++) {
			tmpP[i] = new Vector3f();
		}
	}

	public Shader s = null;//ShaderLibrary.shieldShader;
	private float[] alphas = new float[MAX_NUM];
	private Vector4f[] points = new Vector4f[MAX_NUM];
	private int collisionNum;
	private float minAlpha = 0.0f;
	private float maxDistance = 4f;
	private boolean pointsChanged = false;
	private float[] percent = new float[MAX_NUM];

	public ShieldShader() {
		for (int i = 0; i < MAX_NUM; i++) {
			points[i] = new Vector4f();
		}
	}

	public void addCollision(Vector3f c, float damage, float pc) {
		if (collisionNum < MAX_NUM) {
			alphas[collisionNum] = 1.0f;

			points[collisionNum].set(c.x, c.y, c.z, Math.min(1.0f, Math.max(0.01f, damage / 1000f)));
			
			percent[collisionNum] = pc;
			collisionNum++;
			pointsChanged = true;
		}

	}

	public void drawPoints() {
		
	}

	/**
	 * @return the collisionNum
	 */
	public int getCollisionNum() {
		return collisionNum;
	}

	/**
	 * @param collisionNum the collisionNum to set
	 */
	public void setCollisionNum(int collisionNum) {
		this.collisionNum = collisionNum;
	}

	public boolean hasCollisionInRange(float x, float y, float z) {

		for (int i = 0; i < collisionNum; i++) {
			float xx = x - points[i].x;
			float yy = y - points[i].y;
			float zz = z - points[i].z;
			float len = xx * xx + yy * yy + zz * zz;
			if (len < 576) { //24^2
				return true;
			}
		}

		return false;
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
			throw new NullPointerException("Shield Shader NULL; Shield Drawing enabled: " + EngineSettings.G_DRAW_SHIELDS.isOn());
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

			shader.recompiled = false;

		}

		for (int i = 0; i < collisionNum; i++) {
			fbAlphas.put(i, alphas[i]);
			if (pointsChanged) {
				fb.put(i * 3, points[i].x);
				fb.put(i * 3 + 1, points[i].y);
				fb.put(i * 3 + 2, points[i].z);
				fbDmg.put(i, points[i].w);
				fbPerc.put(i, percent[i]);
			}
		}
		
		fbAlphas.rewind();
		GlUtil.updateShaderFloats1(shader, "m_CollisionAlphas", fbAlphas);

		if (pointsChanged) {
			fb.rewind();
			GlUtil.updateShaderFloats3(shader, "m_Collisions", fb);
			pointsChanged = false;
			GlUtil.updateShaderInt(shader, "m_CollisionNum", collisionNum);
			
			fbDmg.rewind();
			GlUtil.updateShaderFloats1(shader, "m_Damages", fbDmg);
			
			fbPerc.rewind();
			GlUtil.updateShaderFloats1(shader, "m_Percent", fbPerc);
		}

		GlUtil.updateShaderFloat(shader, "m_MinAlpha", minAlpha);

		GlUtil.updateShaderFloat(shader, "m_MaxDistance", maxDistance);

		GlUtil.updateShaderFloat(shader, "m_Time", ShieldDrawerManager.time);

	}

	public void reset() {
		collisionNum = 0;
	}

	public void update(Timer timer) {
		for (int i = 0; i < collisionNum; i++) {
			alphas[i] -= timer.getDelta() * 0.7f;
		}
		for (int i = 0; i < collisionNum; i++) {
			if (alphas[i] <= 0) {
				alphas[i] = 0;
				//del by swapping with last element and dec pointer
				alphas[i] = alphas[collisionNum - 1];
				points[i].set(points[collisionNum - 1]);
				//				dirs[i].set(dirs[getCollisionNum()-1]);
				collisionNum = collisionNum - 1;
				pointsChanged = true;
			}
		}

	}

}
