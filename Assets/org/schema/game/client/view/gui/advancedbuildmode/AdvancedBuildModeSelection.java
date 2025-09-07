package org.schema.game.client.view.gui.advancedbuildmode;

import api.common.GameClient;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.client.controller.manager.ingame.BuildSelectionCopy;
import org.schema.game.client.controller.manager.ingame.BuildSelectionFillHelper;
import org.schema.game.client.controller.manager.ingame.CopyArea;
import org.schema.game.client.controller.manager.ingame.SegmentBuildController;
import org.schema.game.client.view.ElementCollectionDrawer;
import org.schema.game.client.view.gui.advanced.AdvancedGUIElement;
import org.schema.game.client.view.gui.advanced.tools.*;
import org.schema.game.common.controller.BlockTypeSearchRunnable;
import org.schema.game.common.controller.BlockTypeSearchRunnableManager;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.CubeRayCastResult;
import org.schema.game.common.data.physics.PhysicsExt;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDockableDirtyInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.resource.FileExt;

import javax.vecmath.Vector3f;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class AdvancedBuildModeSelection extends AdvancedBuildModeGUISGroup implements DropdownCallback, BlockTypeSearchRunnableManager.BlockTypeSearchProgressCallback {

	protected File selected;
	private ObjectArrayList<GUIElement> v;
	private boolean dirty = true;
	private boolean textChanged;
	private String curText = "";
	private ElementInformation selectedBlockType;
	private BlockTypeSearchRunnable currentSearch;

	public AdvancedBuildModeSelection(AdvancedGUIElement e) {
		super(e);
	}

	@Override
	public void build(GUIContentPane pane, GUIDockableDirtyInterface dInt) {
		pane.setTextBoxHeightLast(30);
		GUITextOverlay l2 = new GUITextOverlay(getState());
		addButton(pane.getContent(0), 0, 0, new ButtonResult() {
			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {
					@Override
					public void pressedRightMouse() {
					}

					@Override
					public void pressedLeftMouse() {
						getBuildToolsManager().setCopyMode(!getBuildToolsManager().isCopyMode());
					}
				};
			}

			@Override
			public boolean isActive() {
				return !getBuildToolsManager().isPasteMode() && !getBuildToolsManager().isSelectMode() && !(getBuildToolsManager().getSelectMode() instanceof BuildSelectionCopy);
			}

			@Override
			public boolean isHighlighted() {
				return getBuildToolsManager().isCopyMode();
			}

			@Override
			public String getName() {
				if(getBuildToolsManager().isCopyMode()) {
					return Lng.str("*PICK AREA*");
				} else {
					return Lng.str("Copy Brush");
				}
			}

			@Override
			public GUIHorizontalArea.HButtonColor getColor() {
				return GUIHorizontalArea.HButtonColor.BLUE;
			}
		});
		addButton(pane.getContent(0), 1, 0, new ButtonResult() {
			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {
					@Override
					public void pressedRightMouse() {
					}

					@Override
					public void pressedLeftMouse() {
						getBuildToolsManager().setSelectMode(getBuildToolsManager().isSelectMode() ? null : new BuildSelectionCopy());
					}
				};
			}

			@Override
			public boolean isActive() {
				return !getBuildToolsManager().isPasteMode() && !getBuildToolsManager().isCopyMode() && !(getBuildToolsManager().getSelectMode() instanceof BuildSelectionFillHelper);
			}

			@Override
			public boolean isHighlighted() {
				return getBuildToolsManager().getSelectMode() != null && getBuildToolsManager().getSelectMode() instanceof BuildSelectionCopy;
			}

			@Override
			public String getName() {
				if(getBuildToolsManager().getSelectMode() instanceof BuildSelectionCopy) {
					if(getBuildToolsManager().getSelectMode().selectionBoxA != null) {
						return Lng.str("*Corner 2*");
					}
					return Lng.str("*Corner 1*");
				}
				return Lng.str("Copy Selection");

			}

			@Override
			public GUIHorizontalArea.HButtonColor getColor() {
				return GUIHorizontalArea.HButtonColor.BLUE;
			}
		});

		addButton(pane.getContent(0), 0, 1, new ButtonResult() {
			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {
					@Override
					public void pressedRightMouse() {
					}

					@Override
					public void pressedLeftMouse() {
						getBuildToolsManager().setPasteMode(!getBuildToolsManager().isPasteMode());
					}
				};
			}

			@Override
			public String getName() {
				if(getBuildToolsManager().isPasteMode()) {
					return Lng.str("*Paste*");
				} else {
					return Lng.str("Paste");
				}
			}

			@Override
			public boolean isHighlighted() {
				return getBuildToolsManager().isPasteMode();
			}

			@Override
			public boolean isActive() {
				return getBuildToolsManager().canPaste() && !getBuildToolsManager().isSelectMode();
			}

			@Override
			public GUIHorizontalArea.HButtonColor getColor() {
				return GUIHorizontalArea.HButtonColor.BLUE;
			}
		});
		addButton(pane.getContent(0), 1, 1, new ButtonResult() {
			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {
					@Override
					public void pressedRightMouse() {
					}

					@Override
					public void pressedLeftMouse() {
						if(getBuildToolsManager().isPasteMode() && getBuildToolsManager().getCopyArea() != null) {
							getBuildToolsManager().getCopyArea().locked = !getBuildToolsManager().getCopyArea().locked;
						}
					}
				};
			}

			@Override
			public String getName() {
				if(getBuildToolsManager().getCopyArea() != null && getBuildToolsManager().getCopyArea().locked) {
					return Lng.str("*Locked*");
				}
				return Lng.str("Lock Placement");
			}

			@Override
			public boolean isActive() {
				return getBuildToolsManager().isPasteMode() && getBuildToolsManager().getCopyArea() != null;
			}

			@Override
			public GUIHorizontalArea.HButtonColor getColor() {
				return GUIHorizontalArea.HButtonColor.BLUE;
			}
		});
		addDropdown(pane.getContent(0), 0, 2, new DropdownResult() {

			@Override
			public DropdownCallback initCallback() {
				return new DropdownCallback() {

					@Override
					public void onChanged(Object value) {
						if(value != null && value instanceof File) {
							selected = (File) value;
							System.err.println("[CLIENT] selected template: " + selected.getAbsolutePath());
						}
					}
				};
			}

			@Override
			public String getToolTipText() {
				if(selected != null) {
					return selected.getName();
				} else {
					return Lng.str("Select Template to load");
				}
			}

			@Override
			public String getName() {
				return Lng.str("Templates");
			}

			@Override
			public Collection<? extends GUIElement> getDropdownElements(GUIElement dep) {
				return getObjects(dep);
			}

			@Override
			public Object getDefault() {
				List<GUIElement> objects = v;
				if(objects != null && objects.size() > 0) {
					return objects.get(0);
				} else {
					return null;
				}
			}

			@Override
			public boolean needsListUpdate() {
				return dirty;
			}

			@Override
			public void flagListNeedsUpdate(boolean b) {
				dirty = b;
			}
		});
		addButton(pane.getContent(0), 0, 3, new ButtonResult() {
			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {
					@Override
					public void pressedRightMouse() {
					}

					@Override
					public void pressedLeftMouse() {
						if(selected != null) {
							File f = selected;
							try {
								getBuildToolsManager().loadCopyArea(f);
								getState().getController().popupInfoTextMessage(Lng.str("Template loaded\n%s", f.getName()), 0);
							} catch(IOException e) {
								e.printStackTrace();
								getState().getController().popupAlertTextMessage(Lng.str("Failed to load\n%s", f.getName()), 0);
							}
						} else {
							getState().getController().popupAlertTextMessage(Lng.str("No Templates found!"), 0);
						}
					}
				};
			}

			@Override
			public String getName() {
				return Lng.str("Load");
			}

			@Override
			public GUIHorizontalArea.HButtonColor getColor() {
				return GUIHorizontalArea.HButtonColor.BLUE;
			}
		});
		addButton(pane.getContent(0), 1, 3, new ButtonResult() {
			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {
					@Override
					public void pressedRightMouse() {
					}

					@Override
					public void pressedLeftMouse() {
						PlayerGameTextInput pp = new PlayerGameTextInput("BuildToolsPanel_SAVE_TMP", getState(), 50, Lng.str("Save Template"), "") {
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
													dirty = true;
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
											dirty = true;
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
					}
				};
			}

			@Override
			public String getName() {
				return Lng.str("Save");
			}

			@Override
			public boolean isActive() {
				return getBuildToolsManager().canPaste();
			}

			@Override
			public GUIHorizontalArea.HButtonColor getColor() {
				return GUIHorizontalArea.HButtonColor.BLUE;
			}
		});

		pane.addNewTextBox(30);
		addTextBar(pane.getContent(1), 0, 0, new TextBarResult() {

			@Override
			public TextBarCallback initCallback() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getToolTipText() {
				return Lng.str("Enter blocks to search for in the drop down");
			}

			@Override
			public String getName() {
				return Lng.str("Search Blocks");
			}

			@Override
			public String onTextChanged(String text) {
				String t = text.trim();
				if(!t.equals(curText)) {
					curText = t;
					textChanged = true;
				}
				return text;
			}
		});
		addDropdown(pane.getContent(1), 0, 1, new DropdownResult() {

			private List<GUIElement> blockElements;

			@Override
			public DropdownCallback initCallback() {
				return AdvancedBuildModeSelection.this;
			}

			@Override
			public String getToolTipText() {
				return "Select block to replace";
			}

			@Override
			public String getName() {
				return "Block";
			}

			@Override
			public boolean needsListUpdate() {
				return textChanged;
			}

			@Override
			public Collection<? extends GUIElement> getDropdownElements(GUIElement dep) {
				blockElements = GUIAdvTool.getBlockElements(getState(), curText, dep, null);
				return blockElements;
			}

			@Override
			public int getDropdownHeight() {
				return 26;
			}

			@Override
			public Object getDefault() {
				if(blockElements != null && !blockElements.isEmpty()) {
					return blockElements.getFirst();
				} else {
					return null;
				}
			}

			@Override
			public void flagListNeedsUpdate(boolean b) {
				textChanged = b;
			}
		});
		addBlockDisplay(pane.getContent(1), 0, 2, new BlockDisplayResult() {

			@Override
			public BlockSelectCallback initCallback() {
				return null;
			}

			@Override
			public String getToolTipText() {
				return "Selected Block";
			}

			@Override
			public short getDefault() {
				return getBuildToolsManager().getRemoveFilter();
			}

			@Override
			public short getCurrentValue() {
				return getBuildToolsManager().getRemoveFilter();
			}

			@Override
			public float getIconScale() {
				return 0.5f;
			}

			@Override
			public float getWeight() {
				return 0.3f;
			}

		});
		addButton(pane.getContent(1), 1, 2, new ButtonResult() {
			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {
					@Override
					public void pressedRightMouse() {
					}

					@Override
					public void pressedLeftMouse() {
						if(getBuildToolsManager().getRemoveFilter() == 0) {
							getBuildToolsManager().setRemoveFilter(selectedBlockType.id);
						} else {
							getBuildToolsManager().setRemoveFilter((short) 0);
						}
					}
				};
			}

			@Override
			public String getName() {
				if(getBuildToolsManager().getRemoveFilter() != 0) {
					return Lng.str("Clear");
				} else {
					return Lng.str("Remove/Replace");
				}
			}

			@Override
			public GUIHorizontalArea.HButtonColor getColor() {
				if(getBuildToolsManager().getRemoveFilter() != 0) {
					return GUIHorizontalArea.HButtonColor.ORANGE;
				} else {
					return GUIHorizontalArea.HButtonColor.BLUE;
				}
			}

			@Override
			public boolean isHighlighted() {
				return getBuildToolsManager().getRemoveFilter() != 0;
			}

		});

		addButton(pane.getContent(1), 2, 2, new ButtonResult() {
			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {
					@Override
					public void pressedRightMouse() {
					}

					@Override
					public void pressedLeftMouse() {

						Vector3f camPos = new Vector3f(Controller.getCamera().getPos());
						Vector3f camTo = new Vector3f(camPos);
						Vector3f forw = new Vector3f(Controller.getCamera().getForward());
						if(Float.isNaN(forw.x)) {
							return;
						}
						forw.normalize();
						forw.scale(SegmentBuildController.EDIT_DISTANCE);
						camTo.add(forw);

						CollisionWorld.ClosestRayResultCallback c = ((PhysicsExt) getState().getPhysics()).testRayCollisionPoint(camPos, camTo, false, getState().getCharacter(), null, true, true, false);

						if(c != null && c.hasHit() && c instanceof CubeRayCastResult cc && cc.getSegment() != null) {
							SegmentPiece p = new SegmentPiece(cc.getSegment(), cc.getCubePos());
							if(ElementKeyMap.isValidType(p.getType())) {
								ElementInformation i = ElementKeyMap.getInfo(p.getType());
								getBuildToolsManager().setRemoveFilter(p.getType());

								System.err.println("[BUILDTOOLS] set type by pick: " + p.getType());
							}
						}

					}
				};
			}

			@Override
			public String getName() {
				return Lng.str("Pick with camera");
			}

			@Override
			public GUIHorizontalArea.HButtonColor getColor() {
				return GUIHorizontalArea.HButtonColor.BLUE;
			}
		});

		addCheckbox(pane.getContent(1), 0, 3, new CheckboxResult() {

			@Override
			public CheckboxCallback initCallback() {
				return null;
			}

			@Override
			public String getName() {
				return "Replace with active slot";
			}

			@Override
			public boolean getDefault() {
				return getBuildToolsManager().isReplaceRemoveFilter();
			}

			@Override
			public boolean getCurrentValue() {
				return getBuildToolsManager().isReplaceRemoveFilter();
			}

			@Override
			public void setCurrentValue(boolean b) {
				getBuildToolsManager().setReplaceRemoveFilter(b);
			}

			@Override
			public String getToolTipText() {
				return Lng.str("Only replace. Don't place any other blocks.");
			}
		});

		addButton(pane.getContent(1), 0, 4, new ButtonResult() {
			@Override
			public ButtonCallback initCallback() {
				return new ButtonCallback() {

					@Override
					public void pressedRightMouse() {
					}

					@Override
					public void pressedLeftMouse() {
						if(ElementCollectionDrawer.searchForTypeResult != null) {
							ElementCollectionDrawer.searchForTypeResult.cleanUp();
							ElementCollectionDrawer.searchForTypeResult = null;
						} else if(selectedBlockType != null && getState().getCurrentPlayerObject() instanceof SegmentController) {
							BlockTypeSearchRunnableManager m = ((SegmentController) getState().getCurrentPlayerObject()).getBlockTypeSearchManager();
							currentSearch = m.searchByBlock(AdvancedBuildModeSelection.this, selectedBlockType.id);
						}

					}
				};
			}

			@Override
			public String getName() {
				return ElementCollectionDrawer.searchForTypeResult != null ? Lng.str("Clear Highlight") : (currentSearch != null ? Lng.str("Searching...") : Lng.str("Highlight blocks in dropdown"));
			}

			@Override
			public boolean isActive() {
				return super.isActive() && (currentSearch == null && selectedBlockType != null && getState().getCurrentPlayerObject() instanceof SegmentController);
			}

			@Override
			public GUIHorizontalArea.HButtonColor getColor() {
				return GUIHorizontalArea.HButtonColor.BLUE;
			}
		});
	}

	@Override
	public String getId() {
		return "BSELECTION";
	}

	@Override
	public String getTitle() {
		return Lng.str("Selection");
	}

	public List<GUIElement> getObjects(GUIElement dep) {
		if(dirty) {
			v = new ObjectArrayList<GUIElement>();

			File path = new FileExt(CopyArea.path);

			if(path.exists()) {
				File[] listFiles = path.listFiles();

				Arrays.sort(listFiles, 0, listFiles.length, new Comparator<File>() {
					@Override
					public int compare(File o1, File o2) {
						return o1.getName().compareTo(o2.getName());
					}
				});
				for(File f : listFiles) {
					if(!f.isDirectory() && f.getName().endsWith(".smtpl")) {
						GUITextOverlay t = new GUITextOverlay(FontLibrary.FontSize.MEDIUM_15, getState()) {

							@Override
							public void draw() {
								super.draw();
								limitTextWidth = (int) (dep.getWidth() - 26);
							}

						};

						t.setTextSimple(f.getName().substring(0, f.getName().lastIndexOf(".smtpl")));
						t.setPos(3, 2, 0);
						t.setUserPointer(f);
						v.add(t);
					}
				}
			}
			dirty = false;
		}
		return v;
	}

	@Override
	public void onChanged(Object value) {
		if(value instanceof ElementInformation) {
			selectedBlockType = (ElementInformation) value;
		}
	}

	@Override
	public void onDone() {
		currentSearch = null;
	}

}
