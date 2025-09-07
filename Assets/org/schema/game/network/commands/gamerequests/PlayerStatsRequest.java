package org.schema.game.network.commands.gamerequests;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.network.StarMadePlayerStats;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.commands.gamerequests.GameRequestAnswerFactory;
import org.schema.schine.network.commands.gamerequests.GameRequestInterface;
import org.schema.schine.network.common.NetworkProcessor;
import org.schema.schine.network.server.ServerState;

public class PlayerStatsRequest implements GameRequestInterface{

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
	}

	@Override
	public GameRequestAnswerFactory getFactory() {
		return GameRequestAnswerFactories.PLAYER_STATS;
	}

	@Override
	public void free() {
	}

	@Override
	public void handleAnswer(NetworkProcessor p, ServerState sd) throws IOException {
		GameServerState state = (GameServerState)sd;
		
		PlayerStatsAnswer a = new PlayerStatsAnswer();
		a.stats = StarMadePlayerStats.create(state);
		GameRequestAnswerFactory.send(a, p);
	}

}
