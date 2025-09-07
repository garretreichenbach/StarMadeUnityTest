package org.schema.game.client.controller.manager.ingame;

import api.common.GameClient;
import api.listener.events.block.ClientSelectSegmentPieceEvent;
import api.mod.StarLoader;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.lwjgl.glfw.GLFW;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.tutorial.states.ConnectedFromToTestState;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.camera.BuildShipCamera;
import org.schema.game.client.view.gui.buildtools.GUIOrientationSettingElement;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ManagerModule;
import org.schema.game.common.controller.elements.UsableControllableSingleElementManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.VoidSegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.CubeRayCastResult;
import org.schema.game.common.data.physics.PhysicsExt;
import org.schema.game.common.data.player.inventory.InventorySlot;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.camera.viewer.FixedViewer;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.Keyboard;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.sound.controller.AudioController;
import org.schema.schine.sound.controller.MusicTags;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;

public class SegmentBuildController extends AbstractBuildControlManager {

	public static final float EDIT_DISTANCE = 300;

	public static Vector3f INITAL_BUILD_CAM_DIST = new Vector3f(0, 0, -2);

	private final LongOpenHashSet done = new LongOpenHashSet(256);

	private final LongArrayList toDo = new LongArrayList(256);

	private final Vector3i tmpPos = new Vector3i();

	private final SegmentPiece tmpPiece = new SegmentPiece();
	// private void selectCurrentBlock()  {
	// currentlyCSelectedBlock = shipBuildCamera.getCurrentBlock();
	// if(currentlyCSelectedBlock != null){
	// if(selectedBlock != null && selectedBlock.getAbsolutePos(new Vector3i()).equals(currentlyCSelectedBlock)){
	// selectedBlock = null;
	// }else{
	// selectedBlock = edit.getSegmentController().getSegmentBuffer().getPointUnsave(currentlyCSelectedBlock, true);
	// }
	// }
	//
	// }
	private final EditSegmentInterface edit;
	private final Vector3i currentIntersectionElement = new Vector3i();
	private final ArrayList<Vector3i> controllers = new ArrayList<Vector3i>();
	Matrix4f tmpCMat = new Matrix4f();
	Matrix4f tmpSMat = new Matrix4f();
	Segment cachedLastSegment;
	int tmpN;
	int mCol;
	int nColMan;
	ElementCollectionManager<?, ?, ?> lastCol;
	Vector3i mPos = new Vector3i();
	private BuildShipCamera shipBuildCamera;
	private Vector3i currentlyCSelectedBlock;
	private SegmentPiece selectedBlock;
	private CollisionWorld.ClosestRayResultCallback currentNearestCollision;
	private boolean elementIntersectionExists;
	private short currentElementType;
	private int controllerIndex;

	private boolean controllersDirty;

	private SegmentController lastShip;

	private long lastNoteContBlock;

	private SegmentPiece currentSegmentPiece;

	public SegmentBuildController(GameClientState state, EditSegmentInterface edit) {
		super(state);
		this.edit = edit;
	}

