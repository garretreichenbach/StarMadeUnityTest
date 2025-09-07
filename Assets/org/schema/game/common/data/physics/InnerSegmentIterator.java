package org.schema.game.common.data.physics;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.common.util.linAlg.Quat4fTools;
import org.schema.common.util.linAlg.TransformTools;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;
import org.schema.schine.graphicsengine.forms.debug.DebugPoint;
import org.schema.schine.physics.ClosestRayCastResultExt;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld.LocalRayResult;
import com.bulletphysics.collision.narrowphase.ConvexCast.CastResult;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.CompoundShapeChild;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

public class InnerSegmentIterator implements SegmentTraversalInterface<SegmentController> {

	public ClosestRayCastResultExt rayResult;
	public CubeRayVariableSet v;
	public boolean debug;
	public int tests;
	public Segment currentSeg;
	public SegmentController segmentController;
	public Transform fromA;
	public Transform toA;
	public Transform testCubes;
	public CastResult result;
	public boolean hitSignal;
	protected CollisionObject collisionObject;
	private short lastUpdateNumDrawPP;
	
	protected boolean isZeroHpPhysical(SegmentData data0, int eA) {
		return rayResult.isZeroHpPhysical() || (data0.getHitpointsByte(eA) > 0 || data0.getType(eA) != ElementKeyMap.CORE_ID);
	}
	
