package api.network;

import api.DebugFile;
import api.common.GameCommon;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.objects.Sendable;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PacketReadBuffer {
    private DataInputStream in;

    public PacketReadBuffer(DataInputStream in) {
        this.in = in;
    }

    public byte[] toByteArray() throws IOException {
        return IOUtils.toByteArray(in);
    }

    public int readInt() throws IOException {
                return in.readInt();
    }
    public int available() throws IOException {
        return in.available();
    }

    public String readString() throws IOException {
        return in.readUTF();
    }

    public double readDouble() throws IOException {
        return in.readDouble();
    }

    public float readFloat() throws IOException {
        return in.readFloat();
    }
    public long readLong() throws IOException {
        return in.readLong();
    }

    public byte readByte() throws IOException {
        return in.readByte();
    }

    public short readShort() throws IOException {
        return in.readShort();
    }
    public boolean readBoolean() throws IOException {
        return in.readBoolean();
    }

    public Vector3i readVector() throws IOException {
        return new Vector3i(readInt(), readInt(), readInt());
    }
    public Vector3f readVector3f() throws IOException {
        return new Vector3f(readFloat(), readFloat(), readFloat());
    }
    public Vector4f readVector4f() throws IOException {
        return new Vector4f(readFloat(), readFloat(), readFloat(), readFloat());
    }
    public ArrayList<String> readStringList() throws IOException {
        int size = readInt();
        ArrayList<String> r = new ArrayList<String>(size);
        for (int i = 0; i < size; i++) {
            r.add(readString());
        }
        return r;
    }

    public ArrayList<Integer> readIntList() throws IOException {
        int size = readInt();
        ArrayList<Integer> r = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            r.add(readInt());
        }
        return r;
    }

    public ArrayList<Long> readLongList() throws IOException {
        int size = readInt();
        ArrayList<Long> r = new ArrayList<Long>(size);
        for (int i = 0; i < size; i++) {
            r.add(readLong());
        }
        return r;
    }

    /**
     * Gets a Sendable from the stream but doesn't allow specifying sides
     */
    public Sendable readSendable() throws IOException {
        int id = readInt();
        return GameCommon.getGameObject(id);
    }

    /**
     * Reads a sendable from the data stream.
     *
     * @param server True = Return server sendable, False = Return client sendable
     */
    public Sendable readSendable(boolean server) throws IOException {
        int id = readInt();
        if(server){
            return GameServerState.instance.getLocalAndRemoteObjectContainer().getLocalObjects().get(id);
        }else{
            return GameClientState.instance.getLocalAndRemoteObjectContainer().getLocalObjects().get(id);
        }
    }

    /**
     * Reads an object from the stream by transforming it from a String using GSON. It is recommanded to look up a GSON
     * guide to see what can and can't be serialized.
     * @throws IOException
     */
    public <T> T readObject(Class<T> objectClass) throws IOException {
        try {
            Gson gson = new Gson();
            return gson.fromJson(readString(), objectClass);
        } catch(Exception e) {
            e.printStackTrace();
            DebugFile.err("Attempted to deserialize " + objectClass.getName() + " but failed!");
            return null;
        }
    }

    public Vector2f readVector2f() throws IOException {
        return new Vector2f(readFloat(), readFloat());
    }

    public byte[] readByteArray() throws IOException {
        byte[] bytes = new byte[readInt()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = readByte();
        }
        return bytes;
    }

    public static PacketReadBuffer fromByteArray(byte[] arr){
        return new PacketReadBuffer(new DataInputStream(new ByteArrayInputStream(arr)));
    }
}
