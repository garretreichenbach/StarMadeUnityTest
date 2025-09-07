package org.schema.game.common.data.player.faction.config;

import org.schema.common.config.ConfigurationElement;
import org.schema.schine.network.StateInterface;

public class FactionPointIncomeConfig extends FactionConfig {

	@ConfigurationElement(name = "GainedPerMember")
	public static float FACTION_POINTS_PER_MEMBER = 0;
	@ConfigurationElement(name = "GainedPerOnlineMember")
	public static float FACTION_POINTS_PER_ONLINE_MEMBER = 0;

	public FactionPointIncomeConfig(StateInterface state) {
		super(state);
	}

	@Override
	protected String getTag() {
		return "FactionPointIncome";
	}

}
