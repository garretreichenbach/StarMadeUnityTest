package org.schema.game.common.controller.damage.projectile;

import api.listener.events.weapon.CannonProjectileAddEvent;
import api.mod.StarLoader;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.TransformTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.MineInterface;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ShootingRespose;
import org.schema.game.common.controller.elements.ammo.cannon.CannonCapacityElementManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.mines.Mine;
import org.schema.game.common.data.missile.Missile;
import org.schema.game.common.data.physics.*;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SectorInformation.SectorType;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.particle.ParticleController;
import org.schema.schine.network.Identifiable;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.network.server.ServerStateInterface;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager.WeaponType.CANNON;

public class ProjectileController extends ParticleController<ProjectileParticleContainer> {
	//	private HashMap<GhostObject, HashSet<Projectile>> ghostVolumes;

	public static final int PROJECTILE_CACHE = 256;
	public static final boolean SORT_PARTICLES = false;
	private final ParticleHitCallback particleHitCallback = new ParticleHitCallback();
	Transform oldTrans = new Transform();
	Transform newTrans = new Transform();
	Transform transTmp = new Transform();
	private StateInterface state;
	private int sectorId;
	private Vector3f velocityHelper = new Vector3f();
	private Vector4f colorHelper = new Vector4f();
	private Vector3f posHelper = new Vector3f();
	private Vector3f userDataHelper = new Vector3f();
	private Vector3f startHelper = new Vector3f();
	private Vector3f posBeforeUpdate = new Vector3f();
	private Vector3f posAfterUpdate = new Vector3f();
	private Vector3i syspos = new Vector3i();
	private final ObjectArrayList<ProjectileBlockHit> blockHitPool;
	private final ObjectArrayList<ProjectileBlockHit> blockHitList;
	private final BlockSorter blockSorter = new BlockSorter();
	
	private final Object2ObjectMap<CollisionType, ProjectileHandler> projectileHandlers = threadHandlers.get();
	private static int projectileIdGen;
	
	private static ThreadLocal<Object2ObjectMap<CollisionType, ProjectileHandler>> threadHandlers = new ThreadLocal<Object2ObjectMap<CollisionType, ProjectileHandler>>() {
		@Override
		protected Object2ObjectMap<CollisionType, ProjectileHandler> initialValue() {
			
			Object2ObjectMap<CollisionType, ProjectileHandler> projectileHandlers = new Object2ObjectOpenHashMap<CollisionType, ProjectileHandler>();
			CollisionType[] v = CollisionType.values();
			for(CollisionType t : v){
				projectileHandlers.put(t, t.projectileHandlerFactory.getInst());
			}
			
			return projectileHandlers;
		}
	};
	private static ThreadLocal<ObjectArrayList<ProjectileBlockHit>> threadCache = new ThreadLocal<ObjectArrayList<ProjectileBlockHit>>() {
		@Override
		protected ObjectArrayList<ProjectileBlockHit> initialValue() {
			ObjectArrayList<ProjectileBlockHit> objectArrayList = new ObjectArrayList<ProjectileBlockHit>(512);
			for(int i = 0; i < 128; i++){
				objectArrayList.add(new ProjectileBlockHit());
			}
			return objectArrayList;
		}
	};
	private static ThreadLocal<ObjectArrayList<ProjectileBlockHit>> threadLocalPool = new ThreadLocal<ObjectArrayList<ProjectileBlockHit>>() {
		@Override
		protected ObjectArrayList<ProjectileBlockHit> initialValue() {
			return new ObjectArrayList(512);
		}
	};
	public ProjectileController(StateInterface state, int sectorId) {
		super(state instanceof GameClientState && SORT_PARTICLES);
		this.state = state;
		this.sectorId = sectorId;
		
		blockHitPool = threadLocalPool.get();
		blockHitList = threadCache.get();
	}

