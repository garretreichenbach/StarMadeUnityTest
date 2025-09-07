package org.schema.game.common.controller.elements.jumpdrive;

import org.schema.common.FastMath;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelpManager;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelperContainer.Hos;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.elements.BlockMetaDataDummy;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.UsableControllableElementManager;
import org.schema.game.common.controller.elements.behavior.interfaces.ManagedActivationInterface;
import org.schema.game.common.controller.elements.behavior.interfaces.ManagedChargingInterface;
import org.schema.game.common.controller.elements.behavior.interfaces.ManagedCooldownInterface;
import org.schema.game.common.controller.elements.behavior.managers.activation.ActivationManager;
import org.schema.game.common.controller.elements.behavior.managers.charging.ChargeManager;
import org.schema.game.common.controller.elements.behavior.managers.reload.CooldownManager;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.network.objects.remote.RemoteValueUpdate;
import org.schema.game.network.objects.valueUpdate.JumpChargeValueUpdate;
import org.schema.game.network.objects.valueUpdate.NTValueUpdateInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.ContextFilter;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.schema.schine.sound.controller.AudioController;

import static java.lang.Math.min;
import static org.schema.game.common.controller.elements.jumpdrive.JumpDriveElementManager.*;
import static org.schema.schine.graphicsengine.movie.craterstudio.math.EasyMath.lerp;

