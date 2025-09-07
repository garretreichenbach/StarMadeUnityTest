package org.schema.game.server.data.simulation.npc.news;

import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.simulation.npc.news.NPCFactionNews.NPCFactionNewsEventType;
import org.schema.schine.common.language.Lng;

public class NPCFactionNewsEventLostStation extends NPCFactionNewsEventOtherEnt{

	@Override
	public NPCFactionNewsEventType getType() {
		return NPCFactionNewsEventType.LOST_STATION;
	}

	@Override
	public String getMessage(FactionState state) {
		return Lng.str("Faction %s has lost its station %s",getOwnName(state), getOtherName(state));
	}

	

}
