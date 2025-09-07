package org.schema.game.network.commands.gamerequests;

import org.schema.schine.network.commands.gamerequests.GameRequestAnswerFactory;
import org.schema.schine.network.server.ServerState;

public class CustomTextureAnswer extends DataRequestAnswer{

	@Override
	public GameRequestAnswerFactory getFactory() {
		return GameRequestAnswerFactories.CUSTOM_BLOCK_TEXTURE;
	}
	@Override
	public void serve(ServerState state) {
		data = state.getCustomTexturesFile();
	}
}
