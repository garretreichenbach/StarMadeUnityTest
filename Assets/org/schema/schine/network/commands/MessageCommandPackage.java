package org.schema.schine.network.commands;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.schine.network.common.commands.CommandPackage;
import org.schema.schine.network.common.commands.Commandable;
import org.schema.schine.network.server.ServerMessage;




public class MessageCommandPackage extends CommandPackage{

	public ServerMessage message;
	
	
	@Override
	public Commandable getType() {
		return BasicCommands.MESSAGE;
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		message.serialize(b, isOnServer);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		message = new ServerMessage();
		message.deserialize(b, updateSenderStateId, isOnServer);
		
	}

	@Override
	public void reset() {
		message = null;
	}


}
