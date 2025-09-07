package org.schema.game.network.commands.gamerequests;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.network.StarMadePlayerStats;
import org.schema.schine.network.commands.gamerequests.GameAnswerInterface;
import org.schema.schine.network.commands.gamerequests.GameRequestAnswerFactory;

public class PlayerStatsAnswer implements GameAnswerInterface{

	public StarMadePlayerStats stats;
	
	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		stats.serialize(b, isOnServer);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		stats.deserialize(b, updateSenderStateId, isOnServer);
	}

	@Override
	public GameRequestAnswerFactory getFactory() {
		return GameRequestAnswerFactories.PLAYER_STATS;
	}

	@Override
	public void free() {
	}

	

}
