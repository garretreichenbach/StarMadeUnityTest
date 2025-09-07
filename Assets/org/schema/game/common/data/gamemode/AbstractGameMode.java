package org.schema.game.common.data.gamemode;

import org.schema.game.common.controller.FactionChange;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.network.objects.NetworkGameState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.Timer;

public abstract class AbstractGameMode {
	public final GameServerState state;

	public AbstractGameMode(GameServerState state) {
		this.state = state;
	}

	public void initialize() throws GameModeException {

		System.err.println("[BATTLEMODE] initializing");

		initBegin();

		onFactionInitServer(state.getFactionManager());

		initEnd();
	}

	public abstract void updateToFullNT(NetworkGameState n);

	protected abstract void initBegin() throws GameModeException;

	protected abstract void initEnd() throws GameModeException;

	public abstract void onFactionInitServer(FactionManager factionManager);

	public abstract void update(Timer timer) throws GameModeException;

	public abstract String getCurrentOutput();

	public abstract boolean allowedToSpawnBBShips(PlayerState playerState, Faction f);

	public abstract void announceKill(PlayerState playerState, int killerEntity);

	public abstract void onFactionChanged(PlayerState playerState, FactionChange factionChange);
}
