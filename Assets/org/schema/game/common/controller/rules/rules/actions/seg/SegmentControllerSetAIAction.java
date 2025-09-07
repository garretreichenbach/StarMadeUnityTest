package org.schema.game.common.controller.rules.rules.actions.seg;

import org.schema.common.util.StringTools;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleSet;
import org.schema.game.common.controller.rules.rules.Rule;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.actions.ActionTypes;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.TopLevelType;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;

public class SegmentControllerSetAIAction extends SegmentControllerAction {
	
	
	@RuleValue(tag = "StateOnTrigger")
	public boolean onTrigger;

	@RuleValue(tag = "StateOnUntrigger")
	public boolean onUntrigger;
	

	public SegmentControllerSetAIAction() {
		super();
	}

	@Override
	public ActionTypes getType() {
		return ActionTypes.SEG_SET_AI;
	}

	@Override
	public String getDescriptionShort() {
		return Lng.str("Switches AI %s when triggered and %s when no longer active", onTrigger?"on":"off", onUntrigger?"on":"off");
	}

	@Override
	public void onTrigger(SegmentController s) {
		s.railController.activateAllAIServer(onTrigger, true, true, true);
	}

	@Override
	public void onUntrigger(SegmentController s) {
		s.railController.activateAllAIServer(onUntrigger, true, true, true);
	}

	
	
}
