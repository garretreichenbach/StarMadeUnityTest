package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

public class GUITileButtonDesc<E> extends GUITileParam<E>{

	
	public OnDrawInterface onDrawInterface;
	public GUIHorizontalButton button;
	public GUITextOverlay descriptionText;
	protected Object userData;
	

	public GUITileButtonDesc(InputState state, float width, float height) {
		super(state, width, height, null);
	}

	@Override
	public void draw() {
		if(onDrawInterface != null && button != null & descriptionText != null){
			onDrawInterface.onDraw(button, descriptionText);
		}
		super.draw();
	}


	
}
