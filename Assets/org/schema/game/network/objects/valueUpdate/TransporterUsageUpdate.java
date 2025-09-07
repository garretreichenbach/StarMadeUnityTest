package org.schema.game.network.objects.valueUpdate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.TransporterModuleInterface;
import org.schema.game.common.controller.elements.transporter.TransporterCollectionManager;

public class TransporterUsageUpdate extends ParameterValueUpdate{

	public long usageTime = Long.MIN_VALUE;

	@Override
	public boolean applyClient(ManagerContainer<?> o) {
		TransporterCollectionManager v = ((TransporterModuleInterface)o).getTransporter().getCollectionManagersMap().get(parameter);
		if (v != null) {
			assert(usageTime != Long.MIN_VALUE);
			v.transporterUsageReceived(usageTime);
			if(v.getSegmentController().isOnServer()){
				v.sendTransporterUsage();
			}
			return true;
		} else {
			return false;
		}
	}
	@Override
	public void serialize(DataOutput b, boolean onServer) throws IOException {
		super.serialize(b, onServer);
		assert(usageTime != Long.MIN_VALUE);
		b.writeLong(usageTime);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean onServer) throws IOException {
		super.deserialize(b, updateSenderStateId, onServer);
		usageTime = b.readLong();
	}
	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		this.parameter = parameter;
	}

	@Override
	public ValTypes getType() {
		return ValTypes.TRANSPORTER_USAGE_UPDATE;
	}

}
