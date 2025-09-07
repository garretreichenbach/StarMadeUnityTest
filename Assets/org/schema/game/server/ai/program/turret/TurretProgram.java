package org.schema.game.server.ai.program.turret;

import java.util.HashMap;

import org.schema.game.server.ai.ShipAIEntity;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.schine.ai.stateMachines.AIConfiguationElementsInterface;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;

public class TurretProgram extends TargetProgram<ShipAIEntity> {

	public static final String STD = "STD";

	public TurretProgram(ShipAIEntity entityState, boolean startSuspended) {
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
		machines.put(STD, new TurretMachine(getEntityState(), this));
	}

}
