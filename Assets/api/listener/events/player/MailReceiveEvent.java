package api.listener.events.player;

import api.listener.events.Event;
import org.schema.game.client.controller.ClientChannel;
import org.schema.game.common.data.player.playermessage.PlayerMessage;

public class MailReceiveEvent extends Event {

    private final PlayerMessage message;
    private final ClientChannel channel;
    private boolean canceled = false;

    public MailReceiveEvent(PlayerMessage message, ClientChannel channel) {

        this.message = message;
        this.channel = channel;
    }
    public void setCanceled(boolean c){
        canceled = c;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public PlayerMessage getMessage() {
        return message;
    }

    public ClientChannel getChannel() {
        return channel;
    }

}
