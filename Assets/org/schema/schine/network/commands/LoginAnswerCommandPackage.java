package org.schema.schine.network.commands;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.util.Version;
import org.schema.schine.network.common.commands.CommandPackage;
import org.schema.schine.network.common.commands.Commandable;




public class LoginAnswerCommandPackage extends CommandPackage{

	
	

	public int idOrFailedReturnCode;
	public String starMadeName = "";
	public boolean upgradedAccount;
	public long loginTime;
	public Version serverVersion;
	public String failReson = "";
	public String playerName;

	@Override
	public Commandable getType() {
		return BasicCommands.LOGIN_ANSWER;
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeInt(idOrFailedReturnCode);
		b.writeUTF(starMadeName);
		b.writeUTF(playerName);
		b.writeBoolean(upgradedAccount);
		b.writeLong(System.currentTimeMillis());
		serverVersion.serialize(b, isOnServer);
		b.writeUTF(failReson);
		
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		idOrFailedReturnCode = b.readInt();
		starMadeName = b.readUTF();
		playerName = b.readUTF();
		upgradedAccount = b.readBoolean();
		loginTime = b.readLong();
		serverVersion = Version.deserializeStatic(b, updateSenderStateId, isOnServer);
		failReson = b.readUTF();
	}

	@Override
	public void reset() {
		idOrFailedReturnCode = 0;
		starMadeName = "";
		upgradedAccount = false;
		loginTime = 0;
		serverVersion = null;
		failReson = "";
	}


}
