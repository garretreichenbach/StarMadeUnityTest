package org.schema.game.common.controller.elements.factory.input;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.factory.FactoryElementManager;
import org.schema.game.common.data.element.ElementKeyMap;

@Deprecated
public class FactoryInputElementManager extends FactoryElementManager {

	public FactoryInputElementManager(SegmentController segController) {
		super(segController, ElementKeyMap.FACTORY_COMPONENT_FAB_ID, ElementKeyMap.FACTORY_INPUT_ENH_ID);
	}

}
