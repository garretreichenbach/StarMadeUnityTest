package org.schema.schine.graphicsengine.forms.particle.trail;

import java.io.IOException;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.schema.common.util.data.DataUtil;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.particle.ParticleController;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;
import org.schema.schine.graphicsengine.texture.Texture;

public class ParticleTrailDrawer extends ParticleTrailDrawerVBO implements
		Shaderable {

	private static final float DUST_SPRITE_SIZE = 0.1f;
	private static Texture tex;
	private Shader shader;
	private boolean init;

	public ParticleTrailDrawer(ParticleController controller) {
		super(controller, DUST_SPRITE_SIZE);

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
		if (getParticleController().getParticleCount() > 1) {
			shader = ShaderLibrary.projectileTrailShader;
			assert (ShaderLibrary.projectileTrailShader != null);
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
			try {
				tex = Controller.getTexLoader().getTexture2D(DataUtil.dataPath + "./effects/noise.png", true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		super.onInit();
		init = true;
	}

	@Override
	public void onExit() {

		GlUtil.glBindTexture(GL12.GL_TEXTURE_3D, 0);
	}

	@Override
	public void updateShader(DrawableScene scene) {
	}

	@Override
	public void updateShaderParameters(Shader shader) {

		GlUtil.updateShaderVector4f(shader, "trailColor", ((TrailControllerInterface) getParticleController()).getColor());
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, tex.getTextureId());
		GlUtil.updateShaderInt(shader, "tex", 0);

	}

	public void update(Timer timer) {
	}

}
