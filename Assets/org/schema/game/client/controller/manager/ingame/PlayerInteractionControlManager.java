package org.schema.game.client.controller.manager.ingame;

import api.common.GameClient;
import api.element.block.Blocks;
import api.listener.events.block.SegmentPieceActivateByPlayer;
import api.mod.StarLoader;
import api.utils.SegmentPieceUtils;
import api.utils.game.SegmentControllerUtils;
import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.lwjgl.glfw.GLFW;
import org.schema.common.FastMath;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.*;
import org.schema.game.client.controller.element.world.ClientSegmentProvider;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.controller.manager.ingame.character.PlayerExternalController;
import org.schema.game.client.controller.manager.ingame.faction.FactionBlockDialog;
import org.schema.game.client.controller.manager.ingame.navigation.NavigationControllerManager;
import org.schema.game.client.controller.manager.ingame.ship.InShipControlManager;
import org.schema.game.client.controller.tutorial.states.PlaceElementOnLastSpawnedTestState;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.BuildModeDrawer;
import org.schema.game.client.view.buildhelper.BuildHelper;
import org.schema.game.client.view.camera.ObjectViewerCamera;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;
import org.schema.game.client.view.gui.PlayerPanel;
import org.schema.game.client.view.gui.RadialMenuDialogShapes;
import org.schema.game.client.view.gui.buildtools.BuildConstructionCommand;
import org.schema.game.client.view.gui.buildtools.BuildConstructionManager;
import org.schema.game.client.view.gui.buildtools.GUIOrientationSettingElement;
import org.schema.game.client.view.gui.factorymanager.FactoryManagerDialog;
import org.schema.game.client.view.gui.fleetmanager.FleetManagerDialog;
import org.schema.game.client.view.gui.reactor.ReactorTreeDialog;
import org.schema.game.client.view.textbox.Replacements;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.ai.*;
import org.schema.game.common.controller.elements.*;
import org.schema.game.common.controller.elements.jumpdrive.JumpDriveElementManager;
import org.schema.game.common.controller.elements.jumpprohibiter.JumpInhibitorElementManager;
import org.schema.game.common.controller.elements.lift.LiftCollectionManager;
import org.schema.game.common.controller.elements.power.PowerAddOn;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;
import org.schema.game.common.controller.elements.powerbattery.PowerBatteryUnit;
import org.schema.game.common.controller.elements.spacescanner.LongRangeScannerElementManager;
import org.schema.game.common.controller.elements.shipyard.ShipyardElementManager;
import org.schema.game.common.controller.elements.structurescanner.StructureScannerElementManager;
import org.schema.game.common.controller.elements.transporter.TransporterCollectionManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.SendableGameState;
import org.schema.game.common.data.VoidUniqueSegmentPiece;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.*;
import org.schema.game.common.data.element.quarters.Quarter;
import org.schema.game.common.data.physics.CubeRayCastResult;
import org.schema.game.common.data.physics.PhysicsExt;
import org.schema.game.common.data.player.PlayerControlledTransformableNotFound;
import org.schema.game.common.data.player.SimplePlayerCommands;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.inventory.InventoryHolder;
import org.schema.game.common.data.player.inventory.InventorySlot;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.network.objects.DragDrop;
import org.schema.game.network.objects.remote.RemoteDragDrop;
import org.schema.game.network.objects.remote.RemoteTextBlockPair;
import org.schema.game.network.objects.remote.TextBlockPair;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.camera.viewer.FixedViewer;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.font.FontStyle;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.newgui.DialogInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.util.WorldToScreenConverter;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.Keyboard;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import java.util.*;

public class PlayerInteractionControlManager extends AbstractControlManager {
	public static final long MENU_DELAY_MS = 200;
	private static final Transform where = new Transform();
	private final List<BuildInstruction> undo = new ObjectArrayList<>(30);
	private final List<BuildInstruction> redo = new ObjectArrayList<>(30);
	private final IntOpenHashSet warned = new IntOpenHashSet();
	private final Object2IntOpenHashMap<BlockStyle> blockStyleMap = new Object2IntOpenHashMap<>();
	private final Vector3i mPos = new Vector3i();
	private final Set<Segment> moddedSegs = new ObjectOpenHashSet<>(16);
	private final BuildConstructionManager buildCommandManager;
	private int selectedSlot;
	private InShipControlManager inShipControlManager;
	private SegmentControlManager segmentControlManager;
	private PlayerExternalController playerCharacterManager;
	private SimpleTransformableSendableObject selectedEntity;
	private BuildToolsManager buildToolsManager;
	private HotbarLayout hotbarLayout;
	private int blockOrientation = Element.FRONT;
	private short lastSelectedType;
	private AiInterfaceContainer selectedCrew;
	private String lastPowerMsg;
	private String lastShieldMsg;
	private SegmentPiece entered;
	private int selectedSubSlot;
	private boolean firstWarning;
	private float undoRedoCooldown = -1;
	private Camera lastCamera;
	private Camera lookCamera;
	private int lastSelectedSlabOrient;
	private short forcedSelect;
	private long lastPressedAdvBuildModeButton;
	private boolean stickyAdvBuildMode;
	private boolean stacked;
	private SimpleTransformableSendableObject selectedAITarget;
	private boolean hasNotMadePin;

	public PlayerInteractionControlManager(GameClientState state) {
		super(state);
		initialize();
		buildCommandManager = new BuildConstructionManager(state);
	}

	public void initialize() {
		inShipControlManager = new InShipControlManager(getState());
		playerCharacterManager = new PlayerExternalController(getState());
		segmentControlManager = new SegmentControlManager(getState());
		buildToolsManager = new BuildToolsManager(getState());
		hotbarLayout = new HotbarLayout(getState());
		getControlManagers().add(buildToolsManager);
		getControlManagers().add(inShipControlManager);
		getControlManagers().add(playerCharacterManager);
		getControlManagers().add(segmentControlManager);
	}

	public static SimpleTransformableSendableObject<?> getLookingAt(GameClientState state, boolean respectFilters) {
		return getLookingAt(state, respectFilters, 250, false, 0, false);
	}

	public static SimpleTransformableSendableObject<?> getLookingAt(GameClientState state, boolean respectFilters, float distFromMid, boolean useDotDist, float dotDirDist, boolean selectClosest) {
		return getLookingAt(state, respectFilters, false, distFromMid, useDotDist, dotDirDist, selectClosest);
	}

	public static SimpleTransformableSendableObject<?> getLookingAt(GameClientState state, boolean respectFilters, boolean prioritizeAlreadySelected, float distFromMid, boolean useDotDist, float dotDirDist, boolean selectClosest) {
		if(state.getScene() == null || state.getScene().getWorldToScreenConverter() == null) {
			return null;
		}
		WorldToScreenConverter worldToScreenConverter = state.getScene().getWorldToScreenConverter();
		Vector3f distFromObj = new Vector3f();
		Vector3f dir = new Vector3f();
		float minDist = 0;
		float minDist2 = -1;
		SimpleTransformableSendableObject nearest = null;
		SimpleTransformableSendableObject selected = state.getSelectedEntity();
		Vector3f middle = worldToScreenConverter.getMiddleOfScreen(new Vector3f());
		for(SimpleTransformableSendableObject to : state.getCurrentSectorEntities().values()) {
			if(to == state.getCurrentPlayerObject() || to.isHidden()) {
				continue;
			}
			PlayerInteractionControlManager ig = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
			if(respectFilters && !ig.getNavigationControllerManager().isDisplayed(to)) {
				continue;
			}
			if(state.isInCharacterBuildMode() && state.getCharacter().getGravity().source == to) {
				// use core
				where.set(to.getWorldTransformOnClient());
			} else {
				to.getWorldTransformOnClientCenterOfMass(where);
			}
			// System.err.println("TESTING "+to);
			dir.set(where.origin);
			// if (state.getCurrentPlayerObject() != null) {
			// dir.sub(where.origin);
			// } else {
			dir.sub(Controller.getCamera().getPos());
			// }
			dir.normalize();
			Vector3f facingDir = Controller.getCamera().getForward();
			facingDir.normalize();
			// float camToPosLen = dir.length();
			Vector3f posOnScreen = worldToScreenConverter.convert(where.origin, new Vector3f(), true);
			Vector3f distFromMiddle = new Vector3f();
			distFromMiddle.sub(posOnScreen, middle);
			distFromObj.sub(state.getCurrentPlayerObject().getWorldTransformOnClient().origin, where.origin);
			// System.err.println("DIST FROM MIDDLE: "+distFromMiddle.length());
			if(distFromMiddle.length() < distFromMid && (!useDotDist || facingDir.dot(dir) > dotDirDist)) {
				if(prioritizeAlreadySelected && to.equals(selected)) {
					return selected; //no need to keep looking
				} else if(nearest == null || (distFromMiddle.length() < minDist && (!selectClosest || distFromObj.length() < minDist2))) {
					nearest = to;
					minDist = distFromMiddle.length();
					minDist2 = distFromObj.length();
				}
			}
		}
		return nearest;
	}

