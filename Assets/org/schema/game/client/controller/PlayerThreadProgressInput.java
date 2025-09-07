package org.schema.game.client.controller;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIProgressBar;
import org.schema.schine.sound.controller.AudioController;

public abstract class PlayerThreadProgressInput extends PlayerInput {

	private final GUIInputPanel inputPanel;

	private ThreadCallback threadCallback;

	public PlayerThreadProgressInput(String windowId, GameClientState state, Object info, Object description, final ThreadCallback threadCallback) {
		super(state);
		inputPanel = new GUIInputPanel(windowId, state, this, info, description);
		inputPanel.setCallback(this);
		inputPanel.setOkButton(false);
		inputPanel.setCancelButton(false);
		this.threadCallback = threadCallback;
		inputPanel.onInit();
		GUIProgressBar b = new GUIProgressBar(state, 400, 10, "", false, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		}) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUIProgressBar#draw()
			 */
			@Override
			public void draw() {
				setPercent(threadCallback.getPercent());
				super.draw();
			}
		};
		b.getPos().y = 30;
		inputPanel.getContent().attach(b);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.PlayerInput#checkDeactivated()
	 */
	@Override
	public boolean checkDeactivated() {
		if (this.threadCallback.isFinished()) {
			return true;
		}
		return super.checkDeactivated();
	}

	@Override
	public GUIInputPanel getInputPanel() {
		return inputPanel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.schema.schine.graphicsengine.forms.gui.GUICallback#callback(org.schema
	 * .schine.graphicsengine.forms.gui.GUIElement)
	 */
	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (!isOccluded() && threadCallback.isFinished()) {
			if (event.pressedLeftMouse()) {
				if (callingGuiElement.getUserPointer().equals("OK")) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(275);
					pressedOK();
				}
				if (callingGuiElement.getUserPointer().equals("CANCEL") || callingGuiElement.getUserPointer().equals("X")) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(276);
					cancel();
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.PlayerInput#isDeactivateOnEscape()
	 */
	@Override
	public boolean isDeactivateOnEscape() {
		return false;
	}

	@Override
	public abstract void onDeactivate();

	public abstract void pressedOK();

	public void setErrorMessage(String msg) {
		inputPanel.setErrorMessage(msg, 2000);
	}
}
