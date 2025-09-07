package org.schema.game.common.controller.elements.stealth;

import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.*;
import org.schema.game.common.controller.elements.stealth.StealthAddOn.StealthLvl;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.network.objects.remote.RemoteValueUpdate;
import org.schema.game.network.objects.valueUpdate.StealthActivationValueUpdate;
import org.schema.game.network.objects.valueUpdate.StealthChargeValueUpdate;
import org.schema.game.network.objects.valueUpdate.NTValueUpdateInterface;
import org.schema.game.network.objects.valueUpdate.ValueUpdate;
import org.schema.game.network.objects.valueUpdate.ValueUpdate.ValTypes;
import org.schema.schine.ai.stateMachines.AiInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.server.ServerMessage;

import static org.schema.game.common.data.element.ElementKeyMap.*;

/**
 * Formerly "cloaking element manager".
 */
public class StealthElementManager extends UsableControllableFiringElementManager<StealthUnit, StealthCollectionManager, StealthElementManager> implements ManagerUpdatableInterface, ManagerActivityInterface, StealthElementManagerInterface, ManagerReloadInterface, NTSenderInterface, SingleElementCollectionContainerInterface<StealthCollectionManager> {
	@ConfigurationElement(name = "PowerConsumedPerSecondBaseResting")
	public static float STEALTH_POWER_CONSUME_ADDITIVE_PER_SECOND_RESTING = 1000;
	@ConfigurationElement(name = "PowerConsumedPerSecondPerBlockResting")
	public static float STEALTH_POWER_CONSUME_PER_BLOCK_PER_SECOND_RESTING = 100;
	@ConfigurationElement(name = "PowerConsumedPerSecondBaseActive")
	public static float STEALTH_POWER_CONSUME_ADDITIVE_PER_SECOND_ACTIVE = 1000;
	@ConfigurationElement(name = "PowerConsumedPerSecondPerBlockActive")
	public static float STEALTH_POWER_CONSUME_PER_BLOCK_PER_SECOND_ACTIVE = 100;

	@ConfigurationElement(name = "StealthStrengthPerBlock")
	public static float STEALTH_STRENGTH_PER_BLOCK = 100;
	@ConfigurationElement(name = "SignatureStrengthPerReactor")
	public static float SIGNATURE_STRENGTH_PER_REACTOR = 1.0f;
	@ConfigurationElement(name = "SignatureStrengthPerMass")
	public static float SIGNATURE_STRENGTH_PER_MASS = 0.1f;
	@ConfigurationElement(name = "StealthStrengthMinimumRatioToSignature")
	public static float SIGNATURE_MINIMUM_RATIO_FOR_STEALTH = 0.05f;

	@ConfigurationElement(name= "UsageDurationBasicMs")
	public static int USAGE_DURATION_BASE_MS = 30000;
	@ConfigurationElement(name = "ReuseDelayOnActionMs")
	public static int REUSE_DELAY_ON_ACTION_MS = 6000;
	@ConfigurationElement(name = "ReuseDelayOnModificationMs")
	public static int REUSE_DELAY_ON_MODIFICATION_MS = 6000;
	@ConfigurationElement(name = "ReuseDelayOnSwitchedOffMs")
	public static int REUSE_DELAY_ON_SWITCHED_OFF_MS = 1000;
	@ConfigurationElement(name = "ReuseDelayOnHitMs")
	public static int REUSE_DELAY_ON_HIT_MS = 10000;
	@ConfigurationElement(name = "ReuseDelayOnScanMs")
	public static int REUSE_DELAY_ON_SCAN_MS = 30000;
	@ConfigurationElement(name = "ReuseDelayOnNoPowerMs")
	public static int REUSE_DELAY_ON_NO_POWER_MS = 6000;
	@ConfigurationElement(name = "PowerFractionForFullDisable")
	public static float POWER_THRESHOLD_FOR_DISABLE = 0.5f;

	private int activationCooldown = 1;

	public StealthElementManager(SegmentController segmentController) {
		super(STEALTH_COMPUTER, STEALTH_MODULE, segmentController);
	}

	public float getTotalStealthStrength(){
		return getTotalStealthStrength(true);
	}

	/**
	 * @param respectActivationState whether to treat a disabled stealth system as having zero stealth strength, regardless of capability.
	 * @return The total net stealth strength of the system,AFTER factoring in the entity's signature size and any applied effects. Will return zero if there is no stealth module collection present.
	 */
	public float getTotalStealthStrength(boolean respectActivationState) {
		if((respectActivationState && !(isActive())) || !hasCollection()) return 0;
		else return getCollection().getTotalStealthStrength();
	}

	@Override
	public boolean isActive() {
		return hasCollection() && getCollection().isActive();
	}

	public boolean hasStealthCapability(StealthLvl lvl) {
		if(isActive()){
			if(lvl == StealthLvl.CLOAKING){
				return getConfigManager().apply(StatusEffectType.STEALTH_CLOAK_CAPABILITY, false);
			}else if(lvl == StealthLvl.JAMMING){
				//return getConfigManager().apply(StatusEffectType.STEALTH_JAMMER_CAPABILITY, false);
				return true; //stealth jams by default
			}
		}
		return false;
	}

