package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.input.InputState;

public class GUITileParam<E> extends GUITile{

	

	public GUITileParam(InputState state, float width, float height, E userData) {
		super(state, width, height, userData);
		
	}


	public E getUserData(){
		return (E)userData;
	}
	
}
