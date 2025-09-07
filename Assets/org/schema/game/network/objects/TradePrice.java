package org.schema.game.network.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.SerializationInterface;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;

public class TradePrice implements SerializationInterface, TradePriceInterface{
	private short type;
	private int amount;
	private int price;
	private int limit;
	private boolean sell;
	
	public TradePrice(short type, int amount, int price, int limit, boolean sell) {
		this.type = type;
		this.amount = amount;
		this.price = price;
		this.sell = sell;
		this.limit = limit;
	}
	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		b.writeShort(type);
		b.writeInt(amount);
		b.writeInt(price);
		b.writeInt(limit);
		b.writeBoolean(sell);
	}
	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
			boolean isOnServer) throws IOException {
		type = b.readShort();
		amount = b.readInt();
		price = b.readInt();
		limit = b.readInt();
		sell = b.readBoolean();
	}
	@Override
	public short getType() {
		return type;
	}
	@Override
	public int getAmount() {
		return amount;
	}
	@Override
	public int getPrice() {
		return price;
	}
	@Override
	public int getLimit() {
		return limit;
	}
	@Override
	public boolean isSell() {
		return sell;
	}
	@Override
	public int hashCode() {
		return (sell ? 100000 : 1) * type;
	}
	@Override
	public boolean equals(Object obj) {
		return ((TradePriceInterface)obj).isSell() == sell && ((TradePriceInterface)obj).getType() == type;
	}
	@Override
	public ElementInformation getInfo() {
		return ElementKeyMap.getInfoFast(type);
	}
	@Override
	public void setAmount(int amount) {
		this.amount = amount;
	}
	@Override
	public boolean isBuy() {
		return !sell;
	}
	public void setLimit(int limit) {
		this.limit = limit;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	public void setSell(boolean b) {
		this.sell = b;
	}
	
}
