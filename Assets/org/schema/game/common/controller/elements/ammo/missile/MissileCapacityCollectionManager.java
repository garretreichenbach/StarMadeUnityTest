package org.schema.game.common.controller.elements.ammo.missile;

import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.elements.BlockKillInterface;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.elements.ammo.AmmoCapacityCollectionManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;

public class MissileCapacityCollectionManager extends AmmoCapacityCollectionManager<MissileCapacityUnit, MissileCapacityCollectionManager, MissileCapacityElementManager>{

	public MissileCapacityCollectionManager(SegmentController segController, MissileCapacityElementManager em) {
		super(ElementKeyMap.MISSILE_CAPACITY_MODULE, segController, em);
	}

	@Override
	protected Class<MissileCapacityUnit> getType() {
		return MissileCapacityUnit.class;
	}

	@Override
	public MissileCapacityUnit getInstance() {
		return new MissileCapacityUnit();
	}

	@Override
	public String getModuleName() {
		return Lng.str("Missile Capacity System");
	}
}
