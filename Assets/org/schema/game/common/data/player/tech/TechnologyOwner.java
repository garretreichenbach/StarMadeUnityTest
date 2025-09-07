package org.schema.game.common.data.player.tech;

import org.schema.schine.network.StateInterface;

public interface TechnologyOwner {

	public StateInterface getState();

	public boolean isOnServer();

}
