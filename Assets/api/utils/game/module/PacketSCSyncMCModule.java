package api.utils.game.module;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.network.objects.Sendable;

import java.io.IOException;

/**
 * Created by Jake on 12/17/2020.
 * <insert description here>
 */
public class PacketSCSyncMCModule extends Packet {
    private ModManagerContainerModule module;
    private boolean sendToServer;

    public PacketSCSyncMCModule(ModManagerContainerModule module, boolean sendToServer) {
        this.module = module;
        this.sendToServer = sendToServer;
    }

    public PacketSCSyncMCModule() {
    }

    @Override
    public void readPacketData(PacketReadBuffer buf) throws IOException {
        // Read the packet directly into the clients MMCM
        int blockId = buf.readInt();
        sendToServer = buf.readBoolean();
        Sendable sendable = buf.readSendable(sendToServer);
        //todo move to respective correct locations
        if (sendable instanceof ManagedSegmentController) {
            ManagedSegmentController<?> container = (ManagedSegmentController<?>) sendable;
            ModManagerContainerModule module = container.getManagerContainer().getModMCModule((short) blockId);
            module.onTagDeserialize(buf);
        } else if(sendable == null) {
            System.err.println("[WARNING] " + (sendToServer ? "Server" : "Client") + " recieved MMCModule synch packet for a null entity!");
            //we can pretty much ignore this
        }
        else {
            throw new RuntimeException("This Sendable does not represent a managed entity! Type: " + sendable.getTopLevelType());
        }
    }

    @Override
    public void writePacketData(PacketWriteBuffer buf) throws IOException {
        write(buf, sendToServer, module);
    }

    public static void write(PacketWriteBuffer buf, boolean server, ModManagerContainerModule module) throws IOException {
        buf.writeInt(module.getBlockId());
        buf.writeBoolean(server);
        buf.writeSendable(module.getManagerContainer().getSegmentController());
        module.onTagSerialize(buf);
    }

    @Override
    public void processPacketOnClient() {

    }

    @Override
    public void processPacketOnServer(PlayerState sender) {

    }
}
