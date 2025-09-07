package org.schema.game.network.objects;

import org.schema.game.common.controller.Vehicle;
import org.schema.schine.network.StateInterface;

public class NetworkVehicle extends NetworkSegmentController {

	public NetworkVehicle(StateInterface state, Vehicle spaceStation) {
		super(state, spaceStation);
	}

}
