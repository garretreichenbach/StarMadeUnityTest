package org.schema.game.common.controller.elements.power.reactor;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.schine.graphicsengine.core.Timer;

public class RailPowerConsumer implements PowerConsumer{

	private final ManagerContainer<?> man;
	private final SegmentController segmentController;
	private float dockedPowered;

	public RailPowerConsumer(ManagerContainer<?> man){
		this.man = man;
		this.segmentController = man.getSegmentController();
	}
	public boolean isDocked(){
		return segmentController.railController.isDocked();
	}
	@Override
	public double getPowerConsumedPerSecondResting() {
		return 0;
	}
	@Override
	public double getPowerConsumedPerSecondCharging() {
		return 0;
	}
	@Override
	public boolean isPowerCharging(long curTime) {
		return false;
	}

	@Override
	public void setPowered(float powered) {
		dockedPowered = powered;
	}

	@Override
	public float getPowered() {
		return dockedPowered;
	}
	@Override
	public boolean isPowerConsumerActive() {
		return !man.getSegmentController().railController.next.isEmpty();
	}
	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.DOCKS;
	}

	@Override
	public void reloadFromReactor(double secTime, Timer timer, float tickTime, boolean powerCharging, float poweredResting) {
	}
	@Override
	public String getName() {
		return "RailConsumer";
	}
	@Override
	public void dischargeFully() {
	}
}
