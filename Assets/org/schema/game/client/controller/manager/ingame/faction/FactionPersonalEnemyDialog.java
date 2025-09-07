package org.schema.game.client.controller.manager.ingame.faction;

import java.util.Locale;

import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.controller.PlayerInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.client.view.gui.faction.FactionPersonalEnemyEditPanel;
import org.schema.game.client.view.gui.faction.newfaction.FactionPersonalEnemyEditPanelNew;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.DialogInterface;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.sound.controller.AudioController;

public class FactionPersonalEnemyDialog extends PlayerInput {

	private Faction from;

	private GUIInputPanel panel;

	public FactionPersonalEnemyDialog(GameClientState state, Faction from) {
		super(state);
		this.from = from;
		if (GUIElement.isNewHud()) {
			panel = new FactionPersonalEnemyEditPanelNew(state, from, this);
		} else {
			panel = new FactionPersonalEnemyEditPanel(state, from, this);
			panel.setOkButton(false);
		}
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.pressedLeftMouse()) {
			if (callingGuiElement.getUserPointer().equals("OK")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(130);
				for (DialogInterface p : getState().getController().getPlayerInputs()) {
					if (p instanceof PlayerGameTextInput) {
						return;
					}
				}
				cancel();
			} else if (callingGuiElement.getUserPointer().equals("CANCEL") || callingGuiElement.getUserPointer().equals("X")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(129);
				for (DialogInterface p : getState().getController().getPlayerInputs()) {
					if (p instanceof PlayerGameTextInput) {
						return;
					}
				}
				System.err.println("CANCEL");
				cancel();
			} else if (callingGuiElement.getUserPointer() != null && callingGuiElement.getUserPointer().toString().startsWith("remove_")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.DELETE)*/
				AudioController.fireAudioEventID(128);
				for (DialogInterface p : getState().getController().getPlayerInputs()) {
					if (p instanceof PlayerGameTextInput) {
						return;
					}
				}
				String name = callingGuiElement.getUserPointer().toString().substring("remove_".length());
				getState().getFactionManager().sendPersonalEnemyRemove(getState().getPlayerName(), from, name.toLowerCase(Locale.ENGLISH));
			// deactivate();
			} else if (callingGuiElement.getUserPointer().equals("ADD")) {
				for (DialogInterface p : getState().getController().getPlayerInputs()) {
					if (p instanceof PlayerGameTextInput) {
						return;
					}
				}
				// getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager()
				// .getFactionControlManager().openOfferPeaceDialog(from, to);
				(new PlayerGameTextInput("FactionPersonalEnemyDialog_ADD_ENEMY", getState(), 460, 180, 32, Lng.str("Enter Player Name"), Lng.str("Enter the name of the player you want to add to the personal enemy list\n\n(case of the name doesn't matter)")) {

					@Override
					public String[] getCommandPrefixes() {
						return null;
					}

					@Override
					public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
						return getState().onAutoComplete(s, this, prefix);
					}

					@Override
					public void onFailedTextCheck(String msg) {
					}

					@Override
					public void onDeactivate() {
					}

					@Override
					public boolean onInput(String entry) {
						if (entry.length() > 0) {
							getState().getFactionManager().sendPersonalEnemyAdd(getState().getPlayerName(), from, entry.trim());
						} else {
							return false;
						}
						return true;
					}
				}).activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(127);
			// deactivate();
			}
		}
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		super.handleKeyEvent(e);
	}

	@Override
	public GUIInputPanel getInputPanel() {
		return panel;
	}

	@Override
	public void onDeactivate() {
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getFactionControlManager().hinderInteraction(500);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getFactionControlManager().suspend(false);
	}
}
