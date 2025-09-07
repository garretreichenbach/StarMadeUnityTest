package org.schema.game.common.controller;

import java.util.Set;

import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementDocking;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.BoxBoxDetector;
import org.schema.game.common.data.physics.CubesCompoundShape;
import org.schema.game.common.data.physics.ModifiedDynamicsWorld;
import org.schema.game.common.data.physics.PhysicsExt;
import org.schema.game.common.data.physics.octree.ArrayOctree;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.DebugBoundingBox;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;
import org.schema.schine.network.objects.Sendable;

import com.bulletphysics.collision.broadphase.CollisionAlgorithm;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.ManifoldResult;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.linearmath.AabbUtil2;
import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.Transform;

public class SegmentControllerElementCollisionChecker {

	private static ThreadLocal<SegmentControllerElementCollisionCheckerVariables> threadLocal =
			new ThreadLocal<SegmentControllerElementCollisionCheckerVariables>() {
				@Override
				protected SegmentControllerElementCollisionCheckerVariables initialValue() {
					return new SegmentControllerElementCollisionCheckerVariables();
				}
			};
	BoxBoxDetector d = new BoxBoxDetector();
	boolean hasBlockInRange;

	private SegmentController ownSegmentController;

	private SegmentControllerElementCollisionCheckerVariables v;

	public SegmentControllerElementCollisionChecker(SegmentController controller) {
		this.ownSegmentController = controller;
		this.v = threadLocal.get();

	}

	public boolean checkAABBCollisionWithSelf(Transform trans, Vector3f localMin, Vector3f localMax, float margin) {

		assert(!ownSegmentController.isOnServer()):"Uses client transform";
		
		
		AabbUtil2.transformAabb(localMin, localMax, margin, trans, v.tmpMinA, v.tmpMaxA);
		
		CubesCompoundShape cc = ((CubesCompoundShape) ownSegmentController.getPhysicsDataContainer().getShape());
		cc.getChildList().get(0).childShape.getAabb(
				ownSegmentController.getWorldTransformOnClient(), v.tmpMinB, v.tmpMaxB);

		return AabbUtil2.testAabbAgainstAabb2(v.tmpMinA, v.tmpMaxA, v.tmpMinB, v.tmpMaxB);
	}
	public boolean checkAABBCollisionWithUnrelatedStructures(Transform trans, Vector3f localMin, Vector3f localMax, float margin) {
		
		assert(!ownSegmentController.isOnServer()):"Uses client transform";
		AabbUtil2.transformAabb(localMin, localMax, margin, trans, v.tmpMinA, v.tmpMaxA);
		
		GameClientState state = (GameClientState) ownSegmentController.getState();
		for (SimpleTransformableSendableObject<?> s : state.getCurrentSectorEntities().values()) {
			if (s.isHidden() || !(s instanceof SegmentController)) {
				continue;
			}
			SegmentController c = (SegmentController)s;
			
			if(c.railController.isInAnyRailRelationWith(ownSegmentController)){
				//only look at unrelated objects
				continue;
			}
			
			CollisionShape cc = c.getPhysicsDataContainer().getShapeChild().childShape;
			cc.getAabb(c.getWorldTransformOnClient(), v.tmpMinB, v.tmpMaxB);
			
			if(AabbUtil2.testAabbAgainstAabb2(v.tmpMinA, v.tmpMaxA, v.tmpMinB, v.tmpMaxB)){
				System.err.println("[COL] BB ovelapping of "+ownSegmentController+" with "+c+"; ["+v.tmpMinA+" - "+v.tmpMaxA+"] ;; ["+v.tmpMinB+" - "+v.tmpMaxB+"]");
				return true;
			}
		}
		
		
		return false;
	}

