package org.schema.game.client.view.gui.shiphud.newhud;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.schema.common.util.StringTools;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;
import org.schema.schine.input.InputState;

public abstract class FillableHorizontalBar extends HudConfig implements Shaderable {

	protected GUITextOverlay textDesc;
	private float glowIntensity = 0;
	private Sprite barSprite;
	private Vector4f color;
	private GUITextOverlay text;

	public FillableHorizontalBar(InputState state) {
		super(state);

	}

	public abstract boolean isBarFlippedX();

	public abstract boolean isBarFlippedY();

	public abstract boolean isFillStatusTextOnTop();

	
	public abstract Vector2f getTextPos();
	public abstract Vector2f getTextDescPos();
//	public Vector2f getTextPos() {
//		return isLongerBar() ? new Vector2f(812, -11) : new Vector2f(200, 0);
//	}
//	public Vector2f getTextDescPos() {
//		return isLongerBar() ? new Vector2f(172, -11) : (isBarFlippedX() ? new Vector2f(6, 0) : new Vector2f(12, 0));
//	}
	@Override
	public void cleanUp() {

	}

	@Override
	public void draw() {

		GlUtil.glPushMatrix();
		transform();
		text.getText().set(0, StringTools.formatPointZero(getFilled() * 100f) + "%");
		textDesc.getText().set(0, getText());

		if (isLongerBar()) {
			textDesc.setColor(1, 1, 1, 1);
			text.setColor(1, 1, 1, 1);
			text.setPos(getTextPos().x, getTextPos().y, 0);
			textDesc.setPos(getTextDescPos().x, getTextDescPos().y, 0);
		} else {
			textDesc.setColor(color);
			text.setColor(color);
			text.setPos(getTextPos().x, getTextPos().y, 0);
			textDesc.setPos(getTextDescPos().x, getTextDescPos().y, 0);
//			int s = 0;
//			if (isFillStatusTextOnTop()) {
//				if (isBarFlippedX()) {
//					text.setPos(200, s, 0);
//					textDesc.setPos(6, s, 0);
//				} else {
//					text.setPos(200, s, 0);
//					textDesc.setPos(12, s, 0);
//				}
//			} else {
//				if (isBarFlippedX()) {
//					text.setPos(200, -s, 0);
//					textDesc.setPos(6, -s, 0);
//				} else {
//					text.setPos(200, -s, 0);
//					textDesc.setPos(12, -s, 0);
//				}
//
//			}
		}
		ShaderLibrary.powerBarShaderHor.setShaderInterface(this);
		ShaderLibrary.powerBarShaderHor.load();
		barSprite.draw();
		ShaderLibrary.powerBarShaderHor.unload();
		text.draw();
		textDesc.draw();
		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		if (isLongerBar()) {
			this.barSprite = Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath()+"HUD_PlayerHP-1x4-gui-");
		} else {
			this.barSprite = Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath()+"HUD_HorizontalBar-1x4-gui-");

		}
		barSprite.setSelectedMultiSprite(1);
		this.width = this.barSprite.getWidth();
		this.height = this.barSprite.getHeight();
		this.color = new Vector4f(getConfigColor().x / 255f, getConfigColor().y / 255f, getConfigColor().z / 255f, getConfigColor().w / 255f);

		text = new GUITextOverlay(FontSize.SMALL_14, getState());
//		text.setColor(color);
		textDesc = new GUITextOverlay(FontSize.SMALL_14, getState());
//		textDesc.setColor(color);
		text.setTextSimple("n/a");
		textDesc.setTextSimple("n/a");
	}

	protected boolean isLongerBar() {
		return false;
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
		if (shader.recompiled) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		if (shader.recompiled) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, barSprite.getMaterial().getTexture().getTextureId());
		if (shader.recompiled) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.updateShaderInt(shader, "barTex", 0);
		if (shader.recompiled) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.updateShaderFloat(shader, "filled", getFilled());
		if (shader.recompiled) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.updateShaderFloat(shader, "glowIntensity", this.glowIntensity);
		if (shader.recompiled) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.updateShaderBoolean(shader, "flippedX", isBarFlippedX());
		if (shader.recompiled) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.updateShaderBoolean(shader, "flippedY", isBarFlippedY());
		if (shader.recompiled) {
			GlUtil.printGlErrorCritical();
		}

		GlUtil.updateShaderFloat(shader, "minTexCoord", 0.005f);
		if (shader.recompiled) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.updateShaderFloat(shader, "maxTexCoord", 0.995f);
		if (shader.recompiled) {
			GlUtil.printGlErrorCritical();
		}

		GlUtil.updateShaderVector4f(shader, "barColor", color);
		if (shader.recompiled) {
			GlUtil.printGlErrorCritical();
		}
		shader.recompiled = false;
	}

	/**
	 * @return the color
	 */
	public final Vector4f getColor() {
		return color;
	}

	/**
	 * @return the filled
	 */
	public abstract float getFilled();

	public abstract String getText();

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
