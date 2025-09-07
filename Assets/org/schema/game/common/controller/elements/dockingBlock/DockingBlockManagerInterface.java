package org.schema.game.common.controller.elements.dockingBlock;

import java.util.Collection;

import org.schema.game.common.controller.elements.ManagerModuleCollection;

public interface DockingBlockManagerInterface<E extends DockingBlockUnit<E, CM, EM>, CM extends DockingBlockCollectionManager<E, CM, EM>, EM extends DockingBlockElementManager<E, CM, EM>> {
	public Collection<ManagerModuleCollection<E, CM, EM>> getDockingBlock();
}
