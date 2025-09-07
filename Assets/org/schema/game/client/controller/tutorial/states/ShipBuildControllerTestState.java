package org.schema.game.client.controller.tutorial.states;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.ai.AiEntityStateInterface;

public class ShipBuildControllerTestState extends SatisfyingCondition {

	/**
	 *
	 */
	

	public ShipBuildControllerTestState(AiEntityStateInterface gObj, String message, GameClientState state) {
		super(gObj, message, state);
		skipIfSatisfiedAtEnter = true;
	}

	@Override
	protected boolean checkSatisfyingCondition() {
		return getGameState().getGlobalGameControlManager().getIngameControlManager()
				.getPlayerGameControlManager().getPlayerIntercationManager().
						getInShipControlManager().getShipControlManager().getSegmentBuildController()
				.isActive();
	}

}
