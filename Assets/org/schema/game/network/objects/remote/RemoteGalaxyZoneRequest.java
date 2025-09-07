package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.network.objects.GalaxyZoneRequestAndAwnser;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteGalaxyZoneRequest extends RemoteField<GalaxyZoneRequestAndAwnser> {

	public RemoteGalaxyZoneRequest(GalaxyZoneRequestAndAwnser entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteGalaxyZoneRequest(GalaxyZoneRequestAndAwnser entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return 1;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {

		get().deserialize(stream, onServer);

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {

		get().serialize(buffer, onServer);

		return byteLength();
	}

}
