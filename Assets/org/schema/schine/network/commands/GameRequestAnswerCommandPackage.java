package org.schema.schine.network.commands;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.schine.network.commands.gamerequests.GameAnswerInterface;
import org.schema.schine.network.commands.gamerequests.GameRequestAnswerFactory;
import org.schema.schine.network.common.commands.CommandPackage;
import org.schema.schine.network.common.commands.Commandable;




public class GameRequestAnswerCommandPackage extends CommandPackage{

	
	public GameAnswerInterface answer;


	@Override
	public Commandable getType() {
		return BasicCommands.GAME_ANSWER;
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeByte(answer.getFactory().getGameRequestid());
		answer.serialize(b, isOnServer);
		
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		byte code = b.readByte();
		GameRequestAnswerFactory fac = GameRequestAnswerFactory.factories.get(code);
		if(fac == null) {
			throw new IOException("Invalid game request/answer id: "+code+"; registered: "+GameRequestAnswerFactory.factories);
		}
		answer = fac.getAnswerInstance();
		answer.deserialize(b, updateSenderStateId, isOnServer);
	}

	@Override
	public void reset() {
		answer.free();
		answer = null;
	}


}
