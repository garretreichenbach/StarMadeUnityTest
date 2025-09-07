package org.schema.game.client.view.effects.segmentcontrollereffects;

import javax.vecmath.Vector3f;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.Timer;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

public class SegmentControllerEffectDrawer implements Drawable {

	public static SpaceParticleHyperSpaceDrawer spaceParticleDrawer;
	public static Vector3f unaffectedTranslation;
	private final Object2ObjectOpenHashMap<SegmentController, RunningEffect> effects = new Object2ObjectOpenHashMap<SegmentController, RunningEffect>();
	private final GameClientState state;

	public SegmentControllerEffectDrawer(GameClientState state) {
		super();
		this.state = state;
		spaceParticleDrawer = new SpaceParticleHyperSpaceDrawer();
	}

	public void update(Timer timer) {
		ObjectIterator<RunningEffect> iterator = effects.values().iterator();
		while (iterator.hasNext()) {
			RunningEffect next = iterator.next();
			next.update(timer);
			if (!next.isAlive()) {
				iterator.remove();
			}
		}

	}

	@Override
	public void cleanUp() {
		
	}

	@Override
	public void draw() {
		for (RunningEffect e : effects.values()) {
			if (state.getCurrentPlayerObject() == e.segmentController ||
					(state.getCharacter() != null && state.getCharacter().getGravity().source == e.segmentController) ||
					(state.getCurrentPlayerObject() instanceof SegmentController && ((SegmentController) state.getCurrentPlayerObject()).getDockingController().isInAnyDockingRelation(e.segmentController))) {
				e.drawInsideEffect();
			}
			//particles etc for all
			e.drawOutsideEffect();

		}
	}

	@Override
	public boolean isInvisible() {
				return false;
	}

	@Override
	public void onInit() {
		spaceParticleDrawer.onInit();
	}

	public void startEffect(SegmentController segmentController, byte type) {
		segmentController = segmentController.railController.getRoot();
		if (type > 0 && type - 1 < SegConEffects.values().length) {
			SegConEffects segConEffects = SegConEffects.values()[type - 1];
			
			effects.put(segmentController, RunningEffect.getInstance(segmentController, segConEffects, System.currentTimeMillis()));

		} else {
			System.err.println("[CLIENT][SegConEffect][ERROR] effect unknown: " + type);
		}

	}

	public RunningEffect getEffect(SegmentController segmentController) {
		return effects.get(segmentController.railController.getRoot());
	}

	/**
	 * @return the state
	 */
	public GameClientState getState() {
		return state;
	}

}
