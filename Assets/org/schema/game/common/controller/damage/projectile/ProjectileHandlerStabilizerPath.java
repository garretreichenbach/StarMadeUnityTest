package org.schema.game.common.controller.damage.projectile;

import javax.vecmath.Vector3f;

import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.projectile.ProjectileController.ProjectileHandleState;
import org.schema.game.common.controller.elements.power.reactor.StabilizerPath;
import org.schema.game.common.data.physics.CubeRayCastResult;

public class ProjectileHandlerStabilizerPath extends ProjectileHandler{

	@Override
	public ProjectileHandleState handle(Damager damager, ProjectileController projectileController,
			Vector3f posBeforeUpdate, Vector3f posAfterUpdate, ProjectileParticleContainer particles, int particleIndex,
			CubeRayCastResult rayCallbackInitial) {
		float initialDamage = particles.getDamage(particleIndex);
		
//		System.err.println(projectileController.getState()+" HIT STABILIZER PATH "+rayCallbackInitial.collisionObject.getUserPointer());
		((StabilizerPath)rayCallbackInitial.collisionObject.getUserPointer()).onHit(damager, initialDamage);
		return ProjectileHandleState.PROJECTILE_HIT_CONTINUE;
	}

	

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

}
