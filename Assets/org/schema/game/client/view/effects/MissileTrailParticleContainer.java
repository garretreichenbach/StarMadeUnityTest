package org.schema.game.client.view.effects;

import java.util.Arrays;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.forms.particle.ParticleContainer;
import org.schema.schine.graphicsengine.forms.particle.StartContainerInterface;



public class MissileTrailParticleContainer extends ParticleContainer implements StartContainerInterface{
	public static final int blocksizeFloat = 11;
	private static final int pos = 0;//0 1 2
	private static final int start = 3; //3 4 5
	private static final int color = 6; //9 10 11 12
	private static final int lifetime = 10; //13
	
	
	private float[] particleArrayFloat;
	
	public MissileTrailParticleContainer(int capacity) {
		super(capacity);
		particleArrayFloat = (new float[capacity * MissileTrailParticleContainer.blocksizeFloat]);
	}
	public void reset() {
		Arrays.fill(particleArrayFloat, 0f);
	}
	public void growCapacity() {
		capacity = capacity * 2;
		assert (capacity < (4096 * 4) * 4):capacity;
		particleArrayFloat = Arrays.copyOf(particleArrayFloat, capacity * MissileTrailParticleContainer.blocksizeFloat);
	}
	
	public static final int getIndexFloat(int i) {
		return i * MissileTrailParticleContainer.blocksizeFloat;
	}
	public Vector4f getColor(int absindex, Vector4f out) {
		absindex = getIndexFloat(absindex);
		out.x = particleArrayFloat[absindex + MissileTrailParticleContainer.color];
		out.y = particleArrayFloat[absindex + MissileTrailParticleContainer.color + 1];
		out.z = particleArrayFloat[absindex + MissileTrailParticleContainer.color + 2];
		out.w = particleArrayFloat[absindex + MissileTrailParticleContainer.color + 3];
		return out;
	}

	public float getLifetime(int absindex) {
		absindex = getIndexFloat(absindex);
		return particleArrayFloat[absindex + MissileTrailParticleContainer.lifetime];
	}


	public Vector3f getPos(int absindex, Vector3f out) {
		absindex = getIndexFloat(absindex);
		out.x = particleArrayFloat[absindex + MissileTrailParticleContainer.pos];
		out.y = particleArrayFloat[absindex + MissileTrailParticleContainer.pos + 1];
		out.z = particleArrayFloat[absindex + MissileTrailParticleContainer.pos + 2];
		return out;
	}

	public Vector3f getStart(int absindex, Vector3f out) {
		absindex = getIndexFloat(absindex);
		out.x = particleArrayFloat[absindex + MissileTrailParticleContainer.start];
		out.y = particleArrayFloat[absindex + MissileTrailParticleContainer.start + 1];
		out.z = particleArrayFloat[absindex + MissileTrailParticleContainer.start + 2];
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
		return out;
	}
	public void setColor(int absindex, float x, float y, float z, float a) {
		absindex = getIndexFloat(absindex);
		particleArrayFloat[absindex + MissileTrailParticleContainer.color] = x;
		particleArrayFloat[absindex + MissileTrailParticleContainer.color + 1] = y;
		particleArrayFloat[absindex + MissileTrailParticleContainer.color + 2] = z;
		particleArrayFloat[absindex + MissileTrailParticleContainer.color + 3] = a;
	}

	public void setColor(int absindex, Vector4f c) {
		setColor(absindex, c.x, c.y, c.z, c.w);
	}

	public void setLifetime(int absindex, float time) {
		particleArrayFloat[getIndexFloat(absindex) + MissileTrailParticleContainer.lifetime] = time;
	}

	
	public void setPos(int absindex, float x, float y, float z) {
		absindex = getIndexFloat(absindex);
		particleArrayFloat[absindex + MissileTrailParticleContainer.pos] = x;
		particleArrayFloat[absindex + MissileTrailParticleContainer.pos + 1] = y;
		particleArrayFloat[absindex + MissileTrailParticleContainer.pos + 2] = z;
	}

	public void setStart(int absindex, float x, float y, float z) {
		absindex = getIndexFloat(absindex);
		particleArrayFloat[absindex + MissileTrailParticleContainer.start] = x;
		particleArrayFloat[absindex + MissileTrailParticleContainer.start + 1] = y;
		particleArrayFloat[absindex + MissileTrailParticleContainer.start + 2] = z;
	}

	public void setVelocity(int absindex, float x, float y, float z) {
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
