package org.schema.game.network.objects;

import org.schema.game.common.controller.PlanetIco;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.remote.RemoteBytePrimitive;
import org.schema.schine.network.objects.remote.RemoteFloatPrimitive;
import org.schema.schine.network.objects.remote.RemoteLongPrimitive;
import org.schema.schine.network.objects.remote.RemoteString;

public class NetworkPlanetIco extends NetworkSpaceStation {

	public RemoteLongPrimitive seed = new RemoteLongPrimitive(0L, this);
	public RemoteString coreUID = new RemoteString("none", this);
	public RemoteFloatPrimitive radius = new RemoteFloatPrimitive(0.0F, this);
	public RemoteBytePrimitive sideId = new RemoteBytePrimitive((byte) 0, this);

	public NetworkPlanetIco(StateInterface state, PlanetIco spaceStation) {
		super(state, spaceStation);
	}
}
