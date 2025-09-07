package api.listener.events.weapon;

import api.listener.events.Event;
import org.schema.game.common.data.explosion.ExplosionData;
import org.schema.game.common.data.explosion.ExplosionRunnable;
import org.schema.game.common.data.world.Sector;

/**
 * Created by Jake on 12/8/2020.
 * Called whenever something explodes
 */
public class ExplosionEvent extends Event {

    private final ExplosionRunnable explosionRunnable;
    private final Sector sector;
    private final ExplosionData explosion;

    public ExplosionEvent(ExplosionRunnable explosionRunnable, Sector sector, ExplosionData explosion) {

        this.explosionRunnable = explosionRunnable;
        this.sector = sector;
        this.explosion = explosion;
    }

    public ExplosionRunnable getExplosionRunnable() {
        return explosionRunnable;
    }

    public Sector getSector() {
        return sector;
    }

    public ExplosionData getExplosion() {
        return explosion;
    }
}
