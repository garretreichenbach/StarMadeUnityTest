package org.schema.game.network.objects.valueUpdate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ShieldContainerInterface;

public class ShieldRechargeValueUpdate extends ValueUpdate {

	boolean val;

	@Override
	public void serialize(DataOutput buffer, boolean onServer) throws IOException {
		buffer.writeBoolean(val);
	}

	@Override
	public void deserialize(DataInput stream, int updateSenderStateId, boolean onServer) throws IOException {
		val = stream.readBoolean();
	}

	@Override
	public boolean applyClient(ManagerContainer<?> o) {
		((ShieldContainerInterface) o).getShieldAddOn().setRegenEnabled(this.val);
		return true;
	}

	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		this.val = ((ShieldContainerInterface) o).getShieldAddOn().isRegenEnabled();
	}

	@Override
	public ValTypes getType() {
		return ValTypes.SHIELD_REGEN_ENABLED;
	}
}
