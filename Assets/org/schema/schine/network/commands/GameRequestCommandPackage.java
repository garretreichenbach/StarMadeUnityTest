package org.schema.schine.network.commands;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.schine.network.commands.gamerequests.GameRequestAnswerFactory;
import org.schema.schine.network.commands.gamerequests.GameRequestInterface;
import org.schema.schine.network.common.commands.CommandPackage;
import org.schema.schine.network.common.commands.Commandable;




public class GameRequestCommandPackage extends CommandPackage{

	
	

	public GameRequestInterface request;


	@Override
	public Commandable getType() {
		return BasicCommands.GAME_REQUEST;
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeByte(request.getFactory().getGameRequestid());
		request.serialize(b, isOnServer);
		
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		byte code = b.readByte();
		GameRequestAnswerFactory fac = GameRequestAnswerFactory.factories.get(code);
		if(fac == null) {
			throw new IOException("Invalid game request/answer id: "+code+"; registered: "+GameRequestAnswerFactory.factories);
		}
		request = fac.getRequestInstance();
		request.deserialize(b, updateSenderStateId, isOnServer);
	}

	@Override
	public void reset() {
		request.free();
		request = null;
	}

}
