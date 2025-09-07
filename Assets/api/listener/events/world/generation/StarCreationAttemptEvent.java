package api.listener.events.world.generation;

import api.listener.events.Event;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.server.data.Galaxy;

import javax.vecmath.Vector4f;
import java.util.Random;

/**
 * STARMADE MOD
 * CREATOR: ITHIRAHAD
 * DATE: Sure, why not?
 * TIME: Of the essence
 *
 * An event which fires every time the galaxy generation method attempts to create a star.
 */
public class StarCreationAttemptEvent extends Event {
    private final Galaxy galaxy;
    private final Vector3i position;
    private byte starWeight;
    private final Vector4f color;
    private final float colorIndex;
    private final float colorIntensity;
    private final int starType;
    private final Random rng;

    /**
     * @param position              Coordinates of the star (in systems, not sectors). Origin is at the lower corner of the galaxy.
     * @param galaxy                The galaxy this star is contained in.
     * @param weight                The spawning weight for this star
     * @param color                 The RGBA color of the star
     * @param index                 The star's color index
     * @param intensity             The star's color intensity
     * @param type                  The star's type ID (Currently valid values: 0-3)
     */
    public StarCreationAttemptEvent(Random rng, Vector3i position, Galaxy galaxy, byte weight, Vector4f color, float index, float intensity, int type){
        this.position = new Vector3i(position);
        this.galaxy = galaxy;
        this.starWeight = weight;
        this.starType = type;
        this.color = new Vector4f(color);
        this.colorIndex = index;
        this.colorIntensity = intensity;
        this.rng = rng;
    }

    /**
     * Provides the coordinates of the star system. The origin is the lower corner of the galaxy block.
     * @return the star's coordinates
     */
    public Vector3i getPosition() {
        return position;
    }

    /**
     * @return the RGBA color of the star/stellar object in the system.
     */
    public Vector4f getColor() {
        return color;
    }

    /**
     * @param weight sets the generation "weight" of the star, determining whether or not it spawns.
     * If weight is below the threshold, the star does not spawn and you get a void system instead.
     */
    public void setStarWeight(byte weight) { starWeight = weight; }

    /**
     * Returns the "weight" for star generation, as assigned by the galaxy generation algorithm.
     * This is not the mass of the star.
     * @return star generation weight
     */
    public byte getStarWeight() {
        return starWeight;
    }

    /**
     * Returns the "color intensity" corresponding to this star.
     * This is a vanilla parameter that is not fully understood at the time of event implementation,
     * but presumably it may be somehow useful, so it is exposed by the event.
     * @return color intensity of the star
     */
    public float getColorIntensity() {
        return colorIntensity;
    }

    /**
     * Returns the "color index" corresponding to this star.
     * This is a vanilla parameter that is not fully understood at the time of event implementation,
     * but presumably it may be somehow useful, so it is exposed by the event.
     * @return color index of the star
     */
    public float getColorIndex() {
        return colorIndex;
    }

    /**
     * Returns the star type.
     * 0 is a standard star, 1 is a giant star, 2 is a black hole, 3 is a double star.
     * Any other values may be modded custom types.
     * @return star type ID
     */
    public int getStarType() {
        return starType;
    }

    /**
     * Returns the galaxy this star is contained in.
     * @return parent galaxy
     */
    public Galaxy getGalaxy(){
        return galaxy;
    }

    public Random getRng() {
        return rng;
    }
}
