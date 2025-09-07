package org.schema.game.common.controller.ai;

import org.schema.schine.ai.stateMachines.AiInterface;

public interface SegmentControllerAIInterface extends AiInterface {
	public void activateAI(boolean active, boolean send);
}
