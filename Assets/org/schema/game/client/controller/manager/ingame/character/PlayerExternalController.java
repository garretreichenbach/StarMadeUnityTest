package org.schema.game.client.controller.manager.ingame.character;

import api.listener.events.block.ClientSelectSegmentPieceEvent;
import api.mod.StarLoader;
import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;
import com.bulletphysics.linearmath.Transform;
import org.schema.common.FastMath;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.GameClientController;
import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.controller.manager.ingame.*;
import org.schema.game.client.controller.tutorial.states.ConnectedFromToTestState;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.client.view.camera.PlayerCamera;
import org.schema.game.client.view.cubes.occlusion.Occlusion;
import org.schema.game.client.view.gui.buildtools.GUIOrientationSettingElement;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.CubeRayCastResult;
import org.schema.game.common.data.physics.PhysicsExt;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.common.data.player.InteractionInterface;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.SimplePlayerCommands;
import org.schema.game.common.data.player.inventory.InventorySlot;
import org.schema.game.common.data.world.GameTransformable;
import org.schema.game.network.commands.gamerequests.EntityRequest;
import org.schema.game.network.objects.InterconnectStructureRequest;
import org.schema.game.network.objects.remote.RemoteInterconnectStructure;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.sound.controller.AudioController;
import org.schema.schine.sound.controller.MusicTags;

