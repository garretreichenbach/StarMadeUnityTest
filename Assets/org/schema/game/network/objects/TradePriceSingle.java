package org.schema.game.network.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.SerializationInterface;
import org.schema.game.common.data.element.ElementInformation;

public class TradePriceSingle implements SerializationInterface, TradePriceInterface{

	public TradePriceInterface tp;
	
	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		b.writeShort(getType());
		b.writeBoolean(isBuy());
		b.writeInt(getPrice());
		b.writeInt(getAmount());
		b.writeInt(getLimit());
		
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
			boolean isOnServer) throws IOException {
		short type = b.readShort();
		boolean buy = b.readBoolean();
		int price = b.readInt();
		int amount = b.readInt();
		int limit = b.readInt();
		
		boolean sell = !buy;
		
		tp = new TradePrice(type, amount, price, limit, sell);
	}

	@Override
	public short getType() {
		return tp.getType();
	}

	@Override
	public int getAmount() {
		return tp.getAmount();
	}

	@Override
	public int getPrice() {
		return tp.getPrice();
	}

	@Override
	public boolean isSell() {
		return tp.isSell();
	}

	@Override
	public boolean isBuy() {
		return tp.isBuy();
	}

	@Override
	public int getLimit() {
		return tp.getLimit();
	}

	@Override
	public ElementInformation getInfo() {
		return tp.getInfo();
	}

	@Override
	public void setAmount(int amount) {
		tp.setAmount(amount);
	}

}
