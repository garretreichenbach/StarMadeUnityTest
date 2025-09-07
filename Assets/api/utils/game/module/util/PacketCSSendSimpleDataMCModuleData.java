package api.utils.game.module.util;

import api.ModPlayground;
import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import api.utils.StarRunnable;
import api.utils.game.module.ModManagerContainerModule;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;

import java.io.IOException;

/**
 * Created by Jake on 9/2/2021.
 * <insert description here>
 */
public class PacketCSSendSimpleDataMCModuleData extends Packet {
    private int controllerId;
    private short moduleId;
    private ModManagerContainerModule module;

    public PacketCSSendSimpleDataMCModuleData() {
    }
    public PacketCSSendSimpleDataMCModuleData(ManagerContainer<?> container, ModManagerContainerModule module) {
        controllerId = container.getSegmentController().getId();
        moduleId = module.getBlockId();
        this.module = module;
    }

    @Override
    public void readPacketData(PacketReadBuffer buf) throws IOException {
        controllerId = buf.readInt();
        moduleId = buf.readShort();
        ManagerContainer<?> container = ((ManagedUsableSegmentController<?>) GameServerState.instance.getLocalAndRemoteObjectContainer().getLocalObjects().get(controllerId)).getManagerContainer();
        ModManagerContainerModule module = container.getModMCModule(moduleId);
        module.onTagDeserialize(buf);
    }

    @Override
    public void writePacketData(PacketWriteBuffer buf) throws IOException {
        buf.writeInt(controllerId);
        buf.writeShort(moduleId);
        module.onTagSerialize(buf);
    }

    @Override
    public void processPacketOnClient() {

    }

    @Override
    public void processPacketOnServer(PlayerState sender) {
        new StarRunnable(){
            @Override
            public void run() {
                ManagerContainer<?> container = ((ManagedUsableSegmentController<?>) GameServerState.instance.getLocalAndRemoteObjectContainer().getLocalObjects().get(controllerId)).getManagerContainer();
                ModManagerContainerModule module = container.getModMCModule(moduleId);
                module.syncToNearbyClients();
            }
        }.runLater(ModPlayground.inst, 0);

    }
}
