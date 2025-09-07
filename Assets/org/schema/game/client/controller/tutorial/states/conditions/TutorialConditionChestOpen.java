package org.schema.game.client.controller.tutorial.states.conditions;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.InventoryControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;

public class TutorialConditionChestOpen extends TutorialCondition {

	private Vector3i pos;

	public TutorialConditionChestOpen(State toObserve, State establishing, Vector3i pos) {
		super(toObserve, establishing);
		this.pos = pos;
	}

	@Override
	public boolean isSatisfied(GameClientState state) {

		InventoryControllerManager iv = state.getGlobalGameControlManager().getIngameControlManager()
				.getPlayerGameControlManager().getInventoryControlManager();
		return iv.isTreeActive() && iv.getSecondInventory() != null && iv.getSecondInventory().getParameter() == ElementCollection.getIndex(pos);

	}

	@Override
	public String getNotSactifiedText() {
		return "CRITICAL: chest not open";
	}
	@Override
	protected Transition getTransition() {
		return Transition.TUTORIAL_CONDITION_CHEST_OPEN;
	}
}
