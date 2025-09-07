package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.server.data.simulation.npc.geo.NPCSystemStub;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteNPCSystem extends RemoteField<NPCSystemStub> {

	public RemoteNPCSystem(NPCSystemStub entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteNPCSystem(NPCSystemStub entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return 1;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {

		get().deserialize(stream, updateSenderStateId, onServer);

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {

		get().serialize(buffer, onServer);

		return 1;
	}

}
