package org.schema.game.network.objects.valueUpdate;

import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ShieldAddOn;
import org.schema.game.common.controller.elements.ShieldContainerInterface;

public class ShieldValueUpdate extends DoubleValueUpdate {
	
	
	@Override
	public boolean applyClient(ManagerContainer<?> o) {
		boolean useRecover = this.val >= 0;
		this.val = Math.abs(this.val);
		
		double shieldsbef = ((ShieldContainerInterface) o).getShieldAddOn().getShields();
		((ShieldContainerInterface) o).getShieldAddOn().setShields(this.val);

		if (shieldsbef > this.val) {
			((ShieldContainerInterface) o).getShieldAddOn().useNormalRecovery();
		}
		((ShieldContainerInterface) o).getShieldAddOn().onShieldsZero(shieldsbef);
		return true;
	}

	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		ShieldAddOn s = ((ShieldContainerInterface) o).getShieldAddOn();
		this.val = s.getRecovery() > 0 ? s.getShields() : -s.getShields();
	}

	@Override
	public ValTypes getType() {
		return ValTypes.SHIELD;
	}

}
