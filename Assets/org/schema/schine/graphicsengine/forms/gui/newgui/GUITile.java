package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.input.InputState;

public class GUITile extends GUIInnerTextbox{

	
	protected Object userData;
	

	public GUITile(InputState state, float width, float height, Object userData) {
		super(state);
		this.setWidth(width);
		this.setHeight(height);
		this. userData = userData;
		onInit();
	}


	
}
