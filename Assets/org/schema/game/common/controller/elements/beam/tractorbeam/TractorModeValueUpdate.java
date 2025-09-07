package org.schema.game.common.controller.elements.beam.tractorbeam;

import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ShipManagerContainer;
import org.schema.game.common.controller.elements.beam.tractorbeam.TractorBeamHandler.TractorMode;
import org.schema.game.network.objects.valueUpdate.ByteModuleValueUpdate;

public class TractorModeValueUpdate  extends ByteModuleValueUpdate{

	@Override
	public boolean applyClient(ManagerContainer<?> o) {
		assert(o instanceof ShipManagerContainer);
		TractorBeamCollectionManager tractorBeamCollectionManager = ((ShipManagerContainer)o).getTractorBeam().getElementManager().getCollectionManagersMap().get(parameter);
		if(tractorBeamCollectionManager != null) {
			tractorBeamCollectionManager.setTractorModeFromReceived(TractorMode.values()[val]);
			return true;
		}
		return false;
	}

	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		this.parameter = parameter;
	}

	@Override
	public ValTypes getType() {
		return ValTypes.TRACTOR_MODE;
	}
}
