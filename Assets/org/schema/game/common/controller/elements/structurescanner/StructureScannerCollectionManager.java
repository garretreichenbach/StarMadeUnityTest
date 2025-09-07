package org.schema.game.common.controller.elements.structurescanner;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.schema.common.FastMath;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelpManager;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelperContainer;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.elements.BlockMetaDataDummy;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.behavior.interfaces.ManagedActivationInterface;
import org.schema.game.common.controller.elements.behavior.interfaces.ManagedChargingInterface;
import org.schema.game.common.controller.elements.behavior.interfaces.ManagedCooldownInterface;
import org.schema.game.common.controller.elements.behavior.managers.activation.ActivationManager;
import org.schema.game.common.controller.elements.behavior.managers.charging.ChargeManager;
import org.schema.game.common.controller.elements.behavior.managers.reload.CooldownManager;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer;
import org.schema.game.common.controller.elements.spacescanner.ScannerMetaDataDummy;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.network.objects.remote.RemoteValueUpdate;
import org.schema.game.network.objects.valueUpdate.NTValueUpdateInterface;
import org.schema.game.network.objects.valueUpdate.StructureScanActivationValueUpdate;
import org.schema.game.network.objects.valueUpdate.StructureScanChargeValueUpdate;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.ContextFilter;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import static java.lang.Math.max;

public class StructureScannerCollectionManager extends ControlBlockElementCollectionManager<StructureScannerUnit, StructureScannerCollectionManager, StructureScannerElementManager> implements PlayerUsableInterface, ManagedActivationInterface, ManagedChargingInterface, ManagedCooldownInterface, PowerConsumer {
	private float initialCharge;
	private long lastSent;
	private long lastSentZero;
	private boolean hideChargedMessage;
	private final ActivationManager activation;
	private final ChargeManager charging;
	private final CooldownManager cooldown;
	private float powered = 0;
	private long lastPowered = 0;

	public StructureScannerCollectionManager(SegmentPiece element,
											 SegmentController segController, StructureScannerElementManager em) {
		super(element, ElementKeyMap.INTELL_ANTENNA, segController, em);
		activation = new ActivationManager(this,isOnServer());
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
		initialCharge = ((ScannerMetaDataDummy) dummy).charge;
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
	}

	@Override
	protected Class<StructureScannerUnit> getType() {
		return StructureScannerUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return true;
	}

	@Override
	public StructureScannerUnit getInstance() {
		return new StructureScannerUnit();
	}

