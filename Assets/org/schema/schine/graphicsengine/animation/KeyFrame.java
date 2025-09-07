package org.schema.schine.graphicsengine.animation;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Quat4Util;

public class KeyFrame implements Comparable<KeyFrame> {
	private final Vector3f translate = new Vector3f();

	private final Quat4f rotation = new Quat4f();
	private final Vector3f scale = new Vector3f(1, 1, 1);
	private final float time;

	public KeyFrame(float time, Vector3f translate, AxisAngle4f rotation, Vector3f scale) {
		this.time = time;
		this.translate.set(translate);
		this.rotation.set(Quat4Util.fromAngleAxis(rotation.angle, new Vector3f(rotation.x, rotation.y, rotation.z), new Quat4f()));
		if (scale != null && scale.x != 0 && scale.y != 0 && scale.z != 0) {
			this.scale.set(scale);
		}
	}

	public KeyFrame(float time, Vector3f translate, Quat4f rotation, Vector3f scale) {
		this.time = time;
		this.translate.set(translate);
		this.rotation.set(rotation);
		if (scale != null && scale.x != 0 && scale.y != 0 && scale.z != 0) {
			this.scale.set(scale);
		}
	}

	@Override
	public int compareTo(KeyFrame o) {
		return (int) (time * 1000 - o.time * 1000);
	}

	/**
	 * @return the rotation
	 */
	public Quat4f getRotation() {
		return rotation;
	}

	/**
	 * @return the scale
	 */
	public Vector3f getScale() {
		return scale;
	}

	/**
	 * @return the time of the keyframe
	 */
	public float getTime() {
		return time;
	}

	/**
	 * @return the translate
	 */
	public Vector3f getTranslate() {
		return translate;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "KeyFrame [translate=" + translate + ", rotation=" + rotation
				+ ", scale=" + scale + ", time=" + time + "]";
	}

}
