package org.schema.game.client.controller.tutorial.states;

import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.ai.AiEntityStateInterface;

public class InventoryClosedTestState extends SatisfyingCondition {

	/**
	 *
	 */
	

	public InventoryClosedTestState(AiEntityStateInterface gObj, String message, GameClientState state) {
		super(gObj, message, state);
		skipIfSatisfiedAtEnter = true;
	}

	@Override
	protected boolean checkSatisfyingCondition() {
		return !getGameState().getGlobalGameControlManager().getIngameControlManager()
				.getPlayerGameControlManager().getInventoryControlManager().isActive();
	}

	@Override
	public boolean onEnter() {
		PlayerGameControlManager playerGameControlManager = getGameState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager();
		boolean inventoryActive = playerGameControlManager.getInventoryControlManager().isActive();
		if (!inventoryActive) {
			playerGameControlManager.inventoryAction(null);
		}
		return super.onEnter();
	}

}
