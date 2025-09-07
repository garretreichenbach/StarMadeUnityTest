package api.utils.sound;

import api.common.GameServer;
import api.mod.ModSkeleton;
import api.network.packets.PacketUtil;
import com.bulletphysics.linearmath.Transform;
import org.apache.commons.io.FileUtils;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;

import javax.vecmath.Vector3f;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class AudioUtils { //Todo: Rewrite using new sound library
    /**
     * Play a sound from a location from the client
     */
    public static void clientPlaySound(String name, float x, float y, float z, float volume, float pitch) {
        //SoundManager manager = Controller.getAudioManager();
        //assert manager.isLoaded() : "Sound manager not loaded";
        //manager.playSound(name, x, y, z, volume, pitch);
    }

    /**
     * Play a sound directly from the client
     */
    public static void clientPlaySound(String name, float volume, float pitch) {
        //SoundManager manager = Controller.getAudioManager();
        //assert manager.isLoaded() : "Sound manager not loaded";
        //manager.playSoundFX(name, volume, pitch);
    }

    /**
     * Instruct clients to play a sound at a location
     */
    public static void serverPlaySound(String name, float x, float y, float z, float vol, float pitch, PlayerState... players) {
        PacketSCPlayAudio audio = new PacketSCPlayAudio(name, x, y, z, vol, pitch);
        for (PlayerState player : players) {
            PacketUtil.sendPacket(player, audio);
        }
    }

    /**
     * Instruct clients to play a sound directly
     */
    public static void serverPlaySound(String name, float vol, float pitch, PlayerState... players) {
        PacketSCPlayAudio audio = new PacketSCPlayAudio(name, vol, pitch);
        for (PlayerState player : players) {
            PacketUtil.sendPacket(player, audio);
        }
    }
    public static void serverPlaySound(String name, float vol, float pitch, Iterable<PlayerState> players) {
        PacketSCPlayAudio audio = new PacketSCPlayAudio(name, vol, pitch);
        for (PlayerState player : players) {
            PacketUtil.sendPacket(player, audio);
        }
    }

    /**
     * Instruct nearby clients to play a sound at a location
     *
     * @param broadcastDistance How far from the origin
     */
    public static void serverPlaySound(String name, float x, float y, float z, float vol, float pitch, float broadcastDistance) {
        //TODO: Make this faster, not that it really matters considering theres gonna be a max of like 10 people
        GameServerState serv = GameServer.getServerState();
        assert serv != null : "Server is null, cannot play sound";
        Vector3f loc = new Vector3f(x, y, z);
        for (PlayerState p : serv.getPlayerStatesByName().values()) {
            Transform t = new Transform();
            p.getWordTransform(t);
            Vector3f playerLocation = new Vector3f(t.origin);
            playerLocation.sub(loc);
            if(playerLocation.lengthSquared() < broadcastDistance*broadcastDistance){
                serverPlaySound(name, x,y,z, vol, pitch, p);
            }
        }

    }

    public static void registerModSound(ModSkeleton mod, String name, InputStream sound){
        // not gonna namespace these for some reason (hillarious bug to happen later)
        try {
            File dest = new File("moddata/StarLoader/modsounds/" + name + ".ogg");
            dest.getParentFile().mkdirs();
            FileUtils.copyInputStreamToFile(sound, dest);
            //Controller.getAudioManager().addSound(name, dest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
