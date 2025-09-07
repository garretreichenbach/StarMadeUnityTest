package org.schema.game.common.controller.elements.warpgate;

import com.bulletphysics.collision.broadphase.BroadphasePair;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.linearmath.AabbUtil2;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.effects.RaisingIndication;
import org.schema.game.client.view.gui.shiphud.HudIndicatorOverlay;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.Planet;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.power.PowerAddOn;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.physics.PairCachingGhostObjectAlignable;
import org.schema.game.common.data.physics.RigidBodySegmentController;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.DebugBox;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;
import org.schema.schine.network.server.ServerMessage;

import javax.vecmath.Vector3f;

public class WarpgateUnit extends ElementCollection<WarpgateUnit, WarpgateCollectionManager, WarpgateElementManager> {

	Vector3i min = new Vector3i();
	Vector3i max = new Vector3i();
	Vector3f minf = new Vector3f();
	Vector3f maxf = new Vector3f();
	Vector3f minOut = new Vector3f();
	Vector3f maxOut = new Vector3f();
	Vector3f minOtherOut = new Vector3f();
	Vector3f maxOtherOut = new Vector3f();
	Vector3f minBoxOther = new Vector3f(1, 1, 1);
	Vector3f maxBoxOther = new Vector3f(1, 1, 1);
	private boolean inGraphics;
	/**
	 * update the valid gate
	 */
	private long lastPopup;
	private Transform otherTrans = new Transform();
	private Transform otherTransBef = new Transform();
	private Transform invTrans = new Transform();
	private Transform invTransBef = new Transform();
	private int xDelta;
	private int yDelta;
	private int zDelta;
	private boolean xDim;
	private boolean yDim;
	private boolean zDim;
	private final Vector3f normal = new Vector3f();
	private final float[] param = new float[1];
	public float getPowerNeeded(SimpleTransformableSendableObject forJumpEntity) {

		return (forJumpEntity.getMass()) * WarpgateElementManager.POWER_NEEDED_PER_MASS;
	}

	public float getPowerConsumption() {

		return (size()) * WarpgateElementManager.POWER_CONST_NEEDED_PER_BLOCK;
	}

