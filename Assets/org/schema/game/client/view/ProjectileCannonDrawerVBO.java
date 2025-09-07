package org.schema.game.client.view;


import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.damage.projectile.ProjectileController;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.particle.ParticleLaserDrawerVBO;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;
import org.schema.schine.input.Keyboard;

public class ProjectileCannonDrawerVBO extends ParticleLaserDrawerVBO implements Shaderable {

	public static final float PROJECTILE_SPRITE_SIZE = 0.2f;
	private GameClientState state;
	private Sprite sprite;
	private Shader shader;

	private boolean initialized;
	private float zoomFac;

	public ProjectileCannonDrawerVBO(ProjectileController controller) {
		super(controller, PROJECTILE_SPRITE_SIZE);
		this.state = (GameClientState) controller.getState();
		this.setBlended(false);
	}

	@Override
	public void draw() {
		if (!initialized) {
			onInit();
		}
		shader = ShaderLibrary.projectileBeamQuadShader;

		DEBUG = Keyboard.isKeyDown(GLFW.GLFW_KEY_KP_1);

		if (getParticleController().getParticleCount() <= 0) {
			return;
		}
		if (WorldDrawer.drawError) {
			GlUtil.printGlErrorCritical();
		}
		if (!DEBUG) {
			shader.setShaderInterface(this);
			shader.load();
		}
		if (WorldDrawer.drawError) {
			GlUtil.printGlErrorCritical();
		}
		super.draw();
		if (WorldDrawer.drawError) {
			GlUtil.printGlErrorCritical();
		}
		if (!DEBUG) {
			shader.unload();
		}

	}

	@Override
	public void onInit() {
		sprite = Controller.getResLoader().getSprite("starSprite");

		super.onInit();
		initialized = true;
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

		//		GlUtil.updateShaderVector4f( shader, "camPos", camPos);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, sprite.getMaterial().getTexture().getTextureId());
		GlUtil.updateShaderInt(shader, "tex", 0);

		GlUtil.updateShaderFloat(shader, "zoomFac", zoomFac);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ProjectileDrawerVBO [state=" + state + ", count=" + getParticleController().getParticleCount() + "]";
	}

	public float getZoomFac() {
		return zoomFac;
	}

	public void setZoomFac(float zoomFac) {
		this.zoomFac = zoomFac;
	}

}
