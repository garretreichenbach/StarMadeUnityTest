package org.schema.game.common.controller.elements;

import javax.vecmath.Vector4f;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.schine.graphicsengine.core.Timer;

public abstract class FiringUnit<E extends FiringUnit<E, CM, EM>, CM extends ElementCollectionManager<E, CM, EM>, EM extends UsableElementManager<E, CM, EM>> extends ElementCollection<E, CM, EM> implements PowerConsumer {

	private long nextShoot;
	private long currentReloadTime;
	private float powered;
	private double reactorReloadNeeded;
	private double reactorReloadNeededFull;
	public long consumptionCombiSignCharge = Long.MIN_VALUE;
	public long consumptionCombiSignRest = Long.MIN_VALUE;
	public double combiConsumptionCharge = 0;
	public double combiConsumptionRest = 0;

	@Override
	public void setValid(boolean valid) {
		super.setValid(valid);
	}

	@Override
	public void initialize(short clazz, CM col, SegmentController controller) {
		super.initialize(clazz, col, controller);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.element.ElementCollection#onChangeFinished()
	 */
	@Override
	public boolean onChangeFinished() {
		if(elementCollectionManager.reloadingNeeded > 0) {

			setStandardShotReloading();
			reactorReloadNeeded = Math.min(elementCollectionManager.reloadingNeeded, reactorReloadNeededFull);

		}
		if(elementCollectionManager instanceof ControlBlockElementCollectionManager<?, ?, ?>) {
			ControlBlockElementCollectionManager<?, ?, ?> c = (ControlBlockElementCollectionManager<?, ?, ?>) elementCollectionManager;
			if(c.getElementManager().getManagerContainer().isConnectionFlagged(c.getControllerIndex())) {
				nextShoot = elementCollectionManager.nextShot;
				currentReloadTime = (long) getReloadTimeMs();
				setStandardShotReloading();
			}
			elementCollectionManager.reloadingNeeded = reactorReloadNeededFull;
		}
		return super.onChangeFinished();
	}

	@Override
	public void dischargeFully() {
		setStandardShotReloading();
	}

	/**
	 * updates the significator so it is the smallest
	 * (by order x,y,z)
	 *
	 * @param v
	 */
	@Override
	protected void significatorUpdate(int x, int y, int z, int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, long index) {
		significatorUpdateZ(x, y, z, xMin, yMin, zMin, xMax, yMax, zMax, index);
	}

	/**
	 * @return the lastShoot
	 */
	public long getNextShoot() {
		return nextShoot;
	}

	public abstract float getBasePowerConsumption();

	public abstract float getPowerConsumption();

	public abstract float getPowerConsumptionWithoutEffect();

	public boolean canUse(long curTime, boolean popupText) {
//		System.err.println("CUR: "+curTime+" / "+getNextShoot()+"; reload: "+this.currentReloadTime);
		if(isUsingPowerReactors()) {
			return reactorReloadNeeded <= 0.000000001d;
		} else {
			return curTime >= nextShoot/* && curTime >= elementCollectionManager.initializationStart + (long)getInitializationTime()*/;
		}
	}

	public boolean isReloading(long curTime) {
		return !canUse(curTime, false);
	}

	public boolean isInitializing(long curTime) {
		return false;/*curTime < elementCollectionManager.initializationStart + (long)getInitializationTime();*/
	}

	public int getCombiBonus(int combiCollectionManagerSize) {
		//add percentage of total blocks of tertiary amount to distribute bonus on units. also cap bonus at size
		return Math.min(size(), (int) (((double) size() / (double) elementCollectionManager.getTotalSize()) * combiCollectionManagerSize));
	}

	public void setStandardShotReloading() {

		if(elementCollectionManager.getElementManager() instanceof UsableCombinableControllableElementManager) {
			//set shot reloading to right reload depending on if we have a combination
			setShotReloading((long) ((UsableCombinableControllableElementManager) elementCollectionManager.getElementManager()).calculateReload(this));
		} else {
			setShotReloading((long) getReloadTimeMs());
		}
//		try {
//			throw new Exception("RELOAD "+reactorReloadNeededFull);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	public abstract float getReloadTimeMs();

	public abstract float getInitializationTime();

	public void setShotReloading(long reload) {
//		try{
//		throw new Exception("SET SHOT RELOADING : "+reload);
//		}catch(Exception e){
//			e.printStackTrace();
//		}

		this.reactorReloadNeededFull = reload / 1000d;
		this.reactorReloadNeeded = this.reactorReloadNeededFull;

		this.currentReloadTime = reload;
		this.nextShoot = getSegmentController().getState().getUpdateTime() + reload;

		elementCollectionManager.nextShot = Math.max(elementCollectionManager.nextShot, (nextShoot));
		elementCollectionManager.getElementManager().nextShot = Math.max(elementCollectionManager.nextShot, (nextShoot));
	}

	/**
	 * @return the currentReloadTime
	 */
	public long getCurrentReloadTime() {
		return currentReloadTime;
	}

	public abstract float getDistanceRaw();

	public float getDistance() {
		return getConfigManager().apply(StatusEffectType.WEAPON_RANGE, getDamageType(), getDistanceRaw());
	}

	protected DamageDealerType getDamageType() {
		return DamageDealerType.GENERAL;
	}

	public boolean consumePower(float powerConsumed) {
		if(isUsingPowerReactors()) {
			return true;
		} else {
			return elementCollectionManager.getElementManager().consumePower(powerConsumed);
		}
	}

	@Override
	public boolean isUsingPowerReactors() {
		return elementCollectionManager.getElementManager().getManagerContainer().getPowerInterface().isUsingPowerReactors();
	}

	public boolean canConsumePower(float powerConsumed) {
		if(isUsingPowerReactors()) {
			return true;
		} else {
			return elementCollectionManager.getElementManager().canConsumePower(powerConsumed);
		}
	}

	public double getPower() {
		return elementCollectionManager.getElementManager().getPower();
	}

	public float getDistanceFull() {
		return getDistance();
	}

	public abstract float getFiringPower();

	private static final Vector4f defaultColor = new Vector4f(1.0f, 0.3f, 0.4f, 1.0f);

	public Vector4f getColor() {
		return elementCollectionManager instanceof ControlBlockElementCollectionManager<?, ?, ?> ? ((ControlBlockElementCollectionManager<?, ?, ?>) elementCollectionManager).getColor() : defaultColor;
	}

	@Override
	public boolean isPowerCharging(long curTime) {
		return isReloading(curTime);
	}

	@Override
	public final void setPowered(float powered) {
		this.powered = powered;
	}

	@Override
	public final float getPowered() {
		return powered;
	}

	@Override
	public void reloadFromReactor(double secTime, Timer timer, float tickTime, boolean powerCharging, float poweredResting) {
		if(canUse(timer.currentTime, false) && poweredResting < VoidElementManager.REACTOR_MODULE_DISCHARGE_MARGIN) {
			//reload if we ran out of power
			setStandardShotReloading();
			this.reactorReloadNeeded = 0.001f;
		}
//		System.err.println("POWER RELOAD:: "+getSegmentController().getState()+"; "+getSegmentController()+"; "+this.getReactorReloadNeeded()+" "+poweredResting);
		if(poweredResting < VoidElementManager.REACTOR_MODULE_DISCHARGE_MARGIN) {
			this.reactorReloadNeeded = Math.min(reactorReloadNeededFull, this.reactorReloadNeeded + (VoidElementManager.REACTOR_MODULE_DISCHARGE_MARGIN - poweredResting) * tickTime);
		} else {
			this.reactorReloadNeeded = Math.max(0, this.reactorReloadNeeded - secTime);
		}

		if(elementCollectionManager.lastReloading != timer.currentTime) {
			elementCollectionManager.reloadingNeeded = this.reactorReloadNeeded;
			elementCollectionManager.lastReloading = timer.currentTime;
		} else {
			elementCollectionManager.reloadingNeeded = Math.max(elementCollectionManager.reloadingNeeded, this.reactorReloadNeeded);
		}
	}

	public double getReactorReloadNeededFull() {
		return reactorReloadNeededFull;
	}

	public double getReactorReloadNeeded() {
		return reactorReloadNeeded;
	}

	@Override
	public boolean isPowerConsumerActive() {
		if(elementCollectionManager instanceof ControlBlockElementCollectionManager<?, ?, ?>) {
			if(((ControlBlockElementCollectionManager<?, ?, ?>) elementCollectionManager).hasCoreConnection()) {
				return true;
			} else {
				ControlBlockElementCollectionManager<?, ?, ?> c = ((ControlBlockElementCollectionManager<?, ?, ?>) elementCollectionManager);
				//not consume power if slave
				return !c.getElementManager().getManagerContainer().getSlavesAndEffects().contains(c.getControllerIndex4());
			}
		} else {
			return true;
		}
	}

	public abstract float getDamage();

	public void setReactorReloadNeeded(double reactorReloadNeeded) {
//		try {
//			throw new Exception("set reload needed "+this.reactorReloadNeeded +" -> "+reactorReloadNeeded);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		this.reactorReloadNeeded = reactorReloadNeeded;
	}

}
