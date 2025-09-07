package org.schema.game.network.objects;

import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.remote.RemoteIntPrimitive;
import org.schema.schine.network.objects.remote.RemoteVector3f;

public class NetworkPlayerHumanCharacter extends NetworkPlayerCharacter {
	public RemoteIntPrimitive clientOwnerId = new RemoteIntPrimitive(-1, this);
	public RemoteIntPrimitive spawnOnObjectId = new RemoteIntPrimitive(-1, this);
	public RemoteVector3f spawnOnObjectLocalPos = new RemoteVector3f(this);

	public NetworkPlayerHumanCharacter(StateInterface state) {
		super(state);
	}

}
