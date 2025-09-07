package api.listener.events.player;

import api.listener.events.Event;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.inventory.FreeItem;

/**
 * Created by Jake on 7/10/2021.
 *
 * The name speaks for itself
 */
public class PlayerInventoryTooFullToPickupFreeItemEvent extends Event {
    private final PlayerState state;
    private final FreeItem item;

    public PlayerInventoryTooFullToPickupFreeItemEvent(PlayerState state, FreeItem item){

        this.state = state;
        this.item = item;
    }

    public PlayerState getPlayer() {
        return state;
    }

    public FreeItem getItem() {
        return item;
    }
}
