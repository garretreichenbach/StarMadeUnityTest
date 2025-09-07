package org.schema.game.network.commands.gamerequests;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.network.StarMadeServerStats;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.commands.gamerequests.GameRequestAnswerFactory;
import org.schema.schine.network.commands.gamerequests.GameRequestInterface;
import org.schema.schine.network.common.NetworkProcessor;
import org.schema.schine.network.server.ServerState;

public class ServerStatsRequest implements GameRequestInterface{

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
	}

	@Override
	public GameRequestAnswerFactory getFactory() {
		return GameRequestAnswerFactories.SERVER_STATS;
	}

	@Override
	public void free() {
	}

	@Override
	public void handleAnswer(NetworkProcessor p, ServerState sd) throws IOException {
		GameServerState state = (GameServerState)sd;
		
		ServerStatsAnswer a = new ServerStatsAnswer();
		a.stats = StarMadeServerStats.create(state);
		GameRequestAnswerFactory.send(a, p);
	}

}