public class JumpDriveCollectionManager extends ControlBlockElementCollectionManager<JumpDriveUnit, JumpDriveCollectionManager, JumpDriveElementManager> implements PlayerUsableInterface, PowerConsumer,
		ManagedChargingInterface, ManagedActivationInterface, ManagedCooldownInterface {

	private float initialCharge;
	private long lastSentMessage;
	private long lastSentZero;
	private short lastCharge;

	private boolean autoCharging = false;
	private long lastSentAutochargeUpdateToClients = 0;
	private static final long AUTO_UPDATE_RATE_MS = 250;

	private float powered = 0;
	private boolean manualCharging = false;

	private final ActivationManager activation;
	private final ChargeManager charging;
	private final CooldownManager cooldown;

	public JumpDriveCollectionManager(SegmentPiece element,
	                                  SegmentController segController, JumpDriveElementManager em) {
		super(element, ElementKeyMap.JUMP_DRIVE_MODULE, segController, em);
		activation = new ActivationManager(this,false){
			@Override
			public void drawReloads(Vector3i iconPos, Vector3i iconSize, InputState state, long currentTime) {
				if(isActive()) UsableControllableElementManager.drawReload(state, iconPos, iconSize, ifc.getActiveIndicatorColor(), true, 1.0f);
				//Always draw 100%, as activation timeout value is just a backup to avoid unusable jump drive
			}
		};
		charging = new ChargeManager(this,isOnServer()){
			@Override
			public float getCharge() {
				return getContainer().floatValueMap.get(ElementCollection.getIndex(getControllerPos()));
			}

			@Override
			public void setCharge(float charge) {
				getContainer().floatValueMap.put(ElementCollection.getIndex(getControllerPos()), charge);
			}
		};
		cooldown = new CooldownManager(this,isOnServer());
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.ControlBlockElementCollectionManager#applyMetaData(org.schema.game.common.controller.elements.BlockMetaDataDummy)
	 */
	@Override
	protected void applyMetaData(BlockMetaDataDummy dummy) {
		assert (initialCharge == 0);
		initialCharge = ((JumpDriveMetaDataDummy) dummy).charge;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.ControlBlockElementCollectionManager#updateStructure(long)
	 */

	@Override
	public void updateStructure(long time) {
//		if(getSegmentController().isOnServer()){
//			System.err.println(getSegmentController().isOnServer()+" ::: "+getSegmentController()+": "+charge);
//		}
		
		if (getSegmentController().isOnServer() && initialCharge > 0) {
			LongOpenHashSet longOpenHashSet = getSegmentController().getControlElementMap().getControllingMap().getAll().get(ElementCollection.getIndex(getControllerPos()));
			if (longOpenHashSet != null && longOpenHashSet.size() <= getTotalSize()) {
				charging.setCharge(initialCharge);
				initialCharge = 0;
				sendChargeUpdate();
			}
		}
		super.updateStructure(time);
	}

	@Override
	protected Tag toTagStructurePriv() {
		return new Tag(Type.FLOAT, null, charging.getCharge());
	} //TODO old. Needs multicharge support

	@Override
	public boolean needsUpdate(){
		return !charging.fullyCharged() || powered < 0.999f; //if underpowered, may need to discharge
	}

	@Override
	protected Class<JumpDriveUnit> getType() {
		return JumpDriveUnit.class;
	}

	@Override
	public void handleKeyPress(ControllerStateInterface unit, Timer timer){
		getElementManager().handle(unit, timer);
	}

	@Override
	public JumpDriveUnit getInstance() {
		return new JumpDriveUnit();
	}

	@Override
	protected void onChangedCollection() {
		if (!getSegmentController().isOnServer()) {
			((GameClientState) getSegmentController().getState()).getWorldDrawer().getGuiDrawer()
					.managerChanged(this);
		}
		if (getSegmentController().isOnServer()) {
			charging.resetAllCharges();
		}
	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		float mass = getSegmentController().getMassWithDocks();
		int rLevel = getPowerInterface().getActiveReactor().getLevel();
		String extratext = "";
		if(getTotalSize() > getMaxUsableSize()) extratext = Lng.str(" (%s extra)", getTotalSize() - getMaxUsableSize());

		return new GUIKeyValueEntry[]{
				new ModuleValueEntry(Lng.str("Base charge requirement"), CHARGE_NEEDED_FOR_JUMP_FIX),
				new ModuleValueEntry(Lng.str("Charge needed per mass"), CHARGE_NEEDED_FOR_JUMP_PER_MASS_LINEAR + " x " + mass + " = " + CHARGE_NEEDED_FOR_JUMP_PER_MASS_LINEAR * mass),
				new ModuleValueEntry(Lng.str("Charge needed per reactor level"), CHARGE_NEEDED_FOR_JUMP_PER_REACTOR_LEVEL + " x " + rLevel + " = " + CHARGE_NEEDED_FOR_JUMP_PER_REACTOR_LEVEL * rLevel),
				new ModuleValueEntry(Lng.str("Total Charge Needed To Jump"), getChargeNeededForJump()),

				new ModuleValueEntry(Lng.str("Power Drain for Jump System Upkeep (e/sec) "), getUpkeepPowerUsage()),
				new ModuleValueEntry(Lng.str("Extra Power Drain for charging (e/sec) "), getExtraChargingPowerUsage()),
				new ModuleValueEntry(Lng.str("Drive Charge Speed (e/sec) "), getChargeAddedPerSec()),
				new ModuleValueEntry(Lng.str("Charging efficiency "), ((Math.ceil(getChargeAddedPerSec()/getPowerConsumedPerSecondCharging()) * 10000)/100 + '%')), //2 decimal places
				new ModuleValueEntry(Lng.str("Total Power Consumed for Charge (e/sec) "), getPowerConsumedPerSecondCharging()),

				new ModuleValueEntry(Lng.str("Minimum possible jump charge time (for any drive) (sec) "), getMinimumChargeTime()),
				new ModuleValueEntry(Lng.str("Maximum possible jump charge rate for this structure (e/sec) "), getChargeNeededForJump()/getMinimumChargeTime()),
				new ModuleValueEntry(Lng.str("Module Blocks / Maximum Module Blocks Usable "), Lng.str("%s / %s blocks", getTotalSize(),  getMaxUsableSize()) + extratext),
				new ModuleValueEntry(Lng.str("Charge Time (sec) "), getChargeNeededForJump()/getChargeAddedPerSec())
		};
	}

	private float getMinimumChargeTime() {
		return getConfigManager().apply(StatusEffectType.JUMP_CHARGE_TIME, MINIMUM_CHARGE_TIME_SEC);
	}

	@Override
	public String getModuleName() {
		return Lng.str("Jump Drive System");
	}

	public void attemptJump() {
		if (getSegmentController().isOnServer()) {
			if (charging.getChargesCount() > 0) { //charge available to use. Will be consumed at end of jump
				if (getSegmentController().engageJump(FastMath.fastFloor(getConfigManager().apply(StatusEffectType.JUMP_DISTANCE,(float)BASE_DISTANCE_SECTORS)))) {
					activation.setActive(true);
				} //TODO else error message
			} else {
				if (System.currentTimeMillis() - lastSentMessage > 3000) {
					getSegmentController().sendControllingPlayersServerMessage(Lng.astr("Cannot jump!\nDrive not charged.\n(%s/%s)",  StringTools.formatPointZero(charging.getCharge()),  StringTools.formatPointZero(getChargeNeededForJump())), ServerMessage.MESSAGE_TYPE_ERROR);
					lastSentMessage = System.currentTimeMillis();
				}
			}
		} //clients will receive a state update automatically anyway, if jump is successful
	}

	public float getChargeNeededForJump() {
		return CHARGE_NEEDED_FOR_JUMP_FIX + (CHARGE_NEEDED_FOR_JUMP_PER_REACTOR_LEVEL * getActiveReactorLevel()) + (CHARGE_NEEDED_FOR_JUMP_PER_MASS_LINEAR * getSegmentController().getMassWithDocks());
	}

	private float getActiveReactorLevel() {
		if(!getSegmentController().hasActiveReactors()) return 0;
		else return getPowerInterface().getActiveReactor().getLevel();
	}

	@Override
	public void update(Timer timer){
		super.update(timer);
		if(!cooldown.isCoolingDown(timer)) {
			if (isActive()){
				activation.update(timer);
			}
			else charging.update(timer);
		}
	}

	private double getPowerConsumedTotal() {
		return ((hasChargeInput() && !charging.fullyCharged()) || isActive()) ? getPowerConsumedPerSecondCharging() : getPowerConsumedPerSecondResting();
	}

	@Override
	public ChargeManager getChargeManager() {
		return charging;
	}

	@Override
	public boolean canCharge(long currentTime) {
		return !cooldown.isCoolingDown(currentTime) && !activation.isActive() && hasChargeInput();
	}

	public boolean hasChargeInput(){
		return manualCharging || autoCharging;
	}

	@Override
	public float getChargeAddedPerSecond() {
		float upkeepFrac = (float) (getUpkeepPowerUsage()/getPowerConsumedTotal());
		if(upkeepFrac <= 0.9999) return getChargeAddedPerSec() * min(1,((getPowered() - upkeepFrac)/(1-upkeepFrac))); //charge power frac = 1 - upkeep
		else return 0; //avoid confusing divide-by-zero error with strange config values
	}

	@Override
	public float getDechargePerSecond() {
		float providedPower = getPowered() * (float)getPowerConsumedTotal();
		if(providedPower < getUpkeepPowerUsage()){
			return lerp(getChargeAddedPerSec(),0,providedPower/getUpkeepPowerUsage());
		}
		else return 0;
	}

	@Override
	public float getMaxCharge() {
		return getChargeNeededForJump();
	}

	public void sendChargeUpdate() {
		JumpChargeValueUpdate chargeValueUpdate = new JumpChargeValueUpdate();
		chargeValueUpdate.setServer(((ManagedSegmentController<?>) getSegmentController()).getManagerContainer(), getControllerElement().getAbsoluteIndex());
		((NTValueUpdateInterface) getSegmentController().getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(chargeValueUpdate, getSegmentController().isOnServer()));
	}

	public void onHit(double damage, DamageDealerType damageType) {
		if (getSegmentController().isOnServer() && (getChargeManager().getCharge() > 0 || getChargeManager().getChargesCount() > 0)) {
			if (System.currentTimeMillis() - lastSentZero > 5000) {
				charging.resetAllCharges();
				lastSentZero = System.currentTimeMillis();
			}
		}
	}
	
	@Override
	public float getSensorValue(SegmentPiece connected){
		if(charging.getChargesCount() > 0) return 1f;
		return min(1f, charging.getCharge() / Math.max(0.0001f, (getChargeNeededForJump())));
	}

	@Override
	public void addHudConext(ControllerStateUnit unit, HudContextHelpManager h, Hos hos) {
		h.addHelper(KeyboardMappings.SHIP_PRIMARY_FIRE, Lng.str("Charge"), hos, ContextFilter.IMPORTANT);
		h.addHelper(KeyboardMappings.SHIP_ZOOM, Lng.str("Jump"), hos, ContextFilter.IMPORTANT);
	}

	private int getTotalUsableSize(){
		return min(getTotalSize(), getMaxUsableSize());
	}

	private int getMaxUsableSize() {
		return FastMath.fastFloor((((1/getMinimumChargeTime()) * getChargeNeededForJump()) - BASE_CHARGE_RATE_SEC)/CHARGE_ADDED_PER_SECOND_PER_BLOCK);
	}

	public float getChargeAddedPerSec() {
		return CHARGE_ADDED_PER_SECOND_PER_BLOCK + (CHARGE_ADDED_PER_SECOND_PER_BLOCK * getTotalUsableSize());
	}

	private float getExtraChargingPowerUsage(){
		return CHARGING_POWER_CONSUMPTION_BASE + (getTotalSize() * CHARGING_POWER_CONSUMPTION_PER_BLOCK); //extra is used even if it can't actually put that much power into the drive
	}

	private float getUpkeepPowerUsage() {
		return RESTING_POWER_CONSUMPTION_BASE + (getTotalSize() * RESTING_POWER_CONSUMPTION_PER_BLOCK);
	}

	@Override
	public double getPowerConsumedPerSecondResting() {
		return getConfigManager().apply(StatusEffectType.JUMP_POWER_TOPOFF_RATE,getUpkeepPowerUsage());
	}

	@Override
	public double getPowerConsumedPerSecondCharging() {
		return getPowerConsumedPerSecondResting() + getConfigManager().apply(StatusEffectType.JUMP_POWER_CHARGE_RATE,getChargeAddedPerSec() + getExtraChargingPowerUsage());
	}

	@Override
	public boolean isPowerCharging(long curTime) {
		return canCharge(curTime);
	}

	@Override
	public void setPowered(float powered) {
		this.powered = powered;
	}

	@Override
	public float getPowered() {
		return this.powered;
	}

	@Override
	public PowerConsumer.PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.JUMP_DRIVE;
	}

	@Override
	public void reloadFromReactor(double secTime, Timer timer, float tickTime, boolean powerCharging, float poweredResting) {

	}

	@Override
	public boolean isPowerConsumerActive() {
		return true;
	}

	@Override
	public String getName() {
		return "Jump Drive";
	}

	@Override
	public void dischargeFully() {
		charging.resetAllCharges();
		sendChargeUpdate();
		lastSentZero = System.currentTimeMillis();
	}

	@Override
	public void onFullCharge() {
		getSegmentController().popupOwnClientMessage(Lng.str("Jump Drive Charged!\nRight click to jump!"), ServerMessage.MESSAGE_TYPE_INFO);
	}

	public void handleManualChargeStatus(boolean v) {
		if(!v && manualCharging) {//i.e. if button just released, but this method hasn't run yet
			//if(getConfigManager().apply(StatusEffectType.JUMP_AUTO_CHARGE, false)) { //TODO uncomment conditional if we want to keep auto-charge as a chamber. (probably not; it is an accessibility hazard and does not meaningfully affect balance)
				setAutoCharging(!autoCharging); //toggle autocharge on mouse up ("was charging, but mouse is up" state)
				//sendChargeUpdate();
			//} else setAutoCharging(false); //TODO see above
		}

		setManualCharging(v);
	}

	public void setManualCharging(boolean v) {
		manualCharging = v;
	}

	@Override
	public void onLogicActivate(SegmentPiece selfBlock, boolean oldActive, Timer timer) {
		super.onLogicActivate(selfBlock, oldActive, timer);
		boolean activation = selfBlock.isActive();
		if(isOnServer()) {
			if(charging.getChargesCount() > 0){
				if(activation) getActivationManager().setActive(true,timer);
			}
			else {
				setAutoCharging(activation);
				sendChargeUpdate();
			}
		}
	}

	public void setAutoCharging(boolean v) {
		autoCharging = v;
		//System.err.println((isOnServer()? "[SERVER]" : "[CLIENT]") + "[JUMP DRIVE] AUTO CHARGE: " + v);
	}

	public boolean isAutoCharging() {
		return autoCharging;
	}

	public boolean isManualCharging() {
		return manualCharging;
	}

	@Override
	public ActivationManager getActivationManager() {
		return activation;
	}

	@Override
	public long getActivationTimeMs() {
		return 30000; //Backup. Should be handled by jump sequence
	}

	@Override
	public void sendActiveStateUpdate() {
		//no action; this doesn't need to be synchronized, as activation is already synched as an input and the deactivation timer is just a backup (precision unimportant)
	}

	@Override
	public void onActivate() {
		attemptJump();
		if(!isOnServer()) AudioController.fireAudioEvent("0022_spaceship user - turbo boost large", AudioController.ent(getSegmentController(), getControllerElement(), getControllerIndex(), 500)); //todo radius based on ship largest dimension
	}

	@Override
	public void onDeactivate() {
		if(!isOnServer()) AudioController.fireAudioEvent("0022_item - forcefield powerdown", AudioController.ent(getSegmentController(), getControllerElement(), getControllerIndex(), 500));
		cooldown.startCooldown(System.currentTimeMillis());
	}

	@Override
	public void onDurationEnd() {
		if(!isOnServer()) getSegmentController().sendClientMessage(Lng.str("[WARNING] Jump Drive activation timed out without server update.\nYou may be experiencing extreme server latency, or an error may have occurred."), ServerMessage.MESSAGE_TYPE_ERROR);
		//should not be reachable. Jump sequence should end before 30s
	}

	@Override
	public CooldownManager getCooldownManager() {
		return cooldown;
	}

	@Override
	public long getCooldownDurationMs() {
		return RELOAD_AFTER_USE_MS;
	}

	public boolean hasChargeAvailable() {
		return getChargeManager().getChargesCount() > 0;
	}

	@Override
	public int getMaxCharges() {
		return getConfigManager().apply(StatusEffectType.JUMP_MULTI_CHARGE_COUNT,1);
	}
}
