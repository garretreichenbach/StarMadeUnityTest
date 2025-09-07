package org.schema.game.common.controller.elements.ammo.cannon;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ammo.AmmoCapacityCollectionManager;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;

public class CannonCapacityCollectionManager extends AmmoCapacityCollectionManager<CannonCapacityUnit, CannonCapacityCollectionManager, CannonCapacityElementManager>{

	public CannonCapacityCollectionManager(SegmentController segController, CannonCapacityElementManager em) {
		super(ElementKeyMap.CANNON_CAPACITY_MODULE, segController, em);
	}

	@Override
	protected Class<CannonCapacityUnit> getType() {
		return CannonCapacityUnit.class;
	}

	@Override
	public CannonCapacityUnit getInstance() {
		return new CannonCapacityUnit();
	}

	@Override
	public String getModuleName() {
		return Lng.str("Cannon Capacity System");
	}
}
