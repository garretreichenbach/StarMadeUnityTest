package org.schema.game.common.data.blockeffects;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.elements.power.PowerAddOn;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.schine.graphicsengine.core.Timer;

public abstract class StatusBlockEffect extends BlockEffect {
	private final int blockCount;
	private long idServer;
	private float effectCap;
	private float powerConsumption;
	private float basicMultiplier;

	public StatusBlockEffect(SendableSegmentController controller,
	                         BlockEffectTypes type, int blockCount, long idServer, float effectCap, float powerConsumption, float basicMultiplier) {
		super(controller, type);
		this.blockCount = blockCount;
		this.effectCap = effectCap;
		this.idServer = idServer;
		this.powerConsumption = powerConsumption;
		this.basicMultiplier = basicMultiplier;
	}

	@Override
	public void update(Timer timer, FastSegmentControllerStatus status) {
		PowerAddOn powerAddOn = ((PowerManagerInterface) ((ManagedSegmentController<?>) segmentController).getManagerContainer()).getPowerAddOn();

		double consumePower = powerAddOn.consumePower(blockCount * powerConsumption, timer);
		if (consumePower == 0) {
			if (segmentController.isOnServer()) {
				//			end();
			}
//			System.err.println(segmentController.getState()+" (blocks: "+blockCount+" CANNOT CONSUME: current power: "+powerAddOn.getPower()+"; needed: "+(getBlockCount() * getBasePoserConsumption() * timer.getDelta()));
		} else {
			if (isAlive() && blockCount > 0) {
				setRatio(status, Math.min(getMax(), (blockCount) / (segmentController.getMassWithDocks()* VoidElementManager.DEVENSIVE_EFFECT_MAX_PERCENT_MASS_MULT)));
			}
		}

		if (segmentController.isOnServer()) {
			SegmentPiece pointUnsave = segmentController.getSegmentBuffer().getPointUnsave(ElementCollection.getPosIndexFrom4(idServer));
			if (pointUnsave != null && pointUnsave.getAbsoluteIndexWithType4() != idServer) {
				end();
				System.err.println("[SERVER][EFFECT] " + this + " ended because effect block has been removed");
			}
		}
	}

	@Override
	public boolean needsDeadUpdate() {
		return true;
	}

	protected float getMax() {
		return 1;
	}

	public abstract void setRatio(FastSegmentControllerStatus status, float ratio);

	/**
	 * @return the blockCount
	 */
	public int getBlockCount() {
		return blockCount;
	}

	public final float getBasePoserConsumption() {
		return powerConsumption;
	}

	public final float getBaseMultiplier() {
		return basicMultiplier;
	}

	public abstract float getRatio(FastSegmentControllerStatus status);

	/**
	 * @return the pos
	 */
	public long getPos() {
		return idServer;
	}

	/**
	 * @param pos the pos to set
	 */
	public void setPos(long pos) {
		this.idServer = pos;
	}

	/**
	 * @return the effectCap
	 */
	public float getEffectCap() {
		return effectCap;
	}

	/**
	 * @return the powerConsumption
	 */
	public float getPowerConsumption() {
		return powerConsumption;
	}

}
