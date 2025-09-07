package org.schema.game.common.data.player.faction.config;

import org.schema.common.config.ConfigurationElement;
import org.schema.schine.network.StateInterface;

public class FactionPointSpendingConfig extends FactionConfig {

	@ConfigurationElement(name = "SpentAbsoluteForDeath")
	public static float FACTION_POINT_ABS_LOSS_PER_DEATH = 0;
	@ConfigurationElement(name = "SpentForDeathMultByMemberCount")
	public static float FACTION_POINT_MULT_BY_MEMBERS_LOSS_PER_DEATH = 0;
	@ConfigurationElement(name = "SpentPerControlledSystem")
	public static float FACTION_POINTS_PER_CONTROLLED_SYSTEM = 0;
	@ConfigurationElement(name = "BasicFlatCost")
	public static float BASIC_FLAT_COST = 0;

	public FactionPointSpendingConfig(StateInterface state) {
		super(state);
	}

	@Override
	protected String getTag() {
		return "FactionPointSpending";
	}

}
