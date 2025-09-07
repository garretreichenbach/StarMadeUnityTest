package api.listener.events.player;

import api.listener.events.Event;
import org.schema.game.common.data.chat.ChannelRouter;
import org.schema.game.network.objects.ChatMessage;

public class PlayerChatEvent extends Event {
    private ChatMessage message;
    private ChannelRouter channelRouter;

    public PlayerChatEvent(ChatMessage message, ChannelRouter channelRouter){

        this.message = message;
        this.channelRouter = channelRouter;
    }

    public ChatMessage getMessage() {
        return message;
    }
    public String getText(){
        return message.text;
    }
    public boolean onServer(){
        return channelRouter.isOnServer();
    }

    public ChannelRouter getChannelRouter() {
        return channelRouter;
    }
}
