package org.schema.game.common.controller.elements.power.reactor;

import org.schema.game.common.controller.elements.ActiveChargeValueUpdate;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.RecharchableActivatableDurationSingleModule;

public class ReactorBoostAddOnChargeValueUpdate extends ActiveChargeValueUpdate {

	@Override
	public RecharchableActivatableDurationSingleModule getModule(ManagerContainer<?> o) {
		return o.getReactorBoostAddOn();
	}

	@Override
	public ValTypes getType() {
		return ValTypes.REACTOR_BOOST_CHARGE;
	}

	
}
