package org.schema.game.network.objects.valueUpdate;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ShipManagerContainer;
import org.schema.game.common.controller.elements.behavior.managers.charging.ChargeManager;
import org.schema.game.common.controller.elements.jumpdrive.JumpDriveCollectionManager;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class JumpChargeValueUpdate extends FloatModuleValueUpdate {
	boolean autoCharging = false;
	boolean manualCharging = false;
	int charges = 0;

	@Override
	public boolean applyClient(ManagerContainer<?> o) {
		JumpDriveCollectionManager v = ((ShipManagerContainer) o).getJumpDrive().getCollectionManagersMap().get(parameter);

		if (v != null) {
			ChargeManager cm = v.getChargeManager();

			if(o.isOnServer() && this.val > cm.getCharge()){
				float oldVal = cm.getCharge() / v.getChargeNeededForJump();
				float newVal = this.val / v.getChargeNeededForJump();
				
				if(newVal - oldVal > 0.5f){
					((GameServerState)o.getState()).getController().broadcastMessageAdmin(
							Lng.astr("Possible hack by player charging jumpdrive too fast!\nEntity: %s in %s.\nfalse positive is possible through lag", o.getSegmentController(), o.getSegmentController().getSector(new Vector3i())), ServerMessage.MESSAGE_TYPE_ERROR);
					return true;
				}
			}

			if(charges < cm.getChargesCount()) v.getActivationManager().setActive(false); //lost a charge. Means jump finished or something's wrong

			cm.setCharge(this.val);
			cm.setChargesCount(this.charges);
			v.setAutoCharging(this.autoCharging);
			v.setManualCharging(this.manualCharging);
			return true;
		} else {
			return false;
//			assert(false);
		}
	}

	@Override
	public void serialize(DataOutput buffer, boolean onServer) throws IOException {
		super.serialize(buffer, onServer);
		buffer.writeBoolean(autoCharging);
		buffer.writeBoolean(manualCharging);
		buffer.writeInt(charges);
	}

	@Override
	public void deserialize(DataInput stream, int updateSenderStateId, boolean onServer) throws IOException {
		super.deserialize(stream, updateSenderStateId, onServer);
		autoCharging = stream.readBoolean();
		manualCharging = stream.readBoolean();
		charges = stream.readInt();
	}

	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		JumpDriveCollectionManager v = ((ShipManagerContainer) o).getJumpDrive().getCollectionManagersMap().get(parameter);
		if (v != null) {
			ChargeManager cm = v.getChargeManager();

			this.val = cm.getCharge();
			this.autoCharging = v.isAutoCharging();
			this.manualCharging = v.isManualCharging();
			this.charges = cm.getChargesCount();
		}
		this.parameter = parameter;
	}

	@Override
	public ValTypes getType() {
		return ValTypes.JUMP_CHARGE;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "JumpChargeValueUpdate [parameter=" + parameter + ", charge=" + val
				+ ", autocharge=" + autoCharging + "]";
	}

}
