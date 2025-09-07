package org.schema.game.network.objects;

import org.schema.game.common.controller.Planet;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.remote.RemoteBoolean;
import org.schema.schine.network.objects.remote.RemoteLongPrimitive;
import org.schema.schine.network.objects.remote.RemoteString;

public class NetworkPlanet extends NetworkSpaceStation {

	public RemoteBoolean blownOff = new RemoteBoolean(false, this);
	public RemoteString planetUid = new RemoteString("none", this);
	public RemoteLongPrimitive seed = new RemoteLongPrimitive(0L, this);

	public NetworkPlanet(StateInterface state, Planet spaceStation) {
		super(state, spaceStation);
	}
}