	public int buildBlock(final EditableSendableSegmentController controller, Vector3f pos, Vector3f dir, BuildCallback callback, DimensionFilter dimensionFilter, SymmetryPlanes symmetryPlanes, float editDistance) {
		short type = getSelectedType();
		stacked = false;
		type = checkCanBuild(controller, symmetryPlanes, type);
		if(!stacked && buildToolsManager.slabSize.setting > 0 && ElementKeyMap.isValidType(type) && ElementKeyMap.getInfoFast(type).slab == 0 && ElementKeyMap.getInfoFast(type).slabIds != null && ElementKeyMap.getInfoFast(type).slabIds.length == 3) {
			type = ElementKeyMap.getInfoFast(type).slabIds[buildToolsManager.slabSize.setting - 1];
		}
		final short origType = type;
		Vector3i size = new Vector3i(1, 1, 1);
		if(PlayerInteractionControlManager.isAdvancedBuildMode(getState())) {
			size.set(buildToolsManager.getSize());
		}
		if(buildToolsManager.isCopyMode()) {
			symmetryPlanes.setPlaceMode(SymmetryPlanes.MODE_COPY);
		} else if(buildToolsManager.isPasteMode()) {
			symmetryPlanes.setPlaceMode(SymmetryPlanes.MODE_PASTE);
		} else if(buildToolsManager.getBuildHelper() != null && !buildToolsManager.getBuildHelper().placed) {
			symmetryPlanes.setPlaceMode(SymmetryPlanes.MODE_HELPER_PLACE);
		}
		if(type == Element.TYPE_NONE || symmetryPlanes.getPlaceMode() > 0) {
			if(symmetryPlanes.getPlaceMode() == 0) {
				getState().getController().popupInfoTextMessage(Lng.str("No element available to build!\nSelected slot is empty!"), 0);
			} else {
				SegmentPiece piece;
				try {
					int placeMode = symmetryPlanes.getPlaceMode();
					BuildInstruction buildInstruction = new BuildInstruction(controller);
					if(buildToolsManager.isCopyMode() || (buildToolsManager.getBuildHelper() != null && !buildToolsManager.getBuildHelper().placed)) {
						piece = controller.getNearestPiece(pos, dir, editDistance, size, size);
					} else {
						piece = controller.getNextToNearestPiece(pos, dir, new Vector3i(), editDistance, new Vector3i(), new Vector3i());
					}
					// place symmetry plane without block selected
					if(placeMode > 0 && piece != null) {
						Vector3i p = piece.getAbsolutePos(new Vector3i());
						switch(placeMode) {
							case (SymmetryPlanes.MODE_XY):
								symmetryPlanes.getXyPlane().z = p.z;
								symmetryPlanes.setXyPlaneEnabled(true);
								break;
							case (SymmetryPlanes.MODE_XZ):
								symmetryPlanes.getXzPlane().y = p.y;
								symmetryPlanes.setXzPlaneEnabled(true);
								break;
							case (SymmetryPlanes.MODE_YZ):
								symmetryPlanes.getYzPlane().x = p.x;
								symmetryPlanes.setYzPlaneEnabled(true);
								break;
							case (SymmetryPlanes.MODE_COPY):
								if(buildToolsManager.isSelectMode()) {
									System.out.println("COPY Case mode_copy");
									// getBuildToolsManager().setCopyMode(false);
								} else {
									if(buildToolsManager.selectionPlaced) {
										// cam selection was made. do not overwrite with normal copy
										System.out.println("COPY Case mode_copy no sel");
										buildToolsManager.selectionPlaced = false;
										return 0;
									} else {
										System.err.println("[CLIENT] COPY AREA SET " + p);
										buildToolsManager.setCopyArea(controller, p, size);
										buildToolsManager.setCopyMode(false);
										symmetryPlanes.setPlaceMode(0);
										playerCharacterManager.getSymmetryPlanes().setPlaceMode(0);
									}
								}
								/*
								System.err.println("COPY AREA SET " + p);
								getBuildToolsManager().setCopyArea(controller,
									buildToolsManager.selectionBoxA, buildToolsManager.selectionBoxB);
								 */
								// System.err.println("COPY AREA SET " + p);
								// getBuildToolsManager().setCopyArea(controller, p, size);
								return 0;
							case (SymmetryPlanes.MODE_PASTE):
								if(buildToolsManager.canPaste()) {
									buildToolsManager.getCopyArea().build(controller, p, buildInstruction, symmetryPlanes);
									undo.add(0, buildInstruction);
									while(undo.size() > EngineSettings.B_UNDO_REDO_MAX.getInt()) {
										undo.remove(undo.size() - 1);
									}
								}
							case (SymmetryPlanes.MODE_HELPER_PLACE):
								if(buildToolsManager.getBuildHelper() != null) {
									buildToolsManager.getBuildHelper().placed = true;
									Vector3f lpos = new Vector3f(p.x - SegmentData.SEG_HALF, p.y - SegmentData.SEG_HALF, p.z - SegmentData.SEG_HALF);
									buildToolsManager.getBuildHelper().localTransform.origin.set(lpos);
									buildToolsManager.getBuildHelper().placedPos = new Vector3i(p);
								}
								break;
						}
						symmetryPlanes.setPlaceMode(0);
						buildToolsManager.setCopyMode(false);
					} else {
						System.err.println("[CLIENT] NO NEAREST PIECE TO BUILD ON");
					}
				} catch(ElementPositionBlockedException e) {
					e.printStackTrace();
				} catch(BlockNotBuildTooFast e) {
					e.printStackTrace();
				}
			}
			return 0;
		}
		// don't build blocks when you're in selection mode
		if(buildToolsManager.isSelectMode()) {
			return 0;
		} else if(buildToolsManager.selectionPlaced) {
			buildToolsManager.selectionPlaced = false;
			return 0;
		}
		if(getState().getPlayer().getInventory(null).isSlotEmpty(selectedSlot)) {
			if(symmetryPlanes.getPlaceMode() == 0) {
				getState().getController().popupInfoTextMessage(Lng.str("No element available to build!\nSelected slot is empty!"), 0);
			}
			return 0;
		}
		assert (getState().getPlayer().getInventory(null).getCount(selectedSlot, origType) > 0) : ElementKeyMap.toString(origType);
		int count = getState().getPlayer().getInventory(null).getCount(selectedSlot, origType);
		try {
			ElementInformation info = ElementKeyMap.getInfo(type);
			if(controller.isUsingOldPower() && (info.isReactorChamberAny() || info.getId() == ElementKeyMap.REACTOR_MAIN || info.getId() == ElementKeyMap.REACTOR_CONDUIT || info.getId() == ElementKeyMap.REACTOR_STABILIZER)) {
				String helpMessage = Lng.str("This entity has %s old power blocks and will not allow the new reactor system to be placed down as long as any old power blocks remain.", controller.getElementClassCountMap().get(ElementKeyMap.POWER_ID_OLD) + controller.getElementClassCountMap().get(ElementKeyMap.POWER_CAP_ID));
				if(controller.isDocked()) {
					helpMessage = Lng.str("This entity or one of the entities it is docked to, has old power blocks and will not allow the new reactor system to be placed down as long as any old power blocks remain.");
				}
				PlayerContextHelpDialog d = new PlayerContextHelpDialog(getState(), EngineSettings.CONTEXT_HELP_PLACED_NEW_REACTOR_ON_OLD_SHIP, helpMessage, true);
				d.ignoreToggle = false;
				d.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(156);
				return 0;
			}
			if(controller.hasAnyReactors() && (info.getId() == ElementKeyMap.POWER_ID_OLD || info.getId() == ElementKeyMap.POWER_CAP_ID)) {
				PlayerContextHelpDialog d = new PlayerContextHelpDialog(getState(), EngineSettings.CONTEXT_HELP_PLACED_OLD_POWER_ON_NEW_SHIP, Lng.str("This entity has new reactor blocks and will not allow the old power system to be placed down as long as any new reactor blocks remain."), true);
				d.ignoreToggle = false;
				d.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(157);
				return 0;
			}
			if((info.isReactorChamberAny() || info.getId() == ElementKeyMap.REACTOR_MAIN || info.getId() == ElementKeyMap.REACTOR_CONDUIT) && controller instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) controller).getManagerContainer().getPowerInterface().isAnyRebooting()) {
				getState().getController().popupAlertTextMessage(Lng.str("Cannot modify reactor configuration while a reactor is rebooting!"));
				return 0;
			}
			if(EngineSettings.CONTEXT_HELP_STABILIZER_EFFICIENCY_PLACE.isOn()) {
				if(type == ElementKeyMap.REACTOR_STABILIZER && BuildModeDrawer.currentStabEfficiency < 1.0) {
					String eff = StringTools.formatPointZero(BuildModeDrawer.currentStabEfficiency * 100d);
					PlayerContextHelpDialog d = new PlayerContextHelpDialog(getState(), EngineSettings.CONTEXT_HELP_STABILIZER_EFFICIENCY_PLACE, Lng.str("You are placing a Stabilizer with %s%%. " + "Make sure that the stabilizers are far enough from any reactor (active or inactive). " + "The minimum distance for 100%% is displayed on the block context help.", eff), true);
					d.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(158);
					return 0;
				}
			}
			if(EngineSettings.CONTEXT_HELP_PLACE_MODULE_WITHOUT_COMPUTER_WARNING.isOn()) {
				SegmentPiece sel = getSelectedBlockByActiveController();
				if(info.needsComputer()) {
					if(sel == null || info.getComputer() != sel.getType()) {
						PlayerContextHelpDialog d = new PlayerContextHelpDialog(getState(), EngineSettings.CONTEXT_HELP_PLACE_MODULE_WITHOUT_COMPUTER_WARNING, Lng.str("This block needs a connection to a computer!\nPlease place a '%s' first, or press [%s] on an existing one before placing this block", ElementKeyMap.getInfoFast(info.getComputer()).getName(), KeyboardMappings.SELECT_MODULE.getKeyChar()), true);
						d.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(160);
						return 0;
					}
				} else if(info.getId() == ElementKeyMap.CARGO_SPACE) {
					if(sel == null || !ElementKeyMap.isValidType(sel.getType()) || !ElementKeyMap.getInfoFast(sel.getType()).isInventory()) {
						PlayerContextHelpDialog d = new PlayerContextHelpDialog(getState(), EngineSettings.CONTEXT_HELP_PLACE_MODULE_WITHOUT_COMPUTER_WARNING, Lng.str("Cargo needs a connection to an inventory!\nPlease place a block with an inventory first (e.g. %s), or press [%s] on an existing one before placing this block", ElementKeyMap.getInfoFast(ElementKeyMap.STASH_ELEMENT).getName(), KeyboardMappings.SELECT_MODULE.getKeyChar()), true);
						d.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(159);
						return 0;
					}
				}
			}
			if(EngineSettings.CONTEXT_HELP_PLACE_REACTOR_WITH_LOW_STABILIZATION.isOn()) {
				if(info.id == ElementKeyMap.REACTOR_MAIN && controller instanceof ManagedSegmentController<?>) {
					ManagerContainer<?> mc = ((ManagedSegmentController<?>) controller).getManagerContainer();
					if(mc.getPowerInterface().getStabilizerEfficiencyTotal() < 0.5) {
						PlayerContextHelpDialog d = new PlayerContextHelpDialog(getState(), EngineSettings.CONTEXT_HELP_PLACE_REACTOR_WITH_LOW_STABILIZATION, Lng.str("""
								Warning: Your reactor stabilization is less than 50%. This negatively impacts the performance of your reactor, and causes extra damage to your reactor when it is hit.

								Place some stabilizers to increase your stabilization and increase power regen."""), true);
						d.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(161);
						return 0;
					}
				}
			}
			if(EngineSettings.CONTEXT_HELP_PLACE_CHAMBER_WITHOUT_CONDUIT_WARNING.isOn()) {
				if(info.isReactorChamberGeneral()) {
					final short fType = type;
					if(!surroundCondition(controller, pos, dir, editDistance, size, info12 -> {
						System.err.println(info12 + " INFO SPEC " + info12.isReactorChamberSpecific() + "; placing " + ElementKeyMap.toString(fType) + "; block root: " + info12.chamberRoot + "; " + info12.isReactorChamberGeneral() + "; " + fType + "; " + info12.id);
						return info12.id == ElementKeyMap.REACTOR_CONDUIT || (info12.isReactorChamberGeneral() && fType == info12.getId()) || (info12.isReactorChamberSpecific() && fType == info12.chamberRoot);
					})) {
						// block was not placed onto a conduit or compatible chamber
						PlayerContextHelpDialog d = new PlayerContextHelpDialog(getState(), EngineSettings.CONTEXT_HELP_PLACE_CHAMBER_WITHOUT_CONDUIT_WARNING, Lng.str("Warning: There is no conduit block next to this chamber block to connect it to your reactor tree.\n" + "A chamber needs to be next to a line of conduit blocks to connect it to either the main reactor or to another group of chambers.\n\n" + "You can also add chamber blocks to existing groups of the same general type (e.g. adding stealth chambers to a group of cloak chamber blocks).\n\n" + "Chamber systems are structured like a tree: the tree trunk is the main reactor, and the branches connect the chambers with conduits.\n" + "Chamber blocks themselves can be placed next to each other of the same type to increase their size.\n"), true);
						d.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(162);
						return 0;
					}
				}
			}
			if(EngineSettings.CONTEXT_HELP_PLACE_CONDIUT_WITHOUT_CHAMBER_OR_MAIN_WARNING.isOn()) {
				if(info.id == ElementKeyMap.REACTOR_CONDUIT) {
					final short fType = type;
					if(!surroundCondition(controller, pos, dir, editDistance, size, info1 -> info1.id == ElementKeyMap.REACTOR_CONDUIT || info1.id == ElementKeyMap.REACTOR_MAIN || info1.isReactorChamberAny())) {
						// block was not placed onto a conduit or compatible chamber
						PlayerContextHelpDialog d = new PlayerContextHelpDialog(getState(), EngineSettings.CONTEXT_HELP_PLACE_CONDIUT_WITHOUT_CHAMBER_OR_MAIN_WARNING, Lng.str("A conduit connects chambers to a main reactor, or to another chamber in the tree.\n\n" + "Build lines of conduits to create a tree between your chambers and your main reactor.\n" + "Then open the radial menu to access your reactor under ship/station -> reactor or press %s when looking at the reactor.", KeyboardMappings.ACTIVATE.getKeyChar()), true);
						d.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(163);
						return 0;
					}
				}
			}
			if(info.getBlockStyle() != BlockStyle.NORMAL) {
				System.err.println("[CLIENT] BLOCK style: " + info.getBlockStyle() + "; ORIENTATION: " + blockOrientation + "; " + BlockShapeAlgorithm.algorithms[info.getBlockStyle().id - 1][blockOrientation].toString());
			}
			BlockOrientation o = ElementInformation.convertOrientation(info, (byte) blockOrientation);
			BuildHelper posesFilter = null;
			if(buildToolsManager.getBuildHelper() != null && buildToolsManager.buildHelperReplace) {
				posesFilter = buildToolsManager.getBuildHelper();
			}
			BuildInstruction buildInstruction = new BuildInstruction(controller);
			int placed = controller.getNearestIntersection(type, pos, dir, callback, o.blockOrientation, o.activateBlock, dimensionFilter, size, count, editDistance, symmetryPlanes, posesFilter, buildInstruction);
			// System.err.println("PLACING: BLOCK WIRH ORIENTATION: "+elementOrientation+" placed: "+placed);
			if(placed > 0) {
				undo.add(0, buildInstruction);
				while(undo.size() > EngineSettings.B_UNDO_REDO_MAX.getInt()) {
					undo.remove(undo.size() - 1);
				}
				// When a block is removed, remove any previous redos
				redo.clear();
				//AudioController.fireAudioEvent(AudioTags.BLOCK, AudioTags.BUILD, AudioTags.ADD);
				//AudioController.fireAudioEventID(962);
				//AudioController.fireAudioEventID(164);
				if(type == ElementKeyMap.GRAVITY_ID) {
					getState().getController().popupInfoTextMessage(Lng.str("sINFO:\nUse gravity when you are\noutside the ship. Look at\na gravity module and press %s.", KeyboardMappings.ACTIVATE.getKeyChar()), 0);
				}
				return placed;
			}
		} catch(ElementPositionBlockedException e) {
			String reason = Lng.str("unknown reason");
			if(e.userData != null) {
				if(e.userData instanceof SegmentController) {
					reason = ((SegmentController) e.userData).getRealName();
				} else if(e.userData instanceof SimpleTransformableSendableObject) {
					reason = ((SimpleTransformableSendableObject) e.userData).toNiceString();
				} else if(e.userData instanceof String) {
					reason = (String) e.userData;
				}
			}
			getState().getController().popupAlertTextMessage(Lng.str("Cannot build here!\nPosition is blocked by\n%s", reason), 0);
		} catch(BlockedByDockedElementException e) {
			getState().getWorldDrawer().getBuildModeDrawer().addBlockedDockIndicator(e.to, null, null);
			getState().getController().popupAlertTextMessage(Lng.str("Cannot build here!\nPosition blocked\nby active docking area!"), 0);
		} catch(BlockNotBuildTooFast e) {
			getState().getController().popupAlertTextMessage(Lng.str("Block not placed.\nArea not initialized yet.\nPlease build slower.."), 0);
		}
		return 0;
	}

	private short getSelectedType() {
		return getState().getPlayer().getInventory().getType(selectedSlot);
	}

	public short checkCanBuild(final EditableSendableSegmentController controller, SymmetryPlanes symmetryPlanes, short type) {
		if(!getState().getController().allowedToEdit(controller)) {
			return 0;
		}
		if(controller.isInTestSector()) {
			getState().getController().popupInfoTextMessage(Lng.str("Cannot build in test sector!"), 0);
			return 0;
		}
		if(!isAllowedToBuildAndSpawnShips()) {
			getState().getController().popupAlertTextMessage(Lng.str("ERROR\n \nCan't do that!\nYou are a spectator!"), 0);
			return 0;
		}
		if(getState().getController().getTutorialMode() != null && getState().getController().getTutorialMode().getMachine().getFsm().getCurrentState() instanceof PlaceElementOnLastSpawnedTestState) {
			if(type == ElementKeyMap.WEAPON_ID) {
				if(inShipControlManager.getShipControlManager().getSegmentBuildController().getSelectedBlock() == null || inShipControlManager.getShipControlManager().getSegmentBuildController().getSelectedBlock().getType() != ElementKeyMap.WEAPON_CONTROLLER_ID) {
					System.err.println("[TUTORIAL] cant place because not selected cannon computer: " + inShipControlManager.getShipControlManager().getSegmentBuildController().getSelectedBlock());
					getState().getController().popupAlertTextMessage(Lng.str("Please place the cannon\ncomputer first or else your\nweapons aren't connected.\n(you'll learn manual later)"), 0);
					return 0;
				}
			}
		}
		stacked = false;
		if(type == InventorySlot.MULTI_SLOT) {
			List<InventorySlot> subSlots = getState().getPlayer().getInventory().getSubSlots(selectedSlot);
			if(subSlots == null || subSlots.isEmpty()) {
				return 0;
			}
			type = subSlots.get(selectedSubSlot % subSlots.size()).getType();
			System.err.println("[CLIENT] PLACING MULTISLOT: " + selectedSubSlot + " -> " + type);
		} else if(ElementKeyMap.isValidType(type) && ElementKeyMap.getInfoFast(type).blocktypeIds != null) {
			short blockType;
			if(forcedSelect != 0) {
				blockType = forcedSelect;
				stacked = true;
			} else {
				blockType = getState().getBlockSyleSubSlotController().getBlockType(type, selectedSubSlot);
				stacked = true;
			}
			if(blockType < 0) {
				System.err.println("[CLIENT] POPUP RADIAL");
				if(getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().checkRadialSelect(type)) {
				}
				return 0;
			}
			type = blockType;
		}
		if(type < 0 && symmetryPlanes.getPlaceMode() == 0) {
			getState().getController().popupInfoTextMessage(Lng.str("Cannot be placed!\nThis is not a building element!"), 0);
			return 0;
		}
		if(controller.getHpController().isRebooting()) {
			getState().getController().popupAlertTextMessage(Lng.str("Cannot place blocks\nwhile rebooting!\n(%s)", StringTools.formatTimeFromMS(controller.getHpController().getRebootTimeLeftMS())), 0);
			return 0;
		}
		if(controller.isScrap()) {
			getState().getController().popupAlertTextMessage(Lng.str("WARNING\nThis station's infrastructure is decayed.\nYou won't be able to salvage the blocks\nyou placed!"), 0);
			if(firstWarning) {
				firstWarning = false;
				return 0;
			}
		}
		if(buildToolsManager.isInCreateDockingMode()) {
			if(buildToolsManager.getBuildToolCreateDocking().potentialCreateDockPos != null) {
				if(buildToolsManager.getBuildToolCreateDocking().docker == null) {
					VoidUniqueSegmentPiece pp = buildToolsManager.getBuildToolCreateDocking().potentialCreateDockPos;
					ElementInformation elem = ElementKeyMap.getInfoFast(pp.getType());
					buildToolsManager.getBuildToolCreateDocking().rail = new VoidUniqueSegmentPiece(pp);
					Oriencube algo = (Oriencube) BlockShapeAlgorithm.getAlgo(elem.getBlockStyle(), pp.getOrientation());
					Oriencube mirrorAlgo = algo.getMirrorAlgo();
					byte orient;
					boolean found = false;
					for(orient = 0; orient < SegmentData.FULL_ORIENT; orient++) {
						if(BlockShapeAlgorithm.algorithms[elem.getBlockStyle().id - 1][orient].getClass() == mirrorAlgo.getClass()) {
							found = true;
							break;
						}
					}
					assert (found);
					pp.setOrientation(orient);
					pp.voidPos.add(Element.DIRECTIONSi[Element.switchLeftRight(algo.getOrientCubePrimaryOrientation())]);
					pp.setType(ElementKeyMap.RAIL_BLOCK_DOCKER);
					SegmentCollisionCheckerCallback cb = new SegmentCollisionCheckerCallback();
					if(pp.getSegmentController().getCollisionChecker().checkPieceCollision(pp, cb, true)) {
						getState().getController().popupAlertTextMessage(Lng.str("Docker can't be placed here because it is blocked\nby another structure"), 0);
						return 0;
					}
					buildToolsManager.getBuildToolCreateDocking().docker = pp;
					buildToolsManager.getBuildToolCreateDocking().potentialCreateDockPos = null;
				}
			} else {
				if(buildToolsManager.getBuildToolCreateDocking().docker != null && buildToolsManager.getBuildToolCreateDocking().core == null) {
					if(buildToolsManager.getBuildToolCreateDocking().potentialCore != null) {
						SegmentCollisionCheckerCallback cb = new SegmentCollisionCheckerCallback();
						if(buildToolsManager.getBuildToolCreateDocking().potentialCore.getSegmentController().getCollisionChecker().checkPieceCollision(buildToolsManager.getBuildToolCreateDocking().potentialCore, cb, true)) {
							getState().getController().popupAlertTextMessage(Lng.str("Core cannot be placed here\nbecause it is blocked\nby another structure"), 0);
							return 0;
						}
						buildToolsManager.getBuildToolCreateDocking().core = buildToolsManager.getBuildToolCreateDocking().potentialCore;
						buildToolsManager.getBuildToolCreateDocking().potentialCore = null;
					} else {
						getState().getController().popupAlertTextMessage(Lng.str("Place core at a free position!"), 0);
					}
				} else if(buildToolsManager.getBuildToolCreateDocking().docker != null && buildToolsManager.getBuildToolCreateDocking().core != null && buildToolsManager.getBuildToolCreateDocking().coreOrientation < 0) {
					buildToolsManager.getBuildToolCreateDocking().coreOrientation = buildToolsManager.getBuildToolCreateDocking().potentialCoreOrientation;
				} else {
					getState().getController().popupAlertTextMessage(Lng.str("Please place on a rail surface!"), 0);
				}
			}
			return 0;
		}
		if(controller.hasStructureAndArmorHP() && controller.getHpController().getHpPercent() < 1d && !warned.contains(controller.getId())) {
			warned.add(controller.getId());
			PlayerGameOkCancelInput check = new PlayerGameOkCancelInput("CONFIRM", getState(), Lng.str("Warning SHP"), Lng.str("Placing blocks on a damaged ship\ncan lead to negative effects!\nPlease reboot the ship before placing\nany blocks.")) {
				@Override
				public boolean isOccluded() {
					return false;
				}

				@Override
				public void onDeactivate() {
				}

				@Override
				public void pressedOK() {
					deactivate();
					getInShipControlManager().popupShipRebootDialog(controller);
				}
			};
			check.getInputPanel().onInit();
			check.getInputPanel().setOkButtonText(Lng.str("Reboot"));
			check.getInputPanel().setCancelButtonText(Lng.str("Ignore"));
			check.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(155);
		}
		// if (ElementKeyMap.exists(type) && ElementKeyMap.getInfo(type).armorHP > 0 && controller.hasStructureAndArmorHP() && controller.getHpController().getMaxArmorHp() > 0 && controller.getHpController().getArmorHpPercent() < 1d && !warned.contains(-controller.getId())) {
		// warned.add(-controller.getId());
		// PlayerGameOkCancelInput check = new PlayerGameOkCancelInput("CONFIRM", getState(),
		// Lng.str("Warning AHP"), Lng.str("Placing armor on a ship that is not at 100%\nwill not add any additional armor!\nPlease repair the ship at a shop before placing\nany armor blocks\nStations will repair all armor on reboot.")) {
		//
		// @Override
		// public boolean isOccluded() {
		// return false;
		// }
		//
		// @Override
		// public void onDeactivate() {
		// }
		//
		// @Override
		// public void pressedOK() {
		// deactivate();
		// if (controller instanceof SpaceStation) {
		// getInShipControlManager().popupShipRebootDialog(controller);
		// }
		//
		// }
		// };
		// check.getInputPanel().onInit();
		// if (controller instanceof SpaceStation) {
		// check.getInputPanel().setOkButtonText(Lng.str("Reboot"));
		// check.getInputPanel().setCancelButtonText(Lng.str("Ignore"));
		// } else {
		// check.getInputPanel().setOkButtonText(Lng.str("Ok"));
		// check.getInputPanel().setCancelButton(false);
		// }
		// check.activate(); AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE);
		// }
		return type;
	}

	public static boolean isAdvancedBuildMode(GameClientState state) {
		PlayerInteractionControlManager p = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
		return !BuildConstructionCommand.issued && (p.stickyAdvBuildMode || KeyboardMappings.BUILD_MODE_FIX_CAM.isDown()) && p.isInAnyBuildMode();
	}

	public SegmentPiece getSelectedBlockByActiveController() {
		if(!playerCharacterManager.isTreeActive() || playerCharacterManager.isSuspended()) {
			if(inShipControlManager.getShipControlManager().getSegmentBuildController().isTreeActive()) {
				return inShipControlManager.getShipControlManager().getSegmentBuildController().getSelectedBlock();
			} else {
				return segmentControlManager.getSegmentBuildController().getSelectedBlock();
			}
		} else {
			return playerCharacterManager.getSelectedBlock();
		}
	}

	private boolean surroundCondition(EditableSendableSegmentController controller, Vector3f pos, Vector3f dir, float editDistance, Vector3i size, SurroundBlockCondition inter) throws ElementPositionBlockedException, BlockNotBuildTooFast {
		SegmentPiece piece = controller.getNextToNearestPiece(new Vector3f(pos), new Vector3f(dir), new Vector3i(), editDistance, size, new Vector3i());
		if(piece != null) {
			boolean ok = false;
			for(int i = 0; i < 6; i++) {
				Vector3i n = piece.getAbsolutePos(new Vector3i());
				n.add(Element.DIRECTIONSi[i]);
				SegmentPiece segmentPiece = controller.getSegmentBuffer().getPointUnsave(n);
				if(segmentPiece != null) {
					if(ElementKeyMap.isValidType(segmentPiece.getType())) {
						if(inter.ok(ElementKeyMap.getInfo(segmentPiece.getType()))) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public boolean checkRadialSelect(short selectedType) {
		if(forcedSelect != 0) {
			return false;
		}
		if(ElementKeyMap.isValidType(selectedType) && ElementKeyMap.getInfoFast(selectedType).blocktypeIds != null) {
			if(selectedSubSlot < getState().getBlockSyleSubSlotController().getSelectedStack(selectedType).length) {
				short blockType = getState().getBlockSyleSubSlotController().getBlockType(selectedType, selectedSubSlot);
				// System.err.println("SELECTED TYPE: "+selectedType+"; sub "+getSelectedSubSlot()+" -> "+blockType+"; Stack "+getState().getBlockSyleSubSlotController().getSelectedStack(selectedType).length+"; "+Arrays.toString(getState().getBlockSyleSubSlotController().getSelectedStack(selectedType)));
				if(blockType < 0) {
					RadialMenuDialogShapes shapeDiag = new RadialMenuDialogShapes(getState(), ElementKeyMap.getInfo(selectedType));
					shapeDiag.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(176);
					return true;
				}
			}
			return false;
		}
		return false;
	}

	/**
	 * @return the inShipControlManager
	 */
	public InShipControlManager getInShipControlManager() {
		return inShipControlManager;
	}

	public boolean isInAnyBuildMode() {
		return isInAnyCharacterBuildMode() || isInAnyStructureBuildMode();
	}

	public boolean isInAnyCharacterBuildMode() {
		return playerCharacterManager.isTreeActive();
	}

	public boolean isInAnyStructureBuildMode() {
		return (segmentControlManager.getSegmentBuildController().isTreeActive() || inShipControlManager.getShipControlManager().getSegmentBuildController().isTreeActive());
		// && WorldDrawer.insideBuildMode;
	}

	public void setSelectedBlockByActiveController(SegmentPiece p) {
		if(!playerCharacterManager.isTreeActive() || playerCharacterManager.isSuspended()) {
			if(inShipControlManager.getShipControlManager().getSegmentBuildController().isTreeActive()) {
				inShipControlManager.getShipControlManager().getSegmentBuildController().setSelectedBlock(p);
				;
			} else {
				segmentControlManager.getSegmentBuildController().setSelectedBlock(p);
			}
		} else {
			playerCharacterManager.setSelectedBlock(p);
		}
	}

	public boolean isAdvancedBuildMode() {
		return isAdvancedBuildMode(getState());
	}

	public int getBlockOrientation() {
		return this.blockOrientation;
	}

	public void setBlockOrientation(int o) {
		this.blockOrientation = o;
	}

	public BuildToolsManager getBuildToolsManager() {
		return buildToolsManager;
	}

	public PlayerExternalController getPlayerCharacterManager() {
		return playerCharacterManager;
	}

	public HotbarLayout getHotbarLayout() {
		return hotbarLayout;
	}

	public boolean checkActivate(final SegmentPiece p) {
		if(p != null) {
			if(!getState().getController().allowedToActivate(p)) {
				return false;
			}
			if(p.getSegmentController() instanceof Planet || p.getSegmentController() instanceof PlanetIco) {
				boolean isComputer = p.getType() == ElementKeyMap.WEAPON_CONTROLLER_ID || p.getType() == ElementKeyMap.MISSILE_DUMB_CONTROLLER_ID || p.getType() == ElementKeyMap.DAMAGE_BEAM_COMPUTER || p.getType() == ElementKeyMap.DAMAGE_PULSE_COMPUTER || p.getType() == ElementKeyMap.SALVAGE_CONTROLLER_ID || p.getType() == ElementKeyMap.REPAIR_CONTROLLER_ID || p.getType() == ElementKeyMap.SHIELD_DRAIN_CONTROLLER_ID || p.getType() == ElementKeyMap.SHIELD_SUPPLY_CONTROLLER_ID || p.getType() == ElementKeyMap.POWER_SUPPLY_BEAM_COMPUTER || p.getType() == ElementKeyMap.POWER_DRAIN_BEAM_COMPUTER || p.getType() == ElementKeyMap.PUSH_PULSE_CONTROLLER_ID;
				if(isComputer) {
					getState().getController().popupGameTextMessage(Lng.str("This block is unusable on a planet"), 0);
					return false;
				}
			}
			if(p.getType() == ElementKeyMap.GRAVITY_ID || p.getType() == ElementKeyMap.GRAVITY_EXIT_ID) {
				getState().getCharacter().activateGravity(p);
				return true;
			} else if(ElementInformation.isMedical(p.getType())) {
				getState().getCharacter().activateMedical(p);
				return true;
			} else if(p.getType() == ElementKeyMap.WARP_GATE_CONTROLLER) {
				if(p.getSegmentController().getConfigManager().apply(StatusEffectType.WARP_FREE_TARGET, false)) {
					final PlayerGameTextInput t = new PlayerGameTextInput("EDIT_FREE_WARP_TARGET", getState(), 80, Lng.str("Set Warp Target"), Lng.str("Set Warp Target (e.g. 10, 20, 111 [or 10 20 111][or 10.20.100])"), p.getAbsolutePos(new Vector3i()).toStringPure()) {
						@Override
						public String[] getCommandPrefixes() {
							return null;
						}

						@Override
						public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
							return null;
						}

						@Override
						public void onFailedTextCheck(String msg) {
						}

						@Override
						public void onDeactivate() {
							suspend(false);
						}

						@Override
						public boolean onInput(String entry) {
							try {
								Vector3i to = Vector3i.parseVector3iFree(entry);
								getState().getPlayer().sendSimpleCommand(SimplePlayerCommands.SET_FREE_WARP_TARGET, p.getSegmentController().getId(), p.getAbsoluteIndex(), to.x, to.y, to.z);
							} catch(Exception r) {
								r.printStackTrace();
								getState().getController().popupAlertTextMessage(Lng.str("Invalid sector format. Type as 'x, y, z'"), 0);
								return false;
							}
							return true;
						}

						@Override
						public boolean isOccluded() {
							return false;
						}
					};
					t.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(167);
				} else {
					getState().getController().popupGameTextMessage(Lng.str("Current reactor doesn't support free warp gate target."), 0);
				}
				return true;
			} else if(p.getType() == ElementKeyMap.PLAYER_SPAWN_MODULE) {
				System.err.println("[CLIENT][SPAWN] attempting to set spawn point to " + p);
				getState().getController().setSpawnPoint(p);
				// Transform transform = new Transform();
				// p.getTransform(transform);
				// Vector3f upVector = GlUtil.getUpVector(new Vector3f(), transform);
				// upVector.scale(1.5f);
				// transform.origin.add(upVector);
				// getState().getController().setSpawnPoint(transform.origin);
				// getState().getController().popupInfoTextMessage("Spawning Point Set", 0);
				return true;
			} else if(p.getType() == ElementKeyMap.AI_ELEMENT) {
				getState().getController().popupGameTextMessage(Lng.str("Activated ships AI Element.\n") + p.getSegment().getSegmentController().toNiceString(), 0);
				getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().aiConfigurationAction(p);
				return true;
			} else if(p.getType() == ElementKeyMap.SHOP_BLOCK_ID) {
				getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().shopAction();
				return true;
			} else if(p.getType() == Blocks.FACTORY_MANAGER.getId()) {
//				(new FactoryManagerDialog(getState(), p)).activate(); We probably don't need a UI for this tbh
				return true;
			} else if(p.getType() == Blocks.FLEET_MANAGER.getId()) {
				(new FleetManagerDialog(getState(), p)).activate();
				return true;
			} else if(p.getType() == ElementKeyMap.LOGIC_REMOTE_INNER) {
				long index = p.getAbsoluteIndexWithType4();
				String prName = p.getSegmentController().getTextMap().get(index);
				if(prName == null) {
					((ClientSegmentProvider) p.getSegmentController().getSegmentProvider()).getSendableSegmentProvider().clientTextBlockRequest(index);
					prName = "";
				}
				final PlayerGameTextInput t = new PlayerGameTextInput("EDIT_LOGIC_NAME", getState(), 16, Lng.str("Edit name"), "", prName) {
					@Override
					public String[] getCommandPrefixes() {
						return null;
					}

					@Override
					public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
						return null;
					}

					@Override
					public void onFailedTextCheck(String msg) {
					}

					@Override
					public void onDeactivate() {
						suspend(false);
					}

					@Override
					public boolean onInput(String entry) {
						SendableSegmentProvider ss = ((ClientSegmentProvider) p.getSegment().getSegmentController().getSegmentProvider()).getSendableSegmentProvider();
						TextBlockPair f = new TextBlockPair();
						f.block = ElementCollection.getIndex4(p.getAbsoluteIndex(), ElementKeyMap.LOGIC_REMOTE_INNER);
						f.text = entry;
						ss.getNetworkObject().textBlockResponsesAndChangeRequests.add(new RemoteTextBlockPair(f, false));
						return true;
					}

					@Override
					public boolean isOccluded() {
						return false;
					}
				};
				t.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(166);
				return true;
			} else if(p.getType() == ElementKeyMap.FACTION_BLOCK) {
				if(getState().getPlayer().getFactionId() != 0) {
					if((getState().getFactionManager().getFaction(getState().getPlayer().getFactionId()) != null)) {
						activateFactionDiag(p.getSegmentController());
					} else {
						getState().getController().popupGameTextMessage(Lng.str("Cannot activate:\nInvalid faction ID!\n") + p.getSegment().getSegmentController().toNiceString(), 0);
					}
				} else {
					if(!activateResetFactionIfOwner(p.getSegmentController())) {
						getState().getController().popupGameTextMessage(Lng.str("You have to be owner of this structure\nor in a faction to activate this block.") + p.getSegment().getSegmentController().toNiceString(), 0);
					}
				}
				return true;
			} else if(p.getType() == ElementKeyMap.LIFT_ELEMENT) {
				SegmentController c = p.getSegmentController();
				if(c instanceof ManagedSegmentController && ((ManagedSegmentController<?>) c).getManagerContainer() instanceof LiftContainerInterface) {
					LiftContainerInterface lmi = ((LiftContainerInterface) ((ManagedSegmentController<?>) c).getManagerContainer());
					LiftCollectionManager lm = lmi.getLiftManager();
					Vector3i pos = p.getAbsolutePos(new Vector3i());
					lm.activate(pos, true);
					lmi.handleClientRemoteLift(pos);
				}
				return true;
			} else if(p.getType() == ElementKeyMap.POWER_ID_OLD || p.getType() == ElementKeyMap.POWER_CAP_ID) {
				SegmentController c = p.getSegmentController();
				if(c instanceof ManagedSegmentController && ((ManagedSegmentController<?>) c).getManagerContainer() instanceof PowerManagerInterface) {
					PowerManagerInterface lmi = ((PowerManagerInterface) ((ManagedSegmentController<?>) c).getManagerContainer());
					PowerAddOn powerManager = lmi.getPowerAddOn();
					if(lastPowerMsg != null) {
						getState().getController().endPopupMessage(lastPowerMsg);
					}
					lastPowerMsg = Lng.str("POWER INFO\n\nCapacity: %s/%s\nRecharge Rate (e/sec): %s", (int) powerManager.getPowerSimple(), (int) powerManager.getMaxPower(), (int) (powerManager.getRecharge()));
					getState().getController().popupInfoTextMessage(lastPowerMsg, 0);
					return true;
				}
			} else if(p.getType() == ElementKeyMap.POWER_BATTERY) {
				SegmentController c = p.getSegmentController();
				if(c instanceof ManagedSegmentController && ((ManagedSegmentController<?>) c).getManagerContainer() instanceof PowerManagerInterface) {
					PowerManagerInterface lmi = ((PowerManagerInterface) ((ManagedSegmentController<?>) c).getManagerContainer());
					PowerAddOn powerManager = lmi.getPowerAddOn();
					if(lastPowerMsg != null) {
						getState().getController().endPopupMessage(lastPowerMsg);
					}
					long t = p.getAbsoluteIndex();
					double capGroup = 0;
					double regenGroup = 0;
					for(PowerBatteryUnit a : lmi.getPowerBatteryManager().getElementCollections()) {
						if(a.contains(t)) {
							capGroup = a.getMaxPower();
							regenGroup = a.getRecharge();
							break;
						}
					}
					lastPowerMsg = Lng.str("AUXILIARY POWER INFO\n\n" + "Capacity: %s/%s\n" + "Recharge Rate (e/sec): %s\n\n" + "This Group:\n" + "Capacity: %s\n" + "Recharge Rate (e/sec): %s", StringTools.formatPointZero(powerManager.getBatteryPower()), StringTools.formatPointZero(powerManager.getBatteryMaxPower()), StringTools.formatPointZero(powerManager.getRecharge()), StringTools.formatPointZero(capGroup), StringTools.formatPointZero(regenGroup));
					getState().getController().popupInfoTextMessage(lastPowerMsg, 0);
					return true;
				}
			} else if(p.getType() == ElementKeyMap.THRUSTER_ID) {
				SegmentController c = p.getSegment().getSegmentController();
				if(c instanceof Ship) {
					getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().activateThrustManager((Ship) c);
					return true;
				} else {
					getState().getController().popupAlertTextMessage(Lng.str("Faction Access denied"), 0);
				}
			} else if(p.getType() == ElementKeyMap.SHIELD_CAP_ID || p.getType() == ElementKeyMap.SHIELD_REGEN_ID) {
				SegmentController c = p.getSegment().getSegmentController();
				if(c instanceof ManagedSegmentController && ((ManagedSegmentController<?>) c).getManagerContainer() instanceof ShieldContainerInterface) {
					ShieldContainerInterface lmi = ((ShieldContainerInterface) ((ManagedSegmentController<?>) c).getManagerContainer());
					ShieldAddOn shieldAddOn = lmi.getShieldAddOn();
					if(lastShieldMsg != null) {
						getState().getController().endPopupMessage(lastShieldMsg);
					}
					if(shieldAddOn.isUsingLocalShields()) {
						ShieldLocal local = shieldAddOn.getShieldLocalAddOn().getContainingShield(lmi, p.getAbsoluteIndex());
						if(local != null) {
							lastShieldMsg = Lng.str("SHIELD INFO %s\n\nRadius: %sm\nShields: %s/%s (capacity banks: %s)\nRecharge rate (e/sec): %s\nShield Upkeep: %s\nPower consumption: %s", local.getPosString(), StringTools.formatPointZero(local.radius), StringTools.formatPointZero(local.getShields()), StringTools.formatPointZero(local.getShieldCapacity()), local.supportIds.size(), StringTools.formatPointZero(local.getRechargeRate()), StringTools.formatPointZero(local.getShieldUpkeep()), StringTools.formatPointZero(local.getShields() < local.getShieldCapacity() ? local.getPowerConsumedPerSecondCharging() : local.getPowerConsumedPerSecondResting()));
						}
					} else {
						int shieldPowerCost = shieldAddOn.getShields() >= shieldAddOn.getShieldCapacity() ? (int) (shieldAddOn.getShieldRechargeRate() * VoidElementManager.SHIELD_FULL_POWER_CONSUMPTION) : (int) (shieldAddOn.getShieldRechargeRate() * VoidElementManager.SHIELD_RECHARGE_POWER_CONSUMPTION);
						lastShieldMsg = Lng.str("SHIELD INFO\n\nCapacity: %s/%s\nRecharge Rate (e/sec): %s\nPower Usage (e/sec): %s", (int) shieldAddOn.getShields(), (int) shieldAddOn.getShieldCapacity(), (int) shieldAddOn.getShieldRechargeRate(), shieldPowerCost);
					}
					if(lastShieldMsg != null) {
						getState().getController().popupInfoTextMessage(lastShieldMsg, 0);
					}
					return true;
				}
			} else if(ElementKeyMap.canOpen(p.getType())) {
				if(p.getSegmentController().isVirtualBlueprint()) {
					getState().getController().popupAlertTextMessage(Lng.str("Can't open this on a design!"), 0);
				} else {
					SegmentController c = p.getSegment().getSegmentController();
					if((c instanceof ManagedSegmentController) && ((((ManagedSegmentController<?>) c).getManagerContainer()) instanceof InventoryHolder)) {
						Vector3i pos = p.getAbsolutePos(new Vector3i());
						ManagerContainer<?> ih = (((ManagedSegmentController<?>) c).getManagerContainer());
						Inventory inventory = ih.getInventory(p.getAbsoluteIndex());
						if(inventory == null) {
							System.err.println("[CLIENT] WARNING: OPEN BLOCK: Inventory NULL: " + pos + ": Holder: " + ih + "; Invs: " + ih.printInventories() + ";");
						} else {
							// getState().getController().popupGameTextMessage(
							// Lng.str("Opened\n %s\nLocation: %s\nof %s",  ElementKeyMap.getInfo(p.getType()).getName(),  inventory.getParameter(),  c.toNiceString()), 0);
						}
						getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().inventoryAction(inventory);
					} else {
					}
				}
				return true;
			} else if(ElementKeyMap.isReactor(p.getType())) {
				SegmentController c = p.getSegmentController();
				if(c.hasAnyReactors()) {
					ReactorTreeDialog d = new ReactorTreeDialog(getState(), (ManagedSegmentController<?>) c);
					d.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(165);
				}
			}
			if(p.getType() == ElementKeyMap.RACE_GATE_CONTROLLER) {
				// ((RaceManagerState)getState()).getRaceManager().onActivateRaceControllerClient(getState().getPlayer(), p);
				boolean cc = true;
				for(DialogInterface x : getState().getController().getPlayerInputs()) {
					if(x instanceof PlayerRaceInput) {
						cc = false;
					}
				}
				if(cc) {
					PlayerRaceInput ri = new PlayerRaceInput(getState(), p);
					ri.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(168);
				}
				return true;
			}
			if(p.getType() == ElementKeyMap.TRANSPORTER_CONTROLLER) {
				if(p.getSegmentController() instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) p.getSegmentController()).getManagerContainer() instanceof TransporterModuleInterface) {
					TransporterModuleInterface c = (TransporterModuleInterface) ((ManagedSegmentController<?>) p.getSegmentController()).getManagerContainer();
					TransporterCollectionManager transporterCollectionManager = c.getTransporter().getCollectionManagersMap().get(p.getAbsoluteIndex());
					if(transporterCollectionManager != null) {
						PlayerTransporterInput ri = new PlayerTransporterInput(getState(), transporterCollectionManager);
						ri.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(169);
					}
				}
				return true;
			}
			if(Quarter.QuarterType.getFromBlock(p.getType()) != null) {
				for(Quarter quarter : p.getSegmentController().getQuarterManager().getQuartersById().values()) {
					if(quarter.getIndex() == p.getAbsoluteIndex()) {
						quarter.openGUI(p, getState().getPlayer());
						return true;
					}
				}
			}
//			if(p.getType() == ElementKeyMap.TEXT_BOX) {
			if(ElementKeyMap.isTextBox(p.getType())) {
				String text = p.getSegment().getSegmentController().getTextMap().get(ElementCollection.getIndex4(p.getAbsoluteIndex(), p.getOrientation()));
				if(text == null) {
					text = "";
				}
				final PlayerTextAreaInput t = new PlayerTextAreaInput("EDIT_DISPLAY_BLOCK_POPUP", getState(), 400, 300, SendableGameState.TEXT_BLOCK_LIMIT, SendableGameState.TEXT_BLOCK_LINE_LIMIT + 1, Lng.str("Edit Sign"), "", text, FontSize.SMALL_14) {
					@Override
					public void onDeactivate() {
						suspend(false);
					}

					@Override
					public boolean onInput(String entry) {
						SendableSegmentProvider ss = ((ClientSegmentProvider) p.getSegment().getSegmentController().getSegmentProvider()).getSendableSegmentProvider();
						TextBlockPair f = new TextBlockPair();
						f.block = ElementCollection.getIndex4(p.getAbsoluteIndex(), p.getOrientation());
						f.text = entry;
						System.err.println("[CLIENT]Text entry:\n\"" + f.text + "\"");
						ss.getNetworkObject().textBlockResponsesAndChangeRequests.add(new RemoteTextBlockPair(f, false));
						return true;
					}

					@Override
					public String[] getCommandPrefixes() {
						return null;
					}

					@Override
					public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
						return null;
					}

					@Override
					public void onFailedTextCheck(String msg) {
					}

					@Override
					public boolean isOccluded() {
						return false;
					}
				};
				t.getTextInput().setAllowEmptyEntry(true);
				GUITextButton help = new GUITextButton(getState(), 100, 20, Lng.str("Help"), new GUICallback() {
					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if(event.pressedLeftMouse()) {
							String desc = Lng.str("" + "You can use different kinds of styles for your display block.\n" + "Use a header like <style></style> to select font and color\n" + "e.g. <style>c=#FF0000,f=1</style> -> red text with medium font size.\n" + "f=x (x can be 0,1,2 for font sizes)\n" + "c=x (x has to be a hex code for font (used in css/html) starting with #)\n\n\n" + "Variables that can be placed inside the text:\n%s\n\n" + "If you place an activation block next to this block and connect that block\n" + "to another display, the content of that display will be replaced with this one.\n" + "There are several other methods to edit the content of the connected display:\n" + "* '[PASSWORD]text': The text in this block will not show up.\n" + "* '[ADD]text': The text after [ADD] is added to the existing text of the connected block ([ADD] must be in the very front)\n" + "* '[DEL]number': Removes an amount of characters from the connected block ([DEL] must be in the very front)\n" + "* '[REPLACEFIRST]regexp[WITH]replacement': use java conform regular expressions to replace specific text ([REPLACEFIRST] must be in the very front)\n" + "* '[REPLACEALL]regexp[WITH]replacement': use java conform regular expressions to replace specific text ([REPLACEALL] must be in the very front)\n\n" + "You can also use the sensor block to check if two display blocks have equal text content. Just connect the sensor block to two display blocks as input. " + "The [password] tag is ignored when comparing text.", Replacements.getVariables(p.getSegmentController().isUsingOldPower()));
							PlayerGameOkCancelInput check = new PlayerGameOkCancelInput("DISPLAY_BLOCK_HELP_D_", getState(), 600, 500, Lng.str("Display Help"), desc, FontStyle.def) {
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
									deactivate();
								}
							};
							check.getInputPanel().setCancelButton(false);
							check.activate();
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
							AudioController.fireAudioEventID(170);
						}
					}

					@Override
					public boolean isOccluded() {
						return false;
					}
				}) {
					/* (non-Javadoc)
					 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
					 */
					@Override
					public void draw() {
						setPos(t.getInputPanel().getButtonCancel().getPos().x + t.getInputPanel().getButtonCancel().getWidth() + 10, t.getInputPanel().getButtonCancel().getPos().y, 0);
						super.draw();
					}
				};
				help.setPos(300, -40, 0);
				t.getInputPanel().onInit();
				((GUIDialogWindow) t.getInputPanel().background).getMainContentPane().getContent(0).attach(help);
				t.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(171);
				return true;
			} else {
				checkMakeCustomOutput(p);
			}
		}
		return false;
	}

	public void activateFactionDiag(SegmentController c) {
		(new FactionBlockDialog(getState(), c, this)).activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(172);
	}

	public boolean activateResetFactionIfOwner(final SegmentController segmentController) {
		if(segmentController.isOwnerSpecific(getState().getPlayer())) {
			if(segmentController.railController.isDockedAndExecuted()) {
				getState().getController().popupAlertTextMessage(Lng.str("Please undock this ship first, or\nfaction would be overwritten!"), 0);
			} else {
				(new PlayerGameOkCancelInput("CONFIRM", getState(), Lng.str("Faction Block"), Lng.str("You are not in any faction.\nDo you want to reset this object to neutral?")) {
					@Override
					public void onDeactivate() {
					}

					@Override
					public void pressedOK() {
						getState().getPlayer().getFactionController().sendEntityFactionIdChangeRequest(getState().getPlayer().getFactionId(), segmentController);
						deactivate();
					}
				}).activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(175);
				return true;
			}
		}
		return false;
	}

	private void checkMakeCustomOutput(SegmentPiece p) {
		//INSERTED CODE @972
		SegmentPieceActivateByPlayer event = new SegmentPieceActivateByPlayer(p, playerCharacterManager.getState().getPlayer(), this);
		StarLoader.fireEvent(event, false);
		///
		ElementInformation info = ElementKeyMap.getInfo(p.getType());
		boolean canUse = getState().getPlayer().getFactionId() == p.getSegmentController().getFactionId() || p.getSegmentController().getFactionId() == 0;
		if(info.isConsole()) {
			SegmentPiece permissionModule = SegmentPieceUtils.getFirstMatchingAdjacent(p, (short) 346);
			if(permissionModule != null) canUse = true;
			//Todo: Maybe handle Faction Permission Modules, though I'm not sure if that's actually necessary...
			if(!canUse) return; //Prevent any potential permission exploits
			ArrayList<SegmentPiece> controlled = SegmentPieceUtils.getControlledPieces(p);
			for(SegmentPiece piece : controlled) {
				if(piece.getInfo().isEnterable()) {
					if(playerCharacterManager.isActive()) {
						if(playerCharacterManager.checkEnterAndEnterIfPossible(piece)) {
							inShipControlManager.setEntered(p);
							AudioController.fireAudioEvent("0022_action - enter digits in digital keypad", AudioController.ent(p.getSegmentController(), p, p.getAbsoluteIndex(), 10));
						}
					} else if(inShipControlManager.isActive()) inShipControlManager.exitShip(false);
					return;
				} else if(piece.getInfo().getId() == ElementKeyMap.REACTOR_MAIN || piece.getInfo().getId() == ElementKeyMap.REACTOR_STABILIZER) { //Reactor Block or Reactor Stabilizer
					if(piece.getSegmentController() instanceof ManagedSegmentController<?> managedSegmentController) {
						(new ReactorTreeDialog(getState(), managedSegmentController)).activate();
						AudioController.fireAudioEvent("0022_action - enter digits in digital keypad", AudioController.ent(p.getSegmentController(), p, p.getAbsoluteIndex(), 10));
					}
					return;
				} else if(piece.getInfo().isReactorChamberSpecific()) {
					if(piece.getSegmentController() instanceof ManagedSegmentController<?> managedSegmentController) {
						(new ReactorTreeDialog(getState(), managedSegmentController)).activate();
						AudioController.fireAudioEvent("0022_action - enter digits in digital keypad", AudioController.ent(p.getSegmentController(), p, p.getAbsoluteIndex(), 10));
					}
					return;
				} else {
					if(piece.getInfo().canActivate()) {
						try {
							switch(piece.getType()) {
								case 544 -> { //Jump Drive Computer
									if(piece.getSegmentController() instanceof ManagedSegmentController<?> managedSegmentController) {
										JumpDriveElementManager jumpDriveElementManager = SegmentControllerUtils.getElementManager(managedSegmentController, JumpDriveElementManager.class);
										if(jumpDriveElementManager != null) {
											jumpDriveElementManager.getCollection().getActivationManager().setActive(true);
											AudioController.fireAudioEvent("0022_action - enter digits in digital keypad", AudioController.ent(p.getSegmentController(), p, p.getAbsoluteIndex(), 5));
										}
									}
									return;
								}
								case 681 -> { //Jump Inhibitor Computer
									if(piece.getSegmentController() instanceof ManagedSegmentController<?> managedSegmentController) {
										JumpInhibitorElementManager jumpInhibitorElementManager = SegmentControllerUtils.getElementManager(managedSegmentController, JumpInhibitorElementManager.class);
										if(jumpInhibitorElementManager != null && !jumpInhibitorElementManager.getCollectionManagers().isEmpty()) {
											jumpInhibitorElementManager.getCollectionManagers().get(0).setActive(true);
											AudioController.fireAudioEvent("0022_action - enter digits in digital keypad", AudioController.ent(p.getSegmentController(), p, p.getAbsoluteIndex(), 5));
										}
									}
									return;
								}
								case 654 -> { //Long Range Scanner
									if(piece.getSegmentController() instanceof ManagedSegmentController<?> managedSegmentController) {
										LongRangeScannerElementManager scannerElementManager = SegmentControllerUtils.getElementManager(managedSegmentController, LongRangeScannerElementManager.class);
										if(scannerElementManager != null && scannerElementManager.hasCollection()) {
											scannerElementManager.checkScan(GameClient.getClientPlayerState());
											AudioController.fireAudioEvent("0022_action - enter digits in digital keypad", AudioController.ent(p.getSegmentController(), p, p.getAbsoluteIndex(), 5));
										}
									}
									return;
								}
								case 1820 -> { //Astro Scanner
									if(piece.getSegmentController() instanceof ManagedSegmentController<?> managedSegmentController) {
										StructureScannerElementManager scannerElementManager = SegmentControllerUtils.getElementManager(managedSegmentController, StructureScannerElementManager.class);
										if(scannerElementManager != null && scannerElementManager.hasCollection()) {
											scannerElementManager.getCollection().setActive(true);
											AudioController.fireAudioEvent("0022_action - enter digits in digital keypad", AudioController.ent(p.getSegmentController(), p, p.getAbsoluteIndex(), 5));
										}
									}
									return;
								}
								case 121 -> { //AI Module
									getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().aiConfigurationAction(piece);
									AudioController.fireAudioEvent("0022_action - enter digits in digital keypad", AudioController.ent(p.getSegmentController(), p, p.getAbsoluteIndex(), 5));
									return;
								}
								case 347 -> { //Shop Module
									if(getState().getCurrentClosestShop() != null) {
										getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getShopControlManager().setActive(true);
										AudioController.fireAudioEvent("0022_action - enter digits in digital keypad", AudioController.ent(p.getSegmentController(), p, p.getAbsoluteIndex(), 5));
									}
									return;
								}
								case 291 -> { //Faction Module
									(new FactionBlockDialog(getState(), p.getSegmentController(), this)).activate();
									AudioController.fireAudioEvent("0022_action - enter digits in digital keypad", AudioController.ent(p.getSegmentController(), p, p.getAbsoluteIndex(), 5));
									return;
								}
								case 677 -> { //Shipyard Computer
									(new PlayerShipyardInfoDialog(getState(), SegmentControllerUtils.getElementManager((ManagedSegmentController<?>) piece.getSegment().getSegmentController(), ShipyardElementManager.class).getCollectionManagers().get(0))).activate();
									AudioController.fireAudioEvent("0022_action - enter digits in digital keypad", AudioController.ent(p.getSegmentController(), p, p.getAbsoluteIndex(), 5));
									return;
								}
								case 8 -> { //Thrust Module
									if(piece.getSegmentController() instanceof Ship) {
										getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().activateThrustManager((Ship) p.getSegmentController());
										AudioController.fireAudioEvent("0022_action - enter digits in digital keypad", AudioController.ent(p.getSegmentController(), p, p.getAbsoluteIndex(), 5));
									}
									return;
								}
								case 687 -> { //Transporter Controller
									if(p.getSegmentController() instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) p.getSegmentController()).getManagerContainer() instanceof TransporterModuleInterface managerContainer) {
										TransporterCollectionManager transporterCollectionManager = managerContainer.getTransporter().getCollectionManagersMap().get(piece.getAbsoluteIndex());
										if(transporterCollectionManager != null) {
											PlayerTransporterInput window = new PlayerTransporterInput(getState(), transporterCollectionManager);
											AudioController.fireAudioEvent("0022_action - enter digits in digital keypad", AudioController.ent(p.getSegmentController(), p, p.getAbsoluteIndex(), 5));
											window.activate();
										}
									}
									return;
								}
								case 479 -> { //Display Module
									String text = p.getSegment().getSegmentController().getTextMap().get(ElementCollection.getIndex4(piece.getAbsoluteIndex(), piece.getOrientation()));
									if(text == null) text = "";
									PlayerTextAreaInput t = new PlayerTextAreaInput("EDIT_DISPLAY_BLOCK_POPUP", getState(), 400, 300, SendableGameState.TEXT_BLOCK_LIMIT, SendableGameState.TEXT_BLOCK_LINE_LIMIT + 1, Lng.str("Edit Sign"), "", text, FontSize.SMALL_14) {
										@Override
										public void onDeactivate() {
											suspend(false);
										}

										@Override
										public boolean onInput(String entry) {
											SendableSegmentProvider ss = ((ClientSegmentProvider) piece.getSegment().getSegmentController().getSegmentProvider()).getSendableSegmentProvider();
											TextBlockPair f = new TextBlockPair();
											f.block = ElementCollection.getIndex4(piece.getAbsoluteIndex(), piece.getOrientation());
											f.text = entry;
											System.err.println("[CLIENT]Text entry:\n\"" + f.text + "\"");
											ss.getNetworkObject().textBlockResponsesAndChangeRequests.add(new RemoteTextBlockPair(f, false));
											return true;
										}

										@Override
										public String[] getCommandPrefixes() {
											return null;
										}

										@Override
										public String handleAutoComplete(String s, TextCallback callback, String prefix) {
											return null;
										}

										@Override
										public void onFailedTextCheck(String msg) {
										}

										@Override
										public boolean isOccluded() {
											return false;
										}
									};
									t.getTextInput().setAllowEmptyEntry(true);
									GUITextButton help = new GUITextButton(getState(), 100, 20, Lng.str("Help"), new GUICallback() {
										@Override
										public void callback(GUIElement callingGuiElement, MouseEvent event) {
											if(event.pressedLeftMouse()) {
												String desc = Lng.str("" + "You can use different kinds of styles for your display block.\n" + "Use a header like <style></style> to select font and color\n" + "e.g. <style>c=#FF0000,f=1</style> -> red text with medium font size.\n" + "f=x (x can be 0,1,2 for font sizes)\n" + "c=x (x has to be a hex code for font (used in css/html) starting with #)\n\n\n" + "Variables that can be placed inside the text:\n%s\n\n" + "If you place an activation block next to this block and connect that block\n" + "to another display, the content of that display will be replaced with this one.\n" + "There are several other methods to edit the content of the connected display:\n" + "* '[PASSWORD]text': The text in this block will not show up.\n" + "* '[ADD]text': The text after [ADD] is added to the existing text of the connected block ([ADD] must be in the very front)\n" + "* '[DEL]number': Removes an amount of characters from the connected block ([DEL] must be in the very front)\n" + "* '[REPLACEFIRST]regexp[WITH]replacement': use java conform regular expressions to replace specific text ([REPLACEFIRST] must be in the very front)\n" + "* '[REPLACEALL]regexp[WITH]replacement': use java conform regular expressions to replace specific text ([REPLACEALL] must be in the very front)\n\n" + "You can also use the sensor block to check if two display blocks have equal text content. Just connect the sensor block to two display blocks as input. " + "The [password] tag is ignored when comparing text.", Replacements.getVariables(p.getSegmentController().isUsingOldPower()));
												PlayerGameOkCancelInput check = new PlayerGameOkCancelInput("DISPLAY_BLOCK_HELP_D_", getState(), 600, 500, Lng.str("Display Help"), desc, FontStyle.def) {
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
														deactivate();
													}
												};
												check.getInputPanel().setCancelButton(false);
												check.activate();
												AudioController.fireAudioEventID(170);
											}
										}

										@Override
										public boolean isOccluded() {
											return false;
										}
									}) {
										/* (non-Javadoc)
										 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
										 */
										@Override
										public void draw() {
											setPos(t.getInputPanel().getButtonCancel().getPos().x + t.getInputPanel().getButtonCancel().getWidth() + 10, t.getInputPanel().getButtonCancel().getPos().y, 0);
											super.draw();
										}
									};
									help.setPos(300, -40, 0);
									t.getInputPanel().onInit();
									((GUIDialogWindow) t.getInputPanel().background).getMainContentPane().getContent(0).attach(help);
									t.activate();
									AudioController.fireAudioEvent("0022_action - enter digits in digital keypad", AudioController.ent(p.getSegmentController(), p, p.getAbsoluteIndex(), 2));
									return;
								}
							}
						} catch(NullPointerException ex) {
							System.err.println("[CLIENT][ERROR] Error using console:");
							ex.printStackTrace();
						}
						boolean active = !piece.isActive();
						piece.getSegment().getSegmentController().sendBlockActivation(ElementCollection.getEncodeActivation(piece, true, active, false));
						AudioController.fireAudioEvent("0022_action - enter digits in digital keypad", AudioController.ent(p.getSegmentController(), p, p.getAbsoluteIndex(), 2));
					}
				}
			}
		} else if(info.getId() == Blocks.PERSONAL_COMPUTER_BLUE.getId() || info.getId() == Blocks.PERSONAL_COMPUTER_RED.getId()) { //Personal Computer
			getState().getGlobalGameControlManager().getIngameControlManager().getPlayerMailManager().setActive(true);
			AudioController.fireAudioEvent("0022_action - enter digits in digital keypad", AudioController.ent(p.getSegmentController(), p, p.getAbsoluteIndex(), 2));
		} else if(info.isDoor()) { //Doors
			p.getSegment().getSegmentController().sendBlockActivation(ElementCollection.getEncodeActivation(p, true, !p.isActive(), false));
			if(!p.isActive()) {
				if(p.getInfo().getName().toLowerCase(Locale.ENGLISH).contains("forcefield")) {
					AudioController.fireAudioEvent("0022_item - forcefield activate", AudioController.ent(p.getSegmentController(), p, p.getAbsoluteIndex(), 2));
				} else AudioController.fireAudioEvent("0022_ambience sfx - air release steam valve", AudioController.ent(p.getSegmentController(), p, p.getAbsoluteIndex(), 2));
			} else {
				if(p.getInfo().getName().toLowerCase(Locale.ENGLISH).contains("forcefield")) {
					AudioController.fireAudioEvent("0022_item - forcefield powerdown", AudioController.ent(p.getSegmentController(), p, p.getAbsoluteIndex(), 2));
				} else AudioController.fireAudioEvent("0022_ambience sfx - air release steam valve", AudioController.ent(p.getSegmentController(), p, p.getAbsoluteIndex(), 2));
			}
		} else {
			if(!playerCharacterManager.canEnter(p.getType())) {
				System.err.println("[CLIENT] ACTIVATE BLOCK (std) " + p);
				PositionControl controlledElements = p.getSegment().getSegmentController().getControlElementMap().getControlledElements(ElementKeyMap.GRAVITY_ID, p.getAbsolutePos(new Vector3i()));
				if(controlledElements.getControlMap().size() > 0) {
					long grav = controlledElements.getControlPosMap().iterator().nextLong();
					SegmentPiece pointUnsave = p.getSegment().getSegmentController().getSegmentBuffer().getPointUnsave(grav);
					if(pointUnsave != null) {
						getState().getCharacter().activateGravity(pointUnsave);
					}
				}
				if(ElementKeyMap.getInfo(p.getType()).canActivate()) {
					boolean signalToSend = !p.isActive();
					// if(p.getType() == ElementKeyMap.SIGNAL_NOT_BLOCK_ID){
					// signalToSend = !signalToSend;
					// }
					if(p.getType() == ElementKeyMap.LOGIC_FLIP_FLOP && p.isActive()) {
						signalToSend = true;
					}
					long index = ElementCollection.getEncodeActivation(p, true, signalToSend, false);
					p.getSegment().getSegmentController().sendBlockActivation(index);
				}
			} else {
				boolean checkEnter;
				if(playerCharacterManager.isActive()) {
					checkEnter = playerCharacterManager.checkEnterAndEnterIfPossible(p);
					if(!checkEnter) getState().getController().popupAlertTextMessage(Lng.str("Cannot activate this block!"), 0);
				} else if(inShipControlManager.isActive()) {
					inShipControlManager.exitShip(false);
				}
			}
		}
	}

	/**
	 * @return the segmentControlManager
	 */
	public SegmentControlManager getSegmentControlManager() {
		return segmentControlManager;
	}

	public SimpleTransformableSendableObject getSelectedEntity() {
		return selectedEntity;
	}

	public void setSelectedEntity(SimpleTransformableSendableObject sendable) {
		final SimpleTransformableSendableObject<?> old = this.selectedEntity;
		this.selectedEntity = sendable;
		if(selectedEntity != null && selectedEntity.getUniqueIdentifier() != null && EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()) {
			GameClientState.debugSelectedObject = selectedEntity.getId();
		} else {
			GameClientState.debugSelectedObject = -1;
		}
		if(old != this.selectedEntity) {
			getState().getController().onSelectedEntityChanged(old, selectedEntity);
		}
	}

	public SimpleTransformableSendableObject getSelectedAITarget() {
		return selectedAITarget;
	}

	public void setSelectedAITarget(SimpleTransformableSendableObject t) {
		selectedAITarget = t;
		hasNotMadePin = true;
		if(t == null) {
			hasNotMadePin = false;
		}
	}

	/**
	 * @return the selectedElementClass
	 */
	public int getSelectedSlot() {
		return selectedSlot;
	}

	public int getSelectedSubSlot() {
		return selectedSubSlot;
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		if(PlayerInput.isDelayedFromMainMenuDeactivation()) {
			return;
		}
		if(e.isDebugKey()) {
			return;
		}
		if(getState().getWorldDrawer().getGuiDrawer().isMouseOnPanel() || PlayerInput.isDelayedFromMainMenuDeactivation() || getState().getController().getPlayerInputs().size() > 0) {
			return;
		}
		if(System.currentTimeMillis() - getState().getController().getInputController().getLastDeactivatedMenu() < MENU_DELAY_MS) {
			return;
		}
		if(e.isTriggered(KeyboardMappings.SWITCH_HOTBAR_WITH_LOOK_AT_BLOCK)) {
			switchHotbarWithLookAt();
			if(e.isTriggered(KeyboardMappings.SWITCH_TO_ENTITY)) switchToLookAt();
			return;
		}
		final boolean pasteMode = buildToolsManager.getCopyArea() != null && buildToolsManager.isPasteMode();
		final boolean zoomButton = e.isTriggered(KeyboardMappings.SCROLL_MOUSE_ZOOM_IN) || e.isTriggered(KeyboardMappings.SCROLL_MOUSE_ZOOM_OUT);
		final boolean createDockingMode = buildToolsManager.isInCreateDockingMode();
		if(Controller.getCamera() != null && zoomButton && !PlayerPanel.mouseInInfoScroll && !pasteMode) {
			Controller.getCamera().setAllowZoom(zoomButton);
		}
		boolean advBuildMode = isAdvancedBuildMode(getState());
		// System.err.println("MMMsadfasdf "+(!getBuildToolsManager().isInCreateDockingMode() &&
		// !advBuildMode && !pasteMode));
		if(!createDockingMode && !advBuildMode && !pasteMode) {
			int switchDir = 0;
			if(e.isTriggered(KeyboardMappings.NEXT_SLOT) || e.isTriggered(KeyboardMappings.PREVIOUS_SLOT)) {
				switchDir = e.isTriggered(KeyboardMappings.NEXT_SLOT) ? 1 : -1;
			}
			if(switchDir != 0) {
				forcedSelect = 0;
				//System.err.println("DWHEEL " + switchDir);
				InventorySlot oldSlot = getState().getPlayer().getInventory().getSlot(selectedSlot);
				// System.err.println("SELECTED::::: "+slot+"; "+(slot != null ? slot.isMultiSlot() : "")+" CURRENT: "+selectedSubSlot+" WHEEL: "+e.dWheel+" MINUS: "+(selectedSubSlot+(e.dWheel < 0 ? -1 : 0))+"; PLUS: "+(selectedSubSlot+(e.dWheel > 0 ? 1 : 0)));
				if(oldSlot != null && oldSlot.isMultiSlot() && (selectedSubSlot + (switchDir > 0 ? 1 : 0)) < oldSlot.getSubSlots().size() && (selectedSubSlot + (switchDir < 0 ? -1 : 0)) >= 0) {
					selectedSubSlot = FastMath.cyclicModulo(selectedSubSlot + (Integer.compare(switchDir, 0)), oldSlot.getSubSlots().size());
				} else if(oldSlot != null && oldSlot.getType() > Element.TYPE_NONE && ElementKeyMap.getInfoFast(oldSlot.getType()).blocktypeIds != null && (selectedSubSlot + (switchDir > 0 ? 1 : 0)) < getState().getBlockSyleSubSlotController().getSelectedStack(oldSlot.getType()).length && (selectedSubSlot + (switchDir < 0 ? -1 : 0)) >= 0) {
					short[] selectedStack = getState().getBlockSyleSubSlotController().getSelectedStack(oldSlot.getType());
					selectedSubSlot = FastMath.cyclicModulo(selectedSubSlot + (Integer.compare(switchDir, 0)), selectedStack.length);
				} else {
					selectedSlot = FastMath.cyclicModulo(selectedSlot + switchDir, 10);
					InventorySlot slotAfter = getState().getPlayer().getInventory().getSlot(selectedSlot);
					selectedSubSlot = 0;
					if(slotAfter != null && slotAfter.isMultiSlot()) {
						if(switchDir < 0) {
							selectedSubSlot = slotAfter.getSubSlots().size() - 1;
						}
					} else if(slotAfter != null && slotAfter.getType() > Element.TYPE_NONE && ElementKeyMap.getInfoFast(slotAfter.getType()).blocktypeIds != null) {
						if(switchDir < 0) {
							short[] selectedStack = getState().getBlockSyleSubSlotController().getSelectedStack(slotAfter.getType());
							selectedSubSlot = selectedStack.length - 1;
						}
					}
				}
				// System.err.println("SUBSLOTS: "+selectedSubSlot);
				getState().getPlayer().setSelectedBuildSlot(selectedSlot);
				checkOrienationForNewSelectedSlot();
			}
		}
		if(buildToolsManager.isSelectMode()) {
			buildToolsManager.getSelectMode().handleKeyEvent(this, e);
		}
		if(pasteMode) {
			Matrix3f rot = new Matrix3f();
			if(e.isTriggered(KeyboardMappings.NEXT_SLOT)) {
				System.err.println("[CLIENT] Rotating copy area +");
				if(KeyboardMappings.COPY_AREA_X_AXIS.isDown()) {
					buildToolsManager.getCopyArea().rotate(1, 0, 0);
				} else if(KeyboardMappings.COPY_AREA_Z_AXIS.isDown()) {
					buildToolsManager.getCopyArea().rotate(0, 0, 1);
				} else {
					buildToolsManager.getCopyArea().rotate(0, 1, 0);
				}
			} else if(e.isTriggered(KeyboardMappings.PREVIOUS_SLOT)) {
				System.err.println("[CLIENT] Rotating copy area -");
				if(KeyboardMappings.COPY_AREA_X_AXIS.isDown()) {
					buildToolsManager.getCopyArea().rotate(-1, 0, 0);
				} else if(KeyboardMappings.COPY_AREA_Z_AXIS.isDown()) {
					buildToolsManager.getCopyArea().rotate(0, 0, -1);
				} else {
					buildToolsManager.getCopyArea().rotate(0, -1, 0);
				}
			}
		}
		/*
		 * update dialogs
		 */
		int size = getState().getController().getPlayerInputs().size();
		if(size > 0) {
			// only the last in list is active
			getState().getController().getPlayerInputs().get(size - 1).handleKeyEvent(e);
			return;
		}
		if(e.getKeyboardKeyRaw() == GLFW.GLFW_KEY_ESCAPE) {
			stickyAdvBuildMode = false;
		}
		if(isInAnyStructureBuildMode()) {
			if(e.isTriggered(KeyboardMappings.BUILD_MODE_FIX_CAM)) {
				if(stickyAdvBuildMode) {
					System.err.println("[CLIENT] DEACTIVATED STICKY MODE");
					stickyAdvBuildMode = false;
				} else {
					if(System.currentTimeMillis() - lastPressedAdvBuildModeButton > EngineSettings.ADVANCED_BUILD_MODE_STICKY_DELAY.getInt()) {
						lastPressedAdvBuildModeButton = System.currentTimeMillis();
					} else {
						System.err.println("[CLIENT] ACTIVATED STICKY MODE");
						stickyAdvBuildMode = true;
					}
				}
			}
		} else {
			stickyAdvBuildMode = false;
		}
		boolean aiControl = KeyboardMappings.CREW_CONTROL.isDown();
		/*
		 * Update local player keys
		 */
		if(getState().getController().getPlayerInputs().isEmpty()) {
			if(e.isTriggered(KeyboardMappings.COPY_AREA_NEXT) || e.isTriggered(KeyboardMappings.COPY_AREA_PRIOR)) {
				if(buildToolsManager.getCopyArea() != null && buildToolsManager.isPasteMode()) {
					System.err.println("[CLIENT] Rotating copy area");
					Matrix3f rot = new Matrix3f();
					if(e.isTriggered(KeyboardMappings.COPY_AREA_NEXT)) {
						if(KeyboardMappings.COPY_AREA_X_AXIS.isDown()) {
							buildToolsManager.getCopyArea().rotate(1, 0, 0);
						} else if(KeyboardMappings.COPY_AREA_Z_AXIS.isDown()) {
							buildToolsManager.getCopyArea().rotate(0, 0, 1);
						} else {
							buildToolsManager.getCopyArea().rotate(0, 1, 0);
						}
					} else {
						if(e.isTriggered(KeyboardMappings.COPY_AREA_X_AXIS)) {
							buildToolsManager.getCopyArea().rotate(-1, 0, 0);
						} else if(KeyboardMappings.COPY_AREA_Z_AXIS.isDown()) {
							buildToolsManager.getCopyArea().rotate(0, 0, -1);
						} else {
							buildToolsManager.getCopyArea().rotate(0, -1, 0);
						}
					}
				}
			}
			if(aiControl && !inShipControlManager.getShipControlManager().getSegmentBuildController().isTreeActive() && !segmentControlManager.getSegmentBuildController().isTreeActive()) {
				checkAI(e);
			}
			if(e.isTriggered(KeyboardMappings.DROP_ITEM)) {
				InventorySlot slot = getState().getPlayer().getInventory().getSlot(selectedSlot);
				if(slot != null) {
					DragDrop d = new DragDrop();
					d.slot = selectedSlot;
					d.count = slot.count();
					d.type = slot.getType();
					d.subType = 0;
					d.invId = getState().getPlayer().getId();
					d.parameter = Long.MIN_VALUE;
					getState().getPlayer().getNetworkObject().dropOrPickupSlots.add(new RemoteDragDrop(d, getState().getPlayer().getNetworkObject()));
				}
			}
			if(e.isTriggered(KeyboardMappings.SELECT_ENTITY_NEXT)) {
				selectEntity(1);
			} else if(e.isTriggered(KeyboardMappings.SELECT_ENTITY_PREV)) {
				selectEntity(-1);
			} else if(e.isTriggered(KeyboardMappings.SELECT_NEAREST_ENTITY)) {
				selectNearestEntity();
			} else if(e.isTriggered(KeyboardMappings.SELECT_LOOK_ENTITY)) {
				selectLookingAt();
			} else if(e.isTriggered(KeyboardMappings.PIN_AI_TARGET)) {
				System.err.println("[CLIENT] SET PIN OF AI TARGET TO " + selectedEntity);
				setSelectedAITarget(selectedEntity);
			}
		}
		if(getState().getController().getPlayerInputs().isEmpty()) {
			super.handleKeyEvent(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.manager.AbstractControlManager#onSuspend(boolean)
	 */
	@Override
	protected void onSuspend(boolean suspend) {
		super.onSuspend(suspend);
		synchronized(getState()) {
			boolean syn = getState().isSynched();
			if(!syn) {
				getState().setSynched();
			}
			if(!syn) {
				getState().setUnsynched();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.manager.AbstractControlManager#onSwitch(boolean)
	 */
	@Override
	public void onSwitch(boolean active) {
		if(active) {
			if(!inShipControlManager.isActive() && !playerCharacterManager.isActive() && !playerCharacterManager.isDelayedActive() && !inShipControlManager.isDelayedActive()) {
				assert (getState().getCharacter() != null);
				playerCharacterManager.setDelayedActive(true);
			}
		}
		super.onSwitch(active);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.manager.AbstractControlManager#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer) {
		super.update(timer);
		if(undoRedoCooldown >= 0) {
			undoRedoCooldown -= timer.getDelta();
		}
		if(isInAnyStructureBuildMode()) {
			buildCommandManager.update(timer);
		} else {
			buildCommandManager.updateOnNotInBuildmode(timer);
		}
		if(getState().getController().getPlayerInputs().isEmpty()) {
			if(KeyboardMappings.OBJECT_VIEW_CAM.isDown() && (Controller.getCamera() == null || !(Controller.getCamera() instanceof ObjectViewerCamera))) {
				lastCamera = Controller.getCamera();
				lookCamera = new ObjectViewerCamera(getState(), new FixedViewer(getState().getCurrentPlayerObject()), getState().getCurrentPlayerObject());
				Controller.setCamera(lookCamera);
			} else {
				if(!KeyboardMappings.OBJECT_VIEW_CAM.isDown() && lookCamera != null && Controller.getCamera() == lookCamera) {
					System.err.println("REVERTED CAMERA");
					Controller.setCamera(lastCamera);
				}
			}
			if(KeyboardMappings.OBJECT_VIEW_CAM.isDown() && Keyboard.isKeyDown(GLFW.GLFW_KEY_1)) {
				getState().getCurrentPlayerObject().setInvisibleNextDraw();
			}
			assert (getState().getPlayerInputs().isEmpty());
			// chat is not active if this is active
			getState().getPlayer().getControllerState().sendInputToServer();
		}
	}

	public void setSelectedSlotForced(int slot, short searchForSubslot) {
		selectedSlot = slot;
		selectedSubSlot = 0;
		getState().getPlayer().setSelectedBuildSlot(selectedSlot);
		InventorySlot slotAfter = getState().getPlayer().getInventory().getSlot(selectedSlot);
		if(searchForSubslot != 0 && slotAfter != null && slotAfter.getType() == InventorySlot.MULTI_SLOT) {
			List<InventorySlot> subSlots = slotAfter.getSubSlots();
			for(int i = 0; i < subSlots.size(); i++) {
				if(subSlots.get(i).getType() == searchForSubslot) {
					selectedSubSlot = i;
				}
			}
		}
	}

	private void switchHotbarWithLookAt() {
		Vector3f camPos = new Vector3f(Controller.getCamera().getPos());
		if(isInAnyCharacterBuildMode() && getState().getCharacter() != null) {
			camPos.set(getState().getCharacter().getHeadWorldTransform().origin);
		}
		Vector3f camTo = new Vector3f(camPos);
		Vector3f forw = new Vector3f(Controller.getCamera().getForward());
		if(Float.isNaN(forw.x)) {
			return;
		}
		if(PlayerInteractionControlManager.isAdvancedBuildMode(getState())) {
			Vector3f mouse = new Vector3f(getState().getWorldDrawer().getAbsoluteMousePosition());
			forw.sub(mouse, camPos);
			forw.normalize();
		}
		forw.scale(100);
		camTo.add(forw);
		ClosestRayResultCallback testRayCollisionPoint = ((PhysicsExt) getState().getPhysics()).testRayCollisionPoint(camPos, camTo, false, null, null, false, true, false);
		if(testRayCollisionPoint.hasHit() && testRayCollisionPoint instanceof CubeRayCastResult) {
			CubeRayCastResult c = (CubeRayCastResult) testRayCollisionPoint;
			if(c.getSegment() != null) {
				SegmentPiece p = new SegmentPiece(c.getSegment(), c.getCubePos());
				short type = p.getType();
				System.err.println("[CLIENT] looking at block type: " + p.toString());
				Inventory inventory = getState().getPlayer().getInventory();
				for(int slot : inventory.getSlots()) {
					if(slot != selectedSlot) {
						InventorySlot act = inventory.getSlot(slot).getCompatible(type);
						if(act != null) {
							inventory.switchSlotsOrCombineClient(selectedSlot, slot, act.count());
						}
					}
				}
			}
		}
	}

	private void switchToLookAt() {
		Vector3f camPos = new Vector3f(Controller.getCamera().getPos());
		Vector3f camTo = new Vector3f(Controller.getCamera().getPos());
		Vector3f forw = new Vector3f(Controller.getCamera().getForward());
		forw.scale(200);
		camTo.add(forw);
		CubeRayCastResult rayCallback = new CubeRayCastResult(camPos, camTo, null);
		rayCallback.setIgnoereNotPhysical(true);
		rayCallback.setOnlyCubeMeshes(true);
		ClosestRayResultCallback testRayCollisionPoint = ((PhysicsExt) getState().getPhysics()).testRayCollisionPoint(camPos, camTo, rayCallback, false);
		if(testRayCollisionPoint != null && testRayCollisionPoint.hasHit() && rayCallback.getSegment() != null) {
			SegmentController segmentController = rayCallback.getSegment().getSegmentController();
			if(segmentController != getState().getCurrentPlayerObject() && getState().getCurrentPlayerObject() != null && (getState().getCurrentPlayerObject() instanceof SegmentController) && ((SegmentController) getState().getCurrentPlayerObject()).railController.isInAnyRailRelationWith(segmentController) && InShipControlManager.checkEnter(segmentController)) {
				InShipControlManager.switchEntered(segmentController);
			}
		}
	}

	private void controlAI(KeyEventInterface e) throws StateParameterNotFoundException, PlayerControlledTransformableNotFound, UnloadedAiEntityException {
		SimpleTransformableSendableObject<?> lookingAt = getLookingAt(getState(), true);
		SegmentPiece p = null;
		if(getState().getCharacter().isConrolledByActivePlayer()) {
			System.err.println("[AIPLAYERINTERACTION] CHECK FOR NEAREST PIECE");
			p = getState().getCharacter().getNearestPiece(40, true);
		} else {
			System.err.println("[AIPLAYERINTERACTION] NO CHECK FOR NEAREST PIECE");
		}
		switch(e.getKeyboardKeyRaw()) {
			case GLFW.GLFW_KEY_1:
				((AIGameConfiguration<?, ?>) selectedCrew.getAi().getAiConfiguration()).get(Types.ORDER).switchSetting(AIGameCreatureConfiguration.BEHAVIOR_IDLING, true);
				getState().getController().popupGameTextMessage(Lng.str("%s now idling...", selectedCrew.getRealName()), 0);
				break;
			case GLFW.GLFW_KEY_2:
				if(lookingAt != null && lookingAt == selectedCrew) {
					getState().getController().popupGameTextMessage(Lng.str("%s not attacking itself", selectedCrew.getRealName()), 0);
					((AIGameConfiguration<?, ?>) selectedCrew.getAi().getAiConfiguration()).get(Types.ATTACK_TARGET).switchSetting("none", true);
					((AIGameConfiguration<?, ?>) selectedCrew.getAi().getAiConfiguration()).get(Types.ORDER).switchSetting(AIGameCreatureConfiguration.BEHAVIOR_IDLING, true);
				} else {
					System.err.println("[PLAYERINTERACTION] AIselected: " + selectedSlot + " -> ATTACK: " + lookingAt);
					if(lookingAt != null) {
						((AIGameConfiguration<?, ?>) selectedCrew.getAi().getAiConfiguration()).get(Types.ATTACK_TARGET).switchSetting(lookingAt.getUniqueIdentifier(), true);
						((AIGameConfiguration<?, ?>) selectedCrew.getAi().getAiConfiguration()).get(Types.ORDER).switchSetting(AIGameCreatureConfiguration.BEHAVIOR_ATTACKING, true);
						getState().getController().popupGameTextMessage(Lng.str("%s attack\n%s", selectedCrew.getRealName(), lookingAt.toNiceString()), 0);
					} else {
						getState().getController().popupAlertTextMessage(Lng.str("Cannot attack: no target!"), 0);
					}
				}
				break;
			case GLFW.GLFW_KEY_3: {
				if(p != null) {
					Vector3i absolutePos = p.getAbsolutePos(new Vector3i());
					((AIGameConfiguration<?, ?>) selectedCrew.getAi().getAiConfiguration()).get(Types.ORIGIN_X).switchSetting(String.valueOf(absolutePos.x), true);
					((AIGameConfiguration<?, ?>) selectedCrew.getAi().getAiConfiguration()).get(Types.ORIGIN_Y).switchSetting(String.valueOf(absolutePos.y), true);
					((AIGameConfiguration<?, ?>) selectedCrew.getAi().getAiConfiguration()).get(Types.ORIGIN_Z).switchSetting(String.valueOf(absolutePos.z), true);
					((AIGameConfiguration<?, ?>) selectedCrew.getAi().getAiConfiguration()).get(Types.ORDER).switchSetting(AIGameCreatureConfiguration.BEHAVIOR_ROAMING, true);
					getState().getController().popupGameTextMessage(Lng.str("%s roaming at \n%s in \n%s", selectedCrew.getRealName(), absolutePos, p.getSegment().getSegmentController().toNiceString()), 0);
				} else {
					((AIGameConfiguration<?, ?>) selectedCrew.getAi().getAiConfiguration()).get(Types.ORDER).switchSetting(AIGameCreatureConfiguration.BEHAVIOR_ROAMING, true);
					getState().getController().popupGameTextMessage(Lng.str("%s now roaming", selectedCrew.getRealName()), 0);
				}
				break;
			}
			case GLFW.GLFW_KEY_4:
				((AIGameConfiguration<?, ?>) selectedCrew.getAi().getAiConfiguration()).get(Types.FOLLOW_TARGET).switchSetting(getState().getPlayer().getFirstControlledTransformable().getUniqueIdentifier(), true);
				((AIGameConfiguration<?, ?>) selectedCrew.getAi().getAiConfiguration()).get(Types.ORDER).switchSetting(AIGameCreatureConfiguration.BEHAVIOR_FOLLOWING, true);
				getState().getController().popupGameTextMessage(Lng.str("%s now following %s", selectedCrew.getRealName(), getState().getPlayer().getFirstControlledTransformable().toNiceString()), 0);
				break;
			case GLFW.GLFW_KEY_5: {
				if(p != null) {
					Vector3i absolutePos = p.getAbsolutePos(new Vector3i());
					((AIGameConfiguration<?, ?>) selectedCrew.getAi().getAiConfiguration()).get(Types.TARGET_AFFINITY).switchSetting(String.valueOf(p.getSegment().getSegmentController().getUniqueIdentifier()), true);
					((AIGameConfiguration<?, ?>) selectedCrew.getAi().getAiConfiguration()).get(Types.TARGET_X).switchSetting(String.valueOf(absolutePos.x - SegmentData.SEG_HALF), true);
					((AIGameConfiguration<?, ?>) selectedCrew.getAi().getAiConfiguration()).get(Types.TARGET_Y).switchSetting(String.valueOf(absolutePos.y - SegmentData.SEG_HALF), true);
					((AIGameConfiguration<?, ?>) selectedCrew.getAi().getAiConfiguration()).get(Types.TARGET_Z).switchSetting(String.valueOf(absolutePos.z - SegmentData.SEG_HALF), true);
					((AIGameConfiguration<?, ?>) selectedCrew.getAi().getAiConfiguration()).get(Types.ORDER).switchSetting(AIGameCreatureConfiguration.BEHAVIOR_GOTO, true);
					getState().getController().popupGameTextMessage(Lng.str("%s moving to \n%s in \n%s", selectedCrew.getRealName(), absolutePos, p.getSegment().getSegmentController().toNiceString()), 0);
				} else {
					getState().getController().popupAlertTextMessage(Lng.str("Cannot move: no target!"), 0);
				}
				break;
			}
		}
	}

	private void checkAI(KeyEventInterface e) {
		if(!e.isSlotKey()) {
			return;
		}
		int slot = e.getSlotKey();
		System.err.println("[PLAYERINTERACTION] CHECK AI SLOT: " + slot + " selected: " + selectedSlot);
		if(selectedCrew == null) {
			if(slot >= 0 && slot < 5) {
				List<AiInterfaceContainer> crew = getState().getPlayer().getPlayerAiManager().getCrew();
				if(slot < crew.size()) {
					selectedCrew = crew.get(slot);
				} else {
					selectedCrew = null;
					getState().getController().popupAlertTextMessage(Lng.str("You don't have crew in this slot."), 0);
				}
			} else {
				selectedCrew = null;
			}
		} else {
			try {
				if(slot >= 0 && slot < 5) {
					try {
						controlAI(e);
					} catch(UnloadedAiEntityException ex) {
						ex.printStackTrace();
						getState().getController().popupAlertTextMessage(Lng.str("AI Entity Not Loaded:\n") + ex.uid, 0);
						selectedCrew = null;
					}
				} else {
					selectedCrew = null;
				}
			} catch(StateParameterNotFoundException ex) {
				ex.printStackTrace();
			} catch(PlayerControlledTransformableNotFound ex) {
				ex.printStackTrace();
			}
		}
	}

	public void handleKeyOrientation(KeyEventInterface e) {
		Transform target = null;
		Transform viewer = Controller.getCamera().getWorldTransform();
		if(getState().getShip() != null) {
			target = getState().getShip().getWorldTransform();
		} else {
			// target = Controller.getCamera().getWorldTransform();
			target = new Transform();
			target.setIdentity();
		}
		if(e.isDebugKey() && e.getKey() == GLFW.GLFW_KEY_PERIOD) {
			int maxRotation = GUIOrientationSettingElement.getMaxRotation(this);
			blockOrientation = FastMath.cyclicModulo(blockOrientation + 1, maxRotation);
			short type = getSelectedType();
			if(type != Element.TYPE_NONE) {
				ElementInformation info = ElementKeyMap.getInfo(type);
				if(!info.isNormalBlockStyle()) {
					System.err.println("BLOCK ORIENTATION: " + blockOrientation + "; " + BlockShapeAlgorithm.algorithms[info.getBlockStyle().id - 1][blockOrientation].toString());
				}
			}
		}
	}

	public boolean handleSlotKey(KeyEventInterface e) {
		if(!e.isSlotKey() || KeyboardMappings.CREW_CONTROL.isDown()) {
			return false;
		}
		short selectedType = getSelectedType();
		boolean same = selectedSlot == e.getSlotKey();
		selectedSlot = e.getSlotKey();
		if(forcedSelect != 0) {
			forcedSelect = 0;
		}
		boolean stacked = false;
		if(ElementKeyMap.isValidType(selectedType) && ElementKeyMap.getInfoFast(selectedType).blocktypeIds != null) {
			stacked = true;
		}
		if(!same || (getSelectedType() != InventorySlot.MULTI_SLOT && !stacked)) {
			selectedSubSlot = 0;
		} else {
			InventorySlot slot = getState().getPlayer().getInventory().getSlot(selectedSlot);
			if(slot != null && slot.isMultiSlot()) {
				selectedSubSlot = FastMath.cyclicModulo(selectedSubSlot + 1, slot.getSubSlots().size());
			} else if(slot != null && slot.getType() > Element.TYPE_NONE && ElementKeyMap.getInfoFast(slot.getType()).blocktypeIds != null) {
				short[] selectedStack = getState().getBlockSyleSubSlotController().getSelectedStack(slot.getType());
				selectedSubSlot = FastMath.cyclicModulo(selectedSubSlot + 1, selectedStack.length);
			} else {
				selectedSubSlot = 0;
			}
		}
		getState().getPlayer().setSelectedBuildSlot(selectedSlot);
		checkOrienationForNewSelectedSlot();
		// System.err.println("Selected slot is now: " + selectedSlot);
		return true;
	}

	public void checkOrienationForNewSelectedSlot() {
		if(lastSelectedType != getSelectedTypeWithSub()) {
			if(getSelectedTypeWithSub() > 0) {
				ElementInformation infoNew = ElementKeyMap.getInfo(getSelectedTypeWithSub());
				if(lastSelectedType <= 0 || (!infoNew.isOrientatable() && infoNew.isNormalBlockStyle())) {
					int lastOrientation = blockOrientation;
					// System.err.println("[CLIENT][BLOCKORIENTATION] BLOCK ORIENTATION FOR " + infoNew.getName() + ": " + Element.getSideString(infoNew.getDefaultOrientation()));
					// changed from nothing
					if(blockStyleMap.containsKey(infoNew.getBlockStyle())) {
						blockOrientation = blockStyleMap.get(infoNew.getBlockStyle());
					} else {
						blockOrientation = infoNew.getDefaultOrientation();
					}
					if(ElementKeyMap.isValidType(lastSelectedType)) {
						ElementInformation infoOld = ElementKeyMap.getInfo(lastSelectedType);
						if(infoOld.getBlockStyle() != BlockStyle.NORMAL) {
							blockStyleMap.put(infoOld.getBlockStyle(), lastOrientation);
						}
					}
				} else {
					ElementInformation infoOld = ElementKeyMap.getInfo(lastSelectedType);
					int lastOrientation = blockOrientation;
					if(infoNew.getBlockStyle() == BlockStyle.NORMAL) {
						if(infoNew.getSlab() != 0) {
							blockOrientation = lastSelectedSlabOrient;
						} else {
							// System.err.println("[CLIENT][BLOCKORIENTATION] SETTING BBB TO DEFAULT:: " + infoNew.getName());
							blockOrientation = infoNew.getDefaultOrientation();
						}
					} else {
						if(infoNew.getBlockStyle() != infoOld.getBlockStyle()) {
							// System.err.println("[CLIENT][BLOCKORIENTATION] STYLE CHANGED: LOADING :: " + infoOld.getName() + " : :: " + infoNew.getName());
							// only record if blockstyle has changed
							if(blockStyleMap.containsKey(infoNew.getBlockStyle())) {
								blockOrientation = blockStyleMap.get(infoNew.getBlockStyle());
							} else {
								blockStyleMap.put(infoOld.getBlockStyle(), blockOrientation);
							}
						}
					}
					if(infoOld.getBlockStyle() != BlockStyle.NORMAL) {
						blockStyleMap.put(infoOld.getBlockStyle(), lastOrientation);
					}
				}
			}
		}
		if(getSelectedTypeWithSub() > 0) {
			lastSelectedType = getSelectedTypeWithSub();
			if(ElementKeyMap.isValidType(lastSelectedType) && ElementKeyMap.getInfoFast(lastSelectedType).getSlab() > 0) {
				lastSelectedSlabOrient = blockOrientation;
			}
		}
	}

	public short getSelectedTypeWithSub() {
		if(forcedSelect != 0) {
			return forcedSelect;
		}
		InventorySlot slot = getState().getPlayer().getInventory().getSlot(selectedSlot);
		boolean stacked = false;
		short type;
		if(slot != null && slot.isMultiSlot() && selectedSubSlot >= 0 && selectedSubSlot < slot.getSubSlots().size()) {
			type = slot.getSubSlots().get(selectedSubSlot).getType();
		} else if(slot != null && slot.getType() > Element.TYPE_NONE && ElementKeyMap.getInfoFast(slot.getType()).blocktypeIds != null) {
			stacked = true;
			if(selectedSubSlot >= getState().getBlockSyleSubSlotController().getSelectedStack(slot.getType()).length) {
				resetSubSlot();
			}
			type = getState().getBlockSyleSubSlotController().getBlockType(slot.getType(), selectedSubSlot);
		} else {
			type = getSelectedType();
		}
		if(ElementKeyMap.isValidType(type) && !stacked && ElementKeyMap.getInfoFast(type).slabIds != null && buildToolsManager.slabSize.setting > 0) {
			type = ElementKeyMap.getInfoFast(type).slabIds[buildToolsManager.slabSize.setting - 1];
		}
		return type;
	}

	public void resetSubSlot() {
		selectedSubSlot = 0;
	}

	public void removeBlock(final EditableSendableSegmentController controller, Vector3f pos, Vector3f dir, final SegmentPiece selectedBlock, float editDistance, SymmetryPlanes symmetryPlanes, short filter, final RemoveCallback removeCallback) {
		if(!getState().getController().allowedToEdit(controller)) {
			return;
		}
		if(controller.isInTestSector()) {
			getState().getController().popupInfoTextMessage(Lng.str("Cannot build in test sector!"), 0);
			return;
		}
		if(!isAllowedToBuildAndSpawnShips()) {
			getState().getController().popupAlertTextMessage("ERROR\n \nCan't do that!\nYou are a spectator!", 0);
			return;
		}
		short replaceFilterWith = Element.TYPE_NONE;
		if(filter == Element.TYPE_ALL && buildToolsManager.getRemoveFilter() != 0) {
			filter = buildToolsManager.getRemoveFilter();
			if(buildToolsManager.isReplaceRemoveFilter()) {
				short selectedType = getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getSelectedTypeWithSub();
				if(ElementKeyMap.isValidType(selectedType)) {
					System.err.println("[CLIENT] Replace filter: replace: " + ElementKeyMap.toString(filter) + " with " + ElementKeyMap.toString(selectedType));
					replaceFilterWith = selectedType;
				}
			}
		}
		BuildHelper posesFilter = null;
		if(buildToolsManager.getBuildHelper() != null && buildToolsManager.buildHelperReplace) {
			posesFilter = buildToolsManager.getBuildHelper();
		}
		Vector3i size = new Vector3i(1, 1, 1);
		if(isAdvancedBuildMode(getState())) {
			size.set(buildToolsManager.getSize());
		}
		if(controller.getHpController().isRebooting()) {
			getState().getController().popupAlertTextMessage(Lng.str("Cannot remove blocks\nwhile rebooting!\n(%s)", StringTools.formatTimeFromMS(controller.getHpController().getRebootTimeLeftMS())), 0);
			return;
		}
		if(controller.hasStructureAndArmorHP() && controller.getHpController().getHpPercent() < 1d && !warned.contains(controller.getId())) {
			warned.add(controller.getId());
			PlayerGameOkCancelInput check = new PlayerGameOkCancelInput("CONFIRM", getState(), Lng.str("Warning SHP"), Lng.str("Placing blocks on a damaged ship\ncan lead to negative effects!\nPlease reboot the ship before placing\nany blocks.")) {
				@Override
				public boolean isOccluded() {
					return false;
				}

				@Override
				public void onDeactivate() {
				}

				@Override
				public void pressedOK() {
					deactivate();
					getInShipControlManager().popupShipRebootDialog(controller);
				}
			};
			check.getInputPanel().onInit();
			check.getInputPanel().setOkButtonText(Lng.str("Reboot"));
			check.getInputPanel().setCancelButtonText(Lng.str("Ignore"));
			check.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(173);
		}
		if(controller.isScrap()) {
			getState().getController().popupAlertTextMessage(Lng.str("The blocks on this Station are\nworn and decayed!\nYou can only salvage scrap!\n(Press %s on the station to repair)", KeyboardMappings.SPAWN_SPACE_STATION.getKeyChar()), 0);
		}
		// final IntOpenHashSet modifiedSlots = new IntOpenHashSet(size.x*size.y*size.z);
		BuildInstruction buildInstruction = new BuildInstruction(controller);
		moddedSegs.clear();
		controller.getNearestIntersectingElementPosition(pos, dir, size, editDistance, new BuildRemoveCallback() {
			@Override
			public void onRemove(long pos, short type) {
				if(type != Element.TYPE_NONE && type != ElementKeyMap.CORE_ID) {
				}
				removeCallback.onRemove(pos, type);
			}

			@Override
			public boolean canRemove(short type) {
				if(ElementKeyMap.isValidType(type)) {
					ElementInformation info = ElementKeyMap.getInfo(type);
					if((info.isReactorChamberAny() || info.getId() == ElementKeyMap.REACTOR_MAIN) && controller instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) controller).getManagerContainer().getPowerInterface().isAnyRebooting()) {
						getState().getController().popupAlertTextMessage(Lng.str("Cannot modify reactor configuration while a reactor is rebooting!"));
						return false;
					}
				}
				boolean canPutIn = getState().getPlayer().getInventory().canPutIn(type, 1);
				if(!canPutIn) {
					getState().getController().popupAlertTextMessage(Lng.str("Can't remove block!\nInventory full"), 0);
				}
				return canPutIn;
			}

			@Override
			public long getSelectedControllerPos() {
				if(selectedBlock != null) {
					return selectedBlock.getAbsoluteIndex();
				}
				return Long.MIN_VALUE;
			}
		}, symmetryPlanes, filter, replaceFilterWith, posesFilter, buildInstruction, moddedSegs);
		moddedSegs.clear();
		/*AudioController.fireAudioEvent(AudioTags.BLOCK, AudioTags.BUILD, AudioTags.REMOVE)*/
		AudioController.fireAudioEventID(174);
		undo.add(0, buildInstruction);
		while(undo.size() > EngineSettings.B_UNDO_REDO_MAX.getInt()) {
			undo.remove(undo.size() - 1);
		}
		// When a block is removed, remove any previous redos
		redo.clear();
		// getState().getPlayer().sendInventoryModification(modifiedSlots, null);
	}

	private void selectEntity(int i) {
		ObjectIterator<SimpleTransformableSendableObject<?>> iterator = getState().getCurrentSectorEntities().values().iterator();
		int nearest = i > 0 ? Integer.MAX_VALUE : Integer.MIN_VALUE;
		int lastId = selectedEntity != null ? selectedEntity.getId() : -1;
		while(iterator.hasNext()) {
			Sendable nextSendable = iterator.next();
			SimpleTransformableSendableObject<?> next = (SimpleTransformableSendableObject<?>) nextSendable;
			if(selectedEntity == null) {
				selectEntityMax(-i);
				return;
			}
			if(i > 0 && next.getId() > lastId && next.getId() < nearest) {
				nearest = next.getId();
			}
			if(i <= 0 && next.getId() < lastId && next.getId() > nearest) {
				nearest = next.getId();
			}
		}
		if(nearest == Integer.MAX_VALUE || nearest == Integer.MIN_VALUE) {
			selectEntityMax(i);
			return;
		}
		if(getState().getLocalAndRemoteObjectContainer().getLocalObjects().containsKey(nearest)) {
			SimpleTransformableSendableObject s = (SimpleTransformableSendableObject) getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(nearest);
			setSelectedEntity(s);
		}
	}

	private void selectEntityMax(int i) {
		Iterator<SimpleTransformableSendableObject<?>> iterator = getState().getCurrentSectorEntities().values().iterator();
		int nearest = i > 0 ? Integer.MAX_VALUE : Integer.MIN_VALUE;
		while(iterator.hasNext()) {
			SimpleTransformableSendableObject<?> next = iterator.next();
			if(i > 0 && next.getId() < nearest) {
				nearest = next.getId();
			}
			if(i <= 0 && next.getId() > nearest) {
				nearest = next.getId();
			}
		}
		if(getState().getLocalAndRemoteObjectContainer().getLocalObjects().containsKey(nearest)) {
			SimpleTransformableSendableObject s = (SimpleTransformableSendableObject) getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(nearest);
			setSelectedEntity(s);
		}
	}

	public NavigationControllerManager getNavigationControllerManager() {
		return getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getNavigationControlManager();
	}

	// old from shipExternalFlightController
	// private SimpleTransformableSendableObject selectLookingAt(boolean respectFilters) {
	// if (getState().getScene() == null || getState().getScene().getWorldToScreenConverter() == null) {
	// return null;
	// }
	//
	// WorldToScreenConverter worldToScreenConverter = getState().getScene().getWorldToScreenConverter();
	//
	// Vector3f dir = new Vector3f();
	// float minDist = 0;
	// float minDist2 = -1;
	// SimpleTransformableSendableObject nearest = null;
	// Vector3f middle = worldToScreenConverter.getMiddleOfScreen(new Vector3f());
	// for (SimpleTransformableSendableObject to : getState().getCurrentSectorEntities().values()) {
	// if (to == getState().getCurrentPlayerObject() || to.isHidden()) {
	// continue;
	// }
	//
	// //don't include own turrets or mothership
	// if (to instanceof SegmentController && getState().getCurrentPlayerObject() instanceof SegmentController &&
	// (((SegmentController) to).railController.isInAnyRailRelationWith((SegmentController) getState().getCurrentPlayerObject()) ||
	// (((SegmentController) to).getDockingController().isInAnyDockingRelation((SegmentController) getState().getCurrentPlayerObject())))) {
	// continue;
	// }
	//
	// if (respectFilters && !getNavigationControllerManager().isDisplayed(to)) {
	// continue;
	// }
	//
	// dir.set(to.getWorldTransformOnClient().origin);
	// dir.sub(Controller.getCamera().getWorldTransform().origin);
	// dir.normalize();
	//
	// Vector3f facingDir = Controller.getCamera().getForward();
	// facingDir.normalize();
	//
	// Vector3f posOnScreen = worldToScreenConverter.convert(to.getWorldTransformOnClient().origin, new Vector3f(), true);
	//
	// Vector3f distFromMiddle = new Vector3f();
	// Vector3f distFromObj = new Vector3f();
	// distFromMiddle.sub(posOnScreen, middle);
	// distFromObj.sub(getShip().getWorldTransform().origin, to.getWorldTransformOnClient().origin);
	//
	// //					System.err.println("DIST FROM MIDDLE: "+distFromMiddle.length());
	// if (distFromMiddle.length() < 90 && facingDir.dot(dir) > 0.9) {
	// if (nearest == null || (distFromMiddle.length() < minDist && distFromObj.length() < minDist2)) {
	// nearest = to;
	// minDist = distFromMiddle.length();
	// minDist2 = distFromObj.length();
	// }
	// }
	// }
	// return nearest;
	// }
	private void selectLookingAt() {
		setSelectedEntity(getLookingAt(getState(), true));
	}

	private void selectNearestEntity() {
		Vector3f pos = new Vector3f();
		Vector3f dir = new Vector3f();
		float lastDist = 0;
		if(getState().getCurrentPlayerObject() != null) {
			pos.set(getState().getCurrentPlayerObject().getWorldTransform().origin);
		} else {
			pos.set(Controller.getCamera().getPos());
		}
		SimpleTransformableSendableObject nearest = null;
		for(SimpleTransformableSendableObject next : getState().getCurrentSectorEntities().values()) {
			// System.err.println("TESTING "+next);
			dir.sub(next.getWorldTransformOnClient().origin, pos);
			if(next != getState().getCurrentPlayerObject() && next != getState().getPlayer().getAbstractCharacterObject() && (nearest == null || dir.length() < lastDist)) {
				System.err.println("!!!!!!!!!NEAREST IS NOW " + next);
				nearest = next;
				lastDist = dir.length();
			}
		}
		setSelectedEntity(nearest);
	}

	/**
	 * @return the selectedCrew
	 */
	public AiInterfaceContainer getSelectedCrew() {
		return selectedCrew;
	}

	/**
	 * @param selectedCrew the selectedCrew to set
	 */
	public void setSelectedCrew(AiInterfaceContainer selectedCrew) {
		this.selectedCrew = selectedCrew;
	}

	/**
	 * @return the entered
	 */
	public SegmentPiece getEntered() {
		return entered;
	}

	/**
	 * @param entered the entered to set
	 */
	public void setEntered(SegmentPiece entered) {
		// try {
		// throw new Exception("[CLIENT] CHANGED ENTERED: set to: " + entered);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		System.err.println("[CLIENT] CHANGED ENTERED: set to: " + entered);
		this.entered = entered;
		if(entered == null) {
			getState().getWorldDrawer().flagJustEntered(null);
		}
	}

	public boolean canUndo() {
		// && undo.get(undo.size()-1).fitsUndo(getState().getPlayer().getInventory());
		return !undo.isEmpty();
	}

	public boolean canRedo() {
		return !redo.isEmpty();
	}

	public void undo() {
		try {
			if((undoRedoCooldown < 0 || GameServerState.isCreated()) && entered != null && undo.size() > 0 && undo.get(0).getController() == entered.getSegment().getSegmentController() && GameClient.getClientController().allowedToEdit(entered.getSegmentController())) {
				BuildInstruction e = undo.remove(0);
				BuildInstruction n = new BuildInstruction(e.getController());
				n.fillTool = e.fillTool;
				undoRedoCooldown = Math.min(5.0F, (e.getAdds().size() + e.getRemoves().size() + e.getReplaces().size() * 2) * 0.0005F);
				((EditableSendableSegmentController) e.getController()).undo(e, n);
				redo.add(0, e);
				System.err.println("[UNDO] ADDING REDO " + e);
				while(redo.size() > EngineSettings.B_UNDO_REDO_MAX.getInt()) {
					redo.remove(redo.size() - 1);
				}
			} else {
				if(undoRedoCooldown >= 0) {
					getState().getController().popupAlertTextMessage(Lng.str("UNDO/REDO is currently on cooldown avoid bandwidth spam."));
				} else {
					getState().getController().popupAlertTextMessage(Lng.str("Cannot undo as you don't have permission to edit this entity!"));
				}
				System.err.println("[UNDO] CANNOT UNDO AT THE MOMENT: " + (undoRedoCooldown < 0) + ", entered: " + (entered != null) + ", undoavail: " + undo.size() + " - " + (undo.size() > 0) + ", contr: " + (undo.get(0).getController() == entered.getSegment().getSegmentController()));
			}
		} catch(Exception e) {
			System.err.println("ERROR IN REDO");
			e.printStackTrace();
		}
	}

	public void redo() {
		try {
			if(undoRedoCooldown < 0 && entered != null && redo.size() > 0 && redo.get(0).getController() == entered.getSegment().getSegmentController() && GameClient.getClientController().allowedToEdit(entered.getSegmentController())) {
				BuildInstruction e = redo.remove(0);
				BuildInstruction n = new BuildInstruction(e.getController());
				n.fillTool = e.fillTool;
				undoRedoCooldown = Math.min(5.0F, (e.getAdds().size() + e.getRemoves().size() + e.getReplaces().size() * 2) * 0.0005F);
				((EditableSendableSegmentController) e.getController()).redo(e, n);
				undo.add(0, n);
				System.err.println("[REDO] ADDING UNDO " + e);
				while(undo.size() > EngineSettings.B_UNDO_REDO_MAX.getInt()) {
					undo.remove(undo.size() - 1);
				}
			}
		} catch(Exception e) {
			System.err.println("ERROR IN REDO");
			e.printStackTrace();
		}
	}

	public boolean isUndoRedoOnCooldown() {
		return undoRedoCooldown >= 0;
	}

	public boolean isMultiSlot() {
		InventorySlot slot = getState().getPlayer().getInventory().getSlot(selectedSlot);
		return slot != null && slot.getType() == InventorySlot.MULTI_SLOT;
	}

	public void removeLayer(int x, int y, int z) {
		SimpleTransformableSendableObject s = selectedEntity;
		if(s != null && s instanceof SegmentController && getState().getPlayer().isAdmin()) {
			EditableSendableSegmentController seg = (EditableSendableSegmentController) s;
			BoundingBox boundingBox = seg.getBoundingBox();
			BoundingBox normBB = new BoundingBox(boundingBox);
			normBB.min.x += SegmentData.SEG_HALF + 1;
			normBB.min.y += SegmentData.SEG_HALF + 1;
			normBB.min.z += SegmentData.SEG_HALF + 1;
			normBB.max.x += SegmentData.SEG_HALF - 1;
			normBB.max.y += SegmentData.SEG_HALF - 1;
			normBB.max.z += SegmentData.SEG_HALF - 1;
			BoundingBox toDel = new BoundingBox(normBB);
			System.err.println("[CLIENT] BB: " + toDel + "; " + seg);
			if(x > 0) {
				toDel.min.x = toDel.max.x - Math.abs(x);
				toDel.max.x = toDel.min.x + 1;
			}
			if(y > 0) {
				toDel.min.y = toDel.max.y - Math.abs(y);
				toDel.max.y = toDel.min.y + 1;
			}
			if(z > 0) {
				toDel.min.z = toDel.max.z - Math.abs(z);
				toDel.max.z = toDel.min.z + 1;
			}
			if(x < 0) {
				toDel.max.x = toDel.min.x + Math.abs(x);
				toDel.min.x = toDel.max.x - 1;
			}
			if(y < 0) {
				toDel.max.y = toDel.min.y + Math.abs(y);
				toDel.min.y = toDel.max.y - 1;
			}
			if(z < 0) {
				toDel.max.z = toDel.min.z + Math.abs(z);
				toDel.min.z = toDel.max.z - 1;
			}
			if(!toDel.isValidIncludingZero() || !toDel.intersects(normBB)) {
				return;
			}
			int removed = removeLayer(toDel, seg);
			if(removed == 0) {
				System.err.println("LAYER ::: " + toDel);
				removeLayer(x + FastMath.sign(x), y + FastMath.sign(y), z + FastMath.sign(z));
			}
		}
	}

	public int removeLayer(BoundingBox toDel, EditableSendableSegmentController seg) {
		ObjectOpenHashSet<Segment> oo = new ObjectOpenHashSet<Segment>(16);
		BuildInstruction d = new BuildInstruction(seg);
		for(int z = (int) toDel.min.z; z < toDel.max.z; z++) {
			for(int y = (int) toDel.min.y; y < toDel.max.y; y++) {
				for(int x = (int) toDel.min.x; x < toDel.max.x; x++) {
					// System.err.println("REMOVING "+x+", "+y+", "+z);
					seg.remove(x, y, z, new BuildRemoveCallback() {
						@Override
						public long getSelectedControllerPos() {
							return Long.MIN_VALUE;
						}

						@Override
						public void onRemove(long pos, short type) {
						}

						@Override
						public boolean canRemove(short type) {
							return true;
						}
					}, true, oo, Element.TYPE_ALL, Element.TYPE_NONE, blockOrientation, null, d);
				}
			}
		}
		for(Segment s : oo) {
			if(!s.isEmpty()) {
				s.getSegmentData().restructBB(true);
			} else {
				seg.getSegmentBuffer().restructBB();
			}
		}
		if(d.getRemoves().size() > 0) {
			undo.add(0, d);
			while(undo.size() > EngineSettings.B_UNDO_REDO_MAX.getInt()) {
				undo.remove(undo.size() - 1);
			}
			// When a block is removed, remove any previous redos
			redo.clear();
		}
		return d.getRemoves().size();
	}

	public void debugPush() {
		SimpleTransformableSendableObject s = selectedEntity;
		if(s != null && s instanceof SegmentController) {
			RigidBody r = ((RigidBody) s.getPhysicsDataContainer().getObject());
			r.setLinearVelocity(new Vector3f(0, 100, 1));
			r.setAngularVelocity(new Vector3f(0, 30, 0));
		}
	}

	public void openRadialSelectShapes(short selectedType) {
		if(ElementKeyMap.isValidType(selectedType)) {
			ElementInformation sourceBlock = ElementKeyMap.getInfoFast(selectedType);
			System.out.println("RADIAL radial shape open for " + sourceBlock.getName());
			if(sourceBlock.blocktypeIds != null || sourceBlock.getSourceReference() != 0) {
				sourceBlock = sourceBlock.getSourceReference() != 0 ? ElementKeyMap.getInfoFast(sourceBlock.getSourceReference()) : sourceBlock;
				RadialMenuDialogShapes shapeDiag = new RadialMenuDialogShapes(getState(), sourceBlock);
				System.out.println("RADIAL creating radial menu for " + sourceBlock.getName());
				shapeDiag.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(177);
			}
		}
	}

	public void selectTypeForced(short type) {
		forcedSelect = type;
	}

	public short getForcedSelect() {
		return forcedSelect;
	}

	public List<BuildInstruction> getUndo() {
		return undo;
	}

	public boolean isStickyAdvBuildMode() {
		return stickyAdvBuildMode;
	}

	public void setStickyAdvBuildMode(boolean stickyAdvBuildMode) {
		this.stickyAdvBuildMode = stickyAdvBuildMode;
	}

	public BuildConstructionManager getBuildCommandManager() {
		return buildCommandManager;
	}

	public void promptBuild(short type, int amount, String info) {
		BuildConstructionCommand c = new BuildConstructionCommand(buildCommandManager, type, amount);
		c.setInstruction(info);
		buildCommandManager.enqueue(c);
	}

	public boolean canQueue(short type, int amount) {
		BuildConstructionCommand c = new BuildConstructionCommand(buildCommandManager, type, amount);
		return buildCommandManager.canQueue(c);
	}

	public void resetQueue() {
		buildCommandManager.resetQueue();
	}

	private static interface SurroundBlockCondition {
		public boolean ok(ElementInformation info);
	}
}
