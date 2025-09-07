package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.data.missile.updates.MissileUpdate;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteMissileUpdate extends RemoteField<MissileUpdate> {
	public RemoteMissileUpdate(MissileUpdate entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteMissileUpdate(MissileUpdate entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_INT;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {

		set(MissileUpdate.decodeMissile(stream));

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {

		get().encodeMissile(buffer);
		return 1;
	}

}
