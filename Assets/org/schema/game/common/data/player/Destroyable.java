package org.schema.game.common.data.player;

import org.schema.game.common.controller.damage.Damager;
import org.schema.schine.network.objects.Sendable;

public interface Destroyable extends Sendable {

	void destroy(Damager from);

}
