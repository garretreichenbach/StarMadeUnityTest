package org.schema.game.server.controller.world.factory.regions;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;

public class PyramidRegion extends Region {
	private Vector3i relPos = new Vector3i();
	private Vector3i relMax = new Vector3i();
	private Vector3i relMin = new Vector3i();
	private Vector3i midTop = new Vector3i();
	private Vector3i quadMin = new Vector3i();
	private Vector3i quadMax = new Vector3i();
	private Vector3f d = new Vector3f();
	private Vector3f d2 = new Vector3f();
	private boolean intact;

	public PyramidRegion(boolean intact, Region[] regions, Vector3i min, Vector3i max,
	                     int priority, int orientation) {
		super(regions, min, max, priority, orientation);
		this.intact = intact;
	}

	@Override
	protected short placeAlgorithm(Vector3i pos) {
		getRelativePos(pos, relPos, true);
		getRelativePos(max, relMax, true);
		getRelativePos(min, relMin, true);

		relBB(relMin, relMax);
		midTop.set((relMax.x - relMin.x) / 2, relMax.y, (relMax.z - relMin.z) / 2);

		int down = (relMax.y - relPos.y) - 1;

		if (midTop.equals(relPos) || (relPos.x >= midTop.x - down && relPos.x <= midTop.x + down &&
				relPos.z >= midTop.z - down && relPos.z <= midTop.z + down)) {
			if (down > (relMax.y - relMin.y) / 4 && relPos.y > 1) {
				if (relPos.x >= midTop.x - down + 2 && relPos.x <= midTop.x + down - 2 &&
						relPos.z >= midTop.z - down + 2 && relPos.z <= midTop.z + down - 2) {

					int heightFac = 8;
					if ((((relMax.x - relMin.x) / 4 == relPos.x) && ((relMax.z - relMin.z) / 4 == relPos.z))) {
						if (relPos.y < (relMax.y - relMin.y) / heightFac) {
							return ElementKeyMap.POWER_ID_OLD;
						} else if (relPos.y == (relMax.y - relMin.y) / heightFac) {
							return ElementKeyMap.LIGHT_ID;
						}
					}
					if ((((relMax.x - relMin.x) / 4 == relPos.x) && (((relMax.z - relMin.z) / 4) * 3 == relPos.z))) {
						if (relPos.y < (relMax.y - relMin.y) / heightFac) {
							return ElementKeyMap.POWER_ID_OLD;
						} else if (relPos.y == (relMax.y - relMin.y) / heightFac) {
							return ElementKeyMap.LIGHT_ID;
						}
					}
					if (((((relMax.x - relMin.x) / 4) * 3 == relPos.x) && (((relMax.z - relMin.z) / 4) == relPos.z))) {
						if (relPos.y < (relMax.y - relMin.y) / heightFac) {
							return ElementKeyMap.POWER_ID_OLD;
						} else if (relPos.y == (relMax.y - relMin.y) / heightFac) {
							return ElementKeyMap.LIGHT_ID;
						}
					}
					if (((((relMax.x - relMin.x) / 4) * 3 == relPos.x) && (((relMax.z - relMin.z) / 4) * 3 == relPos.z))) {
						if (relPos.y < (relMax.y - relMin.y) / heightFac) {
							return ElementKeyMap.POWER_ID_OLD;
						} else if (relPos.y == (relMax.y - relMin.y) / heightFac) {
							return ElementKeyMap.LIGHT_ID;
						}
					}

					return Element.TYPE_NONE;
				}
			}

			if (intact) {

			}
			return ElementKeyMap.HULL_COLOR_YELLOW_ID;
		}

		return Element.TYPE_ALL;

	}

}
