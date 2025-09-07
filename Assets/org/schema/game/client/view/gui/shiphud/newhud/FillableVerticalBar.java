package org.schema.game.client.view.gui.shiphud.newhud;

import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;
import org.schema.schine.graphicsengine.util.timer.LinearTimerUtil;
import org.schema.schine.input.InputState;

public abstract class FillableVerticalBar extends HudConfig implements Shaderable {

	protected GUITextOverlay textDesc;
	private float glowIntensity = 0;
	protected Sprite barSprite;
	protected GUITextOverlay text;

	protected LinearTimerUtil u = new LinearTimerUtil();
	protected Vector4f color;
	protected Vector4f colorWarn;
	public FillableVerticalBar(InputState state) {
		super(state);

	}

	public abstract double getFilledMargin();
	
	public abstract boolean isBarFlippedX();

	public abstract boolean isBarFlippedY();

	public abstract boolean isFillStatusTextOnTop();

	@Override
	public void cleanUp() {

	}
	@Override
	public void update(Timer timer) {
		if(getConfigPosition() != null){
			updateOrientation();
		}
		u.update(timer);
	}
	
	@Override
	public void draw() {

		GlUtil.glPushMatrix();
		transform();
		text.getText().set(0, getBarName()+" "+StringTools.formatPointZero(getFilled() * 100f) + "%");
		textDesc.getText().set(0, getText());

		textDesc.setColor(1, 1, 1, 1);
		text.setColor(1, 1, 1, 1);
		text.setPos(getTextOffsetX()-text.getMaxLineWidth(), getTextOffsetY(), 0);
		textDesc.setPos(getTextOffsetX()-textDesc.getMaxLineWidth(), getTextOffsetY()+40, 0);
		
		ShaderLibrary.powerBarShader.setShaderInterface(this);
		ShaderLibrary.powerBarShader.load();
		barSprite.draw();
		ShaderLibrary.powerBarShader.unload();
		text.draw();
		textDesc.draw();
		GlUtil.glPopMatrix();
	}

	protected abstract int getTextOffsetX();
	protected abstract int getTextOffsetY();

	protected abstract String getBarName();

	@Override
	public void onInit() {
		this.barSprite = Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath()+"HUD_VerticalBar_Long-4x1-gui-");
		barSprite.setSelectedMultiSprite(0);
		this.width = this.barSprite.getWidth();
		this.height = this.barSprite.getHeight();
		
		text = new GUITextOverlay(FontSize.SMALL_14, getState());
//		text.setColor(color);
		textDesc = new GUITextOverlay(FontSize.SMALL_14, getState());
//		textDesc.setColor(color);
		text.setTextSimple("n/a");
		textDesc.setTextSimple("n/a");
		this.color = new Vector4f(getConfigColor().x / 255f, getConfigColor().y / 255f, getConfigColor().z / 255f, getConfigColor().w / 255f);
		this.colorWarn = new Vector4f(getConfigColorWarn().x / 255f, getConfigColorWarn().y / 255f, getConfigColorWarn().z / 255f, getConfigColorWarn().w / 255f);

	}
	public abstract Vector4i getConfigColorWarn();

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
		float filled = getFilled();
		GlUtil.updateShaderFloat(shader, "filled", filled);
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

		GlUtil.updateShaderVector4f(shader, "barColor", getColor(filled));
		if (shader.recompiled) {
			GlUtil.printGlErrorCritical();
		}
		shader.recompiled = false;
	}

	protected abstract Vector4f getColor(float filled);


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
