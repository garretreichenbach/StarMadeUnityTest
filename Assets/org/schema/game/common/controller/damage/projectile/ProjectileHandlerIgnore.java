package org.schema.game.common.controller.damage.projectile;

import javax.vecmath.Vector3f;

import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.projectile.ProjectileController.ProjectileHandleState;
import org.schema.game.common.data.physics.CubeRayCastResult;

public class ProjectileHandlerIgnore extends ProjectileHandler{

	

	@Override
	public ProjectileHandleState handleBefore(Damager damager,
			ProjectileController projectileController,
			Vector3f posBeforeUpdate, Vector3f posAfterUpdate,
			ProjectileParticleContainer particles, int particleIndex,
			CubeRayCastResult rayCallbackInitial) {
		return ProjectileHandleState.PROJECTILE_IGNORE;
	}

	@Override
	public ProjectileHandleState handleAfterIfNotStopped(Damager damager,
			ProjectileController projectileController,
			Vector3f posBeforeUpdate, Vector3f posAfterUpdate,
			ProjectileParticleContainer particles, int particleIndex,
			CubeRayCastResult rayCallbackInitial) {
		return ProjectileHandleState.PROJECTILE_IGNORE;
	}

	@Override
	public ProjectileHandleState handle(Damager damager, ProjectileController projectileController,
			Vector3f posBeforeUpdate, Vector3f posAfterUpdate, ProjectileParticleContainer particles, int particleIndex,
			CubeRayCastResult rayCallbackInitial) {
		return ProjectileHandleState.PROJECTILE_IGNORE;
	}
}
