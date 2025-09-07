package org.schema.game.client.controller;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.graphicsengine.forms.font.FontStyle;

public abstract class PlayerGameOkCancelInput extends PlayerOkCancelInput {

	public PlayerGameOkCancelInput(String windowid, GameClientState state,
			int initialWidth, int initialHeight, Object info,
			Object description, FontStyle style) {
		super(windowid, state, initialWidth, initialHeight, info, description, style);
	}

	public PlayerGameOkCancelInput(String windowid, GameClientState state,
			int initialWidth, int initialHeight, Object info, Object description) {
		super(windowid, state, initialWidth, initialHeight, info, description);
	}

	public PlayerGameOkCancelInput(String windowid, GameClientState state,
			Object info, Object description, FontStyle style) {
		super(windowid, state, info, description, style);
	}

	public PlayerGameOkCancelInput(String windowid, GameClientState state,
			Object info, Object description) {
		super(windowid, state, info, description);
	}

	@Override
	public GameClientState getState() {
		return (GameClientState)super.getState();
	}

	

}
