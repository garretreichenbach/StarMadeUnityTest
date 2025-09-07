package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.server.data.CreatureSpawn;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteCreatureSpawnRequest extends RemoteField<CreatureSpawn> {

	public RemoteCreatureSpawnRequest(CreatureSpawn entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteCreatureSpawnRequest(CreatureSpawn entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return 1;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {

		set(new CreatureSpawn());
		get().deserialize(stream);
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		get().serialize(buffer);

		return byteLength();
	}

}
