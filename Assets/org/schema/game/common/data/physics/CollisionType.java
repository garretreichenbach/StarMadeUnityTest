package org.schema.game.common.data.physics;

import org.schema.game.common.controller.damage.projectile.*;

public enum CollisionType {
	CUBE_STRUCTURE(ProjectileHandlerSegmentController::new),
	SIMPLE(ProjectileHandlerDefault::new),
	CHARACTER(ProjectileHandlerCharacter::new),
	CHARACTER_SIMPLE(ProjectileHandlerDefault::new),
	MISSILE(ProjectileHandlerDefault::new),
	PULSE(ProjectileHandlerDefault::new),
	ENERGY_STREAM(ProjectileHandlerStabilizerPath::new),
	DEBRIS(ProjectileHandlerShard::new),
	PLANET_CORE(ProjectileHandlerIgnore::new),
	GAS_PLANET(ProjectileHandlerIgnore::new),
	LIFT(ProjectileHandlerIgnore::new),
	OTHER(ProjectileHandlerDefault::new);

	public final ProjectileHandlerFactory projectileHandlerFactory;

	CollisionType(ProjectileHandlerFactory projectileHandlerFactory){
		this.projectileHandlerFactory = projectileHandlerFactory;
	}
}
