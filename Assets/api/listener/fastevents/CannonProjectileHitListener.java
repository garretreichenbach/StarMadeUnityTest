package api.listener.fastevents;

import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.projectile.ProjectileController;
import org.schema.game.common.controller.damage.projectile.ProjectileHandlerSegmentController;
import org.schema.game.common.controller.damage.projectile.ProjectileParticleContainer;
import org.schema.game.common.data.physics.CubeRayCastResult;

import javax.vecmath.Vector3f;

/**
 * Created by Jake on 12/30/2020.
 * <insert description here>
 */
public interface CannonProjectileHitListener {

	ProjectileController.ProjectileHandleState handle(Damager damager, ProjectileController projectileController, Vector3f posBeforeUpdate, Vector3f posAfterUpdate, ProjectileParticleContainer particles, int particleIndex, CubeRayCastResult rayCallbackInitial, ProjectileHandlerSegmentController projectileHandlerSegmentController);
	ProjectileController.ProjectileHandleState handleBefore(Damager damager, ProjectileController projectileController, Vector3f posBeforeUpdate, Vector3f posAfterUpdate, ProjectileParticleContainer particles, int particleIndex, CubeRayCastResult rayCallbackInitial, ProjectileHandlerSegmentController projectileHandlerSegmentController);
	ProjectileController.ProjectileHandleState handleAfterIfNotStopped(Damager damager, ProjectileController projectileController, Vector3f posBeforeUpdate, Vector3f posAfterUpdate, ProjectileParticleContainer particles, int particleIndex, CubeRayCastResult rayCallbackInitial, ProjectileHandlerSegmentController projectileHandlerSegmentController);
	void handleAfterAlways(Damager damager, ProjectileController projectileController, Vector3f posBeforeUpdate, Vector3f posAfterUpdate, ProjectileParticleContainer particles, int particleIndex, CubeRayCastResult rayCallbackInitial, ProjectileHandlerSegmentController projectileHandlerSegmentController);
}
