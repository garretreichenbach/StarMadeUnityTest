package org.schema.game.common.data.physics;

import java.util.List;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.physics.octree.IntersectionCallback;
import org.schema.game.common.data.physics.octree.OctreeVariableSet;
import org.schema.schine.graphicsengine.forms.BoundingBox;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.narrowphase.SimplexSolverInterface;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.floats.Float2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class CubeRayVariableSet {

	static final float margin = 0.15f;
	public Matrix3f absolute = new Matrix3f();
	public RayCubeGridSolver solve = new RayCubeGridSolver();
	public RayCubeGridSolver solveBlock = new RayCubeGridSolver();
	public GjkPairDetectorVariables gjkVar = new GjkPairDetectorVariables();
	public AABBVarSet aabbVarSet = new AABBVarSet();
	public BlockRecorder record;
	public int recordAmount;
	public BoxShape box0 = new BoxShape(new Vector3f(Element.BLOCK_SIZE / 2f, Element.BLOCK_SIZE / 2f, Element.BLOCK_SIZE / 2f));
	public BoundingBox outer = new BoundingBox();
	public BoundingBox inner = new BoundingBox();
	public BoundingBox outBB = new BoundingBox();
	public Vector3i minIntA = new Vector3i();
	public Vector3i maxIntA = new Vector3i();
	public Vector3i minIntB = new Vector3i();
	public Vector3i maxIntB = new Vector3i();
	public OctreeVariableSet oSet;
	public SimplexSolverInterface simplexSolver;
	public GjkEpaPenetrationDepthSolverExt gjkEpaPenetrationDepthSolver = new GjkEpaPenetrationDepthSolverExt();
	public ConvexShape shapeA;
	public CubeShape cubesB;
	public Vector3f outMin = new Vector3f();
	public Vector3f outMax = new Vector3f();
	public Vector3f fromHelp = new Vector3f();
	public Vector3f toHelp = new Vector3f();
	public Vector3f fromToHelp = new Vector3f();
	public Vector3f localMinOut = new Vector3f();
	public Vector3f localMaxOut = new Vector3f();
	public Vector3f normal = new Vector3f();
	public float[] hitLambda = new float[1];
	public IntersectionCallback intersectionCallBack = new IntersectionCallback();
	public ChangableSphereShape sphereShape = new ChangableSphereShape(0);
	public CollisionObject cubesCollisionObject;
	public Float2ObjectAVLTreeMap<AABBb> sortedAABB = new Float2ObjectAVLTreeMap<AABBb>();
	public Vector3b elemA = new Vector3b();
	public Vector3f elemPosA = new Vector3f();
	public Vector3b startOut = new Vector3b();
	public Vector3b endOut = new Vector3b();
	public Vector3f minOut = new Vector3f();
	public Vector3f maxOut = new Vector3f();
	public Vector3f nA = new Vector3f();
	public Transform boxETransform = new Transform();
	public Transform from = new Transform();
	public Transform to = new Transform();
	
	public Transform tmpTrans3 = new Transform();
	public Vector3f distTest = new Vector3f();
	public Float2ObjectAVLTreeMap<Vector4i> sorted = new Float2ObjectAVLTreeMap<Vector4i>();
	public Vector3b[] posCache = new Vector3b[8];
	public int posCachePointer = 0;
	public Vector3f lastDistHitpointWorld = new Vector3f();
	public Vector3f bbV[] = new Vector3f[9];
	public BoundingBox segAABB = new BoundingBox();
	public Transform segTrans = new Transform();
	public Vector3f segPos = new Vector3f();
	public Vector3f rayModFrom = new Vector3f();
	public Vector3f rayTmpPos = new Vector3f();
	public Vector3f rayTmp0 = new Vector3f();
	public Vector3f rayTmp1 = new Vector3f();
	public Vector3f rayModTo = new Vector3f();
	public Vector3f orientTT = new Vector3f();
	public Transform BT = new Transform();
	private List<BlockRecorder> brPool = new ObjectArrayList<BlockRecorder>();
	public List<Vector4i> takenPoints = new ObjectArrayList<Vector4i>();
	public void freeBlockRecorder(BlockRecorder p){
		assert(p.isEmpty());
		brPool.add(p);
	}
	public BlockRecorder getBlockRecorder(){
		if(brPool.isEmpty()){
			return new BlockRecorder(this);
		}else{
			BlockRecorder blockRecorder = brPool.remove(brPool.size()-1);
			assert(blockRecorder.isEmpty());
			return blockRecorder;
		}
	}
	
	public CubeRayVariableSet() {
		for (int i = 0; i < bbV.length; i++) {
			bbV[i] = new Vector3f();
		}
		for(int i = 0; i < 32; i++){
			brPool.add(new BlockRecorder(this));
		}
	}
	public BoxShape[] box34 = new BoxShape[]{
			new BoxShape(new Vector3f(
					0.5f + 0.0f,
					0.5f + 0.0f,
					0.375f + 0.0f)),
			new BoxShape(new Vector3f(
					0.5f + 0.0f,
					0.5f + 0.0f,
					0.375f + 0.0f)),
			new BoxShape(new Vector3f(
					0.5f + 0.0f,
					0.375f + 0.0f,
					0.5f + 0.0f)),
			new BoxShape(new Vector3f(
					0.5f + 0.0f,
					0.375f + 0.0f,
					0.5f + 0.0f)),
			new BoxShape(new Vector3f(
					0.375f + 0.0f,
					0.5f + 0.0f,
					0.5f + 0.0f)),
			new BoxShape(new Vector3f(
					0.375f + 0.0f,
					0.5f + 0.0f,
					0.5f + 0.0f)),
			
	};
	public BoxShape[] box12 = new BoxShape[]{
			new BoxShape(new Vector3f(
					0.5f + 0.0f,
					0.5f + 0.0f,
					0.25f + 0.0f)),
			new BoxShape(new Vector3f(
					0.5f + 0.0f,
					0.5f + 0.0f,
					0.25f + 0.0f)),
			new BoxShape(new Vector3f(
					0.5f + 0.0f,
					0.25f + 0.0f,
					0.5f + 0.0f)),
			new BoxShape(new Vector3f(
					0.5f + 0.0f,
					0.25f + 0.0f,
					0.5f + 0.0f)),
			new BoxShape(new Vector3f(
					0.25f + 0.0f,
					0.5f + 0.0f,
					0.5f + 0.0f)),
			new BoxShape(new Vector3f(
					0.25f + 0.0f,
					0.5f + 0.0f,
					0.5f + 0.0f)),
			
	};
	public BoxShape[] box14 = new BoxShape[]{
			new BoxShape(new Vector3f(
					0.5f + 0.0f,
					0.5f + 0.0f,
					0.125f + 0.0f)),
			new BoxShape(new Vector3f(
					0.5f + 0.0f,
					0.5f + 0.0f,
					0.125f + 0.0f)),
			new BoxShape(new Vector3f(
					0.5f + 0.0f,
					0.125f + 0.0f,
					0.5f + 0.0f)),
			new BoxShape(new Vector3f(
					0.5f + 0.0f,
					0.125f + 0.0f,
					0.5f + 0.0f)),
			new BoxShape(new Vector3f(
					0.125f + 0.0f,
					0.5f + 0.0f,
					0.5f + 0.0f)),
			new BoxShape(new Vector3f(
					0.125f + 0.0f,
					0.5f + 0.0f,
					0.5f + 0.0f)),
			
	};
	public final Vector3f tmpA = new Vector3f();
	public final Vector3f tmpB = new Vector3f();
	public Vector3f outInt= new Vector3f();
	public Vector3f posTmp= new Vector3f();
	public Vector3f dirTmp= new Vector3f();
	public Transform lodBlockTransform = new Transform();
	
}
