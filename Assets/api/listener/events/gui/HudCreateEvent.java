package api.listener.events.gui;

import api.listener.events.Event;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.shiphud.newhud.Hud;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;

import java.util.ArrayList;

public class HudCreateEvent extends Event {
    private Hud hud;
    private GameClientState state;
    public ArrayList<GUIElement> elements = new ArrayList<GUIElement>();
    public HudCreateEvent(Hud hud, GameClientState state){

        this.hud = hud;
        this.state = state;
    }

    public Hud getHud() {
        return hud;
    }
    public GameClientState getInputState(){
        return state;
    }
    public void addElement(GUIElement element){
        this.elements.add(element);
    }
}
