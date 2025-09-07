package org.schema.schine.graphicsengine.psys;

import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Transformable;
import org.schema.schine.network.objects.container.TransformTimed;
import org.schema.schine.physics.Physics;

public class ParticleSystem implements Transformable, Drawable {

	private final ParticleSystemConfiguration config;
	private final Transformable transformable;
	private final ParticleContainer tmp = new ParticleContainer();
	private final ParticleVertexBuffer buffer = new ParticleVertexBuffer();
	private float[] rawParticles;
	private boolean running;
	private boolean started;
	private boolean stopped;

	public ParticleSystem(ParticleSystemConfiguration config, Transformable transformable) {
		this.config = config;
		this.transformable = transformable;
		rawParticles = new float[config.getMaxParticles() * ParticleProperty.getPropertyCount()];
	}

	public void resize() {

		float[] old = rawParticles;
		int oldSize = old.length / ParticleProperty.getPropertyCount();
		rawParticles = new float[config.getMaxParticles() * ParticleProperty.getPropertyCount()];
		for (int i = 0; i < old.length && i < rawParticles.length; i++) {
			rawParticles[i] = old[i];
		}

		if (config.getMaxParticles() < oldSize) {
			//if we are now smaller, kill alive particles out of bounds
			config.setParticleCount(Math.min(config.getParticleCount(), config.getMaxParticles()));
		}
	}

	public void init() {

	}

	public void start() {
		this.started = (true);
		this.running = (true);
		config.getRandom().setSeed(config.getSeed());
		if (config.getParticleSystemDuration() <= 0.0f) {

			this.started = (false);
			this.running = (false);
		}
		config.start();
	}

	public void stop() {
		this.stopped = true;
		this.started = (false);
		this.running = (false);
	}

	public void pause() {
		this.running = !this.running;
	}

	public void update(Physics physics, Timer timer) {
		if (started && running) {
			spawnPhase(timer);
			updatePhase(physics, timer);
		}
	}

	private void spawnPhase(Timer timer) {
		boolean spawn = config.spawn(timer, rawParticles, getWorldTransform());
		while (!spawn) {

			resize();
			spawn = config.spawn(timer, rawParticles, getWorldTransform());

		}
	}

	private void updatePhase(Physics physics, Timer timer) {
		int particles = config.getParticleCount();

		for (int i = 0; i < particles; i++) {

			Particle.getParticle(i, rawParticles, tmp);

			config.handleParticleUpdate(physics, timer, tmp);

			if (tmp.lifetime <= 0) {
				Particle.remove(i, config, rawParticles);
				particles--;
			} else {

				Particle.setParticle(i, rawParticles, tmp);
			}
		}
	}

	public void sort(int count) {
		ParticleSort p = new ParticleSort(rawParticles, count);
		Particle.quickSort(p);
	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void draw() {
		config.draw(this, buffer);
	}

	@Override
	public boolean isInvisible() {
		return false;
	}

	@Override
	public void onInit() {

	}

	@Override
	public TransformTimed getWorldTransform() {
		return transformable.getWorldTransform();
	}

	public Transformable getTransformable() {
		return transformable;
	}

	public boolean isStarted() {
		return started;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isCompletelyDead() {
		return stopped;
	}

	public int getParticleCount() {
		return config.getParticleCount();
	}

	public float[] getRawParticles() {
		return rawParticles;
	}

	public void getColor(Vector4f color, ParticleContainer particleContainer) {
		config.getColor(color, particleContainer);
	}

}
