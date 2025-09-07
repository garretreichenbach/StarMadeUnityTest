package org.schema.game.common.controller.elements.spacescanner;

import org.schema.game.common.controller.elements.ActiveChargeValueUpdate;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.RecharchableActivatableDurationSingleModule;

public class ScanAddOnChargeValueUpdate extends ActiveChargeValueUpdate {
	@Override
	public ValTypes getType() {
		return ValTypes.SCAN_CHARGE_REACTOR;
	}

	@Override
	public RecharchableActivatableDurationSingleModule getModule(ManagerContainer<?> o) {
		throw new UnsupportedOperationException("Deprecated Power 2 scanner addon is attempting to update!");
	}

}
