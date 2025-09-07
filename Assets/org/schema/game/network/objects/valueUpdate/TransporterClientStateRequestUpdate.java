package org.schema.game.network.objects.valueUpdate;

import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.TransporterModuleInterface;
import org.schema.game.common.controller.elements.transporter.TransporterCollectionManager;

public class TransporterClientStateRequestUpdate extends ParameterValueUpdate{

	@Override
	public boolean applyClient(ManagerContainer<?> o) {
		TransporterCollectionManager v = ((TransporterModuleInterface)o).getTransporter().getCollectionManagersMap().get(parameter);
		if (v != null) {
			v.sendStateUpdateToClients();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		this.parameter = parameter;
	}

	@Override
	public ValTypes getType() {
		return ValTypes.TRANSPORTER_CLIENT_STATE_REQUEST;
	}

}
