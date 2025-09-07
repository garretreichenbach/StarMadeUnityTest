package org.schema.game.client.view.effects;

import java.util.Arrays;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.forms.particle.ParticleContainer;
import org.schema.schine.graphicsengine.forms.particle.StartContainerInterface;



public class MissileHeadParticleContainer extends ParticleContainer implements StartContainerInterface{
	public static final int blocksizeInt = 1;
	private static final int id = 0;
	
	public static final int blocksizeFloat = 14;
	private static final int pos = 0;//0 1 2
	private static final int velocity = 3; //6 7 8
	private static final int start = 6; //3 4 5
	private static final int color = 9; //9 10 11 12
	private static final int lifetime = 13; //13
	
	
	private float[] particleArrayFloat;
	private int[] particleArrayInt;
	
	public MissileHeadParticleContainer(int capacity) {
		super(capacity);
		particleArrayFloat = (new float[capacity * MissileHeadParticleContainer.blocksizeFloat]);
		particleArrayInt = (new int[capacity * MissileHeadParticleContainer.blocksizeInt]);
	}
	public void reset() {
		Arrays.fill(particleArrayFloat, 0f);
		Arrays.fill(particleArrayInt, 0);
	}
	public void growCapacity() {
		capacity = capacity * 2;
		assert (capacity < (4096 * 4) * 4):capacity;
		particleArrayFloat = Arrays.copyOf(particleArrayFloat, capacity * MissileHeadParticleContainer.blocksizeFloat);
		particleArrayInt = Arrays.copyOf(particleArrayInt, capacity * MissileHeadParticleContainer.blocksizeInt);
	}
	public static final int getIndexFloat(int i) {
		return i * MissileHeadParticleContainer.blocksizeFloat;
	}
	public static final int getIndexInt(int i) {
		return i * MissileHeadParticleContainer.blocksizeInt;
	}
	public Vector4f getColor(int absindex, Vector4f out) {
		absindex = getIndexFloat(absindex);
		out.x = particleArrayFloat[absindex + MissileHeadParticleContainer.color];
		out.y = particleArrayFloat[absindex + MissileHeadParticleContainer.color + 1];
		out.z = particleArrayFloat[absindex + MissileHeadParticleContainer.color + 2];
		out.w = particleArrayFloat[absindex + MissileHeadParticleContainer.color + 3];
		return out;
	}

	public float getLifetime(int absindex) {
		absindex = getIndexFloat(absindex);
		return particleArrayFloat[absindex + MissileHeadParticleContainer.lifetime];
	}

	public int getId(int absindex) {
		absindex = getIndexInt(absindex);
		return particleArrayInt[absindex + MissileHeadParticleContainer.id];
	}

	public Vector3f getPos(int absindex, Vector3f out) {
		absindex = getIndexFloat(absindex);
		out.x = particleArrayFloat[absindex + MissileHeadParticleContainer.pos];
		out.y = particleArrayFloat[absindex + MissileHeadParticleContainer.pos + 1];
		out.z = particleArrayFloat[absindex + MissileHeadParticleContainer.pos + 2];
		return out;
	}

	public Vector3f getStart(int absindex, Vector3f out) {
		absindex = getIndexFloat(absindex);
		out.x = particleArrayFloat[absindex + MissileHeadParticleContainer.start];
		out.y = particleArrayFloat[absindex + MissileHeadParticleContainer.start + 1];
		out.z = particleArrayFloat[absindex + MissileHeadParticleContainer.start + 2];
		return out;
	}
	public void copy(final int from, final int to) {
		int fromFloat = getIndexFloat(from);
		int toFloat = getIndexFloat(to);
		for (int i = 0; i < blocksizeFloat; i++) {
			particleArrayFloat[toFloat + i] = particleArrayFloat[fromFloat + i];
		}
		
		int fromInt = getIndexInt(from);
		int toInt = getIndexInt(to);
		for (int i = 0; i < blocksizeInt; i++) {
			particleArrayInt[toInt + i] = particleArrayInt[fromInt + i];
		}

	}

	public float[] getArrayFloat() {
		return particleArrayFloat;
	}
	public int[] getArrayInt() {
		return particleArrayInt;
	}

	protected void swapValuesInt(int a, int b) {
		int aI = getIndexInt(a);
		int bI = getIndexInt(b);
		for (int i = 0; i < blocksizeInt; i++) {
			
			int t = particleArrayInt[bI + i];
			
			particleArrayInt[bI + i] = particleArrayInt[aI + i];
			
			particleArrayInt[aI + i] = t;
		}
		
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
		out.x = particleArrayFloat[absindex + MissileHeadParticleContainer.velocity];
		out.y = particleArrayFloat[absindex + MissileHeadParticleContainer.velocity + 1];
		out.z = particleArrayFloat[absindex + MissileHeadParticleContainer.velocity + 2];
		return out;
	}
	public void setColor(int absindex, float x, float y, float z, float a) {
		absindex = getIndexFloat(absindex);
		particleArrayFloat[absindex + MissileHeadParticleContainer.color] = x;
		particleArrayFloat[absindex + MissileHeadParticleContainer.color + 1] = y;
		particleArrayFloat[absindex + MissileHeadParticleContainer.color + 2] = z;
		particleArrayFloat[absindex + MissileHeadParticleContainer.color + 3] = a;
	}

	public void setColor(int absindex, Vector4f c) {
		setColor(absindex, c.x, c.y, c.z, c.w);
	}

	public void setLifetime(int absindex, float time) {
		particleArrayFloat[getIndexFloat(absindex) + MissileHeadParticleContainer.lifetime] = time;
	}

	public void setId(int absindex, int id) {
		particleArrayInt[getIndexInt(absindex) + MissileHeadParticleContainer.id] = id;
	}
	
	public void setPos(int absindex, float x, float y, float z) {
		absindex = getIndexFloat(absindex);
		particleArrayFloat[absindex + MissileHeadParticleContainer.pos] = x;
		particleArrayFloat[absindex + MissileHeadParticleContainer.pos + 1] = y;
		particleArrayFloat[absindex + MissileHeadParticleContainer.pos + 2] = z;
	}

	public void setStart(int absindex, float x, float y, float z) {
		absindex = getIndexFloat(absindex);
		particleArrayFloat[absindex + MissileHeadParticleContainer.start] = x;
		particleArrayFloat[absindex + MissileHeadParticleContainer.start + 1] = y;
		particleArrayFloat[absindex + MissileHeadParticleContainer.start + 2] = z;
	}

	public void setVelocity(int absindex, float x, float y, float z) {
		absindex = getIndexFloat(absindex);
		particleArrayFloat[absindex + MissileHeadParticleContainer.velocity] = x;
		particleArrayFloat[absindex + MissileHeadParticleContainer.velocity + 1] = y;
		particleArrayFloat[absindex + MissileHeadParticleContainer.velocity + 2] = z;
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
}
