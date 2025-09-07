package org.schema.game.client.view.effects.segmentcontrollereffects;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.schine.graphicsengine.core.Timer;

public abstract class RunningEffect {
	public final SegmentController segmentController;
	public final SegConEffects type;
	public final long timeStarted;

	public RunningEffect(SegmentController segmentController,
	                     SegConEffects type, long timeStarted) {
		super();
		this.segmentController = segmentController;
		this.type = type;
		this.timeStarted = timeStarted;
	}

	public static RunningEffect getInstance(SegmentController segmentController,
	                                        SegConEffects type, long timeStarted) {

		RunningEffect e = switch(type) {
			case JUMP_END -> new JumpEnd(segmentController, timeStarted);
			case JUMP_START -> new JumpStart(segmentController, timeStarted);
			case TEST -> new TestEffect(segmentController, timeStarted);
		};
		assert (e.type == type);

		return e;

	}

	public abstract boolean isDrawOriginal();

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return segmentController.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return ((RunningEffect) obj).segmentController == segmentController;
	}

	public abstract void update(Timer timer);

	public abstract boolean isAlive();

	public abstract void loadShader();

	public abstract void unloadShader();

	public abstract void drawInsideEffect();

	public abstract void drawOutsideEffect();

	public abstract void modifyModelview(GameClientState state);

	public abstract int overlayBlendMode();

}
