package org.schema.game.common.controller.elements.jumpdrive;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.RecharchableSingleModule;
import org.schema.game.common.controller.elements.ShipManagerContainer;
import org.schema.game.network.objects.valueUpdate.FloatModuleValueUpdate;

public class JumpAddOnChargeValueUpdate extends FloatModuleValueUpdate {
	protected boolean chargeActive;

	@Override
	public boolean applyClient(ManagerContainer<?> o) {
		if(o instanceof ShipManagerContainer){
			((ShipManagerContainer)o).getJumpAddOn().setCharge(RecharchableSingleModule.decodeCharge(this.val));
			((ShipManagerContainer)o).getJumpAddOn().setCharges(RecharchableSingleModule.decodeCharges(this.val));
			((ShipManagerContainer)o).getJumpAddOn().setAutoChargeOn(chargeActive);
			return true;
		}
		assert(false);
		return false;
	}

	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		this.val = RecharchableSingleModule.encodeCharge(
				((ShipManagerContainer)o).getJumpAddOn().getCharge(),
				((ShipManagerContainer)o).getJumpAddOn().getCharges());
		this.chargeActive = ((ShipManagerContainer)o).getJumpAddOn().isAutoChargeOn();
		this.parameter = parameter;
	}

	@Override
	public ValTypes getType() {
		return ValTypes.JUMP_CHARGE_REACTOR;
	}

	@Override
	public void serialize(DataOutput buffer, boolean onServer) throws IOException {
		super.serialize(buffer, onServer);
		buffer.writeBoolean(chargeActive);
	}

	@Override
	public void deserialize(DataInput stream, int updateSenderStateId, boolean onServer) throws IOException {
		super.deserialize(stream, updateSenderStateId, onServer);
		chargeActive = stream.readBoolean();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "JumpChargeReactorValueUpdate [parameter=" + parameter + ", val=" + val
				+ "]";
	}

}
