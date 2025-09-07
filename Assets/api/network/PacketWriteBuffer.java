package api.network;

import api.DebugFile;
import com.google.gson.Gson;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.schine.network.objects.Sendable;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;

public class PacketWriteBuffer {
    private DataOutputStream out;

    public PacketWriteBuffer(DataOutputStream out){
        this.out = out;
    }

    public void writeInt(int i) throws IOException {
        out.writeInt(i);
    }
    public void writeString(String s) throws IOException {
        out.writeUTF(s);
    }
    public void writeDouble(double d) throws IOException {
        out.writeDouble(d);
    }
    public void writeByte(byte b) throws IOException {
        out.writeByte(b);
    }
    public void writeBoolean(boolean b) throws IOException {
        out.writeBoolean(b);
    }
    public void writeFloat(float charge) throws IOException {
        out.writeFloat(charge);
    }
    public void writeLong(long l) throws IOException {
        out.writeLong(l);
    }
    public void writeVector(Vector3i sec) throws IOException {
        out.writeInt(sec.x);
        out.writeInt(sec.y);
        out.writeInt(sec.z);
    }
    public void writeVector3f(Vector3f sec) throws IOException {
        out.writeFloat(sec.x);
        out.writeFloat(sec.y);
        out.writeFloat(sec.z);
    }
    public void writeVector4f(Vector4f v) throws IOException {
        out.writeFloat(v.x);
        out.writeFloat(v.y);
        out.writeFloat(v.z);
        out.writeFloat(v.w);
    }
    public void writeStringList(Collection<String> list) throws IOException {
        //Write list size
        writeInt(list.size());
        //Write entries
        for (String entry : list) {
            writeString(entry);
        }
    }

    public void writeIntList(Collection<Integer> list) throws IOException {
        //Write list size
        writeInt(list.size());
        //Write entries
        for (Integer entry : list) {
            writeInt(entry);
        }
    }

    /**
     * Compresses string before sending it across the network
     * @param s
     */
    public void writeCompressedString(String s){

    }

    public void writeLongList(Collection<Long> list) throws IOException {
        //Write list size
        writeInt(list.size());
        //Write entries
        for (Long entry : list) {
            writeLong(entry);
        }
    }

    public void writeSendable(Sendable sendable) throws IOException {
        writeInt(sendable.getId());
    }

    /**
     * Writes an object to the stream by transforming it into a String using GSON. It is recommanded to look up a GSON
     * guide to see what can and can't be serialized.
     * @param object The object to write
     * @throws IOException
     */
    public void writeObject(Object object) throws IOException {
        try {
            Gson gson = new Gson();
            writeString(gson.toJson(object));
        } catch(Exception e) {
            e.printStackTrace();
            DebugFile.err("Attempted to serialize " + object.getClass().getName() + " but failed!");
        }
    }

    public void writeShort(short moduleId) throws IOException {
        out.writeShort(moduleId);
    }

    public void writeVector2f(Vector2f size) throws IOException {
        out.writeFloat(size.x);
        out.writeFloat(size.y);
    }

    public void writeByteArray(byte[] buffer) throws IOException {
        out.writeInt(buffer.length);
        for (byte b : buffer) {
            out.writeByte(b);
        }
    }
}
