package api.listener.events.world.generation;

import api.listener.events.Event;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SectorInformation;
import org.schema.game.common.data.world.StellarSystem;

import java.util.Random;

/**
 * STARMADE MOD
 * CREATOR: ITHIRAHAD
 * DATE: 1 Oct. 2020
 * TIME: something I lost track of long ago
 *
 * This event fires whenever a sector which is NOT in an NPC-faction system determines how many asteroids it will create.
 */
public class AsteroidNormalPopulateEvent extends Event{
    private final StellarSystem containingSystem;
    private final Sector sector;
    private final SectorInformation.SectorType sectorType;
    private Random asteroidRNG;
    private Random sizeRNG;
    private int maxAsteroidPopulation;
    private int minAsteroidPopulation = 0;
    private boolean allowingAsteroidPopulation;
    private boolean overridingPopulation;
    private int forcedAsteroidCount;

    public AsteroidNormalPopulateEvent(StellarSystem system, Sector sector, SectorInformation.SectorType sectorInfoType, Random asteroidRNG, Random sizeRNG, int sectorMaxAsteroids, boolean doAsteroidPopulation) {
    this.containingSystem = system;
    this.sector = sector;
    this.sectorType = sectorInfoType;
    this.asteroidRNG = asteroidRNG;
    this.sizeRNG = sizeRNG;
    this.maxAsteroidPopulation = sectorMaxAsteroids;
    this.allowingAsteroidPopulation = doAsteroidPopulation;
    this.forcedAsteroidCount = 1; //lol 1 asteroid
    }

    /**
     * @return The system in which the asteroids are populating.
     */
    public StellarSystem getSystem(){
        return this.containingSystem;
    }

    /**
     * @return The sector in which the asteroids are populating.
     */
    public Sector getSector(){
        return this.sector;
    }

    /**
     * @return The coordinates of the sector in which the asteroids are populating.
     */
    public Vector3i getSectorPos(){
        return this.sector.pos; //not strictly necessary, but since it's such a common case and it makes code look marginally nicer to not do .getSector().pos every time, I'm including it :P
    }

    /**
     * @return The maximum population of asteroids that can be spawned in the sector.
     */
    public int getMaxAsteroidPopulation() {
        return maxAsteroidPopulation;
    }

    /**
     * @param maxAsteroidPopulation The maximum population of asteroids that can be spawned in the sector.
     */
    public void setMaxAsteroidPopulation(int maxAsteroidPopulation) {
        this.maxAsteroidPopulation = maxAsteroidPopulation;
    }

    /**
     * @return Is asteroid population allowed in this sector?
     */
    public boolean isAllowingAsteroidPopulation() {
        return allowingAsteroidPopulation;
    }

    /**
     * @param allow Whether or not asteroids may populate in this sector
     */
    public void setAllowingAsteroidPopulation(boolean allow) {
        this.allowingAsteroidPopulation = allow;
    }

    /**
     * @return Is this event overriding the vanilla population randomization?
     */
    public boolean isOverridingPopulation(){
        return this.overridingPopulation;
    }

    /**
     * @param value If true, the default randomization is overridden by the value assigned in setForcedAsteroidCount(). If false, the asteroid population is simply randomized between the minimum and maximum.
     */
    public void overridePopulation(boolean value){
        this.overridingPopulation = value;
    }

    /**
     * @return The minimum amount of asteroids allowed to spawn in the sector.
     */
    public int getMinAsteroidPopulation() {
        return minAsteroidPopulation;
    }

    /**
     * @param minAsteroidPopulation The minimum amount of asteroids allowed to spawn in the sector
     */
    public void setMinAsteroidPopulation(int minAsteroidPopulation) {
        if (minAsteroidPopulation >= 0) this.minAsteroidPopulation = minAsteroidPopulation;
        else this.minAsteroidPopulation = 0;
    }

    /**
     * @return the assigned overriding asteroid count. Note that this is not used if the event is not set to override the default asteroid population selection.
     */
    public int getForcedAsteroidCount() {
        return forcedAsteroidCount;
    }

    /**
     * @param forcedAsteroidCount if overriding population, this will be the number of asteroids that spawn in the sector.
     */
    public void setForcedAsteroidCount(int forcedAsteroidCount) {
        this.forcedAsteroidCount = forcedAsteroidCount;
    }
}
