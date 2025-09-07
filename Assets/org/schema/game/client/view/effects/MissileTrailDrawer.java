package org.schema.game.client.view.effects;

import javax.vecmath.Vector3f;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.missile.Missile;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.particle.simple.ParticleSimpleDrawer;
import org.schema.schine.graphicsengine.forms.particle.simple.ParticleSimpleDrawerMissile;
import org.schema.schine.graphicsengine.forms.particle.trail.ParticleTrailDrawer;

import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;

public class MissileTrailDrawer implements Drawable {

	public static final int MAX_TRAILS = EngineSettings.G_MAX_MISSILE_TRAILS.getInt();
	private final ObjectArrayFIFOQueue<AddRemoveTrail> trailsToAddOrRemove = new ObjectArrayFIFOQueue<AddRemoveTrail>();
	float maxSpeed = 0.2f;
	Vector3f lasPos = new Vector3f();
	private MissileTrailController[] particleSystem;
	private ParticleTrailDrawer particleSystemDrawer;
	private MissileHeadEffectController particleMissileHeadController;
	private ParticleSimpleDrawer particleMissileDrawer;
	private boolean firstDraw = true;
	private GameClientState state;

	public MissileTrailDrawer(GameClientState state) {
		this.state = state;
		particleSystem = new MissileTrailController[MAX_TRAILS];
		for (int i = 0; i < MAX_TRAILS; i++) {
			particleSystem[i] = new MissileTrailController(null, this);
		}

		particleSystemDrawer = new ParticleTrailDrawer(null);

		particleMissileHeadController = new MissileHeadEffectController(false, state.getController().getClientMissileManager());
		particleMissileDrawer = new ParticleSimpleDrawerMissile(particleMissileHeadController, 16);

	}

	private void addTrail(Missile target) {
		for (int i = 0; i < MAX_TRAILS; i++) {
			if (!particleSystem[i].isAlive()) {
//				System.err.println("[MISSILE] STARTING NEW TRAIL "+target);
				particleSystem[i].startTrail(target);
				particleMissileHeadController.add(target);
				break;
			}
		}
	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void draw() {
		if (firstDraw) {
			onInit();
		}
		particleMissileDrawer.draw();

		for (int i = 0; i < MAX_TRAILS; i++) {
			if (particleSystem[i].isAlive()) {
				particleSystemDrawer.setParticleController(particleSystem[i]);
				particleSystemDrawer.draw();
			}
		}

	}

	@Override
	public boolean isInvisible() {

		return false;
	}

	@Override
	public void onInit() {
		particleSystemDrawer.onInit();
		firstDraw = false;
	}

	//	private void drawMissile(MissileTrailController missileTrailController) {
	//		Sprite p = Controller.getResLoader().getSprite("starSprite");
	//		p.setBillboard(true);
	//		p.getPos().set(missileTrailController.getTarget().origin);
	//		p.draw();
	//
	//	}
	public void endTrail(Missile target) {
		synchronized (trailsToAddOrRemove) {
			trailsToAddOrRemove.enqueue(new AddRemoveTrail(target, AddRemoveTrail.REMOVE));
		}
	}

	private void removeTrail(Missile target) {
		boolean found = false;
		for (int i = 0; i < MAX_TRAILS; i++) {
			if (particleSystem[i].isAlive() && !particleSystem[i].letItEnd && particleSystem[i].getMissile() == target) {
				particleSystem[i].endTrail();
				state.getWorldDrawer().getExplosionDrawer().addExplosion(particleSystem[i].getMissile().getWorldTransformOnClient().origin, 50, particleSystem[i].getMissile().getWeaponId());
				//				particleMissileHeadController.remove(i);
				found = true;
//				System.err.println("[CLIENT][MISSILETRAIL] removeing "+target+"");
				break;
			}
		}
		if (!found) {
//			System.err.println("[CLIENT][MISSILETRAIL] cannot remove "+target+" -> not found");
		}

	}

	public void startTrail(Missile target) {
		synchronized (trailsToAddOrRemove) {
			trailsToAddOrRemove.enqueue(new AddRemoveTrail(target, AddRemoveTrail.ADD));
		}
	}

	public void translateTrail(Missile target) {
		synchronized (trailsToAddOrRemove) {
			trailsToAddOrRemove.enqueue(new AddRemoveTrail(target, AddRemoveTrail.TRANSLATE));
		}
	}

	public void update(Timer timer) {

		synchronized (trailsToAddOrRemove) {
			while (!trailsToAddOrRemove.isEmpty()) {
				AddRemoveTrail trail = trailsToAddOrRemove.dequeue();
				if (trail.mode == AddRemoveTrail.ADD) {
					addTrail(trail.t);
				} else if (trail.mode == AddRemoveTrail.REMOVE) {
					removeTrail(trail.t);
				} else if (trail.mode == AddRemoveTrail.TRANSLATE) {
					//					translateTrailPosition(trail.t);
				} else {
					throw new IllegalArgumentException("Unknown mode: " + trail.mode);
				}
			}
		}

		int active = 0;
		for (int i = 0; i < MAX_TRAILS; i++) {
			if (particleSystem[i].isAlive()) {
				particleSystem[i].update(timer);
				active++;
			}
		}

		lasPos.set(Controller.getCamera().getPos());

		//		AbstractScene.infoList.add("MISSILES "+missiles+" | "+active+"/"+MAX_TRAILS);
		if (particleSystemDrawer != null) {
			particleSystemDrawer.update(timer);
		}

		particleMissileHeadController.update(timer);

	}

	//	private Vector3f helpA = new Vector3f();
	//	private Vector3f helpB = new Vector3f();
	//	@Override
	//	public int compareTo(ExplosionDrawer o) {
	//		Vector3f cam = Controller.getCamera().getPos();
	//		helpA.set(cam);
	//		helpB.set(cam);
	//		helpA.sub(result.hitNormalWorld);
	//		helpB.sub(o.result.hitNormalWorld);
	//		float a = helpA.length();
	//		float b = helpB.length();
	//		int i = (int)(a-b);
	//		return i;
	//
	//	}

	private class AddRemoveTrail {
		public static final int ADD = 0;
		public static final int REMOVE = 1;
		public static final int TRANSLATE = 2;
		Missile t;
		int mode;

		public AddRemoveTrail(Missile t, int add) {
			super();
			this.t = t;
			this.mode = add;
		}

	}

}
