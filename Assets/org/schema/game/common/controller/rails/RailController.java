package org.schema.game.common.controller.rails;

import api.listener.events.entity.rail.RailConnectAttemptClientEvent;
import api.listener.events.entity.rail.RailShootoutEvent;
import api.listener.fastevents.FastListenerCommon;
import api.listener.fastevents.RailMoveListener;
import api.mod.StarLoader;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.CompoundShapeChild;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import org.schema.common.util.TranslatableEnum;
import org.schema.common.util.linAlg.*;
import org.schema.game.client.controller.GameClientController;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.client.view.camera.InShipCamera;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelpManager;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelperContainer.Hos;
import org.schema.game.client.view.gui.weapon.WeaponRowElementInterface;
import org.schema.game.client.view.gui.weapon.WeaponSegmentControllerUsableElement;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.ai.AIConfiguationElements;
import org.schema.game.common.controller.ai.AIGameConfiguration;
import org.schema.game.common.controller.ai.SegmentControllerAIInterface;
import org.schema.game.common.controller.ai.Types;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.controller.elements.*;
import org.schema.game.common.controller.rails.RailRelation.DockValidity;
import org.schema.game.common.controller.rails.RailRelation.DockingPermission;
import org.schema.game.common.controller.rails.RailRelation.RotationType;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.SimpleGameObject;
import org.schema.game.common.data.VoidUniqueSegmentPiece;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.CubesCompoundShape;
import org.schema.game.common.data.physics.RigidBodySegmentController;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SectorNotFoundException;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.game.common.util.FastCopyLongOpenHashSet;
import org.schema.game.network.objects.RailMove;
import org.schema.game.network.objects.remote.RemoteRailMoveRequest;
import org.schema.game.network.objects.remote.RemoteRailRequest;
import org.schema.game.server.controller.SectorSwitch;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.stateMachines.AIConfigurationInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.ContextFilter;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.BoundingSphere;
import org.schema.schine.graphicsengine.util.timer.LinearTimerUtil;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.objects.container.UpdateWithoutPhysicsObjectInterface;
import org.schema.schine.network.objects.remote.RemoteVector4f;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class RailController implements UpdateWithoutPhysicsObjectInterface, BlockLogicReplaceInterface {

	public static final byte TYPE_TAG_NOTHING = 0;

	public static final byte TYPE_TAG_DOCKED = 1;

	public static final byte TYPE_TAG_RAIL_ROOT = 2;

	public static final byte TYPE_TAG_ACTIVE_REQUEST = 3;

	public final List<RailRelation> next = new ObjectArrayList<RailRelation>();

	private final SegmentController self;

	private final Transform relativeToRootLocalTransform;

	private final Transform railTurretMovingLocalTransform;

	private final Transform railTurretMovingLocalTransformTarget;

	private final Transform railMovingLocalAtDockTransform;

	private final Transform railMovingLocalTransform;

	private final Transform railOriginalLocalTransform;

	public final Transform potentialBasicRelativeTransform;

	/**
	 * the transform of all parents up to this entity *
	 */
	private final Transform railUpToThisOriginalLocalTransform;

	private final Transform basicRelativeTransform;

	private final Transform potentialRelativeToRootLocalTransform;

	private final Transform potentialRailTurretMovingLocalTransform;

	private final Transform potentialRailMovingLocalTransform;

	private final Transform potentialRailOriginalLocalTransform;

	private final Transform potentialRailUpToThisOriginalLocalTransform;

	private final Transform prefRot = new Transform();

	private Transform railTurretMovingLocalTransformLastUpdate = new Transform();

	private Transform railMovingLocalTransformLastUpdate = new Transform();

	private Transform railTurretMovingLocalTransformLastUpdateR;

	private Transform railMovingLocalTransformLastUpdateR;

	private final List<Quat4f> railMovingProspectedRots = new ObjectArrayList<Quat4f>(8);

	private final RailCollisionChecker collisionChecker = new RailCollisionChecker();

	private final ObjectArrayFIFOQueue<RailRequest> railRequests = new ObjectArrayFIFOQueue<RailRequest>();

	private final Vector3f lastLinearVelocityFromRoot = new Vector3f();

	private final SegmentPiece[] tmpPieces = new SegmentPiece[6];

	private final SegmentPiece tmpPiece = new SegmentPiece();

	private final SegmentPiece tmpPieceR = new SegmentPiece();

	private final Vector3i tmpPos = new Vector3i();

	public Matrix3f turretRotYHelp;

	public RailRelation previous;

	public RailRequest railRequestCurrent;

	public Transform turretRotX;

	private Vector3f railMovingProspectedPos;

	private LinearTimerUtil linearMoveTest = new LinearTimerUtil(0.5f);

	private float dockingMass;

	private boolean dirty;

	private long dockedSince;

	private long lastDisconnect;

	private float railSpeed = 3f;

	private float railSpeedRotBasis = 5f;

	private float railSpeedRot = 1.5f;

	private float railSpeedPercent;

	private boolean requestedToDefaultFlag;

	private long lastValidityCheck;

	private long lastTurretColCheck;

	private long lastCurretCollisionHappened;

	private float railMovingProspectedRotTime;

	private float colLinTimer;

	private float colRotTimer;

	private float railMovingProspectedRotSegmentTime;

	private boolean loadedFromTag;

	private boolean colByLinearMovement;

	public String[] dockedUIDSFromTag;

	private boolean recY;

	private long lastDockRequest;

	private final Vector3f lastMovementDirRelativeToRoot = new Vector3f();

	private boolean shootOut;

	private boolean markedTransformation;

	private boolean didFirstTransform;

	public boolean shootOutFlag;

	public boolean shootOutExecute;

	public int shootOutCount;

	private int level;

	private long rotDelay;

	private long rotDelayAcc;

	private long movDelay;

	private long movDelayAcc;

	private long DELAY_ROT = 1500;

	private long DELAY_MOV = 1500;

	private boolean waitingShootout;

	private long checkedExpected;

	private final ObjectArrayFIFOQueue<RailTrigger> onDock = new ObjectArrayFIFOQueue<RailTrigger>();

	private final ObjectArrayFIFOQueue<RailTrigger> onUndock = new ObjectArrayFIFOQueue<RailTrigger>();

	private boolean allConnectionsLoaded;

	public RailReset activeRailReset;

	public DockValidity currentDockingValidity = DockValidity.UNKNOWN;

	private RailDockerUsableDocked dockedUsable;

	private boolean initDockUsable;

	private int dockCheckCount;

	private boolean audioStarted;

	private RailRelation audioOn;

	public RailController(SegmentController self) {
		this.self = self;
		relativeToRootLocalTransform = new Transform();
		railMovingLocalTransform = new Transform();
		railOriginalLocalTransform = new Transform();
		railMovingLocalAtDockTransform = new Transform();
		railTurretMovingLocalTransform = new Transform();
		basicRelativeTransform = new Transform();
		railUpToThisOriginalLocalTransform = new Transform();
		railTurretMovingLocalTransformTarget = new Transform();
		potentialRelativeToRootLocalTransform = new Transform();
		potentialRailOriginalLocalTransform = new Transform();
		potentialRailTurretMovingLocalTransform = new Transform();
		potentialRailMovingLocalTransform = new Transform();
		potentialBasicRelativeTransform = new Transform();
		potentialRailUpToThisOriginalLocalTransform = new Transform();
		resetTransformations();
	}

	public void onUndock(RailTrigger railTrigger) {
		assert (isOnServer());
		onUndock.enqueue(railTrigger);
	}

	public void onDock(RailTrigger t) {
		assert (isOnServer());
		onDock.enqueue(t);
	}

	public static interface RailTrigger {

		public void handle(RailRelation previous);
	}

	public static RailRequest fromTagR(Tag from, int shift, Set<RailRequest> expected, boolean resetMove) {
		RailRequest railRequestCurrent = null;
		if(from.getType() == Type.STRUCT) {
			final Tag[] v = from.getStruct();
			final byte type = v[0].getByte();
			switch(type) {
				case TYPE_TAG_DOCKED:
					railRequestCurrent = fromTagDocked(v[1], shift, resetMove);
					System.err.println("FROM UID: ::: :: :::::::::::::: " + railRequestCurrent.docked.uniqueIdentifierSegmentController + "\n " + railRequestCurrent.movedTransform.basis);
					break;
				case TYPE_TAG_RAIL_ROOT:
					fromTagRailRoot(v[1]);
					break;
				case TYPE_TAG_ACTIVE_REQUEST:
					// Tag[] st = v[1].getStruct();
					// st[0]
					railRequestCurrent = fromTagActiveRequest(v[1], shift, resetMove);
					break;
				default:
					assert (false) : type;
			}
			if(v.length > 2 && v[2].getType() == Type.STRUCT) {
				fromUIDTag(v[2], shift, expected, resetMove);
			}
		} else {
			// no docking relation
			assert (from.getType() == Type.BYTE);
		}
		return railRequestCurrent;
	}

	private static void fromUIDTag(Tag tag, int shift, Set<RailRequest> expected, boolean resetMove) {
		Tag[] struct = tag.getStruct();
		for(int i = 0; i < struct.length - 1; i++) {
			if(struct[i].getType() == Type.STRUCT) {
				try {
					expected.add(RailRequest.readFromTag(struct[i], shift, resetMove));
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static RailRequest fromTagRailRoot(Tag tag) {
		return null;
	}

	private static RailRequest fromTagActiveRequest(Tag tag, int shift, boolean resetMove) {
		Tag[] v = tag.getStruct();
		RailRequest railRequestCurrent = RailRequest.readFromTag(v[0], shift, resetMove);
		if(railRequestCurrent.disconnect) {
			railRequestCurrent = null;
		} else {
			// System.err.println("[SERVER][RAIL] loaded rail relation from tag: " + railRequestCurrent);
		}
		return railRequestCurrent;
	}

	private static RailRequest fromTagDocked(Tag tag, int shift, boolean resetMove) {
		return fromTagActiveRequest(tag, shift, resetMove);
	}

	/**
	 * used to change the UIDs of a already saved
	 * tag. This is used when importing a sector to
	 * avoid any entity uid collision
	 *
	 * @param tag
	 * @param postfix
	 * @return
	 */
	public static Tag addPostfixToTagUIDS(Tag tag, String postfix, int shift) {
		Set<RailRequest> expected = new ObjectOpenHashSet<RailRequest>();
		RailRequest r = fromTagR(tag, shift, expected, false);
		if(r != null) {
			r.rail.uniqueIdentifierSegmentController += postfix;
			r.docked.uniqueIdentifierSegmentController += postfix;
			System.err.println("[RAIL] LOADED TAG AND APPENDED: " + r.rail.uniqueIdentifierSegmentController + " -> " + r.docked.uniqueIdentifierSegmentController);
			System.err.println("[RAIL] LOADED TAG EXPECTED: " + expected);
			Tag requestTag = getRequestTag(r, postfixExpected(expected, postfix));
			return requestTag;
		} else {
			Tag postfixExpected = postfixExpected(expected, postfix);
			return getRootRailTag(postfixExpected);
		}
	}

	private static Tag postfixExpected(Collection<RailRequest> expected, String postfix) {
		Tag[] dockedUIDs = new Tag[expected.size() + 1];
		int c = 0;
		for(RailRequest s : expected) {
			s.docked.uniqueIdentifierSegmentController = s.docked.uniqueIdentifierSegmentController + postfix;
			dockedUIDs[c] = s.getTag();
			System.err.println("[RAIL] ADDED POSTFIX TO EXPECTED: " + s.docked.uniqueIdentifierSegmentController);
			c++;
		}
		dockedUIDs[dockedUIDs.length - 1] = FinishTag.INST;
		Tag uidTag = new Tag(Type.STRUCT, null, dockedUIDs);
		return uidTag;
	}

	private static Tag getRequestTag(RailRequest railRequestCurrent, Tag uidTag) {
		Tag typeRailByte = new Tag(Type.BYTE, null, TYPE_TAG_ACTIVE_REQUEST);
		Tag requestTag = new Tag(Type.STRUCT, null, new Tag[]{railRequestCurrent.getTag(), FinishTag.INST});
		return new Tag(Type.STRUCT, null, new Tag[]{typeRailByte, requestTag, uidTag != null ? uidTag : new Tag(Type.BYTE, null, (byte) 0), FinishTag.INST});
	}

	public static boolean checkTurretBaseModifiable(SegmentController turret, SegmentController base) {
		if(! (base instanceof PlayerControllable) || ((PlayerControllable) base).getAttachedPlayers().isEmpty()) {
			return true;
		}
		for(RailRelation r : base.railController.next) {
			if(r.docked.getSegmentController() != turret && r.isTurretDock() && (base instanceof PlayerControllable) && ! ((PlayerControllable) base).getAttachedPlayers().isEmpty()) {
				if(! turret.isOnServer()) {
					if(((GameClientState) turret.getState()).getPlayer().getName().compareToIgnoreCase(((PlayerControllable) base).getAttachedPlayers().get(0).getName()) < 0) {
						return false;
					}
				} else {
					return false;
				}
			}
		}
		return true;
	}

	public Tag getDockedUIDsTag(List<RailRelation> next, ObjectOpenHashSet<RailRequest> expectedToDock) {
		Tag[] dockedUIDs = new Tag[next.size() + expectedToDock.size() + 1];
		int c = 0;
		for(int i = 0; i < next.size(); i++) {
			String t = null;
			RailRelation railRelation = next.get(i);
			RailRequest railRequest = railRelation.getRailRequest(this);
			dockedUIDs[c] = railRequest.getTag();
			c++;
		}
		for(RailRequest s : expectedToDock) {
			dockedUIDs[c] = s.getTag();
			c++;
		}
		dockedUIDs[dockedUIDs.length - 1] = FinishTag.INST;
		return new Tag(Type.STRUCT, null, dockedUIDs);
	}

	public void disconnectClient() {
		requestDisconnect();
		this.lastDisconnect = System.currentTimeMillis();
	}

	private void requestDisconnect() {
		RailRequest r = new RailRequest();
		r.disconnect = true;
		r.sentFromServer = isOnServer();
		self.getNetworkObject().railRequestBuffer.add(new RemoteRailRequest(r, self.isOnServer()));
		this.lastDisconnect = System.currentTimeMillis();
	}

	public void connectServer(SegmentPiece docked, SegmentPiece toRail) {
		RailRequest railRequest = getRailRequest(docked, toRail, null, null, DockingPermission.NORMAL);
		railRequest.sentFromServer = false;
		self.onPhysicsRemove();
		self.setSectorId(toRail.getSegmentController().getSectorId());
		if(railRequestCurrent == null && ! isDockedOrDirty()) {
			railRequestCurrent = railRequest;
		}
	}

	public RailRequest getRailRequest(SegmentPiece docked, SegmentPiece toRail, Vector3i railContact, Vector3i railContactToGo, DockingPermission dockingPermission) {
		return getRailRequest(docked, toRail, railContact, railContactToGo, dockingPermission, railTurretMovingLocalTransformTarget, railMovingLocalTransform, railMovingLocalAtDockTransform, self, isOnServer());
	}

	public static RailRequest getRailRequest(SegmentPiece docked, SegmentPiece toRail, Vector3i railContact, Vector3i railContactToGo, DockingPermission dockingPermission, Transform railTurretMovingLocalTransformTarget, Transform railMovingLocalTransform, Transform railMovingLocalAtDockTransform, SegmentController self, boolean onServer) {
		RailRequest r = new RailRequest();
		r.turretTransform.set(railTurretMovingLocalTransformTarget);
		r.movedTransform.set(railMovingLocalTransform);
		r.railMovingLocalAtDockTransform = new Transform(railMovingLocalAtDockTransform);
		r.rail = new VoidUniqueSegmentPiece(toRail);
		r.docked = new VoidUniqueSegmentPiece(docked);
		r.railMovingToDockerPosOnRail = railContactToGo;
		r.dockingPermission = dockingPermission;
		if(railContact == null) {
			toRail.getAbsolutePos(r.railDockerPosOnRail);
			Oriencube orientcubeAlgo = RailRelation.getOrientcubeAlgo(toRail);
			byte orientCubePrimaryOrientation = orientcubeAlgo.getOrientCubePrimaryOrientation();
			if(orientCubePrimaryOrientation == Element.RIGHT || orientCubePrimaryOrientation == Element.LEFT) {
				orientCubePrimaryOrientation = (byte) Element.getOpposite(orientCubePrimaryOrientation);
			}
			Vector3i dirOrient = Element.DIRECTIONSi[orientCubePrimaryOrientation];
			// railContact is the position of the docker from the view of its mother
			r.railDockerPosOnRail.add(dirOrient);
			System.err.println("[RAIL] " + self.getState() + " " + self + " DOCKED AT " + r.railDockerPosOnRail + "; (Primary was: " + Element.getSideString(orientCubePrimaryOrientation) + ")");
		} else {
			r.railDockerPosOnRail.set(railContact);
		}
		r.sentFromServer = onServer;
		return r;
	}

	private void requestConnect(SegmentPiece docked, SegmentPiece toRail, Vector3i railContact, Vector3i railContactToGo, DockingPermission dockingPermission) {
		self.getNetworkObject().railRequestBuffer.add(new RemoteRailRequest(getRailRequest(docked, toRail, railContact, railContactToGo, dockingPermission), self.isOnServer()));
	}

	private void handleRailRequestConnect(RemoteRailRequest r) {
		railRequests.enqueue(r.get());
	}

	public boolean hasInitialRailRequest() {
		return ! railRequests.isEmpty() || railRequestCurrent != null;
	}

	private void handleRailRequestConnectServer(RailRequest r) {
		// System.err.println("[SERVER][RAIL] handleRailRequestConnectServer() removed physics for " + self+"; "+r.toNiceString());
		if(! r.disconnect) {
			if(r.rail.getSegmentController() == null) {
				r.rail.setSegmentControllerFromUID(getState());
			}
		}
		self.onPhysicsRemove();
		this.railRequestCurrent = r;
	}

	private void handleRailRequestConnectClient(RailRequest r) {
		// System.err.println("[CLIENT][RAIL] handleRailRequestConnectClient() removed physics for " + self+"; "+r.toNiceString());
		self.onPhysicsRemove();
		this.railRequestCurrent = r;
	}

	public void updateToFullNetworkObject() {
		if(railRequestCurrent != null) {
			self.getNetworkObject().railRequestBuffer.add(new RemoteRailRequest(railRequestCurrent, self.isOnServer()));
			railRequestCurrent.sentFromServer = true;
		} else
			if(previous != null) {
				requestConnect(previous.docked, previous.rail, previous.currentRailContact, previous.railContactToGo, previous.dockingPermission);
			} else {
			}
	}

	public void updateToNetworkObject() {
		if(railRequestCurrent != null && ! railRequestCurrent.sentFromServer) {
			self.getNetworkObject().railRequestBuffer.add(new RemoteRailRequest(railRequestCurrent, self.isOnServer()));
			railRequestCurrent.sentFromServer = true;
		}
	}

	public void sendClientTurretResetRequest() {
		assert (! isOnServer());
		System.err.println("[CLIENT] sending turret reset request");
		self.getNetworkObject().railMoveToPos.add(new RemoteRailMoveRequest(new RailMove(true), isOnServer()));
	}

	public void flagResetTurretServer() {
		requestedToDefaultFlag = true;
	}

	private void resetTurretServer() {
		assert (isOnServer());
		if(isDockedAndExecuted() && previous.isTurretDockBasic() && ! isChainSendFromClient()) {
			railTurretMovingLocalTransformTarget.setIdentity();
			railTurretMovingLocalTransform.setIdentity();
			self.getNetworkObject().railMoveToPos.add(new RemoteRailMoveRequest(new RailMove(true), isOnServer()));
		}
		for(RailRelation r : next) {
			r.docked.getSegmentController().railController.resetTurretServer();
		}
	}

	public void updateFromNetworkObject() {
		for(int i = 0; i < self.getNetworkObject().railRequestBuffer.getReceiveBuffer().size(); i++) {
			RemoteRailRequest remoteRailRequest = self.getNetworkObject().railRequestBuffer.getReceiveBuffer().get(i);
			handleRailRequestConnect(remoteRailRequest);
		}
		for(int i = 0; i < self.getNetworkObject().railMoveToPos.getReceiveBuffer().size(); i++) {
			RemoteRailMoveRequest r = self.getNetworkObject().railMoveToPos.getReceiveBuffer().get(i);
			RailMove railMove = r.get();
			if(isOnServer()) {
				if(railMove.isTurretBackToDefault) {
					requestedToDefaultFlag = true;
				}
			} else
				if(previous != null) {
					if(railMove.shootOut) {
						shootOutFlag = true;
					}
					if(railMove.isTurretBackToDefault) {
						assert (! isOnServer());
						System.err.println("[CLIENT] received turret reset request for " + self);
						railTurretMovingLocalTransformTarget.setIdentity();
						railTurretMovingLocalTransform.setIdentity();
					} else {
						int side = railMove.rotationSide;
						int rotCode = railMove.rotationCode;
						if(rotCode == RotationType.values().length) {
							if(! railMovingProspectedRots.isEmpty()) {
								// server collided! turn back
								int index = (int) (railMovingProspectedRotTime);
								List<Quat4f> reverse = new ObjectArrayList<Quat4f>();
								Quat4f initial = Quat4fTools.set(railMovingLocalAtDockTransform.basis, new Quat4f());
								for(int j = index - 1; j >= 0; j--) {
									reverse.add(railMovingProspectedRots.get(j));
								}
								reverse.add(initial);
								railMovingProspectedRots.clear();
								railMovingProspectedRots.addAll(reverse);
								railMovingProspectedRotTime = 0;
								railMovingProspectedRotSegmentTime = 0;
								colRotTimer = 0;
							}
						} else {
							if(railMove.hasTranslation) {
								if(railMovingProspectedPos != null && previous.railContactToGo != null && railMovingProspectedRots.isEmpty()) {
									// check if the way to go sent from server
									// is too far
									prefRot.origin.set(railMovingLocalTransform.origin);
									Vector3f originDiff = new Vector3f();
									originDiff.sub(railMovingProspectedPos, railMovingLocalTransform.origin);
									if(originDiff.length() > 1.9f) {
										// we are currently too far
										// which most likely
										// indicated a desync
										// we can warp the block to
										// the current position it
										// would be on server
										railMovingLocalTransform.origin.set(railMovingProspectedPos);
										previous.currentRailContact.set(previous.railContactToGo);
										railMovingLocalAtDockTransform.origin.set(railMovingLocalTransform.origin);
									}
								}
								previous.railContactToGo = new Vector3i(railMove.toGoX, railMove.toGoY, railMove.toGoZ);
							}
							if(railMove.hasLastRailRotation) {
								railMovingLocalTransform.basis.set(new Quat4f(railMove.lastRailTargetX, railMove.lastRailTargetY, railMove.lastRailTargetZ, railMove.lastRailTargetW));
								railMovingProspectedRots.clear();
								railMovingLocalAtDockTransform.basis.set(railMovingLocalTransform.basis);
							}
							previous.rotationCode = RotationType.values()[rotCode];
							previous.rotationSide = (byte) side;
							railSpeedPercent = railMove.speedPercent;
							applyRailGoTo();
						}
					}
				}
		}
		if(! self.getRemoteTransformable().isSendFromClient()) {
			for(int i = 0; i < self.getNetworkObject().railTurretTransSecondary.getReceiveBuffer().size(); i++) {
				RemoteVector4f r = self.getNetworkObject().railTurretTransSecondary.getReceiveBuffer().get(i);
				// System.err.println("RECEIVED TX");
				turretRotX = new Transform();
				turretRotX.setIdentity();
				turretRotX.basis.set(r.getVector(new Quat4f()));
			}
			for(int i = 0; i < self.getNetworkObject().railTurretTransPrimary.getReceiveBuffer().size(); i++) {
				RemoteVector4f r = self.getNetworkObject().railTurretTransPrimary.getReceiveBuffer().get(i);
				// System.err.println("RECEIVED TY");
				turretRotYHelp = new Matrix3f();
				turretRotYHelp.set(r.getVector(new Quat4f()));
				recY = true;
			}
		}
	}

	public void connect(SegmentPiece docked, SegmentPiece toRail, Vector3i railContact, Vector3i railMovingTo, boolean didRotationInPlace, boolean hasAlreadySentFromServer, DockingPermission dockingPermission, boolean fromTag, boolean ignoreCollision) {
		if(self.getDockingController().isInAnyDockingRelation() || toRail.getSegmentController().getDockingController().isInAnyDockingRelation()) {
			if(isOnServer()) {
				docked.getSegmentController().sendControllingPlayersServerMessage(Lng.astr("Cannot dock!\nCan't mix rails with\nold docking system."), ServerMessage.MESSAGE_TYPE_ERROR);
			}
		} else
			if(! isDocked()) {
				if(toRail.getSegmentController().railController.getRoot() == docked.getSegmentController()) {
					System.err.println("[RAIL][CONNECT] " + self.getState() + " " + self + " CANNOT DOCK: LOOP DETECTED");
					docked.getSegmentController().sendControllingPlayersServerMessage(Lng.astr("Cannot dock:\nThis dock would cause\na loop!"), ServerMessage.MESSAGE_TYPE_ERROR);
					return;
				}
				if(toRail.getType() == ElementKeyMap.SHIPYARD_CORE_POSITION) {
					VoidUniqueSegmentPiece d = new VoidUniqueSegmentPiece();
					d.setSegmentController(docked.getSegmentController());
					d.setType(ElementKeyMap.CORE_ID);
					d.voidPos.set(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF);
					docked = d;
				}
				lastLinearVelocityFromRoot.set(0, 0, 0);
				SegmentController toC = toRail.getSegmentController();
				RailRelation relation = new RailRelation(docked, toRail);
				relation.currentRailContact.set(railContact);
				this.previous = relation;
				this.dockedSince = System.currentTimeMillis();
				if(didRotationInPlace) {
					relation.setInRotationServer();
				}
				if(! toC.railController.next.contains(relation)) {
					toC.railController.next.add(relation);
				}
				assert (self == relation.docked.getSegmentController()) : self + " " + relation.docked.getSegmentController();
				assert (this.previous == relation.docked.getSegmentController().railController.previous) : self + " " + relation.docked.getSegmentController();
				assert (previous != null) : self + " -> " + toC;
				assert (getRoot().railController.treeValid()) : self;
				if(self != relation.docked.getSegmentController()) {
					throw new IllegalArgumentException("Ship to dock isnt the same as self: " + self + " " + relation.docked.getSegmentController() + "; This could mean that there are 2 ships with the same UID");
				}
				// calculate potential chain while not changing other transformations
				getRoot().railController.calculatePotentialChainTranformsRecursive(true);
				if(docked.getSegmentController().isVirtualBlueprint() && toRail.getSegmentController().isVirtualBlueprint()) {
					ignoreCollision = true;
				}
				boolean collides = false;
				if(! ignoreCollision) {
					collides = collisionChecker.checkPotentialCollisionWithRail(previous.docked.getSegmentController(), null, true);
				}
				if(! isOnServer()) {
					previous.dockingPermission = dockingPermission;
				}
				boolean factionAllowed = true;
				if(isOnServer()) {
					factionAllowed = checkFactionAllowed(previous, dockingPermission);
				}
				if(getDeepness() > ((GameStateInterface) getState()).getGameState().getMaxChainDocking()) {
					System.err.println("[RAIL][CONNECT] " + self.getState() + " " + self + " CANNOT DOCK: MAX CHAINS " + getDeepness() + " / " + ((GameStateInterface) getState()).getGameState().getMaxChainDocking());
					// disconnect from chain and don't mark other part dirty (no need)
					disconnectFromChain();
					if(isOnServer()) {
						docked.getSegmentController().sendControllingPlayersServerMessage(Lng.astr("Cannot dock:\nNo more than %s chains allowed on this server", ((GameStateInterface) getState()).getGameState().getMaxChainDocking()), ServerMessage.MESSAGE_TYPE_ERROR);
					}
					// mark self dirty to readd to physics
					markTreeDirty();
				} else
					if(isOnServer() && ! factionAllowed && ! docked.getSegmentController().isVirtualBlueprint()) {
						System.err.println("[RAIL][CONNECT] " + self.getState() + " " + self + " CANNOT DOCK: FACTION NOT ALLOWED");
						// disconnect from chain and don't mark other part dirty (no need)
						disconnectFromChain();
						docked.getSegmentController().sendControllingPlayersServerMessage(Lng.astr("Cannot dock:\nFaction permission denied!"), ServerMessage.MESSAGE_TYPE_ERROR);
						// mark self dirty to readd to physics
						markTreeDirty();
					} else
						if(collides) {
							System.err.println("[RAIL][CONNECT] " + self.getState() + " " + self + " CANNOT DOCK: COLLIDES");
							// disconnect from chain and don't mark other part dirty (no need)
							disconnectFromChain();
							if(isOnServer()) {
								docked.getSegmentController().sendControllingPlayersServerMessage(Lng.astr("Cannot dock:\nStructure doesn't fit!"), ServerMessage.MESSAGE_TYPE_ERROR);
							}
							if(isOnServer() && docked.getSegmentController().isVirtualBlueprint()) {
								try {
									throw new Exception("[SERVER] Exception: undocked virtual blueprint. Removing " + docked.getSegmentController() + ". was igonored collision: " + ignoreCollision);
								} catch(Exception e) {
									e.printStackTrace();
								}
								docked.getSegmentController().markForPermanentDelete(true);
							}
							// mark self dirty to readd to physics
							markTreeDirty();
						} else {
							// System.err.println("[RAIL][CONNECT] "+self.getState()+" "+self+" DOCKING PROCEDURE SUCCESSFUL");
							if(! fromTag && isOnServer()) {
								onServerDocking(docked, toRail);
							} else
								if(! isOnServer()) {
									onDockClient();
								}
							// fits: send to clients
							if(isOnServer() && ! hasAlreadySentFromServer) {
								if(docked.getSegmentController() instanceof PlayerControllable && ! ((PlayerControllable) docked.getSegmentController()).getAttachedPlayers().isEmpty()) {
									docked.getSegmentController().lastDockerPlayerServerLowerCase = ((PlayerControllable) docked.getSegmentController()).getAttachedPlayers().get(0).getName().toLowerCase(Locale.ENGLISH);
								}
								// docking permission for server is set in checkFactionAllowed()
								requestConnect(docked, toRail, railContact, railMovingTo, previous.dockingPermission);
							}
							// docking fits and can be done
							markTreeDirty();
						}
			}
	}

	public int getDeepness() {
		return getDeepnessRec(0);
	}

	public int getDockingDepthFromHere() {
		return getDockingDepthFromHere(0);
	}

	private int getDockingDepthFromHere(int count) {
		if(next.size() > 0) {
			count++;
		}
		for(RailRelation a : next) {
			count = Math.max(count, a.getDockedRController().getDockingDepthFromHere(count));
		}
		return count;
	}

	private int getDeepnessRec(int lvl) {
		if(isDocked()) {
			return previous.getRailRController().getDeepnessRec(lvl + 1);
		}
		return lvl;
	}

	public void disconnect() {
		if(this.previous != null) {
			if(isOnServer() && self.isVirtualBlueprint()) {
				self.setVirtualBlueprintRecursive(true);
				try {
					((GameServerState) getState()).getController().writeSingleEntityWithDock(self);
				} catch(IOException e) {
					e.printStackTrace();
				} catch(SQLException e) {
					e.printStackTrace();
				}
				self.setMarkedForDeleteVolatileIncludingDocks(true);
			}
			if(isOnServer()) {
				onUndockedServer(this.previous.currentRailContact);
			} else {
				onUndockClient();
			}
			this.previous.getCurrentRailContactPiece(tmpPieces);
			if(shootOutExecute || (! isOnServer() && shootOutFlag)) {
				if((! isOnServer() && shootOutFlag) && lastMovementDirRelativeToRoot.lengthSquared() == 0) {
					System.err.println("[CLIENT][RAIL][SHOOTOUT] Special case (not moved yet) " + self);
					waitingShootout = true;
				} else {
					System.err.println("[RAIL][DISCONNECT] SHOOTOUT FOR " + getState() + ": " + self + "; DIR: " + lastMovementDirRelativeToRoot);
					this.shootOut = true;
					shootOutExecute = false;
					shootOutFlag = false;
				}
			} else {
				for(int i = 0; i < tmpPieces.length; i++) {
					SegmentPiece lastContact = tmpPieces[i];
					if((lastContact != null && lastContact.getType() == ElementKeyMap.EXIT_SHOOT_RAIL)) {
						System.err.println("[RAIL][" + getState() + "] " + this + " disconnecting from shootout rail. PREPARING SHOOTOUT!");
						if(lastMovementDirRelativeToRoot.lengthSquared() == 0) {
							lastMovementDirRelativeToRoot.set(0.0001f, 0, 0);
						}
						// ((SendableSegmentController) lastContact.getSegmentController()).acivateConnectedSignalsServer(true, lastContact.getAbsoluteIndex());
						this.shootOut = true;
						break;
					}
				}
			}
			getRoot().railController.removeObjectPhysicsRecusively();
			System.err.println("[RAIL] " + self.getState() + " DISCONNECTING FROM RAIL: " + self + " (was connected to rail: " + previous.rail.getSegmentController() + ")");
			RailRelation p = disconnectFromChain();
			// recalculate bottom half of the tree
			p.getRailRController().markTreeDirty();
			// recalculate top half of the tree
			markTreeDirty();
			assert (dirty);
			if(isOnServer()) {
				requestDisconnect();
			}
			System.err.println("[RAIL] " + self.getState() + " DISCONNECTED FROM RAIL: " + self + " LEFT IN RAIL " + p.rail.getSegmentController() + ": " + p.getRailRController().next);
			this.lastDisconnect = System.currentTimeMillis();
			resetTransformations();
		}
	}

	private void onUndockClient() {
		((GameClientState) getState()).onDockChanged(self, false);
		self.onDockingChanged(false);
		if(dockedUsable != null) {
			dockedUsable.man.removePlayerUsable(dockedUsable);
		}
		//INSERTED CODE
		for(RailMoveListener listener : FastListenerCommon.railMoveListeners) {
			listener.onRailUndock(this, previous.rail, previous.docked);
		}
		//
	}

	private void onDockClient() {
		((GameClientState) getState()).onDockChanged(self, true);
		self.onDockingChanged(true);
		if(dockedUsable != null) {
			dockedUsable.man.addPlayerUsable(dockedUsable);
		}
		//INSERTED CODE
		for(RailMoveListener listener : FastListenerCommon.railMoveListeners) {
			listener.onRailDock(this, previous.rail, previous.docked);
		}
		//
	}

	public boolean isOnServer() {
		return self.isOnServer();
	}

	private RailRelation disconnectFromChain() {
		RailRelation p = this.previous;
		((RigidBody) getRoot().getPhysicsDataContainer().getObject()).getLinearVelocity(this.lastLinearVelocityFromRoot);
		boolean remove = p.getRailRController().next.remove(p);
		assert (remove) : "not removed: " + p + ": in col: " + p.getRailRController().next;
		assert (! p.getRailRController().next.contains(p)) : "still contains: " + p + ": in col: " + p.getRailRController().next;
		for(RailRelation a : p.getRailRController().next) {
			assert (a.docked.getSegmentController() != self) : a;
			assert (a.rail.getSegmentController() != self) : a;
		}
		railMovingProspectedRots.clear();
		railMovingProspectedPos = null;
		railMovingProspectedRotSegmentTime = 0;
		railMovingProspectedRotTime = 0;
		this.previous = null;
		return p;
	}

	private void resetTransformations() {
		relativeToRootLocalTransform.setIdentity();
		railOriginalLocalTransform.setIdentity();
		railMovingLocalTransform.setIdentity();
		railMovingLocalAtDockTransform.setIdentity();
		railTurretMovingLocalTransform.setIdentity();
		basicRelativeTransform.setIdentity();
		railUpToThisOriginalLocalTransform.setIdentity();
		railTurretMovingLocalTransformTarget.setIdentity();
		potentialRelativeToRootLocalTransform.setIdentity();
		potentialRailOriginalLocalTransform.setIdentity();
		potentialRailTurretMovingLocalTransform.setIdentity();
		potentialBasicRelativeTransform.setIdentity();
		potentialRailUpToThisOriginalLocalTransform.setIdentity();
		potentialRailMovingLocalTransform.setIdentity();
		railTurretMovingLocalTransformLastUpdate = new Transform();
		railMovingLocalTransformLastUpdate = new Transform();
		railTurretMovingLocalTransformLastUpdateR = new Transform();
		railMovingLocalTransformLastUpdateR = new Transform();
		railMovingLocalTransformTargetBef.setIdentity();
		prefRot.setIdentity();
		railMovingProspectedRots.clear();
		turretRotX = null;
		turretRotYHelp = null;
		didFirstTransform = false;
	}

	public void update(Timer timer) {
		if(! initDockUsable) {
			if(self instanceof ManagedSegmentController<?>) {
				dockedUsable = new RailDockerUsableDocked(((ManagedSegmentController<?>) self).getManagerContainer(), self);
			} else {
				dockedUsable = null;
			}
			initDockUsable = true;
		}
		if(dockedUsable != null && ! isDockedAndExecuted()) {
			dockedUsable.disReq = false;
		}
		if(! isOnServer()) {
			if(previous != null && timer.currentTime < stopAudio) {
				if(! audioStarted) {
					audioOn = previous;
					/*AudioController.fireAudioEvent("RAIL_ACTIVE", new AudioTag[] { AudioTags.GAME, AudioTags.ACTIVATE, AudioTags.SHIP, AudioTags.BLOCK, AudioTags.RAIL }, AudioParam.START, AudioController.ent(self, previous.docked, previous.docked.getAbsoluteIndex(), 5))*/
					AudioController.fireAudioEventID(935, AudioController.ent(self, previous.docked, previous.docked.getAbsoluteIndex(), 5));
				}
			} else {
				if(audioOn != null) {
					/*AudioController.fireAudioEvent("RAIL_ACTIVE", new AudioTag[] { AudioTags.GAME, AudioTags.ACTIVATE, AudioTags.SHIP, AudioTags.BLOCK, AudioTags.RAIL }, AudioParam.STOP, AudioController.ent(self, previous.docked, previous.docked.getAbsoluteIndex(), 5))*/
					if(previous != null) AudioController.fireAudioEventID(934, AudioController.ent(self, previous.docked, previous.docked.getAbsoluteIndex(), 5));
				}
			}
		}
		// if (EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn() && isDockedAndExecuted()) {
		// if (isOnServer()) {
		// previous.rail.getSegmentController().drawPosition(previous.currentRailContact, 0.04f, new Vector4f(1, 1, 0, 1));
		// if (previous.railContactToGo != null) {
		// previous.rail.getSegmentController().drawPosition(previous.railContactToGo, 0.04f, new Vector4f(0, 0, 1, 1));
		// }
		// } else {
		// previous.rail.getSegmentController().drawPosition(previous.currentRailContact, -0.04f, new Vector4f(0, 1, 0, 1));
		// if (previous.railContactToGo != null) {
		// previous.rail.getSegmentController().drawPosition(previous.railContactToGo, -0.04f, new Vector4f(0.0f, 0.001f, 1, 1));
		// }
		// }
		// }
		if(activeRailReset != null) {
			assert (isOnServer());
			if(activeRailReset.waitingForUndock) {
				System.err.println("[RAILRESET] disconnect from rail by reset: " + self);
				disconnect();
				activeRailReset.waitingForUndock = false;
			} else {
				System.err.println("[RAILRESET] redock after reset: " + self);
				connectServer(activeRailReset.docker, activeRailReset.rail);
				activeRailReset = null;
			}
		}
		if(isOnServer() && requestedToDefaultFlag) {
			System.err.println("[SERVER] resetting turret for: " + self);
			resetTurretServer();
			requestedToDefaultFlag = false;
		}
		if(expectedToDock.size() > 0 && timer.currentTime - self.getTimeCreated() > 30000 && timer.currentTime - checkedExpected > 15000) {
			dockCheckCount++;
			if(isOnServer()) {
				GameServerState state = ((GameServerState) getState());
				ObjectIterator<RailRequest> iterator = expectedToDock.iterator();
				while(iterator.hasNext()) {
					RailRequest e = iterator.next();
					Sendable sendable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(e);
					if(sendable != null) {
						SegmentController c = (SegmentController) sendable;
						System.err.println("[SERVER][RAIL] TODOCKCHECK " + self + " in " + self.getSector(new Vector3i()) + " tried to find toDock " + e + "; and found existing loaded entity: " + c + " in " + c.getSector(new Vector3i()));
						c.railController.railRequestCurrent = e;
					} else {
						DatabaseEntry d = state.getDatabaseIndex().getTableManager().getEntityTable().getEntryForFullUID(e.docked.uniqueIdentifierSegmentController);
						if(d != null) {
							DatabaseEntry db = d;
							System.err.println("[SERVER][RAIL] TODOCKCHECK " + self + " in " + self.getSector(new Vector3i()) + " tried to find toDock " + e + "; and found unloaded database entity: " + db);
						} else {
							try {
								throw new Exception("[SERVER][RAIL] TODOCKCHECK " + self + " in " + self.getSector(new Vector3i()) + " tried to find toDock " + e + "; and found no trace of the entity loaded and in the database");
							} catch(Exception ex) {
								ex.printStackTrace();
							}
							// System.err.println("[SERVER][RAIL] "+self+" REMOVING DOCKING CHECK FOR NON-EXISTENT OBJECT: "+e);
							iterator.remove();
						}
					}
				}
			}
			if(expectedToDock.size() > 0) {
				try {
					//throw new Exception("[RAIL] Object " + self + " still needs entities to dock on it: " + expectedToDock + "; currently docked: " + next);
				} catch(Exception e) {
					e.printStackTrace();
				}
				for(RailRelation r : next) {
					// System.err.println("[RAIL] CHECKING ALREADY DOCKED: "+r.docked.getSegmentController().getUniqueIdentifier());
					RailRequest toRem = null;
					ObjectIterator<RailRequest> iterator = expectedToDock.iterator();
					while(iterator.hasNext()) {
						RailRequest e = iterator.next();
						if(r.docked.equalsUniqueIdentifier(e.docked.uniqueIdentifierSegmentController)) {
							try {
								throw new Exception("[RAIL][WARNING] (non serious) FOUND DOCKED OBJECT FOR Object " + self + " still needs entities to dock on it: " + expectedToDock + "; FOUND: " + r.docked.getSegmentController().getUniqueIdentifierFull());
							} catch(Exception ex) {
								ex.printStackTrace();
							}
							iterator.remove();
							break;
						}
					}
					// following is the fix for the import UID bug. can be removed in a few releases
					try {
						for(RailRequest e : expectedToDock) {
							if(r.docked.getSegmentController().getUniqueIdentifier().startsWith(e.docked.uniqueIdentifierSegmentController)) {
								String substring = r.docked.getSegmentController().getUniqueIdentifier().substring(e.docked.uniqueIdentifierSegmentController.length());
								String[] split = substring.split("_");
								if(split != null && split.length == 3) {
									expectedToDock.remove(e);
									try {
										throw new Exception("[RAIL][WARNING] (non serious) FOUND DOCKED OBJECT FOR Object " + self + " SIMILAR: " + e + " WITH " + r.docked.getSegmentController().getUniqueIdentifier());
									} catch(Exception ex) {
										ex.printStackTrace();
									}
									break;
								}
							}
						}
					} catch(Exception m) {
						m.printStackTrace();
					}
				}
			}
			if(isOnServer() && expectedToDock.size() > 0 && dockCheckCount > 5 && dockCheckCount % 5 == 0) {
				((GameServerState) getState()).getController().broadcastMessage(Lng.astr("[ADMIN] WARNING: Ship %s is still waiting for %s docks.\nPlease report this error with server logs", self.toNiceString(), expectedToDock.size()), ServerMessage.MESSAGE_TYPE_ERROR);
			}
			checkedExpected = timer.currentTime;
		}
		handleRailAutoMovementDecision(timer);
		if(isDockedAndExecuted() && timer.currentTime - lastValidityCheck > 800) {
			DockValidity dockingValidity = previous.getDockingValidity();
			previous.docked.getSegmentController().railController.currentDockingValidity = dockingValidity;
			switch(dockingValidity) {
				case RAIL_DOCK_MISSING:
					break;
				case TRACK_MISSING:
					break;
				case SHIPYARD_FAILED:
					if(isOnServer()) {
						if(! self.isMarkedForDeleteVolatile()) {
							System.err.println("[RAIL][SERVER] DISCONNECT FROM RAIL " + self + "! track became invalid");
							disconnect();
						} else {
							System.err.println("[RAIL][SERVER] ***NOT*** DISCONNECTING FROM RAIL " + self + " when track became invalid. object was already marked for volatile removal");
						}
					}
					break;
				case OK:
					break;
				case UNKNOWN:
					break;
				default:
					break;
			}
			lastValidityCheck = timer.currentTime;
		}
		if(! railRequests.isEmpty()) {
			while(! railRequests.isEmpty()) {
				RailRequest dequeue = railRequests.dequeue();
				if(debugFlag) {
					System.err.println("[RAILDEBUG] " + self + ": ACTIVE RAIL QUEUE: " + dequeue);
				}
				if(self.isOnServer()) {
					handleRailRequestConnectServer(dequeue);
				} else {
					handleRailRequestConnectClient(dequeue);
				}
			}
		}
		if(! isOnServer() && shootOutFlag && lastMovementDirRelativeToRoot.lengthSquared() == 0) {
			System.err.println("[CLIENT][RAIL] CLIENT IS WAITING FOR SHOOTOUT DIRECTION");
			waitingShootout = true;
		} else
			if(railRequestCurrent != null) {
				handleCurrentRailRequest();
			}
		if(self.getPhysicsDataContainer() != null) {
			self.getPhysicsDataContainer().updateWithoutPhysicsObjectInterfaceRail = this;
		}
		if(dirty && isRoot()) {
			// System.err.println("[RAIL] " + self.getState() + " RECREATING RAIL FROM ROOT: " + self);
			recreateRootObjectPhysics();
			dirty = false;
			// System.err.println("[RAIL] " + self.getState() + " RECREATING RAIL FROM ROOT DONE: " + self +"; DOCKED AND EXECUTED: "+isDockedAndExecuted()+"; DOCKED OR DIRTY: "+isDockedOrDirty());
		}
		for(int i = 0; i < next.size(); i++) {
			boolean changed = false;
			/*
			 * it is save to check, since in order to actually dock in the first
			 * place, the SegmentController has to already exist. It also exists
			 * as long as it is valid (if removed, either the whole sector
			 * unloads, or the game has ended)
			 */
			if(! getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().containsKey(next.get(i).docked.getSegmentController().getId())) {
				System.err.println("[RAIL] " + getState() + " Object " + next.get(i).docked.getSegmentController() + " docked to " + self + " has been removed from game state");
				next.remove(i);
				i--;
				changed = true;
			}
			if(changed) {
				markTreeDirty();
			}
		}
		if(isOnServer() && isDockedAndExecuted() && isTurretDocked() && markedTransformation && timer.currentTime - lastTurretColCheck > 500) {
			getRoot().railController.calculatePotentialChainTranformsRecursive(true);
			boolean col = collisionChecker.checkPotentialCollisionWithRail(previous.docked.getSegmentController(), null, false);
			if(col) {
				if(lastCurretCollisionHappened == 0) {
					lastCurretCollisionHappened = timer.currentTime;
				} else
					if(getState().getUpdateTime() - lastTurretColCheck > 5000) {
						requestedToDefaultFlag = true;
						lastCurretCollisionHappened = 0;
					}
			} else {
				lastCurretCollisionHappened = 0;
			}
			lastTurretColCheck = getState().getUpdateTime();
			markedTransformation = false;
		}
		debugFlag = false;
	}

	private void handleCurrentRailRequest() {
		if(debugFlag) {
			System.err.println("[RAILDEBUG] " + self + ": ACTIVE RAIL REQUEST: " + railRequestCurrent);
		}
		if(railRequestCurrent.executionTime <= 0) {
			railRequestCurrent.executionTime = System.currentTimeMillis();
		}
		if(isOnServer() && self.getSectorId() != getRoot().getSectorId()) {
			Sector newSector = ((GameServerState) getState()).getUniverse().getSector(getRoot().getSectorId());
			SectorSwitch sw = new SectorSwitch(self, newSector.pos, SectorSwitch.TRANS_JUMP);
			sw.forceEvenOnDocking = true;
			try {
				System.err.println("[RAIL] Switching sector due to rail request: " + self + " " + self.getSectorId() + " -> (RootSector) " + getRoot().getSectorId());
				sw.execute((GameServerState) getState());
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		// System.err.println("[RAIL][UPDATE] " + self.getState() + " EXECUTING RAIL REQUEST ON: " + self + ": " + railRequestCurrent.toNiceString());
		boolean executedSucessfully = executeRailRequest(railRequestCurrent);
		if(executedSucessfully) {
			if(railRequestCurrent.disconnect) {
				// System.err.println("[RAIL][UPDATE] " + self.getState() + " SUCCESSFULLY EXECUTED DISCONNECT RAIL REQUEST ON: " + self + ": " + railRequestCurrent.toNiceString());
			} else {
				SegmentController mother = railRequestCurrent.rail.getSegmentController();
				boolean fromLoad = mother.railController.removeExpectedByUid(self.getUniqueIdentifier());
				// System.err.println("[RAIL][UPDATE] " + self.getState() + " SUCCESSFULLY EXECUTED RAIL REQUEST ON: " + self + ": " + railRequestCurrent.toNiceString()+"; (was from load: "+fromLoad+"; Still to load on mothership: "+mother.railController.expectedToDock.size()+")");
			}
			railRequestCurrent = null;
		} else {
			boolean error = true;
			if(! isOnServer() && System.currentTimeMillis() < railRequestCurrent.executionTime + 10000) {
				// client gets a waiting period to receive objects
				// though the objects are almost guaranteed to arrive in the first update
				// before the rail request is executed for the first time
				error = false;
			}
			System.err.println("[RAIL] Rail Request Error EXECUTION TIME: " + (System.currentTimeMillis() - railRequestCurrent.executionTime));
			if(error) {
				Sendable sA = getState().getLocalAndRemoteObjectContainer().getUidObjectMap().get(railRequestCurrent.rail.uniqueIdentifierSegmentController);
				Sendable sB = getState().getLocalAndRemoteObjectContainer().getUidObjectMap().get(railRequestCurrent.docked.uniqueIdentifierSegmentController);
				try {
					throw new Exception("Dock Failed: Object to dock was not available " + self + ": " + railRequestCurrent + " :: " + sA + " (" + railRequestCurrent.rail.uniqueIdentifierSegmentController + "); " + sB + "(" + railRequestCurrent.docked.uniqueIdentifierSegmentController + ")");
				} catch(Exception e) {
					e.printStackTrace();
				}
				if(isOnServer()) {
					try {
						List<DatabaseEntry> byUIDExact = ((GameServerState) getState()).getDatabaseIndex().getTableManager().getEntityTable().getByUIDExact(DatabaseEntry.removePrefixWOException(railRequestCurrent.docked.uniqueIdentifierSegmentController), 1);
						if(byUIDExact.size() > 0) {
							DatabaseEntry databaseEntry = byUIDExact.get(0);
							System.err.println("[RAIL][ERROR] DOCKED FOUND " + databaseEntry.uid + " IN DATABASE IN SECTOR: " + databaseEntry.sectorPos);
						} else {
							System.err.println("[RAIL][ERROR] DOCKED NOT FOUND IN DATABASE FOR:  " + railRequestCurrent.docked.uniqueIdentifierSegmentController);
						}
					} catch(SQLException e) {
						e.printStackTrace();
					}
					try {
						List<DatabaseEntry> byUIDExact = ((GameServerState) getState()).getDatabaseIndex().getTableManager().getEntityTable().getByUIDExact(DatabaseEntry.removePrefixWOException(railRequestCurrent.rail.uniqueIdentifierSegmentController), 1);
						if(byUIDExact.size() > 0) {
							DatabaseEntry databaseEntry = byUIDExact.get(0);
							System.err.println("[RAIL][ERROR] RAIL FOUND " + databaseEntry.uid + " IN DATABASE IN SECTOR: " + databaseEntry.sectorPos);
						} else {
							System.err.println("[RAIL][ERROR] RAIL NOT FOUND IN DATABASE FOR:  " + railRequestCurrent.rail.uniqueIdentifierSegmentController);
						}
					} catch(SQLException e) {
						e.printStackTrace();
					}
				}
				railRequestCurrent = null;
				if(FactionManager.isNPCFaction(this.self.getFactionId())) {
					if(isOnServer()) {
						this.destroyDockedRecursive();
					}
				}
			} else {
				System.err.println("[DOCK][" + getState() + "] Failed dock of " + railRequestCurrent);
			}
		}
	}

	private boolean removeExpectedByUid(String uniqueIdentifier) {
		ObjectIterator<RailRequest> iterator = expectedToDock.iterator();
		while(iterator.hasNext()) {
			RailRequest e = iterator.next();
			if(uniqueIdentifier.equals(e.docked.uniqueIdentifierSegmentController)) {
				iterator.remove();
				return true;
			}
		}
		return false;
	}

	private RailManagerInterface getRailManagerInterfaceOfRail() {
		if(isDockedAndExecuted()) {
			if(previous.rail.getSegmentController() instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) previous.rail.getSegmentController()).getManagerContainer() instanceof RailManagerInterface) {
				RailManagerInterface rm = (RailManagerInterface) ((ManagedSegmentController<?>) previous.rail.getSegmentController()).getManagerContainer();
				return rm;
			}
		}
		return null;
	}

	public float getRailMassPercent() {
		RailManagerInterface rm = getRailManagerInterfaceOfRail();
		if(rm != null) {
			return rm.getRailMassEnhancer().getCollectionManager().getRailPercent(calculateRailMassIncludingSelf());
		} else {
			return 1;
		}
	}

	private void handleRailAutoMovementDecision(Timer timer) {
		if(isOnServer() && isDockedAndExecuted() && shootOutExecute) {
			// undock because we reached the end of a shootout
			RailShootoutEvent event = new RailShootoutEvent(this, timer);
			StarLoader.fireEvent(event, true);
			disconnect();
			return;
		}
		if(isOnServer() && isDockedAndExecuted() && didFirstTransform && ! isTurretDocked() && ! isShipyardDocked() && getRoot().isSegmentBufferFullyLoadedServerRailRec() && previous.railContactToGo == null && previous.rotationCode == RotationType.NONE && allConnectionsLoaded()) {
			if(previous.delayNextMoveSec > 0) {
				previous.delayNextMoveSec -= timer.getDelta();
				return;
			} else {
				previous.delayNextMoveSec = 0;
			}
			// System.err.println("CHECKING ON::: "+self);
			SegmentPiece[] contactsRes = previous.getCurrentRailContactPiece(tmpPieces);
			for(int i = 0; i < contactsRes.length; i++) {
				SegmentPiece currentRail = contactsRes[i];
				if(currentRail != null) {
					// System.err.println(i+" CHECKING VALIF: "+self+" :: : :: "+currentRail);
					// this is a valid rail under the dock
					// check now if dock can move along
					Oriencube orientcubeAlgoOfCurrentRail = RailRelation.getOrientcubeAlgo(currentRail);
					// secondary orientation is pointing in the
					// direction the rail wants the dock to go
					byte gotoOrientation = orientcubeAlgoOfCurrentRail.getOrientCubeSecondaryOrientation();
					if(gotoOrientation == Element.LEFT) {
						gotoOrientation = Element.RIGHT;
					} else
						if(gotoOrientation == Element.RIGHT) {
							gotoOrientation = Element.LEFT;
						}
					Vector3i dirToMove = new Vector3i(Element.DIRECTIONSi[gotoOrientation]);
					Vector3i posToMove = new Vector3i();
					// position of rail
					currentRail.getAbsolutePos(posToMove);
					// position of next rail
					posToMove.add(dirToMove);
					SegmentPiece blockToMoveOn = previous.rail.getSegmentController().getSegmentBuffer().getPointUnsave(posToMove, tmpPiece);
					// System.err.println("DIR   ----->    "+dirToMove+" ::: "+blockToMoveOn);
					ElementInformation info;
					if(currentRail.getType() == ElementKeyMap.EXIT_SHOOT_RAIL && (blockToMoveOn == null || ! ElementKeyMap.isValidType(blockToMoveOn.getType()) || blockToMoveOn.getType() != ElementKeyMap.EXIT_SHOOT_RAIL || blockToMoveOn.getFullOrientation() != currentRail.getFullOrientation())) {
						System.err.println("[RAIL] Shootout. Reached end of shootout rail. Disconnecting " + self);
						if(previous.railContactToGo == null) {
							System.err.println("[RAIL] Shootout railContactToGo is null");
							return;
						}
						previous.railContactToGo = blockToMoveOn.getAbsolutePos(new Vector3i());
						byte relativeDir = orientcubeAlgoOfCurrentRail.getOrientCubePrimaryOrientationSwitchedLeftRight();
						Vector3i dirRel = new Vector3i(Element.DIRECTIONSi[relativeDir]);
						previous.railContactToGo.add(dirRel);
						applyRailGoTo();
						shootOutFlag = true;
						RailMove railMove = new RailMove(previous.railContactToGo, (byte) (previous.rotationCode.ordinal()), previous.rotationSide, getLastProspectedRot(), railSpeedPercent);
						railMove.shootOut = true;
						self.getNetworkObject().railMoveToPos.add(new RemoteRailMoveRequest(railMove, isOnServer()));
						// break so we dont cause a nullpointer
						break;
					} else
						if(blockToMoveOn != null && ElementKeyMap.isValidType(blockToMoveOn.getType()) && (info = ElementKeyMap.getInfo(blockToMoveOn.getType())).isRailTrack()) {
							// System.err.println("CHECK DIR TO GO "+blockToMoveOn);
							if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()) {
								blockToMoveOn.debugDrawPoint(previous.rail.getSegmentController().getWorldTransform(), 0.1f, 0.1f, 1.0f, 0.3f, 1.0f, 2000L);
							}
							Oriencube orientcubeAlgoOfNextRail = RailRelation.getOrientcubeAlgo(blockToMoveOn);
							// go to the pos that is 'above' the next rail block
							byte relativeDir = orientcubeAlgoOfCurrentRail.getOrientCubePrimaryOrientationSwitchedLeftRight();
							Vector3i dirRel = new Vector3i(Element.DIRECTIONSi[relativeDir]);
							// make sure next rail faces docker block
							if(relativeDir == orientcubeAlgoOfNextRail.getOrientCubePrimaryOrientationSwitchedLeftRight()) {
								// System.err.println("FOUND DIR TO GO SIM");
								if(blockToMoveOn.getType() == ElementKeyMap.EXIT_SHOOT_RAIL) {
									railSpeedPercent = 5f;
								} else {
									if(previous.rail.getSegmentController() instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) previous.rail.getSegmentController()).getManagerContainer() instanceof RailManagerInterface) {
										RailManagerInterface rsm = (RailManagerInterface) ((ManagedSegmentController<?>) previous.rail.getSegmentController()).getManagerContainer();
										railSpeedPercent = rsm.getRailSpeed().getElementManager().getRailSpeedForTrack(currentRail.getAbsoluteIndexWithType4());
										// System.err.println("RAIL SPEED FROM MAN: : "+railSpeedPercent);
									} else {
										railSpeedPercent = 0.5f;
									}
									if(railSpeedPercent == 0) {
										// System.err.println("FOUND DIR TO GO: "+railSpeedPercent);
										return;
									}
								}
								//INSERTED CODE
								// ElementInfo can be marked as a rail controller
								if(! previous.doneInRotationServer() && (currentRail.getInfo().isRailRotator() || currentRail.getType() == ElementKeyMap.RAIL_BLOCK_CW || currentRail.getType() == ElementKeyMap.RAIL_BLOCK_CCW)) {///

									calcRotationDecision(currentRail, orientcubeAlgoOfCurrentRail);
								}
								//
								// we can move here
								previous.railContactToGo = blockToMoveOn.getAbsolutePos(new Vector3i());
								previous.railContactToGo.add(dirRel);
								applyRailGoTo();
								self.getNetworkObject().railMoveToPos.add(new RemoteRailMoveRequest(new RailMove(previous.railContactToGo, (byte) (previous.rotationCode.ordinal()), previous.rotationSide, getLastProspectedRot(), railSpeedPercent), isOnServer()));
							} else {
								// System.err.println("NOT FACING:::: " + currentRail);
								railRotDecision(currentRail, orientcubeAlgoOfCurrentRail);
							}
						} else {
							railRotDecision(currentRail, orientcubeAlgoOfCurrentRail);
						}
				}
			}
		}
	}

//INSERTED CODE
private boolean railRotDecision(SegmentPiece currentRail, Oriencube orientcubeAlgoOfCurrentRail) {
		if(previous.rotationCode == RotationType.NONE && ! previous.doneInRotationServer() && (currentRail.getInfo().isRailRotator() || currentRail.getType() == ElementKeyMap.RAIL_BLOCK_CW || currentRail.getType() == ElementKeyMap.RAIL_BLOCK_CCW)) {
			if(previous.rail.getSegmentController() instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) previous.rail.getSegmentController()).getManagerContainer() instanceof RailManagerInterface) {
				RailManagerInterface rsm = (RailManagerInterface) ((ManagedSegmentController<?>) previous.rail.getSegmentController()).getManagerContainer();
				railSpeedPercent = rsm.getRailSpeed().getElementManager().getRailSpeedForTrack(currentRail.getAbsoluteIndexWithType4());
			} else {
				railSpeedPercent = 0.5f;
			}
			// System.err.println("RAIL SPEED: "+railSpeedPercent);
			if(railSpeedPercent == 0) {
				return false;
			}
			calcRotationDecision(currentRail, orientcubeAlgoOfCurrentRail);
			applyRailGoTo();
			// System.err.println("RAIL ROT  : "+previous.rotationCode.name());
			self.getNetworkObject().railMoveToPos.add(new RemoteRailMoveRequest(new RailMove((byte) (previous.rotationCode.ordinal()), previous.rotationSide, getLastProspectedRot(), railSpeedPercent), isOnServer()));
			return true;
		}
		return false;
	}

	private boolean allConnectionsLoaded() {
		if (!allConnectionsLoaded) {
			SegmentPiece[] contactsRes = previous.getCurrentRailContactPiece(tmpPieces);
			for (int i = 0; i < contactsRes.length; i++) {
				SegmentPiece currentRail = contactsRes[i];
				if (currentRail != null) {
					Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> cc = currentRail.getSegmentController().getControlElementMap().getControllingMap().get(currentRail.getAbsoluteIndex());
					FastCopyLongOpenHashSet fastCopyLongOpenHashSet;
					if (cc != null && (fastCopyLongOpenHashSet = cc.get(ElementKeyMap.ACTIVAION_BLOCK_ID)) != null && fastCopyLongOpenHashSet.size() > 0) {
						for (long a : fastCopyLongOpenHashSet) {
							// autorequest true previously
							SegmentPiece pointUnsave = currentRail.getSegmentController().getSegmentBuffer().getPointUnsave(a, tmpPieceR);
							if (pointUnsave == null) {
								return false;
							}
						}
					}
				}
			}
			allConnectionsLoaded = true;
		}
		return true;
	}

	private boolean isDidFirstTransformation() {
		return didFirstTransform;
	}

	private Quat4f getLastProspectedRot() {
		Quat4f r = new Quat4f();
		Quat4fTools.set(railMovingLocalAtDockTransform.basis, r);
		return r;
	}

	private void calcRotationDecision(SegmentPiece currentRail, Oriencube orientcubeAlgoOfCurrentRail) {
		previous.continueRotation = false;
		// get all activation blocks set to true
		Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> cc = currentRail.getSegmentController().getControlElementMap().getControllingMap().get(currentRail.getAbsoluteIndex());
		FastCopyLongOpenHashSet fastCopyLongOpenHashSet;
		if (cc != null && (fastCopyLongOpenHashSet = cc.get(ElementKeyMap.ACTIVAION_BLOCK_ID)) != null && fastCopyLongOpenHashSet.size() > 0) {
			int activeBlocks = 0;
			for (long a : fastCopyLongOpenHashSet) {
				// autorequest true previously
				SegmentPiece pointUnsave = currentRail.getSegmentController().getSegmentBuffer().getPointUnsave(a, tmpPieceR);
				if (pointUnsave != null && pointUnsave.getType() == ElementKeyMap.ACTIVAION_BLOCK_ID && pointUnsave.isActive()) {
					activeBlocks++;
				}
			}
			// System.err.println("[SERVER][RAIL] rotation block has " + activeBlocks + " connected active blocks");
			if (activeBlocks > 0) {
				previous.rotationSide = orientcubeAlgoOfCurrentRail.getOrientCubePrimaryOrientation();
				//INSERTED CODE
				//isRailRotator handles cases for vanilla and modded blocks
				if(currentRail.getInfo().isRailRotator()){
					this.previous.rotationCode = RotationType.values()[Math.min(8, activeBlocks) + (!currentRail.getInfo().isRotatesClockwise() ? 8 : 0)];
				}
				///
				if (activeBlocks > 8) {
					previous.continueRotation = true;
				}
			// System.err.println("[SERVER][RAIL] set rotation code to " + previous.rotationCode.name());
			} else {
				// no rotation to do. Mark rotation done so this calculation is not done again
				// until the rail is replaced
				previous.setInRotationServer();
			}
		} else {
			previous.rotationSide = orientcubeAlgoOfCurrentRail.getOrientCubePrimaryOrientation();
			//INSERTED CODE
			if(currentRail.getInfo().isRailRotator()){
				this.previous.rotationCode = (currentRail.getInfo().isRotatesClockwise()) ? RotationType.CW_90 : RotationType.CCW_90;
			}
			previous.rotationCode = (currentRail.getType() == ElementKeyMap.RAIL_BLOCK_CCW ? RotationType.CCW_90 : RotationType.CW_90);
		}
	}

	private void applyRailGoTo() {
		// translation
		if (previous.railContactToGo != null) {
			Vector3f to = new Vector3f(railMovingLocalAtDockTransform.origin);
			Vector3f dir = new Vector3f();
			dir.x = previous.railContactToGo.x - previous.currentRailContact.x;
			dir.y = previous.railContactToGo.y - previous.currentRailContact.y;
			dir.z = previous.railContactToGo.z - previous.currentRailContact.z;
			// Vector3f fromPos = previous.rail.getSegmentController().getAbsoluteElementWorldPosition(previous.currentRailContact, new Vector3f());
			// Vector3f toPos = previous.rail.getSegmentController().getAbsoluteElementWorldPosition(previous.currentRailContact, new Vector3f());
			to.add(dir);
			railMovingProspectedPos = to;
			colLinTimer = 0;
		}
		// rotation
		if (previous.rotationCode != RotationType.NONE) {
			Matrix3f from = new Matrix3f(railMovingLocalAtDockTransform.basis);
			Matrix3f fromInv = new Matrix3f(railMovingLocalAtDockTransform.basis);
			fromInv.invert();
			Matrix3f[] dirRotMats = previous.rotationCode.getRotation(previous.rotationSide);
			for (int i = 0; i < previous.rotationCode.rad; i++) {
				Matrix3f res = new Matrix3f(railMovingLocalAtDockTransform.basis);
				Matrix3f dirRot = new Matrix3f(dirRotMats[i]);
				dirRot.invert();
				res.mul(fromInv);
				res.mul(dirRot);
				res.mul(from);
				Quat4f toRot = new Quat4f();
				Quat4fTools.set(res, toRot);
				railMovingProspectedRots.add(toRot);
			}
			assert (railMovingProspectedRots.size() == previous.rotationCode.rad) : previous.rotationCode + ": " + previous.rotationCode.rad + " / " + railMovingProspectedRots.size();
			railMovingProspectedRotTime = 0;
			railMovingProspectedRotSegmentTime = 0;
		}
	// System.err.println("[RAIL] "+getState()+" waypoint dir: "+railMovingLocalAtDockTransform.origin+" -> "+to.origin);
	}

	public StateInterface getState() {
		return self.getState();
	}

	private boolean executeRailRequest(RailRequest r) {
		if (r.disconnect) {
			disconnect();
			return true;
		} else {
			Sendable sA = getState().getLocalAndRemoteObjectContainer().getUidObjectMap().get(r.rail.uniqueIdentifierSegmentController);
			Sendable sB = getState().getLocalAndRemoteObjectContainer().getUidObjectMap().get(r.docked.uniqueIdentifierSegmentController);
			if (sA != null && sB != null) {
				SegmentController railSeg = (SegmentController) sA;
				SegmentController dockSeg = (SegmentController) sB;
				r.rail.setSegmentController(railSeg);
				r.docked.setSegmentController(dockSeg);
				dockSeg.railController.resetTransformations();
				railMovingLocalAtDockTransform.set(r.railMovingLocalAtDockTransform);
				railMovingLocalTransform.set(r.movedTransform);
				railTurretMovingLocalTransform.set(r.turretTransform);
				railTurretMovingLocalTransformTarget.set(railTurretMovingLocalTransform);
				connect(r.docked, r.rail, r.railDockerPosOnRail, r.railMovingToDockerPosOnRail, r.didRotationInPlace, r.sentFromServer, r.dockingPermission, r.fromtag, r.ignoreCollision);
				return true;
			} else {
				System.err.println("[RAIL] Rail Request Failed: Not loaded: " + r.rail.uniqueIdentifierSegmentController + "( " + (sA == null ? "UNLOADED" : "LOADED AS " + sA) + ") or " + r.docked.uniqueIdentifierSegmentController + "(" + (sB == null ? "UNLOADED" : "LOADED AS " + sB) + ")");
			}
			return false;
		}
	}

	private SegmentPiece tmpPc = new SegmentPiece();

	private long stopAudio;

	public void disconnectFromRailRailIfContact(SegmentPiece block, long currentTime) {
		if (isRail()) {
			FastCopyLongOpenHashSet cMap = self.getControlElementMap().getControllingMap().getAll().get(block.getAbsoluteIndex());
			if (cMap != null) {
				for (long l : cMap) {
					// autorequest true previously
					SegmentPiece connectedBlock = self.getSegmentBuffer().getPointUnsave(l, tmpPc);
					if (connectedBlock != null && ElementKeyMap.isValidType(connectedBlock.getType())) {
						boolean found = false;
						for (int i = 0; i < next.size(); i++) {
							RailRelation r = next.get(i);
							if (r.isCurrentRailContactPiece(connectedBlock)) {
								r.docked.getSegmentController().railController.disconnect();
								found = true;
								break;
							}
						}
						if (found) {
							break;
						}
					}
				}
			}
		}
	}

	private void updateAutoRailMovementPos(Timer timer) {
		prefRot.origin.set(railMovingLocalTransform.origin);
		Vector3f originDiff = new Vector3f();
		originDiff.sub(railMovingProspectedPos, railMovingLocalTransform.origin);
		Vector3f originDirNorm = new Vector3f(originDiff);
		originDirNorm.normalize();
		float movement = timer.getDelta() * getRailSpeed() * (railSpeedPercent * getRailMassPercent());
		if (waitingShootout) {
			movement = timer.getDelta() * 50f;
			waitingShootout = false;
		}
		originDirNorm.scale(movement);
		if (originDiff.lengthSquared() == 0 || originDirNorm.length() > originDiff.length()) {
			// put on target
			railMovingLocalTransform.origin.set(railMovingProspectedPos);
		} else {
			railMovingLocalTransform.origin.add(originDirNorm);
		}
		getRoot().railController.calculatePotentialChainTranformsRecursive(true);
		colByLinearMovement = collisionChecker.checkPotentialCollisionWithRail(previous.docked.getSegmentController(), null, false);
		if (colByLinearMovement) {
			movDelay = DELAY_MOV;
			railMovingLocalTransform.origin.set(prefRot.origin);
			colLinTimer += timer.getDelta();
			colLinTimer += (float) (movDelayAcc / 1000d);
			movDelayAcc = 0;
			if (colLinTimer >= 1 && isOnServer()) {
				previous.railContactToGo = new Vector3i(previous.currentRailContact);
				applyRailGoTo();
				previous.delayNextMoveSec = Math.random() * 5f;
				if (railSpeedPercent == 0f) {
					// give at least some speed
					railSpeedPercent = 0.5f;
				}
				self.getNetworkObject().railMoveToPos.add(new RemoteRailMoveRequest(new RailMove(previous.railContactToGo, (byte) (previous.rotationCode.ordinal()), previous.rotationSide, getLastProspectedRot(), railSpeedPercent), isOnServer()));
				// System.err.println("[SERVER][RAIL] collision time limit reached. Returning to last possible");
				return;
			}
		} else if (railMovingProspectedPos.equals(railMovingLocalTransform.origin)) {
			if (isOnServer()) {
				onLeftBlockServer(previous.currentRailContact);
			}
			// we reached the next rail block
			previous.currentRailContact.set(previous.railContactToGo);
			railMovingLocalAtDockTransform.origin.set(railMovingLocalTransform.origin);
			if (isOnServer()) {
				onReachedNextBlockServer(previous.currentRailContact);
			}
			railMovingProspectedPos = null;
			previous.railContactToGo = null;
			colLinTimer = 0;
			stopAudio = System.currentTimeMillis() + 500;
		} else {
			colLinTimer = 0;
		}
	}

	private void updateAutoRailMovementRot(Timer timer) {
		// previous.rotationCode.getRailSpeed();
		railSpeedRot = railSpeedRotBasis * (railSpeedPercent * getRailMassPercent());
		prefRot.set(railMovingLocalTransform);
		// System.err.println(getState()+" TOTAL ROT TIME "+railMovingProspectedRotTime+" ("+timer.getDelta()*railSpeedRot+")  "+railMovingProspectedRots.size());
		boolean matched = false;
		int index = (int) (railMovingProspectedRotTime);
		if (railMovingProspectedRotTime >= previous.rotationCode.rad || index >= railMovingProspectedRots.size()) {
			// put on target
			railMovingLocalTransform.basis.set(railMovingProspectedRots.get(railMovingProspectedRots.size() - 1));
			matched = true;
		// System.err.println("MATCHED :::: "+);
		} else {
			Quat4f from = new Quat4f();
			if (index == 0) {
				Quat4fTools.set(railMovingLocalAtDockTransform.basis, from);
			} else {
				from.set(railMovingProspectedRots.get(index - 1));
			}
			Quat4f res = new Quat4f();
			Quat4Util.slerp(from, railMovingProspectedRots.get(index), railMovingProspectedRotSegmentTime, true, res);
			MatrixUtil.setRotation(railMovingLocalTransform.basis, res);
		// System.err.println("INDEX::: "+index+"::: "+railMovingProspectedRotSegmentTime+"; "+res);
		}
		if (matched) {
			railMovingProspectedRots.clear();
			previous.rotationCode = RotationType.NONE;
			railMovingLocalAtDockTransform.basis.set(railMovingLocalTransform.basis);
			if (!previous.continueRotation) {
				previous.setInRotationServer();
			}
			// if no more translation is queued, do the block activation from that rail now
			if (isOnServer() && railMovingProspectedPos == null) {
				onRotatedOnlyServer();
			}
		} else {
			getRoot().railController.calculatePotentialChainTranformsRecursive(true);
			boolean col = collisionChecker.checkPotentialCollisionWithRail(previous.docked.getSegmentController(), null, false);
			if (col) {
				rotDelay = DELAY_ROT;
				colRotTimer += timer.getDelta();
				colRotTimer += (float) (rotDelayAcc / 1000d);
				rotDelayAcc = 0;
				if (colRotTimer > 1 && isOnServer()) {
					// System.err.println("############################## SERVER COLLISION");
					List<Quat4f> reverse = new ObjectArrayList<Quat4f>();
					Quat4f initial = Quat4fTools.set(railMovingLocalAtDockTransform.basis, new Quat4f());
					for (int i = index - 1; i >= 0; i--) {
						reverse.add(railMovingProspectedRots.get(i));
					}
					reverse.add(initial);
					railMovingProspectedRots.clear();
					railMovingProspectedRots.addAll(reverse);
					colRotTimer = 0;
					railMovingProspectedRotTime = 0;
					railMovingProspectedRotSegmentTime = 0;
					self.getNetworkObject().railMoveToPos.add(new RemoteRailMoveRequest(new RailMove((byte) (RotationType.values().length), (byte) 0, getLastProspectedRot(), railSpeedPercent), isOnServer()));
				}
				railMovingLocalTransform.basis.set(prefRot.basis);
			} else {
				railMovingProspectedRotTime += timer.getDelta() * railSpeedRot;
				railMovingProspectedRotSegmentTime += timer.getDelta() * railSpeedRot;
				if (railMovingProspectedRotSegmentTime >= 1f) {
					railMovingProspectedRotSegmentTime -= 1f;
				}
			// System.err.println("PRORORO: "+railMovingProspectedRotSegmentTime);
			}
		}
	}

	private void onRotatedOnlyServer() {
		assert (isOnServer());
		SegmentPiece[] contactsRes = previous.getCurrentRailContactPiece(tmpPieces);
		for (int i = 0; i < contactsRes.length; i++) {
			SegmentPiece currentRail = contactsRes[i];
			if (currentRail != null) {
				((SendableSegmentController) previous.rail.getSegmentController()).activateSurroundServer(true, currentRail.getAbsolutePos(tmpPos), ElementKeyMap.getSignalTypesActivatedOnSurround());
			}
			//INSERTED CODE
			for (RailMoveListener listener : FastListenerCommon.railMoveListeners) {
				listener.onRailRotate(this, previous.rail, previous.docked);
			}
			///
		}
	}

	private void onServerDocking(SegmentPiece docked, SegmentPiece toRail) {
		((SendableSegmentController) docked.getSegmentController()).activateSurroundServer(true, docked.getAbsolutePos(tmpPos), ElementKeyMap.getSignalTypesActivatedOnSurround());
		((SendableSegmentController) toRail.getSegmentController()).activateSurroundServer(true, toRail.getAbsolutePos(tmpPos), ElementKeyMap.getSignalTypesActivatedOnSurround());
		System.err.println("[RAIL] On Docking, Speed: " + docked.getSegmentController().getSpeedCurrent());
		if (toRail.getType() == ElementKeyMap.PICKUP_AREA) {
			((SendableSegmentController) toRail.getSegmentController()).acivateConnectedSignalsServer(true, toRail.getAbsoluteIndex());
		}
		while (!onDock.isEmpty()) {
			onDock.dequeue().handle(previous);
		}
		self.onDockingChanged(true);
		if (dockedUsable != null) {
			dockedUsable.man.addPlayerUsable(dockedUsable);
		}
	}

	private void onUndockedServer(Vector3i currentRailContact) {
		onLeftBlockServer(currentRailContact);
		((SendableSegmentController) self).activateSurroundServer(false, previous.docked.getAbsolutePos(tmpPos), ElementKeyMap.getSignalTypesActivatedOnSurround());
		while (!onUndock.isEmpty()) {
			onUndock.dequeue().handle(previous);
		}
		self.onDockingChanged(false);
		if (dockedUsable != null) {
			dockedUsable.man.removePlayerUsable(dockedUsable);
		}
	}

	private void onReachedNextBlockServer(Vector3i currentRailContact) {
		assert (isOnServer());
		SegmentPiece[] contactsRes = previous.getCurrentRailContactPiece(tmpPieces);
		for (int i = 0; i < contactsRes.length; i++) {
			SegmentPiece currentRail = contactsRes[i];
			if (currentRail != null && ElementKeyMap.isValidType(currentRail.getType()) && !ElementKeyMap.getInfoFast(currentRail.getType()).isRailRotator()) {
				previous.resetInRotationServer();
				boolean activated = ((SendableSegmentController) previous.rail.getSegmentController()).acivateConnectedSignalsServer(true, currentRail.getAbsoluteIndex());
				if (!activated) {
					((SendableSegmentController) previous.rail.getSegmentController()).activateSurroundServer(true, currentRail.getAbsolutePos(tmpPos), ElementKeyMap.getSignalTypesActivatedOnSurround());
				}
			}
		}
	}

	private void onLeftBlockServer(Vector3i currentRailContact) {
		assert (isOnServer());
		SegmentPiece[] contactsRes = previous.getCurrentRailContactPiece(tmpPieces);
		for (int i = 0; i < contactsRes.length; i++) {
			SegmentPiece currentRail = contactsRes[i];
			if (currentRail != null && ElementKeyMap.isValidType(currentRail.getType()) && !ElementKeyMap.getInfoFast(currentRail.getType()).isRailRotator()) {
				boolean activated = ((SendableSegmentController) previous.rail.getSegmentController()).acivateConnectedSignalsServer(false, currentRail.getAbsoluteIndex());
				if (!activated) {
					((SendableSegmentController) previous.rail.getSegmentController()).activateSurroundServer(false, currentRail.getAbsolutePos(tmpPos), ElementKeyMap.getSignalTypesActivatedOnSurround());
				}
			}
		}
	}

	private void updateAutoRailMovement(Timer timer) {
		long s = System.currentTimeMillis();
		if (isDockedAndExecuted()) {
			if (railMovingProspectedPos != null && previous.railContactToGo != null && railMovingProspectedRots.isEmpty()) {
				if (movDelay > 0) {
					long rem = (long) (timer.getDelta() * 1000f);
					movDelay -= rem;
					movDelayAcc += rem;
				} else {
					// rotation has to be done first
					updateAutoRailMovementPos(timer);
				}
			} else if (!railMovingProspectedRots.isEmpty()) {
				if (rotDelay > 0) {
					long rem = (long) (timer.getDelta() * 1000f);
					rotDelay -= rem;
					rotDelayAcc += rem;
				} else {
					updateAutoRailMovementRot(timer);
					colByLinearMovement = false;
				}
			} else {
				colByLinearMovement = false;
			}
			if (isOnServer() && shootOutFlag && !shootOutExecute) {
				if (shootOutCount > 5) {
					System.err.println("[RAIL][SHOOTOUT] preparing shootout " + getState() + "; " + self);
					if (lastMovementDirRelativeToRoot.lengthSquared() == 0) {
						System.err.println("[RAIL][SHOOTOUT] preparing shootout NOT MOVED YET: " + getState() + "; " + self);
						waitingShootout = true;
						if (previous.railContactToGo != null) {
							System.err.println("[RAIL][SHOOTOUT] TO GO: " + previous.railContactToGo + "; " + railMovingProspectedPos.equals(railMovingLocalTransform.origin));
							updateAutoRailMovementPos(timer);
						}
					} else {
						shootOutExecute = true;
						shootOutFlag = false;
						shootOutCount = 0;
					}
				} else {
					shootOutCount++;
				}
			}
		}
		long took = System.currentTimeMillis() - s;
		if (took > 30) {
			System.err.println("[RAIL] physics of " + self + " took " + took + "; Mov: ProspectMove " + railMovingProspectedPos + "; GoTo " + (previous != null ? previous.railContactToGo : "not Docked") + "; RotSize: " + railMovingProspectedRots.size());
		}
	}

	/**
	 * called by phyiscsExt
	 * @param timeStep
	 */
	public void onOrientatePhysics(float timeStep) {
		if (isDockedAndExecuted() && isTurretDocked()) {
			updateTurretInterpolationF(timeStep);
		}
		if (isDockedAndExecuted() && previous.getRailRController().isDockedAndExecuted()) {
			boolean hadTurretUpdate = previous.getRailRController().doTurretOrientate(timeStep);
			if (hadTurretUpdate) {
				// update docked stuff immediately
				// so it's not having to wait until the next
				// root update (which causes it to lag one frame behind)
				previous.getRailRController().calculateChainTranformsRecursive();
			}
		}
	}

	private boolean checkFactionAllowed(RailRelation rel, DockingPermission providedDockingPermission) {
		SegmentPiece pieceCore = rel.docked;
		SegmentPiece dockingTarget = rel.rail;
		if (this.loadedFromTag) {
			rel.dockingPermission = providedDockingPermission;
			this.loadedFromTag = false;
			return true;
		}
		boolean pub;
		if (pub = SegmentController.isPublicException(dockingTarget, pieceCore.getSegmentController().getFactionId())) {
			rel.dockingPermission = DockingPermission.PUBLIC;
			return true;
		}
		if (dockingTarget.getSegmentController().getFactionId() == 0) {
			return true;
		}
		if (pieceCore.getSegmentController() instanceof Ship && dockingTarget.getSegmentController() instanceof Ship && ((Ship) pieceCore.getSegmentController()).isInFleet() && ((Ship) dockingTarget.getSegmentController()).isInFleet() && ((Ship) pieceCore.getSegmentController()).getFleet() == ((Ship) dockingTarget.getSegmentController()).getFleet()) {
			return true;
		}
		boolean sameFaction = pieceCore.getSegmentController().getFactionId() == dockingTarget.getSegmentController().getFactionId();
		boolean toDockFlownByFactionMember = false;
		if (pieceCore.getSegmentController() instanceof PlayerControllable) {
			for (PlayerState p : ((PlayerControllable) pieceCore.getSegmentController()).getAttachedPlayers()) {
				if (p.getFactionId() == dockingTarget.getSegmentController().getFactionId()) {
					toDockFlownByFactionMember = true;
					break;
				}
			}
		}
		boolean loadedFromTag = this.loadedFromTag;
		this.loadedFromTag = false;
		return !isOnServer() || sameFaction || toDockFlownByFactionMember || loadedFromTag;
	}

	private boolean doTurretOrientate(float timeStep) {
		if (turretRotX != null) {
			Vector3f right = GlUtil.getRightVector(new Vector3f(), turretRotX);
			Vector3f up = GlUtil.getUpVector(new Vector3f(), turretRotX);
			Vector3f forward = GlUtil.getForwardVector(new Vector3f(), turretRotX);
			self.getPhysics().orientateRailTurret(self, forward, up, right, 0, 0, 0, timeStep, getTurretRailSpeed());
			Quat4f rot = new Quat4f();
			Quat4fTools.set(turretRotX.basis, rot);
			if (isOnServer() || self.getRemoteTransformable().isSendFromClient()) {
				self.getNetworkObject().railTurretTransSecondary.add(new RemoteVector4f(new Vector4f(rot), isOnServer()));
			}
			turretRotX = null;
			updateTurretInterpolationF(timeStep);
			if (turretRotYHelp != null) {
				handleTurretPrimary(timeStep);
				turretRotYHelp = null;
			}
			return true;
		}
		if (turretRotYHelp != null) {
			handleTurretPrimary(timeStep);
			turretRotYHelp = null;
			return true;
		}
		return false;
	}

	private void handleTurretPrimary(float timeStep) {
		if (recY) {
			Vector3f rightY = GlUtil.getRightVector(new Vector3f(), turretRotYHelp);
			Vector3f upY = GlUtil.getUpVector(new Vector3f(), turretRotYHelp);
			Vector3f forwardY = GlUtil.getForwardVector(new Vector3f(), turretRotYHelp);
			// System.err.println("RECEIVED::: "+self.getState()+"  "+upY);
			// is already done from orientate
			self.getPhysics().orientateRailTurret(self, forwardY, upY, rightY, 0, 0, 0, timeStep, getTurretRailSpeed());
			recY = false;
			updateTurretInterpolationF(timeStep);
		}
		if (isOnServer() || self.getRemoteTransformable().isSendFromClient()) {
			Quat4f rotY = new Quat4f();
			Quat4fTools.set(turretRotYHelp, rotY);
			self.getNetworkObject().railTurretTransPrimary.add(new RemoteVector4f(new Vector4f(rotY), isOnServer()));
		}
	}

	public void updateChildPhysics(Timer timer) {
		updateAutoRailMovement(timer);
		if (isRoot() && isRail()) {
			calculateChainTranformsRecursive();
		}
	}

	private void updateTurretInterpolationF(float ts) {
		if (!railTurretMovingLocalTransform.basis.equals(railTurretMovingLocalTransformTarget.basis)) {
			Quat4f from = new Quat4f();
			Quat4f to = new Quat4f();
			Quat4fTools.set(railTurretMovingLocalTransform.basis, from);
			Quat4fTools.set(railTurretMovingLocalTransformTarget.basis, to);
			if (from.epsilonEquals(to, 0.001f)) {
				railTurretMovingLocalTransform.basis.set(to);
			} else {
				Quat4f res = new Quat4f();
				Quat4Util.slerp(from, to, Math.max(0, Math.min(1f, ts * getTurretRailSpeed())), res);
				res.normalize();
				railTurretMovingLocalTransform.basis.set(res);
			}
			markTransformationChanged();
		}
		calculateChainTranformsRecursive();
	}

	private void markTransformationChanged() {
		markedTransformation = true;
		for (RailRelation r : next) {
			r.getDockedRController().markTransformationChanged();
		}
	}

	public boolean checkChildCollision(Transform potentialCamera, SegmentController... exceptControllers) {
		Vector3f right = GlUtil.getRightVector(new Vector3f(), potentialCamera);
		Vector3f up = GlUtil.getUpVector(new Vector3f(), potentialCamera);
		Vector3f forward = GlUtil.getForwardVector(new Vector3f(), potentialCamera);
		self.getPhysics().orientateRailTurret(self, forward, up, right, 0, 0, 0, -1, -1, railUpToThisOriginalLocalTransform, potentialRailTurretMovingLocalTransform);
		Transform bef = new Transform(railTurretMovingLocalTransform);
		railTurretMovingLocalTransform.set(potentialRailTurretMovingLocalTransform);
		getRoot().railController.calculatePotentialChainTranformsRecursive(true);
		railTurretMovingLocalTransform.set(bef);
		return collisionChecker.checkPotentialCollisionWithRail(previous.docked.getSegmentController(), exceptControllers, false);
	}

	public void updateDockedFromPhysicsWorld() {
		if (isRoot()) {
			calculateChainTranformsRecursive();
		}
		self.getPhysicsDataContainer().updatePhysical(getState().getUpdateTime());
		for (int i = 0; i < next.size(); i++) {
			next.get(i).getDockedRController().updateDockedFromPhysicsWorld();
		}
	}

	@Override
	public void updateWithoutPhysicsObject() {
		if (isDockedAndExecuted()) {
			if (Vector3fTools.diffLengthSquared(relativeToRootLocalTransform.origin, self.getPhysicsDataContainer().getShapeChild().transform.origin) > 0.001f) {
				lastMovementDirRelativeToRoot.sub(relativeToRootLocalTransform.origin, self.getPhysicsDataContainer().getShapeChild().transform.origin);
				// orientate to the main ship
				getRoot().getWorldTransform().basis.transform(lastMovementDirRelativeToRoot);
			}
			assert (!TransformTools.isNan(relativeToRootLocalTransform));
			self.getPhysicsDataContainer().getShapeChild().transform.set(relativeToRootLocalTransform);
			SegmentController root = getRoot();
			checkRootIntegrity();
			assert (!TransformTools.isNan(root.getWorldTransform()));
			self.getPhysicsDataContainer().updateManuallyWithChildTrans(root.getWorldTransform(), (CompoundShape) root.getPhysicsDataContainer().getShape());
		}
	}

	@Override
	public void checkRootIntegrity() {
	// assert (getRoot().railController.checkIntegrity());
	}

	private void removeObjectPhysicsRecusively() {
		self.onPhysicsRemove();
		// System.err.println("[RAIL] " + getState() + " removeObjectPhysicsRecusively() removed physics for " + self);
		if (!isRoot()) {
			self.getPhysicsDataContainer().setObject(null);
		}
		for (int i = 0; i < next.size(); i++) {
			next.get(i).docked.getSegmentController().railController.removeObjectPhysicsRecusively();
		}
	}

	private void recreateRootObjectPhysics() {
		assert (isRoot());
		Vector3f linVelo = new Vector3f();
		Vector3f angVelo = new Vector3f();
		if (self.getPhysicsDataContainer().getObject() != null && self.getPhysicsDataContainer().getObject() instanceof RigidBody) {
			((RigidBody) self.getPhysicsDataContainer().getObject()).getLinearVelocity(linVelo);
			((RigidBody) self.getPhysicsDataContainer().getObject()).getAngularVelocity(angVelo);
		}
		boolean gotLinearVeloFromMother = false;
		removeObjectPhysicsRecusively();
		CompoundShape motherShape = (CompoundShape) self.getPhysicsDataContainer().getShape();
		if(motherShape == null) return;
		CompoundShapeChild rootChildOriginal = motherShape.getChildList().get(0);
		motherShape.getChildList().clear();
		rootChildOriginal.transform.setIdentity();
		motherShape.getChildList().add(rootChildOriginal);
		calculateChainTranformsRecursive();
		self.flagupdateMass();
		this.level = 0;
		for (int i = 0; i < next.size(); i++) {
			next.get(i).docked.getSegmentController().railController.recreateSubObjectPhysics(motherShape, self, this.level + 1);
		}
		checkRootIntegrity();
		dockingMass = calculateRailMassIncludingSelf();
		calculateInertia();
		assert (motherShape.getChildList().size() > 0);
		if (TransformTools.isNan(self.getWorldTransform())) {
			System.err.println("Exception: (Before CoM) Error adding ship \n" + self + "\nPosition invalid\nPlease send in logs");
			if (isOnServer()) {
				System.err.println("Exception: Error adding ship \n" + self + "\nPosition invalid\nPlease send in logs");
				((GameServerState) getState()).getController().broadcastMessage(Lng.astr("Error adding ship \n%s\nPosition invalid\nPlease send in logs.", self), ServerMessage.MESSAGE_TYPE_ERROR);
			}
			return;
		}
		if (self.getPhysicsDataContainer().lastCenter.length() > 0) {
			// adapt for center of mass only if it didn't start docking
			// and had an update before
			CubesCompoundShape c = (CubesCompoundShape) self.getPhysicsDataContainer().getShape();
			Vector3f centerOfMass = c.getCenterOfMass();
			if (Vector3fTools.isNan(centerOfMass)) {
				System.err.println("Exception: (CoM) Error adding ship \n" + self + "\nPosition invalid\nPlease send in logs");
				if (isOnServer()) {
					System.err.println("Exception: Error adding ship \n" + self + "\nPosition invalid\nPlease send in logs");
					((GameServerState) getState()).getController().broadcastMessage(Lng.astr("Error adding ship \n%s\nPosition invalid\nPlease send in logs.", self), ServerMessage.MESSAGE_TYPE_ERROR);
				}
				return;
			}
			self.getWorldTransform().basis.transform(centerOfMass);
			self.getWorldTransform().origin.add(centerOfMass);
		}
		if (TransformTools.isNan(self.getWorldTransform())) {
			System.err.println("Exception: ");
			if (isOnServer()) {
				System.err.println("Exception: (After CoM)Error adding ship \n" + self + "\nPosition invalid\nPlease send in logs");
				((GameServerState) getState()).getController().broadcastMessage(Lng.astr("Error adding ship \n%s\nPosition invalid\nPlease send in logs.", self), ServerMessage.MESSAGE_TYPE_ERROR);
			}
			return;
		}
		RigidBody bodyFromShape = self.getPhysics().getBodyFromShape(motherShape, self.getMass() > 0 ? dockingMass : 0, self.getWorldTransform());
		bodyFromShape.setUserPointer(self.getId());
		assert (bodyFromShape.getCollisionShape() == motherShape);
		self.getPhysicsDataContainer().setShapeChield(null, -1);
		self.getPhysicsDataContainer().setObject(bodyFromShape);
		assert (!self.getPhysics().containsObject(bodyFromShape));
		for (int i = 0; i < self.getPhysics().getDynamicsWorld().getCollisionObjectArray().size(); i++) {
			CollisionObject co = self.getPhysics().getDynamicsWorld().getCollisionObjectArray().get(i);
			if (co instanceof RigidBodySegmentController) {
				RigidBodySegmentController e = (RigidBodySegmentController) co;
				assert (e.getSegmentController() != self);
			}
		}
		if (TransformTools.isNan(rootChildOriginal.transform)) {
			System.err.println("Exception: (chield) Error adding ship \n" + self + "\nPosition invalid\nPlease send in logs");
			if (isOnServer()) {
				System.err.println("Exception: Error adding ship \n" + self + "\nPosition invalid\nPlease send in logs");
				((GameServerState) getState()).getController().broadcastMessage(Lng.astr("Error adding ship \n%s\nPosition invalid\nPlease send in logs.", self), ServerMessage.MESSAGE_TYPE_ERROR);
			}
			return;
		}
		self.onPhysicsAdd();
		self.getPhysicsDataContainer().setShapeChield(rootChildOriginal, 0);
		// System.err.println("[RAIL] " + getState() + " recreateRootObjectPhysics() added physics for " + self);
		com.bulletphysics.util.ObjectArrayList<CompoundShapeChild> childList = ((CubesCompoundShape) self.getPhysicsDataContainer().getShape()).getChildList();
		for (int i = 0; i < childList.size(); i++) {
			CompoundShapeChild c = childList.get(i);
		// System.err.println("[RAIL] Child #"+i+": "+c);
		}
		gotLinearVeloFromMother = lastLinearVelocityFromRoot.lengthSquared() > 0;
		if (shootOut) {
			lastMovementDirRelativeToRoot.normalize();
			lastMovementDirRelativeToRoot.scale(25);
			System.err.println("[RAIL] SHOOTOUT REGISTERED " + getRoot().getState() + " " + lastMovementDirRelativeToRoot);
			lastLinearVelocityFromRoot.add(lastMovementDirRelativeToRoot);
		}
		bodyFromShape.setLinearVelocity(lastLinearVelocityFromRoot);
		lastLinearVelocityFromRoot.set(0, 0, 0);
		checkRootIntegrity();
		self.getPhysicsDataContainer().updatePhysical(getState().getUpdateTime());
		if (self.getPhysicsDataContainer().getObject() != null && self.getPhysicsDataContainer().getObject() instanceof RigidBody) {
			if (!gotLinearVeloFromMother) {
				if (shootOut) {
					linVelo.add(lastMovementDirRelativeToRoot);
				}
				((RigidBody) self.getPhysicsDataContainer().getObject()).setLinearVelocity(linVelo);
			}
			((RigidBody) self.getPhysicsDataContainer().getObject()).setAngularVelocity(angVelo);
		}
		checkRootIntegrity();
		assert (!isOnServer() || self.getPhysics().containsObject(bodyFromShape));
		if (shootOut) {
			System.err.println("[RAIL] shootout " + self + " shooting out from rail!");
			shootOut = false;
		}
	}

	public boolean isParentDock(SegmentController c) {
		return isInAnyRailRelationWith(c) && c.railController.level <= level;
	}

	public boolean isChildDock(SegmentController c) {
		return isInAnyRailRelationWith(c) && c.railController.level > level;
	}

	private void recreateSubObjectPhysics(CompoundShape root, SegmentController parent, int level) {
		try {
			CompoundShape selfShape = (CompoundShape) self.getPhysicsDataContainer().getShape();
			// clear all other shapes except own
			CompoundShapeChild rootChildOriginal = selfShape.getChildList().get(0);
			selfShape.getChildList().clear();
			selfShape.getChildList().add(rootChildOriginal);
			root.addChildShape(relativeToRootLocalTransform, selfShape.getChildShape(0));
			int index = root.getChildList().size() - 1;
			self.getPhysicsDataContainer().setShapeChield(root.getChildList().get(index), index);
			if (previous == null) {
				throw new NullPointerException("Incosistent dock! " + self + " is not docked but called in chain from parent: " + parent);
			}
			previous.executed = true;
			self.flagupdateMass();
			this.level = level;
			for (int i = 0; i < next.size(); i++) {
				assert (next.get(i).docked.getSegmentController().railController.previous != null) : next.get(i).docked.getSegmentController();
				next.get(i).docked.getSegmentController().railController.recreateSubObjectPhysics(root, self, level + 1);
			}
		} catch (RuntimeException e) {
			System.err.println("EXCEPTION FOR " + self);
			e.printStackTrace();
		}
	}

	private void markTreeDirty() {
		if (previous != null) {
			previous.rail.getSegmentController().railController.markTreeDirty();
		} else {
			dirty = true;
			markRelationsNotExecuted();
		}
		assert (getRoot().railController.treeValid());
	}

	private boolean treeValid() {
		// System.err.println("CHECKING: "+self);
		for (RailRelation a : next) {
			// System.err.println("##CHECKING: "+a.docked.getSegmentController()+" - "+a.docked.getSegmentController().railController.previous);
			if (a.docked.getSegmentController().railController.previous != a) {
				// System.err.println("FAILED ::: "+self+" <---- "+a+"; "+a.docked.getSegmentController().railController.previous);
				return false;
			}
			if (!a.docked.getSegmentController().railController.treeValid()) {
				return false;
			}
		}
		return true;
	}

	private void markRelationsNotExecuted() {
		if (previous != null) {
			previous.executed = false;
		}
		for (int i = 0; i < next.size(); i++) {
			next.get(i).docked.getSegmentController().railController.markRelationsNotExecuted();
		}
	}

	public boolean isRoot() {
		return previous == null;
	}

	public SegmentController getRoot() {
		if (previous != null) {
			return previous.rail.getSegmentController().railController.getRoot();
		} else {
			return self;
		}
	}

	public float calculateRailMassIncludingSelf() {
		float mass = Math.max(0.01f, self.getMassWithoutDockIncludingStation());
		for (int i = 0; i < next.size(); i++) {
			mass += next.get(i).getDockedRController().calculateRailMassIncludingSelf();
		}
		return mass;
	}

	public Vector3f railedInertia = new Vector3f();

	private boolean debugFlag;

	private final ObjectOpenHashSet<RailRequest> expectedToDock = new ObjectOpenHashSet<RailRequest>();

	public Transform railMovingLocalTransformTargetBef = new Transform();

	private static boolean verboseTag = false;

	public Vector3f calculateInertia() {
		railedInertia.set(self.getPhysicsDataContainer().inertia);
		for (int i = 0; i < next.size(); i++) {
			railedInertia.add(next.get(i).getDockedRController().calculateInertia());
		}
		return railedInertia;
	}

	public void calculatePotentialChainTranformsRecursive(boolean useOriginalMovingAsInput) {
		if (isRoot()) {
			potentialRelativeToRootLocalTransform.setIdentity();
			potentialRailMovingLocalTransform.setIdentity();
			potentialRailTurretMovingLocalTransform.setIdentity();
			potentialRailOriginalLocalTransform.setIdentity();
			potentialBasicRelativeTransform.setIdentity();
		} else {
			assert (previous != null);
			potentialRelativeToRootLocalTransform.set(previous.rail.getSegmentController().railController.potentialRelativeToRootLocalTransform);
			potentialRailUpToThisOriginalLocalTransform.set(previous.rail.getSegmentController().railController.potentialRelativeToRootLocalTransform);
			if (useOriginalMovingAsInput) {
				if (railTurretMovingLocalTransform.equals(railTurretMovingLocalTransformLastUpdateR) && railMovingLocalTransform.equals(railMovingLocalTransformLastUpdateR)) {
					potentialBasicRelativeTransform.set(basicRelativeTransform);
					potentialRailOriginalLocalTransform.set(railOriginalLocalTransform);
				} else {
					previous.getBlockTransform(potentialBasicRelativeTransform, potentialRailOriginalLocalTransform, railTurretMovingLocalTransform, railMovingLocalTransform);
					// //since we are using original, we may cache here
					railTurretMovingLocalTransformLastUpdateR.set(railTurretMovingLocalTransform);
					railMovingLocalTransformLastUpdateR.set(railMovingLocalTransform);
				}
			} else {
				previous.getBlockTransform(potentialBasicRelativeTransform, potentialRailOriginalLocalTransform, potentialRailTurretMovingLocalTransform, potentialRailMovingLocalTransform);
			}
			potentialRailUpToThisOriginalLocalTransform.mul(potentialRailOriginalLocalTransform);
			potentialRelativeToRootLocalTransform.mul(potentialBasicRelativeTransform);
		}
		for (int i = 0; i < next.size(); i++) {
			next.get(i).getDockedRController().calculatePotentialChainTranformsRecursive(useOriginalMovingAsInput);
		}
	}

	public String verbose() {
		return getState() + " ------- " + self.getUniqueIdentifier() + " " + railMovingLocalTransform.origin;
	}

	public void calculateChainTranformsRecursive() {
		if (isRoot()) {
			relativeToRootLocalTransform.setIdentity();
			railMovingLocalTransform.setIdentity();
			railTurretMovingLocalTransform.setIdentity();
			railOriginalLocalTransform.setIdentity();
			basicRelativeTransform.setIdentity();
			didFirstTransform = true;
		} else {
			if (isOnServer()) {
				if (previous.rail.getSegmentController().isVirtualBlueprint()) {
					self.setVirtualBlueprint(true);
				}
			}
			assert (previous != null);
			relativeToRootLocalTransform.set(previous.rail.getSegmentController().railController.relativeToRootLocalTransform);
			railUpToThisOriginalLocalTransform.set(previous.rail.getSegmentController().railController.relativeToRootLocalTransform);
			/*
			 * Calculate the transform relative to its parents.
			 * The first 2 transforms are the outs, the other 2 are the ins
			 */
			if (!railTurretMovingLocalTransform.equals(railTurretMovingLocalTransformLastUpdate) || !railMovingLocalTransform.equals(railMovingLocalTransformLastUpdate)) {
				previous.getBlockTransform(basicRelativeTransform, railOriginalLocalTransform, railTurretMovingLocalTransform, railMovingLocalTransform);
				railTurretMovingLocalTransformLastUpdate.set(railTurretMovingLocalTransform);
				railMovingLocalTransformLastUpdate.set(railMovingLocalTransform);
			}
			assert (!TransformTools.isNan(railOriginalLocalTransform));
			assert (!TransformTools.isNan(basicRelativeTransform));
			/*
			 * this is the transformation of everything without this
			 * docked entity's custom movement (rails/turret)
			 *
			 * It is used by the camera for example to determine
			 * the new custom rotation
			 */
			railUpToThisOriginalLocalTransform.mul(railOriginalLocalTransform);
			relativeToRootLocalTransform.mul(basicRelativeTransform);
			assert (!TransformTools.isNan(railUpToThisOriginalLocalTransform));
			assert (!TransformTools.isNan(relativeToRootLocalTransform));
			didFirstTransform = true;
		}
		for (int i = 0; i < next.size(); i++) {
			next.get(i).getDockedRController().calculateChainTranformsRecursive();
		// System.err.println("CALC FOR #"+i+"; "+next.get(i).getDockedRController().self.getUniqueIdentifier()+"; "+next.get(i).getDockedRController().railUpToThisOriginalLocalTransform.origin);
		}
	}

	public Transform getRailMovingLocalTransform() {
		return railTurretMovingLocalTransform;
	}

	public Transform getRailMovingLocalTransformTarget() {
		return railTurretMovingLocalTransformTarget;
	}

	public boolean isRail() {
		return !next.isEmpty();
	}

	public boolean isDockedOrDirty() {
		return isDocked() || dirty;
	}

	public boolean isDocked() {
		return previous != null;
	}

	public boolean isDockedAndExecuted() {
		return previous != null && previous.executed;
	}

	public void fromTag(Tag from, int shift, boolean loadExpectedDocks) {
		Set<RailRequest> exp = new ObjectOpenHashSet<RailRequest>();
		
		boolean resetMove = self.getType() != EntityType.PLANET_ICO; //dont reset partial movement for planets
		railRequestCurrent = fromTagR(from, shift, exp, resetMove);
		if (loadExpectedDocks) {
			for (RailRequest s : exp) {
				addExpectedToDock(s);
			}
		// System.err.println("[SERVER][RAIL] "+self+" Expected to dock "+expectedToDock);
		}
		if (verboseTag) {
			if (railRequestCurrent != null) {
				System.err.println("[SERVER][RAIL] LOADING: " + self);
				System.err.println("[SERVER][RAIL] REQUESTDOCK: " + railRequestCurrent.rail.uniqueIdentifierSegmentController);
				System.err.println("railMovingLocalAtDockTransform:");
				System.err.println(railRequestCurrent.railMovingLocalAtDockTransform.getMatrix(new Matrix4f()));
				System.err.println("movedTransform:");
				System.err.println(railRequestCurrent.movedTransform.getMatrix(new Matrix4f()));
				System.err.println("turretTransform:");
				System.err.println(railRequestCurrent.turretTransform.getMatrix(new Matrix4f()));
				System.err.println("-----------------------------------------------");
			}
		}
		if (railRequestCurrent != null) {
			loadedFromTag = true;
		}
	}

	public Tag getTag() {
		if (railRequestCurrent != null) {
			Tag requestTag = getRequestTag(railRequestCurrent, getDockedUIDsTag(next, expectedToDock));
			assert (requestTag.getType() == Type.STRUCT);
			assert (((Byte) (((Tag[]) requestTag.getValue())[0].getValue())) == TYPE_TAG_ACTIVE_REQUEST);
			return requestTag;
		} else if (previous != null) {
			Tag dockedTag = getDockedTag();
			assert (dockedTag.getType() == Type.STRUCT);
			assert (((Byte) (((Tag[]) dockedTag.getValue())[0].getValue())) == TYPE_TAG_DOCKED);
			return dockedTag;
		} else if (isRoot() && isRail()) {
			Tag rootRailTag = getRootRailTag(getDockedUIDsTag(next, expectedToDock));
			assert (rootRailTag.getType() == Type.STRUCT);
			assert (((Byte) (((Tag[]) rootRailTag.getValue())[0].getValue())) == TYPE_TAG_RAIL_ROOT);
			return rootRailTag;
		} else {
			return new Tag(Type.BYTE, null, (byte) 0);
		}
	}

	private static Tag getRootRailTag(Tag uidTag) {
		// data for root rail if needed goes here
		Tag rootRailByte = new Tag(Type.BYTE, null, TYPE_TAG_RAIL_ROOT);
		Tag requestTag = new Tag(Type.STRUCT, null, new Tag[] { FinishTag.INST });
		return new Tag(Type.STRUCT, null, new Tag[] { rootRailByte, requestTag, uidTag, FinishTag.INST });
	}

	private Tag getDockedTag() {
		Tag dockedByte = new Tag(Type.BYTE, null, TYPE_TAG_DOCKED);
		RailRequest r = new RailRequest();
		// (0.201.027) possible fix for rail missalign
		r.turretTransform.set(railTurretMovingLocalTransform);
		r.movedTransform.set(railMovingLocalTransform);
		r.rail = new VoidUniqueSegmentPiece(previous.rail);
		r.docked = new VoidUniqueSegmentPiece(previous.docked);
		r.railDockerPosOnRail.set(previous.currentRailContact);
		r.railMovingLocalAtDockTransform.set(railMovingLocalAtDockTransform);
		r.didRotationInPlace = previous.doneInRotationServer();
		r.dockingPermission = previous.dockingPermission;
		
		if (verboseTag) {
			System.err.println("[SERVER][RAIL] SAVING: " + self);
			System.err.println("[SERVER][RAIL] REQUESTDOCK: " + r.rail.uniqueIdentifierSegmentController);
			System.err.println("railMovingLocalAtDockTransform:");
			System.err.println(r.railMovingLocalAtDockTransform.getMatrix(new Matrix4f()));
			System.err.println("movedTransform:");
			System.err.println(r.movedTransform.getMatrix(new Matrix4f()));
			System.err.println("turretTransform:");
			System.err.println(r.turretTransform.getMatrix(new Matrix4f()));
			System.err.println("-----------------------------------------------");
		}
		
		Tag requestTag = new Tag(Type.STRUCT, null, new Tag[] { r.getTag(), FinishTag.INST });
		return new Tag(Type.STRUCT, null, new Tag[] { dockedByte, requestTag, getDockedUIDsTag(next, expectedToDock), FinishTag.INST });
	}

	public long isDockedSince() {
		return dockedSince;
	}

	/**
	 * @return the lastDisconnect
	 */
	public long getLastDisconnect() {
		return lastDisconnect;
	}

	/**
	 * @return the relativeToRootLocalTransform
	 */
	public Transform getRelativeToRootLocalTransform() {
		return relativeToRootLocalTransform;
	}

	/**
	 * @return the potentialRelativeToRootLocalTransform
	 */
	public Transform getPotentialRelativeToRootLocalTransform() {
		return potentialRelativeToRootLocalTransform;
	}

	/**
	 * @return the railUpToThisOriginalLocalTransform
	 */
	public Transform getRailUpToThisOriginalLocalTransform() {
		return railUpToThisOriginalLocalTransform;
	}

	public boolean isTurretDocked() {
		return previous != null && previous.isTurretDock();
	}

	public boolean isShipyardDocked() {
		return (previous != null && (previous.isShipyardDock()));
	}

	public boolean isShipyardDockedRecursive() {
		return (previous != null && (previous.isShipyardDock() || previous.getRailRController().isShipyardDockedRecursive()));
	}

	public boolean isClientCameraSet() {
		return !self.isClientOwnObject() || Controller.getCamera() == null || (Controller.getCamera() instanceof InShipCamera && ((InShipCamera) Controller.getCamera()).getHelperCamera().wasDockedOnUpdate());
	}

	public boolean isChainSendFromClient() {
		if (isDockedAndExecuted() && isTurretDocked()) {
			for (RailRelation r : next) {
				if (r.isTurretDock() && r.docked.getSegmentController().getRemoteTransformable().isSendFromClient()) {
					return checkTurretBaseModifiable(r.docked.getSegmentController(), self);
				}
			}
		}
		return false;
	}

	public void modifyDockingRequestNames(String selfUniqueIdentifier, String dockTo) {
		assert (railRequestCurrent != null);
		railRequestCurrent.docked.uniqueIdentifierSegmentController = selfUniqueIdentifier;
		railRequestCurrent.rail.uniqueIdentifierSegmentController = dockTo;
	}

	public boolean hasActiveDockingRequest() {
		return railRequestCurrent != null || isDocked();
	}

	public float getTurretRailSpeed() {
		return 50f;
	}

	public boolean isInAnyRailRelation() {
		return isRail() || isDockedOrDirty();
	}

	public void destroyDockedRecursive() {
		for (RailRelation r : next) {
			r.docked.getSegmentController().railController.destroyDockedRecursive();
			r.docked.getSegmentController().markForPermanentDelete(true);
			r.docked.getSegmentController().setMarkedForDeleteVolatile(true);
		}
	}

	public boolean isPublicPermissionAlongChain() {
		boolean dockedAndExecuted = isDockedAndExecuted();
		if (dockedAndExecuted) {
			if (dockedAndExecuted && previous.dockingPermission == DockingPermission.PUBLIC) {
				return true;
			} else {
				return previous.rail.getSegmentController().railController.isPublicPermissionAlongChain();
			}
		} else {
			return false;
		}
	}

	public int getDockedFactionId(int regularFactionId) {
		if (isDockedAndExecuted() && previous.dockingPermission != DockingPermission.PUBLIC) {
			// apply faction id of mother if there is nothing else set
			// and the dock is not public
			if (regularFactionId == 0) {
				return getNextMotherFaction();
			}
		}
		return regularFactionId;
	}

	private int getNextMotherFaction() {
		if (self.getOriginalFactionId() != 0) {
			return self.getOriginalFactionId();
		} else if (isDockedAndExecuted()) {
			return previous.getRailRController().getNextMotherFaction();
		}
		return 0;
	}

	public SegmentController getChainElementByUID(String uid) {
		if (self.getUniqueIdentifier().equals(uid)) {
			return self;
		}
		for (RailRelation r : next) {
			SegmentController chainElementByUID = r.docked.getSegmentController().railController.getChainElementByUID(uid);
			if (chainElementByUID != null) {
				return chainElementByUID;
			}
		}
		return null;
	}

	public String getRailUID() {
		if (isRoot() || !isDockedAndExecuted()) {
			return "";
		}
		StringBuffer b = new StringBuffer();
		previous.getRailRController().getRailUIDRec(b, previous);
		b.insert(0, "rl");
		return b.toString();
	}

	private void getRailUIDRec(StringBuffer b, RailRelation from) {
		int indexOf = next.indexOf(from);
		b.insert(0, indexOf);
		if (isDockedAndExecuted()) {
			previous.getRailRController().getRailUIDRec(b, previous);
		}
	}

	public boolean isInAnyRailRelationWith(SegmentController segmentController) {
		return getRoot() == segmentController.railController.getRoot();
	}

	public void getAll(List<Sendable> hitBuffer) {
		if (!hitBuffer.contains(self)) {
			hitBuffer.add(self);
		}
		for (RailRelation dock : next) {
			dock.docked.getSegmentController().railController.getAll(hitBuffer);
		}
	}

	public void activateAllAIServer(boolean active, boolean turrets, boolean other, boolean forced) {
		for (RailRelation dock : next) {
			SegmentController docked = dock.docked.getSegmentController();
			if (docked.getElementClassCountMap().get(ElementKeyMap.AI_ELEMENT) > 0 || forced) {
				if (turrets && dock.isTurretDock() && dock.isTurretDockLastAxis()) {
					if (docked instanceof SegmentControllerAIInterface) {
						AIConfigurationInterface aiConfiguration = ((SegmentControllerAIInterface) docked).getAiConfiguration();
						if (aiConfiguration instanceof AIGameConfiguration<?, ?>) {
							AIGameConfiguration<?, ?> c = (AIGameConfiguration<?, ?>) aiConfiguration;
							((AIConfiguationElements<String>) c.get(Types.TYPE)).setCurrentState("Turret", true);
							((AIConfiguationElements<Boolean>) c.get(Types.ACTIVE)).setCurrentState(active, true);
							((Ship) docked).getAiConfiguration().applyServerSettings();
							System.err.println("[SERVER] ACTIVAING TURRET AI " + docked);
						}
					}
				}
				if (other && !dock.isTurretDock()) {
					if (docked instanceof SegmentControllerAIInterface) {
						AIConfigurationInterface aiConfiguration = ((SegmentControllerAIInterface) docked).getAiConfiguration();
						if (aiConfiguration instanceof AIGameConfiguration<?, ?>) {
							AIGameConfiguration<?, ?> c = (AIGameConfiguration<?, ?>) aiConfiguration;
							((AIConfiguationElements<String>) c.get(Types.TYPE)).setCurrentState("Ship", true);
							((AIConfiguationElements<Boolean>) c.get(Types.ACTIVE)).setCurrentState(active, true);
							((Ship) docked).getAiConfiguration().applyServerSettings();
							System.err.println("[SERVER] ACTIVAING NORMAL AI " + docked);
						}
					}
				}
			}
			docked.railController.activateAllAIServer(active, turrets, other, forced);
		}
	}

	public void activateAllAIClient(boolean active, boolean turrets, boolean other) {
		assert (!isOnServer());
		for (RailRelation dock : next) {
			SegmentController docked = dock.docked.getSegmentController();
			if (docked.getElementClassCountMap().get(ElementKeyMap.AI_ELEMENT) > 0) {
				if (turrets && dock.isTurretDock() && dock.isTurretDockLastAxis()) {
					if (docked instanceof SegmentControllerAIInterface) {
						AIConfigurationInterface aiConfiguration = ((SegmentControllerAIInterface) docked).getAiConfiguration();
						if (aiConfiguration instanceof AIGameConfiguration<?, ?>) {
							AIGameConfiguration<?, ?> c = (AIGameConfiguration<?, ?>) aiConfiguration;
							((AIConfiguationElements<String>) c.get(Types.TYPE)).setCurrentState("Turret", true);
							((AIConfiguationElements<Boolean>) c.get(Types.ACTIVE)).setCurrentState(active, true);
						}
					}
				}
				if (other && !dock.isTurretDock()) {
					if (docked instanceof SegmentControllerAIInterface) {
						AIConfigurationInterface aiConfiguration = ((SegmentControllerAIInterface) docked).getAiConfiguration();
						if (aiConfiguration instanceof AIGameConfiguration<?, ?>) {
							AIGameConfiguration<?, ?> c = (AIGameConfiguration<?, ?>) aiConfiguration;
							((AIConfiguationElements<String>) c.get(Types.TYPE)).setCurrentState("Ship", true);
							((AIConfiguationElements<Boolean>) c.get(Types.ACTIVE)).setCurrentState(active, true);
						}
					}
				}
			}
			docked.railController.activateAllAIClient(active, turrets, other);
		}
	}

	public void undockAllClient() {
		assert (!isOnServer());
		for (RailRelation dock : next) {
			dock.docked.getSegmentController().railController.requestDisconnect();
		}
	}

	public void undockAllClientTurret() {
		assert (!isOnServer());
		for (RailRelation dock : next) {
			if (dock.isTurretDock()) {
				dock.docked.getSegmentController().railController.requestDisconnect();
			}
		}
	}

	public void undockAllClientNormal() {
		assert (!isOnServer());
		for (RailRelation dock : next) {
			if (!dock.isTurretDock()) {
				dock.docked.getSegmentController().railController.requestDisconnect();
			}
		}
	}

	public boolean isInAnyRailRelationWith(SimpleTransformableSendableObject target) {
		if (target instanceof SegmentController) {
			return isInAnyRailRelationWith((SegmentController) target);
		}
		return false;
	}

	public boolean isAllAdditionalBlueprintInfoReceived() {
		if (!((SendableSegmentController) self).getNetworkObject().additionalBlueprintData.getBoolean()) {
			return false;
		}
		for (RailRelation dock : next) {
			if (!dock.docked.getSegmentController().railController.isAllAdditionalBlueprintInfoReceived()) {
				return false;
			}
		}
		return true;
	}

	public void sendAdditionalBlueprintInfoToClient() {
		assert (isOnServer());
		if (self instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) self).getManagerContainer() instanceof ActivationManagerInterface) {
			((ManagedSegmentController<?>) self).getManagerContainer().sendFullDestinationUpdate();
		}
		for (RailRelation dock : next) {
			dock.docked.getSegmentController().railController.sendAdditionalBlueprintInfoToClient();
		}
	}

	public boolean isTurretDockLastAxis() {
		return isDockedAndExecuted() && isTurretDocked() && previous.isTurretDockLastAxis();
	}

	public void setAllScrap(boolean b) {
		self.setScrap(b);
		for (RailRelation a : next) {
			a.getDockedRController().setAllScrap(true);
		}
	}

	public double getShieldsRecursive() {
		double shield = 0;
		if (self instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) self).getManagerContainer() instanceof ShieldContainerInterface) {
			ShieldContainerInterface sc = (ShieldContainerInterface) ((ManagedSegmentController<?>) self).getManagerContainer();
			ShieldAddOn shieldAddOn = sc.getShieldAddOn();
			shield = shieldAddOn.getShields();
		}
		if (isDockedAndExecuted()) {
			shield += previous.getRailRController().getShieldsRecursiveP();
		}
		return shield;
	}

	private double getShieldsRecursiveP() {
		double shield = 0;
		if (self instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) self).getManagerContainer() instanceof ShieldContainerInterface) {
			ShieldContainerInterface sc = (ShieldContainerInterface) ((ManagedSegmentController<?>) self).getManagerContainer();
			ShieldAddOn shieldAddOn = sc.getShieldAddOn();
			if (shieldAddOn.getPercentOne() > 0.5) {
				shield += shieldAddOn.getShields() * 0.5f;
			}
		}
		if (isDockedAndExecuted()) {
			shield += previous.getRailRController().getShieldsRecursiveP();
		}
		return shield;
	}

	public void fillShieldsMapRecursive(Vector3f hitWorld, int projectileSectorId, Int2DoubleOpenHashMap shieldMap, Int2DoubleOpenHashMap shieldMapBef, Int2DoubleOpenHashMap shieldMapPercent, Int2IntOpenHashMap railMap, Int2IntOpenHashMap railRootMap, Int2LongOpenHashMap shieldLocalMap) throws SectorNotFoundException {
		if (getRoot().isUsingLocalShields()) {
			if (isDockedAndExecuted()) {
				railMap.put(self.getId(), previous.rail.getSegmentController().getId());
				railRootMap.put(self.getId(), getRoot().getId());
				getRoot().railController.fillShieldsMapRecursive(hitWorld, projectileSectorId, shieldMap, shieldMapBef, shieldMapPercent, railMap, railRootMap, shieldLocalMap);
			} else if (self instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) self).getManagerContainer() instanceof ShieldContainerInterface) {
				ShieldContainerInterface sc = (ShieldContainerInterface) ((ManagedSegmentController<?>) self).getManagerContainer();
				ShieldAddOn shieldAddOn = sc.getShieldAddOn();
				shieldAddOn.getShieldLocalAddOn().fillForExplosion(hitWorld, projectileSectorId, shieldMap, shieldMapBef, shieldMapPercent, shieldLocalMap);
			}
		} else {
			if (self instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) self).getManagerContainer() instanceof ShieldContainerInterface) {
				ShieldContainerInterface sc = (ShieldContainerInterface) ((ManagedSegmentController<?>) self).getManagerContainer();
				ShieldAddOn shieldAddOn = sc.getShieldAddOn();
				if (shieldAddOn.isUsingLocalShields()) {
					shieldAddOn.getShieldLocalAddOn().fillForExplosion(hitWorld, projectileSectorId, shieldMap, shieldMapBef, shieldMapPercent, shieldLocalMap);
				} else {
					shieldMap.put(self.getId(), shieldAddOn.getShields());
					shieldMapBef.put(self.getId(), shieldAddOn.getShields());
					shieldMapPercent.put(self.getId(), (shieldAddOn.getShields() > 0 && shieldAddOn.getShieldCapacity() > 0) ? (shieldAddOn.getShields() / shieldAddOn.getShieldCapacity()) : 0);
				}
			}
			if (isDockedAndExecuted()) {
				railMap.put(self.getId(), previous.rail.getSegmentController().getId());
				railRootMap.put(self.getId(), getRoot().getId());
				previous.getRailRController().fillShieldsMapRecursive(hitWorld, projectileSectorId, shieldMap, shieldMapBef, shieldMapPercent, railMap, railRootMap, shieldLocalMap);
			}
		}
	}

	public RailCollisionChecker getCollisionChecker() {
		return collisionChecker;
	}

	public boolean isFullyLoadedRecursive() {
		if (self.getSegmentBuffer().isFullyLoaded()) {
			for (RailRelation a : next) {
				if (!a.getDockedRController().isFullyLoadedRecursive()) {
					return false;
				}
			}
			return expectedToDock.size() == 0;
		} else {
			return false;
		}
	}

	public void fillElementCountMapRecursive(ElementCountMap currentMapFrom) {
		currentMapFrom.add(self.getElementClassCountMap());
		for (RailRelation a : next) {
			a.getDockedRController().fillElementCountMapRecursive(currentMapFrom);
		}
	}

	public boolean isAnyChildOf(SegmentController controller) {
		if (controller == self) {
			return true;
		}
		if (previous != null) {
			return previous.getRailRController().isAnyChildOf(controller);
		}
		return false;
	}

	public boolean isOkToDockClientCheck(SegmentPiece pieceCore, SegmentPiece dockingTarget, DockingFailReason r) {
		if (!(pieceCore.getSegmentController() instanceof Ship)) {
			r.reason = Lng.str("Can only dock with a ship");
			return false;
		}
		if (pieceCore.getSegmentController().isVirtualBlueprint()) {
			r.reason = Lng.str("Cannot dock a design.");
			return false;
		}
		if (dockingTarget.getSegmentController().isVirtualBlueprint()) {
			r.reason = Lng.str("Cannot dock to a design.");
			return false;
		}
		if (pieceCore.getSegmentController().railController.isDockedAndExecuted()) {
			r.reason = Lng.str("Ship already docked!");
			return false;
		}
		boolean pub;
		if (pub = SegmentController.isPublicException(dockingTarget, pieceCore.getSegmentController().getFactionId())) {
			return true;
		}
		if (dockingTarget.getSegmentController().getFactionId() == 0) {
			return true;
		}
		boolean sameFaction = pieceCore.getSegmentController().getFactionId() == dockingTarget.getSegmentController().getFactionId();
		if (sameFaction) {
			return true;
		}
		boolean toDockFlownByFactionMember = false;
		if (pieceCore.getSegmentController() instanceof PlayerControllable) {
			for (PlayerState p : ((PlayerControllable) pieceCore.getSegmentController()).getAttachedPlayers()) {
				if (p.getFactionId() == dockingTarget.getSegmentController().getFactionId()) {
					return true;
				}
			}
		}
		r.reason = Lng.str("Faction permission denied");
		return false;
	}
	public static long RAIL_PICKUP_DELAY = 5000;
	public void connectClient(SegmentPiece docked, SegmentPiece toRail) {
		long delay = RAIL_PICKUP_DELAY;
		//INSERTED CODE
		RailConnectAttemptClientEvent event = new RailConnectAttemptClientEvent(this, docked, toRail, lastDockRequest);
		StarLoader.fireEvent(event, false);
		///
		if (System.currentTimeMillis() - lastDockRequest > delay && System.currentTimeMillis() - this.lastDisconnect > delay) {
			resetTransformations();
			railMovingLocalAtDockTransform.setIdentity();
			System.err.println("[CLIENT] FIRST DOCKING REQUEST MADE: " + docked + " -> " + toRail);
			requestConnect(docked, toRail, null, null, null);
			lastDockRequest = System.currentTimeMillis();
		} else {
			GameClientState s = ((GameClientState) docked.getSegmentController().getState());
			if (System.currentTimeMillis() - lastDockRequest < delay) {
				s.getController().popupAlertTextMessage(Lng.str("Can dock again in %s sec", (int) (delay - (System.currentTimeMillis() - lastDockRequest)) / 1000), "dockDelay", 0);
			} else if (System.currentTimeMillis() - this.lastDisconnect < delay) {
				s.getController().popupAlertTextMessage(Lng.str("Can dock again in %s sec", (int) (delay - (System.currentTimeMillis() - this.lastDisconnect)) / 1000), "dockDelay", 0);
			}
		}
	}

	public SegmentController getOneBeforeRoot() {
		if (previous != null && previous.rail.getSegmentController().railController.previous != null) {
			return previous.rail.getSegmentController().railController.getRoot();
		} else {
			return self;
		}
	}

	public boolean isInAnyRailRelationWith(SimpleGameObject target) {
		return target != null && target instanceof SimpleTransformableSendableObject && isInAnyRailRelationWith((SimpleTransformableSendableObject) target);
	}

	public void undockAllServer() {
		assert (isOnServer());
		List<RailRelation> a = new ObjectArrayList<RailRelation>();
		a.addAll(next);
		for (RailRelation r : a) {
			System.err.println("[RAIL][UNDOCKALL] UNDOCKING " + r.docked.getSegmentController() + " from " + self);
			r.docked.getSegmentController().railController.disconnect();
		}
	}

	@Override
	public boolean isBlockNextToLogicOkTuUse(SegmentPiece fromBlockSurround, SegmentPiece toReplace) {
		if (toReplace != null && fromBlockSurround != null && ElementKeyMap.isValidType(toReplace.getType()) && ElementKeyMap.isValidType(fromBlockSurround.getType())) {
			ElementInformation toReplaceInfo = ElementKeyMap.getInfoFast(toReplace.getType());
			ElementInformation fromBlockSurroundInfo = ElementKeyMap.getInfoFast(fromBlockSurround.getType());
			return ((toReplaceInfo.isRailTrack() && fromBlockSurroundInfo.isRailTrack()) || ((toReplaceInfo.isRailRotator() && fromBlockSurroundInfo.isRailRotator())));
		}
		return false;
	}

	@Override
	public void afterReplaceBlock(SegmentPiece fromBlockSurroundOriginal, SegmentPiece toReplace) {
		for (RailRelation n : next) {
			SegmentPiece[] currentRailContactPiece = n.getCurrentRailContactPiece(new SegmentPiece[6]);
			for (int k = 0; k < currentRailContactPiece.length; k++) {
				if (currentRailContactPiece[k] != null && toReplace.equalsPos(currentRailContactPiece[k].getAbsolutePos(new Vector3i()))) {
					n.resetInRotationServer();
				}
			}
		}
	}

	@Override
	public boolean fromBlockOk(SegmentPiece fromBlockSurround) {
		return ElementKeyMap.isValidType(fromBlockSurround.getType()) && (ElementKeyMap.getInfo(fromBlockSurround.getType()).isRailTrack() || ElementKeyMap.getInfo(fromBlockSurround.getType()).isRailRotator());
	}

	@Override
	public boolean equalsBlockData(SegmentPiece fromBlockSurround, SegmentPiece toReplace) {
		return fromBlockSurround.getType() == toReplace.getType() && fromBlockSurround.getOrientation() == toReplace.getOrientation();
	}

	@Override
	public void modifyReplacement(SegmentPiece fromBlockSurround, SegmentPiece toReplace) {
	}

	public void markForPermanentDelete(boolean b) {
		self.markForPermanentDelete(b);
		for (RailRelation r : next) {
			r.docked.getSegmentController().railController.markForPermanentDelete(b);
		}
	}

	public void calcBoundingSphereTotal(BoundingSphere b) {
		// total radius is the maximum of sphere radius including the relative positions to the root
		b.radius = Math.max(b.radius, relativeToRootLocalTransform.origin.length() + self.getBoundingSphere().radius);
		for (RailRelation r : next) {
			r.docked.getSegmentController().railController.calcBoundingSphereTotal(b);
		}
	}

	public void setFactionIdForEntitiesWithoutFactionBlock(int factionId) {
		if (self instanceof ManagedSegmentController<?>) {
			if (((ManagedSegmentController<?>) self).getManagerContainer().getFactionBlockPos() == Long.MIN_VALUE) {
				self.setFactionId(factionId);
				for (RailRelation r : next) {
					r.docked.getSegmentController().railController.setFactionIdForEntitiesWithoutFactionBlock(factionId);
				}
			}
		}
	}

	public void resetFactionForEntitiesWithoutFactionBlock(int onlyChangeForFaction) {
		if (onlyChangeForFaction == self.getFactionId()) {
			self.setFactionId(0);
		}
		for (RailRelation r : next) {
			if (r.docked.getSegmentController().getElementClassCountMap().get(ElementKeyMap.FACTION_BLOCK) == 0) {
				r.docked.getSegmentController().railController.resetFactionForEntitiesWithoutFactionBlock(onlyChangeForFaction);
			}
		}
	}

	public boolean isDebugFlag() {
		return debugFlag;
	}

	public void setDebugFlag(boolean debugFlag) {
		this.debugFlag = debugFlag;
		for (RailRelation r : next) {
			r.docked.getSegmentController().railController.setDebugFlag(true);
		}
	}

	public void addExpectedToDock(RailRequest railRequest) {
		this.expectedToDock.add(railRequest);
	}

	public ObjectOpenHashSet<RailRequest> getExpectedToDock() {
		return expectedToDock;
	}

	public float getRailSpeed() {
		return self.getConfigManager().apply(StatusEffectType.RAIL_SPEED, railSpeed);
	}

	public int getTotalDockedFromHere() {
		int docked = next.size();
		for (RailRelation r : next) {
			docked += r.docked.getSegmentController().railController.getTotalDockedFromHere();
		}
		return docked;
	}

	public int getDockedCount() {
		int docked = next.size();
		return docked;
	}

	public int getTurretCount() {
		int docked = 0;
		for (RailRelation r : next) {
			if (r.isTurretDock()) {
				docked++;
			}
		}
		return docked;
	}

	public int getNormalDockCount() {
		int docked = 0;
		for (RailRelation r : next) {
			if (!r.isTurretDock()) {
				docked++;
			}
		}
		return docked;
	}

	public boolean hasTurret() {
		for (RailRelation r : next) {
			if (r.isTurretDock()) {
				return true;
			}
		}
		return false;
	}

	public RailRequest getCurrentRailRequest() {
		return railRequestCurrent;
	}

	public void onClear() {
		allConnectionsLoaded = false;
		didFirstTransform = false;
	}

	public void resetRailAll() {
		getRoot().railController.resetRailRec();
	}

	private void resetRailRec() {
		if (!isRoot()) {
			resetRail();
		}
		for (RailRelation e : next) {
			e.docked.getSegmentController().railController.resetRailRec();
		}
	}

	public void resetRail() {
		if (isDockedAndExecuted()) {
			RailReset r = new RailReset();
			r.docker = previous.docked.getSegmentController().getSegmentBuffer().getPointUnsave(previous.docked.getAbsoluteIndex());
			r.rail = previous.rail.getSegmentController().getSegmentBuffer().getPointUnsave(previous.rail.getAbsoluteIndex());
			if (r.rail.getType() != 0 && r.rail.getInfo().isRailDockable() && r.docker.getType() != 0 && r.docker.getInfo().isRailDocker()) {
				activeRailReset = r;
			} else {
				System.err.println("[RAIL] ERROR: INCOMPATIBLE TYPES: " + self);
			}
		} else {
			System.err.println("[RAIL] ELEMENT IS NOT DOCKED: " + self);
		}
	}

	private class RailReset {

		public SegmentPiece rail;

		public SegmentPiece docker;

		public boolean waitingForUndock = true;
	}

	public class RailDockerUsableDocked extends SegmentControllerUsable {

		private boolean disReq;

		public RailDockerUsableDocked(ManagerContainer<?> man, SegmentController self) {
			super(man, self);
		}

		public void handleMouseEvent(ControllerStateUnit unit, MouseEvent e) {
		}

		@Override
		public void onPlayerDetachedFromThisOrADock(ManagedUsableSegmentController<?> originalCaller, PlayerState pState, PlayerControllable newAttached) {
		}

		public void onSwitched(boolean on) {
		}

		@Override
		public void onLogicActivate(SegmentPiece selfBlock, boolean oldActive, Timer timer) {
		}

		@Override
		public boolean isAddToPlayerUsable() {
			return true;
		}

		@Override
		public WeaponRowElementInterface getWeaponRow() {
			return new WeaponSegmentControllerUsableElement(this);
		}

		@Override
		public boolean isControllerConnectedTo(long index, short type) {
			return true;
		}

		public float getWeaponSpeed() {
			return 0;
		}

		public float getWeaponDistance() {
			return 0;
		}

		@Override
		public boolean isPlayerUsable() {
			return getSegmentController().railController.isDockedAndExecuted();
		}

		@Override
		public long getUsableId() {
			return PlayerUsableInterface.USABLE_ID_UNDOCK;
		}

		@Override
		public void handleKeyPress(ControllerStateInterface unit, Timer timer) {
			if (unit.isDown(KeyboardMappings.SHIP_PRIMARY_FIRE) && isDockedAndExecuted()) {
				if (!isOnServer() && !disReq) {
					if (!getSegmentController().isVirtualBlueprint()) {
						String lst = getSegmentController().railController.getOneBeforeRoot().lastDockerPlayerServerLowerCase;
						if (!(getSegmentController().railController.getRoot() instanceof ShopSpaceStation) || (self instanceof PlayerControllable && (((PlayerControllable) self).getAttachedPlayers().isEmpty() || lst.length() == 0 || ((AbstractOwnerState) ((PlayerControllable) self).getAttachedPlayers().get(0)).getName().toLowerCase(Locale.ENGLISH).equals(lst)))) {
							System.err.println("[CLIENT][RAILBEAM] Disconnecting from tail");
							getSegmentController().railController.disconnectClient();
							disReq = true;
						} else {
							((GameClientController) getState().getController()).popupAlertTextMessage("ALDC", Lng.str("Can't undock from shop!\nOnly %s can undock!", getSegmentController().lastDockerPlayerServerLowerCase), 0);
						}
					} else {
						((GameClientController) getState().getController()).popupAlertTextMessage("ALDC", Lng.str("Can't undock a design!"), 0);
					}
				}
			}
		}

		@Override
		public ManagerReloadInterface getReloadInterface() {
			return null;
		}

		@Override
		public ManagerActivityInterface getActivityInterface() {
			return null;
		}

		@Override
		public String getName() {
			return Lng.str("Rail Docker");
		}

		@Override
		public void handleKeyEvent(ControllerStateUnit unit, KeyboardMappings mapping, Timer timer) {
		}

		@Override
		public void addHudConext(ControllerStateUnit unit, HudContextHelpManager h, Hos hos) {
			h.addHelper(KeyboardMappings.SHIP_PRIMARY_FIRE, Lng.str("Undock"), hos, ContextFilter.IMPORTANT);
		}

		@Override
		public String getWeaponRowName() {
			return Lng.str("Undock");
		}

		@Override
		public short getWeaponRowIcon() {
			return ElementKeyMap.RAIL_BLOCK_DOCKER;
		}
	}

	public boolean isPowered() {
		return currentDockingValidity == RailRelation.DockValidity.OK;
	}
	public void getDockedRecusive(Collection<SegmentController> out) {
		out.add(self);
		for(RailRelation r : next){
			r.getDockedRController().getDockedRecusive(out);
		}
	}

	/**
	 * helps to accumulate sections of a rail hirachy
	 * @author schem
	 */
	public enum RailTarget implements TranslatableEnum {

		ROOT_ONLY, EVERYTHING, DOCKED_ONLY, DOCKED_AND_UPWARDS;

		public String getName() {
			return switch(this) {
				case DOCKED_AND_UPWARDS -> Lng.str("This & what is docked to it");
				case DOCKED_ONLY -> Lng.str("Only this");
				case EVERYTHING -> Lng.str("Root and all docks");
				case ROOT_ONLY -> Lng.str("Only Root");
				default -> throw new RuntimeException("Unknown value: " + this.name());
			};
		}

		public void getTargets(SegmentController c, Collection<SegmentController> out) {
			switch(this) {
				case DOCKED_AND_UPWARDS -> c.railController.getDockedRecusive(out);
				case DOCKED_ONLY -> out.add(c);
				case EVERYTHING -> {
					out.clear();
					c.railController.getRoot().railController.getDockedRecusive(out);
				}
				case ROOT_ONLY -> out.add(c.railController.getRoot());
				default -> throw new RuntimeException("Unknown value: " + this.name());
			}
		}
	}
}
