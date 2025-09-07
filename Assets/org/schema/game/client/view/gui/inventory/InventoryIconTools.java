package org.schema.game.client.view.gui.inventory;

import org.schema.game.client.controller.manager.ingame.InventoryControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUITextInputBar;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class InventoryIconTools extends GUIAnchor implements GUICallback, InventoryToolInterface {

	private GUITextOverlayTable info;

	private GUITextInputBar searchBar;

	private GUITextButton reset;

	public InventoryIconTools(InputState state) {
		super(state);
		searchBar = new GUITextInputBar(state, this, 30);
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		InventoryControllerManager inventoryControlManager = ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getInventoryControlManager();
		if (event.pressedLeftMouse()) {
			inventoryControlManager.setSearchActive(true);
		}
		inventoryControlManager.setInSearchbar(true);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#draw()
	 */
	@Override
	public void draw() {
		searchBar.setInGUIDraw(true);
		super.draw();
		searchBar.setInGUIDraw(false);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();
		searchBar.onInit();
		reset = new GUITextButton(getState(), 15, 15, "x", new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(510);
					clearFilter();
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		}) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
			 */
			@Override
			public void draw() {
				if (getText().length() > 0) {
					super.draw();
				}
			}
		};
		reset.setTextPos(3, -3);
		info = new GUITextOverlayTable(getState());
		info.setTextSimple("Search: ");
		searchBar.getPos().x = info.getWidth();
		reset.getPos().x = info.getWidth() + searchBar.getWidth() + 2;
		attach(info);
		attach(searchBar);
		attach(reset);
	}

	/**
	 * @return
	 * @see org.schema.game.client.view.gui.GUITextInputBar#getText()
	 */
	@Override
	public String getText() {
		return searchBar.getText();
	}

	@Override
	public boolean isActiveInventory(InventoryIconsNew inventoryIcons) {
		return true;
	}

	@Override
	public void clearFilter() {
		searchBar.reset();
	}
}
