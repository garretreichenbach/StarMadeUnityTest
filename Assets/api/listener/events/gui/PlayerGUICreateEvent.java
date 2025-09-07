package api.listener.events.gui;

import api.listener.events.Event;
import org.schema.game.client.view.gui.PlayerPanel;

public class PlayerGUICreateEvent extends Event {

    private PlayerPanel playerPanel;

    public PlayerGUICreateEvent(PlayerPanel playerPanel) {

        this.playerPanel = playerPanel;
    }

    public PlayerPanel getPlayerPanel() {
        return playerPanel;
    }
}
