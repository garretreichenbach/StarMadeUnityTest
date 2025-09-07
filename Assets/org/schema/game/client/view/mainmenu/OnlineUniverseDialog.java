package org.schema.game.client.view.mainmenu;

import org.schema.game.client.controller.GameMainMenuController;
import org.schema.game.client.view.mainmenu.gui.GUIOnlineUniversePanel;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;

public class OnlineUniverseDialog extends MainMenuInputDialog{

	private final GUIOnlineUniversePanel p;
	public OnlineUniverseDialog(GameMainMenuController state) {
		super(state);
		p = new GUIOnlineUniversePanel(state, this);
		p.onInit();
	}


	@Override
	public GUIElement getInputPanel() {
		return p;
	}

	@Override
	public void onDeactivate() {
		p.cleanUp();
	}
	
	
	@Override
	public boolean isInside(){
		return p.isInside();
	}

}
