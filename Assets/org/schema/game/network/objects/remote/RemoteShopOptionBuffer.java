package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.common.controller.ShopOption;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteBuffer;

public class RemoteShopOptionBuffer extends RemoteBuffer<RemoteShopOption> {

	public RemoteShopOptionBuffer(NetworkObject synchOn) {
		super(RemoteShopOption.class, synchOn);
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {
		int collectionSize = buffer.readShort();
		for (int n = 0; n < collectionSize; n++) {
			RemoteShopOption instance = new RemoteShopOption(new ShopOption(), onServer);
			instance.fromByteStream(buffer, updateSenderStateId);
			getReceiveBuffer().add(instance);
		}
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		//add size of collection
		assert (get().size() < Short.MAX_VALUE);
		buffer.writeShort(get().size());

		for (RemoteShopOption remoteField : get()) {
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
