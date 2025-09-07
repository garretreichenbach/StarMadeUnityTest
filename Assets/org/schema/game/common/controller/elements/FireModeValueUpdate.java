package org.schema.game.common.controller.elements;

import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.elements.FocusableUsableModule.FireMode;
import org.schema.game.network.objects.valueUpdate.ByteModuleValueUpdate;

public class FireModeValueUpdate extends ByteModuleValueUpdate{

	@Override
	public boolean applyClient(ManagerContainer<?> o) {
		assert(o instanceof ShipManagerContainer);
		PlayerUsableInterface playerUsable = o.getPlayerUsable(parameter);
		if(playerUsable instanceof FocusableUsableModule ) {
			((FocusableUsableModule)playerUsable).setFireMode(FireMode.values()[val]);
			return true;
		}
		return false;
	}

	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		PlayerUsableInterface playerUsable = o.getPlayerUsable(parameter);
		if(playerUsable instanceof FocusableUsableModule ) {
			val = (byte)((FocusableUsableModule)playerUsable).getFireMode().ordinal();
		}else {
			assert(false):parameter+"; "+playerUsable;
		}
		this.parameter = parameter;
	}

	@Override
	public ValTypes getType() {
		return ValTypes.FIRE_MODE;
	}
}
