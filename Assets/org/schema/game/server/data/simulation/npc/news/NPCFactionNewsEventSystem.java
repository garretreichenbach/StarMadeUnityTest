package org.schema.game.server.data.simulation.npc.news;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.util.linAlg.Vector3i;

public abstract class NPCFactionNewsEventSystem extends NPCFactionNewsEvent{
	public Vector3i system = new Vector3i();
	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		super.serialize(b, isOnServer);
		system.serialize(b);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
			boolean isOnServer) throws IOException {
		super.deserialize(b, updateSenderStateId, isOnServer);
		system.deserialize(b);
	}


}
