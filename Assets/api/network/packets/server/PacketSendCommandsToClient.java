package api.network.packets.server;

import api.mod.StarLoader;
import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import api.utils.game.chat.CommandInterface;
import org.schema.game.common.data.player.PlayerState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * PacketSendCommandsToClient
 * Sends the client the list of commands on login.
 * [Server] -> [Client]
 *
 * @author TheDerpGamer
 * @since 04/28/2021
 */
public class PacketSendCommandsToClient extends Packet {

    public PacketSendCommandsToClient() {

    }

    @Override
    public void readPacketData(PacketReadBuffer buf) throws IOException {
        StarLoader.addClientCommandList(buf.readStringList());
    }

    @Override
    public void writePacketData(PacketWriteBuffer buf) throws IOException {
        ArrayList<String> commandList = new ArrayList<>();
        for(CommandInterface command : StarLoader.getAllCommands()) {
            commandList.add(command.getCommand());
            commandList.addAll(Arrays.asList(command.getAliases()));
        }
        buf.writeStringList(commandList);
    }

    @Override
    public void processPacketOnClient() {

    }

    @Override
    public void processPacketOnServer(PlayerState sender) {

    }
}
