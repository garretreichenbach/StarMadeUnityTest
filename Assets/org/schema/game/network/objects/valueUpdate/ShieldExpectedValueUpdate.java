package org.schema.game.network.objects.valueUpdate;

import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ShieldContainerInterface;

public class ShieldExpectedValueUpdate extends IntValueUpdate {

	@Override
	public boolean applyClient(ManagerContainer<?> o) {
		((ShieldContainerInterface) o).getShieldAddOn().setExpectedShieldClient(this.val);
		return true;
	}

	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		this.val = ((ShieldContainerInterface) o).getShieldAddOn().getExpectedShieldSize();
	}

	@Override
	public ValTypes getType() {
		return ValTypes.SHIELD_EXPECTED;
	}

}
