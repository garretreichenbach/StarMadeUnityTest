package org.schema.game.common.controller.rules.rules.conditions.faction;

import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.schine.common.language.Lng;

public class FactionFPCountCondition extends FactionMoreLessCondition {

	
	@RuleValue(tag = "FactionPoints")
	public float points;
	
	public FactionFPCountCondition() {
		super();
	}
	@Override
	public long getTrigger() {
		return FactionCondition.TRIGGER_ON_FACTION_POINTS_CHANGE;
	}
	@Override
	public ConditionTypes getType() {
		return ConditionTypes.FACTION_FP_COUNT;
	}
	
	
	@Override
	public String getCountString() {
		return String.valueOf(points);
	}

	@Override
	public String getQuantifierString() {
		return Lng.str("%s FactionPoints", points);
	}

	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, Faction a, long trigger,
			boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		float cc = a.factionPoints;
		
		return moreThan ? (cc > points) : (cc <= points);
	}


	

	

	
	
	
	
	
	
	

}
