package org.schema.game.common.controller;

import org.schema.game.network.objects.remote.RemoteCompressedShopPricesBuffer;
import org.schema.game.network.objects.remote.RemoteShopOptionBuffer;
import org.schema.game.network.objects.remote.RemoteTradePriceBuffer;
import org.schema.game.network.objects.remote.RemoteTradePriceSingleBuffer;
import org.schema.schine.network.objects.remote.RemoteBooleanPrimitive;
import org.schema.schine.network.objects.remote.RemoteByteBuffer;
import org.schema.schine.network.objects.remote.RemoteLongPrimitive;

public interface ShopNetworkInterface {
	public RemoteTradePriceSingleBuffer getPriceModifyBuffer();


	public RemoteCompressedShopPricesBuffer getCompressedPricesUpdateBuffer();

	public RemoteLongPrimitive getShopCredits();

	public RemoteBooleanPrimitive getInfiniteSupply();
	
	public RemoteBooleanPrimitive getTradeNodeOn();

	public RemoteByteBuffer getTradeNodeOnRequest();

	public RemoteShopOptionBuffer getShopOptionBuffer();

	public RemoteTradePriceBuffer getPricesUpdateBuffer();


	public RemoteLongPrimitive getTradePermission();
	public RemoteLongPrimitive getLocalPermission();
}
