package org.schema.schine.network.commands;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.schine.network.common.commands.CommandPackage;
import org.schema.schine.network.common.commands.Commandable;




public class RequestSynchronizeAllCommandPackage extends CommandPackage{

	@Override
	public Commandable getType() {
		return BasicCommands.REQUEST_SYNCH_ALL;
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
