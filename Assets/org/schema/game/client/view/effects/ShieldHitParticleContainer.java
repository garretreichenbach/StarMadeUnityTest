package org.schema.game.client.view.effects;

import java.util.Arrays;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.forms.particle.DamageContainerInterface;
import org.schema.schine.graphicsengine.forms.particle.ParticleContainer;
import org.schema.schine.graphicsengine.forms.particle.StartContainerInterface;



public class ShieldHitParticleContainer extends ParticleContainer implements DamageContainerInterface, StartContainerInterface{
	public static final int blocksizeInt = 9;
	private static final int id = 0; //14, 15, 16, 17, 18
	private static final int blockHitIndex = 1;
	private static final int ownerId = 2;
	private static final int wepId0 = 3; //14, 15, 16, 17, 18
	private static final int wepId1 = 4; //14, 15, 16, 17, 18
	private static final int shotStatus = 5; //14, 15, 16, 17, 18
	private static final int acidFormulaIndex = 6; //14, 15, 16, 17, 18
	private static final int penetrationDepth = 7;
	private static final int spriteCode = 8;
	
	public static final int blocksizeFloat = 19;
	private static final int pos = 0;//0 1 2
	private static final int velocity = 3; //6 7 8
	private static final int start = 6; //3 4 5
	private static final int color = 9; //9 10 11 12
	private static final int lifetime = 13; //13
	private static final int damage = 14; //13
	private static final int maxDistance = 15; //13
	private static final int damageInitial = 16; //13
	private static final int width = 17; //13
	private static final int impactForce = 18; //13
	
	
	private float[] particleArrayFloat;
	private int[] particleArrayInt;
	
	public ShieldHitParticleContainer(int capacity) {
		super(capacity);
		particleArrayFloat = (new float[capacity * ShieldHitParticleContainer.blocksizeFloat]);
		particleArrayInt = (new int[capacity * ShieldHitParticleContainer.blocksizeInt]);
	}
	public void reset() {
		Arrays.fill(particleArrayFloat, 0f);
		Arrays.fill(particleArrayInt, 0);
	}
	public void growCapacity() {
		capacity = capacity * 2;
		assert (capacity < (4096 * 4) * 4):capacity;
		particleArrayFloat = Arrays.copyOf(particleArrayFloat, capacity * ShieldHitParticleContainer.blocksizeFloat);
		particleArrayInt = Arrays.copyOf(particleArrayInt, capacity * ShieldHitParticleContainer.blocksizeInt);
	}
	public static final int getIndexFloat(int i) {
		return i * ShieldHitParticleContainer.blocksizeFloat;
	}
	public static final int getIndexInt(int i) {
		return i * ShieldHitParticleContainer.blocksizeInt;
	}
	public Vector4f getColor(int absindex, Vector4f out) {
		absindex = getIndexFloat(absindex);
		out.x = particleArrayFloat[absindex + ShieldHitParticleContainer.color];
		out.y = particleArrayFloat[absindex + ShieldHitParticleContainer.color + 1];
		out.z = particleArrayFloat[absindex + ShieldHitParticleContainer.color + 2];
		out.w = particleArrayFloat[absindex + ShieldHitParticleContainer.color + 3];
		return out;
	}

	public float getLifetime(int absindex) {
		absindex = getIndexFloat(absindex);
		return particleArrayFloat[absindex + ShieldHitParticleContainer.lifetime];
	}

	public int getId(int absindex) {
		absindex = getIndexInt(absindex);
		return particleArrayInt[absindex + ShieldHitParticleContainer.id];
	}

	public Vector3f getPos(int absindex, Vector3f out) {
		absindex = getIndexFloat(absindex);
		out.x = particleArrayFloat[absindex + ShieldHitParticleContainer.pos];
		out.y = particleArrayFloat[absindex + ShieldHitParticleContainer.pos + 1];
		out.z = particleArrayFloat[absindex + ShieldHitParticleContainer.pos + 2];
		return out;
	}

