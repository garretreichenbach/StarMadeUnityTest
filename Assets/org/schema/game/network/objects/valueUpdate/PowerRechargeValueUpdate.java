package org.schema.game.network.objects.valueUpdate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;

public class PowerRechargeValueUpdate extends ValueUpdate {

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
		((PowerManagerInterface) o).getPowerAddOn().setRechargeEnabled(this.val);
		return true;
	}

	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		this.val = ((PowerManagerInterface) o).getPowerAddOn().isRechargeEnabled();
	}

	@Override
	public ValTypes getType() {
		return ValTypes.POWER_REGEN_ENABLED;
	}
}
