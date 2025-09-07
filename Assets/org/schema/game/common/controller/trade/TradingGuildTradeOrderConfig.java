package org.schema.game.common.controller.trade;

import org.schema.game.common.controller.elements.shop.ShopElementManager;

public class TradingGuildTradeOrderConfig extends TradeOrderConfig{

	@Override
	public double getCargoPerShip() {
		return ShopElementManager.TRADING_GUILD_CARGO_PER_SHIP;
	}

	@Override
	public long getCostPerSystem() {
		return ShopElementManager.TRADING_GUILD_COST_PER_SYSTEM;
	}

	@Override
	public long getCostPerCargoShip() {
		return ShopElementManager.TRADING_GUILD_COST_PER_CARGO_SHIP;
	}
	@Override
	public double getTimePerSectorInSecs() {
		return ShopElementManager.TRADING_GUILD_TIME_PER_SECTOR_SEC;
	}

	@Override
	public double getProfitOfValue() {
		return ShopElementManager.TRADING_GUILD_PROFIT_OF_VALUE;
	}

	@Override
	public double getProfitOfValuePerSystem() {
		return ShopElementManager.TRADING_GUILD_PROFIT_OF_VALUE_PER_SYSTEM;
	}

	
	
	
	
	
}
