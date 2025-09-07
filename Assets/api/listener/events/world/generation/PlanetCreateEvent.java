package api.listener.events.world.generation;

import api.listener.events.Event;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.PlanetIco;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.space.PlanetIcoCore;
import org.schema.game.server.controller.world.factory.planet.terrain.TerrainGenerator;

/**
 * Event called when a planet is created
 */
public class PlanetCreateEvent extends Event {

    private final Sector sector;
    private final PlanetIco[] planetSegments;
    private final PlanetIcoCore planetCore;
    private TerrainGenerator.TerrainGeneratorType planetType;

    public PlanetCreateEvent(Sector sector, PlanetIco[] planetSegments, PlanetIcoCore planetCore, TerrainGenerator.TerrainGeneratorType planetType) {
        this.sector = sector;
        this.planetSegments = planetSegments;
        this.planetCore = planetCore;
        this.planetType = planetType;
    }

    public Sector getSector() {
        return sector;
    }

    public Vector3i getSectorPos() {
        return sector.pos;
    }

    public PlanetIco[] getPlanetSegments() {
        return planetSegments;
    }

    public PlanetIco getPlanetSegment(int i) {
        return planetSegments[i];
    }

    public PlanetIcoCore getPlanetCore() {
        return planetCore;
    }

    public TerrainGenerator.TerrainGeneratorType getPlanetType() {
        return planetType;
    }

    public void setPlanetType(TerrainGenerator.TerrainGeneratorType planetType) {
        this.planetType = planetType;
    }
}
