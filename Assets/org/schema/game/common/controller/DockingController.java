package org.schema.game.common.controller;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import javax.vecmath.Matrix3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Quat4Util;
import org.schema.common.util.linAlg.Quat4fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.client.view.camera.InShipCamera;
import org.schema.game.common.Starter;
import org.schema.game.common.controller.elements.ManagerModuleCollection;
import org.schema.game.common.controller.elements.dockingBlock.DockingBlockCollectionManager;
import org.schema.game.common.controller.elements.dockingBlock.DockingBlockElementManager;
import org.schema.game.common.controller.elements.dockingBlock.DockingBlockManagerInterface;
import org.schema.game.common.controller.elements.dockingBlock.DockingBlockUnit;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementDocking;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.CubeShape;
import org.schema.game.common.data.physics.CubesCompoundShape;
import org.schema.game.common.data.physics.RigidBodySegmentController;
import org.schema.game.common.data.player.PlayerCharacter;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.Chunk16SegmentData;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.network.objects.NetworkSegmentController;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.blueprint.DockingTagOverwrite;
import org.schema.schine.ai.stateMachines.AiInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.objects.container.UpdateWithoutPhysicsObjectInterface;
import org.schema.schine.network.objects.remote.UnsaveNetworkOperationException;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.sound.controller.AudioController;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class DockingController implements UpdateWithoutPhysicsObjectInterface {

	private final List<ElementDocking> dockedOnThis = new ObjectArrayList<ElementDocking>();

	private final SegmentController segmentController;

	private final Transform dockingPos = new Transform();

	private final Transform dockingPosInverse = new Transform();

	private final Vector3f size = new Vector3f();

	private final Quat4f fromTmp = new Quat4f();

	private final Quat4f res = new Quat4f();

	public Quat4f targetQuaternion = new Quat4f();

	public DockingTagOverwrite tagOverwrite;

	public Quat4f targetStartQuaternion = new Quat4f();

	Vector3f mod = new Vector3f();

	private ElementDocking dockedOn = null;

	private String delayedDockUID = null;

	private Vector3i delayedDockPos;

	private boolean delayedUndock;

	private long lastDockingOrUndocking;

	private long lastUnDocking;

	private long lastSentMessage;

	private long delayCheck;

	private boolean docked;

	private boolean sizeSetFromTag;

	private boolean loadedFromTag;

	private boolean triggerMotherShipRemoved;

	private long dockingUIDnotFound;

	private boolean updateOnce;

	private Quat4f delayedDockLocalRot;

	private int dockingCode;

	private Quat4f localDockingOrientation = new Quat4f(0, 0, 0, 1);

	public DockingController(SegmentController segmentController) {
		this.segmentController = segmentController;
		dockingPos.setIdentity();
		dockingPosInverse.setIdentity();
		targetStartQuaternion.set(0, 0, 0, 1);
	}

	public static void getDockingTransformation(byte dockingOrientation, Transform out) {
		switch(dockingOrientation) {
			case (Element.TOP):
				;
				break;
			case (Element.BOTTOM):
				out.basis.rotX(FastMath.PI);
				break;
			case (Element.FRONT):
				out.basis.rotX(FastMath.HALF_PI);
				break;
			case (Element.BACK):
				Matrix3f b = new Matrix3f();
				// prevents ships looking like swasticas
				b.rotY(-FastMath.HALF_PI);
				out.basis.rotZ(FastMath.HALF_PI);
				out.basis.mul(b);
				b.rotZ(FastMath.HALF_PI);
				out.basis.mul(b);
				break;
			case (Element.RIGHT):
				out.basis.rotZ(FastMath.HALF_PI);
				break;
			case (Element.LEFT):
				out.basis.rotZ(-FastMath.HALF_PI);
				break;
		}
	}

	public static boolean isTurretDocking(SegmentPiece target) {
		return target.getType() == ElementKeyMap.TURRET_DOCK_ID;
	}

	public static void removeChieldsRecusrive(SegmentController removedSegmentController, CubesCompoundShape targetShape) {
		int count = targetShape.getNumChildShapes();
		targetShape.removeChildShape(removedSegmentController.getPhysicsDataContainer().getShapeChild().childShape);
		if (removedSegmentController.getDockingController().dockedOnThis.size() > 0) {
			// chain
			for (ElementDocking d : removedSegmentController.getDockingController().dockedOnThis) {
				removeChieldsRecusrive(d.from.getSegment().getSegmentController(), targetShape);
			}
		}
	}

	public static void updateTargetWithRemovedController(SegmentController targetSegmentController, SegmentController removedSegmentController) {
		if (targetSegmentController.isMarkedForDeleteVolatile() || !targetSegmentController.getState().getLocalAndRemoteObjectContainer().getLocalObjects().containsKey(targetSegmentController.getId())) {
			System.err.println("[DOCKING] no update needed from undocking for mothership " + targetSegmentController + " -> segmentController has been deleted");
			return;
		}
		Vector3f linearVelobefore = new Vector3f();
		Vector3f angularVelobefore = new Vector3f();
		if (targetSegmentController.getPhysicsDataContainer().getObject() != null && targetSegmentController.getPhysicsDataContainer().getObject() instanceof RigidBody) {
			RigidBody body = (RigidBody) targetSegmentController.getPhysicsDataContainer().getObject();
			body.getLinearVelocity(linearVelobefore);
			body.getAngularVelocity(angularVelobefore);
		}
		// remove this physics object child from the docking-parent object
		targetSegmentController.onPhysicsRemove();
		if (targetSegmentController.getPhysicsDataContainer().lastCenter.length() > 0) {
			// adapt for center of mass only if it didn't start docking
			// and had an update before
			CubesCompoundShape c = (CubesCompoundShape) targetSegmentController.getPhysicsDataContainer().getShape();
			Vector3f centerOfMass = c.getCenterOfMass();
			targetSegmentController.getWorldTransform().basis.transform(centerOfMass);
			targetSegmentController.getWorldTransform().origin.add(centerOfMass);
		}
		if (targetSegmentController.getDockingController().dockedOnThis.size() > 0) {
			CubesCompoundShape targetShape = (CubesCompoundShape) targetSegmentController.getPhysicsDataContainer().getShape();
			removeChieldsRecusrive(removedSegmentController, targetShape);
			if (targetSegmentController.getMass() > 0) {
				targetSegmentController.getPhysicsDataContainer().updateMass(targetSegmentController.getMass(), true);
			}
			RigidBody bodyFromShape = targetSegmentController.getPhysics().getBodyFromShape(targetShape, targetSegmentController.getMass(), targetSegmentController.getWorldTransform());
			bodyFromShape.setUserPointer(targetSegmentController.getId());
			assert (bodyFromShape.getCollisionShape() == targetShape);
			targetSegmentController.getPhysicsDataContainer().setObject(bodyFromShape);
			// set this NEW phyiscs body for all docked entities
			for (ElementDocking doc : targetSegmentController.getDockingController().dockedOnThis) {
				doc.from.getSegment().getSegmentController().getPhysicsDataContainer().setObject(null);
			}
			assert (targetSegmentController.getPhysicsDataContainer().getShape() == targetShape);
			assert (targetSegmentController.getPhysicsDataContainer().getShape() == bodyFromShape.getCollisionShape());
			targetSegmentController.onPhysicsAdd();
			if (targetSegmentController.getMass() > 0) {
				targetSegmentController.getPhysicsDataContainer().updateMass(targetSegmentController.getMass(), true);
			}
		} else {
			System.err.println("[DOCKING] doing complete physics reset for " + targetSegmentController);
			targetSegmentController.getPhysicsDataContainer().setObject(null);
			targetSegmentController.getPhysicsDataContainer().setShape(null);
			targetSegmentController.getPhysicsDataContainer().setShapeChield(null, -1);
			targetSegmentController.getPhysicsDataContainer().initialTransform.set(targetSegmentController.getWorldTransform());
			targetSegmentController.getRemoteTransformable().getInitialTransform().set(targetSegmentController.getWorldTransform());
			targetSegmentController.initPhysics();
			targetSegmentController.onPhysicsAdd();
		// targetSegmentController.setFlagPhysicsInit(true);
		}
		if (targetSegmentController.getPhysicsDataContainer().getObject() != null && targetSegmentController.getPhysicsDataContainer().getObject() instanceof RigidBody) {
			RigidBody body = (RigidBody) targetSegmentController.getPhysicsDataContainer().getObject();
			body.setLinearVelocity(linearVelobefore);
			body.setAngularVelocity(angularVelobefore);
		}
	}

	public static void getDockingTransformationRecursive(SegmentController dock, byte dockingOrientation, Transform doT) {
		Transform t = new Transform();
		t.setIdentity();
		getDockingTransformation(dockingOrientation, t);
		if (dock.getDockingController().docked) {
			getDockingTransformation(dock.getDockingController().dockedOn.to.getOrientation(), doT);
		}
		doT.mul(t);
	}

	public void checkDockingValid() throws CollectionNotLoadedException {
		if (segmentController.isOnServer() || segmentController.isClientOwnObject()) {
			if (segmentController.getDockingController().docked) {
				boolean validDockingBlock = false;
				boolean foundDockedPosition = false;
				SegmentController targetSegmentController = segmentController.getDockingController().dockedOn.to.getSegment().getSegmentController();
				if (targetSegmentController instanceof ManagedSegmentController && ((ManagedSegmentController<?>) targetSegmentController).getManagerContainer() instanceof DockingBlockManagerInterface) {
					DockingBlockManagerInterface<?, ?, ?> c = (DockingBlockManagerInterface<?, ?, ?>) ((ManagedSegmentController<?>) targetSegmentController).getManagerContainer();
					Vector3i toDockPos = segmentController.getDockingController().dockedOn.to.getAbsolutePos(new Vector3i());
					// System.err.println("[DOCKING]["+getState()+"] "+segmentController+" TO DOCKED POS: "+toDockPos+": "+c.getDockingBlock());
					if (isOnServer()) {
						SegmentPiece from = dockedOn.from;
						SegmentPiece to = dockedOn.to;
						SegmentController fromSeg = from.getSegment().getSegmentController();
						SegmentController toSeg = to.getSegment().getSegmentController();
						fromSeg.getSegmentBuffer().getPointUnsave(from.getAbsolutePos(new Vector3i()), from);
						toSeg.getSegmentBuffer().getPointUnsave(to.getAbsolutePos(new Vector3i()), to);
						if (fromSeg == null || toSeg == null) {
							return;
						}
						validDockingBlock = to != null && to.getType() != Element.TYPE_NONE && ElementKeyMap.getInfo(to.getType()).isOldDockable();
						if (!validDockingBlock) {
							System.err.println("[DOCKING] " + segmentController + " -> target is not a valid dockable block: " + (to != null) + ", " + to != null ? (to.getType() != Element.TYPE_NONE) : "null" + ", " + ((to != null && to.getType() != Element.TYPE_NONE) ? ElementKeyMap.getInfo(to.getType()).isOldDockable() : "null"));
						}
					} else {
						// dont test on clients
						validDockingBlock = true;
					}
					for (ManagerModuleCollection<? extends DockingBlockUnit<?, ?, ?>, ? extends DockingBlockCollectionManager<?, ?, ?>, ? extends DockingBlockElementManager<?, ?, ?>> module : c.getDockingBlock()) {
						boolean found = false;
						// System.err.println("TO DOCKED POS COLL: "+module.getCollectionManagers().size());
						try {
							for (DockingBlockCollectionManager<?, ?, ?> colMan : module.getCollectionManagers()) {
								if (colMan.getControllerPos().equals(toDockPos)) {
									foundDockedPosition = true;
									if (!colMan.isObjectDockable(segmentController, segmentController.getDockingController().dockedOn.to.getOrientation(), false) & !((GameStateInterface) getState()).getGameState().isIgnoreDockingArea()) {
										if (isOnServer()) {
											System.err.println("[SERVER] docking invalid: " + segmentController + ": obj not dockable");
											((GameServerState) getState()).getController().broadcastMessageAdmin(Lng.astr("DEBUG-ADMIN ERROR:\n%s cannot dock\nThe target area is invalid to dock", segmentController), ServerMessage.MESSAGE_TYPE_ERROR);
											requestDelayedUndock(true);
										} else if (segmentController.isClientOwnObject()) {
											((GameClientState) segmentController.getState()).getController().popupAlertTextMessage(Lng.str("Size of docked structure\ntoo big for docking area!\nUndocking %s", segmentController.getRealName()), 0);
										}
										found = true;
										break;
									} else {
									}
								}
							}
						} catch (ConcurrentModificationException e) {
							e.printStackTrace();
							System.err.println("Exception could be catched and handeled by deferring docking valid request");
							throw new CollectionNotLoadedException();
						}
						if (found) {
							break;
						}
					}
				} else {
					assert false;
				}
				if (!foundDockedPosition) {
					if (isOnServer()) {
						if (!validDockingBlock) {
							System.err.println("[SERVER] docking invalid: " + segmentController + ": docking module removed");
							requestDelayedUndock(true);
						} else {
							System.err.println("[SERVER] docking valid can not yet be checked. Block is dockable but not in docking manager yet: " + segmentController + "");
						}
					} else if (segmentController.isClientOwnObject() || targetSegmentController.isClientOwnObject()) {
						((GameClientState) segmentController.getState()).getController().popupAlertTextMessage(Lng.str("Docking module has been removed!\n\nUndocking %s", segmentController.getRealName()), 0);
					}
				}
			}
			for (ElementDocking doc : dockedOnThis) {
				doc.from.getSegment().getSegmentController().flagUpdateDocking();
			// System.err.println("[DOCKING]["+getState()+"] Delegating checkValid request "+segmentController+" -> "+doc.to.getSegment().getSegmentController());
			}
		}
	}

	private boolean checkFactionAllowed(SegmentPiece pieceCore, SegmentPiece dockingTarget) {
		if (dockingTarget.getSegment().getSegmentController().getFactionId() == 0) {
			return true;
		}
		boolean sameFaction = pieceCore.getSegment().getSegmentController().getFactionId() == dockingTarget.getSegment().getSegmentController().getFactionId();
		boolean toDockFlownByFactionMember = false;
		if (pieceCore.getSegment().getSegmentController() instanceof PlayerControllable) {
			for (PlayerState p : ((PlayerControllable) pieceCore.getSegment().getSegmentController()).getAttachedPlayers()) {
				if (p.getFactionId() == dockingTarget.getSegment().getSegmentController().getFactionId()) {
					toDockFlownByFactionMember = true;
					break;
				}
			}
		}
		boolean loadedFromTag = this.loadedFromTag;
		this.loadedFromTag = false;
		return !isOnServer() || sameFaction || toDockFlownByFactionMember || loadedFromTag;
	}

	public boolean checkLastDockDelay() {
		return System.currentTimeMillis() - lastDockingOrUndocking > 1000;
	}

	public SegmentController getAbsoluteMother() {
		if (docked) {
			return dockedOn.to.getSegment().getSegmentController().getDockingController().getAbsoluteMother();
		} else {
			return segmentController;
		}
	}

	private boolean dock(SegmentPiece fromControllerPiece, SegmentPiece dockingTarget, boolean publicDockingException, boolean ignoreAlreadyUpdated) throws DockingNotYetUpdatedException {
		if (segmentController.railController.isInAnyRailRelation() || dockingTarget.getSegmentController().railController.isInAnyRailRelation()) {
			segmentController.sendControllingPlayersServerMessage(Lng.astr("Cannot dock!\nCan't mix rails with old dock!"), ServerMessage.MESSAGE_TYPE_ERROR);
			return false;
		}
		assert (segmentController != dockingTarget.getSegment().getSegmentController());
		// for normal docking, those two are the same
		SegmentController targetSegmentController = dockingTarget.getSegment().getSegmentController();
		SegmentController targetAbsoluteMother = dockingTarget.getSegment().getSegmentController();
		this.updateOnce = false;
		if (!dockingTarget.getSegment().getSegmentController().getDockingController().dockedOnThis.isEmpty()) {
			for (ElementDocking ed : dockingTarget.getSegment().getSegmentController().getDockingController().dockedOnThis) {
				if (ed.to.equals(dockingTarget)) {
					segmentController.sendControllingPlayersServerMessage(Lng.astr("Cannot dock!\nDocking block in use by\n", ed.from.getSegment().getSegmentController().toNiceString()), ServerMessage.MESSAGE_TYPE_ERROR);
					lastDockingOrUndocking = System.currentTimeMillis();
					return false;
				}
			}
		}
		// docked local transform is
		// ident for normal docking
		// and the chield transform of the next chain
		// element as its chain
		Transform dockingLocalTransform = new Transform();
		dockingLocalTransform.setIdentity();
		if (dockingTarget.getSegment().getSegmentController().getDockingController().docked) {
			/*
			 * the target we want to dock on is already docked
			 *
			 */
			dockingLocalTransform.set(targetSegmentController.getPhysicsDataContainer().getShapeChild().transform);
			targetAbsoluteMother = dockingTarget.getSegment().getSegmentController().getDockingController().dockedOn.to.getSegment().getSegmentController().getDockingController().getAbsoluteMother();
			if (isTurretDocking(dockingTarget.getSegment().getSegmentController().getDockingController().dockedOn.to)) {
				System.err.println("[DOCKING] ERROR: cannot dock onto docked object (turret chain): " + fromControllerPiece.getSegment().getSegmentController() + " -> " + dockingTarget.getSegment().getSegmentController());
				segmentController.sendControllingPlayersServerMessage(Lng.astr("turret must be on the end\nof the chain (for now)"), ServerMessage.MESSAGE_TYPE_ERROR);
				lastDockingOrUndocking = System.currentTimeMillis();
				return false;
			}
		}
		if (!dockedOnThis.isEmpty()) {
			/*
			 * there are entities docked to us
			 */
			for (int i = 0; i < dockedOnThis.size(); i++) {
				SegmentController alreadyDockedOnThis = dockedOnThis.get(i).from.getSegment().getSegmentController();
				if (!ignoreAlreadyUpdated && !alreadyDockedOnThis.getDockingController().updateOnce) {
					throw new DockingNotYetUpdatedException();
				}
			}
			if (isTurretDocking(dockingTarget)) {
				System.err.println("[DOCKING] ERROR: cannot dock onto docked object (turret chain): " + fromControllerPiece.getSegment().getSegmentController() + " -> " + dockingTarget.getSegment().getSegmentController());
				segmentController.sendControllingPlayersServerMessage(Lng.astr("turret must be on the end\nof the chain (for now)"), ServerMessage.MESSAGE_TYPE_ERROR);
				lastDockingOrUndocking = System.currentTimeMillis();
				return false;
			}
		}
		if (!docked || dockedOn.to.getSegment().getSegmentController() != targetSegmentController) {
			setDockedOn(new ElementDocking(fromControllerPiece, dockingTarget, publicDockingException));
			lastDockingOrUndocking = System.currentTimeMillis();
			ElementDocking elementDocking = new ElementDocking(fromControllerPiece, dockingTarget, publicDockingException);
			if (!targetSegmentController.getDockingController().dockedOnThis.contains(elementDocking)) {
				targetSegmentController.getDockingController().dockedOnThis.add(elementDocking);
			}
			// create actual constraint if necessary
			updateDockingPosition();
			Transform dockingMatrix = new Transform();
			dockingMatrix.setIdentity();
			Vector3i absolutePos = dockedOn.to.getAbsolutePos(new Vector3i());
			dockingMatrix.origin.set(absolutePos.x - SegmentData.SEG_HALF, absolutePos.y - SegmentData.SEG_HALF, absolutePos.z - SegmentData.SEG_HALF);
			switch(dockedOn.to.getOrientation()) {
				case (Element.RIGHT) -> GlUtil.getLeftVector(mod, dockingMatrix);
				case (Element.LEFT) -> GlUtil.getRightVector(mod, dockingMatrix);
				case (Element.TOP) -> GlUtil.getUpVector(mod, dockingMatrix);
				case (Element.BOTTOM) -> GlUtil.getBottomVector(mod, dockingMatrix);
				case (Element.FRONT) -> GlUtil.getForwardVector(mod, dockingMatrix);
				case (Element.BACK) -> GlUtil.getBackVector(mod, dockingMatrix);
			}
			BoundingBox boundingBox = dockedOn.from.getSegment().getSegmentController().getSegmentBuffer().getBoundingBox();
			if ((!sizeSetFromTag && isOnServer()) || size.length() == 0) {
				size.set(boundingBox.min);
			// System.err.println("[DOCKING] SIZE FROM BB "+size);
			} else {
				// do nothing on initial size
				sizeSetFromTag = false;
			// System.err.println("[DOCKING] SIZE FROM TAG "+size);
			}
			// System.err.println("[DOCKING] DOCKING SIZE ON "+getState()+": "+size);
			if (isTurretDocking(dockedOn.to)) {
				mod.scale(1.5f);
			} else {
				float dist = switch(dockedOn.to.getOrientation()) {
					case (Element.RIGHT) -> size.y;
					case (Element.LEFT) -> size.y;
					case (Element.TOP) -> size.y;
					case (Element.BOTTOM) -> size.y;
					case (Element.FRONT) -> size.y;
					case (Element.BACK) -> size.y;
					default -> 0;
				};
				// System.err.println("[DOCK] NOW DOCKING: "+segmentController+": BOUNDING BOX: "+boundingBox+"; DIST: "+dist);
				dist = Math.abs(dist);
				mod.scale(dist + 0.5f);
			}
			dockingMatrix.origin.add(mod);
			// segmentController.getPhysicsDataContainer().getObject().setWorldTransform(getDockingPos());
			segmentController.onPhysicsRemove();
			targetAbsoluteMother.onPhysicsRemove();
			CompoundShape ownShape = (CompoundShape) segmentController.getPhysicsDataContainer().getShape();
			// System.err.println("OWN SHAPE: "+ownShape);
			// for(int i = 0; i < ownShape.getNumChildShapes(); i++){
			// System.err.println("OWN sub "+i+" "+ownShape.getChildShape(i));
			// }
			CompoundShape targetShape = (CompoundShape) targetAbsoluteMother.getPhysicsDataContainer().getShape();
			// System.err.println("TARGET SHAPE: "+targetShape);
			// for(int i = 0; i < targetShape.getNumChildShapes(); i++){
			// System.err.println("TARGET sub "+i+" "+targetShape.getChildShape(i));
			// }
			// targetShape.getChildList().clear();
			assert (targetShape != ownShape) : segmentController + "; " + targetAbsoluteMother;
			// System.err.println("[DOCK] "+segmentController+" "+segmentController.getState()+" CHIELD SHAPES OF DOCKING CONTROLLER: "+ownShape.getNumChildShapes());
			for (int i = 0; i < ownShape.getNumChildShapes(); i++) {
				dockedOn.to.refresh();
				byte orienation = dockedOn.to.getOrientation();
				// for chain docking
				// getChildTransform(i, new Transform());
				Transform lastChildTransform = ownShape.getChildList().get(i).transform;
				Transform dockingTrans = new Transform();
				dockingTrans.setIdentity();
				// chain: is not ident when docking to already docked
				dockingTrans.mul(dockingLocalTransform);
				getDockingTransformation(orienation, dockingMatrix);
				dockingTrans.mul(dockingMatrix);
				// chain: is not ident when docking with already docked to self
				dockingTrans.mul(lastChildTransform);
				// System.err.println("ADDING TO TARGET SHAPE: "+targetShape.getNumChildShapes()+" :::: "+ownShape.getChildShape(i));
				/*
				 *  this should be chain safe as the
				 *  ownShape chields already include everything docked to it
				 *
				 *  and target shape already contains the chain links inbetween
				 *  if docking to an already docked entity
				 */
				targetShape.addChildShape(dockingTrans, ownShape.getChildShape(i));
				System.err.println("[DOCKING] " + getState() + " ADDING: " + i + ": " + ownShape + " -> " + targetShape);
				assert (targetShape != ownShape);
				// System.err.println("SET TARGET QUATERNATION TO "+targetQuaternion);
				if (ownShape.getChildShape(i) instanceof CubeShape) {
					CubeShape cs = (CubeShape) ownShape.getChildShape(i);
					// set the child shape and the target quaternion for the corresponding docking controller
					// from the child shape
					int index = targetShape.getChildList().size() - 1;
					cs.getSegmentController().getPhysicsDataContainer().setShapeChield(targetShape.getChildList().get(index), index);
					assert (targetShape.getChildList().contains(cs.getSegmentController().getPhysicsDataContainer().getShapeChild()));
					Quat4fTools.set(dockingTrans.basis, cs.getSegmentBuffer().getSegmentController().getDockingController().targetStartQuaternion);
					Transform ident = new Transform();
					ident.setIdentity();
					if (lastChildTransform.equals(ident) && dockingLocalTransform.equals(ident) && isTurretDocking(dockingTarget)) {
						// add additional rotation from saved rotation
						Matrix3f mul = new Matrix3f();
						mul.set(new Quat4f(localDockingOrientation));
						mul.mul(dockingTrans.basis);
						dockingTrans.basis.set(mul);
					} else {
						localDockingOrientation.set(0, 0, 0, 1);
					}
					Quat4fTools.set(dockingTrans.basis, cs.getSegmentBuffer().getSegmentController().getDockingController().targetQuaternion);
				}
			}
			targetShape.recalculateLocalAabb();
			System.err.println("[DOCKING] " + segmentController.getState() + " DOCKED ON TARGET SHAPE: " + targetShape);
			float invMass = ((RigidBodySegmentController) segmentController.getPhysicsDataContainer().getObject()).getInvMass();
			segmentController.getPhysicsDataContainer().updateMass(segmentController.getMass(), true);
			float invMass2 = ((RigidBodySegmentController) segmentController.getPhysicsDataContainer().getObject()).getInvMass();
			// System.err.println("[DOCKING] "+segmentController+" MASS: "+segmentController.getMass()+", invBidyMass: "+invMass+"/"+invMass2+" --TO-- "+targetSegmentController+" MASS: "+targetSegmentController.getMass()+"");
			if (segmentController.getSectorId() != targetSegmentController.getSectorId()) {
				segmentController.setSectorId(targetSegmentController.getSectorId());
				if (segmentController instanceof PlayerControllable) {
					PlayerControllable pc = (PlayerControllable) segmentController;
					for (PlayerState ps : pc.getAttachedPlayers()) {
						System.err.println("[SERVER][DOCKING] sector docking on border! " + segmentController + " has players attached. Doing Sector Change for " + ps);
						Vector3i secPos;
						if (isOnServer()) {
							secPos = ((GameServerState) getState()).getUniverse().getSector(targetSegmentController.getSectorId()).pos;
						} else {
							secPos = ((RemoteSector) getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(targetSegmentController.getSectorId())).clientPos();
						}
						ps.setCurrentSector(new Vector3i(secPos));
						ps.setCurrentSectorId(targetSegmentController.getSectorId());
						PlayerCharacter assingedPlayerCharacter = ps.getAssingedPlayerCharacter();
						if (assingedPlayerCharacter != null) {
							System.err.println("[SERVER][DOCKING] sector docking on border! " + secPos + " has CHARACTER. Doing Sector Change for " + assingedPlayerCharacter + ": ");
							assingedPlayerCharacter.setSectorId(targetSegmentController.getSectorId());
						} else {
							System.err.println("[SERVER] WARNING NO PLAYER CHARACTER ATTACHED TO " + ps);
						}
					}
				}
			}
			if (targetAbsoluteMother.getPhysicsDataContainer().lastCenter.length() > 0) {
				// adapt for center of mass only if it didn't start docking
				// and had an update before
				CubesCompoundShape c = (CubesCompoundShape) targetAbsoluteMother.getPhysicsDataContainer().getShape();
				Vector3f centerOfMass = c.getCenterOfMass();
				targetAbsoluteMother.getWorldTransform().basis.transform(centerOfMass);
				targetAbsoluteMother.getWorldTransform().origin.add(centerOfMass);
			}
			RigidBody bodyFromShape = targetAbsoluteMother.getPhysics().getBodyFromShape(targetShape, targetAbsoluteMother.getMass() > 0 ? targetAbsoluteMother.getMass() + segmentController.getMass() : 0, targetAbsoluteMother.getWorldTransform());
			// System.err.println("[DOCKING] ADDED CHILD COMPOUND: "+targetShape.getNumChildShapes()+"; "+targetShape+": InvMass "+bodyFromShape.getInvMass());
			bodyFromShape.setUserPointer(targetAbsoluteMother.getId());
			assert (bodyFromShape.getCollisionShape() == targetShape);
			targetAbsoluteMother.getPhysicsDataContainer().setObject(bodyFromShape);
			// set this NEW phyiscs body for all docked entities
			for (ElementDocking d : targetSegmentController.getDockingController().dockedOnThis) {
				d.from.getSegment().getSegmentController().getPhysicsDataContainer().setObject(null);
			}
			targetAbsoluteMother.getPhysicsDataContainer().updatePhysical(getState().getUpdateTime());
			targetAbsoluteMother.onPhysicsAdd();
			((RigidBodySegmentController) targetAbsoluteMother.getPhysicsDataContainer().getObject()).activate(true);
			// targetSegmentController.setFlagPhysicsInit(true);
			if (segmentController instanceof SendableSegmentController) {
				((SendableSegmentController) segmentController).handleNTDockChanged();
			}
			assert (targetAbsoluteMother.getPhysicsDataContainer().getShape() == targetShape);
			assert (targetAbsoluteMother.getPhysicsDataContainer().getShape() == bodyFromShape.getCollisionShape());
			// System.err.println("[SEGCON] NOW DOCKED ON "+dockingTarget+" "+segmentController+" -> "+dockingTarget.getSegment().getSegmentController()+"  on "+getState());
			if (!isOnServer() && ((GameClientState) getState()).getCurrentSectorId() == segmentController.getSectorId()) {
				/*AudioController.fireAudioEvent("DOCK", new AudioTag[] { AudioTags.GAME, AudioTags.SHIP, AudioTags.DOCKING, AudioTags.DOCK }, AudioParam.ONE_TIME, AudioController.ent(targetSegmentController, dockingTarget, dockingTarget.getAbsoluteIndexWithType4(), fromControllerPiece.getSegmentController().getBoundingBox().min, fromControllerPiece.getSegmentController().getBoundingBox().max))*/
				AudioController.fireAudioEventID(870, AudioController.ent(targetSegmentController, dockingTarget, dockingTarget.getAbsoluteIndexWithType4(), fromControllerPiece.getSegmentController().getBoundingBox().min, fromControllerPiece.getSegmentController().getBoundingBox().max));
			}
			if (isOnServer()) {
				SendableSegmentController s = (SendableSegmentController) dockingTarget.getSegment().getSegmentController();
				Vector3i pos = dockingTarget.getAbsolutePos(new Vector3i());
				System.err.println("[DOCK] ACTIVATE SURROUND SERVER WITH DOCK " + pos);
				s.activateSurroundServer(true, pos, ElementKeyMap.ACTIVAION_BLOCK_ID);
			}
			dockingCode = targetAbsoluteMother.getId();
			Starter.modManager.onSegmentControllerDocking(segmentController);
			// @TODO(sean) I think we need to set the velocity to zero now
			segmentController.flagupdateMass();
			// segmentController.getPhysicsObject().clearForces();
			System.err.println("[DOCK] Speed after docking " + segmentController.getSpeedCurrent());
			if (segmentController.isClientOwnObject()) {
				if (Controller.getCamera() != null && Controller.getCamera() instanceof InShipCamera) {
					((InShipCamera) Controller.getCamera()).docked = true;
				}
			}
			return true;
		}
		return false;
	}

	public boolean isPublicException(SegmentPiece dockingTarget) throws DockingNotYetUpdatedException {
		Vector3i pos = new Vector3i();
		Vector3i posDir = new Vector3i();
		dockingTarget.getAbsolutePos(pos);
		for (int i = 0; i < 6; i++) {
			posDir.add(pos, Element.DIRECTIONSi[i]);
			SegmentPiece pointUnsave;
			pointUnsave = dockingTarget.getSegment().getSegmentController().getSegmentBuffer().getPointUnsave(posDir);
			if (pointUnsave == null) {
				// client only
				throw new DockingNotYetUpdatedException();
			}
			if (pointUnsave.getType() == ElementKeyMap.FACTION_PUBLIC_EXCEPTION_ID) {
				return true;
			}
		}
		return false;
	}

	private boolean dockOrLand(SegmentPiece dockingTarget, SegmentPiece pieceCore) throws DockingNotYetUpdatedException {
		dockingTarget.refresh();
		if (dockingTarget.getType() == Element.TYPE_NONE) {
			System.err.println("[DOCKING] NOT DOCKING " + pieceCore.getSegment().getSegmentController() + " ON NOTHING: " + dockingTarget + " ON " + dockingTarget.getSegment().getSegmentController());
			return false;
		}
		boolean publicException = isPublicException(dockingTarget);
		if (!checkFactionAllowed(pieceCore, dockingTarget)) {
			SegmentController c = dockingTarget.getSegment().getSegmentController();
			if (!publicException) {
				if (!isOnServer() && ((GameClientState) getState()).getShip() == segmentController) {
					((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("You cannot dock on a\nship of another faction!"), 0);
				}
				System.err.println("[DOCKING] NOT DOCKING: faction does not equal " + segmentController.getState());
				if (isOnServer()) {
					if (System.currentTimeMillis() - lastSentMessage > 4000) {
						if (segmentController instanceof PlayerControllable) {
							PlayerControllable pc = (PlayerControllable) segmentController;
							for (PlayerState p : pc.getAttachedPlayers()) {
								p.sendServerMessage(new ServerMessage(Lng.astr("You cannot dock on a\nship of another faction!"), ServerMessage.MESSAGE_TYPE_ERROR, p.getId()));
							}
						}
						lastSentMessage = System.currentTimeMillis();
					}
				}
				return false;
			}
		// playerState.sendServerMessage(new ServerMessage(Lng.str("You cannot dock on a\nship of another faction!",  ServerMessage.MESSAGE_TYPE_ERROR,  playerState.getId()));
		}
		if (ElementKeyMap.getInfo(dockingTarget.getType()).isOldDockable()) {
			// System.err.println("[DOCKING] "+segmentController+" DOING THE DOCK TO "+dockingTarget.getSegment().getSegmentController());
			return dock(pieceCore, dockingTarget, publicException, false);
		} else {
			assert (false) : dockingTarget.getType();
		}
		return false;
	}

	public void fromTagStructure(Tag tag) {
		Tag[] subTags = (Tag[]) tag.getValue();
		String dockedTo = (String) subTags[0].getValue();
		byte dockLocalOrientation = 0;
		if (subTags.length > 5 && subTags[5].getType() == Type.BYTE) {
			dockLocalOrientation = ((Byte) subTags[5].getValue());
		}
		if (subTags.length > 6 && subTags[6].getType() == Type.VECTOR4f) {
			localDockingOrientation = new Quat4f(((Vector4f) subTags[6].getValue()));
		}
		if (!dockedTo.equals("NONE")) {
			segmentController.setHidden(true);
			loadedFromTag = true;
			Vector3i value = (Vector3i) subTags[1].getValue();
			if (segmentController.isLoadedFromChunk16()) {
				value.add(Chunk16SegmentData.SHIFT);
			}
			requestDelayedDock(dockedTo, value, new Quat4f(localDockingOrientation), dockLocalOrientation);
		}
		// String landedTo = (String)subTags[2].getValue();
		// if(!landedTo.equals("NONE")){
		// requestDelayedDock(landedTo, (Vector3i)subTags[3].getValue());
		// }
		if (subTags.length > 4 && subTags[4].getType() == Type.VECTOR3f) {
			// TODO remove "s" after a while
			// it is only to reset the size for docking
			// saves that saved dimension instead of bb.min
			if ("s".equals(subTags[4].getName())) {
				size.set((Vector3f) subTags[4].getValue());
				// System.err.println("[DOCKING] DOCKING SIZE ON FROM TAG: "+size);
				this.sizeSetFromTag = true;
			} else {
			// old format
			// System.err.println("COULD NOT LOAD DOCKING SIZE (old)");
			}
		} else {
		// System.err.println("COULD NOT LOAD DOCKING SIZE");
		}
	}

	/**
	 * @return the delayedDock
	 */
	public String getDelayedDock() {
		return delayedDockUID;
	}

	/**
	 * @return the dockedOn
	 */
	public ElementDocking getDockedOn() {
		return dockedOn;
	}

	/**
	 * @param dockedOn the dockedOn to set
	 */
	public void setDockedOn(ElementDocking dockedOn) {
		this.dockedOn = dockedOn;
		docked = dockedOn != null;
	}

	/**
	 * @return the dockedOnThis
	 */
	public List<ElementDocking> getDockedOnThis() {
		return dockedOnThis;
	}

	/**
	 * @return the dockingPos
	 */
	public Transform getDockingPos() {
		return dockingPos;
	}

	/**
	 * @return the dockingPosInverse
	 */
	public Transform getDockingPosInverse() {
		return dockingPosInverse;
	}

	/**
	 * @return the size
	 */
	public Vector3f getSize() {
		return size;
	}

	private StateInterface getState() {
		return segmentController.getState();
	}

	public void getWorldTransform() {
	}

	/**
	 * @return the docked
	 */
	public boolean isDocked() {
		return docked;
	}

	/**
	 * @param docked the docked to set
	 */
	public void setDocked(boolean docked) {
		this.docked = docked;
	}

	public SegmentController getLocalMother() {
		return dockedOn.to.getSegment().getSegmentController();
	}

	public boolean isOnServer() {
		return segmentController.isOnServer();
	}

	/**
	 * @return the triggerMotherShipRemoved
	 */
	public boolean isTriggerMotherShipRemoved() {
		return triggerMotherShipRemoved;
	}

	/**
	 * @param triggerMotherShipRemoved the triggerMotherShipRemoved to set
	 */
	public void setTriggerMotherShipRemoved(boolean triggerMotherShipRemoved) {
		this.triggerMotherShipRemoved = triggerMotherShipRemoved;
	}

	public void onDockChanged(NetworkSegmentController networkObject) {
		if (isOnServer()) {
			if (docked) {
				assert (getState().isSynched());
				if (NetworkObject.CHECKUNSAVE && !getState().isSynched()) {
					throw new UnsaveNetworkOperationException();
				}
				networkObject.dockingSize.set(size);
				networkObject.dockingOrientation.set(localDockingOrientation.x, localDockingOrientation.y, localDockingOrientation.z, localDockingOrientation.w);
				networkObject.dockedTo.set(dockedOn.to.getSegment().getSegmentController().getUniqueIdentifier());
				byte localDockingOrientation = 0;
				networkObject.dockedElement.set(new Vector4i(dockedOn.to.getAbsolutePos(new Vector3i()), localDockingOrientation));
			} else {
				networkObject.dockingSize.set(new Vector3f(0, 0, 0));
				networkObject.dockedTo.set("NONE");
			}
		// System.err.println("[DOCKING] SET NT DOCK TO "+networkObject.dockedTo.get());
		}
	}

	private void onExistsDelayedDock() throws DockingNotYetUpdatedException {
		assert (delayedDockUID != null);
		boolean foundEntity = false;
		Sendable s = getState().getLocalAndRemoteObjectContainer().getUidObjectMap().get(delayedDockUID);
		foundEntity = s != null && s instanceof SegmentController;
		if (s == segmentController) {
			try {
				throw new DockingException(segmentController + " attempted to dock to itself");
			} catch (DockingException e) {
				e.printStackTrace();
			}
			((GameServerState) getState()).getController().broadcastMessageAdmin(Lng.astr("ERROR:\n%s\ntried to dock to itself!", segmentController), ServerMessage.MESSAGE_TYPE_ERROR);
			System.err.println("[DOCKING] ERROR: docking target for: " + this + " is invalid: " + delayedDockUID + "; unhiding...");
			delayedDockUID = null;
			segmentController.setHidden(false);
			triggerMotherShipRemoved = false;
			return;
		}
		if (foundEntity) {
			dockingUIDnotFound = -1;
			SegmentController con = (SegmentController) s;
			assert (con.getUniqueIdentifier().equals(delayedDockUID));
			if (!con.getPhysicsDataContainer().isInitialized() || !con.getSegmentBuffer().getBoundingBox().isInitialized() || !con.getSegmentBuffer().getBoundingBox().atLeastOne()) {
				// System.err.println("[DOCKING] TARGET PHYSICS NOT YET INITIALIZED: "+getState()+" with "+s+" ON "+(isOnServer() ? "SERVER" : "CLIENT"));
				delayCheck = System.currentTimeMillis();
				return;
			}
			if (!segmentController.getPhysicsDataContainer().isInitialized() || !segmentController.getSegmentBuffer().getBoundingBox().isInitialized() || !segmentController.getSegmentBuffer().getBoundingBox().atLeastOne()) {
				System.err.println("[DOCKING] SELF PHYSICS NOT YET INITIALIZED: " + getState() + " with " + s + " ON " + (isOnServer() ? "SERVER" : "CLIENT"));
				delayCheck = System.currentTimeMillis();
				return;
			}
			if (con.getTotalElements() < 1 || segmentController.getTotalElements() < 1) {
				System.err.println("[DOCKING] Object has zero elements: " + getState() + " with " + s + " ON " + (isOnServer() ? "SERVER" : "CLIENT"));
			}
			// autorequest true previously
			SegmentPiece piece = con.getSegmentBuffer().getPointUnsave(delayedDockPos);
			// autorequest true previously
			SegmentPiece pieceCore = segmentController.getSegmentBuffer().getPointUnsave(Ship.core);
			if (piece != null && pieceCore != null) {
				if (piece.getSegment().getSegmentController().getPhysicsDataContainer().isInitialized() && pieceCore.getSegment().getSegmentController().getPhysicsDataContainer().isInitialized()) {
					boolean docked = dockOrLand(piece, pieceCore);
					if (docked) {
						if (isOnServer()) {
							if (segmentController.getFactionId() == 0 || segmentController.getElementClassCountMap().get(ElementKeyMap.FACTION_BLOCK) == 0) {
								if (!isPublicException(piece)) {
									segmentController.setFactionId(piece.getSegment().getSegmentController().getFactionId());
								}
							}
							// unhide on server
							segmentController.setHidden(false);
						}
					} else {
						if (isOnServer()) {
							if (segmentController.getElementClassCountMap().get(ElementKeyMap.FACTION_BLOCK) == 0) {
								segmentController.setFactionId(0);
							}
							// docking failed!
							// force unhide for client and server
							segmentController.setHidden(false);
							System.err.println("[DOCKING] docking failed (docking not executed): UNHIDE SEGMENTCONTROLLER: " + segmentController);
						}
					}
					delayedDockUID = null;
				} else {
					System.err.println("[DOCKING] Deffered delayed dock ");
				}
			} else {
				delayCheck = System.currentTimeMillis();
			}
		} else {
			// UID of object doesn't exist -> remove after certain amount of time
			if (dockingUIDnotFound <= 0) {
				dockingUIDnotFound = System.currentTimeMillis();
			} else {
				if (isOnServer() && System.currentTimeMillis() - dockingUIDnotFound > 100000) {
					try {
						throw new DockingException("Undocking of " + this + " because of docking timout (target " + delayedDockUID + " did not appear for 100 seconds)");
					} catch (DockingException e) {
						e.printStackTrace();
					}
					((GameServerState) getState()).getController().broadcastMessageAdmin(Lng.astr("ERROR: Docking timed out on\n%s\nto UID '%s'\nStructure will now undock!", this.segmentController.getName(), delayedDockUID), ServerMessage.MESSAGE_TYPE_ERROR);
					System.err.println("[DOCKING] ERROR: docking target for: " + this + " does not exist: " + delayedDockUID + "; unhiding...");
					delayedDockUID = null;
					segmentController.setHidden(false);
					triggerMotherShipRemoved = false;
				}
			}
		}
	}

	public void requestDelayedDock(String value, Vector3i dockPos, Quat4f localDockingRot, int orientation) {
		if (checkLastDockDelay()) {
			// segmentController.getPhysicsDataContainer().getObject().setActivationState(CollisionObject.DISABLE_SIMULATION);
			this.delayedDockUID = value;
			this.delayedDockPos = dockPos;
			this.delayedDockLocalRot = new Quat4f(localDockingRot);
		// System.err.println("[DOCKING] REQUESTED DELAYED DOCK: "+segmentController+": "+delayedDock+": "+delayedDockPos);
		}
	}

	public void requestDelayedUndock(boolean force) {
		if (force || checkLastDockDelay()) {
			// try{
			// throw new NullPointerException();
			// }catch(Exception e){
			// e.printStackTrace();
			// }
			System.err.println("[DOCKING]" + segmentController.getState() + " REQUEST UNDOCK " + segmentController + "; ");
			delayedUndock = true;
		}
	}

	public Tag toTagStructure() {
		if (delayedDockUID != null) {
			// System.err.println("DELAYED DOCK OF "+segmentController+" TO "+delayedDock+" HAS FAILED. re-writing dock!");
			tagOverwrite = new DockingTagOverwrite(delayedDockUID, delayedDockPos, delayedDockLocalRot, (byte) 0);
		} else {
			Quat4f r = new Quat4f(this.targetQuaternion);
			Quat4f rStart = new Quat4f(this.targetStartQuaternion);
			rStart.inverse();
			r.mul(rStart);
			localDockingOrientation.x = r.x;
			localDockingOrientation.y = r.y;
			localDockingOrientation.z = r.z;
			localDockingOrientation.w = r.w;
		}
		Tag dockedToTag;
		if (tagOverwrite == null) {
			dockedToTag = new Tag(Type.STRING, null, docked ? dockedOn.to.getSegment().getSegmentController().getUniqueIdentifier() : "NONE");
		} else {
			dockedToTag = new Tag(Type.STRING, null, tagOverwrite.dockTo);
		}
		if (!((String) dockedToTag.getValue()).equals("NONE")) {
		// System.err.println("WROTE DOCKED TO "+dockedToTag.getValue());
		}
		Tag dockedToPosTag;
		if (tagOverwrite == null) {
			dockedToPosTag = new Tag(Type.VECTOR3i, null, docked ? dockedOn.to.getAbsolutePos(new Vector3i()) : new Vector3i());
		} else {
			dockedToPosTag = new Tag(Type.VECTOR3i, null, tagOverwrite.pos);
		}
		Tag landedToTag = new Tag(Type.BYTE, null, (byte) 0);
		Tag landedToPosTag = new Tag(Type.BYTE, null, (byte) 0);
		Tag sizeTag = new Tag(Type.VECTOR3f, "s", size);
		// if(size.length() > 0){
		// System.err.println("[TAG] saving SIZE "+size);
		// }
		Tag orientTag;
		if (tagOverwrite == null) {
			orientTag = new Tag(Type.BYTE, null, (byte) 0);
		} else {
			orientTag = new Tag(Type.BYTE, null, (byte) 0);
		}
		Tag rotTag;
		if (tagOverwrite == null) {
			rotTag = new Tag(Type.VECTOR4f, null, new Vector4f(localDockingOrientation));
		} else {
			rotTag = new Tag(Type.VECTOR4f, null, new Vector4f(tagOverwrite.rot));
		}
		return new Tag(Type.STRUCT, null, new Tag[] { dockedToTag, dockedToPosTag, landedToTag, landedToPosTag, sizeTag, orientTag, rotTag, FinishTag.INST });
	}

	private void undock() {
		if (docked) {
			this.updateOnce = false;
			ElementDocking d = dockedOn;
			SegmentController dockedToBefore = d.to.getSegment().getSegmentController();
			if (d.to.getType() == Element.TYPE_NONE || ElementKeyMap.getInfo(d.to.getType()).isOldDockable()) {
				System.err.println("[DOCKING] NOW UNDOCKING: " + segmentController + "; " + segmentController.getState() + "; DOCKED TO TYPE: " + d.to.getType() + "; curpos: " + segmentController.getWorldTransform().origin);
				SegmentController targetSegmentController;
				targetSegmentController = d.to.getSegment().getSegmentController();
				Vector3f linVelofromMother = new Vector3f();
				if (d.to.getSegment().getSegmentController().getPhysicsDataContainer().getObject() != null) {
					((RigidBody) targetSegmentController.getPhysicsDataContainer().getObject()).getLinearVelocity(linVelofromMother);
				} else {
					SegmentController mother = d.to.getSegment().getSegmentController().getDockingController().getAbsoluteMother();
					assert (mother.getPhysicsDataContainer().getObject() != null) : mother;
					((RigidBody) mother.getPhysicsDataContainer().getObject()).getLinearVelocity(linVelofromMother);
				}
				boolean remove = targetSegmentController.getDockingController().dockedOnThis.remove(d);
				if (!remove) {
					System.err.println("Exception: WARNING! UNDOCK UNSUCCESSFULL " + d + ": " + targetSegmentController.getDockingController().dockedOnThis);
				}
				setDockedOn(null);
				if (d.to.getSegment().getSegmentController().getPhysicsDataContainer().getObject() != null) {
					updateTargetWithRemovedController(targetSegmentController, segmentController);
				} else {
					// chain: update from absolute mother
					SegmentController mother = d.to.getSegment().getSegmentController().getDockingController().getAbsoluteMother();
					updateTargetWithRemovedController(mother, segmentController);
				}
				if (isOnServer()) {
					segmentController.vServerAttachment.clear();
				}
				if (segmentController.getPhysicsDataContainer().lastCenter.length() > 0) {
					// adapt for center of mass only if it didn't start docking
					// and had an update before
					CubesCompoundShape c = (CubesCompoundShape) segmentController.getPhysicsDataContainer().getShape();
					if (c != null) {
						Vector3f centerOfMass = c.getCenterOfMass();
						segmentController.getWorldTransform().basis.transform(centerOfMass);
						segmentController.getWorldTransform().origin.add(centerOfMass);
					}
				}
				// re-init independent physics object
				segmentController.getPhysicsDataContainer().setObject(null);
				segmentController.getPhysicsDataContainer().setShape(null);
				segmentController.getPhysicsDataContainer().setShapeChield(null, -1);
				segmentController.getPhysicsDataContainer().initialTransform.set(segmentController.getWorldTransform());
				segmentController.getRemoteTransformable().getInitialTransform().set(segmentController.getWorldTransform());
				segmentController.initPhysics();
				segmentController.onPhysicsAdd();
				/*
				 * This removes all
				 * docked objects of the undocker
				 * and adds them again.
				 * This should be fully working with unlimited
				 * chain lengths since undock() is called
				 * recursively for each element
				 */
				if (segmentController.getDockingController().dockedOnThis.size() > 0) {
					ArrayList<ElementDocking> l = new ArrayList<ElementDocking>();
					// chain
					for (ElementDocking stillDocked : segmentController.getDockingController().dockedOnThis) {
						l.add(stillDocked);
					}
					for (ElementDocking ed : l) {
						ed.from.getSegment().getSegmentController().getDockingController().undock();
					}
					for (ElementDocking ed : l) {
						try {
							ed.from.getSegment().getSegmentController().getDockingController().dock(ed.from, ed.to, isPublicException(ed.to), true);
						} catch (DockingNotYetUpdatedException e) {
							e.printStackTrace();
						}
					}
				}
				((RigidBody) segmentController.getPhysicsDataContainer().getObject()).setLinearVelocity(linVelofromMother);
				segmentController.getRemoteTransformable().receivedTransformation.linearVelocity.set(linVelofromMother);
				segmentController.getPhysicsDataContainer().updatePhysical(getState().getUpdateTime());
				segmentController.setFlagPhysicsInit(false);
				size.set(0, 0, 0);
				// segmentController.getPhysicsDataContainer().getObject().activate(true);
				if (segmentController instanceof SendableSegmentController) {
					((SendableSegmentController) segmentController).handleNTDockChanged();
				}
				if (segmentController.getElementClassCountMap().get(ElementKeyMap.FACTION_BLOCK) == 0) {
					if (segmentController.getFactionId() > 0) {
						System.err.println("[DOCKING] resetting faction ID from " + segmentController.getFactionId());
						segmentController.setFactionId(0);
					}
				}
				if (isOnServer()) {
					SendableSegmentController s = (SendableSegmentController) d.to.getSegment().getSegmentController();
					Vector3i pos = d.to.getAbsolutePos(new Vector3i());
					System.err.println("[DOCK] ACTIVATE SURROUND SERVER WITH UNDOCK " + pos);
					s.activateSurroundServer(false, pos, ElementKeyMap.ACTIVAION_BLOCK_ID);
				} else {
				// if(segmentController.isClientOwnObject()){
				// ((GameClientState)getState()).getPlayer().setInvisibilityMode(invisibilityMode);
				// }
				}
			} else {
				assert (false) : d.to.getType();
			}
			lastDockingOrUndocking = System.currentTimeMillis();
			lastUnDocking = System.currentTimeMillis();
			localDockingOrientation.set(0, 0, 0, 1);
			this.dockingCode = -1;
			Starter.modManager.onSegmentControllerUndocking(segmentController);
			segmentController.flagupdateMass();
			dockedToBefore.flagupdateMass();
			if (!docked) {
				((RigidBodySegmentController) segmentController.getPhysicsDataContainer().getObject()).undockingProtection = dockedToBefore;
			}
		}
	}

	/**
	 * update turrets instantly when a transform was received over network
	 *
	 * @param t
	 */
	public void onSmootherSet(Transform t) {
		if (!dockedOnThis.isEmpty()) {
			for (ElementDocking e : dockedOnThis) {
				if (e == null || e.from == null) {
					assert (false);
					throw new NullPointerException("Invalid docking: " + e);
				}
				SegmentController docked = e.from.getSegment().getSegmentController();
				if (!isOnServer() && segmentController.clientVirtualObject != null) {
					// this means the mothership is not in the sector of the player
					// we need to update with transform since the client physics is running on a virtual kinematic object
					assert (segmentController.getWorldTransform().equals(t)) : "Should be equal since we just set it";
					docked.getPhysicsDataContainer().updateManuallyWithChildTrans(segmentController.getWorldTransform(), (CompoundShape) segmentController.getDockingController().getRoot().getPhysicsDataContainer().getShape());
					docked.getDockingController().updateOnce = true;
				}
			}
		}
	}

	public SegmentController getRoot() {
		if (docked) {
			return dockedOn.to.getSegmentController().getDockingController().getRoot();
		}
		return segmentController;
	}

	private void updateDockedOnThis(Timer timer, SegmentController segmentController) {
		if (segmentController.getPhysicsDataContainer().isInitialized()) {
			CollisionObject motherPhysicsObject;
			if (segmentController.getPhysicsDataContainer().getObject() == null) {
				segmentController = dockedOn.to.getSegment().getSegmentController();
				motherPhysicsObject = dockedOn.to.getSegment().getSegmentController().getDockingController().getAbsoluteMother().getPhysicsDataContainer().getObject();
				if (motherPhysicsObject == null) {
					System.err.println("[DOCKING]" + segmentController.getState() + " Exception: cannot update docking position from chain. no mother for " + this.segmentController + " -> " + segmentController + "; mother has no physics object " + dockedOn.to.getSegment().getSegmentController());
					return;
				}
			} else {
				motherPhysicsObject = segmentController.getPhysicsDataContainer().getObject();
				if (motherPhysicsObject == null) {
					System.err.println("[DOCKING]" + segmentController.getState() + " Exception: cannot update docking position. no mother for " + this.segmentController + " -> " + segmentController + "; mother has no physics object " + segmentController);
					return;
				}
			}
			assert (motherPhysicsObject != null) : segmentController;
			// assert(motherPhysicsObject.getCollisionShape() == segmentController.getPhysicsDataContainer().getShape()):segmentController+": "+motherPhysicsObject.getCollisionShape()+"; "+segmentController.getPhysicsDataContainer().getShape();
			ElementDocking rem = null;
			for (ElementDocking e : dockedOnThis) {
				if (e == null || e.from == null) {
					assert (false);
					throw new RuntimeException("Invalid docking: " + e);
				}
				SegmentController docked = e.from.getSegment().getSegmentController();
				if (docked.getPhysicsDataContainer().isInitialized()) {
					if (!(getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().containsKey(docked.getId()))) {
						System.err.println("[DOCKING] UPDATING " + segmentController + " MASS BECAUSE DOCKED SHIP DOESNT EXIST ANYMORE: " + docked);
						updateTargetWithRemovedController(segmentController, docked);
						rem = e;
						if (isOnServer()) {
							// trigger mother ship logic
							((SendableSegmentController) segmentController).activateSurroundServer(false, e.to.getAbsolutePos(new Vector3i()), ElementKeyMap.ACTIVAION_BLOCK_ID);
						}
					} else {
						docked.getDockingController().updateTransform(timer);
					}
				}
				if (!isOnServer() && segmentController.clientVirtualObject != null) {
					// this means the mothership is not in the sector of the player
					// we need to update with transform since the client physics is running on a virtual kinematic object
					// docked.getPhysicsDataContainer().updateManuallyWithChildTrans(segmentController.getWorldTransform());
					// segmentController.getPhysicsDataContainer().addChainUpdate(docked.getPhysicsDataContainer());
					docked.getDockingController().updateOnce = true;
				} else {
					// updating docked objects, since they cant do that for themselves
					// segmentController.getPhysicsDataContainer().addChainUpdate(docked.getPhysicsDataContainer());
					docked.getDockingController().updateOnce = true;
				}
				assert (motherPhysicsObject instanceof RigidBody);
			}
			if (rem != null) {
				dockedOnThis.remove(rem);
			}
			((CompoundShape) motherPhysicsObject.getCollisionShape()).recalculateLocalAabb();
		}
	}

	private void updateDocking(Timer timer) {
		if (!segmentController.getPhysicsDataContainer().isInitialized() || !segmentController.getSegmentBuffer().getBoundingBox().isInitialized() || (timer.currentTime - delayCheck < 1000)) {
			return;
		}
		if (isOnServer() && delayedDockUID != null && triggerMotherShipRemoved) {
			System.err.println("[DOCKING] docking failed (mothership removed): UNHIDE SEGMENTCONTROLLER: " + segmentController);
			// docking failed!
			// force unhide for client and server
			segmentController.setHidden(false);
			triggerMotherShipRemoved = false;
		}
		if (delayedDockUID != null) {
			try {
				onExistsDelayedDock();
			} catch (DockingNotYetUpdatedException e) {
				// e.printStackTrace();
				System.err.println("[DOCK] " + segmentController + " Waiting to dock for chain to be initialized");
			}
		}
		if (delayedUndock) {
			if (isOnServer()) {
				System.err.println("[DOCKING] UNDOCKING: SET HIDDEN FALSE ON SERVER FOR " + this);
				segmentController.setHidden(false);
				segmentController.getNetworkObject().hidden.setChanged(true);
			}
			undock();
			delayedUndock = false;
		}
		if (!dockedOnThis.isEmpty()) {
			updateDockedOnThis(timer, segmentController);
		}
		if (docked) {
			if (!(getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().containsKey(dockedOn.to.getSegment().getSegmentController().getId()))) {
				System.err.println("[DOCKING] undocking this " + segmentController + " because mothership is deleted: " + dockedOn.to.getSegment().getSegmentController());
				if (isOnServer()) {
					System.err.println("[DOCKING] UNDOCKING: SET HIDDEN FALSE ON SERVER FOR " + this);
					segmentController.setHidden(false);
					segmentController.getNetworkObject().hidden.setChanged(true);
				}
				undock();
			}
		}
	}

	public void updateDockingPosition() {
		dockedOn.to.getTransform(dockingPos);
		switch(dockedOn.to.getOrientation()) {
			case (Element.RIGHT) -> GlUtil.getLeftVector(mod, dockingPos);
			case (Element.LEFT) -> GlUtil.getRightVector(mod, dockingPos);
			case (Element.TOP) -> GlUtil.getUpVector(mod, dockingPos);
			case (Element.BOTTOM) -> GlUtil.getBottomVector(mod, dockingPos);
			case (Element.FRONT) -> GlUtil.getForwardVector(mod, dockingPos);
			case (Element.BACK) -> GlUtil.getBackVector(mod, dockingPos);
		}
		BoundingBox boundingBox = dockedOn.from.getSegment().getSegmentController().getSegmentBuffer().getBoundingBox();
		Vector3f size = new Vector3f();
		size.sub(boundingBox.max, boundingBox.min);
		if (isTurretDocking(dockedOn.to)) {
			mod.scale(4.5f);
		} else {
			mod.scale(size.y / 2);
		}
		dockingPos.origin.add(mod);
		dockingPosInverse.set(dockingPos);
		dockingPosInverse.inverse();
	}

	public void updateFromNetworkObject(NetworkSegmentController s) {
		if (!isOnServer()) {
			s.dockingSize.getVector(size);
			s.dockingOrientation.getVector(localDockingOrientation);
			/*
			 * update the transmitted rotation
			 * from network
			 */
			if (!segmentController.isOnServer() && !(segmentController instanceof PlayerControllable && !((PlayerControllable) segmentController).getAttachedPlayers().isEmpty())) {
				// update from server only if there is no one actually flying this thing
				if (!(segmentController instanceof AiInterface && ((AiInterface) segmentController).getAiConfiguration().isActiveAI())) {
					// update only if not controlled by AI (AI client will update by orientation)
					segmentController.getNetworkObject().dockingTrans.getVector(targetQuaternion);
				}
			}
			{
				String rs = s.dockedTo.get();
				boolean noDock = !docked && !rs.equals("NONE");
				boolean diffDock = docked && !rs.equals("NONE") && !rs.equals(dockedOn.to.getSegment().getSegmentController().getUniqueIdentifier());
				boolean undock = docked && rs.equals("NONE");
				boolean removeAttempt = delayedDockUID != null && !docked && rs.equals("NONE");
				if (noDock || diffDock) {
					Vector4i vector = s.dockedElement.getVector();
					if (isOnServer()) {
						segmentController.setHidden(true);
					}
					requestDelayedDock(rs, new Vector3i(vector.x, vector.y, vector.z), new Quat4f(localDockingOrientation.x, localDockingOrientation.y, localDockingOrientation.z, localDockingOrientation.w), vector.w);
				}
				if (undock && !delayedUndock) {
					System.err.println(getState() + " [DOCKING] UNDOCK REQUEST FROM SERVER (delayedRequest) " + segmentController);
					requestDelayedUndock(true);
				}
				if (removeAttempt) {
					delayedDockUID = null;
				}
			}
		} else {
			if (s.dockClientUndockRequests.getReceiveBuffer().size() > 0) {
				requestDelayedUndock(true);
			}
		}
	}

	public void updateLocal(Timer timer) {
		if (docked || dockedOnThis.size() > 0) {
			segmentController.getPhysicsDataContainer().oldDockingUpdateWithoutPhysicsObjectInterface = this;
		}
		updateDocking(timer);
	}

	private void updateTransform(Timer timer) {
		if (docked) {
			updateDockingPosition();
			if (isTurretDocking(dockedOn.to)) {
				udpateTurretTransform(timer);
			}
		}
	}

	private void udpateTurretTransform(Timer timer) {
		Transform transform = segmentController.getPhysicsDataContainer().getShapeChild().transform;
		Quat4fTools.set(transform.basis, fromTmp);
		fromTmp.normalize();
		targetQuaternion.normalize();
		if (targetQuaternion.w != 0) {
			if (fromTmp.w == 0) {
				res.set(targetQuaternion);
			} else {
				float dockedRotationSpeed = 50f;
				Quat4Util.slerp(fromTmp, targetQuaternion, Math.max(0.001f, Math.min(1f, timer.getDelta() * dockedRotationSpeed)), res);
				res.normalize();
			}
			transform.basis.set(res);
		}
		if (segmentController.isOnServer()) {
			Vector4f v = new Vector4f(targetQuaternion.x, targetQuaternion.y, targetQuaternion.z, targetQuaternion.w);
			if (!segmentController.getNetworkObject().dockingTrans.getVector().epsilonEquals(v, 0.01f)) {
				segmentController.getNetworkObject().dockingTrans.set(v);
			}
		}
	}

	@Override
	public void updateWithoutPhysicsObject() {
		if (docked) {
			SegmentController parent = dockedOn.to.getSegment().getSegmentController();
			while (parent.getDockingController().docked) {
				parent = parent.getDockingController().dockedOn.to.getSegment().getSegmentController();
			}
			segmentController.getPhysicsDataContainer().updateManuallyWithChildTransOld(parent.getWorldTransform());
		}
	}

	@Override
	public void checkRootIntegrity() {
	}

	/**
	 * includes chains
	 *
	 * @param other
	 * @return
	 */
	public boolean isInAnyDockingRelation(SegmentController other) {
		// return
		// (other.getDockingController().dockingCode > 0 && other.getDockingController().dockingCode == segmentController.getId()) ||
		// (dockingCode > 0 && dockingCode == other.getId()) ||
		// 
		// (dockingCode >= 0 && other.getDockingController().dockingCode == dockingCode);
		boolean moToOther = false;
		if (docked && other.getDockingController().docked) {
			return getLocalMother() == other.getDockingController().getLocalMother();
		}
		if (docked) {
			// check mother (to catch turret -> turret relations)
			moToOther = dockedOn.to.getSegment().getSegmentController().getDockingController().isInAnyDockingRelation(other);
		}
		return moToOther || isDockedTo(other) || other.getDockingController().isDockedTo(segmentController);
	}

	/**
	 * includes chains
	 *
	 * @param other
	 * @return
	 */
	public boolean isDockedTo(SegmentController other) {
		if (docked) {
			if (dockedOn.to.getSegment().getSegmentController() == other) {
				return true;
			}
			if (dockedOn.to.getSegment().getSegmentController().getDockingController().docked) {
				if (dockedOn.to.getSegment().getSegmentController().getDockingController().dockedOn.to.getSegment().getSegmentController() == other) {
					return true;
				}
			}
		}
		return false;
	}

	public void setFactionAll(int id) {
		segmentController.setFactionId(id);
		for (ElementDocking e : dockedOnThis) {
			e.from.getSegment().getSegmentController().getDockingController().setFactionAll(id);
		}
	}

	/**
	 * @return the lastUnDocking
	 */
	public long getLastUnDocking() {
		return lastUnDocking;
	}

	public boolean isTurretDocking() {
		return docked && isTurretDocking(dockedOn.to);
	}

	public boolean isInAnyDockingRelation() {
		return docked || !dockedOnThis.isEmpty();
	}

	public int getDockingCode() {
		return dockingCode;
	}

	public void setDockingCode(int dockingCode) {
		this.dockingCode = dockingCode;
	}
}
