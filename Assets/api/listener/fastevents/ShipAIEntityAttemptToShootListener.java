package api.listener.fastevents;

import org.schema.game.server.ai.AIControllerStateUnit;
import org.schema.game.server.ai.ShipAIEntity;
import org.schema.schine.graphicsengine.core.Timer;

/**
 * Created by Jake on 7/26/2021.
 * Called when an AI Entity attempts to shoot at a target
 *
 * Refer to ShipAIEntity.doShooting() for examples
 */
public interface ShipAIEntityAttemptToShootListener{
    void doShooting(ShipAIEntity shipAIEntity, AIControllerStateUnit<?> unit, Timer timer);
}
