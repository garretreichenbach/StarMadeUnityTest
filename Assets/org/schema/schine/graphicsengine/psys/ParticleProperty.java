package org.schema.schine.graphicsengine.psys;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

public class ParticleProperty {
	private static int i;
	private static final int angularX = i++;
	private static final int angularY = i++;
	private static final int angularZ = i++;
	private static final int colorR = i++;
	private static final int colorG = i++;
	private static final int colorB = i++;
	private static final int colorA = i++;
	private static final int lifetime = i++;
	private static final int lifespanTotal = i++;
	private static final int posX = i++;
	private static final int posY = i++;
	private static final int posZ = i++;
	private static final int randSeed = i++;
	private static final int rotX = i++;
	private static final int rotY = i++;
	private static final int rotZ = i++;
	private static final int rotW = i++;
	private static final int sizeX = i++;
	private static final int sizeY = i++;
	private static final int sizeZ = i++;
	private static final int startLifetime = i++;
	private static final int velocityX = i++;
	private static final int velocityY = i++;
	private static final int velocityZ = i++;
	private static final int camDist = i++;
	private final static int arraySize = i;

	public static int getIndexRaw(int index) {
		return index * arraySize;
	}

	private static float get(int index, int type, float[] rawParticles) {
		return rawParticles[getIndexRaw(index) + type];
	}

	private static void set(int index, int type, float[] rawParticles, float value) {
		rawParticles[getIndexRaw(index) + type] = value;
	}

	public static void setCamDist(int index, float[] rawParticles, float value) {
		set(index, camDist, rawParticles, value);
	}

	public static void setAngularVelocity(int index, float[] rawParticles, Vector3f value) {
		setAngularVelocityX(index, rawParticles, value.x);
		setAngularVelocityY(index, rawParticles, value.y);
		setAngularVelocityZ(index, rawParticles, value.z);
	}

	public static void setAngularVelocityX(int index, float[] rawParticles, float value) {
		set(index, angularX, rawParticles, value);
	}

	public static void setAngularVelocityY(int index, float[] rawParticles, float value) {
		set(index, angularY, rawParticles, value);
	}

	public static void setAngularVelocityZ(int index, float[] rawParticles, float value) {
		set(index, angularZ, rawParticles, value);
	}

	public static void setColor(int index, float[] rawParticles, Vector4f value) {
		setColorR(index, rawParticles, value.x);
		setColorG(index, rawParticles, value.y);
		setColorB(index, rawParticles, value.z);
		setColorA(index, rawParticles, value.w);
	}

	public static void setColorR(int index, float[] rawParticles, float value) {
		set(index, colorR, rawParticles, value);
	}

	public static void setColorG(int index, float[] rawParticles, float value) {
		set(index, colorG, rawParticles, value);
	}

	public static void setColorB(int index, float[] rawParticles, float value) {
		set(index, colorB, rawParticles, value);
	}

	public static void setColorA(int index, float[] rawParticles, float value) {
		set(index, colorA, rawParticles, value);
	}

	public static void setLifetime(int index, float[] rawParticles, float value) {
		set(index, lifetime, rawParticles, value);
	}

	public static void setLifetimeTotal(int index, float[] rawParticles, float value) {
		set(index, lifespanTotal, rawParticles, value);
	}

	public static void setPos(int index, float[] rawParticles, Vector3f value) {
		setPosX(index, rawParticles, value.x);
		setPosY(index, rawParticles, value.y);
		setPosZ(index, rawParticles, value.z);
	}

	public static void setPosX(int index, float[] rawParticles, float value) {
		set(index, posX, rawParticles, value);
	}

	public static void setPosY(int index, float[] rawParticles, float value) {
		set(index, posY, rawParticles, value);
	}

	public static void setPosZ(int index, float[] rawParticles, float value) {
		set(index, posZ, rawParticles, value);
	}

	public static void setRandomSeed(int index, float[] rawParticles, float value) {
		set(index, randSeed, rawParticles, value);
	}

	public static void setRotation(int index, float[] rawParticles, Quat4f value) {
		setRotX(index, rawParticles, value.x);
		setRotY(index, rawParticles, value.y);
		setRotZ(index, rawParticles, value.z);
		setRotW(index, rawParticles, value.w);
	}

	public static void setRotX(int index, float[] rawParticles, float value) {
		set(index, rotX, rawParticles, value);
	}

	public static void setRotY(int index, float[] rawParticles, float value) {
		set(index, rotY, rawParticles, value);
	}

	public static void setRotZ(int index, float[] rawParticles, float value) {
		set(index, rotZ, rawParticles, value);
	}

	public static void setRotW(int index, float[] rawParticles, float value) {
		set(index, rotW, rawParticles, value);
	}

	public static void setSize(int index, float[] rawParticles, Vector3f value) {
		setSizeX(index, rawParticles, value.x);
		setSizeY(index, rawParticles, value.y);
		setSizeZ(index, rawParticles, value.z);
	}

	public static void setSizeX(int index, float[] rawParticles, float value) {
		set(index, sizeX, rawParticles, value);
	}

	public static void setSizeY(int index, float[] rawParticles, float value) {
		set(index, sizeY, rawParticles, value);
	}

