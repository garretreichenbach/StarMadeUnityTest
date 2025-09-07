package org.schema.game.network.commands.gamerequests;

import org.schema.schine.network.commands.gamerequests.GameRequestAnswerFactory;
import org.schema.schine.network.server.ServerState;

public class BlockPropertiesAnswer extends DataRequestAnswer{

	@Override
	public GameRequestAnswerFactory getFactory() {
		return GameRequestAnswerFactories.BLOCK_PROPERTIES;
	}

	@Override
	public void serve(ServerState state) {
		data = state.getBlockPropertiesFile();
	}

}
