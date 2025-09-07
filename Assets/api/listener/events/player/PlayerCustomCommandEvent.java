package api.listener.events.player;

import api.listener.events.Event;
import api.utils.game.chat.CommandInterface;
import org.schema.game.common.data.player.PlayerState;

import java.util.Arrays;

public class PlayerCustomCommandEvent extends Event {

    private final CommandInterface command;
    private final PlayerState sender;
    private final String[] args;
    private final String fullLine;

    public PlayerCustomCommandEvent(CommandInterface command, String fullLine, String[] args, PlayerState sender) {
        this.command = command;
        this.sender = sender;
        this.args = args;
        this.fullLine = fullLine;
    }

    @Override
    public String toString() {
        return "PlayerCommandEvent{" +
                "command='" + command.getCommand() + '\'' +
                ", player=" + sender +
                ", args=" + Arrays.toString(args) +
                '}';
    }

    public String getFullLine() {
        return fullLine;
    }

    public CommandInterface getCommand() {
        return command;
    }

    public PlayerState getSender() {
        return sender;
    }

    public String[] getArgs() {
        return args;
    }
}
