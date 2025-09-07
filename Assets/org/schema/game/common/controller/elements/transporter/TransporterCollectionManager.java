package org.schema.game.common.controller.elements.transporter;

import java.util.Collection;

import javax.vecmath.Vector3f;

import api.utils.sound.AudioUtils;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.GameClientController;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.client.view.gui.transporter.TransporterDestinations;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.elements.BlockMetaDataDummy;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.ShieldAddOn;
import org.schema.game.common.controller.elements.ShieldContainerInterface;
import org.schema.game.common.controller.elements.TransporterModuleInterface;
import org.schema.game.common.controller.elements.beam.ShieldConditionInterface;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.network.objects.remote.RemoteValueUpdate;
import org.schema.game.network.objects.valueUpdate.NTValueUpdateInterface;
import org.schema.game.network.objects.valueUpdate.TransporterBeaconActivated;
import org.schema.game.network.objects.valueUpdate.TransporterClientStateRequestUpdate;
import org.schema.game.network.objects.valueUpdate.TransporterDestinationUpdate;
import org.schema.game.network.objects.valueUpdate.TransporterSettingsUpdate;
import org.schema.game.network.objects.valueUpdate.TransporterUsageUpdate;
import org.schema.game.network.objects.valueUpdate.ValueUpdate.ValTypes;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.sound.controller.AudioController;

import com.bulletphysics.linearmath.Transform;

public class TransporterCollectionManager extends ControlBlockElementCollectionManager<TransporterUnit, TransporterCollectionManager, TransporterElementManager> implements PowerConsumer {

	public static final byte PRIVATE_ACCESS = 0;

	public static final byte PUBLIC_ACCESS = 1;

	public static final byte FACTION_ACCESS = 2;

	private Vector3i destinationBlock = new Vector3i();

	private String destinationUID = "none";

	private String transporterName = "no name";

	private boolean requestedDestination;

	private byte publicAccess;

	private long transporterUsageStarted;

	private long transporterReceivingUsageStarted;

	private boolean transported = true;

	private static long ACTIVE_DURATION = 5000;

	public TransporterCollectionManager(SegmentPiece element, SegmentController segController, TransporterElementManager em) {
		super(element, ElementKeyMap.TRANSPORTER_MODULE, segController, em);
		assert (element != null);
	}

	@Override
	public boolean isUsingIntegrity() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.ControlBlockElementCollectionManager#applyMetaData(org.schema.game.common.controller.elements.BlockMetaDataDummy)
	 */
	@Override
	protected void applyMetaData(BlockMetaDataDummy dummy) {
		destinationBlock = ((TransporterMetaDataDummy) dummy).destinationBlock;
		destinationUID = ((TransporterMetaDataDummy) dummy).destinationUID;
		transporterName = ((TransporterMetaDataDummy) dummy).name;
		publicAccess = ((TransporterMetaDataDummy) dummy).publicAccess;
	}

	@Override
	protected Tag toTagStructurePriv() {
		Tag nm = new Tag(Type.STRING, null, transporterName);
		Tag dUID = new Tag(Type.STRING, null, destinationUID);
		Tag dLoc = new Tag(Type.VECTOR3i, null, destinationBlock);
		Tag dAc = new Tag(Type.BYTE, null, publicAccess);
		return new Tag(Type.STRUCT, null, new Tag[] { nm, dUID, dLoc, dAc, FinishTag.INST });
	}

	@Override
	public int getMargin() {
		return 0;
	}

	@Override
	protected Class<TransporterUnit> getType() {
		return TransporterUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return true;
	}

	@Override
	public TransporterUnit getInstance() {
		return new TransporterUnit();
	}

	@Override
	protected void onChangedCollection() {
		if (!getSegmentController().isOnServer() && !getSegmentController().getState().isPassive()) {
			((GameClientState) getSegmentController().getState()).getWorldDrawer().getGuiDrawer().managerChanged(this);
			((GameClientState) getSegmentController().getState()).getWorldDrawer().getTransporterEffectManager().onColChanged(this);
		}
	}

