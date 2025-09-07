package org.schema.game.common.data.physics;

import com.bulletphysics.collision.broadphase.CollisionAlgorithm;
import com.bulletphysics.collision.broadphase.CollisionAlgorithmConstructionInfo;
import com.bulletphysics.collision.broadphase.DispatcherInfo;
import com.bulletphysics.collision.dispatch.CollisionAlgorithmCreateFunc;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.ManifoldResult;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.CompoundShapeChild;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;
import com.bulletphysics.util.ObjectPool;
import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.game.common.data.physics.sweepandpruneaabb.OverlappingSweepPair;

import java.util.List;

public class CompoundCollisionAlgorithmExt extends CollisionAlgorithm {

	private static ThreadLocal<CompoundCollisionVariableSet> threadLocal = new ThreadLocal<CompoundCollisionVariableSet>() {
		@Override
		protected CompoundCollisionVariableSet initialValue() {
			return new CompoundCollisionVariableSet();
		}
	};
	private final ObjectArrayList<CollisionAlgorithm> childCollisionAlgorithms = new ObjectArrayList<CollisionAlgorithm>();
	private final ObjectArrayList<CollisionAlgorithm> childCollisionAlgorithmsCOM = new ObjectArrayList<CollisionAlgorithm>();
	public boolean swapped;
	private CollisionObject compoundObject;
	private CollisionObject otherObject;
	private PersistentManifold manifoldPtr;
	private CompoundCollisionVariableSet v;


	@Override
	public void init(CollisionAlgorithmConstructionInfo ci) {
		super.init(ci);

		//		if(compoundObject.toString().contains("|SER")){
		//			System.err.println("##++## "+((System.currentTimeMillis()/1000)%5000)+" CREATED ALGORITHM: "+compoundObject+"; "+otherObject);
		//		}
		manifoldPtr = ci.manifold;
		if (manifoldPtr == null) {
			manifoldPtr = dispatcher.getNewManifold(compoundObject, otherObject);
		}
		//		this.v = new CompoundCollisionVariableSet();//.get();
		this.v = threadLocal.get();
		v.instances++;

		assert (compoundObject.getCollisionShape().isCompound());
		//		assert (!(compoundObject.getCollisionShape() instanceof CubeShape) && !(otherObject.getCollisionShape() instanceof CubeShape))
		//		:compoundObject+" --- "+otherObject;

		CompoundShape compoundShape = (CompoundShape) compoundObject.getCollisionShape();
		int numChildren = compoundShape.getNumChildShapes();
		int i;
		int alt = 0;
		assert (manifoldPtr != null);
		//		childCollisionAlgorithms.resize(numChildren);
		for (i = 0; i < numChildren; i++) {
			CollisionShape tmpShape = compoundObject.getCollisionShape();
			CollisionShape childShape = compoundShape.getChildShape(i);
			compoundObject.internalSetTemporaryCollisionShape(childShape);

			if (otherObject.getCollisionShape().isCompound()) {
				CompoundShape compoundShapeOther = (CompoundShape) otherObject.getCollisionShape();
				for (int j = 0; j < compoundShapeOther.getNumChildShapes(); j++) {

					CollisionShape tmpShapeO = otherObject.getCollisionShape();
					CollisionShape childShapeO = compoundShapeOther.getChildShape(j);
					otherObject.internalSetTemporaryCollisionShape(childShapeO);

					CollisionAlgorithm findAlgorithm = ci.dispatcher1.findAlgorithm(compoundObject, otherObject, manifoldPtr);
					assert (findAlgorithm != null) : compoundObject + " ---------- " + otherObject;
					childCollisionAlgorithmsCOM.add(findAlgorithm);

					otherObject.internalSetTemporaryCollisionShape(tmpShapeO);
				}
			} else {
				childCollisionAlgorithms.add(ci.dispatcher1.findAlgorithm(compoundObject, otherObject, manifoldPtr));
			}

			compoundObject.internalSetTemporaryCollisionShape(tmpShape);
		}
	}

