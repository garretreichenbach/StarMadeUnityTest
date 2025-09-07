package org.schema.game.common.data.player.inventory;

import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.graphicsengine.core.Timer;

public class ItemsToCreditsInventory extends ActiveInventory {

	public ItemsToCreditsInventory(InventoryHolder state, long parameter) {
		super(state, parameter);
	}

	private boolean convert(short type) {
		

		return false;
	}

	@Override
	public int getActiveSlotsMax() {
		return 0;
	}


	@Override
	public int getLocalInventoryType() {
		return INVENTORY_TO_CREDITS;
	}

	@Override
	public String getCustomName() {
		return "";
	}

	@Override
	public void updateLocal(Timer timer) {
		for (short type : ElementKeyMap.getLeveldkeyset()) {
			int overallQuantity = getOverallQuantity(type);
			if (overallQuantity >= 10) {
				if (convert(type)) {
					break;
				}
			}
		}
		deactivate();
	}

}
