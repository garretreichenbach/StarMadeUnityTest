package org.schema.game.common.controller.damage.projectile;

import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.data.physics.CubeRayCastResult;
import org.schema.game.common.data.physics.shape.SphereShapeExt;

import javax.vecmath.Vector3f;

public class ProjectileHandlerPlanetCore extends ProjectileHandler {

	@Override
	public ProjectileController.ProjectileHandleState handle(Damager damager, ProjectileController projectileController, Vector3f posBeforeUpdate, Vector3f posAfterUpdate, ProjectileParticleContainer particles, int particleIndex, CubeRayCastResult rayCallbackInitial) {
		if(rayCallbackInitial.hasHit() && rayCallbackInitial.collisionObject.getCollisionShape() instanceof SphereShapeExt) return ProjectileController.ProjectileHandleState.PROJECTILE_HIT_STOP;
		else return ProjectileController.ProjectileHandleState.PROJECTILE_NO_HIT;
	}

	@Override
	public ProjectileController.ProjectileHandleState handleBefore(Damager damager, ProjectileController projectileController, Vector3f posBeforeUpdate, Vector3f posAfterUpdate, ProjectileParticleContainer particles, int particleIndex, CubeRayCastResult rayCallbackInitial) {
		return ProjectileController.ProjectileHandleState.PROJECTILE_IGNORE;
	}

	@Override
	public ProjectileController.ProjectileHandleState handleAfterIfNotStopped(Damager damager, ProjectileController projectileController, Vector3f posBeforeUpdate, Vector3f posAfterUpdate, ProjectileParticleContainer particles, int particleIndex, CubeRayCastResult rayCallbackInitial) {
		return ProjectileController.ProjectileHandleState.PROJECTILE_IGNORE;
	}
}
