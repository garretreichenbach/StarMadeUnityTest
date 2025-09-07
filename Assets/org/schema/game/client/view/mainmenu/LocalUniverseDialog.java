package org.schema.game.client.view.mainmenu;

import org.schema.game.client.controller.GameMainMenuController;
import org.schema.game.client.view.mainmenu.gui.GUILocalUniversePanel;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;

public class LocalUniverseDialog extends MainMenuInputDialog{

	private final GUILocalUniversePanel p;
	public LocalUniverseDialog(GameMainMenuController state) {
		super(state);
		p = new GUILocalUniversePanel(state, this);
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