	public static void translateSector(StateInterface state, Sector oldSector, Sector newSector, Vector3f inPos, Vector3f outPos) {
		Transform t = new Transform();
		t.setIdentity();
		t.origin.set(inPos);

		//		Universe.calcSecPos(state, oldSector.pos, newSector.pos, 0, 0, t);

		Transform oldTrans = new Transform();
		oldTrans.setIdentity();
		Transform newTrans = new Transform();
		newTrans.setIdentity();

		//calculate pos as if normal
		float pc = 0;

		//use the same system pos, weben if the seconds isnt part of the
		//same system. the pc is 0 anyway
		Vector3i sysPos = StellarSystem.getPosFromSector(oldSector.pos, new Vector3i());
		if (!StellarSystem.isStarSystem(oldSector.pos)) {
			pc = 0;

		}

		{
			Vector3i relSysPos = new Vector3i(sysPos);
			Vector3i fromOldToNew = new Vector3i(oldSector.pos);
			relSysPos.scale(VoidSystem.SYSTEM_SIZE);
			relSysPos.add(VoidSystem.SYSTEM_SIZE / 2, VoidSystem.SYSTEM_SIZE / 2, VoidSystem.SYSTEM_SIZE / 2);
			relSysPos.sub(fromOldToNew);
			Vector3f absCenterPos = new Vector3f();
			absCenterPos.set(
					(relSysPos.x) * ((GameStateInterface) state).getSectorSize(),
					(relSysPos.y) * ((GameStateInterface) state).getSectorSize(),
					(relSysPos.z) * ((GameStateInterface) state).getSectorSize());
			oldTrans.setIdentity();
			oldTrans.origin.add(absCenterPos);
			oldTrans.basis.rotX((FastMath.PI * 2) * pc);

			oldTrans.basis.transform(oldTrans.origin);

			//			Transform wo = new Transform(o.getWorldTransform());
			Transform wo = new Transform();
			wo.setIdentity();
			wo.origin.set(inPos);
			//add local object transform
			wo.origin.negate();
			oldTrans.origin.add(wo.origin);
		}
		{
			Vector3i relSysPos = new Vector3i(sysPos);
			Vector3i fromOldToNew = new Vector3i(newSector.pos);
			relSysPos.scale(VoidSystem.SYSTEM_SIZE);
			relSysPos.add(VoidSystem.SYSTEM_SIZE / 2, VoidSystem.SYSTEM_SIZE / 2, VoidSystem.SYSTEM_SIZE / 2);
			relSysPos.sub(fromOldToNew);
			Vector3f absCenterPos = new Vector3f();
			absCenterPos.set(
					(relSysPos.x) * ((GameStateInterface) state).getSectorSize(),
					(relSysPos.y) * ((GameStateInterface) state).getSectorSize(),
					(relSysPos.z) * ((GameStateInterface) state).getSectorSize());
			newTrans.setIdentity();
			newTrans.origin.add(absCenterPos);
			newTrans.basis.rotX((FastMath.PI * 2) * pc);
			newTrans.basis.transform(newTrans.origin);
		}
		Matrix4f a = new Matrix4f();
		Matrix4f b = new Matrix4f();
		oldTrans.getMatrix(a);
		newTrans.getMatrix(b);

		Transform befTrans = new Transform(oldTrans);
		newTrans.inverse();
		oldTrans.mul(newTrans);

		t.origin.set(oldTrans.origin);
		t.origin.negate();
		//		Matrix3f s = new Matrix3f();
		//		s.rotX((FastMath.PI * 2) * pc);

		//		t.basis.set(o.getWorldTransform().basis);
		outPos.set(t.origin);

	}
	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.particle.ParticleController#addProjectile(javax.vecmath.Vector3f, javax.vecmath.Vector3f)
	 */
	public void addProjectile(Identifiable owner, Vector3f from, Vector3f toForce, float damage, float distance, int acidFormula, float projectileWidth, int penetrationDepth, float impactForce, long usableId, Vector4f color, float capacityUsed) {
		if(owner instanceof SegmentController) {
			SegmentController root = ((SegmentController) owner).railController.getRoot();
			if (root instanceof ManagedSegmentController<?>) {
				ManagerContainer<?> mc = ((ManagedSegmentController<?>) root).getManagerContainer();
				CannonCapacityElementManager cannons = (CannonCapacityElementManager) mc.getAmmoSystem(CANNON).getElementManager();
				float currCapacity = mc.getAmmoCapacity(CANNON);
				if (currCapacity < capacityUsed) {
					//this case should always be caught beforehand by the weaponunit.canUse() method, but the comparison is basically free, so... this is here just in case
					root.sendServerMessage(Lng.astr("Can't fire cannon. It takes %s cannon ammo capacity to fire!", capacityUsed), ServerMessage.MESSAGE_TYPE_ERROR);
					return;
				} else {
					float newTimer;
					if (cannons.ammoReloadResetsOnAIFire() && mc.getSegmentController().isAIControlled() ||
							cannons.ammoReloadResetsOnManualFire() && mc.getSegmentController().isConrolledByActivePlayer()) {
						newTimer = cannons.getAmmoCapacityReloadTime();
					} else newTimer = cannons.getAmmoTimer();
					mc.setAmmoCapacity(CANNON, currCapacity - capacityUsed, newTimer, true);
				}
			}
		}

		int pointer = addParticle(from, toForce);

		getParticles().setColor(pointer, color);
		getParticles().setOwnerId(pointer, owner.getId());
		getParticles().setWeaponId(pointer, usableId);
		getParticles().setDamage(pointer, damage);
		getParticles().setDamageInitial(pointer, damage);
		getParticles().setMaxDistance(pointer, distance);
		getParticles().setAcidFormulaIndex(pointer, acidFormula);
		getParticles().setWidth(pointer, projectileWidth);
		getParticles().setPenetrationDepth(pointer, penetrationDepth);
		getParticles().setImpactForce(pointer, impactForce);
		getParticles().setId(pointer, projectileIdGen++);

		//INSERTED CODE @227
		CannonProjectileAddEvent event = new CannonProjectileAddEvent(this, getParticles(), pointer);
		StarLoader.fireEvent(event, isOnServer());
		///

//		System.err.println(getState()+"_ __ "+owner.getId()+"; "+getParticles().getOwnerId(pointer));
		
//		getParticles().setUserdata(pointer, damage, distance, owner.getId(), usableId);
	}
	private CubeRayCastResult rayCallbackInitial = new CubeRayCastResult(new Vector3f(), new Vector3f(), null);
	
	
	
	
	public void freeBlockHit(ProjectileBlockHit h){
		h.objectId = null;
		h.setSegmentData(null);
		blockHitPool.add(h);
	}
	public ProjectileBlockHit getBlockHit(){
		if(blockHitPool.isEmpty()){
			return new ProjectileBlockHit();
		}else{
			return blockHitPool.remove(blockHitPool.size()-1);
		}
	}
	private Vector3i tmpPos = new Vector3i();
	private Int2ObjectOpenHashMap<BlockRecorder> blockHitCache = new Int2ObjectOpenHashMap<BlockRecorder>();
	private int blocksAlreadyHit;
	private int blockDeepnessFull;
	private class BlockSorter implements Comparator<ProjectileBlockHit>{
		@Override
		public int compare(ProjectileBlockHit o1, ProjectileBlockHit o2) {
			return Float.compare(o1.absPosition.lengthSquared(), o2.absPosition.lengthSquared());
		}
		
	}
	public int addParticle(Vector3f from, Vector3f toForce) {
		if (particlePointer >= getParticles().getCapacity() - 1) {
			getParticles().growCapacity();
			//TODO shrink capacity if count remains low
		}
		int id = idGen++;
		if (isOrderedDelete()) {
			int pointer = particlePointer % getParticles().getCapacity();
			getParticles().setPos(pointer, from.x, from.y, from.z);
			getParticles().setStart(pointer, from.x, from.y, from.z);
			getParticles().setVelocity(pointer, toForce.x, toForce.y, toForce.z);
			getParticles().setLifetime(pointer, 0);
			getParticles().setId(pointer, id);
			getParticles().setBlockHitIndex(pointer, 0);
			getParticles().setShotStatus(pointer, 0);
			particlePointer++;
			return pointer;
		} else {
			int pointer = particlePointer % getParticles().getCapacity();
			getParticles().setPos(pointer, from.x, from.y, from.z);
			getParticles().setStart(pointer, from.x, from.y, from.z);
			getParticles().setVelocity(pointer, toForce.x, toForce.y, toForce.z);
			getParticles().setLifetime(pointer, 0);
			getParticles().setId(pointer, id);
			getParticles().setBlockHitIndex(pointer, 0);
			getParticles().setShotStatus(pointer, 0);
			particlePointer++;
			return pointer;
		}
	}
	private boolean canHitMissile(Missile hitMissile, Damager owner) {
		if (ServerConfig.MISSILE_DEFENSE_FRIENDLY_FIRE.isOn()) {
			return true;
		}
		if (owner == null || owner.getFactionId() == 0 || owner.getFactionId() != hitMissile.getFactionId()) {
			return true;
		}
		return false;
	}
	public enum ProjectileHandleState{
		
		PROJECTILE_IGNORE(false), 
		PROJECTILE_NO_HIT(false),
		PROJECTILE_HIT_CONTINUE(false),
		PROJECTILE_NO_HIT_STOP(true), 
		PROJECTILE_HIT_STOP(true), 
		PROJECTILE_HIT_STOP_INVULNERABLE(true), 
		;
		final boolean stopParticle;
		private ProjectileHandleState(boolean stopParticle) {
			this.stopParticle = stopParticle;
		}
	}
	
