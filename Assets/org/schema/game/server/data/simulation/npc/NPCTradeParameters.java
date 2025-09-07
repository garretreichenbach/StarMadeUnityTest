package org.schema.game.server.data.simulation.npc;

import java.util.Collections;
import java.util.List;

import org.schema.common.util.CompareTools;
import org.schema.game.common.controller.trade.TradeNodeStub;
import org.schema.game.common.controller.trade.TradeOrder;
import org.schema.game.common.controller.trade.TradeOrderConfig;
import org.schema.game.network.objects.TradePriceInterface;
import org.schema.game.network.objects.TradePrices;
import org.schema.game.server.data.GameServerState;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectCollection;

public class NPCTradeParameters {
	public int maxAmount;
	public int currentAmount;
	public int cacheId;
	TradeOrderConfig tradeConfig;
	public final List<Transaction> buysAvailable = new ObjectArrayList<Transaction>(); 
	public final List<Transaction> sellsAvailable = new ObjectArrayList<Transaction>(); 
	
	public class Transaction implements Comparable<Transaction>{
		short type; 
		TradeNodeStub otherNode; 
		int targetAmount;
		double priceDifference;
		TradeNodeStub own;
		private boolean buy;
		public Transaction(short type, boolean buy, TradeNodeStub own,
				TradeNodeStub other, int targetAmount, double priceDifference, long itemPrice) {
			super();
			this.type = type;
			this.own = own;
			this.buy = buy;
			this.otherNode = other;
			this.targetAmount = targetAmount;
			this.priceDifference = priceDifference;
		}
		
		public int getTradeValue(){
			return (int)(priceDifference * targetAmount);
		}
		@Override
		public int compareTo(Transaction o) {
			return CompareTools.compare(o.getTradeValue(), getTradeValue());
		}
		
		public TradeOrder addOrCreateTradeOrders(GameServerState state, Long2ObjectOpenHashMap<TradeOrder> orderMap){
			TradeOrder order = orderMap.get(otherNode.getEntityDBId());
			if(order == null){
				order = new TradeOrder(state, tradeConfig, own.getEntityDBId(), own.getSystem(), otherNode.getSystem(), otherNode);
				orderMap.put(otherNode.getEntityDBId(), order);
			}
			boolean recalc = false;
			assert(targetAmount > 0);
			if(buy){
				
				TradePrices c = otherNode.getTradePricesCached(state, cacheId);
				List<TradePriceInterface> cachedPrices = c.getCachedPrices(cacheId);
				
				assert(c.cachedSellPrices.containsKey(type));
				
				order.addOrChangeBuy(type, targetAmount, recalc);
				assert(!order.isEmpty());
			}else{
				TradePrices c = otherNode.getTradePricesCached(state, cacheId);
				List<TradePriceInterface> cachedPrices = c.getCachedPrices(cacheId);
				assert(c.cachedBuyPrices.containsKey(type));
				
				order.addOrChangeSell(type, targetAmount, recalc);
				assert(!order.isEmpty());
			}
			
			return order;
		}
	}
	
	public Long2ObjectOpenHashMap<TradeOrder> createOrder(GameServerState state, int minTradeValue, int amountToTrade){
		sortByTradeValue();
		
		Long2ObjectOpenHashMap<TradeOrder> orders = new Long2ObjectOpenHashMap<TradeOrder>();
		int amountBought = 0;
		for(int i = 0; i < buysAvailable.size(); i++){
			Transaction transaction = buysAvailable.get(i);
			
			
			if(transaction.getTradeValue() >= minTradeValue){
				TradeOrder order = transaction.addOrCreateTradeOrders(state, orders);
				assert(!order.isEmpty());
				amountBought += transaction.targetAmount;
				if(amountBought >= amountToTrade){
					break;
				}
			}
		}
		int amountSold = 0;
		for(int i = 0; i < sellsAvailable.size(); i++){
			Transaction transaction = sellsAvailable.get(i);
			
			if(transaction.getTradeValue() >= minTradeValue){
				transaction.addOrCreateTradeOrders(state, orders);
				
				amountSold += transaction.targetAmount;
				if(amountSold >= amountToTrade){
					break;
				}
			}
		}
		assert(ordersNonEmpty(orders.values()));
		return orders;
		
	}
	private boolean ordersNonEmpty(ObjectCollection<TradeOrder> values) {
		for(TradeOrder o : values){
			if(o.isEmpty()){
				return false;
			}
		}
		return true;
	}
	public void sortByTradeValue(){
		Collections.sort(buysAvailable);
		Collections.sort(sellsAvailable);
	}
	
	
	public void addBuy(short type, TradeNodeStub own, TradeNodeStub other, int targetAmount, double priceDifference, long itemPrice) {
		add(true, type, own, other, targetAmount, priceDifference, itemPrice);
	}
	public void addSell(short type, TradeNodeStub own, TradeNodeStub other, int targetAmount, double priceDifference, long itemPrice) {
		add(false, type, own, other, targetAmount, priceDifference, itemPrice);
	}
	public void add(boolean buy, short type, TradeNodeStub own, TradeNodeStub t, int targetAmount, double priceDifference, long itemPrice) {
		Transaction tr = new Transaction(type, buy, own, t, targetAmount, priceDifference, itemPrice);
		if(buy){
			buysAvailable.add(tr);
		}else{
			sellsAvailable.add(tr);
		}
	}
}
