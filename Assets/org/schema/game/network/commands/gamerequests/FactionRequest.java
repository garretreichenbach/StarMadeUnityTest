package org.schema.game.network.commands.gamerequests;

import org.schema.schine.network.commands.gamerequests.GameRequestAnswerFactory;

public class FactionRequest extends DataRequest{

	@Override
	public GameRequestAnswerFactory getFactory() {
		return GameRequestAnswerFactories.FACTION_CONFIG;
	}

}
