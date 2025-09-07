package org.schema.game.client.controller.manager.ingame;

import org.schema.common.util.linAlg.Vector3i;

public interface BuildCallback extends BuildSelectionCallback {

	public abstract void onBuild(Vector3i posBuilt, Vector3i posNextToBuild, short type);
}
