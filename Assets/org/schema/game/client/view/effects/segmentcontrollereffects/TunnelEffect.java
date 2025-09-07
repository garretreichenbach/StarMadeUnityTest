package org.schema.game.client.view.effects.segmentcontrollereffects;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.schema.game.client.view.GameResourceLoader;
import org.schema.game.common.controller.SegmentController;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;

import com.bulletphysics.linearmath.Transform;

public class TunnelEffect implements Drawable, Shaderable {

	private Mesh mesh;
	private float time;
	private Transform t = new Transform();

	public TunnelEffect(SegmentController segmentController) {
	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void draw() {
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

		mesh.loadVBO(true);

		ShaderLibrary.tunnelShader.setShaderInterface(this);
		ShaderLibrary.tunnelShader.load();
		GlUtil.glPushMatrix();

		Transform tr = new Transform(t);
		t.origin.set(Controller.getCamera().getPos());

		GlUtil.glMultMatrix(t);
		GlUtil.translateModelview(0, 0, -20000);
		GlUtil.scaleModelview(100, 100, 1500);

		mesh.renderVBO();

		GlUtil.glPopMatrix();
		ShaderLibrary.tunnelShader.unload();

		mesh.unloadVBO(true);

		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glDisable(GL11.GL_BLEND);
	}

	@Override
	public boolean isInvisible() {
		return false;
	}

	@Override
	public void onInit() {
		this.mesh = (Mesh) Controller.getResLoader().getMesh("Tube").getChilds().get(0);
	}

	@Override
	public void onExit() {
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
		//Controller.getResLoader().getSprite("shield_tex").getMaterial().getTexture().getTextureId()
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, GameResourceLoader.simpleStarFieldTexture.getTextureId());
		GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, GameResourceLoader.effectTextures[0].getTextureId());
		//		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, Controller.getResLoader().getSprite("detail").getMaterial().getTexture().getTextureId());
		//		if(shader.recompiled){
		GlUtil.updateShaderInt(shader, "tex0", 0);
//		assert (false) : "TODO: NOISE NOT SET";
		GlUtil.updateShaderInt(shader, "noise", 1);
		//		}

		GlUtil.updateShaderFloat(shader, "time", time);

	}

	public void update(Timer timer) {
		this.time += timer.getDelta();
	}

	public void setTransform(Transform worldTransform) {
		this.t.set(worldTransform);
	}

}
