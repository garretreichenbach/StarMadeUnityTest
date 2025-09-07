package org.schema.game.common.controller.rules.rules.conditions.faction;

import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.game.server.data.FactionState;
import org.schema.schine.common.language.Lng;

import java.util.Collection;

public class FactionRelationshipCountCondition extends FactionMoreLessCondition {

	
	@RuleValue(tag = "Amount")
	public int amount;

	@RuleValue(tag = "Relationship")
	public RType rel = RType.NEUTRAL;
	
	public FactionRelationshipCountCondition() {
		super();
	}
	@Override
	public long getTrigger() {
		return FactionCondition.TRIGGER_ON_FACTION_RELATIONSHIP_MOD;
	}
	@Override
	public ConditionTypes getType() {
		return ConditionTypes.FACTION_RELATIONSHIP_COUNT;
	}
	
	
	@Override
	public String getCountString() {
		return String.valueOf(amount);
	}

	@Override
	public String getQuantifierString() {
		return Lng.str("%s %s", amount, rel.getName());
	}

	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, Faction a, long trigger,
			boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		int cc = 0;
		if(rel == RType.NEUTRAL) {
			FactionManager factionManager = ((FactionState)a.getState()).getFactionManager();
			Collection<Faction> facs = factionManager.getFactionCollection();
			for(Faction f : facs) {
				if(f != a &&factionManager.getRelation(a.getIdFaction(), f.getIdFaction()) == RType.NEUTRAL) {
					cc++;
				}
			}
		}else {
			if(rel == RType.ENEMY) {
				cc = a.getEnemies().size();
			}else {
				assert(rel == RType.FRIEND);
				cc = a.getFriends().size();
			}
		}
		
		return moreThan ? (cc > amount) : (cc <= amount);
	}


	

	

	
	
	
	
	
	
	

}
