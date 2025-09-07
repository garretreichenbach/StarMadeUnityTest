package org.schema.schine.graphicsengine.forms.quadtree;

public class HeightMapInfo {
	//
	// HeightMapInfo
	//
	int[] Data;
	int XOrigin, ZOrigin;
	int XSize, ZSize;
	int RowWidth;
	int Scale;

	float Sample(int x, int z)
	// Returns the height (y-value) of a point in this heightmap.  The given (x,z) are in
	// world coordinates.  Heights outside this heightmap are considered to be 0.  Heights
	// between sample points are bilinearly interpolated from surrounding points.
	// xxx deal with edges: either force to 0 or over-size the query region....
	{
		// Break coordinates into grid-relative coords (ix,iz) and remainder (rx,rz).

		int ix = (x - XOrigin) >> Scale;
		int iz = (z - ZOrigin) >> Scale;

		int mask = (1 << Scale) - 1;

		int rx = (x - XOrigin) & mask;
		int rz = (z - ZOrigin) & mask;

		if (ix < 0 || ix >= XSize - 1 || iz < 0 || iz >= ZSize - 1) {
			return 0;    // Outside the grid.
		}

		float fx = (float) (rx) / (mask + 1);
		float fz = (float) (rz) / (mask + 1);

		float s00 = Data[ix + iz * RowWidth];
		float s01 = Data[(ix + 1) + iz * RowWidth];
		float s10 = Data[ix + (iz + 1) * RowWidth];
		float s11 = Data[(ix + 1) + (iz + 1) * RowWidth];

		return (s00 * (1 - fx) + s01 * fx) * (1 - fz) +
				(s10 * (1 - fx) + s11 * fx) * fz;
	}
}
