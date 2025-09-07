package org.schema.game.common.data.blockeffects;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.effectblock.EffectElementManager.OffensiveEffects;
import org.schema.game.common.data.blockeffects.updates.BlockEffectUpdate;
import org.schema.game.network.objects.remote.RemoteBlockEffectUpdate;
import org.schema.schine.graphicsengine.core.Timer;

import java.util.ArrayList;

public abstract class BlockEffect {

	protected final ArrayList<BlockEffectUpdate> pendingUpdates = new ArrayList<BlockEffectUpdate>();
	protected final ArrayList<BlockEffectUpdate> pendingBroadcastUpdates = new ArrayList<BlockEffectUpdate>();
	protected final ArrayList<BlockEffectUpdate> pendingClientUpdates = new ArrayList<BlockEffectUpdate>();
	protected final SendableSegmentController segmentController;
	private final boolean onServer;
	private final BlockEffectTypes type;
	protected long durationMS = -1;
	private short id = -3131;
	private long start;
	private long blockId = Long.MIN_VALUE;
	private long messageDisplayed;

	public BlockEffect(SendableSegmentController controller, BlockEffectTypes type) {
		start = (System.currentTimeMillis());
		this.segmentController = controller;
		onServer = segmentController.isOnServer();
		this.type = type;
		assert (type.getClazz().equals(this.getClass())) : type + "; " + this.getClass();
	}

	public void clearAllUpdates() {
		pendingUpdates.clear();
		pendingBroadcastUpdates.clear();
	}
	/**
	 * @return the maxVelocity
	 */
	public float getMaxVelocityAbsolute() {
		if (segmentController instanceof Ship) {
			return ((Ship) segmentController).getMaxSpeedAbsolute();
		} else {
			return 0;
		}
	}
    /**
     * @return the maxVelocity
     */
    public float getMaxVelocity() {
        if (segmentController instanceof Ship) {
            return ((Ship) segmentController).getCurrentMaxVelocity();
        } else {
            return 0;
        }
    }
	
	@Override
	public String toString() {
		return "("+type.getName()+"[ID: "+id+"])";
	}

	/**
	 * @return the duration
	 */
	public long getDuration() {
		return durationMS;
	}

	/**
	 * @param duration the duration to set
	 */
	public void setDuration(long duration) {
		this.durationMS = duration;
	}

	/**
	 * @return the id
	 */
	public short getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(short id) {
		this.id = id;
	}

	/**
	 * @return the start
	 */
	public long getStart() {
		return start;
	}

	/**
	 * @return the type
	 */
	public BlockEffectTypes getType() {
		return type;
	}

	public int getTypeOrd() {
		return type.ordinal();
	}

	public boolean hasPendingBroadcastUpdates() {
		return !pendingBroadcastUpdates.isEmpty();
	}

	public boolean hasPendingUpdates() {
		return !pendingUpdates.isEmpty();
	}

	public boolean isAlive() {
//				System.err.println("DIIIIIIIIIED "+segmentController+" "+segmentController.getState()+" "+this.getClass().getSimpleName()+"; s "+start+"; d "+duration);
		return durationMS == -1 || System.currentTimeMillis() - start < durationMS;
	}

	private boolean isOnServer() {
		return onServer;
	}

	public void sendPendingBroadcastUpdates(SendableSegmentController segmentController) {
		for (int i = 0; i < pendingBroadcastUpdates.size(); i++) {
			BlockEffectUpdate effectUpdate = pendingBroadcastUpdates.get(i);
			segmentController.getNetworkObject().effectUpdateBuffer.add(new RemoteBlockEffectUpdate(effectUpdate, onServer));
//			System.err.println("[SERVER] sent effect update "+effectUpdate+"; eid: "+this.getId()+"; bid: "+this.getBlockAndTypeId4());
		}
	}

	public void sendPendingUpdates(SendableSegmentController segmentController) {
		for (int i = 0; i < pendingUpdates.size(); i++) {
			BlockEffectUpdate effectUpdate = pendingUpdates.get(i);
			segmentController.getNetworkObject().effectUpdateBuffer.add(new RemoteBlockEffectUpdate(effectUpdate, onServer));
		}
	}

	public abstract void update(Timer timer, FastSegmentControllerStatus status);

	public long getBlockAndTypeId4() {
		return blockId;
	}

	public void end() {
		durationMS = 0;
	}

	/**
	 * @param blockId the blockId to set
	 */
	public void setBlockId(long blockId) {
		this.blockId = blockId;
	}

	/**
	 * Use if the effect needs a dedicated flag on the server to stop the effect
	 * <p/>
	 * no dead update is needed if the client can determine itself that the
	 * effect has ended (e.g. fix time effects)
	 *
	 * @return true if the effect should send a death update to the clients
	 */
	public abstract boolean needsDeadUpdate();

	public boolean isMessageDisplayed() {
		return System.currentTimeMillis() - messageDisplayed < 3000;
	}

	public void setMessageDisplayed(long l) {
		this.messageDisplayed = (l + (long) (Math.random() * 1000d));
	}

	public OffensiveEffects getMessage() {
		return null;
	}

	public abstract boolean affectsMother();

}
