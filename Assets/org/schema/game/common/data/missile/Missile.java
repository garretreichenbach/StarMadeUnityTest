package org.schema.game.common.data.missile;

import api.listener.events.weapon.MissileHitByProjectileEvent;
import api.listener.events.weapon.MissileHitEvent;
import api.listener.fastevents.FastListenerCommon;
import api.listener.fastevents.MissileUpdateListener;
import api.mod.StarLoader;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.linearmath.AabbUtil2;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.view.WorldDrawer;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.HitType;
import org.schema.game.common.controller.damage.Hittable;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.controller.damage.projectile.ProjectileController;
import org.schema.game.common.controller.elements.missile.dumb.DumbMissileElementManager;
import org.schema.game.common.data.SimpleGameObject;
import org.schema.game.common.data.explosion.ExplosionData;
import org.schema.game.common.data.explosion.ExplosionRunnable;
import org.schema.game.common.data.missile.updates.MissileDeadUpdate;
import org.schema.game.common.data.missile.updates.MissileProjectileHitUpdate;
import org.schema.game.common.data.missile.updates.MissileSpawnUpdate;
import org.schema.game.common.data.missile.updates.MissileSpawnUpdate.MissileType;
import org.schema.game.common.data.missile.updates.MissileUpdate;
import org.schema.game.common.data.physics.*;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.*;
import org.schema.game.network.objects.remote.RemoteMissileUpdate;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Transformable;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.objects.container.PhysicsDataContainer;
import org.schema.schine.network.objects.container.TransformTimed;
import org.schema.schine.network.server.ServerState;
import org.schema.schine.physics.Physical;
import org.schema.schine.physics.PhysicsState;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Missile implements Physical, Damager, SimpleGameObject {

	public static final float HIT_RADIUS = 5;

	private static final Transform serverTmp = new Transform();

	public static boolean OLD_EXPLOSION;

	public final Vector3f serverPos = new Vector3f();

	protected final ArrayList<MissileUpdate> pendingUpdates = new ArrayList<MissileUpdate>();

	protected final ArrayList<MissileUpdate> pendingBroadcastUpdates = new ArrayList<MissileUpdate>();

	protected final ArrayList<MissileUpdate> pendingClientUpdates = new ArrayList<MissileUpdate>();

	private final Vector3f direction = new Vector3f();

	private final TransformTimed worldTransform;

	private final StateInterface state;

	private final boolean onServer;

	private final Transform clientTransform = new Transform();

	private final ArrayList<PlayerState> nearPlayers = new ArrayList<PlayerState>();

	private final Vector3f nearVector = new Vector3f();

	private final Transform out = new Transform();

	private final Vector3f dist = new Vector3f();

	private final TransformaleObjectTmpVars v = new TransformaleObjectTmpVars();

	private final float[] param = new float[1];

	private final Vector3f hitNormal = new Vector3f();

	public boolean selfDamage;

	protected float distanceMade;

	private int hp;

	long nano;

	float res;

	Vector3f aabbMin = new Vector3f();

	Vector3f aabbMax = new Vector3f();

	private float speed;

	private float blastRadius = 1;

	private Damager owner;

	private short colorType;

	private SphereShape sphere;

	private PairCachingGhostObjectUncollidable ghostObject;

	// private PairCachingGhostObjectUncollidable ghostObjectBlast;
	private boolean alive = true;

	private int damageOnServer = 1;

	private SphereShape sphereBlast;

	private short id = -1231;

	private int sectorId;

	private PhysicsDataContainer physicsDataContainer;

	private Transform initialTransform = new Transform();

	private Transform lastWorldTransformTmp = new Transform();

	private Transform trans = new Transform();

	private Transform transTmp = new Transform();

	private Vector3f absSectorPos = new Vector3f();

	private int lastClientTransCalcSectorId = -1;

	private int hitId = -1;

	private boolean killedByProjectile;

	private Transform oldTransform = new Transform();

	private float distance;

	private Transform std = new Transform();

	private Vector3i otherSecAbs = new Vector3i();

	private Vector3i belogingVector = new Vector3i();

	private SphereShape hitSphere;

	public long spawnTime = -1;

	public int currentProjectileSector;

	private float lifetime;

	private long weaponId;

	private final DamageDealerType damageType = DamageDealerType.MISSILE;

	private Vector3f centerOfMass = new Vector3f();

	private short aabbCheckNum;

	private IntSet missileHitProjecileSet = new IntOpenHashSet();

	private float capacityConsumption = 1;

	private int maxHp;

	public Missile(StateInterface state) {
		this.state = state;
		this.onServer = state instanceof ServerState;
		physicsDataContainer = new PhysicsDataContainer();
		worldTransform = new TransformTimed();
		clientTransform.setIdentity();
		worldTransform.setIdentity();
		if (onServer) {
			spawnTime = state.getUpdateTime();
		}
	}

	@Override
	public InterEffectSet getAttackEffectSet(long weaponId, DamageDealerType damageDealerType) {
		if (owner != null) {
			InterEffectSet attackEffectSet = owner.getAttackEffectSet(weaponId, damageDealerType);
			assert (attackEffectSet != null) : owner;
			return attackEffectSet;
		} else {
			assert (false);
			return null;
		}
	}

	public MetaWeaponEffectInterface getMetaWeaponEffect(long weaponId, DamageDealerType damageDealerType) {
		if (owner != null) {
			return owner.getMetaWeaponEffect(weaponId, damageDealerType);
		}
		return null;
	}

	/**
	 * @param frameTime
	 * @param otherNode
	 * @return distanceMadeInThisUpdate
	 */
	public abstract float updateTransform(Timer timer, Transform worldTransform, Vector3f direction, Transform out, boolean prediction);

	/* (non-Javadoc)
	 * @see org.schema.schine.network.Identifiable#setId(int)
	 */
	@Override
	public boolean existsInState() {
		return ((GameServerState) state).getController().getMissileController().getMissileManager().getMissiles().containsKey(id);
	}

	/**
	 * @return the sectorId
	 */
	@Override
	public int getSectorId() {
		return sectorId;
	}

	@Override
	public void calcWorldTransformRelative(int fromSectorId, Vector3i sectorPos) {
		lastClientTransCalcSectorId = -1;
		if (this.sectorId == fromSectorId) {
			clientTransform.set(worldTransform);
		} else {
			boolean changed = false;
			if (lastClientTransCalcSectorId != fromSectorId) {
				changed = true;
				Vector3i relSystemPos = new Vector3i();
				Vector3i sectorThisObject;
				if (onServer) {
					Sector s = ((GameServerState) state).getUniverse().getSector(this.sectorId);
					if (s != null) {
						sectorThisObject = s.pos;
					} else {
						return;
					}
				} else {
					RemoteSector r = (RemoteSector) state.getLocalAndRemoteObjectContainer().getLocalObjects().get(this.sectorId);
					if (r == null) {
						// this could happen if the sector is deactivated
						// but the ship still exists for some reason
						System.err.println("Exception: Sector Not Found: " + this.sectorId + " for " + this + "; from sector: " + fromSectorId);
						clientTransform.set(worldTransform);
						clientTransform.origin.set(10000, 10000, 1000);
						return;
					}
					sectorThisObject = r.clientPos();
				}
				Vector3i sysPos = StellarSystem.getPosFromSector(sectorPos, new Vector3i());
				Vector3i relSectorPos = new Vector3i();
				relSectorPos.sub(sectorThisObject, sectorPos);
				absSectorPos.set(relSectorPos.x * ((GameStateInterface) state).getSectorSize(), relSectorPos.y * ((GameStateInterface) state).getSectorSize(), relSectorPos.z * ((GameStateInterface) state).getSectorSize());
				trans.setIdentity();
				// void
				trans.basis.setIdentity();
				trans.origin.set(absSectorPos);
			}
			if (lastClientTransCalcSectorId != fromSectorId || !lastWorldTransformTmp.equals(worldTransform)) {
				transTmp.set(trans);
				Matrix4fTools.transformMul(transTmp, worldTransform);
				clientTransform.set(worldTransform);
				clientTransform.origin.set(transTmp.origin);
				lastWorldTransformTmp.set(worldTransform);
			}
			if (changed) {
				lastClientTransCalcSectorId = fromSectorId;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.SimpleGameObject#getClientTransform()
	 */
	@Override
	public Transform getClientTransform() {
		return clientTransform;
	}

	@Override
	public Vector3f getCenterOfMass(Vector3f out) {
		out.set(centerOfMass);
		return out;
	}

	@Override
	public Transform getClientTransformCenterOfMass(Transform out) {
		return clientTransform;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.SimpleGameObject#getLinearVelocity()
	 */
	@Override
	public Vector3f getLinearVelocity(Vector3f out) {
		out.set(direction);
		// out.normalize();
		// out.scale(speed);
		return out;
	}

	@Override
	public SimpleTransformableSendableObject<?> getShootingEntity() {
		return owner.getShootingEntity();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.SimpleGameObject#isInPhysics()
	 */
	@Override
	public boolean isInPhysics() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.SimpleGameObject#isHidden()
	 */
	@Override
	public boolean isHidden() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.SimpleGameObject#getAsTargetId()
	 */
	@Override
	public int getAsTargetId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.SimpleGameObject#getTargetType()
	 */
	@Override
	public byte getTargetType() {
		return SimpleGameObject.MISSILE;
	}

	@Override
	public Transform getWorldTransformOnClient() {
		return clientTransform;
	}

	@Override
	public void transformAimingAt(Vector3f to, Damager from, SimpleGameObject target, Random random, float deviation) {
		to.set(0, 0, 0);
		to.add(getClientTransformCenterOfMass(serverTmp).origin);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.Damager#sendHitConfirm()
	 */
	@Override
	public void sendHitConfirm(byte damageType) {
		if (owner != null) {
			owner.sendHitConfirm(damageType);
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.Damager#isSegmentController()
	 */
	@Override
	public boolean isSegmentController() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.Damager#getFactionId()
	 */
	@Override
	public int getFactionId() {
		if (owner != null) {
			return owner.getFactionId();
		}
		return 0;
	}

	@Override
	public String getName() {
		return "Missile<" + owner.getName() + ">";
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.Damager#getPlayerState()
	 */
	@Override
	public AbstractOwnerState getOwnerState() {
		return owner.getOwnerState();
	}

	private Vector3i getSectorBelonging(Sector sectorCurrent) {
		Vector3i pos = sectorCurrent.pos;
		nearVector.set(worldTransform.origin);
		boolean isSelfStarSystem = StellarSystem.isStarSystem(sectorCurrent.pos);
		int nX = -10;
		int nY = -10;
		int nZ = -10;
		for (int z = -1; z < 2; z++) {
			for (int y = -1; y < 2; y++) {
				for (int x = -1; x < 2; x++) {
					std.setIdentity();
					otherSecAbs.set(pos);
					otherSecAbs.add(x, y, z);
					Sector otherSec = ((GameServerState) state).getUniverse().getSectorWithoutLoading(otherSecAbs);
					if (otherSec != null) {
						SimpleTransformableSendableObject.calcWorldTransformRelative(sectorId, pos, otherSec.getId(), std, state, true, out, v);
						dist.sub(worldTransform.origin, out.origin);
						// System.err.println(otherSec.pos+" LEN: "+dist.length()+"; "+nearVector.length()+"; "+(dist.lengthSquared() < nearVector.lengthSquared()));
						if (dist.lengthSquared() < nearVector.lengthSquared()) {
							nearVector.set(dist);
							nX = x;
							nY = y;
							nZ = z;
						// System.err.println(i+"SECTOR CHANGE ::::: "+Element.DIRECTIONSi[i]+": "+dist.length()+" / "+nearVector.length());
						}
					}
				}
			}
		}
		if (nX > -10) {
			belogingVector.set(pos);
			belogingVector.add(nX, nY, nZ);
			// int jump = SectorSwitch.TRANS_LOCAL;
			// state.getController().queueSectorSwitch(o, belogingVector, jump, false);
			// onPhysicsRemove(); //remove from physics
			// sectorId = newSector.getId();
			// onPhysicsAdd();
			return belogingVector;
		} else {
			return pos;
		}
	}

	// private void checkSectorBelonging(Vector3f inout) {
	// assert(state instanceof GameServerState);
	// Vector3f in = new Vector3f(inout);
	// Sector current =  ((GameServerState)state).getUniverse().getSector(sectorId);
	// if(current != null){
	// Vector3i pos = current.pos;
	// int nearest = -1;
	// Vector3f nearVector = new Vector3f(in);
	// Vector3i belongingVector = new Vector3i();
	// boolean isSelfStarSystem = StellarSystem.isStarSystem(current.pos);
	// for(int i = 0; i < Element.DIRECTIONSi.length; i++){
	// Vector3i test = new Vector3i(Element.DIRECTIONSi[i]);
	// test.add(pos);
	// Transform trans = new Transform();
	// trans.setIdentity();
	//
	// //				if(isSelfStarSystem){
	// //					Universe.calcSecPos(state, pos, test, state.getController().calculateStartTime(),System.currentTimeMillis(), trans);
	// //				}else{
	// trans.origin.set(Element.DIRECTIONSi[i].x, Element.DIRECTIONSi[i].y, Element.DIRECTIONSi[i].z);
	// trans.origin.scale(((GameStateInterface)getState()).getSectorSize());
	// //				}
	//
	// Vector3f dist = new Vector3f();
	// dist.sub(in, trans.origin);
	//
	//
	//
	// if(dist.lengthSquared() < nearVector.lengthSquared()){
	// nearVector.set(dist);
	// nearest = i;
	// //					System.err.println(i+"SECTOR CHANGE ::::: "+Element.DIRECTIONSi[i]+": "+dist.length()+" / "+nearVector.length());
	// }
	// }
	// if(nearest >= 0){
	// belongingVector.set(pos);
	// belongingVector.add(Element.DIRECTIONSi[nearest]);
	//
	// }else{
	// return; //no sector change
	// }
	//
	// try {
	// if(((GameServerState)state).getUniverse().isSectorLoaded(belongingVector)){
	// Sector newSector = ((GameServerState)state).getUniverse().getSector(belongingVector, false);
	// ProjectileController.translateSector(state, current, newSector, in, inout);
	// onPhysicsRemove(); //remove from physics
	// sectorId = newSector.getId();
	// onPhysicsAdd();
	//
	// //				System.err.println("[SERVER][PROJECTILE] Sector transition from "+current.pos+" to "+newSector.pos);
	// return;
	// }else{
	// //DO NOT LOAD SECTORS: projectile updates are happening in a sectors iterator, so adding sectors
	// //would fuck up the iterator
	// }
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// };
	//
	// }else{
	// System.err.println("[SERVER][PROJECTILE] Stopping projectile: out of loaded sector range");
	// }
	// //sector
	// setAlive(false);
	// return;
	// }
	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#cleanUpOnEntityDelete()
	 */
	public void cleanUpOnEntityDelete() {
		if (!onServer) {
			System.err.println("[CLIENT] Ending Trail for missile");
			GameClientState gs = (GameClientState) this.state;
			if (!state.isPassive()) {
				gs.getWorldDrawer().getTrailDrawer().endTrail(this);
				if (sectorId == gs.getCurrentSectorId()) {
					/*AudioController.fireAudioEvent("EXPLOSION", new AudioTag[] { AudioTags.GAME, AudioTags.SHIP, AudioTags.MISSILE, AudioTags.EXPLOSION }, AudioParam.ONE_TIME, AudioController.ent(new Transformable() {

    TransformTimed t = new TransformTimed(Missile.this.getWorldTransformOnClient());

    @Override
    public TransformTimed getWorldTransform() {
        return t;
    }
}, new Transform(Missile.this.getWorldTransformOnClient()), 0L, blastRadius))*/
					AudioController.fireAudioEventID(954, AudioController.ent(new Transformable() {

						TransformTimed t = new TransformTimed(Missile.this.getWorldTransformOnClient());

						@Override
						public TransformTimed getWorldTransform() {
							return t;
						}
					}, new Transform(Missile.this.clientTransform), 0L, blastRadius));
					((GameClientState) state).getWorldDrawer().getExplosionDrawer().addExplosion(this.worldTransform.origin, 10, weaponId);
				}
			}
		}
	}

	public void clearAllUpdates() {
		pendingUpdates.clear();
		pendingBroadcastUpdates.clear();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.physics.Physical#createConstraint(org.schema.schine.physics.Physical, org.schema.schine.physics.Physical, java.lang.Object)
	 */
	@Override
	public void createConstraint(Physical a, Physical b, Object userData) {
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.physics.Physical#getInitialTransform()
	 */
	@Override
	public Transform getInitialTransform() {
		return initialTransform;
	}

	/**
	 * /* (non-Javadoc)
	 *
	 * @see org.schema.schine.physics.Physical#getMass()
	 */
	@Override
	public float getMass() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.physics.Physical#getPhysicsDataContainer()
	 */
	@Override
	public PhysicsDataContainer getPhysicsDataContainer() {
		return physicsDataContainer;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.physics.Physical#setPhysicsDataContainer(org.schema.schine.network.objects.container.PhysicsDataContainer)
	 */
	@Override
	public void setPhysicsDataContainer(PhysicsDataContainer physicsDataContainer) {
		this.physicsDataContainer = physicsDataContainer;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.physics.Physical#getState()
	 */
	@Override
	public StateInterface getState() {
		return state;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.physics.Physical#getTransformedAABB(javax.vecmath.Vector3f, javax.vecmath.Vector3f, float, javax.vecmath.Vector3f, javax.vecmath.Vector3f)
	 */
	@Override
	public void getTransformedAABB(Vector3f oMin, Vector3f oMax, float margin, Vector3f tmpMin, Vector3f tmpMax, Transform instead) {
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.physics.Physical#initPhysics()
	 */
	@Override
	public void initPhysics() {
		ghostObject = new PairCachingGhostObjectUncollidable(CollisionType.MISSILE, this.physicsDataContainer);
		ghostObject.setWorldTransform(initialTransform);
		oldTransform.set(initialTransform);
		// ghostObjectBlast = new PairCachingGhostObjectUncollidable(this.getPhysicsDataContainer());
		//
		// ghostObjectBlast.setWorldTransform(getInitialTransform());
		// sphere = new BoxShape(new Vector3f(characterWidth, characterHeight, characterWidth));
		sphereBlast = new SphereShape(blastRadius);
		sphere = new SphereShape(0.5f);
		// BoxShape sphere = new BoxShape(new Vector3f(characterWidth, characterHeight, characterWidth));
		//
		sphere.setMargin(0.1f);
		ghostObject.setCollisionShape(sphere);
		// ghostObjectBlast.setCollisionShape(sphereBlast);
		// ghostObject.setUserPointer(this.getId());
		ghostObject.setUserPointer(null);
		hitSphere = new SphereShape(HIT_RADIUS);
		hitSphere.getAabb(initialTransform, aabbMin, aabbMax);
		physicsDataContainer.setObject(ghostObject);
		physicsDataContainer.setShape(sphere);
		physicsDataContainer.updatePhysical(state.getUpdateTime());
		ghostObject.setCollisionFlags(ghostObject.getCollisionFlags() | CollisionFlags.NO_CONTACT_RESPONSE);
		if (onServer) {
			currentProjectileSector = sectorId;
			addToSectorPhysicsForProjectileCheck(currentProjectileSector);
		}
	}

	public void endTrail() {
		GameClientState gs = (GameClientState) this.state;
		if (gs.getWorldDrawer() != null && gs.getWorldDrawer().getTrailDrawer() != null) {
			gs.getWorldDrawer().getTrailDrawer().endTrail(this);
		// System.err.println("[CLIENT] Removing Trail for missile");
		}
	}

	/**
	 * @return the blastRadius
	 */
	public float getBlastRadius() {
		return blastRadius;
	}

	public int getDamage() {
		return damageOnServer;
	}


	public void setDamage(int damage) {
		this.damageOnServer = damage;
		
		blastRadius = FastMath.pow(((3f * FastMath.QUARTER_PI) * (damage / ServerConfig.MISSILE_RADIUS_HP_BASE.getFloat())), 0.33333333f) ;


	}

	/**
	 * @return the direction
	 */
	public Vector3f getDirection(Vector3f dir) {
		dir.set(direction);
		return dir;
	}

	/**
	 * @return the id
	 */
	public short getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(short id) {
		this.id = id;
	}

	/**
	 * @return the owner
	 */
	public Damager getOwner() {
		return owner;
	}

	/**
	 * @param owner the owner to set
	 */
	public void setOwner(Damager owner) {
		this.owner = owner;
	}

	public Sector getSector(int id) {
		assert (onServer);
		Sector sector = ((GameServerState) state).getUniverse().getSector(id);
		return sector;
	}

	/**
	 * @return the speed
	 */
	public float getSpeed() {
		return speed;
	}

	/**
	 * @param speed the speed to set
	 */
	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public abstract MissileType getType();

	/**
	 * @return the worldTransform
	 */
	@Override
	public TransformTimed getWorldTransform() {
		return worldTransform;
	}

	public boolean hasPendingBroadcastUpdates() {
		return !pendingBroadcastUpdates.isEmpty();
	}

	public boolean hasPendingUpdates() {
		return !pendingUpdates.isEmpty();
	}

	public boolean isAlive() {
		return alive;
	}

	/**
	 * @param alive the alive to set
	 */
	public void setAlive(boolean alive) {
		// try{
		// throw new NullPointerException(this.alive +" -> "+alive +"; "+(isOnServer() ? "SERVER":"CLIENT"));
		// }catch (Exception e) {
		// e.printStackTrace();
		// }
		this.alive = alive;
	}

	public boolean isOnServer() {
		return onServer;
	}

	public List<PlayerState> nearPlayers() {
		return nearPlayers;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#onPhysicsAdd()
	 */
	public void addToSectorPhysicsForProjectileCheck(int sec) {
		ghostObject.setCollisionFlags(CollisionFlags.NO_CONTACT_RESPONSE);
		Sector sector = getSector(sec);
		if (sector != null) {
			sector.addMissile(id);
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#onPhysicsRemove()
	 */
	public void removeFromSectorPhysicsForProjectileCheck(int sec) {
		Sector sector = getSector(sec);
		if (sector != null) {
			sector.removeMissile(id);
		}
	}

	public abstract void onSpawn();

	public void sendPendingBroadcastUpdates(ClientChannel cc) {
		for (int i = 0; i < pendingBroadcastUpdates.size(); i++) {
			MissileUpdate missileUpdate = pendingBroadcastUpdates.get(i);
			cc.getNetworkObject().missileUpdateBuffer.add(new RemoteMissileUpdate(missileUpdate, onServer));
		// System.err.println("[SERVER] sent missile update BB "+missileUpdate);
		}
	}

	public void sendPendingUpdates(ClientChannel cc) {
		for (int i = 0; i < pendingUpdates.size(); i++) {
			MissileUpdate missileUpdate = pendingUpdates.get(i);
			cc.getNetworkObject().missileUpdateBuffer.add(new RemoteMissileUpdate(missileUpdate, onServer));
		}
	}

	/**
	 * @param direction the direction to set
	 */
	public void setDirection(Vector3f direction) {
		assert (!Float.isNaN(direction.x) && !Float.isNaN(direction.y) && !Float.isNaN(direction.z)) : direction + " became NaN";
		this.direction.set(direction);
	}

	public void setFromSpawnUpdate(MissileSpawnUpdate missileSpawnUpdate) {
		worldTransform.setIdentity();
		this.id = missileSpawnUpdate.id;
		this.sectorId = missileSpawnUpdate.sectorId;
		assert (missileSpawnUpdate.missileType == getType()) : missileSpawnUpdate.missileType + " --- " + getType();
		setDirection(missileSpawnUpdate.dir);
		worldTransform.origin.set(missileSpawnUpdate.position);
		this.weaponId = missileSpawnUpdate.weaponId;
		this.speed = missileSpawnUpdate.speed;
		this.colorType = missileSpawnUpdate.colorType;
		this.spawnTime = missileSpawnUpdate.spawnTime;
		if (this instanceof ActivationMissileInterface) {
			((ActivationMissileInterface) this).setActivationTimer(missileSpawnUpdate.bombActivationTime);
		}
	}

	/**
	 * @param sectorId the sectorId to set
	 */
	public void setSectorId(int sectorId, boolean sendUpdate) {
		this.sectorId = sectorId;
		if (!sendUpdate) {
		}
	}

	protected void setTransformMissile(Transform t) {
		this.oldTransform.set(worldTransform);
		worldTransform.set(t);
		if (onServer) {
			ghostObject.setWorldTransform(t);
		}
	}

	public void startTrail() {
		GameClientState gs = (GameClientState) this.state;
		if (gs.getController().isNeighborToClientSector(sectorId)) {
			if (gs.getWorldDrawer() != null && gs.getWorldDrawer().getTrailDrawer() != null) {
				// System.err.println("[CLIENT] Adding Trail for missile "+clientTransform.origin);
				gs.getWorldDrawer().getTrailDrawer().startTrail(this);
			} else {
				System.err.println("[CLIENT] Cannot add Trail for missile (drawer not initialized)");
			}
		}
	}

	public void testCollision(Sector originSector, Sector sector) {
		assert (onServer);
		if (owner == null) {
			System.err.println("[MISSILE] Exception: OWNER IS NULL");
		}
		Transform lastTransform = new Transform(this.oldTransform);
		Transform worldTransform = new Transform(this.worldTransform);
		if (originSector != sector) {
			Vector3f in = new Vector3f(worldTransform.origin);
			Vector3f out = new Vector3f();
			ProjectileController.translateSector(state, originSector, sector, in, out);
			worldTransform.origin.set(out);
			in.set(lastTransform.origin);
			ProjectileController.translateSector(state, originSector, sector, in, out);
			lastTransform.origin.set(out);
			if (currentProjectileSector != sector.getId()) {
				removeFromSectorPhysicsForProjectileCheck(currentProjectileSector);
				currentProjectileSector = sector.getId();
				addToSectorPhysicsForProjectileCheck(currentProjectileSector);
			}
		}
		// hitsphere is in respect to the sector the missile is in since it is being used for projectile calc
		hitSphere.getAabb(worldTransform, aabbMin, aabbMax);
		PhysicsState ps = sector;
		PhysicsExt physics = (PhysicsExt) ps.getPhysics();
		Vector3f dir = new Vector3f();
		dir.sub(worldTransform.origin, lastTransform.origin);
		if (worldTransform.basis.determinant() != 0 && dir.lengthSquared() > 0) {
			assert (physics != null);
			assert (worldTransform != null);
			assert (ghostObject != null);
			Object owner = null;
			if (!canHitSelf()) {
				owner = this.owner;
			}
			CubeRayCastResult rayCallback = new CubeRayCastResult(lastTransform.origin, worldTransform.origin, owner);
			rayCallback.setDamageTest(true);
			ClosestRayResultCallback testRayCollisionPoint = physics.testRayCollisionPoint(lastTransform.origin, worldTransform.origin, rayCallback, false);
			if (rayCallback.hasHit()) {
				Transform hitPos = new Transform(worldTransform);
				hitPos.origin.set(rayCallback.hitPointWorld);
				Vector3f aabbMin = new Vector3f();
				Vector3f aabbMax = new Vector3f();
				Vector3f aabbMinO = new Vector3f();
				Vector3f aabbMaxO = new Vector3f();
				sphereBlast.getAabb(hitPos, aabbMin, aabbMax);
				ObjectArrayList<CollisionObject> collisionObjectArray = physics.getDynamicsWorld().getCollisionObjectArray();
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
				if (rayCallback.hasHit()) {
					ExplosionData e = new ExplosionData();
					e.damageType = getDamageType();
					e.centerOfExplosion = new Transform(hitPos);
					e.fromPos = new Vector3f(lastTransform.origin);
					e.toPos = new Vector3f(worldTransform.origin);
					Vector3f toOld = new Vector3f();
					toOld.sub(e.fromPos, e.toPos);
					toOld.normalize();
					toOld.scale(0.48f);
					// pulling back center of eplosion a bit
					e.centerOfExplosion.origin.add(toOld);
					e.ignoreShields = isIgnoreShields();
					e.ignoreShieldsSelf = isIgnoreShieldsSelf();
					e.radius = blastRadius;
					e.damageInitial = damageOnServer;
					e.damageBeforeShields = 0;
					e.originSectorId = originSector.getId();
					e.sectorId = sector.getId();
					e.hitsFromSelf = selfDamage;
					e.from = this.owner;
					e.weaponId = weaponId;
                    e.to = rayCallback.getSegment().getSegmentController();//added from rel
					e.hitType = HitType.WEAPON;
					e.attackEffectSet = getAttackEffectSet(weaponId, e.damageType);
					if (this.owner != null && this.owner instanceof SegmentController) {
						((SendableSegmentController) this.owner).sendExplosionGraphic(e.centerOfExplosion.origin);
					}
					if (!isDud()) {
						ExplosionRunnable n = new ExplosionRunnable(e, sector);
						//INSERTED CODE
						MissileHitEvent event = new MissileHitEvent(this, rayCallback, e);
						// Though isOnServer exists for missiles, explosions are server-side
						StarLoader.fireEvent(event, true);
						if(event.isCanceled()){
							return;
						}
						///
						((GameServerState) state).enqueueExplosion(n);
					} else {
						onDud();
					}
					alive = false;
				}
			}
		} else {
			if (dir.lengthSquared() == 0) {
			// System.err.println("[MISSILE] not checking hit as before and after are the same position: "+worldTransform.origin+" "+this);
			} else {
				try {
					throw new IllegalStateException("[MISSILE] WORLD TRANSFORM (DIR: " + dir + "; len " + dir.length() + ") IS STRANGE OR PHYSICS NOT INITIALIZED \n" + worldTransform.basis + ";\n" + this);
				} catch (Exception e) {
					e.printStackTrace();
				}
				alive = false;
			}
		}
	}

	protected DamageDealerType getDamageType() {
		return DamageDealerType.MISSILE;
	}

	public void onDud() {
	}

	protected boolean isIgnoreShieldsSelf() {
		return false;
	}

	protected boolean isIgnoreShields() {
		return false;
	}

	protected boolean canHitSelf() {
		return false;
	}

	protected boolean isDud() {
		return false;
	}

	@Override
	public String toString() {
		return "Missile(" + id + " s[" + sectorId + "] OWN: " + owner + ")";
	}

	public void translateTrail() {
		GameClientState gs = (GameClientState) this.state;
		// System.err.println("[CLIENT][MISSILE] translating trail");
		if (gs.getWorldDrawer() != null && gs.getWorldDrawer().getTrailDrawer() != null) {
			calcWorldTransformRelative(gs.getCurrentSectorId(), gs.getPlayer().getCurrentSector());
			gs.getWorldDrawer().getTrailDrawer().translateTrail(this);
		// System.err.println("[CLIENT] Removing Trail for missile");
		}
	}

	public abstract void updateClient(Timer timer);

	public void updateServer(Timer timer) {
		//INSERTED CODE
		if(!FastListenerCommon.missileUpdateListeners.isEmpty()) {
			for (MissileUpdateListener listener : FastListenerCommon.missileUpdateListeners) {
				listener.updateServer(this, timer);
			}
		}
		///
		addLifetime(timer);
		Sector sector = ((GameServerState) state).getUniverse().getSector(sectorId);
		if (sector != null) {
			if (alive) {
				try {
					try {
						Vector3i sectorBelonging = getSectorBelonging(sector);
						Sector newSector = ((GameServerState) state).getUniverse().getSectorWithoutLoading(sectorBelonging);
						if (newSector != null) {
							testCollision(sector, newSector);
						}
					// ghostObjectBlast.setWorldTransform(getWorldTransform());
					} catch (SectorNotFoundRuntimeException e) {
						System.err.println("Exception catched: sector not found. Ending missile " + id + ". (Ignore fatal exception output)");
						e.printStackTrace();
						alive = false;
					}
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}
			// getSectorBelonging(sector);
			}
		} else {
			System.err.println("[MISSILE] WARNING: Missile sector is no longer loaded: " + sectorId + "; ending missile!");
			alive = false;
		}
		if (alive && distanceMade > distance) {
			alive = false;
			System.err.println("[SERVER] MISSILE DIED FROM LIFETIME DISTANCE");
		}
		if (alive && getMissileTimeoutMs() > 0 && state.getUpdateTime() - spawnTime > getMissileTimeoutMs()) {
			alive = false;
			System.err.println("[SERVER] MISSILE DIED FROM LIFETIME TIMEOUT (" + getMissileTimeoutMs() + ")");
		}
		if (killedByProjectile) {
			alive = false;
		}
		if (!alive) {
			// System.err.println("[SERVER] Deleting missile "+this+"; "+getId()+"; HIT: "+hitId);
			MissileDeadUpdate missileDeadUpdate = new MissileDeadUpdate(id);
			onDeadServer();
			missileDeadUpdate.setHitId(hitId);
			pendingBroadcastUpdates.add(missileDeadUpdate);
		} else {
		// if (sectorId != lastSectorId) {
		// MissileSectorChangeUpdate pUpdate = new MissileSectorChangeUpdate(getId());
		// pUpdate.sectorId = sectorId;
		// pendingUpdates.add(pUpdate);
		// lastSectorId = sectorId;
		//
		// }
		// else {
		// if (this instanceof TargetChasingMissile) {
		// if (System.currentTimeMillis() - lastDirUpdate > 500) {
		// MissileDirectionAndPosUpdate pUpdate = new MissileDirectionAndPosUpdate(getId());
		// pUpdate.position.set(getWorldTransform().origin);
		// getDirection(pUpdate.direction);
		// pendingUpdates.add(pUpdate);
		// lastDirUpdate = System.currentTimeMillis() + Universe.getRandom().nextInt(30);
		// }
		// } else {
		// if (System.currentTimeMillis() - lastPosUpdate > 500) {
		// MissilePositionUpdate pUpdate = new MissilePositionUpdate(getId());
		// pUpdate.position.set(getWorldTransform().origin);
		// pendingUpdates.add(pUpdate);
		// lastPosUpdate = System.currentTimeMillis() + Universe.getRandom().nextInt(30);
		// }
		// }
		// }
		}
	}

	protected long getMissileTimeoutMs() {
		return -1;
	}

	protected void onDeadServer() {
	}

	/**
	 * @return the distance
	 */
	public float getDistance() {
		return distance;
	}

	/**
	 * @param distance the distance to set
	 */
	public void setDistance(float distance) {
		this.distance = distance;
	}

	/**
	 * @return the colorType
	 */
	public short getColorType() {
		return colorType;
	}

	/**
	 * @param colorType the colorType to set
	 */
	public void setColorType(short colorType) {
		this.colorType = colorType;
	}

	/**
	 * @return the oldTransform
	 */
	public Transform getOldTransform() {
		return oldTransform;
	}

	public boolean hasHit(Vector3f posBeforeUpdate, Vector3f posAfterUpdate) {
		param[0] = 1f;
		hitNormal.set(0, 0, 0);
		// if(aabbCheckNum != getState().getUpdateNumber()) {
		// hitSphere.getAabb(getWorldTransform(), aabbMin, aabbMax);
		// aabbCheckNum = getState().getUpdateNumber();
		// }
		final boolean hit = AabbUtil2.rayAabb(posBeforeUpdate, posAfterUpdate, aabbMin, aabbMax, param, hitNormal);
		// System.err.println("AABBMIN "+aabbMin+"; "+aabbMax+" -> "+hit);
		return hit;
	}

	/**
	 * @return the killedByProjectile
	 */
	public boolean isKilledByProjectile() {
		return killedByProjectile;
	}

	/**
	 * @param killedByProjectile the killedByProjectile to set
	 */
	public void setKilledByProjectile(boolean killedByProjectile) {
		this.killedByProjectile = killedByProjectile;
	}

	public Transform getWorldTransformRelativeToSector(int fromSector, Transform out) {
		Sector sector = ((GameServerState) state).getUniverse().getSector(fromSector);
		if (sector != null) {
			SimpleTransformableSendableObject.calcWorldTransformRelative(fromSector, sector.pos, sectorId, worldTransform, state, true, out, v);
			return out;
		}
		return null;
	}

	public int getHp() {
		return hp;
	}

	public void setupHp(float damage) {
		float hp;
		switch(DumbMissileElementManager.MISSILE_HP_CALC_STYLE) {
			case LINEAR -> hp = DumbMissileElementManager.MISSILE_HP_MIN + damage * DumbMissileElementManager.MISSILE_HP_PER_DAMAGE;
			case EXP -> hp = DumbMissileElementManager.MISSILE_HP_MIN + Math.max(0, (float) Math.pow(damage, DumbMissileElementManager.MISSILE_HP_EXP) * DumbMissileElementManager.MISSILE_HP_EXP_MULT);
			case LOG -> hp = DumbMissileElementManager.MISSILE_HP_MIN + Math.max(0, ((float) Math.log10(damage) + DumbMissileElementManager.MISSILE_HP_LOG_OFFSET) * DumbMissileElementManager.MISSILE_HP_LOG_FACTOR);
			default -> {
				this.hp = 1;
				throw new RuntimeException("Illegal calc style " + DumbMissileElementManager.MISSILE_HP_CALC_STYLE);
			}
		}
		this.maxHp = (int) FastMath.round(hp);
		this.hp = FastMath.round(hp);
	}

	public void hitByProjectile(int projectileId, float projectileDamage) {
		missileHitProjecileSet.add(projectileId);
		final int hpBef = hp;
		hp = (int) Math.max(0, hp - projectileDamage);
		// System.err.println("MISSILE HP: "+hpBef+" --"+projectileDamage+"-->"+hp+"");
		if (hp <= 0) {
			// kill missiles
			killedByProjectile = true;
		}

		//INSERTED CODE
		MissileHitByProjectileEvent event = new MissileHitByProjectileEvent(this, projectileId, projectileDamage);
		StarLoader.fireEvent(event, onServer);
		///

		MissileProjectileHitUpdate mu = new MissileProjectileHitUpdate(id);
		mu.percent = maxHp > 0 ? (float) hp / (float) maxHp : 0;
		pendingBroadcastUpdates.add(mu);
	}

	//INSERTED CODE
	public ArrayList<MissileUpdate> _getPendingUpdates() {
		return pendingUpdates;
	}
	///


	@Override
	public void sendClientMessage(String str, byte type) {
		if (owner != null) {
			owner.sendClientMessage(str, type);
		}
	}

	@Override
	public void sendServerMessage(Object[] astr, byte msgType) {
		if (owner != null) {
			owner.sendServerMessage(astr, msgType);
		}
	}

	@Override
	public float getDamageGivenMultiplier() {
		if (owner != null) {
			return owner.getDamageGivenMultiplier();
		}
		return 1;
	}

	public void addLifetime(Timer timer) {
		this.lifetime += timer.getDelta();
	}

	public float getLifetime() {
		return lifetime;
	}

	public long getWeaponId() {
		return weaponId;
	}

	public void setWeaponId(long weaponId) {
		this.weaponId = weaponId;
	}

	@Override
	public long getOwnerId() {
		if (owner.getOwnerState() != null) {
			return owner.getOwnerState().getDbId();
		}
		return Long.MIN_VALUE;
	}

	public void onClientDie(int hitId) {
		endTrail();
		Sendable s = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(hitId);
		if (s != null && s instanceof Hittable) {
			System.err.println("[CLIENT] missile dead update. HIT " + s);
		}
	}

	public boolean hadHit(int projectileIdFilter) {
		return missileHitProjecileSet.contains(projectileIdFilter);
	}

	public void onClientProjectileHit(float percent) {
		WorldDrawer worldDrawer = ((GameClientState) state).getWorldDrawer();
		if (worldDrawer != null) {
			worldDrawer.getExplosionDrawer().addShieldBubbleHit(clientTransform.origin, percent);
		}
	}

	public boolean isInNeighborSecorToServer(Vector3i sec) {
		assert (onServer);
		Sector sector = ((GameServerState) state).getUniverse().getSector(sectorId);
		return sector != null && Sector.isNeighbor(sec, sector.pos);
	}

	public float getCapacityConsumption() {
		return capacityConsumption;
	}

	public void setCapacityConsumption(float capacityConsumption) {
		this.capacityConsumption = capacityConsumption;
	}
}
