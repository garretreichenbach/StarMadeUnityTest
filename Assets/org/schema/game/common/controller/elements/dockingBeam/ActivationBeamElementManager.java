package org.schema.game.common.controller.elements.dockingBeam;

import javax.vecmath.Vector3f;

import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.GameClientController;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelpManager;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelperContainer.Hos;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.weapon.WeaponRowElementInterface;
import org.schema.game.client.view.gui.weapon.WeaponSegmentControllerUsableElement;
import org.schema.game.common.controller.BeamHandlerContainer;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.ManagerActivityInterface;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ManagerReloadInterface;
import org.schema.game.common.controller.elements.ManagerUpdatableInterface;
import org.schema.game.common.controller.elements.SegmentControllerUsable;
import org.schema.game.common.controller.elements.UsableElementManager;
import org.schema.game.common.controller.elements.beam.BeamCommand;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.beam.AbstractBeamHandler;
import org.schema.game.common.data.element.beam.BeamReloadCallback;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.ContextFilter;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.sound.controller.AudioController;

public class ActivationBeamElementManager extends UsableElementManager implements BeamHandlerContainer<SegmentController>, BeamReloadCallback, ManagerUpdatableInterface {

	@ConfigurationElement(name = "Distance")
	public static float DOCKING_BEAM_DISTANCE = 100;

	private Vector3f shootingDirTemp = new Vector3f();

	private ActivationBeamHandler activationBeamManager;

	private boolean addedUsable;

	public ActivationBeamElementManager(SegmentController segmentController) {
		super(segmentController);
		this.activationBeamManager = new ActivationBeamHandler(segmentController, this);
	}

	/**
	 * @return the dockingBeamManager
	 */
	public ActivationBeamHandler getActivationBeamManager() {
		return activationBeamManager;
	}

	@Override
	public void update(Timer timer) {
		if (!addedUsable) {
			getManagerContainer().addPlayerUsable(new ActivationBeamDockerUsable(getManagerContainer()));
			addedUsable = true;
		}
		activationBeamManager.update(timer);
	}

	@Override
	public ControllerManagerGUI getGUIUnitValues(ElementCollection firingUnit, ElementCollectionManager col, ControlBlockElementCollectionManager supportCol, ControlBlockElementCollectionManager effectCol) {
		return null;
	}

	@Override
	protected String getTag() {
		return "dockingbeam";
	}

	@Override
	public ElementCollectionManager getNewCollectionManager(SegmentPiece position, Class clazz) {
		throw new IllegalAccessError("This should not be called. ever");
	}

	@Override
	public String getManagerName() {
		return Lng.str("Docking Beam System Collective");
	}

	private final Vector3i controlledFromTmp = new Vector3i();

	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {
		if (!unit.isFlightControllerActive() || !unit.isUnitInPlayerSector()) {
			return;
		}
		if (!KeyboardMappings.SHIP_PRIMARY_FIRE.isDown() && !KeyboardMappings.SHIP_ZOOM.isDown()) {
			return;
		}
		// Vector3i controlledFrom = new Vector3i((Vector3i )unit.parameter);
		Vector3i controlledFrom = unit.getControlledFrom(controlledFromTmp);
		// autorequest true previously
		SegmentPiece fromPiece = getSegmentBuffer().getPointUnsave(controlledFrom);
		// System.err.println(getSegmentController().getState()+" "+getSegmentController()+" CONTROLLED: "+controlledFrom+": "+fromPiece);
		if (fromPiece == null) {
			return;
		}
		if (getSegmentController().railController.isDocked()) {
			if (getSegmentController().isClientOwnObject() && timer.currentTime - getSegmentController().railController.isDockedSince() > 1000 && timer.currentTime - getSegmentController().railController.getLastDisconnect() > 1000 && getSegmentController().railController.isShipyardDocked()) {
				if (!getSegmentController().isVirtualBlueprint()) {
					getSegmentController().railController.disconnectClient();
				} else {
					((GameClientController) getState().getController()).popupAlertTextMessage(Lng.str("Can't undock a design!"), 0);
				}
			}
			return;
		}
		unit.getForward(shootingDirTemp);
		shootingDirTemp.scale(DOCKING_BEAM_DISTANCE);
		Vector3f from = new Vector3f();
		getSegmentController().getAbsoluteElementWorldPosition(new Vector3i(controlledFrom.x - SegmentData.SEG_HALF, controlledFrom.y - SegmentData.SEG_HALF, controlledFrom.z - SegmentData.SEG_HALF), from);
		Vector3f to = new Vector3f();
		to.set(from);
		to.add(shootingDirTemp);
		float coolDown = 0;
		float burstTime = -1;
		float initialTicks = 1;
		float powerConsumed = 0;
		Vector3f relativePos = new Vector3f(controlledFrom.x - SegmentData.SEG_HALF, controlledFrom.y - SegmentData.SEG_HALF, controlledFrom.z - SegmentData.SEG_HALF);
		BeamCommand b = new BeamCommand();
		b.currentTime = timer.currentTime;
		b.identifier = ElementCollection.getIndex(controlledFrom);
		b.relativePos.set(relativePos);
		b.reloadCallback = this;
		b.from.set(from);
		b.to.set(to);
		b.playerState = unit.getPlayerState();
		b.beamTimeout = 0.1f;
		b.tickRate = 0.1f;
		b.beamPower = 0;
		b.controllerPos = controlledFrom;
		b.cooldownSec = coolDown;
		b.bursttime = burstTime;
		b.initialTicks = initialTicks;
		b.powerConsumedByTick = powerConsumed;
		b.powerConsumedExtraByTick = powerConsumed;
		b.weaponId = PlayerUsableInterface.USABLE_ID_ACTIVATION_BEAM;
		b.latchOn = false;
		b.checkLatchConnection = false;
		b.firendlyFire = true;
		b.penetrating = false;
		b.acidDamagePercent = 0;
		b.minEffectiveRange = 1;
		b.minEffectiveValue = 1;
		b.maxEffectiveRange = 1;
		b.maxEffectiveValue = 1;
		activationBeamManager.addBeam(b);
		/*AudioController.fireAudioEvent("BEAM_FIRE", new AudioTag[] { AudioTags.GAME, AudioTags.SHIP, AudioTags.WEAPON, AudioTags.BEAM, AudioTags.ACTIVATE, AudioTags.FIRE }, AudioParam.START, AudioController.ent(getSegmentController(), fromPiece, b.identifier, 10))*/
		AudioController.fireAudioEventID(881, AudioController.ent(getSegmentController(), fromPiece, b.identifier, 10));
		// dockingBeamManager.addBeam(controlledFrom, relativePos, this, from, to, unit.getPlayerState(), -1, 0, 0, coolDown, burstTime, initialTicks, powerConsumed, effectType, effectRatio, effectSize);
		getManagerContainer().onAction();
	}

