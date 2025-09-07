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

public class SegmentControllerSetFactionAction extends SegmentControllerAction {
	
	
	@RuleValue(tag = "FactionId")
	public int factionId;
	

	public SegmentControllerSetFactionAction() {
		super();
	}

	@Override
	public ActionTypes getType() {
		return ActionTypes.SEG_SET_FACTION;
	}

	@Override
	public String getDescriptionShort() {
		return Lng.str("Pops up Message (leave empty to skip)");
	}

	@Override
	public void onTrigger(SegmentController s) {
		s.setFactionId(factionId);
	}

	@Override
	public void onUntrigger(SegmentController s) {
	}

	
	
}
