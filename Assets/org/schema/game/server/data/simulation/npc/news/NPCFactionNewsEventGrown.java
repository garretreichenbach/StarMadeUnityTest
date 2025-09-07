package org.schema.game.server.data.simulation.npc.news;

import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.simulation.npc.news.NPCFactionNews.NPCFactionNewsEventType;
import org.schema.schine.common.language.Lng;

public class NPCFactionNewsEventGrown extends NPCFactionNewsEventSystem{

	@Override
	public NPCFactionNewsEventType getType() {
		return NPCFactionNewsEventType.GROWN;
	}

	@Override
	public String getMessage(FactionState state) {
		return Lng.str("Faction %s has grown its territory to system %s",getOwnName(state), system.toStringPure());
	}

	

}
