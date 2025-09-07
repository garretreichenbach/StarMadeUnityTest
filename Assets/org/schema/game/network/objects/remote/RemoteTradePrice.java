package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.network.objects.TradePrices;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteTradePrice extends RemoteField<TradePrices> {
	public RemoteTradePrice(TradePrices entry, boolean synchOn) {
		super(entry, synchOn);
		assert(!synchOn || entry.entDbId != Long.MIN_VALUE);
	}

	public RemoteTradePrice(TradePrices entry, NetworkObject synchOn) {
		super(entry, synchOn);
		assert(!synchOn.isOnServer() || entry.entDbId != Long.MIN_VALUE);
	}

	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_INT;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {
		get().deserialize(stream, updateSenderStateId, onServer);
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		get().serialize(buffer, onServer);

		return 1;
	}

}
