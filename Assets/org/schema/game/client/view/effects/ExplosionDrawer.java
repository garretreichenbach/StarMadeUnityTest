package org.schema.game.client.view.effects;

import javax.vecmath.Vector3f;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.FrameBufferObjects;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.particle.explosion.particle.ParticleExplosionController;
import org.schema.schine.graphicsengine.forms.particle.explosion.particle.ParticleExplosionDrawer;
import org.schema.schine.graphicsengine.forms.particle.explosion.point.ParticleExplosionPointController;
import org.schema.schine.graphicsengine.forms.particle.explosion.point.ParticleExplosionPointDrawer;

public class ExplosionDrawer implements Drawable {

	private ParticleExplosionController particleSystemSprite;
	private ParticleExplosionDrawer particleSystemDrawerSprite;
	
	private ParticleExplosionPointController particleSystemBall;
	private ParticleExplosionPointDrawer particleSystemDrawerBall;

	private ParticleShieldExplosionPointController particleSystemShieldBall;
	private ParticleShieldExplosionPointDrawer particleSystemDrawerShieldBall;
	private boolean firstDraw = true;

	public ExplosionDrawer() {
		particleSystemSprite = new ParticleExplosionController(false);
		particleSystemDrawerSprite = new ParticleExplosionDrawer(particleSystemSprite);
		particleSystemBall = new ParticleExplosionPointController(false);
		particleSystemDrawerBall = new ParticleExplosionPointDrawer(particleSystemBall);

		particleSystemShieldBall = new ParticleShieldExplosionPointController(false);
		particleSystemDrawerShieldBall = new ParticleShieldExplosionPointDrawer(particleSystemShieldBall);
	}

	public void addExplosion(Vector3f hitPointWorld) {
		long weaponId = Long.MIN_VALUE; 
		particleSystemSprite.addExplosion(new Vector3f(hitPointWorld), Controller.getCamera().getPos(), 4, 3, weaponId);
		particleSystemBall.addExplosion(new Vector3f(hitPointWorld), Controller.getCamera().getPos(), 4, 3, 4f, weaponId);
	}

	public void addExplosion(Vector3f where, float size) {
		long weaponId = Long.MIN_VALUE; 
		particleSystemSprite.addExplosion(new Vector3f(where), Controller.getCamera().getPos(), size, 3, weaponId);
		particleSystemBall.addExplosion(new Vector3f(where), Controller.getCamera().getPos(), size, 3, 4f, weaponId);
	}
	public void addExplosion(Vector3f where, float size, int count, float dmg) {
		long weaponId = Long.MIN_VALUE; 
		particleSystemSprite.addExplosion(new Vector3f(where), Controller.getCamera().getPos(), size, count, weaponId);
		particleSystemBall.addExplosion(new Vector3f(where), Controller.getCamera().getPos(), size, count, dmg, weaponId);
//		particleSystemShieldBall.addExplosion(new Vector3f(where), Controller.getCamera().getPos(), size, count, dmg, weaponId);
	}
	public void addShieldBubbleHit(Vector3f origin, float percent) {
		particleSystemShieldBall.addExplosion(origin, Controller.getCamera().getPos(), percent);		
	}
	public void addExplosion(Vector3f where, float size, long weaponType) {
		particleSystemSprite.addExplosion(new Vector3f(where), Controller.getCamera().getPos(), size, 3, weaponType);
		particleSystemBall.addExplosion(new Vector3f(where), Controller.getCamera().getPos(), size, 3, 5f, weaponType);
	}

	@Override
	public void cleanUp() {

	}
	public void draw(FrameBufferObjects foregroundFbo, FrameBufferObjects fbo, DepthBufferScene depthBuffer, float near, float far) {
		if (firstDraw) {
			onInit();
		}
		particleSystemDrawerSprite.draw(foregroundFbo, fbo, depthBuffer.getDepthTextureId(), near, far, 3);
	}
	
	public void drawShieldBubbled() {
		if (firstDraw) {
			onInit();
		}
		particleSystemDrawerShieldBall.draw();
	}
	public void drawPoints() {
		if (firstDraw) {
			onInit();
		}
		particleSystemDrawerBall.draw();
	}
	@Override
	public void draw() {
		if (firstDraw) {
			onInit();
		}
		particleSystemDrawerSprite.draw();
		
		

	}

	@Override
	public boolean isInvisible() {

		return false;
	}

	@Override
	public void onInit() {
		particleSystemDrawerSprite.onInit();
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

	public void update(Timer timer) {
		particleSystemBall.update(timer);
		particleSystemSprite.update(timer);
		particleSystemShieldBall.update(timer);
	}

	

}
