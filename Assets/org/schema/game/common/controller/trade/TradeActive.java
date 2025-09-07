package org.schema.game.common.controller.trade;

import api.listener.events.player.BuyTradeEvent;
import api.listener.events.player.SellTradeEvent;
import api.mod.StarLoader;
import com.bulletphysics.util.ObjectArrayList;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import org.schema.common.SerializationInterface;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.controller.elements.shop.ShopElementManager;
import org.schema.game.common.controller.trade.TradeOrder.TradeOrderElement;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetCommandTypes;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.network.objects.remote.FleetCommand;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.schine.common.language.Lng;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class TradeActive implements TagSerializable, SerializationInterface{

	public static final byte VERSION = 0; 
	
	public enum UpdateState{
		NO_CHANGE,
		CHANGED,
		EXECUTE, 
		CANCEL
	}
	
	private ElementCountMap blocks = new ElementCountMap();
	private long blockPrice;
	private long deliveryPrice;
	private long startTime;
	private long fromId;
	private long toId;
	private int fromFactionId;
	private int toFactionId;
	private String fromStation;
	private String toStation;
	private String fromPlayer;
	private String toPlayer;
	private Vector3i startSystem;
	private Vector3i targetSystem;
	private double volume;
	private long fleetId = -1;
	private List<Vector3i> sectorWayPoints = new ObjectArrayList<Vector3i>();
	private Vector3i currentSector;
	
	public long lastSectorChangeServer;
	
	private boolean removeFlag;
	private boolean changedFlag;
	private Vector3i startSector;

	
	
	@Override
	protected TradeActive clone() {
		TradeActive t = new TradeActive();
		t.blocks = blocks;
		t.blockPrice = blockPrice;
		t.deliveryPrice = deliveryPrice;
		t.startTime = startTime;
		t.fromId = fromId;
		t.toId = toId;
		t.fromFactionId = fromFactionId;
		t.toFactionId = toFactionId;
		t.fromPlayer = fromPlayer;
		t.toPlayer = toPlayer;
		t.startSystem = startSystem;
		t.targetSystem = targetSystem;
		t.volume = volume;
		t.fleetId = fleetId;
		t.sectorWayPoints = sectorWayPoints;
		t.fromStation = fromStation;
		t.toStation = toStation;
		t.currentSector = currentSector;
		
		return t;
	}

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] vB = (Tag[])tag.getValue();
		byte version = (Byte) vB[0].getValue();
		switch(version) {
			case 0 -> fromTagVersion0((Tag[]) vB[1].getValue());
		}
	}

	
	
	private void fromTagVersion0(Tag[] v) {
		int i = 0;
		blocks = (ElementCountMap) v[i++].getValue();
		blockPrice = (Long)v[i++].getValue();
		deliveryPrice = (Long)v[i++].getValue();
		startTime = (Long)v[i++].getValue();
		fromId = (Long)v[i++].getValue();
		toId = (Long)v[i++].getValue();
		fleetId = (Long)v[i++].getValue();
		volume = (Double)v[i++].getValue();
		startSystem = (Vector3i)v[i++].getValue();
		targetSystem = (Vector3i)v[i++].getValue();
		currentSector = (Vector3i)v[i++].getValue();
		fromFactionId = (Integer)v[i++].getValue();
		toFactionId = (Integer)v[i++].getValue();
		fromPlayer = (String)v[i++].getValue();
		toPlayer = (String)v[i++].getValue();
		fromStation = (String)v[i++].getValue();
		toStation = (String)v[i++].getValue();
		startSector = (Vector3i)v[i++].getValue();
		Tag.listFromTagStruct(sectorWayPoints, v[i++]);
	}

	@Override
	public Tag toTagStructure() {
		return new Tag(Type.STRUCT, null, new Tag[]{
			new Tag(Type.BYTE, null, VERSION),
			new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.SERIALIZABLE, null, blocks),
				new Tag(Type.LONG, null, blockPrice),
				new Tag(Type.LONG, null, deliveryPrice),
				new Tag(Type.LONG, null, startTime),
				new Tag(Type.LONG, null, fromId),
				new Tag(Type.LONG, null, toId),
				new Tag(Type.LONG, null, fleetId),
				new Tag(Type.DOUBLE, null, volume),
				new Tag(Type.VECTOR3i, null, startSystem),
				new Tag(Type.VECTOR3i, null, targetSystem),
				new Tag(Type.VECTOR3i, null, currentSector),
				new Tag(Type.INT, null, fromFactionId),
				new Tag(Type.INT, null, toFactionId),
				new Tag(Type.STRING, null, fromPlayer),
				new Tag(Type.STRING, null, toPlayer),
				new Tag(Type.STRING, null, fromStation),
				new Tag(Type.STRING, null, toStation),
				new Tag(Type.VECTOR3i, null, startSector),
				Tag.listToTagStruct(sectorWayPoints, Type.VECTOR3i, null),
				FinishTag.INST
			}),
			FinishTag.INST,
		});
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		b.writeBoolean(removeFlag);
		if(removeFlag){
			b.writeLong(fromId);
			b.writeLong(toId);
			b.writeLong(startTime);
		}else{
			b.writeBoolean(changedFlag);

			if(changedFlag){
				b.writeLong(fromId);
				b.writeLong(toId);
				b.writeLong(startTime);
				b.writeLong(fleetId);
				currentSector.serialize(b);
			}else{
				blocks.serialize(b);
				b.writeLong(blockPrice);
				b.writeLong(deliveryPrice);
				b.writeLong(startTime);
				b.writeLong(fromId);
				b.writeLong(toId);
				b.writeLong(fleetId);
				b.writeDouble(volume);
				startSystem.serialize(b);
				targetSystem.serialize(b);
				currentSector.serialize(b);
				startSector.serialize(b);
				b.writeInt(fromFactionId);
				b.writeInt(toFactionId);
				b.writeUTF(fromPlayer);
				b.writeUTF(toPlayer);
				b.writeUTF(fromStation);
				b.writeUTF(toStation);
				
				b.writeInt(sectorWayPoints.size());
				for(int i = 0; i < sectorWayPoints.size(); i++){
					sectorWayPoints.get(i).serialize(b);
				}
				}
		}
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
			boolean isOnServer) throws IOException {
		
		removeFlag = b.readBoolean();
		
		if(removeFlag){
			fromId = b.readLong();
			toId = b.readLong();
			startTime = b.readLong();
		}else{
			changedFlag = b.readBoolean();
			if(changedFlag){
				fromId = b.readLong();
				toId = b.readLong();
				startTime = b.readLong();
				fleetId = b.readLong();
				currentSector = Vector3i.deserializeStatic(b);
			}else{
			
				blocks.deserialize(b);
				blockPrice = b.readLong();
				deliveryPrice = b.readLong();
				startTime = b.readLong();
				fromId = b.readLong();
				toId = b.readLong();
				fleetId = b.readLong();
				volume = b.readDouble();
				startSystem = Vector3i.deserializeStatic(b);
				targetSystem = Vector3i.deserializeStatic(b);
				currentSector = Vector3i.deserializeStatic(b);
				startSector = Vector3i.deserializeStatic(b);
				fromFactionId = b.readInt();
				toFactionId = b.readInt();
				fromPlayer = b.readUTF();
				toPlayer = b.readUTF();
				fromStation = b.readUTF();
				toStation = b.readUTF();
				
				int s = b.readInt();
				for(int i = 0; i < s; i++){
					sectorWayPoints.add(Vector3i.deserializeStatic(b));
				}
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (fromId ^ (fromId >>> 32));
		result = prime * result + (int) (toId ^ (toId >>> 32));
		result = prime * result + (int) (startTime ^ (startTime >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		TradeActive other = (TradeActive) obj;
		return fromId == other.fromId && toId == other.toId && startTime == other.startTime;
	}

	public ElementCountMap getBlocks() {
		return blocks;
	}

	public long getBlockPrice() {
		return blockPrice;
	}

	public long getDeliveryPrice() {
		return deliveryPrice;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getFromId() {
		return fromId;
	}

	public Vector3i getStartSector() {
		return startSector;
	}

	public long getToId() {
		return toId;
	}

	public int getFromFactionId() {
		return fromFactionId;
	}

	public int getToFactionId() {
		return toFactionId;
	}

	public String getFromPlayer() {
		return fromPlayer;
	}

	public String getToPlayer() {
		return toPlayer;
	}

	public Vector3i getStartSystem() {
		return startSystem;
	}

	public Vector3i getTargetSystem() {
		return targetSystem;
	}

	public double getVolume() {
		return volume;
	}

	public long getFleetId() {
		return fleetId;
	}

	public List<Vector3i> getSectorWayPoints() {
		return sectorWayPoints;
	}

	public Vector3i getCurrentSector() {
		return currentSector;
	}

	public boolean isRemoveFlag() {
		return removeFlag;
	}

	public void setRemoveFlag(boolean removeFlag) {
		this.removeFlag = removeFlag;
	}

	public boolean isChangedFlag() {
		return changedFlag;
	}

	public void setChangedFlag(boolean changedFlag) {
		this.changedFlag = changedFlag;
	}

	public void updateWith(TradeActive o) {
		fleetId = o.fleetId;
		currentSector = new Vector3i(o.currentSector);
	}

	public String getFromStation() {
		return fromStation;
	}

	public String getToStation() {
		return toStation;
	}

	public void initiateBuyTrade(GameServerState state, TradeNodeStub buyer, TradeNodeStub seller,
			TradeOrder t) throws TradeInvalidException {
		
		TradeNodeStub from = seller;
		TradeNodeStub to = buyer;
		//SHIPS ARE MOVING BLOCKS FROM Seller to Buyer
		
		for(TradeOrderElement e : t.getOrders()){
			if(e.isBuyOrder()){
				blocks.inc(e.type, e.amount);
			}
		}
		volume = t.getBuyVolume();
		blockPrice = t.getBuyPrice();
		
		fillRest(state, t, from, to);
		
		fleetId = t.assignFleetBuy(buyer, seller, startSector, sectorWayPoints.get(0));

		//INSERTED CODE @382
		StarLoader.fireEvent(new BuyTradeEvent(t, buyer, seller, this), true);
		//
	}
	public void initiateSellTrade(GameServerState state, TradeNodeStub seller, TradeNodeStub buyer,
			TradeOrder t) throws TradeInvalidException {
		TradeNodeStub from = seller;
		TradeNodeStub to = buyer;
		//SHIPS ARE MOVING BLOCKS FROM Buyer to Seller
		
		for(TradeOrderElement e : t.getOrders()){
			if(!e.isBuyOrder()){
				blocks.inc(e.type, e.amount);
			}
		}
		volume = t.getSellVolume();
		blockPrice = t.getSellPrice();
		
		fillRest(state, t, from, to);
		
		fleetId = t.assignFleetSell(buyer, seller, startSector, sectorWayPoints.get(0));
		//INSERTED CODE @400
		StarLoader.fireEvent(new SellTradeEvent(t, seller, buyer, this), true);
		//
	}
	private void fillRest(GameServerState state, TradeOrder t, TradeNodeStub from, TradeNodeStub to) throws TradeInvalidException{
		deliveryPrice = t.getTradingGuildPrice();
		fromId = from.getEntityDBId();
		toId = to.getEntityDBId();
		startSystem = new Vector3i(from.getSystem());
		targetSystem = new Vector3i(to.getSystem());
		
		Vector3i fromSec = null;
		Vector3i toSec = null;
		try {
			fromSec = state.getDatabaseIndex().getTableManager().getEntityTable().getSectorOfEntityByDBId(from.getEntityDBId());
			toSec = state.getDatabaseIndex().getTableManager().getEntityTable().getSectorOfEntityByDBId(to.getEntityDBId());
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		if(fromSec == null || toSec == null){
			throw new TradeInvalidException("Can't find start or target shop in database!");
		}
		
		sectorWayPoints.add(toSec);
		startSector = new Vector3i(fromSec);
		currentSector = fromSec;
		fromStation = from.getStationName();
		toStation = to.getStationName();
		fromPlayer = from.getOwnerString();
		toPlayer = to.getOwnerString();
		startTime = System.currentTimeMillis();
		fromFactionId = from.getFactionId();
		toFactionId = to.getFactionId();
	}

	public float getDistance() {
		if(sectorWayPoints.isEmpty()){
			return 0;
		}
		float disatance = Vector3i.getDisatance(currentSector, sectorWayPoints.get(0));
		return disatance;
	}
	public long getEstimatedDuration() {
		return (long) ((double)(getDistance()+1) * (long)(ShopElementManager.TRADING_GUILD_TIME_PER_SECTOR_SEC * 1000D));
	}

	public boolean canView(PlayerState player) {
		{
			String s = fromPlayer.toLowerCase(Locale.ENGLISH);
			String name = player.getName().toLowerCase(Locale.ENGLISH);
			if(s.contains(name+";") || s.endsWith(";"+name) || s.equals(name)){
				return true;
			}
		}
		{
			String s = toPlayer.toLowerCase(Locale.ENGLISH);
			String name = player.getName().toLowerCase(Locale.ENGLISH);
			if(s.contains(name+";") || s.endsWith(";"+name) || s.equals(name)){
				return true;
			}
		}
		
		
		return player.getFactionId() == fromFactionId || player.getFactionId() == toFactionId;
	}

	
	public void sendMembersMessages(GameServerState state, String topic, String msg){
		String from = fromPlayer;
		String to = toPlayer;
		boolean sendA = true;
		boolean sendB = true;
		if(FactionManager.isNPCFaction(fromFactionId)){
			Faction faction = state.getFactionManager().getFaction(fromFactionId);
			if(faction != null){
				fromPlayer = faction.getName();
				sendA = false;
			}
		}
		if(FactionManager.isNPCFaction(toFactionId)){
			Faction faction = state.getFactionManager().getFaction(toFactionId);
			if(faction != null){
				sendB = false;
				toPlayer = faction.getName();
			}
		}
		
		if(sendA){
			state.getServerPlayerMessager().send(
					"<system>", fromPlayer, Lng.str("Trading between %s and %s: %s",fromPlayer, toPlayer,topic), msg);
		}
		if(sendB){
			state.getServerPlayerMessager().send(
					"<system>", toPlayer,Lng.str("Trading between %s and %s: %s",fromPlayer, toPlayer,topic), msg);
		}
		
	}

	//INSERTED CODE @496
	public boolean cancelTrade = false;
	//
	public UpdateState update(GameServerState state, long currentTime) {
		
		if(fleetId >= 0){
			
			Fleet f = state.getFleetManager().getByFleetDbId(fleetId);
			if(f != null && !f.isEmpty()){
				if(f.activeTradeRoute == null){
					f.activeTradeRoute = this;
					state.getFleetManager().executeCommand(new FleetCommand(
							FleetCommandTypes.TRADE_FLEET, f, sectorWayPoints.get(0)));
				}
				if(f.getFlagShip() == null || currentSector.equals(f.getFlagShip().getSector())){
					if(f.getFlagShip() == null){
						System.err.println("[SERVER][ERROR] Faction Trade flagship null");
					}
					return UpdateState.NO_CHANGE;
				}else{
					currentSector.set(f.getFlagShip().getSector());
					
					if(!sectorWayPoints.isEmpty()){
						if(currentSector.equals(sectorWayPoints.get(0))){
							sectorWayPoints.remove(0);
							if(!sectorWayPoints.isEmpty()){
								state.getFleetManager().executeCommand(new FleetCommand(
										FleetCommandTypes.TRADE_FLEET, f, sectorWayPoints.get(0)));
							}else{
								System.err.println("[FLEET] REMOVING TRADE FLEET "+f);
								if(f.isNPCFleetGeneral()){
									Faction faction = state.getFactionManager().getFaction(f.getNpcFaction());
									if(faction != null && faction.isNPC()){
										System.err.println("[FLEET] PURGING TRADE FLEET "+f);
										((NPCFaction)faction).getFleetManager().onFinishedTrade(f);
									}
								}
								return UpdateState.EXECUTE;
							}
						}
					}
					
					return UpdateState.CHANGED;
				}
				
			}else{
				
				sendMembersMessages(state, Lng.str("Trade Canceled"), Lng.str("Trade canceled because fleet was lost!"));
				return UpdateState.CANCEL;
			}
			
		}else{
		
			boolean turn =  currentTime - lastSectorChangeServer > (long)(ShopElementManager.TRADING_GUILD_TIME_PER_SECTOR_SEC * 1000D);
			if(turn){
				if(!sectorWayPoints.isEmpty()){
					currentSector.set(getNearest(currentSector, sectorWayPoints.get(0)));
					lastSectorChangeServer = currentTime;
					if(currentSector.equals(sectorWayPoints.get(0))){
						sectorWayPoints.remove(0);
					}
					return UpdateState.CHANGED;
				}else{
					return UpdateState.EXECUTE;
				}
			}
			return UpdateState.NO_CHANGE;
		}
	}
	
	private Vector3i getNearest(Vector3i from, Vector3i to){
		if (from.equals(to)) {
			return from;
		}
		Vector3i tmp = new Vector3i();
		Vector3i out = new Vector3i();
		float minDist = -1;
		for (int i = 0; i < Element.DIRECTIONSi.length; i++) {

			tmp.add(from, Element.DIRECTIONSi[i]);

			if (tmp.equals(to)) {
				return to;
			}

			float dist = Vector3i.getDisatance(tmp, to);
			if (minDist < 0 || dist < minDist) {
				out.set(tmp);
				minDist = dist;
			}
		}
		return out;
	}

	@Override
	public String toString() {
		return "TradeActive [blocksTotal=" + blocks.getTotalAmount() + ", blockPrice=" + blockPrice
				+ ", deliveryPrice=" + deliveryPrice + ", startTime="
				+ startTime + ", fromId=" + fromId + ", toId=" + toId
				+ ", fromFactionId=" + fromFactionId + ", toFactionId="
				+ toFactionId + ", fromStation=" + fromStation + ", toStation="
				+ toStation + ", fromPlayer=" + fromPlayer + ", toPlayer="
				+ toPlayer + ", startSystem=" + startSystem + ", targetSystem="
				+ targetSystem + ", volume=" + volume + ", fleetId=" + fleetId
				+ ", sectorWayPoints=" + sectorWayPoints + ", currentSector="
				+ currentSector + "]";
	}
	
	
}
