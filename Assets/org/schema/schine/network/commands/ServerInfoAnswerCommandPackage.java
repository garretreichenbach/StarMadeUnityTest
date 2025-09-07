package org.schema.schine.network.commands;

import org.schema.schine.network.ServerInfo;
import org.schema.schine.network.common.commands.CommandPackage;
import org.schema.schine.network.common.commands.Commandable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ServerInfoAnswerCommandPackage extends CommandPackage {

	public ServerInfo info = new ServerInfo();

	public ServerInfoAnswerCommandPackage() {
	}

	@Override
	public Commandable getType() {
		return BasicCommands.SERVER_INFO_ANSWER;
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		info.serialize(b, isOnServer);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		info = new ServerInfo();
		info.deserialize(b, updateSenderStateId, isOnServer);
	}

	@Override
	public void reset() {
		info = null;
	}

}
