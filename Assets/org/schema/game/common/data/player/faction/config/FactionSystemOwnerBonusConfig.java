package org.schema.game.common.data.player.faction.config;

import org.schema.common.config.ConfigurationElement;
import org.schema.schine.network.StateInterface;

public class FactionSystemOwnerBonusConfig extends FactionConfig {

	@ConfigurationElement(name = "MiningBonusOwner")
	public static float MINING_BONUS_OWNER = 0;
	@ConfigurationElement(name = "MiningBonusOthers")
	public static float MINING_BONUS_OTHERS = 0;
	@ConfigurationElement(name = "MiningBonusUnowned")
	public static float MINING_BONUS_UNOWNED = 0;

	public FactionSystemOwnerBonusConfig(StateInterface state) {
		super(state);
	}

	@Override
	protected String getTag() {
		return "FactionSystemOwnerBonus";
	}

}
