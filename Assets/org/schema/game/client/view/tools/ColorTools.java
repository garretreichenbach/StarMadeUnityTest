package org.schema.game.client.view.tools;

import javax.vecmath.Vector4f;

import org.schema.common.FastMath;

public class ColorTools {
	public static void brighten(Vector4f color) {
		color.w = 1;
		while (FastMath.carmackSqrt(color.x * color.x + color.y * color.y +
				color.z * color.z) < 0.5f) {
			color.x = Math.min(1.0f, color.x * 1.33f);
			color.y = Math.min(1.0f, color.y * 1.33f);
			color.z = Math.min(1.0f, color.z * 1.33f);

		}
	}
}