	public boolean checkSectorCollisionWithChildShapeExludingRails() {
		
		
		v.ownPos.set(ownSegmentController.getWorldTransform());
		
		
		
		GameServerState state = (GameServerState) ownSegmentController.getState();
		for (Sendable snd : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
			if(!(snd instanceof SimpleTransformableSendableObject<?>)){
				continue;
			}
			SimpleTransformableSendableObject<?> s = (SimpleTransformableSendableObject<?>)snd;
			if (s.isHidden() || s.getSectorId() != ownSegmentController.getSectorId()) {
				continue;
			}
			
			
			if (s instanceof SegmentController) {
				
				SegmentController other = (SegmentController)s;
				if(other.railController.isInAnyRailRelationWith(ownSegmentController)){
					continue;
				}
				
				if (s == ownSegmentController) {
					continue;
				}
				
				
			}else{
				//ONLY CHECK SEGMENT CONTROLLER, ELSE IT MIGHT CAUSE CLASS CAST EXCEPTIONS
				continue;
			}
			if (s.getPhysicsDataContainer().getObject() != null) {
				SimpleTransformableSendableObject<?> to = (s);
					
//				to.getgetTransformedAABB(v.tmpMinA, v.tmpMaxA, 0, v.tmpMinHelp, v.tmpMaxHelp, null);
//				if (AabbUtil2.testAabbAgainstAabb2(v.tmpMinA, v.tmpMaxA, v.tmpMinB, v.tmpMaxB)) {
					PhysicsExt physics = ownSegmentController.getPhysics();
					
					CollisionObject o = new CollisionObject();
					
					o.setCollisionShape(ownSegmentController.getPhysicsDataContainer().getShapeChild().childShape);
					o.setWorldTransform(ownSegmentController.getWorldTransform());
					o.setInterpolationWorldTransform(ownSegmentController.getWorldTransform());
					
					ManifoldResult r = new ManifoldResult(o, to.getPhysicsDataContainer().getObject());
					CollisionAlgorithm objectQuerySingle = null;
					try {
						objectQuerySingle = ((ModifiedDynamicsWorld) physics.getDynamicsWorld()).objectQuerySingle(to.getPhysicsDataContainer().getObject(), o, r);
						
						PersistentManifold persistentManifold = r.getPersistentManifold();
						
						if (persistentManifold.getNumContacts() > 0) {
							System.err.println("[SECTORCOLLISION] ElementToBuild blocked by " + s + "");
							return true;
						} else {
//							System.err.println("[SECTORCOLLISION] NO CONTACT ------------------------------------- "+s);
						}
					} finally {
						if (objectQuerySingle != null) {
							objectQuerySingle.destroy();
						}
					}
//				}else{
//					System.err.println("NO OVERLAPPING AABB: "+s+" "+ownSegmentController);
//				}
			}
		}
		
		return false;
	}
	public boolean checkPieceCollision(SegmentPiece toPiece, SegmentCollisionCheckerCallback callBack, boolean respectOwnDocks) {
		if(ownSegmentController.isOnServer()) {
			return checkPieceCollisionServer(toPiece.getAbsoluteIndex(), callBack, respectOwnDocks);
		}else {
			return checkPieceCollisionClient(toPiece.getAbsoluteIndex(), callBack, respectOwnDocks);
		}
	}
	public boolean checkPieceCollisionServer(long toPieceIndex, SegmentCollisionCheckerCallback callBack,
			boolean respectOwnDocks) {
		assert (ownSegmentController.isOnServer());
		
		v.ownPos.set(ownSegmentController.getWorldTransform());
		ElementCollection.getPosFromIndex(toPieceIndex, v.elemPosA);
		v.elemPosA.x -= SegmentData.SEG_HALF;
		v.elemPosA.y -= SegmentData.SEG_HALF;
		v.elemPosA.z -= SegmentData.SEG_HALF;
		v.ownPos.basis.transform(v.elemPosA);
		v.ownPos.origin.add(v.elemPosA);
		
		GameServerState state = (GameServerState) ownSegmentController.getState();
		
		
		Set<SimpleTransformableSendableObject<?>> entities = ownSegmentController.getRemoteSector().getServerSector().getEntities();
		
		for (SimpleTransformableSendableObject<?> s : entities) {
			boolean col = checkPiceCollision(s, v, callBack, respectOwnDocks);
			if(col) {
				return true;
			}
		}

		return false;
	}

