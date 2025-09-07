package org.schema.game.common.controller.rules.rules.conditions.seg;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.data.ManagedSegmentController;

public abstract class SegmentControllerAbstractOutputCountCondition extends SegmentControllerMoreLessCondition{

	
	
	
	@RuleValue(tag = "Count")
	public int count;
	
	public SegmentControllerAbstractOutputCountCondition() {
		super();
	}

	@Override
	public long getTrigger() {
		return TRIGGER_ON_COLLECTION_UPDATE;
	}
	public abstract double getOutputCount(ManagedSegmentController<?> a);
	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, SegmentController a, long trigger, boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		if(a instanceof ManagedSegmentController<?>) {
			return moreThan ? (getOutputCount((ManagedSegmentController<?>) a) > count) : (getOutputCount((ManagedSegmentController<?>) a) <= count);
		}else {
			return false;
		}
		
	}

	@Override
	public String getCountString() {
		return String.valueOf(count);
	}

	
	
}
