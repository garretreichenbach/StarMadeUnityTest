package org.schema.game.client.controller.manager.ingame.faction;

import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;

public class FactionHubControlManager extends AbstractControlManager implements GUICallback {

	public FactionHubControlManager(GameClientState state) {
		super(state);
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {

	}
	

}
