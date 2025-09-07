package org.schema.game.common.controller.damage.projectile;

import java.util.Arrays;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.forms.particle.DamageContainerInterface;
import org.schema.schine.graphicsengine.forms.particle.ParticleContainer;
import org.schema.schine.graphicsengine.forms.particle.StartContainerInterface;

public class ProjectileParticleContainer extends ParticleContainer implements DamageContainerInterface, StartContainerInterface{

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
	
	public ProjectileParticleContainer(int capacity) {
		super(capacity);
		particleArrayFloat = (new float[capacity * ProjectileParticleContainer.blocksizeFloat]);
		particleArrayInt = (new int[capacity * ProjectileParticleContainer.blocksizeInt]);
	}
	public void reset() {
		Arrays.fill(particleArrayFloat, 0f);
		Arrays.fill(particleArrayInt, 0);
	}
	public void growCapacity() {
		capacity = capacity * 2;
		assert (capacity < (4096 * 4) * 4):capacity;
		particleArrayFloat = Arrays.copyOf(particleArrayFloat, capacity * ProjectileParticleContainer.blocksizeFloat);
		particleArrayInt = Arrays.copyOf(particleArrayInt, capacity * ProjectileParticleContainer.blocksizeInt);
	}
	public static final int getIndexFloat(int i) {
		return i * ProjectileParticleContainer.blocksizeFloat;
	}
	public static final int getIndexInt(int i) {
		return i * ProjectileParticleContainer.blocksizeInt;
	}
	public Vector4f getColor(int absindex, Vector4f out) {
		absindex = getIndexFloat(absindex);
		out.x = particleArrayFloat[absindex + ProjectileParticleContainer.color];
		out.y = particleArrayFloat[absindex + ProjectileParticleContainer.color + 1];
		out.z = particleArrayFloat[absindex + ProjectileParticleContainer.color + 2];
		out.w = particleArrayFloat[absindex + ProjectileParticleContainer.color + 3];
		return out;
	}

	public float getLifetime(int absindex) {
		absindex = getIndexFloat(absindex);
		return particleArrayFloat[absindex + ProjectileParticleContainer.lifetime];
	}

	public int getId(int absindex) {
		absindex = getIndexInt(absindex);
		return particleArrayInt[absindex + ProjectileParticleContainer.id];
	}

	public Vector3f getPos(int absindex, Vector3f out) {
		absindex = getIndexFloat(absindex);
		out.x = particleArrayFloat[absindex + ProjectileParticleContainer.pos];
		out.y = particleArrayFloat[absindex + ProjectileParticleContainer.pos + 1];
		out.z = particleArrayFloat[absindex + ProjectileParticleContainer.pos + 2];
		return out;
	}

	public Vector3f getStart(int absindex, Vector3f out) {
		absindex = getIndexFloat(absindex);
		out.x = particleArrayFloat[absindex + ProjectileParticleContainer.start];
		out.y = particleArrayFloat[absindex + ProjectileParticleContainer.start + 1];
		out.z = particleArrayFloat[absindex + ProjectileParticleContainer.start + 2];
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
		return particleArrayInt[getIndexInt(absindex) + ProjectileParticleContainer.blockHitIndex];
	}

	public void setBlockHitIndex(int absindex, int index) {
		particleArrayInt[getIndexInt(absindex) + ProjectileParticleContainer.blockHitIndex] = index;
	}
	public int getAcidFormulaIndex(int absindex) {
		return particleArrayInt[getIndexInt(absindex) + ProjectileParticleContainer.acidFormulaIndex];
	}
	
	public void setAcidFormulaIndex(int absindex, int index) {
		particleArrayInt[getIndexInt(absindex) + ProjectileParticleContainer.acidFormulaIndex] = index;
	}
	public void incBlockHitIndex(int absindex) {
		particleArrayInt[getIndexInt(absindex) + ProjectileParticleContainer.blockHitIndex]++;
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
		out.x = particleArrayFloat[absindex + ProjectileParticleContainer.velocity];
		out.y = particleArrayFloat[absindex + ProjectileParticleContainer.velocity + 1];
		out.z = particleArrayFloat[absindex + ProjectileParticleContainer.velocity + 2];
		return out;
	}
	public void setColor(int absindex, float x, float y, float z, float a) {
		absindex = getIndexFloat(absindex);
		particleArrayFloat[absindex + ProjectileParticleContainer.color] = x;
		particleArrayFloat[absindex + ProjectileParticleContainer.color + 1] = y;
		particleArrayFloat[absindex + ProjectileParticleContainer.color + 2] = z;
		particleArrayFloat[absindex + ProjectileParticleContainer.color + 3] = a;
	}

	public void setColor(int absindex, Vector4f c) {
		setColor(absindex, c.x, c.y, c.z, c.w);
	}

	public void setLifetime(int absindex, float time) {
		particleArrayFloat[getIndexFloat(absindex) + ProjectileParticleContainer.lifetime] = time;
	}

