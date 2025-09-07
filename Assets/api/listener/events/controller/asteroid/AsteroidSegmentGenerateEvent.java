package api.listener.events.controller.asteroid;

import api.listener.events.Event;
import org.schema.game.common.controller.generator.AsteroidCreatorThread;
import org.schema.game.common.data.world.Segment;
import org.schema.game.server.controller.RequestDataAsteroid;
import org.schema.game.server.controller.world.factory.asteroid.WorldCreatorFloatingRockFactory;

/**
 * Called whenever the segment of an asteroid is about to be generated
 */
public class AsteroidSegmentGenerateEvent extends Event {
    private final AsteroidCreatorThread asteroidCreatorThread;
    private Segment segment;
    private final RequestDataAsteroid requestData;
    private WorldCreatorFloatingRockFactory factory;

    public AsteroidSegmentGenerateEvent(AsteroidCreatorThread asteroidCreatorThread, Segment segment, RequestDataAsteroid requestData, WorldCreatorFloatingRockFactory factory) {
        this.asteroidCreatorThread = asteroidCreatorThread;
        this.segment = segment;
        this.requestData = requestData;
        this.factory = factory;

    }

    public Segment getSegment() {
        return segment;
    }

    public AsteroidCreatorThread getAsteroidCreatorThread() {
        return asteroidCreatorThread;
    }

    public RequestDataAsteroid getRequestData() {
        return requestData;
    }

    public void setWorldCreatorFloatingRockFactory(WorldCreatorFloatingRockFactory factory) {
        this.factory = factory;
    }

    public WorldCreatorFloatingRockFactory getWorldCreatorFloatingRockFactory() {
        return factory;
    }
}
