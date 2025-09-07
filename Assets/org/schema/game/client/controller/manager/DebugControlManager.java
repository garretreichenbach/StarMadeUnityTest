package org.schema.game.client.controller.manager;

import api.common.GameClient;
import com.bulletphysics.linearmath.Transform;
import org.lwjgl.glfw.GLFW;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.client.view.*;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;
import org.schema.game.client.view.cubes.shapes.pentahedron.topbottom.PentaShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.spike.SpikeShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.tetrahedron.TetrahedronShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.wedge.WedgeShapeAlgorithm;
import org.schema.game.client.view.gui.RadialMenuDialogDebug;
import org.schema.game.client.view.gui.RadialMenuDialogMain;
import org.schema.game.client.view.gui.reactor.ReactorTreeDialog;
import org.schema.game.client.view.gui.rules.RuleSetConfigDialogGame;
import org.schema.game.client.view.mainmenu.gui.effectconfig.EffectConfigEntityDialog;
import org.schema.game.client.view.mainmenu.gui.effectconfig.GUIEffectStat;
import org.schema.game.client.view.mainmenu.gui.ruleconfig.GUIRuleSetStat;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.StationaryManagerContainer;
import org.schema.game.common.controller.rules.RuleSetManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.ConfigManagerInterface;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.PhysicsExt;
import org.schema.game.common.data.physics.Vector3fb;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.network.objects.remote.RemoteSegmentPiece;
import org.schema.game.server.controller.GameServerController;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;
import org.schema.schine.graphicsengine.forms.debug.DebugPoint;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationHighlightCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonColor;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.Keyboard;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.resource.ResourceLoadEntry;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.IOException;
import java.util.List;

public class DebugControlManager extends AbstractControlManager {
	public static float x;
	public static float y;
	public static boolean requestPhysicsCheck;
	public static SegmentController requestShipMissalign;

