package org.schema.game.common.controller.elements;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.data.explosion.ExplosionData;
import org.schema.game.common.data.explosion.ExplosionRunnable;
import org.schema.game.common.data.physics.ChangableSphereShape;
import org.schema.game.common.data.physics.CollisionType;
import org.schema.game.common.data.physics.PairCachingGhostObjectUncollidable;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SectorNotFoundRuntimeException;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.network.Identifiable;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.objects.container.PhysicsDataContainer;
import org.schema.schine.network.server.ServerStateInterface;
import org.schema.schine.physics.Physical;
import org.schema.schine.physics.Physics;
import org.schema.schine.physics.PhysicsState;

import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;

public class Pulse implements Physical {

	public static final byte TYPE_DAMAGE = 0;
	public static final byte TYPE_PUSH = 1;
	private final StateInterface state;
	private final Transform t = new Transform();
	private long weaponId = Long.MIN_VALUE;
	private final byte pulseType;
	private final Vector3f dir;
	private Transform location = new Transform();
	private Transform localLocation;
	private Damager owner;
	private float totalRadius;
	private float currentRadius = 1;
	private boolean active;
	private boolean onServer;
	private int sectorId;
	private PairCachingGhostObjectUncollidable ghostObjectBlast;
	private PhysicsDataContainer physicsDataContainer;
	private ChangableSphereShape sphereBlast;
	private ObjectArrayList<Sendable> hitBuffer = new ObjectArrayList<Sendable>();
	private float force;
	private Vector4f pulseColor;
	private SimpleTransformableSendableObject boundTo;
	public Pulse(StateInterface state, byte pulseType, Transform absolute, SimpleTransformableSendableObject boundTo, Vector3f dir, Damager owner, float force,
	             float totalRadius, int sectorId, long weaponId, Vector4f pulseColor) {

		this.dir = new Vector3f(dir);

		this.state = state;
		this.onServer = state instanceof ServerStateInterface;
		this.boundTo = boundTo;

		this.localLocation = new Transform(absolute);

//		this.location.basis.transform(this.dir);
		this.dir.normalize();
		this.dir.scale(totalRadius);
		this.localLocation.origin.add(this.dir);

		location.set(localLocation);

		Transform inv;
		if (!onServer) {
			inv = new Transform(boundTo.getWorldTransformOnClient());
		} else {
			inv = new Transform(boundTo.getWorldTransform());
//			System.err.println("PUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU");

		}
		inv.inverse();

		inv.mul(this.localLocation);

		
		
		
		this.localLocation.set(inv);

		this.owner = owner;
		this.force = force;
		
		
//		if (effectType != 0 && owner != null && owner instanceof EditableSendableSegmentController) {
//			EffectElementManager<?, ?, ?> effect = ((EditableSendableSegmentController)owner).getEffect(owner, effectType);
//			if (effect != null) {
//				totalRadius += Math.max(0, effectRatio * effect.getPulseExplosiveRadius());
//			}
//		}
		
		this.totalRadius = totalRadius;

		this.sectorId = sectorId;
		this.weaponId = weaponId;
		this.pulseType = pulseType;
		this.pulseColor = pulseColor;
		physicsDataContainer = new PhysicsDataContainer();
		setActive(true);

	}

	@Override
	public void createConstraint(Physical a, Physical b, Object userData) {
		assert (false);
	}

	@Override
	public Transform getInitialTransform() {
		return location;
	}

	@Override
	public float getMass() {
		return 0;
	}

	@Override
	public PhysicsDataContainer getPhysicsDataContainer() {
		return physicsDataContainer;
	}

	@Override
	public void setPhysicsDataContainer(
			PhysicsDataContainer physicsDataContainer) {
		this.physicsDataContainer = physicsDataContainer;
	}

	/**
	 * @return the state
	 */
	@Override
	public StateInterface getState() {
		return state;
	}

	@Override
	public void getTransformedAABB(Vector3f oMin, Vector3f oMax, float margin,
	                               Vector3f tmpMin, Vector3f tmpMax, Transform instead) {
		assert (false);
	}

	@Override
	public void initPhysics() {

		ghostObjectBlast = new PairCachingGhostObjectUncollidable(CollisionType.PULSE, this.physicsDataContainer);

		ghostObjectBlast.setWorldTransform(location);

		//			sphere = new BoxShape(new Vector3f(characterWidth, characterHeight, characterWidth));
		sphereBlast = new ChangableSphereShape(currentRadius);
		//			BoxShape sphere = new BoxShape(new Vector3f(characterWidth, characterHeight, characterWidth));
		//
		ghostObjectBlast.setCollisionShape(sphereBlast);

		ghostObjectBlast.setUserPointer(((Identifiable) owner).getId());

		physicsDataContainer.setObject(ghostObjectBlast);
		physicsDataContainer.setShape(sphereBlast);

		physicsDataContainer.updatePhysical(state.getUpdateTime());

	}

