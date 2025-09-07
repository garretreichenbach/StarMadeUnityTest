/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>Physics</H2>
 * <H3>org.schema.schine.physics</H3>
 * Physics.java
 * <HR>
 * Description goes here. If you see this message, please contact me and the
 * description will be filled.<BR>
 * <BR>
 *
 * @author Robin Promesberger (schema)
 * @mail <A HREF="mailto:schemaxx@gmail.com">schemaxx@gmail.com</A>
 * @site <A
 * HREF="http://www.the-schema.com/">http://www.the-schema.com/</A>
 * @project JnJ / VIR / Project R
 * @homepage <A
 * HREF="http://www.the-schema.com/JnJ">
 * http://www.the-schema.com/JnJ</A>
 * @copyright Copyright ? 2004-2010 Robin Promesberger (schema)
 * @licence Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.schema.schine.physics;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Vector;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL15;
import org.schema.common.FastMath;
import org.schema.common.util.ByteUtil;
import org.schema.schine.graphicsengine.core.GLDebugDrawer;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.DebugBoundingBox;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;
import org.schema.schine.graphicsengine.forms.debug.DebugPoint;
import org.schema.schine.sound.controller.AudioController;

import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.BroadphasePair;
import com.bulletphysics.collision.broadphase.BroadphaseProxy;
import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.collision.broadphase.HashedOverlappingPairCache;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;
import com.bulletphysics.collision.dispatch.CollisionWorld.ConvexResultCallback;
import com.bulletphysics.collision.dispatch.CollisionWorld.LocalConvexResult;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.BvhTriangleMeshShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.collision.shapes.ShapeHull;
import com.bulletphysics.collision.shapes.TriangleIndexVertexArray;
import com.bulletphysics.dynamics.ActionInterface;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.TypedConstraint;
import com.bulletphysics.dynamics.vehicle.RaycastVehicle;
import com.bulletphysics.extras.gimpact.GImpactCollisionAlgorithm;
import com.bulletphysics.extras.gimpact.GImpactMeshShape;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;

/**
 * This class holds the Schine-org.schema.schine.graphicsengine's integration of the physics Library
 * <a href="http://jbullet.advel.cz/">jBullet</a>
 * </br></br>
 * <p/>
 * Any Entity can be automatically added with the
 * {@link Physics#addShapeFromEntity(Physical)} Method
 * </br></br>
 * The class depends on the UserData of the Models in their .scene file,
 * which can be easily edited in 3DsMax with the
 * <a href="http://www.ogremax.com">OgreMax-plugin</a>
 *
 * @author Robin Promesberger
 */
public abstract class Physics {

	/**
	 * commonly known as the earth gravitational acceleration in m/s *.
	 */
	// 9.81274f;
	public static final float PHYSICS_GRAVITY = 0;

	/**
	 * maximum number of physics objects *.
	 */
	// private static final int MAX_PROXIES = (1024);
	public static PhysicsHelperVars helpervars;

	protected static ThreadLocal<PhysicsHelperVars> threadLocal = new ThreadLocal<PhysicsHelperVars>() {

		@Override
		protected PhysicsHelperVars initialValue() {
			return new PhysicsHelperVars();
		}
	};

	/**
	 * The temp trans.
	 */
	private final Transform tempTrans = new Transform();

	/**
	 * The data constraint map.
	 */
	public HashMap<TypedConstraint, PhysicsData> dataConstraintMap = new HashMap<TypedConstraint, PhysicsData>();

	/**
	 * The dynamics world.
	 */
	protected DynamicsWorld dynamicsWorld;

	float counter;

	/**
	 * The vehicles.
	 */
	private Vector<RaycastVehicle> vehicles = new Vector<RaycastVehicle>();

	/**
	 * The overlapping pair cache.
	 */
	private BroadphaseInterface overlappingPairCache;

	/**
	 * The dispatcher.
	 */
	private CollisionDispatcher dispatcher;

	/**
	 * The solver.
	 */
	private ConstraintSolver solver;

	/**
	 * The collision configuration.
	 */
	private CollisionConfiguration collisionConfiguration;

	/**
	 * The state.
	 */
	private PhysicsState state;

	/**
	 * The shape entity map.
	 */
	private HashMap<CollisionObject, Physical> shapeEntityMap = new HashMap<CollisionObject, Physical>();

	private float iterations = 25;

	private boolean hitIndicator;

	private long time_physics_curr;

	private long time_physics_prev;

