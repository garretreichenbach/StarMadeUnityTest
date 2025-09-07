package org.schema.game.server.ai.program.fleetcontrollable;

import java.util.HashMap;

import org.schema.game.server.ai.ShipAIEntity;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.schine.ai.stateMachines.AIConfiguationElementsInterface;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;

public class FleetControllableProgram extends TargetProgram<ShipAIEntity> {
	private String SIMPLE_SND = "SIMPLE_SND";

	public FleetControllableProgram(ShipAIEntity entityState, boolean startSuspended) {
		super(entityState, startSuspended);
	}

	@Override
	public void onAISettingChanged(AIConfiguationElementsInterface setting) {

	}

	@Override
	protected String getStartMachine() {
		return SIMPLE_SND;
	}

	@Override
	protected void initializeMachines(
			HashMap<String, FiniteStateMachine<?>> machines) {
		machines.put(SIMPLE_SND, new FleetControllableMachine(getEntityState(), this));
	}
	@Override
	public boolean isAlwaysOn() {
		return true;
	}
}
