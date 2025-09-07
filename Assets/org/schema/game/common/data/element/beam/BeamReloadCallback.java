package org.schema.game.common.data.element.beam;

public interface BeamReloadCallback {
	public void setShotReloading(long reload);

	public boolean canUse(long curTime, boolean popupText);

	public boolean isInitializing(long curTime);

	public long getNextShoot();

	public long getCurrentReloadTime();

	public boolean consumePower(float powerConsumtionDelta);

	public boolean canConsumePower(float powerConsumtionDelta);

	public double getPower();

	public boolean isUsingPowerReactors();

	public void flagBeamFiredWithoutTimeout();
}