	private boolean hitMarker = false;
	private List<SegmentController> hitCon = new ObjectArrayList<SegmentController>();
	private Int2ObjectOpenHashMap<SegmentController[]> arrayBuffer = new Int2ObjectOpenHashMap<SegmentController[]>();
	{
		arrayBuffer.put(0, new SegmentController[0] );
		arrayBuffer.put(1, new SegmentController[1] );
		arrayBuffer.put(2, new SegmentController[2] );
		arrayBuffer.put(3, new SegmentController[3] );
	}
	public boolean checkCollision(Damager owner, Vector3f posBeforeUpdate, Vector3f posAfterUpdate, int particleIndex, boolean ignoreDebris) {
		hitMarker = false;
		hitCon.clear();
		do {
			//we need to repeat the raycast in case there are multiple entities within this update
			//the check will add the closest hit to a filter and check again until either
			//the projectile was stopped or we aren't hitting anything
			SegmentController[] filter = arrayBuffer.get(hitCon.size());
			if(filter == null) {
				filter = new SegmentController[hitCon.size()];
				arrayBuffer.put(filter.length, filter);
			}
			for(int i = 0; i < filter.length; i++) {
				filter[i] = hitCon.get(i);
			}
			if(checkCollision(owner, posBeforeUpdate, posAfterUpdate, particleIndex, ignoreDebris, filter)) {
				Arrays.fill(filter, null);
				return true;
			}
			Arrays.fill(filter, null);
			
		}while(hitMarker);
		
		hitCon.clear();
		return false;
	}
	public boolean checkCollision(Damager owner, Vector3f posBeforeUpdate, Vector3f posAfterUpdate, int particleIndex, boolean ignoreDebris, SegmentController[] filter) {
		
		rayCallbackInitial.closestHitFraction = 1f;
		rayCallbackInitial.collisionObject = null;
		rayCallbackInitial.setSegment(null);
		rayCallbackInitial.rayFromWorld.set(posBeforeUpdate);
		rayCallbackInitial.rayToWorld.set(posAfterUpdate);
		
		rayCallbackInitial.filterModeSingleNot = true;
		rayCallbackInitial.setFilter(filter);
		rayCallbackInitial.setOwner(owner);
		rayCallbackInitial.setIgnoereNotPhysical(false);
		rayCallbackInitial.setIgnoreDebris(ignoreDebris);
		rayCallbackInitial.setRecordAllBlocks(false);
		rayCallbackInitial.setZeroHpPhysical(true); //do initial check for all blocks 
		rayCallbackInitial.setDamageTest(true);
		rayCallbackInitial.setCheckStabilizerPaths(true); //hit stablizer paths
		
		//do simple test when shot came from a ship or if projectile has enough damage
		boolean segmentControllerShooter = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(getParticles().getOwnerId(particleIndex)) instanceof SegmentController;
		boolean doSimpleTest = segmentControllerShooter;// && getParticles().getDamage(particleIndex) > 100;
		rayCallbackInitial.setSimpleRayTest(doSimpleTest); //initial check is always complex so that we dont hit full block
		
//		System.err.println("SIMPLE: "+doSimpleTest);
		
//		rayCallbackInitial.setDebug(!isOnServer());
		//check if a block gets hit first
		((ModifiedDynamicsWorld) currentPhysics.getDynamicsWorld()).rayTest(posBeforeUpdate, posAfterUpdate, rayCallbackInitial);
		Vector3f dir = new Vector3f();
		dir.sub(posAfterUpdate, posBeforeUpdate);
		dir.normalize();
//		System.err.println(getParticles().getId(particleIndex)+" WE GOT "+getState()+"; "+posBeforeUpdate+" -> "+posAfterUpdate+"; "+dir+"; "+rayCallbackInitial.hasHit());
		if(rayCallbackInitial.hasHit()){
			
			if(rayCallbackInitial.collisionObject instanceof CollisionObjectInterface){
				final CollisionType type = ((CollisionObjectInterface)rayCallbackInitial.collisionObject).getType();
				hitMarker = type == CollisionType.CUBE_STRUCTURE && rayCallbackInitial.getSegment() != null;
				if(hitMarker) {
					//add segmentcontroller to filter of subsequent checks
					
					hitCon.add(rayCallbackInitial.getSegment().getSegmentController());
				}
				final ProjectileHandler ph = projectileHandlers.get(type);
				assert(!(ph instanceof ProjectileHandlerDefault)):"Default handler on type: "+type.name()+"; "+rayCallbackInitial.collisionObject;
				try {
					final ProjectileHandleState hitBef = ph.handleBefore(owner, this, posBeforeUpdate, posAfterUpdate, getParticles(), particleIndex, rayCallbackInitial);
					if(hitBef.stopParticle){
						return true;
					}
					final ProjectileHandleState hitReg = ph.handle(owner, this, posBeforeUpdate, posAfterUpdate, getParticles(), particleIndex, rayCallbackInitial);
					if(hitReg.stopParticle){
						return true;
					}
					
					final ProjectileHandleState hitAft = ph.handleAfterIfNotStopped(owner, this, posBeforeUpdate, posAfterUpdate, getParticles(), particleIndex, rayCallbackInitial);
					if(hitAft.stopParticle){
						return true;
					}
				}finally {
					ph.afterHandleAlways(owner, this, posBeforeUpdate, posAfterUpdate, getParticles(), particleIndex, rayCallbackInitial);
				}
			}else{
				assert(false):rayCallbackInitial.collisionObject;
			}
			
		}else { 
			hitMarker = false;
			if (isOnServer()) {
				GameServerState state = (GameServerState) this.state;
				Sector s = ((Sector) getPhysics().getState());
				int projectileId = getParticles().getId(particleIndex);
				for (short missileId : s.getMissiles()) {
					Missile hitMissile = state.getController().getMissileController().hasHit(missileId, projectileId, posBeforeUpdate, posAfterUpdate);
					if (hitMissile != null && canHitMissile(hitMissile, owner)) {
						float damage = getParticles().getDamage(particleIndex);
						hitMissile.hitByProjectile(projectileId, damage);
					}
				}
				if(!(owner instanceof Mine)) {
					//don't hit mines with mines
					state.getController().getMineController().handleHit(sectorId, posBeforeUpdate, posAfterUpdate);
				}
			}
		}
		
		return false;
	}