	@Override
	public void destroy() {
		if (manifoldPtr != null) {

			dispatcher.releaseManifold(manifoldPtr);
			manifoldPtr = null;
		} else {
			assert (false) : compoundObject;
		}
		{
			int size = childCollisionAlgorithms.size();
			for (int i = 0; i < size; i++) {
				childCollisionAlgorithms.get(i).destroy();
				dispatcher.freeCollisionAlgorithm(childCollisionAlgorithms.getQuick(i));
			}
			childCollisionAlgorithms.clear();
		}
		{
			int size = childCollisionAlgorithmsCOM.size();
			for (int i = 0; i < size; i++) {
				//childCollisionAlgorithms.get(i).destroy();
				dispatcher.freeCollisionAlgorithm(childCollisionAlgorithmsCOM.getQuick(i));
			}
			childCollisionAlgorithmsCOM.clear();
		}
		compoundObject = null;
		otherObject = null;
		childCollisionAlgorithms.clear();
		childCollisionAlgorithmsCOM.clear();
		v.instances--;
	}

		
	@Override
	public void processCollision(CollisionObject body0, CollisionObject body1, DispatcherInfo dispatchInfo, ManifoldResult resultOut) {
		//		if(body0.toString().contains("schema_1370260089078") || body1.toString().contains("schema_1370260089078")){
		//			System.err.println("COLLISION: "+body0+"; "+body1);
		//		}
		final CollisionObject compoundObject = this.compoundObject;
		if (body0 == otherObject && body1 == compoundObject) {
			//swapped
			body0 = compoundObject;
			body1 = otherObject;
		}
//		if(body0 instanceof GamePhysicsObject && !((GamePhysicsObject)body0).getSimpleTransformableSendableObject().isOnServer()){
//			System.err.println("COL: "+((GamePhysicsObject)body0).getSimpleTransformableSendableObject().getState().getUpdateNumber()+"; "+body0+"; "+body1+"; "+compoundObject);
//		}
		//		algo.compoundObject = body1;
		//		algo.otherObject =  body0;
		if (body0 != compoundObject || body1 != otherObject) {
			System.err.println("COMPOUND ALGORITHM MULTIUSE ?!?!\n" +
					"---> " + body0 + ";         " + compoundObject + "\n" +
					"---> " + body1 + ";         " + otherObject);
		}
		assert (!(body0 instanceof RigidBodySegmentController) || !((RigidBodySegmentController) body0).isCollisionException()) : body0 + " -> " + body1;
		assert (!(body1 instanceof RigidBodySegmentController) || !((RigidBodySegmentController) body1).isCollisionException()) : body1 + " -> " + body0;
		//		CollisionObject colObj = isSwapped ? body1 : body0;
		//		CollisionObject otherObj = isSwapped ? body0 : body1;
		assert (body0 == compoundObject) : body0 + "; " + compoundObject;
		assert (body1 == otherObject) : body1 + "; " + otherObject;
		if (manifoldPtr == null) {
			manifoldPtr = dispatcher.getNewManifold(body0, body1);
		}
		if (manifoldPtr.getBody0() != body0 || manifoldPtr.getBody1() != body1) {

			dispatcher.releaseManifold(manifoldPtr);
			System.err.println("[COMPOUNDECOLLISION] Exception: wrong manifold: \n" +
					"----> " + manifoldPtr.getBody0() + " != " + body0 + " or \n" +
					"----> " + manifoldPtr.getBody1() + " != " + body1);
			//			manifoldPtr.init(body0, body1, 0);

			ObjectArrayList<CollisionObject> collisionObjectArray = ((RigidBodySegmentController) compoundObject).getSegmentController().getPhysics().getDynamicsWorld().getCollisionObjectArray();
			for (int i = 0; i < collisionObjectArray.size(); i++) {
				System.err.println("OBJECTS LISTED " + collisionObjectArray.getQuick(i));
			}
			manifoldPtr = dispatcher.getNewManifold(body0, body1);
		}
		resultOut.setPersistentManifold(manifoldPtr);
		assert(compoundObject != null);
		assert(compoundObject.getCollisionShape() != null);
		assert (compoundObject.getCollisionShape().isCompound()) : compoundObject + "; other " + otherObject;
		if(!(compoundObject.getCollisionShape() instanceof CompoundShape)) {
//			System.err.println("ERROR: obj: "+compoundObject+"; other: "+otherObject);
		}
		
		if(body0.getCollisionShape() instanceof CubesCompoundShape && 
				body1.getCollisionShape() instanceof CubesCompoundShape){
			compoundObject.getWorldTransform(v.tmpTrans0);
			otherObject.getWorldTransform(v.tmpTrans1);
			
			v.sweeper.debug = !((GamePhysicsObject)compoundObject).getSimpleTransformableSendableObject().isOnServer();
			
			v.sweeper.fill(
					((CubesCompoundShape)body0.getCollisionShape()), v.tmpTrans0, 
					((CubesCompoundShape)body1.getCollisionShape()), v.tmpTrans1, 
					((CubesCompoundShape)body0.getCollisionShape()).getChildList(), 
					((CubesCompoundShape)body1.getCollisionShape()).getChildList());
			
			v.sweeper.getOverlapping();
			
			List<OverlappingSweepPair<CompoundShapeChild>> pairs = v.sweeper.pairs;
			
			final int pairCount = pairs.size();
			for(int p = 0; p < pairCount; p++){
				OverlappingSweepPair<CompoundShapeChild> pair = pairs.get(p);
				
				Transform tmpTrans = v.tmpTrans;
				Transform orgTrans = v.orgTrans;
				Transform childTrans = v.chieldTrans;
				Transform orgInterpolationTrans = v.interpolationTrans;
				Transform newChildWorldTrans = v.newChildWorldTrans;
				
				
				// temporarily exchange parent btCollisionShape with childShape, and recurse
				CollisionShape childShape = pair.a.seg.childShape;//compoundShape.getChildShape(i);
	
				
				// backup
				compoundObject.getWorldTransform(orgTrans);
				compoundObject.getInterpolationWorldTransform(orgInterpolationTrans);

				if(compoundObject.getCollisionShape() instanceof CompoundShape compound) compound.getChildTransform(((CubesCompoundShapeChild)pair.a.seg).tmpChildIndex, childTrans);
	
				newChildWorldTrans.set(orgTrans);
	
				Matrix4fTools.transformMul(newChildWorldTrans, childTrans);
	
				compoundObject.setWorldTransform(newChildWorldTrans);
				compoundObject.setInterpolationWorldTransform(newChildWorldTrans);
	
				// the contactpoint is still projected back using the original inverted worldtrans
				CollisionShape tmpShape = compoundObject.getCollisionShape();
				compoundObject.internalSetTemporaryCollisionShape(childShape);
				
				//other --------------------------------------
				CollisionAlgorithm algoO = childCollisionAlgorithmsCOM.getQuick(((CubesCompoundShapeChild)pair.b.seg).tmpChildIndex);
				
				Transform tmpTransO = v.tmpTransO;
				Transform orgTransO = v.orgTransO;
				Transform childTransO = v.chieldTransO;
				Transform orgInterpolationTransO = v.interpolationTransO;
				Transform newChildWorldTransO = v.newChildWorldTransO;

				CollisionShape childShapeO = pair.b.seg.childShape;//compoundShapeOther.getChildShape(j);
				
				// backup
				otherObject.getWorldTransform(orgTransO);
				otherObject.getInterpolationWorldTransform(orgInterpolationTransO);

				CompoundShape compoundShapeOther = (CompoundShape) otherObject.getCollisionShape();
				compoundShapeOther.getChildTransform(((CubesCompoundShapeChild)pair.b.seg).tmpChildIndex, childTransO);
//				childTransO.set(pair.b.seg.transform);
				
				
				newChildWorldTransO.set(orgTransO);
				Matrix4fTools.transformMul(newChildWorldTransO, childTransO);

				otherObject.setWorldTransform(newChildWorldTransO);
				otherObject.setInterpolationWorldTransform(newChildWorldTransO);

				// the contactpoint is still projected back using the original inverted worldtrans
				CollisionShape tmpShapeO = otherObject.getCollisionShape();
				otherObject.internalSetTemporaryCollisionShape(childShapeO);
				CollisionObject colObj = compoundObject; //swapped ? otherObject : compoundObject;
				CollisionObject otherObj = otherObject; //swapped ? compoundObject : otherObject;

				assert (algoO != null) : colObj + " -----VS---- " + otherObj;
				algoO.processCollision(colObj, otherObj, dispatchInfo, resultOut);

				otherObject.internalSetTemporaryCollisionShape(tmpShapeO);
				otherObject.setWorldTransform(orgTransO);
				otherObject.setInterpolationWorldTransform(orgInterpolationTransO);
				
				compoundObject.internalSetTemporaryCollisionShape(tmpShape);
				compoundObject.setWorldTransform(orgTrans);
				compoundObject.setInterpolationWorldTransform(orgInterpolationTrans);
	
				resultOut.refreshContactPoints();
			}
			
		}else{
		
		
		

			Transform tmpTrans = v.tmpTrans;
			Transform orgTrans = v.orgTrans;
			Transform childTrans = v.chieldTrans;
			Transform orgInterpolationTrans = v.interpolationTrans;
			Transform newChildWorldTrans = v.newChildWorldTrans;

			int numChildren = (compoundObject.getCollisionShape() instanceof CompoundShape compound) ? compound.getNumChildShapes() : 0;//childCollisionAlgorithms.size();
			int i;
			int alt = 0;
			
			int collisions = 0;
			for (i = 0; i < numChildren; i++) {
				// temporarily exchange parent btCollisionShape with childShape, and recurse
//				CollisionShape childShape = compoundShape.getChildShape(i);
	
				// backup
				compoundObject.getWorldTransform(orgTrans);
				compoundObject.getInterpolationWorldTransform(orgInterpolationTrans);

				if(compoundObject.getCollisionShape() instanceof CompoundShape compound) compound.getChildTransform(i, childTrans);
	
				newChildWorldTrans.set(orgTrans);
	
				Matrix4fTools.transformMul(newChildWorldTrans, childTrans);
	
				compoundObject.setWorldTransform(newChildWorldTrans);
				compoundObject.setInterpolationWorldTransform(newChildWorldTrans);
	
				// the contactpoint is still projected back using the original inverted worldtrans
				CollisionShape tmpShape = compoundObject.getCollisionShape();
				if(compoundObject.getCollisionShape() instanceof CompoundShape compound) compoundObject.internalSetTemporaryCollisionShape(compound.getChildShape(i));
	
				if (otherObject.getCollisionShape().isCompound()) {
					CompoundShape compoundShapeOther = (CompoundShape) otherObject.getCollisionShape();
					for (int j = 0; j < compoundShapeOther.getNumChildShapes(); j++) {
						assert (alt < childCollisionAlgorithmsCOM.size()) : compoundObject + " ---- " + otherObject + "  ->>>>>>>>> " + childCollisionAlgorithmsCOM;
						CollisionAlgorithm algoO = childCollisionAlgorithmsCOM.getQuick(alt);
	
						Transform tmpTransO = v.tmpTransO;
						Transform orgTransO = v.orgTransO;
						Transform childTransO = v.chieldTransO;
						Transform orgInterpolationTransO = v.interpolationTransO;
						Transform newChildWorldTransO = v.newChildWorldTransO;
	
						CollisionShape childShapeO = compoundShapeOther.getChildShape(j);
	
						// backup
						otherObject.getWorldTransform(orgTransO);
						otherObject.getInterpolationWorldTransform(orgInterpolationTransO);
	
						compoundShapeOther.getChildTransform(j, childTransO);
	
						newChildWorldTransO.set(orgTransO);
						Matrix4fTools.transformMul(newChildWorldTransO, childTransO);
	
						otherObject.setWorldTransform(newChildWorldTransO);
						otherObject.setInterpolationWorldTransform(newChildWorldTransO);
	
						// the contactpoint is still projected back using the original inverted worldtrans
						CollisionShape tmpShapeO = otherObject.getCollisionShape();
						otherObject.internalSetTemporaryCollisionShape(childShapeO);
						CollisionObject colObj = compoundObject; //swapped ? otherObject : compoundObject;
						CollisionObject otherObj = otherObject; //swapped ? compoundObject : otherObject;
	
						assert (algoO != null) : colObj + " -----VS---- " + otherObj;
						algoO.processCollision(colObj, otherObj, dispatchInfo, resultOut);
	
						otherObject.internalSetTemporaryCollisionShape(tmpShapeO);
						otherObject.setWorldTransform(orgTransO);
						otherObject.setInterpolationWorldTransform(orgInterpolationTransO);
						alt++;
						collisions++;
					}
				} else {
					CollisionAlgorithm algo = childCollisionAlgorithms.getQuick(i);
					if (compoundObject.getCollisionShape() instanceof CubeShape && otherObject.getCollisionShape() instanceof CubeShape) {
						CollisionAlgorithm algorithm = dispatcher.findAlgorithm(compoundObject, otherObject, manifoldPtr);
						algorithm.processCollision(compoundObject, otherObject, dispatchInfo, resultOut);
					} else {
						algo.processCollision(compoundObject, otherObject, dispatchInfo, resultOut);
					}
					collisions++;
				}
				// revert back
				compoundObject.internalSetTemporaryCollisionShape(tmpShape);
				compoundObject.setWorldTransform(orgTrans);
				compoundObject.setInterpolationWorldTransform(orgInterpolationTrans);
	
				resultOut.refreshContactPoints();
			}
		}
	}

