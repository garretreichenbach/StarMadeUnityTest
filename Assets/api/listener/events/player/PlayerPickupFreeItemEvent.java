package api.listener.events.player;

import api.listener.events.Event;
import api.listener.type.FiresTwiceForPreAndPost;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.inventory.FreeItem;

/**
 * Created by Jake on 7/10/2021.
 * <insert description here>
 */
@FiresTwiceForPreAndPost
public class PlayerPickupFreeItemEvent extends Event {
    private final PlayerState state;
    private final FreeItem item;

    public PlayerPickupFreeItemEvent(PlayerState state, FreeItem item, Condition cond){
        this.state = state;
        this.item = item;
        this.condition = cond;
    }

    public PlayerState getPlayer() {
        return state;
    }

    public FreeItem getItem() {
        return item;
    }
}
