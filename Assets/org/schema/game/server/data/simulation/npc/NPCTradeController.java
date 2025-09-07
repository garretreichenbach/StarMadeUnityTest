package org.schema.game.server.data.simulation.npc;

import api.common.GameCommon;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.schema.common.util.CompareTools;
import org.schema.common.util.LogInterface;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.trade.*;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.world.GalaxyManager;
import org.schema.game.network.objects.TradePriceInterface;
import org.schema.game.network.objects.TradePrices;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.simulation.npc.geo.NPCSystemStructure;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class NPCTradeController implements LogInterface{
	
	private final NPCFaction faction;
	private final GameServerState state;
	private static int cacheIdGen;



	public NPCTradeController(NPCFaction fac, GameServerState state){
		this.state = state;
		this.faction = fac;
	}
	
	public TradeNodeStub getTradeNode(){
		return faction.getTradeNode();
	}
	
	public void tradeTurn(){
		GalaxyManager gman = state.getUniverse().getGalaxyManager();
		TradeManager tman = state.getGameState().getTradeManager();
		
		
		List<TradeActive> list = tman.getTradeActiveMap().getByFromFactionId().get(faction.getIdFaction());
		if(list != null && list.size() > 0){
			log("Not doing Trade since other Trades didn't finish yet", LogLevel.NORMAL);
			return;
		}
		
		NPCTradeParameters tradeParams = new NPCTradeParameters();
		
		synchronized(getClass()){
			tradeParams.cacheId = cacheIdGen++;
		}
		tradeParams.tradeConfig = new NPCTradeOrderConfig();
		
		
		
		List<TradeNodeStub> tradeNodes = new ObjectArrayList<TradeNodeStub>(gman.getTradeNodeDataById().values());
		tradeNodes.remove(getTradeNode());
		Collections.sort(tradeNodes, tradeDist);
		
		TradePrices ownPrices = getTradeNode().getTradePricesCached(state, tradeParams.cacheId);
		List<TradePriceInterface> prices = ownPrices.getCachedPrices(tradeParams.cacheId);
		int buyPrices = 0;
		int sellPrices = 0;
		for(TradePriceInterface pr : prices){
			if(pr.isBuy()){
				processBuy(tradeNodes, pr, tradeParams);
				buyPrices++;
			}else{
				processSell(tradeNodes, pr, tradeParams);
				sellPrices++;
			}
		}
		log("Considered "+buyPrices+" buy prices and "+sellPrices+" sell prices", LogLevel.NORMAL);
		
		
		Long2ObjectOpenHashMap<TradeOrder> omap = tradeParams.createOrder(state, faction.getConfig().minimumTradeValue, faction.getConfig().amountOfBlocksToTradeMax);
		
		for(TradeOrder order : omap.values()){
			order.logger = this;
			
			order.recalc();
			order.normalize(state, getConfig().maxOwnCreditsToUseForTrade, getConfig().maxOtherCreditsToUseForTrade);
			
			log("TRADE ORDER\n"+order.print(state), LogLevel.NORMAL);
			tman.executeTradeOrderServer(order);
		}
		
		
	}
	
	private void processBuy(List<TradeNodeStub> tradeNodes,
				TradePriceInterface pr, NPCTradeParameters tradeParams) {
			
			for(TradeNodeStub other : tradeNodes){
				if(other.getTradePermission() == TradeManager.PERM_ALL || (other.getTradePermission() == TradeManager.PERM_ALL_BUT_ENEMY && !faction.getEnemies().contains(GameCommon.getGameState().getFactionManager().getFaction(other.getFactionId())))) {
					TradePrices otherPrices = other.getTradePricesCached(state, tradeParams.cacheId);
					TradePriceInterface sellPrice = otherPrices.getCachedSellPrice(tradeParams.cacheId, pr.getType());

					if(sellPrice != null) {
						//node is selling what we want to buy, calculate cost


						int canBuyAmount = sellPrice.getAmount() - Math.max(0, sellPrice.getLimit());
						int wantToBuy = pr.getLimit() - pr.getAmount();


						int targetAmount = Math.min(canBuyAmount, wantToBuy);

						log("##BUY: We want to buy: " + ElementKeyMap.toString(pr.getType()) + " max " + targetAmount + " (WE HAVE EXISTING BUY PRICE); Faction " + other.getFactionId() + " HAS IT AS A SELLING PRICE", LogLevel.DEBUG);

						if(targetAmount > 0) {

							float systemDistance = Math.max(0f, Vector3i.getDisatance(getTradeNode().getSystem(), other.getSystem()));
							float additionalPricing = 1f + systemDistance * getConfig().tradePricePerSystem;

							double priceDifference = pr.getPrice() - (double) sellPrice.getPrice() * (double) additionalPricing;

							//					int tradeValue = (int)(priceDifference * (double)targetAmount);
							//					log("TARGET AMOUNT "+ElementKeyMap.toString(pr.getType())+": "+targetAmount+"; DIFF: "+priceDifference, LogLevel.DEBUG);
							tradeParams.addBuy(pr.getType(), getTradeNode(), other, targetAmount, priceDifference, sellPrice.getPrice());

						}
					} else {
						log("NOBUY: We wanted to buy: " + ElementKeyMap.toString(pr.getType()) + " (WE HAVE EXISTING BUY PRICE); Faction " + other.getFactionId() + " HAS NO SELLING PRICE FOR IT", LogLevel.DEBUG);

					}
				}
			}	
		}

	private void processSell(List<TradeNodeStub> tradeNodes,
			TradePriceInterface pr, NPCTradeParameters tradeParams) {
		
		for(TradeNodeStub t : tradeNodes){
			TradePrices otherPrices = t.getTradePricesCached(state, tradeParams.cacheId);
			TradePriceInterface buyPrice = otherPrices.getCachedBuyPrice(tradeParams.cacheId, pr.getType());
			
			if(buyPrice != null){
				//node is buying what we want to sell, calculate profit
				
				int canSellAmount = Math.max(0, buyPrice.getLimit()) - buyPrice.getAmount();
				
				int wantToSell = pr.getAmount() - pr.getLimit();
				
				int targetAmount = Math.min(canSellAmount, wantToSell);
				
//				log("SELL CHECK "+t.getStationName()+": "+ElementKeyMap.toString(pr.getType())+"; "+buyPrice.getAmount()+" / "+buyPrice.getLimit()+" -> "+canSellAmount + " ("+targetAmount+")", LogLevel.FINE);
				
				log("##SELL: We want to sell: "+ElementKeyMap.toString(pr.getType())+" max "+targetAmount+" (WE HAVE EXISTING SELL PRICE); Faction "+t.getFactionId()+" HAS IT AS A BUYING PRICE", LogLevel.DEBUG);
				
				if(targetAmount > 0){
					float systemDistance = Math.max(0f, Vector3i.getDisatance(getTradeNode().getSystem(), t.getSystem()));
					float additionalPricing = 1f + systemDistance * getConfig().tradePricePerSystem;
					
					double priceDifference = (double)buyPrice.getPrice()*(double)additionalPricing - pr.getPrice();
					
//					int tradeValue = (int)(priceDifference * (double)targetAmount);
					
					tradeParams.addSell(pr.getType(), getTradeNode(), t, targetAmount, priceDifference, buyPrice.getPrice());
					
				}
			}else{
				log("NOSELL: We wanted to sell: "+ElementKeyMap.toString(pr.getType())+" (WE HAVE EXISTING SELL PRICE); Faction "+t.getFactionId()+" HAS NO BUYING PRICE FOR IT", LogLevel.DEBUG);
				
			}
		}	
	}
	
	public long getCredits() {
		return faction.getCredits();
	}

	public NPCFactionConfig getConfig() {
		return faction.getConfig();
	}

	private Comparator<TradeNodeStub> tradeDist = (o1, o2) -> {

		long d1 = Vector3i.getDisatanceSquaredD(getHomeSector(), o1.getSector());
		long d2 = Vector3i.getDisatanceSquaredD(getHomeSector(), o2.getSector());

		return CompareTools.compare(d1, d2);
	};
	
	public Vector3i getHomeSector() {
		return faction.getHomeSector();
	}
	private class NPCTradeOrderConfig extends TradeOrderConfig{

		@Override
		public double getCargoPerShip() {
			return 10000;
		}

		@Override
		public long getCostPerSystem() {
			return 0;
		}

		@Override
		public long getCostPerCargoShip() {
			return 0;
		}

		@Override
		public double getTimePerSectorInSecs() {
			return 10;
		}

		@Override
		public double getProfitOfValue() {
			return 0;
		}

		@Override
		public double getProfitOfValuePerSystem() {
			return 0;
		}
		
	}
	@Override
	public void log(String s, LogLevel lvl) {
		faction.log("[TRADE]"+s, lvl);
	}
	
	public Inventory getInventory() {
		return faction.getInventory();
	}

	public void fillInitialInventoryAndTrading(Random random) {
		assert(getTradeNode().getEntityDBId() != Long.MIN_VALUE);
		random.setSeed(faction.getSeed() * 1243212343L);
		try {
			
			TradePrices p = new TradePrices(120);
			p.entDbId = getTradeNode().getEntityDBId();
			for(short type : ElementKeyMap.keySet){
				
				ElementInformation info = ElementKeyMap.getInfoFast(type);
				int amount = getConfig().getInitialAmount(type, random, faction.getSeed());
				
				if(amount > 0){
					getInventory().putNextFreeSlotWithoutException(type, amount, -1);
					setPrice(type, p);
				}
			}
			TradeNode.setPrices(state, getTradeNode(), p);
//			Sendable sendable = state.getLocalAndRemoteObjectContainer().getDbObjects().get(getTradeNode().getEntityDBId());
//			if(sendable != null){
//				assert(false);
//			}
			synchronized(state){
				state.getUniverse().tradeNodesDirty.enqueue(getTradeNode().getEntityDBId());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public NPCSystemStructure getStructure(){
		return faction.structure;
	}
	
	
	public void setPrice(short type, TradePrices p){
		ElementInformation info = ElementKeyMap.getInfoFast(type);
		
		int current = getInventory().getOverallQuantity(type);
		
		//Multi 1 => 2 times normal demand
		//TradeDemand is seen as 100% stock in Andy's graph
		int tradeDemand = faction.getTradeDemand(type, getConfig().tradeDemandMult);
		
		if(tradeDemand > 0){
			float filling = ((float)current / (float)tradeDemand);
			
			long defaultPrice = info.getPrice(true);
			
			int buyPrice = getConfig().getBuyPrice(filling, defaultPrice);
			int sellPrice = getConfig().getSellPrice(filling, defaultPrice);
			//Buy these blocks up to that amount
			int buyLimit = (int) (tradeDemand * getConfig().tradeBuyUpperPercentage);
			//Sell these blocks till you reach this amount
			int sellLimit = (int) (tradeDemand * getConfig().tradeSellLowerPercentage);
			
			
			if(buyPrice > 0){
				log(String.format("%-40s %-40s Price: %-5d Current stock: %-10d up to Limit: %-10d \tFilling: %-10.2f Demand: %-10d DefaultPrice: %-5d", "Set Buying Price for", ElementKeyMap.toString(type), buyPrice, current, buyLimit, filling, tradeDemand, defaultPrice), LogLevel.DEBUG);
				//log("Set Buying Price for "+ElementKeyMap.toString(type)+": "+buyPrice+"c (cur "+current+" lim up to "+buyLimit+"); fill: "+filling+"; demand: "+demand+"; defPrice "+defaultPrice, LogLevel.DEBUG);
				p.addBuy(type, current, buyPrice, buyLimit);
			}else{
				if(tradeDemand > 0){
					log(String.format("%-40s %-40s Price: %-5d Current stock: %-10d up to Limit: %-10d \tFilling: %-10.2f Demand: %-10d DefaultPrice: %-5d", "No Buying Price for demanded resource", ElementKeyMap.toString(type), buyPrice, current, buyLimit, filling, tradeDemand, defaultPrice), LogLevel.DEBUG);
					//log("No Buying Price for demanded resource "+ElementKeyMap.toString(type)+": "+buyPrice+"c (cur "+current+" lim up to "+buyLimit+"); fill: "+filling+"; demand: "+demand+"; defPrice "+defaultPrice, LogLevel.DEBUG);
				}
			}
			if(sellPrice > 0){
				log(String.format("%-40s %-40s Price: %-5d Current stock: %-10d up to Limit: %-10d \tFilling: %-10.2f Demand: %-10d DefaultPrice: %-5d", "Set Selling Price for", ElementKeyMap.toString(type), sellPrice, current, sellLimit, filling, tradeDemand, defaultPrice), LogLevel.DEBUG);
				//log("Set Selling Price for "+ElementKeyMap.toString(type)+": "+sellPrice+"c (cur "+current+" lim down to "+sellLimit+") fill: "+filling+"; demand: "+demand+"; defPrice "+defaultPrice, LogLevel.DEBUG);
				p.addSell(type, current, sellPrice, sellLimit);
			}else{
				if(tradeDemand > 0){
					log(String.format("%-40s %-40s Price: %-5d Current stock: %-10d up to Limit: %-10d \tFilling: %-10.2f Demand: %-10d DefaultPrice: %-5d", "No Selling Price for demanded resource",ElementKeyMap.toString(type), sellPrice, current, sellLimit, filling, tradeDemand, defaultPrice), LogLevel.DEBUG);
					//log("No Selling Price for demaded resource "+ElementKeyMap.toString(type)+": "+sellPrice+"c (cur "+current+" lim down to "+sellLimit+") fill: "+filling+"; demand: "+demand+"; defPrice "+defaultPrice, LogLevel.DEBUG);
				}
			}
		}else{
			log(String.format("%-40s %-40s", "No demand for resource", ElementKeyMap.toString(type)), LogLevel.DEBUG);
			//log("No Demand for resource "+ElementKeyMap.toString(type)+"", LogLevel.DEBUG);
			p.addSell(type, -1, 0, 0);
			p.addBuy(type, -1, 0, 0);
		}
		
		
	}

	public void fetchInventory() throws SQLException, IOException {
		if(faction.getTradeNode() != null && !state.getLocalAndRemoteObjectContainer().getDbObjects().containsKey(faction.getTradeNode().getEntityDBId())){
			TradePrices prices = faction.getTradeNode().getTradePricesInstance(state);
			List<TradePriceInterface> pr = prices.getPrices();
			IntOpenHashSet changed = new IntOpenHashSet();
			for(TradePriceInterface p :pr){
				int quant = faction.getInventory().getOverallQuantity(p.getType());
				if(quant != p.getAmount()){
					faction.getInventory().deleteAllSlotsWithType(p.getType(), changed);
					faction.getInventory().incExistingOrNextFreeSlotWithoutException(p.getType(), p.getAmount());
				}
			}
			if(changed.size() > 0){
				faction.getInventory().sendInventoryModification(changed);
			}
		}
	}
	public TradePrices recalcPrices() throws SQLException, IOException {
		if(getTradeNode() != null){
			fetchInventory();
			TradePrices p = new TradePrices(120);
			p.entDbId = getTradeNode().getEntityDBId();
			for(short type : ElementKeyMap.keySet){
				setPrice(type, p);
			}
			TradeNode.setPrices(state, getTradeNode(), p);
			
			synchronized(state){
				state.getUniverse().tradeNodesDirty.enqueue(getTradeNode().getEntityDBId());
			}
			return p;
		}else{
			System.err.println("Exception: "+faction+" has no trade node station");
		}
		return null;
	}
	
}
