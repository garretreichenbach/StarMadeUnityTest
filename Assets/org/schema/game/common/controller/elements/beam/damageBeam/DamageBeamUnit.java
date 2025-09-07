package org.schema.game.common.controller.elements.beam.damageBeam;

import api.listener.events.weapon.AnyWeaponDamageCalculateEvent;
import api.mod.StarLoader;
import org.schema.common.FastMath;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.damage.HitType;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.beam.BeamUnit;
import org.schema.game.common.controller.elements.combination.CombinationAddOn;
import org.schema.game.common.controller.elements.combination.modifier.BeamUnitModifier;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;

// #RM1958 remove import com.sun.org.apache.bcel.internal.generic.GETSTATIC;

public class DamageBeamUnit extends BeamUnit<DamageBeamUnit, DamageBeamCollectionManager, DamageBeamElementManager> {

	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {

		return elementCollectionManager.getElementManager().getGUIUnitValues(this, elementCollectionManager, supportCol, effectCol);
		//		return ControllerManagerGUI.create(state, "Damage Module", this,
		//				new ModuleValueEntry(Lng.str("Damage/Tick", StringTools.formatPointZero(getBeamPower()) ),
		//				new ModuleValueEntry(Lng.str("TickSpeed", StringTools.formatPointZero(getDamageSpeedFactor()) ),
		//				new ModuleValueEntry(Lng.str("Damage/sec", StringTools.formatPointZero((getDamageSpeedFactor())*getBeamPower() ))
		//		);
	}

	@Override
	public boolean canUse(long curTime, boolean popupText) {
		if(!elementCollectionManager.getElementManager().checkCapacity(calcAmmoReqsPerTick())) {
			if(popupText) {
				getSegmentController().popupOwnClientMessage("NO_BEAM_CAP",
						Lng.str("Beam particle reserves depleted!\nIncrease beam ammo storage by placing beam capacity modules on your main structure."),
						ServerMessage.MESSAGE_TYPE_ERROR);
			}
			return false;
		}
		return super.canUse(curTime, popupText);
	}

	@Override
	public float calcAmmoReqsPerTick() {
		return 1;
	}

	private float getBaseConsume() {
		return ((size() + getEffectBonus()) * DamageBeamElementManager.POWER_CONSUMPTION);
	}
	@Override
	public float getMaxEffectiveRange() {
		return DamageBeamElementManager.MAX_EFFECTIVE_RANGE;
	}
	@Override
	public float getMinEffectiveRange() {
		return DamageBeamElementManager.MIN_EFFECTIVE_RANGE;
	}
	@Override
	public float getMaxEffectiveValue() {
		return DamageBeamElementManager.MAX_EFFECTIVE_VALUE;
	}
	@Override
	public float getMinEffectiveValue() {
		return DamageBeamElementManager.MIN_EFFECTIVE_VALUE;
	}
	@Override
	public void flagBeamFiredWithoutTimeout() {
		elementCollectionManager.flagBeamFiredWithoutTimeout(this);
	}

	float combinedSize(int secondarySize){
		return this.size() + this.getEffectBonus() + secondarySize;
	}

	public float getLinearDamageTerm(int secondarySize){
		return combinedSize(secondarySize) * this.getBaseBeamPower();
	}

	private float getExpDamageTerm(int secondarySize) {
		return DamageBeamElementManager.DAMAGE_PER_HIT_EXP_MULT * FastMath.pow(combinedSize(secondarySize), DamageBeamElementManager.DAMAGE_PER_HIT_EXP);
	}

	/**
	 * @return the damage
	 */
	public float getDamagePerTick(int secondarySize) {
		//INSERTED CODE @...
		float outDamage = getLinearDamageTerm(secondarySize) + getExpDamageTerm(secondarySize); //additive damage is handled later
		outDamage = this.getConfigManager().apply(StatusEffectType.WEAPON_DAMAGE, this.getDamageType(), outDamage);
		AnyWeaponDamageCalculateEvent event = new AnyWeaponDamageCalculateEvent(this, outDamage, false);
		StarLoader.fireEvent(event, this.getSegmentController().isOnServer());
		return event.damage;
		///
	}

	@Override
	public float getBeamPowerWithoutEffect() {
		return getLinearDamageTerm(0) + getExpDamageTerm(0) + getAdditiveBeamPower();
	}

	@Override
	public float getBeamPower() {
		return getDamagePerTick(0);
	}

	@Override
	public float getAdditiveBeamPower() {
		return DamageBeamElementManager.ADDITIVE_DAMAGE;
	}

	@Override
	public float getBaseBeamPower() {
		return DamageBeamElementManager.DAMAGE_PER_HIT_LINEAR.get(getSegmentController().isUsingPowerReactors());
	}

