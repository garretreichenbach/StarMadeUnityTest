package org.schema.game.network.objects.valueUpdate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.TransporterModuleInterface;
import org.schema.game.common.controller.elements.transporter.TransporterCollectionManager;

public class TransporterSettingsUpdate extends ParameterValueUpdate{
	public String name;
	public byte publicAccess;
	
	@Override
	public boolean applyClient(ManagerContainer<?> o) {
		TransporterCollectionManager v = ((TransporterModuleInterface)o).getTransporter().getCollectionManagersMap().get(parameter);
		if (v != null) {
			System.err.println(v.getState()+" Received Transporter Settings: "+name+" pa "+publicAccess);
			v.setTransporterSettings(name, publicAccess);
			if(v.getSegmentController().isOnServer()){
				v.sendSettingsUpdate();
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void serialize(DataOutput b, boolean onServer) throws IOException {
		super.serialize(b, onServer);
		b.writeUTF(name);
		b.writeByte(publicAccess);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean onServer) throws IOException {
		super.deserialize(b, updateSenderStateId, onServer);
		name = b.readUTF();
		publicAccess = b.readByte();
	}

	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		this.parameter = parameter;
		assert(name != null);
	}

	@Override
	public ValTypes getType() {
		return ValTypes.TRANSPORTER_SETTINGS_UPDATE;
	}
	

}
