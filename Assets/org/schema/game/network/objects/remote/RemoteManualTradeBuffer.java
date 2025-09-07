package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.common.controller.trade.manualtrade.ManualTrade;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteBuffer;

public class RemoteManualTradeBuffer extends RemoteBuffer<RemoteManualTrade> {

	public RemoteManualTradeBuffer(NetworkObject synchOn) {
		super(RemoteManualTrade.class, synchOn);
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {

		int collectionSize = buffer.readShort();

		for (int n = 0; n < collectionSize; n++) {
			RemoteManualTrade instance = new RemoteManualTrade(new ManualTrade(), onServer);
			instance.fromByteStream(buffer, updateSenderStateId);
			getReceiveBuffer().add(instance);
		}

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		//add size of collection
		assert (get().size() < Short.MAX_VALUE);
		buffer.writeShort(get().size());

		for (RemoteManualTrade remoteField : get()) {
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
