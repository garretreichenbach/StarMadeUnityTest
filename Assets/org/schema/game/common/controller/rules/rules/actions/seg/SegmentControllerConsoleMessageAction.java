package org.schema.game.common.controller.rules.rules.actions.seg;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.actions.ActionTypes;
import org.schema.schine.common.language.Lng;

public class SegmentControllerConsoleMessageAction extends SegmentControllerAction {
	
	
	@RuleValue(tag = "TriggerMessage")
	public String triggerMessage = "";

	@RuleValue(tag = "UntriggerMessage")
	public String untriggerMessage = "";

	public SegmentControllerConsoleMessageAction() {
		super();
	}

	@Override
	public ActionTypes getType() {
		return ActionTypes.SEG_CONSOLE_MESSAGE;
	}


	@Override
	public String getDescriptionShort() {
		return Lng.str("Prints Message (leave empty to skip)");
	}

	@Override
	public void onTrigger(SegmentController s) {
		if(triggerMessage.trim().length() > 0) {
			if(s.isOnServer()) {
				System.err.println("[SERVER][RULE][ACTION][MESSAGE][TRIGGER] "+triggerMessage);
			}else {
				System.err.println("[CLIENT][RULE][ACTION][MESSAGE][TRIGGER] "+triggerMessage);
			}
		}
	}

	@Override
	public void onUntrigger(SegmentController s) {
		if(untriggerMessage.trim().length() > 0) {
			if(s.isOnServer()) {
				System.err.println("[SERVER][RULE][ACTION][MESSAGE][TRIGGER] "+untriggerMessage);
			}else {
				System.err.println("[CLIENT][RULE][ACTION][MESSAGE][TRIGGER] "+untriggerMessage);
			}
		}
	}

	
	
}
