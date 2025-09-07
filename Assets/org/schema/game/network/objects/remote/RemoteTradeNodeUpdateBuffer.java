package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.trade.TradeNode;
import org.schema.game.common.controller.trade.TradeNodeClient;
import org.schema.game.common.controller.trade.TradeNodeStub;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteBuffer;

public class RemoteTradeNodeUpdateBuffer extends RemoteBuffer<RemoteTradeNode> {

	private StateInterface state;

	public RemoteTradeNodeUpdateBuffer(NetworkObject synchOn, StateInterface state) {
		super(RemoteTradeNode.class, synchOn);
		this.state = state;
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {

		int collectionSize = buffer.readInt();

		for (int n = 0; n < collectionSize; n++) {
			boolean isFullNode = buffer.readBoolean();
			TradeNodeStub na;
			if(!onServer){
				na = new TradeNodeClient((GameClientState)state);
			}else{
				na = isFullNode ? new TradeNode() : new TradeNodeStub();
			}
			RemoteTradeNode instance = new RemoteTradeNode(na, onServer);
			instance.fromByteStream(buffer, updateSenderStateId);
			getReceiveBuffer().add(instance);
		}

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		int size = 0;
		synchronized (get()) {
			//add size of collection
			buffer.writeInt(get().size());
			size += ByteUtil.SIZEOF_INT;

			for (RemoteTradeNode remoteField : get()) {
				buffer.writeBoolean(remoteField.get() instanceof TradeNode);
				size += remoteField.toByteStream(buffer);
			}

			get().clear();

		}
		return size;

	}

	@Override
	protected void cacheConstructor() {
	}

	@Override
	public void clearReceiveBuffer() {
		getReceiveBuffer().clear();
	}

}
