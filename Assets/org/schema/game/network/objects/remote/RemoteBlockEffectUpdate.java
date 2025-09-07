package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.data.blockeffects.updates.BlockEffectUpdate;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteBlockEffectUpdate extends RemoteField<BlockEffectUpdate> {
	public RemoteBlockEffectUpdate(BlockEffectUpdate entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteBlockEffectUpdate(BlockEffectUpdate entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_INT;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {

		set(BlockEffectUpdate.decodeEffect(stream));
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {

		get().encodeEffect(buffer);

		return 1;
	}

}
