package org.schema.schine.network.objects.container;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;

import com.bulletphysics.linearmath.Transform;

public class TransformTimed extends Transform{

	public TransformTimed() {
		super();
	}

	public TransformTimed(Matrix3f mat) {
		super(mat);
	}

	public TransformTimed(Matrix4f mat) {
		super(mat);
	}

	public TransformTimed(Transform tr) {
		super(tr);
	}

	public long lastChanged;
	
}
