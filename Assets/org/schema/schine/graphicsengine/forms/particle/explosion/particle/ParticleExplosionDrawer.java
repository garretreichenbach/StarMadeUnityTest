package org.schema.schine.graphicsengine.forms.particle.explosion.particle;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.FrameBufferObjects;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.particle.DamageContainerInterface;
import org.schema.schine.graphicsengine.forms.particle.ParticleContainer;
import org.schema.schine.graphicsengine.forms.particle.ParticleController;
import org.schema.schine.graphicsengine.forms.particle.ParticleDrawerVBO;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;

public class ParticleExplosionDrawer extends ParticleDrawerVBO implements Shaderable {

	private static final float EXPLOSION_SPRITE_SIZE = 0.6f;
	Vector3f uTmp = new Vector3f();
	float time = 1;
	private Shader shader;
	private int depthTextureId;
	private float near;
	private float far;
	private float particleSize;
	private boolean check = true;
	public static Sprite[] explTex;

	
	public ParticleExplosionDrawer(ParticleController<? extends DamageContainerInterface> controller) {
		super(controller, EXPLOSION_SPRITE_SIZE);

	}
	public void draw(FrameBufferObjects foregroundFbo, FrameBufferObjects fbo, int depthBufferTex, float near, float far, float particleSize) {
		if (getParticleController().getParticleCount() > 0) {
			this.depthTextureId = depthBufferTex;
			this.near = near;
			this.far = far;
			this.particleSize = particleSize;
			this.depthMask = true;
			fbo.enable();
			draw();
			fbo.disable();
			
			if(check) {
				GlUtil.printGlErrorCritical();
				check  = false;
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.ParticleSystem#draw(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void draw() {

		if (getParticleController().getParticleCount() > 0) {
			shader = ShaderLibrary.explosionShader;
			assert (ShaderLibrary.explosionShader != null);
			shader.setShaderInterface(this);
			shader.load();
			super.draw();
			shader.unload();
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.ParticleSystem#onInit(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void onInit() {
		setDrawInverse(true);

		explTex = new Sprite[4];
		explTex[0] = Controller.getResLoader().getSprite("explosion/expl_02-8x4");
		explTex[1] = Controller.getResLoader().getSprite("explosion/expl_03-8x4");
		explTex[2] = Controller.getResLoader().getSprite("explosion/expl_04-8x4");
		explTex[3] = Controller.getResLoader().getSprite("explosion/expl_06-8x4");
//		explTex[4] = Controller.getResLoader().getSprite("explosion/expl_09-4x8");
//		explTex[5] = Controller.getResLoader().getSprite("explosion/expl_10-4x8");
		super.onInit();
	}

	@Override
	public float getSpriteSize(int i, ParticleContainer particles) {
		return ((DamageContainerInterface)particles).getDamage(i)* EXPLOSION_SPRITE_SIZE;
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

	
}
