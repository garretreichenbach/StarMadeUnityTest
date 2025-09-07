package api.network.packets;

import api.common.GameClient;
import api.common.GameServer;
import api.network.Packet;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.network.RegisteredClientOnServer;
import org.schema.schine.network.client.ClientProcessor;
import org.schema.schine.network.server.ServerProcessor;

public class PacketUtil {
    public static void sendPacketToServer(Packet apiPacket) {
        ClientProcessor processor = GameClient.getClientState().getProcessor();
        processor.getModPacketQueue().add(apiPacket);
    }

    public static ServerProcessor getServerProcessor(RegisteredClientOnServer clientOnServer) {
        return (ServerProcessor) clientOnServer.getProcessor();
    }
    public static ObjectArrayFIFOQueue<ImmutablePair<PlayerState, Packet>> serverPacketQueue = new ObjectArrayFIFOQueue<>();
    public static ObjectArrayFIFOQueue<Packet> clientPacketQueue = new ObjectArrayFIFOQueue<>();

    public static void sendPacket(ServerProcessor processor, Packet apiPacket) {
//        processor.getModPacketQueue().add(apiPacket); TODO: Fix ServerProcessor
    }

    public static void sendPacket(PlayerState player, Packet apiPacket) {
        RegisteredClientOnServer serverClient = GameServer.getServerClient(player);
        if(serverClient == null){
            System.err.println("[PacketUtil] [WARNING] Player " + player + " had no server client, likely still logging in");
            System.err.println("[PacketUtil] [WARNING] Player Id: " + player.getId() + ", name: " + player.getName());
            StringBuilder sb = new StringBuilder("[PacketUtil] Clients: ");
            for (RegisteredClientOnServer value : GameServer.getServerState().getClients().values()) {
                sb.append("[id:").append(value.getId()).append(" name=").append(value.getClientName()).append(" state=").append(value.getPlayerObject());
            }
            System.err.println(sb.toString());
            throw new RuntimeException("Could not find player processor, please see debug messages above");
            // return;
        }
        ServerProcessor processor = getServerProcessor(serverClient);
        //processor.getModPacketQueue().add(apiPacket); Todo: Fix this
    }

    public static void registerPacket(Class<? extends Packet> aClass) {
        Packet.registerPacket(aClass);
    }

}
