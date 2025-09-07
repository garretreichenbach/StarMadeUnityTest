package org.schema.game.common.controller.database;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.FogOfWarController;
import org.schema.schine.network.StateInterface;

public interface FogOfWarReceiver {
	long getFogOfWarId();

	StateInterface getState();

	FogOfWarController getFogOfWar();

	void sendFowResetToClient(Vector3i sysTo);
}
