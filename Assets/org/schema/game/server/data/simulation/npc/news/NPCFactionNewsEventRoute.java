package org.schema.game.server.data.simulation.npc.news;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.util.linAlg.Vector3i;

public abstract class NPCFactionNewsEventRoute extends NPCFactionNewsEvent{
	public Vector3i from = new Vector3i();
	public Vector3i to = new Vector3i();
	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		super.serialize(b, isOnServer);
		from.serialize(b);
		to.serialize(b);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
			boolean isOnServer) throws IOException {
		super.deserialize(b, updateSenderStateId, isOnServer);
		from.deserialize(b);
		to.deserialize(b);
	}


}
