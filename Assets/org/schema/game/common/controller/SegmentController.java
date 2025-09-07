package org.schema.game.common.controller;

import api.common.GameCommon;
import api.listener.events.block.BlockPublicPermissionEvent;
import api.listener.events.block.SegmentPieceAddEvent;
import api.listener.events.block.SegmentPieceDamageEvent;
import api.listener.events.block.SegmentPieceRemoveEvent;
import api.listener.events.entity.SegmentControllerChangeFactionEvent;
import api.listener.events.entity.SegmentControllerFullyLoadedEvent;
import api.listener.events.entity.SegmentControllerInstantiateEvent;
import api.listener.fastevents.FastListenerCommon;
import api.listener.fastevents.segmentpiece.SegmentPieceAddListener;
import api.listener.fastevents.segmentpiece.SegmentPieceRemoveListener;
import api.mod.StarLoader;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.AabbUtil2;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.schema.common.FastMath;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.GameClientController;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.client.controller.element.world.ClientSegmentProvider;
import org.schema.game.client.controller.manager.ingame.*;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.client.view.buildhelper.BuildHelper;
import org.schema.game.client.view.camera.InShipCamera;
import org.schema.game.client.view.camera.SegmentControllerCamera;
import org.schema.game.common.controller.HpTrigger.HpTriggerType;
import org.schema.game.common.controller.ai.AIGameSegmentControllerConfiguration;
import org.schema.game.common.controller.ai.SegmentControllerAIInterface;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.HitReceiverType;
import org.schema.game.common.controller.damage.Hittable;
import org.schema.game.common.controller.damage.effects.InterEffectContainer;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.elements.*;
import org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager.WeaponType;
import org.schema.game.common.controller.elements.armorhp.ArmorHPCollection;
import org.schema.game.common.controller.elements.stealth.StealthAddOn.StealthLvl;
import org.schema.game.common.controller.elements.shipyard.ShipyardCollectionManager;
import org.schema.game.common.controller.elements.stealth.StealthCollectionManager;
import org.schema.game.common.controller.elements.stealth.StealthElementManager;
import org.schema.game.common.controller.elements.stealth.StealthUnit;
import org.schema.game.common.controller.elements.structurescanner.StructureScannerCollectionManager;
import org.schema.game.common.controller.elements.structurescanner.StructureScannerElementManager;
import org.schema.game.common.controller.elements.structurescanner.StructureScannerUnit;
import org.schema.game.common.controller.generator.CreatorThread;
import org.schema.game.common.controller.io.SegmentDataFileUtils;
import org.schema.game.common.controller.io.UniqueIdentifierInterface;
import org.schema.game.common.controller.rails.RailController;
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.game.common.controller.rules.rules.SegmentControllerRuleEntityManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.*;
import org.schema.game.common.data.blockeffects.config.ConfigEntityManager.EffectEntityType;
import org.schema.game.common.data.creature.AICreature;
import org.schema.game.common.data.creature.AIPlayer;
import org.schema.game.common.data.creature.CannotInstantiateAICreatureException;
import org.schema.game.common.data.element.*;
import org.schema.game.common.data.element.ElementInformation.ResourceInjectionType;
import org.schema.game.common.data.element.quarters.QuarterManager;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetStateInterface;
import org.schema.game.common.data.physics.CollisionType;
import org.schema.game.common.data.physics.CubeShape;
import org.schema.game.common.data.physics.CubesCompoundShape;
import org.schema.game.common.data.physics.RigidBodySegmentController;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.PlayerCharacter;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.faction.FactionRoles;
import org.schema.game.common.data.world.*;
import org.schema.game.common.util.Collisionable;
import org.schema.game.network.objects.NetworkSegmentController;
import org.schema.game.network.objects.remote.RemoteSegmentPiece;
import org.schema.game.server.controller.ServerSegmentProvider;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.simulation.npc.geo.NPCEntityContingent.NPCEntitySpecification;
import org.schema.game.server.data.simulation.npc.geo.NPCSystem;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.graphicsengine.forms.DebugBox;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;
import org.schema.schine.graphicsengine.forms.debug.DebugLine;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.TopLevelType;
import org.schema.schine.network.UniqueLongIDInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.network.server.ServerState;
import org.schema.schine.network.server.ServerStateInterface;
import org.schema.schine.physics.Physical;
import org.schema.schine.resource.FileExt;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public abstract class SegmentController extends SimpleTransformableSendableObject<SendableSegmentProvider> implements RuleEntityContainer, UniqueIdentifierInterface, UniqueLongIDInterface, Collisionable, Hittable, ConfigManagerInterface {
	private static final byte TAG_VERSION = 1;
	public byte tagVersion;
	// public static float MASS_PER_BLOCK = 0.1f;
	public static int dockingChecks;
	public long lastEditBlocks;
	public long lastDamageTaken;
	public final RailController railController;
	protected final Vector3b tmpLocalPos = new Vector3b();
	protected final Vector3i posTmp = new Vector3i();
	private final Vector3f centerOfMassUnweighted = new Vector3f();
	private final SegmentControllerHpControllerInterface hpController;
	private final DockingController dockingController;
	private final Vector3iSegment maxPos = new Vector3iSegment();
	private final Vector3iSegment minPos = new Vector3iSegment();
	private final SegmentControllerRuleEntityManager ruleEntityManager;
	private final ElementCountMap elementClassCountMap = new ElementCountMap();
	private final SegmentControllerElementCollisionChecker collisionChecker;
	private final Vector3i testPos = new Vector3i();
	private final Vector3f camPosLocal = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
	private final Vector3f camForwLocal = new Vector3f();
	private final Vector3f camLeftLocal = new Vector3f();
	private final Vector3f camUpLocal = new Vector3f();
	private final ControlElementMap controlElementMap;
	private final SlotAssignment slotAssignment;
	private final ObjectArrayFIFOQueue<SegmentPiece> needsActiveUpdateClient = new ObjectArrayFIFOQueue<SegmentPiece>();
	private final LongArrayList textBlocks = new LongArrayList();
	private final Long2ObjectOpenHashMap<String> textMap = new Long2ObjectOpenHashMap<String>();
	private final Transform clientTransformInverse = new Transform();
	private final Vector3f camLocalTmp = new Vector3f();
	private final ObjectOpenHashSet<ActivationTrigger> triggers = new ObjectOpenHashSet<ActivationTrigger>();
	public boolean flagPhysicsAABBUpdate;
	public CollisionObject stuckFrom;
	public long stuckFromTime;
	public boolean forceSpecialRegion;
	protected float totalPhysicalMass;
	protected long coreTimerStarted = -1;
	protected long coreTimerDuration = -1;
	protected boolean flagUpdateMass;
	private final BlockTypeSearchRunnableManager blockTypeSearchManager;
	private final IntSet proximityObjects = new IntOpenHashSet();
	private boolean scrap;
	private boolean vulnerable = true;
	private boolean minable = true;
	private SegmentProvider segmentProvider;
	private int creatorId;
	private CreatorThread creatorThread;
	private int id = -1234;
	protected String realName = "undef";
	private int totalElements = 0;
	private SegmentBufferInterface segmentBuffer;
	private byte factionRights = (byte) 0;
	private String uniqueIdentifier;
	private long timeCreated;
	private boolean aabbRecalcFlag;
	private boolean flagCheckDocking;
	private boolean flagSegmentBufferAABBUpdate;
	private String spawner = "";
	private String lastModifier = "";
	private long seed;
	private long delayDockingCheck;
	private int lastSector;
	private boolean newlyCreated = true;
	public NPCSystem npcSystem;
	private boolean virtualBlueprint;
	private ConfigEntityManager configManager;
	public float percentageDrawn = 1.0f;
	private boolean spawnedInDatabaseAsChunk16;
	private boolean onFullyLoadedExcecuted;
	public boolean hadAtLeastOneElement;
	private long checkVirtualDock;
	public String blueprintSegmentDataPath;
	public String blueprintIdentifier;
	public ElementCountMap itemsToSpawnWith;
	public PullPermission pullPermission = PullPermission.ASK;
	protected long lastAsked;
	protected long askForPullClient;
	public int oldPowerBlocksFromBlueprint;
	private final QuarterManager quarterManager = new QuarterManager(this);
	public int currentSegmentLoadCount;
	public int totalSegmentCount;

	public static enum PullPermission {
		NEVER(Lng.str("Never"), 0), ASK(Lng.str("Always ask"), 1), FACTION(Lng.str("Always allow faction,\nask for rest"), 2), ALWAYS(Lng.str("Always allow"), 3);
		public final String desc;

		private PullPermission(String desc, int id) {
			this.desc = desc;
		}
	}

	private Vector3i checkSector = new Vector3i();
	private boolean onMassUpdate;

	public boolean isInTestSector() {
		Vector3i sector = getSector(checkSector);
		return sector != null && Sector.isPersonalOrTestSector(sector);
	}

	@Override
	public SegmentControllerRuleEntityManager getRuleEntityManager() {
		return ruleEntityManager;
	}

	private static class SegmentControllerEffectSet extends InterEffectContainer {
		@Override
		public InterEffectSet[] setupEffectSets() {
			InterEffectSet[] s = new InterEffectSet[3];
			for(int i = 0; i < s.length; i++) {
				s[i] = new InterEffectSet();
			}
			// block, armor, shield
			return s;
		}

		@Override
		public InterEffectSet get(HitReceiverType type) {
			if(type != HitReceiverType.BLOCK && type != HitReceiverType.SHIELD && type != HitReceiverType.ARMOR) {
				throw new RuntimeException("illegal hit received " + type.name());
			}
			return type == HitReceiverType.BLOCK ? sets[0] : (type == HitReceiverType.ARMOR ? sets[1] : sets[2]);
		}

		@Override
		public void update(ConfigEntityManager c) {
			update(c, HitReceiverType.BLOCK);
			update(c, HitReceiverType.SHIELD);
			update(c, HitReceiverType.ARMOR);
		}

		private void update(ConfigEntityManager c, HitReceiverType t) {
			final InterEffectSet effectSet = get(t);
			addGeneral(c, effectSet);
			if(t == HitReceiverType.ARMOR) {
				addArmor(c, effectSet);
			} else if(t == HitReceiverType.SHIELD) {
				addShield(c, effectSet);
			}
		}
	}

	@Override
	protected InterEffectContainer setupEffectContainer() {
		return new SegmentControllerEffectSet();
	}

	public SegmentController(StateInterface state) {
		super(state);
		collisionChecker = new SegmentControllerElementCollisionChecker(this);
		if(state instanceof ServerStateInterface) {
			segmentProvider = new ServerSegmentProvider((SendableSegmentController) this);
		} else {
			segmentProvider = new ClientSegmentProvider((SendableSegmentController) this);
		}
		segmentBuffer = new SegmentBufferManager(this);
		controlElementMap = new ControlElementMap();
		railController = new RailController(this);
		blockTypeSearchManager = new BlockTypeSearchRunnableManager(this);
		// getControlElementMap().addObserver(this);
		timeCreated = System.currentTimeMillis();
		this.dockingController = new DockingController(this);
		this.slotAssignment = new SlotAssignment((SendableSegmentController) this);
		this.hpController = new SegmentControllerHpController((SendableSegmentController) this);
		ruleEntityManager = new SegmentControllerRuleEntityManager(this);
		//INSERTED CODE
		SegmentControllerInstantiateEvent event = new SegmentControllerInstantiateEvent(this);
		StarLoader.fireEvent(event, state instanceof ServerState);
		///
	}

	@Override
	public SimpleTransformableSendableObject<?> getShootingEntity() {
		return this;
	}

	public float getDamageTakenMultiplier(DamageDealerType damageType) {
		float effectVal = configManager.apply(StatusEffectType.DAMAGE_TAKEN, damageType, 1f) - 1f;
		return 1f + effectVal;
	}

	@Override
	public float getDamageGivenMultiplier() {
		return 1;
	}

	public abstract void onDamageServerRootObject(float actualDamage, Damager from);

	public void onBlockDamage(long pos, short type, int damage, DamageDealerType damageType, Damager from) {
		//INSERTED CODE
		SegmentPieceDamageEvent ev = new SegmentPieceDamageEvent(this, pos, type, damage, damageType, from);
		StarLoader.fireEvent(ev, isOnServer());
		damage = ev.getDamage();
		if(ev.isCanceled()) {
			damage = 0;
		}

		///
		if(isOnServer()) {
			railController.getRoot().onDamageServerRootObject(damage, from);
			onAnyDamageTakenServer(damage, from, damageType);
		}
		if(this instanceof ManagedSegmentController<?>) {
			((ManagedSegmentController<?>) this).getManagerContainer().onBlockDamage(pos, type, damage, damageType, from);
		}
	}

	public abstract void onBlockKill(SegmentPiece piece, Damager from);

	@Override
	public String getObfuscationString() {
		if(!isOnServer()) {
			return ((GameClientState) getState()).getController().getConnection().getHost();
		} else {
			return null;
		}
	}

	//INSERTED CODE
	public void setCoreTimerDuration(long coreTimerDuration) {
		this.coreTimerDuration = coreTimerDuration;
	}

	public static void setConstraintFrameOrientation(byte orientation, Transform frame, Vector3f right, Vector3f up, Vector3f forward) {
		// System.err.println("ORIENTATION GIVEN "+orientation);
		switch(orientation) {
			case (Element.BACK) -> {
				frame.basis.setRow(0, -right.x, -up.x, -forward.x);
				frame.basis.setRow(1, -right.y, -up.y, -forward.y);
				frame.basis.setRow(2, -right.z, -up.z, -forward.z);
			}
			case (Element.RIGHT) -> {
				frame.basis.setRow(0, -forward.x, -up.x, -right.x);
				frame.basis.setRow(1, -forward.y, -up.y, -right.y);
				frame.basis.setRow(2, -forward.z, -up.z, -right.z);
			}
			case (Element.LEFT) -> {
				frame.basis.setRow(0, forward.x, -up.x, right.x);
				frame.basis.setRow(1, forward.y, -up.y, right.y);
				frame.basis.setRow(2, forward.z, -up.z, right.z);
			}
			case (Element.TOP) -> {
				frame.basis.setRow(0, forward.x, right.x, up.x);
				frame.basis.setRow(1, forward.y, right.y, up.y);
				frame.basis.setRow(2, forward.z, right.z, up.z);
			}
			case (Element.BOTTOM) -> {
				frame.basis.setRow(0, -forward.x, right.x, -up.x);
				frame.basis.setRow(1, -forward.y, right.y, -up.y);
				frame.basis.setRow(2, -forward.z, right.z, -up.z);
			}
			default -> {
				frame.basis.setRow(0, right.x, up.x, forward.x);
				frame.basis.setRow(1, right.y, up.y, forward.y);
				frame.basis.setRow(2, right.z, up.z, forward.z);
			}
		}
	}

	public static boolean isPublicException(SegmentPiece dockingTarget, int forFactionId) {
		if(dockingTarget != null) {
			SegmentController seg = dockingTarget.getSegmentController();
			Vector3i pos = new Vector3i();
			dockingTarget.getAbsolutePos(pos);
			if(dockingTarget.getType() == ElementKeyMap.SHIPYARD_CORE_POSITION) {
				ShipyardManagerContainerInterface s = (ShipyardManagerContainerInterface) (((ManagedSegmentController<?>) seg).getManagerContainer());
				for(ShipyardCollectionManager c : s.getShipyard().getCollectionManagers()) {
					if(c.getConnectedCorePositionBlock4() == dockingTarget.getAbsoluteIndexWithType4()) {
						// check the shipyard instad of the docing position
						if(c.isPublicException(forFactionId)) {
							return true;
						}
					}
				}
			}
			if(isBlockPublicException(seg, pos, forFactionId)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isBlockPublicException(SegmentController seg, Vector3i pos, int forFactionId) {
		Vector3i posDir = new Vector3i();
		boolean result = false;
		for(int i = 0; i < 6; i++) {
			posDir.add(pos, Element.DIRECTIONSi[i]);
			SegmentPiece pointUnsave;
			// autorequest true previously
			pointUnsave = seg.segmentBuffer.getPointUnsave(posDir);
			if(pointUnsave == null) {
				// mark as public as long as it's not loaded. Cannot be abused since, well, the block isn't here yet to abuse
				result = true;
				break;
			}
			if(pointUnsave.getType() == ElementKeyMap.FACTION_PUBLIC_EXCEPTION_ID || (pointUnsave.getType() == ElementKeyMap.FACTION_FACTION_EXCEPTION_ID && seg.getFactionId() == forFactionId)) {
				result = true;
				break;
			}
		}
		BlockPublicPermissionEvent ev = new BlockPublicPermissionEvent(seg, pos, forFactionId, result);
		StarLoader.fireEvent(ev, seg.isOnServer());
		result = ev.getPermission();
		return result;
	}

	/**
	 * @return the scrap
	 */
	public boolean isScrap() {
		return scrap;
	}

	/**
	 * @param scrap the scrap to set
	 */
	public void setScrap(boolean scrap) {
		this.scrap = scrap;
	}

	public float getMassWithoutDockIncludingStation() {
		double invMass = (this instanceof ManagedSegmentController<?> ? ((ManagedSegmentController<?>) this).getManagerContainer().getMassFromInventories() : 0);
		return (float) (getTotalPhysicalMass() + invMass);
	}

	public float getMassWithDocks() {
		double invMass = (this instanceof ManagedSegmentController<?> ? ((ManagedSegmentController<?>) this).getManagerContainer().getMassFromInventories() : 0);
		return (float) (railController.calculateRailMassIncludingSelf() + invMass);
	}

	public SegmentController getInterdictingEntity() {
		//Todo: Check for unloaded interdictors
		for(Sendable object : getState().getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
			if(object instanceof ManagedUsableSegmentController<?> entity) {
				if(entity.getSectorId() == getSectorId() && !entity.equals(this) && !entity.railController.getRoot().equals(railController.getRoot())) {
					if(Objects.requireNonNull(GameCommon.getGameState()).getFactionManager().isEnemy(entity.getFactionId(), getFactionId())) {
						if(entity.getManagerContainer().getInterdictionAddOn().isActive()) return entity;
					}
				}
			}
		}
		return null;
	}

	public void sendHitConfirmToDamager(Damager from, boolean shields) {
		if(from != null) {
			if(from.isSegmentController() || !(this instanceof Planet || this instanceof PlanetIco || this instanceof SpaceStation)) {
				from.sendHitConfirm(shields ? Damager.SHIELD : Damager.BLOCK);
			}
		}
	}

	public boolean isIgnorePhysics() {
		return dockingController.getDelayedDock() != null;
	}

	public boolean isFullyLoaded() {
		return segmentBuffer.isFullyLoaded();
	}

	public boolean isFullyLoadedWithDock() {
		return railController.getRoot().railController.isFullyLoadedRecursive();
	}

	protected boolean canObjectOverlap(Physical sendable) {
		return true;
	}

	@Override
	public int getMiningBonus(SimpleTransformableSendableObject<?> forTarget) {
		return configManager.apply(StatusEffectType.MINING_BONUS_ACTIVE, super.getMiningBonus(forTarget));
	}

	public boolean isOverlapping() {
		Vector3f min = new Vector3f();
		Vector3f max = new Vector3f();
		Vector3f minOut = new Vector3f();
		Vector3f maxOut = new Vector3f();
		Vector3f minOutOther = new Vector3f();
		Vector3f maxOutOther = new Vector3f();
		min.set(this.minPos.x * SegmentData.SEG, this.minPos.y * SegmentData.SEG, this.minPos.z * SegmentData.SEG);
		max.set(this.maxPos.x * SegmentData.SEG, this.maxPos.y * SegmentData.SEG, this.maxPos.z * SegmentData.SEG);
		// System.err.println(min+"; "+bb+": "+c);
		AabbUtil2.transformAabb(min, max, 100 * Element.BLOCK_SIZE, this.getWorldTransform(), minOut, maxOut);
		for(Sendable s : getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
			if(s instanceof Physical && s != this) {
				if(s instanceof SimpleTransformableSendableObject && ((SimpleTransformableSendableObject) s).getSectorId() != this.getSectorId()) {
					continue;
				}
				Physical p = ((Physical) s);
				if(!canObjectOverlap(p)) {
					continue;
				}
				// System.err.println("[Sector][LocalCollisionCheck] "+p+":: "+p.getPhysicsDataContainer()+"; "+p.getPhysicsDataContainer().getShape());
				p.getPhysicsDataContainer().getShape().getAabb(p.getPhysicsDataContainer().getCurrentPhysicsTransform(), minOutOther, maxOutOther);
				if(AabbUtil2.testAabbAgainstAabb2(minOut, maxOut, minOutOther, maxOutOther)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isCoreOverheating() {
		return coreTimerStarted > 0;
	}

	public long getCoreOverheatingTimeLeftMS(long timeNow) {
		long timeCoreTimerRunning = timeNow - coreTimerStarted;
		long timeLeft = coreTimerDuration - timeCoreTimerRunning;
		return timeLeft;
	}

	public void stopCoreOverheating() {
		if(coreTimerStarted != -1 || coreTimerDuration != -1) {
			System.err.println(getState() + " " + this + " STOPPED OVERHEATING");
		}
		coreTimerStarted = -1;
		coreTimerDuration = -1;
	}

	/**
	 * @return the coreTimerDuration
	 */
	public long getCoreTimerDuration() {
		return coreTimerDuration;
	}

	/**
	 * @return the coreTimerStarted
	 */
	public long getCoreTimerStarted() {
		return coreTimerStarted;
	}

	public void startCoreOverheating(Damager from) {
		if(isOnServer()) {
			if(coreTimerStarted < 0) {
				Faction faction = getFaction();
				if(faction != null) {
					faction.onEntityOverheatingServer(this);
				}
				this.coreTimerStarted = System.currentTimeMillis();
				this.coreTimerDuration = 1000 * Math.min(VoidElementManager.OVERHEAT_TIMER_MAX, Math.max(VoidElementManager.OVERHEAT_TIMER_MIN + (long) (totalElements * VoidElementManager.OVERHEAT_TIMER_ADDED_PER_BLOCK), VoidElementManager.OVERHEAT_TIMER_MIN));
				kickAllPlayersOutServer();
				if(this instanceof SegmentControllerAIInterface) {
					((AIGameSegmentControllerConfiguration) ((SegmentControllerAIInterface) this).getAiConfiguration()).onStartOverheating(from);
				}
				System.err.println("[SERVER] MAIN CORE STARTED DESTRUCTION [" + this.uniqueIdentifier + "] " + this.getSector(new Vector3i()) + " in " + coreTimerDuration / 1000 + " seconds - started " + coreTimerStarted + " caused by " + (from != null ? from.getOwnerState() : ""));
			} else {
				// put core destruction timer penalty here if decided to do so
			}
			railController.resetFactionForEntitiesWithoutFactionBlock(getFactionId());
		}
	}

	public boolean isHandleHpCondition(HpTriggerType type) {
		return false;
	}

	public boolean hasStructureAndArmorHP() {
		return true;
	}

	public void aabbRecalcFlag() {
		aabbRecalcFlag = true;
	}

	public abstract boolean allowedToEdit(PlayerState player);

	@Override
	public void createConstraint(Physical a, Physical b, Object userData) {
	}

	@Override
	public void getTransformedAABB(Vector3f oMi, Vector3f oMa, float margin, Vector3f tmpMin, Vector3f tmpMax, Transform instead) {
		tmpMin.set(segmentBuffer.getBoundingBox().min);
		tmpMax.set(segmentBuffer.getBoundingBox().max);
		if(segmentBuffer.getTotalNonEmptySize() == 0) {
			tmpMin.set(0, 0, 0);
			tmpMax.set(0, 0, 0);
		} else {
			if(tmpMin.x > tmpMax.x || tmpMin.y > tmpMax.y || tmpMin.z > tmpMax.z) {
				tmpMin.set(0, 0, 0);
				tmpMax.set(0, 0, 0);
			}
			if(instead == null) {
				AabbUtil2.transformAabb(tmpMin, tmpMax, margin, getWorldTransform(), oMi, oMa);
			} else {
				AabbUtil2.transformAabb(tmpMin, tmpMax, margin, instead, oMi, oMa);
			}
		}
	}

	public void checkInitialPositionServer(Transform t) {
	}

	public void avoid(Transform initial, boolean useInitalWorldTransformForOtherEntities) {
		List<SimpleTransformableSendableObject<?>> secObjs = new ObjectArrayList<SimpleTransformableSendableObject<?>>();
		for(Sendable see : getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
			if(see instanceof SimpleTransformableSendableObject<?>) {
				SimpleTransformableSendableObject<?> s = (SimpleTransformableSendableObject<?>) see;
				if(s != this && s.getSectorId() == this.getSectorId()) {
					secObjs.add(s);
				}
			}
		}
		Vector3i posToCheck = new Vector3i();
		posToCheck.set((int) initial.origin.x, (int) initial.origin.y, (int) initial.origin.z);
		int move = 30;
		while(checkCollision(this, initial, useInitalWorldTransformForOtherEntities, secObjs, posToCheck) != null) {
			if(Universe.getRandom().nextBoolean()) {
				posToCheck.x += (Universe.getRandom().nextBoolean() ? move : -move);
			}
			if(Universe.getRandom().nextBoolean()) {
				posToCheck.y += (Universe.getRandom().nextBoolean() ? move : -move);
			}
			if(Universe.getRandom().nextBoolean()) {
				posToCheck.z += (Universe.getRandom().nextBoolean() ? move : -move);
			}
			move += 30;
		}
		System.err.println("[SERVER][SHIP] Collision Avoidance for: " + this + ": " + posToCheck);
	}

	private Sendable checkCollision(SegmentController c, Transform t, boolean otherInitial, List<SimpleTransformableSendableObject<?>> secObjs, Vector3i posToCheck) {
		long time = System.currentTimeMillis();
		Vector3f min = new Vector3f();
		Vector3f max = new Vector3f();
		Vector3f minOutOther = new Vector3f();
		Vector3f maxOutOther = new Vector3f();
		Vector3f minOut = new Vector3f();
		Vector3f maxOut = new Vector3f();
		t.setIdentity();
		t.origin.set(posToCheck.x, posToCheck.y, posToCheck.z);
		min.set((c.minPos.x - 2) * SegmentData.SEG, (c.minPos.y - 2) * SegmentData.SEG, (c.minPos.z - 2) * SegmentData.SEG);
		max.set((c.maxPos.x + 2) * SegmentData.SEG, (c.maxPos.y + 2) * SegmentData.SEG, (c.maxPos.z + 2) * SegmentData.SEG);
		// System.err.println(min+"; "+bb+": "+c);
		AabbUtil2.transformAabb(min, max, 100, t, minOut, maxOut);
		for(SimpleTransformableSendableObject<?> s : secObjs) {
			if(s instanceof Physical) {
				if(s instanceof SegmentController) {
					SegmentController cc = (SegmentController) s;
					minOutOther.set((cc.minPos.x - 2) * SegmentData.SEG, (cc.minPos.y - 2) * SegmentData.SEG, (cc.minPos.z - 2) * SegmentData.SEG);
					maxOutOther.set((cc.maxPos.x + 2) * SegmentData.SEG, (cc.maxPos.y + 2) * SegmentData.SEG, (cc.maxPos.z + 2) * SegmentData.SEG);
					Transform oT = otherInitial ? cc.getInitialTransform() : cc.getWorldTransform();
					AabbUtil2.transformAabb(minOutOther, maxOutOther, 100 * Element.BLOCK_SIZE, oT, minOutOther, maxOutOther);
				} else {
					Physical p = (s);
					Transform oT = otherInitial ? p.getInitialTransform() : p.getPhysicsDataContainer().getCurrentPhysicsTransform();
					p.getPhysicsDataContainer().getShape().getAabb(p.getInitialTransform(), minOutOther, maxOutOther);
				}
				if(AabbUtil2.testAabbAgainstAabb2(minOut, maxOut, minOutOther, maxOutOther)) {
					long took = (System.currentTimeMillis() - time);
					if(took > 10) {
						System.err.println("[Sector] [Sector] collision test at " + posToCheck + " is true: trying another pos " + took + "ms");
					}
					return s;
				}
			}
		}
		long took = (System.currentTimeMillis() - time);
		if(took > 10) {
			System.err.println("[Sector] No Collission: " + took + "ms");
		}
		return null;
	}

	@Override
	public void initPhysics() {
		if(getPhysicsDataContainer().getObject() == null) {
			Transform t = getRemoteTransformable().getInitialTransform();
			if(isOnServer() && needsPositionCheckOnLoad) {
				checkInitialPositionServer(t);
			}
			needsPositionCheckOnLoad = false;
			// lastTransform.set(t);
			CubeShape sShape = new CubeShape(segmentBuffer);
			CubesCompoundShape root = new CubesCompoundShape(this);
			Transform local = new Transform();
			local.setIdentity();
			root.addChildShape(local, sShape);
			int index = root.getChildList().size() - 1;
			getPhysicsDataContainer().setShapeChield(root.getChildList().get(index), index);
			root.recalculateLocalAabb();
			getPhysicsDataContainer().setShape(root);
			getPhysicsDataContainer().setInitial(t);
			RigidBody bodyFromShape = getPhysics().getBodyFromShape(root, getMass(), getPhysicsDataContainer().initialTransform);
			// System.err.println("[ENTITY] "+getState()+" "+this+" initialized physics -> "+t.origin);
			bodyFromShape.setUserPointer(this.id);
			getPhysicsDataContainer().setObject(bodyFromShape);
			getWorldTransform().set(t);
			assert (getPhysicsDataContainer().getObject() != null);
		} else {
			System.err.println("[SegmentController][WARNING] not adding initial physics object. it already exists");
		}
		setFlagPhysicsInit(true);
	}

	public void decTotalElements() {
		setTotalElements(totalElements - 1);
	}

	private List<String> getDataFilesListOld() {
		final String fil = uniqueIdentifier + ".";
		FilenameFilter filter = (dir, name) -> name.startsWith(fil);
		File dir = new FileExt(GameServerState.SEGMENT_DATA_DATABASE_PATH);
		String[] list = dir.list(filter);
		List<String> l = new ObjectArrayList<String>();
		for(int i = 0; i < list.length; i++) {
			l.add(list[i]);
		}
		return l;
	}

	private List<String> getFileNames(List<String> la) {
		List<String> l = new ObjectArrayList<String>();
		for(String s : la) {
			l.add((new FileExt(s)).getName());
		}
		return l;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.objects.Sendable#destroyPersistent()
	 */
	@Override
	public void destroyPersistent() {
		assert (isOnServer());
		GameServerState state = (GameServerState) this.getState();
		final String path = GameServerState.ENTITY_DATABASE_PATH + uniqueIdentifier;
		try {
			state.getDatabaseIndex().getTableManager().getEntityTable().removeSegmentController(this);
			File entity = new FileExt(GameServerState.ENTITY_DATABASE_PATH + uniqueIdentifier + ".ent");
			System.err.println("[SERVER][SEGMENTCONTROLLER] PERMANENTLY DELETING ENTITY: " + entity.getName());
			entity.delete();
			List<String> allFiles = SegmentDataFileUtils.getAllFiles(minPos, maxPos, uniqueIdentifier, this);
			assert (getFileNames(allFiles).containsAll(getDataFilesListOld())) : minPos + "; " + maxPos + "\n" + getFileNames(allFiles) + ";\n\n" + getDataFilesListOld();
			for(String s : allFiles) {
				File f = new FileExt(s);
				if(f.exists()) {
					System.err.println("[SERVER][SEGMENTCONTROLLER] PERMANENTLY DELETING ENTITY DATA: " + f.getName() + " (exists: " + f.exists() + ")");
					f.delete();
				}
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean existsNeighborSegment(Vector3i pos, int dir) {
		getNeighborSegmentPos(pos, dir, testPos);
		return segmentBuffer.containsKey(testPos);
	}

	public final void flagUpdateDocking() {
		flagCheckDocking = true;
	}

	public final void flagupdateMass() {
		// if(GameServerState.debugObj == getId()){
		// if(!flagUpdateMass){
		// try{
		// throw new IllegalArgumentException("MASS SET DEBUG");
		// }catch(Exception e){
		// e.printStackTrace();
		// }
		// }
		if(dockingController.isDocked()) {
			dockingController.getDockedOn().to.getSegment().getSegmentController().flagupdateMass();
		}
		if(railController.isDockedAndExecuted()) {
			railController.getRoot().flagupdateMass();
		}
		// }
		flagUpdateMass = true;
		onMassUpdate = true;
	}

	public boolean isSegmentBufferFullyLoadedServer() {
		assert (isOnServer());
		return ((SegmentBufferManager) segmentBuffer).isFullyLoaded();
	}

	protected boolean fullyLoadedRailRecChache = false;

	public boolean isSegmentBufferFullyLoadedServerRailRec() {
		assert (isOnServer());
		if(!fullyLoadedRailRecChache) {
			// must be fully loaded and no dock waiting (timeout after 30 sec)
			boolean loadSelf = ((SegmentBufferManager) segmentBuffer).isFullyLoaded() && (getState().getUpdateTime() - timeCreated > 30000 || railController.getExpectedToDock().isEmpty());
			if(loadSelf) {
				for(RailRelation r : railController.next) {
					if(!r.docked.getSegmentController().isSegmentBufferFullyLoadedServerRailRec()) {
						return false;
					}
				}
				fullyLoadedRailRecChache = true;
				return true;
			}
			return false;
		} else {
			return true;
		}
	}

	public void readTextBlockData(Tag tag) {
		Tag[] v2 = (Tag[]) tag.getValue();
		for(int i = 0; i < v2.length - 1; i++) {
			Tag[] k0 = (Tag[]) v2[i].getValue();
			long pos = (Long) k0[0].getValue();
			String text = (String) k0[1].getValue();
			if(loadedFromChunk16) {
				pos = ElementCollection.shiftIndex4(pos, Chunk16SegmentData.SHIFT_, Chunk16SegmentData.SHIFT_, Chunk16SegmentData.SHIFT_);
				// System.err.println("LOADED FROM CHUNK16 ::::: "+this+": "+ElementCollection.getPosFromIndex(pos, new Vector3i())+":: "+text);
			}
			textMap.put(pos, text);
		}
	}

	private Tag getNPCTagData() {
		ObjectArrayList<AICreature<? extends AIPlayer>> aff = new ObjectArrayList<AICreature<? extends AIPlayer>>();
		// try{
		// throw new NullPointerException();
		// }catch(NullPointerException e){
		// e.printStackTrace();
		// }
		aff.addAll(getAttachedAffinity());
		Tag[] t = new Tag[aff.size() + 1];
		t[aff.size()] = FinishTag.INST;
		for(int i = 0; i < aff.size(); i++) {
			assert (aff.get(i) != null);
			// System.err.println("SAVING NPC: "+aff.get(i)+"; "+aff.get(i).getAffinity()+" on "+getUniqueIdentifier());
			t[i] = AICreature.toTagNPC(aff.get(i));
		}
		Tag tag = new Tag(Type.STRUCT, aff.toString(), t);
		// Tag quartersTag = quarterManager.toTagStructure();
		//Tag[] t2 = new Tag[2];
		//t2[0] = tag;
		//t2[1] = quartersTag;
		//return new Tag(Type.STRUCT, "NPC TAG " + getUniqueIdentifier(), t2);
		//		System.err.println("NPC TAG "+getUniqueIdentifier()+": "+tag.getName());
		return tag;
	}

	private void readNPCData(Tag tag) {
		Tag[] t = (Tag[]) tag.getValue();
		// System.err.println("TAG DESERIALIZING: "+getUniqueIdentifier()+"; AttachedAI: "+(t.length-1)+"; "+tag.getName());
		if(t.length > 1) {
			// ignore FINISH tag//if(t.length == 2 && t[1] != null && t[1].getName().startsWith("NPC TAG")) quarterManager.fromTagStructure(t[1]);
			//else t = (Tag[]) t[0].getValue();
			for(int i = 0; i < t.length - 1; i++) {
				try {
					AICreature<? extends AIPlayer> npcFromTag = AICreature.getNPCFromTag(t[i], getState());
					attachedAffinityInitial.enqueue(npcFromTag);
					// System.err.println("[TAG][REAF] ATTACHED NEW AFFINITY FOR "+this+" "+npcFromTag);
				} catch(CannotInstantiateAICreatureException e) {
					e.printStackTrace();
				}
			}
			// System.err.println("[ATTACHED] ADDED ATTACHED AFFINITY FOR "+this+": "+attachedAffinityInitial);
		}
	}

	public Vector3f getAbsoluteElementWorldPosition(Vector3i v, Vector3f out) {
		if(isOnServer()) {
			getAbsoluteElementWorldPositionLocal(v, out);
		} else {
			out.set(v.x, v.y, v.z);
			getWorldTransformOnClient().basis.transform(out);
			out.add(getWorldTransformOnClient().origin);
		}
		return out;
	}

	public Vector3f getAbsoluteElementWorldPositionShifted(Vector3i v, Vector3f out) {
		if(isOnServer()) {
			getAbsoluteElementWorldPositionLocalShifted(v, out);
		} else {
			out.set(v.x - SegmentData.SEG_HALF, v.y - SegmentData.SEG_HALF, v.z - SegmentData.SEG_HALF);
			getWorldTransformOnClient().basis.transform(out);
			out.add(getWorldTransformOnClient().origin);
		}
		return out;
	}

	public Vector3f getAbsoluteElementWorldPositionLocal(Vector3i v, Vector3f out) {
		out.set(v.x, v.y, v.z);
		getWorldTransform().basis.transform(out);
		out.add(getWorldTransform().origin);
		return out;
	}

	public Vector3f getAbsoluteElementWorldPositionLocalShifted(Vector3i v, Vector3f out) {
		out.set(v.x - SegmentData.SEG_HALF, v.y - SegmentData.SEG_HALF, v.z - SegmentData.SEG_HALF);
		getWorldTransform().basis.transform(out);
		out.add(getWorldTransform().origin);
		return out;
	}

	public Vector3f getAbsoluteElementWorldPositionShifted(int x, int y, int z, int sectorId, Vector3f out) {
		if(isOnServer()) {
			getAbsoluteElementWorldPositionLocalShifted(x, y, z, sectorId, out);
		} else {
			out.set(x - SegmentData.SEG_HALF, y - SegmentData.SEG_HALF, z - SegmentData.SEG_HALF);
			getWorldTransformOnClient().basis.transform(out);
			out.add(getWorldTransformOnClient().origin);
		}
		return out;
	}

	public Vector3f getAbsoluteElementWorldPositionLocalShifted(int x, int y, int z, int sectorId, Vector3f out) {
		assert (isOnServer());
		out.set(x - SegmentData.SEG_HALF, y - SegmentData.SEG_HALF, z - SegmentData.SEG_HALF);
		getWorldTransform().basis.transform(out);
		out.add(getWorldTransform().origin);
		Sector s = ((GameServerState) getState()).getUniverse().getSector(sectorId);
		if(s != null) {
			v.inT.origin.set(out);
			SimpleTransformableSendableObject.calcWorldTransformRelative(s.getId(), s.pos, getSectorId(), v.inT, getState(), true, v.outT, v);
			out.set(v.outT.origin);
		}
		return out;
	}

	public Vector3f getAbsoluteElementWorldPosition(int x, int y, int z, Vector3f out) {
		if(isOnServer()) {
			getAbsoluteElementWorldPositionLocal(x, y, z, out);
		} else {
			out.set(x, y, z);
			getWorldTransformOnClient().basis.transform(out);
			out.add(getWorldTransformOnClient().origin);
		}
		return out;
	}

	public Vector3f getAbsoluteElementWorldPositionLocal(int x, int y, int z, Vector3f out) {
		out.set(x, y, z);
		getWorldTransform().basis.transform(out);
		out.add(getWorldTransform().origin);
		return out;
	}

	/**
	 * this will NOT subtract 8, as it will return the middle of the segment
	 *
	 * @param e
	 * @param out
	 */
	public void getAbsoluteSegmentWorldPositionClient(Segment e, Vector3f out) {
		out.set(e.pos.x, e.pos.y, e.pos.z);
		Transform worldTransformClient = getWorldTransformOnClient();
		worldTransformClient.basis.transform(out);
		out.add(worldTransformClient.origin);
	}

	public BoundingBox getBoundingBox() {
		return segmentBuffer.getBoundingBox();
	}

	public Vector3f getCamForwLocal() {
		return camForwLocal;
	}

	public Vector3f getCamLeftLocal() {
		return camLeftLocal;
	}

	public Vector3f getCamUpLocal() {
		return camUpLocal;
	}

	/**
	 * @return the clientTransformInverse
	 */
	public Transform getClientTransformInverse() {
		return clientTransformInverse;
	}
	// public Vector3f getAbsoluteElementWorldPositionClient(Vector3i v, Vector3f out) {
	// out.set(v.x , v.y , v.z );
	// getWorldTransformOnClient().basis.transform(out);
	// out.add(getWorldTransformOnClient().origin);
	// return out;
	// }
	// public Vector3f getAbsoluteElementWorldPositionClientShifted(Vector3i v, Vector3f out) {
	// out.set(v.x-8 , v.y-8 , v.z-8 );
	// getWorldTransformOnClient().basis.transform(out);
	// out.add(getWorldTransformOnClient().origin);
	// return out;
	// }

	/**
	 * @return the collisionChecker
	 */
	public SegmentControllerElementCollisionChecker getCollisionChecker() {
		return collisionChecker;
	}

	/**
	 * @return the controlElementMap
	 */
	public ControlElementMap getControlElementMap() {
		return controlElementMap;
	}

	public int getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(int id) {
		if(id != creatorId) {
			setChangedForDb(true);
		}
		creatorId = id;
	}

	/**
	 * @return the creatorThread
	 */
	public CreatorThread getCreatorThread() {
		return creatorThread;
	}

	/**
	 * @param creatorThread the creatorThread to set
	 */
	public void setCreatorThread(CreatorThread creatorThread) {
		this.creatorThread = creatorThread;
	}

	/**
	 * @return the dockingController
	 */
	public DockingController getDockingController() {
		return dockingController;
	}

	/**
	 * @return the elementClassCountMap
	 */
	public ElementCountMap getElementClassCountMap() {
		return elementClassCountMap;
	}

	protected Tag getExtraTagData() {
		return new Tag(Type.BYTE, null, (byte) 0);
	}

	/**
	 * @return the lastModifier
	 */
	public String getLastModifier() {
		return lastModifier;
	}

	/**
	 * @param lastModifier the lastModifier to set
	 */
	public void setLastModifier(String lastModifier) {
		if(!lastModifier.equals(this.lastModifier)) {
			setChangedForDb(true);
		}
		this.lastModifier = lastModifier;
	}

	public Vector3f getLocalCamPos() {
		return camPosLocal;
	}

	public Vector3iSegment getMaxPos() {
		return maxPos;
	}

	/**
	 * @return the worldBaseMesh
	 */
	// public CubeMesh getWorldBaseMesh() {
	// return worldBaseMesh;
	// }
	// /**
	// * @return the segmentDrawer
	// */
	// public SegmentDrawer getSegmentDrawer() {
	// return segmentDrawer;
	// }
	public Vector3iSegment getMinPos() {
		return minPos;
	}

	public abstract void getNearestIntersectingElementPosition(Vector3f fromRay, Vector3f toRay, Vector3i size, float editDistance, BuildRemoveCallback callback, SymmetryPlanes symmetryPlanes, short filter, short replaceFilterWith, BuildHelper posesFilter, BuildInstruction buildInstruction, Set<Segment> moddedSegs) throws IOException, InterruptedException;

	public abstract int getNearestIntersection(short type, Vector3f fromRay, Vector3f toRay, BuildCallback callback, int elementOrientation, boolean activateBlock, DimensionFilter filter, Vector3i size, int count, float editDistance, SymmetryPlanes symmetryPlanes, BuildHelper posesFilter, BuildInstruction buildInstruction) throws ElementPositionBlockedException, BlockedByDockedElementException, BlockNotBuildTooFast;

	/**
	 * @return the needsActiveUpdateClient
	 */
	public ObjectArrayFIFOQueue<SegmentPiece> getNeedsActiveUpdateClient() {
		return needsActiveUpdateClient;
	}

	public SegmentPiece[] getNeighborElements(final Vector3i absPos, final short typeFilter, final SegmentPiece[] out) throws IOException, InterruptedException {
		assert (out.length == 6);
		for(int i = 0; i < 6; i++) {
			posTmp.set(absPos);
			posTmp.add(Element.DIRECTIONSi[i]);
			// autorequest true previously
			SegmentPiece pointUnsave = segmentBuffer.getPointUnsave(posTmp);
			if(pointUnsave == null) {
				return null;
			}
			if((typeFilter == Element.TYPE_ALL || typeFilter == pointUnsave.getType())) {
				out[i] = pointUnsave;
			} else {
				out[i] = null;
			}
		}
		return out;
	}

	/**
	 * Return the segment in relation to the 'from' segment.
	 * e.g. -1,0,0 will return the segment left of 'from' and position 15,0,0
	 *
	 * @param inOut
	 * @param from
	 *
	 * @return the segment and position the in position is
	 */
	public Segment getNeighboringSegment(Vector3b inOut, Segment from, Vector3i out) {
		assert (from != null) : this + ", " + this.getState() + " has null seg";
		out.set(from.pos);
		if(SegmentData.valid(inOut.x, inOut.y, inOut.z)) {
			// this position is inside the segment
			// so return this segment
			return from;
		}
		int x = ByteUtil.divUSeg(inOut.x);
		int y = ByteUtil.divUSeg(inOut.y);
		int z = ByteUtil.divUSeg(inOut.z);
		out.add(x * SegmentData.SEG, y * SegmentData.SEG, z * SegmentData.SEG);
		inOut.x = (byte) ByteUtil.modUSeg(inOut.x);
		inOut.y = (byte) ByteUtil.modUSeg(inOut.y);
		inOut.z = (byte) ByteUtil.modUSeg(inOut.z);
		if(segmentBuffer.getSegmentState(out) >= 0) {
			Segment segment = segmentBuffer.get(out);
			return segment;
		} else {
			return null;
		}
	}

	/**
	 * Return the segment in relation to the 'from' segment.
	 * e.g. -1,0,0 will return the segment left of 'from' and position 15,0,0
	 *
	 * @param from
	 *
	 * @return the segment and position the in position is
	 */
	public Segment getNeighboringSegmentFast(Segment from, byte xIn, byte yIn, byte zIn) {
		assert (from != null) : this + ", " + this.getState() + " has null seg";
		if(SegmentData.valid(xIn, yIn, zIn)) {
			// this position is inside the segment
			// so return this segment
			return from;
		}
		int x = from.pos.x + ByteUtil.divUSeg(xIn) * SegmentData.SEG;
		int y = from.pos.y + ByteUtil.divUSeg(yIn) * SegmentData.SEG;
		int z = from.pos.z + ByteUtil.divUSeg(zIn) * SegmentData.SEG;
		Segment segment = segmentBuffer.get(x, y, z);
		return segment;
	}

	public Vector3i getNeighboringSegmentPosUnsave(Vector3b inOut, Segment from, Vector3i out, Vector3i absSegPos) {
		int x = inOut.x >> 4;
		int y = inOut.y >> 4;
		int z = inOut.z >> 4;
		absSegPos.x = from.absPos.x + x;
		absSegPos.y = from.absPos.y + y;
		absSegPos.z = from.absPos.z + z;
		out.x = from.pos.x + x * SegmentData.SEG;
		out.y = from.pos.y + y * SegmentData.SEG;
		out.z = from.pos.z + z * SegmentData.SEG;
		inOut.x = (byte) (inOut.x & 0xF);
		inOut.y = (byte) (inOut.y & 0xF);
		inOut.z = (byte) (inOut.z & 0xF);
		return out;
	}

	public Vector3i getNeighborSegmentPos(Vector3i pos, int dir, Vector3i out) {
		out.set(pos);
		switch(dir) {
			case (Element.LEFT):
				out.x -= SegmentData.SEG;
				break;
			case (Element.RIGHT):
				out.x += SegmentData.SEG;
				break;
			case (Element.BOTTOM):
				out.y -= SegmentData.SEG;
				break;
			case (Element.TOP):
				out.y += SegmentData.SEG;
				break;
			case (Element.BACK):
				out.z -= SegmentData.SEG;
				break;
			case (Element.FRONT):
				out.z += SegmentData.SEG;
				break;
			default:
				assert (false);
		}
		return out;
	}

	/**
	 * @return the seed
	 */
	public long getSeed() {
		return seed;
	}

	public void setSeed(long nextInt) {
		this.seed = nextInt;
	}

	public SegmentBufferInterface getSegmentBuffer() {
		return segmentBuffer;
	}

	public void setSegmentBuffer(SegmentBufferInterface buffer) {
		segmentBuffer = buffer;
	}

	public Segment getSegmentFromCache(int x, int y, int z) {
		return segmentBuffer.get(x, y, z);
	}

	public SegmentProvider getSegmentProvider() {
		return segmentProvider;
	}

	/**
	 * @return the spawner
	 */
	public String getSpawner() {
		return spawner;
	}

	public void setSpawner(String uniqueIdentifier) {
		if(uniqueIdentifier.equals(this.spawner)) {
			setChangedForDb(true);
		}
		this.spawner = uniqueIdentifier;
	}

	/**
	 * @return the timeCreated
	 */
	public long getTimeCreated() {
		return timeCreated;
	}

	/**
	 * @param timeCreated the timeCreated to set
	 */
	public void setTimeCreated(long timeCreated) {
		this.timeCreated = timeCreated;
	}

	/**
	 * @return the totalElements
	 */
	public int getTotalElements() {
		return totalElements;
	}

	public void setTotalElements(int elementCount) {
		totalElements = elementCount;
		flagupdateMass();
		flagUpdateDocking();
	}

	// /**
	// * @return the transformCacheSerial
	// */
	// public int getTransformCacheSerial() {
	// return transformCacheSerial;
	// }
	@Override
	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}

	public void setUniqueIdentifier(String uniqueIdentifier) {
		// try{
		// throw new NullPointerException("UID SETTING TO "+uniqueIdentifier+"; WAS: "+this.uniqueIdentifier);
		// }catch(Exception e){
		// e.printStackTrace();
		// }
		this.uniqueIdentifier = uniqueIdentifier;
	}

	public boolean hasNeighborElements(final Segment start, byte inX, byte inY, byte inZ) {
		if(SegmentData.allNeighborsInside(inX, inY, inZ)) {
			for(int i = 0; i < 6; i++) {
				byte x = (byte) ByteUtil.modUSeg(inX + Element.DIRECTIONSb[i].x);
				byte y = (byte) ByteUtil.modUSeg(inY + Element.DIRECTIONSb[i].y);
				byte z = (byte) ByteUtil.modUSeg(inZ + Element.DIRECTIONSb[i].z);
				if(start.getSegmentData().containsUnsave(x, y, z)) {
					return true;
				}
			}
		} else {
			for(int i = 0; i < 6; i++) {
				byte x = (byte) (inX + Element.DIRECTIONSb[i].x);
				byte y = (byte) (inY + Element.DIRECTIONSb[i].y);
				byte z = (byte) (inZ + Element.DIRECTIONSb[i].z);
				Segment neighboringSegment = getNeighboringSegmentFast(start, x, y, z);
				if(neighboringSegment != null && !neighboringSegment.isEmpty()) {
					x = (byte) ByteUtil.modUSeg(x);
					y = (byte) ByteUtil.modUSeg(y);
					z = (byte) ByteUtil.modUSeg(z);
					if(neighboringSegment.getSegmentData().containsUnsave(x, y, z)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public void incTotalElements() {
		setTotalElements(totalElements + 1);
	}

	/**
	 * @return the flagSegmentBufferAABBUpdate
	 */
	public boolean isFlagSegmentBufferAABBUpdate() {
		return flagSegmentBufferAABBUpdate;
	}

	/**
	 * @param flagSegmentBufferAABBUpdate the flagSegmentBufferAABBUpdate to set
	 */
	public void setFlagSegmentBufferAABBUpdate(boolean flagSegmentBufferAABBUpdate) {
		this.flagSegmentBufferAABBUpdate = flagSegmentBufferAABBUpdate;
	}

	/**
	 * @return
	 */
	public boolean isInbound(int x, int y, int z) {
		boolean in = (x <= maxPos.x && y <= maxPos.y && z <= maxPos.z) && (x >= minPos.x && y >= minPos.y && z >= minPos.z);
		// System.err.println("INBOUND TESTING "+x+"; "+y+"; "+z+"::: "+getMinPos()+"; "+getMaxPos()+" -> "+in+" "+getState()+"; "+this);
		return in;
	}

	/**
	 * @param pos without HALF_SIZE and divided by segDim
	 *
	 * @return
	 */
	public boolean isInbound(Vector3i pos) {
		return isInbound(pos.x, pos.y, pos.z);
	}

	public boolean isInboundAbs(int segmentPosX, int segmentPosY, int segmentPosZ) {
		int x = ByteUtil.divSeg(segmentPosX);
		int y = ByteUtil.divSeg(segmentPosY);
		int z = ByteUtil.divSeg(segmentPosZ);
		return (x <= maxPos.x && y <= maxPos.y && z <= maxPos.z) && (x >= minPos.x && y >= minPos.y && z >= minPos.z);
	}

	/**
	 * ONLY USED FOR SEGMENT POSITIONS
	 * If used with absolute position keep in minde that
	 * negative position are counted differently
	 *
	 * @return true, if that position is within the min and max bb
	 */
	public boolean isInboundAbs(Vector3i segmentPos) {
		return isInboundAbs(segmentPos.x, segmentPos.y, segmentPos.z);
	}

	/**
	 * @param pos without HALF_SIZE and divided by segDim
	 *
	 * @return
	 */
	public boolean isInboundCoord(int coord, Vector3i pos) {
		boolean in = (pos.getCoord(coord) <= maxPos.getCoord(coord)) && (pos.getCoord(coord) >= minPos.getCoord(coord));
		return in;
	}

	public boolean isInboundCoord(int coord, int pos) {
		boolean in = (pos <= maxPos.getCoord(coord)) && (pos >= minPos.getCoord(coord));
		return in;
	}

	public boolean mayActivateOnThis(SegmentController allowedFor, SegmentPiece testAgainstPiece) {
		return SegmentController.isPublicException(testAgainstPiece, allowedFor.getFactionId()) || !((FactionState) getState()).getFactionManager().existsFaction(getFactionId()) || allowedFor.getFactionId() == getFactionId() || (allowedFor instanceof PlayerControllable && !((PlayerControllable) allowedFor).getAttachedPlayers().isEmpty() && ((PlayerControllable) allowedFor).getAttachedPlayers().get(0).getFactionId() == getFactionId());
	}

	public final Matrix3f tensor = new Matrix3f();
	private final Matrix3f j = new Matrix3f();
	private final Vector3f bPos = new Vector3f();
	public String currentOwnerLowerCase = "";
	public String lastDockerPlayerServerLowerCase = "";
	public long dbId = -1;
	public final Vector3f proximityVector = new Vector3f();
	public long blinkTime;
	public Shader blinkShader;
	private boolean loadedFromChunk16;
	public NPCEntitySpecification npcSpec;
	private long lastStuck;
	private long stuckTime;
	private int stuckCount;
	private boolean factionSetFromBlueprint;
	private boolean flagLongStuck;
	private long lastExceptionPrint;
	private int updateCounter;
	private boolean checkedRootDb;
	public long lastAllowed;
	private float massMod;
	public boolean usedOldPowerFromTag = false;
	public boolean usedOldPowerFromTagForcedWrite = false;
	protected long lastSendHitConfirm;
	private long lastControlledMsgUpdate;
	private boolean blockAdded;
	private boolean blockRemoved;
	public long lastAdminCheckFlag;
	/**
	 * includes shield damage
	 */
	public long lastAnyDamageTakenServer;
	private boolean flagAnyDamageTakenServer;
	private long lastAttackTrigger;

	public void onAddedElementSynched(short newType, byte orientation, byte x, byte y, byte z, Segment segment, boolean updateSegmentBuffer, long absIndex, long time, boolean revalidate) {
		hpController.onAddedElementSynched(newType);
		bPos.set(((segment.pos.x + x) - SegmentData.SEG_HALF), ((segment.pos.y + y) - SegmentData.SEG_HALF), ((segment.pos.z + z) - SegmentData.SEG_HALF));
		ElementInformation info = ElementKeyMap.getInfoFast(newType);
		float massOfBlock = info.getMass();
		centerOfMassUnweighted.x += bPos.x * massOfBlock;
		centerOfMassUnweighted.y += bPos.y * massOfBlock;
		centerOfMassUnweighted.z += bPos.z * massOfBlock;
		totalPhysicalMass += massOfBlock;
		// compute inertia tensor of pointmass at o
		float o2 = bPos.lengthSquared();
		j.m00 = o2;
		j.m01 = 0;
		j.m02 = 0;
		j.m10 = 0;
		j.m11 = o2;
		j.m12 = 0;
		j.m20 = 0;
		j.m21 = 0;
		j.m22 = o2;
		j.m00 += bPos.x * -bPos.x;
		j.m01 += bPos.y * -bPos.x;
		j.m02 += bPos.z * -bPos.x;
		j.m10 += bPos.x * -bPos.y;
		j.m11 += bPos.y * -bPos.y;
		j.m12 += bPos.z * -bPos.y;
		j.m20 += bPos.x * -bPos.z;
		j.m21 += bPos.y * -bPos.z;
		j.m22 += bPos.z * -bPos.z;
		// add inertia tensor of pointmass
		tensor.m00 += massOfBlock * j.m00;
		tensor.m01 += massOfBlock * j.m01;
		tensor.m02 += massOfBlock * j.m02;
		tensor.m10 += massOfBlock * j.m10;
		tensor.m11 += massOfBlock * j.m11;
		tensor.m12 += massOfBlock * j.m12;
		tensor.m20 += massOfBlock * j.m20;
		tensor.m21 += massOfBlock * j.m21;
		tensor.m22 += massOfBlock * j.m22;
		blockAdded = true;
		//INSERTED CODE @1688
		if (StarLoader.hasListeners(SegmentPieceAddEvent.class))
			StarLoader.fireEvent(new SegmentPieceAddEvent(this, newType,orientation,x,y,z,segment,updateSegmentBuffer, absIndex), this.isOnServer());
		for (SegmentPieceAddListener listener : FastListenerCommon.segmentPieceAddListeners)
			listener.onAdd(this, newType,orientation,x,y,z,segment,updateSegmentBuffer, absIndex, isOnServer());
		///
		long indexAndOrientation = ElementCollection.getIndex4(absIndex, orientation);
		if(ElementKeyMap.isTextBox(newType)) textBlocks.add(indexAndOrientation);
		else if(ElementKeyMap.getInfoFast(newType).isArmor()) {
			SegmentController controller = railController.getRoot();
			ArmorHPCollection.getCollection(controller).addBlock(indexAndOrientation, newType);
		} else if(ElementKeyMap.isCrewModule(newType)) addCrewBlock(absIndex, ElementKeyMap.getInfo(newType));
		elementClassCountMap.inc(newType);
		if(info.resourceInjection == ResourceInjectionType.ORE && orientation > 0 && orientation <= 32) {
			int resource = orientation - 1;
			elementClassCountMap.addOre(resource);
		}
		incTotalElements();
	}

	private void addCrewBlock(long index, ElementInformation info) {
		quarterManager.addCrewStation(segmentBuffer.getPointUnsave(index), info);
	}

	public void addFromMeta(float totalPhysicalMass, Vector3f centerOfMassUnweighted, int totalElements, int[] counts, int[] oreCounts, LongArrayList textBlocks, Matrix3f tensor) {
		this.totalPhysicalMass += totalPhysicalMass;
		this.centerOfMassUnweighted.add(centerOfMassUnweighted);
		this.totalElements += totalElements;
		if(isOnServer() && elementClassCountMap.restrictedBlocks(counts)) {
			scrap = true;
			((GameServerState) getState()).getController().broadcastMessage(Lng.astr("Tried to spawn ship with more than the\n" + "allowed amount of restricted block (e.g. faction block)\n" + "%s in %s\n" + "by %s", this.toString(), getSector(new Vector3i()).toString(), spawner != null ? spawner : "unknown"), ServerMessage.MESSAGE_TYPE_ERROR);
		}
		this.elementClassCountMap.add(counts, oreCounts);
		this.textBlocks.addAll(textBlocks);
		this.tensor.add(tensor);
		blockAdded = true;
		hpController.onAddedElementsSynched(counts, oreCounts);
		// System.err.println("ADDED FROM META");
	}

	// private LongOpenHashSet mm = new LongOpenHashSet();
	public void onProximity(SegmentController segmentController) {
		if(this.railController.isInAnyRailRelationWith(segmentController) || (this.dockingController.isDocked() && this.dockingController.getDockedOn().from.getSegment().getSegmentController() == segmentController)) {
			return;
		} else if(!this.dockingController.getDockedOnThis().isEmpty()) {
			for(ElementDocking e : this.dockingController.getDockedOnThis()) {
				if(e.from.getSegment().getSegmentController() == segmentController) {
					return;
				}
			}
		}
		// System.err.println("ON PROXY: "+this+" -> "+segmentController);
		this.proximityObjects.add(segmentController.id);
	}

	public void onRemovedElementSynched(short oldType, int oldSize, byte x, byte y, byte z, byte oldOrientation, Segment segment, boolean preserveControl, long time) {
		if(!isOnServer()) {
			segmentBuffer.onRemovedElementClient(oldType, oldSize, x, y, z, segment, time);
		}
		ElementInformation info = ElementKeyMap.getInfoFast(oldType);
		float massOfBlock = info.getMass();
		bPos.set(((segment.pos.x + x) - SegmentData.SEG_HALF), ((segment.pos.y + y) - SegmentData.SEG_HALF), ((segment.pos.z + z) - SegmentData.SEG_HALF));
		centerOfMassUnweighted.x -= bPos.x * massOfBlock;
		centerOfMassUnweighted.y -= bPos.y * massOfBlock;
		centerOfMassUnweighted.z -= bPos.z * massOfBlock;
		totalPhysicalMass -= massOfBlock;
		// compute inertia tensor of pointmass at o
		float o2 = bPos.lengthSquared();
		j.setRow(0, o2, 0, 0);
		j.setRow(1, 0, o2, 0);
		j.setRow(2, 0, 0, o2);
		j.m00 += bPos.x * -bPos.x;
		j.m01 += bPos.y * -bPos.x;
		j.m02 += bPos.z * -bPos.x;
		j.m10 += bPos.x * -bPos.y;
		j.m11 += bPos.y * -bPos.y;
		j.m12 += bPos.z * -bPos.y;
		j.m20 += bPos.x * -bPos.z;
		j.m21 += bPos.y * -bPos.z;
		j.m22 += bPos.z * -bPos.z;
		// add inertia tensor of pointmass
		tensor.m00 -= massOfBlock * j.m00;
		tensor.m01 -= massOfBlock * j.m01;
		tensor.m02 -= massOfBlock * j.m02;
		tensor.m10 -= massOfBlock * j.m10;
		tensor.m11 -= massOfBlock * j.m11;
		tensor.m12 -= massOfBlock * j.m12;
		tensor.m20 -= massOfBlock * j.m20;
		tensor.m21 -= massOfBlock * j.m21;
		tensor.m22 -= massOfBlock * j.m22;
		hpController.onRemovedElementSynched(oldType);
		blockRemoved = true;
		elementClassCountMap.dec(oldType);
		if(info.resourceInjection == ResourceInjectionType.ORE && oldOrientation > 0 && oldOrientation <= 32) {
			int resource = oldOrientation - 1;
			elementClassCountMap.decOre(resource);
		}
		decTotalElements();
		if(segment.isEmpty()) {
			segmentBuffer.onSegmentBecameEmpty(segment);
		}
		//INSERTED CODE @1796
		if (StarLoader.hasListeners(SegmentPieceRemoveEvent.class))
			StarLoader.fireEvent(new SegmentPieceRemoveEvent(oldType, oldSize, x, y, z, oldOrientation, segment, preserveControl), this.isOnServer());
		for (SegmentPieceRemoveListener listener : FastListenerCommon.segmentPieceRemoveListeners)
			listener.onBlockRemove(oldType, oldSize, x, y, z, oldOrientation, segment, preserveControl, isOnServer());
		///
		if(ElementKeyMap.getInfoFast(oldType).isArmor()) {
			SegmentController controller = railController.getRoot();
			ArmorHPCollection.getCollection(controller).removeBlock(ElementCollection.getIndex4(ElementCollection.getIndex(x, y, z, segment), oldOrientation), oldType);
		}

		//mm.remove(segment.getAbsoluteIndex(x, y, z));
		if(!preserveControl) {
			controlElementMap.onRemoveElement(segment.getAbsoluteIndex(x, y, z), oldType);
			if(isOnServer()) {
				if(oldType == ElementKeyMap.FACTION_BLOCK) {
					System.err.println("[SERVER] FACTION BLOCK REMOVED FROM " + this + "; resetting faction !!!!!!!!!!!!!!");
					railController.resetFactionForEntitiesWithoutFactionBlock(getFactionId());
				}
			}
			if(ElementKeyMap.isTextBox(oldType) || oldType == ElementKeyMap.LOGIC_REMOTE_INNER) {
				segment.getAbsoluteIndex(x, y, z);
				long absoluteIndex = segment.getAbsoluteIndex(x, y, z);
				long indexAndOrientation = ElementCollection.getIndex4(absoluteIndex, oldType == ElementKeyMap.LOGIC_REMOTE_INNER ? ElementKeyMap.LOGIC_REMOTE_INNER : oldOrientation);
				textBlocks.remove(indexAndOrientation);
				textMap.remove(indexAndOrientation);
			}
		} else {
		}
	}

	@Override
	public boolean hasAnyReactors() {
		return (this instanceof ManagedSegmentController<?>) && ((ManagedSegmentController<?>) this).getManagerContainer().hasAnyReactors();
	}

	@Override
	public boolean isInAdminInvisibility() {
		if(railController.isRoot()) {
			return super.isInAdminInvisibility();
		} else {
			return super.isInAdminInvisibility() || railController.getRoot().isInAdminInvisibility();
		}
	}

	protected void readExtraTagData(Tag t) {
	}

	public PlayerState isInGodmode() {
		if(this instanceof PlayerControllable) {
			for(PlayerState s : ((PlayerControllable) this).getAttachedPlayers()) {
				if(s.isGodMode()) {
					return s;
				}
			}
		}
		if(dockingController.isDocked()) {
			return dockingController.getDockedOn().to.getSegment().getSegmentController().isInGodmode();
		}
		if(railController.isDockedAndExecuted()) {
			return railController.previous.rail.getSegmentController().isInGodmode();
		}
		return null;
	}

	public void resetTotalElements() {
		setTotalElements(0);
		totalPhysicalMass = 0;
		centerOfMassUnweighted.set(0, 0, 0);
		getPhysicsDataContainer().lastCenter.set(0, 0, 0);
	}

	public void setCurrentBlockController(SegmentPiece controlling, SegmentPiece tmpPiece, long controlled) throws CannotBeControlledException {
		setCurrentBlockController(controlling, tmpPiece, controlled, 0);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.Damager#isSegmentController()
	 */
	@Override
	public boolean isSegmentController() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.Damager#getName()
	 */
	@Override
	public String getName() {
		return toNiceString();
	}

	public void setCurrentBlockController(SegmentPiece controlling, SegmentPiece tmp, long controlled, int mode) throws CannotBeControlledException {
		// autorequest true previously
		SegmentPiece controlledPiece = segmentBuffer.getPointUnsave(controlled, tmp);
		// System.err.println("[SEGMENTCONTROLLER] CONTROLLED PIECE: "+controlledPiece+" ("+controlledPiece.getType()+")");
		if(controlledPiece != null && controlling != null) {
			controlling.refresh();
			assert (controlling.getSegment().getSegmentData() != null) : controlling;
			assert (controlledPiece.getSegment().getSegmentData() != null) : "Exception: " + this.getState() + " " + this + " controlled piece is null " + controlled + "; " + controlledPiece.getSegment();
			if(controlledPiece.getSegment().getSegmentData() == null) {
				System.err.println("Exception: " + this.getState() + " " + this + " controlled piece SegmentData is null " + controlled + "; " + controlledPiece.getSegment());
				return;
			}
			final short fromType = controlling.getType();
			final short toType = controlledPiece.getType();
			assert (fromType > 0) : toType + "; " + controlling + "; " + this;
			assert (toType > 0) : toType + "; " + controlledPiece + "; " + this;
			if(toType <= 0 || fromType <= 0) {
				System.err.println("Exception: set controller for invalid type: " + fromType + " for: " + controlling + " -> " + controlled + "; ");
				System.err.println("Exception (Cnt): set controller for invalid type: " + toType + " for: " + controlling + " -> " + controlled + "; ");
				return;
			}
			if(controlling.getAbsoluteIndex() == controlled) {
				if(!isOnServer()) {
					((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Cannot connect a block\nto itself!"), 0);
				}
				return;
			}
			ElementInformation fromInfo = ElementKeyMap.getInfo(fromType);
			ElementInformation toInfo = ElementKeyMap.getInfo(toType);
			if(this instanceof ManagedSegmentController<?>) {
				if(!((ManagedSegmentController<?>) this).getManagerContainer().canBeControlled(fromType, toType)) {
					System.err.println("[SEGMENTCONTROLLER] This cant be controlled by " + this + ": " + ElementKeyMap.toString(fromType) + " -> " + ElementKeyMap.toString(toType) + "; ManagerContainer.canBeController(from, to) failed.");
					throw new CannotBeControlledException(fromInfo, toInfo);
				}
			}
			if((toInfo.getControlledBy().contains(fromType)) || fromInfo.controlsAll() || fromInfo.isCombiConnectAny(toType) || ElementInformation.canBeControlled(fromType, toType)) {
				ControlElementMap cMap = controlling.getSegmentController().controlElementMap;
				long controllingIndex = controlling.getAbsoluteIndex();
				if(mode == 0) {
					cMap.switchControllerForElement(controllingIndex, controlled, toType);
				} else if(mode == 1) {
					// force on
					if(!cMap.isControlling(controllingIndex, controlled, toType)) {
						cMap.switchControllerForElement(controllingIndex, controlled, toType);
					}
				} else {
					// force off
					if(cMap.isControlling(controllingIndex, controlled, toType)) {
						cMap.switchControllerForElement(controllingIndex, controlled, toType);
					}
				}
			} else {
				if(lastControlledMsgUpdate != getState().getUpdateTime()) {
					System.err.println(getState() + " [SegmentController][setCurrentBlockController]  " + ElementKeyMap.toString(toType) + " CANNOT BE CONTROLLED BY " + ElementKeyMap.toString(fromType) + "; controlled by: " + toInfo.getControlledBy() + "; controlling " + ElementKeyMap.getInfo(fromType).getControlling() + "; " + ElementKeyMap.getInfo(fromType) + ":  -> " + controlled + " (message will display only once per update)");
					lastControlledMsgUpdate = getState().getUpdateTime();
				}
				if(!isOnServer()) {
					if(toInfo.getControlledBy().size() > 0) {
						((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("%s cannot be\nconnected to %s.\nReason: Incompatible blocks", ElementKeyMap.getInfo(toType).getName(), ElementKeyMap.getInfo(fromType).getName()), 0);
					}
				}
			}
		}
	}

	public abstract void startCreatorThread();

	public Tag getTextBlockTag() {
		return Tag.listToTagStruct(textMap, null);
	}

	public DebugLine[] getCenterOfMassCross() {
		assert (!isOnServer());
		return DebugLine.getCross(getClientTransform(), getPhysicsDataContainer().lastCenter, getBoundingBox().getSize() / 2, getBoundingBox().getSize() / 2, getBoundingBox().getSize() / 2, false);
	}

	public void drawDebugTransform() {
		Transform t;
		if(isOnServer() && GameClientState.staticSector == this.getSectorId()) {
			t = getWorldTransform();
		} else {
			t = getClientTransform();
		}
		Vector3f up = GlUtil.getUpVector(new Vector3f(), t);
		Vector3f right = GlUtil.getRightVector(new Vector3f(), t);
		Vector3f forward = GlUtil.getForwardVector(new Vector3f(), t);
		if(isOnServer()) {
			Vector3f a = new Vector3f();
			a.add(up);
			a.add(right);
			a.add(forward);
			a.normalize();
			a.scale(3);
			a.add(t.origin);
			DebugLine l = new DebugLine(new Vector3f(t.origin), a, new Vector4f(1, 0, 1, 1));
			if(!DebugDrawer.lines.contains(l)) {
				DebugDrawer.lines.add(l);
			}
		}
		Vector3f up1 = new Vector3f(up);
		Vector3f right1 = new Vector3f(right);
		Vector3f forward1 = new Vector3f(forward);
		up.set(0, 0, 0);
		right.set(0, 0, 0);
		forward.set(0, 0, 0);
		up1.scale(3);
		right1.scale(3);
		forward1.scale(3);
		up.add(t.origin);
		right.add(t.origin);
		forward.add(t.origin);
		up1.add(t.origin);
		right1.add(t.origin);
		forward1.add(t.origin);
		Vector4f uC = new Vector4f(0, 1, 0, 1);
		Vector4f rC = new Vector4f(1, 0, 0, 1);
		Vector4f fC = new Vector4f(0, 0, 1, 1);
		if(isOnServer()) {
			uC.x += 0.8f;
			rC.z += 0.8f;
			fC.y += 0.8f;
		}
		DebugLine uLine = new DebugLine(up, up1, uC);
		DebugLine rLine = new DebugLine(right, right1, rC);
		DebugLine fLine = new DebugLine(forward, forward1, fC);
		if(!DebugDrawer.lines.contains(uLine)) {
			DebugDrawer.lines.add(uLine);
		}
		if(!DebugDrawer.lines.contains(rLine)) {
			DebugDrawer.lines.add(rLine);
		}
		if(!DebugDrawer.lines.contains(fLine)) {
			DebugDrawer.lines.add(fLine);
		}
	}

	private void handleTriggers(long time) {
		if(!triggers.isEmpty()) {
			ObjectIterator<ActivationTrigger> iterator = triggers.iterator();
			while(iterator.hasNext()) {
				ActivationTrigger act = iterator.next();
				// System.err.println("HANDLING TRIGGER "+act);
				if(!act.fired) {
					try {
						fireActivation(act);
					} catch(IOException e) {
						e.printStackTrace();
					}
					act.fired = true;
				}
				if(time - act.ping > 700) {
					iterator.remove();
				}
			}
		}
	}

	@Override
	public abstract InterEffectSet getAttackEffectSet(long weaponId, DamageDealerType damageDealerType);

	private void handleSlowdown(long time) {
		if(time - getLastSlowdown() < 5000 || isImmediateStuck()) {
			if((getSlowdownStart() > 0 && time - getSlowdownStart() > 10000) || isImmediateStuck()) {
				System.err.println("[SEGCON] " + getState() + " stuck physics detected on " + this);
				if(isOnServer()) {
					if(isImmediateStuck()) {
						((GameServerState) getState()).getController().broadcastMessage(Lng.astr("Structure\n%s\ndetected to be stuck!\nWARPING OUT OF COLLISION!", this), ServerMessage.MESSAGE_TYPE_ERROR);
					} else {
						((GameServerState) getState()).getController().broadcastMessage(Lng.astr("Structure\n%s\nis slowing down the server!\nWARPING OUT OF COLLISION!", this), ServerMessage.MESSAGE_TYPE_ERROR);
					}
				}
				warpOutOfCollision();
				getPhysicsDataContainer().updatePhysical(getState().getUpdateTime());
				resetSlowdownStart();
				setImmediateStuck(false);
			}
		} else {
			resetSlowdownStart();
		}
	}

	@Override
	public void destroy() {
		System.out.println("[SEGMENTCONTROLLER] ENTITY " + this + " HAS BEEN DESTROYED... ");
		Faction faction = getFaction();
		if(faction != null) {
			faction.onEntityDestroyedServer(this);
		}
		this.markForPermanentDelete(true);
		this.setMarkedForDeleteVolatile(true);
	}

	private void updateCoreOverheating(Timer timer, long time) {
		if(isCoreOverheating()) {
			long timeCoreTimerRunning = time - coreTimerStarted;
			long timeLeft = coreTimerDuration - timeCoreTimerRunning;
			if(isOnServer() && !ServerConfig.USE_STRUCTURE_HP.isOn() && timeCoreTimerRunning > 1000 && (this instanceof Ship) && ((Ship) this).getAttachedPlayers().size() > 0) {
				stopCoreOverheating();
				// autorequest true previously
				SegmentPiece segmentPiece = segmentBuffer.getPointUnsave(Ship.core);
				if(segmentPiece != null) {
					int infoIndex = segmentPiece.getInfoIndex();
					int recover = 1;
					short hitPoints = segmentPiece.getSegment().getSegmentData().getHitpointsByte(infoIndex);
					short maxHitPoints = ElementKeyMap.MAX_HITPOINTS;
					try {
						segmentPiece.getSegment().getSegmentData().setHitpointsByte(infoIndex, (short) Math.min(maxHitPoints, hitPoints + recover));
					} catch(SegmentDataWriteException e) {
						SegmentDataWriteException.replaceData(segmentPiece.getSegment());
						try {
							segmentPiece.getSegment().getSegmentData().setHitpointsByte(infoIndex, (short) Math.min(maxHitPoints, hitPoints + recover));
						} catch(SegmentDataWriteException e1) {
							throw new RuntimeException(e1);
						}
					}
					sendBlockMod(new RemoteSegmentPiece(segmentPiece, isOnServer()));
				}
				// destruction averted in the old way by entering core
			} else if(timeCoreTimerRunning > coreTimerDuration) {
				if(isOnServer()) {
					System.err.println("[SERVER][DESTROY] CORE OVERHEATED COMPLETELY: KILLING ALL SHIP CREW " + this);
					if(this instanceof PlayerControllable) {
						List<PlayerState> attachedPlayers = ((PlayerControllable) this).getAttachedPlayers();
						for(int i = 0; i < attachedPlayers.size(); i++) {
							attachedPlayers.get(i).handleServerHealthAndCheckAliveOnServer(0, this);
						}
					}
					railController.undockAllServer();
					destroy();
				} else {
					// add final explosions
				}
				coreTimerStarted = -1;
				coreTimerDuration = -1;
			}
			if(!isOnServer()) {
				// add some explosions
				float explostionDesity = 0.00003f;
				if(timeLeft < 4000) {
					explostionDesity = 0.1f;
				} else if(timeLeft < 30000) {
					explostionDesity = 0.02f;
				} else if(timeLeft < 60000 * 2) {
					explostionDesity = 0.003f;
				} else if(timeLeft < 60000 * 4) {
					explostionDesity = 0.0005f;
				}
				if(this.isClientOwnObject()) {
					((GameClientState) getState()).getController().showBigTitleMessage("overheat", "SYSTEMS OVERHEATING\n" + StringTools.formatTimeFromMS(timeLeft) + " until destruction. Reboot Systems to Stop ['" + KeyboardMappings.REBOOT_SYSTEMS.getKeyChar() + "']", 0);
				}
				if(Math.random() < explostionDesity) {
					GameClientState s = (GameClientState) getState();
					Vector3f a = new Vector3f();
					segmentBuffer.getBoundingBox().calculateHalfSize(a);
					float size = a.length();
					Vector3f randomDir = new Vector3f((float) (Math.random() - 0.5f), (float) (Math.random() - 0.5f), (float) (Math.random() - 0.5f));
					while(randomDir.lengthSquared() == 0) {
						randomDir.set((float) (Math.random() - 0.5f), (float) (Math.random() - 0.5f), (float) (Math.random() - 0.5f));
					}
					randomDir.normalize();
					randomDir.scale((float) (size * (1 + Math.random())));
					a.set(getWorldTransform().origin);
					a.add(randomDir);
					// System.err.println("ADDING EXPLOSION: "+a);
					s.getWorldDrawer().getExplosionDrawer().addExplosion(a, (float) (2f + Math.random() * 40f));
				}
			}
		}
	}

	protected abstract void fireActivation(ActivationTrigger act) throws IOException;

	public boolean updateMassServer() {
		// only for dynamic objects
		return true;
	}

	private void warpOutOfCollision() {
		if(isOnServer() && getMass() > 0) {
			Vector3f outMinSelf = new Vector3f();
			Vector3f outMaxSelf = new Vector3f();
			Transform t = new Transform(getWorldTransform());
			boolean collision = false;
			do {
				getTransformedAABB(outMinSelf, outMaxSelf, 1, new Vector3f(), new Vector3f(), null);
				collision = false;
				for(Sendable sen : getState().getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
					if(sen instanceof SimpleTransformableSendableObject) {
						SimpleTransformableSendableObject s = (SimpleTransformableSendableObject) sen;
						if(s != this && s.getSectorId() == this.getSectorId() && !s.isHidden()) {
							Vector3f outMinOther = new Vector3f();
							Vector3f outMaxOther = new Vector3f();
							s.calcWorldTransformRelative(getSectorId(), ((GameServerState) getState()).getUniverse().getSector(getSectorId()).pos);
							s.getTransformedAABB(outMinOther, outMaxOther, 1, new Vector3f(), new Vector3f(), s.getClientTransform());
							if(AabbUtil2.testAabbAgainstAabb2(outMinSelf, outMaxSelf, outMinOther, outMaxOther)) {
								collision = true;
								break;
							}
						}
					}
				}
				if(collision) {
					getWorldTransform().origin.y += 20;
				}
			} while(collision);
			System.err.println("[SEREVR][SEGMENTCONTROLLER] WARNING: COLLISION RECOVER: " + this + " warped from " + t.origin + " to " + getWorldTransform().origin);
			Transform moved = new Transform(getWorldTransform());
			getWorldTransform().set(t);
			warpTransformable(moved, true, false, null);
		}
	}

	public void kickAllPlayersOutServer() {
		kickPlayerOutServer(null);
	}

	public void kickPlayerOutServer(PlayerState player) {
		assert (isOnServer());
		if(this instanceof PlayerControllable) {
			PlayerControllable pa = (PlayerControllable) this;
			for(PlayerState p : pa.getAttachedPlayers()) {
				if(player == null || p == player) {
					p.getControllerState().forcePlayerOutOfSegmentControllers();
				}
			}
		}
	}

	public abstract int writeAllBufferedSegmentsToDatabase(boolean includeDocked, boolean forced, boolean forceWriteUnchanged) throws IOException;

	public boolean isNewlyCreated() {
		return newlyCreated;
	}

	public void onSegmentAddedSynchronized(Segment s) {
	}

	/**
	 * @return the triggers
	 */
	public ObjectOpenHashSet<ActivationTrigger> getTriggers() {
		return triggers;
	}

	public boolean checkClientLoadedOverlap(PlayerCharacter playerCharacter) {
		boolean ok = true;
		Vector3f min = new Vector3f(minPos.x * SegmentData.SEG, minPos.y * SegmentData.SEG, minPos.z * SegmentData.SEG);
		Vector3f max = new Vector3f(maxPos.x * SegmentData.SEG, maxPos.y * SegmentData.SEG, maxPos.z * SegmentData.SEG);
		assert (min.x <= max.x) : min + "; " + max;
		assert (min.y <= max.y) : min + "; " + max;
		assert (min.z <= max.z) : min + "; " + max;
		Vector3f outMin = new Vector3f();
		Vector3f outMax = new Vector3f();
		Vector3f coutMin = new Vector3f();
		Vector3f coutMax = new Vector3f();
		AabbUtil2.transformAabb(min, max, 0, getWorldTransformOnClient(), outMin, outMax);
		playerCharacter.getPhysicsDataContainer().getShape().getAabb(playerCharacter.getWorldTransformOnClient(), coutMin, outMax);
		if(AabbUtil2.testAabbAgainstAabb2(outMin, outMax, coutMin, outMax)) {
			Transform convexShapeViewFromCubes = new Transform();
			Transform cubeShapeTransformInv = new Transform(getWorldTransformOnClient());
			cubeShapeTransformInv.inverse();
			convexShapeViewFromCubes.set(cubeShapeTransformInv);
			convexShapeViewFromCubes.mul(playerCharacter.getWorldTransformOnClient());
			playerCharacter.getPhysicsDataContainer().getShape().getAabb(convexShapeViewFromCubes, coutMin, coutMax);
			BoundingBox bbSeg = new BoundingBox(min, max);
			BoundingBox bbChar = new BoundingBox(coutMin, coutMax);
			BoundingBox intersection = bbChar.getIntersection(bbSeg, new BoundingBox());
			Vector3i minIntA = new Vector3i();
			Vector3i maxIntA = new Vector3i();
			if(intersection != null && intersection.isValid()) {
				minIntA.x = ByteUtil.divSeg((int) (intersection.min.x - SegmentData.SEG_HALF)) * SegmentData.SEG;
				minIntA.y = ByteUtil.divSeg((int) (intersection.min.y - SegmentData.SEG_HALF)) * SegmentData.SEG;
				minIntA.z = ByteUtil.divSeg((int) (intersection.min.z - SegmentData.SEG_HALF)) * SegmentData.SEG;
				maxIntA.x = (FastMath.fastCeil((intersection.max.x + SegmentData.SEG_HALF) / SegmentData.SEGf)) * SegmentData.SEG;
				maxIntA.y = (FastMath.fastCeil((intersection.max.y + SegmentData.SEG_HALF) / SegmentData.SEGf)) * SegmentData.SEG;
				maxIntA.z = (FastMath.fastCeil((intersection.max.z + SegmentData.SEG_HALF) / SegmentData.SEGf)) * SegmentData.SEG;
				for(int z = minIntA.z; z < maxIntA.z; z += SegmentData.SEG) {
					for(int y = minIntA.y; y < maxIntA.y; y += SegmentData.SEG) {
						for(int x = minIntA.x; x < maxIntA.x; x += SegmentData.SEG) {
							int segState = segmentBuffer.getSegmentState(x, y, z);
							if(segState == SegmentBufferOctree.NOTHING) {
								if(!((ClientSegmentProvider) segmentProvider).existsOrIsInRequest(x, y, z)) {
									((ClientSegmentProvider) segmentProvider).enqueueHightPrio(x, y, z, false);
								} else {
									// System.err.println("NOT Client queuing: "+x+", "+y+" "+z+" of "+this+"; "+this.getMinPos()+"; "+this.getMaxPos()+"; inbound: "+isInbound(Segment.getSegmentIndexFromSegmentElement(x,y,z, new Vector3i()))+"; Q: "+((ClientSegmentProvider)getSegmentProvider()).isInQueue(x, y, z)+"; PRO: "+((ClientSegmentProvider)getSegmentProvider()).isInProgress(x, y, z));
								}
								playerCharacter.waitingForToSpawn.add(ElementCollection.getIndex(x, y, z));
								ok = false;
							}
						}
					}
				}
			} else {
			}
		} else {
		}
		return ok;
	}

	public RigidBody getPhysicsObject() {
		if(getPhysicsDataContainer().getObject() != null && getPhysicsDataContainer().getObject() instanceof RigidBody) {
			return (RigidBody) getPhysicsDataContainer().getObject();
		}
		if(dockingController.isDocked()) {
			SegmentController s0 = dockingController.getDockedOn().to.getSegment().getSegmentController();
			RigidBody physicsObject = s0.getPhysicsObject();
			if(physicsObject != null) {
				return physicsObject;
			}
		}
		return null;
	}

	public boolean isDocked() {
		if(dockingController.isDocked()) {
			return true;
		} else if(railController.isDockedAndExecuted()) {
			return true;
		}
		return false;
	}

	/**
	 * @return the textBlocks
	 */
	public LongArrayList getTextBlocks() {
		return textBlocks;
	}

	/**
	 * @return the textMap
	 */
	public Long2ObjectOpenHashMap<String> getTextMap() {
		return textMap;
	}

	public void popupOwnClientMessage(String message, int messageType) {
		if(isClientOwnObject()) {
			GameClientController controller = ((GameClientState) getState()).getController();
			switch(messageType) {
				case ServerMessage.MESSAGE_TYPE_ERROR:
					controller.popupAlertTextMessage(message, 0);
					break;
				case ServerMessage.MESSAGE_TYPE_INFO:
					controller.popupInfoTextMessage(message, 0);
					break;
				case ServerMessage.MESSAGE_TYPE_SIMPLE:
					controller.getState().chat(controller.getState().getPlayerName(), message, "[MESSAGE]", false);
					break;
				case ServerMessage.MESSAGE_TYPE_WARNING:
					controller.popupGameTextMessage(message, 0);
					break;
				default:
					assert false;
					break;
			}
		}
	}

	public void popupOwnClientMessage(String msgId, String message, int messageType) {
		if(isClientOwnObject()) {
			GameClientController controller = ((GameClientState) getState()).getController();
			switch(messageType) {
				case ServerMessage.MESSAGE_TYPE_ERROR:
					controller.popupAlertTextMessage(message, msgId, 0);
					break;
				case ServerMessage.MESSAGE_TYPE_INFO:
					controller.popupInfoTextMessage(message, msgId, 0);
					break;
				case ServerMessage.MESSAGE_TYPE_SIMPLE:
					controller.getState().chat(controller.getState().getPlayerName(), message, "[MESSAGE]", false);
					break;
				case ServerMessage.MESSAGE_TYPE_WARNING:
					controller.popupGameTextMessage(message, msgId, 0);
					break;
				default:
					assert false;
					break;
			}
		}
	}

	public void scan() {
		assert (isOnServer());
		System.err.println("[SCAN] doing scan on server");
	}

	@Override
	public float getSpeedCurrent() {
		CollisionObject object = dockingController.getAbsoluteMother().getPhysicsDataContainer().getObject();
		if(object instanceof RigidBody) {
			return ((RigidBody) object).getLinearVelocity(new Vector3f()).length();
		} else {
			return 0;
		}
	}

	@Override
	public Vector3f getLinearVelocity(Vector3f out) {
		out.set(0, 0, 0);
		if(getPhysicsDataContainer().getObject() != null) {
			RigidBody b = (RigidBody) getPhysicsDataContainer().getObject();
			out = b.getLinearVelocity(out);
		}
		return out;
	}

	@Override
	public boolean isClientOwnObject() {
		return !isOnServer() && ((GameClientState) getState()).getCurrentPlayerObject() == this;
	}

	@Override
	public void cleanUpOnEntityDelete() {
		super.cleanUpOnEntityDelete();
		try {
			segmentProvider.releaseFileHandles();
		} catch(IOException e) {
			e.printStackTrace();
		}
		creatorThread.terminate();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#fromTagStructure(org.schema.game.common.controller.io.Tag)
	 */
	@Override
	public void fromTagStructure(Tag tag) {
		newlyCreated = false;
		if("sc".equals(tag.getName())) {
			loadedFromChunk16 = true;
		}
		// newest version
		Tag[] subTags = (Tag[]) tag.getValue();
		if(subTags.length > 40 && subTags[40].getType() == Type.LONG) {
			tagVersion = subTags[40].getByte();
		}
		uniqueIdentifier = (String) subTags[0].getValue();
		Vector3i min = (Vector3i) subTags[1].getValue();
		Vector3i max = (Vector3i) subTags[2].getValue();
		minPos.set(min);
		maxPos.set(max);
		dockingController.fromTagStructure(subTags[3]);
		controlElementMap.setLoadedFromChunk16(true);
		controlElementMap.fromTagStructure(subTags[4]);
		setRealName((String) subTags[5].getValue());
		super.fromTagStructure(subTags[6]);
		if(this instanceof ManagedSegmentController) {
			((ManagedSegmentController<?>) this).getManagerContainer().fromTagStructure(subTags[7]);
		}
		// if(this instanceof ShopSpaceStation){
		// System.err.println("TAG DESERIALIZING: "+getUniqueIdentifier()+"; ");
		// }
		setCreatorId(((Integer) subTags[8].getValue()));
		setSpawner((String) subTags[9].getValue());
		setLastModifier((String) subTags[10].getValue());
		if(subTags.length > 11 && subTags[11].getType() == Type.LONG && subTags[11].getType() != Type.NOTHING) {
			seed = (Long) subTags[11].getValue();
		} else {
			seed = Universe.getRandom().nextLong();
		}
		if(subTags.length > 12 && subTags[12].getType() == Type.BYTE && subTags[12].getType() != Type.NOTHING) {
			if(this instanceof TransientSegmentController) {
				((TransientSegmentController) this).setTouched(((Byte) subTags[12].getValue()) == (byte) 1, false);
			}
		} else {
			if(this instanceof TransientSegmentController) {
				((TransientSegmentController) this).setTouched(true, false);
			}
		}
		if(subTags.length > 13 && subTags[13].getType() != Type.FINISH && subTags[13].getType() != Type.NOTHING) {
			readExtraTagData(subTags[13]);
		}
		if(subTags.length > 14 && subTags[14].getType() != Type.FINISH && subTags[14].getType() != Type.NOTHING) {
			readNPCData(subTags[14]);
		}
		if(subTags.length > 15 && subTags[15].getType() != Type.FINISH && subTags[15].getType() != Type.BYTE) {
			readTextBlockData(subTags[15]);
		} else if(subTags.length > 15 && subTags[15].getType() != Type.FINISH && subTags[15].getType() == Type.BYTE) {
			scrap = ((Byte) subTags[15].getValue()) > 0;
		}
		if(subTags.length > 16 && subTags[16].getType() != Type.FINISH && subTags[16].getType() == Type.BYTE) {
			vulnerable = ((Byte) subTags[16].getValue()) > 0;
		}
		if(subTags.length > 17 && subTags[17].getType() != Type.FINISH && subTags[17].getType() == Type.BYTE) {
			minable = ((Byte) subTags[17].getValue()) > 0;
		}
		byte factionRights = -2;
		if(subTags.length > 18 && subTags[18].getType() != Type.FINISH && subTags[18].getType() == Type.BYTE) {
			factionRights = (Byte) subTags[18].getValue();
		}
		if(subTags.length > 19 && subTags[19].getType() != Type.FINISH && subTags[19].getType() != Type.NOTHING) {
			boolean loadExpectedToDock = true;
			railController.fromTag(subTags[19], loadedFromChunk16 ? Chunk16SegmentData.SHIFT_ : 0, loadExpectedToDock);
		}
		if(subTags.length > 20 && subTags[20].getType() != Type.FINISH && subTags[20].getType() != Type.NOTHING) {
			int nonEmpty = ((Integer) subTags[20].getValue()).intValue();
			if(loadedFromChunk16) {
				// worst case is that tehre is one chunk32 for 27 chunk16
				nonEmpty = (int) Math.ceil(nonEmpty / 27f);
			}
			((SegmentBufferManager) segmentBuffer).setExpectedNonEmptySegmentsFromLoad(nonEmpty);
		}
		if(subTags.length > 21 && subTags[21].getType() != Type.FINISH && subTags[21].getType() != Type.NOTHING) {
			hpController.fromTagStructure(subTags[21]);
		}
		if(subTags.length > 22 && subTags[22].getType() != Type.FINISH && subTags[22].getType() != Type.NOTHING) {
			Tag[] ts = (Tag[]) subTags[22].getValue();
			long timeLeft = (Long) ts[0].getValue();
			if(timeLeft >= 0) {
				coreTimerStarted = System.currentTimeMillis();
				coreTimerDuration = timeLeft;
			}
		}
		if(subTags.length > 23 && subTags[23].getType() != Type.FINISH && subTags[23].getType() != Type.NOTHING) {
			byte b = (Byte) subTags[23].getValue();
			virtualBlueprint = b != 0;
		}
		if(subTags.length > 24 && subTags[24].getType() != Type.FINISH && subTags[24].getType() == Type.STRUCT) {
			blueprintSegmentDataPath = (String) ((Tag[]) subTags[24].getValue())[0].getValue();
			blueprintIdentifier = (String) ((Tag[]) subTags[24].getValue())[1].getValue();
		}
		if(subTags.length > 25 && subTags[25].getType() != Type.FINISH) {
			currentOwnerLowerCase = (String) subTags[25].getValue();
		}
		if(subTags.length > 26 && subTags[26].getType() != Type.FINISH) {
			lastDockerPlayerServerLowerCase = (String) subTags[26].getValue();
		}
		if(subTags.length > 27 && subTags[27].getType() != Type.FINISH) {
			// classification = BlueprintClassification.values()[subTags[27].getByte()];
		}
		if(subTags.length > 28 && subTags[28].getType() == Type.BYTE_ARRAY) {
			itemsToSpawnWith = new ElementCountMap();
			itemsToSpawnWith.readByteArray(subTags[28].getByteArray());
		}
		if(subTags.length > 29 && subTags[29].getType() == Type.BYTE) {
			if(subTags[29].getByte() != 0) {
				// old blueprint was spawned in database
				loadedFromChunk16 = true;
			}
		}
		if(subTags.length > 30 && subTags[30].getType() == Type.BYTE) {
			factionSetFromBlueprint = subTags[30].getByte() != 0;
		}
		if(tagVersion < (byte) 1 && subTags.length > 31 && subTags[31].getType() == Type.BYTE) {
			// Deprecated!!!! pull permission moved to ManagerContainer to save in blueprint
			// pullPermission = PullPermission.values()[subTags[31].getByte()];
		}
		if(subTags.length > 32 && subTags[32].getType() == Type.LONG) {
			lastAsked = subTags[32].getLong();
		}
		if(subTags.length > 33 && subTags[33].getType() == Type.LONG) {
			lastAllowed = subTags[33].getLong();
		}
		if(subTags.length > 34 && subTags[34].getType() == Type.BYTE) {
			usedOldPowerFromTag = subTags[34].getBoolean();
		} else {
			usedOldPowerFromTag = true;
		}
		if(subTags.length > 35 && subTags[35].getType() == Type.INT) {
			oldPowerBlocksFromBlueprint = subTags[35].getInt();
		}
		if(subTags.length > 36 && subTags[36].getType() == Type.SERIALIZABLE && this instanceof ManagedUsableSegmentController<?>) {
			((ManagedUsableSegmentController<?>) this).setBlockKillRecorder(BlockBuffer.fromTag(subTags[36]));
		}
		if(subTags.length > 37 && subTags[37].getType() == Type.LONG) {
			lastEditBlocks = subTags[37].getLong();
		}
		if(subTags.length > 38 && subTags[38].getType() == Type.LONG) {
			lastDamageTaken = subTags[38].getLong();
		}
		if(subTags.length > 39 && subTags[39].getType() == Type.LONG) {
			lastAdminCheckFlag = subTags[39].getLong();
		}
		if(subTags.length > 41 && subTags[41].getType() == Type.STRUCT) {
			try {
				quarterManager.fromTagStructure(subTags[41]);
			} catch(Exception ignored) {}
		}
		setChangedForDb(false);
		if(factionRights == -1 && currentOwnerLowerCase.trim().length() == 0) {
			this.factionRights = (byte) -2;
		} else {
			this.factionRights = factionRights;
		}
		((SegmentControllerHpController) hpController).checkOneHp();
	}

	public boolean hasIntegrityStructures() {
		return false;
	}

	@Override
	public int getFactionId() {
		if(railController.isDockedAndExecuted()) {
			int dockedFactionId = railController.getDockedFactionId(super.getFactionId());
			if(((FactionState) getState()).getFactionManager().existsFaction(super.getFactionId()) && !((FactionState) getState()).getFactionManager().existsFaction(dockedFactionId)) {
				/*
				 * don't overwrite faction with NEUTRAL or a faction that
				 * doesn't exist if this are part of an existing faction
				 */
				return super.getFactionId();
			}
			return dockedFactionId;
		}
		return super.getFactionId();
	}

	@Override
	public void setFactionId(int factionId) {
        // INSERTED CODE
        SegmentControllerChangeFactionEvent ev = new SegmentControllerChangeFactionEvent(this,this.getFactionId(),factionId);
        StarLoader.fireEvent(ev,this.isOnServer());
        if(ev.isCanceled()) return;
        else factionId = ev.getNewFaction();
        ///
		if(isOnServer() && factionId != this.getFactionId()) {
			if(this.getFactionId() != 0) {
				factionRights = (byte) -2;
			}
		}
		super.setFactionId(factionId);
	}

	/**
	 * @return the id
	 */
	@Override
	public final int getId() {
		return id;
	}

	@Override
	public abstract NetworkSegmentController getNetworkObject();

	@Override
	public boolean isHomeBaseFor(int forFactionId) {
		if(dockingController.isDocked()) {
			return dockingController.getAbsoluteMother().isHomeBaseFor(dockingController.getLocalMother().getFactionId());
		} else if(railController.isDockedAndExecuted()) {
			if(railController.getRoot() instanceof ShopSpaceStation) {
				return true;
			}
			return railController.getRoot().isHomeBaseFor(forFactionId);
		} else {
			return super.isHomeBaseFor(forFactionId);
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#onPhysicsAdd()
	 */
	@Override
	public void onPhysicsAdd() {
		super.onPhysicsAdd();
		if(isOnServer()) {
			vServerAttachment.update();
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#onPhysicsRemove()
	 */
	@Override
	public void onPhysicsRemove() {
		super.onPhysicsRemove();
		if(isOnServer()) {
			vServerAttachment.update();
		}
	}

	/**
	 * @param id the id to set
	 */
	@Override
	public void setId(int id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#toTagStructure()
	 */
	@Override
	public Tag toTagStructure() {
		Tag idTag = new Tag(Type.STRING, "uniqueId", this.uniqueIdentifier);
		Tag creatorID = new Tag(Type.INT, "creatoreId", this.getCreatorId());
		// System.err.println("Written crator ID: "+this.getCreatorId()+" on "+this);
		Tag minPosTag = new Tag(Type.VECTOR3i, "minPos", this.minPos);
		Tag maxPosTag = new Tag(Type.VECTOR3i, "maxPos", this.maxPos);
		Tag realNameTag = new Tag(Type.STRING, "realname", getRealName());
		Tag docking = dockingController.toTagStructure();
		Tag managerContainerTag = null;
		if(this instanceof ManagedSegmentController) {
			managerContainerTag = ((ManagedSegmentController<?>) this).getManagerContainer().toTagStructure();
		} else {
			managerContainerTag = new Tag(Type.BYTE, "dummy", (byte) 0);
		}
		Tag controllerTag = controlElementMap.toTagStructure();
		Tag spawnerTag = new Tag(Type.STRING, null, spawner != null ? spawner : "");
		Tag lastModifierTag = new Tag(Type.STRING, null, lastModifier != null ? lastModifier : "");
		Tag seed = new Tag(Type.LONG, null, this.seed);
		Tag touched;
		if(this instanceof TransientSegmentController) {
			touched = new Tag(Type.BYTE, null, ((TransientSegmentController) this).isTouched() ? (byte) 1 : (byte) 0);
		} else {
			touched = new Tag(Type.BYTE, null, (byte) 1);
		}
		// Tag factionRightsTag = Tag.listToTagStruct(factionRights, null);
		Tag railTag = railController.getTag();
		Tag nonemptySegments = new Tag(Type.INT, null, segmentBuffer.getTotalNonEmptySize());
		long timeLeft = -1;
		if(coreTimerStarted > 0) {
			long timeCoreTimerRunning = System.currentTimeMillis() - coreTimerStarted;
			timeLeft = coreTimerDuration - timeCoreTimerRunning;
		}
		Tag overHeatingTag = new Tag(Type.STRUCT, null, new Tag[] {new Tag(Type.LONG, null, timeLeft), FinishTag.INST});
		Tag virtualBBTag = new Tag(Type.BYTE, null, virtualBlueprint ? (byte) 1 : (byte) 0);
		Tag blueprintLoadTag = isLoadByBlueprint() ? new Tag(Type.STRUCT, null, new Tag[] {new Tag(Type.STRING, null, blueprintSegmentDataPath), new Tag(Type.STRING, null, blueprintIdentifier), FinishTag.INST}) : new Tag(Type.BYTE, null, (byte) 1);
		return new Tag(Type.STRUCT, "s3", new Tag[] { // 0
			idTag, // 1
			minPosTag, maxPosTag, docking, controllerTag, realNameTag, // 6
			super.toTagStructure(), // 7
			managerContainerTag, // 8
			creatorID, spawnerTag, lastModifierTag, // 11
			seed, touched, getExtraTagData(), // 14
			getNPCTagData(), new Tag(Type.BYTE, null, scrap ? (byte) 1 : (byte) 0), new Tag(Type.BYTE, null, isVulnerable() ? (byte) 1 : (byte) 0), new Tag(Type.BYTE, null, isMinable() ? (byte) 1 : (byte) 0), new Tag(Type.BYTE, null, factionRights), // 19
			railTag, nonemptySegments, hpController.toTagStructure(), overHeatingTag, virtualBBTag, blueprintLoadTag, new Tag(Type.STRING, null, currentOwnerLowerCase), new Tag(Type.STRING, null, lastDockerPlayerServerLowerCase), // placeholder
			new Tag(Type.BYTE, null, (byte) 0), itemsToSpawnWith != null ? new Tag(Type.BYTE_ARRAY, null, itemsToSpawnWith.getByteArray()) : new Tag(Type.BYTE, null, (byte) 0), new Tag(Type.BYTE, null, spawnedInDatabaseAsChunk16 ? (byte) 1 : (byte) 0), new Tag(Type.BYTE, null, factionSetFromBlueprint ? (byte) 1 : (byte) 0), // placeholder
			new Tag(Type.BYTE, null, (byte) 0), new Tag(Type.LONG, null, lastAsked), new Tag(Type.LONG, null, lastAllowed), new Tag(Type.BYTE, null, (isUsingOldPower() || usedOldPowerFromTagForcedWrite) ? (byte) 1 : (byte) 0), new Tag(Type.INT, null, oldPowerBlocksFromBlueprint), (this instanceof ManagedUsableSegmentController<?>) ? ((ManagedUsableSegmentController<?>) this).getBlockKillRecorder().getTag() : new Tag(Type.BYTE, null, (byte) 0), new Tag(Type.LONG, null, lastEditBlocks), new Tag(Type.LONG, null, lastDamageTaken), new Tag(Type.LONG, null, lastAdminCheckFlag), new Tag(Type.BYTE, null, TAG_VERSION), quarterManager.toTagStructure(), FinishTag.INST});
	}

	@Override
	public boolean isGravitySource() {
		return personalGravitySwitch;
	}

	public boolean completedFirstUpdate = false;

	@Override
	public void updateLocal(Timer timer) {
		getState().getDebugTimer().start(this, "SegmentController");
		if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()) {
			drawDebugTransform();
		}
		boolean hasGrav = configManager.apply(StatusEffectType.GRAVITY_OVERRIDE_ENTITY_SWITCH, false);
		if(hasGrav != personalGravitySwitch) {
			personalGravity.x = 0;
			personalGravity.y = 1;
			personalGravity.z = 0;
			if(hasGrav) {
				personalGravity.y = configManager.apply(StatusEffectType.GRAVITY_OVERRIDE_ENTITY_DIR, 1f);
			} else {
				personalGravity.x = 0;
				personalGravity.y = 0;
				personalGravity.z = 0;
			}
			personalGravitySwitch = hasGrav;
			((GravityStateInterface) getState()).getCurrentGravitySources().remove(this);
			if(isGravitySource()) {
				((GravityStateInterface) getState()).getCurrentGravitySources().add(this);
			}
		}
		if(flagAnyDamageTakenServer && getState().getUpdateTime() - lastAttackTrigger > 5000) {
			ruleEntityManager.triggerOnAttack();
			flagAnyDamageTakenServer = false;
			lastAttackTrigger = getState().getUpdateTime();
		}
		float massMod = configManager.apply(StatusEffectType.MASS_MOD, 1f);
		if(massMod != this.massMod) {
			this.massMod = massMod;
			flagupdateMass();
		}
		// long t0 = System.currentTimeMillis();
		super.updateLocal(timer);
		getEffectContainer().reset();
		getEffectContainer().update(configManager);
		updateCoreOverheating(timer, getState().getUpdateTime());
		blockTypeSearchManager.update(timer);
		railController.update(timer);
		railController.updateChildPhysics(timer);
		quarterManager.update(timer);
		if(blockAdded) {
			onAfterBlockAddedOnUpdateLocal();
			blockAdded = false;
		}
		if(blockRemoved) {
			onAfterBlockRemovedOnUpdateLocal();
			blockRemoved = false;
		}
		if(onMassUpdate) {
			if(isOnServer()) {
				ruleEntityManager.triggerOnMassUpdate();
			}
			onMassUpdate = false;
		}
		// if(getUniqueIdentifier().equals("ENTITY_SHIP_Large ship that moves_1434735214659")){
		// System.err.println("-------------"+getState()+" POS::: "+getWorldTransform().origin+";        ; "+getPhysicsDataContainer().getObject().getWorldTransform(new Transform()).origin+"; LAST: "+getPhysicsDataContainer().lastCenter+"; CUR: "+((CubesCompoundShape)getPhysicsDataContainer().getShape()).getCenterOfMass());
		// }
		if(!onFullyLoadedExcecuted && segmentBuffer.isFullyLoaded()) {
			onFullyLoaded();
			onFullyLoadedExcecuted = true;
		}
		boolean transformChanged = true;
		if(flagLongStuck) {
			handleLongStuck();
			flagLongStuck = false;
		}
		handleTriggers(timer.currentTime);
		handleSlowdown(timer.currentTime);
		if(isOnServer()) {
			try {
				((GameServerState) getState()).debugController.check(this);
			} catch(Exception e) {
				if(((GameServerState) getState()).getTimedShutdownStart() <= 0) {
					e.printStackTrace();
					System.err.println("[SERVER] Exception: TERMINATE! SHUTTING DOWN: DEBUG OPERATION REQUESTED SHUTDOWN TO PRESERVE LOG!");
					((GameServerState) getState()).getController().broadcastMessage(Lng.astr("DEBUG OPERATION CAUSING SHUTDOWN. PLEASE SEND IN LOGS!"), ServerMessage.MESSAGE_TYPE_SIMPLE);
					((GameServerState) getState()).addTimedShutdown(120);
					railController.setDebugFlag(true);
				} else {
					if(System.currentTimeMillis() - lastExceptionPrint > 3000) {
						System.err.println("(only showing 1 per 3 sec) FOLLOWUP EXCEPTION: " + e.getMessage());
						lastExceptionPrint = System.currentTimeMillis();
						railController.setDebugFlag(true);
					}
				}
			}
			if(!checkedRootDb && updateCounter > 100 && this.dbId > 0 && this instanceof Ship) {
				// this will trigger once and check if there are any docks missing
				checkedRootDb = true;
				if(railController.isRoot()) {
					Sector sec = ((GameServerState) getState()).getUniverse().getSector(getSectorId());
					List<EntityUID> list = ((GameServerState) getState()).getDatabaseIndex().getTableManager().getEntityTable().loadByDockedEntity(this.dbId);
					if(list != null && !list.isEmpty()) {
						int c = 0;
						for(EntityUID docked : list) {
							if(!((GameServerState) getState()).getLocalAndRemoteObjectContainer().getDbObjects().containsKey(docked.id)) {
								Sendable dock;
								try {
									((GameServerState) getState()).getDatabaseIndex().getTableManager().getEntityTable().changeSectorForEntity(docked.id, sec.pos, new Vector3f(getWorldTransform().origin), true);
									dock = sec.loadEntitiy((GameServerState) getState(), docked);
									((GameServerState) getState()).getController().broadcastMessageAdmin(Lng.astr("[ADMIN] (NON FATAL) WARNING: Dock for %s was not in\nthe right sector.\n It has been moved to its mothership.\nPlease send in logs since this fallback shouldn't happen.", this.toNiceString()), ServerMessage.MESSAGE_TYPE_ERROR);
									try {
										throw new Exception("WARNING (non critical): Docked entity of " + this + " UID: " + this.uniqueIdentifier + "; SECTOR: " + sec.pos + " WAS NOT LOADED PHYSICALLY. Loaded (and possibly moved) it forcefully! DOCK #" + c + ": " + dock + "; UID: " + ((SegmentController) dock).uniqueIdentifier);
									} catch(Exception e) {
										e.printStackTrace();
									}
									assert (dock instanceof SegmentController && ((SegmentController) dock).getSectorId() == this.getSectorId()) : ((SegmentController) dock).getSectorId() + " != " + this.getSectorId();
								} catch(SQLException e) {
									e.printStackTrace();
								}
								c++;
							}
						}
					}
				}
			}
		}
		if(isOnServer() && virtualBlueprint) {
			if(checkVirtualDock == 0) {
				checkVirtualDock = timer.currentTime;
			} else if(checkVirtualDock > 0 && timer.currentTime - checkVirtualDock > 60000) {
				if(!railController.isDockedAndExecuted()) {
					sendControllingPlayersServerMessage(Lng.astr("INVALID DESIGN: REMOVING"), ServerMessage.MESSAGE_TYPE_ERROR);
					setMarkedForDeleteVolatile(true);
					markForPermanentDelete(true);
				}
				checkVirtualDock = -1;
			}
		}
		if(isOnServer() && factionSetFromBlueprint) {
			if(railController.isDockedAndExecuted()) {
				factionSetFromBlueprint = false;
			} else {
				if(railController.isFullyLoadedRecursive()) {
					if(this instanceof ManagedSegmentController<?>) {
						ManagerContainer<?> c = ((ManagedSegmentController<?>) this).getManagerContainer();
						if(c.getFactionBlockPos() == Long.MIN_VALUE) {
							// ship was spawned with faction but doesnt have a faction
							// block after being fully loaded
							if(getFactionId() > 0 && !FactionManager.isNPCFaction(getFactionId())) {
								// reset only for player ships
								railController.setFactionIdForEntitiesWithoutFactionBlock(0);
							}
						}
					}
					factionSetFromBlueprint = false;
				}
			}
		}
		if(!isOnServer() && askForPullClient != 0) {
			if(isClientOwnObject()) {
				final long toSend = askForPullClient;
				(new PlayerOkCancelInput("CONFIRM", (InputState) getState(), 300, 100, Lng.str("Pull Permission"), Lng.str("Entity %s wants to pull items via your rail blocks. Allow?", getState().getLocalAndRemoteObjectContainer().getDbObjects().get(toSend).toString())) {
					@Override
					public void pressedOK() {
						getNetworkObject().pullPermissionAskAnswerBuffer.add(toSend);
						deactivate();
					}

					@Override
					public void cancel() {
						getNetworkObject().pullPermissionAskAnswerBuffer.add(-toSend);
						super.cancel();
					}

					@Override
					public void onDeactivate() {
					}
				}).activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(939);
			}
			askForPullClient = 0;
		}
		dockingController.updateLocal(timer);
		assert (uniqueIdentifier != null);
		if(transformChanged) {
			updateInverseTransform();
		}
		// FIXME: ugly checking for floating rock (this can be removed after the rail system is in, since old docking is no longer used)
		if(flagCheckDocking && !(this instanceof FloatingRock) && getPhysicsDataContainer().isInitialized() && (timer.currentTime - delayDockingCheck > 2000)) {
			try {
				if(dockingController.isDocked()) {
					if(isOnServer()) {
						SegmentController.dockingChecks++;
					}
				}
				dockingController.checkDockingValid();
				flagCheckDocking = false;
				delayDockingCheck = timer.currentTime;
			} catch(CollectionNotLoadedException e) {
				// System.err.println("[SEGMENT-CONTROLLER] Cannot validate docking yet, because enhancers aren't fully loaded -> DELAY by 1 sec");
				delayDockingCheck = timer.currentTime;
			}
		}
		if(!isOnServer()) {
			while(!needsActiveUpdateClient.isEmpty()) {
				SegmentPiece activationChangedPiece = needsActiveUpdateClient.dequeue();
				// no info on client. block is gonna deactivate by checking its own activation
				long fromActivationBlockServer = Long.MIN_VALUE;
				(((ManagedSegmentController<?>) this)).getManagerContainer().handleActivateBlockActivate(activationChangedPiece, fromActivationBlockServer, !activationChangedPiece.isActive(), timer);
			}
		}
		hpController.updateLocal(timer);
		if(aabbRecalcFlag && getPhysicsDataContainer().isInitialized()) {
			if(railController.isDocked()) {
				railController.getRoot().aabbRecalcFlag();
			}
			flagupdateMass();
			((CompoundShape) getPhysicsDataContainer().getShape()).recalculateLocalAabb();
			recalcBoundingSphere();
			aabbRecalcFlag = false;
			if(isOnServer()) {
				ruleEntityManager.triggerOnBBUpdate();
			}
		}
		if(flagSegmentBufferAABBUpdate && getPhysicsDataContainer().isInitialized()) {
			if(getPhysicsDataContainer().getObject() != null) {
				getPhysicsDataContainer().getObject().activate(true);
			}
			aabbRecalcFlag();
			flagSegmentBufferAABBUpdate = false;
		}
		if(!isOnServer() && Controller.getCamera() != null) {
			// if(transformChanged){
			// switch(SegmentDrawer.distanceMode){
			// case SegmentDrawer.DISTANCE_VIEWER:
			//
			// n.set(Controller.getCamera().getViewable().getPos());
			//
			// break;
			// case SegmentDrawer.DISTANCE_CAMERA:
			camLocalTmp.set(Controller.getCamera().getPos());
			// break;
			// }
			if(transformChanged) {
				clientTransformInverse.set(getWorldTransformOnClient());
				clientTransformInverse.inverse();
			}
			clientTransformInverse.transform(camLocalTmp);
			camPosLocal.set(camLocalTmp);
			if(Controller.getCamera() instanceof SegmentControllerCamera && ((SegmentControllerCamera) Controller.getCamera()).getSegmentController() == this) {
				camForwLocal.set(Controller.getCamera().getCachedForward());
				camLeftLocal.set(Controller.getCamera().getCachedRight());
				camLeftLocal.negate();
				camUpLocal.set(Controller.getCamera().getCachedUp());
				getWorldTransformInverse().basis.transform(camForwLocal);
				getWorldTransformInverse().basis.transform(camLeftLocal);
				getWorldTransformInverse().basis.transform(camUpLocal);
			}
			// }
		}
		// if(!lastTransform.equals(getWorldTransform())){
		// }
		segmentProvider.update(isOnServer() ? null : ((GameClientState) getState()).getController().getCreatorThreadController().clientQueueManager);
		// if(getState() instanceof ClientStateInterface){
		// AbstractScene.infoList.add(this+" "+getState()+" update percentage: "+(System.currentTimeMillis() - t));
		// }
		getPhysicsState().handleNextPhysicsSubstep(0);
		segmentBuffer.update();
		lastSector = getSectorId();
		updateCounter++;
		completedFirstUpdate = true;
		getState().getDebugTimer().end(this, "SegmentController");
	}

	private void onAfterBlockAddedOnUpdateLocal() {
		if(isOnServer()) {
			ruleEntityManager.triggerOnBlockBuild();
		}
	}

	private void onAfterBlockRemovedOnUpdateLocal() {
		if(isOnServer()) {
			ruleEntityManager.triggerOnRemoveBlock();
		}
	}

	public void updateInverseTransform() {
		getWorldTransformInverse().set(getWorldTransform());
		// reverse the transformation, the physics did into local space
		getWorldTransformInverse().inverse();
	}

	private void recalcBoundingSphere() {
		getBoundingSphere().radius = 0;
		getBoundingSphereTotal().radius = 0;
		getBoundingSphere().setFrom(segmentBuffer.getBoundingBox());
		if(railController.isRoot()) {
			railController.calcBoundingSphereTotal(getBoundingSphereTotal());
		}
	}

	protected void onFullyLoaded() {
		if(this instanceof ManagedSegmentController<?>) {
			((ManagedSegmentController<?>) this).getManagerContainer().onFullyLoaded();
		}
		//INSERTED CODE
		SegmentControllerFullyLoadedEvent event = new SegmentControllerFullyLoadedEvent(this);
		StarLoader.fireEvent(event, this.isOnServer());
		///
	}

	@Override
	protected boolean addToPhysicsOnInit() {
		return !railController.hasActiveDockingRequest();
	}

	@Override
	public void onSmootherSet(Transform t) {
		dockingController.onSmootherSet(t);
	}

	/**
	 * @return the realName
	 */
	@Override
	public String getRealName() {
		return realName;
	}

	/**
	 * @param realName the realName to set
	 */
	public void setRealName(String realName) {
		if(!realName.equals(this.realName)) {
			setChangedForDb(true);
		}
		this.realName = realName;
	}

	@Override
	public boolean isPhysicalForDamage() {
		return !virtualBlueprint;
	}

	@Override
	public void executeGraphicalEffectClient(byte b) {
		assert (!isOnServer());
		GameClientState state = (GameClientState) getState();
		if(state.getWorldDrawer() != null) {
			state.getWorldDrawer().getSegmentControllerEffectDrawer().startEffect(this, b);
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#getInfo()
	 */
	@Override
	public String getInfo() {
		return "LoadedEntity [uid=" + uniqueIdentifier + ", type=" + getType().name() + ", seed=" + seed + ", lastModifier=" + lastModifier + ", spawner=" + spawner + ", realName=" + realName + ", touched=" + (this instanceof TransientSegmentController ? ((TransientSegmentController) this).isTouched() : "true") + ", faction=" + getFactionId() + ", pos=" + getWorldTransform().origin + ", minPos=" + minPos + ", maxPos=" + maxPos + ", creatorID=" + getCreatorId() + ", emptyObject=" + isEmptyOnServer() + "]";
	}

	@Override
	public boolean isChainedToSendFromClient() {
		return railController.isChainSendFromClient();
	}

	/**
	 * @return the vulnerable
	 */
	@Override
	public boolean isVulnerable() {
		return vulnerable;
	}

	/**
	 * @param vulnerable the vulnerable to set
	 */
	public void setVulnerable(boolean vulnerable) {
		this.vulnerable = vulnerable;
	}

	/**
	 * @return the minable
	 */
	public boolean isMinable() {
		return minable;
	}

	/**
	 * @param minable the minable to set
	 */
	public void setMinable(boolean minable) {
		this.minable = minable;
	}

	/**
	 * @return the factionRights
	 */
	@Override
	public byte getFactionRights() {
		return factionRights;
	}

	@Override
	public byte getOwnerFactionRights() {
		AbstractOwnerState ownerState = getOwnerState();
		if(ownerState != null) {
			return ownerState.getFactionRights();
		}
		return factionRights;
	}

	/**
	 * @param factionRights the factionRights to set
	 */
	public void setFactionRights(byte factionRights) {
		this.factionRights = factionRights;
	}

	public abstract boolean isEmptyOnServer();

	public abstract void sendBlockActivation(long encodeActivation);

	public abstract void sendBlockMod(RemoteSegmentPiece mod);

	public abstract void sendBeamLatchOn(long beamId, int objId, long blockPos);

	public abstract void sendBlockHpByte(int x, int y, int z, short hp);

	public abstract void sendBlockSalvage(int x, int y, int z);

	public abstract void sendBlockKill(SegmentPiece p);

	public abstract void sendBlockHpByte(SegmentPiece p, short hp);

	public abstract void sendBlockServerMessage(ServerMessage m);

	public abstract void sendBlockSalvage(SegmentPiece p);

	public void drawPosition(Vector3i p, float margin, Vector4f c) {
		DebugBox b = new DebugBox(new Vector3f(((p.x - SegmentData.SEG_HALF) - 0.5f) - margin, ((p.y - SegmentData.SEG_HALF) - 0.5f) - margin, ((p.z - SegmentData.SEG_HALF) - 0.5f) - margin), new Vector3f(((p.x - SegmentData.SEG_HALF) + 0.5f) + margin, ((p.y - SegmentData.SEG_HALF) + 0.5f) + margin, ((p.z - SegmentData.SEG_HALF) + 0.5f) + margin), isOnServer() ? getWorldTransform() : getWorldTransformOnClient(), c.x, c.y, c.z, c.w);
		DebugDrawer.boxes.add(b);
	}

	/**
	 * @return the slotAssignment
	 */
	public SlotAssignment getSlotAssignment() {
		return slotAssignment;
	}

	public boolean isCloakedFor(SimpleTransformableSendableObject<?> viewer) {
		return false;
	}

	public boolean isJammingFor(SimpleTransformableSendableObject<?> viewer) {
		return false;
	}

	/**
	 * @return the centerOfMass
	 */
	public Vector3f getCenterOfMassUnweighted() {
		return centerOfMassUnweighted;
	}

	/**
	 * @return the totalPhysicalMass
	 */
	public float getTotalPhysicalMass() {
		return configManager.apply(StatusEffectType.MASS_MOD, totalPhysicalMass);
	}

	/**
	 * @return the hpController
	 */
	public SegmentControllerHpControllerInterface getHpController() {
		return hpController;
	}

	public boolean isRankAllowedToChangeFaction(int targetFactionId, PlayerState player, byte rank) {
		return !((FactionState) getState()).getFactionManager().existsFaction(getFactionId()) || isSufficientFactionRights(player);
	}

	public boolean isVirtualBlueprint() {
		return this.virtualBlueprint;
	}

	public void setVirtualBlueprint(boolean virtualBlueprint) {
		this.virtualBlueprint = virtualBlueprint;
	}

	public void setVirtualBlueprintRecursive(boolean virtualBlueprint) {
		this.virtualBlueprint = virtualBlueprint;
		for(RailRelation r : railController.next) {
			r.docked.getSegmentController().setVirtualBlueprintRecursive(virtualBlueprint);
		}
	}

	public void setMarkedForDeleteVolatileIncludingDocks(boolean b) {
		setMarkedForDeleteVolatile(b);
		for(RailRelation r : railController.next) {
			r.docked.getSegmentController().setMarkedForDeleteVolatileIncludingDocks(b);
		}
	}

	public void setMarkedForDeletePermanentIncludingDocks(boolean b) {
		setMarkedForDeleteVolatile(b);
		markForPermanentDelete(b);
		for(RailRelation r : railController.next) {
			r.docked.getSegmentController().setMarkedForDeletePermanentIncludingDocks(b);
		}
	}

	public boolean checkBlockMassServerLimitOk() {
		return (((GameStateInterface) getState()).getGameState().isBlocksOk(this, totalElements) && (((GameStateInterface) getState()).getGameState().isMassOk(this, getMassWithoutDockIncludingStation())));
	}

	@Override
	public String getWriteUniqueIdentifier() {
		return uniqueIdentifier;
	}

	@Override
	public String getReadUniqueIdentifier() {
		if(isLoadByBlueprint()) {
			// System.err.println("READING FROM BB '"+blueprintIdentifier+"'; "+getBlueprintSegmentDataPath());
			return blueprintIdentifier;
		} else {
			return uniqueIdentifier;
		}
	}

	@Override
	public boolean isLoadByBlueprint() {
		return blueprintSegmentDataPath != null && blueprintIdentifier != null;
	}

	@Override
	public String getBlueprintSegmentDataPath() {
		return blueprintSegmentDataPath;
	}

	public void sendSectorBroadcast(Object[] astr, byte messageType) {
		assert (isOnServer());
		((GameServerState) getState()).getController().broadcastMessageSector(astr, messageType, getSectorId());
	}

	public void setAllTouched(boolean b) {
		if(this instanceof TransientSegmentController && !((TransientSegmentController) this).isTouched()) {
			((TransientSegmentController) this).setTouched(b, false);
		}
		for(RailRelation r : railController.next) {
			r.docked.getSegmentController().setAllTouched(b);
		}
	}

	public float getLinearDamping() {
		return getPhysics().getState().getLinearDamping();
	}

	public float getRotationalDamping() {
		return getPhysics().getState().getRotationalDamping();
	}

	public boolean isOwnerSpecific(PlayerState player) {
		return factionRights == FactionRoles.PERSONAL_RANK && currentOwnerLowerCase.equals(player.getName().toLowerCase(Locale.ENGLISH));
	}

	public boolean isOwnerWithoutFactionCheck(PlayerState player) {
		return factionRights == FactionRoles.PERSONAL_RANK && (currentOwnerLowerCase.length() == 0 || isOwnerSpecific(player));
	}

	public boolean isSufficientFactionRights(PlayerState player) {
		return factionRights == FactionRoles.NOT_SET_RANK || ((factionRights >= 0 && player.getFactionRights() >= factionRights) || isOwnerWithoutFactionCheck(player));
	}

	public void setRankRecursive(byte rank, PlayerState p, boolean popup) {
		setRankRecursive(rank, p, popup, 0);
	}

	private void setRankRecursive(byte rank, PlayerState p, boolean popup, int deepness) {
		boolean ok = false;
		if(rank >= 0 && factionRights == FactionRoles.PERSONAL_RANK && deepness == 0 && isOwnerSpecific(p)) {
			// setting from personal to normal as owner only works on first in tree
			ok = true;
		} else if(factionRights == FactionRoles.NOT_SET_RANK) {
			// no rank set
			ok = true;
		} else if(factionRights >= FactionRoles.LOWEST_RANK && p.getFactionRights() >= factionRights) {
			// normal rank check
			ok = true;
		}
		if(!isOwnerSpecific(p) && getFactionId() != 0 && p.getFactionId() != getFactionId()) {
			// only the owner can change rank of an object if he is not part of the object's faction
			ok = false;
		}
		if(ok) {
			factionRights = rank;
			currentOwnerLowerCase = (p.getName().toLowerCase(Locale.ENGLISH));
			if(popup) {
				p.sendServerMessagePlayerInfo(Lng.astr("Rank Set! Only rank\n'%s'\n%smay edit this structure", p.getFactionRankName(rank), (rank < 4 ? "or better " : "")));
			}
		} else {
			if(rank >= 0 && factionRights == FactionRoles.PERSONAL_RANK && deepness > 0 && isOwnerSpecific(p)) {
			} else {
				p.sendServerMessagePlayerError(Lng.astr("%s:\nYou don't have the\nright to change permission\nto this value!", getName()));
			}
		}
		if(ok) {
			// only continue for docked objects if sucessfully changed rank
			for(RailRelation r : railController.next) {
				r.docked.getSegmentController().setRankRecursive(rank, p, false, deepness + 1);
			}
		}
	}

	public int getTotalElementsIncRails() {
		int elements = totalElements;
		for(RailRelation r : railController.next) {
			elements += r.docked.getSegmentController().getTotalElementsIncRails();
		}
		return elements;
	}

	public boolean canBeRequestedOnClient(int x, int y, int z) {
		return isInboundAbs(x, y, z);
	}

	public IntSet getProximityObjects() {
		return proximityObjects;
	}

	public boolean isInFleet() {
		return getFleet() != null;
	}

	public Fleet getFleet() {
		return ((FleetStateInterface) getState()).getFleetManager().getByEntity(this);
	}

	@Override
	public long getDbId() {
		return dbId;
	}

	@Override
	public void initialize() {
		super.initialize();
		configManager = new ConfigEntityManager(dbId, EffectEntityType.STRUCTURE, ((ConfigPoolProvider) this.getState()));
		try {
			configManager.entityName = toNiceString();
		} catch(Exception e) {
			configManager.entityName = toString();
		}
		if(isOnServer()) {
			configManager.loadFromDatabase((GameServerState) getState());
		}
	}

	public boolean isLoadedFromChunk16() {
		return loadedFromChunk16;
	}

	public void setLoadedFromChunk16(boolean loadedFromChunk16) {
		this.loadedFromChunk16 = loadedFromChunk16;
	}

	@Override
	public void sendServerMessage(Object[] astr, byte msgType) {
		if(isOnServer() && this instanceof PlayerControllable) {
			if(((PlayerControllable) this).getAttachedPlayers().size() == 0 && railController.isDockedAndExecuted()) {
				railController.getRoot().sendServerMessage(astr, msgType);
				return;
			}
			for(PlayerState s : ((PlayerControllable) this).getAttachedPlayers()) {
				s.sendServerMessage(new ServerMessage(astr, msgType));
			}
		}
	}

	public void sendServerMessage(String untranslatedMessage, byte msgType) {
		sendServerMessage(new Object[] {untranslatedMessage}, msgType);
	}

	@Override
	public void sendClientMessage(String str, byte type) {
		if(!isOnServer() && this instanceof PlayerControllable) {
			if(((PlayerControllable) this).getAttachedPlayers().size() == 0 && railController.isDockedAndExecuted()) {
				railController.getRoot().sendClientMessage(str, type);
				return;
			}
			for(PlayerState s : ((PlayerControllable) this).getAttachedPlayers()) {
				if(s.isClientOwnPlayer()) {
					switch(type) {
						case (ServerMessage.MESSAGE_TYPE_INFO) -> ((GameClientState) getState()).getController().popupInfoTextMessage(str, 0);
						default -> ((GameClientState) getState()).getController().popupAlertTextMessage(str, 0);
					}
					return;
				}
			}
		}
	}

	public boolean isSpawnedInDatabaseAsChunk16() {
		return spawnedInDatabaseAsChunk16;
	}

	public void setSpawnedInDatabaseAsChunk16(boolean spawnedInDatabaseAsChunk16) {
		this.spawnedInDatabaseAsChunk16 = spawnedInDatabaseAsChunk16;
	}

	public void onStuck() {
		if(isOnServer()) {
			if(System.currentTimeMillis() - lastStuck > 15000) {
				stuckTime = System.currentTimeMillis();
				stuckCount = 0;
			} else {
				stuckCount += (System.currentTimeMillis() - stuckTime);
				System.err.println("############### OBJECT STUCK FOR SECONDS: " + stuckCount / 1000L);
			}
			if(stuckCount > 10000) {
				lastStuck = 0;
				stuckCount = 0;
				flagLongStuck = true;
			} else {
				lastStuck = System.currentTimeMillis();
			}
		}
	}

	public void saveDebugRail() {
		assert (isOnServer());
		((GameServerState) getState()).debugController.saveRail(this);
	}

	private void handleLongStuck() {
		if(isOnServer()) {
			System.err.println("[SERVER][PHYSICS] LAG HANDLING ############### START " + this);
			Transform to = new Transform(getWorldTransform());
			avoid(to, false);
			System.err.println("[SERVER][PHYSICS] LAG HANDLING ############### WARPING " + this + " to a position without a collision: " + getWorldTransform().origin + " -> " + to.origin);
			warpTransformable(to, true, true, null);
		}
	}

	@Override
	public void registerTransientEffects(List<ConfigProviderSource> transientEffectSources) {
		if(this instanceof ManagedSegmentController<?>) {
			((ManagedSegmentController<?>) this).getManagerContainer().registerTransientEffetcs(transientEffectSources);
		}
		RemoteSector remoteSector = getRemoteSector();
		if(remoteSector != null) {
			transientEffectSources.add(remoteSector);
		}
	}

	public RemoteSector getRemoteSector() {
		Sendable sendable = getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(getSectorId());
		if(sendable instanceof RemoteSector) {
			return ((RemoteSector) sendable);
		} else {
			return null;
		}
	}

	public void setFactionFromBlueprint(boolean b) {
		factionSetFromBlueprint = b;
	}

	public void onRevealingAction() {
	}

	/**
	 * Only called on server.
	 */
	public void onFTLJump() {
	}

	@Override
	public TopLevelType getTopLevelType() {
		return TopLevelType.SEGMENT_CONTROLLER;
	}

	public boolean isAllowedToTakeItemsByRail(SegmentController segmentController) {
		switch(pullPermission) {
			case ALWAYS:
				return true;
			case FACTION:
				if(getFactionId() != 0 && getFactionId() == segmentController.getFactionId()) {
					return true;
				}
			case ASK:
				if(lastAsked != segmentController.dbId) {
					getNetworkObject().pullPermissionAskAnswerBuffer.add(segmentController.dbId);
					lastAsked = segmentController.dbId;
					railController.onUndock(previous -> lastAsked = 0);
				}
				return lastAllowed == segmentController.dbId;
			case NEVER:
				return false;
			default:
				throw new IllegalArgumentException("Unknown pull permission " + pullPermission.name());
		}
	}

	@Override
	public ConfigEntityManager getConfigManager() {
		return configManager;
	}

	public boolean hasActiveReactors() {
		// isusingpowerreactors checked in powerimplmentation hasActiveReactors()
		return ((this instanceof ManagedSegmentController<?>) && ((ManagedSegmentController<?>) this).getManagerContainer().hasActiveReactors());
	}

	public boolean isUsingPowerReactors() {
		return !isUsingOldPower() || ((this instanceof ManagedSegmentController<?>) && ((ManagedSegmentController<?>) this).getManagerContainer().isUsingPowerReactors());
	}

	@Override
	public float getReconStrengthRaw() {
		if(this instanceof ManagedSegmentController<?>) {
			ManagerModuleCollection<StructureScannerUnit, StructureScannerCollectionManager, StructureScannerElementManager> scanSystems = ((ManagedSegmentController<?>) this).getManagerContainer().getShortRangeScanner();
			if(scanSystems == null) return 0;
			else{
				StructureScannerCollectionManager scanner = scanSystems.getElementManager().getCollection();
				return scanner == null ? 0 : scanner.getTotalActiveScanStrength();
			}
		} else {
			return 0;
		}
	}

	@Override
	public float getStealthStrength() {
		if(this instanceof ManagedSegmentController<?>) {
			ManagerModuleCollection<StealthUnit, StealthCollectionManager, StealthElementManager> stealthSystems = ((ManagedSegmentController<?>) this).getManagerContainer().getStealth();
			if(stealthSystems == null) return 0;
			else return stealthSystems.getElementManager().getTotalStealthStrength();
		} else {
			return 0;
		}
	}

	@Override
	public boolean hasStealth(StealthLvl lvl) {
		if(this instanceof ManagedSegmentController<?>) {
			ManagerModuleCollection<StealthUnit, StealthCollectionManager, StealthElementManager> stealthSystems = ((ManagedSegmentController<?>) this).getManagerContainer().getStealth();
			return stealthSystems.getElementManager().hasStealthCapability(lvl);
		} else {
			return false;
		}
	}

	public boolean isUsingLocalShields() {
		if(this instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) railController.getRoot()).getManagerContainer() instanceof ShieldContainerInterface) {
			return ((ShieldContainerInterface) ((ManagedSegmentController<?>) railController.getRoot()).getManagerContainer()).getShieldAddOn().isUsingLocalShields();
		} else {
			return false;
		}
	}

	public boolean isUsingOldPower() {
		return false;
	}

	public abstract boolean isStatic();

	public BlockTypeSearchRunnableManager getBlockTypeSearchManager() {
		return blockTypeSearchManager;
	}

	public float getAmmoCapacity(WeaponType type) {
		return 0;
	}

	public float getAmmoCapacityMax(WeaponType type) {
		return 1;
	}

	public float getRootAmmoCapacity(WeaponType type) {
		return railController.getRoot().getAmmoCapacity(type);
	}

	public float getRootAmmoCapacityMax(WeaponType type) {
		return railController.getRoot().getAmmoCapacityMax(type);
	}

	public void hitWithPhysicalRecoil(Vector3f worldPos, Vector3f dir, float strength, boolean negateTorque) {
		assert (strength > 0);
		SegmentController root = railController.getRoot();
		CollisionObject object = root.getPhysicsDataContainer().getObject();
		// if(isOnServer() || isClientOwnObject() || root.isClientOwnObject()) {
		if(object instanceof RigidBodySegmentController) {
			Vector3f rPos = new Vector3f(worldPos);
			Vector3f force = new Vector3f(dir);
			RigidBodySegmentController r = (RigidBodySegmentController) object;
			if(force.lengthSquared() > 0) {
				if(isOnServer()) {
					root.getWorldTransformInverse().transform(rPos);
				} else {
					root.clientTransformInverse.transform(rPos);
				}
				float forceStrength = strength;
				force.normalize();
				force.scale(forceStrength);
				assert (!Vector3fTools.isNan(force)) : force;
				r.applyCentralForce(force);
				if(negateTorque) {
					// negate again to get the rigth rotation (not sure why this works for cannon own recoil)
					force.negate();
				}
				Vector3f tmp = new Vector3f();
				tmp.cross(rPos, force);
				if(isClientOwnObject() && (Controller.getCamera() instanceof InShipCamera) && !((InShipCamera) Controller.getCamera()).isInAdjustMode() && !KeyboardMappings.FREE_CAM.isDown()) {
					// don't apply torque for played controlled ships
				} else {
					if(isOnServer() && getRemoteTransformable().isSendFromClient()) {
						// don't apply torque for played controlled ships
					} else {
						r.applyTorque(tmp);
					}
				}
				r.hadRecoil = true;
				// System.err.println("[CANNON] Applying Recoil on "+root+": "+forceStrength);
			}
			object.activate(true);
		}
		// }
	}

	public CollisionType getCollisionType() {
		return CollisionType.CUBE_STRUCTURE;
	}

	public void onDockingChanged(boolean docked) {
		if(isOnServer()) {
			ruleEntityManager.triggerOnDockingChange();
		}
	}

	public float getMaxServerSpeed() {
		if(isStatic()) {
			return 0;
		} else {
			return ((GameStateInterface) getState()).getGameState().getMaxGalaxySpeed();
		}
	}

	public boolean canBeDamagedBy(Damager from, DamageDealerType beam) {
		return true;
	}

	public void onShieldDamageServer(ShieldHitCallback hit) {
		onAnyDamageTakenServer(hit.getDamage(), hit.damager, hit.damageType);
	}

	/**
	 * includes shield damage
	 *
	 * @param damage
	 * @param damager
	 * @param damageType
	 */
	public void onAnyDamageTakenServer(double damage, Damager damager, DamageDealerType damageType) {
		lastAnyDamageTakenServer = getState().getUpdateTime();
		flagAnyDamageTakenServer = true;
	}

	public QuarterManager getQuarterManager() {
		return quarterManager;
	}
}
