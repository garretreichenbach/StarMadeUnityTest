package org.schema.game.client.view.mainmenu.gui.ruleconfig;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.client.controller.PlayerDropDownInput;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.client.view.mainmenu.MainMenuGUI;
import org.schema.game.common.controller.rules.rules.Rule;
import org.schema.game.common.controller.rules.rules.actions.Action;
import org.schema.game.common.controller.rules.rules.actions.ActionList;
import org.schema.game.common.controller.rules.rules.actions.ActionTypes;
import org.schema.game.common.controller.rules.rules.conditions.Condition;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonColor;
import org.schema.schine.input.InputState;

import java.io.IOException;
import java.util.List;

public class GUIActionListConfigPanel extends GUIElement implements GUIActiveInterface, RuleListUpdater {


	public GUIMainWindow mainPanel;
	private GUIContentPane mainTab;
	private DialogInput diag;

	private List<GUIElement> toCleanUp = new ObjectArrayList<GUIElement>();

	private GUIRuleStat stat;
	private boolean init;
	private GUIConditionAndActionDetailList dList;
	private GUIContentPane t;
	private final ActionList<?,?> g;
	private RuleListUpdater lastRSC;
	private int detailIndex;

	public GUIActionListConfigPanel(InputState state, GUIRuleStat stat, ActionList<?,?> g, DialogInput diag) {
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
		for(GUIElement e : toCleanUp){
			e.cleanUp();
		}
		toCleanUp.clear();
		stat.rsc = lastRSC;
	}

	@Override
	public void draw() {
		if(!init){
			onInit();
		}
		GlUtil.glPushMatrix();
		transform();

		mainPanel.draw();


		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		if(init){
			return;
		}
		mainPanel = new GUIMainWindow(getState(), GLFrame.getWidth()-410, GLFrame.getHeight()-20, 400, 10, "RuleWindow");
		mainPanel.onInit();
		mainPanel.setPos(435, 35, 0);
		mainPanel.setWidth(GLFrame.getWidth()-470);
		mainPanel.setHeight(GLFrame.getHeight()-70);


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
				if(event.pressedLeftMouse()){
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

		t = mainPanel.addTab(Lng.str("ACTIONLIST"));
		t.setTextBoxHeightLast(24);


		t.addNewTextBox(72);

		addEditButtonPanel(t, 1);

		t.addNewTextBox(280);

		GUIActionList elemList = new GUIActionList(getState(), t.getContent(2), this, stat, g);
		elemList.onInit();
		t.getContent(2).attach(elemList);


		t.addNewTextBox(100);

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
		GUIHorizontalButtonTablePane bb = new GUIHorizontalButtonTablePane(getState(), 3, 1, t.getContent(index));
		bb.onInit();


		bb.addButton(0, 0, Lng.str("Add Action"), HButtonColor.BLUE, new GUICallback() {
			@Override
			public boolean isOccluded() {
				return !isActive();
			}
			private ConditionTypes selected;
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()){


					int i = 0;


					List<ActionTypes> v = ActionTypes.getSortedByName(g.getEntityType());
					GUIElement[] conds = new GUIElement[v.size()];
					for(ActionTypes c : v){
						GUIAnchor w = new GUIAnchor(getState(), 200, 24);
						GUITextOverlay t = new GUITextOverlay(FontSize.MEDIUM_15,getState());
						t.setTextSimple(c.getName());
						t.setPos(4, 4, 0);
						w.attach(t);
						w.setUserPointer(c);
						conds[i] = w;
						i++;
					}

					PlayerDropDownInput p = new PlayerDropDownInput("RULESELACTIONTYPE", getState(), 400, 200, Lng.str("Add Action"), 24, new Object() {
						public String toString() {
							return Lng.str("Select Action Type")+"\n"+(selected != null ? selected.getDesc() : "");
						}
					}, conds) {

						@Override
						public void pressedOK(GUIListElement current) {
							ActionTypes cType = (ActionTypes) current.getContent().getUserPointer();
							Action<?> c = cType.fac.instantiateAction();
							g.add(c);
							stat.selectedAction = c;
							System.err.println("SELECTED ACTION: "+c);
							stat.selectedCondition = null;
							stat.change();
							deactivate();
						}

						@Override
						public void onDeactivate() {

						}

						@Override
						public void onSelectionChanged(GUIListElement element) {
							super.onSelectionChanged(element);
							if(element != null) {
								selected = (ConditionTypes) element.getUserPointer();
							}
						}

					};
					p.activate();

				}
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return true;
			}
			@Override
			public boolean isActive(InputState state) {
				return GUIActionListConfigPanel.this.isActive() && stat.manager != null && stat.selectedRule != null;
			}
		});
		bb.addButton(1, 0, Lng.str("Dupl. Action"), HButtonColor.BLUE, new GUICallback() {
			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()){
					if(stat.selectedCondition != null) {
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
				return GUIActionListConfigPanel.this.isActive() && stat.manager != null  && stat.selectedAction != null;
			}
		});
		bb.addButton(2, 0, Lng.str("Remove Action"), HButtonColor.RED, new GUICallback() {
			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()){
					if(stat.selectedAction != null) {
						g.remove(stat.selectedAction);
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
				return GUIActionListConfigPanel.this.isActive() && stat.manager != null  && stat.selectedAction != null;
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
		return !MainMenuGUI.runningSwingDialog && (playerInputs.isEmpty() || playerInputs.get(playerInputs.size()-1).getInputPanel() == this);
	}




}
