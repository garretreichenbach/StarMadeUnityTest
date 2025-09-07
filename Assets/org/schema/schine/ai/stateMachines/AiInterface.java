package org.schema.schine.ai.stateMachines;

import org.schema.schine.network.StateInterface;

public interface AiInterface {
	public AIConfigurationInterface getAiConfiguration();

	public String getRealName();

	public String getUniqueIdentifier();

	public StateInterface getState();
}

