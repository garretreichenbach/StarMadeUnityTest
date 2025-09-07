package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.texture.Texture;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector2f;

public enum GUIHelperTextureType {
	NONE(0,0,0, 1, false),
	SINGLE(0,0,1f, 1f, true),
	ONEANDHALF(1,0,2f, 1f, true),
	TWO(3,0,2f, 1f, true),
	MOUSE_LEFT(5,0,1f, 1f, false),
	MOUSE_RIGHT(6,0,1f, 1f, false),
	MOUSE_MID(7,0,1f, 1f, false),
	MOUSE_WUP(8,0,1f, 1f, false),
	MOUSE_WDOWN(9,0,1f, 1f, false),
	;
	public static final String tex = UIScale.getUIScale().getGuiPath()+"helpers-16x16-gui-";
	private final int indexX;
	public final boolean hasText;
	private final int indexY;
	private final float widthScale;
	private final float heightScale;
	private GUIHelperTextureType(int indexX, int indexY, float width, float height, boolean hasText){
		this.indexX = indexX;
		this.indexY = indexY;
		this.widthScale = width;
		this.heightScale = height;
		this.hasText = hasText;
	}
	
	private static final GUITexDrawableArea[] buffer = new GUITexDrawableArea[GUIHelperTextureType.values().length];
	
	public static Texture getTexture(){
		return getSprite().getMaterial().getTexture();
	}
	public static Sprite getSprite(){
		return Controller.getResLoader().getSprite(tex);
	}
	public GUITexDrawableArea get(InputState state){
		if(buffer[ordinal()] == null){
			float xOffset = ((float)UIScale.getUIScale().ICON_SIZE / (float)getTexture().getWidth())*indexX;
			float yOffset = ((float)UIScale.getUIScale().ICON_SIZE / (float)getTexture().getWidth())*indexY;
			GUITexDrawableArea g = new GUITexDrawableArea(state, getTexture(), xOffset, yOffset);
			g.setWidth((int) (widthScale * (float)UIScale.getUIScale().ICON_SIZE));
			g.setHeight((int) (heightScale * (float)UIScale.getUIScale().ICON_SIZE));
			buffer[ordinal()] = g;
		}
		return buffer[ordinal()];
	}
	public Vector2f getCenter(Vector2f out){
		out.set((int)(widthScale*(float)UIScale.getUIScale().ICON_SIZE*0.5f), (int)(heightScale*(float)UIScale.getUIScale().ICON_SIZE*0.5f));
		return out;
	}
}
