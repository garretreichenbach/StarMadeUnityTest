package api.listener.fastevents;

import org.schema.game.common.data.world.RemoteSector;
import org.schema.game.common.data.world.Sector;
import org.schema.schine.graphicsengine.core.Timer;

/**
 * Created by Jake on 2/28/2021.
 * <insert description here>
 */
public interface SectorUpdateListener {
    void local_preUpdate(Sector sector, Timer timer);
    void local_activeUpdate(Sector sector, Timer timer);
    void local_postUpdate(Sector sector, Timer timer);

    void remote_preUpdate(RemoteSector sector, Timer timer);
    void remote_postUpdate(RemoteSector sector, Timer timer);
}
