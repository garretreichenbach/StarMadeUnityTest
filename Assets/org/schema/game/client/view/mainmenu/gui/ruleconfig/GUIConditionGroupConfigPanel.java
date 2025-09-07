package org.schema.game.client.view.mainmenu.gui.ruleconfig;

import java.io.IOException;
import java.util.List;

import org.schema.game.client.controller.PlayerDropDownInput;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.client.view.mainmenu.MainMenuGUI;
import org.schema.game.common.controller.rules.rules.Rule;
import org.schema.game.common.controller.rules.rules.actions.Action;
import org.schema.game.common.controller.rules.rules.conditions.Condition;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.controller.rules.rules.conditions.seg.ConditionGroup;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationHighlightCallback;
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

public class GUIConditionGroupConfigPanel extends GUIElement implements GUIActiveInterface, RuleListUpdater {

	public GUIMainWindow mainPanel;

	private GUIContentPane mainTab;

	private DialogInput diag;

	private List<GUIElement> toCleanUp = new ObjectArrayList<GUIElement>();

	private GUIRuleStat stat;

	private boolean init;

	private GUIConditionAndActionDetailList dList;

	private GUIContentPane t;

	private final ConditionGroup g;

	private RuleListUpdater lastRSC;

	private int detailIndex;

	public GUIConditionGroupConfigPanel(InputState state, GUIRuleStat stat, ConditionGroup g, DialogInput diag) {
		super(state);
		this.diag = diag;
		this.stat = stat;
		stat.selectedCondition = null;
		stat.selectedAction = null;
		this.g = g;
		lastRSC = stat.rsc;
		stat.rsc = this;
	}

	@Override
	public void cleanUp() {
		for (GUIElement e : toCleanUp) {
			e.cleanUp();
		}
		toCleanUp.clear();
		stat.rsc = lastRSC;
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
					AudioController.fireAudioEventID(795);
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

	private GUIContentPane createRuleSetTab() {
		t = mainPanel.addTab(Lng.str("CONDITIONGROUP"));
		t.setTextBoxHeightLast(UIScale.getUIScale().P_SMALL_PANE_HEIGHT);
		t.addNewTextBox(UIScale.getUIScale().scale(72));
		addEditButtonPanel(t, 1);
		t.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		GUIConditionList elemList = new GUIConditionList(getState(), t.getContent(2), this, stat, g);
		elemList.onInit();
		t.getContent(2).attach(elemList);
		t.addNewTextBox(UIScale.getUIScale().scale(100));
		detailIndex = 3;
		dList = new GUIConditionAndActionDetailList(getState(), t.getContent(detailIndex), this, stat, null);
		dList.onInit();
		t.getContent(detailIndex).attach(dList);
		return t;
	}

	public void updateDetailPanel(Rule selectedRule, Condition<?> selectedCondition, Action<?> selectedAction) {
		t.getContent(detailIndex).detach(dList);
		dList.cleanUp();
		dList = new GUIConditionAndActionDetailList(getState(), t.getContent(detailIndex), this, stat, selectedCondition != null ? selectedCondition : selectedAction);
		dList.onInit();
		t.getContent(detailIndex).attach(dList);
	}

	private void addEditButtonPanel(GUIContentPane t, int index) {
		GUIHorizontalButtonTablePane bb = new GUIHorizontalButtonTablePane(getState(), 3, 2, t.getContent(index));
		bb.onInit();
		bb.addButton(0, 0, Lng.str("Add Condition"), HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			private ConditionTypes selected;

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					int i = 0;
					List<ConditionTypes> v = ConditionTypes.getSortedByName(stat.selectedRule.getEntityType());
					GUIElement[] conds = new GUIElement[v.size()];
					for (ConditionTypes c : v) {
						GUIAnchor w = new GUIAnchor(getState(), UIScale.getUIScale().scale(200), UIScale.getUIScale().h);
						GUITextOverlay t = new GUITextOverlay(FontSize.MEDIUM_15, getState());
						t.setTextSimple(c.getName());
						t.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
						w.attach(t);
						w.setUserPointer(c);
						conds[i] = w;
						i++;
					}
					PlayerDropDownInput p = new PlayerDropDownInput("RULESELCONDITIONTYPE", getState(), UIScale.getUIScale().scale(400), UIScale.getUIScale().scale(200), Lng.str("Add Condition"), UIScale.getUIScale().h, new Object() {

						public String toString() {
							return Lng.str("Select Condition Type") + "\n" + (selected != null ? selected.getDesc() : "");
						}
					}, conds) {

						@Override
						public void pressedOK(GUIListElement current) {
							ConditionTypes cType = (ConditionTypes) current.getContent().getUserPointer();
							Condition<?> c = cType.fac.instantiateCondition();
							g.addCondition(c);
							stat.selectedCondition = c;
							System.err.println("SELECTED CONDITION: " + c);
							stat.selectedAction = null;
							stat.change();
							deactivate();
						}

						@Override
						public void onDeactivate() {
						}

						@Override
						public void onSelectionChanged(GUIListElement element) {
							super.onSelectionChanged(element);
							if (element != null) {
								selected = (ConditionTypes) element.getUserPointer();
							}
						}
					};
					p.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(796);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIConditionGroupConfigPanel.this.isActive() && stat.manager != null && stat.selectedRule != null;
			}
		});
		bb.addButton(1, 0, Lng.str("Dupl. Condition"), HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					if (stat.selectedCondition != null) {
						try {
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
							AudioController.fireAudioEventID(797);
							Condition<?> c = stat.selectedCondition.duplicate();
							stat.selectedRule.addCondition(c);
							stat.selectedCondition = c;
							stat.selectedAction = null;
							stat.change();
						} catch (IOException e) {
							e.printStackTrace();
						}
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
				return GUIConditionGroupConfigPanel.this.isActive() && stat.manager != null && stat.selectedCondition != null;
			}
		});
		bb.addButton(2, 0, Lng.str("Remove Condition"), HButtonColor.RED, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.DELETE)*/
					AudioController.fireAudioEventID(798);
					if (stat.selectedCondition != null) {
						g.removeCondition(stat.selectedCondition);
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
				return GUIConditionGroupConfigPanel.this.isActive() && stat.manager != null && stat.selectedCondition != null;
			}
		});
		bb.addButton(0, 1, Lng.str("All True"), HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(799);
					if (stat.selectedRule != null) {
						g.setAllTrue(!g.isAllTrue());
						stat.change();
					}
				}
			}
		}, new GUIActivationHighlightCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIConditionGroupConfigPanel.this.isActive() && stat.manager != null;
			}

			@Override
			public boolean isHighlighted(InputState state) {
				return g.isAllTrue();
			}
		});
		bb.addButton(1, 1, Lng.str("Either True"), HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					if (stat.selectedRule != null) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(800);
						g.setAllTrue(!g.isAllTrue());
						stat.change();
					}
				}
			}
		}, new GUIActivationHighlightCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIConditionGroupConfigPanel.this.isActive() && stat.manager != null;
			}

			@Override
			public boolean isHighlighted(InputState state) {
				return !g.isAllTrue();
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
