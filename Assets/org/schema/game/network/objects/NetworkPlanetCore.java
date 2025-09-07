package org.schema.game.network.objects;

import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkEntity;
import org.schema.schine.network.objects.remote.RemoteFloatPrimitive;
import org.schema.schine.network.objects.remote.RemoteString;
import org.schema.schine.network.objects.remote.RemoteStringArray;

public class NetworkPlanetCore extends NetworkEntity {

	public RemoteFloatPrimitive radius = new RemoteFloatPrimitive(200, this);
	public RemoteString uid = new RemoteString("", this);
	public RemoteFloatPrimitive hp = new RemoteFloatPrimitive(1, this);
	public RemoteStringArray plates = new RemoteStringArray(20, this);
	public RemoteString planetType = new RemoteString("", this);

	public NetworkPlanetCore(StateInterface state) {
		super(state);
	}

	@Override
	public void onDelete(StateInterface stateI) {

	}

	@Override
	public void onInit(StateInterface stateI) {

	}

}
