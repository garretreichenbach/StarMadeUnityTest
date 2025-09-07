package org.schema.game.common.controller.trade;

import java.util.List;

import org.schema.game.network.objects.remote.RemoteTradeActive;
import org.schema.game.network.objects.remote.RemoteTradeActiveBuffer;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;
import org.schema.schine.resource.tag.ListSpawnObjectCallback;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.TagSerializable;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class TradeActiveMap extends GUIObservable implements TagSerializable{
	private final List<TradeActive> tradeList = new ObjectArrayList<TradeActive>();
	private final Long2ObjectOpenHashMap<List<TradeActive>> byFromId = new Long2ObjectOpenHashMap<List<TradeActive>>();
	private final Long2ObjectOpenHashMap<List<TradeActive>> byToId = new Long2ObjectOpenHashMap<List<TradeActive>>();
	private final Int2ObjectOpenHashMap<List<TradeActive>> byFromFactionId = new Int2ObjectOpenHashMap<List<TradeActive>>();
	private final Int2ObjectOpenHashMap<List<TradeActive>> byToFactionId = new Int2ObjectOpenHashMap<List<TradeActive>>();
	private final LongOpenHashSet fleetsInTrades = new LongOpenHashSet();
	private List<TradeActive> receivedTradeActive = new ObjectArrayList<TradeActive>();
	
	
	public void handleReceived(RemoteTradeActiveBuffer buffer){
		for(int i = 0; i < buffer.getReceiveBuffer().size(); i++){
			TradeActive tradeActive = buffer.getReceiveBuffer().get(i).get();
			
			receivedTradeActive.add(tradeActive);
		}
	}
	
	
	public void sendAll(RemoteTradeActiveBuffer buffer){
		assert(buffer.onServer);
		for(TradeActive t : tradeList){
			buffer.add(new RemoteTradeActive(t, true));
		}
	}
	public void addOnServer(TradeActive t, RemoteTradeActiveBuffer buffer){
		assert(buffer.onServer);
		add(t);
		buffer.add(new RemoteTradeActive(t, true));
	}
	public void changeOnServer(TradeActive t, RemoteTradeActiveBuffer buffer){
		assert(buffer.onServer);
		TradeActive clone = t.clone();
		clone.setChangedFlag(true);
		buffer.add(new RemoteTradeActive(clone, true));
	}
	public void removeOnServer(TradeActive t, RemoteTradeActiveBuffer buffer){
		assert(buffer.onServer);
		remove(t);
		t.setRemoveFlag(true);
		buffer.add(new RemoteTradeActive(t, true));
		
	}
	private void add(TradeActive t){
		tradeList.add(t);
		addMap(byFromId, t, t.getFromId());
		addMap(byToId, t, t.getToId());
		addMap(byFromFactionId, t, t.getFromFactionId());
		addMap(byToFactionId, t, t.getToFactionId());
		if(t.getFleetId() >= 0){
			fleetsInTrades.add(t.getFleetId());
		}
		
	}
	private void remove(TradeActive t){
		boolean remove = tradeList.remove(t);
		if(!remove){
			try {
				throw new Exception("Trade not removed right. This is not breaking, but shouldnt happen "+t);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		removeMap(byFromId, t, t.getFromId());
		removeMap(byToId, t, t.getToId());
		removeMap(byFromFactionId, t, t.getFromFactionId());
		removeMap(byToFactionId, t, t.getToFactionId());
		if(t.getFleetId() >= 0){
			fleetsInTrades.remove(t.getFleetId());
		}
		
	}
	private static void addMap(Long2ObjectOpenHashMap<List<TradeActive>> m, TradeActive v, long key){
		List<TradeActive> list = m.get(key);
		if(list == null){
			list = new ObjectArrayList();
			m.put(key, list);
		}
		list.add(v);
	}
	private static void addMap(Int2ObjectOpenHashMap<List<TradeActive>> m, TradeActive v, int key){
		List<TradeActive> list = m.get(key);
		if(list == null){
			list = new ObjectArrayList();
			m.put(key, list);
		}
		list.add(v);
	}
	private static void removeMap(Long2ObjectOpenHashMap<List<TradeActive>> m, TradeActive v, long key){
		List<TradeActive> list = m.get(key);
		if(list != null){
			list.remove(v);
			if(list.isEmpty()){
				m.remove(key);
			}
		}
	}
	private static void removeMap(Int2ObjectOpenHashMap<List<TradeActive>> m, TradeActive v, int key){
		List<TradeActive> list = m.get(key);
		if(list != null){
			list.remove(v);
			if(list.isEmpty()){
				m.remove(key);
			}
		}
	}
	public List<TradeActive> getTradeList() {
		return tradeList;
	}
	public Long2ObjectOpenHashMap<List<TradeActive>> getByFromId() {
		return byFromId;
	}
	public Long2ObjectOpenHashMap<List<TradeActive>> getByToId() {
		return byToId;
	}
	public Int2ObjectOpenHashMap<List<TradeActive>> getByFromFactionId() {
		return byFromFactionId;
	}
	public Int2ObjectOpenHashMap<List<TradeActive>> getByToFactionId() {
		return byToFactionId;
	}


	@Override
	public Tag toTagStructure() {
		
		return Tag.listToTagStruct(tradeList, null);
	}




	@Override
	public void fromTagStructure(Tag tag) {
		
		
		
		List<TradeActive> pr = new ObjectArrayList<TradeActive>();
		Tag.listFromTagStructSP(pr, tag, fromValue -> {
			TradeActive a = new TradeActive();
			a.fromTagStructure((Tag)fromValue);
			return a;
		});
		
		for(TradeActive t : pr){
			add(t);
		}
	}


	public LongOpenHashSet getFleetsInTrades() {
		return fleetsInTrades;
	}


	public void update() {
		boolean changed = false;
		for(TradeActive tradeActive : receivedTradeActive){
			if(tradeActive.isRemoveFlag()){
				remove(tradeActive);
			}else if(tradeActive.isChangedFlag()){
				int indexOf = tradeList.indexOf(tradeActive);
				if(indexOf >= 0){
					tradeList.get(indexOf).updateWith(tradeActive);
				}
			}else{
				if(!tradeList.contains(tradeActive)){
					add(tradeActive);
				}
			}
			changed = true;
		}
		receivedTradeActive.clear();
		if(changed){
			notifyObservers();
		}		
	}
	
	
}
