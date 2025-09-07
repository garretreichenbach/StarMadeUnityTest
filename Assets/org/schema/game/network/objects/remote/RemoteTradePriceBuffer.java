package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.network.objects.TradePrices;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteBuffer;

public class RemoteTradePriceBuffer extends RemoteBuffer<RemoteTradePrice> {

	public RemoteTradePriceBuffer(NetworkObject synchOn) {
		super(RemoteTradePrice.class, synchOn);
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {

		int collectionSize = buffer.readShort();

		for (int n = 0; n < collectionSize; n++) {
			RemoteTradePrice instance = new RemoteTradePrice(new TradePrices(128), onServer);
			instance.fromByteStream(buffer, updateSenderStateId);
			getReceiveBuffer().add(instance);
		}

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		//add size of collection
		assert (get().size() < Short.MAX_VALUE);
		buffer.writeShort(get().size());

		for (RemoteTradePrice remoteField : get()) {
			remoteField.toByteStream(buffer);
		}

		get().clear();

		return 1;

	}

	@Override
	protected void cacheConstructor() {
	}

	@Override
	public void clearReceiveBuffer() {
		getReceiveBuffer().clear();
	}

}
