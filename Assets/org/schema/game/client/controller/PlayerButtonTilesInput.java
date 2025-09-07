package org.schema.game.client.controller;

import org.schema.game.client.view.gui.GUIInputContentSizeInterface;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonColor;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITilePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public abstract class PlayerButtonTilesInput extends DialogInput {

	private GUIInputPanel inputPanel;

	private GUITilePane<Object> tiles;

	public PlayerButtonTilesInput(String windowid, InputState state, Object info, int tilesWidth, int tilesHeight) {
		super(state);
		init(windowid, UIScale.getUIScale().scale(400), UIScale.getUIScale().scale(300), state, info, tilesWidth, tilesHeight);
	}

	public PlayerButtonTilesInput(String windowid, InputState state, int initialWidth, int initialHeight, Object info, int tilesWidth, int tilesHeight) {
		super(state);
		init(windowid, initialWidth, initialHeight, state, info, tilesWidth, tilesHeight);
	}

	protected void init(String windowid, int initialWidth, int initialHeight, InputState state, Object info, int tilesWidth, int tilesHeight) {
		inputPanel = new GUIInputPanel(windowid, state, initialWidth, initialHeight, this, info, "");
		inputPanel.setCallback(this);
		inputPanel.setOkButton(false);
		inputPanel.setCancelButton(false);
		// no padding for ok/cancel
		((GUIDialogWindow) inputPanel.getBackground()).innerHeightSubstraction = UIScale.getUIScale().W_innerHeightSubstraction_noPadding;
		inputPanel.onInit();
		inputPanel.getContent().onInit();
		tiles = new GUITilePane<Object>(state, inputPanel.getBackground(), tilesWidth, tilesHeight);
		tiles.activeInterface = () -> inputPanel.getBackground().activeInterface == null || inputPanel.getBackground().activeInterface.isActive();
		inputPanel.contentInterface = new GUIInputContentSizeInterface() {

			@Override
			public int getWidth() {
				return (int) tiles.getWidth();
			}

			@Override
			public int getHeight() {
				return (int) tiles.getHeight();
			}
		};
		assert (!inputPanel.getChilds().contains(tiles));
		inputPanel.setContentInScrollable(tiles);
	}

	public boolean isInside() {
		return inputPanel != null && inputPanel.getBackground() != null && inputPanel.getBackground().isInside();
	}

	public void addTile(String tile, String description, HButtonColor type, GUICallback callback, GUIActivationCallback actCallback) {
		tiles.addButtonTile(tile, description, type, callback, actCallback);
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
		if (!isOccluded()) {
			if (event.pressedLeftMouse()) {
				if (callingGuiElement.getUserPointer().equals("OK")) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(216);
				}
				if (callingGuiElement.getUserPointer().equals("CANCEL") || callingGuiElement.getUserPointer().equals("X")) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(217);
					cancel();
				}
			}
		}
	}

	@Override
	public GUIInputPanel getInputPanel() {
		return inputPanel;
	}

	@Override
	public abstract void onDeactivate();

	public void setErrorMessage(String msg) {
		inputPanel.setErrorMessage(msg, 2000);
	}
}
