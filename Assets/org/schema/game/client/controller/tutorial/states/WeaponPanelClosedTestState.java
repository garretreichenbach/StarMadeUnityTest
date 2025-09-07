package org.schema.game.client.controller.tutorial.states;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.ai.AiEntityStateInterface;

public class WeaponPanelClosedTestState extends SatisfyingCondition {

	/**
	 *
	 */
	

	public WeaponPanelClosedTestState(AiEntityStateInterface gObj, String message, GameClientState state) {
		super(gObj, message, state);
		skipIfSatisfiedAtEnter = true;
	}

	@Override
	protected boolean checkSatisfyingCondition() {
		return !getGameState().getGlobalGameControlManager().getIngameControlManager()
				.getPlayerGameControlManager().getWeaponControlManager().isActive();
	}

}
