package org.schema.schine.network.commands;

import org.schema.schine.network.common.commands.CommandPackage;
import org.schema.schine.network.common.commands.Commandable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ServerInfoRequestCommandPackage extends CommandPackage {

	@Override
	public Commandable getType() {
		return BasicCommands.SERVER_INFO_REQUEST;
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
	}

	@Override
	public void reset() {
	}

}
