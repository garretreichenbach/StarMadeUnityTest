package org.schema.game.server.data.simulation.npc.news;

import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.simulation.npc.news.NPCFactionNews.NPCFactionNewsEventType;
import org.schema.schine.common.language.Lng;

public class NPCFactionNewsEventWar extends NPCFactionNewsEventOtherEnt{

	@Override
	public NPCFactionNewsEventType getType() {
		return NPCFactionNewsEventType.WAR;
	}

	@Override
	public String getMessage(FactionState state) {
		return Lng.str("Faction %s is now at war with %s",getOwnName(state), getOtherName(state));
	}

	

}