	private void controlCurrentIntersectionBlock() {
		System.err.println("[CLIENT][SEGBUILDCONTROLLER] NORMAL CONNECTION");
		try {
			if(currentIntersectionElement != null) {
				if(getState().getController().getTutorialMode() != null && getState().getController().getTutorialMode().getMachine().getFsm().getCurrentState() instanceof ConnectedFromToTestState cc) {
					if(!cc.checkConnectToBlock(currentIntersectionElement, getState())) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUILD, AudioTags.BLOCK, AudioTags.CONNECT, AudioTags.ERROR)*/
						AudioController.fireAudioEventID(968);
						return;
					}
				}
				edit.getSegmentController().setCurrentBlockController(selectedBlock, tmpPiece, ElementCollection.getIndex(currentIntersectionElement));
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUILD, AudioTags.BLOCK, AudioTags.CONNECT)*/
				AudioController.fireAudioEventID(969);
			} else {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUILD, AudioTags.BLOCK, AudioTags.CONNECT, AudioTags.ERROR)*/
				AudioController.fireAudioEventID(967);
			}
		} catch(CannotBeControlledException e) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUILD, AudioTags.BLOCK, AudioTags.CONNECT, AudioTags.ERROR)*/
			AudioController.fireAudioEventID(966);
			handleConnotBuild(e);
		}
	}

	private void controlCurrentIntersectionBlockBulk() {
		System.err.println("[CLIENT][SEGBUILDCONTROLLER] BULK CONNECTION");
		if(currentIntersectionElement != null && getState().getController().getTutorialMode() != null && getState().getController().getTutorialMode().getMachine().getFsm().getCurrentState() instanceof ConnectedFromToTestState cc) {
			if(!cc.checkConnectToBlock(currentIntersectionElement, getState())) {
				return;
			}
		}
		try {
			SegmentBufferManager m = (SegmentBufferManager) edit.getSegmentController().getSegmentBuffer();
			NeighboringBlockCollection neighborCollection = m.getNeighborCollection(currentIntersectionElement);
			Vector3i pos = new Vector3i();
			if(selectedBlock != null && neighborCollection != null && neighborCollection.getResult().size() > 0) {
				System.err.println("[CLIENT][SEGBUILDCONTROLLER] BULK CONNECTING " + neighborCollection.getResult().size() + " elements of type " + neighborCollection.getType());
				boolean controlling = edit.getSegmentController().getControlElementMap().isControlling(selectedBlock.getAbsoluteIndex(), currentIntersectionElement, neighborCollection.getType());
				for(long p : neighborCollection.getResult()) {
					int mode = controlling ? 2 : 1;
					edit.getSegmentController().setCurrentBlockController(selectedBlock, tmpPiece, p, mode);
				}
			} else if(selectedBlock != null && selectedBlock.getType() != Element.TYPE_NONE && ElementKeyMap.getInfo(selectedBlock.getType()).isSignal()) {
				System.err.println("[CLIENT][SEGBUILDCONTROLLER] CHECKING SINGLE BLOCK MANAGERS FOR SIGNAL " + selectedBlock);
				SegmentController segmentController = selectedBlock.getSegment().getSegmentController();
				boolean found = false;
				if(segmentController instanceof ManagedSegmentController<?>) {
					ManagerContainer<?> managerContainer = ((ManagedSegmentController<?>) segmentController).getManagerContainer();
					for(ManagerModule<?, ?, ?> mod : managerContainer.getModules()) {
						if(mod.getElementManager() instanceof UsableControllableSingleElementManager<?, ?, ?> voidMan) {
							ElementCollectionManager<?, ?, ?> ec = voidMan.getCollection();
							if(ec.isDetailedElementCollections()) {
								if(ec.rawCollection != null && ec.rawCollection.contains(ElementCollection.getIndex(currentIntersectionElement))) {
									for(ElementCollection<?, ?, ?> e : ec.getElementCollections()) {
										if(e.getNeighboringCollection().contains(ElementCollection.getIndex(currentIntersectionElement))) {
											boolean controlling;
											short type = e.getClazzId();
											if(ElementKeyMap.isDoor(type)) {
												controlling = edit.getSegmentController().getControlElementMap().isControlling(selectedBlock.getAbsoluteIndex(), currentIntersectionElement, ElementKeyMap.doorTypes);
											} else {
												controlling = edit.getSegmentController().getControlElementMap().isControlling(selectedBlock.getAbsoluteIndex(), currentIntersectionElement, type);
											}
											for(long p : e.getNeighboringCollection()) {
												int mode = controlling ? 2 : 1;
												edit.getSegmentController().setCurrentBlockController(selectedBlock, tmpPiece, p, mode);
											}
										}
									}
									found = true;
								}
							} else {
								// no neighboring collection attached
								System.err.println("[CLIENT] Cannot bulk link to non detailed collection. alternative search result: " + neighborCollection.getResult().size());
							}
						}
					}
				}
				if(elementIntersectionExists && !found && ElementKeyMap.getInfo(currentElementType).canActivate()) {
					SegmentPiece cur = new SegmentPiece(currentSegmentPiece);
					assert (cur.getType() > 0) : cur;
					done.clear();
					toDo.clear();
					done.add(cur.getAbsoluteIndex());
					toDo.add(cur.getAbsoluteIndexWithType4());
					boolean controlling = edit.getSegmentController().getControlElementMap().isControlling(selectedBlock.getAbsoluteIndex(), currentIntersectionElement, currentElementType);
					int mode = controlling ? 2 : 1;
					// using iteration instead of recursion to speed up things
					while(!toDo.isEmpty()) {
						long current = toDo.removeLong(toDo.size() - 1);
						int type = ElementCollection.getType(current);
						if(type == currentElementType) {
							long indexWithoutType = ElementCollection.getPosIndexFrom4(current);
							edit.getSegmentController().setCurrentBlockController(selectedBlock, tmpPiece, indexWithoutType, mode);
							for(int i = 0; i < 6; i++) {
								cur.getAbsolutePos(tmpPos);
								tmpPos.add(Element.DIRECTIONSi[i]);
								long shiftedIndex = ElementCollection.getIndex(tmpPos);
								if(!done.contains(shiftedIndex)) {
									done.add(shiftedIndex);
									SegmentPiece pointUnsave = edit.getSegmentController().getSegmentBuffer().getPointUnsave(tmpPos, tmpPiece);
									// System.err.println("EEE: "+pointUnsave);
									if(pointUnsave != null && pointUnsave.getType() == currentElementType) {
										assert (pointUnsave.getAbsoluteIndex() == shiftedIndex);
										toDo.add(pointUnsave.getAbsoluteIndexWithType4());
									}
								}
							}
						}
					}
					// connectRecursively(cur, currentElementType, done, mode);
				}
				done.clear();
				toDo.clear();
			}
		} catch(CannotBeControlledException e) {
			handleConnotBuild(e);
		}
	}

	public Vector3i getCurrentBlock() {
		return currentlyCSelectedBlock;
	}

	/**
	 * @return the currentElementType
	 */
	public short getCurrentElementType() {
		return currentElementType;
	}

	public CollisionWorld.ClosestRayResultCallback getCurrentNearestCollision() {
		return currentNearestCollision;
	}

	public SegmentController getSegmentController() {
		return edit.getSegmentController();
	}

	public SegmentPiece getSelectedBlock() {
		return selectedBlock;
	}

	public void setSelectedBlock(SegmentPiece p) {
		selectedBlock = p;
	}

	/**
	 * @return the symmetryPlanes
	 */
	public SymmetryPlanes getSymmetryPlanes() {
		BuildToolsManager bt = getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager();
		return bt.getSymmetryPlanes();
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		BuildToolsManager bt = getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager();
		if(e.isTriggered(KeyboardMappings.USE_SLOT_ITEM_CHARACTER) && bt.isInCreateDockingMode() && bt.getBuildToolCreateDocking().core != null && bt.getBuildToolCreateDocking().coreOrientation >= 0) {
			System.err.println("[CLIENT] create docking mode executing now");
			bt.getBuildToolCreateDocking().core.setOrientation((byte) (bt.getBuildToolCreateDocking().coreOrientation));
			bt.getBuildToolCreateDocking().execute(getState());
			bt.cancelCreateDockingMode();
		} else if(e.isTriggered(KeyboardMappings.SCROLL_BOTTOM_BAR_NEXT)) {
			if(getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().isInAnyStructureBuildMode()) {
				getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getHotbarLayout().next();
			}
		} else if(e.isTriggered(KeyboardMappings.SCROLL_BOTTOM_BAR_PREVIOUS)) {
			if(getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().isInAnyStructureBuildMode()) {
				getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getHotbarLayout().previous();
			}
		} else if((e.isTriggered(KeyboardMappings.NEXT_SLOT) || e.isTriggered(KeyboardMappings.PREVIOUS_SLOT)) && bt.isInCreateDockingMode()) {
			int dWheel = e.isTriggered(KeyboardMappings.NEXT_SLOT) ? 1 : -1;
			int maxRotation = 32;
			bt.getBuildToolCreateDocking().potentialCoreOrientation = FastMath.cyclicModulo(bt.getBuildToolCreateDocking().potentialCoreOrientation + dWheel, maxRotation);
		} else if(((e.isTriggered(KeyboardMappings.NEXT_SLOT) || e.isTriggered(KeyboardMappings.PREVIOUS_SLOT)) && PlayerInteractionControlManager.isAdvancedBuildMode(getState())) && !e.isTriggered(KeyboardMappings.SCROLL_BOTTOM_BAR_NEXT) && !e.isTriggered(KeyboardMappings.SCROLL_BOTTOM_BAR_NEXT)) {
			int dWheel = e.isTriggered(KeyboardMappings.NEXT_SLOT) ? 1 : -1;
			PlayerInteractionControlManager playerIntercationManager = getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
			if(ElementKeyMap.isValidType(playerIntercationManager.getSelectedTypeWithSub())) {
				int maxRotation = GUIOrientationSettingElement.getMaxRotation(playerIntercationManager);
				playerIntercationManager.setBlockOrientation(FastMath.cyclicModulo(playerIntercationManager.getBlockOrientation() + (dWheel > 0 ? 1 : dWheel < 0 ? -1 : 0), maxRotation));
			}
		}
		if(edit.getSegmentController() != null && Controller.getCamera() != null && !isSuspended()) {
			// build and remove on button release
			if(EngineSettings.BUILD_AND_REMOVE_ON_BUTTON_RELEASE.isOn()) {
				if(e.isTriggeredRelease(KeyboardMappings.BUILD_BLOCK_BUILD_MODE)) {
					buildButtonPressed(e);
				}
				if(e.isTriggeredRelease(KeyboardMappings.REMOVE_BLOCK_BUILD_MODE)) {
					removeButtonPressed(e);
				}
			} else {
				if(e.isTriggered(KeyboardMappings.BUILD_BLOCK_BUILD_MODE)) {
					buildButtonPressed(e);
				}
				if(e.isTriggered(KeyboardMappings.REMOVE_BLOCK_BUILD_MODE)) {
					removeButtonPressed(e);
				}
			}
		}
		if(getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().handleSlotKey(e)) {
			return;
		}
		if(e.isTriggered(KeyboardMappings.SHAPES_RADIAL_MENU)) {
			short selectedType = getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getSelectedTypeWithSub();
			getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().openRadialSelectShapes(selectedType);
		}
		if(e.isTriggered(KeyboardMappings.UNDO)) {
			getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager().undo();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUILD, AudioTags.UNDO)*/
			AudioController.fireAudioEventID(970);
		}
		if(e.isTriggered(KeyboardMappings.REDO)) {
			getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager().redo();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUILD, AudioTags.REDO)*/
			AudioController.fireAudioEventID(971);
		}
		if(e.isTriggered(KeyboardMappings.BUILD_MODE_FLASHLIGHT)) {
			getState().getPlayer().getBuildModePosition().setFlashlightOnClient(!getState().getPlayer().getBuildModePosition().isFlashlightOn());
		} else if(e.isTriggered(KeyboardMappings.SELECT_MODULE)) {
			selectCameraBlock();
		} else if(!KeyboardMappings.KEY_BULK_CONNECTION_MOD.isDown() && e.isTriggered(KeyboardMappings.NEXT_CONTROLLER)) {
			selectControllerBlock(true);
		} else if(!KeyboardMappings.KEY_BULK_CONNECTION_MOD.isDown() && e.isTriggered(KeyboardMappings.PREVIOUS_CONTROLLER)) {
			selectControllerBlock(false);
		} else if(!KeyboardMappings.KEY_BULK_CONNECTION_MOD.isDown() && e.isTriggered(KeyboardMappings.SELECT_CORE)) {
			selectCoreBlock(false);
		} else if(!KeyboardMappings.KEY_BULK_CONNECTION_MOD.isDown() && e.isTriggered(KeyboardMappings.JUMP_TO_MODULE)) {
			jumpToCurrentBlock();
		} else if(!KeyboardMappings.KEY_BULK_CONNECTION_MOD.isDown() && e.isTriggered(KeyboardMappings.CONNECT_MODULE)) {
			controlCurrentIntersectionBlock();
		} else if(KeyboardMappings.KEY_BULK_CONNECTION_MOD.isDown() && e.isTriggered(KeyboardMappings.CONNECT_MODULE)) {
			controlCurrentIntersectionBlockBulk();
		} else if(e.isAnyAlt()) {
			if(isAnyArrowKeyDown() && GameClient.getControlManager().getBuildToolsManager().isPasteMode()) {
				Camera camera = Controller.getCamera();
				if(!GameClient.getControlManager().getBuildToolsManager().getCopyArea().locked) {
					Vector3i min = new Vector3i(GameClient.getControlManager().getBuildToolsManager().getCopyArea().min);
					Vector3i max = new Vector3i(GameClient.getControlManager().getBuildToolsManager().getCopyArea().max);
					ArrayList<VoidSegmentPiece> pieces = new ArrayList<>(GameClient.getControlManager().getBuildToolsManager().getCopyArea().getPieces());

					int moveAmount = Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) ? 10 : 1;
					Vector3f axis = new Vector3f();
					axis = switch(e.getKey()) {
						case GLFW.GLFW_KEY_LEFT -> new Vector3f(moveAmount, 0, 0);
						case GLFW.GLFW_KEY_RIGHT -> new Vector3f(-moveAmount, 0, 0);
						case GLFW.GLFW_KEY_DOWN -> new Vector3f(0, 0, -moveAmount);
						case GLFW.GLFW_KEY_UP -> new Vector3f(0, 0, moveAmount);
						case GLFW.GLFW_KEY_PAGE_UP -> new Vector3f(0, moveAmount, 0);
						case GLFW.GLFW_KEY_PAGE_DOWN -> new Vector3f(0, -moveAmount, 0);
						default -> axis;
					};

					camera.getWorldTransform().basis.transform(axis);
					Vector3i move = new Vector3i();
					if(Math.abs(axis.x) > Math.abs(axis.y) && Math.abs(axis.x) > Math.abs(axis.z)) move.x = Math.round(axis.x);
					else if(Math.abs(axis.y) > Math.abs(axis.x) && Math.abs(axis.y) > Math.abs(axis.z)) move.y = Math.round(axis.y);
					else move.z = Math.round(axis.z);
					min.add(move.x, move.y, move.z);
					max.add(move.x, move.y, move.z);
					GameClient.getControlManager().getBuildToolsManager().getCopyArea().min.set(min);
					GameClient.getControlManager().getBuildToolsManager().getCopyArea().max.set(max);
					for(VoidSegmentPiece piece : pieces) piece.voidPos.add(move.x, move.y, move.z);

					if(PlayerInteractionControlManager.isAdvancedBuildMode(getState())) {
						PlayerInteractionControlManager p = getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
						p.handleKeyOrientation(e);
					}
				}
			}
		}
		if(PlayerInteractionControlManager.isAdvancedBuildMode(getState())) {
			PlayerInteractionControlManager p = getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
			p.handleKeyOrientation(e);
		}
		shipBuildCamera.handleKeyEvent(e);
	}


	private boolean isAnyArrowKeyDown() {
		return Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT) || Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT) || Keyboard.isKeyDown(GLFW.GLFW_KEY_DOWN) || Keyboard.isKeyDown(GLFW.GLFW_KEY_UP) || Keyboard.isKeyDown(GLFW.GLFW_KEY_PAGE_UP) || Keyboard.isKeyDown(GLFW.GLFW_KEY_PAGE_DOWN);
	}

	@Override
	public void onSwitch(boolean active) {
		if(active) {
			if(edit.getSegmentController() != lastShip) {
				currentlyCSelectedBlock = null;
				selectedBlock = null;
				lastShip = edit.getSegmentController();
			}
			getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager().load(edit.getSegmentController().getUniqueIdentifier());
			getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager().user = (edit.getSegmentController().getUniqueIdentifier());
			// if(edit.getSegmentController() instanceof ManagedSegmentController<?>){
			// ((ManagedSegmentController<?>)edit.getSegmentController()).getManagerContainer().a
			// }
			edit.getSegmentController().getControlElementMap().setObs(this);
			controllers.clear();
			controllersDirty = true;
			// getState().getController().popupInfoTextMessage("INFO:\n" +
			// "You are now in Build Mode!\n" +
			// "Tip: press "+KeyboardMappings.CHANGE_SHIP_MODE.getKeyChar()+" change\n" +
			// "to flight mode", 0);
			if(shipBuildCamera == null || ((FixedViewer) shipBuildCamera.getViewable()).getEntity() != edit.getSegmentController()) {
				if(edit.getSegmentController() != null) {
					Transform t = new Transform(edit.getSegmentController().getWorldTransform());
					GlUtil.setUpVector(new Vector3f(0, 1, 0), t);
					GlUtil.setRightVector(new Vector3f(1, 0, 0), t);
					GlUtil.setForwardVector(new Vector3f(0, 0, 1), t);
					// Matrix3f up = new Matrix3f();
					// Matrix3f left = new Matrix3f();
					// up.setIdentity();
					// left.setIdentity();
					// up.rotZ(0.5f);
					// left.rotY(0.1f);
					// t.basis.mul(up);
					// t.basis.mul(left);
					shipBuildCamera = new BuildShipCamera(getState(), Controller.getCamera(), edit, INITAL_BUILD_CAM_DIST, null);
					if(edit.getEntered() != null) {
						Vector3i absolutePos = edit.getEntered().getAbsolutePos(new Vector3i());
						absolutePos.z -= SegmentData.SEG_HALF;
						shipBuildCamera.jumpToInstantly(absolutePos);
					} else {
						shipBuildCamera.jumpToInstantly(new Vector3i(SegmentData.SEG_HALF, SegmentData.SEG_HALF, 0));
					}
					shipBuildCamera.setCameraStartOffset(0);
					// shipBuildCamera.getRotation().set(0,90,0);
				} else if(shipBuildCamera != null) {
					shipBuildCamera.resetTransition(Controller.getCamera());
				}
			} else if(shipBuildCamera != null) {
				shipBuildCamera.resetTransition(Controller.getCamera());
			}
			// if(edit.getSegmentController() != null){
			// //				System.err.println("SWITCHED TO BUILDMODE OF "+edit.getSegmentController());
			// getState().getWorldDrawer().flagJustEntered(edit.getSegmentController());
			// }
			getState().getController().timeOutBigMessage(Lng.str("Flight Mode"));
			getState().getController().showBigMessage("Build Mode", Lng.str("Build Mode"), Lng.str("(Press %s to switch to FLIGHT MODE; press %s to exit structure)", KeyboardMappings.CHANGE_SHIP_MODE.getKeyChar(), KeyboardMappings.ENTER_SHIP.getKeyChar()), 0);
			assert (edit.getSegmentController() != null);
			Controller.setCamera(shipBuildCamera);
		} else {
			getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager().save(getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager().user);
		}
		super.onSwitch(active);
	}

	@Override
	public void update(Timer timer) {
		if(selectedBlock != null) {
			selectedBlock.refresh();
			if(selectedBlock.getType() == Element.TYPE_NONE) {
				selectedBlock = null;
			}
		}
		if(controllersDirty) {
			// if(edit.getSegmentController().getControlElementMap().getControllingMap().size() != controllers.size()){
			controllers.clear();
			for(long s : edit.getSegmentController().getControlElementMap().getControllingMap().keySet()) {
				controllers.add(ElementCollection.getPosFromIndex(s, new Vector3i()));
			}
			// }
			controllersDirty = false;
		}
		CameraMouseState.setGrabbed(!PlayerInteractionControlManager.isAdvancedBuildMode(getState()));
		if(edit.getSegmentController() != null) {
			updateNearsetIntersection();
		}
	}

	private void removeButtonPressed(KeyEventInterface e) {
		Vector3f pos = new Vector3f(Controller.getCamera().getPos());
		Vector3f dir = new Vector3f(Controller.getCamera().getForward());
		if(PlayerInteractionControlManager.isAdvancedBuildMode(getState())) {
			dir = new Vector3f(getState().getWorldDrawer().getAbsoluteMousePosition());
			dir.sub(pos);
		}
		dir.normalize();
		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().removeBlock(edit.getSegmentController(), pos, dir, selectedBlock, EDIT_DISTANCE, getSymmetryPlanes(), Element.TYPE_ALL, new RemoveCallback() {

			@Override
			public long getSelectedControllerPos() {
				if(selectedBlock != null) {
					return selectedBlock.getAbsoluteIndex();
				}
				return Long.MIN_VALUE;
			}

			@Override
			public void onRemove(long pos, short type) {
				getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildCommandManager().onRemovedBlock(pos, type);
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUILD, AudioTags.BLOCK, AudioTags.REMOVE)*/
				AudioController.fireAudioEventID(972);
			}
		});
	}

	private void buildButtonPressed(KeyEventInterface e) {
		Vector3f pos = new Vector3f(Controller.getCamera().getPos());
		Vector3f dir = new Vector3f(Controller.getCamera().getForward());
		if(PlayerInteractionControlManager.isAdvancedBuildMode(getState())) {
			dir = new Vector3f(getState().getWorldDrawer().getAbsoluteMousePosition());
			dir.sub(pos);
		}
		dir.normalize();
		PlayerInteractionControlManager inter = getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
		InventorySlot slot = getState().getPlayer().getInventory().getSlot(inter.getSelectedSlot());
		short typeSelected = inter.getSelectedTypeWithSub();
		if(slot != null && inter.checkRadialSelect(slot.getType())) {
			return;
		}
		inter.buildBlock(edit.getSegmentController(), pos, dir, new BuildCallback() {

			@Override
			public long getSelectedControllerPos() {
				if(selectedBlock != null) {
					return selectedBlock.getAbsoluteIndex();
				}
				return Long.MIN_VALUE;
			}

			@Override
			public void onBuild(Vector3i posBuilt, Vector3i posNextToBuild, short typeBuilt) {
				getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildCommandManager().onBuiltBlock(posBuilt, posNextToBuild, typeBuilt);
				if(ElementKeyMap.getInfo(typeSelected).getControlledBy().contains(ElementKeyMap.CORE_ID)) {
					if(EngineSettings.G_AUTOSELECT_CONTROLLERS.isOn() && ElementKeyMap.getInfo(typeSelected).getControlling().size() > 0) {
						SegmentPiece pointUnsave = edit.getSegmentController().getSegmentBuffer().getPointUnsave(posBuilt);
						if(pointUnsave != null) {
							selectedBlock = pointUnsave;
						}
					}
				} else if(selectedBlock != null && posBuilt != null) {
					selectedBlock.refresh();
					if(selectedBlock.getType() != Element.TYPE_NONE) {
					} else {
						if(!ElementKeyMap.getInfo(typeSelected).isRailTrack() && !ElementKeyMap.getInfo(typeSelected).isSignal() && ElementKeyMap.getInfo(typeSelected).getControlledBy().size() > 0) {
							if(System.currentTimeMillis() - lastNoteContBlock > 1000 * 60 * 20) {
								getState().getController().popupInfoTextMessage(Lng.str("Note:\nYou placed a controllable Block\nWithout having a conroller selected!\nE.g. if you place weapons, please\nselect/place the weapon computer\nfirst."), 0);
								lastNoteContBlock = System.currentTimeMillis();
							}
						}
					}
				} else if(selectedBlock == null && ElementKeyMap.getInfo(typeSelected).getControlledBy().size() > 0) {
					if(!ElementKeyMap.getInfo(typeSelected).isRailTrack() && !ElementKeyMap.getInfo(typeSelected).isSignal() && System.currentTimeMillis() - lastNoteContBlock > 1000 * 60 * 20) {
						getState().getController().popupInfoTextMessage(Lng.str("Note:\nYou placed a controllable Block\nWithout having a conroller selected!\nE.g. if you place weapons, please\nselect/place the weapon computer\nfirst."), 0);
						lastNoteContBlock = System.currentTimeMillis();
					}
				}
				getState().getPlayer().fireMusicTag(MusicTags.BUILDING);

				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUILD, AudioTags.BLOCK, AudioTags.ADD)*/
				AudioController.fireAudioEventID(973);
			}
		}, new DimensionFilter(), getSymmetryPlanes(), EDIT_DISTANCE);
	}

	private void jumpToCurrentBlock() {
		shipBuildCamera.jumpToInstantly(new Vector3i(SegmentData.SEG_HALF, SegmentData.SEG_HALF, 0));
	}

	@Override
	public void notifyElementChanged() {
		controllersDirty = true;
		getState().getWorldDrawer().getBuildModeDrawer().flagControllerSetChanged();
	}

	public void reset() {
	}

	private void selectCameraBlock() {
		if(elementIntersectionExists) {
			if(currentIntersectionElement != null) {
				if(selectedBlock != null && selectedBlock.getAbsolutePos(new Vector3i()).equals(currentIntersectionElement)) {
					selectedBlock = null;
				} else {
					selectedBlock = edit.getSegmentController().getSegmentBuffer().getPointUnsave(currentIntersectionElement);
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUILD, AudioTags.BLOCK, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(974);
					if(selectedBlock != null && getState().getController().getTutorialMode() != null && getState().getController().getTutorialMode().getMachine().getFsm().getCurrentState() instanceof ConnectedFromToTestState cc) {
						if(!cc.checkConnectBlock(selectedBlock, getState())) {
							selectedBlock = null;
						}
					}
				}
			}
			//INSERTED CODE
			ClientSelectSegmentPieceEvent event = new ClientSelectSegmentPieceEvent(selectedBlock, ClientSelectSegmentPieceEvent.Context.BUILD_MODE);
			StarLoader.fireEvent(event, false);
			///
		}
	}

	private void selectControllerBlock(boolean b) {
		if(controllers.isEmpty()) {
			return;
		}
		controllerIndex = FastMath.cyclicModulo(controllerIndex + (b ? 1 : -1), controllers.size() - 1);
		System.err.println("SWITCH " + b + " " + controllerIndex);
		SegmentPiece block = edit.getSegmentController().getSegmentBuffer().getPointUnsave(controllers.get(controllerIndex));
		if(block != null) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUILD, AudioTags.BLOCK, AudioTags.SELECT)*/
			AudioController.fireAudioEventID(975);
			selectedBlock = block;
		}
	}

	private void selectCoreBlock(boolean b) {
		if(controllers.isEmpty()) {
			return;
		}
		SegmentPiece block = edit.getSegmentController().getSegmentBuffer().getPointUnsave(edit.getCore());
		if(block != null) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUILD, AudioTags.BLOCK, AudioTags.SELECT)*/
			AudioController.fireAudioEventID(976);
			selectedBlock = block;
			controllerIndex = Math.max(0, controllers.indexOf(edit.getCore()));
		}
	}

	private void updateNearsetIntersection() {
		Vector3f camPos = new Vector3f(Controller.getCamera().getPos());
		Vector3f forw = new Vector3f(Controller.getCamera().getForward());
		if(Float.isNaN(forw.x)) {
			return;
		}
		if(PlayerInteractionControlManager.isAdvancedBuildMode(getState())) {
			Vector3f mouseTo = new Vector3f(getState().getWorldDrawer().getAbsoluteMousePosition());
			forw.sub(mouseTo, camPos);
		}
		forw.normalize();
		forw.scale(EDIT_DISTANCE);
		Vector3f camTo = new Vector3f(camPos);
		camTo.add(forw);
		currentNearestCollision = ((PhysicsExt) getState().getPhysics()).testRayCollisionPoint(camPos, camTo, false, null, edit.getSegmentController(), true, true, false);
		if(currentNearestCollision != null && currentNearestCollision.hasHit() && currentNearestCollision instanceof CubeRayCastResult c) {
			if(c.getSegment() != null && c.getCubePos() != null) {
				c.getSegment().getAbsoluteElemPos(c.getCubePos(), currentIntersectionElement);
				currentElementType = c.getSegment().getSegmentData().getType(c.getCubePos());
				cachedLastSegment = c.getSegment();
				currentSegmentPiece = new SegmentPiece(c.getSegment(), c.getCubePos());
				elementIntersectionExists = true;
			} else {
				cachedLastSegment = null;
				currentSegmentPiece = null;
				elementIntersectionExists = false;
			}
		} else {
			cachedLastSegment = null;
			currentSegmentPiece = null;
			elementIntersectionExists = false;
		}
	}

	/**
	 * @return the currentSegmentPiece
	 */
	public SegmentPiece getCurrentSegmentPiece() {
		return currentSegmentPiece;
	}

	public BuildShipCamera getShipBuildCamera() {
		return shipBuildCamera;
	}
}
