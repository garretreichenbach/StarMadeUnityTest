package org.schema.game.common.controller.rules.rules.conditions.seg;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.schine.common.language.Lng;

public class SegmentControllerChainDockDepthCondition extends SegmentControllerMoreLessCondition{

	

	
	@RuleValue(tag = "Entities")
	public int entities;
	
	public SegmentControllerChainDockDepthCondition() {
		super();
	}

	@Override
	public long getTrigger() {
		return TRIGGER_ON_DOCKING_CHANGED;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_CHAIN_DOCK_DEPTH;
	}

	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, SegmentController a, long trigger, boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		return moreThan ? (a.railController.getDockingDepthFromHere() > entities) : (a.railController.getDockingDepthFromHere() <= entities);
	}
	@Override
	public String getQuantifierString() {
		return Lng.str("Chain Depth");
	}

	@Override
	public String getCountString() {
		return String.valueOf(entities);
	}
	
}
