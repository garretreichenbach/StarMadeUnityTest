package org.schema.game.common.data.physics;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.linearmath.Transform;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.physics.octree.IntersectionCallback;
import org.schema.game.common.data.physics.octree.OctreeVariableSet;
import org.schema.game.common.data.physics.shape.DodecahedronShapeExt;
import org.schema.schine.graphicsengine.forms.BoundingBox;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

public class CubeConvexVariableSet {

	public final boolean[][] dodecaOverlap = new boolean[12][6];
	public int cubeCallbackPointer;
	
	public Vector3f nA = new Vector3f();
	public Transform boxETransform = new Transform();
	public Transform cubeMeshTransform = new Transform();
	public Transform convexShapeViewFromCubes = new Transform();
	public Transform cubeShapeTransformInv = new Transform();
	public Transform convexShapeTransform = new Transform();
	public Transform boxTransformation = new Transform();
	public Vector3f tmp = new Vector3f();
	public Vector3f pos0 = new Vector3f();
	public Vector3f pos1 = new Vector3f();
	public Vector3f diff = new Vector3f();
	public Vector3f normalOnSurfaceB = new Vector3f();
	public BoundingBox outer = new BoundingBox();
	// Vector3f normalOnSurfaceA = new Vector3f();
	public BoundingBox inner = new BoundingBox();
	public BoundingBox outBB = new BoundingBox();
	public Vector3i minIntA = new Vector3i();
	public Vector3i maxIntA = new Vector3i();
	public Vector3i minIntB = new Vector3i();
	public Vector3i maxIntB = new Vector3i();
	public Vector3f min = new Vector3f();
	public Vector3f max = new Vector3f();
	public Vector3f minOut = new Vector3f();
	public Vector3f maxOut = new Vector3f();
	public Vector3f localMinOut = new Vector3f();
	public Vector3f localMaxOut = new Vector3f();
	public Vector3f otherminOut = new Vector3f();
	public Vector3f othermaxOut = new Vector3f();
	public Vector3f elemPosA = new Vector3f();
	public Vector3f elemPosB = new Vector3f();
	public Vector3f hitMin = new Vector3f();
	public Vector3f hitMax = new Vector3f();
	public Vector3b startA = new Vector3b();
	public Vector3b endA = new Vector3b();
	public Vector3f dist = new Vector3f();
	//	BoxShape box0 = new BoxShape(new Vector3f(
	//			Element.HALF_SIZE / 2f + 0.0f,
	//			Element.HALF_SIZE / 2f + 0.0f,
	//			Element.HALF_SIZE / 2f + 0.0f));
	public BoxShape box0 = new BoxShape(new Vector3f(
			0.5f + 0.06f,
			0.5f + 0.06f,
			0.5f + 0.06f));
	public BoxShape[] box34 = new BoxShape[]{
			new BoxShape(new Vector3f(
					0.5f + 0.06f,
					0.5f + 0.06f,
					0.375f + 0.06f)),
			new BoxShape(new Vector3f(
					0.5f + 0.06f,
					0.5f + 0.06f,
					0.375f + 0.06f)),
			new BoxShape(new Vector3f(
					0.5f + 0.06f,
					0.375f + 0.06f,
					0.5f + 0.06f)),
			new BoxShape(new Vector3f(
					0.5f + 0.06f,
					0.375f + 0.06f,
					0.5f + 0.06f)),
			new BoxShape(new Vector3f(
					0.375f + 0.06f,
					0.5f + 0.06f,
					0.5f + 0.06f)),
			new BoxShape(new Vector3f(
					0.375f + 0.06f,
					0.5f + 0.06f,
					0.5f + 0.06f)),
			
	};
	public BoxShape[] box12 = new BoxShape[]{
			new BoxShape(new Vector3f(
					0.5f + 0.06f,
					0.5f + 0.06f,
					0.25f + 0.06f)),
			new BoxShape(new Vector3f(
					0.5f + 0.06f,
					0.5f + 0.06f,
					0.25f + 0.06f)),
			new BoxShape(new Vector3f(
					0.5f + 0.06f,
					0.25f + 0.06f,
					0.5f + 0.06f)),
			new BoxShape(new Vector3f(
					0.5f + 0.06f,
					0.25f + 0.06f,
					0.5f + 0.06f)),
			new BoxShape(new Vector3f(
					0.25f + 0.06f,
					0.5f + 0.06f,
					0.5f + 0.06f)),
			new BoxShape(new Vector3f(
					0.25f + 0.06f,
					0.5f + 0.06f,
					0.5f + 0.06f)),
			
	};
	public BoxShape[] box14 = new BoxShape[]{
			new BoxShape(new Vector3f(
					0.5f + 0.06f,
					0.5f + 0.06f,
					0.125f + 0.06f)),
			new BoxShape(new Vector3f(
					0.5f + 0.06f,
					0.5f + 0.06f,
					0.125f + 0.06f)),
			new BoxShape(new Vector3f(
					0.5f + 0.06f,
					0.125f + 0.06f,
					0.5f + 0.06f)),
			new BoxShape(new Vector3f(
					0.5f + 0.06f,
					0.125f + 0.06f,
					0.5f + 0.06f)),
			new BoxShape(new Vector3f(
					0.125f + 0.06f,
					0.5f + 0.06f,
					0.5f + 0.06f)),
			new BoxShape(new Vector3f(
					0.125f + 0.06f,
					0.5f + 0.06f,
					0.5f + 0.06f)),
			
	};
	public IntersectionCallback intersectionCallBackAwithB = new IntersectionCallback();
	public Vector3f shapeMin = new Vector3f();
	public Vector3f shapeMax = new Vector3f();
	public Vector3f outMin = new Vector3f();
	public Vector3f outMax = new Vector3f();
	public Matrix3f absolute = new Matrix3f();
	public OctreeVariableSet oSet;
	public GjkPairDetectorVariables gjkVars = new GjkPairDetectorVariables();
	public Transform inv = new Transform();
	public AABBVarSet aabbVarSet = new AABBVarSet();
	public CubeShape cubesShape0;
	public ConvexShape convexShape;
	public DodecahedronShapeExt dodecahedron;
	public Vector3f closest = new Vector3f();
	public Vector3f orientTT = new Vector3f();
	public Transform BT = new Transform();
	public Vector3f blockA = new Vector3f();
	public Vector3f blockB = new Vector3f();
	public Transform lodBlockTransform = new Transform();
	public CubeConvexVariableSet() {
		super();

	}
}
