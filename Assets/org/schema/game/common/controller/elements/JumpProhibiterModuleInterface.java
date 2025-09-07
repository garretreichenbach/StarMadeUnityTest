package org.schema.game.common.controller.elements;

import org.schema.game.common.controller.elements.jumpprohibiter.JumpInhibitorCollectionManager;
import org.schema.game.common.controller.elements.jumpprohibiter.JumpInhibitorElementManager;
import org.schema.game.common.controller.elements.jumpprohibiter.JumpInhibitorUnit;

public interface JumpProhibiterModuleInterface {
	public ManagerModuleCollection<JumpInhibitorUnit, JumpInhibitorCollectionManager, JumpInhibitorElementManager> getJumpProhibiter();
}
