package org.schema.game.common.controller.elements;

import org.schema.game.common.controller.elements.trigger.TriggerCollectionManager;
import org.schema.game.common.controller.elements.trigger.TriggerElementManager;
import org.schema.game.common.controller.elements.trigger.TriggerUnit;

public interface TriggerManagerInterface {
	public ManagerModuleCollection<TriggerUnit, TriggerCollectionManager, TriggerElementManager> getTrigger();
}
