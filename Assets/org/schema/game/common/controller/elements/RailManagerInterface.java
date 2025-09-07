package org.schema.game.common.controller.elements;

import org.schema.game.common.controller.elements.rail.massenhancer.RailMassEnhancerCollectionManager;
import org.schema.game.common.controller.elements.rail.massenhancer.RailMassEnhancerUnit;
import org.schema.game.common.controller.elements.rail.speed.RailSpeedCollectionManager;
import org.schema.game.common.controller.elements.rail.speed.RailSpeedElementManager;
import org.schema.game.common.controller.elements.rail.speed.RailSpeedUnit;

public interface RailManagerInterface {
	public ManagerModuleCollection<RailSpeedUnit, RailSpeedCollectionManager, RailSpeedElementManager> getRailSpeed();

	public ManagerModuleSingle<RailMassEnhancerUnit, RailMassEnhancerCollectionManager, VoidElementManager<RailMassEnhancerUnit, RailMassEnhancerCollectionManager>> getRailMassEnhancer();
}