	public boolean isOnServer() {
		return state instanceof ServerStateInterface;
	}

//	private float hitBlock(ProjectileBlockHit h, float damageInShot, int particleIndex,
//			SimpleTransformableSendableObject<?> owner, Vector3f posBeforeUpdate,
//			Vector3f posAfterUpdate, short effectType, float effectRatio, float effectSize, 
//			Vector3f hitpointWorld, Vector3f hitNormalWorld, long beforeBlockIndex, long afterBlockIndex) {
//		
//		SegmentController segmentController = h.objectId;
//		int restDamage = 0;
//		if (segmentController.isOnServer()) {
//			Sector sector = ((GameServerState) getState()).getUniverse().getSector(segmentController.getSectorId());
//			if (sector != null && sector.isProtected()) {
//				if (owner != null && owner instanceof PlayerControllable) {
//					List<PlayerState> attachedPlayers = ((PlayerControllable) owner).getAttachedPlayers();
//					for (int i = 0; i < attachedPlayers.size(); i++) {
//						PlayerState ps = attachedPlayers.get(i);
//						if (System.currentTimeMillis() - ps.lastSectorProtectedMsgSent > 5000) {
//							ps.lastSectorProtectedMsgSent = System.currentTimeMillis();
//							ps.sendServerMessage(new ServerMessage(Lng.astr("This Sector is Protected!"), ServerMessage.MESSAGE_TYPE_WARNING, ps.getId()));
//						}
//					}
//				}
//				return 0;
//			}
//		}
//		if (owner != null && owner instanceof SegmentController && segmentController.getDockingController().getAbsoluteMother() == ((SegmentController) owner).getDockingController().getAbsoluteMother()) {
//			return 0;
//		}
//		
//		boolean targetMotherShip = false;
//		boolean targetOtherTurretShip = false;
//		if (owner instanceof SegmentController && ((SegmentController) owner).getDockingController().isDocked()) {
//			SegmentController motherShip = ((SegmentController) owner).getDockingController().getDockedOn().to.getSegment().getSegmentController();
//			targetMotherShip = motherShip == segmentController;
//
//			for (ElementDocking d : motherShip.getDockingController().getDockedOnThis()) {
//				if (d.from.getSegment().getSegmentController().equals(segmentController)) {
//					targetOtherTurretShip = true;
//					break;
//				}
//			}
//		}
//		if (targetMotherShip || targetOtherTurretShip) {
//			return 0;
//		}
//		if (!owner.equals(segmentController) && !targetMotherShip && !targetOtherTurretShip) {
//
//			float damage = damageInShot;
//			
//			if(owner != null){
//				damage *= owner.getDamageGivenMultiplier();
//			}
//			damage *= segmentController.getDamageTakenMultiplier(DamageDealerType.PROJECTILE);
//			
//
//			if (!((EditableSendableSegmentController) segmentController).canAttack(owner)) {
//				return 0;
//			}
//
//			if (owner != null && owner instanceof SegmentController && segmentController.getDockingController().getAbsoluteMother() == ((SegmentController) owner).getDockingController().getAbsoluteMother()) {
//				return 0;
//			}
//			boolean shieldAbsorbed = false;
//			/* hit shields */
//			float damageBeforeShields = damage;
//			if (segmentController instanceof ManagedSegmentController<?>) {
//				ManagerContainer<?> managerContainer = ((ManagedSegmentController<?>) segmentController).getManagerContainer();
//				if (managerContainer instanceof ShieldContainerInterface) {
//					ShieldContainerInterface sc = (ShieldContainerInterface) managerContainer;
//					if (sc.getShieldAddOn().getShields() > 0 || sc.getShieldAddOn().isUsingLocalShieldsAtLeastOneActive() || segmentController.railController.isDockedAndExecuted()) {
////						damage = (float) sc.handleShieldHit(owner, hitpointWorld, DamageDealerType.PROJECTILE, damage, effectType, effectRatio, effectSize);
//						if (damage <= 0) {
//							//completely consumed by shield
//							shieldAbsorbed = true;
//							((EditableSendableSegmentController) segmentController).sendHitConfirmToDamager(owner, true);
//
//							if (((EditableSendableSegmentController) segmentController).getEffect(owner, effectType) == null || !((EditableSendableSegmentController) segmentController).getEffect(owner, effectType).isEffectIgnoreShields()) {
//								return 0;
//							}
//						}
//						throw new RuntimeException("TODO");
//					}
//				}
//			}
//			//shield absobtion subtracted from particle
//			getParticles().setDamageInital(particleIndex, damage);
//
//			particleHitCallback.reset();
//			particleHitCallback.beforeBlockIndex = beforeBlockIndex;
//			particleHitCallback.afterBlockIndex = afterBlockIndex;
//			particleHitCallback.blockHit = h;
//			particleHitCallback.hitPointWorld = hitpointWorld;
//			particleHitCallback.hitNormalWorld = hitNormalWorld;
//			
//			boolean firstBlock = blocksAlreadyHit == 0;
//			/*
//			 *we hit as often as we must to accumulate default punchthrough
//			 *
//			 *in case the block gets killed, we fly on to the next update
//			 */
//			while (blocksAlreadyHit < blockDeepnessFull && !particleHitCallback.killedBlock) {
//				float blockIndexDamage = (float) WeaponElementManager.calculateDamageForBlock(blocksAlreadyHit, blockDeepnessFull, damage);
//				float blockIndexDamageBefShields = (float) WeaponElementManager.calculateDamageForBlock(blocksAlreadyHit, blockDeepnessFull, damageBeforeShields);
////				System.err.println(getState()+"    "+blockHitIndex+" / "+blockDeepness+"; Damage: "+blockIndexDamage);
////				particleHitCallback.reset();
//				/* hit regular */
//				throw new RuntimeException("TODO");
////				segmentController.handleHit
////				(particleHitCallback, owner, blockIndexDamage, 
////						blockIndexDamageBefShields, posBeforeUpdate, 
////						posAfterUpdate, shieldAbsorbed, effectType, effectRatio, effectSize);
////
////				blocksAlreadyHit++;
////				
////				
////				if(particleHitCallback.abortWhole){
////					return 0;
////				}
////				if(!particleHitCallback.hit){
////					//we hit air (which can happen on explosion effect)
////					break;
////				}
//			}
//			particleHitCallback.blockHit = null;
//			
//			
//			
//			if (!segmentController.isOnServer() && firstBlock) {
//				Transform t = new Transform();
//				t.setIdentity();
//				t.origin.set(hitpointWorld);
//				if(HudIndicatorOverlay.toDrawTexts.size() < 100){
//					HudIndicatorOverlay.toDrawTexts.add(new RaisingIndication(t, StringTools.formatPointZero(particleHitCallback.getDamageDone()), 1, 0, 0, 1));
//				}
//				
//				
//				
//				// prepare to add some explosions
//				GameClientState s = (GameClientState) getState();
//				
//				s.getEventFactory().fireProjectileHitEvent(owner != null ? owner.getId() : -1, segmentController.getId(), damageInShot, hitpointWorld.x, hitpointWorld.y, hitpointWorld.z, DamageDealerType.PROJECTILE);
//				
//				s.getWorldDrawer().getExplosionDrawer().addExplosion(hitpointWorld);
//				if (particleHitCallback.getDamageDone() < 300) {
//					((GameClientController)getState().getController()).queueTransformableAudio("0022_spaceship enemy - hit small explosion small enemy ship blow up", t, 2, 50);
//				} else if (particleHitCallback.getDamageDone() < 600) {
//					((GameClientController)getState().getController()).queueTransformableAudio("0022_spaceship enemy - hit medium explosion medium enemy ship blow up", t, 2, 100);
//				} else {
//					((GameClientController)getState().getController()).queueTransformableAudio("0022_spaceship enemy - hit large explosion big enemy ship blow up", t, 2, 150);
//				}
//			}
//
//			
//			
//
//			restDamage = (int) Math.max(0, damage - particleHitCallback.getDamageDone());
////			if(isOnServer()){
////				System.err.println("REST DAMAGE: "+restDamage);
////			}
//			if (effectType != 0) {
//				EffectElementManager<?, ?, ?> effect = 
//						((EditableSendableSegmentController) segmentController).getEffect(owner, effectType);//((EffectManagerContainer)((ManagedSegmentController<?>)seg.getSegmentController()).getManagerContainer()).getEffect(effectType);
//				if (effect != null) {
//					if (effect.isPiercing() || effect.isPunchThrough()) {
//						getParticles().setDamageInital(particleIndex, damage);
//					}
//				}
//			}
//
//		}
//		
//		return restDamage;
//	}
//	@Deprecated
//	private boolean handleCubeCollisionOld(CubeRayCastResult cubeResult, int particleIndex, SimpleTransformableSendableObject<?> owner, Vector3f posBeforeUpdate, Vector3f posAfterUpdate, int iteration) {
//
//		if(cubeResult == null || cubeResult.getSegment() == null || cubeResult.getSegment().getSegmentData() == null || cubeResult.getSegment().getSegmentData().getSegmentController() == null){
//			return true;
//		}
//		SegmentController segmentController = cubeResult.getSegment().getSegmentController();
//
//		if (owner != null && owner instanceof SegmentController && segmentController.getDockingController().getAbsoluteMother() == ((SegmentController) owner).getDockingController().getAbsoluteMother()) {
//			return true;
//		}
//
//		boolean targetMotherShip = false;
//		boolean targetOtherTurretShip = false;
//		if (owner instanceof SegmentController && ((SegmentController) owner).getDockingController().isDocked()) {
//			SegmentController motherShip = ((SegmentController) owner).getDockingController().getDockedOn().to.getSegment().getSegmentController();
//			targetMotherShip = motherShip == segmentController;
//
//			for (ElementDocking d : motherShip.getDockingController().getDockedOnThis()) {
//				if (d.from.getSegment().getSegmentController().equals(segmentController)) {
//					targetOtherTurretShip = true;
//					break;
//				}
//			}
//		}
//		if (targetMotherShip || targetOtherTurretShip) {
//			return true;
//		}
//		if (segmentController.isOnServer()) {
//			Sector sector = ((GameServerState) getState()).getUniverse().getSector(segmentController.getSectorId());
//			if (sector != null && sector.isProtected()) {
//				if (owner != null && owner instanceof PlayerControllable) {
//					List<PlayerState> attachedPlayers = ((PlayerControllable) owner).getAttachedPlayers();
//					for (int i = 0; i < attachedPlayers.size(); i++) {
//						PlayerState ps = attachedPlayers.get(i);
//						if (System.currentTimeMillis() - ps.lastSectorProtectedMsgSent > 5000) {
//							ps.lastSectorProtectedMsgSent = System.currentTimeMillis();
//							ps.sendServerMessage(new ServerMessage(Lng.astr("This Sector is Protected!"), ServerMessage.MESSAGE_TYPE_WARNING, ps.getId()));
//						}
//					}
//				}
//				return true;
//			}
//		}
//		if (!owner.equals(segmentController) && !targetMotherShip && !targetOtherTurretShip) {
//			if (EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn() && GameClientState.isDebugObject(segmentController)) {
//				System.err.println(segmentController.getState() + " Cube Result on " + segmentController + ": " + cubeResult.getCubePos() + "; userdata " + cubeResult.getUserData() + " .. " + segmentController.getSectorId() + "/" + owner.getSectorId());
//			}
//			Segment seg = cubeResult.getSegment();
//
//			Vector3f userdata = getParticles().getUserdata(particleIndex, userDataHelper);
//
//			float damage = userdata.x;
//
//			
//			if(owner != null){
//				damage *= owner.getDamageGivenMultiplier();
//			}
//			damage *= seg.getSegmentController().getDamageTakenMultiplier(DamageDealerType.PROJECTILE);
//			
//			userdata = getParticles().getUserdataSecond(particleIndex, userDataHelper);
//			short effectType = (short) userdata.x;
//			assert (effectType == 0 || (ElementKeyMap.isValidType(effectType) && ElementKeyMap.getInfo(effectType).isEffectCombinationController())) : ElementKeyMap.toString(effectType);
//			float effectRatio = userdata.y;
//			float effectSize = userdata.z;
//
//			if (seg.getSegmentController() instanceof EditableSendableSegmentController) {
//				if (!((EditableSendableSegmentController) seg.getSegmentController()).canAttack(owner)) {
//					return true;
//				}
//
//				if (owner != null && owner instanceof SegmentController) {
//					if (((EditableSendableSegmentController) seg.getSegmentController()).getDockingController().getAbsoluteMother() == ((SegmentController) owner).getDockingController().getAbsoluteMother()) {
//						return true;
//					}
//				}
//			}
//			boolean shieldAbsorbed = false;
//			/* hit shields */
//			float damageBeforeShields = damage;
//			if (seg.getSegmentController() instanceof ManagedSegmentController<?>) {
//				ManagerContainer<?> managerContainer = ((ManagedSegmentController<?>) seg.getSegmentController()).getManagerContainer();
//				if (managerContainer instanceof ShieldContainerInterface) {
//					ShieldContainerInterface sc = (ShieldContainerInterface) managerContainer;
//					if (sc.getShieldAddOn().getShields() > 0 || segmentController.railController.isDockedAndExecuted()) {
//						damage = (float) sc.handleShieldHit(owner, cubeResult.hitPointWorld, DamageDealerType.PROJECTILE, damage, effectType, effectRatio, effectSize);
//						if (damage <= 0) {
//							//completely consumed by shield
//							shieldAbsorbed = true;
//							((EditableSendableSegmentController) seg.getSegmentController()).sendHitConfirmToDamager(owner, true);
//
//							if (((EditableSendableSegmentController) seg.getSegmentController()).getEffect(owner, effectType) == null || !((EditableSendableSegmentController) seg.getSegmentController()).getEffect(owner, effectType).isEffectIgnoreShields()) {
//								return true;
//							}
//						}
//					}
//				}
//			}
//			//shield absobtion subtracted from particle
//			getParticles().setUserdataAt(particleIndex, 0, damage);
//
//
//			int blockHitIndex = getParticles().getBlockHitIndex(particleIndex);
//			int blockDeepness = WeaponElementManager.calculateBlockDeepness(damage);
//			if (blockHitIndex == blockDeepness) {
//				return true;
//			}
//
//			particleHitCallback.reset();
//			/*
//			 *we hit as often as we must to accumulate default punchthrough
//			 *
//			 *in case the block gets killed, we fly on to the next update
//			 */
//			while (blockHitIndex < blockDeepness && !particleHitCallback.killedBlock) {
//				float blockIndexDamage = (float) WeaponElementManager.calculateDamageForBlock(blockHitIndex, blockDeepness, damage);
//				float blockIndexDamageBefShields = (float) WeaponElementManager.calculateDamageForBlock(blockHitIndex, blockDeepness, damageBeforeShields);
////				System.err.println(getState()+"    "+blockHitIndex+" / "+blockDeepness+"; Damage: "+blockIndexDamage);
////				particleHitCallback.reset();
//				/* hit regular */
////				seg.getSegmentController().handleHit(particleHitCallback, cubeResult, owner, blockIndexDamage, blockIndexDamageBefShields, posBeforeUpdate, posAfterUpdate, shieldAbsorbed, effectType, effectRatio, effectSize);
//
//				blockHitIndex++;
//			}
//
//			if (!seg.getSegmentController().isOnServer()) {
//
//				Transform t = new Transform();
//				t.setIdentity();
//				t.origin.set(cubeResult.hitPointWorld);
//				HudIndicatorOverlay.toDrawTexts.add(new RaisingIndication(t, StringTools.formatPointZero(particleHitCallback.getDamageDone()), 1, 0, 0, 1));
//				// prepare to add some explosions
//				GameClientState s = (GameClientState) getState();
////				s.getWorldDrawer().getExplosionDrawer().addExplosion(cubeResult);
//				if (particleHitCallback.getDamageDone() < 300) {
//					((GameClientController)getState().getController()).queueTransformableAudio("0022_spaceship enemy - hit small explosion small enemy ship blow up", t, 2, 50);
//				} else if (particleHitCallback.getDamageDone() < 600) {
//					((GameClientController)getState().getController()).queueTransformableAudio("0022_spaceship enemy - hit medium explosion medium enemy ship blow up", t, 2, 100);
//				} else {
//					((GameClientController)getState().getController()).queueTransformableAudio("0022_spaceship enemy - hit large explosion big enemy ship blow up", t, 2, 150);
//				}
//			}
//
//			getParticles().setBlockHitIndex(particleIndex, blockHitIndex);
//			if (blockHitIndex < blockDeepness) {
//				addTocontinous(seg, cubeResult, particleIndex);
//				return checkCollisionOld(owner, posBeforeUpdate, posHelper, particleIndex, false, iteration + 1);
//			}
//
//			int restDamage = (int) Math.max(0, damage - particleHitCallback.getDamageDone());
//
//			if (effectType != 0) {
//				EffectElementManager<?, ?, ?> effect = ((EditableSendableSegmentController) seg.getSegmentController()).getEffect(owner, effectType);//((EffectManagerContainer)((ManagedSegmentController<?>)seg.getSegmentController()).getManagerContainer()).getEffect(effectType);
//				if (effect != null) {
//					if (effect.isPiercing() || effect.isPunchThrough()) {
//						getParticles().setUserdataAt(particleIndex, 0, restDamage);
//					}
//				}
//			}
//
//		}
//		return true;
//	}

