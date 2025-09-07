package org.schema.game.common.controller.elements.jumpprohibiter;

import org.schema.game.common.controller.elements.ActiveChargeValueUpdate;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.RecharchableActivatableDurationSingleModule;

public class InterdictionAddOnChargeValueUpdate extends ActiveChargeValueUpdate {

	@Override
	public RecharchableActivatableDurationSingleModule getModule(ManagerContainer<?> o) {
		return o.getInterdictionAddOn();
	}

	@Override
	public ValTypes getType() {
		return ValTypes.INTERDICTION_CHARGE;
	}

	
}
