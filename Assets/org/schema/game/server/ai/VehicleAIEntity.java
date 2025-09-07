package org.schema.game.server.ai;

import org.schema.game.common.controller.Vehicle;
import org.schema.schine.ai.MachineProgram;
import org.schema.schine.graphicsengine.core.Timer;

public class VehicleAIEntity extends SegmentControllerAIEntity<Vehicle> {

	/**
	 *
	 */
	

	public VehicleAIEntity(String name, MachineProgram<?> program, Vehicle s) {
		super(name, s);
	}

	@Override
	public void updateAIClient(Timer timer) {
	}

	@Override
	public void updateAIServer(Timer timer) {
	}

}
