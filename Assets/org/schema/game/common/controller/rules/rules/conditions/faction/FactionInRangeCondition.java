package org.schema.game.common.controller.rules.rules.conditions.faction;

import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.controller.rules.rules.conditions.FactionRange;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.schine.common.language.Lng;

public class FactionInRangeCondition extends FactionCondition {

	@RuleValue(tag = "Range")
	public FactionRange range = new FactionRange();
	
	public FactionInRangeCondition() {
		super();
	}

	@Override
	public long getTrigger() {
		return FactionCondition.TRIGGER_ON_RULE_CHANGE;
	}

	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, Faction a, long trigger,
			boolean forceTrue) {
		return range.isInRange(a.getIdFaction());
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.FACTION_IS_RANGE;
	}

	@Override
	public String getDescriptionShort() {
		return Lng.str("Faction ID is in range %s",range);
	}
	
	
	
	
	
	
	

}
