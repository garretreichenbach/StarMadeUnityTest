package org.schema.game.client.controller;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.common.JoystickInputHandler;

public abstract class PlayerJoystickInput extends PlayerInput implements JoystickInputHandler {

	public PlayerJoystickInput(GameClientState state) {
		super(state);
	}

}
