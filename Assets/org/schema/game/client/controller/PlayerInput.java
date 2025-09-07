package org.schema.game.client.controller;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.mainmenu.DialogInput;

public abstract class PlayerInput extends DialogInput {


	public PlayerInput(GameClientState state) {
		super(state);
		initialize();
	}

	@Override
	protected void initialize() {
		
	}
	
	

	@Override
	public GameClientState getState(){
		return (GameClientState)super.getState();
	}

}
