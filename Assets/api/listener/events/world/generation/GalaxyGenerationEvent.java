package api.listener.events.world.generation;

import api.listener.events.Event;
import org.schema.common.util.linAlg.Vector3i;

/**
 * STARMADE MOD
 * CREATOR: IR0NSIGHT
 * DATE: 25.08.2020
 * TIME: 15:48
 *
 * Fired when a new universe is generated. Holds all values needed to tweak the random spiral galaxy generation.
 * starCount, armCount, spread, range and rotation are vanilla fields.
 * starChance is a modded field. 1/starChance stars will be created. starCount will not be met if starChance != 1
 */
public class GalaxyGenerationEvent extends Event {
    private long seed;
    private double starCount;
    private double armCount;
    private double spread;
    private double range;
    private double rotation;
    private final Vector3i pos;

    public GalaxyGenerationEvent(final Vector3i galaxyPos, final long seed, double starCount, double armCount, double spread, double range, double rotation) {
        this.pos = galaxyPos;
        this.seed = seed;
        this.starCount = starCount;
        this.armCount = armCount;
        this.spread = spread;
        this.range = range;
        this.rotation = rotation;
    }

    public void setStarCount(double c) {
        starCount = c;
    }

    public void  setArmCount(double c) {
        armCount = c;
    }

    public void setSpread (double s) {
        spread = s;
    }

    public void setRange (double r) {
        range = r;
    }

    public void setRotation(double r) {
        rotation = r;
    }

    public void setSeed(long s) { seed = s; }

    public double getArmCount() {
        return armCount;
    }

    public double getRange() {
        return range;
    }

    public double getRotation() {
        return rotation;
    }

    public double getSpread() {
        return spread;
    }

    public double getStarCount() {
        return starCount;
    }

    public long getSeed() {
        return seed;
    }

    public Vector3i getGalaxyCoordinates() {
        return pos;
    }
}
