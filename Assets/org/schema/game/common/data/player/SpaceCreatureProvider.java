package org.schema.game.common.data.player;

import org.schema.game.common.data.SendableTypes;
import org.schema.game.network.objects.NetworkEntityProvider;
import org.schema.schine.network.SendableType;
import org.schema.schine.network.StateInterface;

public class SpaceCreatureProvider extends GenericProvider<AbstractCharacter<AbstractOwnerState>>{

	private NetworkEntityProvider networkEntityProvider;

	public SpaceCreatureProvider(StateInterface state) {
		super(state);
	}

	@Override
	public NetworkEntityProvider getNetworkObject() {
		return networkEntityProvider;
	}
	@Override
	public SendableType getSendableType() {
		return SendableTypes.SPACE_CREATURE_PROVIDER;
	}
	@Override
	public void newNetworkObject() {
		networkEntityProvider = new NetworkEntityProvider(getState());
	}
	@Override
	public boolean isPrivateNetworkObject(){
		return true;
	}
}
