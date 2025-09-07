package org.schema.game.client.view.gui.buildtools;

import api.common.GameClient;
import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.schema.game.client.controller.*;
import org.schema.game.client.controller.manager.ingame.*;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.buildhelper.BuildHelper;
import org.schema.game.client.view.buildhelper.BuildHelperClass;
import org.schema.game.client.view.buildhelper.BuildHelperFactory;
import org.schema.game.client.view.buildhelper.BuildHelperVar;
import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.client.view.gui.GUISizeSettingSelectorScroll;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.CubeRayCastResult;
import org.schema.game.common.data.physics.PhysicsExt;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructureBlueprint;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.forms.Transformable;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.settingsnew.GUIScrollSettingSelector;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.resource.FileExt;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

public class BuildToolsPanel extends GUIElement implements TooltipProvider {

	public static int HEIGHT = 436 + 128 + 32 + 40 + 38 + 60;

	public static int HEIGHT_UNEXP = 390;
	public static byte blueprintPlacementSetting = TerrainStructureBlueprint.PLACEMENT_FORCED;
	private GUIAnchor background;
	private GUIAnchor buildMode;
	private GUIAnchor nothing;
	private GUIScrollablePanel scrollPanel;
	private GUITextOverlay altText;
	private GUITextOverlay inModeText;
	private boolean firstDraw = true;
	private GUIElementList generalList;
	private boolean astronaut;
	private short lastReplaceTypeSelected;
	private Int2ObjectOpenHashMap<Object2ObjectOpenHashMap<Class<? extends BuildHelper>, BuildHelper>> mm = new Int2ObjectOpenHashMap<Object2ObjectOpenHashMap<Class<? extends BuildHelper>, BuildHelper>>();

	public BuildToolsPanel(InputState state, boolean astronaut) {
		super(state);
		background = new GUIAnchor(getState(), 300, HEIGHT);
		this.astronaut = astronaut;
	}

	private BuildHelper getBuildHelperCached(Class<? extends BuildHelper> buildHelperClass, SimpleTransformableSendableObject obj) {
		Object2ObjectOpenHashMap<Class<? extends BuildHelper>, BuildHelper> map = mm.get(obj.getId());
		BuildHelper buildHelper = null;
		if(map != null) {
			buildHelper = map.get(buildHelperClass);
		}
		if(buildHelper == null) {
			Constructor<? extends BuildHelper> constructor;
			try {
				constructor = buildHelperClass.getConstructor(Transformable.class);
				buildHelper = constructor.newInstance(obj);
				if(map == null) {
					map = new Object2ObjectOpenHashMap();
					mm.put(obj.getId(), map);
				}
				map.put(buildHelperClass, buildHelper);
			} catch(NoSuchMethodException e) {
				e.printStackTrace();
			} catch(SecurityException e) {
				e.printStackTrace();
			} catch(InstantiationException e) {
				e.printStackTrace();
			} catch(IllegalAccessException e) {
				e.printStackTrace();
			} catch(IllegalArgumentException e) {
				e.printStackTrace();
			} catch(InvocationTargetException e) {
				e.printStackTrace();
			}
		} else {
			buildHelper.reset();
		}
		return buildHelper;
	}

	@Override
	public void attach(GUIElement o) {
		background.attach(o);
	}

	@Override
	public void detach(GUIElement o) {
		background.detach(o);
	}

	@Override
	public float getHeight() {
		return background.getHeight();
	}

	@Override
	public float getWidth() {
		return background.getWidth();
	}

