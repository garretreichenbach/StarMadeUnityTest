package api.utils.game.module;

import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;

import java.io.IOException;

/**
 * Created by Jake on 12/10/2020.
 * Interface for serializing/deserializing data to a byte array
 */
public interface ByteArrayTagSerializable {
    void onTagSerialize(PacketWriteBuffer buffer) throws IOException;
    void onTagDeserialize(PacketReadBuffer buffer) throws IOException;
}
