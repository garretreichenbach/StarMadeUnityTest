package api.utils.addon;

import api.listener.events.systems.ReactorRecalibrateEvent;
import api.mod.StarMod;
import api.network.packets.PacketUtil;
import api.utils.PlayerUsableHelper;
import api.utils.game.SegmentControllerUtils;
import org.schema.common.util.StringTools;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.RecharchableActivatableDurationSingleModule;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.network.objects.valueUpdate.ServerValueRequestUpdate;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.Timer;

import java.util.ArrayList;

public abstract class SimpleAddOn extends RecharchableActivatableDurationSingleModule {
    public short blockId;
    public long usableId;

    public SimpleAddOn(ManagerContainer<?> var1, short blockId, StarMod container, String uidName) {
        super(var1);
        this.blockId = blockId;
        // specify an id
        this.usableId = PlayerUsableHelper.getPlayerUsableId(blockId, container, uidName);
    }
    /**
     * Side: Common
     * When the reactor of a ship (the ship this is attached to) is recalibrated
     * */
    public void onReactorRecalibrate(ReactorRecalibrateEvent event){

    }

    /**
     * Side: Do-Not-Override
     * Sync's the add-on with the client, from server
     * */
    public void sendChargeUpdate() {
        if (this.isOnServer()) {
            PacketSCSyncSimpleAddOn packet = new PacketSCSyncSimpleAddOn(this.getSegmentController(), this, getCharge(), getCharges(), isAutoChargeOn());
            for (PlayerState player : GameServerState.instance.getPlayerStatesByName().values()) {
                //TODO filter out players that arent close, make sure every player that MIGHT ever enter the ship gets synched?
                //if a sc gets loaded in for a player, while it was already loaded on the server, are the addons correclty synched by default?
                PacketUtil.sendPacket(player, packet);
            }
        }
    }
    /**
     * Gets all players attached to thing
     */
    public ArrayList<PlayerState> getAttachedPlayers(){
        return SegmentControllerUtils.getAttachedPlayers(this.getSegmentController());
    }

    /**
     * Side: Common
     * If we should discharge the addon when it is hit.
     * */
    public boolean isDischargedOnHit() {
        return false;
    }

    /**
     * Side: ???
     * Displays error message when addon is not charged
     * */
    public void onChargedFullyNotAutocharged() {
        this.getSegmentController().popupOwnClientMessage("Addon not fully charged", 1);
    }


    /**
     * Side: Common, usually called on server
     * Discharge an addon
     * */
    public void dischargeToZero(){
        this.setCharge(0);
        this.setCharges(0);
        this.sendChargeUpdate();
    }

    /**
     * Side: Common
     * Charge time in seconds
     * */
    public abstract float getChargeRateFull();

    /**
     * Side: Common, Possibly never called?
     *
     * */
    public boolean canExecute() {
        return !this.isActive() && this.getCharge() >= 1;
    }

    /**
     * Side: Common
     * Consumed power per second when not charging
     * */
    public abstract double getPowerConsumedPerSecondResting();

    /**
     * Side: Common
     * Consumed power per second when charging
     * */
    public abstract double getPowerConsumedPerSecondCharging();

    /**
     * Side: Common
     * If the addon is autocharging
     * */
    public boolean isAutoCharging() {
        return true;
    }

    public boolean isAutoChargeToggable() {
        return true;
    }

    /**
     * Side: Do-Not-Override
     * */
    public long getUsableId() {
        return usableId;
    }
    /**
     * Side: Common
     * */
    public void chargingMessage() {
        this.getSegmentController().popupOwnClientMessage("Mod add-on charging", 1);
    }

    /**
     * Side: Common
     * */
    public void onCooldown(long var1) {
        this.getSegmentController().popupOwnClientMessage(StringTools.format("On cooldown: \n(%d secs)", new Object[]{var1}), 3);
    }

    /**
     * Side: Common
     * */
    public void onUnpowered() {
        this.getSegmentController().popupOwnClientMessage("Add-on Unpowered", 3);
    }

