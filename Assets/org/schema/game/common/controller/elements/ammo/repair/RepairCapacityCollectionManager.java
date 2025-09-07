package org.schema.game.common.controller.elements.ammo.repair;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ammo.AmmoCapacityCollectionManager;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class RepairCapacityCollectionManager extends AmmoCapacityCollectionManager<RepairCapacityUnit, RepairCapacityCollectionManager, RepairCapacityElementManager> {

	public RepairCapacityCollectionManager(SegmentController segController, RepairCapacityElementManager em) {
		super(ElementKeyMap.REPAIR_PASTE_MODULE, segController, em);
	}

	@Override
	protected Class<RepairCapacityUnit> getType() {
		return RepairCapacityUnit.class;
	}

	@Override
	public RepairCapacityUnit getInstance() {
		return new RepairCapacityUnit();
	}

	@Override
	public String getModuleName() {
		return Lng.str("Repair Paste System");
	}
}
