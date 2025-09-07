package org.schema.game.server.data.simulation.npc.news;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.server.data.FactionState;

public abstract class NPCFactionNewsEventOtherEnt extends NPCFactionNewsEvent{
	public String otherEnt;
	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		super.serialize(b, isOnServer);
		b.writeUTF(otherEnt);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
			boolean isOnServer) throws IOException {
		super.deserialize(b, updateSenderStateId, isOnServer);
		otherEnt = b.readUTF();
	}


	public String getOtherName(FactionState state){
		return otherEnt;
	}
	
}
