package org.schema.game.common.data.element.meta.weapon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.FastMath;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.HitType;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.elements.TransporterModuleInterface;
import org.schema.game.common.controller.elements.transporter.TransporterCollectionManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.MetaObjectState;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.PlayerCharacter;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButton;
import org.schema.schine.input.InputState;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.sound.controller.AudioController;

public class TransporterBeaconBeam extends Weapon {

	public String marking = "unmarked";

	public long markerLocation = Long.MIN_VALUE;

	public String realName = "unmarked";

	float speed = 70f;

	private float charge = 0;

	public float chargePerSec = 1.4f;

	private Vector4f color = new Vector4f((1f), (1f), (1f), 1.0f);

	private float maxCharge = 100;

	private boolean charging;

	public TransporterBeaconBeam(int id) {
		super(id, WeaponSubType.TRANSPORTER_MARKER.type);
	}

	public static TransporterBeaconBeam getMarkingFromTag(Tag t, int shift) {
		Tag[] v = (Tag[]) t.getValue();
		TransporterBeaconBeam m = new TransporterBeaconBeam(0);
		m.marking = (String) v[0].getValue();
		m.markerLocation = (Long) v[1].getValue();
		if (shift != 0) {
			m.markerLocation = ElementCollection.shiftIndex(m.markerLocation, shift, shift, shift);
		}
		return m;
	}

	@Override
	public void deserialize(DataInputStream stream) throws IOException {
		marking = stream.readUTF();
		realName = stream.readUTF();
		markerLocation = stream.readLong();
		chargePerSec = stream.readFloat();
	}

	@Override
	public void fromTag(Tag tag) {
		Tag[] v = (Tag[]) tag.getValue();
		marking = (String) v[0].getValue();
		realName = (String) v[1].getValue();
		markerLocation = (Long) v[2].getValue();
		if (v.length > 3 && v[3].getType() == Type.FLOAT) {
			chargePerSec = (Float) v[3].getValue();
		}
	}

	@Override
	public Tag getBytesTag() {
		return new Tag(Type.STRUCT, null, new Tag[] { new Tag(Type.STRING, null, marking), new Tag(Type.STRING, null, realName), new Tag(Type.LONG, null, markerLocation), new Tag(Type.FLOAT, null, chargePerSec), FinishTag.INST });
	}

	@Override
	public void serialize(DataOutputStream stream) throws IOException {
		stream.writeUTF(marking);
		stream.writeUTF(realName);
		stream.writeLong(markerLocation);
		stream.writeFloat(chargePerSec);
	}

	@Override
	public void fire(AbstractCharacter<?> playerCharacter, AbstractOwnerState state, boolean addButton, boolean removeButton, Timer timer) {
		Vector3f dir = state.getForward(new Vector3f());
		if (removeButton) {
			charging = true;
			charge = Math.min(charge + chargePerSec * timer.getDelta(), maxCharge);
			if (!playerCharacter.isOnServer()) {
				if (!hasValidTarget((GameClientState) playerCharacter.getState())) {
					((GameClientState) playerCharacter.getState()).getController().showBigTitleMessage("TTC", Lng.str("Connection with transporter cannot be established,\ndestination not set or out of reach!"), 0);
				} else {
					((GameClientState) playerCharacter.getState()).getController().showBigTitleMessage("TTC", Lng.str("Transporter is trying to locate you!\nCompleted %s%%", (int) ((charge / maxCharge) * 100f)), 0);
				}
			}
			if (charge == maxCharge) {
				// TRANSPORT
				if (!playerCharacter.isOnServer()) {
					transport((GameClientState) playerCharacter.getState());
				}
				charge = 0;
			}
		} else if (addButton) {
			fire(playerCharacter, state, dir, addButton, removeButton, timer);
		}
		if (charge > 0) {
			((MetaObjectState) state.getState()).getMetaObjectManager().updatableObjects.add(this);
		}
	}

	@Override
	public boolean update(Timer timer) {
		if (charging) {
			charging = false;
			return true;
		}
		boolean rem = charge > 0;
		charge = Math.max(0, charge - (chargePerSec * 3 * timer.getDelta()));
		return rem;
	}

	@Override
	public void fire(AbstractCharacter<?> playerCharacter, AbstractOwnerState state, Vector3f dir, boolean addButton, boolean removeButton, Timer timer) {
		dir.scale(speed);
		if (!state.isOnServer() && ((GameClientState) state.getState()).getCurrentSectorId() != playerCharacter.getSectorId()) {
			return;
		}
		if (playerCharacter instanceof PlayerCharacter) {
			((PlayerCharacter) playerCharacter).shootTransporterMarkerBeam(((PlayerState) playerCharacter.getOwnerState()).getControllerState().getUnits().iterator().next(), 100, this, addButton, removeButton);
		}
	}