	@Override
	public boolean handle(int absX, int absY, int absZ, RayTraceGridTraverser traverser) {
		SegmentController controller = segmentController;

		int x = (absX - currentSeg.pos.x) + SegmentData.SEG_HALF;
		int y = (absY - currentSeg.pos.y) + SegmentData.SEG_HALF;
		int z = (absZ - currentSeg.pos.z) + SegmentData.SEG_HALF;
//		if (debug) {
//			traverser.drawDebug(absX+SegmentData.SEG_HALF, absY+SegmentData.SEG_HALF, absZ+SegmentData.SEG_HALF, tests, controller.getWorldTransform());
//		}
		tests++;
		
		SegmentData data0 = currentSeg.getSegmentData();
		if (x >= 0 && x < SegmentData.SEG && y >= 0 && y < SegmentData.SEG && z >= 0 && z < SegmentData.SEG) {
			int infoIndex = SegmentData.getInfoIndex((byte) x, (byte) y, (byte) z);
			short type = data0.getType(infoIndex);
			if(debug ) {
				System.err.println("[INNER] CHECKING "+x+"; "+y+"; "+z+": TYPE "+type+"; Abs "+(currentSeg.pos.x+x)+", "+(currentSeg.pos.y+y)+", "+(currentSeg.pos.z+z));
			}
			
			
//			assert(false):type+"; ibt "+rayResult.isIgnoreBlockType(type)+"; inp "+rayResult.isIgnoereNotPhysical()+"; prt "+ElementInformation.isPhysicalRayTests(type, data0, infoIndex)+"; zhp "+isZeroHpPhysical(data0, infoIndex);
			if (type > 0 && !rayResult.isIgnoreBlockType(type) && (rayResult.isIgnoereNotPhysical() || ElementInformation.isPhysicalRayTests(type, data0, infoIndex) && isZeroHpPhysical(data0, infoIndex))) {

				v.elemA.set((byte) x, (byte) y, (byte) z);
				v.elemPosA.set(
						v.elemA.x - SegmentData.SEG_HALF,
						v.elemA.y - SegmentData.SEG_HALF,
						v.elemA.z - SegmentData.SEG_HALF);

				v.elemPosA.x += currentSeg.pos.x;
				v.elemPosA.y += currentSeg.pos.y;
				v.elemPosA.z += currentSeg.pos.z;


				v.nA.set(v.elemPosA);
				v.tmpTrans3.set(testCubes);
				v.tmpTrans3.basis.transform(v.nA);
				v.tmpTrans3.origin.add(v.nA);
				
				ElementInformation info = ElementKeyMap.getInfoFast(type);
				int orientationOrig = currentSeg.getSegmentData().getOrientation(infoIndex);
				int orientation = orientationOrig;
				if(type == ElementKeyMap.CARGO_SPACE){
					orientation = Element.TOP;
					if(orientationOrig == 4 && ( currentSeg.getSegmentController().isOnServer() || !((GameClientState)currentSeg.getSegmentController().getState()).isInAnyStructureBuildMode())){
						return true;
					}else{
						//full block in build mode
						orientationOrig = 0;
					}
				}
				//only do simple test on actual cubes
				if(rayResult.isSimpleRayTest() && !info.blockStyle.solidBlockStyle && info.getSlab(orientationOrig) == 0){
					float fraction = Vector3fTools.length(fromA.origin, v.tmpTrans3.origin) / Vector3fTools.length(fromA.origin, toA.origin);
					
					if(fraction < rayResult.closestHitFraction){
						if (rayResult.isHasCollidingBlockFilter()) {
							assert (data0 != null);
							assert (rayResult.getCollidingBlocks() != null);
							LongOpenHashSet filteredBlocks = rayResult.getCollidingBlocks().get(v.cubesB.getSegmentBuffer().getSegmentController().getId());
							if (filteredBlocks.contains(data0.getSegment().getAbsoluteIndex(v.elemA.x, v.elemA.y, v.elemA.z))) {
								//this block in in the filter
								return true;
							}
						}
						rayResult.closestHitFraction = fraction;
						rayResult.setSegment(data0.getSegment());
						rayResult.getCubePos().set(v.elemA);
						rayResult.hitPointWorld.set(v.tmpTrans3.origin);
						rayResult.hitNormalWorld.sub(fromA.origin, toA.origin);
						
						FastMath.normalizeCarmack(rayResult.hitNormalWorld);
						rayResult.collisionObject = collisionObject;
						if (rayResult.isRecordAllBlocks()) {
//							System.err.println("RECORDED ADD "+v.elemA);
							v.record.blockAbsIndices.add(data0.getSegment().getAbsoluteIndex(v.elemA.x, v.elemA.y, v.elemA.z));
							v.record.datas.add(data0);
							v.record.blockLocalIndices.add(infoIndex);
						}
						if (!rayResult.isRecordAllBlocks() || v.record.size() >= v.recordAmount) {
							if(debug){
								System.err.println("COTR:: isRecordALl: "+rayResult.isRecordAllBlocks());
								if(v.record != null){
									System.err.println("COTR REC:: isRecordALl: "+v.record.size()+"; "+v.recordAmount);
								}
							}
							hitSignal = true;
							return false;
						}else{
							//continue
							return true;
						}
					}
				}
				
				
				BlockStyle blockStyle = info.getBlockStyle();
				boolean active = currentSeg.getSegmentData().isActive(infoIndex);
				
				v.simplexSolver.reset();
				v.box0.setMargin(0.01f);

				
				CollisionShape cShape = v.box0;
				if(info.lodUseDetailCollision) {
					cShape = info.lodDetailCollision.getShape(type, (byte) orientation, v.lodBlockTransform);
				}else if (info.blockStyle.solidBlockStyle) {
					cShape = BlockShapeAlgorithm.getShape(blockStyle, (byte) orientation);
				}
				Transform boxTransform = v.tmpTrans3;
				if(info.getSlab(orientationOrig) > 0){
					boxTransform = v.BT;
					boxTransform.set(v.tmpTrans3);
					
					v.orientTT.set(Element.DIRECTIONSf[Element.switchLeftRight(orientation%6)]);
					boxTransform.basis.transform(v.orientTT);
					switch(info.getSlab(orientationOrig)) {
						case 1 -> {
							v.orientTT.scale(0.125f);
							cShape = v.box34[orientation % 6];
						}
						case 2 -> {
							v.orientTT.scale(0.25f);
							cShape = v.box12[orientation % 6];
						}
						case 3 -> {
							v.orientTT.scale(0.375f);
							cShape = v.box14[orientation % 6];
						}
					}
					boxTransform.origin.sub(v.orientTT);
					
				}
				boolean res = false;

				if(cShape == null) {
					System.err.println("[PHYSICS][ERROR] InnerSegmentIterator: Shape null: UseDetail: "+info.lodUseDetailCollision+"; Type: "+info.lodDetailCollision.type.name()+"; Block: "+info.name+" ("+info.id+")");
					cShape = v.box0;
				}
				if(cShape instanceof CompoundShape) {
					CompoundShape cs = (CompoundShape)cShape;
					
					for(int c = 0; c < cs.getNumChildShapes() && !res; c++) {
						CompoundShapeChild child = cs.getChildList().get(c);
						
//						child.transform.basis.setIdentity();
						Matrix4fTools.transformMul(v.lodBlockTransform, child.transform);
						
						v.boxETransform.set(boxTransform);
						Matrix4fTools.transformMul(v.boxETransform, v.lodBlockTransform);//mul local tranform of compound child
						
						ConvexShape shape = (ConvexShape)child.childShape;
						shape.setMargin(0);
						
						ContinuousConvexCollision convexCaster = new ContinuousConvexCollision(v.shapeA, shape, v.simplexSolver, v.gjkEpaPenetrationDepthSolver);
						res = convexCaster.calcTimeOfImpact(fromA, toA, v.boxETransform, v.boxETransform, result, v.gjkVar);
						
						if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn() && !segmentController.isOnServer() && ((GameClientState)segmentController.getState()).getNumberOfUpdate() > lastUpdateNumDrawPP+10) {
							
//							Quat4f s = new Quat4f();
//							Quat4fTools.set(child.transform.basis, s);
//							System.err.println("QUAT: "+s);
							ConvexHullShapeExt e = (ConvexHullShapeExt)shape;
							
							Vector4f color = new Vector4f(1,1,1,1);
							if(res) {
								color.set(1,0,0f,1);
								for(Vector3f p : e.getPoints()) {
									Vector3f dp = new Vector3f(p);
									v.boxETransform.transform(dp);
									
									DebugPoint debugPoint = new DebugPoint(dp, color, 0.1f);
									debugPoint.LIFETIME = 1000;
									DebugDrawer.points.add(debugPoint);
									
								}
							
							}else if(c == 0) {
								color.set(0.3f,0.7f,0.8f,1);
							}else if(c == 1) {
								color.set(0,1,0,1);
							}else if(c == 2) {
								color.set(0,0,1,1);
							}else if(c == 3) {
								color.set(1,0,1,1);
							}else if(c == 4) {
								color.set(0,1,1,1);
							}else if(c == 6) {
								color.set(1,1,0,1);
							}
							
						}
						
						
						//for loop will not continue if res is true
					}
					
				}else {
					/*
					 * GJK seems better than subsimplex cast. subsimplex produces
					 * strange hitFractions
					 */
					if(v.shapeA == null || cShape == null) {
						throw new NullPointerException("Physics shape null: "+v.shapeA+"; "+cShape);
					}
					ContinuousConvexCollision convexCaster = new ContinuousConvexCollision(v.shapeA, (ConvexShape)cShape, v.simplexSolver, v.gjkEpaPenetrationDepthSolver);
					res = convexCaster.calcTimeOfImpact(fromA, toA, boxTransform, boxTransform, result, v.gjkVar);
				}
				if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn() && !segmentController.isOnServer() && ((GameClientState)segmentController.getState()).getNumberOfUpdate() > lastUpdateNumDrawPP+10) {
					lastUpdateNumDrawPP = ((GameClientState)segmentController.getState()).getNumberOfUpdate();
				}
				if (res) {

					if (rayResult.isHasCollidingBlockFilter()) {
						assert (data0 != null);
						assert (rayResult.getCollidingBlocks() != null);
						LongOpenHashSet filteredBlocks = rayResult.getCollidingBlocks().get(v.cubesB.getSegmentBuffer().getSegmentController().getId());
						assert (filteredBlocks != null);
						assert (data0.getSegment() != null);
						assert (v.elemA != null);
						if (filteredBlocks.contains(data0.getSegment().getAbsoluteIndex(v.elemA.x, v.elemA.y, v.elemA.z))) {
							//this block in in the filter
							return true;
						}
					} else {
					}

					if (result.normal.lengthSquared() > 0.000001f) {

						if (rayResult.isRecordAllBlocks()) {
							v.record.blockAbsIndices.add(data0.getSegment().getAbsoluteIndex(v.elemA.x, v.elemA.y, v.elemA.z));
							v.record.datas.add(data0);
							v.record.blockLocalIndices.add(infoIndex);
						}

						if (result.fraction < rayResult.closestHitFraction) {
							assert (data0.getSegment() != null) : "SEGMENT NULL OF DATA: " + data0 + " ";

							rayResult.setSegment(data0.getSegment());
							rayResult.getCubePos().set(v.elemA);
							//rotate normal into worldspace
							fromA.basis.transform(result.normal);

							result.normal.normalize();
							LocalRayResult localRayResult = new LocalRayResult(
									collisionObject,
									null,
									result.normal,
									result.fraction);
							//						System.err.println("REAL! COLLISION WITH ELEMENT POS: "+v.elemPosA);
							boolean normalInWorldSpace = true;
							rayResult.addSingleResult(localRayResult, normalInWorldSpace);

							assert (!rayResult.hasHit() || rayResult.getSegment() != null);

							if (!rayResult.isRecordAllBlocks() || v.record.size() >= v.recordAmount) {

								hitSignal = true;
								//return false to not continue the traverse
								return false;
							}
						} 
					} 
				}
			}
		}

		return true;
	}		
	@Override
	public SegmentController getContextObj() {
		return segmentController;
	}

	public boolean onOuterSegmentHitTest(Segment sOuter, boolean hadHit) {
		return true;
	}



}
