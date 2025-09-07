package org.schema.game.common.controller.rules.rules.actions.seg;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.actions.ActionTypes;
import org.schema.schine.common.language.Lng;

public class SegmentControllerModifyThrusterAction extends SegmentControllerAction {
	
	
	@RuleValue(tag = "ThrusterPercent")
	public float thrusterPercent = 1f;

	public SegmentControllerModifyThrusterAction() {
		super();
	}

	@Override
	public ActionTypes getType() {
		return ActionTypes.SEG_MODIFY_THRUSTER_ACTION;
	}


	@Override
	public String getDescriptionShort() {
		return Lng.str("Set Thruster to %s%%", (thrusterPercent*100f));
	}

	@Override
	public void onTrigger(SegmentController s) {
		if(s instanceof Ship) {
			System.err.println(s.getState()+"[ACTION] Trigger Thruster Modify action for "+s+": "+thrusterPercent);
			((Ship)s).getManagerContainer().getThrusterElementManager().ruleModifierOnThrust = thrusterPercent;
		}
	}

	@Override
	public void onUntrigger(SegmentController s) {
		if(s instanceof Ship) {
			((Ship)s).getManagerContainer().getThrusterElementManager().ruleModifierOnThrust = 1;
		}
	}

	
	
}
