package org.schema.game.common.controller.trade;

import java.io.DataInput;
import java.io.IOException;

public class GenericTradeOrderConfig extends TradeOrderConfig{

	private double cargoPerShip;
	private long costPerSystem;
	private long costPerCargoShip;
	private double timePerSectorInSecs;
	private double profitOfValue;
	private double profitOfValuePerSystem;

	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
			boolean isOnServer) throws IOException {
		cargoPerShip = b.readDouble();
		costPerSystem = b.readLong();
		costPerCargoShip = b.readLong();
		timePerSectorInSecs = b.readDouble();
		profitOfValue = b.readDouble();
		profitOfValuePerSystem = b.readDouble();
	}

	@Override
	public double getCargoPerShip() {
		return cargoPerShip;
	}

	@Override
	public long getCostPerSystem() {
		return costPerSystem;
	}

	@Override
	public long getCostPerCargoShip() {
		return costPerCargoShip;
	}

	@Override
	public double getTimePerSectorInSecs() {
		return timePerSectorInSecs;
	}

	@Override
	public double getProfitOfValue() {
		return profitOfValue;
	}

	@Override
	public double getProfitOfValuePerSystem() {
		return profitOfValuePerSystem;
	}

}
