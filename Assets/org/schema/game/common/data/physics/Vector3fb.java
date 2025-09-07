package org.schema.game.common.data.physics;

import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import org.schema.game.common.data.physics.qhull.DPoint3d;

public class Vector3fb extends Vector3f {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	public float w;

	public Vector3fb(DPoint3d a) {
		super((float) a.x, (float) a.y, (float) a.z);
	}

	public Vector3fb(Vector3fb a) {
		super(a);
		this.w = a.w;
	}

	public Vector3fb(float x, float y, float z) {
		super(x, y, z);
	}

	public Vector3fb(float[] v) {
		super(v);
	}

	public Vector3fb(Tuple3d t1) {
		super(t1);
	}


	public Vector3fb(Vector3d v1) {
		super(v1);
	}

	public Vector3fb(Vector3f v1) {
		super(v1);
	}

	public Vector3fb() {
		super();
	}

	public void set(Vector3fb a) {
		super.set(a);
		this.w = a.w;
	}

	public Vector3fb cross(Vector3fb n2) {
		Vector3fb o = new Vector3fb();
		o.cross(this, n2);
		return o;
	}

	/* (non-Javadoc)
	 * @see javax.vecmath.Tuple3f#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + "[" + w + "]";
	}

}
