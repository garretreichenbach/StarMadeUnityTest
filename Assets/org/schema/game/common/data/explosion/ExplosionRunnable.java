package org.schema.game.common.data.explosion;

import api.listener.events.weapon.ExplosionEvent;
import api.listener.fastevents.FastListenerCommon;
import api.listener.fastevents.segmentpiece.SegmentPieceDamageListener;
import api.mod.StarLoader;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.linearmath.AabbUtil2;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.schema.common.FastMath;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.Hittable;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.elements.*;
import org.schema.game.common.controller.elements.armorhp.ArmorHPCollection;
import org.schema.game.common.data.BlockBulkSerialization;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementDocking;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.*;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.world.*;
import org.schema.game.network.objects.remote.RemoteBlockBulk;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.objects.remote.RemoteVector4f;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

public class ExplosionRunnable implements Runnable {

	private static final ExplosionDataHandler dataHandler = new ExplosionDataHandler();
	private static final List<ExplosionCollisionSegmentCallback> pool = new ObjectArrayList<ExplosionCollisionSegmentCallback>();
	private static ObjectOpenHashSet<Segment> lockedSegments = new ObjectOpenHashSet<Segment>(512);
	private static int running;
	private static boolean init;
	private SphereShape sphereBlast;
	private ExplosionData explosion;
	private Sector sector;
	private ObjectArrayList<Sendable> hitBuffer = new ObjectArrayList<Sendable>();
	private ExplosionCollisionSegmentCallback callback;

	public ExplosionRunnable(ExplosionData e, Sector sector) {
		this.explosion = e;
		this.sector = sector;
	}

	public static void initialize() {
		synchronized(pool){
			if(!init){
				dataHandler.loadData();
				for (int i = 0; i < 4; i++) {
					pool.add(new ExplosionCollisionSegmentCallback(dataHandler));
				}
				init = true;
			}
		}
	}

	private static ExplosionCollisionSegmentCallback getCallback() {
		synchronized (pool) {
			if (!pool.isEmpty()) {
				return pool.remove(pool.size() - 1);
			}
			return new ExplosionCollisionSegmentCallback(dataHandler);
		}

	}

	private static void freeCallback(ExplosionCollisionSegmentCallback cb) {
		synchronized (pool) {
			int size = ServerConfig.MAX_EXPLOSION_POOL.getInt();
			if(size <= 0 || pool.size() < size) {
				pool.add(cb);
			}
		}
	}

	public GameServerState getState() {
		return sector.getState();
	}

	public PhysicsExt getPhysics() {
		return (PhysicsExt) sector.getPhysics();
	}

	public boolean canExecute() {
		return running < ServerConfig.MAX_SIMULTANEOUS_EXPLOSIONS.getInt();
	}

	public boolean readyToRun() {
		return areSegmentsLocked();
	}

	private boolean areSegmentsLocked() {
		synchronized (lockedSegments) {
			return Collections.disjoint(lockedSegments, callback.ownLockedSegments);
		}
	}

