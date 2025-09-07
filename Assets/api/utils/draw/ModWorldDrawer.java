package api.utils.draw;

import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.Timer;

/**
 * Created by Jake on 3/11/2021.
 * Mod World drawers, can hook into various parts of the world draw cycle
 *
 * Not an interface because we are living in the dark ages of java 7 where you cant have defualt methods
 */
public abstract class ModWorldDrawer implements Drawable {
    public abstract void update(Timer timer);

    /**
     * Dummy draw method
     */
    @Override
    public void draw() { }

    /**
     * Draw uhhhhhhhh planet like stuff here idk how it works
     */
    public void preCameraPrepare(){}
    //TODO More hooks to be added

    public void postWorldDraw(){}

    public void postGameMapDraw(){}
}
