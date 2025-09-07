package org.schema.game.common.controller.elements.cannon;

import api.listener.events.weapon.AnyWeaponDamageCalculateEvent;
import api.mod.StarLoader;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.combination.modifier.tagMod.DamageUnitInterface;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.CustomOutputUnit;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ShootContainer;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.server.ServerMessage;

public class CannonUnit extends CustomOutputUnit<CannonUnit, CannonCollectionManager, CannonElementManager> implements DamageUnitInterface {


	private float projectileWidth = 1;
	
	
	private float getBaseConsume() {
		return ((size() + getEffectBonus()) * CannonElementManager.BASE_POWER_CONSUMPTION);
	}
	@Override
	public int getEffectBonus() {
		//add percentage of total blocks of tertiary amount to distribute bonus on units. also cap bonus at size
		return Math.min(size(), (int) (((double) size() / (double) elementCollectionManager.getTotalSize()) * elementCollectionManager.getEffectTotal()));
	}

	@Override
	protected DamageDealerType getDamageType() {
		return DamageDealerType.PROJECTILE;
	}

	float combinedSize(int secondarySize){
		return this.size() + this.getEffectBonus() + secondarySize;
	}

	public float getLinearDamageTerm(int secondarySize){
		return combinedSize(secondarySize) * this.getBaseDamage();
	}

	private float getExpDamageTerm(int secondarySize) {
		return CannonElementManager.DAMAGE_EXP_MULT * FastMath.pow(combinedSize(secondarySize), CannonElementManager.DAMAGE_EXP);
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
	public float getBaseDamage() {
		return CannonElementManager.DAMAGE_LINEAR.get(getSegmentController().isUsingPowerReactors()) * elementCollectionManager.currentDamageMult;
	}
	@Override
	public float getExtraConsume() {
		return (1f + (Math.max(0, elementCollectionManager.getElementCollections().size() - 1) * CannonElementManager.ADDITIONAL_POWER_CONSUMPTION_PER_UNIT_MULT));
	}

	public float getSpeed() {
		return ((GameStateInterface) getSegmentController().getState()).getGameState().getMaxGalaxySpeed() * CannonElementManager.BASE_SPEED;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "WeaponUnit " + super.toString();
	}

	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return elementCollectionManager.getElementManager().getGUIUnitValues(this, elementCollectionManager, supportCol, effectCol);
	}

	@Override
	public float getBasePowerConsumption() {
		return CannonElementManager.BASE_POWER_CONSUMPTION;
	}

