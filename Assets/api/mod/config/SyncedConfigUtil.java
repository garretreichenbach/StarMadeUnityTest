package api.mod.config;

import api.common.GameServer;
import api.network.packets.PacketUtil;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.network.server.ServerProcessor;

/**
 * Created by Jake on 12/5/2020.
 * <insert description here>
 */
public class SyncedConfigUtil {
    public static void sendConfigToClients(FileConfiguration config){
        if(!config.getMod().getSkeleton().isServerMod()) {
            PacketSCSyncConfig packetSCSyncConfig = new PacketSCSyncConfig(config);
            for(PlayerState value : GameServer.getServerState().getPlayerStatesByName().values()) {
                PacketUtil.sendPacket(value, packetSCSyncConfig);
            }
        }
    }
    public static void sendConfigToClient(ServerProcessor processor, FileConfiguration config){
        if(!config.getMod().getSkeleton().isServerMod()) {
            PacketSCSyncConfig packetSCSyncConfig = new PacketSCSyncConfig(config);
            PacketUtil.sendPacket(processor, packetSCSyncConfig);
        }
    }
}
