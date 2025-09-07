package org.schema.schine.graphicsengine.forms.particle.simple;

import org.lwjgl.opengl.GL11;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.particle.ParticleController;
import org.schema.schine.graphicsengine.forms.particle.ParticleDrawerVBO;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;
import org.schema.schine.graphicsengine.texture.Texture;

public class ParticleSimpleDrawer extends ParticleDrawerVBO implements
		Shaderable {

	private static final float SPRITE_SIZE = 2.5f;
	float time = 0;
	private Shader shader;
	private boolean init;
	private Texture tex;

	public ParticleSimpleDrawer(ParticleController controller) {
		this(controller, SPRITE_SIZE);
	}

	public ParticleSimpleDrawer(ParticleController controller, float size) {
		super(controller, size);

		setDrawInverse(true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.schema.schine.graphicsengine.forms.ParticleSystem#draw(javax.media
	 * .openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void draw() {
		if (!init) {
			onInit();
		}
		if (getParticleController().getParticleCount() > 0) {
			shader = ShaderLibrary.projectileQuadShader;
			assert (shader != null);
			shader.setShaderInterface(this);
			shader.load();
			super.draw();
			shader.unload();
		}
	}
	public void drawRaw() {
		if (!init) {
			onInit();
		}
		if (getParticleController().getParticleCount() > 0) {
			shader = ShaderLibrary.projectileQuadBloomShader;
			assert (shader != null);
			shader.setShaderInterface(this);
			shader.load();
			super.draw();
			shader.unload();
		}		
	}
	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.schema.schine.graphicsengine.forms.ParticleSystem#onInit(javax.media
	 * .openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void onInit() {

		if (tex == null) {
			tex = Controller.getResLoader().getSprite("beacon").getMaterial().getTexture();
		}
		super.onInit();
		init = true;
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

		GlUtil.updateShaderFloat(shader, "time", time);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, tex.getTextureId());
		GlUtil.updateShaderInt(shader, "tex", 0);

	}

	public void update(Timer timer) {
		this.time += timer.getDelta();
		while (time > 1.0f) {
			time -= 1.0f;
		}
	}

	protected Texture getTexture() {
		return this.tex;
	}

	protected void setTexture(Texture tex) {
		this.tex = tex;
	}
}
