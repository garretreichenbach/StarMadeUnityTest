package api.listener.events.world.generation;

import api.listener.events.Event;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.SectorInformation;
import org.schema.game.common.data.world.StellarSystem;

/**
 * STARMADE MOD
 * CREATOR: Ithirahad Ivrar'kiim
 * DATE: 4/8/2021
 * TIME: Who knows?
 */

public class TerrestrialPlanetTypeSelectEvent extends Event {
    /**
     * This event fires when the game chooses planet types.
     * Status: Not yet tested.
     */
    private SectorInformation.PlanetType planetType;
    private final int sectorIndex;
    private final StellarSystem system;
    private final Vector3i position;
    public TerrestrialPlanetTypeSelectEvent(int x, int y, int z, StellarSystem starSystem, int index, SectorInformation.PlanetType type) {
        planetType = type;
        system = starSystem;
        sectorIndex = index;
        position = new Vector3i(x,y,z);
    }

    public void setPlanetType(SectorInformation.PlanetType type){
        planetType = type;
    }

    public SectorInformation.PlanetType getPlanetType() { return planetType; }

    public StellarSystem getStellarSystem() { return system; }

    public Vector3i getSectorCoordinates() {
        return new Vector3i(position);
    }

    public int getSectorIndex() {
        return sectorIndex;
    }
}