	@Override
	protected void onChangedCollection() {
		if (!getSegmentController().isOnServer()) {
			((GameClientState) getSegmentController().getState()).getWorldDrawer().getGuiDrawer()
					.managerChanged(this);
		}
		if (getSegmentController().isOnServer()) {
			resetCharge();
			setActive(false);
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.ElementCollectionManager#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer) {
		super.update(timer);
		if(!cooldown.isCoolingDown(timer)) {
			if (isActive()){
				activation.update(timer);
			}
			else charging.update(timer);
		}
	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		float power = 0;
		String dur = "[ERROR]";
		for (int i = 0; i < getElementCollections().size(); i++) {
			power += getElementCollections().get(i).getPowerConsumption();
		}
		int neededBlocks = (int) FastMath.ceil((float) ((double) getSegmentController().getTotalElements() * (double) StructureScannerElementManager.RATIO_NEEDED_TO_REACTOR_LVL_FOR_MAX));

		int sizeAfterRatio = max(0, getTotalSize() - neededBlocks);

		float bef = ((getTotalSize() - sizeAfterRatio) * StructureScannerElementManager.CHARGE_NEEDED_FOR_SCAN_PER_BLOCK);
		float aft = ((sizeAfterRatio) * StructureScannerElementManager.CHARGE_NEEDED_FOR_SCAN_PER_BLOCK_AFTER_RATIO);

		float d = getActivationTimeMs();
		if(d < 0) dur = Lng.str("unlimited");
		else dur = d + Lng.str(" sec");

		return new GUIKeyValueEntry[]{
				new ModuleValueEntry(Lng.str("Total Charge Needed"), getChargeNeededForScan()),
				new ModuleValueEntry(Lng.str("Charge needed per block"), StructureScannerElementManager.CHARGE_NEEDED_FOR_SCAN_PER_BLOCK + " x " + (getTotalSize() - sizeAfterRatio) + " = " + bef),
				new ModuleValueEntry(Lng.str("Scan Duration"), dur),
				new ModuleValueEntry(Lng.str("Charge needed per extra block over ratio"), StructureScannerElementManager.CHARGE_NEEDED_FOR_SCAN_PER_BLOCK_AFTER_RATIO + " x " + (sizeAfterRatio) + " = " + aft),
				new ModuleValueEntry(Lng.str("Power Usage for standby (/sec)"), getPowerConsumedPerSecondResting()),
				new ModuleValueEntry(Lng.str("Power Usage for charge (/sec)"), getPowerConsumedPerSecondCharging()),
				new ModuleValueEntry(Lng.str("Ratio of total blocks needed "), StructureScannerElementManager.RATIO_NEEDED_TO_REACTOR_LVL_FOR_MAX),
				new ModuleValueEntry(Lng.str("Module Blocks / Module Blocks Needed "), getTotalSize() + " / " + neededBlocks)};
	}

	@Override
	public String getModuleName() {
		return Lng.str("Structure Scan System");
	}

	public float getChargeTimeMs() {
		return getConfigManager().apply(StatusEffectType.SCAN_CHARGE_TIME,StructureScannerElementManager.CHARGE_TIME_MS + (StructureScannerElementManager.CHARGE_MS_ADDED_PER_BLOCK * getTotalSize()));
	}

	@Override
	public ChargeManager getChargeManager() {
		return charging;
	}

	@Override
	public boolean canCharge(long currentTime) {
		return !isActive() && !cooldown.isCoolingDown(currentTime) && getPowered() > 0; //charge automatically
	}

	@Override
	public float getChargeAddedPerSecond() {
		float t = getChargeTimeMs();
		if(t <= 0) return 1000; //1ms for now. TODO bypass entire charge module
		return getPowered() / (getChargeTimeMs()/1000); //for simplicity, charge to 1, so we use powered fraction as numerator //TODO MS/seconds, may need to change interface depending on how timer works
	}

	@Override
	public float getDechargePerSecond() {
		return getPowered() <= 0? 0.1f : 0;
	}

	@Override
	public float getMaxCharge() {
		return 1;
	}

	@Override
	public double getPowerConsumedPerSecondResting() {
		return getConfigManager().apply(StatusEffectType.SCAN_INACTIVE_RESTING_POWER_CONS_MULT,StructureScannerElementManager.CHARGE_NEEDED_FOR_SCAN_BASE);
	}

	@Override
	public double getPowerConsumedPerSecondCharging() {
		return getConfigManager().apply(StatusEffectType.SCAN_ACTIVE_RESTING_POWER_CONS_MULT,StructureScannerElementManager.CHARGE_NEEDED_FOR_SCAN_BASE + (StructureScannerElementManager.CHARGE_NEEDED_FOR_SCAN_PER_BLOCK * getTotalSize()));
	}

	@Override
	public boolean isPowerCharging(long curTime) {
		return !isCharged() || isActive();
	} //determines which power to use

	@Override
	public void setPowered(float v) {
		powered = v;
	}

	@Override
	public float getPowered() {
		return powered;
	}


	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.SCANNER;
	}

	@Override
	public void reloadFromReactor(double secTime, Timer timer, float tickTime, boolean powerCharging, float poweredResting) {
		if(powered > 0.05) lastPowered = timer.currentTime;
		else{
			if(timer.currentTime - lastPowered > 100) { //100ms grace period
				setActive(false);
				//discharge the system incrementally, 5% per second
				charging.setCharge(max(0,charging.getCharge() - ((getChargeTimeMs())*timer.getDelta()*(1-powered)))); //discharge at charge rate at 0% power
			}
		}
	}

	@Override
	public boolean isPowerConsumerActive() {
		return true;
	}

	@Override
	public void dischargeFully() {
		resetCharge();
	}

	public boolean isCharged() {
		return charging.fullyCharged();
	}

	public float getChargeNeededForScan() {
		return getSegmentController().getConfigManager().apply(StatusEffectType.SCAN_CHARGE_TIME, (StructureScannerElementManager.CHARGE_TIME_MS + (getTotalSize() * StructureScannerElementManager.CHARGE_MS_ADDED_PER_BLOCK)));
	}

	@Override
	public void handleKeyEvent(ControllerStateUnit unit, KeyboardMappings mapping, Timer timer) {
		super.handleKeyEvent(unit, mapping, timer);
		if(mapping == KeyboardMappings.SHIP_PRIMARY_FIRE) {
			getElementManager().handle(unit, timer);
		}
	}

