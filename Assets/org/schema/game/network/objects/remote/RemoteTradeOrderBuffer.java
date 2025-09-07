package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.common.controller.trade.TradeOrder;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteBuffer;

public class RemoteTradeOrderBuffer extends RemoteBuffer<RemoteTradeOrder> {

	private final StateInterface state;
	public RemoteTradeOrderBuffer(NetworkObject synchOn, StateInterface state) {
		super(RemoteTradeOrder.class, synchOn);
		this.state = state;
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {

		int collectionSize = buffer.readShort();

		for (int n = 0; n < collectionSize; n++) {
			RemoteTradeOrder instance = new RemoteTradeOrder(new TradeOrder(state), onServer);
			instance.fromByteStream(buffer, updateSenderStateId);
			getReceiveBuffer().add(instance);
		}

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		//add size of collection
		assert (get().size() < Short.MAX_VALUE);
		buffer.writeShort(get().size());

		for (RemoteTradeOrder remoteField : get()) {
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
