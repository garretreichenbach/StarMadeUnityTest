package api.listener.events.world.generation;

import api.listener.events.Event;
import org.namegen.NameGenerator;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.server.data.Galaxy;

import javax.validation.constraints.NotNull;
import javax.vecmath.Vector4f;
import java.util.Random;

/**
 * STARMADE MOD
 * CREATOR: ITHIRAHAD
 * DATE: 1 Sept. 2020
 * TIME: 0420 EDT (not even kidding, my sleep schedule's just a mess)
 *
 *  This event fires after the default name for a star system is generated, but before it is actually assigned to the system.
 *  You can use it to implement your own name generator (or re-call the stock one with new arguments) and substitute those names for the defaults.
 *  Please note that as of now, this event only fires when generating STAR systems, not void systems (which don't get a permanent name; requesting their method always returns the string "Void".)
 */
public class SystemNameGenerationEvent extends Event {
    private String name;
    private final Vector3i systemCoordinates;
    private final Boolean isVoid;
    private final int systemType; //TYPE_SUN = 0; TYPE_GIANT = 1; TYPE_BLACK_HOLE = 2; TYPE_DOUBLE_STAR = 3.
    /* Please note that void systems carry these types too.
    They are assigned to every system block regardless of whether or not it actually receives a star in generation.
     */
    private final Vector4f sunColor;
    private final Galaxy galaxy;
    private final int defaultSyllables;
    private final Random random;
    /*
    Can't make this usefully mutable because by the time the event can be fired, this number was already polled.
    Just here in case someone wants to use the default number of syllables for anything for whatever reason.
    I refuse to split this into a PreSystemNameGenerationEvent and a SystemNameGenerationFinishedEvent just for this ONE case -
    if you REALLY want to change this and only this about default gen, you can just call the name gen again with your own
    parameters and spit the result out into the event.

    ~Ithir
    */
    private NameGenerator nameGenerator;
    private final long seed; //ditto - if you want to use your own seeds with default gen, call it again

    /**
     * @param name              The name of the system. The event initializes with the default generated name, but will override it with a custom name if assigned.
     * @param seed              The default seed passed to the stock name generator. Read-only.
     * @param defaultSyllables  The number of syllables chosen for the stock name generator. Read-only.
     * @param systemType        The type of system that did or would generate here. Per Galaxy class: TYPE_SUN = 0; TYPE_GIANT = 1; TYPE_BLACK_HOLE = 2; TYPE_DOUBLE_STAR = 3. Please note that void systems carry these types too; they are assigned to every system block regardless of whether or not it actually receives a star in generation. Read-only.
     * @param galaxy            The galaxy this system is generated in.
     * @param systemCoordinates The system block's coordinates in the star system grid. Read-only.
     */

    public SystemNameGenerationEvent(String name, NameGenerator nameGenerator, final long seed, final Random random, final int defaultSyllables, final boolean isVoid, final int systemType, final Vector4f sunColor, final Galaxy galaxy, final Vector3i systemCoordinates) {
        this.name = name;
        this.seed = seed;
        this.random = random;
        this.defaultSyllables = defaultSyllables;
        this.systemType = systemType;
        this.sunColor = sunColor;
        this.systemCoordinates = systemCoordinates;
        this.galaxy = galaxy;
        this.isVoid = isVoid;
        this.nameGenerator = nameGenerator;
    }

    public long getSeed(){
        return this.seed;
    }

    public Galaxy getContainingGalaxy(){
        return this.galaxy;
    }

    public Vector3i getSystemCoordinates(){
        return new Vector3i(systemCoordinates); //definitely not passing this by reference. :P
    }

    public String getName(){
        return this.name;
    }

    public Boolean getIsVoid(){
        return this.isVoid;
    }

    public void setName(String name){
        this.name = name;
    }

    public Vector4f getSunColor(){
        return new Vector4f(sunColor);
    }

    public NameGenerator getNameGenerator() {
        return nameGenerator;
    }

    public int getSystemType(){
        return systemType;
    }

    public void setNameGenerator(@NotNull NameGenerator nameGenerator) {
        this.nameGenerator = nameGenerator;
    }

    public Random getRNG() {
        return random;
    }

    public int getDefaultSyllables() {
        return defaultSyllables;
    }
}
