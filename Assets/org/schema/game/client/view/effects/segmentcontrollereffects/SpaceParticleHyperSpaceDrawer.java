package org.schema.game.client.view.effects.segmentcontrollereffects;

import javax.vecmath.Vector3f;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.particle.movingeffect.ParticleMovingEffectController;
import org.schema.schine.graphicsengine.forms.particle.movingeffect.ParticleMovingEffectDrawer;

import com.bulletphysics.linearmath.Transform;

public class SpaceParticleHyperSpaceDrawer implements Drawable {

	public Transform currentCamera;
	protected ParticleMovingEffectController particleSystem;
	protected ParticleMovingEffectDrawer particleSystemDrawer;
	float maxSpeed = 0.2f;

	Vector3f dist = new Vector3f();
	Vector3f lasPos = new Vector3f();
	float distance = 0;
	private boolean firstDraw = true;
	private Vector3f forward = new Vector3f();

	public SpaceParticleHyperSpaceDrawer() {
		currentCamera = new Transform();
		currentCamera.setIdentity();

		particleSystem = new ParticleMovingEffectController(false, 1024) {

			@Override
			public boolean updateParticle(int i, Timer timer) {

				Transform wt = getCamera();

				float sc = 3f;
				float lived = getParticles().getLifetime(i);

				getParticles().setLifetime(i, (float) (lived + sc * timer.getDelta() * 1000.0));

				return lived < 300;
			}

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.particle.movingeffect.ParticleMovingEffectController#getCamera()
			 */
			@Override
			public Transform getCamera() {

				return currentCamera;
			}
		};
		particleSystem.spawnCount = 10;
		particleSystemDrawer = new ParticleMovingEffectDrawer(particleSystem, 0.3f);
		particleSystemDrawer.smear = 0.03f;
		particleSystemDrawer.frustumCulling = false;
	}

	public void updateCam() {
		Controller.getMat(Controller.modelviewMatrix, currentCamera);
		//		currentCamera.set(Controller.getCamera().getWorldTransform());
	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void draw() {
		if (firstDraw) {
			onInit();
		}
		particleSystemDrawer.draw();

		//			System.err.println("SPACE PARTICLES: "+particleSystem.getParticleCount());
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

	public void onPlanet(boolean inside) {
	}

	public void update(Timer timer, Transform where, float speed, Vector3f forward) {
		particleSystem.updateEffectFromCam(where, speed);
		distance = 0;
		this.forward.set(forward);

		particleSystem.update(timer);
	}

	public void reset() {
		particleSystem.reset();
		particleSystemDrawer.reset();
	}

}
