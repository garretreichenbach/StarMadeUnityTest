package org.schema.game.client.view.mainmenu.gui.catalogmanager;

import api.smd.SMDEntryUtils;
import api.utils.gui.SimpleGUIVerticalButtonPane;
import api.utils.gui.SimplePopup;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.io.FileUtils;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.client.controller.PlayerTextInput;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.client.view.mainmenu.FileChooserDialog;
import org.schema.game.client.view.mainmenu.MainMenuGUI;
import org.schema.game.client.view.mainmenu.gui.FileChooserStats;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.controller.CatalogEntryNotFoundException;
import org.schema.game.server.data.blueprintnw.BlueprintEntry;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CatalogManagerPanel extends GUIElement implements GUIActiveInterface {

	private static BlueprintEntryScrollableTableList blueprintList;
	private static TemplateScrollableList templateList;
	public GUIMainWindow mainPanel;
	private boolean init;
	private GUIContentPane blueprintsTab;
	private GUIContentPane templatesTab;
	private GUIContentPane browseTab;
	private final DialogInput dialogInput;
	private final List<GUIElement> toCleanUp = new ObjectArrayList<>();
	private FileChooserDialog blueprintChooserDialog;
	private FileChooserDialog templateChooserDialog;

	public CatalogManagerPanel(InputState state, DialogInput dialogInput) {
		super(state);
		this.dialogInput = dialogInput;
	}

	public static void updateLists() {
		if(blueprintList != null) blueprintList.updateBlueprints();
		if(templateList != null) templateList.updateTemplates();
	}

	@Override
	public float getWidth() {
		return GLFrame.getWidth() - 470;
	}

	@Override
	public float getHeight() {
		return GLFrame.getHeight() - 70;
	}

	@Override
	public boolean isInside() {
		return mainPanel.isInside();
	}

	@Override
	public boolean isActive() {
		List<DialogInterface> playerInputs = getState().getController().getInputController().getPlayerInputs();
		return !MainMenuGUI.runningSwingDialog && (playerInputs.isEmpty() || playerInputs.get(playerInputs.size() - 1).getInputPanel() == this);
	}

	@Override
	public void cleanUp() {
		for(GUIElement e : toCleanUp) {
			e.cleanUp();
		}
		toCleanUp.clear();
	}

	@Override
	public void draw() {
		if(!init) onInit();
		GlUtil.glPushMatrix();
		transform();
		mainPanel.draw();
		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		if(init) return;
		mainPanel = new GUIMainWindow(getState(), GLFrame.getWidth() - 410, GLFrame.getHeight() - 20, 400, 10, "Catalog Manager");
		mainPanel.onInit();
		mainPanel.setPos(435, 35, 0);
		mainPanel.setWidth(GLFrame.getWidth() - 470);
		mainPanel.setHeight(GLFrame.getHeight() - 70);
		mainPanel.clearTabs();

		createBlueprintListTab();
		blueprintsTab.onInit();
		createTemplatesTab();
		templatesTab.onInit();
		createBrowseTab();
		browseTab.onInit();

		mainPanel.setSelectedTab(0);
		mainPanel.activeInterface = this;

		mainPanel.setCloseCallback(new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					dialogInput.deactivate();
				}
			}

			@Override
			public boolean isOccluded() {
				return !isActive();
			}
		});

		toCleanUp.add(mainPanel);

		blueprintChooserDialog = new FileChooserDialog(getState(), new FileChooserStats(getState(), getStarMadeFolder(), "", new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory() || file.getName().toLowerCase(Locale.ENGLISH).endsWith(".sment");
			}

			@Override
			public String getDescription() {
				return "StarMade Blueprint (.sment)";
			}
		}) {

			@Override
			public void onSelectedFile(File dir, String file) {
				File f = new File(dir, file);
				ArrayList<String> blueprintNames = new ArrayList<>();
				for(BlueprintEntry BP : BluePrintController.active.readBluePrints()) blueprintNames.add(BP.getName());
				String bpName = f.getName().substring(0, f.getName().indexOf(".sment"));
				if(f.exists() && !(blueprintNames.contains(bpName))) {
					try {
						System.err.println("IMPORTING BLUEPRINT " + f.getCanonicalPath());
						BluePrintController.active.importFile(f, null);
						blueprintList.updated = false;
						blueprintList.updateBlueprints();
						//getState().getController().queueUIAudio("0022_menu_ui - enter");
					} catch(Exception e) { //Maybe this will fix the import string error nonsense?
						e.printStackTrace();
					}
				} else if(f.exists() && blueprintNames.contains(bpName)) {
					PlayerOkCancelInput playerInput = new PlayerOkCancelInput("CONFIRM", getState(), 500, 300, Lng.str("Error"), "A Blueprint with the name " + bpName + " already exists in your catalog!") {
						@Override
						public void onDeactivate() { }

						@Override
						public void pressedOK() {
							//getState().getController().queueUIAudio("0022_menu_ui - error 1");
							deactivate();
						}
					};
					playerInput.getInputPanel().onInit();
					playerInput.getInputPanel().setCancelButton(false);
					playerInput.getInputPanel().background.setPos(470.0F, 35.0F, 0.0F);
					playerInput.getInputPanel().background.setWidth((GLFrame.getWidth() - 435));
					playerInput.getInputPanel().background.setHeight((GLFrame.getHeight() - 70));
					playerInput.activate();
				} else {
					PlayerOkCancelInput playerInput = new PlayerOkCancelInput("CONFIRM", getState(), 500, 300, Lng.str("Error"), Lng.str("The file %s doesn't exist", bpName)) {
						@Override
						public void onDeactivate() { }

						@Override
						public void pressedOK() {
							//getState().getController().queueUIAudio("0022_menu_ui - error 2");
							deactivate();
						}
					};
					playerInput.getInputPanel().onInit();
					playerInput.getInputPanel().setCancelButton(false);
					playerInput.getInputPanel().background.setPos(470.0F, 35.0F, 0.0F);
					playerInput.getInputPanel().background.setWidth((GLFrame.getWidth() - 435));
					playerInput.getInputPanel().background.setHeight((GLFrame.getHeight() - 70));
					playerInput.activate();
				}
			}
		});

		templateChooserDialog = new FileChooserDialog(getState(), new FileChooserStats(getState(), getStarMadeFolder(), "", new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory() || file.getName().toLowerCase(Locale.ENGLISH).endsWith(".smtpl");
			}

			@Override
			public String getDescription() {
				return "StarMade Template (.smtpl)";
			}
		}) {

			@Override
			public void onSelectedFile(File dir, String file) {
				File f = new File(dir, file);
				ArrayList<String> templateNames = new ArrayList<>();
				File templatesFolder = new File("./templates");
				if(!templatesFolder.exists()) templatesFolder.mkdirs();
				if(templatesFolder.listFiles() != null) {
					for(File templateFile : templatesFolder.listFiles()) {
						templateNames.add(templateFile.getName().substring(0, templateFile.getName().indexOf(".smtpl") - 1));
					}
				}
				String templateName = f.getName().substring(0, f.getName().indexOf(".smtpl"));
				if(f.exists() && !(templateNames.contains(templateName))) {
					try {
						System.err.println("IMPORTING TEMPLATE " + f.getCanonicalPath());
						FileUtils.copyFile(f, new File(templatesFolder.getAbsolutePath() + "/" + f.getName()));
						templateList.updated = false;
						templateList.updateTemplates();
						//getState().getController().queueUIAudio("0022_menu_ui - enter");
					} catch(IOException e) {
						e.printStackTrace();
					}
				} else if(f.exists() && templateNames.contains(templateName)) {
					PlayerOkCancelInput playerInput = new PlayerOkCancelInput("CONFIRM", getState(), 500, 300, Lng.str("Error"), "A Template with the name " + templateName + " already exists in your catalog!") {
						@Override
						public void onDeactivate() { }

						@Override
						public void pressedOK() {
							//getState().getController().queueUIAudio("0022_menu_ui - error 1");
							deactivate();
						}
					};
					playerInput.getInputPanel().onInit();
					playerInput.getInputPanel().setCancelButton(false);
					playerInput.getInputPanel().background.setPos(470.0F, 35.0F, 0.0F);
					playerInput.getInputPanel().background.setWidth((GLFrame.getWidth() - 435));
					playerInput.getInputPanel().background.setHeight((GLFrame.getHeight() - 70));
					playerInput.activate();
				} else {
					PlayerOkCancelInput playerInput = new PlayerOkCancelInput("CONFIRM", getState(), 500, 300, Lng.str("Error"), Lng.str("The file %s doesn't exist", templateName)) {
						@Override
						public void onDeactivate() { }

						@Override
						public void pressedOK() {
							//getState().getController().queueUIAudio("0022_menu_ui - error 2");
							deactivate();
						}
					};
					playerInput.getInputPanel().onInit();
					playerInput.getInputPanel().setCancelButton(false);
					playerInput.getInputPanel().background.setPos(470.0F, 35.0F, 0.0F);
					playerInput.getInputPanel().background.setWidth((GLFrame.getWidth() - 435));
					playerInput.getInputPanel().background.setHeight((GLFrame.getHeight() - 70));
					playerInput.activate();
				}
			}
		});

		init = true;
	}

	private void createBlueprintListTab() {
		blueprintsTab = mainPanel.addTab("BLUEPRINTS");
		blueprintsTab.setTextBoxHeightLast(mainPanel.getInnerHeigth() - 80);
		blueprintList = new BlueprintEntryScrollableTableList(getState(), mainPanel.getInnerWidth(), mainPanel.getInnerHeigth() - 80, blueprintsTab.getContent(0));
		blueprintList.onInit();
		blueprintsTab.getContent(0).attach(blueprintList);
		toCleanUp.add(blueprintList);

		blueprintsTab.addNewTextBox(28);
		GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 3, 1, blueprintsTab.getContent(1));
		buttonPane.onInit();

		buttonPane.addButton(0, 0, "IMPORT", GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent mouseEvent) {
				if(mouseEvent.pressedLeftMouse()) {
					//getState().getController().queueUIAudio("0022_menu_ui - select 1");
					toggleBlueprintFileBrowser();
				}
			}

			@Override
			public boolean isOccluded() {
				return !inputsEmpty();
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return inputsEmpty();
			}
		});

		buttonPane.addButton(1, 0, "EXPORT", GUIHorizontalArea.HButtonColor.GREEN, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent mouseEvent) {
				if(mouseEvent.pressedLeftMouse()) {
					if(blueprintList.getSelectedRow() != null && blueprintList.getSelectedRow().f != null) {
						//getState().getController().queueUIAudio("0022_menu_ui - select 2");
						try {
							BluePrintController.active.export(blueprintList.getSelectedRow().f.getName());
							new SimplePopup(getState(), "Success", "Successfully exported blueprint \"" + blueprintList.getSelectedRow().f.getName() + "\".");
						} catch(IOException | CatalogEntryNotFoundException e) {
							e.printStackTrace();
						}
						blueprintList.updated = false;
						blueprintList.updateBlueprints();
					}
				}
			}

			@Override
			public boolean isOccluded() {
				return !inputsEmpty();
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return inputsEmpty();
			}
		});

		buttonPane.addButton(2, 0, "DELETE", GUIHorizontalArea.HButtonColor.RED, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent mouseEvent) {
				if(mouseEvent.pressedLeftMouse()) {
					if(blueprintList.getSelectedRow() != null && blueprintList.getSelectedRow().f != null) {
						//getState().getController().queueUIAudio("0022_menu_ui - select 3");
						PlayerOkCancelInput confirmBox = new PlayerOkCancelInput("CONFIRM", getState(), 500, 300, "Confirm Deletion", "This will remove the blueprint from your catalog and delete it on disk. Proceed?") {
							@Override
							public void onDeactivate() {
							}

							@Override
							public void pressedOK() {
								try {
									BluePrintController.active.removeBluePrint(blueprintList.getSelectedRow().f);
								} catch(IOException e) {
									e.printStackTrace();
								}
								//if(SteamAPIHandler.initialized) Starter.apiHandler.removeBlueprint(blueprintList.getSelectedRow().f.getName());
								blueprintList.updated = false;
								blueprintList.updateBlueprints();
								BluePrintController.active.readBluePrints();
								//getState().getController().queueUIAudio("0022_menu_ui - enter");
								deactivate();
							}
						};
						confirmBox.getInputPanel().onInit();
						confirmBox.getInputPanel().background.setPos(470.0F, 35.0F, 0.0F);
						confirmBox.getInputPanel().background.setWidth((GLFrame.getWidth() - 435));
						confirmBox.getInputPanel().background.setHeight((GLFrame.getHeight() - 70));
						confirmBox.activate();
					}
				}
			}

			@Override
			public boolean isOccluded() {
				return !inputsEmpty();
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return inputsEmpty();
			}
		});

		blueprintsTab.getContent(1).attach(buttonPane);
		toCleanUp.add(buttonPane);
		toCleanUp.add(blueprintsTab);
	}

	private void createTemplatesTab() {
		templatesTab = mainPanel.addTab("TEMPLATES");
		templatesTab.setTextBoxHeightLast(mainPanel.getInnerHeigth() - 80);

		templateList = new TemplateScrollableList(getState(), mainPanel.getInnerWidth(), mainPanel.getInnerHeigth() - 80, templatesTab.getContent(0));
		templateList.onInit();
		templatesTab.getContent(0).attach(templateList);
		toCleanUp.add(templateList);

		templatesTab.addNewTextBox(28);
		GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 3, 1, templatesTab.getContent(1));
		buttonPane.onInit();

		buttonPane.addButton(0, 0, "IMPORT", GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent mouseEvent) {
				if(mouseEvent.pressedLeftMouse()) {
					//getState().getController().queueUIAudio("0022_menu_ui - select 1");
					toggleTemplateFileBrowser();
					templateList.updated = false;
					templateList.updateTemplates();
				}
			}

			@Override
			public boolean isOccluded() {
				return !inputsEmpty();
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return inputsEmpty();
			}
		});

		buttonPane.addButton(1, 0, "RENAME", GUIHorizontalArea.HButtonColor.ORANGE, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent mouseEvent) {
				if(mouseEvent.pressedLeftMouse()) {
					if(templateList.getSelectedRow() != null && templateList.getSelectedRow().f != null) {
						//getState().getController().queueUIAudio("0022_menu_ui - select 2");
						ArrayList<String> fileNames = new ArrayList<>();
						File templatesFolder = new File("./templates");
						if(!templatesFolder.exists()) templatesFolder.mkdirs();
						if(templatesFolder.listFiles() != null) {
							for(File templateFile : templatesFolder.listFiles()) {
								fileNames.add(templateFile.getName().substring(0, templateFile.getName().indexOf(".smtpl")));
							}
						}

						PlayerTextInput textInputBox = new PlayerTextInput("RENAME_TEMPLATE", getState(), 64, "Enter a new name", "Enter a new name for template " + templateList.getSelectedRow().f.getName().substring(0, templateList.getSelectedRow().f.getName().indexOf(".smtpl"))) {
							@Override
							public void onDeactivate() {

							}

							@Override
							public boolean onInput(String s) {
								if(fileNames.contains(s)) {
									PlayerOkCancelInput playerInput = new PlayerOkCancelInput("CONFIRM", getState(), 300, 150, Lng.str("Error"), "A Template with the name " + s + " already exists in your catalog!") {
										@Override
										public void onDeactivate() {
										}

										@Override
										public void pressedOK() {
											//getState().getController().queueUIAudio("0022_menu_ui - error 1");
											deactivate();
										}
									};
									playerInput.getInputPanel().onInit();
									playerInput.getInputPanel().setCancelButton(false);
									playerInput.getInputPanel().background.setPos(470.0F, 35.0F, 0.0F);
									playerInput.getInputPanel().background.setWidth((GLFrame.getWidth() - 435));
									playerInput.getInputPanel().background.setHeight((GLFrame.getHeight() - 70));
									playerInput.activate();
									return false;
								} else {
									try {
										File newTemplate = new File(templatesFolder.getAbsolutePath() + "/" + s + ".smtpl");
										newTemplate.createNewFile();
										//if(SteamAPIHandler.initialized) Starter.apiHandler.removeTemplate(templateList.getSelectedRow().f.getName());
										FileUtils.copyFile(templateList.getSelectedRow().f, newTemplate);
										FileUtils.forceDelete(templateList.getSelectedRow().f);
										//if(SteamAPIHandler.initialized) Starter.apiHandler.addTemplate(newTemplate.getName());
										templateList.updated = false;
										templateList.updateTemplates();
										//getState().getController().queueUIAudio("0022_menu_ui - enter");
										return true;
									} catch(IOException e) {
										e.printStackTrace();
										return false;
									}
								}
							}

							@Override
							public String[] getCommandPrefixes() {
								return null;
							}

							@Override
							public String handleAutoComplete(String s, TextCallback textCallback, String s1) throws PrefixNotFoundException {
								return null;
							}

							@Override
							public void onFailedTextCheck(String s) {

							}
						};
						textInputBox.getInputPanel().onInit();
						textInputBox.getInputPanel().background.setPos(470.0F, 35.0F, 0.0F);
						textInputBox.getInputPanel().background.setWidth((GLFrame.getWidth() - 435));
						textInputBox.getInputPanel().background.setHeight((GLFrame.getHeight() - 70));
						textInputBox.activate();
					}

					templateList.updated = false;
					templateList.updateTemplates();
				}
			}

			@Override
			public boolean isOccluded() {
				return !inputsEmpty();
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return inputsEmpty();
			}
		});

		buttonPane.addButton(2, 0, "DELETE", GUIHorizontalArea.HButtonColor.RED, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent mouseEvent) {
				if(mouseEvent.pressedLeftMouse()) {
					//getState().getController().queueUIAudio("0022_menu_ui - select 3");
					if(templateList.getSelectedRow() != null && templateList.getSelectedRow().f != null) {
						PlayerOkCancelInput confirmBox = new PlayerOkCancelInput("CONFIRM", getState(), 300, 150, "Confirm Deletion", "This will remove the template from your catalog and delete it on disk. Proceed?") {
							@Override
							public void onDeactivate() {
							}

							@Override
							public void pressedOK() {
								try {
									//if(SteamAPIHandler.initialized) Starter.apiHandler.removeTemplate(templateList.getSelectedRow().f.getName());
									FileUtils.forceDelete(templateList.getSelectedRow().f);
									//getState().getController().queueUIAudio("0022_menu_ui - enter");
								} catch(IOException e) {
									e.printStackTrace();
								}
								templateList.updated = false;
								templateList.updateTemplates();
								deactivate();
							}
						};
						confirmBox.getInputPanel().onInit();
						confirmBox.getInputPanel().background.setPos(470.0F, 35.0F, 0.0F);
						confirmBox.getInputPanel().background.setWidth((GLFrame.getWidth() - 435));
						confirmBox.getInputPanel().background.setHeight((GLFrame.getHeight() - 70));
						confirmBox.activate();
					} //else getState().getController().queueUIAudio("0022_menu_ui - error 1");
				}
			}

			@Override
			public boolean isOccluded() {
				return !inputsEmpty();
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return inputsEmpty();
			}
		});

		templatesTab.getContent(1).attach(buttonPane);
		toCleanUp.add(buttonPane);
		toCleanUp.add(templatesTab);
	}

	private void createBrowseTab() {
		SMDEntryUtils.fetchDataOnThread();
		browseTab = mainPanel.addTab("BROWSE");
		browseTab.addDivider(121);

		SMDContentScrollableList contentList = new SMDContentScrollableList(getState(), mainPanel.getInnerWidth(), mainPanel.getInnerHeigth() - 44, browseTab.getContent(1, 0));
		contentList.onInit();

		SimpleGUIVerticalButtonPane buttonPane = new SimpleGUIVerticalButtonPane(getState(), 24, browseTab.getHeight() - 94, 2);

		GUITextButton allButton = new GUITextButton(getState(), 105, 30, GUITextButton.ColorPalette.OK, "ALL", new GUICallback() {
			@Override
			public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
				if(mouseEvent.pressedLeftMouse()) contentList.setCurrentFilter(FilterType.ALL);
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
		buttonPane.addButton(allButton);

		GUITextButton allShipsButton = new GUITextButton(getState(), 105, 30, GUITextButton.ColorPalette.OK, "SHIPS", new GUICallback() {
			@Override
			public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
				if(mouseEvent.pressedLeftMouse()) contentList.setCurrentFilter(FilterType.SHIPS_ALL);
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
		buttonPane.addButton(allShipsButton);

		GUITextButton rpShipsButton = new GUITextButton(getState(), 105, 30, GUITextButton.ColorPalette.OK, "RP SHIPS", new GUICallback() {
			@Override
			public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
				if(mouseEvent.pressedLeftMouse()) contentList.setCurrentFilter(FilterType.SHIPS_RP);
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
		buttonPane.addButton(rpShipsButton);

		GUITextButton pvpShipsButton = new GUITextButton(getState(), 105, 30, GUITextButton.ColorPalette.OK, "PVP SHIPS", new GUICallback() {
			@Override
			public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
				if(mouseEvent.pressedLeftMouse()) contentList.setCurrentFilter(FilterType.SHIPS_PVP);
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
		buttonPane.addButton(pvpShipsButton);

		GUITextButton shipShellsButton = new GUITextButton(getState(), 105, 30, GUITextButton.ColorPalette.OK, "SHIP SHELLS", new GUICallback() {
			@Override
			public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
				if(mouseEvent.pressedLeftMouse()) contentList.setCurrentFilter(FilterType.SHIPS_SHELLS);
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
		buttonPane.addButton(shipShellsButton);

		GUITextButton allStationsButton = new GUITextButton(getState(), 105, 30, GUITextButton.ColorPalette.OK, "STATIONS", new GUICallback() {
			@Override
			public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
				if(mouseEvent.pressedLeftMouse()) contentList.setCurrentFilter(FilterType.STATIONS_ALL);
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
		buttonPane.addButton(allStationsButton);

		GUITextButton rpStationsButton = new GUITextButton(getState(), 105, 30, GUITextButton.ColorPalette.OK, "RP STATIONS", new GUICallback() {
			@Override
			public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
				if(mouseEvent.pressedLeftMouse()) contentList.setCurrentFilter(FilterType.STATIONS_RP);
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
		buttonPane.addButton(rpStationsButton);

		GUITextButton pvpStationsButton = new GUITextButton(getState(), 105, 30, GUITextButton.ColorPalette.OK, "PVP STATIONS", new GUICallback() {
			@Override
			public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
				if(mouseEvent.pressedLeftMouse()) contentList.setCurrentFilter(FilterType.STATIONS_PVP);
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
		buttonPane.addButton(pvpStationsButton);

		GUITextButton stationsShellsButton = new GUITextButton(getState(), 105, 30, GUITextButton.ColorPalette.OK, "STATION SHELLS", new GUICallback() {
			@Override
			public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
				if(mouseEvent.pressedLeftMouse()) contentList.setCurrentFilter(FilterType.STATIONS_SHELLS);
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
		buttonPane.addButton(stationsShellsButton);

		GUITextButton logicButton = new GUITextButton(getState(), 105, 30, GUITextButton.ColorPalette.OK, "LOGIC", new GUICallback() {
			@Override
			public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
				if(mouseEvent.pressedLeftMouse()) contentList.setCurrentFilter(FilterType.LOGIC);
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
		buttonPane.addButton(logicButton);

		GUITextButton turretsButton = new GUITextButton(getState(), 105, 30, GUITextButton.ColorPalette.OK, "TURRETS", new GUICallback() {
			@Override
			public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
				if(mouseEvent.pressedLeftMouse()) contentList.setCurrentFilter(FilterType.TURRETS);
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
		buttonPane.addButton(turretsButton);

		GUITextButton templatesButton = new GUITextButton(getState(), 105, 30, GUITextButton.ColorPalette.OK, "TEMPLATES", new GUICallback() {
			@Override
			public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
				if(mouseEvent.pressedLeftMouse()) contentList.setCurrentFilter(FilterType.TEMPLATES);
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
		buttonPane.addButton(templatesButton);

		buttonPane.setPos(0, 8, 0);
		browseTab.getContent(0, 0).attach(buttonPane);
		browseTab.getContent(1, 0).attach(contentList);
		toCleanUp.add(contentList);
		toCleanUp.add(buttonPane);
		toCleanUp.add(browseTab);
	}

	private boolean inputsEmpty() {
		return !blueprintChooserDialog.isActive() && !templateChooserDialog.isActive();
	}

	private void toggleBlueprintFileBrowser() {
		blueprintChooserDialog.deactivate();
		blueprintChooserDialog = new FileChooserDialog(getState(), new FileChooserStats(getState(), getStarMadeFolder(), "", new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory() || file.getName().toLowerCase(Locale.ENGLISH).endsWith(".sment");
			}

			@Override
			public String getDescription() {
				return "StarMade Blueprint (.sment)";
			}
		}) {

			@Override
			public void onSelectedFile(File dir, String file) {
				File f = new File(dir, file);
				ArrayList<String> blueprintNames = new ArrayList<>();
				for(BlueprintEntry BP : BluePrintController.active.readBluePrints()) blueprintNames.add(BP.getName());
				if(f.exists()) {
					String bpName = f.getName().substring(0, f.getName().indexOf(".sment"));
					if(!(blueprintNames.contains(bpName))) {
						try {
							System.err.println("IMPORTING BLUEPRINT " + f.getCanonicalPath());
							BluePrintController.active.importFile(f, null);
							//if(SteamAPIHandler.initialized) Starter.apiHandler.addBlueprint(bpName);
							blueprintList.updated = false;
							blueprintList.updateBlueprints();
							BluePrintController.active.readBluePrints();
							//getState().getController().queueUIAudio("0022_menu_ui - enter");
						} catch(Exception e) { //Maybe this will fix the import string error nonsense?
							e.printStackTrace();
						}
					} else if(f.exists() && blueprintNames.contains(bpName)) {
						PlayerOkCancelInput playerInput = new PlayerOkCancelInput("CONFIRM", getState(), 500, 300, Lng.str("Error"), "A Blueprint with the name " + bpName + " already exists in your catalog!") {
							@Override
							public void onDeactivate() { }

							@Override
							public void pressedOK() {
								//getState().getController().queueUIAudio("0022_menu_ui - error 1");
								deactivate();
							}
						};
						playerInput.getInputPanel().onInit();
						playerInput.getInputPanel().setCancelButton(false);
						playerInput.getInputPanel().background.setPos(470.0F, 35.0F, 0.0F);
						playerInput.getInputPanel().background.setWidth((GLFrame.getWidth() - 435));
						playerInput.getInputPanel().background.setHeight((GLFrame.getHeight() - 70));
						playerInput.activate();
					} else {
						PlayerOkCancelInput playerInput = new PlayerOkCancelInput("CONFIRM", getState(), 500, 300, Lng.str("Error"), Lng.str("The file %s doesn't exist", bpName)) {
							@Override
							public void onDeactivate() { }

							@Override
							public void pressedOK() {
								//getState().getController().queueUIAudio("0022_menu_ui - error 2");
								deactivate();
							}
						};
						playerInput.getInputPanel().onInit();
						playerInput.getInputPanel().setCancelButton(false);
						playerInput.getInputPanel().background.setPos(470.0F, 35.0F, 0.0F);
						playerInput.getInputPanel().background.setWidth((GLFrame.getWidth() - 435));
						playerInput.getInputPanel().background.setHeight((GLFrame.getHeight() - 70));
						playerInput.activate();
					}
				}
			}
		});
		blueprintChooserDialog.activate();
	}

	private void toggleTemplateFileBrowser() {
		templateChooserDialog.deactivate();
		templateChooserDialog = new FileChooserDialog(getState(), new FileChooserStats(getState(), getStarMadeFolder(), "", new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory() || file.getName().toLowerCase(Locale.ENGLISH).endsWith(".smtpl");
			}

			@Override
			public String getDescription() {
				return "StarMade Template (.smtpl)";
			}
		}) {

			@Override
			public void onSelectedFile(File dir, String file) {
				File f = new File(dir, file);
				ArrayList<String> templateNames = new ArrayList<>();
				File templatesFolder = new File("./templates");
				if(!templatesFolder.exists()) templatesFolder.mkdirs();
				if(templatesFolder.listFiles() != null) {
					for(File templateFile : templatesFolder.listFiles()) {
						templateNames.add(templateFile.getName().substring(0, templateFile.getName().indexOf(".smtpl") - 1));
					}
				}
				if(f.exists()) {
					String templateName = f.getName().substring(0, f.getName().indexOf(".smtpl"));
					if(!(templateNames.contains(templateName))) {
						try {
							System.err.println("IMPORTING TEMPLATE " + f.getCanonicalPath());
							FileUtils.copyFile(f, new File(templatesFolder.getAbsolutePath() + "/" + f.getName()));
							//if(SteamAPIHandler.initialized) Starter.apiHandler.addTemplate(f.getName());
							templateList.updated = false;
							templateList.updateTemplates();
							//getState().getController().queueUIAudio("0022_menu_ui - enter");
						} catch(IOException e) {
							e.printStackTrace();
						}
					} else if(f.exists() && templateNames.contains(templateName)) {
						PlayerOkCancelInput playerInput = new PlayerOkCancelInput("CONFIRM", getState(), 500, 300, Lng.str("Error"), "A Template with the name " + templateName + " already exists in your catalog!") {
							@Override
							public void onDeactivate() { }

							@Override
							public void pressedOK() {
								//getState().getController().queueUIAudio("0022_menu_ui - error 1");
								deactivate();
							}
						};
						playerInput.getInputPanel().onInit();
						playerInput.getInputPanel().setCancelButton(false);
						playerInput.getInputPanel().background.setPos(470.0F, 35.0F, 0.0F);
						playerInput.getInputPanel().background.setWidth((GLFrame.getWidth() - 435));
						playerInput.getInputPanel().background.setHeight((GLFrame.getHeight() - 70));
						playerInput.activate();
					} else {
						PlayerOkCancelInput playerInput = new PlayerOkCancelInput("CONFIRM", getState(), 500, 300, Lng.str("Error"), Lng.str("The file %s doesn't exist", templateName)) {
							@Override
							public void onDeactivate() { }

							@Override
							public void pressedOK() {
								//getState().getController().queueUIAudio("0022_menu_ui - error 2");
								deactivate();
							}
						};
						playerInput.getInputPanel().onInit();
						playerInput.getInputPanel().setCancelButton(false);
						playerInput.getInputPanel().background.setPos(470.0F, 35.0F, 0.0F);
						playerInput.getInputPanel().background.setWidth((GLFrame.getWidth() - 435));
						playerInput.getInputPanel().background.setHeight((GLFrame.getHeight() - 70));
						playerInput.activate();
					}
				}
			}
		});
		templateChooserDialog.activate();
	}

	private File getStarMadeFolder() {
		return new File("./");
	}
}
