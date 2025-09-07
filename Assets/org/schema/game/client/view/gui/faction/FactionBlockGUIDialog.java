package org.schema.game.client.view.gui.faction;

import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.manager.ingame.faction.FactionBlockDialog;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.common.controller.Planet;
import org.schema.game.common.controller.PlanetIco;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.SimplePlayerCommands;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionRoles;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.Vector4f;

public class FactionBlockGUIDialog extends GUIInputPanel {

	private SimpleTransformableSendableObject obj;

	public FactionBlockGUIDialog(InputState state, FactionBlockDialog guiCallback, SimpleTransformableSendableObject obj) {
		super("FACTION_BLOCK_GUI_DIALOG_S", state, 450, 270, guiCallback, Lng.str("Faction Block Config"), "");
		this.obj = obj;
		setCancelButton(false);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.gui.GUIInputPanel#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();
		GUITextButton makeNeutral = new GUITextButton(getState(), 200, 20, Lng.str("Reset Faction Signature"), getCallback());
		makeNeutral.setUserPointer("NEUTRAL");
		GUITextButton makeFaction = new GUITextButton(getState(), 200, 20, Lng.str("Enter Faction Signature"), getCallback());
		makeFaction.setUserPointer("FACTION");
		makeFaction.getPos().y = 30;
		final GameClientState state = (GameClientState) getState();
		GUITextButton makeFactionHomebase = new GUITextButton(getState(), 200, 20, Lng.str("Make Faction Home"), getCallback()) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
			 */
			@Override
			public void draw() {
				if ((((GameClientState) getState()).getFaction() != null) && obj.getFactionId() == state.getPlayer().getFactionId() && state.getPlayer().getFactionController().hasHomebaseEditPermission() && !state.getPlayer().getFactionController().isHomebase(obj) && (obj instanceof Planet || obj instanceof PlanetIco || obj instanceof SpaceStation)) {
					super.draw();
				}
			}
		};
		makeFactionHomebase.setUserPointer("HOMEBASE");
		makeFactionHomebase.getPos().y = 60;
		getContent().attach(makeFactionHomebase);
		GUITextButton takeSystemOwnership = new GUITextButton(getState(), 200, 20, Lng.str("Take System Ownership"), getCallback()) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
			 */
			@Override
			public void draw() {
				if ((((GameClientState) getState()).getFaction() != null) && obj.getFactionId() == ((GameClientState) getState()).getPlayer().getFactionId() && (obj instanceof Planet || obj instanceof PlanetIco || obj instanceof SpaceStation)) {
					VoidSystem systemOnClient = ((GameClientState) getState()).getController().getClientChannel().getGalaxyManagerClient().getSystemOnClient(((GameClientState) getState()).getCurrentRemoteSector().clientPos());
					if (systemOnClient != null) {
						if (systemOnClient.getOwnerFaction() == ((GameClientState) getState()).getPlayer().getFactionId()) {
							setText(Lng.str("Revoke Galaxy Sys ownership"));
							setUserPointer("SYSTEM_REVOKE");
						} else if (systemOnClient.getOwnerFaction() != 0) {
							GameClientState s = (GameClientState) getState();
							Faction faction = s.getFactionManager().getFaction(systemOnClient.getOwnerFaction());
							if (faction != null) {
								setText(Lng.str("Galaxy System belongs to %s", faction.getName()));
								setUserPointer("SYSTEM_NONE");
							} else {
								setText(Lng.str("Claim Galaxy System"));
								setUserPointer("SYSTEM_OWN");
							}
						} else {
							setText(Lng.str("Take System control for faction"));
							setUserPointer("SYSTEM_OWN");
						}
					} else {
						setText(Lng.str("Scanning..."));
					}
					super.draw();
				}
			}
		};
		takeSystemOwnership.getPos().x = 200;
		takeSystemOwnership.getPos().y = 60;
		getContent().attach(takeSystemOwnership);
		GUITextButton rename = new GUITextButton(getState(), 200, 20, Lng.str("Rename"), getCallback()) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
			 */
			@Override
			public void draw() {
				if ((((GameClientState) getState()).getFaction() != null) && obj.getFactionId() == ((GameClientState) getState()).getPlayer().getFactionId()) {
					super.draw();
				}
			}
		};
		rename.setUserPointer("RENAME");
		rename.getPos().y = 90;
		getContent().attach(rename);
		if (obj instanceof SegmentController) {
			GUITextOverlay owner = new GUITextOverlay(getState());
			owner.setTextSimple(new Object() {

				@Override
				public String toString() {
					if (((SegmentController) obj).currentOwnerLowerCase.length() > 0) {
						return Lng.str("Owner: %s", ((SegmentController) obj).currentOwnerLowerCase);
					} else {
						return Lng.str("No owner");
					}
				}
			});
			owner.setPos(5, 110 + 5, 0);
			getContent().attach(owner);
		}
		GUITextOverlay l = new GUITextOverlay(getState());
		l.setTextSimple(Lng.str("Permission Rank:"));
		l.getPos().x = 5;
		l.getPos().y = 110 + 18 + 5;
		for (byte i = -1; i < 5; i++) {
			addFactionRank(i);
		}
		// GUITextButton makeFactionHomebaseRevoke = new GUITextButton(getState(), 200, 20, "Reset Faction Home", getCallback());
		// makeFactionHomebaseRevoke.setUserPointer("HOMEBASE_REVOKE");
		// makeFactionHomebaseRevoke.getPos().y = 60;
		// makeFactionHomebaseRevoke.getPos().x = 210;
		// content.attach(makeFactionHomebaseRevoke);
		getContent().attach(l);
		getContent().attach(makeNeutral);
		getContent().attach(makeFaction);
	}

	private void addFactionRank(final byte rank) {
		GUITextButton permission = new GUITextButton(getState(), 20, 20, getRankString(rank) + (rank == ((GameClientState) getState()).getPlayer().getFactionRights() ? "*" : ""), new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					SegmentController sc = ((SegmentController) obj);
					final PlayerState p = ((GameClientState) getState()).getPlayer();
					if ((sc.getFactionRights() != -1 && p.getFactionRights() == 4) || sc.isSufficientFactionRights(p)) {
						if (rank > p.getFactionRights()) {
							(new PlayerGameOkCancelInput("CONFIRM", (GameClientState) getState(), Lng.str("Confirm"), Lng.str("WARNING!\nIf you set this rank, you will no longer\nbe able to edit or change it.\n\nDo you really want to do this?")) {

								@Override
								public boolean isOccluded() {
									return false;
								}

								@Override
								public void onDeactivate() {
								}

								@Override
								public void pressedOK() {
									p.sendSimpleCommand(SimplePlayerCommands.SET_FACTION_RANK_ON_OBJ, obj.getId(), rank);
									deactivate();
								}
							}).activate();
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
							AudioController.fireAudioEventID(443);
						} else {
							p.sendSimpleCommand(SimplePlayerCommands.SET_FACTION_RANK_ON_OBJ, obj.getId(), rank);
						}
					} else {
						((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("You don't have the right\nto set permission to that\nvalue"), 0);
						System.err.println("[CLIENT] Permission failed: " + p + ": Rank: " + p.getFactionRights() + "; Target " + sc + ": Rank: " + sc.getFactionRights() + "; toSet: " + rank);
					}
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		}) {

			private Vector4f selected = new Vector4f(0.5f, 0.9f, 0.4f, 1.0f);

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
			 */
			@Override
			public void draw() {
				if ((((GameClientState) getState()).getFaction() != null) && obj.getFactionId() == ((GameClientState) getState()).getPlayer().getFactionId()) {
					super.draw();
				}
			}

			private Vector4f uselected = new Vector4f(0.8f, 0.2f, 0.4f, 1.0f);

			private Vector4f blue = new Vector4f(0.2f, 0.4f, 0.8f, 1.0f);

			private Vector4f mouse = new Vector4f(0.5f, 0.4f, 0.2f, 1.0f);

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#getBackgroundColor()
			 */
			@Override
			public Vector4f getBackgroundColor() {
				if (((SegmentController) obj).getFactionRights() == -1) {
					if (rank == -1) {
						return selected;
					} else {
						return uselected;
					}
				} else if (((SegmentController) obj).getFactionRights() == -2) {
					if (rank == -1) {
						return blue;
					} else {
						return uselected;
					}
				} else {
					if (rank >= ((SegmentController) obj).getFactionRights()) {
						return selected;
					} else {
						if (rank == -1) {
							return blue;
						}
						return uselected;
					}
				}
			// return super.getBackgroundColor();
			}

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#getSelectedBackgroundColor()
			 */
			@Override
			public Vector4f getBackgroundColorMouseOverlay() {
				return mouse;
			}
		};
		permission.setTextPos(3, 1);
		permission.getPos().y = 110 + 16 + 5;
		permission.getPos().x = 106 + (rank >= 0 ? rank * 20 : FactionRoles.ROLE_COUNT * 20);
		getContent().attach(permission);
	}

	private String getRankString(byte rank) {
		if (rank == -1) {
			return "P";
		}
		if (rank == 4) {
			return "F";
		}
		return String.valueOf(4 - rank);
	}
}
