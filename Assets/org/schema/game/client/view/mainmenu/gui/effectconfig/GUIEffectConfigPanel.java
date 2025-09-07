package org.schema.game.client.view.mainmenu.gui.effectconfig;

import java.util.List;
import java.util.Locale;

import org.schema.game.client.controller.PlayerDropDownInput;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.client.controller.PlayerTextInput;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.client.view.mainmenu.MainMenuGUI;
import org.schema.game.common.data.blockeffects.config.ConfigGroup;
import org.schema.game.common.data.blockeffects.config.ConfigManagerInterface;
import org.schema.game.common.data.blockeffects.config.ConfigPool;
import org.schema.game.common.data.blockeffects.config.EffectConfigElement;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.DialogInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonColor;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIMainWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUIEffectConfigPanel extends GUIElement implements GUIActiveInterface {

	public GUIMainWindow mainPanel;

	private GUIContentPane mainTab;

	private DialogInput diag;

	private List<GUIElement> toCleanUp = new ObjectArrayList<GUIElement>();

	private GUIEffectStat stat;

	private boolean init;

	private ConfigManagerInterface specifiedMan;

	public GUIEffectConfigPanel(InputState state, ConfigManagerInterface man, GUIEffectStat stat, DialogInput diag) {
		super(state);
		this.diag = diag;
		this.stat = stat;
		this.specifiedMan = man;
	}

	@Override
	public void cleanUp() {
		for (GUIElement e : toCleanUp) {
			e.cleanUp();
		}
		toCleanUp.clear();
	}

	@Override
	public void draw() {
		if (!init) {
			onInit();
		}
		GlUtil.glPushMatrix();
		transform();
		mainPanel.draw();
		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		if (init) {
			return;
		}
		mainPanel = new GUIMainWindow(getState(), GLFrame.getWidth() - 410, GLFrame.getHeight() - 20, 400, 10, "UniversePanelWindow");
		mainPanel.onInit();
		mainPanel.setPos(UIScale.getUIScale().scale(435), UIScale.getUIScale().scale(35), 0);
		mainPanel.setWidth(GLFrame.getWidth() - 470);
		mainPanel.setHeight(GLFrame.getHeight() - 70);
		mainPanel.clearTabs();
		if (specifiedMan != null) {
			createTestTab();
			mainTab = createEffectsTab();
		} else {
			mainTab = createEffectsTab();
			createTestTab();
		}
		mainPanel.activeInterface = this;
		mainPanel.setCloseCallback(new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(751);
					diag.deactivate();
				}
			}
		});
		init = true;
	}

	@Override
	public boolean isInside() {
		return mainPanel.isInside();
	}

	private GUIContentPane createTestTab() {
		final GUIContentPane t;
		if (specifiedMan != null) {
			t = mainPanel.addTab(specifiedMan.toString());
		} else {
			t = mainPanel.addTab(Lng.str("TEST"));
		}
		t.setTextBoxHeightLast(UIScale.getUIScale().scale(280));
		GUIEffectConfigGroupTestList groupList;
		if (specifiedMan != null) {
			groupList = new GUIEffectConfigGroupTestList(getState(), t.getContent(0), this, stat, specifiedMan.getConfigManager(), specifiedMan);
		} else {
			groupList = new GUIEffectConfigGroupTestList(getState(), t.getContent(0), this, stat, stat.testManager, null);
		}
		groupList.onInit();
		t.getContent(0).attach(groupList);
		t.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		final GUIEffectConfigTestList elemList;
		if (specifiedMan != null) {
			elemList = new GUIEffectConfigTestList(getState(), t.getContent(1), this, specifiedMan.getConfigManager());
		} else {
			elemList = new GUIEffectConfigTestList(getState(), t.getContent(1), this, stat.testManager);
		}
		elemList.onInit();
		t.getContent(1).attach(elemList);
		return t;
	}

	private GUIContentPane createEffectsTab() {
		GUIContentPane t = mainPanel.addTab(Lng.str("EFFECTS"));
		t.setTextBoxHeightLast(UIScale.getUIScale().P_SMALL_PANE_HEIGHT);
		addMenuButtonPanel(t, 0);
		t.addNewTextBox(UIScale.getUIScale().scale(53));
		t.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		addEditButtonPanel(t, 1);
		t.setTextBoxHeight(2, UIScale.getUIScale().scale(280));
		GUIEffectConfigGroupList groupList = new GUIEffectConfigGroupList(getState(), t.getContent(2), this, stat);
		groupList.onInit();
		t.getContent(2).attach(groupList);
		t.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		GUIEffectConfigElementList elemList = new GUIEffectConfigElementList(getState(), t.getContent(3), this, stat);
		elemList.onInit();
		t.getContent(3).attach(elemList);
		return t;
	}

	private void addMenuButtonPanel(GUIContentPane t, int index) {
		GUIHorizontalButtonTablePane bb = new GUIHorizontalButtonTablePane(getState(), 4, 1, t.getContent(index));
		bb.onInit();
		bb.addButton(0, 0, Lng.str("New"), HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					(new PlayerOkCancelInput("CONFIRM", getState(), 300, 140, Lng.str("Confirm"), Lng.str("All unsaved changes will be lost. Proceeed?")) {

						@Override
						public void pressedOK() {
							stat.configPool = new ConfigPool();
							stat.selectedElement = null;
							stat.selectedGroup = null;
							stat.change();
							deactivate();
						}

						@Override
						public void onDeactivate() {
						}
					}).activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(752);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIEffectConfigPanel.this.isActive() && stat.configPool != null;
			}
		});
		bb.addButton(1, 0, Lng.str("Save"), HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(753);
					stat.save(getState(), null);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIEffectConfigPanel.this.isActive() && stat.configPool != null;
			}
		});
		bb.addButton(2, 0, Lng.str("Save as"), HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					(new PlayerTextInput("INININSKS", getState(), 256, Lng.str("Save As..."), Lng.str("Enter path:"), stat.getLoadedPath()) {

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
							stat.save(getState(), entry);
							return true;
						}

						@Override
						public void onDeactivate() {
						}
					}).activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(754);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIEffectConfigPanel.this.isActive() && stat.configPool != null;
			}
		});
		bb.addButton(3, 0, Lng.str("Load"), HButtonColor.YELLOW, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					(new PlayerTextInput("INININSKS", getState(), 256, Lng.str("Load..."), Lng.str("Enter path:"), stat.getLoadedPath()) {

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
							stat.load(getState(), entry, true);
							stat.selectedElement = null;
							stat.selectedGroup = null;
							stat.change();
							return true;
						}

						@Override
						public void onDeactivate() {
						}
					}).activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(755);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIEffectConfigPanel.this.isActive();
			}
		});
		t.getContent(index).attach(bb);
	}

	private void addEditButtonPanel(GUIContentPane t, int index) {
		GUIHorizontalButtonTablePane bb = new GUIHorizontalButtonTablePane(getState(), 3, 2, t.getContent(index));
		bb.onInit();
		bb.addButton(0, 0, Lng.str("Add Group"), HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					PlayerTextInput input = new PlayerTextInput("TXTTSTTTS", getState(), 64, Lng.str("ID"), Lng.str("Enter a group ID")) {

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
								ConfigGroup g = new ConfigGroup(entry.toLowerCase(Locale.ENGLISH));
								if (!stat.configPool.poolMapLowerCase.containsKey(g.id)) {
									stat.configPool.add(g);
									stat.selectedGroup = g;
									stat.change();
									return true;
								} else {
									return false;
								}
							} else {
								return false;
							}
						}

						@Override
						public void onDeactivate() {
						}
					};
					input.getInputPanel().onInit();
					input.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(756);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIEffectConfigPanel.this.isActive();
			}
		});
		bb.addButton(1, 0, Lng.str("Duplicate Group"), HButtonColor.PINK, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					if (stat.selectedGroup != null) {
						PlayerTextInput input = new PlayerTextInput("TXTTSTTTS", getState(), 64, Lng.str("ID"), Lng.str("Enter a group ID"), stat.selectedGroup.id) {

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
									if (stat.selectedGroup != null) {
										ConfigGroup g = new ConfigGroup(entry.toLowerCase(Locale.ENGLISH));
										for (EffectConfigElement e : stat.selectedGroup.elements) {
											EffectConfigElement add = new EffectConfigElement(e);
											g.elements.add(add);
										}
										if (!stat.configPool.poolMapLowerCase.containsKey(g.id)) {
											stat.configPool.add(g);
											stat.selectedGroup = g;
											stat.change();
											return true;
										} else {
											return false;
										}
									} else {
										return false;
									}
								} else {
									return false;
								}
							}

							@Override
							public void onDeactivate() {
							}
						};
						input.getInputPanel().onInit();
						input.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(757);
					}
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIEffectConfigPanel.this.isActive() && stat.selectedGroup != null;
			}
		});
		bb.addButton(2, 0, Lng.str("Remove Group"), HButtonColor.RED, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.DELETE)*/
					AudioController.fireAudioEventID(758);
					if (stat.selectedGroup != null) {
						stat.configPool.remove(stat.selectedGroup);
						stat.selectedGroup = null;
						stat.change();
					}
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIEffectConfigPanel.this.isActive() && stat.selectedGroup != null;
			}
		});
		bb.addButton(0, 1, Lng.str("Add Element"), HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (stat.selectedGroup != null && event.pressedLeftMouse()) {
					List<GUIElement> elems = new ObjectArrayList<GUIElement>();
					for (StatusEffectType t : StatusEffectType.values()) {
						GUIAnchor l = new GUIAnchor(getState(), 200, 24);
						GUITextOverlayTable lt = new GUITextOverlayTable(getState());
						lt.setTextSimple(t.getCategory().getName() + " - " + t.getName());
						lt.setPos(5, 5, 0);
						l.attach(lt);
						l.setUserPointer(t);
						elems.add(l);
					}
					PlayerDropDownInput bb = new PlayerDropDownInput("DDDJSKMDKS", getState(), 400, 300, Lng.str("Add Element"), 24, Lng.str("Select Type"), elems) {

						@Override
						public void pressedOK(GUIListElement current) {
							StatusEffectType t = (StatusEffectType) current.getContent().getUserPointer();
							EffectConfigElement elem = new EffectConfigElement();
							elem.init(t);
							stat.selectedGroup.elements.add(elem);
							stat.change();
							deactivate();
						}

						@Override
						public void onDeactivate() {
						}
					};
					bb.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(759);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIEffectConfigPanel.this.isActive() && stat.selectedGroup != null;
			}
		});
		bb.addButton(1, 1, Lng.str("Rename Group"), HButtonColor.YELLOW, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					if (stat.selectedGroup != null) {
						PlayerTextInput input = new PlayerTextInput("TXTTSTTTS", getState(), 64, Lng.str("ID"), Lng.str("Enter a group ID"), stat.selectedGroup.id) {

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
									if (stat.selectedGroup != null) {
										stat.configPool.remove(stat.selectedGroup);
										String old = stat.selectedGroup.id;
										stat.selectedGroup.id = entry.trim().toLowerCase(Locale.ENGLISH);
										stat.configPool.add(stat.selectedGroup);
										System.err.println("CHANGED ID TO " + old + " -> " + stat.selectedGroup.id);
										stat.change();
										return true;
									} else {
										return false;
									}
								} else {
									return false;
								}
							}

							@Override
							public void onDeactivate() {
							}
						};
						input.getInputPanel().onInit();
						input.activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(760);
					}
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIEffectConfigPanel.this.isActive() && stat.selectedGroup != null;
			}
		});
		bb.addButton(2, 1, Lng.str("Remove Element"), HButtonColor.RED, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(761);
					if (stat.selectedGroup != null && stat.selectedGroup.elements.contains(stat.selectedElement)) {
						stat.selectedGroup.elements.remove(stat.selectedElement);
						stat.selectedElement = null;
						stat.change();
					}
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIEffectConfigPanel.this.isActive() && stat.selectedGroup != null && stat.selectedElement != null;
			}
		});
		t.getContent(index).attach(bb);
	}

	@Override
	public float getHeight() {
		return 0;
	}

	@Override
	public float getWidth() {
		return 0;
	}

	@Override
	public boolean isActive() {
		List<DialogInterface> playerInputs = getState().getController().getInputController().getPlayerInputs();
		return !MainMenuGUI.runningSwingDialog && (playerInputs.isEmpty() || playerInputs.get(playerInputs.size() - 1).getInputPanel() == this);
	}
}
