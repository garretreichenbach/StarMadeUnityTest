package org.schema.game.client.controller.tutorial.states.conditions;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.InventoryControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;

public class TutorialConditionProductionSet extends TutorialCondition {

	private Vector3i pos;
	private short production;

	public TutorialConditionProductionSet(State toObserve, State establishing, Vector3i pos, short production) {
		super(toObserve, establishing);
		this.pos = pos;
		this.production = production;
	}

	@Override
	public boolean isSatisfied(GameClientState state) {

		InventoryControllerManager iv = state.getGlobalGameControlManager().getIngameControlManager()
				.getPlayerGameControlManager().getInventoryControlManager();
		return iv.getSecondInventory() != null && iv.getSecondInventory().getParameter() == ElementCollection.getIndex(pos)
				&& iv.getSecondInventory().getProduction() == production;

	}

	@Override
	public String getNotSactifiedText() {
		return "CRITICAL: production set wrong";
	}
	@Override
	protected Transition getTransition() {
		return Transition.TUTORIAL_CONDITION_PRODUCTION_SET;
	}
}
