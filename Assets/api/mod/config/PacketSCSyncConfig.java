package api.mod.config;

import api.ModPlayground;
import api.mod.ModIdentifier;
import api.mod.ModSkeleton;
import api.mod.StarLoader;
import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import api.utils.StarRunnable;
import org.schema.game.common.data.player.PlayerState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by Jake on 12/5/2020.
 * <insert description here>
 */
public class PacketSCSyncConfig extends Packet {
    public PacketSCSyncConfig() {
    }

    private ArrayList<String> keys = new ArrayList<>();
    private ArrayList<String> values = new ArrayList<>();
    private ModIdentifier mod;
    private String configName;
    public PacketSCSyncConfig(FileConfiguration config) {
        Set<String> keys = config.getKeys();
        for (String key : keys) {
            values.add(config.getString(key));
        }
        this.keys.addAll(keys);
        ModSkeleton modSkeleton = config.getMod().getSkeleton();
        mod = new ModIdentifier(modSkeleton.getSmdResourceId(), modSkeleton.getModVersion());
        configName = config.getName();
    }

    @Override
    public void readPacketData(PacketReadBuffer buf) throws IOException {
        keys = buf.readStringList();
        values = buf.readStringList();
        int id = buf.readInt();
        String ver = buf.readString();
        configName = buf.readString();

        mod = new ModIdentifier(id, ver);
    }

    @Override
    public void writePacketData(PacketWriteBuffer buf) throws IOException {
        buf.writeStringList(keys);
        buf.writeStringList(values);
        buf.writeInt(mod.id);
        buf.writeString(mod.version);
        buf.writeString(configName);
    }

    @Override
    public void processPacketOnClient() {
        new StarRunnable(){
            @Override
            public void run() {
                ModSkeleton modFromName = StarLoader.getModFromId(mod);
                if(modFromName == null){
                    System.err.println("getModFromId was null, dumping mod infos: ");
                    StarLoader.dumpModInfos(false);
                    throw new NullPointerException("Mod from id null. mod: " + mod);
                }
                FileConfiguration config = new FileConfiguration(modFromName.getRealMod(), configName, keys, values);
                SyncedConfigReceiveEvent event = new SyncedConfigReceiveEvent(config);
                StarLoader.fireEvent(event, false);
            }
        }.runLater(ModPlayground.inst, 0);
    }

    @Override
    public void processPacketOnServer(PlayerState sender) {

    }
}
