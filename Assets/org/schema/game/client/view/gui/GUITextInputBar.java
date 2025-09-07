package org.schema.game.client.view.gui;

import org.schema.game.client.controller.PlayerTextInputBar;
import org.schema.game.client.controller.manager.ingame.InventoryControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.common.TextCallback;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle;
import org.schema.schine.graphicsengine.forms.gui.GUITextInput;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.Vector4f;

public class GUITextInputBar extends GUIColoredRectangle {

	static final Vector4f inactiveC = new Vector4f(0.3f, 0.3f, 0.3f, 0.4f);

	static final Vector4f activeC = new Vector4f(0.4f, 0.4f, 0.4f, 0.7f);

	private boolean active;

	private final PlayerTextInputBar playerTextInputBar;

	private boolean inGUIDraw;

	private final GUITextInput textInput;

	public GUITextInputBar(InputState state, GUICallback guiCallback, int limit) {
		this(state, guiCallback, limit, 1);
	}

	public GUITextInputBar(InputState state, GUICallback guiCallback, int limit, int lineLimit) {
		super(state, 180, 20, new Vector4f());
		setMouseUpdateEnabled(true);
		setCallback(guiCallback);
		textInput = new GUITextInput(190, 20, state);
		textInput.setTextBox(true);
		playerTextInputBar = new PlayerTextInputBar((GameClientState) getState(), limit, lineLimit,this, textInput) {

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
				return null;
			}

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public void onDeactivate() {
			}

			@Override
			public boolean onInput(String entry) {
				return false;
			}
		};
		InventoryControllerManager inventoryControlManager = ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getInventoryControlManager();
		inventoryControlManager.playerTextInputBar = playerTextInputBar;
	}

	@Override
	public void cleanUp() {
		super.cleanUp();
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle#draw()
	 */
	@Override
	public void draw() {
		if (inGUIDraw) {
			InventoryControllerManager inventoryControlManager = ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getInventoryControlManager();
			inventoryControlManager.setInSearchbar(false);
			if (active != inventoryControlManager.isSearchActive()) {
				if (inventoryControlManager.isSearchActive()) {
					textInput.setDrawCarrier(true);
					playerTextInputBar.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(506);
				} else {
					playerTextInputBar.deactivate();
					textInput.setDrawCarrier(false);
				}
				active = inventoryControlManager.isSearchActive();
			}
			getColor().set(inventoryControlManager.isSearchActive() ? activeC : inactiveC);
			super.draw();
		}
	}

	@Override
	public void onInit() {
		getColor().set(inactiveC);
		textInput.setDrawCarrier(false);
		attach(textInput);
	}

	public String getText() {
		return playerTextInputBar.getText();
	}

	public void setText(String text) {
		playerTextInputBar.setText(text);
	}

	/**
	 * @return the inGUIDraw
	 */
	public boolean isInGUIDraw() {
		return inGUIDraw;
	}

	/**
	 * @param inGUIDraw the inGUIDraw to set
	 */
	public void setInGUIDraw(boolean inGUIDraw) {
		this.inGUIDraw = inGUIDraw;
	}

	public void reset() {
		playerTextInputBar.reset();
	}
}
