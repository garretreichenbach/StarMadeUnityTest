package org.schema.game.common.controller.rules.rules.actions.seg;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleSet;
import org.schema.game.common.controller.rules.rules.Rule;
import org.schema.game.common.controller.rules.rules.actions.ActionTypes;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.TopLevelType;
import org.schema.schine.network.objects.Sendable;

public class SegmentControllerVoidAction extends SegmentControllerAction {

	public SegmentControllerVoidAction() {
		super();
	}

	@Override
	public ActionTypes getType() {
		return ActionTypes.DO_NOTHING;
	}


	@Override
	public String getDescriptionShort() {
		return Lng.str("Do nothing");
	}

	@Override
	public void onTrigger(SegmentController s) {
		
	}

	@Override
	public void onUntrigger(SegmentController s) {
		
	}

	
	
}
