package org.schema.game.network.objects;

import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkEntity;
import org.schema.schine.network.objects.remote.RemoteBoolean;
import org.schema.schine.network.objects.remote.RemoteIntPrimitive;
import org.schema.schine.network.objects.remote.RemoteVector3f;

public class NetworkMissile extends NetworkEntity {

	public RemoteVector3f dir = new RemoteVector3f(this);
	public RemoteIntPrimitive targetId = new RemoteIntPrimitive(-1, this);
	public RemoteBoolean alive = new RemoteBoolean(true, this);

	public NetworkMissile(StateInterface state) {
		super(state);
	}

	@Override
	public void onDelete(StateInterface stateI) {
	}

	@Override
	public void onInit(StateInterface stateI) {
	}

}
