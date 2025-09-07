package org.schema.game.client.view.mainmenu.gui.catalogmanager;

import api.mod.ModIdentifier;
import api.mod.ModUpdater;
import api.mod.SMDModInfo;
import api.mod.StarLoader;
import api.smd.SMDEntryData;
import api.smd.SMDEntryExtraData;
import api.smd.SMDEntryUtils;
import api.smd.SMDUtils;
import api.utils.gui.SimplePopup;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.hsqldb.lib.StringComparator;
import org.schema.common.util.CompareTools;
import org.schema.common.util.StringTools;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.util.DesktopUtils;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.controller.ImportFailedException;
import org.schema.game.server.controller.MayImportCallback;
import org.schema.game.server.data.blueprintnw.BlueprintEntry;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

public class SMDContentScrollableList extends ScrollableTableList<SMDEntryData> {

	private List<SMDEntryData> entries;
	public boolean updated;
	private FilterType currentFilter;
	private final GUIElement element;

	public SMDContentScrollableList(InputState state, float width, float height, GUIElement element) {
		super(state, width, height, element);
		this.element = element;
		currentFilter = FilterType.ALL;
		updated = false;
		updateEntries();
	}

	@Override
	public void initColumns() {
		addColumn("Name", 18.0F, Comparator.comparing(o -> o.title.toLowerCase(Locale.ROOT)));

		addColumn("Author", 8.0F, Comparator.comparing(o -> o.username.toLowerCase(Locale.ROOT)));

		addColumn("Rating", 2.5F, (o1, o2) -> CompareTools.compare(o1.rating_average, o2.rating_average));

		addColumn("Downloads", 7.0F, (o1, o2) -> CompareTools.compare(o1.downloadCount, o2.downloadCount));

		addTextFilter(new GUIListFilterText<>() {
			public boolean isOk(String s, SMDEntryData entry) {
				if(entry != null && entry.title != null)
					return entry.title.toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT));
				else return false;
			}
		}, "SEARCH BY NAME", ControllerElement.FilterRowStyle.LEFT);

		addTextFilter(new GUIListFilterText<>() {
			public boolean isOk(String s, SMDEntryData entry) {
				if(entry != null && entry.username != null)
					return entry.username.toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT));
				return false;
			}
		}, "SEARCH BY AUTHOR", ControllerElement.FilterRowStyle.RIGHT);

		activeSortColumnIndex = 0;
	}

	@Override
	protected Collection<SMDEntryData> getElementList() {
		if(!updated) updateEntries();
		return entries;
	}

	public void setCurrentFilter(FilterType currentFilter) {
		this.currentFilter = currentFilter;
		updateEntries();
	}

	public void updateEntries() {
		entries = new ArrayList<>();
		if(currentFilter == FilterType.ALL) {
			entries.addAll(SMDEntryUtils.getAllBlueprints().values());
			entries.addAll(SMDEntryUtils.getAllTemplates().values());
		} else {
			for(SMDEntryData entry : SMDEntryUtils.getAllBlueprints().values()) {
				if(entry.resource_category == currentFilter.num) entries.add(entry);
			}
			for(SMDEntryData entry : SMDEntryUtils.getAllTemplates().values()) {
				if(entry.resource_category == currentFilter.num) entries.add(entry);
			}
		}

		flagDirty();
		updated = true;
	}

	@Override
	public void updateListEntries(GUIElementList list, Set<SMDEntryData> set) {
		if(!updated) updateEntries();
		setColumnsHeight(50);
		for(SMDEntryData entry : set) {
			GUITextOverlayTable nameTextElement;
			(nameTextElement = new GUITextOverlayTable(getState())).setTextSimple(entry.title);
			GUIClippedRow nameRowElement;
			(nameRowElement = new GUIClippedRow(getState())).attach(nameTextElement);
			nameTextElement.autoWrapOn = nameRowElement;
			nameTextElement.autoHeight = true;
			nameTextElement.limitTextWidth = 500;
			nameTextElement.setLimitTextDraw(1);

			GUITextOverlayTable authorTextElement;
			(authorTextElement = new GUITextOverlayTable(getState())).setTextSimple(entry.username);
			GUIClippedRow authorRowElement;
			(authorRowElement = new GUIClippedRow(getState())).attach(authorTextElement);
			authorTextElement.autoWrapOn = authorRowElement;
			authorTextElement.autoHeight = true;
			authorTextElement.setLimitTextDraw(1);

			GUITextOverlayTable ratingTextElement;
			(ratingTextElement = new GUITextOverlayTable(getState())).setTextSimple(StringTools.formatPointZero(entry.rating_average));
			GUIClippedRow ratingRowElement;
			(ratingRowElement = new GUIClippedRow(getState())).attach(ratingTextElement);
			ratingTextElement.autoWrapOn = ratingRowElement;
			ratingTextElement.autoHeight = true;
			ratingTextElement.setLimitTextDraw(1);

			GUITextOverlayTable downloadsTextElement;
			(downloadsTextElement = new GUITextOverlayTable(getState())).setTextSimple(entry.downloadCount + " downloads");
			GUIClippedRow downloadsRowElement;
			(downloadsRowElement = new GUIClippedRow(getState())).attach(downloadsTextElement);
			downloadsTextElement.autoWrapOn = downloadsRowElement;
			downloadsTextElement.autoHeight = true;
			downloadsTextElement.setLimitTextDraw(1);

			SMDContentListRow entryListRow = new SMDContentListRow(getState(), entry, nameRowElement, authorRowElement, ratingRowElement, downloadsRowElement);
			entryListRow.expanded = new GUIElementList(getState());
			GUIAnchor anchor = new GUIAnchor(getState(), element.getWidth() - 28.0f, 28.0f) {
				@Override
				public void draw() {
					setWidth(element.getWidth() - 28.0f);
					super.draw();
				}
			};
			anchor.attach(redrawButtonPane(entry, anchor));
			entryListRow.expanded.add(new GUIListElement(anchor, getState()));
			entryListRow.expanded.attach(anchor);
			entryListRow.onInit();
			list.addWithoutUpdate(entryListRow);
		}
		list.updateDim();
	}

	public GUIHorizontalButtonTablePane redrawButtonPane(SMDEntryData entry, GUIAnchor anchor) {
		GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 2, 1, anchor);
		buttonPane.onInit();

		buttonPane.addButton(0, 0, "DOWNLOAD", GUIHorizontalArea.HButtonColor.GREEN, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent mouseEvent) {
				if(mouseEvent.pressedLeftMouse()) {
					PlayerOkCancelInput confirmBox = new PlayerOkCancelInput("CONFIRM", getState(), 300, 150,"Confirm Download", "Do you wish to download \"" + entry.title + "\"?") {
						@Override
						public void pressedOK() {
							ArrayList<String> names = new ArrayList<>();
							File templatesFolder = new File("templates");
							List<BlueprintEntry> blueprints = BluePrintController.active.readBluePrints();
							for(BlueprintEntry BP : blueprints) names.add(BP.getName());
							if(!templatesFolder.exists()) templatesFolder.mkdirs();
							if(templatesFolder.listFiles() != null) {
								for(File templateFile : Objects.requireNonNull(templatesFolder.listFiles())) {
									names.add(templateFile.getName().substring(0, templateFile.getName().indexOf(".smtpl")));
								}
							}

							try {
								SMDEntryExtraData extraData = entry.getExtraData();
								String downloadURL = extraData.getDownloadURL();
								downloadURL = downloadURL.substring("https://starmadedock.net/api/".length());
								InputStream stream = SMDUtils.GETFromSMD(downloadURL).getInputStream();
								if(FilterType.getContentType(entry.resource_category) == 1) {
									if(extraData.getFileName().toLowerCase(Locale.ENGLISH).endsWith(".sment")) {
										File downloadsFolder = new File("download-cache");
										if (!downloadsFolder.exists()) downloadsFolder.mkdirs();
										File downloadFile = new File(downloadsFolder.getAbsolutePath() + "/" + SMDUtils.sanitizeName(entry.title) + ".sment");
										FileUtils.copyInputStreamToFile(stream, downloadFile);
										boolean[] downloadFlags = new boolean[2];
										BlueprintEntry[] toRemove = new BlueprintEntry[1];
										ArrayList<String> modsNeeded = new ArrayList<>();
										BluePrintController.active.importFile(downloadFile, new MayImportCallback() {
											@Override
											public void callbackOnImportDenied(BlueprintEntry e) {

											}

											@Override
											public boolean mayImport(BlueprintEntry e) {
												if(e.getModMappings() != null && !e.getModMappings().getNamespacedBlocks().isEmpty()) {
													for(String nameSpace : e.getModMappings().getReverseNamespacedBlocks().keySet()) {
														String modName = nameSpace.split("~")[0].replace("\\", "");
														if(StarLoader.getModFromName(modName) == null && !modsNeeded.contains(modName)) modsNeeded.add(modName);
													}

													if(!modsNeeded.isEmpty()) {
														StringBuilder neededModBuilder = new StringBuilder();
														for(int i = 0; i < modsNeeded.size() - 1; i ++) neededModBuilder.append(modsNeeded.get(i)).append(",\n");
														neededModBuilder.append(modsNeeded.get(modsNeeded.size() - 1));

														PlayerOkCancelInput modsNeededBox = new PlayerOkCancelInput("CONFIRM", getState(), 300, 500, "Mods Required", "This blueprint will require the following mods to be downloaded in order to function properly:\n" + neededModBuilder) {
															@Override
															public void onDeactivate() {

															}

															@Override
															public void pressedOK() {
																downloadFlags[0] = true;
																downloadFlags[1] = true;
																deactivate();
															}

															@Override
															public void pressedSecondOption() {
																downloadFlags[0] = false;
																toRemove[0] = e;
																deactivate();
															}
														};
														modsNeededBox.getInputPanel().onInit();
														modsNeededBox.getInputPanel().setCancelButton(true);
														modsNeededBox.getInputPanel().background.setPos(470.0F, 35.0F, 0.0F);
														modsNeededBox.getInputPanel().background.setWidth((GLFrame.getWidth() - 435));
														modsNeededBox.getInputPanel().background.setHeight((GLFrame.getHeight() - 70));
														modsNeededBox.activate();
													}
												}
												return false;
											}

											@Override
											public void onImport(BlueprintEntry e) {

											}
										});

										FileUtils.forceDelete(downloadFile);
										FileUtils.forceDelete(downloadsFolder);
										if(!downloadFlags[0]) {
											new SimplePopup(getState(), Lng.str("Blueprint Download"), Lng.str("Successfully downloaded blueprint."));
											if(toRemove[0] != null) BluePrintController.active.removeBluePrint(toRemove[0]);
										} else {
											new SimplePopup(getState(), Lng.str("Blueprint Download"), Lng.str("Successfully downloaded blueprint."));
											if(downloadFlags[1]) {
												try {
													ArrayList<SMDModInfo> infoList = new ArrayList<>();
													JsonArray mods = SMDUtils.getSMDMods();
													for(JsonElement jsonModElement : mods) {
														JsonObject jsonModObject = jsonModElement.getAsJsonObject();
														SMDModInfo info = SMDModInfo.fromJson(jsonModObject);
														if(modsNeeded.contains(info.getName())) infoList.add(info);
													}
													for(SMDModInfo modInfo : infoList) ModUpdater.downloadAndLoadMod(new ModIdentifier(modInfo.getResourceId(), modInfo.getLatestDownloadVersion()), null);
												} catch(Exception e) { //Probably unnecessary, but I cant actually compile to test this right now, so better to be safe than sorry...
													e.printStackTrace();
												}
											}
										}
										CatalogManagerPanel.updateLists();
										updated = false;
										updateEntries();
									} else {
										new SimplePopup(getState(), "Error", "Error: This is not an .sment file, and so it cannot be downloaded.");
									}
								} else if(FilterType.getContentType(entry.resource_category) == 2) {
									if(extraData.getFileName().toLowerCase(Locale.ENGLISH).endsWith(".smtpl")) {
										File downloadsFolder = new File("download-cache");
										if(!downloadsFolder.exists()) downloadsFolder.mkdirs();
										String title =  SMDUtils.sanitizeName(entry.title);
										File downloadFile = new File(downloadsFolder.getAbsolutePath() + "/" + title + ".smtpl");
										FileUtils.copyInputStreamToFile(stream, downloadFile);
										FileUtils.copyFile(downloadFile, new File(templatesFolder.getAbsolutePath() + "/" + title + ".smtpl"));
										FileUtils.forceDelete(downloadFile);
										FileUtils.forceDelete(downloadsFolder);
										new SimplePopup(getState(), "Successfully downloaded template.");
										CatalogManagerPanel.updateLists();
										updated = false;
										updateEntries();
									} else {
										new SimplePopup(getState(), "Error", "Error: This is not a .smtpl file, and so it cannot be downloaded.");
									}
								} else {
									PlayerOkCancelInput errorBox = new PlayerOkCancelInput("CONFIRM", getState(), 300, 150, "Error", "Could not download \"" + entry.title + "\" due to an unexpected error.") {
										@Override
										public void onDeactivate() {

										}

										@Override
										public void pressedOK() {
											deactivate();
										}
									};
									errorBox.getInputPanel().onInit();
									errorBox.getInputPanel().setCancelButton(false);
									errorBox.getInputPanel().background.setPos(470.0F, 35.0F, 0.0F);
									errorBox.getInputPanel().background.setWidth((GLFrame.getWidth() - 435));
									errorBox.getInputPanel().background.setHeight((GLFrame.getHeight() - 70));
									errorBox.activate();
								}

							} catch(IOException | ImportFailedException e) {
								e.printStackTrace();

								PlayerOkCancelInput errorBox = new PlayerOkCancelInput("CONFIRM", getState(), 300, 150, "Error", "Could not download \"" + entry.title + "\" due to an unexpected error.") {
									@Override
									public void onDeactivate() {

									}

									@Override
									public void pressedOK() {
										deactivate();
									}
								};
								errorBox.getInputPanel().onInit();
								errorBox.getInputPanel().setCancelButton(false);
								errorBox.getInputPanel().background.setPos(470.0F, 35.0F, 0.0F);
								errorBox.getInputPanel().background.setWidth((GLFrame.getWidth() - 435));
								errorBox.getInputPanel().background.setHeight((GLFrame.getHeight() - 70));
								errorBox.activate();
							} catch(NullPointerException e) {
								e.printStackTrace();
								new SimplePopup(getState(), "Error", "Could not download, the file attached is not a .sment or .smtpl");
							}
							updated = false;
							updateEntries();
							deactivate();
						}

						@Override
						public void onDeactivate() {

						}
					};
					confirmBox.getInputPanel().onInit();
					confirmBox.getInputPanel().background.setPos(470.0F, 35.0F, 0.0F);
					confirmBox.getInputPanel().background.setWidth((GLFrame.getWidth() - 435));
					confirmBox.getInputPanel().background.setHeight((GLFrame.getHeight() - 70));
					confirmBox.activate();
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

		buttonPane.addButton(1, 0, "VIEW ON SMD", GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent mouseEvent) {
				if(mouseEvent.pressedLeftMouse()) {
					String url = "http://starmadedock.net/content/" + entry.resource_id + "/";
					try {
						DesktopUtils.browseURL(new URI(url));
					} catch(Exception exception) {
						exception.printStackTrace();
						new SimplePopup(getState(), "Error", "Could not open link \"" + url + "\" due to an unexpected error.");
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

		return buttonPane;
	}

	private boolean inputsEmpty() {
		return getState().getController().getPlayerInputs().isEmpty() || getState().getController().getPlayerInputs().get(getState().getController().getPlayerInputs().size() - 1).getInputPanel() != this;
	}

	public class SMDContentListRow extends ScrollableTableList<SMDEntryData>.Row {
		public SMDContentListRow(InputState inputState, SMDEntryData entry, GUIElement... guiElements) {
			super(inputState, entry, guiElements);
			highlightSelect = true;
			highlightSelectSimple = true;
			setAllwaysOneSelected(true);
		}
	}
}
