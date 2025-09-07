package org.schema.game.network.commands.gamerequests;

import org.schema.schine.network.commands.gamerequests.GameRequestAnswerFactory;

public class CustomTextureRequest extends DataRequest{

	@Override
	public GameRequestAnswerFactory getFactory() {
		return GameRequestAnswerFactories.CUSTOM_BLOCK_TEXTURE;
	}

}
