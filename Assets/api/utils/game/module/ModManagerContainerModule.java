package api.utils.game.module;

import api.mod.StarMod;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import api.network.packets.PacketUtil;
import api.utils.particle.ModParticleUtil;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.graphicsengine.core.Timer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Jake on 12/9/2020.
 * <insert description here>
 */
public abstract class ModManagerContainerModule implements PowerConsumer, ByteArrayTagSerializable {
    private float powered;
    public Long2ByteOpenHashMap blocks = new Long2ByteOpenHashMap();
    public SegmentController segmentController;
    private final ManagerContainer<?> managerContainer;
    private StarMod mod;
    private short blockId;
    private boolean onServer;

    public abstract void handle(Timer timer);

    public final boolean isOnServer() {
        return onServer;
    }


    public ModManagerContainerModule(SegmentController ship, ManagerContainer<?> managerContainer, StarMod mod, short blockId) {
        this.segmentController = ship;
        this.managerContainer = managerContainer;
        this.mod = mod;
        this.blockId = blockId;
        this.onServer = managerContainer.isOnServer();
    }

    public final short getBlockId() {
        return blockId;
    }

    public final ManagerContainer<?> getManagerContainer() {
        return managerContainer;
    }

    public final int getSize() {
        return blocks.size();
    }

    public void handlePlace(long abs, byte orientation) {
        long var4 = ElementCollection.getPosIndexFrom4(abs);
        System.err.println("Placed: " + abs + ", onserver =" + onServer);
        this.blocks.put(var4, orientation);
    }

    public void handleRemove(long abs) {
        long var3 = ElementCollection.getPosIndexFrom4(abs);
        System.err.println("Removed: " + abs);
        this.blocks.remove(var3);
    }


    @Override
    public boolean isPowerCharging(long l) {
        return true;
    }

    @Override
    public void setPowered(float v) {
        this.powered = v;
    }

    @Override
    public float getPowered() {
        return powered;
    }

    @Override
    public PowerConsumerCategory getPowerConsumerCategory() {
        return PowerConsumerCategory.OTHERS;
    }

    @Override
    public void reloadFromReactor(double v, Timer timer, float v1, boolean b, float v2) {

    }

    @Override
    public boolean isPowerConsumerActive() {
        return true;
    }


    @Override
    public void dischargeFully() {

    }

    public final String getTagName() {
        return mod.getSkeleton().getName() + "~" + getName();
    }

    public final StarMod getMod() {
        return mod;
    }

    //==== SIDING

    /**
     * Standard method for when the client needs to send info to the server
     *
     * @throws IOException
     */
    private final ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

    public final PacketWriteBuffer openCSBuffer() throws IOException {
        return new PacketWriteBuffer(new DataOutputStream(byteBuffer));
    }
    public final void sendBufferToServer(){
        //Convert our written data to a byte array and send it to the server
        PacketCSSendMCModuleData packet = new PacketCSSendMCModuleData(this, byteBuffer.toByteArray());
        PacketUtil.sendPacketToServer(packet);
        byteBuffer.reset();
    }

    /**
     * Standard method for when the server receives data from the client
     *
     * @throws IOException
     */
    public void onReceiveDataServer(PacketReadBuffer buffer) throws IOException {

    }


    /**
     * Write data of this module to a buffer and send it to the client
     */
    public final void syncToClient(PlayerState state) {
        PacketUtil.sendPacket(state, new PacketSCSyncMCModule(this, false));
    }

    /**
     * Write data of this module to a buffer and send it to nearby clients
     *
     */
    public final void syncToNearbyClients() {
        if(!this.onServer) throw new IllegalStateException("Method cannot be called from client");
        ArrayList<PlayerState> playersInRange = ModParticleUtil.getPlayersInRange(
                this.managerContainer.getSegmentController().getSector(new Vector3i()));
        for (PlayerState player : playersInRange) {
            syncToClient(player);
        }
    }


}
