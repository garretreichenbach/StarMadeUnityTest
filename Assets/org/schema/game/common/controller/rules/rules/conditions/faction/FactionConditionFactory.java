package org.schema.game.common.controller.rules.rules.conditions.faction;

import org.schema.game.common.controller.rules.rules.conditions.ConditionFactory;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.schine.network.TopLevelType;

	public abstract class FactionConditionFactory implements ConditionFactory<Faction, FactionCondition> {

		@Override
		public TopLevelType getType() {
			return TopLevelType.FACTION;
		}
		
		
}
