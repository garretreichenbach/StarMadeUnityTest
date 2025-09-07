package org.schema.game.client.view.cubes.occlusion;

import org.schema.common.FastMath;

public class Sample {
	public final Ray rays[]; // [ray_count];
	final float[] data = new float[6];
	final float[] dataInv = new float[6];

	public Sample(int rayCount) {
		super();
		this.rays = new Ray[rayCount];
	}

	public void initRays() {
		float inc;

		inc = FastMath.PI * (3.0f - FastMath.sqrt(5));
		float off = 2.0f / rays.length;
		for (int k = 0; k < rays.length; k++) {
			rays[k] = new Ray(Occlusion.RAY_LENGTH);
			float y = k * off - 1.0f + (off / 2.0f);
			float r = FastMath.sqrt(1.0f - y * y);
			float phi = k * inc;
			//				System.err.println("SPHERE PIC: "+FastMath.cos(phi) * r+", "+ y+", "+ FastMath.sin(phi) * r);
			rays[k].compute(FastMath.cosFast(phi) * r, y, FastMath.sinFast(phi) * r);

			for (int i = 0; i < 6; i++) {
				//sums of left right, top etc rays for normalization
				data[i] += rays[k].data[i];
				//					assert(data[i] < In.MAX_VALUE && data[i] >= 0):"Ray: "+rays[k].data[i];
			}
		}
		for (int i = 0; i < 6; i++) {
			//sums of left right, top etc rays for normalization
			dataInv[i] = 1.0f / data[i];
			//					assert(data[i] < In.MAX_VALUE && data[i] >= 0):"Ray: "+rays[k].data[i];
		}

	}
}
