package api.listener.events.world.generation;

import api.listener.events.Event;
import org.schema.game.common.controller.FloatingRock;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.StellarSystem;

/**
 * STARMADE MOD
 * CREATOR: ITHIRAHAD
 * DATE: 1 Oct. 2020
 * TIME: flies when you're having fun
 *
 * This event fires whenever an asteroid is about to initialize.
 * All relevant information can be accessed and modified within the FloatingRock itself, obtained via this event's getAsteroid() method.
 * (That is how the default spawning method functions.)
 */
public class AsteroidPreSpawnEvent extends Event {
    private final FloatingRock asteroid;
    private final Sector sector;
    private final StellarSystem system;

    public AsteroidPreSpawnEvent(FloatingRock asteroid, Sector sector, StellarSystem starSystem) {
        this.asteroid = asteroid;
        this.sector = sector;
        this.system = starSystem;
    }

    /**
     * @return the FloatingRock segment controller which is the asteroid being spawned.
     */
    public FloatingRock getAsteroid(){
        return asteroid;
    }

    /**
     * @return the containing sector.
     */
    public Sector getSector(){
        return sector;
    }

    /**
     * @return the containing StellarSystem.
     */
    public StellarSystem getSystem() {
        return system;
    }
}
