package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.gui.ColoredInterface;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector4f;

public class GUIHelperIcon extends GUIElement{
	
	private final GUIHelperTextureType type;
	private boolean init;
	private GUITextOverlay on;
	private int afterTextDist() {
		return UIScale.getUIScale().scale( 15);
	}
	private int afterTextYMod() {
		return UIScale.getUIScale().scale( 0);
	}
	private int insideTextPosX() {
		return UIScale.getUIScale().scale(12);
	}
	private int insideTextPosY(){
		return UIScale.getUIScale().scale(12);
	}
	
	
	public float iconScale = 0.5f;
	private GUITextOverlay after;
	public ColoredInterface colorInterface;
	public GUIHelperIcon(InputState state, GUIHelperTextureType type, FontInterface fontOn, FontInterface fontAfter) {
		super(state);
		this.type = type;
		this.on = new GUITextOverlay(fontOn, getState()); 
		this.after = new GUITextOverlay(fontAfter, getState()); 
	}

	public void setColor(final Vector4f color){
		this.colorInterface = () -> new Vector4f(color);
	}
	
	@Override
	public void cleanUp() {
		if(on != null){
			on.cleanUp();
		}
		if(after != null){
			after.cleanUp();
		}
	}
	public void setTextOn(Object onTxt){
		setTextOn(onTxt, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
	}
	
	public void setTextOn(final Object onTxt, final Vector4f color){
		if(on.getText() == null || on.getText().isEmpty() || !on.getText().get(0).equals(onTxt)){
			on.setTextSimple(new ColoredInterface() {
				@Override
				public Vector4f getColor() {
					return color;
				}

				@Override
				public String toString() {
					return onTxt.toString();
				}	
			});
		}
	}
	
	public void setTextAfter(Object afterTxt){
		setTextAfter(afterTxt, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
	}
	
	public void setTextAfter(final Object afterTxt, final Vector4f color){
		if(after.getText() == null || after.getText().isEmpty() || !after.getText().get(0).equals(afterTxt)){
			after.setTextSimple(new ColoredInterface() {
				@Override
				public Vector4f getColor() {
					return color;
				}

				@Override
				public String toString() {
					return afterTxt.toString();
				}	
			});
		}
	}
	@Override
	public void draw() {
		if(!init){
			onInit();
		}
		
		GUITexDrawableArea guiTexDrawableArea = type.get(getState());
		
		assert(!getChilds().isEmpty());
		on.setPos((int)(insideTextPosX() * iconScale), (int)(insideTextPosY()* iconScale), 0);
		after.setPos((int)((float)guiTexDrawableArea.getWidth()*(float)iconScale+afterTextDist()), (int)(((float)guiTexDrawableArea.getHeight()*iconScale)/2-after.getTextHeight()/2)+afterTextYMod());
		GlUtil.glPushMatrix();
		transform();
		GlUtil.scaleModelview(iconScale, iconScale, 1);
		if(isMouseUpdateEnabled()){
			checkMouseInside();
		}
		
		if(colorInterface != null){
			guiTexDrawableArea.setColor(colorInterface.getColor());
		}else{
			guiTexDrawableArea.setColor(null);
		}
		guiTexDrawableArea.draw();
		guiTexDrawableArea.setColor(null);
		GlUtil.glColor4f(1,1,1,1);
		GlUtil.glPopMatrix();
		
		drawAttached();
		
	}

	@Override
	public void onInit() {
		if(init){
			return;
		}
		
		
		attach(on);
		attach(after);
		
		init = true;
	}

	@Override
	public float getWidth() {
		if(after.getText().isEmpty() || after.getText().get(0).toString().trim().isEmpty()){
			return (int)((type.get(getState()).getWidth()*iconScale));
		}else{
			return ((type.get(getState()).getWidth()*iconScale)+afterTextDist()+after.getMaxLineWidth());
		}
	}

	@Override
	public float getHeight() {
		if(type == GUIHelperTextureType.NONE){
			return (int) after.getHeight() * 2;
		} else {
			return (int)(type.get(getState()).getHeight()*iconScale);
		}
		
	}
	
}
