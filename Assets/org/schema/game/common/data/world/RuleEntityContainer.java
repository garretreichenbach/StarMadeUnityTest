package org.schema.game.common.data.world;

import org.schema.game.common.controller.rules.rules.RuleEntityManager;
import org.schema.game.network.objects.NTRuleInterface;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.TopLevelType;

public interface RuleEntityContainer {
	public RuleEntityManager<?> getRuleEntityManager();

	public TopLevelType getTopLevelType();

	public NTRuleInterface getNetworkObject();
	
	public StateInterface getState();
	
	public boolean isOnServer();

}
