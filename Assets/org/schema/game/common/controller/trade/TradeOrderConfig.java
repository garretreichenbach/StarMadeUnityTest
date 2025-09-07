package org.schema.game.common.controller.trade;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.SerializationInterface;


public abstract class TradeOrderConfig implements SerializationInterface{
	
	
	public abstract double getCargoPerShip();
	public abstract long getCostPerSystem();
	public abstract long getCostPerCargoShip();
	public abstract double getTimePerSectorInSecs();
	public abstract double getProfitOfValue();
	public abstract double getProfitOfValuePerSystem();
	
	
	
	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		b.writeDouble(getCargoPerShip());
		b.writeLong(getCostPerSystem());
		b.writeLong(getCostPerCargoShip());
		b.writeDouble(getTimePerSectorInSecs());
		b.writeDouble(getProfitOfValue());
		b.writeDouble(getProfitOfValuePerSystem());
	}
	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
			boolean isOnServer) throws IOException {
		throw new IllegalArgumentException("Receiving part is only implemented by generic Config class");
	}
	
	
}
