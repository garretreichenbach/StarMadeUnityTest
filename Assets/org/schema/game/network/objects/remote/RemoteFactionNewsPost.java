package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.data.player.faction.FactionNewsPost;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteFactionNewsPost extends RemoteField<FactionNewsPost> {

	public RemoteFactionNewsPost(FactionNewsPost entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteFactionNewsPost(FactionNewsPost entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_INT;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {

		int factionId = stream.readInt();
		String op = stream.readUTF();
		long date = stream.readLong();
		String message = stream.readUTF();
		String topic = stream.readUTF();
		int permission = stream.readInt();
		boolean delete = stream.readBoolean();
		get().set(factionId, op, date, topic, message, permission, delete);

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {

		buffer.writeInt(get().getFactionId());
		buffer.writeUTF(get().getOp());
		buffer.writeLong(get().getDate());
		buffer.writeUTF(get().getMessage());
		buffer.writeUTF(get().getTopic());
		buffer.writeInt(get().getPermission());
		buffer.writeBoolean(get().isDelete());

		return byteLength();
	}

}
