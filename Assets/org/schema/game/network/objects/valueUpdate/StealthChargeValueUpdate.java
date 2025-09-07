package org.schema.game.network.objects.valueUpdate;

import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.behavior.managers.charging.ChargeManager;
import org.schema.game.common.controller.elements.stealth.StealthElementManager;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Synchronizes the current recharge-time state of the stealth system.
 */
public class StealthChargeValueUpdate extends IntValueUpdate {
	float chargeProgress = 0;
	int chargeCount = 0;

	@Override
	public boolean applyClient(ManagerContainer<?> o) {
		StealthElementManager st = o.getStealth().getElementManager();
		st.setActivationCooldown(this.val);
		if(st.hasCollection()){
			st.getCollection().getChargeManager().setCharge(chargeProgress);
			st.getCollection().getChargeManager().setChargesCount(chargeCount);
		} else return false;

		return true;
	}

	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		StealthElementManager st = o.getStealth().getElementManager();
		this.val = st.getActivationCooldownMs();
		if(st.hasCollection()){
			ChargeManager cm = st.getCollection().getChargeManager();
			chargeProgress = cm.getCharge();
			chargeCount = cm.getChargesCount();
		}
	}

	@Override
	public void serialize(DataOutput buffer, boolean onServer) throws IOException {
		super.serialize(buffer, onServer);
		buffer.writeFloat(chargeProgress);
		buffer.writeInt(chargeCount);
	}

	@Override
	public void deserialize(DataInput stream, int updateSenderStateId, boolean onServer) throws IOException {
		super.deserialize(stream, updateSenderStateId, onServer);
		chargeProgress = stream.readFloat();
		chargeCount = stream.readInt();
	}

	@Override
	public ValTypes getType() {
		return ValTypes.STEALTH_RECHARGE;
	}
}