	/**
	 * Creates and initializes {@link #initPhysics()} the Physics instance.
	 *
	 * @param state the Worlds ClientState
	 */
	public Physics(PhysicsState state) {
		time_physics_prev = System.currentTimeMillis();
		time_physics_curr = System.currentTimeMillis();
		this.state = state;
		initPhysics();
	}

	public void addObject(CollisionObject object) {
		addObject(object, CollisionFilterGroups.ALL_FILTER, CollisionFilterGroups.ALL_FILTER);
	}

	public void addObject(CollisionObject object, short group, short mask) {
		if (!containsObject(object)) {
			if (object instanceof RigidBody) {
				((DiscreteDynamicsWorld) dynamicsWorld).addRigidBody((RigidBody) object, group, mask);
			} else {
				((DiscreteDynamicsWorld) dynamicsWorld).addCollisionObject(object, group, mask);
			}
		}
	}

	public void cleanUp() {
		dynamicsWorld.destroy();
	}

	public boolean containsAction(ActionInterface characterController) {
		for (int i = 0; i < dynamicsWorld.getNumActions(); i++) {
			if (dynamicsWorld.getAction(i) == characterController) {
				return true;
			}
		}
		return false;
	}

	public boolean containsObject(CollisionObject object) {
		return ((DiscreteDynamicsWorld) dynamicsWorld).getCollisionObjectArray().contains(object);
	}

	/**
	 * @param vertices          the vertices
	 * @param shiftCenterOfMass
	 * @return the Collision Shape of the HullShape
	 */
	public CollisionShape createConvexHullShapeExt(Vector3f[] vertices, Vector3f shiftCenterOfMass, boolean optimize) {
		// System.err.println("create convex hull shape");
		ObjectArrayList<Vector3f> vs = new ObjectArrayList<Vector3f>(vertices.length);
		for (Vector3f v : vertices) {
			v.add(shiftCenterOfMass);
			vs.add(v);
		}
		ConvexHullShape shape = new ConvexHullShape(vs);
		if (!optimize) {
			return shape;
		}
		// btConvexShape* originalConvexShape; is the original convexHullShape
		// create a hull approximation
		ShapeHull hull = new ShapeHull(shape);
		float margin = shape.getMargin();
		hull.buildHull(margin);
		ConvexHullShape simplifiedConvexShape = new ConvexHullShape(hull.getVertexPointer());
		simplifiedConvexShape.recalcLocalAabb();
		return simplifiedConvexShape;
	}

	/**
	 * This Function can make dynamic and static Mesh-Collision Objects from VertexBufferObjects
	 * WARNING: using dynamic Mesh Shapes can be VERY expensive. only use
	 * this if the object absolutely can'transformationArray be emulated with compound objects
	 *
	 * @param staticMesh the static mesh
	 * @return if static -> the responding BvhTriangleMeshShape
	 * if dynamic -> the responding GImpactMeshShape
	 */
	public CollisionShape createMeshShapeFromIndexBuffer(ByteBuffer vertexBuffer, int vertexCount, ByteBuffer indexBuffer, int triangleCount, boolean staticMesh) {
		int bytesPerVert = ByteUtil.SIZEOF_FLOAT * 3;
		int bytesPerTriangle = 3 * ByteUtil.SIZEOF_INT;
		// gIndices.rewind();
		// gVertices.rewind();
		TriangleIndexVertexArray array = new TriangleIndexVertexArray(triangleCount, indexBuffer, bytesPerTriangle, vertexCount, vertexBuffer, bytesPerVert);
		boolean useQuantanizedCompression = true;
		CollisionShape shape = null;
		if (staticMesh) {
			// used for static meshes. performance is OK.
			shape = new BvhTriangleMeshShape(array, useQuantanizedCompression, true);
		} else {
			// used for dynamic Meshes. expensive!
			shape = new GImpactMeshShape(array);
			/*
			 * this call is important. if this isn'transformationArray done, bullet will crash
			 * because it thinks, the bounding boxes are infinite
			 */
			((GImpactMeshShape) shape).updateBound();
		}
		return shape;
	}

