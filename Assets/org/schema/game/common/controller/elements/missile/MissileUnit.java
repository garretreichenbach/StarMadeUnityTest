package org.schema.game.common.controller.elements.missile;

import api.listener.events.weapon.AnyWeaponDamageCalculateEvent;
import api.mod.StarLoader;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.elements.missile.dumb.DumbMissileElementManager;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.CustomOutputUnit;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ShootContainer;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.server.ServerMessage;

public abstract class MissileUnit<
		E extends MissileUnit<E, CM, EM>,
		CM extends MissileCollectionManager<E, CM, EM>,
		EM extends MissileElementManager<E, CM, EM>> extends CustomOutputUnit<E, CM, EM> {

	private Vector3i minSig = new Vector3i(0, 0, 0);
	private Vector3i maxSig = new Vector3i(0, 0, 0);

	@Override
	public int getEffectBonus() {
		//add percentage of total blocks of tertiary amount to distribute bonus on units. also cap bonus at size
		return Math.min(size(), (int) (((double) size() / (double) elementCollectionManager.getTotalSize()) * elementCollectionManager.getEffectTotal()));
	}

	@Override
	public float getExtraConsume() {
		return (1f + (Math.max(0, elementCollectionManager.getElementCollections().size() - 1) * DumbMissileElementManager.ADDITIONAL_POWER_CONSUMPTION_PER_UNIT_MULT));
	}
	@Override
	protected DamageDealerType getDamageType() {
		return DamageDealerType.MISSILE;
	}
	public float getBaseConsume() {
		return ((size() + getEffectBonus()) * DumbMissileElementManager.BASE_POWER_CONSUMPTION);
	}

	float combinedSize(int secondarySize){
		return this.size() + this.getEffectBonus() + secondarySize;
	}

	public float getLinearDamageTerm(int secondarySize){
		return combinedSize(secondarySize) * this.getBaseDamage();
	}

	private float getExpDamageTerm(int secondarySize) {
		return DumbMissileElementManager.DAMAGE_EXP_MULT * FastMath.pow(combinedSize(secondarySize), DumbMissileElementManager.DAMAGE_EXP);
	}

	/**
	 * @return the damage
	 */
	public float getDamage(int secondarySize) {
		//INSERTED CODE @...
		float outDamage = getLinearDamageTerm(secondarySize) + getExpDamageTerm(secondarySize); //additive damage is handled later
		outDamage = this.getConfigManager().apply(StatusEffectType.WEAPON_DAMAGE, this.getDamageType(), outDamage);
		AnyWeaponDamageCalculateEvent event = new AnyWeaponDamageCalculateEvent(this, outDamage, false);
		StarLoader.fireEvent(event, this.getSegmentController().isOnServer());
		return event.damage;
		///
	}

	@Override
	public float getDamage() {
		return getDamage(0);
	}

	@Override
	public boolean canUse(long curTime, boolean popupText) {
		if(!elementCollectionManager.getElementManager().checkMissileCapacity()) {
			if(popupText) {
			getSegmentController().popupOwnClientMessage("NO_MISSILE_CAP", 
					Lng.str("Missile storage depleted!\nIncrease missile storage by placing missile capacity modules on your main structure."),
					ServerMessage.MESSAGE_TYPE_ERROR);
			}
			return false;
		}
		return super.canUse(curTime, popupText);
	}

	public float getSpeed() {
		return ((GameStateInterface) getSegmentController().getState()).getGameState().getMaxGalaxySpeed() * DumbMissileElementManager.BASE_SPEED;
	}

	/**
	 * updates the significator so it is the smallest
	 * (by order x,y,z)
	 */
	@Override
	protected void significatorUpdate(int x, int y, int z, int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, long index) {

		long zSig = getPosZ(significator);
		long ySig = getPosY(significator);
		long xSig = getPosX(significator);

		if (z > zSig) {
			minSig.set(x, y, z);
			maxSig.set(x, y, z);
			zSig = zMax;
		} else if (x == xSig) {
			minSig.set(x, Math.min(y, (int) ySig), Math.min(z, (int) zSig));
			maxSig.set(x, Math.max(y, (int) ySig), Math.max(z, (int) zSig));
		}
		ySig = maxSig.y - (maxSig.y - minSig.y) / 2;
		xSig = maxSig.x - (maxSig.x - minSig.x) / 2;

		significator = ElementCollection.getIndex((int) xSig, (int) ySig, (int) zSig);

	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.element.FireingUnit#getBasePowerConsumption()
	 */
	@Override
	public float getBasePowerConsumption() {
		return DumbMissileElementManager.BASE_POWER_CONSUMPTION;
	}

	@Override
	public float getPowerConsumption() {
		return getExtraConsume() * getBaseConsume();
	}

	@Override
	public float getPowerConsumptionWithoutEffect() {
		return (size()) * DumbMissileElementManager.BASE_POWER_CONSUMPTION;
	}

	@Override
	public float getReloadTimeMs() {
		return DumbMissileElementManager.BASE_RELOAD;
	}

	@Override
	public float getInitializationTime() {
		return DumbMissileElementManager.BASE_RELOAD;
	}

	@Override
	public float getDistanceRaw() {
		return DumbMissileElementManager.BASE_DISTANCE * ((GameStateInterface) getSegmentController().getState()).getGameState().getWeaponRangeReference();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "MissileUnit [significator=" + significator + "]";
	}

	public float getBaseDamage() {
		return DumbMissileElementManager.BASE_DAMAGE.get(getSegmentController().isUsingPowerReactors());
	}

	@Override
	public float getFiringPower() {
		return getDamage();
	}
	public double getPowerConsumedPerSecondRestingPerBlock() {
		double p = DumbMissileElementManager.REACTOR_POWER_CONSUMPTION_RESTING;
		return getConfigManager().apply(StatusEffectType.WEAPON_TOP_OFF_RATE, getDamageType(), p);
	}

	public double getPowerConsumedPerSecondChargingPerBlock() {
		double p = DumbMissileElementManager.REACTOR_POWER_CONSUMPTION_CHARGING;
		return getConfigManager().apply(StatusEffectType.WEAPON_CHARGE_RATE, getDamageType(), p);
	}
	@Override
	public double getPowerConsumedPerSecondResting() {
		return elementCollectionManager.getElementManager()
		.calculatePowerConsumptionCombi(
				getPowerConsumedPerSecondRestingPerBlock(), false, (E) this);
		
	}
	@Override
	public double getPowerConsumedPerSecondCharging() {
		return elementCollectionManager.getElementManager()
				.calculatePowerConsumptionCombi(
						getPowerConsumedPerSecondChargingPerBlock(), true, (E) this);
	}
	@Override
	public void doShot(ControllerStateInterface unit, Timer timer, ShootContainer shootContainer) {
		
		boolean focus = elementCollectionManager.isInFocusMode();
		boolean lead = true; //will only lead for AI ControllerUnit
		
		unit.getShootingDir(
				getSegmentController(),
				shootContainer,
				getDistanceFull(),
				getSpeed(),
				elementCollectionManager.getControllerPos(),
				focus,
				lead);
		if(shootContainer.shootingDirTemp.lengthSquared() > 0) {
			shootContainer.shootingDirTemp.normalize();
			float speed = getSpeed() * ((GameStateInterface) getSegmentController().getState()).getGameState().isRelativeProjectiles();
			shootContainer.shootingDirTemp.scale(speed);
			assert(!Float.isNaN(shootContainer.shootingDirTemp.x) ):speed+"; "+shootContainer.shootingDirTemp;
			EM em = elementCollectionManager.getElementManager();
			em.doShot((E)this, elementCollectionManager, shootContainer, getSpeed(), unit.getAquiredTarget(), unit.getPlayerState(), timer);		
		}
	}
	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.MISSILES;
	}


	public float getAdditionalCapacityUsedPerDamageStatic() {
		return DumbMissileElementManager.ADDITIONAL_CAPACITY_USER_PER_DAMAGE;
	}

	public float AdditionalCapacityUsedPerDamageMultStatic() {
		return DumbMissileElementManager.ADDITIONAL_CAPACITY_USER_PER_DAMAGE_MULT;
	}

	public float percentagePowerUsageCharging() {
		return DumbMissileElementManager.PERCENTAGE_POWER_USAGE_CHARGING;
	}

	public float percentagePowerUsageResting() {
		return DumbMissileElementManager.PERCENTAGE_POWER_USAGE_RESTING;
	}
}