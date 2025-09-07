package org.schema.game.common.data.physics;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;
import com.bulletphysics.collision.dispatch.GhostPairCallback;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.extras.gimpact.GImpactCollisionAlgorithm;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Quat4fTools;
import org.schema.common.util.linAlg.TransformTools;
import org.schema.game.client.view.camera.InShipCamera;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.physics.shape.GameShape;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.client.ClientState;
import org.schema.schine.physics.*;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

public class PhysicsExt extends Physics {

	public static int UPDATE_NUM;

	long wasDocked;

	private AxisSweep3Ext axisSweep3Ext;

	private DbvtBroadphase dbvtBroadphase;

	// public static void getTarget(Vector3f toDirectionForward){
	// PhysicsHelperVars h = threadLocal.get();
	// 
	// h.toForward.set(toDirectionForward);
	// h.toForward.normalize();
	// 
	// h.toRight.set(toDirectionRight);
	// h.toRight.normalize();
	// 
	// h.toUp.set(toDirectionUp);
	// h.toUp.normalize();
	// 
	// dockingTarget.getWorldTransformInverse().basis.transform(h.toRight);
	// dockingTarget.getWorldTransformInverse().basis.transform(h.toForward);
	// dockingTarget.getWorldTransformInverse().basis.transform(h.toUp);
	// 
	// 
	// Transform toMatrix = new Transform();
	// GlUtil.setForwardVector(h.toForward, toMatrix);
	// GlUtil.setUpVector(h.toUp, toMatrix);
	// GlUtil.setRightVector(h.toRight, toMatrix);
	// 
	// Quat4f to = new Quat4f();
	// Quat4fTools.set(toMatrix.basis, to);
	// segmentController.getDockingController().targetQuaternion.set(to);
	// }
	/* (non-Javadoc)
	 * @see org.schema.schine.physics.Physics#update(org.schema.schine.graphicsengine.core.Timer, float)
	 */
	// @Override
	// public void update(Timer timer, float highestSubStep) {
	// super.update(timer, highestSubStep);
	// if(this.getState() instanceof Sector && ((Sector)getState()).pos.equals(1, 2, 2)){
	// for(int i = 0; i < getDynamicsWorld().getNumCollisionObjects(); i++){
	// CollisionObject collisionObject = getDynamicsWorld().getCollisionObjectArray().get(i);
	// if(collisionObject instanceof RigidBody){
	// if(collisionObject.toString().contains("schema_1370260089078")){
	// //						System.err.println("colllll "+i+": "+collisionObject+"; "+collisionObject.getWorldTransform(new Transform()).origin);
	// }
	// }
	// }
	// }
	// }
	public PhysicsExt(PhysicsState state) {
		super(state);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.physics.Physics#addObject(com.bulletphysics.collision.dispatch.CollisionObject, short, short)
	 */
	@Override
	public void addObject(CollisionObject object, short group, short mask) {
		if (object == null || object.getCollisionShape() instanceof CubeShape) {
			throw new NullPointerException("Added nonconform object " + object);
		}
		if (object instanceof RigidBodySegmentController && !((RigidBodySegmentController) object).isVirtual() && getState() instanceof Sector) {
			Sector ownSector = (Sector) getState();
			// server
			RigidBodySegmentController b = (RigidBodySegmentController) object;
			int sectorId = b.getSegmentController().getSectorId();
			assert (sectorId == ownSector.getId()) : b + " :" + ownSector + "; object sector id " + sectorId + "; current load: " + ownSector.getState().getUniverse().getSector(sectorId);
			assert (ownSector == b.getSectorOnServer()) : ownSector + " --- " + b.getSectorOnServer();
			if (ownSector != b.getSectorOnServer()) {
				try {
					throw new IllegalArgumentException("Trying to add entity to sector " + ownSector + " but it's sectorID is currently " + b.getSectorOnServer());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		assert (getState() == ((ModifiedDynamicsWorld) getDynamicsWorld()).getState());
		super.addObject(object, group, mask);
	}

	@Override
	public void cleanUp() {
		super.cleanUp();
		if (axisSweep3Ext != null) {
			axisSweep3Ext.cleanUp();
		}
		dynamicsWorld.setBroadphase(null);
		setOverlappingPairCache(null);
		setState(null);
	}

	/**
	 * Adds the body.
	 *
	 * @param shape           the shape
	 * @param mass            the mass
	 * @param groundTransform the ground transform
	 * @param collisionGroup  the collision group
	 * @param colissionMask   the colission mask
	 * @return the rigid body
	 */
	@Override
	public RigidBody getBodyFromShape(CollisionShape shape, float mass, Transform groundTransform) {
		// rigidbody is dynamic if and only if mass is non zero, otherwise
		// static
		boolean isDynamic = (mass != 0f);
		assert (!(shape instanceof CompoundShape && ((CompoundShape) shape).getNumChildShapes() == 0)) : "tried to add empty compound shape";
		Vector3f inertia = new Vector3f();
		if (isDynamic) {
			shape.calculateLocalInertia(mass, inertia);
		// System.err.println("local inertia "+inertia);
		}
		shape.setMargin(0.001f);
		// using motionstate is recommended, it provides interpolation
		// capabilities, and only synchronizes 'active' objects
		ExtMotionState myMotionState = new ExtMotionState(groundTransform);
		// try {
		// throw new NullPointerException(shape.toString());
		// }catch(Exception e){
		// e.printStackTrace();
		// }
		// System.err.println("[PHYSICS] creating rb info");
		RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, myMotionState, shape, inertia);
		// System.err.println("[PHYSICS] ground transform "+rbInfo.startWorldTransform.origin+", inertia "+rbInfo.localInertia);
		// System.err.println("CREATING NEW PHYSICS BODY with SHAPE: "+shape);
		RigidBody body = null;
		assert (!(shape instanceof CubeShape));
		if (shape instanceof CubesCompoundShape) {
			body = new RigidBodySegmentController(((CubesCompoundShape) shape).getSegmentController(), rbInfo);
		} else if (shape instanceof GameShape) {
			body = new RigidBodySimple(((GameShape) shape).getSimpleTransformableSendableObject(), rbInfo);
		} else if ("shard".equals(shape.getUserPointer())) {
			body = new RigidDebrisBody(rbInfo);
		} else if (shape instanceof LiftBoxShape) {
			body = new RigidBodyExt(CollisionType.LIFT, rbInfo);
		} else {
			assert (false) : "OTHER SHAPE " + shape;
			body = new RigidBodyExt(CollisionType.OTHER, rbInfo);
		}
		/*
		 * Anti tunneling by Motion Clamping
		 *
		 * Bullet 2.72 re-enables CCD based motion clamping. It lets the
		 * developer enable clamping of motion if the motion per simulation step
		 * exceeds a given threshold. This feature works on a per-object basis.
		 *
		 * CCD motion clamping activates for convex objects that exceed a
		 * (squared to avoid taking square roots) velocity threshold. By default
		 * this threshold is zero, which means this feature is disabled for
		 * rigid bodies.
		 */
		// thinnest object
		body.setCcdMotionThreshold(1);
		// FIXME check if correct! added 16.7.2014
		// thinnest object sweep sphere
		body.setCcdSweptSphereRadius(FastMath.sqrt(2));
		/*
		 * Resitution(0.0 to 1.0) represent a value of energy return on
		 * collision. Bullet doesn'transformationArray naturally support infinite return.
		 * So setting a value of 1+ will not cause an object to bounce
		 * forever, but it will bounce for a long time.
		 */
		if (mass > 0) {
			body.setRestitution(0.1f);
		} else {
			body.setRestitution(0.0f);
		}
		/*
		 * Friction(0.0 to 1.0) roughness of the surface. This is slide
		 * of an object. 0 is as slick as possible while 1 should
		 * present no sliding. Think of it as Black Ice to very coarse
		 * sandpaper.
		 */
		body.setFriction(0.7f);
		/*
		 * Damping(0.0 to 1.0) Linear and rotation/axis damping. This is
		 * best represented as atmosphere. Space has no atmosphere an
		 * thus objects will move and rotate forever. While water would
		 * be very dense and limit movement and rotation very fast
		 * without constant force being added.
		 */
		if (mass > 0) {
			if (body instanceof RigidBodySegmentController) {
				body.setDamping(((RigidBodySegmentController) body).getSegmentController().getLinearDamping(), ((RigidBodySegmentController) body).getSegmentController().getRotationalDamping());
			} else {
				body.setDamping(getState().getLinearDamping(), getState().getRotationalDamping());
			}
		} else {
			body.setDamping(0, 0);
		}
		body.setMassProps(mass, inertia);
		if (mass > 0) {
			body.updateInertiaTensor();
		}
		if (!isDynamic) {
			body.setCollisionFlags(body.getCollisionFlags() | CollisionFlags.KINEMATIC_OBJECT);
		}
		/*
		 * can save CPU modify the threshold velocities for when bullet
		 * does deactivate a rigid body. The default values are a bit
		 * "aggressive", in order to deactivate as much as possible and
		 * save CPU. default 0.8, 1.0
		 */
		// body.setSleepingThresholds(0.8, 1);
		/*
		 * Kinematic Bodies If you plan to animate or move static objects, you
		 * should flag them as kinematic. Also disable the sleeping/deactivation
		 * for them during the animation. This means Bullet dynamics world will
		 * get the new worldtransform from the btMotionState every simulation
		 * frame.
		 *
		 * body->setCollisionFlags( body->getCollisionFlags() |
		 * btCollisionObject::CF_KINEMATIC_OBJECT);
		 * body->setActivationState(DISABLE_DEACTIVATION);
		 */
		// body.setSleepingThresholds(20, 4);
		/* since jBullet is not giving any documentation about it, I will:
		 * void com.bulletphysics.dynamics.DiscreteDynamicsWorld.addRigidBody(RigidBody body, short group, short mask)
		 * - the group is a simple short identifier, in which collision group this body belongs
		 * - all the values like (short)(GROUP_GROUND ^ GROUP_DEFAULT_OBJECT ^ ...) determine,
		 * which which group this body can collide with.
		 * both colliding objects have to have one identical group in their mask
		 * (Object A has to collide with Group_of_B and object B has to collide with Group_of_A)
		 * both respective group identifiers have to be in the collision mask!
		 *
		 */
		// add the body to the dynamics world
		// ((DiscreteDynamicsWorld)dynamicsWorld).addRigidBody(body);
		return body;
	}

	/**
	 * this method initializes the basic functionality of jBullet
	 * it sets the gravity and adds a static box as the ground.
	 */
	@Override
	public void initPhysics() {
		// collision configuration contains default setup for memory, collision
		// setup
		setCollisionConfiguration(new CubeCollissionConfiguration());
		// use the default collision dispatcher. For parallel processing you can
		// use a diffent dispatcher (see Extras/BulletMultiThreaded)
		setDispatcher(new CollisionDispatcherExt(getCollisionConfiguration()));
		// the maximum size of the collision world. Make sure objects stay
		// within these boundaries
		// TODO: AxisSweep3
		// Don'transformationArray make the world AABB size too large, it will harm simulation
		// quality and performance
		Vector3f worldAabbMin = new Vector3f(-2000, -2000, -2000);
		Vector3f worldAabbMax = new Vector3f(2000, 2000, 2000);
		// axisSweep3Ext = new AxisSweep3Ext(worldAabbMin, worldAabbMax,
		// 16, new HashedOverlappingPairCache());
		// setOverlappingPairCache(axisSweep3Ext);
		/*
		 * The btDbvtBroadphase implements a broadphase using two dynamic AABB
		 * bounding volume hierarchies/trees (see btDbvt). One tree is used for
		 * static/non-moving objects, and another tree is used for dynamic
		 * objects. Objects can move from one tree to the other. This is a very
		 * fast broadphase, especially for very dynamic worlds where many
		 * objects are moving. Its insert/add and remove of objects is generally
		 * faster than the sweep and prune broadphases btAxisSweep3 and
		 * bt32BitAxisSweep3.
		 */
		dbvtBroadphase = new DbvtBroadphaseExt(new HashedOverlappingPairCacheExt());
		setOverlappingPairCache(dbvtBroadphase);
		// overlappingPairCache = new SimpleBroadphase(MAX_PROXIES);
		// the default constraint solver. For parallel processing you can use a
		// different solver (see Extras/BulletMultiThreaded)
		ConstraintSolver sol = new SequentialImpulseConstraintSolverExt();
		setSolver(sol);
		// TODO: needed for SimpleDynamicsWorld
		// sol.setSolverMode(sol.getSolverMode() &
		// ~SolverMode.SOLVER_CACHE_FRIENDLY.getMask());
		setDynamicsWorld(new ModifiedDynamicsWorld(getDispatcher(), getOverlappingPairCache(), getSolver(), getCollisionConfiguration(), this));
		// dynamicsWorld = new SimpleDynamicsWorld(dispatcher,
		// overlappingPairCache, solver, collisionConfiguration);
		CollisionDispatcher dispatcher = (CollisionDispatcher) dynamicsWorld.getDispatcher();
		GImpactCollisionAlgorithm.registerAlgorithm(dispatcher);
		getDynamicsWorld().setGravity(new Vector3f(0f, -PHYSICS_GRAVITY, 0f));
		// create a few basic rigid bodies
		getDynamicsWorld().getPairCache().setInternalGhostPairCallback(new GhostPairCallback());
		// getDynamicsWorld().getSolverInfo().splitImpulse = true;
		BulletGlobals.setContactBreakingThreshold(0.04f);
		getDynamicsWorld().getSolverInfo().linearSlop = 0.000001f;
	// addGround();
	}

	@Override
	public void onOrientateOnly(Physical entity, float timeStep) {
		((SegmentController) entity).railController.onOrientatePhysics(timeStep);
	}

	/**
	 * This method orientates a physical objects by the means of
	 * setting its angular velocity to go towards a desired rotation
	 * defined by toDirectionForward, toDirectionUp, and toDirectionRight
	 *
	 * The speed/force of the rotation is defined by oForce
	 *
	 * How it currently works:
	 *
	 * 1. Project the "to" vectors onto the coordinate system defined by the the current rotation
	 * 2. Take the difference as an angle for each vector
	 * 3. That difference defines the angular velocity to go towards the desired direction
	 */
	@Override
	public void orientate(Physical entity, Vector3f toDirectionForward, Vector3f toDirectionUp, Vector3f toDirectionRight, float oForceX, float oForceY, float oForceZ, float timeStep) {
		if (entity instanceof SegmentController && ((SegmentController) entity).railController.isDockedAndExecuted()) {
			if (!((SegmentController) entity).railController.isClientCameraSet()) {
				return;
			}
			if (((SegmentController) entity).railController.isTurretDocked()) {
				orientateRailTurret(entity, toDirectionForward, toDirectionUp, toDirectionRight, oForceX, oForceY, oForceZ, timeStep, ((SegmentController) entity).railController.getTurretRailSpeed());
				((SegmentController) entity).railController.onOrientatePhysics(timeStep);
			}
			wasDocked = entity.getState().getUpdateTime();
			return;
		} else if (entity instanceof SegmentController && ((SegmentController) entity).getDockingController().isDocked()) {
			// old deprecated
			orientateTurret(entity, toDirectionForward, toDirectionUp, toDirectionRight, oForceX, oForceY, oForceZ, timeStep);
			wasDocked = System.currentTimeMillis();
			return;
		}
		if (entity.getState() instanceof ClientState && Controller.getCamera() != null && Controller.getCamera() instanceof InShipCamera && ((InShipCamera) Controller.getCamera()).docked) {
			return;
		}
		RigidBody body = (RigidBody) entity.getPhysicsDataContainer().getObject();
		if (body == null || body.getCollisionFlags() == CollisionFlags.STATIC_OBJECT || entity.getMass() <= 0) {
			return;
		}
		float fMod = 1;
		// get the transform for this mesh that corresponds to that game entity
		Transform currentTranform = entity.getPhysicsDataContainer().getCurrentPhysicsTransform();
		PhysicsHelperVars h = threadLocal.get();
		// extract the vectors
		GlUtil.getForwardVector(h.currentForward, currentTranform);
		GlUtil.getUpVector(h.currentUp, currentTranform);
		GlUtil.getRightVector(h.currentRight, currentTranform);
		Transform toTrans = new Transform();
		toTrans.setIdentity();
		// set the 3 desiered outcome vectors into toTrans
		GlUtil.setForwardVector(toDirectionForward, toTrans);
		GlUtil.setUpVector(toDirectionUp, toTrans);
		GlUtil.setRightVector(toDirectionRight, toTrans);
		// EULER TRY
		Quat4f from = new Quat4f();
		Quat4fTools.set(currentTranform.basis, from);
		Quat4f to = new Quat4f();
		Quat4fTools.set(toTrans.basis, to);
		// slerp between from and t
		Quat4f res = new Quat4f();
		res.set(to);
		res.scale(timeStep);
		// mult inverse to get necessary rotation
		res.inverse();
		res.mul(from);
		// convert to euler
		Vector3f eul = Quat4fTools.toEuler(res);
		// save roll of this rotation
		float rollZ = -eul.y;
		// create rotation matrix that undoes roll
		// for the goal
		Matrix3f s = new Matrix3f();
		s.setIdentity();
		s.rotZ(rollZ);
		s.invert();
		Quat4f mu = new Quat4f();
		Quat4fTools.set(s, mu);
		to.mul(mu);
		// slerp to new goal (without roll)
		// Quat4Util.slerp(from, to, 1, res);
		res.set(to);
		res.scale(timeStep);
		res.inverse();
		res.mul(from);
		eul = Quat4fTools.toEuler(res);
		// apply new pitch/yaw that compensate for roll. apply original roll
		// -eul.y
		h.axis.set(-eul.z * oForceX, -eul.x * oForceY, rollZ * oForceZ);
		h.axis.scale(timeStep * 80f);
		// transform the result because setAngularVelocity takes ABSOLUTE values
		// this means that setting it to e.g. 0 1 0 will roll you if you happen
		// to look up
		currentTranform.basis.transform(h.axis);
		if (Float.isNaN(h.axis.x) || Float.isNaN(h.axis.y) || Float.isNaN(h.axis.z)) {
			// fatal glitch prevention in case any value is NaN
			System.err.println("[PHYSICS] " + entity.getState() + ": " + entity + " WARNING: Axis was NaN: " + h.axis + "; happens when desired direction is exactly opposite to the current one (vec length 0): giving vector small nugde to fix");
			h.axis.set(0, 0, 0.0000001f);
		}
		if (h.axis.length() > 0.00005f && !body.isActive()) {
			// if body is sleepy, activate it
			body.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			//AudioController.fireAudioEventID(955);
		}
		boolean constr = false;
		if (toDirectionForward != null && toDirectionForward.length() > 0 && toDirectionUp != null && toDirectionUp.length() > 0) {
			Transform pred = new Transform();
			pred.setIdentity();
			// does repeated stepts to make sure that the movement doesn't jitter
			// this keeps rotation for overshooting target on low frame rates
			// or lag
			if (!Float.isNaN(h.axis.x) && !Float.isNaN(h.axis.y) && !Float.isNaN(h.axis.z)) {
				// not exactly efficient but it works
				boolean toFar = false;
				int i = 0;
				float angleP;
				float angleO;
				do {
					TransformTools.integrateTransform(body.getWorldTransform(new Transform()), body.getLinearVelocity(new Vector3f()), h.axis, timeStep, pred, new Vector3f(), new Quat4f(), new Quat4f(), new Quat4f(), new float[4]);
					angleP = TransformTools.calculateDiffAxisAngle(body.getWorldTransform(new Transform()), pred, new Vector3f(), new Matrix3f(), new Matrix3f(), new Quat4f());
					angleO = TransformTools.calculateDiffAxisAngle(body.getWorldTransform(new Transform()), toTrans, new Vector3f(), new Matrix3f(), new Matrix3f(), new Quat4f());
					toFar = (angleP > angleO);
					if (toFar) {
						h.axis.scale(0.9f);
						i++;
					}
				} while (toFar && i < 300);
				if (i >= 300) {
					System.err.println("PHYSICS: warning: " + body + " has >" + i + " orientation iterations: " + h.axis + "; " + body.getLinearVelocity(new Vector3f()) + ", " + angleP + " > " + angleO + "\n" + body.getWorldTransform(new Transform()).getMatrix(new Matrix4f()) + "\n--------\n" + pred.getMatrix(new Matrix4f()) + "\n-------------\n" + toTrans.getMatrix(new Matrix4f()));
				}
			}
			// Result
			if (!Float.isNaN(h.axis.x) && !Float.isNaN(h.axis.y) && !Float.isNaN(h.axis.z)) {
				// This is where the entity's angular velocity is set
				// orienting it towards the the matrix it was set to be looking at
				((RigidBodySegmentController) body).setAngularVelocity(h.axis);
			} else {
			}
			h.lastAxis.set(h.axis);
		}
	}

	@Override
	public void softClean() {
		int numCollisionObjects = getDynamicsWorld().getCollisionObjectArray().size();
		for (int i = 0; i < numCollisionObjects; i++) {
			CollisionObject collisionObject = getDynamicsWorld().getCollisionObjectArray().get(i);
			System.err.println("WARNING: REMOVING EXCESS OBJECT FROM PHYSICS " + getState() + ": " + collisionObject);
			if (collisionObject instanceof RigidBody) {
				getDynamicsWorld().removeRigidBody((RigidBody) collisionObject);
			} else {
				getDynamicsWorld().removeCollisionObject(collisionObject);
			}
		}
		if (dbvtBroadphase != null) {
			// FIXME: using the old may cause physics exceptions (probably because of virtual objects)
			dbvtBroadphase = new DbvtBroadphaseExt(new HashedOverlappingPairCacheExt());
			setOverlappingPairCache(dbvtBroadphase);
		}
		if (axisSweep3Ext != null) {
			axisSweep3Ext.cleanUpReferences();
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.physics.Physics#update(org.schema.schine.graphicsengine.core.Timer, float)
	 */
	@Override
	public void update(Timer timer, float highestSubStep) {
		UPDATE_NUM++;
		super.update(timer, highestSubStep);
	}

	public void orientateTurret(Physical entity, Vector3f toDirectionForward, Vector3f toDirectionUp, Vector3f toDirectionRight, float oForceX, float oForceY, float oForceZ, float timeStep) {
		CubeShape shapeChield = (CubeShape) entity.getPhysicsDataContainer().getShapeChild().childShape;
		SegmentController segmentController = shapeChield.getSegmentBuffer().getSegmentController();
		SegmentController dockingTarget = segmentController.getDockingController().getDockedOn().to.getSegment().getSegmentController();
		PhysicsHelperVars h = threadLocal.get();
		h.toRight.set(toDirectionRight);
		h.toRight.normalize();
		h.toForward.set(toDirectionForward);
		h.toForward.normalize();
		h.toUp.set(toDirectionUp);
		h.toUp.normalize();
		Vector3f f = new Vector3f(h.toForward);
		Vector3f r = new Vector3f(h.toRight);
		Vector3f u = new Vector3f(h.toUp);
		dockingTarget.getWorldTransformInverse().basis.transform(h.toRight);
		dockingTarget.getWorldTransformInverse().basis.transform(h.toForward);
		dockingTarget.getWorldTransformInverse().basis.transform(h.toUp);
		Transform toMatrix = GlUtil.setTrans(h.toRight, h.toUp, h.toForward, new Transform());
		Quat4f to = Quat4fTools.set(toMatrix.basis, new Quat4f());
		segmentController.getDockingController().targetQuaternion.set(to);
	// if(entity.getState() instanceof ClientState){
	// System.err.println("#### "+" OOO: "+r+"; "+u+"; "+f+" ---- "+h.toRight+";;;; "+h.toUp+";;;; "+h.toForward+";    "+to);
	// }
	}

	public void orientateRailTurret(Physical entity, Vector3f toDirectionForward, Vector3f toDirectionUp, Vector3f toDirectionRight, float oForceX, float oForceY, float oForceZ, float timeStep, float rotationSpeed) {
		SegmentController segmentController = ((CubeShape) entity.getPhysicsDataContainer().getShapeChild().childShape).getSegmentBuffer().getSegmentController();
		// if(segmentController.isClientOwnObject()){
		// System.err.println("INPUT::: \n"+toDirectionRight+", "+toDirectionUp+", "+toDirectionForward+"---\n"+segmentController.railController.getRailMovingLocalTransform().basis);
		// }
		segmentController.railController.railMovingLocalTransformTargetBef.set(segmentController.railController.getRailMovingLocalTransformTarget());
		orientateRailTurret(entity, toDirectionForward, toDirectionUp, toDirectionRight, oForceX, oForceY, oForceZ, timeStep, rotationSpeed, segmentController.railController.getRailUpToThisOriginalLocalTransform(), segmentController.railController.getRailMovingLocalTransformTarget());
		if (!segmentController.railController.isTurretDocked()) {
			if (!segmentController.railController.railMovingLocalTransformTargetBef.equals(segmentController.railController.getRailMovingLocalTransformTarget())) {
				System.err.println("MATRIX BEF:\n" + segmentController.railController.railMovingLocalTransformTargetBef.getMatrix(new Matrix4f()) + "\nNOW:\n" + segmentController.railController.getRailMovingLocalTransformTarget().getMatrix(new Matrix4f()));
				throw new RuntimeException("Illegal turret transformation (possible culprit of turret missalign bug. please report this error and the logs)");
			}
		}
	// if(segmentController.isClientOwnObject()){
	// System.err.println("OUTPUT::: \n"+segmentController.railController.getRailMovingLocalTransform().basis);
	// }
	}

	private Transform tmp0 = new Transform();

	private Transform tmp1 = new Transform();

	private Quat4f qtmp0 = new Quat4f();

	public void orientateRailTurret(Physical entity, Vector3f toDirectionForward, Vector3f toDirectionUp, Vector3f toDirectionRight, float oForceX, float oForceY, float oForceZ, float timeStep, float rotationSpeed, Transform tFromSet, Transform tToSet) {
		assert (entity.getPhysicsDataContainer().getShapeChild() != null) : entity;
		CubeShape shapeChield = (CubeShape) entity.getPhysicsDataContainer().getShapeChild().childShape;
		SegmentController segmentController = shapeChield.getSegmentBuffer().getSegmentController();
		SegmentController dockingTarget = segmentController.railController.previous.rail.getSegmentController();
		PhysicsHelperVars h = threadLocal.get();
		h.toRight.set(toDirectionRight);
		h.toRight.normalize();
		h.toForward.set(toDirectionForward);
		h.toForward.normalize();
		h.toUp.set(toDirectionUp);
		h.toUp.normalize();
		Transform motherShipTranform = tmp0;
		motherShipTranform.set(segmentController.railController.getRoot().getWorldTransform());
		motherShipTranform.mul(tFromSet);
		motherShipTranform.inverse();
		Transform toMatrix = GlUtil.setTrans(h.toRight, h.toUp, h.toForward, tmp1);
		motherShipTranform.mul(toMatrix);
		toMatrix.set(motherShipTranform);
		// assert(checkConvo(toMatrix));
		tToSet.basis.set(toMatrix.basis);
	}

	// /**
	// * this checks if quaternion conversions are fine to to at all (might have been the reason for the rail missaglin bug)
	// *
	// * this can be removed if bug is fixed or another reason caused the bug
	// * @param toMatrix
	// * @return
	// */
	// private boolean checkConvo(Transform toMatrix) {
	// PhysicsHelperVars h = threadLocal.get();
	// Quat4f to = Quat4fTools.set(toMatrix.basis, qtmp0);
	// h.mat.set(to);
	// assert(h.mat.equals(toMatrix.basis)):h.mat+" \n\n"+toMatrix.basis;
	// return h.mat.equals(toMatrix.basis);
	// }
	public ClosestRayResultCallback testRayCollisionPoint(Vector3f position, Vector3f posTo, boolean staticOnly, SimpleTransformableSendableObject owner, SegmentController filter, boolean ignoreNotPhysical, boolean ignoreDebris, boolean checkStabilizerPath) {
		return testRayCollisionPoint(position, posTo, staticOnly, owner, filter, ignoreNotPhysical, ignoreDebris, null, true, false, checkStabilizerPath);
	}

	public ClosestRayResultCallback testRayCollisionPoint(Vector3f position, Vector3f posTo, boolean staticOnly, SimpleTransformableSendableObject owner, SegmentController filter, boolean ignoreNotPhysical, boolean ignoreDebris, boolean zeroHpPhysical, boolean damageDealing, boolean checkStabilizerPath) {
		return testRayCollisionPoint(position, posTo, staticOnly, owner, filter, ignoreNotPhysical, ignoreDebris, null, zeroHpPhysical, damageDealing, checkStabilizerPath);
	}

	public ClosestRayResultCallback testRayCollisionPoint(Vector3f position, Vector3f posTo, boolean staticOnly, SimpleTransformableSendableObject owner, SegmentController filter, boolean ignoreNotPhysical, boolean ignoreDebris, Int2ObjectOpenHashMap<LongOpenHashSet> blockFilter, boolean checkStabilizerPath) {
		return testRayCollisionPoint(position, posTo, staticOnly, owner, filter, ignoreNotPhysical, ignoreDebris, blockFilter, true, false, checkStabilizerPath);
	}

	public ClosestRayResultCallback testRayCollisionPoint(Vector3f position, Vector3f posTo, boolean staticOnly, SimpleTransformableSendableObject owner, SegmentController filter, boolean ignoreNotPhysical, boolean ignoreDebris, Int2ObjectOpenHashMap<LongOpenHashSet> blockFilter, boolean zeroHpPhysical, boolean isDamageTest, boolean checkStabilizerPath) {
		CubeRayCastResult rayCallback;
		if (filter != null) {
			rayCallback = new CubeRayCastResult(position, posTo, owner, filter);
		} else {
			rayCallback = new CubeRayCastResult(position, posTo, owner);
		}
		rayCallback.setDamageTest(isDamageTest);
		rayCallback.setIgnoereNotPhysical(ignoreNotPhysical);
		rayCallback.setIgnoreDebris(ignoreDebris);
		rayCallback.setZeroHpPhysical(zeroHpPhysical);
		rayCallback.setCheckStabilizerPaths(checkStabilizerPath);
		if (blockFilter != null) {
			rayCallback.setHasCollidingBlockFilter(true);
			rayCallback.setCollidingBlocks(blockFilter);
		} else {
			rayCallback.setHasCollidingBlockFilter(false);
			rayCallback.setCollidingBlocks(null);
		}
		assert (!rayCallback.hasHit());
		((ModifiedDynamicsWorld) dynamicsWorld).rayTest(position, posTo, rayCallback);
		if (rayCallback.collisionObject != null && !(rayCallback.collisionObject instanceof RigidBodySegmentController)) {
			// collision with non cube
			// clear hit segment
			rayCallback.setSegment(null);
		}
		if (rayCallback.collisionObject != null) {
			RigidBody body = RigidBody.upcast(rayCallback.collisionObject);
			if (body != null) {
				if (staticOnly && !body.isStaticObject() && !body.isKinematicObject()) {
					return null;
				}
			}
		}
		return rayCallback;
	}

	public ClosestRayResultCallback testRayCollisionPoint(Vector3f position, Vector3f posTo, CubeRayCastResult rayCallback, boolean staticOnly) {
		((ModifiedDynamicsWorld) dynamicsWorld).rayTest(position, posTo, rayCallback);
		if (rayCallback.collisionObject != null) {
			RigidBody body = RigidBody.upcast(rayCallback.collisionObject);
			if (body != null) {
				if (staticOnly && !body.isStaticObject() && !body.isKinematicObject()) {
					// System.err.println("STATIC ONLY AND BODY "+body+" IS NOT STATIC");
					return null;
				}
			}
		}
		return rayCallback;
	}
}
