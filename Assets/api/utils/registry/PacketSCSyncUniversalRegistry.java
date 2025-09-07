package api.utils.registry;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import org.schema.game.common.data.player.PlayerState;

import java.io.IOException;
import java.util.Map;

/**
 * Created by Jake on 10/23/2020.
 * <insert description here>
 */
public class PacketSCSyncUniversalRegistry extends Packet {
    public PacketSCSyncUniversalRegistry(){
    }

    //No threading issues here
    @Override
    public void readPacketData(PacketReadBuffer buf) throws IOException {
        for (int i = 0; i < UniversalRegistry.RegistryType.values().length; i++) {
            byte ordinal = buf.readByte();
            UniversalRegistry.RegistryType type = UniversalRegistry.RegistryType.values()[ordinal];
            long entries = buf.readLong();
            for (long j = 0; j < entries; j++) {
                type.getDataMap().put(buf.readString(), buf.readLong());
            }
        }
        UniversalRegistry.dumpRegistry();
    }

    @Override
    public void writePacketData(PacketWriteBuffer buf) throws IOException {
        for (UniversalRegistry.RegistryType type : UniversalRegistry.RegistryType.values()) {
            //URType(byte)


            //Keys(int)

            //ModName:Uid(string)
            //value(long)
            //...
            //...
            buf.writeByte((byte) type.ordinal());
            buf.writeLong(type.getDataMap().size());
            for (Map.Entry<String, Long> entry : type.getDataMap().entrySet()) {
                buf.writeString(entry.getKey());
                buf.writeLong(entry.getValue());
            }

        }
    }

    @Override
    public void processPacketOnClient() {

    }

    @Override
    public void processPacketOnServer(PlayerState sender) {

    }
}
