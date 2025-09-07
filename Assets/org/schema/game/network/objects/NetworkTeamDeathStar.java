package org.schema.game.network.objects;

import org.schema.game.common.controller.TeamDeathStar;
import org.schema.schine.network.StateInterface;

public class NetworkTeamDeathStar extends NetworkSegmentController {

	public NetworkTeamDeathStar(StateInterface state, TeamDeathStar teamDeathStar) {
		super(state, teamDeathStar);
	}

}
