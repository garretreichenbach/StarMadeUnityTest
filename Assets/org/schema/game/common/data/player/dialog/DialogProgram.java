package org.schema.game.common.data.player.dialog;

import java.util.HashMap;

import org.schema.schine.ai.MachineProgram;
import org.schema.schine.ai.stateMachines.AIConfiguationElementsInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;

public class DialogProgram extends MachineProgram<PlayerConversationManager> {

	public DialogProgram(PlayerConversationManager entityState,
	                     boolean startSuspended, HashMap<String, FiniteStateMachine<?>> machines) {
		super(entityState, startSuspended, machines);
	}

	@Override
	public void onAISettingChanged(AIConfiguationElementsInterface setting)
			throws FSMException {
	}

	@Override
	protected String getStartMachine() {
		return "default";
	}

	@Override
	protected void initializeMachines(
			HashMap<String, FiniteStateMachine<?>> machines) {

	}

}
