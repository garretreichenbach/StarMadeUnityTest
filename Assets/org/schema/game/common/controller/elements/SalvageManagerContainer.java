package org.schema.game.common.controller.elements;

import org.schema.game.common.controller.elements.beam.harvest.SalvageBeamCollectionManager;
import org.schema.game.common.controller.elements.beam.harvest.SalvageElementManager;
import org.schema.game.common.controller.elements.beam.harvest.SalvageUnit;

public interface SalvageManagerContainer {
	public ManagerModuleCollection<SalvageUnit, SalvageBeamCollectionManager, SalvageElementManager> getSalvage();
}
