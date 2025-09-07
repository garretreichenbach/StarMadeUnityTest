package org.schema.game.client.controller.tutorial.states.conditions;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;

public class TutorialConditionWeaponPanelOpen extends TutorialCondition {

	public TutorialConditionWeaponPanelOpen(State toObserve, State establishing) {
		super(toObserve, establishing);
	}

	@Override
	public boolean isSatisfied(GameClientState state) {

		return state.getGlobalGameControlManager().getIngameControlManager()
				.getPlayerGameControlManager().getWeaponControlManager().isActive();

	}

	@Override
	public String getNotSactifiedText() {
		return "CRITICAL: weapon panel not open";
	}

	@Override
	protected Transition getTransition() {
		return Transition.TUTORIAL_CONDITION_WEAPON_PANEL_OPEN;
	}

}
