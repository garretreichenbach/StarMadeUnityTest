package org.schema.game.common.controller.ai;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.schine.ai.stateMachines.AiInterface;

public interface AiInterfaceContainer {

	public String getUID();

	public String getRealName() throws UnloadedAiEntityException;

	public AiInterface getAi() throws UnloadedAiEntityException;

	public byte getType();

	public Vector3i getLastKnownSector() throws UnloadedAiEntityException;

}
