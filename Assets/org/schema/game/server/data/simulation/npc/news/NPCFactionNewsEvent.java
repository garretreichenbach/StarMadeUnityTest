package org.schema.game.server.data.simulation.npc.news;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.SerializationInterface;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.simulation.npc.news.NPCFactionNews.NPCFactionNewsEventType;
import org.schema.schine.common.language.Lng;
import org.schema.schine.resource.tag.SerializableTagElement;

public abstract class NPCFactionNewsEvent implements SerializationInterface, SerializableTagElement{
	public long time;
	public int factionId;
	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		b.writeLong(time);
		b.writeInt(factionId);
	}
	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
			boolean isOnServer) throws IOException {
		time = b.readLong();
		factionId = b.readInt();
	}
	@Override
	public byte getFactoryId() {
		return SerializableTagElement.NPC_FACTION_NEWS_EVENT;
	}
	@Override
	public void writeToTag(DataOutput dos) throws IOException {
		dos.writeByte(getType().ordinal());
		serialize(dos, true);
	}
	public String getOwnName(FactionState state){
		Faction faction = state.getFactionManager().getFaction(factionId);
		if(faction != null){
			return faction.getName();
		}else{
			return Lng.str("Unknown");
		}
	}
	
	public abstract NPCFactionNewsEventType getType();
	public abstract String getMessage(FactionState state);
	
}
