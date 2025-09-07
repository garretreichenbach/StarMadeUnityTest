package org.schema.game.common.controller;

import org.schema.common.util.linAlg.Vector3i;

public class DimensionFilter {
	private int[] f = new int[3];
	private boolean[] b = new boolean[3];

	public DimensionFilter() {
	}

	public int getXFilter() {
		return f[0];
	}

	public void setXFilter(int x) {
		f[0] = x;
		b[0] = true;
	}

	public int getYFilter() {
		return f[1];
	}

	public void setYFilter(int y) {
		f[1] = y;
		b[1] = true;
	}

	public int getZFilter() {
		return f[2];
	}

	public void setZFilter(int z) {
		f[2] = z;
		b[2] = true;
	}

	public boolean hasXFilter() {
		return b[0];
	}

	public boolean hasYFilter() {
		return b[1];
	}

	public boolean hasZFilter() {
		return b[2];
	}

	public boolean isValid(Vector3i p) {

		if (b[0]) {
			if (p.x != f[0]) {
				System.err.println("X valid " + p.x + "/" + f[0] + " ");
			}
			return p.x == f[0];
		}
		if (b[1]) {
			if (p.y != f[1]) {
				System.err.println("Y valid " + p.y + "/" + f[1] + " ");
			}
			return p.y == f[1];
		}
		if (b[2]) {
			if (p.z != f[2]) {
				System.err.println("Z valid " + p.z + "/" + f[2] + " ");
			}
			return p.z == f[2];
		}
		return true;
	}
}
