package org.schema.game.common.controller;

import api.config.BlockConfig;
import api.listener.events.block.SegmentPieceKillEvent;
import api.mod.StarLoader;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.SolverConstraint;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.GameClientController;
import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.controller.manager.ingame.*;
import org.schema.game.client.controller.manager.ingame.BuildInstruction.Add;
import org.schema.game.client.controller.manager.ingame.BuildInstruction.Remove;
import org.schema.game.client.controller.tutorial.states.PlaceElementTestState;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.client.view.buildhelper.BuildHelper;
import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.client.view.gui.buildtools.BuildToolsPanel;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.HitType;
import org.schema.game.common.controller.damage.acid.AcidDamageManager;
import org.schema.game.common.controller.damage.beam.DamageBeamHittable;
import org.schema.game.common.controller.damage.projectile.ProjectileController;
import org.schema.game.common.controller.damage.projectile.ProjectileHittable;
import org.schema.game.common.controller.elements.*;
import org.schema.game.common.controller.elements.beam.repair.RepairBeamHandler;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;
import org.schema.game.common.controller.elements.warpgate.WarpgateCollectionManager;
import org.schema.game.common.controller.generator.EmptyCreatorThread;
import org.schema.game.common.controller.rails.DockingFailReason;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.VoidSegmentPiece;
import org.schema.game.common.data.element.*;
import org.schema.game.common.data.element.ElementInformation.ResourceInjectionType;
import org.schema.game.common.data.explosion.AfterExplosionCallback;
import org.schema.game.common.data.explosion.ExplosionData;
import org.schema.game.common.data.explosion.ExplosionRunnable;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.physics.CubeRayCastResult;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.inventory.InventoryHolder;
import org.schema.game.common.data.world.*;
import org.schema.game.network.objects.NetworkSegmentController;
import org.schema.game.network.objects.remote.RemoteSegmentPiece;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.simulation.npc.diplomacy.DiplomacyAction.DiplActionType;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.sound.controller.AudioController;
import org.schema.schine.sound.controller.MusicTags;

