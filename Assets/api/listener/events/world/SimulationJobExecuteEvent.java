package api.listener.events.world;

import api.listener.events.Event;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.server.data.simulation.SimulationManager;
import org.schema.game.server.data.simulation.jobs.SimulationJob;

public class SimulationJobExecuteEvent extends Event {
    private final SimulationJob simulationJob;
    private final int faction;
    private final SimulationManager simulationManager;

    public SimulationJobExecuteEvent(SimulationJob simulationJob, int faction, SimulationManager man) {
        this.simulationJob = simulationJob;
        this.faction = faction;
        this.simulationManager = man;
    }

    public SimulationJob getSimulationJob() {
        return simulationJob;
    }

    public SimulationManager getSimulationManager() {
        return simulationManager;
    }

    public int getFaction() {
        return faction;
    }

    public Vector3i getStartLocation(){
        return simulationJob.getStartLocation();
    }
}
