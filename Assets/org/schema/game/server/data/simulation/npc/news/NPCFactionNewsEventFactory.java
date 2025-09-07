package org.schema.game.server.data.simulation.npc.news;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;

import org.schema.game.server.data.simulation.npc.news.NPCFactionNews.NPCFactionNewsEventType;
import org.schema.schine.resource.tag.SerializableTagFactory;

public class NPCFactionNewsEventFactory implements SerializableTagFactory{
	@Override
	public Object create(DataInput dis) throws IOException {
		NPCFactionNewsEventType type = NPCFactionNewsEventType.values()[dis.readByte()];
		NPCFactionNewsEvent e = type.instance();
		e.deserialize((DataInputStream) dis, 0, true);
		return e;
	}
}
