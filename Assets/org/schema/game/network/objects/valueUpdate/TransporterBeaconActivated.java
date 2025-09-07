package org.schema.game.network.objects.valueUpdate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.TransporterModuleInterface;
import org.schema.game.common.controller.elements.transporter.TransporterCollectionManager;

public class TransporterBeaconActivated extends ParameterValueUpdate{

	public int entityId = -1;

	@Override
	public boolean applyClient(ManagerContainer<?> o) {
		TransporterCollectionManager v = ((TransporterModuleInterface)o).getTransporter().getCollectionManagersMap().get(parameter);
		if (v != null) {
			assert(entityId != -1);
			v.transporterBeaconActivatedReceived(entityId);
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
		assert(entityId != -1);
		b.writeInt(entityId);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean onServer) throws IOException {
		super.deserialize(b, updateSenderStateId, onServer);
		entityId = b.readInt();
	}
	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		this.parameter = parameter;
	}

	@Override
	public ValTypes getType() {
		return ValTypes.TRANSPORTER_BEACON_ACTIVATED;
	}

}
