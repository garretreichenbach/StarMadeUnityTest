package org.schema.game.network.commands.gamerequests;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.commands.gamerequests.GameRequestAnswerFactory;
import org.schema.schine.network.commands.gamerequests.GameRequestInterface;
import org.schema.schine.network.common.NetworkProcessor;
import org.schema.schine.network.server.ServerState;

public class KillCharacterRequest implements GameRequestInterface{

	public int id = -1;
	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeInt(id);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		id = b.readInt();
	}

	@Override
	public GameRequestAnswerFactory getFactory() {
		return GameRequestAnswerFactories.KILL_OWN_CHARACTER;
	}

	@Override
	public void free() {
	}

	@Override
	public void handleAnswer(NetworkProcessor p, ServerState sd) throws IOException {
		GameServerState state = (GameServerState)sd;
		assert(id > 0):id;
		if (state.getLocalAndRemoteObjectContainer().getLocalObjects().containsKey(id)) {
			state.getLocalAndRemoteObjectContainer().getLocalObjects().get(id).setMarkedForDeleteVolatile(true);
		}else {
			System.err.println("[SERVER] Can't suicide non-existing character. ID "+id);
		}
	}

}