	@Override
	public float calculateTimeOfImpact(CollisionObject body0, CollisionObject body1, DispatcherInfo dispatchInfo, ManifoldResult resultOut) {
		//		CollisionObject colObj = isSwapped ? body1 : body0;
		//		CollisionObject otherObj = isSwapped ? body0 : body1;

		assert (compoundObject.getCollisionShape().isCompound());

		CompoundShape compoundShape = (CompoundShape) compoundObject.getCollisionShape();

		// We will use the OptimizedBVH, AABB tree to cull potential child-overlaps
		// If both proxies are Compound, we will deal with that directly, by performing sequential/parallel tree traversals
		// given Proxy0 and Proxy1, if both have a tree, Tree0 and Tree1, this means:
		// determine overlapping nodes of Proxy1 using Proxy0 AABB against Tree1
		// then use each overlapping node AABB against Tree0
		// and vise versa.

		Transform tmpTrans = v.tmpTrans;//new @Stack Transform();
		Transform orgTrans = v.orgTrans;//new @Stack Transform();
		Transform childTrans = v.chieldTrans;//new @Stack Transform();
		float hitFraction = 1f;

		int numChildren = childCollisionAlgorithms.size();
		int i;
		for (i = 0; i < numChildren; i++) {
			// temporarily exchange parent btCollisionShape with childShape, and recurse
			CollisionShape childShape = compoundShape.getChildShape(i);

			// backup
			compoundObject.getWorldTransform(orgTrans);

			compoundShape.getChildTransform(i, childTrans);
			//btTransform	newChildWorldTrans = orgTrans*childTrans ;
			tmpTrans.set(orgTrans);
			tmpTrans.mul(childTrans);
			compoundObject.setWorldTransform(tmpTrans);

			CollisionShape tmpShape = compoundObject.getCollisionShape();
			compoundObject.internalSetTemporaryCollisionShape(childShape);
			float frac = childCollisionAlgorithms.getQuick(i).calculateTimeOfImpact(compoundObject, otherObject, dispatchInfo, resultOut);
			if (frac < hitFraction) {
				hitFraction = frac;
			}
			// revert back
			compoundObject.internalSetTemporaryCollisionShape(tmpShape);
			compoundObject.setWorldTransform(orgTrans);
		}
		return hitFraction;
	}