	public void draw(Mesh mesh) {

		long t = System.currentTimeMillis();
		GlUtil.glPushMatrix();

		GlUtil.glMultMatrix(location);

		float r = currentRadius / 256f;
		float percent = currentRadius / totalRadius;
		float m = (1.0f - Math.min(1f, 1.8f - percent)) * 5f;
		GlUtil.updateShaderFloat(ShaderLibrary.pulseShader, "m_Alpha", 1.0f - m);
		GlUtil.updateShaderVector4f(ShaderLibrary.pulseShader, "m_Color", pulseColor);

		GlUtil.scaleModelview(r, r, r);

		mesh.renderVBO();

		GlUtil.glPopMatrix();

	}

	/**
	 * @return the force
	 */
	public float getForce() {
		return force;
	}

	/**
	 * @param force the force to set
	 */
	public void setForce(float force) {
		this.force = force;
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

	private Physics getPhysics() {
		return getPhysicsState().getPhysics();
	}

	public PhysicsState getPhysicsState() throws SectorNotFoundRuntimeException {
		if (onServer) {
			long t = System.currentTimeMillis();
			Sector sector = ((GameServerState) state).getUniverse().getSector(sectorId);

			long took = System.currentTimeMillis() - t;

			if (took > 5) {
				System.err.println("[SERVER][STO] WARNING: Loading sector " + sectorId + " for " + this + " took " + took + " ms");
			}
			//			assert(sector != null):"Server Sector NULL: "+this+"; Sector that is not found: "+getSectorId();
			if (sector == null) {
				//				System.err.println("[ERROR][FATAL] Fatal Exception: SECTOR NULL FOR "+this+" "+getSectorId());
				throw new SectorNotFoundRuntimeException(sectorId);
			}
			return sector;
		} else {
			return ((GameClientState) state);
		}
	}

	/**
	 * @return the sectorId
	 */
	public int getSectorId() {
		return sectorId;
	}

	/**
	 * @param sectorId the sectorId to set
	 */
	public void setSectorId(int sectorId) {
		this.sectorId = sectorId;
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 * @
	 */
	public void setActive(boolean active) {
		if (!active && this.active) {
			getPhysics().removeObject(ghostObjectBlast);
		} else if (active && !this.active) {

			initPhysics();
			ghostObjectBlast.setCollisionFlags(CollisionFlags.NO_CONTACT_RESPONSE);
			short filter = CollisionFilterGroups.ALL_FILTER - CollisionFilterGroups.DEBRIS_FILTER;
			getPhysics().addObject(ghostObjectBlast, filter, CollisionFilterGroups.SENSOR_TRIGGER);
			ghostObjectBlast.setCollisionFlags(CollisionFlags.NO_CONTACT_RESPONSE);
		}
		this.active = active;
	}

	public boolean isOnServer() {
		return onServer;
	}

	

	public void update(Timer timer) {
		try {

			Transform wt;
			if (!onServer) {
				wt = new Transform(boundTo.getWorldTransformOnClient());
			} else {
				wt = new Transform(boundTo.getWorldTransform());
			}

			location.set(wt);
			location.mul(localLocation);

			ghostObjectBlast.setWorldTransform(location);
			physicsDataContainer.updatePhysical(state.getUpdateTime());

			float delta = timer.getDelta();

			currentRadius += totalRadius * delta;
			if (currentRadius > totalRadius) {

				if (onServer) {
					ExplosionData e = new ExplosionData();
					e.damageType = DamageDealerType.MISSILE;
					e.centerOfExplosion = new Transform(getWorldTransform());

					e.fromPos = new Vector3f(getWorldTransform().origin);
					e.toPos = new Vector3f(getWorldTransform().origin);

					e.radius = currentRadius;
					e.damageInitial = force;
					e.damageBeforeShields = 0;
					e.sectorId = sectorId;
					e.hitsFromSelf = false;
					e.from = owner;
					e.weaponId = weaponId;


					Sector sector = ((GameServerState) state).getUniverse().getSector(sectorId);

					if (sector != null) {

						ExplosionRunnable n = new ExplosionRunnable(e, sector);

						((GameServerState) state).enqueueExplosion(n);
					}
				}

				setActive(false);
			} else {
				sphereBlast.setRadius(currentRadius);
			}
		} catch (SectorNotFoundRuntimeException e) {
			System.err.println(state + " ERROR But Recovered: SectorNotFoundException on Pulse Update");
			active = false;
		}
	}

	/**
	 * @return the pulseType
	 */
	public byte getPulseType() {
		return pulseType;
	}


	/**
	 * @return the distance
	 */
	public float getDistance() {
		return totalRadius;
	}

	/**
	 * @param distance the distance to set
	 */
	public void setDistance(float distance) {
		this.totalRadius = distance;
	}

	public Transform getWorldTransform() {
		return physicsDataContainer.getCurrentPhysicsTransform();
	}

	/**
	 * @return the pulseColor
	 */
	public Vector4f getPulseColor() {
		return pulseColor;
	}

	/**
	 * @param pulseColor the pulseColor to set
	 */
	public void setPulseColor(Vector4f pulseColor) {
		this.pulseColor = pulseColor;
	}

}
