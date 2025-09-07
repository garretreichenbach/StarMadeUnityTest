package org.schema.game.common.data.player;

import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import org.schema.common.FastMath;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.HitType;
import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.controller.elements.ShootingRespose;
import org.schema.game.common.controller.elements.beam.BeamCommand;
import org.schema.game.common.data.MetaObjectState;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.SendableTypes;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.beam.BeamReloadCallback;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.element.meta.weapon.GrappleBeam;
import org.schema.game.common.data.element.meta.weapon.SniperRifle;
import org.schema.game.common.data.element.meta.weapon.TorchBeam;
import org.schema.game.common.data.element.meta.weapon.Weapon;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.network.CharacterBlockActivation;
import org.schema.game.network.objects.NetworkPlayerHumanCharacter;
import org.schema.game.network.objects.remote.RemoteCharacterBlockActivation;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.GameServerState.PlayerAttachedInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.NetworkGravity;
import org.schema.schine.network.SendableType;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.FileExt;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import java.io.File;
import java.util.ArrayList;

import static org.schema.game.common.data.world.GravityState.G;

public class PlayerCharacter extends AbstractCharacter<PlayerState> implements PlayerControllable {

	public static final float headUpScale = 0.685f;

	public static final float shoulderUpScale = 0.385f;

	private final ArrayList<PlayerState> attachedPlayers = new ArrayList<PlayerState>();

	public LongArrayList waitingForToSpawn = new LongArrayList();

	public int spawnOnObjectId;

	public Vector3f spawnOnObjectLocalPos;

	boolean wasMouseDown = false;

	Vector3f shootingDirTemp = new Vector3f(0, 1, 0);

	float yaw = 0;

	private float characterWidth = 0.2f;

	private float characterHeight = 1.13f;

	private float characterMargin = 0.1f;

	private float characterHeightOffset = 0.2f;

	private NetworkPlayerHumanCharacter networkPlayerCharacterObject;

	private int clientOwnerId;

	private PlayerState ownerState;

	@Override
	public SendableType getSendableType() {
		return SendableTypes.PLAYER_CHARACTER;
	}

	private BeamReloadCallback beamReloadCallback = new BeamReloadCallback() {

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

		@Override
		public boolean consumePower(float powerConsumtionDelta) {
			return true;
		}

		@Override
		public boolean canConsumePower(float powerConsumtionDelta) {
			return true;
		}

		@Override
		public double getPower() {
			return 1;
		}

		@Override
		public boolean isUsingPowerReactors() {
			return true;
		}

		@Override
		public void flagBeamFiredWithoutTimeout() {
		}
	};

	private Vector3f up = new Vector3f(0, 1, 0);

	private Vector3f down = new Vector3f(0, -1, 0);

	private float speed = 4;

	private Vector3i blockDim = new Vector3i(1, 1, 1);