	@Override
	protected void onRemovedCollection(long absPos, TransporterCollectionManager instance) {
		super.onRemovedCollection(absPos, instance);
		if (!getSegmentController().isOnServer() && !getSegmentController().getState().isPassive()) {
			((GameClientState) getSegmentController().getState()).getWorldDrawer().getTransporterEffectManager().onColRemoved(this);
		}
	}

	@Override
	public void update(Timer timer) {
		if (isValid()) {
			if (!getSegmentController().isOnServer() && !requestedDestination) {
				requestTransporterState();
				requestedDestination = true;
			}
			if (!transported && isTransporterActive()) {
				if ((timer.currentTime - transporterUsageStarted > ACTIVE_DURATION / 2)) {
					transport();
					transported = true;
				}
			}
		}
	}

	private SegmentPiece p = new SegmentPiece();

	private Vector3i out = new Vector3i();

	private Vector3i tPos = new Vector3i();

	private void transport() {
		int moved = 0;
		TransporterCollectionManager destination = getDestination();
		for (Sendable s : getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
			if (s instanceof AbstractCharacter<?> && !((AbstractCharacter<?>) s).isHidden() && getSegmentController().isNeighbor(getSegmentController().getSectorId(), ((AbstractCharacter<?>) s).getSectorId())) {
				AbstractCharacter<?> c = (AbstractCharacter<?>) s;
				Sector sector = ((GameServerState) getState()).getUniverse().getSector(getSegmentController().getSectorId());
				if (sector != null) {
					c.calculateRelToThis(sector, sector.pos);
					Transform t = c.getClientTransform();
					SegmentController.getBlockPositionRelativeTo(t.origin, getSegmentController(), out);
					// System.err.println("[TRANSPORTER] OUT ::: "+out);
					for (TransporterUnit u : getElementCollections()) {
						for (long l : u.getNeighboringCollection()) {
							// autorequest true previously
							SegmentPiece pp = getSegmentController().getSegmentBuffer().getPointUnsave(l, p);
							if (pp != null) {
								pp.getAbsolutePos(tPos);
								for (int i = 0; i < 2; i++) {
									tPos.add(Element.DIRECTIONSi[Element.switchLeftRight(pp.getOrientation())]);
									if (tPos.equals(out)) {
										/*AudioController.fireAudioEventRemote("TRANSPORTER", getSegmentController().getId(), new AudioTag[] { AudioTags.GAME, AudioTags.ACTIVATE, AudioTags.SHIP, AudioTags.BLOCK, AudioTags.TRANSPORTER }, AudioParam.ONE_TIME, AudioController.ent(getSegmentController(), u.getElementCollectionId(), u.getSignificator(), u))*/
										AudioController.fireAudioEventRemoteID(931, getSegmentController().getId(), AudioController.ent(getSegmentController(), u.getElementCollectionId(), u));
										moveToDestination(c, destination, moved, false);
										moved++;
									} else {
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private void shieldOutage(SegmentController c) {
		if (c != null && c instanceof ManagedSegmentController<?>) {
			if (((ManagedSegmentController<?>) c).getManagerContainer() instanceof ShieldContainerInterface) {
				ShieldAddOn shieldAddOn = ((ShieldContainerInterface) ((ManagedSegmentController<?>) c).getManagerContainer()).getShieldAddOn();
				if (shieldAddOn.isUsingLocalShields()) {
					final long lastUsed = getState().getNumberOfUpdate();
					boolean onCompleteStructure = true;
					shieldAddOn.getShieldLocalAddOn().addShieldCondition(new ShieldConditionInterface() {

						@Override
						public boolean isActive() {
							return getState().getNumberOfUpdate() - lastUsed > TransporterElementManager.SHIELD_DOWN_TIME_MS;
						}

						@Override
						public boolean isShieldUsable() {
							return false;
						}
					}, onCompleteStructure);
				} else {
					shieldAddOn.onHit(0L, (short) 0, (long) Math.ceil(shieldAddOn.getShields()), DamageDealerType.GENERAL);
				}
				if (c.railController.isDocked()) {
					shieldOutage(c.railController.previous.rail.getSegmentController());
				}
			}
		}
	}

	private void moveToDestination(AbstractCharacter<?> c, TransporterCollectionManager destination, int moved, boolean forceShieldOutage) {
		if (c.isSitting()) {
			c.sendControllingPlayersServerMessage(Lng.astr("Cannot transport while sitting!"), ServerMessage.MESSAGE_TYPE_ERROR);
			return;
		}
		int cursor = 0;
		if (destination != null && getSegmentController().isNeighbor(getSegmentController().getSectorId(), destination.getSegmentController().getSectorId())) {
			if (!getSegmentController().railController.isInAnyRailRelationWith(destination.getSegmentController()) || forceShieldOutage) {
				if (destination.isPublicAccess() || (destination.isFactionAccess() && destination.getFactionId() == getSegmentController().getFactionId())) {
					boolean noShieldDown = getConfigManager().apply(StatusEffectType.TRANSPORTER_NO_SHIELD_DOWN, false);
					if (!noShieldDown) {
						c.sendControllingPlayersServerMessage(Lng.astr("Transporting! Shields of both structures ineffective for %s secs.", StringTools.formatPointZero((double) TransporterElementManager.SHIELD_DOWN_TIME_MS / 1000d)), ServerMessage.MESSAGE_TYPE_INFO);
						shieldOutage(getSegmentController());
						shieldOutage(destination.getSegmentController());
					}
				} else {
					System.err.println("[SERVER][TRANSPORTER] DENIED ACCESS: public: " + destination.isPublicAccess() + "; faction: " + destination.isFactionAccess() + "; OWNFID " + getSegmentController().getFactionId() + "; DESTFID " + destination.getFactionId());
					c.sendControllingPlayersServerMessage(Lng.astr("Cannot transport!\nDestination denied access!"), ServerMessage.MESSAGE_TYPE_ERROR);
					return;
				}
			}
			for (TransporterUnit u : destination.getElementCollections()) {
				for (int i = 0; i < u.getNeighboringCollection().size(); i++) {
					if (cursor < moved) {
						cursor++;
						continue;
					}
					long pos = u.getNeighboringCollection().getLong(i);
					// autorequest true previously
					SegmentPiece p = destination.getSegmentController().getSegmentBuffer().getPointUnsave(pos);
					if (p != null) {
						int newSid = p.getSegmentController().getSectorId();
						c.onPhysicsRemove();
						c.setSectorId(newSid);
						if (c.getAttachedPlayers().get(0) instanceof PlayerState) {
							((PlayerState) c.getAttachedPlayers().get(0)).setCurrentSectorId(newSid);
							((PlayerState) c.getAttachedPlayers().get(0)).setCurrentSector(((GameServerState) getState()).getUniverse().getSector(newSid).pos);
						}
						c.onPhysicsAdd();
						if (p.getSegmentController() != getSegmentController()) {
							// Only change gravity if it's a different segment controller
							c.scheduleGravityServerForced(new Vector3f(), p.getSegmentController());
						}
						Vector3f to = new Vector3f();
						p.getAbsolutePos(to);
						to.x -= SegmentData.SEG_HALF;
						to.y -= SegmentData.SEG_HALF;
						to.z -= SegmentData.SEG_HALF;
						to.y += 1.38f;
						p.getSegmentController().getWorldTransform().transform(to);
						c.warpTransformable(to.x, to.y, to.z, true, null);
						System.err.println("WARP POSITION: " + to);
					} else {
						System.err.println("[TRANSPORTER][ERROR] Endpoint not loaded: " + pos);
					}
					return;
				}
			}
			c.sendControllingPlayersServerMessage(Lng.astr("Cannot transport!\nNot enough endpoints!"), ServerMessage.MESSAGE_TYPE_ERROR);
		} else {
			c.sendControllingPlayersServerMessage(Lng.astr("Cannot transport!\nDestination not set or out of reach!"), ServerMessage.MESSAGE_TYPE_ERROR);
		}
	}

	@Override
	public void clear() {
		requestedDestination = false;
		super.clear();
	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		return new GUIKeyValueEntry[] {};
	}

	@Override
	public String getModuleName() {
		return Lng.str("Transporter System");
	}

	public boolean isValid() {
		return getElementCollections().size() > 0;
	}

	public void setDestinationUID(String uid) {
		destinationUID = uid;
	}

	public void setDestination(String uid, long pos) {
		destinationUID = uid;
		ElementCollection.getPosFromIndex(pos, destinationBlock);
	}

	public void setTransporterSettings(String name, byte publicAccess) {
		this.transporterName = name;
		this.publicAccess = publicAccess;
	}

	public void sendSettingsUpdate() {
		TransporterSettingsUpdate v = new TransporterSettingsUpdate();
		assert (v.getType() == ValTypes.TRANSPORTER_SETTINGS_UPDATE);
		v.name = transporterName;
		v.publicAccess = publicAccess;
		v.setServer(((ManagedSegmentController<?>) getSegmentController()).getManagerContainer(), getControllerElement().getAbsoluteIndex());
		((NTValueUpdateInterface) getSegmentController().getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(v, getSegmentController().isOnServer()));
	}

	public void sendDestinationUpdate() {
		TransporterDestinationUpdate v = new TransporterDestinationUpdate();
		assert (v.getType() == ValTypes.TRANSPORTER_DESTINATION_UPDATE);
		v.uid = destinationUID;
		v.pos = ElementCollection.getIndex(destinationBlock);
		v.setServer(((ManagedSegmentController<?>) getSegmentController()).getManagerContainer(), getControllerElement().getAbsoluteIndex());
		((NTValueUpdateInterface) getSegmentController().getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(v, getSegmentController().isOnServer()));
	}

	private void requestTransporterState() {
		assert (!getSegmentController().isOnServer());
		TransporterClientStateRequestUpdate v = new TransporterClientStateRequestUpdate();
		assert (v.getType() == ValTypes.TRANSPORTER_CLIENT_STATE_REQUEST);
		v.setServer(((ManagedSegmentController<?>) getSegmentController()).getManagerContainer(), getControllerElement().getAbsoluteIndex());
		((NTValueUpdateInterface) getSegmentController().getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(v, getSegmentController().isOnServer()));
	}

	public void sendStateUpdateToClients() {
		sendSettingsUpdate();
		sendDestinationUpdate();
	}

	public byte getPublicAccess() {
		return publicAccess;
	}

	public String getTransporterName() {
		return transporterName;
	}

	public String getDestinationUID() {
		return destinationUID;
	}

	public Vector3i getDestinationBlock() {
		return destinationBlock;
	}

	public TransporterCollectionManager getDestination() {
		Sendable sendable = getState().getLocalAndRemoteObjectContainer().getUidObjectMap().get(destinationUID);
		if (sendable != null && sendable instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) sendable).getManagerContainer() instanceof TransporterModuleInterface) {
			TransporterModuleInterface t = (TransporterModuleInterface) ((ManagedSegmentController<?>) sendable).getManagerContainer();
			TransporterCollectionManager transporterCollectionManager = t.getTransporter().getCollectionManagersMap().get(ElementCollection.getIndex(destinationBlock));
			if (transporterCollectionManager != null) {
				return transporterCollectionManager;
			}
		}
		return null;
	}

	public void transporterUsageReceived(long usageTimeOnServer) {
		if (getSegmentController().isOnServer()) {
			// handle actual warping
			this.transporterUsageStarted = System.currentTimeMillis();
			transported = false;
		} else {
			// initialize effect
			this.transporterUsageStarted = usageTimeOnServer - ((GameClientState) getState()).getServerTimeDifference();
			/*AudioController.fireAudioEvent("TRANSPORTER_ACTIVE", new AudioTag[] { AudioTags.GAME, AudioTags.ACTIVATE, AudioTags.SHIP, AudioTags.BLOCK, AudioTags.TRANSPORTER }, AudioParam.ONE_TIME, AudioController.ent(getSegmentController(), getControllerElement(), getUsableId(), 10))*/
			AudioController.fireAudioEventID(932, AudioController.ent(getSegmentController(), getControllerElement(), getUsableId(), 10));
		}
		if (getDestination() != null) {
			getDestination().transporterReceivingUsageStarted = this.transporterUsageStarted;
		}
	}

	public void sendTransporterUsage() {
		TransporterUsageUpdate v = new TransporterUsageUpdate();
		if (getSegmentController().isOnServer()) {
			v.usageTime = this.transporterUsageStarted;
		} else {
			v.usageTime = 0;
		}
		assert (v.getType() == ValTypes.TRANSPORTER_USAGE_UPDATE);
		v.setServer(((ManagedSegmentController<?>) getSegmentController()).getManagerContainer(), getControllerElement().getAbsoluteIndex());
		((NTValueUpdateInterface) getSegmentController().getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(v, getSegmentController().isOnServer()));
	}

	public boolean canUse() {
		return getElementCollections().size() > 0 && !isTransporterActive();
	}

	public Collection<TransporterDestinations> getActiveTransporterDestinations(SegmentController from) {
		assert (!getSegmentController().isOnServer());
		return ((GameClientController) getState().getController()).getActiveTransporterDestinations(from);
	}

	public boolean isTransporterReceivingActive() {
		return getState().getUpdateTime() - transporterReceivingUsageStarted <= ACTIVE_DURATION;
	}

	public boolean isTransporterActive() {
		return getState().getUpdateTime() - transporterUsageStarted <= ACTIVE_DURATION;
	}

	public void sendBeaconActivated(int entityId) {
		assert (!getSegmentController().isOnServer());
		TransporterBeaconActivated v = new TransporterBeaconActivated();
		assert (v.getType() == ValTypes.TRANSPORTER_BEACON_ACTIVATED);
		v.entityId = entityId;
		v.setServer(((ManagedSegmentController<?>) getSegmentController()).getManagerContainer(), getControllerElement().getAbsoluteIndex());
		((NTValueUpdateInterface) getSegmentController().getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(v, getSegmentController().isOnServer()));
		transporterReceivingUsageStarted = System.currentTimeMillis();
	}

	public void transporterBeaconActivatedReceived(int entityId) {
		assert (getSegmentController().isOnServer());
		transporterReceivingUsageStarted = getState().getUpdateTime();
		Sendable sendable = getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().get(entityId);
		if (sendable instanceof AbstractCharacter<?>) {
			moveToDestination((AbstractCharacter<?>) sendable, this, 0, true);
		}
	}

	private final long intensityDuration = 150;

	private float powered;

	public float getEffectIntesity() {
		long running = getState().getUpdateTime() - transporterUsageStarted;
		long dist = Math.abs(running - ACTIVE_DURATION / 2);
		if (dist < intensityDuration) {
			float p = (float) dist / (float) intensityDuration;
			return 1f - p;
		}
		return 0;
	}

	public boolean isFactionAccess() {
		return publicAccess == FACTION_ACCESS;
	}

	public boolean isPublicAccess() {
		return publicAccess == PUBLIC_ACCESS;
	}

	public double getPowerCons() {
		return TransporterElementManager.POWER_CONST_NEEDED_PER_BLOCK * getTotalSize();
	}

	@Override
	public double getPowerConsumedPerSecondResting() {
		return getConfigManager().apply(StatusEffectType.TRANSPORTER_POWER_TOPOFF_RATE, getPowerCons());
	}

	@Override
	public double getPowerConsumedPerSecondCharging() {
		return getConfigManager().apply(StatusEffectType.TRANSPORTER_POWER_CHARGE_RATE, getPowerCons());
	}

	@Override
	public boolean isPowerCharging(long curTime) {
		return false;
	}

	@Override
	public void setPowered(float powered) {
		this.powered = powered;
	}

	@Override
	public float getPowered() {
		return powered;
	}

	@Override
	public void reloadFromReactor(double secTime, Timer timer, float tickTime, boolean powerCharging, float poweredResting) {
	}

	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.OTHERS;
	}

	@Override
	public boolean isPowerConsumerActive() {
		return true;
	}

	@Override
	public void dischargeFully() {
	}
}