	/**
	 * Sets internal stealth activation value if a collection is present to update to.<br/>
	 * This will automatically and implicitly propagate updates, as long as the new value is different from the old one.
	 * @param v the value to set
	 */
	public void setStealthActivation(boolean v) {
		if(hasCollection()) getCollection().getActivationManager().setActive(v);
	}

	public void onHit() {
		stopStealth(REUSE_DELAY_ON_HIT_MS);
	}

	public boolean tryStartStealth() {
		boolean isCooledDown = !isCoolingDown(System.currentTimeMillis());
		float stealth = getTotalStealthStrength(false);
		if(stealth > 0 && isCooledDown){
			startStealth();
			return true;
		}
		else {
			String reason = "[ERROR] UNKNOWN REASON";
			if(!isCooledDown) reason = Lng.str("System is on cooldown!");
			else if(!getCollection().isPowered()) reason = Lng.str("No power!");
			else if(stealth <= 0) reason = Lng.str("Stealth system is too small!");
			AbstractOwnerState owner = getSegmentController().getOwnerState();

			ServerMessage msg = new ServerMessage(Lng.astr("Could not activate stealth: %s\nDisable active systems or link more Stealth Modules to your Stealth Computer to engage stealth.",reason), ServerMessage.MESSAGE_TYPE_INFO);

			if(owner != null) owner.sendServerMessage(msg.getMessage(), ServerMessage.MESSAGE_TYPE_INFO);
			else if(getSegmentController().getFleet() != null){
				getSegmentController().getFleet().sendOwnerMessageServer(msg);
			}
		}
		return false;
	}

	protected void startStealth() {
		if (getSegmentController().isOnServer() && !isActive()) {
			System.err.println("[STEALTH] " + getSegmentController().getState() + " " + getSegmentController() + " STARTING STEALTH");
			setStealthActivation(true);
			System.err.println("[STEALTH] " + getSegmentController().getState() + " " + getSegmentController() + " STEALTH SYSTEM STARTED WITH STRENGTH " + getTotalStealthStrength());
		}
	}

	public void stopStealth(int cooldown) {
		if (getSegmentController().isOnServer() && isActive()) {
			System.err.println("[STEALTH SYSTEM] " + getSegmentController().getState() + " " + getSegmentController() + " STOPPING STEALTH");
			this.activationCooldown = (int) getConfigManager().apply(StatusEffectType.STEALTH_CHARGE_TIME,(float)cooldown);
			setStealthActivation(false);
			System.err.println("[STEALTH SYSTEM] " + getSegmentController().getState() + " " + getSegmentController() + " STEALTH STOPPED");
		}
	}

	public int getActivationCooldownMs() {
		return activationCooldown;
	}

	public void setActivationCooldown(int val) {
		activationCooldown = val;
	}

	public void sendCooldownValueUpdate() {
		assert (getSegmentController().isOnServer());
		StealthChargeValueUpdate vu = new StealthChargeValueUpdate();
		assert (vu.getType() == ValTypes.STEALTH_RECHARGE);
		setAndSendUpdate(vu);
	}

	public void sendActivationValueUpdate() {
		assert (getSegmentController().isOnServer());
		StealthActivationValueUpdate vu = new StealthActivationValueUpdate();
		assert (vu.getType() == ValTypes.STEALTH_ACTIVE);
		setAndSendUpdate(vu);
	}

	private void setAndSendUpdate(ValueUpdate v){
		v.setServer(((ManagedSegmentController<?>) getSegmentController()).getManagerContainer());
		((NTValueUpdateInterface) getSegmentController().getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(v, getSegmentController().isOnServer()));
	}

	public float getPowerConsumption(float delta) {
		float amount = 0;
		for(StealthUnit u : getCollection().getElementCollections()) amount += u.getPowerConsumption();
		return amount;
	}

	@Override
	public void update(Timer timer) {
		if (isActive()){
			getSegmentController().popupOwnClientMessage(Lng.str("STEALTH ACTIVE") + Lng.str("\nStealth Strength: ") + getTotalStealthStrength(false) + Lng.str("\nRadar Jamming: ") + (hasJamCapability()) + Lng.str("\nCloaking: ") + hasCloakCapability(), ServerMessage.MESSAGE_TYPE_INFO);
			// TODO translatable true/false values
			// TODO use notification UI similar to power outage rather than warning bubbles...?
			//  Need to investigate state and capabilities of that system
			if (uncontrolledShip()) {
				if (getSegmentController().isOnServer()) {
					stopStealth(REUSE_DELAY_ON_SWITCHED_OFF_MS);
				}
			}
			else if(!getCollection().isPowered()) stopStealth(REUSE_DELAY_ON_NO_POWER_MS);
		}
	}

	/**
	 * Checks if ship is uncontrolled by any AI or player. This is important because a permanently-stealthed craft without players or AI may be effectively uninteractable and lost unless scanned.
	 * @return {@code true} if the ship has neither attached players nor an active AI system, otherwise {@code false}.
	 */
	private boolean uncontrolledShip(){
		//TODO include aligned players, and ensure they can see the ship
		return ((PlayerControllable) getSegmentController()).getAttachedPlayers().isEmpty() && !((AiInterface) getSegmentController()).getAiConfiguration().isActiveAI();
	}

