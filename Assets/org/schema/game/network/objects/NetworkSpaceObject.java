package org.schema.game.network.objects;

import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkEntity;
import org.schema.schine.network.objects.remote.RemoteInteger;

public class NetworkSpaceObject extends NetworkEntity {

	public static final int TYPE_ASTEROID = 0;

	public static final int TYPE_ASTEROID_STYLE_1 = 0;
	public RemoteInteger starSystemId = new RemoteInteger(-1, this);

	public RemoteInteger objectType = new RemoteInteger(-1, this);
	public RemoteInteger objectSubtype = new RemoteInteger(-1, this);

	public NetworkSpaceObject(StateInterface state) {
		super(state);
		// TODO Auto-generated constructor stub
	}

	/**
	 * The transormation Matrix
	 */

	@Override
	public void onDelete(StateInterface stateI) {

	}

	@Override
	public void onInit(StateInterface stateI) {

	}

}
