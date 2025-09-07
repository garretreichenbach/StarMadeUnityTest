package org.schema.game.common.controller.elements;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.network.objects.valueUpdate.FloatModuleValueUpdate;

public abstract class ActiveChargeValueUpdate extends FloatModuleValueUpdate{
	protected long activeStarted;
	protected boolean chargeActive;

	@Override
	public void serialize(DataOutput buffer, boolean onServer) throws IOException {
		super.serialize(buffer, onServer);
		buffer.writeLong(activeStarted);
		buffer.writeBoolean(chargeActive);
	}
	@Override
	public boolean applyClient(ManagerContainer<?> o) {
//		System.err.println("[VALUEUPDATE] "+o.getState()+" Received value update: "+this.getClass().getSimpleName()+" "+this.val+" -> "+RecharchableSingleModule.decodeCharge(this.val)+", "+RecharchableSingleModule.decodeCharges(this.val));
		getModule(o).setCharge(RecharchableSingleModule.decodeCharge(this.val));
		getModule(o).setCharges(RecharchableSingleModule.decodeCharges(this.val));
		getModule(o).receivedActive(activeStarted);
		getModule(o).setAutoChargeOn(chargeActive);
		return true;
	}

	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		this.parameter = 		parameter;
		this.val = 				RecharchableSingleModule.encodeCharge(getModule(o).getCharge(), getModule(o).getCharges());
		this.activeStarted = 	getModule(o).getStarted();
		this.chargeActive = 	getModule(o).isAutoChargeOn();
		
//		System.err.println("[VALUEUPDATE] "+o.getState()+" Sending value update: "+this.getClass().getSimpleName()+" "+this.val);
	}
	@Override
	public void deserialize(DataInput stream, int updateSenderStateId, boolean onServer) throws IOException {
		super.deserialize(stream, updateSenderStateId, onServer);
		activeStarted = stream.readLong();
		chargeActive = stream.readBoolean();
	}
	public abstract RecharchableActivatableDurationSingleModule getModule(ManagerContainer<?> o);
}
