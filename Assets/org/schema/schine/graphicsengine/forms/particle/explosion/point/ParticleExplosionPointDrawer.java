package org.schema.schine.graphicsengine.forms.particle.explosion.point;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;

public class ParticleExplosionPointDrawer implements Shaderable {

	private static final float EXPLOSION_SPRITE_SIZE = 0.6f;
	Vector3f uTmp = new Vector3f();
	float time = 1;
	private Shader shader;
	private int depthTextureId;
	private float near;
	private float far;
	private float particleSize;
	private boolean check = true;
	private ParticleExplosionPointController controller;
	private Vector3f pos = new Vector3f();
 	private Vector4f color = new Vector4f(1,1,1,1);
	public static Sprite[] explTex;

	
	public ParticleExplosionPointDrawer(ParticleExplosionPointController controller) {
		this.controller = controller;
	}
	public void draw() {
		
		if (controller.getParticleCount() > 0) {
//			shader = ShaderLibrary.explosionPointShader;
//			assert (ShaderLibrary.explosionPointShader != null);
//			shader.setShaderInterface(this);
//			shader.load();
			
			ShaderLibrary.simpleColorShader.loadWithoutUpdate();
			Mesh sphere = (Mesh) Controller.getResLoader().getMesh("SphereLowPoly").getChilds().get(0);
			sphere.loadVBO(true);
			
			GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GlUtil.glDisable(GL11.GL_TEXTURE_2D);
			GlUtil.glDisable(GL12.GL_TEXTURE_3D);
			GlUtil.glDisable(GL11.GL_LIGHTING);
			GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
			
			
			GlUtil.glEnable(GL11.GL_BLEND);
			GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
			final float basicSize = 0.2f;
			final int count = controller.getParticleCount();
			for(int i = 0; i < count; i++) {
				controller.getParticles().getPos(i, pos);
				float size = controller.getParticles().getDamage(i);
				float maxLife = controller.getParticles().getImpactForce(i);
				
				controller.getParticles().getColor(i, color);
				float time = controller.getParticles().getLifetime(i);
				
				float perc = time / maxLife;
				
				if(perc > 0.5f) {
					float cperc = 1 - perc;
					
					color.scale(cperc*2.0f);
					
				}
				
				float pSoue = perc * size;
				GlUtil.glColor4f(color);
				GlUtil.updateShaderVector4f(ShaderLibrary.simpleColorShader, "col", color);
				GlUtil.glPushMatrix();
				GlUtil.glTranslatef(pos);
				
				GlUtil.rotateModelview(perc*1000, 1, 1, 1);
				
				GlUtil.scaleModelview(-basicSize*pSoue, -basicSize*pSoue, -basicSize*pSoue);
				
				sphere.renderVBO();
				
				GlUtil.scaleModelview(-0.6f, -0.6f, -0.6f);
				sphere.renderVBO();
				GlUtil.glPopMatrix();
				
			}
			GlUtil.glColor4f(1, 1, 1, 1);
			sphere.unloadVBO(true);
			
			
//			shader.unload();
			
			GlUtil.glEnable(GL11.GL_TEXTURE_2D);
			GlUtil.glEnable(GL11.GL_LIGHTING);
			
			ShaderLibrary.simpleColorShader.unloadWithoutExit();
		}
	}



	@Override
	public void onExit() {
		
		for(int i = 0; i < explTex.length; i++) {
			int texIndex = (i+1);
			GL13.glActiveTexture(GL13.GL_TEXTURE0+texIndex);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		}
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		
	}

	@Override
	public void updateShader(DrawableScene scene) {
	}

	@Override
	public void updateShaderParameters(Shader shader) {
		
		
		GlUtil.updateShaderFloat(shader, "zNear", near);
		GlUtil.updateShaderFloat(shader, "zFar", far);
		GlUtil.updateShaderFloat(shader, "particleSize", particleSize);

		GlUtil.updateShaderVector2f(shader, "viewport", new Vector2f(GLFrame.getWidth(), GLFrame.getHeight()));
		
		
		GlUtil.updateShaderFloat(shader, "time", time);
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, depthTextureId);
		GlUtil.updateShaderInt(shader, "depthTex", 0);
		
		for(int i = 0; i < explTex.length; i++) {
			int texIndex = (i+1);
			GL13.glActiveTexture(GL13.GL_TEXTURE0+texIndex);
			GlUtil.glEnable(GL11.GL_TEXTURE_2D);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, explTex[i].getMaterial().getTexture().getTextureId());
			GlUtil.updateShaderInt(shader, "tex"+i, texIndex);
		}
		
		
	}
	public ParticleExplosionPointController getParticleController() {
		return controller;
	}

	
}
