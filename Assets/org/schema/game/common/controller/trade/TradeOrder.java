package org.schema.game.common.controller.trade;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import org.schema.common.SerializationInterface;
import org.schema.common.util.LogInterface;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.controller.ShopInterface;
import org.schema.game.common.controller.ShoppingAddOn;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.world.GalaxyManager;
import org.schema.game.network.objects.TradePriceInterface;
import org.schema.game.network.objects.TradePrices;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;
import org.schema.schine.network.StateInterface;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public class TradeOrder extends GUIObservable implements SerializationInterface, LogInterface{
	
	private TradeOrderConfig config;
	public LogInterface logger;
	
	
	@Override
	public void log(String s, LogLevel l){
		if(logger != null){
			logger.log(s, l);
		}else{
			System.err.println("[TRADEORDER] "+s);
		}
	}
	public class TradeOrderElement implements SerializationInterface{
		public short type;
		public int amount;
		private boolean buyOrder;
		public boolean over;
		public boolean isBuyOrder(){
			return buyOrder;
		};
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + type * 123 * (buyOrder ? -234123 : 0);
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			TradeOrderElement other = (TradeOrderElement) obj;
			return type == other.type && buyOrder == other.buyOrder;
		}
		public TradeOrderElement(short type, int amount, boolean buy) {
			super();
			this.type = type;
			this.amount = amount;
			this.buyOrder = buy;
		}
		public TradeOrderElement(){}
		public ElementInformation getInfo() {
			return ElementKeyMap.getInfoFast(type);
		}
		public short getType() {
			return type;
		}
		@Override
		public void serialize(DataOutput b, boolean isOnServer) throws IOException {
			b.writeBoolean(buyOrder);
			b.writeShort(type);
			b.writeInt(amount);
			
		}
		@Override
		public void deserialize(DataInput b, int updateSenderStateId,
				boolean isOnServer) throws IOException {
			buyOrder = b.readBoolean();
			type = b.readShort();
			amount = b.readInt();			
		}
		public boolean isOverAmount(TradeNodeClient node) {
			return over;
		}
		@Override
		public String toString() {
			return "TradeOrderElement [type=" + type + ", amount=" + amount
					+ ", buy=" + buyOrder + ", over=" + over + "]";
		}
		
		
	}
	
	
	
	public TradeOrder(StateInterface state, TradeOrderConfig config, long fromDbId, Vector3i fromSys, Vector3i toSys, TradeNodeStub node){
		this(state);
		this.fromSystem = new Vector3i(fromSys); 
		this.toSystem = new Vector3i(toSys); 
		this.fromDbId = fromDbId;
		this.toDbId = node.getEntityDBId();
		this.config = config;
		assert(fromDbId > 0);
		assert(toDbId > 0);
	}
	public TradeOrder(StateInterface state){
		this.state = state;
	}
	private StateInterface state;
	public long toDbId;
	public long fromDbId;
	private Vector3i fromSystem;
	private Vector3i toSystem;
	private List<TradeOrderElement> orders = new ObjectArrayList<TradeOrderElement>();
	private double buyVolume;
	public ClientChannel clientChannelReceivedOn;
	private double sellVolume;
	private long usedPriceBuy;
	private long usedPriceSell;
	public boolean dirty;
	public int cacheId = Integer.MIN_VALUE;
	
	
	public int getTradeDistance(){
		return (int) Math.max(1, Math.ceil(Vector3i.getDisatance(fromSystem, toSystem)));
	}
	public long getTotalPrice(){
		return getPriceDifference()+getTradingGuildPrice();
	}
	public long getPricePerDistance(){
		return getTradeDistance() * config.getCostPerSystem();//ShopElementManager.TRADING_GUILD_COST_PER_SYSTEM;
	}
	
	public long getPriceDifference(){
		return (usedPriceBuy - usedPriceSell);
	}
	public long getTradeingGuildProfitTotal(){
		return getTradeingGuildProfit() + getTradeingGuildProfitPerSystem();
	}
	public long getTradeingGuildProfit(){
		return (long) ((usedPriceBuy + usedPriceSell) * config.getProfitOfValue());//ShopElementManager.TRADING_GUILD_PROFIT_OF_VALUE);
	}
	public long getTradeingGuildProfitPerSystem(){
		double absPrice = usedPriceBuy + usedPriceSell;
		
		double profitPerSystem = absPrice * config.getProfitOfValuePerSystem();//ShopElementManager.TRADING_GUILD_PROFIT_OF_VALUE_PER_SYSTEM;
		return (long) (profitPerSystem * getTradeDistance());
	}
	public long getTradeShipCost(){
		return (getUsedTradeShips() * config.getCostPerCargoShip());//ShopElementManager.TRADING_GUILD_COST_PER_CARGO_SHIP);
	}
	public int getUsedTradeShips(){
		return getUsedTradeShipsBuy() + getUsedTradeShipsSell();
	}
	public int getUsedTradeShipsBuy(){
		return (int) Math.max(0, Math.ceil(buyVolume / config.getCargoPerShip()));//ShopElementManager.TRADING_GUILD_CARGO_PER_SHIP));
	}
	public int getUsedTradeShipsSell(){
		return (int) Math.max(0, Math.ceil(sellVolume / config.getCargoPerShip()));//ShopElementManager.TRADING_GUILD_CARGO_PER_SHIP));
	}
	public long getDeliveryCost() {
		return getPricePerDistance() + getTradeShipCost();
	}
	public long getTradingGuildPrice() {
		return Math.max(getTradeingGuildProfitTotal(), getDeliveryCost());
	}
	
	
	private void calcVolume(Short2ObjectOpenHashMap<TradePriceInterface> buyMap, Short2ObjectOpenHashMap<TradePriceInterface> sellMap){
		buyVolume = 0D;
		sellVolume = 0D;
		for(int i = 0; i < orders.size(); i++){
			TradeOrderElement e = orders.get(i);
			assert(e.getInfo() != null);
			if(e.getInfo() != null){
			if(e.isBuyOrder()){
				buyVolume += e.getInfo().getVolume() * e.amount; 
			}else{
				sellVolume += e.getInfo().getVolume() * e.amount; 
			}
			}else{
				System.err.println("UNKNOWN BLOCK USED IN TRADE ORDER:::: "+e.type);
			}
		}
	}
	public void normalize(GameServerState state, float buyCredits, float sellCredits) {
		TradeNodeStub own = state.getUniverse().getGalaxyManager().getTradeNodeDataById().get(fromDbId);
		TradeNodeStub other = state.getUniverse().getGalaxyManager().getTradeNodeDataById().get(toDbId);
		
		long ownCredits = (long)((double)own.getCredits() * (double)buyCredits);
		long otherCredits = (long)((double)other.getCredits() * (double)sellCredits);
		
		
		if(usedPriceSell > otherCredits){
			
			
			double priceDiff = (double) otherCredits / (double) usedPriceSell;
			
			//reduce amount to match credits
			for(TradeOrderElement o : orders){
				if(!o.isBuyOrder()){
					int priceOtherBuysAt = other.getTradePricesCached(state, cacheId).getCachedBuyPrice(cacheId, o.type).getPrice();
					double creds = priceOtherBuysAt*o.amount * priceDiff;
					o.amount = (int)creds / priceOtherBuysAt;
				}
			}
		}
		
		//recalculate to get new total price
		recalc();
		
		if(getTotalPrice() > ownCredits){
			
			double priceDiff = (double) ownCredits / (double)getTotalPrice();
			
			//reduce amount to match credits
			for(TradeOrderElement o : orders){
				if(o.isBuyOrder()){
					int priceWeBuyAt = other.getTradePricesCached(state, cacheId).getCachedSellPrice(cacheId, o.type).getPrice();
					double creds = priceWeBuyAt*o.amount * priceDiff;
					o.amount = (int)creds / priceWeBuyAt;
				}
			}
		}
		recalc();
	}
	public void recalc(){
		boolean empty = isEmpty();
		Short2ObjectOpenHashMap<TradePriceInterface> buyMap;
		Short2ObjectOpenHashMap<TradePriceInterface> sellMap;
		
		TradeNodeStub ownNode;
		TradeNodeStub otherNode;
		if(state instanceof GameClientState){
			ownNode =   ((GameClientState)state).getController().getClientChannel().getGalaxyManagerClient().getTradeNodeDataById().get(fromDbId);
			otherNode = ((GameClientState)state).getController().getClientChannel().getGalaxyManagerClient().getTradeNodeDataById().get(toDbId);
		}else{
			ownNode =   ((GameServerState)state).getUniverse().getGalaxyManager().getTradeNodeDataById().get(fromDbId);
			otherNode = ((GameServerState)state).getUniverse().getGalaxyManager().getTradeNodeDataById().get(toDbId);
		}
		
		
		
		
		if(otherNode != null){
			if(otherNode instanceof TradeNodeClient){
				List<TradePriceInterface> tradePricesClient = ((TradeNodeClient)otherNode).getTradePricesClient();
				buyMap = TradeNodeStub.toMap(tradePricesClient, true);
				sellMap = TradeNodeStub.toMap(tradePricesClient, false);
				if(tradePricesClient.isEmpty()){
					this.dirty = true;
				}
			}else{
				TradePrices c = otherNode.getTradePricesCached((GameServerState) state, cacheId);
				List<TradePriceInterface> cachedPrices = c.getCachedPrices(cacheId);
				buyMap = c.cachedBuyPrices;
				sellMap = c.cachedSellPrices;
			}
		}else{
			assert(state != null);
			DataInputStream tpStream = ((GameServerState)state).getDatabaseIndex().getTableManager().getTradeNodeTable().getTradePricesAsStream(toDbId);
			if(tpStream != null){
				try {
					TradePrices pc = ShoppingAddOn.deserializeTradePrices(tpStream, true);
					tpStream.close();
					
					List<TradePriceInterface> prices = pc.getPrices();
					
					if(prices.isEmpty()){
						assert(false):toDbId;
					}
					buyMap = TradeNodeStub.toMap(prices, true);
					sellMap = TradeNodeStub.toMap(prices, false);
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}else{
				log(" Exception. Trade Prices of DB ID OF TRADE NOT FOUND: "+toDbId, LogLevel.ERROR);
				return;
			}
		}
		calcVolume(buyMap, sellMap);
		
		calcPrices(buyMap, sellMap);
		
		
		if(ownNode != null){
				calcStock(buyMap, sellMap, ownNode, otherNode);
		}else{
			TradeNodeStub fromNodeStub = ((GameServerState)state).getUniverse().getGalaxyManager().getTradeNodeDataById().get(fromDbId);
			TradeNodeStub toNodeStub = ((GameServerState)state).getUniverse().getGalaxyManager().getTradeNodeDataById().get(toDbId);
			if(fromNodeStub != null && toNodeStub != null){
				calcStock(buyMap, sellMap, fromNodeStub, toNodeStub );
			}
		}
		assert(empty == isEmpty()):empty+" -> "+isEmpty();
	}
	private void calcStock(Short2ObjectOpenHashMap<TradePriceInterface> buyMap,
			Short2ObjectOpenHashMap<TradePriceInterface> sellMap, TradeNodeStub fromNode, TradeNodeStub toNode) {
		for(int i = 0; i < orders.size(); i++){
			TradeOrderElement e = orders.get(i);
			
			if(e.isBuyOrder()){
				//since we are buying, check the sell map of other
				TradePriceInterface f = sellMap.get(e.type);
				e.over = toNode.getMax(e.buyOrder, f) < e.amount;
				
			}else{
				//since we are selling, check the buy map of other
				TradePriceInterface f = buyMap.get(e.type);
				e.over = fromNode.getMax(e.buyOrder, f) < e.amount;
			}
		}		
	}
	private void calcPrices(Short2ObjectOpenHashMap<TradePriceInterface> buyMapOfOther, Short2ObjectOpenHashMap<TradePriceInterface> sellMapOfOther){
		
		usedPriceBuy = 0L;
		usedPriceSell = 0L;
		long v = 0;
		for(int i = 0; i < orders.size(); i++){
			TradeOrderElement e = orders.get(i);
			
			if(e.isBuyOrder()){
				//since we are buying, check the sell price of other
				TradePriceInterface p = sellMapOfOther.get(e.type);
				if(p != null){
					assert(p.getPrice() >= 0);
					usedPriceBuy += (long) p.getPrice() * (long) e.amount; 
					assert(usedPriceBuy >= 0);
				}else{
					log("REMOVE NONEXISTENT BUY ORDER "+orders.get(i)+"; \nBUYMAP: "+buyMapOfOther+"\nSELLMAP: "+sellMapOfOther, LogLevel.DEBUG);
					orders.remove(i);
					i--;
					notifyObservers();
				}
			}else{
				//since we are selling, check the buy price of other
				TradePriceInterface p = buyMapOfOther.get(e.type);
				if(p != null){
					assert(p.getPrice() >= 0);
					usedPriceSell += (long) p.getPrice() * (long) e.amount; 
					assert(usedPriceSell >= 0);
				}else{
					log("REMOVE NONEXISTENT SELL ORDER "+orders.get(i)+"; \nBUYMAP: "+buyMapOfOther+"\nSELLMAP: "+sellMapOfOther, LogLevel.DEBUG);
					orders.remove(i);
					i--;
					notifyObservers();
				}
			}
		}
	}
	
	
	public void addOrChangeBuy(short type, int amount, boolean recalc){
		boolean buy = true;
		TradeOrderElement e = new TradeOrderElement(type, amount, buy);
		
		addOrChangeOrder(e, recalc);
	}
	public void addOrChangeSell(short type, int amount, boolean recalc){
		boolean buy = false;
		TradeOrderElement e = new TradeOrderElement(type, amount, buy);
		
		addOrChangeOrder(e, recalc);
		
		
	}
	public void addOrChangeOrder(TradeOrderElement e, boolean recalc){
		int indexOf = orders.indexOf(e);
		if(indexOf >= 0){
			if(e.amount == 0){
				orders.remove(indexOf);
			}else{
				orders.get(indexOf).amount = e.amount;
			}
		}else{
			orders.add(e);
			assert(!isEmpty()):orders;
		}
		notifyObservers();
		if(recalc){
			recalc();
		}
	}
	public boolean isEmpty() {
		return orders.isEmpty();
	}
	public List<TradeOrderElement> getElements() {
		return orders;
	}
	public double getBuyVolume() {
		return buyVolume;
	}
	public double getSellVolume() {
		return sellVolume;
	}
	public long getBuyPrice() {
		return usedPriceBuy;
	}
	public long getSellPrice() {
		return usedPriceSell;
	}
	public Vector3i getFromSystem() {
		return fromSystem;
	}
	public void setFromSystem(Vector3i fromSystem) {
		this.fromSystem = fromSystem;
	}
	public Vector3i getToSystem() {
		return toSystem;
	}
	public void setToSystem(Vector3i toSystem) {
		this.toSystem = toSystem;
	}
	
	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		b.writeLong(fromDbId);
		b.writeLong(toDbId);
		b.writeDouble(buyVolume);
		b.writeDouble(sellVolume);
		b.writeLong(usedPriceBuy);
		b.writeLong(usedPriceSell);
		fromSystem.serialize(b);
		toSystem.serialize(b);
		b.writeShort(orders.size());
		for(TradeOrderElement e : orders){
			e.serialize(b, isOnServer);
		}
		config.serialize(b, isOnServer);
	}
	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
			boolean isOnServer) throws IOException {
		fromDbId = b.readLong();
		toDbId = b.readLong();
		buyVolume = b.readDouble();
		sellVolume = b.readDouble();
		usedPriceBuy = b.readLong();
		usedPriceSell = b.readLong();
		fromSystem = Vector3i.deserializeStatic(b);
		toSystem = Vector3i.deserializeStatic(b);
		short size = b.readShort();
		for(int i = 0; i < size; i++){
			TradeOrderElement o = new TradeOrderElement();
			o.deserialize(b, updateSenderStateId, isOnServer);
			orders.add(o);
		}
		assert(fromDbId > 0);
		assert(toDbId > 0);
		
		config = new GenericTradeOrderConfig();
		config.deserialize(b, updateSenderStateId, isOnServer);

	}
	public boolean checkCanSellWithInventory(Short2ObjectOpenHashMap<TradePriceInterface> sellMap, TradeNodeStub tA) {
		for(TradeOrderElement e : orders){
			if(!e.buyOrder){
				
				TradePriceInterface p = sellMap.get(e.type);
				int amount = 0;
				if(p != null){
					amount = p.getAmount();
				}
				if(tA instanceof TradeNode){
					amount = ((TradeNode)tA).shop.getShopInventory().getOverallQuantity(e.type);
				}else if(tA instanceof TradeNodeClient){
					ShopInterface shop = ((TradeNodeClient)tA).getLoadedShop();
					if(shop != null){
						amount = shop.getShopInventory().getOverallQuantity(e.type);
					}
				}
				
				int limit = -1; //this is the initiating shop. it doesn't care about its own limit
				if(amount > 0){
//					log("CHECK: check HAVE TO SELL block: "+ElementKeyMap.toString(e.type)
//							+"; Stock: "+amount+"; Wanted: "+e.amount);
					
						
					
					if(amount < e.amount){
						log("ERROR: check HAVE TO SELL block (DENIED AMOUNT < WANTED): "+ElementKeyMap.toString(e.type)
								+"; Stock: "+amount+"; Wanted: "+e.amount, LogLevel.ERROR);
						return false;
					}
				}else{
					log("ERROR: check HAVE TO SELL block (DENIED AMOUNT == 0): "+ElementKeyMap.toString(e.type)
							+"; Stock: "+amount+"; Wanted: "+e.amount, LogLevel.ERROR);
					return false;
				}
			}
		}
		return true;
	}
	public boolean checkCanSellToOtherWithInventory(Short2ObjectOpenHashMap<TradePriceInterface> sellMap ) {
		//check against the buy prices of the other shop
		
		for(TradeOrderElement e : orders){
			if(!e.buyOrder){
				TradePriceInterface p = sellMap.get(e.type);
				if(p != null){
//					log("CHECK: check SELL block: "+ElementKeyMap.toString(e.type)
//							+"; Limit: "+p.getLimit()+"; Stock: "+p.getAmount()+"; Wanted: "+e.amount);
					
					
					if(p.getLimit() < 0){
						return true;
					}
					if(p.getAmount() + e.amount > p.getLimit()){
						log("ERROR: check SELL block (OVER LIMIT): "+ElementKeyMap.toString(e.type)
								+"; Limit: "+p.getLimit()+"; Stock: "+p.getAmount()+"; Wanted: "+e.amount, LogLevel.ERROR);
						return false;
					}
					
				}else{
					log("ERROR: check SELL block (NO PRICE SET): "+ElementKeyMap.toString(e.type)+"\n:"+sellMap, LogLevel.ERROR);
					return false;
				}
			}
		}
		return true;
	}
	public boolean checkCanFromOtherBuyWithInventory(Short2ObjectOpenHashMap<TradePriceInterface> otherShopoSellMap) {
		
		for(TradeOrderElement e : orders){
			if(e.buyOrder){
				TradePriceInterface p = otherShopoSellMap.get(e.type);
				if(p != null){
//					log("CHECK: check BUY block: "+ElementKeyMap.toString(e.type)
//							+"; Limit: "+p.getLimit()+"; Stock: "+p.getAmount()+"; Wanted: "+e.amount);
					if(p.getLimit() < 0 || p.getAmount() > p.getLimit()){
						int availableToBuy =  p.getAmount() - Math.max(0, p.getLimit());
						
						
						if(availableToBuy < e.amount){
							log("ERROR: check block: "+ElementKeyMap.toString(e.type)
									+"; Available: "+availableToBuy+"; Wanted: "+e.amount, LogLevel.ERROR);
							return false;
						}
					}else{
						log("ERROR: check block: "+ElementKeyMap.toString(e.type)+"; Amount over limit; Limit: "+p.getLimit()+"; Stock: "+p.getAmount()+"; Wanted: "+e.amount, LogLevel.ERROR);
						return false;
					}
				}else{
					log("ERROR: check block: "+ElementKeyMap.toString(e.type)+"; No Price found for wanted block", LogLevel.ERROR);
					return false;
				}
			}
		}
		return true;
	}
	public List<TradeOrderElement> getOrders() {
		return orders;
	}
	public void sendMsgError(Object[] msg) {
		if(clientChannelReceivedOn != null){
			clientChannelReceivedOn.getPlayer().sendServerMessagePlayerError(msg);
		}else if(state != null && state instanceof GameClientState){
			((GameClientState)state).getController().popupAlertTextMessage(StringTools.getFormatedMessage(msg), 0);
		}else{
//			log(StringTools.getFormatedMessage(msg));
		}
	}
	public void sendMsgInfo(Object[] msg) {
		if(clientChannelReceivedOn != null){
			clientChannelReceivedOn.getPlayer().sendServerMessagePlayerInfo(msg);
		}else if(state != null && state instanceof GameClientState){
			((GameClientState)state).getController().popupInfoTextMessage(StringTools.getFormatedMessage(msg), 0);
		}else{
//			log(StringTools.getFormatedMessage(msg));
		}
	}
	public String print(GameServerState state){
		GalaxyManager g = state.getUniverse().getGalaxyManager();
		TradeNodeStub from = g.getTradeNodeDataById().get(fromDbId);
		TradeNodeStub to = g.getTradeNodeDataById().get(toDbId);
		StringBuffer b = new StringBuffer();
		b.append("---- PRINTING TRADE ORDER START ----\n");
		b.append("FROM: "+from.getStationName()+" sys "+from.getSystem()+" sec "+from.getSector()+"; Capacity: "+StringTools.formatPointZero(from.getVolume())+" / "+StringTools.formatPointZero(from.getCapacity())+"; Credits: "+StringTools.formatSmallAndBig(from.getCredits())+"; "+"\n");
		b.append("TO  : "+to.getStationName()+" sys "+to.getSystem()+" sec "+to.getSector()+"; Capacity: "+StringTools.formatPointZero(to.getVolume())+" / "+StringTools.formatPointZero(to.getCapacity())+"; Credits: "+StringTools.formatSmallAndBig(to.getCredits())+"; "+"\n");
		b.append("-----"+"\n");
		b.append("BUY   VOLUME : "+StringTools.formatPointZero(buyVolume)+"\n");
		b.append("SELL  VOLUME : "+StringTools.formatPointZero(sellVolume)+"\n");
		b.append("-----"+"\n");
		b.append("BUY PRICE    : "+StringTools.formatSmallAndBig(usedPriceBuy)+"\n");
		b.append("SELL PRICE   : "+StringTools.formatSmallAndBig(usedPriceSell)+"\n");
		b.append("---------------------------------"+"\n");
		b.append("TOTAL PRICE  : "+StringTools.formatSmallAndBig(getTotalPrice())+"\n");
		b.append("DELIVERY COST: "+StringTools.formatSmallAndBig(getDeliveryCost())+"\n");
		b.append("USED SHIPS   : "+StringTools.formatSmallAndBig(getUsedTradeShips())+"\n");
		b.append("---------------------------------"+"\n");
		
		for(TradeOrderElement e : orders){
			if(e.isBuyOrder()){
				b.append("  ORDER BUY  "+e.amount+" x "+ElementKeyMap.toString(e.getType())+"\n");
			}
		}
		for(TradeOrderElement e : orders){
			if(!e.isBuyOrder()){
				b.append("  ORDER SELL "+e.amount+" x "+ElementKeyMap.toString(e.getType())+"\n");
			}
		}
		b.append("'''' PRINTING TRADE ORDER END ''''\n");
		return b.toString();
	}
	public boolean hasCache() {
		return cacheId != Integer.MIN_VALUE;
	}
	public long assignFleetBuy(TradeNodeStub buyer, TradeNodeStub seller, Vector3i from, Vector3i to) {
		try {
			NPCFaction traders = (NPCFaction) ((GameServerState) state).getFactionManager().getFaction(FactionManager.NPC_FACTION_START);
			return traders.getFleetManager().spawnTradingFleet(getItems(true), from, to, true).dbid; //Todo: Include buyer and seller info
		} catch(Exception exception) {
			exception.printStackTrace();
			return -1;
		}
		/*
		Faction fac = ((GameServerState)state).getFactionManager().getFaction(buyer.getFactionId());
		if(fac != null && fac.isNPC()){
			NPCFaction f = (NPCFaction)fac;
			ElementCountMap c = getItems(true);
			
			Fleet fleet = f.getFleetManager().spawnTradingFleet(c, from, to);
			return fleet.dbid;
		}
		return -1;
		 */
	}
	private ElementCountMap getItems(boolean buy) {
		ElementCountMap c = new ElementCountMap();
		for(TradeOrderElement e : orders){
			if(buy == e.isBuyOrder()){
				c.inc(e.type, e.amount);
			}else{
				c.inc(e.type, e.amount);
			}
		}
		return c;
	}
	public long assignFleetSell(TradeNodeStub buyer, TradeNodeStub seller, Vector3i from, Vector3i to) {
		try {
			NPCFaction traders = (NPCFaction) ((GameServerState) state).getFactionManager().getFaction(FactionManager.NPC_FACTION_START);
			return traders.getFleetManager().spawnTradingFleet(getItems(true), from, to, true).dbid; //Todo: Include buyer and seller info
		} catch(Exception exception) {
			exception.printStackTrace();
			return -1;
		}
		/*
		Faction fac = ((GameServerState)state).getFactionManager().getFaction(seller.getFactionId());
		if(fac != null && fac.isNPC()){
			NPCFaction f = (NPCFaction)fac;
			ElementCountMap c = getItems(true);
			
			Fleet fleet = f.getFleetManager().spawnTradingFleet(c, from, to);
			return fleet.dbid;
		}
		return -1;
		 */
	}
	public short checkActiveRoutesCanSellAllTypesTo(long sellToId, TradeActiveMap tradeActiveMap) {
		for(TradeActive t : tradeActiveMap.getTradeList()){
			if(t.getToId() == sellToId){
				for(TradeOrderElement o : orders){
					if(!o.isBuyOrder()){
						//we are seeling a type to sellTo
						int amount = t.getBlocks().get(o.getType());
						if(amount > 0){
							//we try to sell type to sellTo, but sellTo is expecting a shipment of that type currently
							return o.getType();
						}
					}
					
				}
			}
		}
		return 0;
	}
	public static TradeActive checkActiveRoutesCanSellTypeTo(long sellToId, short type, TradeActiveMap tradeActiveMap) {
		for(TradeActive t : tradeActiveMap.getTradeList()){
			if(t.getToId() == sellToId){
				//we are seeling a type to sellTo
				int amount = t.getBlocks().get(type);
				if(amount > 0){
					//we try to sell type to sellTo, but sellTo is expecting a shipment of that type currently
					return t;
				}
			}
		}
		return null;
	}
	
	
}
