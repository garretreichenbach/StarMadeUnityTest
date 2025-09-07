package org.schema.game.common.controller.ai;

import org.schema.game.common.controller.damage.Damager;

public interface HittableAIEntityState {

	void handleHitBy(float actualDamage, Damager from);

}
