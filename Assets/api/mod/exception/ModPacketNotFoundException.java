package api.mod.exception;

/**
 * Created by Jake on 10/27/2020.
 * <insert description here>
 */
public class ModPacketNotFoundException extends NullPointerException {
    public ModPacketNotFoundException(short packetId) {
        super("The packet ID: " + packetId + " could not be found in the packet lookup. " +
                "\nMake sure your packet is:" +
                "\n - registered in onEnable()" +
                "\n - The mod is on both client AND server");
    }
}
