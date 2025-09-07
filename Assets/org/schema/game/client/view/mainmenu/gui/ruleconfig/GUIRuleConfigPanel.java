package org.schema.game.client.view.mainmenu.gui.ruleconfig;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.client.controller.PlayerDropDownInput;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.client.controller.PlayerTextInput;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.client.view.mainmenu.MainMenuGUI;
import org.schema.game.common.controller.rules.rules.Rule;
import org.schema.game.common.controller.rules.rules.actions.Action;
import org.schema.game.common.controller.rules.rules.actions.ActionTypes;
import org.schema.game.common.controller.rules.rules.conditions.Condition;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonColor;
import org.schema.schine.input.InputState;
import org.schema.schine.network.TopLevelType;
import org.schema.schine.sound.controller.AudioController;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GUIRuleConfigPanel extends GUIElement implements GUIActiveInterface, RuleListUpdater {

    public GUIMainWindow mainPanel;

    private GUIContentPane mainTab;

    private DialogInput diag;

    private List<GUIElement> toCleanUp = new ObjectArrayList<GUIElement>();

    private GUIRuleStat stat;

    private boolean init;

    private GUIConditionAndActionDetailList dList;

    private GUIContentPane t;

    private int detailIndex;

    public GUIRuleConfigPanel(InputState state, GUIRuleStat stat, DialogInput diag) {
        super(state);
        this.diag = diag;
        this.stat = stat;
        stat.rsc = this;
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
        mainPanel = new GUIMainWindow(getState(), GLFrame.getWidth() - 410, GLFrame.getHeight() - 20, 400, 10, "RuleWindow") {

            @Override
            protected int getMinHeight() {
                return totalHeight + 160;
            }
        };
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
                    AudioController.fireAudioEventID(801);
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

    private int totalHeight = 0;

    private GUIContentPane createRuleSetTab() {
        totalHeight = 0;
        int i = 0;
        t = mainPanel.addTab(Lng.str("RULES"));
        t.setTextBoxHeightLast(UIScale.getUIScale().P_SMALL_PANE_HEIGHT);
        addMenuButtonPanel(t, i);
        i++;
        t.addNewTextBox(UIScale.getUIScale().scale(150));
        totalHeight += 150;
        GUIRuleList groupList = new GUIRuleList(getState(), t.getContent(i), this, stat);
        groupList.onInit();
        t.getContent(i).attach(groupList);
        i++;
        t.addNewTextBox(UIScale.getUIScale().scale(48));
        totalHeight += 48;
        addConditionButtonPanel(t, i);
        i++;
        t.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
        totalHeight += 200;
        GUIConditionList condList = new GUIConditionList(getState(), t.getContent(i), this, stat, stat);
        condList.onInit();
        t.getContent(i).attach(condList);
        i++;
        t.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
        totalHeight += 24;
        //addActionButtonPanel(t, i);
        i++;
        t.addNewTextBox(UIScale.getUIScale().scale(100));
        totalHeight += 100;
        List<Action<?>> acts = stat.selectedRule != null ? stat.selectedRule.getActions() : new ObjectArrayList<Action<?>>();
        GUIActionList actionList = new GUIActionList(getState(), t.getContent(i), this, stat, stat);
        actionList.onInit();
        t.getContent(i).attach(actionList);
        i++;
        t.addNewTextBox(UIScale.getUIScale().scale(100));
        // rezizable
        totalHeight += 10;
        this.detailIndex = i;
        dList = new GUIConditionAndActionDetailList(getState(), t.getContent(detailIndex), this, stat, null);
        dList.onInit();
        t.getContent(detailIndex).attach(dList);
        i++;
        return t;
    }

    public void updateDetailPanel(Rule selectedRule, Condition<?> selectedCondition, Action<?> selectedAction) {
        t.getContent(detailIndex).detach(dList);
        dList.cleanUp();
        dList = new GUIConditionAndActionDetailList(getState(), t.getContent(detailIndex), this, stat, selectedCondition != null ? selectedCondition : selectedAction);
        dList.onInit();
        t.getContent(detailIndex).attach(dList);
    }

    private TopLevelType selectedToplevel = TopLevelType.SEGMENT_CONTROLLER;

    private void addMenuButtonPanel(GUIContentPane t, int index) {
        GUIHorizontalButtonTablePane bb = new GUIHorizontalButtonTablePane(getState(), 3, 2, t.getContent(index));
        bb.onInit();
        bb.addButton(0, 0, Lng.str("Add Rule"), HButtonColor.BLUE, new GUICallback() {


            @Override
            public boolean isOccluded() {
                return !isActive();
            }

            @Override
            public void callback(GUIElement callingGuiElement, MouseEvent event) {
                if (event.pressedLeftMouse()) {
                    int selectedIndex = 0;
                    List<GUIElement> ruleTypes = new ObjectArrayList<GUIElement>();
                    int cc = 0;
                    for (TopLevelType t : TopLevelType.values()) {
                        if (t.hasRules()) {
                            GUITextOverlay l = new GUITextOverlay(FontSize.MEDIUM_15, getState());
                            l.setTextSimple(t.getName());
                            GUIAnchor c = new GUIAnchor(getState(), 300, 24);
                            c.attach(l);
                            l.setPos(3, 2, 0);
                            ruleTypes.add(c);
                            c.setUserPointer(t);
                            if (t == selectedToplevel) {
                                selectedIndex = cc;
                            }
                            cc++;
                        }
                    }
                    GUIDropDownList list = new GUIDropDownList(getState(), 300, 24, 100, element -> selectedToplevel = (TopLevelType) element.getContent().getUserPointer(), ruleTypes);
                    list.setSelectedIndex(selectedIndex);

                    PlayerTextInput input = new PlayerTextInput("TXTTSTTTS", getState(), 64, Lng.str("ID"), Lng.str("Select Name and Type")) {
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
                                    Rule s;
                                    s = new Rule(true);
                                    s.ruleType = selectedToplevel;

                                    s.setUniqueIdentifier(entry.trim());
                                    stat.ruleSet.add(s);
                                    stat.selectedRule = s;
                                    stat.change();
                                    return true;
                                } else {
                                    setErrorMessage(Lng.str("Rule Name Already Exists"));
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
                    AudioController.fireAudioEventID(802);
                }
            }
        }, new GUIActivationCallback() {

            @Override
            public boolean isVisible(InputState state) {
                return true;
            }

            @Override
            public boolean isActive(InputState state) {
                return GUIRuleConfigPanel.this.isActive() && stat.manager != null;
            }
        });
        bb.addButton(1, 0, Lng.str("Remove Rule"), HButtonColor.RED, new GUICallback() {
            @Override
            public boolean isOccluded() {
                return !isActive();
            }

            @Override
            public void callback(GUIElement callingGuiElement, MouseEvent event) {
                if (event.pressedLeftMouse()) {
                    if (stat.selectedRule != null) {
                        PlayerOkCancelInput c = new PlayerOkCancelInput("CONFIRM", getState(), 300, 150, Lng.str("Confirm"), Lng.str("Confirm Remove Rule?")) {

                            @Override
                            public void pressedOK() {
                                stat.ruleSet.remove(stat.selectedRule);
                                stat.setSelectedRule(null);
                                stat.setSelectedAction(null);
                                stat.setSelectedCondition(null);
                                stat.change();
                                deactivate();
                            }

                            @Override
                            public void onDeactivate() {
                            }
                        };
                        c.getInputPanel().onInit();
                        c.activate();
                        /*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
                        AudioController.fireAudioEventID(807);
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
                return GUIRuleConfigPanel.this.isActive() && stat.manager != null && stat.selectedRule != null;
            }
        });
        t.getContent(index).attach(bb);
    }

    private void addConditionButtonPanel(GUIContentPane t, int index) {
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
                    List<ConditionTypes> v = ConditionTypes.getSortedByName(stat.selectedRule.ruleType);
                    GUIElement[] conds = new GUIElement[v.size()];
                    for (ConditionTypes c : v) {
                        if (c.getType() == stat.selectedRule.ruleType) {
                            GUIAnchor w = new GUIAnchor(getState(), 200, 24);
                            GUITextOverlay t = new GUITextOverlay(FontSize.MEDIUM_15, getState());
                            t.setTextSimple(c.getName());
                            t.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
                            w.attach(t);
                            w.setUserPointer(c);
                            conds[i] = w;
                            i++;
                       }
                        PlayerDropDownInput p = new PlayerDropDownInput("RULESELCONDITIONTYPE", getState(), 400, 200, Lng.str("Add Condition for rule type %s", stat.selectedRule.ruleType.getName()), 24, new Object() {
                            public String toString() {
                                return Lng.str("Select Condition Type") + "\n" + (selected != null ? selected.getDesc() : "");
                            }
                        }, conds) {

                            @Override
                            public void pressedOK(GUIListElement current) {
                                ConditionTypes cType = (ConditionTypes) current.getContent().getUserPointer();
                                Condition<?> c = cType.fac.instantiateCondition();
                                stat.selectedRule.addCondition(c);
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
                        AudioController.fireAudioEventID(808);
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
                return GUIRuleConfigPanel.this.isActive() && stat.manager != null && stat.selectedRule != null;
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
                    if (stat.selectedRule != null && stat.selectedCondition != null) {
                        try {
                            Condition<?> c = stat.selectedCondition.duplicate();
                            stat.selectedRule.addCondition(c);
                            stat.selectedCondition = c;
                            stat.selectedAction = null;
                            stat.change();
                            /*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
                            AudioController.fireAudioEventID(809);
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
                return GUIRuleConfigPanel.this.isActive() && stat.manager != null && stat.selectedRule != null && stat.selectedCondition != null;
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
                    if (stat.selectedRule != null && stat.selectedCondition != null) {
                        PlayerOkCancelInput c = new PlayerOkCancelInput("CONFIRM", getState(), 300, 150, Lng.str("Confirm"), Lng.str("Confirm Remove Condition?")) {

                            @Override
                            public void pressedOK() {
                                stat.selectedRule.removeCondition(stat.selectedCondition);
                                stat.change();
                                deactivate();
                            }

                            @Override
                            public void onDeactivate() {
                            }
                        };
                        c.getInputPanel().onInit();
                        c.activate();
                        /*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
                        AudioController.fireAudioEventID(810);
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
                return GUIRuleConfigPanel.this.isActive() && stat.manager != null && stat.selectedRule != null && stat.selectedCondition != null;
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
                    if (stat.selectedRule != null) {
                        stat.selectedRule.allTrue = !stat.selectedRule.allTrue;
                        stat.change();
                        /*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
                        AudioController.fireAudioEventID(811);
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
                return GUIRuleConfigPanel.this.isActive() && stat.manager != null && stat.selectedRule != null;
            }

            @Override
            public boolean isHighlighted(InputState state) {
                return stat.selectedRule != null && stat.selectedRule.allTrue;
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
                        stat.selectedRule.allTrue = !stat.selectedRule.allTrue;
                        stat.change();
                        /*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
                        AudioController.fireAudioEventID(812);
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
                return GUIRuleConfigPanel.this.isActive() && stat.manager != null && stat.selectedRule != null;
            }

            @Override
            public boolean isHighlighted(InputState state) {
                return stat.selectedRule != null && !stat.selectedRule.allTrue;
            }
        });
        t.getContent(index).attach(bb);
    }

    private void addActionButtonPanel(GUIContentPane t, int index) {
        GUIHorizontalButtonTablePane bb = new GUIHorizontalButtonTablePane(getState(), 3, 1, t.getContent(index));
        bb.onInit();
        bb.addButton(0, 0, Lng.str("Add Action"), HButtonColor.BLUE, new GUICallback() {

            @Override
            public boolean isOccluded() {
                return !isActive();
            }

            private ActionTypes selected;

            @Override
            public void callback(GUIElement callingGuiElement, MouseEvent event) {
                if (event.pressedLeftMouse()) {
                    int size = 0;
                    for (ActionTypes c : ActionTypes.values()) {
                        if (c.getType() == stat.selectedRule.ruleType) {
                            size++;
                        }
                    }
                    GUIElement[] conds = new GUIElement[size];
                    int i = 0;
                    for (ActionTypes c : ActionTypes.values()) {
                        if (c.getType() == stat.selectedRule.ruleType) {
                            GUIAnchor w = new GUIAnchor(getState(), UIScale.getUIScale().scale(200), UIScale.getUIScale().h);
                            GUITextOverlay t = new GUITextOverlay(FontSize.MEDIUM_15, getState());
                            t.setTextSimple(c.getName());
                            t.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
                            w.attach(t);
                            w.setUserPointer(c);
                            conds[i] = w;
                            i++;
                        }
                    }

                    PlayerDropDownInput p = new PlayerDropDownInput("RULESELACTIONTYPE", getState(), 400, 200, Lng.str("Add Action"), 24, new Object() {
                        public String toString() {
                            return Lng.str("Select Action Type") + "\n" + (selected != null ? selected.getDesc() : "");
                        }
                    }, conds) {

                        @Override
                        public void pressedOK(GUIListElement current) {
                            ActionTypes cType = (ActionTypes) current.getContent().getUserPointer();
                            Action<?> c = cType.fac.instantiateAction();
                            stat.selectedRule.addAction(c);
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
                                selected = (ActionTypes) element.getUserPointer();
                            }
                        }
                    };
                    p.activate();
                    /*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
                    AudioController.fireAudioEventID(813);
                }
            }
        }, new GUIActivationCallback() {

            @Override
            public boolean isVisible(InputState state) {
                return true;
            }

            @Override
            public boolean isActive(InputState state) {
                return GUIRuleConfigPanel.this.isActive() && stat.selectedRule != null;
            }
        });
        bb.addButton(1, 0, Lng.str("Dupl. Action"), HButtonColor.BLUE, new GUICallback() {

            @Override
            public boolean isOccluded() {
                return !isActive();
            }

            @Override
            public void callback(GUIElement callingGuiElement, MouseEvent event) {
                if (event.pressedLeftMouse()) {
                    if (stat.selectedRule != null && stat.selectedAction != null) {
                        try {
                            Action<?> c = stat.selectedAction.duplicate();
                            stat.selectedRule.addAction(c);
                            stat.selectedAction = c;
                            stat.selectedCondition = null;
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
                return GUIRuleConfigPanel.this.isActive() && stat.manager != null && stat.selectedRule != null && stat.selectedCondition != null;
            }
        });
        bb.addButton(2, 0, Lng.str("Remove Action"), HButtonColor.RED, new GUICallback() {

            @Override
            public boolean isOccluded() {
                return !isActive();
            }

            @Override
            public void callback(GUIElement callingGuiElement, MouseEvent event) {
                if (event.pressedLeftMouse()) {
                    if (stat.selectedRule != null && stat.selectedAction != null) {
                        PlayerOkCancelInput c = new PlayerOkCancelInput("CONFIRM", getState(), 300, 150, Lng.str("Confirm"), Lng.str("Confirm Remove Action?")) {

                            @Override
                            public void pressedOK() {
                                stat.selectedRule.removeAction(stat.selectedAction);
                                stat.change();
                                deactivate();
                            }

                            @Override
                            public void onDeactivate() {
                            }
                        };
                        c.getInputPanel().onInit();
                        c.activate();
                        /*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
                        AudioController.fireAudioEventID(814);
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
                return GUIRuleConfigPanel.this.isActive() && stat.selectedRule != null && stat.selectedAction != null;
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
