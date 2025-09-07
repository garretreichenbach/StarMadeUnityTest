package api.listener.events.gui;

import api.listener.events.Event;
import org.schema.game.client.view.gui.PlayerPanel;

public class PlayerGUIDrawEvent extends Event {
    private PlayerPanel playerPanel;

    public PlayerGUIDrawEvent(PlayerPanel playerPanel) {

        this.playerPanel = playerPanel;
    }

    public PlayerPanel getPlayerPanel() {
        return playerPanel;
    }
}