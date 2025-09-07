package org.schema.schine.network.objects.container;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Matrix4fTools;

import com.bulletphysics.collision.broadphase.BroadphaseNativeType;
import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.CompoundShapeChild;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;

public class PhysicsDataContainer {

	//	/** The physics vehicle map. */
	//	@Deprecated
	//	public HashMap<RaycastVehicle , ArrayList<AbstractSceneNode>> physicsVehicleMap = new HashMap<RaycastVehicle , ArrayList<AbstractSceneNode>>();

	private static final Transform ident = new Transform();

	static {
		ident.setIdentity();
	}

	public final Transform originalTransform = new Transform();
	public final Transform lastTransform = new Transform();
	public final Transform thisTransform = new Transform();
	public final Vector3f lastCenter = new Vector3f();
	private final TransformExt cacheTransform = new TransformExt();
	private final Transform tmpTransform = new Transform();
	private final Transform tmp = new Transform();
	private final Vector3f tmpLC = new Vector3f();
	public UpdateWithoutPhysicsObjectInterface onPhysicsObjectUpdateEnergyBeamInterface;
	public UpdateWithoutPhysicsObjectInterface oldDockingUpdateWithoutPhysicsObjectInterface;
	public UpdateWithoutPhysicsObjectInterface updateWithoutPhysicsObjectInterfaceRail;
	/**
	 * The initial transforms.
	 */
	public Transform initialTransform;
	public int shapeShieldIndex = -1;
	private CollisionShape shape;
	private CompoundShapeChild shapeChield;
	/**
	 * The bodies.
	 */
	private CollisionObject object;
	public final Vector3f inertia = new Vector3f();
	private boolean initialized;
	//	private float[] openGlMatrix = new float[] {
	//			1.0f, 0.0f, 0.0f, 0.0f,
	//
	//			0.0f, 1.0f, 0.0f, 0.0f,
	//
	//			0.0f, 0.0f, 1.0f, 0.0f,
	//
	//			0.0f, 0.0f, 0.0f, 1.0f };
	private float lastUpdatedMass = -1;

	/**
	 * Clear physics info.
	 */
	public void clearPhysicsInfo() {
		setObject(null);
		initialTransform = null;
		setShape(null);
		setShapeChield(null, -1);
		//		physicsVehicleMap.clear();
		initialized = (false);
	}

	public TransformTimed getCurrentPhysicsTransform() {
		return cacheTransform;
	}

	//	public float[] getCurrentPhysicsOpenGl() {
	//		return openGlMatrix;
	//	}
	//
	//	public float[] getCurrentPhysicsOpenGlRefresh() {
	//		cacheTransform.getOpenGLMatrix(openGlMatrix);
	//		return openGlMatrix;
	//	}

	public CollisionObject getObject() {
		return object;
	}

	public void setObject(CollisionObject object) {
		if (object != null) {
			if (object.getCollisionShape().getShapeType() == BroadphaseNativeType.TERRAIN_SHAPE_PROXYTYPE) {
				throw new IllegalArgumentException("Tried to add raw cubeshape " + object);
			}
	        /*
             * prevent that a object switch to a new ColObject would result in
			 * one frame idMatrix from the not initialized motion state
			 */

			if (object instanceof RigidBody) {

				MotionState myMotionState = ((RigidBody) object)
						.getMotionState();
				myMotionState.setWorldTransform(object.getWorldTransform(new Transform()));

			}
			object.setInterpolationWorldTransform(object.getWorldTransform(new Transform()));
		}
		this.object = object;
	}

	public CollisionShape getShape() {
		return shape;
	}

	public void setShape(CollisionShape shape) {
		this.shape = shape;
		if (updateWithoutPhysicsObjectInterfaceRail != null) {
			updateWithoutPhysicsObjectInterfaceRail.checkRootIntegrity();
		}
	}

	/**
	 * @return the shapeChield
	 */
	public CompoundShapeChild getShapeChild() {
		return shapeChield;
	}

	/**
	 * @return the initialized
	 */
	public boolean isInitialized() {
		return initialized;
	}

	public void onPhysicsAdd() {
		lastUpdatedMass = -1;
	}

	public void onPhysicsRemove() {
		lastUpdatedMass = -1;
	}

	/**
	 * @param shapeChield the shapeChield to set
	 */
	public void setShapeChield(CompoundShapeChild shapeChield, int index) {
		this.shapeChield = shapeChield;
		this.shapeShieldIndex = index;
	}

	public void updateManually(Transform t) {
		cacheTransform.set(t);
	}
	private Transform intermediateTransform = new Transform();
	public void updateManuallyWithChildTrans(Transform t, CompoundShape rootShape) {
		if (rootShape instanceof CenterOfMassInterface && ((CenterOfMassInterface) rootShape).getTotalPhysicalMass() > 0f) {
			tmpTransform.set(t);
			Vector3f cc = ((CenterOfMassInterface) rootShape).getCenterOfMass();
			tmpTransform.basis.transform(cc);
			tmpTransform.origin.add(cc);
			intermediateTransform.set(tmpTransform);
		} else {
			intermediateTransform.set(t);
		}

		if (shapeChield != null) {
			assert (shapeShieldIndex < rootShape.getChildList().size()) : shapeShieldIndex + " : " + shapeChield.childShape + " -> " + rootShape + "; " + rootShape.getChildList();
			rootShape.getChildTransform(shapeShieldIndex, tmpTransform);
			if (!tmpTransform.equals(ident)) {
				Matrix4fTools.transformMul(intermediateTransform, tmpTransform);
			}
		}
		cacheTransform.set(intermediateTransform);
	}

