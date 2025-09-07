package org.schema.game.client.view.cubes.occlusion;

public class Offset {
	public float depth;
	public int x, y, z;

	public Offset(float depth, int cx, int cy, int cz) {
		this.x = cx;
		this.y = cy;
		this.z = cz;
		this.depth = depth;
	}

	@Override
	public String toString() {
		return ("(" + x + ", " + y + ", " + z + "; " + depth + ")");
	}
}