	@Override
	public void handleKeyPress(ControllerStateInterface unit, Timer timer) {
		super.handleKeyPress(unit, timer);
	}

	public void resetCharge(){
		setActive(false);
		charging.resetCurrentCharge();
	}

	public void sendChargeUpdate() {
		StructureScanChargeValueUpdate valueUpdate = new StructureScanChargeValueUpdate();
		valueUpdate.setServer(((ManagedSegmentController<?>) getSegmentController()).getManagerContainer(), getControllerElement().getAbsoluteIndex());
		((NTValueUpdateInterface) getSegmentController().getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(valueUpdate, getSegmentController().isOnServer()));
	}

	public void hasHit(double damage, DamageDealerType damageType) {
		if (getSegmentController().isOnServer()) {
			if (charging.getCharge() > 0 && System.currentTimeMillis() - lastSentZero > 5000) {
				resetCharge();
				lastSentZero = System.currentTimeMillis();
			}
		}
	}
	@Override
	public float getSensorValue(SegmentPiece connected){
		if(charging.getChargesCount() > 0) return 1;
		return Math.min(1f, charging.getCharge() / max(0.0001f, (getChargeNeededForScan())));
	}

	public float getTotalActiveScanStrength() {
		if(!getSegmentController().hasActiveReactors() || !isActive()) return 0;

		float result = StructureScannerElementManager.SCAN_STRENGTH_BASE;
		for(StructureScannerUnit u : getElementCollections()){
			result += u.getScanStrength();
		}
		result = result * getPowered();
		float threshold = StructureScannerElementManager.RATIO_NEEDED_TO_REACTOR_LVL_FOR_MAX * getElementManager().getManagerContainer().getMainReactor().getPowerInterface().getActiveReactor().getLevel();
		//threshold of raw scan strength that would be needed to reach overall scan strength cap. functions as scaling basis
		result = Math.min(1,result/threshold) * StructureScannerElementManager.SCAN_STRENGTH_CAP;

		result = getConfigManager().apply(StatusEffectType.SCAN_STRENGTH,result);
		//TODO: I still dislike this math. Though stealth strength is a ratio, a hardcap on scan strength feels wrong.
		// If we do keep this system, maybe some systems should create 'interference' which raises the threshold and effectively makes it harder to reach the scan strength cap
		return result;
	}

	@Override
	public void addHudConext(ControllerStateUnit unit, HudContextHelpManager h, HudContextHelperContainer.Hos hos) {
		h.addHelper(KeyboardMappings.SHIP_PRIMARY_FIRE, Lng.str("Scan"), hos, ContextFilter.IMPORTANT);
	}

	public void setActive(boolean val) {
		activation.setActive(val);
	}

	/**
	 * Changes the activation state and time of activation. Should only be called for synchronization or other exceptional situations. Otherwise, the activation manager handles the time automatically.
	 * @param val
	 * @param timeOfChange
	 */
	public void setActiveState(boolean val, long timeOfChange) {
		activation.setActiveState(val,timeOfChange);
	}

	@Override
	public ActivationManager getActivationManager() {
		return activation;
	}

	@Override
	public long getActivationTimeMs() {
		return (long) getConfigManager().apply(StatusEffectType.SCAN_USAGE_TIME, StructureScannerElementManager.SCAN_DURATION_BASE_MS * 1000); //negative numbers are the magical value for infinite duration
	}

	@Override
	public void sendActiveStateUpdate() {
		StructureScanActivationValueUpdate vu = new StructureScanActivationValueUpdate();
		vu.setServer(((ManagedSegmentController<?>) getSegmentController()).getManagerContainer());
		((NTValueUpdateInterface) getSegmentController().getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(vu, getSegmentController().isOnServer()));
	}

	@Override
	public void onActivate() {
		//do nothing. Game tracks activity state to determine visibility
		//TODO audiovisual effects
	}

	@Override
	public void onDeactivate() {
		charging.resetCurrentCharge();
		cooldown.startCooldown(System.currentTimeMillis());
		//TODO audiovisual effects
	}

	@Override
	public void onDurationEnd() {
		//no distinct behaviour
		//TODO audiovisual effects
	}

	@Override
	public CooldownManager getCooldownManager() {
		return cooldown;
	}

	@Override
	public long getCooldownDurationMs() {
		return StructureScannerElementManager.RELOAD_AFTER_USE_MS;
	}
}
