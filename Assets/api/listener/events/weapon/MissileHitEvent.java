package api.listener.events.weapon;

import api.listener.events.Event;
import org.schema.game.common.data.explosion.ExplosionData;
import org.schema.game.common.data.missile.Missile;
import org.schema.game.common.data.physics.CubeRayCastResult;

/**
 * Created by Jake on 1/6/2021.
 * <insert description here>
 */
public class MissileHitEvent extends Event {
    private final Missile missile;
    private final CubeRayCastResult raycast;
    private final ExplosionData explosionData;

    public MissileHitEvent(Missile missile, CubeRayCastResult raycast, ExplosionData explosionData) {

        this.missile = missile;
        this.raycast = raycast;
        this.explosionData = explosionData;
    }

    public Missile getMissile() {
        return missile;
    }

    public CubeRayCastResult getRaycast() {
        return raycast;
    }

    public ExplosionData getExplosionData() {
        return explosionData;
    }
}
