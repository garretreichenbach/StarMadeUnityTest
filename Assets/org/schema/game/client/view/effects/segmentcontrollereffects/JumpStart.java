package org.schema.game.client.view.effects.segmentcontrollereffects;

import javax.vecmath.Vector3f;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.cubes.CubeMeshInterface;
import org.schema.game.client.view.shader.JumpOverlayShader;
import org.schema.game.common.controller.SegmentController;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.util.timer.SinusTimerUtil;

import com.bulletphysics.linearmath.Transform;

public class JumpStart extends RunningEffect {

	private static final float SECS_TILL_JUMP = 1.5f;
	private static final long LIFE_TIME = 5000;
	private static final long FIRST_PUSH_ANIM_TIME = 400;
	private static final long SECOND_PUSH_ANIM_TIME = 400;
	private static final float SECS_TILL_JUMP_PUSH = 3.0f;
	SinusTimerUtil aSin = new SinusTimerUtil();
	SinusTimerUtil bSin = new SinusTimerUtil();
	Vector3f tmp = new Vector3f();
	private JumpOverlayShader shader;
	private float timeLived;
	private Vector3f forward = new Vector3f();
	private Vector3f forwardLocal = new Vector3f(0, 0, 1);
	private Vector3f forwardScaled = new Vector3f();
	private Vector3f forwardLocalScaled = new Vector3f();
	private Vector3f forwardTotal = new Vector3f();
	private Vector3f forwardLocalTotal = new Vector3f();
	private long firstPushInitialized;
	private long secondPushInitialized;
	private JumpFlare flare1;
	private JumpFlare flare2;
	private Transform cameraTransUpdate = new Transform();
	private Transform cameraTrans = new Transform();
	private Transform cameraTransInv = new Transform();
	private TunnelEffect tunnel;

	public JumpStart(SegmentController segmentController,
	                 long timeStarted) {
		super(segmentController, SegConEffects.JUMP_START, timeStarted);

		shader = new JumpOverlayShader();

		GlUtil.getForwardVector(forward, segmentController.getWorldTransform());

		cameraTrans.set(Controller.getCamera().getWorldTransform());
		cameraTrans.origin.set(Controller.getCamera().getPos());
		cameraTransInv.set(Controller.getCamera().getWorldTransform());
		cameraTransInv.origin.set(Controller.getCamera().getPos());
		cameraTransInv.inverse();

		flare1 = new JumpFlare();
		flare2 = new JumpFlare();

		flare1.onInit();
		flare2.onInit();

		aSin.setSpeed(3000f / FIRST_PUSH_ANIM_TIME);
		bSin.setSpeed(3000f / SECOND_PUSH_ANIM_TIME);

		tunnel = new TunnelEffect(segmentController);
		tunnel.onInit();
		tunnel.setTransform(segmentController.getWorldTransform());
		SegmentControllerEffectDrawer.spaceParticleDrawer.reset();
	}

	@Override
	public boolean isDrawOriginal() {
		return true;
	}

