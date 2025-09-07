package org.schema.game.client.controller;

import org.schema.game.common.controller.ParticleEntry;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.network.objects.remote.RemoteParticleEntry;
import org.schema.game.server.data.GameServerState;

public class ParticleUtil {

    public static void sendToAllPlayers(GameServerState state, ParticleEntry particle) {
        for (PlayerState player : state.getPlayerStatesByName().values()) {
            sendToPlayer(player, particle);
        }
    }

    public static void sendToPlayer(PlayerState player, ParticleEntry particle) {
        player.getClientChannel().getNetworkObject().particles.add(new RemoteParticleEntry(particle, true));
    }
}
