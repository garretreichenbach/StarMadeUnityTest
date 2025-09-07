package org.schema.common.util.linAlg;

import javax.vecmath.Quat4f;

import org.schema.common.FastMath;

public class QuaternionTools {
	public static float getAngle(Quat4f q) {
		float s = 2f * (float) FastMath.acosFast(q.w);
		return s;
	}
}
