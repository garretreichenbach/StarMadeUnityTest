package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.data.BlockBulkSerialization;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteBlockBulk extends RemoteField<BlockBulkSerialization> {

	public RemoteBlockBulk(BlockBulkSerialization entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteBlockBulk(BlockBulkSerialization entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_INT;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {
		get().deserialize(stream);

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		get().serialize(buffer);
		return 1;
	}
}