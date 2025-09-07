package api.listener.fastevents;

import org.schema.game.common.data.missile.Missile;
import org.schema.schine.graphicsengine.core.Timer;

/**
 * Created by Jake on 1/1/2021.
 * <insert description here>
 */
public interface MissileUpdateListener {
    void updateServer(Missile missile, Timer timer);
    void updateServerPost(Missile missile, Timer timer);
    void updateClient(Missile missile, Timer timer);
    void updateClientPost(Missile missile, Timer timer);
}
