package org.schema.game.common.controller;

import org.schema.game.network.objects.NetworkEntityProvider;
import org.schema.schine.network.objects.Sendable;

public interface NetworkListenerEntity extends Sendable{
	public void setClientId(int id);
	public int getClientId();
	/**
	 * same id as the client it belongs to
	 * @param id
	 */
	@Override
	public void setId(int id);
	
	/**
	 * ID
	 * @return same id as the client it belongs to
	 */
	@Override
	public int getId();
	
	
	@Override
	public NetworkEntityProvider getNetworkObject();
	public boolean isSendTo();
}
