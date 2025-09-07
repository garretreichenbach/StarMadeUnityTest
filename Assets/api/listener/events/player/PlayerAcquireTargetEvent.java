package api.listener.events.player;

import api.listener.events.Event;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;

/**
 * Created by Jake on 10/24/2020.
 * <insert description here>
 */
public class PlayerAcquireTargetEvent extends Event {
    private final PlayerState player;
    private final SimpleTransformableSendableObject<?> target;

    public PlayerAcquireTargetEvent(PlayerState player, SimpleTransformableSendableObject<?> target){

        this.player = player;
        this.target = target;
    }

    public PlayerState getPlayer() {
        return player;
    }

    public SimpleTransformableSendableObject<?> getTarget() {
        return target;
    }
}
