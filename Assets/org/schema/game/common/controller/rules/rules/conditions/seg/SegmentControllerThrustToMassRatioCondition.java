package org.schema.game.common.controller.rules.rules.conditions.seg;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.schine.common.language.Lng;

public class SegmentControllerThrustToMassRatioCondition extends SegmentControllerMoreLessCondition{
	
	@RuleValue(tag = "Ratio")
	public float ratio;
	
	public SegmentControllerThrustToMassRatioCondition() {
		super();
	}

	@Override
	public long getTrigger() {
		return TRIGGER_ON_MASS_UPDATE | TRIGGER_ON_COLLECTION_UPDATE;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_THRUST_TO_MASS_RATIO;
	}

	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, SegmentController a, long trigger, boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		if(a instanceof Ship) {
			Ship s = (Ship)a;
			float sRatio = s.getManagerContainer().getThrusterElementManager().getActualThrust() / Math.max(0.00000f, s.getMass());
			return moreThan ? (sRatio > ratio) : (sRatio <= ratio);
		}
		return false;
	}

	@Override
	public String getCountString() {
		return String.valueOf(ratio);
	}

	@Override
	public String getQuantifierString() {
		return Lng.str("Thrust-to-Mass Ratio");
	}
	
}
