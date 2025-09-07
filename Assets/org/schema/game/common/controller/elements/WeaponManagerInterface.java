package org.schema.game.common.controller.elements;

import org.schema.game.common.controller.elements.beam.damageBeam.DamageBeamCollectionManager;
import org.schema.game.common.controller.elements.beam.damageBeam.DamageBeamElementManager;
import org.schema.game.common.controller.elements.beam.damageBeam.DamageBeamUnit;
import org.schema.game.common.controller.elements.missile.dumb.DumbMissileCollectionManager;
import org.schema.game.common.controller.elements.missile.dumb.DumbMissileElementManager;
import org.schema.game.common.controller.elements.missile.dumb.DumbMissileUnit;
import org.schema.game.common.controller.elements.cannon.CannonCollectionManager;
import org.schema.game.common.controller.elements.cannon.CannonElementManager;
import org.schema.game.common.controller.elements.cannon.CannonUnit;

public interface WeaponManagerInterface {
	public ManagerModuleCollection<CannonUnit, CannonCollectionManager, CannonElementManager> getWeapon();
	public ManagerModuleCollection<DumbMissileUnit, DumbMissileCollectionManager, DumbMissileElementManager> getMissile();
	public ManagerModuleCollection<DamageBeamUnit, DamageBeamCollectionManager, DamageBeamElementManager> getBeam();

}
