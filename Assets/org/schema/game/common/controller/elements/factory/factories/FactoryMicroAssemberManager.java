package org.schema.game.common.controller.elements.factory.factories;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.factory.FactoryCollectionManager;
import org.schema.game.common.controller.elements.factory.FactoryElementManager;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.FixedRecipe;
import org.schema.game.common.data.player.inventory.Inventory;

@Deprecated
public class FactoryMicroAssemberManager extends FactoryElementManager {

	public FactoryMicroAssemberManager(SegmentController segController) {
		super(segController, ElementKeyMap.FACTORY_MICRO_ASSEMBLER_ID, ElementKeyMap.FACTORY_INPUT_ENH_ID);
	}

	@Override
	public FixedRecipe getCurrentRecipe(Inventory ownInventory,
	                                    FactoryCollectionManager factoryCollectionManager) {
		return ElementKeyMap.microAssemblerRecipe;
	}
}
