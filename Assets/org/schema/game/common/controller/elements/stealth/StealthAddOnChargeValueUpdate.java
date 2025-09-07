package org.schema.game.common.controller.elements.stealth;

import org.schema.game.common.controller.elements.ActiveChargeValueUpdate;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.RecharchableActivatableDurationSingleModule;

public class StealthAddOnChargeValueUpdate extends ActiveChargeValueUpdate {

	@Override
	public RecharchableActivatableDurationSingleModule getModule(ManagerContainer<?> o) {
		throw new UnsupportedOperationException("Deprecated Power 2 stealth addon is attempting to update!");
	}

	@Override
	public ValTypes getType() {
		return ValTypes.STEALTH_CHARGE_REACTOR;
	}

}
