package api.listener.events.controller.planet;

import api.listener.events.Event;
import org.schema.game.common.controller.generator.PlanetCreatorThread;
import org.schema.game.common.data.world.Segment;
import org.schema.game.server.controller.RequestDataPlanet;
import org.schema.game.server.controller.world.factory.WorldCreatorPlanetFactory;

/**
 * PlanetSegmentGenerateEvent.java
 * Called when a planet's terrain is generated.
 * ==================================================
 * Created 2/25/2021
 * @author TheDerpGamer
 */
public class PlanetSegmentGenerateEvent extends Event {

    private PlanetCreatorThread creatorThread;
    private RequestDataPlanet requestData;
    private WorldCreatorPlanetFactory factory;
    private Segment segment;

    public PlanetSegmentGenerateEvent(PlanetCreatorThread creatorThread, Segment segment, RequestDataPlanet requestData, WorldCreatorPlanetFactory factory) {
        this.creatorThread = creatorThread;
        this.requestData = requestData;
        this.factory = factory;
        this.segment = segment;
    }

    public PlanetCreatorThread getCreatorThread() {
        return creatorThread;
    }

    public RequestDataPlanet getRequestData() {
        return requestData;
    }

    public WorldCreatorPlanetFactory getFactory() {
        return factory;
    }

    public Segment getSegment() {
        return segment;
    }
}