	@Override
	public void update(Timer timer) {
		timeLived += timer.getDelta();

		//blend in
		shader.minAlpha = Math.min(0.95f, shader.minAlpha + timer.getDelta() * 1.4f);
		shader.m_time = timeLived;

		float speed = 300;

		if (timeLived > SECS_TILL_JUMP_PUSH) {
			speed = 10000f;
			bSin.update(timer);
		}

		if (timeLived > SECS_TILL_JUMP) {
			forwardScaled.set(forward);
			forwardScaled.scale(speed * timer.getDelta());
			forwardTotal.add(forwardScaled);

			forwardLocalScaled.set(forwardLocal);
			forwardLocalScaled.scale(speed * timer.getDelta());
			forwardLocalTotal.add(forwardLocalScaled);
			aSin.update(timer);

			Vector3f forw = new Vector3f(forwardTotal);

			cameraTransUpdate.set(Controller.getCamera().getWorldTransform());
			cameraTransUpdate.origin.set(Controller.getCamera().getPos());

			cameraTransUpdate.basis.mul(cameraTransInv.basis);

			cameraTransUpdate.basis.transform(forw);

			Transform t = new Transform();
			t.set(Controller.getCamera().getWorldTransform());
//			t.basis.mul(cameraTransUpdate.basis);
			t.origin.set(Controller.getCamera().getPos());
			t.origin.add(forwardTotal);

			SegmentControllerEffectDrawer.spaceParticleDrawer.update(timer, t, speed, forw);
		}
		if (timeLived > SECS_TILL_JUMP_PUSH) {
			tunnel.update(timer);
		}

		if (firstPushInitialized == 0 && timeLived > SECS_TILL_JUMP) {
			firstPushInitialized = System.currentTimeMillis();
			assert (flare1.pos.lengthSquared() == 0);

			flare1.pos.set(segmentController.getWorldTransform().origin);
			flare1.pos.add(forwardTotal);
		}

		if (secondPushInitialized == 0 && timeLived > SECS_TILL_JUMP_PUSH) {
			secondPushInitialized = System.currentTimeMillis();
			assert (flare2.pos.lengthSquared() == 0);

			flare2.pos.set(segmentController.getWorldTransform().origin);
			flare2.pos.add(forwardTotal);

		}
	}

	@Override
	public boolean isAlive() {
		return System.currentTimeMillis() - timeStarted < LIFE_TIME;
	}

	@Override
	public void loadShader() {
		ShaderLibrary.jumpOverlayShader.setShaderInterface(shader);
		ShaderLibrary.jumpOverlayShader.load();
	}

	@Override
	public void unloadShader() {

		ShaderLibrary.jumpOverlayShader.unload();
	}

	@Override
	public void drawInsideEffect() {

		SegmentControllerEffectDrawer.spaceParticleDrawer.updateCam();

		if (timeLived > SECS_TILL_JUMP_PUSH) {
			tunnel.draw();
		}

		SegmentControllerEffectDrawer.unaffectedTranslation = new Vector3f(forwardTotal);

		cameraTransUpdate.set(Controller.getCamera().getWorldTransform());
		cameraTransUpdate.origin.set(Controller.getCamera().getPos());

		cameraTransUpdate.basis.mul(cameraTransInv.basis);

		GlUtil.glPushMatrix();
		SegmentControllerEffectDrawer.unaffectedTranslation.negate();
		GlUtil.translateModelview(SegmentControllerEffectDrawer.unaffectedTranslation);
		SegmentControllerEffectDrawer.spaceParticleDrawer.draw();
		SegmentControllerEffectDrawer.unaffectedTranslation.negate();
		GlUtil.glPopMatrix();

		cameraTransUpdate.basis.transform(SegmentControllerEffectDrawer.unaffectedTranslation);

		SegmentControllerEffectDrawer.unaffectedTranslation.negate();

		GlUtil.translateModelview(SegmentControllerEffectDrawer.unaffectedTranslation);

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
		if (firstPushInitialized > 0 && System.currentTimeMillis() - firstPushInitialized < FIRST_PUSH_ANIM_TIME) {
			flare1.extraScale = aSin.getTime() * 0.05f;
			flare1.draw();
		}

		if (secondPushInitialized > 0 && System.currentTimeMillis() - secondPushInitialized < SECOND_PUSH_ANIM_TIME) {
			flare2.extraScale = bSin.getTime() * 0.3f;
			flare2.draw();

		}
	}

	@Override
	public void modifyModelview(GameClientState state) {
		if (state.getCurrentPlayerObject() == segmentController ||
				(state.getCharacter() != null && state.getCharacter().getGravity().source == segmentController) ||
				(state.getCurrentPlayerObject() instanceof SegmentController && ((SegmentController) state.getCurrentPlayerObject()).getDockingController().isInAnyDockingRelation(segmentController))) {
			tmp.set(forwardLocalTotal);
			//			tmp.negate();
			GlUtil.translateModelview(tmp);
		} else {
			GlUtil.translateModelview(forwardLocalTotal);
		}
	}

	@Override
	public int overlayBlendMode() {
		return CubeMeshInterface.OPAQUE;
	}

}