	public boolean beforeExplosion() {
		PhysicsExt physics = getPhysics();
		if (physics == null) {
			System.err.println("[SERVER][WARNING] not spawned missile in unloaded sector: " + sector);
			return false;
		}
		DynamicsWorld dynamicsWorld = physics.getDynamicsWorld();
		if (dynamicsWorld == null) {
			System.err.println("[SERVER][WARNING] not spawned missile in unloaded sector (dynWorld): " + sector);
			return false;
		}

		synchronized (pool) {
			running++;
		}

		callback = getCallback();
		callback.reset();

		hitBuffer.clear();

		Vector3f aabbMin = new Vector3f();
		Vector3f aabbMax = new Vector3f();

		Vector3f aabbMinO = new Vector3f();
		Vector3f aabbMaxO = new Vector3f();
		sphereBlast = new SphereShape(explosion.radius);
		sphereBlast.getAabb(explosion.centerOfExplosion, aabbMin, aabbMax);

		List<CollisionObject> collisionObjectArray = dynamicsWorld.getCollisionObjectArray();

		ObjectArrayList<CollisionObject> overlapping = new ObjectArrayList<CollisionObject>();
		Transform tmp = new Transform();
		for (int i = 0; i < collisionObjectArray.size(); i++) {
			CollisionObject collisionObject = collisionObjectArray.get(i);

			if (!(collisionObject instanceof PairCachingGhostObjectUncollidable) && !(collisionObject instanceof RigidDebrisBody)) {

				collisionObject.getCollisionShape().getAabb(collisionObject.getWorldTransform(tmp), aabbMinO, aabbMaxO);

				if (AabbUtil2.testAabbAgainstAabb2(aabbMin, aabbMax, aabbMinO, aabbMaxO)) {
					overlapping.add(collisionObject);
				}
			}
		}
		for (int i = 0; i < overlapping.size(); ++i) {
			CollisionObject colObject = overlapping.get(i);
			CollisionObject obj = colObject;

//			System.err.println(this+" HIT BOADPHASE WITH "+colObject+" ");
			// Now you have one object. Do something here
			//								System.err.println("OVERLAPPING USR POINTER: "+obj.getUserPointer());

			if (obj.getUserPointer() != null && obj.getUserPointer() instanceof Integer) {
				int id = (Integer) obj.getUserPointer();

				Sendable sendable = getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(id);
				Damager owner = explosion.from;
				if (!explosion.hitsFromSelf && sendable == owner) {
					continue;
				}
				if(sendable != null && owner != null && sendable instanceof SegmentController && owner instanceof SegmentController){
					SegmentController from = (SegmentController)owner;
					SegmentController to = (SegmentController)sendable;
					
					if(from.railController.isInAnyRailRelationWith(to)){
						if (!explosion.hitsFromSelf) {
							//dont hit own docked
							continue;
						}else if(explosion.ignoreShieldsSelf){
							//dont hit shields of own docked
							callback.entitiesToIgnoreShieldsOn.add(to.getId());
						}
					}
					
				}
				
				if (sendable != null && sendable instanceof Hittable) {
					
					if(!((Hittable)sendable).isPhysicalForDamage()){
						continue;
					}
					
					if (sendable instanceof SegmentController && owner != null && owner instanceof SegmentController) {
						SegmentController c = (SegmentController) sendable;

						if (!explosion.hitsFromSelf && c.getDockingController().isDocked() && (c.getDockingController().isInAnyDockingRelation((SegmentController) owner))) {
							continue;
						}

						if (!explosion.hitsFromSelf && c.railController.isInAnyRailRelationWith((SegmentController) owner)) {
							continue;
						}
					}
					if (!((Hittable)sendable).canBeDamagedBy(explosion.from, DamageDealerType.MISSILE)) {
						continue;
					}
					if (((Hittable) sendable).checkAttack(explosion.from, explosion.damageType != DamageDealerType.EXPLOSIVE, explosion.damageType != DamageDealerType.EXPLOSIVE)) {

						hitBuffer.add(sendable);
						if (sendable instanceof SegmentController) {
							SegmentController seg = (SegmentController) sendable;
							for (ElementDocking dock : seg.getDockingController().getDockedOnThis()) {
								hitBuffer.add(dock.from.getSegment().getSegmentController());
							}
							seg.railController.getRoot().railController.getAll(hitBuffer);

						}
					}
				}
			}
		}

		ExplosionPhysicsSegemtsChecker checker = new ExplosionPhysicsSegemtsChecker();

		callback.centerOfExplosion.set(explosion.centerOfExplosion.origin);
		callback.explosionRadius = explosion.radius;

		callback.hitType = explosion.hitType;

		callback.attack = explosion.attackEffectSet;
		assert(explosion.attackEffectSet != null);
		callback.ignoreShieldsGlobal = explosion.ignoreShields;
	
		callback.useLocalShields = true;
		callback.damageType = explosion.damageType;
		callback.weaponId = explosion.weaponId;

		callback.shieldDamageBonus = VoidElementManager.EXPLOSION_SHIELD_DAMAGE_BONUS;

		//INSERTED CODE
		ExplosionEvent explosionEvent = new ExplosionEvent(this, this.sector, this.explosion);
		StarLoader.fireEvent(explosionEvent, true);
		if(explosionEvent.isCanceled()){
			return false;
		}
		///

		if(explosion.damageType == DamageDealerType.MISSILE) { //doesn't apply to warheads and sun damage which don't even have a proper from and to, nor bombs which should ignore armour
			retrieveArmorInfo(explosion.to, explosion.fromPos, explosion.toPos);
			explosion.damageInitial = calcPreviousArmorDamageReduction(explosion.damageInitial);
		}

		assert (callback.cubeCallbackPointer == 0);
		for (Sendable s : hitBuffer) {
			if (s instanceof Hittable && ((Hittable) s).isVulnerable()) {
				if (((Hittable) s).checkAttack(explosion.from, explosion.damageType != DamageDealerType.EXPLOSIVE, explosion.damageType != DamageDealerType.EXPLOSIVE)) {
					if (s instanceof SegmentController) {
						SegmentController c = (SegmentController) s;
						CubeShape cs = (CubeShape) c.getPhysicsDataContainer().getShapeChild().childShape;
						
						Transform t = c.getWorldTransform();
						
						if(explosion.sectorId != c.getSectorId()) {
							Sector explSec = ((GameServerState)c.getState()).getUniverse().getSector(explosion.sectorId);
							Sector objSec = ((GameServerState)c.getState()).getUniverse().getSector(c.getSectorId());
							
							if(explSec == null || objSec == null) {
//								System.err.println("[EXPLOSION] ERROR: SECTOR NOT LOADED: "+explosion.sectorId+": "+explSec+"; "+c.getSectorId()+": "+objSec);
								continue;
							}else {
								c.calcWorldTransformRelative(explSec.getSectorId(), explSec.pos);
								t = c.getClientTransform();
							}
						}
						
						checker.processCollision(cs, t, sphereBlast, explosion.centerOfExplosion, callback);
					} else if (s instanceof AbstractCharacter<?>) {
						callback.addCharacterHittable((AbstractCharacter<?>) s);
					}
				}
			}
		}

//		System.err.println("[SERVER] MISSILE CALLBACK RETURN FOR "+hitBuffer.size()+" objects. Reported: "+callback.cubeCallbackPointer+" hits!");

		try {
			callback.sortInsertShieldAndArmorValues(explosion.centerOfExplosion.origin, explosion.sectorId, hitBuffer);
		} catch (SectorNotFoundException e) {
			e.printStackTrace();
			explosion.damageInitial = 0;
		}
		return true;
	}

