package org.schema.game.common.controller.elements.stealth;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelpManager;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelperContainer.Hos;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.ShipManagerContainer;
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
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.ContextFilter;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.sound.controller.AudioController;

import static org.schema.game.common.controller.elements.stealth.StealthElementManager.POWER_THRESHOLD_FOR_DISABLE;
import static org.schema.game.common.controller.elements.stealth.StealthElementManager.REUSE_DELAY_ON_NO_POWER_MS;

/**
 * Formerly "cloaking collection manager".
 */
public class StealthCollectionManager extends ControlBlockElementCollectionManager<StealthUnit, StealthCollectionManager, StealthElementManager> implements PlayerUsableInterface, PowerConsumer, ManagedChargingInterface, ManagedActivationInterface, ManagedCooldownInterface {
	private float powered = 0.0f; //percentage powered

	private final ActivationManager activation;
	private final ChargeManager charging;
	private final CooldownManager cooldown;

	public StealthCollectionManager(SegmentPiece element, SegmentController segController, StealthElementManager em) {
		super(element, ElementKeyMap.STEALTH_MODULE, segController, em);
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

	@Override
	protected Class<StealthUnit> getType() {
		return StealthUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return true;
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

	@Override
	public StealthUnit getInstance() {
		return new StealthUnit();
	}

	@Override
	protected void onChangedCollection() {
		if (!getSegmentController().isOnServer()) {
			((GameClientState) getSegmentController().getState()).getWorldDrawer().getGuiDrawer()
					.managerChanged(this);
		}
		if (getElementCollections().isEmpty() || getRawStealth() < StealthElementManager.SIGNATURE_MINIMUM_RATIO_FOR_STEALTH) {
			StealthElementManager stealthElementManager = ((ShipManagerContainer) ((ManagedSegmentController<?>) getSegmentController()).getManagerContainer())
					.getStealthElementManager();
			if (stealthElementManager.isActive()) {
				stealthElementManager.stopStealth(StealthElementManager.REUSE_DELAY_ON_MODIFICATION_MS); //we refer back to elementmanager for everything, for safety's sake
			}
		}
	}

	@Override
	protected void onRemovedCollection(long absPos, StealthCollectionManager instance) {
		super.onRemovedCollection(absPos, instance);

		StealthElementManager stealthElementManager = ((ShipManagerContainer) ((ManagedSegmentController<?>) getSegmentController()).getManagerContainer()).getStealthElementManager();
		if (stealthElementManager.isActive()) {
			stealthElementManager.stopStealth(0);
		}
	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		return new GUIKeyValueEntry[0];
	}

	@Override
	public String getModuleName() {
		return Lng.str("Stealth System");
	}

	/**
	 * @return the total stealth strength (after power fraction attenuation, ratio against signature size, and effect application)
	 */
	protected float getTotalStealthStrength() {
		float stealthPotencyAfterPower = getRawStealth() * getPowered();
		float signature = getElementManager().getEntityScanSignatureSize();

		float result = stealthPotencyAfterPower/signature;
		result = getConfigManager().apply(StatusEffectType.STEALTH_STRENGTH,result); //apply chamber/status effects
		return result > StealthElementManager.SIGNATURE_MINIMUM_RATIO_FOR_STEALTH ? result : 0f;
	}

	/**
	 * @return the raw stealth power of the stealth blocks (before power limits, comparison to signature size, or effects)
	 */
	public float getRawStealth() {
		return getTotalSize() * StealthElementManager.STEALTH_STRENGTH_PER_BLOCK;
	}

	@Override
	public float getSensorValue(SegmentPiece connected){
		return getElementManager().isActive() ? 1 : 0;
	}

	@Override
	public void handleKeyEvent(ControllerStateUnit unit, KeyboardMappings mapping, Timer timer) {
		if(mapping == KeyboardMappings.SHIP_PRIMARY_FIRE || mapping == KeyboardMappings.SHIP_ZOOM) {
			getElementManager().handle(unit, timer);
		}
	}
	@Override
	public void addHudConext(ControllerStateUnit unit, HudContextHelpManager h, Hos hos) {
		String s = activation.isActive() ? Lng.str("Deactivate Stealth System") : Lng.str("Activate Stealth System");
		h.addHelper(KeyboardMappings.SHIP_PRIMARY_FIRE, s, hos, ContextFilter.IMPORTANT);
	}

	public boolean isPowered() {
		return powered > StealthElementManager.POWER_THRESHOLD_FOR_DISABLE;
	}

	@Override
	public double getPowerConsumedPerSecondResting() {
		return getSegmentController().getConfigManager().apply(StatusEffectType.STEALTH_POWER_TOPOFF_RATE,StealthElementManager.STEALTH_POWER_CONSUME_ADDITIVE_PER_SECOND_RESTING + (getTotalSize() * StealthElementManager.STEALTH_POWER_CONSUME_PER_BLOCK_PER_SECOND_RESTING));
	}

	@Override
	public double getPowerConsumedPerSecondCharging() {
		return getSegmentController().getConfigManager().apply(StatusEffectType.STEALTH_POWER_CHARGE_RATE,StealthElementManager.STEALTH_POWER_CONSUME_ADDITIVE_PER_SECOND_ACTIVE + (getTotalSize() * StealthElementManager.STEALTH_POWER_CONSUME_PER_BLOCK_PER_SECOND_ACTIVE));
	}

	@Override
	public boolean isPowerCharging(long curTime) {
		return activation.isActive() || !charging.fullyCharged() && !cooldown.isCoolingDown(curTime);
	}

	@Override
	public void setPowered(float powered) {
		this.powered = powered;
	}

	@Override
	public float getPowered() {
		return powered;
	}

	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.STEALTH;
	}

	@Override
	public void reloadFromReactor(double secTime, Timer timer, float tickTime, boolean powerCharging, float poweredResting) {
		if(isOnServer() && !isPowered() && getElementManager().isActive())
			getElementManager().stopStealth(REUSE_DELAY_ON_NO_POWER_MS);
	}

	@Override
	public boolean isPowerConsumerActive() {
		return true;
	}

	@Override
	public void dischargeFully() {
		getElementManager().stopStealth(REUSE_DELAY_ON_NO_POWER_MS);
		charging.resetAllCharges();
	}

	@Override
	public boolean isPlayerUsable() {
		return super.isPlayerUsable() && getPowered() > POWER_THRESHOLD_FOR_DISABLE && !getSegmentController().railController.isDockedAndExecuted();
	}

	@Override
	public ActivationManager getActivationManager() {
		return activation;
	}

	@Override
	public long getActivationTimeMs() {
		return getElementManager().getStealthDuration();
	}

	@Override
	public void sendActiveStateUpdate() {
		getElementManager().sendActivationValueUpdate();
	}

	@Override
	public void onActivate() {
		if(isOnServer() && getSegmentController() instanceof Ship entity) {
			entity.getNetworkObject().stealthActive.set(true);
		} else if(!isOnServer()){
			AudioController.fireAudioEvent("0022_spaceship user - large engine thruster start push gas", AudioController.ent(getSegmentController(), getControllerElement(), getControllerIndex(), 500));
		}
	}

	@Override
	public void onDeactivate() {
		if(isOnServer() && getSegmentController() instanceof Ship entity) {
			entity.getNetworkObject().stealthActive.set(false);
		}

		if(!isOnServer()){
			if(getSegmentController().isClientOwnObject()){
				AudioController.fireAudioEvent("0022_gameplay - notification chime 1", AudioController.ent(getSegmentController(), getControllerElement(), getControllerIndex(), 500));
				if(!isOnServer()) getSegmentController().sendClientMessage(Lng.str("[WARNING] Stealth system deactivated.\nShip is visible again!"), ServerMessage.MESSAGE_TYPE_WARNING);
			}
			AudioController.fireAudioEvent("0022_item - forcefield activate", AudioController.ent(getSegmentController(), getControllerElement(), getControllerIndex(), 500));
		}
		cooldown.startCooldown(System.currentTimeMillis());
	}

	@Override
	public ChargeManager getChargeManager() {
		return charging;
	}

	@Override
	public boolean canCharge(long currentTime) {
		return !isActive() && !cooldown.isCoolingDown(currentTime) && getPowered() > POWER_THRESHOLD_FOR_DISABLE;
	}

	@Override
	public float getChargeAddedPerSecond() {
        if (getPowered() > POWER_THRESHOLD_FOR_DISABLE)
            return getPowered() / (getElementManager().getActivationCooldownMs() / 1000f);
        else return 0;
    }

	@Override
	public float getDechargePerSecond() {
        if (getPowered() < POWER_THRESHOLD_FOR_DISABLE)
			return 1f / (getElementManager().getActivationCooldownMs() / 1000f);
        else return 0;
    }

	@Override
	public float getMaxCharge() {
		return 1f;
	}

	@Override
	public void sendChargeUpdate() {
		getElementManager().sendCooldownValueUpdate(); //charge is "cooldown"
	}

	@Override
	public CooldownManager getCooldownManager() {
		return cooldown;
	}

	@Override
	public long getCooldownDurationMs() {
		return 500; //mostly anti spam
	}
}
