package org.schema.game.client.view.effects;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.MainGameGraphics;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.particle.simple.ParticleSimpleDrawerFlare;

import com.bulletphysics.linearmath.Transform;

public class FlareDrawer implements Drawable, BloomEffectInterface {

	private static final float DEFAULT_SPRITE_SIZE = 8;
	private static final float BLOOM_SPRITE_SIZE = 3.0f;
	ManagedSegmentController<?> controller;
	Vector3i tmp = new Vector3i();
	private ParticleSimpleDrawerFlare particleDrawer;
	private ShipFlareParticleController particleController;
	private boolean firstDraw = true;
	private Vector3f posTmp = new Vector3f();

	public FlareDrawer(ManagedSegmentController<?> s, FlareDrawerManager manager) {
		set(s);
		particleController = new ShipFlareParticleController(false);
		particleDrawer = new ParticleSimpleDrawerFlare(particleController, 8);
	}

	public void addFlare(SegmentPiece s) {
		int p = particleController.getParticleCount();

		particleController.addFlare(s.getAbsolutePos(tmp));

	}

	@Override
	public void cleanUp() {
		particleController.reset();
	}

	@Override
	public void draw() {
		if (firstDraw) {
			onInit();
		}
		if(MainGameGraphics.drawBloomedEffects()){
			return;
		}
		particleDrawer.setSpriteSize(DEFAULT_SPRITE_SIZE);
		GlUtil.glPushMatrix();
		GlUtil.glMultMatrix(controller.getSegmentController().getWorldTransformOnClient());

		particleDrawer.currentSystemTransform.set(controller.getSegmentController().getWorldTransformOnClient());
		particleDrawer.draw();
		GlUtil.glPopMatrix();
	}
	@Override
	public void drawRaw(){
		if (firstDraw) {
			onInit();
		}
		particleDrawer.setSpriteSize(BLOOM_SPRITE_SIZE);
		GlUtil.glPushMatrix();
		GlUtil.glMultMatrix(controller.getSegmentController().getWorldTransformOnClient());

		particleDrawer.currentSystemTransform.set(controller.getSegmentController().getWorldTransformOnClient());
		particleDrawer.drawRaw();
		GlUtil.glPopMatrix();
	}
	@Override
	public boolean isInvisible() {
				return false;
	}

	@Override
	public void onInit() {
		if (particleDrawer.currentSystemTransform == null) {
			particleDrawer.currentSystemTransform = new Transform();
		}
		particleDrawer.onInit();

		firstDraw = false;

	}

	public void clear(Segment s) {
		/*
		 * remove all particles of this segment
		 */
		for (int i = 0; i < particleController.getParticleCount(); i++) {
			particleController.getParticles().getPos(i, posTmp);

			//			System.err.println("checking "+posTmp+" against "+s.pos);
			if (s.pos.x <= posTmp.x + SegmentData.SEG_HALF && s.pos.y <= posTmp.y + SegmentData.SEG_HALF && s.pos.z <= posTmp.z + SegmentData.SEG_HALF &&
					s.pos.x + SegmentData.SEG > posTmp.x + SegmentData.SEG_HALF && s.pos.y + SegmentData.SEG > posTmp.y + SegmentData.SEG_HALF 
					&& s.pos.z + SegmentData.SEG > posTmp.z + SegmentData.SEG_HALF) {
				particleController.deleteParticle(i);
				//				System.err.println("JJJAA");
				i--;
			} else {
				//				System.err.println("FAA");
			}
		}
		//		System.err.println("LEFT;: "+particleController.getParticleCount());
	}

	public boolean containsFlares() {
		return particleController.getParticleCount() > 0;
	}

	public void refillBuffer() {

	}

	public void set(ManagedSegmentController<?> s) {
		this.controller = s;
	}

	public void unset() {
		controller = null;
	}
}
