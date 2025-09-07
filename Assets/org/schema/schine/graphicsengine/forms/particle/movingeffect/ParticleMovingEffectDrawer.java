package org.schema.schine.graphicsengine.forms.particle.movingeffect;

import java.nio.FloatBuffer;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.particle.ParticleContainer;
import org.schema.schine.graphicsengine.forms.particle.ParticleController;
import org.schema.schine.graphicsengine.forms.particle.ParticleDrawerVBO;
import org.schema.schine.graphicsengine.forms.particle.StartContainerInterface;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;
import org.schema.schine.graphicsengine.texture.Texture;

public class ParticleMovingEffectDrawer extends ParticleDrawerVBO implements Shaderable {

	private static final float DUST_SPRITE_SIZE = 0.1f;
	static FloatBuffer store = MemoryUtil.memAllocFloat(16);
	private static Texture tex;
	public boolean frustumCulling = true;
	public float smear = 0.015f;
	float time = 1;
	Matrix4f old[];
	private Shader shader;
	private int pointer;
	private boolean first = true;

	public ParticleMovingEffectDrawer(ParticleController controller) {
		this(controller, DUST_SPRITE_SIZE);

	}

	public ParticleMovingEffectDrawer(ParticleController controller, float size) {
		super(controller, size);

		old = new Matrix4f[300];
		for (int i = 0; i < old.length; i++) {
			old[i] = new Matrix4f();
			old[i].setIdentity();

		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.ParticleSystem#draw(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void draw() {
		if(!EngineSettings.D_INFO_DRAW_SPACE_PARTICLE.isOn()) {
			return;
		}
		if (getParticleController().getParticleCount() > 0) {
			shader = ShaderLibrary.spacedustShader;
			assert (ShaderLibrary.spacedustShader != null);

			shader.setShaderInterface(this);
			shader.load();

			if (!first) {
				super.draw();
			}
			shader.unload();
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.ParticleSystem#onInit(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void onInit() {
		setDrawInverse(true);

		if (tex == null) {
			tex = Controller.getResLoader().getSprite("star").getMaterial().getTexture();
		}
		super.onInit();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.particle.ParticleDrawerVBO#handleQuad(int, org.schema.schine.graphicsengine.forms.particle.ParticleContainer)
	 */
	@Override
	protected int handleQuad(int i, ParticleContainer particles) {
		particles.getPos(i, posHelper);
		if (frustumCulling && !Controller.getCamera().isPointInFrustrum(posHelper)) {
			return 0;
		}

		posHelperAndTime.set(posHelper.x, posHelper.y, posHelper.z, particles.getLifetime(i));
		((StartContainerInterface)particles).getStart(i, startHelper);
		attribHelper.set(startHelper.x, startHelper.y, startHelper.z, 0);
		for (int g = 0; g < 4; g++) {
			GlUtil.putPoint4(vertexBuffer, posHelperAndTime);

			attribHelper.w = tCoords[g];
			GlUtil.putPoint4(attribBuffer, attribHelper);
		}
		return 4;
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

		//		GlUtil.updateShaderFloat( shader, "time", time);
		store.rewind();
		Matrix4fTools.store(old[FastMath.cyclicModulo(pointer - old.length - 2, old.length - 1)], (store));
		store.rewind();
		GlUtil.updateShaderMat4(shader, "oldModelViewMatrix", store, false);

		GlUtil.updateShaderVector4f(shader, "Param", new Vector4f(1f, 0.1f, 0.15f, 0.0005f));
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, tex.getTextureId());
		GlUtil.updateShaderInt(shader, "Texture", 0);

		ParticleMovingEffectController p = (ParticleMovingEffectController) getParticleController();

		if (p.getAccumTime() > smear || first) {
			old[pointer].set(Controller.modelviewMatrix);

			pointer = FastMath.cyclicModulo(pointer + 1, old.length - 1);
			if (pointer > 2) {
				first = false;
			}
			p.setAccumTime(0);
		}
	}

	public void update() {

	}

	public void reset() {
		pointer = 0;
		this.first = true;
	}
}