	private void copyAdd(ProjectileController to, int indexFrom, Vector3f pPos) {
		int indexTo = to.addEmptyParticle();
		{
			float[] array = getParticles().getArrayFloat();
			//make sure that the 'to' array is referenced after addEmptyParticle
			//or else this references the old array in case it grows
			float[] toArray = to.getParticles().getArrayFloat();
			for (int i = 0; i < ProjectileParticleContainer.blocksizeFloat; i++) {
				toArray[indexTo * ProjectileParticleContainer.blocksizeFloat + i] = array[indexFrom * ProjectileParticleContainer.blocksizeFloat + i];
			}
			
		}
		{
			int[] array = getParticles().getArrayInt();
			//make sure that the 'to' array is referenced after addEmptyParticle
			//or else this references the old array in case it grows
			int[] toArray = to.getParticles().getArrayInt();
			for (int i = 0; i < ProjectileParticleContainer.blocksizeInt; i++) {
				toArray[indexTo * ProjectileParticleContainer.blocksizeInt + i] = array[indexFrom * ProjectileParticleContainer.blocksizeInt + i];
			}
		}
		to.getParticles().setPos(indexTo, pPos.x, pPos.y, pPos.z);
	}
//	@Deprecated
//	private boolean checkCollisionOld(SimpleTransformableSendableObject<?> owner, Vector3f posBeforeUpdate, Vector3f posAfterUpdate, int particleIndex, boolean ignoreDebris, int iteration) {
//	
//			Vector3f userdata = getParticles().getUserdataSecond(particleIndex, userDataHelper);
//			short effectType = (short) userdata.x;
//			float effectRatio = userdata.y;
//			float effectSize = userdata.z;
//	
//	//		rayCallback = (CubeRayCastResult) getPhysics().testRayCollisionPoint(posBeforeUpdate,
//	//				posAfterUpdate, false, owner, null, false, ignoreDebris, piercingCol ? collidingBlocks : null, false, true);
//	
//			rayCallbackInitial.closestHitFraction = 1f;
//			rayCallbackInitial.collisionObject = null;
//			rayCallbackInitial.setSegment(null);
//			rayCallbackInitial.rayFromWorld.set(posBeforeUpdate);
//			rayCallbackInitial.rayToWorld.set(posAfterUpdate);
//			rayCallbackInitial.setFilter(null);
//			rayCallbackInitial.setOwner(owner);
//			rayCallbackInitial.setIgnoereNotPhysical(false);
//			rayCallbackInitial.setIgnoreDebris(ignoreDebris);
////			rayCallback.setCollidingBlocks(piercingCol ? collidingBlocks : null);
//			rayCallbackInitial.setHasCollidingBlockFilter(piercingCol);
//			rayCallbackInitial.setZeroHpPhysical(false);
//			rayCallbackInitial.setDamageTest(true);
//			rayCallbackInitial.setSimpleRayTest(owner.isOnServer() || (FastMath.carmackLength(posBeforeUpdate.x - clientPos.x, posBeforeUpdate.y - clientPos.y, posBeforeUpdate.z - clientPos.z) > 250));
//			
//			((ModifiedDynamicsWorld) currentPhysics.getDynamicsWorld()).rayTest(posBeforeUpdate, posAfterUpdate, rayCallbackInitial);
//			
//			
//			
//			assert (!piercingCol || rayCallbackInitial.getCollidingBlocks() != null);
//			if (rayCallbackInitial.hasHit() && rayCallbackInitial.collisionObject.getUserPointer() != null) {
//				assert (rayCallbackInitial.collisionObject.getUserPointer() != null) : rayCallbackInitial.collisionObject;
//				Sendable particleTarget;
//	
//				if (rayCallbackInitial.collisionObject.getUserPointer() == null || !(rayCallbackInitial.collisionObject.getUserPointer() instanceof Integer)) {
//					System.err.println("Exception: ERROR: projectile cannot hit this object: Userpointer: " + rayCallbackInitial.collisionObject.getUserPointer());
//					return true;
//				}
//				int id = (Integer) rayCallbackInitial.collisionObject.getUserPointer();
//				if (id < 0) {
//					//stuff like lift
//					return true;
//				}
//	
//				particleTarget = state.getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().get(id);
//	
//				if ((rayCallbackInitial.collisionObject.getCollisionShape() instanceof CubeShape || rayCallbackInitial.collisionObject.getCollisionShape() instanceof CubesCompoundShape) && rayCallbackInitial.getSegment() != null) {
//					CubeRayCastResult cubeResult = rayCallbackInitial;
//					return handleCubeCollisionOld(cubeResult, particleIndex, owner, posBeforeUpdate, posAfterUpdate, iteration);
//	
//				}
//				Physical physicalElement = (Physical) particleTarget;
//				if (!rayCallbackInitial.hasHit() || (physicalElement != null && owner.getId() == ((Identifiable) physicalElement).getId())) {
//				} else {
//					if (physicalElement == null) {
//						if (state instanceof GameClientState) {
//							GameClientState ss = (GameClientState) state;
//							ss.getWorldDrawer().getExplosionDrawer().addExplosion(rayCallbackInitial.hitPointWorld);
//						}
//					} else {
//						StateInterface ss = state;
//	
//						//						if(getState() instanceof GameServerState){
//						//							System.err.println("[SERVER][PROJECTILE] HIT: "+result.collisionObject+" ... "+particleTarget+" "+getState());
//						//						}
//						if (particleTarget instanceof Hittable) {
//							//							System.err.println("OBJECT IS HITTABLE: "+particleTarget+"; hitting with "+result.getClass().getSimpleName());
//							userdata = getParticles().getUserdata(particleIndex, userDataHelper);
//							if (rayCallbackInitial.collisionObject.getCollisionShape() instanceof LiftBoxShape) {
//								//not hitting lifts
//							} else {
//								float damage = userdata.x;
//								boolean shieldAbsorbed = false;
//								float damageBeforeShields = damage;
//								particleHitCallback.reset();
////								((Hittable) particleTarget).handleHit(particleHitCallback, rayCallback, owner, damage, damageBeforeShields, posBeforeUpdate, posAfterUpdate, shieldAbsorbed, effectType, effectRatio, effectSize);
//							}
//						}
//	
//					}
//					//								System.err.println("LKSDALAKSDJALKDJS "+owner.getId()+", "+particleTarget.getId());
//					return true;
//	
//				}
//			} else if (rayCallbackInitial.hasHit()) {
//				if (rayCallbackInitial.hasHit() && rayCallbackInitial.collisionObject != null && rayCallbackInitial.collisionObject.getUserPointer() == null) {
//					if (rayCallbackInitial.collisionObject instanceof RigidDebrisBody) {
//						((RigidDebrisBody) rayCallbackInitial.collisionObject).shard.kill();
//						return false;
//					} else {
//						System.err.println("[PROJECTILE] no user pointer for hit object " + rayCallbackInitial.collisionObject);
//					}
//				}
//				//			if(getState() instanceof GameServerState){
//				//				System.err.println("[SERVER][PROJECTILE] DIED: "+result.collisionObject+" ... "+getState());
//				//			}
//				return true;
//			} else {
//	
//				if (isOnServer()) {
//					GameServerState state = (GameServerState) getState();
//					Sector s = ((Sector) getPhysics().getState());
//					for (short missileId : s.getMissiles()) {
//						System.err.println("CHECKING FOR MISSILE HIT "+missileId);
//						Missile hitMissile = state.getController().getMissileController().hasHit(missileId, posBeforeUpdate, posAfterUpdate);
//						if (hitMissile != null && canHitMissile(hitMissile, owner)) {
//							userdata = getParticles().getUserdata(particleIndex, userDataHelper);
//							float damage = userdata.x;
//							hitMissile.hitByProjectile(damage);
//							
//						}
//					}
//				}
//			}
//			return false;
//		}

