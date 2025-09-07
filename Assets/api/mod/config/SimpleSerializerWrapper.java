package api.mod.config;

import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;

/**
 * Created by Jake on 10/7/2021.
 * <insert description here>
 */
public abstract class SimpleSerializerWrapper {
    public byte[] rawData;
    public abstract void onDeserialize(PacketReadBuffer buffer);
    public abstract void onSerialize(PacketWriteBuffer buffer);
}
