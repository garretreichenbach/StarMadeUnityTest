package org.schema.game.server.controller;

import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;

public interface SectorListener {
	public void onSectorAdded(Sector sec);
	public void onSectorRemoved(Sector sec);
	public void onSectorEntityAdded(SimpleTransformableSendableObject<?> s, Sector sector);
	public void onSectorEntityRemoved(SimpleTransformableSendableObject<?> s, Sector sector);
	
}
