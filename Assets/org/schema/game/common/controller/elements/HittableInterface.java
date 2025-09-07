package org.schema.game.common.controller.elements;

import org.schema.game.common.controller.damage.DamageDealerType;

public interface HittableInterface {

	public void onHit(long pos, short type, double damage, DamageDealerType damageType);

}
