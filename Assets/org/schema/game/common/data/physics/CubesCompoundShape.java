package org.schema.game.common.data.physics;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.CompoundShapeChild;
import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.linearmath.VectorUtil;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.physics.shape.GameShape;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.network.objects.container.CenterOfMassInterface;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

public class CubesCompoundShape extends CompoundShape implements GameShape, CenterOfMassInterface {

	private static final boolean ONLY_MASTER_CENTER_OF_MASS = true;

	private final SegmentController segmentController;
	private final Vector3f localAabbMin = new Vector3f();
	private final Vector3f localAabbMax = new Vector3f();
	private final Vector3f tmpLocalAabbMin = new Vector3f();
	private final Vector3f tmpLocalAabbMax = new Vector3f();
	private final Vector3f _localAabbMin = new Vector3f();
	private final Vector3f _localAabbMax = new Vector3f();
	private final Vector3f localCenterTmp = new Vector3f();
	private final Vector3f localHalfExtentsTmp = new Vector3f();
	private final Matrix3f abs_bTmp = new Matrix3f();
	private final Vector3f centerTmp = new Vector3f();
	private final Vector3f tmp = new Vector3f();
	private final Vector3f extentTmp = new Vector3f();
	private final Vector3f centerOfMass = new Vector3f();
	private final Transform oTmp = new Transform();
	private final Transform oTmp2 = new Transform();
	Vector3f localAabbMinLast = new Vector3f(1e30f, 1e30f, 1e30f);
	Vector3f localAabbMaxLast = new Vector3f(-1e30f, -1e30f, -1e30f);
	private Transform lastTrans = new Transform();
	private short lastUpdateNum;
	private boolean changedAabb = false;

	public CubesCompoundShape(SegmentController segmentController) {
		this.segmentController = segmentController;
	}

	@Override
	public Vector3f getCenterOfMass() {

		if (((GameStateInterface) segmentController.getState()).getGameState().isWeightedCenterOfMass() && segmentController.getTotalPhysicalMass() > 0
				&& segmentController instanceof Ship) {

			if (ONLY_MASTER_CENTER_OF_MASS) {
				centerOfMass.set(segmentController.getCenterOfMassUnweighted());
				centerOfMass.scale(1f / segmentController.getTotalPhysicalMass());
			} else {

				centerOfMass.set(0, 0, 0);
				float totalMass = 0;
				for (CompoundShapeChild c : getChildList()) {
					CubeShape chieldShape = (CubeShape) c.childShape;
					totalMass += chieldShape.getSegmentController().getTotalPhysicalMass();
					centerOfMass.x += (c.transform.origin.x + chieldShape.getSegmentController().getCenterOfMassUnweighted().x);
					centerOfMass.y += (c.transform.origin.y + chieldShape.getSegmentController().getCenterOfMassUnweighted().y);
					centerOfMass.z += (c.transform.origin.z + chieldShape.getSegmentController().getCenterOfMassUnweighted().z);
				}
				centerOfMass.scale(1f / totalMass);
			}

			return centerOfMass;
		} else {
			centerOfMass.set(0, 0, 0);
			return centerOfMass;
		}
	}

	@Override
	public boolean isVirtual() {
		return !segmentController.isOnServer() && segmentController.hasClientVirtual();
	}

	@Override
	public float getTotalPhysicalMass() {
		return segmentController.getTotalPhysicalMass();
	}

	@Override
	public void addChildShape(Transform localTransform, CollisionShape shape) {
		//m_childTransforms.push_back(localTransform);
		//m_childShapes.push_back(shape);
		CubesCompoundShapeChild child = new CubesCompoundShapeChild();
		child.transform.set(localTransform);
		child.transformPivoted.set(localTransform);
		child.childShape = shape;
		child.childShapeType = shape.getShapeType();
		child.childMargin = shape.getMargin();

		getChildList().add(child);

		// extend the local aabbMin/aabbMax

		shape.getAabb(localTransform, _localAabbMin, _localAabbMax);

		// JAVA NOTE: rewritten
		//		for (int i=0;i<3;i++)
		//		{
		//			if (this.localAabbMin[i] > _localAabbMin[i])
		//			{
		//				this.localAabbMin[i] = _localAabbMin[i];
		//			}
		//			if (this.localAabbMax[i] < _localAabbMax[i])
		//			{
		//				this.localAabbMax[i] = _localAabbMax[i];
		//			}
		//		}
		VectorUtil.setMin(this.localAabbMin, _localAabbMin);
		VectorUtil.setMax(this.localAabbMax, _localAabbMax);

	}

