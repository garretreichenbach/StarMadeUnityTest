package org.schema.schine.graphicsengine.forms.gui.newgui;

import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIResizableElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUIToolTip;
import org.schema.schine.graphicsengine.forms.gui.TooltipProviderCallback;
import org.schema.schine.input.InputState;

public abstract class GUIProgressBarDynamic extends GUIResizableElement implements TooltipProviderCallback{
	private GUIElement dependent;
	private GUIProgressBarDynamicFillableBackground background;
	private GUIProgressBarDynamicFillableEmpty empty;
	private GUIProgressBarDynamicFillableFilled filled;
	private GUITextOverlay textOverlay;
	private final Vector4f progressClip = new Vector4f();
	private float animValue;
	private boolean isAnimated;
	private Vector4f startColor;
	private Vector4f endColor;
	public GUIProgressBarDynamic(InputState state, int width, int height) {
		super(state);
		this.width = width;
		this.height = height;
	}
	public GUIProgressBarDynamic(InputState state, GUIElement dependent) {
		super(state);
		this.dependent = dependent;
		this.width = (int) dependent.getWidth();
		this.height = (int) dependent.getHeight();
	}
	public abstract FontInterface getFontSize();
	public abstract String getLabelText();
	public abstract float getValue();
	
	private int width;
	private int height;
	private boolean init;
	private GUIToolTip toolTip;
	@Override
	public void cleanUp() {
		
	}
	
	public void setBackgroundColor(Vector4f c){
		background.setColor(c);
	}
	public void setEmptyColor(Vector4f c){
		empty.setColor(c);
	}
	public void setFilledColor(Vector4f c){
		setFilledColor(c, c);
	}
	public void setFilledColor(Vector4f startColor, Vector4f endColor){
		this.startColor = startColor;
		this.endColor = endColor;
		filled.setColor(startColor);
	}
	
	@Override
	public GUIToolTip getToolTip() {
		return toolTip;
	}
	@Override
	public void setToolTip(GUIToolTip toolTip) {
		this.toolTip = toolTip;
	}
	@Override
	public void draw() {
		if(isInvisible()){
			return;
		}
		if(!init){
			onInit();
		}
		if(dependent != null){
			this.width = (int) dependent.getWidth();
			this.height = (int) dependent.getHeight();
		}
		if(toolTip != null){
			setMouseUpdateEnabled(true);
		}
		background.setWidth(width);
		background.setHeight(height);
		
		int inset = 4;
		int innerWidth = width-inset*2;
		int innerHeight = height-inset*2+2;
		empty.setWidth(innerWidth);
		empty.setHeight(innerHeight);
		
		empty.getPos().x = inset;
		empty.getPos().y = inset;
		
		
		filled.setWidth(innerWidth);
		filled.setHeight(innerHeight);
		
		filled.getPos().x = inset;
		filled.getPos().y = inset;
		
		
		GlUtil.glPushMatrix();
		
		transform();
		setInside(false);
		if(isMouseUpdateEnabled()){
			checkMouseInside();
		}
		
		background.draw();
		empty.draw();
		
		float value = Math.max(0f, Math.min(1f, getValue()));

		if (isAnimated() && Math.abs(value - animValue) > 0.002f) {
			float sign = Math.signum(value - animValue) * 0.002f;
			animValue = Math.max(0f, Math.min(1f, animValue + sign));
			
			filled.setColor(new Vector4f(
				startColor.x + Math.signum(endColor.x - startColor.x) * animValue, 
				startColor.y + Math.signum(endColor.y - startColor.y) * animValue, 
				startColor.z + Math.signum(endColor.z - startColor.z) * animValue, 
				startColor.w + Math.signum(endColor.w - startColor.w) * animValue));
			progressClip.set(inset, (int) ((animValue) * innerWidth), 0, innerHeight);
		} else {
			filled.setColor(new Vector4f(
				startColor.x + Math.signum(endColor.x - startColor.x) * animValue, 
				startColor.y + Math.signum(endColor.y - startColor.y) * animValue, 
				startColor.z + Math.signum(endColor.z - startColor.z) * animValue, 
				startColor.w + Math.signum(endColor.w - startColor.w) * animValue));
			progressClip.set(inset, (int) ((getValue()) * innerWidth), 0, innerHeight);
		}

		
		GlUtil.pushClipSubtract(progressClip);
		filled.draw();
		GlUtil.popClip();
		
		String labelText = getLabelText();
		if(labelText != null && labelText.length() > 0){
			int tWidth = textOverlay.getMaxLineWidth();
			int tHeight = textOverlay.getTextHeight();
			
			textOverlay.setPos(Math.max(2, (int)(getWidth()/2-tWidth/2)), (int)(getHeight()/2-tHeight/2), 0);
			textOverlay.draw();
		}
		
		
		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		if(init){
			return;
		}
		
		background = new GUIProgressBarDynamicFillableBackground(getState(), width, height);
		empty = new GUIProgressBarDynamicFillableEmpty(getState(), width, height);
		filled = new GUIProgressBarDynamicFillableFilled(getState(), width, height);
		textOverlay = new GUITextOverlay(getFontSize(), getState());
		textOverlay.setTextSimple(new Object(){

			@Override
			public String toString() {
				String s = getLabelText();
				return s == null ? "" : s;
			}
			
		});
		animValue = getValue();
		isAnimated = false;
		
		init = true;
	}

	@Override
	public void setWidth(float width) {
		this.width = (int)width;		
	}

	@Override
	public void setHeight(float height) {
		this.height = (int)height;
	}

	@Override
	public float getWidth() {
		return this.width;
	}

	@Override
	public float getHeight() {
		return this.height;
	}
	
	public float getAnimValue() {
		return this.animValue;
	}
	
	@Override
	public boolean isAnimated() {
		return isAnimated;
	}

}