	public boolean checkPieceCollisionClient(long toPieceIndex, SegmentCollisionCheckerCallback callBack, boolean respectOwnDocks) {

		assert (!ownSegmentController.isOnServer());
		
		v.ownPos.set(ownSegmentController.getWorldTransform());
		ElementCollection.getPosFromIndex(toPieceIndex, v.elemPosA);
		v.elemPosA.x -= SegmentData.SEG_HALF;
		v.elemPosA.y -= SegmentData.SEG_HALF;
		v.elemPosA.z -= SegmentData.SEG_HALF;
		v.ownPos.basis.transform(v.elemPosA);
		v.ownPos.origin.add(v.elemPosA);
		
		GameClientState state = (GameClientState) ownSegmentController.getState();
		for (SimpleTransformableSendableObject<?> s : state.getCurrentSectorEntities().values()) {
			boolean col = checkPiceCollision(s, v, callBack, respectOwnDocks);
			if(col) {
				return true;
			}
		}

		return false;
	}
	public boolean checkPiceCollision(SimpleTransformableSendableObject<?> s, SegmentControllerElementCollisionCheckerVariables v, SegmentCollisionCheckerCallback callBack, boolean respectOwnDocks) {
		
		

		SegmentControllerElementCollisionCheckerVariables.boxGhostObject.getAabb(v.ownPos, v.tmpMinB, v.tmpMaxB);
		
		if (s.isHidden()) {
			return false;
		}
		if (s.getProhibitingBuildingAroundOrigin() > 0) {
			Vector3fTools.clamp(v.closest, v.tmpMinB, v.tmpMaxB);
	
			v.closest.sub(s.getWorldTransformOnClient().origin);
	
			if (v.closest.length() < s.getProhibitingBuildingAroundOrigin()) {
				callBack.userData = "Reason: '" + s.toNiceString() + "'\nis using a Build Prohibiter";
				//blocked by build prohibiter
				return true;
			}
	
		}
	
		if (s instanceof SegmentController) {
			if (s == ownSegmentController) {
				return false;
			}
			SegmentController c = ((SegmentController) s);
			if(!respectOwnDocks && c.railController.isInAnyRailRelationWith(ownSegmentController)){
				return false;
			}
			if (c.getSectorId() != ownSegmentController.getSectorId()) {
				return false;
			}
			if (checkSegmentController(c, v.ownPos, 0.00f, false, false)) {
				callBack.userData = c;
				return true;
			}
	
		} else if (s.getPhysicsDataContainer().getObject() != null) {
			SimpleTransformableSendableObject to = (s);
			if (ownSegmentController.getSectorId() == to.getSectorId()) {
	
				to.getTransformedAABB(v.tmpMinA, v.tmpMaxA, 0, v.tmpMinHelp, v.tmpMaxHelp, null);
				if (AabbUtil2.testAabbAgainstAabb2(v.tmpMinA, v.tmpMaxA, v.tmpMinB, v.tmpMaxB)) {
					PhysicsExt physics = ownSegmentController.getPhysics();
	
					CollisionObject o = new CollisionObject();
	
					o.setCollisionShape(SegmentControllerElementCollisionCheckerVariables.boxGhostObject);
					o.setWorldTransform(v.ownPos);
					o.setInterpolationWorldTransform(v.ownPos);
	
					ManifoldResult r = new ManifoldResult(o, to.getPhysicsDataContainer().getObject());
					CollisionAlgorithm objectQuerySingle = null;
					try {
						objectQuerySingle = ((ModifiedDynamicsWorld) physics.getDynamicsWorld()).objectQuerySingle(to.getPhysicsDataContainer().getObject(), o, r);
	
						PersistentManifold persistentManifold = r.getPersistentManifold();
	
						if (persistentManifold.getNumContacts() > 0) {
							System.err.println("[PIECECOLLISION] ElementToBuild blocked by " + s + "");
							callBack.userData = to;
							return true;
						} else {
							System.err.println("NO CONTACT -------------------------------------");
						}
					} finally {
						if (objectQuerySingle != null) {
							objectQuerySingle.destroy();
						}
					}
				}
			}
		}
		return false;
	}
	private boolean checkSegmentController(SegmentController c, Transform t, float margin, boolean aabbMode, boolean usePointInsteadOfHalfAABB) {
		c.getPhysicsDataContainer().getShapeChild().childShape.getAabb(c.isOnServer() ? c.getWorldTransform() : c.getWorldTransformOnClient(), v.tmpMinA, v.tmpMaxA);

		boolean aabbcheck = AabbUtil2.testAabbAgainstAabb2(v.tmpMinA, v.tmpMaxA, v.tmpMinB, v.tmpMaxB);
		if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()){
			DebugBoundingBox bbA = new DebugBoundingBox(new Vector3f(v.tmpMinA), new Vector3f(v.tmpMaxA), 1, 0, 0, 1);
			DebugBoundingBox bbB = new DebugBoundingBox(new Vector3f(v.tmpMinB), new Vector3f(v.tmpMaxB), 0, 0, 1, 1);
			DebugDrawer.boundingBoxes.addElement(bbA);
			DebugDrawer.boundingBoxes.addElement(bbB);
		}
		if (aabbMode) {
			System.err.println("[COLLISIONCHECKER] PRE-AABB TEST " + ownSegmentController + " -> " + c + " AT AABB[" + v.tmpMinA + ", " + v.tmpMaxA + " | " + v.tmpMinB + ", " + v.tmpMaxB + "] -> " + aabbcheck + "; " + ownSegmentController.getState());
		}
//		System.err.println("AABB CHECK BETWEEN "+ownSegmentController+" and "+c+": "+aabbcheck);
		if (aabbcheck) {

			
			
			IntersectionIterator callBack = new IntersectionIterator();
			callBack.collision = false;
			callBack.margin = margin;
			callBack.t = new Transform(t);
			callBack.aabb = aabbMode;
			callBack.usePointInsteadOfHalfAABB = usePointInsteadOfHalfAABB;
			//			callBack.t.origin.x -= SegmentData.SEG_HALF;
			//			callBack.t.origin.y -= SegmentData.SEG_HALF;
			//			callBack.t.origin.z -= SegmentData.SEG_HALF;
			c.getSegmentBuffer().iterateOverNonEmptyElement(callBack, true);

			//			System.err.println("[COLLISIONCHECKER] CHECKING "+ownSegmentController+" -> "+c+" AT ["+t.origin+"; AABB "+aabbMode+"] -> "+callBack.collision+"; "+ownSegmentController.getState());
			return callBack.collision;
		}
		if (!c.getDockingController().getDockedOnThis().isEmpty()) {
			for (ElementDocking e : c.getDockingController().getDockedOnThis()) {
				checkSegmentController(e.from.getSegment().getSegmentController(),
						t, margin, aabbMode, false);
			}
		}

