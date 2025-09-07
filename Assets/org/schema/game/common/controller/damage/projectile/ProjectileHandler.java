package org.schema.game.common.controller.damage.projectile;

import javax.vecmath.Vector3f;

import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.projectile.ProjectileController.ProjectileHandleState;
import org.schema.game.common.data.physics.CubeRayCastResult;

public abstract class ProjectileHandler {
	public abstract ProjectileHandleState handle(Damager damager, ProjectileController projectileController,
			Vector3f posBeforeUpdate, Vector3f posAfterUpdate, ProjectileParticleContainer particles, int particleIndex,
			CubeRayCastResult rayCallbackInitial);
	public abstract ProjectileHandleState handleBefore(Damager damager, ProjectileController projectileController,
			Vector3f posBeforeUpdate, Vector3f posAfterUpdate, ProjectileParticleContainer particles, int particleIndex,
			CubeRayCastResult rayCallbackInitial);
	public abstract ProjectileHandleState handleAfterIfNotStopped(Damager damager, ProjectileController projectileController,
			Vector3f posBeforeUpdate, Vector3f posAfterUpdate, ProjectileParticleContainer particles, int particleIndex,
			CubeRayCastResult rayCallbackInitial);
	public void afterHandleAlways(Damager damager, ProjectileController projectileController,
			Vector3f posBeforeUpdate, Vector3f posAfterUpdate, ProjectileParticleContainer particles, int particleIndex,
			CubeRayCastResult rayCallbackInitial) {
		
	}
}
