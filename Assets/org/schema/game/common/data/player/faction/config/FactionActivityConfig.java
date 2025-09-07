package org.schema.game.common.data.player.faction.config;

import org.schema.common.config.ConfigurationElement;
import org.schema.schine.network.StateInterface;

public class FactionActivityConfig extends FactionConfig {

	@ConfigurationElement(name = "SetInactiveAfterHours")
	public static float SET_INACTIVE_AFTER_HOURS = 0;
	@ConfigurationElement(name = "SetActiveAfterOnlineForMin")
	public static float SET_ACTIVE_AFTER_ONLINE_FOR_MIN = 0;

	public FactionActivityConfig(StateInterface state) {
		super(state);
	}

	@Override
	protected String getTag() {
		return "FactionActivity";
	}

}
