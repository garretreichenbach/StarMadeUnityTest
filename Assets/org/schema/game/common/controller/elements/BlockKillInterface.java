package org.schema.game.common.controller.elements;

import org.schema.game.common.controller.damage.Damager;

public interface BlockKillInterface {

	public void onKilledBlock(long pos, short type, Damager from);

}
