package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.common.controller.ShoppingAddOn;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteBuffer;

public class RemoteCompressedShopPricesBuffer extends RemoteBuffer<RemoteCompressedShopPrices> {

	private ShoppingAddOn shop;

	public RemoteCompressedShopPricesBuffer(ShoppingAddOn shop, NetworkObject synchOn) {
		super(RemoteCompressedShopPrices.class, synchOn);
		this.shop = shop;
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {

		int collectionSize = buffer.readInt();

		for (int n = 0; n < collectionSize; n++) {
			RemoteCompressedShopPrices instance = new RemoteCompressedShopPrices(shop, onServer);
			instance.fromByteStream(buffer, updateSenderStateId);
			getReceiveBuffer().add(instance);
		}

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		//add size of collection
		buffer.writeInt(get().size());

		for (RemoteCompressedShopPrices remoteField : get()) {
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