	public PlayerCharacter(StateInterface state) {
		super(state);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#getType()
	 */
	@Override
	public EntityType getType() {
		return EntityType.ASTRONAUT;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#cleanUpOnEntityDelete()
	 */
	@Override
	public void cleanUpOnEntityDelete() {
		super.cleanUpOnEntityDelete();
		System.err.println("[DELETE] Cleaning up playerCharacter for playerState " + getOwnerState() + " on " + getState() + "; " + this.getUniqueIdentifier() + "; " + this);
		if (getState() instanceof GameClientState) {
			GameClientState s = ((GameClientState) getState());
			/*
			 * s.getCurrentPlayerObject()  is already set to null because of control request
			 */
			if (s.getCharacter() != null && s.getCharacter().getId() == this.getId()) {
				s.getController().popupAlertTextMessage(Lng.str("\nYOU DIED!\n(some of your credits are lost)\n(the character entity got deleted)"), 0);
				s.setCharacter(null);
				s.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().exitShip(true);
				System.err.println("[DELETE] Deactivating character control manager for: " + this + "; (setting spawn screen for attached player)");
				s.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().setActive(false);
				s.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().setActive(false);
				s.getGlobalGameControlManager().getIngameControlManager().getAutoRoamController().setActive(true);
				s.setCurrentPlayerObject(null);
			}
		}
	}

	@Override
	public void fromTagStructure(Tag tag) {
		assert ("PlayerCharacter".equals(this.getClass().getSimpleName()));
		Tag[] subTags = (Tag[]) tag.getValue();
		setId((Integer) subTags[0].getValue());
		// stepHeight = (Float) subTags[2].getValue();
		super.fromTagStructure(subTags[3]);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#toTagStructure()
	 */
	@Override
	public Tag toTagStructure() {
		Tag idTag = new Tag(Type.INT, "id", this.getId());
		Tag speedTag = new Tag(Type.FLOAT, "speed", speed);
		Tag stepHeightTag = new Tag(Type.FLOAT, "stepHeight", stepHeight);
		return new Tag(Type.STRUCT, "PlayerCharacter", new Tag[] { idTag, speedTag, stepHeightTag, super.toTagStructure(), FinishTag.INST });
	}

	@Override
	public String toNiceString() {
		if (attachedPlayers.isEmpty()) {
			return "ghost <bug: playerCharacter without host>";
		} else {
			return attachedPlayers.get(0).getName();
		}
	}

	@Override
	public String getRealName() {
		return ownerState != null ? ownerState.getName() : "(unknown)";
	}

	/**
	 * @return the clientOwnerId
	 */
	public int getClientOwnerId() {
		return clientOwnerId;
	}

	public void setClientOwnerId(int id) {
		clientOwnerId = id;
	}

	@Override
	public void handleKeyEvent(ControllerStateUnit unit, KeyboardMappings mapping, Timer timer) {
		if (mapping == KeyboardMappings.REMOVE_BLOCK_CHARACTER) {
			boolean weaponSelected = false;
			if (!getOwnerState().getInventory().isSlotEmpty(getOwnerState().getSelectedBuildSlot())) {
				if (getOwnerState().getInventory().getType(getOwnerState().getSelectedBuildSlot()) < 0) {
					int metaId = getOwnerState().getInventory().getMeta(getOwnerState().getSelectedBuildSlot());
					MetaObject object = ((MetaObjectState) getState()).getMetaObjectManager().getObject(metaId);
					if (object != null) {
						if (object instanceof Weapon) {
							weaponSelected = true;
						}
					}
				}
			}
			if (!weaponSelected) {
				shootFabricatorBeam(unit);
			}
		}
		Inventory inventory = unit.playerState.getInventory(null);
		int selectedBuildSlot = unit.playerState.getSelectedBuildSlot();
		if (!inventory.isSlotEmpty(selectedBuildSlot)) {
			if (inventory.getType(selectedBuildSlot) < 0) {
				int metaId = inventory.getMeta(selectedBuildSlot);
				MetaObject object = ((MetaObjectState) getState()).getMetaObjectManager().getObject(metaId);
				if (object != null) {
					object.handleKeyEvent(this, unit, mapping);
				}
			}
		}
	}

	@Override
	public void handleKeyPress(Timer timer, ControllerStateInterface u) {
		if (!(u instanceof ControllerStateUnit)) {
			return;
		}
		ControllerStateUnit unit = (ControllerStateUnit) u;
		unit.playerState.getShopsInDistance().addAll(getShopsInDistance());
		handleMovingInput(timer, unit);
		Inventory inventory = unit.playerState.getInventory(null);
		int selectedBuildSlot = unit.playerState.getSelectedBuildSlot();
		if (!inventory.isSlotEmpty(selectedBuildSlot)) {
			if (inventory.getType(selectedBuildSlot) < 0) {
				int metaId = inventory.getMeta(selectedBuildSlot);
				MetaObject object = ((MetaObjectState) getState()).getMetaObjectManager().getObject(metaId);
				if (object != null) {
					object.handleKeyPress(this, unit, timer);
				}
			}
		}
	}

	@Override
	public void onAttachPlayer(PlayerState playerState, Sendable detachedFrom, Vector3i fromWhere, Vector3i parameter) {
		lastAttach = System.currentTimeMillis();
		setHidden(false);
		if (isClientOwnObject()) {
			networkPlayerCharacterObject.hidden.set(false);
		}
		onPhysicsRemove();
		Transform worldTransform = new Transform(getWorldTransform());
		if (detachedFrom != null) {
			if (getSectorId() != ((SimpleTransformableSendableObject) detachedFrom).getSectorId()) {
				if (!isOnServer()) {
					((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("SPAWNING POINT ERROR DETECTED\nPLEASE SEND IN A REPORT\nSECTOR DIFF: %s / %s", getSectorId(), ((SimpleTransformableSendableObject) detachedFrom).getSectorId()), 0);
				} else {
					playerState.sendServerMessage(new ServerMessage(Lng.astr("SPAWNING POINT ERROR DETECTED\nPLEASE SEND IN A REPORT\nSECTOR DIFF: %s / %s", getSectorId(), ((SimpleTransformableSendableObject) detachedFrom).getSectorId()), ServerMessage.MESSAGE_TYPE_ERROR, playerState.getId()));
				}
				System.err.println("[PLAYERCHARACTER][DETACHED][" + getState() + "] Exception while SETTING SPAWNING POINT FOR " + this + ": (from " + detachedFrom + ") SECTOR DIFF: " + getSectorId() + " / " + ((SimpleTransformableSendableObject) detachedFrom).getSectorId() + "; playerState sector: " + playerState.getId());
				setSectorId(((SimpleTransformableSendableObject) detachedFrom).getSectorId());
			}
			worldTransform.set(((SimpleTransformableSendableObject) detachedFrom).getWorldTransform());
			if (isClientOwnObject() && detachedFrom instanceof SegmentController && getGravity().source == null) {
				scheduleGravity(new Vector3f(0, 0, 0), ((SegmentController) detachedFrom).railController.getRoot());
			}
		} else {
			if (playerState.getControllerState().getLastTransform() != null) {
				System.err.println(getState() + "[PLAYERCHARACTER] USING LAST TRANSFORM ON ATTACH PLAYER " + playerState.getControllerState().getLastTransform().origin);
				worldTransform.set(playerState.getControllerState().getLastTransform());
			}
		}
		if (fromWhere != null) {
			Vector3f p = new Vector3f(fromWhere.x, fromWhere.y, fromWhere.z);
			if (parameter != null) {
				p.x += parameter.x;
				p.y += parameter.y;
				p.z += parameter.z;
			}
			if (detachedFrom != null && detachedFrom instanceof SegmentController) {
				// shift when detached from blockStructure
				p.x -= SegmentData.SEG_HALF;
				p.y -= SegmentData.SEG_HALF;
				p.z -= SegmentData.SEG_HALF;
			}
			worldTransform.basis.transform(p);
			worldTransform.origin.add(p);
		}
		// worldTransform
		// System.err.println("[CHARACTER] on Attach "+this+": "+getWorldTransform().origin+" -> "+worldTransform.origin+" (from = "+detachedFrom+")");
		getInitialTransform().set(worldTransform);
		initPhysics();
		if (!isOnServer() && ((GameClientState) getState()).getPlayer() == playerState) {
			for (PlayerAttachedInterface l : ((GameClientState) getState()).playerAttachedListeners) {
				l.onPlayerAttached(playerState, detachedFrom, fromWhere, parameter);
			}
			if (!((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().isActive()) {
				((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().setActive(true);
			}
			((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getPlayerCharacterManager().setActive(true);
		}

		if (playerState.isClientOwnPlayer()) {
			//((GameClientState)getState()).getController().queueBackgroundAudio("0022_ambience loop - space wind omnious light warbling tones (loop)", 0.1f);
		}
		flagGravityUpdate = true;
	}

	@Override
	public void onDetachPlayer(PlayerState playerState, boolean hide, Vector3i parameter) {
		if (!isHidden()) {
			setHidden(hide);
			if (isClientOwnObject()) {
				networkPlayerCharacterObject.hidden.set(hide);
			}
			onPhysicsRemove();
		}
		if (!isOnServer() && ((GameClientState) getState()).getPlayer() == playerState) {
			for (PlayerAttachedInterface l : ((GameClientState) getState()).playerAttachedListeners) {
				l.onPlayerDetached(playerState, hide, parameter);
			}
			((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getPlayerCharacterManager().setActive(false);
		}
	}

	@Override
	public boolean hasSpectatorPlayers() {
		for (PlayerState s : attachedPlayers) {
			if (s.isSpectator()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isControlHandled(ControllerStateInterface unit) {
		// must be in same sector and must be either person in the right controller mode or remote controlled (e.g. AI)
		return unit.isUnitInPlayerSector() && (!(unit instanceof ControllerStateUnit) || ((ControllerStateUnit) unit).playerState.getNetworkObject().activeControllerMask.get(AbstractControlManager.CONTROLLER_PLAYER_EXTERN).get());
	}

	public void shootFabricatorBeam(ControllerStateUnit unit) {
		shootBeam(unit, PersonalBeamHandler.SALVAGE, 0, null, false, true);
	}

	private void shootBeam(ControllerStateUnit unit, int type, float power, MetaObject originMetaObject, boolean addButton, boolean removeButton) {
		Vector3f from = new Vector3f();
		Vector3f to = new Vector3f();
		if (needsYaw()) {
			unit.playerState.getForward(shootingDirTemp);
			shootingDirTemp.scale(2);
		// shootingDirTemp.scale(5000);
		}
		Vector3f relPos;
		if (type == PersonalBeamHandler.SALVAGE) {
			relPos = new Vector3f(-0.2f, 0.18f, 0.2f);
		} else {
			relPos = new Vector3f(0f, 0.32f, 0.0f);
		}
		Transform t = new Transform();
		if (isOnServer()) {
			t.set(getWorldTransform());
		} else {
			t.set(getWorldTransformOnClient());
		}
		t.transform(from);
		Vector3f r = new Vector3f(relPos);
		getYawRotation().transform(r);
		from.add(r);
		to.set(getShoulderWorldTransform().origin);
		to.add(shootingDirTemp);
		float secsToHarvest = 0.7f;
		short effectType = 0;
		float effectRatio = 0;
		float effectSize = 0;
		float coolDown = 0;
		float burstTime = -1;
		float initialTicks = 0;
		float powerConsumptionDelta = 0;
		// use default
		float beamTimeout = 0.1f;
		BeamCommand b = new BeamCommand();
		b.currentTime = System.currentTimeMillis();
		b.beamType = type;
		b.identifier = 0;
		b.relativePos.set(relPos);
		b.reloadCallback = beamReloadCallback;
		b.from.set(from);
		b.to.set(to);
		b.originMetaObject = originMetaObject;
		b.playerState = getOwnerState();
		b.beamTimeout = beamTimeout;
		b.tickRate = secsToHarvest;
		b.controllerPos = null;
		b.beamPower = power;
		// b.latchOn = true;
		b.hitType = originMetaObject instanceof Weapon ? ((Weapon) originMetaObject).getHitType() : HitType.GENERAL;
		b.cooldownSec = coolDown;
		b.bursttime = burstTime;
		b.initialTicks = initialTicks;
		b.powerConsumedByTick = 0;
		b.powerConsumedExtraByTick = 0;
		b.weaponId = originMetaObject != null ? originMetaObject.getId() : MetaObject.SALVAGE_TOOL_ID;
		b.handheld = true;
		b.minEffectiveRange = 1;
		b.minEffectiveValue = 1;
		b.maxEffectiveRange = 1;
		b.maxEffectiveValue = 1;
		b.latchOn = false;
		b.checkLatchConnection = false;
		b.firendlyFire = true;
		b.penetrating = false;
		b.acidDamagePercent = 0;
		ShootingRespose response = getHandler().addBeam(b);
	}

	public void shootHealingBeam(ControllerStateUnit unit, float power, MetaObject originMetaObject, boolean addButton, boolean removeButton) {
		shootBeam(unit, PersonalBeamHandler.HEAL, power, originMetaObject, addButton, removeButton);
	}

	public void shootSniperBeam(ControllerStateUnit unit, float power, float reload, float distance, MetaObject originMetaObject, boolean addButton, boolean removeButton) {
		shootWeaponBeam(unit, power, reload, distance, originMetaObject, addButton, removeButton, PersonalBeamHandler.SNIPER, ((SniperRifle) originMetaObject).reloadCallback);
	}

	public void shootGrappleBeam(ControllerStateUnit unit, float power, float reload, float distance, MetaObject originMetaObject, boolean addButton, boolean removeButton) {
		shootWeaponBeam(unit, power, reload, distance, originMetaObject, addButton, removeButton, PersonalBeamHandler.GRAPPLE, ((GrappleBeam) originMetaObject).reloadCallback);
	}

	public void shootTorchBeam(ControllerStateUnit unit, float power, float reload, float distance, MetaObject originMetaObject, boolean addButton, boolean removeButton) {
		shootWeaponBeam(unit, power, reload, distance, originMetaObject, addButton, removeButton, PersonalBeamHandler.TORCH, ((TorchBeam) originMetaObject).reloadCallback);
	}

	public void shootWeaponBeam(ControllerStateUnit unit, float power, float reload, float distance, MetaObject originMetaObject, boolean addButton, boolean removeButton, int beamType, BeamReloadCallback reloadCallback) {
		Vector3f from = new Vector3f();
		Vector3f to = new Vector3f();
		if (needsYaw()) {
			unit.playerState.getForward(shootingDirTemp);
			shootingDirTemp.scale(distance);
		}
		Vector3f relPos = new Vector3f(0f, 0.32f, 0.0f);
		Transform t = new Transform();
		t.set(getShoulderWorldTransform());
		t.transform(from);
		// t.inverse();
		// Vector3f f = new Vector3f(unit.playerState.getForward());
		// f.scale(0.4f);
		// t.basis.transform(f);
		// if(type == PersonalBeamHandler.SALVAGE){
		// }else{
		// relPos.add(f);
		// }
		Vector3f r = new Vector3f(relPos);
		getYawRotation().transform(r);
		from.add(r);
		to.set(getShoulderWorldTransform().origin);
		to.add(shootingDirTemp);
		float tickrate = 1.55f;
		short effectType = 0;
		float effectRatio = 0;
		float effectSize = 0;
		float coolDown = reload;
		float burstTime = 0.1f;
		float initialTicks = 1;
		float powerConsumptionDelta = 0;
		// use default
		float beamTimeout = 0.1f;
		BeamCommand b = new BeamCommand();
		b.currentTime = System.currentTimeMillis();
		b.beamType = beamType;
		b.identifier = 0;
		b.relativePos.set(relPos);
		b.reloadCallback = reloadCallback;
		b.from.set(from);
		b.to.set(to);
		b.originMetaObject = originMetaObject;
		b.playerState = getOwnerState();
		b.beamTimeout = beamTimeout;
		b.tickRate = tickrate;
		b.hitType = originMetaObject instanceof Weapon ? ((Weapon) originMetaObject).getHitType() : HitType.GENERAL;
		b.controllerPos = null;
		b.beamPower = power;
		b.cooldownSec = coolDown;
		b.bursttime = burstTime;
		b.initialTicks = initialTicks;
		b.powerConsumedByTick = 0;
		b.powerConsumedExtraByTick = 0;
		b.weaponId = originMetaObject.getId();
		b.handheld = true;
		b.ignoreShields = (originMetaObject instanceof Weapon && ((Weapon) originMetaObject).isIgnoringShields());
		//b.ignoreArmor = (originMetaObject instanceof Weapon && ((Weapon) originMetaObject).isIgnoringArmor());
		
		b.minEffectiveRange = 1;
		b.minEffectiveValue = 1;
		b.maxEffectiveRange = 1;
		b.maxEffectiveValue = 1;
		b.latchOn = false;
		b.firendlyFire = true;
		b.penetrating = false;
		b.acidDamagePercent = 0;
		ShootingRespose response = getHandler().addBeam(b);
	}

	public void shootMarkerBeam(ControllerStateUnit unit, float power, MetaObject originMetaObject, boolean addButton, boolean removeButton) {
		shootBeam(unit, PersonalBeamHandler.MARKER, power, originMetaObject, addButton, removeButton);
	}

	public void shootTransporterMarkerBeam(ControllerStateUnit unit, float power, MetaObject originMetaObject, boolean addButton, boolean removeButton) {
		shootBeam(unit, PersonalBeamHandler.TRANSPORTER_MARKER, power, originMetaObject, addButton, removeButton);
	}

	public void shootPowerSupplyBeam(ControllerStateUnit unit, float power, MetaObject originMetaObject, boolean addButton, boolean removeButton) {
		shootBeam(unit, PersonalBeamHandler.POWER_SUPPLY, power, originMetaObject, addButton, removeButton);
	}

	protected Matrix3f getYawRotation() {
		Vector3f f = getOwnerState().getForward(new Vector3f());
		Vector3f u = getOwnerState().getUp(new Vector3f());
		if (needsYaw()) {
			Transform gravityOrientation = new Transform();
			gravityOrientation.setIdentity();
			GlUtil.setRightVector(getCharacterController().upAxisDirection[0], gravityOrientation);
			GlUtil.setUpVector(getCharacterController().upAxisDirection[1], gravityOrientation);
			GlUtil.setForwardVector(getCharacterController().upAxisDirection[2], gravityOrientation);
			gravityOrientation.inverse();
			gravityOrientation.transform(f);
			gravityOrientation.transform(u);
			if (f.epsilonEquals(up, 0.01f)) {
				yaw = FastMath.atan2Fast(-u.x, -u.z);
			} else if (f.epsilonEquals(down, 0.01f)) {
				yaw = FastMath.atan2Fast(u.x, u.z);
			} else {
				yaw = FastMath.atan2Fast(f.x, f.z);
			}
		}
		Matrix3f aa = new Matrix3f();
		aa.setIdentity();
		aa.rotY(yaw);
		return aa;
	}

	private boolean needsYaw() {
		return !KeyboardMappings.FREE_CAM.isDownOrSticky(getState());
	}

	public void handleMovingInput(Timer timer, ControllerStateUnit unit) {
		if (getOwnerState().isSitting()) {
			return;
		}
		if (isClientOwnObject() && !checkClintSpawnSanity(((GameClientState) getState()).getCurrentSectorEntities())) {
			((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Waiting for segments(chunks)\nto load %s...", waitingForToSpawn.size()), "W", 0);
			waitingForToSpawn.clear();
			return;
		}
		// //		System.err.println("Owner of "+this+" is "+owner);
		Vector3f up = unit.playerState.getUp(new Vector3f());
		Vector3f forward = unit.playerState.getForward(new Vector3f());
		Vector3f right = unit.playerState.getRight(new Vector3f());
		if (getGravity().isGravityOn()) {
			Vector3f projectedRight = new Vector3f();
			GlUtil.project(getCharacterController().upAxisDirection[1], right, projectedRight);
			projectedRight.normalize();
			Vector3f down = new Vector3f(getCharacterController().upAxisDirection[1]);
			down.negate();
			Vector3f projectedForward = new Vector3f();
			GlUtil.project(getCharacterController().upAxisDirection[1], forward, projectedForward);
			projectedForward.normalize();
			if (forward.epsilonEquals(getCharacterController().upAxisDirection[1], 0.01f)) {
				// System.err.println("LOOKING DIRECTLY UP -> "+projectedForward);
				projectedForward.cross(projectedRight, forward);
				projectedForward.normalize();
			} else if (forward.epsilonEquals(down, 0.01f)) {
				// System.err.println("LOOKING DIRECTLY DOWN -> "+projectedForward);
				projectedForward.cross(forward, projectedRight);
				projectedForward.normalize();
			}
			// System.err.println("FORWARD: "+forward+" -> "+projectedForward+" UP "+getCharacterController().upAxisDirection[1]);
			right.set(projectedRight);
			up.set(getCharacterController().upAxisDirection[1]);
			forward.set(projectedForward);
		}
		Vector3f down = new Vector3f(up);
		down.scale(-1);
		Vector3f left = new Vector3f(right);
		// left/right are switched :(
		right.scale(-1);
		Vector3f backward = new Vector3f(forward);
		backward.scale(-1);
		Vector3f dir = new Vector3f();
		dir.set(0, 0, 0);
		if (isClientOwnObject() && !getGravity().isGravityOn() && unit.playerState.isDown(KeyboardMappings.GRAPPLING_HOOK)) {
			grappleToNearest();
		}
		if (unit.playerState.isDown(KeyboardMappings.JUMP)) {
			if (getGravity().isGravityOn()) {
				getCharacterController().jump();
			}
		} else {
			getCharacterController().breakJump(timer);
		}
		if (getGravity().isGravityOn()) {
			if (unit.playerState.isDown(KeyboardMappings.FORWARD)) {
				dir.add(forward);
			}
			if (unit.playerState.isDown(KeyboardMappings.BACKWARDS)) {
				dir.add(backward);
			}
			if (unit.playerState.isDown(KeyboardMappings.STRAFE_LEFT)) {
				dir.add(left);
			}
			if (unit.playerState.isDown(KeyboardMappings.STRAFE_RIGHT)) {
				dir.add(right);
			}
			getOwnerState().handleJoystickDir(dir, forward, right, up);
		} else {
			if (unit.playerState.isDown(KeyboardMappings.FORWARD)) {
				dir.add(forward);
			}
			if (unit.playerState.isDown(KeyboardMappings.BACKWARDS)) {
				dir.add(backward);
			}
			if (unit.playerState.isDown(KeyboardMappings.STRAFE_LEFT)) {
				dir.add(left);
			}
			if (unit.playerState.isDown(KeyboardMappings.STRAFE_RIGHT)) {
				dir.add(right);
			}
			if (unit.playerState.isDown(KeyboardMappings.UP)) {
				dir.add(up);
			}
			if (unit.playerState.isDown(KeyboardMappings.DOWN)) {
				dir.add(down);
			}
			getOwnerState().handleJoystickDir(dir, forward, right, up);
		}
		if (getCharacterController().getGravity() > 0) {
			forward = GlUtil.getForwardVector(forward, getWorldTransform());
			right = GlUtil.getRightVector(right, getWorldTransform());
			up = GlUtil.getUpVector(up, getWorldTransform());
			dir = GlUtil.projectOntoPlane(dir, dir, up);
		}
		if (unit.playerState.isDown(KeyboardMappings.WALK)) {
			dir.scale(0.5f);
		}
		// reducing vector accordingly if more keys are held
		dir.scale(1 / dir.length());
		dir.scale(timer.getDelta() * speed);
		if (dir.length() > 0) {
			characterController.setWalkDirectionStacked(dir);
		}
	}

	public void grappleToNearest() {
		if ((System.currentTimeMillis() - lastAlignRequest) > 500) {
			System.err.println("[CLIENT] CHARACTER ALIGN PRESSED ON " + getState() + "; " + attachedPlayers.get(0).getForward(new Vector3f()));
			SegmentPiece nearestPiece = getNearestPiece(true);
			if (nearestPiece != null) {
				SegmentController align = nearestPiece.getSegment().getSegmentController();
				scheduleGravity(new Vector3f(0, 0, 0), align);
				((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("Aligned to\n%s", align.toNiceString()), 0);
			} else if (getGravity().source != null && getGravity().magnitudeSquared() == 0) {
				final SimpleTransformableSendableObject source = getGravity().source;

				if ( EngineSettings.G_MUST_CONFIRM_DETACHEMENT_AT_SPEED.getFloat() >= 0 && source.getSpeedPercentServerLimitCurrent() >=
						EngineSettings.G_MUST_CONFIRM_DETACHEMENT_AT_SPEED.getFloat() * 0.01f) {
					PlayerGameOkCancelInput c = new PlayerGameOkCancelInput("CONFIRM_Exit", (GameClientState) getState(), Lng.str("Exit"), Lng.str("Do you really want to do that.\nThe current object was flying at %s m/s.\n\n(this message can be customized or\nturned off in the game option\n'Popup Detach Warning' in the ingame options menu)", StringTools.formatPointZero(source.getSpeedCurrent()))) {

						@Override
						public boolean isOccluded() {
							return false;
						}

						@Override
						public void onDeactivate() {
						}

						@Override
						public void pressedOK() {
							if (getGravity().source != null && getGravity().magnitudeSquared() == 0) {
								scheduleGravity(new Vector3f(0, 0, 0), null);
								getState().getController().popupInfoTextMessage(Lng.str("Ended alignment."), 0);
								deactivate();
							}
						}
					};
					c.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(959);
				} else {
					scheduleGravity(new Vector3f(0, 0, 0), null);
					((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("Ended alignment."), 0);
				}
			}
			lastAlignRequest = System.currentTimeMillis();
		}
	}

	@Override
	public void destroyPersistent() {
		assert (isOnServer());
		assert ("ENTITY_PLAYERCHARACTER_".equals(getType().dbPrefix));
		String uName = getType().dbPrefix + getOwnerState().getName();
		File f = new FileExt(GameServerState.ENTITY_DATABASE_PATH + uName + ".ent");
		f.delete();
	}

	@Override
	public float getCharacterHeightOffset() {
		return characterHeightOffset;
	}

	@Override
	public float getCharacterHeight() {
		return characterHeight;
	}

	@Override
	public float getCharacterWidth() {
		return characterWidth;
	}

	@Override
	public Vector3i getBlockDim() {
		return blockDim;
	}

	@Override
	public Transform getHeadWorldTransform() {
		Transform worldTransform = new Transform(super.getWorldTransform());
		Vector3f up = GlUtil.getUpVector(new Vector3f(), worldTransform);
		up.scale(headUpScale);
		worldTransform.origin.add(up);
		return worldTransform;
	}

	/**
	 * @return the attachedPlayers
	 */
	@Override
	public ArrayList<PlayerState> getAttachedPlayers() {
		return attachedPlayers;
	}

	@Override
	public PlayerState getOwnerState() {
		if (ownerState == null) {
			synchronized (getState().getLocalAndRemoteObjectContainer().getLocalObjects()) {
				for (Sendable s : getState().getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
					if (s instanceof PlayerState) {
						PlayerState p = (PlayerState) s;
						if (clientOwnerId == p.getClientId()) {
							ownerState = p;
							return ownerState;
						}
					}
				}
			}
		}
		if (ownerState == null) {
			System.err.println(getState() + " Exception: no owner state found for " + this);
		}
		return ownerState;
	}

	@Override
	public Transform getShoulderWorldTransform() {
		if (isOnServer()) {
			Transform worldTransform = new Transform(super.getWorldTransform());
			Vector3f up = GlUtil.getUpVector(new Vector3f(), worldTransform);
			up.scale(shoulderUpScale);
			worldTransform.origin.add(up);
			return worldTransform;
		} else {
			Transform worldTransform = new Transform(super.getWorldTransformOnClient());
			Vector3f up = GlUtil.getUpVector(new Vector3f(), worldTransform);
			up.scale(shoulderUpScale);
			worldTransform.origin.add(up);
			return worldTransform;
		}
	}

	@Override
	public NetworkPlayerHumanCharacter getNetworkObject() {
		return networkPlayerCharacterObject;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.player.AbstractCharacter#initFromNetworkObject(org.schema.schine.network.objects.NetworkObject)
	 */
	@Override
	public void initFromNetworkObject(NetworkObject o) {
		super.initFromNetworkObject(o);
		clientOwnerId = networkPlayerCharacterObject.clientOwnerId.get();
		assert (!isOnServer());
		System.err.println("[CLIENT][PLAYERCHARACTER] received character for client " + clientOwnerId);
		if (networkPlayerCharacterObject.gravity.get().gravityReceived) {
			this.receivedGravity = new NetworkGravity(networkPlayerCharacterObject.gravity.get());
			if (receivedGravity.gravityIdReceive != 0) {
				getGravity().grappleStart = System.currentTimeMillis();
			}
			networkPlayerCharacterObject.gravity.get().gravityReceived = false;
		}
		spawnOnObjectId = networkPlayerCharacterObject.spawnOnObjectId.getInt();
		spawnOnObjectLocalPos = networkPlayerCharacterObject.spawnOnObjectLocalPos.getVector();
	}

	@Override
	public void initPhysics() {
		if (isClientOwnObject() && spawnOnObjectId != 0) {
			Sendable sendable = getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(spawnOnObjectId);
			if (sendable != null && sendable instanceof SimpleTransformableSendableObject) {
				SimpleTransformableSendableObject sc = (SimpleTransformableSendableObject) sendable;
				Vector3f a = new Vector3f(spawnOnObjectLocalPos);
				sc.getWorldTransform().transform(a);
				getInitialTransform().origin.set(a);
				System.err.println("[CLIENT][SPAWN] Spawned on object: " + sc + "; " + sc.getWorldTransform().origin + ", local " + spawnOnObjectLocalPos + " -> " + a);
			// assert(false):a;
			}
			spawnOnObjectId = 0;
		}
		super.initPhysics();
	}

	@Override
	protected float getCharacterMargin() {
		return characterMargin;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.player.AbstractCharacter#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void updateLocal(Timer timer) {
		super.updateLocal(timer);
		if (!isOnServer()) {
			if (clientOwnerId == ((GameClientState) getState()).getId()) {
				getRemoteTransformable().useSmoother = false;
				getRemoteTransformable().setSendFromClient(true);
			} else {
				getRemoteTransformable().useSmoother = true;
				getRemoteTransformable().setSendFromClient(false);
			}
		}
	}

	@Override
	public boolean isClientOwnObject() {
		return !isOnServer() && attachedPlayers.contains(((GameClientState) getState()).getPlayer());
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#updateToFullNetworkObject()
	 */
	@Override
	public void updateToFullNetworkObject() {
		super.updateToFullNetworkObject();
		networkPlayerCharacterObject.clientOwnerId.set(clientOwnerId, true);
		getGravity().copyAccelerationTo(networkPlayerCharacterObject.gravity.get().gravity);
		networkPlayerCharacterObject.gravity.get().gravityId = getGravity().source != null ? getGravity().source.getId() : -1;
		networkPlayerCharacterObject.gravity.setChanged(true);
		if (spawnOnObjectId != 0 && spawnOnObjectLocalPos != null) {
			networkPlayerCharacterObject.spawnOnObjectId.set(spawnOnObjectId);
			networkPlayerCharacterObject.spawnOnObjectLocalPos.set(spawnOnObjectLocalPos);
		}
	}

	@Override
	public float getSpeed() {
		return speed;
	}

	@Override
	public void transformBeam(BeamState con) {
		con.from.set(0, 0, 0);
		Transform t = new Transform(getWorldTransform());
		t.transform(con.from);
		Vector3f r = new Vector3f(con.relativePos);
		getYawRotation().transform(r);
		con.from.add(r);
		if (!con.latchOn) {
			con.to.set(getShoulderWorldTransform().origin);
			con.to.add(shootingDirTemp);
		}
	}

	@Override
	public void newNetworkObject() {
		networkPlayerCharacterObject = new NetworkPlayerHumanCharacter(getState());
	}

	public void activateGravity(final SegmentPiece p) {
		if (!getGravity().isGravityOn()) {
			Vector3f gravity = new Vector3f(0, -G, 0);
			switch(p.getOrientation()) {
				case (Element.RIGHT) -> gravity.set(G, 0, 0);
				case (Element.LEFT) -> gravity.set(-G, 0, 0);
				case (Element.TOP) -> gravity.set(0, G, 0);
				case (Element.BOTTOM) -> gravity.set(0, -G, 0);
				case (Element.FRONT) -> gravity.set(0, 0, -G);
				case (Element.BACK) -> gravity.set(0, 0, G);
			}
			scheduleGravity(gravity, p.getSegment().getSegmentController());
			if (!isOnServer()) {
				((GameClientState) getState()).getController().popupGameTextMessage(Lng.str("You entered the\ngravity field of %s\n", p.getSegment().getSegmentController().toNiceString()), 0);
				System.err.println("[CLIENT][ACTIVATE] Enter gravity of " + p.getSegment().getSegmentController());
			} else {
				forcedCheckFlag = true;
				System.err.println("[SERVER][ACTIVATE] Enter gravity of " + p.getSegment().getSegmentController());
			}
		} else {
            final boolean isExit = p.getType() == ElementKeyMap.GRAVITY_EXIT_ID;
			final Vector3f gravity = new Vector3f(0, -G, 0);
			switch(p.getOrientation()) {
				case (Element.RIGHT) -> gravity.set(G, 0, 0);
				case (Element.LEFT) -> gravity.set(-G, 0, 0);
				case (Element.TOP) -> gravity.set(0, G, 0);
				case (Element.BOTTOM) -> gravity.set(0, -G, 0);
				case (Element.FRONT) -> gravity.set(0, 0, -G);
				case (Element.BACK) -> gravity.set(0, 0, G);
			}
			System.err.println("[PLAYEREXTERNAL][GRAVITY] " + this + " ALREADY HAS GRAV: " + getGravity().accelToString() + "; " + gravity);
			// Inside gravity, activating an exit module counts as activating a same-direction gravity module.
			if (getGravity().source == p.getSegment().getSegmentController() && (isExit || getGravity().accelerationEquals(gravity))) {

				if (!isOnServer()) {
					if (EngineSettings.G_MUST_CONFIRM_DETACHEMENT_AT_SPEED.getFloat() >= 0 && getGravity().source.getSpeedPercentServerLimitCurrent() >= EngineSettings.G_MUST_CONFIRM_DETACHEMENT_AT_SPEED.getFloat() * 0.01f) {
						PlayerGameOkCancelInput c = new PlayerGameOkCancelInput("CONFIRM_Exit", (GameClientState) getState(), "Exit", "Do you really want to do that.\nThe current object was flying at " + StringTools.formatPointZero(getGravity().source.getSpeedCurrent()) + " speed\n\n(this message can be customized or\nturned off in the game option\n'Popup Detach Warning' in the ingame options menu)") {

							@Override
							public boolean isOccluded() {
								return false;
							}

							@Override
							public void pressedOK() {
								if (getGravity().source != null && getGravity().accelerationEquals(gravity)) {
									scheduleGravity(new Vector3f(0, 0, 0), getGravity().source);
									getState().getController().popupGameTextMessage(Lng.str("You exited the \ngravity field of \n%s", p.getSegment().getSegmentController().toNiceString()), 0);
									System.err.println("[CLIENT][ACTIVATE] Exit gravity of " + p.getSegment().getSegmentController().getId());
									deactivate();
								}
							}

							@Override
							public void onDeactivate() {
							}
						};
						c.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(960);
					} else {
						scheduleGravity(new Vector3f(0, 0, 0), getGravity().source);
						((GameClientState) getState()).getController().popupGameTextMessage(Lng.str("You exited the \ngravity field of \n%s", p.getSegment().getSegmentController().toNiceString()), 0);
						System.err.println("[CLIENT][ACTIVATE] Exit gravity of " + p.getSegment().getSegmentController().getId());
					}
				} else {
					scheduleGravity(new Vector3f(0, 0, 0), getGravity().source);
					forcedCheckFlag = true;
					System.err.println("[SERVER][ACTIVATE] Exit gravity of " + p.getSegment().getSegmentController().getId());
				}
			} else {
				scheduleGravity(gravity, p.getSegment().getSegmentController());
				if (!isOnServer()) {
					((GameClientState) getState()).getController().popupGameTextMessage(Lng.str("You switched to the \ngravity field of \n%s", p.getSegment().getSegmentController().toNiceString()), 0);
					System.err.println("[CLIENT][ACTIVATE] Change to gravity of " + p.getSegment().getSegmentController().getId());
				} else {
					forcedCheckFlag = true;
					System.err.println("[SERVER][ACTIVATE] Change to gravity of " + p.getSegment().getSegmentController().getId());
				}
			}
		}
	}

	public boolean checkClintSpawnSanity(Int2ObjectOpenHashMap<SimpleTransformableSendableObject<?>> currentSectorEntities) {
		if (((GameClientState) getState()).getPlayer().hasSpawnWait) {
			for (Sendable s : getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
				if (s instanceof SimpleTransformableSendableObject && ((SimpleTransformableSendableObject) s).getSectorId() == getSectorId()) {
					if (s instanceof SegmentController) {
						SegmentController c = ((SegmentController) s);
						if (!c.checkClientLoadedOverlap(this)) {
							// System.err.println("[CLIENT] CHECKING TO SPAWN: "+s+": "+c.getMinPos()+" -- "+c.getMaxPos()+"; "+waitingForToSpawn.size());
							assert (waitingForToSpawn.size() > 0);
							return false;
						}
					}
				}
			}
			((GameClientState) getState()).getPlayer().hasSpawnWait = false;
		}
		return true;
	}

	public void activateMedical(SegmentPiece p) {
		CharacterBlockActivation a = new CharacterBlockActivation();
		a.activate = true;
		a.charId = getId();
		a.location = p.getAbsoluteIndexWithType4();
		a.objectId = p.getSegment().getSegmentController().getId();
		networkPlayerCharacterObject.blockActivationsWithReaction.add(new RemoteCharacterBlockActivation(a, networkPlayerCharacterObject));
	}

	@Override
	public void onPlayerDetachedFromThis(PlayerState pState, PlayerControllable newAttached) {
		if (newAttached != null && newAttached instanceof SegmentController) {
			System.err.println("[PLAYERCHARACTER] " + this + " removed warp token, because character entered structure!");
			setWarpToken(false);
		}
	}
}
