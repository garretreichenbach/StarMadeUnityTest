package org.schema.game.client.view.gui.shiphud;

import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;
import org.schema.schine.input.InputState;

public class GUIPowerBar extends GUIElement implements Shaderable {

	private final Vector4f color = new Vector4f(0.4f, 0.4f, 1, 1);
	private float glowIntensity = 0;
	private Sprite barSprite;
	private float filled;

	public GUIPowerBar(InputState state) {
		super(state);
	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void draw() {
		GlUtil.glPushMatrix();

		transform();
		ShaderLibrary.powerBarShader.setShaderInterface(this);
		ShaderLibrary.powerBarShader.load();
		barSprite.draw();
		ShaderLibrary.powerBarShader.unload();
		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		this.barSprite = Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath()+"bar-4x1-gui-");
	}

	@Override
	public float getHeight() {
		return 512;
	}

	@Override
	public float getWidth() {
		return 128;
	}

	@Override
	public void onExit() {
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	@Override
	public void updateShader(DrawableScene scene) {

	}

	@Override
	public void updateShaderParameters(Shader shader) {
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, barSprite.getMaterial().getTexture().getTextureId());
		GlUtil.updateShaderInt(shader, "barTex", 0);
		GlUtil.updateShaderFloat(shader, "filled", this.filled);
		GlUtil.updateShaderFloat(shader, "glowIntensity", this.glowIntensity);

		GlUtil.updateShaderFloat(shader, "minTexCoord", 0.01f);
		GlUtil.updateShaderFloat(shader, "maxTexCoord", 0.99f);

		GlUtil.updateShaderVector4f(shader, "barColor", color);
	}

	/**
	 * @return the color
	 */
	public Vector4f getColor() {
		return color;
	}

	/**
	 * @return the filled
	 */
	public float getFilled() {
		return filled;
	}

	/**
	 * @param filled the filled to set
	 */
	public void setFilled(float filled) {
		this.filled = filled;
	}

	/**
	 * @return the glowIntensity
	 */
	public float getGlowIntensity() {
		return glowIntensity;
	}

	/**
	 * @param glowIntensity the glowIntensity to set
	 */
	public void setGlowIntensity(float glowIntensity) {
		this.glowIntensity = glowIntensity;
	}

}
