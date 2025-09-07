package org.schema.game.common.controller.elements;

import org.schema.game.common.controller.elements.thrust.ThrusterCollectionManager;
import org.schema.game.common.controller.elements.thrust.ThrusterElementManager;
import org.schema.game.common.controller.elements.thrust.ThrusterUnit;

public interface ManagerThrustInterface {
	public ManagerModuleSingle<ThrusterUnit, ThrusterCollectionManager, ThrusterElementManager> getThrust();

	public ThrusterElementManager getThrusterElementManager();

	
}
