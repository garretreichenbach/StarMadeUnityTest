package api.listener.events.weapon;

import api.listener.events.Event;
import org.schema.game.common.controller.elements.missile.MissileController;
import org.schema.game.common.data.missile.Missile;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;

import javax.annotation.Nullable;

/**
 * Created by Jake on 10/14/2020.
 * <insert description here>
 */
public class MissilePostAddEvent extends Event {
    private final MissileController missileController;
    private final Missile missile;

    public MissilePostAddEvent(MissileController missileController, Missile missile) {
        this.missileController = missileController;
        this.missile = missile;
    }

    public MissileController getMissileController() {
        return missileController;
    }

    public Missile getMissile() {
        return missile;
    }

    @Nullable
    public SimpleTransformableSendableObject<?> getShooter() {
        return missile.getShootingEntity();
    }
}
