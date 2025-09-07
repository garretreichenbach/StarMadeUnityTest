package org.schema.game.common.controller.rules.rules.conditions.seg;

import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.schine.common.language.Lng;

public abstract class SegmentControllerMoreLessCondition extends SegmentControllerCondition {

	@RuleValue(tag = "MoreThan")
	public boolean moreThan;
	
	public SegmentControllerMoreLessCondition() {
		super();
	}

	public abstract String getCountString();
	public abstract String getQuantifierString();
	@Override
	public String getDescriptionShort() {
		return (moreThan ? Lng.str("Must have more than") : Lng.str("Must have less than or exactly"))+" "+getCountString()+" "+getQuantifierString();
	}
}
