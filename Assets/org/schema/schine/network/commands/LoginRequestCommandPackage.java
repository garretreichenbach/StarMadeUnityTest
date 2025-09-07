package org.schema.schine.network.commands;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.util.Version;
import org.schema.schine.network.common.commands.CommandPackage;
import org.schema.schine.network.common.commands.Commandable;





public class LoginRequestCommandPackage extends CommandPackage{

	
	public String playerName;
	public Version version;
	public String uniqueSessionID;
	public String authCodeToken;
	public byte userAgent;
	

	@Override
	public Commandable getType() {
		return BasicCommands.LOGIN_REQUEST;
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeUTF(playerName);
		version.serialize(b, isOnServer);
		b.writeUTF(uniqueSessionID);
		b.writeUTF(authCodeToken);
		b.writeByte(userAgent);
		
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		playerName = b.readUTF();
		version = Version.deserializeStatic(b, updateSenderStateId, isOnServer);
		uniqueSessionID = b.readUTF();
		authCodeToken = b.readUTF();
		userAgent = b.readByte();
	}

	@Override
	public void reset() {
		playerName = null;
		version = null;
		uniqueSessionID = null;
		authCodeToken = null;
		userAgent = 0;
	}


}
