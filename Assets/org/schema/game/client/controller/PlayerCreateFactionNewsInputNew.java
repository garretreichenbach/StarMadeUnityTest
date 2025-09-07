package org.schema.game.client.controller;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.faction.newfaction.GUICreateNewsPostPanel;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.sound.controller.AudioController;

public class PlayerCreateFactionNewsInputNew extends PlayerInput {

	private GUICreateNewsPostPanel newsInputPanel;

	public PlayerCreateFactionNewsInputNew(GameClientState state, String predefinedTo, String predefinedTopic) {
		super(state);
		this.newsInputPanel = new GUICreateNewsPostPanel(state, this);
		newsInputPanel.setCallback(this);
	}

	public PlayerCreateFactionNewsInputNew(GameClientState state) {
		this(state, "", "");
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (!isOccluded()) {
			if (event.pressedLeftMouse()) {
				if (callingGuiElement.getUserPointer().equals("OK")) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(223);
					if (sendMail()) {
						deactivate();
					}
				}
				if (callingGuiElement.getUserPointer().equals("CANCEL") || callingGuiElement.getUserPointer().equals("X")) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(224);
					cancel();
				}
			}
		}
	}

	public String getSubject() {
		return newsInputPanel.getSubject().trim();
	}

	public String getMessage() {
		return newsInputPanel.getMessage().trim();
	}

	private boolean sendMail() {
		return getState().getPlayer().getFactionController().postNewsClient(getSubject(), getMessage());
	}

	@Override
	public GUIElement getInputPanel() {
		return newsInputPanel;
	}

	@Override
	public void onDeactivate() {
		newsInputPanel.cleanUp();
	}
}
