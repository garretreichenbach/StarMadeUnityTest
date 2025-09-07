package org.schema.game.common.controller.rules.rules.conditions.faction;

import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.schine.common.language.Lng;

public class FactionMemberCountCondition extends FactionMoreLessCondition {

	
	@RuleValue(tag = "Members")
	public int members;
	
	public FactionMemberCountCondition() {
		super();
	}
	@Override
	public long getTrigger() {
		return FactionCondition.TRIGGER_ON_FACTION_MEMBER_MOD;
	}
	@Override
	public ConditionTypes getType() {
		return ConditionTypes.FACTION_MEMBER_COUNT;
	}
	
	
	@Override
	public String getCountString() {
		return String.valueOf(members);
	}

	@Override
	public String getQuantifierString() {
		return Lng.str("%s Members", members);
	}

	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, Faction a, long trigger,
			boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		long cc = a.getMembersUID().size();
		
		return moreThan ? (cc > members) : (cc <= members);
	}


	

	

	
	
	
	
	
	
	

}
