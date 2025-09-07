package org.schema.game.network.commands;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.schine.network.common.commands.CommandPackage;
import org.schema.schine.network.common.commands.Commandable;




public class ExecuteAdminCommandCommandPackage extends CommandPackage{

	
	public String serverPassword;
	public String command;


	@Override
	public Commandable getType() {
		return GameCommands.ADMIN;
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeUTF(serverPassword);
		b.writeUTF(command);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		serverPassword = b.readUTF();
		command = b.readUTF();
	}

	@Override
	public void reset() {
		serverPassword = null;
		command = null;
	}


}
