package org.schema.game.common.data.mines;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import javax.vecmath.Vector3f;

import org.schema.common.util.CompareTools;
import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.common.util.linAlg.SphereTools;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.MineInterface;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.effects.InterEffectHandler.InterEffectType;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.elements.stealth.StealthAddOn.StealthLvl;
import org.schema.game.common.controller.elements.mines.MineLayerElementManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SimpleGameObject;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.mines.MineActivityLevelContainer.ActiveLevel;
import org.schema.game.common.data.mines.handler.CannonMineHandler;
import org.schema.game.common.data.mines.handler.HeatSeekerMineHandler;
import org.schema.game.common.data.mines.handler.MineHandler;
import org.schema.game.common.data.mines.updates.MineUpdateSectorData.MineData;
import org.schema.game.common.data.missile.Missile;
import org.schema.game.common.data.missile.MissileControllerInterface;
import org.schema.game.common.data.missile.MissileManagerInterface;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.faction.FactionRelation.AttackType;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.StealthReconEntity;
import org.schema.game.common.data.world.TransformaleObjectTmpVars;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.Identifiable;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.objects.container.TransformTimed;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class Mine implements Damager, SimpleGameObject, Identifiable, StealthReconEntity {
	private final TransformaleObjectTmpVars v = new TransformaleObjectTmpVars();
	private final TransformTimed clientTransform = new TransformTimed();
	public static final int COMPOSITION_COUNT = 6;
	private static final Vector3f staticVelo = new Vector3f();
	private static final float HIT_RADIUS = 1.2f;
	private StateInterface state;
	private int id = Integer.MIN_VALUE;
	private short hp;
	private Vector3i sectorPos;
	private int sectorId;
	private Transform trans = new Transform();
	private Transform transTmp = new Transform();
	private Vector3f absSectorPos = new Vector3f();
	public ServerMineInfo serverInfo;
	private long ownerId;
	private int factionId;
	private short[] composition = new short[COMPOSITION_COUNT];
	private ActiveLevel activeLevel = ActiveLevel.INACTIVE; //proximity based
	private final TransformTimed worldTransform = new TransformTimed();
	private boolean inDatabase;
	private boolean changed;
	private int lastClientTransCalcSectorId;
	private final boolean onServer;
	
	
	public boolean hit;
	
	private boolean active;
	private MineHandler mineHandler;
	
	private boolean armed;
	private List<SimpleGameObject> detectedEntities = new ObjectArrayList<SimpleGameObject>();
	private Object2FloatOpenHashMap<SimpleGameObject> detectedEntitiesWithDist = new Object2FloatOpenHashMap<SimpleGameObject>();
	private List<SimpleTransformableSendableObject<?>> colEntities = new ObjectArrayList<SimpleTransformableSendableObject<?>>();
	
	private final DistOrder distOrder = new DistOrder();
	
	private final MineSettings settings = new MineSettings();
	private Vector3f centerOfMass = new Vector3f();
	
	public static class MineSettings{
		public MineType mineType;
		public short maxHp = -1;
		public float detectionRadius;
		public int stealthStrength;
		public int firingMode;
		private int strength;
		private short maxAmmo = -1;
		
		
		
		public void calculate(short[] composition) {
			
			maxHp = calculateMaxHp(composition);
			mineType = calculateMineType(composition);
			detectionRadius = calculateDetectionRadius(composition);
			stealthStrength = calculateStealthStrength(composition);
			strength = calculateStrength(composition);
			firingMode = calculateFiringMode(composition);
			maxAmmo = calculateMaxAmmo(composition);
		}
		
		private short calculateMaxAmmo(short[] composition) {
			return switch(mineType) {
				case MINE_TYPE_CANNON -> (short) MineLayerElementManager.CANNON_AMMO;
				case MINE_TYPE_MISSILE -> (short) MineLayerElementManager.MISSILE_AMMO;
				case MINE_TYPE_PROXIMITY -> 1;
				default -> 10;
			};
			
		}

		private MineType calculateMineType(short[] composition) {
		
				MineType[] values = MineType.values();
				for(int i = 0; i < COMPOSITION_COUNT; i++) {
					if(ElementKeyMap.isValidType(composition[i])) {
						for(MineType t : values) {
							if(t.type == composition[i]) {
								return t;
							}
						}
					}
				}
				
				return MineType.MINE_TYPE_CANNON;
			}
		public static short calculateMaxHp(short[] composition) {
			return 100;
		}
		public static float calculateDetectionRadius(short[] composition) {
			return 100;
		}
		public static int calculateFiringMode(short[] composition) {
			for(short type : composition) {
				if(type == ElementKeyMap.MINE_MOD_FRIENDS) {
					return AttackType.ENEMY.code;
				}else if(type == ElementKeyMap.MINE_MOD_PERSONAL) {
					return AttackType.ENEMY.code | AttackType.FACTION.code | AttackType.ALLY.code | AttackType.NEUTRAL.code;
				}
			}
			return AttackType.getAll();
		}
		public static int calculateStealthStrength(short[] composition) {
			int s = 1;
			for(short type : composition) {
				if(type == ElementKeyMap.MINE_MOD_STEALTH) {
					s++;
				}
			}
			return s;
		}
		public static int calculateStrength(short[] composition) {
			int s = 1;
			for(short type : composition) {
				if(type == ElementKeyMap.MINE_MOD_STRENGTH) {
					s++;
				}
			}
			return s;
		}
	}
	
	private class DistOrder implements Comparator<SimpleGameObject>{

		@Override
		public int compare(SimpleGameObject o1, SimpleGameObject o2) {
			return CompareTools.compare(detectedEntitiesWithDist.getFloat(o1), detectedEntitiesWithDist.getFloat(o2));
		}
		
	}
	public enum MineType{
		MINE_TYPE_CANNON(
					"MineTypeA", "MineTypeA-Active",
					"MineTypeA-LOD-Medium", "MineTypeA-LOD-Medium-Active",
					"MineTypeA-LOD-Far", "MineTypeA-LOD-Far-Active",
					"MineSprite-4x1-c-", 
					0, 3, 
					ElementKeyMap.MINE_TYPE_CANNON, true, false),
		MINE_TYPE_MISSILE(
					"MineTypeB",
					"MineTypeB-Active",
					"MineTypeB-LOD-Medium",
					"MineTypeB-LOD-Medium-Active",
					"MineTypeB-LOD-Far",
					"MineTypeB-LOD-Far-Active",
					"MineSprite-4x1-c-", 1, 3, ElementKeyMap.MINE_TYPE_MISSILE, false, false),
		MINE_TYPE_PROXIMITY(
					"MineTypeC",
					"MineTypeC-Active",
					"MineTypeC-LOD-Medium",
					"MineTypeC-LOD-Medium-Active",
					"MineTypeC-LOD-Far",
					"MineTypeC-LOD-Far-Active",
					"MineSprite-4x1-c-", 2, 3, ElementKeyMap.MINE_TYPE_PROXIMITY, false, true), 
//		MINE_TYPE_D("MineTypeD",
//					"MineTypeD-LOD-Medium",
//					"MineTypeD-LOD-Far",
//					"MineSprite-4x1-c-", 3, ElementKeyMap.MINE_TYPE_D),
		;
		public final String name0;
		public final String name1;
		public final String name2;
		public final String sprite;
		public final int subSpriteIndex;
		public final short type;
		public final int subSpriteIndexActive;
		public final String name2_active;
		public final String name1_active;
		public final String name0_active;

		public final boolean orientatedTowardsTriggerEntity;
		public final boolean projectileIsMine;
		
		private MineType(
				String name0, String name0_active,
				String name1, String name1_active,
				String name2, String name2_active,
				String sprite,
				int subSpriteIndex, int subSpriteIndexActive, 
				short type, boolean orientatedTowardsTriggerEntity, boolean projectileIsMine) {
			this.name0 = name0;
			this.name0_active = name0_active;
			this.name1 = name1;
			this.name1_active = name1_active;
			this.name2 = name2;
			this.name2_active = name2_active;
			
			
			this.sprite = sprite;
			this.type = type;
			this.subSpriteIndex = subSpriteIndex;
			this.subSpriteIndexActive = subSpriteIndexActive;
			this.orientatedTowardsTriggerEntity = orientatedTowardsTriggerEntity;
			this.projectileIsMine = projectileIsMine;
		}
	}
	
	
	public class ServerMineInfo{
		public long timeSpawned;
		public int armInSecs = -1;
		public Sector sector;
	}

	
	
	
	
	public void initialize(GameServerState state, int sectorId, Vector3f localPos, AbstractOwnerState ownerState,
			int factionId, int mineArmedInSecs, short[] composition) throws MineDataException {
		serverInfo = new ServerMineInfo();
		serverInfo.sector = state.getUniverse().getSector(sectorId);
		serverInfo.armInSecs = mineArmedInSecs;
		
		if(serverInfo.sector == null) {
			throw new MineDataException("Sector null "+sectorId);
		}
		
		if(ownerState != null) {
			this.ownerId = ownerState.getDbId();
		}else {
			this.ownerId = Long.MIN_VALUE;
		}
		this.factionId = factionId;
		this.sectorId = sectorId;
		this.serverInfo.timeSpawned = System.currentTimeMillis();
		this.sectorPos = new Vector3i(serverInfo.sector.pos);
		this.worldTransform.origin.set(localPos);
		setCompositionByVal(composition);
		this.ammo = getMaxAmmo();
		this.hp = getMaxHp();
		
		this.inDatabase = false;
		this.changed = true;
	}
	public short getMaxHp() {
		assert(settings.maxHp > 0);
		return settings.maxHp ;
	}
	public short getMaxAmmo() {
		return settings.maxAmmo;
	}
	
	private void calculateCompositionData() {
			settings.calculate(composition);
			mineHandler = createMineHandler();
			
	//		System.err.println(getState()+" NEW MINE: "+this+" ");
			assert(getType() != null);
		}
	private MineHandler createMineHandler() {
		return switch(getType()) {
			case MINE_TYPE_CANNON -> createCannonMineHandler();
			case MINE_TYPE_MISSILE -> createMissileMineHandler();
			case MINE_TYPE_PROXIMITY -> createProximityMineHandler();
			default -> throw new RuntimeException("Unknown Mine Type");
		};
	}
	private MineHandler createMissileMineHandler() {
		HeatSeekerMineHandler h = new HeatSeekerMineHandler(this);
		h.damage = (int) (MineLayerElementManager.MISSILE_DAMAGE * MineLayerElementManager.getMissileDamageMult((int) getStrength()));
		h.attackEffect = new InterEffectSet();
		h.attackEffect.setStrength(InterEffectType.EM, 1);
		h.attackEffect.setStrength(InterEffectType.HEAT, 1);
		h.attackEffect.setStrength(InterEffectType.KIN, 1);
		h.color.set(1,0,0,1);
		h.missileSpeed = MineLayerElementManager.MISSILE_SPEED * ((GameStateInterface) state).getGameState().getMaxGalaxySpeed();
		h.shotFreqMilli = (long) (MineLayerElementManager.MISSILE_RELOAD_SEC * 1000f);
		h.missileDistance = 3000;
		h.dieOnShot = false;
		
		return h; 
	}
	private MineHandler createProximityMineHandler() {
		HeatSeekerMineHandler h = new HeatSeekerMineHandler(this);
		h.damage = (int) (MineLayerElementManager.PROXMITY_DAMAGE * MineLayerElementManager.getProximityDamageMult((int) getStrength()));
		h.attackEffect = new InterEffectSet();
		h.attackEffect.setStrength(InterEffectType.EM, 1);
		h.attackEffect.setStrength(InterEffectType.HEAT,1);
		h.attackEffect.setStrength(InterEffectType.KIN, 1);
		h.color.set(1,0,1,1);
		h.missileSpeed = MineLayerElementManager.PROXIMITY_SPEED * ((GameStateInterface) state).getGameState().getMaxGalaxySpeed();
		h.missileDistance = 3000;
		h.dieOnShot = true;
		
		return h; 
	}
	private MineHandler createCannonMineHandler() {
		CannonMineHandler h = new CannonMineHandler(this);
		h.damage = (int) (MineLayerElementManager.CANNON_DAMAGE * MineLayerElementManager.getCannonDamageMult((int) getStrength()));
		h.attackEffect = new InterEffectSet();
		h.attackEffect.setStrength(InterEffectType.EM, 1);
		h.attackEffect.setStrength(InterEffectType.HEAT, 1);
		h.attackEffect.setStrength(InterEffectType.KIN, 1);
		h.shootAtTargetCount = MineLayerElementManager.CANNON_SHOOT_AT_TARGET_COUNT;
		h.color.set(1,0,0,1);
		h.projectileSpeed = MineLayerElementManager.CANNON_SPEED * ((GameStateInterface) state).getGameState().getMaxGalaxySpeed();
		h.shotFreqMilli = (long) (MineLayerElementManager.CANNON_RELOAD_SEC * 1000f);
		
		return h; 
	}
	
	public void updateActivityLevelOnly(Collection<SimpleTransformableSendableObject<?>> entities) {
		for(SimpleTransformableSendableObject<?> s : entities) {
			//distance to bounding sphere. negative if inside
			float d = Vector3fTools.diffLengthSquared(worldTransform.origin, s.getWorldTransform().origin);
			float distanceDetection = d - s.getBoundingSphereTotal().radius - getDetectionRadius();
			float distance = d - s.getBoundingSphereTotal().radius - HIT_RADIUS;
			
			ActiveLevel[] values = ActiveLevel.values();
			for(ActiveLevel a : values) {
				if(distance > a.distance) {
					this.setActiveLevel(a);
				}
			}
		}
	}
	public void updateActivityLevelAndDetect() {
		float minDistanceDetection = Float.POSITIVE_INFINITY; 
		float minDistance = Float.POSITIVE_INFINITY; 
		SimpleTransformableSendableObject<?> closest = null;
		Collection<SimpleTransformableSendableObject<?>> entities;
		Vector3f minePos;
		if(onServer) {
			entities = serverInfo.sector.getEntities();
			minePos = worldTransform.origin;
		}else {
			entities = ((GameClientState)state).getCurrentSectorEntities().values();
			minePos = clientTransform.origin;
		}
		
		
		
		colEntities.clear();
		detectedEntities.clear();
		for(SimpleTransformableSendableObject<?> s : entities) {
			if(!isMineDetectable(s)) {
				continue;
			}
			Vector3f entityPos = onServer ? s.getWorldTransform().origin : s.getWorldTransformOnClient().origin;
			//distance to bounding sphere. negative if inside
			
			
			
			float d = Vector3fTools.diffLength(minePos, entityPos);
			float distanceDetection = d - s.getBoundingSphereTotal().radius - getDetectionRadius();
			float distance = d - s.getBoundingSphereTotal().radius - HIT_RADIUS;
			
			if(distance < minDistance) {
				minDistance = d;
			}
			
			if(distance < 0) {
				colEntities.add(s);
			}
			if(distanceDetection < minDistanceDetection) {
				minDistanceDetection = distanceDetection;
				closest = s;
			}
			if(distanceDetection < 0) {
				detectedEntitiesWithDist.put(s, d);
				
				detectedEntities.add(s);
			}
		}
		if(mineHandler.isPointDefense()) {
			MissileManagerInterface missileManager = ((MissileControllerInterface) state).getMissileManager();
			for(Missile s : missileManager.getMissiles().values()) {
				if(s.getOwner() != this) {
					Vector3f entityPos = onServer ? s.getWorldTransform().origin : s.getWorldTransformOnClient().origin;
					float d = Vector3fTools.diffLength(minePos, entityPos);
					float missileRadiusSize = 1;
					float distanceDetection = d - missileRadiusSize - getDetectionRadius();
					if(distanceDetection < 0) {
						detectedEntitiesWithDist.put(s, d);
						detectedEntities.add(s);
					}
				}
			}
		}
		
		Collections.sort(detectedEntities, distOrder);
		
		
		ActiveLevel[] values = ActiveLevel.values();
		for(ActiveLevel a : values) {
			if(minDistanceDetection > a.distance) {
				this.setActiveLevel(a);
				break;
			}
		}
		if(minDistanceDetection < 0) {
			//closest entity has activated mine
			onActivate(closest, minDistanceDetection);
		}else {
			onDeactivate();
		}
	}
	
	public void handleCollision(Timer timer) {
		if(armed) {
			for(SimpleTransformableSendableObject<?> f : colEntities) {
				if(f instanceof SegmentController && isColliding((SegmentController)f, timer)) {
					destroyOnServer();
					break;
				}
			}
		}
	}
	private boolean isColliding(SegmentController c, Timer timer) {
		Transform t = this.worldTransform;
		return c.getCollisionChecker().checkSegmentControllerWithRails(c, t, 0.01f, false);
	}
	public void handleInactive(Timer timer) {
		mineHandler.handleInactive(timer);
	}
	public void handleActive(Timer timer) {
		mineHandler.handleActive(timer, detectedEntities);
		if(!onServer && isOrientatedTowardsTriggerEntity() && detectedEntities.size() > 0) {
			
			
			
			SimpleGameObject simpleGameObject = detectedEntities.get(0);
			
			
			
			
			Vector3f forward = new Vector3f();
			forward.sub(simpleGameObject.getWorldTransformOnClient().origin, clientTransform.origin);
			if(forward.lengthSquared() == 0) {
				forward.set(0,0,1);
			}
			forward.normalize();
			Vector3f up = new Vector3f(0, 1, 0);
			Vector3f right = new Vector3f();

			right.cross(up, forward);
			right.normalize();
			up.cross(forward, right);
			up.normalize();

			forward.normalize();
			
			forward.negate();
			GlUtil.setForwardVector(up, worldTransform);
			GlUtil.setUpVector(forward, worldTransform);
			GlUtil.setRightVector(right, worldTransform);
			
			clientTransform.basis.set(worldTransform.basis);
//			System.err.println("ORIENTATE : "+simpleGameObject+" "+detectedEntitiesWithDist.getFloat(simpleGameObject)+"\n"+worldTransform.basis+"; \n\n "+clientTransform.basis);
			
		}
	}
	private boolean isProjectileIsMine() {
		return getType().projectileIsMine;
	}
	private boolean isOrientatedTowardsTriggerEntity() {
		return getType().orientatedTowardsTriggerEntity;
	}
	private boolean isMineDetectable(SimpleTransformableSendableObject<?> s) {
		return (!(s instanceof SegmentController) || ((SegmentController)s).railController.isRoot()) && MineHandler.isAttacking(this, s) ;
	}
	private void onDeactivate() {
		if(active) {
			mineHandler.onBecomingInactive();
			((MineInterface) state.getController()).getMineController().onBecomingInactive(this);
		}
		active = false;
	}
	private void onActivate(SimpleTransformableSendableObject<?> closest, float closestDist) {
		if(!active) {
			mineHandler.onBecomingActive(detectedEntities);
			
			((MineInterface) state.getController()).getMineController().onBecomingActive(this);
		}
		active = true;
	}
	public float getDetectionRadius() {
		return settings.detectionRadius;
	}
	public void setActiveLevel(ActiveLevel a) {
		ActiveLevel old = this.activeLevel;
		this.activeLevel = a;
		((MineInterface) state.getController()).getMineController().updateActiveLevel(this, old, a);
	}

	public Mine(StateInterface state) {
		this.state = state;
		this.onServer = state instanceof GameServerState;
		this.worldTransform.setIdentity();
	}

	@Override
	public TransformTimed getWorldTransform() {
		return worldTransform;
	}

	@Override
	public boolean existsInState() {
		return ((MineInterface) state.getController()).getMineController().exists(this);
	}

	@Override
	public int getSectorId() {
		return sectorId;
	}

	@Override
	public void calcWorldTransformRelative(int fromSectorId, Vector3i targetSector) {
		lastClientTransCalcSectorId = -1;

		if (this.sectorId == fromSectorId) {
			clientTransform.set(worldTransform);
		} else {
			boolean changed = false;
			if (lastClientTransCalcSectorId != fromSectorId) {
				changed = true;
				Vector3i relSystemPos = new Vector3i();
				Vector3i sectorThisObject = this.sectorPos;


				Vector3i relSectorPos = new Vector3i();
				relSectorPos.sub(sectorThisObject, targetSector);

				absSectorPos.set(
						relSectorPos.x * ((GameStateInterface) state).getSectorSize(),
						relSectorPos.y * ((GameStateInterface) state).getSectorSize(),
						relSectorPos.z * ((GameStateInterface) state).getSectorSize());


				trans.setIdentity();

				//void
				trans.basis.setIdentity();
				trans.origin.set(absSectorPos);
			}
			if (lastClientTransCalcSectorId != fromSectorId ) {
				transTmp.set(trans);

				Matrix4fTools.transformMul(transTmp, worldTransform);
				
				clientTransform.set(worldTransform);
				clientTransform.origin.set(transTmp.origin);

			}

			
			if (changed) {
				lastClientTransCalcSectorId = fromSectorId;
			}
		}		
	}
	public void calcWorldTransformRelativeForced(int fromSectorId, Vector3i targetSector, Transform out) {
		
		if (this.sectorId == fromSectorId) {
			out.set(worldTransform);
		} else {
			boolean changed = false;
			if (lastClientTransCalcSectorId != fromSectorId) {
				changed = true;
				Vector3i relSystemPos = new Vector3i();
				Vector3i sectorThisObject = this.sectorPos;
				
				
				Vector3i relSectorPos = new Vector3i();
				relSectorPos.sub(sectorThisObject, targetSector);
				
				absSectorPos.set(
						relSectorPos.x * ((GameStateInterface) state).getSectorSize(),
						relSectorPos.y * ((GameStateInterface) state).getSectorSize(),
						relSectorPos.z * ((GameStateInterface) state).getSectorSize());
				
				
				trans.setIdentity();
				
				//void
				trans.basis.setIdentity();
				trans.origin.set(absSectorPos);
			}
			transTmp.set(trans);
			
			Matrix4fTools.transformMul(transTmp, worldTransform);
			
			out.set(worldTransform);
			out.origin.set(transTmp.origin);
		}		
	}

	public boolean isOnServer() {
		return onServer;
	}

	@Override
	public Transform getClientTransform() {
		return clientTransform;
	}

	@Override
	public Transform getClientTransformCenterOfMass(Transform out) {
		return clientTransform;
	}
	@Override
	public Vector3f getCenterOfMass(Vector3f out) {
		out.set(centerOfMass);
		return out;
	}
	@Override
	public Vector3f getLinearVelocity(Vector3f out) {
		return staticVelo;
	}
	public int getStrength() {
		return settings.strength;
	}
	@Override
	public boolean isInPhysics() {
		return false;
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public int getAsTargetId() {
		return id;
	}

	@Override
	public byte getTargetType() {
		return SimpleGameObject.MINE;
	}

	@Override
	public TransformTimed getWorldTransformOnClient() {
		return clientTransform;
	}

	public Transform getWorldTransformRelativeToSector(int fromSector, Transform out) {

		Sector sector = ((GameServerState) state).getUniverse().getSector(fromSector);

		if (sector != null) {
			SimpleTransformableSendableObject.calcWorldTransformRelative(fromSector, sector.pos, sectorId, worldTransform, state, true, out, v);

			return out;
		}
		return null;
	}
	@Override
	public void transformAimingAt(Vector3f to, Damager from, SimpleGameObject target, Random random, float deviation) {
		
	}

	@Override
	public void sendHitConfirm(byte damageType) {
		
	}

	@Override
	public boolean isSegmentController() {
		return false;
	}

	@Override
	public SimpleTransformableSendableObject<?> getShootingEntity() {
		return null;
	}

	@Override
	public int getFactionId() {
		return factionId;
	}

	@Override
	public String getName() {
		return "Mine#"+id;
	}

	@Override
	public AbstractOwnerState getOwnerState() {
		return null;
	}

	@Override
	public void sendClientMessage(String str, byte type) {
	}
	@Override
	public void sendServerMessage(Object[] astr, byte msgType) {
	}
	@Override
	public float getDamageGivenMultiplier() {
		return 1;
	}

	@Override
	public InterEffectSet getAttackEffectSet(long weaponId, DamageDealerType damageDealerType) {
		assert(mineHandler != null);
		InterEffectSet attackEffectSet = mineHandler.getAttackEffectSet(weaponId, damageDealerType);
		assert(attackEffectSet != null);
		return attackEffectSet;
	}

	@Override
	public MetaWeaponEffectInterface getMetaWeaponEffect(long weaponId, DamageDealerType damageDealerType) {
		return null;
	}


	@Override
	public StateInterface getState() {
		return state;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public short getHp() {
		return hp;
	}

	public void setHp(short hp) {
		this.hp = hp;
	}

	public long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(long ownerId) {
		this.ownerId = ownerId;
	}

	public void getComposition(short[] setTo) {
		for(int i = 0; i < COMPOSITION_COUNT; i++) {
			setTo[i] = composition[i];
		}
	}

	public void setCompositionByVal(short[] from) {
		for(int i = 0; i < COMPOSITION_COUNT; i++) {
			composition[i] = from[i];
		}
		calculateCompositionData();
	}

	public void setFactionId(int factionId) {
		this.factionId = factionId;
	}

	public Vector3i getSectorPos() {
		return sectorPos;
	}

	public void setSectorPos(Vector3i sectorPos) {
		this.sectorPos = sectorPos;
	}

	public void setSectorId(int sectorId) {
		this.sectorId = sectorId;
	}

	public ActiveLevel getActiveLevel() {
		return this.activeLevel;
	}

	public void setInDatabase(boolean b) {
		this.inDatabase = b;
	}

	public boolean isInDatabase() {
		return this.inDatabase;
	}

	public short[] getComposition() {
		return composition;
	}

	public long getTimeCreated() {
		return serverInfo.timeSpawned;
	}

	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public void setFromDatabase(Sector sector, ResultSet r) throws SQLException {
		//SELECT ID, OWNER, FACTION, HP, COMPOSITION, SECTOR_X, SECTOR_Y, SECTOR_Z, LOCAL_X, LOCAL_Y, LOCAL_Z, CREATION_DATE, ARMED, ARMED_IN_SECS
		serverInfo = new ServerMineInfo();
		id = r.getInt(1);
		ownerId = r.getLong(2);
		factionId = r.getInt(3);
		hp = r.getShort(4);
		Array compoArray = r.getArray(5);
		Object[] cArray = (Object[]) compoArray.getArray();
		for(int i = 0; i < cArray.length; i++) {
			composition[i] = ((Integer) cArray[i]).shortValue();
		}
		int secX = r.getInt(6);
		int secY = r.getInt(7);
		int secZ = r.getInt(8);
		assert(sector.pos.equals(secX, secY, secZ)):sector+"; "+secX+", "+secY+", "+secZ;
		
		float x = r.getFloat(9);
		float y = r.getFloat(10);
		float z = r.getFloat(11);
		
		worldTransform.origin.set(x, y, z);
		
		serverInfo.timeSpawned = r.getLong(12);
		armed = r.getBoolean(13);
		serverInfo.armInSecs = r.getInt(14);
		serverInfo.sector = sector;

		this.ammo = r.getShort(15);
		
		this.sectorId = sector.getId();
		this.sectorPos = new Vector3i(sector.pos);
		inDatabase = true;
		
		changed = false;
		
		calculateCompositionData();
		
		
		if(ammo <= -2) {
			//-1 means that ammo was unset previously
			changed = true;
			this.ammo = getMaxAmmo();
		}
	}
	
	public static class MineDataException extends Exception{

		/**
		 * 
		 */
		private static final long serialVersionUID = 6498858465343001644L;

		public MineDataException() {
			super();
		}

		public MineDataException(String message, Throwable cause, boolean enableSuppression,
				boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}

		public MineDataException(String message, Throwable cause) {
			super(message, cause);
		}

		public MineDataException(String message) {
			super(message);
		}

		public MineDataException(Throwable cause) {
			super(cause);
		}
		
	}
	public void setFrom(int sectorId, MineData data) throws MineDataException{
		this.sectorId = sectorId;
		this.id = data.id;
		this.factionId = data.factionId;
		this.ownerId = data.ownerId;
		this.hp = data.hp;
		this.ammo = data.ammo;
		this.armed = data.armed;
		this.worldTransform.origin.set(data.localPos);
		this.setCompositionByVal(data.composition);
		Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(sectorId);
		if(sendable instanceof RemoteSector) {
			RemoteSector r = (RemoteSector)sendable;
			this.sectorPos = new Vector3i(r.clientPos());
		}else {
			throw new MineDataException("SectorId not loaded: "+sectorId);
		}
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		return id == ((Mine)obj).id;
	}
	public MineType getType() {
		return settings.mineType;
	}
	public void updateClientTransform() {
//		calcWorldTransformRelative(getSectorId(), ((GameClientState)getState()).getPlayer().getCurrentSector());
		calcWorldTransformRelative(((GameClientState) state).getPlayer().getSectorId(), ((GameClientState) state).getPlayer().getCurrentSector());
	}
	public boolean isHitClient(Vector3f from, Vector3f to) {
		return SphereTools.lineSphereIntersect(from, to, clientTransform.origin, HIT_RADIUS);
	}
	public boolean isHitServer(Vector3f from, Vector3f to) {
		return SphereTools.lineSphereIntersect(from, to, worldTransform.origin, HIT_RADIUS);
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	@Override
	public String toString() {
		return "Mine [id=" + id + ", sectorPos=" + sectorPos + ", mineType=" + getType().name() + ", active=" + active + "("+detectedEntities.size()+")]";
	}
	public void kill() {
		destroyOnServer();
	}
	public void destroyOnServer() {
		((GameServerState) state).getController().getMineController().deleteMineServer(this);
	}
	public boolean isArmed() {
		return armed;
	}
	public void setArmed(boolean armed) {
		this.armed = armed;
	}
	public void checkArming(Timer timer) {
		if(!armed && onServer) {
			if(serverInfo.armInSecs >= 0 && (long)(timer.currentTime - serverInfo.timeSpawned)/1000L >= serverInfo.armInSecs) {
				
				try {
					armed = true;
					((GameServerState) state).getDatabaseIndex().getTableManager().getMinesTable().updateOrInsertMine(this);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				((GameServerState) state).getController().getMineController().sendArmed(this);
				
			}
		}
	}
	
	public float getStealthStrength() {
		return settings.stealthStrength;
	}

	@Override
	public Vector3f getPosition() {
		return getWorldTransform().origin;
	}

	public boolean canSeeStructure(StealthReconEntity target, boolean inclSharing){
			return true;
	}
	public boolean canSeeIndicator(StealthReconEntity target, boolean inclSharing){
		float val = getReconStrengthRaw() - target.getStealthStrength();
		if(val >= VoidElementManager.RECON_DIFFERENCE_MIN_JAMMING){
			return true;
		}else{
			if(target instanceof ManagedSegmentController<?>) {
				return !((SegmentController)target).hasStealth(StealthLvl.JAMMING);
			}
			return false;
		}
	}
	@Override
	public float getReconStrengthRaw() {
		return 0;
	}

	@Override
	public float getReconStrength(float distance) {
		return 0;
	}

	public int getFiringMode() {
		return settings.firingMode;
	}
	public boolean isVisibleForClientAt(float distance) {
		if(active) {
			return true;
		}
		if(!armed && ((GameClientState) state).getPlayer() != null && ((GameClientState) state).getPlayer().getDbId() == ownerId) {
			return true;
		}
		
		SimpleTransformableSendableObject currentPlayerObject = ((GameClientState) state).getCurrentPlayerObject();
		
		if(currentPlayerObject == null) {
			return false;
		}
//		return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
		
		if(currentPlayerObject instanceof StealthReconEntity) {
			float str = getStealthStrength() - ((StealthReconEntity)currentPlayerObject).getReconStrengthRaw();
			return distance < str * getStealthRadiusPerStealthLevel();
		}else {
			return getStealthStrength() <= 1f;
		}
	}
	private float getStealthRadiusPerStealthLevel() {
		return 300;
	}
	private final Transform tmpOut = new Transform();
	private short ammo;
	private float destroyTimer;
	private float outOfAmmoTimer;
	public float getDistanceTo(SimpleTransformableSendableObject<?> from) {
		return getDistanceTo(from.getSectorId(), from.getWorldTransform().origin);
	}
	public float getDistanceTo(int sectorFrom, Vector3f from) {
		if(!onServer) {
			
			Sendable s = ((GameClientState) state).getLocalAndRemoteObjectContainer().getLocalObjects().get(sectorFrom);
			if(!(s instanceof RemoteSector)) {
				return Float.POSITIVE_INFINITY;
			}
			calcWorldTransformRelativeForced(sectorFrom, ((RemoteSector)s).clientPos(), tmpOut);
		}else {
			Sector sector = ((GameServerState) state).getUniverse().getSector(sectorFrom);
			if(sector == null) {
				return Float.POSITIVE_INFINITY;
			}
			calcWorldTransformRelativeForced(sectorFrom, sector.pos, tmpOut);
		}
		tmpOut.origin.sub(from);
		return tmpOut.origin.length();
	}
	public short getAmmo() {
		return ammo;
	}
	public void setAmmo(short ammo) {
		
		this.ammo = ammo;
		
		
	}
	public void setAmmoServer(short ammo) {
		assert(onServer);
		final short oldAmmo = this.ammo; 
		this.ammo = ammo;
		if(oldAmmo != ammo) {
			changed = true;
			
			if(oldAmmo > 0 && ammo <= 0) {
				//send depleted ammo
				((GameServerState) state).getController().getMineController().sendAmmo(this);
			}
		}
	}
	public void handleOutOfAmmo(Timer timer) {
		outOfAmmoTimer += timer.getDelta();
		if(outOfAmmoTimer > 20f) {
			if(onServer) {
				destroyOnServer();
			}
		}
	}

}
