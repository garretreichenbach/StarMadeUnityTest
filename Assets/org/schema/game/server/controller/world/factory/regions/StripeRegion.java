package org.schema.game.server.controller.world.factory.regions;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;

public class StripeRegion extends Region {

	private Vector3i relPos = new Vector3i();
	private Vector3i relMax = new Vector3i();
	private Vector3i relMin = new Vector3i();
	private int dockOrientation;
	private Vector3i tmp = new Vector3i();
	private Vector3i corePosition;
	private short stripeType;

	public StripeRegion(Region[] regions, Vector3i min, Vector3i max,
	                    int priority, int orientation, int cutOrientation, Vector3i corePosition, short stripeType) {
		super(regions, min, max, priority, orientation);
		this.dockOrientation = cutOrientation;
		this.corePosition = corePosition;
		this.stripeType = stripeType;
		;
	}

	@Override
	protected short placeAlgorithm(Vector3i pos) {
		getRelativePos(pos, relPos, true);
		getRelativePos(max, relMax, true);
		getRelativePos(min, relMin, true);

		relBB(relMin, relMax);

		tmp.set(corePosition);
		Vector3i dir = Element.DIRECTIONSi[dockOrientation];
		tmp.add(dir);

		if ((dir.x > 0 && pos.x > corePosition.x) || (dir.x < 0 && pos.x < corePosition.x) ||
				(dir.y > 0 && pos.y > corePosition.y) || (dir.y < 0 && pos.y < corePosition.y) ||
				(dir.z > 0 && pos.z > corePosition.z) || (dir.z < 0 && pos.z < corePosition.z)
				) {
			//not part of weapon
			return Element.TYPE_ALL;
		} else {
			tmp.set(corePosition);
			tmp.sub(dir);

			if ((relPos.x == (relMax.x - relMin.x) / 2 ||
					relPos.y == (relMax.y - relMin.y) / 2)) {
				if (relPos.z > relMax.z - 3 && relPos.z < relMax.z - 1) {
					return ElementKeyMap.LIGHT_RED;
				} else {
					if (relPos.z < relMax.z - 1) {
						return ElementKeyMap.TERRAIN_ICEPLANET_CRYSTAL;
					}
				}
			}
			if (relPos.z % 2 == 0 && relPos.z < relMax.z - 4) {
				return stripeType;
			}
		}

		return Element.TYPE_ALL;

	}
}