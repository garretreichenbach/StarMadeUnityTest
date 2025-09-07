package org.schema.game.client.view.gui.reactor;

import api.utils.gui.SimplePlayerTextInput;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.StringTools;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.client.view.mainmenu.gui.effectconfig.GUIEffectConfigTestList;
import org.schema.game.common.controller.elements.ManagerModuleSingle;
import org.schema.game.common.controller.elements.power.reactor.PowerInterface;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberCollectionManager;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberElementManager;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberPreset;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberUnit;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorElement;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorSet;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorTree;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.blockeffects.config.ConfigManagerInterface;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonColor;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import java.util.*;

public class GUIReactorPanel extends GUIElement implements GUIActiveInterface, Observer, GUIReactorManagerInterface {

	public GUIMainWindow mainPanel;

	private DialogInput diag;

	private List<GUIElement> toCleanUp = new ObjectArrayList<GUIElement>();

	private boolean init;

	private ManagedSegmentController<?> sm;

	private int selectedTree;

	private int treeContentIndex;

	private GUIContentPane treePane;

	private GUIReactorTree tree;

	public GUIReactorNodeOptions opt;

	private GUIReactorTabs reactorTabs;

	private static ElementInformation selectedTreeTab;

	public GUIReactorPanel(InputState state, ManagedSegmentController<?> sm, DialogInput diag) {
		super(state);
		this.diag = diag;
		this.sm = sm;
		List<ReactorTree> trees = sm.getManagerContainer().getPowerInterface().getReactorSet().getTrees();
		for(int i = 0; i < trees.size(); i++) {
			if(sm.getManagerContainer().getPowerInterface().isActiveReactor(trees.get(i))) {
				selectedTree = i;
				break;
			}
		}
		if(selectedTreeTab == null) {
			for(short s : ElementKeyMap.typeList()) {
				if(ElementKeyMap.getInfoFast(s).isReactorChamberGeneral()) {
					selectedTreeTab = ElementKeyMap.getInfoFast(s);
					break;
				}
			}
		}
		sm.getManagerContainer().getPowerInterface().getPowerConsumerPriorityQueue().addObserver(this);
	}

	@Override
	public void cleanUp() {
		for(GUIElement e : toCleanUp) {
			e.cleanUp();
		}
		toCleanUp.clear();
		sm.getManagerContainer().getPowerInterface().getPowerConsumerPriorityQueue().deleteObserver(this);
	}

