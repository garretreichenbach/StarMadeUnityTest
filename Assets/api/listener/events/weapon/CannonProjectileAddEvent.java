package api.listener.events.weapon;

import api.listener.events.Event;
import org.schema.game.common.controller.damage.projectile.ProjectileController;
import org.schema.game.common.controller.damage.projectile.ProjectileParticleContainer;

public class CannonProjectileAddEvent extends Event {
    private final ProjectileController controllerf;
    private final ProjectileParticleContainer containerf;
    private final int indexf;

    public CannonProjectileAddEvent(ProjectileController controller, ProjectileParticleContainer particleContainer, int index) {
        controllerf = controller;
        containerf = particleContainer;
        indexf = index;
    }

    public ProjectileController getController() {
        return controllerf;
    }

    public ProjectileParticleContainer getContainer() {
        return containerf;
    }

    public int getIndex() {
        return indexf;
    }
}
