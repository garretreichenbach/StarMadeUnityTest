package org.schema.game.common.data.player;

import org.schema.game.common.data.SendableTypes;
import org.schema.game.network.objects.NetworkEntityProvider;
import org.schema.schine.network.SendableType;
import org.schema.schine.network.StateInterface;

public class CharacterProvider extends GenericProvider<AbstractCharacter<AbstractOwnerState>>{

	private NetworkEntityProvider networkEntityProvider;

	public CharacterProvider(StateInterface state) {
		super(state);
	}

	@Override
	public NetworkEntityProvider getNetworkObject() {
		return networkEntityProvider;
	}

	@Override
	public void newNetworkObject() {
		networkEntityProvider = new NetworkEntityProvider(getState());
	}
	@Override
	public boolean isPrivateNetworkObject(){
		return true;
	}
	@Override
	public SendableType getSendableType() {
		return SendableTypes.CHARACTER_PROVIDER;
	}
}
