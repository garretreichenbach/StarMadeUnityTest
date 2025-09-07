package api.utils.game.module;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.network.objects.Sendable;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by Jake on 4/23/2021.
 * <insert description here>
 */
public class PacketCSSendMCModuleData extends Packet {

    private ModManagerContainerModule module;
    private byte[] buffer;

    public PacketCSSendMCModuleData() {
    }

    public PacketCSSendMCModuleData(ModManagerContainerModule module, byte[] buffer) {
        this.buffer = buffer;
        this.module = module;
    }
    @Override
    public void readPacketData(PacketReadBuffer buf) throws IOException {
        int blockId = buf.readInt();
        Sendable sendable = buf.readSendable(true);
        if (sendable instanceof ManagedUsableSegmentController) {
            ManagedUsableSegmentController<?> container = (ManagedUsableSegmentController<?>) sendable;
            module = container.getManagerContainer().getModMCModule((short) blockId);
            buffer = buf.readByteArray();
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public void writePacketData(PacketWriteBuffer buf) throws IOException {
        buf.writeInt(module.getBlockId());
        buf.writeSendable(module.getManagerContainer().getSegmentController());
        buf.writeByteArray(buffer);
    }

    @Override
    public void processPacketOnClient() {

    }

    @Override
    public void processPacketOnServer(PlayerState sender) {
        // Wrap our buffer in a PacketReadBuffer and send it to the module
        try {
            module.onReceiveDataServer(new PacketReadBuffer(new DataInputStream(new ByteArrayInputStream(buffer))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
