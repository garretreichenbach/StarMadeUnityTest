package org.schema.game.common.controller.elements;

import org.schema.game.common.controller.elements.activation.AbstractUnit;
import org.schema.game.common.controller.elements.activation.ActivationCollectionManager;
import org.schema.game.common.controller.elements.activation.ActivationElementManager;

public interface ActivationManagerInterface {
	public ManagerModuleCollection<AbstractUnit, ActivationCollectionManager, ActivationElementManager> getActivation();
}
