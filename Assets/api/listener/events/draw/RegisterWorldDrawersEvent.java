package api.listener.events.draw;

import api.listener.events.Event;
import api.utils.draw.ModWorldDrawer;
import org.schema.game.client.view.WorldDrawer;

import java.util.ArrayList;

public class RegisterWorldDrawersEvent extends Event {
    private WorldDrawer drawer;

    public RegisterWorldDrawersEvent(WorldDrawer drawer){

        this.drawer = drawer;
    }
    private ArrayList<ModWorldDrawer> drawables = new ArrayList<ModWorldDrawer>();

    public ArrayList<ModWorldDrawer> getModDrawables() {
        return drawables;
    }

    public WorldDrawer getDrawer() {
        return drawer;
    }
}