	@Override
	public boolean isValid() {
//		getMin(min);
//		getMax(max);
//		int x = max.x - min.x;
//		int y = max.y - min.y;
//		int z = max.z - min.z;
//
//		boolean xDim = x == 1 && y > 1 && z > 1;
//		boolean yDim = x > 1 && y == 1 && z > 1;
//		boolean zDim = x > 1 && y > 1 && z == 1;
		//super valid checks the neighbor count to be exactly 2
		return (xDim || yDim || zDim) && super.isValid();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "WarpgateUnit " + super.toString();
	}

	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return elementCollectionManager.getElementManager().getGUIUnitValues(this, elementCollectionManager, supportCol, effectCol);
	}

	public String getValidInfo() {

//		getMin(min);
//		getMax(max);
//
//		int x = max.x - min.x;
//		int y = max.y - min.y;
//		int z = max.z - min.z;
//
//		boolean xDim = x == 1 && y > 1 && z > 1;
//		boolean yDim = x > 1 && y == 1 && z > 1;
//		boolean zDim = x > 1 && y > 1 && z == 1;

		boolean dimOk = (xDim || yDim || zDim);

		return "DimOK: " + dimOk + " (" + xDelta+ ", " + yDelta + ", " + zDelta + "); 2Neighbors: " + super.isValid();
	}

	/**
	 * @return the inGraphics
	 */
	public boolean isInGraphics() {
		return inGraphics;
	}

	/**
	 * @param inGraphics the inGraphics to set
	 */
	public void setInGraphics(boolean inGraphics) {
		this.inGraphics = inGraphics;
	}
	private boolean consumePowerToJumpOk(Timer timer){
		if(getSegmentController().isUsingPowerReactors()){
			return elementCollectionManager.getPowered() >= 0.999999999f;
		}else{
			PowerAddOn powerAddOn = ((PowerManagerInterface) ((ManagedSegmentController<?>) getSegmentController()).getManagerContainer()).getPowerAddOn();
			float powerNeed = getPowerConsumption() * timer.getDelta();
			return powerAddOn.canConsumePowerInstantly(powerNeed) && powerAddOn.consumePowerInstantly(powerNeed);
		}
	}
	public void update(Timer timer) {
		assert (isValid());
		
		if (getSegmentController().isOnServer()) {
			if (consumePowerToJumpOk(timer)) {
				boolean calcedMinMax = false;
				for (int i = 0; i < getSegmentController().getPhysics().getDynamicsWorld().getBroadphase().getOverlappingPairCache().getNumOverlappingPairs(); i++) {
					BroadphasePair broadphasePair = getSegmentController().getPhysics().getDynamicsWorld().getBroadphase().getOverlappingPairCache().getOverlappingPairArray().get(i);

					RigidBodySegmentController other = null;
					PairCachingGhostObjectAlignable otherCharacter = null;

					if (broadphasePair.pProxy0.clientObject == getSegmentController().getPhysicsDataContainer().getObject()) {
						if (broadphasePair.pProxy1.clientObject instanceof RigidBodySegmentController) {
							other = (RigidBodySegmentController) broadphasePair.pProxy1.clientObject;
						} else if (broadphasePair.pProxy1.clientObject instanceof PairCachingGhostObjectAlignable) {
							otherCharacter = (PairCachingGhostObjectAlignable) broadphasePair.pProxy1.clientObject;
						}
					} else if (broadphasePair.pProxy1.clientObject == getSegmentController().getPhysicsDataContainer().getObject()) {
						if (broadphasePair.pProxy0.clientObject instanceof RigidBodySegmentController) {
							other = (RigidBodySegmentController) broadphasePair.pProxy0.clientObject;
						} else if (broadphasePair.pProxy0.clientObject instanceof PairCachingGhostObjectAlignable) {
							otherCharacter = (PairCachingGhostObjectAlignable) broadphasePair.pProxy0.clientObject;
						}
					}

					if (other != null) {
						if (!calcedMinMax) {
							getMin(min);
							getMax(max);
							minf.set(min.x - SegmentData.SEG_HALF, min.y - SegmentData.SEG_HALF, min.z - SegmentData.SEG_HALF);
							maxf.set(max.x - SegmentData.SEG_HALF, max.y - SegmentData.SEG_HALF, max.z - SegmentData.SEG_HALF);
							AabbUtil2.transformAabb(minf, maxf, 0.0f, getSegmentController().getWorldTransform(), minOut, maxOut);
							calcedMinMax = true;
						}

						other.getAabb(minOtherOut, maxOtherOut);
						getBeforAndAfter(other);
						param[0] = 1;
						normal.set(0,0,0);
						boolean bbTest = AabbUtil2.testAabbAgainstAabb2(minOut, maxOut, minOtherOut, maxOtherOut) ;
						boolean rayTest = AabbUtil2.rayAabb(otherTransBef.origin, otherTrans.origin, minOut, maxOut, param, normal);
						if (bbTest || rayTest) {
							narrowTest(other);
						}

					} else if (otherCharacter != null) {
						if (!calcedMinMax) {
							getMin(min);
							getMax(max);
							minf.set(min.x - SegmentData.SEG_HALF, min.y - SegmentData.SEG_HALF, min.z - SegmentData.SEG_HALF);
							maxf.set(max.x - SegmentData.SEG_HALF, max.y - SegmentData.SEG_HALF, max.z - SegmentData.SEG_HALF);
							AabbUtil2.transformAabb(minf, maxf, 0.0f, getSegmentController().getWorldTransform(), minOut, maxOut);
							calcedMinMax = true;
						}

						otherCharacter.getCollisionShape().getAabb(otherCharacter.getWorldTransform(new Transform()), minOtherOut, maxOtherOut);
						getBeforAndAfter(otherCharacter);
						param[0] = 1;
						normal.set(0,0,0);
						boolean bbTest = AabbUtil2.testAabbAgainstAabb2(minOut, maxOut, minOtherOut, maxOtherOut) ;
						boolean rayTest = AabbUtil2.rayAabb(otherTransBef.origin, otherTrans.origin, minOut, maxOut, param, normal);
						
						if (bbTest || rayTest) {
							narrowTest(otherCharacter);
						}
					}
				}
			} else {
//								System.err.println("NO POWER "+elementCollectionManager.getControllerPos());
			}
		} else {
			if (!inGraphics) {
				//notify graphics system of new or changed (identified with controlblock)

			}

			boolean powered = consumePowerToJumpOk(timer);
			if (!getSegmentController().isOnServer() && ((GameClientState) getSegmentController().getState()).getCurrentSectorId() == getSegmentController().getSectorId()) {
				if (!powered) {
					if (System.currentTimeMillis() - lastPopup > 5000) {
						
						Transform t = new Transform();
						t.setIdentity();
						Vector3i p = elementCollectionManager.getControllerPos();
						t.origin.set(p.x - SegmentData.SEG_HALF, p.y - SegmentData.SEG_HALF, p.z - SegmentData.SEG_HALF);
						getSegmentController().getWorldTransform().transform(t.origin);
						
						RaisingIndication raisingIndication;
						if(isUsingPowerReactors()){
							raisingIndication = new RaisingIndication(t, Lng.str("Underpowered: %s%%",StringTools.formatPointZero(elementCollectionManager.getPowered()*100d)), 1f, 0.1f, 0.1f, 1f);
						}else{
							PowerAddOn powerAddOn = ((PowerManagerInterface) ((ManagedSegmentController<?>) getSegmentController()).getManagerContainer()).getPowerAddOn();
							raisingIndication = new RaisingIndication(t, Lng.str("Insufficient Energy\nNeeded/Sec vs PowerRecharge/sec:\n") + StringTools.formatPointZero(getPowerConsumption()) + " / " + StringTools.formatPointZero(powerAddOn.getRecharge()), 1f, 0.1f, 0.1f, 1f);
						}
						raisingIndication.speed = 0.2f;
						raisingIndication.lifetime = 1.0f;
						HudIndicatorOverlay.toDrawTexts.add(raisingIndication);

						
						lastPopup = System.currentTimeMillis();
					}
				} else {
					if (System.currentTimeMillis() - lastPopup > 5000 && elementCollectionManager.isActive()) {
						Transform t = new Transform();
						t.setIdentity();
						Vector3i p = elementCollectionManager.getControllerPos();
						t.origin.set(p.x - SegmentData.SEG_HALF, p.y - SegmentData.SEG_HALF, p.z - SegmentData.SEG_HALF);
						getSegmentController().getWorldTransform().transform(t.origin);
						RaisingIndication raisingIndication = new RaisingIndication(t, Lng.str("Warpgate Ready"), 0.2f, 1.0f, 0.2f, 1f);
						raisingIndication.speed = 0.2f;
						raisingIndication.lifetime = 1.0f;
						HudIndicatorOverlay.toDrawTexts.add(raisingIndication);

						lastPopup = System.currentTimeMillis();
					}
				}
			}
		}
	}

	public void getBeforAndAfter(CollisionObject o) {
		if (o instanceof RigidBodySegmentController) {
			RigidBodySegmentController other = (RigidBodySegmentController) o;
			otherTrans.set(other.getSegmentController().getPhysicsDataContainer().thisTransform);
			otherTransBef.set(other.getSegmentController().getPhysicsDataContainer().lastTransform);

		} else if (o instanceof PairCachingGhostObjectAlignable) {
			PairCachingGhostObjectAlignable other = (PairCachingGhostObjectAlignable) o;

			otherTrans.set(other.getObj().getPhysicsDataContainer().thisTransform);
			otherTransBef.set(other.getObj().getPhysicsDataContainer().lastTransform);
		}
		invTrans.set(getSegmentController().getWorldTransformInverse());
		invTrans.mul(otherTrans);

		invTransBef.set(getSegmentController().getWorldTransformInverse());
		invTransBef.mul(otherTransBef);
	}

	public void debugDraw(Vector3i block) {
		debugDraw(block.x, block.y, block.z);
	}

	public void debugDraw(int x, int y, int z) {
		if (EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()) {
			float scale = 0.51f;
			Transform t = new Transform(getSegmentController().getWorldTransform());
			Vector3f p = new Vector3f();
			p.set(x, y, z);
			p.x -= SegmentData.SEG_HALF;
			p.y -= SegmentData.SEG_HALF;
			p.z -= SegmentData.SEG_HALF;
			t.basis.transform(p);
			t.origin.add(p);
			DebugBox bo = new DebugBox(new Vector3f(-scale, -scale, -scale), new Vector3f(scale, scale, scale), t, 1, 0, 0, 1);
			bo.LIFETIME = 200;
			DebugDrawer.boxes.add(bo);
		}
	}

	private void narrowTest(CollisionObject other) {
		getBeforAndAfter(other);

		Vector3f intersectLinePlane = null;

		Vector3f a = new Vector3f(invTransBef.origin);
		Vector3f b = new Vector3f(invTrans.origin);

//		System.err.println("BEFORE AND AFTER: "+a+" -> "+b+"; "+a.equals(b));

		b.sub(a);
		if (b.lengthSquared() > 0) {
			//normal
			if (max.x - min.x == 1) {
				intersectLinePlane = Vector3fTools.intersectLinePLane(invTransBef.origin, invTrans.origin, new Vector3f(minf.x, 0, 0), new Vector3f(1, 0, 0));
				if (intersectLinePlane != null) {

					if (intersectLinePlane.y > maxf.y || intersectLinePlane.y < minf.y || intersectLinePlane.z > maxf.z || intersectLinePlane.z < minf.z) {
						// no intersection!
					} else {

						//next calc the block

						Vector3i block = new Vector3i(
								(Math.round(intersectLinePlane.x)) + SegmentData.SEG_HALF, 
								(Math.round(intersectLinePlane.y)) + SegmentData.SEG_HALF, 
								(Math.round(intersectLinePlane.z)) + SegmentData.SEG_HALF);

						debugDraw(block);
						//check top to bottom
						int okA = 0;
						int y = block.y + 1;
						while (y <= max.y) {
							debugDraw(block.x, y, block.z);
							long index = getIndex(block.x, y, block.z);
							if (getNeighboringCollection().contains(index)) {
								okA++;
							}
							y++;
						}
						int okB = 0;
						if (okA % 2 == 1) {
							y = block.y - 1;
							while (y >= min.y) {
								debugDraw(block.x, y, block.z);
								long index = getIndex(block.x, y, block.z);
								if (getNeighboringCollection().contains(index)) {
									okB++;
								}
								y--;
							}
						}

						if (okA % 2 == 1 && okB % 2 == 1) {
							if (EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()) {
								float scale = 0.51f;
								Transform t = new Transform(getSegmentController().getWorldTransform());
								Vector3f p = new Vector3f();
								p.set(block.x, block.y, block.z);
								p.x -= SegmentData.SEG_HALF;
								p.y -= SegmentData.SEG_HALF;
								p.z -= SegmentData.SEG_HALF;
								t.basis.transform(p);
								t.origin.add(p);
								DebugBox bo = new DebugBox(new Vector3f(-scale, -scale, -scale), new Vector3f(scale, scale, scale), t, 0, 0, 1, 1);
								bo.LIFETIME = 200;
								DebugDrawer.boxes.add(bo);
							}
							System.err.println("X#################### WARP!!!!!!!!!!!!! " + intersectLinePlane);
							warp(other);
						} else {
							System.err.println("X-------------------- WARP FAILED ON GATE: DirA intersection " + okA + "; DirB intersection: " + okB);
						}
					}
				}
			} else if (max.y - min.y == 1) {
				intersectLinePlane = Vector3fTools.intersectLinePLane(invTransBef.origin, invTrans.origin, new Vector3f(0, minf.y, 0), new Vector3f(0, 1, 0));
				if (intersectLinePlane != null) {
					if (intersectLinePlane.x > maxf.x || intersectLinePlane.x < minf.x || intersectLinePlane.z > maxf.z || intersectLinePlane.z < minf.z) {
						// no intersection!
//						System.err.println("NOINTERSECT "+invTransBef.origin+" -> "+invTrans.origin);
					} else {
						Vector3i block = new Vector3i((Math.round(intersectLinePlane.x)) + SegmentData.SEG_HALF, (Math.round(intersectLinePlane.y)) + SegmentData.SEG_HALF, (Math.round(intersectLinePlane.z)) + SegmentData.SEG_HALF);

						debugDraw(block);
						//check top to bottom
						int okA = 0;
						int x = block.x + 1;
						while (x <= max.x) {
							debugDraw(x, block.y, block.z);
							long index = getIndex(x, block.y, block.z);
							if (getNeighboringCollection().contains(index)) {
								okA++;
							}
							x++;
						}
						int okB = 0;
						if (okA % 2 == 1) {
							x = block.x - 1;
							while (x >= min.x) {
								debugDraw(x, block.y, block.z);
								long index = getIndex(x, block.y, block.z);
								if (getNeighboringCollection().contains(index)) {
									okB++;
								}
								x--;
							}
						}

						if (okA % 2 == 1 && okB % 2 == 1) {
							if (EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()) {
								float scale = 0.51f;
								Transform t = new Transform(getSegmentController().getWorldTransform());
								Vector3f p = new Vector3f();
								p.set(block.x, block.y, block.z);
								p.x -= SegmentData.SEG_HALF;
								p.y -= SegmentData.SEG_HALF;
								p.z -= SegmentData.SEG_HALF;
								t.basis.transform(p);
								t.origin.add(p);
								DebugBox bo = new DebugBox(new Vector3f(-scale, -scale, -scale), new Vector3f(scale, scale, scale), t, 0, 0, 1, 1);
								bo.LIFETIME = 200;
								DebugDrawer.boxes.add(bo);
							}
							System.err.println("Y#################### WARP!!!!!!!!!!!!! " + intersectLinePlane);
							warp(other);
						} else {
							System.err.println("Y-------------------- WARP FAILED ON GATE: DirA intersection " + okA + "; DirB intersection: " + okB);
						}

					}

				} else {
//					if(getSegmentController().isOnServer()){
//						System.err.println("NOINTERSECT "+invTransBef.origin+" -> "+invTrans.origin+"; "+elementCollectionManager.getControllerPos());
//					}
				}
			} else {
				intersectLinePlane = Vector3fTools.intersectLinePLane(invTransBef.origin, invTrans.origin, new Vector3f(0, 0, minf.z), new Vector3f(0, 0, 1));
				if (intersectLinePlane != null) {
					if (intersectLinePlane.x > maxf.x || intersectLinePlane.x < minf.x || intersectLinePlane.y > maxf.y || intersectLinePlane.y < minf.y) {
						// no intersection!
					} else {

						Vector3i block = new Vector3i(
								(Math.round(intersectLinePlane.x)) + SegmentData.SEG_HALF, 
								(Math.round(intersectLinePlane.y)) + SegmentData.SEG_HALF, 
								(Math.round(intersectLinePlane.z)) + SegmentData.SEG_HALF);
//						System.err.println("INTERSECTION "+block);
						debugDraw(block);
						//check top to bottom
						int okA = 0;
						int y = block.y + 1;
						while (y <= max.y) {
							debugDraw(block.x, y, block.z);
							long index = getIndex(block.x, y, block.z);
							if (getNeighboringCollection().contains(index)) {
								//								System.err.println("FOUND AT: "+block.x+", "+ y+", "+block.z);
								okA++;
							}
							y++;
						}
						int okB = 0;
						if (okA % 2 == 1) {
							y = block.y - 1;
							while (y >= min.y) {
								debugDraw(block.x, y, block.z);
								long index = getIndex(block.x, y, block.z);
								if (getNeighboringCollection().contains(index)) {
									okB++;
								}
								y--;
							}
						}

						if (okA % 2 == 1 && okB % 2 == 1) {

							if (EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()) {
								float scale = 0.51f;
								Transform t = new Transform(getSegmentController().getWorldTransform());
								Vector3f p = new Vector3f();
								p.set(block.x, block.y, block.z);
								p.x -= SegmentData.SEG_HALF;
								p.y -= SegmentData.SEG_HALF;
								p.z -= SegmentData.SEG_HALF;
								t.basis.transform(p);
								t.origin.add(p);
								DebugBox bo = new DebugBox(new Vector3f(-scale, -scale, -scale), new Vector3f(scale, scale, scale), t, 0, 0, 1, 1);
								bo.LIFETIME = 200;
								DebugDrawer.boxes.add(bo);
							}
							System.err.println("Z#################### WARP!!!!!!!!!!!!! " + intersectLinePlane);
							warp(other);
						} else {
							System.err.println("Z-------------------- WARP FAILED ON GATE: DirA intersection " + okA + "; DirB intersection: " + okB);
						}

					}

				}
			}
		}

		//		System.err.println("WARP: "+intersectLinePlane+"; <- "+minf+"; "+maxf+"; "+invTransBef.origin+"; "+invTrans.origin);

	}
	private void warp(CollisionObject o) {
		System.err.println("[WARPGATE] WARPING " + o);
		String destUID = elementCollectionManager.getWarpDestinationUID();
		Vector3i local = elementCollectionManager.getLocalDestination();
		int maxDist = elementCollectionManager.getMaxDistance();
		boolean active = elementCollectionManager.isActive();
		boolean allowDirect = getConfigManager().apply(StatusEffectType.WARP_FREE_TARGET, false);
		if (o instanceof RigidBodySegmentController) {
			RigidBodySegmentController other = (RigidBodySegmentController) o;
			if(active) {
				PowerAddOn powerAddOn = ((PowerManagerInterface) ((ManagedSegmentController<?>) getSegmentController()).getManagerContainer()).getPowerAddOn();
				if (getSegmentController().isUsingPowerReactors() || powerAddOn.consumePowerInstantly(getPowerNeeded(other.getSegmentController()))) {
					System.err.println("[WARPGATE] " + getSegmentController().getState() + " warping object: " + other);

					other.getSegmentController().engageWarp(destUID, allowDirect, 4000, local, maxDist);
				} else {
					other.getSegmentController().sendControllingPlayersServerMessage(Lng.astr("Cannot jump!\nNot enough power in gate!\nPower needed for mass: \n", getPowerNeeded(other.getSegmentController())), ServerMessage.MESSAGE_TYPE_ERROR);
				}
			} else {
				other.getSegmentController().sendControllingPlayersServerMessage(Lng.astr("Cannot jump!\nGate is deactivated!\nGate controller needs logic signal to activate."), ServerMessage.MESSAGE_TYPE_WARNING);
			}

		} else if (o instanceof PairCachingGhostObjectAlignable) {
			PairCachingGhostObjectAlignable other = (PairCachingGhostObjectAlignable) o;
			if(active) {
				PowerAddOn powerAddOn = ((PowerManagerInterface) ((ManagedSegmentController<?>) getSegmentController()).getManagerContainer()).getPowerAddOn();

				if (other.getAttached() == null || (other.getAttached() instanceof SpaceStation || other.getAttached() instanceof Planet)) {
					if (getSegmentController().isUsingPowerReactors() || powerAddOn.consumePowerInstantly(1)) {
						System.err.println("[WARPGATE] " + getSegmentController().getState() + " warping object: " + other);
						other.getObj().engageWarp(destUID, allowDirect, 500, local, maxDist);
					} else {
						other.getObj().sendControllingPlayersServerMessage(Lng.astr("Cannot jump!\nNot enough power in gate!\nPower needed for mass: 1"), ServerMessage.MESSAGE_TYPE_ERROR);
					}
				} else {
					other.getObj().sendControllingPlayersServerMessage(Lng.astr("Cannot jump!\nCan only jump while free floating\nor in gravity/aligned to a station."), ServerMessage.MESSAGE_TYPE_ERROR);
				}
			} else {
				other.getObj().sendControllingPlayersServerMessage(Lng.astr("Cannot jump!\nGate is deactivated!\nGate controller needs logic signal to activate."), ServerMessage.MESSAGE_TYPE_WARNING);
			}

		}

	}
	

	@Override
	public void calculateExtraDataAfterCreationThreaded(long updateSignture, LongOpenHashSet totalCollectionSet) {
		
		getMin(min);
		getMax(max);

		xDelta = max.x - min.x;
		yDelta = max.y - min.y;
		zDelta = max.z - min.z;

		xDim = xDelta == 1 && yDelta > 1 && zDelta > 1;
		yDim = xDelta > 1 && yDelta == 1 && zDelta > 1;
		zDim = xDelta > 1 && yDelta > 1 && zDelta == 1;

	}
}