	@Override
	public boolean isInside() {
		return background.isInside();
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	@Override
	public void cleanUp() {
		background.cleanUp();
	}

	@Override
	public void draw() {
		if(firstDraw) {
			onInit();
		}
		// if(needsReOrientation()){
		// doOrientation();
		// }
		String s = "press " + KeyboardMappings.BUILD_MODE_FIX_CAM.getKeyChar() + "\nto enter advanced\nbuild mode\n\n";
		if(!s.equals(altText.getText().get(0))) {
			altText.getText().set(0, s);
		}
		GlUtil.glPushMatrix();
		transform();
		background.draw();
		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		altText = new GUITextOverlay(FontLibrary.FontSize.SMALL_14, getState());
		altText.setText(new ArrayList());
		altText.getText().add(Lng.str("press %s\nto enter advanced\nbuild mode\n\n", KeyboardMappings.BUILD_MODE_FIX_CAM.getKeyChar()));
		altText.getText().add(Lng.str("press %s\nto reset the camera\n\n", KeyboardMappings.JUMP_TO_MODULE.getKeyChar()));
		altText.getText().add(Lng.str("hold %s\nto move faster\n\n", KeyboardMappings.BUILD_MODE_FAST_MOVEMENT.getKeyChar()));
		altText.getText().add(Lng.str("press %s\nfor flight mode\n\n", KeyboardMappings.CHANGE_SHIP_MODE.getKeyChar()));
		altText.getText().add(Lng.str("Shift + MouseWheel to zoom\n\n"));
		buildMode = new GUIAnchor(getState());
		inModeText = new GUITextOverlay(FontLibrary.FontSize.MEDIUM_18, getState());
		inModeText.setText(new ArrayList());
		inModeText.getText().add("");
		inModeText.getPos().y = 100;
		scrollPanel = new GUIScrollablePanel(background.getWidth(), background.getHeight(), getState());
		scrollPanel.getPos().set(0, 0, 0);
		generalList = new GUIElementList(getState());
		generalList.setCallback(getBuildToolsManager());
		buildMode.attach(generalList);
		buildMode.attach(inModeText);
		nothing = new GUIAnchor(getState(), 1, 1);
		scrollPanel.setContent(altText);
		generalList.attach(inModeText);
		background.onInit();
		if(astronaut) {
			reconstructListAstronaut();
		} else {
			reconstructListFull();
		}
		super.attach(background);
		background.attach(scrollPanel);
		doOrientation();
		background.getPos().set(GLFrame.getWidth() - 270, 64, 0);
		background.setMouseUpdateEnabled(true);
		firstDraw = false;
	}

	public BuildToolsManager getBuildToolsManager() {
		return ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager();
	}

	public PlayerInteractionControlManager getPlayerInteractionControlManager() {
		return ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
	}

	public SymmetryPlanes getSymmetryPlanes() {
		PlayerInteractionControlManager pp = ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
		if(pp.getInShipControlManager().getShipControlManager().getSegmentBuildController().isTreeActive()) {
			return pp.getInShipControlManager().getShipControlManager().getSegmentBuildController().getSymmetryPlanes();
		} else {
			return pp.getSegmentControlManager().getSegmentBuildController().getSymmetryPlanes();
		}
	}

	public void reconstructListAstronaut() {
		generalList.clear();
		GUISettingsListElement guiSettingsPanel = new GUISettingsListElement(getState(), 90, 70, "Orientation", new GUIOrientationSettingElement(getState()), false, false);
		generalList.add(guiSettingsPanel);
		generalList.getPos().y += 400;
	}

	public void reconstructListFull() {
		generalList.clear();
		GUIAnchor createDockPanel = new GUIAnchor(getState(), 240, 30);
		GUIAnchor undoRedoPanel = new GUIAnchor(getState(), 240, 30);
		GUIAnchor copyPastePanel = new GUIAnchor(getState(), 240, 90);
		GUIAnchor removeFilterPanel = new GUIAnchor(getState(), 240, 30);
		GUITextButton copy = new GUITextButton(getState(), 100, 20, new Object() {

			@Override
			public String toString() {
				if(getBuildToolsManager().isCopyMode()) {
					return Lng.str("*PICK AREA*");
				} else {
					return Lng.str("Copy");
				}
			}
		}, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					if(getBuildToolsManager().isCopyMode()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.DESELECT)*/
						AudioController.fireAudioEventID(328);
					} else {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
						AudioController.fireAudioEventID(327);
					}
					getBuildToolsManager().setCopyMode(!getBuildToolsManager().isCopyMode());
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		}) {

			Vector4f c = new Vector4f(0.7f, 0, 0, 1);

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
			 */
			@Override
			public void draw() {
				super.draw();
			}

			@Override
			public Vector4f getBackgroundColorMouseOverlay() {
				if(getBuildToolsManager().isCopyMode()) {
					return c;
				} else {
					return super.getBackgroundColorMouseOverlay();
				}
			}

			@Override
			public Vector4f getBackgroundColor() {
				if(getBuildToolsManager().isCopyMode()) {
					return c;
				} else {
					return super.getBackgroundColor();
				}
			}
		};
		GUITextButton copySelectionMode = new GUITextButton(getState(), 100, 20, new Object() {

			@Override
			public String toString() {
				return Lng.str("Copy Selection");
			}
		}, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(329);
					getBuildToolsManager().setSelectMode(getBuildToolsManager().isSelectMode() ? null : new BuildSelectionCopy());
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		}) {

			Vector4f c = new Vector4f(0.7f, 0, 0, 1);

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
			 */
			@Override
			public void draw() {
				super.draw();
			}

			@Override
			public Vector4f getBackgroundColorMouseOverlay() {
				if(getBuildToolsManager().isSelectMode()) {
					return c;
				} else {
					return super.getBackgroundColorMouseOverlay();
				}
			}

			@Override
			public Vector4f getBackgroundColor() {
				if(getBuildToolsManager().isSelectMode()) {
					return c;
				} else {
					return super.getBackgroundColor();
				}
			}
		};
		GUITextButton paste = new GUITextButton(getState(), 140, 20, new Object() {

			@Override
			public String toString() {
				if(getBuildToolsManager().isPasteMode()) {
					return Lng.str("*PASTE MODE*");
				} else {
					return Lng.str("Paste");
				}
			}
		}, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(330);
					getBuildToolsManager().setPasteMode(!getBuildToolsManager().isPasteMode());
				}
			}
		}) {

			Vector4f c = new Vector4f(0.7f, 0, 0, 1);

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
			 */
			@Override
			public void draw() {
				if(getBuildToolsManager().canPaste()) {
					super.draw();
				}
			}

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#getSelectedBackgroundColor()
			 */
			@Override
			public Vector4f getBackgroundColorMouseOverlay() {
				if(getBuildToolsManager().isPasteMode()) {
					return c;
				} else {
					return super.getBackgroundColorMouseOverlay();
				}
			}

			@Override
			public Vector4f getBackgroundColor() {
				if(getBuildToolsManager().isPasteMode()) {
					return c;
				} else {
					return super.getBackgroundColor();
				}
			}
		};
		GUITextButton load = new GUITextButton(getState(), 100, 20, Lng.str("Load"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					ArrayList<GUIElement> v = new ArrayList<GUIElement>();
					File path = new FileExt(CopyArea.path);
					if(path.exists()) {
						File[] listFiles = path.listFiles();
						Arrays.sort(listFiles, 0, listFiles.length, (o1, o2) -> o1.getName().compareTo(o2.getName()));
						for(int i = 0; i < listFiles.length; i++) {
							File f = listFiles[i];
							if(!f.isDirectory() && f.getName().endsWith(".smtpl")) {
								GUITextOverlay t = new GUITextOverlay(getState());
								t.setTextSimple(f.getName().substring(0, f.getName().lastIndexOf(".smtpl")));
								t.setUserPointer(f);
								v.add(t);
							}
						}
						PlayerGameDropDownInput p = new PlayerGameDropDownInput("BuildToolsPanel_SELECT_TMP", (GameClientState) getState(), Lng.str("Select Template"), 16, Lng.str("Choose a template and confirm"), v) {

							@Override
							public boolean isOccluded() {
								return false;
							}

							@Override
							public void onDeactivate() {
							}

							@Override
							public void pressedOK(GUIListElement current) {
								File f = (File) current.getContent().getUserPointer();
								try {
									getBuildToolsManager().loadCopyArea(f);
									getState().getController().popupInfoTextMessage(Lng.str("Template loaded\n%s", f.getName()), 0);
									deactivate();
									/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
									AudioController.fireAudioEventID(332);
								} catch(IOException e) {
									e.printStackTrace();
									/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.ERROR)*/
									AudioController.fireAudioEventID(331);
									getState().getController().popupAlertTextMessage(Lng.str("Failed to load\n%s", f.getName()), 0);
								}
							}
						};
						p.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(333);
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
						AudioController.fireAudioEventID(334);
					} else {
						((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("No Templates found!"), 0);
					}
				}
			}
		}) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
			 */
			@Override
			public void draw() {
				super.draw();
			}
		};
		GUITextButton save = new GUITextButton(getState(), 140, 20, new Object() {

			@Override
			public String toString() {
				if(getBuildToolsManager().canPaste()) {
					return Lng.str("Save");
				} else {
					return "-";
				}
			}
		}, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					PlayerGameTextInput pp = new PlayerGameTextInput("BuildToolsPanel_SAVE_TMP", (GameClientState) getState(), 50, Lng.str("Save Template"), "") {

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
							setErrorMessage(Lng.str("NAME INVALID: ", msg));
						}

						@Override
						public boolean isOccluded() {
							return false;
						}

						@Override
						public void onDeactivate() {
						}

						@Override
						public boolean onInput(String entry) {
							if(!entry.isEmpty()) {
								File templateFile = new FileExt(CopyArea.path + entry.trim() + ".smtpl");
								if(templateFile.exists()) {
									(new PlayerOkCancelInput("OverwriteTemplateDialog", getState(), "Overwrite Template", "A template with the name \"" + entry + "\" already exists in your local database.\nWould you like to overwrite it?") {
										@Override
										public void onDeactivate() {
										}

										@Override
										public void pressedOK() {
											try {
												getBuildToolsManager().saveCopyArea(entry.trim());
												GameClient.getClientState().getController().popupInfoTextMessage(Lng.str("Template saved."), 0);
												deactivate();
											} catch(IOException exception) {
												exception.printStackTrace();
											}
										}
									}).activate();
								} else {
									try {
										getBuildToolsManager().saveCopyArea(entry.trim());
										getState().getController().popupInfoTextMessage(Lng.str("Template saved."), 0);
										return true;
									} catch(IOException e) {
										e.printStackTrace();
										getState().getController().popupAlertTextMessage(Lng.str("Failed to save template!"), 0);
									}
								}
							} else {
								getState().getController().popupAlertTextMessage(Lng.str("Name is too short!"), 0);
							}
							return false;
						}
					};
					pp.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(338);
				}
			}
		}) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
			 */
			@Override
			public void draw() {
				if(getBuildToolsManager().canPaste()) {
					super.draw();
				}
			}
		};
		final int normalWidthRemoveFilter = 130;
		GUITextButton removeFilter = new GUITextButton(getState(), normalWidthRemoveFilter, 20, new Object() {

			@Override
			public String toString() {
				if(getBuildToolsManager().getRemoveFilter() != 0) {
					return Lng.str("Clear %s", ElementKeyMap.getInfo(getBuildToolsManager().getRemoveFilter()).getName());
				} else {
					return Lng.str("Remove/Replace");
				}
			}
		}, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					if(getBuildToolsManager().getRemoveFilter() == 0) {
						GUICheckBox cb = new GUICheckBox(getState()) {

							@Override
							protected void activate() throws StateParameterNotFoundException {
								getBuildToolsManager().setReplaceRemoveFilter(true);
							}

							@Override
							protected boolean isActivated() {
								return getBuildToolsManager().isReplaceRemoveFilter();
							}

							@Override
							protected void deactivate() throws StateParameterNotFoundException {
								getBuildToolsManager().setReplaceRemoveFilter(false);
							}

							@Override
							public void draw() {
								super.draw();
							}
						};
						PlayerBlockTypeDropdownInput pp = new PlayerBlockTypeDropdownInput("BuildToolsPanel_TYPE", (GameClientState) getState(), Lng.str("Pick a type")) {

							@Override
							public void onOk(ElementInformation info) {
								lastReplaceTypeSelected = info.getId();
								getBuildToolsManager().setRemoveFilter(info.getId());
								/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
								AudioController.fireAudioEventID(340);
							}
						};
						PlayerBlockTypeDropdownInput p = pp;
						pp.getInputPanel().onInit();
						if(ElementKeyMap.isValidType(lastReplaceTypeSelected)) {
							pp.setSelectedUserPointer(ElementKeyMap.getInfoFast(lastReplaceTypeSelected));
						}
						GUITextOverlay desc = new GUITextOverlay(getState());
						desc.setTextSimple(Lng.str("replace with active slot"));
						cb.setPos(10, 30, 0);
						desc.setPos(30, 30, 0);
						p.getInputPanel().getContent().attach(cb);
						p.getInputPanel().getContent().attach(desc);
						p.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(341);
					} else {
						getBuildToolsManager().setRemoveFilter((short) 0);
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.DESELECT)*/
						AudioController.fireAudioEventID(339);
					}
				}
			}
		}) {

			Vector4f c = new Vector4f(0.7f, 0, 0, 1);

			@Override
			public Vector4f getBackgroundColorMouseOverlay() {
				if(getBuildToolsManager().getRemoveFilter() != 0) {
					return c;
				} else {
					return super.getBackgroundColorMouseOverlay();
				}
			}

			@Override
			public Vector4f getBackgroundColor() {
				if(getBuildToolsManager().getRemoveFilter() != 0) {
					return c;
				} else {
					return super.getBackgroundColor();
				}
			}

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
			 */
			@Override
			public void draw() {
				if(getBuildToolsManager().getRemoveFilter() != 0) {
					setWidth(230);
				} else {
					setWidth(normalWidthRemoveFilter);
				}
				super.draw();
			}
		};
		GUITextButton removeFilterPickLookingAt = new GUITextButton(getState(), 100, 20, new Object() {

			@Override
			public String toString() {
				return Lng.str("Pick aimed at");
			}
		}, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					if(getBuildToolsManager().getRemoveFilter() == 0) {
						Vector3f camPos = new Vector3f(Controller.getCamera().getPos());
						Vector3f camTo = new Vector3f(camPos);
						Vector3f forw = new Vector3f(Controller.getCamera().getForward());
						if(Float.isNaN(forw.x)) {
							return;
						}
						forw.scale(SegmentBuildController.EDIT_DISTANCE);
						camTo.add(forw);
						ClosestRayResultCallback c = ((PhysicsExt) ((GameClientState) getState()).getPhysics()).testRayCollisionPoint(camPos, camTo, false, ((GameClientState) getState()).getCharacter(), null, true, true, false);
						if(c != null && c.hasHit() && c instanceof CubeRayCastResult) {
							CubeRayCastResult cc = (CubeRayCastResult) c;
							SegmentPiece p = new SegmentPiece(cc.getSegment(), cc.getCubePos());
							if(ElementKeyMap.isValidType(p.getType())) {
								ElementInformation i = ElementKeyMap.getInfo(p.getType());
								getBuildToolsManager().setRemoveFilter(p.getType());
								System.err.println("[BUILDTOOLS] set type by pick: " + p.getType());
							}
						}
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.SELECT)*/
						AudioController.fireAudioEventID(342);
					}
				}
			}
		}) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
			 */
			@Override
			public void draw() {
				if(getBuildToolsManager().getRemoveFilter() == 0) {
					super.draw();
				}
			}
		};
		GUITextButton buildHelperRestrict = new GUITextButton(getState(), 100, 20, new Object() {

			@Override
			public String toString() {
				if(getBuildToolsManager().buildHelperReplace) {
					return Lng.str("restricted");
				} else {
					return Lng.str("not restricted");
				}
			}
		}, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(343);
					getBuildToolsManager().buildHelperReplace = !getBuildToolsManager().buildHelperReplace;
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		}) {

			Vector4f c = new Vector4f(0.7f, 0, 0, 1);

			@Override
			public Vector4f getBackgroundColorMouseOverlay() {
				if(getBuildToolsManager().buildHelperReplace) {
					return c;
				} else {
					return super.getBackgroundColorMouseOverlay();
				}
			}

			@Override
			public Vector4f getBackgroundColor() {
				if(getBuildToolsManager().buildHelperReplace) {
					return c;
				} else {
					return super.getBackgroundColor();
				}
			}

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
			 */
			@Override
			public void draw() {
				if(getBuildToolsManager().getBuildHelper() != null) {
					super.draw();
				}
			}
		};
		GUITextButton buildHelper = new GUITextButton(getState(), 100, 20, new Object() {

			@Override
			public String toString() {
				if(getBuildToolsManager().getBuildHelper() != null) {
					return Lng.str("clear helper");
				} else {
					return Lng.str("build helper");
				}
			}
		}, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					if(getBuildToolsManager().getBuildHelper() == null) {
						PlayerGameDropDownInput p = new PlayerGameDropDownInput("BuildToolsPanel_HELPERS", (GameClientState) getState(), Lng.str("Helpers"), 24, Lng.str("Choose a helper function"), getHelperList()) {

							@Override
							public boolean isOccluded() {
								return false;
							}

							@Override
							public void pressedOK(GUIListElement current) {
								deactivate();
								if(getBuildToolsManager().getBuildHelper() != null) {
									getBuildToolsManager().getBuildHelper().clean();
								}
								Class<? extends BuildHelper> buildHelperClass = (Class<? extends BuildHelper>) current.getContent().getUserPointer();
								try {
									final BuildHelper newInstance = getBuildHelperCached(buildHelperClass, getState().getCurrentPlayerObject());
									PlayerGameOkCancelInput c = new PlayerGameOkCancelInput("BuildToolsPanel_SETTINGS", getState(), Lng.str("Settings"), "") {

										@Override
										public void onDeactivate() {
										}

										@Override
										public boolean isOccluded() {
											return false;
										}

										@Override
										public void pressedOK() {
											/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
											AudioController.fireAudioEventID(344);
											deactivate();
											Thread t = new Thread(newInstance);
											t.start();
											(new PlayerThreadProgressInput("BuildToolsPanel_CALCULATING", getState(), Lng.str("Calculating Shape"), Lng.str("calculating..."), newInstance) {

												@Override
												public void onDeactivate() {
													// make OpenGL
													newInstance.onFinished();
													getBuildToolsManager().setBuildHelper(newInstance);
													System.err.println("[CLIENT] using build helper: " + newInstance);
												}

												@Override
												public boolean isOccluded() {
													return false;
												}

												@Override
												public void pressedOK() {
												}
											}).activate();
											/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
											AudioController.fireAudioEventID(345);
											// newInstance.create();
										}
									};
									c.getInputPanel().onInit();
									GUIScrollablePanel sc = new GUIScrollablePanel(400, 100, getState());
									GUIElementList helperFieldList = getHelperFieldList(newInstance);
									sc.setContent(helperFieldList);
									helperFieldList.setScrollPane(sc);
									c.getInputPanel().getContent().attach(sc);
									c.activate();
									/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
									AudioController.fireAudioEventID(346);
								} catch(SecurityException e) {
									e.printStackTrace();
								} catch(IllegalArgumentException e) {
									e.printStackTrace();
								}
							}

							@Override
							public void onDeactivate() {
							}
						};
						p.getInputPanel().onInit();
						GUICheckBox cb = new GUICheckBox(getState()) {

							@Override
							protected boolean isActivated() {
								return getBuildToolsManager().buildHelperReplace;
							}

							@Override
							protected void deactivate() throws StateParameterNotFoundException {
								getBuildToolsManager().buildHelperReplace = false;
							}

							@Override
							protected void activate() throws StateParameterNotFoundException {
								getBuildToolsManager().buildHelperReplace = true;
							}
						};
						GUITextOverlay t = new GUITextOverlay(getState());
						t.setTextSimple(Lng.str("restrict building/removing to helper area"));
						if(GUIElement.isNewHud()) {
							cb.getPos().x = 5;
							cb.getPos().y = 20;
							t.getPos().x = 30;
							t.getPos().y = 20;
						} else {
							cb.getPos().y = 64;
							t.getPos().x = 40;
							t.getPos().y = 70;
						}
						p.getInputPanel().getContent().attach(cb);
						p.getInputPanel().getContent().attach(t);
						p.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(347);
					} else {
						getBuildToolsManager().getBuildHelper().clean();
						getBuildToolsManager().setBuildHelper(null);
					}
				}
			}
		}) {

			Vector4f c = new Vector4f(0.7f, 0, 0, 1);

			@Override
			public Vector4f getBackgroundColorMouseOverlay() {
				if(getBuildToolsManager().getBuildHelper() != null) {
					return c;
				} else {
					return super.getBackgroundColorMouseOverlay();
				}
			}

			@Override
			public Vector4f getBackgroundColor() {
				if(getBuildToolsManager().getBuildHelper() != null) {
					return c;
				} else {
					return super.getBackgroundColor();
				}
			}

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
			 */
			@Override
			public void draw() {
				super.draw();
			}
		};
		GUITextButton undo = new GUITextButton(getState(), 50, 20, Lng.str("UNDO"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(348);
					getBuildToolsManager().undo();
				}
			}
		}) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
			 */
			@Override
			public void draw() {
				if(getBuildToolsManager().canUndo()) {
					super.draw();
				}
			}

			@Override
			public boolean isActive() {
				return super.isActive() && !getBuildToolsManager().isUndoRedoOnCooldown();
			}
		};
		GUITextButton redo = new GUITextButton(getState(), 50, 20, Lng.str("REDO"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(349);
					getBuildToolsManager().redo();
				}
			}
		}) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
			 */
			@Override
			public void draw() {
				if(getBuildToolsManager().canRedo()) {
					super.draw();
				}
			}

			@Override
			public boolean isActive() {
				return super.isActive() && !getBuildToolsManager().isUndoRedoOnCooldown();
			}
		};
		GUITextButton dockMode = new GUITextButton(getState(), 220, 20, new Object() {

			@Override
			public String toString() {
				return getBuildToolsManager().getCreateDockingModeMsg();
			}
		}, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return false;
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					if(!getBuildToolsManager().isInCreateDockingMode()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
						AudioController.fireAudioEventID(351);
						getBuildToolsManager().startCreateDockingMode();
					} else {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.DESELECT)*/
						AudioController.fireAudioEventID(350);
						getBuildToolsManager().cancelCreateDockingMode();
					}
				}
			}
		}) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextButton#draw()
			 */
			@Override
			public void draw() {
				super.draw();
			}

			@Override
			public boolean isActive() {
				return super.isActive();
			}
		};
		createDockPanel.attach(dockMode);
		undoRedoPanel.attach(undo);
		redo.setPos(80, 0, 0);
		undoRedoPanel.attach(redo);
		copy.setPos(0, 30, 0);
		copySelectionMode.setPos(0, 60, 0);
		copyPastePanel.attach(copy);
		copyPastePanel.attach(load);
		copyPastePanel.attach(copySelectionMode);
		paste.setPos(105, 0, 0);
		save.setPos(105, 30, 0);
		copyPastePanel.attach(paste);
		copyPastePanel.attach(save);
		removeFilterPanel.attach(removeFilter);
		removeFilterPanel.attach(removeFilterPickLookingAt);
		removeFilterPickLookingAt.setPos(removeFilter.getPos().x + removeFilter.getWidth() + 5, removeFilter.getPos().y, 0);
		GUIAnchor lightenRemoveMode = new GUIAnchor(getState(), 200, 60);
		GUISettingsListElement removeMode = new GUISettingsListElement(getState(), 100, 30, Lng.str("Remove Mode"), new GUICheckBox(getState()) {

			@Override
			public GUIToolTip initToolTip() {
				if(EngineSettings.DRAW_TOOL_TIPS.isOn()) {
					return new GUIToolTip(getState(), Lng.str("Bulk remove\nmultiple blocks\nat once"), this);
				} else {
					return null;
				}
			}

			@Override
			protected void activate() throws StateParameterNotFoundException {
				getBuildToolsManager().add = false;
			}

			@Override
			protected void deactivate() throws StateParameterNotFoundException {
				getBuildToolsManager().add = true;
			}

			@Override
			protected boolean isActivated() {
				return !getBuildToolsManager().add;
			}
		}, false, false);
		GUISettingsListElement showCenterOfMass = new GUISettingsListElement(getState(), 100, 30, Lng.str("Show Center of Mass"), new GUICheckBox(getState()) {

			@Override
			protected void activate() throws StateParameterNotFoundException {
				getBuildToolsManager().showCenterOfMass = true;
			}

			@Override
			protected void deactivate() throws StateParameterNotFoundException {
				getBuildToolsManager().showCenterOfMass = false;
			}

			@Override
			protected boolean isActivated() {
				return getBuildToolsManager().showCenterOfMass;
			}

			@Override
			public GUIToolTip initToolTip() {
				if(EngineSettings.DRAW_TOOL_TIPS.isOn()) {
					return new GUIToolTip(getState(), Lng.str("shows the center of mass"), this);
				} else {
					return null;
				}
			}
		}, false, false);
		GUISettingsListElement lightenMode = new GUISettingsListElement(getState(), 50, 30, Lng.str("Lighten"), new GUICheckBox(getState()) {

			@Override
			protected void activate() throws StateParameterNotFoundException {
				getBuildToolsManager().lighten = true;
			}

			@Override
			protected void deactivate() throws StateParameterNotFoundException {
				getBuildToolsManager().lighten = false;
			}

			@Override
			protected boolean isActivated() {
				return getBuildToolsManager().lighten;
			}

			@Override
			public GUIToolTip initToolTip() {
				if(EngineSettings.DRAW_TOOL_TIPS.isOn()) {
					return new GUIToolTip(getState(), Lng.str("Lighten mode:\nremoves shading\nfor easier\nbuilding"), this);
				} else {
					return null;
				}
			}
		}, false, false);
		GUISettingsListElement buildInfo = new GUISettingsListElement(getState(), 100, 30, Lng.str("Build Mode Info"), new GUICheckBox(getState()) {

			@Override
			protected void activate() throws StateParameterNotFoundException {
				getBuildToolsManager().buildInfo = true;
			}

			@Override
			protected void deactivate() throws StateParameterNotFoundException {
				getBuildToolsManager().buildInfo = false;
			}

			@Override
			protected boolean isActivated() {
				return getBuildToolsManager().buildInfo;
			}

			@Override
			public GUIToolTip initToolTip() {
				if(EngineSettings.DRAW_TOOL_TIPS.isOn()) {
					return new GUIToolTip(getState(), Lng.str("shows extra block info"), this);
				} else {
					return null;
				}
			}
		}, false, false);
		lightenRemoveMode.attach(removeMode);
		lightenMode.getPos().x = 150;
		lightenRemoveMode.attach(lightenMode);
		lightenRemoveMode.attach(showCenterOfMass);
		lightenRemoveMode.attach(buildInfo);
		showCenterOfMass.getPos().y = 20;
		buildInfo.getPos().y = 40;
		generalList.add(new GUIListElement(lightenRemoveMode, lightenRemoveMode, getState()));
		// generalList.add(new GUISettingsPanel( getState(), 60, 30, "X", new GUIBuildToolSettingSelector(getState(), getBuildToolsManager().width), false, false));
		// generalList.add(new GUISettingsPanel( getState(), 60, 30, "Y", new GUIBuildToolSettingSelector(getState(), getBuildToolsManager().height), false, false));
		// generalList.add(new GUISettingsPanel( getState(), 60, 70, "Z", new GUIBuildToolSettingSelector(getState(), getBuildToolsManager().depth), false, false));
		generalList.add(new GUISettingsListElement(getState(), 30, 28, "X", new GUISizeSettingSelectorScroll(getState(), getBuildToolsManager().width), false, false));
		generalList.add(new GUISettingsListElement(getState(), 30, 28, "Y", new GUISizeSettingSelectorScroll(getState(), getBuildToolsManager().height), false, false));
		generalList.add(new GUISettingsListElement(getState(), 30, 35, "Z", new GUISizeSettingSelectorScroll(getState(), getBuildToolsManager().depth), false, false));
		generalList.add(new GUISettingsListElement(getState(), 30, 40, Lng.str("Slab"), new GUISizeSettingSelectorScroll(getState(), getBuildToolsManager().slabSize), false, false) {

			@Override
			public void draw() {
				short type = getPlayerInteractionControlManager().getSelectedTypeWithSub();
				if(ElementKeyMap.isValidType(type) && ElementKeyMap.getInfoFast(type).slabIds != null && (getPlayerInteractionControlManager().isMultiSlot() || (ElementKeyMap.getInfoFast(type).slab == 0 && ElementKeyMap.getInfoFast(type).blocktypeIds == null && ElementKeyMap.getInfoFast(type).getSourceReference() == 0)) && ElementKeyMap.getInfoFast(type).blockStyle == BlockStyle.NORMAL) {
					super.draw();
				}
			}
		});
		final GUISettingsListElement guiSettingsPanel = new GUISettingsListElement(getState(), 90, 70, "Orientation", new GUIOrientationSettingElement(getState()), false, false);
		final GUITextOverlay pasteRot = new GUITextOverlay(FontLibrary.FontSize.TINY_12, getState());
		pasteRot.setTextSimple(Lng.str("Use PgUp/Down or wheel to rotate Y-axis\npaste. hold Left Control/Shift for X/Z"));
		GUIAnchor c = new GUIAnchor(getState(), 90, 70) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#draw()
			 */
			@Override
			public void draw() {
				super.draw();
				if(getBuildToolsManager().isPasteMode()) {
					pasteRot.draw();
				} else {
					guiSettingsPanel.draw();
				}
			}
		};
		generalList.add(new GUIListElement(c, c, getState()));
		generalList.add(new GUIListElement(undoRedoPanel, undoRedoPanel, getState()));
		generalList.add(new GUIListElement(copyPastePanel, copyPastePanel, getState()));
		generalList.add(new GUIListElement(createDockPanel, createDockPanel, getState()));
		GUITextOverlay sym = new GUITextOverlay(FontLibrary.FontSize.MEDIUM_18, getState());
		sym.setTextSimple(Lng.str("Symmetry Build Planes"));
		generalList.add(new GUIListElement(sym, sym, getState()));
		generalList.add(new GUISettingsListElement(getState(), 60, 30, Lng.str("XY-Plane"), new GUIBuildToolSymmetrySelector(getState(), SymmetryPlanes.MODE_XY), false, false));
		generalList.add(new GUISettingsListElement(getState(), 60, 30, Lng.str("XZ-Plane"), new GUIBuildToolSymmetrySelector(getState(), SymmetryPlanes.MODE_XZ), false, false));
		generalList.add(new GUISettingsListElement(getState(), 60, 30, Lng.str("YZ-Plane"), new GUIBuildToolSymmetrySelector(getState(), SymmetryPlanes.MODE_YZ), false, false));
		generalList.add(new GUISettingsListElement(getState(), 100, 34, Lng.str("Odd-sym Mode"), new GUICheckBox(getState()) {

			@Override
			protected void activate() throws StateParameterNotFoundException {
				getSymmetryPlanes().setExtraDist(0);
			}

			@Override
			protected void deactivate() throws StateParameterNotFoundException {
				getSymmetryPlanes().setExtraDist(1);
			}

			@Override
			protected boolean isActivated() {
				return getSymmetryPlanes().getExtraDist() == 0;
			}

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUISettingsElement#initToolTip()
			 */
			@Override
			public GUIToolTip initToolTip() {
				return new GUIToolTip(getState(), Lng.str("Place plane in the\nmiddle or on the\nedge"), this);
			}
		}, false, false));
		generalList.add(new GUISettingsListElement(getState(), 100, 34, Lng.str("Mirror Cubes"), new GUICheckBox(getState()) {

			@Override
			protected void activate() throws StateParameterNotFoundException {
				getSymmetryPlanes().setMirrorCubeShapes(true);
			}

			@Override
			protected void deactivate() throws StateParameterNotFoundException {
				getSymmetryPlanes().setMirrorCubeShapes(false);
			}

			@Override
			protected boolean isActivated() {
				return getSymmetryPlanes().isMirrorCubeShapes();
			}

			@Override
			public GUIToolTip initToolTip() {
				if(EngineSettings.DRAW_TOOL_TIPS.isOn()) {
					return new GUIToolTip(getState(), Lng.str("Mirror blocks like\nWeapon computers,\nhazard stripes,\netc"), this);
				} else {
					return null;
				}
			}
		}, false, false));
		GUIAnchor buildHelperPanel = new GUIAnchor(getState(), 240, 34);
		buildHelperPanel.attach(buildHelper);
		buildHelperRestrict.getPos().x = 100;
		buildHelperPanel.attach(buildHelperRestrict);
		generalList.add(new GUIListElement(buildHelperPanel, buildHelperPanel, getState()));
		generalList.add(new GUIListElement(removeFilterPanel, removeFilterPanel, getState()));
		if(EngineSettings.BLUEPRINT_STRUCTURE_BUILD_OPTIONS.isOn()) {
			GUIAnchor blueprintStructurePanel = new GUIAnchor(getState(), 240, 40);
			GUISettingsListElement blueprintEmptySwitch = new GUISettingsListElement(getState(), 100, 20, Lng.str("BPS Place in empty"), new GUICheckBox(getState()) {

				@Override
				protected void activate() {
					blueprintPlacementSetting = TerrainStructureBlueprint.PLACEMENT_EMPTY;
				}

				@Override
				protected void deactivate() {
					blueprintPlacementSetting = TerrainStructureBlueprint.PLACEMENT_FORCED;
				}

				@Override
				protected boolean isActivated() {
					return blueprintPlacementSetting == TerrainStructureBlueprint.PLACEMENT_EMPTY;
				}

				@Override
				public GUIToolTip initToolTip() {
					return new GUIToolTip(getState(), Lng.str("Used in blueprint structures to place blocks only if replacing empty space"), this);
				}
			}, false, false);
			GUISettingsListElement blueprintSolidSwitch = new GUISettingsListElement(getState(), 100, 20, Lng.str("BPS Place in solid"), new GUICheckBox(getState()) {

				@Override
				protected void activate() {
					blueprintPlacementSetting = TerrainStructureBlueprint.PLACEMENT_NOTEMPTY;
				}

				@Override
				protected void deactivate() {
					blueprintPlacementSetting = TerrainStructureBlueprint.PLACEMENT_FORCED;
				}

				@Override
				protected boolean isActivated() {
					return blueprintPlacementSetting == TerrainStructureBlueprint.PLACEMENT_NOTEMPTY;
				}

				@Override
				public GUIToolTip initToolTip() {
					return new GUIToolTip(getState(), Lng.str("Used in blueprint structures to place blocks only if replacing a solid block"), this);
				}
			}, false, false);
			blueprintStructurePanel.attach(blueprintEmptySwitch);
			blueprintSolidSwitch.getPos().y = 20;
			blueprintStructurePanel.attach(blueprintSolidSwitch);
			generalList.add(new GUIListElement(blueprintStructurePanel, blueprintStructurePanel, getState()));
		}
		// if(ai != null){
		// for(AIConfiguationElements s : ai.getAiConfiguration().getElements()){
		// if(s.getCurrentState() instanceof Boolean){
		// GUIBuildToolCheckBox guiCheckBox = new GUIBuildToolCheckBox(getState(), s);
		// generalList.add(new GUISettingsPanel(getState(), s.getDescription(), guiCheckBox));
		// }else{
		// generalList.add(new GUISettingsPanel(getState(), s.getDescription(), new GUIBuildToolSettingSelector(getState(), s)));
		// }
		// }
		// }
	}

	public GUIElementList getHelperFieldList(BuildHelper c) {
		Field[] fields = c.getClass().getFields();
		GUIElementList list = new GUIElementList(getState());
		for(int i = 0; i < fields.length; i++) {
			Field f = fields[i];
			BuildHelperVar annotation = f.getAnnotation(BuildHelperVar.class);
			if(annotation != null) {
				list.add(new GUISettingsListElement(getState(), 100, 28, annotation.name().toString(), new GUIScrollSettingSelector(getState(), GUIScrollablePanel.SCROLLABLE_HORIZONTAL, 200, FontLibrary.FontSize.MEDIUM_18) {

					@Override
					protected void decSetting() {
						try {
							f.setFloat(c, f.getFloat(c) - 1);
							settingChanged(null);
						} catch(IllegalArgumentException e) {
						} catch(IllegalAccessException e) {
							e.printStackTrace();
						}
					}

					@Override
					protected void incSetting() {
						try {
							f.setFloat(c, f.getFloat(c) + 1);
							settingChanged(null);
						} catch(IllegalArgumentException e) {
						} catch(IllegalAccessException e) {
							e.printStackTrace();
						}
					}

					@Override
					protected float getSettingX() {
						try {
							return f.getFloat(c);
						} catch(IllegalArgumentException e) {
							e.printStackTrace();
						} catch(IllegalAccessException e) {
							e.printStackTrace();
						}
						return 0;
					}

					@Override
					protected void setSettingX(float value) {
						try {
							f.setFloat(c, (int) value);
							settingChanged(null);
						} catch(IllegalArgumentException e) {
						} catch(IllegalAccessException e) {
							e.printStackTrace();
						}
					}

					@Override
					protected float getSettingY() {
						try {
							return f.getFloat(c);
						} catch(IllegalArgumentException e) {
							e.printStackTrace();
						} catch(IllegalAccessException e) {
							e.printStackTrace();
						}
						return 0;
					}

					@Override
					protected void setSettingY(float value) {
						try {
							f.setFloat(c, (int) value);
							settingChanged(null);
						} catch(IllegalArgumentException e) {
						} catch(IllegalAccessException e) {
							e.printStackTrace();
						}
					}

					@Override
					public void settingChanged(Object setting) {
						super.settingChanged(setting);
						// try {
						// ((GUITextOverlay)getSettingName()).getText().set(0, String.valueOf((int) f.getFloat(c)));
						// } catch (IllegalArgumentException e) {
						// } catch (IllegalAccessException e) {
						// e.printStackTrace();
						// }
					}

					@Override
					public float getMaxX() {
						return annotation.max();
					}

					@Override
					public float getMaxY() {
						return annotation.max();
					}

					@Override
					public float getMinX() {
						return 0;
					}

					@Override
					public float getMinY() {
						return 0;
					}

					@Override
					public boolean isVerticalActive() {
						return false;
					}
				}, false, false));
			}
		}
		return list;
	}

	private ArrayList<GUIElement> getHelperList() {
		ArrayList<GUIElement> helperList = new ArrayList<GUIElement>();
		for(BuildHelperFactory a : getBuildToolsManager().getBuildHelperClasses()) {
			GUIAnchor c = new GUIAnchor(getState(), 200, 24);
			GUITextOverlay o = new GUITextOverlay(getState());
			o.setTextSimple(a.getBuildHelperClass().getAnnotation(BuildHelperClass.class).name());
			o.setPos(5, 4, 0);
			c.attach(o);
			helperList.add(c);
			c.setUserPointer(a);
		}
		return helperList;
	}

	@Override
	public void update(Timer timer) {
		super.update(timer);
		if(KeyboardMappings.BUILD_MODE_FIX_CAM.isDown()) {
			scrollPanel.setContent(buildMode);
		} else {
			if(astronaut) {
				scrollPanel.setContent(nothing);
			} else {
				scrollPanel.setContent(altText);
			}
		}
		// if(getBuildToolsManager().isNeedsUpdate()){
		// if(getBuildToolsManager().getAiBlock() != null){
		// reconstructList((AiInterface) getBuildToolsManager().getAiBlock().getSegment().getSegmentController());
		// }else{
		// reconstructList(null);
		// }
		// getBuildToolsManager().setNeedsUpdate(false);
		// 
		// }
	}

	@Override
	public void drawToolTip() {
		generalList.drawToolTip();
	}
}
