package org.schema.schine.graphicsengine.forms.particle;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.util.ArraySortInterface;
import org.schema.common.util.QuickSort;

public abstract class ParticleContainer implements ArraySortInterface<Float> {
	
	
	
//	private static final int userData = 14; //14, 15, 16, 17, 18
	
	public static final int DEFAULT_CAPACITY = 512;
	
	public int capacity;
	protected Vector3f tmpPos = new Vector3f();
	protected Vector3f fromTmp = new Vector3f();

	public ParticleContainer() {
		this(DEFAULT_CAPACITY);
	}

	public ParticleContainer(int capacity) {
		this.capacity = capacity;
	}
	
	public abstract float getLifetime(int absindex);
	public abstract void setLifetime(int absindex, float time);
	public abstract void copy(final int from, final int to);
	public abstract void setPos(int absindex, float x, float y, float z);
	public abstract Vector3f getVelocity(int absindex, Vector3f out);
	public abstract Vector4f getColor(int absindex, Vector4f out);
	public abstract void setVelocity(int absindex, float x, float y, float z);

	/**
	 * @return the capacity
	 */
	public int getCapacity() {
		return capacity;
	}



	@Override
	public float getValue(int index) {

		getPos(index, tmpPos);
		tmpPos.sub(fromTmp);

		return tmpPos.length();
	}

	@Override
	public void swapValues(int a, int b) {
		swapValuesFloat(a, b);
		swapValuesInt(a, b);

	}
	protected abstract void swapValuesInt(int a, int b);
	protected abstract void swapValuesFloat(int a, int b);

	public abstract Vector3f getPos(int absindex, Vector3f out);

	public abstract void growCapacity();
	

	public abstract void reset();
	

	public void sort(Vector3f from, int start, int end) {
		if (Math.abs(start - end) == 0) {
			return;
		}
		this.fromTmp.set(from);
		QuickSort.sort(start, end, this);
	}

	public int getSpriteCode(int absIndex) {
		return 0;
	}



	
	

}
