package api.utils.game.module.util;

import api.mod.StarMod;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import api.network.packets.PacketUtil;
import api.utils.game.module.ModManagerContainerModule;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.schine.graphicsengine.core.Timer;

import java.io.IOException;

/**
 * Created by Jake on 9/2/2021.
 * Saves data using ModMCManagerContainers and the Json serializer
 *
 * Goals:
 */
public abstract class SimpleDataStorageMCModule extends ModManagerContainerModule {
    public Object data;

    public SimpleDataStorageMCModule(SegmentController ship, ManagerContainer<?> managerContainer, StarMod mod, short blockId) {
        super(ship, managerContainer, mod, blockId);
    }

    @Override
    public void onTagSerialize(PacketWriteBuffer buffer) throws IOException {
        if(data == null){
            buffer.writeBoolean(false);
        }else{
            buffer.writeBoolean(true);
            buffer.writeString(data.getClass().getName());
            buffer.writeObject(data);
        }
    }

    @Override
    public void onTagDeserialize(PacketReadBuffer buffer) throws IOException {
        if(buffer.readBoolean()) {
            String s = buffer.readString();
            try {
                Class<?> clazz = Class.forName(s);
                data = buffer.readObject(clazz);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void handle(Timer timer) {

    }

    @Override
    public double getPowerConsumedPerSecondResting() {
        return 0;
    }

    @Override
    public double getPowerConsumedPerSecondCharging() {
        return 0;
    }

    public void flagUpdatedData(){
        if(isOnServer()){
            syncToNearbyClients();
        }else{
            PacketCSSendSimpleDataMCModuleData packet = new PacketCSSendSimpleDataMCModuleData(getManagerContainer(), this);
            PacketUtil.sendPacketToServer(packet);
        }
    }
}
