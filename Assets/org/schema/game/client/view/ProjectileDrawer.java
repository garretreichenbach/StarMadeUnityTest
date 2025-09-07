package org.schema.game.client.view;

import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.damage.projectile.ProjectileController;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;

public class ProjectileDrawer implements Drawable, Shaderable {

	Vector4f camPos;
	Vector4f lightPos;
	private ProjectileController projectileController;
	@SuppressWarnings("unused")
	private GameClientState state;
	private float cover;
	private Sprite sprite;
	private Shader shader;

	public ProjectileDrawer(ProjectileController controller) {
		this.projectileController = controller;
		this.state = (GameClientState) controller.getState();
	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void draw() {

		//		Mesh.drawFastVBOInstancedPositionOnly(projectileController.getProjectiles(), cubeMeshes);
		sprite.setBillboard(true);

		//		GlUtil.glEnable(GL11.GL_POINT_SPRITE);
		//		GL11.glBegin(GL11.GL_POINTS);
		sprite.getScale().set(0.3f, 0.3f, 0.3f);
		if (projectileController.getParticleCount() > 0) {
			shader = ShaderLibrary.projectileShader;

			GlUtil.glPushMatrix();

			shader.setShaderInterface(this);
			shader.load();
			GlUtil.glEnable(GL11.GL_DEPTH_TEST);
			GlUtil.glDepthMask(false);
			GlUtil.glEnable(GL11.GL_BLEND);
			GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			sprite.setFlip(true);

			//			Sprite.draw(  sprite, projectileController.getProjectileCount(), projectileController.getProjectiles());

			shader.unload();
			GlUtil.glDisable(GL11.GL_BLEND);
			GlUtil.glDepthMask(true);
			GlUtil.glPopMatrix();

			GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
			GlUtil.glEnable(GL11.GL_LIGHTING);
		}
		//		GL11.glEnd();
		//		GlUtil.glDisable(GL11.GL_POINT_SPRITE);

	}

	@Override
	public boolean isInvisible() {

		return false;
	}

	@Override
	public void onInit() {
		sprite = Controller.getResLoader().getSprite("smoke4");

		lightPos = new Vector4f();
		camPos = new Vector4f();

	}

	@Override
	public void onExit() {

	}

	@Override
	public void updateShader(DrawableScene scene) {
		lightPos.set(scene.getLight().getPos().x, scene.getLight().getPos().y, scene.getLight().getPos().z, 1);
		camPos.set(Controller.getCamera().getPos().x, Controller.getCamera().getPos().y, Controller.getCamera().getPos().z, 0.0f);
		//		throw new NullPointerException("[redign note] fix cover setting");
		//		cover = ((GameDrawManager)scene).getSky().getCover();
	}

	@Override
	public void updateShaderParameters(Shader shader) {
		cover = 5;
		GlUtil.updateShaderVector4f(shader, "source", camPos);

		GlUtil.updateShaderFloat(shader, "cCover", cover * 2);

		GlUtil.updateShaderVector4f(shader, "lightPos", lightPos);

		GlUtil.updateShaderTexture2D(shader, "noiseVolume", sprite.getMaterial().getTexture().getTextureId(), 0);

	}

}
