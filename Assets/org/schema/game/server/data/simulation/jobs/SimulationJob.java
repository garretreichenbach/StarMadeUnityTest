package org.schema.game.server.data.simulation.jobs;

import api.listener.events.world.SimulationJobExecuteEvent;
import api.mod.StarLoader;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.server.data.simulation.SimulationManager;

public abstract class SimulationJob {
	public final void executeJob(SimulationManager man){
		SimulationJobExecuteEvent ev = new SimulationJobExecuteEvent(this, _getFaction(),man);
		StarLoader.fireEvent(ev,true);
		if(!ev.isCanceled()) execute(man);
	}

	public abstract int _getFaction();

	protected abstract void execute(SimulationManager man);

	public abstract Vector3i getStartLocation();
}