	@Override
	public void removeChildShape(CollisionShape shape) {
		super.removeChildShape(shape);
	}

	/* (non-Javadoc)
	 * @see com.bulletphysics.collision.shapes.CompoundShape#getChildTransform(int, com.bulletphysics.linearmath.Transform)
	 */
	@Override
	public Transform getChildTransform(int index, Transform out) {
		CubesCompoundShapeChild c = (CubesCompoundShapeChild) getChildList().getQuick(index);
		assert (c != null) : index + "; " + segmentController;
		if(c != null){
		out.set(c.transform);

//		if(index == 0){
		if (segmentController.getTotalPhysicalMass() > 0) {
			//			System.err.println(getSegmentController().getState()+" "+getSegmentController()+" MASS:::  "+segmentController.getTotalPhysicalMass());
			Vector3f cc = getCenterOfMass();
			out.origin.sub(cc);

		} else {
			//			System.err.println("NILLLL ");
		}
		}else{
			out.setIdentity();
			
			System.err.println("[PHYSICS][WARNING] no child shape for "+segmentController);
		}
//		}
		return out;
	}

	/**
	 * getAabb's default implementation is brute force, expected derived classes to implement a fast dedicated version.
	 */
	@Override
	public void getAabb(Transform trans, Vector3f aabbMin, Vector3f aabbMax) {
		if (!lastTrans.equals(trans) || changedAabb || lastUpdateNum != segmentController.getState().getNumberOfUpdate()) {
			localHalfExtentsTmp.sub(localAabbMax, localAabbMin);
			localHalfExtentsTmp.scale(0.5f);
			localHalfExtentsTmp.x += getMargin();
			localHalfExtentsTmp.y += getMargin();
			localHalfExtentsTmp.z += getMargin();
			//		if(segmentController.toString().contains("CSS-Schema_1367765418233")){
			//			System.err.println("CALCULATING AABB LOCAL HALF "+segmentController.getState()+"; "+localHalfExtents+"; Margin "+getMargin());
			//		}
			localCenterTmp.add(localAabbMax, localAabbMin);
			localCenterTmp.scale(0.5f);
			//		if(segmentController.toString().contains("CSS-Schema_1367765418233")){
			//			System.err.println("CALCULATING AABB LOCAL CENTER "+segmentController.getState()+"; "+localCenter+"; ");
			//		}
			abs_bTmp.set(trans.basis);
			MatrixUtil.absolute(abs_bTmp);

			centerTmp.set(localCenterTmp);
			trans.transform(centerTmp);

			//		Vector3f tmp = new @Stack Vector3f();

			//		Vector3f extent = new @Stack Vector3f();
			abs_bTmp.getRow(0, tmp);
			extentTmp.x = tmp.dot(localHalfExtentsTmp);
			abs_bTmp.getRow(1, tmp);
			extentTmp.y = tmp.dot(localHalfExtentsTmp);
			abs_bTmp.getRow(2, tmp);
			extentTmp.z = tmp.dot(localHalfExtentsTmp);

			lastTrans.set(trans);

			lastUpdateNum = segmentController.getState().getNumberOfUpdate();
		}
		aabbMin.sub(centerTmp, extentTmp);
		aabbMax.add(centerTmp, extentTmp);

		//		if(segmentController.toString().contains("CSS-Schema_1367765418233")){
		//			System.err.println("CALCULATING AABB RESULT "+segmentController.getState()+"; "+aabbMin+"; "+aabbMax);
		//		}
	}

