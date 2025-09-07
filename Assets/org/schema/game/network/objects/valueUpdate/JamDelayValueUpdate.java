package org.schema.game.network.objects.valueUpdate;

import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ShipManagerContainer;

@Deprecated //stealth unified under former cloaking
public class JamDelayValueUpdate extends IntValueUpdate {

	@Override
	public boolean applyClient(ManagerContainer<?> o) {
		((ShipManagerContainer) o).getStealth().getElementManager().setActivationCooldown(this.val);

		return true;
	}

	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		this.val = ((ShipManagerContainer) o).getStealth().getElementManager().getActivationCooldownMs();
	}

	@Override
	public ValTypes getType() {
		return ValTypes.JAM_DELAY;
	}

}
