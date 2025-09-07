package org.schema.game.client.view.mainmenu;

import org.schema.game.client.controller.GameMainMenuController;

public interface MainMenuInputDialogInterface {
	public abstract boolean isInside();
	
	public GameMainMenuController getState();
}