	@Override
	public ControllerManagerGUI getGUIUnitValues(StealthUnit firingUnit,
                                                 StealthCollectionManager col,
                                                 ControlBlockElementCollectionManager<?, ?, ?> supportCol,
                                                 ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return ControllerManagerGUI.create((GameClientState) getState(), Lng.str("Stealth Unit"), firingUnit
		);
	}

	@Override
	protected String getTag() {
		return "stealth";
	}

	@Override
	public StealthCollectionManager getNewCollectionManager(
			SegmentPiece position, Class<StealthCollectionManager> clazz) {
		return new StealthCollectionManager(position, getSegmentController(), this);
	}

	@Override
	public String getManagerName() {
		return "Stealth System Collective";
	}


	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {
		if (!unit.isFlightControllerActive()) {
			return;
		}
		if (isCoolingDown(timer.currentTime)){
			return;
		}
		if (!hasCollection() || getCollection().getElementCollections().isEmpty()) {
			return;
		}
		if (getSegmentController().isOnServer()) {
			if (unit.isDown(KeyboardMappings.SHIP_PRIMARY_FIRE)) {
				if (!isActive()) {
					if(!isCoolingDown(timer.currentTime)) {
						boolean hasCharge = getCollection().getChargeManager().consumeFullCharge(1);
						if (hasCharge) tryStartStealth();
						else getSegmentController().sendServerMessage("Cannot activate!\nStealth System is recharging!", ServerMessage.MESSAGE_TYPE_ERROR);
					} else getSegmentController().popupOwnClientMessage("Cannot activate!\nStealth System on cooldown!", ServerMessage.MESSAGE_TYPE_ERROR);
				} else {
					stopStealth(StealthElementManager.REUSE_DELAY_ON_SWITCHED_OFF_MS);
				}
			}
		}
	}

	@Override
	public double calculateStealthIndex(double scoreForConstant) {
		if (hasCollection() && getCollection().getTotalSize() > 1) {
			return Math.min(1d, getManagerContainer().getMainReactor().getPowerInterface().getRechargeRatePowerPerSec() / getPowerConsumption(1)) * scoreForConstant;
		}
		return 0;
	}
	@Override
	public String getReloadStatus(long id) {
		return Lng.str("STANDBY");
	}
	@Override
	public void drawReloads(Vector3i iconPos, Vector3i iconSize,
	                        long controllerPos) {
		StealthCollectionManager ec = getCollectionManagersMap().get(controllerPos);
		if (ec != null) {
			long currentTime = System.currentTimeMillis();
			InputState state = (InputState) getState();

			if (ec.isActive()) {
				ec.getActivationManager().drawReloads(iconPos, iconSize, state, currentTime);
			} else if (ec.getCooldownManager().isCoolingDown(currentTime)) {
				ec.getCooldownManager().drawReloads(iconPos, iconSize, state, currentTime);
			} else {
				ec.getChargeManager().drawReloads(iconPos, iconSize, state);
			}
		}
	}

	/**
	 * @return The stealth usage duration in milliseconds (after effect modifiers are applied). If this value is less than 0, it should be treated as infinite duration.
	 */
	public long getStealthDuration() {
		return (long) getConfigManager().apply(StatusEffectType.STEALTH_USAGE_TIME,(float)USAGE_DURATION_BASE_MS);
	}

	@Override
	public boolean canUpdate() {
		return true;
	}

	@Override
	public void onNoUpdate(Timer timer) {
	}

	@Override
	public boolean hasCollection(){
		return !getCollectionManagers().isEmpty();
	}

	@Override
    public StealthCollectionManager getCollection() {
		if(!hasCollection()) return null;
		return getCollectionManagers().get(0); //should only ever be one
	}

	public boolean hasJamCapability() {
		return getConfigManager().apply(StatusEffectType.STEALTH_CLOAK_CAPABILITY, true); //true by default - what is the point of a stealth system that doesn't at least jam you?
	}

	public boolean hasCloakCapability() {
		return getConfigManager().apply(StatusEffectType.STEALTH_CLOAK_CAPABILITY, false);
	}

	public boolean hasMissileLockBreak() {
		return getConfigManager().apply(StatusEffectType.STEALTH_MISSILE_LOCK_ON_TIME, false);
	}

	public float getEntityScanSignatureSize() {
		float fromMass = SIGNATURE_STRENGTH_PER_MASS * getSegmentController().getMass();
		float fromReactor = SIGNATURE_STRENGTH_PER_REACTOR * getManagerContainer().getMainReactor().getTotalSize();
		//TODO from active systems
		return fromMass + fromReactor;
	}

	@Override
	public void updateToFullNT(NetworkObject obj) {
		//TODO
	}

	public boolean isCoolingDown(long curTime) {
		if(hasCollection()) return getCollection().getCooldownManager().isCoolingDown(curTime);
		else return false;
	}
}
