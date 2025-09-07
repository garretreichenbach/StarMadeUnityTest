package org.schema.game.common.controller.elements.effectblock;

import org.schema.game.common.controller.elements.ActiveChargeValueUpdate;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.RecharchableActivatableDurationSingleModule;

public class EffectAddOnChargeValueUpdate  extends ActiveChargeValueUpdate {

	@Override
	public RecharchableActivatableDurationSingleModule getModule(ManagerContainer<?> o) {
		return o.getEffectAddOnManager().get(parameter);
	}

	@Override
	public ValTypes getType() {
		return ValTypes.EFFECT_ADD_ON_CHARGE;
	}
}


