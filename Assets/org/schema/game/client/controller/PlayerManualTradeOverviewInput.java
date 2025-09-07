package org.schema.game.client.controller;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.manualtrade.GUIPlayerTradePlayerOverviewPanel;
import org.schema.game.client.view.gui.messagelog.GUIClientLogPanel;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.sound.controller.AudioController;

public class PlayerManualTradeOverviewInput extends PlayerInput implements GUIActiveInterface {

	private GUIPlayerTradePlayerOverviewPanel panel;

	public PlayerManualTradeOverviewInput(GameClientState state) {
		super(state);
		panel = new GUIPlayerTradePlayerOverviewPanel(state, 800, 500, this);
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (callingGuiElement.getUserPointer() != null && !callingGuiElement.wasInside() && callingGuiElement.isInside()) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.HOVER)*/
			AudioController.fireAudioEventID(245);
		}
		// System.err.println("CALLBACK: "+callingGuiElement.getUserPointer());
		if (event.pressedLeftMouse()) {
			if (callingGuiElement.getUserPointer().equals("OK")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(248);
				deactivate();
			} else if (callingGuiElement.getUserPointer().equals("CANCEL")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(247);
				deactivate();
			} else if (callingGuiElement.getUserPointer().equals("X")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(246);
				deactivate();
			} else {
				assert (false) : "not known command: '" + callingGuiElement.getUserPointer() + "'";
			}
		}
	}

	public String getCurrentChatPrefix() {
		if (GUIElement.isNewHud()) {
			return "";
		} else {
			return ((GUIClientLogPanel) getInputPanel()).getCurrentChatPrefix();
		}
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		super.handleKeyEvent(e);
	// nothing to do. just consume the key
	}

	@Override
	public boolean isActive() {
		return getState().getController().getPlayerInputs().isEmpty() || getState().getController().getPlayerInputs().get(getState().getController().getPlayerInputs().size() - 1).getInputPanel() == panel;
	}

	@Override
	public GUIElement getInputPanel() {
		return panel;
	}

	@Override
	public void onDeactivate() {
		panel.cleanUp();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.PlayerInput#allowChat()
	 */
	@Override
	public boolean allowChat() {
		return true;
	}

	public void setErrorMessage(String msg) {
		System.err.println(msg);
	}
}
