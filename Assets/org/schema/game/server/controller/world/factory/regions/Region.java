package org.schema.game.server.controller.world.factory.regions;

import java.util.List;
import java.util.Map;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.Element;

import com.bulletphysics.linearmath.AabbUtil2;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class Region {
	protected final Vector3i min, max;
	private final int priority;
	private final int orientation;
	public int optimizeFactor;
	private Region[] overlapping;
	private Region[] regions;

	public Region(Region[] regions, Vector3i min, Vector3i max, int priority, int orientation) {
		super();
		this.regions = regions;

		this.priority = priority;

		this.orientation = orientation;

		if (orientation % 2 != 0) {
			int z = min.x;
			min.x = min.z;
			min.z = z;

			int zm = max.x;
			max.x = max.z;
			max.z = zm;
		}
		relBB(min, max);

		this.min = min;
		this.max = max;
	}

	public void calculateOverlapping() {
		ObjectArrayList<Region> overlapping = new ObjectArrayList<Region>();

		Vector3f aMin = new Vector3f(min.x, min.y, min.z);
		Vector3f aMax = new Vector3f(max.x, max.y, max.z);

		Vector3f bMin = new Vector3f();
		Vector3f bMax = new Vector3f();
		for (int i = 0; i < getRegions().length; i++) {
			Region test = getRegions()[i];
			bMin.set(test.min.x, test.min.y, test.min.z);
			bMax.set(test.max.x, test.max.y, test.max.z);

			if (test != this && AabbUtil2.testAabbAgainstAabb2(aMin, aMax, bMin, bMax)) {
				overlapping.add(test);
			}
		}

		this.overlapping = new Region[overlapping.size()];
		for (int i = 0; i < overlapping.size(); i++) {
			this.overlapping[i] = overlapping.get(i);
		}
		overlapping = null;
	}

	public void calculateOverlappingOptimized(Map<Vector3i, List<Region>> map, int factor) {
		this.optimizeFactor = factor;
		calculateOverlapping();

		int mXCalc = (min.x + Short.MAX_VALUE) / factor;
		int mYCalc = (min.y + Short.MAX_VALUE) / factor;
		int mZCalc = (min.z + Short.MAX_VALUE) / factor;
		int maXCalc = (max.x + Short.MAX_VALUE) / factor;
		int maYCalc = (max.y + Short.MAX_VALUE) / factor;
		int maZCalc = (max.z + Short.MAX_VALUE) / factor;
		Vector3i pos = new Vector3i();

		for (int z = mZCalc; z <= maZCalc; z++) {
			for (int y = mYCalc; y <= maYCalc; y++) {
				for (int x = mXCalc; x <= maXCalc; x++) {
					pos.set(x, y, z);
					List<Region> list = map.get(pos);
					if (list == null) {
						list = new ObjectArrayList();
						map.put(new Vector3i(pos), list);
					}
					list.add(this);
				}
			}

		}
	}

	public boolean contains(Vector3i pos) {
		return pos.x < max.x && pos.x >= min.x &&
				pos.y < max.y && pos.y >= min.y &&
				pos.z < max.z && pos.z >= min.z;
	}

	public short deligate(Vector3i pos) {
		for (int i = 0; i < overlapping.length; i++) {
			if (overlapping[i].priority > priority && overlapping[i].contains(pos)) {
				//this will move up to the highest priority recursively
				short t = overlapping[i].deligate(pos);
				if (t != Element.TYPE_ALL) {
					return t;
				}
			}
		}
		short t = placeAlgorithm(pos);
		for (int i = 0; i < overlapping.length && (t == Element.TYPE_NONE || t == Element.TYPE_ALL); i++) {
			if (overlapping[i].priority == priority && overlapping[i].contains(pos)) {
				t = overlapping[i].placeAlgorithm(pos);
			}
		}
		return t;
	}

	public byte getBlockOrientation(Vector3i pos) {
		return -1;
	}

	public Region[] getOverlapping() {
		return overlapping;
	}

	/**
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * @return the regions
	 */
	public Region[] getRegions() {
		return regions;
	}

	/**
	 * @param regions the regions to set
	 */
	public void setRegions(Region[] regions) {
		this.regions = regions;
	}

	protected Vector3i getRelativePos(Vector3i pos, Vector3i out, boolean orientate) {
		out.set(pos);
		out.sub(min);

		if (orientate) {
			/*
			 * clockwise rotation around y axis
			 */
			switch (orientation) {
				case (0):
					break;

				case (1): {
					int z = out.x;
					out.x = out.z;
					out.z = z - 1;
					break;
				}

				case (2): {
					out.z = ((max.z - min.z)) - out.z;
					break;
				}

				case (3): {
					int z = out.x;
					out.x = out.z;
					out.z = z;
					out.z = (max.z - min.z) - out.z;
					break;
				}
			}
		}
		return out;
	}

	protected abstract short placeAlgorithm(Vector3i pos);

	public void relBB(Vector3i relMin, Vector3i relMax) {
		if (relMax.x < relMin.x) {
			int m = relMax.x + 1;
			relMax.x = relMin.x + 1;
			relMin.x = m;
		}
		if (relMax.y < relMin.y) {
			int m = relMax.y + 1;
			relMax.y = relMin.y + 1;
			relMin.y = m;
		}
		if (relMax.z < relMin.z) {
			int m = relMax.z + 1;
			relMax.z = relMin.z + 1;
			relMin.z = m;
		}
	}

}
