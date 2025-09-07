package org.schema.game.common.controller.elements;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.elements.door.DoorCollectionManager;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;

public interface DoorContainerInterface extends PowerManagerInterface {
	public DoorCollectionManager getDoorManager();

	public void handleClientRemoteDoor(Vector3i pos);
}
