package api.network;

import api.DebugFile;
import api.mod.exception.ModPacketNotFoundException;
import org.schema.game.common.data.player.PlayerState;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class Packet {
    public abstract void readPacketData(PacketReadBuffer buf) throws IOException;
    public abstract void writePacketData(PacketWriteBuffer buf) throws IOException;
    public abstract void processPacketOnClient();
    public abstract void processPacketOnServer(PlayerState sender);
    private static HashMap<Short, Class<? extends Packet>> packetLookup = new HashMap<Short, Class<? extends Packet>>();
    private static HashMap<Class<? extends Packet>, Short> reversePacketLookup = new HashMap<Class<? extends Packet>, Short>();
    private static short idLog = Short.MIN_VALUE;
    public static void registerPacket(Class<? extends Packet> clazz){
        if(reversePacketLookup.containsKey(clazz)){
            DebugFile.info("Already registered packet, skipping");
            return;
        }
        short id = idLog++;
        packetLookup.put(id, clazz);
        reversePacketLookup.put(clazz, id);
    }
    public static void dumpPacketLookup(){
        System.err.println("===== [ Packet Lookup ] =====");
        for (Map.Entry<Short, Class<? extends Packet>> entry : packetLookup.entrySet()) {
            System.err.println(entry.getKey() + " ---> " + entry.getValue());
        }
        System.err.println("===== [ Reverse Packet Lookup ] =====");
        for (Map.Entry<Class<? extends Packet>, Short> entry : reversePacketLookup.entrySet()) {
            System.err.println(entry.getKey() + " ---> " + entry.getValue());
        }
    }
    public static void clearPackets(){
        packetLookup.clear();
        reversePacketLookup.clear();
        idLog = Short.MIN_VALUE;
    }
    public static Packet newPacket(short id){
        Class<? extends Packet> clazz = packetLookup.get(id);
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            DebugFile.err("!!! INSTANTIATION ERROR !!! Likely your packet class does not have a default constructor");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            throw new ModPacketNotFoundException(id);
        }
        return null;
    }
    private static short getId(Class<? extends Packet> clazz){
        Short s = reversePacketLookup.get(clazz);
        if(s == null){
            DebugFile.err(" !!! PACKET ID NOT FOUND !!! Likely you did not register it with Packet.registerPacket(PacketClass.class) in onEnable: " + clazz.getSimpleName());
            throw new NullPointerException("Packet not found in the reverse packet lookup");
        }
        return s;
    }
    public short getId(){
        return getId(getClass());
    }

}
