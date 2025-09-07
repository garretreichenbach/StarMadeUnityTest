package org.schema.game.network.commands.gamerequests;

import org.schema.schine.network.commands.gamerequests.GameRequestAnswerFactory;

public class BlockPropertiesRequest extends DataRequest{

	@Override
	public GameRequestAnswerFactory getFactory() {
		return GameRequestAnswerFactories.BLOCK_PROPERTIES;
	}

}
