package org.schema.game.common.controller.elements.combination;

import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.UsableControllableElementManager;
import org.schema.game.common.data.element.ElementCollection;

public interface Combinable<E extends ElementCollection<E, CM, EM>, CM extends ControlBlockElementCollectionManager<E, CM, EM>, EM extends UsableControllableElementManager<E, CM, EM>, S extends CombinationSettings> {

	public abstract CombinationAddOn<E, CM, ? extends EM, S> getAddOn();

}
