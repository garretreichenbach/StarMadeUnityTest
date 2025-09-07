package org.schema.game.server.data.simulation.npc.geo;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.schema.game.common.controller.ShoppingAddOn;
import org.schema.game.common.controller.trade.TradeNodeStub;
import org.schema.game.network.objects.TradePrices;
import org.schema.game.server.data.simulation.npc.NPCFaction;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;

public class NPCTradeNode extends TradeNodeStub{
	
	private final NPCFaction faction;
	public NPCTradeNode(NPCFaction faction){
		this.faction = faction;
	}
	
	private final TradePrices prices = new TradePrices(32);
	@Override
	public InputStream getPricesInputStream() throws IOException {
		FastByteArrayOutputStream b = new FastByteArrayOutputStream(10*1024);
			
		ShoppingAddOn.serializeTradePrices(new DataOutputStream(b), true, prices, getEntityDBId());
		
		return new FastByteArrayInputStream(b.array, 0, (int)b.length());
	}
	public TradePrices getPrices() {
		return prices;
	}
	
	@Override
	public double getVolume() {
		this.setVolume(faction.getInventory().getVolume());
		return super.getVolume();
	}


	@Override
	public double getCapacity() {
		this.setCapacity(faction.getInventory().getCapacity());
		return super.getCapacity();
		
	}

}
