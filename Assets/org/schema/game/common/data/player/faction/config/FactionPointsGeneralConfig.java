package org.schema.game.common.data.player.faction.config;

import org.schema.common.config.ConfigurationElement;
import org.schema.schine.network.StateInterface;

public class FactionPointsGeneralConfig extends FactionConfig {

	@ConfigurationElement(name = "FactionPointDeathProtectionMinutes")
	public static float FACTION_POINT_DEATH_PROTECTION_MIN = 0;
	@ConfigurationElement(name = "IncomeExpensePeriodMinutes")
	public static float INCOME_EXPENSE_PERIOD_MINUTES = 3;
	@ConfigurationElement(name = "InitialFactionPoints")
	public static float INITIAL_FACTION_POINTS = 0;

	public FactionPointsGeneralConfig(StateInterface state) {
		super(state);
	}

	@Override
	protected String getTag() {
		return "FactionPointGeneral";
	}

}