import javax.vecmath.Vector3f;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public abstract class EditableSendableSegmentController extends SendableSegmentController implements ParticleHandler, Salvage, DamageBeamHittable, ProjectileHittable, BuilderInterface {

	private static final long MIN_TIME_BETWEEN_EDITS = 50;

	private final Vector3i absPosCache = new Vector3i();

	private boolean flagCharacterExitCheckByExplosion;

	private Object flagCoreDestroyedByExplosion;

	private Vector3f tmpPosA = new Vector3f();

	private Vector3f tmpPosB = new Vector3f();

	private Vector3i local = new Vector3i();

	public final DryTestBuild dryBuildTest = new DryTestBuild();

	private Damager lastSalvaged = null;

	private final AcidDamageManager acidDamageManagerServer;

	public EditableSendableSegmentController(StateInterface state) {
		super(state);
		if(isOnServer()) {
			this.acidDamageManagerServer = new AcidDamageManager(this);
		} else {
			this.acidDamageManagerServer = null;
		}
	}

	public PulseController getPulseController() {
		if(!isOnServer()) {
			return ((GameClientState) getState()).getPulseController();
		} else {
			return ((GameServerState) getState()).getUniverse().getSector(getSectorId()).getPulseController();
		}
	}

	protected void onSalvaged(Damager from) {
		lastSalvaged = from;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SegmentController#allowedToEdit(int)
	 */
	@Override
	public boolean allowedToEdit(PlayerState p) {
		if(railController.isDockedAndExecuted() && railController.getRoot() instanceof ShopSpaceStation) {
			boolean a = lastDockerPlayerServerLowerCase.length() == 0 || lastDockerPlayerServerLowerCase.equals(p.getName().toLowerCase(Locale.ENGLISH));
			return a;
		}
		if(getFactionId() == 0 || !((FactionState) p.getState()).getFactionManager().existsFaction(getFactionId())) {
			return true;
		}
		if(getFactionId() == p.getFactionId() && isSufficientFactionRights(p)) {
			return true;
		}
		if(getFactionId() == p.getFactionId() && isOwnerSpecific(p)) {
			return true;
		}
		return getFactionId() == 0 || (((FactionState) p.getState()).getFactionManager().existsFaction(getFactionId()) && getFactionId() == p.getFactionId() && isSufficientFactionRights(p));
	}

	@Override
	public void setWrittenForUnload(boolean b) {
		hadAtLeastOneElement = false;
		super.setWrittenForUnload(b);
	}

	@Override
	public void onWrite() {
		hadAtLeastOneElement = false;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.data.ship.Segment#delOn(javax.vecmath.Vector3f, javax.vecmath.Vector3f)
	 */
	/*
	 *
	 *

	 */
	@Override
	public void getNearestIntersectingElementPosition(Vector3f fromRay, Vector3f rayDir, Vector3i wantedSize, float editDistance, final BuildRemoveCallback buildRemoveCallback, SymmetryPlanes symmetryPlanes, short filter, short replaceFilterWith, BuildHelper posesFilter, BuildInstruction buildInstruction, Set<Segment> moddedSegs) {
		if(System.currentTimeMillis() - lastEditBlocks < MIN_TIME_BETWEEN_EDITS) {
			return;
		}
		Vector3i size = new Vector3i();
		final SegmentPiece opiece = getNearestPiece(fromRay, rayDir, editDistance, size, wantedSize);
		if(opiece == null) {
			System.err.println("[SEGCONTROLLER][ELEMENT][REMOVE] NO NEAREST PIECE FOUND");
			return;
		} else {
			System.err.println("[SEGCONTROLLER][ELEMENT][REMOVE] PICKING UP: " + opiece.toString() + "; orientation: " + opiece.getOrientation() + "; " + Element.getSideString(opiece.getOrientation()));
		}
		final boolean singleUpdate = wantedSize.equals(-1, -1, -1);
		Vector3i origPosToBuild = opiece.getAbsolutePos(new Vector3i());
		int minX = Math.min(origPosToBuild.x, origPosToBuild.x + size.x);
		int minY = Math.min(origPosToBuild.y, origPosToBuild.y + size.y);
		int minZ = Math.min(origPosToBuild.z, origPosToBuild.z + size.z);
		int maxX = Math.max(origPosToBuild.x, origPosToBuild.x + size.x);
		int maxY = Math.max(origPosToBuild.y, origPosToBuild.y + size.y);
		int maxZ = Math.max(origPosToBuild.z, origPosToBuild.z + size.z);
		if(maxX == origPosToBuild.x) {
			minX += 1;
			maxX += 1;
		}
		if(maxY == origPosToBuild.y) {
			minY += 1;
			maxY += 1;
		}
		if(maxZ == origPosToBuild.z) {
			minZ += 1;
			maxZ += 1;
		}
		if(symmetryPlanes.getPlaceMode() > 0) {
			symmetryPlanes.setPlaceMode(0);
			return;
		}
		// minX = minX < 0 ? minX+1 : minX;
		// minY = minY < 0 ? minY+1 : minY;
		// minZ = minZ < 0 ? minZ+1 : minZ;
		// maxX = maxX < 0 ? maxX+1 : maxX;
		// maxY = maxY < 0 ? maxY+1 : maxY;
		// maxZ = maxZ < 0 ? maxZ+1 : maxZ;
		// System.err.println("[SEGCONTROLLER][ELEMENT][REMOVE] REMOVING BLOCKS: SIZE: "+size);
		byte selOrientation = (byte) ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBlockOrientation();
		long t = System.currentTimeMillis();
		for(int z = minZ; z < maxZ; z++) {
			for(int y = minY; y < maxY; y++) {
				for(int x = minX; x < maxX; x++) {
					remove(x, y, z, buildRemoveCallback, singleUpdate, moddedSegs, filter, replaceFilterWith, selOrientation, posesFilter, buildInstruction);
					removeInSymmetry(x, y, z, buildRemoveCallback, singleUpdate, moddedSegs, filter, replaceFilterWith, selOrientation, posesFilter, buildInstruction, symmetryPlanes);
				}
			}
		}
		if(!singleUpdate) {
			boolean restructAABB = false;
			System.err.println("[CLIENT][REMOVEBLOCKS] multiRem: UPDATING AABBS: " + wantedSize);
			for(Segment s : moddedSegs) {
				if(!s.isEmpty()) {
					s.getSegmentController().getSegmentProvider().enqueueAABBChange(s);
				} else {
					restructAABB = true;
				}
			}
			if(restructAABB) {
				getSegmentBuffer().restructBB();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.data.ship.Segment#addOn(org.schema.game.common.data.element.Element, javax.vecmath.Vector3f, javax.vecmath.Vector3f)
	 */
	@Override
	public int getNearestIntersection(short type, Vector3f fromRay, Vector3f toRay, BuildCallback callback, int elementOrientation, boolean activateBlock, DimensionFilter filter, Vector3i size, int count, float editDistance, SymmetryPlanes symmetryPlanes, BuildHelper posesFilter, BuildInstruction buildInstruction) throws ElementPositionBlockedException, BlockedByDockedElementException, BlockNotBuildTooFast {
		count = checkAllPlace(type, count, symmetryPlanes);
		if(count < 0) {
			return 0;
		}
		if(symmetryPlanes.getPlaceMode() == 0 && !allowedType(type)) {
			System.err.println("Type is not allowed on " + this + "; " + type);
			return 0;
		}
		if(System.currentTimeMillis() - lastEditBlocks < MIN_TIME_BETWEEN_EDITS) {
			return 0;
		}
		Vector3i wantedSize = new Vector3i();
		SegmentPiece piece = null;
		Vector3i absOnOut = new Vector3i();
		try {
			piece = getNextToNearestPiece(fromRay, toRay, absOnOut, editDistance, size, wantedSize);
			if(symmetryPlanes.getPlaceMode() > 0 && piece != null) {
				Vector3i p = piece.getAbsolutePos(new Vector3i());
				switch(symmetryPlanes.getPlaceMode()) {
					case (SymmetryPlanes.MODE_XY) -> {
						System.err.println("SYM XY PLANE SET");
						symmetryPlanes.getXyPlane().z = p.z;
						symmetryPlanes.setXyPlaneEnabled(true);
					}
					case (SymmetryPlanes.MODE_XZ) -> {
						System.err.println("SYM XZ PLANE SET");
						symmetryPlanes.getXzPlane().y = p.y;
						symmetryPlanes.setXzPlaneEnabled(true);
					}
					case (SymmetryPlanes.MODE_YZ) -> {
						System.err.println("SYM YZ PLANE SET");
						symmetryPlanes.getYzPlane().x = p.x;
						symmetryPlanes.setYzPlaneEnabled(true);
					}
				}
				symmetryPlanes.setPlaceMode(0);
				return 0;
			}
			System.err.println("[CLIENT][EDIT] PLACING AT " + piece + "; size: " + size + " --> " + wantedSize + "; orient " + elementOrientation + "(" + Element.getSideString(elementOrientation) + ") -map-> " + elementOrientation + " PHY: " + (piece != null ? piece.getSegment().getSegmentController().getPhysicsDataContainer().getObject() : ""));
		} catch(CannotImmediateRequestOnClientException e) {
			System.err.println("[CLIENT][WARNING] Cannot ADD! segment not yet in buffer " + e.getSegIndex() + ". -> requested");
			return 0;
		}
		if(piece != null) {
			if(filter != null && !filter.isValid(piece.getAbsolutePos(new Vector3i()))) {
				return 0;
			}
			if(piece.getSegment().isEmpty()) {
				SegmentData newSegmentData = getSegmentProvider().getFreeSegmentData();
				newSegmentData.assignData(piece.getSegment());
			}
			System.err.println("[CLIENT][EDIT] adding new element to " + this.getClass().getSimpleName() + " at " + piece + ", type " + type);
			int[] addedAndRest = new int[2];
			Vector3i origPosToBuild = piece.getAbsolutePos(new Vector3i());
			addedAndRest[1] = count;
			int minX = wantedSize.x < 0 ? origPosToBuild.x + wantedSize.x + 1 : origPosToBuild.x;
			int minY = wantedSize.y < 0 ? origPosToBuild.y + wantedSize.y + 1 : origPosToBuild.y;
			int minZ = wantedSize.z < 0 ? origPosToBuild.z + wantedSize.z + 1 : origPosToBuild.z;
			int maxX = wantedSize.x < 0 ? origPosToBuild.x + 1 : origPosToBuild.x + wantedSize.x;
			int maxY = wantedSize.y < 0 ? origPosToBuild.y + 1 : origPosToBuild.y + wantedSize.y;
			int maxZ = wantedSize.z < 0 ? origPosToBuild.z + 1 : origPosToBuild.z + wantedSize.z;
			try {
				for(int z = minZ; z < maxZ && addedAndRest[1] > 0; z++) {
					for(int y = minY; y < maxY && addedAndRest[1] > 0; y++) {
						for(int x = minX; x < maxX && addedAndRest[1] > 0; x++) {
							dryBuildTest.build(x, y, z, type, elementOrientation, activateBlock, callback, absOnOut, addedAndRest, posesFilter, buildInstruction);
							buildInSymmetry(x, y, z, type, elementOrientation, activateBlock, callback, absOnOut, addedAndRest, buildInstruction, posesFilter, symmetryPlanes, dryBuildTest);
						}
					}
				}
			} catch(PositionBlockedException e) {
				if(!isOnServer()) {
					((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("One or more blocks\ncould not be placed\nbecause they are blocked\nby another structure"), 0);
				}
				return 0;
			}
			for(int z = minZ; z < maxZ && addedAndRest[1] > 0; z++) {
				for(int y = minY; y < maxY && addedAndRest[1] > 0; y++) {
					for(int x = minX; x < maxX && addedAndRest[1] > 0; x++) {
						build(x, y, z, type, elementOrientation, activateBlock, callback, absOnOut, addedAndRest, posesFilter, buildInstruction);
						buildInSymmetry(x, y, z, type, elementOrientation, activateBlock, callback, absOnOut, addedAndRest, buildInstruction, posesFilter, symmetryPlanes, this);
					}
				}
			}
			return addedAndRest[0];
		} else {
			System.err.println("no intersection found in world currentSegmentContext");
		}
		return 0;
	}

	public class DryTestBuild implements BuilderInterface {

		public BoundingBox boundingBox = new BoundingBox();

		@Override
		public void build(int x, int y, int z, short type, int elementOrientation, boolean activateBlock, BuildSelectionCallback callback, Vector3i absOnOut, int[] addedAndRest, BuildHelper posesFilter, BuildInstruction buildInstruction) {
			boundingBox.min.x = Math.min(boundingBox.min.x, x);
			boundingBox.min.y = Math.min(boundingBox.min.y, y);
			boundingBox.min.z = Math.min(boundingBox.min.z, z);
			boundingBox.max.x = Math.max(boundingBox.max.x, x + 1);
			boundingBox.max.y = Math.max(boundingBox.max.y, y + 1);
			boundingBox.max.z = Math.max(boundingBox.max.z, z + 1);
			SegmentPiece toBuildPiece = getSegmentBuffer().getPointUnsave(new Vector3i(x, y, z));
			if(toBuildPiece != null) {
				SegmentCollisionCheckerCallback cb = new SegmentCollisionCheckerCallback();
				if(getCollisionChecker().checkPieceCollision(toBuildPiece, cb, false)) {
					System.err.println(getState() + "; " + this + " Block at " + toBuildPiece + " blocked");
					throw new PositionBlockedException();
				}
			}
		}
	}

	@Override
	public void startCreatorThread() {
		if(getCreatorThread() == null) {
			setCreatorThread(new EmptyCreatorThread(this));
		}
	}

	@Override
	public boolean isEmptyOnServer() {
		return hadAtLeastOneElement && getTotalElements() == 0;
	}

	public boolean allowedType(short type) {
		if(!ElementKeyMap.getInfo(type).isPlacable()) {
			if(!isOnServer()) {
				if(ElementKeyMap.CORE_ID == type) {
					((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("ERROR\nShip Cores cannot be placed,\nthey are used to spawn new ships."), 0);
				} else {
					((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("ERROR\nThis Item cannot be placed.\n"), 0);
				}
			}
			return false;
		}
		return true;
	}

	@Override
	public void build(final int x, final int y, final int z, short type, int elementOrientation, boolean activateBlock, BuildSelectionCallback callback, Vector3i absOnOut, int[] addedAndRest, BuildHelper posesFilter, BuildInstruction buildInstruction) {
		if(posesFilter != null && !posesFilter.contains(x, y, z)) {
			// System.err.println("[BUILD] cannot build as helper is restricted: " + posesFilter);
			return;
		}
		if(ElementKeyMap.getInfo(type).resourceInjection != ResourceInjectionType.OFF) {
			// never place resources into blocks
			elementOrientation = 0;
		}
		if(addedAndRest[1] > 0) {
			SegmentPiece toBuildPiece = getSegmentBuffer().getPointUnsave(x, y, z);
			if(toBuildPiece != null) {
				SegmentCollisionCheckerCallback cb = new SegmentCollisionCheckerCallback();
				if(getCollisionChecker().checkPieceCollision(toBuildPiece, cb, true)) {
					System.err.println(getState() + "; " + this + " Block at " + toBuildPiece + " blocked");
					if(!isOnServer()) {
						((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("One or more blocks\ncould not be placed\nbecause they are blocked\nby another structure"), 0);
					}
					return;
				}
				short hitpoints = 0;
				if(type > 0) {
					if(BuildToolsPanel.blueprintPlacementSetting > 0) {
						hitpoints = BuildToolsPanel.blueprintPlacementSetting;
					} else {
						hitpoints = ElementKeyMap.MAX_HITPOINTS;
					}
				}
				short typeToAdd = type;
				if(ElementKeyMap.isValidType(type) && ElementKeyMap.getInfoFast(type).isReactorChamberGeneral()) {
					Vector3i p = new Vector3i();
					SegmentPiece sp = new SegmentPiece();
					for(int i = 0; i < 6; i++) {
						p.set(x, y, z);
						p.add(Element.DIRECTIONSi[i]);
						SegmentPiece pointUnsave = getSegmentBuffer().getPointUnsave(p, sp);
						if(pointUnsave != null && ElementKeyMap.isValidType(pointUnsave.getType()) && ElementKeyMap.getInfo(pointUnsave.getType()).chamberRoot == type) {
							// System.err.println("REPLACING WITH SPECIFIED CHAMBER");
							typeToAdd = pointUnsave.getType();
							break;
						}
					}
				}
				boolean addElement = toBuildPiece.getSegment().addElement(typeToAdd, toBuildPiece.getPos(tmpLocalPos), elementOrientation, activateBlock, hitpoints, this);
				if(addElement) {
					if(buildInstruction != null) {
						buildInstruction.recordAdd(type, ElementCollection.getIndex(x, y, z), elementOrientation, activateBlock, callback != null ? callback.getSelectedControllerPos() : null);
					}
					this.lastEditBlocks = System.currentTimeMillis();
					((RemoteSegment) toBuildPiece.getSegment()).setLastChanged(System.currentTimeMillis());
					toBuildPiece.refresh();
					Vector3i absOut = toBuildPiece.getAbsolutePos(new Vector3i());
					final long absIndex = toBuildPiece.getAbsoluteIndex();
					if(callback != null && callback instanceof BuildCallback) {
						((BuildCallback) callback).onBuild(absOut, absOnOut, type);
					}
					// System.err.println("ADDINING SEGMENT MODIFICATION "+p.getSegment().pos+" -> "+p.getPos(new Vector3b()));
					RemoteSegmentPiece remoteSegmentPiece = new RemoteSegmentPiece(toBuildPiece, getNetworkObject());
					if(callback != null && callback.getSelectedControllerPos() != Long.MIN_VALUE) {
						if(callback.getSelectedControllerPos() == absIndex) {
							// the controller wants to connect to self
							// happens when bulk mode placing controllers
							// System.err.println("[CLIENT] WARNING1: not sending controller equals block to build: "+absOut);
							remoteSegmentPiece.controllerPos = Long.MIN_VALUE;
						} else {
							SegmentPiece pointUnsave = getSegmentBuffer().getPointUnsave(callback.getSelectedControllerPos());
							if(pointUnsave != null) {
								if(this instanceof ManagedSegmentController<?> && !((ManagedSegmentController<?>) this).getManagerContainer().canBeControlled(pointUnsave.getType(), type)) {
									// System.err.println("ManCannotBeControlled "+pointUnsave);
									remoteSegmentPiece.controllerPos = Long.MIN_VALUE;
								} else {
									if(ElementKeyMap.isValidType(pointUnsave.getType()) && (ElementKeyMap.getInfo(pointUnsave.getType()).controlsAll() || ElementKeyMap.getInfo(type).getControlledBy().contains(pointUnsave.getType()) || ElementInformation.canBeControlled(pointUnsave.getType(), type))) {
										// System.err.println("[CLIENT] WARNING1: sending block controller "+callback.getSelectedControllerPos());
										remoteSegmentPiece.controllerPos = callback.getSelectedControllerPos();
									} else {
										// System.err.println("[CLIENT] WARNING1: not sending controller: controller type cannot control this: "+absOut+" tryed to connect to "+pointUnsave);
										remoteSegmentPiece.controllerPos = Long.MIN_VALUE;
									}
								}
							} else {
								System.err.println("[CLIENT] ERROR: piece not loaded: " + callback.getSelectedControllerPos());
							}
						}
					} else {
						remoteSegmentPiece.controllerPos = Long.MIN_VALUE;
					}
					sendBlockMod(remoteSegmentPiece);
					addedAndRest[0]++;
					addedAndRest[1]--;
				} else {
					// System.err.println("Block at "+toBuildPiece+" already exists");
				}
				if(!toBuildPiece.getSegment().isEmpty() && getSegmentBuffer().getSegmentState(toBuildPiece.getAbsolutePos(new Vector3i())) < 0) {
					getSegmentBuffer().addImmediate(toBuildPiece.getSegment());
				}
			} else {
				this.lastEditBlocks = System.currentTimeMillis();
				toBuildPiece = new SegmentPiece();
				byte activeByte = !activateBlock ? (byte) 0 : ElementInformation.defaultActive(type);
				toBuildPiece.setActive(activeByte != 0);
				toBuildPiece.setType(type);
				toBuildPiece.setOrientation((byte) elementOrientation);
				toBuildPiece.setHitpointsByte(ElementKeyMap.MAX_HITPOINTS);
				if(buildInstruction != null) {
					buildInstruction.recordAdd(type, ElementCollection.getIndex(x, y, z), elementOrientation, activateBlock, callback.getSelectedControllerPos());
				}
				RemoteSegmentPiece remoteSegmentPiece = new RemoteSegmentPiece(toBuildPiece, getNetworkObject()) {

					@Override
					public int toByteStream(DataOutputStream buffer) throws IOException {
						assert (get() != null);
						writeDynamicPosition(x, y, z, true, buffer);
						SegmentPiece.serializeData(buffer, get().getData());
						return 1;
					}
				};
				if(callback.getSelectedControllerPos() != Long.MIN_VALUE) {
					if(callback.getSelectedControllerPos() == ElementCollection.getIndex(x, y, z)) {
						System.err.println("[CLIENT] WARNING2: not sending controller equals block to build: " + x + "," + y + "," + z);
						remoteSegmentPiece.controllerPos = Long.MIN_VALUE;
					} else {
						SegmentPiece pointUnsave = getSegmentBuffer().getPointUnsave(callback.getSelectedControllerPos());
						if(pointUnsave != null) {
							if(this instanceof ManagedSegmentController<?> && !((ManagedSegmentController<?>) this).getManagerContainer().canBeControlled(pointUnsave.getType(), type)) {
								remoteSegmentPiece.controllerPos = Long.MIN_VALUE;
							} else {
								if(pointUnsave.getType() > 0 && (ElementKeyMap.getInfo(type).getControlledBy().contains(pointUnsave.getType()))) {
									remoteSegmentPiece.controllerPos = callback.getSelectedControllerPos();
								} else {
									System.err.println("[CLIENT] WARNING1: not sending controller: controller type cannot control this: " + x + ", " + y + ", " + z + " tryed to connect to " + pointUnsave);
									remoteSegmentPiece.controllerPos = Long.MIN_VALUE;
								}
							}
						} else {
							System.err.println("[CLIENT] not loaded piece: " + pointUnsave);
						}
					}
				} else {
					remoteSegmentPiece.controllerPos = Long.MIN_VALUE;
				}
				// System.err.println("ADDINING SEGMENT MODIFICATION "+p.getSegment().pos+" -> "+p.getPos(new Vector3b()));
				sendBlockMod(remoteSegmentPiece);
				addedAndRest[0]++;
				addedAndRest[1]--;
			}
		}
	}

	public boolean canAttack(Damager from) {
		// System.err.println("CAN ATTACK "+this+" from "+from+"; "+isHomeBase()+", "+isHomeBaseFor(getFactionId()));
		if(isHomeBase() || isHomeBaseFor(getFactionId())) {
			if(from != null && from instanceof PlayerControllable) {
				List<PlayerState> attachedPlayers = ((PlayerControllable) from).getAttachedPlayers();
				for(int i = 0; i < attachedPlayers.size(); i++) {
					PlayerState ps = attachedPlayers.get(i);
					if(System.currentTimeMillis() - ps.lastSectorProtectedMsgSent > 5000) {
						ps.lastSectorProtectedMsgSent = System.currentTimeMillis();
						ps.sendServerMessage(new ServerMessage(Lng.astr("Cannot attack a faction's\nhome base!"), ServerMessage.MESSAGE_TYPE_WARNING, ps.getId()));
					}
				}
			}
			return false;
		}
		return true;
	}

	private void checkCharacterExit() {
		System.err.println("[SegController] CHECKING CHARACTER EXIT");
		if(this instanceof PlayerControllable) {
			for(PlayerState s : ((PlayerControllable) this).getAttachedPlayers()) {
				s.getControllerState().checkPlayerControllers();
			}
		}
	}

	public boolean checkCore(SegmentPiece pointUnsave) {
		return true;
	}

	public float damageElement(short type, int infoIndex, SegmentData segmentData, int damage, Damager from, DamageDealerType damageType, long weaponId) {
		if(ElementKeyMap.exists(type)) {
			ElementInformation elementInformation = ElementKeyMap.getInfoFast(type);
			short hpBeforeByte = segmentData.getHitpointsByte(infoIndex);
			int hitpointsFullBefore = ElementKeyMap.convertToFullHP(type, hpBeforeByte);
			int hitpointsFullAfter = Math.max(0, (hitpointsFullBefore - damage));
			float actualDamage = hitpointsFullBefore - hitpointsFullAfter;
			if(!isOnServer()) {
				// dont set damage to blocks on client
				return actualDamage;
			}
			// System.out.println("Damaging Element (dam: " + actualDamage + "): BefHP " + hitpointsBefore + " -> EHP " + hitpointsEffective + " -> Remaining EHP " +  hitpointsEffectiveAfter + " -> Remaining HP " + hitpointsAfter);
			int byteHpAfter = ElementKeyMap.convertToByteHP(type, hitpointsFullAfter);
			assert (byteHpAfter <= 127) : "FULL: " + byteHpAfter + "; " + ElementKeyMap.getInfo(type).getMaxHitPointsFull();
			// only
			if(byteHpAfter != hpBeforeByte) {
				// only process damage if the hitpoints went down
				try {
					segmentData.setHitpointsByte(infoIndex, byteHpAfter);
				} catch(SegmentDataWriteException e) {
					assert (segmentData == segmentData.getSegment().getSegmentData()) : segmentData + "; " + segmentData.getSegment().getSegmentData();
					segmentData = SegmentDataWriteException.replaceData(segmentData.getSegment());
					try {
						segmentData.setHitpointsByte(infoIndex, ElementKeyMap.convertToByteHP(type, hitpointsFullAfter));
					} catch(SegmentDataWriteException e1) {
						e1.printStackTrace();
						throw new RuntimeException(e1);
					}
				}
				// System.err.println("DAMAGE "+x+", "+y+", "+z+": "+hitpointsBefore+" -> "+hitpointsAfter);
				if(hitpointsFullAfter <= 0) {
					getHpController().onElementDestroyed(from, elementInformation, damageType, weaponId);
					if(isEnterable(type) && segmentData.getSegment() != null) {
						forceCharacterExit(new SegmentPiece(segmentData.getSegment(), infoIndex));
					}
					if(type == getCoreType() && segmentData.getSegment().getAbsoluteIndex(infoIndex) == ElementCollection.getIndex(Ship.core)) {
						try {
							segmentData.setHitpointsByte(infoIndex, (short) 0);
						} catch(SegmentDataWriteException e) {
							segmentData = SegmentDataWriteException.replaceData(segmentData.getSegment());
							try {
								segmentData.setHitpointsByte(infoIndex, (short) 0);
							} catch(SegmentDataWriteException e1) {
								e1.printStackTrace();
								throw new RuntimeException(e1);
							}
						}
						onCoreDestroyed(from);
						onCoreHitAlreadyDestroyed(damage);
					} else {
						segmentData.getSegment().removeElement(infoIndex, false);
						getSegmentProvider().enqueueAABBChange(segmentData.getSegment());
					}
					if(ServerConfig.ENABLE_BREAK_OFF.isOn()) {
						segmentData.getSegment().getAbsoluteElemPos(infoIndex, absPosCache);
						checkBreak(absPosCache);
					}
				}
			}
			return actualDamage;
		}
		return 0;
	}

	public void doDimExtensionIfNecessary(Segment segment, byte x, byte y, byte z) {
		// segment.getAbsoluteElemPos(x,y,z, absPosCache);
		if(x == 0) {
			// tmpPos.set(segment.absPos.x-1, segment.absPos.y, segment.absPos.z );
			// assert(Segment.getSegmentIndexFromSegmentElement(absPosCache.x-1, absPosCache.y, absPosCache.z, new Vector3i()).equals(tmpPos));
			extendDim(0, segment.absPos.x - 1, -1, 0, 0);
		}
		if(y == 0) {
			// tmpPos.set(segment.absPos.x, segment.absPos.y-1, segment.absPos.z );
			// assert(Segment.getSegmentIndexFromSegmentElement(absPosCache.x, absPosCache.y-1, absPosCache.z, new Vector3i()).equals(tmpPos));
			extendDim(1, segment.absPos.y - 1, 0, -1, 0);
		}
		if(z == 0) {
			// tmpPos.set(segment.absPos.x, segment.absPos.y, segment.absPos.z-1 );
			// assert(Segment.getSegmentIndexFromSegmentElement(absPosCache.x, absPosCache.y, absPosCache.z-1, new Vector3i()).equals(tmpPos));
			extendDim(2, segment.absPos.z - 1, 0, 0, -1);
		}
		if(x == SegmentData.SEG - 1) {
			// tmpPos.set(segment.absPos.x+1, segment.absPos.y, segment.absPos.z );
			// assert(Segment.getSegmentIndexFromSegmentElement(absPosCache.x+1, absPosCache.y, absPosCache.z, new Vector3i()).equals(tmpPos));
			extendDim(0, segment.absPos.x + 1, 1, 0, 0);
		}
		if(y == SegmentData.SEG - 1) {
			// tmpPos.set(segment.absPos.x, segment.absPos.y+1, segment.absPos.z );
			// assert(Segment.getSegmentIndexFromSegmentElement(absPosCache.x, absPosCache.y+1, absPosCache.z, new Vector3i()).equals(tmpPos));
			extendDim(1, segment.absPos.y + 1, 0, 1, 0);
		}
		if(z == SegmentData.SEG - 1) {
			// tmpPos.set(segment.absPos.x, segment.absPos.y, segment.absPos.z+1 );
			// assert(Segment.getSegmentIndexFromSegmentElement(absPosCache.x, absPosCache.y, absPosCache.z+1, new Vector3i()).equals(tmpPos));
			extendDim(2, segment.absPos.z + 1, 0, 0, 1);
		}
	}

	public void extendDim(int coord, int from, int x, int y, int z) {
		/*
		 * check axis individually if they need to be expanded, not just all
		 * with isInbound because if eg x == 0 and y == 15, it will endless
		 * expand to x -> -1 since the point is never inbound
		 */
		if(!isInboundCoord(coord, from)) {
			getMaxPos().x += x > 0 ? x : 0;
			getMaxPos().y += y > 0 ? y : 0;
			getMaxPos().z += z > 0 ? z : 0;
			getMinPos().x += x < 0 ? x : 0;
			getMinPos().y += y < 0 ? y : 0;
			getMinPos().z += z < 0 ? z : 0;
			setChangedForDb(true);
		}
	}

	public void forceAllCharacterExit() {
		if(this instanceof PlayerControllable) {
			for(PlayerState s : ((PlayerControllable) this).getAttachedPlayers()) {
				PlayerState p = s;
				p.getControllerState().forcePlayerOutOfSegmentControllers();
			}
		}
	}

	public void forceCharacterExit(SegmentPiece segmentPiece) {
		if(segmentPiece.getType() != ElementKeyMap.CORE_ID) {
			synchronized(getState().getLocalAndRemoteObjectContainer().getLocalObjects()) {
				for(Sendable s : getState().getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
					if(s instanceof PlayerState) {
						PlayerState p = (PlayerState) s;
						p.onDestroyedElement(segmentPiece);
					}
				}
			}
		}
	}

	@Override
	public ProjectileController getParticleController() {
		if(!isOnServer()) {
			return ((GameClientState) getState()).getParticleController();
		} else {
			return ((GameServerState) getState()).getUniverse().getSector(getSectorId()).getParticleController();
		}
	}

	protected short getCoreType() {
		return ElementKeyMap.CORE_ID;
	}

	/**
	 * @return the flagCoreDestroyedByExplosion
	 */
	public Object getFlagCoreDestroyedByExplosion() {
		return flagCoreDestroyedByExplosion;
	}

	/**
	 * @param flagCoreDestroyedByExplosion the flagCoreDestroyedByExplosion to set
	 */
	public void setFlagCoreDestroyedByExplosion(Object flagCoreDestroyedByExplosion) {
		this.flagCoreDestroyedByExplosion = flagCoreDestroyedByExplosion;
	}

	public int checkPlace(short type, short typeToPlace, int count, SymmetryPlanes symmetryPlanes) {
		if((symmetryPlanes == null || symmetryPlanes.getPlaceMode() == 0) && type == typeToPlace) {
			if(getElementClassCountMap().get(typeToPlace) > 0) {
				if(!isOnServer()) {
					((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("ERROR\nOnly one %s\nis permitted per structure!", ElementKeyMap.toString(typeToPlace)), 0);
				}
				return -1;
			} else {
				return 1;
			}
		}
		return count;
	}

	public int checkAllPlace(short type, int count, SymmetryPlanes symmetryPlanes) {
		count = checkPlace(type, ElementKeyMap.FACTION_BLOCK, count, symmetryPlanes);
		if(count < 0) {
			return -1;
		}
		count = checkPlace(type, ElementKeyMap.AI_ELEMENT, count, symmetryPlanes);
		if(count < 0) {
			return -1;
		}
		count = checkPlace(type, ElementKeyMap.SHOP_BLOCK_ID, count, symmetryPlanes);
		if(count < 0) {
			return -1;
		}
		count = checkPlace(type, ElementKeyMap.FACTORY_CORE_EXTRACTOR, count, symmetryPlanes);
		if(count < 0) {
			return -1;
		}
		count = checkPlace(type, ElementKeyMap.FACTORY_GAS_EXTRACTOR, count, symmetryPlanes);
		if(count < 0) {
			return -1;
		}

		for(Short id : BlockConfig.restrictedBlocks) {
			count = checkPlace(type, id, count, symmetryPlanes);
			if(count < 0) {
				return -1;
			}
		}

		count = checkPlace(type, ElementKeyMap.SCANNER_COMPUTER, count, symmetryPlanes);
		if(count < 0) {
			return -1;
		}
		return count;
	}

	public void buildInSymmetry(int x, int y, int z, short type, int elementOrientation, boolean activateBlock, BuildCallback callback, Vector3i absOnOut, int[] addedAndRest, BuildInstruction buildInstruction, BuildHelper posesFilter, SymmetryPlanes symmetryPlanes, BuilderInterface bInt) {
		if(symmetryPlanes.isXyPlaneEnabled() && !symmetryPlanes.isXzPlaneEnabled() && !symmetryPlanes.isYzPlaneEnabled()) {
			// XY
			int planePosXY = symmetryPlanes.getXyPlane().z;
			int distXY = (planePosXY - z) * 2 + symmetryPlanes.getXyExtraDist();
			bInt.build(x, y, z + distXY, type, symmetryPlanes.getMirrorOrientation(type, elementOrientation, true, false, false), activateBlock, callback, absOnOut, addedAndRest, posesFilter, buildInstruction);
		} else if(!symmetryPlanes.isXyPlaneEnabled() && symmetryPlanes.isXzPlaneEnabled() && !symmetryPlanes.isYzPlaneEnabled()) {
			// XZ
			int planePosXZ = symmetryPlanes.getXzPlane().y;
			int distXZ = (planePosXZ - y) * 2 + symmetryPlanes.getXzExtraDist();
			bInt.build(x, y + distXZ, z, type, symmetryPlanes.getMirrorOrientation(type, elementOrientation, false, true, false), activateBlock, callback, absOnOut, addedAndRest, posesFilter, buildInstruction);
		} else if(!symmetryPlanes.isXyPlaneEnabled() && !symmetryPlanes.isXzPlaneEnabled() && symmetryPlanes.isYzPlaneEnabled()) {
			// YZ
			int planePosYZ = symmetryPlanes.getYzPlane().x;
			int distYZ = (planePosYZ - x) * 2 + symmetryPlanes.getYzExtraDist();
			bInt.build(x + distYZ, y, z, type, symmetryPlanes.getMirrorOrientation(type, elementOrientation, false, false, true), activateBlock, callback, absOnOut, addedAndRest, posesFilter, buildInstruction);
		} else if(symmetryPlanes.isXyPlaneEnabled() && symmetryPlanes.isXzPlaneEnabled() && !symmetryPlanes.isYzPlaneEnabled()) {
			// XY XZ
			int planePosXY = symmetryPlanes.getXyPlane().z;
			int planePosXZ = symmetryPlanes.getXzPlane().y;
			int distXY = (planePosXY - z) * 2 + symmetryPlanes.getXyExtraDist();
			int distXZ = (planePosXZ - y) * 2 + symmetryPlanes.getXzExtraDist();
			bInt.build(x, y, z + distXY, type, symmetryPlanes.getMirrorOrientation(type, elementOrientation, true, false, false), activateBlock, callback, absOnOut, addedAndRest, posesFilter, buildInstruction);
			bInt.build(x, y + distXZ, z, type, symmetryPlanes.getMirrorOrientation(type, elementOrientation, false, true, false), activateBlock, callback, absOnOut, addedAndRest, posesFilter, buildInstruction);
			bInt.build(x, y + distXZ, z + distXY, type, symmetryPlanes.getMirrorOrientation(type, elementOrientation, true, true, false), activateBlock, callback, absOnOut, addedAndRest, posesFilter, buildInstruction);
		} else if(symmetryPlanes.isXyPlaneEnabled() && !symmetryPlanes.isXzPlaneEnabled() && symmetryPlanes.isYzPlaneEnabled()) {
			// XY YZ
			int planePosXY = symmetryPlanes.getXyPlane().z;
			int planePosYZ = symmetryPlanes.getYzPlane().x;
			int distXY = (planePosXY - z) * 2 + symmetryPlanes.getXyExtraDist();
			int distYZ = (planePosYZ - x) * 2 + symmetryPlanes.getYzExtraDist();
			bInt.build(x, y, z + distXY, type, symmetryPlanes.getMirrorOrientation(type, elementOrientation, true, false, false), activateBlock, callback, absOnOut, addedAndRest, posesFilter, buildInstruction);
			bInt.build(x + distYZ, y, z, type, symmetryPlanes.getMirrorOrientation(type, elementOrientation, false, false, true), activateBlock, callback, absOnOut, addedAndRest, posesFilter, buildInstruction);
			bInt.build(x + distYZ, y, z + distXY, type, symmetryPlanes.getMirrorOrientation(type, elementOrientation, true, false, true), activateBlock, callback, absOnOut, addedAndRest, posesFilter, buildInstruction);
		} else if(!symmetryPlanes.isXyPlaneEnabled() && symmetryPlanes.isXzPlaneEnabled() && symmetryPlanes.isYzPlaneEnabled()) {
			// XZ YZ
			int planePosXZ = symmetryPlanes.getXzPlane().y;
			int planePosYZ = symmetryPlanes.getYzPlane().x;
			int distXZ = (planePosXZ - y) * 2 + symmetryPlanes.getXzExtraDist();
			int distYZ = (planePosYZ - x) * 2 + symmetryPlanes.getYzExtraDist();
			bInt.build(x, y + distXZ, z, type, symmetryPlanes.getMirrorOrientation(type, elementOrientation, false, true, false), activateBlock, callback, absOnOut, addedAndRest, posesFilter, buildInstruction);
			bInt.build(x + distYZ, y, z, type, symmetryPlanes.getMirrorOrientation(type, elementOrientation, false, false, true), activateBlock, callback, absOnOut, addedAndRest, posesFilter, buildInstruction);
			bInt.build(x + distYZ, y + distXZ, z, type, symmetryPlanes.getMirrorOrientation(type, elementOrientation, false, true, true), activateBlock, callback, absOnOut, addedAndRest, posesFilter, buildInstruction);
		} else if(symmetryPlanes.isXyPlaneEnabled() && symmetryPlanes.isXzPlaneEnabled() && symmetryPlanes.isYzPlaneEnabled()) {
			// ALL
			int planePosXY = symmetryPlanes.getXyPlane().z;
			int planePosXZ = symmetryPlanes.getXzPlane().y;
			int planePosYZ = symmetryPlanes.getYzPlane().x;
			int distXY = (planePosXY - z) * 2 + symmetryPlanes.getXyExtraDist();
			int distXZ = (planePosXZ - y) * 2 + symmetryPlanes.getXzExtraDist();
			int distYZ = (planePosYZ - x) * 2 + symmetryPlanes.getYzExtraDist();
			bInt.build(x + distYZ, y, z, type, symmetryPlanes.getMirrorOrientation(type, elementOrientation, false, false, true), activateBlock, callback, absOnOut, addedAndRest, posesFilter, buildInstruction);
			bInt.build(x, y + distXZ, z, type, symmetryPlanes.getMirrorOrientation(type, elementOrientation, false, true, false), activateBlock, callback, absOnOut, addedAndRest, posesFilter, buildInstruction);
			bInt.build(x, y, z + distXY, type, symmetryPlanes.getMirrorOrientation(type, elementOrientation, true, false, false), activateBlock, callback, absOnOut, addedAndRest, posesFilter, buildInstruction);
			bInt.build(x + distYZ, y + distXZ, z, type, symmetryPlanes.getMirrorOrientation(type, elementOrientation, false, true, true), activateBlock, callback, absOnOut, addedAndRest, posesFilter, buildInstruction);
			bInt.build(x + distYZ, y, z + distXY, type, symmetryPlanes.getMirrorOrientation(type, elementOrientation, true, false, true), activateBlock, callback, absOnOut, addedAndRest, posesFilter, buildInstruction);
			bInt.build(x, y + distXZ, z + distXY, type, symmetryPlanes.getMirrorOrientation(type, elementOrientation, true, true, false), activateBlock, callback, absOnOut, addedAndRest, posesFilter, buildInstruction);
			bInt.build(x + distYZ, y + distXZ, z + distXY, type, symmetryPlanes.getMirrorOrientation(type, elementOrientation, true, true, true), activateBlock, callback, absOnOut, addedAndRest, posesFilter, buildInstruction);
		}
	}

	public Segment getNearestLoadedSegment(Transform transform) {
		Vector3f thisPos = getWorldTransform().origin;
		Vector3f otherPos = transform.origin;
		CubeRayCastResult rayCallback = new CubeRayCastResult(thisPos, otherPos, false);
		rayCallback.setIgnoereNotPhysical(true);
		rayCallback.setOnlyCubeMeshes(true);
		CollisionWorld.ClosestRayResultCallback result = getPhysics().testRayCollisionPoint(thisPos, otherPos, rayCallback, false);
		if(result.hasHit() && result.collisionObject != null) {
			if(result instanceof CubeRayCastResult && ((CubeRayCastResult) result).getSegment() != null) {
				Segment segment = ((CubeRayCastResult) result).getSegment();
				if(!segment.isEmpty()) return segment;
			}
		}
		return null;
	}

	public SegmentPiece getNearestPiece(Vector3f from, Vector3f dir, float scale, Vector3i buildSize, Vector3i wantedBuildSize) {
		Vector3f to = new Vector3f();
		dir.scale(scale);
		to.add(from, dir);
		// ClosestRayResultCallback result = getPhysics().testRayCollisionPoint(from,
		// to,  false, null, this, false, cacheLastHitSegmentFromAddAndRemove, false);
		CubeRayCastResult rayCallback = new CubeRayCastResult(from, to, false, this);
		// rayCallback.setLastHitSegment(cacheLastHitSegmentFromAddAndRemove);
		rayCallback.setOnlyCubeMeshes(true);
		rayCallback.setIgnoereNotPhysical(true);
		System.err.println("NEAREST: " + from + " -> " + to + "; DIR: " + dir + "; scale: " + scale);
		CollisionWorld.ClosestRayResultCallback result = getPhysics().testRayCollisionPoint(from, to, rayCallback, false);
		if(result.hasHit() && result.collisionObject != null) {
			if(result instanceof CubeRayCastResult && ((CubeRayCastResult) result).getSegment() != null) {
				CubeRayCastResult cubeResult = (CubeRayCastResult) result;
				SegmentController segmentController = cubeResult.getSegment().getSegmentData().getSegmentController();
				Segment seg = cubeResult.getSegment();
				// cacheLastHitSegmentFromAddAndRemove = cubeResult.getSegment();
				Vector3i p = new Vector3i(cubeResult.getSegment().pos.x, cubeResult.getSegment().pos.y, cubeResult.getSegment().pos.z);
				// absOnOut.set(cubeResult.getSegment().pos.x+cubeResult.cubePos.x
				// ,cubeResult.getSegment().pos.y+cubeResult.cubePos.y
				// ,cubeResult.getSegment().pos.z+cubeResult.cubePos.z);
				p.x += (cubeResult.getCubePos().x - SegmentData.SEG_HALF);
				p.y += (cubeResult.getCubePos().y - SegmentData.SEG_HALF);
				p.z += (cubeResult.getCubePos().z - SegmentData.SEG_HALF);
				// Transform t = new Transform();
				// t.setIdentity();
				// DebugBox bg1 = new DebugBox(
				// new Vector3f(testRayCollisionPoint.hitPointWorld.x-0.7f, testRayCollisionPoint.hitPointWorld.y-0.7f, testRayCollisionPoint.hitPointWorld.z-0.7f),
				// new Vector3f(testRayCollisionPoint.hitPointWorld.x+0.7f, testRayCollisionPoint.hitPointWorld.y+0.7f, testRayCollisionPoint.hitPointWorld.z+0.7f),
				// t,
				// 1, 0, 1, 1);
				// DebugDrawer.boxes.add(bg1);
				getWorldTransformInverse().transform(cubeResult.hitPointWorld);
				// System.err.println("near: "+p+", on "+segmentController);
				IntSet disabledSides = new IntOpenHashSet();
				SegmentPiece pieceAt = cubeResult.getSegment().getSegmentController().getSegmentBuffer().getPointUnsave(new Vector3i(p.x + SegmentData.SEG_HALF, p.y + SegmentData.SEG_HALF, p.z + SegmentData.SEG_HALF));
				int side = Element.getSide(cubeResult.hitPointWorld, pieceAt == null ? null : pieceAt.getAlgorithm(), p, pieceAt != null ? pieceAt.getType() : (short) 0, pieceAt != null ? pieceAt.getOrientation() : 0, disabledSides);
				System.err.println("[GETNEAREST] SIDE: " + Element.getSideString(side) + "(" + side + "): " + cubeResult.hitPointWorld + "; " + p);
				wantedBuildSize.x = -wantedBuildSize.x;
				wantedBuildSize.y = -wantedBuildSize.y;
				wantedBuildSize.z = -wantedBuildSize.z;
				switch(side) {
					case // p.x += 1f;
							(Element.RIGHT) -> buildSize.set(wantedBuildSize.x, wantedBuildSize.y, wantedBuildSize.z);
					case // p.x -= 1f;
							(Element.LEFT) -> buildSize.set(-wantedBuildSize.x, wantedBuildSize.y, wantedBuildSize.z);
					case // p.y += 1f;
							(Element.TOP) -> buildSize.set(wantedBuildSize.x, wantedBuildSize.y, wantedBuildSize.z);
					case // p.y -= 1f;
							(Element.BOTTOM) -> buildSize.set(wantedBuildSize.x, -wantedBuildSize.y, wantedBuildSize.z);
					case // p.z += 1f;
							(Element.FRONT) -> buildSize.set(wantedBuildSize.x, wantedBuildSize.y, wantedBuildSize.z);
					case // p.z -= 1f;
							(Element.BACK) -> buildSize.set(wantedBuildSize.x, wantedBuildSize.y, -wantedBuildSize.z);
					default -> System.err.println("[BUILDMODEDRAWER] WARNING: NO SIDE recognized!!!");
				}
				return new SegmentPiece(seg, cubeResult.getCubePos());
			}
		} else {
		}
		// cacheLastHitSegmentFromAddAndRemove = null;
		return null;
	}

	@Override
	public NetworkSegmentController getNetworkObject() {
		return super.getNetworkObject();
	}

	@Override
	public void updateLocal(Timer timer) {
		getState().getDebugTimer().start(this, "EditableSegmentController");
		if(getTotalElements() > 0) {
			hadAtLeastOneElement = true;
		}
		// if(this instanceof PlayerControllable && !((PlayerControllable)this).getAttachedPlayers().isEmpty()){
		// System.err.println(getState()+" DIMENSION: "+getMinPos()+" "+getMaxPos());
		// }
		if(this.isMarkedForDeleteVolatile()) {
			System.err.println("[EditableSegmentControleler] " + this + " MARKED TO DELETE ON " + getState());
		}
		if(isOnServer()) {
			acidDamageManagerServer.update(timer);
		}
		if(lastSalvaged != null) {
			if(isOnServer()) {
				try {
					StellarSystem sys = ((GameServerState) getState()).getUniverse().getStellarSystemFromSecPos(getSector(new Vector3i()));
					if(FactionManager.isNPCFaction(sys.getOwnerFaction())) {
						long dbId;
						if(lastSalvaged.getOwnerState() != null && lastSalvaged.getOwnerState() instanceof PlayerState) {
							dbId = ((PlayerState) lastSalvaged.getOwnerState()).getDbId();
						} else {
							dbId = lastSalvaged.getFactionId();
						}
						if(dbId != 0 && sys.getOwnerFaction() != dbId) {
							((FactionState) getState()).getFactionManager().diplomacyAction(DiplActionType.MINING, sys.getOwnerFaction(), dbId);
						}
					}
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
			lastSalvaged = null;
		}
		if(flagCoreDestroyedByExplosion != null) {
			System.err.println("[EditSegController] " + this + " CORE HAS BEEN DESTROYED BY " + flagCoreDestroyedByExplosion);
			if(flagCoreDestroyedByExplosion instanceof Sendable) {
				onCoreDestroyed((Damager) flagCoreDestroyedByExplosion);
			} else {
				onCoreDestroyed(null);
			}
			flagCoreDestroyedByExplosion = null;
		}
		if(flagCharacterExitCheckByExplosion) {
			checkCharacterExit();
			flagCharacterExitCheckByExplosion = false;
		}
		super.updateLocal(timer);
		getState().getDebugTimer().end(this, "EditableSegmentController");
	}

	public void addExplosion(Damager from, DamageDealerType damageDealerType, HitType hitType, long weaponId, Transform where, float radius, float damage, boolean chain, AfterExplosionCallback c, int type) {
		sendExplosionGraphic(where.origin);
		ExplosionData e = new ExplosionData();
		e.damageType = DamageDealerType.EXPLOSIVE;
		e.centerOfExplosion = new Transform(where);
		e.fromPos = new Vector3f(where.origin);
		e.toPos = new Vector3f(where.origin);
		e.radius = radius;
		e.damageInitial = damage;
		e.damageBeforeShields = 0;
		e.sectorId = getSectorId();
		e.hitsFromSelf = (type & ExplosionData.INNER) == ExplosionData.INNER;
		e.from = from;
		e.weaponId = Long.MIN_VALUE;
		e.ignoreShieldsSelf = (type & ExplosionData.INNER) == ExplosionData.INNER;
		e.ignoreShields = (type & ExplosionData.IGNORESHIELDS_GLOBAL) == ExplosionData.IGNORESHIELDS_GLOBAL;
		e.chain = chain;
		e.attackEffectSet = from.getAttackEffectSet(weaponId, damageDealerType);
		assert (e.attackEffectSet != null);
		e.hitType = hitType;
		e.afterExplosionHook = c;
		Sector sector = ((GameServerState) getState()).getUniverse().getSector(getSectorId());
		if(sector != null) {
			ExplosionRunnable n = new ExplosionRunnable(e, sector);
			((GameServerState) getState()).enqueueExplosion(n);
		}
	}

	@Override
	public void newNetworkObject() {
		this.setNetworkObject(new NetworkSegmentController(getState(), this));
	}

	public SegmentPiece getNextToNearestPiece(Vector3f from, Vector3f dir, Vector3i absOnOut, float editDist, Vector3i wantedBuildSize, Vector3i buildSize) throws ElementPositionBlockedException, BlockNotBuildTooFast {
		CollisionWorld.ClosestRayResultCallback testRayCollisionPoint;
		testRayCollisionPoint = ((GameClientState) getState()).getWorldDrawer().getBuildModeDrawer().testRayCollisionPoint;
		if(testRayCollisionPoint != null && testRayCollisionPoint.hasHit() && testRayCollisionPoint instanceof CubeRayCastResult) {
			Vector3f hitPointWorld = new Vector3f(testRayCollisionPoint.hitPointWorld);
			CubeRayCastResult c = (CubeRayCastResult) testRayCollisionPoint;
			CubeRayCastResult cubeResult = (CubeRayCastResult) testRayCollisionPoint;
			if(cubeResult.getSegment() == null) {
				System.err.println("CUBERESULT SEGMENT NULL");
				return null;
			}
			// cacheLastHitSegmentFromAddAndRemove = cubeResult.getSegment();
			Vector3i p = new Vector3i(cubeResult.getSegment().pos.x, cubeResult.getSegment().pos.y, cubeResult.getSegment().pos.z);
			absOnOut.set(cubeResult.getSegment().pos.x + cubeResult.getCubePos().x, cubeResult.getSegment().pos.y + cubeResult.getCubePos().y, cubeResult.getSegment().pos.z + cubeResult.getCubePos().z);
			p.x += (cubeResult.getCubePos().x - SegmentData.SEG_HALF);
			p.y += (cubeResult.getCubePos().y - SegmentData.SEG_HALF);
			p.z += (cubeResult.getCubePos().z - SegmentData.SEG_HALF);
			if(((GameClientState) getState()).getCurrentSectorId() == getSectorId()) {
				getWorldTransformInverse().transform(hitPointWorld);
			} else {
				Transform t = new Transform(getWorldTransformOnClient());
				t.inverse();
				t.transform(hitPointWorld);
			}
			IntSet disabledSides = new IntOpenHashSet();
			for(int i = 0; i < 6; ++i) {
				Vector3i dir0 = Element.DIRECTIONSi[i];
				SegmentPiece piece = cubeResult.getSegment().getSegmentController().getSegmentBuffer().getPointUnsave(new Vector3i(p.x + dir0.x + SegmentData.SEG_HALF, p.y + dir0.y + SegmentData.SEG_HALF, p.z + dir0.z + SegmentData.SEG_HALF));
				if(piece != null && piece.getType() != Element.TYPE_NONE) {
					disabledSides.add(i);
				}
			}
			SegmentPiece pieceAt = cubeResult.getSegment().getSegmentController().getSegmentBuffer().getPointUnsave(new Vector3i(p.x + SegmentData.SEG_HALF, p.y + SegmentData.SEG_HALF, p.z + SegmentData.SEG_HALF));
			int side = Element.getSide(hitPointWorld, pieceAt == null ? null : pieceAt.getAlgorithm(), p, pieceAt != null ? pieceAt.getType() : (short) 0, pieceAt != null ? pieceAt.getOrientation() : 0, disabledSides);
			System.err.println("[GETNEXTTONEAREST] SIDE: " + Element.getSideString(side) + ": " + hitPointWorld + "; " + p);
			switch(side) {
				case (Element.RIGHT) -> {
					p.x += 1f;
					buildSize.set(wantedBuildSize.x, wantedBuildSize.y, wantedBuildSize.z);
				}
				case (Element.LEFT) -> {
					p.x -= 1f;
					buildSize.set(-wantedBuildSize.x, wantedBuildSize.y, wantedBuildSize.z);
				}
				case (Element.TOP) -> {
					p.y += 1f;
					buildSize.set(wantedBuildSize.x, wantedBuildSize.y, wantedBuildSize.z);
				}
				case (Element.BOTTOM) -> {
					p.y -= 1f;
					buildSize.set(wantedBuildSize.x, -wantedBuildSize.y, wantedBuildSize.z);
				}
				case (Element.FRONT) -> {
					p.z += 1f;
					buildSize.set(wantedBuildSize.x, wantedBuildSize.y, wantedBuildSize.z);
				}
				case (Element.BACK) -> {
					p.z -= 1f;
					buildSize.set(wantedBuildSize.x, wantedBuildSize.y, -wantedBuildSize.z);
				}
				default -> System.err.println("[BUILDMODEDRAWER] WARNING: NO SIDE recognized!!!");
			}
			p.add(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF);
			SegmentPiece piece = new SegmentPiece();
			piece = getSegmentBuffer().getPointUnsave(p, piece);
			if(piece == null) {
				throw new BlockNotBuildTooFast(p);
			}
			if(piece != null && piece.getSegment().isEmpty()) {
				SegmentData newSegmentData = getSegmentProvider().getFreeSegmentData();
				newSegmentData.assignData(piece.getSegment());
			}
			boolean blocked = false;
			SegmentCollisionCheckerCallback callback = new SegmentCollisionCheckerCallback();
			try {
				if(piece != null && getCollisionChecker().checkPieceCollision(piece, callback, true)) {
					blocked = true;
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
			if(piece != null && getState() instanceof GameClientState && ((GameClientState) getState()).getController().getTutorialMode() != null && ((GameClientState) getState()).getController().getTutorialMode().getMachine().getFsm().getCurrentState() instanceof PlaceElementTestState) {
				PlaceElementTestState cc = (PlaceElementTestState) ((GameClientState) getState()).getController().getTutorialMode().getMachine().getFsm().getCurrentState();
				if(cc.getWhere() != null && !piece.equalsPos(cc.getWhere())) {
					((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Please place the block\nat the indicated position."), 0);
					return null;
				}
			}
			if(blocked) {
				throw new ElementPositionBlockedException(callback.userData);
			}
			// piece is null if out of bounds
			return piece;
		}
		return null;
	}

	protected abstract String getSegmentControllerTypeString();

	//
	@Override
	public void handleBeingSalvaged(BeamState hittingBeam, BeamHandlerContainer<? extends SimpleTransformableSendableObject> container, Vector3f to, SegmentPiece hitPiece, int beamHitsForReal) {
		// can be overwritten id needed
		if(this instanceof TransientSegmentController) {
			((TransientSegmentController) this).setTouched(true, true);
		}

		SimpleTransformableSendableObject<?> shootingEntity = hittingBeam.getHandler().getShootingEntity();
		if(shootingEntity != null) {
			AbstractOwnerState ownerState = shootingEntity.getOwnerState();
			if(ownerState instanceof PlayerState) {
				if(getType() == EntityType.ASTEROID || getType() == EntityType.ASTEROID_MANAGED) {
					((PlayerState) ownerState).fireMusicTag(MusicTags.MINING);
				} else {
					((PlayerState) ownerState).fireMusicTag(MusicTags.SALVAGING);
				}
			}
		}
	}

	@Override
	public boolean isRepariableFor(RepairBeamHandler harvester, String[] cannotHitReason, Vector3i position) {
		ManagerContainer<?> managerContainer;
		if(this instanceof ManagedSegmentController<?>) {
			managerContainer = ((ManagedSegmentController<?>) this).getManagerContainer();
			if(managerContainer.getRepairDelay() > 0) {
				String msg = Lng.str("Cannot use repair beam on this structure for another %s sec.\nafter it last took damage.", (int) Math.ceil(managerContainer.getRepairDelay()));
				// harvester.getBeamShooter().popupOwnClientMessage("NoRepNoteTo", msg,  ServerMessage.MESSAGE_TYPE_ERROR);
				cannotHitReason[0] = msg;
				return false;
			}
		}
		return true;
	}

	/**
	 * @param powerDamage
	 * @param sendPowerUpdate (should only be true if the damage is server only, so not for example for normal projectiles)
	 */
	public void powerDamage(float powerDamage, boolean sendPowerUpdate) {
		ManagerContainer<?> managerContainer;
		if(powerDamage > 0 && this instanceof ManagedSegmentController<?> && (managerContainer = ((ManagedSegmentController<?>) this).getManagerContainer()) instanceof PowerManagerInterface) {
			((PowerManagerInterface) managerContainer).getPowerAddOn().consumePowerInstantly(powerDamage, true);
			if(sendPowerUpdate) {
				((PowerManagerInterface) managerContainer).getPowerAddOn().sendPowerUpdate();
			}
		}
	}

	// @Override
	// public ParticleHitCallback handleHit(ParticleHitCallback callback, Damager damager, float damage, float damageBeforeShield, Vector3f startPos, Vector3f endPos, boolean shieldAbsorbed, long weaponId) {
	//
	// if (this instanceof TransientSegmentController) {
	// ((TransientSegmentController) this).setTouched(true, true);
	// }
	//
	//
	//
	// SegmentData segmentData = callback.blockHit.getSegmentData();
	//
	// int infoIndex = callback.blockHit.localBlock;
	//
	//
	// short type = segmentData.getType(infoIndex);
	// short healthBefore = segmentData.getHitpoints(infoIndex);
	// if (healthBefore <= 0 && type == 0) {
	// if(isOnServer()){
	// System.err.println("[SERVER][WARNING] " + getState() + " " + this + " BlockHealth is already 0 of "+infoIndex);
	// }
	// callback.hit = false;
	// return callback;
	// }
	//
	// Starter.modManager.onSegmentControllerHitByLaser(this);
	//
	// FastSegmentControllerStatus status = getBlockEffectManager().status;
	//
	// float restDamage = 0;
	// float armorEfficiency = 0;
	// int explosiveRadius = 0;
	// float pushForce = 0;
	// float pullForce = 0;
	// float grabForce = 0;
	// float powerDamage = 0;
	// EffectElementManager<?, ?, ?> effect = null;
	// if (effectType != 0) {
	// effect = getEffect(damager, effectType);
	//
	// if (effect != null) {
	//
	// effect.onHit(this);
	//
	// float damageBef = damage;
	// //				System.err.println("HIT EFFECT::: "+effect+": Pier "+effect.isPiercing()+"; punch "+effect.isPunchThrough()+"; expl "+effect.isExplosive());
	//
	// armorEfficiency = effect.getCannonArmorEfficiency() * effectRatio;
	// pushForce = effect.getCannonPush() * effectRatio * effectSize;
	// pullForce = effect.getCannonPull() * effectRatio * effectSize;
	// grabForce = effect.getCannonGrab() * effectRatio * effectSize;
	//
	// powerDamage = effect.getPowerDamage(damageBeforeShield, effectRatio, status);
	//
	// if (effect.isExplosive()) {
	// explosiveRadius = (int) Math.max(0, effect.getCannonExplosiveRadius());
	// }
	// float armor = Math.max(0f, ElementKeyMap.getInfo(type).getArmor() + ElementKeyMap.getInfo(type).getArmor() * armorEfficiency);
	// short hitpointsEffective = (short) (healthBefore + healthBefore * armor);
	//
	// if (effect.isPiercing()) {
	// restDamage = damage;
	// if (effectRatio == 0) {
	// restDamage = 0;
	// } else {
	//
	// restDamage *= effectRatio * (effect.getPierchingDamagePresevedOnImpact() * (1f - status.pierchingProtection));
	// }
	// }
	//
	// if (effect.isPunchThrough() && ElementKeyMap.isValidType(type)) {
	//
	// restDamage = Math.max(0, damage - hitpointsEffective);
	// restDamage *= effectRatio * effect.getPunchThroughDamagePreserved() * (1f - status.pierchingProtection);
	// }
	// damage = effect.modifyBlockDamage(damage, DamageDealerType.PROJECTILE, effectRatio);
	// if (damager != null && damager instanceof SegmentController && railController.isInAnyRailRelationWith(((SegmentController) damager))) {
	// damage = 0;
	// }
	// //				System.err.println("DAMAGE MOD: "+damageBef+" -> "+damage+" -rest> "+restDamage+" ;hpBef: "+healthBefore );
	// }
	// } else {
	// if (damager != null && damager instanceof SegmentController && railController.isInAnyRailRelationWith(((SegmentController) damager))) {
	// damage = 0;
	// }
	// }
	//
	// powerDamage(powerDamage, false);
	//
	//
	//
	// short oldType = callback.blockHit.getSegmentData().getType(callback.blockHit.localBlock);
	// if (!checkAttack(damager, true, true) && !(!isOnServer() && ((GameClientState) getState()).getPlayer().isInTutorial())) {
	// callback.hit = false;
	// callback.abortWhole = true;
	// return callback;
	// }
	// boolean ignoreArmor = false;
	// damage = getHpController().onHullDamage(damager, damage, type, effectRatio, DamageDealerType.PROJECTILE, -1, ignoreArmor, effect);
	//
	// if (isOnServer()) {
	//
	// //segmentData.segment may become null after killing the last block. it's still needed for the explosive damage
	// Segment fromSeg = segmentData.getSegment();
	//
	// int propagationTest = 0; //switch to 0 to turn off
	// int propagationDamage = 10000;
	// this.acidDamageManagerServer.inputDamage(fromSeg.getAbsoluteIndex(infoIndex), propagationDamage, propagationTest, damager);
	//
	// byte x = SegmentData.getPosXFromIndex(callback.blockHit.localBlock);
	// byte y = SegmentData.getPosYFromIndex(callback.blockHit.localBlock);
	// byte z = SegmentData.getPosZFromIndex(callback.blockHit.localBlock);
	// float actualDamage = damageElement(type, infoIndex, segmentData, (int) damage, armorEfficiency, damager, effectRatio, DamageDealerType.PROJECTILE, effect);
	// callback.hit = true;
	// callback.addDamageDone(actualDamage);
	// if (explosiveRadius > 0 && actualDamage > 0) {
	//
	// if (fromSeg != null) {
	// int lDir = -1;
	// if(callback.afterBlockIndex == Long.MIN_VALUE && callback.beforeBlockIndex != Long.MIN_VALUE){
	// //we are on the last block of this update (penetration)
	// //dont hit the block that is opposite to the back
	//
	// int bx = ElementCollection.getPosX(callback.beforeBlockIndex);
	// int by = ElementCollection.getPosY(callback.beforeBlockIndex);
	// int bz = ElementCollection.getPosZ(callback.beforeBlockIndex);
	//
	// for(int k = 0; k < 6; k++){
	// int gx = Element.DIRECTIONSb[k].x + bx;
	// int gy = Element.DIRECTIONSb[k].y + by;
	// int gz = Element.DIRECTIONSb[k].z + bz;
	// long bi = ElementCollection.getIndex(gx, gy, gz);
	// if(bi == callback.blockHit.block){
	// //this is the direction of the projectile
	// lDir = k;
	// break;
	// }
	// }
	//
	// }
	//
	// for (int i = 0; i < 6; i++) {
	// if(i == lDir){
	// //we are on the last block of the update
	// //and found this direction to be the direction
	// //the projectile is flying.
	// //Do not apply explosive damage in that direction
	// //or the next update might hit air, which then
	// //in turn doesnt cause explosive damage
	// continue;
	// }
	//
	// long sideIndex = ElementCollection.getSide(infoIndex, i);
	// Vector3b inOut = new Vector3b(x, y, z);
	// inOut.add(Element.DIRECTIONSb[i]);
	//
	//
	//
	// Segment neighboringSegment = getNeighboringSegment(inOut, fromSeg, new Vector3i());
	//
	//
	//
	// SegmentData d;
	// if (neighboringSegment != null && (d = neighboringSegment.getSegmentData()) != null && !neighboringSegment.isEmpty()) {
	// int absX = neighboringSegment.pos.x+inOut.x;
	// int absY = neighboringSegment.pos.y+inOut.y;
	// int absZ = neighboringSegment.pos.z+inOut.z;
	// long absIndex = ElementCollection.getIndex(absX, absY, absZ);
	//
	// if(
	// callback.afterBlockIndex == absIndex ||
	// callback.beforeBlockIndex == absIndex){
	// //dont hit the block that is in the path of the projectile anyways
	// //also dont hit the last block of the update to prevent
	// //missing damage for the next update (when hitting air possibly
	// continue;
	// }
	// int neighInfoIndex = SegmentData.getInfoIndex(inOut);
	// short t = d.getType(neighInfoIndex);
	// short hitpoints = d.getHitpoints(neighInfoIndex);
	// if (t != Element.TYPE_NONE && hitpoints > 0) {
	// float sideDamage = damageElement(t, neighInfoIndex, d, (int) ((damage / 6f) * effectRatio), armorEfficiency, damager, effectRatio, DamageDealerType.PROJECTILE, effect);
	// short afterDamHitpoints = d.getHitpoints(neighInfoIndex);
	//
	//
	// if(afterDamHitpoints <= 0){
	//
	// sendBlockHp(absIndex, (short) 0);
	// onBlockKill(absIndex, t, damager);
	// }else{
	// onBlockDamage(absIndex, t, (int)sideDamage, DamageDealerType.PROJECTILE, damager);
	// sendBlockHp(absX, absY, absZ, afterDamHitpoints);
	// }
	// }
	// }
	// }
	// } else {
	// System.err.println("[HANDLEHIT] Exception: FromSegment is null " + segmentData);
	// }
	// }
	// sendHitConfirmToDamager(damager, false);
	//
	// if (actualDamage > 0) {
	// onDamageServer(actualDamage, damager);
	// }
	// if (pullForce > 0) {
	// Vector3f force = new Vector3f();
	// force.sub(endPos, startPos);
	// if (force.lengthSquared() == 0) {
	// force.set(callback.hitNormalWorld);
	// } else {
	// force.normalize();
	// }
	// getBlockEffectManager().addEffect(new PullEffect(this, force, pullForce, false));
	// }
	// if (pushForce > 0) {
	// Vector3f force = new Vector3f();
	// force.sub(endPos, startPos);
	// if (force.lengthSquared() == 0) {
	// force.set(callback.hitNormalWorld);
	// } else {
	// force.normalize();
	// }
	// Vector3f relPos = new Vector3f(callback.hitPointWorld);
	// getWorldTransformInverse().transform(relPos);
	// //				getWorldTransformInverse().transform(force);
	// getBlockEffectManager().addEffect(new PushEffect(this, relPos, force, pushForce, false));
	// }
	// if (grabForce > 0) {
	// getBlockEffectManager().addEffect(new StopEffect(this, grabForce));
	// }
	// final Segment seg = segmentData.getSegment();
	// if(seg != null){
	// ((RemoteSegment)seg).setLastChanged(System.currentTimeMillis());
	// }
	//
	//
	// if (segmentData.getType(callback.blockHit.localBlock) == Element.TYPE_NONE) {
	// callback.killedBlock = true;
	// }
	// if(segmentData.getType(callback.blockHit.localBlock) != ElementKeyMap.CORE_ID &&
	// segmentData.getHitpoints(callback.blockHit.localBlock) == 0){
	// //				System.err.println("BLOCK KILL ON "+getState()+"; "+this);
	// sendBlockKill(callback.blockHit.block);
	// onBlockKill(callback.blockHit.block, oldType, damager);
	// }else{
	// assert(segmentData.getType(callback.blockHit.localBlock) == ElementKeyMap.CORE_ID || segmentData.getHitpoints(callback.blockHit.localBlock) > 0):segmentData.getHitpoints(callback.blockHit.localBlock);
	// sendBlockHp(callback.blockHit.block, (short)segmentData.getHitpoints(callback.blockHit.localBlock));
	// onBlockDamage(callback.blockHit.block, oldType, (int)callback.getDamageDone(), DamageDealerType.PROJECTILE, damager);
	// }
	// }
	// if (!isOnServer()) {
	// //			System.err.println(this+"  "+this.getState()+" HANDLE HIT "+result.hitPointWorld);
	// ElementInformation elementInformation = ElementKeyMap.getInfo(type);
	// //			int actualDamage = (int) Math.max(0,damage - Math.ceil(damage*elementInformation.getArmourPercent()));
	// float actualDamage = Math.max(0, damage);
	// //			byte x = SegmentData.getPosXFromIndex(callback.blockHit.localBlock);
	// //			byte y = SegmentData.getPosYFromIndex(callback.blockHit.localBlock);
	// //			byte z = SegmentData.getPosZFromIndex(callback.blockHit.localBlock);
	// //			if (explosiveRadius > 0 && actualDamage > 0) {
	// //				for (int i = 0; i < 6; i++) {
	// //					long sideIndex = ElementCollection.getSide(infoIndex, i);
	// //					Vector3b inOut = new Vector3b(x,y,z);
	// //					inOut.add(Element.DIRECTIONSb[i]);
	// //					Vector3i out = new Vector3i();
	// //					Segment neighboringSegment = getNeighboringSegment(inOut, segmentData.getSegment(), out);
	// //
	// //					if (neighboringSegment != null && !neighboringSegment.isEmpty()) {
	// //						int neightInfoIndex = SegmentData.getInfoIndex(inOut);
	// //						short typeNeight = neighboringSegment.getSegmentData().getType(neightInfoIndex);
	// //						if (typeNeight != Element.TYPE_NONE) {
	// //							Transform t = new Transform();
	// //							t.setIdentity();
	// //							t.origin.set(out.x + inOut.x - SegmentData.SEG_HALF, out.y + inOut.y - SegmentData.SEG_HALF, out.z + inOut.z - SegmentData.SEG_HALF);
	// //
	// //							getWorldTransformOnClient().transform(t.origin);
	// //							if(HudIndicatorOverlay.toDrawTexts.size() < 300){
	// //								HudIndicatorOverlay.toDrawTexts.add(new RaisingIndication(t, actualDamage / 6f * effectRatio < 1f ? String.valueOf("<1") : String.valueOf(Math.round(actualDamage / 6 * effectRatio)), 1, 0.3f, 0, 1));
	// //							}
	// //						}
	// //					}
	// //				}
	// //			}
	// if (callback.getDamageDone() + actualDamage >= segmentData.getHitpoints(callback.blockHit.localBlock)) {
	// callback.killedBlock = true;
	// }
	// callback.addDamageDone(actualDamage);
	// callback.hit = true;
	// }
	// CollisionObject pObject = getPhysicsDataContainer().getObject();
	// if (pObject != null) {
	// pObject.activate(true);
	// }
	// Starter.modManager.onSegmentControllerDamageTaken(this);
	// return callback;
	// }
	@Override
	public void cleanUpOnEntityDelete() {
		super.cleanUpOnEntityDelete();
		if(isOnServer()) {
			acidDamageManagerServer.clear();
		}
	}

	@Override
	public boolean checkAttack(Damager from, boolean checkDocked, boolean notifyFaction) {
		// don't let spectators damage or be damaged
		if(isSpectator() || (from != null && from instanceof SimpleTransformableSendableObject && ((SimpleTransformableSendableObject) from).isSpectator())) {
			if(from instanceof SimpleTransformableSendableObject && ((SimpleTransformableSendableObject) from).isClientOwnObject()) {
				((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Cannot attack\nspectators!"), 0);
			}
			return false;
		}
		if(isOnServer()) {
			if(!isVulnerable()) {
				if(from != null) {
					if(from instanceof PlayerState) {
						((PlayerState) from).lastSectorProtectedMsgSent = System.currentTimeMillis();
						((PlayerState) from).sendServerMessage(new ServerMessage(Lng.astr("Structure is invulnerable!"), ServerMessage.MESSAGE_TYPE_WARNING, ((PlayerState) from).getId()));
					}
				}
				return false;
			}
			if(this instanceof PlayerControllable) {
				for(PlayerState s : ((PlayerControllable) this).getAttachedPlayers()) {
					if(s.isGodMode()) {
						// System.err.println("[SHIP][SegmentController] "+getState()+" cannot hit ship of godmode player "+s);
						if(from != null && from instanceof PlayerControllable) {
							List<PlayerState> attachedPlayers = ((PlayerControllable) from).getAttachedPlayers();
							for(int i = 0; i < attachedPlayers.size(); i++) {
								PlayerState ps = attachedPlayers.get(i);
								if(System.currentTimeMillis() - ps.lastSectorProtectedMsgSent > 5000) {
									ps.lastSectorProtectedMsgSent = System.currentTimeMillis();
									ps.sendServerMessage(new ServerMessage(Lng.astr("The vessel you are trying to\nhit is in godmode from player %s!", s.getName()), ServerMessage.MESSAGE_TYPE_WARNING, ps.getId()));
								}
							}
						}
						return false;
					}
				}
			}
			if(notifyFaction && getFactionId() != 0) {
				// player faction
				Faction f = ((FactionState) getState()).getFactionManager().getFaction(getFactionId());
				if(f != null) {
					f.onAttackOnServer(from);
				} else {
					System.err.println("[SERVER][EDITABLESEGMENTCONTROLLER] ON HIT: faction not found: " + getFactionId());
				}
				Fleet fleet = getFleet();
				if(fleet != null) {
					fleet.onHitFleetMember(from, this);
				}
			}
			Sector sector = ((GameServerState) getState()).getUniverse().getSector(getSectorId());
			if(sector != null) {
				if(sector.isProtected()) {
					// if (from != null && from instanceof PlayerControllable) {
					// List<PlayerState> attachedPlayers = ((PlayerControllable) from).getAttachedPlayers();
					// for (int i = 0; i < attachedPlayers.size(); i++) {
					// PlayerState ps = attachedPlayers.get(i);
					// if (System.currentTimeMillis() - ps.lastSectorProtectedMsgSent > 5000) {
					// ps.lastSectorProtectedMsgSent = System.currentTimeMillis();
					// ps.sendServerMessage(new ServerMessage(Lng.astr("This Sector is Protected!"), ServerMessage.MESSAGE_TYPE_WARNING, ps.getId()));
					// }
					// }
					// }
					return false;
				}
				((GameServerState) getState()).getUniverse().attackInSector(sector.pos);
			}
			if(!canAttack(from)) {
				return false;
			}

			if(checkDocked && from != null && from instanceof SegmentController && ((SegmentController) from).railController.isInAnyRailRelationWith(this)) {
				return false;
			}
			PlayerState p;
			if((p = isInGodmode()) != null) {
				// System.err.println("[SHIP][SegmentController] "+getState()+" cannot hit ship of godmode player "+p);
				if(from != null && from instanceof PlayerControllable) {
					List<PlayerState> attachedPlayers = ((PlayerControllable) from).getAttachedPlayers();
					for(int i = 0; i < attachedPlayers.size(); i++) {
						PlayerState ps = attachedPlayers.get(i);
						if(System.currentTimeMillis() - ps.lastSectorProtectedMsgSent > 5000) {
							ps.lastSectorProtectedMsgSent = System.currentTimeMillis();
							ps.sendServerMessage(new ServerMessage(Lng.astr("The vessel you are trying to\nhit is in godmode from player %s!", p.getName()), ServerMessage.MESSAGE_TYPE_WARNING, ps.getId()));
						}
					}
				}
				return false;
			}
		} else {
			RemoteSector remoteSector = getRemoteSector();
			if(remoteSector != null && remoteSector.isProtectedClient()) {
				if(from != null) {
					from.sendClientMessage(Lng.str("This Sector is protected!"), ServerMessage.MESSAGE_TYPE_WARNING);
				}
				return false;
			}
			if(this instanceof PlayerControllable) {
				for(PlayerState s : ((PlayerControllable) this).getAttachedPlayers()) {
					if(s.isGodMode()) {
						// System.err.println("[SHIP][SegmentController] "+getState()+" cannot hit ship of godmode player "+s);
						if(from != null && from instanceof PlayerControllable) {
							List<PlayerState> attachedPlayers = ((PlayerControllable) from).getAttachedPlayers();
							for(int i = 0; i < attachedPlayers.size(); i++) {
								PlayerState ps = attachedPlayers.get(i);
								if(System.currentTimeMillis() - ps.lastSectorProtectedMsgSent > 5000) {
									ps.lastSectorProtectedMsgSent = System.currentTimeMillis();
									ps.sendServerMessage(new ServerMessage(Lng.astr("The vessel you are trying to\nhit is in godmode from player %s!", s.getName()), ServerMessage.MESSAGE_TYPE_WARNING, ps.getId()));
								}
							}
						}
						return false;
					}
				}
			}
			if(!canAttack(from)) {
				return false;
			}
		}
		return true;
	}

	// @Override
	// public int handleBeamDamage(BeamState beam, int beamHits, BeamHandlerContainer<? extends SimpleTransformableSendableObject> beamOwner, Vector3f from, Vector3f to, CubeRayCastResult cubeResult, boolean ignoreShields, Timer timer) {
	//
	//
	// if (this instanceof TransientSegmentController) {
	// ((TransientSegmentController) this).setTouched(true, true);
	// }
	//
	// Starter.modManager.onSegmentControllerHitByBeam(this);
	// SegmentPiece segmentPiece = new SegmentPiece(cubeResult.getSegment(), cubeResult.getCubePos());
	//
	// short oldType = segmentPiece.getType();
	// Damager damager = beamOwner.getHandler().getBeamShooter();
	// if (!checkAttack(damager, true, true)) {
	// return 0;
	// }
	//
	// final long weaponId = beam.weaponId;
	//
	// float armorEfficiency = 0;
	// int explosiveRadius = 0;
	// float pushForce = 0;
	// float pullForce = 0;
	// float grabForce = 0;
	//
	//
	// boolean piercing = false;
	// boolean punchThrough = false;
	// float piercingPreserve = 0;
	// float punchThroughPreserve = 0;
	// float powerDamage = 0;
	// if (beamHits > 0) {
	// FastSegmentControllerStatus status = getBlockEffectManager().status;
	// float damage = (int) (beamHits * beam.getPower());
	//
	// DamageDealerType damageType = DamageDealerType.BEAM;
	// damage *= getDamageTakenMultiplier(damageType);
	//
	// EffectElementManager<?, ?, ?> effect = null;
	// if (effectType != 0) {
	//
	// effect = getEffect(beamOwner.getHandler().getBeamShooter(), effectType);//((EffectManagerContainer)((ManagedSegmentController<?>)this).getManagerContainer()).getEffect(effectType);
	// if (effect != null) {
	//
	// effect.onHit(this);
	// float damageBef = damage;
	//
	// armorEfficiency = effect.getBeamArmorEfficiency() * effectRatio;
	// pushForce = effect.getBeamPush() * effectRatio * effectSize;
	// pullForce = effect.getBeamPull() * effectRatio * effectSize;
	// grabForce = effect.getBeamGrab() * effectRatio * effectSize;
	//
	// powerDamage = effect.getPowerDamage(damage, effectRatio, status);
	//
	// if (effect.isExplosive()) {
	// explosiveRadius = (int) Math.max(0, effect.getBeamExplosiveRadius());
	// }
	//
	// if (effect.isPiercing()) {
	// piercing = true;
	// piercingPreserve = effect.getPierchingDamagePresevedOnImpact() * (1f - status.pierchingProtection);
	// }
	// if (effect.isPunchThrough()) {
	// punchThrough = true;
	// punchThroughPreserve = effect.getPunchThroughDamagePreserved();
	// }
	//
	// }
	// }
	// if(damager != null){
	// damage *= damager.getDamageGivenMultiplier();
	// }
	//
	// boolean shieldHit = false;
	// if (!ignoreShields && this instanceof ManagedSegmentController<?> && ((EffectManagerContainer) ((ManagedSegmentController<?>) this).getManagerContainer()) instanceof ShieldContainerInterface) {
	// ShieldContainerInterface sh = ((ShieldContainerInterface) ((EffectManagerContainer) ((ManagedSegmentController<?>) this).getManagerContainer()));
	// if(isUsingLocalShields()){
	// if(sh.getShieldAddOn().isUsingLocalShieldsAtLeastOneActive() || railController.isDockedAndExecuted()){
	// damage = (float) sh.handleShieldHit(damager, cubeResult.hitPointWorld, DamageDealerType.BEAM, damage, effectType, effectRatio, effectSize);
	// if (damage <= 0 && (effect == null || !effect.isEffectIgnoreShields())) {
	// sendHitConfirmToDamager(beamOwner, true);
	// return 0;
	// }
	// }
	// }else{
	// //check if shields are present so we dont return on 0 damage (to apply possible effects)
	// if (sh.getShieldAddOn().getShields() > 0 || railController.isDockedAndExecuted()) {
	// damage = (float) sh.handleShieldHit(damager, cubeResult.hitPointWorld, DamageDealerType.BEAM, damage, effectType, effectRatio, effectSize);
	// if (damage <= 0 && (effect == null || !effect.isEffectIgnoreShields())) {
	// sendHitConfirmToDamager(beamOwner, true);
	// return 0;
	// }
	// }
	//
	// shieldHit = sh.getShieldAddOn().getShields() > 0;
	// }
	// }
	// sendHitConfirmToDamager(beamOwner, shieldHit);
	//
	// if (effect != null) {
	// damage = effect.modifyBlockDamage(damage, DamageDealerType.BEAM, effectRatio);
	// }
	// powerDamage(powerDamage, false);
	//
	// int blockDeepness = DamageBeamElementManager.calculateBlockDeepness(damage);
	//
	// damage = getHpController().onHullDamage(damager, damage, segmentPiece.getType(), effectRatio, DamageDealerType.BEAM, blockDeepness, beam.ignoreArmor, effect);
	//
	// Int2ObjectOpenHashMap<BlockRecorder> recorded = null;
	// if (blockDeepness > 0) {
	// CubeRayCastResult rayResult = new CubeRayCastResult(
	// from, to, damager, null);
	//
	// rayResult.setIgnoereNotPhysical(true);
	//
	// rayResult.setFilter(this);
	//
	//
	// rayResult.setDebug(isOnServer() && EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn());
	// recorded = new Int2ObjectOpenHashMap<BlockRecorder>();
	// rayResult.setRecordedBlocks(recorded, blockDeepness);
	// rayResult.setRecordAllBlocks(true);
	// rayResult.setDamageTest(true);
	// assert (!rayResult.hasHit());
	//
	// SimpleTransformableSendableObject beamShooter = beamOwner.getHandler().getBeamShooter();
	//
	// beamShooter.getPhysics().testRayCollisionPoint(from, to, rayResult, false);
	//
	// }
	// int minDamage = beam.beamType == PersonalBeamHandler.TORCH ? 1 : 0;
	// if (isOnServer()) {
	//
	// SegmentPiece p = segmentPiece;
	// if (pullForce > 0) {
	// Vector3f force = new Vector3f();
	// force.sub(beam.to, beam.from);
	//
	// if (force.lengthSquared() == 0) {
	// force.set(cubeResult.hitNormalWorld);
	// } else {
	// force.normalize();
	// }
	// getBlockEffectManager().addEffect(new PullEffect(this, force, pullForce, false));
	// }
	// if (pushForce > 0) {
	// Vector3f force = new Vector3f();
	// force.sub(beam.to, beam.from);
	//
	// if (force.lengthSquared() == 0) {
	// force.set(cubeResult.hitNormalWorld);
	// } else {
	// force.normalize();
	// }
	// Vector3f relPos = new Vector3f(cubeResult.hitPointWorld);
	// getWorldTransformInverse().transform(relPos);
	// getBlockEffectManager().addEffect(new PushEffect(this, relPos, force, pushForce, false));
	// }
	// if (grabForce > 0) {
	// getBlockEffectManager().addEffect(new StopEffect(this, grabForce));
	// }
	// {
	//
	// }
	//
	//
	// if (recorded != null) {
	// try{
	// int blockIndex = 0;
	// BlockRecorder longOpenHashSet;
	// if ((longOpenHashSet = recorded.get(this.getId())) != null) {
	// int size = Math.min(longOpenHashSet.size(), blockDeepness);
	//
	// for (int i = blockIndex; i < size; i++) {
	//
	// //If the current looked at block isn't armor, just apply damage once and move on to the next
	// //If the current looked at block is armor, apply damage to that block till it's dead, then move on
	//
	// //encountered armor already, apply damage on armorPos block till block is destroyed
	//
	// p = getSegmentBuffer().getPointUnsave(longOpenHashSet.blockAbsIndices.get(blockIndex));
	// if (p != null && ElementKeyMap.isValidType(p.getType())) {
	//
	// double blockIndexDamage = WeaponElementManager.calculateDamageForBlock(i, blockDeepness, damage);
	//
	// int infoIndex = p.getInfoIndex();
	//
	// if (explosiveRadius > 0 && blockIndexDamage > 0) {
	// if (p.getSegment() != null) {
	//
	// for (int j = 0; j < 6; j++) {
	// Vector3b inOut = new Vector3b();
	// p.getPos(inOut);
	// inOut.add(Element.DIRECTIONSb[j]);
	// Segment neighboringSegment = getNeighboringSegment(inOut, p.getSegment(), new Vector3i());
	// if (neighboringSegment != null && !neighboringSegment.isEmpty()) {
	// long absoluteIndex = neighboringSegment.getAbsoluteIndex(inOut.x, inOut.y, inOut.z);
	// SegmentData data = neighboringSegment.getSegmentData();
	// if(data != null && !longOpenHashSet.blockAbsIndices.contains(absoluteIndex)){
	// int neightInfoIndex = SegmentData.getInfoIndex(inOut);
	// short typeNeight = data.getType(neightInfoIndex);
	// if (typeNeight != Element.TYPE_NONE) {
	// //System.out.println("BEAM DAMAGE explosion: " + (neighboringSegment.pos.x + inOut.x) + " " + (neighboringSegment.pos.y + inOut.y) + " " + (neighboringSegment.pos.z + inOut.z));
	// int spltDamage = (int) (blockIndexDamage / 6 * effectRatio);
	// damageElement(typeNeight, neightInfoIndex, data, spltDamage, armorEfficiency, damager, effectRatio, DamageDealerType.BEAM, effect, beam.ignoreArmor, minDamage);
	//
	// if(data.getHitpoints(neightInfoIndex) == 0){
	// sendBlockHp(neighboringSegment.pos.x+inOut.x, neighboringSegment.pos.y+inOut.y, neighboringSegment.pos.z+inOut.z, (short)0);
	// onBlockKill(absoluteIndex, typeNeight, damager);
	// }else{
	// onBlockDamage(absoluteIndex, typeNeight, spltDamage, DamageDealerType.BEAM, damager);
	// sendBlockMod(new RemoteSegmentPiece(new SegmentPiece(neighboringSegment, inOut.x, inOut.y, inOut.z), getNetworkObject()));
	// }
	//
	// }
	// }
	// }
	// }
	// } else {
	// System.err.println("[HANDLEHIT] Exception: FromSegment is null " + cubeResult.getSegment());
	// }
	// }
	//
	// oldType = p.getType();
	// float damageDone = damageElement(p.getType(), infoIndex, p.getSegment().getSegmentData(), (int) FastMath.round(blockIndexDamage), armorEfficiency, damager, effectRatio, DamageDealerType.BEAM, effect, beam.ignoreArmor, minDamage);
	//
	// onDamageServer(damageDone, damager);
	//
	// ((RemoteSegment) p.getSegment()).setLastChanged(System.currentTimeMillis());
	//
	// p.refresh();
	//
	// if(p.getHitpoints() <= 0 ){
	// //always move on, doesn't matter what block it is
	// sendBlockKill(p);
	// onBlockKill(p.getAbsoluteIndex(), oldType, damager);
	//
	// blockIndex++;
	// }else{
	// onBlockDamage(p.getAbsoluteIndex(), oldType, (int) damageDone, DamageDealerType.BEAM, damager);
	// //if it's a system block, move on anyway
	// if (ElementKeyMap.getInfoFast(p.getType()).armorHP == 0){
	// blockIndex++;
	// }
	// p.getSegmentController().sendBlockMod(new RemoteSegmentPiece(p, getNetworkObject()));
	// }
	//
	// }
	// }
	// }
	// }finally{
	// for(BlockRecorder r : recorded.values()){
	// r.free();
	// }
	// }
	// }
	//
	// } else if (damage > 0 && ((GameClientState) getState()).getCurrentSectorEntities().containsKey(this.getId())) {
	//
	//
	// SegmentPiece p = segmentPiece;
	//
	//
	//
	// if (recorded != null) {
	// try{
	// //					System.err.println("CLIENT BEAM PIERCING RECORDED for "+this.getId()+": "+recorded);
	// int blockIndex = 0;
	// BlockRecorder longOpenHashSet;
	// if ((longOpenHashSet = recorded.get(this.getId())) != null && longOpenHashSet.size() > 0) {
	//
	//
	//
	// int size = Math.min(longOpenHashSet.size(), blockDeepness);
	// double[] hitIndicators = new double[size];
	// int hpLeft = 0;
	// if(getSegmentBuffer().getPointUnsave(longOpenHashSet.blockAbsIndices.get(blockIndex)) != null){
	// hpLeft = getSegmentBuffer().getPointUnsave(longOpenHashSet.blockAbsIndices.get(blockIndex)).getHitpoints();
	// }
	//
	//
	// //fill up hitindicators
	// Transform t = new Transform();
	// t.setIdentity();
	// for (int i = blockIndex; i < size; i++) {
	//
	// p = getSegmentBuffer().getPointUnsave(longOpenHashSet.blockAbsIndices.get(blockIndex));
	//
	// if (p != null && ElementKeyMap.isValidType(p.getType())) {
	//
	// double blockIndexDamage = WeaponElementManager.calculateDamageForBlock(i, blockDeepness, damage);
	//
	// ElementInformation elementInformation = ElementKeyMap.getInfoFast(p.getType());
	//
	//
	// float armor;
	//
	// if(beam.ignoreArmor){
	// armor = 0;
	// }else{
	// armor = Math.max(0f, (elementInformation.getArmor() +  elementInformation.getArmor() * armorEfficiency));
	// }
	//
	//
	// float actualDamage = Math.max(minDamage, (float) blockIndexDamage);
	//
	// short hitpointsEffective = (short) (hpLeft + hpLeft * armor);
	// short hitpointsEffectiveAfter = (short) Math.max(0, (hitpointsEffective - FastMath.round(actualDamage)));
	// short hitpointsAfter = (short) Math.max(0, hitpointsEffectiveAfter / (1 + armor));
	//
	// hpLeft -= (short) Math.max(0, hitpointsEffectiveAfter / (1 + armor));
	//
	// hitIndicators[blockIndex] += actualDamage;
	// if(i == 0){
	// if(hpLeft <= 0){
	//
	//
	// //p = getSegmentBuffer().getPointUnsave(longOpenHashSet.get(blockIndex), false);
	// p.getTransform(t);
	//
	// if(hitIndicators[blockIndex] != 0){
	// RaisingIndication raisingIndication = new RaisingIndication(t, String.valueOf((int) hitIndicators[blockIndex]),
	// 1, (0.75f/size * i), 0, 1.0f - (0.75f/size * i));
	// raisingIndication.speed = 0.5f;
	// HudIndicatorOverlay.toDrawTexts.add(raisingIndication);
	// }
	// blockIndex++;
	// if(blockIndex < size){
	// hpLeft = getSegmentBuffer().getPointUnsave(longOpenHashSet.blockAbsIndices.get(blockIndex)).getHitpoints();
	// }
	//
	// } else if ( elementInformation.armorHP == 0 || i == size - 1){
	//
	// //p = getSegmentBuffer().getPointUnsave(longOpenHashSet.get(blockIndex), false);
	// p.getTransform(t);
	//
	// if(hitIndicators[blockIndex] != 0){
	// HudIndicatorOverlay.toDrawTexts.add(new RaisingIndication(t, String.valueOf((int) hitIndicators[blockIndex]),
	// 1, (0.75f/size * i), 0, 1.0f - (0.75f/size * i)));
	// }
	// blockIndex++;
	// }
	// }
	// }
	// }
	//
	//
	// }
	// }finally{
	// for(BlockRecorder r : recorded.values()){
	// r.free();
	// }
	// }
	// }
	// }
	// }
	// CollisionObject pObject = getPhysicsDataContainer().getObject();
	// if (pObject != null) {
	// pObject.activate(true);
	// }
	// Starter.modManager.onSegmentControllerDamageTaken(this);
	// return beamHits;
	// }
	//
	//
	//
	// public int handleRepair(BeamState beam, int beamHits, BeamHandlerContainer<?> container, Vector3f to, CubeRayCastResult cubeResult, Timer timer) {
	// SegmentPiece segmentPiece = new SegmentPiece(cubeResult.getSegment(), cubeResult.getCubePos());
	//
	// int hpPlus = (int) (beamHits * beam.getPower());
	//
	// if (isOnServer() && beamHits > 0) {
	//
	// segmentPiece.getSegment().getSegmentData();
	// int infoIndex = segmentPiece.getInfoIndex();
	//
	// short hitPoints = segmentPiece.getSegment().getSegmentData().getHitpoints(infoIndex);
	// short maxHitPoints = ElementKeyMap.getInfo(segmentPiece.getType()).getMaxHitPoints();
	// try {
	// segmentPiece.getSegment().getSegmentData().setHitpoints(infoIndex, (short) Math.min(maxHitPoints, hitPoints + hpPlus));
	// } catch (SegmentDataWriteException e) {
	// SegmentDataWriteException.replaceData(segmentPiece.getSegment());
	// try {
	// segmentPiece.getSegment().getSegmentData().setHitpoints(infoIndex, (short) Math.min(maxHitPoints, hitPoints + hpPlus));
	// } catch (SegmentDataWriteException e1) {
	// e1.printStackTrace();
	// throw new RuntimeException(e1);
	// }
	// }
	// ((RemoteSegment) segmentPiece.getSegment()).setLastChanged(System.currentTimeMillis());
	// segmentPiece.refresh();
	// segmentPiece.getSegment().getSegmentController().sendBlockMod(new RemoteSegmentPiece(segmentPiece, getNetworkObject()));
	//
	// }
	// if (beamHits > 0 && !isOnServer() && ((GameClientState) getState()).getCurrentSectorEntities().containsKey(this.getId())) {
	//
	// ElementInformation elementInformation = ElementKeyMap.getInfo(segmentPiece.getType());
	// int actualPlusHp = Math.max(0, hpPlus);
	//
	// Transform t = new Transform();
	// t.setIdentity();
	// t.origin.set(cubeResult.hitPointWorld);
	// HudIndicatorOverlay.toDrawTexts.add(new RaisingIndication(t, String.valueOf(actualPlusHp), 0, 1, 0, 1));
	// }
	// Starter.modManager.onSegmentControllerHitByBeam(this);
	// return beamHits;
	//
	// }
	private void checkBreak(Vector3i absPosCache) {
		for(int i = 0; i < 6; i++) {
			Vector3i tmp = new Vector3i(absPosCache);
			tmp.add(Element.DIRECTIONSi[i]);
			// autorequest true previously
			SegmentPiece p = getSegmentBuffer().getPointUnsave(tmp);
			System.err.println("CHECKING BREAK OFF PP: " + tmp + ": " + p.getType());
			if(p.getType() != 0) {
				System.err.println("CHECKING BREAK OFF: " + p);
				((GameServerState) getState()).getController().queueSegmentControllerBreak(p);
			}
		}
	}

	private boolean isEnterable(short type) {
		return type != Element.TYPE_NONE && ElementKeyMap.getInfo(type).isEnterable();
	}

	/**
	 * @return the flagCharacterExitCheckByExplosion
	 */
	public boolean isFlagCharacterExitCheckByExplosion() {
		return flagCharacterExitCheckByExplosion;
	}

	/**
	 * @param flagCharacterExitCheckByExplosion the flagCharacterExitCheckByExplosion to set
	 */
	public void setFlagCharacterExitCheckByExplosion(boolean flagCharacterExitCheckByExplosion) {
		this.flagCharacterExitCheckByExplosion = flagCharacterExitCheckByExplosion;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.util.Collisionable#needsManifoldCollision()
	 */
	@Override
	public boolean needsManifoldCollision() {
		return getElementClassCountMap().get(ElementKeyMap.EXPLOSIVE_ID) > 0;
	}

	protected abstract void onCoreDestroyed(Damager from);

	protected void onCoreHitAlreadyDestroyed(float damage) {
	}

	public void onDamageServerRootObject(float actualDamage, Damager from) {
		this.lastDamageTaken = getState().getUpdateTime();
	}

	private void removeConfirmDialog(final SegmentPiece piece, final short type, final BuildRemoveCallback buildRemoveCallback, final boolean singleUpdate, final BuildInstruction buildInstruction, String desc) {
		PlayerGameOkCancelInput check = new PlayerGameOkCancelInput("CONFIRM", (GameClientState) getState(), Lng.str("Remove block?"), desc) {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void onDeactivate() {
				getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().hinderInteraction(400);
			}

			@Override
			public void pressedOK() {
				short ttype = removeBlock(piece, type, buildRemoveCallback, singleUpdate, buildInstruction);
				if(ttype != Element.TYPE_NONE) {
					deactivate();
				}
			}
		};
		check.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(871);
	}

	public void remove(int x, int y, int z, final BuildRemoveCallback buildRemoveCallback, final boolean singleUpdate, Set<Segment> moddedSegs, short filter, final short replaceFilterWith, int replaceFilterOrientation, BuildHelper posesFilter, final BuildInstruction buildInstruction) {
		if(posesFilter != null && !posesFilter.contains(x, y, z)) {
			return;
		}
		final SegmentPiece piece = getSegmentBuffer().getPointUnsave(x, y, z);
		if(piece != null && piece.getType() != Element.TYPE_NONE) {
			final short type = piece.getType();
			if(!(filter == Element.TYPE_ALL || type == filter)) {
				// System.err.println("[REMOVE] Cannot remove: filter not matched " + ElementKeyMap.toString(filter));
				return;
			}
			if(!buildRemoveCallback.canRemove(type)) {
				return;
			}
			boolean specialCase = false;
			String desc;
			if(ElementKeyMap.CORE_ID == type || (this instanceof SpaceStation && getTotalElements() == 1)) {
				// Core or last block
				if(piece.equalsPos(Ship.core) || this instanceof SpaceStation) {
					specialCase = true;
					if(!isOnServer()) {
						if(getTotalElements() == 1) {
							desc = (this instanceof Ship) ? Lng.str("Are you sure you want to delete the core?\n\nThis will destroy the ship,\nand you will get the core.\n") : Lng.str("Are you sure you want to delete the last block?\n\nThis will destroy the station!\n\nNo refunds!!");
							removeConfirmDialog(piece, type, buildRemoveCallback, true, buildInstruction, desc);
						} else {
							((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("Cannot delete core!\nIt must be the last\nblock remaining to delete."), 0);
						}
					}
				}
			} else if(ElementKeyMap.isInventory(type) || ElementKeyMap.isMacroFactory(type)) {
				// Inventory blocks
				SegmentController c = piece.getSegment().getSegmentController();
				if((c instanceof ManagedSegmentController) && ((((ManagedSegmentController<?>) c).getManagerContainer()) instanceof InventoryHolder)) {
					ManagerContainer<?> ih = (((ManagedSegmentController<?>) c).getManagerContainer());
					Inventory inventory = ih.getInventory(piece.getAbsoluteIndex());
					if(inventory != null && !inventory.isEmpty()) {
						specialCase = true;
						desc = Lng.str("Are you sure you want to delete %s at %s", ElementKeyMap.getInfoFast(type).getName(), piece.getPos(new Vector3b()).toString());
						removeConfirmDialog(piece, type, buildRemoveCallback, true, buildInstruction, desc);
					}
				}
			} else if(ElementKeyMap.FACTION_BLOCK == type) {
				// Faction in use
				SegmentController c = piece.getSegment().getSegmentController();
				if(c.getFactionId() != 0) {
					specialCase = true;
					desc = Lng.str("Are you sure you want to delete %s at %s", ElementKeyMap.getInfoFast(type).getName(), piece.getPos(new Vector3b()).toString());
					removeConfirmDialog(piece, type, buildRemoveCallback, true, buildInstruction, desc);
				}
			} else if(ElementKeyMap.WARP_GATE_CONTROLLER == type) {
				SegmentController c = piece.getSegment().getSegmentController();
				if(c instanceof ManagedSegmentController && ((ManagedSegmentController<?>) c).getManagerContainer() instanceof StationaryManagerContainer<?>) {
					StationaryManagerContainer<?> man = (StationaryManagerContainer<?>) ((ManagedSegmentController<?>) c).getManagerContainer();
					for(int i = 0; i < man.getWarpgate().getCollectionManagers().size(); i++) {
						WarpgateCollectionManager warpgateCollectionManager = man.getWarpgate().getCollectionManagers().get(i);
						if(warpgateCollectionManager.getControllerIndex() == piece.getAbsoluteIndex() && !warpgateCollectionManager.getWarpDestinationUID().equals("none")) {
							// Found warpgate and has destination set
							specialCase = true;
							desc = Lng.str("Are you sure you want to delete %s at %s", ElementKeyMap.getInfoFast(type).getName(), piece.getPos(new Vector3b()).toString());
							removeConfirmDialog(piece, type, buildRemoveCallback, true, buildInstruction, desc);
						}
					}
				}
			}
			if(!specialCase) {
				final byte recOrientationBef = piece.getOrientation();
				final boolean recActBef = piece.isActive();
				byte orientationBef = recOrientationBef;
				boolean actBef = recActBef;
				if(ElementKeyMap.isValidType(replaceFilterWith) && allowedType(replaceFilterWith) && checkAllPlace(replaceFilterWith, 1, null) > 0) {
					int previousAdds = buildInstruction.getAdds().size();
					int previousRemoves = buildInstruction.getRemoves().size();
					removeBlock(piece, type, buildRemoveCallback, singleUpdate, buildInstruction);
					moddedSegs.add(piece.getSegment());
					ElementInformation r = ElementKeyMap.getInfo(replaceFilterWith);
					ElementInformation v = ElementKeyMap.getInfo(type);
					// use mirrored orientations when replacing
					ElementInformation info = ElementKeyMap.getInfo(replaceFilterWith);
					BlockOrientation o = ElementInformation.convertOrientation(info, (byte) replaceFilterOrientation);
					if(r.orientatable != v.orientatable || r.getIndividualSides() != v.getIndividualSides() || r.getBlockStyle() != v.getBlockStyle() || r.resourceInjection != ResourceInjectionType.OFF) {
						// System.err.println("[CLIENT] REPLACING WITH DEFAULT ORIENTATION; orientatable: " + r.orientatable + "; sidesEqual: "
						// + (r.getIndividualSides() == v.getIndividualSides())
						// + "; styleEqual: " + (r.getBlockStyle() == v.getBlockStyle()));
						orientationBef = o.blockOrientation;
						actBef = o.activateBlock;
					}
					if(replaceFilterWith == ElementKeyMap.CARGO_SPACE) {
						// always replace cargo space with 0 orientation (because its at a random orientation depending on its state)
						orientationBef = 0;
						actBef = false;
					}
					// System.err.println("[CLIENT][SEGMENTCONTROLLER] Using Replace filter: replace: " + ElementKeyMap.toString(filter) + " with " + ElementKeyMap.toString(replaceFilterWith)+"; Orientation of replace: "+orientationBef);
					if(!r.canActivate() && r.getBlockStyle() == BlockStyle.NORMAL) {
						actBef = o.activateBlock;
					}
					build(x, y, z, replaceFilterWith, orientationBef, actBef, buildRemoveCallback, new Vector3i(), new int[]{0, 1}, posesFilter, buildInstruction);
					// Remove any adds/removes added by the previous statements
					while(buildInstruction.getAdds().size() > previousAdds) {
						buildInstruction.getAdds().removeQuick(buildInstruction.getAdds().size() - 1);
					}
					while(buildInstruction.getRemoves().size() > previousRemoves) {
						buildInstruction.getRemoves().removeQuick(buildInstruction.getRemoves().size() - 1);
					}
					// Add a replace instruction instead of individual adds/removes
					buildInstruction.recordReplace(ElementCollection.getIndex(x, y, z), type, replaceFilterWith, recOrientationBef, recActBef);
				} else if((filter == Element.TYPE_ALL || filter == piece.getType()) && (!ElementKeyMap.isValidType(replaceFilterWith) || allowedType(replaceFilterWith))) {
					removeBlock(piece, type, buildRemoveCallback, singleUpdate, buildInstruction);
					moddedSegs.add(piece.getSegment());
				}
			}
		} else {
			// System.err.println("[WARN]SHIP][NO-INTERSECTION] no intersection found in "+this+"("+getState()+") ");
		}
	}

	public void removeInSymmetry(int x, int y, int z, BuildRemoveCallback buildRemoveCallback, boolean singleUpdate, Set<Segment> moddedSegs, short filter, short replaceFilterWith, int selOrientation, BuildHelper posesFilter, BuildInstruction buildInstruction, SymmetryPlanes symmetryPlanes) {
		if(symmetryPlanes.isXyPlaneEnabled() && !symmetryPlanes.isXzPlaneEnabled() && !symmetryPlanes.isYzPlaneEnabled()) {
			// XY
			int planePosXY = symmetryPlanes.getXyPlane().z;
			int distXY = (planePosXY - z) * 2 + symmetryPlanes.getXyExtraDist();
			remove(x, y, z + distXY, buildRemoveCallback, singleUpdate, moddedSegs, filter, replaceFilterWith, symmetryPlanes.getMirrorOrientation(replaceFilterWith, selOrientation, true, false, false), posesFilter, buildInstruction);
		} else if(!symmetryPlanes.isXyPlaneEnabled() && symmetryPlanes.isXzPlaneEnabled() && !symmetryPlanes.isYzPlaneEnabled()) {
			// XZ
			int planePosXZ = symmetryPlanes.getXzPlane().y;
			int distXZ = (planePosXZ - y) * 2 + symmetryPlanes.getXzExtraDist();
			remove(x, y + distXZ, z, buildRemoveCallback, singleUpdate, moddedSegs, filter, replaceFilterWith, symmetryPlanes.getMirrorOrientation(replaceFilterWith, selOrientation, false, true, false), posesFilter, buildInstruction);
		} else if(!symmetryPlanes.isXyPlaneEnabled() && !symmetryPlanes.isXzPlaneEnabled() && symmetryPlanes.isYzPlaneEnabled()) {
			// YZ
			int planePosYZ = symmetryPlanes.getYzPlane().x;
			int distYZ = (planePosYZ - x) * 2 + symmetryPlanes.getYzExtraDist();
			remove(x + distYZ, y, z, buildRemoveCallback, singleUpdate, moddedSegs, filter, replaceFilterWith, symmetryPlanes.getMirrorOrientation(replaceFilterWith, selOrientation, false, false, true), posesFilter, buildInstruction);
		} else if(symmetryPlanes.isXyPlaneEnabled() && symmetryPlanes.isXzPlaneEnabled() && !symmetryPlanes.isYzPlaneEnabled()) {
			// XY XZ
			int planePosXY = symmetryPlanes.getXyPlane().z;
			int planePosXZ = symmetryPlanes.getXzPlane().y;
			int distXY = (planePosXY - z) * 2 + symmetryPlanes.getXyExtraDist();
			int distXZ = (planePosXZ - y) * 2 + symmetryPlanes.getXzExtraDist();
			remove(x, y, z + distXY, buildRemoveCallback, singleUpdate, moddedSegs, filter, replaceFilterWith, symmetryPlanes.getMirrorOrientation(replaceFilterWith, selOrientation, true, false, false), posesFilter, buildInstruction);
			remove(x, y + distXZ, z, buildRemoveCallback, singleUpdate, moddedSegs, filter, replaceFilterWith, symmetryPlanes.getMirrorOrientation(replaceFilterWith, selOrientation, false, true, false), posesFilter, buildInstruction);
			remove(x, y + distXZ, z + distXY, buildRemoveCallback, singleUpdate, moddedSegs, filter, replaceFilterWith, symmetryPlanes.getMirrorOrientation(replaceFilterWith, selOrientation, true, true, false), posesFilter, buildInstruction);
		} else if(symmetryPlanes.isXyPlaneEnabled() && !symmetryPlanes.isXzPlaneEnabled() && symmetryPlanes.isYzPlaneEnabled()) {
			// XY YZ
			int planePosXY = symmetryPlanes.getXyPlane().z;
			int planePosYZ = symmetryPlanes.getYzPlane().x;
			int distXY = (planePosXY - z) * 2 + symmetryPlanes.getXyExtraDist();
			int distYZ = (planePosYZ - x) * 2 + symmetryPlanes.getYzExtraDist();
			remove(x, y, z + distXY, buildRemoveCallback, singleUpdate, moddedSegs, filter, replaceFilterWith, symmetryPlanes.getMirrorOrientation(replaceFilterWith, selOrientation, true, false, false), posesFilter, buildInstruction);
			remove(x + distYZ, y, z, buildRemoveCallback, singleUpdate, moddedSegs, filter, replaceFilterWith, symmetryPlanes.getMirrorOrientation(replaceFilterWith, selOrientation, false, false, true), posesFilter, buildInstruction);
			remove(x + distYZ, y, z + distXY, buildRemoveCallback, singleUpdate, moddedSegs, filter, replaceFilterWith, symmetryPlanes.getMirrorOrientation(replaceFilterWith, selOrientation, true, false, true), posesFilter, buildInstruction);
		} else if(!symmetryPlanes.isXyPlaneEnabled() && symmetryPlanes.isXzPlaneEnabled() && symmetryPlanes.isYzPlaneEnabled()) {
			// XZ YZ
			int planePosXZ = symmetryPlanes.getXzPlane().y;
			int planePosYZ = symmetryPlanes.getYzPlane().x;
			int distXZ = (planePosXZ - y) * 2 + symmetryPlanes.getXzExtraDist();
			int distYZ = (planePosYZ - x) * 2 + symmetryPlanes.getYzExtraDist();
			remove(x, y + distXZ, z, buildRemoveCallback, singleUpdate, moddedSegs, filter, replaceFilterWith, symmetryPlanes.getMirrorOrientation(replaceFilterWith, selOrientation, false, true, false), posesFilter, buildInstruction);
			remove(x + distYZ, y, z, buildRemoveCallback, singleUpdate, moddedSegs, filter, replaceFilterWith, symmetryPlanes.getMirrorOrientation(replaceFilterWith, selOrientation, false, false, true), posesFilter, buildInstruction);
			remove(x + distYZ, y + distXZ, z, buildRemoveCallback, singleUpdate, moddedSegs, filter, replaceFilterWith, symmetryPlanes.getMirrorOrientation(replaceFilterWith, selOrientation, false, true, true), posesFilter, buildInstruction);
		} else if(symmetryPlanes.isXyPlaneEnabled() && symmetryPlanes.isXzPlaneEnabled() && symmetryPlanes.isYzPlaneEnabled()) {
			// ALL
			int planePosXY = symmetryPlanes.getXyPlane().z;
			int planePosXZ = symmetryPlanes.getXzPlane().y;
			int planePosYZ = symmetryPlanes.getYzPlane().x;
			int distXY = (planePosXY - z) * 2 + symmetryPlanes.getXyExtraDist();
			int distXZ = (planePosXZ - y) * 2 + symmetryPlanes.getXzExtraDist();
			int distYZ = (planePosYZ - x) * 2 + symmetryPlanes.getYzExtraDist();
			remove(x + distYZ, y, z, buildRemoveCallback, singleUpdate, moddedSegs, filter, replaceFilterWith, symmetryPlanes.getMirrorOrientation(replaceFilterWith, selOrientation, false, false, true), posesFilter, buildInstruction);
			remove(x, y + distXZ, z, buildRemoveCallback, singleUpdate, moddedSegs, filter, replaceFilterWith, symmetryPlanes.getMirrorOrientation(replaceFilterWith, selOrientation, false, true, false), posesFilter, buildInstruction);
			remove(x, y, z + distXY, buildRemoveCallback, singleUpdate, moddedSegs, filter, replaceFilterWith, symmetryPlanes.getMirrorOrientation(replaceFilterWith, selOrientation, true, false, false), posesFilter, buildInstruction);
			remove(x + distYZ, y + distXZ, z, buildRemoveCallback, singleUpdate, moddedSegs, filter, replaceFilterWith, symmetryPlanes.getMirrorOrientation(replaceFilterWith, selOrientation, false, true, true), posesFilter, buildInstruction);
			remove(x + distYZ, y, z + distXY, buildRemoveCallback, singleUpdate, moddedSegs, filter, replaceFilterWith, symmetryPlanes.getMirrorOrientation(replaceFilterWith, selOrientation, true, false, true), posesFilter, buildInstruction);
			remove(x, y + distXZ, z + distXY, buildRemoveCallback, singleUpdate, moddedSegs, filter, replaceFilterWith, symmetryPlanes.getMirrorOrientation(replaceFilterWith, selOrientation, true, true, false), posesFilter, buildInstruction);
			remove(x + distYZ, y + distXZ, z + distXY, buildRemoveCallback, singleUpdate, moddedSegs, filter, replaceFilterWith, symmetryPlanes.getMirrorOrientation(replaceFilterWith, selOrientation, true, true, true), posesFilter, buildInstruction);
		}
	}

	public void redo(BuildInstruction s, BuildInstruction n) {
		ObjectOpenHashSet<Segment> moddedSegs = new ObjectOpenHashSet<Segment>();
		boolean singleUpdate = (s.getAdds().size() == 1 && s.getRemoves().isEmpty()) || (s.getRemoves().size() == 1 && s.getAdds().isEmpty());
		SegmentPiece pieceTmp = new SegmentPiece();
		for(final BuildInstruction.Replace r : s.getReplaces()) {
			SegmentPiece piece = getSegmentBuffer().getPointUnsave(r.where, pieceTmp);
			if(piece != null) {
				// First remove
				int previousAdds = n.getAdds().size();
				if(s.fillTool != null) {
					s.fillTool.redo(piece.getAbsoluteIndex());
				}
				boolean removeElement = piece.getSegment().removeElement(piece.x, piece.y, piece.z, singleUpdate);
				moddedSegs.add(piece.getSegment());
				if(removeElement) {
					this.lastEditBlocks = System.currentTimeMillis();
					((RemoteSegment) piece.getSegment()).setLastChanged(System.currentTimeMillis());
				}
				piece.refresh();
				sendBlockMod(new RemoteSegmentPiece(new SegmentPiece(piece), getNetworkObject()));
				// Now replace
				// doen't matter since we have no callback
				Vector3i absOnOut = new Vector3i();
				BuildCallback b = new BuildCallback() {

					@Override
					public void onBuild(Vector3i posBuilt, Vector3i posNextToBuild, short type) {
					}

					@Override
					public long getSelectedControllerPos() {
						return Long.MIN_VALUE;
					}
				};
				int x = ElementCollection.getPosX(r.where);
				int y = ElementCollection.getPosY(r.where);
				int z = ElementCollection.getPosZ(r.where);
				build(x, y, z, r.to, r.prevOrientation, r.prevIsActive, b, absOnOut, new int[]{0, 1}, null, n);
				while(n.getAdds().size() > previousAdds) {
					n.getAdds().removeQuick(n.getAdds().size() - 1);
				}
				n.recordReplace(r.where, r.from, r.to, r.prevOrientation, r.prevIsActive);
				// piece.getOrientation(), piece.isActive());
			}
		}
		for(final Remove r : s.getRemoves()) {
			SegmentPiece p = r.where;
			n.recordRemove(p);
			boolean removeElement = p.getSegment().removeElement(p.x, p.y, p.z, singleUpdate);
			moddedSegs.add(p.getSegment());
			if(removeElement) {
				this.lastEditBlocks = getState().getUpdateTime();
				((RemoteSegment) p.getSegment()).setLastChanged(getState().getUpdateTime());
			}
			sendBlockMod(new RemoteSegmentPiece(p, getNetworkObject()));
			// System.err.println("[REDO] removing "+piece);
		}
		int notDone = 0;
		for(final Add r : s.getAdds()) {
			long pos = r.where;
			SegmentPiece piece = getSegmentBuffer().getPointUnsave(r.where, pieceTmp);
			if(piece == null || piece.getType() == 0) {
				VoidSegmentPiece p = new VoidSegmentPiece();
				p.voidPos = ElementCollection.getPosFromIndex(pos, new Vector3i());
				p.setType(r.type);
				p.setOrientation((byte) r.elementOrientation);
				p.setActive(r.activateBlock);
				SegmentCollisionCheckerCallback cb = new SegmentCollisionCheckerCallback();
				if(getCollisionChecker().checkPieceCollision(p, cb, false)) {
					notDone++;
				}
			}
		}
		if(notDone == 0) {
			for(final Add a : s.getAdds()) {
				// doen't matter since we have no callback
				Vector3i absOnOut = new Vector3i();
				BuildCallback callback = new BuildCallback() {

					@Override
					public void onBuild(Vector3i posBuilt, Vector3i posNextToBuild, short type) {
					}

					@Override
					public long getSelectedControllerPos() {
						return a.selectedController;
					}
				};
				int x = ElementCollection.getPosX(a.where);
				int y = ElementCollection.getPosY(a.where);
				int z = ElementCollection.getPosZ(a.where);
				build(x, y, z, a.type, a.elementOrientation, a.activateBlock, callback, absOnOut, new int[]{0, 1}, null, n);
			}
		} else {
			((GameClientController) getState().getController()).popupAlertTextMessage(Lng.str("%s block placements blocked by another object.\nUndo not executed to avoid possible exploitation.", notDone), "BLOCKED_POS", 0);
		}
		for(Segment seg : moddedSegs) {
			if(!seg.isEmpty()) {
				seg.getSegmentData().restructBB(true);
			} else {
				getSegmentBuffer().restructBB();
			}
		}
	}

	public void undo(BuildInstruction s, BuildInstruction n) {
		ObjectOpenHashSet<Segment> moddedSegs = new ObjectOpenHashSet<Segment>();
		boolean singleUpdate = (s.getAdds().size() == 1 && s.getRemoves().isEmpty()) || (s.getRemoves().size() == 1 && s.getAdds().isEmpty());
		SegmentPiece pieceTmp = new SegmentPiece();
		int addSize = s.getAdds().size();
		boolean invFull = false;
		for(final BuildInstruction.Replace r : s.getReplaces()) {
			if(!((GameClientState) getState()).getPlayer().getInventory().canPutIn(r.to, 1)) {
				invFull = true;
				continue;
			}
			SegmentPiece piece = getSegmentBuffer().getPointUnsave(r.where, pieceTmp);
			if(piece != null) {
				// First remove
				if(s.fillTool != null) {
					s.fillTool.undo(piece.getAbsoluteIndex());
				}
				int previousAdds = n.getAdds().size();
				boolean removeElement = piece.getSegment().removeElement(piece.x, piece.y, piece.z, singleUpdate);
				moddedSegs.add(piece.getSegment());
				if(removeElement) {
					this.lastEditBlocks = System.currentTimeMillis();
					((RemoteSegment) piece.getSegment()).setLastChanged(System.currentTimeMillis());
				}
				piece.refresh();
				sendBlockMod(new RemoteSegmentPiece(new SegmentPiece(piece), getNetworkObject()));
				// Now replace
				// doen't matter since we have no callback
				Vector3i absOnOut = new Vector3i();
				BuildCallback b = new BuildCallback() {

					@Override
					public void onBuild(Vector3i posBuilt, Vector3i posNextToBuild, short type) {
					}

					@Override
					public long getSelectedControllerPos() {
						return Long.MIN_VALUE;
					}
				};
				int x = ElementCollection.getPosX(r.where);
				int y = ElementCollection.getPosY(r.where);
				int z = ElementCollection.getPosZ(r.where);
				build(x, y, z, r.from, r.prevOrientation, r.prevIsActive, b, absOnOut, new int[]{0, 1}, null, n);
				piece.refresh();
				while(n.getAdds().size() > previousAdds) {
					n.getAdds().removeQuick(n.getAdds().size() - 1);
				}
				n.recordReplace(r.where, r.to, r.from, piece.getOrientation(), piece.isActive());
			}
		}
		for(int i = 0; i < addSize; i++) {
			Add a = s.getAdds().get(i);
			SegmentPiece piece = getSegmentBuffer().getPointUnsave(a.where, pieceTmp);
			if(piece != null) {
				if(!((GameClientState) getState()).getPlayer().getInventory().canPutIn(a.type, 1)) {
					invFull = true;
					continue;
				}
				// System.err.println("UNDOOOOOO "+piece+"; "+n.fillTool);
				if(s.fillTool != null) {
					s.fillTool.undo(piece.getAbsoluteIndex());
				}
				n.recordRemove(new SegmentPiece(piece));
				boolean removeElement = piece.getSegment().removeElement(piece.x, piece.y, piece.z, singleUpdate);
				moddedSegs.add(piece.getSegment());
				if(removeElement) {
					this.lastEditBlocks = System.currentTimeMillis();
					((RemoteSegment) piece.getSegment()).setLastChanged(System.currentTimeMillis());
				}
				piece.refresh();
				sendBlockMod(new RemoteSegmentPiece(new SegmentPiece(piece), getNetworkObject()));
			}
			// System.err.println("[UNDO] removing "+piece);
		}
		if(invFull) {
			((GameClientController) getState().getController()).popupAlertTextMessage(Lng.str("Can't undo!\nInventory full."), 0);
		}
		int notDone = 0;
		for(final Remove r : s.getRemoves()) {
			SegmentPiece p = r.where;
			SegmentCollisionCheckerCallback cb = new SegmentCollisionCheckerCallback();
			if(getCollisionChecker().checkPieceCollision(p, cb, false)) {
				notDone++;
			}
		}
		if(notDone == 0) {
			for(final Remove r : s.getRemoves()) {
				// doen't matter since we have no callback
				Vector3i absOnOut = new Vector3i();
				BuildCallback b = new BuildCallback() {

					@Override
					public void onBuild(Vector3i posBuilt, Vector3i posNextToBuild, short type) {
					}

					@Override
					public long getSelectedControllerPos() {
						return r.controller;
					}
				};
				final SegmentPiece p = r.where;
				if(p.getType() == 0) {
					// Prevent crash caused from too many undo/redo calls too quick
					continue;
				}
				Vector3i pos = p.getAbsolutePos(new Vector3i());
				build(pos.x, pos.y, pos.z, p.getType(), p.getOrientation(), p.isActive(), b, absOnOut, new int[]{0, 1}, null, n);
				if(r.connectedFromThis != null) {
					for(long l : r.connectedFromThis) {
						getControlElementMap().removeControlledFromAll(ElementCollection.getPosIndexFrom4(l), (short) ElementCollection.getType(l), true);
						getControlElementMap().addControllerForElement(p.getAbsoluteIndex(), ElementCollection.getPosIndexFrom4(l), (short) ElementCollection.getType(l));
					}
				}
			}
		} else {
			((GameClientController) getState().getController()).popupAlertTextMessage(Lng.str("%s block placements blocked by another object.\nUndo not executed to avoid possible exploitation.", notDone), "BLOCKED_POS", 0);
		}
		for(Segment seg : moddedSegs) {
			if(!seg.isEmpty()) {
				seg.getSegmentData().restructBB(true);
			} else {
				getSegmentBuffer().restructBB();
			}
		}
	}

	private short removeBlock(SegmentPiece piece, short type, BuildRemoveCallback buildRemoveCallback, boolean singleUpdate, BuildInstruction buildInstruction) {
		buildInstruction.recordRemove(piece);
		boolean removeElement = piece.getSegment().removeElement(piece.x, piece.y, piece.z, singleUpdate);
		this.lastEditBlocks = System.currentTimeMillis();
		((RemoteSegment) piece.getSegment()).setLastChanged(System.currentTimeMillis());
		piece.refresh();
		sendBlockMod(new RemoteSegmentPiece(piece, getNetworkObject()));
		buildRemoveCallback.onRemove(piece.getAbsoluteIndex(), type);
		// System.err.println("REST REM: "+(System.currentTimeMillis() -t));
		return type;
	}

	@Override
	public String toString() {
		return getSegmentControllerTypeString() + "(" + getId() + ")";
	}

	int uNumMagnetDock = 0;

	@Override
	public boolean handleCollision(int index, RigidBody originalBody, RigidBody originalBody2, SolverConstraint contactConstraint) {
		Sendable secSend = getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(getSectorId());
		if(secSend != null && secSend instanceof RemoteSector) {
			RemoteSector s = (RemoteSector) secSend;
			// should work on client as well as server
			if(s.isProtectedClient()) {
				return false;
			}
		} else {
			return false;
		}
		ManifoldPoint m = (ManifoldPoint) contactConstraint.originalContactPoint;
		Sendable sendable = getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(m.starMadeIdA);
		if(sendable != null && sendable.getId() != this.getId() && sendable instanceof EditableSendableSegmentController && ((EditableSendableSegmentController) sendable).railController.isInAnyRailRelationWith(this)) {
			// collision handled by sub object (docked stuff)
			return ((EditableSendableSegmentController) sendable).handleCollision(index, originalBody, originalBody2, contactConstraint);
		}
		if(!isOnServer() && uNumMagnetDock != getState().getNumberOfUpdate() && (ElementKeyMap.isValidType(m.starMadeTypeA) && ElementKeyMap.isValidType(m.starMadeTypeB)) && ((ElementKeyMap.getInfoFast(m.starMadeTypeA).isRailDockable() && ElementKeyMap.getInfoFast(m.starMadeTypeB).isRailDocker()) || (ElementKeyMap.getInfoFast(m.starMadeTypeB).isRailDockable() && ElementKeyMap.getInfoFast(m.starMadeTypeA).isRailDocker()))) {
			SegmentController a = (SegmentController) getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(m.starMadeIdA);
			SegmentController b = (SegmentController) getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(m.starMadeIdB);
			if(a == null || b == null) {
				return false;
			}
			SegmentPiece aP = a.getSegmentBuffer().getPointUnsave(m.starMadeXA + SegmentData.SEG_HALF, m.starMadeYA + SegmentData.SEG_HALF, m.starMadeZA + SegmentData.SEG_HALF);
			SegmentPiece bP = b.getSegmentBuffer().getPointUnsave(m.starMadeXB + SegmentData.SEG_HALF, m.starMadeYB + SegmentData.SEG_HALF, m.starMadeZB + SegmentData.SEG_HALF);
			if(aP != null && bP != null) {
				assert (aP.getType() == m.starMadeTypeA && bP.getType() == m.starMadeTypeB) : aP + "; " + m.starMadeTypeA + " ::: " + bP + "; " + m.starMadeTypeB;
				if(!a.railController.isDockedOrDirty() && ElementKeyMap.getInfoFast(m.starMadeTypeA).isRailDocker()) {
					DockingFailReason r = new DockingFailReason();
					if(a.railController.isOkToDockClientCheck(aP, bP, r)) {
						a.railController.connectClient(aP, bP);
					} else {
						r.popupClient(a);
					}
				} else if(!b.railController.isDockedOrDirty()) {
					DockingFailReason r = new DockingFailReason();
					if(b.railController.isOkToDockClientCheck(bP, aP, r)) {
						b.railController.connectClient(bP, aP);
					} else {
						r.popupClient(b);
					}
				}
				uNumMagnetDock = getState().getNumberOfUpdate();
			}
		}
		if(isOnServer() && (m.starMadeTypeA == ElementKeyMap.EXPLOSIVE_ID || m.starMadeTypeB == ElementKeyMap.EXPLOSIVE_ID)) {
			ExplosiveManagerContainerInterface exp = null;
			Sendable collider;
			short type;
			if(index == 0) {
				local.set(m.starMadeXA, m.starMadeYA, m.starMadeZA);
				type = m.starMadeTypeA;
				// ((RigidBodyExt)originalBody2).getSegmentController();
				collider = getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(m.starMadeIdB);
				exp = ((ExplosiveManagerContainerInterface) ((ManagedSegmentController<?>) this).getManagerContainer());
			} else {
				local.set(m.starMadeXB, m.starMadeYB, m.starMadeZB);
				type = m.starMadeTypeB;
				collider = getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(m.starMadeIdB);
				if(collider instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) collider).getManagerContainer() instanceof ExplosiveManagerContainerInterface) {
					exp = ((ExplosiveManagerContainerInterface) ((ManagedSegmentController<?>) collider).getManagerContainer());
				}
			}
			if(type == ElementKeyMap.EXPLOSIVE_ID && collider != null && collider instanceof EditableSendableSegmentController && !exp.getExplosiveCollectionManager().getElementCollections().isEmpty()) {
				local.x += SegmentData.SEG_HALF;
				local.y += SegmentData.SEG_HALF;
				local.z += SegmentData.SEG_HALF;
				m.getPositionWorldOnB(tmpPosA);
				getWorldTransformInverse().transform(tmpPosA);
				// System.err.println("EXPLOSIVE HIT FROM "+this+" TO "+collider);
				exp.getExplosiveElementManager().addExplosion(local, tmpPosA);
			}
		}
		// System.err.println(getState()+" CHECKING COLLISION "+ServerConfig.COLLISION_DAMAGE.isOn()+"; "+isInGodmode()+"; "+getMass());
		if(isOnServer() && getMass() > 0 && ServerConfig.COLLISION_DAMAGE.isOn()) {
			// System.err.println("DOING INDEX "+index+"; "+contactConstraint.appliedImpulse+" / "+ServerConfig.COLLISION_DAMAGE_THRESHOLD.getFloat());
			// float max = Math.max(originalBody.getLinearVelocity(v.tmpVec3a).length(), originalBody2.getLinearVelocity(v.tmpVec3b).length());
			if(contactConstraint.appliedImpulse > ServerConfig.COLLISION_DAMAGE_THRESHOLD.getFloat()) {
				// System.err.println("[SERVER][DEBUG] COLLISION DAMAGE: "+this+": impulse " + contactConstraint.appliedImpulse + "; unum " + PhysicsExt.UPDATE_NUM+"; push: "+contactConstraint.appliedPushImpulse+"; pen "+contactConstraint.penetration+"; res "+contactConstraint.restitution);
			} else {
				return false;
			}
			if(isInGodmode() == null) {
				// if(max < 20){
				// return false;
				// }
				Vector3i local = v.local;
				if(index == 0) {
					local.set(m.starMadeXA, m.starMadeYA, m.starMadeZA);
				} else {
					local.set(m.starMadeXB, m.starMadeYB, m.starMadeZB);
				}
				local.x += SegmentData.SEG_HALF;
				local.y += SegmentData.SEG_HALF;
				local.z += SegmentData.SEG_HALF;
				SegmentPiece segmentPiece = getSegmentBuffer().getPointUnsave(local, v.tmpPiece);
				if(segmentPiece != null && segmentPiece.isValid() && segmentPiece.isAlive()) {
					boolean coreDestroyed = false;
					int infoIndex = SegmentData.getInfoIndex(segmentPiece.x, segmentPiece.y, segmentPiece.z);
					SegmentData segmentData = segmentPiece.getSegment().getSegmentData();
					short type = segmentPiece.getType();
					int damage = 600;
					float actualDamage = damageElement(type, infoIndex, segmentData, damage, null, DamageDealerType.GENERAL, Long.MIN_VALUE);
					System.err.println("[COLLISION DAMAGE] " + getState() + "; " + this + "; " + segmentPiece + " damaged: " + actualDamage);
					boolean removeElement = false;
					if(segmentData.getHitpointsByte(infoIndex) <= 0) {
						if(type == getCoreType() && segmentPiece.getAbsolutePos(absPosCache).equals(Ship.core)) {
							try {
								segmentData.setHitpointsByte(infoIndex, (short) 0);
							} catch(SegmentDataWriteException e) {
								segmentData = SegmentDataWriteException.replaceData(segmentData.getSegment());
								try {
									segmentData.setHitpointsByte(infoIndex, (short) 0);
								} catch(SegmentDataWriteException e1) {
									e1.printStackTrace();
									throw new RuntimeException(e1);
								}
							}
							onCoreDestroyed(null);
							coreDestroyed = true;
							onCoreHitAlreadyDestroyed(damage);
							segmentPiece.refresh();
							sendBlockMod(new RemoteSegmentPiece(new SegmentPiece(segmentPiece), getNetworkObject()));
							onBlockDamage(segmentPiece.getAbsoluteIndex(), type, damage, DamageDealerType.GENERAL, null);
						} else {
							removeElement = killBlock(segmentPiece);
						}
						if(ServerConfig.ENABLE_BREAK_OFF.isOn()) {
							segmentPiece.getAbsolutePos(absPosCache);
							checkBreak(absPosCache);
						}
						if(isEnterable(type)) {
							forceCharacterExit(segmentPiece);
						}
					} else {
						segmentPiece.refresh();
						sendBlockMod(new RemoteSegmentPiece(new SegmentPiece(segmentPiece), getNetworkObject()));
						onBlockDamage(segmentPiece.getAbsoluteIndex(), type, damage, DamageDealerType.GENERAL, null);
					}
					((RemoteSegment) segmentPiece.getSegment()).setLastChanged(System.currentTimeMillis());
				} else {
					// block already gone?
					// System.err.println("BLOCK GONE");
				}
				return true;
			}
		}
		return false;
	}

	public boolean killBlock(SegmentPiece segmentPiece) {
		//INSERTED CODE @1251
		if(StarLoader.hasListeners(SegmentPieceKillEvent.class)) {
			SegmentPieceKillEvent event = new SegmentPieceKillEvent(segmentPiece, this, null);
			StarLoader.fireEvent(SegmentPieceKillEvent.class, event, this.isOnServer());
			if(event.isCanceled()) return false;
		}
		///

		boolean removeElement = segmentPiece.getSegment().removeElement(segmentPiece.x, segmentPiece.y, segmentPiece.z, true);
		onBlockKill(segmentPiece, null);
		segmentPiece.refresh();
		sendBlockKill(segmentPiece);
		return removeElement;
	}

	public AcidDamageManager getAcidDamageManagerServer() {
		return acidDamageManagerServer;
	}

	public boolean isExtraAcidDamageOnDecoBlocks() {
		return false;
	}
}
