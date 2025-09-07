package org.schema.game.client.view.gui.inventory;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.elements.shipyard.ShipyardCollectionManager;
import org.schema.game.common.data.element.ElementInformation;

public class ShipyardTypeRowItem {
	public final ElementInformation info;
	public final ShipyardCollectionManager yard;
	public ShipyardTypeRowItem(ElementInformation info, ShipyardCollectionManager itemId, GameClientState state) {
		super();
		this.info = info;
		this.yard = itemId;
	}


	public int getProgress() {
		return yard.clientGoalTo.get(info.getId());
	}

	public int getGoal() {
		return yard.clientGoalFrom.get(info.getId());
	}

	public float getPercent() {
		return (float) getProgress() / (float) getGoal();
	}

	@Override
	public int hashCode() {
		return info.hashCode();
	}

	// #RM1863 added .equals() and .hashCode()
	@Override
	public boolean equals(Object o) {
		return o instanceof ShipyardTypeRowItem && info.equals(((ShipyardTypeRowItem) o).info);
	}

}
