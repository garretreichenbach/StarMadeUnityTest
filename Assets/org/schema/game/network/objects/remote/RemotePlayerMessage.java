package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.data.player.playermessage.PlayerMessage;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemotePlayerMessage extends RemoteField<PlayerMessage> {
	public RemotePlayerMessage(PlayerMessage entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemotePlayerMessage(PlayerMessage entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_INT;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {

		set(PlayerMessage.decode(stream));
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {

		get().encode(buffer);

		return 1;
	}

}
