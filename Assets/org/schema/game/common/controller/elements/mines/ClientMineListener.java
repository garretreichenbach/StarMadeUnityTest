package org.schema.game.common.controller.elements.mines;

import org.schema.game.common.data.mines.Mine;

public interface ClientMineListener {

	public void onRemovedMine(Mine m);

	public void onAddMine(Mine m);

	public void onChangedMine(Mine m);

	public void onBecomingInactive(Mine m);

	public void onBecomingActive(Mine m);
}
