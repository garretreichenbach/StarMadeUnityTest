package org.schema.game.common.controller.rules.rules.conditions.sector;

import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.schine.common.language.Lng;

public abstract class SectorMoreLessCondition extends SectorCondition{
	@RuleValue(tag = "MoreThan")
	public boolean moreThan;
	
	public SectorMoreLessCondition() {
		super();
	}

	public abstract String getCountString();
	public abstract String getQuantifierString();
	@Override
	public String getDescriptionShort() {
		return (moreThan ? Lng.str("Must have more than") : Lng.str("Must have less than or exactly"))+" "+getCountString()+" "+getQuantifierString();
	}
}
