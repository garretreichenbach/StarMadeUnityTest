package org.schema.game.client.view.gui;

import org.schema.game.client.controller.manager.HelpManager;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.input.InputState;

public class HelpPanel extends GUIElement {

	private boolean expanded;
	private GUIOverlay helpButton;

	public HelpPanel(InputState state) {
		super(state);
	}

	@Override
	public void cleanUp() {
		
	}

	@Override
	public void draw() {
		helpButton.draw();
	}

	@Override
	public void onInit() {
		this.helpButton = new GUIOverlay(Controller.getResLoader().getSprite("help-gui-"), getState());
		this.helpButton.orientate(ORIENTATION_LEFT | ORIENTATION_BOTTOM);
		this.helpButton.onInit();
		this.helpButton.getPos().y -= 64;

	}

	@Override
	public float getHeight() {
				return 0;
	}

	@Override
	public float getWidth() {
				return 0;
	}

	@Override
	public boolean isPositionCenter() {
				return false;
	}

	public HelpManager getHelpManager() {
		return ((GameClientState) getState()).getGlobalGameControlManager().getHelpManager();
	}

	public PlayerInteractionControlManager getInteractionManager() {
		return ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager()
				.getPlayerIntercationManager();
	}

	/**
	 * @return the expanded
	 */
	public boolean isExpanded() {
		return expanded;
	}

	/**
	 * @param expanded the expanded to set
	 */
	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

}
