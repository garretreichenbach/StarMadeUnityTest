package org.schema.game.network.commands.gamerequests;

import org.schema.schine.network.commands.gamerequests.GameRequestAnswerFactory;
import org.schema.schine.network.server.ServerState;

public class BlockBehaviorAnswer extends DataRequestAnswer{

	@Override
	public GameRequestAnswerFactory getFactory() {
		return GameRequestAnswerFactories.BLOCK_BEHAVIOR;
	}

	@Override
	public void serve(ServerState state) {
		data = state.getBlockBehaviorFile();
	}

}
