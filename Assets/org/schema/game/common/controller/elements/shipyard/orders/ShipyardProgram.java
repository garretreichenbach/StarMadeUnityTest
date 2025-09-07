package org.schema.game.common.controller.elements.shipyard.orders;

import java.util.HashMap;

import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.schine.ai.stateMachines.AIConfiguationElementsInterface;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;

public class ShipyardProgram extends TargetProgram<ShipyardEntityState> {

	public static final String STD = "STD";

	public ShipyardProgram(ShipyardEntityState entityState, boolean startSuspended) {
		super(entityState, startSuspended);
	}

	@Override
	public void onAISettingChanged(AIConfiguationElementsInterface setting) {
	}

	@Override
	protected String getStartMachine() {
		return STD;
	}

	@Override
	protected void initializeMachines(
			HashMap<String, FiniteStateMachine<?>> machines) {
		machines.put(STD, new ShipyardMachine(getEntityState(), this));
	}

}
