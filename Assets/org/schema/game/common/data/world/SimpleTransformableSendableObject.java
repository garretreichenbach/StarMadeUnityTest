package org.schema.game.common.data.world;

import api.common.GameCommon;
import api.listener.events.entity.ShipJumpEngageEvent;
import api.mod.StarLoader;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.SolverConstraint;
import com.bulletphysics.linearmath.AabbUtil2;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.bytes.ByteArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.schema.common.FastMath;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.TransformTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.GameClientController;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.client.data.SectorChange;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.HitReceiverType;
import org.schema.game.common.controller.damage.effects.InterEffectContainer;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.controller.database.DatabaseInsertable;
import org.schema.game.common.controller.database.tables.FTLTable;
import org.schema.game.common.controller.elements.EffectManagerContainer;
import org.schema.game.common.controller.elements.StationaryManagerContainer;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.elements.stealth.StealthAddOn.StealthLvl;
import org.schema.game.common.controller.elements.effectblock.EffectElementManager;
import org.schema.game.common.controller.elements.structurescanner.StructureScannerElementManager;
import org.schema.game.common.controller.elements.warpgate.WarpgateCollectionManager;
import org.schema.game.common.controller.rules.rules.RuleEntityManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SimpleGameObject;
import org.schema.game.common.data.blockeffects.config.ConfigEntityManager;
import org.schema.game.common.data.blockeffects.config.ConfigManagerInterface;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.creature.AICreature;
import org.schema.game.common.data.creature.AIPlayer;
import org.schema.game.common.data.mission.spawner.SpawnController;
import org.schema.game.common.data.physics.*;
import org.schema.game.common.data.player.*;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.game.common.data.world.SectorInformation.SectorType;
import org.schema.game.network.objects.NTRuleInterface;
import org.schema.game.server.controller.GameServerController;
import org.schema.game.server.controller.SectorSwitch;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.blueprintnw.BlueprintClassification;
import org.schema.schine.ai.stateMachines.AiInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.language.Translatable;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.BoundingSphere;
import org.schema.schine.graphicsengine.forms.Light;
import org.schema.schine.network.Identifiable;
import org.schema.schine.network.NetworkGravity;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.TopLevelType;
import org.schema.schine.network.client.ClientState;
import org.schema.schine.network.objects.LocalSectorTransition;
import org.schema.schine.network.objects.NetworkEntity;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.objects.container.PhysicsDataContainer;
import org.schema.schine.network.objects.container.TransformTimed;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.network.server.ServerStateInterface;
import org.schema.schine.physics.Physical;
import org.schema.schine.physics.PhysicsState;
import org.schema.schine.resource.DiskWritable;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;

import javax.vecmath.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.Set;

