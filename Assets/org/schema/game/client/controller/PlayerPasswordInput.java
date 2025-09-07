package org.schema.game.client.controller;

import org.schema.game.client.data.GameClientState;

public abstract class PlayerPasswordInput extends PlayerGameTextInput {

	public PlayerPasswordInput(String windowId, GameClientState state,
	                           int width, int height, int limit, Object info, Object description,
	                           String predefined) {
		super(windowId, state, width, height, limit, info, description, predefined);

		inputPanel.setDisplayAsPassword(true);
	}

	public PlayerPasswordInput(String windowId, GameClientState state,
	                           int width, int height, int limit, Object info, Object description) {
		super(windowId, state, width, height, limit, info, description);

		inputPanel.setDisplayAsPassword(true);
	}

	public PlayerPasswordInput(String windowId, GameClientState state,
	                           int limit, Object info, Object description, String predefined) {
		super(windowId, state, limit, info, description, predefined);

		inputPanel.setDisplayAsPassword(true);
	}

	public PlayerPasswordInput(String windowId, GameClientState state,
	                           int limit, Object info, Object description) {
		super(windowId, state, limit, info, description);

		inputPanel.setDisplayAsPassword(true);
	}

}