	public static void setSizeZ(int index, float[] rawParticles, float value) {
		set(index, sizeZ, rawParticles, value);
	}

	public static void setStartLifetime(int index, float[] rawParticles, float value) {
		set(index, startLifetime, rawParticles, value);
	}

	public static void setVelocity(int index, float[] rawParticles, Vector3f value) {
		setVelocityX(index, rawParticles, value.x);
		setVelocityY(index, rawParticles, value.y);
		setVelocityZ(index, rawParticles, value.z);
	}

	public static void setVelocityX(int index, float[] rawParticles, float value) {
		set(index, velocityX, rawParticles, value);
	}

	public static void setVelocityY(int index, float[] rawParticles, float value) {
		set(index, velocityY, rawParticles, value);
	}

	public static void setVelocityZ(int index, float[] rawParticles, float value) {
		set(index, velocityZ, rawParticles, value);
	}

	public static float getCameraDistance(int index, float[] rawParticles) {
		return get(index, camDist, rawParticles);
	}

	public static Vector3f getAngularVelocity(int index, float[] rawParticles) {
		return new Vector3f(getAngularVelocityX(index, rawParticles), getAngularVelocityY(index, rawParticles), getAngularVelocityZ(index, rawParticles));
	}

	public static float getAngularVelocityX(int index, float[] rawParticles) {
		return get(index, angularX, rawParticles);
	}

	public static float getAngularVelocityY(int index, float[] rawParticles) {
		return get(index, angularY, rawParticles);
	}

	public static float getAngularVelocityZ(int index, float[] rawParticles) {
		return get(index, angularZ, rawParticles);
	}

	public static Vector4f getColor(int index, float[] rawParticles) {
		return new Vector4f(getColorR(index, rawParticles), getColorG(index, rawParticles), getColorB(index, rawParticles), getColorA(index, rawParticles));
	}

	public static float getColorR(int index, float[] rawParticles) {
		return get(index, colorR, rawParticles);
	}

	public static float getColorG(int index, float[] rawParticles) {
		return get(index, colorG, rawParticles);
	}

	public static float getColorB(int index, float[] rawParticles) {
		return get(index, colorB, rawParticles);
	}

	public static float getColorA(int index, float[] rawParticles) {
		return get(index, colorA, rawParticles);
	}

	public static float getLifetime(int index, float[] rawParticles) {
		return get(index, lifetime, rawParticles);
	}

	public static float getLifetimeTotal(int index, float[] rawParticles) {
		return get(index, lifespanTotal, rawParticles);
	}

	public static Vector3f getPosition(int index, float[] rawParticles) {
		return new Vector3f(getPosX(index, rawParticles), getPosY(index, rawParticles), getPosZ(index, rawParticles));
	}

	public static float getPosX(int index, float[] rawParticles) {
		return get(index, posX, rawParticles);
	}

	public static float getPosY(int index, float[] rawParticles) {
		return get(index, posY, rawParticles);
	}

	public static float getPosZ(int index, float[] rawParticles) {
		return get(index, posZ, rawParticles);
	}

	public static float getRandomSeed(int index, float[] rawParticles) {
		return get(index, randSeed, rawParticles);
	}

	public static Quat4f getRotation(int index, float[] rawParticles) {
		return new Quat4f(getRotX(index, rawParticles), getRotY(index, rawParticles), getRotZ(index, rawParticles), getRotW(index, rawParticles));
	}

	public static float getRotX(int index, float[] rawParticles) {
		return get(index, rotX, rawParticles);
	}

	public static float getRotY(int index, float[] rawParticles) {
		return get(index, rotY, rawParticles);
	}

	public static float getRotZ(int index, float[] rawParticles) {
		return get(index, rotZ, rawParticles);
	}

	public static float getRotW(int index, float[] rawParticles) {
		return get(index, rotW, rawParticles);
	}

	public static Vector3f getSize(int index, float[] rawParticles) {
		return new Vector3f(getSizeX(index, rawParticles), getSizeY(index, rawParticles), getSizeZ(index, rawParticles));
	}

	public static float getSizeX(int index, float[] rawParticles) {
		return get(index, sizeX, rawParticles);
	}

	public static float getSizeY(int index, float[] rawParticles) {
		return get(index, sizeY, rawParticles);
	}

	public static float getSizeZ(int index, float[] rawParticles) {
		return get(index, sizeZ, rawParticles);
	}

	public static float getStartLifetime(int index, float[] rawParticles) {
		return get(index, startLifetime, rawParticles);
	}

	public static Vector3f getVelocity(int index, float[] rawParticles) {
		return new Vector3f(getVelocityX(index, rawParticles), getVelocityY(index, rawParticles), getVelocityZ(index, rawParticles));
	}

	public static float getVelocityX(int index, float[] rawParticles) {
		return get(index, velocityX, rawParticles);
	}

	public static float getVelocityY(int index, float[] rawParticles) {
		return get(index, velocityY, rawParticles);
	}

	public static float getVelocityZ(int index, float[] rawParticles) {
		return get(index, velocityZ, rawParticles);
	}

	public static int getPropertyCount() {
		return arraySize;
	}

	public static int getParticleCount(float[] rawParticles) {
		return rawParticles.length / arraySize;
	}
}
