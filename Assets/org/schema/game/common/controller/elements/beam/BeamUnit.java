package org.schema.game.common.controller.elements.beam;

import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.HitType;
import org.schema.game.common.controller.elements.beam.harvest.SalvageUnit;
import org.schema.game.common.data.element.CustomOutputUnit;
import org.schema.game.common.data.element.ShootContainer;
import org.schema.game.common.data.element.beam.BeamReloadCallback;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyboardMappings;

import javax.vecmath.Vector4f;

public abstract class BeamUnit<E extends BeamUnit<E, CM, EM>, CM extends BeamCollectionManager<E, CM, EM>, EM extends BeamElementManager<E, CM, EM>> extends CustomOutputUnit<E, CM, EM> implements BeamReloadCallback {

	public abstract float getBeamPowerWithoutEffect();

	public abstract float getBeamPower();

	public abstract float getBaseBeamPower();

	@Override
	public abstract float getPowerConsumption();

	public boolean isLatchOn() {
		return false;
	}

	public abstract HitType getHitType();

	@Override
	public float getReloadTimeMs() {
		return getCoolDownSec() * 1000.0F;
	}

	@Override
	public final boolean isPowerCharging(long curTime) {
		return super.isPowerCharging(curTime) || (curTime - elementCollectionManager.lastBeamFired < 300);
	}

	@Override
	public float getInitializationTime() {
		return getReloadTimeMs();
	}

	public float getMaxEffectiveRange() {
		return 1.0f;
	}

	public float getMinEffectiveRange() {
		return 0.0f;
	}

	public float getMaxEffectiveValue() {
		return 1.0f;
	}

	public float getMinEffectiveValue() {
		return 1.0f;
	}

	@Override
	protected DamageDealerType getDamageType() {
		return DamageDealerType.BEAM;
	}

	@Override
	public abstract float getDistanceRaw();

	public float getTickRate() {
		return elementCollectionManager.getElementManager().getTickRate();
	}

	public float getCoolDownSec() {
		return elementCollectionManager.getElementManager().getCoolDown();
	}

	public float getBurstTime() {
		return elementCollectionManager.getElementManager().getBurstTime();
	}

	public float getInitialTicks() {
		return elementCollectionManager.getElementManager().getInitialTicks();
	}

	@Override
	public int getEffectBonus() {
		//add percentage of total blocks of tertiary amount to distribute bonus on units. also cap bonus at size
		return Math.min(size(), (int) (((double) size() / (double) elementCollectionManager.getTotalSize()) * elementCollectionManager.getEffectTotal()));
	}

	@Override
	public float getExtraConsume() {
		return 1;
	}

	@Override
	public float getFiringPower() {
		return getBeamPower();
	}

	@Override
	public Vector4f getColor() {
		return elementCollectionManager.getColor();
	}

	@Override
	public void doShot(ControllerStateInterface unit, Timer timer, ShootContainer shootContainer) {
		Object o = this;

		boolean fire = unit.getPlayerState() != null && unit.isDown(KeyboardMappings.SHIP_PRIMARY_FIRE) && getSegmentController().isClientOwnObject() && o instanceof SalvageUnit;

		boolean focus = elementCollectionManager.isInFocusMode();
		boolean lead = false; //will only lead for AI ControllerUnit

		unit.getShootingDir(getSegmentController(), shootContainer, getDistanceFull(), 3000, elementCollectionManager.getControllerPos(), focus, lead);

		if(! isAimable()) {

			shootContainer.shootingDirTemp.set(shootContainer.shootingDirStraightTemp);
		}
		//		try {
		//			throw new Exception("SHOT");
		//		} catch (Exception e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		//								System.err.println("SHOOTING DIE: "+shootingDirTemp.length()+"; "+shootingDirTemp+": "+shootingForwardTemp.length()+"; "+shootingUpTemp.length()+"; "+shootingRightTemp.length());
		//		System.err.println("OUTPUT: "+weapontOutputWorldPos+" -> "+shootingDirTemp);

		shootContainer.shootingDirTemp.normalize();
		EM em = elementCollectionManager.getElementManager();
		CM m = elementCollectionManager;
		em.doShot((E) this, m, shootContainer, unit.getPlayerState(), unit.getBeamTimeout(), timer, fire);
	}


	public boolean isFriendlyFire() {
		return false;
	}

	public boolean isAimable() {
		return true;
	}

	public float getAcidDamagePercentage() {
		return 0;
	}

	public boolean isPenetrating() {
		return false;
	}

	public boolean isCheckLatchConnection() {
		return false;
	}

	public float calcAmmoReqsPerTick() {
		return 0;
	}

	public float getBaseCapacityUsedPerTick() {
		return 0;
	}

	public float getAdditionalCapacityUsedPerDamage() {
		return 0;
	}

	public float getAdditiveBeamPower() {
		return 0;
	}
}