	@Override
	public void run() {

		synchronized (lockedSegments) {
			boolean needsUpdate = false;
			while (!readyToRun()) {
				needsUpdate = true;
				try {
					lockedSegments.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (needsUpdate) {
				callback.updateCallbacks();
			}
			lockedSegments.addAll(callback.ownLockedSegments);
		}

		//do unsynched threaded stuff here

		if (!hitBuffer.isEmpty()) {

//			System.err.println("[SERVER] Now applying missile damage to blocks threaded");

			callback.sortAndInsertCallbackCache();

			
			
			float damage = explosion.damageInitial * (explosion.from != null ? explosion.from.getDamageGivenMultiplier() : 1f);
			
//			if (callback.effect != null) {
//				damage = (int) callback.effect.modifyTotalDamage(damage, callback.damageType, callback.effectRatio);
//			}

			//this method is thread save with a thread individual callback
			dataHandler.applyDamage(callback, (int) explosion.radius, damage);

//			System.err.println("[SERVER] Finished applying missile damage to blocks threaded");
			//notify state of doing the finish in synch
		}
		synchronized (getState().getExplosionOrdersFinished()) {
			getState().getExplosionOrdersFinished().enqueue(this);
		}

	}

	/**
	 * called from the server controller
	 * <p/>
	 * this method is synched
	 */
	public void afterExplosion() {
//		System.err.println("[SERVER] Executing after explosion Hook");
		applyCallbackDamage(callback);
		synchronized (lockedSegments) {
			lockedSegments.removeAll(callback.ownLockedSegments);
			lockedSegments.notifyAll();
		}

		freeCallback(callback);

		callback = null;
//		System.err.println("[SERVER] DONE Executing after explosion Hook");

		synchronized (pool) {
			running--;
		}
	}
	private final static InterEffectSet voidEffectSet = new InterEffectSet();
	private void applyCallbackDamage(ExplosionCollisionSegmentCallback cb) {
		ExplosionCubeConvexBlockCallback[] d = cb.callbackCache;

		SegmentData lastData = null;
		cb.hitSegments.clear();
		cb.hitSendSegments.clear();

		long time = System.currentTimeMillis();
		ByteArrayList byteArrayList = null;
		Long2ObjectOpenHashMap<ByteArrayList> seg = null;
		int totalDamageDone = 0;
		cb.sentDamage.clear();
		try {
			for (int i = 0; i < cb.cubeCallbackPointer; i++) {
				ExplosionCubeConvexBlockCallback e = d[i];

				if (e.type == ExplosionCubeConvexBlockCallback.CHARACTER) {

					Sendable ss = getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(e.segEntityId);

					if (ss != null && ss instanceof AbstractCharacter<?>) {

						AbstractCharacter<?> ac = (AbstractCharacter<?>) ss;

						if (e.blockHpOrig > e.blockHp) {
							ac.getOwnerState().damage(e.blockHpOrig - e.blockHp, ac, explosion.from);

//							System.err.println("[EXPLOSION] CHARACTER HIT "+ac+"; HP after: "+e.blockHp);
						} else {
//							System.err.println("[EXPLOSION] CHARACTER -NOT- HIT "+ac);
						}

					}

				} else if (e.type == ExplosionCubeConvexBlockCallback.SEGMENT_CONTROLLER) {
					try {
						e.data.checkWritable();
					} catch (SegmentDataWriteException e1) {
						//THIS SHOULD NOT HAPPEN. DATA SHOULD BE REPLACE BEFORE RUNNABLE IS STARTED
						e1.printStackTrace();
						assert(e.data.getSegment().getSegmentData() == e.data):e.data.getSegment().getSegmentData()+"; "+e.data;
						e.data = SegmentDataWriteException.replaceDataOnServer(e.data);
					}
					
					if (e.data.getSegment() == null) {
						Sendable sendable = getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(e.segEntityId);
						if (sendable == null || !(sendable instanceof SegmentController)) {
							continue;
						}
						SegmentController bck = (SegmentController) sendable;

						Segment segment = bck.getSegmentBuffer().get(e.segmentPos);

						if (segment.isEmpty()) {
							continue;
						}
						e.data = segment.getSegmentData();

					}
					try {
						e.data.checkWritable();
					} catch (SegmentDataWriteException e1) {
						e1.printStackTrace();
						e.data = SegmentDataWriteException.replaceDataOnServer(e.data);
					}
					if (lastData != e.data) {
						if (lastData != null) {
							lastData.rwl.writeLock().unlock();
						}
						lastData = e.data;
						lastData.rwl.writeLock().lock();

						cb.hitSegments.add(e.data.getSegment());

						seg = cb.hitSendSegments.get(e.data.getSegmentController());

						if (seg == null) {
							seg = new Long2ObjectOpenHashMap();
							cb.hitSendSegments.put(e.data.getSegmentController(), seg);

						}

						long sedIndex = ElementCollection.getIndex(e.data.getSegment().pos);
						byteArrayList = seg.get(sedIndex);

						if (byteArrayList == null) {
							byteArrayList = BlockBulkSerialization.getBufferServer();
							seg.put(sedIndex, byteArrayList);
						}

						cb.hitSegments.add(e.data.getSegment());
					}

					Segment segment = e.data.getSegment();
					if (!segment.isEmpty()) {
						short oldType = e.data.getType(e.segDataIndex);
						int oldHitpoints = e.blockHpOrig;
						//don't do any action if block has already been taken out (multiple missiles at the same time)
						if (oldType != Element.TYPE_NONE && e.blockHp != oldHitpoints) {

							int damage = (int) Math.max(0, (oldHitpoints - e.blockHp));
							///INSERTED CODE
							for(SegmentPieceDamageListener l : FastListenerCommon.segmentPieceDamageListeners){
								damage = l.onBlockDamage(segment.getSegmentController(),ElementCollection.getIndex(e.blockPos),oldType,damage,explosion.damageType,explosion.from,segment.getSegmentController().isOnServer());
							}
							/*!!!IDIOSYNCRACY:
							 * Because the explosion callback internally computes the final block HP
							 * instead of returning the damage shared to it, the damage given to the listener
							 * is NOT the 'true' damage inflicted; rather it is limited by the HP of the block that gets hit.
							*/
							////
							if (damage > 0) {
								damage += damage * VoidElementManager.EXPLOSION_HULL_DAMAGE_BONUS;
								e.blockHp = (int) Math.max(0, Math.min(oldHitpoints, oldHitpoints - damage));
							}

							int newHP;

							//					System.err.println("####### SENDING DAMAGE: "+(byteArrayList.getByte(byteArrayList.size()-3)&255)+"; "+(byteArrayList.getByte(byteArrayList.size()-2)&255)+"; "+(byteArrayList.getByte(byteArrayList.size()-1)&255)+" ;;; "+e.segPosX+", "+e.segPosY+", "+e.segPosZ+"; hp "+e.blockHp+"; segmentPos: "+segment.pos);

							if (oldHitpoints - damage <= 0) {

								newHP = 0;
								//block destroyed
								boolean wasActive = e.data.isActive(e.segDataIndex);
								byte oldOrientation = e.data.getOrientation(e.segDataIndex);

								if (oldType != ElementKeyMap.CORE_ID) {
									e.data.setType(e.segDataIndex, (short) 0);
								}
								e.data.setHitpointsByte(e.segDataIndex, 0);
								SegmentController c = e.data.getSegmentController();
								if (oldType != ElementKeyMap.CORE_ID) {
									e.data.onRemovingElement(e.segDataIndex, e.segPosX, e.segPosY, e.segPosZ, oldType, false, false, oldOrientation, wasActive, false, time, false);
								}
								
								if(explosion.chain){
									SegmentPiece p = new SegmentPiece(e.data.getSegment(), e.segPosX, e.segPosY, e.segPosZ);
									p.setType(oldType);
									c.onBlockKill(p, explosion.from);
								}

								c.getHpController().onElementDestroyed(
										explosion.from, ElementKeyMap.getInfo(oldType), DamageDealerType.MISSILE, explosion.weaponId);

								

							} else {
								newHP = ElementKeyMap.convertToByteHP(oldType, (oldHitpoints - damage));
								//block damaged
								e.data.setHitpointsByte(e.segDataIndex, (byte)newHP);

							}

							byteArrayList.add(e.segPosX);
							byteArrayList.add(e.segPosY);
							byteArrayList.add(e.segPosZ);
							byteArrayList.add((byte) (newHP));

							if (!cb.sentDamage.contains(e.data.getSegmentController().getId())) {
								
								boolean selfHit = explosion.from != null && explosion.from instanceof SegmentController && ((SegmentController)explosion.from).railController.isInAnyRailRelationWith(e.data.getSegmentController());
								
								if(!selfHit){
									e.data.getSegmentController().sendHitConfirmToDamager(explosion.from, false);
								}

								cb.sentDamage.add(e.data.getSegmentController().getId());
							}

							totalDamageDone += damage;
						}
					} else {
						//				System.err.println("####### SENDING EMPTY: from update block "+e.segPosX+", "+e.segPosY+", "+e.segPosZ+"; hp "+e.blockHp+"; segmentPos: "+segment.pos);

						//everything is empty. this is a
						//non ambigious way, since all other entries that are not
						//in empty segments have at least 3 or 0
						byteArrayList.clear();
						byteArrayList.add((byte) 0);
					}
					//so removed ojects can be garbage collected
					e.data = null;
				}
			}
		} catch (SegmentDataWriteException e1) {
			e1.printStackTrace();
			throw new RuntimeException("SegmentData should already be normal version", e1);
		} finally {
			if (lastData != null) {

				lastData.rwl.writeLock().unlock();
			}
		}

//		System.err.println("[EXPLOSION] Damage of blast: "+explosion.damageInitial+"; damage done to blocks: "+totalDamageDone);
		for (Segment s : cb.hitSegments) {

			((RemoteSegment) s).setLastChanged(time);
			if (s.isEmpty() && s.getSegmentData() != null) {
				SegmentData segmentData = ((RemoteSegment) s).getSegmentData();
				segmentData.getSegmentController().getSegmentProvider().addToFreeSegmentDataFast(s.getSegmentData());
			} else if (s.getSegmentData() != null) {
				s.getSegmentController().getSegmentProvider().enqueueAABBChange(s);
			}

		}
		//needs extra iteration because sharing
		//can affect entities that are not hit by the actual explosion
		for (int cid : cb.shieldMap.keySet()) {

			boolean hasShieldsLeft = false;
			double shields = cb.shieldMap.get(cid);
			double shieldsBef = cb.shieldMapBef.get(cid);

			if (shields == shieldsBef) {
				continue;
			}

			ExplosionCubeConvexBlockCallback shieldHit = cb.shieldHitPosMap.get(cid);

			Sendable sendable = getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().get(cid);

			if (sendable == null || !(sendable instanceof SegmentController)) {
			} else {
				SegmentController sc = (SegmentController) sendable;

				ShieldAddOn shieldAddOn = ((ShieldContainerInterface) ((ManagedSegmentController<?>) sc).getManagerContainer()).getShieldAddOn();

				double shieldDamage = Math.max(0, shieldsBef - shields);
				if (shields != shieldsBef && shieldHit != null) {

					
					if(shieldAddOn.isUsingPowerReactors()){
						ShieldLocalAddOn localShields = shieldAddOn.getShieldLocalAddOn();
						
						long localId = cb.shieldLocalMap.get(cid);
						
						ShieldLocal shieldLocal = localShields.getLocalShieldMap().get(localId);
						if(shieldLocal != null){
							
							if (shields <= 0) {
								//take out all to account for at least one frame of recharge
								//as the damage ran on a thread
								//the callback will signal -1 if in the running all shield got taken out
								//from the time it cloned the structure status into the thead's data
								
								shieldDamage = Math.ceil(shieldLocal.getShields());
							}
							
							try {
								shieldAddOn.handleShieldHit(explosion.from, 
										voidEffectSet, //damage to shields is already calculated
										shieldHit.boxTransform.origin, explosion.sectorId,
										explosion.damageType,
										explosion.hitType,
										shieldDamage, explosion.weaponId);
							} catch (SectorNotFoundException e) {
								e.printStackTrace();
								return;
							}
							System.err.println("LOCAL SHIELDS HIT:::: "+shieldLocal);
							localShields.sendShieldUpdate(shieldLocal);
						
							hasShieldsLeft = shieldLocal.getShields() > 0;
							if (shieldHit != null) {
								boolean selfHit = 
										explosion.from != null && 
										explosion.from instanceof SegmentController && 
										((SegmentController)explosion.from).railController.isInAnyRailRelationWith(sc);
								
								if(!selfHit){
									//send pos to client to display shield hit
									sc.sendHitConfirmToDamager(explosion.from, true);
								}
	
								shieldAddOn.sendShieldHit(shieldHit.boxTransform.origin, (int) shieldDamage);
	
							}
						}
						
					}else{
						if (shields <= 0) {
							//take out all to account for at least one frame of recharge
							//as the damage ran on a thread
							//the callback will signal -1 if in the running all shield got taken out
							//from the time it cloned the structure status into the thead's data
							
							shieldDamage = Math.ceil(shieldAddOn.getShields());
						}
						try {
							shieldAddOn.handleShieldHit(
									explosion.from,
									voidEffectSet,
									shieldHit.boxTransform.origin, explosion.sectorId, 
									explosion.damageType,
									explosion.hitType, shieldDamage, explosion.weaponId);
						} catch (SectorNotFoundException e) {
							e.printStackTrace();
							return;
						}
						

						shieldAddOn.sendShieldUpdate();
					
						hasShieldsLeft = shieldAddOn.getShields() > 0;
						if (shieldHit != null) {
							boolean selfHit = explosion.from != null && explosion.from instanceof SegmentController && ((SegmentController)explosion.from).railController.isInAnyRailRelationWith(sc);
							
							if(!selfHit){
								//send pos to client to display shield hit
								sc.sendHitConfirmToDamager(explosion.from, true);
							}

							shieldAddOn.sendShieldHit(shieldHit.boxTransform.origin, (int) shieldDamage);

						}
					}
				}
			}

		}

		for (int cid : cb.hitPosMap.keySet()) {

			if (cb.appliedDamageMap.containsKey(cid) && cb.hitPosMap.containsKey(cid)) {
				ExplosionCubeConvexBlockCallback explosionCubeConvexBlockCallback = cb.hitPosMap.get(cid);
				ExplosionCubeConvexBlockCallback hit = cb.hitPosMap.get(cid);
				Sendable sendable = getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().get(cid);

				if (sendable == null || !(sendable instanceof SegmentController) || cb.appliedDamageMap.get(cid) <= 0) {
				} else {
					SegmentController sc = (SegmentController) sendable;
					boolean selfHit = explosion.from != null && explosion.from instanceof SegmentController && ((SegmentController)explosion.from).railController.isInAnyRailRelationWith(sc);
					
						sc.getNetworkObject().hits.add(new RemoteVector4f(new Vector4f(hit.boxTransform.origin.x, hit.boxTransform.origin.y, hit.boxTransform.origin.z, 
								selfHit ? -cb.appliedDamageMap.get(cid) : cb.appliedDamageMap.get(cid)), true));
				}
			}

		}
		ObjectIterator<Entry<SegmentController, Long2ObjectOpenHashMap<ByteArrayList>>> iterator = cb.hitSendSegments.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<SegmentController, Long2ObjectOpenHashMap<ByteArrayList>> entry = iterator.next();
			SendableSegmentController sc = (SendableSegmentController) entry.getKey();

			if (totalDamageDone > 0 && sc instanceof TransientSegmentController ) {
				((TransientSegmentController) sc).setTouched(true, true);
			}
			if (totalDamageDone > 0) {
				((EditableSendableSegmentController)sc).onDamageServerRootObject(totalDamageDone, explosion.from);
			}
//			if (cb.appliedDamageMap.containsKey(sc.getId())) {
//				float totalDamageApplied = cb.appliedDamageMap.get(sc.getId());
//
//				if (explosion.effect != null) {
//					explosion.effect.onHit(sc);
//					float powerDamage = explosion.effect.getPowerDamage(totalDamageApplied, explosion.effectRatio, sc.getBlockEffectManager().status);
//					powerDamage(powerDamage, sc);
//
//				}
//
//			}

			boolean hasShieldsLeft = false;
			if (sc instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) sc).getManagerContainer() instanceof ShieldContainerInterface) {
				ShieldAddOn shieldAddOn = ((ShieldContainerInterface) ((ManagedSegmentController<?>) sc).getManagerContainer()).getShieldAddOn();
				hasShieldsLeft = shieldAddOn.getShields() > 0;
			}
//			if (explosion.effect != null && (!hasShieldsLeft || explosion.effect.isEffectIgnoreShields())) {
//				explosion.effect.onHit(sc);
//
//				float pushForce = 0;
//				float pullForce = 0;
//				float grabForce = 0;
//				if (explosion.damageType == DamageDealerType.MISSILE) {
//					pushForce = explosion.effect.getMissilePush() * explosion.effectRatio * explosion.effectSize;
//					pullForce = explosion.effect.getMissilePull() * explosion.effectRatio * explosion.effectSize;
//					grabForce = explosion.effect.getMissileGrab() * explosion.effectRatio * explosion.effectSize;
//				} else if (explosion.damageType == DamageDealerType.PULSE) {
//					pushForce = explosion.effect.getPulsePush() * explosion.effectRatio * explosion.effectSize;
//					pullForce = explosion.effect.getPulsePull() * explosion.effectRatio * explosion.effectSize;
//					grabForce = explosion.effect.getPulseGrab() * explosion.effectRatio * explosion.effectSize;
//				}
//				if (pullForce > 0) {
//					Vector3f force = new Vector3f();
//					force.sub(explosion.toPos, explosion.fromPos);
//					if (force.lengthSquared() > 0) {
//						force.normalize();
//					} else {
//						force.set(0, 1, 0);
//					}
//					sc.getBlockEffectManager().addEffect(new PullEffect(sc, force, pullForce, false));
//				}
//				if (pushForce > 0) {
//					Vector3f force = new Vector3f();
//					force.sub(explosion.toPos, explosion.fromPos);
//					if (force.lengthSquared() > 0) {
//						force.normalize();
//					} else {
//						force.set(0, 1, 0);
//					}
//					Vector3f relPos = new Vector3f(explosion.centerOfExplosion.origin);
//					sc.getWorldTransformInverse().transform(relPos);
//					sc.getBlockEffectManager().addEffect(new PushEffect(sc, relPos, force, pushForce, false));
//				}
//				if (grabForce > 0) {
//					sc.getBlockEffectManager().addEffect(new StopEffect(sc, grabForce));
//				}
//			}

//			if (cb.armorHPMap.containsKey(sc.getId())) {
//				long armorHp = cb.armorHPMap.get(sc.getId());
//				long armorHpBef = cb.armorHPMapBef.get(sc.getId());
//
//				sc.getHpController().setArmorHp(sc.getHpController().getArmorHp() - Math.max(0, armorHpBef - armorHp));
//
//			}

			sc.getSegmentBuffer().restructBB();

			BlockBulkSerialization sm = new BlockBulkSerialization();

			Long2ObjectOpenHashMap<ByteArrayList> segmentMap = entry.getValue();

			sm.buffer = segmentMap;

			//send this update to all people in the area

			sc.sendBlockBulkMod(new RemoteBlockBulk(sm, true));

			//remove this instance from the map
			//the final clean up of the byteLists is done after the actual physical sending
			iterator.remove();
		}

