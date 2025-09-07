package org.schema.game.common.controller.elements.spacescanner;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.schema.common.FastMath;
import org.schema.common.util.StringTools;
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
import org.schema.game.common.controller.elements.behavior.interfaces.ManagedChargingInterface;
import org.schema.game.common.controller.elements.behavior.interfaces.ManagedCooldownInterface;
import org.schema.game.common.controller.elements.behavior.managers.charging.ChargeManager;
import org.schema.game.common.controller.elements.behavior.managers.reload.CooldownManager;
import org.schema.game.common.controller.elements.effectblock.EffectElementManager.OffensiveEffects;
import org.schema.game.common.controller.elements.power.PowerAddOn;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.network.objects.remote.RemoteValueUpdate;
import org.schema.game.network.objects.valueUpdate.LongRangeScanChargeValueUpdate;
import org.schema.game.network.objects.valueUpdate.NTValueUpdateInterface;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.ContextFilter;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class LongRangeScannerCollectionManager extends ControlBlockElementCollectionManager<LongRangeScannerUnit, LongRangeScannerCollectionManager, LongRangeScannerElementManager> implements PlayerUsableInterface, PowerConsumer, ManagedCooldownInterface, ManagedChargingInterface {

	private float initialCharge;
	private boolean hideChargedMessage;
	private float powered;
	private final ChargeManager charging;
	private final CooldownManager cooldown;
	private long lastSentFeedbackMessage;

	public LongRangeScannerCollectionManager(SegmentPiece element,
											 SegmentController segController, LongRangeScannerElementManager em) {
		super(element, ElementKeyMap.SCANNER_MODULE, segController, em);

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
	public int getMargin() {
		return 0;
	}

	@Override
	protected Class<LongRangeScannerUnit> getType() {
		return LongRangeScannerUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return true;
	}

	@Override
	public LongRangeScannerUnit getInstance() {
		return new LongRangeScannerUnit();
	}

	@Override
	public void handleKeyPress(ControllerStateInterface unit, Timer timer){
		getElementManager().handle(unit, timer);
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

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.ElementCollectionManager#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer) {
		super.update(timer);

		if(!cooldown.isCoolingDown(timer))
			charging.update(timer);
	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		float dps = 0;
		float power = 0;
		for (int i = 0; i < getElementCollections().size(); i++) {
			power += getElementCollections().get(i).getPowerConsumption();
		}
		int neededBlocks = (int) FastMath.ceil((float) ((double) getSegmentController().getTotalElements() * (double) LongRangeScannerElementManager.RATIO_NEEDED_TO_REACTOR_LEVEL));

		int sizeAfterRatio = Math.max(0, getTotalSize() - neededBlocks);

		float bef = ((getTotalSize() - sizeAfterRatio) * LongRangeScannerElementManager.SCAN_CHARGING_POWER_CONSUMED_PER_BLOCK);
		float aft = ((sizeAfterRatio) * LongRangeScannerElementManager.SCAN_CHARGING_POWER_CONSUMED_PER_BLOCK_AFTER_RATIO);

		return new GUIKeyValueEntry[]{
				new ModuleValueEntry(Lng.str("Total Charge Needed"), getChargeNeededForScan()),
				new ModuleValueEntry(Lng.str("Charge needed per block"), LongRangeScannerElementManager.SCAN_CHARGING_POWER_CONSUMED_PER_BLOCK + " x " + (getTotalSize() - sizeAfterRatio) + " = " + bef),
				new ModuleValueEntry(Lng.str("Charge needed per extra block over ratio"), LongRangeScannerElementManager.SCAN_CHARGING_POWER_CONSUMED_PER_BLOCK_AFTER_RATIO + " x " + (sizeAfterRatio) + " = " + aft),
				new ModuleValueEntry(Lng.str("Power Usage for charge (/sec)"), getChargeAddedPerSec()),
				new ModuleValueEntry(Lng.str("Ratio of total blocks needed "), LongRangeScannerElementManager.RATIO_NEEDED_TO_REACTOR_LEVEL),
				new ModuleValueEntry(Lng.str("Module Blocks / Module Blocks Needed "), getTotalSize() + " / " + neededBlocks)};
	}

	@Override
	public String getModuleName() {
		return Lng.str("Scan System");
	}

	public float getChargeAddedPerSec() {
		return LongRangeScannerElementManager.BASE_CHARGE_TIME_MS + LongRangeScannerElementManager.CHARGE_TIME_ADDED_PER_SECOND_PER_BLOCK * getTotalSize();
	}

	public void scanOnServer(PlayerState player) {
		assert (getSegmentController().isOnServer());
		long time = System.currentTimeMillis();
		if (getSegmentController().isOnServer()) {
			if (isCharged() && !cooldown.isCoolingDown(time)) {
				charging.resetAllCharges(); //implicitly sends update
				getSegmentController().sendControllingPlayersServerMessage(Lng.astr("Scanning in system: %s...", player.getCurrentSystem()), ServerMessage.MESSAGE_TYPE_INFO);

				/*
				System.err.println("[SERVER][SCAN] finding scannable...");
				for (Sendable se : getSegmentController().getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
					if (se instanceof Ship) {
						Ship s = (Ship) se;						
						if (s.isNeighbor(getSegmentController().getSectorId(), s.getSectorId())) {
							boolean revealed = false;
							if ((s.isCloakedFor(null) || s.isJammingFor(null)) && s.getMass() <= (getTotalSize() * LongRangeScannerElementManager.ECM_STRENGTH_MOD)) {
								s.getManagerContainer().getStealthElementManager().stopStealth(StealthElementManager.REUSE_DELAY_ON_SCAN_MS);
								revealed = true;
							}

							if (revealed) {
								s.sendControllingPlayersServerMessage(Lng.astr("A scanner signal in\nthis sector has interrupted\nyour cloaking and jamming\nsystems!"), ServerMessage.MESSAGE_TYPE_ERROR);
								getSegmentController().sendControllingPlayersServerMessage(Lng.astr("Our scanners decloaked an\nentity:\n",  s.toNiceString()), ServerMessage.MESSAGE_TYPE_INFO);
							} else {
								if(s.isCloakedFor(null) || s.isJammingFor(null)) {
									getSegmentController().sendControllingPlayersServerMessage(Lng.astr("There is a cloaked/jamming\nentity in this sector\nbut our signal isn't strong\nenough to reveal it!"), ServerMessage.MESSAGE_TYPE_ERROR);
								}
							}

						}
					}
				}

				System.err.println("[SERVER][SCAN] finding scannables done...");
				 */ //All this functionality now belongs to the new Structure Scanner. A full de-stealth burst feature might be useful as a capital ship or station system, but should not be available to all ships.

				((GameServerState) getSegmentController().getState()).scanOnServer(player, getConfigManager().apply(StatusEffectType.SCAN_LONG_RANGE_DISTANCE, LongRangeScannerElementManager.DEFAULT_SCAN_DISTANCE));
				hideChargedMessage = false;
			} else {
				getSegmentController().sendControllingPlayersServerMessage(Lng.astr("Cannot Scan!\nscanner not charged\n(%s/%s)",  StringTools.formatPointZero(charging.getCharge()),  StringTools.formatPointZero(getChargeNeededForScan())), ServerMessage.MESSAGE_TYPE_ERROR);
				lastSentFeedbackMessage = time;
			}
		}
		getElementManager().setLastScan(time);
	}

	public boolean isCharged() {
		return charging.getChargesCount() > 0;
	}

	public float getChargeNeededForScan() {

		int neededBlocks = (int) FastMath.ceil((float) ((double) getSegmentController().getTotalElements() * (double) LongRangeScannerElementManager.RATIO_NEEDED_TO_REACTOR_LEVEL));

		int sizeAfterRatio = Math.max(0, getTotalSize() - neededBlocks);

		float bef = ((getTotalSize() - sizeAfterRatio) * LongRangeScannerElementManager.SCAN_CHARGING_POWER_CONSUMED_PER_BLOCK);
		float aft = ((sizeAfterRatio) * LongRangeScannerElementManager.SCAN_CHARGING_POWER_CONSUMED_PER_BLOCK_AFTER_RATIO);

		double extra = 0;
		if (((double) getTotalSize() / (double) getSegmentController().getTotalElements()) < LongRangeScannerElementManager.RATIO_NEEDED_TO_REACTOR_LEVEL) {
			extra = 1.0 - ((double) getTotalSize() / (double) getSegmentController().getTotalElements()) / LongRangeScannerElementManager.RATIO_NEEDED_TO_REACTOR_LEVEL;
		}

		float one = LongRangeScannerElementManager.SCAN_POWER_CONSUMPTION_FIXED + bef + aft;

		return (float) (one + (extra * one));
	}

	@Override
	public ChargeManager getChargeManager() {
		return charging;
	}

	@Override
	public boolean canCharge(long currentTime) {
		return powered > 0.999;
	}

	@Override
	public float getChargeAddedPerSecond() {
		return getChargeAddedPerSec();
	}

	@Override
	public float getDechargePerSecond() {
		return powered > 0.999 ? 0 : getChargeAddedPerSec() * (1-powered);
	}

	@Override
	public float getMaxCharge() {
		return getChargeNeededForScan();
	}

	public void sendChargeUpdate() {
		LongRangeScanChargeValueUpdate chargeValueUpdate = new LongRangeScanChargeValueUpdate();
		chargeValueUpdate.setServer(((ManagedSegmentController<?>) getSegmentController()).getManagerContainer(), getControllerElement().getAbsoluteIndex());
		((NTValueUpdateInterface) getSegmentController().getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(chargeValueUpdate, getSegmentController().isOnServer()));
	}

	public void hasHit(double damage, DamageDealerType damageType) {
		if (getSegmentController().isOnServer()) {
			charging.resetAllCharges();
		}
	}
	@Override
	public float getSensorValue(SegmentPiece connected){
		return  Math.min(1f, charging.getCharge() / Math.max(0.0001f, (getChargeNeededForScan())));
	}

	@Override
	public void addHudConext(ControllerStateUnit unit, HudContextHelpManager h, HudContextHelperContainer.Hos hos) {
		h.addHelper(KeyboardMappings.SHIP_PRIMARY_FIRE, Lng.str("Scan"), hos, ContextFilter.IMPORTANT);
	}

	@Override
	public double getPowerConsumedPerSecondResting() {
		return 0; //TODO: base consumption?
	}

	@Override
	public double getPowerConsumedPerSecondCharging() {
		return LongRangeScannerElementManager.SCAN_POWER_CONSUMPTION_FIXED + (LongRangeScannerElementManager.SCAN_CHARGING_POWER_CONSUMED_PER_BLOCK * getTotalSize());
	}

	@Override
	public boolean isPowerCharging(long curTime) {
		return !isCharged();
	}

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

	}

	@Override
	public boolean isPowerConsumerActive() {
		return true;
	}

	@Override
	public void dischargeFully() {
		charging.resetAllCharges();
	}

	@Override
	public CooldownManager getCooldownManager() {
		return cooldown;
	}

	@Override
	public long getCooldownDurationMs() {
		return LongRangeScannerElementManager.RELOAD_AFTER_USE_MS;
	}
}