	public Vector3f getStart(int absindex, Vector3f out) {
		absindex = getIndexFloat(absindex);
		out.x = particleArrayFloat[absindex + ShieldHitParticleContainer.start];
		out.y = particleArrayFloat[absindex + ShieldHitParticleContainer.start + 1];
		out.z = particleArrayFloat[absindex + ShieldHitParticleContainer.start + 2];
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

	public int getBlockHitIndex(int absindex) {
		return particleArrayInt[getIndexInt(absindex) + ShieldHitParticleContainer.blockHitIndex];
	}

	public void setBlockHitIndex(int absindex, int index) {
		particleArrayInt[getIndexInt(absindex) + ShieldHitParticleContainer.blockHitIndex] = index;
	}
	public int getAcidFormulaIndex(int absindex) {
		return particleArrayInt[getIndexInt(absindex) + ShieldHitParticleContainer.acidFormulaIndex];
	}
	
	public void setAcidFormulaIndex(int absindex, int index) {
		particleArrayInt[getIndexInt(absindex) + ShieldHitParticleContainer.acidFormulaIndex] = index;
	}
	public void incBlockHitIndex(int absindex) {
		particleArrayInt[getIndexInt(absindex) + ShieldHitParticleContainer.blockHitIndex]++;
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
		out.x = particleArrayFloat[absindex + ShieldHitParticleContainer.velocity];
		out.y = particleArrayFloat[absindex + ShieldHitParticleContainer.velocity + 1];
		out.z = particleArrayFloat[absindex + ShieldHitParticleContainer.velocity + 2];
		return out;
	}
	public void setColor(int absindex, float x, float y, float z, float a) {
		absindex = getIndexFloat(absindex);
		particleArrayFloat[absindex + ShieldHitParticleContainer.color] = x;
		particleArrayFloat[absindex + ShieldHitParticleContainer.color + 1] = y;
		particleArrayFloat[absindex + ShieldHitParticleContainer.color + 2] = z;
		particleArrayFloat[absindex + ShieldHitParticleContainer.color + 3] = a;
	}

	public void setColor(int absindex, Vector4f c) {
		setColor(absindex, c.x, c.y, c.z, c.w);
	}

	public void setLifetime(int absindex, float time) {
		particleArrayFloat[getIndexFloat(absindex) + ShieldHitParticleContainer.lifetime] = time;
	}

	public void setId(int absindex, int id) {
		particleArrayInt[getIndexInt(absindex) + ShieldHitParticleContainer.id] = id;
	}
	
	public int getOwnerId(int absindex) {
		return particleArrayInt[getIndexInt(absindex) + ShieldHitParticleContainer.ownerId];
	}
	public void setOwnerId(int absindex, int id) {
		particleArrayInt[getIndexInt(absindex) + ShieldHitParticleContainer.ownerId] = id;
		
	}
	public int getShotStatus(int absindex) {
		return particleArrayInt[getIndexInt(absindex) + ShieldHitParticleContainer.shotStatus];
	}
	public void setShotStatus(int absindex, int status) {
		particleArrayInt[getIndexInt(absindex) + ShieldHitParticleContainer.shotStatus] = status;
		
	}
	public long getWeaponId(int absindex) {
		int a = particleArrayInt[getIndexInt(absindex) + ShieldHitParticleContainer.wepId0];
		int b = particleArrayInt[getIndexInt(absindex) + ShieldHitParticleContainer.wepId1];
		return (long)a << 32 | b & 0xFFFFFFFFL;
	}
	public void setWeaponId(int absindex, long id) {
		absindex = getIndexInt(absindex);
		particleArrayInt[absindex + ShieldHitParticleContainer.wepId0] = (int)(id >> 32);
		particleArrayInt[absindex + ShieldHitParticleContainer.wepId1] = (int)(id);
	}
	
	public int getPenetrationDepth(int absindex) {
		return particleArrayInt[getIndexInt(absindex) + ShieldHitParticleContainer.penetrationDepth];
	}
	
	public void setPenetrationDepth(int absindex, int penetrationDepth) {
		particleArrayInt[getIndexInt(absindex) + ShieldHitParticleContainer.penetrationDepth] = penetrationDepth;
	}
	
	
	public float getDamage(int absindex) {
		return particleArrayFloat[getIndexFloat(absindex) + ShieldHitParticleContainer.damage];
	}
	
	public void setDamage(int absindex, float damage) {
		particleArrayFloat[getIndexFloat(absindex) + ShieldHitParticleContainer.damage] = damage;
	}
	public float getWidth(int absindex) {
		return particleArrayFloat[getIndexFloat(absindex) + ShieldHitParticleContainer.width];
	}
	
	public void setWidth(int absindex, float w) {
		particleArrayFloat[getIndexFloat(absindex) + ShieldHitParticleContainer.width] = w;
	}
	
	public float getDamageInitial(int absindex) {
		return particleArrayFloat[getIndexFloat(absindex) + ShieldHitParticleContainer.damageInitial];
	}
	
	public void setDamageInitial(int absindex, float initialDamage) {
		particleArrayFloat[getIndexFloat(absindex) + ShieldHitParticleContainer.damageInitial] = initialDamage;
	}
	public float getImpactForce(int absindex) {
		return particleArrayFloat[getIndexFloat(absindex) + ShieldHitParticleContainer.impactForce];
	}
	
	public void setImpactForce(int absindex, float impact) {
		particleArrayFloat[getIndexFloat(absindex) + ShieldHitParticleContainer.impactForce] = impact;
	}
	
	public void setMaxDistance(int absindex, float maxDistance) {
		particleArrayFloat[getIndexFloat(absindex) + ShieldHitParticleContainer.maxDistance] = maxDistance;
	}
	public float getMaxDistance(int absindex) {
		return particleArrayFloat[getIndexFloat(absindex) + ShieldHitParticleContainer.maxDistance];
	}
	public void setPos(int absindex, float x, float y, float z) {
		absindex = getIndexFloat(absindex);
		particleArrayFloat[absindex + ShieldHitParticleContainer.pos] = x;
		particleArrayFloat[absindex + ShieldHitParticleContainer.pos + 1] = y;
		particleArrayFloat[absindex + ShieldHitParticleContainer.pos + 2] = z;
	}

	public void setStart(int absindex, float x, float y, float z) {
		absindex = getIndexFloat(absindex);
		particleArrayFloat[absindex + ShieldHitParticleContainer.start] = x;
		particleArrayFloat[absindex + ShieldHitParticleContainer.start + 1] = y;
		particleArrayFloat[absindex + ShieldHitParticleContainer.start + 2] = z;
	}

	public void setVelocity(int absindex, float x, float y, float z) {
		absindex = getIndexFloat(absindex);
		particleArrayFloat[absindex + ShieldHitParticleContainer.velocity] = x;
		particleArrayFloat[absindex + ShieldHitParticleContainer.velocity + 1] = y;
		particleArrayFloat[absindex + ShieldHitParticleContainer.velocity + 2] = z;
	}
	public int getSpriteCode(int absindex) {
		return particleArrayInt[getIndexInt(absindex) + ShieldHitParticleContainer.spriteCode];
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
	public void setSpriteCode(int absindex, int spriteIndex, int spriteMaxX, int spriteMaxY) {
		setSpriteCode(absindex, spriteIndex+spriteMaxX*100+spriteMaxY*10000);
	}
	public void setSpriteCode(int absindex, int code) {
		particleArrayInt[getIndexInt(absindex) + ShieldHitParticleContainer.spriteCode] = code;
	}
}
