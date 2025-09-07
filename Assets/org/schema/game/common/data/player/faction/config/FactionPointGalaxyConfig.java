package org.schema.game.common.data.player.faction.config;

import org.schema.common.config.ConfigurationElement;
import org.schema.schine.network.StateInterface;

public class FactionPointGalaxyConfig extends FactionConfig {

	@ConfigurationElement(name = "FreeHomeBase")
	public static boolean FREE_HOMEBASE = false;
	@ConfigurationElement(name = "PenaltyFromCenterMult")
	public static float PENALTY_FROM_CENTER_MULT = 0;
	@ConfigurationElement(name = "PenaltyPerDistanceUnitFromHomeBase")
	public static float PENALTY_PER_DISTANCE_UNIT_FROM_HOMEBASE = 2;
	@ConfigurationElement(name = "ThresholdTakenRadiusForTradeGates")
	public static float THRESHHOLD_TAKE_RADIUS_FOR_TRADE_GATES = 0;

	public FactionPointGalaxyConfig(StateInterface state) {
		super(state);
	}

	@Override
	protected String getTag() {
		return "FactionPointGalaxy";
	}

}
