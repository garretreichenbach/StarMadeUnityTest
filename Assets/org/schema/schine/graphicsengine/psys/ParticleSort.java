package org.schema.schine.graphicsengine.psys;

public class ParticleSort implements ISortableParticles {

	final int max;
	final float[] rawArray;

	public ParticleSort(float[] rawParticles, int max) {
		this.rawArray = rawParticles;
		this.max = max;
	}

	@Override
	public float get(int i) {
		assert (i >= 0) : i;
		return ParticleProperty.getCameraDistance(i, rawArray);
	}

	@Override
	public void switchVal(int a, int b) {
		Particle.switchIndex(a, b, rawArray);
	}

	@Override
	public int getSize() {
		return max;
	}

}
