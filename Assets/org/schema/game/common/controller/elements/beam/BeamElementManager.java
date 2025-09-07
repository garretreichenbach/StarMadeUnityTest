package org.schema.game.common.controller.elements.beam;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.BeamHandlerContainer;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.*;
import org.schema.game.common.controller.elements.ManagerContainer.ReceivedBeamLatch;
import org.schema.game.common.controller.elements.beam.damageBeam.DamageBeamCollectionManager;
import org.schema.game.common.controller.elements.combination.BeamCombiSettings;
import org.schema.game.common.controller.elements.combination.CombinationAddOn;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ShootContainer;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.IOException;

public abstract class BeamElementManager<E extends BeamUnit<E, CM, EM>, CM extends BeamCollectionManager<E, CM, EM>, EM extends BeamElementManager<E, CM, EM>> extends UsableCombinableControllableElementManager<E, CM, EM, BeamCombiSettings> implements BlockActivationListenerInterface, IntegrityBasedInterface {

	//Temporary ids until tag system is working
	private static final int START_FIRE_ID = 876;
	private static final int FIRING_ID = 1000;
	private static final int END_FIRE_ID = 1001;
	//
	protected BeamStatus beamStatus;

	public final ShootContainer shootContainer = new ShootContainer();

	public BeamElementManager(short controller, short controlling, SegmentController segmentController) {
		super(controller, controlling, segmentController);
	}

	private final BeamCombiSettings combiSettings = new BeamCombiSettings();

	public BeamCombiSettings getCombiSettings() {
		return combiSettings;
	}

	@Override
	public int onActivate(SegmentPiece piece, boolean oldActive, boolean active) {
		long absPos = piece.getAbsoluteIndex();
		for(int i = 0; i < getCollectionManagers().size(); i++) {
			for(E d : getCollectionManagers().get(i).getElementCollections()) {
				if(d.contains(absPos)) {
					d.setMainPiece(piece, active);

					return active ? 1 : 0;
				}
			}
		}
		return active ? 1 : 0;
	}

	/**
	 * Performs a shot using a beam weapon.
	 *
	 * @param c                 the BeamUnit object representing the beam weapon
	 * @param m                 the CM object representing the weapon module
	 * @param shootContainer    the ShootContainer object containing information about the shot
	 * @param playerState       the PlayerState object representing the player's state
	 * @param beamTimeout       the float value representing the timeout for the beam
	 * @param timer             the Timer object representing the game timer
	 * @param focusBeamOnClient the boolean flag indicating whether to focus the beam on the client
	 */
	public void doShot(E c, CM m, ShootContainer shootContainer, PlayerState playerState, float beamTimeout, Timer timer, boolean focusBeamOnClient) {


		ManagerModuleCollection<?, ?, ?> effectModuleCollection = null;

		if(m.getEffectConnectedElement() != Long.MIN_VALUE) {
			short connectedType = 0;
			String errorReason = "";
			connectedType = (short) ElementCollection.getType(m.getEffectConnectedElement());
			effectModuleCollection = getManagerContainer().getModulesControllerMap().get(connectedType);

		}
		if(m.getEffectConnectedElement() != Long.MIN_VALUE) {
			short connectedType = 0;
			String errorReason = "";
			connectedType = (short) ElementCollection.getType(m.getEffectConnectedElement());
			effectModuleCollection = getManagerContainer().getModulesControllerMap().get(connectedType);

			ControlBlockElementCollectionManager<?, ?, ?> effect = CombinationAddOn.getEffect(m.getEffectConnectedElement(), effectModuleCollection, getSegmentController());
			if(effect != null) {
				m.setEffectTotal(effect.getTotalSize());
			}
		}
		if(isCombinable() && m.getSlaveConnectedElement() != Long.MIN_VALUE) {
			short connectedType = 0;
			String errorReason = "";
			connectedType = (short) ElementCollection.getType(m.getSlaveConnectedElement());
			ManagerModuleCollection<?, ?, ?> managerModuleCollection = getManagerContainer().getModulesControllerMap().get(connectedType);
			ShootingRespose handled = handleAddOn(this, m, c, managerModuleCollection, effectModuleCollection, shootContainer, null, playerState, timer, beamTimeout);
			//			shootContainer.weapontOutputWorldPos, shootContainer.shootingDirTemp, shootContainer.shootingUpTemp, shootContainer.shootingRightTemp
			handleResponse(handled, c, shootContainer.weapontOutputWorldPos);

		} else {

			m.setEffectTotal(0);
			float extraConsumption = 0;
			Vector3f to = new Vector3f();
			to.set(shootContainer.weapontOutputWorldPos);
			shootContainer.shootingDirTemp.scale(c.getDistance());
			to.add(shootContainer.shootingDirTemp);


			BeamCommand b = new BeamCommand();

			b.minEffectiveRange = c.getMinEffectiveRange();
			b.minEffectiveValue = c.getMinEffectiveValue();
			b.maxEffectiveRange = c.getMaxEffectiveRange();
			b.maxEffectiveValue = c.getMaxEffectiveValue();
			b.currentTime = timer.currentTime;
			b.identifier = c.getSignificator();
			b.relativePos.set(c.getOutput().x - SegmentData.SEG_HALF, c.getOutput().y - SegmentData.SEG_HALF, c.getOutput().z - SegmentData.SEG_HALF);
			b.reloadCallback = c;
			b.from.set(shootContainer.weapontOutputWorldPos);
			b.to.set(to);
			b.playerState = playerState;
			b.beamTimeout = c.getBurstTime() > 0 ? c.getBurstTime() : beamTimeout; //if the beam is burst. activation block will fire for exactly that. else use the time provided by the controller unit
			b.tickRate = c.getTickRate();
			b.beamPower = c.getBeamPower() + c.getAdditiveBeamPower();
			b.cooldownSec = c.getCoolDownSec();
			b.bursttime = c.getBurstTime();
			b.initialTicks = c.getInitialTicks();
			b.powerConsumedByTick = c.getPowerConsumption();
			b.latchOn = c.isLatchOn();
			b.checkLatchConnection = c.isCheckLatchConnection();
			b.hitType = c.getHitType();
			b.powerConsumedExtraByTick = extraConsumption;
			b.railParent = getRailHitMultiplierParent();
			b.railChild = getRailHitMultiplierChild();
			if(playerState != null && playerState.isDown(KeyboardMappings.WALK)) {
				b.dontFade = true;
			}

			b.weaponId = m.getUsableId();
			b.controllerPos = m.getControllerPos();

			b.firendlyFire = c.isFriendlyFire();
			b.penetrating = c.isPenetrating();
			b.acidDamagePercent = c.getAcidDamagePercentage();
			b.capacityPerTick = c.calcAmmoReqsPerTick();


			ShootingRespose response = ((BeamHandlerContainer<?>) m).getHandler().addBeam(b);

			fireSoundEvent(c, BeamStatus.START_FIRE);


			handleResponse(response, c, shootContainer.weapontOutputWorldPos);
			//			if (focusBeamOnClient && response == ShootingRespose.FIRED) {
			//				((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("You are focusing beams\nwith the left mouse button.\nSalvage beams are more powerful\nunfocused with the right mouse button.\n(works with all weapons/etc)"), 0);
			//			}

		}
	}

