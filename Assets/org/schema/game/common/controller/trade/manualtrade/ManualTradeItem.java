package org.schema.game.common.controller.trade.manualtrade;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.SerializationInterface;
import org.schema.game.common.controller.trade.manualtrade.ManualTrade.Mode;
import org.schema.game.common.data.MetaObjectState;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.element.meta.MetaObjectManager;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.server.ServerStateInterface;

public class ManualTradeItem implements SerializationInterface{
	
	public byte side;
	public short type;
	public int amount;
	public int metaId;
	public short itemId;
	public int tradeId;
	public boolean acc;
	public boolean join;
	
	public Mode mode = Mode.ITEM;
	StateInterface state;
	
	public ManualTradeItem(){}
	public ManualTradeItem(StateInterface state, int tradeId, byte side) {
		this.state = state;
		this.tradeId = tradeId;
		this.side = side;
		
	}
	
	public ManualTradeItem(StateInterface state, int tradeId, byte side, short type, int amount, int metaId,
			short itemId) {
		super();
		this.state = state;
		this.tradeId = tradeId;
		this.side = side;
		this.type = type;
		this.amount = amount;
		this.metaId = metaId;
		this.itemId = itemId;
	}

	private boolean requested;
	
	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		b.writeInt(tradeId);
		b.writeByte(side);
		b.writeByte(mode.ordinal());
		
		if(mode == Mode.ITEM){
			b.writeShort(itemId);
			b.writeShort(type);
			b.writeInt(amount);
			b.writeInt(metaId);
		}else if(mode == Mode.ACCEPT){
			b.writeBoolean(acc); 
		}else if(mode == Mode.JOIN){
			b.writeBoolean(join); 
		}if(mode == Mode.CANCEL){
		}else{
			throw new IllegalArgumentException(mode.toString());
		}
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
			boolean isOnServer) throws IOException {
		tradeId = b.readInt();
		side = b.readByte();
		mode = Mode.values()[b.readByte()];
		
		if(mode == Mode.ITEM){
			itemId = b.readShort();
			type = b.readShort();
			amount = b.readInt();
			metaId = b.readInt();			
		}else if(mode == Mode.ACCEPT){
			acc = b.readBoolean(); 
		}else if(mode == Mode.JOIN){
			join = b.readBoolean(); 
		}else if(mode == Mode.CANCEL){
		}else{
			throw new IllegalArgumentException(mode.toString());
		}
	}
	
	@Override
	public String toString(){
		if(type > 0){
			ElementInformation info = ElementKeyMap.getInfo(type);
			return info.getName();
		}else if(type < 0){
			MetaObjectManager ms = ((MetaObjectState)state).getMetaObjectManager();
			if(!isOnServer() && !requested){
				ms.checkAvailable(metaId, ((MetaObjectState)state));
				requested = true;
			}
			MetaObject object = ms.getObject(metaId);
			if(object == null){
				return Lng.str("unknown object (requesting...)");
			}else{
				return object.getName();
			}
		}else{
			return Lng.str("Credits");
		}
	}
	
	private boolean isOnServer() {
		return state instanceof ServerStateInterface;
	}

	@Override
	public boolean equals(Object obj) {
		
		ManualTradeItem other = (ManualTradeItem) obj;
		
		return itemId == other.itemId;
	}

	@Override
	public int hashCode() {
		return itemId;
	}
}
