package org.schema.game.client.view.mainmenu.gui.ruleconfig;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.swing.filechooser.FileFilter;

import org.schema.game.client.controller.PlayerDropDownInput;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.client.controller.PlayerTextInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.view.gui.rules.GUITrackingList;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.client.view.mainmenu.FileChooserDialog;
import org.schema.game.client.view.mainmenu.MainMenuGUI;
import org.schema.game.client.view.mainmenu.gui.FileChooserStats;
import org.schema.game.common.controller.rules.RuleSet;
import org.schema.game.common.controller.rules.RuleSetManager;
import org.schema.game.common.controller.rules.rules.Rule;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.DialogInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonColor;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIMainWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUIRuleSetConfigPanel extends GUIElement implements GUIActiveInterface {

	public GUIMainWindow mainPanel;

	private GUIContentPane mainTab;

	private DialogInput diag;

	private List<GUIElement> toCleanUp = new ObjectArrayList<GUIElement>();

	private GUIRuleSetStat stat;

	private boolean init;

	public GUIRuleSetConfigPanel(InputState state, GUIRuleSetStat stat, DialogInput diag) {
		super(state);
		this.diag = diag;
		this.stat = stat;
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
		mainPanel = new GUIMainWindow(getState(), GLFrame.getWidth() - 410, GLFrame.getHeight() - 20, 400, 10, "RuleWindow");
		mainPanel.onInit();
		mainPanel.setPos(435, 35, 0);
		mainPanel.setWidth(GLFrame.getWidth() - 470);
		mainPanel.setHeight(GLFrame.getHeight() - 70);
		mainPanel.clearTabs();
		mainTab = createRuleSetTab();
		if (stat.getGameState() != null) {
			createRuleEntityTab();
			createTrackedEntityTab();
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
					AudioController.fireAudioEventID(815);
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

	GUIEnttityRuleStat global;

	GUIEnttityRuleStat individual;

	private GUIEntitySelectedRuleStat selectedEntityRuleStat;

	private GUIContentPane createRuleEntityTab() {
		GUIContentPane t = mainPanel.addTab(Lng.str("SEL. ENTITY"));
		t.setTextBoxHeightLast(UIScale.getUIScale().scale(48));
		int currentIndex = 0;
		GUIHorizontalButtonTablePane lbl = new GUIHorizontalButtonTablePane(getState(), 1, 2, t.getContent(currentIndex));
		lbl.onInit();
		lbl.addText(0, 0, Lng.str("Global Rules"));
		lbl.addButton(0, 1, Lng.str("Switch Ignore Global Set"), HButtonColor.YELLOW, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !GUIRuleSetConfigPanel.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse() && global.getSelectedRule() != null) {
					if (global.getSelectedRule().ignoreRule) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.DELETE)*/
						AudioController.fireAudioEventID(817);
						global.getEntityContainer().getRuleEntityManager().removeIgnorelRuleSetByRule(global.getSelectedRule());
					} else {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(816);
						global.getEntityContainer().getRuleEntityManager().addIgnorelRuleSetByRule(global.getSelectedRule());
					}
					global.change();
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIRuleSetConfigPanel.this.isActive() && global.selectedRule != null;
			}
		});
		t.getContent(currentIndex).attach(lbl);
		this.selectedEntityRuleStat = new GUIEntitySelectedRuleStat();
		global = new GUIEntityRuleStatGlobal((GameClientState) getState(), this.selectedEntityRuleStat) {

			@Override
			public void setSelectedRule(Rule f) {
				individual.selectedRule = null;
				individual.change();
				super.setSelectedRule(f);
			}
		};
		individual = new GUIEntityRuleStatIndividual((GameClientState) getState(), this.selectedEntityRuleStat) {

			@Override
			public void setSelectedRule(Rule f) {
				global.selectedRule = null;
				global.change();
				super.setSelectedRule(f);
			}
		};
		t.addNewTextBox(UIScale.getUIScale().scale(150));
		currentIndex++;
		GUIRuleList globalList = new GUIRuleList(getState(), t.getContent(currentIndex), this, global);
		globalList.onInit();
		toCleanUp.add(globalList);
		t.getContent(currentIndex).attach(globalList);
		t.addNewTextBox(UIScale.getUIScale().scale(48));
		currentIndex++;
		GUIHorizontalButtonTablePane indlbl = new GUIHorizontalButtonTablePane(getState(), 2, 2, t.getContent(currentIndex));
		indlbl.onInit();
		indlbl.addText(0, 0, Lng.str("Custom Rules"));
		indlbl.addButton(0, 1, Lng.str("Add Rule Set"), HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !GUIRuleSetConfigPanel.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					// drop down of ruleSets
					GUIElement[] conds = new GUIElement[((GameStateInterface) getState()).getGameState().getRuleManager().getRuleSets().size()];
					int i = 0;
					for (RuleSet c : ((GameStateInterface) getState()).getGameState().getRuleManager().getRuleSets()) {
						GUIAnchor w = new GUIAnchor(getState(), UIScale.getUIScale().scale(200), UIScale.getUIScale().h);
						GUITextOverlay t = new GUITextOverlay(FontSize.MEDIUM_15, getState());
						t.setTextSimple(c.uniqueIdentifier);
						t.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
						w.attach(t);
						w.setUserPointer(c);
						conds[i] = w;
						i++;
					}
					PlayerDropDownInput p = new PlayerDropDownInput("RULESELCONDITIONTYPE", getState(), UIScale.getUIScale().scale(400), UIScale.getUIScale().scale(200), Lng.str("Add Rule Set"), UIScale.getUIScale().h, new Object() {

						public String toString() {
							return Lng.str("Select a rule set to add to this entity");
						}
					}, conds) {

						@Override
						public void pressedOK(GUIListElement current) {
							RuleSet cType = (RuleSet) current.getContent().getUserPointer();
							individual.getEntityContainer().getRuleEntityManager().addIndividualRuleSet(cType);
							stat.change();
							deactivate();
						}

						@Override
						public void onDeactivate() {
						}
					};
					p.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(818);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIRuleSetConfigPanel.this.isActive() && individual.getEntityContainer() != null;
			}
		});
		indlbl.addButton(1, 1, Lng.str("Remove Rule Set"), HButtonColor.RED, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !GUIRuleSetConfigPanel.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse() && individual.getSelectedRule() != null) {
					individual.getEntityContainer().getRuleEntityManager().removeIndividualRuleSetByRule(individual.getSelectedRule());
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIRuleSetConfigPanel.this.isActive() && individual.getEntityContainer() != null && individual.selectedRule != null;
			}
		});
		t.getContent(currentIndex).attach(indlbl);
		t.addNewTextBox(UIScale.getUIScale().scale(150));
		currentIndex++;
		GUIRuleList individualList = new GUIRuleList(getState(), t.getContent(currentIndex), this, individual);
		individualList.onInit();
		toCleanUp.add(individualList);
		t.getContent(currentIndex).attach(individualList);
		t.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		currentIndex++;
		GUIHorizontalButtonTablePane lblCond = new GUIHorizontalButtonTablePane(getState(), 1, 1, t.getContent(currentIndex));
		lblCond.onInit();
		lblCond.addText(0, 0, Lng.str("Conditions of selected Rule"));
		t.getContent(currentIndex).attach(lblCond);
		GUIConditionStatusList statusList = new GUIConditionStatusList(getState(), t.getContent(currentIndex), this, selectedEntityRuleStat, selectedEntityRuleStat);
		statusList.onInit();
		toCleanUp.add(statusList);
		t.getContent(currentIndex).attach(statusList);
		return t;
	}

	private GUIContentPane createTrackedEntityTab() {
		GUIContentPane t = mainPanel.addTab(Lng.str("TRACKING"));
		t.setTextBoxHeightLast(UIScale.getUIScale().scale(48));
		int currentIndex = 0;
		GUITrackingList statusList = new GUITrackingList((GameClientState) getState(), t.getContent(currentIndex), this);
		statusList.onInit();
		toCleanUp.add(statusList);
		t.getContent(currentIndex).attach(statusList);
		return t;
	}

	private GUIContentPane createRuleSetTab() {
		GUIContentPane t = mainPanel.addTab(Lng.str("RULESETS"));
		t.setTextBoxHeightLast(UIScale.getUIScale().scale(48));
		addMenuButtonPanel(t, 0);
		t.addNewTextBox(UIScale.getUIScale().scale(48));
		t.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		addEditButtonPanel(t, 1);
		GUIRuleSetList ruleSetList = new GUIRuleSetList(getState(), t.getContent(2), this, stat);
		toCleanUp.add(ruleSetList);
		ruleSetList.onInit();
		t.getContent(2).attach(ruleSetList);
		// 
		// 
		// 
		// t.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		// 
		// GUIEffectConfigElementList elemList = new GUIEffectConfigElementList(getState(), t.getContent(3), this, stat);
		// elemList.onInit();
		// t.getContent(3).attach(elemList);
		return t;
	}

	private void addMenuButtonPanel(GUIContentPane t, int index) {
		GUIHorizontalButtonTablePane bb = new GUIHorizontalButtonTablePane(getState(), 3, 2, t.getContent(index));
		toCleanUp.add(bb);
		bb.onInit();
		bb.addButton(0, 0, Lng.str("Clear All"), HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					(new PlayerOkCancelInput("CONFIRM", getState(), 300, 140, Lng.str("Confirm"), Lng.str("This removes all RuleSets.\nAll unsaved changes will be lost. Proceeed?")) {

						@Override
						public void pressedOK() {
							// stat.configPool = new ConfigPool();
							// stat.selectedElement = null;
							// stat.selectedGroup = null;
							RuleSetManager m = new RuleSetManager();
							stat.manager = m;
							stat.change();
							deactivate();
						}

						@Override
						public void onDeactivate() {
						}
					}).activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(819);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIRuleSetConfigPanel.this.isActive() && stat.manager != null;
			}
		});
		bb.addButton(1, 0, stat.gameState != null ? Lng.str("Send All") : Lng.str("Save"), HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(820);
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
				return GUIRuleSetConfigPanel.this.isActive() && stat.manager != null;
			}
		});
		bb.addButton(2, 0, stat.gameState != null ? Lng.str("Save local") : Lng.str("Save as"), HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					(new FileChooserDialog(getState(), new FileChooserStats(getState(), new File("./"), "", new FileFilter() {

						@Override
						public String getDescription() {
							return "RuleSet XML";
						}

						@Override
						public boolean accept(File f) {
							return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".xml");
						}
					}) {

						@Override
						public void onSelectedFile(File dir, String file) {
							final File f = new File(dir, file);
							if (!f.exists()) {
								try {
									System.err.println("SAVING AS FILE " + f.getCanonicalPath());
								} catch (IOException e) {
									e.printStackTrace();
								}
								stat.saveAs(getState(), f);
							} else {
								PlayerOkCancelInput c = new PlayerOkCancelInput("CONFIRM", getState(), 300, 150, Lng.str("Error"), Lng.str("The file %s already exists. Overwrite?", f.getName())) {

									@Override
									public void pressedOK() {
										stat.saveAs(getState(), f);
										deactivate();
									}

									@Override
									public void onDeactivate() {
									}
								};
								c.getInputPanel().onInit();
								c.activate();
								/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
								AudioController.fireAudioEventID(821);
							}
						}
					})).activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(822);
				// (new PlayerTextInput("INININSKS", getState(), 256, Lng.str("Save As..."), Lng.str("Enter path:"), stat.getLoadedPath()) {
				// 
				// @Override
				// public void onFailedTextCheck(String msg) {
				// 
				// }
				// 
				// @Override
				// public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
				// return null;
				// }
				// 
				// @Override
				// public String[] getCommandPrefixes() {
				// //							return null;
				// }
				// 
				// @Override
				// public boolean onInput(String entry) {
				// stat.save(getState(), entry);
				// return true;
				// }
				// 
				// @Override
				// public void onDeactivate() {
				// }
				// }).activate(); AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIRuleSetConfigPanel.this.isActive() && stat.manager != null;
			}
		});
		bb.addButton(0, 1, Lng.str("Load"), HButtonColor.YELLOW, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIRuleSetConfigPanel.this.isActive();
			}
		});
		bb.addButton(1, 1, Lng.str("Import & Merge"), HButtonColor.YELLOW, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					(new FileChooserDialog(getState(), new FileChooserStats(getState(), new File("./"), "", new FileFilter() {

						@Override
						public String getDescription() {
							return "RuleSet XML";
						}

						@Override
						public boolean accept(File f) {
							return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".xml");
						}
					}) {

						@Override
						public void onSelectedFile(File dir, String file) {
							final File f = new File(dir, file);
							if (f.exists()) {
								try {
									System.err.println("IMPORTING FILE " + f.getCanonicalPath());
								} catch (IOException e) {
									e.printStackTrace();
								}
								stat.importFile(f);
							} else {
								PlayerOkCancelInput c = new PlayerOkCancelInput("CONFIRM", getState(), 300, 150, Lng.str("Error"), Lng.str("The file %s doesn't exist", f.getName())) {

									@Override
									public void pressedOK() {
										deactivate();
									}

									@Override
									public void onDeactivate() {
									}
								};
								c.getInputPanel().setCancelButton(false);
								c.getInputPanel().onInit();
								c.activate();
								/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
								AudioController.fireAudioEventID(823);
							}
						}
					})).activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(824);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIRuleSetConfigPanel.this.isActive();
			}
		});
		bb.addButton(2, 1, Lng.str("Export All"), HButtonColor.YELLOW, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					(new FileChooserDialog(getState(), new FileChooserStats(getState(), new File("./"), "ruleset-export.xml", new FileFilter() {

						@Override
						public String getDescription() {
							return "RuleSet XML";
						}

						@Override
						public boolean accept(File f) {
							return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".xml");
						}
					}) {

						@Override
						public void onSelectedFile(File dir, String file) {
							final File f = new File(dir, file);
							if (f.exists()) {
								PlayerOkCancelInput c = new PlayerOkCancelInput("CONFIRM", getState(), 300, 150, Lng.str("Warning"), Lng.str("The file %s already exists. Overwrite?", f.getName())) {

									@Override
									public void pressedOK() {
										stat.exportAll(f);
										deactivate();
									}

									@Override
									public void onDeactivate() {
									}
								};
								c.getInputPanel().onInit();
								c.activate();
								/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
								AudioController.fireAudioEventID(825);
							} else {
								stat.exportAll(f);
							}
						}
					})).activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(826);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIRuleSetConfigPanel.this.isActive();
			}
		});
		t.getContent(index).attach(bb);
	}

	private void addEditButtonPanel(GUIContentPane t, int index) {
		GUIHorizontalButtonTablePane bb = new GUIHorizontalButtonTablePane(getState(), 3, 2, t.getContent(index));
		bb.onInit();
		bb.addButton(0, 0, Lng.str("New Rule Set"), HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					PlayerTextInput input = new PlayerTextInput("TXTTSTTTS", getState(), 64, Lng.str("ID"), Lng.str("Enter a name")) {

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
								if (!stat.manager.ruleUIDlkMap.containsKey(entry.trim().toLowerCase(Locale.ENGLISH))) {
									RuleSet s;
									s = new RuleSet();
									s.uniqueIdentifier = entry.trim();
									stat.manager.addRuleSet(s);
									System.err.println("RULESET ADDED: Size: " + stat.manager.getRuleSets().size());
									stat.selectedRuleset = s;
									stat.change();
									return true;
								} else {
									setErrorMessage(Lng.str("Rule Set Name Already Exists"));
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
					AudioController.fireAudioEventID(827);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIRuleSetConfigPanel.this.isActive();
			}
		});
		bb.addButton(1, 0, Lng.str("Duplicate Rule Set"), HButtonColor.PINK, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					if (stat.selectedRuleset != null) {
						PlayerTextInput input = new PlayerTextInput("TXTTSTTTS", getState(), 64, Lng.str("ID"), Lng.str("Enter a name"), stat.selectedRuleset.getUniqueIdentifier()) {

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
									if (stat.selectedRuleset != null) {
										if (!stat.manager.ruleUIDlkMap.containsKey(entry.trim().toLowerCase(Locale.ENGLISH))) {
											RuleSet s;
											try {
												s = new RuleSet(stat.selectedRuleset, entry.trim());
												s.checkUIDRules(stat.manager);
												s.assignNewIds();
												stat.manager.addRuleSet(s);
												stat.selectedRuleset = s;
												stat.change();
												return true;
											} catch (IOException e) {
												e.printStackTrace();
												return false;
											}
										} else {
											setErrorMessage(Lng.str("Rule Set Name Already Exists"));
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
						AudioController.fireAudioEventID(828);
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
				return GUIRuleSetConfigPanel.this.isActive() && stat.selectedRuleset != null;
			}
		});
		bb.addButton(2, 0, Lng.str("Remove Rule Set"), HButtonColor.RED, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					if (stat.selectedRuleset != null) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(829);
						stat.manager.removeRuleSet(stat.selectedRuleset.uniqueIdentifier);
						stat.selectedRuleset = null;
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
				return GUIRuleSetConfigPanel.this.isActive() && stat.selectedRuleset != null;
			}
		});
		bb.addButton(0, 1, Lng.str("Edit Ruleset"), HButtonColor.GREEN, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (stat.selectedRuleset != null && event.pressedLeftMouse()) {
					GUIRuleStat stat = new GUIRuleStat(getState(), GUIRuleSetConfigPanel.this.stat.manager, GUIRuleSetConfigPanel.this.stat, GUIRuleSetConfigPanel.this.stat.selectedRuleset);
					RuleConfigDialog d = new RuleConfigDialog(getState(), stat);
					d.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(830);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIRuleSetConfigPanel.this.isActive() && stat.selectedRuleset != null;
			}
		});
		bb.addButton(1, 1, Lng.str("Rename Rule Set"), HButtonColor.YELLOW, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					if (stat.selectedRuleset != null) {
						PlayerTextInput input = new PlayerTextInput("TXTTSTTTS", getState(), 64, Lng.str("ID"), Lng.str("Enter a rule set name"), stat.selectedRuleset.uniqueIdentifier) {

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
									if (stat.selectedRuleset != null) {
										if (!stat.manager.ruleUIDlkMap.containsKey(entry.trim().toLowerCase(Locale.ENGLISH))) {
											stat.selectedRuleset.uniqueIdentifier = entry.trim();
											stat.change();
											return true;
										} else {
											setErrorMessage(Lng.str("Rule Set Name Already Exists"));
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
						AudioController.fireAudioEventID(831);
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
				return GUIRuleSetConfigPanel.this.isActive() && stat.selectedRuleset != null;
			}
		});
		bb.addButton(2, 1, Lng.str("Export Ruleset"), HButtonColor.RED, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					if (stat.selectedRuleset != null) {
						(new FileChooserDialog(getState(), new FileChooserStats(getState(), new File("./"), "ruleset-" + stat.selectedRuleset.uniqueIdentifier + ".xml", new FileFilter() {

							@Override
							public String getDescription() {
								return "RuleSet XML";
							}

							@Override
							public boolean accept(File f) {
								return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".xml");
							}
						}) {

							@Override
							public void onSelectedFile(File dir, String file) {
								final File f = new File(dir, file);
								if (f.exists()) {
									PlayerOkCancelInput c = new PlayerOkCancelInput("CONFIRM", getState(), 300, 150, Lng.str("Warning"), Lng.str("The file %s already exists. Overwrite?", f.getName())) {

										@Override
										public void pressedOK() {
											stat.exportSelected(f);
											deactivate();
										}

										@Override
										public void onDeactivate() {
										}
									};
									c.getInputPanel().onInit();
									c.activate();
									/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
									AudioController.fireAudioEventID(832);
								} else {
									stat.exportSelected(f);
								}
							}
						})).activate();
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
						AudioController.fireAudioEventID(833);
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
				return GUIRuleSetConfigPanel.this.isActive() && stat.selectedRuleset != null;
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
