package org.schema.game.client.controller;

import java.util.Collection;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.graphicsengine.forms.gui.DropDownCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;

public abstract class PlayerGameDropDownInput extends PlayerDropDownInput implements DropDownCallback {

	
	public PlayerGameDropDownInput(String windowId, GameClientState state,
			int intitialWidth, int initialHeight, Object info, int height,
			int style, Object description, GUIElement... elements) {
		super(windowId, state, intitialWidth, initialHeight, info, height, style,
				description, elements);
	}

	public PlayerGameDropDownInput(String windowId, GameClientState state,
			int intitialWidth, int initialHeight, Object info, int height,
			Object description, Collection<? extends GUIElement> elements) {
		super(windowId, state, intitialWidth, initialHeight, info, height, description,
				elements);
	}

	public PlayerGameDropDownInput(String windowId, GameClientState state,
			int intitialWidth, int initialHeight, Object info, int height,
			Object description, GUIElement... elements) {
		super(windowId, state, intitialWidth, initialHeight, info, height, description,
				elements);
	}

	public PlayerGameDropDownInput(String windowId, GameClientState state,
			int intitialWidth, int initialHeight, Object info, int height) {
		super(windowId, state, intitialWidth, initialHeight, info, height);
	}

	public PlayerGameDropDownInput(String windowId, GameClientState state,
			Object info, int height, Object description,
			Collection<? extends GUIElement> elements) {
		super(windowId, state, info, height, description, elements);
	}

	public PlayerGameDropDownInput(String windowId, GameClientState state,
			Object info, int height, Object description, GUIElement... elements) {
		super(windowId, state, info, height, description, elements);
	}

	public PlayerGameDropDownInput(String windowId, GameClientState state,
			Object info, int height) {
		super(windowId, state, info, height);
	}

	@Override
	public GameClientState getState() {
		return (GameClientState)super.getState();
	}

	

	

}
