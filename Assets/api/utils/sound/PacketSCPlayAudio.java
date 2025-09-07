package api.utils.sound;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import org.schema.game.common.data.player.PlayerState;

import java.io.IOException;

public class PacketSCPlayAudio extends Packet {
    private static final float DO_NOT_USE_LOCATION_FLAG = Float.POSITIVE_INFINITY;
    private String namef;
    private float xf;
    private float yf;
    private float zf;
    private float volumef;
    private float pitchf;

    public PacketSCPlayAudio(){

    }
    public PacketSCPlayAudio(String name, float x, float y, float z, float volume, float pitch){
        namef = name;
        xf = x;
        yf = y;
        zf = z;
        volumef = volume;
        pitchf = pitch;
    }
    public PacketSCPlayAudio(String name, float volume, float pitch){
        namef = name;
        xf = DO_NOT_USE_LOCATION_FLAG;
        yf = 0;
        zf = 0;
        volumef = volume;
        pitchf = pitch;
    }


    @Override
    public void readPacketData(PacketReadBuffer buf) throws IOException {
        namef = buf.readString();
        xf = buf.readFloat();
        yf = buf.readFloat();
        zf = buf.readFloat();
        volumef = buf.readFloat();
        pitchf = buf.readFloat();
    }

    @Override
    public void writePacketData(PacketWriteBuffer buf) throws IOException {
        buf.writeString(namef);
        buf.writeFloat(xf);
        buf.writeFloat(yf);
        buf.writeFloat(zf);
        buf.writeFloat(volumef);
        buf.writeFloat(pitchf);
    }

    @Override
    public void processPacketOnClient() {
        if(xf == DO_NOT_USE_LOCATION_FLAG){
            AudioUtils.clientPlaySound(namef, volumef, pitchf);
        }else{
            AudioUtils.clientPlaySound(namef, xf, yf, zf, volumef, pitchf);
        }
    }

    @Override
    public void processPacketOnServer(PlayerState sender) {

    }
}
