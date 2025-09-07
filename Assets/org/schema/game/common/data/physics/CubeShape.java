package org.schema.game.common.data.physics;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.game.common.controller.SegmentBufferInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.physics.sweepandpruneaabb.SegmentAabbInterface;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;

import com.bulletphysics.collision.broadphase.BroadphaseNativeType;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.Transform;

public class CubeShape extends CollisionShape implements SegmentAabbInterface {

	private static Vector3f localScaling = new Vector3f(1, 1, 1);
	private final SegmentBufferInterface segmentBuffer;
	private final Vector3f min = new Vector3f();
	private final Vector3f max = new Vector3f();
	private final Vector3f minCached = new Vector3f();
	private final Vector3f maxCached = new Vector3f();
	private final Transform cachedTransform = new Transform();
	private float margin = 0.0f;
	private short cacheDate = -1;
	private float cachedMargin;
	private final AABBVarSet varSet;
	
	private static ThreadLocal<AABBVarSet> threadLocal = new ThreadLocal<AABBVarSet>() {

		@Override
		protected AABBVarSet initialValue() {
			return new AABBVarSet();
		}
	};
	public CubeShape(SegmentBufferInterface segmentBufferInterface) {
		this.segmentBuffer = segmentBufferInterface;
		cachedTransform.setIdentity();
		this.varSet = threadLocal.get();
	}

	public static void transformAabb(Vector3f localAabbMin, Vector3f localAabbMax, float margin, Transform trans, Vector3f aabbMinOut, Vector3f aabbMaxOut, AABBVarSet varSet) {
		//		AabbUtil2.transformAabb(localAabbMin, localAabbMax, margin, trans, aabbMinOut, aabbMaxOut);
		assert (localAabbMin.x <= localAabbMax.x);
		assert (localAabbMin.y <= localAabbMax.y);
		assert (localAabbMin.z <= localAabbMax.z);

		Vector3f localHalfExtents = varSet.localHalfExtents;
		localHalfExtents.sub(localAabbMax, localAabbMin);
		localHalfExtents.scale(0.5f);

		localHalfExtents.x += margin;
		localHalfExtents.y += margin;
		localHalfExtents.z += margin;

		Vector3f localCenter = varSet.localCenter;
		localCenter.add(localAabbMax, localAabbMin);
		localCenter.scale(0.5f);

		Matrix3f abs_b = varSet.abs_b;
		abs_b.set(trans.basis);
		MatrixUtil.absolute(abs_b);

		Vector3f center = varSet.center;
		center.set(localCenter);
		trans.transform(center);

		Vector3f extent = varSet.extent;

		extent.x = (abs_b.m00 * localHalfExtents.x + abs_b.m01 * localHalfExtents.y + abs_b.m02 * localHalfExtents.z);
		extent.y = (abs_b.m10 * localHalfExtents.x + abs_b.m11 * localHalfExtents.y + abs_b.m12 * localHalfExtents.z);
		extent.z = (abs_b.m20 * localHalfExtents.x + abs_b.m21 * localHalfExtents.y + abs_b.m22 * localHalfExtents.z);

		aabbMinOut.sub(center, extent);
		aabbMaxOut.add(center, extent);
	}

	public static void transformAabb(Vector3f localAabbMin, Vector3f localAabbMax, float margin, Transform trans, Vector3f aabbMinOut, Vector3f aabbMaxOut, AABBVarSet varSet, Matrix3f absMat) {
		//		AabbUtil2.transformAabb(localAabbMin, localAabbMax, margin, trans, aabbMinOut, aabbMaxOut);
		assert (localAabbMin.x <= localAabbMax.x);
		assert (localAabbMin.y <= localAabbMax.y);
		assert (localAabbMin.z <= localAabbMax.z);

		Vector3f localHalfExtents = varSet.localHalfExtents;
		localHalfExtents.sub(localAabbMax, localAabbMin);
		localHalfExtents.scale(0.5f);

		localHalfExtents.x += margin;
		localHalfExtents.y += margin;
		localHalfExtents.z += margin;

		Vector3f localCenter = varSet.localCenter;
		localCenter.add(localAabbMax, localAabbMin);
		localCenter.scale(0.5f);

		Matrix3f abs_b = varSet.abs_b;
		abs_b.set(absMat);
		//absolute matrix was provided
		//		MatrixUtil.absolute(abs_b);

		Vector3f center = varSet.center;
		center.set(localCenter);
		trans.transform(center);

		Vector3f extent = varSet.extent;

		//		Vector3f tmp = varSet.tmp;
		//		abs_b.getRow(0, tmp);
		//		extent.x = tmp.dot(localHalfExtents);
		//		abs_b.getRow(1, tmp);
		//		extent.y = tmp.dot(localHalfExtents);
		//		abs_b.getRow(2, tmp);
		//		extent.z = tmp.dot(localHalfExtents);

		extent.x = (abs_b.m00 * localHalfExtents.x + abs_b.m01 * localHalfExtents.y + abs_b.m02 * localHalfExtents.z);
		//		absoluteMat.getRow(1, tmp);
		//		tmp.set(absoluteMat.m10, absoluteMat.m11, absoluteMat.m12);
		//		extent.y = tmp.dot(OctreeVariableSet.localHalfExtends[lvl]);
		extent.y = (abs_b.m10 * localHalfExtents.x + abs_b.m11 * localHalfExtents.y + abs_b.m12 * localHalfExtents.z);

		//		absoluteMat.getRow(2, tmp);
		//		tmp.set(absoluteMat.m20, absoluteMat.m21, absoluteMat.m22);
		//		extent.z = tmp.dot(OctreeVariableSet.localHalfExtends[lvl]);
		extent.z = (abs_b.m20 * localHalfExtents.x + abs_b.m21 * localHalfExtents.y + abs_b.m22 * localHalfExtents.z);

		aabbMinOut.sub(center, extent);
		aabbMaxOut.add(center, extent);
	}

