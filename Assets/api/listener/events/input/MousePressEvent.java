package api.listener.events.input;

import api.listener.events.Event;
import api.listener.type.ClientEvent;
import org.schema.schine.graphicsengine.core.MouseEvent;

/**
 * MousePressEvent.java
 * StarLoader mouse press event
 * ==================================================
 * Updated 1/21/2021
 * @author JakeV, TheDerpGamer
 */
@ClientEvent
public class MousePressEvent extends Event {

    private MouseEvent event;

    public MousePressEvent(MouseEvent event) {
        this.event = event;
    }

    /**
     * @return The integer representing the mouse button pressed.
     * 0 : Left Mouse
     * 1 : Right Mouse
     * 2 : Middle Mouse
     */
    //public int getButton() {
    //    return event.getEventButton();
    //}

    /**
     * @return The scrolled direction.
     * 1 : Up
     * -1 : Down
     * 0 : Neutral (No scroll)
     */
    public int getScrollDirection() {
        return Integer.compare(event.dWheel, 0);
    }

    /**
     * @return The raw event.
     */
    public MouseEvent getRawEvent() {
        return event;
    }
}