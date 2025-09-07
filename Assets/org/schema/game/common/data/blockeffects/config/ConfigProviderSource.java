package org.schema.game.common.data.blockeffects.config;

import it.unimi.dsi.fastutil.shorts.ShortList;

public interface ConfigProviderSource {

	public ShortList getAppliedConfigGroups(ShortList out);
	public long getSourceId();
	
}