	@Override
	public void draw() {
		if(!init) {
			onInit();
		}
		if(getTreeCount() == 0) {
			System.err.println("[CLIENT] Error: No Tree to draw (should not be able to open this panel)");
			return;
		}
		GlUtil.glPushMatrix();
		transform();
		mainPanel.draw();
		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		if(init) {
			return;
		}
		mainPanel = new GUIMainWindow(getState(), GLFrame.getWidth() - 410, GLFrame.getHeight() - 20, 400, 10, "REACTOR_PANEL");
		mainPanel.onInit();
		mainPanel.clearTabs();
		createTreeTab();
		createPresetsTab();
		createPriorityTab();
		createFunctionalityEffectsTab();
		createActiveEffectsTab();
		createDisabledTab();
		createConsumersTab();
		mainPanel.activeInterface = this;
		mainPanel.setCloseCallback(new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(669);
					diag.deactivate();
				}
			}
		});
		init = true;
	}

	private GUIContentPane createActiveEffectsTab() {
		final GUIContentPane t;
		t = mainPanel.addTab(Lng.str("ACTIVE EFFECTS"));
		t.setTextBoxHeightLast(UIScale.getUIScale().scale(48));
		GUIEffectConfigTestList active = new GUIEffectConfigTestList(getState(), t.getContent(0), this, ((ConfigManagerInterface) sm).getConfigManager());
		active.onInit();
		t.getContent(0).attach(active);
		return t;
	}

	private GUIContentPane createFunctionalityEffectsTab() {
		final GUIContentPane t;
		t = mainPanel.addTab(Lng.str("FUNCTIONALITY"));
		t.setTextBoxHeightLast(UIScale.getUIScale().scale(48));
		GUIReactorFunctionalityList active = new GUIReactorFunctionalityList(getState(), sm.getManagerContainer(), t.getContent(0));
		active.onInit();
		t.getContent(0).attach(active);
		return t;
	}

	private GUIContentPane createConsumersTab() {
		final GUIContentPane t;
		t = mainPanel.addTab(Lng.str("CONSUMERS"));
		t.setTextBoxHeightLast(UIScale.getUIScale().scale(48));
		GUIReactorPowerConsumerList active = new GUIReactorPowerConsumerList(getState(), sm.getManagerContainer(), t.getContent(0));
		active.onInit();
		t.getContent(0).attach(active);
		return t;
	}

	@Override
	public boolean isInside() {
		return mainPanel.isInside();
	}

	private GUIContentPane createDisabledTab() {
		final GUIContentPane t;
		t = mainPanel.addTab(new Object() {

			@Override
			public String toString() {
				if(!sm.getManagerContainer().getPowerInterface().isAnyDamaged()) {
					int size = 0;
					List<ManagerModuleSingle<ReactorChamberUnit, ReactorChamberCollectionManager, ReactorChamberElementManager>> chambers = sm.getManagerContainer().getPowerInterface().getChambers();
					for(ManagerModuleSingle<ReactorChamberUnit, ReactorChamberCollectionManager, ReactorChamberElementManager> cham : chambers) {
						for(ReactorChamberUnit e : cham.getCollectionManager().getElementCollections()) {
							if(!sm.getManagerContainer().getPowerInterface().isInAnyTree(e)) {
								size++;
							}
						}
					}
					return Lng.str("DISABLED (%s)", size);
				} else {
					return Lng.str("DISABLED (Damaged)");
				}
			}
		});
		t.setTabActivationCallback(new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return false;
			}

			@Override
			public boolean isActive(InputState state) {
				return !sm.getManagerContainer().getPowerInterface().isAnyDamaged();
			}
		});
		t.setTextBoxHeightLast(UIScale.getUIScale().scale(200));
		GUIReactorChamberList disabled = new GUIReactorChamberList(getState(), sm.getManagerContainer(), t.getContent(0));
		disabled.onInit();
		t.getContent(0).attach(disabled);
		return t;
	}

	private GUIContentPane createTreeTab() {
		final GUIContentPane t;
		t = mainPanel.addTab(Lng.str("TREE"));
		t.setTextBoxHeightLast(UIScale.getUIScale().P_SMALL_PANE_HEIGHT);
		createReactorSelect(t, 0);
		t.addNewTextBox(UIScale.getUIScale().scale(52));
		createReactorInfo(t, 1);
		t.addNewTextBox(UIScale.getUIScale().scale(70));
		t.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		createTree(t, 3, selectedTreeTab);
		t.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		createTreeOption(t, 4);
		t.setListDetailMode(1, t.getTextboxes(0).get(3));
		return t;
	}

	private GUIContentPane createPresetsTab() {
		GUIContentPane contentPane = mainPanel.addTab(Lng.str("PRESETS"));
		contentPane.setTextBoxHeightLast(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 3, 1, contentPane.getContent(0));
		buttonPane.onInit();
		buttonPane.addButton(0, 0, Lng.str("NEW CONFIG"), GUIHorizontalArea.HButtonColor.GREEN, new GUICallback() {
			@Override
			public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
				if(mouseEvent.pressedLeftMouse()) {
					new SimplePlayerTextInput("New Configuration", "Enter Configuration Name") {
						@Override
						public boolean onInput(String input) {
							if(!input.trim().isEmpty()) {
								ReactorChamberPreset preset = new ReactorChamberPreset(input.trim(), tree.getTree().getActiveOrUnspecifiedChambers());
								tree.getTree().addPreset(preset);
								onInit();
								return true;
							} else return false;
						}
					}.activate();
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState inputState) {
				return true;
			}

			@Override
			public boolean isActive(InputState inputState) {
				return true;
			}
		});
		buttonPane.addButton(1, 0, Lng.str("SAVE CURRENT CONFIG"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
			@Override
			public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
				if(mouseEvent.pressedLeftMouse()) {
					String name = tree.getTree().getCurrentPresetName();
					tree.getTree().removePreset(name);
					tree.getTree().addPreset(new ReactorChamberPreset(name, tree.getTree().getActiveOrUnspecifiedChambers()));
					onInit();
				}
			}

			@Override
			public boolean isOccluded() {
				return tree.getTree().getCurrentPreset() == null;
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState inputState) {
				return true;
			}

			@Override
			public boolean isActive(InputState inputState) {
				return tree.getTree().getCurrentPreset() != null;
			}
		});
		buttonPane.addButton(2, 0, Lng.str("REMOVE CURRENT CONFIG"), GUIHorizontalArea.HButtonColor.RED, new GUICallback() {
			@Override
			public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
				if(mouseEvent.pressedLeftMouse()) {
					(new PlayerOkCancelInput("Remove_Configuration", getState(), "Remove Configuration", "Are you sure you want to remove this configuration?") {
						@Override
						public void onDeactivate() {

						}

						@Override
						public void pressedOK() {
							tree.getTree().removePreset(tree.getTree().getCurrentPresetName());
							onInit();
						}
					}).activate();
				}
			}

			@Override
			public boolean isOccluded() {
				return tree.getTree().getCurrentPreset() == null;
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState inputState) {
				return true;
			}

			@Override
			public boolean isActive(InputState inputState) {
				return tree.getTree().getCurrentPreset() != null;
			}
		});
		contentPane.getContent(0).attach(buttonPane);

		contentPane.addNewTextBox(UIScale.getUIScale().scale(500));
		GUITilePane<ReactorChamberPreset> tilePane = new GUITilePane<>(getState(), contentPane.getContent(1), 270, 500);
		for(ReactorChamberPreset preset : tree.getTree().getPresets().values()) {
			GUITile tile = tilePane.addButtonTile(preset.getName().toUpperCase(Locale.ENGLISH).trim(), preset.getName(), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
				@Override
				public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
					try {
						String selected = (String) ((GUIElement) guiElement.getParent()).getUserPointer();
						if(mouseEvent.pressedLeftMouse()) tree.getTree().loadPreset(selected);
						else if(mouseEvent.pressedRightMouse()) {
							(new PlayerOkCancelInput("Remove_Configuration", getState(), "Remove Configuration", "Are you sure you want to remove this configuration?") {
								@Override
								public void onDeactivate() {

								}

								@Override
								public void pressedOK() {
									tree.getTree().removePreset(selected);
									onInit();
								}
							}).activate();
						}
						onInit();
					} catch(Exception exception) {
						exception.printStackTrace();
					}
				}

				@Override
				public boolean isOccluded() {
					return false;
				}
			}, new GUIActivationCallback() {
				@Override
				public boolean isVisible(InputState inputState) {
					return true;
				}

				@Override
				public boolean isActive(InputState inputState) {
					return true;
				}
			});
			tile.getContent().setUserPointer(preset.getName());
			tile.setUserPointer(preset.getName());

			GUIElementList elementList = new GUIElementList(getState());
			for(ReactorElement reactorElement : preset.getElements()) {
				GUITextOverlay overlay = new GUITextOverlay(getState());
				overlay.onInit();
				overlay.setTextSimple(" - " + reactorElement.getInfo().getName() + " : " + StringTools.formatPointZero(reactorElement.getChamberCapacity() * 100) + "%");
				overlay.setUserPointer(preset.getName());
				overlay.getPos().x += 10;
				overlay.getPos().y += 50 + (elementList.size() * 10);
				GUIListElement element = new GUIListElement(overlay, getState());
				element.setUserPointer(preset.getName());
				elementList.add(element);
			}

			elementList.onInit();
			tile.getContent().attach(elementList);
			elementList.setUserPointer(preset.getName());
			GUIToolTip toolTip = new GUIToolTip(getState(), "Left Click : Set Current\nRight Click : Remove Configuration", tile);
			tile.attach(toolTip);
			tile.getContent().attach(toolTip);
			elementList.attach(toolTip);
			tile.setUserPointer(preset.getName());
			contentPane.getContent(1).attach(tilePane);
		}

		return contentPane;
	}

	private GUIContentPane createPriorityTab() {
		final GUIContentPane t;
		t = mainPanel.addTab(Lng.str("PRIORITY"));
		t.setTextBoxHeightLast(UIScale.getUIScale().scale(48));
		GUIReactorPriorityList disabled = new GUIReactorPriorityList(getState(), sm.getManagerContainer(), t.getContent(0));
		disabled.onInit();
		t.getContent(0).attach(disabled);
		return t;
	}

	private void createReactorInfo(GUIContentPane t, int i) {
		GUIHorizontalButtonTablePane p = new GUIHorizontalButtonTablePane(getState(), 5, 2, t.getContent(i));
		p.onInit();
		p.addButton(4, 0, new Object() {

			@Override
			public String toString() {
				PowerInterface pw = sm.getManagerContainer().getPowerInterface();
				float cdR = pw.getReactorRebootCooldown();
				if(cdR > 0) {
					return Lng.str("COOLDOWN %s sec", StringTools.formatPointZero(cdR));
				} else {
					return Lng.str("RECALIBRATE HP");
				}
			}
		}, HButtonColor.RED, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(670);
					if(sm.getManagerContainer().getPowerInterface().isAnyDamaged()) {
						sm.getManagerContainer().getPowerInterface().requestRecalibrate();
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
				PowerInterface pw = sm.getManagerContainer().getPowerInterface();
				float cdR = pw.getReactorRebootCooldown();
				return sm.getManagerContainer().getPowerInterface().isAnyDamaged() && cdR <= 0;
			}
		});
		p.addText(0, 0, new Object() {

			@Override
			public String toString() {
				if(tree == null || tree.getTree() == null) {
					return Lng.str("No Reactor");
				} else {
					ReactorTree rTree = tree.getTree();
					return Lng.str("Reactor: %s", rTree.getName());
				}
			}
		}, FontSize.BIG_20, ORIENTATION_LEFT);
		p.setButtonSpacing(0, 0, 4);
		p.addText(0, 1, new Object() {

			@Override
			public String toString() {
				if(tree == null || tree.getTree() == null) {
					return Lng.str("Reactor HP: %s / %s", StringTools.formatSeperated(sm.getManagerContainer().getPowerInterface().getCurrentHp()), StringTools.formatSeperated(sm.getManagerContainer().getPowerInterface().getCurrentMaxHp()));
				} else {
					ReactorTree rTree = tree.getTree();
					return Lng.str("Reactor HP: %s / %s", StringTools.formatSeperated(rTree.getHp()), StringTools.formatSeperated(rTree.getMaxHp()));
				}
			}
		}, FontSize.MEDIUM_15, ORIENTATION_LEFT);
		p.setButtonSpacing(0, 1, 3);
		p.addText(3, 1, new Object() {

			@Override
			public String toString() {
				if(tree == null || tree.getTree() == null) {
					return "";
				} else {
					ReactorTree rTree = tree.getTree();
					return Lng.str("Level: %s (next at %s / %s blocks)", rTree.getLevelReadable(), rTree.getSize(), StringTools.formatSeperated(rTree.getMaxLvlSize()));
				}
			}
		}, FontSize.MEDIUM_15, ORIENTATION_LEFT);
		p.setButtonSpacing(3, 1, 2);
		t.getContent(i).attach(p);
	}

	private void createReactorSelect(GUIContentPane t, int i) {
		GUIHorizontalButtonTablePane p = new GUIHorizontalButtonTablePane(getState(), 5, 1, t.getContent(i));
		p.onInit();
		p.addButton(0, 0, Lng.str("Previous"), HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(671);
					if(selectedTree == 0) {
						selectedTree = getTreeCount() - 1;
					} else {
						selectedTree--;
					}
					createTree(treePane, treeContentIndex, selectedTreeTab);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return getTreeCount() > 1;
			}
		});
		p.addButton(1, 0, new Object() {

			@Override
			public String toString() {
				PowerInterface pw = sm.getManagerContainer().getPowerInterface();
				float cd = pw.getReactorSwitchCooldown();
				String cooldown = "";
				boolean active = getSelectedTree().isActiveTree();
				if(cd > 0f && !active) {
					cooldown = Lng.str("[Cooldown %s sec]", (int) Math.ceil(cd));
				}
				if(active) {
					return Lng.str("%s; Size-Lvl: %s [%s-%s] (chamber size: %s, free capacity: %s%%)", getSelectedTree().getDisplayName(), getSelectedTree().getLevelReadable(), getSelectedTree().getMinLvlSize(), getSelectedTree().getMaxLvlSize(), getSelectedTree().getMinChamberSize(), StringTools.formatPointZero(((1f - getSelectedTree().getChamberCapacity()) * 100f)));
				} else {
					if(pw.getSegmentController().railController.isDockedAndExecuted()) {
						return Lng.str("Diabled when docked");
					} else {
						return Lng.str("Switch to %s %s", getSelectedTree().getDisplayName(), cooldown);
					}
				}
			}
		}, HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(672);
					getSelectedTree().boot();
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				PowerInterface pw = sm.getManagerContainer().getPowerInterface();
				float cd = pw.getReactorSwitchCooldown();
				if(pw.getSegmentController().railController.isDockedAndExecuted()) {
					return false;
				}
				return !getSelectedTree().isActiveTree() && cd <= 0;
			}
		});
		p.setButtonSpacing(1, 0, 3);
		p.addButton(4, 0, Lng.str("Next"), HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(673);
					selectedTree = (selectedTree + 1) % getTreeCount();
					createTree(treePane, treeContentIndex, selectedTreeTab);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return getTreeCount() > 1;
			}
		});
		t.getContent(i).attach(p);
	}

	private void createTree(GUIContentPane pane, int index, ElementInformation selectedTab) {
		treePane = pane;
		treeContentIndex = index;
		if(hasSelectedTree()) {
			ReactorTree reactorTree = getSelectedTree();
			if(tree != null) {
				tree.cleanUp();
				pane.getContent(index).detach(tree);
			}
			tree = new GUIReactorTree((GameClientState) getState(), diag, sm, this, pane.getContent(index), reactorTree);
			tree.onInit();
			pane.getContent(index).attach(tree);
		}
		if(tree != null) {
			createTreeTabs(tree, pane, index - 1);
		}
	}

	private void createTreeTabs(GUIReactorTree tree, GUIContentPane pane, int index) {
		if(reactorTabs == null) {
			reactorTabs = new GUIReactorTabs(getState(), pane.getContent(index), this);
			reactorTabs.setTree(tree);
			pane.getContent(index).attach(reactorTabs);
		} else {
			reactorTabs.setTree(tree);
		}
	}

	private void createTreeOption(GUIContentPane pane, int i) {
		opt = new GUIReactorNodeOptions(getState(), pane.getContent(i), this);
		opt.onInit();
		pane.getContent(i).attach(opt);
	}

	public int getTreeCount() {
		ReactorSet reactorSet = sm.getManagerContainer().getPowerInterface().getReactorSet();
		List<ReactorTree> trees = reactorSet.getTrees();
		return trees.size();
	}

	public boolean hasSelectedTree() {
		return getTreeCount() > 0;
	}

	public ReactorTree getSelectedTree() {
		if(hasSelectedTree()) {
			ReactorSet reactorSet = sm.getManagerContainer().getPowerInterface().getReactorSet();
			List<ReactorTree> trees = reactorSet.getTrees();
			return trees.get(selectedTree % trees.size());
		} else {
			throw new RuntimeException("No Tree: " + selectedTree);
		}
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
		return diag.isActive();
	}

	@Override
	public void update(Timer timer) {
		if(reactorTabs != null) {
			reactorTabs.update(timer);
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		if(arg != null && arg instanceof ReactorTree) {
			createTree(treePane, treeContentIndex, selectedTreeTab);
		}
	}

	@Override
	public void onTreeNotFound(GUIReactorTree guiReactorTree) {
		createTree(treePane, treeContentIndex, selectedTreeTab);
	}

	@Override
	public void setSelectedTab(ElementInformation info) {
		selectedTreeTab = info;
		createTree(treePane, treeContentIndex, selectedTreeTab);
	}

	@Override
	public ElementInformation getSelectedTab() {
		return selectedTreeTab;
	}
}
