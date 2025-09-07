package api.listener.events.gui;

import api.listener.events.Event;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;

public class GUIElementInstansiateEvent extends Event {

    private GUIElement guiElement;
    private InputState inputState;

    public GUIElementInstansiateEvent(GUIElement guiElement, InputState inputState) {
        this.guiElement = guiElement;
        this.inputState = inputState;
    }

    public GUIElement getGUIElement() {
        return guiElement;
    }

    public InputState getInputState() {
        return inputState;
    }
}