	@Override
	public float getPowerConsumption() {
		return getBaseConsume() * getExtraConsume();
	}

	@Override
	public float getDistanceRaw() {
		return DamageBeamElementManager.DISTANCE * ((GameStateInterface) getSegmentController().getState()).getGameState().getWeaponRangeReference();
	}
	@Override
	public float getDistanceFull() {
		
		
		ControlBlockElementCollectionManager<?, ?, ?> effectCollectionManager = CombinationAddOn.getEffect(
				elementCollectionManager.getEffectConnectedElement(), 
				null, getSegmentController());
		ControlBlockElementCollectionManager<?, ?, ?> we = elementCollectionManager.getElementManager()
				.getCollectionManagersMap().get(ElementCollection.getPosIndexFrom4(elementCollectionManager.getSlaveConnectedElement()));
		if (we != null) {
			int slaveType = ElementCollection.getType(elementCollectionManager.getSlaveConnectedElement());
			assert (!ElementKeyMap.getInfo((short) slaveType).isEffectCombinationController()) : ElementKeyMap.toString((short) slaveType);
			BeamUnitModifier<?> gui = (BeamUnitModifier<?>) elementCollectionManager.getElementManager().getAddOn().getGUI(elementCollectionManager, this, we, effectCollectionManager);
			return gui.outputDistance;
		}
		return getConfigManager().apply(StatusEffectType.WEAPON_RANGE, getDamageType(), getDistanceRaw());
	}
	/**
	 * @return the salvageSpeedFactor
	 */
	@Override
	public float getTickRate() {
		return DamageBeamElementManager.TICK_RATE;
	}

	@Override
	public float getExtraConsume() {
		return (1f + (Math.max(0, elementCollectionManager.getElementCollections().size() - 1) * DamageBeamElementManager.ADDITIONAL_POWER_CONSUMPTION_PER_UNIT_MULT));
	}

	@Override
	public float getBasePowerConsumption() {
		return DamageBeamElementManager.POWER_CONSUMPTION;
	}

	@Override
	public float getPowerConsumptionWithoutEffect() {
		return (size()) * DamageBeamElementManager.POWER_CONSUMPTION;
	}
	
	@Override
	public double getPowerConsumedPerSecondResting() {
		return elementCollectionManager.getElementManager()
		.calculatePowerConsumptionCombi(
				getPowerConsumedPerSecondRestingPerBlock(), false, this);
		
	}
	@Override
	public double getPowerConsumedPerSecondCharging() {
		return elementCollectionManager.getElementManager()
				.calculatePowerConsumptionCombi(
						getPowerConsumedPerSecondChargingPerBlock(), true, this);
	}
	public double getPowerConsumedPerSecondRestingPerBlock() {
		double p = DamageBeamElementManager.REACTOR_POWER_CONSUMPTION_RESTING;
		return getConfigManager().apply(StatusEffectType.WEAPON_TOP_OFF_RATE, getDamageType(), p);
	}

	public double getPowerConsumedPerSecondChargingPerBlock() {
		double p = DamageBeamElementManager.REACTOR_POWER_CONSUMPTION_CHARGING;
		return getConfigManager().apply(StatusEffectType.WEAPON_CHARGE_RATE, getDamageType(), p);
	}
	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.BEAMS;
	}

	@Override
	public boolean isLatchOn() {
		return DamageBeamElementManager.LATCH_ON != 0;
	}
	@Override
	public boolean isCheckLatchConnection() {
		return DamageBeamElementManager.CHECK_LATCH_CONNECTION != 0;
	}

	@Override
	public HitType getHitType() {
		return HitType.WEAPON;
	}
	@Override
	public boolean isFriendlyFire() {
		return DamageBeamElementManager.FRIENDLY_FIRE != 0;
	}
	@Override
	public boolean isAimable() {
		return DamageBeamElementManager.AIMABLE != 0;
	}
	@Override
	public float getAcidDamagePercentage() {
		return DamageBeamElementManager.ACID_DAMAGE_PERCENTAGE;
	}
	@Override
	public boolean isPenetrating() {
		return DamageBeamElementManager.PENETRATION != 0;
	}

	@Override
	public float getBaseCapacityUsedPerTick() {
		return DamageBeamElementManager.BASE_CAPACITY_USED_PER_TICK;
	}

	@Override
	public float getAdditionalCapacityUsedPerDamage() {
		return DamageBeamElementManager.ADDITIONAL_CAPACITY_USED_PER_DAMAGE;
	}

	@Override
	public float getDamage() {
		return getBeamPower() + getAdditiveBeamPower();
	}

	@Override
	public float getFiringPower() {
		return getDamage();
	}
}
