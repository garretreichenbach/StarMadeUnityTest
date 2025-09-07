package org.schema.game.common.controller.trade;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ShopInterface;
import org.schema.game.common.controller.trade.TradeOrder.TradeOrderElement;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.network.objects.TradePriceInterface;
import org.schema.game.network.objects.TradePrices;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.objects.Sendable;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;


public class TradeNode extends TradeNodeStub{
	
	
	public ShopInterface shop;
	
	@Override
	public InputStream getPricesInputStream() throws IOException {
		FastByteArrayOutputStream b = new FastByteArrayOutputStream(10*1024);
			
		shop.getShoppingAddOn().serializeTradePrices(new DataOutputStream(b));
		
		return new FastByteArrayInputStream(b.array, 0, (int)b.length());
	}

	@Override
	public TradePrices getTradePricesInstance(GameServerState state){
		return shop.getShoppingAddOn().getTradePrices();
	}
	
	public void setFromShop(ShopInterface shop) {
		this.shop = shop;
	}
	@Override
	public void modCredits(long credits, GameServerState s) {
		super.modCredits(credits, s);
		shop.modCredits(credits);
	}

	@Override
	public long getEntityDBId() {
		return shop.getSegmentController().dbId;
	}

	@Override
	public Vector3i getSystem() {
		Sendable sendable = shop.getSegmentController().getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(shop.getSegmentController().getSectorId());
		if(sendable != null && sendable instanceof RemoteSector){
			RemoteSector r = (RemoteSector)sendable;
			if(shop.getSegmentController().isOnServer()){
				system = VoidSystem.getContainingSystem(r.getServerSector().pos, new Vector3i());
				return system;
			}else{
				system = VoidSystem.getContainingSystem(r.clientPos(), new Vector3i());
				return system;
			}
		}
		return null;
	}
	
	
	@Override
	public Vector3i getSector() {
		Sendable sendable = shop.getSegmentController().getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(shop.getSegmentController().getSectorId());
		if(sendable != null && sendable instanceof RemoteSector){
			RemoteSector r = (RemoteSector)sendable;
			if(shop.getSegmentController().isOnServer()){
				sector = new Vector3i(r.getServerSector().pos);
				return sector;
			}else{
				sector = new Vector3i(r.clientPos());
				return sector;
			}
		}
		return null;
	}

	@Override
	public Set<String> getOwners() {
		setOwners(shop.getShoppingAddOn().getOwnerPlayers());
		return super.getOwners();
	}

	@Override
	public String getStationName() {
		setStationName(shop.getSegmentController().getRealName());
		return super.getStationName();
	}

	@Override
	public int getFactionId() {
		setFactionId(shop.getSegmentController().getFactionId());
		return super.getFactionId();
	}

	@Override
	public long getTradePermission() {
		setTradePermission(shop.getPermissionToTrade());
		return super.getTradePermission();
	}
	
	@Override
	public double getVolume() {
		this.setVolume(shop.getShopInventory().getVolume());
		return super.getVolume();
	}


	@Override
	public double getCapacity() {
		this.setCapacity(shop.getShopInventory().getCapacity());
//		System.err.println("SAVING SHOP CAP "+shop+" "+super.getCapacity()+ "   "+shop+"; "+shop.getShopInventory()+"; "+shop.getShopInventory().getInventoryHolder());
		return super.getCapacity();
		
	}

	@Override
	public long getCredits() {
		super.setCredits(shop.getCredits());
		return super.getCredits();
	}
	@Override
	public void addBlocks(TradeActive t, List<TradePriceInterface> prices, GameServerState s) {
		IntOpenHashSet h = new IntOpenHashSet();
		for(short r : ElementKeyMap.keySet){
			short type = r;
			int amount = t.getBlocks().get(type);
			if(amount > 0){
				h.add(shop.getShopInventory().incExistingOrNextFreeSlotWithoutException(type, amount));
			}
		}
		shop.getShopInventory().sendInventoryModification(h); 
		
	}

	@Override
	public void removeBoughtBlocks(TradeOrder t, List<TradePriceInterface> prices, GameServerState s) {
		List<TradeOrderElement> orders = t.getOrders();
		for(TradeOrderElement e : orders){
			if(e.isBuyOrder()){
				removeBlocks(e, prices);
			}
		}
		
	}

	public void removeBlocksDirect(TradeOrderElement e) {
		IntOpenHashSet h = new IntOpenHashSet();
		shop.getShopInventory().decreaseBatch(e.type, e.amount, h);
		shop.getShopInventory().sendInventoryModification(h); 
	}

	@Override
	public void removeSoldBlocks(TradeOrder t, List<TradePriceInterface> prices, GameServerState s) {
		List<TradeOrderElement> orders = t.getOrders();
		for(TradeOrderElement e : orders){
			if(!e.isBuyOrder()){
				//System.err.println("REMOVING BLOCKS FOR SELL ORDER OF OWN: "+e.amount+"; ");
				removeBlocksDirect(e);
			}
		}		
	}


	

	
}
