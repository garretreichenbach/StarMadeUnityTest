package org.schema.game.mod.listeners;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.data.player.PlayerCharacter;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.objects.Sendable;

public interface PlayerStateListener extends ModListener {

	public void onPlayerKilled(PlayerState ps, Damager from);

	public void onPlayerCreated(PlayerState ps);

	public void onPlayerSpawned(PlayerState ps, PlayerCharacter spawnedObject);

	public void onPlayerCreditsChanged(PlayerState ps);

	public void onPlayerUpdate(PlayerState ps, Timer t);

	public void onPlayerSectorChanged(PlayerState ps);

	public void onPlayerRemoved(PlayerState s);

	public void onPlayerChangedContol(PlayerState ps, PlayerControllable to,
	                                  Vector3i toParameter, Sendable from, Vector3i fromParameter);

}
