package org.schema.game.network.commands;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.server.data.admin.AdminCommands;
import org.schema.schine.network.common.commands.Command;
import org.schema.schine.network.common.commands.CommandPackage;
import org.schema.schine.network.common.commands.Commandable;




public class AdminCommandCommandPackage extends CommandPackage{

	
	public AdminCommands adminCommand;
	public Object[] commandParams;


	@Override
	public Commandable getType() {
		return GameCommands.ADMIN;
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeInt(adminCommand.ordinal());
		Command.serialize(commandParams, b);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		adminCommand = AdminCommands.values()[b.readInt()];
		commandParams = Command.deserialize(b);
	}

	@Override
	public void reset() {
		adminCommand = null;
		commandParams = null;
	}


}
