package org.schema.game.common.controller.trade.manualtrade;

import org.schema.game.network.objects.NetworkClientChannel;
import org.schema.game.server.data.PlayerNotFountException;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.server.ServerStateInterface;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;

public class ManualTradeManager {

	
	private Int2ObjectOpenHashMap<ManualTrade> tradeMap = new Int2ObjectOpenHashMap<ManualTrade>();
	
	private StateInterface state;

	private ObjectArrayFIFOQueue<ManualTrade> receivedTrades = new ObjectArrayFIFOQueue<ManualTrade>();
	private ObjectArrayFIFOQueue<ManualTradeItem> receivedTradeItems = new ObjectArrayFIFOQueue<ManualTradeItem>();

	private int idGen = 1;
	
	public ManualTradeManager(StateInterface state) {
		this.state = state;
	}

	public void receiveTrade(ManualTrade trade){
		this.receivedTrades.enqueue(trade);
	}
	
	public void updateLocal(Timer timer){
		while(!receivedTrades.isEmpty()){
			handleTrade(receivedTrades.dequeue());
		}
		while(!receivedTradeItems.isEmpty()){
			handleMod(receivedTradeItems.dequeue());
		}
	}
	
	
	public void updateFromNetworkObject(NetworkClientChannel o){
		for(int i = 0; i < o.manualTradeBuffer.getReceiveBuffer().size(); i++){
			receiveTrade(o.manualTradeBuffer.getReceiveBuffer().get(i).get());
		}
		for(int i = 0; i < o.manualTradeItemBuffer.getReceiveBuffer().size(); i++){
			receiveMod(o.manualTradeItemBuffer.getReceiveBuffer().get(i).get());
		}
	}
	
	public void receiveMod(ManualTradeItem item){
		this.receivedTradeItems.enqueue(item);
	}
	public void handleTrade(ManualTrade trade){
		try {
			trade.state = state;
			trade.setFromPlayers();
			
			if(isOnServer()){
				trade.id = idGen ++;
				tradeMap.put(trade.id, trade);
				trade.sendSelfToClients();
			}
		} catch (PlayerNotFountException e) {
			e.printStackTrace();
		}
	}
	public void handleMod(ManualTradeItem item){
		ManualTrade manualTrade = tradeMap.get(item.tradeId);
		
		if(manualTrade != null){
			manualTrade.received(item);
			
			if(manualTrade.isCanceled()){
				tradeMap.remove(item.tradeId);
			}
			if(manualTrade.isReadyToExecute()){
				
				if(isOnServer()){
					manualTrade.execute();
				}
				manualTrade.executed = true;
				tradeMap.remove(item.tradeId);
			}
		}
	}
	
	
	public boolean isOnServer(){
		return state instanceof ServerStateInterface;
	}
}
