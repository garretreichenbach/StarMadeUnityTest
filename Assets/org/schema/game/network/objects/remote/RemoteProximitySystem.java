package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.data.world.ClientProximitySystem;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteProximitySystem extends RemoteField<ClientProximitySystem> {

	public RemoteProximitySystem(ClientProximitySystem entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteProximitySystem(ClientProximitySystem entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return 3 * ByteUtil.SIZEOF_INT            //pos
				+ ClientProximitySystem.ALEN * ByteUtil.SIZEOF_BYTE            //type
				+ ClientProximitySystem.ALEN * ByteUtil.SIZEOF_LONG            //simStart
				;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {
		get().deserialize(stream);
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		get().serialize(buffer);
		return byteLength();
	}

}
