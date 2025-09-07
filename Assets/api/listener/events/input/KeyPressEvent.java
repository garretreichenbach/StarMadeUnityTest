package api.listener.events.input;

import api.listener.events.Event;
import api.listener.type.ClientEvent;
import org.schema.schine.input.KeyboardEvent;

/**
 * KeyPressEvent.java
 * StarLoader key press event
 * ==================================================
 * Updated 1/21/2021
 * @author JakeV, TheDerpGamer
 */
@ClientEvent
public class KeyPressEvent extends Event {

    private KeyboardEvent event;

    public KeyPressEvent(KeyboardEvent event) {
        this.event = event;
    }

    /**
     * @return The character representing the key pressed.
     */
    public String getChar(){
        return event.getCharacter();
    }

    /**
     * @return The integer representing the key pressed.
     */
    public int getKey(){
        return event.getKey();
    }

    /**
     * @return If the key is currently down.
     */
    public boolean isKeyDown(){
        return event.actionState == 1;
    }

    /**
     * @return The raw event.
     */
    public KeyboardEvent getRawEvent(){
        return event;
    }
}
