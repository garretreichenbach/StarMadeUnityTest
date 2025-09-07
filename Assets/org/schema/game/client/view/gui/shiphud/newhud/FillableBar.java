package org.schema.game.client.view.gui.shiphud.newhud;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.schema.common.util.StringTools;
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
import org.schema.schine.input.InputState;

public abstract class FillableBar extends HudConfig implements Shaderable {

	private static final float TITLE_DRAWN_OPAQUE = 4;
	private static final float TITLE_DRAWN_BLEND = 1.3f;
	private static final float TITLE_DRAWN_TOTAL = TITLE_DRAWN_OPAQUE + TITLE_DRAWN_BLEND;
	protected boolean drawPercent = true;
	protected boolean drawExtraText;
	private float glowIntensity = 0;
	private Sprite barSprite;
	private Vector4f color;
	private GUITextOverlay text;
	private GUITextOverlay textExtra;
	private GUITextOverlay textTitle;
	private float displayed;

	public FillableBar(InputState state) {
		super(state);

	}

	protected abstract String getDisplayTitle();

	public abstract boolean isBarFlippedX();

	public abstract boolean isBarFlippedY();

	public abstract boolean isFillStatusTextOnTop();

	public abstract Vector2f getOffsetText();

	public boolean isDrawn(){
		return true;
	}
	
	@Override
	public void cleanUp() {

	}
	public String getPercentText(){
		return StringTools.formatPointZero(getFilled()[0] * 100f) + "%";
	}
	public abstract String getText(int i);
	public String getPercentText(int i){
		return getPercentText();
	}
	
	public float getSpriteSpan() {
		return 512;
	}
	public float getTextIndent() {
		return 80;
	}
	public float getExtraTextYOffset() {
		return 16;
	}
	public float getTitleTextYOffset() {
		return 22;
	}
	public float getXOffset() {
		return isBarFlippedX() ? 25 : -23;
	}
	public Vector2f getStaticTextPos() {
		return new Vector2f(isBarFlippedX() ? 108 : 50, 500);
	}
	public Vector2f getStaticExtraTextPos() {
		return new Vector2f(isBarFlippedX() ? 168 : 110, 498);
	}
	public Vector2f getStaticTitleTextPos() {
		return new Vector2f(isBarFlippedX() ? 168 : 110, 498);
	}
	
	public void drawText() {
		GlUtil.glPushMatrix();
		transform();
		
		int sections = getFilled().length;
		
		float len = getSpriteSpan();
		float lenHalf = len/2;
		float s = 1f/sections;
		float lenSec = s*len;
		float linearXMod = (isBarFlippedX() ? -1f : 1f) * getTextIndent();
		for(int i = 0; i < sections; i++){
			float yPosSection = (int)(lenSec*i);
			
			int xPositionSection = (int)((1.0f - (Math.abs(yPosSection - lenHalf) / lenHalf)) * linearXMod); 
			
			if (drawPercent) {
				text.getText().set(0, getPercentText(i));
				if (drawExtraText) {
					textExtra.getText().set(0, getText(i));
				}
			} else {
				text.getText().set(0, getText());
			}
			int extraTextYOffset = 16;
			int titleTextYOffset = 22;
			if (isFillStatusTextOnTop()) {
				text.setPos(getXOffset()+xPositionSection, yPosSection, 0);
				textExtra.setPos(getXOffset()+xPositionSection, yPosSection+extraTextYOffset, 0);
				textTitle.setPos(getXOffset(), -titleTextYOffset, 0);
			} else {
				text.setPos(getStaticTextPos().x, getStaticTextPos().y, 0);
				textExtra.setPos(getStaticExtraTextPos().x, getStaticExtraTextPos().y, 0);
				textTitle.setPos(getStaticTitleTextPos().x, getStaticTitleTextPos().y, 0);
			}
			text.getPos().x += getOffsetText().x;
			text.getPos().y += getOffsetText().y;
	
			textExtra.getPos().x += getOffsetText().x;
			textExtra.getPos().y += getOffsetText().y;
	
			textTitle.getPos().x += getOffsetText().x;
			textTitle.getPos().y += getOffsetText().y;
			
			
			text.draw();
			if (drawExtraText) {
				textExtra.draw();
			}
		}
		if (displayed < TITLE_DRAWN_TOTAL) {
			if (displayed > TITLE_DRAWN_OPAQUE) {
				float tAlph = displayed - TITLE_DRAWN_OPAQUE;
				float pAlph = tAlph / TITLE_DRAWN_BLEND;
				textTitle.getColor().a = 1.0f - pAlph;
			} else {
				textTitle.getColor().a = 1.0f;
			}
			textTitle.draw();
		} else {
		}
		
		GlUtil.glPopMatrix();
	}
	@Override
	public void draw() {

		if(!isDrawn()){
			return;
		}
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

		this.color = new Vector4f(getConfigColor().x / 255f, getConfigColor().y / 255f, getConfigColor().z / 255f, getConfigColor().w / 255f);

		text = new GUITextOverlay(FontSize.SMALL_15, getState());
		text.setColor(color);
		text.setTextSimple("n/a");

		textExtra = new GUITextOverlay(FontSize.SMALL_15, getState());
		textExtra.setColor(color);
		textExtra.setTextSimple("n/a");

		textTitle = new GUITextOverlay(FontSize.MEDIUM_18, getState());
		textTitle.setColor(color);
		textTitle.setTextSimple(getDisplayTitle());
	}

	@Override
	public float getHeight() {
		return UIScale.getUIScale().scale(512);
	}

	@Override
	public float getWidth() {
		return UIScale.getUIScale().scale(128);
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
		float[] filled = getFilled();
		if(filled.length > 8){
			float[] oldfilled = filled;;
			filled = new float[8];
			for(int i = 0; i < 8; i++){
				filled[i] = oldfilled[i];
			}
		}
		GlUtil.updateShaderFloatArray(shader, "filled", filled);
		if (shader.recompiled) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.updateShaderInt(shader, "sections", filled.length);
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

		GlUtil.updateShaderFloat(shader, "minTexCoord", 0.001f);
		if (shader.recompiled) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.updateShaderFloat(shader, "maxTexCoord", 0.999f);
		if (shader.recompiled) {
			GlUtil.printGlErrorCritical();
		}

		GlUtil.updateShaderVector4f(shader, "barColor", color);

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
	public abstract float[] getFilled();

	public String getText(){
		return getText(0);
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

	/**
	 * @return the drawPercent
	 */
	public boolean isDrawPercent() {
		return drawPercent;
	}

	/**
	 * @param drawPercent the drawPercent to set
	 */
	public void setDrawPercent(boolean drawPercent) {
		this.drawPercent = drawPercent;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.AbstractSceneNode#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer) {
		updateOrientation();
		displayed += timer.getDelta();
	}

	public void resetDrawn() {
		displayed = 0;
	}
}