	@Override
	public float getPowerConsumption() {
		return getExtraConsume() * getBaseConsume();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.element.FireingUnit#getPowerConsumptionWithoutEffect()
	 */
	@Override
	public float getPowerConsumptionWithoutEffect() {
		return ((size()) * CannonElementManager.BASE_POWER_CONSUMPTION);
	}
	public float getImpactForce() {
		return CannonElementManager.IMPACT_FORCE;
	}
	public float getRecoil() {
		return CannonElementManager.RECOIL;
	}
	
	public int getPenetrationDepth(float damage) {
		int depth = Math.max(1, 
			CannonElementManager.PROJECTILE_PENETRATION_DEPTH_BASIC	+ 
				Math.round((float) Math.pow(damage, CannonElementManager.PROJECTILE_PENETRATION_DEPTH_EXP) 
					* CannonElementManager.PROJECTILE_PENETRATION_DEPTH_EXP_MULT));
//		System.out.println("[WEAPON] Penetration Depth for damage " + getDamage() + " is " + depth);
		return depth;
	}
	@Override
	public float getReloadTimeMs() {
		return CannonElementManager.BASE_RELOAD;//getEffects()[RELOAD].getValue();
	}

	@Override
	public float getInitializationTime() {
		return CannonElementManager.BASE_RELOAD;
	}

	@Override
	public float getDistanceRaw() {
		return CannonElementManager.BASE_DISTANCE * ((GameStateInterface) getSegmentController().getState()).getGameState().getWeaponRangeReference();//getEffects()[DISTANCE].getValue();
	}

	@Override
	public float getFiringPower() {
		return getDamage();
	}

	@Override
	public double getPowerConsumedPerSecondResting() {
		return elementCollectionManager.getElementManager()
		.calculatePowerConsumptionCombi(
				getPowerConsumedPerSecondRestingPerBlock(), false, this);
		
	}

	@Override
	public boolean canUse(long curTime, boolean popupText) {
		if(!elementCollectionManager.getElementManager().checkCapacity()) {
			if(popupText) {
				getSegmentController().popupOwnClientMessage("NO_CANNON_CAP", //no cap
						Lng.str("Cannon ammo reserves depleted!\nIncrease ammo storage by placing beam capacity modules on your main structure."),
						ServerMessage.MESSAGE_TYPE_ERROR);
			}
			return false;
		}
		return super.canUse(curTime, popupText);
	}

	@Override
	public double getPowerConsumedPerSecondCharging() {
		return elementCollectionManager.getElementManager()
				.calculatePowerConsumptionCombi(
						getPowerConsumedPerSecondChargingPerBlock(), true, this);
	}
	public double getPowerConsumedPerSecondRestingPerBlock() {
		double powCons = CannonElementManager.REACTOR_POWER_CONSUMPTION_RESTING;
		powCons = getConfigManager().apply(StatusEffectType.WEAPON_TOP_OFF_RATE, getDamageType(), powCons);
		
		return powCons;
	}
	public double getPowerConsumedPerSecondChargingPerBlock() {
		double powCons = CannonElementManager.REACTOR_POWER_CONSUMPTION_CHARGING;
		powCons = getConfigManager().apply(StatusEffectType.WEAPON_CHARGE_RATE, getDamageType(), powCons);
		
		return powCons;
	}

	@Override
	public void doShot(ControllerStateInterface unit, Timer timer, ShootContainer shootContainer) {


		boolean focus = unit.canFocusWeapon() && elementCollectionManager.isInFocusMode();
		boolean lead = true; //will only lead for AI ControllerUnit
		unit.getShootingDir(
				getSegmentController(),
				shootContainer,
				getDistanceFull(),
				getSpeed(),
				elementCollectionManager.getControllerPos(),
				focus,
				lead);
		
		assert(shootContainer.shootingDirTemp.lengthSquared() > 0):shootContainer.shootingDirTemp;
		if(!isAimable()){
			shootContainer.shootingDirTemp.set(shootContainer.shootingDirStraightTemp);
		}
		
		shootContainer.shootingDirTemp.normalize();
		
		float speed = getSpeed() * ((GameStateInterface) getSegmentController().getState()).getGameState().isRelativeProjectiles();
		shootContainer.shootingDirTemp.scale(speed);
		CannonElementManager em = elementCollectionManager.getElementManager();
		


		em.doShot(this, elementCollectionManager, shootContainer, unit.getPlayerState(), timer);		
	}

	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.CANNONS;
	}
	@Override
	public void calculateExtraDataAfterCreationThreaded(long updateSignture, LongOpenHashSet totalCollectionSet) {
		super.calculateExtraDataAfterCreationThreaded(updateSignture, totalCollectionSet);
		
		
		int[] dirs = new int[4];
		byte[] dCodes = new byte[4];
		
		byte controllerOrientation = elementCollectionManager.getControllerElement().getOrientation();
		
		//determine which sides are perpendicular to the firing direction and put their index into an array
		for(byte i = 0, dp = 0; i < 6; i++) {
			if(i != controllerOrientation && i != Element.getOpposite(controllerOrientation)) {
				dCodes[dp] = i;
				dp++;
			}
		}
		
		for(int d = 0; d < dirs.length; d++) {
			final byte dCode = dCodes[d];
			final Vector3i dir = Element.DIRECTIONSi[dCode];
			long index;
			int i = 0;
			boolean contains;
			do {
				
				int x = getOutput().x + (i+1) * dir.x;  
				int y = getOutput().y + (i+1) * dir.y;  
				int z = getOutput().z + (i+1) * dir.z;  
				
				index = ElementCollection.getIndex(x, y, z);
				
				contains = totalCollectionSet.contains(index);
				if(contains) {
					i++;
				}
				
			}while(contains);
			dirs[d] = i;
			
		}
		int sizeA = dirs[0] + 1 + dirs[1];
		int sizeB = dirs[2] + 1 + dirs[3];
		
		int width = Math.max(sizeA, sizeB);
		
		this.projectileWidth = width * CannonElementManager.PROJECTILE_WIDTH_MULT;
	}

	public float getProjectileWidth() {
		return projectileWidth;
	}
	public boolean isAimable() {
		return CannonElementManager.AIMABLE != 0;
	}

	public float getBaseCapacityUsedPerShot() {
		return 1; //it's a cannon round. Method only exists for extensibility (e.g. flak cannons, etc.) where baseline consumption might be higher and/or dynamic
	}

	public float getAdditionalCapacityUsedPerDamage() {
		return CannonElementManager.ADDITIONAL_CAPACITY_USED_PER_DAMAGE;
	}
}