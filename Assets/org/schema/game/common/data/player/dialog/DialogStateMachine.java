package org.schema.game.common.data.player.dialog;

import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.MachineProgram;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;
import org.schema.schine.ai.stateMachines.Message;
import org.schema.schine.ai.stateMachines.State;

public class DialogStateMachine extends FiniteStateMachine<String> {


	public DialogStateMachine(AiEntityStateInterface obj,
	                          MachineProgram<?> program, State initializedState) {
		super(obj, program, "", initializedState);

	}

	@Override
	public void createFSM(String parameter) {
		
	}

	@Override
	public void onMsg(Message message) {
		
	}

}
