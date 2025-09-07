package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.data.player.faction.FactionRoles;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteFactionRoles extends RemoteField<FactionRoles> {

	public RemoteFactionRoles(FactionRoles entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteFactionRoles(FactionRoles entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_INT;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {
		get().factionId = stream.readInt();
		get().senderId = updateSenderStateId;
		for (int i = 0; i < FactionRoles.ROLE_COUNT; i++) {
			get().getRoles()[i].role = stream.readLong();
			get().getRoles()[i].name = stream.readUTF();
		}
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		buffer.writeInt(get().factionId);
		for (int i = 0; i < FactionRoles.ROLE_COUNT; i++) {
			buffer.writeLong(get().getRoles()[i].role);
			buffer.writeUTF(get().getRoles()[i].name);
		}

		return byteLength();
	}

}
