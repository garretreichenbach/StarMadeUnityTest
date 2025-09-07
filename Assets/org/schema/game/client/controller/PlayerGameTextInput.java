package org.schema.game.client.controller;

import org.schema.game.client.data.GameClientState;

public abstract class PlayerGameTextInput extends PlayerTextInput  {

	public PlayerGameTextInput(String windowId, GameClientState state, int width,
			int height, int limit, Object info, Object description,
			String predefined) {
		super(windowId, state, width, height, limit, info, description, predefined);
	}

	public PlayerGameTextInput(String windowId, GameClientState state, int width,
			int height, int limit, Object info, Object description) {
		super(windowId, state, width, height, limit, info, description);
	}

	public PlayerGameTextInput(String windowId, GameClientState state, int limit,
			Object info, Object description, String predefined) {
		super(windowId, state, limit, info, description, predefined);
	}

	public PlayerGameTextInput(String windowId, GameClientState state, int limit,
			Object info, Object description) {
		super(windowId, state, limit, info, description);
	}

	@Override
	public GameClientState getState() {
		return (GameClientState)super.getState();
	}


}
