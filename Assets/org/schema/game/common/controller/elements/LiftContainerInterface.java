package org.schema.game.common.controller.elements;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.elements.lift.LiftCollectionManager;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;

public interface LiftContainerInterface extends PowerManagerInterface {
	public LiftCollectionManager getLiftManager();

	public void handleClientRemoteLift(Vector3i pos);
}
