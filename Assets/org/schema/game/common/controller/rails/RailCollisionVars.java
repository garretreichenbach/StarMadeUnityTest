package org.schema.game.common.controller.rails;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.physics.AABBVarSet;
import org.schema.game.common.data.physics.BoxShapeExt;
import org.schema.game.common.data.physics.GjkEpaPenetrationDepthSolverExt;
import org.schema.game.common.data.physics.GjkPairDetectorVariables;
import org.schema.game.common.data.physics.SimpleIntList;
import org.schema.game.common.data.physics.octree.IntersectionCallback;
import org.schema.game.common.data.physics.octree.OctreeVariableSet;
import org.schema.game.common.data.physics.sweepandpruneaabb.ArrayOctreeToplevelSweeper;
import org.schema.game.common.data.physics.sweepandpruneaabb.SegmentOctreeSweeper;
import org.schema.game.common.data.physics.sweepandpruneaabb.SegmentSweeper;
import org.schema.game.common.data.world.Segment;
import org.schema.schine.graphicsengine.forms.BoundingBox;

import com.bulletphysics.collision.narrowphase.VoronoiSimplexSolver;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class RailCollisionVars {
	public final Matrix3f absolute1 = new Matrix3f();
	public final Matrix3f absolute2 = new Matrix3f();
	public final GjkPairDetectorVariables gjkVar = new GjkPairDetectorVariables();
	public final Transform wtInv1 = new Transform();
	public final Transform wtInv0 = new Transform();
	public final AABBVarSet aabbVarSet = new AABBVarSet();
	public final Vector3f elemPosTest = new Vector3f();
	public final Vector3f elemPosTestTmp = new Vector3f();
	public final Vector3i elemPosCheck = new Vector3i();
	public final Vector3i elemPosCheckD = new Vector3i();
	public final SegmentPiece pieceTmp = new SegmentPiece();
	public final VoronoiSimplexSolver simplexSolver = new VoronoiSimplexSolver();
	public final GjkEpaPenetrationDepthSolverExt pdSolver = new GjkEpaPenetrationDepthSolverExt();
	public final SegmentSweeper sweeper = new SegmentSweeper();
	public final SegmentOctreeSweeper octreeSweeper = new SegmentOctreeSweeper();
	public final ArrayOctreeToplevelSweeper octreeTopLevelSweeper = new ArrayOctreeToplevelSweeper();
	final Transform tmpTrans3 = new Transform();
	final Transform tmpTrans4 = new Transform();
	final Vector3f localMinOut = new Vector3f();
	final Vector3f localMaxOut = new Vector3f();
	final Vector3f tmp = new Vector3f();
	final Vector3f pos0 = new Vector3f();
	final Vector3f pos1 = new Vector3f();
	final Vector3f diff = new Vector3f();
	final Vector3f normalOnSurfaceB = new Vector3f();
	final BoundingBox outerWorld = new BoundingBox();
	final BoundingBox innerWorld = new BoundingBox();
	final BoundingBox innerWorldAABBforA = new BoundingBox();
	final BoundingBox outerWorldAABBforB = new BoundingBox();
	final BoundingBox innerAABBforA = new BoundingBox();
	final BoundingBox outerAABBforB = new BoundingBox();
	final BoundingBox intersection = new BoundingBox();
	final BoundingBox intersection2 = new BoundingBox();
	final BoundingBox intersectionInASpaceWithB = new BoundingBox();
	final BoundingBox intersectionInBSpaceWithA = new BoundingBox();
	final BoundingBox intersectionInASpaceWithB2 = new BoundingBox();
	final BoundingBox intersectionInBSpaceWithA2 = new BoundingBox();
	final BoundingBox outBB_A = new BoundingBox();
	final BoundingBox outBB_B = new BoundingBox();
	final BoundingBox outBBCopy = new BoundingBox();
	final ObjectArrayList<Segment> outerNonEmptySegments = new ObjectArrayList<Segment>();
	final ObjectArrayList<Segment> innerNonEmptySegments = new ObjectArrayList<Segment>();
	final FloatArrayList bbCacheInner = new FloatArrayList();
	final Vector3i minIntA = new Vector3i();
	final Vector3i maxIntA = new Vector3i();
	final Vector3i minIntB = new Vector3i();
	final Vector3i maxIntB = new Vector3i();
	final Vector3i minIntA2 = new Vector3i();
	final Vector3i maxIntA2 = new Vector3i();
	final Vector3i minIntB2 = new Vector3i();
	final Vector3i maxIntB2 = new Vector3i();
	final Vector3f min = new Vector3f();
	final Vector3f max = new Vector3f();
	final Vector3f bMinOut = new Vector3f();
	final Vector3f bMaxOut = new Vector3f();
	final Vector3f minOut = new Vector3f();
	final Vector3f maxOut = new Vector3f();
	final Vector3f othermin = new Vector3f();
	final Vector3f othermax = new Vector3f();
	final Vector3f elemPosA = new Vector3f();
	final Vector3f elemPosB = new Vector3f();
	final Vector3f elemPosAAbs = new Vector3f();
	final Vector3f elemPosBAbs = new Vector3f();
	final Vector3f elemPosDist = new Vector3f();
	//	Vector3f hitMinA = new Vector3f();
	//	Vector3f hitMaxA = new Vector3f();
	//	Vector3f hitMinB = new Vector3f();
	//	Vector3f hitMaxB = new Vector3f();
	final Vector3b startA = new Vector3b();
	final Vector3b startB = new Vector3b();
	final Vector3b endA = new Vector3b();
	final Vector3b endB = new Vector3b();
	//	final BoxShapeExt box0 = new BoxShapeExt(new Vector3f(
//			Element.HALF_SIZE / 2f  -0.999f,
//			Element.HALF_SIZE / 2f  -0.999f,
//			Element.HALF_SIZE / 2f  -0.999f));
//	final BoxShapeExt box1 = new BoxShapeExt(new Vector3f(
//			Element.HALF_SIZE / 2f  -0.999f,
//			Element.HALF_SIZE / 2f  -0.999f,
//			Element.HALF_SIZE / 2f  -0.999f));
	final BoxShapeExt box0 = new BoxShapeExt(new Vector3f(
			0.49899f,
			0.49899f,
			0.49899f));
	final BoxShapeExt box1 = new BoxShapeExt(new Vector3f(
			0.49899f,
			0.49899f,
			0.49899f));
	final BoxShapeExt box0M = new BoxShapeExt(new Vector3f(
			0.49899f,
			0.49899f,
			0.49899f));
	final BoxShapeExt box1M = new BoxShapeExt(new Vector3f(
			0.49899f,
			0.49899f,
			0.49899f));
	{
		box0M.setMargin(-0.03f);
		box1M.setMargin(-0.03f);
	}
	final Vector3f outInnerMin = new Vector3f();
	final Vector3f outInnerMax = new Vector3f();
	final Vector3f outOuterMin = new Vector3f();
	final Vector3f outOuterMax = new Vector3f();
	final Vector3f nA = new Vector3f();
	final Vector3f nB = new Vector3f();
	final Vector3f otherMinIn = new Vector3f();
	final Vector3f otherMaxIn = new Vector3f();
	final BoundingBox bbOuterSeg = new BoundingBox();
	final BoundingBox bbInnerSeg = new BoundingBox();
	final BoundingBox bbSectorIntersection = new BoundingBox();
	final BoundingBox bbSeg16a = new BoundingBox();
	final BoundingBox bbSeg16b = new BoundingBox();
	final BoundingBox bbSectorIntersectionTest = new BoundingBox();
	final BoundingBox bbOuterOct = new BoundingBox();
	final BoundingBox bbInnerOct = new BoundingBox();
	final BoundingBox bbOctIntersection = new BoundingBox();
	public GjkPairDetectorVariables gjkVariables = new GjkPairDetectorVariables();
	Transform tmpAABBTrans0 = new Transform();
	Transform tmpAABBTrans1 = new Transform();
	Transform tmpTrans0 = new Transform();
	Transform tmpTrans1 = new Transform();
	Transform tmpTrans0Actual = new Transform();
	Transform tmpTrans1Actual = new Transform();
	Transform tmpTrans0Rel = new Transform();
	Transform tmpTrans1Rel = new Transform();
	// Vector3f normalOnSurfaceA = new Vector3f();
	OctreeVariableSet oSet;
	IntersectionCallback intersectionCallBackAwithB = new IntersectionCallback();
	IntersectionCallback intersectionCallBackBwithA = new IntersectionCallback();
	public Vector3i innerMaxBlock = new Vector3i();
	public Vector3i innerMinBlock = new Vector3i();
	
	public Vector3i outerMaxBlock = new Vector3i();
	public Vector3i outerMinBlock = new Vector3i();
	
	public Vector3f innerMaxf = new Vector3f();
	public Vector3f innerMinf = new Vector3f();
	
	public Vector3f outerMaxf = new Vector3f();
	public Vector3f outerMinf = new Vector3f();
	
	public BoundingBox innerOutf = new BoundingBox();
	public BoundingBox outerOutf = new BoundingBox();
	public SimpleIntList simpleListA = new SimpleIntList();
	public SimpleIntList simpleListB = new SimpleIntList();
	public Vector3f right0 = new Vector3f();
	public Vector3f right1 = new Vector3f();
	public Vector3f up0 = new Vector3f();
	public Vector3f up1 = new Vector3f();
	public Vector3f forw0 = new Vector3f();
	public Vector3f forw1 = new Vector3f();
	public Vector3f orientTT = new Vector3f();
	public Transform BT_A = new Transform();
	public Transform BT_B = new Transform();
	public BoxShape[] box34 = new BoxShape[]{
			new BoxShape(new Vector3f(
					0.5f + -0.00101f,
					0.5f + -0.00101f,
					0.375f + -0.00101f)),
			new BoxShape(new Vector3f(
					0.5f + -0.00101f,
					0.5f + -0.00101f,
					0.375f + -0.00101f)),
			new BoxShape(new Vector3f(
					0.5f + -0.00101f,
					0.375f + -0.00101f,
					0.5f + -0.00101f)),
			new BoxShape(new Vector3f(
					0.5f + -0.00101f,
					0.375f + -0.00101f,
					0.5f + -0.00101f)),
			new BoxShape(new Vector3f(
					0.375f + -0.00101f,
					0.5f + -0.00101f,
					0.5f + -0.00101f)),
			new BoxShape(new Vector3f(
					0.375f + -0.00101f,
					0.5f + -0.00101f,
					0.5f + -0.00101f)),
			
	};
	public BoxShape[] box12 = new BoxShape[]{
			new BoxShape(new Vector3f(
					0.5f + -0.00101f,
					0.5f + -0.00101f,
					0.25f + -0.00101f)),
			new BoxShape(new Vector3f(
					0.5f + -0.00101f,
					0.5f + -0.00101f,
					0.25f + -0.00101f)),
			new BoxShape(new Vector3f(
					0.5f + -0.00101f,
					0.25f + -0.00101f,
					0.5f + -0.00101f)),
			new BoxShape(new Vector3f(
					0.5f + -0.00101f,
					0.25f + -0.00101f,
					0.5f + -0.00101f)),
			new BoxShape(new Vector3f(
					0.25f + -0.00101f,
					0.5f + -0.00101f,
					0.5f + -0.00101f)),
			new BoxShape(new Vector3f(
					0.25f + -0.00101f,
					0.5f + -0.00101f,
					0.5f + -0.00101f)),
			
	};
	public BoxShape[] box14 = new BoxShape[]{
			new BoxShape(new Vector3f(
					0.5f + -0.00101f,
					0.5f + -0.00101f,
					0.125f + -0.00101f)),
			new BoxShape(new Vector3f(
					0.5f + -0.00101f,
					0.5f + -0.00101f,
					0.125f + -0.00101f)),
			new BoxShape(new Vector3f(
					0.5f + -0.00101f,
					0.125f + -0.00101f,
					0.5f + -0.00101f)),
			new BoxShape(new Vector3f(
					0.5f + -0.00101f,
					0.125f + -0.00101f,
					0.5f + -0.00101f)),
			new BoxShape(new Vector3f(
					0.125f + -0.00101f,
					0.5f + -0.00101f,
					0.5f + -0.00101f)),
			new BoxShape(new Vector3f(
					0.125f + -0.00101f,
					0.5f + -0.00101f,
					0.5f + -0.00101f)),
			
	};
	public BoxShape[] box34M = new BoxShape[]{
			new BoxShape(new Vector3f(
					0.5f + -0.00101f,
					0.5f + -0.00101f,
					0.375f + -0.00101f)),
			new BoxShape(new Vector3f(
					0.5f + -0.00101f,
					0.5f + -0.00101f,
					0.375f + -0.00101f)),
			new BoxShape(new Vector3f(
					0.5f + -0.00101f,
					0.375f + -0.00101f,
					0.5f + -0.00101f)),
			new BoxShape(new Vector3f(
					0.5f + -0.00101f,
					0.375f + -0.00101f,
					0.5f + -0.00101f)),
			new BoxShape(new Vector3f(
					0.375f + -0.00101f,
					0.5f + -0.00101f,
					0.5f + -0.00101f)),
			new BoxShape(new Vector3f(
					0.375f + -0.00101f,
					0.5f + -0.00101f,
					0.5f + -0.00101f)),
			
	};
	public BoxShape[] box12M = new BoxShape[]{
			new BoxShape(new Vector3f(
					0.5f + -0.00101f,
					0.5f + -0.00101f,
					0.25f + -0.00101f)),
			new BoxShape(new Vector3f(
					0.5f + -0.00101f,
					0.5f + -0.00101f,
					0.25f + -0.00101f)),
			new BoxShape(new Vector3f(
					0.5f + -0.00101f,
					0.25f + -0.00101f,
					0.5f + -0.00101f)),
			new BoxShape(new Vector3f(
					0.5f + -0.00101f,
					0.25f + -0.00101f,
					0.5f + -0.00101f)),
			new BoxShape(new Vector3f(
					0.25f + -0.00101f,
					0.5f + -0.00101f,
					0.5f + -0.00101f)),
			new BoxShape(new Vector3f(
					0.25f + -0.00101f,
					0.5f + -0.00101f,
					0.5f + -0.00101f)),
			
	};
	public BoxShape[] box14M = new BoxShape[]{
			new BoxShape(new Vector3f(
					0.5f + -0.00101f,
					0.5f + -0.00101f,
					0.125f + -0.00101f)),
			new BoxShape(new Vector3f(
					0.5f + -0.00101f,
					0.5f + -0.00101f,
					0.125f + -0.00101f)),
			new BoxShape(new Vector3f(
					0.5f + -0.00101f,
					0.125f + -0.00101f,
					0.5f + -0.00101f)),
			new BoxShape(new Vector3f(
					0.5f + -0.00101f,
					0.125f + -0.00101f,
					0.5f + -0.00101f)),
			new BoxShape(new Vector3f(
					0.125f + -0.00101f,
					0.5f + -0.00101f,
					0.5f + -0.00101f)),
			new BoxShape(new Vector3f(
					0.125f + -0.00101f,
					0.5f + -0.00101f,
					0.5f + -0.00101f)),
			
	};
	{
		for(int i = 0; i< 6; i++){
			box34M[i].setMargin(-0.03f);
			box12M[i].setMargin(-0.03f);
			box14M[i].setMargin(-0.03f);
		}
	}
}