	@Override
	public void recalculateLocalAabb() {
		// Recalculate the local aabb
		// Brute force, it iterates over all the shapes left.
		localAabbMin.set(1e30f, 1e30f, 1e30f);
		localAabbMax.set(-1e30f, -1e30f, -1e30f);
		changedAabb = false;

		// extend the local aabbMin/aabbMax
		for (int j = 0; j < getChildList().size(); j++) {
			Transform childTransform = getChildTransform(j, oTmp);
			getChildList().getQuick(j).childShape.getAabb(childTransform, tmpLocalAabbMin, tmpLocalAabbMax);

			for (int i = 0; i < 3; i++) {
				if (VectorUtil.getCoord(localAabbMin, i) > VectorUtil.getCoord(tmpLocalAabbMin, i)) {
					VectorUtil.setCoord(localAabbMin, i, VectorUtil.getCoord(tmpLocalAabbMin, i));
				}
				if (VectorUtil.getCoord(localAabbMax, i) < VectorUtil.getCoord(tmpLocalAabbMax, i)) {
					VectorUtil.setCoord(localAabbMax, i, VectorUtil.getCoord(tmpLocalAabbMax, i));
				}
			}
		}

		if (!localAabbMinLast.equals(localAabbMin) || !localAabbMaxLast.equals(localAabbMax)) {
			localAabbMinLast.set(localAabbMin);
			localAabbMaxLast.set(localAabbMax);
			changedAabb = true;
		} else {

		}

		//		if(segmentController.toString().contains("CSS-Schema_1367765418233")){
		//			System.err.println("CALCULATING AABB COMBINE "+segmentController.getState()+"; "+aabbMin+"; "+aabbMax+"; "+segmentController.getBoundingBox());
		//		}
	}
	public void getAABB(int childIndex, Transform trans,
			Vector3f outOuterMin, Vector3f outOuterMax, AABBVarSet varSet) {
		oTmp.set(trans);
		CollisionShape child = getChildShape(childIndex);
		getChildTransform(childIndex, oTmp2);
		oTmp.mul(oTmp2);
		child.getAabb(oTmp, outOuterMin, outOuterMax);
	}
	
	Matrix3f tensor = new Matrix3f();
	Transform principal = new Transform();

	public final Vector3f lastPhysicsAABBMin = new Vector3f();
	public final Vector3f lastPhysicsAABBMax = new Vector3f();
	@Override
	public void calculateLocalInertia(float mass, Vector3f inertia) {
		principal.setIdentity();
		tensor.set(segmentController.tensor);
		MatrixUtil.diagonalize(tensor, principal.basis, 0.00001f, 20);
		inertia.set(Math.max(0f, tensor.m00), Math.max(0f, tensor.m11), Math.max(0f, tensor.m22));
	}

	public SegmentController getSegmentController() {
		return segmentController;
	}



	/* (non-Javadoc)
	 * @see com.bulletphysics.collision.shapes.CompoundShape#recalculateLocalAabb()
	 */
	//	@Override
	//	public void recalculateLocalAabb() {
	//
	////		super.recalculateLocalAabb();
	//		// Recalculate the local aabb
	//				// Brute force, it iterates over all the shapes left.
	//
	////				Transform t = new Transform();
	////				t.setIdentity();
	////				Vector3f aabbMin = new Vector3f();
	////				Vector3f aabbMax = new Vector3f();
	////				getAabb(t, aabbMin, aabbMax);
	////				if(segmentController instanceof Ship){
	////					System.err.println("AABB FROM "+getNumChildShapes()+" childs: "+aabbMin+"; "+aabbMax+"   ---- "+this+"; "+segmentController+"; "+segmentController.getState());
	////				}
	//	}



	/* (non-Javadoc)
	 * @see com.bulletphysics.collision.shapes.CompoundShape#calculateLocalInertia(float, javax.vecmath.Vector3f)
	 */
	//	@Override
	//	public void calculateLocalInertia(float mass, Vector3f inertia) {
	//		segmentController.getPhysicsDataContainer().getShapeChild().childShape.calculateLocalInertia(mass, inertia);
	//			////		super.calculateLocalInertia(mass, inertia);
	//	}

	//	@Override
	//	public BroadphaseNativeType getShapeType() {
	//		return BroadphaseNativeType.FAST_CONCAVE_MESH_PROXYTYPE;
	////		return BroadphaseNativeType.COMPOUND_SHAPE_PROXYTYPE;
	//	}
	/* (non-Javadoc)
	 * @see com.bulletphysics.collision.shapes.CollisionShape#isCompound()
	 */
	@Override
	public boolean isCompound() {
		return true;
	}

	@Override
	public String toString() {
		return "[CCS" + (segmentController.isOnServer() ? "|SER " : "|CLI ") + segmentController + "]";
	}

	@Override
	public SimpleTransformableSendableObject getSimpleTransformableSendableObject() {
		return segmentController;
	}

	

}
