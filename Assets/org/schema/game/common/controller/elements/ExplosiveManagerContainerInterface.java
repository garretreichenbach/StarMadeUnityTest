package org.schema.game.common.controller.elements;

import org.schema.game.common.controller.elements.explosive.ExplosiveCollectionManager;
import org.schema.game.common.controller.elements.explosive.ExplosiveElementManager;
import org.schema.game.common.controller.elements.explosive.ExplosiveUnit;

public interface ExplosiveManagerContainerInterface {
	public ExplosiveElementManager getExplosiveElementManager();

	public ExplosiveCollectionManager getExplosiveCollectionManager();

	public ManagerModuleSingle<ExplosiveUnit, ExplosiveCollectionManager, ExplosiveElementManager> getExplosive();
}
