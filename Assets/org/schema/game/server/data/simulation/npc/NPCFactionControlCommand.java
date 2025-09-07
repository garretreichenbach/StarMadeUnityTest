package org.schema.game.server.data.simulation.npc;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.network.objects.remote.SimpleCommand;
import org.schema.game.server.data.simulation.npc.NPCFaction.NPCFactionControlCommandType;

public class NPCFactionControlCommand extends SimpleCommand<NPCFactionControlCommandType>{

	public int factionId;

	public NPCFactionControlCommand(int factionId, NPCFactionControlCommandType command, Object... args) {
		super(command, args);
		this.factionId =  factionId;
	}

	public NPCFactionControlCommand() {
	}

	@Override
	protected void checkMatches(NPCFactionControlCommandType command, Object[] args) {
		command.checkMatches(args);		
	}
	
	@Override
	public void serialize(DataOutput buffer) throws IOException {
		super.serialize(buffer);
		buffer.writeInt(factionId);
	}

	@Override
	public void deserialize(DataInput buffer, int updateSenderStateId) throws IOException {
		super.deserialize(buffer, updateSenderStateId);
		factionId = buffer.readInt();
	}
	
}