	/**
	 * Fires a sound event based on the given beam status.
	 *
	 * @param c          the BeamUnit object for which the sound event is fired
	 * @param beamStatus the BeamStatus indicating the state of the beam firing
	 */
	protected void fireSoundEvent(BeamUnit c, BeamStatus beamStatus) {
		/*AudioController.fireAudioEvent("BEAM_FIRE", new AudioTag[] { AudioTags.GAME, AudioTags.SHIP, AudioTags.WEAPON, AudioTags.BEAM, AudioTags.DAMAGE, AudioTags.FIRE }, AudioParam.START, AudioController.ent(getSegmentController(), c.getElementCollectionId(), c.getSignificator(), c))*/
		//AudioController.fireAudioEventID(876, AudioController.ent(getSegmentController(), c.getElementCollectionId(), c.getSignificator(), c));
		//Todo: Beams need start and stop firing sounds instead of just spamming fire and blowing out my damn ears
		if(this.beamStatus == null || this.beamStatus != beamStatus) {
			if(this.beamStatus == null) this.beamStatus = BeamStatus.END_FIRE;
			switch(beamStatus) {
				case START_FIRE:
					if(this.beamStatus == BeamStatus.END_FIRE) {
						AudioController.fireAudioEventID(START_FIRE_ID, AudioController.ent(getSegmentController(), c.getElementCollectionId(), c));
						this.beamStatus = BeamStatus.START_FIRE;
						break;
					}
				case FIRING:
					if(this.beamStatus == BeamStatus.START_FIRE) {
						AudioController.fireAudioEventID(FIRING_ID, AudioController.ent(getSegmentController(), c.getElementCollectionId(), c));
						this.beamStatus = BeamStatus.FIRING;
						break;
					}
				case END_FIRE:
					if(this.beamStatus == BeamStatus.FIRING) {
						AudioController.fireAudioEventID(END_FIRE_ID, AudioController.ent(getSegmentController(), c.getElementCollectionId(), c));
						this.beamStatus = BeamStatus.END_FIRE;
						break;
					}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.UsableControllableElementManager#onControllerBlockAdd()
	 */
	@Override
	public void onAddedCollection(long absPos, CM instance) {
		super.onAddedCollection(absPos, instance);
		notifyBeamDrawer();
	}

	public void notifyBeamDrawer() {
		if(!getSegmentController().isOnServer()) {
			((GameClientState) getSegmentController().getState()).getWorldDrawer().getBeamDrawerManager().update(this, true, null);
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.UsableControllableElementManager#onControllerBlockRemove()
	 */
	@Override
	public void onConnectionRemoved(long absPos, CM instance) {
		super.onConnectionRemoved(absPos, instance);
		notifyBeamDrawer();
	}


	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.UsableElementManager#getGUIUnitValues(org.schema.game.common.data.element.ElementCollection, org.schema.game.common.controller.elements.ElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager)
	 */
	@Override
	public ControllerManagerGUI getGUIUnitValues(E firingUnit, CM col, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return null;
	}

	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {
		if(!unit.isFlightControllerActive()) {
			return;
		}

		if(getCollectionManagers().isEmpty()) {
			//nothing to shoot with
			return;
		}

		try {
			if(!convertDeligateControls(unit, shootContainer.controlledFromOrig, shootContainer.controlledFrom)) {
				return;
			}
		} catch(IOException e) {
			e.printStackTrace();
			return;
		}

		int size = getCollectionManagers().size();
		for(int i = 0; i < size; i++) {
			CM m = getCollectionManagers().get(i);
			boolean selected = unit.isSelected(m.getControllerElement(), shootContainer.controlledFrom);
			boolean aiSelected = unit.isAISelected(m.getControllerElement(), shootContainer.controlledFrom, m instanceof DamageBeamCollectionManager ? i : ControllerStateInterface.ALWAYS_SELECTED_FOR_AI, getCollectionManagers().size(), m);
			if(selected && aiSelected) {


				boolean controlling = shootContainer.controlledFromOrig.equals(shootContainer.controlledFrom);
				controlling |= getControlElementMap().isControlling(shootContainer.controlledFromOrig, m.getControllerPos(), controllerId);
				if(controlling) {
					if(!m.allowedOnServerLimit()) {
						continue;
					}
					if(shootContainer.controlledFromOrig.equals(Ship.core)) {
						unit.getControlledFrom(shootContainer.controlledFromOrig);
					}
					m.handleControlShot(unit, timer);
				}
			}
		}
	}

	public abstract float getTickRate();

	public abstract float getCoolDown();

	public abstract float getBurstTime();

	public abstract float getInitialTicks();

	public abstract double getRailHitMultiplierParent();

	public abstract double getRailHitMultiplierChild();

	public boolean handleBeamLatch(ReceivedBeamLatch d) {
		for(CM e : getCollectionManagers()) {
			boolean h = e.handleBeamLatch(d);
			if(h) {
				return h;
			}
		}
		return false;
	}

	private static GUITextOverlay chargesText;
	public static final Vector4f chargeColor = new Vector4f(0.8F, 0.5F, 0.3F, 0.4F);

	public class DrawReloadListener implements ReloadListener {


		@Override
		public String onDischarged(InputState state, Vector3i iconPos, Vector3i iconSize, Vector4f reloadColor, boolean backwards, float percent) {
			drawReload(state, iconPos, iconSize, reloadColor, backwards, percent);
			return null;
		}

		@Override
		public String onReload(InputState state, Vector3i iconPos, Vector3i iconSize, Vector4f reloadColor, boolean backwards, float percent) {
			drawReload(state, iconPos, iconSize, reloadColor, backwards, percent);
			return null;
		}


		@Override
		public String onFull(InputState state, Vector3i iconPos, Vector3i iconSize, Vector4f reloadColor, boolean backwards, float percent, long controllerPos) {

			return null;
		}

		@Override
		public void drawForElementCollectionManager(InputState state, Vector3i iconPos, Vector3i iconSize, Vector4f reloadcolor, long controllerPos) {
			CM wep = getCollectionManagersMap().get(controllerPos);
			if(wep != null) {
				BeamCombiSettings cp = wep.getWeaponChargeParams();
				if(cp.chargeTime > 0 && wep.beamCharge > 0) {
					if(chargesText == null) {
						chargesText = new GUITextOverlay(FontSize.MEDIUM_15, (InputState) getState());
						chargesText.onInit();
					}
					float p = Math.min(wep.beamCharge / cp.chargeTime, 0.99999f);
					drawReload(state, iconPos, iconSize, chargeColor, false, p, true, wep.beamCharge, (int) cp.chargeTime, -1, chargesText);
				}
			}
		}

	}

	private final DrawReloadListener drawReloadListener = new DrawReloadListener();

	@Override
	public void drawReloads(Vector3i iconPos, Vector3i iconSize, long controllerPos) {
		handleReload(iconPos, iconSize, controllerPos, drawReloadListener);
	}

	public boolean isUsingRegisteredActivation() {
		return true;
	}
}
