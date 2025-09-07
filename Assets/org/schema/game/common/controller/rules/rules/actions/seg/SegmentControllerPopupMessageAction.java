package org.schema.game.common.controller.rules.rules.actions.seg;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.actions.ActionTypes;
import org.schema.schine.common.language.Lng;

public class SegmentControllerPopupMessageAction extends SegmentControllerAction {
	
	
	@RuleValue(tag = "TriggerMessage")
	public String triggerMessage = "";
	@RuleValue(tag = "TriggerMsgType", intMap = {0,1,2,3,4}, int2StringMap = {"Simple", "Info", "Warning", "Error", "Dialog"})
	public byte triggerMsgType;
	

	@RuleValue(tag = "UntriggerMessage")
	public String untriggerMessage = "";
	@RuleValue(tag = "UntriggerMsgType", intMap = {0,1,2,3,4}, int2StringMap = {"Simple", "Info", "Warning", "Error", "Dialog"})
	public byte untriggerMsgType;
	
	public SegmentControllerPopupMessageAction() {
		super();
	}

	@Override
	public ActionTypes getType() {
		return ActionTypes.SEG_POPUP_MESSAGE;
	}

	@Override
	public String getDescriptionShort() {
		return Lng.str("Pops up Message (leave empty to skip)");
	}

	@Override
	public void onTrigger(SegmentController s) {
		if(s.isOnServer() && triggerMessage.trim().length() > 0) {
			s.sendServerMessage(triggerMessage, triggerMsgType);
		}
	}

	@Override
	public void onUntrigger(SegmentController s) {
		if(s.isOnServer() && untriggerMessage.trim().length() > 0) {
			s.sendServerMessage(untriggerMessage, untriggerMsgType);
		}
	}

	
	
}
