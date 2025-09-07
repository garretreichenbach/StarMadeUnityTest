package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.data.player.CrewFleetRequest;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteCrewFleet extends RemoteField<CrewFleetRequest> {

	public RemoteCrewFleet(CrewFleetRequest entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteCrewFleet(CrewFleetRequest entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_INT;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {
		get().mode = stream.readByte();
		get().type = stream.readByte();
		get().ai = stream.readUTF();

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		buffer.writeByte(get().mode);
		buffer.writeByte(get().type);
		buffer.writeUTF(get().ai);

		return 1;
	}

}
