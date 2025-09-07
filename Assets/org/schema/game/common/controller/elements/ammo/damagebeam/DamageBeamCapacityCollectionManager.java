package org.schema.game.common.controller.elements.ammo.damagebeam;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ammo.AmmoCapacityCollectionManager;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;

public class DamageBeamCapacityCollectionManager extends AmmoCapacityCollectionManager<DamageBeamCapacityUnit, DamageBeamCapacityCollectionManager, DamageBeamCapacityElementManager>{

	public DamageBeamCapacityCollectionManager(SegmentController segController, DamageBeamCapacityElementManager em) {
		super(ElementKeyMap.BEAM_CAPACITY_MODULE, segController, em);
	}

	@Override
	protected Class<DamageBeamCapacityUnit> getType() {
		return DamageBeamCapacityUnit.class;
	}

	@Override
	public DamageBeamCapacityUnit getInstance() {
		return new DamageBeamCapacityUnit();
	}

	@Override
	public String getModuleName() {
		return Lng.str("Beam Capacity System");
	}
}
