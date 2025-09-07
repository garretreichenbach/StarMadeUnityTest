package org.schema.game.client.controller;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.catalog.newcatalog.GUIBlockStorageMetaItemFillScrollableList;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.BlockStorageMetaItem;
import org.schema.game.common.data.player.SimplePlayerCommands;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class PlayerBlockStorageMetaDialog extends PlayerGameOkCancelInput {

	private BlockStorageMetaItem it;

	private boolean useOwnFaction;

	public final Inventory openedFrom;

	public PlayerBlockStorageMetaDialog(final GameClientState state, final BlockStorageMetaItem it, final Inventory openedFrom) {
		super("PlayerStorageMetaDialogSS", state, UIScale.getUIScale().scale(600), UIScale.getUIScale().scale(400), Lng.str("Block Storage Item"), "");
		this.it = it;
		getInputPanel().onInit();
		this.openedFrom = openedFrom;
		useOwnFaction = getState().getPlayer().getFactionId() > 0;
		((GUIDialogWindow) getInputPanel().background).getMainContentPane().setTextBoxHeightLast(UIScale.getUIScale().scale(25));
		((GUIDialogWindow) getInputPanel().background).getMainContentPane().addNewTextBox(UIScale.getUIScale().scale(40));
		GUIHorizontalButtonTablePane buttons = new GUIHorizontalButtonTablePane(state, 2, 1, ((GUIDialogWindow) getInputPanel().background).getMainContentPane().getContent(0));
		buttons.onInit();
		((GUIDialogWindow) getInputPanel().background).getMainContentPane().getContent(0).attach(buttons);
		buttons.addButton(0, 0, Lng.str("GET ALL BLOCKS"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !PlayerBlockStorageMetaDialog.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(204);
					PlayerBlockStorageMetaDialog.this.pressedGetAll(openedFrom);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return !((GameClientState) state).getPlayer().getInventory().isFull();
			}
		});
		buttons.addButton(1, 0, Lng.str("ADD ALL BLOCKS (Admin)"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !PlayerBlockStorageMetaDialog.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(205);
					PlayerBlockStorageMetaDialog.this.pressedAddAll(openedFrom);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return ((GameClientState) state).getPlayer().isAdmin();
			}

			@Override
			public boolean isActive(InputState state) {
				return true;
			}
		});
		GUIBlockStorageMetaItemFillScrollableList l = new GUIBlockStorageMetaItemFillScrollableList(getState(), ((GUIDialogWindow) getInputPanel().background).getMainContentPane().getContent(1), this, getItem());
		l.onInit();
		((GUIDialogWindow) getInputPanel().background).getMainContentPane().getContent(1).attach(l);
		getInputPanel().setCancelButton(false);
		getInputPanel().setOkButtonText(Lng.str("DONE"));
	}

	public BlockStorageMetaItem getItem() {
		// reRequest becuase else update doesnt refresh counts
		return (BlockStorageMetaItem) getState().getMetaObjectManager().getObject(it.getId());
	}

	public void pressedGetAll(final Inventory openedFrom) {
		(new PlayerGameOkCancelInput("PlayerStorageMetaDialog_GET_ALL", getState(), Lng.str("Confirm"), Lng.str("This gets as many items as capacity allows in your inventory.")) {

			@Override
			public void onDeactivate() {
				PlayerBlockStorageMetaDialog.this.deactivate();
			}

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void pressedOK() {
				getState().getPlayer().sendSimpleCommand(SimplePlayerCommands.GET_BLOCK_STORAGE_META_ALL, getItem().getId(), openedFrom.getInventoryHolder().getId(), openedFrom.getParameterX(), openedFrom.getParameterY(), openedFrom.getParameterZ());
				deactivate();
			}
		}).activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(206);
	}

	public void pressedAddAll(final Inventory openedFrom) {
		(new PlayerGameOkCancelInput("PlayerStorageMetaDialog_ADD_ALL", getState(), Lng.str("Confirm"), Lng.str("This puts all the blocks of your inventory in this storage\n(Admin only)")) {

			@Override
			public void onDeactivate() {
			}

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void pressedOK() {
				getState().getPlayer().sendSimpleCommand(SimplePlayerCommands.ADD_BLOCK_STORAGE_META_ALL, getItem().getId(), openedFrom.getInventoryHolder().getId(), openedFrom.getParameterX(), openedFrom.getParameterY(), openedFrom.getParameterZ());
				deactivate();
			}
		}).activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(207);
	}

	public void pressedGet(final short type) {
		(new PlayerGameTextInput("PlayerBlueprintMetaDialog_ADD", getState(), 20, Lng.str("Get Blocks"), Lng.str("How many %s do you want to get", ElementKeyMap.getInfo(type).getName())) {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
				return null;
			}

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}

			@Override
			public boolean onInput(String entry) {
				if (entry.length() < 1) {
					getState().getController().popupAlertTextMessage(Lng.str("Please enter a valid\nnumber!"), 0);
					return false;
				}
				try {
					int count = Integer.parseInt(entry.trim());
					short invType = type;
					if (ElementKeyMap.getInfoFast(type).getSourceReference() != 0) {
						invType = (short) ElementKeyMap.getInfoFast(type).getSourceReference();
					}
					int have = getItem().storage.get(invType);
					if (have < count) {
						getTextInput().clear();
						getTextInput().append(String.valueOf(have));
						getState().getController().popupAlertTextMessage(Lng.str("You only have %s", have), 0);
						return false;
					}
					getState().getPlayer().sendSimpleCommand(SimplePlayerCommands.GET_BLOCK_STORAGE_META_SINGLE, getItem().getId(), type, count, openedFrom.getInventoryHolder().getId(), openedFrom.getParameterX(), openedFrom.getParameterY(), openedFrom.getParameterZ());
				} catch (NumberFormatException e) {
					getState().getController().popupAlertTextMessage(Lng.str("Please enter a valid\nnumber!"), 0);
					return false;
				}
				return true;
			}

			@Override
			public void onDeactivate() {
			}
		}).activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(208);
	}

	public void pressedAdd(final short type) {
		(new PlayerGameTextInput("PlayerBlueprintMetaDialog_ADD", getState(), 20, Lng.str("Add"), Lng.str("How many %s do you want to add?\n(Admin only)", ElementKeyMap.getInfo(type).getName())) {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
				return null;
			}

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}

			@Override
			public boolean onInput(String entry) {
				if (entry.length() < 1) {
					getState().getController().popupAlertTextMessage(Lng.str("Please enter a valid\nnumber!"), 0);
					return false;
				}
				try {
					int count = Integer.parseInt(entry.trim());
					short invType = type;
					if (ElementKeyMap.getInfoFast(type).getSourceReference() != 0) {
						invType = (short) ElementKeyMap.getInfoFast(type).getSourceReference();
					}
					int have = getState().getPlayer().getInventory().getOverallQuantity(invType);
					if (have < count) {
						getTextInput().clear();
						getTextInput().append(String.valueOf(have));
						getState().getController().popupAlertTextMessage(Lng.str("You only have %s", have), 0);
						return false;
					}
					getState().getPlayer().sendSimpleCommand(SimplePlayerCommands.ADD_BLOCK_STORAGE_META_SINGLE, getItem().getId(), type, count, openedFrom.getInventoryHolder().getId(), openedFrom.getParameterX(), openedFrom.getParameterY(), openedFrom.getParameterZ());
				} catch (NumberFormatException e) {
					getState().getController().popupAlertTextMessage(Lng.str("Please enter a valid\nnumber!"), 0);
					return false;
				}
				return true;
			}

			@Override
			public void onDeactivate() {
			}
		}).activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(209);
	}

	@Override
	public void onDeactivate() {
	}

	@Override
	public void pressedOK() {
		deactivate();
	}
}