	@Override
	public void getAllContactManifolds(ObjectArrayList<PersistentManifold> manifoldArray) {
		for (int i = 0; i < childCollisionAlgorithms.size(); i++) {
			childCollisionAlgorithms.getQuick(i).getAllContactManifolds(manifoldArray);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CompoundAlgo[" + compoundObject + "->" + otherObject + "]";
	}

	public static class CreateFunc extends CollisionAlgorithmCreateFunc {

		@Override
		public CollisionAlgorithm createCollisionAlgorithm(CollisionAlgorithmConstructionInfo ci, CollisionObject body0, CollisionObject body1) {
			CompoundCollisionAlgorithmExt algo = new CompoundCollisionAlgorithmExt();
			algo.compoundObject = body0;
			algo.otherObject = body1;
			algo.init(ci);
			return algo;
		}

		@Override
		public void releaseCollisionAlgorithm(CollisionAlgorithm algo) {
			//			pool.release((CompoundCollisionAlgorithmExt)algo);
			CompoundCollisionAlgorithmExt a = (CompoundCollisionAlgorithmExt) algo;
			//			a.v.pool.release(a);
		}
	}

	public static class SwappedCreateFunc extends CollisionAlgorithmCreateFunc {
		private final ObjectPool<CompoundCollisionAlgorithmExt> pool = ObjectPool.get(CompoundCollisionAlgorithmExt.class);

		@Override
		public CollisionAlgorithm createCollisionAlgorithm(CollisionAlgorithmConstructionInfo ci, CollisionObject body0, CollisionObject body1) {
			CompoundCollisionAlgorithmExt algo = new CompoundCollisionAlgorithmExt();//pool.get();
			//			if(body1.getCollisionShape() instanceof CubesCompoundShape && body0.getCollisionShape() instanceof CubesCompoundShape){
			//				algo.compoundObject = body0;
			//				algo.otherObject =  body1;
			//			}else{
			algo.compoundObject = body1;
			algo.otherObject = body0;
			//			}
			algo.init(ci);
			algo.swapped = true;
			return algo;
		}

		@Override
		public void releaseCollisionAlgorithm(CollisionAlgorithm algo) {
			//			pool.release((CompoundCollisionAlgorithmExt)algo);
			CompoundCollisionAlgorithmExt a = (CompoundCollisionAlgorithmExt) algo;
			//			a.v.pool.release(a);
		}
	}
	//	@Override
	//	public void processCollision(CollisionObject body0, CollisionObject body1, DispatcherInfo dispatchInfo, ManifoldResult resultOut) {
	//
	//
	//
	//
	//		if(body0.getCollisionShape() instanceof CubeShape && body1.getCollisionShape() instanceof CubeShape){
	////			CollisionObject colObj = isSwapped ? body1 : body0;
	////			CollisionObject otherObj = isSwapped ? body0 : body1;
	////			CollisionAlgorithm algorithm = dispatcher.findAlgorithm(colObj, otherObj);
	////			algorithm.processCollision(colObj, otherObj, dispatchInfo, resultOut);
	//			throw new RuntimeException("Illegal Physics State");
	//			assert(false);
	//		}else{
	//			System.err.println("SWAPPEDSDPSSKLNLNK "+isSwapped);
	//
	//			super.processCollision(body0, body1, dispatchInfo, resultOut);
	//
	//		}
	//	};
}
