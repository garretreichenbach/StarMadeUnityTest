package org.schema.game.common.controller;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.BuildInstruction;
import org.schema.game.client.controller.manager.ingame.BuildSelectionCallback;
import org.schema.game.client.view.buildhelper.BuildHelper;

public interface BuilderInterface{
	public void build(final int x, final int y, final int z, short type, int elementOrientation, boolean activateBlock, BuildSelectionCallback callback, Vector3i absOnOut, int[] addedAndRest, BuildHelper posesFilter, BuildInstruction buildInstruction) throws PositionBlockedException; 
}