package api.listener.events.world.generation;

import api.listener.events.Event;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.FloatingRock;
import org.schema.game.common.data.world.Sector;

/**
 * STARMADE MOD
 * AUTHOR: ITHIRAHAD
 * DATE: 2 Oct 2020
 * TIME: out
 *
 * This event fires when the mysterious asteroid Vector3i "pos" is set.
 * I completely fail to understand what pos is or how it affects asteroids, but the game seems to care about it a lot.
 * So, this event lets you modify it.
 * Since it fires after asteroid initialization, you can use it to reposition the asteroid via its transform or accomplish a number of other things that can't be done in AsteroidPreSpawnEvent.
 * If you modify the position by these other methods, you will likely have to call forceMarkPosChanged() to stop the addRandomRock method from removing your asteroid entirely.
 */
public class AsteroidPositionEvent extends Event {
    private boolean posChanged;
    private Vector3i pos;
    private final Sector sector;
    private FloatingRock asteroid;
    private final int attempts;

    public AsteroidPositionEvent(Sector sector, FloatingRock rock, Vector3i pos, int attempts) {
        posChanged = false;
        this.pos = pos;
        this.sector = sector;
        this.asteroid = rock;
        this.attempts = attempts;
    }

    /**
     * forces the game to treat the position as modded, bypassing the vanilla collision checks
     */
    public void forceMarkPosChanged(){
        posChanged = true;
    }

    public Vector3i getPos() {
        return pos;
    }

    public void setPos(Vector3i pos) {
        posChanged = true;
        this.pos = pos;
    }

    public boolean posChanged(){
        return posChanged;
    }

    /**
     * @return The number of vanilla attempts to make "pos" avoid collision with the floating rock.
     * If this reaches 1000 and the event does not change "pos", the game will refuse to place the asteroid.
     * However, calling setPos automatically resets this counter to 1 after the event resolves, allowing the asteroid to be placed.
     */
    public int getAttempts() {
        return attempts;
    }

    public FloatingRock getAsteroid() {
        return asteroid;
    }

    public void setAsteroid(FloatingRock asteroid) {
        this.asteroid = asteroid;
    }

    public Sector getSector() {
        return sector;
    }
}
