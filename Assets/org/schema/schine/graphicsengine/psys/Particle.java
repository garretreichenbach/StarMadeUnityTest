package org.schema.schine.graphicsengine.psys;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.schine.graphicsengine.core.Controller;

public class Particle {
	private static final Vector4f pos = new Vector4f();
	private static final Vector4f res = new Vector4f();

	public static void getParticle(int index, float[] rawParticles, ParticleContainer out) {
		int indexRaw = ParticleProperty.getIndexRaw(index);
		out.angularVelocity.set(ParticleProperty.getAngularVelocity(index, rawParticles));
		out.color.set(ParticleProperty.getColor(index, rawParticles));
		out.lifetime = ParticleProperty.getLifetime(index, rawParticles);
		out.lifetimeTotal = ParticleProperty.getLifetimeTotal(index, rawParticles);
		out.position.set(ParticleProperty.getPosition(index, rawParticles));
		out.randomSeed = ParticleProperty.getRandomSeed(index, rawParticles);
		out.rotation.set(ParticleProperty.getRotation(index, rawParticles));
		out.size.set(ParticleProperty.getSize(index, rawParticles));
		out.startLifetime = ParticleProperty.getStartLifetime(index, rawParticles);
		out.velocity.set(ParticleProperty.getVelocity(index, rawParticles));
		out.camDist = ParticleProperty.getCameraDistance(index, rawParticles);
	}

	public static void setParticle(int index, float[] rawParticles, ParticleContainer in) {

		ParticleProperty.setAngularVelocity(index, rawParticles, in.angularVelocity);
		ParticleProperty.setColor(index, rawParticles, in.color);
		ParticleProperty.setLifetime(index, rawParticles, in.lifetime);
		ParticleProperty.setLifetimeTotal(index, rawParticles, in.lifetimeTotal);
		ParticleProperty.setPos(index, rawParticles, in.position);
		ParticleProperty.setRandomSeed(index, rawParticles, in.randomSeed);
		ParticleProperty.setRotation(index, rawParticles, in.rotation);
		ParticleProperty.setSize(index, rawParticles, in.size);
		ParticleProperty.setStartLifetime(index, rawParticles, in.startLifetime);
		ParticleProperty.setVelocity(index, rawParticles, in.velocity);
		ParticleProperty.setCamDist(index, rawParticles, in.camDist);
	}

	public static void switchIndex(int indexA, int indexB, float[] rawParticles) {
		assert (indexA >= 0) : indexA;
		assert (indexB >= 0) : indexB;
		int indexRawA = ParticleProperty.getIndexRaw(indexA);
		int indexRawB = ParticleProperty.getIndexRaw(indexB);
		for (int i = 0; i < ParticleProperty.getPropertyCount(); i++) {
			float tmpA = rawParticles[indexRawA + i];
			rawParticles[indexRawA + i] = rawParticles[indexRawB + i];
			rawParticles[indexRawB + i] = tmpA;
		}

	}

	public static void add(ParticleContainer p, ParticleSystemConfiguration config, float[] rawParticles) {
		setParticle(config.getParticleCount(), rawParticles, p);
		config.setParticleCount(config.getParticleCount() + 1);
	}

	public static void remove(int index, ParticleSystemConfiguration config, float[] rawParticles) {
		assert (config.getParticleCount() > 0) : config.getParticleCount();
		if (config.getParticleCount() > 0 && index < config.getParticleCount()) {
			switchIndex(index, config.getParticleCount() - 1, rawParticles);
		}
		config.setParticleCount(config.getParticleCount() - 1);
	}

	public static void updateDistance(int particleIndex, float[] rawParticles) {

		Vector3f position = ParticleProperty.getPosition(particleIndex, rawParticles);
		pos.set(position.x, position.y, position.z, 0.0F);

		Matrix4fTools.transform(Controller.modelviewMatrix, pos, res);

		ParticleProperty.setCamDist(particleIndex, rawParticles, -res.z);
	}

	static void quickSort(ISortableParticles array, int start_pos, int end_pos) {
		if (start_pos == end_pos) { // Only one element
			return;
		}
		assert (start_pos < end_pos) : start_pos + "; " + end_pos;
		int middle_pos = partition(array, start_pos, end_pos); // Reposition elements

		quickSort(array, start_pos, middle_pos); // Sort left half
		quickSort(array, middle_pos + 1, end_pos); // Sort right half
	}

	static int partition(ISortableParticles array, int start_pos, int end_pos) {
		float pivot = array.get(start_pos); // Smaller than pivot on left;
		// larger on right
		int left_index = start_pos; // First element
		int right_index = end_pos; // Last element
		assert (end_pos > 0);

		while (true) { // Loop forever; return once partitioning is completed
			// Skip over large elements on right
			while (array.get(right_index) > pivot && right_index >= start_pos) {
				right_index--;
			}

			// Skip over small elements on left
			while (array.get(left_index) < pivot && left_index <= end_pos) {
				left_index++;
			}

			if (left_index < right_index) { // Exchange if halves aren't complete
				array.switchVal(left_index, right_index);
				left_index++; // Skip over exchanged values
				right_index--;
			} else {
				// Otherwise, return location of pivot
				return right_index;
			}
		}
	}

	public static void quickSort(ISortableParticles arr) {
		if (arr.getSize() > 0) {
			quickSort(arr, 0, arr.getSize() - 1);
		}
	}
}
