package org.schema.game.common.controller.rules.rules;

import org.schema.game.common.controller.rules.rules.conditions.faction.FactionCondition;
import org.schema.game.common.data.player.faction.Faction;

public class FactionRuleEntityManager  extends RuleEntityManager<Faction>{

	public FactionRuleEntityManager(Faction entity) {
		super(entity);
	}

	@Override
	public byte getEntitySubType() {
		return 105;
	}
	public void triggerFactionMemberMod() {
		trigger(FactionCondition.TRIGGER_ON_FACTION_MEMBER_MOD);		
	}
	public void triggerFactionPointsChange() {
		trigger(FactionCondition.TRIGGER_ON_FACTION_POINTS_CHANGE);		
	}
	public void triggerFactionRelationShipChanged() {
		trigger(FactionCondition.TRIGGER_ON_FACTION_RELATIONSHIP_MOD);		
	}
}
