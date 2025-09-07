package api.network.packets.client;

import api.listener.events.player.PlayerCustomCommandEvent;
import api.mod.StarLoader;
import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import api.utils.game.PlayerUtils;
import api.utils.game.chat.CommandInterface;
import org.schema.game.common.data.player.PlayerState;

import java.io.IOException;

/**
 * Created by Jake on 10/18/2020.
 * <insert description here>
 */
public class PacketCSAdminCommand extends Packet {
    String command;
    String[] args;

    public PacketCSAdminCommand(String command, String[] args) {
        this.command = command;
        this.args = args;
    }
    public PacketCSAdminCommand(){

    }

    @Override
    public void readPacketData(PacketReadBuffer buf) throws IOException {
        command = buf.readString();
        args = buf.readString().split(", ");
    }

    @Override
    public void writePacketData(PacketWriteBuffer buf) throws IOException {
        buf.writeString(command);
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < args.length; i ++) {
            builder.append(args[i]);
            if(i < args.length - 1) builder.append(", ");
        }
        buf.writeString(builder.toString());
    }

    @Override
    public void processPacketOnClient() {
    }

    @Override
    public void processPacketOnServer(PlayerState sender) {
        CommandInterface cmd = StarLoader.getCommand(command);
        if(cmd != null) {
            if(cmd.isAdminOnly() && !sender.isAdmin()) {
                PlayerUtils.sendMessage(sender, "You must be an admin to perform this command!");
            } else {
                boolean success = cmd.onCommand(sender, args);
                StringBuilder builder = new StringBuilder();
                for(String arg : args) builder.append(arg).append(" ");
                StarLoader.fireEvent(new PlayerCustomCommandEvent(cmd,command + " " + builder.toString().trim(), args, sender), true);
                if(!success) PlayerUtils.sendMessage(sender, "Incorrect usage \"/" + command + " " + builder.toString().trim() + "\".\nUse /help " + command + " for proper usages.");
                //cmd.serverAction(sender, args);
            }
        } else PlayerUtils.sendMessage(sender, command + " is not a valid command.");
    }
}