	/**
	 * This Function can make dynamic and static Mesh-Collision Objects from VertexBufferObjects
	 * WARNING: using dynamic Mesh Shapes can be VERY expensive. only use
	 * this if the object absolutely can'transformationArray be emulated with compound objects
	 *
	 * @param staticMesh the static mesh
	 * @return if static -> the responding BvhTriangleMeshShape
	 * if dynamic -> the responding GImpactMeshShape
	 */
	public CollisionShape createMeshShapeFromIndexBuffer(int vertexBuffer, int indexBuffer, int indexCount, int vertexCount, boolean staticMesh) {
		/*
		 * integer has a size of 4 bytes and each vertex also needs 3 floats for
		 * x,y and z Also, order it in the way, this OS is used to (VM knows
		 * how)
		 */
		System.err.println("binding vertexBuffer to " + vertexBuffer);
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBuffer);
		ByteBuffer gVertices = null;
		GL15.glMapBuffer(GL15.GL_ARRAY_BUFFER, GL15.GL_READ_ONLY, gVertices);
		/*
		 * float has a size of 4 bytes and each face has 3 vertex indices ->
		 * numTriangles * 3 * 4 Also, order it in the way, this OS is used to
		 * (VM knows how)
		 */
		System.err.println("binding index to " + indexBuffer);
		GlUtil.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
		ByteBuffer gIndices = null;
		GL15.glMapBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, GL15.GL_READ_ONLY, gIndices);
		int vertStride = 3 * 4;
		int indexStride = 3 * 4;
		// gIndices.rewind();
		// gVertices.rewind();
		TriangleIndexVertexArray array = new TriangleIndexVertexArray(indexCount, gIndices, indexStride, vertexCount, gVertices, vertStride);
		boolean useQuantanizedCompression = true;
		CollisionShape shape = null;
		if (staticMesh) {
			// used for static meshes. performance is OK.
			shape = new BvhTriangleMeshShape(array, useQuantanizedCompression, true);
		} else {
			// used for dynamic Meshes. expensive!
			shape = new GImpactMeshShape(array);
			/*
			 * this call is important. if this isn'transformationArray done, bullet will crash
			 * because it thinks, the bounding boxes are infinite
			 */
			((GImpactMeshShape) shape).updateBound();
		}
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBuffer);
		GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);
		GlUtil.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
		GL15.glUnmapBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER);
		return shape;
	}

	/**
	 * Draws a debug representation of the {@link CollisionShape}s in the world.
	 *
	 * @param gl  the gl
	 * @param glu the glu
	 */
	public void drawDebugObjects() {
		assert (false);
		// System.err.println("drawing debug objects");
		if (dynamicsWorld == null || dynamicsWorld.getDebugDrawer() == null) {
			return;
		}
		DiscreteDynamicsWorld dw = ((DiscreteDynamicsWorld) dynamicsWorld);
		// SimpleDynamicsWorld dw = ((SimpleDynamicsWorld) getDynamicsWorld());
		int numObjects = dw.getNumCollisionObjects();
		// System.err.println("drawing "+numObjects+" objects");
		try {
			for (int i = 0; i < numObjects; i++) {
				CollisionObject colObj = dw.getCollisionObjectArray().get(i);
				if (colObj == null) {
					continue;
				}
				// RigidBody body = RigidBody.upcast(colObj);
				// if(body == null){
				// continue;
				// }
				// System.err.println("drawing "+body.getCollisionShape());
				Transform tempTrans = new Transform();
				colObj.getWorldTransform(tempTrans);
				// GLDebugDrawer.drawOpenGL( tempTrans, colObj.getCollisionShape(),
				// new Vector3f(1, 0.3f, 1), getDynamicsWorld().getDebugDrawer()
				// .getDebugMode());
				RigidBody body = RigidBody.upcast(colObj);
				if (body == null) {
					DebugPoint p = new DebugPoint(tempTrans.origin, new Vector4f(1, 1, 1, 1), 5);
					DebugDrawer.points.add(p);
				// GLDebugDrawer.drawPoint( tempTrans, 10f,
				// new Vector3f(1, 0.3f, 1) );
				} else {
					Vector3f aabbMin = new Vector3f();
					Vector3f aabbMax = new Vector3f();
					body.getAabb(aabbMin, aabbMax);
					body.getCenterOfMassTransform(tempTrans);
					DebugBoundingBox bb = new DebugBoundingBox(aabbMin, aabbMax, 1, 1, 0, 1);
					DebugDrawer.boundingBoxes.add(bb);
					DebugPoint p = new DebugPoint(tempTrans.origin, new Vector4f(1, 1, 1, 1), 5);
					DebugDrawer.points.add(p);
				// GLDebugDrawer.drawPoint( tempTrans, 10f,
				// new Vector3f(1, 0.3f, 1) );
				}
			}
			int numConstraints = dw.getNumConstraints();
			for (int i = 0; i < numConstraints; i++) {
				TypedConstraint constraint = dw.getConstraint(i);
				GLDebugDrawer.drawOpenGL(constraint, new Vector3f(1, 0, 1), dynamicsWorld.getDebugDrawer().getDebugMode());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	public abstract RigidBody getBodyFromShape(CollisionShape shape, float mass, Transform groundTransform);

	/**
	 * Gets the collision configuration.
	 *
	 * @return the collision configuration
	 */
	public CollisionConfiguration getCollisionConfiguration() {
		return collisionConfiguration;
	}

	/**
	 * Sets the collision configuration.
	 *
	 * @param collisionConfiguration the new collision configuration
	 */
	public void setCollisionConfiguration(CollisionConfiguration collisionConfiguration) {
		this.collisionConfiguration = collisionConfiguration;
	}

	/**
	 * Gets the collision point.
	 *
	 * @param position   the position
	 * @param rayTo      the ray to
	 * @param select     the select
	 * @param staticOnly the static only
	 * @return the collision point
	 */
	public Vector3f getCollisionPoint(Vector3f position, Vector3f rayTo, boolean staticOnly) {
		return testRayCollisionPoint(position, rayTo, staticOnly).hitPointWorld;
	}

	/**
	 * Gets the dispatcher.
	 *
	 * @return the dispatcher
	 */
	public CollisionDispatcher getDispatcher() {
		return dispatcher;
	}

	/**
	 * Sets the dispatcher.
	 *
	 * @param dispatcher the new dispatcher
	 */
	public void setDispatcher(CollisionDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	/**
	 * Gets the dynamics world.
	 *
	 * @return the dynamics world
	 */
	public DynamicsWorld getDynamicsWorld() {
		return dynamicsWorld;
	}

	/**
	 * Sets the dynamics world.
	 *
	 * @param dynamicsWorld the new dynamics world
	 */
	public void setDynamicsWorld(DynamicsWorld dynamicsWorld) {
		this.dynamicsWorld = dynamicsWorld;
	}

	/**
	 * @return the iterations
	 */
	public float getIterations() {
		return iterations;
	}

	/**
	 * @param iterations the iterations to set
	 */
	public void setIterations(float iterations) {
		this.iterations = iterations;
	}

	/**
	 * Gets the overlapping pair cache.
	 *
	 * @return the overlapping pair cache
	 */
	public BroadphaseInterface getOverlappingPairCache() {
		return overlappingPairCache;
	}

	/**
	 * Sets the overlapping pair cache.
	 *
	 * @param overlappingPairCache the new overlapping pair cache
	 */
	public void setOverlappingPairCache(BroadphaseInterface overlappingPairCache) {
		this.overlappingPairCache = overlappingPairCache;
	}

	/**
	 * Gets the solver.
	 *
	 * @return the solver
	 */
	public ConstraintSolver getSolver() {
		return solver;
	}

	/**
	 * Sets the solver.
	 *
	 * @param solver the new solver
	 */
	public void setSolver(ConstraintSolver solver) {
		this.solver = solver;
	}

	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	public PhysicsState getState() {
		return state;
	}

	/**
	 * Sets the state.
	 *
	 * @param state the new state
	 */
	public void setState(PhysicsState state) {
		this.state = state;
	}

	/**
	 * Gets the vehicles.
	 *
	 * @return the vehicles
	 */
	public Vector<RaycastVehicle> getVehicles() {
		return vehicles;
	}

	/**
	 * Sets the vehicles.
	 *
	 * @param vehicles the new vehicles
	 */
	public void setVehicles(Vector<RaycastVehicle> vehicles) {
		this.vehicles = vehicles;
	}

	/**
	 * this method initializes the basic functionality of jBullet
	 * it sets the gravity and adds a static box as the ground.
	 */
	public void initPhysics() {
		// collision configuration contains default setup for memory, collision
		// setup
		collisionConfiguration = new DefaultCollisionConfiguration();
		// use the default collision dispatcher. For parallel processing you can
		// use a diffent dispatcher (see Extras/BulletMultiThreaded)
		dispatcher = new CollisionDispatcher(collisionConfiguration);
		// the maximum size of the collision world. Make sure objects stay
		// within these boundaries
		// TODO: AxisSweep3
		// Don'transformationArray make the world AABB size too large, it will harm simulation
		// quality and performance
		Vector3f worldAabbMin = new Vector3f(-10000, -10000, -10000);
		Vector3f worldAabbMax = new Vector3f(10000, 10000, 10000);
		overlappingPairCache = new AxisSweep3(worldAabbMin, worldAabbMax, 1024, new HashedOverlappingPairCache());
		// overlappingPairCache = new SimpleBroadphase(MAX_PROXIES);
		// the default constraint solver. For parallel processing you can use a
		// different solver (see Extras/BulletMultiThreaded)
		SequentialImpulseConstraintSolver sol = new SequentialImpulseConstraintSolver();
		solver = sol;
		// TODO: needed for SimpleDynamicsWorld
		// sol.setSolverMode(sol.getSolverMode() &
		// ~SolverMode.SOLVER_CACHE_FRIENDLY.getMask());
		dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, overlappingPairCache, solver, collisionConfiguration);
		// dynamicsWorld = new SimpleDynamicsWorld(dispatcher,
		// overlappingPairCache, solver, collisionConfiguration);
		CollisionDispatcher dispatcher = (CollisionDispatcher) dynamicsWorld.getDispatcher();
		GImpactCollisionAlgorithm.registerAlgorithm(dispatcher);
		dynamicsWorld.setGravity(new Vector3f(0f, -PHYSICS_GRAVITY, 0f));
	// create a few basic rigid bodies
	// addGround();
	}

	public void orientate(Physical entity, Vector3f toDirectionForward, Vector3f toDirectionUp, Vector3f toDirectionRight, float oForceX, float oForceY, float oForceZ, float timer) {
		RigidBody body = (RigidBody) entity.getPhysicsDataContainer().getObject();
		if (body.getCollisionFlags() == CollisionFlags.STATIC_OBJECT || entity.getMass() <= 0) {
			return;
		}
		if (toDirectionForward != null && toDirectionForward.lengthSquared() > 0 && toDirectionUp != null && toDirectionUp.lengthSquared() > 0) {
			PhysicsHelperVars h = threadLocal.get();
			// get the transform matrix for this mesh that corresponds to that
			// game entity
			Transform currentTranform = entity.getPhysicsDataContainer().getCurrentPhysicsTransform();
			// this entities forward Vector
			GlUtil.getForwardVector(h.currentForward, currentTranform);
			GlUtil.getUpVector(h.currentUp, currentTranform);
			GlUtil.getRightVector(h.currentRight, currentTranform);
			h.toRight.set(toDirectionRight);
			h.toRight.normalize();
			h.toForward.set(toDirectionForward);
			h.toForward.normalize();
			h.toUp.set(toDirectionUp);
			h.toUp.normalize();
			Vector3f distForw = new Vector3f();
			Vector3f distUp = new Vector3f();
			Vector3f distRight = new Vector3f();
			distForw.sub(h.currentForward, h.toForward);
			distUp.sub(h.currentUp, h.toUp);
			distRight.sub(h.currentRight, h.toRight);
			h.axisYaw.cross(h.currentForward, h.toForward);
			h.axisYaw.normalize();
			h.axisRoll.cross(h.currentUp, h.toUp);
			h.axisRoll.normalize();
			h.axisPitch.cross(h.currentRight, h.toRight);
			h.axisPitch.normalize();
			if (distForw.length() < FastMath.FLT_EPSILON && distUp.length() < FastMath.FLT_EPSILON && distRight.length() < FastMath.FLT_EPSILON) {
				return;
			}
			h.axisYaw.scale(distForw.length());
			h.axisRoll.scale(distUp.length());
			h.axisPitch.scale(distRight.length());
			// System.err.println("aFo: "+h.axisFo.length()+"; aUp "+h.axisUp.length());
			h.axis.add(h.axisYaw, h.axisRoll);
			h.axis.add(h.axisPitch);
			// if body is sleepy sleepy
			if (h.axis.lengthSquared() > 0 && h.axis.length() > 0.00005f && !body.isActive()) {
				// System.err.println("ACIVATE");
				body.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(32);
			}
			Vector3f currentAngularVelocity = new Vector3f();
			body.getAngularVelocity(currentAngularVelocity);
			// axis.add(currentAngularVelocity);
			h.axis.scale(oForceX);
			// if(h.lastAxis.length() > 0){
			// if(
			// Math.signum(h.axis.x) != Math.signum(h.lastAxis.x) &&
			// Math.signum(h.axis.y) != Math.signum(h.lastAxis.y) &&
			// Math.signum(h.axis.z) != Math.signum(h.lastAxis.z)){
			// System.err.println("COMPLEEEEEEEEEEEEEEEEETE FLICKER ");
			// }
			// }
			// body.setAngularVelocity(h.axis);
			if (!Float.isNaN(h.axis.x) && !Float.isNaN(h.axis.y) && !Float.isNaN(h.axis.z)) {
				body.setAngularVelocity(h.axis);
			// body.applyTorqueImpulse(h.axis);
			} else {
			// System.err.println("NOT SETTING orientation NaN: "+h.axis);
			}
			h.lastAxis.set(h.axis);
		// linVel.scale(force);// (float)(FastMath.nextRandomFloat()*10));
		// apply linear force to body
		// body.setLinearVelocity(linVel);
		// System.err.println("shot "+body+" with "+linVel+", sleepy: "+body.wantsSleeping()+", active: "+body.isActive());
		// no angular impulse
		// body.setAngularVelocity(new Vector3f(0f, 0f, 0f));
		} else {
		// System.err.println("NO ORIENTATIONS");
		}
	}

	public String physicsSlowMsg() {
		return state.getPhysicsSlowMsg();
	}

	public void removeObject(CollisionObject object) {
		if (dynamicsWorld.getCollisionObjectArray().contains(object)) {
			if (object instanceof RigidBody) {
				dynamicsWorld.removeRigidBody((RigidBody) object);
			} else {
				dynamicsWorld.removeCollisionObject(object);
			}
		// System.err.println("REMOVED PHYSICS OBJECT: "+object);
		} else {
		// System.err.println("COULD NOT REMOVE OBEJCT FROM PHYSICS: "+object);
		}
	}

	/**
	 * this Method removes a GameEntity from the physics world completely.
	 *
	 * @param n the {@link Physical} to remove
	 */
	public void removeShapeOfEntity(Physical n) {
		// System.err.println("removing physics shape of "+n);
		if (n.getPhysicsDataContainer().getObject() == null) {
			return;
		}
		CollisionObject obj = n.getPhysicsDataContainer().getObject();
		// System.err.println("removing body for "+n);
		if (obj instanceof RigidBody) {
			dynamicsWorld.removeRigidBody((RigidBody) obj);
		} else {
			dynamicsWorld.removeCollisionObject(obj);
		}
		shapeEntityMap.remove(obj);
		// remove all constraints, that are connected to that body
		for (int i = 0; i < dynamicsWorld.getNumConstraints(); i++) {
			TypedConstraint tc = dynamicsWorld.getConstraint(i);
			if (tc.getRigidBodyA() == obj || tc.getRigidBodyB() == obj) {
				dynamicsWorld.removeConstraint(tc);
			}
		}
		// remove all vehicles attached to this entity
		// for(RaycastVehicle v : n.getPhysicsDataContainer().physicsVehicleMap.keySet()){
		// dynamicsWorld.removeVehicle(v);
		// this.vehicles.remove(v);
		// }
		n.getPhysicsDataContainer().clearPhysicsInfo();
	}

	/**
	 * Reset all.
	 */
	public void resetAll() {
		System.err.println("[PHYSICS] TOTAL RESET!");
		for (int i = 0; i < dynamicsWorld.getCollisionObjectArray().size(); i++) {
			dynamicsWorld.removeCollisionObject(dynamicsWorld.getCollisionObjectArray().get(i));
		}
		for (int i = 0; i < dynamicsWorld.getNumConstraints(); i++) {
			dynamicsWorld.removeConstraint(dynamicsWorld.getConstraint(i));
		}
		for (RaycastVehicle vehicle : vehicles) {
			dynamicsWorld.removeVehicle(vehicle);
		}
		vehicles.clear();
		shapeEntityMap.clear();
		dataConstraintMap.clear();
		initPhysics();
	}

	/**
	 * shoots any GameEntity with the given force in the
	 * given direction.
	 *
	 * @param gameEntity         the game entity
	 * @param force              the force
	 * @param destination        the destination
	 * @param addToExistingForce
	 */
	public void shoot(Physical gameEntity, float force, Vector3f destination, boolean addToExistingForce) {
		RigidBody body = (RigidBody) gameEntity.getPhysicsDataContainer().getObject();
		// System.err.println("shooting "+body+" "+destination);
		// if(body.getCollisionShape() instanceof CompoundShape){
		// CompoundShape cs = (CompoundShape)body.getCollisionShape();
		// for(int i = 0; i < cs.getNumChildShapes(); i++){
		// cs.getChildShape(i);
		// }
		// }else{
		shoot(body, force, destination, addToExistingForce);
	// }
	}

	public void shoot(RigidBody body, float force, Vector3f destination, boolean addToExistingForce) {
		// System.err.println("shooting "+body+" "+destination);
		if (destination != null && destination.length() > 0) {
			Vector3f linVel = new Vector3f(destination);
			// if body is sleepy sleepy
			// if(!body.isActive()){
			// body.activate(true);
			// }
			linVel.normalize();
			// (float)(FastMath.nextRandomFloat()*10));
			linVel.scale(force);
			// 
			// if(addToExistingForce){
			// Vector3f existing = new Vector3f();
			// body.getLinearVelocity(existing);
			// linVel.add(existing);
			// }
			// System.err.println("ACTIVEBEFORE: "+body.isActive());
			// body.forceActivationState(CollisionObject.ACTIVE_TAG);
			// System.err.println("ACTIVE: "+body.isActive()+", "+body.getActivationState());
			// destination.normalize();
			// destination.scale(10);
			// body.applyCentralForce(new Vector3f(destination));
			// body.applyCentralImpulse(new Vector3f(destination));
			// Vector3f vel = new Vector3f();
			// body.getLinearVelocity(vel);
			// body.applyForce(linVel, destination);
			body.setLinearVelocity(linVel);
		// System.err.println("SHOOTING: "+body.getCollisionShape()+", "+destination+", "+body.getInvMass()+", linVelo "+body.getLinearVelocity(new Vector3f()));
		// System.err.println("SHOOTING: "+body.getWorldTransform(new Transform()).origin+", ");
		// apply linear force to body
		// System.err.println("shot "+body+" with "+linVel+", sleepy: "+body.wantsSleeping()+", active: "+body.isActive());
		// no angular impulse
		// body.setAngularVelocity(new Vector3f(0f, 0f, 0f));
		}
	}

	public void softClean() {
	}

	public void stopAngular(Physical entity) {
		RigidBody body = (RigidBody) entity.getPhysicsDataContainer().getObject();
		body.setAngularVelocity(new Vector3f(0, 0, 0));
	}

	public void stopForces(Physical gameEntity) {
		RigidBody body = (RigidBody) gameEntity.getPhysicsDataContainer().getObject();
		assert (body != null);
		if (!body.isActive()) {
			body.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(33);
		}
		body.clearForces();
		body.setLinearVelocity(new Vector3f(0, 0, 0));
		body.setAngularVelocity(new Vector3f(0, 0, 0));
	}

	public void testOverlapAABB(PairCachingGhostObject ghost) {
		System.err.println("overlapping: " + ghost.getNumOverlappingObjects() + ", " + ghost.getActivationState());
		ObjectArrayList<BroadphasePair> pairs = ghost.getOverlappingPairCache().getOverlappingPairArray();
		for (int i = 0; i < pairs.size(); ++i) {
			final BroadphasePair pair = pairs.get(i);
			BroadphaseProxy proxy = pair.pProxy0.clientObject != ghost ? pair.pProxy0 : pair.pProxy1;
			CollisionObject obj = (CollisionObject) proxy.clientObject;
			// Now you have one object. Do something here
			System.err.println("overlapping: " + obj);
		}
	// dynamicsWorld.removeCollisionObject(ghost);
	}

	public ClosestRayResultCallback testRayCollisionPoint(Vector3f position, Vector3f rayTo, boolean staticOnly) {
		CollisionWorld.ClosestRayResultCallback rayCallback = new CollisionWorld.ClosestRayResultCallback(position, rayTo);
		dynamicsWorld.rayTest(position, rayTo, rayCallback);
		if (rayCallback.collisionObject != null) {
			RigidBody body = RigidBody.upcast(rayCallback.collisionObject);
			if (body != null) {
				if (staticOnly && !body.isStaticObject()) {
					return null;
				}
			}
		}
		return rayCallback;
	}

	public void testSweepBoundingBox(Vector3f from, Vector3f to, Vector3f halfSize) {
		BoxShape box = new BoxShape(halfSize);
		// convexSweepTest(ConvexShape castShape, Transform convexFromWorld, Transform convexToWorld, CollisionWorld.ConvexResultCallback resultCallback, float allowedCcdPenetration)
		Transform convexFromWorld = new Transform();
		convexFromWorld.setIdentity();
		convexFromWorld.origin.set(from);
		Transform convexToWorld = new Transform();
		convexToWorld.setIdentity();
		convexToWorld.origin.set(to);
		hitIndicator = false;
		ConvexResultCallback callBack = new ConvexResultCallback() {

			@Override
			public float addSingleResult(LocalConvexResult convexResult, boolean normalInWorldSpace) {
				hitIndicator = true;
				System.err.println("hitIndicator: " + hitIndicator + " " + convexResult.hitCollisionObject);
				return 0;
			}
		};
		box.recalcLocalAabb();
		dynamicsWorld.convexSweepTest(box, convexFromWorld, convexToWorld, callBack);
		// go.convexSweepTest(box, convexFromWorld, convexToWorld, callBack, 0);
		System.err.println("tested bb: " + hitIndicator + ", callback: has hit " + callBack.hasHit());
	}

	/**
	 * this method will update all physics objects in the scene.
	 *
	 * @param f
	 */
	public void update(Timer timer, float highestSubStep) {
		assert (state != null);
		// counter += 100f/frameTime;
		// if(counter < 1){
		// }else{
		counter = 0;
		// float dt = getDeltaTimeMicroseconds() * 0.00001f;
		// step the simulation
		try {
			/*
			 * The first and third parameters to stepSimulation are measured in
			 * seconds, and not milliseconds. A common and easy mistake is to
			 * just pass it the value returned by your system's
			 * getTime-equivalent function, which commonly returns time in
			 * milliseconds.
			 *
			 * This mistake can give strange results such as: No framerate
			 * Dependence no matter what you do. Objects not moving at all until
			 * you apply a huge force and then they give huge acceleration.
			 *
			 * Simply divide the time by 1000.0f before passing it to
			 * stepSimulation.
			 */
			// if(getState().toString().contains("(8, 8, 5)")){
			// System.err.println("PHYSICS UPDATE ON "+getState().toString());
			// }
			// if(getState() instanceof ClientState){
			// System.err.println("PHYSICS "+getState()+": ACTIONS: "+getDynamicsWorld().getNumActions()+"; OBJECTS: "+getDynamicsWorld().getNumCollisionObjects());
			// //				for(int i = 0; i < getDynamicsWorld().getNumCollisionObjects(); i++){
			// //					System.err.println("POBJ: "+getDynamicsWorld().getCollisionObjectArray().getQuick(i));
			// //				}
			// }
			time_physics_curr = System.currentTimeMillis();
			int stepSimulation = dynamicsWorld.stepSimulation((time_physics_curr - time_physics_prev) / 1000.0f, 14);
			time_physics_prev = time_physics_curr;
		// if(state instanceof ClientStateInterface && EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()){
		// drawDebugObjects();
		// }
		} catch (Exception e) {
			e.printStackTrace();
			assert (state != null);
			System.err.println("Exception: PHYSICS EXCEPTION HAS BEEN CATCHED! " + state.toStringDebug());
		}
	// }
	// int numObjects = dynamicsWorld.getNumCollisionObjects();
	// for (int i = 0; i < numObjects; i++) {
	// 
	// // Collision Object is super class of RigidBody
	// CollisionObject colObj = dynamicsWorld.getCollisionObjectArray()
	// .get(i);
	// if(state instanceof ServerStateInterface){
	// if (colObj == null) {
	// continue;
	// }
	// 
	// // safe casting
	// RigidBody body = RigidBody.upcast(colObj);
	// 
	// if(body != null){
	// if (body.getMotionState() != null) {
	// DefaultMotionState myMotionState = (DefaultMotionState) body
	// .getMotionState();
	// tempTrans.set(myMotionState.graphicsWorldTrans);
	// } else {
	// colObj.getWorldTransform(tempTrans);
	// }
	// }
	// if(body != null){
	// System.err.println("body "+body+" now has "+tempTrans.origin);
	// }else{
	// System.err.println("body null: "+colObj);
	// }
	// }
	// }
	/*
		for (RaycastVehicle vehicle : getVehicles()) {
			VehicleData physicsData = (VehicleData) dataVehicleMap.get(vehicle);
			Physical gameEntity = physicsData.getEntity();


			for (int i = 0; i < vehicle.getNumWheels(); i++) {
				WheelInfo wheelInfo = vehicle.getWheelInfo(i);

				if (wheelInfo.bIsFrontWheel) {
					vehicle.setSteeringValue(physicsData.getSteering(), i);
				} else {
					vehicle.applyEngineForce(physicsData.getEngineForce(), i);
					vehicle.setBrake(physicsData.getBreakingForce(), i);
				}

				vehicle.updateWheelTransform(i, true);
				Transform wheelWorldTransform = wheelInfo.worldTransform;
			}
//			vehicle.getChassisWorldTransform(gameEntity.transform);
		}

		 */
	}

	public void onOrientateOnly(Physical entity, float timeStep) {
	}
}
