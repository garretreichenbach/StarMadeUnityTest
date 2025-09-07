package org.schema.game.client.controller.tutorial.states.conditions;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.creature.AIPlayer;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.network.objects.Sendable;

public class TutorialConditionNPCExists extends TutorialCondition {

	private String npcName;

	public TutorialConditionNPCExists(State toObserve, State establishing, String npcName) {
		super(toObserve, establishing);
		this.npcName = npcName;
	}

	@Override
	public boolean isSatisfied(GameClientState state) {

		synchronized (state.getLocalAndRemoteObjectContainer().getLocalObjects()) {
			for (Sendable s : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
				if (s instanceof AIPlayer) {
					AIPlayer p = (AIPlayer) s;
					if (p.getName().equals(npcName)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	@Override
	public String getNotSactifiedText() {
		return "CRITICAL: The npc does not exist";
	}
	@Override
	protected Transition getTransition() {
		return Transition.TUTORIAL_CONDITION_NPC_EXISTS;
	}
}
