package api.listener.events;

import org.schema.game.client.controller.ClientChannel;
import org.schema.game.common.data.player.playermessage.PlayerMessage;

public class MailReceiveEvent extends Event{

    public static int id = 6;

    private final PlayerMessage message;
    private final ClientChannel channel;
    private final boolean onServer;
    private boolean canceled = false;

    public MailReceiveEvent(PlayerMessage message, ClientChannel channel, boolean onServer) {

        this.message = message;
        this.channel = channel;
        this.onServer = onServer;
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

    public boolean isOnServer() {
        return onServer;
    }
}
