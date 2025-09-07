package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.data.world.ClientProximitySector;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteProximitySector extends RemoteField<ClientProximitySector> {

	public RemoteProximitySector(ClientProximitySector entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteProximitySector(ClientProximitySector entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return 3 * ByteUtil.SIZEOF_INT            //pos
				+ ByteUtil.SIZEOF_INT            //secId
				+ ClientProximitySector.ALEN    //types
				+ ClientProximitySector.ALEN;    //planetTypes
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
