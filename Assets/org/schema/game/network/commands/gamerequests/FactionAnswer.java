package org.schema.game.network.commands.gamerequests;

import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.commands.gamerequests.GameRequestAnswerFactory;
import org.schema.schine.network.server.ServerState;

public class FactionAnswer extends DataRequestAnswer{

	@Override
	public GameRequestAnswerFactory getFactory() {
		return GameRequestAnswerFactories.FACTION_CONFIG;
	}
	@Override
	public void serve(ServerState sd)  {
		GameServerState state = (GameServerState)sd;
		data = state.getFactionConfigFile();
	}
}
