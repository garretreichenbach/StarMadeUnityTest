package org.schema.game.client.view.effects.segmentcontrollereffects;

import javax.vecmath.Vector3f;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.cubes.CubeMeshInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.schine.graphicsengine.core.Timer;

public class TestEffect extends RunningEffect {

	Vector3f tmp = new Vector3f();
	private float timeLived;
	private TunnelEffect tunnel;

	public TestEffect(SegmentController segmentController,
	                  long timeStarted) {
		super(segmentController, SegConEffects.TEST, timeStarted);

		tunnel = new TunnelEffect(segmentController);
		tunnel.onInit();
		SegmentControllerEffectDrawer.spaceParticleDrawer.reset();

		tunnel.setTransform(segmentController.getWorldTransform());
	}

	@Override
	public boolean isDrawOriginal() {
		return true;
	}

	@Override
	public void update(Timer timer) {
		timeLived += timer.getDelta();

		//blend in

		float speed = 300;

		tunnel.update(timer);
	}

	@Override
	public boolean isAlive() {
		return true;
	}

	@Override
	public void loadShader() {
	}

	@Override
	public void unloadShader() {

	}

	@Override
	public void drawInsideEffect() {

		SegmentControllerEffectDrawer.spaceParticleDrawer.updateCam();

		//		if(secondPushInitialized > 0){
		tunnel.draw();
		//		}

		//		if(timeLived > SECOND_PUSH_ANIM_TIME){
		//
		//		}

		//		if(firstPushInitialized > 0 && System.currentTimeMillis() - firstPushInitialized < FIRST_PUSH_ANIM_TIME){
		//			flare1.extraScale = 0.03f;
		//			flare1.draw();
		//		}
		//
		//		if(secondPushInitialized > 0 && System.currentTimeMillis() - secondPushInitialized < SECOND_PUSH_ANIM_TIME){
		//			flare2.extraScale = 0.03f;
		//			flare2.draw();
		//		}
	}

	@Override
	public void drawOutsideEffect() {

	}

	@Override
	public void modifyModelview(GameClientState state) {

	}

	@Override
	public int overlayBlendMode() {
		return CubeMeshInterface.OPAQUE;
	}
}
