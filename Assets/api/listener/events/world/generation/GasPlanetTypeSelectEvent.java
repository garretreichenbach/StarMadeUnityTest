package api.listener.events.world.generation;

import api.listener.events.Event;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.SectorInformation;
import org.schema.game.common.data.world.StellarSystem;

public class GasPlanetTypeSelectEvent extends Event {
    private final Vector3i position;
    private final StellarSystem system;
    private final int sectorIndex;
    private SectorInformation.GasPlanetType planetType;

    public GasPlanetTypeSelectEvent(int x, int y, int z, StellarSystem system, int index, SectorInformation.GasPlanetType type) {
        position = new Vector3i(x,y,z);
        this.system = system;
        this.sectorIndex = index;
        this.planetType = type;
    }

    public void setPlanetType(SectorInformation.GasPlanetType type){
        planetType = type;
    }

    public SectorInformation.GasPlanetType getPlanetType() { return planetType; }

    public StellarSystem getStellarSystem() { return system; }

    public Vector3i getSectorCoordinates() {
        return new Vector3i(position);
    }

    public int getSectorIndex() {
        return sectorIndex;
    }
}
