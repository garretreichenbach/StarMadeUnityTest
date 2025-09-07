package org.schema.game.common.data.blockeffects.config;

import java.util.List;

import org.schema.schine.network.StateInterface;

public interface ConfigManagerInterface {
	public ConfigEntityManager getConfigManager();

	public void registerTransientEffects(List<ConfigProviderSource> transientEffectSources);

	public StateInterface getState();


}
