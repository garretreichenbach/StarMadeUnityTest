package api.listener.events.gui;

import api.listener.events.Event;
import org.schema.game.client.view.mainmenu.DialogInput;

/**
 * Called whenever a DialogInput is activated.
 *
 * These are used for many things, such as the text input for display modules, among many other things.
 */
public class DialogInputActivateEvent extends Event {
    private DialogInput input;

    public DialogInputActivateEvent(DialogInput input) {
        this.input = input;
    }

    /**
     * @return The DialogInput that is about to be activated.
     */
    public DialogInput getInput() {
        return input;
    }
}
