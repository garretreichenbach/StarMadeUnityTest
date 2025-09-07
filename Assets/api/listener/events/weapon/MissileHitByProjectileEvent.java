package api.listener.events.weapon;

import api.listener.events.Event;
import org.schema.game.common.data.missile.Missile;

public class MissileHitByProjectileEvent extends Event {

    private final Missile missilef;
    private final int idf;
    private final float damagef;

    public MissileHitByProjectileEvent(Missile missile, int id, float damage) {

        missilef = missile;
        idf = id;
        damagef = damage;
    }

    public Missile getMissile() {
        return missilef;
    }

    public int getId() {
        return idf;
    }

    public float getDamage() {
        return damagef;
    }
}
