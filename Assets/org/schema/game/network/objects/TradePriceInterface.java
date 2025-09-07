package org.schema.game.network.objects;

import org.schema.game.common.data.element.ElementInformation;

public interface TradePriceInterface {
	public short getType() ;
	public int getAmount();
	public int getPrice();
	public boolean isSell();
	public boolean isBuy();
	public int getLimit();
	public ElementInformation getInfo();
	public void setAmount(int amount);
}
