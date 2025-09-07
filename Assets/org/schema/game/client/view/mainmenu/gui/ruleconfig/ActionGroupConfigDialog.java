package org.schema.game.client.view.mainmenu.gui.ruleconfig;

import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.common.controller.rules.rules.actions.Action;
import org.schema.game.common.controller.rules.rules.actions.ActionList;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;

public class ActionGroupConfigDialog extends DialogInput{

	private final GUIActionListConfigPanel p;
	private GUIRuleStat stat;
	private Action<?> act;
	public ActionGroupConfigDialog(InputState state, GUIRuleStat stat, Action<?> currentAction, ActionList<?, ?> actionList) {
		super(state);
		p = new GUIActionListConfigPanel(state, stat, actionList, this);
		p.onInit();
		this.stat = stat;
		this.act = currentAction;
	}

	//@Override
	//public void handleMouseEvent(MouseEvent e) {
	//}

	@Override
	public GUIElement getInputPanel() {
		return p;
	}

	@Override
	public void onDeactivate() {
		p.cleanUp();
		//select the original action
		stat.selectedAction = act;
		stat.change();
		
	}


	@Override
	public void update(Timer timer) {
		super.update(timer);
		stat.updateLocal(timer);
	}

}