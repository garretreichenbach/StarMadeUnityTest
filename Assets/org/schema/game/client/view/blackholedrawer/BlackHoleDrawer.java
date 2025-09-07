package org.schema.game.client.view.blackholedrawer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.schema.schine.graphicsengine.core.AbstractScene;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;

public class BlackHoleDrawer implements Drawable, Shaderable {

	/**
	 * The time.
	 */
	private float time;

	/**
	 * The sky tex.
	 */
	private int skyTex;

	/**
	 * The sky normal.
	 */
	private int skyNormal;

	/**
	 * The sharp.
	 */
	private float sharp;

	/**
	 * The cover.
	 */
	private float cover;

	private Mesh planetMesh;

	public BlackHoleDrawer() {

	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void draw() {
		GlUtil.glPushMatrix();
		//		GlUtil.translateModelview(
		//				relSectorPos.x * Universe.getSectorSizeWithMargin(),
		//				relSectorPos.y * Universe.getSectorSizeWithMargin(),
		//				relSectorPos.z * Universe.getSectorSizeWithMargin());
		GlUtil.translateModelview(AbstractScene.mainLight.getPos());
		GlUtil.scaleModelview(10, 10, 10);
		GlUtil.glEnable(GL11.GL_CULL_FACE);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		ShaderLibrary.blackHoleShader.setShaderInterface(this);
		ShaderLibrary.blackHoleShader.load();
		planetMesh.draw();
		ShaderLibrary.blackHoleShader.unload();
		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glPopMatrix();
	}

	@Override
	public boolean isInvisible() {
				return false;
	}

	@Override
	public void onInit() {
		planetMesh = (Mesh) Controller.getResLoader().getMesh("BlackHole").getChilds().iterator().next();

		sharp = 0.0000005f;
		cover = 0.7f;

		skyTex = Controller.getResLoader().getSprite("detail").getMaterial().getTexture().getTextureId();
		skyNormal = Controller.getResLoader().getSprite("detail_normal").getMaterial().getTexture().getTextureId();
	}

	/**
	 * Gets the cover.
	 *
	 * @return the cover
	 */
	public float getCover() {
		return cover;
	}

	/**
	 * Sets the cover.
	 *
	 * @param cover the cover to set
	 */
	public void setCover(float cover) {
		this.cover = cover;
	}

	/**
	 * Gets the sharp.
	 *
	 * @return the sharp
	 */
	public float getSharp() {
		return sharp;
	}

	/**
	 * Sets the sharp.
	 *
	 * @param sharp the sharp to set
	 */
	public void setSharp(float sharp) {
		this.sharp = sharp;
	}

	@Override
	public void onExit() {
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);

	}

	@Override
	public void updateShader(DrawableScene scene) {
		
	}

	/**
	 * uniform sampler2D tex;
	 * uniform sampler2D nmap;
	 * uniform float cSharp;
	 * uniform float cCover;
	 * uniform float cMove;
	 * uniform vec4 lightPos;.
	 *
	 * @param gl            the gl
	 * @param glu           the glu
	 * @param shaderprogram the shaderprogram
	 */
	@Override
	public void updateShaderParameters(Shader shader) {
		;
		GlUtil.updateShaderFloat(shader, "cSharp", sharp);
		GlUtil.updateShaderFloat(shader, "cCover", cover);
		GlUtil.updateShaderFloat(shader, "cMove", time);
		GlUtil.updateShaderVector4f(shader, "lightPos", AbstractScene.mainLight.getPos().x,
				AbstractScene.mainLight.getPos().y,
				AbstractScene.mainLight.getPos().z,
				1);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, skyTex);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, skyNormal);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE2);

		GlUtil.updateShaderInt(shader, "tex", 0);
		GlUtil.updateShaderInt(shader, "nmap", 1);
	}

	public void update(Timer timer) {
		time += timer.getDelta() * 0.07f;
		if (time > 1) {
			time -= Math.floor(time);
		}
	}
}
