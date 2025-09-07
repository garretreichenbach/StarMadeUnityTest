package org.schema.game.common.data.physics;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.physics.octree.IntersectionCallback;
import org.schema.schine.graphicsengine.forms.BoundingBox;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.narrowphase.SimplexSolverInterface;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.floats.Float2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;

public class CubeConvexCastVariableSet {

	public Matrix3f absolute1 = new Matrix3f();
	public Vector3f dist = new Vector3f();
	public GjkPairDetectorVariables gjkVar = new GjkPairDetectorVariables();
	public GjkEpaPenetrationDepthSolverExt gjkEpaPenetrationDepthSolver = new GjkEpaPenetrationDepthSolverExt();
	public Transform inv = new Transform();
	public AABBVarSet aabbVarSet = new AABBVarSet();
	Vector3f outMin = new Vector3f();
	Vector3f outMax = new Vector3f();
	Vector3f fromHelp = new Vector3f();
	Vector3f toHelp = new Vector3f();
	Vector3f fromToHelp = new Vector3f();
	Vector3f normal = new Vector3f();
	Vector3f localMinOut = new Vector3f();
	Vector3f localMaxOut = new Vector3f();
	float[] hitLambda = new float[1];
	IntersectionCallback intersectionCallBack = new IntersectionCallback();
	BoundingBox outer = new BoundingBox();
	BoundingBox inner = new BoundingBox();
	BoundingBox outBB = new BoundingBox();
	BoundingBox localOutBB = new BoundingBox();
	Vector3i minBlockA = new Vector3i();
	Vector3i maxBlockA = new Vector3i();
	Vector3i minIntA = new Vector3i();
	Vector3i maxIntA = new Vector3i();
	ChangableSphereShape sphereShape = new ChangableSphereShape(0);
	CollisionObject cubesObject;
	Transform f = new Transform();
	Transform t = new Transform();
	Float2ObjectAVLTreeMap<AABBb> sortedAABB = new Float2ObjectAVLTreeMap<AABBb>();
	Vector3b elemA = new Vector3b();
	Vector3f elemPosA = new Vector3f();
	Vector3b startOut = new Vector3b();
	Vector3b endOut = new Vector3b();
	Vector3f minOut = new Vector3f();
	Vector3f maxOut = new Vector3f();
	Vector3f nA = new Vector3f();
	Transform tmpCubesTrans = new Transform();
	Vector3f distTest = new Vector3f();
	ObjectRBTreeSet<SortEntry> sorted = new ObjectRBTreeSet<SortEntry>();
	Vector3b[] posCache = new Vector3b[8];
	int posCachePointer = 0;
	Vector3f castedAABBMin = new Vector3f();
	Vector3f castedAABBMax = new Vector3f();
	Vector3f convexFromAABBMin = new Vector3f();
	Vector3f convexFromAABBMax = new Vector3f();
	Vector3f convexToAABBMin = new Vector3f();
	Vector3f convexToAABBMax = new Vector3f();
	Vector3f tmp = new Vector3f();
	Transform boxETransform = new Transform();
	Transform tmpTrans1 = new Transform();
	Transform tmpTrans2 = new Transform();
	Transform tmpTrans3 = new Transform();
	Transform tmpTrans4 = new Transform();
	boolean disableCcd = false;
	SegmentPiece tmpPice = new SegmentPiece();
	Vector3i absPos = new Vector3i();
	BoxShape box0 = new BoxShape(new Vector3f(Element.BLOCK_SIZE / 2f + 0.001f,
			Element.BLOCK_SIZE / 2f + 0.001f, Element.BLOCK_SIZE / 2f + 0.001f));
	public Vector3f orientTT = new Vector3f();
	public Transform BT = new Transform();
	public BoxShape[] box34 = new BoxShape[]{
			new BoxShape(new Vector3f(
					0.5f + 0.001f,
					0.5f + 0.001f,
					0.375f + 0.001f)),
			new BoxShape(new Vector3f(
					0.5f + 0.001f,
					0.5f + 0.001f,
					0.375f + 0.001f)),
			new BoxShape(new Vector3f(
					0.5f + 0.001f,
					0.375f + 0.001f,
					0.5f + 0.001f)),
			new BoxShape(new Vector3f(
					0.5f + 0.001f,
					0.375f + 0.001f,
					0.5f + 0.001f)),
			new BoxShape(new Vector3f(
					0.375f + 0.001f,
					0.5f + 0.001f,
					0.5f + 0.001f)),
			new BoxShape(new Vector3f(
					0.375f + 0.001f,
					0.5f + 0.001f,
					0.5f + 0.001f)),
			
	};
	public BoxShape[] box12 = new BoxShape[]{
			new BoxShape(new Vector3f(
					0.5f + 0.001f,
					0.5f + 0.001f,
					0.25f + 0.001f)),
			new BoxShape(new Vector3f(
					0.5f + 0.001f,
					0.5f + 0.001f,
					0.25f + 0.001f)),
			new BoxShape(new Vector3f(
					0.5f + 0.001f,
					0.25f + 0.001f,
					0.5f + 0.001f)),
			new BoxShape(new Vector3f(
					0.5f + 0.001f,
					0.25f + 0.001f,
					0.5f + 0.001f)),
			new BoxShape(new Vector3f(
					0.25f + 0.001f,
					0.5f + 0.001f,
					0.5f + 0.001f)),
			new BoxShape(new Vector3f(
					0.25f + 0.001f,
					0.5f + 0.001f,
					0.5f + 0.001f)),
			
	};
	public BoxShape[] box14 = new BoxShape[]{
			new BoxShape(new Vector3f(
					0.5f + 0.001f,
					0.5f + 0.001f,
					0.125f + 0.001f)),
			new BoxShape(new Vector3f(
					0.5f + 0.001f,
					0.5f + 0.001f,
					0.125f + 0.001f)),
			new BoxShape(new Vector3f(
					0.5f + 0.001f,
					0.125f + 0.001f,
					0.5f + 0.001f)),
			new BoxShape(new Vector3f(
					0.5f + 0.001f,
					0.125f + 0.001f,
					0.5f + 0.001f)),
			new BoxShape(new Vector3f(
					0.125f + 0.001f,
					0.5f + 0.001f,
					0.5f + 0.001f)),
			new BoxShape(new Vector3f(
					0.125f + 0.001f,
					0.5f + 0.001f,
					0.5f + 0.001f)),
			
	};
	SimplexSolverInterface simplexSolver;
	ConvexShape shapeA;
	CubeShape cubesB;
	public Transform lodBlockTransform = new Transform();
	

}