	@Override
	public void setShotReloading(long reload) {
	}

	@Override
	public boolean canUse(long curTime, boolean popupText) {
		return true;
	}

	@Override
	public boolean isInitializing(long curTime) {
		return false;
	}

	@Override
	public long getNextShoot() {
		return 0;
	}

	@Override
	public long getCurrentReloadTime() {
		return 0;
	}

	public class ActivationBeamDockerUsable extends SegmentControllerUsable {

		public ActivationBeamDockerUsable(ManagerContainer<?> o) {
			super(o);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + (int) (PlayerUsableInterface.USABLE_ID_ACTIVATION_BEAM ^ (PlayerUsableInterface.USABLE_ID_ACTIVATION_BEAM >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof ActivationBeamDockerUsable;
		}

		private ActivationBeamElementManager getOuterType() {
			return ActivationBeamElementManager.this;
		}

		@Override
		public void addHudConext(ControllerStateUnit unit, HudContextHelpManager h, Hos hos) {
			h.addHelper(KeyboardMappings.SHIP_PRIMARY_FIRE, Lng.str("Activate/Deactivate target block"), hos, ContextFilter.IMPORTANT);
		}

		@Override
		public WeaponRowElementInterface getWeaponRow() {
			return new WeaponSegmentControllerUsableElement(this);
		}

		@Override
		public boolean isAddToPlayerUsable() {
			return true;
		}

		@Override
		public boolean isControllerConnectedTo(long index, short type) {
			return type == ElementKeyMap.CORE_ID;
		}

		@Override
		public void onPlayerDetachedFromThisOrADock(ManagedUsableSegmentController<?> originalCaller, PlayerState pState, PlayerControllable newAttached) {
		}

		@Override
		public boolean isPlayerUsable() {
			return true;
		}

		@Override
		public long getUsableId() {
			return PlayerUsableInterface.USABLE_ID_ACTIVATION_BEAM;
		}

		@Override
		public void handleKeyPress(ControllerStateInterface unit, Timer timer) {
			handle(unit, timer);
		}

		@Override
		public ManagerReloadInterface getReloadInterface() {
			return null;
		}

		@Override
		public ManagerActivityInterface getActivityInterface() {
			return null;
		}

		@Override
		public String getWeaponRowName() {
			return getName();
		}

		@Override
		public short getWeaponRowIcon() {
			return ElementKeyMap.ACTIVAION_BLOCK_ID;
		}

		@Override
		public String getName() {
			return Lng.str("Activation Beam");
		}
	}

	@Override
	public boolean canUpdate() {
		return true;
	}

	@Override
	public void onNoUpdate(Timer timer) {
	}

	@Override
	public void onElementCollectionsChanged() {
	}

	@Override
	public void sendHitConfirm(byte damageType) {
	}

	@Override
	public boolean isSegmentController() {
		return true;
	}

	@Override
	public SimpleTransformableSendableObject<?> getShootingEntity() {
		return getSegmentController();
	}

	@Override
	public int getFactionId() {
		return getSegmentController().getFactionId();
	}

	@Override
	public String getName() {
		return Lng.str("Activation Beam");
	}

	@Override
	public AbstractOwnerState getOwnerState() {
		return getSegmentController().getOwnerState();
	}

	@Override
	public void sendClientMessage(String str, byte type) {
		getSegmentController().sendClientMessage(str, type);
	}

	@Override
	public void sendServerMessage(Object[] astr, byte msgType) {
		getSegmentController().sendServerMessage(astr, msgType);
	}

	@Override
	public float getDamageGivenMultiplier() {
		return 1;
	}

	@Override
	public InterEffectSet getAttackEffectSet(long weaponId, DamageDealerType damageDealerType) {
		return null;
	}

	@Override
	public MetaWeaponEffectInterface getMetaWeaponEffect(long weaponId, DamageDealerType damageDealerType) {
		return null;
	}

	@Override
	public int getSectorId() {
		return getSegmentController().getSectorId();
	}

	@Override
	public AbstractBeamHandler<SegmentController> getHandler() {
		return activationBeamManager;
	}

	@Override
	public void flagCheckUpdatable() {
		setUpdatable(false);
	}

	@Override
	public void flagBeamFiredWithoutTimeout() {
	}
}
