package org.schema.game.client.controller;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.catalog.newcatalog.GUIBlueprintFillScrollableList;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.BlueprintMetaItem;
import org.schema.game.common.data.player.SimplePlayerCommands;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.network.commands.gamerequests.EntityRequest;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUICheckBoxTextPair;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class PlayerBlueprintMetaDialog extends PlayerGameOkCancelInput {

	private BlueprintMetaItem it;

	public PlayerBlueprintMetaDialog(final GameClientState state, final BlueprintMetaItem it, final Inventory openedFrom) {
		super("PlayerBlueprintMetaDialog", state, UIScale.getUIScale().scale(600), UIScale.getUIScale().scale(400), Lng.str("Blueprint %s", it.blueprintName), "");
		this.it = it;
		getInputPanel().onInit();
		useOwnFaction = getState().getPlayer().getFactionId() > 0;
		((GUIDialogWindow) getInputPanel().background).getMainContentPane().setTextBoxHeightLast(UIScale.getUIScale().scale(25));
		((GUIDialogWindow) getInputPanel().background).getMainContentPane().addNewTextBox(UIScale.getUIScale().scale(40));
		GUIHorizontalButtonTablePane buttons = new GUIHorizontalButtonTablePane(state, 2, 1, ((GUIDialogWindow) getInputPanel().background).getMainContentPane().getContent(0));
		buttons.onInit();
		((GUIDialogWindow) getInputPanel().background).getMainContentPane().getContent(0).attach(buttons);
		buttons.addButton(0, 0, Lng.str("ADD ALL"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(211);
					PlayerBlueprintMetaDialog.this.pressedAll();
				}
			}

			@Override
			public boolean isOccluded() {
				return !PlayerBlueprintMetaDialog.this.isActive();
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return !getItem().metGoal();
			}
		});
		buttons.addButton(1, 0, Lng.str("SPAWN"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !PlayerBlueprintMetaDialog.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(212);
					PlayerBlueprintMetaDialog.this.pressedSpawn(openedFrom);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return getItem().metGoal();
			}
		});
		GUIBlueprintFillScrollableList l = new GUIBlueprintFillScrollableList(getState(), ((GUIDialogWindow) getInputPanel().background).getMainContentPane().getContent(1), this, getItem());
		l.onInit();
		((GUIDialogWindow) getInputPanel().background).getMainContentPane().getContent(1).attach(l);
		getInputPanel().setCancelButton(false);
		getInputPanel().setOkButtonText(Lng.str("DONE"));
	}

	public BlueprintMetaItem getItem() {
		return (BlueprintMetaItem) getState().getMetaObjectManager().getObject(it.getId());
	}

	public void pressedAll() {
		(new PlayerGameOkCancelInput("PlayerBlueprintMetaDialog_ADD_ALL", getState(), Lng.str("Confirm"), Lng.str("This adds as many items as possible\nfrom your inventory to this blueprint.\n(WARNING: you can't take items out of a blueprint again)")) {

			@Override
			public void onDeactivate() {
			}

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void pressedOK() {
				getState().getPlayer().sendSimpleCommand(SimplePlayerCommands.ADD_BLUEPRINT_META_ALL, getItem().getId());
				deactivate();
			}
		}).activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(213);
	}

	private boolean useOwnFaction;

	public void pressedSpawn(Inventory openedFrom) {
		final int invId = openedFrom.getInventoryHolder().getId();
		final long invParam = openedFrom.getParameter();
		PlayerGameTextInput pp = (new PlayerGameTextInput("PlayerBlueprintMetaDialog_SPAWN", getState(), 50, Lng.str("Spawn"), Lng.str("Please enter a name."), getItem().blueprintName + System.currentTimeMillis()) {

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}

			@Override
			public void onDeactivate() {
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
				return null;
			}

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public boolean onInput(String entry) {
				if (entry.length() > 0 && EntityRequest.isShipNameValid(entry)) {
					getState().getPlayer().sendSimpleCommand(SimplePlayerCommands.SPAWN_BLUEPRINT_META, getItem().getId(), entry, invId, ElementCollection.getPosX(invParam), ElementCollection.getPosY(invParam), ElementCollection.getPosZ(invParam), useOwnFaction, -1, Long.MIN_VALUE);
					PlayerBlueprintMetaDialog.this.deactivate();
					return true;
				} else {
					getState().getController().popupAlertTextMessage(Lng.str("Invalid name!\nMust only contain\nletters or numbers or '_', '-'"), 0);
				}
				return false;
			}
		});
		pp.getInputPanel().onInit();
		GUICheckBoxTextPair useFact = new GUICheckBoxTextPair(getState(), Lng.str("Set as own Faction (needs faction block)"), 240, FontSize.SMALL_14, 24) {

			@Override
			public boolean isActivated() {
				return useOwnFaction;
			}

			@Override
			public void deactivate() {
				useOwnFaction = false;
			}

			@Override
			public void activate() {
				if (((GameClientState) getState()).getPlayer().getFactionId() > 0) {
					useOwnFaction = true;
				} else {
					((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("You are not in a faction!"), 0);
					useOwnFaction = false;
				}
			}
		};
		useFact.setPos(3, 35, 0);
		((GUIDialogWindow) pp.getInputPanel().background).getMainContentPane().getContent(0).attach(useFact);
		pp.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(214);
	}

	@Override
	public boolean allowChat() {
		return true;
	}

	@Override
	public boolean isOccluded() {
		return false;
	}

	public void pressedAdd(final short type) {
		(new PlayerGameTextInput("PlayerBlueprintMetaDialog_ADD", getState(), 20, Lng.str("Add"), Lng.str("How many %s do you want\nto use for this blueprint?\n(Warning: once used, you can't get the resource back)", ElementKeyMap.getInfo(type).getName())) {

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
					getState().getPlayer().sendSimpleCommand(SimplePlayerCommands.ADD_BLUEPRINT_META_SINGLE, getItem().getId(), type, count);
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
		AudioController.fireAudioEventID(215);
	}

	@Override
	public void onDeactivate() {
	}

	@Override
	public void pressedOK() {
		deactivate();
	}
}
