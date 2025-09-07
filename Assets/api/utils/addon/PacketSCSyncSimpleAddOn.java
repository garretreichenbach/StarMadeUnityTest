package api.utils.addon;

import api.common.GameCommon;
import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import api.utils.game.SegmentControllerUtils;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.elements.SingleModuleActivation;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.network.objects.Sendable;

import java.io.IOException;

/**
 * Packet [Server -> Client]
 * Updates a simple add on
 */
public class PacketSCSyncSimpleAddOn extends Packet {
    /**
     * Default constructor, the data will be constructed from the PacketReadBuffer when readPacketData is called
     */
    public PacketSCSyncSimpleAddOn() {

    }

    /**
     * Constructor to set the data that will be written to the network
     */
    public PacketSCSyncSimpleAddOn(Sendable entity, SimpleAddOn addOn, float charge, int charges, boolean autoCharge) {
        this.entityId = entity.getId();
        this.usableId = addOn.getUsableId();
        this.charge = charge;
        this.charges = charges;
        this.autoCharge = autoCharge;
        this.activation = (addOn.activation!=null)? addOn.activation.startTime : -1;
    }

    private int entityId;
    private long usableId;
    private float charge;
    private int charges;
    private boolean autoCharge;
    private long activation;
    /**
     * Read data from a packet buffer (MUST BE IN THE SAME ORDER AS writePacketData)
     */
    @Override
    public void readPacketData(PacketReadBuffer buf) throws IOException {
        entityId = buf.readInt();
        usableId = buf.readLong();
        charge = buf.readFloat();
        charges = buf.readInt();
        autoCharge = buf.readBoolean();
        activation = buf.readLong();
    }

    @Override
    public void writePacketData(PacketWriteBuffer buf) throws IOException {
        buf.writeInt(entityId);
        buf.writeLong(usableId);
        buf.writeFloat(charge);
        buf.writeInt(charges);
        buf.writeBoolean(autoCharge);
        buf.writeLong(activation);
    }
    //When the client receives the packet
    @Override
    public void processPacketOnClient() {
        Sendable s = GameCommon.getGameObject(entityId);
        assert s instanceof ManagedSegmentController<?> : "Packet to update SimpleAddOn did not happen on a MUSC<?>";
        ManagedSegmentController<?> ship = (ManagedSegmentController<?>) s;
        PlayerUsableInterface addon = SegmentControllerUtils.getAddon(ship, usableId);
        assert addon instanceof SimpleAddOn : "SimpleAddOn update packet did not point to SimpleAddOn";
        SimpleAddOn on = (SimpleAddOn) addon;
        //
        on.setCharge(charge);
        on.setCharges(charges);
        on.setAutoChargeOn(autoCharge);
        if (activation == -1) //only write the activation if its actually activated :brain:
                return;
        if (on.activation == null)
            on.activation = new SingleModuleActivation();
        on.activation.startTime = activation;

        //
//        ModPlayground.broadcastMessage(((ManagedUsableSegmentController<?>) s).getName() + ", " + addon.getName() + ", " + charge + ", " + charges + ", " + autoCharge);
    }
    //When the server receives the packet
    @Override
    public void processPacketOnServer(PlayerState playerState) {

    }
}
