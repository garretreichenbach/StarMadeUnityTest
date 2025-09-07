package api.listener.events.player;

import api.listener.events.Event;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.data.player.Destroyable;
import org.schema.game.common.data.player.PlayerState;

public class PlayerDamageEvent extends Event {
    private final float damage;
    private final Destroyable destroyable;
    private final Damager damager;
    private final PlayerState player;

    public PlayerDamageEvent(float damage, Destroyable destroyable, Damager damager, PlayerState state){
        this.player = state;
        this.damage = damage;
        this.destroyable = destroyable;
        this.damager = damager;
    }

    public float getDamage() {
        return damage;
    }

    public Destroyable getDestroyable() {
        return destroyable;
    }

    public Damager getDamager() {
        return damager;
    }

    public PlayerState getPlayer() {
        return player;
    }
}