	public DebugControlManager(GameClientState state) {
		super(state);
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		super.handleKeyEvent(e);
		try {
			getState().getWorldDrawer().handleKeyEvent(e);
			short selectedType = getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getSelectedTypeWithSub();
			int blockOrientation = getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBlockOrientation();
			ElementInformation info = null;
			if(selectedType > 0) {
				info = ElementKeyMap.getInfo(selectedType);
			}
			switch(e.getKeyboardKeyRaw()) {
				case GLFW.GLFW_KEY_R:
					if(GameClient.getCurrentControl() != null) {
						WorldDrawer.flagEntityRender(GameClient.getCurrentControl());
//						StarLoaderTexture.runOnGraphicsThread(() -> {
//							try {
//								SegmentControllerRenderCreator renderer = new SegmentControllerRenderCreator();
//								SegmentController segmentController = GameClient.getCurrentControl();
//								assert segmentController != null;
//								BufferedImage image = renderer.bake(segmentController);
//								String imagePath = "./screenshots/" + segmentController.getUniqueIdentifier() + "_render_" + System.currentTimeMillis() + ".png";
//								File file = new File(imagePath);
//								file.createNewFile();
//								ImageIO.write(image, "png", file);
//								System.out.println("Render saved to " + imagePath);
//							} catch(Exception exception) {
//								exception.printStackTrace();
//							}
//						});
					}
					break;
				case GLFW.GLFW_KEY_RIGHT_SHIFT:
					getState().getParticleSystemManager().openGUI(getState());
					break;
				case GLFW.GLFW_KEY_W:
					getState().getWorldDrawer().toggleWireframe();
					break;
				case GLFW.GLFW_KEY_L:
					if(EngineSettings.T_ENABLE_TEXTURE_BAKER.isOn()) {
						WorldDrawer.flagTextureBake = true;
					}
					break;
				case GLFW.GLFW_KEY_G:
					EngineSettings.G_DRAW_NO_OVERLAYS.switchOn();
					break;
				case GLFW.GLFW_KEY_K:
					if(Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
						getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().debugPush();
					}
					break;
				case GLFW.GLFW_KEY_E:
					if(Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
						final RadialMenuDialogMain d = new RadialMenuDialogMain(getState());
						d.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(96);
					}
					break;
				case GLFW.GLFW_KEY_Q:
					SimpleTransformableSendableObject ent = ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getSelectedEntity();
					if(ent instanceof SegmentController && ((SegmentController) ent).railController.isRoot()) {
						DebugControlManager.requestShipMissalign = ((SegmentController) ent);
					}
					if(Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
						final RadialMenuDialogMain d = new RadialMenuDialogMain(getState());
						d.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(97);
					}
					break;
				case GLFW.GLFW_KEY_B:
					if(getState().isAdmin()) {
						final RadialMenuDialogDebug d = new RadialMenuDialogDebug(getState());
						d.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(98);
					}
					break;
				case GLFW.GLFW_KEY_INSERT:
					if(getState().isAdmin()) {
						RuleSetManager ruleSetManager = new RuleSetManager(getState().getGameState().getRuleManager());
						ruleSetManager.setState(getState());
						GUIRuleSetStat stat = new GUIRuleSetStat(getState(), ruleSetManager);
						stat.gameState = getState().getGameState();
						final RuleSetConfigDialogGame d = new RuleSetConfigDialogGame(getState(), stat);
						d.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(99);
					}
					break;
				// case GLFW.GLFW_KEY_Z:
				// Element.nextCombo(true);
				// EngineSettings.G_SHADER_RELOAD.switchSetting();
				// break;
				// case GLFW.GLFW_KEY_T:
				// Element.nextCombo(false);
				// EngineSettings.G_SHADER_RELOAD.switchSetting();
				// break;
				case GLFW.GLFW_KEY_N:
					if(getState().isAdmin()) {
						SimpleTransformableSendableObject selectedEntity = getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getSelectedEntity();
						ConfigManagerInterface c = null;
						if(selectedEntity != null && selectedEntity instanceof ConfigManagerInterface) {
							c = (ConfigManagerInterface) selectedEntity;
						} else if(getState().getCurrentRemoteSector() != null) {
							c = getState().getCurrentRemoteSector();
						}
						if(c != null) {
							final EffectConfigEntityDialog d = new EffectConfigEntityDialog(getState(), c, new GUIEffectStat(getState(), getState().getConfigPool()));
							d.activate();
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
							AudioController.fireAudioEventID(100);
						}
					}
					break;
				case GLFW.GLFW_KEY_P:
					if(getState().isAdmin()) {
						SimpleTransformableSendableObject<?> c = null;
						if(c == null) {
							SimpleTransformableSendableObject<?> selectedEntity = getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getSelectedEntity();
							if(selectedEntity != null) {
								c = selectedEntity;
							}
						}
						if(c == null) {
							c = getState().getCurrentPlayerObject();
						}
						if(c != null && c.hasAnyReactors()) {
							final ReactorTreeDialog d = new ReactorTreeDialog(getState(), (ManagedSegmentController<?>) c);
							d.activate();
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
							AudioController.fireAudioEventID(101);
						} else {
							System.err.println("NOTHING SELECTED OR STRUCTURE IS NOT USING NEW POWER SYSTEM");
						}
					}
					break;
				case GLFW.GLFW_KEY_M:
					if(Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
						GameServerController.debugLogoutOnShutdown = !GameServerController.debugLogoutOnShutdown;
						getState().getController().popupAlertTextMessage("DEBUG SHUTDOWN: " + GameServerController.debugLogoutOnShutdown, 0);
					}
					break;
				case GLFW.GLFW_KEY_J:
					ElementCollectionDrawer.flagAllDirty = true;
					break;
				case GLFW.GLFW_KEY_UP:
					if(Keyboard.isKeyDown(GLFW.GLFW_KEY_CAPS_LOCK)) {
						getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().removeLayer(0, 1, 0);
					} else {
						y++;
						System.err.println("DEBUG VAL Y: " + y);
					}
					break;
				case GLFW.GLFW_KEY_DOWN:
					if(Keyboard.isKeyDown(GLFW.GLFW_KEY_CAPS_LOCK)) {
						getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().removeLayer(0, -1, 0);
					} else {
						y--;
						System.err.println("DEBUG VAL Y: " + y);
					}
					break;
				case GLFW.GLFW_KEY_LEFT:
					if(Keyboard.isKeyDown(GLFW.GLFW_KEY_CAPS_LOCK)) {
						getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().removeLayer(0, 0, -1);
					} else {
						x--;
						System.err.println("DEBUG VAL X: " + x);
					}
					break;
				case GLFW.GLFW_KEY_RIGHT:
					if(Keyboard.isKeyDown(GLFW.GLFW_KEY_CAPS_LOCK)) {
						getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().removeLayer(0, 0, 1);
					} else {
						x++;
						System.err.println("DEBUG VAL X: " + x);
					}
					break;
				case GLFW.GLFW_KEY_X:
					EngineSettings.G_FRAMERATE_FIXED.switchOn();
					;
					System.err.println("SYNC");
					break;
				case GLFW.GLFW_KEY_F2:
					EngineSettings.G_SHADER_RELOAD.switchOn();
					System.err.println("RELOADING SHADERS");
					break;
				case GLFW.GLFW_KEY_F3:
					EngineSettings.F_FRAME_BUFFER.switchOn();
					System.err.println("fbo: " + EngineSettings.F_FRAME_BUFFER.isOn());
					break;
				case GLFW.GLFW_KEY_F4:
					EngineSettings.F_BLOOM.switchOn();
					System.err.println("bloom: " + EngineSettings.F_BLOOM.isOn());
					break;
				case GLFW.GLFW_KEY_Z:
					GameClientState.smoothDisableDebug = !GameClientState.smoothDisableDebug;
					System.err.println("disable smooth: " + GameClientState.smoothDisableDebug);
					getState().getController().popupAlertTextMessage("Smooth Debug Disabled: " + GameClientState.smoothDisableDebug);
					break;
				case GLFW.GLFW_KEY_I:
					EngineSettings.S_INFO_DRAW.switchOn();
					System.err.println("info: " + EngineSettings.S_INFO_DRAW.isOn());
					break;
				case GLFW.GLFW_KEY_V:
					SimpleTransformableSendableObject se = getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getSelectedEntity();
					if(se != null && se instanceof SegmentController) {
						SegmentController c = (SegmentController) se;
						if(!Segment2ObjWriter.isRunning()) {
							Segment2ObjWriter w = new Segment2ObjWriter(c, Segment2ObjWriter.DEFAULT_PATH, "testWrite");
							(new Thread(w, "WavefrontObjWriter")).start();
						} else {
							((GameClientState) c.getState()).getController().popupAlertTextMessage("Writing in progress", 0);
						}
					}
					break;
				case GLFW.GLFW_KEY_Y:
					BlockShapeAlgorithm[] blockShapeAlgorithms = BlockShapeAlgorithm.algorithms[1];
					for(int i = 0; i < blockShapeAlgorithms.length - 1; i++) {
						SpikeShapeAlgorithm s = (SpikeShapeAlgorithm) blockShapeAlgorithms[i];
						s.calcAngledSideVertsFromWedges();
					}
					// getState().getShip().railController.disconnect();
					break;
				case GLFW.GLFW_KEY_9:
					List<ResourceLoadEntry> resourceLoadEntry = GameResourceLoader.getBlockTextureResourceLoadEntry();
					Controller.getResLoader().enqueueWithReset(resourceLoadEntry);
					EngineSettings.G_SHADER_RELOAD.switchOn();
					System.err.println("RELOADING SHADERS");
					break;
				case GLFW.GLFW_KEY_1:
					if(!Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL)) {
						return;
					}
					if(BuildModeDrawer.currentInfo != null) {
						if(getState().getCurrentPlayerObject() != null && getState().getCurrentPlayerObject() instanceof SegmentController) {
							if(BuildModeDrawer.currentInfo.getBlockStyle() != BlockStyle.NORMAL) {
								BlockShapeAlgorithm algo = BlockShapeAlgorithm.getAlgo(BuildModeDrawer.currentInfo.getBlockStyle(), BuildModeDrawer.currentPiece.getOrientation());
								algo.modProperty(Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) ? 6 : BuildModeDrawer.currentSide, -1);
								System.err.println("SET SIDE VERTEX ORDER (previous): " + algo.getClass().getSimpleName() + ": LOOKINGKT_SIDE: " + Element.getSideString(BuildModeDrawer.currentSide));
							} else {
								System.err.println("Changing a normal block: SIDE: " + BuildModeDrawer.currentSide + "; " + BuildModeDrawer.currentInfo);
								BlockShapeAlgorithm.changeTexCoordOrder(BuildModeDrawer.currentPiece, BuildModeDrawer.currentSide, -1);
							}
							SegmentDrawer.forceFullLightingUpdate = true;
						} else {
							try {
								throw new Exception("This only works right inside build mode");
							} catch(Exception ex) {
								ex.printStackTrace();
							}
							getState().getController().popupAlertTextMessage("Only in build mode", 0);
						}
					}
					break;
				case GLFW.GLFW_KEY_2:
					if(!Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL)) {
						return;
					}
					if(BuildModeDrawer.currentInfo != null) {
						if(getState().getCurrentPlayerObject() != null && getState().getCurrentPlayerObject() instanceof SegmentController) {
							if(BuildModeDrawer.currentInfo.getBlockStyle() != BlockStyle.NORMAL) {
								if(BuildModeDrawer.currentSide >= 0) {
									BlockShapeAlgorithm algo = BlockShapeAlgorithm.getAlgo(BuildModeDrawer.currentInfo.getBlockStyle(), BuildModeDrawer.currentPiece.getOrientation());
									algo.modProperty(Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) ? 6 : BuildModeDrawer.currentSide, 1);
									System.err.println("SET SIDE VERTEX ORDER (next): " + algo.getClass().getSimpleName() + ": LOOKING_AT_SIDE:  " + Element.getSideString(BuildModeDrawer.currentSide));
								} else {
									System.err.println("INVALID CURRENT SIDE: " + BuildModeDrawer.currentSide);
								}
							} else {
								BlockShapeAlgorithm.changeTexCoordOrder(BuildModeDrawer.currentPiece, BuildModeDrawer.currentSide, 1);
								System.err.println("Changing a normal block: SIDE: " + BuildModeDrawer.currentSide + "; " + BuildModeDrawer.currentInfo);
							}
							SegmentDrawer.forceFullLightingUpdate = true;
						} else {
							try {
								throw new Exception("This only works right inside build mode");
							} catch(Exception ex) {
								ex.printStackTrace();
							}
							getState().getController().popupAlertTextMessage("Only in build mode", 0);
						}
					}
					break;
				case GLFW.GLFW_KEY_PAGE_UP:
					if(Keyboard.isKeyDown(GLFW.GLFW_KEY_CAPS_LOCK)) {
						getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().removeLayer(1, 0, 0);
					} else if(BuildModeDrawer.currentInfo != null) {
						if(BuildModeDrawer.currentInfo.getBlockStyle() != BlockStyle.NORMAL) {
							BlockShapeAlgorithm algo = BlockShapeAlgorithm.getAlgo(BuildModeDrawer.currentInfo.getBlockStyle(), BuildModeDrawer.currentPiece.getOrientation());
							algo.modAngledVertex(-(1 + (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) ? 100 : 0)));
							System.err.println("SET SIDE VERTEX ORDER (next): " + algo.getClass().getSimpleName() + ": " + Element.getSideString(BuildModeDrawer.currentSide));
						} else {
							System.err.println("NO CHANGE::::: Block style is not special: " + BuildModeDrawer.currentInfo.getBlockStyle());
						}
						SegmentDrawer.forceFullLightingUpdate = true;
					}
					break;
				case GLFW.GLFW_KEY_PAGE_DOWN:
					if(Keyboard.isKeyDown(GLFW.GLFW_KEY_CAPS_LOCK)) {
						getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().removeLayer(-1, 0, 0);
					} else if(BuildModeDrawer.currentInfo != null) {
						if(BuildModeDrawer.currentInfo.getBlockStyle() != BlockStyle.NORMAL) {
							BlockShapeAlgorithm algo = BlockShapeAlgorithm.getAlgo(BuildModeDrawer.currentInfo.getBlockStyle(), BuildModeDrawer.currentPiece.getOrientation());
							algo.modAngledVertex((1 + (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) ? 100 : 0)));
							System.err.println("SET SIDE VERTEX ORDER (next): " + algo.getClass().getSimpleName() + ": " + Element.getSideString(BuildModeDrawer.currentSide));
						} else {
							System.err.println("NO CHANGE::::: Block style is not special: " + BuildModeDrawer.currentInfo.getBlockStyle());
						}
						SegmentDrawer.forceFullLightingUpdate = true;
					}
					break;
				case GLFW.GLFW_KEY_3:
					if(!Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL)) {
						return;
					}
					if(BuildModeDrawer.currentInfo != null) {
						SegmentPiece p = new SegmentPiece(BuildModeDrawer.currentPiece);
						byte orientation = (p.getOrientation());
						orientation = (byte) (orientation - 1);
						if(orientation <= 0) {
							orientation = 31;
						}
						p.setOrientation(orientation);
						p.getSegment().getSegmentController().sendBlockMod(new RemoteSegmentPiece(p, false));
						System.err.println("SENT (shouldbeorient " + orientation + "); " + p.getOrientation() + "; " + p);
					}
					break;
				case GLFW.GLFW_KEY_END:
					if(Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL) && Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL)) {
						if(GLFrame.stateChangeRequest == null) {
							GLFrame.stateChangeRequest = new StateChangeRequest();
						}
					}
					break;
				case GLFW.GLFW_KEY_4:
					if(!Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL)) {
						return;
					}
					if(BuildModeDrawer.currentInfo != null) {
						SegmentPiece p = new SegmentPiece(BuildModeDrawer.currentPiece);
						byte orientation = (p.getOrientation());
						orientation = (byte) (orientation + 1);
						if(orientation >= 32) {
							orientation = 0;
						}
						p.setOrientation(orientation);
						p.getSegment().getSegmentController().sendBlockMod(new RemoteSegmentPiece(p, false));
						System.err.println("SENT (shouldbeorient " + orientation + "); " + p.getOrientation() + "; " + p);
						// p.setO
						//
						// boolean active = orientation;
					}
					// if(info != null && info.getBlockStyle() != BlockStyle.NORMAL){
					// BlockShapeAlgorithm.algorithms[info.getBlockStyle()-1][blockOrientation].rearrage(3, Keyboard.isKeyDown(GLFW.GLFW_KEY_CAPITAL) ? -1 : 1);
					// SegmentDrawer.forceFullLightingUpdate = true;
					// }
					// else{
					// System.err.println("BlockStyle of "+info+" not rearrangable");
					// }
					break;
				case GLFW.GLFW_KEY_5:
					System.err.println("SHATTERING");
					float size = 0.5f;
					Vector3fb bbt = new Vector3fb(getState().getCurrentPlayerObject().getWorldTransform().origin);
					Vector3f f = getState().getPlayer().getForward(new Vector3f());
					f.scale(3);
					bbt.add(f);
					Transform tt = new Transform();
					tt.setIdentity();
					tt.origin.set(bbt);
					getState().getWorldDrawer().getShards().voronoiBBShatter((PhysicsExt) getState().getPhysics(), tt, (short) 1, getState().getCurrentSectorId(), tt.origin, null);
					// Vector3fb bbmax = new Vector3fb(size, size, size);
					// Vector3fb bbmin = new Vector3fb(-size, -size, -size);
					// // Place it 10 units above ground
					// Vector3fb bbt = new Vector3fb();
					// //				Vector3fb bbt = new Vector3fb(getState().getCurrentPlayerObject().getWorldTransform().origin);
					// //				Vector3f f = new Vector3f(getState().getPlayer().getForward());
					// //				f.scale(10);
					// //				bbt.add(f);
					// // Use an arbitrary material density for shards (should be consitent/relative with/to rest of RBs in world)
					// float matDensity = 100f;
					// // Using random rotation
					// //				btQuaternion bbq(btScalar(rand() / btScalar(RAND_MAX)) * 2. -1.,btScalar(rand() / btScalar(RAND_MAX)) * 2. -1.,btScalar(rand() / btScalar(RAND_MAX)) * 2. -1.,btScalar(rand() / btScalar(RAND_MAX)) * 2. -1.);
					// //				bbq.normalize();
					// Quat4f bbq = new Quat4f(0,0,0,1);
					// // Generate random points for voronoi cells
					// ObjectArrayList<Vector3fb> points = new ObjectArrayList<Vector3fb>();
					// Vector3fb diff = new Vector3fb();
					// diff.sub(bbmax, bbmin);
					// //				for (int i=0; i < 10; i++) {
					// //					// Place points within box area (points are in world coordinates)
					// //					Vector3fb point = new Vector3fb(FastMath.rand.nextFloat()-0.5f,FastMath.rand.nextFloat()-0.5f,FastMath.rand.nextFloat()-0.5f);
					// ////					Vector3fb point =		new Vector3fb(QuaternionUtil.quatRotate(bbq, new Vector3f(
					// ////							(float)(Math.random() ) * diff.x -diff.x/2f,
					// ////							(float)(Math.random() ) * diff.y -diff.y/2f,
					// ////							(float)(Math.random() ) * diff.z -diff.z/2f), new Vector3f()));
					// ////					point.scale(3.001f);
					// //					point.add(bbt);
					// //					points.add(point);
					// //				}
					// 
					// points.add(new Vector3fb(0,0.25f,0));
					// points.add(new Vector3fb(0,-0.25f,0));
					// //				points.add(new Vector3fb(0,0,0.25f));
					// //				points.add(new Vector3fb(0,0,-0.25f));
					// //				points.add(new Vector3fb(0.25f,0,0));
					// //				points.add(new Vector3fb(-0.25f,0,0));
					// for(Vector3f p :points){
					// DebugPoint debugPoint = new DebugPoint(new Vector3f(p), new Vector4f(1,0,0,1), 0.1f);
					// debugPoint.LIFETIME = 7000;
					// DebugDrawer.points.add(debugPoint);
					// 
					// 
					// }
					// DebugBoundingBox debugBoundingBox = new DebugBoundingBox(bbmin, bbmax, 1, 1, 1, 1);
					// debugBoundingBox.LIFETIME = 7000;
					// DebugDrawer.boundingBoxes.add(debugBoundingBox);
					// VonoroiShatter.voronoiBBShatter((PhysicsExt)getState().getPhysics(), points, bbmin, bbmax, bbq, bbt, matDensity);
					break;
				case GLFW.GLFW_KEY_6:
					System.err.println("[DEBUG] REQUESTING PHYSICS CHECK");
					requestPhysicsCheck = true;
					// if(info != null && info.getBlockStyle() != BlockStyle.NORMAL){
					// BlockShapeAlgorithm.algorithms[info.getBlockStyle()-1][blockOrientation].rearrage(5, Keyboard.isKeyDown(GLFW.GLFW_KEY_CAPITAL) ? -1 : 1);
					// SegmentDrawer.forceFullLightingUpdate = true;
					// }
					// else{
					// System.err.println("BlockStyle of "+info+" not rearrangable");
					// }
					break;
				case GLFW.GLFW_KEY_7:
					AbstractScene.setZoomFactorForRender(!getState().getWorldDrawer().getGameMapDrawer().isMapActive(), Math.min(1f, AbstractScene.getZoomFactorUnchecked() + .1f));
					System.err.println("ZOOM: " + AbstractScene.getZoomFactorForRender(!getState().getWorldDrawer().getGameMapDrawer().isMapActive()));
					break;
				case GLFW.GLFW_KEY_8:
					AbstractScene.setZoomFactorForRender(!getState().getWorldDrawer().getGameMapDrawer().isMapActive(), Math.max(.1f, AbstractScene.getZoomFactorUnchecked() - .1f));
					System.err.println("ZOOM: " + AbstractScene.getZoomFactorForRender(!getState().getWorldDrawer().getGameMapDrawer().isMapActive()));
					break;
				case GLFW.GLFW_KEY_PERIOD:
					getState().getController().parseBlockBehavior(GameServerController.BLOCK_BEHAVIOR_DEFAULT_PATH);
					getState().getController().reapplyBlockConfigInstantly();
					getState().getController().getClientChannel().uploadClientBlockBehavior(GameServerController.BLOCK_BEHAVIOR_DEFAULT_PATH);
					break;
				case GLFW.GLFW_KEY_KP_1:
					BlockShapeAlgorithm.rearrageStat(0, Keyboard.isKeyDown(GLFW.GLFW_KEY_CAPS_LOCK));
					SegmentDrawer.forceFullLightingUpdate = true;
					break;
				case GLFW.GLFW_KEY_KP_2:
					BlockShapeAlgorithm.rearrageStat(1, Keyboard.isKeyDown(GLFW.GLFW_KEY_CAPS_LOCK));
					SegmentDrawer.forceFullLightingUpdate = true;
					break;
				case GLFW.GLFW_KEY_KP_3:
					BlockShapeAlgorithm.rearrageStat(2, Keyboard.isKeyDown(GLFW.GLFW_KEY_CAPS_LOCK));
					SegmentDrawer.forceFullLightingUpdate = true;
					break;
				case GLFW.GLFW_KEY_KP_4:
					BlockShapeAlgorithm.rearrageStat(3, Keyboard.isKeyDown(GLFW.GLFW_KEY_CAPS_LOCK));
					SegmentDrawer.forceFullLightingUpdate = true;
					break;
				case GLFW.GLFW_KEY_KP_5:
					BlockShapeAlgorithm.rearrageStat(4, Keyboard.isKeyDown(GLFW.GLFW_KEY_CAPS_LOCK));
					SegmentDrawer.forceFullLightingUpdate = true;
					break;
				case GLFW.GLFW_KEY_KP_6:
					BlockShapeAlgorithm.rearrageStat(5, Keyboard.isKeyDown(GLFW.GLFW_KEY_CAPS_LOCK));
					SegmentDrawer.forceFullLightingUpdate = true;
					break;
				case GLFW.GLFW_KEY_0:
					if(info != null && info.getBlockStyle() != BlockStyle.NORMAL) {
						BlockShapeAlgorithm.algorithms[info.getBlockStyle().id - 1][blockOrientation].output();
						;
					}
					break;
				case GLFW.GLFW_KEY_F5:
					BlockShapeAlgorithm.readTexOrder();
					SegmentDrawer.forceFullLightingUpdate = true;
					System.err.println("FORCE FULL LIGHTING UPDATE: " + SegmentDrawer.forceFullLightingUpdate);
					break;
				case GLFW.GLFW_KEY_F6:
					BlockShapeAlgorithm.readTexOrder();
					if(Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
						SegmentDrawer.reinitializeMeshes = true;
					}
					if(getState().getPlayer().getNetworkObject().isAdminClient.get() || getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getPlayerCharacterManager().isTreeActive()) {
						getState().getController().clearAllSegmentBuffers();
						if(getState().getPlayer() != null) {
							getState().getPlayer().hasSpawnWait = true;
							if(getState().getPlayer().getAssingedPlayerCharacter() != null) {
								// clear old list so we aren't waiting for segments that will never come
								getState().getPlayer().getAssingedPlayerCharacter().waitingForToSpawn.clear();
							}
						}
					} else {
						getState().getController().popupAlertTextMessage(Lng.str("Only allowed in\nastronaut mode\nor as admin!"), 0);
					}
					break;
				case GLFW.GLFW_KEY_HOME:
					WorldDrawer.flagCreatureTool = true;
					CameraMouseState.ungrabForced = !CameraMouseState.ungrabForced;
					break;
				case GLFW.GLFW_KEY_F7:
					getState().setDbPurgeRequested(true);
					// if (SegmentDrawer.distanceMode == SegmentDrawer.DISTANCE_VIEWER) {
					// SegmentDrawer.distanceMode = SegmentDrawer.DISTANCE_CAMERA;
					// System.err.println("Distance mode: CAMERA");
					// } else {
					// SegmentDrawer.distanceMode = SegmentDrawer.DISTANCE_VIEWER;
					// System.err.println("Distance mode: VIEWER");
					// }
					break;
				case GLFW.GLFW_KEY_F12:
					System.err.println("REPARSING GUI CONFIG");
					Controller.getResLoader().enqueueConfigResources("GuiConfig.xml", false);
					Controller.getResLoader().setLoaded(false);
					if(getState().getPlayerInputs().isEmpty()) {
						PlayerOkCancelInput p = (new PlayerOkCancelInput("TTEEDEBUG", getState(), 700, 500, "TEST (F1+F12 to Refresh Config Load)", "") {
							@Override
							public void onDeactivate() {
							}

							@Override
							public void pressedOK() {
								deactivate();
							}
						});
						p.getInputPanel().onInit();
						GUIContentPane m = ((GUIDialogWindow) p.getInputPanel().getBackground()).getMainContentPane();
						m.setTextBoxHeightLast(UIScale.getUIScale().scale(30));
						GUIHorizontalButtonTablePane t = new GUIHorizontalButtonTablePane(getState(), 1, HButtonColor.values().length, m.getContent(0));
						t.onInit();
						for(int i = 0; i < HButtonColor.values().length; i++) {
							Cbutton b = new Cbutton(i);
							GUIHorizontalButton button = b.getButton(getState());
							t.addButton(button, 0, i);
						}
						m.getContent(0).attach(t);
						p.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(102);
					}
					break;
				case GLFW.GLFW_KEY_F8:
					if(getState().getPlayer().getNetworkObject().isAdminClient.get()) {
						PlayerInteractionControlManager controlManager = getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
						if(controlManager.getSegmentControlManager().getSegmentController() != null && ((ManagedSegmentController<?>) controlManager.getSegmentControlManager().getSegmentController()).getManagerContainer() instanceof StationaryManagerContainer) {
							getState().getController().popupAlertTextMessage(Lng.str("Exit structure before using this!"), 0);
							break;
						}
						SimpleTransformableSendableObject selectedEntity = getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getSelectedEntity();
						if(selectedEntity != null && selectedEntity instanceof Ship) {
							SegmentPiece nearestPiece = ((Ship) selectedEntity).getSegmentBuffer().getPointUnsave(Ship.core);
							if(nearestPiece != null) {
								if(nearestPiece.getSegment().getSegmentController() instanceof Ship) {
									controlManager.getInShipControlManager().setEntered(nearestPiece);
									System.err.println("[CLIENT] Debug enter used; Entered (ship) " + nearestPiece + "; " + nearestPiece.getSegment().getSegmentController());
									getState().getController().requestControlChange(getState().getCharacter(), (PlayerControllable) nearestPiece.getSegment().getSegmentController(), new Vector3i(), nearestPiece.getAbsolutePos(new Vector3i()), true);
								}
								break;
							}
						}
						for(Sendable s : getState().getCurrentSectorEntities().values()) {
							if(s instanceof Ship && ((Ship) s).getSectorId() == getState().getCurrentSectorId()) {
								SegmentPiece nearestPiece = ((Ship) s).getSegmentBuffer().getPointUnsave(Ship.core);
								if(nearestPiece != null) {
									controlManager.getSegmentControlManager().exit();
									controlManager.getInShipControlManager().setEntered(nearestPiece);
									getState().getController().requestControlChange(getState().getCharacter(), (PlayerControllable) nearestPiece.getSegment().getSegmentController(), new Vector3i(), nearestPiece.getAbsolutePos(new Vector3i()), true);
									break;
								}
							}
							;
						}
					} else {
						getState().getController().popupAlertTextMessage(Lng.str("Only allowed for admins!"), 0);
					}
					break;
				// case GLFW.GLFW_KEY_F9:
				// 
				// CubeOptOptMesh.showOccludedBoundingBoxes =
				// !CubeOptOptMesh.showOccludedBoundingBoxes;
				// System.err.println("Occluded bounding boxes: "+CubeOptOptMesh.showOccludedBoundingBoxes);
				// break;
				case GLFW.GLFW_KEY_F10:
					if(getState().getPlayer().getNetworkObject().isAdminClient.get()) {
						EngineSettings.P_PHYSICS_DEBUG_ACTIVE.switchOn();
						System.err.println("physics: " + EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn());
						getState().getController().popupAlertTextMessage(Lng.str("(Admin-Only) Debug Mode: %s", EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()), 0);
					} else {
						getState().getController().popupAlertTextMessage(Lng.str("Only allowed for admins!"), 0);
					}
					break;
				// case GLFW.GLFW_KEY_F12:
				// 
				// System.err.println("REQUESTING TEST");
				// getState().getController().getClientChannel().getClientMapRequestManager().requestSystem(new Vector3i(0, 0, 0));
				// break;
				case GLFW.GLFW_KEY_F11:
					System.err.println("[CLIENT] Tab+F11 Pressed: lwjgl event key: " + e.getKeyboardKeyRaw());
					getState().flagRequestServerTime();
					break;
				case GLFW.GLFW_KEY_BACKSPACE:
					System.err.println("[CLIENT] RE-READING Engine Settings");
					EngineSettings.read();
					break;
				case GLFW.GLFW_KEY_F9:
					EngineSettings.T_ENABLE_TEXTURE_BAKER.switchOn();
					break;
				// case GLFW.GLFW_KEY_O:
				// WorldDrawer.flagRecipeTrees = true;
				// break;
				case GLFW.GLFW_KEY_U:
					// getState().getController().get
					System.err.println("MACHINES: " + getState().getController().getTutorialMode().getMachineNames());
					openTutorialPanel();
					break;
				case GLFW.GLFW_KEY_ENTER:
					if(EngineSettings.T_ENABLE_TEXTURE_BAKER.isOn()) {
						WorldDrawer.flagTextureBake = true;
					}
					break;
				case GLFW.GLFW_KEY_C:
					break;
			}
		} catch(IOException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void update(Timer timer) {
		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
			if(BuildModeDrawer.currentInfo != null && BuildModeDrawer.currentInfo.getBlockStyle() == BlockStyle.NORMAL24) {
				Vector3f blockPosLocal = BuildModeDrawer.currentPiece.getAbsolutePos(new Vector3f());
				blockPosLocal.x -= SegmentData.SEG_HALF;
				blockPosLocal.y -= SegmentData.SEG_HALF;
				blockPosLocal.z -= SegmentData.SEG_HALF;
				Oriencube algo = (Oriencube) BlockShapeAlgorithm.getAlgo(BuildModeDrawer.currentInfo.getBlockStyle(), BuildModeDrawer.currentPiece.getOrientation());
				Transform primaryTransform = algo.getPrimaryTransform(blockPosLocal, 1, new Transform());
				Transform secondaryTransform = algo.getSecondaryTransform(new Transform());
				Transform res = new Transform();
				res.set(BuildModeDrawer.currentPiece.getSegmentController().getClientTransform());
				res.mul(primaryTransform);
				res.mul(secondaryTransform);
				DebugDrawer.addArrowFromTransform(res);
			} else if(BuildModeDrawer.currentInfo != null && BuildModeDrawer.currentInfo.getBlockStyle() != BlockStyle.NORMAL) {
				BlockShapeAlgorithm algo = BlockShapeAlgorithm.getAlgo(BuildModeDrawer.currentInfo.getBlockStyle(), BuildModeDrawer.currentPiece.getOrientation());
				if(algo != null && algo instanceof WedgeShapeAlgorithm) {
					Vector3i[] angledSideVerts = ((WedgeShapeAlgorithm) algo).getAngledSideVerts();
					Vector3f blockPosLocal = BuildModeDrawer.currentPiece.getAbsolutePos(new Vector3f());
					blockPosLocal.x -= SegmentData.SEG_HALF;
					blockPosLocal.y -= SegmentData.SEG_HALF;
					blockPosLocal.z -= SegmentData.SEG_HALF;
					Transform res = new Transform();
					res.set(BuildModeDrawer.currentPiece.getSegmentController().getClientTransform());
					for(int i = 0; i < angledSideVerts.length; i++) {
						Vector3f pn = new Vector3f(blockPosLocal);
						pn.x += angledSideVerts[i].x * 0.5f;
						pn.y += angledSideVerts[i].y * 0.5f;
						pn.z += angledSideVerts[i].z * 0.5f;
						res.transform(pn);
						DebugPoint p = new DebugPoint(pn, new Vector4f(1, 0, 0, 1), 0.15f);
						DebugDrawer.points.addElement(p);
					}
				}
				if(algo != null && algo instanceof TetrahedronShapeAlgorithm) {
					Vector3i[] angledSideVerts = ((TetrahedronShapeAlgorithm) algo).getAngledSideVerts();
					Vector3f blockPosLocal = BuildModeDrawer.currentPiece.getAbsolutePos(new Vector3f());
					blockPosLocal.x -= SegmentData.SEG_HALF;
					blockPosLocal.y -= SegmentData.SEG_HALF;
					blockPosLocal.z -= SegmentData.SEG_HALF;
					Transform res = new Transform();
					res.set(BuildModeDrawer.currentPiece.getSegmentController().getClientTransform());
					for(int i = 0; i < angledSideVerts.length; i++) {
						Vector3f pn = new Vector3f(blockPosLocal);
						pn.x += angledSideVerts[i].x * 0.5f;
						pn.y += angledSideVerts[i].y * 0.5f;
						pn.z += angledSideVerts[i].z * 0.5f;
						res.transform(pn);
						DebugPoint p = new DebugPoint(pn, new Vector4f(1, 0, 0, 1), 0.15f);
						DebugDrawer.points.addElement(p);
					}
				}
				if(algo != null && algo instanceof PentaShapeAlgorithm) {
					Vector3i[] angledSideVerts = ((PentaShapeAlgorithm) algo).getAngledSideVerts();
					Vector3f blockPosLocal = BuildModeDrawer.currentPiece.getAbsolutePos(new Vector3f());
					blockPosLocal.x -= SegmentData.SEG_HALF;
					blockPosLocal.y -= SegmentData.SEG_HALF;
					blockPosLocal.z -= SegmentData.SEG_HALF;
					Transform res = new Transform();
					res.set(BuildModeDrawer.currentPiece.getSegmentController().getClientTransform());
					for(int i = 0; i < angledSideVerts.length; i++) {
						Vector3f pn = new Vector3f(blockPosLocal);
						pn.x += angledSideVerts[i].x * 0.5f;
						pn.y += angledSideVerts[i].y * 0.5f;
						pn.z += angledSideVerts[i].z * 0.5f;
						res.transform(pn);
						DebugPoint p = new DebugPoint(pn, new Vector4f(1, 0, 0, 1), 0.15f);
						DebugDrawer.points.addElement(p);
					}
				}
				if(algo != null && algo instanceof SpikeShapeAlgorithm) {
					Vector3i[][] angledSideVerts = ((SpikeShapeAlgorithm) algo).getAngledSideVerts();
					Vector3f blockPosLocal = BuildModeDrawer.currentPiece.getAbsolutePos(new Vector3f());
					blockPosLocal.x -= SegmentData.SEG_HALF;
					blockPosLocal.y -= SegmentData.SEG_HALF;
					blockPosLocal.z -= SegmentData.SEG_HALF;
					Transform res = new Transform();
					res.set(BuildModeDrawer.currentPiece.getSegmentController().getClientTransform());
					for(int c = 0; c < angledSideVerts.length; c++) {
						for(int i = 0; i < angledSideVerts[c].length; i++) {
							Vector3f pn = new Vector3f(blockPosLocal);
							pn.x += angledSideVerts[c][i].x * 0.5f;
							pn.y += angledSideVerts[c][i].y * 0.5f;
							pn.z += angledSideVerts[c][i].z * 0.5f;
							res.transform(pn);
							if(c == 0) {
								DebugPoint p = new DebugPoint(pn, new Vector4f(1, 0, 0, 1), 0.15f);
								DebugDrawer.points.addElement(p);
							} else {
								DebugPoint p = new DebugPoint(pn, new Vector4f(0, 1, 0, 1), 0.15f);
								DebugDrawer.points.addElement(p);
							}
						}
					}
				}
			}
		}
	}

	private static class Cbutton {
		public int m;
		private HButtonColor c;

		public Cbutton(int i) {
			this.c = HButtonColor.values()[i];
		}

		public GUIHorizontalButton getButton(InputState state) {
			return new GUIHorizontalButton(state, c, c.name(), new GUICallback() {
				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) {
						m = (m + 1) % 3;
					}
				}

				@Override
				public boolean isOccluded() {
					return false;
				}
			}, () -> true, new GUIActivationHighlightCallback() {
				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return m != 2;
				}

				@Override
				public boolean isHighlighted(InputState state) {
					return m == 1;
				}
			});
		}
	}
}
