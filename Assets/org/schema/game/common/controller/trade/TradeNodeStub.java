package org.schema.game.common.controller.trade;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import org.schema.common.SerializationInterface;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ShopInterface;
import org.schema.game.common.controller.trade.TradeManager.TradePerm;
import org.schema.game.common.controller.trade.TradeOrder.TradeOrderElement;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.network.objects.TradePrice;
import org.schema.game.network.objects.TradePriceInterface;
import org.schema.game.network.objects.TradePrices;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.objects.Sendable;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public class TradeNodeStub implements SerializationInterface{
	private long tradePermission = TradeManager.PERM_ALL_BUT_ENEMY;
	private long entityDBId = -1;
	protected Vector3i system = new Vector3i();
	private Set<String> owners;
	private String stationName;
	private int factionId;
	private double volume;
	private double capacity;
	private long credits;
	public boolean remove;
	protected Vector3i sector;
	private TradePrices tradePricesCached;
	private int cacheDate;
	
	public double getVolume() {
		return volume;
	}

	public void setVolume(double volume) {
		this.volume = volume;
	}

	public double getCapacity() {
		return capacity;
	}

	public void setCapacity(double capacity) {
		this.capacity = capacity;
	}

	public boolean isPermission(TradePerm perm){
		return (tradePermission & perm.val) == perm.val;
	}
	public TradePrices getTradePricesInstance(GameServerState state){
		TradePrices tradePrices = state.getDatabaseIndex().getTableManager().getTradeNodeTable().getTradePrices(getEntityDBId());
		assert(tradePrices.entDbId == getEntityDBId());
		return tradePrices;
	}
	public void cacheTradePrices(GameServerState state){
		tradePricesCached = getTradePricesInstance(state);
	}
	public TradePrices getTradePricesCached(GameServerState state, int cacheDate){
		if(tradePricesCached == null || cacheDate != this.cacheDate){
			cacheTradePrices(state);
			tradePricesCached.getCachedPrices(cacheDate);
		}
		return tradePricesCached;
	}
	public String getOwnerString(){
		owners = getOwners();
		if(owners.isEmpty()){
			return "";
		}
		StringBuffer b = new StringBuffer();
		int i = 0;
		int total = 0;
		for(String s : owners){
			if(total + (s.length()+1) >= 128){
				break;
			}
			b.append(s);
			if(i < owners.size()-1){
				b.append(";");
			}
			total += s.length()+1;
			i++;
		}
		return b.toString();
	}
	public void parseOwnerString(String s){
		owners = new ObjectOpenHashSet<String>();
		String[] split = s.split(";");
		for(String o : split){
			owners.add(o);
		}
	}
	public long getTradePermission() {
		return tradePermission;
	}
	public void setTradePermission(long tradePermission) {
		this.tradePermission = tradePermission;
	}
	public InputStream getPricesInputStream() throws IOException {
		throw new IllegalArgumentException();
	}
	public long getEntityDBId() {
		return entityDBId;
	}
	public void setEntityDBId(long entityDBId) {
		this.entityDBId = entityDBId;
	}
	public Vector3i getSystem() {
		return system;
	}
	public void setSystem(Vector3i system) {
		this.system = system;
	}
	public Set<String> getOwners() {
		return owners;
	}
	public void setOwners(Set<String> owners) {
		this.owners = owners;
	}
	public String getStationName() {
		return stationName;
	}
	public void setStationName(String stationName) {
		this.stationName = stationName;
	}
	public int getFactionId() {
		return factionId;
	}
	public void setFactionId(int factionId) {
		this.factionId = factionId;
	}

	public void updateWith(TradeNodeStub s) {
		sector = new Vector3i(s.getSector());
		system = new Vector3i(s.getSystem());
		owners = s.getOwners();
		stationName = s.stationName;
		factionId = s.factionId;
		volume = s.volume;
		capacity = s.capacity;
		credits = s.credits;
	}
	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		b.writeBoolean(remove);
		b.writeLong(entityDBId);
		assert(entityDBId >= 0);
		if(!remove){
			getSystem().serialize(b);
			sector.serialize(b);
			b.writeUTF(getOwnerString());
			b.writeUTF(getStationName());
			b.writeInt(getFactionId());
			b.writeDouble(getVolume());
			b.writeDouble(getCapacity());
			b.writeLong(getCredits());
		}
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
			boolean isOnServer) throws IOException {
		remove = b.readBoolean();	
		entityDBId = b.readLong();
		if(!remove){
			system = Vector3i.deserializeStatic(b);
			sector = Vector3i.deserializeStatic(b);
			parseOwnerString(b.readUTF());
			stationName = b.readUTF();
			factionId = b.readInt();
			volume = b.readDouble();
			capacity = b.readDouble();
			credits = b.readLong();
		}
	}

	public void setCredits(long c) {
		this.credits = c;
	}
	public long getCredits() {
		return this.credits;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (entityDBId ^ (entityDBId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		TradeNodeStub other = (TradeNodeStub) obj;
		return getEntityDBId() == other.getEntityDBId();
	}
	public int getMaxOwn(boolean buy, TradePriceInterface f){
		return getMax(!buy, f);
	}
	public int getMax(boolean weWantToBuy, TradePriceInterface f){
		assert(f != null);
		assert(f.getInfo() != null);
		if(weWantToBuy){
			return f.getLimit() < 0 ? f.getAmount() : Math.max(0, f.getAmount() - f.getLimit());
		}else{
			double capLeft = getCapacity() - getVolume();
			int max = (int)Math.floor((Math.max(0.0, capLeft)) / f.getInfo().getVolume());
			
			if(f.getLimit() < 0){
				return Math.max(0, max);
			}else{
				return Math.max(0, Math.min(max, f.getLimit() - f.getAmount() ));
			}
		}
	}
	public static Short2ObjectOpenHashMap<TradePriceInterface> toMap(List<TradePriceInterface> m, boolean buy){
		Short2ObjectOpenHashMap<TradePriceInterface> map = new Short2ObjectOpenHashMap<TradePriceInterface>(m.size());
		
		for(TradePriceInterface p : m){
			if(buy != p.isSell()){
				map.put(p.getType(), p);
			}
		}
		return map;
	}
	
	@Override
	public String toString() {
		return getEntityDBId()+" : OWN "+getOwnerString()+" : "+getStationName()+" : "+getSector()+"; "+this.getClass().getSimpleName();
	}

	public void removeBoughtBlocks(TradeOrder t, List<TradePriceInterface> ofOther, GameServerState s) {
		List<TradeOrderElement> orders = t.getOrders();
		for(TradeOrderElement e : orders){
			if(e.isBuyOrder()){
				removeBlocks(e, ofOther);
			}
		}
		try {
			s.getDatabaseIndex().getTableManager().getTradeNodeTable().setTradePrices(getEntityDBId(), getVolume(), getCredits(), ofOther);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
	public void addBlocks(TradeActive t, List<TradePriceInterface> prices, GameServerState s) {
		
		Short2ObjectOpenHashMap<TradePriceInterface> bMap = new Short2ObjectOpenHashMap<TradePriceInterface>();
		Short2ObjectOpenHashMap<TradePriceInterface> sMap = new Short2ObjectOpenHashMap<TradePriceInterface>();
		for(TradePriceInterface p : prices){
			if(p.isBuy()){
				bMap.put(p.getType(), p);
			}else{
				sMap.put(p.getType(), p);
			}
		}
		for(short r : ElementKeyMap.keySet){
			short type = r;
			int amount = t.getBlocks().get(type);
			if(amount > 0){
				TradePriceInterface p = bMap.get(type);
				if(p != null){
					p.setAmount(p.getAmount() + amount);
//					System.err.println("ADDED AMOUNT TO "+p.getType()+"; "+p.isBuy()+" -> "+p.getAmount());
				}else{
					boolean sell = false;
					prices.add(new TradePrice(type, amount, -1, -1, sell));
				}
				p = sMap.get(type);
				if(p != null){
					p.setAmount(p.getAmount() + amount);
				}else{
					boolean sell = true;
					prices.add(new TradePrice(type, amount, -1, -1, sell));
				}
				double vol = ElementKeyMap.getInfo(type).getVolume() * amount;
				volume = getVolume() + vol;
			}
		}
		assert(getEntityDBId() > 0);
		try {
			s.getDatabaseIndex().getTableManager().getTradeNodeTable().setTradePrices(getEntityDBId(), getVolume(), getCredits(), prices);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
	public void removeBlocks(TradeOrderElement e, List<TradePriceInterface> prices) {
		boolean found = false;
		for(TradePriceInterface p : prices){
			if(p.getType() == e.type){
				found = true;
				double vol = ElementKeyMap.getInfo(e.type).getVolume() * e.amount;
				p.setAmount(p.getAmount() - e.amount);
				volume = getVolume() - vol;
			}
		}
		assert(found);
		
	}

	public void removeSoldBlocks(TradeOrder t, List<TradePriceInterface> pricesOfOwn, GameServerState s) {
		List<TradeOrderElement> orders = t.getOrders();
		for(TradeOrderElement e : orders){
			if(!e.isBuyOrder()){
				//System.err.println("REMOVING BLOCKS FOR SELL ORDER OF OWN: "+e.amount+"; ");
				removeBlocks(e, pricesOfOwn);
			}
		}		
		try {
			s.getDatabaseIndex().getTableManager().getTradeNodeTable().setTradePrices(getEntityDBId(), getVolume(), getCredits(), pricesOfOwn);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	public void modCredits(long credits, GameServerState s) {
		this.credits += credits;
		
		try {
			s.getDatabaseIndex().getTableManager().getTradeNodeTable().setTradeCredits(getEntityDBId(), getCredits());
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}


	public void setSector(Vector3i s) {
		this.sector = s;
		VoidSystem.getContainingSystem(sector, system);
	}
	public Vector3i getSector() {
		return sector;
	}

	public static void modifyCreditsServer(GameServerState state, long dbId, long newCredits) throws SQLException{
		synchronized(state){
			final boolean wasSynched = state.isSynched();
			if(!wasSynched){
				state.setSynched();
			}
			Sendable sendable = state.getLocalAndRemoteObjectContainer().getDbObjects().get(dbId);
			try{
				if(sendable != null && sendable instanceof ShopInterface){
					
					long mod = newCredits - ((ShopInterface)sendable).getShoppingAddOn().getCredits();
					((ShopInterface)sendable).getShoppingAddOn().modCredits(mod);
					return;
				}
			}finally{
				if(!wasSynched){
					state.setUnsynched();
				}
			}
		}
		state.getDatabaseIndex().getTableManager().getTradeNodeTable().setTradeCredits(dbId, newCredits);
	}
	
//	public static void priceRequest(GameServerState state, TradeNodeStub tradeNode, TradePriceInterface f, int price) throws IOException, SQLException {
//		ElementInformation info = ElementKeyMap.getInfoFast(f.getType());
//		sendPriceRequest(state, tradeNode, info, f.isBuy(), f.getAmount(), true, price, false, -1);
//	}
//	public static void limitRequest(GameServerState state, TradeNodeStub tradeNode, TradePriceInterface f, int limit) throws IOException, SQLException {
//		ElementInformation info = ElementKeyMap.getInfoFast(f.getType());
//		sendPriceRequest(state, tradeNode, info, f.isBuy(), f.getAmount(), false, 0, true, limit);
//	}
//	public static void priceLimitRequest(GameServerState state, TradeNodeStub tradeNode, TradePriceInterface f, int price, int limit) throws IOException, SQLException {
//		ElementInformation info = ElementKeyMap.getInfoFast(f.getType());
//		sendPriceRequest(state, tradeNode, info, f.isBuy(), f.getAmount(), true, price, true, limit);
//	}
//	private static void sendPriceRequest(GameServerState state, TradeNodeStub tradeNode, ElementInformation info, boolean buy, int amount, boolean setPrice, int price, boolean setLimit, int limit) throws IOException, SQLException{
//		long dbId = tradeNode.getEntityDBId();
//		synchronized(state){
//			
//			final boolean wasSynched = state.isSynched();
//			if(!wasSynched){
//				state.setSynched();
//			}
//			Sendable sendable = state.getLocalAndRemoteObjectContainer().getDbObjects().get(dbId);
//			try{
//				if(sendable != null && sendable instanceof ShopInterface){
//						
//					ShopInterface currentClosestShop = ((ShopInterface)sendable);
//
//					Price p = currentClosestShop.getPriceBasedOfQuantity(info);
//					
//					Price pr;
//					if(buy){
//						pr = getPrice(
//								info.getId(),
//								(short)-1,
//								setPrice ? price : p.amountBuy,
//								setLimit ? limit : p.buyDownTo,
//								p.priceTypeSell,
//								p.amountSell,
//								p.sellUpTo);
//					}else{
//						pr = getPrice(
//								info.getId(),
//								p.priceTypeBuy,
//								p.amountBuy,
//								p.buyDownTo,
//								(short)-1,
//								setPrice ? price : p.amountSell,
//								setLimit ? limit : p.sellUpTo);
//					}
//					currentClosestShop.getShoppingAddOn().modifyPriceServer(pr);
//					return;
//				}
//			}finally{
//				if(!wasSynched){
//					state.setUnsynched();
//				}
//			}
//		}
//		
//		DataInputStream tpStream = state.getDatabaseIndex().getTradePricesAsStream(dbId);
//		if(tpStream != null){
//				TradePrices pc = ShoppingAddOn.deserializeTradePrices(tpStream, true);
//				tpStream.close();
//				pc.changeOrAdd(buy, info.getId(), amount, price, limit);
//				
//				state.getDatabaseIndex().setTradePrices(dbId, tradeNode.getCredits(), tradeNode.getVolume(), tradeNode.getCapacity(), pc);
//		}
//	}
	public static void setPrices(GameServerState state, TradeNodeStub tradeNode,
			TradePrices pc) throws SQLException, IOException {
		
		
		long dbId = tradeNode.getEntityDBId();
		synchronized(state){
			
			final boolean wasSynched = state.isSynched();
			if(!wasSynched){
				state.setSynched();
			}
			Sendable sendable = state.getLocalAndRemoteObjectContainer().getDbObjects().get(dbId);
			try{
				if(sendable != null && sendable instanceof ShopInterface){
						
					ShopInterface currentClosestShop = ((ShopInterface)sendable);
					List<TradePriceInterface> prices = pc.getPrices();
					
					currentClosestShop.getShoppingAddOn().putAllPrices(prices);
					currentClosestShop.getShoppingAddOn().sendPrices();
					
					return;
				}
			}finally{
				if(!wasSynched){
					state.setUnsynched();
				}
			}
		}
		
		state.getDatabaseIndex().getTableManager().getTradeNodeTable().setTradePrices(tradeNode.getEntityDBId(), tradeNode.getCredits(), tradeNode.getVolume(), tradeNode.getCapacity(), pc);
	}
	
	
}
