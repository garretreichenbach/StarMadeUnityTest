package org.schema.game.server.data.simulation.npc.news;

import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.simulation.npc.news.NPCFactionNews.NPCFactionNewsEventType;
import org.schema.schine.common.language.Lng;

public class NPCFactionNewsEventLostSystem extends NPCFactionNewsEventSystem{

	@Override
	public NPCFactionNewsEventType getType() {
		return NPCFactionNewsEventType.LOST_TERRITORY;
	}

	@Override
	public String getMessage(FactionState state) {
		return Lng.str("Faction %s has lost the system %s",getOwnName(state), system.toStringPure());
	}

	

}
