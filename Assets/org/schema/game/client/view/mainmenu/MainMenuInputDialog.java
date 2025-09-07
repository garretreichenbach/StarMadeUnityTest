package org.schema.game.client.view.mainmenu;

import org.schema.game.client.controller.GameMainMenuController;

public abstract class MainMenuInputDialog extends DialogInput implements MainMenuInputDialogInterface{
	public MainMenuInputDialog(GameMainMenuController state) {
		super(state);
	}

	@Override
	public GameMainMenuController getState(){
		return (GameMainMenuController)super.getState();
	}
	
	
}
