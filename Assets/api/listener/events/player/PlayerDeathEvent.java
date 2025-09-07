package api.listener.events.player;

import api.listener.events.Event;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.data.player.PlayerState;

public class PlayerDeathEvent extends Event {
    private PlayerState player;
    private Damager damager;

    public PlayerDeathEvent(PlayerState state, Damager var1) {
        this.player = state;
        this.damager = var1;
    }

    public PlayerState getPlayer() {
        return player;
    }

    public Damager getDamager() {
        return damager;
    }
}
