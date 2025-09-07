package org.schema.game.common.controller;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.physics.octree.IntersectionCallback;
import org.schema.schine.graphicsengine.forms.BoundingBox;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.ManifoldResult;
import com.bulletphysics.collision.narrowphase.DiscreteCollisionDetectorInterface.ClosestPointInput;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.linearmath.Transform;

public class SegmentControllerElementCollisionCheckerVariables {

	static final BoxShape boxGhostObject = new BoxShape(new Vector3f(0.5f,
			0.5f, 0.5f));
	static final BoxShape boxGhostObjectSm = new BoxShape(new Vector3f(0.4999f,
			0.4999f, 0.4999f));
	public final Vector3f tmpMinHelp = new Vector3f();
	public final Vector3f tmpMaxHelp = new Vector3f();
	final Vector3f tmpMinA = new Vector3f();
	final Vector3f tmpMaxA = new Vector3f();
	final Vector3f tmpMinB = new Vector3f();
	final Vector3f tmpMaxB = new Vector3f();
	final Vector3f tmpMinC = new Vector3f();
	final Vector3f tmpMaxC = new Vector3f();
	final Vector3f ctmpMinA = new Vector3f();
	final Vector3f ctmpMaxA = new Vector3f();
	final Vector3f nA = new Vector3f();
	final Vector3b start = new Vector3b();
	final Vector3b end = new Vector3b();
	final Vector3b elemA = new Vector3b();
	final Transform tmpTrans = new Transform();
	final Vector3f elemPosB = new Vector3f();
	final IntersectionCallback intersectionCallBack = new IntersectionCallback();
	final Vector3b tmpLocalPos = new Vector3b();
	final Matrix3f absolute = new Matrix3f();
	final Vector3i tmpAbsPos = new Vector3i();
	public Vector3f elemPosA = new Vector3f();
	public ClosestPointInput input = new ClosestPointInput();
	public ManifoldResult output = new ManifoldResult();
	public Transform ownPos = new Transform();
	public CollisionObject col0 = new CollisionObject();
	public CollisionObject col1 = new CollisionObject();
	public PersistentManifold m = new PersistentManifold();
	public Vector3f closest = new Vector3f();
	public BoundingBox b = new BoundingBox();
}