	public PhysicsExt getPhysics() {
		if (state instanceof GameServerState) {
			Sector sector = ((GameServerState) state).getUniverse().getSector(sectorId);
			if(sector == null){
				return null;
			}
			return (PhysicsExt) sector.getPhysics();
		} else {
			return (PhysicsExt) ((GameClientState) state).getPhysics();
		}
	}

	/**
	 * @return the state
	 */
	public StateInterface getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(StateInterface state) {
		this.state = state;
	}

	public void setSectorId(int id) {
		this.sectorId = id;
	}

	public Vector3f secPos(Sector newSector, Sector oldSector, Vector3f from) throws IOException {
		oldTrans.setIdentity();

		newTrans.setIdentity();

		//calculate pos as if normal
		float year = ((GameStateInterface) state).getGameState().getRotationProgession();

		//use the same system pos, weben if the seconds isnt part of the
		//same system. the pc is 0 anyway
		Vector3i sysPos = StellarSystem.getPosFromSector(oldSector.pos, syspos);

		Vector3i dir = new Vector3i();
		dir.sub(newSector.pos, oldSector.pos);

		Vector3f otherSecCenter = new Vector3f(
				dir.x * ((GameStateInterface) state).getSectorSize(),
				dir.y * ((GameStateInterface) state).getSectorSize(),
				dir.z * ((GameStateInterface) state).getSectorSize());

		Transform t = new Transform();
		t.setIdentity();
		t.origin.set(from);
		Matrix3f rot = new Matrix3f();
		rot.rotX((FastMath.PI * 2) * year);

		Transform newSectorTrans = new Transform();
		newSectorTrans.setIdentity();
		newSectorTrans.origin.set(otherSecCenter);

		Transform add = new Transform();
		add.setIdentity();
		add.origin.set(otherSecCenter);
		add.origin.negate();
		if (oldSector.getSectorType() != SectorType.PLANET && newSector.getSectorType() == SectorType.PLANET) {
			//normal -> planet
			//calculate postion as seen from new sector
			TransformTools.rotateAroundPoint(new Vector3f(), rot, newSectorTrans, transTmp);
		} else {
			newSectorTrans.origin.set(otherSecCenter);
		}
		newSectorTrans.inverse();
		//		t.inverse();
		//new sector's view of the object
		newSectorTrans.mul(t);

		t.set(newSectorTrans);

		return t.origin;
	}