		return false;
	}

	private boolean checkSegmentControllerWithRailsRec(SegmentController c, Transform t, float margin, boolean usePointInsteadOfHalfAABB) {
		if(checkSingleSegmentController(c, t, margin, usePointInsteadOfHalfAABB)) {
			return true;
		}
		for(RailRelation r : c.railController.next) {
			if(r.docked.getSegmentController().getCollisionChecker().checkSegmentControllerWithRailsRec(r.docked.getSegmentController(), t, margin, usePointInsteadOfHalfAABB)) {
				return true;
			}
		}
		return false;
	}
	public boolean checkSegmentControllerWithRails(SegmentController c, Transform t, float margin, boolean usePointInsteadOfHalfAABB) {
		return c.railController.getRoot().getCollisionChecker().checkSegmentControllerWithRailsRec(c, t, margin, usePointInsteadOfHalfAABB);
	}
	public boolean checkSingleSegmentController(SegmentController c, Transform t, float margin, boolean usePointInsteadOfHalfAABB) {

		SegmentControllerElementCollisionCheckerVariables.boxGhostObject.getAabb(t, v.tmpMinB, v.tmpMaxB);
		return checkSegmentController(c, t, margin, false, usePointInsteadOfHalfAABB);
	}

	/**
	 * tests if there is an existing block in the AABB relative to the seg controller
	 *
	 * @param min
	 * @param max
	 * @return
	 */
	public boolean existsBlockInAABB(final Vector3i min, final Vector3i max) {

		Vector3i minRange = new Vector3i();
		Vector3i maxRange = new Vector3i();

		minRange.x = ByteUtil.divUSeg((min.x - 1)) * SegmentData.SEG;
		minRange.y = ByteUtil.divUSeg((min.y - 1)) * SegmentData.SEG;
		minRange.z = ByteUtil.divUSeg((min.z - 1)) * SegmentData.SEG;

		maxRange.x = (FastMath.fastCeil((max.x + 1) / SegmentData.SEGf)) * SegmentData.SEG;
		maxRange.y = (FastMath.fastCeil((max.y + 1) / SegmentData.SEGf)) * SegmentData.SEG;
		maxRange.z = (FastMath.fastCeil((max.z + 1) / SegmentData.SEGf)) * SegmentData.SEG;

		//		System.err.println("CHCKING RANGE FROM "+minRange+" TO "+maxRange);

		hasBlockInRange = false;

		ownSegmentController.getSegmentBuffer().iterateOverNonEmptyElementRange((s, lastChanged) -> {
			//				System.err.println("CHECKING COL COL"+s);

			hasBlockInRange = s.hasBlockInRange(min, max);

			//				System.err.println("COLLISION = "+hasBlockInRange);
			if (hasBlockInRange) {
				return false;
			}
			return true;
		}, minRange, maxRange, false);

		return hasBlockInRange;

	}

	public boolean hasSegmentPartialAABBBlock(Segment segment, Vector3i min, Vector3i max) {

		SegmentData segmentData = segment.getSegmentData();

		if (!v.intersectionCallBack.initialized) {
			v.intersectionCallBack.createHitCache(ArrayOctree.POSSIBLE_LEAF_HITS);
		}
		v.intersectionCallBack.reset();

		v.tmpTrans.setIdentity();

		v.tmpMinB.set(min.x - SegmentData.SEG_HALF, min.y - SegmentData.SEG_HALF, min.z - SegmentData.SEG_HALF);
		v.tmpMaxB.set(max.x - SegmentData.SEG_HALF, max.y - SegmentData.SEG_HALF, max.z - SegmentData.SEG_HALF);

		//		DebugBox bbb = new DebugBox(
		//				new Vector3f(tmpMinB.x, tmpMinB.y, tmpMinB.z),
		//				new Vector3f(tmpMaxB.x, tmpMaxB.y, tmpMaxB.z),
		//				segment.getSegmentController().getWorldTransform(),
		//				1, 1, 1, 1);
		//		DebugDrawer.boxes.add(bbb);

		//		System.err.println("CHECKING INTERSECTION "+tmpMinB+" AND "+tmpMaxB);
		v.absolute.set(v.tmpTrans.basis);
		MatrixUtil.absolute(v.absolute);

		segmentData.getOctree()
				.findIntersectingAABB(segmentData.getOctree().getSet(), v.intersectionCallBack, segment, v.tmpTrans, v.absolute, 0.0f, v.tmpMinB, v.tmpMaxB, 1.0f);
		//		System.err.println("OCTREE HITS: "+intersectionCallBack.hitCount);

		if (v.intersectionCallBack.hitCount > 0) {
			for (int i = 0; i < v.intersectionCallBack.hitCount; i++) {
				v.intersectionCallBack.getHit(i, v.ctmpMinA, v.ctmpMaxA, v.start, v.end);

				for (byte x = v.start.x; x < v.end.x; x++) {
					for (byte y = v.start.y; y < v.end.y; y++) {
						for (byte z = v.start.z; z < v.end.z; z++) {

							v.elemA.set((byte) (x + SegmentData.SEG_HALF), (byte) (y + SegmentData.SEG_HALF), (byte) (z + SegmentData.SEG_HALF));

							if (segmentData.contains(v.elemA)) {

								//								elemPosB.set(
								//										x + segment.pos.x,
								//										y + segment.pos.y,
								//										z + segment.pos.z);

								v.tmpAbsPos.set(segment.pos.x + (x + SegmentData.SEG_HALF), segment.pos.y + (y + SegmentData.SEG_HALF), segment.pos.z + (z + SegmentData.SEG_HALF));
								if (v.tmpAbsPos.x <= max.x && v.tmpAbsPos.y <= max.y && v.tmpAbsPos.z <= max.z &&
										v.tmpAbsPos.x >= min.x && v.tmpAbsPos.y >= min.y && v.tmpAbsPos.z >= min.z) {

									//									System.err.println("DIRECT COLLISION "+tmpAbsPos);
									return true;
								}

							}

						}
					}
				}
			}
		}
		return false;
	}

	private class IntersectionIterator implements SegmentBufferIteratorInterface {

		public boolean usePointInsteadOfHalfAABB;
		Transform t;
		float margin;
		boolean aabb = false;
		private boolean collision;

		@Override
		public boolean handle(Segment segment, long lastChanged) {
			if (segment == null || segment.isEmpty()) {
				if (aabb) {
					System.err.println("Segment empty " + segment);
				}
				return true;
			}
			SegmentData segmentData = segment.getSegmentData();
			if (!v.intersectionCallBack.initialized) {
				v.intersectionCallBack.createHitCache(ArrayOctree.POSSIBLE_LEAF_HITS);
			}
			v.intersectionCallBack.reset();

			v.absolute.set(segment.getSegmentController().getWorldTransform().basis);
			MatrixUtil.absolute(v.absolute);

			segmentData.getOctree()
					.findIntersectingAABB(segmentData.getOctree().getSet(), v.intersectionCallBack, segment, segment.getSegmentController().getWorldTransform(), v.absolute, 0.0f, v.tmpMinB, v.tmpMaxB, 1.0f);

			if (v.intersectionCallBack.hitCount > 0) {
				//				System.err.println("[COLLISIONCHECKER] SINGLE INTERSECTION: "+v.intersectionCallBack.hitCount+"; AABB "+aabb);
				for (int i = 0; i < v.intersectionCallBack.hitCount; i++) {

					v.intersectionCallBack.getHit(i, v.ctmpMinA, v.ctmpMaxA, v.start, v.end);

					for (byte x = v.start.x; x < v.end.x; x++) {
						for (byte y = v.start.y; y < v.end.y; y++) {
							for (byte z = v.start.z; z < v.end.z; z++) {

								v.elemA.set((byte) (x + SegmentData.SEG_HALF), (byte) (y + SegmentData.SEG_HALF), (byte) (z + SegmentData.SEG_HALF));

								if (segmentData.contains(v.elemA)) {

									v.elemPosB.set(
											x + segment.pos.x,
											y + segment.pos.y,
											z + segment.pos.z);

									v.nA.set(v.elemPosB);
									v.tmpTrans.set(segment.getSegmentController().getWorldTransform());

									v.tmpTrans.basis.transform(v.nA);
									v.tmpTrans.origin.add(v.nA);
									if(usePointInsteadOfHalfAABB){
										SegmentControllerElementCollisionCheckerVariables.boxGhostObject.setMargin(margin);
										SegmentControllerElementCollisionCheckerVariables.boxGhostObject.getAabb(v.tmpTrans, v.tmpMinA, v.tmpMaxA);
										
										v.b.set(v.tmpMinA, v.tmpMaxA);
										if (v.b.isInside(t.origin)) {
//											assert(false);
											this.collision = true;
											return false;
										}else{
										}
									}else{
										SegmentControllerElementCollisionCheckerVariables.boxGhostObject.setMargin(margin);
										SegmentControllerElementCollisionCheckerVariables.boxGhostObject.getAabb(v.tmpTrans, v.tmpMinA, v.tmpMaxA);
										if (aabb) {
											v.tmpMinC.set(v.tmpMinB);
											v.tmpMaxC.set(v.tmpMaxB);
											//										ownSegmentController.getPhysicsDataContainer().getShape().getAabb(t, tmpMinC, tmpMaxC);
											//										System.err.println("##AABB CHECK: ["+v.tmpMinA+", "+ v.tmpMaxA+" | "+v.tmpMinC+", "+v.tmpMaxC+"] --> "+AabbUtil2.testAabbAgainstAabb2(v.tmpMinA, v.tmpMaxA, v.tmpMinC, v.tmpMaxC));
										} else {
											SegmentControllerElementCollisionCheckerVariables.boxGhostObject.getAabb(t, v.tmpMinC, v.tmpMaxC);
										}
									}

									//									if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()){
									//										Transform t = new Transform();
									//										t.setIdentity();
									//										DebugBox a = new DebugBox(new Vector3f(v.tmpMinA), new Vector3f(v.tmpMaxA), t,1, 1, 1, 1);
									//										DebugBox b = new DebugBox(new Vector3f(v.tmpMinC), new Vector3f(v.tmpMaxC), t,1, 1, 0, 1);
									//
									//										DebugDrawer.boxes.add(a);
									//										DebugDrawer.boxes.add(b);
									//									}

									if (AabbUtil2.testAabbAgainstAabb2(v.tmpMinA, v.tmpMaxA, v.tmpMinC, v.tmpMaxC)) {

										v.input.init();
										v.input.transformA.set(v.tmpTrans);
										v.input.transformB.set(t);

										v.m = new PersistentManifold();
										v.m.init(v.col0, v.col1, 0);
										v.output.init(v.col0, v.col1);
										v.output.setPersistentManifold(v.m);

										d.GetClosestPoints(
												SegmentControllerElementCollisionCheckerVariables.boxGhostObjectSm,
												SegmentControllerElementCollisionCheckerVariables.boxGhostObjectSm,
												v.input,
												v.output,
												null, false, v.elemPosA, v.elemPosB, ElementKeyMap.HULL_ID, ElementKeyMap.HULL_ID, 0, 0);

//										System.err.println("HIT :::::: "+v.m.getNumContacts());
										if (v.m.getNumContacts() > 0) {
											this.collision = true;
											return false; //don't iterate any further
										}
									}
								}
							}
						}
					}
				}
			}
			return true;
		}

	}
}
