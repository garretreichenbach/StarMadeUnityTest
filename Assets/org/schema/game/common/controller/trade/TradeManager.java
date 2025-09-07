package org.schema.game.common.controller.trade;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.schema.common.util.LogInterface.LogLevel;
import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.shop.shopnew.ShopPanelNew;
import org.schema.game.common.controller.ShopInterface;
import org.schema.game.common.controller.ShoppingAddOn;
import org.schema.game.common.controller.trade.TradeActive.UpdateState;
import org.schema.game.common.controller.trade.TradeOrder.TradeOrderElement;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.faction.FactionNewsPost;
import org.schema.game.network.objects.NetworkClientChannel;
import org.schema.game.network.objects.NetworkGameState;
import org.schema.game.network.objects.TradePriceInterface;
import org.schema.game.network.objects.TradePrices;
import org.schema.game.network.objects.remote.RemoteTradeTypeRequest;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.game.server.data.simulation.npc.diplomacy.DiplomacyAction.DiplActionType;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerStateInterface;
import org.schema.schine.resource.DiskWritable;
import org.schema.schine.resource.FileExt;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public class TradeManager implements DiskWritable {
	public enum TradePerm{
		NONE(0),
		NEUTRAL(1),
		FACTION(2),
		ALLY(4),
		NPC(8),
		ENEMY(16),
		
		;
		
		public final long val;
		
		private TradePerm(long val){
			this.val = val;
		}
		
		public String getDescription(){
			switch(this){
			case ALLY:
				return Lng.str("Allies");
			case ENEMY:
				return Lng.str("Enemies");
			case FACTION:
				return Lng.str("Faction");
			case NEUTRAL:
				return Lng.str("Neutral");
			case NPC:
				return Lng.str("AI-Players (NPC)");
			case NONE:
				break;
			default:
				break;
			}
			return Lng.str("n/a");
		}
		
		
	}
	public static final long NONE = TradePerm.NONE.val;
	public static final long PERM_ALL = TradePerm.ALLY.val | 
			TradePerm.NEUTRAL.val | TradePerm.ENEMY.val | TradePerm.NPC.val | TradePerm.FACTION.val;
	public static final long PERM_ALL_BUT_ENEMY = TradePerm.ALLY.val | 
			TradePerm.NEUTRAL.val | TradePerm.NPC.val | TradePerm.FACTION.val;
	
	public static final long PERM_ALLIES_AND_FACTION = TradePerm.ALLY.val | TradePerm.FACTION.val;
	public static final long PERM_FACTION = TradePerm.FACTION.val;
	public static final String FILENAME = "TRADING.tag";
	
	
	private final TradeActiveMap tradeActiveMap = new TradeActiveMap();
	private final boolean onServer;
	private final StateInterface state;
	private final ObjectArrayFIFOQueue<TradeOrder> receivedTradeOrders = new ObjectArrayFIFOQueue<TradeOrder>();
	private final ObjectArrayFIFOQueue<TradeTypeRequ> tradeTypePriceRequests = new ObjectArrayFIFOQueue<TradeTypeRequ>();
	private boolean shutdown;
	private final static byte VERSION = 0;
	
	
	public TradeManager(StateInterface state){
		this.state = state;
		this.onServer = state instanceof ServerStateInterface;
		
		
		if(onServer){
			
			TradeTypeRequestProcessor c = new TradeTypeRequestProcessor();
			c.start();
			
			
			loadFromFile();
			
//			throw new RuntimeException("CATCH THIS");
		}
	}
	
	private void loadFromFile() {
		File over = new FileExt(GameServerState.ENTITY_DATABASE_PATH + FILENAME);
		try {
			fromTagStructure(Tag.readFrom(new BufferedInputStream(new FileInputStream(over)), true, false));
		} catch (FileNotFoundException e) {
			System.err.println("ERROR HAPPENED WHEN LOADING: "+over.getAbsolutePath());
			System.err.println("[SERVER] Cant load trade routes. no saved data found");
		} catch (RuntimeException e) {
			System.err.println("ERROR HAPPENED WHEN LOADING: "+over.getAbsolutePath());
			throw e;
		} catch (IOException e) {
			System.err.println("ERROR HAPPENED WHEN LOADING: "+over.getAbsolutePath());
			e.printStackTrace();
		}
	}
	private void handleOfferRequest(short type, NetworkClientChannel chan){
		assert(onServer);
		Long2ObjectOpenHashMap<TradeNodeStub> tMap = ((GameServerState) state).getUniverse().getGalaxyManager().getTradeNodeDataById();
		List<TradeNodeStub> nodes = new ObjectArrayList<TradeNodeStub>(tMap.size());
		
		
		synchronized(state){
			state.setSynched();
			nodes.addAll(tMap.values());
			state.setUnsynched();
		}
		System.err.println("[TRADEMANAGER] SEARCHING FOR TYPE "+ElementKeyMap.toString(type)+" in "+nodes.size()+" Trade Nodes!");
		
		for(int i = 0; i < nodes.size(); i++){
			TradeNodeStub node = nodes.get(i);
			
			
			List<TradePriceInterface> pricesRep = null;
			synchronized(state){
				state.setSynched();
				//check synchronized if
				Sendable sendable = state.getLocalAndRemoteObjectContainer().getDbObjects().get(node.getEntityDBId());
				if(sendable != null && sendable instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>)sendable).getManagerContainer() instanceof ShopInterface){
					ShopInterface s = (ShopInterface)((ManagedSegmentController<?>)sendable).getManagerContainer();
					pricesRep = s.getShoppingAddOn().getPricesRep();
				}
				state.setUnsynched();
			}
			
			if(pricesRep == null){
				//do unsynchronized db request
				pricesRep = ((GameServerState)state).getDatabaseIndex().getTableManager().getTradeNodeTable().getTradePricesAsList(node.getEntityDBId());
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if(pricesRep == null){
				System.err.println("[TRADEMANAGER] NO TRADE PRICES FOUND FOR "+node+"; ("+node.getEntityDBId()+")");
				continue;
			}else{
				System.err.println("[TRADEMANAGER] FOUND "+pricesRep.size()+" TRADE PRICES FOR "+node+"; ("+node.getEntityDBId()+")");
			}
			long buyPrice = -1;
			long sellPrice = -1;
			int availableBuy = -1;
			int availableSell = -1;
			
			for(int j = 0; j < pricesRep.size(); j++){
				TradePriceInterface price = pricesRep.get(j);
				
				if(price.getType() == type){
					
					
					if(price.isBuy()){
						sellPrice = price.getPrice();
						availableSell = node.getMaxOwn(price.isBuy(), price);
					}else{
						buyPrice = price.getPrice();
						availableBuy = node.getMaxOwn(price.isBuy(), price);
					}
					
					System.err.println("[TRADEMANAGER] FOUND PRICE FOR "+ElementKeyMap.toString(type)+": "+(price.isBuy() ? "BUY" : "SELL")+"; PRICE "+price.getPrice()+"c; AMOUNT "+price.getAmount()+"; LIM "+price.getLimit()+"; AVAIL: "+(price.isBuy() ? availableBuy : availableSell)+" FOR "+node+"; ("+node.getEntityDBId()+")");
					
				}
				if((buyPrice >= 0 && availableBuy > 0) && (sellPrice >= 0 && availableSell > 0)){
					break;
				}
			}
			
			
			if((buyPrice >= 0 && availableBuy > 0) || sellPrice >= 0 && availableSell > 0){
//				assert(false):buyPrice+"; "+availableBuy+"; "+sellPrice+"; "+availableSell;
				synchronized(state){
					
					chan.tradeTypeBuffer.add(new RemoteTradeTypeRequest(
							new TradeTypeRequestAwnser(
									type, node.getEntityDBId(), 
									buyPrice, availableBuy, sellPrice, availableSell), onServer));
				}
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}else{
				System.err.println("[SERVER][TRADE] Offer search for "+ElementKeyMap.toString(type)+" didn't result in a hit on "+node);
			}
			
		}
	}
	public void updateFromNetworkObject(NetworkGameState nt){
		if(!onServer){
			tradeActiveMap.handleReceived(nt.tradeActiveBuffer);
		}
	}
	public void initFromNetworkObject(NetworkGameState nt){
		if(!onServer){
			tradeActiveMap.handleReceived(nt.tradeActiveBuffer);
		}
	}
	
	public void updateToNetworkObject(NetworkGameState nt){
	}
	public void updateToFullNetworkObject(NetworkGameState nt){
		if(onServer){
			tradeActiveMap.sendAll(nt.tradeActiveBuffer);
		}
	}
	
	public void updateLocal(Timer timer) {
		while(!receivedTradeOrders.isEmpty()){
			executeTradeOrderServer(receivedTradeOrders.dequeue());
		}
		tradeActiveMap.update();
		if(onServer){
			for(int i = 0; i < tradeActiveMap.getTradeList().size(); i++){
				TradeActive t = tradeActiveMap.getTradeList().get(i);
				UpdateState m = t.update((GameServerState) state, timer.currentTime);
				if(m == UpdateState.CANCEL ){
					System.err.println("[SERVER][TRADING] Trade route cancelled.");
					tradeActiveMap.removeOnServer(t, ((GameServerState) state).getGameState().getNetworkObject().tradeActiveBuffer);
					i--;
				}else if(m == UpdateState.CHANGED ){
					tradeActiveMap.changeOnServer(t, ((GameServerState) state).getGameState().getNetworkObject().tradeActiveBuffer);
				}else if(m == UpdateState.EXECUTE){
					
					executeFinishedTradeRoute(t);
					
					tradeActiveMap.removeOnServer(t, ((GameServerState) state).getGameState().getNetworkObject().tradeActiveBuffer);
					i--;
				}
			}
		}
	}
	
	private void executeFinishedTradeRoute(TradeActive t) {
		System.err.println("[SERVER] Trade Route Finished "+t);
		
		
		TradeNodeStub tGoalNode = ((GameServerState) state).getUniverse().getGalaxyManager().getTradeNodeDataById().get(t.getToId());
		
		Sendable sB = state.getLocalAndRemoteObjectContainer().getDbObjects().get(t.getToId());
		
		if(sB != null && sB instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>)sB).getManagerContainer() instanceof ShopInterface){
			ShopInterface s = (ShopInterface)((ManagedSegmentController<?>)sB).getManagerContainer();
			tGoalNode = s.getTradeNode();
		}
		
		if(tGoalNode == null){
			System.err.println("[SERVER] Exception: target station not found for trade: "+t);
			return;
		}
		List<TradePriceInterface> pricesGoal;
		if(tGoalNode instanceof TradeNode){
			pricesGoal = ((TradeNode) tGoalNode).shop.getShoppingAddOn().getPricesRep();
		}else{
			DataInputStream tpStream = ((GameServerState) state).getDatabaseIndex().getTableManager().getTradeNodeTable().getTradePricesAsStream(tGoalNode.getEntityDBId());
			if(tpStream != null){
				try {
					TradePrices pc = ShoppingAddOn.deserializeTradePrices(tpStream, true);
					tpStream.close();
					pricesGoal = pc.getPrices();
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}else{
				System.err.println("[SERVER] Exception: Error in trade order. Receiver Trade Node not found. "+t);
				return;
			}
		}
		tGoalNode.addBlocks(t, pricesGoal, (GameServerState) state);
		
		String op = Lng.str("Trading Guild");
		String topic = Lng.str("Trading Completed");
		String msg = Lng.str("Space Station %s received blocks from %s:\n%s", t.getToStation(), t.getFromStation(), t.getBlocks().printList());
		if(t.getToFactionId() > 0){
			FactionNewsPost o = new FactionNewsPost();
			
			o.set(t.getToFactionId(), op, 
					System.currentTimeMillis(), topic, msg, 0);
			((FactionState) state).getFactionManager().addNewsPostServer(o);
		}
		String[] pls = t.getToPlayer().split(";");
		for(String p : pls){
			((GameServerState) state).getServerPlayerMessager().send(op, p, topic, msg);
		}
		
		Faction fA = ((GameServerState) state).getFactionManager().getFaction(t.getFromFactionId());
		if(fA != null && fA.isNPC()){
			try {
				System.err.println("[TRADE] OnFinish: Recalculating trade prices for "+fA.getName());
				TradePrices recalcPrices = ((NPCFaction)fA).getTradeController().recalcPrices();
				if(recalcPrices != null && ((NPCFaction)fA).getTradeNode() != null){
					((GameServerState) state).getUniverse().getGalaxyManager().broadcastPrices(recalcPrices);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Faction fB = ((GameServerState) state).getFactionManager().getFaction(t.getToFactionId());
		if(fB != null && fB.isNPC()){
			try {
				System.err.println("[TRADE] OnFinish: Recalculating trade prices for "+fB.getName());
				TradePrices recalcPrices = ((NPCFaction)fB).getTradeController().recalcPrices();
				if(recalcPrices != null){
					pricesGoal = recalcPrices.getPrices();
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		((GameServerState) state).getUniverse().getGalaxyManager().broadcastPrices(TradePrices.getFromPrices(pricesGoal, tGoalNode.getEntityDBId()));
	}

	public boolean executeTradeOrderServer(TradeOrder t) {
		assert(onServer);
		TradeNodeStub tA = ((GameServerState) state).getUniverse().getGalaxyManager().getTradeNodeDataById().get(t.fromDbId);
		TradeNodeStub tB = ((GameServerState) state).getUniverse().getGalaxyManager().getTradeNodeDataById().get(t.toDbId);
		
		Sendable sA = state.getLocalAndRemoteObjectContainer().getDbObjects().get(t.fromDbId);
		Sendable sB = state.getLocalAndRemoteObjectContainer().getDbObjects().get(t.toDbId);
		
		if(sA != null && sA instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>)sA).getManagerContainer() instanceof ShopInterface){
			ShopInterface s = (ShopInterface)((ManagedSegmentController<?>)sA).getManagerContainer();
			tA = s.getTradeNode();
		}
		if(sB != null && sB instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>)sB).getManagerContainer() instanceof ShopInterface){
			ShopInterface s = (ShopInterface)((ManagedSegmentController<?>)sB).getManagerContainer();
			tB = s.getTradeNode();
		}
		if(tA == null || tB == null){
			t.sendMsgError(Lng.astr("Error in trade order. Trade stations not found (removed or not in database yet)."));
			t.log("TRADE ORDER FAILED: "+t.fromDbId+"; "+t.toDbId+"; NOT FOUND "+ state.getLocalAndRemoteObjectContainer().getDbObjects(), LogLevel.ERROR);
			return false;
		}
		List<TradePriceInterface> pricesA;
		List<TradePriceInterface> pricesB;
		if(tA instanceof TradeNode){
			pricesA = ((TradeNode) tA).shop.getShoppingAddOn().getPricesRep();
		}else{
			if(t.hasCache() && onServer){
				pricesA = tA.getTradePricesCached(((GameServerState)state), t.cacheId).getCachedPrices(t.cacheId);
			}else{
				DataInputStream tpStream = ((GameServerState) state).getDatabaseIndex().getTableManager().getTradeNodeTable().getTradePricesAsStream(tA.getEntityDBId());
				if(tpStream != null){
					try {
						TradePrices pc = ShoppingAddOn.deserializeTradePrices(tpStream, true);
						tpStream.close();
						
						pricesA = pc.getPrices();
						
					} catch (IOException e) {
						e.printStackTrace();
						t.log("ERROR: "+e.getClass().getSimpleName()+": "+e.getMessage(), LogLevel.ERROR);
						t.sendMsgError(Lng.astr("Error in trade order. Sender Trade Node not found."));
						return false;
					}
				}else{
					t.log("Error in trade order. Sender Trade Node not found.", LogLevel.ERROR);
					t.sendMsgError(Lng.astr("Error in trade order. Sender Trade Node not found."));
					return false;
				}
			}
		}
		if(tB instanceof TradeNode){
			pricesB = ((TradeNode) tB).shop.getShoppingAddOn().getPricesRep();
		}else{
			if(t.hasCache() && onServer){
				pricesB = tB.getTradePricesCached(((GameServerState)state), t.cacheId).getCachedPrices(t.cacheId);
			}else{
				DataInputStream tpStream = ((GameServerState) state).getDatabaseIndex().getTableManager().getTradeNodeTable().getTradePricesAsStream(tB.getEntityDBId());
				if(tpStream != null){
					try {
						TradePrices pc = ShoppingAddOn.deserializeTradePrices(tpStream, true);
						tpStream.close();
						
						pricesB = pc.getPrices();
						
					} catch (IOException e) {
						e.printStackTrace();
						t.sendMsgError(Lng.astr("Error in trade order. Receiver Trade Node not found."));
						t.log("Error in trade order. Receiver Trade Node not found.", LogLevel.ERROR);
						return false;
					}
				}else{
					t.sendMsgError(Lng.astr("Error in trade order. Receiver Trade Node not found."));
					t.log("Error in trade order. Receiver Trade Node not found.", LogLevel.ERROR);
					return false;
				}
			}
		}
		
		t.recalc();
		
		if(!checkTrade(t, tA, tB, pricesA, pricesB)){
			return false;
		}
		try{
			if(t.getBuyVolume() > 0 || t.getBuyPrice() > 0){
				//route from from sellet to us
				TradeActive tradeActive = new TradeActive();
				tradeActive.initiateBuyTrade((GameServerState)state, tA, tB, t);
				
				//System.err.println("REMOVING BOUGHT UNITS FROM -OTHER- NODE");
				tB.removeBoughtBlocks(t, pricesB, ((GameServerState) state));
				tB.modCredits(t.getBuyPrice(), ((GameServerState) state));
				assert(!t.isEmpty());
				t.log("TRADE MADE: STATION "+tA.getStationName()+" BOUGHT ITEMS FROM "+tB.getStationName()+"; Shop Received Credits: "+t.getBuyPrice(), LogLevel.NORMAL);
				tradeActiveMap.addOnServer(tradeActive, ((GameServerState) state).getGameState().getNetworkObject().tradeActiveBuffer);
				
				
				String op = Lng.str("Trading Guild");
				String topic = Lng.str("Trading Order");
				String msg = Lng.str("Space Station %s sold blocks to %s:\nReceived Credits: %s\n%s", tB.getStationName(), tA.getStationName(), Math.abs(t.getBuyPrice()), tradeActive.getBlocks().printList());
				if(tB.getFactionId() > 0){
					FactionNewsPost o = new FactionNewsPost();
					
					o.set(tB.getFactionId(), op, 
							System.currentTimeMillis(), topic, msg, 0);
					((FactionState) state).getFactionManager().addNewsPostServer(o);
				}
				for(String p : tB.getOwners()){
					
					((GameServerState) state).getServerPlayerMessager().send(op, p, topic, msg);
				}
			}
			if(t.getSellVolume() > 0 || t.getSellPrice() > 0){
				//route from from us to seller
				TradeActive tradeActive = new TradeActive();
				tradeActive.initiateSellTrade((GameServerState)state, tA, tB, t);
				
				assert(!t.isEmpty());
				//System.err.println("REMOVING SOLD UNITS FROM -OUR- NODE");
				tA.removeSoldBlocks(t, pricesA, ((GameServerState) state));
				
				tB.modCredits(-t.getSellPrice(), ((GameServerState) state));
				
				t.log("TRADE MADE: STATION "+tA.getStationName()+" SOLD BLOCKS TO "+tB.getStationName(), LogLevel.NORMAL);
				tradeActiveMap.addOnServer(tradeActive, ((GameServerState) state).getGameState().getNetworkObject().tradeActiveBuffer);
				
				String op = Lng.str("Trading Guild");
				String topic = Lng.str("Trading Order");
				String msg = Lng.str("Space Station %s sold blocks to %s:\nSell Price: %s, Delivery Cost: %s\n%s", tA.getStationName(), tB.getStationName(), t.getSellPrice(), t.getTradingGuildPrice(), tradeActive.getBlocks().printList());
				
				if(tA.getFactionId() > 0){
					FactionNewsPost o = new FactionNewsPost();
					
					o.set(tA.getFactionId(), op, 
							System.currentTimeMillis(), topic, msg, 0);
					((FactionState) state).getFactionManager().addNewsPostServer(o);
				}
				for(String p : tA.getOwners()){
					((GameServerState) state).getServerPlayerMessager().send(op, p, topic, msg);
				}
			}
			t.log("TRADE UNDERWAY: STATION "+tA.getStationName()+" MADE TRADE WITH "+tB.getStationName()+"; Total Price Deducted: "+t.getTotalPrice()+"; Had Credits: "+tA.getCredits(), LogLevel.NORMAL);
			tA.modCredits(-t.getTotalPrice(), ((GameServerState) state));
			
			((GameServerState) state).getUniverse().tradeNodesDirty.enqueue(tA.getEntityDBId());
			if(!tA.system.equals(tB.system)){
				((GameServerState) state).getUniverse().tradeNodesDirty.enqueue(tB.getEntityDBId());
			}
			Faction fA = ((GameServerState) state).getFactionManager().getFaction(tA.getFactionId());
			if(fA != null && fA.isNPC()){
				try {
					t.log("Recalculating trade prices for "+fA.getName(), LogLevel.NORMAL);
					TradePrices recalcPrices = ((NPCFaction)fA).getTradeController().recalcPrices();
					if(recalcPrices != null){
						pricesA = recalcPrices.getPrices();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			Faction fB = ((GameServerState) state).getFactionManager().getFaction(tB.getFactionId());
			if(fB != null && fB.isNPC()){
				try {
					t.log("Recalculating trade prices for "+fB.getName(), LogLevel.NORMAL);
					TradePrices recalcPrices = ((NPCFaction)fB).getTradeController().recalcPrices();
					if(recalcPrices != null){
						pricesB = recalcPrices.getPrices();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			((GameServerState) state).getUniverse().getGalaxyManager().broadcastPrices(TradePrices.getFromPrices(pricesA, tA.getEntityDBId()));
			((GameServerState) state).getUniverse().getGalaxyManager().broadcastPrices(TradePrices.getFromPrices(pricesB, tB.getEntityDBId()));
			t.log("TRADE SENT: STATION "+tA.getStationName()+" MADE TRADE WITH "+tB.getStationName()+"; Now Credits: "+tA.getCredits(), LogLevel.NORMAL);
			t.sendMsgError(Lng.astr("Trade order sent!"));
			
			((GameServerState) state).getFactionManager().diplomacyAction(DiplActionType.TRADING_WITH_US, tB.getFactionId(), tA.getFactionId());
			
			Faction faction = ((GameServerState) state).getFactionManager().getFaction(tB.getFactionId());
			if(faction != null && faction.isNPC()){
				List<Faction> enemies = faction.getEnemies();
				
				for(Faction enemy : enemies){
					((GameServerState) state).getFactionManager().diplomacyAction(DiplActionType.TRADING_WITH_ENEMY, enemy.getIdFaction(), tA.getFactionId());
				}
			}
			
			if(FactionManager.isNPCFaction(tA.getFactionId())){
				assert(tA.getSector() != null);
				assert(tB.getSector() != null);
				((GameServerState) state).getFactionManager().getNpcFactionNews().trading(tA.getFactionId(), tA.getSector(), tB.getSector());
				
				
				
			}else if(FactionManager.isNPCFaction(tB.getFactionId())){
				assert(tA.getSector() != null);
				assert(tB.getSector() != null);
				((GameServerState) state).getFactionManager().getNpcFactionNews().trading(tB.getFactionId(), tA.getSector(), tB.getSector());
			}
			
			
		}catch(TradeInvalidException e){
			e.printStackTrace();
			t.sendMsgError(Lng.astr("Error in trade order. Other station not found (removed or not in database yet)."));
			t.log("Error in trade order. Other station not found (removed or not in database yet).", LogLevel.ERROR);
			return false;
		}
		return true;
	}
	public boolean checkTrade(TradeOrder t, final TradeNodeStub tA, final TradeNodeStub tB, List<TradePriceInterface> pricesA, List<TradePriceInterface> pricesB){
		double availableCapacityB = tB.getCapacity() - tB.getVolume();
		
		t.log("TRADE ORDER CHECK: "+ state +"; "+tA.getStationName()+" <-> "+tB.getStationName()+"; capB: "+tB.getCapacity()+"; volB: "+tB.getVolume()+" = availableCapB "+availableCapacityB+"; CreditsA: "+tA.getCredits()+"; CreditsB: "+tB.getCredits(), LogLevel.NORMAL);
		if(t.getSellVolume() == 0 && t.getBuyVolume() == 0 && t.getSellPrice() == 0 && t.getBuyPrice() == 0){
			t.log("trade order empty -> no trade", LogLevel.ERROR);
			if(state instanceof GameServerState && t.hasCache()){
				
				for(TradeOrderElement e : t.getElements()){
					int price = 0;
					int limit = 0;
					if(e.isBuyOrder()){
						price = tB.getTradePricesCached((GameServerState)state, t.cacheId).getCachedSellPrice(t.cacheId, e.type).getPrice();
						limit = tB.getTradePricesCached((GameServerState)state, t.cacheId).getCachedSellPrice(t.cacheId, e.type).getLimit();
					}else{
						price = tB.getTradePricesCached((GameServerState)state, t.cacheId).getCachedBuyPrice(t.cacheId, e.type).getPrice();
						limit = tB.getTradePricesCached((GameServerState)state, t.cacheId).getCachedBuyPrice(t.cacheId, e.type).getLimit();
					}
					t.log("DEBUG EMPTY: "+ElementKeyMap.toString(e.type)+"x"+e.amount+"; "+(e.isBuyOrder() ? "BUY" : "SELL")+"; Price: "+price+"; Limit: "+limit, LogLevel.ERROR);
				}
			}
			
			t.sendMsgError(
					Lng.astr("Order is Empty*"));
			return false;
		}
		if(t.getSellVolume() > 0 && t.getSellVolume() > availableCapacityB ){
			t.log("can't sell. not enough space in target shop "+t.getSellVolume()+" / "+availableCapacityB+"; (Total cap b: "+tB.getCapacity()+")", LogLevel.ERROR);
			if(onServer){
				t.sendMsgError(
						Lng.astr("Trade order denied. Cannot put target trade node at over capacity\nVolume sold: %s\nTarget Capacity: %s*", 
								StringTools.formatPointZero(t.getSellVolume()), 
								StringTools.formatPointZero(availableCapacityB)));
			}else{
				ShopInterface loadedShop = ((TradeNodeClient)tB).getLoadedShop();
				
				if(loadedShop != null){
					System.err.println("[TRADE] Shop B is currently loaded: "+loadedShop);
					Sendable sendable = loadedShop.getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(loadedShop.getSegmentController().getId());
					assert(sendable != null);
				}
				
				((GameClientState) state).getController().popupAlertTextMessage(
						Lng.str("Trade order denied. Cannot put target trade node at over capacity\nVolume sold: %s\nTarget Capacity: %s", 
								StringTools.formatPointZero(t.getSellVolume()), 
								StringTools.formatPointZero(availableCapacityB)), 0);
				
			}
			return false;
		}
		
		if(t.getSellPrice() > tB.getCredits()){
			t.log("not enough credis in targetShop. cant sell to it. "+ state +"; sell price "+t.getSellPrice()+"; target credits: "+tB.getCredits(), LogLevel.ERROR);
			t.sendMsgError(
					Lng.astr("Trade order denied. Target trade node doesn't have enough credits to\npay for the blocks you are selling\nShop needs %s credits more!*", 
							t.getSellPrice() - tB.getCredits()));
			return false;
		}
		if(t.getTotalPrice() > tA.getCredits()){
			System.err.println("[SHOP] "+ state +"; total price "+t.getTotalPrice()+"; own shop credits: "+tA.getCredits());
			long needed =  (t.getTotalPrice() - tA.getCredits());
			
			t.log("not enough credits to pay ("+t.getTotalPrice()+" / "+tA.getCredits()+")", LogLevel.ERROR);
			t.sendMsgError(
					Lng.astr("Trade order denied. You do not have enough credits to pay for this trade\nYour own shop needs %s credits more!*", 
							t.getTotalPrice() - tA.getCredits()
							));
			if(!onServer){
				
				ShopPanelNew.popupDeposit((GameClientState) state,
						Lng.str("Shop needs at least %s more credits\nto make this trade.\nDo you want to deposit some money?", needed));
				
			}
			return false;
		}
		
		
		Short2ObjectOpenHashMap<TradePriceInterface> pricesSellAMap;
		if(onServer && t.hasCache()){
			pricesSellAMap = tA.getTradePricesCached((GameServerState) state, t.cacheId).cachedSellPrices;
		}else{
			pricesSellAMap = TradeNodeStub.toMap(pricesA, false);
		}
		short cantSell;
		if((cantSell = t.checkActiveRoutesCanSellAllTypesTo(tB.getEntityDBId(), tradeActiveMap)) != 0){
			t.log("Trade order denied. "
					+ "The other shop is currently expecting a shipment of "+ElementKeyMap.getNameSave(cantSell)+"\n"
					+ "and is waiting until arrival until its next order.", LogLevel.ERROR);
			t.sendMsgError(
					Lng.astr("Trade order denied. "
							+ "The other shop is currently expecting a shipment of %s\n"
							+ "and is waiting until arrival until its next order.", ElementKeyMap.getNameSave(cantSell)));
			return false;
		}
		
		if(!t.checkCanSellWithInventory(pricesSellAMap, tA)){
			t.log("Trade order denied. Your shop does not have the blocks you want to sell!", LogLevel.ERROR);
			t.sendMsgError(
					Lng.astr("Trade order denied. Your shop does not have the blocks you want to sell!"));
			return false;
		}
		
		
		
		Short2ObjectOpenHashMap<TradePriceInterface> pricesBuyBMap;
		if(onServer && t.hasCache()){
			pricesBuyBMap = tB.getTradePricesCached((GameServerState) state, t.cacheId).cachedSellPrices;
		}else{
			pricesBuyBMap = TradeNodeStub.toMap(pricesB, true);
		}
		
		if(!t.checkCanSellToOtherWithInventory(pricesBuyBMap)){
			t.log("Trade order denied. Other shop doesn't buy that many blocks!", LogLevel.ERROR);
			t.sendMsgError(Lng.astr("Trade order denied. Other shop doesn't buy that many blocks!"));
				
			return false;
		}
		
		
		
		
		Short2ObjectOpenHashMap<TradePriceInterface> pricesSellBMap;
		if(onServer && t.hasCache()){
			pricesSellBMap = tB.getTradePricesCached((GameServerState) state, t.cacheId).cachedSellPrices;
		}else{
			pricesSellBMap = TradeNodeStub.toMap(pricesB, false);
		}
		if(!t.checkCanFromOtherBuyWithInventory(pricesSellBMap)){
			t.log("Trade order denied. Target shop doesn't have the requested block amount", LogLevel.ERROR);
			t.sendMsgError(
					Lng.astr("Trade order denied. Target shop doesn't have the requested block amount!"));
			return false;
		}
		
		
		
		t.log("TRADE ORDER CHECK SUCCESSFUL", LogLevel.NORMAL);
		return true;
	}
	
	public static boolean isPermission(long perm, TradePerm p){
		return (perm & p.val) == p.val;
	}

	public TradeActiveMap getTradeActiveMap() {
		return tradeActiveMap;
	}

	public boolean isOnServer() {
		return onServer;
	}

	public StateInterface getState() {
		return state;
	}

	public void receivedOrderOnServer(TradeOrder r) {
		this.receivedTradeOrders.enqueue(r);
	}

	@Override
	public String getUniqueIdentifier() {
				return null;
	}

	@Override
	public boolean isVolatile() {
				return false;
	}

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] tr = (Tag[])tag.getValue();
		byte version = (Byte)tr[0].getValue();
		
		Tag trades = tr[1];
		tradeActiveMap.fromTagStructure(trades);
	}

	@Override
	public Tag toTagStructure() {
		
		
		
		return new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.BYTE, null, VERSION),
				tradeActiveMap.toTagStructure(),
				
				FinishTag.INST});
	}

	public void requestTradeTypePrices(
			NetworkClientChannel networkClientChannel, short r) {
		TradeTypeRequ q = new TradeTypeRequ(networkClientChannel, r);
		
		synchronized(tradeTypePriceRequests){
			tradeTypePriceRequests.enqueue(q);
			tradeTypePriceRequests.notify();
		}
	}
	private class TradeTypeRequestProcessor extends Thread{

		public TradeTypeRequestProcessor() {
			super("TradeTypeRequestProcessor");
		}

		@Override
		public void run() {
			while(!shutdown){
				TradeTypeRequ dequeue;
				synchronized(tradeTypePriceRequests){
					while(tradeTypePriceRequests.isEmpty()){
						try {
							tradeTypePriceRequests.wait();
							if(shutdown){
								return;
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					dequeue = tradeTypePriceRequests.dequeue();
				}
				handleOfferRequest(dequeue.type, dequeue.networkClientChannel);
			}
		}
		
	}
	private class TradeTypeRequ{
		NetworkClientChannel networkClientChannel;
		short type;
		public TradeTypeRequ(NetworkClientChannel networkClientChannel,
				short type) {
			super();
			this.networkClientChannel = networkClientChannel;
			this.type = type;
		}
		
		
	}
	public void onStop(){
		shutdown = true;
		synchronized(tradeTypePriceRequests){
			tradeTypePriceRequests.notify();
		}
	}
}
