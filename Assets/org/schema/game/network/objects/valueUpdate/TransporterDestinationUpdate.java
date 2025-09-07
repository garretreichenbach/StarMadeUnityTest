package org.schema.game.network.objects.valueUpdate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.TransporterModuleInterface;
import org.schema.game.common.controller.elements.transporter.TransporterCollectionManager;

public class TransporterDestinationUpdate extends ParameterValueUpdate{

	public String uid;
	public long pos = Long.MIN_VALUE;
	
	@Override
	public boolean applyClient(ManagerContainer<?> o) {
		TransporterCollectionManager v = ((TransporterModuleInterface)o).getTransporter().getCollectionManagersMap().get(parameter);
		if (v != null) {
			v.setDestination(uid, pos);
			if(v.getSegmentController().isOnServer()){
				v.sendDestinationUpdate();
			}
			return true;
		} else {
			return false;
		}
	}
	@Override
	public void serialize(DataOutput b, boolean onServer) throws IOException {
		super.serialize(b, onServer);
		b.writeUTF(uid);
		b.writeLong(pos);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean onServer) throws IOException {
		super.deserialize(b, updateSenderStateId, onServer);
		uid = b.readUTF();
		pos = b.readLong();
	}
	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		this.parameter = parameter;
		assert(uid != null);
		assert(pos != Long.MIN_VALUE);
	}

	@Override
	public ValTypes getType() {
		return ValTypes.TRANSPORTER_DESTINATION_UPDATE;
	}
	
	

}
