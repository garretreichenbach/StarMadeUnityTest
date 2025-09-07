package org.schema.game.common.data.fleet.missions;

import java.util.HashMap;

import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.missions.machines.FleetFiniteStateMachine;
import org.schema.game.common.data.fleet.missions.machines.FleetFiniteStateMachineFactory;
import org.schema.schine.ai.MachineProgram;
import org.schema.schine.ai.stateMachines.AIConfiguationElementsInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;

public class MissionProgram extends MachineProgram<Fleet>{

	public MissionProgram(Fleet entityState, boolean startSuspended) {
		super(entityState, startSuspended);
	}

	@Override
	public void onAISettingChanged(AIConfiguationElementsInterface setting)
			throws FSMException {
		
	}

	@Override
	protected String getStartMachine() {
		return "IDLE";
	}

	@Override
	protected void initializeMachines(
			HashMap<String, FiniteStateMachine<?>> machines) {
		machines.put("IDLE", new FleetFiniteStateMachine(getEntityState(), this, new FleetFiniteStateMachineFactory()));
	}

	@Override
	public boolean isAlwaysOn() {
		return true;
	}
	
	
}
