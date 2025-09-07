package org.schema.game.common.controller.rules.rules.conditions.seg;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.data.ManagedSegmentController;

public abstract class SegmentControllerAbstractIntegrityCondition extends SegmentControllerMoreLessCondition{

	
	
	@RuleValue(tag = "Integrity")
	public double integrity;
	
	public SegmentControllerAbstractIntegrityCondition() {
		super();
	}

	@Override
	public long getTrigger() {
		return TRIGGER_ON_COLLECTION_UPDATE;
	}
	public abstract double getSmallestIntegrity(ManagedSegmentController<?> a);
	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, SegmentController a, long trigger, boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		if(a instanceof ManagedSegmentController<?>) {
			double smallestIntegrity = getSmallestIntegrity((ManagedSegmentController<?>)a);
			return moreThan ? (smallestIntegrity > integrity) : (smallestIntegrity <= integrity);
		}else {
			return false;
		}
		
	}
	@Override
	public String getCountString() {
		return String.valueOf(integrity);
	}

}
