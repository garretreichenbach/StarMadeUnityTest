package org.schema.schine.graphicsengine.forms.particle.simple;

import java.util.Arrays;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.forms.particle.ParticleContainer;
import org.schema.schine.graphicsengine.forms.particle.StartContainerInterface;



public class SimpleParticleContainer extends ParticleContainer implements StartContainerInterface{
	
	public static final int blocksizeFloat = 14;
	private static final int pos = 0;//0 1 2
	private static final int velocity = 3; //6 7 8
	private static final int start = 6; //3 4 5
	private static final int color = 9; //9 10 11 12
	private static final int lifetime = 13; //13
	
	
	private float[] particleArrayFloat;
	
	public SimpleParticleContainer(int capacity) {
		super(capacity);
		particleArrayFloat = (new float[capacity * SimpleParticleContainer.blocksizeFloat]);
	}
	public void reset() {
		Arrays.fill(particleArrayFloat, 0f);
	}
	public void growCapacity() {
		capacity = capacity * 2;
		assert (capacity < (4096 * 4) * 4):capacity;
		particleArrayFloat = Arrays.copyOf(particleArrayFloat, capacity * SimpleParticleContainer.blocksizeFloat);
	}
	
	public static final int getIndexFloat(int i) {
		return i * SimpleParticleContainer.blocksizeFloat;
	}
	public Vector4f getColor(int absindex, Vector4f out) {
		absindex = getIndexFloat(absindex);
		out.x = particleArrayFloat[absindex + SimpleParticleContainer.color];
		out.y = particleArrayFloat[absindex + SimpleParticleContainer.color + 1];
		out.z = particleArrayFloat[absindex + SimpleParticleContainer.color + 2];
		out.w = particleArrayFloat[absindex + SimpleParticleContainer.color + 3];
		return out;
	}

	public float getLifetime(int absindex) {
		absindex = getIndexFloat(absindex);
		return particleArrayFloat[absindex + SimpleParticleContainer.lifetime];
	}


	public Vector3f getPos(int absindex, Vector3f out) {
		absindex = getIndexFloat(absindex);
		out.x = particleArrayFloat[absindex + SimpleParticleContainer.pos];
		out.y = particleArrayFloat[absindex + SimpleParticleContainer.pos + 1];
		out.z = particleArrayFloat[absindex + SimpleParticleContainer.pos + 2];
		return out;
	}

	public Vector3f getStart(int absindex, Vector3f out) {
		absindex = getIndexFloat(absindex);
		out.x = particleArrayFloat[absindex + SimpleParticleContainer.start];
		out.y = particleArrayFloat[absindex + SimpleParticleContainer.start + 1];
		out.z = particleArrayFloat[absindex + SimpleParticleContainer.start + 2];
		return out;
	}
	public void copy(final int from, final int to) {
		int fromFloat = getIndexFloat(from);
		int toFloat = getIndexFloat(to);
		for (int i = 0; i < blocksizeFloat; i++) {
			particleArrayFloat[toFloat + i] = particleArrayFloat[fromFloat + i];
		}
		

	}

	public float[] getArrayFloat() {
		return particleArrayFloat;
	}
	protected void swapValuesFloat(int a, int b) {
		int aI = getIndexFloat(a);
		int bI = getIndexFloat(b);
		for (int i = 0; i < blocksizeFloat; i++) {
			
			float t = particleArrayFloat[bI + i];
			
			particleArrayFloat[bI + i] = particleArrayFloat[aI + i];
			
			particleArrayFloat[aI + i] = t;
		}
		
	}
	public Vector3f getVelocity(int absindex, Vector3f out) {
		absindex = getIndexFloat(absindex);
		out.x = particleArrayFloat[absindex + SimpleParticleContainer.velocity];
		out.y = particleArrayFloat[absindex + SimpleParticleContainer.velocity + 1];
		out.z = particleArrayFloat[absindex + SimpleParticleContainer.velocity + 2];
		return out;
	}
	public void setColor(int absindex, float x, float y, float z, float a) {
		absindex = getIndexFloat(absindex);
		particleArrayFloat[absindex + SimpleParticleContainer.color] = x;
		particleArrayFloat[absindex + SimpleParticleContainer.color + 1] = y;
		particleArrayFloat[absindex + SimpleParticleContainer.color + 2] = z;
		particleArrayFloat[absindex + SimpleParticleContainer.color + 3] = a;
	}

	public void setColor(int absindex, Vector4f c) {
		setColor(absindex, c.x, c.y, c.z, c.w);
	}

	public void setLifetime(int absindex, float time) {
		particleArrayFloat[getIndexFloat(absindex) + SimpleParticleContainer.lifetime] = time;
	}

	
	public void setPos(int absindex, float x, float y, float z) {
		absindex = getIndexFloat(absindex);
		particleArrayFloat[absindex + SimpleParticleContainer.pos] = x;
		particleArrayFloat[absindex + SimpleParticleContainer.pos + 1] = y;
		particleArrayFloat[absindex + SimpleParticleContainer.pos + 2] = z;
	}

	public void setStart(int absindex, float x, float y, float z) {
		absindex = getIndexFloat(absindex);
		particleArrayFloat[absindex + SimpleParticleContainer.start] = x;
		particleArrayFloat[absindex + SimpleParticleContainer.start + 1] = y;
		particleArrayFloat[absindex + SimpleParticleContainer.start + 2] = z;
	}

	public void setVelocity(int absindex, float x, float y, float z) {
		absindex = getIndexFloat(absindex);
		particleArrayFloat[absindex + SimpleParticleContainer.velocity] = x;
		particleArrayFloat[absindex + SimpleParticleContainer.velocity + 1] = y;
		particleArrayFloat[absindex + SimpleParticleContainer.velocity + 2] = z;
	}
	public int getSpriteCodeSpriteMaxY(int absindex, int spriteIndex, int spriteMaxX, int spriteMaxY) {
		return (int)(getSpriteCode(absindex)/10000);
	}
	public int getSpriteCodeSpriteMaxX(int absindex, int spriteIndex, int spriteMaxX, int spriteMaxY) {
		return (int)((getSpriteCode(absindex)%10000)/100);
	}
	public int getSpriteCodeSpriteIndex(int absindex, int spriteIndex, int spriteMaxX, int spriteMaxY) {
		return getSpriteCode(absindex)%100;
	}
	@Override
	protected void swapValuesInt(int a, int b) {
	}
}