	public SegmentController getSegmentController() {
		return segmentBuffer.getSegmentController();
	}

	public void getAabb(Transform t, float margin, Vector3f aabbMin, Vector3f aabbMax) {
		if (margin == cachedMargin &&
				segmentBuffer.getSegmentController().getState().getNumberOfUpdate() == cacheDate &&
				t.equals(cachedTransform)) {
			aabbMin.set(minCached);
			aabbMax.set(maxCached);
		} else {
			getAabbUncached(t, margin, aabbMin, aabbMax, true);
		}
	}

	@Override
	public void getAabb(Transform t, Vector3f aabbMin, Vector3f aabbMax) {
		getAabb(t, margin, aabbMin, aabbMax);
	}

	@Override
	public BroadphaseNativeType getShapeType() {
		return BroadphaseNativeType.TERRAIN_SHAPE_PROXYTYPE;
	}

	@Override
	public void setLocalScaling(Vector3f scaling) {
		localScaling.absolute(scaling);

	}

	@Override
	public Vector3f getLocalScaling(Vector3f out) {
		return localScaling;
	}

	@Override
	public void calculateLocalInertia(float mass, Vector3f inertia) {

		//WARNING: THIS IS NOT USED: go to ----> CubesCompoundShape.calculateLocalInertia

		float lx = segmentBuffer.getBoundingBox().max.x - segmentBuffer.getBoundingBox().min.x + margin;
		float ly = segmentBuffer.getBoundingBox().max.y - segmentBuffer.getBoundingBox().min.y + margin;
		float lz = segmentBuffer.getBoundingBox().max.z - segmentBuffer.getBoundingBox().min.z + margin;

		//		inertia.set(mass/12f  * ((ly * ly + lz * lz)),
		//				mass/12f * ((lx * lx + lz * lz)),
		//				mass/12f * ((lx * lx + ly * ly)));
		inertia.set(mass / 3f * ((ly * ly + lz * lz)),
				mass / 3f * ((lx * lx + lz * lz)),
				mass / 3f * ((lx * lx + ly * ly)));

	}	@Override
	public float getMargin() {
		return margin;
	}

	@Override
	public String getName() {
		return "CUBES_MESH";
	}

	private void setBB(){
		if (!segmentBuffer.getBoundingBox().isInitialized()) {
			//				System.err.println("NOT YET INITIALIZED");
			min.set(0, 0, 0);
			max.set(0, 0, 0);
		} else {
			
			if(!segmentBuffer.getBoundingBox().min.equals(min) || !segmentBuffer.getBoundingBox().max.equals(max)){
				min.set(segmentBuffer.getBoundingBox().min);
				max.set(segmentBuffer.getBoundingBox().max);
				
				if (min.x > max.x || min.y > max.y || min.z > max.z ||
						Float.isNaN(min.x) || Float.isNaN(min.y) || Float.isNaN(min.z) ||
						Float.isNaN(max.x) || Float.isNaN(max.y) || Float.isNaN(max.z)) {
					System.err.println("[EXCEPTION] " + segmentBuffer.getSegmentController().getState() + " WARNING. physics cube AABB is corrupt: " + segmentBuffer.getSegmentController() + "; " + min + "/" + max);
					//happened when spawing ship while segments were loading
					min.set(0, 0, 0);
					max.set(0, 0, 0);
				}
			}
		}
	}
	@Override
	public void getAabbUncached(Transform t, Vector3f aabbMin, Vector3f aabbMax, boolean cache) {
		getAabbUncached(t, margin, aabbMin, aabbMax, false);
	}
	public void getAabbUncached(Transform t, float margin, Vector3f aabbMin, Vector3f aabbMax, boolean cache) {

		setBB();
		
		transformAabb(min, max, margin, t, aabbMin, aabbMax, varSet);
		
		if(cache){
			minCached.set(aabbMin);
			maxCached.set(aabbMax);
			cachedTransform.set(t);
			cachedMargin = margin;
			cacheDate = segmentBuffer.getSegmentController().getState().getNumberOfUpdate();
		}

	}

