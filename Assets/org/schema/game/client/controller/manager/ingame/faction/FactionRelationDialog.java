package org.schema.game.client.controller.manager.ingame.faction;

import org.schema.game.client.controller.PlayerInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.faction.FactionRelationEditPanel;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.sound.controller.AudioController;

public class FactionRelationDialog extends PlayerInput {

	private Faction from;

	private Faction to;

	private FactionRelationEditPanel panel;

	public FactionRelationDialog(GameClientState state, Faction from, Faction to) {
		super(state);
		this.from = from;
		this.to = to;
		panel = new FactionRelationEditPanel(state, from, to, this);
		panel.setOkButton(false);
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.pressedLeftMouse()) {
			if (callingGuiElement.getUserPointer().equals("OK")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(138);
				cancel();
			} else if (callingGuiElement.getUserPointer().equals("CANCEL") || callingGuiElement.getUserPointer().equals("X")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(137);
				System.err.println("CANCEL");
				cancel();
			} else if (callingGuiElement.getUserPointer().equals("WAR")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(136);
				getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getFactionControlManager().openDeclareWarDialog(from, to);
				deactivate();
			} else if (callingGuiElement.getUserPointer().equals("PEACE")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(135);
				getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getFactionControlManager().openOfferPeaceDialog(from, to);
				deactivate();
			} else if (callingGuiElement.getUserPointer().equals("ALLY")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(134);
				getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getFactionControlManager().openOfferAllyDialog(from, to);
				deactivate();
			} else if (callingGuiElement.getUserPointer().equals("ALLY_REVOKE")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(133);
				getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getFactionControlManager().revokeAlly(from, to);
				deactivate();
			} else if (callingGuiElement.getUserPointer().equals("PEACE_OFFER_REVOKE")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(132);
				getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getFactionControlManager().revokePeaceOffer(from, to);
				deactivate();
			} else if (callingGuiElement.getUserPointer().equals("ALLY_OFFER_REVOKE")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(131);
				getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getFactionControlManager().revokeAllyOffer(from, to);
				deactivate();
			}
		}
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		super.handleKeyEvent(e);
	}

	@Override
	public GUIElement getInputPanel() {
		return panel;
	}

	@Override
	public void onDeactivate() {
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getFactionControlManager().hinderInteraction(500);
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getFactionControlManager().suspend(false);
	}
}
