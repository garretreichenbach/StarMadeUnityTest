package org.schema.game.client.view.gui.options.newoptions;

import org.schema.common.util.settings.SettingState.SettingStateType;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.mainmenu.MainMenuGUI;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.gui.CustomSkinCreateDialog;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.core.settings.EngineSettingsType;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.ControllerElement.FilterRowStyle;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.settingsnew.GUISettingsElementPanel;
import org.schema.schine.input.InputState;
import org.schema.schine.resource.FileExt;
import org.schema.schine.sound.controller.AudioController;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class OptionsScrollableListNew extends ScrollableTableList<EngineSettings> {

	private EngineSettingsType settingsType;

	private GUIActiveInterface a;

	public OptionsScrollableListNew(InputState state, GUIActiveInterface a, GUIElement p, EngineSettingsType settingsType) {
		super(state, 100, 100, p);
		this.a = a;
		this.settingsType = settingsType;
		this.setColumnsHeight(UIScale.getUIScale().scale(32));
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#cleanUp()
	 */
	@Override
	public void cleanUp() {
		super.cleanUp();
	}

	@Override
	public void initColumns() {
		addFixedWidthColumnScaledUI(Lng.str("#"), 0, (o1, o2) -> o1.ordinal() - o2.ordinal());
		addColumn(Lng.str("Name"), 7, (o1, o2) -> o1.getDescription().compareToIgnoreCase(o2.getDescription()));
		addColumn(Lng.str("Setting"), 3, (o1, o2) -> 0);
		addTextFilter(new GUIListFilterText<EngineSettings>() {

			@Override
			public boolean isOk(String input, EngineSettings listElement) {
				return listElement.getDescription().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, Lng.str("SEARCH BY NAME"), FilterRowStyle.FULL);
	}

	@Override
	protected Collection<EngineSettings> getElementList() {
		return Arrays.asList(EngineSettings.values());
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<EngineSettings> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		int i = 0;
		int cnt = 0;
		for (final EngineSettings f : collection) {
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			final GUIAnchor hlp = new GUIAnchor(getState());
			GUIElement elem;
			if (f == EngineSettings.PLAYER_SKIN_CREATE) {
				elem = new GUIHorizontalButton(getState(), HButtonType.BUTTON_BLUE_MEDIUM, new Object() {

					@Override
					public String toString() {
						return Lng.str("CREATE FROM IMAGES");
					}
				}, new GUICallback() {

					@Override
					public boolean isOccluded() {
						return !a.isActive();
					}

					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if (event.pressedLeftMouse()) {
							// browse
							MainMenuGUI.runningSwingDialog = true;
							SwingUtilities.invokeLater(() -> {
								CustomSkinCreateDialog d = new CustomSkinCreateDialog(null);
								d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
								d.setVisible(true);
								d.addWindowListener(new WindowListener() {

									@Override
									public void windowOpened(WindowEvent e) {
									}

									@Override
									public void windowIconified(WindowEvent e) {
									}

									@Override
									public void windowDeiconified(WindowEvent e) {
									}

									@Override
									public void windowDeactivated(WindowEvent e) {
									}

									@Override
									public void windowClosing(WindowEvent e) {
										MainMenuGUI.runningSwingDialog = false;
									}

									@Override
									public void windowClosed(WindowEvent e) {
										MainMenuGUI.runningSwingDialog = false;
									}

									@Override
									public void windowActivated(WindowEvent e) {
									}
								});
							});
						}
					}
				}, a, new GUIActivationCallback() {

					@Override
					public boolean isVisible(InputState state) {
						return true;
					}

					@Override
					public boolean isActive(InputState state) {
						return a.isActive();
					}
				}) {

					@Override
					public void draw() {
						setWidth(hlp.getWidth());
						super.draw();
					}
				};
				elem.onInit();
			} else if (f == EngineSettings.PLAYER_SKIN) {
				elem = new GUIAnchor(getState()) {

					@Override
					public void draw() {
						setWidth(hlp.getWidth());
						setHeight(getDefaultColumnsHeight());
						super.draw();
					}
				};
				GUIHorizontalButton m = new GUIHorizontalButton(getState(), HButtonType.BUTTON_BLUE_MEDIUM, new Object() {

					@Override
					public String toString() {
						String s = EngineSettings.PLAYER_SKIN.getString().trim();
						if (s.length() > 0) {
							File f = new FileExt(s);
							return Lng.str("%s (CHANGE SKIN)", f.getName());
						}
						return Lng.str("BROWSE");
					}
				}, new GUICallback() {

					@Override
					public boolean isOccluded() {
						return !a.isActive();
					}

					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if (event.pressedLeftMouse()) {
							// browse
							MainMenuGUI.runningSwingDialog = true;
							SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
									JDialog d = new JDialog();
									d.setAlwaysOnTop(true);
									d.setVisible(true);
									JFileChooser fc = new JFileChooser(new FileExt("./")) {

										/**
										 */
										private static final long serialVersionUID = 1L;

										@Override
										protected JDialog createDialog(Component parent) throws HeadlessException {
											JDialog dialog = super.createDialog(parent);
											// config here as needed - just to see a difference
											dialog.setLocationByPlatform(true);
											dialog.setModal(true);
											// might help - can't know because I can't reproduce the problem
											dialog.setAlwaysOnTop(true);
											return dialog;
										}
									};
									FileFilter fileFilter = new FileFilter() {

										@Override
										public boolean accept(File arg0) {
											if (arg0.isDirectory()) {
												return true;
											}
											if (arg0.getName().endsWith(".smskin")) {
												return true;
											}
											return false;
										}

										@Override
										public String getDescription() {
											return ".smskin (StarMade Skin)";
										}
									};
									fc.addChoosableFileFilter(fileFilter);
									fc.setFileFilter(fileFilter);
									fc.setAcceptAllFileFilterUsed(false);
									// Show it.
									int returnVal = fc.showDialog(null, "Select Skin");
									// Process the results.
									if (returnVal == JFileChooser.APPROVE_OPTION) {
										File file = fc.getSelectedFile();
										EngineSettings.PLAYER_SKIN.setString(file.getAbsolutePath());
										try {
											EngineSettings.write();
										} catch (IOException e) {
											e.printStackTrace();
										}
									}
									d.dispose();
									MainMenuGUI.runningSwingDialog = false;
								}
							});
						}
					}
				}, a, new GUIActivationCallback() {

					@Override
					public boolean isVisible(InputState state) {
						return true;
					}

					@Override
					public boolean isActive(InputState state) {
						return a.isActive();
					}
				}) {

					@Override
					public void draw() {
						setWidth(hlp.getWidth() - 20);
						super.draw();
					}
				};
				m.onInit();
				GUIHorizontalButton m2 = new GUIHorizontalButton(getState(), HButtonType.BUTTON_RED_MEDIUM, new Object() {

					@Override
					public String toString() {
						return "X";
					}
				}, new GUICallback() {

					@Override
					public boolean isOccluded() {
						return !a.isActive();
					}

					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if (event.pressedLeftMouse()) {
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
							AudioController.fireAudioEventID(608);
							// browse
							EngineSettings.PLAYER_SKIN.setString("");
							try {
								EngineSettings.write();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}, a, new GUIActivationCallback() {

					@Override
					public boolean isVisible(InputState state) {
						return true;
					}

					@Override
					public boolean isActive(InputState state) {
						String s = EngineSettings.PLAYER_SKIN.getString().trim();
						return a.isActive() && s.length() > 0;
					}
				}) {

					@Override
					public void draw() {
						setWidth(20);
						setPos(hlp.getWidth() - 20, 0, 0);
						super.draw();
					}
				};
				m2.onInit();
				elem.attach(m);
				elem.attach(m2);
			} else if (f.getSettingsType() == SettingStateType.BOOLEAN) {
				elem = new GUIEngineSettingsCheckBox(getState(), a, f);
			} else {
				elem = f.getSettingsForGUI().getGUIElement(getState(), hlp);
//				GUISettingSelector e = new GUISettingSelector(getState(), a, f);
//				e.dependent = hlp;
//				elem = e;
			}
			GUISettingsElementPanel set = new GUISettingsElementPanel(getState(), elem, false, false);
			nameText.setTextSimple(f.getDescription());
			int heightInset = 5;
			nameText.getPos().y = heightInset;
			GUIAnchor invis = new GUIAnchor(getState());
			SettingRow r = new SettingRow(getState(), f, invis, nameText, set);
			r.onInit();
			r.useColumnWidthElements[2] = hlp;
			mainList.addWithoutUpdate(r);
			i++;
		}
		mainList.updateDim();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList#isFiltered(java.lang.Object)
	 */
	@Override
	protected boolean isFiltered(EngineSettings e) {
		return super.isFiltered(e) || !(e.getType() == settingsType && !e.isDebug());
	}

	public boolean isPlayerAdmin() {
		return ((GameClientState) getState()).getPlayer().getNetworkObject().isAdminClient.get();
	}

	public boolean canEdit(CatalogPermission f) {
		return f.ownerUID.toLowerCase(Locale.ENGLISH).equals(((GameClientState) getState()).getPlayer().getName().toLowerCase(Locale.ENGLISH)) || isPlayerAdmin();
	}

	private class SettingRow extends Row {

		public SettingRow(InputState state, EngineSettings f, GUIElement... elements) {
			super(state, f, elements);
			this.highlightSelect = true;
		}
	}
}
