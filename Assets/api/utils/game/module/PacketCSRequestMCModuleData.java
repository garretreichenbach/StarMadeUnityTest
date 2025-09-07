package api.utils.game.module;

import api.ModPlayground;
import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import api.utils.StarRunnable;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;

import java.io.IOException;

/**
 * Created by Jake on 12/19/2020.
 * <insert description here>
 */
public class PacketCSRequestMCModuleData extends Packet {
    private int controllerId;
    private short moduleId;

    public PacketCSRequestMCModuleData() {

    }

    public PacketCSRequestMCModuleData(ManagerContainer<?> container, ModManagerContainerModule module) {
        controllerId = container.getSegmentController().getId();
        moduleId = module.getBlockId();
    }

    @Override
    public void readPacketData(PacketReadBuffer buf) throws IOException {
        controllerId = buf.readInt();
        moduleId = buf.readShort();
    }

    @Override
    public void writePacketData(PacketWriteBuffer buf) throws IOException {
        buf.writeInt(controllerId);
        buf.writeShort(moduleId);
    }

    @Override
    public void processPacketOnClient() {

    }

    @Override
    public void processPacketOnServer(final PlayerState sender) {
        new StarRunnable(){
            @Override
            public void run() {
                try {
                    ManagerContainer<?> container = ((ManagedUsableSegmentController<?>) GameServerState.instance.getLocalAndRemoteObjectContainer().getLocalObjects().get(controllerId)).getManagerContainer();
                    ModManagerContainerModule module = container.getModMCModule(moduleId);
                    module.syncToClient(sender);
                }catch (NullPointerException e){
                    e.printStackTrace();
                    System.err.println("[Error] Controller: " + controllerId + ", managerId");
                }
            }
        }.runLater(ModPlayground.inst, 0);


    }
}
