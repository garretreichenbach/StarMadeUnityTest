package org.schema.game.client.view.cubes.occlusion;

import org.schema.common.FastMath;
import org.schema.game.common.data.element.Element;

public class Ray {

	public byte points[]; //[point_count]
	;
	public float depths[]; //[point_count]
	float[] data = new float[6];
	int point_count;

	public Ray(int point_count) {
		this.point_count = point_count;

	}

	public void compute(float x, float y, float z) {
		points = new byte[point_count * 3];
		depths = new float[point_count];

		togrid(x, y, z, points, depths);

		float scale = 1.0f;
		//lamberts law per side
		data[Element.RIGHT] = ((x < 0 ? -x : 0.0f) * scale);
		data[Element.LEFT] = ((x > 0 ? x : 0.0f) * scale);

		data[Element.TOP] = ((y < 0 ? -y : 0.0f) * scale);
		data[Element.BOTTOM] = ((y > 0 ? y : 0.0f) * scale);

		data[Element.FRONT] = ((z < 0 ? -z : 0.0f) * scale);
		data[Element.BACK] = ((z > 0 ? z : 0.0f) * scale);

		//average per side
		//		        this.right = 1.0 if x < 0 else 0.0f;
		//		        this.left = 1.0 if x > 0 else 0.0f;
		//
		//		        this.top = 1.0 if y < 0 else 0.0f;
		//		        this.bottom = 1.0 if y > 0 else 0.0f;
		//
		//		        this.front  = 1.0 if z < 0 else 0.0f;
		//		        this.back = 1.0 if z > 0 else 0.0f;

		//energy influx
		//		    	data[Element.RIGHT] = 1.0f;
		//		    	data[Element.LEFT] = 1.0f;
		//
		//		    	data[Element.TOP] = 1.0f;
		//		    	data[Element.BOTTOM] = 1.0f;
		//
		//		    	data[Element.FRONT]  = 1.0f;
		//		    	data[Element.BACK] = 1.0f;

	}

	private void togrid(float dirX, float dirY, float dirZ, byte[] points, float[] depths) {
		float scaleDirX = dirX * 0.3f;
		float scaleDirY = dirY * 0.3f;
		float scaleDirZ = dirZ * 0.3f;

		dirX = 0.0f;
		dirY = 0.0f;
		dirZ = 0.0f;

		int cx = 0;
		int cy = 0;
		int cz = 0;

		int pointLen = 0;
		int dLen = 0;
		while (pointLen < point_count * 3) {
			int ncx = FastMath.round(dirX);
			int ncy = FastMath.round(dirY);
			int ncz = FastMath.round(dirZ);
			if (ncx != cx || ncy != cy || ncz != cz) {

				int rToX = ncx;
				int rToY = ncy;
				int rToZ = ncz;

				while (pointLen < point_count * 3 && (rToX != cx || rToY != cy || rToZ != cz)) {

					if (rToX != cx) {
						if (cx < rToX) {
							cx++;
						} else {
							cx--;
						}
					} else if (rToY != cy) {
						if (cy < rToY) {
							cy++;
						} else {
							cy--;
						}
					} else if (rToZ != cz) {
						if (cz < rToZ) {
							cz++;
						} else {
							cz--;
						}
					}
					float depth = FastMath.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
					//					cx = ncx;
					//					cy = ncy;
					//					cz = ncz;
					points[pointLen + 0] = (byte) cx;
					points[pointLen + 1] = (byte) cy;
					points[pointLen + 2] = (byte) cz;
					depths[dLen] = 1f / depth;
					pointLen += 3;
					dLen++;

				}
			}
			dirX += scaleDirX;
			dirY += scaleDirY;
			dirZ += scaleDirZ;
		}
	}
}
