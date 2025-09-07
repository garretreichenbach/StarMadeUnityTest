package org.schema.game.common.controller.rules.rules.conditions.faction;

import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.schine.common.language.Lng;

public abstract class FactionMoreLessCondition extends FactionCondition {

	public FactionMoreLessCondition() {
		super();
	}
	
	@RuleValue(tag = "MoreThan")
	public boolean moreThan;
	

	public abstract String getCountString();
	public abstract String getQuantifierString();
	@Override
	public String getDescriptionShort() {
		return (moreThan ? Lng.str("Must have more than") : Lng.str("Must have less than or exactly"))+" "+getCountString()+" "+getQuantifierString();
	}
	
	
	
	
	

}
