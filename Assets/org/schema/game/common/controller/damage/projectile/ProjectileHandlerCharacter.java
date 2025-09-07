package org.schema.game.common.controller.damage.projectile;

import javax.vecmath.Vector3f;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.projectile.ProjectileController.ProjectileHandleState;
import org.schema.game.common.data.physics.CubeRayCastResult;
import org.schema.game.common.data.physics.PairCachingGhostObjectAlignable;
import org.schema.game.common.data.physics.RigidBodySimple;
import org.schema.game.common.data.player.AbstractCharacter;

public class ProjectileHandlerCharacter extends ProjectileHandler{

	@Override
	public ProjectileHandleState handle(Damager damager, ProjectileController projectileController,
			Vector3f posBeforeUpdate, Vector3f posAfterUpdate, ProjectileParticleContainer particles, int particleIndex,
			CubeRayCastResult rayCallbackInitial) {
		if(rayCallbackInitial.hasHit()) {
			
			float damage = particles.getDamage(particleIndex);
			AbstractCharacter<?> c;
			if(rayCallbackInitial.collisionObject instanceof PairCachingGhostObjectAlignable) {
				
				
				
				PairCachingGhostObjectAlignable charObj = (PairCachingGhostObjectAlignable) rayCallbackInitial.collisionObject;
				c = (AbstractCharacter<?>) charObj.getObj();
			}else {
				//virtual object
				RigidBodySimple bdh = (RigidBodySimple)rayCallbackInitial.collisionObject;
				c = (AbstractCharacter<?>) bdh.getSimpleTransformableSendableObject();
			}
			
			if(damager.getOwnerState() != c.getOwnerState()) {
				
				if (!c.checkAttack(damager, true, true)) {
					return ProjectileHandleState.PROJECTILE_NO_HIT;
				}
				
				return damageCharacter(c, damage, damager, rayCallbackInitial.hitPointWorld);
			}else {
				return ProjectileHandleState.PROJECTILE_NO_HIT;
			}
		}
		return ProjectileHandleState.PROJECTILE_NO_HIT;
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



	private ProjectileHandleState damageCharacter(AbstractCharacter<?> c, float damage, Damager damager, Vector3f hitPointWorld) {
		
		if(c.isOnServer()) {
			if (!c.canAttack(damager)) {
				return ProjectileHandleState.PROJECTILE_NO_HIT_STOP;
			}
			c.damage(damage, damager);
		} else {
			// prepare to add some explosions
			GameClientState s = (GameClientState) c.getState();
			s.getWorldDrawer().getExplosionDrawer().addExplosion(hitPointWorld);
		
		}	
		return ProjectileHandleState.PROJECTILE_HIT_STOP;
	}

}
