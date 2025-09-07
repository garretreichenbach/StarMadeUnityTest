package org.schema.game.server.data.simulation.npc.news;

import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.simulation.npc.news.NPCFactionNews.NPCFactionNewsEventType;
import org.schema.schine.common.language.Lng;

public class NPCFactionNewsEventNeutral extends NPCFactionNewsEventOtherEnt{

	@Override
	public NPCFactionNewsEventType getType() {
		return NPCFactionNewsEventType.PEACE;
	}

	@Override
	public String getMessage(FactionState state) {
		return Lng.str("Faction %s is now at peace with %s",getOwnName(state), getOtherName(state));
	}

	

}
