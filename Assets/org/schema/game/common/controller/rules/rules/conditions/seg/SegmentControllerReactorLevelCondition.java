package org.schema.game.common.controller.rules.rules.conditions.seg;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.power.reactor.PowerInterface;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.data.ManagedSegmentController;

public class SegmentControllerReactorLevelCondition extends SegmentControllerMoreLessCondition{

	
	
	@RuleValue(tag = "Capacity")
	public int level;
	
	public SegmentControllerReactorLevelCondition() {
		super();
	}

	@Override
	public long getTrigger() {
		return TRIGGER_ON_COLLECTION_UPDATE;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_REACTOR_LEVEL_CONDITION;
	}

	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, SegmentController a, long trigger, boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		if(a instanceof ManagedSegmentController<?>) {
			PowerInterface p = (((ManagedSegmentController<?>)a).getManagerContainer()).getPowerInterface();
			if(p.getActiveReactor() != null) {
				return moreThan ? (p.getActiveReactor().getLevel() > level) : (p.getActiveReactor().getLevel() <= level);
			}else {
				return false;
			}
		}else {
			return false;
		}
		
	}

	@Override
	public String getCountString() {
		return String.valueOf(level);
	}

	@Override
	public String getQuantifierString() {
		return "Reactor Level";
	}
	
}
