package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.data.player.faction.FactionInvite;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteFactionInvitation extends RemoteField<FactionInvite> {

	public RemoteFactionInvitation(FactionInvite entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteFactionInvitation(FactionInvite entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_INT;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {
		String from = stream.readUTF();
		String to = stream.readUTF();
		int facId = stream.readInt();
		long date = stream.readLong();

		get().set(from, to, facId, date);

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {

		buffer.writeUTF(get().getFromPlayerName());
		buffer.writeUTF(get().getToPlayerName());
		buffer.writeInt(get().getFactionUID());
		buffer.writeLong(get().getDate());

		return byteLength();
	}

}
