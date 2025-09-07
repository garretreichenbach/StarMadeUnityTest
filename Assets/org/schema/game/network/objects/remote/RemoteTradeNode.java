package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.common.controller.trade.TradeNodeStub;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteTradeNode extends RemoteField<TradeNodeStub> {

	public RemoteTradeNode(TradeNodeStub entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteTradeNode(TradeNodeStub entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return 1;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {

		get().deserialize(stream, updateSenderStateId, onServer);

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {

		get().serialize(buffer, onServer);

		return byteLength();
	}

}