	private boolean translateSector(Vector3f inout, int index) {
		assert (state instanceof GameServerState);
		Vector3f in = new Vector3f(inout);
		Sector current = ((GameServerState) state).getUniverse().getSector(sectorId);
		if (current != null) {
			Vector3i pos = current.pos;
			int nearest = -1;
			Vector3f nearVector = new Vector3f(in);
			Vector3i belongingVector = new Vector3i();
			for (int i = 0; i < Element.DIRECTIONSi.length; i++) {
				Vector3i test = new Vector3i(Element.DIRECTIONSi[i]);
				test.add(pos);
				Transform trans = new Transform();
				trans.setIdentity();

				trans.origin.set(Element.DIRECTIONSi[i].x, Element.DIRECTIONSi[i].y, Element.DIRECTIONSi[i].z);
				trans.origin.scale(((GameStateInterface) state).getSectorSize());

				Vector3f dist = new Vector3f();
				dist.sub(in, trans.origin);

				if (dist.lengthSquared() < nearVector.lengthSquared()) {
					nearVector.set(dist);
					nearest = i;
				}
			}
			if (nearest >= 0) {
				belongingVector.set(pos);
				belongingVector.add(Element.DIRECTIONSi[nearest]);

			} else {
				return false; //no sector change
			}

			//			Vector3i newPos = new Vector3i(current.pos);
			//
			//			if(posHelper.x > ((GameStateInterface)state).getSectorSize()/2){
			//				newPos.x ++;
			//			}
			//			if(posHelper.y > ((GameStateInterface)state).getSectorSize()/2){
			//				newPos.y ++;
			//			}
			//			if(posHelper.z > ((GameStateInterface)state).getSectorSize()/2){
			//				newPos.z ++;
			//			}
			//			if(posHelper.x < ((GameStateInterface)state).getSectorSize()/2){
			//				newPos.x --;
			//			}
			//			if(posHelper.y < ((GameStateInterface)state).getSectorSize()/2){
			//				newPos.y --;
			//			}
			//			if(posHelper.z < ((GameStateInterface)state).getSectorSize()/2){
			//				newPos.z --;
			//			}
			try {
				if (((GameServerState) state).getUniverse().isSectorLoaded(belongingVector)) {
					Sector newSector = ((GameServerState) state).getUniverse().getSector(belongingVector, false);
					if (newSector != null && newSector.isActive()) {
						try {
							if (current != null && newSector != null && current.getSectorType() != SectorType.PLANET && newSector.getSectorType() == SectorType.PLANET) {
								inout.set(secPos(newSector, current, in));
							} else {
								translateSector(state, current, newSector, in, inout);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}

						copyAdd(newSector.getParticleController(), index, inout);
						//									System.err.println("[SERVER][PROJECTILE] Sector transition from "+current.pos+" to "+newSector.pos+"; -> "+inout);
						return true;
					} else {
						if (newSector != null && newSector.isActive()) {
							System.err.println("[SERVER][PROJECTILE] not translating projectile to inactive sector");
						}
						return false;
					}
				} else {
					//DO NOT LOAD SECTORS: projectile updates are happening in a sectors iterator, so adding sectors
					//would fuck up the iterator
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			;

		} else {
			System.err.println("[SERVER][PROJECTILE] Stopping projectile: out of loaded sector range");
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.particle.ParticleController#deleteParticle(int)
	 */
	@Override
	public void deleteParticle(int toDelete) {
//		continuousCollisions.remove(getParticles().getId(toDelete));
		super.deleteParticle(toDelete);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.particle.ParticleController#onUpdateStart()
	 */
	@Override
	protected void onUpdateStart() {
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.particle.ParticleController#reset()
	 */
	@Override
	public void reset() {
		super.reset();
	}

	@Override
	public boolean updateParticle(int particleIndex, Timer timer) {
		getParticles().getVelocity(particleIndex, velocityHelper);
		//		getParticles().getColor(i, colorHelper);
		getParticles().getPos(particleIndex, posHelper);
		getParticles().getStart(particleIndex, startHelper);

		posBeforeUpdate.set(posHelper);

		float lived = getParticles().getLifetime(particleIndex);

		if (velocityHelper.length() == 0) {
			getParticles().setLifetime(particleIndex, lived + timer.getDelta());
		} else {
			velocityHelper.scale((timer.getDelta()));
			getParticles().setLifetime(particleIndex, lived + velocityHelper.length());
			posHelper.add(velocityHelper);
		}
//		System.err.println("Lived: "+lived);
		posAfterUpdate.set(posHelper);
		//		System.err.println(timer.getDelta()+" on "+state+": "+posHelper);
		//		System.err.println("put particle: "+posHelper+", "+velocityHelper);
		try {


		float maxDistance = getParticles().getMaxDistance(particleIndex);
		final int ownerId = getParticles().getOwnerId(particleIndex);
		long usableId = getParticles().getWeaponId(particleIndex);
		Identifiable owner;
		
		if(usableId == PlayerUsableInterface.USABLE_ID_MINE_SHOOTER) {
			owner = ((MineInterface)state.getController()).getMineController().getMine(ownerId);
		}else {
			owner = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(ownerId);
		}

		if (owner instanceof Damager) {


			int particleId = getParticles().getId(particleIndex);

			if (owner instanceof SimpleTransformableSendableObject<?> && !((SimpleTransformableSendableObject<?>)owner).isOnServer() && !((SimpleTransformableSendableObject<?>)owner).isInClientRange()) {
				return false;
			}

			boolean stopParticle = checkCollision( (Damager)owner, posBeforeUpdate, posAfterUpdate, particleIndex, false);//, 0);
			
			getParticles().setPos(particleIndex, posAfterUpdate.x, posAfterUpdate.y, posAfterUpdate.z);
			if(stopParticle) {
				return false;
			}
			if (!stopParticle && state instanceof GameServerState && (
					Math.abs(posHelper.x) > ((GameStateInterface) state).getSectorSize() / 3 ||
							Math.abs(posHelper.y) > ((GameStateInterface) state).getSectorSize() / 3 ||
							Math.abs(posHelper.z) > ((GameStateInterface) state).getSectorSize() / 3)) {
				if (translateSector(posHelper, particleIndex)) {
					return false;
				}
			}
//			System.err.println("Lived::: "+lived+"; "+maxDistance);
			return lived < maxDistance && !stopParticle;
		} else {
			System.err.println("Exception: No owner for particle found: " + ownerId + " -> " + owner);
			return false;
		}
		}finally {
			//update projectil to new position
			getParticles().setPos(particleIndex, posAfterUpdate.x, posAfterUpdate.y, posAfterUpdate.z);
		}

	}
	Vector3f clientPos = new Vector3f();
	private PhysicsExt currentPhysics;
	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.particle.ParticleController#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer) {
		currentPhysics = null;
		if (getParticleCount() > 0) {
			currentPhysics = getPhysics();
			if(currentPhysics != null){
				if(state instanceof GameClientState){
					clientPos.set(Controller.getCamera().getPos());
				} else {
				}
				
				if (currentPhysics.getDynamicsWorld().getNumCollisionObjects() > 32) {
					((ModifiedDynamicsWorld) currentPhysics.getDynamicsWorld()).buildCache();
				}
				super.update(timer);
	
				((ModifiedDynamicsWorld) currentPhysics.getDynamicsWorld()).cacheValid = false;
			}else{
				System.err.println("Exception: Projectile physics null for sector "+sectorId);
			}
		}
	}

	public PhysicsExt getCurrentPhysics() {
		return currentPhysics;
	}

	public void setCurrentPhysics(PhysicsExt currentPhysics) {
		this.currentPhysics = currentPhysics;
	}

	@Override
	protected ProjectileParticleContainer getParticleInstance(int size) {
		return new ProjectileParticleContainer(size);
	}

	public int getSectorId() {
		return sectorId;
	}

}
