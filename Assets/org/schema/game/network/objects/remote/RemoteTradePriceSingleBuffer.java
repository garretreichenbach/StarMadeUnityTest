package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.network.objects.TradePriceSingle;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteBuffer;

public class RemoteTradePriceSingleBuffer extends RemoteBuffer<RemoteTradePriceSingle> {

	public RemoteTradePriceSingleBuffer(NetworkObject synchOn) {
		super(RemoteTradePriceSingle.class, synchOn);
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {

		int collectionSize = buffer.readShort();

		for (int n = 0; n < collectionSize; n++) {
			RemoteTradePriceSingle instance = new RemoteTradePriceSingle(new TradePriceSingle(), onServer);
			instance.fromByteStream(buffer, updateSenderStateId);
			getReceiveBuffer().add(instance);
		}

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		//add size of collection
		assert (get().size() < Short.MAX_VALUE);
		buffer.writeShort(get().size());

		for (RemoteTradePriceSingle remoteField : get()) {
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
