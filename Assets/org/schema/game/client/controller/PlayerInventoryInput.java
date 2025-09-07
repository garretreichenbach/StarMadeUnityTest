package org.schema.game.client.controller;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.inventory.inventorynew.InventoryPanelNew;
import org.schema.game.client.view.gui.inventory.inventorynew.SecondaryInventoryPanelNew;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.inventory.PersonalFactoryInventory;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.sound.controller.AudioController;

public class PlayerInventoryInput extends PlayerInput {

	private SecondaryInventoryPanelNew panel;

	private GUIActiveInterface actInterface;

	private InventoryPanelNew invPanel;

	public PlayerInventoryInput(String invId, GameClientState state, Object title, GUIActiveInterface actInterface, InventoryPanelNew invPanel, Inventory inventory, InventoryPanelNew mainPanel) {
		super(state);
		this.actInterface = actInterface;
		this.invPanel = invPanel;
		if (panel == null) {
			int initWidth = 520;
			int initHeight = 510;
			if (inventory instanceof PersonalFactoryInventory) {
				initWidth = 370;
				initHeight = 166 + 25;
			}
			panel = new SecondaryInventoryPanelNew(invId, state, title, initWidth, initHeight, inventory, this, actInterface, mainPanel);
		}
		panel.setCallback(this);
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (callingGuiElement.getUserPointer() != null && !callingGuiElement.wasInside() && callingGuiElement.isInside()) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.HOVER)*/
			AudioController.fireAudioEventID(231);
		}
		// System.err.println("CALLBACK: "+callingGuiElement.getUserPointer());
		if (event.pressedLeftMouse()) {
			if (callingGuiElement.getUserPointer().equals("OK")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(234);
				deactivate();
			} else if (callingGuiElement.getUserPointer().equals("CANCEL")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(233);
				deactivate();
			} else if (callingGuiElement.getUserPointer().equals("X")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(232);
				deactivate();
			} else {
				assert (false) : "not known command: '" + callingGuiElement.getUserPointer() + "'";
			}
		}
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
	// nothing to do. just consume the key
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.PlayerInput#deactivate()
	 */
	@Override
	public void deactivate() {
		invPanel.deactivate(this);
	}

	@Override
	public SecondaryInventoryPanelNew getInputPanel() {
		return panel;
	}

	@Override
	public void onDeactivate() {
		invPanel.deactivateSecondary(panel);
	}

	@Override
	public boolean isOccluded() {
		return !actInterface.isActive();
	}

	public void setErrorMessage(String msg) {
		System.err.println(msg);
	}

	public void clearFilter() {
		panel.getInventoryIcons().clearFilter();
	}
}
