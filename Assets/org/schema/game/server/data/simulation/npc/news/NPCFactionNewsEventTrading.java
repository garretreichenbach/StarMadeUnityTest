package org.schema.game.server.data.simulation.npc.news;

import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.simulation.npc.news.NPCFactionNews.NPCFactionNewsEventType;
import org.schema.schine.common.language.Lng;

public class NPCFactionNewsEventTrading extends NPCFactionNewsEventRoute{

	@Override
	public NPCFactionNewsEventType getType() {
		return NPCFactionNewsEventType.TRADING;
	}

	@Override
	public String getMessage(FactionState state) {
		return Lng.str("Scanners are showing that the faction %s is establishing a trade route from sector %s to %s",getOwnName(state), from.toStringPure(), to.toStringPure());
	}

	

}
