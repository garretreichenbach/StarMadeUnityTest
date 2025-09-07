package org.schema.game.common.controller.elements;

import org.schema.common.util.linAlg.Vector3i;

public interface ManagerReloadInterface {
	public void drawReloads(Vector3i iconPos, Vector3i iconSize, long controllerPos);
	public int getCharges();
	public int getMaxCharges();
	public String getReloadStatus(long id);
}