	public void updateManuallyWithChildTransOld(Transform t) {
		cacheTransform.set(t);
		if (shapeChield != null) {
			if (!shapeChield.transform.equals(ident)) {
				Matrix4fTools.transformMul(cacheTransform, shapeChield.transform);
			}
		}

	}

	public boolean updateMass(float mass, boolean force) {
		if (initialized && shape != null){
			(shape).calculateLocalInertia(Math.max(mass, 2.5f), inertia);
			if(object != null) {
				if ((mass != lastUpdatedMass || force)) {
					if(object instanceof RigidBody){
						
						((RigidBody) object).setMassProps(mass, inertia);
						lastUpdatedMass = mass;
										
						//				System.err.println("SET MASS: "+mass);
						return true;
					}else{
						try{
							throw new NullPointerException("trued updating mass on "+ object +", "+shape);
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				}
			}

		} else {
			System.err.println("[PHYSICSCONTAINER][WARNING] Could not set mass!");
		}
		return false;
	}

	public void updatePhysical(final long time) {
		updatePhysical(object, time);
	}

	public void checkCenterOfMass(CollisionObject object) {
		if (shape instanceof CenterOfMassInterface && ((CenterOfMassInterface) shape).getTotalPhysicalMass() > 0f) {
			Vector3f cc = ((CenterOfMassInterface) shape).getCenterOfMass();
			if (!lastCenter.equals(cc)) {
				tmpLC.set(lastCenter);
				lastCenter.set(cc);

				Transform worldTransform = object.getWorldTransform(tmp);
				cc.sub(tmpLC);
				worldTransform.basis.transform(cc);
				worldTransform.origin.add(cc);

				((RigidBody) object).setWorldTransform(worldTransform);
				((RigidBody) object).setInterpolationWorldTransform(worldTransform);
				((RigidBody) object).getMotionState().setWorldTransform(worldTransform);
			}
		}

	}

	public Transform getOriginalTransform() {
		if (object != null) {
			object.getWorldTransform(originalTransform);
			tmpLC.set(lastCenter);
//			System.err.println(" "+lastCenter+"; ;; ;; "+originalTransform.origin);
			originalTransform.basis.transform(tmpLC);
			originalTransform.origin.sub(tmpLC);
//			System.err.println("#"+lastCenter+"; ;; ;; "+originalTransform.origin);
			return originalTransform;
		} else {
			originalTransform.set(cacheTransform);
			return originalTransform;
		}
	}

	public Transform addCenterOfMassToTransform(Transform inOut) {
		tmpLC.set(lastCenter);
		inOut.basis.transform(tmpLC);
		inOut.origin.add(tmpLC);
		return inOut;
	}

	private final Vector3f cc = new Vector3f();
	private final Vector3f cc1 = new Vector3f();
	private long currentTime;
	public short collisionGroup = CollisionFilterGroups.ALL_FILTER;
	public short collisionMask = CollisionFilterGroups.ALL_FILTER;
	/**
	 * called from
	 * org.schema.game.common.data.physics.ModifiedDynamicsWorld.updateActions(ModifiedDynamicsWorld.java:1281)
	 * at org.schema.game.common.data.physics.ModifiedDynamicsWorld.internalSingleStepSimulation(ModifiedDynamicsWorld.java:343)
	 *
	 * @param object
	 */
	public void updatePhysical(CollisionObject object, long time) {
		this.currentTime = time;
		if (object != null) {
			if (object instanceof RigidBody) {
				checkCenterOfMass(object);
				cc.set(cacheTransform.origin); 
				object.getWorldTransform(cacheTransform);
				cc1.set(cacheTransform.origin); 
				if (shapeChield != null) {
					//apply center of mass
					((CompoundShape) shape).getChildTransform(shapeShieldIndex, tmpTransform);
					if (!tmpTransform.equals(ident)) {
						Matrix4fTools.transformMul(cacheTransform, tmpTransform);
					}
				}
			} else {
				object.getWorldTransform(cacheTransform);
			}
			initialized = (true);
			
			if(onPhysicsObjectUpdateEnergyBeamInterface != null){
				onPhysicsObjectUpdateEnergyBeamInterface.updateWithoutPhysicsObject();
			}
		} else {
			updatePhysicalWithoutObject();
		}
	}

	public void updatePhysicalWithoutObject() {
		if (oldDockingUpdateWithoutPhysicsObjectInterface != null) {
			oldDockingUpdateWithoutPhysicsObjectInterface.updateWithoutPhysicsObject();
		}
		if (updateWithoutPhysicsObjectInterfaceRail != null) {
			updateWithoutPhysicsObjectInterfaceRail.updateWithoutPhysicsObject();
		}
	}

	public void setInitial(Transform t) {
		initialTransform = new TransformExt();
		initialTransform.set(t);
	}

	public class TransformExt extends TransformTimed {
		@Override
		public void set(Transform tr) {
			if(!tr.equals(this)){
				lastChanged = currentTime;
			}
			super.set(tr);
		}

		
	}

	public boolean isStatic() {
		return object == null && object instanceof RigidBody;
	}

}