	@Override
	public void getAabbIdent(Vector3f minOut, Vector3f maxOut) {
		setBB();
		minOut.set(this.min);
		maxOut.set(this.max);
	}

	@Override
	public void getSegmentAabb(Segment seg, Transform t, Vector3f aabbMin, Vector3f aabbMax, Vector3f localMin, Vector3f localMax, AABBVarSet varSet) {
		if (seg.cacheDate == segmentBuffer.getSegmentController().getState().getNumberOfUpdate() &&
				Matrix4fTools.transformEquals(t, seg.cachedTransform)) {
			//valid cache
			aabbMin.set(seg.cacheBBMinX, seg.cacheBBMinY, seg.cacheBBMinZ);
			aabbMax.set(seg.cacheBBMaxX, seg.cacheBBMaxY, seg.cacheBBMaxZ);
		} else {
			if (!seg.isEmpty()) {
				SegmentData sData = seg.getSegmentData();

				if (sData.getMin().x <= sData.getMax().x) {
					localMin.set(
							seg.pos.x + sData.getMin().x - SegmentData.SEG_HALF - 0.5f,
							seg.pos.y + sData.getMin().y - SegmentData.SEG_HALF - 0.5f,
							seg.pos.z + sData.getMin().z - SegmentData.SEG_HALF - 0.5f);

					localMax.set(
							seg.pos.x + sData.getMax().x - SegmentData.SEG_HALF - 0.5f,
							seg.pos.y + sData.getMax().y - SegmentData.SEG_HALF - 0.5f,
							seg.pos.z + sData.getMax().z - SegmentData.SEG_HALF - 0.5f);
//					System.err.println("MIN MAX: "+localMin+"; "+localMax);
					transformAabb(localMin,
							localMax,
							margin, t, aabbMin, aabbMax, varSet);
				} else {

					System.err.println("[CUBESHAPE] " + sData.getSegmentController().getState() + " WARNING: NON INIT SEGMENT DATA AABB REQUEST " + sData.getMin() + "; " + sData.getMax() + ": " + sData.getSegmentController() + ": RESTRUCTING AABB");
					sData.restructBB(true);
					seg.getSegmentController().getSegmentBuffer().restructBB();
					aabbMax.set(0, 0, 0);
					aabbMin.set(0, 0, 0);
				}
			} else {
				System.err.println("[CUBESHAPE] EMPTY SEGMENT DATA AABB REQUEST");
				aabbMax.set(0, 0, 0);
				aabbMin.set(0, 0, 0);
			}
			seg.cacheDate = segmentBuffer.getSegmentController().getState().getNumberOfUpdate();
			seg.cachedTransform[0] = t.basis.m00;
			seg.cachedTransform[1] = t.basis.m10;
			seg.cachedTransform[2] = t.basis.m20;
			seg.cachedTransform[4] = t.basis.m01;
			seg.cachedTransform[5] = t.basis.m11;
			seg.cachedTransform[6] = t.basis.m21;
			seg.cachedTransform[8] = t.basis.m02;
			seg.cachedTransform[9] = t.basis.m12;
			seg.cachedTransform[10] = t.basis.m22;
			seg.cachedTransform[12] = t.origin.x;
			seg.cachedTransform[13] = t.origin.y;
			seg.cachedTransform[14] = t.origin.z;

			seg.cacheBBMinX = aabbMin.x;
			seg.cacheBBMinY = aabbMin.y;
			seg.cacheBBMinZ = aabbMin.z;

			seg.cacheBBMaxX = aabbMax.x;
			seg.cacheBBMaxY = aabbMax.y;
			seg.cacheBBMaxZ = aabbMax.z;
		}
	}

	public SegmentBufferInterface getSegmentBuffer() {
		return segmentBuffer;
	}

	@Override
	public String toString() {
		return "[CubesShape" + (segmentBuffer.getSegmentController().isOnServer() ? "|SER " : "|CLI ") + segmentBuffer.getSegmentController() + "]";
	}

	public Vector3f getHalfExtends(Transform trans) {
		Vector3f localMin = new Vector3f();
		Vector3f localMax = new Vector3f();

		localMin.set(segmentBuffer.getBoundingBox().min);
		localMax.set(segmentBuffer.getBoundingBox().max);

		Vector3f localCenter = new Vector3f();
		localCenter.add(localMin, localMax);
		localCenter.scale(0.5f);

		//		Matrix3f abs_b = new Matrix3f();
		//		abs_b.set(trans.basis);
		//		MatrixUtil.absolute(abs_b);

		Vector3f center = new Vector3f();
		center.set(localCenter);
		trans.transform(center);

		return localCenter;
	}	@Override
	public void setMargin(float margin) {
		this.margin = margin;

	}





}