	public void setId(int absindex, int id) {
		particleArrayInt[getIndexInt(absindex) + ProjectileParticleContainer.id] = id;
	}
	
	public int getOwnerId(int absindex) {
		return particleArrayInt[getIndexInt(absindex) + ProjectileParticleContainer.ownerId];
	}
	public void setOwnerId(int absindex, int id) {
		particleArrayInt[getIndexInt(absindex) + ProjectileParticleContainer.ownerId] = id;
		
	}
	public int getShotStatus(int absindex) {
		return particleArrayInt[getIndexInt(absindex) + ProjectileParticleContainer.shotStatus];
	}
	public void setShotStatus(int absindex, int status) {
		particleArrayInt[getIndexInt(absindex) + ProjectileParticleContainer.shotStatus] = status;
		
	}
	public long getWeaponId(int absindex) {
		int a = particleArrayInt[getIndexInt(absindex) + ProjectileParticleContainer.wepId0];
		int b = particleArrayInt[getIndexInt(absindex) + ProjectileParticleContainer.wepId1];
		return (long)a << 32 | b & 0xFFFFFFFFL;
	}
	public void setWeaponId(int absindex, long id) {
		absindex = getIndexInt(absindex);
		particleArrayInt[absindex + ProjectileParticleContainer.wepId0] = (int)(id >> 32);
		particleArrayInt[absindex + ProjectileParticleContainer.wepId1] = (int)(id);
	}
	
	public int getPenetrationDepth(int absindex) {
		return particleArrayInt[getIndexInt(absindex) + ProjectileParticleContainer.penetrationDepth];
	}
	
	public void setPenetrationDepth(int absindex, int penetrationDepth) {
		particleArrayInt[getIndexInt(absindex) + ProjectileParticleContainer.penetrationDepth] = penetrationDepth;
	}
	
	
	public float getDamage(int absindex) {
		return particleArrayFloat[getIndexFloat(absindex) + ProjectileParticleContainer.damage];
	}
	
	public void setDamage(int absindex, float damage) {
		particleArrayFloat[getIndexFloat(absindex) + ProjectileParticleContainer.damage] = damage;
	}
	public float getWidth(int absindex) {
		return particleArrayFloat[getIndexFloat(absindex) + ProjectileParticleContainer.width];
	}
	
	public void setWidth(int absindex, float w) {
		particleArrayFloat[getIndexFloat(absindex) + ProjectileParticleContainer.width] = w;
	}
	
	public float getDamageInitial(int absindex) {
		return particleArrayFloat[getIndexFloat(absindex) + ProjectileParticleContainer.damageInitial];
	}
	
	public void setDamageInitial(int absindex, float initialDamage) {
		particleArrayFloat[getIndexFloat(absindex) + ProjectileParticleContainer.damageInitial] = initialDamage;
	}
	public float getImpactForce(int absindex) {
		return particleArrayFloat[getIndexFloat(absindex) + ProjectileParticleContainer.impactForce];
	}
	
	public void setImpactForce(int absindex, float impact) {
		particleArrayFloat[getIndexFloat(absindex) + ProjectileParticleContainer.impactForce] = impact;
	}
	
	public void setMaxDistance(int absindex, float maxDistance) {
		particleArrayFloat[getIndexFloat(absindex) + ProjectileParticleContainer.maxDistance] = maxDistance;
	}
	public float getMaxDistance(int absindex) {
		return particleArrayFloat[getIndexFloat(absindex) + ProjectileParticleContainer.maxDistance];
	}
	public void setPos(int absindex, float x, float y, float z) {
		absindex = getIndexFloat(absindex);
		particleArrayFloat[absindex + ProjectileParticleContainer.pos] = x;
		particleArrayFloat[absindex + ProjectileParticleContainer.pos + 1] = y;
		particleArrayFloat[absindex + ProjectileParticleContainer.pos + 2] = z;
	}

	public void setStart(int absindex, float x, float y, float z) {
		absindex = getIndexFloat(absindex);
		particleArrayFloat[absindex + ProjectileParticleContainer.start] = x;
		particleArrayFloat[absindex + ProjectileParticleContainer.start + 1] = y;
		particleArrayFloat[absindex + ProjectileParticleContainer.start + 2] = z;
	}

	public void setVelocity(int absindex, float x, float y, float z) {
		absindex = getIndexFloat(absindex);
		particleArrayFloat[absindex + ProjectileParticleContainer.velocity] = x;
		particleArrayFloat[absindex + ProjectileParticleContainer.velocity + 1] = y;
		particleArrayFloat[absindex + ProjectileParticleContainer.velocity + 2] = z;
	}
	public int getSpriteCode(int absindex) {
		return particleArrayInt[getIndexInt(absindex) + ProjectileParticleContainer.spriteCode];
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
		particleArrayInt[getIndexInt(absindex) + ProjectileParticleContainer.spriteCode] = code;
	}
}
