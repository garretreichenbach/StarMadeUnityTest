package org.schema.game.client.controller.manager.ingame.faction;

import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.client.view.gui.faction.FactionBlockGUIDialog;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.network.commands.gamerequests.EntityRequest;
import org.schema.schine.common.InputChecker;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.sound.controller.AudioController;

public class FactionBlockDialog extends PlayerGameOkCancelInput {

	private final SimpleTransformableSendableObject obj;

	private FactionBlockGUIDialog panel;

	private AbstractControlManager pc;

	public FactionBlockDialog(GameClientState state, SimpleTransformableSendableObject obj, AbstractControlManager pc) {
		super("FactionBlockDialog", state, Lng.str("Faction"), "");
		this.obj = obj;
		this.pc = pc;
		pc.suspend(true);
		panel = new FactionBlockGUIDialog(getState(), this, obj);
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		super.callback(callingGuiElement, event);
		if (event.pressedLeftMouse()) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
			AudioController.fireAudioEventID(113);
			if (callingGuiElement.getUserPointer().equals("NEUTRAL")) {
				if (obj instanceof SegmentController && ((SegmentController) obj).isSufficientFactionRights(getState().getPlayer())) {
					getState().getPlayer().getFactionController().sendEntityFactionIdChangeRequest(0, obj);
				} else {
					getState().getController().popupAlertTextMessage(Lng.str("Access denied by faction rank!"), 0);
				}
				deactivate();
			} else if (callingGuiElement.getUserPointer().equals("FACTION")) {
				System.err.println("[CLIENT] trying to set faction to " + getState().getPlayer().getFactionId() + " on " + obj);
				getState().getPlayer().getFactionController().sendEntityFactionIdChangeRequest(getState().getPlayer().getFactionId(), obj);
				deactivate();
			} else if (callingGuiElement.getUserPointer().equals("HOMEBASE")) {
				String e = Lng.str("you already have a homebase");
				boolean h = getState().getPlayer().getFactionController().hasHomebase();
				PlayerGameOkCancelInput check = new PlayerGameOkCancelInput("CONFIRM", getState(), Lng.str("WARN"), Lng.str("Do you really want to do this?") + "\n" + (h ? e : "")) {

					@Override
					public boolean isOccluded() {
						return false;
					}

					@Override
					public void onDeactivate() {
					}

					@Override
					public void pressedOK() {
						deactivate();
						getState().getFactionManager().sendClientHomeBaseChange(getState().getPlayer().getName(), getState().getPlayer().getFactionId(), obj.getUniqueIdentifier());
					}
				};
				check.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(115);
				deactivate();
			} else if (callingGuiElement.getUserPointer().equals("SYSTEM_OWN")) {
				final SegmentController c = (SegmentController) obj;
				if (getState().getPlayer().getFactionId() != 0 && getState().getPlayer().getFactionId() == c.getFactionId() && !c.isSufficientFactionRights(getState().getPlayer())) {
					getState().getController().popupAlertTextMessage(Lng.str("Permission denied!\n(faction rank)"), 0);
				} else {
					getState().getFactionManager().sendClientSystemOwnerChange(getState().getPlayer().getName(), getState().getPlayer().getFactionId(), obj);
					deactivate();
				}
			} else if (callingGuiElement.getUserPointer().equals("SYSTEM_NONE")) {
			} else if (callingGuiElement.getUserPointer().equals("SYSTEM_REVOKE")) {
				final SegmentController c = (SegmentController) obj;
				if (getState().getPlayer().getFactionId() != 0 && getState().getPlayer().getFactionId() == c.getFactionId() && !c.isSufficientFactionRights(getState().getPlayer())) {
					getState().getController().popupAlertTextMessage(Lng.str("Permission denied!\n(faction rank)"), 0);
				} else {
					getState().getFactionManager().sendClientSystemOwnerChange(getState().getPlayer().getName(), 0, obj);
					deactivate();
				}
			} else if (callingGuiElement.getUserPointer().equals("RENAME")) {
				final SegmentController c = (SegmentController) obj;
				if (getState().getPlayer().getFactionId() != 0 && getState().getPlayer().getFactionId() == c.getFactionId() && !c.isSufficientFactionRights(getState().getPlayer())) {
					getState().getController().popupAlertTextMessage(Lng.str("Permission denied!\n(faction rank)"), 0);
				} else {
					PlayerGameTextInput p = new PlayerGameTextInput("FactionBlockDialog_CHANGE_NAME", getState(), 50, Lng.str("Change Name"), Lng.str("Change the name of the object"), c.getRealName()) {

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
							pc.hinderInteraction(400);
							pc.suspend(false);
						}

						@Override
						public boolean onInput(String entry) {
							if (!c.getRealName().equals(entry.trim())) {
								System.err.println("[CLIENT] sending name for object: " + c + ": " + entry.trim());
								c.getNetworkObject().realName.set(entry.trim(), true);
								assert (c.getNetworkObject().realName.hasChanged());
								assert (c.getNetworkObject().isChanged());
							}
							return true;
						}
					};
					p.setInputChecker((entry, callback) -> {
						if (EntityRequest.isShipNameValid(entry)) {
							return true;
						} else {
							callback.onFailedTextCheck(Lng.str("Must only contain letters or numbers or ( _-)!"));
							return false;
						}
					});
					p.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(114);
					deactivate();
				}
			} else if (callingGuiElement.getUserPointer().equals("HOMEBASE_REVOKE")) {
				final SegmentController c = (SegmentController) obj;
				if (getState().getPlayer().getFactionId() != 0 && getState().getPlayer().getFactionId() == c.getFactionId() && getState().getPlayer().getFactionRights() < c.getFactionRights()) {
					getState().getController().popupAlertTextMessage(Lng.str("Permission denied!\n(faction rank)"), 0);
				} else {
					getState().getFactionManager().sendClientHomeBaseChange(getState().getPlayer().getName(), getState().getPlayer().getFactionId(), "");
					deactivate();
				}
			}
		// else if (callingGuiElement.getUserPointer().equals("CANCEL") || callingGuiElement.getUserPointer().equals("X")) {
		// cancel();
		// }
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
		if (getState().getController().getPlayerInputs().isEmpty()) {
			pc.hinderInteraction(400);
			pc.suspend(false);
		}
	}

	@Override
	public boolean isOccluded() {
		return false;
	}

	@Override
	public void pressedOK() {
		deactivate();
	}
}