import javax.vecmath.Vector3f;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class PlayerExternalController extends AbstractBuildControlManager {

	public static final float EDIT_DISTANCE = 6;

	private final SymmetryPlanes symmetryPlanes = new SymmetryPlanes();

	private final long suckProtectMillisDuration = 8000;

	private final SegmentPiece tmpPiece = new SegmentPiece();

	String description = Lng.str("To create a new ship/station, please enter a name of your choice.\nIt can be changed later with a faction module.");

	private Camera followerCamera;

	private SegmentPiece selectedBlock;

	private boolean controllersDirty;

	private String last;

	private Vector3i posM = new Vector3i();

	private Vector3i toSelectBox;

	private SegmentController toSelectSegController;

	private long toSelectTime;

	public PlayerExternalController(GameClientState state) {
		super(state);
		initialize();
	}

	public boolean canEnter(short type) {
		return ElementKeyMap.getInfo(type).isEnterable();
	}

	public void sit() {
		if (getState().getPlayer().isSitting()) {
			System.err.println("[CLIENT][SIT] standing up");
			getState().getPlayer().sendSimpleCommand(SimplePlayerCommands.SIT_DOWN, -1, 0L, 0L, 0L);
		} else {
			getState().getCharacter().sitDown(getNearestIntersection(), getState().getCharacter().getHeadWorldTransform().origin, Controller.getCamera().getForward(), PlayerExternalController.EDIT_DISTANCE);
		}
	}

	public void checkAddAndRemove(boolean add) throws ElementPositionBlockedException {
		// if(!add){
		// return;
		// }
		PlayerInteractionControlManager inter = getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
		short selectedType = 0;
		InventorySlot slot = getState().getPlayer().getInventory().getSlot(inter.getSelectedSlot());
		selectedType = inter.getSelectedTypeWithSub();
		System.err.println("[CLIENT][ASTRONAUTMODE] Selected Type: " + ElementKeyMap.toString(selectedType) + "; Slot: " + (slot != null ? slot.getType() : "NULL"));
		if (add && slot != null && inter.checkRadialSelect(slot.getType())) {
			return;
		}
		if (slot == null || (selectedType < 0 && selectedType != InventorySlot.MULTI_SLOT)) {
			// dont check when meta item is selected
			return;
		}
		if (add && selectedType == ElementKeyMap.CORE_ID) {
			spawnShip();
			return;
		}
		ClosestRayResultCallback nearestIntersection = getNearestIntersection();
		if (nearestIntersection != null && nearestIntersection.hasHit() && nearestIntersection instanceof CubeRayCastResult) {
			final CubeRayCastResult c = (CubeRayCastResult) nearestIntersection;
			if (c.getSegment() != null && c.getSegment().getSegmentController() instanceof EditableSendableSegmentController) {
				c.getSegment().getSegmentController().getControlElementMap().setObs(this);
				if (c.getSegment().getSegmentController().isVirtualBlueprint()) {
					getState().getController().popupAlertTextMessage(Lng.str("Can only edit design in build mode!"), 0);
					return;
				}
				// DebugBox b = new DebugBox(
				// new Vector3f(c.segment.pos.x +c.cubePos.x-8-0.51f, c.segment.pos.y+c.cubePos.y-8-0.51f,c.segment.pos.z+c.cubePos.z-8-0.51f),
				// new Vector3f(c.segment.pos.x+c.cubePos.x+1-8-0.49f, c.segment.pos.y+c.cubePos.y+1-8-0.49f, c.segment.pos.z+c.cubePos.z+1-8-0.49f),
				// c.segment.getSegmentController().getWorldTransform(),
				// 0, 1, 0, 1);
				// DebugDrawer.boxes.add(b);
				Vector3f pos = new Vector3f(getState().getCharacter().getHeadWorldTransform().origin);
				Vector3f forw = new Vector3f(Controller.getCamera().getForward());
				if (add) {
					if (getState().getPlayer().isInTutorial() && ElementKeyMap.isValidType(selectedType) && !ElementKeyMap.getInfo(selectedType).isRailTrack() && ElementKeyMap.getInfo(selectedType).getControlling().size() > 0 && c.getSegment().getSegmentController() instanceof Ship) {
						getState().getController().popupAlertTextMessage(Lng.str("Controller blocks placed\nin astronaut-mode have\nto be connected manually.\n(Please complete tutorial first)"), 0);
						return;
					}
					if (selectedType == ElementKeyMap.CARGO_SPACE) {
						getState().getController().popupAlertTextMessage(Lng.str("Cargo can only be placed in build mode.\nEnter build block or ship core."), 0);
						return;
					}
					System.err.println("[CLIENT][ExternalController] adding block to segment: " + c.getSegment() + "; " + c.getSegment().getSegmentController().getSegmentBuffer().get(c.getSegment().pos));
					getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().buildBlock((EditableSendableSegmentController) c.getSegment().getSegmentController(), pos, forw, new BuildCallback() {

						@Override
						public long getSelectedControllerPos() {
							if (selectedBlock != null) {
								return selectedBlock.getAbsoluteIndex();
							}
							return Long.MIN_VALUE;
						}

						@Override
						public void onBuild(Vector3i posBuilt, Vector3i posNextToBuild, short type) {

							getState().getPlayer().fireMusicTag(MusicTags.BUILDING);

							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUILD, AudioTags.BLOCK, AudioTags.ADD)*/
							AudioController.fireAudioEventID(973);

							Occlusion.dbPos.set(posBuilt);
							if (ElementKeyMap.isValidType(type) && !ElementKeyMap.getInfo(type).isRailTrack() && ElementKeyMap.getInfo(type).getControlling().size() > 0 && c.getSegment().getSegmentController() instanceof Ship) {
								toSelectBox = posBuilt;
								toSelectSegController = c.getSegment().getSegmentController();
								toSelectTime = System.currentTimeMillis();
							// getState().getController().popupAlertTextMessage(Lng.str("Warning:\nController blocks placed\nin astronaut mode have\nto be connected manually.\n(use '%s'(select) and '%s'(connect))", KeyboardMappings.SELECT_MODULE.getKeyChar(),  KeyboardMappings.CONNECT_MODULE.getKeyChar()), 0);
							}
						}
					}, new DimensionFilter(), symmetryPlanes, EDIT_DISTANCE);
				} else {
					//do not remove as astronaut. ONLY PICK UP CORE
					getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager()
							.getPlayerIntercationManager().removeBlock(
							(EditableSendableSegmentController) c.getSegment().getSegmentController(), pos, forw, selectedBlock, EDIT_DISTANCE, symmetryPlanes, Element.TYPE_ALL, new RemoveCallback() {
								
								@Override
								public long getSelectedControllerPos() {
									if (selectedBlock != null) {
										return selectedBlock.getAbsoluteIndex();
									}
									return Long.MIN_VALUE;
								}
								
								@Override
								public void onRemove(long pos, short type) {
								}
							});

				}
			}
		} else {
			System.err.println("[PLAYEREXTERNALEFT_CONTROLLER] CUBE RESULT NOT AVAILABLE");
		}
	}

	public boolean checkEnterDry(SegmentPiece p, boolean verbose) {
		if (p != null && !getState().getController().allowedToActivate(p)) {
			return false;
		}
		SegmentController c = p.getSegmentController();
		SegmentPiece nearestPiece = p;
		if (!(nearestPiece.getSegment().getSegmentController() instanceof PlayerControllable)) {
			if (verbose) {
				getState().getController().popupAlertTextMessage(Lng.str("ERROR\n \nCannot enter here.\nOnly ships can be entered\nat the moment!"), 0);
			}
			return false;
		}
		if (c instanceof SpaceStation && getState().getPlayer().isInTutorial()) {
			if (verbose) {
				getState().getController().popupAlertTextMessage(Lng.str("Can't do that in tutorial."), 0);
			}
			return false;
		}
		int cfid = c.getFactionId();
		if (cfid == 0 && !isAllowedToBuildAndSpawnShips()) {
			if (verbose) {
				getState().getController().popupAlertTextMessage(Lng.str("ERROR\n \nCannot enter here.\nYou are a spectator.\n"), 0);
			}
			return false;
		}
		if (cfid != 0 && cfid == getState().getPlayer().getFactionId() && !c.isSufficientFactionRights(getState().getPlayer())) {
			if (verbose) {
				getState().getController().popupAlertTextMessage(Lng.str("ERROR\n \nCannot enter here.\nYou don't have the necessary permissions.\n") + "faction rank!", 0);
			}
			return false;
		}
		if (c.isCoreOverheating()) {
			getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().popupShipRebootDialog(c);
			return false;
		}
		if (c.getHpController().isRebootingRecoverFromOverheating()) {
			if (verbose) {
				getState().getController().popupAlertTextMessage(Lng.str("ERROR\n \nCannot enter while\nrecovering from overheating\n%s", StringTools.formatTimeFromMS(c.getHpController().getRebootTimeLeftMS())), 0);
			}
			return false;
		}
		if (canEnter(nearestPiece.getType())) {
			return true;
		} else {
			if (verbose) {
				getState().getController().popupAlertTextMessage(Lng.str("ERROR\n \nCannot enter ship here.\nMust enter at core or\nanother controller!"), 0);
			}
		}
		return false;
	}

	public boolean checkEnter(SegmentPiece p) {
		if (p != null && !getState().getController().allowedToActivate(p)) {
			return false;
		}
		SegmentController c = p.getSegmentController();
		SegmentPiece nearestPiece = p;
		if (checkEnterDry(nearestPiece, true)) {
			PlayerGameControlManager playerGameControlManager = getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager();
			PlayerInteractionControlManager iMan = playerGameControlManager.getPlayerIntercationManager();
			boolean entered = false;
			if (c instanceof Ship) {
				if (nearestPiece.getType() == ElementKeyMap.CORE_ID && !((Ship) c).getAttachedPlayers().isEmpty()) {
					List<PlayerState> attachedPlayers = ((Ship) c).getAttachedPlayers();
					for (ControllerStateUnit un : attachedPlayers.get(0).getControllerState().getUnits()) {
						if (un.parameter != null && un.parameter.equals(Ship.core)) {
							getState().getController().popupAlertTextMessage(Lng.str("ERROR\n \nCannot enter here.\nThere is already someone in this ship.\nYou can still enter\ncomputers, etc. though..."), 0);
							return false;
						}
					}
				}
				entered = true;
			} else if (c instanceof SpaceStation) {
				iMan.getSegmentControlManager().setEntered(nearestPiece);
				entered = true;
			} else if (c instanceof Planet || c instanceof PlanetIco) {
				iMan.getSegmentControlManager().setEntered(nearestPiece);
				entered = true;
			} else if (c instanceof FloatingRock) {
				iMan.getSegmentControlManager().setEntered(nearestPiece);
				entered = true;
			}
			return true;
		} else {
			getState().getController().popupAlertTextMessage(Lng.str("ERROR\n \nCannot enter ship here.\nMust enter at core or\nanother controller!"), 0);
		}
		return false;
	}

	public boolean checkEnterAndEnterIfPossible(SegmentPiece p) {
		if (p != null && !getState().getController().allowedToActivate(p)) {
			return false;
		}
		SegmentController c = p.getSegment().getSegmentController();
		if (c instanceof SpaceStation && getState().getPlayer().isInTutorial()) {
			getState().getController().popupAlertTextMessage(Lng.str("Can't do that in tutorial."), 0);
			return false;
		}
		SegmentPiece nearestPiece = p;
		if (!(nearestPiece.getSegment().getSegmentController() instanceof PlayerControllable)) {
			getState().getController().popupAlertTextMessage(Lng.str("ERROR\n \nCannot enter here.\nOnly ships can be entered\nat the moment!"), 0);
			return false;
		}
		int cfid = nearestPiece.getSegment().getSegmentController().getFactionId();
		if (cfid == 0 && !isAllowedToBuildAndSpawnShips()) {
			getState().getController().popupAlertTextMessage(Lng.str("ERROR\n \nCannot enter here.\nYou are a spectator.\n"), 0);
			return false;
		}
		if (cfid != 0 && cfid == getState().getPlayer().getFactionId() && !nearestPiece.getSegmentController().isSufficientFactionRights(getState().getPlayer())) {
			getState().getController().popupAlertTextMessage(Lng.str("ERROR\n \nCannot enter here.\nYou don't have the necessary permissions.\n") + "faction rank!", 0);
			return false;
		}
		if (nearestPiece.getSegmentController().isCoreOverheating()) {
			getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().popupShipRebootDialog(nearestPiece.getSegmentController());
			return false;
		}
		if (nearestPiece.getSegmentController().getHpController().isRebootingRecoverFromOverheating()) {
			getState().getController().popupAlertTextMessage(Lng.str("ERROR\n \nCannot enter while\nrecovering from overheating\n%s", StringTools.formatTimeFromMS(nearestPiece.getSegmentController().getHpController().getRebootTimeLeftMS())), 0);
			return false;
		}
		if (canEnter(nearestPiece.getType())) {
			PlayerGameControlManager playerGameControlManager = getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager();
			PlayerInteractionControlManager iMan = playerGameControlManager.getPlayerIntercationManager();
			boolean entered = false;
			if (nearestPiece.getSegment().getSegmentController() instanceof Ship) {
				if (nearestPiece.getType() == ElementKeyMap.CORE_ID && !((Ship) nearestPiece.getSegment().getSegmentController()).getAttachedPlayers().isEmpty()) {
					List<PlayerState> attachedPlayers = ((Ship) nearestPiece.getSegment().getSegmentController()).getAttachedPlayers();
					for (ControllerStateUnit un : attachedPlayers.get(0).getControllerState().getUnits()) {
						if (un.parameter != null && un.parameter.equals(Ship.core)) {
							getState().getController().popupAlertTextMessage(Lng.str("ERROR\n \nCannot enter here.\nThere is already someone in this ship.\nYou can still enter\ncomputers, etc. though..."), 0);
							return false;
						}
					}
				}
				iMan.getInShipControlManager().setEntered(nearestPiece);
				entered = true;
			} else if (nearestPiece.getSegment().getSegmentController() instanceof SpaceStation) {
				iMan.getSegmentControlManager().setEntered(nearestPiece);
				entered = true;
			} else if (nearestPiece.getSegment().getSegmentController() instanceof Planet || nearestPiece.getSegment().getSegmentController() instanceof PlanetIco) {
				iMan.getSegmentControlManager().setEntered(nearestPiece);
				entered = true;
			} else if (nearestPiece.getSegment().getSegmentController() instanceof FloatingRock) {
				iMan.getSegmentControlManager().setEntered(nearestPiece);
				entered = true;
			} else {
				throw new RuntimeException("Cannot enter " + nearestPiece.getSegment().getSegmentController());
			}
			System.err.println("[CLIENT] Player character enter used");
			getState().currentEnterTry = nearestPiece.getSegment().getSegmentController();
			getState().currentEnterTryTime = System.currentTimeMillis();
			getState().getController().requestControlChange(getState().getCharacter(), (PlayerControllable) nearestPiece.getSegmentController(), new Vector3i(), nearestPiece.getAbsolutePos(new Vector3i()), true);
			if (nearestPiece.getSegmentController().railController.isDockedAndExecuted()) {
				float railMassPercent = nearestPiece.getSegmentController().railController.getRailMassPercent();
				if (railMassPercent < 1) {
					getState().getController().popupAlertTextMessage(Lng.str("This entity is heavier than the rail\ncan support.\nPlease reduce mass or add more rail\nmass enhancers!\nCurrently at %s%% speed", StringTools.formatPointZero(railMassPercent * 100f)), 0);
				}
			}
			return true;
		} else {
			getState().getController().popupAlertTextMessage(Lng.str("ERROR\n \nCannot enter ship here.\nMust enter at core or\nanother controller!"), 0);
		}
		return false;
	}

	private void controlCurrentIntersectionBlock() {
		if (selectedBlock != null) {
			if (ElementKeyMap.isValidType(selectedBlock.getType())) {
				ClosestRayResultCallback nearestIntersection = getNearestIntersection();
				if (nearestIntersection != null && nearestIntersection.hasHit() && nearestIntersection instanceof CubeRayCastResult) {
					CubeRayCastResult c = (CubeRayCastResult) nearestIntersection;
					if (c.getSegment() != null && c.getSegment().getSegmentController() instanceof EditableSendableSegmentController) {
						if (c.getSegment().getSegmentController() == selectedBlock.getSegmentController()) {
							// connecting within ship
							if (getState().getController().allowedToConnect(c.getSegment().getSegmentController())) {
								Vector3i absoluteElemPos = c.getSegment().getAbsoluteElemPos(c.getCubePos(), new Vector3i());
								if (getState().getController().getTutorialMode() != null && getState().getController().getTutorialMode().getMachine().getFsm().getCurrentState() instanceof ConnectedFromToTestState) {
									ConnectedFromToTestState cc = (ConnectedFromToTestState) getState().getController().getTutorialMode().getMachine().getFsm().getCurrentState();
									if (!cc.checkConnectToBlock(absoluteElemPos, getState())) {
										/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUILD, AudioTags.BLOCK, AudioTags.CONNECT, AudioTags.ERROR)*/
										AudioController.fireAudioEventID(963);
										return;
									}
								}
								try {
									SegmentPiece controlledPiece = selectedBlock.getSegmentController().getSegmentBuffer().getPointUnsave(absoluteElemPos);
									if (controlledPiece != null && controlledPiece.getType() > 0) {
										

										/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUILD, AudioTags.BLOCK, AudioTags.CONNECT)*/
										AudioController.fireAudioEventID(965);
										selectedBlock.getSegmentController().setCurrentBlockController(selectedBlock, tmpPiece, controlledPiece.getAbsoluteIndex());
									} else {
										System.err.println("[CLIENT][PlayerExternal][Connect] WARNING: intersection ok, but intersected with 0 type " + controlledPiece);
									}
								} catch (CannotBeControlledException e) {
									/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUILD, AudioTags.BLOCK, AudioTags.CONNECT, AudioTags.ERROR)*/
									AudioController.fireAudioEventID(964);
									handleConnotBuild(e);
								}
							}
						} else {
							Vector3i absoluteElemPos = c.getSegment().getAbsoluteElemPos(c.getCubePos(), new Vector3i());
							SegmentPiece controlledPiece = new SegmentPiece(c.getSegment(), c.getCubePos());
							if (selectedBlock.getType() == ElementKeyMap.LOGIC_WIRELESS && controlledPiece.getType() == ElementKeyMap.LOGIC_WIRELESS) {
								InterconnectStructureRequest s = new InterconnectStructureRequest(selectedBlock, controlledPiece, getState().getPlayer().getId());
								selectedBlock.getSegmentController().getNetworkObject().structureInterconnectRequestBuffer.add(new RemoteInterconnectStructure(s, false));
							}
						}
					}
				}
			} else {
				System.err.println("[CLIENT][PlayerExternal][Connect] WARNING: selected block is type 0 ");
			}
		}
	}

	public boolean getHideConditions() {
		PlayerGameControlManager playerGameControlManager = getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager();
		boolean hide = (playerGameControlManager.getPlayerIntercationManager().getInShipControlManager().isActive());
		hide = hide || playerGameControlManager.getPlayerIntercationManager().getInShipControlManager().isDelayedActive();
		return hide;
	}

	public ClosestRayResultCallback getNearestIntersection() {
		Vector3f camPos = new Vector3f(getState().getCharacter().getHeadWorldTransform().origin);
		Vector3f camTo = new Vector3f(getState().getCharacter().getHeadWorldTransform().origin);
		Vector3f forw = new Vector3f(Controller.getCamera().getForward());
		forw.scale(PlayerExternalController.EDIT_DISTANCE);
		camTo.add(forw);
		// SubsimplexRayCubesCovexCast.debug = true;
		CubeRayCastResult rayCallback = new CubeRayCastResult(camPos, camTo, false);
		rayCallback.setIgnoereNotPhysical(true);
		rayCallback.setOnlyCubeMeshes(true);
		ClosestRayResultCallback testRayCollisionPoint = ((PhysicsExt) getState().getPhysics()).testRayCollisionPoint(camPos, camTo, rayCallback, false);
		// SubsimplexRayCubesCovexCast.debug = false;
		return testRayCollisionPoint;
	// ((PhysicsExt)getState().getPhysics()).testRayCollisionPoint(camPos, camTo, false, null, null, false, null, false);
	}

	/**
	 * @return the selectedBlock
	 */
	public SegmentPiece getSelectedBlock() {
		return selectedBlock;
	}

	/**
	 * @return the symmetryPlanes
	 */
	public SymmetryPlanes getSymmetryPlanes() {
		return symmetryPlanes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.schema.game.client.controller.manager.AbstractControlManager#
	 * handleKeyEvent()
	 */
	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		super.handleKeyEvent(e);
		if (getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().handleSlotKey(e)) {
			return;
		}
		if (e.isTriggered(KeyboardMappings.SIT_ASTRONAUT)) {
			sit();
		}
		
		if(e.isTriggered(KeyboardMappings.FORWARD) || e.isTriggered(KeyboardMappings.UP)) {
			int dir = 0;
			if(e.isTriggered(KeyboardMappings.FORWARD)) {
				dir = 1;
			} else if(e.isTriggered(KeyboardMappings.UP)) {
				dir = -1;
			}
			handleLadderMovement(dir);
		}
		
		boolean aiControl = KeyboardMappings.CREW_CONTROL.isDown();
		if (e.isTriggered(KeyboardMappings.STUCK_PROTECT)) {
			long timeSince = System.currentTimeMillis() - getState().getCharacter().getActivatedStuckProtection();
			if (getState().getCharacter().getActivatedStuckProtection() > 0 && timeSince < suckProtectMillisDuration) {
				if (last != null) {
					getState().getController().endPopupMessage(last);
				}
				if (getState().getCharacter() != null) {
					getState().getCharacter().flagWapOutOfClient(1);
				}
				last = null;
				getState().getCharacter().setActivatedStuckProtection(0);
			} else {
				System.err.println("[CLIENT] CHECKING GRAV UP FOR DIFF OBJECT ::: in grav: " + getState().getCharacter().getGravity().isGravityOn() + "; diff obj touched: " + getState().getCharacter().getGravity().differentObjectTouched);
				if (getState().getCharacter().getGravity().isGravityOn() && getState().getCharacter().getGravity().differentObjectTouched) {
					getState().getCharacter().scheduleGravity(new Vector3f(0, 0, 0), null);
					getState().getCharacter().getGravity().differentObjectTouched = false;
				}
			}
		}
		if (e.isTriggered(KeyboardMappings.PLAYER_EMOTE_NEXT)) {
			int intValue = getState().getPlayer().getNetworkObject().playerFaceId.get().intValue();
			getState().getPlayer().getNetworkObject().playerFaceId.set(FastMath.cyclicModulo(intValue + 1, 3), true);
		}
		if (e.isTriggered(KeyboardMappings.PLAYER_EMOTE_PREVIOUS)) {
			int intValue = getState().getPlayer().getNetworkObject().playerFaceId.get().intValue();
			getState().getPlayer().getNetworkObject().playerFaceId.set(FastMath.cyclicModulo(intValue - 1, 3), true);
		}
		if (e.isTriggered(KeyboardMappings.SHAPES_RADIAL_MENU)) {
			short selectedType = getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getSelectedTypeWithSub();
			getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().openRadialSelectShapes(selectedType);
		}
		if (e.isTriggered(KeyboardMappings.ACTIVATE)) {
			GameTransformable nearest = getState().getCharacter().getNearestEntity(true);System.err.println("[CLIENT] ACTIVATE::: "+nearest);
			if (nearest instanceof SegmentController) {
				SegmentPiece p = getState().getCharacter().getNearestPiece(true);
				if (p != null && p.getType() == ElementKeyMap.SHIPYARD_CORE_POSITION) {
					for (RailRelation r : p.getSegmentController().railController.next) {
						if (r.rail.getAbsoluteIndex() == p.getAbsoluteIndex()) {
							p = r.docked.getSegmentController().getSegmentBuffer().getPointUnsave(r.docked.getAbsoluteIndex());
							if (p == null) {
								return;
							}
							nearest = p.getSegmentController();
							break;
						}
					}
				}
				if (nearest instanceof ManagedShop && ((ManagedShop) nearest).isAdvancedShop() && p.getType() == ElementKeyMap.DECORATIVE_PANEL_1 && p.getAbsolutePosY() == 267) {
					getState().getPlayer().sendSimpleCommand(SimplePlayerCommands.SPAWN_SHOPKEEP, nearest.getId());
				} else {
					getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().checkActivate(p);
				}
			} else {
				assert (nearest != getState().getCharacter());
				System.err.println("[PlayerExternal] nearest: " + nearest + "; (interaction: " + (nearest instanceof InteractionInterface) + ")");
				if (nearest instanceof InteractionInterface) {
					((InteractionInterface) nearest).interactClient(getState().getPlayer());
				}
			}
		} else if (e.isTriggered(KeyboardMappings.SELECT_MODULE)) {
			selectCameraBlock();
		} else if (e.isTriggered(KeyboardMappings.CONNECT_MODULE)) {
			controlCurrentIntersectionBlock();
		} else if (e.isTriggered(KeyboardMappings.SPAWN_SHIP)) {
			spawnShip();
		} else if (e.isTriggered(KeyboardMappings.SPAWN_SPACE_STATION)) {
			spawnStation();
		}
		if (e.isTriggered(KeyboardMappings.NEXT_BLOCK_ROTATION) || e.isTriggered(KeyboardMappings.PREVIOUS_BLOCK_ROTATION)) {
			PlayerInteractionControlManager playerIntercationManager = getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
			if (ElementKeyMap.isValidType(playerIntercationManager.getSelectedTypeWithSub())) {
				int maxRotation = GUIOrientationSettingElement.getMaxRotation(playerIntercationManager);
				playerIntercationManager.setBlockOrientation(FastMath.cyclicModulo(playerIntercationManager.getBlockOrientation() + (e.isTriggered(KeyboardMappings.NEXT_BLOCK_ROTATION) ? 1 : e.isTriggered(KeyboardMappings.PREVIOUS_BLOCK_ROTATION) ? -1 : 0), maxRotation));
			}
		}
		if (e.isTriggered(KeyboardMappings.USE_SLOT_ITEM_CHARACTER) || e.isTriggered(KeyboardMappings.REMOVE_BLOCK_CHARACTER)) {
			try {
				checkAddAndRemove(e.isTriggered(KeyboardMappings.USE_SLOT_ITEM_CHARACTER));
			} catch (ElementPositionBlockedException e1) {
				e1.printStackTrace();
			}
		}
	}

	private void handleLadderMovement(int dir) {
		if (getState().getPlayer().isClimbing()) {
			System.err.println("[CLIENT][CLIMB] climbing");
			getState().getPlayer().sendSimpleCommand(SimplePlayerCommands.CLIMB, -1, -1, 0);
		} else {
			getState().getCharacter().climb(getNearestIntersection(), dir);
		}
	}

	private void spawnStation() {
		if (!isAllowedToBuildAndSpawnShips()) {
			getState().getController().popupAlertTextMessage(Lng.str("ERROR\n\nCan't do that!\nYou are a spectator!"), 0);
			return;
		}
		SegmentPiece np = getState().getCharacter().getNearestPiece(false);
		if (np != null) {
			if (np.getSegment().getSegmentController().isScrap()) {
				long price = np.getSegment().getSegmentController().getElementClassCountMap().getPrice();
				if (getState().getPlayer().getCredits() < price) {
					getState().getController().popupAlertTextMessage(Lng.str("You don't have enough money\nto repair this!\n(%s  credits required)", price), 0);
					return;
				}
				repairSpaceStation(np, price);
				return;
			} else {
				getState().getController().popupAlertTextMessage(Lng.str("Cannot spawn station here.\nNo room to spawn in the target area!"), 0);
				return;
			}
		}
		if (getState().getPlayer().getCredits() < getState().getGameState().getStationCost()) {
			getState().getController().popupAlertTextMessage(Lng.str("You don't have enough money\nfor a Space Station! \n(%s credits required)", StringTools.formatSeperated(getState().getGameState().getStationCost())), 0);
			return;
		}
		buildSpaceStation();
	}

	private void spawnShip() {
		if (!isAllowedToBuildAndSpawnShips()) {
			getState().getController().popupAlertTextMessage(Lng.str("ERROR\n \nCan't do that!\nYou are a spectator!"), 0);
			return;
		}
		if (!getState().getPlayer().getInventory(null).existsInInventory(ElementKeyMap.CORE_ID)) {
			getState().getController().popupAlertTextMessage(Lng.str("ERROR\n\nYou need a ship core\nto create a ship!"), 0);
			return;
		}
		SegmentPiece np = getState().getCharacter().getNearestPiece(false);
		if (np != null) {
			getState().getController().popupAlertTextMessage(Lng.str("ERROR\n \nCannot spawn ship here.\nNo space!"), 0);
			return;
		}
		PlayerGameTextInput pp = new PlayerGameTextInput("PlayerExternalController_NEW_ENTITY_NAME", getState(), 50, Lng.str("New Ship"), description, getState().getPlayerName() + "_" + System.currentTimeMillis()) {

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) {
				return s;
			}

			@Override
			public void onDeactivate() {
				System.err.println("deactivate");
				suspend(false);
			}

			@Override
			public void onFailedTextCheck(String msg) {
				setErrorMessage("SHIPNAME INVALID: " + msg);
			}

			@Override
			public boolean onInput(String entry) {
				if (getState().getCharacter() == null || getState().getCharacter().getPhysicsDataContainer() == null || !getState().getCharacter().getPhysicsDataContainer().isInitialized()) {
					System.err.println("[ERROR] Character might not have been initialized");
					return false;
				}
				Transform t = new Transform();
				Vector3f forward = new Vector3f(Controller.getCamera().getForward());
				t.set(getState().getCharacter().getPhysicsDataContainer().getCurrentPhysicsTransform());
				forward.scale(2 * Element.BLOCK_SIZE);
				t.origin.add(forward);
				t.basis.rotY(-FastMath.HALF_PI);
				/*
				if(entry.toLowerCase(Locale.ENGLISH).contains("vehicle")){
					lastShipCreatedName = EntityRequest.convertVehicleEntityName(entry.trim());
					getState().getController().requestNewVehicle(
							t,
							new Vector3i(-2, -2, -2),
							new Vector3i(2, 2, 2),
							getState().getPlayer(),
							lastShipCreatedName, entry.trim());
				}
				 */
				if (getState().getPlayer().getInventory(null).existsInInventory(ElementKeyMap.CORE_ID)) {
					if (entry.toLowerCase(Locale.ENGLISH).contains("vehicle")) {
					} else {
						String lastShipCreatedName = EntityRequest.convertShipEntityName(entry.trim());
						try {
							getState().getController().requestNewShip(t, new Vector3i(-2, -2, -2), new Vector3i(2, 2, 2), getState().getPlayer(), lastShipCreatedName, entry.trim());
						} catch (IOException e) {
							e.printStackTrace();
						}
						System.err.println("SENDING LAST NAME: " + lastShipCreatedName + "; obs: " + listener.size());
						sendLastShipName(lastShipCreatedName);
					}
				} else {
					getState().getController().popupAlertTextMessage("ERROR\n" + "You need a ship core\n" + "to create a ship", 0);
				}
				return true;
			}
		};
		pp.setInputChecker((entry, callback) -> {
			if (System.currentTimeMillis() - getState().getController().lastShipSpawn < GameClientController.SHIP_SPAM_PROTECT_TIME_SEC * 1000) {
				return false;
			}
			if (EntityRequest.isShipNameValid(entry)) {
				return true;
			} else {
				callback.onFailedTextCheck("Must only contain Letters or numbers or ( _-)!");
				return false;
			}
		});
		pp.getInputPanel().onInit();
		pp.getInputPanel().getButtonOK().setText(new Object() {

			@Override
			public String toString() {
				getState().getController();
				if (System.currentTimeMillis() - getState().getController().lastShipSpawn > GameClientController.SHIP_SPAM_PROTECT_TIME_SEC * 1000) {
					return Lng.str("OK");
				} else {
					getState().getController();
					return Lng.str("WAIT ") + "(" + (GameClientController.SHIP_SPAM_PROTECT_TIME_SEC - (int) Math.ceil((System.currentTimeMillis() - getState().getController().lastShipSpawn) / 1000f) + ")");
				}
			}
		});
		pp.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(110);
		suspend(true);
	}

	@Override
	public void onSwitch(boolean active) {
		if (active) {
			if (getState().getCharacter() == null) {
				setActive(false);
				return;
			}
			symmetryPlanes.setXyPlaneEnabled(false);
			symmetryPlanes.setXzPlaneEnabled(false);
			symmetryPlanes.setYzPlaneEnabled(false);
			getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager().save(getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager().user);
			getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager().user = (getState().getCharacter().getUniqueIdentifier());
			getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager().reset();
			if (followerCamera == null) {
				followerCamera = new PlayerCamera(getState(), getState().getCharacter());
			// followerCamera = new PlayerCamera(getState(), ((GameClientState)getState()).getCharacter());
			// followerCamera.setCameraStartOffset(2500);
			}
			if (((PlayerCamera) followerCamera).getCharacter() != getState().getCharacter()) {
				((PlayerCamera) followerCamera).setCharacter(getState().getCharacter());
			}
			Controller.setCamera(followerCamera);
		} else {
		}
		super.onSwitch(active);
	}

	@Override
	public void update(Timer timer) {
		CameraMouseState.setGrabbed(true);
		if (selectedBlock != null) {
			if (!getState().getCurrentSectorEntities().containsKey(selectedBlock.getSegmentController().getId())) {
				selectedBlock = null;
			} else {
				selectedBlock.refresh();
				if (selectedBlock.getType() == Element.TYPE_NONE) {
					selectedBlock = null;
				}
			}
		}
		if (toSelectSegController != null) {
			if (!getState().getCurrentSectorEntities().containsKey(toSelectSegController.getId()) || System.currentTimeMillis() - toSelectTime > 8000) {
				toSelectSegController = null;
				toSelectBox = null;
			} else {
				SegmentPiece pointUnsave = toSelectSegController.getSegmentBuffer().getPointUnsave(toSelectBox);
				if (pointUnsave != null && ElementKeyMap.isValidType(pointUnsave.getType())) {
					selectedBlock = pointUnsave;
					toSelectSegController = null;
					toSelectBox = null;
				}
			}
		}
		if (controllersDirty) {
			// no controller set to update
			controllersDirty = false;
		}
		if (getState().getCharacter() == null) {
			System.err.println("[WARNING] state character is removed: entering spawn screen!");
			this.setActive(false);
			getState().getGlobalGameControlManager().getIngameControlManager().getAutoRoamController().setActive(true);
		} else {
			if (getState().getCharacter().getActivatedStuckProtection() > 0) {
				long timeSince = System.currentTimeMillis() - getState().getCharacter().getActivatedStuckProtection();
				if (timeSince < suckProtectMillisDuration) {
					int seconds = (int) (suckProtectMillisDuration / 1000 - timeSince / 1000);
					String msg = Lng.str("If you are stuck in a block,\npress %s.\nTime left to use this: %s sec", KeyboardMappings.STUCK_PROTECT.getKeyChar(), seconds);
					if (last != null) {
						getState().getController().changePopupMessage(last, msg);
					} else {
						getState().getController().popupInfoTextMessage(msg, 0);
					}
					last = msg;
				} else {
					if (last != null) {
						getState().getController().endPopupMessage(last);
					}
					getState().getCharacter().setActivatedStuckProtection(0);
					last = null;
				}
			}
		}
	}

	private void buildSpaceStation() {
		PlayerGameTextInput pp = new PlayerGameTextInput("PlayerExternalController_NEW_ENTITY_NAME", getState(), 50, Lng.str("New Space Station"), description, getState().getPlayerName() + "_" + System.currentTimeMillis()) {

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) {
				return s;
			}

			@Override
			public void onDeactivate() {
				System.err.println("deactivate");
				suspend(false);
			}

			@Override
			public void onFailedTextCheck(String msg) {
				setErrorMessage("NAME INVALID: " + msg);
			}

			@Override
			public boolean onInput(String entry) {
				if (getState().getCharacter() == null || getState().getCharacter().getPhysicsDataContainer() == null || !getState().getCharacter().getPhysicsDataContainer().isInitialized()) {
					return false;
				}
				Transform t = new Transform();
				Vector3f forward = new Vector3f(Controller.getCamera().getForward());
				t.set(getState().getCharacter().getPhysicsDataContainer().getCurrentPhysicsTransform());
				forward.scale(2 * Element.BLOCK_SIZE);
				t.origin.add(forward);
				t.basis.rotY(-FastMath.HALF_PI);
				try {
					{
						String convertStationEntityName = EntityRequest.convertStationEntityName(entry.trim());
						getState().getController().requestNewStation(t, getState().getPlayer(), convertStationEntityName, entry.trim());
					}
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
				return true;
			}
		};
		pp.setInputChecker((entry, callback) -> {
			if (EntityRequest.isShipNameValid(entry)) {
				return true;
			} else {
				callback.onFailedTextCheck(Lng.str("Must only contain letters or numbers or (_-)!"));
				return false;
			}
		});
		pp.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(111);
	}

	private void repairSpaceStation(final SegmentPiece np, long price) {
		PlayerGameTextInput pp = new PlayerGameTextInput("CONFIRM", getState(), 50, Lng.str("Repair Space Station"), Lng.str("This will cost: %s credits!\nDo you want to rename the station?", price), np.getSegment().getSegmentController().getRealName()) {

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) {
				return s;
			}

			@Override
			public void onDeactivate() {
				System.err.println("deactivate");
				suspend(false);
			}

			@Override
			public void onFailedTextCheck(String msg) {
				setErrorMessage(Lng.str("NAME INVALID: %s", msg));
			}

			@Override
			public boolean onInput(String entry) {
				if (getState().getCharacter() == null || getState().getCharacter().getPhysicsDataContainer() == null || !getState().getCharacter().getPhysicsDataContainer().isInitialized()) {
					return false;
				}
				getState().getPlayer().sendSimpleCommand(SimplePlayerCommands.REPAIR_STATION, np.getSegment().getSegmentController().getId(), entry);
				return true;
			}
		};
		pp.setInputChecker((entry, callback) -> {
			if (EntityRequest.isShipNameValid(entry)) {
				return true;
			} else {
				callback.onFailedTextCheck(Lng.str("Must only contain letters or numbers or (_-)!"));
				return false;
			}
		});
		pp.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(112);
	}

	private void initialize() {
	}

	@Override
	public void notifyElementChanged() {
		this.controllersDirty = true;
		getState().getWorldDrawer().getBuildModeDrawer().flagControllerSetChanged();
	}

	private void selectCameraBlock() {
		ClosestRayResultCallback nearestIntersection = getNearestIntersection();
		if (nearestIntersection != null && nearestIntersection.hasHit() && nearestIntersection instanceof CubeRayCastResult) {
			CubeRayCastResult c = (CubeRayCastResult) nearestIntersection;
			if (c.getSegment() != null && c.getSegment().getSegmentController() instanceof EditableSendableSegmentController) {
				Vector3i absoluteElemPos = c.getSegment().getAbsoluteElemPos(c.getCubePos(), new Vector3i());
				if (selectedBlock != null && selectedBlock.getAbsolutePos(new Vector3i()).equals(absoluteElemPos)) {
					selectedBlock = null;
				} else {
					selectedBlock = c.getSegment().getSegmentController().getSegmentBuffer().getPointUnsave(absoluteElemPos);
					if (selectedBlock != null && getState().getController().getTutorialMode() != null && getState().getController().getTutorialMode().getMachine().getFsm().getCurrentState() instanceof ConnectedFromToTestState) {
						ConnectedFromToTestState cc = (ConnectedFromToTestState) getState().getController().getTutorialMode().getMachine().getFsm().getCurrentState();
						if (!cc.checkConnectBlock(selectedBlock, getState())) {
							selectedBlock = null;
						}
					}
				}
			} else {
				selectedBlock = null;
			}
		} else {
			selectedBlock = null;
		}
		//INSERTED CODE
		ClientSelectSegmentPieceEvent event = new ClientSelectSegmentPieceEvent(selectedBlock, ClientSelectSegmentPieceEvent.Context.PLAYER);
		StarLoader.fireEvent(event, false);
		///
	}

	public void sendLastShipName(String lastShipName) {
		notifyObservers();
	}

	public void setSelectedBlock(SegmentPiece p) {
		selectedBlock = p;
	}
}
