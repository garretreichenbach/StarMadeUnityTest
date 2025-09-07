package org.schema.game.client.view.effects;

import java.nio.FloatBuffer;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.system.MemoryUtil;
import org.schema.game.client.view.GameResourceLoader;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;

public class ParticleShieldExplosionPointDrawer implements Shaderable {

	private static final float EXPLOSION_SPRITE_SIZE = 0.6f;
	Vector3f uTmp = new Vector3f();
	float time = 1;
	private Shader shader;
	private int depthTextureId;
	private float near;
	private float far;
	private float particleSize;
	private boolean check = true;
	private ParticleShieldExplosionPointController controller;
	private Vector3f pos = new Vector3f();
 	private Vector4f color = new Vector4f(1,1,1,1);
	public static Sprite[] explTex;
	private float minAlpha = 0.0f;
	private float maxDistance = 4f;
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
	private float[] alphas = new float[MAX_NUM];
	private Vector4f[] points = new Vector4f[MAX_NUM];
	public ParticleShieldExplosionPointDrawer(ParticleShieldExplosionPointController controller) {
		this.controller = controller;
		points[0] = new Vector4f(1, 1, 1, 1);
	}
	public void draw() {
		
		if (controller.getParticleCount() > 0) {
//			shader = ShaderLibrary.explosionPointShader;
//			assert (ShaderLibrary.explosionPointShader != null);
//			shader.setShaderInterface(this);
//			shader.load();
			
			
			GlUtil.glDisable(GL11.GL_CULL_FACE);
			GlUtil.glEnable(GL11.GL_TEXTURE_2D);
			GlUtil.glEnable(GL11.GL_BLEND);
			GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
			
			Mesh sphere = (Mesh) Controller.getResLoader().getMesh("Sphere").getChilds().get(0);
			sphere.loadVBO(true);
			
			shader = ShaderLibrary.shieldBubbleShader;
			shader.setShaderInterface(this);
			shader.load();
			
			
			
			
			
			final float basicSize = 0.2f;
			final int count = controller.getParticleCount();
			for(int i = 0; i < count; i++) {
				controller.getParticles().getPos(i, pos);
				float missileHpPercent = controller.getParticles().getDamage(i);
				float maxLife = controller.getParticles().getImpactForce(i);
				
				controller.getParticles().getColor(i, color);
				float time = controller.getParticles().getLifetime(i);
				
				float perc = time / maxLife;
				
				int colNum = 1;
				
				
				points[0].set(-6.91342f,-5.5557f, -4.6194f,1);;
				alphas[0] = 1.0f-perc;
				float percC = perc;
				boolean pointsChanged = true;
				for (int m = 0; m < colNum; m++) {
					fbAlphas.put(m, alphas[m]);
					if (pointsChanged) {
						fb.put(m * 3, points[m].x);
						fb.put(m * 3 + 1, points[m].y);
						fb.put(m * 3 + 2, points[m].z);
						fbDmg.put(m, points[m].w);
						fbPerc.put(m, missileHpPercent);
					}
				}
				
				fbAlphas.rewind();
				GlUtil.updateShaderFloats1(shader, "m_CollisionAlphas", fbAlphas);

				if (pointsChanged) {
					fb.rewind();
					GlUtil.updateShaderFloats3(shader, "m_Collisions", fb);
					pointsChanged = false;
					GlUtil.updateShaderInt(shader, "m_CollisionNum", colNum);
					
					fbDmg.rewind();
					GlUtil.updateShaderFloats1(shader, "m_Damages", fbDmg);
					
					fbPerc.rewind();
					GlUtil.updateShaderFloats1(shader, "m_Percent", fbPerc);
				}
				
				
				GlUtil.updateShaderFloat(shader, "m_TexCoordMult", 5);
				if(perc > 0.5f) {
					float cperc = 1 - perc;
					
					color.scale(cperc*2.0f);
					
				}
				
				GlUtil.glColor4f(1,1,1,1);
				GlUtil.updateShaderVector4f(shader, "col", color);
				GlUtil.glPushMatrix();
				GlUtil.glTranslatef(pos);
				
//				GlUtil.rotateModelview(perc*1000, 1, 1, 1);
				
				GlUtil.scaleModelview(0.01f, 0.01f, 0.01f);
				sphere.renderVBO();
				
				
				GlUtil.glPopMatrix();
				
			}
			GlUtil.glColor4f(1, 1, 1, 1);
			sphere.unloadVBO(true);
			
			
//			shader.unload();
			
			GlUtil.glEnable(GL11.GL_TEXTURE_2D);
			GlUtil.glEnable(GL11.GL_LIGHTING);
			GlUtil.glEnable(GL11.GL_CULL_FACE);
			
			shader.unload();
		}
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
		
		
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, Controller.getResLoader().getSprite("shield_tex").getMaterial().getTexture().getTextureId());
		GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, GameResourceLoader.effectTextures[0].getTextureId());
		GlUtil.glActiveTexture(GL13.GL_TEXTURE2);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, GameResourceLoader.effectTextures[1].getTextureId());
		
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);

			GlUtil.updateShaderInt(shader, "m_ShieldTex", 0);

			GlUtil.updateShaderInt(shader, "m_Distortion", 1);

			GlUtil.updateShaderInt(shader, "m_Noise", 2);

		

		GlUtil.updateShaderFloat(shader, "m_MinAlpha", minAlpha);

		GlUtil.updateShaderFloat(shader, "m_MaxDistance", maxDistance);

		GlUtil.updateShaderFloat(shader, "m_Time", controller.time);
	}
	public ParticleShieldExplosionPointController getParticleController() {
		return controller;
	}

	
}