public abstract class SimpleTransformableSendableObject<E extends NetworkListenerEntity> implements GameTransformable, StealthReconEntity, Sendable, DiskWritable, Damager, SimpleGameObject, DatabaseInsertable, BoundingSphereObject {
	public static final int DEBUG_NT_SMOOTHER = 1;
	private static final Vector3f noGrav = new Vector3f(0, 0, 0);
	private static final Transform serverTmp = new Transform();
	private static final Transform clientTmp = new Transform();
	private static ThreadLocal<TransformaleObjectTmpVars> threadLocal = new ThreadLocal<TransformaleObjectTmpVars>() {
		@Override
		protected TransformaleObjectTmpVars initialValue() {
			return new TransformaleObjectTmpVars();
		}
	};
	public final VirtualEntityAttachment vServerAttachment;
	public final ObjectArrayFIFOQueue<AICreature<? extends AIPlayer>> attachedAffinityInitial = new ObjectArrayFIFOQueue<AICreature<? extends AIPlayer>>();
	public final TransformaleObjectTmpVars v;
	private final Transform worldTransformInverse = new Transform();
	private final InterEffectContainer effectContainer;
	private final BoundingSphere boundingSphere = new BoundingSphere();
	private final BoundingSphere boundingSphereTotal = new BoundingSphere();
	private final StateInterface state;
	private final boolean onServer;
	private final Vector3i tagSectorId = new Vector3i(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
	private final SpawnController spawnController;
	private final Vector3f tmp = new Vector3f();
	//	private Transform transTmp = new Transform();
	private final GravityState gravity = new GravityState();
	private final ObjectOpenHashSet<AICreature<? extends AIPlayer>> attachedAffinity = new ObjectOpenHashSet<AICreature<? extends AIPlayer>>();
	private final ByteArrayFIFOQueue graphicsEffectRecBuffer = new ByteArrayFIFOQueue();
	private final Vector3f minTmp = new Vector3f();
	private final Vector3f maxTmp = new Vector3f();
	private final Vector3f minTarTmp = new Vector3f();
	private final Vector3f maxTarTmp = new Vector3f();
	public long sectorChangedTimeOwnClient;
	public Vector3i transientSectorPos = new Vector3i(); //used to use toTagStructure on a custom server
	public boolean transientSector;
	public long lastSectorSwitch;
	public CollisionObject clientVirtualObject;
	public long heatDamageStart;
	public Vector3i heatDamageId;
	public boolean needsPositionCheckOnLoad;
	protected boolean flagGravityUpdate;
	protected NetworkGravity receivedGravity;
	protected Boolean hiddenUpdate;
	protected boolean forcedCheckFlag;
	protected GravityState scheduledGravity;
	protected Vector3f personalGravity = new Vector3f();
	protected boolean personalGravitySwitch = false;
	boolean hiddenflag = false;
	private boolean warpToken;
	private int factionId = FactionManager.ID_NEUTRAL;
	private int id;
	private RemoteTransformable remoteTransformable;
	private int sectorId = Sector.SECTOR_INITIAL;
	private float prohibitingBuildingAroundOrigin;
	private boolean hidden = false;
	private String owner = new String();
	private boolean markedForDelete;
	private boolean flagPhysicsInit;
	private boolean markedForDeleteSent;
	private boolean immediateStuck;
	private TransformTimedSet clientTransform = new TransformTimedSet();
	private Vector3f d = new Vector3f();
	private boolean markedForPermanentDelete;
	private long lastSlowdown;
	private long slowdownStart;
	private boolean flagGravityDeligation;
	private boolean writtenForUnload;
	private long lastSearch;
	private long lastWrite;
	private boolean changed = true; //is reset in fromTagStructure
	private byte invisibleNextDraw = 2;
	private Light light;
	private long lastLagSent;
	private long currentLag;
	private long lastLagReceived;
	private long sectorChangedTime;
	private boolean clientCleanedUp;
	private boolean first = true;
	private boolean inClientRange;
	private boolean adminInvisibility;
	private boolean tracked;
	private long lastGravityUpdate;

	public SimpleTransformableSendableObject(StateInterface state) {
		this.state = state;
		this.v = threadLocal.get();
		spawnController = new SpawnController(this);
		onServer = state instanceof ServerStateInterface;
		vServerAttachment = new VirtualEntityAttachment(this);
		effectContainer = setupEffectContainer();
	}

	protected InterEffectContainer setupEffectContainer() {
		return new GenericEffectSet();
	}

	public static void calcWaypointSecPos(Vector3i fromAbsSec, Vector3i absSec, Transform out, GameServerState state, Vector3i tmpSysPos) {
		Vector3i pPos = new Vector3i(absSec);
		pPos.sub(fromAbsSec);
		out.setIdentity();
		float year = state.getGameState().getRotationProgession();
		Vector3f otherSecCenter = new Vector3f(pPos.x * state.getSectorSize(), pPos.y * state.getSectorSize(), pPos.z * state.getSectorSize());
		Matrix3f rot = new Matrix3f();
		rot.rotX((FastMath.PI * 2) * year);
		Sector sec = state.getUniverse().getSectorWithoutLoading(absSec);
		if(sec != null) {
			try {
				if(sec.getSectorType() == SectorType.PLANET) {
					//we are next to a planet sector
					//-> rotate planet sector
					rot.invert();
					Vector3f bb = new Vector3f();
					bb.add(otherSecCenter);
					TransformTools.rotateAroundPoint(bb, rot, out, new Transform());
					out.origin.add(otherSecCenter);
					return;
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		out.origin.set(otherSecCenter);
	}

	public static Vector3i getBlockPositionRelativeTo(Vector3f pos, SimpleTransformableSendableObject obj, Vector3i out) {
		if(obj == null) {
			int x = FastMath.round(pos.x + SegmentData.SEG_HALF);
			int y = FastMath.round(pos.y + SegmentData.SEG_HALF);
			int z = FastMath.round(pos.z + SegmentData.SEG_HALF);
			out.set(x, y, z);
		} else {
			Transform other = new Transform(obj.getWorldTransform());
			other.inverse();
			Transform self = new Transform();
			self.origin.set(pos);
			other.mul(self);
			int x = FastMath.round(other.origin.x + SegmentData.SEG_HALF);
			int y = FastMath.round(other.origin.y + SegmentData.SEG_HALF);
			int z = FastMath.round(other.origin.z + SegmentData.SEG_HALF);
			out.set(x, y, z);
		}
		return out;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.Transformable#getWorldTransform()
	 */
	@Override
	public TransformTimed getWorldTransform() {
		return remoteTransformable.getWorldTransform();
	}

	public static void calcWorldTransformRelative(StateInterface state, Vector3i fromSectorPos, Vector3i toSectorPos, boolean fromPlanet, boolean toPlanet, Transform fromT, Transform toOut, TransformaleObjectTmpVars v) {
		//sector pos for client calc is current player sector of client (as well as fromSectorId)
		if(fromSectorPos.equals(toSectorPos)) {
			toOut.set(fromT);
		} else {
			float year = ((GameStateInterface) state).getGameState().getRotationProgession();
			//one of the sectors is a planet
			Vector3i sysPos = StellarSystem.getPosFromSector(fromSectorPos, v.systemPos);
			v.dir.sub(toSectorPos, fromSectorPos);
			v.otherSecCenter.set(v.dir.x * ((GameStateInterface) state).getSectorSize(), v.dir.y * ((GameStateInterface) state).getSectorSize(), v.dir.z * ((GameStateInterface) state).getSectorSize());
			v.t.set(fromT);
			v.rot.rotX((FastMath.PI * 2) * year);
			if(toPlanet) {
				//we are next to a planet sector
				//-> rotate planet sector
				//
				v.bb.set(v.otherSecCenter);
				TransformTools.rotateAroundPoint(v.bb, v.rot, v.t, v.transTmp);
				v.t.origin.add(v.otherSecCenter);
			} else if(fromPlanet) {
				//we are in a planet sector
				//-> rotate everything around us
				v.rot.invert();
				v.bb.set(v.t.origin);
				v.bb.add(v.otherSecCenter);
				TransformTools.rotateAroundPoint(v.bb, v.rot, v.t, v.transTmp);
				v.t.origin.add(v.otherSecCenter);
			} else {
				v.t.origin.add(v.otherSecCenter);
			}
			toOut.set(v.t);
		}
	}

	public Faction getFaction() {
		return ((FactionState) state).getFactionManager().getFaction(getFactionId());
	}

	@Override
	public int getFactionId() {
		return factionId;
	}

	/**
	 * @param factionId the factionId to set
	 */
	public void setFactionId(int factionId) {
		final int oldFaction = this.factionId;
		Faction f;
		if(onServer && factionId != this.factionId && (f = ((FactionState) state).getFactionManager().getFaction(this.factionId)) != null) {
			if(f.getHomebaseUID().equals(getUniqueIdentifier())) {
				((FactionState) state).getFactionManager().serverRevokeFactionHome(this.factionId);
			}
			Sector sector = ((GameServerState) state).getUniverse().getSector(sectorId);
			if(sector != null) {
				StellarSystem stellarSystemFromSecPos;
				try {
					stellarSystemFromSecPos = ((GameServerState) state).getUniverse().getStellarSystemFromSecPos(sector.pos);
					if(stellarSystemFromSecPos.getOwnerFaction() == this.factionId && stellarSystemFromSecPos.getOwnerUID() != null && stellarSystemFromSecPos.getOwnerUID().equals(getUniqueIdentifier())) {
						((FactionState) state).getFactionManager().sendServerSystemFactionOwnerChange("SYSTEM", 0, "", sector.pos, stellarSystemFromSecPos.getPos(), stellarSystemFromSecPos.getName());
					}
				} catch(IOException e) {
					System.err.println("[SERVER][SimpleTranformableObject] Fatal Exception: system to revoke system ownership could not be retrieved: " + this + ": sid: " + sectorId);
					e.printStackTrace();
				}
			} else {
				System.err.println("[SERVER][SimpleTranformableObject] Fatal Exception: sector to revoke system ownership could not be retrieved: " + this + ": sid: " + sectorId);
			}
		}
		this.factionId = factionId;
		if(onServer && oldFaction != this.factionId && getRuleEntityManager() != null) {
			getRuleEntityManager().triggerOnFactionChange();
		}
	}

	@Override
	public float getDamageGivenMultiplier() {
		return 1;
	}

	public Vector3i getClientSector() {
		assert (!onServer);
		Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(sectorId);
		if(sendable != null && sendable instanceof RemoteSector) {
			return ((RemoteSector) sendable).clientPos();
		}
		return null;
	}

	/**
	 * @return the worldTransformInverse
	 */
	public Transform getWorldTransformInverse() {
		return worldTransformInverse;
	}

	public void destroy() {
		System.out.println("[SIMPLETRANSFORMABLE] ENTITY " + this + " HAS BEEN DESTROYED... ");
		this.markForPermanentDelete(true);
		this.setMarkedForDeleteVolatile(true);
	}

	public boolean isPhysicalForDamage() {
		return true;
	}

	public ByteBuffer getDataByteBuffer() {
		return state.getDataByteBuffer();
	}

	public long getUpdateTime() {
		return state.getUpdateTime();
	}

	public void releaseDataByteBuffer(ByteBuffer buffer) {
		state.releaseDataByteBuffer(buffer);
	}

	public abstract void addListener(E s);

	public abstract List<E> getListeners();

	public float getSpeedPercentServerLimitCurrent() {
		return (getSpeedCurrent() / ((GameStateInterface) state).getGameState().getMaxGalaxySpeed());
	}

	public float getSpeedCurrent() {
		//overwritten by segment controller
		CollisionObject object = getPhysicsDataContainer().getObject();
		if(object != null && object instanceof RigidBody) {
			return ((RigidBody) object).getLinearVelocity(new Vector3f()).length();
		} else {
			return 0;
		}
	}

	public String getTypeString() {
		return getTypeString(this);
	}

	public static String getTypeString(SimpleTransformableSendableObject f) {
		return f.getType().getName();
	}

	public abstract EntityType getType();

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.SimpleGameObject#existsInState()
	 */
	@Override
	public boolean existsInState() {
		return state.getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().containsKey(getId());
	}

	@Override
	public void calcWorldTransformRelative(int fromSectorId, Vector3i sectorPos) {
		calcWorldTransformRelative(fromSectorId, sectorPos, this.sectorId);
	}

	/**
	 * @return the clientTransform
	 */
	@Override
	public Transform getClientTransform() {
		return clientTransform;
	}

	@Override
	public Transform getClientTransformCenterOfMass(Transform out) {
		tmp.set(getPhysicsDataContainer().lastCenter);
		out.set(clientTransform);
		out.basis.transform(tmp);
		out.origin.add(tmp);
		return out;
	}

	@Override
	public Vector3f getCenterOfMass(Vector3f out) {
		out.set(getPhysicsDataContainer().lastCenter);
		return out;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.SimpleGameObject#getLinearVelocity()
	 */
	@Override
	public Vector3f getLinearVelocity(Vector3f out) {
		//overwritten when needed
		return out;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.SimpleGameObject#isInPhysics()
	 */
	@Override
	public boolean isInPhysics() {
		return getPhysicsDataContainer().getObject() != null;
	}

	@Override
	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		if(onServer) {
			//			assert(hidden || !(this.toString().contains("CSS-Valhalla-Reactor") && this.toString().contains("_dock_"))):this;
			this.hidden = hidden;
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.SimpleGameObject#getAsTargetId()
	 */
	@Override
	public int getAsTargetId() {
		return getId();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.SimpleGameObject#getTargetType()
	 */
	@Override
	public byte getTargetType() {
		return SimpleGameObject.SIMPLE_TRANSFORMABLE_SENSABLE_OBJECT;
	}

	@Override
	public void transformAimingAt(Vector3f to, Damager from, SimpleGameObject target, Random random, float deviation) {
		if(this instanceof ManagedSegmentController<?>) {
			((ManagedSegmentController<?>) this).getManagerContainer().transformAimingAt(to, from, target, random, deviation);
		} else {
			to.x = (float) ((Math.random() - 0.5f) * deviation);
			to.y = (float) ((Math.random() - 0.5f) * deviation);
			to.z = (float) ((Math.random() - 0.5f) * deviation);
			if(onServer) {
				to.add(target.getClientTransformCenterOfMass(serverTmp).origin);
			} else {
				to.add(target.getClientTransformCenterOfMass(clientTmp).origin);
			}
		}
	}

	@Override
	public long getOwnerId() {
		AbstractOwnerState o = getOwnerState();
		return o == null ? Long.MIN_VALUE : o.getDbId();
	}

	public Transform getWorldTransformCenterOfMass(Transform out) {
		tmp.set(getPhysicsDataContainer().lastCenter);
		out.set(getWorldTransform());
		out.basis.transform(tmp);
		out.origin.add(tmp);
		return out;
	}

	public Transform getWorldTransformOnClientCenterOfMass(Transform out) {
		tmp.set(getPhysicsDataContainer().lastCenter);
		out.set(getWorldTransformOnClient());
		out.basis.transform(tmp);
		out.origin.add(tmp);
		return out;
	}

	public void getAimingAtRelativePos(Vector3f out, Damager from, SimpleGameObject target, Random random, float deviation) {
		if(this instanceof ManagedSegmentController<?>) {
			((ManagedSegmentController<?>) this).getManagerContainer().getAimingAtRelative(out, from, target, random, deviation);
		} else {
			//dont apply missshooting when targeting missiles
			out.x = (random.nextFloat() - 0.5f) * deviation;
			out.y = (random.nextFloat() - 0.5f) * deviation;
			out.z = (random.nextFloat() - 0.5f) * deviation;
		}
	}

	public boolean engageJump(int distance) {
		assert (onServer);
		GameServerState state = (GameServerState) this.state;
		Transform out = new Transform();
		out.setIdentity();
		float year = state.getGameState().getRotationProgession();
		Matrix3f rot = new Matrix3f();
		rot.rotX((FastMath.PI * 2) * year);
		Vector3f otherSecCenter = new Vector3f();
		GlUtil.getForwardVector(otherSecCenter, getWorldTransform());
		otherSecCenter.scale(state.getSectorSize() * distance);
		Sector sec = state.getUniverse().getSector(sectorId);
		int jumpTypeMsg = 0;
		if(sec != null) {
			try {
				if(sec.getSectorType() == SectorType.PLANET) {
					rot.invert();
					Vector3f bb = new Vector3f();
					bb.add(otherSecCenter);
					TransformTools.rotateAroundPoint(bb, rot, out, new Transform());
					out.origin.add(otherSecCenter);
				} else {
					out.origin.set(otherSecCenter);
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		System.err.println("[JUMPDRIVE] direct forward: " + out.origin);
		if(this instanceof PlayerControllable && !((PlayerControllable) this).getAttachedPlayers().isEmpty()) {
			for(PlayerState p : ((PlayerControllable) this).getAttachedPlayers()) {
				for(ControllerStateUnit u : p.getControllerState().getUnits()) {
					if(u.parameter != null && u.parameter.equals(Ship.core) && !p.getNetworkObject().waypoint.getVector().equals(PlayerState.NO_WAYPOINT)) {
						if(sec != null) {
							Vector3i vector = p.getNetworkObject().waypoint.getVector();
							vector.sub(sec.pos);
							otherSecCenter = new Vector3f(vector.x * state.getSectorSize(), vector.y * state.getSectorSize(), vector.z * state.getSectorSize());
							if(otherSecCenter.length() > state.getSectorSize() * distance) {
								otherSecCenter.normalize();
								otherSecCenter.scale(state.getSectorSize() * distance);
								jumpTypeMsg = 1;
							} else {
								jumpTypeMsg = 2;
							}
							out.origin.set(otherSecCenter);
							System.err.println("[JUMPDRIVE] setting dir from waypoint: " + p.getNetworkObject().waypoint.getVector() + " -> " + out.origin);
						}
					}
				}
			}
		}
		//		state.getUniverse().getSector(newSecPos);
		if(sec != null) {
			Vector3i newSecPos = new Vector3i(sec.pos);
			System.err.println("[JUMPDRIVE] scaling: " + out.origin + " -> " + 1f / state.getSectorSize());
			out.origin.scale(1f / state.getSectorSize());
			newSecPos.add((int) out.origin.x, (int) out.origin.y, (int) out.origin.z);
			Sector other;
			try {
				other = state.getUniverse().getSector(newSecPos);
				if(other.isNoEntry()) {
					sendControllingPlayersServerMessage(Lng.astr("Cannot enter this sector!\n(admin forced)"), ServerMessage.MESSAGE_TYPE_ERROR);
				} else if(sec.isNoExit()) {
					sendControllingPlayersServerMessage(Lng.astr("Cannot exit this sector!\n(admin forced)"), ServerMessage.MESSAGE_TYPE_ERROR);
				} else {
					//INSERTED CODE @768
					if(this instanceof SegmentController) {
						Vector3i oldSector = this.getSector(new Vector3i());
						ShipJumpEngageEvent event = new ShipJumpEngageEvent((SegmentController) this, oldSector, newSecPos);
						StarLoader.fireEvent(ShipJumpEngageEvent.class, event, this.onServer);
						if(event.isCanceled()) {
							return false;
						}
					} else {
						//how did it even jump
					}
					///
					System.err.println("[JUMPDRIVE] scaled to secSize: " + out.origin + " -> " + newSecPos);
					getNetworkObject().graphicsEffectModifier.add((byte) 1);
					SectorSwitch queueSectorSwitch = ((GameServerState) this.state).getController().queueSectorSwitch(this, newSecPos, SectorSwitch.TRANS_JUMP, false, true, true);
					if(queueSectorSwitch != null) {
						queueSectorSwitch.delay = System.currentTimeMillis() + 4000;
						queueSectorSwitch.jumpSpawnPos = new Vector3f(getWorldTransform().origin);
						queueSectorSwitch.executionGraphicsEffect = (byte) 2;
						queueSectorSwitch.keepJumpBasisWithJumpPos = true;
						if(jumpTypeMsg == 0) {
							sendControllingPlayersServerMessage(Lng.astr("Jumping in current\ndirection -> %s", newSecPos.toStringPure()), ServerMessage.MESSAGE_TYPE_INFO);
						} else if(jumpTypeMsg == 1) {
							sendControllingPlayersServerMessage(Lng.astr("Waypoint not in range.\nJumping to waypoint direction \n-> %s", newSecPos.toStringPure()), ServerMessage.MESSAGE_TYPE_INFO);
						} else {
							sendControllingPlayersServerMessage(Lng.astr("Waypoint in range.\nJumping exactly there\n-> %s", newSecPos.toStringPure()), ServerMessage.MESSAGE_TYPE_INFO);
						}
						return true;
					}
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public Vector3i getSector(Vector3i out) {
		Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(sectorId);
		if(sendable != null && sendable instanceof RemoteSector) {
			RemoteSector r = (RemoteSector) sendable;
			if(onServer) {
				out.set(r.getServerSector().pos);
				return out;
			} else {
				out.set(r.clientPos());
				return out;
			}
		}
		return null;
	}

	protected boolean checkGravityDownwards(SimpleTransformableSendableObject<?> forTarget) {
		if(sectorId == forTarget.sectorId) {
			if(getPhysicsDataContainer() != null && getPhysicsDataContainer().isInitialized()) {
				getGravityAABB(minTmp, maxTmp);
				forTarget.getGravityAABB(minTarTmp, maxTarTmp);
				if(AabbUtil2.testAabbAgainstAabb2(minTarTmp, maxTarTmp, minTmp, maxTmp)) {
					Vector3f from = new Vector3f(forTarget.getWorldTransform().origin);
					Vector3f to = new Vector3f(forTarget.getWorldTransform().origin);
					Vector3f objectUp = GlUtil.getUpVector(new Vector3f(), getWorldTransform());
					objectUp.scale(-64.0f);
					to.add(objectUp);
					CubeRayCastResult rayCallback = new CubeRayCastResult(from, to, forTarget);
					CollisionWorld.ClosestRayResultCallback testRayCollisionPoint = getPhysics().testRayCollisionPoint(from, to, rayCallback, getPhysicsDataContainer().isStatic());
					if(testRayCollisionPoint != null && testRayCollisionPoint.hasHit()) {
						if(rayCallback.getSegment() != null) {
							if(rayCallback.getSegment().getSegmentController() == this) {
								Segment segment = rayCallback.getSegment();
								boolean inside = segment.pos.y + SegmentData.SEG <= segment.getSegmentController().getMaxPos().y * SegmentData.SEG;
								boolean contains = segment.getSegmentController().getSegmentBuffer().containsKey(segment.pos.x, segment.pos.y + SegmentData.SEG, segment.pos.z);
								boolean notLoadedYet = inside && !contains;
								return !notLoadedYet;
							}
						}
					}
				}
			}
		}
		return false;
	}

	protected boolean affectsGravityOf(SimpleTransformableSendableObject<?> target) {
		if(target instanceof AbstractCharacter<?> && ((AbstractCharacter<?>) target).getOwnerState() != null && ((AbstractCharacter<?>) target).getOwnerState().isSitting()) {
			return false;
		}
		if(FactionManager.isNPCFaction(target.getFactionId())) {
			//no gravity for NPC ships
			return false;
		}
		//		if(target instanceof AbstractCharacter<?>){
		//			System.err.println("  .. "+this+"->"+target+": "+personalGravitySwitch+"; "+checkGravityDownwards(target));
		//		}
		return personalGravitySwitch && target.sectorId == sectorId && (target.getMass() > 0 || target instanceof AbstractCharacter<?>) && checkGravityDownwards(target);
	}

	public void calculateRelToThis(Sector fromSector, Vector3i fromPos) {
		this.calcWorldTransformRelative(fromPos.equals(fromSector.pos) ? fromSector.getId() : -1, fromPos, this.sectorId);
		//		this.calcWorldTransformRelative(fromSector.getId(), fromSector.pos, this.getSectorId());
	}

	public void calcWorldTransformRelative(int fromSectorId, Vector3i sectorPos, int toSectorId) {
		if(remoteTransformable == null) {
			throw new NullPointerException("No remote Transformable");
		}
		calcWorldTransformRelative(fromSectorId, sectorPos, toSectorId, remoteTransformable.getWorldTransform(), state, onServer, clientTransform, v);
	}

	public static void calcWorldTransformRelative(int fromSectorId, Vector3i sectorPos, int toSectorId, Transform localTransform, StateInterface state, boolean onServer, Transform outputTransform, TransformaleObjectTmpVars v) {
		//sector pos for client calc is current player sector of client (as well as fromSectorId)
		Vector3i toPos;
		Vector3i fromPos;
		SectorType fromType;
		SectorType toType;
		if(!onServer) {
			RemoteSector from = (RemoteSector) state.getLocalAndRemoteObjectContainer().getLocalObjects().get(fromSectorId);
			RemoteSector to = (RemoteSector) state.getLocalAndRemoteObjectContainer().getLocalObjects().get(toSectorId);
			if(fromSectorId != toSectorId && (from == null || to == null)) {
				System.err.println("[ERROR][CLIENT] " + state + ": sector yet not loaded: " + from + "; " + to + " should be: " + sectorPos + "; fromTo sectorID: " + fromSectorId + " -> " + toSectorId);
				outputTransform.set(localTransform);
				return;
			}
			fromPos = from.clientPos();
			toPos = to.clientPos();
			fromType = from.getType();
			toType = to.getType();
		} else {
			Sector from = ((GameServerState) state).getUniverse().getSector(fromSectorId);
			Sector to = ((GameServerState) state).getUniverse().getSector(toSectorId);
			if(fromSectorId != toSectorId && (from == null || to == null)) {
				System.err.println("[ERROR][SERVER] " + state + ": sector yet not loaded: " + from + "; " + to + " should be: " + sectorPos + "; fromTo sectorID: " + fromSectorId + " -> " + toSectorId);
				outputTransform.set(localTransform);
				return;
			}
			fromPos = from.pos;
			toPos = to.pos;
			try {
				fromType = from.getSectorType();
				toType = to.getSectorType();
			} catch(IOException e) {
				e.printStackTrace();
				return;
			}
		}
		if(fromSectorId == toSectorId) {
			outputTransform.set(localTransform);
		} else {
			float year = ((GameStateInterface) state).getGameState().getRotationProgession();
			//one of the sectors is a planet
			Vector3i sysPos = StellarSystem.getPosFromSector(sectorPos, v.systemPos);
			v.dir.sub(toPos, sectorPos);
			v.otherSecCenter.set(v.dir.x * ((GameStateInterface) state).getSectorSize(), v.dir.y * ((GameStateInterface) state).getSectorSize(), v.dir.z * ((GameStateInterface) state).getSectorSize());
			v.t.set(localTransform);
			if(fromType != SectorType.PLANET && toType == SectorType.PLANET) {
				v.rot.rotX((FastMath.PI * 2) * year);
				//we are next to a planet sector
				//-> rotate planet sector
				v.transTmp.origin.set(v.otherSecCenter);
				v.transTmp.basis.set(v.rot);
				v.transTmp.mul(v.t);
				v.t.set(v.transTmp);
			} else if(fromType == SectorType.PLANET && toType != SectorType.PLANET) {
				v.rot.rotX((FastMath.PI * 2) * year);
				//we are in a planet sector
				//-> rotate everything around us
				v.rot.invert();
				v.bb.set(v.t.origin);
				v.bb.add(v.otherSecCenter);
				TransformTools.rotateAroundPoint(v.bb, v.rot, v.t, v.transTmp);
				v.t.origin.add(v.otherSecCenter);
			} else {
				v.t.origin.add(v.otherSecCenter);
			}
			outputTransform.set(v.t);
		}
	}

	public boolean isSpectator() {
		Faction f = ((FactionState) state).getFactionManager().getFaction(getFactionId());
		if(f != null && f.isFactionMode(Faction.MODE_SPECTATORS)) {
			return true;
		}
		if(this instanceof PlayerControllable) {
			if(((PlayerControllable) this).hasSpectatorPlayers()) {
				return true;
			}
		}
		return false;
	}

	public boolean isInServerClientRange() {
		return onServer || isInClientRange();
	}

	public boolean isInClientRange() {
		boolean a = ((GameClientState) state).getCurrentSectorEntities().containsKey(getId());
		boolean b = inClientRange;
		return a;
	}

	public void setInClientRange(boolean inClientRange) {
		this.inClientRange = inClientRange;
	}

	public int getMiningBonus(SimpleTransformableSendableObject<?> forTarget) {
		if(this instanceof PlayerControllable && ((PlayerControllable) this).getAttachedPlayers().size() > 0) {
			return ((GameServerState) state).getUniverse().getSystemOwnerShipType(forTarget.sectorId, ((PlayerControllable) this).getAttachedPlayers().get(0).getFactionId()).getMiningBonusMult() * ServerConfig.MINING_BONUS.getInt();
		} else {
			return ((GameServerState) state).getUniverse().getSystemOwnerShipType(forTarget.sectorId, getFactionId()).getMiningBonusMult() * ServerConfig.MINING_BONUS.getInt();
		}
	}

	public boolean hasClientVirtual() {
		return clientVirtualObject != null;
	}

	protected void checkForGravity() {
		gravity.setChanged(false);
		if(gravityChecksRequired()) {
			if(scheduledGravity != null) {
				if(scheduledGravity.withBlockBelow) {
					Vector3f camPos = new Vector3f(getWorldTransform().origin);
					Vector3f camTo = new Vector3f(getWorldTransform().origin);
					Vector3f forw = GlUtil.getUpVector(new Vector3f(), getWorldTransform());
					forw.negate();
					forw.normalize();
					forw.scale(10);
					camTo.add(forw);
					//		SubsimplexRayCubesCovexCast.debug = true;
					CubeRayCastResult rayCallback = new CubeRayCastResult(camPos, camTo, false);
					rayCallback.setIgnoereNotPhysical(true);
					rayCallback.setOnlyCubeMeshes(true);
					CollisionWorld.ClosestRayResultCallback testRayCollisionPoint = (getPhysics()).testRayCollisionPoint(camPos, camTo, rayCallback, false);
					if(!testRayCollisionPoint.hasHit()) {
						forcedCheckFlag = true;
						return;
					}
				}
				System.err.println(state + " " + this + " HANDLE SCHEDULED GRAVITY " + scheduledGravity.accelToString() + ", " + scheduledGravity.source);
				setGravity(scheduledGravity.copyAccelerationTo(new Vector3f()), scheduledGravity.source, scheduledGravity.central, scheduledGravity.forcedFromServer);
				scheduledGravity = null;
			} else {
				if(state.getUpdateTime() - lastSearch > 5000) {
					searchForGravity();
					lastSearch = state.getUpdateTime();
				}
			}
			checkGravityValid();
		}
		if(gravity.isChanged()) {
			System.err.println("[GRAVITY] " + this + " changed gravity on " + state + " " + gravity.source + " -> " + gravity.accelToString());
		}
		if(flagGravityUpdate) {
			System.err.println("[GRAVITY] " + this + " FLAG changed gravity on " + state + " " + gravity.source + " -> " + gravity.accelToString());
			gravity.setChanged(true);
			flagGravityUpdate = false;
		}
	}

	protected void checkGravityValid() {
		if(hidden) {
			return;
		}
		if(!gravity.isValid(this)) {
			assert (!(gravity.isAligedOnly() || gravity.isGravityOn()) || gravity.source != null);
			removeGravity();
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.objects.Sendable#cleanUpOnEntityDelete()
	 */
	@Override
	public void cleanUpOnEntityDelete() {
		//		if(Controller.entitySoundMap.contains(this)){
		//			ArrayList<XAudio> sounds = Controller.entitySoundMap.get(this);
		//			synchronized (sounds) {
		//				for(int i = 0; i < sounds.size(); i++){
		//					sounds.get(i).stop();
		//				}
		//			}
		//		}
		//		System.err.println("[ENTITY] "+getState()+" "+this+"; cleanup: remove from physics now!");
		if(onServer) {
			vServerAttachment.clear();
		}
		onPhysicsRemove();
		attachedAffinity.clear();
	}

	@Override
	public abstract NetworkEntity getNetworkObject();

	@Override
	public void initFromNetworkObject(NetworkObject o) {
		NetworkEntity p = (NetworkEntity) o;
		setId(p.id.get());
		setFactionId(getNetworkObject().factionCode.get());
		if(!onServer) {
			this.setTracked(p.tracked.get());
			if(sectorId != p.sector.get()) {
				clientSectorChange(sectorId, p.sector.get());
				setSectorId(p.sector.get()); //this has to be done initially so it gets added to physics
			}
			//we can directly set hidden in the update as the object
			//hasnt spawned yet on client
			hidden = p.hidden.get();
		} else {
			//			System.err.println("SERVER RECEIVED SECTOR: "+p.sector.get());
			setSectorId(p.sector.get());
		}
		if(onServer && sectorId == Sector.SECTOR_INITIAL && p.sector.get() == Sector.SECTOR_INITIAL) {
			Sector defaultSector;
			//syncronized under state
			try {
				defaultSector = getDefaultSector();
			} catch(IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			System.err.println("[SERVER] NO SECTOR INFORMATION PROVIDED ON INIT: ASSIGNING SECTOR " + defaultSector.getId() + " TO " + this);
			setSectorId(defaultSector.getId());
		}
		//		System.err.println("INIT FROM NT: "+getState()+" "+this+"; Sector L/R "+getSectorId()+" / "+p.sector.get()+" ( "+getState()+" )");
		remoteTransformable.updateFromRemoteInitialTransform(p);
	}

	@Override
	public void initialize() {
		if(this.remoteTransformable == null) {
			this.remoteTransformable = new RemoteTransformable(this, state) {
				@Override
				public void createConstraint(Physical a, Physical b, Object userData) {
				}

				@Override
				public StateInterface getState() {
					return SimpleTransformableSendableObject.this.getState();
				}

				@Override
				public void getTransformedAABB(Vector3f oMin, Vector3f oMax, float margin, Vector3f tmpMin, Vector3f tmpMax, Transform instead) {
					SimpleTransformableSendableObject.this.getTransformedAABB(oMin, oMax, margin, tmpMin, tmpMax, instead);
				}

				@Override
				public void initPhysics() {
					SimpleTransformableSendableObject.this.initPhysics();
					getPhysicsDataContainer().lastTransform.set(remoteTransformable.getInitialTransform());
				}
			};
		} else {
			System.err.println("[Transformable][WARNING] Remote transformable already exists. skipped creation: " + this);
		}
	}

	@Override
	public boolean isMarkedForDeleteVolatile() {
		return markedForDelete;
	}

	@Override
	public void setMarkedForDeleteVolatile(boolean markedForDelete) {
		this.markedForDelete = markedForDelete;
		if(onServer) {
			Sector oldSector;
			if((oldSector = ((GameServerState) state).getUniverse().getSector(this.sectorId)) != null) {
				oldSector.removeEntity(this);
			}
		}
	}

	@Override
	public boolean isMarkedForDeleteVolatileSent() {
		return markedForDeleteSent;
	}

	@Override
	public void setMarkedForDeleteVolatileSent(boolean b) {
		markedForDeleteSent = b;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.objects.Sendable#isMarkedForPermanentDelete()
	 */
	@Override
	public boolean isMarkedForPermanentDelete() {
		return markedForPermanentDelete;
	}

	@Override
	public boolean isOkToAdd() {
		if(onServer) {
			return ((GameServerState) state).getUniverse().existsSector(sectorId);
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.objects.Sendable#isUpdatable()
	 */
	@Override
	public boolean isUpdatable() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.objects.Sendable#markedForPermanentDelete(boolean)
	 */
	@Override
	public void markForPermanentDelete(boolean mark) {
		this.markedForPermanentDelete = mark;
		if(onServer) {
			Sector oldSector;
			if((oldSector = ((GameServerState) state).getUniverse().getSector(this.sectorId)) != null) {
				oldSector.removeEntity(this);
			}
		}
	}

	@Override
	public void updateFromNetworkObject(NetworkObject o, int senderId) {
		remoteTransformable.updateFromRemoteTransform((NetworkEntity) o);
		NetworkEntity p = (NetworkEntity) o;
		this.setTracked(p.tracked.get());
		if(!onServer) {
			if(!getNetworkObject().graphicsEffectModifier.getReceiveBuffer().isEmpty()) {
				synchronized(graphicsEffectRecBuffer) {
					for(int i = 0; i < getNetworkObject().graphicsEffectModifier.getReceiveBuffer().size(); i++) {
						graphicsEffectRecBuffer.enqueue(getNetworkObject().graphicsEffectModifier.getReceiveBuffer().getByte(i));
					}
				}
			}
			if(this instanceof SegmentController && ((SegmentController) this).getDockingController().getDelayedDock() != null) {
				hiddenUpdate = true;
				//do NOT update hidden status as long as there a dock requested
			} else {
				if(hidden != p.hidden.get()) {
					//					System.err.println("[CLIENT] Object: "+this+" Set NT hidden "+hidden+" -> "+p.hidden.get());
					hiddenUpdate = p.hidden.get();
				}
			}
			if(sectorId != p.sector.get()) {
				int old = sectorId;
				int newSec = p.sector.get();
				clientSectorChange(old, newSec);
			}
			setFactionId(getNetworkObject().factionCode.get());
		}
		if(o instanceof NTRuleInterface) {
			getRuleEntityManager().receive(((NTRuleInterface) o));
		}
		if(p.gravity.get().gravityReceived) {
			this.receivedGravity = new NetworkGravity(p.gravity.get());
			p.gravity.get().gravityReceived = false;
			//			System.err.println(getState()+"; "+this+": Gravity received: "+this.receivedGravity);
		}
		for(long s : getNetworkObject().lagAnnouncement.getReceiveBuffer()) {
			currentLag = s;
			lastLagReceived = System.currentTimeMillis();
		}
	}

	@Override
	public void updateLocal(Timer timer) {
		state.getDebugTimer().start(this, "SimpleTransformableSendableObj");
		long t = System.currentTimeMillis();
		if(first) {
			if(!onServer) {
				E nt = createNetworkListenEntity();
				nt.setClientId(((GameClientState) state).getId());
				addListener(nt);
				nt.setId(this.getId());
				((GameClientController) state.getController()).getPrivateChannelSynchController().addNewSynchronizedObjectQueued(nt);
			}
			first = false;
		}
		if(onServer) {
			for(int i = 0; i < getListeners().size(); i++) {
				if(getListeners().get(i).isMarkedForDeleteVolatileSent()) {
					getListeners().remove(i);
					i--;
				}
			}
		}
		adminInvisibility = getOwnerState() != null && getOwnerState() instanceof PlayerState && ((PlayerState) getOwnerState()).isInvisibilityMode();
		if(invisibleNextDraw < 2) {
			invisibleNextDraw++;
		}
		if(hiddenUpdate != null) {
			//update from NT. Do in here so physics is ok on the update
			hidden = hiddenUpdate;
			hiddenUpdate = null;
		}
		//		if(!isOnServer()){
		//			assert(hidden || !(this.toString().contains("CSS-Valhalla-Reactor") && this.toString().contains("_dock_"))):this;
		//			//			System.err.println("[CLIENT] SET UNHIDDEN "+this);
		//		}
		//		if(isOnServer() && toString().contains("schema_1405985961592")){
		//			System.err.println("SERVER SEC: "+((GameServerState)getState()).getUniverse().getSector(getSectorId()));
		//		}
		if(!graphicsEffectRecBuffer.isEmpty()) {
			synchronized(graphicsEffectRecBuffer) {
				while(!graphicsEffectRecBuffer.isEmpty()) {
					executeGraphicalEffectClient(graphicsEffectRecBuffer.dequeueByte());
				}
			}
		}
		if(flagPhysicsInit || hidden != hiddenflag) {
			onPhysicsRemove();
			if(hidden) {
			} else {
				if(getPhysicsDataContainer().getObject() != null) {
					//dont add docked objects
					//they will be added in dock()
					//					if(isOnServer()){
					//						for(int i = 0; i < Element.DIRECTIONSi.length; i++){
					//							surround[i].add(((GameServerState)getState()).getUniverse().getSector(getSectorId()).pos, Element.DIRECTIONSi[i]);
					//						}
					//					}
					if(addToPhysicsOnInit()) {
						onPhysicsAdd();
					}
				}
			}
			hiddenflag = hidden;
			getPhysicsDataContainer().updatePhysical(state.getUpdateTime());
			flagPhysicsInit = false;
		}
		if(receivedGravity != null) {
			if(state instanceof ClientState && remoteTransformable.isSendFromClient() && !receivedGravity.forcedFromServer) {
				//clients that own that do not receive
				if(this instanceof AbstractCharacter<?>) {
					//					System.err.println(getState() + " [GRAVITY] " + this + " !!!IGNORE!!! RECEIVED REMOTE GRAVITY: " + receivedGravity.gravityIdReceive + " --- " + receivedGravity.gravityReceive+"; gravity was initialized from this. Currently: "+getGravity());
				}
				receivedGravity = null;
			} else {
				if(this instanceof AbstractCharacter<?>) {
					//					System.err.println(getState() + " [GRAVITY] " + this + " RECEIVED REMOTE GRAVITY: " + receivedGravity.gravityIdReceive + " --- " + receivedGravity.gravityReceive);
				}
				Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(receivedGravity.gravityIdReceive);
				if(receivedGravity.gravityIdReceive > 0 && sendable != null && sendable instanceof SimpleTransformableSendableObject) {
					if(gravity.source != sendable || (receivedGravity.gravityReceive.length() > 0 && gravity.magnitude() > 0 && !gravity.accelerationEquals(receivedGravity.gravityReceive))) {
						gravity.source = (SimpleTransformableSendableObject) (sendable);
						flagGravityUpdate = true;
						//						System.err.println(this + " GRAVITY UPDATE:::::: " + getGravity());
					}
					gravity.setAcceleration(receivedGravity.gravityReceive);
					receivedGravity = null;
				} else {
					if(receivedGravity.gravityIdReceive != -1) {
						//						System.err.println("[GRAVITY] GRAVITY DELAYED FOR " + this + ". source not found: " + receivedGravity.gravityIdReceive);
					} else {
						//						System.err.println(getState()+" "+this+" deactivated gravity from "+getGravity().source+" to nothing");
						gravity.setAcceleration(0, 0, 0);
//						if(gravity.source != null) {
//							gravity.source = null;
//							flagGravityUpdate = true;
//						}
						receivedGravity = null;
					}
				}
				if(onServer && timer.getDelta() - t > 200) {
					flagGravityDeligation = true;
				}
			}
		}
		boolean isMovingObject = getPhysicsDataContainer().isInitialized() && getPhysicsDataContainer().getObject() != null && !getPhysicsDataContainer().getObject().isStaticObject();
		if(onServer) {
			if(!hidden) {
				spawnController.update(timer);
			}
			vServerAttachment.update();
			if(!attachedAffinityInitial.isEmpty()) {
				int i = 0;
				while(!attachedAffinityInitial.isEmpty()) {
					AICreature<? extends AIPlayer> p = attachedAffinityInitial.dequeue();
					System.err.println("SPAWNING WITH " + this + ": " + p);
					p.setId(((ServerStateInterface) state).getNextFreeObjectId());
					p.setSectorId(sectorId);
					assert (p.getUniqueIdentifier() != null);
					p.setAffinity(this);
					((GameServerState) state).getController().getSynchController().addNewSynchronizedObjectQueued(p);
				}
				attachedAffinityInitial.clear();
			}
			if(Float.isNaN(getWorldTransform().origin.x)) {
				try {
					throw new IllegalStateException("Exception: NaN position for " + this + "; PObject: " + getPhysicsDataContainer().getObject());
				} catch(Exception e) {
					e.printStackTrace();
				}
				getWorldTransform().origin.set(400, 400, 400);
				if(getPhysicsDataContainer().getObject() != null) {
					((GameServerController) state.getController()).broadcastMessageAdmin(Lng.astr("Invalid position (NaN) for \n%s\nposition reset to\n local(400, 400, 400)", this), ServerMessage.MESSAGE_TYPE_ERROR);
					Transform tr = new Transform();
					tr.setIdentity();
					tr.origin.set(400, 400, 400);
					getPhysicsDataContainer().updateManually(tr);
					getPhysicsDataContainer().getObject().setWorldTransform(tr);
					if(!getPhysicsDataContainer().getObject().isStaticOrKinematicObject() && getPhysicsDataContainer().getObject() instanceof RigidBody) {
						MotionState myMotionState = ((RigidBody) getPhysicsDataContainer().getObject()).getMotionState();
						myMotionState.setWorldTransform(tr);
					}
				} else {
				}
			}
		}
		remoteTransformable.update(timer);
		if(onServer) {
			if(isMovingObject && isCheckSectorActive()) {
				//				if(!getWorldTransform().equals(getPhysicsDataContainer().lastTransform)){
				((GameServerState) state).getUniverse().getSectorBelonging(this);
				//				}
			}
		} else {
			if(isPlayerNeighbor(this.sectorId)) {
				calcWorldTransformRelative(((GameClientState) state).getCurrentSectorId(), ((GameClientState) state).getPlayer().getCurrentSector());
				if(((GameClientState) state).getCurrentSectorId() != this.sectorId) {
					//update static ghost object
					if(clientVirtualObject != null) {
						//						System.err.println("UPDATED CLIENT VIRTUAL BODY: "+this+"; "+getState()+": "+getWorldTransformClient().origin);
						//						clientVirtualObject.setWorldTransform(getWorldTransformOnClient());
						//						clientVirtualObject.setInterpolationWorldTransform(getWorldTransformOnClient());
						//						((RigidBody)clientVirtualObject).getMotionState().setWorldTransform(getWorldTransformOnClient());
						//
						//
						//						getPhysicsDataContainer().checkCenterOfMass(clientVirtualObject);
						Transform tr = new Transform(getWorldTransformOnClient());
						Vector3f lc = new Vector3f(getPhysicsDataContainer().lastCenter);
						tr.basis.transform(lc);
						tr.origin.add(lc);
						clientVirtualObject.setWorldTransform(tr);
						clientVirtualObject.setInterpolationWorldTransform(tr);
						((RigidBody) clientVirtualObject).getMotionState().setWorldTransform(tr);
						clientVirtualObject.activate(true);
					}
				}
			}
		}
		if(isMovingObject && (t - lastGravityUpdate > 1000 || lastGravityUpdate == 0)) {
			checkForGravity();
			handleGravity();
			lastGravityUpdate = t;
//			handlePlanetLoadChecker();
		}
		RuleEntityManager<?> ruleEntityManager = getRuleEntityManager();
		if(ruleEntityManager != null) {
			ruleEntityManager.update(timer);
		}
		if(timer.currentTime - lastLagReceived > 7000) {
			currentLag = 0;
		}
		updateToNetworkObject();
		long took = System.currentTimeMillis() - t;
		if(took > 200) {
			System.err.println("[SIMPLETRANSFORMABLE] " + state + " " + this + " update took " + took + " ms");
		}
		state.getDebugTimer().end(this, "SimpleTransformableSendableObj");
	}

	@Override
	public void updateToFullNetworkObject() {
		getNetworkObject().id.set(getId());
		getNetworkObject().sector.set(sectorId);
		getNetworkObject().factionCode.set(getFactionId());
		getNetworkObject().hidden.set(hidden);
		getNetworkObject().tracked.set(tracked);
		assert (state.getId() >= 0);
		remoteTransformable.updateToRemoteInitialTransform(getNetworkObject());
		updateToNetworkObject();
	}

	@Override
	public void updateToNetworkObject() {
		//		System.err.println("SIMPLETRANSFORM "+this+" "+getState()+" TO NT OBJT");
		if(onServer) {
			getNetworkObject().tracked.set(tracked);
			if(getNetworkObject().sector.getInt() != sectorId) {
				//				System.err.println("[SERVER] sending new sector ID for "+this+": "+getNetworkObject().sector.get()+" -> "+getSectorId());
				getNetworkObject().sector.set(sectorId);
			}
			int fid = getFactionId();
			if(getNetworkObject().factionCode.getInt() != fid) {
				getNetworkObject().factionCode.set(fid);
			}
			getNetworkObject().hidden.set(hidden);
		} else {
		}
		if(flagGravityDeligation) {
			System.err.println("[SimpleTransformable][GRAVITY] " + state + " sending gravity update " + gravity);
			//this is activated on server if it received
			//a gravity update.
			//the server will now deligate the change to all clients,
			//but the client who sent it originally will ignore it
			gravity.copyAccelerationTo(getNetworkObject().gravity.get().gravity);
			getNetworkObject().gravity.get().gravityId = gravity.source != null ? gravity.source.getId() : -1;
			getNetworkObject().gravity.setChanged(true);
			getNetworkObject().setChanged(true);
			flagGravityDeligation = false;
		}
		remoteTransformable.updateToRemoteTransform(getNetworkObject(), state);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.objects.Sendable#isWrittenForUnload()
	 */
	@Override
	public boolean isWrittenForUnload() {
		return writtenForUnload;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.objects.Sendable#setWrittenForUnload(boolean)
	 */
	@Override
	public void setWrittenForUnload(boolean b) {
		writtenForUnload = b;
	}

	@Override
	public void announceLag(long timeTaken) {
		if(System.currentTimeMillis() - lastLagSent > 1000) {
			assert (state.isSynched());
			getNetworkObject().lagAnnouncement.add(timeTaken);
			lastLagSent = System.currentTimeMillis();
		}
	}

	@Override
	public long getCurrentLag() {
		return currentLag;
	}

	@Override
	public boolean isPrivateNetworkObject() {
		return false;
	}

	private void clientSectorChange(int from, int to) {
		assert (!onServer);
		((GameClientState) state).getController().scheduleSectorChange(new SectorChange(this, from, to));
	}

	private Sector getDefaultSector() throws IOException {
		return ((GameServerState) state).getUniverse().getSector(Sector.DEFAULT_SECTOR);
	}

	public RuleEntityManager<?> getRuleEntityManager() {
		return null;
	}

	public abstract boolean isClientOwnObject();

	public void onPhysicsRemove() {
		getPhysicsDataContainer().onPhysicsRemove();
		try {
			if(onServer) {
				if(((GameServerState) state).getUniverse().getSector(sectorId) != null) {
					/*
					 * only remove on laoded sector. marked-for-remove sectors
					 * are already removed from the sector-map. This happens
					 * when SimpleTransformableSendableObject.cleanUpOnEntityDelete
					 * is called, triggered by a sector unload process
					 */
					getPhysics().removeObject(getPhysicsDataContainer().getObject());
				}
			} else {
				//always remove object on client
				getPhysics().removeObject(getPhysicsDataContainer().getObject());
				try {
					if(clientVirtualObject != null) {
						getPhysics().removeObject(clientVirtualObject);
						assert (!getPhysics().containsObject(clientVirtualObject)) : clientVirtualObject;
						clientVirtualObject = null;
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		} catch(SectorNotFoundRuntimeException e) {
			e.printStackTrace();
			System.err.println("[EXCEPTION] OBJECT REMOVAL FROM PHYSICS FAILED -> can continue");
		}
	}

	public PhysicsExt getPhysics() throws SectorNotFoundRuntimeException {
		return (PhysicsExt) getPhysicsState().getPhysics();
	}

	public PhysicsState getPhysicsState() throws SectorNotFoundRuntimeException {
		if(onServer) {
			Sector sector = ((GameServerState) state).getUniverse().getSector(sectorId);
			if(sector == null) {
				System.err.println("[ERROR][FATAL] Fatal Exception: SECTOR NULL FOR " + this + " " + sectorId);
				throw new SectorNotFoundRuntimeException(sectorId);
			}
			return sector;
		} else {
			return ((GameClientState) state);
		}
	}

	@Override
	public Transform getInitialTransform() {
		return remoteTransformable.getInitialTransform();
	}

	@Override
	public float getMass() {
		return remoteTransformable.getMass();
	}

	public void setMass(float mass) {
		this.remoteTransformable.setMass(mass);
	}

	@Override
	public PhysicsDataContainer getPhysicsDataContainer() {
		return remoteTransformable.getPhysicsDataContainer();
	}

	@Override
	public void setPhysicsDataContainer(PhysicsDataContainer physicsDataContainer) {
		remoteTransformable.setPhysicsDataContainer(physicsDataContainer);
	}

	/**
	 * @return the state
	 */
	@Override
	public StateInterface getState() {
		return state;
	}

	/**
	 * @return the onServer
	 */
	@Override
	public boolean isOnServer() {
		return onServer;
	}

	@Override
	public TransformTimed getWorldTransformOnClient() {
		assert (!onServer);
		GameClientState state = ((GameClientState) this.state);
		if(this.sectorId == state.getCurrentSectorId()) {
			return remoteTransformable.getWorldTransform();
		} else {
			return clientTransform;
		}
	}

	public abstract E createNetworkListenEntity();

	public void setAdminTrackedClient(PlayerState from, boolean tracked) {
		if(from.isAdmin()) {
			getNetworkObject().tracked.set(tracked, true);
		}
	}

	public void onWrite() {
	}

	public void flagPhysicsSlowdown() {
		long lastSlowdown = System.currentTimeMillis();
		if(lastSlowdown - this.lastSlowdown > 5000 || slowdownStart == 0) {
			//reset counter
			slowdownStart = lastSlowdown;
		}
		this.lastSlowdown = lastSlowdown;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.io.TagSerializable#fromTagStructure(org.schema.game.common.controller.io.Tag)
	 */
	@Override
	public void fromTagStructure(Tag tag) {
		assert (tag.getName().equals("transformable"));
		Tag[] values = (Tag[]) tag.getValue();
		setMass((Float) values[0].getValue());
		Tag[] list = (Tag[]) values[1].getValue();
		float[] t = new float[list.length];
		for(int i = 0; i < list.length; i++) {
			t[i] = (Float) list[i].getValue();
		}
		if(values.length > 2 && values[2].getType() != Type.FINISH && values[2].getType() != Type.BYTE) {
			aiFromTagStructure(values[2]);
		}
		if(values.length > 3 && values[3].getType() == Type.VECTOR3i) {
			tagSectorId.set((Vector3i) values[3].getValue());
		}
		if(values.length > 4 && values[4].getType() == Type.INT && "fid".equals(values[4].getName())) {
			setFactionId(((Integer) values[4].getValue()));
		}
		if(values.length > 5 && values[5].getType() == Type.STRING && "own".equals(values[5].getName())) {
			this.owner = (((String) values[5].getValue()));
		}
		if(values.length > 6 && values[6].getType() != Type.FINISH) {
			spawnController.fromTagStructure(values[6]);
		}
		Matrix4f ma = new Matrix4f(t);
		if(ma.determinant() == 0) {
			ma.setIdentity();
			Transform tr = new Transform(ma);
			tr.getOpenGLMatrix(t);
			try {
				throw new NullPointerException("ERROR: Read 0 matrix: " + this + ": catched! continue with standard matrix");
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		remoteTransformable.getInitialTransform().setFromOpenGLMatrix(t);
		remoteTransformable.getWorldTransform().setFromOpenGLMatrix(t);
	}

	public void aiFromTagStructure(Tag t) {
		if(this instanceof AiInterface && t.getType() != Type.BYTE) {
			((TagSerializable) ((AiInterface) this).getAiConfiguration()).fromTagStructure(t);
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.io.TagSerializable#toTagStructure()
	 */
	@Override
	public Tag toTagStructure() {
		Tag mass = new Tag(Type.FLOAT, "mass", getMass());
		Tag transform = new Tag("transform", Type.FLOAT);
		float[] t = new float[16];
		remoteTransformable.getWorldTransform().getOpenGLMatrix(t);
		for(int i = 0; i < 16; i++) {
			transform.addTag(new Tag(Type.FLOAT, "matrix", t[i]));
		}
		Tag aiTag;
		if(this instanceof ManagedSegmentController<?>) {
			aiTag = new Tag(Type.BYTE, null, (byte) 0);// //no longer save ai in tag to preserve settings for blueprint
		} else {
			aiTag = aiToTagStructure();
		}
		Tag secPos;
		if(sectorId < 0 || ((GameServerState) state).getUniverse().getSector(sectorId) == null) {
			//transient sector has been set by sector beforehand
			//or by shipOutline
			secPos = new Tag(Type.VECTOR3i, "sPos", transientSectorPos);
		} else {
			secPos = new Tag(Type.VECTOR3i, "sPos", ((GameServerState) state).getUniverse().getSector(sectorId).pos);
		}
		Tag fCode = (new Tag(Type.INT, "fid", factionId));
		assert (spawnController != null);
		Tag ownerTag = (new Tag(Type.STRING, "own", this.owner));
		return new Tag(Type.STRUCT, "transformable", new Tag[] {mass, transform, aiTag, secPos, fCode, ownerTag, spawnController.toTagStructure(), new Tag(Type.FINISH, "fin", null)});
	}

	public Tag aiToTagStructure() {
		Tag t = null;
		if(this instanceof AiInterface && (((AiInterface) this).getAiConfiguration() != this)) {
			assert (((AiInterface) this).getAiConfiguration() != null) : toString();
			t = ((TagSerializable) ((AiInterface) this).getAiConfiguration()).toTagStructure();
		} else {
			t = new Tag(Type.BYTE, "noAI", (byte) 0);
		}
		return t;
	}

	public byte getDebugMode() {
		return getNetworkObject().debugMode.get().byteValue();
	}

	public void setDebugMode(byte b) {
		getNetworkObject().debugMode.set(b, true);
	}

	public boolean isInExitingFaction() {
		return ((FactionState) state).getFactionManager().existsFaction(factionId);
	}

	/**
	 * @return the gravity
	 */
	public GravityState getGravity() {
		return gravity;
	}

	public void getGravityAABB(Vector3f minOut, Vector3f maxOut) {
		Transform tt = getPhysicsDataContainer().addCenterOfMassToTransform(new Transform(getWorldTransform()));
		getPhysicsDataContainer().getShape().getAabb(tt, minOut, maxOut);
	}

	/**
	 * @return the lastSlowdown
	 */
	public long getLastSlowdown() {
		return lastSlowdown;
	}

	public void getRelationColor(RType relation, boolean sameFaction, Vector4f out, float select, float pulse) {
		if(relation == RType.ENEMY) {
			out.x = 1f + select;
			out.y = select;
			out.z = select;
			return;
		}
		if(((GameClientState) state).getPlayer().getFactionId() != this.getFactionId() && ((GameClientState) state).getFactionManager().existsFaction(this.getFactionId()) && ((GameClientState) state).getFactionManager().getFaction(this.getFactionId()).getFactionMode() != 0) {
			Faction faction = ((GameClientState) state).getFactionManager().getFaction(this.getFactionId());
			out.x = faction.getColor().x;
			out.y = faction.getColor().y;
			out.z = faction.getColor().z;
			return;
		}
		if(((GameClientState) state).getPlayer().getFactionId() == this.getFactionId() && ((GameClientState) state).getFactionManager().existsFaction(this.getFactionId()) && ((GameClientState) state).getFactionManager().getFaction(this.getFactionId()).isFactionMode(Faction.MODE_FIGHTERS_FFA)) {
			//always enemy
			out.x = 1f + select;
			out.y = select;
			out.z = select;
			return;
		}
		out.x = 0.3f + select;
		out.y = 0.3f;
		out.z = select;
	}

	public Vector3f getRelativeUniverseServerPosition(SimpleTransformableSendableObject from) {
		//FIXME not accounted for rotating star sys
		assert (onServer);
		Sector fromSector = ((GameServerState) state).getUniverse().getSector(from.sectorId);
		Sector sector = ((GameServerState) state).getUniverse().getSector(sectorId);
		Vector3i d = new Vector3i();
		d.sub(fromSector.pos, sector.pos);
		Vector3f pos = new Vector3f(d.x * ((GameStateInterface) state).getSectorSize(), d.y * ((GameStateInterface) state).getSectorSize(), d.z * ((GameStateInterface) state).getSectorSize());
		pos.add(getWorldTransform().origin);
		return pos;
	}

	public RemoteTransformable getRemoteTransformable() {
		return remoteTransformable;
	}

	public void setRemoteTransformable(RemoteTransformable remoteTransformable) {
		this.remoteTransformable = remoteTransformable;
	}

	/**
	 * @return the slowdownStart
	 */
	public long getSlowdownStart() {
		return slowdownStart;
	}

	/**
	 * @return the tagSectorId
	 */
	public Vector3i getTagSectorId() {
		return tagSectorId;
	}

	private boolean gravityChecksRequired() {
		//only check for the state that is currently
		//in control
		boolean check = remoteTransformable.isSendFromClient() || (onServer && (!isConrolledByActivePlayer() || forcedCheckFlag));
		forcedCheckFlag = false;
//		return check;
		return true;
	}

	protected void handleGravity() {
		if(getPhysicsDataContainer().getObject() instanceof RigidBody) {
			RigidBody body = (RigidBody) getPhysicsDataContainer().getObject();
			float grav = 1f;
			if(this instanceof ConfigManagerInterface) {
				grav = ((ConfigManagerInterface) this).getConfigManager().apply(StatusEffectType.THRUSTER_ANTI_GRAVITY, grav);
			}
			this.gravity.copyAccelerationTo(tmp);
			tmp.scale(grav);
			modivyGravity(tmp);
			if(this.gravity.source != null) {
				this.gravity.source.getWorldTransform().basis.transform(tmp);
			}
			body.setGravity(tmp);
		}
	}

	protected void modivyGravity(Vector3f inout) {
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		assert (id < Integer.MAX_VALUE && id > Integer.MIN_VALUE);
		return id;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return obj instanceof Identifiable && ((Identifiable) obj).getId() == getId();
	}

	protected boolean hasVirtual() {
		return ((GameStateInterface) state).isPhysicalAsteroids() || !(this instanceof FloatingRock);
	}

	@Override
	public void engageWarp(String warpToUID, boolean allowDirect, long delay, Vector3i local, int maxDist) {
		assert (onServer);
		if(warpToUID.equals("none")) {
			sendControllingPlayersServerMessage(Lng.astr("Gate has no\nDestination!"), ServerMessage.MESSAGE_TYPE_ERROR);
		} else {
			if(warpToUID.startsWith("ENTITY_")) {
				synchronized(state) {
					Sendable sendable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(warpToUID);
					Sector to = null;
					Vector3f pos = null;
					String destName = null;
					Sector own = ((GameServerState) state).getUniverse().getSector(sectorId);
					if(own == null) {
						sendControllingPlayersServerMessage(Lng.astr("ERROR\nOwn sector invalid (%s)", sectorId), ServerMessage.MESSAGE_TYPE_ERROR);
						return;
					}
					if(sendable != null) {
						if(sendable instanceof SimpleTransformableSendableObject) {
							if(sendable instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) sendable).getManagerContainer() instanceof StationaryManagerContainer<?>) {
								StationaryManagerContainer<?> man = (StationaryManagerContainer<?>) ((ManagedSegmentController<?>) sendable).getManagerContainer();
								boolean found = false;
								for(int i = 0; i < man.getWarpgate().getCollectionManagers().size(); i++) {
									WarpgateCollectionManager warpgateCollectionManager = man.getWarpgate().getCollectionManagers().get(i);
									if(warpgateCollectionManager.isValid()) {
										to = ((GameServerState) state).getUniverse().getSector(((SimpleTransformableSendableObject) sendable).sectorId);
										to.setActive(true);
										pos = new Vector3f(((SimpleTransformableSendableObject) sendable).getWorldTransform().origin);
										destName = ((SimpleTransformableSendableObject) sendable).getRealName();
										found = true;
										break;
									}
								}
								if(!found) {
									System.err.println("[WARPGATE] Object was loaded but no destination gate is valid!");
									sendControllingPlayersServerMessage(Lng.astr("Destination gate not valid!"), ServerMessage.MESSAGE_TYPE_ERROR);
								}
							}
						}
					} else {
						try {
							List<DatabaseEntry> byUIDExact = ((GameServerState) state).getDatabaseIndex().getTableManager().getEntityTable().getByUIDExact(DatabaseEntry.removePrefix(warpToUID), -1);
							if(byUIDExact.size() > 0) {
								for(int i = 0; i < byUIDExact.size(); i++) {
									DatabaseEntry databaseEntry = byUIDExact.get(i);
									if(databaseEntry.type == EntityType.PLANET_SEGMENT.dbTypeId || databaseEntry.type == EntityType.SPACE_STATION.dbTypeId) {
										Vector3i activeWarpGatePos = StationaryManagerContainer.getActiveWarpGate(warpToUID);
										if(activeWarpGatePos != null) {
											to = ((GameServerState) state).getUniverse().getSector(databaseEntry.sectorPos, true);
											pos = new Vector3f(databaseEntry.pos);
											destName = databaseEntry.realName;
										} else {
											sendControllingPlayersServerMessage(Lng.astr("Destination gate is inactive or not valid!"), ServerMessage.MESSAGE_TYPE_ERROR);
										}
									} else {
										sendControllingPlayersServerMessage(Lng.astr("Destination invalid!\n"), ServerMessage.MESSAGE_TYPE_ERROR);
									}
								}
							} else {
								sendControllingPlayersServerMessage(Lng.astr("Gate destination invalid!\nDestination structure\nnot found!"), ServerMessage.MESSAGE_TYPE_ERROR);
							}
						} catch(SQLException e) {
							e.printStackTrace();
						} catch(IOException e) {
							e.printStackTrace();
						}
					}
					if(to == null) {
						sendControllingPlayersServerMessage(Lng.astr("Destination unavailable!"), ServerMessage.MESSAGE_TYPE_ERROR);
					} else {
						Vector3d dist = new Vector3d(to.pos.x - own.pos.x, to.pos.y - own.pos.y, to.pos.z - own.pos.z);
						if(dist.length() < maxDist) {
							getNetworkObject().graphicsEffectModifier.add((byte) 1);
							SectorSwitch queueSectorSwitch = ((GameServerState) state).getController().queueSectorSwitch(this, to.pos, SectorSwitch.TRANS_JUMP, false, true, true);
							if(queueSectorSwitch != null) {
								queueSectorSwitch.delay = System.currentTimeMillis() + delay;
								queueSectorSwitch.jumpSpawnPos = new Vector3f(pos);
								queueSectorSwitch.keepJumpBasisWithJumpPos = true;
								queueSectorSwitch.executionGraphicsEffect = (byte) 2;
								sendControllingPlayersServerMessage(Lng.astr("Jumping to Station\n%s\nin sector", destName, to.pos), ServerMessage.MESSAGE_TYPE_INFO);
							} else {
								sendControllingPlayersServerMessage(Lng.astr("Gate on cooldown!"), ServerMessage.MESSAGE_TYPE_ERROR);
							}
						} else {
							sendControllingPlayersServerMessage(Lng.astr("Gate Destination too far!\nDistance in Sectors: %s,\nbut max %s Sectors possible", StringTools.formatPointZero(dist.length()), maxDist), ServerMessage.MESSAGE_TYPE_ERROR);
						}
					}
				}
			} else if(warpToUID.startsWith(FTLTable.DIRECT_PREFIX)) {
				if(!allowDirect) {
					sendControllingPlayersServerMessage(Lng.astr("Reactor doesn't support direct warp gates!"), ServerMessage.MESSAGE_TYPE_ERROR);
					return;
				}
				Sector own = ((GameServerState) state).getUniverse().getSector(sectorId);
				if(own == null) {
					sendControllingPlayersServerMessage(Lng.astr("ERROR\nOwn sector invalid (%s)", sectorId), ServerMessage.MESSAGE_TYPE_ERROR);
					return;
				}
				warpArbitrary(local, delay, own, maxDist);
			}
		}
	}

	@Override
	public void sendControllingPlayersServerMessage(Object[] msg, int msgtype) {
		if(onServer && this instanceof PlayerControllable && !((PlayerControllable) this).getAttachedPlayers().isEmpty()) {
			((GameServerState) state).getController().sendPlayerMessage(((PlayerControllable) this).getAttachedPlayers().get(0).getName(), msg, msgtype);
		}
	}

	@Override
	public boolean handleCollision(int index, RigidBody originalBody, RigidBody originalBody2, SolverConstraint contactConstraint) {
		return false;
	}

	/**
	 * @return the sectorId
	 */
	@Override
	public final int getSectorId() {
		return sectorId;
	}

	/**
	 * @param sectorId the sectorId to set
	 */
	public void setSectorId(int sectorId) {
		//		if(this instanceof AbstractCharacter<?>){
		//		try{
		//		throw new NullPointerException("SETTING FROM "+this.sectorId+" to "+sectorId);
		//		}catch (Exception e) {
		//			e.printStackTrace();
		//		}
		//		}
		if(isClientOwnObject() && this.sectorId != sectorId) {
			sectorChangedTimeOwnClient = System.currentTimeMillis();
		}
		if(this.sectorId != sectorId) {
			sectorChangedTime = System.currentTimeMillis();
			if(onServer) {
				Sector oldSector = ((GameServerState) state).getUniverse().getSector(this.sectorId);
				Sector newSector = ((GameServerState) state).getUniverse().getSector(sectorId);
				if(oldSector != null) {
					oldSector.removeEntity(this);
				}
				if(newSector != null) {
					newSector.addEntity(this);
				}
			}
		}
		this.sectorId = sectorId;
	}

	@Override
	public void getGravityAABB(Transform t, Vector3f minOut, Vector3f maxOut) {
		getPhysicsDataContainer().getShape().getAabb(t, minOut, maxOut);
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	private void warpArbitrary(Vector3i local, long delay, Sector own, int maxDist) {
		Vector3i to = new Vector3i(local); //local is used as sector for direct
		Vector3d dist = new Vector3d(to.x - own.pos.x, to.y - own.pos.y, to.z - own.pos.z);
		if(dist.length() < maxDist) {
			getNetworkObject().graphicsEffectModifier.add((byte) 1);
			try {
				Sector sector = ((GameServerState) state).getUniverse().getSector(to);
				if(sector.getSectorType() == SectorType.PLANET) {
					sendControllingPlayersServerMessage(Lng.astr("Cannot jump directly to sector %s (planet)", to.toStringPure()), ServerMessage.MESSAGE_TYPE_INFO);
					to.y++;
					warpArbitrary(to, delay, own, maxDist);
					return;
				}
				Set<SimpleTransformableSendableObject<?>> entities = sector.getEntities();
				for(SimpleTransformableSendableObject<?> e : entities) {
					if(e.isHomeBase() && !e.isHomeBaseFor(getFactionId())) {
						sendControllingPlayersServerMessage(Lng.astr("Cannot jump directly to sector %s (homebase)", to.toStringPure()), ServerMessage.MESSAGE_TYPE_INFO);
						to.y++;
						warpArbitrary(to, delay, own, maxDist);
						return;
					}
				}
				SectorSwitch queueSectorSwitch = ((GameServerState) state).getController().queueSectorSwitch(this, to, SectorSwitch.TRANS_JUMP, false, true, true);
				if(queueSectorSwitch != null) {
					queueSectorSwitch.delay = System.currentTimeMillis() + delay;
					queueSectorSwitch.jumpSpawnPos = new Vector3f(0, 0, 0);
					queueSectorSwitch.keepJumpBasisWithJumpPos = true;
					queueSectorSwitch.executionGraphicsEffect = (byte) 2;
					sendControllingPlayersServerMessage(Lng.astr("Jumping directly to sector %s", to.toStringPure()), ServerMessage.MESSAGE_TYPE_INFO);
				} else {
					sendControllingPlayersServerMessage(Lng.astr("Gate on cooldown!"), ServerMessage.MESSAGE_TYPE_ERROR);
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		} else {
			sendControllingPlayersServerMessage(Lng.astr("Gate Destination too far!\nDistance in Sectors: %s,\nbut max %s Sectors possible", StringTools.formatPointZero(dist.length()), maxDist), ServerMessage.MESSAGE_TYPE_ERROR);
		}
	}

	protected boolean isCheckSectorActive() {
		return true;
	}

	public boolean isClientSectorIdValidForSpawning(int sectorId) {
		return sectorId == ((GameClientState) state).getCurrentSectorId() || isPlayerNeighbor(sectorId);
	}

	public boolean isConrolledByActivePlayer() {
		return (this instanceof PlayerControllable) && !((PlayerControllable) this).getAttachedPlayers().isEmpty();
	}

	/**
	 * @return the flagPhysicsInit
	 */
	public boolean isFlagPhysicsInit() {
		return flagPhysicsInit;
	}

	/**
	 * @param flagPhysicsInit the flagPhysicsInit to set
	 */
	public void setFlagPhysicsInit(boolean flagPhysicsInit) {
		this.flagPhysicsInit = flagPhysicsInit;
	}

	public boolean isGravitySource() {
		return false;
	}

	public boolean isHomeBase() {
		//		Faction f = ((FactionState)getState()).getFactionManager().getFaction(getFactionId());
		//		return f != null && f.getHomebaseUID().equals(getUniqueIdentifier());
		return isHomeBaseFor(getFactionId());
	}

	public boolean isHomeBaseFor(int forFactionId) {
		if(getFactionId() == 0 || forFactionId != getFactionId()) {
			return false;
		} else {
			Faction f = ((FactionState) state).getFactionManager().getFaction(forFactionId);
			boolean homeBase = f != null && f.getHomebaseUID().equals(getUniqueIdentifier());
			//			if(f != null && f.isNPC()){
			//				System.err.println("IS NPC HOMEBASE: '"+getUniqueIdentifier()+"' equals '"+f.getHomebaseUID()+"'?");
			//			}
			if(homeBase) {
				if(f != null && f.isNPC()) {
					return true;
				}
				if(f != null && f.lastSystemSectors.size() == 0 && f.factionPoints < 0) {
					return false;
				}
			}
			return homeBase;
		}
	}

	/**
	 * @return the immediateStuck
	 */
	public boolean isImmediateStuck() {
		return immediateStuck;
	}

	/**
	 * @param immediateStuck the immediateStuck to set
	 */
	public void setImmediateStuck(boolean immediateStuck) {
		this.immediateStuck = immediateStuck;
	}

	public boolean isNeighbor(int sectorId, int currentSectorId) {
		if(onServer) {
			Sector objSector = ((GameServerState) state).getUniverse().getSector(sectorId);
			Sector currentSector = ((GameServerState) state).getUniverse().getSector(currentSectorId);
			if(objSector != null && currentSector != null) {
				return Sector.isNeighbor(objSector.pos, currentSector.pos);
			} else {
				System.err.println("WARNING while checking neighbor: " + objSector + "; " + currentSector + ";    " + sectorId + "; " + currentSectorId);
				return false;
			}
		} else {
			RemoteSector objSector = (RemoteSector) state.getLocalAndRemoteObjectContainer().getLocalObjects().get(sectorId);
			RemoteSector currentSector = (RemoteSector) state.getLocalAndRemoteObjectContainer().getLocalObjects().get(currentSectorId);
			if(objSector != null && currentSector != null) {
				return Sector.isNeighbor(objSector.clientPos(), currentSector.clientPos());
			} else {
				System.err.println("WARNING while checking neighbor: " + objSector + "; " + currentSector + ";    " + sectorId + "; " + currentSectorId);
				return false;
			}
		}
	}

	public boolean isPlayerNeighbor(int sectorId) {
		RemoteSector objSector = (RemoteSector) state.getLocalAndRemoteObjectContainer().getLocalObjects().get(sectorId);
		return objSector != null && Sector.isNeighbor(objSector.clientPos(), ((GameClientState) state).getPlayer().getCurrentSector());
	}

	public void onPhysicsAdd() {
		assert (!onServer || ((Sector) getPhysics().getState()) == ((GameServerState) state).getUniverse().getSector(sectorId));
		//		assert(!isHidden()):this;
		assert (sectorId != Sector.SECTOR_INITIAL);
		getPhysicsDataContainer().onPhysicsAdd();
		assert (!onServer || ((Sector) getPhysics().getState()) == ((GameServerState) state).getUniverse().getSector(sectorId));
		if(onServer || isClientSectorIdValidForSpawning(sectorId)) {
			if(onServer) {
				//server always adds real object to its own sector physics
				assert (((Sector) getPhysics().getState()) == ((GameServerState) state).getUniverse().getSector(sectorId));
				assert (getPhysics().getState() == getPhysicsState());
				getPhysics().addObject(getPhysicsDataContainer().getObject(), getPhysicsDataContainer().collisionGroup, getPhysicsDataContainer().collisionMask);
			} else {
				if(sectorId == ((GameClientState) state).getCurrentSectorId()) {
					//real client object
					getPhysics().addObject(getPhysicsDataContainer().getObject(), getPhysicsDataContainer().collisionGroup, getPhysicsDataContainer().collisionMask);
				} else {
					//virtual client object
					getPhysicsDataContainer().getObject().getWorldTransform(getWorldTransform());
					calcWorldTransformRelative(((GameClientState) state).getCurrentSectorId(), ((GameClientState) state).getPlayer().getCurrentSector());
					RigidBody clientVirtualObject = getPhysics().getBodyFromShape(getPhysicsDataContainer().getShape(), 0, new Transform(getWorldTransformOnClient()));
					clientVirtualObject.setCollisionFlags(CollisionFlags.KINEMATIC_OBJECT);
					clientVirtualObject.setUserPointer(getId());
					//						getPhysicsDataContainer().setObject(stat);
					getPhysics().addObject(clientVirtualObject, getPhysicsDataContainer().collisionGroup, getPhysicsDataContainer().collisionMask);
					if(clientVirtualObject instanceof RigidBodySegmentController) {
						((RigidBodySegmentController) clientVirtualObject).virtualString = "virtC" + ((GameClientState) state).getPlayer().getCurrentSector() + "{orig" + this.getPhysicsDataContainer().getObject().getWorldTransform(new Transform()).origin + "}";
						((RigidBodySegmentController) clientVirtualObject).virtualSec = new Vector3i(((GameClientState) state).getPlayer().getCurrentSector());
					}
					assert (getPhysics().containsObject(clientVirtualObject));
					this.clientVirtualObject = clientVirtualObject;
					this.clientVirtualObject.setInterpolationWorldTransform(getWorldTransformOnClient());
					this.clientVirtualObject.activate(true);
				}
			}
			//			System.err.println("[PHYSICS-ADD]["+(isOnServer() ? "SERVER" : "CLIENT")+"] ADDING TO PHYSICS: "+this);
		} else {
			//			System.err.println("[PHYSICS-ADD] !! NOT ADDING TO PHYSICS: "+this+":  "+sectorId +" / "+ ((GameClientState)getState()).getCurrentSectorId());
		}
	}

	public void onSectorInactiveClient() {
		//may be overwritted
	}

	public boolean overlapsAABB(SimpleTransformableSendableObject target, Transform targetTransform, float margin) {
		Vector3f minA = new Vector3f();
		Vector3f maxA = new Vector3f();
		Vector3f minB = new Vector3f();
		Vector3f maxB = new Vector3f();
		getTransformedAABB(minA, maxA, margin, new Vector3f(), new Vector3f(), null);
		target.getTransformedAABB(minB, maxB, margin, new Vector3f(), new Vector3f(), targetTransform);
		return AabbUtil2.testAabbAgainstAabb2(minA, maxA, minB, maxB);
	}

	public void removeGravity() {
		setGravity(noGrav, null, false, false);
	}

	public void resetSlowdownStart() {
		this.slowdownStart = 0;
	}

	public void scheduleGravity(Vector3f acc, SimpleTransformableSendableObject o) {
		scheduledGravity = new GravityState();
		scheduledGravity.setAcceleration(acc);
		scheduledGravity.source = o;
		scheduledGravity.forcedFromServer = false;
	}

	public void scheduleGravityWithBlockBelow(Vector3f acc, SimpleTransformableSendableObject o) {
		scheduledGravity = new GravityState();
		scheduledGravity.setAcceleration(acc);
		scheduledGravity.source = o;
		scheduledGravity.forcedFromServer = false;
		scheduledGravity.withBlockBelow = true;
	}

	public void scheduleGravityServerForced(Vector3f acc, SimpleTransformableSendableObject source) {
		scheduledGravity = new GravityState();
		scheduledGravity.setAcceleration(acc);
		scheduledGravity.source = source;
		scheduledGravity.forcedFromServer = true;
		forcedCheckFlag = true;
	}

	public void forceGravityCheck() {
		forcedCheckFlag = true;
	}

	public void searchForGravity() {
		if(hidden || (this instanceof AbstractCharacter<?> && ((AbstractCharacter<?>) this).getOwnerState() != null && ((AbstractCharacter<?>) this).getOwnerState().isSitting())) {
			return;
		}
		boolean found = false;
		for(SimpleTransformableSendableObject<?> o : ((GravityStateInterface) state).getCurrentGravitySources()) {
			//			if(this instanceof PlayerCharacter){
			//				System.err.println("[GRAVITY] CHECKING FOR "+getState()+" "+o+"; "+o.affectsGravityOf(this));
			//			}
			if(gravity.source != o && o.affectsGravityOf(this)) {
//				if(o.personalGravitySwitch) {
				System.err.println("[GRAVITY] FOUND GRAV FOR " + this + "; " + state);
				if(o instanceof SegmentController && ((SegmentController) o).getConfigManager().apply(StatusEffectType.GRAVITY_OVERRIDE_ENTITY_CENTRAL, 1f) != 1f) {
					if(o instanceof CelestialBodyGravityHandler celestialObject) {
						setGravity(celestialObject.getGravityVector(this), o, true, false);
					} else {
						setGravity(new Vector3f(0.0f, 0.0f, 0.1f), o, true, false);
					}
				} else {
					if(o instanceof CelestialBodyGravityHandler celestialObject) {
						setGravity(celestialObject.getGravityVector(this), o, false, false);
						//Should (hopefully) fix gravity on new planets not pulling entities in the right direction.
					} else {
						setGravity(new Vector3f(0, 0, 10.1f), o, false, false);
					}
				}
//				}
			}
		}
		//		if(!found){
		//			if(isOnServer() && Keyboard.isCreated() && Keyboard.isKeyDown(GLFW.GLFW_KEY_F2) && this instanceof AICharacter){
		//				System.err.println(this+"; "+this.getState()+" no gravity found: "+((GravityStateInterface)getState()).getCurrentGravitySources());
		//			}
		//		}
	}

	public void setGravity(Vector3f acceleration, SimpleTransformableSendableObject<?> source, boolean central, boolean forcedFromServer) {
		if(gravity.source != source) {
//			if(gravity.source == null || source == null || gravity.source.id != source.id) {
			System.err.println("[GRAVITY] " + this + " " + state + " SOURCE CHANGE " + gravity.source + " -> " + source);
			gravity.source = source;
			gravity.setChanged(true);
//			}
		}
		if(gravity.central != central) {
			System.err.println("[GRAVITY] " + this + " " + state + " central CHANGE " + gravity.central + " -> " + central);
			gravity.central = central;
			gravity.setChanged(true);
		}
		if(!gravity.accelerationEquals(acceleration)) {
			System.err.println("[GRAVITY] " + this + " " + state + " Acceleration changed to " + acceleration);
			gravity.setAcceleration(acceleration);
			gravity.setChanged(true);
		}
		if(gravity.isChanged()) {
			getNetworkObject().gravity.get().gravityId = source != null ? source.getId() : -1;
			getNetworkObject().gravity.get().gravity.set(acceleration);
			getNetworkObject().gravity.get().forcedFromServer = forcedFromServer;
			getNetworkObject().gravity.setChanged(true);
			getNetworkObject().setChanged(true);
			System.err.println("[SIMPLETRANSFORMABLE] " + state + " " + this + " gravity change sent: " + getNetworkObject().gravity.get());
		}
	}

	public void setHiddenForced(boolean hidden) {
		this.hidden = hidden;
	}

	public abstract String toNiceString();

	/**
	 * Structures can overwrite the getFactionId() function.
	 * this will return the original
	 *
	 * @return the faction id as it is set in the field of this instance
	 */
	public final int getOriginalFactionId() {
		return factionId;
	}

	protected boolean addToPhysicsOnInit() {
		// overwritten by segmentcontroller for example
		// to avoid adding ships with a rail request
		// in progress
		return true;
	}

	public void warpTransformable(float x, float y, float z, boolean broadcast, LocalSectorTransition trn) {
		Transform worldTransform = new Transform(getWorldTransform());
		worldTransform.origin.set(x, y, z);
		warpTransformable(worldTransform, broadcast, false, null);
	}

	public void warpTransformable(Transform t, boolean broadcast, boolean withRot, LocalSectorTransition trn) {
		if(getPhysicsDataContainer().isInitialized()) {
			if(getPhysicsDataContainer().getObject() instanceof PairCachingGhostObjectAlignable) {
				PairCachingGhostObjectAlignable o = ((PairCachingGhostObjectAlignable) getPhysicsDataContainer().getObject());
				if(o.getAttached() != null && o.localWorldTransform != null) {
					Transform f = new Transform(o.getAttached().getWorldTransform());
					f.inverse();
					Transform rel = new Transform(t);
					f.mul(rel);
					o.localWorldTransform.set(f);
				}
			}
			remoteTransformable.warp(new Transform(t), withRot);
			if(onServer && broadcast) {
				if(getPhysicsDataContainer().getObject() instanceof RigidBody) {
					RigidBody b = (RigidBody) getPhysicsDataContainer().getObject();
					Vector3f linVelo = new Vector3f();
					Vector3f angVelo = new Vector3f();
					b.getLinearVelocity(linVelo);
					b.getAngularVelocity(angVelo);
					remoteTransformable.broadcastTransform(new Transform(t), linVelo, angVelo, trn, getNetworkObject());
				} else {
					remoteTransformable.broadcastTransform(new Transform(t), new Vector3f(), new Vector3f(), trn, getNetworkObject());
				}
			}
		}
	}

	public void onSmootherSet(Transform t) {
	}

	public abstract String getRealName();

	/**
	 * @return the lastWrite
	 */
	public long getLastWrite() {
		return this.lastWrite;
	}

	public void setLastWrite(long a) {
		this.lastWrite = a;
	}

	public void executeGraphicalEffectServer(byte effect) {
		getNetworkObject().graphicsEffectModifier.add(effect);
	}

	/**
	 * to be overwritten
	 *
	 * @param b
	 */
	public void executeGraphicalEffectClient(byte b) {
	}

	/**
	 * @return the spawnController
	 */
	public SpawnController getSpawnController() {
		return spawnController;
	}

	public float getIndicatorDistance() {
		return 0;
	}

	public float getIndicatorMaxDistance(RType relation) {
		return ((GameStateInterface) state).getSectorSize();
	}

	@Override
	public boolean hasChangedForDb() {
		return changed;
	}

	@Override
	public void setChangedForDb(boolean changed) {
		this.changed = changed;
	}

	public void setInvisibleNextDraw() {
		invisibleNextDraw = -10;
	}

	public String getInfo() {
		return "LoadedEntity [uid=" + getUniqueIdentifier() + ", type=" + getType().name() + "]";
	}

	public boolean isInvisibleNextDraw() {
		return invisibleNextDraw < 2 || adminInvisibility;
	}

	public EffectElementManager<?, ?, ?> getEffect(Damager from, short effectType) {
		if(effectType == 0) {
			return null;
		}
		if(from != null && from instanceof ManagedSegmentController<?>) {
			EffectElementManager<?, ?, ?> effect = ((EffectManagerContainer) ((ManagedSegmentController<?>) from).getManagerContainer()).getEffect(effectType);
			return effect;
		} else if(this instanceof ManagedSegmentController<?>) {
			EffectElementManager<?, ?, ?> effect = ((EffectManagerContainer) ((ManagedSegmentController<?>) this).getManagerContainer()).getEffect(effectType);
			return effect;
		} else {
			System.err.println(state + " SEVERE WARNING:  NO EFFECT FOUND FOR: DAMAGER: " + from + " -> " + this + "; ");
			return null;
		}
	}

	public boolean isInAdminInvisibility() {
		return adminInvisibility;
	}

	public boolean isChainedToSendFromClient() {
		return false;
	}

	public ObjectOpenHashSet<AICreature<? extends AIPlayer>> getAttachedAffinity() {
		return attachedAffinity;
	}

	public float getProhibitingBuildingAroundOrigin() {
		return prohibitingBuildingAroundOrigin;
	}

	public void setProhibitingBuildingAroundOrigin(float prohibitingBuildingAroundOrigin) {
		this.prohibitingBuildingAroundOrigin = prohibitingBuildingAroundOrigin;
	}

	/**
	 * overwrite for extra information in the target panel
	 *
	 * @return
	 */
	public String getAdditionalObjectInformation() {
		return null;
	}

	public Light getLight() {
		return light;
	}

	public void setLight(Light light) {
		this.light = light;
	}

	public boolean isWarpToken() {
		return warpToken;
	}

	public void setWarpToken(boolean warpToken) {
		this.warpToken = warpToken;
	}

	public long getSectorChangedTime() {
		return sectorChangedTime;
	}

	public void setSectorChangedTime(long sectorChangedTime) {
		this.sectorChangedTime = sectorChangedTime;
	}

	public void onSectorSwitchServer(Sector newSector) {
	}

	public boolean isClientCleanedUp() {
		return clientCleanedUp;
	}

	public void setClientCleanedUp(boolean clientCleanedUp) {
		this.clientCleanedUp = clientCleanedUp;
	}

	public void setNeedsPositionCheckOnLoad(boolean needsposcheck) {
		this.needsPositionCheckOnLoad = needsposcheck;
	}

	public boolean isNPCFactionControlledAI() {
		return false;
	}

	public boolean isAIControlled() {
		return false;
	}

	public BoundingSphere getBoundingSphere() {
		return boundingSphere;
	}

	@Override
	public BoundingSphere getBoundingSphereTotal() {
		return boundingSphereTotal;
	}

	public String getUniqueIdentifierFull() {
		return getType().dbPrefix + getUniqueIdentifier();
	}

	private boolean canAllySeeStructure(ManagedSegmentController<?> target) {
		FactionManager factionManager = GameCommon.getGameState().getFactionManager();
		for(Sendable sendable : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
			if(sendable instanceof ManagedUsableSegmentController<?> other) {
				if(sendable != this) {
					if(factionManager.isFriend(other.getFactionId(), getFactionId()) && getFactionId() != 0) {
						if(other.canSeeStructure(target.getSegmentController(), false) && other.getConfigManager().apply(StatusEffectType.SCAN_SHORT_RANGE_SHARING_ENABLE, false)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private boolean canAllySeeIndicator(ManagedSegmentController<?> target) {
		FactionManager factionManager = GameCommon.getGameState().getFactionManager();
		for(Sendable sendable : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
			if(sendable instanceof ManagedUsableSegmentController<?> other) {
				if(sendable != this) {
					if(factionManager.isFriend(other.getFactionId(), getFactionId()) && getFactionId() != 0) {
						if(other.canSeeIndicator(target.getSegmentController(), false) && other.getConfigManager().apply(StatusEffectType.SCAN_SHORT_RANGE_SHARING_ENABLE, false)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public boolean hasStealth(StealthLvl lvl) {
		return false;
	}

	@Override
	public Vector3f getPosition() {
		return getWorldTransform().origin;
	}

	@Override
	public float getReconStrength(float distance) {
		float viewDistance = 2f * GameCommon.getGameState().getSectorSize();
		return (float) (getReconStrengthRaw() / StructureScannerElementManager.SCAN_FALLOFF_MULTIPLIER * Math.pow((distance/viewDistance), StructureScannerElementManager.SCAN_FALLOFF_EXPONENT));
	}

	@Override
	public boolean canSeeStructure(StealthReconEntity target, boolean inclAllies) {
		if(target.getStealthStrength() <= 0) return true;
		float val = getReconStrength(target.getPosition().getDistance(getWorldTransform().origin)) - target.getStealthStrength();
		if(val > 0) {
			return true;
		} else {
			if(target instanceof ManagedSegmentController<?> segmentController) {
				return !((SegmentController) target).hasStealth(StealthLvl.CLOAKING) || (inclAllies && canAllySeeStructure(segmentController));
			}
			return false;
		}
	}

	@Override
	public boolean canSeeIndicator(StealthReconEntity target, boolean inclAllies) {
		if(target.getStealthStrength() <= 0) return true;
		float val = getReconStrength(target.getPosition().getDistance(getWorldTransform().origin)) - target.getStealthStrength();
		if(val > 0) {
			return true;
		} else {
			if(target instanceof ManagedSegmentController<?> segmentController) {
				return !((SegmentController) target).hasStealth(StealthLvl.JAMMING) || (inclAllies && canAllySeeStructure(segmentController));
			}
			return false;
		}
	}

	public float getReconStrengthRaw() {
		return 0;
	}

	public float getStealthStrength() {
		return 0;
	}

	public Vector3i getSystem(Vector3i out) {
		Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(sectorId);
		if(sendable != null && sendable instanceof RemoteSector) {
			RemoteSector r = (RemoteSector) sendable;
			if(onServer) {
				VoidSystem.getContainingSystem(r.getServerSector().pos, out);
				return out;
			} else {
				VoidSystem.getContainingSystem(r.clientPos(), out);
				return out;
			}
		}
		return null;
	}

	public boolean canSeeReactor(SimpleTransformableSendableObject<?> target, boolean checkAllySharing) {
		float val = getReconStrength(target.getPosition().getDistance(getWorldTransform().origin)) - target.getStealthStrength();
		return val >= VoidElementManager.RECON_DIFFERENCE_MIN_REACTOR || (checkAllySharing && canAllySeeReactor(target)); //TODO: Recon differences must become percentages or something else
	}

	private boolean canAllySeeReactor(SimpleTransformableSendableObject<?> target) {
		FactionManager factionManager = GameCommon.getGameState().getFactionManager();
		for(Sendable sendable : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
			if(sendable instanceof ManagedUsableSegmentController<?> other) {
				if(sendable != this && ((ManagedUsableSegmentController<?>) sendable).railController.isRoot()) {
					if(factionManager.isFriend(other.getFactionId(), getFactionId()) && getFactionId() != 0) {
						if(other.canSeeReactor(target, false) && other.getConfigManager().apply(StatusEffectType.SCAN_SHORT_RANGE_SHARING_ENABLE, false)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public boolean canSeeChambers(SimpleTransformableSendableObject<?> target, boolean checkAllySharing) {
		float val = getReconStrength(target.getPosition().getDistance(getWorldTransform().origin)) - target.getStealthStrength();
		return val >= VoidElementManager.RECON_DIFFERENCE_MIN_CHAMBERS || (checkAllySharing && canAllySeeChambers(target));
	}

	private boolean canAllySeeChambers(SimpleTransformableSendableObject<?> target) {
		FactionManager factionManager = GameCommon.getGameState().getFactionManager();
		for(Sendable sendable : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
			if(sendable instanceof ManagedUsableSegmentController<?> other) {
				if(sendable != this && ((ManagedUsableSegmentController<?>) sendable).railController.isRoot()) {
					if(factionManager.isFriend(other.getFactionId(), getFactionId()) && getFactionId() != 0) {
						if(other.canSeeChambers(target, false) && other.getConfigManager().apply(StatusEffectType.SCAN_SHORT_RANGE_SHARING_ENABLE, false)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public boolean canSeeWeapons(SimpleTransformableSendableObject<?> target, boolean checkAllySharing) {
		float val = getReconStrength(target.getPosition().getDistance(getWorldTransform().origin)) - target.getStealthStrength();
		return val >= VoidElementManager.RECON_DIFFERENCE_MIN_WEAPONS || canAllySeeWeapons(target);
	}

	private boolean canAllySeeWeapons(SimpleTransformableSendableObject<?> target) {
		FactionManager factionManager = GameCommon.getGameState().getFactionManager();
		for(Sendable sendable : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
			if(sendable instanceof ManagedUsableSegmentController<?> other) {
				if(sendable != this && ((ManagedUsableSegmentController<?>) sendable).railController.isRoot()) {
					if(factionManager.isFriend(other.getFactionId(), getFactionId()) && getFactionId() != 0) {
						if(other.canSeeWeapons(target, false) && other.getConfigManager().apply(StatusEffectType.SCAN_SHORT_RANGE_SHARING_ENABLE, false)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public boolean hasAnyReactors() {
		return false;
	}

	public InterEffectContainer getEffectContainer() {
		return effectContainer;
	}

	public abstract CollisionType getCollisionType();

	public boolean isTracked() {
		return tracked;
	}

	public void setTracked(boolean tracked) {
		if(this.tracked != tracked && !onServer) {
			((GameClientState) state).getController().flagTrackingChanged();
		}
		this.tracked = tracked;
	}

	public RType getRelationTo(SimpleTransformableSendableObject<?> e) {
		return ((FactionState) state).getFactionManager().getRelation(this.getFactionId(), e.getFactionId());
	}

	public enum EntityType {
		SHIP(en -> Lng.str("Ship"), true, 5, "ENTITY_SHIP_", 10, TopLevelType.SEGMENT_CONTROLLER),
		SHOP(en -> Lng.str("Shop"), true, 1, "ENTITY_SHOP_", 8, TopLevelType.SEGMENT_CONTROLLER),
		DEATH_STAR(en -> Lng.str("Death Star"), false, -1, "ENTITY_DEATHSTAR_", 0, TopLevelType.SEGMENT_CONTROLLER),
		ASTEROID(en -> Lng.str("Asteroid"), true, 3, "ENTITY_FLOATINGROCK_", 0, TopLevelType.SEGMENT_CONTROLLER),
		ASTRONAUT(en -> Lng.str("Astronaut"), true, -1, "ENTITY_PLAYERCHARACTER_", 0, TopLevelType.ASTRONAUT),
		NPC(en -> Lng.str("NPC"), true, -1, "ENTITY_NPC_", 0, TopLevelType.ASTRONAUT),
		SPACE_STATION(en -> Lng.str("Station"), true, 2, "ENTITY_SPACESTATION_", 9, TopLevelType.SEGMENT_CONTROLLER),
		PLANET_SEGMENT(en -> Lng.str("Planet Segment"), true, 4, "ENTITY_PLANET_", 0, TopLevelType.SEGMENT_CONTROLLER),
		PLANET_CORE(en -> Lng.str("Planet"), true, -1, "ENTITY_PLANETCORE_", 0, TopLevelType.SEGMENT_CONTROLLER),
		BLACK_HOLE(en -> Lng.str("Black Hole"), false, -1, "ENTITY_BLACKHOLE_", 0, TopLevelType.OTHER_SPACE),
		SUN(en -> Lng.str("Sun"), false, -1, "ENTITY_SUN_", 0, TopLevelType.OTHER_SPACE),
		VEHICLE(en -> Lng.str("Vehicle"), false, -1, "ENTITY_VEHICLE_", 0, TopLevelType.SEGMENT_CONTROLLER),
		ASTEROID_MANAGED(en -> Lng.str("Asteroid"), true, 6, "ENTITY_FLOATINGROCKMANAGED_", 0, TopLevelType.SEGMENT_CONTROLLER),
		SPACE_CREATURE(en -> Lng.str("Space Creature"), true, 7, "ENTITY_SPACECREATURE_", 0, TopLevelType.OTHER_SPACE),
		PLANET_ICO(en -> Lng.str("Planet"), true, 8, "ENTITY_PLANETICO_", 0, TopLevelType.SEGMENT_CONTROLLER),
		GAS_PLANET(en -> Lng.str("Gas Giant"), true, 9, "ENTITY_GASPLANET_", 0, TopLevelType.SEGMENT_CONTROLLER);
		public static EntityType[] usedArray;
		public static EntityType[] byDbId;

		static {
			int max = 0;
			for(EntityType t : values()) {
				max = Math.max(max, t.dbTypeId + 1);
			}
			byDbId = new EntityType[max];
			for(EntityType t : values()) {
				if(t.dbTypeId >= 0) {
					byDbId[t.dbTypeId] = t;
				}
			}
		}

		public final boolean used;
		public final int dbTypeId;
		public final String dbPrefix;
		public final int mapSprite;
		public final TopLevelType topLevelType;
		private final Translatable name;

		EntityType(Translatable name, boolean used, int dbTypeId, String dbPrefix, int mapSprite, TopLevelType topLevelType) {
			this.name = name;
			this.used = used;
			this.dbTypeId = dbTypeId;
			this.dbPrefix = dbPrefix;
			this.mapSprite = mapSprite;
			this.topLevelType = topLevelType;
		}

		public static EntityType[] getUsed() {
			if(usedArray == null) {
				ObjectArrayList<EntityType> l = new ObjectArrayList<>();
				for(int i = 0; i < values().length; i++) {
					if(values()[i].used) {
						l.add(values()[i]);
					}
				}
				usedArray = new EntityType[l.size()];
				for(int i = 0; i < usedArray.length; i++) {
					usedArray[i] = l.get(i);
				}
			}
			assert (usedArray != null);
			return usedArray;
		}

		public static EntityType getByDatabaseId(int type) {
			EntityType entityType = byDbId[type];
			if(entityType == null) {
				throw new IllegalArgumentException("Database type doesnt exist: " + type);
			}
			return entityType;
		}

		public final BlueprintClassification getDefaultClassification() {
			switch(this) {
				case ASTEROID:
					return BlueprintClassification.NONE_ASTEROID;
				case ASTEROID_MANAGED:
					return BlueprintClassification.NONE_ASTEROID_MANAGED;
				case PLANET_SEGMENT:
					return BlueprintClassification.NONE_PLANET;
				case SHOP:
					return BlueprintClassification.NONE_SHOP;
				case SPACE_STATION:
					return BlueprintClassification.NONE_STATION;
				case PLANET_ICO:
					return BlueprintClassification.NONE_ICO;
				default:
					break;
			}
			return BlueprintClassification.NONE;
		}

		public String getName() {
			return name.getName(this);
		}
	}

	private static class GenericEffectSet extends InterEffectContainer {
		@Override
		public void update(ConfigEntityManager c) {
		}

		@Override
		public InterEffectSet[] setupEffectSets() {
			InterEffectSet[] s = new InterEffectSet[1];
			for(int i = 0; i < s.length; i++) {
				s[i] = new InterEffectSet();
			}
			return s;
		}

		@Override
		public InterEffectSet get(HitReceiverType type) {
			return sets[0];
		}
	}

	private class TransformTimedSet extends TransformTimed {
		@Override
		public void set(Transform tr) {
			if(!tr.equals(this)) {
				lastChanged = getState().getUpdateTime();
			}
			super.set(tr);
		}
	}
}
