package api.listener.events.entity;

import api.listener.events.Event;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.projectile.ProjectileController;
import org.schema.game.common.controller.damage.projectile.ProjectileHandlerSegmentController;
import org.schema.game.common.controller.damage.projectile.ProjectileParticleContainer;
import org.schema.game.common.data.physics.CubeRayCastResult;

public class SegmentHitByProjectileEvent extends Event {

    private final CubeRayCastResult rayCastResult;
    private final ProjectileHandlerSegmentController.ShotHandler shotHandler;
    private final ProjectileHandlerSegmentController projectileHandler;
    private final Damager damager;
    private final ProjectileController projectileController;
    private final ProjectileParticleContainer particles;
    private final int particleIndex;

    public SegmentHitByProjectileEvent(CubeRayCastResult rayCastResult, ProjectileHandlerSegmentController.ShotHandler shotHandler, ProjectileHandlerSegmentController projectileHandler, Damager damager, ProjectileController projectileController, ProjectileParticleContainer particles, int particleIndex) {
        this.rayCastResult = rayCastResult;
        this.shotHandler = shotHandler;
        this.projectileHandler = projectileHandler;
        this.damager = damager;
        this.projectileController = projectileController;
        this.particles = particles;
        this.particleIndex = particleIndex;
    }

    public CubeRayCastResult getRayCastResult() {
        return rayCastResult;
    }

    public ProjectileHandlerSegmentController.ShotHandler getShotHandler() {
        return shotHandler;
    }

    public ProjectileHandlerSegmentController getProjectileHandler() {
        return projectileHandler;
    }

    public Damager getDamager() {
        return damager;
    }

    public ProjectileController getProjectileController() {
        return projectileController;
    }

    public ProjectileParticleContainer getParticles() {
        return particles;
    }

    public int getParticleIndex() {
        return particleIndex;
    }
}
