package org.schema.game.client.view.mainmenu.gui;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.schema.game.client.controller.GameMainMenuController;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.client.controller.PlayerTextInput;
import org.schema.game.client.view.mainmenu.LocalUniverse;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterPos;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUICheckBoxTextPair;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContextPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIListFilterText;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;
import org.schema.schine.resource.FileExt;
import org.schema.schine.sound.controller.AudioController;

public class GUILocalUniverseList extends ScrollableTableList<LocalUniverse> {

	private boolean createBackupOnDelete = true;

	public GUILocalUniverseList(InputState state, GUIElement p) {
		super(state, 100, 100, p);
		((GUIObservable) getState()).addObserver(this);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		// messageController.deleteObserver(this);
		((GUIObservable) getState()).deleteObserver(this);
		super.cleanUp();
	}

	@Override
	public void initColumns() {
		addColumn(Lng.str("Universe"), 5f, (o1, o2) -> (o1.name.toLowerCase(Locale.ENGLISH)).compareTo(o2.name.toLowerCase(Locale.ENGLISH)));
		addFixedWidthColumnScaledUI(Lng.str("Last Played"), 120, (o1, o2) -> o1.lastChanged > o2.lastChanged ? -1 : (o1.lastChanged < o2.lastChanged ? 1 : 0), true);
		addTextFilter(new GUIListFilterText<LocalUniverse>() {

			@Override
			public boolean isOk(String input, LocalUniverse listElement) {
				return listElement.name.toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY NAME"), FilterRowStyle.FULL, FilterPos.TOP);
	}

	@Override
	protected Collection<LocalUniverse> getElementList() {
		return LocalUniverse.readUniverses();
	}

	boolean first = true;

	private long newest = 0;

	@Override
	public void updateListEntries(GUIElementList mainList, Set<LocalUniverse> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		final DateFormat dateFormatter;
		dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault());
		int i = 0;
		for (final LocalUniverse f : collection) {
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			GUITextOverlayTable lastChangedText = new GUITextOverlayTable(getState());
			assert (f.name != null);
			nameText.setTextSimple(new Object() {

				@Override
				public String toString() {
					return f.name;
				}
			});
			lastChangedText.setTextSimple(new Object() {

				@Override
				public String toString() {
					String r = dateFormatter.format(new Date(f.lastChanged));
					assert (r != null);
					return r;
				}
			});
			GUIClippedRow nameP = new GUIClippedRow(getState());
			nameP.attach(nameText);
			GUIClippedRow lastChangedP = new GUIClippedRow(getState());
			lastChangedP.attach(lastChangedText);
			nameText.getPos().y = 5;
			lastChangedText.getPos().y = 5;
			LocalUniverseRow r = new LocalUniverseRow(getState(), f, nameP, lastChangedP);
			GUIAnchor c = new GUIAnchor(getState(), 100, 100);
			r.onInit();
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
		first = false;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#isFiltered(java.lang.Object)
	 */
	@Override
	protected boolean isFiltered(LocalUniverse e) {
		return super.isFiltered(e);
	}

	private class LocalUniverseRow extends Row {

		@Override
		protected GUIContextPane createContext() {
			GUIContextPane p = new GUIContextPane(getState(), 180, 25);
			GUIHorizontalButtonTablePane buttons = new GUIHorizontalButtonTablePane(getState(), 1, 3, p);
			buttons.onInit();
			buttons.addButton(0, 0, Lng.str("EXPORT"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						PlayerOkCancelInput backUpExport = new PlayerOkCancelInput("BACKUP", getState(), 300, 150, Lng.str("Export?"), Lng.str("The export/backup will be created in your StarMade installation folder.")) {

							// 
							@Override
							public void pressedOK() {
								((GameMainMenuController) getState()).backupAndDeleteSelected(true, false);
								deactivate();
							}

							@Override
							public void onDeactivate() {
							}
						};
						backUpExport.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(769);
					}
				}

				@Override
				public boolean isOccluded() {
					return false;
				}
			}, new GUIActivationCallback() {

				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return true;
				}
			});
			buttons.addButton(0, 1, Lng.str("RENAME"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						PlayerTextInput p = new PlayerTextInput("RENAME_UNIVERSE", getState(), 32, Lng.str("Rename Universe"), Lng.str("Choose a new name for your universe."), f.name) {

							@Override
							public void onFailedTextCheck(String msg) {
							}

							@Override
							public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
								return null;
							}

							@Override
							public String[] getCommandPrefixes() {
								return null;
							}

							@Override
							public boolean onInput(String entry) {
								if (entry.trim().length() > 0) {
									try {
										FileUtils.moveDirectory(new FileExt(GameServerState.SERVER_DATABASE + f.name), new FileExt(GameServerState.SERVER_DATABASE + entry.trim()));
										f.name = entry.trim();
										return true;
									} catch (IOException e) {
										e.printStackTrace();
										return false;
									}
								}
								return false;
							}

							@Override
							public void onDeactivate() {
							}
						};
						p.setInputChecker((entry, callback) -> {
							File f = new FileExt(entry);
							try {
								// checks if this name is a valid file name
								f.getCanonicalPath();
								return true;
							} catch (IOException e) {
								callback.onFailedTextCheck("Name must be a legal file name");
								return false;
							}
						});
						p.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(770);
					}
				}

				@Override
				public boolean isOccluded() {
					return false;
				}
			}, new GUIActivationCallback() {

				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return true;
				}
			});
			buttons.addButton(0, 2, Lng.str("DELETE"), HButtonType.BUTTON_RED_MEDIUM, new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						PlayerOkCancelInput delCheck = new PlayerOkCancelInput("CONFIRM_DEL", getState(), 300, 150, Lng.str("DELETE?"), Lng.str("Do you really want to delete the universe %s", ((GameMainMenuController) getState()).getSelectedLocalUniverse().name)) {

							@Override
							public void pressedOK() {
								((GameMainMenuController) getState()).deleteSelectedWorld(createBackupOnDelete);
								deactivate();
							}

							@Override
							public void onDeactivate() {
							}
						};
						delCheck.getInputPanel().onInit();
						GUICheckBoxTextPair backup = new GUICheckBoxTextPair(getState(), Lng.str("Create Backup"), 100, 24) {

							@Override
							public boolean isActivated() {
								return createBackupOnDelete;
							}

							@Override
							public void deactivate() {
								createBackupOnDelete = false;
							}

							@Override
							public void activate() {
								createBackupOnDelete = true;
							}
						};
						((GUIDialogWindow) delCheck.getInputPanel().background).getMainContentPane().getContent(0).attach(backup);
						backup.setPos(10, 50, 0);
						delCheck.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(771);
					}
				}

				@Override
				public boolean isOccluded() {
					return false;
				}
			}, new GUIActivationCallback() {

				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return true;
				}
			});
			p.attach(buttons);
			return p;
		}

		public LocalUniverseRow(InputState state, LocalUniverse f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelectSimple = true;
			setAllwaysOneSelected(true);
			this.rightClickSelectsToo = true;
			if (first && f.lastChanged > newest) {
				clickedOnRow();
				newest = f.lastChanged;
			}
		}
	}
}