		if (explosion.afterExplosionHook != null) {
			explosion.afterExplosionHook.onExplosionDone();
		}
	}

	private final ArmorCheckTraverseHandler pt = new ArmorCheckTraverseHandler();
	private final ArmorValue armorValue = new ArmorValue();
	private CubeRayCastResult rayCallbackTraverse = new CubeRayCastResult(new Vector3f(), new Vector3f(), null){

		@Override
		public InnerSegmentIterator newInnerSegmentIterator() {

			return pt;
		}

	};
	private ArmorValue retrieveArmorInfo(SegmentController c, Vector3f from, Vector3f to) {


		rayCallbackTraverse.closestHitFraction = 1f;
		rayCallbackTraverse.collisionObject = null;
		rayCallbackTraverse.setSegment(null);

		rayCallbackTraverse.rayFromWorld.set(from);
		rayCallbackTraverse.rayToWorld.set(to);

		rayCallbackTraverse.setFilter(c); //filter for performance since inital check already succeeded
		rayCallbackTraverse.setOwner(null);
		rayCallbackTraverse.setIgnoereNotPhysical(false);
		rayCallbackTraverse.setIgnoreDebris(false);
		rayCallbackTraverse.setRecordAllBlocks(false);
		rayCallbackTraverse.setZeroHpPhysical(false); //dont hit 0 hp blocks
		rayCallbackTraverse.setDamageTest(true);
		rayCallbackTraverse.setCheckStabilizerPaths(false); //hit stablizer paths
		rayCallbackTraverse.setSimpleRayTest(true);


		armorValue.reset();
		pt.armorValue = armorValue;
		((ModifiedDynamicsWorld) c.getPhysics().getDynamicsWorld()).rayTest(from, to, rayCallbackTraverse);

		if(!armorValue.typesHit.isEmpty()) {
			armorValue.calculate();
		}


		rayCallbackTraverse.collisionObject = null;
		rayCallbackTraverse.setSegment(null);
		rayCallbackTraverse.setFilter();

		return armorValue;

	}

	public int calcPreviousArmorDamageReduction(final float dmgOriginal) {
		float dmgOut = dmgOriginal;
		if(VoidElementManager.ARMOR_CALC_STYLE == ArmorDamageCalcStyle.EXPONENTIAL) {
			//  Damage Dealt = (Damage Incoming^3)/((Armour Value In Line of Shot)^3+Damage Incoming^2)

			dmgOut = Math.max(0,
					FastMath.pow(dmgOriginal, VoidElementManager.MISSILE_ARMOR_EXPONENTIAL_INCOMING_EXPONENT) /
							(FastMath.pow(armorValue.totalArmorValue, VoidElementManager.MISSILE_ARMOR_EXPONENTIAL_ARMOR_VALUE_TOTAL_EXPONENT) +
									FastMath.pow(dmgOriginal, VoidElementManager.MISSILE_ARMOR_EXPONENTIAL_INCOMING_DAMAGE_ADDED_EXPONENT)));
		}else {
			dmgOut = Math.max(0, dmgOut - (VoidElementManager.MISSILE_ARMOR_FLAT_DAMAGE_REDUCTION *  dmgOut));
			dmgOut = Math.max(0, dmgOut - Math.min(VoidElementManager.MISSILE_ARMOR_THICKNESS_DAMAGE_REDUCTION_MAX, ((VoidElementManager.BEAM_ARMOR_THICKNESS_DAMAGE_REDUCTION * (float)armorValue.typesHit.size())) *  dmgOut));
		}

		if(explosion.to instanceof ManagedSegmentController<?> msc) {
			final ArmorHPCollection c = msc.getManagerContainer().getArmorHP().getCollectionManager();
			dmgOut = c.processDamageToArmor(dmgOriginal, dmgOut);
		}
		return (int)dmgOut;
	}

//	public void powerDamage(float powerDamage, SegmentController c) {
//		((EditableSendableSegmentController) c).powerDamage(powerDamage, powerDamage > 0);
//	}

}