    /**
     * Side: Common
     * */
    public String getTagId() {
        return "RSCN";
    }

    /**
     * Side: Common
     * */
    public int updatePrio() {
        return 1;
    }

    /**
     * Side: Common
     * */
    public PowerConsumerCategory getPowerConsumerCategory(){
        return PowerConsumerCategory.OTHERS;
    }

    /**
     * Side: Common
     * */
    public boolean isPlayerUsable() {
        return true;
        //!((GameStateInterface)this.getSegmentController().getState()).getGameState().isModuleEnabledByDefault(this.getUsableId()) && !this.getConfigManager().apply(StatusEffectType.SCAN_SHORT_RANGE_SCANNER_ENABLE, false) ? false : super.isPlayerUsable();
    }

    /**
     * Side: Common
     * */
    public String getWeaponRowName(){
        return getName();
    }

    /**
     * Side: Common
     * */
    @Override
    public short getWeaponRowIcon() {
        return blockId;
    }

    /**
     * Side: Common
     * */
    public boolean isPowerConsumerActive() {
        return true;
    }

    /**
     * Side: Common
     * */
    public abstract float getDuration();// {
    //return this.getConfigManager().apply(StatusEffectType.SCAN_USAGE_TIME, VoidElementManager.SCAN_DURATION_BASIC);
    //}

    //    public float getActiveStrength() {
//        return this.isActive() ? this.getConfigManager().apply(StatusEffectType.SCAN_STRENGTH, VoidElementManager.SCAN_STRENGTH_BASIC) : 0.0F;
//    }
    /**
     * Deprecated, since asteroids are not MUSCs, it throws a CCE.
     */
    @Deprecated
    public ManagedUsableSegmentController<?> getManagerUsableSegmentController(){
        return (ManagedUsableSegmentController<?>) getContainer().getSegmentController();
    }
    public ManagedSegmentController<?> getManagedSegmentController(){
        return (ManagedSegmentController<?>) getContainer().getSegmentController();
    }

    /**
     * Side: Common
     * */
    @Override
    public boolean executeModule() {
        onAttemptToExecute();
        //Handle execute if on server
        boolean success = super.executeModule();
        if (success && this.isOnServer()) {
            onExecuteServer();
            dischargeToZero();
        }
        //Handle if executed on client
        boolean clientSuccess = this.getCharges() > 0 || this.getCharge() > 0.999;
        if (!isOnServer() && clientSuccess) {
            //Make the client think it activated it so it calls onActive.
            setActiveFromTag(true);
            //Will be updated by the server
            onExecuteClient();
            dischargeToZero();
        }

        return success;
    }

    /**
     * Side: Common
     * */
    public void onAttemptToExecute(){ }

    /**
     * Side: Server
     * */
    public abstract boolean onExecuteServer();

    /**
     * Side: Server
     * */
    public abstract boolean onExecuteClient();

    /**
     * Side: Common
     * */
    public void onDeactivateFromTime(){ }

    /**
     * Side: Common
     * */
    public void update(Timer var1) {
        boolean active = this.activation != null;
        super.update(var1);
        if (this.isActive()) {
            onActive();
        }else{
            onInactive();
        }
        if(active && this.activation == null){
            //Set charge to zero just in case
            this.onDeactivateFromTime();
            //Also sends charge update
            this.setAutoChargeOn(true);
            this.dischargeToZero();
        }
    }

    /**
     * Side: Common
     * */
    public abstract void onActive();

    /**
     * Side: Common
     * */
    public abstract void onInactive();

    public abstract String getName();

    /**
     * Side: Common
     * */
    protected ServerValueRequestUpdate.Type getServerRequestType() {
        return ServerValueRequestUpdate.Type.SCAN;
    }

    /**
     * Side: Common
     * */
    protected boolean isDeactivatableManually() {
        return false;
    }

    /**
     * Side: Common
     * */
    protected void onNoLongerConsumerActiveOrUsable(Timer var1) {

    }

    /**
     * Side: Common
     * */
    public String getExecuteVerb() {
        return "Execute";
    }
}
