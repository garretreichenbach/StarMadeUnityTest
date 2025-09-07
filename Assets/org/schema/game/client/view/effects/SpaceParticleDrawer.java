package org.schema.game.client.view.effects;

import javax.vecmath.Vector3f;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.particle.movingeffect.ParticleMovingEffectController;
import org.schema.schine.graphicsengine.forms.particle.movingeffect.ParticleMovingEffectDrawer;

public class SpaceParticleDrawer implements Drawable {

	protected ParticleMovingEffectController particleSystem;
	protected ParticleMovingEffectDrawer particleSystemDrawer;
	float maxSpeed = 0.2f;
	Vector3f dist = new Vector3f();
	Vector3f lasPos = new Vector3f();
	float distance = 0;
	private boolean firstDraw = true;
	private boolean onPlanet;

	public SpaceParticleDrawer() {
		particleSystem = new ParticleMovingEffectController(false);
		particleSystemDrawer = new ParticleMovingEffectDrawer(particleSystem);

	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void draw() {
		if (EngineSettings.G_SPACE_PARTICLE.isOn()) {
			if (firstDraw) {
				onInit();
			}

			particleSystemDrawer.draw();

			//			if(EngineSettings.D_INFO_DRAW_SPACE_PARTICLE.isOn()){
			//				AbstractScene.infoList.add("SPACE PARTICLES: "+particleSystem.getParticleCount());
			//			}
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
		this.onPlanet = inside;
	}

	public void update(Timer timer) {

		if (!onPlanet) {
			dist.set(Controller.getCamera().getPos());
			dist.sub(lasPos);

			float speed = dist.length();
			distance += speed;

			if (distance > 0.05f) {
				particleSystem.updateEffectFromCam(Math.min(distance, maxSpeed), maxSpeed);
				distance = 0;
			}
		}
		particleSystem.update(timer);

		lasPos.set(Controller.getCamera().getPos());
	}

}
