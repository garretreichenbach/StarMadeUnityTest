package org.schema.game.server.ai.program.common.states;

import org.schema.game.common.controller.SegmentController;
import org.schema.schine.ai.AiEntityStateInterface;

public abstract class SegmentControllerGameState<E extends SegmentController> extends GameState<E>{

	public SegmentControllerGameState(AiEntityStateInterface gObj) {
		super(gObj);
	}
}
