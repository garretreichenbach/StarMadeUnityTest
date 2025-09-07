package org.schema.game.network.objects;

import org.schema.game.common.controller.GasPlanet;
import org.schema.game.common.controller.SegmentController;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkEntity;
import org.schema.schine.network.objects.remote.RemoteByteArrayDyn;
import org.schema.schine.network.objects.remote.RemoteIntPrimitive;
import org.schema.schine.network.objects.remote.RemoteString;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class NetworkGasPlanet extends NetworkEntity {
	//public RemoteByteArrayDyn tagsInfo = new RemoteByteArrayDyn(new byte[]{},this);
	public RemoteString uid = new RemoteString("", this);
	public RemoteIntPrimitive radius = new RemoteIntPrimitive(1,this);
	public RemoteString name = new RemoteString("Gas Planet", this);

    public NetworkGasPlanet(StateInterface state) {
		super(state);
	}

	@Override
	public void onDelete(StateInterface stateI) {

	}

	@Override
	public void onInit(StateInterface stateI) {

	}
}
