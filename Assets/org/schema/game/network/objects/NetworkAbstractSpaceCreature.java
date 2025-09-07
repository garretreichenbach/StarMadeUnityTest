package org.schema.game.network.objects;

import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkEntity;
import org.schema.schine.network.objects.remote.RemoteString;

public abstract class NetworkAbstractSpaceCreature extends NetworkEntity {

	public RemoteString uniqueIdentifier = new RemoteString(this);
	public RemoteString realName = new RemoteString(this);

	public NetworkAbstractSpaceCreature(StateInterface state) {
		super(state);
	}

	@Override
	public void onDelete(StateInterface stateI) {
	}

	@Override
	public void onInit(StateInterface stateI) {
	}

}
