package org.schema.game.common.data.physics;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.collision.broadphase.BroadphasePair;
import com.bulletphysics.collision.broadphase.BroadphaseProxy;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.dispatch.CollisionWorld.LocalConvexResult;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.dynamics.character.KinematicCharacterController;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.player.AbstractCharacterInterface;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.client.ClientStateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerStateInterface;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

public class KinematicCharacterControllerExt extends
		KinematicCharacterController {

	// private ArrayList<Vector3f> touchingNormals = new ArrayList<Vector3f>();
	public static Vector3f[] upAxisDirectionDefault = new Vector3f[]{
			new Vector3f(1.0f, 0.0f, 0.0f),
			new Vector3f(0.0f, 1.0f, 0.0f),
			new Vector3f(0.0f, 0.0f, 1.0f),};
	private final float extendedCharacterHeight;
	private final Vector3f startedPosition = new Vector3f();
	private final Vector3f endedPosition = new Vector3f();
	private final Vector3f lastActualWalkingDist = new Vector3f();
	private final Vector3f minAabb = new Vector3f();
	private final Vector3f maxAabb = new Vector3f();
	public Vector3f[] upAxisDirection = new Vector3f[]{
			new Vector3f(1.0f, 0.0f, 0.0f),
			new Vector3f(0.0f, 1.0f, 0.0f),
			new Vector3f(0.0f, 0.0f, 1.0f),};
	Vector3f move = new Vector3f();
	Transform xform = new Transform();
	Transform start = new Transform();
	Transform end = new Transform();
	Vector3f distance2Vec = new Vector3f();
	Vector3f hitDistanceVec = new Vector3f();
	Vector3f hitDistanceDownPosVec = new Vector3f();
	Vector3f currentDir = new Vector3f();
	Transform tmp = new Transform();
	Vector3f before = new Vector3f();
	Vector3f after;
	int penetrationCounter;
	// keep track of the contact manifolds
	ObjectArrayList<PersistentManifold> manifoldArray = new ObjectArrayList<PersistentManifold>();
	Transform ttmp = new Transform();

	Vector3f normal = new Vector3f();
	private AbstractCharacterInterface obj;
	private CapsuleShapeExt capsule;
	private CapsuleShapeExt strafeCapsule;
	private long jumpingStarted = -1;
	private Vector3f velocity = new Vector3f();
	private boolean lastStepDown;

	public KinematicCharacterControllerExt(AbstractCharacterInterface obj,
	                                       PairCachingGhostObjectExt ghostObject, ConvexShape convexShape,
	                                       float stepHeight) {
		super(ghostObject, convexShape, stepHeight);
		extendedCharacterHeight = obj.getCharacterHeight() + 0.27f;
		this.obj = obj;
		currentPosition
				.set(ghostObject.getWorldTransform(new Transform()).origin);
		//		System.err.println("[KINEMATIC] CREATED PLAYERCHARACTER OBJECT: "
		//				+ currentPosition);

	}

	public static void getAddRot(int orient, Matrix3f out) {
		switch (orient) {
			case (Element.TOP):

				break;
			case (Element.BOTTOM):
				out.rotX(FastMath.PI);
				//				Vector3f f = new Vector3f();
				//				Vector3f u = new Vector3f();
				//				Vector3f r = new Vector3f();
				//				addRot.negate();
				//				GlUtil.getUpVector(up, addRot);
				break;
			case (Element.FRONT):
				out.rotX(-FastMath.HALF_PI);
				//				Vector3f r = GlUtil.getRightVector(new Vector3f(), addRot);
				//				r.negate();
				//				GlUtil.setRightVector(r, addRot);
				//
				//				Vector3f u = GlUtil.getUpVector(new Vector3f(), addRot);
				//				u.negate();
				//				GlUtil.setUpVector(u, addRot);

				//				addRot.negate();
				break;
			case (Element.BACK):
				out.rotX(FastMath.HALF_PI);
				break;
			case (Element.RIGHT):
				out.rotZ(FastMath.HALF_PI);
				break;
			case (Element.LEFT):
				out.rotZ(-FastMath.HALF_PI);
				//				addRot.negate();
				break;
		}
	}

	private void adaptLocalAttached() {
		PairCachingGhostObjectAlignable ghostObject = (PairCachingGhostObjectAlignable) this.ghostObject;
		if (ghostObject.getAttached() != null) {
			//			System.err.println("ATTACHED: "+ghostObject.getAttached());
			// Matrix4f attWT =
			// ghostObject.attached.getPhysicsDataContainer().getObject().getWorldTransform(new
			// Transform()).getMatrix(new Matrix4f());
			
			Transform tAttached = new Transform(ghostObject.getAttached()
					.getWorldTransform());
			tAttached.basis.mul(getAddRoation());

			Matrix4f attWT = tAttached.getMatrix(new Matrix4f());

			Matrix4f matrixWorld = new Matrix4f(attWT);

			Matrix4f matrixInv = new Matrix4f(attWT);
			matrixInv.invert();

			Matrix4f matrixNewWorld = xform.getMatrix(new Matrix4f());

			matrixInv.mul(matrixNewWorld);
			
//			Transform tAfter = new Transform(matrixInv);
			
//			if(!obj.isOnServer()){
//				System.err.println("TTT ::: "+tAfter.origin+";  ATTACHED: "+tAttached.origin+"; XFORM(LOCAL) "+xform.origin);
//			}

			if (obj.isSitting() && obj.getGravity().source != null) {
				if (EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()) {
					DebugDrawer.debugDraw(obj.getSittingPos().x, obj.getSittingPos().y, obj.getSittingPos().z, SegmentData.SEG_HALF, ghostObject.getAttached());

					DebugDrawer.debugDraw(obj.getSittingPosTo().x, obj.getSittingPosTo().y, obj.getSittingPosTo().z, SegmentData.SEG_HALF, ghostObject.getAttached());
					DebugDrawer.debugDraw(obj.getSittingPosLegs().x, obj.getSittingPosLegs().y, obj.getSittingPosLegs().z, SegmentData.SEG_HALF, ghostObject.getAttached());
				}

				Vector3f relPos = new Vector3f(obj.getSittingPos().x - SegmentData.SEG_HALF, obj.getSittingPos().y - SegmentData.SEG_HALF, obj.getSittingPos().z - SegmentData.SEG_HALF);
				switch(ghostObject.attachedOrientation) {
					case (Element.TOP) -> relPos.y += 0.36f;
					case (Element.BOTTOM) -> relPos.y -= 0.36f;
					case (Element.FRONT) -> relPos.z -= 0.36f;
					case (Element.BACK) -> relPos.z += 0.36f;
					case (Element.RIGHT) -> relPos.x -= 0.36f;
					case (Element.LEFT) -> relPos.x += 0.36f;
					default -> relPos.y += 0.36f;
				}

				Matrix3f gr = new Matrix3f(getAddRoation());
				gr.invert();
				gr.transform(relPos);
				ghostObject.localWorldTransform.origin.set(relPos);

			} else {
				ghostObject.localWorldTransform.set(matrixInv);
			}
		} else {
			if (obj.isSitting()) {
				System.err.println("[KINEMATIC][ERROR] " + obj.getState() + "; " + obj + " Sitting gravity source");
			}
		}
	}
	public boolean wasAttached = false;
	public void loadAttached(long time) {
		PairCachingGhostObjectAlignable ghostObject = (PairCachingGhostObjectAlignable) this.ghostObject;
		if(ghostObject.getAttached() != null && ghostObject.getAttached().isMarkedForDeleteVolatile()){
			System.err.println("[KINEMATIC] LOST "+obj+" ATTACHED TO :"+ghostObject.getAttached()+" BECAUSE IS WAS DELETED");
			ghostObject.setAttached(null);
		}
		boolean attached = ghostObject.getAttached() != null;
		
//		if(attached != wasAttached){
//			System.err.println("[KINEMATIC]" + obj.getState() + "; " + obj + " Attach changed to "+ghostObject.getAttached());
//		}
		
		if (attached) {
			
			ghostObject.getAttached().getPhysicsDataContainer().updatePhysical(time);

			Transform transforrmationOfAttached;

			transforrmationOfAttached = new Transform(ghostObject.getAttached().getWorldTransform());
//			if(!obj.isOnServer()){
//				System.err.println(obj.getState()+" OBJ::: "+transforrmationOfAttached.origin+"; LOCAL: "+ghostObject.localWorldTransform.origin+"; "+obj);
//			}
			if (ghostObject.getAttached().getPhysicsDataContainer().getObject() != null) {
				ghostObject.getAttached().getPhysicsDataContainer().getObject()
						.activate(true);
			}

			transforrmationOfAttached.basis.mul(getAddRoation());

			GlUtil.getRightVector(upAxisDirection[0], transforrmationOfAttached);
			GlUtil.getUpVector(upAxisDirection[1], transforrmationOfAttached);
			GlUtil.getForwardVector(upAxisDirection[2], transforrmationOfAttached);

			Matrix4f matrixWorld = transforrmationOfAttached.getMatrix(new Matrix4f());
			Matrix4f matrixLocal = ghostObject.localWorldTransform
					.getMatrix(new Matrix4f());

			Matrix4f worldPlusLocal = new Matrix4f(matrixWorld);
			worldPlusLocal.mul(matrixLocal);

			ghostObject.setWorldTransform(new Transform(worldPlusLocal));
		}
		
		wasAttached = attached;
	}

	public void breakJump(Timer timer) {
		if (verticalVelocity > 0 && jumpingStarted != -1) {
			long t = System.currentTimeMillis() - jumpingStarted;
			if (t > 80) {

				verticalVelocity = Math.max(0,
						verticalVelocity - timer.getDelta() * 50f);
				// System.err.println("BREAKING "+verticalVelocity);
			}
		} else {
			jumpingStarted = -1;
		}
	}

	public boolean isJumping() {
		// System.err.println("SLKFJLSFKJ: "+jumpingStarted);
		return jumpingStarted != -1;
	}

	public Matrix3f getAddRoation() {
		PairCachingGhostObjectAlignable ghostObject = (PairCachingGhostObjectAlignable) this.ghostObject;
		Matrix3f addRot = new Matrix3f();
		addRot.setIdentity();
		if (ghostObject.getAttached() != null) {
			getAddRot(ghostObject.attachedOrientation, addRot);

		}
		return addRot;
	}

	public Vector3f getLinearVelocity(Vector3f o) {
		o.set(velocity);
		o.scale(1000);
		// System.err.println("DDDD "+o);
		return o;
	}

	public void resetVerticalVelocity() {
		verticalVelocity = 0;
	}

	public float getVerticalVelocity() {
		return verticalVelocity;
	}

	public void setLocalJumpSpeed(float speed) {
	}

	/**
	 * Caller provides a velocity with which the character should move for the
	 * given percentage period. After the percentage period, velocity is reset
	 * to zero. This call will reset any walk direction set by
	 * {@link #setWalkDirection}. Negative percentage intervals will result in
	 * no motion.
	 */
	public void setVelocityForTimeIntervalStacked(Vector3f velocity,
	                                              float timeInterval) {
		useWalkDirection = false;
		walkDirection.add(velocity);
		normal.set(this.walkDirection);
		normal.normalize();
		normalizedDirection.set(normal);
		velocityTimeInterval += timeInterval;
		// useWalkDirection = false;
		// walkDirection.set(velocity);
		// normalizedDirection.set(getNormalizedVector(walkDirection, new
		// Vector3f()));
		// velocityTimeInterval = timeInterval;
	}

	/**
	 * This should probably be called setPositionIncrementPerSimulatorStep. This
	 * is neither a direction nor a velocity, but the amount to increment the
	 * position each simulation iteration, regardless of dt.
	 * <p/>
	 * <p/>
	 * This call will reset any velocity set by
	 * {@link #setVelocityForTimeInterval}.
	 */
	public void setWalkDirectionStacked(Vector3f walkDirection) {
		useWalkDirection = true;
		this.walkDirection.add(walkDirection);
		normal.set(this.walkDirection);
		normal.normalize();
		normalizedDirection.set(normal);
	}

	private void createCapsule() {
		float characterWidth = obj.getCharacterWidth();
		float characterHeight = extendedCharacterHeight;
		float characterMargin = 0.1f;
		capsule = new CapsuleShapeExt((SimpleTransformableSendableObject<?>) obj, characterWidth, characterHeight);
		capsule.setMargin(characterMargin);
	}

	public void stopJump() {
		verticalVelocity = 0;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bulletphysics.dynamics.character.KinematicCharacterController#
	 * updateAction(com.bulletphysics.collision.dispatch.CollisionWorld, float)
	 */
	@Override
	public void updateAction(CollisionWorld collisionWorld, float deltaTime) {

		try {
			if (obj.isHidden()) {
				if (obj.isOnServer()) {
					System.err
							.println("[SERVER] ######## EXCEPTION: Object "+obj+" was hidden. Throwing NullPointer");

					throw new NullPointerException("Hidden object "+obj);
				} else {
					System.err
							.println("[CLIENT] Kinematic ######## WARNING: Object was hidden. Maybe it just died, and is not yet removed");
					throw new NullPointerException("Hidden object "+obj);
				}
			}
			// if(!obj.isOnServer() &&
			// ((GameClientState)obj.getState()).getCharacter() == obj){
			// System.err.println(obj+" ACTION UPDATE");
			// }
			preStep(collisionWorld);
			// if(((PairCachingGhostObjectAlignable)this.ghostObject).attached
			// == null){
			playerStep(collisionWorld, deltaTime);
			// }
			obj.getPhysicsDataContainer().updatePhysical(obj.getState().getUpdateTime());
			if (!obj.isOnServer()
					&& ((GameClientState) obj.getState()).getCharacter() == obj) {
				obj.setActionUpdate(true);
			}
		} catch (NullPointerException e) {
			System.err.println("[KINEMATICCONTROLLER][UPDATEACTION] "
					+ obj.getState() + " NULLPOINTER EXCEPTION: CAUSED BY "
					+ obj + ": In sector " + obj.getSectorId() + "; hidden: "
					+ obj.isHidden());
			e.printStackTrace();
		}
	}

	@Override
	public void preStep(CollisionWorld collisionWorld) {

		loadAttached(obj.getState().getUpdateTime());

		int numPenetrationLoops = 0;

		Vector3f origin = ghostObject.getWorldTransform(ttmp).origin;
		// System.err.println("SETTING CURRENT: "+origin+" msfd "+obj.getState());
		currentPosition.set(origin);

		touchingContact = false;
		while (recoverFromPenetration(collisionWorld)) {
			numPenetrationLoops++;
			touchingContact = true;
			if (numPenetrationLoops > 4) {
				// printf("character could not recover from penetration = %d\n",
				// numPenetrationLoops);
				break;
			}
		}
		// if(numPenetrationLoops > 0){
		// System.err.println(obj+" "+numPenetrationLoops+" RECOVERED FROM PENE "+currentPosition+"; "+ghostObject);
		// }
		targetPosition.set(currentPosition);

		// printf("m_targetPosition=%f,%f,%f\n",m_targetPosition[0],m_targetPosition[1],m_targetPosition[2]);
	}

	// public SegmentController getAttached() {
	// return ((PairCachingGhostObjectExt)ghostObject).attached;
	// }
	// public void setAttached(SegmentController c) {
	//
	// System.err.println("KINEMATIC ATTACHMENT set to "+c);
	//
	// ((PairCachingGhostObjectExt)ghostObject).attached = c;
	//
	// }

	@Override
	public void playerStep(CollisionWorld collisionWorld, float dt) {

		// System.err.println("PLAYER STEP: "+dt);
		// printf("playerStep(): ");
		// printf("  dt = %f", dt);

		// System.err.println(obj.getState()+" KINEMATICS: "+walkDirection);

		
		
		// quick check...
		if (!useWalkDirection && velocityTimeInterval <= 0.0f) {
			// ghostObject.setWorldTransform(xform);
			// adaptLocalAttached();

			// printf("\n");
			return; // no motion
		}

		PairCachingGhostObjectAlignable ghostObject = (PairCachingGhostObjectAlignable) this.ghostObject;

		wasOnGround = onGround();

		// Update fall velocity.
		verticalVelocity -= gravity * dt;
		// System.err.println("Vertical velocity: " + verticalVelocity
		// +" (jSpeed)"+jumpSpeed
		// + "; gravity: " + getGravity() + ", on Ground: " + onGround());
		if (gravity == 0 && verticalVelocity > 0) {
			verticalVelocity = 0;
		}
		if (verticalVelocity > 0.0 && verticalVelocity > jumpSpeed) {
			verticalVelocity = jumpSpeed;
		}
		if (verticalVelocity < 0.0
				&& Math.abs(verticalVelocity) > Math.abs(fallSpeed)) {
			verticalVelocity = -Math.abs(fallSpeed);
		}
		verticalOffset = verticalVelocity * dt;

		ghostObject.getWorldTransform(this.xform);
		startedPosition.set(this.xform.origin);
//		if(!obj.isOnServer() && ((PairCachingGhostObjectAlignable)ghostObject).localWorldTransform != null){
//			System.err.println("STEP ::: START "+obj.getState()+" OBJ::: "+startedPosition+"; LOCAL: "+((PairCachingGhostObjectAlignable)ghostObject).localWorldTransform.origin+"; "+obj);
//		}
		this.velocity.set(this.xform.origin);
		// if(((PairCachingGhostObjectExt)ghostObject).attached != null){
		// this.xform.mul(((PairCachingGhostObjectExt)ghostObject).attached.getWorldTransform(),
		// this.xform);
		// }

		// printf("walkDirection(%f,%f,%f)\n",walkDirection[0],walkDirection[1],walkDirection[2]);
		// printf("walkSpeed=%f\n",walkSpeed);

		stepUp(collisionWorld);
		this.lastStepDown = false;
		if (useWalkDirection) {
			// System.err.println("## "+obj.getState()+" playerStep 3 "+walkDirection);
			stepForwardAndStrafe(collisionWorld, walkDirection);
		} else {
			// System.out.println("playerStep 4");
			// printf("  percentage: %f", m_velocityTimeInterval);

			// still have some percentage left for moving!
			float dtMoving = (dt < velocityTimeInterval) ? dt
					: velocityTimeInterval;
			velocityTimeInterval -= dt;

			// how far will we move while we are moving?

			move.scale(dtMoving, walkDirection);

			// printf("  dtMoving: %f", dtMoving);

			// okay, step
			stepForwardAndStrafe(collisionWorld, move);
			// System.err.println("steping: " + currentPosition + ": "
			// + walkDirection + ": " + move);
		}
		/*
		 * only needed, when with gravity
		 */
		if (gravity > 0) {
			stepDown(collisionWorld, dt);
		}

		// printf("\n");

		xform.origin.set(currentPosition);

		GlUtil.setRightVector(upAxisDirection[0], xform);
		GlUtil.setUpVector(upAxisDirection[1], xform);
		GlUtil.setForwardVector(upAxisDirection[2], xform);

		// Vector3f up = new Vector3f(, upAxisDirection[1], upAxisDirection[2]);
		// up.normalize();
		// Vector3f forward = GlUtil.getUpVector(new Vector3f(),
		// getEntity().getWorldTransform());
		// Vector3f right = new Vector3f();
		//
		// right.cross(forward, up);
		// right.normalize();
		//
		// forward.cross(up, right);
		// forward.normalize();
		//
		//

		// if(((PairCachingGhostObjectExt)ghostObject).attached != null){
		// this.xform.mul(((PairCachingGhostObjectExt)ghostObject).attached.getWorldTransformInverse(),
		// this.xform);
		// }

		ghostObject.setWorldTransform(xform);
		adaptLocalAttached();

		this.velocity.sub(this.xform.origin);
		this.velocity.negate();
		this.walkDirection.set(0, 0, 0);
		velocityTimeInterval = 0;

		endedPosition.set(this.xform.origin);
		lastActualWalkingDist.sub(endedPosition, startedPosition);
//		if(!obj.isOnServer() && ((PairCachingGhostObjectAlignable)ghostObject).localWorldTransform != null){
//			System.err.println("STEP ::: END   "+obj.getState()+" OBJ::: "+endedPosition+"; LOCAL: "+((PairCachingGhostObjectAlignable)ghostObject).localWorldTransform.origin+"; "+obj);
//		}
	}

	// // static helper method
	// private static Vector3f getNormalizedVector(Vector3f v, Vector3f out) {
	// out.set(v);
	// out.normalize();
	// if (out.length() < BulletGlobals.SIMD_EPSILON) {
	// out.set(0, 0, 0);
	// }
	// return out;
	// }

	@Override
	public void jump() {

		if (!canJump()) {

			return;
		}

		jumpingStarted = System.currentTimeMillis();
		verticalVelocity = jumpSpeed;

		// #if 0
		// currently no jumping.
		// btTransform xform;
		// m_rigidBody->getMotionState()->getWorldTransform (xform);
		// btVector3 up = xform.getBasis()[1];
		// up.normalize ();
		// btScalar magnitude = (btScalar(1.0)/m_rigidBody->getInvMass()) *
		// btScalar(8.0);
		// m_rigidBody->applyCentralImpulse (up * magnitude);
		// #endif
	}

	@Override
	protected boolean recoverFromPenetration(CollisionWorld collisionWorld) {
		//		if(!obj.isOnServer() || obj.isOnServer()){
		//			return false;
		//		}

		if (obj.isSitting() || obj.isHidden()) {
			return false;
		}
		boolean penetration = false;
		// Here we must refresh the overlapping paircache as the penetrating
		// movement itself or the
		// previous recovery iteration might have used setWorldTransform and
		// pushed us into an object
		// that is not in the previous cache contents from the last timestep, as
		// will happen if we
		// are pushed into a new AABB overlap. Unhandled this means the next
		// convex sweep gets stuck.
		//
		// Do this by calling the broadphase's setAabb with the moved AABB, this
		// will update the broadphase
		// paircache and the ghostobject's internal paircache at the same time.
		// /BW

		convexShape.getAabb(ghostObject.getWorldTransform(new Transform()),
				minAabb, maxAabb);

		if (!collisionWorld.getCollisionObjectArray().contains(ghostObject)) {
			throw new NullPointerException(obj.getState() + " Object "
					+ ghostObject + "; " + obj + " is not part of physics ("
					+ obj.getSectorId() + ") Hidden: " + obj.isHidden());
		}

		collisionWorld.getBroadphase().setAabb(
				ghostObject.getBroadphaseHandle(), minAabb, maxAabb,
				collisionWorld.getDispatcher());
		ghostObject.activate(true);

		collisionWorld.getDispatcher().dispatchAllCollisionPairs(
				ghostObject.getOverlappingPairCache(),
				collisionWorld.getDispatchInfo(),
				collisionWorld.getDispatcher());

		currentPosition.set(ghostObject.getWorldTransform(ttmp).origin);// ttmp
		// = new
		// Transform

		float maxPen = 0.0f;
		for (int i = 0; i < ghostObject.getOverlappingPairCache()
				.getNumOverlappingPairs(); i++) {
			manifoldArray.clear();

			BroadphasePair collisionPair = ghostObject
					.getOverlappingPairCache().getOverlappingPairArray()
					.getQuick(i);

			if (collisionPair.algorithm != null) {
				collisionPair.algorithm.getAllContactManifolds(manifoldArray);
			}
			for (int j = 0; j < manifoldArray.size(); j++) {
				PersistentManifold manifold = manifoldArray.getQuick(j);
				float directionSign = manifold.getBody0() == ghostObject ? -1.0f
						: 1.0f;
				CollisionObject other = (CollisionObject) (manifold.getBody0() == ghostObject ? manifold
						.getBody1() : manifold.getBody0());

				if(other == null) continue;
				if ((other.getCollisionFlags() & CollisionFlags.NO_CONTACT_RESPONSE) == CollisionFlags.NO_CONTACT_RESPONSE) {
					// System.err.println("NO CONTACT RESPONSE SHOULDBE DONE WITH "+other);
					// dont recover from something that doesn't contact response
					continue;
				}
				if(manifold.getNumContacts() > 0){
				int otherId = (obj.getId() == manifold.getContactPoint(0).starMadeIdA ? manifold.getContactPoint(0).starMadeIdB : manifold.getContactPoint(0).starMadeIdA);
				
					if (!obj.isOnServer() && 
							other instanceof RigidBodySegmentController && 
							obj.getGravity().isGravityOrAlignedOn() && 
							obj.getGravity().source.getId() != otherId) {
						System.err.println("[CLIENT][CHARACTER] We hit something: " + ((RigidBodySegmentController) other).getSegmentController() + "; contacts " + manifold.getNumContacts() + " in alignment or gravity that is not the source. Ending alignment/gravity for " + obj);
						
						obj.getGravity().differentObjectTouched = true;
						
						((GameClientState)obj.getState()).getController().popupAlertTextMessage(Lng.str("Gravity sensors detected different structure.\nPress '%s' to deactivate current gravity!", KeyboardMappings.STUCK_PROTECT.getKeyChar()), 0);
//						obj.scheduleGravity(new Vector3f(0, 0, 0), null);
					}
				}
				if (!obj.isVulnerable() && other instanceof RigidBodySegmentController && ((RigidBodySegmentController) other).getSegmentController() instanceof Ship) {

					if (((Ship) ((RigidBodySegmentController) other).getSegmentController()).isClientOwnObject()) {
						((GameClientState) obj.getState()).getController().popupAlertTextMessage(Lng.str("Collision detection is disabled for\ninvulnerable characters!"), 0);
					}
					continue;
				}

				for (int p = 0; p < manifold.getNumContacts(); p++) {
					ManifoldPoint pt = manifold.getContactPoint(p);

					float dist = pt.getDistance();
					//					 if(obj.getState() instanceof ClientStateInterface){
					//						 System.err.println("DISTANCE < 0: "+dist);
					//					 }
					if (dist < 0.1f) {

						// if(dist < -0.01){
						// System.err.println("INSIDE : "+dist+"/"+maxPen+" (#"+p+") "+pt.lifeTime+" "+obj.getState());
						// currentPosition.scaleAdd(directionSign * dist * 0.2f,
						// pt.normalWorldOnB, currentPosition);
						// }
						if (dist < maxPen) {

							maxPen = dist;
							touchingNormal.set(pt.normalWorldOnB);// ??
							touchingNormal.scale(directionSign);

							// touchingNormals.add(new
							// Vector3f(touchingNormal));

							// if(obj.getState() instanceof
							// ClientStateInterface){
							// System.err.println("TOUCHING NORMAL NOW: "+touchingNormal);
							// }
						}
//						if(!obj.isOnServer()){
//							System.err.println("CURPOSBEF:                                            "+currentPosition);
//						}
						// currentPosition.scaleAdd(directionSign * dist * 0.2f,
						// pt.normalWorldOnB, currentPosition);
						currentPosition.scaleAdd(directionSign * dist * 0.2f,
								pt.normalWorldOnB, currentPosition);
//						if(!obj.isOnServer()){
//							System.err.println("RECOVER: "+directionSign+"; dist "+dist+"; "+pt.normalWorldOnB+" ---> "+currentPosition);
//						}

						//						 if(obj.getState() instanceof ClientStateInterface){
						//						 System.err.println(collisionPair.pProxy0.clientObject+" <-> "+collisionPair.pProxy1.clientObject+"; "+p+"; DIST: "+dist+" NORMAL NOW: "+pt.normalWorldOnB+": "+currentPosition+"; A: "+pt.positionWorldOnA+"; B: "+pt.positionWorldOnB);
						//						 }

						penetration = true;

					} else {
						if (obj.getState() instanceof ClientStateInterface) {
							// System.err.println("TOUCHINGf: "+dist);
						}
						// printf("touching %f\n", dist);
					}
				}
				//WHY IN THE FUCK IS THAT MISSING IN JBULLET?!?!?
				// This basically fucks up manifolds if missing
				// to the point where stuff is drawn back to a previous
				// point of recovery
				manifold.clearManifold();
				// manifold->clearManifold();
			}
		}
		manifoldArray.clear();
		Transform newTrans = ghostObject.getWorldTransform(ttmp); // ttmp = new
		// Transform
		newTrans.origin.set(currentPosition);
		ghostObject.setWorldTransform(newTrans);
		// printf("m_touchingNormal = %f,%f,%f\n",m_touchingNormal[0],m_touchingNormal[1],m_touchingNormal[2]);

		// System.out.println("recoverFromPenetration "+penetration+" "+touchingNormal);

		return penetration;
	}
	// //////////////////////////////////////////////////////////////////////////

	@Override
	protected void stepUp(CollisionWorld world) {

		if (obj.isSitting()) {
			return;
		}
		if (obj.isOnServer()) {
			SubsimplexCubesCovexCast.mode = "UP";
		}
		// phase 1: up
		Transform start = new Transform();
		Transform end = new Transform();

		//only use step height if gravity is there,
		//else the character will float up
		//if we were on ground the last time check, do force up
		float stepHeight = (gravity > 0 && wasOnGround) ? this.stepHeight : 0.0f;

		if (gravity == 0 && lastStepDown) {
			//			System.err.println("LAST STEP DOWN: "+lastStepDown);
			stepHeight = this.stepHeight;
		}
//				if(obj instanceof PlayerCharacter){
//					System.err.println(obj.getState()+" "+obj+" STEP HEIGHT "+stepHeight+"; lsd "+lastStepDown+"; vOff: "+verticalOffset);
//				}
		targetPosition.scaleAdd(stepHeight
						+ (verticalOffset > 0.0 ? verticalOffset : 0.0f),
				upAxisDirection[upAxis], currentPosition);

		Vector3f d = new Vector3f();
		d.sub(targetPosition, currentPosition);
		//		System.err.println("fff "+d);

		//		if(targetPosition.epsilonEquals(currentPosition, BulletGlobals.SIMD_EPSILON)){
		//			return;
		//		}

		if (capsule == null) {
			createCapsule();
		}
		start.setIdentity();
		end.setIdentity();
		assert (ghostObject.getWorldTransform(new Transform())
				.getMatrix(new Matrix4f()).determinant() != 0) : ghostObject
				.getWorldTransform(new Transform()).getMatrix(new Matrix4f());
		start.basis.set(ghostObject.getWorldTransform(new Transform()).basis);
		end.basis.set(ghostObject.getWorldTransform(new Transform()).basis);
		/* FIXME: Handle penetration properly */
		// start.origin.scaleAdd(convexShape.getMargin() + addedMargin,
		// upAxisDirection[upAxis], currentPosition);
		start.origin.set(currentPosition);
		end.origin.set(targetPosition);

		// Find only sloped/flat surface hits, avoid wall and ceiling hits...
		Vector3f up = new Vector3f();
		up.scale(-1f, upAxisDirection[upAxis]);

		boolean hit = false;
		float closestHitFraction = 0;
		Segment segment = null;
		Vector3b cubePos = null;
//		if (false) {
//			Vector3f fromRay = new Vector3f(currentPosition);
//			Vector3f toRay = new Vector3f(currentPosition);
//
//			Vector3f dir = new Vector3f(upAxisDirection[upAxis]);
//			dir.negate();
//			dir.normalize();
//			dir.scale(obj.getCharacterHeight() / 2);
//			toRay.add(dir);
//
//			CubeRayCastResult ray = new CubeRayCastResult(fromRay, toRay, ghostObject, null);
//			//
//			world.rayTest(fromRay, toRay, ray);
//
//			hit = ray.hasHit();
//
//			closestHitFraction = ray.closestHitFraction;
//			segment = ray.getSegment();
//			cubePos = ray.getCubePos();
//		}
		if (!hit) {
			KinematicClosestNotMeConvexResultCallback callback = new KinematicClosestNotMeConvexResultCallback(
					ghostObject, up, 0.1f);
			callback.collisionFilterGroup = ghostObject.getBroadphaseHandle().collisionFilterGroup;
			callback.collisionFilterMask = ghostObject.getBroadphaseHandle().collisionFilterMask;

			if (useGhostObjectSweepTest) {
				ghostObject.convexSweepTest(capsule, start, end, callback,
						world.getDispatchInfo().allowedCcdPenetration);
			} else {
				world.convexSweepTest(convexShape, start, end, callback);
			}
			hit = callback.hasHit();
			closestHitFraction = callback.closestHitFraction;
			segment = callback.segment;
			cubePos = callback.cubePos;
		} else {
			System.err.println("UP RAY HAS HIT");
		}
		if (hit) {
			//			 System.err.println("HIT ON TOP");

			// we moved up only a fraction of the step height
			currentStepOffset = stepHeight * closestHitFraction;
			currentPosition.interpolate(currentPosition, targetPosition,
					closestHitFraction);
			verticalVelocity = -0.00000001f;
			verticalOffset = 0.0f;
			jumpingStarted = -1;

			if (segment != null) {
				obj.handleCollision(new SegmentPiece(segment,
						cubePos), currentPosition);
			} else {
				//				System.err
				//						.println("[KINEMATIC] UP: collision: No Segment To handle: "+closestHitFraction);
			}
		} else {
			//			if(verticalOffset > 0.0){
			//only change when there is actual up force (not by step only)
			currentStepOffset = stepHeight;
			currentPosition.set(targetPosition);
			//			}
		}
	}

	@Override
	protected void stepForwardAndStrafe(CollisionWorld collisionWorld,
	                                    Vector3f walkMove) {
		//		System.err.println("CURRENT POS: "+currentPosition+"; "+obj.getState());
		if (obj.isSitting()) {
			return;
		}
		if (obj.isOnServer()) {

			SubsimplexCubesCovexCast.mode = "FW";
		}
		// printf("m_normalizedDirection=%f,%f,%f\n",
		// m_normalizedDirection[0],m_normalizedDirection[1],m_normalizedDirection[2]);
		// phase 2: forward and strafe
		targetPosition.add(currentPosition, walkMove);
		start.setIdentity();
		end.setIdentity();
		//		GlUtil.getRightVector(upAxisDirection[0], start);
		//		GlUtil.getUpVector(upAxisDirection[1], start);
		//		GlUtil.getForwardVector(upAxisDirection[2], start);
		//
		//		GlUtil.getRightVector(upAxisDirection[0], end);
		//		GlUtil.getUpVector(upAxisDirection[1], end);
		//		GlUtil.getForwardVector(upAxisDirection[2], end);
		start.basis.set(ghostObject.getWorldTransform(new Transform()).basis);
		end.basis.set(ghostObject.getWorldTransform(new Transform()).basis);

		float fraction = 1.0f;

		distance2Vec.sub(targetPosition, currentPosition);

		if (targetPosition.epsilonEquals(currentPosition, BulletGlobals.SIMD_EPSILON)) {
			return;
		}

		float distance2 = distance2Vec.lengthSquared();

		if (distance2 == 0) {
			return;
		}

		// printf("distance2=%f\n",distance2);

		// This is causing the character to flicker in corners and at the edge two adjacent cubes
		//		if (touchingContact)
		//		{
		//			System.err.println("TOUCHING: "+touchingNormals.size());
		//			for(Vector3f n : touchingNormals){
		//
		//				if (normalizedDirection.dot(n) > 0.0f)
		//				{
		//					updateTargetPositionBasedOnCollision (n);
		//				}
		//			}
		//			penetrationCounter++;
		//		}else{
		//			touchingNormals.clear();
		//		}
		if (strafeCapsule == null) {
			float characterWidth = 0.3f;
			float characterHeight = obj.getCharacterHeight();
			float characterMargin = 0.1f;
			strafeCapsule = new CapsuleShapeExt((SimpleTransformableSendableObject<?>) obj, characterWidth, characterHeight);
			strafeCapsule.setMargin(characterMargin);
		}

		int maxIter = 10;

		boolean hit = false;
		before.set(currentPosition);
		lastStepDown = false;
		while (fraction > 0.01f && maxIter-- > 0) {

			start.origin.set(currentPosition);
			end.origin.set(targetPosition);

			KinematicClosestNotMeConvexResultCallback callback =
					new KinematicClosestNotMeConvexResultCallback(
							ghostObject, upAxisDirection[upAxis], -1.0f);

			callback.collisionFilterGroup = ghostObject
					.getBroadphaseHandle().collisionFilterGroup;
			callback.collisionFilterMask = ghostObject
					.getBroadphaseHandle().collisionFilterMask;

			float margin = convexShape.getMargin();
			convexShape.setMargin(margin + addedMargin);

			if (useGhostObjectSweepTest) {
				//collisionWorld.getDispatchInfo().allowedCcdPenetration
				ghostObject.convexSweepTest(strafeCapsule, start, end, callback,
						collisionWorld.getDispatchInfo().allowedCcdPenetration);
			} else {
				collisionWorld.convexSweepTest(convexShape, start, end,
						callback);
			}

			convexShape.setMargin(margin);

			fraction -= callback.closestHitFraction;
			if (callback.hasHit()) {

				hitDistanceVec.sub(callback.hitPointWorld, currentPosition);

				hitDistanceDownPosVec.scaleAdd(-extendedCharacterHeight / 2f, this.upAxisDirection[1], currentPosition);
				hitDistanceDownPosVec.sub(callback.hitPointWorld);

				if (hitDistanceDownPosVec.length() < 0.126f) {
					//do a small upstep when we hang on the bottom
					//					System.err.println("UP "+hitDistanceDownPosVec.length());
					lastStepDown = true;
				}

				//				start.origin.scaleAdd(convexShape.getMargin() + addedMargin, upAxisDirection[upAxis], currentPosition);

				hit = true;

				// we moved only a fraction

				hitDistanceVec.sub(callback.hitPointWorld, currentPosition);
				float hitDistance = hitDistanceVec.length();

				// if the distance is farther than the collision margin, move
				//				 if (hitDistance > addedMargin) {
				// //printf("callback.m_closestHitFraction=%f\n",callback.m_closestHitFraction);
				//				 currentPosition.interpolate(currentPosition, targetPosition,
				//				 callback.closestHitFraction);
				//				 }
				updateTargetPositionBasedOnCollision(callback.hitNormalWorld);

				currentDir.sub(targetPosition, currentPosition);
				distance2 = currentDir.lengthSquared();
				if (distance2 > BulletGlobals.SIMD_EPSILON) {
					currentDir.normalize();
					// see Quake2:
					// "If velocity is against original velocity, stop ead to avoid tiny oscilations in sloping corners."
					if (currentDir.dot(normalizedDirection) <= 0.0f) {
						if (callback.segment != null) {
							obj.handleCollision(new SegmentPiece(callback.segment, callback.cubePos), currentPosition);
						} else {
							//							System.err.println("[KINEMATIC] collision: No Segment To handle");
						}
						break;
					}
				} else {
					if (callback.segment != null) {
						obj.handleCollision(new SegmentPiece(callback.segment, callback.cubePos), currentPosition);
					} else {
						//						System.err.println("[KINEMATIC] collision: No Segment To handle");
					}
					// printf("currentDir: don't normalize a zero vector\n");
					break;
				}

			} else {
				// we moved whole way
				currentPosition.set(targetPosition);
			}

			// if (callback.m_closestHitFraction == 0.f)
			// break;
		}
		if (obj.getState() instanceof ClientStateInterface) {
			//		if(hit){
			//			System.err.println("A HIT callback ");
			//		}else{
			//			System.err.println("NO HIT callBack");
			//		}
		}
		if (after == null) {
			after = new Vector3f();
		}
		after.set(currentPosition);

	}

	@Override
	protected void stepDown(CollisionWorld collisionWorld, float dt) {

		if (obj.isSitting()) {
			return;
		}
		if (obj.isOnServer()) {
			SubsimplexCubesCovexCast.mode = "DWN";
		}
		Transform start = new Transform();
		Transform end = new Transform();
		// phase 3: down
		float additionalDownStep = (wasOnGround /* && !onGround() */) ? stepHeight
				: 0.0f;
		Vector3f step_drop = new Vector3f();
		step_drop.scale(currentStepOffset + additionalDownStep,
				upAxisDirection[upAxis]);

		float downVelocity = (additionalDownStep == 0.0f
				&& verticalVelocity < 0.0f ? -verticalVelocity : 0.0f)
				* dt;

		Vector3f gravity_drop = new Vector3f();

		gravity_drop.scale(downVelocity, upAxisDirection[upAxis]);

		//		System.err.println("DROP "+step_drop+", "+downVelocity+"; g: "+gravity_drop);

		targetPosition.sub(step_drop);
		targetPosition.sub(gravity_drop);

		start.setIdentity();
		end.setIdentity();
		// GlUtil.getRightVector(upAxisDirection[0], start);
		// GlUtil.getUpVector(upAxisDirection[1], start);
		// GlUtil.getForwardVector(upAxisDirection[2], start);
		//
		// GlUtil.getRightVector(upAxisDirection[0], end);
		// GlUtil.getUpVector(upAxisDirection[1], end);
		// GlUtil.getForwardVector(upAxisDirection[2], end);

		// System.err.println("DROPPING: "+upAxisDirection[upAxis]);

		start.basis.set(ghostObject.getWorldTransform(new Transform()).basis);
		end.basis.set(ghostObject.getWorldTransform(new Transform()).basis);

		start.origin.set(currentPosition);
		end.origin.set(targetPosition);

		if (targetPosition.epsilonEquals(currentPosition, BulletGlobals.SIMD_EPSILON)) {
			return;
		}

		float lastMargin = ghostObject.getCollisionShape().getMargin();
		// System.err.println("Reseeting margin from "+ghostObject.getCollisionShape().getMargin()+" to "+1.2f);
		// ghostObject.getCollisionShape().setMargin(0.5f);
		if (capsule == null) {
			createCapsule();
		}

		Vector3f cdir = new Vector3f();
		cdir.sub(targetPosition, currentPosition);
		if (cdir.length() == 0) {
			// if(obj.isOnServer()){
			// System.err.println("Distance from pos to target 0");
			// }
			return;
		}

		Vector3f from = new Vector3f(currentPosition);
		Vector3f to = new Vector3f(cdir);
		to.normalize();
		to.scale(0.999f);
		to.add(currentPosition);

		// if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn() && obj.isOnServer()){
		// Vector3f fromB = new Vector3f(currentPosition);
		// Vector3f toB = new Vector3f(cdir);
		// toB.normalize();
		// toB.scale(10.7f);
		// toB.add(currentPosition);
		// DebugLine dd = new DebugLine(fromB, toB, new Vector4f(1,1,0,1));
		// DebugDrawer.lines.add(dd);
		// }

		boolean hit = false;
		float closestHitFraction = 0;
		Segment segment = null;
		Vector3b cubePos = null;
//		if (false) {
//			Vector3f fromRay = new Vector3f(currentPosition);
//			Vector3f toRay = new Vector3f(currentPosition);
//
//			Vector3f dir = new Vector3f(upAxisDirection[upAxis]);
//			dir.negate();
//			dir.normalize();
//			dir.scale(obj.getCharacterHeight() / 2);
//			toRay.add(dir);
//
//			CubeRayCastResult ray = new CubeRayCastResult(fromRay, toRay, ghostObject, null);
//			//
//			collisionWorld.rayTest(fromRay, toRay, ray);
//
//			// System.err.println("HIT: "+ray.hasHit());
//			hit = ray.hasHit();
//			closestHitFraction = ray.closestHitFraction;
//			segment = ray.getSegment();
//			cubePos = ray.getCubePos();
//		}
		if (!hit) {
			KinematicClosestNotMeConvexResultCallback callback = new KinematicClosestNotMeConvexResultCallback(
					ghostObject, upAxisDirection[upAxis], maxSlopeCosine);
			callback.collisionFilterGroup = ghostObject.getBroadphaseHandle().collisionFilterGroup;
			callback.collisionFilterMask = ghostObject.getBroadphaseHandle().collisionFilterMask;
			if (useGhostObjectSweepTest) {
				ghostObject.convexSweepTest(capsule, start, end, callback,
						collisionWorld.getDispatchInfo().allowedCcdPenetration);
			} else {
				collisionWorld.convexSweepTest(capsule, start, end, callback);
			}
			hit = callback.hasHit();
			closestHitFraction = callback.closestHitFraction;
			segment = callback.segment;
			cubePos = callback.cubePos;
		} else {
			System.err.println("DOWN RAY HIT");
		}

		if (hit) {
			//			System.err.println("DOWNHIT");
			// if(!(callback.hitCollisionObject.getCollisionShape() instanceof
			// CubeShape)){
			// if(!obj.isOnServer()){
			// System.err.println("[CLIENT] GROUND HIT "+obj);
			// }else{
			// System.err.println("[SERVER] GROUND HIT "+obj);
			// }
			// }
			// we dropped a fraction of the height -> hit floor
			currentPosition.interpolate(currentPosition, targetPosition,
					closestHitFraction);
			verticalVelocity = 0.0f;
			verticalOffset = 0.0f;
			// jumpingStarted = -1;

			// if(obj.isOnServer()){
			// System.err.println("BGROUND HIT NORMAL "+verticalVelocity+"; "+verticalOffset);
			// }
			if (segment != null) {
				obj.handleCollision(new SegmentPiece(segment,
						cubePos), currentPosition);
			} else {
				//				System.err
				//						.println("Exception: Kinmatik: NO SEGMENT TO HANDLE AFTER COLLISION (LIFT?)");
			}
		} else {
			// if(obj.isOnServer()){
			// System.err.println("DROPPING FROM "+currentPosition+" TO "+targetPosition);
			// }
			if (targetPosition.epsilonEquals(currentPosition, 0.0000001f)) {
				verticalVelocity = 0.0f;
				verticalOffset = 0.0f;
				// jumpingStarted = -1;
			}
			// we dropped the full height
			currentPosition.set(targetPosition);
		}

		// System.err.println("Reseeting margin from "+ghostObject.getCollisionShape().getMargin()+" to "+lastMargin);
		ghostObject.getCollisionShape().setMargin(lastMargin);
	}

	// //////////////////////////////////////////////////////////////////////////
	private Vector3f upTmp = new Vector3f();
	private boolean checkHit(StateInterface state, Vector3f targetPosition, Vector3f up, float segmentCheckerMargin) {
		boolean hit = false;
		ObjectCollection<? extends Sendable> values;
		if (state instanceof ServerStateInterface) {
			values = state.getLocalAndRemoteObjectContainer()
					.getLocalUpdatableObjects().values();
		} else {
			values = ((GameClientState) state)
					.getCurrentSectorEntities().values();
		}
		for (Sendable s : values) {
			if (s instanceof SegmentController) {
				SegmentController c = (SegmentController) s;
				if (c.getSectorId() == obj.getSectorId()) {
					Transform t = new Transform();
					t.setIdentity();
					t.origin.set(targetPosition);

					hit = c.getCollisionChecker()
							.checkSingleSegmentController(c, t,
									segmentCheckerMargin, true);

					t.setIdentity();
					t.origin.set(targetPosition);
					upTmp.set(up);
					upTmp.scale(0.25f);
					t.origin.add(upTmp);
					
					
					hit = hit || c.getCollisionChecker()
							.checkSingleSegmentController(c, t,
									segmentCheckerMargin, true);

					t.setIdentity();
					t.origin.set(targetPosition);
					upTmp.set(up);
					upTmp.scale(-0.25f);
					t.origin.add(upTmp);

					hit = hit || c.getCollisionChecker()
							.checkSingleSegmentController(c, t,
									segmentCheckerMargin, true);
					if (hit) {

//						 System.err.println("[KIN] HIT WITH "+c+": "+c.getWorldTransform());
						break;
					}
				}
			}
		}
		return hit;
	}

	// public void warpOutOfCollision(CollisionWorld collisionWorld, Vector3f
	// walkMove, CollisionObject collisionObject, CollisionShape collisionShape,
	// Transform colObjWorldTransform) {
	public void warpOutOfCollision(StateInterface state,
	                               CollisionWorld collisionWorld, Transform from, Vector3f charUp) {
		float segmentCheckerMargin = 0.1f;

		 System.err.println("[KINEMATIC] "+obj.getState()+" WARPING OUT OF COLLISION START for "+obj+": FROM "+from.origin+"; "+ghostObject.getWorldTransform(new
		 Transform()).origin);

		warp(from.origin);

		// System.err.println("[KIN] 2 WARPING OUT OF COLLISION START for "+obj+": FROM "+from.origin+"; "+ghostObject.getWorldTransform(new
		// Transform()).origin);
		start.setIdentity();
		end.setIdentity();

		distance2Vec.sub(from.origin, targetPosition);
		Vector3f warpPos = new Vector3f();
		boolean hit = checkHit(state, from.origin, charUp, segmentCheckerMargin);

		if (!hit) {
			System.err.println("[KINEMATIC] WARP OUT DIDNT DETECT A HIT");
			return;
		}

		collisionWorld.getBroadphase().calculateOverlappingPairs(
				collisionWorld.getDispatcher());
		int direction = Element.FRONT;
		float dirCounter = 1;

		Vector3f dir = new Vector3f();
		switch(direction) {
			case (Element.FRONT) -> GlUtil.getForwardVector(dir, from);
			case (Element.BACK) -> GlUtil.getBackVector(dir, from);
			case (Element.TOP) -> GlUtil.getUpVector(dir, from);
			case (Element.BOTTOM) -> GlUtil.getBottomVector(dir, from);
			case (Element.LEFT) -> GlUtil.getLeftVector(dir, from);
			case (Element.RIGHT) -> GlUtil.getRightVector(dir, from);
		}
		int tries = 0;
		targetPosition.set(from.origin);

		// initialTest
		if (hit) {
			hit = false;
			// we moved whole way
			// warp(targetPosition);(-33.0, -27.0, 39.0) : (-31.0, -25.0, 39.0)
			// } else {
			warp(targetPosition);
			// System.err.println("[KIN] 3 WARPING OUT OF COLLISION START for "+obj+": FROM "+from.origin+"; "+ghostObject.getWorldTransform(new
			// Transform()).origin);

			hit = checkHit(state, targetPosition, charUp, segmentCheckerMargin);
			if (!hit) {
				System.err
						.println("[WARPING OT OF COLLISION][CHECK_SINGLE] NOHIT -> can spawn here!! "
								+ targetPosition+"; "+obj.getState());
			}else{
				System.err
				.println("[WARPING OT OF COLLISION][CHECK_SINGLE] HIT -> cannot spawn here!! "
						+ targetPosition);
			}
		}

		while (hit && tries < 10000) {
			start.origin.set(targetPosition);
			switch(direction) {
				case (Element.FRONT) ->
					//using Up first as that is most likely
					GlUtil.getUpVector(dir, from);
				//					System.err.println("UPPP: "+dir+" VS charUp "+charUp);
				case (Element.BACK) -> GlUtil.getBackVector(dir, from);
				case (Element.TOP) ->
					//using Up first and then forw later
					GlUtil.getForwardVector(dir, from);
				case (Element.BOTTOM) -> GlUtil.getBottomVector(dir, from);
				case (Element.LEFT) -> GlUtil.getLeftVector(dir, from);
				case (Element.RIGHT) -> GlUtil.getRightVector(dir, from);
			}
			direction = (direction + 1) % 6;

			dir.scale((1.0f + ((tries / 6)) * 0.2f));
			// System.err.println("TRYING: "+dir);
			targetPosition.add(from.origin, dir);
			end.origin.set(targetPosition);
			// for(int i = 0; i < ghostObject.getOverlappingPairs().size();
			// i++){
			// System.err.println("OO "+ghostObject.getOverlappingPairs().get(i).getCollisionShape().getClass());
			// }

			KinematicClosestNotMeConvexResultCallback callback = new KinematicClosestNotMeConvexResultCallback(
					ghostObject, upAxisDirection[upAxis], -1.0f);

			if (ghostObject == null) {
				System.err
						.println("Exception KinematikCharacter: warp out of collision ghost object "
								+ ghostObject + " is null " + obj);
				return;
			}
			if (ghostObject.getBroadphaseHandle() == null) {
				System.err
						.println("Exception KinematikCharacter: warp out of collision broadphase handle of ghost object "
								+ ghostObject + " is null " + obj);
				return;
			}
			callback.collisionFilterGroup = ghostObject.getBroadphaseHandle().collisionFilterGroup;
			callback.collisionFilterMask = ghostObject.getBroadphaseHandle().collisionFilterMask;

			warp(targetPosition);
			hit = checkHit(state, targetPosition, charUp, segmentCheckerMargin);
			if (!hit) {
				System.err
						.println("[WARPING OT OF COLLISION][CHECK WARP] NOHIT -> can spawn here!! "
								+ targetPosition+"; "+obj.getState());
			}else{
				System.err
				.println("[WARPING OT OF COLLISION][CHECK_SINGLE] HIT -> cannot spawn here!! "
						+ targetPosition);
			}

			// ghostObject.setCollisionFlags( CollisionFlags.CHARACTER_OBJECT);
			// if (callback.m_closestHitFraction == 0.f)
			// break;
			tries++;
		}
		if (tries >= 10000) {
			try {
				throw new RuntimeException(
						"Exceeded warping out of collision!!!!! " + from.origin
								+ "; " + from.basis);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * @return the lastActualWalkingDist
	 */
	public Vector3f getLastActualWalkingDist() {
		return lastActualWalkingDist;
	}

	/**
	 * @return the updateNum
	 */
	public int getUpdateNum() {
		return obj.getActionUpdateNum();
	}

	/**
	 * @param updateNum the updateNum to set
	 */
	public void setUpdateNum(int updateNum) {
		obj.setActionUpdateNum(updateNum);
	}

	@Override
	public String toString() {
		return super.toString() + "(" + obj + ")";
	}

	static class KinematicClosestNotMeConvexResultCallback extends
			CollisionWorld.ClosestConvexResultCallback {
		protected final Vector3f up;
		protected CollisionObject me;
		protected float minSlopeDot;

		Transform t = new Transform();
		private Segment segment;
		private Vector3b cubePos;

		public KinematicClosestNotMeConvexResultCallback(CollisionObject me,
		                                                 final Vector3f up, float minSlopeDot) {
			super(new Vector3f(), new Vector3f());
			this.me = me;
			this.up = up;
			this.minSlopeDot = minSlopeDot;
		}

		/* (non-Javadoc)
		 * @see com.bulletphysics.collision.dispatch.CollisionWorld.ConvexResultCallback#needsCollision(com.bulletphysics.collision.broadphase.BroadphaseProxy)
		 */
		@Override
		public boolean needsCollision(BroadphaseProxy proxy0) {

			if (proxy0.clientObject instanceof RigidDebrisBody) {
				return false;
			}
			
//			System.err.println("KIKI US::: "+collisionFilterGroup+"; "+collisionFilterMask+"; THEM "+proxy0.collisionFilterGroup+"; "+proxy0.collisionFilterMask+";; WE -> THEM "+(((proxy0.collisionFilterGroup & collisionFilterMask) & 0xFFFF) != 0)+";  THEM -> US "+(((collisionFilterGroup & proxy0.collisionFilterMask) & 0xFFFF) != 0));
			
			return super.needsCollision(proxy0);
		}

		@Override
		public float addSingleResult(
				CollisionWorld.LocalConvexResult convexResult,
				boolean normalInWorldSpace) {
			if (convexResult.hitCollisionObject == me) {
				return 1.0f;
			}

			Vector3f hitNormalWorld;
			if (normalInWorldSpace) {
				hitNormalWorld = convexResult.hitNormalLocal;
			} else {
				// need to transform normal into worldspace
				hitNormalWorld = new Vector3f();
				hitCollisionObject.getWorldTransform(t).basis.transform(
						convexResult.hitNormalLocal, hitNormalWorld);
			}

			float dotUp = up.dot(hitNormalWorld);
			if (dotUp < minSlopeDot) {
				return 1.0f;
			}

			return super.addSingleResult(convexResult, normalInWorldSpace);
		}

		public void addSingleResult(LocalConvexResult localConvexResult,
		                            boolean normalInWorldSpace, Segment segment, Vector3b elemA) {

			// if(segment.getSegmentController().isOnServer()){
			// if(segment != null && cubePos != null){
			// System.err.println("ALREADY HAD HIT: "+this.segment+": "+cubePos+": ");
			// System.err.println("---------------: "+hitPointWorld+"; "+hitNormalWorld);
			// }else{
			// System.err.println("FIRST HIT");
			// System.err.println("##################################### "+segment+": "+elemA+": ");
			// System.err.println("#####################################");
			// }
			// }
			//
			addSingleResult(localConvexResult, normalInWorldSpace);

			this.segment = segment;
			this.cubePos = new Vector3b(elemA);
		}
	}
}
