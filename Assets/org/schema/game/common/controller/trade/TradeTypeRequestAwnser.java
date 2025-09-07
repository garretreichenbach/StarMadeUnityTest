package org.schema.game.common.controller.trade;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.SerializationInterface;

public class TradeTypeRequestAwnser implements SerializationInterface{
	public long buyPrice = -1;
	public long sellPrice = -1;
	public long node = -1;
	public int availableBuy = -1;
	public int availableSell = -1;
	
	public TradeNodeClient nodeClient;
	short type = -1;
	public TradeTypeRequestAwnser(short type, long node, long buyPrice, int availableBuy, long sellPrice, 
			int availableSell) {
		super();
		this.buyPrice = buyPrice;
		this.sellPrice = sellPrice;
		this.node = node;
		this.availableBuy = availableBuy;
		this.availableSell = availableSell;
		this.type = type;
	}
	public TradeTypeRequestAwnser() {
		super();
	}
	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		b.writeLong(this.buyPrice);
		b.writeLong(this.sellPrice);
		b.writeLong(this.node);
		b.writeInt(this.availableBuy);
		b.writeInt(this.availableSell);
		b.writeShort(this.type);
	}
	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
			boolean isOnServer) throws IOException {
		this.buyPrice = b.readLong();
		this.sellPrice = b.readLong();
		this.node = b.readLong();
		this.availableBuy = b.readInt();
		this.availableSell = b.readInt();
		this.type = b.readShort();
	}
	@Override
	public String toString() {
		return "TradeTypeRequestAwnser [buyPrice=" + buyPrice + ", sellPrice="
				+ sellPrice + ", node=" + node + ", availableBuy="
				+ availableBuy + ", availableSell=" + availableSell
				+ ", nodeClient=" + nodeClient + ", type=" + type + "]";
	}
	
	
	
}
