package org.schema.game.common.controller.rules.rules.actions.seg;

import org.schema.common.util.StringTools;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.rules.RuleSet;
import org.schema.game.common.controller.rules.rules.Rule;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.actions.ActionTypes;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.TopLevelType;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;

public class SegmentControllerTrackAction extends SegmentControllerAction {
	
	

	public SegmentControllerTrackAction() {
		super();
	}

	@Override
	public ActionTypes getType() {
		return ActionTypes.SEG_TRACK;
	}


	@Override
	public String getDescriptionShort() {
		return Lng.str("Track the entity for admins");
	}

	@Override
	public void onTrigger(SegmentController s) {
		if(s.isOnServer()) {
			s.setTracked(true);
		}
	}

	@Override
	public void onUntrigger(SegmentController s) {
		if(s.isOnServer()) {
			s.setTracked(false);
		}
	}

	
	
}