	@Override
	public void drawPossibleOverlay(GUIOverlay reload, Inventory inventory) {
		TransporterBeaconBeam r = this;
		if (charge > 0) {
			int base = 2;
			float max = 8;
			float percent = 0;
			percent = 1.0f - (charge / maxCharge);
			int sprite = base;
			sprite = (int) FastMath.floor(FastMath.clamp(base + percent * max, base, base + max));
			reload.setSpriteSubIndex(sprite);
			reload.draw();
		}
	}

	@Override
	protected GUIHorizontalButton[] getButtons(GameClientState state, Inventory inventory) {
		return new GUIHorizontalButton[] { getInfoButton(state, inventory), getDeleteButton(state, inventory) // getTransportButton(state, inventory)
		};
	}

	private boolean hasValidTarget(GameClientState state) {
		Sendable sendable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(marking);
		if (sendable != null && sendable instanceof SegmentController && ((SegmentController) sendable).isNeighbor(state.getCharacter().getSectorId(), ((SegmentController) sendable).getSectorId()) && sendable instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) sendable).getManagerContainer() instanceof TransporterModuleInterface && ((TransporterModuleInterface) ((ManagedSegmentController<?>) sendable).getManagerContainer()).getTransporter().getCollectionManagersMap().get(markerLocation) != null) {
			return true;
		}
		return false;
	}

	private void transport(GameClientState state) {
		if (state.getCurrentPlayerObject() instanceof PlayerCharacter) {
			Sendable sendable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(marking);
			if (sendable != null && sendable instanceof SegmentController && ((SegmentController) sendable).isNeighbor(state.getCharacter().getSectorId(), ((SegmentController) sendable).getSectorId()) && sendable instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) sendable).getManagerContainer() instanceof TransporterModuleInterface && ((TransporterModuleInterface) ((ManagedSegmentController<?>) sendable).getManagerContainer()).getTransporter().getCollectionManagersMap().get(markerLocation) != null) {
				TransporterCollectionManager transporterCollectionManager = ((TransporterModuleInterface) ((ManagedSegmentController<?>) sendable).getManagerContainer()).getTransporter().getCollectionManagersMap().get(markerLocation);
				transporterCollectionManager.sendBeaconActivated(state.getCharacter().getId());
			} else {
				state.getController().popupAlertTextMessage(Lng.str("Destination not set or out of reach!\n"), 0);
			}
		} else {
			state.getController().popupAlertTextMessage(Lng.str("Cannot transport from inside structures!\n"), 0);
		}
	}

	protected GUIHorizontalButton getTransportButton(final GameClientState state, final Inventory inventory) {
		return new GUIHorizontalButton(state, HButtonType.BUTTON_BLUE_MEDIUM, Lng.str("TRANSPORT"), new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					PlayerGameOkCancelInput confirm = new PlayerGameOkCancelInput("CONFIRM", state, Lng.str("Confirm"), Lng.str("Transport to destination?\nShield of sending and destination structure \nwill be dropped!")) {

						@Override
						public boolean isOccluded() {
							return false;
						}

						@Override
						public void onDeactivate() {
						}

						@Override
						public void pressedOK() {
							transport(state);
							deactivate();
						}
					};
					confirm.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(949);
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		}, null, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return marking != null && !marking.equals("unmarked");
			}
		});
	}

	@Override
	public String getName() {
		return Lng.str("Transporter Beacon");
	}

	@Override
	protected String toDetailedString() {
		return Lng.str("Use this (left click) on a transporter computer to warp back to it\nRight click to charge and trigger transporter beacon.\nCurrent Destination: %s; %s\nCharge Speed Percent/sec: %s", realName, (markerLocation != Long.MIN_VALUE ? ElementCollection.getPosFromIndex(markerLocation, new Vector3i()) : Lng.str("undefined")), StringTools.formatPointZero(chargePerSec));
	}

	/**
	 * @return the color
	 */
	public Vector4f getColor() {
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(Vector4f color) {
		this.color = color;
	}

	public boolean equalsBeam(TransporterBeaconBeam obj) {
		return marking != null && marking.equals(obj.marking) && markerLocation == obj.markerLocation;
	}

	@Override
	public boolean equalsObject(MetaObject other) {
		return super.equalsTypeAndSubId(other) && marking.equals(((TransporterBeaconBeam) other).marking) && markerLocation == (((TransporterBeaconBeam) other).markerLocation);
	}

	@Override
	protected void setupEffectSet(InterEffectSet s) {
	}

	public HitType getHitType() {
		return HitType.SUPPORT;
	}
}
